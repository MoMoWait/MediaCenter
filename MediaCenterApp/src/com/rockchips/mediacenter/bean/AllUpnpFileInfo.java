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
	/**
	 * 对应图片,音频，视频的预览图路径，存储在本地
	 */
	private String previewPath;
	/**
	 * 是否已经加载过预览图
	 */
	private boolean isLoadPreview;
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
	public String getPreviewPath() {
		return previewPath;
	}
	public void setPreviewPath(String previewPath) {
		this.previewPath = previewPath;
	}
	public boolean isLoadPreview() {
		return isLoadPreview;
	}
	public void setLoadPreview(boolean isLoadPreview) {
		this.isLoadPreview = isLoadPreview;
	}
	
}
