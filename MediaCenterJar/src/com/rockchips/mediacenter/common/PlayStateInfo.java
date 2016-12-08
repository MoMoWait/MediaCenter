/**
 * 
 * com.rockchips.iptv.stb.dlna.player
 * dd.java
 * 
 * 2011-10-31-下午06:57:57
 * Copyright 2011 Huawei Technologies Co., Ltd
 * 
 */
package com.rockchips.mediacenter.common;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.rockchips.mediacenter.basicutils.bean.LocalMediaInfo;
import com.rockchips.mediacenter.basicutils.constant.Constant;
import com.rockchips.mediacenter.basicutils.util.IICLOG;
import com.rockchips.mediacenter.basicutils.util.StringUtils;

/**
 * 
 * PlayStateInfo
 * 
 * 2011-10-31 下午06:57:57
 * 
 * @version 1.0.0
 * 
 */
// 记录当前播状态的信息
public final class PlayStateInfo
{
    private static final String TAG = "PlayStateInfo";
    
    private IICLOG mLog = IICLOG.getInstance();
    
    // 播放列表
    private List<LocalMediaInfo> mMediaList = null;
    
    //图片播放的时候背景音乐列表
    private static List<LocalMediaInfo> mBackGroupAudiolist = null;
    
    // zkf61715
    private static Set<Integer> mFavoriteSet = new HashSet<Integer>();
    
    //背景音乐所在的设备
    private static String mBgAudioDeviceId;
    
    // 当前播放索引
    private int mCurrIndex = 0;
    
    // 是否从图片播放界面进入音乐浏览
    private boolean bFromImage = false;
    
    private int playMode = Constant.MediaPlayMode.MP_MODE_ALL_CYC;
    
    private boolean bStop = false;
    
    private String mSenderClientUniq = null;
    
    // 需要一个线程来处理这些数据的 插入、删除
    private HandlerThread dataProcessThread = new HandlerThread("PlayStateInfo");
    
    private Handler dataProcHandler = null;
    
    // 数据操作消息
    public static final int MSG_DATA_PROC_DEL = 0;
    
    public static final int MSG_DATA_PROC_INSERT = 1;
    
    private static final int CACHE_NUM = 10;
    
    // 当前正在播放的曲目
    private LocalMediaInfo mCurrentMBI = null;
    
    private static String mCurrentDevId = null;
    
    private Map<String, String> deletingDevices = new HashMap<String, String>();
    
    private static PlayStateInfo instance = null;
    
    private static Object instanceLock = new Object();
    
//    public static synchronized PlayStateInfo getInstance()
//    {
//        synchronized (instanceLock)
//        {
//            if (instance == null)
//            {
//                instance = new PlayStateInfo();
//            }
//            return instance;
//        }
//    }
    
    public static String getCurrentDevId()
    {
        return mCurrentDevId;
    }
    
    public static void setCurrentDevId(String CurrentDevId)
    {
        mCurrentDevId = CurrentDevId;
    }
    public static void recycle()
    {
        synchronized (instanceLock)
        {
            if (instance != null)
            {
                instance.release();
                instance = null;
            }
        }
    }
    
    private void release()
    {
        synchronized (deletingDevices)
        {
            deletingDevices.clear();
        }
        if (mMediaList != null)
        {
            synchronized (mMediaList)
            {
                mMediaList.clear();
                mCurrIndex = 0;
            }
        }
        
    }
    
    public void setSenderClientUniq(String senderClientUniq)
    {
        mSenderClientUniq = senderClientUniq;
    }
    
    public String getSenderClientUniq()
    {
        return mSenderClientUniq;
    }
    
    public PlayStateInfo()
    {
        dataProcessThread.start();
        dataProcHandler = new DataProcessHandler(dataProcessThread.getLooper());
    }
    
    public boolean isMbiInDeletingDevices(LocalMediaInfo mbi)
    {
        mLog.d(TAG, "isMbiInDeletingDevices()");
        
        if (mbi == null)
        {
            mLog.d(TAG, "mbi == null");
            
            return false;
        }
        
        if (deletingDevices.size() == 0)
        {
            mLog.d(TAG, "deletingDevices.size() == 0");
            
            return false;
        }
        
        mLog.d(TAG, "deletingDevices:" + deletingDevices);
        
        String url = mbi.getmParentPath() + mbi.getmFileName();
        mLog.d(TAG, "url:" + url);
        
        // 判断mbi的Url
        Set<String> keySet = deletingDevices.keySet();
        for (String tempDevId : keySet)
        {
            mLog.d(TAG, "tempDevId:" + tempDevId);
            if (tempDevId != null)
            {
                if (url != null && url.startsWith(tempDevId))
                {
                    mLog.d(TAG, "This mbi in deletingDevices");
                    
                    return true;
                }
            }
        }
        
        return false;
    }
    
    // 插入、删除的 handler
    private class DataProcessHandler extends Handler
    {
        /**
         * constructor PlayStateInfo.DataProcessHandler.
         * 
         */
        public DataProcessHandler(Looper looper)
        {
            super(looper);
        }
        
