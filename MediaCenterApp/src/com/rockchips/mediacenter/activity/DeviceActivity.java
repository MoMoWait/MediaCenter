/**
 * Title: DevListenerActivity.java<br>
 * Package: com.rockchips.mediacenter.activity<br>
 * Description: TODO<br>
 * @author w00190739
 * @version v1.0<br>
 * Date: 2014-8-8下午4:45:29<br> 
 * Copyright © Huawei Technologies Co., Ltd. 2014. All rights reserved.
 */

package com.rockchips.mediacenter.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.rockchips.mediacenter.basicutils.constant.Constant;
import com.rockchips.mediacenter.basicutils.util.IICLOG;

/**
 * Description: 监听本地及DLAN设备变化的Acitivity<br>
 * @author w00190739
 * @version v1.0 Date: 2014-8-8 下午4:45:29<br>
 */

public class DeviceActivity extends AppBaseActivity
{
    protected static final IICLOG Log = IICLOG.getInstance();

    private static final String TAG = "MediaCenterApp";

    public static final String ACTION_DEV_DOWN = "com.rockchips.mediacenter.action.device.down";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //registerDeviceChangeBroadcastReceiver();
    }

    @Override
    protected void onDestroy()
    {
        //unRegisterDeviceChangeBroadcastReceiver();
        super.onDestroy();
    }

    private DeviceChangeBroadcastReceiver mDeviceChangeBroadcastReceiver;

    private synchronized void registerDeviceChangeBroadcastReceiver()
    {
        if (null == mDeviceChangeBroadcastReceiver)
        {
            mDeviceChangeBroadcastReceiver = new DeviceChangeBroadcastReceiver();
            IntentFilter mIntentFilter = new IntentFilter();
            mIntentFilter.addAction(ACTION_DEV_DOWN);
            registerReceiver(mDeviceChangeBroadcastReceiver, mIntentFilter, Constant.MEDIACENTER_PERMISSION, null);
        }
    }

    private synchronized void unRegisterDeviceChangeBroadcastReceiver()
    {
        if (null == mDeviceChangeBroadcastReceiver)
        {
            return;
        }

        unregisterReceiver(mDeviceChangeBroadcastReceiver);
        mDeviceChangeBroadcastReceiver = null;
    }

    private class DeviceChangeBroadcastReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (ACTION_DEV_DOWN.equals(action))
            {
                Log.d(TAG, "mCurrDeviceInfo is Off Line");
                finish();
                return;
            }
        }
    }
}

