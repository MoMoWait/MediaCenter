package com.rockchips.mediacenter.service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.support.contentdirectory.callback.Browse;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.container.Container;
import org.json.JSONObject;
import com.rockchips.mediacenter.bean.Device;
import com.rockchips.mediacenter.bean.DeviceScanInfo;
import com.rockchips.mediacenter.bean.FileInfo;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.modle.db.FileInfoService;
import com.rockchips.mediacenter.utils.MediaFileUtils;
import android.util.Log;
/**
 * @author GaoFei
 * Upnp文件扫描器
 */
public class UpnpFileScanThread extends Thread {
	public static final String TAG = UpnpFileScanThread.class.getSimpleName();
	private DeviceMonitorService mService;
	private RemoteDevice mRemoteDevice;
	private AndroidUpnpService mUpnpService;
	/**
	 * Upnp原始目录列表
	 */
	
	private LinkedList<FileInfo> mDirFileInfos = new LinkedList<FileInfo>();
	/**
	 * Upnp扫描时，暂存目录表
	 */
	public Map<String, FileInfo> mTmpDirFileInfos = new HashMap<>();
	/**
	 * 排序依据
	 */
	private SortCriterion[] mSortCriterions =  {new SortCriterion(true, "dc:title")};
	/**
	 * 目录服务
	 */
	private Service mContentDirectoryService;
	/**
	 * 是否正则打开目录
	 */
	private volatile boolean mIsOpenDirectory;
	
	private FileInfoService mFileInfoService;
	private Device mDevice;
	public UpnpFileScanThread(DeviceMonitorService service, Device device){
		this.mService =service;
		this.mRemoteDevice = mService.getRemoteDevices().get(device.getLocalMountPath());
		this.mUpnpService = mService.getUpnpService();
		this.mDevice = device;
		mFileInfoService = new FileInfoService();
		if(mRemoteDevice != null){
			mContentDirectoryService = mRemoteDevice.findService(new UDAServiceType("ContentDirectory"));
		}
		FileInfo rootFileInfo = createRootFileInfo();
		mDirFileInfos.add(rootFileInfo);
		mTmpDirFileInfos.put("0", rootFileInfo);
	}
	
