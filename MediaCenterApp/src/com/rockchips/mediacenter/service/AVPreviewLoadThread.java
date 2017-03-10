/**
 * 
 */
package com.rockchips.mediacenter.service;

import java.io.File;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

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
import android.text.TextUtils;
import android.util.Log;
import com.rockchips.mediacenter.bean.FileInfo;
import com.rockchips.mediacenter.bean.PreviewPhotoInfo;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.modle.db.FileInfoService;
import com.rockchips.mediacenter.modle.db.PreviewPhotoInfoService;
import com.rockchips.mediacenter.utils.MediaUtils;

/**
 * @author GaoFei
 * 文件浏览时音频/视频文件预览图加载线程
 */
public class AVPreviewLoadThread extends AbstractPreviewLoadThread{
    private static final String TAG = "AVPreviewLoadThread";
    private FileInfo mFileInfo;
    private DeviceMonitorService mService;
    private boolean isOOM;
    public AVPreviewLoadThread(FileInfo fileInfo, DeviceMonitorService service){
        mFileInfo = fileInfo;
        mService = service;
    }
    
    @Override
    public void run() {
    	boolean haveVideoPlay = MediaUtils.hasMediaClient();
		Log.i(TAG, "AVPreviewLoadThread->haveVideoPlay:" + haveVideoPlay);
		Log.i(TAG, "AVPreviewLoadThread->proprity:" + getThreadPriporty());
        if(haveVideoPlay)
        	return;
        if(!TextUtils.isEmpty(mFileInfo.getPreviewPath()))
            return;
		PreviewPhotoInfoService previewPhotoInfoService = new PreviewPhotoInfoService();
		//读取缓存数据库
		PreviewPhotoInfo photoInfo = previewPhotoInfoService.getPreviewPhotoInfo(mFileInfo.getDeviceID(), mFileInfo.getPath());
		if(photoInfo != null){
			mFileInfo.setPreviewPath(photoInfo.getPreviewPath());
			mFileInfo.setDuration(photoInfo.getDuration());
			updateToDB();
			sendRefreshBroadCast();
			return;
		}
    	long startTime = System.currentTimeMillis();
		Log.i(TAG, "AVPreviewLoadThread->startTime:" + startTime);
        /**
         * 媒体信息元数据获取器
         * */
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        try{
        	long sourceStart = System.currentTimeMillis();
        	Log.i(TAG, "AVPreviewLoadThread->setDataSource->start:" + sourceStart);
            //此处发生异常，直接导致文件元数据无法解析
            mediaMetadataRetriever.setDataSource(mFileInfo.getPath());
            long sourceEnd = System.currentTimeMillis();
            Log.i(TAG, "AVPreviewLoadThread->setDataSource->end:" + sourceEnd);
            Log.i(TAG, "AVPreviewLoadThread->setDataSource->all:" + (sourceEnd - sourceStart) / 1000);
        }catch (Exception e){
            //存在发生异常的可能性
            Log.e(TAG, "AVPreviewLoadThread->setDataSource->exception:" + e);
        }
        
        String durationStr = null;
        try{
        	long timeStart = System.currentTimeMillis();
        	Log.i(TAG, "AVPreviewLoadThread->getTime->start:" + timeStart);
            durationStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long timeEnd = System.currentTimeMillis();
            Log.i(TAG, "AVPreviewLoadThread->getTime->end:" + timeEnd);
            Log.i(TAG, "AVPreviewLoadThread->getTime->all:" + (timeEnd - timeStart) / 1000 + "s");
        }catch (Exception e){
            //存在发生异常的可能性
            Log.e(TAG, "AVPreviewLoadThread->getTime->exception:" + e);
        }
        String timeDuration = null;
        if(durationStr != null){
        	timeDuration = getDuration(Long.parseLong(durationStr));
        	Log.i(TAG, "AVPreviewLoadThread->timeDuration:" + timeDuration);
            mFileInfo.setDuration(timeDuration);
        }
        Bitmap priviewBitmap = null;
        if(mFileInfo.getType() == ConstData.MediaType.VIDEO){
        	long videoThumbnailStart = System.currentTimeMillis();
        	Log.i(TAG, "AVPreviewLoadThread->getVideoThumbnail->start:" + videoThumbnailStart);
            priviewBitmap = ThumbnailUtils.createVideoThumbnail(mFileInfo.getPath(), Thumbnails.MICRO_KIND);
            long videoThumbnailEnd = System.currentTimeMillis();
            Log.i(TAG, "AVPreviewLoadThread->getVideoThumbnail->start:" + videoThumbnailEnd);
            Log.i(TAG, "AVPreviewLoadThread->getVideoThumbnail->all:" + (videoThumbnailEnd - videoThumbnailStart) / 1000 + "s");
        }else{
            byte[] albumData = mediaMetadataRetriever.getEmbeddedPicture();
            String title = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            String album = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            String artist = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            Log.i(TAG, "AVPreviewLoadThread->title:" + title);
            Log.i(TAG, "AVPreviewLoadThread->albumName:" + album);
            Log.i(TAG, "AVPreviewLoadThread->artist:" + artist);
            JSONObject otherInfoObject = new JSONObject();
            try {
				otherInfoObject.put(ConstData.AudioOtherInfo.TITLE, title == null ? "" : title);
				otherInfoObject.put(ConstData.AudioOtherInfo.ALBUM, album == null ? "" : album);
				otherInfoObject.put(ConstData.AudioOtherInfo.ARTIST, title == null ? "" : artist);
				mFileInfo.setOtherInfo(otherInfoObject.toString());
			} catch (JSONException e) {
				e.printStackTrace();
			}
            
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
                    isOOM = true;
                }
                
            }
        }
        Log.i(TAG, "AVPreviewLoadThread->previewBitmap:" + priviewBitmap);
        File cacheImageDirFile = new File(ConstData.CACHE_IMAGE_DIRECTORY);
        if(!cacheImageDirFile.exists())
            cacheImageDirFile.mkdirs();
        String savePath = cacheImageDirFile.getPath() + "/" + UUID.randomUUID().toString() + ".png";
        if(priviewBitmap != null && BitmapUtils.saveBitmapToImage(priviewBitmap, savePath, CompressFormat.PNG, 80))
            mFileInfo.setPreviewPath(savePath);
        //未发生OOM异常
        if(!isOOM){
        	//为发生异常，获取缩列图为null,文件存在问题或者无法获取
        	if(priviewBitmap == null){
        		mFileInfo.setPreviewPath(ConstData.UNKNOW);
        	}
        	updateToDB();
        	PreviewPhotoInfo saveInfo = new PreviewPhotoInfo();
        	saveInfo.setDeviceID(mFileInfo.getDeviceID());
        	saveInfo.setOriginPath(mFileInfo.getPath());
        	saveInfo.setDuration(timeDuration);
        	saveInfo.setPreviewPath(savePath);
        	saveInfo.setOhterInfo(mFileInfo.getOtherInfo());
        	previewPhotoInfoService.save(saveInfo);
            //发送广播
            sendRefreshBroadCast();
        }
        long endTime = System.currentTimeMillis();
		Log.i(TAG, "AVPreviewLoadThread->endTime:" + endTime);
		Log.i(TAG, "AVPreviewLoadThread->totalTime:" + (endTime - startTime) / 1000 + "s");
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
		 Intent previewIntent = new Intent();
         if(mFileInfo.getType() == ConstData.MediaType.AUDIO){
         	previewIntent.setAction(ConstData.BroadCastMsg.REFRESH_VIDEO_PREVIEW);
         }else{
         	previewIntent.setAction(ConstData.BroadCastMsg.REFRESH_AUDIO_PREVIEW);
         }
         previewIntent.putExtra(ConstData.IntentKey.EXTRA_FILE_INFO, mFileInfo);
         LocalBroadcastManager.getInstance(mService).sendBroadcast(previewIntent);
	}
    
}