        /*
         * (non-Javadoc)
         * 
         * @see android.os.Handler#handleMessage(android.os.Message)
         */
        @Override
        public void handleMessage(Message msg)
        {
            
            switch (msg.what)
            {
                case MSG_DATA_PROC_DEL:
                    
                    if (msg.obj == null)
                    {
                        mLog.e(TAG, "handleMessage MSG_DATA_PROC_DEL msg.obj is null");
                        return;
                    }
                    
                    String devid = (String)msg.obj;
                    
                    delList(devid);
                    
                    break;
                
                case MSG_DATA_PROC_INSERT:
                    
                    if (msg.obj == null)
                    {
                        mLog.e(TAG, "handleMessage MSG_DATA_PROC_DEL msg.obj is null");
                        return;
                    }
                    
                    @SuppressWarnings("unchecked")
                    List<Bundle> list = (List<Bundle>)msg.obj;
                    
                    addList(list);
                    
                    break;
                
                default:
                    break;
            }
            
            super.handleMessage(msg);
        }
        
    }
    
    /**
     * 
     * setCurrentIndex 设置当前的index
     * 
     * @param i
     *            void
     * @exception
     */
    public void setCurrentIndex(int i)
    {
        mLog.d(TAG, "Befor setCurrentIndex, mCurrIndex:" + mCurrIndex);
        mLog.d(TAG, "setCurrentIndex to:" + i);
        
        mCurrIndex = i;
        
        if (mMediaList != null)
        {
            int itemp = mMediaList.size();
            mCurrIndex = i >= itemp ? itemp - 1 : mCurrIndex;
        }
        
        mLog.d(TAG, "After setCurrentIndex, mCurrIndex:" + mCurrIndex);
    }
    
    public int getCurrentIndex()
    {
        return mCurrIndex;
    }
    
    /**
     * 
     * setPlayMode 设置播放模式
     * 
     * @param mode
     *            void
     * @exception
     */
    public void setPlayMode(int mode)
    {
        playMode = mode;
    }
    
    /**
     * 
     * getPlayMode 获取播放模式
     * 
     * @return int
     * @exception
     */
    public int getPlayMode()
    {
        return playMode;
    }
    
    public static String getBgAudioDeviceId()
    {
        return mBgAudioDeviceId;
    }

    public static void setBgAudioDeviceId(String bgAudioDeviceId)
    {
        mBgAudioDeviceId = bgAudioDeviceId;
    }

    public boolean isCycPlayMode()
    {
        /**
         * 循环播放包括，整体循环、单曲循环、随即播放
         */
        if (playMode == Constant.MediaPlayMode.MP_MODE_ALL_CYC)
        {
            return true;
        }
        
        if (playMode == Constant.MediaPlayMode.MP_MODE_RONDOM)
        {
            return true;
        }
        
        if (playMode == Constant.MediaPlayMode.MP_MODE_SINGLE_CYC)
        {
            return true;
        }
        
        return false;
    }
    
    // 判断如果是单曲模式【单曲循环】/【单曲】
    public boolean isSingleMode()
    {
        
        if (playMode == Constant.MediaPlayMode.MP_MODE_SINGLE_CYC)
        {
            return true;
        }
        
        if (playMode == Constant.MediaPlayMode.MP_MODE_SINGLE)
        {
            return true;
        }
        
        return false;
    }
    
    // 移动指针到U盘媒体文件列表头部
    // 如果找到了U盘媒体文件列表的头,返回true 并且修改currentIndex为当前位置
    public boolean seekToUMedia()
    {
        if (mMediaList == null)
        {
            return false;
        }
        
        synchronized (this)
        {
            
            for (int i = 0; i < mMediaList.size(); i++)
            {
                if (mMediaList.get(i).getmDeviceType() == Constant.DeviceType.DEVICE_TYPE_U)
                {
                    setCurrentIndex(i);
                    return true;
                }
                
            }
        }
        
        return false;
    }
    
    // 修还函数返回值，返回增加的媒体文件数量
    public int addList(List<Bundle> list)
    {
        mLog.d(TAG, "addList ---> In");
        
        int sumAdded = 0;
        
        // 同步，确保insert、delete、set只能排队进行
        if (list == null || list.size() == 0)
        {
            mLog.d(TAG, " list == null || list.size() == 0");
            return sumAdded;
        }
        
        LocalMediaInfo mbiTmp = getCurrentMediaInfo();
        
        if (mMediaList == null)
        {
            mMediaList = new ArrayList<LocalMediaInfo>();
        }
        
        synchronized (this)
        {
            for (int i = 0; i < list.size(); i++)
            {
                if (list.get(i) == null)
                {
                    mLog.d(TAG, " list in pos :" + i + " is null");
                    continue;
                }
                
                //B add by xkf76249
                Bundle bindle = list.get(i);
                
                LocalMediaInfo mbi = null;
                
//                if (bindle.getInt("DEVICE_TYPE") == Constant.DeviceType.DEVICE_TYPE_CLOUD)
//                {
//                    mbi = new CloudMediaInfo(null);
//                }
//                else
                {
                    mbi = new LocalMediaInfo();
                }
                
                mbi.decompress(list.get(i));
                
                if (mMediaList.contains(mbi))
                {
                    continue;
                }
                else
                {
                    mMediaList.add(mbi);
                    sumAdded += 1;
                }
            }
            
            if (mMediaList.size() > 1)
            {
                // Collections.sort(mMediaList, mediaCompType);
            }
            
            if (mbiTmp != null)
            {
                mLog.d(TAG, "mbiTmp != null");
                
                int index = mMediaList.indexOf(mbiTmp);
                
                mLog.d(TAG, "addList(list), modify current index to:" + index);
                
                // setCurrentIndex(index);
            }
            else
            {
                mLog.d(TAG, "mbiTmp == null");
                mLog.d(TAG, "addList(list), modify current index to: -1");
                
                setCurrentIndex(-1);
            }
            
            mLog.d(TAG, "addList sumAdded:" + sumAdded);
            mLog.d(TAG, "addList ---> out");
            
            return sumAdded;
        }
    }
    
