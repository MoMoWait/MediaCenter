package com.rockchips.mediacenter.localscan.application;

import android.app.Application;

/**
 * @author GaoFei
 * 本地引用实例子
 */
public class LocalScanApplication extends Application {
	
	private static LocalScanApplication mInstance;
	
	@Override
	public void onCreate() {
		super.onCreate();
		mInstance = this;
	}
	
	public static LocalScanApplication getInstance(){
		return mInstance;
	}
}
