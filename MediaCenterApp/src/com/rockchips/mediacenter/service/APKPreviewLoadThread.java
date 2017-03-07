package com.rockchips.mediacenter.service;

import java.io.File;
import java.util.UUID;
import momo.cn.edu.fjnu.androidutils.utils.BitmapUtils;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.rockchips.mediacenter.bean.FileInfo;
import com.rockchips.mediacenter.bean.PreviewPhotoInfo;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.modle.db.FileInfoService;
import com.rockchips.mediacenter.modle.db.PreviewPhotoInfoService;
import com.rockchips.mediacenter.utils.APKUtils;
import com.rockchips.mediacenter.utils.MediaUtils;
/**
 * @author GaoFei
 * APK图标加载线程
 */
public class APKPreviewLoadThread extends AbstractPreviewLoadThread{
	private static final String TAG = "APKPreviewLoadThread";
    private FileInfo mFileInfo;
    private DeviceMonitorService mService;
	public APKPreviewLoadThread(FileInfo fileInfo, DeviceMonitorService service){
		mFileInfo = fileInfo;
		mService =service;
		Log.i(TAG, "APKPreviewLoadThread->mFileInfo:" + mFileInfo);
	}
	
	@Override
	public void run() {
		boolean haveVideoPlay = MediaUtils.hasMediaClient();
		Log.i(TAG, "APKPreviewLoadThread->haveVideoPlay:" + haveVideoPlay);
		if(haveVideoPlay)
			return;
		if(!TextUtils.isEmpty(mFileInfo.getPreviewPath()))
			return;
	    //读取缓存数据库
		PreviewPhotoInfoService previewPhotoInfoService = new PreviewPhotoInfoService();
		PreviewPhotoInfo photoInfo = previewPhotoInfoService.getPreviewPhotoInfo(mFileInfo.getDeviceID(), mFileInfo.getPath());
		if(photoInfo != null){
			mFileInfo.setPreviewPath(photoInfo.getPreviewPath());
			updateToDB();
			sendRefreshBroadCast();
			return;
		}
		Drawable apkDrawable = APKUtils.getApkIcon(mService, mFileInfo.getPath());
        if(apkDrawable != null){
            Bitmap previewBitmap = ((BitmapDrawable)apkDrawable).getBitmap();
            File cacheImageDirFile = new File(ConstData.CACHE_IMAGE_DIRECTORY);
            if(!cacheImageDirFile.exists())
                cacheImageDirFile.mkdirs();
            String savePath = cacheImageDirFile.getPath() + "/" + UUID.randomUUID().toString() + ".png";
            if(previewBitmap != null && BitmapUtils.saveBitmapToImage(previewBitmap, savePath, CompressFormat.PNG, 80)){
            	mFileInfo.setPreviewPath(savePath);
            	updateToDB();
            	if(mFileInfo.getId() == -1){
            		//缓存信息存储至数据库
            		PreviewPhotoInfo saveInfo = new PreviewPhotoInfo();
            		saveInfo.setDeviceID(mFileInfo.getDeviceID());
            		saveInfo.setOriginPath(mFileInfo.getPath());
            		saveInfo.setPreviewPath(savePath);
            		previewPhotoInfoService.save(saveInfo);
            	}
            	sendRefreshBroadCast();
            }
        }
	}
	
	/**
	 * 更新至数据库
	 */
	private void updateToDB(){
		if(mFileInfo.getId() != -1){
    		FileInfoService fileInfoService = new FileInfoService();
    		fileInfoService.update(mFileInfo);
    	}
	}
	
	/**
	 * 发送更新广播
	 */
	private void sendRefreshBroadCast(){
	    Intent previewIntent = new Intent(ConstData.BroadCastMsg.REFRESH_APK_PREVIEW);
        previewIntent.putExtra(ConstData.IntentKey.EXTRA_FILE_INFO, mFileInfo);
        LocalBroadcastManager.getInstance(mService).sendBroadcast(previewIntent);
	}

}
