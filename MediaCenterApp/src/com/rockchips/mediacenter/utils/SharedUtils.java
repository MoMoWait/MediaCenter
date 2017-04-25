/**
 * 
 */
package com.rockchips.mediacenter.utils;

import momo.cn.edu.fjnu.androidutils.data.CommonValues;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author GaoFei
 * SharedPreference工具
 */
public class SharedUtils {
	/**
	 * 存储值
	 * @param name
	 * @param key
	 * @param value
	 */
	public static void saveValue(String name, String key, String value){
		SharedPreferences.Editor editor = CommonValues.application.getSharedPreferences(name, Context.MODE_PRIVATE).edit();
		editor.putString(key, value);
		editor.commit();
	}
	
	/**
	 * 获取存储的值
	 * @param name
	 * @param key
	 * @return
	 */
	public static String getValue(String name, String key){
		SharedPreferences sharedPreferences = CommonValues.application.getSharedPreferences(name, Context.MODE_PRIVATE);
		return sharedPreferences.getString(key, "");
	}
}
