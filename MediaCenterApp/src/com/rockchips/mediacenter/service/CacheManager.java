/**
 * com.rockchips.mediacenter.basicutils.widget
 * ThumbnailManager.java
 * Description: 媒体中心USB外接设备的内容展示/媒体中心DLNA设备的内容展示<br>
 * @author w00190739
 * @version v1.0<br>
 * Date: 2014-7-15下午4:07:16<br> 
 * Copyright © Huawei Technologies Co., Ltd. 2014. All rights reserved.
 */

package com.rockchips.mediacenter.service;

import java.io.File;
import java.io.IOException;

import com.rockchips.mediacenter.utils.DiskUtil;
import com.rockchips.mediacenter.utils.IICLOG;
import com.rockchips.mediacenter.utils.Utils;

/**
 * Description: 外接媒体设备内容浏览和缓存机制/DLNA设备内容浏览、刷新和缓存机制<br>
 * @author w00190739
 * @version v1.0 Date: 2014-7-15 下午4:07:16<br>
 */
public class CacheManager
{
    private static final String TAG = "CacheManager";

    private static final IICLOG Log = IICLOG.getInstance();

    public final static String CACHE_FILE_TAG = "mymedia_";

    //cache分区剩余临界值，超过该值进行清理工作
    public final static int FREE_SIZE_CRITICAL_SIZE_MB = 300;
    
    //默认清理多少天前的cache
    public final static int DEL_CACHE_DAYS_BEFORE = 7;
    /**
     * 单例 <功能详细描述>
     * 
     * @return 单例的CacheManager
     * @see [类、类#方法、类#成员]
     */
    private static CacheManager mCacheManager = null;

    public static synchronized CacheManager getInstance()
    {
        if (mCacheManager == null)
        {
            mCacheManager = new CacheManager();
        }
        return mCacheManager;
    }

    private String mCacheDir = null;

    /**
     * DLNA所有相关应用共享的缓存， 使用sharedUserId="com.rockchips.iptv.stb.dlna"
     */
    private static final String SYSTEM_CACHE_DIR = "/data/data/com.rockchips.mediacenter/cache/";

    private CacheManager()
    {
        File f = new File(SYSTEM_CACHE_DIR + "test");
        try
        {
            if (null != f)
            {
                f.getParentFile().mkdirs();
                f.createNewFile();
            }
            mCacheDir = SYSTEM_CACHE_DIR;

        }
        catch (IOException e)
        {
            Log.e(TAG, "CacheManager IOException ");
            mCacheDir = SYSTEM_CACHE_DIR;// DLNA_DATACACHE_DIR;
        }
    }

    public String getCacheDir()
    {
        return mCacheDir;
    }

    public String getCacheFilePath(String strTobeHashed)
    {
    	return mCacheDir + strTobeHashed.hashCode();
    }
    
    public String getCacheBitmapPath(String url, int width, int height)
    {
        StringBuffer pathStrBuf = new StringBuffer();
        pathStrBuf.append(CacheManager.getInstance().getCacheDir());
        pathStrBuf.append(CacheManager.CACHE_FILE_TAG);
        pathStrBuf.append(width).append("x").append(height);
        pathStrBuf.append("_").append(url.hashCode()).append("_");
        pathStrBuf.append(Utils.Crc64(url));
        
        return String.valueOf(pathStrBuf);
    }
    
    public boolean delCacheFile(String strTobeHashed)
    {
    	File file = new File(getCacheFilePath(strTobeHashed));
        if (file.exists())
        {
            file.delete();
            return true;
        }
        return false;
    }
    /**
     * caller can select dir <功能详细描述>
     * 
     * @param dir
     * @see [类、类#方法、类#成员]
     */
    public void setCacheDir(String dir)
    {
        if (dir != null)
        {
            mCacheDir = dir;
        }
    }


    /**
     * 时间转换: 天与毫秒
     * 
     * @param days
     * @return
     */
    private long convertDaysToMilliseconds(int days)
    {
        return days * 24L * 3600 * 1000;
    }

    /**
     * 清理目录中过期的文件
     * @param file 文件
     * @param days 过期天数
     * @return
     */
    private void clearOverdueFile(File file, int days)
    {
        int myDays = days;
        long delayTime = convertDaysToMilliseconds(myDays);

        // 判断文件是否存在
        if (file != null && file.exists())
        {
            // 判断是否是文件
            if (file.isFile())
            {
                Log.d(TAG, "filename=" + file.getName());
                if (file.getName() != null && file.getName().startsWith(CACHE_FILE_TAG))
                {
                    long lastTime = file.lastModified();
                    long curtTime = System.currentTimeMillis();
                    // 根据其最后的修改时间，判断文件文件是否过期
                    if (delayTime == 0 || curtTime - lastTime > delayTime)
                    {
                        Log.d(TAG, "delete filename=" + file.getName());
                        file.delete();
                    }
                }
            }
            // 否则如果它是一个目录
            else if (file.isDirectory())
            {
                // 声明目录下所有的文件 files[];
                File[] files = file.listFiles();
                if (files != null)
                {
                    for (int i = 0; i < files.length; i++)
                    {
                        // 遍历目录下所有的文件
                        clearOverdueFile(files[i], myDays); // 把每个文件 用这个方法进行迭代
                    }
                }
            }
        }
    }
    
    /**
     * 清理默认data cache目录中过期的文件
     * @param days 过期天数
     * @return
     */
    public void clearCacheDirectory(int daysBefore)
    {
    	File cacheDir = new File(SYSTEM_CACHE_DIR);
    	clearOverdueFile(cacheDir,daysBefore);
    }
    
    /**
     * 清理默认data cache目录中所有的文件
     * @param days 过期天数
     * @return
     */
    public void clearAllCache()
    {
    	clearCacheDirectory(0);
    }
    
    public void clearOverdueCache()
    {
    	clearCacheDirectory(DEL_CACHE_DAYS_BEFORE);
    }
    
    public boolean checkCacheable()
    {
    	boolean cacheAble = (DiskUtil.getDataFreeSizeInMB() > FREE_SIZE_CRITICAL_SIZE_MB);
      	return cacheAble;
    }
    /**
     * 清理默认data cache目录中所有的文件
     * @param days 过期天数
     * @return
     */
    public void doCacheClear()
    {
    	//first 
    	clearOverdueCache();
    	if(!checkCacheable())
    	{
    		clearAllCache();
    	}
    		
    }
}
