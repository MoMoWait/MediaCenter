package com.rockchips.mediacenter.bean;
import java.io.File;
/**
 * @author GaoFei
 * 文件列表
 */
public class AllFileInfo {
	/**
	 * 文件相关信息
	 */
	private File file;
	/**
	 * 文件类型
	 */
	private int type;
	/**
	 * 视频或音频
	 * 时长xx:xx:xx
	 */
	private String duration;
	/**
	 * 视频或音频缩列图路径
	 */
	private String priviewPhotoPath;
	/**
	 * 是否已经加载过预览内容
	 */
	private boolean isLoadPreview;
	public File getFile() {
		return file;
	}
	public void setFile(File file) {
		this.file = file;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getDuration() {
		return duration;
	}
	public void setDuration(String duration) {
		this.duration = duration;
	}
	public String getPriviewPhotoPath() {
		return priviewPhotoPath;
	}
	public boolean isLoadPreview() {
		return isLoadPreview;
	}
	public void setLoadPreview(boolean isLoadPreview) {
		this.isLoadPreview = isLoadPreview;
	}
	public void setPriviewPhotoPath(String priviewPhotoPath) {
		this.priviewPhotoPath = priviewPhotoPath;
	}
	@Override
	public String toString() {
		return "AllFileInfo [file=" + file + ", type=" + type + ", duration="
				+ duration + ", priviewPhotoPath=" + priviewPhotoPath
				+ ", isLoadPreview=" + isLoadPreview + "]";
	}
	
	
}
