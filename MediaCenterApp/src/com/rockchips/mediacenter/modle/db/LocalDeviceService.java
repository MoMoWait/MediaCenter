package com.rockchips.mediacenter.modle.db;

import java.util.List;

import org.xutils.db.sqlite.WhereBuilder;
import com.rockchips.mediacenter.application.MediaCenterApplication;
import com.rockchips.mediacenter.bean.LocalDevice;
import com.rockchips.mediacenter.data.ConstData;

/**
 * @author GaoFei
 *
 */
public class LocalDeviceService extends AppBeanService<LocalDevice>{

	@Override
	public boolean isExist(LocalDevice object) {
		LocalDevice device = getObjectById(LocalDevice.class, object.getDeviceID());
		return device != null;
	}
	
	/**
	 * 根据挂载路径删除设备
	 * @param mountPath
	 */
	public void deleteDeviceByPath(String mountPath){
		try{
			MediaCenterApplication.mDBManager.delete(LocalDevice.class, WhereBuilder.b("mountPath", "=", mountPath));
		}catch (Exception e){
			//MediaCenterApplication.class.getDeclaredMethod("a", String[].getClass());
		}
		
	}
	
	/**
	 * 根据挂载路径获取设备信息
	 * @param mountPath
	 */
	public LocalDevice getDeviceByPath(String mountPath){
		LocalDevice localDevice = null;
		try{
			localDevice = MediaCenterApplication.mDBManager.selector(LocalDevice.class).
			where("mountPath", "=", mountPath).findFirst();
		}catch(Exception e){
			
		}
		return localDevice;
	}
	
	/**
	 * 根据设备类型获取设备
	 * @param deviceType
	 * @return
	 */
	public List<LocalDevice> getDevicesByType(int deviceType){

		List<LocalDevice> devices = null;
		try{
			devices = MediaCenterApplication.mDBManager.selector(LocalDevice.class).
			where("devices_type", "=", deviceType).findAll();
		}catch(Exception e){
			
		}
		return devices;
	
	}
	
	/**
	 * 获取所有本地存储设备
	 * @return
	 */
	public List<LocalDevice> getAllLocalStorageDevices(){
		List<LocalDevice> allLocalDevices = null;
		try{
			allLocalDevices = MediaCenterApplication.mDBManager.selector(LocalDevice.class).
			where("devices_type", "=", ConstData.DeviceType.DEVICE_TYPE_SD).
			or("devices_type", "=", ConstData.DeviceType.DEVICE_TYPE_U).findAll();
		}catch(Exception e){
			
		}
		return allLocalDevices;
	}
	
	/**
	 * 获取所有的UPNP设备
	 * @return
	 */
	public List<LocalDevice> getAllUpnpDevices(){

		List<LocalDevice> allUpnpDevices = null;
		try{
			allUpnpDevices = MediaCenterApplication.mDBManager.selector(LocalDevice.class).
			where("devices_type", "=", ConstData.DeviceType.DEVICE_TYPE_DMS).findAll();
		}catch(Exception e){
			
		}
		return allUpnpDevices;
	
	}
}
