/**
 * Title: AudioPlayerService.java<br>
 * Package: com.rockchips.mediacenter.audioplayer<br>
 * Description: TODO<br>
 * @author c00224451
 * @version v1.0<br>
 * Date: 2014年9月28日下午2:40:54<br> 
 * Copyright © Huawei Technologies Co., Ltd. 2014. All rights reserved.
 */

package com.rockchips.mediacenter.audioplayer;

import java.util.ArrayList;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import com.hisilicon.android.mediaplayer.HiMediaPlayer;
import com.rockchips.mediacenter.basicutils.bean.LocalMediaInfo;
import com.rockchips.mediacenter.basicutils.constant.Constant;
import com.rockchips.mediacenter.basicutils.constant.Constant.AudioPlayerMsg;
import com.rockchips.mediacenter.basicutils.util.IICLOG;
import com.rockchips.mediacenter.config.PlatformConfig;
import com.rockchips.mediacenter.portable.IMediaPlayerAdapter;
import com.rockchips.mediacenter.portable.IVideoViewAdapter;
import com.rockchips.mediacenter.portable.hisi.HisiVideoViewNoView;
import com.rockchips.mediacenter.portable.listener.OnCompleteListener;
import com.rockchips.mediacenter.portable.listener.OnErrorListener;
import com.rockchips.mediacenter.portable.listener.OnInfoListener;
import com.rockchips.mediacenter.portable.listener.OnPreparedListener;
import com.rockchips.mediacenter.portable.orig.OrigVideoViewNoView;
import com.rockchips.mediacenter.playerclient.MediaCenterPlayerClient;

/**
 * Description: TODO<br>
 * @author c00224451
 * @version v1.0
 * Date: 2014年9月28日 下午2:40:54<br>
 */

public class AudioPlayerService extends Service
{
    private static final String TAG = "MediaCenterApp";
    private IICLOG Log = IICLOG.getInstance();
    
    private PlayerCallbackMessenger mPlayerCallbackMessenger;
    
    protected static final int MCS_RECEIVE_MSG_MODE_REFUSE = 0;
    protected static final int MCS_RECEIVE_MSG_MODE_PROCESS = 1;
    // 是否接受消息
    private int mbReceiveMsg = MCS_RECEIVE_MSG_MODE_REFUSE;    
    
    private String mSenderClientUniq;
    
//    private RemoteController mRemoteControlCallback = null;
    // MCS Client端
    protected MediaCenterPlayerClient mMediaCenterPlayerClient;
    
    // 音量管理器，用于调节系统音量
    protected AudioManager mAudioManager;   
    
    private HandlerThread mMCSHandlerThread;
    private Object mHandlerLock = new Object();
    /**
     * 数据处理handler
     */
    private Handler mLogicalHandler;

    /**
     * 数据处理线程
     */
    private HandlerThread mLogicalThread;
    
    /**
     * 音乐播放器对象
     */
    private MusicPlayer mMusicPlayer;
    private long mBeginPrepareTime;
    private long mEndPrepareTime;
    private LocalMediaInfo mCurrentMediaInfo;
    
    private boolean mPausedByTransientLossOfFocus;
    
    private int mPlayIndex;
    private ArrayList<Bundle> mMediaBaseList;    
    
