/**
 * Title: LocDevicesManager.java<br>
 * Package: com.rockchips.mediacenter.localscan.manager<br>
 * Description: TODO<br>
 * @author c00224451
 * @version v1.0<br>
 * Date: 2014-6-26下午5:07:37<br> 
 * Copyright © Huawei Technologies Co., Ltd. 2014. All rights reserved.
 */

package com.rockchips.mediacenter.localscan.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.rockchips.mediacenter.basicutils.bean.LocalDeviceInfo;
import com.rockchips.mediacenter.basicutils.constant.DeviceDataConst;
import com.rockchips.mediacenter.basicutils.util.IICLOG;
import com.rockchips.mediacenter.basicutils.util.MountIntentUtil;
import com.rockchips.mediacenter.localscan.database.LocalDBHelper;
import com.rockchips.mediacenter.localscan.database.table.TableDeviceManager;
import com.rockchips.mediacenter.localscan.devicemgr.DevDataRetriByCmd;
import com.rockchips.mediacenter.localscan.devicemgr.DeviceDataUtils;
import com.rockchips.mediacenter.localscan.devicemgr.DevicesMountPath;
import com.rockchips.mediacenter.localscan.devicemgr.IDeviceDataRetriever;
import com.rockchips.mediacenter.localscan.devicemgr.LocDevicesQueue;

/**
 * Description: 本地设备管理器，主要处理设备上下线消息
 * @author c00224451
 * @version v1.0
 * Date: 2014-6-26 下午5:07:37<br>
 */

public final class LocDevicesManager
{
    private static final String TAG = "LocalScanService";
    private IICLOG mLog = IICLOG.getInstance();
    private static LocDevicesManager mStLocDevicesManager;
    private Context mContext;    
    private LocDevicesQueue mLocDevicesQueue;
    private DevicesMountThread mDevicesMountThread;
    private boolean mIsRunning;
    // 目前外设数据的获取是通过解析SHELL命令来实现的
    private IDeviceDataRetriever mDeviceDataGet;
    private TableDeviceManager mTableDeviceManager;
    
    private List<IDeviceMountListener> mMountListenerList;
    private LocDevicesManager(Context context)
    {        
        mIsRunning = true;
        mContext = context;   
        
        mMountListenerList = new ArrayList<IDeviceMountListener>();
        
        mDeviceDataGet = new DevDataRetriByCmd(context);
        mTableDeviceManager = TableDeviceManager.getInstance(mContext);
        
        mLocDevicesQueue = LocDevicesQueue.getInstance();
        mDevicesMountThread = new DevicesMountThread();        
        mLog.d(TAG, "onCreate end!");
    }
    
    public static LocDevicesManager getInstance(Context context)
    {
        if (null == mStLocDevicesManager)
        {
            mStLocDevicesManager = new LocDevicesManager(context);
        }
        return mStLocDevicesManager;
    }
    
    public void start(IDeviceMountListener listener)
    {
        addDeviceMountListener(listener);
        //启动设备上下线队列消息的处理线程
        new Thread(mDevicesMountThread).start();        
    }
         
    /**
     * Description: TODO<br>
     * @author c00224451
     * @version v1.0
     * Date: 2014-6-26 下午5:07:37<br>
     */
    class DevicesMountThread implements Runnable
    {        
        @Override
        public void run()
        {
            while (mIsRunning)
            {
                // 获取一个设备上下线消息
                Intent intent = mLocDevicesQueue.dequeue();            
                // 发生异常，则退出
                if (intent == null)
                {
                    continue;
                }                           
                // 处理Intent消息，解析后入库
                dealWithIntentMsg(intent);
            }
            
        }
        
    }
    
    /**
     * 处理Intent消息，解析后入库
     * 
     * @param intent 外设插拔消息
     * @see [类、类#方法、类#成员]
     */
    private void dealWithIntentMsg(Intent intent)
    {
        // 解析file://mount_path格式
        String mountPath = parseIntentData(intent);
        mLog.d(TAG, "cc msg dealWithIntentMsg mountPath = " + mountPath);
        if (mountPath == null || mountPath.trim().length() == 0)
        {
            return;
        }
        int currentVersion = android.os.Build.VERSION.SDK_INT;
        // 对挂载消息进行过滤，只入库USB设备及SD卡,对于6.0以下设备进行验证        
        if (currentVersion < 23 && !isSupportMountPath(mountPath))
        {
        	mLog.d(TAG, "not support " + mountPath);
            return;
        }        
        
        if (MountIntentUtil.isDeviceUnmountIntent(intent))
        {            
            //发送本地设备删除消息通知
            for (int i = 0; i < mMountListenerList.size(); ++i)
            {
                IDeviceMountListener listener = mMountListenerList.get(i);
                listener.onDeviceUnmount(mountPath);
            }  
            DevicesMountPath.removeMountPath(mountPath);
        }
        else if (MountIntentUtil.isDeviceMountIntent(intent))
        {
          //扫描当前mnt/sda/sda1是否存在，如果不存在则直接返回，不入库
            //File file = new File(mountPath);
            //if (!file.exists())
            //{
            //    return;
            //}
            
            //if (!file.canRead() && !file.canWrite())
            //{
             //   return;
            //} 
            
            mLog.d(TAG, "----mountPath---:" + mountPath);            
            for (int i = 0; i < mMountListenerList.size(); ++i)
            {
                IDeviceMountListener listener = mMountListenerList.get(i);
                listener.onDeviceMount(mountPath);
            }
            DevicesMountPath.addMountPath(mountPath);
        }
    }
    
