/**
 * 
 */
package com.rockchips.mediacenter.modle.db;

import java.util.List;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;

import android.util.Log;
import com.rockchips.mediacenter.application.MediaCenterApplication;
import com.rockchips.mediacenter.bean.Device;
import com.rockchips.mediacenter.data.ConstData;

/**
 * @author GaoFei
 * 设备服务，用于操作设备的增加，删除
 */
public class DeviceService extends AppBeanService<Device>{
	
	private static final String TAG = "DeviceService";
	
	@Override
	public boolean isExist(Device object) {
		return false;
	}
	
	/**
	 * 通过挂载路径获取设备
	 * @param mountPath
	 * @return
	 */
	public List<Device> getDeviceByMountPath(String mountPath){
		List<Device> devices = null;
		try {
			devices = MediaCenterApplication.mDBManager.selector(Device.class).where("localMountPath", "=", mountPath).findAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return devices;
	}
	
	/**
	 * 获取所有网络设备
	 * @return
	 */
	public List<Device> getAllNetWorkDevices(){
		List<Device> devices = null;
		try {
			devices = MediaCenterApplication.mDBManager.selector(Device.class).
					where(WhereBuilder.b("deviceType", "=", ConstData.DeviceType.DEVICE_TYPE_DMS).
							or("deviceType", "=", ConstData.DeviceType.DEVICE_TYPE_NFS).
							or("deviceType", "=", ConstData.DeviceType.DEVICE_TYPE_SMB)).findAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return devices;
	}
	
	
	/**
	 * 获取所有本地设备(USB,SD,内部存储)
	 * @return
	 */
	public List<Device> getAllLocalDevices(){
		List<Device> devices = null;
		try {
			devices = MediaCenterApplication.mDBManager.selector(Device.class).
					where(WhereBuilder.b("deviceType", "=", ConstData.DeviceType.DEVICE_TYPE_INTERNEL_STORAGE).
							or("deviceType", "=", ConstData.DeviceType.DEVICE_TYPE_U).
							or("deviceType", "=", ConstData.DeviceType.DEVICE_TYPE_SD)).findAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return devices;
	
	}
	
	/**
	 * 删除设备列表
	 * @param devices
	 */
	public void deleteAll(List<Device> devices){
		try {
			MediaCenterApplication.mDBManager.delete(devices);
		} catch (Exception e) {
			Log.e(TAG, "deleteAll->devices:" + devices);
			e.printStackTrace();
		}
	}
	
	public void deleteAll(){
		try {
			MediaCenterApplication.mDBManager.delete(Device.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void deleteAll(int deviceType){
		try {
			MediaCenterApplication.mDBManager.delete(Device.class, WhereBuilder.b("deviceType", "=", deviceType));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//获取已经排序的设备
	public List<Device> getAllorderDevices(){
		List<Device> devices = null;
		try {
			devices = MediaCenterApplication.mDBManager.selector(Device.class).orderBy("deviceType").findAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return devices;
	}
	/**
	 * 获取所有的已经添加Smb或NFS设备
	 * @return
	 */
	public List<Device> getAllSmbOrNFSDevices(){
		List<Device> devices = null;
		try {
			devices = MediaCenterApplication.mDBManager.selector(Device.class).
					where(WhereBuilder.b("deviceType", "=", ConstData.DeviceType.DEVICE_TYPE_SMB).or("deviceType", "=", ConstData.DeviceType.DEVICE_TYPE_NFS))
					.findAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return devices;
	}
}
