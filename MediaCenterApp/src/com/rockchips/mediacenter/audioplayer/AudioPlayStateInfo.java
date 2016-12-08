/**
 *
 * com.rockchips.iptv.stb.dlna.player
 * dd.java
 *
 * 2011-10-31-下午06:57:57
 * Copyright 2011 Huawei Technologies Co., Ltd
 *
 */
package com.rockchips.mediacenter.audioplayer;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.os.Bundle;
import android.util.Log;

import com.rockchips.mediacenter.basicutils.bean.LocalMediaInfo;
import com.rockchips.mediacenter.basicutils.constant.Constant;

/**
 * 
 * PlayStateInfo 1 2011-10-31 下午06:57:57
 * 
 * @version 1.0.0
 * 
 */

// 记录当前播状态的信息
public class AudioPlayStateInfo
{
    private static final String TAG = "MediaCenterApp";

    private static AudioPlayStateInfo instance = null;

    private static Object instanceLock = new Object();

    private String deviceId = null;

//    private boolean mFirstSync = true;

    /**
     * @return the mFirstSync
     */
//    public boolean isFirstSync()
//    {
//        return mFirstSync;
//    }

    /**
     * @param mFirstSync the mFirstSync to set
     */
//    public void setFirstSync(boolean firstSync)
//    {
//        Log.d(TAG, "setFirstSync() IN..., firstSync " + firstSync);
//        this.mFirstSync = firstSync;
//    }

//    public static synchronized AudioPlayStateInfo getInstance()
//    {
//        synchronized (instanceLock)
//        {
//            if (instance == null)
//            {
//                instance = new AudioPlayStateInfo();
//            }
//            return instance;
//        }
//    }

    public static void recycle()
    {
        Log.d(TAG, "recycle() IN...");
        synchronized (instanceLock)
        {
            if (instance != null)
            {
                instance.release();
                instance = null;
            }
        }
    }

    /**
     * @return the deviceId
     */
    public String getDeviceId()
    {
        Log.d(TAG, "current DeviceId is: " + deviceId);
        return deviceId;
    }

    /**
     * @param deviceId the deviceId to set
     */
    public void setDeviceId(String deviceId)
    {
        this.deviceId = deviceId;
    }

    // 播放列表
    private List<LocalMediaInfo> mMediaList = new ArrayList<LocalMediaInfo>();

    // 当前播放索引
    private int mCurrIndex = 0;

    private HashMap<String, PlayRecord> mPlayHistoryMap = new HashMap<String, PlayRecord>();

    // 播放列表
    private List<LocalMediaInfo> mMediaListCache = new ArrayList<LocalMediaInfo>();

    // 当前播放索引
    private int mPrepareIndex = -1;

    private boolean mNeedSync = false;

    // add by s00203507 2012年7月30日 begin
    // 是否需要显示菜单栏，当从音乐列表或者搜索出来的音乐点击进行播放时，需要显示菜单栏；
    // 当推甩屏或者从第三方管理软件点击进入播放时，不需要显示菜单栏。
    private boolean isNeedShowMenuBar = false;

    public boolean isNeedShowMenuBar()
    {
        if (null != mSenderClientUniq)
        {
            if (Constant.ClientTypeUniq.DMS_UNIQ.equalsIgnoreCase(mSenderClientUniq))
            {
                isNeedShowMenuBar = true;
            }
        }
        return isNeedShowMenuBar;
    }

    public void setNeedShowMenuBar(boolean isNeedShowMenuBar)
    {
        this.isNeedShowMenuBar = isNeedShowMenuBar;
    }

    // add by s00203507 2012年7月30日 end

    private static int playMode = Constant.MediaPlayMode.MP_MODE_ALL_CYC;

    private String mSenderClientUniq = null;

    private SecureRandom mSecureRandom = new SecureRandom();

    /**
     * 二分查找
     * 
     * 注意：二分查找只是针对有序排列的各种数组或集合
     * 
     * @param target
     * @param array
     * @return
     */

