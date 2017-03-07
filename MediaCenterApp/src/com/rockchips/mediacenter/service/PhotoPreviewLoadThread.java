/**
 * 
 */
package com.rockchips.mediacenter.service;

import java.io.File;
import java.util.UUID;
import momo.cn.edu.fjnu.androidutils.utils.BitmapUtils;
import momo.cn.edu.fjnu.androidutils.utils.SizeUtils;
import android.content.Intent;
import android.graphics.Bitmap.CompressFormat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.rockchips.mediacenter.bean.FileInfo;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.modle.db.FileInfoService;
import com.rockchips.mediacenter.utils.MediaUtils;

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
    	boolean haveVideoPlay = MediaUtils.hasMediaClient();
		Log.i(TAG, "PhotoPreviewLoadThread->haveVideoPlay:" + haveVideoPlay);
        if(haveVideoPlay)
            return;
        if(!TextUtils.isEmpty(mFileInfo.getPreviewPath()))
        	return;
        File cacheImageDirFile = new File(ConstData.CACHE_IMAGE_DIRECTORY);
        if(!cacheImageDirFile.exists())
            cacheImageDirFile.mkdirs();
        String savePath = cacheImageDirFile.getPath() + "/" + UUID.randomUUID().toString() + ".png";
        boolean isSaveSuccess = BitmapUtils.saveScaledBitmap(mFileInfo.getPath(), SizeUtils.dp2px(mService, 280), SizeUtils.dp2px(mService, 280), savePath, CompressFormat.PNG, 80);
        if(isSaveSuccess){
        	mFileInfo.setPreviewPath(savePath);
        	//更新至数据库
        	if(mFileInfo.getId() != -1){
        		FileInfoService fileInfoService = new FileInfoService();
            	fileInfoService.update(mFileInfo);
        	}
            //发送广播
            Intent previewIntent = new Intent(ConstData.BroadCastMsg.REFRESH_PHOTO_PREVIEW);
            previewIntent.putExtra(ConstData.IntentKey.EXTRA_FILE_INFO, mFileInfo);
            LocalBroadcastManager.getInstance(mService).sendBroadcast(previewIntent);
        }
            
    }
    
}