    // 修改函数返回值，返回增加的媒体文件个数
    public int insertList(List<Bundle> list)
    {
        mLog.d(TAG, "insertList(list) --->IN");
        
        int sumInsert = 0;
        
        // 改成同步的方式
        sumInsert = addList(list);
        
        mLog.d(TAG, "sumInsert:" + sumInsert);
        mLog.d(TAG, "insertList(list) --->Out");
        
        return sumInsert;
    }
    
    /**
     * 
     * getMediaBaseList 转换数据到具体的类型并返回
     * 
     * @param list
     * @return List<LocalMediaInfo>
     * @exception
     */
    public List<LocalMediaInfo> getMediaBaseList(List<Bundle> list)
    {
        if (list == null)
        {
            mLog.d(TAG, "getMediaBaseList - input is null");
            return null;
        }
        
        List<LocalMediaInfo> mbiList = new ArrayList<LocalMediaInfo>();
        
        for (int i = 0; i < list.size(); i++)
        {
            if (list.get(i) == null)
            {
                mLog.d(TAG, " list in pos :" + i + " is null");
                continue;
            }
            
            LocalMediaInfo mbi = new LocalMediaInfo();
            
            mbi.decompress(list.get(i));
            mbiList.add(mbi);
        }
        
        if (mbiList.size() > 1)
        {
            // Collections.sort(mbiList, mediaCompType);
        }
        
        return mbiList;
    }
    
