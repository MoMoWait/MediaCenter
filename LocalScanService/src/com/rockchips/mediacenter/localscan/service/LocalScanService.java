package com.rockchips.mediacenter.localscan.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.rockchips.mediacenter.basicutils.constant.DeviceDataConst;
import com.rockchips.mediacenter.basicutils.util.IICLOG;
import com.rockchips.mediacenter.localscan.devicemgr.DeviceDataUtils;
import com.rockchips.mediacenter.localscan.manager.IDeviceMountListener;
import com.rockchips.mediacenter.localscan.manager.LocDevScanManager;
import com.rockchips.mediacenter.localscan.manager.LocDevicesManager;
/**
 * 本地扫描服务，启动本地设备管理器，本地扫描管理器
 * @author GaoFei
 *
 */
public class LocalScanService extends Service implements IDeviceMountListener
{    
    private static final String TAG = "LocalScanService";
    private IICLOG mLog = IICLOG.getInstance();
    private LocDevicesManager mLocDevicesManager;
    private LocDevScanManager mLocDevScanManager;
       
    @Override
    public void onCreate()
    {
        super.onCreate();          
        init();
        mLog.d(TAG, "onCreate!");
    }

    @Override
    public IBinder onBind(Intent arg0)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    private void init()
    {
        mLocDevicesManager = LocDevicesManager.getInstance(this);
        mLocDevScanManager = LocDevScanManager.getInstance(this);
        
        mLocDevicesManager.start(this);
    }
    
    @Override
    public void onDestroy()
    {
        mLocDevicesManager.removeDeviceMountListener(this);
        mLocDevicesManager.stopThread();
        super.onDestroy();        
    }
        
    @Override
    public void onDeviceMount(String mountPath)
    {
        Message msg = deviceMountHandler.obtainMessage();
        msg.obj = mountPath;
        msg.what = DeviceDataConst.BC_TYPE_DEV_UP;
        deviceMountHandler.sendMessage(msg);         
        mLog.d(TAG, "onDeviceMount mountPath = " + mountPath);
    }
    
    @Override
    public void onDeviceUnmount(String mountPath)
    {          
        Message msg = deviceMountHandler.obtainMessage();
        msg.obj = mountPath;
        msg.what = DeviceDataConst.BC_TYPE_DEV_DOWN;
        deviceMountHandler.sendMessage(msg);          
        mLog.d(TAG, "onDeviceUnmount mountPath = " + mountPath);      
    }   
    
    private Handler deviceMountHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            if (null == msg)
            {
                return;
            }
            String mountPath = (String) msg.obj;
            super.handleMessage(msg);   
            switch (msg.what)
            {
                case DeviceDataConst.BC_TYPE_DEV_UP:                                         
                    mLocDevScanManager.createTableFileAndFolder(mountPath);
                    // 查询数据库，看该挂载路径是否已存在，存在则不进行处理。不存在，则入库
                    mLocDevicesManager.insDeviceWhenMounted(mountPath);
                    // 发送本地设备上线消息
                    DeviceDataUtils.sendBroadcastToMyMedia(DeviceDataConst.BC_TYPE_DEV_UP, mountPath, getBaseContext());
                    //开始磁盘扫描
                    mLocDevScanManager.startDiskScan(mountPath);
                    break;
                case DeviceDataConst.BC_TYPE_DEV_DOWN:
                    mLocDevicesManager.delLocalDevDate(mountPath); 
                    // 发送本地设备删除消息
                    DeviceDataUtils.sendBroadcastToMyMedia(DeviceDataConst.BC_TYPE_DEV_DOWN, mountPath, getBaseContext());
                    //停止磁盘扫描
                    mLocDevScanManager.stopDiskScan(mountPath);
                    break;
            }
            
        }
        
    };
}
