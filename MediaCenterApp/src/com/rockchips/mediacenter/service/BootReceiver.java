package com.rockchips.mediacenter.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * @author GaoFei
 * 开机监听器
 */
public class BootReceiver extends BroadcastReceiver {
	public static final String TAG = "BootReceiver";
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "onReceive");
		Intent deviceMonitorIntent = new Intent(context, DeviceMonitorService.class);
		context.startService(deviceMonitorIntent);
	}

}