    /**
     * 解析出mount path
     * file:///mnt/sda/sda1 mountpath = /mnt/sda/sda1
     * @param intent 外设插拔消息
     * @return mount path
     * @see [类、类#方法、类#成员]
     */
    private String parseIntentData(Intent intent)
    {
        String uriData = intent.getDataString();
        
        // 解析file://mount_path格式
        if (uriData != null && uriData.startsWith(DeviceDataConst.FILE_PROTOCOL)
            && uriData.length() > DeviceDataConst.FILE_PROTOCOL.length())
        {
            uriData = uriData.substring(DeviceDataConst.FILE_PROTOCOL.length()).trim();
        }
        
        return uriData;
    }
    
    public void stopThread()
    {
        mIsRunning = false;
    }
    
    public void addDeviceMountListener(IDeviceMountListener listener)
    {
        if (!mMountListenerList.contains(listener))
        {
            mMountListenerList.add(listener);
        }
    }
    
    public void removeDeviceMountListener(IDeviceMountListener listener)
    {
        if (mMountListenerList.contains(listener))
        {
            mMountListenerList.remove(listener);
        }
    }
    
    /**
     * 删除本地设备
     * 根据挂载路径，删除本地设备及其分区
     * @param mountPath 载路径
     * @see [类、类#方法、类#成员]
     */
    public void delLocalDevDate(String mountPath)
    {
        mLog.d(TAG, "begin: delete device by mount path = " + mountPath);
        
        if (mountPath == null)
        {
            return;
        }
        mTableDeviceManager.delete(mountPath);                
        mLog.d(TAG, "end: delete device by mount path");
    }
    
    /**
     * 添加本地设备
     * 根据挂载路径，若该设备新挂载则入库
     * @param mountPath 载路径
     * @see [类、类#方法、类#成员]
     */
    public void insDeviceWhenMounted(String mountPath)
    {
        mLog.d(TAG, "begin: insert device by mount path = " + mountPath);
        
        if (mountPath == null)
        {
            return;
        }
        
        // 是否需要抽象一个设备
        boolean isNeedPhyDev = checkPhyAndDiskData(mountPath);
        
        // 根据MountService上报的广播消息的mount path获得相关设备的数据
        List<LocalDeviceInfo> devList = mDeviceDataGet.getDeviceDataByBroadcast(mountPath, isNeedPhyDev);
        // 设备数据入库
        insertDevices(devList);
        
        mLog.d(TAG, "end: insert device by mount path");
    }
    
    /**
     * 查询数据库，判断该条语句是否需要入库
     * 入库时需要判断是否存在物理设备
     * 
     * @param mountPath
     * @return 物理设备是否需要入库
     * @see [类、类#方法、类#成员]
     */
    private boolean checkPhyAndDiskData(String mountPath)
    {        
        boolean hasPhyDev = false;
        boolean hasDiskDev = false;
        
        // 查询数据库
        Cursor cursor = mTableDeviceManager.queryDevicesIsExists(mountPath);
        if (null == cursor)
        {
            return false;
        }
        
        
        // 临时数据
        int item = 0;
        String devType = null;
        
        // 遍历检索数据
        while (cursor.moveToNext())
        {
            // 存在的数据行数
            item = cursor.getInt(cursor.getColumnIndex(LocalDeviceInfo.DEVICE_COUNT));
            // 设备类型
            devType = cursor.getString(cursor.getColumnIndex(LocalDeviceInfo.DEVICE_TYPE));
            
            // 该分区已经存在
            if (DeviceDataConst.DEVICE_TYPE_DISK.equals(devType) && item > 0)
            {
                hasDiskDev = true;
            }
            // 物理设备已经存在
            else if (DeviceDataConst.DEVICE_TYPE_PHY.equals(devType) && item > 0)
            {
                hasPhyDev = true;
            }
        }        
        cursor.close();        
        
        // 存在相同的分区，说明有异常
        if (hasDiskDev)
        {
            // 删除相关数据
            delLocalDevDate(mountPath);
            return true;
        }
        
        // 存在物理设备，则不需要再入库
        if (hasPhyDev && !hasDiskDev)
        {
            return false;
        }
        
        // 物理设备及分区均要入库
        return true;
    }
    
    /**
     * 设备数据入库
     * 
     * @param devList 设备列表
     * @see [类、类#方法、类#成员]
     */
    private void insertDevices(List<LocalDeviceInfo> devList)
    {
    	Log.i(TAG, "insertDevices->db path:" + LocalDBHelper.getDbDirPath());
    	Log.i(TAG, "insertDevices->devList:" + devList);
        // 没有挂载外设，则直接退出
        if (devList == null || devList.size() == 0)
        {
            return;
        }
        List<ContentValues> contentList = new ArrayList<ContentValues>();
        
        // 遍历数据构造批量插入SQL语句及参数
        for (LocalDeviceInfo dto : devList)
        {
            ContentValues contentValues = TableDeviceManager.getContentValues(dto);
            contentList.add(contentValues); 
        }
        // 入库
        mTableDeviceManager.insert(contentList);
    }
    
    public boolean isScanned(String path)
    {
        return mTableDeviceManager.isScanned(path);
    }
        
    private boolean isSupportMountPath(String mountPath)
    {
        String[] deviceMountPath = DeviceDataUtils.getDeviceMountPath();
        if (null == deviceMountPath)
        {
            return false;
        }
        for (int i = 0; i < deviceMountPath.length; ++i)
        {
            if (mountPath.startsWith(deviceMountPath[i]))
            {
                return true;
            }
        }
        return false;
    }
}
