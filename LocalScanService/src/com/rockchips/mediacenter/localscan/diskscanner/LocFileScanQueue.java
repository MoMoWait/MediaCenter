package com.rockchips.mediacenter.localscan.diskscanner;

import java.util.LinkedList;

import com.rockchips.mediacenter.basicutils.util.IICLOG;

/**
 * 
 * 解析的文件目录的路径队列
 * 需要同步，因为在解析时会有多线程来读写该队列
 * 
 * @author  l00174030
 * @version  [2013-1-21]
 */
public final class LocFileScanQueue
{
    private static final String TAG = "LocalScanService";
    private IICLOG mLog = IICLOG.getInstance();
    // 待处理的目录队列
    private LinkedList<String> mFilesList;
    // 用来计数，判断是否是所有的线程均进入了等待状态
    private int mCurrThreadCount;
    //
    private static final long MAX_WAIT_TIME = 15000;
    
    private static LocFileScanQueue mStLocFileScanQueue;
    
    private static Object mInstanceLock = new Object();
    
    public static LocFileScanQueue getInstance()
    {
        synchronized (mInstanceLock)
        {
            if (null == mStLocFileScanQueue)
            {
                mStLocFileScanQueue = new LocFileScanQueue();
            }
            return mStLocFileScanQueue;
        }
    }
    
    private LocFileScanQueue()
    {
        mCurrThreadCount = 0;
        mFilesList = new LinkedList<String>();
    }
    
    /**
     * 向队列中添加数据
     * 
     * @param lsOut 广播详细信息
     * @see [类、类#方法、类#成员]
     */
    public void enqueue(String lsOut)
    {
        synchronized (mFilesList)
        {
            mFilesList.addLast(lsOut);
            mFilesList.notifyAll();
        }
    }
    
    /**
     * 获取队列的第一个元素，并删除该元素
     * 
     * @param isWait 是否进行等待
     * @see [类、类#方法、类#成员]
     */
    public String dequeue(boolean isWait)
    {
        String lsout = null;
        //mLog.i(TAG, "mFilesList contents->" + mFilesList);
        synchronized (mFilesList)
        {
            while (mFilesList.isEmpty())
            {
            	//Thread.sleep(100);
            	
            	mLog.i(TAG, "mFilesList.isEmpty()");
                // 用来判断退出
                mCurrThreadCount++;
                if (mCurrThreadCount >= DiskScanner.THREAD_NUM)
                {
                    DiskScanner.setmStIsExit(true);
                    mFilesList.notifyAll();
                    mLog.i(TAG, "all of the thread is stopped!");
                    return null;
                }
                
                if (!isWait)
                {
                    return null;
                }
                try
                {
                    // Log.D("QUEUE", "has no str wait()");
                    // 已没有intent消息要处理，线程等待，释放资源
                    mFilesList.wait(MAX_WAIT_TIME);
                    // 15S后，按时间被唤醒后，还是没有需要扫描的目录则退出该线程
                    if (mFilesList.isEmpty())
                    {
                        return null;
                    }
                    
                    mCurrThreadCount--;
                }
                catch (InterruptedException e)
                {
                    mLog.e(TAG, "java file scan occur a error when called wait()");
                }
            }
            
            lsout = mFilesList.getFirst();
            mFilesList.removeFirst();
        }
        
        return lsout;
    }
    
    /**
     * 通知其他线程继续
     * 
     * 
     * @see [类、类#方法、类#成员]
     */
    public void notifyOther()
    {
        synchronized (mFilesList)
        {
            mFilesList.notifyAll();
        }
    }
    
    /**
     * 删除所有的数据
     * 
     * @see [类、类#方法、类#成员]
     */
    public void removeAll()
    {
        synchronized (mFilesList)
        {
            while (!mFilesList.isEmpty())
            {
                mFilesList.removeFirst();
            }
            
            mCurrThreadCount = 0;
        }
    }
    
}
