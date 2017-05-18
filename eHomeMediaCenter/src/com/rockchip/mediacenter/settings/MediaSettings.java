/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Filename:    MediaSettings.java  
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2012-4-11 上午10:26:55  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2012-4-11      fxw         1.0         create
*******************************************************************/   


package com.rockchip.mediacenter.settings;


import com.rockchip.mediacenter.DLNAService;
import com.rockchip.mediacenter.IndexActivity;
import com.rockchip.mediacenter.R;
import com.rockchip.mediacenter.SystemDeviceService;
import com.rockchip.mediacenter.SystemSettingUtils;
import com.rockchip.mediacenter.common.logging.Log;
import com.rockchip.mediacenter.common.logging.LogFactory;
import com.rockchip.mediacenter.core.util.LocalStorageProvider;
import com.rockchip.mediacenter.plugins.widget.EditPreference;
import com.rockchip.mediacenter.plugins.widget.SwitchPreference;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

/**
 *
 * @author fxw
 * @since 1.0
 */
public class MediaSettings extends PreferenceActivity implements Preference.OnPreferenceChangeListener {
	public static Log logger = LogFactory.getLog(MediaSettings.class);
	private EditPreference mEditPreference;
	private SwitchPreference mUploadSwitchPreference;
	//将开关DMR和设置随机启动两项设置合二为一 
	//private SwitchPreference mAutoSwitchPreference;
	private MediaRendererSwitcher mMediaRendererSwitcher;
	
	/** 
	 * <p>Title: onCreate</p> 
	 * <p>Description: </p> 
	 * @param savedInstanceState 
	 * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle) 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.dlna_settings);
		setContentView(R.layout.dlna_settings);
		mEditPreference = (EditPreference)findPreference("SettingName");
		mUploadSwitchPreference = (SwitchPreference)findPreference("SettingPermission");
		//mAutoSwitchPreference = (SwitchPreference)findPreference("SettingAutoStart");
		mEditPreference.setOnPreferenceChangeListener(this);
		mUploadSwitchPreference.setOnPreferenceChangeListener(this);
		//mAutoSwitchPreference.setOnPreferenceChangeListener(this);
		findPreference("SettingUploadLocation").setSummary(LocalStorageProvider.getUploadLocalPath(this));
		findPreference("SettingVersion").setSummary(getVersionInfo());
		String serverName = SystemSettingUtils.getMediaServerName(this);
		mEditPreference.setText(serverName);
		//As a remote media playback device
		SwitchPreference mediaRenderSP = (SwitchPreference)findPreference("SettingMediaRenderer");
		mediaRenderSP.setOnPreferenceChangeListener(this);
		mMediaRendererSwitcher = new MediaRendererSwitcher(mediaRenderSP);
		mMediaRendererSwitcher.onCreate(this);
	}
	
	/** 
	 * <p>Title: onResume</p> 
	 * <p>Description: </p>  
	 * @see android.app.Activity#onResume() 
	 */
	@Override
	protected void onResume() {
		syncNameSummary(mEditPreference.getText());
		syncUploadSummary(mUploadSwitchPreference.isChecked());
		//syncAutoSummary(mAutoSwitchPreference.isChecked());
		super.onResume();
	}

	/** 
	 * <p>Title: onPreferenceChange</p> 
	 * <p>Description: </p> 
	 * @param preference
	 * @param obj
	 * @return 
	 * @see android.preference.Preference.OnPreferenceChangeListener#onPreferenceChange(android.preference.Preference, java.lang.Object) 
	 */
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if(newValue==null||"".equals(newValue)){
			return false;
		}
		Intent intent = new Intent();
		intent.setClass(this, SystemDeviceService.class);
		intent.putExtra(SystemDeviceService.KEY_CMD, SystemDeviceService.CMD_UPDATE_CONFIG);
		if(preference==mEditPreference){
			if(!mEditPreference.getText().equals(newValue)){
				syncNameSummary(newValue+"");
				intent.putExtra(SystemDeviceService.KEY_FRIEND_NAME, newValue+"");
				startService(intent);
				intent.setClass(this, DLNAService.class);
				startService(intent);
				SystemSettingUtils.saveDeviceNameForRemoteControl(this, newValue+"");
			}
		}else if(preference==mUploadSwitchPreference){
			Boolean value = (Boolean)newValue;
			if(mUploadSwitchPreference.isChecked()!=value){
				syncUploadSummary(value);
				intent.putExtra(SystemDeviceService.KEY_UPLOAD_PERMISSION, value);
				startService(intent);
			}
		/*}else if(preference==mAutoSwitchPreference){
			Boolean value = (Boolean)newValue;
			if(mAutoSwitchPreference.isChecked()!=value){
				syncAutoSummary(value);
				//startService(intent);
			}*/
		}else if(preference==mMediaRendererSwitcher.getPreference()){//As a remote media playback device
			Boolean value = (Boolean)newValue;
			if(mMediaRendererSwitcher.getPreference().isChecked()!=value){
				if(value){
					mMediaRendererSwitcher.startMediaRenderer();
					SystemSettingUtils.saveMediaRendererAutoable(this, true);
				}else{
					mMediaRendererSwitcher.stopMediaRenderer();
					SystemSettingUtils.saveMediaRendererAutoable(this, false);
				}
			}
		}
		return true;
	}
	
	/** 
	 * <p>Title: onBackPressed</p> 
	 * <p>Description: </p>  
	 * @see android.app.Activity#onBackPressed() 
	 */
	@Override
	public void onBackPressed() {
		Intent intent = new Intent(this, IndexActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}
	
	/** 
	 * <p>Title: onDestroy</p> 
	 */
	@Override
	protected void onDestroy() {
		mMediaRendererSwitcher.onDestroy();
		super.onDestroy();
	}
	
	private void syncNameSummary(String value){
		mEditPreference.setSummary(value);
	}
	
	private void syncUploadSummary(boolean value){
		if(value){
			mUploadSwitchPreference.setSummary(R.string.settings_upload_allow);
		}else{
			mUploadSwitchPreference.setSummary(R.string.settings_upload_reject);
		}
	}
	
	/*
	private void syncAutoSummary(boolean value){
		if(value){
			mAutoSwitchPreference.setSummary(R.string.settings_auto_yes);
		}else{
			mAutoSwitchPreference.setSummary(R.string.settings_auto_no);
		}
	}*/
	
	/**
	 * 获取版本信息
	 */
	private String getVersionInfo(){
		try {
			PackageInfo packInfo = getPackageManager().getPackageInfo(this.getPackageName(), 0);
			String appName = getString(R.string.app_name);
			return appName+" "+packInfo.versionName;
		} catch (NameNotFoundException e) {
			return "Unknow";
		}
	}
	
}
