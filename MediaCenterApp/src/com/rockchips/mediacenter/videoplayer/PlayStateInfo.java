/**
 * 
 * com.rockchips.iptv.stb.dlna.player
 * dd.java
 * 
 * 2011-10-31-下午06:57:57
 * Copyright 2011 Huawei Technologies Co., Ltd
 * 
 */
package com.rockchips.mediacenter.videoplayer;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.net.Uri;
import android.os.Bundle;
import android.os.HandlerThread;

import com.rockchips.mediacenter.basicutils.constant.Constant;
import com.rockchips.mediacenter.basicutils.util.IICLOG;
import com.rockchips.mediacenter.basicutils.util.StringUtils;
import com.rockchips.mediacenter.videoplayer.data.VideoInfo;

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
public class PlayStateInfo
{
    private static final String TAG = "MediaCenterApp";
    private IICLOG Log = IICLOG.getInstance();
    
    // 播放列表
    private List<VideoInfo> mMediaList = null;
    
    // 当前播放索引
    private int mCurrIndex = 0;
    
    // 当前播放的设备名
    private String mCurrDevName = "";
    
    private int playMode = Constant.MediaPlayMode.MP_MODE_SINGLE;
    
    private boolean bStop = false;
    
    private String mSenderClientUniq = null;
    
    //需要一个线程来处理这些数据的 插入、删除
//    private HandlerThread dataProcessThread = new HandlerThread("PlayStateInfo");
    
    /**Mender:l00174030; Reason: this handler doing nothing, kill it. **/
    //private Handler dataProcHandler = null;
    
    //数据操作消息
    public static final int MSG_DATA_PROC_DEL = 0;
    
    public static final int MSG_DATA_PROC_INSERT = 1;
    
    //当前正在播放的曲目
    private VideoInfo mCurrentMBI = null;
    
    private Map<String, String> deletingDevices = new HashMap<String, String>();
    
    private static PlayStateInfo playStateInfo = null;
    