    public void delList(String devID, String realDeviceId)
    {
        mLog.d(TAG, "delList(devID, realDeviceId) --> In");
        mLog.d(TAG, "delList --> devID:" + devID);
        mLog.d(TAG, "delList --> realDeviceId:" + realDeviceId);
        if(mMediaList != null){
            mLog.d(TAG, "Before delList, mMediaList.size():" + mMediaList.size());
        }
        
        if (StringUtils.isEmpty(devID))
        {
            mLog.d(TAG, "devID is null");
            return;
        }
        
        if (StringUtils.isEmpty(realDeviceId))
        {
            mLog.d(TAG, "realDeviceId is null");
            return;
        }
        
        if (mMediaList == null)
        {
            mLog.d(TAG, "mMediaList is null");
            return;
        }
        
        // 将正在删除的设备ID保存起来
        mLog.d(TAG, "deletingDevices.put(" + devID + ", " + realDeviceId + ");");
        deletingDevices.put(devID, realDeviceId);
        
        // 如果当前播放的媒体文件在待删除的媒体列表中，则将当前播放的索引修正到上一个DMS的最后一个文件的索引位置
        
        // 判断当前播放的Url是否在删除的列表中
        boolean isCurrUrlInDeleList = false;
        
        // 获取当前播放的Url
        mLog.d(TAG, "mCurrIndex:" + mCurrIndex);
        LocalMediaInfo currentMbi = null;
        if (mCurrIndex >= 0)
        {
            mLog.d(TAG, "get current mediabase info");
            
            currentMbi = mMediaList.get(mCurrIndex);
        }
        
        // 当前播放的mbi不为空才进行判断
        if (currentMbi != null)
        {
            mLog.d(TAG, "currentMbi != null");
            String currentPlayUrl = currentMbi.getUrl();
            String currentPlayUrlDevId = currentMbi.getmPhysicId();
            mLog.d(TAG, "currentPlayUrl:" + currentPlayUrl);
            mLog.d(TAG, "currentPlayUrlDevId:" + currentPlayUrlDevId);
            
            // 当前播放的url不为空才进行判断
            if (currentPlayUrl != null)
            {
                mLog.d(TAG, "currentPlayUrl != null");
                
                if ((currentPlayUrlDevId != null && currentPlayUrlDevId.startsWith(devID))
                    || (currentPlayUrlDevId != null && currentPlayUrlDevId.equals(realDeviceId))
                    || currentPlayUrl.startsWith(devID))
                {
                    mLog.d(TAG, "Current Play Url In Delete List");
                    isCurrUrlInDeleList = true;
                }
            }
        }
        
        // 当前播放的Url在要删除的列表中，查找删除的列表中第一个文件的当前索引
        int modifyIndex = -1;
        if (isCurrUrlInDeleList)
        {
            mLog.d(TAG, "isCurrUrlInDeleList == true");
            
            for (int j = 0; j < mMediaList.size(); j++)
            {
                mLog.d(TAG, "The first loop, j:" + j);
                mLog.d(TAG, "The first loop, mMediaList.size():" + mMediaList.size());
                
                String tempDataUrl = mMediaList.get(j).getUrl();
                String tempDeviceId = mMediaList.get(j).getmPhysicId();
                mLog.d(TAG, "mMediaList.get(" + j + ").getData():" + tempDataUrl);
                mLog.d(TAG, "mMediaList.get(" + j + ").getDeviceId():" + tempDeviceId);
                
                if ((tempDeviceId != null && tempDeviceId.startsWith(devID))
                    || (tempDeviceId != null && tempDeviceId.equals(realDeviceId))
                    || (tempDataUrl != null && tempDataUrl.startsWith(devID)))
                {
                    mLog.d(TAG, "Find the index of the del list in mMediaList, index = " + j);
                    modifyIndex = j - 1;
                    mLog.d(TAG, "Find the index of the del list in mMediaList, modifyIndex = " + modifyIndex);
                    break;
                }
            }
        }
        
        // 执行删除操作
        for (int j = 0; j < mMediaList.size();)
        {
            mLog.d(TAG, "The second loop, j:" + j);
            mLog.d(TAG, "The second loop, mMediaList.size():" + mMediaList.size());
            
            // 每次循环都修改索引，避免循环播放时的下一首出现问题
            mLog.d(TAG, "Before Modify, the current index:" + mCurrIndex);
            if (isCurrUrlInDeleList)
            {
                mCurrIndex = modifyIndex;
            }
            else
            {
                mCurrIndex = mMediaList.indexOf(currentMbi);
            }
            mLog.d(TAG, "After Modify, the current index:" + mCurrIndex);
            
            String tempDeviceId = mMediaList.get(j).getmPhysicId();
            String tempDataUrl = mMediaList.get(j).getUrl();
            mLog.d(TAG, "mMediaList.get(" + j + ").getDeviceId():" + tempDeviceId);
            mLog.d(TAG, "mMediaList.get(" + j + ").getData():" + tempDataUrl);
            
            if ((tempDeviceId != null && tempDeviceId.startsWith(devID))
                || (tempDeviceId != null && tempDeviceId.equals(realDeviceId))
                || (tempDataUrl != null && tempDataUrl.startsWith(devID)))
            {
                mLog.d(TAG, "Delete tempDataUrl:" + tempDataUrl);
                mMediaList.remove(j);
                
                mLog.d(TAG, "mCurrIndex:" + mCurrIndex);
                mLog.d(TAG, "mMediaList.size():" + mMediaList.size());
                
                // 修正索引
                if (mCurrIndex >= mMediaList.size())
                {
                    mLog.d(TAG, "mCurrIndex >= mMediaList.size()");
                    
                    mCurrIndex = mMediaList.size() - 1;
                    
                    mLog.d(TAG, "mCurrIndex:" + mCurrIndex);
                }
            }
            else
            {
                mLog.d(TAG, "else in, j++");
                j++;
                mLog.d(TAG, "j:" + j);
            }
        }
        
        // 删除完成以后再次修改索引
        mLog.d(TAG, "Before Modify, the current index:" + mCurrIndex);
        if (isCurrUrlInDeleList)
        {
            mCurrIndex = modifyIndex;
        }
        else
        {
            mCurrIndex = mMediaList.indexOf(currentMbi);
        }
        mLog.d(TAG, "After Modify, the current index:" + mCurrIndex);
        
        // 将正在删除的设备ID从记录中删除
        mLog.d(TAG, "deletingDevices.remove(" + devID + ");");
        deletingDevices.remove(devID);
        
        mLog.d(TAG, "After delList, mMediaList.size():" + mMediaList.size());
    }
    
