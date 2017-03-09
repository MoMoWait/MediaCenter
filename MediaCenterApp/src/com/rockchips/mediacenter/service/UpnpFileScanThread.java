package com.rockchips.mediacenter.service;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.support.contentdirectory.callback.Browse;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.DIDLObject.Property;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;
import com.rockchips.mediacenter.bean.UpnpFile;
import com.rockchips.mediacenter.bean.UpnpFolder;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.modle.db.LocalDeviceService;
import com.rockchips.mediacenter.modle.db.UpnpFileService;
import com.rockchips.mediacenter.modle.db.UpnpFolderService;
import com.rockchips.mediacenter.utils.MediaUtils;

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
	
	private List<Container> mContainers = new LinkedList<Container>();

	/**
	 * Upnp封装目录列表
	 */
	private List<UpnpFolder> mUpnpFolders = new ArrayList<UpnpFolder>();
	/**
	 * Upnp原始目录表，用Map<id, directory>的形式，加快搜索速度
	 */
	public Map<String, Container> mContainerMap = new HashMap<String, Container>();
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
	public UpnpFileScanThread(RemoteDevice remoteDevice, DeviceMonitorService service, AndroidUpnpService upnpService){
		this.mService =service;
		this.mRemoteDevice = remoteDevice;
		this.mUpnpService = upnpService;
		mUpnpFileService = new UpnpFileService();
		mUpnpFolderService = new UpnpFolderService();
		mLocalDeviceService = new LocalDeviceService();
		mContentDirectoryService = mRemoteDevice.findService(new UDAServiceType("ContentDirectory"));
		/*Log.i(TAG, "mContentDirectoryService->serviceType:" + mContentDirectoryService.getServiceType()); 
		Log.i(TAG, "mContentDirectoryService->serviceId:" + mContentDirectoryService.getServiceId());
		Log.i(TAG, "mContentDirectoryService->className:" + mContentDirectoryService.getClass().getName());
		RemoteService remoteService = (RemoteService)mContentDirectoryService;
		Log.i(TAG, "remoteService->descriptorURI:" + remoteService.getDescriptorURI());
		Log.i(TAG, "remoteService->controlURI:" + remoteService.getControlURI());
		Log.i(TAG, "remoteService->enventSubScriptionURI:" + remoteService.getEventSubscriptionURI());*/
		Container rootContainer = createRootContainer();
		mContainers.add(rootContainer);
		mContainerMap.put(rootContainer.getId(), rootContainer);
	}
	
	@Override
	public void run() {
		//Log.i(TAG, getName() + "->UpnpFileScanThread->start");
		while(isServiceRunning){
			if(!mService.isMounted(mRemoteDevice.getIdentity().getDescriptorURL().toString())){
				//删除数据库对应的数据
				mLocalDeviceService.deleteDeviceByPath(mRemoteDevice.getIdentity().getDescriptorURL().toString());
				mUpnpFileService.deleteFilesByDeviceId(mRemoteDevice.getIdentity().getUdn().getIdentifierString());
				mUpnpFolderService.deleteFoldersByDeviceId(mRemoteDevice.getIdentity().getUdn().getIdentifierString());
				mUpnpFiles.clear();
				mUpnpFolders.clear();
				isServiceRunning = false;
				if(mCurrentBrowser != null)
					mCurrentBrowser.isEnd = true;
				continue;
			}
			//Log.i(TAG, "run->mUpnpFolders:" + mUpnpFolders);
			//Log.i(TAG, "run->mUpnpFiles:" + mUpnpFiles);
			synchronized (mUpnpFiles) {
				if(mUpnpFiles.size() >= 1){
					mUpnpFileService.saveAll(mUpnpFiles);
					//文件入库
					mUpnpFiles.clear();
				}
			}
			
			synchronized (mUpnpFolders) {
				if(mUpnpFolders.size() >= 1){
					mUpnpFolderService.saveAll(mUpnpFolders);
					//文件夹入库
					mUpnpFolders.clear();
				}
			}
			
			
			if(mContainers.isEmpty()){
			    try{
	                while(MediaUtils.hasMediaClient()){
	                    //睡眠1s
	                    Thread.sleep(1000);
	                }
	            }catch (Exception e){
	                Log.i(TAG, "UpnpFileScanThread->exception:" + e);
	            }
				if(isWaitTenSecond){
					//表示已经等待10s，退出线程
					isServiceRunning = false;
					continue;
				}
				try{
					Thread.sleep(10000);
					isWaitTenSecond = true;
					continue;
				}catch (Exception e){
					Log.i(TAG, "UpnpFileScanThread->waitSecond:" + e);
				}
			}else{
				isWaitTenSecond = false;
				long currentTime = System.currentTimeMillis();
				if(mStartTimeContainers.get(mCurrentOpenContainer) != null){
					long subTime = currentTime - mStartTimeContainers.get(mCurrentOpenContainer);
					//多于10s
					if(subTime >= 10000){
						mIsOpenDirectory = false;
						//移除时间记录
						mStartTimeContainers.remove(mCurrentOpenContainer);
						if(mCurrentBrowser != null)
							mCurrentBrowser.isEnd = true;
					}
				
				}
				if(!mIsOpenDirectory){
					mIsOpenDirectory = true;
					Container itemContainer = mContainers.remove(0);
					mCurrentBrowser = new FileBrowser(mContentDirectoryService, itemContainer.getId(), BrowseFlag.DIRECT_CHILDREN, "*", 0, 100000L, mSortCriterions);
					mCurrentOpenContainer = itemContainer;
					mStartTimeContainers.put(itemContainer, System.currentTimeMillis());
					mUpnpService.getControlPoint().execute(mCurrentBrowser);
				}else{
					try{
						Thread.sleep(1000);
					}catch (Exception e){
						Log.i(TAG, "UpnpFileScanThread->run exception:" + e);
					}
				}
				
			}
		} 
		synchronized (mUpnpFiles) {
			if(!mUpnpFiles.isEmpty()){
				mUpnpFileService.saveAll(mUpnpFiles);
				mUpnpFiles.clear();
			}
		}
		synchronized (mUpnpFolders) {
			if(!mUpnpFolders.isEmpty()){
				mUpnpFolderService.saveAll(mUpnpFolders);
				mUpnpFolders.clear();
			}
		}
		
		//Log.i(TAG, "UpnpFileScanThred->end");
	}
	
	private Container createRootContainer() {
		Container rootContainer = new Container();
		rootContainer.setId("0");
		rootContainer.setTitle(mRemoteDevice.getDetails().getFriendlyName());
		return rootContainer;
	}
	
	
	
	/**
	 * 将Upnp原始目录转换成封装目录
	 * @param container
	 * @return
	 */
	private UpnpFolder containerToFolder(Container container){
		if(mUpnpFolderMap.get(container.getId())[0] == 0)
			return null;
		UpnpFolder folder = new UpnpFolder();
		folder.setItmeId(container.getId());
		folder.setDeviceID(mRemoteDevice.getIdentity().getUdn().getIdentifierString());
		folder.setDevicetype(ConstData.DeviceType.DEVICE_TYPE_DMS);
		folder.setFileCount(mUpnpFolderMap.get(container.getId())[0]);
		folder.setAudioCount(mUpnpFolderMap.get(container.getId())[1]);
		folder.setVideoCount(mUpnpFolderMap.get(container.getId())[2]);
		folder.setImageCount(mUpnpFolderMap.get(container.getId())[3]);
		folder.setLast_modify_date(0);
		folder.setName(container.getTitle());
		folder.setParentId(container.getParentID());
		folder.setPath("");
		folder.setPhysic_dev_id(mRemoteDevice.getDetails().getFriendlyName());	
		return folder;
	}
	
	
	/**
	 * 将Upnp原始文件转化成封装文件
	 * @return
	 */
	private UpnpFile itemToFile(Item item){
		UpnpFile upnpFile = new UpnpFile();
		List<Res> resources = item.getResources();
		if(resources != null &&  resources.size() > 0 && resources.get(0) != null && resources.get(0).getProtocolInfo() != null
				&& resources.get(0).getProtocolInfo().getContentFormat() != null){
			String contentFormat = resources.get(0).getProtocolInfo().getContentFormat();
			//Log.i(TAG, "UpnpFileScanThread->itemToFile->contentFormat:" + contentFormat);
			List<Property> properties = item.getProperties();
			/**
			 * 设置属性值
			 */
			if(properties != null && properties.size() > 0){
				for(Property itemProperty : properties){
					try {
						Field field = upnpFile.getClass().getDeclaredField(itemProperty.getDescriptorName());
						field.setAccessible(true);
						field.set(upnpFile, itemProperty.getValue().toString());
					} catch (NoSuchFieldException e) {
						//Log.i(TAG, "itemToFile exception:" + e);
						//e.printStackTrace();
					} catch (Exception e) {
						//Log.i(TAG, "itemToFile exception:" + e);
					}
				}
				
			}
			//Log.i(TAG, "itemToFile->5");
			
			if (resources.get(0).getBitrate() != null)
				upnpFile.setBitrate(resources.get(0).getBitrate());
			if (resources.get(0).getBitsPerSample() != null)
				upnpFile.setBitsPerSample(resources.get(0).getBitsPerSample());
			upnpFile.setDeviceID(mRemoteDevice.getIdentity().getUdn().getIdentifierString());
			upnpFile.setDevicetype(ConstData.DeviceType.DEVICE_TYPE_DMS);
			upnpFile.setDuration(resources.get(0).getDuration());
			upnpFile.setHeight(resources.get(0).getResolutionY());
			upnpFile.setLast_modify_date(0);
			upnpFile.setName(item.getTitle());
			if (resources.get(0).getNrAudioChannels() != null)
				upnpFile.setNrAudioChannels(resources.get(0).getNrAudioChannels());
			upnpFile.setParentId(item.getParentID());
			upnpFile.setPath(resources.get(0).getValue());
			upnpFile.setPhysic_dev_id(mRemoteDevice.getDetails().getFriendlyName());
			if (resources.get(0).getSampleFrequency() != null)
				upnpFile.setSampleFrequency(resources.get(0).getSampleFrequency());
			upnpFile.setSize(resources.get(0).getSize());
			upnpFile.setWidth(resources.get(0).getResolutionX());
			
			//Log.i(TAG, "itemToFile->6");
			Integer[] fileCounts = mUpnpFolderMap.get(item.getParentID());
			if(fileCounts == null)
				fileCounts = new Integer[]{0,0,0,0};
			//Log.i(TAG, "itemToFile->7");
			if(contentFormat.contains("audio")){
				upnpFile.setType(ConstData.MediaType.AUDIO);
				++fileCounts[0];
				++fileCounts[1];
				//Log.i(TAG, "itemToFile->8");
				mUpnpFolderMap.put(item.getParentID(), fileCounts);
			}else if(contentFormat.contains("video")){
				upnpFile.setType(ConstData.MediaType.VIDEO);
				++fileCounts[0];
				++fileCounts[2];
				//Log.i(TAG, "itemToFile->9");
				mUpnpFolderMap.put(item.getParentID(), fileCounts);
			}else if(contentFormat.contains("image")){
				upnpFile.setType(ConstData.MediaType.IMAGE);
				++fileCounts[0];
				++fileCounts[3];
				//Log.i(TAG, "itemToFile->10");
				mUpnpFolderMap.put(item.getParentID(), fileCounts);
			}else{
				//Log.i(TAG, "UpnpFileScanThread->itemToFile3:");
				return null;
			}
		}else{
			return null;
		}
		//Log.i(TAG, "UpnpFileScanThread->itemToFile4:" + upnpFile);
		return upnpFile;
	}
	
	
	private boolean isPhotoItem(Item item){
		List<Res> resources = item.getResources();
		if(resources != null &&  resources.size() > 0 && resources.get(0) != null && resources.get(0).getProtocolInfo() != null
				&& resources.get(0).getProtocolInfo().getContentFormat() != null){
			String contentFormat = resources.get(0).getProtocolInfo().getContentFormat();
			if(contentFormat.contains("image")){
				//Log.i(TAG, "itemToFile->10");
				return true;
			}
		}
	
		return false;
	}
	
	
	/**
	 * 
	 * @author GaoFei
	 *
	 */
	
	class FileBrowser extends Browse{
		
		@Override
		public void run() {
			if(!isEnd)
				super.run();
		}
		
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
			if(isEnd)
				return;
			//目录列表
			List<Container> containers = didl.getContainers();
			if(containers != null && containers.size() > 0){
				mContainers.addAll(containers);
				for(Container itemContainer : containers){
					mContainerMap.put(itemContainer.getId(), itemContainer);
				}
			}
			Item photoItem = null;
			List<Item> items = didl.getItems();
			if(items != null && items.size() > 0){
				for(Item item : items){
					if(photoItem == null && isPhotoItem(item))
						photoItem = item;
					UpnpFile upnpFile = itemToFile(item);
					if(upnpFile != null){
						synchronized (mUpnpFiles) {
							mUpnpFiles.add(upnpFile);
						}
					}
						
				}
				//item搜索完之后，对folder进行处理
				Container parentContainer = mContainerMap.get(items.get(0).getParentID());
				UpnpFolder upnpFolder = containerToFolder(parentContainer);
				if(photoItem != null){
					//设置相册的第一张图片
					upnpFolder.setFirstPhotoUrl(photoItem.getResources().get(0).getValue());
				}
				//加入列表
				if(upnpFolder != null){
					synchronized (mUpnpFolders) {
						mUpnpFolders.add(upnpFolder);
					}
				}
					
			}
			mStartTimeContainers.remove(mCurrentOpenContainer);
			mIsOpenDirectory = false;
		}

		@Override
		public void updateStatus(Status status) {
			if(isEnd)
				return;
			//Log.i(TAG, "FileBrowser->updateStatus:" + status);
		}

		@Override
		public void failure(ActionInvocation invocation,
				UpnpResponse operation, String defaultMsg) {
			if(isEnd)
				return;
			//打开目录失败
			mIsOpenDirectory = false;
			//Log.i(TAG, "FileBrowser->failure:" + operation);
			//Log.i(TAG, "FileBrowser->deaultMsg:" + defaultMsg);
		}
		
	}
}
