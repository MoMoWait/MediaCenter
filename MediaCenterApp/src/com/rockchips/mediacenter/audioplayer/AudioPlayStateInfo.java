package com.rockchips.mediacenter.audioplayer;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.util.Log;
import com.rockchips.mediacenter.bean.FileInfo;
import com.rockchips.mediacenter.data.ConstData;

/**
 * 记录播放信息
 * @author GaoFei
 *
 */
public class AudioPlayStateInfo
{
    private static final String TAG = "AudioPlayStateInfo";

    private static AudioPlayStateInfo instance = null;

    private static Object instanceLock = new Object();

    private String deviceId = null;

    public void recycle()
    {
        Log.d(TAG, "recycle() IN...");
        synchronized (instanceLock)
        {
           release();
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
    private List<FileInfo> mMediaList = new ArrayList<FileInfo>();

    // 当前播放索引
    private int mCurrIndex = 0;

    private HashMap<String, PlayRecord> mPlayHistoryMap = new HashMap<String, PlayRecord>();

    // 播放列表
    private List<FileInfo> mMediaListCache = new ArrayList<>();

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
            if (ConstData.ClientTypeUniq.DMS_UNIQ.equalsIgnoreCase(mSenderClientUniq))
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

    private int playMode = ConstData.MediaPlayMode.MP_MODE_ALL_CYC;

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

    private void setCurrentMediaInfo(FileInfo hCurrentMediaInfo)
    {
        mCurrIndex = 0;
        if (hCurrentMediaInfo != null)
        {
            for (int i = 0; i < mMediaList.size(); i++)
            {
                FileInfo fileInfo = mMediaList.get(i);
                if (fileInfo.getPath().equals(hCurrentMediaInfo.getPath()))
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
    public  int getPlayMode()
    {
        return playMode;
    }
    
    
    public FileInfo getMediaInfo(int index)
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

    public FileInfo getCurrentMediaInfo(){
    	 synchronized (mMediaList)
         {
            return mMediaList.get(mCurrIndex);
         }
    }

    public void setMediaList(List<FileInfo> hBundleList, int currentIndex)
    {
        synchronized (mMediaListCache)
        {
            mPrepareIndex = currentIndex;
        }
        mMediaList = hBundleList;
    }
    

    /**
     * 
     * getMediaList 获取媒体数据
     * 
     * @return List<Bundle>
     * @exception
     */
    public final List<FileInfo> getMediaList()
    {
        synchronized (mMediaList)
        {
            return mMediaList;
        }
    }

    public int getIndex(FileInfo hMediaInfo)
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
        	FileInfo currentMediaInfo = getMediaInfo(mCurrIndex);
            // 恢复当前焦点
            setCurrentMediaInfo(currentMediaInfo);
         
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
        int currendIndex = getCurrentIndex();
        int totalCount = 0;
        int nextIndex = 0;
        synchronized (mMediaList)
        {
            int playmode = getPlayMode();
            if (getMediaList().size() == 0)
            {
                return -1;
            }

            switch (playmode)
            {
                case ConstData.MediaPlayMode.MP_MODE_ALL_CYC: // 所有循环
                    nextIndex = (currendIndex + 1) % getMediaList().size();
                    while(!isCanPlay(getMediaList().get(nextIndex)) && totalCount < getMediaList().size()){
                    	++totalCount;
                    	nextIndex = (currendIndex + 1) % getMediaList().size();
                    }
                    if(totalCount != getMediaList().size())
                    	index = nextIndex;
                    break;
                case ConstData.MediaPlayMode.MP_MODE_ALL: // 所有顺序播放
                    // 遍历到结尾
                	  nextIndex = currendIndex + 1;
                      while(nextIndex < getMediaList().size() && !isCanPlay(getMediaList().get(nextIndex))){
                    	  ++nextIndex;
                      }
                      if(nextIndex != getMediaList().size())
                    	  index = nextIndex;
                case ConstData.MediaPlayMode.MP_MODE_SINGLE_CYC: // 单曲循环
                	nextIndex = getCurrentIndex();
                	if(isCanPlay(getCurrentMediaInfo()))
                		index = nextIndex;
                    break;
                case ConstData.MediaPlayMode.MP_MODE_RONDOM: // 随机
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
    public boolean isMediaInfoExistedInMediaList(FileInfo info)
    {
        synchronized (mMediaList)
        {

            // 修正空指针异常，getData()有空能为空
            if (null == info || null == info.getPath())
            {
                return false;
            }

            Log.d(TAG, "info data is : " + info.getPath());

            if (mMediaList.size() <= 0)
            {
                Log.d(TAG, "the size of mMediaList is 0,will return!!!");
                return false;
            }

            FileInfo tempInfo = null;

            for (int i = 0; i < mMediaList.size(); i++)
            {
                tempInfo = mMediaList.get(i);

                // 修正空指针异常，getData()有空能为空
                if (null == tempInfo || null == tempInfo.getPath())
                {
                    continue;
                }

                Log.d(TAG, "tempInfo data is : " + tempInfo.getPath());

                // 如果相等则说明当前媒体存在于播放列表中

                if (info.getPath().equalsIgnoreCase(tempInfo.getPath()))
                {
                    Log.d(TAG, "current mediainfo existed in playlist!!!");
                    return true;
                }
            }
            Log.d(TAG, "current mediainfo not existed in playlist!!!");
            return false;
        }
    }

    public boolean isCanPlay(FileInfo hMediaInfo)
    {
        if (hMediaInfo != null && hMediaInfo.getPath() != null)
        {
            synchronized (mPlayHistoryMap)
            {
                PlayRecord historyRecord = mPlayHistoryMap.get(hMediaInfo.getPath());
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
        FileInfo mediaInfo = getMediaInfo(index);
        if (mediaInfo != null && mediaInfo.getPath() != null)
        {
            synchronized (mPlayHistoryMap)
            {
                PlayRecord historyRecord = mPlayHistoryMap.get(mediaInfo.getPath());
                if (historyRecord != null)
                {
                    historyRecord.setPosion(pos);
                }
                else
                {
                    historyRecord = new PlayRecord();
                    historyRecord.setPosion(pos);
                    mPlayHistoryMap.put(mediaInfo.getPath(), historyRecord);
                }
            }
        }
    }

    public void saveIsCanPlay(int index, boolean bCanPlay)
    {
        FileInfo mediaInfo = getMediaInfo(index);
        if (mediaInfo != null && mediaInfo.getPath() != null)
        {
            synchronized (mPlayHistoryMap)
            {
                PlayRecord historyRecord = mPlayHistoryMap.get(mediaInfo.getPath());
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
                        mPlayHistoryMap.put(mediaInfo.getPath(), historyRecord);
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
            if (mSenderClientUniq.equalsIgnoreCase(ConstData.ClientTypeUniq.PUSH_UNIQ))
            {
                return true;
            }

            if (mSenderClientUniq.equalsIgnoreCase(ConstData.ClientTypeUniq.SYN_UINQ))
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
            if (mSenderClientUniq.equalsIgnoreCase(ConstData.ClientTypeUniq.DMS_UNIQ))
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