    private int mSeekTarget = 0;

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }
    
    @Override
    public void onCreate()
    {        
        // 初始化音量管理器
        if (mAudioManager == null)
        {
            Log.d(TAG, "audioManager == null, create a AudioManager object");
            mAudioManager = (AudioManager) this.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        }
        
        mMusicPlayer = new MusicPlayer();
        
        synchronized (mHandlerLock)
        {
            // 创建过程执行线程
            mLogicalThread = new HandlerThread(this.getClass().toString());
            mLogicalThread.start();
            mLogicalHandler = new Handler(mLogicalThread.getLooper(), mLogicalHandlerCallback);

        }
        super.onCreate();        
    }
    

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {       
        mbReceiveMsg = MCS_RECEIVE_MSG_MODE_PROCESS;        
        if (intent != null)
        {
            parseInputIntent(intent);
            dobind();
            requestPlay(0);            
        }
        else
        {
            this.stopSelf(startId);
        }       
        
        Log.d(TAG, "onStartCommand startId = " + startId);
        int ret = super.onStartCommand(intent, flags, startId);
        return ret;
        
    }
    
    
    
    @Override
    public void onDestroy()
    {
        mbReceiveMsg = MCS_RECEIVE_MSG_MODE_REFUSE;        
        destroy();
        super.onDestroy();        
    }

    /**
     * 
     * dobind：建立与MediaCenterService之间的绑定，并注册回调
     * 
     * void
     * 
     * @exception
     */
    private void dobind()
    {
        Log.i(TAG, "dobind - IN ");        
        if (mMCSHandlerThread == null)
        {
            Log.d(TAG, "Create mMCSHandlerThread and start it");

            mMCSHandlerThread = new HandlerThread("MCS_IN_PLAYER");
            mMCSHandlerThread.start();
        }

        if (null == mPlayerCallbackMessenger)
        {
            Log.d(TAG, "Create mPlayerCallbackMessenger");

            mPlayerCallbackMessenger = new PlayerCallbackMessenger(mMCSHandlerThread.getLooper());
        }

        if (mMediaCenterPlayerClient == null)
        {
            Log.d(TAG, "Create mMediaCenterPlayerClient");

            mMediaCenterPlayerClient = new MediaCenterPlayerClient();
        }

        Log.d(TAG, " start to connect MCS service ");
        if (!(mMediaCenterPlayerClient.isConnected()))
        {
            mMediaCenterPlayerClient.setPlayerType(Constant.MediaType.AUDIO);            
            mMediaCenterPlayerClient.setSenderUniq(mSenderClientUniq);

            mMediaCenterPlayerClient.registerPlayerCallBack(mPlayerCallbackMessenger);
            mMediaCenterPlayerClient.doBindService(this.getApplicationContext());
            mMediaCenterPlayerClient.setListener(mConnectListenerHandler);
        }
        Log.d(TAG, " end with connect MCS service ");
    }
    
    /**
     * 
     * unbind：解除与MediaCenterService之间的绑定，并取消注册的回调
     * 
     * void
     * 
     * @exception
     */
    protected void unbind()
    {
        // 对解绑定的过程加把锁
        synchronized (this)
        {
            Log.i(TAG, " unbind() - IN ");
            if (mMediaCenterPlayerClient != null)
            {
                Log.d(TAG, "mMediaCenterPlayerClient != null");

                // 只有在连接的情况下，才进行反注册和解除连接
                if (mMediaCenterPlayerClient.isConnected())
                {
                    Log.d(TAG, "mMediaCenterPlayerClient.isConnected()");

                    if (mPlayerCallbackMessenger != null)
                    {
                        Log.d(TAG, "mPlayerCallbackMessenger != null");

                        mMediaCenterPlayerClient.unregisterPlayerCallBack();
                    }

                    mMediaCenterPlayerClient.doUnbindService();
                }
                mMediaCenterPlayerClient = null;
            }

            try
            {
                if (mMCSHandlerThread != null && mMCSHandlerThread.isAlive())
                {
                    Log.d(TAG, "Quit mMCSHandlerThread");

                    mMCSHandlerThread.getLooper().quit();
                }

            }
            catch (Exception e)
            {

            }

            mMCSHandlerThread = null;

            mPlayerCallbackMessenger = null;

            Log.d(TAG, " end with disconnect MCS service ");
        }
    }

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

        // 获取当前播放索引
        mPlayIndex = intent.getIntExtra(Constant.IntentKey.CURRENT_INDEX, 0);
        
        // 解析当前播放列表
        mMediaBaseList = intent.getParcelableArrayListExtra(Constant.IntentKey.MEDIA_INFO_LIST);
              
        // 获取客户端唯一标识
        mSenderClientUniq = intent.getStringExtra(Constant.IntentKey.UNIQ);        
        Log.d(TAG, "senderClientUniq is :" + mSenderClientUniq);        
        // DTS2012030204438
        if (mMediaCenterPlayerClient != null)
        {
            Log.d(TAG, "Reset the sender client in the player client");            
            mMediaCenterPlayerClient.setSenderUniq(mSenderClientUniq);
        }
        int size = 0;
        if (mMediaBaseList != null)
        {
            size = mMediaBaseList.size();
        }
        Log.i(TAG, "parseInputIntent:playIndex " + mPlayIndex + ",MediaList size is " + size);

        return true;
    }
    
    /**
     * 收到媒体中心发过来的控制播放的消息
     * 
     * PlayerCallbackMessenger
     * 
     * 2011-10-20 上午10:58:02
     * 
     * @version 1.0.0
     * 
     */
    private class PlayerCallbackMessenger extends Handler
    {
        public PlayerCallbackMessenger(Looper looper)
        {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg)
        {

            if (!canReceiveMsg())
            {
                super.handleMessage(msg);
                return;
            }

            Intent intent = (Intent) msg.obj;
            switch (msg.what)
            {
                case Constant.MCSMessage.MSG_SET_MEDIA_DATA:
                    Log.d(TAG, "MSG_SET_MEDIA_DATA start");
                    parseInputIntent(intent);  
                    setDataSource();                                        
                    Log.d(TAG, "MSG_SET_MEDIA_DATA end");

                    break;
                case Constant.MCSMessage.MSG_PLAY:
                    Log.d(TAG, "MSG_PLAY start");
                    play();                                        
                    Log.d(TAG, "MSG_PLAY end");

                    break;
                case Constant.MCSMessage.MSG_PAUSE:
                    Log.d(TAG, "MSG_PAUSE start");
                    pause();    
                    Log.d(TAG, "MSG_PAUSE end");

                    break;
                case Constant.MCSMessage.MSG_SEEK:
                    Log.d(TAG, "MSG_SEEK start"); 
                    seekTo(intent);
                    Log.d(TAG, "MSG_SEEK end");

                    break;
                case Constant.MCSMessage.MSG_STOP:
                    Log.d(TAG, "MSG_STOP start");

                    // 收到stop信令就进行解绑操作，避免由于在onDestroy()中解绑太慢，导致MCS中刚注册上的播放器回调被正销毁的播放器反注册掉
                    // 执行拉回时，stop状态已经在MCS中返回给Sender端
                    unbind();                    
                    stop();  
                    stopSelf();  
                    Log.d(TAG, "MSG_STOP end");

                    break;
                case Constant.MCSMessage.MSG_APPEND_MEDIA_DATA:
                    Log.d(TAG, "MSG_APPEND_MEDIA_DATA start");
                    break;
                case Constant.MCSMessage.MSG_DEVICE_DOWN:
                    Log.d(TAG, "MSG_DEVICE_DOWN start");
                    String deviceId = intent.getStringExtra(Constant.IntentKey.DEVICE_ID);
                    Log.d(TAG, "deviceId:" + deviceId);

                    if (null == deviceId || deviceId.trim().equals(""))
                    {
                        Log.e(TAG, "The deviceId transfered from the DMS client is null");
                        break;
                    }

                    break;
                case Constant.MCSMessage.MSG_ADJUST_VOLUME:
                    Log.d(TAG, "MSG_ADJUST_VOLUME start");

                    int volumeAdjustType = intent.getIntExtra(Constant.IntentKey.VOLUME_ADJUST_TYPE, Constant.VolumeAdjustType.ADJUST_UNKNOWND);
                    Log.d(TAG, "volumeAdjustType:" + volumeAdjustType);

                    if (volumeAdjustType != Constant.VolumeAdjustType.ADJUST_UNKNOWND)
                    {
                        if (mAudioManager == null)
                        {
                            Log.d(TAG, "audioManager is null, new a AudioManager object");
                            mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                        }

                        if (volumeAdjustType == Constant.VolumeAdjustType.ADJUST_LOWER)
                        {
                            Log.d(TAG, "Adjust the volume to lower");

                            mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER,
                                    AudioManager.FX_FOCUS_NAVIGATION_UP);

                            // 将设置好的音量值返回给Sender端
                            if (mMediaCenterPlayerClient != null)
                            {
                                Log.d(TAG, "Send adjust volume lower to Sender client");
                                mMediaCenterPlayerClient.adjustVolume(Constant.VolumeAdjustType.ADJUST_LOWER, -1);
                            }

                            return;
                        }
                        else if (volumeAdjustType == Constant.VolumeAdjustType.ADJUST_SAME)
                        {
                            Log.d(TAG, "Not Adjust the volume");

                            mAudioManager
                                    .adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_SAME, AudioManager.FX_FOCUS_NAVIGATION_UP);

                            // 将设置好的音量值返回给Sender端
                            if (mMediaCenterPlayerClient != null)
                            {
                                Log.d(TAG, "Send adjust volume same to Sender client");
                                mMediaCenterPlayerClient.adjustVolume(Constant.VolumeAdjustType.ADJUST_SAME, -1);
                            }

                            return;
                        }
                        else if (volumeAdjustType == Constant.VolumeAdjustType.ADJUST_RAISE)
                        {
                            Log.d(TAG, "Adjust the volume to raise");

                            mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE,
                                    AudioManager.FX_FOCUS_NAVIGATION_UP);

                            // 将设置好的音量值返回给Sender端
                            if (mMediaCenterPlayerClient != null)
                            {
                                Log.d(TAG, "Send adjust volume raise to Sender client");
                                mMediaCenterPlayerClient.adjustVolume(Constant.VolumeAdjustType.ADJUST_RAISE, -1);
                            }

                            return;
                        }
                        else if (volumeAdjustType == Constant.VolumeAdjustType.ADJUST_MUTE_ON)
                        {
                            Log.d(TAG, "Turn on the mute mode");

                            mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);

                            // add by xWX184171 2014.3.19 显示音量条
                            mAudioManager
                                    .adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_SAME, AudioManager.FX_FOCUS_NAVIGATION_UP);

                            // 将设置好的音量值返回给Sender端
                            if (mMediaCenterPlayerClient != null)
                            {
                                Log.d(TAG, "Send mute on to Sender client");
                                mMediaCenterPlayerClient.adjustVolume(Constant.VolumeAdjustType.ADJUST_MUTE_ON, -1);
                            }

                            return;
                        }
                        else if (volumeAdjustType == Constant.VolumeAdjustType.ADJUST_MUTE_OFF)
                        {
                            Log.d(TAG, "Close the mute mode");

                            mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);

                            // add by xWX184171 2014.3.19 显示音量条
                            mAudioManager
                                    .adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_SAME, AudioManager.FX_FOCUS_NAVIGATION_UP);

                            // 将设置好的音量值返回给Sender端
                            if (mMediaCenterPlayerClient != null)
                            {
                                Log.d(TAG, "Send mute off to Sender client");
                                mMediaCenterPlayerClient.adjustVolume(Constant.VolumeAdjustType.ADJUST_MUTE_OFF, -1);
                            }

                            return;
                        }
                        else if (volumeAdjustType == Constant.VolumeAdjustType.ADJUST_SET)
                        {
                            Log.d(TAG, "Set the volume to a fixed value");

                            float volumePercent = intent.getFloatExtra(Constant.IntentKey.VOLUME_SET_VALUE, -1);
                            Log.d(TAG, "volumePercent:" + volumePercent);

                            if (volumePercent < 0 || volumePercent > 1)
                            {
                                Log.d(TAG, "The value of volumeValue not in [0, 1]");
                                return;
                            }
                            

                            int volumeValueIndex = (int) (mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * volumePercent);
                            Log.d(TAG, "volumeValueIndex:" + volumeValueIndex);
                            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volumeValueIndex, AudioManager.FX_FOCUS_NAVIGATION_UP);

                            // 将设置好的音量值返回给Sender端
                            if (mMediaCenterPlayerClient != null)
                            {
                                Log.d(TAG, "Send the volume percent to Sender client");
                                mMediaCenterPlayerClient.adjustVolume(Constant.VolumeAdjustType.ADJUST_SET, volumePercent);
                            }
                            return;
                        }
                    }

                    Log.d(TAG, "MSG_ADJUST_VOLUME end");

                    break;
                default:
                    Log.d(TAG, "UNKOWN MSG FROM MCS: " + msg.what + "  -- start");

