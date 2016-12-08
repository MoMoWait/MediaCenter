package com.rockchips.mediacenter.localscan.devicemgr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.rockchips.mediacenter.basicutils.bean.LocalDeviceInfo;
import com.rockchips.mediacenter.basicutils.constant.Constant;
import com.rockchips.mediacenter.basicutils.constant.Constant.DeviceType;
import com.rockchips.mediacenter.basicutils.constant.DeviceDataConst;
import com.rockchips.mediacenter.basicutils.util.IICLOG;
import com.rockchips.mediacenter.localscan.application.LocalScanApplication;
import com.rockchips.mediacenter.localscan.utils.StorageUtils;

/**
 * 
 * 用来处理SHELL命令的共同函数
 * 
 * @author  l00174030
 * @version  [2013-1-11]
 */
public class DeviceDataUtils
{
    private static final String TAG = "LocalScanService";
    private static IICLOG mStLog = IICLOG.getInstance();
    private static HashMap<String, Integer> mStDeviceMountPath = new HashMap<String, Integer>()
    {
        private static final long serialVersionUID = -1847164517848461915L;
        {
           // put(DeviceDataConst.SD_PREFIX_PATH, DeviceType.DEVICE_TYPE_SD);
          //  put(DeviceDataConst.USB_PREFIX_PATH, DeviceType.DEVICE_TYPE_U);
            put(DeviceDataConst.ROCKCHIP_USB_MOUNT_PATH, DeviceType.DEVICE_TYPE_U);
            put(DeviceDataConst.ROCKCHIP_SD_MOUNT_PATH, DeviceType.DEVICE_TYPE_SD);
          //  put(DeviceDataConst.NOT_USB_PREFIX_PATH, DeviceType.DEVICE_TYPE_SD);
          //  put(DeviceDataConst.ZHAOGE_SDCARD_MOUNT_PATH, DeviceType.DEVICE_TYPE_SD);
          //  put(DeviceDataConst.PHONE_EXTSDCARD_PATH, DeviceType.DEVICE_TYPE_SD);
        }
    };
    
    private static final int INVALID = -1;
    /**
     * 通过mount命令构造一条分区设备
     * 
     * @param mountPath
     * @return
     * @see [类、类#方法、类#成员]
     */
    @SuppressLint("DefaultLocale")
	public static LocalDeviceInfo getDeviceFromCmdDetail(String mountPath)
    {
        LocalDeviceInfo deviceDto = null;
        
        // 若挂载路径为空，则数据非法
        if (mountPath == null)
        {
            return null;
        }
        
        deviceDto = new LocalDeviceInfo();
        // 解析SD卡外设
        int deviceType = getDeviceTypeByPath(mountPath);
        if (INVALID == deviceType)
        {            
            return null;
        }
        //得到设备类型
        deviceDto.setDeviceType(deviceType);
        // 挂载路径
        deviceDto.setMountPath(mountPath);
        // 解析物理设备ID
        deviceDto.setPhysicId(getPhysicDevId(mountPath));
        
        return deviceDto;
    }
    
    /**
     * 识别设备及其对应的分区
     * 对设备分区进行处理，抽象出设备信息
     * @param deviceList 本地设备的信息列表
     * @see [类、类#方法、类#成员]
     */
    public static void getAbstractDevice(List<LocalDeviceInfo> deviceList)
    {
        if (deviceList == null || deviceList.size() == 0)
        {
            return;
        }
        
        // 保存物理设备
        List<LocalDeviceInfo> physicDevList = new ArrayList<LocalDeviceInfo>();
        String mountPath = null;
        String physicDevId = null;
        // 保存已创建的物理设备，避免重复创建
        HashMap<String, String> phyDevMap = new HashMap<String, String>();
        // 物理设备
        LocalDeviceInfo physicDevDto = null;
        
        // 通过比较挂载分区，把相同物理设备的分区归属到一个设备
        for (LocalDeviceInfo deviceDto : deviceList)
        {
            // 获取分区的挂载路径
            mountPath = deviceDto.getMountPath();
            if (mountPath == null)
            {
                continue;
            }
            
            // 设置物理设备标识/mnt/sda/sda1 -> sda
            physicDevId = deviceDto.getPhysicId();
            if (physicDevId == null || physicDevId.trim().length() == 0)
            {
                continue;
            }
            
            // 检查是否是重复创建，是则不创建
            if (phyDevMap.containsKey(physicDevId))
            {
                continue;
            }
            else
            {
                phyDevMap.put(physicDevId, physicDevId);
            }
            
            // 不存在则，创建一个物理设备，区别于分区
            physicDevDto = new LocalDeviceInfo();
            
            // 保存设备类型SD/USB
            physicDevDto.setDeviceType(deviceDto.getDeviceType());
            // 设置其为物理设备
            physicDevDto.setIsPhysicDev(DeviceDataConst.PHYSIC_DEVICE_TYPE);
            // 设置物理设备标识
            physicDevDto.setPhysicId(physicDevId);
            
            physicDevList.add(physicDevDto);
        }
        
        // 保存物理设备到设备列表
        for (LocalDeviceInfo deviceDto : physicDevList)
        {
            deviceList.add(deviceDto);
        }
    }
    
