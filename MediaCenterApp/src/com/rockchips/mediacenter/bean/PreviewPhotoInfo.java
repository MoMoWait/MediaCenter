/**
 * 
 */
package com.rockchips.mediacenter.bean;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * @author GaoFei
 * 预览图缓存文件信息
 */
@Table(name = "PreviewPhotoInfo")
public class PreviewPhotoInfo {
	@Column(name = "id", isId = true, autoGen = true)
	private int id;
	@Column(name = "deviceID")
	private String deviceID;
	@Column(name = "previewPath")
	private String previewPath;
	@Column(name = "originPath")
	private String originPath;
	@Column(name = "duration")
	private String duration;
	@Column(name = "bigPhotoPath")
	private String bigPhotoPath;
	//以json格式数据存储
	@Column(name = "ohterInfo")
	private String ohterInfo;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getDeviceID() {
		return deviceID;
	}
	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}
	public String getPreviewPath() {
		return previewPath;
	}
	public void setPreviewPath(String previewPath) {
		this.previewPath = previewPath;
	}
	public String getOriginPath() {
		return originPath;
	}
	public void setOriginPath(String originPath) {
		this.originPath = originPath;
	}
	public String getDuration() {
		return duration;
	}
	public void setDuration(String duration) {
		this.duration = duration;
	}
	public String getOhterInfo() {
		return ohterInfo;
	}
	public void setOhterInfo(String ohterInfo) {
		this.ohterInfo = ohterInfo;
	}
	public String getBigPhotoPath() {
		return bigPhotoPath;
	}
	public void setBigPhotoPath(String bigPhotoPath) {
		this.bigPhotoPath = bigPhotoPath;
	}
	@Override
	public String toString() {
		return "PreviewPhotoInfo [id=" + id + ", deviceID=" + deviceID
				+ ", previewPath=" + previewPath + ", originPath=" + originPath
				+ ", duration=" + duration + ", bigPhotoPath=" + bigPhotoPath
				+ ", ohterInfo=" + ohterInfo + "]";
	}
	
	
}