    static boolean binarySearch(int target, int[] array)
    {
        int front = 0;
        int tail = array.length - 1;
        // 判断子数组是否能再次二分
        while (front <= tail)
        {
            // 获取子数组的中间位置，并依据此中间位置进行二分
            int middle = (front + tail) / 2;
            if (array[middle] == target)
            {
                return true;
            }
            else if (array[middle] > target)
            {
                tail = middle - 1;
            }
            else
            {
                front = middle + 1;
            }
        }
        return false;
    }

    public interface OnPlayListChangedListener
    {
        public void OnPlayListChanged();
    }

    private OnPlayListSyncCompletedListener mOnPlayListSyncCompletedListener;

    public interface OnPlayListSyncCompletedListener
    {
        void onPlayListSyncCompleted(boolean isNeedSetAdapter);
    }

    public void registerOnPlayListSyncCompletedListener(OnPlayListSyncCompletedListener listener)
    {
        synchronized (mMediaListCache)
        {
            this.mOnPlayListSyncCompletedListener = listener;
        }
    }

    // 第三方监听列表变化回调
    private List<OnPlayListChangedListener> mOnPlayListChangedCallback = new ArrayList<OnPlayListChangedListener>();

    public void registerOnPlayListChangedListener(OnPlayListChangedListener l)
    {
        synchronized (mOnPlayListChangedCallback)
        {
            if (l != null)
            {
                mOnPlayListChangedCallback.add(l);
            }
        }
    }

