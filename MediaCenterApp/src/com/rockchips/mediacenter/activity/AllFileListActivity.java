package com.rockchips.mediacenter.activity;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import momo.cn.edu.fjnu.androidutils.utils.BitmapUtils;
import momo.cn.edu.fjnu.androidutils.utils.JsonUtils;
import momo.cn.edu.fjnu.androidutils.utils.SizeUtils;
import momo.cn.edu.fjnu.androidutils.utils.StorageUtils;
import momo.cn.edu.fjnu.androidutils.utils.ToastUtils;
import org.fourthline.cling.support.model.container.Container;
import org.json.JSONObject;
import org.xutils.view.annotation.ViewInject;
import com.rockchips.mediacenter.adapter.AllFileListAdapter;
import com.rockchips.mediacenter.audioplayer.InternalAudioPlayer;
import com.rockchips.mediacenter.bean.Device;
import com.rockchips.mediacenter.bean.FileInfo;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.imageplayer.InternalImagePlayer;
import com.rockchips.mediacenter.imageplayer.downloader.FileService;
import com.rockchips.mediacenter.modle.task.AllFileLoadTask;
import com.rockchips.mediacenter.modle.task.FileMutiDeleteTask;
import com.rockchips.mediacenter.modle.task.FileOpTask;
import com.rockchips.mediacenter.modle.task.FileSearchTask;
import com.rockchips.mediacenter.service.UpnpFileLoadCallback;
import com.rockchips.mediacenter.utils.ActivityUtils;
import com.rockchips.mediacenter.utils.DialogUtils;
import com.rockchips.mediacenter.utils.MediaFileUtils;
import com.rockchips.mediacenter.utils.GetDateUtil;
import com.rockchips.mediacenter.videoplayer.InternalVideoPlayer;

import android.R.integer;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import com.rockchips.mediacenter.view.FileDeleteTipDialog;
import com.rockchips.mediacenter.view.FileOpDialog;
import com.rockchips.mediacenter.view.FileRenameDialog;
import com.rockchips.mediacenter.view.FileSearchDialog;
import com.rockchips.mediacenter.view.FileSortDialog;
import com.rockchips.mediacenter.view.OprationProgressDialog;
import com.rockchips.mediacenter.view.PreviewWidget;
import com.rockchips.mediacenter.R;
import com.rockchips.mediacenter.utils.ResLoadUtil;
import com.rockchips.mediacenter.utils.DiskUtil;
import com.rockchips.mediacenter.bean.LocalDeviceInfo;
import com.rockchips.mediacenter.bean.LocalMediaInfo;

/**
 * @author GaoFei
 * 所有文件浏览页面
 */
public class AllFileListActivity extends AppBaseActivity implements OnItemSelectedListener, OnItemClickListener, OnItemLongClickListener,FileOpDialog.Callback{

	public static final String TAG = "AllFileListActivity";
	protected static final int START_PLAYER_REQUEST_CODE = 99;
	@ViewInject(R.id.text_path_title)
	private TextView mTextPathTitle;
	@ViewInject(R.id.list_file)
	private ListView mListFile;
	@ViewInject(R.id.widget_preview)
	private PreviewWidget mWidgetPreview;
	@ViewInject(R.id.layout_no_files)
	private RelativeLayout mLayoutNoFiles;
	@ViewInject(R.id.layout_search_no_data)
	private LinearLayout mLayoutSearchNoData;
	@ViewInject(R.id.progress_loading)
	private ProgressBar mPregressLoading;
	@ViewInject(R.id.layout_content_page)
	private LinearLayout mLayoutContentPage;
	@ViewInject(R.id.text_file_name)
	private TextView mTextFileName;
	/**
	 * 当前设备
	 */
	private Device mCurrDevice;
	/**
	 * 当前目录路径
	 */
	private String mCurrFolder;
	/**
	 * 当前目录信息
	 */
	private FileInfo mCurrDirFileInfo;
	/**
	 * 当前媒体文件类型
	 */
	private int mCurrMediaType;
	private AllFileListAdapter mAllFileListAdapter;
	/**
	 * 文件列表加载器
	 */
	private AllFileLoadTask mAllFileLoadTask;
	
	/**
	 * 当前焦点文件
	 */
	private FileInfo mCurrentFileInfo;
	
	private Bitmap mOldBitmap;
	
	/**
	 * 上次选中的路径
	 */
	private String mLastSelectPath;
	
	/**
	 * 更新音频或视频预览图监听器
	 */
	private RefreshPreviewReceiver mRefreshPreviewReceiver;
	/**
	 * 文件操作任务
	 */
	private FileOpTask mFileOpTask;
	/**
	 * 文件重命名对话框
	 */
	private FileRenameDialog mRenameDialog;	
	/**文件搜索对话框*/
	private FileSearchDialog mFileSearchDialog;
	/**
	 * Bitmap内存缓存器
	 */
	private LruCache<String, Bitmap> mMemoryBitmapCache;
	
