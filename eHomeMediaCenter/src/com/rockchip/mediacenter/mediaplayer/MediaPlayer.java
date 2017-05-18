/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Filename:    MediaPlayer.java  
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2012-4-6 上午10:54:27  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2012-4-6      fxw         1.0         create
*******************************************************************/   


package com.rockchip.mediacenter.mediaplayer;

import java.util.ArrayList;
import java.util.List;

import com.rockchip.mediacenter.DLNAManager;
import com.rockchip.mediacenter.DLNAService;
import com.rockchip.mediacenter.IndexActivity;
import com.rockchip.mediacenter.NetworkDetecting;
import com.rockchip.mediacenter.R;
import com.rockchip.mediacenter.common.logging.Log;
import com.rockchip.mediacenter.common.logging.LogFactory;
import com.rockchip.mediacenter.common.util.StringUtils;
import com.rockchip.mediacenter.core.constants.MediaPlayConsts;
import com.rockchip.mediacenter.core.dlna.AsyncTaskCallback;
import com.rockchip.mediacenter.core.dlna.protocols.response.contentdirectory.BrowseResponse;
import com.rockchip.mediacenter.dlna.model.DeviceItem;
import com.rockchip.mediacenter.core.upnp.Device;
import com.rockchip.mediacenter.core.util.LocalStorageProvider;
import com.rockchip.mediacenter.dlna.dmd.DigitalMediaDownloader;
import com.rockchip.mediacenter.dlna.dmp.DigitalMediaPlayer;
import com.rockchip.mediacenter.dlna.dmp.model.ContainerItem;
import com.rockchip.mediacenter.dlna.dmp.model.ContentItem;
import com.rockchip.mediacenter.dlna.dmp.model.ContentItemList;
import com.rockchip.mediacenter.dlna.dmp.model.MediaItem;
import com.rockchip.mediacenter.dlna.dmu.DigitalMediaUploader;
import com.rockchip.mediacenter.mediaplayer.model.FileInfo;
import com.rockchip.mediacenter.mediaplayer.model.LastLevelFileInfo;
import com.rockchip.mediacenter.mediaplayer.ui.DeviceSelectorActivity;
import com.rockchip.mediacenter.mediaplayer.ui.MediaListAdapter;
import com.rockchip.mediacenter.mediaplayer.ui.TransferActivity;
import com.rockchip.mediacenter.mediaplayer.util.FileInfoRenderUtil;
import com.rockchip.mediacenter.plugins.imageloader.ImageLoader;
import com.rockchip.mediacenter.plugins.imageloader.LocalBitmapCache;
import com.rockchip.mediacenter.plugins.widget.Alert;
import com.rockchip.mediacenter.plugins.widget.MediaGridView;

import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

/**
 *
 * @author fxw
 * @since 1.0
 */
public class MediaPlayer extends Activity implements AdapterView.OnItemClickListener, View.OnClickListener, AdapterView.OnItemLongClickListener, AdapterView.OnItemSelectedListener {

	private static final Log logger = LogFactory.getLog(MediaPlayer.class);
	private static final int REQUEST_CODE_PLAY = 1;
	private static final int DIALOG_DOWNLOAD_FILE = 1;
	private FileExplorer mFileExplorer;
	private FileControl mFileControl;
	private DLNAManager dlnaManager;
	private DigitalMediaPlayer dmp;
	@SuppressWarnings("unused")
	private DownloadManager mDownloadManager;
	private FileInfo selectedFileInfo;
	private NetworkDetecting mNetworkDetecting;
	
