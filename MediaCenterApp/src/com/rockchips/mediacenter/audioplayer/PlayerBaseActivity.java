/**
 *
 * com.rockchips.iptv.stb.dlna.player
 * PlayerBaseActivity.java
 *
 * 2011-11-1-下午04:15:14
 * Copyright 2011 Huawei Technologies Co., Ltd
 *
 */
package com.rockchips.mediacenter.audioplayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Process;
import com.rockchips.mediacenter.bean.FileInfo;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.utils.IICLOG;
import com.rockchips.mediacenter.utils.StringUtils;
import com.rockchips.mediacenter.activity.DeviceActivity;


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
    private static final String TAG = "AudioPlayer_Base";
    private IICLOG Log = IICLOG.getInstance();

    // 当前使用url
    protected String mStrCurrentUrl;
    
    protected static final int MCS_RECEIVE_MSG_MODE_REFUSE = 0;

    protected static final int MCS_RECEIVE_MSG_MODE_PROCESS = 1;

    // 是否接受消息
    private int mbReceiveMsg = MCS_RECEIVE_MSG_MODE_REFUSE;
    
    private Intent mExtraIntent = null;

    protected boolean mReuseAudioplayer = false;

    public boolean mIsInternalAudioPlayer = false;    

    protected AudioPlayStateInfo mAudioPlayStateInfo;
    private static List<FileInfo> mStBundleList;
    private static int mStCurrentIndex;
    /**
     * 从其他应用跳转至媒体中心的音乐播放器
     */
    protected Uri mExtraUri;
    protected FileInfo mExtraFileInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreate - IN: " + this);
        mExtraIntent = getIntent();
        mAudioPlayStateInfo = new AudioPlayStateInfo();
        mIsInternalAudioPlayer = mExtraIntent.getBooleanExtra(ConstData.IntentKey.IS_INTERNAL_PLAYER, false);
        if(!mIsInternalAudioPlayer){
        	mExtraUri = mExtraIntent.getData();
        	if(mExtraUri == null)
        		finish();
        	else{
        		List<FileInfo> extraFileInfos = new ArrayList<>();
        		mExtraFileInfo = new FileInfo();
        		mExtraFileInfo.setPath(mExtraUri.toString());
        		extraFileInfos.add(mExtraFileInfo);
        		mAudioPlayStateInfo.setMediaList(extraFileInfos, 0);
                mAudioPlayStateInfo.setCurrentIndex(0);
        	}
        }else{
        	 mAudioPlayStateInfo.setMediaList(mStBundleList, mStCurrentIndex);
             mAudioPlayStateInfo.setCurrentIndex(mStCurrentIndex);
        }
        super.onCreate(savedInstanceState);
        Log.d(TAG, "mExtraIntent is " + mExtraIntent);
        Log.d(TAG, "onCreate - OUT");
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
    protected void onNewIntent(Intent intent)
    {
        Log.d(TAG, "onNewIntent - IN: " + this);
        mExtraIntent = intent;
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent - OUT");
    }

    @Override
    protected void onResume()
    {
        Log.d(TAG, "onResume - IN: " + this);
        if (mExtraIntent != null)
        {
            // 解析Intent参数，并保存在mPlayStateInfo中
            if (!parseInputIntent(mExtraIntent))
            {
                finish();
            }
            mExtraIntent = null;
        }
        super.onResume();
        Log.d(TAG, "onResume - OUT");
    }

    @Override
    protected void onPause()
    {
        Log.d(TAG, "onPause - IN: " + this);
        super.onPause();
        Log.d(TAG, "onPause - OUT");
    }

    @Override
    protected void onStop()
    {
        Log.d(TAG, "onStop - IN: " + this);
        super.onStop();
        Log.d(TAG, "onStop - OUT");
    }

    @Override
    protected void onDestroy()
    {
        Log.d(TAG, "onDestroy - IN: " + this);

        mAudioPlayStateInfo.recycle();
        // System.gc();

        super.onDestroy();
        Log.d(TAG, "onDestroy - OUT");
    }

    private boolean mSupportToSeekWhenParepare = true;

    /**
     * parseInputIntent 解析Intent中的数据
     * 
     * @param intent void
     * @exception
     */
    protected boolean parseInputIntent(Intent intent)
    {
        Log.d(TAG, "parseInputIntent --IN");
        if (intent == null)
        {
            Log.d(TAG, "parseInputIntent: intent = null");
            return false;
        }
        mAudioPlayStateInfo.setNeedShowMenuBar(false);
      /*  Log.d(TAG, "isInternalPlayer is : " + mIsInternalAudioPlayer);
        if (mIsInternalAudioPlayer)
        {
            mAudioPlayStateInfo.syncList();
            return true;
        }
        mReuseAudioplayer = intent.getBooleanExtra(ConstData.IntentKey.IS_REUSE_AUDIOPLAYER, false);
        Log.d(TAG, "mReuseAudioplayer is : " + mReuseAudioplayer);      
        mAudioPlayStateInfo.setSenderClientUniq(ConstData.ClientTypeUniq.UNKNOWN_UNIQ);*/
        return true;
    }

    /**
     * getUUID
     * 
     * @return int
     * @exception
     */
    protected abstract int getUUID();

    /**
     * canReceiveMsg
     * 
     * @return boolean
     * @exception
     */
    public boolean canReceiveMsg()
    {
        return mbReceiveMsg == MCS_RECEIVE_MSG_MODE_PROCESS;
    }
    

    protected String getCurMediaUrl()
    {
        return mStrCurrentUrl;
    }

    /**
     * 
     * setPlayMode 设置播放模式
     * 
     * @param playMode void
     * @exception
     */
    protected void setPlayMode(int playMode)
    {
        mAudioPlayStateInfo.setPlayMode(playMode);
    }

    /**
     * 
     * getPlayMode 获取播放模式
     * 
     * @return int
     * @exception
     */
    protected int getPlayMode()
    {
        return mAudioPlayStateInfo.getPlayMode();
    }

    /**
     * mcsDefaultMSGProcess
     * 
     * @param intent void
     * @exception
     */
    public void mcsDefaultMSGProcess(Intent intent)
    {

    }

    /**
     * mcsDelDevice
     * 
     * @param intent void
     * @exception
     */
    public void mcsDelDevice(Intent intent)
    {
        if (mAudioPlayStateInfo.isPlayListEmpty())
        {
            Log.w(TAG, "mcsDelDevice CHECK LIST IS EMPTY, SO FINISH ACTIVITY");
//            exitQuickly();
        }

    }

    /**
     * mcsAppendList
     * 
     * @param intent void
     * @exception
     */
    public void mcsAppendList(Intent intent)
    {

    }

    /********************************************************
     * abstract function start
     ********************************************************/

    /**
     * 
     * getMediaType 获取播放器媒体类型 void
     * 
     * @exception
     */
    protected abstract int getMediaType();

    /**
     * 当删除了设备ID后，需要判断是否正在播放的源是在该id上，需要做响应的处理 onDeleteDeviceId
     * 
     * @return int
     * @exception
     */
    protected abstract int onDeleteDeviceId(String devId);

    /********************************************************
     * abstract function end
     ********************************************************/

    /**
     * 是否是我是媒体发送过来的请求 isSenderMyMedia
     * 
     * @return boolean
     * @exception
     */
    public boolean isSenderMyMedia()
    {
        if (mAudioPlayStateInfo.getSenderClientUniq().equals(ConstData.ClientTypeUniq.DMS_UNIQ))
        {
            return true;
        }
        else
        {
            return false;
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
        if (mAudioPlayStateInfo.getSenderClientUniq().equalsIgnoreCase(ConstData.ClientTypeUniq.PUSH_UNIQ))
        {
            return true;
        }

        if (mAudioPlayStateInfo.getSenderClientUniq().equalsIgnoreCase(ConstData.ClientTypeUniq.SYN_UINQ))
        {
            return true;
        }

        return false;
    }

    protected boolean setMediaData()
    {
        return false;
    }

    // 是否为推送
    protected boolean isDLNAPushType()
    {
        if (!StringUtils.isEmpty(mAudioPlayStateInfo.getSenderClientUniq()))
        {
            if (mAudioPlayStateInfo.getSenderClientUniq().equals(ConstData.ClientTypeUniq.PUSH_UNIQ))
            {
                return true;
            }
        }

        return false;
    }

    // 是否为甩屏端
    protected boolean isAirPushType()
    {
        if (!StringUtils.isEmpty(mAudioPlayStateInfo.getSenderClientUniq()))
        {
            if (mAudioPlayStateInfo.getSenderClientUniq().equals(ConstData.ClientTypeUniq.SYN_UINQ))
            {
                return true;
            }
        }

        return false;
    }

    // 是否为点击播放
    protected boolean isMyMediaType()
    {
        if (!StringUtils.isEmpty(mAudioPlayStateInfo.getSenderClientUniq()))
        {
            if (mAudioPlayStateInfo.getSenderClientUniq().equals(ConstData.ClientTypeUniq.DMS_UNIQ))
            {
                return true;
            }
        }

        return false;
    }
    

    public boolean isFinishSelfOnStop()
    {
        Log.d(TAG, "isFinishSelfOnStop --In");
        return true;
    }
    

    protected void exitQuickly()
    {
        Process.killProcess(Process.myPid());
    }

    // 推送过来请求的seek位置
    private int mRequestSeekPos = 0;

    /**
     * 用完后立即初始化为false
     * @return 返回 supportToSeekWhenParepare
     */
    public boolean isSupportToSeekWhenParepare()
    {
        return mSupportToSeekWhenParepare;
    }

    /**
     * @param 对supportToSeekWhenParepare进行赋值
     */
    public void setSupportToSeekWhenParepare(boolean supportToSeekWhenParepare)
    {
        this.mSupportToSeekWhenParepare = supportToSeekWhenParepare;
    }

    /**
     * @return 返回 mRequestSeekPos
     */
    public int getRequestSeekPos()
    {
        return mRequestSeekPos;
    }

    /**
     * @param 对mRequestSeekPos进行赋值
     */
    public void setRequestSeekPos(int requestSeekPos)
    {
        this.mRequestSeekPos = requestSeekPos;
    }

    class MediaListPackage
    {
        long package_id;

        int package_count;

        int orderId;

        ArrayList<Bundle> mMediaInfoList;
    }
    
    public static void setMediaList(List<FileInfo> bundleList, int currentIndex)
    {
        mStBundleList =  bundleList;
        mStCurrentIndex = currentIndex;        
    }
    
}
