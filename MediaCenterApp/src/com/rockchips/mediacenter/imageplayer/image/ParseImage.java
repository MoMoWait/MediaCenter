package com.rockchips.mediacenter.imageplayer.image;

import java.lang.ref.SoftReference;

import android.graphics.drawable.Drawable;

public class ParseImage {
	/**
	 * 图片路径
	 */
	private String mUrl;
	/**
	 * 图片
	 */
	private SoftReference<Drawable> mSrfBitmap;
	/**
	 * 图片旋转角度
	 */
	private int mDegree;
	
	public ParseImage() {

	}
	public String getUrl() {
		return mUrl;
	}
	public void setUrl(String mUrl) {
		this.mUrl = mUrl;
	}
	public SoftReference<Drawable> getSrfBitmap() {
		return mSrfBitmap;
	}
	public void setSrfBitmap(SoftReference<Drawable> mSrfBitmap) {
		this.mSrfBitmap = mSrfBitmap;
	}
	public int getDegree() {
		return mDegree;
	}
	public void setDegree(int mDegree) {
		this.mDegree = mDegree;
	}

	
}
