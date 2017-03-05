/**
 * 
 */
package com.rockchips.mediacenter.modle.db;

import java.util.List;

import org.xutils.ex.DbException;

import com.rockchips.mediacenter.application.MediaCenterApplication;
import com.rockchips.mediacenter.bean.Device;

/**
 * @author GaoFei
 * 设备服务，用于操作设备的增加，删除
 */
public class DeviceService extends AppBeanService<Device>{

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
		} catch (DbException e) {
			e.printStackTrace();
		}
		return devices;
	}
}
