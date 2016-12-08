/**
 * Title: LocDevScanManager.java<br>
 * Package: com.rockchips.mediacenter.localscan.manager<br>
 * Description: TODO<br>
 * @author c00224451
 * @version v1.0<br>
 * Date: 2014-6-26下午5:01:25<br> 
 * Copyright © Huawei Technologies Co., Ltd. 2014. All rights reserved.
 */

package com.rockchips.mediacenter.localscan.manager;

import java.util.ArrayList;

import android.content.Context;

import com.rockchips.mediacenter.basicutils.constant.DiskScanConst;
import com.rockchips.mediacenter.basicutils.util.IICLOG;
import com.rockchips.mediacenter.localscan.database.table.TableFileAndFolderManager;
import com.rockchips.mediacenter.localscan.diskscanner.DiskScanner;
import com.rockchips.mediacenter.localscan.diskscanner.IDiskScanController;
import com.rockchips.mediacenter.localscan.diskscanner.IDiskScanner;
import com.rockchips.mediacenter.localscan.diskscanner.LocFileScanQueue;

/**
 * Description: TODO<br>
 * @author c00224451
 * @version v1.0
 * Date: 2014-6-26 下午5:01:25<br>
 */

public final class LocDevScanManager implements IDiskScanController, Runnable
{
    private static final String TAG = "LocalScanService";
    private IICLOG mLog = IICLOG.getInstance();
    private static LocDevScanManager mStLocDevScanManager;
    
    private static Object mInstanceLock = new Object();
    // 是否正在扫描
    public boolean mIsScaning;
    // 用来加锁操作
    public String mIsScanFlag = ""; 
    // 保存将要扫描的路径
    private ArrayList<String> mScanPathList = new ArrayList<String>();
    // 磁盘扫描器
    private IDiskScanner mDiskScanner;
    // 控制其是否继续还是停止
    private static boolean mStIsStop;

    // 当前正在扫描的路径
    private String mCurrentScanPath;
    
    private Context mContext;    
    private TableFileAndFolderManager mTableFileAndFolderManager;
    
    private LocDevScanManager(Context context)
    {        
        // 保存进程上下文，发送设备文件更新广播使用
        mContext = context;
        mTableFileAndFolderManager = TableFileAndFolderManager.getInstance(mContext);        
        mDiskScanner = new DiskScanner(mContext);        
        init();
    }
    
    public static LocDevScanManager getInstance(Context context)
    {
        synchronized (mInstanceLock)
        {
            if (null == mStLocDevScanManager)
            {
                mStLocDevScanManager = new LocDevScanManager(context);
            }
            return mStLocDevScanManager;
        }
    }
    private void init()
    {
        mCurrentScanPath = null;
        mStIsStop = false;
        synchronized (mIsScanFlag)
        {
            mIsScaning = false;
        }
    }

    /**
     * 开始磁盘扫描
     * 
     * @param path 扫描的分区路径
     * @see [类、类#方法、类#成员]
     */
    @Override
    public void startDiskScan(String path)
    {
        // 检查扫描路径合法性
        if (path == null)
        {
            mLog.e(TAG, "Invalid scan path NULL to scan ");
            return;
        }
        
        // 添加到等待队列 
        addScanPath(path);
        
        // 锁住isScaning后再判断，防止多次打开扫描线程
        synchronized (mIsScanFlag)
        {
            // 没有扫描，则开始扫描
            if (!mIsScaning)
            {
                mIsScaning = true;
                // 开始扫描
                (new Thread(this, DiskScanConst.THREAD_NAME_JAVA)).start();
            }              
        }
    }

    /**
     * 停止磁盘扫描
     * 
     * @param path 将要停止扫描的分区路径
     * @see [类、类#方法、类#成员]
     */
    @Override
    public void stopDiskScan(String path)
    {
        // 检查扫描路径合法性
        if (path == null)
        {
            mLog.e(TAG, "Invalid scan path NULL to scan ");
            return;
        } 
        
        // 是否是当前正在扫描的路径
        if (path.equals(mCurrentScanPath))
        {
            mLog.i(TAG, "stop scan " + path);
            setmStIsStop(true);
            LocFileScanQueue.getInstance().notifyOther();
            return;
        }
        
        // 是否是等待队列的路径
        synchronized (mScanPathList)
        {
            for (int i = 0; i < mScanPathList.size(); i++)
            {
                if (path.equals(mScanPathList.get(i)))
                {
                    mLog.i(TAG, "remove from scan queue " + path);
                    mScanPathList.remove(i);
                    return;
                }
            }
        }
    }
    
