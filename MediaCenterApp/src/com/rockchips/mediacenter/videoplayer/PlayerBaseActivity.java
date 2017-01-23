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
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.rockchips.mediacenter.basicutils.bean.LocalDeviceInfo;
import com.rockchips.mediacenter.basicutils.constant.Constant;
import com.rockchips.mediacenter.basicutils.util.StringUtils;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.activity.DeviceActivity;
import com.rockchips.mediacenter.playerclient.MediaCenterPlayerClient;
import com.rockchips.mediacenter.videoplayer.data.VideoInfo;
import com.rockchips.mediacenter.viewutils.menu.BottomPopMenu.VolumeKeyListener;

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
    protected int TOAST_SHOW_TIME = 2000;
    
    //当前使用url
    protected String mStrCurrentUrl = null;
    
    private HandlerThread mMCSHandlerThread = null;
    
    protected static final int MCS_RECEIVE_MSG_MODE_REFUSE = 0;
    
    protected static final int MCS_RECEIVE_MSG_MODE_PROCESS = 1;
    
    //是否接受消息
    private int mbReceiveMsg = MCS_RECEIVE_MSG_MODE_REFUSE;
    
    private VideoInfo mMediaBaseInfo = null;
    
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
    
    protected static List<Bundle> mStbundleList;
    protected static int mStcurrentIndex;
//    protected static String mStdevName;
    protected LocalDeviceInfo mSelectedDev;
    
    /**
     * 标记是否是阿拉伯地区语言
     * */
    protected boolean mbAR = false;
    
    /**
     * 标志Sender端的标示
     */
    private String mSenderClientUniq = null;
    
    private Handler connectListenerHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case Constant.ServiceConnectionMSG.MSG_SERVICE_DISCONNECTED:
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
        //获取国家代码 modify zwx143228
        String able= getResources().getConfiguration().locale.getCountry();
        Log.d(TAG, "locale.getCountry() = " + able);
        if("EG".equalsIgnoreCase(able))
        {
            mbAR = true;
        }
        else
        {
            mbAR = false;
        }
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
        
        // 获取当前播放的媒体信息，媒体与索引有关。
        mMediaBaseInfo = mPlayStateInfo.getCurrentMediaInfo();
        
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
                Log.d(TAG, "mMediaCenterPlayerClient != null && mMediaCenterPlayerClient.isConnected()");
                
                mMediaCenterPlayerClient.requestList();
            }
        }
        
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
        
        // 解析来自MCS的参数
        ArrayList<Bundle> mediaBaseList = intent.getParcelableArrayListExtra(Constant.IntentKey.MEDIA_INFO_LIST);
        
        //获取当前播放索引
        int playIndex = intent.getIntExtra(ConstData.IntentKey.CURRENT_INDEX, 0);
        
        //获取客户端唯一标识
        String senderClientUniq = intent.getStringExtra(ConstData.IntentKey.UNIQ);
        
        boolean isInternalPlayer = intent.getBooleanExtra(ConstData.IntentKey.IS_INTERNAL_PLAYER, false);
        
        if (!TextUtils.isEmpty(senderClientUniq))
        {
            setSenderClientUniq(senderClientUniq);
        }
        else
        {
            setSenderClientUniq(ConstData.ClientTypeUniq.UNKNOWN_UNIQ);
        }
        
        // 创建备份列表
        if (mPlayStateInfo != null)
        {
            mPlayStateInfo.setStop(true);
            mPlayStateInfo.destroyPlayState();
            mPlayStateInfo = null;
        }
        
        mSelectedDev = new LocalDeviceInfo();
        Bundle devbundle = intent.getBundleExtra(LocalDeviceInfo.DEVICE_EXTRA_NAME);
        if (devbundle != null)
        {
            mSelectedDev.decompress(devbundle);
        }
        
        mPlayStateInfo = new PlayStateInfo();        
        
        // 如果是内部播放器，这直接返回
        if (isInternalPlayer)
        {
            bMCSMode = false;
            if (null == mediaBaseList)
            {
                mPlayStateInfo.setMediaList(mStbundleList);
                mPlayStateInfo.setCurrentIndex(mStcurrentIndex);
            }
            else
            {
                mPlayStateInfo.setMediaList(mediaBaseList);
                mPlayStateInfo.setCurrentIndex(playIndex);
            }
            String mStdevName = "";
            if (null != mSelectedDev)
            {
                mStdevName = mSelectedDev.getPhysicId();
            }
            mPlayStateInfo.setCurrDevName(mStdevName);
            return;
        }
        
        // 判断参数是否有效
        if (mediaBaseList != null)
        {
            Log.i(TAG, "MCS DATA : mediaBaseList.size:" + mediaBaseList.size());
            
            //            if (mediaBaseList.size() > 0)
            //            {
            //                bMCSMode = true;
            //            }
            if (mediaBaseList.size() > 0)
            {
                if (isInternalPlayer)
                {
                    bMCSMode = false;
                }
                else
                {
                    bMCSMode = true;
                }
            }
            else
            {
                finish();
                return;
            }
        }
        // 保存输入参数
