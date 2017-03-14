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
import java.util.ArrayList;
import java.util.List;
import org.xutils.x;
import org.xutils.view.annotation.ViewInject;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.GridView;
import android.widget.TextView;
import com.rockchips.mediacenter.R;
import com.rockchips.mediacenter.bean.LocalDeviceInfo;
import com.rockchips.mediacenter.bean.LocalMediaInfo;
import com.rockchips.mediacenter.adapter.PhotoGridAdapter;
import com.rockchips.mediacenter.bean.Device;
import com.rockchips.mediacenter.bean.FileInfo;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.imageplayer.InternalImagePlayer;
import com.rockchips.mediacenter.modle.task.AllFileLoadTask;
import com.rockchips.mediacenter.modle.task.ImageFileLoadTask;
import com.rockchips.mediacenter.utils.DialogUtils;
import com.rockchips.mediacenter.utils.MediaFileUtils;
/**
 * 本地相册浏览
 * @author GaoFei
 *
 */

public class ALImageActivity extends AppBaseActivity implements OnItemClickListener, OnItemSelectedListener{
	
	public static final String TAG = "ALImageActivity";
	public static final int START_PLAYER_REQUEST_CODE = 99;
	@ViewInject(R.id.text_path_title)
	private TextView mTextPathTitle;
	@ViewInject(R.id.grid_album)
	private GridView mGridAlbum;
	@ViewInject(R.id.grid_image)
	private GridView mGridImage;
	@ViewInject(R.id.layout_no_files)
	private ViewGroup mLayoutNoFiles;
	
