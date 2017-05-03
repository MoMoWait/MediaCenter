/**
 * 
 */
package com.rockchips.mediacenter.utils;

import android.os.SystemProperties;
/**
 * @author GaoFei
 * 平台工具
 */
public class PlatformUtils {
    /**
     * 是否支持PIP
     * @return
     */
    public static boolean isSupportPIP(){
        return android.os.Build.VERSION.SDK_INT >= 24;
    }
    
    /**
     * 获取SDK版本
     * @return
     */
    public static int getSDKVersion(){
    	return android.os.Build.VERSION.SDK_INT;
    }
    
    /**
     * 是否支持IPTV
     * @return
     */
    public static boolean isSupportIPTV(){
    	//return false;
    	return getSDKVersion() <= 19 && "true".equals(SystemProperties.get("ro.iptv.enable", "false"));
    }
    
    /**
     * 是否支持文件管理
     * @return
     */
    public static boolean isSupportFileManager(){
    	return getSDKVersion() > 19 || isSupportIPTV();
    }
    
    /**
     * 浏览图片时，是否支持背景音乐
     * @return
     */
    public static boolean isSupportBackMusic(){
    	return getSDKVersion() > 19 || isSupportIPTV();
    }
    
    /**
     * 播放音乐时，是否支持背景图
     * @return
     */
    public static boolean isSupportBackPhoto(){
    	return getSDKVersion() > 19 || isSupportIPTV();
    }
    
    /**
     * 播放视频时，是否支持声道设置
     * @return
     */
    public static boolean isSupportSoundChannel(){
    	return getSDKVersion() > 19 || isSupportIPTV();
    }
}