//        if (mPlayStateInfo != null)
//        {
//            mPlayStateInfo.setStop(true);
//            mPlayStateInfo = null;
//        }
//        
//        mPlayStateInfo = PlayStateInfo.getPlayStateInfoInst();
        //设置当前播放
        mPlayStateInfo.setCurrentIndex(playIndex);
        mPlayStateInfo.setSenderClientUniq(senderClientUniq);
        mPlayStateInfo.setMediaList(mediaBaseList);
        mediaBaseList = null;
        
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
            return Constant.DeviceType.DEVICE_TYPE_DMS;
        }
        else
        {
            return Constant.DeviceType.DEVICE_TYPE_U;
        }
    }
    
    protected int insertList(List<Bundle> list)
    {
        mPlayStateInfo.insertList(list);
        
        return mPlayStateInfo.getMediaList().size();
    }
    
    protected int deleteList(String devId)
    {
        
        VideoInfo mbi = getCurrentMediaInfo();
        
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
            
            if (mPlayStateInfo != null)
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
                
                //只有在连接的情况下，才进行反注册和解除连接
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
        PlayerCallbackMessenger(Looper looper)
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
                case Constant.MCSMessage.MSG_SET_MEDIA_DATA:
                    Log.d(TAG, "MSG_SET_MEDIA_DATA start");
                    
                    mcsSetMediaData(intent);
                    
                    Log.d(TAG, "MSG_SET_MEDIA_DATA end");
                    
                    break;
                case Constant.MCSMessage.MSG_PLAY:
                    Log.d(TAG, "MSG_PLAY start");
                    
                    mcsPlay(intent);
                    
                    Log.d(TAG, "MSG_PLAY end");
                    
                    break;
                case Constant.MCSMessage.MSG_PAUSE:
                    Log.d(TAG, "MSG_PAUSE start");
                    
                    mcsPause(intent);
                    
                    Log.d(TAG, "MSG_PAUSE end");
                    
                    break;
                case Constant.MCSMessage.MSG_SEEK:
                    Log.d(TAG, "MSG_SEEK start");
                    
                    mcsSeek(intent);
                    
                    Log.d(TAG, "MSG_SEEK end");
                    
                    break;
                case Constant.MCSMessage.MSG_STOP:
                    Log.d(TAG, "MSG_STOP start");
                    
                    // 收到stop信令就进行解绑操作，避免由于在onDestroy()中解绑太慢，导致MCS中刚注册上的播放器回调被正销毁的播放器反注册掉
                    // 执行拉回时，stop状态已经在MCS中返回给Sender端
                    unbind();
                    mcsStop(intent);
                    
                    Log.d(TAG, "MSG_STOP end");
                    
                    break;
                case Constant.MCSMessage.MSG_APPEND_MEDIA_DATA:
                    Log.d(TAG, "MSG_APPEND_MEDIA_DATA start");
                    long package_id = intent.getLongExtra(Constant.IntentKey.MEDIALIST_ID, 0);
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
                        mediaListPackage.orderId = intent.getIntExtra(Constant.IntentKey.MEDIALIST_PACKAGE_ORDERID, -1);
                        mediaListPackage.package_count =
                            intent.getIntExtra(Constant.IntentKey.MEDIALIST_PACKAGE_COUNT, -1);
                        mediaListPackage.mMediaInfoList =
                            intent.getParcelableArrayListExtra(Constant.IntentKey.MEDIA_INFO_LIST);
                        mMediaListPackageArray.add(mediaListPackage);
                        
                        if (mediaListPackage.package_count == mMediaListPackageArray.size())
                        {
                            //包结束
                            ArrayList<Bundle> allMediaList = new ArrayList<Bundle>();
                            for (int i = 0; i < mMediaListPackageArray.size(); i++)
                            {
                                allMediaList.addAll(mMediaListPackageArray.get(i).mMediaInfoList);
                            }
                            mMediaListPackageArray.clear();
                            mPlayStateInfo.setMediaList(allMediaList);
                            
                        }
                        
                    }
                    else
                    {
                        ArrayList<Bundle> mediaBaseList =
                            intent.getParcelableArrayListExtra(Constant.IntentKey.MEDIA_INFO_LIST);
                        
                        if (mPlayStateInfo != null)
                        {
                            mPlayStateInfo.insertList(mediaBaseList);
                            mcsAppendList(intent);
                        }
                        
                    }
                    
                    Log.d(TAG, "MSG_APPEND_MEDIA_DATA end");
                    
                    break;
                case Constant.MCSMessage.MSG_DEVICE_DOWN:
