package com.rockchip.mediacenter.mediaplayer;

import java.util.ArrayList;
import java.util.List;

import com.rockchip.mediacenter.DLNAManager;
import com.rockchip.mediacenter.DLNAService;
import com.rockchip.mediacenter.R;
import com.rockchip.mediacenter.SystemSettingUtils;
import com.rockchip.mediacenter.common.logging.Log;
import com.rockchip.mediacenter.common.logging.LogFactory;
import com.rockchip.mediacenter.common.util.StringUtils;
import com.rockchip.mediacenter.core.dlna.AsyncTaskCallback;
import com.rockchip.mediacenter.core.dlna.DLNADevice;
import com.rockchip.mediacenter.core.dlna.enumeration.MediaClassType;
import com.rockchip.mediacenter.core.dlna.enumeration.ServiceType;
import com.rockchip.mediacenter.dlna.model.DeviceItem;
import com.rockchip.mediacenter.core.dlna.protocols.response.contentdirectory.BrowseResponse;
import com.rockchip.mediacenter.core.upnp.Device;
import com.rockchip.mediacenter.core.upnp.DeviceCache;
import com.rockchip.mediacenter.core.upnp.Service;
import com.rockchip.mediacenter.dlna.dmp.DigitalMediaPlayer;
import com.rockchip.mediacenter.dlna.dmp.model.ContentItemList;
import com.rockchip.mediacenter.dlna.dmp.model.MediaItem;
import com.rockchip.mediacenter.dlna.dmp.model.ObjectIdentity;
import com.rockchip.mediacenter.dlna.dmp.model.RootIdentity;
import com.rockchip.mediacenter.mediaplayer.model.FileInfo;
import com.rockchip.mediacenter.mediaplayer.ui.WaitDialog;
import com.rockchip.mediacenter.mediaplayer.util.FileInfoRenderUtil;
import com.rockchip.mediacenter.plugins.imageloader.ImageLoader;
import com.rockchip.mediacenter.plugins.renderplay.MediaPlayListTempCache;
import com.rockchip.mediacenter.plugins.widget.Alert;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class FileExplorer implements AsyncTaskCallback<BrowseResponse> {

	private static final Log logger = LogFactory.getLog(FileExplorer.class);
	private static final boolean CONTAIN_LOCAL_DEVICE = true;//是否包含本地设备
	private static final int REFRESH_DATASOURCE = 1;
	public static final int SHOW_DIALOG = 1;
	public static final int CLOSE_DIALOG = 2;
	public static final int SHOW_OPEN_DIALOG = 3;
	private WaitDialog mWaitDialog = null;
	private String currentDeviceUID;
	private Dialog retrySearchDialog;
	
	public static final int SEARCH_INTERVAL = 3000;
	public static final int MAX_RETRY_SEARCH = 3;
	
	private MediaPlayer mMediaPlayer;
	private boolean isPauseByPlay = false;
	private boolean isPaused = false;
	private DLNAManager dlnaManager;
	private DigitalMediaPlayer dmp;
	private DeviceCache mDeviceCache;
	private ImageLoader mImageLoader;
	
	//分页请求
	private static final boolean PAGE_REQEUST_ENABLED = true;
	private static final int PAGE_SIZE = 30;
	private int startIndex;
	private String currentItemID;
	private long requestTime;
	
	public FileExplorer(MediaPlayer mediaPlayer, DLNAManager dlnaManager, ImageLoader imageLoader){
		this.dlnaManager = dlnaManager;
		mImageLoader = imageLoader;
		mMediaPlayer = mediaPlayer;
		mDeviceCache = DeviceCache.getInstance();
	}
	
	public void setDigitalMediaPlayer(DigitalMediaPlayer dmp){
		this.dmp = dmp;
	}
	
	public void onCreate(){
		registerAddOrRemoveDevice();
	}
	
	public void onResume(){
		isPaused = false;
	}
	
	public void onPause(){
		closeDialog();
		isPaused = true;
	}
	
	public void onDestroy(){
		mMediaPlayer.unregisterReceiver(mDeviceAddOrRemoveListener);
		mImageLoader.clear();
	}
	
	/**
	 * 是否因为播放而pause
	 * 如果是的话  不刷新当前列表
	 */
	public boolean isPauseByPlay(){
		return isPauseByPlay;
	}
	public void setPauseByPlay(boolean isPauseByPlay){
		this.isPauseByPlay = isPauseByPlay;
	}
	
	/**
	 * 获取DLNA内容
	 * @param path
	 */
	public void getDLNAContent(String path){
		if(mWaitDialog!=null&&mWaitDialog.isShowing()){
			return;
		}
		requestTime = System.currentTimeMillis();
		getFileControl().setCurrentPath(path);
		if(FileInfoRenderUtil.isDLNARoot(path)){
			currentItemID = "";
			getLivingDevice();
			getFileControl().setParentPath(null);
		}else if(FileInfoRenderUtil.isDLNADevice(path)){
			getFileControl().setParentPath(FileInfoRenderUtil.DLNA_DIR);
			currentDeviceUID = FileInfoRenderUtil.getDLNAFileDeviceID(path);
			Device device = mDeviceCache.getDeviceByUDN(currentDeviceUID);
			if(device==null){
				onCompleted(null, -1);
			}else{
				showWaitDialog(SHOW_OPEN_DIALOG);
				currentItemID = "0";
				startIndex = 0;
				if(PAGE_REQEUST_ENABLED){//分页请求
					dmp.browseDirectChildren(device, new RootIdentity(), startIndex, PAGE_SIZE, "*", null, this);
				}else{
					dmp.browseRootChildren(device, this);
				}
				if(SystemSettingUtils.isDLNACertified()){
					Service service = DLNADevice.getService(device, ServiceType.ConnectionManager);
					dmp.subscribe(service, 300);
					//dmp.browseMetaData(device, new ObjectIdentity("0"), null);
				}
			}
		}else if(FileInfoRenderUtil.isDLNAFile(path)){
			getFileControl().setParentPath(FileInfoRenderUtil.getDLNAFileParentPath(path));
			currentDeviceUID = FileInfoRenderUtil.getDLNAFileDeviceID(path);
			Device device = mDeviceCache.getDeviceByUDN(currentDeviceUID);
			if(device==null){
				onCompleted(null, -1);
			}else{
				showWaitDialog(SHOW_OPEN_DIALOG);
				String itemId = FileInfoRenderUtil.getDLNAFilePathID(path);
				currentItemID = itemId;
				startIndex = 0;
				if(PAGE_REQEUST_ENABLED){//分页请求
					dmp.browseDirectChildren(device, new ObjectIdentity(itemId), startIndex, PAGE_SIZE, "*", null, this);
				}else{
					dmp.browseDirectChildren(device, new ObjectIdentity(itemId), this);
				}
			}
		}
	}
	
	
	//---------------------------------Search device------------------------------------------------//
	/**
	 * 获取在线设备
	 */
	private int retryTimes;
	public void getLivingDevice(){
		retryTimes = 0;
		showWaitDialog(SHOW_DIALOG);
		searchLivingDevice();
	}
	

	private Handler searchDeviceHandler = new Handler(){
		public void handleMessage(Message msg) {
			if(msg.what==REFRESH_DATASOURCE){
				mMediaPlayer.refreshDataSource();
			}
		}
	};
	
	private Runnable searchRunnable = new Runnable(){
		public void run() {
			search();
			searchLivingDevice();
		}
	};
	
	/**
	 * 搜索在线DLNA设备
	 * @author xwf
	 */
	private void searchLivingDevice(){
		if(dmp!=null){
			List<DeviceItem> deviceItemList = refreshDeviceList();
			if(deviceItemList.size()==0&&retryTimes<MAX_RETRY_SEARCH){
				searchDeviceHandler.postDelayed(searchRunnable, SEARCH_INTERVAL*retryTimes);
			}else{
				if(retryTimes==0){
					search();
				}
				if(deviceItemList.size()==0){
					showRetrySearchDialog();
				}
				stopRefresh();
			}
			retryTimes ++;
		}else{
			Toast.makeText(mMediaPlayer, mMediaPlayer.getString(R.string.toast_disconn_service), Toast.LENGTH_SHORT).show();
		}
	}
	
	/**
	 * 广播搜索设备
	 */
	public void search(){
		dmp.search();
	}
	
	/**
	 * 停止刷新
	 */
	private void stopRefresh(){
		searchDeviceHandler.removeCallbacks(searchRunnable);
		if(mWaitDialog!=null){
			mWaitDialog.dismiss();
		}
		retryTimes = MAX_RETRY_SEARCH;
	}
	
	/**
	 * 刷新在线设备列表
	 */
	private List<DeviceItem> refreshDeviceList(){
		List<DeviceItem> deviceItemList = dlnaManager.getMediaServerDevice(CONTAIN_LOCAL_DEVICE);
		for(DeviceItem item : deviceItemList){
			handleLocalDevice(item);
		}
		ArrayList<FileInfo> fileInfoList = FileInfoRenderUtil.toFileInfo(deviceItemList);
		mMediaPlayer.setDataSource(fileInfoList, true);
		mMediaPlayer.updateTitle();
		return deviceItemList;
	}
	/**
	 * 重命名本地设备名称 
	 * @param item
	 * @return 如果是本机本应用设备返回true，否则返回false
	 */
	private boolean handleLocalDevice(DeviceItem item){
		String serverName = SystemSettingUtils.getMediaServerName(mMediaPlayer);
		if(item.isLocalDevice()){
			if(serverName.equals(item.getFriendlyName())){
				item.setFriendlyName(mMediaPlayer.getString(R.string.dmp_local_media_center));
				return true;
			}else{
				String name = String.format(mMediaPlayer.getString(R.string.dmp_local_device), item.getFriendlyName());
				item.setFriendlyName(name);
			}
		}
		return false;
	}
	
	/**
	 * 注册设备添加移除广播监听
	 */
	public void registerAddOrRemoveDevice() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(DLNAService.ACTION_RESTART_DEVICE);
		intentFilter.addAction(DLNAService.ACTION_ADD_SERVER_DEVICE);
		intentFilter.addAction(DLNAService.ACTION_REMOVE_SERVER_DEVICE);
		mMediaPlayer.registerReceiver(mDeviceAddOrRemoveListener, intentFilter);
	}
	
	/**
	 * 设备添加移除广播监听
	 */
	private BroadcastReceiver mDeviceAddOrRemoveListener = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			String path = getFileControl().getCurrentPath();
			DeviceItem deviceItem = intent.getParcelableExtra(DLNAService.KEY_DEVICE);
			if(FileInfoRenderUtil.isDLNARoot(path)){
				if(action.equals(DLNAService.ACTION_RESTART_DEVICE)){
					refreshDeviceList();
					return;
				}else{
					if(!CONTAIN_LOCAL_DEVICE&&deviceItem.isLocalDevice()){
						return;//排除本机设备
					}
					handleLocalDevice(deviceItem);
					FileInfo fileInfo = FileInfoRenderUtil.toFileInfo(deviceItem);
					if (action.equals(DLNAService.ACTION_ADD_SERVER_DEVICE)) {
						//refreshDeviceList();
						mMediaPlayer.addDataSource(fileInfo);
						closeDialog();
					} else if (action.equals(DLNAService.ACTION_REMOVE_SERVER_DEVICE)) {
						//refreshDeviceList();
						mMediaPlayer.delDataSource(fileInfo);
					}
				}
			}else if(FileInfoRenderUtil.isDLNADevice(path)||FileInfoRenderUtil.isDLNAFile(path)){
				if (action.equals(DLNAService.ACTION_REMOVE_SERVER_DEVICE)) {
					String deviceUID = FileInfoRenderUtil.getDLNAFileDeviceID(path);
					if(deviceUID!=null && deviceUID.equals(deviceItem.getUdn())&&!isPaused){
						if(isPaused){
							refreshDeviceList();
						}else{
							getDLNAContent(FileControl.TOP_DIR);
						}
					}
				}
			}
			if (action.equals(DLNAService.ACTION_REMOVE_SERVER_DEVICE)) {
				if(deviceItem.getLargestIconURL()!=null){
					mImageLoader.unload(deviceItem.getLargestIconURL());
					logger.debug("unload device item icon.");
				}
			}
		}
	};
	
	
	/**
	 * 关闭提示框
	 */
	private void closeDialog() {
		searchDeviceHandler.removeCallbacks(searchRunnable);
		if(retrySearchDialog!=null&&retrySearchDialog.isShowing()){
			retrySearchDialog.dismiss();
		}
		if(mWaitDialog!=null)
			mWaitDialog.dismiss();
	}
	
	private FileControl getFileControl(){
		return mMediaPlayer.getFileControl();
	}
	
	/**
	 * 添加查看Intent参数
	 */
	public void addIntentParameter(Intent intent, FileInfo fileItem){
		if(!fileItem.isMediaItem()) return;
		MediaItem mediaItem = (MediaItem)fileItem.getItem();
		MediaClassType itemClass = MediaClassType.getMediaClassTypeByClass(mediaItem.getObjectClass());
		ArrayList<MediaItem> mediaItemList = getMediaWithSameType(itemClass);
		int selectIndex = mediaItemList.indexOf(mediaItem);
		if(MediaClassType.IMAGE==itemClass){
			intent.setClass(mMediaPlayer, com.rockchip.mediacenter.plugins.pictureplay.PictureViewer.class);
		}else if(MediaClassType.AUDIO==itemClass){
			intent.setClass(mMediaPlayer, com.rockchip.mediacenter.plugins.musicplay.MusicPlayer.class);
		}else if(MediaClassType.VIDEO==itemClass){
			intent.setClass(mMediaPlayer, com.rockchip.mediacenter.plugins.videoplay.VideoPlayer.class);
		}
		MediaPlayListTempCache.getInstance().setPlayList(mediaItemList);
		MediaPlayListTempCache.getInstance().setPlayIndex(selectIndex);
		//intent.putExtra(MediaPlayConsts.KEY_MEDIA_LIST, mediaItemList);
		//intent.putExtra(MediaPlayConsts.KEY_MEDIA_SELECT, selectIndex);
		isPauseByPlay = true;
	}
	
	/**
	 * 返回指定类的媒体列表
	 * @param itemClass 指定媒体类型
	 * @return 返回同一类的媒体列表
	 */
	private ArrayList<MediaItem> getMediaWithSameType(MediaClassType itemClass){
		ArrayList<MediaItem> mediaItemList = new ArrayList<MediaItem>();
		List<FileInfo> fileInfoList = mMediaPlayer.getDataSource();
		for(int i=0; i<fileInfoList.size(); i++){
			FileInfo fileInfo = fileInfoList.get(i);
			if(!fileInfo.isMediaItem()) continue;
			
			MediaItem item = (MediaItem)fileInfo.getItem();
			if(!StringUtils.hasText(item.getResourceURL())){
				continue;
			}
			MediaClassType iClass = MediaClassType.getMediaClassTypeByClass(item.getObjectClass());
			if(itemClass==iClass){
				mediaItemList.add(item);
			}
		}
		return mediaItemList;
	}
	
	/**
	 * 清理缓存
	 */
	public void clearCache(boolean search){
		dmp.removeAllDevice();
		if(search){
			search();
		}
	}
	
	/** 
	 * <p>Title: onCompleted</p> 
	 * <p>Description: </p> 
	 * @param result 
	 * @see com.rockchip.mediacenter.core.dlna.AsyncTaskCallback#onCompleted(java.lang.Object, int) 
	 */
	@Override
	public void onCompleted(BrowseResponse result, int requestCode) {
		mWaitDialog.dismiss();
		if(result==null){
			mMediaPlayer.setDataSource(null, false);
		}else{
			ContentItemList contentItemList = result.getParsedResult();
			ArrayList<FileInfo> fileInfoList = FileInfoRenderUtil.toFileInfoForMedia(contentItemList);
			
			if(!PAGE_REQEUST_ENABLED){//未开启分页浏览或根目录浏览
				mMediaPlayer.setDataSource(fileInfoList, result.isSuccessed());
				return;
			}
			
			if(requestTime>result.getRequestTimestamp()||"".equals(currentItemID)){
				logger.error("Request is invalidation. "+result.getRequestTimestamp());
				return;
			}
			
			boolean isSameBrowseRequest = false;
			if(result.getRequestUserData()!=null){
				ObjectIdentity responseId = (ObjectIdentity)result.getRequestUserData();
				isSameBrowseRequest = (responseId!=null&&responseId.getId().equals(currentItemID));
			}
			
			if(isSameBrowseRequest){
//				if("0".equals(currentItemID)){
//					mMediaPlayer.setDataSource(fileInfoList, result.isSuccessed());
//					return;
				if(startIndex==0){//第一页
					mMediaPlayer.setDataSource(fileInfoList, result.isSuccessed());
				}else if(isSameBrowseRequest){//下一页
					mMediaPlayer.addDataSource(fileInfoList);
				}
			}else{
				logger.debug("New browse request. ");
			}
			
			if(result.isSuccessed()&&isSameBrowseRequest&&contentItemList.size()>0){//继续浏览下一页内容
				browseNextPage(result);
			}
		}
	}
	//浏览下一页
	private void browseNextPage(BrowseResponse result){
		int totalRec = result.getTotalMaches();
		startIndex += result.getNumberReturned();
		if(result.getNumberReturned()>0&&startIndex<totalRec){
			Device device = mDeviceCache.getDeviceByUDN(currentDeviceUID);
			if(device!=null){
				dmp.browseDirectChildren(device, new ObjectIdentity(currentItemID), startIndex, PAGE_SIZE, "*", null, this);
				logger.debug("Browse direct children. startIndex: "+startIndex+", ItemId: "+currentItemID);
			}
		}else{
			//logger.debug("Browse completed. ");
		}
	}
	
	/**
	 * 获取当前选择设备
	 */
	public Device getCurrentDevice(){
		return mDeviceCache.getDeviceByUDN(currentDeviceUID);
	}
	
	/**
	 * 重新搜索提示框
	 */
	private void showRetrySearchDialog() {
		if(retrySearchDialog==null){
			Alert.Builder builder = new Alert.Builder(mMediaPlayer);
			builder.setMessage(mMediaPlayer.getString(R.string.dialog_search_msg));
			builder.setTitle(mMediaPlayer.getString(R.string.dialog_prompt));
			builder.setPositiveButton(mMediaPlayer.getString(R.string.dialog_ok), new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					getLivingDevice();
				}
			});
	
			builder.setNegativeButton(mMediaPlayer.getString(R.string.dialog_cancel), new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			builder.setCancelable(false);
			retrySearchDialog = builder.create();
		}
		retrySearchDialog.show();
	}
	
	/**
	 * 创建对话框
	 * @return
	 */
	protected WaitDialog showWaitDialog(int id){
		if(mWaitDialog==null)
			mWaitDialog = new WaitDialog(mMediaPlayer);
		switch(id){
		case SHOW_DIALOG:{
			mWaitDialog.setMessage(mMediaPlayer.getString(R.string.dialog_searching_msg));
			mWaitDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface arg0) {
					stopRefresh();
				}
			});
			break;
		}
		case SHOW_OPEN_DIALOG:{
			mWaitDialog.setMessage(mMediaPlayer.getString(R.string.dialog_openning_msg));
			mWaitDialog.setOnCancelListener(null);
		}
		}
		mWaitDialog.show();
		return mWaitDialog;
	}
}
