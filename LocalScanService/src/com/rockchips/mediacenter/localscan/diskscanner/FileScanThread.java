package com.rockchips.mediacenter.localscan.diskscanner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.rockchips.mediacenter.basicutils.bean.LocalMediaInfo;
import com.rockchips.mediacenter.basicutils.constant.Constant;
import com.rockchips.mediacenter.basicutils.constant.DeviceDataConst;
import com.rockchips.mediacenter.basicutils.constant.DiskScanConst;
import com.rockchips.mediacenter.basicutils.util.IICLOG;
import com.rockchips.mediacenter.basicutils.util.StringUtils;
import com.rockchips.mediacenter.localscan.database.table.TableFileAndFolderManager;
import com.rockchips.mediacenter.localscan.devicemgr.DeviceDataUtils;
import com.rockchips.mediacenter.localscan.manager.LocDevScanManager;

/**
 * 
 * 扫描目录的线程，只扫描当前目录，目录下的目录入队列
 * 
 * @author  l00174030
 * @version  [版本号, 2013-4-14]
 */
public class FileScanThread implements Runnable
{
    private static String TAG = "LocalScanService";
    private IICLOG mLog = IICLOG.getInstance();
    
    // 文件
    private File mFile = null;
    // 队列
    private LocFileScanQueue mLocQueue;
    // 目录列表
    private List<LocalMediaInfo> mFolderList = null;
    // 文件列表
    private List<LocalMediaInfo> mFileList = null;
    // 解析文件时保存文件信息
    LocalMediaInfo mFileInfo = null;
    // 解析目录时保存目录信息
    LocalMediaInfo mFolderInfo = null;
    // 解析的文件计数，用来批量入库
    private int mFileCount;
    // 解析的目录计数，用来批量入库
    private int mFolderCount;
    // 文件/目录表管理
    private TableFileAndFolderManager mTableFileAndFolder;
    // 用来计数，发送设备文件更新广播使用
    private int mMsgCount;
    // 根目录
    private String mRootPath;
    // 保存进程上下文，发送设备文件更新广播使用
    private Context mContext;
    // 用来等待所有线程的退出
    private ThreadPoolController mThreadPoolController;
    // 用来进行睡眠操作
    private boolean mIsInsert = false;
    private static final int MILLI_SECOND = 1000;
    
    private static final int SCANNED_SLEEP_TIME = 200;
    
    private String mPhysicId;
    private int mDeviceType;
    
    private LocalMediaInfo[] mFileArrays;
    private int mCurrFileIndex;
    
    private LocalMediaInfo[] mFolderArrays;
    private int mCurrFolderIndex;
    
    /**
     * 用来打印日志
     */
    FileScanThread(int threadNo, String path, 
        Context contextTemp, ThreadPoolController poolCtlTemp)
    {
    	mLog.d(TAG, "FileScanThread:" + Thread.currentThread().getId());
//        TAG += threadNo;
        //TAG = Thread.currentThread().getName();
        mLocQueue = LocFileScanQueue.getInstance();
        mRootPath = path;
        mContext = contextTemp;
        mThreadPoolController = poolCtlTemp;
        
        mFileCount = 0;
        mFolderCount = 0;
        mMsgCount = 0;
        
        initFileAndFolderArray();        
        
        mTableFileAndFolder = TableFileAndFolderManager.getInstance(mContext);
        // 初始化目录列表        
        mFolderList = new ArrayList<LocalMediaInfo>(DiskScanConst.INSERT_CNT);

        
        // 初始化文件列表        
        mFileList = new ArrayList<LocalMediaInfo>(DiskScanConst.INSERT_CNT);
        
        mPhysicId = DeviceDataUtils.getPhysicDevId(mRootPath);
        mDeviceType = DeviceDataUtils.getDeviceTypeByPath(mRootPath);

    }
    
