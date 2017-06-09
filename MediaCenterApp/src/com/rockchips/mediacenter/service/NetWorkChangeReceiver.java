package com.rockchips.mediacenter.service;
import com.rockchip.mediacenter.SystemDeviceService;
import com.rockchips.mediacenter.data.ConstData;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.os.Parcelable;
/**
 * @author GaoFei
 * 网络状态改变监听器
 */
public class NetWorkChangeReceiver extends BroadcastReceiver{
	
	public static final String TAG = "NetWorkChangeReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "onReceive->intent->action:" + intent.getAction());
		LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ConstData.BroadCastMsg.CHECK_NETWORK));
		Intent newIntent = new Intent(context, SystemDeviceService.class);
		newIntent.putExtra(ConnectivityManager.EXTRA_NETWORK_INFO, (Parcelable)intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO));
		newIntent.putExtra(SystemDeviceService.KEY_CMD, SystemDeviceService.CMD_CONN_CHANGED);
		context.startService(newIntent);
	}
}
