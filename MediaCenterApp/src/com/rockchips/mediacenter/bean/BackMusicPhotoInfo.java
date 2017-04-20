package com.rockchips.mediacenter.bean;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * @author GaoFei
 * 音乐播放背景图片信息
 */
@Table(name="BackMusicPhotoInfo")
public class BackMusicPhotoInfo {
	@Column(name="id", autoGen=true, isId=true)
	private int id;
	@Column(name="path")
	private String path;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	@Override
	public String toString() {
		return "BackMusicPhotoInfo [id=" + id + ", path=" + path + "]";
	}
	
}
