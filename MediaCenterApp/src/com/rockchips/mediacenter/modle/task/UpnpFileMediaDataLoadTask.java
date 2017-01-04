package com.rockchips.mediacenter.modle.task;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import momo.cn.edu.fjnu.androidutils.data.CommonValues;
import momo.cn.edu.fjnu.androidutils.utils.BitmapUtils;
import momo.cn.edu.fjnu.androidutils.utils.SizeUtils;
import com.rockchips.mediacenter.bean.UpnpFile;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.modle.db.UpnpFileService;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

/**
 * @author GaoFei
 * UPNP文件预览图获取器
 */
public class UpnpFileMediaDataLoadTask extends AsyncTask<UpnpFile, Integer, Integer> {
	public static final String TAG = UpnpFileMediaDataLoadTask.class.getSimpleName();
	public interface CallBack{
		void onFinish(UpnpFile upnpFile);
	}
	private CallBack mCallBack;
	private UpnpFile mUpnpFile;
	private boolean isOOM;
	public UpnpFileMediaDataLoadTask(CallBack callBack){
		mCallBack = callBack;
	}
	
	@Override
	protected Integer doInBackground(UpnpFile... params) {
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		mUpnpFile = params[0];
		String photoUrl = null;
		if(mUpnpFile.getType() == ConstData.MediaType.IMAGE)
			photoUrl = mUpnpFile.getPath();
		else
		    photoUrl = mUpnpFile.getAlbumArtURI();
		Log.i(TAG, "doInBackground->photoUrl:" + photoUrl);
		if (!TextUtils.isEmpty(photoUrl)) {
			try {
				File cacheImageDirFile = new File(ConstData.CACHE_IMAGE_DIRECTORY);
				if(!cacheImageDirFile.exists())
					cacheImageDirFile.mkdirs();
				URL url = new URL(photoUrl);
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				BitmapFactory.Options options = new BitmapFactory.Options();
				int targetWidth  = SizeUtils.dp2px(CommonValues.application, 280);
				int targetHeight = SizeUtils.dp2px(CommonValues.application, 280);
				options.inJustDecodeBounds = true;
				InputStream sourceInputStream = connection.getInputStream();
				byte[] bufferData = new byte[2048];
				String originPath = cacheImageDirFile.getPath() + "/" + UUID.randomUUID().toString();
				BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(new File(originPath)));
				int readLength = sourceInputStream.read(bufferData, 0, bufferData.length);
				while(readLength > 0){
					outputStream.write(bufferData, 0, readLength);
					readLength = sourceInputStream.read(bufferData, 0, bufferData.length);
				}
				outputStream.flush();
				outputStream.close();
				BitmapFactory.decodeFile(originPath, options);
				int bitmapWidth = options.outWidth;
				int bitmapHeight = options.outHeight;
				Log.i(TAG, "doInBackground->bitmapWidth:" + bitmapWidth);
				Log.i(TAG, "doInBackground->bitmapHeight:" + bitmapHeight);
				options.inJustDecodeBounds = false;
				int scaleX = bitmapWidth / targetWidth;
				int scaleY = bitmapHeight / targetHeight;
				int scale = Math.max(scaleX, scaleY);
				if(scale > 1)
					options.inSampleSize = scale;
				else
					options.inSampleSize = 1;
				Bitmap previewBitmap = null;
				try{
					 previewBitmap = BitmapFactory.decodeFile(originPath, options);
				}catch (OutOfMemoryError error){
					isOOM = true;
				}
				Log.i(TAG, "previewBitmap == null ?" + (previewBitmap == null));
				String savePath = cacheImageDirFile.getPath() + "/" + UUID.randomUUID().toString() + ".png";
				if(previewBitmap != null && BitmapUtils.saveBitmapToImage(previewBitmap, savePath, CompressFormat.PNG, 80))
					mUpnpFile.setPreviewPhotoPath(savePath);
				
				if(!isOOM)
					mUpnpFile.setLoadPreviewPhoto(true);
				//更新至数据库中
				UpnpFileService upnpFileService = new UpnpFileService();
				upnpFileService.update(mUpnpFile);
				connection.disconnect();
			} catch (Exception e) {
				e.printStackTrace();
				Log.i(TAG, "refreshView exception:" + e);
			}
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(Integer result) {
		mCallBack.onFinish(mUpnpFile);
	}
}