    private static final String lock = "";
    
//    public static PlayStateInfo getPlayStateInfoInst()
//    {
//        synchronized (lock)
//        {
//            if (playStateInfo == null)
//            {
//                playStateInfo = new PlayStateInfo();
//            }
//        }		
//    	
//    	return playStateInfo;
//    }
    
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
//        dataProcessThread.start();        
    }
    
    public boolean isMbiInDeletingDevices(VideoInfo mbi)
    {
        Log.d(TAG, "isMbiInDeletingDevices()");
        
        if (mbi == null)
        {
            Log.d(TAG, "mbi == null");
            
            return false;
        }
        
        if (deletingDevices.size() == 0)
        {
            Log.d(TAG, "deletingDevices.size() == 0");
            
            return false;
        }
        
        Log.d(TAG, "deletingDevices:" + deletingDevices);
        
        String deviceId = mbi.getmPhysicId();
        String url = mbi.getUrl();
        Log.d(TAG, "deviceId:" + deviceId);
        Log.d(TAG, "url:" + url);
        
        // 判断mbi的deviceID
        if ((deviceId != null && deletingDevices.containsKey(deviceId))
            || (deviceId != null && deletingDevices.containsValue(deviceId)))
        {
            Log.d(TAG, "This mbi in deletingDevices");
            
            return true;
        }
        
        // 判断mbi的Url
        Set<String> keySet = deletingDevices.keySet();
        for (String tempDevId : keySet)
        {
            Log.d(TAG, "tempDevId:" + tempDevId);
            if (tempDevId != null)
            {
                if (url != null && url.startsWith(tempDevId))
                {
                    Log.d(TAG, "This mbi in deletingDevices");
                    
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * 
     * setCurrentIndex
     *设置当前的index 
     * @param i 
     *void
     * @exception
     */
    public void setCurrentIndex(int i)
    {
        Log.d(TAG, "Befor setCurrentIndex, mCurrIndex:" + mCurrIndex);
        Log.d(TAG, "setCurrentIndex to:" + i);
        
        mCurrIndex = i;
        
        if (mMediaList != null)
        {
            int itemp = mMediaList.size();
            mCurrIndex = i >= itemp ? itemp - 1 : mCurrIndex;
        }
        
        Log.d(TAG, "After setCurrentIndex, mCurrIndex:" + mCurrIndex);
    }
    
    public int getCurrentIndex()
    {
        return mCurrIndex;
    }
    
    /**
     * 
     * setPlayMode
     * 设置播放模式
     * @param mode 
     *void
     * @exception
     */
    public void setPlayMode(int mode)
    {
        playMode = mode;
    }
    
    /**
     * 
     * getPlayMode
     * 获取播放模式
     * @return 
     *int
     * @exception
     */
    public int getPlayMode()
    {
        return playMode;
    }
    
    public boolean isCycPlayMode()
    {
        /**
         * 循环播放包括，整体循环、单曲循环、随即播放
         */
        if (playMode == Constant.MediaPlayMode.MP_MODE_ALL_CYC)
        {
            return false;
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
    
    //判断如果是单曲模式【单曲循环】/【单曲】
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
    
    //移动指针到U盘媒体文件列表头部
    //如果找到了U盘媒体文件列表的头,返回true 并且修改currentIndex为当前位置
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
    
    public void addList(List<Bundle> list)
    {
        Log.d(TAG, "----- addList In -----");
        
        //同步，确保insert、delete、set只能排队进行
        if (list == null || list.size() == 0)
        {
            Log.d(TAG, " list == null || list.size() == 0");
            return;
        }
        
        VideoInfo mbiTmp = getCurrentMediaInfo();
        
        if (mMediaList == null)
        {
            mMediaList = new ArrayList<VideoInfo>();
        }
        
        synchronized (this)
        {
            VideoInfo mbi = null;
            for (int i = 0; i < list.size(); i++)
            {
                if (list.get(i) == null)
                {
                    Log.d(TAG, " list in pos :" + i + " is null");
                    continue;
                }
                
                mbi = new VideoInfo();
                mbi.decompress(list.get(i));
                
                if (mMediaList.contains(mbi))
                {
                    continue;
                }
                else
                {
                    mMediaList.add(mbi);
                }
            }
            
            if (mMediaList.size() > 1)
            {               
            }
            
            if (mbiTmp != null)
            {
                Log.d(TAG, "mbiTmp != null");
                
                int index = mMediaList.indexOf(mbiTmp);
                
                Log.d(TAG, "addList(list), modify current index to:" + index);
                
                setCurrentIndex(index);
            }
            else
            {
                Log.d(TAG, "mbiTmp == null");
                Log.d(TAG, "addList(list), modify current index to: -1");
                
                setCurrentIndex(-1);
            }
        }
    }
    
    public void insertList(List<Bundle> list)
    {
        Log.d(TAG, "insertList(list) --->IN");
        
        //改成同步的方式
        addList(list);
        
        Log.d(TAG, "insertList(list) --->Out");
    }
    
    /**
     * 
     * getMediaBaseList
     * 转换数据到具体的类型并返回
     * @param list
     * @return 
     *List<MediaBaseInfo>
     * @exception
     */
    public List<VideoInfo> getMediaBaseList(List<Bundle> list)
    {
        if (list == null)
        {
            Log.d(TAG, "getMediaBaseList - input is null");
            return null;
        }
        
        List<VideoInfo> mbiList = new ArrayList<VideoInfo>();
        
        VideoInfo mbi = null;
        for (int i = 0; i < list.size(); i++)
        {
            if (list.get(i) == null)
            {
                Log.d(TAG, " list in pos :" + i + " is null");
                continue;
            }
            
            mbi = new VideoInfo();
            
            mbi.decompress(list.get(i));
            
            mbiList.add(mbi);
            
        }
        
        if (mbiList.size() > 1)
        {            
        }
        
        return mbiList;
        
    }
    
    public void delList(String devID, String realDeviceId)
    {
        Log.d(TAG, "delList(devID, realDeviceId) --> In");
        Log.d(TAG, "delList --> devID:" + devID);
        Log.d(TAG, "delList --> realDeviceId:" + realDeviceId);
        if(mMediaList != null){
            Log.d(TAG, "Before delList, mMediaList.size():" + mMediaList.size());
        }
        
        if (StringUtils.isEmpty(devID))
        {
            Log.d(TAG, "devID is null");
            return;
        }
        
        if (StringUtils.isEmpty(realDeviceId))
        {
            Log.d(TAG, "realDeviceId is null");
            return;
        }
        
        if (mMediaList == null)
        {
            Log.d(TAG, "mMediaList is null");
            return;
        }
        
        // 将正在删除的设备ID保存起来
        Log.d(TAG, "deletingDevices.put(" + devID + ", " + realDeviceId + ");");
        deletingDevices.put(devID, realDeviceId);
        
        // 如果当前播放的媒体文件在待删除的媒体列表中，则将当前播放的索引修正到上一个DMS的最后一个文件的索引位置
        
        // 判断当前播放的Url是否在删除的列表中
        boolean isCurrUrlInDeleList = false;
        
        // 获取当前播放的Url
        Log.d(TAG, "mCurrIndex:" + mCurrIndex);
        VideoInfo currentMbi = null;
        if (mCurrIndex >= 0)
        {
            Log.d(TAG, "get current mediabase info");
            
            currentMbi = mMediaList.get(mCurrIndex);
        }
        
        // 当前播放的mbi不为空才进行判断
        if (currentMbi != null)
        {
            Log.d(TAG, "currentMbi != null");
            String currentPlayUrl = currentMbi.getUrl();
            String currentPlayUrlDevId = currentMbi.getmPhysicId();
            Log.d(TAG, "currentPlayUrl:" + currentPlayUrl);
            Log.d(TAG, "currentPlayUrlDevId:" + currentPlayUrlDevId);
            
            // 当前播放的url不为空才进行判断
            if (currentPlayUrl != null)
            {
                Log.d(TAG, "currentPlayUrl != null");
                
                if ((currentPlayUrlDevId != null && currentPlayUrlDevId.startsWith(devID))
                    || (currentPlayUrlDevId != null && currentPlayUrlDevId.equals(realDeviceId))
                    || currentPlayUrl.startsWith(devID))
                {
                    Log.d(TAG, "Current Play Url In Delete List");
                    isCurrUrlInDeleList = true;
                }
            }
        }
        
        // 当前播放的Url在要删除的列表中，查找删除的列表中第一个文件的当前索引
        int modifyIndex = -1;
        if (isCurrUrlInDeleList)
        {
            Log.d(TAG, "isCurrUrlInDeleList == true");
            
            String tempDataUrl = null;
            String tempDeviceId = null;
            for (int j = 0; j < mMediaList.size(); j++)
            {
                Log.d(TAG, "The first loop, j:" + j);
                Log.d(TAG, "The first loop, mMediaList.size():" + mMediaList.size());
                
                tempDataUrl = mMediaList.get(j).getUrl();
                tempDeviceId = mMediaList.get(j).getmPhysicId();
                Log.d(TAG, "mMediaList.get(" + j + ").getData():" + tempDataUrl);
                Log.d(TAG, "mMediaList.get(" + j + ").getDeviceId():" + tempDeviceId);
                
                if ((tempDeviceId != null && tempDeviceId.startsWith(devID))
                    || (tempDeviceId != null && tempDeviceId.equals(realDeviceId))
                    || (tempDataUrl != null && tempDataUrl.startsWith(devID)))
                {
                    Log.d(TAG, "Find the index of the del list in mMediaList, index = " + j);
                    modifyIndex = j - 1;
                    Log.d(TAG, "Find the index of the del list in mMediaList, modifyIndex = " + modifyIndex);
                    break;
                }
            }
        }
        
        String tempDeviceId = null;
        String tempDataUrl = null;
        // 执行删除操作
        for (int j = 0; j < mMediaList.size();)
        {
            Log.d(TAG, "The second loop, j:" + j);
            Log.d(TAG, "The second loop, mMediaList.size():" + mMediaList.size());
            
            //每次循环都修改索引，避免循环播放时的下一首出现问题
            Log.d(TAG, "Before Modify, the current index:" + mCurrIndex);
            if (isCurrUrlInDeleList)
            {
                mCurrIndex = modifyIndex;
            }
            else
            {
                mCurrIndex = mMediaList.indexOf(currentMbi);
            }
            Log.d(TAG, "After Modify, the current index:" + mCurrIndex);
            
            tempDeviceId = mMediaList.get(j).getmPhysicId();
            tempDataUrl = mMediaList.get(j).getUrl();
            Log.d(TAG, "mMediaList.get(" + j + ").getDeviceId():" + tempDeviceId);
            Log.d(TAG, "mMediaList.get(" + j + ").getData():" + tempDataUrl);
            
            if ((tempDeviceId != null && tempDeviceId.startsWith(devID))
                || (tempDeviceId != null && tempDeviceId.equals(realDeviceId))
                || (tempDataUrl != null && tempDataUrl.startsWith(devID)))
            {
                Log.d(TAG, "Delete tempDataUrl:" + tempDataUrl);
                mMediaList.remove(j);
                
                Log.d(TAG, "mCurrIndex:" + mCurrIndex);
                Log.d(TAG, "mMediaList.size():" + mMediaList.size());
                
                //修正索引
                if (mCurrIndex >= mMediaList.size())
                {
                    Log.d(TAG, "mCurrIndex >= mMediaList.size()");
                    
                    mCurrIndex = mMediaList.size() - 1;
                    
                    Log.d(TAG, "mCurrIndex:" + mCurrIndex);
                }
            }
            else
            {
                Log.d(TAG, "else in, j++");
                j++;
                Log.d(TAG, "j:" + j);
            }
        }
        
        // 删除完成以后再次修改索引
        Log.d(TAG, "Before Modify, the current index:" + mCurrIndex);
        if (isCurrUrlInDeleList)
        {
            mCurrIndex = modifyIndex;
        }
        else
        {
            mCurrIndex = mMediaList.indexOf(currentMbi);
        }
        Log.d(TAG, "After Modify, the current index:" + mCurrIndex);
        
        // 将正在删除的设备ID从记录中删除
        Log.d(TAG, "deletingDevices.remove(" + devID + ");");
        deletingDevices.remove(devID);
        
        Log.d(TAG, "After delList, mMediaList.size():" + mMediaList.size());
    }
    
    public void delList(String devID)
    {
        Log.d(TAG, "delList(devID) --> In");
        Log.d(TAG, "delList --> devID:" + devID);
        if(mMediaList != null){
           Log.d(TAG, "Before delList, mMediaList.size():" + mMediaList.size());
        }
        
        if (StringUtils.isEmpty(devID))
        {
            Log.d(TAG, "devID is null");
            return;
        }
        
        if (mMediaList == null)
        {
            Log.d(TAG, "mMediaList is null");
            return;
        }
        
        // 将正在删除的设备ID保存起来
        Log.d(TAG, "deletingDevices.put(" + devID + ", null);");
        deletingDevices.put(devID, null);
        
        // 如果当前播放的媒体文件在待删除的媒体列表中，则将当前播放的索引修正到上一个DMS的最后一个文件的索引位置
        
        // 判断当前播放的Url是否在删除的列表中
        boolean isCurrUrlInDeleList = false;
        
        // 获取当前播放的Url
        Log.d(TAG, "mCurrIndex:" + mCurrIndex);
        VideoInfo currentMbi = null;
        if (mCurrIndex >= 0)
        {
            Log.d(TAG, "get current mediabase info");
            
            currentMbi = mMediaList.get(mCurrIndex);
        }
        
        // 当前播放的mbi不为空才进行判断
        if (currentMbi != null)
        {
            Log.d(TAG, "currentMbi != null");
            String currentPlayUrl = currentMbi.getUrl();
            String currentPlayUrlDevId = currentMbi.getmPhysicId();
            Log.d(TAG, "currentPlayUrl:" + currentPlayUrl);
            Log.d(TAG, "currentPlayUrlDevId:" + currentPlayUrlDevId);
            
            // 当前播放的url不为空才进行判断
            if (currentPlayUrl != null)
            {
                Log.d(TAG, "currentPlayUrl != null");
                
                if ((currentPlayUrlDevId != null && currentPlayUrlDevId.startsWith(devID))
                    || currentPlayUrl.startsWith(devID))
                {
                    Log.d(TAG, "Current Play Url In Delete List");
                    isCurrUrlInDeleList = true;
                }
            }
        }
        
        // 当前播放的Url在要删除的列表中，查找删除的列表中第一个文件的当前索引
        int modifyIndex = -1;
        if (isCurrUrlInDeleList)
        {
            Log.d(TAG, "isCurrUrlInDeleList == true");
            
            String tempDataUrl = null;
            String tempDeviceId = null;
            for (int j = 0; j < mMediaList.size(); j++)
            {
                Log.d(TAG, "The first loop, j:" + j);
                Log.d(TAG, "The first loop, mMediaList.size():" + mMediaList.size());
                
                tempDataUrl = mMediaList.get(j).getUrl();
                tempDeviceId = mMediaList.get(j).getmPhysicId();
                Log.d(TAG, "mMediaList.get(" + j + ").getData():" + tempDataUrl);
                Log.d(TAG, "mMediaList.get(" + j + ").getDeviceId():" + tempDeviceId);
                
                if ((tempDeviceId != null && tempDeviceId.startsWith(devID))
                    || (tempDataUrl != null && tempDataUrl.startsWith(devID)))
                {
                    Log.d(TAG, "Find the index of the del list in mMediaList, index = " + j);
                    modifyIndex = j - 1;
                    Log.d(TAG, "Find the index of the del list in mMediaList, modifyIndex = " + modifyIndex);
                    break;
                }
            }
        }
        
        String tempDeviceId = null;
        String tempDataUrl = null;
        // 执行删除操作
        for (int j = 0; j < mMediaList.size();)
        {
            Log.d(TAG, "The second loop, j:" + j);
            Log.d(TAG, "The second loop, mMediaList.size():" + mMediaList.size());
            
            // 每次循环都修改索引，避免循环播放时的下一首出现问题
            Log.d(TAG, "Before Modify, the current index:" + mCurrIndex);
            if (isCurrUrlInDeleList)
            {
                mCurrIndex = modifyIndex;
            }
            else
            {
                mCurrIndex = mMediaList.indexOf(currentMbi);
            }
            Log.d(TAG, "After Modify, the current index:" + mCurrIndex);
            
            tempDeviceId = mMediaList.get(j).getmPhysicId();
            tempDataUrl = mMediaList.get(j).getUrl();
            Log.d(TAG, "mMediaList.get(" + j + ").getDeviceId():" + tempDeviceId);
            Log.d(TAG, "mMediaList.get(" + j + ").getData():" + tempDataUrl);
            
            if ((tempDeviceId != null && tempDeviceId.startsWith(devID))
                || (tempDataUrl != null && tempDataUrl.startsWith(devID)))
            {
                Log.d(TAG, "Delete tempDataUrl:" + tempDataUrl);
                mMediaList.remove(j);
                
                Log.d(TAG, "mCurrIndex:" + mCurrIndex);
                Log.d(TAG, "mMediaList.size():" + mMediaList.size());
                
                //修正索引
                if (mCurrIndex >= mMediaList.size())
                {
                    Log.d(TAG, "mCurrIndex >= mMediaList.size()");
                    
                    mCurrIndex = mMediaList.size() - 1;
                    
                    Log.d(TAG, "mCurrIndex:" + mCurrIndex);
                }
            }
            else
            {
                Log.d(TAG, "else in, j++");
                j++;
                Log.d(TAG, "j:" + j);
            }
        }
        
        // 删除完成以后再次修改索引
        Log.d(TAG, "Before Modify, the current index:" + mCurrIndex);
        if (isCurrUrlInDeleList)
        {
            mCurrIndex = modifyIndex;
        }
        else
        {
            mCurrIndex = mMediaList.indexOf(currentMbi);
        }
        Log.d(TAG, "After Modify, the current index:" + mCurrIndex);
        
        // 将正在删除的设备ID从记录中删除
        Log.d(TAG, "deletingDevices.remove(" + devID + ");");
        deletingDevices.remove(devID);
        
        Log.d(TAG, "After delList, mMediaList.size():" + mMediaList.size());
    }
    
    /**
     * delete all data in the list
     * delAllData
     *  
     *void
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
            Log.e(TAG, "no devID");
            return;
        }
    }
    
    public VideoInfo getCurrentMediaInfo()
    {
        Log.d(TAG, "PlayStateInfo -- getCurrentMediaInfo()");
        mCurrentMBI = getCurrentPlayingMediaInfo();
        
        return mCurrentMBI;
    }
    
    /**
     * 获取当前在播放的界面的缓冲
     * getCurrentPlayingMediaInfo
     * 
     * @return 
     *MediaBaseInfo
     * @exception
     */
    public VideoInfo getCurrentPlayingMediaInfo()
    {
        
        if (mMediaList == null)
        {
            Log.d(TAG, "getCurrentMediaInfo mMediaList == null");
            
            return null;
        }
        
        if (mCurrIndex >= mMediaList.size())
        {
            Log.d(TAG, "mCurrIndex >= mMediaList.size()");
            
            return null;
        }
        
        if (mCurrIndex < 0)
        {
            Log.d(TAG, "mCurrIndex < 0, set it to 0");
            
            return null;
        }
        VideoInfo item = mMediaList.get(mCurrIndex);
        String mimeType = item.getmMimeType();
        String uriStr = item.getUrl();
        if (hasReUpdated(item.getmResUri()))
        {
            Log.d(TAG, "Has been updated");
        }
        else if (isNetMimeType(mimeType))
        {
            uriStr += (uriStr.indexOf("?") > 0) ? "&" : "?";
            uriStr += "dlna=mov";
            item.setmResUri(uriStr);
        }
        Log.d(TAG, "3---uriStr = " + uriStr);
        return item ;
    }
    
    private boolean hasReUpdated(String uriStr)
    {
        return (StringUtils.isNotEmpty(uriStr) && uriStr.endsWith("?dlna=mov"));
    }
    private static final String []MimiTypeArray = {"video/quicktime", "video/mov", "video/mp4","video/3gpp","video/3gp"};
    private boolean isNetMimeType(String mimeType)
    {
        Log.d(TAG, "1---mimeType = " + mimeType);
        if (StringUtils.isEmpty(mimeType))
        {
            return false;
        }
        for (String str : MimiTypeArray)
        {
            if (mimeType.contains(str))
            {
                return true;
            }
        }
        return false;
    }
    
    public VideoInfo getNextMediaInfo()
    {
        Log.d(TAG, "PlayStateInfo -- getNextMediaInfo()");
        
        VideoInfo mbi = null;
        
        if (mMediaList == null)
        {
            return null;
        }
        
        synchronized (this)
        {
            while (mMediaList.size() > 0 && !isStop())
            {
                mCurrIndex += 1;
                Log.d(TAG, "getNextMediaInfo index:" + mCurrIndex);
                
                mbi = getCurrentPlayingMediaInfo();
                
                if (null == mbi)
                {
                    if (isCycPlayMode())
                    {
                        //回到第一个
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
                
                if (mbi.getUrl() != null && mbi.getUrl().trim().length() != 0)
                {
                    break;
                }
            }
        }
        
        mCurrentMBI = mbi;
        
        return mbi;
    }
    
    public VideoInfo getPreMediaInfo()
    {
        Log.d(TAG, "PlayStateInfo -- getPreMediaInfo()");
        VideoInfo mbi = null;
        
        if (mMediaList == null)
        {
            return null;
        }
        
        synchronized (this)
        {
            while (mMediaList.size() > 0)
            {
                mCurrIndex -= 1;
                Log.d(TAG, "getPreMediaInfo index:" + mCurrIndex);
                
                mbi = getCurrentPlayingMediaInfo();
                if (null == mbi)
                {
                    if (isCycPlayMode())
                    {
                        //回到最后一个
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
                
                if (mbi.getUrl() != null && mbi.getUrl().trim().length() != 0)
                {
                    break;
                }
            }
        }
        
        mCurrentMBI = mbi;
        
        return mbi;
    }
    
    
    
    public VideoInfo getIndexMediaInfo(int index)
    {
        Log.d(TAG, "PlayStateInfo -- getIndexMediaInfo()");
        VideoInfo mbi = null;
        
        if (mMediaList == null)
        {
            return null;
        }
        
        synchronized (this)
        {
            while (mMediaList.size() > 0)
            {
                mCurrIndex = index;
                Log.d(TAG, "getIndexMediaInfo index:" + mCurrIndex);
                
                mbi = getCurrentPlayingMediaInfo();
                if (null == mbi)
                {
                    if (isCycPlayMode())
                    {
                        //回到最后一个
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
                
                if (mbi.getUrl() != null && mbi.getUrl().trim().length() != 0)
                {
                    break;
                }
            }
        }
        
        mCurrentMBI = mbi;
        
        return mbi;
    }
    
    public void setMediaList(List<Bundle> mediaList)
    {
        if (mediaList == null)
        {
            Log.e(TAG, "setMediaList is null");
            return;
        }
        
        if (mediaList.size() == 0)
        {
            Log.e(TAG, "setMediaList size is zero");
            return;
        }
        
        synchronized (this)
        {
            
            List<VideoInfo> mbiList = getMediaBaseList(mediaList);
              
            if (mbiList == null)
            {
                Log.e(TAG, "mbiList is null after converting list");
                return;
            }            
            if (mbiList.size() == 0)
            {
                Log.e(TAG, "mbiList size is zero");
                return;
            }

            mMediaList = mbiList;
        }
        
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
     * getMediaList
     * 获取媒体数据
     * @return 
     *List<Bundle>
     * @exception
     */
    public List<VideoInfo> getMediaList()
    {
        return mMediaList;
    }
    
    /**
     * @param bStop the bStop to set
     */
    public void setStop(boolean bStop)
    {
        this.bStop = bStop;
        
        if (bStop)
        {
        }
        
    }
    
    /**
     * bStop
     *
     * @return  the bStop
     * @since   1.0.0
    */
    public boolean isStop()
    {
        return bStop;
    }
    
    //判断是否有U盘数据
    public boolean isIncludedUTypeData()
    {
        List<VideoInfo> mbiList = mMediaList;
        if (mbiList == null)
        {
            return false;
        }
        
        VideoInfo mbi = null;
        //遍历看是否存在U盘的数据
        for (int i = 0; i < mbiList.size(); i++)
        {
            mbi = mbiList.get(i);
            if (mbi == null)
            {
                continue;
            }
            
            if (mbi.getmDeviceType() == Constant.DeviceType.DEVICE_TYPE_U)
            {//存在数据
                return true;
            }
        }
        
        mbiList = null;
        
        return false;
        
    }
    
    //TODO ..
    //此函数需要提炼出来 在Activity2Data也有用到
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
            
            VideoInfo dbi1 = (VideoInfo)(object1);
            VideoInfo dbi2 = (VideoInfo)(object2);
            
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
                        
//            int iret = dbi1.getId() - dbi2.getId();
            int iret = 0;            
            if (iret == 0 && dbi1.getmDeviceType() == Constant.DeviceType.DEVICE_TYPE_U)
            {
                iret = collator.compare(dbi1.getmPhysicId(), dbi2.getmPhysicId());
            }
            
            return iret;
        }
    };
    
    /**l00174030;记录当前屏幕的状态 **/
    // 全屏模式
    private int screenMode = 1;
    
    public int getScreenMode()
    {
        return screenMode;
    }
    
    public void setScreenMode(int screenMode)
    {
        this.screenMode = screenMode;
    }
    
    /**
     * 清空播放列表，语音检索使用
     */
    public void destroyPlayState()
    {
    	synchronized (lock) 
		{
			playStateInfo = null;
		}   
    }
    
    public String getCurrDevName()
    {
        return mCurrDevName;
    }

    public void setCurrDevName(String mCurrDevName)
    {
        this.mCurrDevName = mCurrDevName;
    }
}
