package com.rockchips.mediacenter.bean;

import com.rockchips.mediacenter.data.ConstData;

/**
 * @author GaoFei
 * 设备扫描信息
 */
public class DeviceScanInfo {
	private int mountState;
	private int deviceType;
	private String mountPath;
	/**当前扫描状态*/
	private int scanStatus = ConstData.DeviceScanStatus.INITIAL;
	/**
	 * 网络路径
	 */
	private String netWrokPath;
	/**
	 * 设备名称
	 */
	private String deviceName;
	public DeviceScanInfo(){
		
	}
	
	public DeviceScanInfo(DeviceScanInfo otherScanInfo){
		
	}
	
	public int getMountState() {
		return mountState;
	}
	public void setMountState(int mountState) {
		this.mountState = mountState;
	}
	public int getDeviceType() {
		return deviceType;
	}
	public void setDeviceType(int deviceType) {
		this.deviceType = deviceType;
	}
	public String getMountPath() {
		return mountPath;
	}
	public void setMountPath(String mountPath) {
		this.mountPath = mountPath;
	}
	public int getScanStatus() {
		return scanStatus;
	}
	public void setScanStatus(int scanStatus) {
		this.scanStatus = scanStatus;
	}
	public String getNetWrokPath() {
		return netWrokPath;
	}
	public void setNetWrokPath(String netWrokPath) {
		this.netWrokPath = netWrokPath;
	}
	public String getDeviceName() {
		return deviceName;
	}
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}
	
}
