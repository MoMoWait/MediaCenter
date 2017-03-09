/**
 * 
 */
package com.rockchips.mediacenter.bean;

/**
 * @author GaoFei
 * 设备扫描信息
 */
public class DeviceScanInfo {
	private int mountState;
	private boolean needRescan;
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
	
}
