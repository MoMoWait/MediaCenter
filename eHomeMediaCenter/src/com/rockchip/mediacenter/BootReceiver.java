/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Filename:    BootReceiver.java  
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2012-4-23 上午09:13:38  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2012-4-6      fxw         1.0         create
*******************************************************************/   
package com.rockchip.mediacenter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Parcelable;
public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if(Intent.ACTION_BOOT_COMPLETED.equals(action)){
			if(SystemSettingUtils.getMediaRendererAutoable(context)){
				Intent newIntent = new Intent(context, SystemDeviceService.class);
				//newIntent.putExtra(SystemDeviceService.KEY_CMD, SystemDeviceService.CMD_START_RENDER);
				context.startService(newIntent);
			}
		}else if(ConnectivityManager.CONNECTIVITY_ACTION.equals(action)){
			if(SystemSettingUtils.getMediaRendererAutoable(context)){
				Intent newIntent = new Intent(context, SystemDeviceService.class);
				newIntent.putExtra(ConnectivityManager.EXTRA_NETWORK_INFO, (Parcelable)intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO));
				newIntent.putExtra(SystemDeviceService.KEY_CMD, SystemDeviceService.CMD_CONN_CHANGED);
				context.startService(newIntent);
			}
		}
	}
	
}
