/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Filename:    AddShareFolderActivity.java  
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2012-4-16 下午04:25:50  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2012-4-16      fxw         1.0         create
*******************************************************************/   


package com.rockchip.mediacenter.mediaserver.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.rockchip.mediacenter.R;
import com.rockchip.mediacenter.SystemDeviceManager;
import com.rockchip.mediacenter.common.logging.Log;
import com.rockchip.mediacenter.common.logging.LogFactory;
import com.rockchip.mediacenter.mediaplayer.model.FileInfo;
import com.rockchip.mediacenter.mediaplayer.model.LastLevelFileInfo;
import com.rockchip.mediacenter.mediaplayer.ui.WaitDialog;
import com.rockchip.mediacenter.mediaserver.FileDirectoryManager;
import com.rockchip.mediacenter.mediaserver.FileDirectoryManager.Listener;
import com.rockchip.mediacenter.mediaserver.model.FlashStorageDevice;
import com.rockchip.mediacenter.mediaserver.model.SDCardStorageDevice;
import com.rockchip.mediacenter.mediaserver.model.StorageDevice;
import com.rockchip.mediacenter.mediaserver.model.StorageDeviceList;
import com.rockchip.mediacenter.mediaserver.model.USBStorageDevice;
import com.rockchip.mediacenter.plugins.widget.Alert;
import com.rockchip.mediacenter.plugins.widget.MediaGridView;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.TextView;

/**
 *
 * @author fxw
 * @since 1.0
 */
public class AddShareFolderActivity extends Activity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, Listener  {

	private static final Log logger = LogFactory.getLog(AddShareFolderActivity.class);
	public static final int DIALOG_OPENNING = 1;
	public static final int DIALOG_SHARE_DELETE = 2;
	private MediaGridView mMediaGridView;
	private FolderListAdapter mFolderListAdapter;
	private ViewGroup pathContainer;
	
