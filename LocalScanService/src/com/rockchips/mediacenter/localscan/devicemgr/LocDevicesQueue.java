/**
 * Title: LocMessageQueue.java<br>
 * Package: com.rockchips.mediacenter.localscan.base<br>
 * Description: TODO<br>
 * @author c00224451
 * @version v1.0<br>
 * Date: 2014-6-27上午10:02:17<br> 
 * Copyright © Huawei Technologies Co., Ltd. 2014. All rights reserved.
 */

package com.rockchips.mediacenter.localscan.devicemgr;

import com.rockchips.mediacenter.basicutils.util.MountMsgQueue;

/**
 * Description: TODO<br>
 * @author c00224451
 * @version v1.0
 * Date: 2014-6-27 上午10:02:17<br>
 */
public final class LocDevicesQueue extends MountMsgQueue
{    
    private static LocDevicesQueue mStLocDevicesQueue;
    private static Object mInstanceLock = new Object();
    private LocDevicesQueue()
    {            
        super();
    }
    public static LocDevicesQueue getInstance()
    {
        synchronized (mInstanceLock)
        {
            if (null == mStLocDevicesQueue)
            {
                mStLocDevicesQueue = new LocDevicesQueue();
            }
            return mStLocDevicesQueue;
        }
    } 
}
