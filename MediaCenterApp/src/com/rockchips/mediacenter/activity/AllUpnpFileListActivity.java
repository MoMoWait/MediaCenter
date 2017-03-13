package com.rockchips.mediacenter.activity;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import momo.cn.edu.fjnu.androidutils.utils.BitmapUtils;
import momo.cn.edu.fjnu.androidutils.utils.SizeUtils;
import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.support.contentdirectory.callback.Browse;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.DIDLObject.Property;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;
import org.xutils.x;
import org.xutils.common.util.DensityUtil;
import org.xutils.image.ImageOptions;
import org.xutils.view.annotation.ViewInject;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
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

import com.rockchips.mediacenter.adapter.AllUpnpFileListAdapter;
import com.rockchips.mediacenter.adapter.UpnpFileListAdapter;
import com.rockchips.mediacenter.adapter.UpnpFolderListAdapter;
import com.rockchips.mediacenter.audioplayer.InternalAudioPlayer;
import com.rockchips.mediacenter.bean.AllFileInfo;
import com.rockchips.mediacenter.bean.AllUpnpFileInfo;
import com.rockchips.mediacenter.bean.LocalDevice;
import com.rockchips.mediacenter.bean.UpnpFile;
import com.rockchips.mediacenter.bean.UpnpFolder;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.imageplayer.InternalImagePlayer;
import com.rockchips.mediacenter.modle.db.UpnpFileService;
import com.rockchips.mediacenter.modle.task.UpnpFileLoadTask;
import com.rockchips.mediacenter.modle.task.UpnpFileMediaDataLoadTask;
import com.rockchips.mediacenter.modle.task.UpnpFilePreviewLoadTask;
import com.rockchips.mediacenter.modle.task.UpnpFolderLoadTask;
import com.rockchips.mediacenter.service.DeviceMonitorService;
import com.rockchips.mediacenter.utils.DialogUtils;
import com.rockchips.mediacenter.utils.MediaFileUtils;
import com.rockchips.mediacenter.utils.GetDateUtil;
import com.rockchips.mediacenter.videoplayer.InternalVideoPlayer;
import com.rockchips.mediacenter.view.PreviewWidget;
import com.rockchips.mediacenter.R;
import com.rockchips.mediacenter.utils.ResLoadUtil;
import com.rockchips.mediacenter.utils.DiskUtil;
import com.rockchips.mediacenter.bean.LocalDeviceInfo;
import com.rockchips.mediacenter.bean.LocalMediaInfo;


/**
 * @author GaoFei
 * 所有Upnp设备的文件，按目录结构访问
 */
public class AllUpnpFileListActivity extends AppBaseActivity implements OnItemSelectedListener, OnItemClickListener{

	public static final String TAG = "AllUpnpFileListActivity";
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
	private UpnpFilePreviewLoadTask mUpnpFilePreviewLoadTask;
	/**
	 * 当前目录
	 */
	private Container mCurrContainer;
	/**
	 * 上一次目录
	 */
	private Container mLastContainer;
	
