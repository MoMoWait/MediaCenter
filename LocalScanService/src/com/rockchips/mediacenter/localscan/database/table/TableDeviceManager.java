/**
 * Title: TableDeviceManager.java<br>
 * Package: com.rockchips.mediacenter.localscan.database.table<br>
 * Description: TODO<br>
 * @author c00224451
 * @version v1.0<br>
 * Date: 2014-7-1下午3:49:51<br> 
 * Copyright © Huawei Technologies Co., Ltd. 2014. All rights reserved.
 */

package com.rockchips.mediacenter.localscan.database.table;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.rockchips.mediacenter.basicutils.bean.LocalDeviceInfo;
import com.rockchips.mediacenter.basicutils.constant.DeviceDataConst;
import com.rockchips.mediacenter.basicutils.constant.DiskScanConst;
import com.rockchips.mediacenter.basicutils.util.IICLOG;
import com.rockchips.mediacenter.basicutils.util.StringUtils;
import com.rockchips.mediacenter.localscan.database.LocalDBHelper;
import com.rockchips.mediacenter.localscan.devicemgr.DeviceDataUtils;

/**
 * Description: 设备表管理操作<br>
 * @author c00224451
 * @version v1.0
 * Date: 2014-7-1 下午3:49:51<br>
 */

public final class TableDeviceManager
{    
    private final static String TABLE_DEVICES = "Devices";  
    public final static String COUNT_ITEM = "item";
    public final static String DEV_TYPE = "dev_type";
    private final static String CREATE_DEVICE_TABLE = 
            "CREATE TABLE IF NOT EXISTS " + TABLE_DEVICES + '('            
            // 设备类型：SD卡，USB设备
            + LocalDeviceInfo.DEVICE_TYPE + " integer,"
            // 设备容量及其使用情况
            + LocalDeviceInfo.TOTAL_SIZE + " text," 
            + LocalDeviceInfo.USED_SIZE + " text," 
            + LocalDeviceInfo.FREE_SIZE + " text," 
            + LocalDeviceInfo.USED_PERCENT + " text,"
            // 对应的物理设备的标识
            + LocalDeviceInfo.PHYSIC_ID + " text ,"
            // 是否是物理设备
            + LocalDeviceInfo.IS_PHYSIC_DEV + " integer,"
            // 挂载路径
            + LocalDeviceInfo.MOUNT_PATH + " text," 
            // 是否已进行了文件内容扫描
            + LocalDeviceInfo.IS_SCANNED + " integer," 
            + "PRIMARY KEY (" + LocalDeviceInfo.PHYSIC_ID + "," +  LocalDeviceInfo.MOUNT_PATH + "));";
    
    // 设备表的Delete语句
    String DEVICES_DELETE_SQL = "delete from " + TABLE_DEVICES 
        + " where " + LocalDeviceInfo.PHYSIC_ID + " in "
        + "(select distinct " + LocalDeviceInfo.PHYSIC_ID + " from " + TABLE_DEVICES
        + " where " + LocalDeviceInfo.MOUNT_PATH + " = ?)";
    
    // 查询语句，查询指定的分区及其设备是否存在
    String DEVICES_ISEXIST_SQL = " select count( + " + LocalDeviceInfo.PHYSIC_ID + ") as " + LocalDeviceInfo.DEVICE_COUNT + ", \"PhysicDev\" as " + LocalDeviceInfo.DEVICE_TYPE 
                               + " from " + TABLE_DEVICES 
                               + " where " + LocalDeviceInfo.PHYSIC_ID + " = ? and " + LocalDeviceInfo.MOUNT_PATH + " = \"\" "
                               + " union " 
                               + " select count(" + LocalDeviceInfo.PHYSIC_ID + ") as " + LocalDeviceInfo.DEVICE_COUNT + ", \"Disk\" as " + LocalDeviceInfo.DEVICE_TYPE 
                               + " from  "+ TABLE_DEVICES 
                               + " where " + LocalDeviceInfo.MOUNT_PATH + " = ?";
          
    private static TableDeviceManager mStTableDeviceManager;
    
    protected static final String TAG = "LocalScanService";
    protected IICLOG mLog = IICLOG.getInstance();
    protected LocalDBHelper mLocalDBHelper;
    
    private TableDeviceManager(Context context)
    {      
        mLocalDBHelper = LocalDBHelper.getInstance(context);
        createTable();
    }
    
    public static TableDeviceManager getInstance(Context context)
    {
        if (null == mStTableDeviceManager)
        {
            mStTableDeviceManager = new TableDeviceManager(context);
        }
        return mStTableDeviceManager;
    }
    
    private void createTable()
    {
        mLocalDBHelper.execSQL(CREATE_DEVICE_TABLE);
    }
        
    public long insert(ContentValues values) 
    {
        long rowId = mLocalDBHelper.insert(TABLE_DEVICES, values);         
        return rowId;
    }
        
    public void insert(List<ContentValues> valueList)
    {
        mLocalDBHelper.insert(TABLE_DEVICES, valueList);
    }
    
    public int update(ContentValues values,  String whereClause, String[] whereArgs)
    {
        return mLocalDBHelper.update(TABLE_DEVICES, values, whereClause, whereArgs);
    }
    
