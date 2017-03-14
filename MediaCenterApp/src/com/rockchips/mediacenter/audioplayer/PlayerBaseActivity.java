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

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

import com.rockchips.mediacenter.bean.LocalMediaInfo;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.utils.IICLOG;
import com.rockchips.mediacenter.utils.StringUtils;
import com.rockchips.mediacenter.service.IMediaPlayerAdapter;
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
    private static final String TAG = "AudioPlayer_Base";
    private IICLOG Log = IICLOG.getInstance();
    /*
     * 标示播放的是否为来自Media Center Service的数据
     */
    public boolean mBMCSMode;

    private PlayerCallbackMessenger mPlayerCallbackMessenger;

    // MCS Client端
    protected MediaCenterPlayerClient mMediaCenterPlayerClient;

    // 当前使用url
    protected String mStrCurrentUrl;

    private HandlerThread mMCSHandlerThread;

    protected static final int MCS_RECEIVE_MSG_MODE_REFUSE = 0;

    protected static final int MCS_RECEIVE_MSG_MODE_PROCESS = 1;

    // 是否接受消息
    private int mbReceiveMsg = MCS_RECEIVE_MSG_MODE_REFUSE;

    // 视频播放器
    private static final String STR_VIDEO_CLASS_NAME = "com.rockchips.mediacenter.videoplayer.VideoPlayerActivity";
    
    // 音乐播放器
    private static final String STR_AUDIO_CLASS_NAME = "com.rockchips.mediacenter.audioplayer.AudioPlayerActivity1";
    
    // 图片播放器
    private static final String STR_IMAGE_CLASS_NAME = "com.rockchips.mediacenter.imageplayer.ImagePlayerActivity1";

    // Home键退到Launcher界面
    private static final String STR_LAUNCHER_CLASS_NAME = "com.android.launcher2.Launcher";
    private static final String STR_REAL6_LAUNCHER_CLASS_NAME = "com.rockchips.launcher.Home2";

    // 回到mymedia浏览界面
    private static final String STR_MYMEDIA_CLASS_NAME = "com.rockchips.mediacenter.activity.MainActivity";

    private static final String STR_MYMEDIA_CLASS_NAME_EX = "com.rockchips.iptv.stb.dlna.mymedia.MediaBrowserActivity";

    // 音量管理器，用于调节系统音量
    protected AudioManager mAudioManager;

    private Object mCallbackLock = new Object();

    private RemoteController mRemoteControlCallback = null;

    private Intent mExtraIntent = null;

    protected boolean mReuseAudioplayer = false;

    public static boolean mIsInternalAudioPlayer = false;    

    protected AudioPlayStateInfo mAudioPlayStateInfo;
    private static List<Bundle> mStBundleList;
    private static int mStCurrentIndex;
    /**
     * 从其他应用跳转至媒体中心的音乐播放器
     */
    protected Uri mExtraUri;
    private Handler mConnectListenerHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case ConstData.ServiceConnectionMSG.MSG_SERVICE_DISCONNECTED:
                    Log.d(TAG, "------------->proc MSG_SERVICE_DISCONNECTED IN");
                    synchronized (mCallbackLock)
                    {
                        if (mRemoteControlCallback != null)
                        {
                            mRemoteControlCallback.onRemoteDisconnect();
                        }
                        else
                        {
                            // FIXME: 有时3D引擎还没有初始化好，此时强行退出程序
                            // exitQuickly();
                            finish();
                        }
                    }

                    Log.d(TAG, "------------->proc MSG_SERVICE_DISCONNECTED OUT");
                    break;

                default:
                    break;
            }
        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreate - IN: " + this);
        // 初始化音量管理器
        if (mAudioManager == null)
        {
            Log.d(TAG, "audioManager == null, create a AudioManager object");
            mAudioManager = (AudioManager) this.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        }
        
        mAudioPlayStateInfo = new AudioPlayStateInfo();
        mAudioPlayStateInfo.setMediaList(mStBundleList, mStCurrentIndex);
        mAudioPlayStateInfo.setCurrentIndex(mStCurrentIndex);
        
        super.onCreate(savedInstanceState);
        mExtraIntent = getIntent();
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
//                exitQuickly();
            }
            else
            {
                if (mBMCSMode)
                {
                    dobind();
                }
            }
            mExtraIntent = null;
        }
        setMCSReceiveMsgMode(MCS_RECEIVE_MSG_MODE_PROCESS);

        if (mBMCSMode)
        {
            if (mMediaCenterPlayerClient != null && mMediaCenterPlayerClient.isConnected())
            {
                Log.d(TAG, "mMediaCenterPlayerClient != null && mMediaCenterPlayerClient.isConnected()");

                mMediaCenterPlayerClient.requestList();
            }
        }

        super.onResume();
        Log.d(TAG, "onResume - OUT");
    }

    @Override
    protected void onPause()
    {
        Log.d(TAG, "onPause - IN: " + this);

        super.onPause();

//        if (isFinishSelfOnPause())
//        {
//            Log.d(TAG, "Finish myself!");
//
//            if (isMCSMode() && getPlayerClient() != null && getPlayerClient().isConnected())
//            {
//                Log.d(TAG, "----------->MediaCenterPlayerClient::stop()");
//                getPlayerClient().stop();
//            }
//
//            if (isMCSMode())
//            {
//                unbind();
//                finish();
////                exitQuickly();
//            }
//        }

        Log.d(TAG, "onPause - OUT");
    }

    @Override
    protected void onStop()
    {
        Log.d(TAG, "onStop - IN: " + this);
        super.onStop();

        if (isFinishSelfOnStop())
        {
            Log.d(TAG, "Finish myself!");
            onNotifyStop();
            unbind();            
            finish();
        }
        Log.d(TAG, "onStop - OUT");
    }

    @Override
    protected void onDestroy()
    {
        Log.d(TAG, "onDestroy - IN: " + this);

        // 设置拒绝信令
        setMCSReceiveMsgMode(MCS_RECEIVE_MSG_MODE_REFUSE);

        if (mBMCSMode)
        {
            unbind();
        }

        AudioPlayStateInfo.recycle();

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

        // 获取当前播放索引
        int playIndex = intent.getIntExtra(ConstData.IntentKey.CURRENT_INDEX, 0);
        
        // 解析当前播放列表
        ArrayList<Bundle> mediaBaseList = intent.getParcelableArrayListExtra(ConstData.IntentKey.MEDIA_INFO_LIST);

        mIsInternalAudioPlayer = intent.getBooleanExtra(ConstData.IntentKey.IS_INTERNAL_PLAYER, false);
        Log.d(TAG, "isInternalPlayer is : " + mIsInternalAudioPlayer);

        if (mIsInternalAudioPlayer)
        {
            if (mediaBaseList != null)
            {
                mAudioPlayStateInfo.setMediaList(mediaBaseList, playIndex);
                mAudioPlayStateInfo.setCurrentIndex(playIndex);
            }
            mAudioPlayStateInfo.syncList();
            mBMCSMode = false;
            return true;
        }

        mReuseAudioplayer = intent.getBooleanExtra(ConstData.IntentKey.IS_REUSE_AUDIOPLAYER, false);
        Log.d(TAG, "mReuseAudioplayer is : " + mReuseAudioplayer);      

        // 获取客户端唯一标识
        String senderClientUniq = intent.getStringExtra(ConstData.IntentKey.UNIQ);

        Log.d(TAG, "senderClientUniq is :" + senderClientUniq);

        if (StringUtils.isNotEmpty(senderClientUniq))
        {
            mAudioPlayStateInfo.setSenderClientUniq(senderClientUniq);
        }
        else
        {
            mAudioPlayStateInfo.setSenderClientUniq(ConstData.ClientTypeUniq.UNKNOWN_UNIQ);
        }

        // 判断参数是否有效
        if (mediaBaseList != null)
        {
            Log.d(TAG, "MCS DATA : mediaBaseList.size:" + mediaBaseList.size());
            if (mediaBaseList.size() > 0)
            {
                if (mIsInternalAudioPlayer)
                {
                    mBMCSMode = false;
                }
                else
                {
                    mBMCSMode = true;
                }
            }
            else
            {
                return false;
            }
        }
        else
        {
            // 搜索出来的文件进行播放
            Log.i(TAG, "data: " + intent.getData());

            String url = intent.getStringExtra(ConstData.IntentKey.SEARCH_DATA);

            Log.i(TAG, "parseInputIntent ---> url: " + url);

            if (StringUtils.isNotEmpty(url))
            {
                // 都不为空，认为是搜索出来的
                String mediaTitle = intent.getStringExtra(ConstData.IntentKey.SEARCH_TITLE);
                String mediaName = intent.getStringExtra(ConstData.IntentKey.SEARCH_DISPLAYNAME);
                String artist = intent.getStringExtra(ConstData.IntentKey.SEARCH_ARTIST);
                String title = intent.getStringExtra(ConstData.IntentKey.SEARCH_TITLE);
                String album = intent.getStringExtra(ConstData.IntentKey.SEARCH_ALBUM);
                int deviceType = intent.getIntExtra("DeviceType", ConstData.DeviceType.DEVICE_TYPE_UNKNOWN);
                int mediaType = intent.getIntExtra(ConstData.IntentKey.SEARCH_MEDIATYPE, ConstData.MediaType.UNKNOWN_TYPE);
                Log.d(TAG, "parseInputIntent ---> artist:" + artist);
                Log.d(TAG, "parseInputIntent ---> title:" + title);
                Log.d(TAG, "parseInputIntent ---> deviceType:" + deviceType);
                Log.d(TAG, "parseInputIntent ---> album:" + album);
                Log.i(TAG, "parseInputIntent ---> mediaName: " + mediaName);
                Log.i(TAG, "parseInputIntent ---> mediaTitle: " + mediaTitle);

                LocalMediaInfo mbi = new LocalMediaInfo();
                mbi.setmData(url);
//                mbi.setDeviceId("ANDROID_SYSTEM");

                // 设置设备类型
                mbi.setmDeviceType(deviceType);

                // 设置艺术家、标题和唱片集，便于音乐播放器搜索歌词
                mbi.setmArtist(artist);
                mbi.setmTitle(title);
                mbi.setmAlbum(album);

                mbi.setmFileName(mediaName);

                mbi.setmFileType(mediaType);
                mediaBaseList = new ArrayList<Bundle>();

                Bundle bundle = mbi.compress();
                mediaBaseList.add(bundle);

                mAudioPlayStateInfo.setNeedShowMenuBar(true);

                Log.i(TAG, "parseInputIntent ---> LocalMediaInfo: " + mbi.toString());
            }
            else
            {// 第三方文件管理软件点击进入的.
            	
            	mExtraUri = intent.getData();
                Uri httpUri = intent.getData();

                if (httpUri == null)
                {
                    // 输入的数据无效
                    Log.i(TAG, "parseInputIntent: MCS DATA and HTTP URI are invalidate");
                    return false;
                }

                String struri = httpUri.toString();
                //
          /*      if (struri.startsWith("file://"))
                {
                    try
                    {
                        struri = Uri.parse(struri).getPath();
                    }
                    catch (Exception e)
                    {
                        struri = struri.substring("file://".length());
                    }
                }
*/
                LocalMediaInfo mbi = new LocalMediaInfo();
                mbi.setmData(struri);
//                mbi.setDeviceId("ANDROID_SYSTEM");
                int deviceType = getDeviceType(struri);
                mbi.setmDeviceType(deviceType);
                Log.i(TAG, "parseInputIntent->extraDeviceType:" + deviceType);
                if(deviceType == ConstData.DeviceType.DEVICE_TYPE_OTHER){
                	mbi.setUrl(httpUri.toString());
                }
                
                // modify by w00184463 2012-7-3 begin
                String strDisplayName = null;
                // 从intent中获取 适用于C02Launcher启动音乐播放器
                strDisplayName = intent.getStringExtra("displayname");

                // 从uri中获取文件名
                if (strDisplayName == null || strDisplayName.equals(""))
                {
                    strDisplayName = StringUtils.getFileName(struri);
                }
                // modify by w00184463 2012-7-3 end

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

            mBMCSMode = false;
        }

        // 设置当前播放
        mAudioPlayStateInfo.setMediaList(mediaBaseList, playIndex);
        mAudioPlayStateInfo.setCurrentIndex(playIndex);
        mAudioPlayStateInfo.setSenderClientUniq(senderClientUniq);

        mediaBaseList = null;

        // DTS2012030204438
        if (mMediaCenterPlayerClient != null)
        {
            Log.d(TAG, "Reset the sender client in the player client");
            Log.d(TAG, "Reset to:" + mAudioPlayStateInfo.getSenderClientUniq());
            mMediaCenterPlayerClient.setSenderUniq(mAudioPlayStateInfo.getSenderClientUniq());
        }

        mAudioPlayStateInfo.syncList();

        Log.i(TAG, "parseInputIntent:playIndex " + playIndex + ",MediaList size is " + mAudioPlayStateInfo.getMediaList().size());

        return true;
    }

    /**
     * 
     * getDeviceType 通过url判断是网络的还是本地的视频
     * 
     * @param str
     * @return int
     * @exception
     */
    private int getDeviceType(String str)
    {
        if (str.startsWith("http") || str.startsWith("rtsp"))
        {
            return ConstData.DeviceType.DEVICE_TYPE_DMS;
        }
        else if(str.startsWith("file"))
        {
            return ConstData.DeviceType.DEVICE_TYPE_U;
        }else{
        	return ConstData.DeviceType.DEVICE_TYPE_OTHER;
        }
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
        if (!mBMCSMode)
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

            Log.d(TAG, mAudioPlayStateInfo.getSenderClientUniq());
            mMediaCenterPlayerClient.setSenderUniq(mAudioPlayStateInfo.getSenderClientUniq());

            mMediaCenterPlayerClient.registerPlayerCallBack(mPlayerCallbackMessenger);
            mMediaCenterPlayerClient.doBindService(this.getApplicationContext());
            mMediaCenterPlayerClient.setListener(mConnectListenerHandler);
        }
        Log.d(TAG, " end with connect MCS service ");
    }

    /**
     * getUUID
     * 
     * @return int
     * @exception
     */
    protected abstract int getUUID();

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

            if (!mBMCSMode)
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

            // IfxFramework hIfxFramework = (IfxFramework) getApplication();

            Intent intent = (Intent) msg.obj;

            switch (msg.what)
            {
                case ConstData.MCSMessage.MSG_SET_MEDIA_DATA:
                    Log.d(TAG, "MSG_SET_MEDIA_DATA start");
                    parseInputIntent(intent);
                    synchronized (mCallbackLock)
                    {
                        if (mRemoteControlCallback != null)
                        {
                            mRemoteControlCallback.setDataSource(intent);
                        }
                    }
                    Log.d(TAG, "MSG_SET_MEDIA_DATA end");

                    break;
                case ConstData.MCSMessage.MSG_PLAY:
                    Log.d(TAG, "MSG_PLAY start");
                    synchronized (mCallbackLock)
                    {
                        if (mRemoteControlCallback != null)
                        {
                            mRemoteControlCallback.play(intent);
                        }
                    }

                    Log.d(TAG, "MSG_PLAY end");

                    break;
                case ConstData.MCSMessage.MSG_PAUSE:
                    Log.d(TAG, "MSG_PAUSE start");
                    synchronized (mCallbackLock)
                    {
                        if (mRemoteControlCallback != null)
                        {
                            mRemoteControlCallback.pause(intent);
                        }
                    }
                    Log.d(TAG, "MSG_PAUSE end");

                    break;
                case ConstData.MCSMessage.MSG_SEEK:
                    Log.d(TAG, "MSG_SEEK start");
                    synchronized (mCallbackLock)
                    {
                        if (mRemoteControlCallback != null)
                        {
                            mRemoteControlCallback.seekTo(intent);
                        }
                    }

                    Log.d(TAG, "MSG_SEEK end");

                    break;
                case ConstData.MCSMessage.MSG_STOP:
                    Log.d(TAG, "MSG_STOP start");

                    // 收到stop信令就进行解绑操作，避免由于在onDestroy()中解绑太慢，导致MCS中刚注册上的播放器回调被正销毁的播放器反注册掉
                    // 执行拉回时，stop状态已经在MCS中返回给Sender端
                    unbind();

                    synchronized (mCallbackLock)
                    {
                        if (mRemoteControlCallback != null)
                        {
                            mRemoteControlCallback.stop(intent);
//                            exitQuickly();
                        }
                        else
                        {
                            Log.d(TAG, "MSG_STOP -----------NULL");
                            // FIXME: 有时3D引擎还没有初始化好，此时强行退出程序
                            // finish();
//                            exitQuickly();
                        }
                        finish();
                    }

                    Log.d(TAG, "MSG_STOP end");

                    break;
                case ConstData.MCSMessage.MSG_APPEND_MEDIA_DATA:
                    Log.d(TAG, "MSG_APPEND_MEDIA_DATA start");
                    long package_id = intent.getLongExtra(ConstData.IntentKey.MEDIALIST_ID, 0);
                    if (package_id != 0)
                    {
                        if (mMediaListPackageArray.size() > 0)
                        {
                            if (mMediaListPackageArray.get(0).package_id != package_id)
                            {
                                mMediaListPackageArray.clear();
                            }
                        }
                        MediaListPackage mediaListPackage = new MediaListPackage();
                        mediaListPackage.package_id = package_id;
                        mediaListPackage.orderId = intent.getIntExtra(ConstData.IntentKey.MEDIALIST_PACKAGE_ORDERID, -1);
                        mediaListPackage.package_count = intent.getIntExtra(ConstData.IntentKey.MEDIALIST_PACKAGE_COUNT, -1);
                        mediaListPackage.mMediaInfoList = intent.getParcelableArrayListExtra(ConstData.IntentKey.MEDIA_INFO_LIST);
                        mMediaListPackageArray.add(mediaListPackage);

                        if (mediaListPackage.package_count == mMediaListPackageArray.size())
                        {
                            // 包结束
                            ArrayList<Bundle> allMediaList = new ArrayList<Bundle>();
                            for (int i = 0; i < mMediaListPackageArray.size(); i++)
                            {
                                allMediaList.addAll(mMediaListPackageArray.get(i).mMediaInfoList);
                            }
                            mMediaListPackageArray.clear();
                            mAudioPlayStateInfo.setMediaList(allMediaList);

                        }

                    }
                    else
                    {
                        ArrayList<Bundle> mediaBaseList = intent.getParcelableArrayListExtra(ConstData.IntentKey.MEDIA_INFO_LIST);

                        mAudioPlayStateInfo.addList(mediaBaseList);
                        mcsAppendList(intent);

                    }

                    Log.d(TAG, "MSG_APPEND_MEDIA_DATA end");

                    break;
                case ConstData.MCSMessage.MSG_DEVICE_DOWN:
                    Log.d(TAG, "MSG_DEVICE_DOWN start");

                    if (mAudioPlayStateInfo.getDeviceId() == null)
                    {
                        break;
                    }

                    String deviceId = intent.getStringExtra(ConstData.IntentKey.DEVICE_ID);
                    Log.d(TAG, "deviceId:" + deviceId);

                    if (null == deviceId || deviceId.trim().equals(""))
                    {
                        Log.e(TAG, "The deviceId transfered from the DMS client is null");
                        break;
                    }

                    break;
                case ConstData.MCSMessage.MSG_ADJUST_VOLUME:
                    Log.d(TAG, "MSG_ADJUST_VOLUME start");

                    int volumeAdjustType = intent.getIntExtra(ConstData.IntentKey.VOLUME_ADJUST_TYPE, ConstData.VolumeAdjustType.ADJUST_UNKNOWND);
                    Log.d(TAG, "volumeAdjustType:" + volumeAdjustType);

                    if (volumeAdjustType != ConstData.VolumeAdjustType.ADJUST_UNKNOWND)
                    {
                        if (mAudioManager == null)
                        {
                            Log.d(TAG, "audioManager is null, new a AudioManager object");
                            mAudioManager = (AudioManager) PlayerBaseActivity.this.getSystemService(Context.AUDIO_SERVICE);
                        }

                        if (volumeAdjustType == ConstData.VolumeAdjustType.ADJUST_LOWER)
                        {
                            Log.d(TAG, "Adjust the volume to lower");

                            mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER,
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

                            mAudioManager
                                    .adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_SAME, AudioManager.FX_FOCUS_NAVIGATION_UP);

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

                            mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE,
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

                            mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);

                            // add by xWX184171 2014.3.19 显示音量条
                            mAudioManager
                                    .adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_SAME, AudioManager.FX_FOCUS_NAVIGATION_UP);

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

                            mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);

                            // add by xWX184171 2014.3.19 显示音量条
                            mAudioManager
                                    .adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_SAME, AudioManager.FX_FOCUS_NAVIGATION_UP);

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
                            

                            int volumeValueIndex = (int) (mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * volumePercent);
							Log.d(TAG, "volumeValueIndex:" + volumeValueIndex);
                            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volumeValueIndex, AudioManager.FX_FOCUS_NAVIGATION_UP);

                            // 将设置好的音量值返回给Sender端
                            if (mMediaCenterPlayerClient != null)
                            {
                                Log.d(TAG, "Send the volume percent to Sender client");
                                mMediaCenterPlayerClient.adjustVolume(ConstData.VolumeAdjustType.ADJUST_SET, volumePercent);
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
     * @return boolean
     * @exception
     */
    public boolean canReceiveMsg()
    {
        return mbReceiveMsg == MCS_RECEIVE_MSG_MODE_PROCESS;
    }

    /**
     * canReceiveMsg
     * 
     * @return boolean
     * @exception
     */
    public void setMCSReceiveMsgMode(int mode)
    {
        mbReceiveMsg = mode;
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
        AudioPlayStateInfo.setPlayMode(playMode);
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
        return AudioPlayStateInfo.getPlayMode();
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

    // 判断该播放器被覆盖时，是否关闭该播放器
    // 场景1：在该播放器播放媒体文件时，第三方软件通过系统再次启动了三个播放器中的一个（如，搜索播放），此时该播放器被后启动的播放器覆盖时应该销毁
    // 场景2：Home键回到Launcher界面时，关闭播放器
    public boolean isFinishSelfOnPause()
    {
        Log.d(TAG, "isFinishSelfOnPause --In");

        try
        {
            ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

            List<RunningTaskInfo> runningTaskInfo = activityManager.getRunningTasks(1);
            if (null == runningTaskInfo || runningTaskInfo.isEmpty())
            {
                Log.e(TAG, "null == runningTaskInfo || runningTaskInfo.isEmpty()");
                return false;
            }
            // 获取当前运行的task的第一个activity
            ComponentName componentName = runningTaskInfo.get(0).topActivity;
            
            // 获取栈顶Activity的类名
            String componentClassName = componentName.getClassName();

            Log.d(TAG, "isFinishSelfOnPause --> componentClassName:" + componentClassName);

            if (componentClassName.equalsIgnoreCase(STR_VIDEO_CLASS_NAME))
            {
                Log.d(TAG, "The Forgroud Activity Is VideoPlayerActivity Or MusicPlayerActivity Or ImagePlayerActivity");
                
                return true;
            }            
            else if (componentClassName.equalsIgnoreCase(STR_IMAGE_CLASS_NAME))
            {
                return true;
            }
            else if (componentClassName.equalsIgnoreCase(STR_LAUNCHER_CLASS_NAME)
                    || componentClassName.equalsIgnoreCase(STR_REAL6_LAUNCHER_CLASS_NAME)
                    || componentClassName.equalsIgnoreCase(STR_MYMEDIA_CLASS_NAME) 
                    || componentClassName.equalsIgnoreCase(STR_MYMEDIA_CLASS_NAME_EX))
            {
                Log.d(TAG, "The Forgroud Activity Is " + componentClassName);

                return true;
            }
        }
        catch (SecurityException e)
        {
            Log.w(TAG, "isFinishSelfOnPause --> SecurityException:" + e.getLocalizedMessage());
        }

        return false;
    }

    public boolean isFinishSelfOnStop()
    {
        Log.d(TAG, "isFinishSelfOnStop --In");
        if (!isMCSMode())
        {
            return false;
        }

//        try
//        {
//            ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
//
//            // 获取当前运行的task的第一个activity
//            ComponentName componentName = activityManager.getRunningTasks(1).get(0).topActivity;
//
//            // 获取栈顶Activity的类名
//            String componentClassName = componentName.getClassName();
//
//            Log.d(TAG, "isFinishSelfOnStop --> componentClassName:" + componentClassName);
//
//            if (componentClassName.equalsIgnoreCase(STR_AUDIO_CLASS_NAME))
//            {
//                Log.d(TAG, "The Forgroud Activity Is " + componentClassName);
//
//                return true;
//            }
//            else if (componentClassName.equalsIgnoreCase(STR_VIDEO_CLASS_NAME))
//            {
//                Log.d(TAG, "The Forgroud Activity Is VideoPlayerActivity Or MusicPlayerActivity Or ImagePlayerActivity");
//                
//                return true;
//            }            
//            else if (componentClassName.equalsIgnoreCase(STR_IMAGE_CLASS_NAME))
//            {
//                return true;
//            }
//            else if (componentClassName.equalsIgnoreCase(STR_LAUNCHER_CLASS_NAME)
//                    || componentClassName.equalsIgnoreCase(STR_REAL6_LAUNCHER_CLASS_NAME)
//                    || componentClassName.equalsIgnoreCase(STR_MYMEDIA_CLASS_NAME) 
//                    || componentClassName.equalsIgnoreCase(STR_MYMEDIA_CLASS_NAME_EX))
//            {
//                Log.d(TAG, "The Forgroud Activity Is " + componentClassName);
//
//                return true;
//            }
//        }
//        catch (SecurityException e)
//        {
//            Log.w(TAG, "isFinishSelfOnStop --> SecurityException:" + e.getLocalizedMessage());
//        }

        return true;
    }

    private RemoteCallback mRemoteCallback = new RemoteCallback()
    {
        @Override
        public void onStop()
        {
            Log.d(TAG, "RemoteCallback::onStop - IN");
            if (isMCSMode() && getPlayerClient() != null && getPlayerClient().isConnected())
            {
                Log.d(TAG, "----------->MediaCenterPlayerClient::stop()");
                onNotifyStop();
            }

        }

        @Override
        public void onProgress(int pos)
        {
            Log.d(TAG, "RemoteCallback::onProgress - IN");
            if (isMCSMode() && getPlayerClient() != null && getPlayerClient().isConnected())
            {
                Log.d(TAG, "----------->MediaCenterPlayerClient::seek()" + pos);
                getPlayerClient().seek(pos);
            }

        }

        @Override
        public void onPlay()
        {
            Log.d(TAG, "RemoteCallback::onPlay - IN");
            if (isMCSMode() && getPlayerClient() != null && getPlayerClient().isConnected())
            {
                Log.d(TAG, "----------->MediaCenterPlayerClient::play()");
                getPlayerClient().play();
            }

        }

        @Override
        public void onPause()
        {
            Log.d(TAG, "RemoteCallback::onPause - IN");
            if (isMCSMode() && getPlayerClient() != null && getPlayerClient().isConnected())
            {
                Log.d(TAG, "----------->MediaCenterPlayerClient::pause()");
                getPlayerClient().pause();
            }

        }

        @Override
        public void onPrepared(IMediaPlayerAdapter mp)
        {
            Log.d(TAG, "RemoteCallback::onPrepared - IN");
            if (isMCSMode() && getPlayerClient() != null && getPlayerClient().isConnected())
            {

                int duration = mp.getDuration();
                int position = mp.getCurrentPosition();
                Log.d(TAG, "----------->MediaCenterPlayerClient::reportDuration()" + duration);
                getPlayerClient().reportDuration(position, duration);

                // tangss modify it
                if (isSupportToSeekWhenParepare())
                {

                }
                else
                {
                    Log.d(TAG, "----------->MediaCenterPlayerClient::seek(0)");
                    getPlayerClient().seek(0);
                }
            }

        }

        @Override
        public void onCompletion(IMediaPlayerAdapter mp)
        {
            Log.d(TAG, "RemoteCallback::onCompletion - IN");
            if (isMCSMode() && getPlayerClient() != null && getPlayerClient().isConnected())
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
            if (isMCSMode() && getPlayerClient() != null && getPlayerClient().isConnected())
            {

//                getPlayerClient().stop();
            }
        }
    };

    public void setRemoteControlCallback(RemoteController callback)
    {
        synchronized (mCallbackLock)
        {
            mRemoteControlCallback = callback;
            if (mRemoteControlCallback != null)
            {
                mRemoteControlCallback.setCallback(mRemoteCallback);
            }
        }

    }
       

    // public RemoteController getRemoteControlCallback() {
    // return mRemoteControlCallback;
    // }

    public MediaCenterPlayerClient getPlayerClient()
    {
        return mMediaCenterPlayerClient;
    }

    public boolean isMCSMode()
    {
        return mBMCSMode;
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

    private ArrayList<MediaListPackage> mMediaListPackageArray = new ArrayList<MediaListPackage>();

    class MediaListPackage
    {
        long package_id;

        int package_count;

        int orderId;

        ArrayList<Bundle> mMediaInfoList;
    }
    
    public static void setMediaList(List<Bundle> bundleList, int currentIndex)
    {
        mStBundleList =  bundleList;
        mStCurrentIndex = currentIndex;        
    }

    protected void onNotifyStop()
    {
        LocalMediaInfo info = mAudioPlayStateInfo.getMediaInfo(mAudioPlayStateInfo.getCurrentIndex());

        if ((getPlayerClient() != null) && (info != null))
        {
            getPlayerClient().stop(info.getUrl());
        }
    }
}