    public void unregisterOnPlayListChangedListener(OnPlayListChangedListener l)
    {
        synchronized (mOnPlayListChangedCallback)
        {
            if (l != null)
            {
                mOnPlayListChangedCallback.remove(l);
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

    public AudioPlayStateInfo()
    {

    }

    private void release()
    {
        synchronized (mMediaListCache)
        {
            mMediaListCache.clear();
            mNeedSync = false;
        }
        synchronized (mMediaList)
        {
            mMediaList.clear();
            mCurrIndex = 0;
        }
        synchronized (mPlayHistoryMap)
        {
            mPlayHistoryMap.clear();
        }
    }

    private void setCurrentMediaInfo(LocalMediaInfo hCurrentMediaInfo)
    {
        mCurrIndex = 0;
        if (hCurrentMediaInfo != null)
        {
            for (int i = 0; i < mMediaList.size(); i++)
            {
                LocalMediaInfo mediaInfo = mMediaList.get(i);
                if (mediaInfo.getUrl().equals(hCurrentMediaInfo.getUrl()))
                {
                    mCurrIndex = i;
                    break;
                }
            }
        }
    }

    /**
     * 
     * setCurrentIndex 设置当前的index
     * 
     * @param i void
     * @exception
     */
    public void setCurrentIndex(int index)
    {
        synchronized (mMediaList)
        {
            mCurrIndex = index;
            if (mCurrIndex < 0 || mCurrIndex >= mMediaList.size())
            {
                mCurrIndex = 0;
            }

        }
    }

    public int getCurrentIndex()
    {
        return mCurrIndex;
    }

    /**
     * 
     * setPlayMode 设置播放模式
     * 
     * @param mode void
     * @exception
     */
    public static void setPlayMode(int mode)
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
    public static int getPlayMode()
    {
        return playMode;
    }

    public void addList(List<Bundle> hBundleList)
    {
        List<LocalMediaInfo> hMediaList = null;
        if (hBundleList != null && hBundleList.size() > 0)
        {
            hMediaList = parseBundleList(hBundleList);
        }

        synchronized (mMediaListCache)
        {

            if (hMediaList != null && hMediaList.size() > 0)
            {
                mMediaListCache.addAll(hMediaList);
            }
            if (!mNeedSync)
            {
                mNeedSync = true;
                synchronized (mOnPlayListChangedCallback)
                {
                    for (int i = 0; i < mOnPlayListChangedCallback.size(); i++)
                    {
                        mOnPlayListChangedCallback.get(i).OnPlayListChanged();
                    }
                }

            }
        }
    }

//    public void deleteList(String devID, String realDeviceId)
//    {
//        String currentDeviceId = "";
//        LocalMediaInfo tempInfo = null;
//        String tempUrl = "";
//
//        synchronized (mMediaList)
//        {
//            synchronized (mMediaListCache)
//            {
//
//                // TODO:根据devID删除
//                Log.d(TAG, "deleteList()");
//
//                if (StringUtils.isEmpty(devID))
//                {
//                    Log.d(TAG, "devID is null");
//                    return;
//                }
//
//                if (StringUtils.isEmpty(realDeviceId))
//                {
//                    Log.d(TAG, "realDeviceId is null");
//                    realDeviceId = devID;
//                }
//
//                if (mMediaList == null || mMediaList.size() == 0)
//                {
//                    Log.d(TAG, "mMediaList is null or mMediaList.size()==0 ");
//                    return;
//                }
//
//                // 删除被拔掉U盘在播放列表中的记录。
//                for (int i = mMediaList.size() - 1; i >= 0; i--)
//                {
//                    tempInfo = mMediaList.get(i);
//
//                    if (null == tempInfo)
//                    {
//                        Log.d(TAG, "null == tempInfo");
//                        continue;
//                    }
//
//                    tempUrl = tempInfo.getData();
//                    Log.d(TAG, "tempUrl is: " + tempUrl);
//
//                    currentDeviceId = tempInfo.getDeviceId();
//
//                    Log.d(TAG, "currentDeviceId is: " + currentDeviceId);
//
//                    if (!StringUtils.isEmpty(tempUrl))
//                    {
//                        if (devID.equalsIgnoreCase(currentDeviceId) || realDeviceId.equalsIgnoreCase(currentDeviceId) || tempUrl.startsWith(devID))
//                        {
//                            Log.d(TAG, "deleted song displayname is :" + tempInfo.getDisplayName());
//
//                            mMediaList.remove(i);
//                            Log.d(TAG, "mMediaList size is:" + mMediaList.size());
//                        }
//                    }
//
//                }
//
//                // 删除被拔掉U盘在缓存播放列表中的记录。
//
//                if (mMediaListCache == null || mMediaListCache.size() == 0)
//                {
//                    Log.d(TAG, "mMediaListCache is null or mMediaListCache.size()==0 ");
//                    return;
//                }
//                for (int i = mMediaListCache.size() - 1; i >= 0; i--)
//                {
//                    tempInfo = mMediaListCache.get(i);
//
//                    if (null == tempInfo)
//                    {
//                        continue;
//                    }
//
//                    tempUrl = tempInfo.getData();
//
//                    currentDeviceId = tempInfo.getDeviceId();
//
//                    if (!StringUtils.isEmpty(tempUrl))
//                    {
//                        if (devID.equalsIgnoreCase(currentDeviceId) || realDeviceId.equalsIgnoreCase(currentDeviceId) || tempUrl.startsWith(devID))
//                        {
//                            mMediaListCache.remove(i);
//                        }
//                    }
//
//                }
//
//                if (!mNeedSync)
//                {
//                    Log.d(TAG, "mNeedSync is false!!!");
//                    mNeedSync = true;
//                    Log.d(TAG, "mNeedSync is true!!!");
//
//                    Log.d(TAG, "before delete!!!");
//                    for (int i = 0; i < mOnPlayListChangedCallback.size(); i++)
//                    {
//                        mOnPlayListChangedCallback.get(i).OnPlayListChanged();
//                    }
//                    Log.d(TAG, "after delete!!!");
//                }
//            }
//        }
//    }

    /**
     * 
     * parseBundleList 转换数据到具体的类型并返回
     * 
     * @param list
     * @return List<LocalMediaInfo>
     * @exception
     */
    private List<LocalMediaInfo> parseBundleList(List<Bundle> hBundleList)
    {
        List<LocalMediaInfo> hMediaList = new ArrayList<LocalMediaInfo>();
        if (hBundleList != null)
        {
            for (int i = 0; i < hBundleList.size(); i++)
            {
                LocalMediaInfo hMediaInfo = new LocalMediaInfo();
                hMediaInfo.decompress(hBundleList.get(i));
                hMediaList.add(hMediaInfo);
            }
        }
        return hMediaList;
    }

    public LocalMediaInfo getMediaInfo(int index)
    {
        synchronized (mMediaList)
        {
            if (index >= 0 && index < mMediaList.size())
            {
                return mMediaList.get(index);
            }

            return null;
        }
    }

    public void setMediaList(List<Bundle> hBundleList)
    {
        Log.d(TAG, "setMediaList()");
        List<LocalMediaInfo> hMediaList = null;
        if (hBundleList != null && hBundleList.size() > 0)
        {
            hMediaList = parseBundleList(hBundleList);
        }
        else
        {
            return;
        }
        Log.d(TAG, "list size=" + hMediaList.size());
        synchronized (mMediaListCache)
        {
            Log.d(TAG, "set new media list....");
            LocalMediaInfo targetMediaInfo = null;
            if (mPrepareIndex != -1 && mPrepareIndex < mMediaListCache.size())
            {
                targetMediaInfo = mMediaListCache.get(mPrepareIndex);
            }
            mMediaListCache.clear();

            if (hMediaList != null && hMediaList.size() > 0)
            {
                mMediaListCache.addAll(hMediaList);

                if (targetMediaInfo != null)
                {
                    for (int i = 0; i < mMediaListCache.size(); i++)
                    {
                        LocalMediaInfo mediaInfo = mMediaListCache.get(i);
                        if (mediaInfo.getUrl().equals(targetMediaInfo.getUrl()))
                        {
                            mPrepareIndex = i;
                            break;
                        }
                    }
                }
            }
            Log.d(TAG, "set new media list ok....");

            if (!mNeedSync)
            {
                mNeedSync = true;
                synchronized (mOnPlayListChangedCallback)
                {
                    for (int i = 0; i < mOnPlayListChangedCallback.size(); i++)
                    {
                        Log.d(TAG, "OnPlayListChanged call back... ");
                        mOnPlayListChangedCallback.get(i).OnPlayListChanged();
                    }
                }
            }
            else
            {
                Log.d(TAG, "not repeat sync ...");
            }
        }

    }

    public void setMediaList(List<Bundle> hBundleList, int currentIndex)
    {
        synchronized (mMediaListCache)
        {
            mPrepareIndex = currentIndex;
        }
        setMediaList(hBundleList);
    }

    public void setMediaList2(List<LocalMediaInfo> list, int currentIndex)
    {
        mCurrIndex = currentIndex;
        mMediaList = list;
    }

    /**
     * 
     * getMediaList 获取媒体数据
     * 
     * @return List<Bundle>
     * @exception
     */
    public final List<LocalMediaInfo> getMediaList()
    {
        synchronized (mMediaList)
        {
            return mMediaList;
        }
    }

    public int getIndex(LocalMediaInfo hMediaInfo)
    {

        synchronized (mMediaList)
        {
            for (int i = 0; i < mMediaList.size(); i++)
            {
                if (mMediaList.get(i) == hMediaInfo)
                {
                    return i;
                }
            }
        }
        return -1;
    }

    public void syncList()
    {
        Log.d(TAG, "syncList()");
        synchronized (mMediaList)
        {

            synchronized (mMediaListCache)
            {
                if (mNeedSync)
                {
                    Log.d(TAG, "syncList run begin...");
                    // 备份当前焦点
                    LocalMediaInfo currentMediaInfo = getMediaInfo(mCurrIndex);

                    mMediaList.clear();

                    if (mMediaListCache.size() > 0)
                    {
                        mMediaList.addAll(mMediaListCache);
                    }
                    if (mPrepareIndex != -1)
                    {
                        setCurrentIndex(mPrepareIndex);
                        mPrepareIndex = -1;
                    }
                    else if (currentMediaInfo != null)
                    {
                        // 恢复当前焦点
                        setCurrentMediaInfo(currentMediaInfo);
                    }
                    else
                    {

                    }
                    mNeedSync = false;

                    if (mOnPlayListSyncCompletedListener != null)
                    {
//                        if (mFirstSync)
//                        {
                            mOnPlayListSyncCompletedListener.onPlayListSyncCompleted(true);
//                        }
//                        else
//                        {
//                            mOnPlayListSyncCompletedListener.onPlayListSyncCompleted(false);
//                        }
                    }

//                    mFirstSync = false;
                    Log.d(TAG, "syncList run end...");
                }
                else
                {
                    Log.d(TAG, "not need sync...");
                }

            }
        }
    }

    public boolean isPlayListEmpty()
    {
        synchronized (mMediaListCache)
        {
            if (0 != mMediaListCache.size())
            {
                return false;
            }
        }
        if (0 != getMediaList().size())
        {
            return false;
        }
        
        return true;
    }

    public boolean isNeedSysc()
    {
        synchronized (mMediaListCache)
        {
            return mNeedSync;
        }
    }

    public int getNextIndex()
    {
        int index = -1;

        if (isPushType())
        {
            return 0;
        }

        synchronized (mMediaList)
        {
            int playmode = getPlayMode();
            if (getMediaList().size() == 0)
            {
                return -1;
            }

            switch (playmode)
            {
                case Constant.MediaPlayMode.MP_MODE_ALL_CYC: // 所有循环
                    if (getMediaList().size() == 1)
                    {
                        // 当只有一首歌时，等同单曲循环
                        if (isCanPlay(getMediaInfo(getCurrentIndex())))
                        {
                            index = getCurrentIndex();
                        }
                    }
                    else
                    {
                        // 先遍历到结尾
                        for (int i = getCurrentIndex() + 1; i < getMediaList().size(); i++)
                        {
                            if (isCanPlay(getMediaInfo(i)))
                            {
                                index = i;
                                break;
                            }
                        }
                        // 从0遍历到当前聚焦的项
                        if (index == -1)
                        {
                            for (int i = 0; i < getCurrentIndex(); i++)
                            {
                                if (isCanPlay(getMediaInfo(i)))
                                {
                                    index = i;
                                    break;
                                }
                            }
                        }
                    }
                    break;
                case Constant.MediaPlayMode.MP_MODE_ALL: // 所有顺序播放
                    // 遍历到结尾
                    for (int i = getCurrentIndex() + 1; i < getMediaList().size(); i++)
                    {
                        if (isCanPlay(getMediaInfo(i)))
                        {
                            index = i;
                            break;
                        }
                    }
                    break;
                case Constant.MediaPlayMode.MP_MODE_SINGLE_CYC: // 单曲循环
                    if (isCanPlay(getMediaInfo(getCurrentIndex())))
                    {
                        index = getCurrentIndex();
                    }
                    break;
                case Constant.MediaPlayMode.MP_MODE_RONDOM: // 随机
                    List<Integer> indexList = new ArrayList<Integer>();
                    for (int i = 0; i < getMediaList().size(); i++)
                    {
                        if (isCanPlay(getMediaInfo(i)))
                        {
                            indexList.add(Integer.valueOf(i));
                        }
                    }

                    if (indexList.size() > 0)
                    {
                        if (indexList.size() == 1)
                        {
                            index = indexList.get(0);
                        }
                        else
                        {

                            index = indexList.get(mSecureRandom.nextInt(indexList.size())).intValue();
                            while (getCurrentIndex() == index)
                            {
                                index = indexList.get(mSecureRandom.nextInt(indexList.size())).intValue();
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        return index;
    }

    // 当设备下线时，判断当前的媒体信息是否存在于播放列表中。
    public boolean isMediaInfoExistedInMediaList(LocalMediaInfo info)
    {
        synchronized (mMediaList)
        {

            // 修正空指针异常，getData()有空能为空
            if (null == info || null == info.getUrl())
            {
                return false;
            }

            Log.d(TAG, "info data is : " + info.getUrl());

            if (mMediaList.size() <= 0)
            {
                Log.d(TAG, "the size of mMediaList is 0,will return!!!");
                return false;
            }

            LocalMediaInfo tempInfo = null;

            for (int i = 0; i < mMediaList.size(); i++)
            {
                tempInfo = mMediaList.get(i);

                // 修正空指针异常，getData()有空能为空
                if (null == tempInfo || null == tempInfo.getUrl())
                {
                    continue;
                }

                Log.d(TAG, "tempInfo data is : " + tempInfo.getUrl());

                // 如果相等则说明当前媒体存在于播放列表中

                if (info.getUrl().equalsIgnoreCase(tempInfo.getUrl()))
                {
                    Log.d(TAG, "current mediainfo existed in playlist!!!");
                    return true;
                }
            }
            Log.d(TAG, "current mediainfo not existed in playlist!!!");
            return false;
        }
    }

    public boolean isCanPlay(LocalMediaInfo hMediaInfo)
    {
        if (hMediaInfo != null && hMediaInfo.getUrl() != null)
        {
            synchronized (mPlayHistoryMap)
            {
                PlayRecord historyRecord = mPlayHistoryMap.get(hMediaInfo.getUrl());
                if (historyRecord != null)
                {
                    return historyRecord.isCanPlay();
                }
            }
        }
        return true;
    }

    public void savePlayPostion(int index, int pos)
    {
        LocalMediaInfo mediaInfo = getMediaInfo(index);
        if (mediaInfo != null && mediaInfo.getUrl() != null)
        {
            synchronized (mPlayHistoryMap)
            {
                PlayRecord historyRecord = mPlayHistoryMap.get(mediaInfo.getUrl());
                if (historyRecord != null)
                {
                    historyRecord.setPosion(pos);
                }
                else
                {
                    historyRecord = new PlayRecord();
                    historyRecord.setPosion(pos);
                    mPlayHistoryMap.put(mediaInfo.getUrl(), historyRecord);
                }
            }
        }
    }

    public void saveIsCanPlay(int index, boolean bCanPlay)
    {
        LocalMediaInfo mediaInfo = getMediaInfo(index);
        if (mediaInfo != null && mediaInfo.getUrl() != null)
        {
            synchronized (mPlayHistoryMap)
            {
                PlayRecord historyRecord = mPlayHistoryMap.get(mediaInfo.getUrl());
                if (historyRecord != null)
                {
                    historyRecord.setCanPlayFlag(bCanPlay);
                }
                else
                {
                    if (!bCanPlay)
                    {
                        historyRecord = new PlayRecord();
                        historyRecord.setCanPlayFlag(bCanPlay);
                        mPlayHistoryMap.put(mediaInfo.getUrl(), historyRecord);
                    }
                }
            }
        }
    }

    /**
     * 当前播放类型是否为推(包括了甩与DLNA推)过来的 isPushType
     * 
     * @return boolean
     * @exception
     */
    public boolean isPushType()
    {
        if (mSenderClientUniq != null)
        {
            if (mSenderClientUniq.equalsIgnoreCase(Constant.ClientTypeUniq.PUSH_UNIQ))
            {
                return true;
            }

            if (mSenderClientUniq.equalsIgnoreCase(Constant.ClientTypeUniq.SYN_UINQ))
            {
                return true;
            }

        }

        return false;
    }

    public boolean isDMSType()
    {
        if (mSenderClientUniq != null)
        {
            if (mSenderClientUniq.equalsIgnoreCase(Constant.ClientTypeUniq.DMS_UNIQ))
            {
                return true;
            }
        }

        return false;
    }

    class PlayRecord
    {
        private int mPostion = 0;

        private boolean mCanPlay = true;

        public PlayRecord()
        {

        }

        public void setPosion(int pos)
        {
            mPostion = pos;
        }

        public void setCanPlayFlag(boolean bCanPlay)
        {
            mCanPlay = bCanPlay;
        }

        public int getPostion()
        {
            return mPostion;
        }

        public boolean isCanPlay()
        {
            return mCanPlay;
        }

    }
}
