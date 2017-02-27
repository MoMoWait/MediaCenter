/**
 * 
 * com.rockchips.iptv.stb.dlna.util
 * performance.java
 * 
 * 2011-10-13-上午11:21:26
 * Copyright 2011 Huawei Technologies Co., Ltd
 * 
 */
package com.rockchips.mediacenter.utils;

import android.util.Log;

/**
 * 
 * performance
 * 性能输出工具类
 * 
 * 使用方法example:
 * performance pf = new performance("performance Name");
 * pf.start();
 * ...你测试的代码段
 * pf.end();
 * 
 * 2011-10-13 上午11:21:26
 * 
 * @version 1.0.0
 * 
 */
public class Performance
{
    private static final String TAG = "performance";
    
    private String perfName = null;
    
    public long start = 0;
    
    /**
     *constructor performance.
     *
     */
    public Performance(String strName)
    {
        if (strName == null)
        {
            perfName = "UNKNOWN NAME";
        }
        else
        {
            perfName = strName;
        }
    }
    
    /**
     *constructor performance.
     *
     */
    public Performance()
    {
        perfName = "UNKNOWN NAME";
    }
    
    public final long start()
    {
        start = System.currentTimeMillis();
        Log.d(TAG, "["+ perfName + " SAY]: "+"start timer now : " + start + "ms");
        return start;
    }
    
    /**
     * 
     * end
     * 结束计数
     * 返回从start 到 end的时间间隔
     * @return 
     *long
     * @exception
     */
    public final long end()
    {
        long end = System.currentTimeMillis();
        long wentBy = end - start;
        Log.d(TAG, "["+ perfName + " SAY]: "+"went by : " + wentBy + "ms ---- end: " + end + "ms - start: " + start + "ms");
        
        start = end;
        
        return wentBy;
    }
    
}