    public void delList(String devID)
    {
        mLog.d(TAG, "delList(devID) --> In");
        mLog.d(TAG, "delList --> devID:" + devID);
        if(mMediaList != null){
            mLog.d(TAG, "Before delList, mMediaList.size():" + mMediaList.size());
        }
        
        if (StringUtils.isEmpty(devID))
        {
            mLog.d(TAG, "devID is null");
            return;
        }
        
        if (mMediaList == null)
        {
            mLog.d(TAG, "mMediaList is null");
            return;
        }
        
        // 将正在删除的设备ID保存起来
        mLog.d(TAG, "deletingDevices.put(" + devID + ", null);");
        deletingDevices.put(devID, null);
        
        // 如果当前播放的媒体文件在待删除的媒体列表中，则将当前播放的索引修正到上一个DMS的最后一个文件的索引位置
        
        // 判断当前播放的Url是否在删除的列表中
        boolean isCurrUrlInDeleList = false;
        
        // 获取当前播放的Url
        mLog.d(TAG, "mCurrIndex:" + mCurrIndex);
        LocalMediaInfo currentMbi = null;
        if (mCurrIndex >= 0)
        {
            mLog.d(TAG, "get current mediabase info");
            
            currentMbi = mMediaList.get(mCurrIndex);
        }
        
        // 当前播放的mbi不为空才进行判断
        if (currentMbi != null)
        {
            mLog.d(TAG, "currentMbi != null");
            String currentPlayUrl = currentMbi.getUrl();
            String currentPlayUrlDevId = currentMbi.getmPhysicId();
            mLog.d(TAG, "currentPlayUrl:" + currentPlayUrl);
            mLog.d(TAG, "currentPlayUrlDevId:" + currentPlayUrlDevId);
            
            // 当前播放的url不为空才进行判断
            if (currentPlayUrl != null)
            {
                mLog.d(TAG, "currentPlayUrl != null");
                
                if ((currentPlayUrlDevId != null && currentPlayUrlDevId.startsWith(devID))
                    || currentPlayUrl.startsWith(devID))
                {
                    mLog.d(TAG, "Current Play Url In Delete List");
                    isCurrUrlInDeleList = true;
                }
            }
        }
        
        // 当前播放的Url在要删除的列表中，查找删除的列表中第一个文件的当前索引
        int modifyIndex = -1;
        if (isCurrUrlInDeleList)
        {
            mLog.d(TAG, "isCurrUrlInDeleList == true");
            
            for (int j = 0; j < mMediaList.size(); j++)
            {
                mLog.d(TAG, "The first loop, j:" + j);
                mLog.d(TAG, "The first loop, mMediaList.size():" + mMediaList.size());
                
                String tempDataUrl = mMediaList.get(j).getUrl();
                String tempDeviceId = mMediaList.get(j).getmPhysicId();
                mLog.d(TAG, "mMediaList.get(" + j + ").getData():" + tempDataUrl);
                mLog.d(TAG, "mMediaList.get(" + j + ").getDeviceId():" + tempDeviceId);
                
                if ((tempDeviceId != null && tempDeviceId.startsWith(devID))
                    || (tempDataUrl != null && tempDataUrl.startsWith(devID)))
                {
                    mLog.d(TAG, "Find the index of the del list in mMediaList, index = " + j);
                    modifyIndex = j - 1;
                    mLog.d(TAG, "Find the index of the del list in mMediaList, modifyIndex = " + modifyIndex);
                    break;
                }
            }
        }
        
        // 执行删除操作
        for (int j = 0; j < mMediaList.size();)
        {
            mLog.d(TAG, "The second loop, j:" + j);
            mLog.d(TAG, "The second loop, mMediaList.size():" + mMediaList.size());
            
            // 每次循环都修改索引，避免循环播放时的下一首出现问题
            mLog.d(TAG, "Before Modify, the current index:" + mCurrIndex);
            if (isCurrUrlInDeleList)
            {
                mCurrIndex = modifyIndex;
            }
            else
            {
                mCurrIndex = mMediaList.indexOf(currentMbi);
            }
            mLog.d(TAG, "After Modify, the current index:" + mCurrIndex);
            
            String tempDeviceId = mMediaList.get(j).getmPhysicId();
            String tempDataUrl = mMediaList.get(j).getUrl();
            mLog.d(TAG, "mMediaList.get(" + j + ").getDeviceId():" + tempDeviceId);
            mLog.d(TAG, "mMediaList.get(" + j + ").getData():" + tempDataUrl);
            
            if ((tempDeviceId != null && tempDeviceId.startsWith(devID))
                || (tempDataUrl != null && tempDataUrl.startsWith(devID)))
            {
                mLog.d(TAG, "Delete tempDataUrl:" + tempDataUrl);
                mMediaList.remove(j);
                
                mLog.d(TAG, "mCurrIndex:" + mCurrIndex);
                mLog.d(TAG, "mMediaList.size():" + mMediaList.size());
                
                // 修正索引
                if (mCurrIndex >= mMediaList.size())
                {
                    mLog.d(TAG, "mCurrIndex >= mMediaList.size()");
                    
                    mCurrIndex = mMediaList.size() - 1;
                    
                    mLog.d(TAG, "mCurrIndex:" + mCurrIndex);
                }
            }
            else
            {
                mLog.d(TAG, "else in, j++");
                j++;
                mLog.d(TAG, "j:" + j);
            }
        }
        
        // 删除完成以后再次修改索引
        mLog.d(TAG, "Before Modify, the current index:" + mCurrIndex);
        if (isCurrUrlInDeleList)
        {
            mCurrIndex = modifyIndex;
        }
        else
        {
            mCurrIndex = mMediaList.indexOf(currentMbi);
        }
        mLog.d(TAG, "After Modify, the current index:" + mCurrIndex);
        
        // 将正在删除的设备ID从记录中删除
        mLog.d(TAG, "deletingDevices.remove(" + devID + ");");
        deletingDevices.remove(devID);
        
        mLog.d(TAG, "After delList, mMediaList.size():" + mMediaList.size());
    }
    
    /**
     * delete all data in the list delAllData
     * 
     * void
     * 
     * @exception
     */
    public void delAllData()
    {
        mCurrIndex = 0;
        if (mMediaList != null)
        {
            mMediaList.clear();
        }
    }
    
