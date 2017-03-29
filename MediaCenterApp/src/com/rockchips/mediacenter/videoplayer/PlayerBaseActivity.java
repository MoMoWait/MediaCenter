/**
 *
 * com.rockchips.iptv.stb.dlna.player
 * PlayerBaseActivity.java
 *
 * 2011-11-1-下午04:15:14
 * Copyright 2011 Huawei Technologies Co., Ltd
 *
 */
package com.rockchips.mediacenter.videoplayer;

import java.util.ArrayList;
import java.util.List;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import com.rockchips.mediacenter.bean.Device;
import com.rockchips.mediacenter.bean.FileInfo;
import com.rockchips.mediacenter.bean.LocalDeviceInfo;
import com.rockchips.mediacenter.utils.IICLOG;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.activity.DeviceActivity;
import com.rockchips.mediacenter.playerclient.MediaCenterPlayerClient;

/**
*
* PlayerBaseActivity
*
* 2011-11-1 下午04:15:14
*
* @version 1.0.0
*
*/
public abstract class PlayerBaseActivity extends DeviceActivity
{
    private static final String TAG = "PlayerBaseActivity";
    private IICLOG Log = IICLOG.getInstance();
    /**
     * save the player list & state
     */
    protected PlayStateInfo mPlayStateInfo = null;
    
    // MCS Client端
    protected MediaCenterPlayerClient mMediaCenterPlayerClient = null;
    
    //提示显示时间
    protected int TOAST_SHOW_TIME = 2000;
    
    //当前使用url
    protected String mStrCurrentUrl = null;
        
    protected static final int MCS_RECEIVE_MSG_MODE_REFUSE = 0;
    
    protected static final int MCS_RECEIVE_MSG_MODE_PROCESS = 1;
    
    //是否接受消息
    private int mbReceiveMsg = MCS_RECEIVE_MSG_MODE_REFUSE;
        
    protected static List<FileInfo> mFileInfos;
    protected static int mStcurrentIndex;
//    protected static String mStdevName;
    protected LocalDeviceInfo mSelectedDev;
    
    /**
     * 标记是否是从其他应用跳转到媒体中心视频播放器
     */
    protected Uri mExtraVideoUri;
    
    protected Device mCurrDevice;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreate - IN");
        // 解析Intent参数，并保存在mPlayStateInfo中
        parseInputIntent(getIntent());
        
        // 获取当前播放的媒体信息，媒体与索引有关。
        FileInfo currFileInfo = mPlayStateInfo.getCurrentMediaInfo();
        
        // 如果获取出来的信息是空的就直接finish掉
        if (currFileInfo == null)
        {
            Log.e(TAG, "Current Media Info is null");
            finish();
            return;
        }
        
