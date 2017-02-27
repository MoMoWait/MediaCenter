package com.rockchips.mediacenter.data;
/**
 * 
 * 本地设备的一些相关常量
 * 
 * @author  l00174030
 * @version  [2013-1-9]
 */
public final class DeviceDataConst
{
    private DeviceDataConst()
    {
        
    }
    // 半角空格
    public static final String STR_BLANK = " ";
    
    // SD卡挂载路径
    public static final String SD_PREFIX_PATH = "/mnt/mmcblk";
    
    // USB外设挂载路径
    public static final String USB_PREFIX_PATH = "/mnt/sd";
    
    // 排除虚拟的sdcard目录
    public static final String NOT_USB_PREFIX_PATH = "/mnt/sdcard";
    
    //朝歌的Sdcard挂载路径
    public static final String ZHAOGE_SDCARD_MOUNT_PATH = "/mnt/extSdCard";
    
    public static final String ROCKCHIP_USB_MOUNT_PATH = "/mnt/usb";
    
    public static final String ROCKCHIP_SD_MOUNT_PATH = "/mnt/external_sd";
    
    //手机sdcard挂载路径
    public static final String PHONE_EXTSDCARD_PATH = "/mnt/ext_sdcard";
    
    //手机内置sdcard挂载路径
    public static final String PHONE_STORAGE_PATH = "/storage/emulated";
    
    // 分割DF命令的字符串
    public static final String DF_CMD_SPLITER = "##";
    
    // 标识物理设备，默认为0表示是分区
    public static final int PHYSIC_DEVICE_TYPE = 1;
    
    // 是否已做文件内容扫描，0标识未作
    public static final int ALREADY_SCANED_N = 0;
    
    // 是否已做文件内容扫描，1标识已作
    public static final int ALREADY_SCANED_Y = 1;
    
    // 分割mount path
    public static final String MOUNT_PATH_SPLITER = "/";
    
    // 挂载分区
    public static final String MOUNT_ROOT_PATH = "/mnt/";
    
    // 空字符串
    public static final String BLANK_STRING = "";
    
    // 文件协议file://
    public static final String FILE_PROTOCOL = "file://";
    
    // 设备类型：物理设备
    public static final String DEVICE_TYPE_PHY = "PhysicDev";
    
    // 设备类型：分区
    public static final String DEVICE_TYPE_DISK = "Disk";
    
    // mount命令
    public static final String BUSY_BOX_MOUNT = "/system/busybox/bin/busybox mount";
    
    // DF命令
    public static final String BUSY_BOX_DF = "/system/busybox/bin/busybox df -h";
    
    // 临时分区
    public static final String TEMP_FS = "tmpfs";
    
    // 1：设备上线 2：设备下线 3：设备文件刷新
    public static final int BC_TYPE_DEV_UP = 1;
    public static final int BC_TYPE_DEV_DOWN = 2;
    public static final int BC_TYPE_DEV_UPDATE = 3;
    
    /**
     * // 设备ID，通过该ID可以唯一标识一台DMS设备
     */
    public static final String EXTRA_RESERVE = "com.rockchips.dlna.service.extra.reserve";
    
    /**
     * 设备ID
     */
    public static final String EXTRA_DEVICE_ID = "com.rockchips.dlna.service.extra.deviceID";
    
    /**
     * // 设备上线通知
     */
    public static final String ACTION_ON_DMS_UP = "com.rockchips.dlna.service.action.OnDMSUp";

    /**
     * // 设备下线通知
     */
    public static final String ACTION_ON_DMS_DOWN = "com.rockchips.dlna.service.action.OnDMSDown";
    
    /**
     * // 特定DMS设备内容更新通知
     */
    public static final String ACTION_ON_DMS_BROWSE_RESULT = "com.rockchips.dlna.service.action.OnDMSBrowseResult";
    
}
