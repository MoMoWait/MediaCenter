package com.rockchips.mediacenter.service;

import java.io.File;
import java.util.UUID;

import momo.cn.edu.fjnu.androidutils.utils.BitmapUtils;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.LocalBroadcastManager;

import com.rockchips.mediacenter.bean.AllFileInfo;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.utils.APKUtils;

/**
 * @author GaoFei
 * APK图标加载线程
 */
public class APKPreviewLoadThread extends Thread implements Comparable<APKPreviewLoadThread>{
	
	
private static final String TAG = "APKPreviewLoadThread";
    
    private AllFileInfo mAllFileInfo;
    private DeviceMonitorService mService;
    private int mPriority;
	
	public APKPreviewLoadThread(AllFileInfo fileInfo, DeviceMonitorService service, int priority){
		mAllFileInfo = fileInfo;
		mService =service;
		mPriority = priority;
	}
	
	@Override
	public void run() {
		Drawable apkDrawable = APKUtils.getApkIcon(mService, mAllFileInfo.getFile().getPath());
        if(apkDrawable != null){
            Bitmap previewBitmap = ((BitmapDrawable)apkDrawable).getBitmap();
            File cacheImageDirFile = new File(ConstData.CACHE_IMAGE_DIRECTORY);
            if(!cacheImageDirFile.exists())
                cacheImageDirFile.mkdirs();
            String savePath = cacheImageDirFile.getPath() + "/" + UUID.randomUUID().toString() + ".png";
            if(previewBitmap != null && BitmapUtils.saveBitmapToImage(previewBitmap, savePath, CompressFormat.PNG, 80)){
            	mAllFileInfo.setLoadPreview(true);
            	mAllFileInfo.setPriviewPhotoPath(savePath);
            	//发送广播
                Intent previewIntent = new Intent(ConstData.BroadCastMsg.REFRESH_APK_PREVIEW);
                previewIntent.putExtra(ConstData.IntentKey.EXTRA_ALL_FILE_INFO, mAllFileInfo);
                LocalBroadcastManager.getInstance(mService).sendBroadcast(previewIntent);
            }
        }
	}

	@Override
	public int compareTo(APKPreviewLoadThread o) {
		if(mPriority < o.mPriority)
			return -1;
		else if(mPriority == o.mPriority)
			return 0;
		return 1;
	}
}
