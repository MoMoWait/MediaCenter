/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Filename:    SystemSettingUtils.java  
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2012-4-11 下午10:09:53  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2012-4-11      fxw         1.0         create
*******************************************************************/   


package com.rockchip.mediacenter;

import java.util.UUID;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.rockchip.mediacenter.R;
import com.rockchip.mediacenter.common.logging.Log;
import com.rockchip.mediacenter.common.logging.LogFactory;
import com.rockchip.mediacenter.common.util.ReflectionUtils;
import com.rockchip.mediacenter.common.util.StringUtils;
import com.rockchip.mediacenter.core.constants.DLNAConst;
import com.rockchip.mediacenter.core.dlna.enumeration.UploadPermission;
import com.rockchip.mediacenter.mediaserver.MediaShareType;

/**
 * 
 *  在android/device/rockchip/rkxxxx/device.mk添加属性配置
 *	ro.dlna.devicename   配置默认设备名称
 *  ro.dlna.startserver  配置默认启动媒体共享 true/false, 不配置默认不会随机启动
 *  ro.dlna.startrender  配置默认启动Renderer true/false, 不配置默认也会随机启动
 *  
 *  e.g:
 *  
 *  PRODUCT_PROPERTY_OVERRIDES += \
 *			  ro.dlna.devicename = rockchip \
 *			  ro.dlna.startserver = true \
 *			  ro.dlna.startrender = true
 *
 *
 * @author fxw
 * @since 1.0
 */
public class SystemSettingUtils {
	
	public static Log logger = LogFactory.getLog(SystemSettingUtils.class);
	
	private static final boolean DLNA_CERTIFIED = DLNAConst.DLNA_CERTIFIED_TEST;
	public static final String KEY_SHARE_TYPE = "MediaShareType";
	public static final String KEY_SERVER_STATE = "MediaServerState";
	public static final String KEY_SERVER_NAME = "SettingName";
	public static final String KEY_UPLOAD_PERMISSION = "SettingPermission";
	public static final String KEY_AUTO_START = "SettingAutoStart";

	/**
	 * 保存共享方式
	 * @param context
	 * @param type
	 */
	public static void saveMediaShareType(Context context, MediaShareType type){
		SharedPreferences pref = getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = pref.edit();
		editor.putInt(KEY_SHARE_TYPE, type==null?-1:type.getId());
		editor.commit();
	}
	
	/**
	 * 获取共享方式
	 * @param context
	 * @return
	 */
	public static MediaShareType getMediaShareType(Context context){
		SharedPreferences pref = getDefaultSharedPreferences(context);
		int type = pref.getInt(KEY_SHARE_TYPE, MediaShareType.MEDIA_SHARE.getId());//改用默认使用媒体库共享
		return MediaShareType.getById(type);
	}
	
	private static SharedPreferences getDefaultSharedPreferences(Context context){
		if(Build.VERSION.SDK_INT<=Build.VERSION_CODES.GINGERBREAD){
			return PreferenceManager.getDefaultSharedPreferences(context);
		}
		String defaultSpName = context.getPackageName() + "_preferences";
		int mode = Context.MODE_PRIVATE;
		int mutilProcess = (Integer)ReflectionUtils.getStaticFieldValue("android.content.Context", "MODE_MULTI_PROCESS");
		if(mutilProcess>0){
			mode = mode | mutilProcess;
		}
		return context.getSharedPreferences(defaultSpName, mode);
	}
	
	/**
	 * 退出前保存下当前媒体服务器是否处于开启状态
	 * 如果开启, 下次进入的时候自动开启
	 * @param context
	 * @param isStarted
	 */
	public static void saveMediaServerState(Context context, boolean isStarted){
		SharedPreferences pref = getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = pref.edit();
		editor.putBoolean(KEY_SERVER_STATE, isStarted);
		editor.commit();
	}
	
	/**
	 * 获取上次保存的媒体服务器状态
	 * @param context
	 * @return
	 */
	public static boolean getMediaServerState(Context context){
		SharedPreferences pref = getDefaultSharedPreferences(context);
		return pref.getBoolean(KEY_SERVER_STATE, false);
	}
	
	/**
	 * 保存设备名称
	 * @param context
	 * @return
	 */
	public static void saveMediaServerName(Context context, String friendlyName){
		SharedPreferences pref = getDefaultSharedPreferences(context);
		pref.edit().putString("SettingName", friendlyName).commit();
		saveDeviceNameForRemoteControl(context, friendlyName);
	}
	
	/**
	 * 获取媒体服务器名称
	 * @param context
	 * @return
	 */
	public static String getMediaServerName(Context context){
		SharedPreferences pref = getDefaultSharedPreferences(context);
		String serverName = pref.getString(KEY_SERVER_NAME, "");
		if(!TextUtils.isEmpty(serverName)){
			return serverName;
		}
		serverName = UUID.randomUUID().toString();
		serverName = serverName.substring(serverName.length() - 8, serverName.length());
		/*if(StringUtils.isEmptyObj(defaultName)){//未在系统中配置默认名称
			defaultName = context.getString(R.string.device_name);
		}*/
		serverName = "MediaCenter-" + serverName;
		pref.edit().putString(KEY_SERVER_NAME, serverName).commit();
		return serverName;
	}
	
