package com.rockchips.mediacenter.modle.task;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import org.fourthline.cling.support.model.DIDLObject.Property;
import org.fourthline.cling.support.model.item.Item;
import momo.cn.edu.fjnu.androidutils.data.CommonValues;
import momo.cn.edu.fjnu.androidutils.utils.BitmapUtils;
import momo.cn.edu.fjnu.androidutils.utils.SizeUtils;
import com.rockchips.mediacenter.bean.AllUpnpFileInfo;
import com.rockchips.mediacenter.data.ConstData;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

/**
 * @author GaoFei
 * Upnp文件预览图
 */
public class UpnpFilePreviewLoadTask extends AsyncTask<AllUpnpFileInfo, Integer, Integer> {
	
	static final String TAG = "UpnpFilePreviewLoadTask";
	
	public interface CallBack{
		void onFinished(AllUpnpFileInfo upnpFileInfo);
	}
	
	private CallBack mCallBack;
	private AllUpnpFileInfo mFileInfo;
	private boolean isOOM = false;
	public UpnpFilePreviewLoadTask(CallBack callBack){
		mCallBack = callBack;
	}
	
	@Override
	protected Integer doInBackground(AllUpnpFileInfo... params) {
		mFileInfo = params[0];
		String photoUrl = null;
		Item item = (Item)mFileInfo.getFile();
		if(mFileInfo.getType() == ConstData.MediaType.IMAGE)
			photoUrl = item.getResources().get(0).getValue();
		else{
			List<Property> properties = item.getProperties();
			if(properties != null && properties.size() > 0){
				for(Property property : properties){
					if(property.getDescriptorName().equals("albumArtURI")){
						photoUrl = property.getValue().toString();
						break;
					}
					
				}
			}
		}
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
					mFileInfo.setPreviewPath(savePath);
				
				if(!isOOM)
					mFileInfo.setLoadPreview(true);
				//更新至数据库中
		/*		UpnpFileService upnpFileService = new UpnpFileService();
				upnpFileService.update(mUpnpFile);*/
				connection.disconnect();
			} catch (Exception e) {
				e.printStackTrace();
				Log.i(TAG, "refreshView exception:" + e);
			}
		}
		return ConstData.TaskExecuteResult.SUCCESS;
	
	}
	
	@Override
	protected void onPostExecute(Integer result) {
		mCallBack.onFinished(mFileInfo);
	}
}