	@Override
	public void run() {
		if(mRemoteDevice == null || mContentDirectoryService == null)
			return;
		long startScanTime = System.currentTimeMillis();
		Log.i(TAG, "start Time " + startScanTime);
		DeviceScanInfo deviceScanInfo = mService.getDeviceScanInfo(mDevice.getDeviceID());
		if(deviceScanInfo == null)
			return;
		while(!mDirFileInfos.isEmpty()){
			deviceScanInfo = mService.getDeviceScanInfo(mDevice.getDeviceID());
		    //获取设备扫描信息
			if(deviceScanInfo == null){
				//设备已经下线，不扫描直接返回
				Log.i(TAG, mDevice.getDeviceName() + "is offline or stop scanner");
				return;
			}
			if(mIsOpenDirectory)
				continue;
			FileInfo dirFileInfo = mDirFileInfos.remove(0);
			Container container = createContainerFromFileInfo(dirFileInfo);
			mIsOpenDirectory = true;
			if(mContentDirectoryService == null)
				 return;
			mUpnpService.getControlPoint().execute(new FileBrowser(mContentDirectoryService, container.getId(), BrowseFlag.DIRECT_CHILDREN, "*", 0, 100000L, mSortCriterions));
			if(mDirFileInfos.isEmpty()){
				try {
					//尝试等待5s
					TimeUnit.SECONDS.sleep(5L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		long endScanTime = System.currentTimeMillis();
		Log.i(TAG, "end Time " + endScanTime);
		Log.i(TAG, "end Time " + (endScanTime - startScanTime) / 1000 + "s");
	}
	
	
	/**
	 * 
	 * @author GaoFei
	 *
	 */
	
	class FileBrowser extends Browse{
		public FileBrowser(Service service, String objectID, BrowseFlag flag,
				String filter, long firstResult, Long maxResults,
				SortCriterion[] orderBy) {
			super(service, objectID, flag, filter, firstResult, maxResults, orderBy);
		}

		@Override
		public void received(ActionInvocation actionInvocation, DIDLContent didl) {
			List<FileInfo> allFileInfos = MediaFileUtils.getFileInfos(didl, mDevice);
			if(allFileInfos != null && allFileInfos.size() > 0){
				List<FileInfo> mTempFileInfos = new ArrayList<>();
				Set<String> parentIDs = new HashSet<>();
				for(FileInfo itemFileInfo : allFileInfos){
					try{
						JSONObject otherInfoObject = new JSONObject(itemFileInfo.getOtherInfo());
						if(itemFileInfo.getType() == ConstData.MediaType.FOLDER && itemFileInfo.getChildCount() > 0){
							mTmpDirFileInfos.put(otherInfoObject.getString(ConstData.UpnpFileOhterInfo.ID), itemFileInfo);
							mDirFileInfos.add(itemFileInfo);
						}else{
						    String parentID = otherInfoObject.getString(ConstData.UpnpFileOhterInfo.PARENT_ID);
						    parentIDs.add(parentID);
						    itemFileInfo.setParentPath(mTmpDirFileInfos.get(parentID).getPath());
						    mTempFileInfos.add(itemFileInfo);
						    if(itemFileInfo.getType() == ConstData.MediaType.VIDEO){
						    	int videoCount = mTmpDirFileInfos.get(parentID).getVideoCount();
						    	mTmpDirFileInfos.get(parentID).setVideoCount(videoCount + 1);
						    }else if(itemFileInfo.getType() == ConstData.MediaType.AUDIO){
						    	int audioCount = mTmpDirFileInfos.get(parentID).getMusicCount();
						    	mTmpDirFileInfos.get(parentID).setMusicCount(audioCount + 1);
						    }else if(itemFileInfo.getType() == ConstData.MediaType.IMAGE){
						    	int imageCount = mTmpDirFileInfos.get(parentID).getImageCount();
						    	mTmpDirFileInfos.get(parentID).setImageCount(imageCount + 1);
						    }
						    	
						}
					}catch (Exception e){
						Log.e(TAG, "FileBrowser->received->exception:" + e);
					}
				}
				Iterator<String> parentIDIterator = parentIDs.iterator();
				while(parentIDIterator.hasNext()){
					String itemID = parentIDIterator.next();
					mTempFileInfos.add(mTmpDirFileInfos.get(itemID));
					mTmpDirFileInfos.remove(itemID);
				}
				//Log.i(TAG, "FileBrowser->received->mTempFileInfos:" + mTempFileInfos);
				if(mTempFileInfos != null && mTempFileInfos.size() > 0)
					mFileInfoService.saveAll(mTempFileInfos);
			}
			mIsOpenDirectory = false;
		}

		@Override
		public void updateStatus(Status status) {
			
		}
		

		@Override
		public void failure(ActionInvocation invocation,
				UpnpResponse operation, String defaultMsg) {
			mIsOpenDirectory = false;
		}
		
	}
	
	/**
	 * 从文件信息中获取Container信息
	 * @return
	 */
	private Container createContainerFromFileInfo(FileInfo fileInfo){
		Container container = new Container();
		String otherInfo = fileInfo.getOtherInfo();
		try{
			JSONObject otherJsonObject = new JSONObject(otherInfo);
			String containerID = otherJsonObject.getString(ConstData.UpnpFileOhterInfo.ID);
			container.setId(containerID);
			if(!"0".equals(otherJsonObject)){
				container.setParentID(otherJsonObject.getString(ConstData.UpnpFileOhterInfo.PARENT_ID));
				container.setChildCount(fileInfo.getChildCount());
			}
			container.setTitle(fileInfo.getName());
		}catch (Exception e){
			Log.e(TAG, "createFromFileInfo->createFromFileInfo->exception:" + e);
		}
		
		return container;
	}
	
	/**
	 * 创建根目录
	 * @return
	 */
	private FileInfo createRootFileInfo(){
		FileInfo fileInfo = new FileInfo();
		fileInfo.setDeviceID(mDevice.getDeviceID());
		fileInfo.setName(mDevice.getDeviceName());
		fileInfo.setType(ConstData.MediaType.FOLDER);
		try{
			JSONObject jsonInfo = new JSONObject();
			jsonInfo.put(ConstData.UpnpFileOhterInfo.ID, "0");
			jsonInfo.put(ConstData.UpnpFileOhterInfo.PARENT_ID, "0");
			jsonInfo.put(ConstData.UpnpFileOhterInfo.DATE, "2017-3-13");
			fileInfo.setOtherInfo(jsonInfo.toString());
		}catch (Exception e){
			
		}
		return fileInfo;
	}
}
