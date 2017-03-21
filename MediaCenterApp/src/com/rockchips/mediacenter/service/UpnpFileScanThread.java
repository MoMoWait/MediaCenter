package com.rockchips.mediacenter.service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import com.rockchips.mediacenter.bean.FileInfo;
import com.rockchips.mediacenter.bean.UpnpFile;
import com.rockchips.mediacenter.bean.UpnpFolder;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.modle.db.FileInfoService;
import com.rockchips.mediacenter.modle.db.LocalDeviceService;
import com.rockchips.mediacenter.modle.db.UpnpFileService;
import com.rockchips.mediacenter.modle.db.UpnpFolderService;
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
	 * Upnp封装目录列表
	 */
	private List<UpnpFolder> mUpnpFolders = new ArrayList<UpnpFolder>();
	/**
	 * Upnp扫描时，暂存目录表
	 */
	public Map<String, FileInfo> mTmpDirFileInfos = new HashMap<>();
	/**
	 * Upnp封装文件列表
	 */
	private List<UpnpFile> mUpnpFiles = new ArrayList<UpnpFile>();
	/**
	 * 排序依据
	 */
	private SortCriterion[] mSortCriterions =  {new SortCriterion(true, "dc:title")};
	/**
	 * 目录服务
	 */
	private Service mContentDirectoryService;
	/**
	 * 服务是否正在运行
	 */
	private boolean isServiceRunning = true;
	/**
	 * 是否已经等待10s
	 */
	private boolean isWaitTenSecond = false;
	/**
	 * Upnp文件服务
	 */
	private UpnpFileService mUpnpFileService;
	/**
	 * Upnp目录服务
	 */
	private UpnpFolderService mUpnpFolderService;
	/**
	 * 本地设备服务
	 */
	private LocalDeviceService mLocalDeviceService;
	/**
	 * id与文件数的关系
	 */
	private Map<String, Integer[]> mUpnpFolderMap = new HashMap<String, Integer[]>();
	/**
	 * 是否正则打开目录
	 */
	private boolean mIsOpenDirectory;
	/**
	 * 记录打开Container的时间
	 */
	private Map<Container, Long> mStartTimeContainers = new HashMap<Container, Long>();
	/**
	 * 当前打开的Container
	 */
	private Container mCurrentOpenContainer;
	/**
	 * 当前目录浏览
	 */
	private FileBrowser mCurrentBrowser;
	private FileInfoService mFileInfoService;
	private Device mDevice;
	public UpnpFileScanThread(DeviceMonitorService service, Device device){
		this.mService =service;
		this.mRemoteDevice = mService.getRemoteDevices().get(device.getLocalMountPath());
		this.mUpnpService = mService.getUpnpService();
		this.mDevice = device;
		mFileInfoService = new FileInfoService();
		mContentDirectoryService = mRemoteDevice.findService(new UDAServiceType("ContentDirectory"));
		FileInfo rootFileInfo = createRootFileInfo();
		mDirFileInfos.add(rootFileInfo);
		mTmpDirFileInfos.put("0", rootFileInfo);
	}
	
	@Override
	public void run() {
		while(!mDirFileInfos.isEmpty()){
			FileInfo dirFileInfo = mDirFileInfos.remove(0);
		}
	}
	
	private Container createRootContainer() {
		Container rootContainer = new Container();
		rootContainer.setId("0");
		rootContainer.setTitle(mRemoteDevice.getDetails().getFriendlyName());
		return rootContainer;
	}
	
	
	/**
	 * 
	 * @author GaoFei
	 *
	 */
	
	class FileBrowser extends Browse{
		
		/**
		 * 是否结束
		 */
		boolean isEnd = false;
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
						if(itemFileInfo.getType() == ConstData.MediaType.FOLDER){
							if(itemFileInfo.getChildCount() > 0)
								mTmpDirFileInfos.put(otherInfoObject.getString(ConstData.UpnpFileOhterInfo.ID), itemFileInfo);
						}else{
							mTempFileInfos.add(itemFileInfo);
						    String parentID = otherInfoObject.getString(ConstData.UpnpFileOhterInfo.PARENT_ID);
						    parentIDs.add(parentID);
						    if(itemFileInfo.getType() == ConstData.MediaType.VIDEO){
						    	int videoCount = mTmpDirFileInfos.get(parentID).getVideoCount();
						    	mTmpDirFileInfos.get(parentID).setVideoCount(videoCount + 1);
						    }else if(itemFileInfo.getType() == ConstData.MediaType.AUDIO){
						    	int audioCount = mTmpDirFileInfos.get(parentID).getVideoCount();
						    	mTmpDirFileInfos.get(parentID).setVideoCount(audioCount + 1);
						    }else if(itemFileInfo.getType() == ConstData.MediaType.IMAGE){
						    	int imageCount = mTmpDirFileInfos.get(parentID).getVideoCount();
						    	mTmpDirFileInfos.get(parentID).setVideoCount(imageCount + 1);
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
				mFileInfoService.saveAll(mTempFileInfos);
			}
			
			
		}

		@Override
		public void updateStatus(Status status) {
			
		}
		

		@Override
		public void failure(ActionInvocation invocation,
				UpnpResponse operation, String defaultMsg) {
			
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
			container.setId(otherJsonObject.getString(ConstData.UpnpFileOhterInfo.ID));
			container.setParentID(otherJsonObject.getString(ConstData.UpnpFileOhterInfo.PARENT_ID));
			container.setChildCount(fileInfo.getChildCount());
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
