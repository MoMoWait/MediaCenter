/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Filename:    MediaServer.java  
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2012-4-11 下午04:02:22  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2012-4-11      fxw         1.0         create
*******************************************************************/   


package com.rockchip.mediacenter.mediaserver;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.rockchip.mediacenter.IndexActivity;
import com.rockchip.mediacenter.NetworkDetecting;
import com.rockchip.mediacenter.R;
import com.rockchip.mediacenter.SystemDeviceManager;
import com.rockchip.mediacenter.SystemSettingUtils;
import com.rockchip.mediacenter.common.logging.Log;
import com.rockchip.mediacenter.common.logging.LogFactory;
import com.rockchip.mediacenter.core.dlna.enumeration.MediaClassType;
import com.rockchip.mediacenter.core.dlna.service.contentdirectory.format.MediaFormat;
import com.rockchip.mediacenter.dlna.dmp.model.MediaItem;
import com.rockchip.mediacenter.mediaplayer.model.FileInfo;
import com.rockchip.mediacenter.mediaplayer.model.LastLevelFileInfo;
import com.rockchip.mediacenter.mediaplayer.ui.WaitDialog;
import com.rockchip.mediacenter.mediaserver.FileDirectoryManager.Listener;
import com.rockchip.mediacenter.mediaserver.model.FolderAddFileInfo;
import com.rockchip.mediacenter.mediaserver.ui.AddShareFolderActivity;
import com.rockchip.mediacenter.mediaserver.ui.PathTextView;
import com.rockchip.mediacenter.mediaserver.ui.SetShareChoiceActivity;
import com.rockchip.mediacenter.mediaserver.ui.ShareListAdapter;
import com.rockchip.mediacenter.plugins.renderplay.MediaPlayListTempCache;
import com.rockchip.mediacenter.plugins.widget.Alert;
import com.rockchip.mediacenter.plugins.widget.MediaGridView;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 *
 * @author fxw
 * @since 1.0
 */
public class MediaServer extends Activity implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener, View.OnClickListener, AdapterView.OnItemLongClickListener, Listener {

	public static Log logger = LogFactory.getLog(IndexActivity.class);
	public static final String KEY_SHARE_TYPE = SystemSettingUtils.KEY_SHARE_TYPE;
	public static final int DIALOG_OPENNING = 1;
	public static final int DIALOG_SHARE_ENABLE = 2;
	public static final int DIALOG_SHARE_DISABLE = 3;
	public static final int DIALOG_SHARE_DELETE = 4;
	public static final int REQUEST_CODE_SET_SHARE = 1;
	public static final int REQUEST_CODE_ADD_FOLDER = 2;
	private SystemDeviceManager mSystemDeviceManager;
	private ShareMediaFileInfoProvider mShareMediaProvider;
	private MediaShareType mMediaShareType;
	private Handler mMainHandler = new Handler();
	private NetworkDetecting mNetworkDetecting;

	private MediaGridView mMediaGridView;
	private ShareListAdapter mShareListAdapter;
	private Button mSwitchShareBtn;
	private ViewGroup pathContainer;
	private FileDirectoryManager mFileDirectoryManager;
	private FileInfo selectedFileInfo;
	private Toast mTitleToast;
	
	private List<FileInfo> mPathList = new ArrayList<FileInfo>();//用于记录浏览的文件路径
	
