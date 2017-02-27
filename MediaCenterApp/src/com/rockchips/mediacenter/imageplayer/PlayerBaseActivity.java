/**
 * 
 * com.rockchips.iptv.stb.dlna.player
 * PlayerBaseActivity.java
 * 
 * 2011-11-1-下午04:15:14
 * Copyright 2011 Huawei Technologies Co., Ltd
 * 
 */
package com.rockchips.mediacenter.imageplayer;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.rockchips.mediacenter.bean.LocalMediaInfo;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.utils.IICLOG;
import com.rockchips.mediacenter.utils.StringUtils;
import com.rockchips.mediacenter.bean.PlayStateInfo;
import com.rockchips.mediacenter.activity.DeviceActivity;
import com.rockchips.mediacenter.audioplayer.AudioPlayerActivity;
import com.rockchips.mediacenter.audioplayer.DBUtils;
import com.rockchips.mediacenter.playerclient.MediaCenterPlayerClient;
import com.rockchips.mediacenter.videoplayer.VideoPlayerActivity;
import com.rockchips.mediacenter.view.PopMenu.VolumeKeyListener;

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
    private static final String TAG = "MediaCenterApp";
    
    private IICLOG Log = IICLOG.getInstance();
    
    /*
     * 标示播放的是否为来自Media Center Service的数据
     */
    public boolean bMCSMode = false;
    
    /**
     * save the player list & state
     */
    protected PlayStateInfo mPlayStateInfo = null;
    
    private PlayerCallbackMessenger mPlayerCallbackMessenger = null;
    
    // MCS Client端
    protected MediaCenterPlayerClient mMediaCenterPlayerClient = null;
    
    //提示显示时间
    protected int TOAST_SHOW_TIME = 0;
    
    //当前使用url
    protected String mStrCurrentUrl = null;
    
    private HandlerThread mMCSHandlerThread = null;
    
    protected static final int MCS_RECEIVE_MSG_MODE_REFUSE = 0;
    
    protected static final int MCS_RECEIVE_MSG_MODE_PROCESS = 1;
    
    //是否接受消息
    private int mbReceiveMsg = MCS_RECEIVE_MSG_MODE_REFUSE;
    
    private LocalMediaInfo mMediaBaseInfo = null;
    
    // 视频播放器
    private static final String STR_VIDEO_CLASS_NAME = "com.rockchips.mediacenter.videoplayer.VideoPlayerActivity";

    // 音乐播放器
    private static final String STR_AUDIO_CLASS_NAME = "com.rockchips.mediacenter.audioplayer.AudioPlayerActivity";

    // 图片播放器
    private static final String STR_IMAGE_CLASS_NAME = "com.rockchips.mediacenter.imageplayer.ImagePlayerActivity";
    
    // Home键退到Launcher界面
    private static final String STR_LAUNCHER_CLASS_NAME = "com.android.launcher2.Launcher";
    private static final String STR_REAL6_LAUNCHER_CLASS_NAME = "com.rockchips.launcher.Home2";
    
    // 音量管理器，用于调节系统音量
    protected AudioManager audioManager = null;
    
    //是否是内置播放器标志位  是--true 否--false
    protected boolean mbInternalPlayer = false;
        
    /**
     * 标志Sender端的标示
     */
    private String mSenderClientUniq = null;
        
    protected static List<Bundle> mStMediaInfoList;
    protected static int mStIndex;
    
    private Handler connectListenerHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case ConstData.ServiceConnectionMSG.MSG_SERVICE_DISCONNECTED:
                    finish();
                    break;
                
                default:
                    break;
            }
        };
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreate - IN");
        super.onCreate(savedInstanceState);
        mPlayStateInfo = new PlayStateInfo();        
        Log.d(TAG, "mPlayStateInfo = c"+mPlayStateInfo);
        
        //全屏
   /*     if (Integer.parseInt(Build.VERSION.SDK) < 14)
        {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        else
        {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
            getWindow().getDecorView().setSystemUiVisibility(View.INVISIBLE);
        }*/
        
        // 加载资源
        loadResource();
        
        // 初始化音量管理器
        if (audioManager == null)
        {
            Log.d(TAG, "audioManager == null, create a AudioManager object");
            audioManager = (AudioManager)this.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        }
        
       
        
        // 解析Intent参数，并保存在mPlayStateInfo中
        parseInputIntent(getIntent());
        
        // 获取当前播放的媒体信息，媒体与索引有关。add by zwx160481 zouzhiyi  添加空指针判断
        if(mPlayStateInfo !=null 
        		&& mPlayStateInfo.getMediaList()!=null 
        		&& mPlayStateInfo.getMediaList().size()>0
        		&& mPlayStateInfo.getCurrentIndex()>=0
        		&& mPlayStateInfo.getCurrentIndex()<mPlayStateInfo.getMediaList().size()){
        	
            Log.d(TAG, "mPlayStateInfo.getCurrentIndex()=="+mPlayStateInfo.getCurrentIndex());
        	mMediaBaseInfo = mPlayStateInfo.getMediaList().get(mPlayStateInfo.getCurrentIndex());
        }
        // 如果获取出来的信息是空的就直接finish掉
        if (mMediaBaseInfo == null)
        {
            Log.e(TAG, "Current Media Info is null");
            finish();
            return;
        }
        
        if (bMCSMode) 
        {
            dobind();
        }
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
        
        if (bMCSMode)
        {
            if (mMediaCenterPlayerClient != null && mMediaCenterPlayerClient.isConnected())
            {
                Log.d(TAG,
                    "mMediaCenterPlayerClient != null && mMediaCenterPlayerClient.isConnected(), request list from Sender");
                
                mMediaCenterPlayerClient.requestList();
            }
        }
        
