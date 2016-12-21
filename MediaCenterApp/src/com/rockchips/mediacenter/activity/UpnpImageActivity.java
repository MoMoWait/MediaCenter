/**
 * Title: AlImageActivity.java<br>
 * Package: com.rockchips.mediacenter.activity<br>
 * Description: TODO<br>
 * @author w00190739
 * @version v1.0<br>
 * Date: 2014-7-15下午7:23:09<br> 
 * Copyright © Huawei Technologies Co., Ltd. 2014. All rights reserved.
 */

package com.rockchips.mediacenter.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xutils.x;
import org.xutils.view.annotation.ViewInject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rockchips.mediacenter.R;
import com.rockchips.mediacenter.basicutils.bean.LocalDeviceInfo;
import com.rockchips.mediacenter.basicutils.bean.LocalMediaInfo;
import com.rockchips.mediacenter.basicutils.constant.Constant;
import com.rockchips.mediacenter.basicutils.constant.Constant.EBrowerType;
import com.rockchips.mediacenter.basicutils.util.DiskUtil;
import com.rockchips.mediacenter.basicutils.util.StringUtils;
import com.rockchips.mediacenter.widget.ThumbnailManager;
import com.rockchips.mediacenter.basicutils.constant.Constant;
import com.rockchips.mediacenter.adapter.FileListAdapter;
import com.rockchips.mediacenter.adapter.FolderListAdapter;
import com.rockchips.mediacenter.adapter.PhotoGridAdapter;
import com.rockchips.mediacenter.audioplayer.InternalAudioPlayer;
import com.rockchips.mediacenter.bean.LocalDevice;
import com.rockchips.mediacenter.bean.LocalMediaFile;
import com.rockchips.mediacenter.bean.LocalMediaFolder;
import com.rockchips.mediacenter.bean.UpnpFile;
import com.rockchips.mediacenter.bean.UpnpFolder;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.imageplayer.InternalImagePlayer;
import com.rockchips.mediacenter.listadapter.ALImageBrowserAdapater;
import com.rockchips.mediacenter.listadapter.LocalDeviceDiskAdapter;
import com.rockchips.mediacenter.modle.db.LocalMediaFileService;
import com.rockchips.mediacenter.modle.task.FileLoadTask;
import com.rockchips.mediacenter.modle.task.FolderLoadTask;
import com.rockchips.mediacenter.modle.task.UpnpFileLoadTask;
import com.rockchips.mediacenter.modle.task.UpnpFolderLoadTask;
import com.rockchips.mediacenter.util.DialogUtils;
import com.rockchips.mediacenter.util.MediaFileUtils;
import com.rockchips.mediacenter.videoplayer.InternalVideoPlayer;
import com.rockchips.mediacenter.view.LoadingDialog;
import com.rockchips.mediacenter.viewutils.animgridview.AnimGridView;
import com.rockchips.mediacenter.viewutils.animgridview.HWListViewBaseAdapter;

/**
 * Description:Upnp文件浏览器
 * @author w00190739
 * @version v1.0 Date: 2014-7-15 下午7:23:09<br>
 */

public class UpnpImageActivity extends AppBaseActivity implements OnItemClickListener, OnItemSelectedListener{
	
	public static final String TAG = "UpnpImageActivity";
	public static final int START_PLAYER_REQUEST_CODE = 99;
	@ViewInject(R.id.text_path_title)
	private TextView mTextPathTitle;
	@ViewInject(R.id.grid_album)
	private GridView mGridAlbum;
	@ViewInject(R.id.grid_image)
	private GridView mGridImage;
	@ViewInject(R.id.layout_no_files)
	private ViewGroup mLayoutNoFiles;
	
