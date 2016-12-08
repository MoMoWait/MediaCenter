package com.rockchips.mediacenter.localscan.devicemgr;

import java.util.List;

import com.rockchips.mediacenter.basicutils.bean.LocalDeviceInfo;

/**
 * 
 * 获取本地外设的信息接口
 * <功能详细描述>
 * 
 * @author  l00174030
 * @version  [2013-1-7]
 */
public interface IDeviceDataRetriever 
{
    /**
     * 获取本地外设的信息
     * 该方法为外设信息的接口，具体的获取外设信息的方式均从此处继承
     * 
     * @return 所有外设的具体列表
     * @see [类、类#方法、类#成员]
     */
	List<LocalDeviceInfo> getDeviceData();
	
    /**
     * 获取本地外设的信息，通过接收设备的上线广播
     * 该方法为外设信息的接口，具体的获取外设信息的方式均从此处继承
     * 
     * @param mountPath 挂载路径
     * @param isNeedPhyDev 是否需要构造抽象设备
     * @return 所有外设的具体列表
     * @see [类、类#方法、类#成员]
     */
	List<LocalDeviceInfo> getDeviceDataByBroadcast(String mountPath, boolean isNeedPhyDev);
}