    /**
     * 设置具体的磁盘扫描器
     * 
     * @param diskScanner 磁盘扫描器
     * @see [类、类#方法、类#成员]
     */
    public void setDiskScanner(IDiskScanner diskScanner)
    {
        this.mDiskScanner = diskScanner;
    }
    
    /**
     * 获取具体的磁盘扫描器
     *
     * @return 磁盘扫描器
     * @see [类、类#方法、类#成员]
     */
    public IDiskScanner getDiskScanner()
    {
        return mDiskScanner;
    }
    
    /**
     * 扫描路径加入等待队列
     * 此处要考虑初始与receiver的冲突
     * 
     * @param path 扫描路径
     * @see [类、类#方法、类#成员]
     */
    private void addScanPath(String path)
    {
        // 是否是当前正在扫描的分区　||　是否已加入等待队列
        if (path == null || hasAlreadyExist(path))
        {
            //Log.I(TAG, path + " has already exist");
            return;
        }        
        
        // 是否已经扫描过（数据库查询），已扫描则不再入等待队列
        if (LocDevicesManager.getInstance(mContext).isScanned(path))
        {
            mLog.i(TAG, path + " has already scanned");
            return;
        }
        
        // 未扫描，加入等待队列
        synchronized (mScanPathList)
        {
            mScanPathList.add(path);
        }
        
        mLog.i(TAG, path + " has added to sacn queue");
    }
    
    /**
     * 获取一个扫描路径
     * 
     * @return path 扫描路径
     * @see [类、类#方法、类#成员]
     */
    public String getScanPath()
    {
        String path = null;
        
        synchronized (mScanPathList)
        {
            if (mScanPathList.size() == 0)
            {
                mLog.d(TAG, "Scan path is empty ");
                // 更新当前正在扫描的路径
                mCurrentScanPath = path;
                return null;
            }
            
            path = mScanPathList.get(0);
            
            // 更新当前正在扫描的路径
            mCurrentScanPath = path;
            
            // 删除已取出的路径
            mScanPathList.remove(0);
        }
        
        return path;
    }
    
    /**
     * 是否是当前正在扫描的分区
     * 是否已加入等待队列
     * 
     * @param path 路径
     * @return true：已存在不用再加入
     * @see [类、类#方法、类#成员]
     */
    private boolean hasAlreadyExist(String path)
    {
        // 是否是当前正在扫描的分区
        if (path.equals(mCurrentScanPath))
        {
            mLog.i(TAG, path + " is scanning");
            return true;
        }
        
        // 是否已加入等待队列
        synchronized (mScanPathList)
        {
            for (int i = 0; i < mScanPathList.size(); i++)
            {
                if (path.equals(mScanPathList.get(i)))
                {
                    mLog.i(TAG, path + " has already exist in scan queue");
                    return true;
                }
            }
        }
        
        return false;
    }

    @Override
    public void run()
    {               
     // 获取一个分区
        String path = null;
        
        while (true)
        {
            // 没开始扫描一个分区前，把停止位复位
            setmStIsStop(false);            
            // 获取扫描路径
            path = getScanPath();
            
            // 路径为空，则退出
            if (path == null)
            {
                mLog.i(TAG, "scan disk end ");
                break;
            }                        
            mLog.i(TAG, "scan disk " + path);            
            // 扫描
            mDiskScanner.scanDiskByPath(path);
        } 
        
        // 扫描线程结束时，正在扫描标志位复位
        synchronized (mIsScanFlag)
        {
            mIsScaning = false;
        }
        
        mLog.i(TAG, "end JavaLayerScanner Thread.");
    }
    
    public void createTableFileAndFolder(String mountPath)
    {
        // 创建分区对应的文件表与文件夹表
        mTableFileAndFolderManager.createFileAndDirTable(mountPath);
    }
    
    public static boolean ismStIsStop()
    {
        return mStIsStop;
    }

    public static void setmStIsStop(boolean isStop)
    {
        mStIsStop = isStop;
    }
}
