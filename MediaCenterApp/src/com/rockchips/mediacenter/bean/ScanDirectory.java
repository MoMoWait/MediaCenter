package com.rockchips.mediacenter.bean;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;


/**
 * @author GaoFei
 * 扫描目录表
 */
@Table(name="ScanDirectory")
public class ScanDirectory {
	@Column(name = "directoryId", autoGen = true, isId = true)
	private int directoryId;
	@Column(name = "path")
	private String path;
	@Column(name = "deviceId")
	private String deviceId;
	public ScanDirectory(String path, String deviceId){
		this.path = path;
		this.deviceId = deviceId;
	}
	public ScanDirectory(){
		
	}
	public int getDirectoryId() {
		return directoryId;
	}
	public void setDirectoryId(int directoryId) {
		this.directoryId = directoryId;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	@Override
	public String toString() {
		return "ScanDirectory [directoryId=" + directoryId + ", path=" + path
				+ ", deviceId=" + deviceId + "]";
	}
	
	
}