	/**
	 * 获取默认的媒体服务器名称
	 * @param context
	 * @return
	 */
	public static String getDefaultMediaServerName(Context context){
		SharedPreferences pref = getDefaultSharedPreferences(context);
		String defaultName = pref.getString("DefaultServerName", "");
		if(StringUtils.isEmptyObj(defaultName)){
			//defaultName = (String)ReflectionUtils.invokeStaticMethod("android.os.SystemProperties", "get", "ro.dlna.devicename");
			String serverName = UUID.randomUUID().toString();
			serverName = serverName.substring(serverName.length() - 8, serverName.length());
			/*if(StringUtils.isEmptyObj(defaultName)){//未在系统中配置默认名称
				defaultName = context.getString(R.string.device_name);
			}*/
			defaultName = "MediaCenter-" + serverName;
			pref.edit().putString("DefaultServerName", defaultName).commit();
			saveDeviceNameForRemoteControl(context, defaultName);
		}
		return defaultName;
	}
	
	// 用于remote control 读取设备名称
	public static void saveDeviceNameForRemoteControl(Context context, String deviceName){
		SharedPreferences sp = context.getSharedPreferences("external", Context.MODE_WORLD_READABLE);
		sp.edit().putString("devicename", deviceName).commit();
		/*
		int mode = Context.MODE_WORLD_READABLE|Context.MODE_WORLD_WRITEABLE;
		int mutilProcess = (Integer)ReflectionUtils.getStaticFieldValue("android.content.Context", "MODE_MULTI_PROCESS");
		if(mutilProcess>0){
			mode = mode | mutilProcess;
		}*/
	}
	
	/**
	 * 获取媒体上传权限
	 * @param context
	 * @return
	 */
	public static UploadPermission getMediaUploadPermission(Context context){
		SharedPreferences pref = getDefaultSharedPreferences(context);
		boolean isAllow = pref.getBoolean(KEY_UPLOAD_PERMISSION, true);
		return isAllow?UploadPermission.PERMISSION_ALLOW:UploadPermission.PERMISSION_REJECT;
	}
	
	/**
	 * 获取Renderer device是否随机启动
	 * @param context
	 * @return
	 */
	public static boolean getMediaRendererAutoable(Context context){
		SharedPreferences pref = getDefaultSharedPreferences(context);
		/*
		if(!pref.contains(KEY_AUTO_START)){//用户还未进行配置，读取系统默认配置
			Object defRenderCfg = ReflectionUtils.invokeStaticMethod("android.os.SystemProperties", "get", "ro.dlna.startrender");
			if(!StringUtils.isEmptyObj(defRenderCfg)){//已在系统中进行配置
				try{
					String rendercfgStr = (String)defRenderCfg;
					Boolean autoStartRender = Boolean.valueOf(rendercfgStr.trim());
					logger.debug("ro.dlna.startrender config value: "+rendercfgStr);
					saveMediaRendererAutoable(context, autoStartRender);
					return autoStartRender;
				}catch(Exception e){
					logger.error("ro.dlna.startrender config error. true/false?");
				}
			}
		}*/
		return pref.getBoolean(KEY_AUTO_START, true);
	}
	
	/**
	 * 设置Renderer device是否随机启动
	 * @param context
	 * @return
	 */
	public static void saveMediaRendererAutoable(Context context, boolean auto){
		SharedPreferences pref = getDefaultSharedPreferences(context);
		pref.edit().putBoolean(KEY_AUTO_START, auto).commit();
	}
	
	/**
	 * 获取Server device是否随机启动
	 * @param context
	 * @return
	 */
	public static boolean getMediaServerAutoable(Context context){
		/*
		//读取系统默认配置
		Object defServeerCfg = ReflectionUtils.invokeStaticMethod("android.os.SystemProperties", "get", "ro.dlna.startserver");
		if(!StringUtils.isEmptyObj(defServeerCfg)){//已在系统中进行配置
			try{
				String servercfgStr = (String)defServeerCfg;
				Boolean autoStartRender = Boolean.valueOf(servercfgStr.trim());
				logger.debug("ro.dlna.startserver config value: "+servercfgStr);
				saveMediaRendererAutoable(context, autoStartRender);
				return autoStartRender;
			}catch(Exception e){
				logger.error("ro.dlna.startserver config error. true/false?");
			}
		}*/
		return true;
	}
	
	/**
	 * 是否为DLNA认证模式
	 */
	public static boolean isDLNACertified(){
		return DLNA_CERTIFIED;
	}
	
}