    /**
     * 从mount path中获取物理设备的ID
     * /mnt/sda/sda1 -> sda
     * 
     * @param mountPath 挂载路径
     * @return 物理设备的ID
     * @see [类、类#方法、类#成员]
     */
    public static String getPhysicDevId(String mountPath)
    {
        // 通过/分割挂载路径
        String[] pathSplit = mountPath.split(DeviceDataConst.MOUNT_PATH_SPLITER);
        if(pathSplit == null || pathSplit.length == 0)
        	return null;
        //if(pathSplit.length < 4 && INVALID == getDeviceTypeByPath(mountPath))
        //	return null;
        // modify by DTS2014102300884 解决挂载多个外置存储，媒体中心只展示一个USB设备:usb_storage
        if (StorageUtils.isMountSdCard(LocalScanApplication.getInstance(), mountPath)
        		|| StorageUtils.isMountUsb(LocalScanApplication.getInstance(), mountPath))
        {
        	//返回最后一个挂载路径
            return pathSplit[pathSplit.length - 1];
        }
        // 设置物理设备标识/mnt/sda/sda1 -> sda
        else if (pathSplit.length >= 3)
        {
            return pathSplit[2];
        }        
        else
        {
            return null;
        }
    }
    
    /**
     * 根据分区路径构造文件表及目录表名
     * /mnt/sda/sda1 -> _mnt_sda_sda1_Files/ _mnt_sda_sda1_Folders
     * @param path
     * @return
     * @see [类、类#方法、类#成员]
     */
    public static String createTableName (String path)
    {
        return path.replace(' ', '_').replace('/', '_').replace('-', '_');
    }
    
    /**
     * 在本地扫描完成后/处理完后，向界面发送刷新消息
     * 
     * @param type 类型 1：设备上线 2：设备下线 3：设备文件刷新
     * @param mountPath 挂载路径
     * @param context 上下文
     * @see [类、类#方法、类#成员]
     */
    public static void sendBroadcastToMyMedia(int type, String mountPath, Context context)
    {
        // 无上下文则直接退出
        if (context == null)
        {
            return;
        }
        
        // 创建广播
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        
        // 向上传递挂载路径
        bundle.putString(DeviceDataConst.EXTRA_RESERVE, mountPath);
        bundle.putInt(DeviceDataConst.EXTRA_DEVICE_ID, Constant.DeviceType.DEVICE_TYPE_U);
        intent.putExtras(bundle);
        
        switch (type)
        {
            // 1：设备上线
            case DeviceDataConst.BC_TYPE_DEV_UP:
                intent.setAction(DeviceDataConst.ACTION_ON_DMS_UP);
                break;
            //  2：设备下线
            case DeviceDataConst.BC_TYPE_DEV_DOWN:
                intent.setAction(DeviceDataConst.ACTION_ON_DMS_DOWN);
                break;
            // 3：设备文件刷新
            case DeviceDataConst.BC_TYPE_DEV_UPDATE:
                intent.setAction(DeviceDataConst.ACTION_ON_DMS_BROWSE_RESULT);
                break;
            default:
                return;
        }

        // 发送广播
        context.sendBroadcast(intent, Constant.MEDIACENTER_PERMISSION);
        
        mStLog.d("SendBroadcastToMyMedia", "BroadCast type = " + type + " path = " + mountPath);
    }
    
    public static int getDeviceTypeByPath(String mountPath)
    {
    	if(StorageUtils.isMountSdCard(LocalScanApplication.getInstance(), mountPath))
    		return DeviceType.DEVICE_TYPE_SD;
    	else if(StorageUtils.isMountUsb(LocalScanApplication.getInstance(), mountPath))
    		return DeviceType.DEVICE_TYPE_U;
        //Set<String> keys = mStDeviceMountPath.keySet();
        //Iterator<String> iterator = keys.iterator();
        //while (iterator.hasNext())
        //{
        //    String path = iterator.next();
        //    if (mountPath.startsWith(path))
        //    {
        //        return mStDeviceMountPath.get(path);
        //    }
        //}
        return INVALID;
    }
    
    public static String[] getDeviceMountPath()
    {
        Set<String> keys = mStDeviceMountPath.keySet();
        String[] arrays = new String[mStDeviceMountPath.size()];
        return keys.toArray(arrays);
    }
}