    public void deleteList(String devid)
    {
        if (StringUtils.isEmpty(devid))
        {
            mLog.e(TAG, "no devID");
            return;
        }
        
        // 同步调用
        // modify by t00181037
        delList(devid);
        
        // 异步调用
        // Message msg = Message.obtain();
        // msg.what = MSG_DATA_PROC_DEL;
        // msg.obj = devid;
        //
        // if(dataProcHandler != null)
        // {
        // dataProcHandler.sendMessage(msg);
        // }
        
    }
    
    public LocalMediaInfo getCurrentMediaInfo()
    {
        mLog.d(TAG, "PlayStateInfo -- getCurrentMediaInfo()");
        
        // if (mCurrentMBI != null)
        // {
        // return mCurrentMBI;
        // }
        
        // vvvv
        
        mCurrentMBI = getCurrentPlayingMediaInfo();
        
        return mCurrentMBI;
    }
    
    /**
     * 获取当前在播放的界面的缓冲 getCurrentPlayingMediaInfo
     * 
     * @return LocalMediaInfo
     * @exception
     */
    public LocalMediaInfo getCurrentPlayingMediaInfo()
    {
        
        if (mMediaList == null)
        {
            mLog.d(TAG, "getCurrentMediaInfo mMediaList == null");
            
            return null;
        }
        
        if (mCurrIndex >= mMediaList.size())
        {
            mLog.d(TAG, "mCurrIndex >= mMediaList.size()");
            
            return null;
        }
        
        if (mCurrIndex < 0)
        {
            mLog.d(TAG, "mCurrIndex < 0, set it to 0");
            
            return null;
        }
        
        return mMediaList.get(mCurrIndex);
    }
    
    public LocalMediaInfo getNextMediaInfo()
    {
        mLog.d(TAG, "PlayStateInfo -- getNextMediaInfo()");
        
        LocalMediaInfo mbi = null;
        
        if (mMediaList == null)
        {
            return null;
        }
        
        synchronized (this)
        {
            while (mMediaList.size() > 0 && !isStop())
            {
                mCurrIndex += 1;
                mLog.d(TAG, "getNextMediaInfo index:" + mCurrIndex);
                
                mbi = getCurrentPlayingMediaInfo();
                
                if (null == mbi)
                {
                    if (isCycPlayMode())
                    {
                        // 回到第一个
                        mCurrIndex = -1;
                        continue;
                    }
                    else
                    {
                        setCurrentIndex(mMediaList.size() - 1);
                        
                        mCurrentMBI = null;
                        
                        return null;
                    }
                }
                
                if (!StringUtils.isEmpty(mbi.getUrl()))
                {
                    // //是否可以访问该文件
                    // if (mbi.getDeviceType() ==
                    // Constant.DeviceType.DEVICE_TYPE_U &&
                    // FileUtils.isExists(mbi.getData()))
                    // {
                    // break;
                    // }
                    // else if (mbi.getDeviceType() ==
                    // Constant.DeviceType.DEVICE_TYPE_DMS &&
                    // Network.existFile(mbi.getData()))
                    // {
                    // break;
                    // }
                    // else
                    // {
                    // mMediaList.remove(mCurrIndex);
                    // mCurrIndex -= 1;
                    // }
                    
                    break;
                }
            }
        }
        
        mCurrentMBI = mbi;
        
        return mbi;
    }
    
    public LocalMediaInfo getPreMediaInfo()
    {
        mLog.d(TAG, "PlayStateInfo -- getPreMediaInfo()");
        LocalMediaInfo mbi = null;
        
        if (mMediaList == null)
        {
            return null;
        }
        
        synchronized (this)
        {
            while (mMediaList.size() > 0)
            {
                mCurrIndex -= 1;
                mLog.d(TAG, "getPreMediaInfo index:" + mCurrIndex);
                
                mbi = getCurrentPlayingMediaInfo();
                if (null == mbi)
                {
                    if (isCycPlayMode())
                    {
                        // 回到最后一个
                        mCurrIndex = mMediaList.size();
                        continue;
                    }
                    else
                    {
                        setCurrentIndex(0);
                        
                        mCurrentMBI = null;
                        
                        return null;
                    }
                }
                
                if (!StringUtils.isEmpty(mbi.getUrl()))
                {
                    // 是否可以访问该文件
                    // if (mbi.getDeviceType() ==
                    // Constant.DeviceType.DEVICE_TYPE_U &&
                    // FileUtils.isExists(mbi.getData()))
                    // {
                    // break;
                    // }
                    //
                    // if (mbi.getDeviceType() ==
                    // Constant.DeviceType.DEVICE_TYPE_DMS &&
                    // Network.existFile(mbi.getData()))
                    // {
                    // break;
                    // }
                    // else
                    // {
                    // mMediaList.remove(mCurrIndex);
                    // mCurrIndex += 1;
                    // }
                    
                    break;
                }
            }
        }
        
        mCurrentMBI = mbi;
        
        return mbi;
    }
        
