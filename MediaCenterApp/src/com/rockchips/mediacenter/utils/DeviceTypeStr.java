package com.rockchips.mediacenter.utils;

import android.content.Context;

import com.rockchips.mediacenter.R;
import com.rockchips.mediacenter.data.ConstData;

public class DeviceTypeStr
{
    public static String getDevTypeStr(Context context, int type)
    {
        String devType;
        switch (type)
        {
            case ConstData.DeviceType.DEVICE_TYPE_SD:
            	devType = context.getResources().getString(R.string.sd);
            	break;
            case ConstData.DeviceType.DEVICE_TYPE_U:
                devType = context.getResources().getString(R.string.usb_device);
                break;
            case ConstData.DeviceType.DEVICE_TYPE_INTERNEL_STORAGE:
            	 devType = context.getResources().getString(R.string.internel_storage);
            	break;
            case ConstData.DeviceType.DEVICE_TYPE_DMS:
                devType = context.getResources().getString(R.string.dlna_device);
                break;
            case ConstData.DeviceType.DEVICE_TYPE_NFS:
            	 devType = context.getResources().getString(R.string.nfs_device);
            	break;
            case ConstData.DeviceType.DEVICE_TYPE_SMB:
           	 	devType = context.getResources().getString(R.string.smb_device);
           	 	break;
            case ConstData.DeviceType.DEVICE_TYPE_UNKNOWN:
            default:
                devType = context.getResources().getString(R.string.unknown_device);
                break;
        }
        
        return devType;
    }
}
