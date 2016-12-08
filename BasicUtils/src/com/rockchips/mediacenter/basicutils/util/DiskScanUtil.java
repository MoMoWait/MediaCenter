package com.rockchips.mediacenter.basicutils.util;

import com.rockchips.mediacenter.basicutils.constant.DiskScanConst;

/**
 * 
 * 扫描外设具体文件功能相关的常量
 * 
 */
public class DiskScanUtil extends DiskScanConst
{
    
    private DiskScanUtil()
    {
        
    }

    public static boolean isLocalVideo(String ext)
    {
        for (int i = 0; i < VIDEO_SUFFIX.length; i++)
        {
            if (VIDEO_SUFFIX[i].equalsIgnoreCase(ext))
            {
                return true;
            }
        }

        return false;
    }

    public static boolean isLocalAudio(String ext)
    {
        for (int i = 0; i < AUDIO_SUFFIX.length; i++)
        {
            if (AUDIO_SUFFIX[i].equalsIgnoreCase(ext))
            {
                return true;
            }
        }

        return false;
    }

    public static boolean isLocalImage(String ext)
    {
        for (int i = 0; i < IMAGE_SUFFIX.length; i++)
        {
            if (IMAGE_SUFFIX[i].equalsIgnoreCase(ext))
            {
                return true;
            }
        }

        return false;
    }

    public static boolean isLocalSubTitle(String ext)
    {
        for (int i = 0; i < SUBTITLE_SUFFIX.length; i++)
        {
            if (SUBTITLE_SUFFIX[i].equalsIgnoreCase(ext))
            {
                return true;
            }
        }

        return false;
    }
}
