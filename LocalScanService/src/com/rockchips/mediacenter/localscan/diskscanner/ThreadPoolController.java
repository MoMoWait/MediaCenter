package com.rockchips.mediacenter.localscan.diskscanner;

import com.rockchips.mediacenter.basicutils.util.IICLOG;

/**
 * 
 * 用来控制线程池的退出，不采用遍历的方式
 * 
 * 
 * @author  l00174030
 * @version  [版本号, 2013-4-14]
 */
public class ThreadPoolController
{
    private static final String TAG = "LocalScanService";
    private IICLOG mLog = IICLOG.getInstance();
    // 排他处理
    private final Object mLock = new Object();   
    // 当前的线程数
    private int mCurrThreadCount = 1;
    
    /**
     * 该方法用来通知管理进程，各子线程以及处理完毕，可以进行下面的操作。
     * 
     * @see [类、类#方法、类#成员]
     */
    public void notifyParent()
    {
        synchronized (mLock)
        {
            if (mCurrThreadCount >= DiskScanner.THREAD_NUM)
            {
                mLog.i(TAG, "all of the thread is stopped, notify main.");
                mLock.notifyAll();
            }
            else
            {
                mCurrThreadCount++;
                mLog.i(TAG, "one of the thread is stopped, index = " + (mCurrThreadCount - 1));
            }
        }
    }
    
    /**
     * 主线程调用该方法进行等待子线程的完成
     * 
     * @see [类、类#方法、类#成员]
     */
    public void waitParent()
    {
        synchronized (mLock)
        {
            // 多个线程时，需要等待所有子线程处理完毕
            while (mCurrThreadCount < DiskScanner.THREAD_NUM)
            {
                try
                {
                    mLog.i(TAG, "main scan thread is waiting");
                    mLock.wait();
                }
                catch (InterruptedException e)
                {
                    mLog.e(TAG, "main scan thread wait exception.");
                }
            }       
            
            mLog.i(TAG, "main scan thread is going on");
        }
    }
    
    
}