	/** 
	 * <p>Title: onCreate</p> 
	 * <p>Description: </p> 
	 * @param savedInstanceState 
	 * @see android.app.Activity#onCreate(android.os.Bundle) 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dms_main);
		initView();
		mSystemDeviceManager = new SystemDeviceManager(this);
		mSystemDeviceManager.setBindListener(new SystemDeviceManager.BindListener() {
			public void onBindCompleted() {
				boolean isRunning = mSystemDeviceManager.isServerRunning();
				mSwitchShareBtn.setText(isRunning?R.string.dms_close_share:R.string.dms_open_share);
				mShareListAdapter.setShareEnabled(isRunning);
				mMediaShareType = SystemSettingUtils.getMediaShareType(MediaServer.this);
				showRootDirectory();
			}
		});
		mSystemDeviceManager.startManager();
		mShareMediaProvider = new ShareMediaFileInfoProvider(this);
		mFileDirectoryManager = new FileDirectoryManager(this);
		mFileDirectoryManager.setOnlyDirectory(false);
		mFileDirectoryManager.setFileFilter(new FileFilter(){
			public boolean accept(File file) {
				return file.isDirectory()||MediaFormat.isMedia(file);
			}
		});
		mNetworkDetecting = new NetworkDetecting(this);
	}
	
	private void initView(){
		pathContainer = (ViewGroup)findViewById(R.id.dms_ll_path);
		mMediaGridView = (MediaGridView)findViewById(R.id.dms_gv_media_list);
		mMediaGridView.setOnItemClickListener(this);
		mMediaGridView.setOnItemLongClickListener(this);
		mMediaGridView.setOnItemSelectedListener(this);
		mShareListAdapter = new ShareListAdapter(this, new ArrayList<FileInfo>());
		mMediaGridView.setMediaListAdapter(mShareListAdapter);
		mSwitchShareBtn = (Button)findViewById(R.id.dms_switch_share);
		mSwitchShareBtn.setOnClickListener(this);
		findViewById(R.id.dms_set_share).setOnClickListener(this);
		mTitleToast = Toast.makeText(this, "", Toast.LENGTH_LONG);
		int yOffset = getResources().getDimensionPixelSize(R.dimen.toast_y_offset);
		mTitleToast.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM, 0, yOffset);
	}
	
	/** 
	 * <p>Title: onResume</p> 
	 * <p>Description: </p>  
	 * @see android.app.Activity#onResume() 
	 */
	@Override
	protected void onResume() {
		super.onResume();
		updateTitle(null);
		mMediaGridView.requestFocus();
		mNetworkDetecting.detect();
	}
	
	/** 
	 * <p>Title: onDestroy</p> 
	 * <p>Description: </p>  
	 * @see android.app.Activity#onDestroy() 
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mSystemDeviceManager.stopManager();
	}
	
	/** 
	 * <p>Title: onBackPressed</p> 
	 * <p>Description: </p>  
	 * @see android.app.Activity#onBackPressed() 
	 */
	@Override
	public void onBackPressed() {
		if(mPathList.size()>0){
			showRootDirectory();
		}else{
			super.onBackPressed();
		}
	}
	
	public List<FileInfo> getDataSource(){
		return mMediaGridView.getDataSource();
	}
	
	/**
	 * 设置列表数据源
	 * @param fileInfoList
	 */
	public void setDataSource(final List<FileInfo> fileInfoList) {
		fileInfoList.add(0, new LastLevelFileInfo());
		runOnUiThread(new Runnable(){
			public void run() {
				mMediaGridView.setDataSource(fileInfoList);
				dismissDialog(DIALOG_OPENNING);
				mMediaGridView.requestFocus();
			}
		});
	}
	
	/** 
	 * <p>Title: getTopLevelDirecotry</p> 
	 * <p>Description: </p> 
	 * @return 
	 * @see com.rockchip.mediacenter.mediaserver.FileDirectoryManager.Listener#getTopLevelDirecotry() 
	 */
	@Override
	public List<FileInfo> getTopLevelDirecotry() {//unused
		if(mMediaShareType==MediaShareType.FOLDER_SHARE){//文件夹共享
    		return getShareFolderDataSource();
    	}else if(mMediaShareType==MediaShareType.MEDIA_SHARE){//媒体库共享
    		return mShareMediaProvider.getMediaCategory();
    	}else{
    		return new ArrayList<FileInfo>();
    	}
	}
	
