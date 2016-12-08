package com.rockchips.mediacenter.localscan.devicemgr;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.StatFs;

import com.rockchips.mediacenter.basicutils.bean.LocalDeviceInfo;
import com.rockchips.mediacenter.basicutils.util.DiskUtil;
import com.rockchips.mediacenter.basicutils.util.IICLOG;
import com.rockchips.mediacenter.localscan.R;

/**
 * 
 * 获取本地外设的信息
 * 通过解析mount命令的输出来发现外设信息
 * 
 * @author  l00174030
 * @version  [2013-1-7]
 */
public class DevDataRetriByCmd implements IDeviceDataRetriever
{
    private static final String TAG = "LocalScanService";
    private IICLOG mLog = IICLOG.getInstance();
    
    // 用来国际化
    private Context mContext;
    
    // 获取mountService信息
//    MountManagerEx mMountManagerEx;
    
    public DevDataRetriByCmd(Context context)
    {
        mContext = context;
        
        // 获取mountService信息
//        mMountManagerEx = new MountManagerEx();
//        if (mMountManagerEx != null)
//        {
//            // 获取挂载的磁盘信息
//            mMountManagerEx.getMountInfoList();
//        }
    }
    
    /**
     * 获取本地外设的信息
     * 该方法为外设信息的接口，具体的获取外设信息的方式均从此处继承
     * 
     * @return 所有外设的具体列表
     * @see [类、类#方法、类#成员]
     */
    @Override
    public List<LocalDeviceInfo> getDeviceData()
    {
        mLog.d(TAG, "start to getDeviceData() by shell command");
        // 执行mount命令，获取mount命令的输出
        // 改用MountService接口获取路径
        //String[] mountOut = execueteCmd(DeviceDataConst.BUSY_BOX_MOUNT);
        List<LocalDeviceInfo> deviceList = dealWithMountOutput();
        
        // 执行df命令，获取分区的使用情况
        //String[] dfOut = execueteCmd(DeviceDataConst.BUSY_BOX_DF);
        //dealWithDfOutput(deviceList, dfOut);
        /**DF命令有挂死，采用StatFs代替**/
        dealWithDfOutputReplace(deviceList);
        
        // 识别设备及其对应的分区
        DeviceDataUtils.getAbstractDevice(deviceList);
        
        mLog.d(TAG, "end to getDeviceData() by shell command");
        
        return deviceList;
    }
    
    /**
     * 获取本地外设的信息，通过接收设备的上线广播
     * 该方法为外设信息的接口，具体的获取外设信息的方式均从此处继承
     * 
     * @param mountPath 挂载路径
     * @param isNeedPhyDev 是否需要构造抽象设备
     * @return 所有外设的具体列表
     * @see [类、类#方法、类#成员]
     */
    @Override
    public List<LocalDeviceInfo> getDeviceDataByBroadcast(String mountPath, boolean isNeedPhyDev)
    {
        mLog.d(TAG, "start to getDeviceDataByBroadcast() by shell command " + mountPath);
        
        List<LocalDeviceInfo> deviceDtoList = null;
        
        // 根据挂载路径，创建一个分区
        LocalDeviceInfo dto = DeviceDataUtils.getDeviceFromCmdDetail(mountPath);
        if (dto == null)
        {
            return null;
        }
        
        //  添加到设备列表
        deviceDtoList = new ArrayList<LocalDeviceInfo>();
        deviceDtoList.add(dto);
        
        // 通过df命令获取设备的使用情况
        //String[] dfOut = execueteCmd(DeviceDataConst.BUSY_BOX_DF);
        //dealWithDfOutput(deviceDtoList, dfOut);
        /**DF命令有挂死，采用StatFs代替**/
        dealWithDfOutputReplace(deviceDtoList);
        
        // 识别设备及其对应的分区
        if (isNeedPhyDev)
        {
            DeviceDataUtils.getAbstractDevice(deviceDtoList);
        }
        
        mLog.d(TAG, "end to getDeviceDataByBroadcast() by shell command");
        
        return deviceDtoList;
    }
    
