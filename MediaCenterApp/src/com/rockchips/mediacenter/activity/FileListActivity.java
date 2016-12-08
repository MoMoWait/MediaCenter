package com.rockchips.mediacenter.activity;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.xutils.x;
import org.xutils.view.annotation.ViewInject;
import momo.cn.edu.fjnu.androidutils.utils.BitmapUtils;
import momo.cn.edu.fjnu.androidutils.utils.SizeUtils;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.rockchips.mediacenter.R;
import com.rockchips.mediacenter.basicutils.bean.LocalDeviceInfo;
import com.rockchips.mediacenter.basicutils.bean.LocalMediaInfo;
import com.rockchips.mediacenter.basicutils.util.DateUtil;
import com.rockchips.mediacenter.basicutils.util.DiskUtil;
import com.rockchips.mediacenter.basicutils.util.FileUtil;
import com.rockchips.mediacenter.basicutils.util.IICLOG;
import com.rockchips.mediacenter.basicutils.util.ResLoadUtil;
import com.rockchips.mediacenter.basicutils.util.StringUtils;
import com.rockchips.mediacenter.widget.ThumbnailManager;
import com.rockchips.mediacenter.adapter.FileListAdapter;
import com.rockchips.mediacenter.adapter.FolderListAdapter;
import com.rockchips.mediacenter.adapter.UpnpFolderListAdapter;
import com.rockchips.mediacenter.audioplayer.InternalAudioPlayer;
import com.rockchips.mediacenter.audioplayer.SongInfo;
import com.rockchips.mediacenter.bean.LocalDevice;
import com.rockchips.mediacenter.bean.LocalMediaFile;
import com.rockchips.mediacenter.bean.LocalMediaFolder;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.data.ConstData.EBrowerType;
import com.rockchips.mediacenter.data.ConstData.MCSMessage;
import com.rockchips.mediacenter.imageplayer.InternalImagePlayer;
import com.rockchips.mediacenter.imageplayer.image.ImageUtils;
import com.rockchips.mediacenter.listadapter.DiskShowAdapter;
import com.rockchips.mediacenter.modle.db.LocalMediaFileService;
import com.rockchips.mediacenter.modle.task.FileLoadTask;
import com.rockchips.mediacenter.modle.task.FileMediaDataLoadTask;
import com.rockchips.mediacenter.modle.task.FolderLoadTask;
import com.rockchips.mediacenter.retrieve.RetrieveCompleteListener;
import com.rockchips.mediacenter.retrieve.RetrieveInfoManager;
import com.rockchips.mediacenter.util.DialogUtils;
import com.rockchips.mediacenter.util.MediaFileUtils;
import com.rockchips.mediacenter.util.SystemUiHider;
import com.rockchips.mediacenter.utils.GetDateUtil;
import com.rockchips.mediacenter.videoplayer.InternalVideoPlayer;
import com.rockchips.mediacenter.view.LoadingDialog;
import com.rockchips.mediacenter.viewutils.animgridview.AnimGridView;
import com.rockchips.mediacenter.viewutils.animgridview.HWListViewBaseAdapter;
import com.rockchips.mediacenter.viewutils.animgridview.OnItemStateChangedListener;
import com.rockchips.mediacenter.viewutils.preview.PreviewWidget;
import com.rockchips.mediacenter.viewutils.tipdialog.TipDialog;
import com.rockchips.mediacenter.viewutils.tipdialog.TipDialog.OnTipDialogClickListener;

