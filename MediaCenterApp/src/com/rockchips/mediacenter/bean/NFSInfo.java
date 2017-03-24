package com.rockchips.mediacenter.bean;
import java.io.Serializable;
import java.util.UUID;


/**
 * @author GaoFei
 * NFS信息
 */
public class NFSInfo implements Serializable{
	
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
	@Override
	public String toString() {
		return "NFSInfo [uniqueID=" + uniqueID + ", netWorkPath=" + netWorkPath
				+ ", localMountPath=" + localMountPath + "]";
	}
	
	@Override
	public boolean equals(Object obj) {
		if(localMountPath.equals(obj))
			return true;
		return super.equals(obj);
	}
}
