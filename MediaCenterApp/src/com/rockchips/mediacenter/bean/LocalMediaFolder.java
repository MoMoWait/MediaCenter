package com.rockchips.mediacenter.bean;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

@Table(name = "LocalMediaFolder")
public class LocalMediaFolder {
	@Column(isId = true, autoGen = true, name="folderId")
	private int folderId;
	@Column(name="name")
	private String name;
	@Column(name="path")
	private String path;
	@Column(name="folders")
	private int folders;
	@Column(name="last_modify_date")
	private long last_modify_date;
	@Column(name="pinyin")
	private String pinyin;
	@Column(name="devicetype")
	private int devicetype;
	@Column(name="physic_dev_id")
	private String physic_dev_id;
	@Column(name="deviceID")
	private String deviceID;
	@Column(name="folderType")
	private int folderType;
	@Column(name="parentPath")
	private String parentPath;
	@Column(name = "fileCount")
	private int fileCount;
	public LocalMediaFolder(){
		
	}
	
	public LocalMediaFolder(LocalMediaFolder srcFolder){
		this.name = srcFolder.name;
		this.deviceID = srcFolder.deviceID;
		this.devicetype = srcFolder.devicetype;
		this.folders = srcFolder.folders;
		this.folderType = srcFolder.folderType;
		this.last_modify_date = srcFolder.last_modify_date;
		this.parentPath = srcFolder.parentPath;
		this.path = srcFolder.path;
		this.physic_dev_id = srcFolder.physic_dev_id;
		this.pinyin = srcFolder.pinyin;
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
	public int getFolderType() {
		return folderType;
	}
	public void setFolderType(int folderType) {
		this.folderType = folderType;
	}
	
	public String getParentPath() {
		return parentPath;
	}
	
	public void setParentPath(String parentPath) {
		this.parentPath = parentPath;
	}
	
	public int getFileCount() {
		return fileCount;
	}
	
	public void setFileCount(int fileCount) {
		this.fileCount = fileCount;
	}
	
	
	public int getFolderId() {
		return folderId;
	}
	
	public void setFolderId(int folderId) {
		this.folderId = folderId;
	}
	
	@Override
	public String toString() {
		return "LocalMediaFolder [folderId=" + folderId + ", name=" + name
				+ ", path=" + path + ", folders=" + folders
				+ ", last_modify_date=" + last_modify_date + ", pinyin="
				+ pinyin + ", devicetype=" + devicetype + ", physic_dev_id="
				+ physic_dev_id + ", deviceID=" + deviceID + ", folderType="
				+ folderType + ", parentPath=" + parentPath + ", fileCount="
				+ fileCount + "]";
	}

	@Override
	public boolean equals(Object o) {
		if(o == null)
			return false;
		if(!(o instanceof LocalMediaFolder))
			return false;
		LocalMediaFolder other = (LocalMediaFolder)o;
		return this.path.equals(other.path) && this.folderType == other.folderType;
	}
	
}