	private Device mCurrDevice; 
	private int mCurrMediaType;
	private ImageFileLoadTask mFolderLoadTask;
	private ImageFileLoadTask mFileLoadTask;
	private PhotoGridAdapter mAlbumAdapter;
	private PhotoGridAdapter mPhotoAdapter;
	private List<FileInfo> mLocalMediaFolders;
	private List<LocalMediaInfo> mLocalMediaInfos;
	private List<FileInfo> mLocalMediaFiles;
	/**
	 * 当前选中的文件
	 */
	private FileInfo mSelectFile;
	/**
	 * 当前选中的目录
	 */
	private FileInfo mSelectMediaFolder;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			if(mGridImage.getVisibility() == View.VISIBLE){
				loadFolders(true);
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		Log.i(TAG, "onItemSelected->position:" + position);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Log.i(TAG, "onItemClick");
		if(parent == mGridAlbum){
			mSelectMediaFolder = mLocalMediaFolders.get(position);
			loadFiles(mSelectMediaFolder, false);
		}else{
			mSelectFile = mLocalMediaFiles.get(position);
			loadActivity(position);
		}
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == START_PLAYER_REQUEST_CODE){
			loadFiles(mSelectMediaFolder, false);
		}
	}
	
	public void initDataAndView(){
		mCurrMediaType = getIntent().getIntExtra(ConstData.IntentKey.EXTRAL_MEDIA_TYPE, -1);
    	mCurrDevice = (Device)getIntent().getSerializableExtra(ConstData.IntentKey.EXTRAL_LOCAL_DEVICE);
		loadFolders(true);
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
	public void loadFolders(boolean isLoadRoot){
		DialogUtils.showLoadingDialog(this, false);
		startTimer(ConstData.MAX_LOAD_FILES_TIME);
		mFolderLoadTask = new ImageFileLoadTask(new ImageFileLoadTask.CallBack() {
			@Override
			public void onGetFiles(List<FileInfo> fileInfos) {
			    endTimer();
				DialogUtils.closeLoadingDialog();
				if(isOverTimer())
				    return;
				mTextPathTitle.setText(mCurrDevice.getDeviceName());
				mGridImage.setVisibility(View.GONE);
				if(fileInfos != null && fileInfos.size() > 0){
					mLayoutNoFiles.setVisibility(View.GONE);
					mLocalMediaFolders = fileInfos;
					mGridAlbum.setVisibility(View.VISIBLE);
					mAlbumAdapter = new PhotoGridAdapter(ALImageActivity.this, R.layout.adapter_photo_grid_item, fileInfos);
					mGridAlbum.setAdapter(mAlbumAdapter);
					mGridAlbum.setFocusable(true);
					mGridAlbum.setFocusableInTouchMode(true);
					mGridAlbum.requestFocus();
					int selectIndex = 0;
					if(mSelectMediaFolder != null){
						selectIndex = getFolderIndex(mSelectMediaFolder, fileInfos);
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
		});
		
    	mFolderLoadTask.execute(mCurrDevice, mCurrMediaType, mCurrDevice.getLocalMountPath(), isLoadRoot);
	}
	
	
	/**
	 * 加载文件列表
	 */
	public void loadFiles(final FileInfo folderFileInfo, boolean isLoadRoot){
		DialogUtils.showLoadingDialog(this, false);
		startTimer(ConstData.MAX_LOAD_FILES_TIME);
		mFileLoadTask = new ImageFileLoadTask(new ImageFileLoadTask.CallBack() {
			@Override
			public void onGetFiles(List<FileInfo> fileInfos) {
			    endTimer();
				DialogUtils.closeLoadingDialog();
				if(isOverTimer())
				    return;
				mLocalMediaFiles = fileInfos;
				mGridAlbum.setVisibility(View.GONE);
				mTextPathTitle.setText(mCurrDevice.getDeviceName() + ">" + folderFileInfo.getName());
				//Log.i(TAG, "loadFiles->onSuccess->mediaFiles:" + mediaFiles);
				if(fileInfos != null && fileInfos.size() > 0){
					mGridImage.setVisibility(View.VISIBLE);
					mGridImage.requestFocus();
					mLayoutNoFiles.setVisibility(View.GONE);
					mPhotoAdapter = new PhotoGridAdapter(ALImageActivity.this,  R.layout.adapter_photo_grid_item, mLocalMediaFiles);
					mGridImage.setAdapter(mPhotoAdapter);
					mGridImage.setFocusable(true);
					mGridImage.setFocusableInTouchMode(true);
					mGridImage.requestFocus();
					int selectIndex = 0;
					if(mSelectFile != null){
						selectIndex = getFileIndex(mSelectFile, mLocalMediaFiles);
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
		});    	
    	mFileLoadTask.execute(mCurrDevice, mCurrMediaType, mSelectMediaFolder.getPath(), isLoadRoot);
	}

	
	
    /**
     * 加载播放器
     */
    public void loadActivity(int position){
        Intent intent = new Intent();
        intent.putExtra(ConstData.IntentKey.IS_INTERNAL_PLAYER, true);
        intent.putExtra(LocalDeviceInfo.DEVICE_EXTRA_NAME, MediaFileUtils.getDeviceInfoFromDevice(mCurrDevice).compress());
        intent.putExtra(ConstData.IntentKey.EXTRAL_LOCAL_DEVICE, mCurrDevice);
        List<LocalMediaInfo> mediaInfos = MediaFileUtils.getLocalMediaInfos(mLocalMediaFiles, mCurrDevice, ConstData.MediaType.IMAGE);
        if(mediaInfos == null || mediaInfos.size() == 0)
        	return;
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
     private int getFolderIndex(FileInfo folderFileInfo, List<FileInfo> fileInfos){
     	if(folderFileInfo == null)
     		return -1;
     	if(fileInfos == null || fileInfos.size() == 0)
     		return -1;
     	for(int i = 0; i != fileInfos.size(); ++i){
     		if(fileInfos.get(i).getId() == folderFileInfo.getId()){
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
     private int getFileIndex(FileInfo fileInfo, List<FileInfo> fileInfos){
     	if(fileInfo == null)
     		return -1;
     	if(fileInfos == null || fileInfos.size() == 0)
     		return -1;
     	for(int i = 0; i != fileInfos.size(); ++i){
     		if(fileInfos.get(i).getId() == fileInfo.getId()){
     			return i;
     		}
     	}
     	return -1;
     }

	@Override
	public void onServiceConnected() {
		
	}
    
	
	@Override
	public int getLayoutRes() {
		return R.layout.local_image_browser;
	}
	
	@Override
	public void init() {
		initDataAndView();
		initEvent();
	}
}
