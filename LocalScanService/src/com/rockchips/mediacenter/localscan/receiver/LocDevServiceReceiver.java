package com.rockchips.mediacenter.localscan.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.rockchips.mediacenter.basicutils.util.IICLOG;
import com.rockchips.mediacenter.basicutils.util.MountIntentUtil;
import com.rockchips.mediacenter.localscan.database.LocalDBHelper;
import com.rockchips.mediacenter.localscan.devicemgr.LocDevicesQueue;

/**
 * 
 * 广播接收器
 * 主要接受处理外设的上下线
 * 
 * @author  l00174030
 * @version  [2013-1-10]
 */
public class LocDevServiceReceiver extends BroadcastReceiver
{    
    private static final String TAG = "LocalScanService";
    private IICLOG mLog = IICLOG.getInstance();
       
    @Override
    public void onReceive(final Context context, Intent intent)
    {
        // 添加为null的判断
        if (intent == null || intent.getAction() == null)
        {
            return;
        }
        mLog.d(TAG, "db dir path:" + LocalDBHelper.getDbDirPath());
        mLog.d(TAG, "cc msg LocDevServiceReceiver receive action " + intent.getAction());
        
    	// 外设上下线消息处理，这里不能做耗时操作，防止丢掉广播消息，这里只是把相应的消息入队列
        if (MountIntentUtil.isDeviceIntent(intent))
        {
            LocDevicesQueue.getInstance().enqueue(intent);
        }        
    }
}
