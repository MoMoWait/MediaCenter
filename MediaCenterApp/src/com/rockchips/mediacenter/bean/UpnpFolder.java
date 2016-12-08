package com.rockchips.mediacenter.bean;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import android.R.string;

/**
 * @author GaoFei
 * Unnp设备下文件夹描述
 */
@Table(name="UpnpFolder")
public class UpnpFolder {
	@Column(isId = true, autoGen = true, name="folderId")
	private int folderId;
	@Column(name="itmeId")
	private String itmeId;
	/**
	 * 对应于IDILObject下的title
	 */
	@Column(name="name")
	private String name;
	/**
	 * 这里的path表示网络路径
	 */
	@Column(name="path")
	private String path;
	@Column(name="last_modify_date")
	private long last_modify_date;
	@Column(name="devicetype")
	private int devicetype;
	@Column(name="physic_dev_id")
	private String physic_dev_id;
	@Column(name="deviceID")
	private String deviceID;
	@Column(name = "fileCount")
	private int fileCount;
	@Column(name = "imageCount")
	private int imageCount;
	@Column(name = "videoCount")
	private int videoCount;
	@Column(name = "audioCount")
	private int audioCount;
	@Column(name = "parentId")
	private String parentId;
	/**
	 * 第一张图片的URL,用于相册列表展示
	 */
	@Column(name = "firstPhotoUrl")
	private String firstPhotoUrl;
	
	public int getFolderId() {
		return folderId;
	}
	public void setFolderId(int folderId) {
		this.folderId = folderId;
	}
	public String getItmeId() {
		return itmeId;
	}
	public void setItmeId(String itmeId) {
		this.itmeId = itmeId;
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
	public long getLast_modify_date() {
		return last_modify_date;
	}
	public void setLast_modify_date(long last_modify_date) {
		this.last_modify_date = last_modify_date;
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
	
	public int getFileCount() {
		return fileCount;
	}
	public void setFileCount(int fileCount) {
		this.fileCount = fileCount;
	}
	public String getParentId() {
		return parentId;
	}
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}
	
	public int getImageCount() {
		return imageCount;
	}
	public void setImageCount(int imageCount) {
		this.imageCount = imageCount;
	}
	public int getVideoCount() {
		return videoCount;
	}
	public void setVideoCount(int videoCount) {
		this.videoCount = videoCount;
	}
	public int getAudioCount() {
		return audioCount;
	}
	public void setAudioCount(int audioCount) {
		this.audioCount = audioCount;
	}
	public String getFirstPhotoUrl() {
		return firstPhotoUrl;
	}
	public void setFirstPhotoUrl(String firstPhotoUrl) {
		this.firstPhotoUrl = firstPhotoUrl;
	}
	@Override
	public String toString() {
		return "UpnpFolder [folderId=" + folderId + ", itmeId=" + itmeId
				+ ", name=" + name + ", path=" + path + ", last_modify_date="
				+ last_modify_date + ", devicetype=" + devicetype
				+ ", physic_dev_id=" + physic_dev_id + ", deviceID=" + deviceID
				+ ", fileCount=" + fileCount + ", imageCount=" + imageCount
				+ ", videoCount=" + videoCount + ", audioCount=" + audioCount
				+ ", parentId=" + parentId + "]";
	}
	
	
	
}