	/**
	 * 目录服务
	 */
	private Service mDirectoryService;
	/**
	 * 设备监听服务
	 */
	private DeviceMonitorService mMonitorService;
	/**
	 * 设备服务连接器
	 */
	private ServiceConnection mDeviceMonitorConnection;
	/**
	 * Android Upnp服务
	 */
	private AndroidUpnpService mUpnpService;
	/**
	 * 当前对应的远程设备
	 */
	private RemoteDevice mCurrRemoteDevice;
	/**
	 * 当前文件浏览
	 */
	private FileBrowser mCurrFileBrowser;
	/**
	 * 排序依据
	 */
	private SortCriterion[] mSortCriterions =  {new SortCriterion(true, "dc:title")};
	/**
	 * Upnp文件消息处理
	 */
	private BrowserHandler mBrowserHandler = new BrowserHandler();
	/**
	 * 所有Upnp文件列表适配器
	 */
	private AllUpnpFileListAdapter mUpnpFileListAdapter;
	/**
	 * 当前选中的Upnp文件
	 */
	private AllUpnpFileInfo mCurrFocusFileInfo;
	/**
	 * 文件目录title
	 */
	private LinkedList<Container> mContainerTitles = new LinkedList<Container>();
	/**
	 * Upnp文件浏览消息
	 * @author GaoFei
	 *
	 */
	/**
	 * 当前文件，文件夹内容
	 */
	private DIDLContent mDidlContent;
	interface BrowserMsg{
		/**请求文件*/
		int REQUEST_FILES = 1;
		/**接收文件成功*/
		int RECEIVED_SUCC = 2;
		/**接收文件失败*/
		int RECEIVED_FAILED = 3;
	}
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_list);
        x.view().inject(this);
        initDataAndView();
        initEvent();
        bindServices();
    }
    
    
    
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		//Log.i(TAG, "onItemClick");
		if(mCurrFocusFileInfo.getType() == ConstData.MediaType.FOLDER){
			mLastContainer = null;
			mCurrContainer = (Container)mCurrFocusFileInfo.getFile();
			mContainerTitles.add(mCurrContainer);
			Log.i(TAG, "onItemClick->mCurrContainer:" + mCurrContainer.getId() + ":" + mCurrContainer.getTitle());
			loadFiles();
		}else{
			//启动Activity
			loadActivity(mCurrFocusFileInfo);
		}
		/*mCurrFocusFileInfo.getType();
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
		}*/
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		mFocusPosition = position;
		mCurrFocusFileInfo = (AllUpnpFileInfo)parent.getAdapter().getItem(position);
		/*if(itemObject instanceof UpnpFile){
			UpnpFile itemFile = (UpnpFile)itemObject;
			mCurrentFocusFile = itemFile;
		}*/
		refreshPreview(mCurrFocusFileInfo);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			//String currContainerID = mCurrContainer.getId();
			Log.i(TAG, "onKeyDown->currContainerID:" + mCurrContainer.getId());
			Log.i(TAG, "onKeyDown->parentID:" + mCurrContainer.getParentID());
			if(mContainerTitles.size() > 1){
				mLastContainer = mContainerTitles.removeLast();
				mCurrContainer = mContainerTitles.getLast();
				Log.i(TAG, "onKeyDown->mCurrContainer:" + mCurrContainer.getId() + ":" + mCurrContainer.getTitle());
				loadFiles(); 
				return true;
			}
			/*if(mListFile.getAdapter() instanceof UpnpFileListAdapter){
				if(mUpnpFilePreviewLoadTask != null && mUpnpFilePreviewLoadTask.getStatus() == Status.RUNNING)
					mUpnpFilePreviewLoadTask.cancel(true);
				//loadFolders();
				return true;
			}*/
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onDestroy() {
		if(mUpnpFilePreviewLoadTask != null && mUpnpFilePreviewLoadTask.getStatus() == Status.RUNNING)
			mUpnpFilePreviewLoadTask.cancel(true);
		unbindService(mDeviceMonitorConnection);
		super.onDestroy();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//loadFiles(mSelectFolder, true);
		//loadFiles(mSelectFolder, true);
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
    	mCurrContainer = createRootContainer();
    	mContainerTitles.add(mCurrContainer);
    	mDeviceMonitorConnection = new ServiceConnection() {
			
			@Override
			public void onServiceDisconnected(ComponentName name) {
				
			}
			
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.i(TAG, "mDeviceMonitorConnection->onServiceConnected");
				mMonitorService = ((DeviceMonitorService.MonitorBinder)service).getMonitorService();
				mUpnpService = mMonitorService.getUpnpService();
				mCurrRemoteDevice = mMonitorService.getRemoteDevices().get(mCurrDevice.getMountPath());
				if(mCurrRemoteDevice != null){
					mDirectoryService =  mCurrRemoteDevice.findService(new UDAServiceType("ContentDirectory"));
					mBrowserHandler.sendEmptyMessage(BrowserMsg.REQUEST_FILES);
				}
				
			}
		};
    }

    public void initEvent(){
    	mListFile.setOnItemClickListener(this);
    	mListFile.setOnItemSelectedListener(this);
    }
    
    /**
     * 刷新预览信息
     * @param upnpFileInfo
     */
    private void refreshPreview(AllUpnpFileInfo upnpFileInfo)
    {
    	if(upnpFileInfo.getType() == ConstData.MediaType.FOLDER){
    		//UpnpFolder mediaFolder = (UpnpFolder)mListFile.getAdapter().getItem(position);
    		Container container = (Container)upnpFileInfo.getFile();
    	    mWidgetPreview.updateName(container.getTitle());
            mWidgetPreview.updateImage(getPreviewIcon(ConstData.MediaType.FOLDER));
            mWidgetPreview.updateOtherText(getPreviewInfo(container));
            mTextFileName.setText(container.getTitle());
    	}else{
    		Item item = (Item)upnpFileInfo.getFile();
    		//final UpnpFile mediaFile = (UpnpFile)mListFile.getAdapter().getItem(position);
    		mWidgetPreview.updateName(item.getTitle());
    		mWidgetPreview.updateImage(getPreviewIcon(upnpFileInfo.getType()));
    		mTextFileName.setText(item.getTitle());
    		String previewPhotoPath = upnpFileInfo.getPreviewPath();
    		Bitmap preViewBitmap = null;
    		if(!TextUtils.isEmpty(previewPhotoPath)){
    			//加载至页面
    			preViewBitmap = BitmapUtils.getScaledBitmapFromFile(previewPhotoPath, SizeUtils.dp2px(this, 280), SizeUtils.dp2px(this, 280));
    			if(preViewBitmap != null)
        			mWidgetPreview.updateImage(preViewBitmap);
    		}else{
    			loadExtraMediaInfo(upnpFileInfo);
    		}
            int mediaType = upnpFileInfo.getType();
            switch (mediaType)
            {
                case ConstData.MediaType.AUDIO:
                case ConstData.MediaType.VIDEO:
                	updateOtherText(upnpFileInfo);
                    break;
                case ConstData.MediaType.IMAGE:
                	updateOtherText(upnpFileInfo);
                    break;
            }  
            
    	}
       

    }
    
    /**
     * 是否显示文件夹
     * @return
     *///
    public boolean isShowFolder(){
    	return (mListFile.getAdapter() instanceof UpnpFolderListAdapter);
    }
    
	/**
	 * 加载Upnp文件列表
	 */
	public void loadFiles(){
		Log.i(TAG, "loadFiles");
		DialogUtils.showLoadingDialog(AllUpnpFileListActivity.this, false);
		mCurrFileBrowser = new FileBrowser(mDirectoryService, mCurrContainer.getId(), BrowseFlag.DIRECT_CHILDREN, "*", 0, 100000L, mSortCriterions);
		mUpnpService.getControlPoint().execute(mCurrFileBrowser);
	}
	
	
    protected String getPreviewInfo(Container container)
    {
        String info = getFolderPreviewInfo(container);;
        return info;
    }
	
    
    private String getAudioPreviewInfo(LocalMediaInfo mediaInfo){
        return getMediaPreviewInfo(mediaInfo);
    }

    private String getImagePreviewInfo(LocalMediaInfo mediaInfo){
        return getMediaPreviewInfo(mediaInfo);
    }

    
    private String getFolderPreviewInfo(Container container){
        String info = getString(R.string.file_tip);
        info += container.getChildCount();
        /*if(mCurrMediaType == ConstData.MediaType.FOLDER){
        	info += mediaFolder.getFileCount();
        }else if(mCurrMediaType == ConstData.MediaType.AUDIOFOLDER){
        	info += mediaFolder.getAudioCount();
        }else if(mCurrMediaType == ConstData.MediaType.VIDEOFOLDER){
        	info += mediaFolder.getVideoCount();
        }else{
        	info += mediaFolder.getImageCount();
        }*/
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
    
    
    /**
     * 从Upnp文件中获取文件大小
     * @param upnpFileInfo
     * @return
     */
    private long getFileSizeFromUpnpFileInfo(AllUpnpFileInfo upnpFileInfo){
    	Item item = (Item)upnpFileInfo.getFile();
    	try{
    		return item.getResources().get(0).getSize();
    	}catch(Exception e){
    		
    	}
    	return 0;
    }
    
    /**
     * 从Upnp文件中读取时长
     * @param upnpFileInfo
     * @return
     */
    private String getDurationFromUpnpFileInfo(AllUpnpFileInfo upnpFileInfo){
    	Item item = (Item)upnpFileInfo.getFile();
    	try{
    		return item.getResources().get(0).getDuration();
    	}catch(Exception e){
    		
    	}
    	return "";
    
    }
    
    /**
     * 从Upnp文件中读取日期
     * @param upnpFileInfo
     * @return
     */
    private String getDateFromUpnpFileInfo(AllUpnpFileInfo upnpFileInfo){
    	Item item = (Item)upnpFileInfo.getFile();
    	List<Property> properties = item.getProperties();
    	if(properties != null && properties.size() > 0){
			for(Property property : properties){
				if(property.getDescriptorName().equals("date")){
					return property.getValue().toString();
				}
				
			}
		}
    	return "";
    
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
    
    
    private void updateOtherText(AllUpnpFileInfo upnpFileInfo)
    {
        //UpnpFile mediaFile = (UpnpFile)mListFile.getAdapter().getItem(position);
        int mediaType = upnpFileInfo.getType();
        Item item = (Item)upnpFileInfo.getFile();
        String strInfo = null;
        switch(mediaType)
        {
            case ConstData.MediaType.AUDIO:
            	strInfo = String.format(getString(R.string.audio_preview_info), 
                		getFileSize(getFileSizeFromUpnpFileInfo(upnpFileInfo)), 
                		getFileType(item.getTitle(),getString(R.string.music), mCurrDevice.getDevices_type()), 
                		getRunningTime(getDurationFromUpnpFileInfo(upnpFileInfo)),
                		formatCreateDate(upnpFileInfo),getDescription(""));
            	break;
            case ConstData.MediaType.VIDEO:
            	strInfo = String.format(getString(R.string.video_preview_info),
                        getFileSize(getFileSizeFromUpnpFileInfo(upnpFileInfo)), 
                        getFileType(item.getTitle(),getString(R.string.video), mCurrDevice.getDevices_type()), 
                        getRunningTime(getDurationFromUpnpFileInfo(upnpFileInfo)), 
                        formatCreateDate(upnpFileInfo),getDescription(""));
              break;
            // 显示尺寸
            case ConstData.MediaType.IMAGE:
                strInfo = String.format(getString(R.string.image_preview_info), getFileSize(getFileSizeFromUpnpFileInfo(upnpFileInfo)),
                        getFileType(item.getTitle(),getString(R.string.image), mCurrDevice.getDevices_type()), formatCreateDate(upnpFileInfo),getDescription(""));
                break;
        }
        mWidgetPreview.updateOtherText(strInfo);
    }
    
    /**
     * 加载播放器
     */
    public void loadActivity(AllUpnpFileInfo upnpFileInfo){
        Intent intent = new Intent();
        intent.putExtra(ConstData.IntentKey.IS_INTERNAL_PLAYER, true);
        intent.putExtra(ConstData.IntentKey.EXTRAL_LOCAL_DEVICE, mCurrDevice);
        //intent.putExtra(LocalDeviceInfo.DEVICE_EXTRA_NAME, MediaFileUtils.getDeviceInfoFromDevice(mCurrDevice).compress());
        //UpnpFileService upnpFileService = new UpnpFileService();
        //List<UpnpFile> mediaFiles = upnpFileService.getFilesByDeviceIdAndParentId(mediaFile.getDeviceID(), mediaFile.getParentId(), mediaFile.getType());
        List<LocalMediaInfo> mediaInfos = MediaFileUtils.getMediaInfosFromAllUpnpFileInfo(upnpFileInfo, mDidlContent, mCurrDevice);
        Log.i(TAG, "loadActivity->mediaInfos:" + mediaInfos);
        List<Bundle> mediaInfoList = new ArrayList<Bundle>();
        for(LocalMediaInfo itemInfo : mediaInfos){
        	mediaInfoList.add(itemInfo.compress());
        }
        int newPosition = 0;
        String fileTitle = ((Item)upnpFileInfo.getFile()).getTitle();
        for(int i = 0; i != mediaInfos.size(); ++i){
        	if(fileTitle.equals(mediaInfos.get(i).getmFileName())){
        		newPosition = i;
        		break;
        	}
        }
        if (upnpFileInfo.getType() == ConstData.MediaType.AUDIO)
        {
            intent.setClass(this, InternalAudioPlayer.class);
            intent.putExtra(ConstData.IntentKey.CURRENT_INDEX, newPosition);
            InternalAudioPlayer.setMediaList(mediaInfoList, newPosition);
        }
        else if (upnpFileInfo.getType() == ConstData.MediaType.VIDEO)
        {
            intent.setClass(this, InternalVideoPlayer.class);
            intent.putExtra(ConstData.IntentKey.CURRENT_INDEX, newPosition);
            InternalVideoPlayer.setMediaList(mediaInfoList, newPosition);
        }
        else if (upnpFileInfo.getType() == ConstData.MediaType.IMAGE)
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
    
    private String formatCreateDate(AllUpnpFileInfo upnpFileInfo){
        String dataStr = getDateFromUpnpFileInfo(upnpFileInfo);
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
		if(deviceType != ConstData.DeviceType.DEVICE_TYPE_DMS)
			fileType = filename.substring(filename.lastIndexOf(".")+1);
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
    private void loadExtraMediaInfo(AllUpnpFileInfo upnpFileInfo){
    	if(mUpnpFilePreviewLoadTask != null && mUpnpFilePreviewLoadTask.getStatus() == Status.RUNNING)
    		mUpnpFilePreviewLoadTask.cancel(true);
    	mUpnpFilePreviewLoadTask = new UpnpFilePreviewLoadTask(new UpnpFilePreviewLoadTask.CallBack() {
			@Override
			public void onFinished(AllUpnpFileInfo upnpFileInfo) {
				if(mCurrFocusFileInfo == upnpFileInfo)
					refreshPreview(mCurrFocusFileInfo);
			}
		});
    	mUpnpFilePreviewLoadTask.execute(upnpFileInfo);
    }


    /**
     * 创建UPNP根目录
     * @return
     */
	private Container createRootContainer() {
		Container rootContainer = new Container();
		rootContainer.setId("0");
		rootContainer.setTitle(mCurrDevice.getPhysic_dev_id());
		return rootContainer;
	}
	
	
	/**
	 * 绑定相关服务(DeviceMonitorService)
	 */
	public void bindServices(){
		Intent intent = new Intent(this, DeviceMonitorService.class);
		bindService(intent, mDeviceMonitorConnection, android.app.Service.BIND_AUTO_CREATE);
	}
	
	/**
	 * 对相关服务解除班定(DeviceMonitorService)
	 */
	public void unBindServices(){
		unbindService(mDeviceMonitorConnection);
	}
	
	/**
	 * 填充Upnp文件列表适配器
	 * @param content
	 */
	private void fillUpnpFileAdapter(DIDLContent content){
		mDidlContent = content;
		Log.i(TAG, "fillUpnpFileAdapter->content:" + content);
		DialogUtils.closeLoadingDialog();
		List<Container> containers = content.getContainers();
		List<Item> items = content.getItems();
		List<AllUpnpFileInfo> allUpnpFileInfos = new ArrayList<AllUpnpFileInfo>();
		Log.i(TAG, "fillUpnpFileAdapter->containers:" + containers);
		if(containers != null && containers.size() > 0){
			for(Container itemContainer : containers){
				AllUpnpFileInfo fileInfo = new AllUpnpFileInfo();
				fileInfo.setFile(itemContainer);
				fileInfo.setType(ConstData.MediaType.FOLDER);
				allUpnpFileInfos.add(fileInfo);
			}
			
		}
		Log.i(TAG, "fillUpnpFileAdapter->items:" + items);
		if(items != null && items.size() > 0){
			for(Item item : items){
				AllUpnpFileInfo fileInfo = new AllUpnpFileInfo();
				fileInfo.setFile(item);
				List<Res> resources = item.getResources();
				if(resources != null &&  resources.size() > 0 && resources.get(0) != null && resources.get(0).getProtocolInfo() != null
						&& resources.get(0).getProtocolInfo().getContentFormat() != null){
					String contentFormat = resources.get(0).getProtocolInfo().getContentFormat();
					if(contentFormat.contains("audio")){
						fileInfo.setType(ConstData.MediaType.AUDIO);
					}else if(contentFormat.contains("video")){
						fileInfo.setType(ConstData.MediaType.VIDEO);
					}else if(contentFormat.contains("image")){
						fileInfo.setType(ConstData.MediaType.IMAGE);
					}else {
						fileInfo.setType(ConstData.MediaType.UNKNOWN_TYPE);
					}
				}else{
					fileInfo.setType(ConstData.MediaType.UNKNOWN_TYPE);
				}
				allUpnpFileInfos.add(fileInfo);
			}
		}
		mTextPathTitle.setText(mCurrContainer.getTitle());
		if(allUpnpFileInfos.size() > 0){
			mLayoutContentPage.setVisibility(View.VISIBLE);
			mLayoutNoFiles.setVisibility(View.GONE);
			AllUpnpFileListAdapter upnpFileListAdapter = new AllUpnpFileListAdapter(this, R.layout.adapter_file_list_item, allUpnpFileInfos);
			mListFile.setAdapter(upnpFileListAdapter);
			mListFile.requestFocus();
			if(mLastContainer != null){
				int focusIndex = getFocusIndex(mLastContainer, allUpnpFileInfos);
				mListFile.setSelection(focusIndex);
			}
		}else{
			mLayoutContentPage.setVisibility(View.GONE);
			mLayoutNoFiles.setVisibility(View.VISIBLE);
		}
	
	} 
	
	/**
	 * 获取焦点位置
	 * @param upnpFileInfo
	 * @param upnpFileInfos
	 * @return
	 */
	private int getFocusIndex(Container lastContainer, List<AllUpnpFileInfo> upnpFileInfos){
		if(upnpFileInfos != null && upnpFileInfos.size() > 0){
			for(int i = 0; i < upnpFileInfos.size(); ++i){
				try{
					String containerId = ((Container)(upnpFileInfos.get(i).getFile())).getId();
					boolean isEqual = lastContainer.getId().equals(containerId);
					if(isEqual)
						return i;
				}catch (Exception e){
					
				}
				
			}
		}
		return 0;
	}
	
	/**
	 * 处理文件接收失败
	 */
	private void processFailedReceiveFiles(){
		DialogUtils.closeLoadingDialog();
		mTextPathTitle.setText(mCurrContainer.getTitle());
		mLayoutContentPage.setVisibility(View.GONE);
		mLayoutNoFiles.setVisibility(View.VISIBLE);
	}
	
	/**
	 * @author GaoFei
	 * UPNP文件浏览
	 */
	
	class FileBrowser extends Browse{
		
		public FileBrowser(Service service, String objectID, BrowseFlag flag,
				String filter, long firstResult, Long maxResults,
				SortCriterion[] orderBy) {
			super(service, objectID, flag, filter, firstResult, maxResults, orderBy);
		}

		@Override
		public void received(ActionInvocation actionInvocation, DIDLContent didl) {
			Log.i("FileBrowser", "FileBrowser->received");
			Message receivedMessage = new Message();
			receivedMessage.what = BrowserMsg.RECEIVED_SUCC;
			receivedMessage.obj = didl;
			mBrowserHandler.sendMessage(receivedMessage);
			//List<Container> containers = didl.getContainers();
			//List<Item> items = didl.getItems();
		}

		@Override
		public void updateStatus(Status status) {
			Log.i("FileBrowser", "FileBrowser->updateStatus:" + status);
		}
		@Override
		public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
			Log.i("FileBrowser", "FileBrowser->failure");
			Message failedMessage = new Message();
			failedMessage.what = BrowserMsg.RECEIVED_FAILED;
			mBrowserHandler.sendMessage(failedMessage);
		}
		
	}
	
	/**
	 * Upnp文件浏览处理相关
	 * @author GaoFei
	 *
	 */
	class BrowserHandler extends Handler{
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case BrowserMsg.REQUEST_FILES:
				loadFiles();
				break;
			case BrowserMsg.RECEIVED_SUCC:
				fillUpnpFileAdapter((DIDLContent)msg.obj);
				break;
			case BrowserMsg.RECEIVED_FAILED:
				processFailedReceiveFiles();
				break;
			default:
				break;
			}
		}
	}
	@Override
	public void onServiceConnected() {
		// TODO Auto-generated method stub
		
	}
	
}
