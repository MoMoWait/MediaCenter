/**
 * Title: IDeviceMountListener.java<br>
 * Package: com.rockchips.mediacenter.localscan.manager<br>
 * Description: TODO<br>
 * @author c00224451
 * @version v1.0<br>
 * Date: 2014-7-8上午10:49:58<br> 
 * Copyright © Huawei Technologies Co., Ltd. 2014. All rights reserved.
 */

package com.rockchips.mediacenter.localscan.manager;
/**
 * Description: TODO<br>
 * @author c00224451
 * @version v1.0
 * Date: 2014-7-8 上午10:49:58<br>
 */

public interface IDeviceMountListener
{
    //设备挂载消息回调
    void onDeviceMount(String mountPath);
    //设备卸载消息回调
    void onDeviceUnmount(String mountPath);
}
