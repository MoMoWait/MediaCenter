/**
 * 
 */
package com.rockchips.mediacenter.utils;


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
    
}
