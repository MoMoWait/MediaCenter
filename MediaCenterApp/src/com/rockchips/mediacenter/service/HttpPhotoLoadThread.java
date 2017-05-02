package com.rockchips.mediacenter.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.UUID;
import momo.cn.edu.fjnu.androidutils.utils.BitmapUtils;
import momo.cn.edu.fjnu.androidutils.utils.SizeUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import org.xutils.x;
import org.xutils.common.util.DensityUtil;
import org.xutils.image.ImageOptions;
import com.rockchips.mediacenter.R;
import com.rockchips.mediacenter.bean.FileInfo;
import com.rockchips.mediacenter.bean.PreviewPhotoInfo;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.modle.db.FileInfoService;
import com.rockchips.mediacenter.modle.db.PreviewPhotoInfoService;
import android.content.Intent;
import android.graphics.Bitmap.CompressFormat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

/**
 * @author GaoFei
 * Http图片下载线程
 */
public class HttpPhotoLoadThread extends AbstractPreviewLoadThread{
	private static final String TAG = "HttpPhotoLoadThread";
	private ImageView mImg;
	private FileInfo mFileInfo;
	private DeviceMonitorService mService;
	private ImageOptions mImageOptions;
	public HttpPhotoLoadThread(ImageView img, FileInfo fileInfo, DeviceMonitorService service){
		mImg = img;
		mFileInfo = fileInfo;
		mService = service;
		mImageOptions = new ImageOptions.Builder()
        .setSize(DensityUtil.dip2px(100), DensityUtil.dip2px(100))
        .setRadius(DensityUtil.dip2px(5))
        // 如果ImageView的大小不是定义为wrap_content, 不要crop.
        .setCrop(true) // 很多时候设置了合适的scaleType也不需要它.
        // 加载中或错误图片的ScaleType
        //.setPlaceholderScaleType(ImageView.ScaleType.MATRIX)
        .setImageScaleType(ImageView.ScaleType.FIT_XY)
        .setLoadingDrawableId(R.drawable.icon_preview_image)
        .setFailureDrawableId(R.drawable.icon_preview_image)
        .build();
	}
	
	@Override
	public void run() {
		if(!TextUtils.isEmpty(mFileInfo.getPreviewPath()))
			return;
		try{
			String photoUrl = mFileInfo.getPath();
			if(!TextUtils.isEmpty(photoUrl)){
				Log.i(TAG, "run->photoUrl:" + photoUrl);
				InputStream inputStream = null;
				HttpClient client = new DefaultHttpClient();
				HttpGet httpGet = new HttpGet(photoUrl);
				client.execute(httpGet);
				HttpResponse response = client.execute(httpGet);
				if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
					inputStream = response.getEntity().getContent();
				Log.i(TAG, "run->inputStream:" + inputStream);
				String tmpPath = ConstData.CACHE_IMAGE_DIRECTORY + File.separator + UUID.randomUUID().toString();
				Log.i(TAG, "run->bigPath:" + tmpPath);
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
				mFileInfo.setBigPhotoPath(tmpPath);
				String savePath = tmpPath + ".png";
				boolean isSuccess = BitmapUtils.saveScaledBitmap(tmpPath, SizeUtils.dp2px(mService, 280), SizeUtils.dp2px(mService, 280), savePath, CompressFormat.PNG, 80);
				//预览文件存储成功
				if(isSuccess){
					//获取成功
					mFileInfo.setPreviewPath(savePath);
					sendRefreshBroadCast();
					x.image().bind(mImg, savePath, mImageOptions, null);
				}
				Log.i(TAG, "run->savePath:" + savePath);
				savePreviewPhotoInfo();
				updateToDB();
			}
		}catch (Exception e){
			Log.i(TAG, "http download exception:" + e);
		}
	
	}
	
	
	private void savePreviewPhotoInfo(){
		PreviewPhotoInfoService previewPhotoInfoService = new PreviewPhotoInfoService();
		PreviewPhotoInfo updatePhotoInfo = previewPhotoInfoService.getPreviewPhotoInfo(mFileInfo.getDeviceID(), mFileInfo.getPath());
		if(updatePhotoInfo != null){
			updatePhotoInfo.setPreviewPath(mFileInfo.getPreviewPath());
			updatePhotoInfo.setBigPhotoPath(mFileInfo.getBigPhotoPath());
			previewPhotoInfoService.update(updatePhotoInfo);
		}else{
			PreviewPhotoInfo saveInfo = new PreviewPhotoInfo();
			saveInfo.setDeviceID(mFileInfo.getDeviceID());
			saveInfo.setOriginPath(mFileInfo.getPath());
			saveInfo.setPreviewPath(mFileInfo.getPreviewPath());
			saveInfo.setPreviewPath(mFileInfo.getBigPhotoPath());
			previewPhotoInfoService.save(saveInfo);
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
	 * 更新至数据库
	 */
	private void updateToDB(){
		if(mFileInfo.getId() != -1){
    		FileInfoService fileInfoService = new FileInfoService();
    		fileInfoService.update(mFileInfo);
    	}
	}
}