    /**
     * 获取需要缓存的list
     * 
     * @return
     */
    public synchronized List<LocalMediaInfo> getCacheList(int offsetOfcurrentIndex)
    {
        int index = getCurrentIndex() + offsetOfcurrentIndex;
        
        List<LocalMediaInfo> cacheList = new ArrayList<LocalMediaInfo>();
        
        int changefactor = 1;
        
        //modify by xkf76249  修改---有序的缓存前后5张
        // 缓存前后5张
        for (int i = 0; i < CACHE_NUM / 2; i++)
        {
            //modify by xkf76249
            int addIndex = index + i + 1;
            mLog.d(TAG, "- index -" + addIndex);
            if (addIndex < 0 || addIndex >= getMediaList().size())
            {
                continue;
            }
            LocalMediaInfo mi = getMediaList().get(addIndex);
            
            cacheList.add(mi);
            
            int decIndex = index - i - 1;
            
            mLog.d(TAG, "- index -" + decIndex);
            if (decIndex < 0 || decIndex >= getMediaList().size())
            {
                continue;
            }
            mi = getMediaList().get(decIndex);
            
            cacheList.add(mi);
            
        }
        //        //modify by xkf76249
        //        index = getCurrentIndex() + offsetOfcurrentIndex;
        //        mLog.d(TAG, "- getCurrentIndex -" + index);
        //        // 缓存前后5张
        //        for (int i = 0; i < CACHE_NUM / 2; i++)
        //        {
        //            //modify by xkf76249
        //            index--;
        //            
        //            mLog.d(TAG, "- index -" + index);
        //            if (index < 0 || index >= getMediaList().size())
        //            {
        //                continue;
        //            }
        //            MediaInfo mi = getMediaList().get(index);
        //            cacheList.add(mi);
        //            
        //        }
        
        return cacheList;
    }
    
    public void setMediaList(List<Bundle> mediaList)
    {
        if (mediaList == null)
        {
            mLog.e(TAG, "setMediaList is null");
            return;
        }
        
        if (mediaList.size() == 0)
        {
            mLog.e(TAG, "setMediaList size is zero");
            return;
        }
        
        synchronized (this)
        {
            
            List<LocalMediaInfo> mbiList = getMediaBaseList(mediaList);
            
            //
            // LocalMediaInfo mbi = getCurrentMediaInfo();
            //
            // if (mbi != null)
            // {
            // setCurrentIndex(mbiList.indexOf(mbi));
            // }
            //
            if (mbiList == null)
            {
                mLog.e(TAG, "mbiList is null after converting list");
                return;
            }
            
            if (mbiList.size() == 0)
            {
                mLog.e(TAG, "mbiList size is zero");
                return;
            }
            
            mMediaList = mbiList;
        }
        
    }
    
    /**
     * add for internal mediaplayer
     * @param mediaList
     */
    public void setMediaFileList(List<LocalMediaInfo> mediaList)
    {
        if (mediaList == null)
        {
            mLog.e(TAG, "setMediaList is null");
            return;
        }
        /* BEGIN: Added by s00211113 for DTS2014031904523 2014/3/19 */
//        if (mediaList.size() == 0)
//        {
//            mLog.e(TAG, "setMediaList size is zero");
//            return;
//        }
		/* END: Added by s00211113 for DTS2014031904523 2014/3/19 */
        
        synchronized (this)
        {
            mMediaList = mediaList;
        }
        
    }
    
    public static List<LocalMediaInfo> getmBackGroupAudiolist()
    {
        return mBackGroupAudiolist;
    }
    
    public static void setmBackGroupAudiolist(List<LocalMediaInfo> backGroupAudiolist)
    {
        mBackGroupAudiolist = backGroupAudiolist;
    }
    
    public static Set<Integer> getmFavoriteSet()
    {
        return mFavoriteSet;
    }

    public static void setmFavoriteList(Set<Integer> mFavoriteSet)
    {
        PlayStateInfo.mFavoriteSet = mFavoriteSet;
    }
    
    public void addFavorite(int position)
    {
        mFavoriteSet.add(position);
    }
    
    public void removeFavorite(int position)
    {
        mFavoriteSet.remove(position);
    }
    
    public boolean isbFromImage()
    {
        return bFromImage;
    }

    public void setbFromImage(boolean bFromImage)
    {
        this.bFromImage = bFromImage;
    }

    public boolean isFirstElement()
    {
        return mCurrIndex == 0;
    }
    
    public boolean isLastElement()
    {
        return mCurrIndex == mMediaList.size() - 1;
    }
    
    /**
     * 
     * getMediaList 获取媒体数据
     * 
     * @return List<Bundle>
     * @exception
     */
    public List<LocalMediaInfo> getMediaList()
    {
        return mMediaList;
    }
	/* BEGIN: Modified by s00211113 for DTS2014033000145    2014/3/31 */
	/* BEGIN: Modified by c00224451 for  DTS2014032605871   2014/3/27 */
    /* BEGIN: Added by s00211113 for DTS2014031904523 2014/3/19 */
    private static ArrayList<Integer> mSelectedImgIdxListForAudioPlayer = new ArrayList<Integer>();
    public static void setSelectedImgIdxListForAudioPlayer(ArrayList<Integer> selectedIdxList)
    {
        mSelectedImgIdxListForAudioPlayer.clear();
        if (null == selectedIdxList || 0 == selectedIdxList.size())
        {
            return;
        }        
        mSelectedImgIdxListForAudioPlayer.addAll(selectedIdxList);
    }
    
