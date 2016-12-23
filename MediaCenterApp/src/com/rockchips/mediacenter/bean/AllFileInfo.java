package com.rockchips.mediacenter.bean;
import java.io.File;

import android.graphics.Bitmap;

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
	 * 视频或音频缩列图
	 */
	private Bitmap bitmap;	
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
	public Bitmap getBitmap() {
		return bitmap;
	}
	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}
	
}
