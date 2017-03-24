package com.rockchips.mediacenter.bean;

import java.io.Serializable;
import java.util.UUID;

/**
 * @author GaoFei
 * SMB信息
 */
public class SmbInfo implements Serializable{
	/**
	 * 唯一标识符
	 */
	private String uniqueID = UUID.randomUUID().toString();
	/**
	 * 网络路径
	 */
	private String netWorkPath = "";
	/**
	 * 本地挂载路径
	 */
	private String localMountPath = "";
	/**
	 * 验证用户名
	 */
	private String userName = "";
	/**
	 * 验证密码
	 */
	private String password = "";
	/**
	 * 是否是匿名
	 */
	private boolean isUnknowName;
	public String getUniqueID() {
		return uniqueID;
	}
	public void setUniqueID(String uniqueID) {
		this.uniqueID = uniqueID;
	}
	public String getNetWorkPath() {
		return netWorkPath;
	}
	public void setNetWorkPath(String netWorkPath) {
		this.netWorkPath = netWorkPath;
	}
	public String getLocalMountPath() {
		return localMountPath;
	}
	public void setLocalMountPath(String localMountPath) {
		this.localMountPath = localMountPath;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	public boolean isUnknowName() {
		return isUnknowName;
	}
	public void setUnknowName(boolean isUnknowName) {
		this.isUnknowName = isUnknowName;
	}
	/**
	 * 生成Mount命令
	 * @return
	 */
	public String genMountCommand(){
		return "";
	}
	
	@Override
	public String toString() {
		return "SmbInfo [uniqueID=" + uniqueID + ", netWorkPath=" + netWorkPath
				+ ", localMountPath=" + localMountPath + ", userName="
				+ userName + ", password=" + password + ", isUnknowName="
				+ isUnknowName + "]";
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null || !(obj instanceof SmbInfo))
			return false;
		SmbInfo other = (SmbInfo)obj;
		if(other.getLocalMountPath() == null)
			return false;
		return other.getLocalMountPath().equals(localMountPath);
	}
}