    /**
     * 处理mount命令输出
     * mount命令输出格式为：
     * 
     * @param mountOut 命令输出信息
     * @return 本地设备的信息列表
     * @see [类、类#方法、类#成员]
     */
    private List<LocalDeviceInfo> dealWithMountOutput()
    {
//        // 获取MountService上已挂载上的磁盘
//        List<ExtraInfoDto> extraInfoList = mMountManagerEx.getExtraInfoList();
//        if (extraInfoList == null || extraInfoList.size() == 0)
//        {
//            mLog.e(TAG, "has no disk on MountService.");
//            return null;
//        }
//        
//        // 用来保存发现的外设
//        List<LocalDeviceInfo> deviceList = new ArrayList<LocalDeviceInfo>();
//        LocalDeviceInfo deviceDto = null;
//        
//        for (ExtraInfoDto extraInfo : extraInfoList)
//        {
//            if (extraInfo == null)
//            {
//                continue;
//            }
//            
//            // 解析一个分区的数据，第三个为挂载路径
//            deviceDto = DeviceDataUtils.getDeviceFromCmdDetail(extraInfo.getmMountPoint());
//            // USB及SD卡以外的设备不入库
//            if (deviceDto == null)
//            {
//                continue;
//            }
//            else
//            {
//                // 保存UUID等属性
//                deviceDto.setmUUID(extraInfo.getmUUID());
//            }
//            
//            deviceList.add(deviceDto);
//            
//        }
//        
//        return deviceList;
        return null;
    }
        
    /**
     * 替代处理df命令输出，使用StatFs来处理
     * df命令输出格式为：
     * Filesystem   Size   Used   Free   Blksize 
     * @param dfOut 命令输出信息
     * @return 本地设备的信息列表
     * @see [类、类#方法、类#成员]
     */
    private void dealWithDfOutputReplace(List<LocalDeviceInfo> deviceList)
    {
        if (deviceList == null || deviceList.size() == 0)
        {
            mLog.e(TAG, "StatFs begin to get Size, but has no output");
            return;
        }
        
        // 遍历设备列表找到对应的设备
        for (LocalDeviceInfo deviceDto : deviceList)
        {
            getLocDevSizeByStatFs(deviceDto);
        }
    }
    
    /**
     * 通过StatFs获取外设的大小
     * 
     * @param deviceDto 外设
     * @see [类、类#方法、类#成员]
     */
    private void getLocDevSizeByStatFs(LocalDeviceInfo deviceDto)
    {
        // 判断空值情况
        String path = deviceDto.getMountPath();
        if (path == null) 
        {
            mLog.e(TAG, "getLocDevSizeByStatFs getMountPath is null");
            return;
        }
        
        try
        {
            // 此处的MountPath != NULL       
            StatFs stat = new StatFs(path);            
            // 获取块的大小(KB)
            long blockSize = stat.getBlockSizeLong() >> 10;
            // 获取总的块数
            long totalBlocks =  stat.getBlockCountLong();
            // 获取空闲的块数
            long freeBlocks = stat.getFreeBlocksLong();
            // 设置设备的容量及使用情况
            deviceDto.setTotalSize(DiskUtil.getDiskSizeStringL(mContext, blockSize * totalBlocks, R.string.unknown, 
                    R.string.unit_disk_size_kb, R.string.unit_disk_size_mb, R.string.unit_disk_size_gb, R.string.unit_disk_size_tb));
            deviceDto.setUsedSize(DiskUtil.getDiskSizeStringL(mContext, blockSize * (totalBlocks - freeBlocks), R.string.unknown, 
                    R.string.unit_disk_size_kb, R.string.unit_disk_size_mb, R.string.unit_disk_size_gb, R.string.unit_disk_size_tb));
            deviceDto.setFreeSize(DiskUtil.getDiskSizeStringL(mContext, blockSize * freeBlocks, R.string.unknown, 
                    R.string.unit_disk_size_kb, R.string.unit_disk_size_mb, R.string.unit_disk_size_gb, R.string.unit_disk_size_tb));
            deviceDto.setUsedPercent("");   
        }
        catch (IllegalArgumentException e)
        {       
            mLog.e(TAG, "getLocDevSizeByStatFs IllegalArgumentException: Invalid path:" + path);
        }
    }
}