        super.onCreate(savedInstanceState);
    }
    
    @Override
    protected void onRestart()
    {
        Log.d(TAG, "onRestart - IN");
        
        super.onRestart();
    }
    
    @Override
    protected void onStart()
    {
        Log.d(TAG, "onStart - IN");

        super.onStart();
    }
    
    @Override
    protected void onResume()
    {
        Log.d(TAG, "onResume - IN");
        
        getPlayStateInfo().setStop(false);
        
        setMCSReceiveMsgMode(MCS_RECEIVE_MSG_MODE_PROCESS);
        
        
        super.onResume();
    }
    
    @Override
    protected void onPause()
    {
        Log.d(TAG, "onPause - IN");
        
        //System.gc();
        
        super.onPause();
    }
    
    @Override
    protected void onStop()
    {
        Log.d(TAG, "onStop - IN");
        
        if (isFinishSelf())
        {
            Log.d(TAG, "Finish myself!");
            
            stop();
            finish();
        }
        
        super.onStop();
    }
    
    @Override
    protected void onDestroy()
    {
        Log.d(TAG, "onDestroy - IN");
        
        //设置拒绝信令
        setMCSReceiveMsgMode(MCS_RECEIVE_MSG_MODE_REFUSE);
        
        getPlayStateInfo().setStop(true);
        mPlayStateInfo = null;
        
        //System.gc();
        
        super.onDestroy();
    }
    
    /**
     * parseInputIntent
     * 解析Intent中的数据
     * @param intent
     * void
     * @exception
    */
    protected void parseInputIntent(Intent intent)
    {
        Log.i(TAG, "parseInputIntent --IN");
        
        if (intent == null)
        {
            Log.e(TAG, "parseInputIntent: intent = null");
            return;
        }
        mCurrDevice = (Device)intent.getSerializableExtra(ConstData.IntentKey.EXTRAL_LOCAL_DEVICE);      
        //获取当前播放索引
        int playIndex = intent.getIntExtra(ConstData.IntentKey.CURRENT_INDEX, 0);
        boolean isInternalPlayer = intent.getBooleanExtra(ConstData.IntentKey.IS_INTERNAL_PLAYER, false);       
        
        mPlayStateInfo = new PlayStateInfo();        
        
        // 如果是内部播放器，这直接返回
        if (isInternalPlayer)
        {
        	mPlayStateInfo.setMediaList(mFileInfos);
            mPlayStateInfo.setCurrentIndex(mStcurrentIndex);
            mPlayStateInfo.setCurrDevName(mCurrDevice.getDeviceName());
            return;
        }
        //不是内部播放器，则表示从其他应用跳转此处
        mExtraVideoUri = intent.getData();
        Log.i(TAG, "mExtraVideoUri:" + mExtraVideoUri);
        //设置当前播放
        mPlayStateInfo.setCurrentIndex(playIndex);
        if(mExtraVideoUri != null){
        	List<FileInfo> extraFileInfos = new ArrayList<FileInfo>();
        	FileInfo extraInfo = new FileInfo();
        	String extraPath = mExtraVideoUri.toString();
        	Log.i(TAG, "parseIntent->extraPath:" + extraPath);
        	if(extraPath != null){
        		extraInfo.setPath(extraPath);
        	}
        	
        	extraFileInfos.add(extraInfo);
        	mPlayStateInfo.setmMediaList(extraFileInfos);
        }
        
        Log.i(TAG, "parseInputIntent:playIndex " + playIndex);
    }

    
    
    protected void sendMessage(Message msg)
    {
      
    }
    
    /**
     * canReceiveMsg
     *
     * @return
     *boolean
     * @exception
    */
    public boolean canReceiveMsg()
    {
        return mbReceiveMsg == MCS_RECEIVE_MSG_MODE_PROCESS;
    }
    
    /**
     * canReceiveMsg
     *
     * @return
     *boolean
     * @exception
    */
    public void setMCSReceiveMsgMode(int mode)
    {
        mbReceiveMsg = mode;
    }
    
    protected FileInfo getCurrentMediaInfo()
    {
        if (mPlayStateInfo == null)
        {
            Log.e(TAG, "mPlayStateInfo null");
            return null;
        }
        
        FileInfo currFileInfo = mPlayStateInfo.getCurrentMediaInfo();
        if (currFileInfo == null)
        {
            Log.e(TAG, "getCurrentMediaInfo null");
            //            mStrCurrentUrl = null;
            return null;
        }
        
        if (currFileInfo.getPath() == null)
        {
            Log.e(TAG, "mStrCurrentUrl  null");
            
            return null;
        }
        
        mStrCurrentUrl = new String(currFileInfo.getPath());
        Log.d(TAG, "mStrCurrentUrl:" + mStrCurrentUrl);
        
        return currFileInfo;
    }
    
    protected String getCurMediaUrl()
    {
        return mStrCurrentUrl;
    }
    
   /**
    *
    * getPreMediaInfo
    * 获取mPlayStateInfo的前一个
    * @return
    *MediaBaseInfo
    * @exception
    */
    protected FileInfo getPreMediaInfo()
    {
        if (mPlayStateInfo == null)
        {
            Log.e(TAG, "mPlayStateInfo null");
            return null;
        }
        
        FileInfo preFileInfo = mPlayStateInfo.getPreMediaInfo();
        if (preFileInfo == null)
        {
            Log.e(TAG, "getPreMediaInfo null");
            //            mStrCurrentUrl = null;
            return null;
        }
        
        mStrCurrentUrl = new String(preFileInfo.getPath());
        return preFileInfo;
    }
    
   /**
    *
    * getPreMediaInfo
    * 获取mPlayStateInfo数据列表中下一个数据
    * @return
    *MediaBaseInfo
    * @exception
    */
    protected FileInfo getNextMediaInfo()
    {
        if (mPlayStateInfo == null)
        {
            Log.e(TAG, "mPlayStateInfo null");
            return null;
        }
        
        FileInfo nextFileInfo = mPlayStateInfo.getNextMediaInfo();
        
        if (nextFileInfo == null)
        {
            Log.e(TAG, "getNextMediaInfo null");
            //            mStrCurrentUrl = null;
            return null;
        }
        
        mStrCurrentUrl = new String(nextFileInfo.getPath());
        
        return nextFileInfo;
    }
    
   /**
    *
    * setPlayMode
    * 设置播放模式
    * @param playMode
    * void
    * @exception
    */
    protected void setPlayMode(int playMode)
    {
        mPlayStateInfo.setPlayMode(playMode);
    }
    
   /**
    *
    * getPlayMode
    * 获取播放模式
    * @return
    * int
    * @exception
    */
    protected int getPlayMode()
    {
        return mPlayStateInfo.getPlayMode();
    }
    
    /**
     *
     * getmPlayStateInfo
     * ��ȡ����״̬��Ϣ
     * @return
     * PlayStateInfo
     * @exception
     */
    public PlayStateInfo getPlayStateInfo()
    {
        return mPlayStateInfo;
    }
    
   /**
    *
    * getmPlayStateInfo
    * 获取播放状态信息
    * @return
    * PlayStateInfo
    * @exception
    */
    public void mcsDefaultMSGProcess(Intent intent)
    {
        
    }
    
    /**
     * mcsDelDevice
     *
     * @param intent
     * void
     * @exception
    */
    public void mcsDelDevice(Intent intent)
    {
        if (mPlayStateInfo != null)
        {
            if (mPlayStateInfo.getMediaList() == null || mPlayStateInfo.getMediaList().size() == 0)
            {
                Log.w(TAG, "mcsDelDevice CHECK LIST IS EMPTY, SO FINISH ACTIVITY");
                finish();
            }
        }
        else
        {
            Log.w(TAG, "mcsDelDevice CHECK LIST IS INVALIDATE, SO FINISH ACTIVITY");
            finish();
        }
    }
    
    /**
     * mcsAppendList
     *
     * @param intent
     * void
     * @exception
    */
    public void mcsAppendList(Intent intent)
    {
        
    }
    
    /********************************************************
     * abstract function start
     ********************************************************/
    
    /**
     * play
     *
     * void
     * @exception
    */
    protected abstract void play();
    
    /**
     * pause
     *
     * void
     * @exception
    */
    protected abstract void stop();
    
   /**
     * LoadResource
     * 加载资源
     * void
     * @exception
    */
    protected abstract void loadResource();
    
    protected boolean setMediaData()
    {
        return false;
    }
    

    public boolean isFinishSelf()
    {
        Log.d(TAG, "isFinishSelf --In");
        return false;
    }
    
    
    
//    public static void setMediaList(List<Bundle> bundleList, int currentIndex, String devName)
    public static void setMediaList(List<FileInfo> fileInfos, int currentIndex)
    {
        mFileInfos = fileInfos;
        mStcurrentIndex = currentIndex;
//        mStdevName = devName;       
    }
}