    public static ArrayList<Integer> getSelectedImgIdxListForAudioPlayer()
    {
        return mSelectedImgIdxListForAudioPlayer;
    }
    
    private static String mDevIdForSelectImg;
    public static void setDevIdForSelectImg(String devId)
    {
        mDevIdForSelectImg = devId;
    }
    
    public static String getDevIdForSelectImg()
    {
        return mDevIdForSelectImg;
    }
    /* END: Added by s00211113 for DTS2014031904523 2014/3/19 */    
	
    private static ArrayList<Integer> mSelectedAudioIdxListForImagePlayer = new ArrayList<Integer>();
    public static void setSelectedAudioIdxListForImagePlayer(ArrayList<Integer> selectedIdxList)
    {
        mSelectedAudioIdxListForImagePlayer.clear();
        if (null == selectedIdxList || 0 == selectedIdxList.size())
        {
            return;
        }        
        mSelectedAudioIdxListForImagePlayer.addAll(selectedIdxList);
    }
    
    public static ArrayList<Integer> getSelectedAudioIdxListForImagePlayer()
    {
        return mSelectedAudioIdxListForImagePlayer;
    }
    
    private static String mDevIdForSelectAud;
    public static void setDevIdForSelectAud(String devId)
    {
        mDevIdForSelectAud = devId;
    }
    
    public static String getDevIdForSelectAud()
    {
        return mDevIdForSelectAud;
    }
	/* END: Modified by s00211113 for DTS2014033000145    2014/3/31 */
	
    private static List<LocalMediaInfo> mBackgroundImages = new ArrayList<LocalMediaInfo>();
    public static void setBackgroundImages(List<LocalMediaInfo> mediaList)
    {
        mBackgroundImages = mediaList;
    }
    public static List<LocalMediaInfo> getBackgroundImages()
    {
        return mBackgroundImages;
    }
    
    /* END: Modified by c00224451 for  DTS2014032605871   2014/3/27 */
    /**
     * @param bStop
     *            the bStop to set
     */
    public void setStop(boolean bStop)
    {
        this.bStop = bStop;
        
        if (bStop)
        {
            if (dataProcHandler != null)
            {
                dataProcHandler.getLooper().quit();
            }
        }
        
    }
    
    /**
     * bStop
     * 
     * @return the bStop
     * @since 1.0.0
     */
    public boolean isStop()
    {
        return bStop;
    }
    
    // 判断是否有U盘数据
    public boolean isIncludedUTypeData()
    {
        List<LocalMediaInfo> mbiList = mMediaList;
        if (mbiList == null)
        {
            return false;
        }
        
        // 遍历看是否存在U盘的数据
        for (int i = 0; i < mbiList.size(); i++)
        {
            LocalMediaInfo mbi = mbiList.get(i);
            if (mbi == null)
            {
                continue;
            }
            
            if (mbi.getmDeviceType() == Constant.DeviceType.DEVICE_TYPE_U)
            {// 存在数据
                return true;
            }
        }
        
        mbiList = null;
        
        return false;
        
    }
    
    // TODO ..
    // 此函数需要提炼出来 在Activity2Data也有用到
    /**
     * 默认排序按照设备ID排序
     */
    protected Comparator<Object> mediaCompType = new Comparator<Object>()
    {
        public int compare(Object object1, Object object2)
        {
            if (object1 == null || object2 == null)
            {
                return 0;
            }
            
            LocalMediaInfo dbi1 = (LocalMediaInfo)(object1);
            LocalMediaInfo dbi2 = (LocalMediaInfo)(object2);
            
            Collator collator = Collator.getInstance();
            
            if (dbi1.getmDeviceType() == Constant.DeviceType.DEVICE_TYPE_U
                && dbi2.getmDeviceType() == Constant.DeviceType.DEVICE_TYPE_DMS)
            {
                return -1;
            }
            
            if (dbi1.getmDeviceType() == Constant.DeviceType.DEVICE_TYPE_DMS
                && dbi2.getmDeviceType() == Constant.DeviceType.DEVICE_TYPE_U)
            {
                return 1;
            }
            
            if (dbi1.getmPhysicId() == null || dbi2.getmPhysicId() == null)
            {
                return 0;
            }
            
            // int iret = collator.compare(dbi1.getDeviceId(),
            // dbi2.getDeviceId());
            
//            int iret = dbi1.getId() - dbi2.getId();
//            
//            if (iret == 0 && dbi1.getDeviceType() == Constant.DeviceType.DEVICE_TYPE_U)
//            {
//                iret = collator.compare(dbi1.getDeviceId(), dbi2.getDeviceId());
//            }
            
            int iret = collator.compare(dbi1.getUrl(), dbi2.getUrl());
            
            // mLog.d(TAG, "collator.compare(dbi1.getDeviceId(), dbi2.getDeviceId()) : "
            // + iret);
            return iret;
        }
    };
}
