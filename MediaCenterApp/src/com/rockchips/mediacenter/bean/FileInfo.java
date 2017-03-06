package com.rockchips.mediacenter.bean;

import java.io.Serializable;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * 
 * @author GaoFei
 * 文件描述信息
 */
@Table(name = "FileInfo")
public class FileInfo implements Serializable
{
	@Column(name = "id", isId = true, autoGen = true)
	private int id = -1;
	@Column(name = "deviceID")
	private String deviceID;
	@Column(name = "path")
	private String path;
	@Column(name = "parentPath")
	private String parentPath;
	@Column(name = "name")
	private String name;
	@Column(name = "modifyTime")
	private long modifyTime;
	@Column(name = "size")
	private long size;
	@Column(name = "type")
	private int type;
	@Column(name = "musicCount")
	private int musicCount;
	@Column(name = "videoCount")
	private int videoCount;
	@Column(name = "imageCount")
	private int imageCount;
	@Column(name = "previewPath")
	private String previewPath;
	@Column(name = "childCount")
	private int childCount;
	@Column(name = "duration")
	private String duration;
	@Column(name = "otherInfo")
	private String otherInfo;
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
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getModifyTime() {
		return modifyTime;
	}
	public void setModifyTime(long modifyTime) {
		this.modifyTime = modifyTime;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getMusicCount() {
		return musicCount;
	}
	public void setMusicCount(int musicCount) {
		this.musicCount = musicCount;
	}
	public int getVideoCount() {
		return videoCount;
	}
	public void setVideoCount(int videoCount) {
		this.videoCount = videoCount;
	}
	public int getImageCount() {
		return imageCount;
	}
	public void setImageCount(int imageCount) {
		this.imageCount = imageCount;
	}
	public String getPreviewPath() {
		return previewPath;
	}
	public void setPreviewPath(String previewPath) {
		this.previewPath = previewPath;
	}
	public String getOtherInfo() {
		return otherInfo;
	}
	public void setOtherInfo(String otherInfo) {
		this.otherInfo = otherInfo;
	}
	public int getChildCount() {
		return childCount;
	}
	public void setChildCount(int childCount) {
		this.childCount = childCount;
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
	@Override
	public String toString() {
		return "FileInfo [id=" + id + ", deviceID=" + deviceID + ", path="
				+ path + ", parentPath=" + parentPath + ", name=" + name
				+ ", modifyTime=" + modifyTime + ", size=" + size + ", type="
				+ type + ", musicCount=" + musicCount + ", videoCount="
				+ videoCount + ", imageCount=" + imageCount + ", previewPath="
				+ previewPath + ", childCount=" + childCount + ", duration="
				+ duration + ", otherInfo=" + otherInfo + "]";
	}
	
}
