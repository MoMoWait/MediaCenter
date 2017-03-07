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
	@Override
	public String toString() {
		return "PreviewPhotoInfo [id=" + id + ", deviceID=" + deviceID
				+ ", previewPath=" + previewPath + ", originPath=" + originPath
				+ "]";
	}
	
}