    /**
     * 获取一个扫描路径
     * 
     * @return path 扫描路径
     * @see [类、类#方法、类#成员]
     */
    @Override
    public void run()
    {
        mLog.d(TAG, "scan thread start");        
        // 获取的目录
        String dir = null;
        File[] files = null;
        File tempFile = null;
        String dirTemp = null;
        
        while (true)
        {
            // 强制退出，如正在扫描的时候拔下外设则终止对应的扫描
            if (LocDevScanManager.ismStIsStop())
            {
                mLog.i(TAG, "force to stop scan ");
                
                // 丢弃已保存的数据
                clearScanVo();
                mLocQueue.removeAll();
                
                // 不能直接返回，导致线程退出数不够，不能退出主扫描线程
                //return;
                break;
            }
            
            // 进行文件及目录的入库操作
            saveFileAndDir(false);
            
            // 从队列获取扫描路径
            dir = mLocQueue.dequeue(!DiskScanner.ismStIsExit());
            
            // 如果目录为空，则直接退出该线程
            if (dir == null)
            {
                mLog.d(TAG, "scan thread dir == null");
                break;
            }
            
            // 创建目录对应的文件
            mFile = new File(dir);
            
            // 判断文件是否存在
            if (mFile == null || !mFile.exists())
            {
                mLog.e(TAG, "scan thread dir is wrong " + dir);
                continue;
            }
            
            // 保存目录信息			
            processDir(mFile, dir);	
            // 获取当前目录下的列表
            files = mFile.listFiles();
            
            if (files == null || files.length == 0)
            {
            	mLog.e(TAG, "no files in " + dir);
                continue;
            }
            
            // 遍历目录下的文件
            for (int i = 0; i < files.length; i++)
            {
                tempFile = files[i];                				
                if (tempFile == null || null == tempFile.getName() 
                        || !tempFile.exists() || "LOST.DIR".equals(tempFile.getName())
                        || tempFile.getName().startsWith("."))
                {
                    continue;
                }
                // 如果是目录文件，则入栈
                if (tempFile.isDirectory())
                {
                    dirTemp = tempFile.getAbsolutePath();
                    if (dirTemp != null && dirTemp.trim().length() != 0)
                    {
                    	//mLog.e(TAG, "scan temp dir:" + dir);
                        mLocQueue.enqueue(dirTemp);
                    }
                }
                // 过滤媒体文件
                else if (tempFile.isFile())
                {
                    processFile(tempFile, tempFile.getName(), dir);
                }
                
                // 进行文件及目录的入库操作  ###个别目录下的文件超多，导致一次入库的数据比较多
                saveFileAndDir(false);
            }
        }
        
        // 退出后把剩余的文件及目录入库
        saveFileAndDir(true);
        
        // 当前线程结束，通知主线程
        mThreadPoolController.notifyParent();
        
        mLog.d(TAG, "scan thread end");
    }
    
    /**
     * 处理目录，用来对进行过滤以及入库
     * 
     * @param fileName 文件名
     * @param dir 文件所在的目录
     * @see [类、类#方法、类#成员]
     */	 
    private void processDir(File tempFile, String dir)
    {
        int len = dir.lastIndexOf(DiskScanConst.SPLITER_STR);		
        mFolderInfo = getAvailableFolderInfo();
        if (null == mFolderInfo)
        {
            mFolderInfo = new LocalMediaInfo();  
            Log.d(TAG, "cc msg getAvailableFolder Info == null!");
        }
        // 目录名
        mFolderInfo.setmFileName(dir.substring(len + 1));
        // 目录的前半段
        mFolderInfo.setmParentPath(dir.substring(0, len));
        mFolderInfo.setmFolders(0);
        mFolderInfo.setmFiles(0);        
        mFolderInfo.setmModifyDate((int)(tempFile.lastModified() / MILLI_SECOND));
        mFolderInfo.setmPinyin(StringUtils.getFullPinYinLower(dir.substring(len + 1)));
        mFolderInfo.setmPhysicId(mPhysicId);
        mFolderInfo.setmDeviceType(mDeviceType);
        
        mFolderList.add(mFolderInfo);
        
        mFolderCount++;
        mMsgCount++;
    }	
    
    /**
     * 处理文件，用来对文件进行过滤以及入库
     * 
     * @param fileName 文件名
     * @param dir 文件所在的目录
     * @see [类、类#方法、类#成员]
     */
    private void processFile(File tempFile, String fileName, String dir)
    {
        // 判断文件名的合法性
        if (fileName == null || fileName.trim().length() == 0)
        {
            mLog.e(TAG, "file name is null");
            return;
        }
        
        // 获取文件类型，过滤掉非媒体文件
        int fileType = getFileType(fileName);
        if (fileType == Constant.MediaType.UNKNOWN_TYPE)
        {
            return;
        }
        
        // 处理文件并文件
		/* BEGIN: Modified by s00211113 for DTS2014021404690 2014/2/26 */
        mFileInfo = getAvailableFileInfo();
        if (null == mFileInfo)
        {            
            mFileInfo = new LocalMediaInfo();
            Log.d(TAG, "cc msg getAvailableFile Info == null!");
        }
        // 文件名
        mFileInfo.setmFileName(fileName);
        // 所在的文件夹
        mFileInfo.setmParentPath(dir);
        // 文件类型
        mFileInfo.setmFileType(fileType);
        // 文件大小
        mFileInfo.setmFileSize((int)tempFile.length());
        // 最后修改时间		
        mFileInfo.setmModifyDate((int)(tempFile.lastModified() / MILLI_SECOND));
        mFileInfo.setmPinyin(StringUtils.getFullPinYinLower(fileName));	
        mFileInfo.setmPhysicId(mPhysicId);
        mFileInfo.setmDeviceType(mDeviceType);
                
        // 保存文件信息
        mFileList.add(mFileInfo);
        
        mFileCount++;
        mMsgCount++;
    }
    
