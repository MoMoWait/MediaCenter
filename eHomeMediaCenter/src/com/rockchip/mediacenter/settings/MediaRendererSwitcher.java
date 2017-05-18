/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Filename:    MediaRendererMenu.java  
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2013-1-24 下午02:22:55  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2013-1-24      fxw         1.0         create
*******************************************************************/   


package com.rockchip.mediacenter.settings;

import com.rockchip.mediacenter.R;
import com.rockchip.mediacenter.SystemDeviceManager;
import com.rockchip.mediacenter.plugins.widget.SwitchPreference;

import android.app.Activity;

/**
 *
 * @author fxw
 * @since 1.0
 */
public class MediaRendererSwitcher {
	
	public static final int MENU_RENDER = 1;
	private SystemDeviceManager mSystemDeviceManager;
	private final SwitchPreference mRendererSwitch;
	private Activity mActivity;
	private boolean isHandling = true;
	
	public MediaRendererSwitcher(SwitchPreference rendererSwitch){
		mRendererSwitch = rendererSwitch;
		mRendererSwitch.setEnabled(false);
	}
	
	public void onCreate(Activity activity){
		mActivity = activity;
		mSystemDeviceManager = new SystemDeviceManager(mActivity);
		mSystemDeviceManager.setBindListener(new SystemDeviceManager.BindListener() {
			public void onBindCompleted() {
				isHandling = false;
				mRendererSwitch.setEnabled(true);
				if(mSystemDeviceManager.isRendererRunning()){
					mRendererSwitch.setSummary(R.string.settings_dmr_opened);
					mRendererSwitch.setChecked(true);
				}else{
					mRendererSwitch.setSummary(R.string.settings_dmr_closed);
					mRendererSwitch.setChecked(false);
				}
			}
		});
		mSystemDeviceManager.startManager();
	}
	
	public void onDestroy(){
		mSystemDeviceManager.stopManager();
	}
	
	
	/**
	 * 启动远程播放器
	 */
	public void startMediaRenderer(){
		if(isHandling) return;
		isHandling = true;
		mRendererSwitch.setEnabled(false);
		mRendererSwitch.setSummary(R.string.settings_dmr_opening);
		new Thread(){
			public void run() {
				mSystemDeviceManager.startMediaRenderer();
				mActivity.runOnUiThread(new Runnable(){
					public void run() {
						mRendererSwitch.setSummary(R.string.settings_dmr_opened);
						mRendererSwitch.setEnabled(true);
						isHandling = false;
					}
				});
			}
		}.start();
	}
	
	/**
	 * 停止远程播放器
	 */
	public void stopMediaRenderer(){
		if(isHandling) return;
		isHandling = true;
		mRendererSwitch.setEnabled(false);
		mRendererSwitch.setSummary(R.string.settings_dmr_closing);
		new Thread(){
			public void run() {
				mSystemDeviceManager.stopMediaRenderer();
				mActivity.runOnUiThread(new Runnable() {
					public void run() {
						mRendererSwitch.setSummary(R.string.settings_dmr_closed);
						mRendererSwitch.setEnabled(true);
						isHandling = false;
					}
				});
			}
		}.start();
	}
	
	public SwitchPreference getPreference(){
		return mRendererSwitch;
	}

}
