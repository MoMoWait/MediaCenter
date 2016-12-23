package com.rockchips.mediacenter.activity;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import momo.cn.edu.fjnu.androidutils.utils.BitmapUtils;
import momo.cn.edu.fjnu.androidutils.utils.SizeUtils;

import org.eclipse.jetty.util.UrlEncoded;
import org.xutils.ImageManager;
import org.xutils.x;
import org.xutils.common.Callback;
import org.xutils.common.Callback.CancelledException;
import org.xutils.common.util.DensityUtil;
import org.xutils.image.ImageOptions;
import org.xutils.view.annotation.ViewInject;

import android.R.integer;
import android.R.string;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.AsyncTask.Status;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

import com.rockchips.mediacenter.R;
import com.rockchips.mediacenter.basicutils.bean.*;
import com.rockchips.mediacenter.basicutils.util.*;
import com.rockchips.mediacenter.adapter.FileListAdapter;
import com.rockchips.mediacenter.adapter.FolderListAdapter;
import com.rockchips.mediacenter.adapter.UpnpFileListAdapter;
import com.rockchips.mediacenter.adapter.UpnpFolderListAdapter;
import com.rockchips.mediacenter.audioplayer.InternalAudioPlayer;
import com.rockchips.mediacenter.bean.LocalDevice;
import com.rockchips.mediacenter.bean.LocalMediaFile;
import com.rockchips.mediacenter.bean.LocalMediaFolder;
import com.rockchips.mediacenter.bean.UpnpFile;
import com.rockchips.mediacenter.bean.UpnpFolder;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.imageplayer.InternalImagePlayer;
import com.rockchips.mediacenter.modle.db.LocalMediaFileService;
import com.rockchips.mediacenter.modle.db.UpnpFileService;
import com.rockchips.mediacenter.modle.task.FileLoadTask;
import com.rockchips.mediacenter.modle.task.FolderLoadTask;
import com.rockchips.mediacenter.modle.task.UpnpFileLoadTask;
import com.rockchips.mediacenter.modle.task.UpnpFileMediaDataLoadTask;
import com.rockchips.mediacenter.modle.task.UpnpFolderLoadTask;
import com.rockchips.mediacenter.util.DialogUtils;
import com.rockchips.mediacenter.util.MediaFileUtils;
import com.rockchips.mediacenter.utils.GetDateUtil;
import com.rockchips.mediacenter.videoplayer.InternalVideoPlayer;
import com.rockchips.mediacenter.viewutils.preview.PreviewWidget;
/**
 * @author GaoFei
 * Upnp文件列表浏览页面
 */
public class UpnpFileListActivity extends AppBaseActivity  implements OnItemSelectedListener, OnItemClickListener{

