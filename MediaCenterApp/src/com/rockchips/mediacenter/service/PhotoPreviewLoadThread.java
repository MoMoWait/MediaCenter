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
import com.rockchips.mediacenter.bean.AllFileInfo;
import com.rockchips.mediacenter.data.ConstData;

/**
 * @author GaoFei
 * 加载图片预览图
 */
public class PhotoPreviewLoadThread extends Thread implements Comparable<PhotoPreviewLoadThread>{

    private static final String TAG = "PhotoPreviewLoadThread";
    private int mPriority;
    private AllFileInfo mAllFileInfo;
    private DeviceMonitorService mService;
    public PhotoPreviewLoadThread(AllFileInfo fileInfo, DeviceMonitorService service, int priority){
        mAllFileInfo = fileInfo;
        mService = service;
        mPriority = priority; 
    }
    
    @Override
    public void run() {
        if(mAllFileInfo.isLoadPreview())
            return;
        File cacheImageDirFile = new File(ConstData.CACHE_IMAGE_DIRECTORY);
        if(!cacheImageDirFile.exists())
            cacheImageDirFile.mkdirs();
        String savePath = cacheImageDirFile.getPath() + "/" + UUID.randomUUID().toString() + ".png";
        boolean isSaveSuccess = BitmapUtils.saveScaledBitmap(mAllFileInfo.getFile().getPath(), SizeUtils.dp2px(mService, 280), SizeUtils.dp2px(mService, 280), savePath, CompressFormat.PNG, 80);
        if(isSaveSuccess){
            mAllFileInfo.setLoadPreview(true);
            mAllFileInfo.setPriviewPhotoPath(savePath);
            //发送广播
            Intent previewIntent = new Intent(ConstData.BroadCastMsg.REFRESH_PHOTO_PREVIEW);
            previewIntent.putExtra(ConstData.IntentKey.EXTRA_ALL_FILE_INFO, mAllFileInfo);
            LocalBroadcastManager.getInstance(mService).sendBroadcast(previewIntent);
        }
            
    }

	@Override
	public int compareTo(PhotoPreviewLoadThread o) {
		if(mPriority < o.mPriority)
			return -1;
		else if(mPriority == o.mPriority)
			return 0;
		return 1;
	}
    
}
