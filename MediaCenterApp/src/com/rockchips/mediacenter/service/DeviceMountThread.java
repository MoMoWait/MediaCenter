package com.rockchips.mediacenter.service;

import java.util.List;

import com.rockchips.mediacenter.bean.Device;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.modle.db.DeviceService;
import com.rockchips.mediacenter.modle.db.FileInfoService;
import com.rockchips.mediacenter.utils.MediaFileUtils;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * @author GaoFei
 * 设备挂载，卸载线程
 */
public class DeviceMountThread extends Thread{
	private static final String TAG = "DeviceMountThread";
	private DeviceMonitorService mService;
	private Bundle mMsg;
	public DeviceMountThread(DeviceMonitorService service, Bundle msg){
		mService = service;
		mMsg = msg;
	}
	
	@Override
	public void run() {
		Log.i(TAG, "DeviceMountThread->startTime:" + System.currentTimeMillis());
		int deviceType = mMsg.getInt(ConstData.DeviceMountMsg.MOUNT_TYPE);
		String mountPath = mMsg.getString(ConstData.DeviceMountMsg.MOUNT_PATH);
		int mountState = mMsg.getInt(ConstData.DeviceMountMsg.MOUNT_STATE);
		boolean isFromNetWork = mMsg.getBoolean(ConstData.DeviceMountMsg.IS_FROM_NETWORK);
		String netWrokPath = mMsg.getString(ConstData.DeviceMountMsg.NETWORK_PATH);
		Log.i(TAG, "DeviceMountThread->deviceType:" + deviceType);
		Log.i(TAG, "DeviceMountThread->mountPath:" + mountPath);
		Log.i(TAG, "DeviceMountThread->mountState:" + mountState);
		Log.i(TAG, "DeviceMountThread->isFromNetWork:" + mountState);
		Log.i(TAG, "DeviceMountThread->netWorkPath:" + netWrokPath);
		DeviceService deviceService = new DeviceService();
		FileInfoService fileInfoService = new FileInfoService();
		//删除相关数据
		List<Device> devices = deviceService.getDeviceByMountPath(mountPath);
		if(devices != null && devices.size() > 0){
			for(Device device : devices){
				deviceService.delete(device);
				fileInfoService.deleteFileInfosByDeviceID(device.getDeviceID());
			}
		}
		Intent broadIntent = new Intent();
		broadIntent.putExtra(ConstData.DeviceMountMsg.MOUNT_PATH, mountPath);
		broadIntent.putExtra(ConstData.DeviceMountMsg.MOUNT_TYPE, deviceType);
		broadIntent.putExtra(ConstData.DeviceMountMsg.MOUNT_STATE, mountState);
		broadIntent.putExtra(ConstData.DeviceMountMsg.IS_FROM_NETWORK, isFromNetWork);
		//设备上线
		if(mountState == ConstData.DeviceMountState.DEVICE_UP){
			broadIntent.setAction(ConstData.BroadCastMsg.DEVICE_UP);
			//通过路径构建Device
			Device mountDevice = MediaFileUtils.getDeviceFromMountPath(mountPath, netWrokPath, deviceType);
			//将设备存储至数据库中
			deviceService.save(mountDevice);
			//启动文件扫描线程
			mService.getFileScanService().execute(new FileScanThread(mService, mountDevice));
		}else{
			//设备下线
			broadIntent.setAction(ConstData.BroadCastMsg.DEVICE_DOWN);
		}
		
		//发送设备上下线广播
		LocalBroadcastManager.getInstance(mService).sendBroadcast(broadIntent);
		Log.i(TAG, "DeviceMountThread->endTime:" + System.currentTimeMillis());
	}
}