    /**
     * 利用文件类型哈希表，查找文件类型
     *
     * @param fileName 文件名带后缀
     * @return 文件类型：1：视频， 2：音频， 3：图片
     * @see [类、类#方法、类#成员]
     */
    @SuppressLint("DefaultLocale")
    private int getFileType(String fileName)
    {
        // 获取文件的后缀
        fileName = fileName.substring(fileName.lastIndexOf('.') + 1);
        // 把后缀统一用小写字母表示
        fileName = fileName.toLowerCase();
        if (DiskScanner.fileTypeMap != null && DiskScanner.fileTypeMap.containsKey(fileName))
        {
            // 文件类型：1：视频， 2：音频， 3：图片
            return DiskScanner.fileTypeMap.get(fileName);
        }

        return Constant.MediaType.UNKNOWN_TYPE;
    }
    
    /**
     * 进行文件及目录的入库操作
     * 
     * @see [类、类#方法、类#成员]
     */
    private void saveFileAndDir(boolean flag)
    {
        mIsInsert = false;
        
        // 如果文件处理已达到阈值，则入库
        if (flag || mFileCount >= DiskScanConst.INSERT_CNT)
        {
            mTableFileAndFolder.insertFile(mFileList, mRootPath);
            clearFileVo();
            if (mFolderCount != 0)
            {
                mTableFileAndFolder.insertDir(mFolderList, mRootPath);
                clearDirVo();
            }
            mIsInsert = true;
        }
        
        // 如果目录处理已达到阈值，则入库
        if (flag || mFolderCount >= DiskScanConst.INSERT_CNT)
        {
            mTableFileAndFolder.insertDir(mFolderList, mRootPath);
            clearDirVo();
            mIsInsert = true;
        }
        
        // 发送设备内容更新消息
        if (flag || mMsgCount >= DiskScanConst.BC_UPDATE_CNT)
        {
            DeviceDataUtils.sendBroadcastToMyMedia(DeviceDataConst.BC_TYPE_DEV_UPDATE, mRootPath, mContext);
            mMsgCount = 0;
        }
        
        // 如果进行了数据插入操作，该线程睡眠一会，降低CPU
        if (mIsInsert)
        {
            SystemClock.sleep(SCANNED_SLEEP_TIME);
        }
    }
    
    /**
     * 清空成员变量，以免影响下次扫描
     * 
     * @see [类、类#方法、类#成员]
     */
    private void clearScanVo()
    {
        mFile = null;
        clearFileVo();
        clearDirVo();
    }
    
    /**
     * 清空文件相关的数据
     *
     * @see [类、类#方法、类#成员]
     */
    private void clearFileVo()
    {
        mFileCount = 0;
        mCurrFileIndex = 0;        
        mFileList.clear();        
    }
    
    /**
     * 清空目录相关的数据
     *
     * @see [类、类#方法、类#成员]
     */
    private void clearDirVo()
    {
        mFolderCount = 0;
        mCurrFolderIndex = 0;
        mFolderList.clear();
    }
    
    private LocalMediaInfo getAvailableFileInfo()
    {                
        if (mCurrFileIndex  < DiskScanConst.INSERT_CNT && mCurrFileIndex >= 0)
        {
            return mFileArrays[mCurrFileIndex++];
        }
        return null;
    }
    
    private LocalMediaInfo getAvailableFolderInfo()
    {              
        if (mCurrFolderIndex  < DiskScanConst.INSERT_CNT && mCurrFolderIndex >= 0)
        {
            return mFolderArrays[mCurrFolderIndex++];
        }
        return null;
    }
    
    private void initFileAndFolderArray()
    {
        mCurrFileIndex = 0;
        mCurrFolderIndex = 0;        
        
        mFileArrays = new LocalMediaInfo[DiskScanConst.INSERT_CNT];
        mFolderArrays = new LocalMediaInfo[DiskScanConst.INSERT_CNT];
        
        for (int i = 0; i < DiskScanConst.INSERT_CNT; ++i)
        {
            mFileArrays[i] = new LocalMediaInfo();
        }
        
        for (int i = 0; i < DiskScanConst.INSERT_CNT; ++i)
        {
            mFolderArrays[i] = new LocalMediaInfo();
        }
    }
}