/**
 * An example full-screen activity that shows and hides the system UI (i.e. status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class FileListActivity extends AppBaseActivity implements OnItemSelectedListener, OnItemClickListener{
	public static final String TAG = "FileListActivity";
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
	private int mCurrMediaType;
	private LocalDevice mCurrDevice; 
	private FolderListAdapter mFolderAdapter;
	private FileListAdapter mFileAdapter;
	private FolderLoadTask mFolderLoadTask;
	private FileLoadTask mFileLoadTask;
	private LocalMediaFolder mSelectFolder;
	private LocalMediaFile mSelectFile;
	/**
	 * 当前焦点文件
	 */
	private LocalMediaFile mCurrentFocusFile;
	/**
	 * 当前文件夹列表选中的位置
	 */
	private int mFolderSelection = 0;
	/**
	 * 当前文件列表选中位置
	 */
	private int mFileSelection = 0;
	/**
	 * 当前焦点位置
	 */
	private int mCurrentFocusPosition;
	/**
	 * 媒体文件元数据获取器
	 */
	private FileMediaDataLoadTask mMediaDataLoadTask;
	
	private Bitmap mOldBitmap;
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_list);
        x.view().inject(this);
        initDataAndView();
        initEvent();
    }
    
    
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Log.i(TAG, "onItemClick");
		Object itemObject = parent.getAdapter().getItem(position);
		if(itemObject instanceof LocalMediaFolder){
			Log.i(TAG, "click folder");
			LocalMediaFolder itemFolder = (LocalMediaFolder)itemObject;
			mSelectFolder = itemFolder;
			loadFiles(itemFolder, false);
		}else{
			mFileSelection = position;
			LocalMediaFile itemFile = (LocalMediaFile)itemObject;
			mSelectFile = itemFile;
			loadActivity(itemFile);
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		Object itemObject = parent.getAdapter().getItem(position);
		if(itemObject instanceof LocalMediaFile){
			mCurrentFocusPosition = position;
			mCurrentFocusFile = (LocalMediaFile)itemObject;
		}
		refreshPreview(position);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			if(mListFile.getAdapter() instanceof FileListAdapter){
				if(mMediaDataLoadTask != null && mMediaDataLoadTask.getStatus() == Status.RUNNING)
					mMediaDataLoadTask.cancel(true);
				loadFolders();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	
	@Override
	protected void onDestroy() {
		//hadle media data load task
		if(mMediaDataLoadTask != null && mMediaDataLoadTask.getStatus() == Status.RUNNING)
			mMediaDataLoadTask.cancel(true);
		super.onDestroy();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		loadFiles(mSelectFolder, true);
	}
	
    public void initDataAndView(){
    	mPregressLoading.setVisibility(View.GONE);
    	mCurrMediaType = getIntent().getIntExtra(ConstData.IntentKey.EXTRAL_MEDIA_TYPE, -1);
    	mCurrDevice = (LocalDevice)getIntent().getSerializableExtra(ConstData.IntentKey.EXTRAL_LOCAL_DEVICE);
    	loadFolders();
    }

    public void initEvent(){
    	mListFile.setOnItemClickListener(this);
    	mListFile.setOnItemSelectedListener(this);
    }
    
    private void refreshPreview(int position)
    {
    	if(isShowFolder()){
    		LocalMediaFolder mediaFolder = (LocalMediaFolder)mListFile.getAdapter().getItem(position);
    	    mWidgetPreview.updateName(mediaFolder.getName());
            mWidgetPreview.updateImage(getPreviewIcon(ConstData.MediaType.FOLDER));
            mWidgetPreview.updateOtherText(getPreviewInfo(mediaFolder));
    	}else{
    		LocalMediaFile mediaFile = (LocalMediaFile)mListFile.getAdapter().getItem(position);
    		mWidgetPreview.updateName(mediaFile.getName());
    		Bitmap previewBitmap = null;
    		if(mediaFile.getType() == ConstData.MediaType.IMAGE)
    			previewBitmap = BitmapUtils.getScaledBitmapFromFile(mediaFile.getPath(), SizeUtils.dp2px(this, 280), SizeUtils.dp2px(this, 280));
    		else if(!TextUtils.isEmpty(mediaFile.getPreviewPhotoPath())){
    			previewBitmap = BitmapUtils.getScaledBitmapFromFile(mediaFile.getPreviewPhotoPath(),  SizeUtils.dp2px(this, 280), SizeUtils.dp2px(this, 280));
    		}
            if(previewBitmap != null)
            	mWidgetPreview.updateImage(previewBitmap);
            else
            	mWidgetPreview.updateImage(getPreviewIcon(mediaFile.getType()));
            int mediaType = mediaFile.getType();
            switch (mediaType)
            {
                case ConstData.MediaType.AUDIO:
                case ConstData.MediaType.VIDEO:
                	updateOtherText(position);
                	if(!mediaFile.isLoadPreviewPhoto()){
                		loadExtraMediaInfo(mediaFile);
                	}
                    //asyncGetAVInfo(selectMediaInfo);
                    break;
                case ConstData.MediaType.IMAGE:
                	updateOtherText(position);
                  /*  if (StringUtils.isNotEmpty(selectMediaInfo.getmResoulution()))
                    {
                        Log.d(TAG, " mResoulution 已经存在，无需再次获取......");
                        updateOtherInfo(selectMediaType);
                    }
                    else
                    {
                        asyncGetImagResoulution(selectMediaInfo);
                    }*/
                    /*mUIHandler.removeMessages(MSG_UI_REQUEST_THUMBNAIL);
                    mUIHandler.sendMessageDelayed(Message.obtain(mUIHandler, MSG_UI_REQUEST_THUMBNAIL,
                                                             null), REQUEST_THUMBNAIL_DELAY);*/
                    break;
            }  
            
    	}
       

    }
    
    /**
     * 是否显示文件夹
     * @return
     */
    public boolean isShowFolder(){
    	return (mListFile.getAdapter() instanceof FolderListAdapter);
    }
    
	/**
	 * 加载文件夹列表
	 */
	public void loadFolders(){
		DialogUtils.showLoadingDialog(this, false);
    	mFolderLoadTask = new FolderLoadTask(new FolderLoadTask.Callback() {
			@Override
			public void onSuccess(List<LocalMediaFolder> mediaFiles) {
				DialogUtils.closeLoadingDialog();
				mTextPathTitle.setText(mCurrDevice.getPhysic_dev_id());
				Log.i(TAG, "onSuccess->mediaFiles:" + mediaFiles);
				if(mediaFiles != null && mediaFiles.size() > 0){
					mLayoutContentPage.setVisibility(View.VISIBLE);
					mLayoutNoFiles.setVisibility(View.GONE);
					mListFile.requestFocus();
					mFolderAdapter = new FolderListAdapter(FileListActivity.this, R.layout.adapter_file_list_item, mediaFiles);
					mListFile.setAdapter(mFolderAdapter);
					if(mSelectFolder != null){
						int lastSelctIndex = getFolderIndex(mSelectFolder, mediaFiles);
						if(lastSelctIndex >= 0){
							mListFile.setSelection(lastSelctIndex);
						}else{
							mListFile.setSelection(0);
						}
					}else{
						mListFile.setSelection(0);
					}
				}else{
					mLayoutContentPage.setVisibility(View.GONE);
					mLayoutNoFiles.setVisibility(View.VISIBLE);
				}
			}
			
			@Override
			public void onFailed() {
				DialogUtils.closeLoadingDialog();
			}
		});
    	
    	mFolderLoadTask.execute("" + mCurrMediaType, mCurrDevice.getDeviceID());
	}
	
	/**
	 * 加载文件列表
	 * @param mediaFolder 父目录  
	 * @param isBack      是否从其他Activity返回
	 */
	public void loadFiles(final LocalMediaFolder mediaFolder, final boolean isBack){
		DialogUtils.showLoadingDialog(this, false);
    	mFileLoadTask = new FileLoadTask(new FileLoadTask.Callback() {
			@Override
			public void onSuccess(List<LocalMediaFile> mediaFiles) {
				DialogUtils.closeLoadingDialog();
				mTextPathTitle.setText(mCurrDevice.getPhysic_dev_id() + ">" + mediaFolder.getName());
				Log.i(TAG, "loadFiles->onSuccess->mediaFiles:" + mediaFiles);
				if(mediaFiles != null && mediaFiles.size() > 0){
					mLayoutContentPage.setVisibility(View.VISIBLE);
					mLayoutNoFiles.setVisibility(View.GONE);
					mFileAdapter = new FileListAdapter(FileListActivity.this, R.layout.adapter_file_list_item, mediaFiles);
					mListFile.setAdapter(mFileAdapter);
					//从其他Activity返回
					if(isBack && mSelectFile != null){
						int lastSelectIndex = getFileIndex(mSelectFile, mediaFiles);
						if(lastSelectIndex >= 0)
							mListFile.setSelection(lastSelectIndex);
						else
							mListFile.setSelection(0);
					}else{
						mListFile.setSelection(0);
					}
				}else{
					mLayoutContentPage.setVisibility(View.GONE);
					mLayoutNoFiles.setVisibility(View.VISIBLE);
				}
			}
			
			@Override
			public void onFailed() {
				DialogUtils.closeLoadingDialog();
			}
		});
    	
    	if(mCurrMediaType == ConstData.MediaType.FOLDER){
    		mFileLoadTask.execute(mediaFolder.getPath(), "-1");
    	}else if(mCurrMediaType == ConstData.MediaType.AUDIOFOLDER){
    		mFileLoadTask.execute(mediaFolder.getPath(), "" + ConstData.MediaType.AUDIO);
    	}else if(mCurrMediaType == ConstData.MediaType.IMAGEFOLDER){
    		mFileLoadTask.execute(mediaFolder.getPath(), "" + ConstData.MediaType.IMAGE);
    	}else if(mCurrMediaType == ConstData.MediaType.VIDEOFOLDER){
    		mFileLoadTask.execute(mediaFolder.getPath(), "" + ConstData.MediaType.VIDEO);
    	}
	}
	
	
    protected String getPreviewInfo(LocalMediaFolder mediaFolder)
    {
        String info = getFolderPreviewInfo(mediaFolder);;
        return info;
    }
	
    
    private String getAudioPreviewInfo(LocalMediaInfo mediaInfo){
        return getMediaPreviewInfo(mediaInfo);
    }

    private String getImagePreviewInfo(LocalMediaInfo mediaInfo){
        return getMediaPreviewInfo(mediaInfo);
    }

    
    private String getFolderPreviewInfo(LocalMediaFolder mediaFolder){
        String info = getString(R.string.file_tip) + mediaFolder.getFileCount();
        return info;
    }

    private String getVideoPreviewInfo(LocalMediaInfo mediaInfo){
        return getMediaPreviewInfo(mediaInfo);
    }
    
    private String getMediaPreviewInfo(LocalMediaInfo mediaInfo){
        String Date = null;
        Date = GetDateUtil.getTime(this, mediaInfo.getmModifyDate());
        if (Date == null){
            Date = getString(R.string.unknown);
        }
        String info = getString(R.string.file_size_tip) + getFileSize(mediaInfo.getmFileSize()) + "\n" + getString(R.string.modify_time_tip) + Date;
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
        int resId;
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
            default:
                resId = R.drawable.icon_preview_folder;
                break;
        }
        return getBitmapById(resId);
    }

    private Bitmap getBitmapById(int id){
    	Bitmap bitmap = BitmapFactory.decodeResource(getResources(), id);
    	if(mOldBitmap != null && !mOldBitmap.isRecycled())
    		mOldBitmap.recycle();
    	mOldBitmap = bitmap;
    	return bitmap;
    }
    
    
    private void updateOtherText(int position)
    {
        LocalMediaFile mediaFile = (LocalMediaFile)mListFile.getAdapter().getItem(position);
        int mediaType = mediaFile.getType();
        String strInfo = null;
        switch(mediaType)
        {
            case ConstData.MediaType.AUDIO:
            	strInfo = String.format(getString(R.string.audio_preview_info), 
                		getFileSize(mediaFile.getSize()), 
                		getFileType(mediaFile.getName(),getString(R.string.music),mediaFile.getDevicetype()), 
                		getRunningTime(mediaFile),
                		formatCreateDate(mediaFile),getDescription(""));
            	break;
            case ConstData.MediaType.VIDEO:
            	strInfo = String.format(getString(R.string.video_preview_info),
                        getFileSize(mediaFile.getSize()), 
                        getFileType(mediaFile.getName(),getString(R.string.video),mediaFile.getDevicetype()), 
                        getRunningTime(mediaFile), 
                        formatCreateDate(mediaFile),getDescription(""));
              break;
            // 显示尺寸
            case ConstData.MediaType.IMAGE:
                strInfo = String.format(getString(R.string.image_preview_info), getFileSize(mediaFile.getSize()),
                        getFileType(mediaFile.getName(),getString(R.string.picture),mediaFile.getDevicetype()), formatCreateDate(mediaFile),getDescription(""));
                break;
        }
        mWidgetPreview.updateOtherText(strInfo);
    }
    
    /**
     * 加载播放器
     */
    public void loadActivity(LocalMediaFile mediaFile){
        Intent intent = new Intent();
        intent.putExtra(ConstData.IntentKey.IS_INTERNAL_PLAYER, true);
        intent.putExtra(ConstData.IntentKey.EXTRAL_LOCAL_DEVICE, mCurrDevice);
        intent.putExtra(LocalDeviceInfo.DEVICE_EXTRA_NAME, MediaFileUtils.getDeviceInfoFromDevice(mCurrDevice).compress());
        LocalMediaFileService localMediaFileService = new LocalMediaFileService();
        List<LocalMediaFile> mediaFiles = localMediaFileService.getFilesByParentPath(mediaFile.getParentPath(), mediaFile.getType());
        List<LocalMediaInfo> mediaInfos = MediaFileUtils.getMediaInfoList(mediaFiles);
        List<Bundle> mediaInfoList = new ArrayList<Bundle>();
        for(LocalMediaInfo itemInfo : mediaInfos){
        	mediaInfoList.add(itemInfo.compress());
        }
        int newPosition = 0;
        for(int i = 0; i != mediaFiles.size(); ++i){
        	if(mediaFiles.get(i).getFileId() == mediaFile.getFileId()){
        		newPosition = i;
        		break;
        	}
        }
        if (mediaFile.getType() == ConstData.MediaType.AUDIO)
        {
            intent.setClass(this, InternalAudioPlayer.class);
           
            //int newPosition = getMediaBundleList(ConstData.MediaType.AUDIO, mediaInfoList, position);
            intent.putExtra(ConstData.IntentKey.CURRENT_INDEX, newPosition);
//            intent.putParcelableArrayListExtra(Constant.IntentKey.MEDIA_INFO_LIST, mediaInfoList);  
            InternalAudioPlayer.setMediaList(mediaInfoList, newPosition);
        }
        else if (mediaFile.getType() == ConstData.MediaType.VIDEO)
        {
//            String dispName = mSelectDisk.getPhysicId();
            intent.setClass(this, InternalVideoPlayer.class);
            //int newPosition = getMediaBundleList(ConstData.MediaType.VIDEO, mediaInfoList, position);
            intent.putExtra(ConstData.IntentKey.CURRENT_INDEX, newPosition);
//            intent.putParcelableArrayListExtra(Constant.IntentKey.MEDIA_INFO_LIST, mediaInfoList);  
//            InternalVideoPlayer.setMediaList(mediaInfoList, newPosition, dispName);
            InternalVideoPlayer.setMediaList(mediaInfoList, newPosition);
        }
        else if (mediaFile.getType() == ConstData.MediaType.IMAGE)
        {
            intent.setClass(this, InternalImagePlayer.class);
            intent.putExtra(ConstData.IntentKey.IS_INTERNAL_PLAYER, true);
            //int newPosition = getMediaBundleList(ConstData.MediaType.IMAGE, mediaInfoList, position);
            intent.putExtra(ConstData.IntentKey.CURRENT_INDEX, newPosition);
//            intent.putParcelableArrayListExtra(Constant.IntentKey.MEDIA_INFO_LIST, mediaInfoList);
            InternalImagePlayer.setMediaList(mediaInfoList, newPosition);
        }
        Log.i(TAG, "start internal player");
        startActivityForResult(intent, START_PLAYER_REQUEST_CODE);
    }
    
    private static final int INDEX_OF_SPLIT_01 = -1;
    private static final int INDEX_OF_SPLIT_1 = 1;
    /** DTS2015012807455 解决音乐、视频，不显示时长的问题  by zWX238093 */
    protected String getRunningTime(LocalMediaFile mediaFile)
    {
    	if(TextUtils.isEmpty(mediaFile.getDuration()))
    	{
    		return getString(R.string.unknown_durnation);
    	}
    	return mediaFile.getDuration();
    }
    
    private String getDescription(String description){	
		if (TextUtils.isEmpty(description))
		{		
			description = getString(R.string.unknown);
		}		
		return description;
	}
    
    private String formatCreateDate(LocalMediaFile mediaFile){
        String dataStr;
        dataStr = GetDateUtil.getTime(FileListActivity.this, mediaFile.getLast_modify_date());
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
		if (deviceType == ConstData.DeviceType.DEVICE_TYPE_U || deviceType == ConstData.DeviceType.DEVICE_TYPE_SD)
		{					
			fileType = filename.substring(filename.lastIndexOf(".")+1);
		}		
		return fileType;
	}
    
   /**
    * 获取文件夹索引
    * @param mediaFolder
    * @param mediaFiles
    * @return
    */
    private int getFolderIndex(LocalMediaFolder mediaFolder, List<LocalMediaFolder> mediaFiles){
    	if(mediaFolder == null)
    		return -1;
    	if(mediaFiles == null || mediaFiles.size() == 0)
    		return -1;
    	for(int i = 0; i != mediaFiles.size(); ++i){
    		if(mediaFiles.get(i).getFolderId() == mediaFolder.getFolderId()){
    			return i;
    		}
    	}
    	return -1;
    }
    
    /**
     * 获取文件索引
     * @param mediaFile
     * @param mediaFiles
     * @return
     */
    private int getFileIndex(LocalMediaFile mediaFile, List<LocalMediaFile> mediaFiles){
    	if(mediaFile == null)
    		return -1;
    	if(mediaFiles == null || mediaFiles.size() == 0)
    		return -1;
    	for(int i = 0; i != mediaFiles.size(); ++i){
    		if(mediaFiles.get(i).getFileId() == mediaFile.getFileId()){
    			return i;
    		}
    	}
    	return -1;
    }
    
    /**
     * 加载额外的媒体信息
     * @param mediaFile
     */
    private void loadExtraMediaInfo(LocalMediaFile mediaFile){
    	if(mMediaDataLoadTask != null && mMediaDataLoadTask.getStatus() == Status.RUNNING)
    		mMediaDataLoadTask.cancel(true);
    	mMediaDataLoadTask = new FileMediaDataLoadTask(new FileMediaDataLoadTask.CallBack(){
    		@Override
    		public void onFinish(LocalMediaFile mediaFile) {
    			Log.i(TAG, "loadExtraMediaInfo->onFinish->mediaFile:" + mediaFile);
    			if(mediaFile == mCurrentFocusFile){
    				Log.i(TAG, "loadExtraMediaInfo->mediaFile==mSelectFile");
    				//更新当前预览图，时长
    				refreshPreview(mCurrentFocusPosition);
    			}
    		};
    	});
    	
    	mMediaDataLoadTask.execute(mediaFile);
    }
}

