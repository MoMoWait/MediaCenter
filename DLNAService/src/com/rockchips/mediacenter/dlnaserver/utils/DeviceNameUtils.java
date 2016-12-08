/**
 * Title: DeviceNameUtils.java<br>
 * Package: com.rockchips.mirrorsinkserver.util<br>
 * Description: 获取本机设备名<br>
 * @author s00211113
 * @version v1.0<br>
 * Date: 2014-4-28下午4:49:15<br> 
 * Copyright © Huawei Technologies Co., Ltd. 2014. All rights reserved.
 */

package com.rockchips.mediacenter.dlnaserver.utils;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Settings;
import android.provider.Settings.Global;

import com.rockchips.mediacenter.basicutils.util.IICLOG;
import com.rockchips.mediacenter.basicutils.util.StringUtils;

/**
 * @Description: 获取本机设备名<br>
 * @author s00211113
 * @version v1.0
 * @Date: 2014-4-28 下午4:49:15<br>
 */

public class DeviceNameUtils
{
    private static final String TAG = "DeviceNameUtils";

    private IICLOG mLog = IICLOG.getInstance();

    /* 暂时去掉参数ip */
    /**
     * 
     * @description:获取本机设备名的方法，源码copy自媒体中心
     * @date:2014-5-6
     * @Author:s00211113
     * @case :
     */
    public String getDeviceName(Context context, String defaultName)
    {
        ContentResolver cr = context.getContentResolver();

        String boxDeviceName = null;
        Uri uri = null;

        try
        {
            uri = Uri.parse("content://settings/stbconfig");
            boxDeviceName = getValue(cr, uri, "stb_device_name");
        }
        catch (NullPointerException e)
        {
            mLog.e(TAG, "getDeviceName: " + e);
        }
        catch (IllegalArgumentException e)
        {
            mLog.e(TAG, "getDeviceName: " + e);
        }

        mLog.d(TAG, "getDeviceName: DeviceName = " + boxDeviceName);
        try
        {
            if (StringUtils.isEmpty(boxDeviceName))
            {
                uri = Uri.parse("content://stbconfig/stbconfig/stb_device_name");
                boxDeviceName = getValue(cr, uri);
            }
        }
        catch (NullPointerException e)
        {
            mLog.e(TAG, "getDeviceName: " + e);
        }
        catch (IllegalArgumentException e)
        {
            mLog.e(TAG, "getDeviceName: " + e);
        }

        mLog.d(TAG, "getDeviceName: 2 DeviceName = " + boxDeviceName);
        
        if (StringUtils.isEmpty(boxDeviceName))
        {
            //boxDeviceName = Settings.Global.getString(cr, Global.DEVICE_NAME);
			boxDeviceName = Settings.Global.getString(cr, "Media_Center");
        }
        
        mLog.d(TAG, "getDeviceName: 3 DeviceName = " + boxDeviceName);

        if (StringUtils.isEmpty(boxDeviceName))
        {
            boxDeviceName = defaultName;
        }

        return boxDeviceName;
    }

    private String getValue(ContentResolver cr, Uri uri, String col)
    {
        mLog.d(TAG, "getValue: col = " + col);
        Cursor cursor = cr.query(uri, new String[]
        { "value" }, "name =?", new String[]
        { col }, null);
        String value = null;

        if (cursor == null)
        {
            return value;
        }

        if (cursor.getCount() == 0)
        {
            cursor.close();
            return value;
        }

        if (cursor.moveToFirst())
        {
            value = cursor.getString(0);
        }

        cursor.close();

        return value;
    }

    private String getValue(ContentResolver cr, Uri uri)
    {
        Cursor cursor = cr.query(uri, null, null, null, null);
        String value = null;

        if (cursor != null)
        {
            if (cursor.moveToFirst())
            {
                value = cursor.getString(cursor.getColumnIndex("value"));
                mLog.d(TAG, "cursor.getColumnCount" + cursor.getColumnCount());
                mLog.d(TAG, "cursor.getCount" + cursor.getCount());
                mLog.d(TAG, "cursor.getString(0)" + value);
                mLog.d(TAG, "cursor.getColumnIndex---stb_device_name:" + cursor.getColumnIndex("stb_device_name"));
            }

            cursor.close();
        }

        return value;
    }
}
