/**
 * Title: HideMethod.java<br>
 * Package: com.rockchips.icos.shine.service<br>
 * Description: TODO<br>
 * @author shine Team
 * @version v1.0<br>
 * Date: 2012-11-26下午06:47:31<br>
 * Copyright © Huawei Technologies Co., Ltd. 2012. All rights reserved.
 */

package com.rockchips.mediacenter.dlnaserver.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.rockchips.android.airsharing.util.IICLOG;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;


/**
 * Description: AP操作相关方法<br>
 * @author r00178559
 * @version v1.0
 * Date: 2012-11-26 下午06:47:31<br>
 */

public final class HideMethod
{
    private HideMethod()
    {

    }

    private static final String TAG = HideMethod.class.getSimpleName();

    private static final IICLOG MLOG = IICLOG.getInstance();

    private static Method mStGetWifiApConfigurationMethod;
    private static Method mStSetWifiApConfigurationMethod;
    private static Method mStGetWifiApStateMethod;
    private static Method mStSetWifiApEnabledMethod;

    public static final String TAG_WIFI_AP_STATE_CHANGED_ACTION = "WIFI_AP_STATE_CHANGED_ACTION"; //String
    public static final String TAG_EXTRA_WIFI_AP_STATE = "EXTRA_WIFI_AP_STATE"; //String
    public static final String TAG_EXTRA_PREVIOUS_WIFI_AP_STATE = "EXTRA_PREVIOUS_WIFI_AP_STATE"; //String
    public static final String TAG_WIFI_AP_STATE_DISABLING = "WIFI_AP_STATE_DISABLING"; //int
    public static final String TAG_WIFI_AP_STATE_DISABLED = "WIFI_AP_STATE_DISABLED"; //int
    public static final String TAG_WIFI_AP_STATE_ENABLING = "WIFI_AP_STATE_ENABLING"; //int
    public static final String TAG_WIFI_AP_STATE_ENABLED = "WIFI_AP_STATE_ENABLED"; //int
    public static final String TAG_WIFI_AP_STATE_FAILED = "WIFI_AP_STATE_FAILED"; //int

    public static boolean getDeclaredMethods()
    {
        try
        {
            mStGetWifiApConfigurationMethod = WifiManager.class.getDeclaredMethod("getWifiApConfiguration");
            mStSetWifiApConfigurationMethod = WifiManager.class.getDeclaredMethod("setWifiApConfiguration", WifiConfiguration.class);
            mStGetWifiApStateMethod = WifiManager.class.getDeclaredMethod("getWifiApState");
            mStSetWifiApEnabledMethod = WifiManager.class.getDeclaredMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
        }
        catch (NoSuchMethodException e)
        {
            MLOG.e(TAG, "NoSuchMethodException occur");
            return false;
        }

        return true;
    }

    public static WifiConfiguration getWifiApConfiguration(WifiManager mWifiManager)
    {
        Object object = invokeMethod(mWifiManager, mStGetWifiApConfigurationMethod);
        if (null != object)
        {
            return (WifiConfiguration) object;
        }

        return null;
    }

    public static boolean setWifiApConfiguration(WifiManager mWifiManager, WifiConfiguration wifiConfiguration)
    {
        Object object = invokeMethod(mWifiManager, mStSetWifiApConfigurationMethod, wifiConfiguration);
        if (null != object)
        {
            return ((Boolean) object).booleanValue();
        }

        return false;
    }

    public static int getWifiApState(WifiManager mWifiManager)
    {
        Object object = invokeMethod(mWifiManager, mStGetWifiApStateMethod);
        if (null != object)
        {
            return ((Integer) object).intValue();
        }

        return getIntValue(mWifiManager, TAG_WIFI_AP_STATE_DISABLED);
    }

    public static boolean setWifiApEnabled(WifiManager mWifiManager, WifiConfiguration mConfig, boolean enable)
    {
        Object object = invokeMethod(mWifiManager, mStSetWifiApEnabledMethod, mConfig, enable);
        if (null != object)
        {
            return ((Boolean) object).booleanValue();
        }

        return false;
    }

    private static Object invokeMethod(Object receiver, Method method, Object... args)
    {
        try
        {
            Object mResult = method.invoke(receiver, args);
            return mResult;
        }
        catch (IllegalArgumentException e)
        {
            MLOG.e(TAG, "IllegalArgumentException occur at " + method.getName() + ": " + e.getLocalizedMessage());
        }
        catch (IllegalAccessException e)
        {
            MLOG.e(TAG, "IllegalAccessException occur at " + method.getName() + ": " + e.getLocalizedMessage());
        }
        catch (InvocationTargetException e)
        {
            MLOG.e(TAG, "InvocationTargetException occur at " + method.getName() + ": " + e.getLocalizedMessage());
        }

        return null;
    }

    private static Object getValue(Object instance, String name)
    {
        Object object = null;
        Field field = null;
        try
        {
            field = WifiManager.class.getDeclaredField(name);
        }
        catch (NoSuchFieldException e1)
        {
            MLOG.e(TAG, "NoSuchMethodException occur : " + e1.getLocalizedMessage());
            return null;
        }
        // 参数值为true，禁用访问控制检查
        field.setAccessible(true);
        try
        {
            object = field.get(instance);
        }
        catch (IllegalArgumentException e)
        {
            MLOG.e(TAG, "IllegalArgumentException occur at getValue() : " + e.getLocalizedMessage());
        }
        catch (IllegalAccessException e)
        {
            MLOG.e(TAG, "IllegalAccessException occur at getValue() : " + e.getLocalizedMessage());
        }
        return object;
    }

    public static String getStringValue(Object instance, String name)
    {
        Object object = getValue(instance, name);
        return (String) object;
    }

    public static int getIntValue(Object instance, String name)
    {
        Object object = getValue(instance, name);
        if (null == object)
        {
            return Integer.MIN_VALUE;
        }
        
        return ((Integer) object).intValue();
    }
}
