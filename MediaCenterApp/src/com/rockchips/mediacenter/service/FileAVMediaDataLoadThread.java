package com.rockchips.mediacenter.service;
import java.io.File;
import java.util.UUID;
import momo.cn.edu.fjnu.androidutils.data.CommonValues;
import momo.cn.edu.fjnu.androidutils.utils.BitmapUtils;
import momo.cn.edu.fjnu.androidutils.utils.SizeUtils;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.provider.MediaStore.Video.Thumbnails;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.rockchips.mediacenter.bean.LocalMediaFile;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.modle.db.LocalMediaFileService;
import com.rockchips.mediacenter.modle.task.FileMediaDataLoadTask;
/**
 * 音乐，视频类别下的获取缩列图方式
 * @author GaoFei
 *
 */
public class FileAVMediaDataLoadThread extends Thread implements Comparable<FileAVMediaDataLoadThread>{

	public static final String TAG = FileMediaDataLoadTask.class.getSimpleName();
	private LocalMediaFile mLocalMediaFile;
	private DeviceMonitorService mService;
	private int mPriority;
	private boolean isOOM;
	public FileAVMediaDataLoadThread(LocalMediaFile mediaFile, DeviceMonitorService service, int priority){
		mLocalMediaFile = mediaFile;
		mService = service;
		mPriority = priority;
	}
	
	@Override
	public void run() {

		//Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		Log.i(TAG, "doInBackground");
		/**
		 * 媒体信息元数据获取器
		 * */
		MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
		try{
			//此处发生异常，直接导致文件元数据无法解析
			mediaMetadataRetriever.setDataSource(mLocalMediaFile.getPath());
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
			mLocalMediaFile.setDuration(getDuration(Long.parseLong(durationStr)));
		}
		Bitmap priviewBitmap = null;
		if(mLocalMediaFile.getType() == ConstData.MediaType.VIDEO){
			priviewBitmap = ThumbnailUtils.createVideoThumbnail(mLocalMediaFile.getPath(), Thumbnails.MICRO_KIND);
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
					priviewBitmap = BitmapFactory.decodeByteArray(albumData, 0, albumData.length, options);
				}catch (OutOfMemoryError error){
					isOOM = true;
				}
				
			}
		}
		File cacheImageDirFile = new File(ConstData.CACHE_IMAGE_DIRECTORY);
		if(!cacheImageDirFile.exists())
			cacheImageDirFile.mkdirs();
		String savePath = cacheImageDirFile.getPath() + "/" + UUID.randomUUID().toString() + ".png";
		if(priviewBitmap != null && BitmapUtils.saveBitmapToImage(priviewBitmap, savePath, CompressFormat.PNG, 80))
			mLocalMediaFile.setPreviewPhotoPath(savePath);
		//未发生OOM异常
		if(!isOOM){
			mLocalMediaFile.setLoadPreviewPhoto(true);
			//更新至数据库中
			LocalMediaFileService localMediaFileService = new LocalMediaFileService();
			localMediaFileService.update(mLocalMediaFile);
			//发送广播
			Intent intent = new Intent(ConstData.BroadCastMsg.REFRESH_LOCAL_MEDIA_AV_PREVIEW);
			intent.putExtra(ConstData.IntentKey.EXTRA_LOCAL_MEDIA_FILE, mLocalMediaFile);
			LocalBroadcastManager.getInstance(mService).sendBroadcast(intent);
		}
			
	
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

	@Override
	public int compareTo(FileAVMediaDataLoadThread o) {
		if(mPriority < o.mPriority)
			return -1;
		else if(mPriority == o.mPriority)
			return 0;
		return 1;
	}

}
