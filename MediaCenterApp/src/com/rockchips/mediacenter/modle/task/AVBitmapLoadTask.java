package com.rockchips.mediacenter.modle.task;
import momo.cn.edu.fjnu.androidutils.data.CommonValues;
import momo.cn.edu.fjnu.androidutils.utils.SizeUtils;
import com.rockchips.mediacenter.bean.AllFileInfo;
import com.rockchips.mediacenter.data.ConstData;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.provider.MediaStore.Video.Thumbnails;
import android.util.Log;
/**
 * @author GaoFei
 * 获取音频，视频文件缩列图任务
 */
public class AVBitmapLoadTask extends AsyncTask<AllFileInfo, Integer, Integer> {
	private String TAG = AVBitmapLoadTask.class.getSimpleName();
	public interface CallBack{
		void onFinished(AllFileInfo allFileInfo);
	}
	private CallBack mCallBack;
	private AllFileInfo mFileInfo;
	public AVBitmapLoadTask(CallBack callBack){
		mCallBack = callBack;
	}
	
	@Override
	protected Integer doInBackground(AllFileInfo... params) {
		/**
		 * 媒体信息元数据获取器
		 * */
		MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
		mFileInfo = params[0];
		try{
			//此处发生异常，直接导致文件元数据无法解析
			mediaMetadataRetriever.setDataSource(mFileInfo.getFile().getPath());
		}catch (Exception e){
			//存在发生异常的可能性
			Log.e(TAG, "doInBackground->setDataSource->exception:" + e);
		}
		
		String durationStr = null;
		try{
			 durationStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
		}catch (Exception e){
			//存在发生异常的可能性
			Log.e(TAG, "doInBackground->extractMetadata->exception:" + e);
		}
		
		if(durationStr != null){
			mFileInfo.setDuration(getDuration(Long.parseLong(durationStr)));
		}
		Bitmap priviewBitmap = null;
		if(mFileInfo.getType() == ConstData.MediaType.VIDEO){
			priviewBitmap = ThumbnailUtils.createVideoThumbnail(mFileInfo.getFile().getPath(), Thumbnails.MICRO_KIND);
		}else{
			byte[] albumData = mediaMetadataRetriever.getEmbeddedPicture();
			if(albumData != null && albumData.length > 0){
				BitmapFactory.Options options = new BitmapFactory.Options();
				int targetWidth  = SizeUtils.dp2px(CommonValues.application, 280);
				int targetHeight = SizeUtils.dp2px(CommonValues.application, 280);
				options.inJustDecodeBounds = true;
				BitmapFactory.decodeByteArray(albumData, 0, albumData.length, options);
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
				try{
					//存在发生OOM的可能性
					priviewBitmap = BitmapFactory.decodeByteArray(albumData, 0, albumData.length, options);
				}catch (OutOfMemoryError error){
					//no handle
				}
				
			}
		}
		
		if(priviewBitmap != null)
			mFileInfo.setBitmap(priviewBitmap);
		
		return ConstData.TaskExecuteResult.SUCCESS;
	
	}
	
	@Override
	protected void onPostExecute(Integer result) {
		mCallBack.onFinished(mFileInfo);
	}
	
	public String getDuration(long time){
		String duration = null;
		long secondes = time / 1000;
		long hour = secondes / 60 / 60;
		long minute = secondes / 60 % 60;
		long second = secondes % 60;
		duration = String.format("%02d:%02d:%02d", hour, minute, second);
		return duration;
	}

}
