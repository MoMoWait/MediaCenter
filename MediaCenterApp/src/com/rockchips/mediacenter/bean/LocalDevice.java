/**
 * Title: DeviceInfo.java<br>
 * Package: com.rockchips.mediacenter.localscan.base<br>
 * Description: TODO<br>
 * @author c00224451
 * @version v1.0<br>
 * Date: 2014-7-1下午2:07:16<br> 
 * Copyright © Huawei Technologies Co., Ltd. 2014. All rights reserved.
 */

package com.rockchips.mediacenter.bean;

import java.io.Serializable;
import java.util.UUID;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import com.rockchips.mediacenter.data.ConstData;

import android.os.Bundle;


/**
 * Description: 和设备表对应的本地设备信息结构<br>
 * @author c00224451
 * @version v1.0 Date: 2014-7-1 下午2:07:16<br>
 */
@Table(name="LocalDevice")
public class LocalDevice implements Serializable
{
	@Column(isId = true, name="deviceID")
	private String deviceID = UUID.randomUUID().toString();
	@Column(name="devices_type")
	private int devices_type;
	@Column(name="size")
	private String size;
	@Column(name="used")
	private String used;
	@Column(name="free")
	private String free;
	@Column(name="physic_dev_id")
	private String physic_dev_id;
	@Column(name="mountPath")
	private String mountPath;
	@Column(name="has_scaned")
	private boolean has_scaned;
	/**
	 * 扫描状态
	 */
	@Column(name="scanStatus")
	private int scanStatus = ConstData.DeviceScanStatus.INITIAL;
	public int getDevices_type() {
		return devices_type;
	}
	public void setDevices_type(int devices_type) {
		this.devices_type = devices_type;
	}
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
	public String getUsed() {
		return used;
	}
	public void setUsed(String used) {
		this.used = used;
	}
	public String getFree() {
		return free;
	}
	public void setFree(String free) {
		this.free = free;
	}
	public String getPhysic_dev_id() {
		return physic_dev_id;
	}
	public void setPhysic_dev_id(String physic_dev_id) {
		this.physic_dev_id = physic_dev_id;
	}
	public String getMountPath() {
		return mountPath;
	}
	public void setMountPath(String mountPath) {
		this.mountPath = mountPath;
	}
	public boolean isHas_scaned() {
		return has_scaned;
	}
	public void setHas_scaned(boolean has_scaned) {
		this.has_scaned = has_scaned;
	}
	public String getDeviceID() {
		return deviceID;
	}
	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}
	public int getScanStatus() {
		return scanStatus;
	}
	public void setScanStatus(int scanStatus) {
		this.scanStatus = scanStatus;
	}
	@Override
	public String toString() {
		return "LocalDevice [deviceID=" + deviceID + ", devices_type="
				+ devices_type + ", size=" + size + ", used=" + used
				+ ", free=" + free + ", physic_dev_id=" + physic_dev_id
				+ ", mountPath=" + mountPath + ", has_scaned=" + has_scaned
				+ ", scanStatus=" + scanStatus + "]";
	}
	
	
}
