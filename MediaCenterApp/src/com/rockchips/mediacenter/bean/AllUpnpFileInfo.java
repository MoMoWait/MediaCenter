package com.rockchips.mediacenter.bean;
/**
 * @author GaoFei
 * Upnp文件列表
 */
public class AllUpnpFileInfo {
	/**
	 * Upnp目录或文件
	 */
	private Object file;
	/**
	 * 文件类型
	 */
	private int type;
	public Object getFile() {
		return file;
	}
	public void setFile(Object file) {
		this.file = file;
	}
	
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
}