    public int updateDeviceScanned(String mountPath)
    {        
        ContentValues values = new ContentValues();
        values.put(LocalDeviceInfo.IS_SCANNED, 1);
        String whereClause = LocalDeviceInfo.MOUNT_PATH + " = ? ";
        String[] arg = {mountPath};
        return mLocalDBHelper.update(TABLE_DEVICES, values, whereClause, arg);
    }
    
    public void delete(String mountPath)
    {
        if (mountPath == null)
        {
            return;
        }        
        // 构造删除条件
        String[] params = {mountPath};        
        mLocalDBHelper.execSQL(DEVICES_DELETE_SQL, params);
    }
    
    public Cursor queryAllByExcludePath(String mountPath)
    {
        String selection = LocalDeviceInfo.MOUNT_PATH + " != ? ";
        String[] selectionArg = {mountPath};
        String[] colnumName = {LocalDeviceInfo.MOUNT_PATH, LocalDeviceInfo.PHYSIC_ID, LocalDeviceInfo.DEVICE_TYPE};
        return mLocalDBHelper.queryNotDistinct(TABLE_DEVICES, colnumName, selection, selectionArg, null);
    }
    
    public Cursor queryAllPartion(String physicId, boolean isPhysic)
    {        
        String selection;
        String[] selectionArg;
        if (StringUtils.isEmpty(physicId))
        {
            selection = LocalDeviceInfo.IS_PHYSIC_DEV + " = " + getString(isPhysic);
            selectionArg = null;
                        
        }
        else
        {
            selection = LocalDeviceInfo.PHYSIC_ID + " = ? " + " and " + LocalDeviceInfo.IS_PHYSIC_DEV + " = " + getString(isPhysic);
            
            selectionArg = new String[] {physicId};
        }
                
        String orderBy = LocalDeviceInfo.MOUNT_PATH;
        return mLocalDBHelper.queryNotDistinct(TABLE_DEVICES, null, selection, selectionArg, orderBy);
    }
    
    public Cursor queryAllDevByIsPhysicDev(boolean isPhysicDev)
    {
        String[] columns = {LocalDeviceInfo.PHYSIC_ID, LocalDeviceInfo.DEVICE_TYPE};
        String selection = LocalDeviceInfo.IS_PHYSIC_DEV + " = " + getString(isPhysicDev);
        return mLocalDBHelper.queryDistinct(TABLE_DEVICES, columns, selection, null, null);
    }
    
    public Cursor queryPathByIsScanned(boolean isScanned)
    {
        String[] columnName = {LocalDeviceInfo.MOUNT_PATH};
        String selection = LocalDeviceInfo.IS_SCANNED + " = " + getString(isScanned) + " and " + LocalDeviceInfo.IS_PHYSIC_DEV + " = 0";
        return mLocalDBHelper.queryDistinct(TABLE_DEVICES, columnName, selection, null, null);
    }
    
    private String getString(boolean isTrue)
    {
        return isTrue ?  "1" : "0";
    }
    public boolean isScanned(String mountPath)
    {
        String[] columnName = {LocalDeviceInfo.IS_SCANNED};
        String selection = LocalDeviceInfo.MOUNT_PATH + " = ? ";
        String[] selectionArg = {mountPath};
        Cursor cursor = mLocalDBHelper.queryNotDistinct(TABLE_DEVICES, columnName, selection, selectionArg, null);
        int isScanned = 0;
        if (cursor != null)
        {
            if (cursor.moveToNext())
            {
                isScanned = cursor.getInt(cursor.getColumnIndex(LocalDeviceInfo.IS_SCANNED));
            }
            cursor.close();
        }
        
        if (isScanned == DiskScanConst.HAS_ALREAY_SCANNED)
        {
            return true;
        }
        return false;        
    }
    
    public Cursor queryDevicesIsExists(String mountPath)
    {
        // 分割挂载路径/mnt/sda/sda1
        String physicId  = DeviceDataUtils.getPhysicDevId(mountPath);
        if (null == physicId)
        {
            return null;
        }
        // 查询该设备是否已经在数据库中存在       参数：/mnt/sda/sda1 -> sda
        String[] params = {physicId, mountPath};        
        return mLocalDBHelper.query(DEVICES_ISEXIST_SQL, params);
    }
    
    public static ContentValues getContentValues(LocalDeviceInfo deviceInfo)
    {
        ContentValues values = new ContentValues();
        values.put(LocalDeviceInfo.DEVICE_TYPE, deviceInfo.getDeviceType());
        values.put(LocalDeviceInfo.TOTAL_SIZE, StringUtils.filterNullData(deviceInfo.getTotalSize()));
        values.put(LocalDeviceInfo.USED_SIZE, StringUtils.filterNullData(deviceInfo.getUsedSize()));
        values.put(LocalDeviceInfo.FREE_SIZE, StringUtils.filterNullData(deviceInfo.getFreeSize()));
        values.put(LocalDeviceInfo.USED_PERCENT, StringUtils.filterNullData(deviceInfo.getUsedPercent()));
        values.put(LocalDeviceInfo.PHYSIC_ID, StringUtils.filterNullData(deviceInfo.getPhysicId()));
        values.put(LocalDeviceInfo.IS_PHYSIC_DEV, deviceInfo.getIsPhysicDev());
        values.put(LocalDeviceInfo.IS_SCANNED, deviceInfo.getIsScanned()); 
        values.put(LocalDeviceInfo.MOUNT_PATH, deviceInfo.getMountPath());
        return values;
    }
}
