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
import android.util.Log;

import com.rockchips.mediacenter.bean.AllFileInfo;
import com.rockchips.mediacenter.data.ConstData;

/**
 * @author GaoFei
 * 音频/视频文件预览图加载线程
 */
public class AVPreviewLoadThread extends Thread {
    private static final String TAG = "AVPreviewLoadThread";
    
    private AllFileInfo mAllFileInfo;
    private DeviceMonitorService mService;
    private boolean isOOM;
    public AVPreviewLoadThread(AllFileInfo fileInfo, DeviceMonitorService service){
        mAllFileInfo = fileInfo;
        mService = service;
    }
    
    @Override
    public void run() {
        //Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        if(mAllFileInfo.isLoadPreview())
            return;
        /**
         * 媒体信息元数据获取器
         * */
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        try{
            //此处发生异常，直接导致文件元数据无法解析
            mediaMetadataRetriever.setDataSource(mAllFileInfo.getFile().getPath());
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
            mAllFileInfo.setDuration(getDuration(Long.parseLong(durationStr)));
        }
        Bitmap priviewBitmap = null;
        if(mAllFileInfo.getType() == ConstData.MediaType.VIDEO){
            priviewBitmap = ThumbnailUtils.createVideoThumbnail(mAllFileInfo.getFile().getPath(), Thumbnails.MICRO_KIND);
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
        File cacheImageDirFile = new File(ConstData.CACHE_IMAGE_DIRECTORY);
        if(!cacheImageDirFile.exists())
            cacheImageDirFile.mkdirs();
        String savePath = cacheImageDirFile.getPath() + "/" + UUID.randomUUID().toString() + ".png";
        if(priviewBitmap != null && BitmapUtils.saveBitmapToImage(priviewBitmap, savePath, CompressFormat.PNG, 80))
            mAllFileInfo.setPriviewPhotoPath(savePath);
        //未发生OOM异常
        if(!isOOM){
            mAllFileInfo.setLoadPreview(true);
            //发送广播
            Intent previewIntent = new Intent(ConstData.BroadCastMsg.REFRESH_AV_PREVIEW);
            previewIntent.putExtra(ConstData.IntentKey.EXTRA_ALL_FILE_INFO, mAllFileInfo);
            LocalBroadcastManager.getInstance(mService).sendBroadcast(previewIntent);
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

}
