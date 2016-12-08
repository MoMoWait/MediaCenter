/**
 * 
 * com.rockchips.iptv.stb.dlna.util
 * Math.java
 * 
 * 2012-1-31-下午03:22:07
 * Copyright 2012 Huawei Technologies Co., Ltd
 * 
 */
package com.rockchips.mediacenter.basicutils.util;

/**
 * 
 * Math
 * 
 * 2012-1-31 下午03:22:07
 * 
 * @version 1.0.0
 * 
 */
public class MathUtil
{
    /**
     * 转换 百分比 为数值;
     * 
     * 如果 ratio 在 (0,1)内 转换,否则直接返回 ratio
     * 如果ratio < 0表示值无效;返回 -1;
     * ConvertPercentageToValue
     * 
     * @param ratio
     * @param duration
     * @return 
     *int
     * @exception
     */
    public static int ConvertPercentageToValue(float ratio, int duration)
    {
        if(ratio < 0)
        {
            return -1;
            
        }else if(ratio >= 1)
        {
            return (int)ratio;
        }else
        {
            return (int)(duration * ratio);
        }
        
    }
}
