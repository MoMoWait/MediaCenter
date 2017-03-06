/**
 * 
 */
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
import android.text.TextUtils;
import android.util.Log;
import com.rockchips.mediacenter.bean.FileInfo;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.modle.db.FileInfoService;

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
        if(mService.isHaveVideoPlay())
        	return;
        if(!TextUtils.isEmpty(mFileInfo.getPreviewPath()))
            return;
    	long startTime = System.currentTimeMillis();
		Log.i(TAG, "AVPreviewLoadThread->startTime:" + startTime);
        /**
         * 媒体信息元数据获取器
         * */
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        try{
            //此处发生异常，直接导致文件元数据无法解析
            mediaMetadataRetriever.setDataSource(mFileInfo.getPath());
        }catch (Exception e){
            //存在发生异常的可能性
            Log.e(TAG, "AVPreviewLoadThread->setDataSource->exception:" + e);
        }
        
        String durationStr = null;
        try{
             durationStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        }catch (Exception e){
            //存在发生异常的可能性
            Log.e(TAG, "AVPreviewLoadThread->extractMetadata->exception:" + e);
        }
        Log.i(TAG, "AVPreviewLoadThread->durationStr:" + durationStr);
        if(durationStr != null){
            mFileInfo.setDuration(getDuration(Long.parseLong(durationStr)));
        }
        Bitmap priviewBitmap = null;
        if(mFileInfo.getType() == ConstData.MediaType.VIDEO){
            priviewBitmap = ThumbnailUtils.createVideoThumbnail(mFileInfo.getPath(), Thumbnails.MICRO_KIND);
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
        	if(mFileInfo.getId() != -1){
        		//尝试更新至数据库
            	FileInfoService fileInfoService = new FileInfoService();
            	fileInfoService.update(mFileInfo);
        	}
            //发送广播
            Intent previewIntent = new Intent();
            if(mFileInfo.getType() == ConstData.MediaType.AUDIO){
            	previewIntent.setAction(ConstData.BroadCastMsg.REFRESH_VIDEO_PREVIEW);
            }else{
            	previewIntent.setAction(ConstData.BroadCastMsg.REFRESH_AUDIO_PREVIEW);
            }
            previewIntent.putExtra(ConstData.IntentKey.EXTRA_FILE_INFO, mFileInfo);
            LocalBroadcastManager.getInstance(mService).sendBroadcast(previewIntent);
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

	@Override
	public int getThreadPriporty() {
		return ConstData.THREAD_PRIORITY--;
	}

}
