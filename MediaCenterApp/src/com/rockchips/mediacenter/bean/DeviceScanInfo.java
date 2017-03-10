package com.rockchips.mediacenter.bean;

/**
 * @author GaoFei
 * 设备扫描信息
 */
public class DeviceScanInfo {
	private int mountState;
	private boolean needRescan;
	private int deviceType;
	private String mountPath;
	public int getMountState() {
		return mountState;
	}
	public void setMountState(int mountState) {
		this.mountState = mountState;
	}
	public boolean isNeedRescan() {
		return needRescan;
	}
	public void setNeedRescan(boolean needRescan) {
		this.needRescan = needRescan;
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
	
}
