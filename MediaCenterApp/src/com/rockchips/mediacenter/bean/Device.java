package com.rockchips.mediacenter.bean;

import java.util.UUID;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * @author GaoFei
 * 设备描述信息，包括本地存储(内部存储，SD卡，U盘，移动硬盘)，网络存储(DLNA，Samba,NFS)
 */
@Table(name = "Device")
public class Device {
	/**
	 * 设备ID（唯一，UUID生成）
	 */
	@Column(name="deviceID", isId = true)
	private String deviceID = UUID.randomUUID().toString();
	/**
	 * 设备类型
	 */
	@Column(name="deviceType")
	private int deviceType;
	/**
	 * 本地挂载路径
	 */
	@Column(name="localMountPath")
	private String localMountPath;
	/**
	 * 设备名称，用于UI显示
	 */
	@Column(name="deviceName")
	private String deviceName;
	/**
	 * 网络路径，用于网络存储
	 */
	@Column(name="netWorkPath")
	private String netWorkPath;
	public String getDeviceID() {
		return deviceID;
	}
	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}
	public int getDeviceType() {
		return deviceType;
	}
	public void setDeviceType(int deviceType) {
		this.deviceType = deviceType;
	}
	public String getLocalMountPath() {
		return localMountPath;
	}
	public void setLocalMountPath(String localMountPath) {
		this.localMountPath = localMountPath;
	}
	public String getDeviceName() {
		return deviceName;
	}
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}
	public String getNetWorkPath() {
		return netWorkPath;
	}
	public void setNetWorkPath(String netWorkPath) {
		this.netWorkPath = netWorkPath;
	}
	@Override
	public String toString() {
		return "Device [deviceID=" + deviceID + ", deviceType=" + deviceType
				+ ", localMountPath=" + localMountPath + ", deviceName="
				+ deviceName + ", netWorkPath=" + netWorkPath + "]";
	}
	
}