	/**
	 * 文件夹共享--更新标题栏
	 */
	public void updateTitle(String currentPath){
		runOnUiThread(new Runnable(){
			public void run() {
				pathContainer.removeAllViews();
				String rootName = getString(R.string.dms_device_share);
				pathContainer.addView(createPathView(rootName, 0, null));
				if(mPathList.size()>0){
					for(int i=0; i<mPathList.size(); i++){
						FileInfo fileInfo = mPathList.get(i);
						if(!fileInfo.isFileItem()) continue;
						File file = (File)fileInfo.getItem();
						View view = createPathView(file.getName(), i+1, fileInfo);
						pathContainer.addView(view);
					}
				}
			}
		});
	}
	private TextView createPathView(String name, final int linkID, final FileInfo fileInfo){
		TextView tv = new PathTextView(this, name);
		tv.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(linkID==0){//根目录
					showRootDirectory();
				}else{
					browseFileDirectory(fileInfo);
					Iterator<FileInfo> it = mPathList.iterator();
					int i=0;
					while(it.hasNext()){
						it.next();
						i++;
						if(i>linkID) it.remove();
					}
				}
			}
		});
		return tv;
	}
	
	/**
     * 共享模式切换
     */
    public void switchShareScheme(MediaShareType shareType){
    	if(mMediaShareType==shareType) return;
    	
    	mMediaShareType = shareType;
    	SystemSettingUtils.saveMediaShareType(this, shareType);
    	mSystemDeviceManager.updateContentSharePolicy();
    	showRootDirectory();
    }
    
    /**
     * 查看根目录
     */
    public void showRootDirectory(){
    	if(mMediaShareType==MediaShareType.FOLDER_SHARE){//文件夹共享
    		List<FileInfo> fileInfoList = getShareFolderDataSource();
    		fileInfoList.add(0, new FolderAddFileInfo());
    		mMediaGridView.setDataSource(fileInfoList);
    	}else if(mMediaShareType==MediaShareType.MEDIA_SHARE){//媒体库共享
    		mMediaGridView.setDataSource(mShareMediaProvider.getMediaCategory());
    	}else{
    		mMediaGridView.clearDataSource();
    	}
    	mShareListAdapter.setTopDirectory(true);
    	mPathList.clear();
    	updateTitle(null);
    	mMediaGridView.requestFocus();
    }
    
    /**
     * 获取保存在库表中的共享目录列表
     * @return
     */
    public List<FileInfo> getShareFolderDataSource(){
    	List<FileInfo> fileInfoList = new ArrayList<FileInfo>();
    	List<String> directoryList = mSystemDeviceManager.queryShareDirectory();
    	for(String dir : directoryList){
    		File file = new File(dir);
    		if(file.exists()){
    			FileInfo fileInfo = new FileInfo(file);
    			//fileInfo.setIcon()
    			fileInfoList.add(fileInfo);
    		}else{
    			mSystemDeviceManager.deleteShareDirectory(dir);
    		}
    	}
    	return fileInfoList;
    }

	/** 
	 * <p>Title: onItemLongClick</p> 
	 * <p>Description: </p> 
	 * @param adapter
	 * @param view
	 * @param position
	 * @param id
	 * @return 
	 * @see android.widget.AdapterView.OnItemLongClickListener#onItemLongClick(android.widget.AdapterView, android.view.View, int, long) 
	 */
	@Override
	public boolean onItemLongClick(AdapterView<?> adapter, View view, int position,
			long id) {
		FileInfo fileInfo = (FileInfo)adapter.getItemAtPosition(position);
		if(!fileInfo.isFileItem()){
			return true;
		}
		selectedFileInfo = fileInfo;
		showDialog(DIALOG_SHARE_DELETE);
		return true;
	}
	
	/** 
	 * <p>Title: onItemSelected</p> 
	 * <p>Description: </p> 
	 */
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		FileInfo fileInfo = (FileInfo)parent.getItemAtPosition(position);
		if(!fileInfo.isDir()){
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
			FileInfo fileInfo = (FileInfo)mMediaGridView.getSelectedItem();
			if(fileInfo==null||!fileInfo.isFileItem()){
				return true;
			}
			selectedFileInfo = fileInfo;
			showDialog(DIALOG_SHARE_DELETE);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	

	/** 
	 * <p>Title: onClick</p> 
	 * <p>Description: </p> 
	 * @param v 
	 * @see android.view.View.OnClickListener#onClick(android.view.View) 
	 */
	@Override
	public void onClick(View v) {
		int viewId = v.getId();
		if(viewId == R.id.dms_switch_share){
			switchMediaShare();
		}else if(viewId == R.id.dms_set_share){
			setMediaShare();
		}
	}
	//开启/关闭共享
	private void switchMediaShare(){
		//开启-->关闭
		if(mSystemDeviceManager.isServerRunning()){
			showDialog(DIALOG_SHARE_DISABLE);
			new Thread(new Runnable(){
				public void run() {
					mSystemDeviceManager.stopMediaServer();
					runOnUiThread(new Runnable(){
						public void run() {
							mSwitchShareBtn.setText(R.string.dms_open_share);
							dismissDialog(DIALOG_SHARE_DISABLE);
							mShareListAdapter.setShareEnabled(false);
							mShareListAdapter.notifyDataSetChanged();
							SystemSettingUtils.saveMediaServerState(MediaServer.this, false);
						}
					});
				}
			}).start();
		//关闭-->开启
		}else{
			if(!mNetworkDetecting.detect()){//网络未连接
				return;
			}
			showDialog(DIALOG_SHARE_ENABLE);
			new Thread(new Runnable(){
				public void run() {
					mSystemDeviceManager.startMediaServerWithSavedConfig();
					runOnUiThread(new Runnable(){
						public void run() {
							mSwitchShareBtn.setText(R.string.dms_close_share);
							dismissDialog(DIALOG_SHARE_ENABLE);
							mShareListAdapter.setShareEnabled(true);
							mShareListAdapter.notifyDataSetChanged();
							SystemSettingUtils.saveMediaServerState(MediaServer.this, true);
						}
					});
				}
			}).start();
		}
	}
	//设置共享目录--设置共享方式
	private void setMediaShare(){
		Intent intent = new Intent(this, SetShareChoiceActivity.class);
		intent.putExtra(KEY_SHARE_TYPE, mMediaShareType);
		startActivityForResult(intent, REQUEST_CODE_SET_SHARE);
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
		if(requestCode==REQUEST_CODE_SET_SHARE){
			if(data==null) return;
			MediaShareType shareType = (MediaShareType)data.getSerializableExtra(KEY_SHARE_TYPE);
			switchShareScheme(shareType);
			if(shareType==MediaShareType.FOLDER_SHARE){
				Intent intent = new Intent(this, AddShareFolderActivity.class);
				startActivityForResult(intent, REQUEST_CODE_ADD_FOLDER);
			}
		}else if(requestCode==REQUEST_CODE_ADD_FOLDER){//添加共享目录
			if(mPathList.size()==0){//如果在根目录进行刷新
				showRootDirectory();
			}
		}
	}
	

	/** 
	 * <p>Title: onItemClick</p> 
	 * <p>Description: </p> 
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3 
	 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long) 
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		final FileInfo fileInfo = (FileInfo)parent.getItemAtPosition(position);
		if(fileInfo.isDir()){
			browseFileDirectory(fileInfo);
		}else{//本地播放媒体
			playLocalMedia(fileInfo);
		}
	}
	
	/**
	 * 浏览文件目录信息
	 * @param fileInfo
	 */
	private void browseFileDirectory(final FileInfo fileInfo){
		mShareListAdapter.setTopDirectory(false);
		if(isMediaStoreShare()){
			if(LastLevelFileInfo.isLastLevelFileInfo(fileInfo)){
				showRootDirectory();//回到根目录
			}else{
				mPathList.add(fileInfo);
				browseMediaStore(fileInfo);
			}
		}else if(isFolderShare()){
			if(LastLevelFileInfo.isLastLevelFileInfo(fileInfo)){
				if(mPathList.size()==1){//在一级目录
					showRootDirectory();
				}else{
					mPathList.remove(mPathList.size()-1);
					showDialog(MediaServer.DIALOG_OPENNING);
					mFileDirectoryManager.browseLastLevelDirectory();
				}
			}else if(FolderAddFileInfo.isFolderAddFileInfo(fileInfo)){//添加共享目录
				Intent intent = new Intent(this, AddShareFolderActivity.class);
				startActivityForResult(intent, REQUEST_CODE_ADD_FOLDER);
			}else{
				mPathList.add(fileInfo);
				showDialog(MediaServer.DIALOG_OPENNING);
				mFileDirectoryManager.browseDirectory(fileInfo);
			}
		}
	}
	//浏览媒体库共享
	private void browseMediaStore(final FileInfo fileInfo){
		showDialog(DIALOG_OPENNING);
		new Thread(){
			public void run() {
				final List<FileInfo> fileInfoList = mShareMediaProvider.getMediaContents(fileInfo.getPath());
				if(!LastLevelFileInfo.isLastLevelFileInfo(fileInfo)){
					fileInfoList.add(0, new LastLevelFileInfo());
				}
				MediaServer.this.runOnUiThread(new Runnable(){
					public void run() {
						mMediaGridView.setDataSource(fileInfoList);
						updateTitle(fileInfo.getPath());
						dismissDialog(DIALOG_OPENNING);
					}
				});
			}
		}.start();
	}
	
	/**
	 * Local Renderer
	 * 在本地利用其它APP播放媒体
	 */
	private void playLocalMedia(FileInfo fileItem){
		Intent intent = new Intent();
	    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    intent.setAction(android.content.Intent.ACTION_VIEW);
	    
	    if(fileItem.isFileItem()&&!fileItem.isDir()){
	    	File file = (File)fileItem.getItem();
	    	MediaItem mediaItem = convertMediaItem(file);
	    	if(MediaFormat.isImage(file)){
	    		intent.setClass(this, com.rockchip.mediacenter.plugins.pictureplay.PictureViewer.class);
	    		mediaItem.setObjectClass(MediaClassType.IMAGE.getDefaultObjectClass());
	    	}else if(MediaFormat.isAudio(file)){
				intent.setClass(this, com.rockchip.mediacenter.plugins.musicplay.MusicPlayer.class);
				mediaItem.setObjectClass(MediaClassType.AUDIO.getDefaultObjectClass());
	    	}else if(MediaFormat.isVideo(file)){
				intent.setClass(this, com.rockchip.mediacenter.plugins.videoplay.VideoPlayer.class);
				mediaItem.setObjectClass(MediaClassType.VIDEO.getDefaultObjectClass());
	    	}
			ArrayList<MediaItem> mediaItemList = new ArrayList<MediaItem>();
			int selectIndex = getMediaWithSameType(file, mediaItemList);
			MediaPlayListTempCache.getInstance().setPlayList(mediaItemList);
			MediaPlayListTempCache.getInstance().setPlayIndex(selectIndex);
			//intent.putExtra(MediaPlayConsts.KEY_MEDIA_LIST, mediaItemList);
			//intent.putExtra(MediaPlayConsts.KEY_MEDIA_SELECT, selectIndex);
	    	//MediaItem currMediaItem = mediaItemList.get(selectIndex);
	    	//Uri tmpUri = Uri.parse(currMediaItem.getResourceURL());
	    	//intent.setDataAndType(tmpUri, mediaItem.getDefaultMimeType());
	    }
	    
	    try { 
	    	startActivity(intent);
	    } catch (android.content.ActivityNotFoundException e) {
	    	Toast.makeText(this, getString(R.string.no_such_app), Toast.LENGTH_SHORT).show();
        }
	}
	/**
	 * 返回与指定文件类型相同的媒体列表
	 * @param file 指定文件
	 * @return 返回文件在媒体列表中的位置
	 */
	private int getMediaWithSameType(File file, ArrayList<MediaItem> mediaItemList){
		List<FileInfo> fileInfoList = getDataSource();
		int currentIndex = -1;
		for(int i=0; i<fileInfoList.size(); i++){
			FileInfo fileInfo = fileInfoList.get(i);
			if(!fileInfo.isFileItem()||fileInfo.isDir()) continue;
			
			File item = (File)fileInfo.getItem();
			MediaItem mediaItem = convertMediaItem(item);
			if(MediaFormat.isImage(item)&&MediaFormat.isImage(file)){
				mediaItem.setObjectClass(MediaClassType.IMAGE.getDefaultObjectClass());
				mediaItemList.add(mediaItem);
	    	}else if(MediaFormat.isAudio(item)&&MediaFormat.isAudio(file)){
				mediaItem.setObjectClass(MediaClassType.AUDIO.getDefaultObjectClass());
				mediaItemList.add(mediaItem);
	    	}else if(MediaFormat.isVideo(item)&&MediaFormat.isVideo(file)){
				mediaItem.setObjectClass(MediaClassType.VIDEO.getDefaultObjectClass());
				mediaItemList.add(mediaItem);
	    	}
			if(item.equals(file)){//记录当前选中文件 在播放列表的位置
				currentIndex = mediaItemList.size()-1;
			}
		}
		return currentIndex;
	}
	private MediaItem convertMediaItem(File file){
		MediaItem mi = new MediaItem();
		mi.setTitle(file.getName());
		mi.setResourceURL(Uri.fromFile(file).toString());
		return mi;
	}
	
	/**
	 * 是否为文件夹共享
	 * @return
	 */
	public boolean isFolderShare(){
		return mMediaShareType == MediaShareType.FOLDER_SHARE;
	}
	
	/**
	 * 是否为媒体库共享
	 * @return
	 */
	public boolean isMediaStoreShare(){
		return mMediaShareType == MediaShareType.MEDIA_SHARE;
	}
	
	/**
     * 获得主线程Handler
     * @return
     */
    public Handler getUIHandler(){
    	return mMainHandler;
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
    	if(id==DIALOG_SHARE_DELETE){
    		String msg = null;
			if(isMediaStoreShare()){
				msg = getString(R.string.dms_remove_mediastore);
			}else{
				if(mPathList.size()>0){//当前不在文件共享根目录
					selectedFileInfo = mPathList.get(0);
				}
				File file = (File)selectedFileInfo.getItem();
				msg = String.format(getString(R.string.dms_remove_directory), file.getName());
			}
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
		WaitDialog waitDialog = new WaitDialog(this);
		//waitDialog.setTitle(R.string.dialog_waiting);
		switch(id){
		case DIALOG_OPENNING:{
			waitDialog.setMessage(getString(R.string.dialog_openning_msg));
			return waitDialog;
		}
		case DIALOG_SHARE_ENABLE:{
			waitDialog.setMessage(getString(R.string.dms_share_enable_msg));
			return waitDialog;
		}
		case DIALOG_SHARE_DISABLE:{
			waitDialog.setMessage(getString(R.string.dms_share_disable_msg));
			return waitDialog;
		}
		case DIALOG_SHARE_DELETE:{
			if(selectedFileInfo!=null&&selectedFileInfo.isFileItem()){
				Alert.Builder builder = new Alert.Builder(this);
				builder.setTitle(R.string.dialog_prompt);
				String msg = null;
				if(isMediaStoreShare()){
					msg = getString(R.string.dms_remove_mediastore);
				}else{
					if(mPathList.size()>0){//当前不在文件共享根目录
						selectedFileInfo = mPathList.get(0);
					}
					File file = (File)selectedFileInfo.getItem();
					msg = String.format(getString(R.string.dms_remove_directory), file.getName());
				}
				builder.setMessage(msg);
				builder.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int which) {
						if(isMediaStoreShare()){
							switchShareScheme(null);
						}else{
							mSystemDeviceManager.deleteShareDirectory(selectedFileInfo.getPath());
							showRootDirectory();
						}
					}
				});
				builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				return builder.create();
			}
		}
		}
		return super.onCreateDialog(id);
	}
	
}
