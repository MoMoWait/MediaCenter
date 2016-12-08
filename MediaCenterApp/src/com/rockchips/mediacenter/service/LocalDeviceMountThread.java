package com.rockchips.mediacenter.service;

import java.util.List;

import android.os.Environment;
import android.util.Log;

import com.rockchips.mediacenter.bean.LocalDevice;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.modle.db.LocalDeviceService;
import com.rockchips.mediacenter.modle.db.LocalMediaFileService;
import com.rockchips.mediacenter.modle.db.LocalMediaFolderService;
import com.rockchips.mediacenter.util.StorageUtils;

/**
 * 本地存储设备挂载器
 * @author GaoFei
 *
 */
public class LocalDeviceMountThread extends Thread{
	
	public static final String TAG = LocalDeviceMountThread.class.getSimpleName();
	private DeviceMonitorService mService;
	
	public LocalDeviceMountThread(DeviceMonitorService service){
		mService = service;
	}
	
	@Override
	public void run() {
		//内部存储路径
		String internelStoragePath = StorageUtils.getFlashStoragePath();
		List<String> allUsbPaths = StorageUtils.getUSBPaths(mService);
		Log.i(TAG, "initStorage->allUsbPaths:" + allUsbPaths);
		List<String> allSdCardPaths = StorageUtils.getSdCardPaths(mService);
		Log.i(TAG, "initStorage->allSdCardPaths:" + allSdCardPaths);
		LocalDeviceService localDeviceService = new LocalDeviceService();
		LocalMediaFolderService localMediaFolderService = new LocalMediaFolderService();
		LocalMediaFileService localMediaFileService = new LocalMediaFileService();
		List<LocalDevice> devices = localDeviceService.getAllLocalStorageDevices();
		Log.i(TAG, "run->devices:" + devices);
		if(devices != null && devices.size() > 0){
			for(LocalDevice device : devices){
				localDeviceService.delete(device);
				localMediaFolderService.deleteFoldersByPhysicId(device.getPhysic_dev_id());
				localMediaFileService.deleteFilesByPhysicId(device.getPhysic_dev_id());
			}
		}
		//服务重启，添加USB设备
		if(allUsbPaths != null && allUsbPaths.size() > 0){
			for(String usbPath : allUsbPaths){
				mService.processMountMsg(usbPath, Environment.MEDIA_MOUNTED, ConstData.DeviceType.DEVICE_TYPE_U);
			}
		}
		//添加SD卡
		if(allSdCardPaths != null && allSdCardPaths.size() > 0){
			for(String sdCardPath : allSdCardPaths){
				mService.processMountMsg(sdCardPath, Environment.MEDIA_MOUNTED, ConstData.DeviceType.DEVICE_TYPE_SD);
			}
		}
		
		//添加内部存储
		mService.processMountMsg(internelStoragePath, Environment.MEDIA_MOUNTED, ConstData.DeviceType.DEVICE_TYPE_INTERNEL_STORAGE);
	
	}
}
