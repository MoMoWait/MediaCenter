package com.rockchips.mediacenter.bean;

import java.io.Serializable;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

@Table(name="LocalMediaFile")
public class LocalMediaFile implements Serializable{
	@Column(isId = true, autoGen = true, name="fileId")
	private int fileId;
	@Column(name="name")
	private String name;
	@Column(name="path")
	private String path;
	@Column(name="type")
	private int type;
	@Column(name="last_modify_date")
	private long last_modify_date;
	@Column(name="size")
	private long size;
	@Column(name="pinyin")
	private String pinyin;
	@Column(name="devicetype")
	private int devicetype;
	@Column(name="physic_dev_id")
	private String physic_dev_id;
	@Column(name="deviceID")
	private String deviceID;
	@Column(name="parentPath")
	private String parentPath;
	@Column(name="duration")
	private String duration;
	@Column(name="previewPhotoPath")
	private String previewPhotoPath;
	/**
	 * 是否加载预览图
	 */
	@Column(name="isLoadPreviewPhoto")
	private boolean isLoadPreviewPhoto;
	public int getFileId() {
		return fileId;
	}
	public void setFileId(int fileId) {
		this.fileId = fileId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public long getLast_modify_date() {
		return last_modify_date;
	}
	public void setLast_modify_date(long last_modify_date) {
		this.last_modify_date = last_modify_date;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public String getPinyin() {
		return pinyin;
	}
	public void setPinyin(String pinyin) {
		this.pinyin = pinyin;
	}
	public int getDevicetype() {
		return devicetype;
	}
	public void setDevicetype(int devicetype) {
		this.devicetype = devicetype;
	}
	public String getPhysic_dev_id() {
		return physic_dev_id;
	}
	public void setPhysic_dev_id(String physic_dev_id) {
		this.physic_dev_id = physic_dev_id;
	}
	public String getDeviceID() {
		return deviceID;
	}
	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}
	
	public String getParentPath() {
		return parentPath;
	}
	
	public void setParentPath(String parentPath) {
		this.parentPath = parentPath;
	}
	
	public String getDuration() {
		return duration;
	}
	
	public void setDuration(String duration) {
		this.duration = duration;
	}
	
	public String getPreviewPhotoPath() {
		return previewPhotoPath;
	}
	
	public void setPreviewPhotoPath(String previewPhotoPath) {
		this.previewPhotoPath = previewPhotoPath;
	}
	
	public boolean isLoadPreviewPhoto() {
		return isLoadPreviewPhoto;
	}
	public void setLoadPreviewPhoto(boolean isLoadPreviewPhoto) {
		this.isLoadPreviewPhoto = isLoadPreviewPhoto;
	}
	@Override
	public String toString() {
		return "LocalMediaFile [fileId=" + fileId + ", name=" + name
				+ ", path=" + path + ", type=" + type + ", last_modify_date="
				+ last_modify_date + ", size=" + size + ", pinyin=" + pinyin
				+ ", devicetype=" + devicetype + ", physic_dev_id="
				+ physic_dev_id + ", deviceID=" + deviceID + ", parentPath="
				+ parentPath + ", duration=" + duration + ", previewPhotoPath="
				+ previewPhotoPath + ", isLoadPreviewPhoto="
				+ isLoadPreviewPhoto + "]";
	}
	
	
}