//                    mcsDefaultMSGProcess(intent);

                    Log.d(TAG, "UNKOWN MSG FROM MCS: " + msg.what + "  -- end");

                    break;
            }
            super.handleMessage(msg);
        }
    }
    
    public boolean canReceiveMsg()
    {
        return mbReceiveMsg == MCS_RECEIVE_MSG_MODE_PROCESS;
    }
    
    private Handler mConnectListenerHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case Constant.ServiceConnectionMSG.MSG_SERVICE_DISCONNECTED:
                    Log.d(TAG, "------------->proc MSG_SERVICE_DISCONNECTED IN");                    
                    onRemoteDisconnect();                    
                    Log.d(TAG, "------------->proc MSG_SERVICE_DISCONNECTED OUT");
                    break;

                default:
                    break;
            }
        };
    };
    
    private RemoteCallback mRemoteCallback = new RemoteCallback()
    {
        @Override
        public void onStop()
        {
            Log.d(TAG, "RemoteCallback::onStop - IN");
            removeLogicalMessage(AudioPlayerMsg.MSG_SYNC_POSTION);
            if (getPlayerClient() != null && getPlayerClient().isConnected())
            {
                Log.d(TAG, "----------->MediaCenterPlayerClient::stop()");
                if (mCurrentMediaInfo != null)
                {
                    getPlayerClient().stop(mCurrentMediaInfo.getUrl());
                }
            }
            destroy();

        }

        @Override
        public void onProgress(int pos)
        {            
            if (getPlayerClient() != null && getPlayerClient().isConnected())
            {
                Log.d(TAG, "----------->MediaCenterPlayerClient::seek()" + pos);
                getPlayerClient().seek(pos);
            }

        }

        @Override
        public void onPlay()
        {
            Log.d(TAG, "RemoteCallback::onPlay - IN");
            if (getPlayerClient() != null && getPlayerClient().isConnected())
            {
                Log.d(TAG, "----------->MediaCenterPlayerClient::play()");
                getPlayerClient().play();
            }

        }

        @Override
        public void onPause()
        {
            Log.d(TAG, "RemoteCallback::onPause - IN");
            if (getPlayerClient() != null && getPlayerClient().isConnected())
            {
                Log.d(TAG, "----------->MediaCenterPlayerClient::pause()");
                getPlayerClient().pause();
            }

        }

        @Override
        public void onPrepared(IMediaPlayerAdapter mp)
        {
            Log.d(TAG, "RemoteCallback::onPrepared - IN");
            if (getPlayerClient() != null && getPlayerClient().isConnected())
            {

                int duration = mp.getDuration();
                int position = mp.getCurrentPosition();
                Log.d(TAG, "----------->MediaCenterPlayerClient::reportDuration()" + duration);
                getPlayerClient().reportDuration(position, duration);

//                if (isSupportToSeekWhenParepare())
//                {
//
//                }
//                else
//                {
//                    Log.d(TAG, "----------->MediaCenterPlayerClient::seek(0)");
//                    getPlayerClient().seek(0);
//                }
            }

        }

        @Override
        public void onCompletion(IMediaPlayerAdapter mp)
        {
            Log.d(TAG, "RemoteCallback::onCompletion - IN");
            removeLogicalMessage(AudioPlayerMsg.MSG_SYNC_POSTION);
            if (getPlayerClient() != null && getPlayerClient().isConnected())
            {
                int duration = mp.getDuration();
                getPlayerClient().seek(duration); // 播放完成，把总时长当成当前进度，通知推送端，以便能继续推送下一首

//                getPlayerClient().stop();
            }
        }

        @Override
        public void onError(IMediaPlayerAdapter mp, int what, int extra)
        {
            Log.d(TAG, "RemoteCallback::onError - IN");
            if (getPlayerClient() != null && getPlayerClient().isConnected())
            {
//                getPlayerClient().stop();
            }
        }
    };
    
    public MediaCenterPlayerClient getPlayerClient()
    {
        return mMediaCenterPlayerClient;
    }
    
    public void setDataSource()
    {        
        Log.d(TAG, "---------->setDataSource(Intent intent)");        
//        stop();
        mSeekTarget = 0;
        requestPlay(0);
    }
    
    public void play()
    {
        Log.d(TAG, "---------->play(Intent intent)");                
        if (mMusicPlayer != null && mMusicPlayer.isInitialized())
        {
            mMusicPlayer.start();
            if (mRemoteCallback != null)
            {
                mRemoteCallback.onPlay();
            }
        }    
    }
    
    public void pause()
    {
        Log.d(TAG, "---------->pause(Intent intent)");               
        if (mMusicPlayer != null && mMusicPlayer.isPlaying())
        {
            mMusicPlayer.pause();
            if (mRemoteCallback != null)
            {
                mRemoteCallback.onPause();
            }
        }     
    }
    
    public void seekTo(Intent intent)
    {
        Log.d(TAG, "---------->seekTo(Intent intent)");
        
        int targetPostion;
        try
        {
            targetPostion = intent.getIntExtra(Constant.IntentKey.SEEK_POS, -1);
        }
        catch (Exception e)
        {                
            targetPostion = -1;
        }
        try
        {
            if (targetPostion == -1)
            {
                // 尝试百分比
                Log.d(TAG, "will calculate by percent!!!");
                float postionPercent = intent.getFloatExtra(Constant.IntentKey.SEEK_POS, -1);
                Log.d(TAG, "postionPercent is " + postionPercent);
                if (postionPercent < 1.0)
                {
                    int totalDuration = -1;
                    if (mMusicPlayer != null && mMusicPlayer.isInitialized())
                    {
                        totalDuration = (int)mMusicPlayer.getDuration();
                    }
                    targetPostion = (int)(totalDuration * postionPercent);
                }
                else
                {
                    targetPostion = (int)(postionPercent);
                }
                Log.d(TAG, "targetPostion is " + targetPostion);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            targetPostion = -1;
        }
        
        if (targetPostion >= 0)
        {     
            mSeekTarget = targetPostion;
            if (mMusicPlayer != null)
            {
                mMusicPlayer.seekTo();
            }
        }        
    }
    
    public void stop()
    {
        Log.d(TAG, "---------->stop(Intent intent)");
        if (mMusicPlayer != null)
        {
            mMusicPlayer.resetPlayer();
            mMusicPlayer.release();
        }
        if (mRemoteCallback != null)
        {
            mRemoteCallback.onStop();
        }
    }
    
    /**
     * 请求播放播放列表中索引为index的媒体文件
     * 
     * @param index 媒体文件在播放列表中的索引值
     */
    private void requestPlay(int index)
    {
        Log.d(TAG, "requestPlay() IN");
        Log.d(TAG, "input-parameter index is : " + index);

        LocalMediaInfo LocalMediaInfo = getMediaInfoByIndex(index);

        Log.d(TAG, "mCurrentMediaInfo is " + mCurrentMediaInfo);
        Log.d(TAG, "LocalMediaInfo is " + LocalMediaInfo);
        if (LocalMediaInfo != null && LocalMediaInfo != mCurrentMediaInfo)
        {
            removeLogicalMessage(AudioPlayerMsg.MSG_REQUEST_PLAY);
            sendLogicalMessage(obtainLogicalMessage(AudioPlayerMsg.MSG_REQUEST_PLAY, 0, 0, LocalMediaInfo), 0);
        }
        else
        {
            // Log.d("1111", "2222");
            removeLogicalMessage(AudioPlayerMsg.MSG_REQUEST_PLAY);
        }
        Log.d(TAG, "requestPlay() OUT");
    }
    
    private LocalMediaInfo getMediaInfoByIndex(int index)
    {
        if (null == mMediaBaseList)
        {
            return null;
        }
        if (index >= mMediaBaseList.size())
        {
            return null;
        }
        LocalMediaInfo hMediaInfo = new LocalMediaInfo();
        hMediaInfo.decompress(mMediaBaseList.get(index));
        return hMediaInfo;
    }
    
    public void onRemoteDisconnect()
    {
        Log.d(TAG, "---------->onRemoteDisconnect()");
        destroy();
    }
    
    public int getDuration()
    {
        Log.d(TAG, "---------->getDuration()");
        if (mMusicPlayer != null && mMusicPlayer.isInitialized())
        {
            return (int)mMusicPlayer.getDuration();
        }
        else
        {
            return 0;
        }
    }
    
    public int getPosition()
    {
        Log.d(TAG, "---------->getPosition()");
        if (mMusicPlayer != null && mMusicPlayer.isInitialized())
        {
            return mMusicPlayer.getCurrentPosition();
        }
        else
        {
            return 0;
        }
    }
    
    private IVideoViewAdapter mMediaPlayer = null;

    private Object mPlayerLock = new Object();
    
    private class MusicPlayer
    {
        // private IMediaPlayerAdapter mMediaPlayer = null;

        private Object miObj = new Object();

        private LocalMediaInfo mi = null;

        private boolean mIsInitialized = false;

        public MusicPlayer()
        {
            Log.d(TAG, "---------->MusicPlayer()");
        }

        public void setMeidaInfo(LocalMediaInfo mi)
        {
            synchronized (miObj)
            {
                this.mi = mi;
            }
        }

        public void resetPlayer()
        {
            Log.d(TAG, "---------->resetPlayer()");
            release();
        }

        public void setDataSourceAsync(String path)
        {
            Log.d(TAG, "---------->setDataSourceAsync()");
            resetPlayer();
            synchronized (mPlayerLock)
            {
                try
                {
                    // TODO:海思播放器暂不能播放
                    if (PlatformConfig.isSupportHisiMediaplayer())
                    {
                        // himediaplayer.java
                        HisiVideoViewNoView tmp = new HisiVideoViewNoView(getApplicationContext());
                        mMediaPlayer = tmp;
                    }
                    else
                    {
                        OrigVideoViewNoView tmp = new OrigVideoViewNoView(getApplicationContext());
                        mMediaPlayer = tmp;
                    }

                    mIsInitialized = false;
                    mMediaPlayer.setOnPreparedListener(preparedListener);
                    mMediaPlayer.setOnCompletionListener(mOnCompletionListener);
                    mMediaPlayer.setOnErrorListener(mOnErrorListener);
                    mMediaPlayer.setOnInfoListener(mOnInfoListener);                                       

                    mMediaPlayer.setVideoURI(Uri.parse(Uri.encode(path)));
                    mMediaPlayer.start();
                }                
                catch (IllegalArgumentException ex)
                {
                    mMediaPlayer = null;
                    removeLogicalMessage(AudioPlayerMsg.MSG_PROC_ERROR);
                    sendLogicalMessage(AudioPlayerMsg.MSG_PROC_ERROR, 0);
                    return;
                }
                catch (RuntimeException ex)
                {
                }

            }

        }

        public boolean isInitialized()
        {
            synchronized (mPlayerLock)
            {
                return mIsInitialized;
            }
        }

        public void start()
        {
            Log.d(TAG, "---------->start()");
            synchronized (mPlayerLock)
            {
                if (mIsInitialized && mMediaPlayer != null)
                {
                    mMediaPlayer.start();                    
//                    if (mIsResumeNeedSeek)
//                    {
//                        Log.d(TAG, "after this MediaPlayer start.we're seek duration to onPause save the position.");
//                        mMusicPlayer.seekTo(mResumeNeedSeekValue);
//                        int totalDuration = mMediaPlayer.getDuration();
//                        mIsResumeNeedSeek = false;
//                        mResumeNeedSeekValue = 0;
//                    }

                }
            }

        }

        public void stop()
        {
            synchronized (mPlayerLock)
            {
                Log.d(TAG, "---------->stop()");
                if (mIsInitialized)
                {
                    mIsInitialized = false;
                }
//                mAudioManager.abandonAudioFocus(mAudioFocusListener);
                Log.d(TAG, "---------->mAudioManager.abandonAudioFocus() end !");                
            }
        }
        
        public boolean isPlaying()
        {
            synchronized (mPlayerLock)
            {
                if (mMediaPlayer != null)
                {
                    return mMediaPlayer.isPlaying();
                }
                return false;
            }
        }

        public void release()
        {
            Log.d(TAG, "---------->release()");
            stop();

            synchronized (mPlayerLock)
            {
                if (mMediaPlayer != null)
                {
                    // FIXME: 快速切换歌曲时，无法退出
                    Log.d(TAG, "--------> MediaPlayer.release() start");
                    mMediaPlayer.stopPlayback();
                    mMediaPlayer = null;
                    Log.d(TAG, "--------> MediaPlayer.release() end: ok");

                }
            }

        }

        public void pause()
        {
            Log.d(TAG, "---------->pause()");
            synchronized (mPlayerLock)
            {
                if (mIsInitialized && mMediaPlayer != null)
                {
                    mMediaPlayer.pause();
                }
            }

        }

        public int getDuration()
        {
            // Log.d(TAG, "---------->getDuration()");
            synchronized (mPlayerLock)
            {
                if (mIsInitialized && mMediaPlayer != null)
                {
                    return mMediaPlayer.getDuration();
                }
                else
                {
                    return -1;
                }
            }
        }

        public int getCurrentPosition()
        {

            // Log.d(TAG, "---------->getCurrentPosition()");
            synchronized (mPlayerLock)
            {
                if (mIsInitialized && mMediaPlayer != null)
                {
                    return mMediaPlayer.getCurrentPosition();
                }
                else
                {
                    return -1;
                }
            }
        }

        public void seekTo()
        {
            Log.d(TAG, "---------->seekTo(): " + mSeekTarget);
            synchronized (mPlayerLock)
            {
                if (mSeekTarget != 0 && mMediaPlayer != null && mIsInitialized)
                {
                    mMediaPlayer.seekTo(mSeekTarget);
                }                 
            }            
        }

        private OnPreparedListener preparedListener = new OnPreparedListener()
        {
            @Override
            public void onPrepared(IMediaPlayerAdapter mp)
            {                
//                setVolumeControlStream(AudioManager.STREAM_MUSIC);
//                int ret = mAudioManager.requestAudioFocus(mAudioFocusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
                Log.d(TAG, "requestAudioFocus ret:" + AudioManager.AUDIOFOCUS_REQUEST_GRANTED
                        + " 1 is AudioManager.AUDIOFOCUS_REQUEST_GRANTED else failed");
                
                synchronized (mPlayerLock)
                {
                    mIsInitialized = true;
                }
                seekTo();
                mOnPreparedListener.onPrepared(mp);
            }
        };
    }

    /**
     * 音乐播放完成监听器
     */
    private OnCompleteListener mOnCompletionListener = new OnCompleteListener()
    {

        @Override
        public void onCompletion(IMediaPlayerAdapter mp)
        {

            Log.d(TAG, "--------->MediaPlayer.OnCompletionListener:  onCompletion()");            
            removeLogicalMessage(AudioPlayerMsg.MSG_SYNC_POSTION);

            if (mRemoteCallback != null)
            {
                mRemoteCallback.onCompletion(mp);
            }
            sendLogicalMessage(AudioPlayerMsg.MSG_PROC_COMPLETED, 0);
        }
    };

    /**
     * 音乐播放出错监听器
     */
    private OnErrorListener mOnErrorListener = new OnErrorListener()
    {

        @Override
        public boolean onError(IMediaPlayerAdapter mp, int what, int extra)
        {
            Log.d(TAG, "--------->MediaPlayer.OnErrorListener:  onError(): what=" + what + ", extra=" + extra);            
            switch (what)
            {
                case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                    Log.d(TAG, "---------->media server died..........");
                    if (mMusicPlayer != null)
                    {
                        mMusicPlayer.resetPlayer(); 
                    }
                    return true;
                    
                case HiMediaPlayer.MEDIA_INFO_NETWORK:
                case HiMediaPlayer.MEDIA_INFO_NOT_SUPPORT:
                    Log.d(TAG, "----------network erro..........");
                    return true;                
                default:
                    Log.d(TAG, "Unkown Error: " + what + "," + extra);
                    break;
            }

            removeLogicalMessage(AudioPlayerMsg.MSG_PROC_ERROR);
            sendLogicalMessage(AudioPlayerMsg.MSG_PROC_ERROR, 0);

            if (mRemoteCallback != null)
            {
                mRemoteCallback.onError(mp, what, extra);
            }

            return true;
        }
    };

    /**
     * 网络错误码，供mOnInfoListener使用 copy from bigfish/hidolphin/component/player/include/hi_svr_format.h
     * @author w00227386
     */
    public enum HI_FORMAT_MSG_NETWORK_E
    {
        HI_FORMAT_MSG_NETWORK_ERROR_UNKNOW, HI_FORMAT_MSG_NETWORK_ERROR_CONNECT_FAILED, HI_FORMAT_MSG_NETWORK_ERROR_TIMEOUT, HI_FORMAT_MSG_NETWORK_ERROR_DISCONNECT, HI_FORMAT_MSG_NETWORK_ERROR_NOT_FOUND, HI_FORMAT_MSG_NETWORK_NORMAL, HI_FORMAT_MSG_NETWORK_ERROR_BUTT,
    }

    /**
     * modify by wanghuanlai 音乐播放信息码
     */
    private OnInfoListener mOnInfoListener = new OnInfoListener()
    {
        @Override
        public boolean onInfo(IMediaPlayerAdapter mp, int what, int extra)
        {
            if (what == HiMediaPlayer.MEDIA_INFO_NETWORK && extra != HI_FORMAT_MSG_NETWORK_E.HI_FORMAT_MSG_NETWORK_NORMAL.ordinal())
            {
                Log.d(TAG, "----->process network disconnected!!!");
                return mOnErrorListener.onError(mp, what, extra);
            }
            return false;
        }
    };


    /**
     * 音乐准备完成监听器
     */
    private OnPreparedListener mOnPreparedListener = new OnPreparedListener()
    {

        @Override
        public void onPrepared(IMediaPlayerAdapter mp)
        {
            mEndPrepareTime = System.currentTimeMillis();
            Log.d(TAG, "---------->onPrepared()");            
            if (mEndPrepareTime - mBeginPrepareTime > 1000)
            {
                sendLogicalMessage(AudioPlayerMsg.MSG_CONTROL_PLAY, 0);
            }
            else
            {
                sendLogicalMessage(AudioPlayerMsg.MSG_CONTROL_PLAY, (int) (1000 - (mEndPrepareTime - mBeginPrepareTime)));
            }
            if (mRemoteCallback != null)
            {
                mRemoteCallback.onPrepared(mp);
            }            
        }
    };

    public void removeLogicalMessage(int what)
    {
        // Log.d(TAG, "---------->removeLogicalMessage(),what " + what);
        synchronized (mHandlerLock)
        {
            if (mLogicalHandler != null)
            {
                mLogicalHandler.removeMessages(what);
            }
        }
    }
    
    public final void sendLogicalMessage(Message msg, int delayMillis)
    {
        if (msg != null)
        {
            synchronized (mHandlerLock)
            {
                if (mLogicalHandler != null)
                {
                    mLogicalHandler.sendMessageDelayed(msg, delayMillis);
                }
            }
        }
    }
    
    public final void sendLogicalMessage(int what, int delayMillis)
    {

        // Log.d(TAG, "---------->sendLogicalMessage(),what " + what);
        synchronized (mHandlerLock)
        {
            if (mLogicalHandler != null)
            {
                mLogicalHandler.sendEmptyMessageDelayed(what, delayMillis);
            }
        }

    }
    
    public final Message obtainLogicalMessage(int what, int arg1, int arg2, Object obj)
    {
        // Log.d(TAG, "---------->obtainLogicalMessage(),what " + what);
        synchronized (mHandlerLock)
        {
            if (mLogicalHandler != null)
            {
                return Message.obtain(mLogicalHandler, what, arg1, arg2, obj);
            }
            else
            {
                return null;
            }
        }
    }
    
    private Handler.Callback mLogicalHandlerCallback = new Handler.Callback()
    {

        @Override
        public boolean handleMessage(Message msg)
        {

            switch (msg.what)
            {
                case AudioPlayerMsg.MSG_REQUEST_PLAY:
                {
                    Log.d(TAG, "--------proc message AudioPlayerMsg.MSG_REQUEST_PLAY");
                    if (msg.obj != null)
                    {
                        mCurrentMediaInfo = (LocalMediaInfo) msg.obj;                         
                        
                        String uri = mCurrentMediaInfo.getUrl();
                        if (uri != null)
                        {                            
                            mBeginPrepareTime = System.currentTimeMillis();
                            if (mMusicPlayer != null)
                            {
                                mMusicPlayer.setMeidaInfo(mCurrentMediaInfo);
                            }
                            openFile(uri);                            
                        }
                    }
                    else
                    {                        
                        mCurrentMediaInfo = null;
                        stop();                        
                    }

                    removeLogicalMessage(AudioPlayerMsg.MSG_SYNC_POSTION);
                    sendLogicalMessage(AudioPlayerMsg.MSG_SYNC_POSTION, 0);

                    break;
                }
                case AudioPlayerMsg.MSG_CONTROL_PLAY:
                    Log.d(TAG, "--------proc message AudioPlayerMsg.MSG_CONTROL_PLAY");
                    if (mMusicPlayer != null && mMusicPlayer.isInitialized())
                    {                        
                        play();
                    }
                    break;
                case AudioPlayerMsg.MSG_CONTROL_PAUSE:
                    Log.d(TAG, "--------proc message AudioPlayerMsg.MSG_CONTROL_PAUSE");
                    if (mMusicPlayer != null && mMusicPlayer.isInitialized())
                    {                        
                        pause();
                    }
                    break;

                case AudioPlayerMsg.MSG_PROC_ERROR:
                {
                    Log.d(TAG, "--------proc message AudioPlayerMsg.MSG_PROC_ERROR");
                    destroy();
                    break;
                }
                case AudioPlayerMsg.MSG_PROC_COMPLETED:
                {                    
                    Log.d(TAG, "--------proc message AudioPlayerMsg.MSG_PROC_COMPLETED");                    
                    break;
                }
                case AudioPlayerMsg.MSG_SYNC_POSTION:  
                    if (mMusicPlayer == null)
                    {
                        break;
                    }
                    int pos = mMusicPlayer.getCurrentPosition();                    
                    removeLogicalMessage(AudioPlayerMsg.MSG_SYNC_POSTION);
                    sendLogicalMessage(AudioPlayerMsg.MSG_SYNC_POSTION, 300);

                    if (mRemoteCallback != null)
                    {
                        mRemoteCallback.onProgress(pos);
                    }
                    break;
                default:
                    Log.d(TAG, "---------->proc message default");

                    return false;
            }

            return true;
        }
    };
    
//    private OnAudioFocusChangeListener mAudioFocusListener = new OnAudioFocusChangeListener()
//    {
//        public void onAudioFocusChange(int focusChange)
//        {
//            if (mMusicPlayer != null)
//            {
//
//                mAudioManager.abandonAudioFocus(this);
//                return;
//            }
//            switch (focusChange)
//            {
//                case AudioManager.AUDIOFOCUS_LOSS:
//                    mPausedByTransientLossOfFocus = false;
//                    if (null != mMusicPlayer)
//                    {
//                        mMusicPlayer.pause();
//                    }
//                    break;
//                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
//                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
//                    mPausedByTransientLossOfFocus = true;
//                    if (null != mMusicPlayer)
//                    {
//                        mMusicPlayer.pause();
//                    }
//                    break;
//                case AudioManager.AUDIOFOCUS_GAIN:
//                    if (mPausedByTransientLossOfFocus)
//                    {
//                        mPausedByTransientLossOfFocus = false;
//                        if (null != mMusicPlayer)
//                        {
//                            mMusicPlayer.start();
//                        }
//                    }
//                    break;
//            }
//            // updatePlayPause();
//        }
//    };
    
    public void openFile(String filepath)
    {        
        Log.d(TAG, "-------->openFile:" + filepath);
        if (mMusicPlayer != null)
        {
            mMusicPlayer.setDataSourceAsync(filepath);
        }
    }
    
    private void destroy()
    {
        removeLogicalMessage(AudioPlayerMsg.MSG_SYNC_POSTION);
        mRemoteCallback = null;
        unbind();
        stop();
        stopSelf();
        synchronized (mHandlerLock)
        {

            if (mLogicalHandler != null)
            {
                mLogicalHandler.getLooper().quit();

                mLogicalHandlerCallback = null;
                mLogicalHandler = null;
                mLogicalThread = null;
            }
        }
    }

}
