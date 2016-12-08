/**
 * 
 * com.rockchips.iptv.stb.dlna.player
 * DBUtils.java
 * 
 * 2011-12-28-下午04:33:17
 * Copyright 2011 Huawei Technologies Co., Ltd
 * 
 */
package com.rockchips.mediacenter.audioplayer;

import java.util.HashMap;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.rockchips.mediacenter.basicutils.bean.LocalMediaInfo;
import com.rockchips.mediacenter.basicutils.util.StringUtils;

/**
 * 
 * DBUtils
 * 
 * 2011-12-28 下午04:33:17
 * 
 * @version 1.0.0
 * 
 */
public class DBUtils
{
    private static final String LOGTAG = "MediaCenterApp";
    
    private static final String SYSTEM_MEDIA__PATH = "/data/data/com.android.providers.media/databases/external.db";
    
    private static final String SQL_DEVICEID_BY_MOUNTPATH = "select device_id from devices where mount_path = ?";
    
    private static HashMap<String, String> mountDevices = new HashMap<String, String>();
    
    /**
     * 获取所有的挂载路径和DeviceId的对应表
     * @param context
     * @param mountPath
     * @return 设备ID
     */
    public static HashMap<String, String> getMountDevicesMap(Context context)
    {
        mountDevices.clear();
        
        Uri devicesUri = Uri.parse("content://media/external/devices");
        
        Cursor c = null;
        try
        {
            Log.i(LOGTAG, "getMountDevicesMap---------------------------");
            
            c =
                context.getContentResolver()
                    .query(devicesUri, new String[] {"mount_path, device_id"}, null, null, null);
            
            if (c != null)
            {
                int deviceIdColumn = c.getColumnIndex("device_id");
                int mountPathColumn = c.getColumnIndex("mount_path");
                
                while (c.moveToNext())
                {
                    mountDevices.put(c.getString(mountPathColumn), c.getString(deviceIdColumn));
                }
            }
        }
        catch (Exception ex)
        {
            Log.w(LOGTAG, StringUtils.getSystemTimeLogText(), ex);
        }
        finally
        {
            if (c != null)
            {
                c.close();
            }
        }
        
        return mountDevices;
    }
    
    public static String getDeviceId(String mountPath){
        Log.i(LOGTAG, "mountDevices---------------------------"+mountDevices);
        
        return mountDevices.get(mountPath);
    }
    
    /**
     * 通过MountPath得到DeviceId，前提：系统有提供ContextProvider接口
     * @param context
     * @param mountPath
     * @return 设备ID
     */
    public static String getDeviceIdByMountPath(Context context, String mountPath)
    {
        Uri devicesUri = Uri.parse("content://media/external/devices");
        
        Cursor c = null;
        try
        {
            Log.i(LOGTAG, "getDeviceIdByMountPath-----------------------" + mountPath);
            
            c =
                context.getContentResolver().query(devicesUri,
                    new String[] {"device_id"},
                    "mount_path = ?",
                    new String[] {mountPath},
                    null);
            if (c != null)
            {
                int deviceIdColumn = c.getColumnIndex("device_id");
                
                if (c.moveToNext())
                {
                    return c.getString(deviceIdColumn);
                }
            }
        }
        catch (Exception ex)
        {
            Log.w(LOGTAG, StringUtils.getSystemTimeLogText(), ex);
        }
        finally
        {
            if (c != null)
            {
                c.close();
            }
        }
        
        return null;
    }
    
    /**
     * @deprecated 目前该版本暂无系统权限
     * 通过MountPath得到DeviceId，前提：应用需要系统权限
     * @param mountPath
     * @return
     */
    public static String getDeviceIdByMountPath(String mountPath)
    {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        
        try
        {
            db = SQLiteDatabase.openDatabase(SYSTEM_MEDIA__PATH, null, SQLiteDatabase.OPEN_READONLY);
            
            cursor = db.rawQuery(SQL_DEVICEID_BY_MOUNTPATH, new String[] {mountPath});
            if (cursor != null)
            {
                if (cursor.moveToNext())
                {
                    return cursor.getString(cursor.getColumnIndex("device_id"));
                }
            }
        }
        catch (Exception ex)
        {
            Log.w(LOGTAG, StringUtils.getSystemTimeLogText(), ex);
        }
        finally
        {
            if (cursor != null)
            {
                try
                {
                    cursor.close();
                }
                catch (Exception ex)
                {
                }
            }
            
            if (db != null)
            {
                try
                {
                    db.close();
                }
                catch (Exception ex)
                {
                }
            }
        }
        
        return null;
    }
    
    /**
     * 通过displayName来填充媒体对象的值
     * @param mediaInfo 要填充的媒体对象
     * @param context 当前的上下文对象
     * @param displayName 以"content://"开头的显示名
     */    
    public static void fillValuesByDisplayName(LocalMediaInfo mediaInfo, Context context, String displayName){
        Log.i(LOGTAG, "data----------content://---------------" + displayName);
        
        Uri uri = Uri.parse(displayName);
        
        Cursor c = null;
        try
        {
            //需要关注的数据类型：歌唱者、唱片集、标题、数据
            String[] projection =
                {MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA};
            
            c = context.getContentResolver().query(uri, projection, null, null, null);
            
            if (c != null)
            {
                int dataColumn = c.getColumnIndex(MediaStore.Audio.Media.DATA);
                int artistColumn = c.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                int titleColumn = c.getColumnIndex(MediaStore.Audio.Media.TITLE);
                int albumColumn = c.getColumnIndex(MediaStore.Audio.Media.ALBUM);
                
                if (c.moveToNext())
                {
                    mediaInfo.setmFileName(c.getString(dataColumn));
                    mediaInfo.setmAlbum(c.getString(albumColumn));
                    mediaInfo.setmArtist(c.getString(artistColumn));
                    mediaInfo.setmTitle(c.getString(titleColumn));
                }
            }
        }
        catch (Exception ex)
        {
            Log.w(LOGTAG, StringUtils.getSystemTimeLogText(), ex);
        }
        finally
        {
            if (c != null)
            {
                c.close();
            }
        }
    }
    
}
