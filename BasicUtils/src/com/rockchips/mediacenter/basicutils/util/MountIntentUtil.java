/**
 * Title: MountIntentUtil.java<br>
 * Package: com.rockchips.mediacenter.basicutils.util<br>
 * Description: TODO<br>
 * @author c00224451
 * @version v1.0<br>
 * Date: 2014年10月23日下午1:58:11<br> 
 * Copyright © Huawei Technologies Co., Ltd. 2014. All rights reserved.
 */

package com.rockchips.mediacenter.basicutils.util;

import android.content.Intent;

/**
 * Description: TODO<br>
 * @author c00224451
 * @version v1.0
 * Date: 2014年10月23日 下午1:58:11<br>
 */

public class MountIntentUtil
{
    public static boolean isDeviceMountIntent(Intent intent)
    {
        if (intent != null && intent.getAction() != null)
        {
            if (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED))
            {
                return true;
            }
        }
        return false;        
    }
    
    public static boolean isDeviceUnmountIntent(Intent intent)
    {
        if (intent != null && intent.getAction() != null)
        {
            if (intent.getAction().equals(Intent.ACTION_MEDIA_UNMOUNTED)
                 || intent.getAction().equals(Intent.ACTION_MEDIA_BAD_REMOVAL)
                 || intent.getAction().equals(Intent.ACTION_MEDIA_REMOVED))
            {
                return true;
            }
        }
        return false;
    }
    
    public static boolean isDeviceIntent(Intent intent)
    {
        if (isDeviceMountIntent(intent) || isDeviceUnmountIntent(intent))
        {
            return true;
        }
        return false;
    }
}