	private MediaGridView mMediaGridView;
	private ViewGroup pathContainer;
	private boolean hasInit;
	private Toast mTitleToast;
	private ImageLoader mImageLoader;
	private MediaListAdapter mMediaListAdapter;
	private Handler mMainHandler = new Handler();
	private MulticastLock mMulticastLock; 
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dmp_main);
		allowMulticast();
		mImageLoader = new ImageLoader();
		mImageLoader.setContext(this);
		mImageLoader.setImageCacheStrategy(new LocalBitmapCache(this));
		initView();
		dlnaManager = new DLNAManager(this);
		mFileExplorer = new FileExplorer(this, dlnaManager, mImageLoader);
		mFileControl = new FileControl(this, mFileExplorer);
		mNetworkDetecting = new NetworkDetecting(this);
		dlnaManager.setBindListener(new DLNAManager.BindListener() {
			public void onBindCompleted() {
				new Thread(){
					public void run() {
						dmp = dlnaManager.getDigitalMediaPlayer();
						mFileExplorer.setDigitalMediaPlayer(dmp);
						mFileExplorer.onCreate();
						hasInit = false;
						initRoot();
					}
				}.start();
			}
		});
		dlnaManager.startManager();
		mDownloadManager = (DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE);
		IntentFilter intentFilter = new IntentFilter(MediaPlayConsts.ACTION_PLAYER_NEW_URL);
		intentFilter.addDataScheme("http");
		registerReceiver(mPlayerBroadcastReceiver, intentFilter);
	}
	
	private void initView(){
		pathContainer = (ViewGroup)findViewById(R.id.dmp_ll_path);
		mMediaGridView = (MediaGridView)findViewById(R.id.dmp_gv_media_list);
		mMediaGridView.setOnItemClickListener(this);
		mMediaGridView.setOnItemLongClickListener(this);
		mMediaGridView.setOnItemSelectedListener(this);
		mMediaListAdapter = new MediaListAdapter(this, new ArrayList<FileInfo>(), mImageLoader);
		mMediaListAdapter.setMediaGridView(mMediaGridView);
		mMediaGridView.setMediaListAdapter(mMediaListAdapter);
		mMediaGridView.requestFocus();
		findViewById(R.id.dmp_btn_refresh).setOnClickListener(this);
		findViewById(R.id.dmp_btn_transfer).setOnClickListener(this);
		mTitleToast = Toast.makeText(this, "", Toast.LENGTH_LONG);
		int yOffset = getResources().getDimensionPixelSize(R.dimen.toast_y_offset);
		mTitleToast.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM, 0, yOffset);
	}
	
	/**
	 * 首次进入时, 初始化
	 */
	private void initRoot(){
		runOnUiThread(new Runnable(){
			public void run() {
				if(!mNetworkDetecting.detect()){
					return;
				}
				if(hasInit==true){
					return;
				}
				hasInit = true;
				mFileControl.browseTopDirectory(true);
			}
		});
	}
	
	/** 
	 * <p>Title: onResume</p> 
	 * <p>Description: </p>  
	 * @see android.app.Activity#onResume() 
	 */
	@Override
	protected void onResume() {
		mMediaListAdapter.setShowing(true);
		super.onResume();
		mFileExplorer.onResume();
		//mMediaGridView.clearSelectedPlaying();
		if(dlnaManager.isConnectService()){
			initRoot();
		}
	}
	
	/** 
	 * <p>Title: onPause</p> 
	 * <p>Description: </p>  
	 * @see android.app.Activity#onPause() 
	 */
	@Override
	protected void onPause() {
		mMediaListAdapter.setShowing(false);
		super.onPause();
		mFileExplorer.onPause();
	}
	
	@Override
	public void onBackPressed() {
		if(!mFileControl.browseLastLevelDirectory()){
			super.onBackPressed();
			Intent intent = new Intent(this, IndexActivity.class);
			startActivity(intent);
		}
	}
	
	/** 
	 * <p>Title: onDestroy</p> 
	 * <p>Description: </p>  
	 * @see android.app.Activity#onDestroy() 
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mPlayerBroadcastReceiver);
		mFileExplorer.onDestroy();
		dlnaManager.stopManager();
		mImageLoader.clear();
		releaseMulticast();
	}
	
	public List<FileInfo> getDataSource(){
		return mMediaGridView.getDataSource();
	}
	
	public void refreshDataSource(){
		mMediaGridView.refreshDataSource();
	}
	
	public void setDataSource(final ArrayList<FileInfo> fileInfoList, final boolean isSuccessed) {
		mMainHandler.post(new Runnable(){
			public void run() {
				List<FileInfo> dataList = fileInfoList;
				if(dataList==null) dataList = new ArrayList<FileInfo>();
				if(!FileInfoRenderUtil.isDLNARoot(mFileControl.getCurrentPath())){
					dataList.add(0, new LastLevelFileInfo());
				}
				mMediaGridView.setDataSource(dataList);
				mMediaGridView.requestFocus();
				if(!isSuccessed){
					Toast.makeText(MediaPlayer.this, R.string.dmp_request_fail, Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
	public void addDataSource(final FileInfo fileInfo){
		mMainHandler.post(new Runnable(){
			public void run() {
				mMediaGridView.addFileInfo(fileInfo);
				mMediaGridView.requestFocus();
			}
		});
	}
	public void addDataSource(final ArrayList<FileInfo> fileInfoList){
		mMainHandler.post(new Runnable(){
			public void run() {
				mMediaGridView.addFileInfosWithNoCheck(fileInfoList);
				mMediaGridView.requestFocus();
			}
		});
	}
	public void delDataSource(final FileInfo fileInfo){
		mMainHandler.post(new Runnable(){
			public void run() {
				mMediaGridView.removeFileInfo(fileInfo);
				mMediaGridView.requestFocus();
			}
		});
	}

	public FileControl getFileControl() {
		return mFileControl;
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		if(!mNetworkDetecting.detect()){//网络未连接
			return;
		}
		FileInfo fileItem = (FileInfo)adapter.getItemAtPosition(position);
		if(fileItem.isDir()){
			String path = fileItem.getPath();
			if(LastLevelFileInfo.isLastLevelPath(path)){
				mFileControl.browseLastLevelDirectory();
			}else{
				path = FileInfoRenderUtil.changeDLNAFilePath(mFileControl.getCurrentPath(), path);
				mFileControl.browseDirectDirectory(path);
			}
		}else{
			//updatePlayingMedia(fileItem);
			playLocalMedia(fileItem, position);
			logger.debug("Play media on the local device. ");
		}
	}
	
	@Override
	public void onClick(View v) {
		if(v.getId()==R.id.dmp_btn_refresh){
			if(!mNetworkDetecting.detect()){//网络未连接
				return;
			}
			mFileControl.refreshDirectory();
		}else if(v.getId()==R.id.dmp_btn_transfer){
			startActivity(new Intent(this, TransferActivity.class));
		}
	}
	
	/** 
	 * <p>Title: onItemLongClick</p> 
	 * <p>Description: </p> 
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @return 
	 * @see android.widget.AdapterView.OnItemLongClickListener#onItemLongClick(android.widget.AdapterView, android.view.View, int, long) 
	 */
	@Override
	public boolean onItemLongClick(AdapterView<?> adapter, View v, int position, long id) {
		FileInfo fileInfo = (FileInfo)adapter.getItemAtPosition(position);
		if(fileInfo.isContainerItem()){
			browseContainerMeta(fileInfo);
		}else if(fileInfo.isMediaItem()){
			selectedFileInfo = fileInfo;
			showDialog(DIALOG_DOWNLOAD_FILE);
		}
		return true;
	}
	
	/**
	 * 浏览文件夹信息
	 */
	private void browseContainerMeta(FileInfo fileInfo){
		Device device = mFileExplorer.getCurrentDevice();
		if(device!=null){
			ContainerItem item = (ContainerItem)fileInfo.getItem();
			dmp.browseMetaData(device, item.getId(), new AsyncTaskCallback<BrowseResponse>() {
				public void onCompleted(BrowseResponse response, int arg1) {
					if(response!=null){
						ContentItemList itemList = response.getParsedResult();
						if(itemList!=null&&itemList.size()>0){
							ContentItem item = itemList.get(0);
							if(item instanceof ContainerItem){
								final ContainerItem cItem = (ContainerItem)item;
								runOnUiThread(new Runnable(){
									public void run() {
										Toast.makeText(MediaPlayer.this, cItem.getChildCount()+getString(R.string.dmp_device_item_desc), Toast.LENGTH_LONG).show();
									}
								});
							}
						}
					}
				}
			});
		}
	}
	
	/** 
	 * <p>Title: onItemSelected</p> 
	 * <p>Description: </p> 
	 */
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		FileInfo fileInfo = (FileInfo)parent.getItemAtPosition(position);
		if(fileInfo.isMediaItem()){
			mTitleToast.setText(fileInfo.getTitle());
			mTitleToast.show();
		}else{
			mTitleToast.cancel();
		}
	}

	/** 
	 * <p>Title: onNothingSelected</p> 
	 * <p>Description: </p> 
	 */
	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		mTitleToast.cancel();
	}
	
	/** 
	 * <p>Title: onPrepareDialog</p> 
	 * <p>Description: </p> 
	 * @param id
	 * @param dialog 
	 * @see android.app.Activity#onPrepareDialog(int, android.app.Dialog) 
	 */
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		if(id==DIALOG_DOWNLOAD_FILE){
			if(getCurrentDevice()==null){
				Toast.makeText(this, R.string.dmp_invalid_device, Toast.LENGTH_LONG).show();
				return;
			}
			int msgResID = -1;
			if(isInLocalDevice()){//本地设备 上传
				msgResID = R.string.dmp_upload_file_msg;
			}else{
				msgResID = R.string.dmp_download_file_msg;
			}
			String msg = String.format(getString(msgResID), selectedFileInfo.getTitle());
			((Alert)dialog).setMessage(msg);
			return;
		}
		super.onPrepareDialog(id, dialog);
	}
	
	/** 
	 * <p>Title: onCreateDialog</p> 
	 * <p>Description: </p> 
	 * @param id
	 * @return 
	 * @see android.app.Activity#onCreateDialog(int) 
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		if(id==DIALOG_DOWNLOAD_FILE&&selectedFileInfo!=null&&selectedFileInfo.isMediaItem()){
			Alert.Builder builder = new Alert.Builder(this);
			builder.setTitle(R.string.dialog_prompt);
			MediaItem mediaItem = (MediaItem)selectedFileInfo.getItem();
			String msg = String.format(getString(R.string.dmp_download_file_msg), mediaItem.getTitle());
			builder.setMessage(msg);
			builder.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which) {
					if(isInLocalDevice()){//本地设备 上传
						uploadMediaItem();
					}else{
						downLoadMediaItem();
					}
				}
			});
			builder.setNeutralButton(R.string.dmp_see_all_transfer, new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which) {
//					Intent intent = new Intent();
//					intent.setClassName("com.android.providers.downloads.ui", "com.android.providers.downloads.ui.DownloadList");
//					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//					try{
//						MediaPlayer.this.startActivity(intent);
//					}catch(Exception ex){
//					}
					startActivity(new Intent(MediaPlayer.this, TransferActivity.class));
				}
			});
			builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			return builder.create();
		}
		return super.onCreateDialog(id);
	}
	
	/** 
	 * <p>Title: onKeyDown</p> 
	 * <p>Description: </p> 
	 * @param keyCode
	 * @param event
	 * @return 
	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent) 
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==84||keyCode==KeyEvent.KEYCODE_MENU){
			selectedFileInfo = (FileInfo)mMediaGridView.getSelectedItem();
			if(selectedFileInfo!=null){
				showDialog(DIALOG_DOWNLOAD_FILE);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	/** 
	 * <p>Title: onActivityResult</p> 
	 * <p>Description: </p> 
	 * @param requestCode
	 * @param resultCode
	 * @param data 
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent) 
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode==RESULT_OK){
			if(selectedFileInfo==null||!selectedFileInfo.isMediaItem()){
				return;
			}
			MediaItem mediaItem = (MediaItem)selectedFileInfo.getItem();
			DeviceItem deviceItem = data.getParcelableExtra(DeviceSelectorActivity.EXTRA_SELECTED_DEVICE);
			if(deviceItem==null) return;
			DigitalMediaUploader dmu = dlnaManager.getDigitalMediaUploader();
			dmu.upload(mediaItem, deviceItem, new AsyncTaskCallback<Boolean>(){
				@Override
				public void onCompleted(final Boolean result, int arg1) {
					runOnUiThread(new Runnable(){
						public void run() {
							showAddTransferMsg(result);
						}
					});
				}
			});
		}
	}
	
	/**
	 * 上传本地媒体文件
	 */
	private void uploadMediaItem(){
		if(!selectedFileInfo.isMediaItem()){
			return;
		}
		MediaItem mediaItem = (MediaItem)selectedFileInfo.getItem();
		if(StringUtils.isEmptyObj(mediaItem.getResourceURL())){
			Toast.makeText(this, R.string.dmp_media_item_null, Toast.LENGTH_SHORT).show();
			return;
		}
		startActivityForResult(new Intent(this, DeviceSelectorActivity.class), 1);
	}
	
	/**
	 * 下载媒体文件
	 */
	private void downLoadMediaItem(){
		if(!selectedFileInfo.isMediaItem()){
			return;
		}
		MediaItem mediaItem = (MediaItem)selectedFileInfo.getItem();
		if(StringUtils.isEmptyObj(mediaItem.getResourceURL())){
			Toast.makeText(this, R.string.dmp_media_item_null, Toast.LENGTH_SHORT).show();
			return;
		}
		DigitalMediaDownloader dmd = dlnaManager.getDigitalMediaDownloader();
		String path = LocalStorageProvider.getDownloadLocalPath(this);
		boolean result = dmd.download(mediaItem, path);
//		Uri uri = Uri.parse(mediaItem.getResourceURL());
//		DownloadManager.Request downRequest = new DownloadManager.Request(uri);
//		downRequest.setShowRunningNotification(true);
//		downRequest.setTitle(mediaItem.getTitle());
//		//downRequest.setAllowedNetworkTypes(Request.NETWORK_WIFI);
//		downRequest.setAllowedOverRoaming(false);
//		downRequest.setDestinationInExternalPublicDir(SystemConstants.SYS_NAME+File.separator+"download", mediaItem.getTitleWithExtension());
//		mDownloadManager.enqueue(downRequest);
		showAddTransferMsg(result);
	}
	
	/**
	 * 提示添加到传输列表
	 */
	private void showAddTransferMsg(boolean result){
		if(result) Toast.makeText(this, R.string.dmp_download_add_item, Toast.LENGTH_SHORT).show();
		else Toast.makeText(this, R.string.dmp_download_add_item_fail, Toast.LENGTH_SHORT).show();
	}
	
	/**
	 * 更新当前正在播放的媒体项
	 */
	public void updatePlayingMedia(String uri){
		List<FileInfo> fileInfoList = getDataSource();
		FileInfo selectedItem = null;
		for(int i=0; i<fileInfoList.size(); i++){
			FileInfo fileInfo = fileInfoList.get(i);
			if(fileInfo.isMediaItem()){
				MediaItem item = (MediaItem)fileInfo.getItem();
				String itemURL = item.getResourceURL();
				if(itemURL!=null&&itemURL.equals(uri)){
					selectedItem = fileInfo;
					mMediaGridView.setSelection(i);
					break;
				}
			}
		}
		updatePlayingMedia(selectedItem);
	}
	public void updatePlayingMedia(MediaItem mediaItem){
		updatePlayingMedia(mediaItem.getResourceURL());
	}
	public void updatePlayingMedia(FileInfo fileInfo){
		if(fileInfo==null) return;
		mMediaGridView.setSelectedPlaying(fileInfo);
	}
	
	/**
	 * 更新标题栏
	 */
	public void updateTitle(){
		String currPath = mFileControl.getCurrentPath();
		if(currPath==null) return;
		String pathName = FileInfoRenderUtil.getAbsolutePath(currPath);
		String pathNames[] = pathName.split(FileInfoRenderUtil.SPLIT2);
		pathContainer.removeAllViews();
		for(int i=0; i<pathNames.length; i++){
			pathContainer.addView(createPathView(pathNames[i], i));
		}
	}
	private TextView createPathView(String name, final int linkID){
		TextView tv = new TextView(this);
		if(FileControl.TOP_DIR.equals(name)){
			name = getString(R.string.dmp_media_device);
		}
		tv.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				String currPath = mFileControl.getCurrentPath();
				currPath = FileInfoRenderUtil.getPathByIndex(currPath, linkID);
				mFileControl.browseDirectDirectory(currPath);
			}
		});
		tv.setTextColor(this.getResources().getColorStateList(R.drawable.dmp_path_text_color));
		tv.setText(name);
		tv.setFocusable(true);
		tv.setClickable(true);
		tv.setGravity(Gravity.CENTER_VERTICAL);
		tv.setTextSize(20);
		tv.setPadding(10, 0, 0, 0);
		tv.setCompoundDrawablePadding(-30);
		tv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.dmp_path_split, 0);
		return tv;
	}
	
	/**
	 * Local Renderer
	 * 在本地利用其它APP播放媒体
	 */
	private void playLocalMedia(FileInfo fileItem, int position){
		Intent intent = new Intent();
	    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    intent.setAction(android.content.Intent.ACTION_VIEW);
	    
	    if(fileItem.isMediaItem()){
	    	MediaItem mediaItem = (MediaItem)fileItem.getItem();
	    	if(!StringUtils.hasText(mediaItem.getResourceURL())){
	    		Toast.makeText(this, getString(R.string.plug_video_err_unknown), Toast.LENGTH_SHORT).show();
	    		return;
	    	}
	    	mFileExplorer.addIntentParameter(intent, fileItem);
	    	//Uri tmpUri = Uri.parse(mediaItem.getResourceURL());
	    	//intent.setDataAndType(tmpUri, mediaItem.getDefaultMimeType());
	    }
	    try { 
	    	startActivityForResult(intent, REQUEST_CODE_PLAY);
	    } catch (android.content.ActivityNotFoundException e) {
	    	Toast.makeText(this, getString(R.string.no_such_app), Toast.LENGTH_SHORT).show();
        }
	}
	
	/**
	 * 是否在本地设备
	 * @param device
	 * @return
	 */
	private boolean isInLocalDevice(){
		Device currDevice = getCurrentDevice();
		if(currDevice!=null){
			return DLNAService.isLocalDevice(currDevice);
		}
		return false;
	}
	
	/**
	 * 获取当前所在设备
	 */
	private Device getCurrentDevice(){
		return mFileControl.getCurrentDevice();
	}
	
	/**
	 * 监听播放器状态
	 */
	private BroadcastReceiver mPlayerBroadcastReceiver = new BroadcastReceiver(){
		public void onReceive(Context context, Intent intent) {
			if(MediaPlayConsts.ACTION_PLAYER_NEW_URL.equals(intent.getAction())){
				if(intent.getData()!=null){
					updatePlayingMedia(intent.getData().toString());
				}
			}
		}
	};
	
	/**
	 * 申请多播锁/权限
	 */
    private void allowMulticast(){ 
        WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        try{
	        mMulticastLock = wifiManager.createMulticastLock("multicast.dmp"); 
	        mMulticastLock.acquire();
        }catch(Exception e){}//ignore
    }
    /**
	 * 释放多播锁/权限
	 */
    private void releaseMulticast(){
    	try{
    		mMulticastLock.release();
    	}catch(Exception e){}//ignore
    }

}
