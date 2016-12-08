package com.rockchips.mediacenter.localscan.diskscanner;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;

import com.rockchips.mediacenter.basicutils.constant.Constant;
import com.rockchips.mediacenter.basicutils.constant.DiskScanConst;
import com.rockchips.mediacenter.basicutils.util.IICLOG;

/**
 * 
 * 通过Java File文件操作扫描外设
 * 并把相关数据入库
 * 
 * @author  l00174030
 * @version  [2013-4-14]
 */
public class DiskScanner implements IDiskScanner
{
    private static final String TAG = "LocalScanService";
    private IICLOG mLog = IICLOG.getInstance();
    
    // 保存扫描时目录的队列
    private LocFileScanQueue mLocFileScanQueue;
    
    // 开启的线程数
    public static final int THREAD_NUM = 5;
    
    // 线程池
    private ExecutorService mExecutorService;
    
    // 用来保存文件类型，缩短查找文件类型时间
    public static HashMap<String, Integer> fileTypeMap;
    
    // 保存进程上下文，发送设备文件更新广播使用
    private Context mContext = null;
    
    // 用来退出所有线程的标志
    private static boolean mStIsExit = false;
    

    // 用来等待所有线程的退出
    private ThreadPoolController mController;
    
    /**
     * 初始化线程池及信号量
     */
    public DiskScanner(Context context)
    {
        // 初始化文件类型哈希表
        initFileTypeMap();
        mContext = context;
        mController = new ThreadPoolController();
        mLocFileScanQueue = LocFileScanQueue.getInstance();
    }
    
    /**
     * 重新初始化一些变量
     * 
     * @see [类、类#方法、类#成员]
     */
    public void reinit()
    {
        // 启用线程池
        mExecutorService = Executors.newFixedThreadPool(THREAD_NUM);
        // 线程退出控制清空
        mController = new ThreadPoolController();
        // 队列清空
        mLocFileScanQueue.removeAll();
    }
    
    /**
     * 对每一个分区的扫描入口
     * 主要用来启动JNI层扫描，处理扫描数据及入库
     * 
     * @param path 分区路径
     * @see [类、类#方法、类#成员]
     */
    @Override
    public void scanDiskByPath(String path)
    {
        mLog.d(TAG, "start to scan path = " + path);
        
        // just log
        long start = System.currentTimeMillis();
        
        reinit();
        
        // 初始时不退出
        mStIsExit = false;
        
        // 把初始路径入栈
        mLocFileScanQueue.enqueue(path);
        
        // 把线程放入线程池进行管理
        for (int i = 0; i < THREAD_NUM; i++)
        {
            mExecutorService.execute(new FileScanThread(i, path, mContext, mController));
        }
        
        // 关闭线程池，退出扫描
        mController.waitParent();
        mLog.d(TAG, "scan main thread continue.");
        
        // 可能会线程还没处理完成，直接关闭线程池
        mExecutorService.shutdown();
        mLog.i(TAG, "scan path = " + path + " used time = " + (System.currentTimeMillis() - start));
        mLog.d(TAG, "end to scan path = " + path);
    }
    
    /**
     * 初始化文件类型的哈希表
     * 用来保存文件类型，缩短查找文件类型时间,避免多次遍历
     * 
     * @see [类、类#方法、类#成员]
     */
    private synchronized void initFileTypeMap()
    {
        if (fileTypeMap != null)
        {
            return;
        }
        
        // 实例哈希表
        fileTypeMap = new HashMap<String, Integer>();
        
        // 初始化音频文件类型
        int i = 0;
        for (i = 0; i < DiskScanConst.AUDIO_SUFFIX.length; i++)
        {
            fileTypeMap.put(DiskScanConst.AUDIO_SUFFIX[i], Constant.MediaType.AUDIO);
        }
        
        // 初始化视频文件类型
        for (i = 0; i < DiskScanConst.VIDEO_SUFFIX.length; i++)
        {
            fileTypeMap.put(DiskScanConst.VIDEO_SUFFIX[i], Constant.MediaType.VIDEO);
        }
        
        // 初始化图象类型
        for (i = 0; i < DiskScanConst.IMAGE_SUFFIX.length; i++)
        {
            fileTypeMap.put(DiskScanConst.IMAGE_SUFFIX[i], Constant.MediaType.IMAGE);
        }
        
        // 初始化字幕类型
        for (i = 0; i < DiskScanConst.SUBTITLE_SUFFIX.length; i++)
        {
            fileTypeMap.put(DiskScanConst.SUBTITLE_SUFFIX[i], Constant.MediaType.SUBTITLE);
        }
    }
    
    public static boolean ismStIsExit()
    {
        return mStIsExit;
    }

    public static void setmStIsExit(boolean isExit)
    {
        mStIsExit = isExit;
    }
}
