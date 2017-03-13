package com.rockchips.mediacenter.service;

import java.util.List;
import android.os.Bundle;
import android.util.Log;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.utils.StorageUtils;

/**
 * 设备初始化检测线程
 * @author GaoFei
 *
 */
public class DeviceInitCheckThread extends Thread{
	
	public static final String TAG = "DeviceInitCheckThread";
	private DeviceMonitorService mService;
	
	public DeviceInitCheckThread(DeviceMonitorService service){
		mService = service;
	}
	
	@Override
	public void run() {
		Log.i(TAG, "DeviceInitCheckThread->startTime:" + System.currentTimeMillis());
		//内部存储路径
		String internelStoragePath = StorageUtils.getFlashStoragePath();
		List<String> allUsbPaths = StorageUtils.getUSBPaths(mService);
		List<String> allSdCardPaths = StorageUtils.getSdCardPaths(mService);
		Log.i(TAG, "DeviceInitCheckThread->allUsbPaths:" + allUsbPaths);
		Log.i(TAG, "DeviceInitCheckThread->allSdCardPaths:" + allSdCardPaths);
		Log.i(TAG, "DeviceInitCheckThread->internelStoragePath:" + internelStoragePath);
		Bundle mountBundle = null;
		//服务重启，添加USB设备
		if(allUsbPaths != null && allUsbPaths.size() > 0){
			for(String usbPath : allUsbPaths){
				mountBundle = new Bundle();
				mountBundle.putString(ConstData.DeviceMountMsg.MOUNT_PATH, usbPath);
				mountBundle.putInt(ConstData.DeviceMountMsg.MOUNT_STATE, ConstData.DeviceMountState.DEVICE_UP);
				mountBundle.putInt(ConstData.DeviceMountMsg.MOUNT_TYPE, ConstData.DeviceType.DEVICE_TYPE_U);
				mountBundle.putBoolean(ConstData.DeviceMountMsg.IS_FROM_NETWORK, false);
				mountBundle.putString(ConstData.DeviceMountMsg.NETWORK_PATH, "");
				mService.getDeviceMountService().execute(new DeviceMountThread(mService, mountBundle));
			}
		}
		//添加SD卡
		if(allSdCardPaths != null && allSdCardPaths.size() > 0){
			for(String sdCardPath : allSdCardPaths){
				mountBundle = new Bundle();
				mountBundle.putString(ConstData.DeviceMountMsg.MOUNT_PATH, sdCardPath);
				mountBundle.putInt(ConstData.DeviceMountMsg.MOUNT_STATE, ConstData.DeviceMountState.DEVICE_UP);
				mountBundle.putInt(ConstData.DeviceMountMsg.MOUNT_TYPE, ConstData.DeviceType.DEVICE_TYPE_SD);
				mountBundle.putBoolean(ConstData.DeviceMountMsg.IS_FROM_NETWORK, false);
				mountBundle.putString(ConstData.DeviceMountMsg.NETWORK_PATH, "");
				mService.getDeviceMountService().execute(new DeviceMountThread(mService, mountBundle));
			}
		}
		mountBundle = new Bundle();
		mountBundle.putString(ConstData.DeviceMountMsg.MOUNT_PATH, internelStoragePath);
		mountBundle.putInt(ConstData.DeviceMountMsg.MOUNT_STATE, ConstData.DeviceMountState.DEVICE_UP);
		mountBundle.putInt(ConstData.DeviceMountMsg.MOUNT_TYPE, ConstData.DeviceType.DEVICE_TYPE_INTERNEL_STORAGE);
		mountBundle.putBoolean(ConstData.DeviceMountMsg.IS_FROM_NETWORK, false);
		mountBundle.putString(ConstData.DeviceMountMsg.NETWORK_PATH, "");
		//添加内部存储
		mService.getDeviceMountService().execute(new DeviceMountThread(mService, mountBundle));
		//启动网络监测线程
		mService.getNetworkCheckService().execute(new NetWorkCheckThread(mService));
		Log.i(TAG, "DeviceInitCheckThread->endTime:" + System.currentTimeMillis());
	}
}