	/**
	 * 当前加载的文件列表
	 */
	private List<FileInfo> mLoadFileInfos;
	/**
	 * Upnp文件加载器
	 */
	private UpnpFileLoad mUpnpFileLoad = new UpnpFileLoad();
	/**
	 * 当前Upnp文件目录
	 */
	private Container mCurrentContainer;
	/**
	 * 上级Upnp文件目录
	 */
	private Container mLastContainer;
	/**
	 * Upnp目录访问列表
	 */
	private LinkedList<Container> mContainers = new LinkedList<>();
	/**文件排序对话框*/
	private FileSortDialog mFileSortDialog;
	/**排序方式*/
	private int mSortWay;
	/**排序类型*/
	private int mSortType;
	/**是否是删除模式*/
	private boolean mIsDeleteMode;
	/**选中删除的文件*/
	private List<FileInfo> mSelectDeleteFileInfos = new ArrayList<>();
	/**是否是搜索模式*/
	private boolean mIsSearchMode;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        /**test crash*/
       /* String ex = null;
        ex.toCharArray();*/
    }
    
    
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Log.i(TAG, "mListFile->onItemclick");
		FileInfo fileInfo = (FileInfo)parent.getAdapter().getItem(position);
		if(mIsDeleteMode){
			CheckBox checkBox = (CheckBox)view.findViewById(R.id.check_select);
			checkBox.setChecked(!checkBox.isChecked());
			if(checkBox.isChecked())
				mSelectDeleteFileInfos.add(fileInfo);
			else
				mSelectDeleteFileInfos.remove(fileInfo);
			return;
		}
		if(fileInfo.getType() == ConstData.MediaType.FOLDER){
			mCurrDirFileInfo = fileInfo;
			mCurrFolder = fileInfo.getPath();
			if(mCurrDevice.getDeviceType() != ConstData.DeviceType.DEVICE_TYPE_DMS){
				loadFiles();
			}else{
				if(mCurrMediaType == ConstData.MediaType.FOLDER){
					mCurrentContainer = createContainerFromFileInfo(fileInfo);
					mContainers.add(mCurrentContainer);
					DialogUtils.showLoadingDialog(this, false);
					startTimer(ConstData.MAX_LOAD_FILES_TIME);
					mDeviceMonitorService.loadUpnpFile(mCurrentContainer, mCurrDevice, mUpnpFileLoad);
				}else{
					loadFiles();
				}
			}
		}else{
			loadActivity(fileInfo);
		}
	}

	
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		return true;
	}
	
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		mCurrentFileInfo = (FileInfo)parent.getAdapter().getItem(position);
		refreshPreview(mCurrentFileInfo);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		
	}
	

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			if(mIsDeleteMode && mCurrDevice.getDeviceType() != ConstData.DeviceType.DEVICE_TYPE_DMS){
				mIsDeleteMode = false;
				loadFiles();
				return true;
			}
			if(mIsSearchMode && mCurrDevice.getDeviceType() != ConstData.DeviceType.DEVICE_TYPE_DMS){
				mIsSearchMode = false;
				loadFiles();
				return true;
			}
			if(mCurrDevice.getDeviceType() != ConstData.DeviceType.DEVICE_TYPE_DMS && !mCurrFolder.equals(mCurrDevice.getLocalMountPath())){
				mLastSelectPath = mCurrFolder;
				if(mCurrMediaType == ConstData.MediaType.FOLDER){
					mCurrFolder = new File(mCurrFolder).getParentFile().getPath();
					loadFiles();
				}
				else{
					mCurrFolder = mCurrDevice.getLocalMountPath();
					loadFiles();
				}
				return true;
			}
			if(mCurrDevice.getDeviceType() == ConstData.DeviceType.DEVICE_TYPE_DMS && mCurrentContainer != null 
					&& !mCurrentContainer.getId().equals("0") && mCurrMediaType == ConstData.MediaType.FOLDER){
				mLastContainer = mContainers.removeLast();
				mCurrentContainer = mContainers.getLast();
				DialogUtils.showLoadingDialog(this, false);
				startTimer(ConstData.MAX_LOAD_FILES_TIME);
				mDeviceMonitorService.loadUpnpFile(mCurrentContainer, mCurrDevice, mUpnpFileLoad);
				return true;
			}
			if(mCurrDevice.getDeviceType() == ConstData.DeviceType.DEVICE_TYPE_DMS && !mCurrDevice.getLocalMountPath().equals(mCurrFolder)
					&& mCurrMediaType != ConstData.MediaType.FOLDER){
				mLastSelectPath = mCurrFolder;
				mCurrFolder = mCurrDevice.getLocalMountPath();
				loadFiles();
				return true;
			}
		}else{
			if(mCurrDevice.getDeviceType() == ConstData.DeviceType.DEVICE_TYPE_DMS || 
					mCurrMediaType != ConstData.MediaType.FOLDER)
				return super.onKeyDown(keyCode, event);
			boolean isEmptyFolder = (mLoadFileInfos == null || mLoadFileInfos.size() == 0);
			String strCopyFileInfo = StorageUtils.getDataFromSharedPreference(ConstData.SharedKey.COPY_FILE_PATH);
			String strMoveFileInfo = StorageUtils.getDataFromSharedPreference(ConstData.SharedKey.MOVE_FILE_PATH);
			boolean isEmptyPaste = TextUtils.isEmpty(strCopyFileInfo) && TextUtils.isEmpty(strMoveFileInfo);
			if(isEmptyFolder && isEmptyPaste)
				return super.onKeyDown(keyCode, event);
			if(isEmptyFolder){
				try{
					FileInfo copyOrMoveFileInfo = (FileInfo) (TextUtils.isEmpty(strCopyFileInfo) ? JsonUtils.jsonToObject(FileInfo.class, new JSONObject(strMoveFileInfo)) :JsonUtils.jsonToObject(FileInfo.class, new JSONObject(strCopyFileInfo)));
					if(mCurrentFileInfo.getPath().startsWith(copyOrMoveFileInfo.getPath()) || !new File(copyOrMoveFileInfo.getPath()).exists())
						return super.onKeyDown(keyCode, event);
				}catch (Exception e){
					//no handle
					return super.onKeyDown(keyCode, event);
				}
			}
			if(keyCode == KeyEvent.KEYCODE_MENU){
				new FileOpDialog(this, mCurrentFileInfo, isEmptyFolder, this).show();
				return true;
			}else if(keyCode == KeyEvent.KEYCODE_DPAD_CENTER){
				if(event.isLongPress()){
					//长按打开操作对话框
					new FileOpDialog(this, mCurrentFileInfo, isEmptyFolder, this).show();
					return true;
				}
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onResume() {
	    super.onResume();
	    IntentFilter refershFilter  = new IntentFilter();
	    refershFilter.addAction(ConstData.BroadCastMsg.REFRESH_VIDEO_PREVIEW);
	    refershFilter.addAction(ConstData.BroadCastMsg.REFRESH_AUDIO_PREVIEW);
	    refershFilter.addAction(ConstData.BroadCastMsg.REFRESH_PHOTO_PREVIEW);
	    refershFilter.addAction(ConstData.BroadCastMsg.REFRESH_APK_PREVIEW);
	    refershFilter.addAction(ConstData.BroadCastMsg.DEVICE_UP);
	    LocalBroadcastManager.getInstance(this).registerReceiver(mRefreshPreviewReceiver, refershFilter);
	}
	
	@Override
	protected void onPause() {
	    super.onPause();
	    LocalBroadcastManager.getInstance(this).unregisterReceiver(mRefreshPreviewReceiver);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
	}
	
	@Override
	public void onServiceConnected() {
		if(mCurrDevice.getDeviceType() == ConstData.DeviceType.DEVICE_TYPE_DMS && 
				mCurrMediaType == ConstData.MediaType.FOLDER){
			mCurrentContainer = createRootContainer();
			mContainers.add(mCurrentContainer);
			mLastContainer = mCurrentContainer;
			DialogUtils.showLoadingDialog(this, false);
			startTimer(ConstData.MAX_LOAD_FILES_TIME);
			mDeviceMonitorService.loadUpnpFile(mCurrentContainer, mCurrDevice, mUpnpFileLoad);
		}
	
	}
	
	@Override
	public int getLayoutRes() {
		return R.layout.activity_file_list;
	}
	
	
	
	@Override
	public void init() {
		initDataAndView();
		initEvent();
	}
	
	@Override
	public void onCopy(FileInfo fileInfo) {
		if(fileInfo == null)
			return;
		//拷贝文件,存储当前待拷贝文件
		StorageUtils.saveDataToSharedPreference(ConstData.SharedKey.COPY_FILE_PATH, JsonUtils.objectToJson(fileInfo).toString());
		//接切信息设为空
		StorageUtils.saveDataToSharedPreference(ConstData.SharedKey.MOVE_FILE_PATH, "");
	}


	@Override
	public void onDelete(final FileInfo fileInfo) {
		if(ConstData.IS_SUPPORT_MUTIL_DELETE){
			//如果是删除模式
			if(mIsDeleteMode){
				if(mSelectDeleteFileInfos.size() > 0){
					mIsDeleteMode = false;
					//显示删除提示
					showMutiFileDeleteTipDialog();
				}else{
					ToastUtils.showToast(getString(R.string.select_delete_files));
				}
				
			}else{
				mSelectDeleteFileInfos.clear();
				mIsDeleteMode = true;
				loadFiles();
			}
			return;
		}
		showSingleFileDeleteTipDialog(fileInfo);
	}


	@Override
	public void onMove(FileInfo fileInfo) {
		StorageUtils.saveDataToSharedPreference(ConstData.SharedKey.MOVE_FILE_PATH, JsonUtils.objectToJson(fileInfo).toString());
		StorageUtils.saveDataToSharedPreference(ConstData.SharedKey.COPY_FILE_PATH,"");
	}


	@Override
	public void onPaste(FileInfo fileInfo) {
		final OprationProgressDialog opProgressDialog = new OprationProgressDialog(this, new OprationProgressDialog.Callback() {
			
			@Override
			public void onStop() {
				mFileOpTask.setStopPaste(true);
			}
		});
		opProgressDialog.setCancelable(false);
		opProgressDialog.show();
		//黏贴文件
		mFileOpTask = new FileOpTask(mCurrDevice, new FileOpTask.CallBack() {
			
			@Override
			public void onFinish(int errorCode) {
				if(opProgressDialog != null && opProgressDialog.isShowing())
					opProgressDialog.dismiss();
				if(errorCode == ConstData.FileOpErrorCode.WRITE_ERR)
					ToastUtils.showToast(getString(R.string.no_write_permission));
				else if(errorCode == ConstData.FileOpErrorCode.PASTE_ERR)
					ToastUtils.showToast(getString(R.string.paste_error));
				else if(errorCode == ConstData.FileOpErrorCode.PASTE_SAME_FILE)
					ToastUtils.showToast(getString(R.string.exist_same_file));
				else if(errorCode == ConstData.FileOpErrorCode.NO_ENOUGH_SPACE)
					ToastUtils.showToast(getString(R.string.no_left_space));
				else if(errorCode == ConstData.FileOpErrorCode.READ_ERR)
					ToastUtils.showToast(getString(R.string.read_err));
				else if(errorCode == ConstData.FileOpErrorCode.FILE_CREATE_FAILED)
					ToastUtils.showToast(getString(R.string.file_create_failed));
				else if(errorCode == ConstData.FileOpErrorCode.PASTE_PART_FILE_ERR){
					ToastUtils.showToast(getString(R.string.part_file_paste_err));
					loadFiles();
				}else if(errorCode == ConstData.FileOpErrorCode.STOP_PASTE){
					ToastUtils.showToast(getString(R.string.stop_paste));
					loadFiles();
				}else if(errorCode == ConstData.FileOpErrorCode.FILE_NOT_EXIST){
					//源文件不存在
					ToastUtils.showToast(getString(R.string.file_not_exist));
				}
				else
					loadFiles();
			}
			
			@Override
			public void onProgress(int value) {
				//更新进度条
				opProgressDialog.updateProgress(value);
			}
		});
		mFileOpTask.setOpMode(ConstData.FileOpMode.PASTE);
		mFileOpTask.setStopPaste(false);
		boolean isEmptyFolder = (mLoadFileInfos == null || mLoadFileInfos.size() == 0);
		mFileOpTask.execute(fileInfo, isEmptyFolder);
	}


	@Override
	public void onRename(FileInfo fileInfo) {
		if(mRenameDialog == null){
			mRenameDialog = new FileRenameDialog(this, mCurrDevice, fileInfo, new FileRenameDialog.Callback() {
				@Override
				public void onFinish(int errorCode) {
					DialogUtils.closeLoadingDialog();
					if(errorCode == ConstData.FileOpErrorCode.RENAME_ERR){
						ToastUtils.showToast(getString(R.string.rename_failed));
					}else{
						loadFiles();
					}
					
				}
			});
		}else{
			mRenameDialog.setAllFileInfo(fileInfo);
		}
		mRenameDialog.show();
	}
	
	@Override
	public void onSort(FileInfo fileInfo) {
		if(mFileSortDialog == null)
			mFileSortDialog = new FileSortDialog(this, new FileSortDialog.CallBack() {
				@Override
				public void onSelected(int way, int type) {
					if(mSortWay != way || mSortType != type){
						mSortWay = way;
						mSortType = type;
						loadFiles();
					}
				}
			});
		else
			mFileSortDialog.focusSortWayList();
		mFileSortDialog.show();
	}
	
	@Override
	public void onSearch(FileInfo fileInfo) {
		if(mFileSearchDialog == null)
			mFileSearchDialog = new FileSearchDialog(this, new FileSearchDialog.Callback() {
				
				@Override
				public void onOk(String text) {
					mIsSearchMode = true;
					DialogUtils.showLoadingDialog(AllFileListActivity.this, false);
					//启动搜索异步块
					FileSearchTask searchTask = new FileSearchTask(mLoadFileInfos, new FileSearchTask.Callback() {
						
						@Override
						public void OnFinished(List<FileInfo> resultFileInfos) {
							DialogUtils.closeLoadingDialog();
							if(resultFileInfos != null && resultFileInfos.size() > 0){
								mLayoutContentPage.setVisibility(View.VISIBLE);
								mLayoutNoFiles.setVisibility(View.GONE);
								mListFile.requestFocus();
								mAllFileListAdapter = new AllFileListAdapter(AllFileListActivity.this, R.layout.adapter_file_list_item, resultFileInfos);
								mAllFileListAdapter.setIsDeleteMode(mIsDeleteMode);
								mListFile.setAdapter(mAllFileListAdapter);
								if(!TextUtils.isEmpty(mLastSelectPath)){
									int position = getFilePosition(mLastSelectPath, resultFileInfos);
									mListFile.setSelection(position);
								}
							}else{
								mTextFileName.setText("");
								mLayoutContentPage.setVisibility(View.GONE);
								mLayoutNoFiles.setVisibility(View.VISIBLE);
							}
						}
					});
					searchTask.execute(text);
				}
			});
		else
			mFileSearchDialog.focusInputSearch();
		mFileSearchDialog.show();
	}
	
    public void initDataAndView(){
    	mMemoryBitmapCache = new LruCache<String, Bitmap>((int)Runtime.getRuntime().maxMemory() / 8){
    		@Override
    		protected int sizeOf(String key, Bitmap value) {
    			return value.getByteCount() / 1024;
    		}
    	};
        mRefreshPreviewReceiver = new RefreshPreviewReceiver();
    	mPregressLoading.setVisibility(View.GONE);
    	mCurrDevice = (Device)getIntent().getSerializableExtra(ConstData.IntentKey.EXTRAL_LOCAL_DEVICE);
    	mCurrMediaType = getIntent().getIntExtra(ConstData.IntentKey.EXTRAL_MEDIA_TYPE, -1);
    	//挂载目录作为当前目录
    	mCurrFolder = mCurrDevice.getLocalMountPath();
    	if(mCurrDevice.getDeviceType() == ConstData.DeviceType.DEVICE_TYPE_DMS &&
    			mCurrMediaType == ConstData.MediaType.FOLDER){
    		mCurrentContainer = createRootContainer();
    	}else{
    		//加载mSortType,mSortWay
    		String sortWay = StorageUtils.getDataFromSharedPreference(ConstData.SharedKey.FILE_SORT_WAY);
    		String sortType = StorageUtils.getDataFromSharedPreference(ConstData.SharedKey.FILE_SORT_TYPE);
    		if(TextUtils.isEmpty(sortWay)){
    			mSortWay = ConstData.FILE_SORT_WAY.TYPE;
    		}else{
    			mSortWay = Integer.parseInt(sortWay);
    		}
    		if(TextUtils.isEmpty(sortType)){
    			mSortType = ConstData.FILE_SORT_TYPE.INCREASING;
    		}else{
    			mSortType = Integer.parseInt(sortType);
    		}
    		loadFiles();
    	}
    	
    }

    public void initEvent(){
    	mListFile.setOnItemClickListener(this);
    	mListFile.setOnItemSelectedListener(this);
    	mListFile.setOnItemLongClickListener(this);
    }
    
    private void refreshPreview(FileInfo fileInfo)
    {
    	//更新头部信息
        mTextFileName.setText(fileInfo.getName());
    	mWidgetPreview.updateName(fileInfo.getName());
    	mWidgetPreview.updateOtherText(getAllFilePreviewInfo(fileInfo));
    	mWidgetPreview.updateImage(getPreviewIcon(fileInfo.getType()));
		Bitmap previewBitmap = null;
        switch (fileInfo.getType())
        {
            case ConstData.MediaType.AUDIO:
            case ConstData.MediaType.VIDEO:
            	updateOtherText(fileInfo);
            	if(TextUtils.isEmpty(fileInfo.getPreviewPath())){
            		if(fileInfo.getType() == ConstData.MediaType.VIDEO){
            			loadBitmapForVideoFile(fileInfo);
            		}else{
            			loadBitmapForAudioFile(fileInfo);
            		}
            		
            	}else if(!ConstData.UNKNOW.equals(fileInfo.getPreviewPath())){   
            		recycleOldPreview();
            		previewBitmap = BitmapUtils.getScaledBitmapFromFile(fileInfo.getPreviewPath(), SizeUtils.dp2px(this, 280), SizeUtils.dp2px(this, 280));
        			mOldBitmap = previewBitmap;
            		if(previewBitmap != null){
        				mWidgetPreview.updateImage(previewBitmap);
        			}
            	}
                break;
            case ConstData.MediaType.IMAGE:
            	if(TextUtils.isEmpty(fileInfo.getPreviewPath())){
            		loadBitmapForPhotoFile(fileInfo);
            	}else{
            		recycleOldPreview();
            		previewBitmap = BitmapUtils.getScaledBitmapFromFile(fileInfo.getPreviewPath(), SizeUtils.dp2px(this, 280), SizeUtils.dp2px(this, 280));
        			mOldBitmap = previewBitmap;
            		if(previewBitmap != null){
        				mWidgetPreview.updateImage(previewBitmap);
        			}
            	}
            	updateOtherText(fileInfo);
                break;
            case ConstData.MediaType.APK:
            	if(TextUtils.isEmpty(fileInfo.getPreviewPath())){
            		loadBitmapForApkFile(fileInfo);
            	}else{
            		recycleOldPreview();
            		previewBitmap = BitmapUtils.getScaledBitmapFromFile(fileInfo.getPreviewPath(), SizeUtils.dp2px(this, 280), SizeUtils.dp2px(this, 280));
        			mOldBitmap = previewBitmap;
            		if(previewBitmap != null){
        				mWidgetPreview.updateImage(previewBitmap);
        			}
            	}
                break;
        }  
	

    }
    
    /**
     * 回收上次缓存文件
     */
    private void recycleOldPreview(){
    	if(mOldBitmap != null && !mOldBitmap.isRecycled())
			mOldBitmap.recycle();
    }
    
    
	/**
	 * 加载文件夹列表
	 */
	public void loadFiles(){
		DialogUtils.showLoadingDialog(this, false);
		//开启定时器
		startTimer(ConstData.MAX_LOAD_FILES_TIME);
		mAllFileLoadTask = new AllFileLoadTask(new AllFileLoadTask.CallBack() {
			@Override
			public void onGetFiles(List<FileInfo> fileInfos) {
				mLoadFileInfos = fileInfos;
			    endTimer();
				DialogUtils.closeLoadingDialog();
				if(isOverTimer())
				    return;
				if(mCurrDevice.getDeviceType() != ConstData.DeviceType.DEVICE_TYPE_DMS)
					mTextPathTitle.setText(mCurrFolder.substring(mCurrFolder.lastIndexOf("/") + 1, mCurrFolder.length()));
				else{
					if(mCurrDirFileInfo == null)
						mTextPathTitle.setText(mCurrDevice.getDeviceName());
					else
						mTextPathTitle.setText(mCurrDirFileInfo.getName());
				}
					
				if(fileInfos != null && fileInfos.size() > 0){
					mLayoutContentPage.setVisibility(View.VISIBLE);
					mLayoutNoFiles.setVisibility(View.GONE);
					mListFile.requestFocus();
					mAllFileListAdapter = new AllFileListAdapter(AllFileListActivity.this, R.layout.adapter_file_list_item, fileInfos);
					mAllFileListAdapter.setIsDeleteMode(mIsDeleteMode);
					mListFile.setAdapter(mAllFileListAdapter);
					if(!TextUtils.isEmpty(mLastSelectPath)){
						int position = getFilePosition(mLastSelectPath, fileInfos);
						mListFile.setSelection(position);
					}
				}else{
					mTextFileName.setText("");
					mLayoutContentPage.setVisibility(View.GONE);
					mLayoutNoFiles.setVisibility(View.VISIBLE);
				}
			}
		});
		mAllFileLoadTask.execute(mCurrDevice, mCurrMediaType, mCurrFolder, mSortWay, mSortType);
	}
	
	
    
    
    private String getAllFilePreviewInfo(FileInfo fileInfo){
    	String info = "";
    	if(fileInfo.getType() == ConstData.MediaType.FOLDER){
    		if(mCurrMediaType == ConstData.MediaType.FOLDER)
    			info = getString(R.string.file_tip) + fileInfo.getChildCount();
    		else if(mCurrMediaType == ConstData.MediaType.AUDIOFOLDER)
    			info = getString(R.string.file_tip) + fileInfo.getMusicCount();
    		else
    			info = getString(R.string.file_tip) + fileInfo.getVideoCount();
    	}else{
    		 String dateStr = null;
    	     dateStr = GetDateUtil.getTime(this, fileInfo.getModifyTime() / 1000);
    	     if (dateStr == null){
    	    	 dateStr = getString(R.string.unknown);
    	     }
    	     info = getString(R.string.file_size_tip) + getFileSize(fileInfo.getSize()) + "\n" + getString(R.string.modify_time_tip) + dateStr;
    	}
    	return info;
    }
    
    
    protected String getFileSize(long size){
        if (size < 1024 && size > 0)
        {
            return size + " " + ResLoadUtil.getStringById(this, R.string.unit_disk_size_b);
        }
        else if (size == 0)
        {
            return ResLoadUtil.getStringById(this, R.string.real_unknown);
        }

        return DiskUtil.getDiskSizeString(this, Long.valueOf(size / 1024).intValue(), R.string.unknown, R.string.unit_disk_size_kb,
                R.string.unit_disk_size_mb, R.string.unit_disk_size_gb, R.string.unit_disk_size_tb);
    }
    
    protected Bitmap getPreviewIcon(int type)
    {
    	String cacheKey = getCacheKey(type);
    	if(cacheKey != null){
    		Bitmap cacheBitmap = mMemoryBitmapCache.get(cacheKey);
    		if(cacheBitmap != null)
    			return cacheBitmap;
    	}
        int resId = R.drawable.icon_preview_unknow;
        switch (type)
        {
            case ConstData.MediaType.AUDIO:
                resId = R.drawable.icon_preview_audio;
                break;
            case ConstData.MediaType.IMAGE:
                resId = R.drawable.icon_preview_image;
                break;
            case ConstData.MediaType.FOLDER:
                resId = R.drawable.icon_preview_folder;
                break;
            case ConstData.MediaType.VIDEO:
                resId = R.drawable.icon_preview_video;
                break;
            case ConstData.MediaType.DEVICE:
                resId = R.drawable.icon_preview_disk;
                break;
            case ConstData.MediaType.APK:
            	resId = R.drawable.icon_apk_preview;
            	break;
            case ConstData.MediaType.UNKNOWN_TYPE:
                resId = R.drawable.icon_preview_unknow;
                break;
        }
        return getBitmapById(type, resId);
    }

    
    /**
     * 获取缓存key
     * @param mediaType
     * @return
     */
    public String getCacheKey(int mediaType){
    	String cacheKey = null;
    	switch(mediaType){
    		case ConstData.MediaType.AUDIO:
    			cacheKey = ConstData.DefaultFileIconKey.AUDIO;
    			break;
    		case ConstData.MediaType.VIDEO:
    			cacheKey = ConstData.DefaultFileIconKey.VIDEO;
    			break;
    		case ConstData.MediaType.FOLDER:
    			cacheKey = ConstData.DefaultFileIconKey.FOLDER;
    			break;
    		case ConstData.MediaType.IMAGE:
    			cacheKey = ConstData.DefaultFileIconKey.IMAGE;
    			break;
    		case ConstData.MediaType.APK:
    			cacheKey = ConstData.DefaultFileIconKey.APK;
    			break;
    		case ConstData.MediaType.UNKNOWN_TYPE:
    			cacheKey = ConstData.DefaultFileIconKey.UNKNOW_TYPE;
    			break;
    	}
    	return cacheKey;
    }
    
    private Bitmap getBitmapById(int mediaType, int resId){
    	Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resId);
    	/*if(mOldBitmap != null && !mOldBitmap.isRecycled())
    		mOldBitmap.recycle();
    	mOldBitmap = bitmap;*/
    	if(bitmap != null)
    		mMemoryBitmapCache.put(getCacheKey(mediaType), bitmap);
    	return bitmap;
    }
    
    
    private void updateOtherText(FileInfo fileInfo)
    {
        String strInfo = null;
        switch(fileInfo.getType())
        {
            case ConstData.MediaType.AUDIO:
            	strInfo = String.format(getString(R.string.audio_preview_info), 
                		getFileSize(fileInfo.getSize()), 
                		getFileType(fileInfo.getName(),getString(R.string.music), mCurrDevice.getDeviceType()), 
                		getRunningTime(fileInfo),
                		formatCreateDate(fileInfo),getDescription(""));
            	break;
            case ConstData.MediaType.VIDEO:
            	strInfo = String.format(getString(R.string.audio_preview_info), 
                		getFileSize(fileInfo.getSize()), 
                		getFileType(fileInfo.getName(),getString(R.string.video), mCurrDevice.getDeviceType()), 
                		getRunningTime(fileInfo),
                		formatCreateDate(fileInfo),getDescription(""));
              break;
            // 显示尺寸
            case ConstData.MediaType.IMAGE:
                strInfo = String.format(getString(R.string.image_preview_info), getFileSize(fileInfo.getSize()),
                        getFileType(fileInfo.getName(),getString(R.string.picture), mCurrDevice.getDeviceType()), formatCreateDate(fileInfo),getDescription(""));
                break;
        }
        mWidgetPreview.updateOtherText(strInfo);
    }
    
    /**
     * 加载播放器
     */
    public void loadActivity(FileInfo fileInfo){
    	int fileType = fileInfo.getType();
    	if(fileType != ConstData.MediaType.AUDIO && fileType != ConstData.MediaType.VIDEO
    			&& fileType != ConstData.MediaType.IMAGE && fileType != ConstData.MediaType.APK){
    		return;
    	}
        Intent intent = new Intent();
        intent.putExtra(ConstData.IntentKey.IS_INTERNAL_PLAYER, true);
        intent.putExtra(ConstData.IntentKey.EXTRAL_LOCAL_DEVICE, mCurrDevice);
        intent.putExtra(LocalDeviceInfo.DEVICE_EXTRA_NAME, MediaFileUtils.getDeviceInfoFromDevice(mCurrDevice).compress());
        List<FileInfo> fileInfos = MediaFileUtils.filterFileInfos(mLoadFileInfos, fileType);
        List<LocalMediaInfo> mediaInfos = MediaFileUtils.getLocalMediaInfos(mLoadFileInfos, mCurrDevice, fileType);
        List<Bundle> mediaInfoList = new ArrayList<Bundle>();
        for(LocalMediaInfo itemInfo : mediaInfos){
        	mediaInfoList.add(itemInfo.compress());
        }
        int newPosition = 0;
        for(int i = 0; i != fileInfos.size(); ++i){
        	if(fileInfo.getName().equals(fileInfos.get(i).getName())){
        		newPosition = i;
        		break;
        	}
        }
        int requestCode = START_PLAYER_REQUEST_CODE;
        if (fileInfo.getType() == ConstData.MediaType.AUDIO)
        {
            intent.setClass(this, InternalAudioPlayer.class);
            intent.putExtra(ConstData.IntentKey.CURRENT_INDEX, newPosition);
            InternalAudioPlayer.setMediaList(fileInfos, newPosition);
        }
        else if (fileInfo.getType() == ConstData.MediaType.VIDEO)
        {
        	requestCode = ConstData.ActivityRequestCode.REQUEST_VIDEO_PLAYER;
            intent.setClass(this, InternalVideoPlayer.class);
            intent.putExtra(ConstData.IntentKey.CURRENT_INDEX, newPosition);
            InternalVideoPlayer.setMediaList(fileInfos, newPosition);
            if(android.os.Build.VERSION.SDK_INT >= 24){
                //关闭PIP页面
                //ActivityManager activityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
                List<Integer> videoPlayerTaskIds = ActivityUtils.getTaskIds(getPackageName() + "/" + InternalVideoPlayer.class.getName());
                if(videoPlayerTaskIds != null && videoPlayerTaskIds.size() > 0){
                    ActivityUtils.removeAllTask(videoPlayerTaskIds);
                }
            }    
        }
        else if (fileInfo.getType() == ConstData.MediaType.IMAGE)
        {
            intent.setClass(this, InternalImagePlayer.class);
            intent.putExtra(ConstData.IntentKey.IS_INTERNAL_PLAYER, true);
            intent.putExtra(ConstData.IntentKey.CURRENT_INDEX, newPosition);
            InternalImagePlayer.setMediaList(mediaInfoList, newPosition);
        }else if(fileInfo.getType() == ConstData.MediaType.APK){
        	Intent installIntent = new Intent(Intent.ACTION_VIEW);
        	installIntent.setDataAndType(Uri.fromFile(new File(fileInfo.getPath())), "application/vnd.android.package-archive");
        	installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        	startActivity(installIntent);
        	return;
        }
        startActivityForResult(intent, requestCode);
    }
    
    private static final int INDEX_OF_SPLIT_01 = -1;
    private static final int INDEX_OF_SPLIT_1 = 1;
    /** DTS2015012807455 解决音乐、视频，不显示时长的问题  by zWX238093 */
    protected String getRunningTime(FileInfo fileInfo)
    {
    	if(TextUtils.isEmpty(fileInfo.getDuration())){
    		return getString(R.string.unknown_durnation);
    	}
    	return fileInfo.getDuration();
    }
    
    private String getDescription(String description){	
		if (TextUtils.isEmpty(description))
		{		
			description = getString(R.string.unknown);
		}		
		return description;
	}
    
    private String formatCreateDate(FileInfo fileInfo){
        String dataStr;
        dataStr = GetDateUtil.getTime(AllFileListActivity.this, fileInfo.getModifyTime() / 1000);
        if (TextUtils.isEmpty(dataStr))
        {
            dataStr = getString(R.string.unknown);
        }
        return dataStr;
    }
    
    private String getFileType(String filename,String typename,int deviceType)
	{				
		Log.d(TAG, "=====deviceType==="+deviceType);
		String fileType=typename;
		if (deviceType == ConstData.DeviceType.DEVICE_TYPE_U 
				|| deviceType == ConstData.DeviceType.DEVICE_TYPE_SD
				|| deviceType == ConstData.DeviceType.DEVICE_TYPE_SMB
				|| deviceType == ConstData.DeviceType.DEVICE_TYPE_NFS
				|| deviceType == ConstData.DeviceType.DEVICE_TYPE_INTERNEL_STORAGE)
		{					
			fileType = filename.substring(filename.lastIndexOf(".")+1);
		}		
		return fileType;
	}
    
    
    
    /**
     * 加载视频文件缩列图
     * @param allFileInfo
     */
    private void loadBitmapForAudioFile(FileInfo fileInfo){
        //此处直接发送广播出去,服务接受后开始获取缩列图
        Intent loadIntent = new Intent(ConstData.BroadCastMsg.LOAD_AUDIO_PREVIEW);
        loadIntent.putExtra(ConstData.IntentKey.EXTRA_FILE_INFO, fileInfo);
        LocalBroadcastManager.getInstance(this).sendBroadcast(loadIntent);
    }
    
    /**
     * 获取视频文件的缩列图
     * @param fileInfo
     */
    private void loadBitmapForVideoFile(FileInfo fileInfo){
    	//此处直接发送广播出去,服务接受后开始获取缩列图
        Intent loadIntent = new Intent(ConstData.BroadCastMsg.LOAD_VIDEO_PREVIEW);
        loadIntent.putExtra(ConstData.IntentKey.EXTRA_FILE_INFO, fileInfo);
        LocalBroadcastManager.getInstance(this).sendBroadcast(loadIntent);
    }
    
   /**
    * 获取图片文件的预览图
    * @param allFileInfo
    */
    private void loadBitmapForPhotoFile(FileInfo fileInfo){
    	//此处直接发送广播出去,服务接受后开始获取缩列图
        Intent loadIntent = new Intent(ConstData.BroadCastMsg.LOAD_PHOTO_PREVIEW);
        loadIntent.putExtra(ConstData.IntentKey.EXTRA_FILE_INFO, fileInfo);
        LocalBroadcastManager.getInstance(this).sendBroadcast(loadIntent);
    }
    
    /**
     * 获取APK文件的预览图
     * @param fileInfo
     */
    private void loadBitmapForApkFile(FileInfo fileInfo){
    	//此处直接发送广播出去,服务接受后开始获取缩列图
        Intent loadIntent = new Intent(ConstData.BroadCastMsg.LOAD_APK_PREVIEW);
        loadIntent.putExtra(ConstData.IntentKey.EXTRA_FILE_INFO, fileInfo);
        LocalBroadcastManager.getInstance(this).sendBroadcast(loadIntent);
    }
    
    
    /**
     * 根据路径获取列表位置,最好改成异步实现
     * @param path
     * @param allFileInfos
     * @return
     */
	public int getFilePosition(String path, List<FileInfo> allFileInfos){
		int position = 0;
		if(allFileInfos != null && allFileInfos.size() > 0){
			for(int i = 0; i < allFileInfos.size(); ++i){
				if(mCurrDevice.getDeviceType() != ConstData.DeviceType.DEVICE_TYPE_DMS && allFileInfos.get(i).getPath().equals(path)){
					position = i;
					break;
				}else if(mCurrDevice.getDeviceType() == ConstData.DeviceType.DEVICE_TYPE_DMS){
					if(mCurrMediaType == ConstData.MediaType.FOLDER){
						try{
							JSONObject otherInfoObject = new JSONObject(allFileInfos.get(i).getOtherInfo());
							if(mLastContainer != null && mLastContainer.getId().equals(otherInfoObject.getString(ConstData.UpnpFileOhterInfo.ID))){
								position = i;
								break;
							}
							//otherInfoObject.getString(ConstData.UpnpFileOhterInfo.ID).equals(mLastContainer.get)
						}catch(Exception e){
							Log.e(TAG, "getFilePosition->exception:" + e);
						}
					}else{
						if(allFileInfos.get(i).getPath().equals(path)){
							position = i;
							break;
						}
							
					}
				}
			}
		}
		return position;
	}
	
	
    /**
     * 创建UPNP根目录
     * @return
     */
	private Container createRootContainer() {
		Container rootContainer = new Container();
		rootContainer.setId("0");
		rootContainer.setTitle(mCurrDevice.getDeviceName());
		return rootContainer;
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
	 * 显示单个文件删除提示
	 */
	private void showSingleFileDeleteTipDialog(final FileInfo fileInfo){
		FileDeleteTipDialog deleteTipDialog = new FileDeleteTipDialog(this, new FileDeleteTipDialog.CallBack() {
			@Override
			public void onOK() {
				mFileOpTask = new FileOpTask(mCurrDevice, new FileOpTask.CallBack() {
					@Override
					public void onFinish(int errorCode) {
						DialogUtils.closeLoadingDialog();
						if(errorCode == ConstData.FileOpErrorCode.WRITE_ERR){
							//没有写权限
							ToastUtils.showToast(getString(R.string.no_delete_permission));
						}else if(errorCode == ConstData.FileOpErrorCode.DELETE_PART_FILE_ERR){
							//部分文件无法删除
							ToastUtils.showToast(getString(R.string.delete_part_file_error));
						}else{
							loadFiles();
						}
						
					}
					
					@Override
					public void onProgress(int value) {
						
					}
				});
				mFileOpTask.setOpMode(ConstData.FileOpMode.DELETE);
				DialogUtils.showLoadingDialog(AllFileListActivity.this, false);
				mFileOpTask.execute(fileInfo);
			}
			
			@Override
			public void onCancel() {
				
			}
		});
		deleteTipDialog.show();
	}
	/**
	 * 显示多文件删除提示对话框
	 */
	private void showMutiFileDeleteTipDialog(){
		FileDeleteTipDialog deleteTipDialog = new FileDeleteTipDialog(this, new FileDeleteTipDialog.CallBack() {
			@Override
			public void onOK() {
				DialogUtils.showLoadingDialog(AllFileListActivity.this, false);
				FileMutiDeleteTask mutiDeleteTask = new FileMutiDeleteTask(new FileMutiDeleteTask.Callback() {
					
					@Override
					public void onProgress(int value) {						
					}
					
					@Override
					public void onFinished(int errCode) {
						DialogUtils.closeLoadingDialog();
						if(errCode == ConstData.FileOpErrorCode.NO_ERR){
							//删除成功
							ToastUtils.showToast(getString(R.string.alerady_delete_select));
						}else if(errCode == ConstData.FileOpErrorCode.DELETE_PART_FILE_ERR){
							//部分文件无法删除
							ToastUtils.showToast(getString(R.string.delete_part_file_error));
						}else if(errCode == ConstData.FileOpErrorCode.DELETE_ERR){
							//无法删除文件
							ToastUtils.showToast(getString(R.string.delete_select_error));
						}
						loadFiles();
					}
				}, mCurrDevice, mSelectDeleteFileInfos);
				//启动多文件删除任务
				mutiDeleteTask.execute();
			}
			
			@Override
			public void onCancel() {
				
			}
		});
		deleteTipDialog.setTipText(getString(R.string.delete_selected_files));
		deleteTipDialog.show();
	
	}
	
	
	class UpnpFileLoad implements UpnpFileLoadCallback{

		@Override
		public void onSuccess(List<FileInfo> fileInfos) {
			mLoadFileInfos = fileInfos;
		    endTimer();
			DialogUtils.closeLoadingDialog();
			if(isOverTimer())
			    return;
			mTextPathTitle.setText(mCurrentContainer.getTitle());
			if(fileInfos != null && fileInfos.size() > 0){
				mLayoutContentPage.setVisibility(View.VISIBLE);
				mLayoutNoFiles.setVisibility(View.GONE);
				mListFile.requestFocus();
				mAllFileListAdapter = new AllFileListAdapter(AllFileListActivity.this, R.layout.adapter_file_list_item, fileInfos);
				mListFile.setAdapter(mAllFileListAdapter);
				int position = getFilePosition(mLastSelectPath, fileInfos);
				mListFile.setSelection(position);
			}else{
				mLayoutContentPage.setVisibility(View.GONE);
				mLayoutNoFiles.setVisibility(View.VISIBLE);
				mTextFileName.setText("");
			}
		
		}

		@Override
		public void onFailed() {
			endTimer();
			DialogUtils.closeLoadingDialog();
			mTextFileName.setText("");
			mTextPathTitle.setText(mCurrentContainer.getTitle());
			mLayoutContentPage.setVisibility(View.GONE);
			mLayoutNoFiles.setVisibility(View.VISIBLE);
		}
		
	}
	
	/**
	 * 
	 * @author GaoFei
	 * 更新音频,视频，图片的文件预览图监听器
	 */
	class RefreshPreviewReceiver extends BroadcastReceiver{
	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	if(intent.getAction().equals(ConstData.BroadCastMsg.DEVICE_UP)){
	    		//重设ID
	    		//String deviceID = intent.getStringExtra(ConstData.DeviceMountMsg.DEVICE_ID);
	    		String mountPath = intent.getStringExtra(ConstData.DeviceMountMsg.MOUNT_PATH);
	    		if(mCurrDevice.getLocalMountPath().equals(mountPath)){
	    			mCurrDevice.setDeviceID(ConstData.devicePathIDs.get(mountPath));
	    		}
	    		//if(deviceID != null)
	    		//	mCurrDevice.setDeviceID(deviceID);
	    	}else{
	    		//更新预览图
	            FileInfo fileInfo = (FileInfo)intent.getSerializableExtra(ConstData.IntentKey.EXTRA_FILE_INFO);
	            if(mCurrentFileInfo != null && fileInfo != null && fileInfo.getPath().equals(mCurrentFileInfo.getPath()))
	                refreshPreview(fileInfo);
	    	}
	    	
	    }
	}
	
}