//                    Log.d(TAG, "MSG_DEVICE_DOWN start");
//                    
//                    String deviceId = intent.getStringExtra(Constant.IntentKey.DEVICE_ID);
//                    Log.d(TAG, "deviceId:" + deviceId);
//                    
//                    if (null == deviceId || deviceId.trim().length() == 0)
//                    {
//                        Log.e(TAG, "The deviceId transfered from the DMS client is null");
//                        break;
//                    }
//                    
//                    String realDeviceId = DBUtils.getDeviceId(deviceId);
//                    
//                    if (mPlayStateInfo != null)
//                    {
//                        if (realDeviceId == null)
//                        {
//                            Log.d(TAG, "realDeviceId == null");
//                            Log.d(TAG, "deviceId:" + deviceId);
//                            
//                            mPlayStateInfo.deleteList(deviceId);
//                        }
//                        else
//                        {
//                            Log.d(TAG, "realDeviceId:" + realDeviceId);
//                            
//                            mPlayStateInfo.delList(deviceId, realDeviceId);
//                        }
//                        
//                        mcsDelDevice(intent);
//                    }
                    
                    Log.d(TAG, "MSG_DEVICE_DOWN end");
                    
                    break;
                case Constant.MCSMessage.MSG_ADJUST_VOLUME:
                    Log.d(TAG, "MSG_ADJUST_VOLUME start");
                    
                    int volumeAdjustType =
                        intent.getIntExtra(Constant.IntentKey.VOLUME_ADJUST_TYPE,
                            Constant.VolumeAdjustType.ADJUST_UNKNOWND);
                    Log.d(TAG, "volumeAdjustType:" + volumeAdjustType);
                    
                    if (volumeAdjustType != Constant.VolumeAdjustType.ADJUST_UNKNOWND)
                    {
                        if (audioManager == null)
                        {
                            Log.d(TAG, "audioManager is null, new a AudioManager object");
                            audioManager =
                                (AudioManager)PlayerBaseActivity.this.getApplicationContext()
                                    .getSystemService(Context.AUDIO_SERVICE);
                        }
                        
                        if (volumeAdjustType == Constant.VolumeAdjustType.ADJUST_LOWER)
                        {
                            Log.d(TAG, "Adjust the volume to lower");
                            
                            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                                AudioManager.ADJUST_LOWER,
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
                            
                            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                                AudioManager.ADJUST_SAME,
                                AudioManager.FX_FOCUS_NAVIGATION_UP);
                            
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
                            
                            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                                AudioManager.ADJUST_RAISE,
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
                            
                            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
                            
                            //add by xWX184171 2014.3.19 显示音量条
                            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                                AudioManager.ADJUST_LOWER,
                                AudioManager.FX_FOCUS_NAVIGATION_UP);
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
                            
                            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
                            
                          //add by xWX184171 2014.3.19 显示音量条
                            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                                AudioManager.ADJUST_LOWER,
                                AudioManager.FX_FOCUS_NAVIGATION_UP);
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
                            
                            int volumeValueIndex =
                                Float.valueOf(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * volumePercent).intValue();
                            Log.d(TAG, "volumeValueIndex:" + volumeValueIndex);
                            
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                                volumeValueIndex,
                                AudioManager.FX_FOCUS_NAVIGATION_UP);
                            
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
    
    protected VideoInfo getCurrentMediaInfo()
    {
        if (mPlayStateInfo == null)
        {
            Log.e(TAG, "mPlayStateInfo null");
            return null;
        }
        
        VideoInfo mbi = mPlayStateInfo.getCurrentMediaInfo();
        if (mbi == null)
        {
            Log.e(TAG, "getCurrentMediaInfo null");
            //            mStrCurrentUrl = null;
            return null;
        }
        
        if (mbi.getUrl() == null)
        {
            Log.e(TAG, "mStrCurrentUrl  null");
            
            return null;
        }
        
        mStrCurrentUrl = new String(mbi.getUrl());
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
    *MediaBaseInfo
    * @exception
    */
    protected VideoInfo getPreMediaInfo()
    {
        if (mPlayStateInfo == null)
        {
            Log.e(TAG, "mPlayStateInfo null");
            return null;
        }
        
        VideoInfo mbi = mPlayStateInfo.getPreMediaInfo();
        if (mbi == null)
        {
            Log.e(TAG, "getPreMediaInfo null");
            //            mStrCurrentUrl = null;
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
    *MediaBaseInfo
    * @exception
    */
    protected VideoInfo getNextMediaInfo()
    {
        if (mPlayStateInfo == null)
        {
            Log.e(TAG, "mPlayStateInfo null");
            return null;
        }
        
        VideoInfo mbi = mPlayStateInfo.getNextMediaInfo();
        
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
    	// l00174030 不在发送该标志
    	return !bMCSMode;
//        if (getSenderClientUniq().equals(Constant.ClientTypeUniq.DMS_UNIQ))
//        {
//            return true;
//        }
//        else
//        {
//            return false;
//        }
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
        if (getSenderClientUniq().equalsIgnoreCase(Constant.ClientTypeUniq.PUSH_UNIQ))
        {
            return true;
        }
        
        if (getSenderClientUniq().equalsIgnoreCase(Constant.ClientTypeUniq.SYN_UINQ))
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
            if (mSenderClientUniq.equals(Constant.ClientTypeUniq.PUSH_UNIQ))
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
            if (mSenderClientUniq.equals(Constant.ClientTypeUniq.SYN_UINQ))
            {
                return true;
            }
        }
        
        return false;
    }
    
    // 是否为点击播放
    protected boolean isMyMediaType()
    {
    	// l00174030 修改模式目前没有该标志
    	return !bMCSMode;
//        if (!StringUtils.isEmpty(mSenderClientUniq))
//        {
//            if (mSenderClientUniq.equals(Constant.ClientTypeUniq.DMS_UNIQ))
//            {
//                return true;
//            }
//        }
//        
//        return false;
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
            float volumePercent = Float.valueOf(currentVolume) / Float.valueOf(maxVolume);
            Log.d(TAG, "currentVolume:" + currentVolume);
            Log.d(TAG, "maxVolume:" + maxVolume);
            Log.d(TAG, "volumePercent:" + volumePercent);
            
            if (mMediaCenterPlayerClient != null)
            {
                Log.d(TAG, "Send the volume percent to Sender client");
                mMediaCenterPlayerClient.adjustVolume(Constant.VolumeAdjustType.ADJUST_SET, volumePercent);
            }
        }
    };
    
    private ArrayList<MediaListPackage> mMediaListPackageArray = new ArrayList<MediaListPackage>();
    
    class MediaListPackage
    {
        long package_id;
        
        int package_count;
        
        int orderId;
        
        ArrayList<Bundle> mMediaInfoList;
    }
    
//    public static void setMediaList(List<Bundle> bundleList, int currentIndex, String devName)
    public static void setMediaList(List<Bundle> bundleList, int currentIndex)
    {
        mStbundleList = bundleList;
        mStcurrentIndex = currentIndex;
//        mStdevName = devName;       
    }
}
