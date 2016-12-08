/**
 * Title: ILocalDataListner.java<br>
 * Package: com.rockchips.android.mediacenter.api<br>
 * Description: TODO<br>
 * @author w00190739
 * @version v1.0<br>
 * Date: 2014-7-15上午10:46:23<br> 
 * Copyright © Huawei Technologies Co., Ltd. 2014. All rights reserved.
 */

package com.rockchips.mediacenter.api;
/**
 * Description: 本地设备磁盘的挂载，去挂载、媒体变化的消息处理回调接口<br>
 * @author w00190739
 * @version v1.0
 * Date: 2014-7-15 上午10:46:23<br>
 */

public interface ILocalDataListner
{
    void onDiskMount();
    
    void onDiskUnMount(String unMountPath);
    
    void onMediaChanged();
}


