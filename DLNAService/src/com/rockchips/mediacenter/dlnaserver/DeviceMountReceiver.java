/**
 * Title: DeviceMountReceiver.java<br>
 * Package: com.rockchips.mediacenter.dlnaserver<br>
 * Description: TODO<br>
 * @author c00224451
 * @version v1.0<br>
 * Date: 2014-7-29下午9:33:41<br> 
 * Copyright © Huawei Technologies Co., Ltd. 2014. All rights reserved.
 */

package com.rockchips.mediacenter.dlnaserver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.rockchips.android.airsharing.util.IICLOG;
import com.rockchips.mediacenter.basicutils.util.MountIntentUtil;

/**
 * Description: TODO<br>
 * @author c00224451
 * @version v1.0
 * Date: 2014-7-29 下午9:33:41<br>
 */

public class DeviceMountReceiver extends BroadcastReceiver
{
    private IICLOG mLog = IICLOG.getInstance();
    private static final String TAG = "DeviceMountReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent)
    {

     // 添加为null的判断
        if (intent == null || intent.getAction() == null)
        {
            return;
        }
        
        mLog.d(TAG, "cc msg LocDevServiceReceiver receive action " + intent.getAction());
        
        // 外设上下线消息处理，这里不能做耗时操作，防止丢掉广播消息，这里只是把相应的消息入队列
        if (MountIntentUtil.isDeviceIntent(intent))
        {
            DevicesMountQueue.getInstance().enqueue(intent);
        } 

    }

}
