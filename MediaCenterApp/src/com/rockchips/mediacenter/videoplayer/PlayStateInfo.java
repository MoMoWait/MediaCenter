package com.rockchips.mediacenter.videoplayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.rockchips.mediacenter.bean.FileInfo;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.utils.IICLOG;
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
    private List<FileInfo> mMediaList = null;
    
    // 当前播放索引
    private int mCurrIndex = 0;
    
    // 当前播放的设备名
    private String mCurrDevName = "";
    
    private int playMode = ConstData.MediaPlayMode.MP_MODE_SINGLE;
    
    /**
     * 默认2D
     */
    private int m3DMode = ConstData.ThreeDMode.TWO_D;
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
    private FileInfo mCurrentMBI = null;
    
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
        if (playMode == ConstData.MediaPlayMode.MP_MODE_ALL_CYC)
        {
            return true;
        }
        
        /*if (playMode == ConstData.MediaPlayMode.MP_MODE_RONDOM)
        {
            return true;
        }*/
        
     /*   if (playMode == ConstData.MediaPlayMode.MP_MODE_SINGLE_CYC)
        {
            return true;
        }*/
        
        return false;
    }
    
    //判断如果是单曲模式【单曲循环】/【单曲】
    public boolean isSingleMode()
    {
        
        if (playMode == ConstData.MediaPlayMode.MP_MODE_SINGLE_CYC)
        {
            return true;
        }
        
        if (playMode == ConstData.MediaPlayMode.MP_MODE_SINGLE)
        {
            return true;
        }
        
        return false;
    }
    

    
    
    public FileInfo getCurrentMediaInfo()
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
    public FileInfo getCurrentPlayingMediaInfo()
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
        FileInfo item = mMediaList.get(mCurrIndex);
        return item ;
    }
    
    
    public FileInfo getNextMediaInfo()
    {
        Log.d(TAG, "PlayStateInfo -- getNextMediaInfo()");
        
        FileInfo mbi = null;
        
        if (mMediaList == null)
        {
            return null;
        }
        
        if(playMode == ConstData.MediaPlayMode.MP_MODE_SINGLE_CYC)
        	return mCurrentMBI;
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
                
                if (mbi.getPath() != null && mbi.getPath().trim().length() != 0)
                {
                    break;
                }
            }
        }
        
        mCurrentMBI = mbi;
        
        return mbi;
    }
    
    public FileInfo getPreMediaInfo()
    {
        Log.d(TAG, "PlayStateInfo -- getPreMediaInfo()");
        FileInfo mbi = null;
        
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
                
                if (mbi.getPath() != null && mbi.getPath().trim().length() != 0)
                {
                    break;
                }
            }
        }
        
        mCurrentMBI = mbi;
        
        return mbi;
    }
    
    
    
    public FileInfo getIndexMediaInfo(int index)
    {
        Log.d(TAG, "PlayStateInfo -- getIndexMediaInfo()");
        FileInfo mbi = null;
        
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
                
                if (mbi.getPath() != null && mbi.getPath().trim().length() != 0)
                {
                    break;
                }
            }
        }
        
        mCurrentMBI = mbi;
        
        return mbi;
    }
    
    public void setMediaList(List<FileInfo> mediaList)
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
        mMediaList = new ArrayList<>(mediaList);
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
    public List<FileInfo> getMediaList()
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
    
    /**
     * 设置3D模式
     * @param mode
     */
    public void set3DMode(int mode){
    	m3DMode = mode;
    }
    
    /**
     * 获取3D模式
     * @return
     */
    public int get3DMode(){
    	return m3DMode;
    }
    
    /**
     * 设置播放列表
     * @param infoList
     */
    public void setmMediaList(List<FileInfo> infoList){
    	mMediaList = infoList;
    }
}
