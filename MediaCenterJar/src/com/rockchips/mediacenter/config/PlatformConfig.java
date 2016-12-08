package com.rockchips.mediacenter.config;

import com.rockchips.mediacenter.basicutils.util.PlatformUtil;

public class PlatformConfig
{
    private static final boolean SUPPORT_HISI_MEDIAPLAYER = true;
    
    public static boolean isSupportHisiMediaplayer()
    {
        if (SUPPORT_HISI_MEDIAPLAYER && PlatformUtil.supportHisiMediaPlayerOnJava())
        {
            return true;
        }
        return false;
    }
}
