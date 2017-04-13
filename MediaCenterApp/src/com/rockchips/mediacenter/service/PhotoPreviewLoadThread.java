/**
 * 
 */
package com.rockchips.mediacenter.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import org.json.JSONObject;

import momo.cn.edu.fjnu.androidutils.utils.BitmapUtils;
import momo.cn.edu.fjnu.androidutils.utils.SizeUtils;
import android.content.Intent;
import android.graphics.Bitmap.CompressFormat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.rockchips.mediacenter.bean.FileInfo;
import com.rockchips.mediacenter.bean.PreviewPhotoInfo;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.modle.db.FileInfoService;
import com.rockchips.mediacenter.modle.db.PreviewPhotoInfoService;
import com.rockchips.mediacenter.utils.MediaUtils;
import com.rockchips.mediacenter.utils.PlatformUtils;

/**
 * @author GaoFei
 * 加载图片预览图
 */
public class PhotoPreviewLoadThread extends AbstractPreviewLoadThread{
    private static final String TAG = "PhotoPreviewLoadThread";
    private FileInfo mFileInfo;
    private DeviceMonitorService mService;
    public PhotoPreviewLoadThread(FileInfo fileInfo, DeviceMonitorService service){
        mFileInfo = fileInfo;
        mService = service;
    }
    
    @Override
    public void run() {
    	//存在视频播放，关闭缩列图获取
        if(PlatformUtils.getSDKVersion() >= 24 && MediaUtils.hasMediaClient())
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
		if(mFileInfo.getPath().startsWith("http")){
			String otherInfo = mFileInfo.getOtherInfo();
			try{
				JSONObject otherInfoObject = new JSONObject(otherInfo);
				String albumPhotoURI = otherInfoObject.getString(ConstData.UpnpFileOhterInfo.ALBUM_URI);
				if(!TextUtils.isEmpty(albumPhotoURI)){
					URL url = new URL(albumPhotoURI);
					HttpURLConnection connection = (HttpURLConnection)url.openConnection();
					InputStream inputStream = connection.getInputStream();
					String tmpPath = ConstData.CACHE_IMAGE_DIRECTORY + File.separator + UUID.randomUUID().toString();
					BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(tmpPath));
					byte[] buffer = new byte[2048];
					int readLength = 0;
					//存储临时文件
					while((readLength = inputStream.read(buffer)) > 0){
						bufferedOutputStream.write(buffer, 0, readLength);
					}
					bufferedOutputStream.flush();
					bufferedOutputStream.close();
					inputStream.close();
					String savePath = tmpPath + ".png";
					boolean isSuccess = BitmapUtils.saveScaledBitmap(tmpPath, SizeUtils.dp2px(mService, 280), SizeUtils.dp2px(mService, 280), savePath, CompressFormat.PNG, 80);
					//预览文件存储成功
					if(isSuccess){
						//删除临时文件
						File tmpFile = new File(tmpPath);
						tmpFile.delete();
						//获取成功
						mFileInfo.setPreviewPath(savePath);
						updateToDB();
						savePreviewPhotoInfo(savePath);
						sendRefreshBroadCast();
					}
				}
			}catch (Exception e){
				Log.i(TAG, "get net work preview photo exception:" + e);
			}
			return;
		}
        File cacheImageDirFile = new File(ConstData.CACHE_IMAGE_DIRECTORY);
        if(!cacheImageDirFile.exists())
            cacheImageDirFile.mkdirs();
        String savePath = cacheImageDirFile.getPath() + "/" + UUID.randomUUID().toString() + ".png";
        boolean isSaveSuccess = BitmapUtils.saveScaledBitmap(mFileInfo.getPath(), SizeUtils.dp2px(mService, 280), SizeUtils.dp2px(mService, 280), savePath, CompressFormat.PNG, 80);
        if(isSaveSuccess){
        	mFileInfo.setPreviewPath(savePath);
        	//更新至数据库
        	updateToDB();
        	//缓存信息存储至数据库
    		savePreviewPhotoInfo(savePath);
            //发送广播
            sendRefreshBroadCast();
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
		Intent previewIntent = new Intent(ConstData.BroadCastMsg.REFRESH_PHOTO_PREVIEW);
        previewIntent.putExtra(ConstData.IntentKey.EXTRA_FILE_INFO, mFileInfo);
        LocalBroadcastManager.getInstance(mService).sendBroadcast(previewIntent);
	}
	
	/**
	 * 
	 * @param savePath
	 */
	private void savePreviewPhotoInfo(String savePath){
		PreviewPhotoInfoService previewPhotoInfoService = new PreviewPhotoInfoService();
		PreviewPhotoInfo saveInfo = new PreviewPhotoInfo();
		saveInfo.setDeviceID(mFileInfo.getDeviceID());
		saveInfo.setOriginPath(mFileInfo.getPath());
		saveInfo.setPreviewPath(savePath);
		previewPhotoInfoService.save(saveInfo);
	}
}
