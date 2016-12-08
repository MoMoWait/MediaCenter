package com.rockchips.mediacenter.service;

import com.rockchips.mediacenter.data.ConstData;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * @author GaoFei
 * 网络状态改变监听器
 */
public class NetWorkChangeReceiver extends BroadcastReceiver{
	
	public static final String TAG = "NetWorkChangeReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "onReceive");
		Intent refershIntent = new Intent(ConstData.BroadCastMsg.REFRESH_NETWORK_DEVICE);
		LocalBroadcastManager.getInstance(context).sendBroadcast(refershIntent);
	}
}