	private FileDirectoryManager mFileDirectoryManager;
	private SystemDeviceManager mSystemDeviceManager;
	private List<String> mSharePathList = new ArrayList<String>();//用于记录已共享的文件路径
	private FileInfo selectedFileInfo;
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
		getWindow().setWindowAnimations(R.style.PopupAnimation);
		setContentView(R.layout.dms_share_folder);
		getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		initView();
		mFileDirectoryManager = new FileDirectoryManager(this);
		mSystemDeviceManager = new SystemDeviceManager(this);
		mSystemDeviceManager.setBindListener(new SystemDeviceManager.BindListener() {
			public void onBindCompleted() {
				mSharePathList = getShareFolderDataSource();
			}
		});
		mSystemDeviceManager.startManager();
	}
	
	private void initView(){
		pathContainer = (ViewGroup)findViewById(R.id.dms_ll_path);
		mMediaGridView = (MediaGridView)findViewById(R.id.dms_gv_folder_list);
		mMediaGridView.setOnItemClickListener(this);
		mMediaGridView.setOnItemLongClickListener(this);
		mFolderListAdapter = new FolderListAdapter(this, new ArrayList<FileInfo>());
		mFolderListAdapter.setAddShareFolderActivity(this);
		mMediaGridView.setMediaListAdapter(mFolderListAdapter);
		mMediaGridView.requestFocus();
		mMediaGridView.setSelection(0);
	}
	
	/** 
	 * <p>Title: onResume</p> 
	 * <p>Description: </p>  
	 * @see android.app.Activity#onResume() 
	 */
	@Override
	protected void onResume() {
		super.onResume();
		showRootDirectory();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
		intentFilter.addDataScheme("file");
		registerReceiver(mDeviceMountListener, intentFilter);
	}
	
	/** 
	 * <p>Title: onPause</p> 
	 * <p>Description: </p>  
	 * @see android.app.Activity#onPause() 
	 */
	@Override
	protected void onPause() {
		super.onPause();
		setResult(RESULT_OK);
		unregisterReceiver(mDeviceMountListener);
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
	
	/**
	 * 是否在根目录
	 * @return
	 */
	private boolean isRootDirecotry(){
		return mPathList.size()==0;
	}
	
	/**
	 * 显示根目录
	 * @return
	 */
	public void showRootDirectory(){
		mFileDirectoryManager.browseTopDirectory();
		mPathList.clear();
		updateTitle(null);
		mMediaGridView.requestFocus();
	}
	
	public List<FileInfo> getDataSource(){
		return mMediaGridView.getDataSource();
	}
	
	public List<String> getShareFolderDataSource(){
    	return mSystemDeviceManager.queryShareDirectory();
    }
	
	/** 
	 * <p>Title: updateTitle</p> 
	 * <p>Description: </p> 
	 * @param currentPath 
	 * @see com.rockchip.mediacenter.mediaserver.FileDirectoryManager.Listener#updateTitle(java.lang.String) 
	 */
	@Override
	public void updateTitle(String currentPath) {
		runOnUiThread(new Runnable(){
			public void run() {
				pathContainer.removeAllViews();
				String rootName = getString(R.string.dms_device_storage);
				pathContainer.addView(createPathView(rootName, 0, null));
				
				if(mPathList.size()>0){
					for(int i=0; i<mPathList.size(); i++){
						FileInfo fileInfo = mPathList.get(i);
						if(!fileInfo.isFileItem()) continue;
						View view = createPathView(fileInfo.getTitle(), i+1, fileInfo);
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
				showDialog(DIALOG_OPENNING);
				if(linkID==0){//根目录
					showRootDirectory();
				}else{
					mFileDirectoryManager.browseDirectory(fileInfo);
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
	 * <p>Title: setDataSource</p> 
	 * <p>Description: </p> 
	 * @param fileInfoList 
	 * @see com.rockchip.mediacenter.mediaserver.FileDirectoryManager.Listener#setDataSource(java.util.List) 
	 */
	@Override
	public void setDataSource(final List<FileInfo> fileInfoList) {
		if(!mFileDirectoryManager.isTopDirectory()){
			fileInfoList.add(0, new LastLevelFileInfo());
		}
		runOnUiThread(new Runnable(){
			public void run() {
				mMediaGridView.setDataSource(fileInfoList);
				try{
					dismissDialog(DIALOG_OPENNING);
				}catch(Exception ex){}
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
	public List<FileInfo> getTopLevelDirecotry() {
		StorageDeviceList deviceList = getStorageDeviceList(true);
    	List<FileInfo> fileInfoList = new ArrayList<FileInfo>();
    	StorageDevice storageDevice = null;
    	for(int i=0; i<deviceList.size(); i++){
    		storageDevice = deviceList.get(i);
    		FileInfo fileInfo = new FileInfo(new File(storageDevice.getPath()));
    		fileInfo.setTitle(storageDevice.getName());
    		fileInfo.setIcon(storageDevice.getIcon());
    		fileInfoList.add(fileInfo);
    	}
    	return fileInfoList;
	}
	/**
     * 获取存储设备列表
     */
    public StorageDeviceList getStorageDeviceList(boolean onlyLiving){
    	StorageDeviceList deviceList = new StorageDeviceList();
    	StorageDevice sdDevice = new SDCardStorageDevice(getString(R.string.dms_sdcard_storage));
    	StorageDevice flashDevice = new FlashStorageDevice(getString(R.string.dms_flash_storage));
    	StorageDevice usbDevice = new USBStorageDevice(getString(R.string.dms_usb_storage));
    	sdDevice.setIcon(this, R.drawable.dms_storage_sdcard);
    	flashDevice.setIcon(this, R.drawable.dms_storage_flash);
    	usbDevice.setIcon(this, R.drawable.dms_storage_usb);
    	if(onlyLiving){
	    	if(flashDevice.isLiving())
	    		deviceList.add(flashDevice);
	    	if(sdDevice.isLiving())
	    		deviceList.add(sdDevice);
	    	if(usbDevice.isLiving())
	    		deviceList.add(usbDevice);
    	}else{
    		deviceList.add(flashDevice);
    		deviceList.add(sdDevice);
    		deviceList.add(usbDevice);
    	}
    	return deviceList;
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
		switchShareFolder(fileInfo);
		return true;
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
			if(fileInfo!=null){
				switchShareFolder(fileInfo);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	/**
	 * 开关文件夹共享
	 * @param fileInfo
	 */
	private void switchShareFolder(FileInfo fileInfo){
		if(!fileInfo.isFileItem()){
			return;
		}
		String path = fileInfo.getPath();
		if(isSharedFolder(fileInfo)){//已共享-->取消共享
			mSystemDeviceManager.deleteShareDirectory(path);
			int index = mSharePathList.indexOf(path);
			if(index>=0) mSharePathList.remove(index);
		}else if(!mFileDirectoryManager.isTopDirectory()){//设置共享
			mSystemDeviceManager.addShareDirectory(path);
			mSharePathList.add(path);
		}
		mFolderListAdapter.notifyDataSetChanged();
	}
	
	/**
	 * 是否已共享
	 * @param fileInfo
	 * @return
	 */
	public boolean isSharedFolder(FileInfo fileInfo){
		if(fileInfo==null) return false;
		return (mSharePathList.indexOf(fileInfo.getPath())!=-1);
	}

	/** 
	 * <p>Title: onItemClick</p> 
	 * <p>Description: </p> 
	 * @param parent
	 * @param view
	 * @param position
	 * @param id 
	 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long) 
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		final FileInfo fileInfo = (FileInfo)parent.getItemAtPosition(position);
		if(fileInfo.isDir()){
			if(isSharedFolder(fileInfo)){
				selectedFileInfo = fileInfo;
				showDialog(DIALOG_SHARE_DELETE);
			}else{
				showDialog(DIALOG_OPENNING);
				if(LastLevelFileInfo.isLastLevelFileInfo(fileInfo)){
					if(mPathList.size()==1){//在一级目录
						showRootDirectory();
					}else{
						mPathList.remove(mPathList.size()-1);
						mFileDirectoryManager.browseLastLevelDirectory();
					}
				}else{
					mPathList.add(fileInfo);
					mFileDirectoryManager.browseDirectChildren(fileInfo);
				}
			}
		}
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
			String msg = String.format(getString(R.string.dms_remove_directory), selectedFileInfo.getTitle());
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
		switch(id){
		case DIALOG_OPENNING:{
			WaitDialog waitDialog = new WaitDialog(this);
			waitDialog.setMessage(getString(R.string.dialog_openning_msg));
			return waitDialog;
		}
		case DIALOG_SHARE_DELETE:{
			if(selectedFileInfo!=null&&selectedFileInfo.isFileItem()){
				Alert.Builder builder = new Alert.Builder(this);
				builder.setTitle(R.string.dialog_prompt);
				String msg = String.format(getString(R.string.dms_remove_directory), selectedFileInfo.getTitle());
				builder.setMessage(msg);
				builder.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int which) {
						switchShareFolder(selectedFileInfo);
					}
				});
				builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int which) {
						dismissDialog(DIALOG_SHARE_DELETE);
					}
				});
				return builder.create();
			}
		}
		}
		return super.onCreateDialog(id);
	}
	
	private BroadcastReceiver mDeviceMountListener = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Uri uri = intent.getData();
			if(Intent.ACTION_MEDIA_EJECT.equals(action)
					||Intent.ACTION_MEDIA_UNMOUNTED.equals(action)){
				if(isRootDirecotry()){
					showRootDirectory();
				}else if(uri!=null){
					String uriPath = uri.getPath();
					String currRootPath = mPathList.get(0).getPath();
					if(currRootPath.equals(uriPath)){
						showRootDirectory();
						logger.debug("unmounted device. "+uriPath);
					}
				}
			}else if(Intent.ACTION_MEDIA_MOUNTED.equals(action)){
				if(isRootDirecotry()){
					showRootDirectory();
				}
			}
		}
	};
	
}