	private LocalDevice mCurrDevice; 
	private int mCurrMediaType;
	private UpnpFolderLoadTask mFolderLoadTask;
	private UpnpFileLoadTask mFileLoadTask;
	private PhotoGridAdapter mAlbumAdapter;
	private PhotoGridAdapter mPhotoAdapter;
	private List<UpnpFolder> mLocalMediaFolders;
	private List<LocalMediaInfo> mLocalMediaInfos;
	private List<UpnpFile> mLocalMediaFiles;
	/**
	 * 当前选中的文件
	 */
	private UpnpFile mSelectFile;
	/**
	 * 当前选中的目录
	 */
	private UpnpFolder mSelectMediaFolder;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		 setContentView(R.layout.local_image_browser);
		 x.view().inject(this);
		 initDataAndView();
		 initEvent();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			if(mGridImage.getVisibility() == View.VISIBLE){
				loadFolders();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		//Log.i(TAG, "onItemSelected->position:" + position);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		//Log.i(TAG, "onItemClick");
		if(parent == mGridAlbum){
			mSelectMediaFolder = mLocalMediaFolders.get(position);
			loadFiles(mSelectMediaFolder);
		}else{
			mSelectFile = mLocalMediaFiles.get(position);
			loadActivity(position);
		}
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == START_PLAYER_REQUEST_CODE){
			loadFiles(mSelectMediaFolder);
		}
	}
	
	public void initDataAndView(){
		mCurrMediaType = getIntent().getIntExtra(ConstData.IntentKey.EXTRAL_MEDIA_TYPE, -1);
    	mCurrDevice = (LocalDevice)getIntent().getSerializableExtra(ConstData.IntentKey.EXTRAL_LOCAL_DEVICE);
		loadFolders();
	}
	
	public void initEvent(){
		mGridAlbum.setOnItemClickListener(this);
		mGridAlbum.setOnItemSelectedListener(this);
		mGridImage.setOnItemClickListener(this);
		mGridImage.setOnItemSelectedListener(this);
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
				//Log.i(TAG, "onSuccess->mediaFolders:" + mediaFolders);
				mGridImage.setVisibility(View.GONE);
				if(mediaFolders != null && mediaFolders.size() > 0){
					mLayoutNoFiles.setVisibility(View.GONE);
					mLocalMediaFolders = mediaFolders;
					mGridAlbum.setVisibility(View.VISIBLE);
					mGridAlbum.requestFocus();
					mAlbumAdapter = new PhotoGridAdapter(UpnpImageActivity.this, R.layout.adapter_photo_grid_item, MediaFileUtils.getMediaInfoListFromUpnpFolders(mediaFolders, ConstData.MediaType.IMAGEFOLDER));
					mGridAlbum.setAdapter(mAlbumAdapter);
					int selectIndex = 0;
					if(mSelectMediaFolder != null){
						selectIndex = getFolderIndex(mSelectMediaFolder, mediaFolders);
					}
					if(selectIndex >= 0){
						mGridAlbum.setSelection(selectIndex);
					}else{
						mGridAlbum.setSelection(0);
					}
					
				}else{
					//no files
					mLayoutNoFiles.setVisibility(View.VISIBLE);
					mGridAlbum.setVisibility(View.GONE);
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
	 */
	public void loadFiles(final UpnpFolder mediaFolder){
		DialogUtils.showLoadingDialog(this, false);
    	mFileLoadTask = new UpnpFileLoadTask(new UpnpFileLoadTask.Callback() {
			@Override
			public void onSuccess(List<UpnpFile> mediaFiles) {
				DialogUtils.closeLoadingDialog();
				mLocalMediaFiles = mediaFiles;
				mGridAlbum.setVisibility(View.GONE);
				mTextPathTitle.setText(mCurrDevice.getPhysic_dev_id() + ">" + mediaFolder.getName());
				//Log.i(TAG, "loadFiles->onSuccess->mediaFiles:" + mediaFiles);
				if(mediaFiles != null && mediaFiles.size() > 0){
					mGridImage.setVisibility(View.VISIBLE);
					mGridImage.requestFocus();
					mLayoutNoFiles.setVisibility(View.GONE);
					//mGridAlbum.setVisibility(View.GONE);
					mLocalMediaInfos = MediaFileUtils.getMediaInfoListFromUpnpFileList(mediaFiles);
					mPhotoAdapter = new PhotoGridAdapter(UpnpImageActivity.this,  R.layout.adapter_photo_grid_item, mLocalMediaInfos);
					mGridImage.setAdapter(mPhotoAdapter);
					int selectIndex = 0;
					if(mSelectFile != null){
						selectIndex = getFileIndex(mSelectFile, mediaFiles);
					}
					if(selectIndex >= 0)
						mGridImage.setSelection(selectIndex);
					else
						mGridImage.setSelection(0);
				}else{
					//no files
					mLayoutNoFiles.setVisibility(View.VISIBLE);
					mGridImage.setVisibility(View.GONE);
				}
			}
			
			@Override
			public void onFailed() {
				DialogUtils.closeLoadingDialog();
			}
		});
    	
    	mFileLoadTask.execute(mCurrDevice.getDeviceID(), mediaFolder.getItmeId(),  "" + ConstData.MediaType.IMAGE);
	}

	
	
    /**
     * 加载播放器
     */
    public void loadActivity(int position){
        Intent intent = new Intent();
        intent.putExtra(ConstData.IntentKey.IS_INTERNAL_PLAYER, true);
        intent.putExtra(ConstData.IntentKey.EXTRAL_LOCAL_DEVICE, mCurrDevice);
        intent.putExtra(LocalDeviceInfo.DEVICE_EXTRA_NAME, MediaFileUtils.getDeviceInfoFromDevice(mCurrDevice).compress());
        List<LocalMediaInfo> mediaInfos =mLocalMediaInfos;
        List<Bundle> mediaInfoList = new ArrayList<Bundle>();
        for(LocalMediaInfo itemInfo : mediaInfos){
        	mediaInfoList.add(itemInfo.compress());
        }
        int newPosition = position;
        intent.setClass(this, InternalImagePlayer.class);
        intent.putExtra(ConstData.IntentKey.IS_INTERNAL_PLAYER, true);
        intent.putExtra(ConstData.IntentKey.CURRENT_INDEX, newPosition);
        InternalImagePlayer.setMediaList(mediaInfoList, newPosition);
        //Log.i(TAG, "start internal player");
        startActivityForResult(intent, START_PLAYER_REQUEST_CODE);
    }
    
    
    /**
     * 获取文件夹索引
     * @param mediaFolder
     * @param mediaFiles
     * @return
     */
     private int getFolderIndex(UpnpFolder mediaFolder, List<UpnpFolder> mediaFiles){
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
    
}