	public static final String TAG = "UpnpFileListActivity";
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
	private int mCurrMediaType;
	private LocalDevice mCurrDevice; 
	private UpnpFolderListAdapter mFolderAdapter;
	private UpnpFileListAdapter mFileAdapter;
	private UpnpFolderLoadTask mFolderLoadTask;
	private UpnpFileLoadTask mFileLoadTask;
	private UpnpFolder mSelectFolder;
	private UpnpFile mSelectFile;
	private UpnpFile mCurrentFocusFile;
	private int mFocusPosition;
	/**
	 * 当前文件夹列表选中的位置
	 */
	private int mFolderSelection = 0;
	/**
	 * 当前文件列表选中位置
	 */
	private int mFileSelection = 0;
	private ImageOptions mImageOptions;
	private UpnpFileMediaDataLoadTask mUpnpFileMediaDataLoadTask;
	
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
		//Log.i(TAG, "onItemClick");
		Object itemObject = parent.getAdapter().getItem(position);
		if(itemObject instanceof UpnpFolder){
			//Log.i(TAG, "click folder");
			UpnpFolder itemFolder = (UpnpFolder)itemObject;
			mSelectFolder = itemFolder;
			loadFiles(itemFolder, false);
		}else{
			UpnpFile itemFile = (UpnpFile)itemObject;
			mSelectFile = itemFile;
			loadActivity(itemFile);
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		mFocusPosition = position;
		Object itemObject = parent.getAdapter().getItem(position);
		if(itemObject instanceof UpnpFile){
			UpnpFile itemFile = (UpnpFile)itemObject;
			mCurrentFocusFile = itemFile;
		}
		refreshPreview(position);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			if(mListFile.getAdapter() instanceof UpnpFileListAdapter){
				if(mUpnpFileMediaDataLoadTask != null && mUpnpFileMediaDataLoadTask.getStatus() == Status.RUNNING)
					mUpnpFileMediaDataLoadTask.cancel(true);
				loadFolders();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onDestroy() {
		if(mUpnpFileMediaDataLoadTask != null && mUpnpFileMediaDataLoadTask.getStatus() == Status.RUNNING)
			mUpnpFileMediaDataLoadTask.cancel(true);
		super.onDestroy();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//loadFiles(mSelectFolder, true);
		loadFiles(mSelectFolder, true);
	}
	
    public void initDataAndView(){
    	mPregressLoading.setVisibility(View.GONE);
    	mCurrMediaType = getIntent().getIntExtra(ConstData.IntentKey.EXTRAL_MEDIA_TYPE, -1);
    	mCurrDevice = (LocalDevice)getIntent().getSerializableExtra(ConstData.IntentKey.EXTRAL_LOCAL_DEVICE);
    	mImageOptions = new ImageOptions.Builder()
        .setSize(DensityUtil.dip2px(100), DensityUtil.dip2px(100))
        .setRadius(DensityUtil.dip2px(5))
        // 如果ImageView的大小不是定义为wrap_content, 不要crop.
        .setCrop(true) // 很多时候设置了合适的scaleType也不需要它.
        // 加载中或错误图片的ScaleType
        .setImageScaleType(ImageView.ScaleType.FIT_XY)
        .setLoadingDrawableId(R.drawable.image_browser_default)
        .setFailureDrawableId(R.drawable.image_browser_default)
        .build();
    	loadFolders();
    }

    public void initEvent(){
    	mListFile.setOnItemClickListener(this);
    	mListFile.setOnItemSelectedListener(this);
    }
    
    private void refreshPreview(int position)
    {
    	if(isShowFolder()){
    		UpnpFolder mediaFolder = (UpnpFolder)mListFile.getAdapter().getItem(position);
    	    mWidgetPreview.updateName(mediaFolder.getName());
            mWidgetPreview.updateImage(getPreviewIcon(ConstData.MediaType.FOLDER));
            mWidgetPreview.updateOtherText(getPreviewInfo(mediaFolder));
            mTextFileName.setText(mediaFolder.getName());
    	}else{
    		final UpnpFile mediaFile = (UpnpFile)mListFile.getAdapter().getItem(position);
    		mWidgetPreview.updateName(mediaFile.getName());
    		mWidgetPreview.updateImage(getPreviewIcon(mediaFile.getType()));
    		mTextFileName.setText(mediaFile.getName());
    		String previewPhotoPath = mediaFile.getPreviewPhotoPath();
    		Bitmap preViewBitmap = null;
    		if(!TextUtils.isEmpty(previewPhotoPath)){
    			//加载至页面
    			preViewBitmap = BitmapUtils.getScaledBitmapFromFile(previewPhotoPath, SizeUtils.dp2px(this, 280), SizeUtils.dp2px(this, 280));
    			if(preViewBitmap != null)
        			mWidgetPreview.updateImage(preViewBitmap);
    		}else{
    			loadExtraMediaInfo(mediaFile);
    		}
            int mediaType = mediaFile.getType();
            switch (mediaType)
            {
                case ConstData.MediaType.AUDIO:
                case ConstData.MediaType.VIDEO:
                	updateOtherText(position);
                    break;
                case ConstData.MediaType.IMAGE:
                	updateOtherText(position);
                    break;
            }  
            
    	}
       

    }
    
    /**
     * 是否显示文件夹
     * @return
     */
    public boolean isShowFolder(){
    	return (mListFile.getAdapter() instanceof UpnpFolderListAdapter);
    }
    
	/**
	 * 加载文件夹列表
	 */
	public void loadFolders(){
		DialogUtils.showLoadingDialog(this, false);
    	mFolderLoadTask = new UpnpFolderLoadTask(new UpnpFolderLoadTask.Callback() {
			@Override
			public void onSuccess(List<UpnpFolder> mediaFolders) {
				DialogUtils.closeLoadingDialog();
				mTextPathTitle.setText(mCurrDevice.getPhysic_dev_id());
				if(mediaFolders != null && mediaFolders.size() > 0){
					mLayoutContentPage.setVisibility(View.VISIBLE);
					mLayoutNoFiles.setVisibility(View.GONE);
					mListFile.requestFocus();
					mFolderAdapter = new UpnpFolderListAdapter(UpnpFileListActivity.this, R.layout.adapter_file_list_item, mediaFolders);
					mListFile.setAdapter(mFolderAdapter);
					if(mSelectFolder != null){
						int lastSelctIndex = getFolderIndex(mSelectFolder, mediaFolders);
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
    	
    	mFolderLoadTask.execute(mCurrDevice.getDeviceID(), "" + mCurrMediaType);
	}
	
	/**
	 * 加载文件列表
	 * @param mediaFolder 父目录  
	 * @param isBack      是否从其他Activity返回
	 */
	public void loadFiles(final UpnpFolder mediaFolder, final boolean isBack){
		DialogUtils.showLoadingDialog(this, false);
    	mFileLoadTask = new UpnpFileLoadTask(new UpnpFileLoadTask.Callback() {
			@Override
			public void onSuccess(List<UpnpFile> mediaFiles) {
				DialogUtils.closeLoadingDialog();
				mTextPathTitle.setText(mCurrDevice.getPhysic_dev_id() + ">" + mediaFolder.getName());
				//Log.i(TAG, "loadFiles->onSuccess->mediaFiles:" + mediaFiles);
				if(mediaFiles != null && mediaFiles.size() > 0){
					mLayoutContentPage.setVisibility(View.VISIBLE);
					mLayoutNoFiles.setVisibility(View.GONE);
					mFileAdapter = new UpnpFileListAdapter(UpnpFileListActivity.this, R.layout.adapter_file_list_item, mediaFiles);
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
    		mFileLoadTask.execute(mCurrDevice.getDeviceID(), mediaFolder.getItmeId(), "-1");
    	}else if(mCurrMediaType == ConstData.MediaType.AUDIOFOLDER){
    		mFileLoadTask.execute(mCurrDevice.getDeviceID(), mediaFolder.getItmeId(), "" + ConstData.MediaType.AUDIO);
    	}else if(mCurrMediaType == ConstData.MediaType.IMAGEFOLDER){
    		mFileLoadTask.execute(mCurrDevice.getDeviceID(), mediaFolder.getItmeId(), "" + ConstData.MediaType.IMAGE);
    	}else if(mCurrMediaType == ConstData.MediaType.VIDEOFOLDER){
    		mFileLoadTask.execute(mCurrDevice.getDeviceID(), mediaFolder.getItmeId(), "" + ConstData.MediaType.VIDEO);
    	}
	}
	
	
    protected String getPreviewInfo(UpnpFolder mediaFolder)
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

    
    private String getFolderPreviewInfo(UpnpFolder mediaFolder){
        String info = getString(R.string.file_tip);
        if(mCurrMediaType == ConstData.MediaType.FOLDER){
        	info += mediaFolder.getFileCount();
        }else if(mCurrMediaType == ConstData.MediaType.AUDIOFOLDER){
        	info += mediaFolder.getAudioCount();
        }else if(mCurrMediaType == ConstData.MediaType.VIDEOFOLDER){
        	info += mediaFolder.getVideoCount();
        }else{
        	info += mediaFolder.getImageCount();
        }
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
        InputStream is = getResources().openRawResource(id);
        return BitmapFactory.decodeStream(is);
    }
    
    
    private void updateOtherText(int position)
    {
        UpnpFile mediaFile = (UpnpFile)mListFile.getAdapter().getItem(position);
        int mediaType = mediaFile.getType();
        String strInfo = null;
        switch(mediaType)
        {
            case ConstData.MediaType.AUDIO:
            	strInfo = String.format(getString(R.string.audio_preview_info), 
                		getFileSize(mediaFile.getSize()), 
                		getFileType(mediaFile.getName(),getString(R.string.music),mediaFile.getDevicetype()), 
                		getRunningTime(mediaFile.getDuration()),
                		formatCreateDate(mediaFile),getDescription(""));
            	break;
            case ConstData.MediaType.VIDEO:
            	strInfo = String.format(getString(R.string.video_preview_info),
                        getFileSize(mediaFile.getSize()), 
                        getFileType(mediaFile.getName(),getString(R.string.video),mediaFile.getDevicetype()), 
                        getRunningTime(mediaFile.getDuration()), 
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
    public void loadActivity(UpnpFile mediaFile){
        Intent intent = new Intent();
        intent.putExtra(ConstData.IntentKey.IS_INTERNAL_PLAYER, true);
        intent.putExtra(ConstData.IntentKey.EXTRAL_LOCAL_DEVICE, mCurrDevice);
        intent.putExtra(LocalDeviceInfo.DEVICE_EXTRA_NAME, MediaFileUtils.getDeviceInfoFromDevice(mCurrDevice).compress());
        UpnpFileService upnpFileService = new UpnpFileService();
        List<UpnpFile> mediaFiles = upnpFileService.getFilesByDeviceIdAndParentId(mediaFile.getDeviceID(), mediaFile.getParentId(), mediaFile.getType());
        List<LocalMediaInfo> mediaInfos = MediaFileUtils.getMediaInfoListFromUpnpFileList(mediaFiles);
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
            intent.putExtra(ConstData.IntentKey.CURRENT_INDEX, newPosition);
            InternalAudioPlayer.setMediaList(mediaInfoList, newPosition);
        }
        else if (mediaFile.getType() == ConstData.MediaType.VIDEO)
        {
            intent.setClass(this, InternalVideoPlayer.class);
            intent.putExtra(ConstData.IntentKey.CURRENT_INDEX, newPosition);
            InternalVideoPlayer.setMediaList(mediaInfoList, newPosition);
        }
        else if (mediaFile.getType() == ConstData.MediaType.IMAGE)
        {
            intent.setClass(this, InternalImagePlayer.class);
            intent.putExtra(ConstData.IntentKey.IS_INTERNAL_PLAYER, true);
            intent.putExtra(ConstData.IntentKey.CURRENT_INDEX, newPosition);
            InternalImagePlayer.setMediaList(mediaInfoList, newPosition);
        }
        //Log.i(TAG, "start internal player");
        startActivityForResult(intent, START_PLAYER_REQUEST_CODE);
    }
    
    private static final int INDEX_OF_SPLIT_01 = -1;
    private static final int INDEX_OF_SPLIT_1 = 1;
    /** DTS2015012807455 解决音乐、视频，不显示时长的问题  by zWX238093 */
    protected String getRunningTime(String mDuration)
    {
    	Log.d(TAG, "mDuration: "+ mDuration);
    	if(TextUtils.isEmpty(mDuration))
    	{
    		return getString(R.string.unknown_durnation);
    	}
    	else
    	{

    		int index = mDuration.indexOf(".");
    		
    		if(index != INDEX_OF_SPLIT_01 )
    		{
    			String subStr = mDuration.substring(0,index);
    			Log.d(TAG, "indexOf \":\" is" + mDuration.indexOf(":"));
    			if(mDuration.indexOf(":") == INDEX_OF_SPLIT_1)
    			{
    				StringBuffer strBuff = new StringBuffer("0");
    				strBuff.append(subStr);
    				return strBuff.toString();
    			}
    			return subStr;
    		}
    		return 	mDuration;	
    	}
    }
    
    private String getDescription(String description){	
		if (TextUtils.isEmpty(description))
		{		
			description = getString(R.string.unknown);
		}		
		return description;
	}
    
    private String formatCreateDate(UpnpFile mediaFile){
        String dataStr = mediaFile.getDate();
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
    private int getFolderIndex(UpnpFolder mediaFolder, List<UpnpFolder> mediaFolders){
    	if(mediaFolder == null)
    		return -1;
    	if(mediaFolders == null || mediaFolders.size() == 0)
    		return -1;
    	for(int i = 0; i != mediaFolders.size(); ++i){
    		if(mediaFolders.get(i).getFolderId() == mediaFolder.getFolderId()){
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
    private int getFileIndex(UpnpFile mediaFile, List<UpnpFile> mediaFiles){
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
     * @param upnpFile
     */
    private void loadExtraMediaInfo(UpnpFile upnpFile){
    	if(mUpnpFileMediaDataLoadTask != null && mUpnpFileMediaDataLoadTask.getStatus() == Status.RUNNING)
    		mUpnpFileMediaDataLoadTask.cancel(true);
    	mUpnpFileMediaDataLoadTask = new UpnpFileMediaDataLoadTask(new UpnpFileMediaDataLoadTask.CallBack() {
			
			@Override
			public void onFinish(UpnpFile upnpFile) {
				if(mCurrentFocusFile == upnpFile)
					refreshPreview(mFocusPosition);
			}
		});
    	mUpnpFileMediaDataLoadTask.execute(upnpFile);
    }

}