//        // 当播放器启动起来的时候，回发当前机顶盒的音量给推送或甩屏端
//        if (audioManager == null)
//        {
//            mLog.d(TAG, "audioManager is null, new a AudioManager object");
//            audioManager =
//                (AudioManager)PlayerBaseActivity.this.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
//        }
//        
//        int stbCurrVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
//        int stbMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
//        float volumePercent = (float)stbCurrVolume / (float)stbMaxVolume;
//        mLog.d(TAG, "stbCurrVolume:" + stbCurrVolume);
//        mLog.d(TAG, "stbMaxVolume:" + stbMaxVolume);
//        mLog.d(TAG, "volumePercent:" + volumePercent);
//        
//        if (mMediaCenterPlayerClient != null)
//        {
//            mLog.d(TAG, "mMediaCenterPlayerClient != null, send volumePercent to Sender");
//            
//            mMediaCenterPlayerClient.adjustVolume(ConstData.VolumeAdjustType.ADJUST_SET, volumePercent);
//        }
        
        super.onResume();
    }
    
    @Override
    protected void onPause()
    {
        Log.d(TAG, "onPause - IN");
        
        //如果是外置播放器，则finish
//        if(!mbInternalPlayer){
//        	Log.d(TAG, "onPause()--->Finish myself!");
//        	stop();
//            unbind();
//        	finish();
//        }
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
            unbind();            
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
        
        if (bMCSMode)
        {
            unbind();
        }
        
        getPlayStateInfo().setStop(true);
        mPlayStateInfo.recycle();
        mPlayStateInfo = null;
        
        System.gc();
        
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
        
        //获取当前播放索引
        int playIndex = intent.getIntExtra(ConstData.IntentKey.CURRENT_INDEX, 0);
        
        //获取客户端唯一标识
        String senderClientUniq = intent.getStringExtra(ConstData.IntentKey.UNIQ);
        ArrayList<Bundle> mediaBaseList = intent.getParcelableArrayListExtra(ConstData.IntentKey.MEDIA_INFO_LIST);
        
        if (StringUtils.isNotEmpty(senderClientUniq))
        {
            setSenderClientUniq(senderClientUniq);
        }
        else
        {
            setSenderClientUniq(ConstData.ClientTypeUniq.UNKNOWN_UNIQ);
        }
        
        mbInternalPlayer = intent.getBooleanExtra(ConstData.IntentKey.IS_INTERNAL_PLAYER, false);
		Log.d(TAG, "isInternalPlayer is : " + mbInternalPlayer);
		
		//如果是媒体中心内置播放器，则不启动服务
		if (mbInternalPlayer) 
		{
			bMCSMode = false;
			//设置当前播放
			if (null == mediaBaseList)
			{
			    mPlayStateInfo.setMediaList(mStMediaInfoList);
		        mPlayStateInfo.setCurrentIndex(mStIndex);
			}
			else
			{
			    mPlayStateInfo.setMediaList(mediaBaseList);
			    mPlayStateInfo.setCurrentIndex(playIndex);
			}
	        	        
	        mPlayStateInfo.setSenderClientUniq(senderClientUniq);
		}
		else 
		{
	        // 解析来自MCS的参数
	        
	      
	        // 判断参数是否有效
	        if (mediaBaseList != null)
	        {
	            Log.i(TAG, "MCS DATA : mediaBaseList.size:" + mediaBaseList.size());
	            
	            if (mediaBaseList.size() > 0)
	            {
	                bMCSMode = true;
	            }
	            else
	            {
	                finish();
	                return;
	            }
	        }
	        else
	        {
	            // 搜索出来的文件进行播放            
	            Log.i(TAG, "data: " + intent.getData());
	            
	            String mediaInfo = intent.getStringExtra("MediaInfo");
	            String mediaName = intent.getStringExtra("MediaName");
	            
	            Log.i(TAG, "parseInputIntent ---> mediaInfo: " + mediaInfo);
	            Log.i(TAG, "parseInputIntent ---> mediaName: " + mediaName);
	            
	            if (StringUtils.isNotEmpty(mediaInfo) && StringUtils.isNotEmpty(mediaName))
	            {
	                // 都不为空，认为是搜索出来的
	                String artist = intent.getStringExtra("Artist");
	                String title = intent.getStringExtra("Title");
	                String album = intent.getStringExtra("Album");
	                int deviceType = intent.getIntExtra("DeviceType", ConstData.DeviceType.DEVICE_TYPE_UNKNOWN);
	                Log.d(TAG, "parseInputIntent ---> artist:" + artist);
	                Log.d(TAG, "parseInputIntent ---> title:" + title);
	                Log.d(TAG, "parseInputIntent ---> deviceType:" + deviceType);
	                Log.d(TAG, "parseInputIntent ---> album:" + album);
	                
	                LocalMediaInfo mbi = new LocalMediaInfo();
	                mbi.setmData(mediaInfo);
	                mbi.setmPhysicId("ANDROID_SYSTEM");
	                
	                // 设置设备类型
	                mbi.setmDeviceType(deviceType);
	                
	                // 设置艺术家、标题和唱片集，便于音乐播放器搜索歌词
	                mbi.setmArtist(artist);
	                mbi.setmTitle(title);
	                mbi.setmAlbum(album);
	                if (mediaName != null && mediaName.contains("."))
	                {
	                    // 如果搜索传递过来的有文件后缀名，去掉后缀名
	                    Log.d(TAG, "parseInputIntent ---> Before delete the file suffix, mediaName:" + mediaName);
	                    mediaName = StringUtils.getFileName(mediaName);
	                    Log.d(TAG, "parseInputIntent ---> After delete the file suffix, mediaName:" + mediaName);
	                }
	                
	                mbi.setmFileName(mediaName);
	                mediaBaseList = new ArrayList<Bundle>();
	                
	                Bundle bundle = mbi.compress();
	                mediaBaseList.add(bundle);
	                
	                Log.i(TAG, "parseInputIntent ---> MediaFileInfo: " + mbi.toString());
	            }
	            else
	            {
	                Uri httpUri = intent.getData();
	                
	                if (httpUri == null)
	                {
	                    //输入的数据无效
	                    Log.i(TAG, "parseInputIntent: MCS DATA and HTTP URI are invalidate");
	                    
	                    finish();
	                    return;
	                }
	                
	                String struri = httpUri.toString();
	                
	                LocalMediaInfo mbi = new LocalMediaInfo();
	                mbi.setmData(struri);
	                mbi.setmPhysicId("ANDROID_SYSTEM");
	                
	                mbi.setmDeviceType(getDeviceType(struri));
	                
	                // 从uri中获取文件名
	                String strDisplayName = StringUtils.getFileName(struri);
	                
	                // 如果无法获取就用url表示
	                if (strDisplayName == null)
	                {
	                    strDisplayName = struri;
	                }
	                
	                mbi.setmFileName(strDisplayName);
	                
	                mbi.setmFileType(getMediaType());
	                
	                mediaBaseList = new ArrayList<Bundle>();
	                
	                Bundle bundle = mbi.compress();
	                mediaBaseList.add(bundle);
	                
	                Log.i(TAG, mbi.toString());
	            }
	            
	            bMCSMode = false;
	        }
	        
//	        // 保存输入参数
//	        if (mPlayStateInfo != null)
//	        {
//	            mPlayStateInfo.setStop(true);
//	            mPlayStateInfo = null;
//	        }
	        
	//        mPlayStateInfo = new PlayStateInfo();
	        
	        Log.d(TAG, "mPlayStateInfo = "+mPlayStateInfo);
	        //设置当前播放
	        mPlayStateInfo.setCurrentIndex(playIndex);
	        mPlayStateInfo.setSenderClientUniq(senderClientUniq);
	        mPlayStateInfo.setMediaList(mediaBaseList);
	        mediaBaseList = null;
		}
        
        // DTS2012030204438
        if (mMediaCenterPlayerClient != null)
        {
            Log.d(TAG, "Reset the sender client in the player client");
            Log.d(TAG, "Reset to:" + mPlayStateInfo.getSenderClientUniq());
            mMediaCenterPlayerClient.setSenderUniq(mPlayStateInfo.getSenderClientUniq());
        }
        
        Log.i(TAG, "parseInputIntent:playIndex " + playIndex);
    }
    
    /**
     * 
     * getDeviceType
     * 通过url判断是网络的还是本地的视频
     * @param str
     * @return 
     *int
     * @exception
     */
    private int getDeviceType(String str)
    {
        if (str.startsWith("http") || str.startsWith("rtsp"))
        {
            return ConstData.DeviceType.DEVICE_TYPE_DMS;
        }
        else
        {
            return ConstData.DeviceType.DEVICE_TYPE_U;
        }
    }
    
    protected int insertList(List<Bundle> list)
    {
        mPlayStateInfo.insertList(list);
        
        return mPlayStateInfo.getMediaList().size();
    }
    
    protected int deleteList(String devId)
    {
        
        LocalMediaInfo mbi = getCurrentMediaInfo();
        
        mPlayStateInfo.deleteList(devId);
        
        if (mbi != null)
        {
            if (mbi.getmPhysicId().equals(devId))
            {
                onDelecteDeviceId(devId);
            }
        }
        
        return mPlayStateInfo.getMediaList().size();
    }
    
    /**
     * 
     * dobind：建立与MediaCenterService之间的绑定，并注册回调
     *  
     * void
     * @exception
     */
    private void dobind()
    {
        Log.i(TAG, "dobind - IN ");
        if (!bMCSMode)
        {
            Log.d(TAG, " bMCSMode is not MCS ");
            return;
        }
        
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
            mMediaCenterPlayerClient.setPlayerType(getUUID());
            
            if (mPlayStateInfo != null && mPlayStateInfo.getSenderClientUniq() != null)
            {
                Log.d(TAG, mPlayStateInfo.getSenderClientUniq());
                
                mMediaCenterPlayerClient.setSenderUniq(mPlayStateInfo.getSenderClientUniq());
            }
            mMediaCenterPlayerClient.registerPlayerCallBack(mPlayerCallbackMessenger);
            mMediaCenterPlayerClient.doBindService(this.getApplicationContext());
            mMediaCenterPlayerClient.setListener(connectListenerHandler);
        }
        Log.d(TAG, " end with connect MCS service ");
    }
    
    /**
     * getUUID
     * 
     * @return 
     *int
     * @exception 
    */
    protected abstract int getUUID();
    
    /**
     * 
     * unbind：解除与MediaCenterService之间的绑定，并取消注册的回调
     *  
     * void
     * @exception
     */
    protected void unbind()
    {
        // 对解绑定的过程加把锁
        synchronized (this)
        {
            Log.i(TAG, " unbind() - IN ");
            
            if (!bMCSMode)
            {
                Log.d(TAG, "Not MCSMode, no need to unbind!");
                return;
            }
            
            Log.d(TAG, " start to disconnect MCS service ");
            
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
            }
            
            try
            {
                if (mMCSHandlerThread != null)
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
            
            Intent intent = (Intent)msg.obj;
            
            switch (msg.what)
            {
                case ConstData.MCSMessage.MSG_SET_MEDIA_DATA:
                    Log.d(TAG, "MSG_SET_MEDIA_DATA start");
                    
                    mcsSetMediaData(intent);
                    
                    Log.d(TAG, "MSG_SET_MEDIA_DATA end");
                    
                    break;
                case ConstData.MCSMessage.MSG_PLAY:
                    Log.d(TAG, "MSG_PLAY start");
                    
                    mcsPlay(intent);
                    
                    Log.d(TAG, "MSG_PLAY end");
                    
                    break;
                case ConstData.MCSMessage.MSG_PAUSE:
                    Log.d(TAG, "MSG_PAUSE start");
                    
                    mcsPause(intent);
                    
                    Log.d(TAG, "MSG_PAUSE end");
                    
                    break;
                case ConstData.MCSMessage.MSG_SEEK:
                    Log.d(TAG, "MSG_SEEK start");
                    
                    mcsSeek(intent);
                    
                    Log.d(TAG, "MSG_SEEK end");
                    
                    break;
                case ConstData.MCSMessage.MSG_STOP:
                    Log.d(TAG, "MSG_STOP start");
                    
                    // 收到stop信令就进行解绑操作，避免由于在onDestroy()中解绑太慢，导致MCS中刚注册上的播放器回调被正销毁的播放器反注册掉
                    // 执行拉回时，stop状态已经在MCS中返回给Sender端
                    unbind();
                    mcsStop(intent);
                    
                    Log.d(TAG, "MSG_STOP end");
                    
                    break;
                case ConstData.MCSMessage.MSG_APPEND_MEDIA_DATA:
                    Log.d(TAG, "MSG_APPEND_MEDIA_DATA start");
                    
                    ArrayList<Bundle> mediaBaseList =
                        intent.getParcelableArrayListExtra(ConstData.IntentKey.MEDIA_INFO_LIST);
                    
                    if (mPlayStateInfo != null)
                    {
                        int sumAppend = mPlayStateInfo.insertList(mediaBaseList);
                        Log.d(TAG, "MSG_APPEND_MEDIA_DATA --->sumAppend:" + sumAppend);
                        
                        if(sumAppend > 0)
                        {
                            // DTS2012022505854:当mPlayStateInfo.insertList()增加的媒体文件个数大于0时，同步一下
                            // DLNAService遇到DMS中的文件变更时，将数据库中该DMS上的所有数据删除，同时发送设备下线消息，然后重新将该DMS上的数据添加到数据库同时发送Browner消息
                            mcsAppendList(intent);
                        }
                    }
                    
                    Log.d(TAG, "MSG_APPEND_MEDIA_DATA end");
                    
                    break;
                case ConstData.MCSMessage.MSG_DEVICE_DOWN:
                    Log.d(TAG, "MSG_DEVICE_DOWN start");
                    
                    String deviceId = intent.getStringExtra(ConstData.IntentKey.DEVICE_ID);
                    Log.d(TAG, "deviceId:" + deviceId);
                    
                    if (null == deviceId || deviceId.trim().equals(""))
                    {
                        Log.e(TAG, "The deviceId transfered from the DMS client is null");
                        break;
                    }
                    
                    String realDeviceId = DBUtils.getDeviceId(deviceId);
                    
                    if (mPlayStateInfo != null)
                    {
                        if (realDeviceId == null)
                        {
                            Log.d(TAG, "realDeviceId == null");
                            Log.d(TAG, "deviceId:" + deviceId);
                        }
                        else
                        {
                            Log.d(TAG, "realDeviceId:" + realDeviceId);
                        }
                        
                        mcsDelDevice(intent);
                    }
                    
                    Log.d(TAG, "MSG_DEVICE_DOWN end");
                    
                    break;
                case ConstData.MCSMessage.MSG_ADJUST_VOLUME:
                    Log.d(TAG, "MSG_ADJUST_VOLUME start");
                    
                    int volumeAdjustType =
                        intent.getIntExtra(ConstData.IntentKey.VOLUME_ADJUST_TYPE,
                            ConstData.VolumeAdjustType.ADJUST_UNKNOWND);
                    Log.d(TAG, "volumeAdjustType:" + volumeAdjustType);
                    
                    if (volumeAdjustType != ConstData.VolumeAdjustType.ADJUST_UNKNOWND)
                    {
                        if (audioManager == null)
                        {
                            Log.d(TAG, "audioManager is null, new a AudioManager object");
                            audioManager =
                                (AudioManager)PlayerBaseActivity.this.getApplicationContext()
                                    .getSystemService(Context.AUDIO_SERVICE);
                        }
                        
                        if (volumeAdjustType == ConstData.VolumeAdjustType.ADJUST_LOWER)
                        {
                            Log.d(TAG, "Adjust the volume to lower");
                            
                            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                                AudioManager.ADJUST_LOWER,
                                AudioManager.FX_FOCUS_NAVIGATION_UP);
                            
                            // 将设置好的音量值返回给Sender端
                            if (mMediaCenterPlayerClient != null)
                            {
                                Log.d(TAG, "Send adjust volume lower to Sender client");
                                mMediaCenterPlayerClient.adjustVolume(ConstData.VolumeAdjustType.ADJUST_LOWER, -1);
                            }
                            
                            return;
                        }
                        else if (volumeAdjustType == ConstData.VolumeAdjustType.ADJUST_SAME)
                        {
                            Log.d(TAG, "Not Adjust the volume");
                            
                            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                                AudioManager.ADJUST_SAME,
                                AudioManager.FX_FOCUS_NAVIGATION_UP);
                            
                            // 将设置好的音量值返回给Sender端
                            if (mMediaCenterPlayerClient != null)
                            {
                                Log.d(TAG, "Send adjust volume same to Sender client");
                                mMediaCenterPlayerClient.adjustVolume(ConstData.VolumeAdjustType.ADJUST_SAME, -1);
                            }
                            
                            return;
                        }
                        else if (volumeAdjustType == ConstData.VolumeAdjustType.ADJUST_RAISE)
                        {
                            Log.d(TAG, "Adjust the volume to raise");
                            
                            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                                AudioManager.ADJUST_RAISE,
                                AudioManager.FX_FOCUS_NAVIGATION_UP);
                            
                            // 将设置好的音量值返回给Sender端
                            if (mMediaCenterPlayerClient != null)
                            {
                                Log.d(TAG, "Send adjust volume raise to Sender client");
                                mMediaCenterPlayerClient.adjustVolume(ConstData.VolumeAdjustType.ADJUST_RAISE, -1);
                            }
                            
                            return;
                        }
                        else if (volumeAdjustType == ConstData.VolumeAdjustType.ADJUST_MUTE_ON)
                        {
                            Log.d(TAG, "Turn on the mute mode");
                            
                            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
                            
                            //add by xWX184171 PC推送禁音无显示
                            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                                AudioManager.ADJUST_RAISE,
                                AudioManager.FX_FOCUS_NAVIGATION_UP);
                            
                            // 将设置好的音量值返回给Sender端
                            if (mMediaCenterPlayerClient != null)
                            {
                                Log.d(TAG, "Send mute on to Sender client");
                                mMediaCenterPlayerClient.adjustVolume(ConstData.VolumeAdjustType.ADJUST_MUTE_ON, -1);
                            }
                            
                            return;
                        }
                        else if (volumeAdjustType == ConstData.VolumeAdjustType.ADJUST_MUTE_OFF)
                        {
                            Log.d(TAG, "Close the mute mode");
                            
                            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
                            
                          //add by xWX184171 PC推送禁音无显示
                            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                                AudioManager.ADJUST_RAISE,
                                AudioManager.FX_FOCUS_NAVIGATION_UP);
                            
                            // 将设置好的音量值返回给Sender端                           
                            if (mMediaCenterPlayerClient != null)
                            {
                                Log.d(TAG, "Send mute off to Sender client");
                                mMediaCenterPlayerClient.adjustVolume(ConstData.VolumeAdjustType.ADJUST_MUTE_OFF, -1);
                            }
                            
                            return;
                        }
                        else if (volumeAdjustType == ConstData.VolumeAdjustType.ADJUST_SET)
                        {
                            Log.d(TAG, "Set the volume to a fixed value");
                            
                            float volumePercent = intent.getFloatExtra(ConstData.IntentKey.VOLUME_SET_VALUE, -1);
                            Log.d(TAG, "volumePercent:" + volumePercent);
                            
                            if (volumePercent < 0 || volumePercent > 1)
                            {
                                Log.d(TAG, "The value of volumeValue not in [0, 1]");
                                return;
                            }
                            
                            int volumeValueIndex =
                                (int)(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * volumePercent);
                            Log.d(TAG, "volumeValueIndex:" + volumeValueIndex);
                            
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                                volumeValueIndex,
                                AudioManager.FX_FOCUS_NAVIGATION_UP);
                            
                            // 将设置好的音量值返回给Sender端
                            if (mMediaCenterPlayerClient != null)
                            {
                                Log.d(TAG, "Send the volume percent to Sender client");
                                mMediaCenterPlayerClient.adjustVolume(ConstData.VolumeAdjustType.ADJUST_SET,
                                        volumePercent);
                            }
                            return;
                        }
                    }
                    
                    Log.d(TAG, "MSG_ADJUST_VOLUME end");
                    
                    break;
                default:
                    Log.d(TAG, "UNKOWN MSG FROM MCS: " + msg.what + "  -- start");
                    
                    mcsDefaultMSGProcess(intent);
                    
                    Log.d(TAG, "UNKOWN MSG FROM MCS: " + msg.what + "  -- end");
                    
                    break;
            }
            super.handleMessage(msg);
        }
    }
    
    protected void sendMessage(Message msg)
    {
        if (null != mPlayerCallbackMessenger)
        {
            mPlayerCallbackMessenger.sendMessage(msg);
        }
        else
        {
            Log.e(TAG, "callback of msg is null, send msg:" + msg + " failed!");
        }
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
    
    protected LocalMediaInfo getCurrentMediaInfo()
    {
        if (mPlayStateInfo == null)
        {
            Log.e(TAG, "mPlayStateInfo null");
            return null;
        }
        
        List<LocalMediaInfo> fileInfos = mPlayStateInfo.getMediaList();
        if(fileInfos==null)
        {
        	return null;
        }
        int size = fileInfos.size();
        int currentIndex = mPlayStateInfo.getCurrentIndex();
        if(currentIndex<0 || currentIndex>=size){
        	return null;
        }
        LocalMediaInfo mbi = mPlayStateInfo.getMediaList().get(mPlayStateInfo.getCurrentIndex());
        if (mbi == null)
        {
            Log.e(TAG, "getCurrentMediaInfo null");
            //            mStrCurrentUrl = null;
            return null;
        }
        if(mbi!=null && mbi.getUrl() != null){
        	mStrCurrentUrl = new String(mbi.getUrl());
        }
        Log.d(TAG, "mStrCurrentUrl:" + mStrCurrentUrl);
        
        return mbi;
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
     *MediaFileInfo
     * @exception
     */
    protected LocalMediaInfo getPreMediaInfo()
    {
        if (mPlayStateInfo == null)
        {
            Log.e(TAG, "mPlayStateInfo null");
            return null;
        }
        
        if(mPlayStateInfo.getCurrentIndex() == 0)
        {
        	return null;
        }
        
        LocalMediaInfo mbi = mPlayStateInfo.getPreMediaInfo();
        if (mbi == null)
        {
            Log.e(TAG, "getPreMediaInfo null");
            return null;
        }
        
        mStrCurrentUrl = new String(mbi.getUrl());
        return mbi;
    }
    
    /**
     * 
     * getPreMediaInfo
     * 获取mPlayStateInfo数据列表中下一个数据
     * @return 
     *MediaFileInfo
     * @exception
     */
    protected LocalMediaInfo getNextMediaInfo()
    {
        if (mPlayStateInfo == null)
        {
            Log.e(TAG, "mPlayStateInfo null");
            return null;
        }
        
        int size  = mPlayStateInfo.getMediaList().size();
        
        if(mPlayStateInfo.getCurrentIndex() + 1 >= size)
        {
        	return null;
        }
        
        
        LocalMediaInfo mbi = mPlayStateInfo.getNextMediaInfo();
        
        if (mbi == null)
        {
            Log.e(TAG, "getNextMediaInfo null");
            //            mStrCurrentUrl = null;
            return null;
        }
        
        mStrCurrentUrl = new String(mbi.getUrl());
        
        return mbi;
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
     * 获取播放状态信息
     * @return 
     * PlayStateInfo
     * @exception
     */
    public PlayStateInfo getPlayStateInfo()
    {
        return mPlayStateInfo;
    }
    
    /**
     * mcsDefaultMSGProcess
     * 
     * @param intent 
     * void
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
    
    /**
     * mcsStop
     * 
     * @param intent 
     * void
     * @exception 
    */
    protected abstract void mcsStop(Intent intent);
    
    /**
     * mcsSeek
     * 
     * @param intent 
     * void
     * @exception 
    */
    protected abstract void mcsSeek(Intent intent);
    
    /**
     * mcsPause
     *  
     * void
     * @exception 
    */
    protected abstract void mcsPause(Intent intent);
    
    /**
     * @param intent 
     * mcsPlay
     *  
     * void
     * @exception 
    */
    protected abstract void mcsPlay(Intent intent);
    
    /**
     * mcsSetMediaData
     * 
     * @param intent 
     * void
     * @exception 
    */
    protected abstract void mcsSetMediaData(Intent intent);
    
    /**
     * 
     * getMediaType
     * 获取播放器媒体类型 
     *void
     * @exception
     */
    protected abstract int getMediaType();
    
    /**
     * 当删除了设备ID后，需要判断是否正在播放的源是在该id上，需要做响应的处理
     * onDelecteDeviceId
     * 
     * @return 
     *int
     * @exception
     */
    protected abstract int onDelecteDeviceId(String devId);
    
    /********************************************************
     * abstract function end 
     ********************************************************/
    
    /**
     * @param mSenderClientUniq the mSenderClientUniq to set
     */
    public void setSenderClientUniq(String senderClientUniq)
    {
        this.mSenderClientUniq = senderClientUniq;
    }
    
    /**
     * mSenderClientUniq
     *
     * @return  the mSenderClientUniq
     * @since   1.0.0
    */
    
    public String getSenderClientUniq()
    {
        return mSenderClientUniq;
    }
    
    /**
     * 是否是我是媒体发送过来的请求
     * isSenderMyMedia
     * 
     * @return 
     *boolean
     * @exception
     */
    public boolean isSenderMyMedia()
    {
        if (getSenderClientUniq() == null || getSenderClientUniq().equals(ConstData.ClientTypeUniq.DMS_UNIQ))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    /**
     * 当前播放类型是否为推(包括了甩与DLNA推)过来的
     * isPushType
     * 
     * @return 
     *boolean
     * @exception
     */
    public boolean isPushType()
    {
        if (getSenderClientUniq().equalsIgnoreCase(ConstData.ClientTypeUniq.PUSH_UNIQ))
        {
            return true;
        }
        
        if (getSenderClientUniq().equalsIgnoreCase(ConstData.ClientTypeUniq.SYN_UINQ))
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
        if (!StringUtils.isEmpty(mSenderClientUniq))
        {
            if (mSenderClientUniq.equals(ConstData.ClientTypeUniq.PUSH_UNIQ))
            {
                return true;
            }
        }
        
        return false;
    }
    
    // 是否为甩屏端
    protected boolean isAirPushType()
    {
        if (!StringUtils.isEmpty(mSenderClientUniq))
        {
            if (mSenderClientUniq.equals(ConstData.ClientTypeUniq.SYN_UINQ))
            {
                return true;
            }
        }
        
        return false;
    }
    
    // 是否为点击播放
    protected boolean isMyMediaType()
    {
        if (!StringUtils.isEmpty(mSenderClientUniq))
        {
            if (mSenderClientUniq.equals(ConstData.ClientTypeUniq.DMS_UNIQ))
            {
                return true;
            }
        }
        
        return false;
    }
    
    // 判断该播放器被覆盖时，是否关闭该播放器
    // 场景1：在该播放器播放媒体文件时，第三方软件通过系统再次启动了三个播放器中的一个（如，搜索播放），此时该播放器被后启动的播放器覆盖时应该销毁
    // 场景2：Home键回到Launcher界面时，关闭播放器
    public boolean isFinishSelf()
    {
        Log.d(TAG, "isFinishSelf --In");
        if (!bMCSMode)
        {
            return false;
        }
        
//        try
//        {
//            ActivityManager activityManager = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
//            
//            // 获取当前运行的task的第一个activity
//            ComponentName componentName = activityManager.getRunningTasks(1).get(0).topActivity;
//            
//            // 获取栈顶Activity的类名
//            String componentClassName = componentName.getClassName();
//            
//            Log.d(TAG, "isFinishSelf --> componentClassName:" + componentClassName);
//            
//            if (componentClassName.equalsIgnoreCase(STR_VIDEO_CLASS_NAME))
//            {
//                Log.d(TAG, "The Forgroud Activity Is VideoPlayerActivity Or MusicPlayerActivity Or ImagePlayerActivity");
//                
//                return true;
//            }
//            else if (componentClassName.equalsIgnoreCase(STR_AUDIO_CLASS_NAME))
//            {
//                return true;
//            }
//            else if (componentClassName.equalsIgnoreCase(STR_IMAGE_CLASS_NAME))
//            {
//                return true;
//            }
//            else if (componentClassName.equalsIgnoreCase(STR_LAUNCHER_CLASS_NAME))
//            {
//                return true;
//            }
//            else if (componentClassName.equalsIgnoreCase(STR_REAL6_LAUNCHER_CLASS_NAME))
//            {
//                return true;
//            }
//        }
//        catch (SecurityException e)
//        {
//            Log.w(TAG, "isFinishSelf --> SecurityException:" + e.getLocalizedMessage());
//        }
        
        return true;
    }
    
    // 设置Menu监听，监听音量键的处理，向推送端或甩屏端回传音量值
    protected VolumeKeyListener mVolumeKeyListener = new VolumeKeyListener()
    {
        // 向推送端或甩屏端回传音量值
        public void reportVolumeToSender()
        {
            Log.d(TAG, "VolumeKeyListener --> reportVolumeToSender()");
            if (audioManager == null)
            {
                Log.d(TAG, "audioManager == null, create a AudioManager object");
                audioManager =
                    (AudioManager)PlayerBaseActivity.this.getApplicationContext()
                        .getSystemService(Context.AUDIO_SERVICE);
            }
            
            int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            float volumePercent = (float)currentVolume / (float)maxVolume;
            Log.d(TAG, "currentVolume:" + currentVolume);
            Log.d(TAG, "maxVolume:" + maxVolume);
            Log.d(TAG, "volumePercent:" + volumePercent);
            
            if (mMediaCenterPlayerClient != null)
            {
                Log.d(TAG, "Send the volume percent to Sender client");
                mMediaCenterPlayerClient.adjustVolume(ConstData.VolumeAdjustType.ADJUST_SET, volumePercent);
            }
        }
    };
}
