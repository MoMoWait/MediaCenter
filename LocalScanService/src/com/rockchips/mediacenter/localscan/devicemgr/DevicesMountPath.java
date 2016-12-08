/**
 * Title: DevicesMountPath.java<br>
 * Package: com.rockchips.mediacenter.localscan.devicemgr<br>
 * Description: TODO<br>
 * @author c00224451
 * @version v1.0<br>
 * Date: 2014年10月27日下午4:15:09<br> 
 * Copyright © Huawei Technologies Co., Ltd. 2014. All rights reserved.
 */

package com.rockchips.mediacenter.localscan.devicemgr;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Description: TODO<br>
 * @author c00224451
 * @version v1.0
 * Date: 2014年10月27日 下午4:15:09<br>
 */

public class DevicesMountPath
{
    private static Set<String> mountPathList = new HashSet<String>();
    private static Object mLock = new Object();
    
    public static void addMountPath(String mountPath)
    {
        synchronized (mLock)
        {
            if (!mountPathList.contains(mountPath))
            {
                mountPathList.add(mountPath);
            }
        }
    }
    
    public static void removeMountPath(String mountPath)
    {
        synchronized (mLock)
        {
            if (mountPathList.contains(mountPath))
            {
                mountPathList.remove(mountPath);
            }
        }
    }
    
    public static String getMountPath(String path)
    {
        if (path == null || path.length() == 0)
        {
            return null;
        }
                
        synchronized (mLock)
        {
            Iterator<String> iterator = mountPathList.iterator();
            for (; iterator.hasNext();)
            {
                String mountPath = iterator.next();
                if (path.startsWith(mountPath))
                {
                    return mountPath;
                }
            }
            return null;
        }
    }
}
