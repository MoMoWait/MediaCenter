package com.rockchips.mediacenter.utils;

import java.io.File;

import android.os.SystemProperties;

/**
 * 
 * 获取平台的一些属性操作
 * 
 * @author  l00174030
 * @version  [版本号, 2013-6-8]
 */
public class PlatformUtil
{
    private final static String TAG = "PlatformUtil";
    private static IICLOG mStLog = IICLOG.getInstance();
    
    // 3716C平台
    private final static String H3716C_EC6108V2 = "EC6108V2";
    
    // 3716M平台
    //private final static String H3716M_EC6106V3 = "EC6106V3";
    // 平台key
    private final static String KEY = "ro.product.model";
    
    private final static String KEY_REGION = "ro.build.office";
    
    //沙特版本标志信息
    public final static String VALUE_REGION_JPL = "MOBILY";
    
    private final static String AudiaPassThrough = "persist.sys.audio.hdmi.output";
    
    private final static int audioPassThroiugh = 3;
    
    //    android.os.Build.MODEL + ",\n android.os.Build.VERSION.SDK:" + + ",\n android.os.Build.VERSION.RELEASE:"+ android.os.Build.VERSION.RELEASE;
    /**
     * 通过系统的property属性，获取平台的版本
     * 
     * @return true:3716C平台 false:3716M平台
     * @see [类、类#方法、类#成员]
     */
    public static boolean isH3716C()
    {
        
        String property = SystemProperties.get(KEY);
        
        if (H3716C_EC6108V2.equals(property))
        {
            return true;
        }
        
        return false;
    }
    
    /**
     *  获取版本地域信息
     *  
     *  @author zWX160481
     *  @param value 标志某地域的值
     *  @return true--属于  false--不属于
     */
    public static boolean isTheRegionWeNeed()
    {
        String regionValue = SystemProperties.get(KEY_REGION);
        
        mStLog.d(TAG, "getRegion--->current devices offic is-->" + regionValue);
        
        if (StringUtils.isEmpty(regionValue))
        {
            mStLog.d(TAG, "getRegion--false");
            return false;
        }
        
        if (VALUE_REGION_JPL.equals(regionValue.trim()))
        {
            mStLog.d(TAG, "getRegion--true");
            return true;
        }
        else
        {
            mStLog.d(TAG, "getRegion--false");
            return false;
        }
    }
    
    public static boolean isGDDX()
    {
        String regionValue = SystemProperties.get(KEY_REGION);
        
        mStLog.d(TAG, "isGDDX--->current devices offic is-->" + regionValue);
        
        if (StringUtils.isEmpty(regionValue))
        {
            mStLog.d(TAG, "isGDDX--false");
            return false;
        }
        
        if (regionValue.trim().toUpperCase().contains("GDDX"))
        {
            mStLog.d(TAG, "isGDDX--true");
            return true;
        }
        else
        {
            mStLog.d(TAG, "isGDDX--false");
            return false;
        }
    }
    /**
     *  获取版本地域信息
     *  
     *  @author zWX160481
     *  @param value 标志某地域的值
     *  @return true--属于  false--不属于
     */
    public static boolean getRegion(String value)
    {
        if (StringUtils.isEmpty(value))
        {
            mStLog.d(TAG, "getRegion--false");
            return false;
        }
        return true;
    }
    
    /**
      *  区分是否是沙特版本内置磁盘
      *  
      * @author zWX160481
      * @param rootPath 根目录
      * @return true--是   false--不是
      * @see [类、类#方法、类#成员]
      */
    public static boolean isInnerDisk(String rootPath)
    {
        mStLog.d(TAG, "InnerDisk-->rootPath:" + rootPath);
        if (StringUtils.isEmpty(rootPath) || !getRegion(VALUE_REGION_JPL))
        {
            mStLog.d(TAG, "is not InnerDisk");
            return false;
        }
        
        // 判断磁盘里面是否有内置的PVR文件
        File identificationFile = new File(rootPath + "/pvrIdentification.sys");
        File diskBindFile = new File(rootPath + "/pvrDiskBind.sys");
        if (identificationFile.exists() && diskBindFile.exists())
        {
            mStLog.d(TAG, "is InnerDisk");
            return true;
        }
        else
        {
            mStLog.d(TAG, "is not InnerDisk");
            return false;
        }
        
    }
    
    /**
     * SDK >= 17 且為hisi平臺"EC"开头 支持HisiMediaPlayer
     * <功能详细描述>
     * @return
     * @see [类、类#方法、类#成员]
     */
    public static boolean supportHisiMediaPlayerOnJava()
    {
        mStLog.d(TAG, "getPlatformSDKVersion:" + getPlatformSDKVersion());
        return getPlatformSDKVersion() >= 17 && isHisiPlatform();
    }
    
    /**
     * 是否为hisi平台
     * <功能详细描述>
     * @return
     * @see [类、类#方法、类#成员]
     */
    public static boolean isHisiPlatform()
    {
       //add by rym, 删除之前的判断方式，依赖产品的Model是不靠谱的，每次产品自己切换芯片后修改此处的值即可
//        mStLog.d(TAG, "android.os.Build.MODEL:" + android.os.Build.MODEL);
//        return android.os.Build.MODEL.startsWith("EC") || android.os.Build.MODEL.startsWith("MSG")
//            ||android.os.Build.MODEL.startsWith("Hi")||android.os.Build.MODEL.startsWith("M220");
        boolean isHisiPlatform = false;
        mStLog.d(TAG, "isHisiPlatform : " + isHisiPlatform);
        return isHisiPlatform;
    }
    
    /**
     * 获取平台SDK版本
     * <功能详细描述>
     * @return
     * @see [类、类#方法、类#成员]
     */
    public static int getPlatformSDKVersion()
    {
        return Integer.valueOf(android.os.Build.VERSION.SDK);
    }
    
    /**
     * 是否为解决方案的天津版本
     * 注意不包含数字家庭的天津联通版本
     * <功能详细描述>
     * @return 是返回true
     * @see [类、类#方法、类#成员]
     */
    public static boolean isTJVersion()
    {
        String regionValue = SystemProperties.get(KEY_REGION);
        
        mStLog.d(TAG, "isTJVersion--->current devices offic is-->" + regionValue);
        
        if (StringUtils.isEmpty(regionValue))
        {
            mStLog.d(TAG, "isTJVersion--false");
            return false;
        }
        
        if(regionValue.toUpperCase().equals("TJ") 
            || regionValue.toUpperCase().equals("TJ_MSG5100"))
        {
            mStLog.d(TAG, "isTJVersion--true");
            return true;
        }
        else
        {
            mStLog.d(TAG, "isTJVersion--false");
            return false;
        }
    }
    
    public static boolean isAudioPassThrough(){
        String isPassThrough = SystemProperties.get(AudiaPassThrough);
        
        if(isPassThrough == null || isPassThrough.length() == 0)
        {
            return false;
        }
        
        if(audioPassThroiugh == Integer.parseInt(isPassThrough))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    /**
     * 关于局点的判断方法越来越多，以后直接这么写吧。
     * 在这里获取局点，然后在需要的地方再比较好了。
     * add by wanghuanlai
     */
    public static String getVersionOffice(){
        return SystemProperties.get(KEY_REGION);
    }
}
