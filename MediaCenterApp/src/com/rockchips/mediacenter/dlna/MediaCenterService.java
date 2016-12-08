/*
 * com.rockchips.iptv.stb.dlna.mediacenter
 * MediaCenterService.java
 * 
 * 2011-10-14-下午02:02:55
 * Copyright 2011 Huawei Technologies Co., Ltd
 * 
 */
package com.rockchips.mediacenter.dlna;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.rockchips.mediacenter.basicutils.bean.LocalMediaInfo;
import com.rockchips.mediacenter.basicutils.constant.Constant;
import com.rockchips.mediacenter.basicutils.util.IICLOG;
import com.rockchips.mediacenter.audioplayer.AudioPlayerActivity;
import com.rockchips.mediacenter.audioplayer.AudioPlayerService;
import com.rockchips.mediacenter.imageplayer.ImagePlayerActivity;
import com.rockchips.mediacenter.videoplayer.VideoPlayerActivity;

/**
 * 
 * MediaCenterService
 * 
 * 2011-10-14 下午02:02:55
 * 
 * @author z00184367
 * @version 1.0.0
 * 
 */
public class MediaCenterService extends Service
{
    private static final String TAG = "MediaCenterApp";

    private IICLOG Log = IICLOG.getInstance();

    private HandlerThread mSenderHandlerThread = new HandlerThread("SenderMessenger");

    private HandlerThread mPlayerHandlerThread = new HandlerThread("PlayerMessenger");

    private Messenger mSenderMessenger = null;

    private Messenger mPlayerMessenger = null;

    private Map<String, Messenger> mSenderCallbacks = new HashMap<String, Messenger>();

    private Map<String, Messenger> mPlayerCallbacks = new HashMap<String, Messenger>();

    // 播放记录信息
    private MediaInfo mMediaInfo = null;

    private Queue<Message> senderMsgQueue = new LinkedList<Message>();

    // 标示是否视频电话来了
    private boolean bVideoPhoneCalling = false;

    private VideoPhoneReceiver videoPhoneReceiver = null;

    private Notification notification = null;

    // 视频播放器
    private static final String STR_VIDEO_CLASS_NAME = "com.rockchips.mediacenter.videoplayer.VideoPlayerActivity";

    // 音乐播放器
    private static final String STR_AUDIO_CLASS_NAME = "com.rockchips.mediacenter.audioplayer.AudioPlayerActivity";

    // 图片播放器
    private static final String STR_IMAGE_CLASS_NAME = "com.rockchips.mediacenter.imageplayer.ImagePlayerActivity";

    private static final String STR_EXIT_PLAYER = "com.rockchips.iptv.stb.dlna.action.exitplayer";

    @Override
    public void onCreate()
    {
        Log.d(TAG, "MediaCenterService --> onCreate()");

        if (mSenderMessenger == null)
        {
            mSenderHandlerThread.start();
            mSenderMessenger = new Messenger(new SenderHandler(mSenderHandlerThread.getLooper()));
        }

        if (mPlayerMessenger == null)
        {
            mPlayerHandlerThread.start();
            mPlayerMessenger = new Messenger(new PlayerHandler(mPlayerHandlerThread.getLooper()));

        }

        // 提高此Service的优先级为最高
        if (notification == null)
        {
            // 1、在使用startForeground时，如果id为0将不会显示notification
            // 2、在使用startForeground时，notification不能为null
            notification = new Notification();
            startForeground(0, notification);
        }

        mSenderCallbacks.clear();
        mPlayerCallbacks.clear();
        senderMsgQueue.clear();

        if (null == videoPhoneReceiver)
        {
            videoPhoneReceiver = new VideoPhoneReceiver();
        }

        registerVideoPhoneReceiver(videoPhoneReceiver);

        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        Log.d(TAG, "MediaCenterService --> onBind()");

        /**
         * 判断视频电话是否处于通话状态，如果是，则屏蔽Sender端发送过来的所有消息
         */
        if (isVideoPhoneCalling())
        {
            Log.d(TAG, "The video phone is calling, discard all messages to the player!");
            return null;
        }

        if ("com.rockchips.mediacenter.dlna.MediaCenterService.SenderService".equals(intent.getAction()))
        {
            Log.d(TAG, "MediaCenterService.onBind() --> mSenderMessenger.getBinder()");
            return mSenderMessenger.getBinder();
        }

        if ("com.rockchips.mediacenter.dlna.MediaCenterService.PlayerService".equals(intent.getAction()))
        {
            Log.d(TAG, "MediaCenterService.onBind() --> mPlayerMessenger.getBinder()");
            return mPlayerMessenger.getBinder();
        }
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        Log.d(TAG, "MediaCenterService --> onUnbind()");
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent)
    {
        Log.d(TAG, "MediaCenterService --> onRebind()");
        super.onRebind(intent);
    }

    @Override
    public void onLowMemory()
    {
        Log.d(TAG, "MediaCenterService --> onLowMemory()");
        super.onLowMemory();
    }

    @Override
    public void onDestroy()
    {
        Log.d(TAG, "MediaCenterService --> onDestroy()");

        if (null != videoPhoneReceiver)
        {
            unregisterVideoPhoneReceiver(videoPhoneReceiver);
        }

        if (mSenderHandlerThread != null)
        {
            mSenderHandlerThread.getLooper().quit();
            mSenderHandlerThread = null;
        }

        if (mPlayerHandlerThread != null)
        {
            mPlayerHandlerThread.getLooper().quit();
            mPlayerHandlerThread = null;
        }

        super.onDestroy();
    }

    /**
     * 执行Sender端发送过来的消息处理
     * 
     * SenderHandler
     * 
     * 2011-10-17 上午11:43:35
     * 
     * @version 1.0.0
     * 
     */
    private class SenderHandler extends Handler
    {
        public SenderHandler(Looper looper)
        {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg)
        {
            /**
             * 判断视频电话是否处于通话状态，如果是，则屏蔽Sender端发送过来的所有消息
             */
            if (isVideoPhoneCalling())
            {
                Log.d(TAG, "The video phone is calling, discard all messages to the player!");
                return;
            }

            Intent intent = null;
            String strUniqueIndication = null;
            ArrayList<Bundle> mediaBaseList = null;
            LocalMediaInfo mediaFileInfo = null;
            Messenger msger = null;
            switch (msg.what)
            {
                case Constant.MCSMessage.MSG_REGISTER_CALLBACK:

                    Log.d(TAG, "SenderHandler --> SENDER_MSG_REGISTER_CALLBACK");

                    intent = (Intent) msg.obj;
                    strUniqueIndication = intent.getStringExtra(Constant.IntentKey.UNIQ);

                    Log.d(TAG, "strUniqueIndication:" + strUniqueIndication);

                    mSenderCallbacks.put(strUniqueIndication, msg.replyTo);

                    Log.d(TAG, "mSenderCallbacks.put(), strUniqueIndication=" + strUniqueIndication + ", msg.replyTo=" + msg.replyTo.toString());
                    break;

                case Constant.MCSMessage.MSG_UNREGISTER_CALLBACK:

                    Log.d(TAG, "SenderHandler --> SENDER_MSG_UNREGISTER_CALLBACK");

                    intent = (Intent) msg.obj;
                    strUniqueIndication = intent.getStringExtra(Constant.IntentKey.UNIQ);

                    Log.d(TAG, strUniqueIndication);
                    mSenderCallbacks.remove(strUniqueIndication);
                    break;
                case Constant.MCSMessage.MSG_SET_MEDIA_DATA:

                    // DTS2012030105161:发送系统广播消息Intent.ACTION_CLOSE_SYSTEM_DIALOGS，取消系统的提示框
                    Log.d(TAG, "SenderHandler --> MSG_SET_MEDIA_DATA --> Send Broadcast to close the system dialogs");
                    Intent intentCloseSysDia = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                    MediaCenterService.this.getApplicationContext().sendBroadcast(intentCloseSysDia, Constant.MEDIACENTER_PERMISSION);
//                            ,                            Constant.BROADCAST_PERMISSION_MOUNT_UNMOUNT_FILESYSTEMS);

                    // 清空Sender端发送过来的消息缓存记录
                    senderMsgQueue.clear();
                    MediaInfo mediaInfo = new MediaInfo();
                    Log.d(TAG, "SenderHandler --> MSG_SET_MEDIA_DATA");

                    // 获取唯一标识
                    intent = (Intent) msg.obj;
                    strUniqueIndication = intent.getStringExtra(Constant.IntentKey.UNIQ);
                    mediaInfo.setUniq(strUniqueIndication);
                    Log.d(TAG, "strUniqueIndication = [" + strUniqueIndication + "]");

                    // 获取媒体播放列表
                    mediaBaseList = intent.getParcelableArrayListExtra(Constant.IntentKey.MEDIA_INFO_LIST);

                    mediaFileInfo = new LocalMediaInfo();
                    mediaFileInfo.decompress(mediaBaseList.get(0));

                    // 设置当前播放类型
                    mediaInfo.setPlayerType(mediaFileInfo.getmFileType());

                    Log.d(TAG, "SenderHandler --> MSG_SET_MEDIA_DATA --> MediaType = [" + mediaFileInfo.getmFileType() + "]");

                    // 获取当前需要播放的媒体文件在媒体列表中的索引
                    int currentIndex = intent.getIntExtra(Constant.IntentKey.CURRENT_INDEX, 0);
                    boolean isMirrorOn = intent.getBooleanExtra(Constant.IntentKey.IS_MIRROR_ON, false);
                    if (currentIndex < 0)
                    {
                        currentIndex = 0;
                    }

                    if (mMediaInfo == null)
                    {
                        // 第一次播放，启动播放
                        Log.d(TAG, "SenderHandler --> MSG_SET_MEDIA_DATA - First Start Player Activity");
                        mMediaInfo = mediaInfo;
                        startPlayerActivity(mediaInfo.getPlayerType(), mMediaInfo.getUniq(), mediaBaseList, currentIndex, isMirrorOn);
                        break;
                    }

                    // 当前播放的Sender是否为请求sender
                    if (mMediaInfo.getUniq().equalsIgnoreCase(mediaInfo.getUniq()))
                    {
                        // 同一个Sender
                        Log.d(TAG, "SenderHandler --> MSG_SET_MEDIA_DATA - The Same Sender");
                        if (mMediaInfo.getPlayerType() != mediaInfo.getPlayerType())
                        {
                            // 不同播放器
                            Log.d(TAG, "SenderHandler --> MSG_SET_MEDIA_DATA - The Same Sender And different Player");

                            // 发送Stop消息给Sender端
                            msger = mSenderCallbacks.get(mMediaInfo.getUniq());
                            if (msger != null)
                            {
                                Message msgSenderStop = Message.obtain();
                                msgSenderStop.what = Constant.MCSMessage.MSG_STOP;
                                Intent intentSenderStop = new Intent();
                                intentSenderStop.putExtra(Constant.IntentKey.UNIQ, mMediaInfo.getPlayerType());
                                intentSenderStop.putExtra(Constant.IntentKey.SENDER_UNIQ_PLAYER_TO_MCS, mMediaInfo.getUniq());

                                msgSenderStop.obj = intentSenderStop;
                                if (!sendMsgToSender(msgSenderStop))
                                {
                                    Log.e(TAG, "Notify previous sender stop failed!");
                                }
                            }

                            // 发送stop消息给Player端
                            msger = mPlayerCallbacks.get(String.valueOf(mMediaInfo.getPlayerType()));
                            if (msger != null)
                            {
                                Message msgPlayerStop = Message.obtain();
                                msgPlayerStop.what = Constant.MCSMessage.MSG_STOP;
                                Intent intentPlayerStop = new Intent();
                                intentPlayerStop.putExtra(Constant.IntentKey.UNIQ, mMediaInfo.getUniq());
                                msgPlayerStop.obj = intentPlayerStop;
                                if (!sendMsgToPlayer(msgPlayerStop))
                                {
                                    Log.e(TAG, "Notify previous player stop failed!");
                                }
                            }

                            // 根据播放器类型启动播放器
                            mMediaInfo = mediaInfo;
                            startPlayerActivity(mediaInfo.getPlayerType(), mMediaInfo.getUniq(), mediaBaseList, currentIndex, isMirrorOn);
                        }
                        else
                        {
                            // 同一播放器
                            Log.d(TAG, "SenderHandler --> MSG_SET_MEDIA_DATA - The Same Sender And Player");
                            mMediaInfo = mediaInfo;
                            msger = mPlayerCallbacks.get(String.valueOf(mMediaInfo.getPlayerType()));

                            // 如果播放器处于后台，关闭后台的播放器，重新启动新的播放器
                            if (isPlayerInBackground(mMediaInfo.getPlayerType()))
                            {
                                Log.d(TAG, "Close backgroud player and start new player");

                                // 发送stop给后台播放器
                                if (mMediaInfo.getPlayerType() == Constant.MediaType.VIDEO && msger != null)
                                {
                                    Message msgPlayerStop = Message.obtain();
                                    msgPlayerStop.what = Constant.MCSMessage.MSG_STOP;
                                    Intent intentPlayerStop = new Intent();
                                    intentPlayerStop.putExtra(Constant.IntentKey.UNIQ, mMediaInfo.getUniq());
                                    msgPlayerStop.obj = intentPlayerStop;
                                    if (!sendMsgToPlayer(msgPlayerStop))
                                    {
                                        Log.e(TAG, "Notify backgroud player stop failed!");
                                    }
                                }

                                // 根据播放器类型启动播放器
                                startPlayerActivity(mediaInfo.getPlayerType(), mMediaInfo.getUniq(), mediaBaseList, currentIndex, isMirrorOn);

                            }
                            else
                            {
                                // 复用之前的播放器
                                Log.d(TAG, "Reuse previous player");

                                if (msger != null)
                                {
                                    Message msgSamePlayer = Message.obtain();
                                    msgSamePlayer.what = Constant.MCSMessage.MSG_SET_MEDIA_DATA;
                                    Intent intentSamePlayer = new Intent();
                                    intentSamePlayer.putParcelableArrayListExtra(Constant.IntentKey.MEDIA_INFO_LIST, mediaBaseList);
                                    intentSamePlayer.putExtra(Constant.IntentKey.CURRENT_INDEX, currentIndex);
                                    intentSamePlayer.putExtra(Constant.IntentKey.UNIQ, mMediaInfo.getUniq());
                                    intentSamePlayer.putExtra(Constant.IntentKey.IS_REUSE_AUDIOPLAYER, true);
                                    msgSamePlayer.obj = intentSamePlayer;

                                    if (!sendMsgToPlayer(msgSamePlayer))
                                    {
                                        Log.e(TAG, "Send MSG_SET_MEDIA_DATA msg to same player failed!");
                                    }
                                }
                                else
                                {
                                    Log.d(TAG, "SenderHandler --> MSG_SET_MEDIA_DATA - Player Callback not Exist");
                                    startPlayerActivity(mMediaInfo.getPlayerType(), mMediaInfo.getUniq(), mediaBaseList, currentIndex, isMirrorOn);
                                }
                            }
                        }
                    }
                    else
                    {
                        // 不同的Sender
                        Log.d(TAG, "SenderHandler --> MSG_SET_MEDIA_DATA - Different Sender");

                        // 通知上一次的Sender停止
                        msger = mSenderCallbacks.get(mMediaInfo.getUniq());
                        if (msger != null)
                        {
                            Log.d(TAG, "SenderHandler --> MSG_SET_MEDIA_DATA - Previous Sender Callback Exist");
                            Log.d(TAG, "SenderHandler --> Previous Sender is:" + mMediaInfo.getUniq());
                            Log.d(TAG, "Send Stop Command to Previous Sender");

                            Message msgSenderStop = Message.obtain();
                            msgSenderStop.what = Constant.MCSMessage.MSG_STOP;

                            Intent intentSenderStop = new Intent();
                            intentSenderStop.putExtra(Constant.IntentKey.UNIQ, mMediaInfo.getPlayerType());
                            intentSenderStop.putExtra(Constant.IntentKey.SENDER_UNIQ_PLAYER_TO_MCS, mMediaInfo.getUniq());
                            msgSenderStop.obj = intentSenderStop;
                            if (!sendMsgToSender(msgSenderStop))
                            {
                                Log.e(TAG, "Notify previous sender stop failed!");
                            }
                        }

                        if (mMediaInfo.getPlayerType() != mediaInfo.getPlayerType())
                        {
                            // 不同播放器
                            Log.d(TAG, "SenderHandler --> MSG_SET_MEDIA_DATA - Different Sender And Player");

                            // 发送stop消息给Player端
                            msger = mPlayerCallbacks.get(String.valueOf(mMediaInfo.getPlayerType()));
                            if (msger != null)
                            {
                                Message msgPlayerStop = Message.obtain();
                                msgPlayerStop.what = Constant.MCSMessage.MSG_STOP;
                                Intent intentPlayerStop = new Intent();
                                intentPlayerStop.putExtra(Constant.IntentKey.UNIQ, mMediaInfo.getUniq());
                                msgPlayerStop.obj = intentPlayerStop;
                                if (!sendMsgToPlayer(msgPlayerStop))
                                {
                                    Log.e(TAG, "Notify previous player stop failed!");
                                }
                            }

                            // 根据播放器类型启动播放器
                            mMediaInfo = mediaInfo;
                            startPlayerActivity(mediaInfo.getPlayerType(), mMediaInfo.getUniq(), mediaBaseList, currentIndex, isMirrorOn);
                        }
                        else
                        {
                            // 同一播放器
                            Log.d(TAG, "SenderHandler --> MSG_SET_MEDIA_DATA - Different Sender And Same Player");
                            Log.d(TAG, "SenderHandler --> Previous Sender is:" + mMediaInfo.getUniq());

                            mMediaInfo = mediaInfo;
                            msger = mPlayerCallbacks.get(String.valueOf(mMediaInfo.getPlayerType()));

                            // 如果播放器处于后台，关闭后台的播放器，重新启动新的播放器
                            if (isPlayerInBackground(mMediaInfo.getPlayerType()))
                            {
                                Log.d(TAG, "Close backgroud player and start new player");

                                // 发送stop给后台播放器
                                if (mMediaInfo.getPlayerType() == Constant.MediaType.VIDEO && msger != null)
                                {
                                    Message msgPlayerStop = Message.obtain();
                                    msgPlayerStop.what = Constant.MCSMessage.MSG_STOP;
                                    Intent intentPlayerStop = new Intent();
                                    intentPlayerStop.putExtra(Constant.IntentKey.UNIQ, mMediaInfo.getUniq());
                                    msgPlayerStop.obj = intentPlayerStop;
                                    if (!sendMsgToPlayer(msgPlayerStop))
                                    {
                                        Log.e(TAG, "Notify backgroud player stop failed!");
                                    }
                                }

                                // 根据播放器类型启动播放器
                                startPlayerActivity(mediaInfo.getPlayerType(), mMediaInfo.getUniq(), mediaBaseList, currentIndex, isMirrorOn);
                            }
                            else
                            {
                                // 复用之前的播放器
                                Log.d(TAG, "Reuse previous player");

                                if (msger != null)
                                {
                                    Message msgSamePlayer = Message.obtain();
                                    msgSamePlayer.what = Constant.MCSMessage.MSG_SET_MEDIA_DATA;
                                    Intent intentSamePlayer = new Intent();
                                    intentSamePlayer.putParcelableArrayListExtra(Constant.IntentKey.MEDIA_INFO_LIST, mediaBaseList);
                                    intentSamePlayer.putExtra(Constant.IntentKey.CURRENT_INDEX, currentIndex);
                                    intentSamePlayer.putExtra(Constant.IntentKey.UNIQ, mMediaInfo.getUniq());
                                    msgSamePlayer.obj = intentSamePlayer;

                                    if (!sendMsgToPlayer(msgSamePlayer))
                                    {
                                        Log.e(TAG, "Send MSG_SET_MEDIA_DATA msg to same player failed!");
                                    }
                                }
                                else
                                {
                                    Log.d(TAG, "SenderHandler --> MSG_SET_MEDIA_DATA - Player Callback not Exist");
                                    startPlayerActivity(mMediaInfo.getPlayerType(), mMediaInfo.getUniq(), mediaBaseList, currentIndex, isMirrorOn);
                                }
                            }
                        }
                    }
                    break;

                case Constant.MCSMessage.MSG_APPEND_MEDIA_DATA:
                    Log.d(TAG, "SenderHandler --> MSG_APPEND_MEDIA_DATA");

                    if (mMediaInfo == null)
                    {
                        Log.e(TAG, "Current mediaInfo not exist");
                        break;
                    }

                    // 获取媒体播放列表
                    intent = (Intent) msg.obj;
                    mediaBaseList = intent.getParcelableArrayListExtra(Constant.IntentKey.MEDIA_INFO_LIST);
                    if (mediaBaseList == null)
                    {
                        Log.e(TAG, "mediaBaseList == null");
                        break;
                    }
                    mediaFileInfo = new LocalMediaInfo();
                    mediaFileInfo.decompress(mediaBaseList.get(0));

                    int mediaType = mediaFileInfo.getmFileType();
                    Log.d(TAG, "The mediaType of append media list is: [" + mediaType + "]");

                    if (mediaType != mMediaInfo.getPlayerType())
                    {
                        Log.e(TAG, "The mediaType of current media list is different from the appended media list!");
                        break;
                    }

                    // 发送该消息给Player端
                    if (!sendMsgToPlayer(msg))
                    {
                        Log.e(TAG, "Send append media list msg to player failed!");
                    }

                    break;

                case Constant.MCSMessage.MSG_DEVICE_DOWN:
                    Log.d(TAG, "SenderHandler --> MSG_DEVICE_DOWN ");

                    if (mMediaInfo == null)
                    {
                        Log.e(TAG, "Current mediaInfo not exist");
                        break;
                    }

                    if (!sendMsgToPlayer(msg))
                    {
                        Log.e(TAG, "Send device down msg to player failed!");
                    }

                    break;
                case Constant.MCSMessage.MSG_PLAY:
                    Log.d(TAG, "SenderHandler --> MSG_PLAY");

                    if (mMediaInfo == null)
                    {
                        Log.e(TAG, "Current mediaInfo not exist");
                        break;
                    }

                    if (!sendMsgToPlayer(msg))
                    {
                        Log.e(TAG, "Send MSG_PLAY to player failed!");
                    }

                    break;
                case Constant.MCSMessage.MSG_PAUSE:
                    Log.d(TAG, "SenderHandler --> MSG_PAUSE");

                    if (mMediaInfo == null)
                    {
                        Log.e(TAG, "Current mediaInfo not exist");
                        break;
                    }

                    if (!sendMsgToPlayer(msg))
                    {
                        Log.e(TAG, "Send MSG_PAUSE to player failed!");
                    }

                    break;
                case Constant.MCSMessage.MSG_SEEK:
                    Log.d(TAG, "SenderHandler --> MSG_SEEK");

                    if (mMediaInfo == null)
                    {
                        Log.e(TAG, "Current mediaInfo not exist");
                        break;
                    }

                    if (!sendMsgToPlayer(msg))
                    {
                        Log.e(TAG, "Send MSG_SEEK to player failed!");
                    }

                    break;

                case Constant.MCSMessage.MSG_STOP:

                    Log.d(TAG, "SenderHandler --> MSG_STOP");

                    if (mMediaInfo == null)
                    {
                        break;
                    }

                    // DTS:DTS2012021300498
                    // 如果是DLNA过来的stop信令，而且当前是图片播放器，丢弃这个stop信令
                    // 自研的软件版本再手机上已经不再发stop信令了，但是windows会发送 所以这里
                    // if (mMediaInfo.getPlayerType() == Constant.MediaType.IMAGE)
                    // {
                    // Log.D(TAG, "DLNA stop command, not close the image player");
                    //
                    // break;
                    // }

                    if (!sendMsgToPlayer(msg))
                    {
                        Log.e(TAG, "Send MSG_STOP to player failed!");
                        break;
                    }

                    // 如果是相同的Sender，回发Stop消息，清除mediaInfo，如果是不同的Sender，Stop命令已被sendMsgToPlayer()阻止
                    // 此处清除mMediaInfo，解决Sender先发Stop再发SetMediaData导致SetMediaData进入到第一个尚未结束的Activity而没有启动第二个Activity
                    intent = (Intent) msg.obj;
                    String strUniq = intent.getStringExtra(Constant.IntentKey.UNIQ);
                    if (mMediaInfo.getUniq().equals(strUniq))
                    {
                        // 回发Stop消息
                        Message msgSenderStop = Message.obtain();
                        msgSenderStop.what = Constant.MCSMessage.MSG_STOP;

                        Intent intentSenderStop = new Intent();
                        intentSenderStop.putExtra(Constant.IntentKey.UNIQ, mMediaInfo.getPlayerType());
                        intentSenderStop.putExtra(Constant.IntentKey.SENDER_UNIQ_PLAYER_TO_MCS, mMediaInfo.getUniq());

                        msgSenderStop.obj = intentSenderStop;
                        if (!sendMsgToSender(msgSenderStop))
                        {
                            Log.e(TAG, "Send MSG_STOP msg to sender failed!");
                        }

                        // 清除信息
                        Log.d(TAG, "mMediaInfo = null !");
                        mMediaInfo = null;
                    }

                    break;
                case Constant.MCSMessage.MSG_ADJUST_VOLUME:
                    Log.d(TAG, "SenderHandler --> MSG_ADJUST_VOLUME");

                    if (mMediaInfo == null)
                    {
                        Log.e(TAG, "Current mediaInfo not exist");
                        break;
                    }

                    if (!sendMsgToPlayer(msg))
                    {
                        Log.e(TAG, "Send MSG_ADJUST_VOLUME to player failed!");
                    }

                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    }

    /**
     * 执行Player端发送过来的消息处理
     * 
     * PlayerHandler
     * 
     * 2011-10-17 上午11:43:44
     * 
     * @version 1.0.0
     * 
     */
    private class PlayerHandler extends Handler
    {
        public PlayerHandler(Looper looper)
        {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg)
        {
            int playerType = Constant.MediaType.UNKNOWN_TYPE;
            Intent intent = null;

            switch (msg.what)
            {
                case Constant.MCSMessage.MSG_REGISTER_CALLBACK:
                    Log.d(TAG, "PlayerHandler --> PLAYER_MSG_REGISTER_CALLBACK");

                    intent = (Intent) msg.obj;
                    playerType = intent.getIntExtra(Constant.IntentKey.UNIQ, Constant.MediaType.UNKNOWN_TYPE);

                    if (playerType == Constant.MediaType.UNKNOWN_TYPE)
                    {
                        Log.d(TAG, "PLAYER_MSG_REGISTER_CALLBACK - playerType is UNKNOWN_TYPE");
                        break;
                    }

                    Log.d(TAG, "PlayerHandler --> PLAYER_MSG_REGISTER_CALLBACK --> playerType:" + playerType);
                    Log.d(TAG, "PlayerHandler --> PLAYER_MSG_REGISTER_CALLBACK --> msg.replyTo:" + msg.replyTo);
                    mPlayerCallbacks.put(String.valueOf(playerType), msg.replyTo);

                    // 此处需要统一处理Sender端发送过来的缓存消息
                    senderMsgQueueProcess(msg.replyTo);
                    break;

                case Constant.MCSMessage.MSG_UNREGISTER_CALLBACK:
                    Log.d(TAG, "PlayerHandler --> PLAYER_MSG_UNREGISTER_CALLBACK");

                    intent = (Intent) msg.obj;
                    playerType = intent.getIntExtra(Constant.IntentKey.UNIQ, Constant.MediaType.UNKNOWN_TYPE);

                    if (playerType == Constant.MediaType.UNKNOWN_TYPE)
                    {
                        Log.e(TAG, "PLAYER_MSG_UNREGISTER_CALLBACK - playerType is UNKNOWN_TYPE");

                        break;
                    }

                    // 从MCS的播放器回调列表中获取对应于该播放器类型的Callback，如果与反注册的Callback相同，则将其删除，否则不删除
                    Messenger callbackMessenger = mPlayerCallbacks.get(String.valueOf(playerType));
                    Log.d(TAG, "callbackMessenger=" + callbackMessenger);
                    Log.d(TAG, "Receive the unregistered callback, msg.replyTo:" + msg.replyTo);

                    if (callbackMessenger != null && callbackMessenger.equals(msg.replyTo))
                    {
                        Log.d(TAG, "Remove the callback form mPlayerCallbacks, msg.replyTo:" + msg.replyTo);

                        mPlayerCallbacks.remove(String.valueOf(playerType));
                    }

                    break;

                case Constant.MCSMessage.MSG_REQUEST_MEDIA_LIST:
                    Log.d(TAG, "PlayerHandler --> MSG_REQUEST_MEDIA_LIST");

                    if (!sendMsgToSender(msg))
                    {
                        Log.e(TAG, "Send MSG_REQUEST_MEDIA_LIST to Sender failed!");
                    }
                    break;

                case Constant.MCSMessage.MSG_REPORT_ERROR:

                    Log.d(TAG, "PlayerHandler --> MSG_REPORT_ERROR");

                    if (!sendMsgToSender(msg))
                    {
                        Log.e(TAG, "Send MSG_REPORT_ERROR to Sender failed!");
                    }
                    break;

                case Constant.MCSMessage.MSG_PLAY:

                    Log.d(TAG, "PlayerHandler --> MSG_PLAY");

                    if (!sendMsgToSender(msg))
                    {
                        Log.e(TAG, "Send MSG_PLAY to Sender failed!");
                    }

                    break;

                case Constant.MCSMessage.MSG_DURATION:

                    Log.d(TAG, "PlayerHandler --> MSG_DURATION");

                    if (!sendMsgToSender(msg))
                    {
                        Log.e(TAG, "Send MSG_DURATION to Sender failed!");
                    }

                    break;

                case Constant.MCSMessage.MSG_PAUSE:

                    Log.d(TAG, "PlayerHandler --> MSG_PAUSE");

                    if (!sendMsgToSender(msg))
                    {
                        Log.e(TAG, "Send MSG_PAUSE to Sender failed!");
                    }

                    break;

                case Constant.MCSMessage.MSG_SEEK:

                    Log.d(TAG, "PlayerHandler --> MSG_SEEK");
                    intent = (Intent) msg.obj;
                    playerType = intent.getIntExtra(Constant.IntentKey.UNIQ, Constant.MediaType.UNKNOWN_TYPE);
                    int seekTo = intent.getIntExtra(Constant.IntentKey.SEEK_POS, -1);
                    Log.d(TAG, "The PlayerType is:" + playerType);
                    Log.d(TAG, "The Seek Position is:" + seekTo);

                    if (!sendMsgToSender(msg))
                    {
                        Log.e(TAG, "Send MSG_SEEK to Sender failed!");
                    }

                    break;

                case Constant.MCSMessage.MSG_STOP:

                    Log.d(TAG, "PlayerHandler --> MSG_STOP");

                    if (!sendMsgToSender(msg))
                    {
                        Log.e(TAG, "Send MSG_STOP to Sender failed!");
                    }

                    break;

                case Constant.MCSMessage.MSG_ADJUST_VOLUME:

                    Log.d(TAG, "PlayerHandler --> MSG_ADJUST_VOLUME");

                    if (!sendMsgToSender(msg))
                    {
                        Log.e(TAG, "Send MSG_ADJUST_VOLUME to Sender failed!");
                    }

                    break;

                default:
                    break;
            }
            super.handleMessage(msg);
        }
    }

    /**
     * 统一处理Sender端发送过来的缓存消息 senderMsgQueueProcess
     * 
     * @param playerCallback void
     * @exception
     */
    private void senderMsgQueueProcess(Messenger playerCallback)
    {
        Log.d(TAG, "msgQueueProcessFunc");
        while (!senderMsgQueue.isEmpty())
        {
            Message msg = senderMsgQueue.remove();
            try
            {
                Thread.sleep(100);

                // 发送给Player端进行处理
                Log.d(TAG, "msg.what:" + msg.what);

                playerCallback.send(msg);
            }
            catch (RemoteException e)
            {
            }
            catch (InterruptedException e)
            {
            }
        }
    }

    private boolean sendMsgToPlayer(Message msg)
    {
        Log.d(TAG, "sendMsgToPlayer");

        try
        {
            /**
             * 判断视频电话是否处于通话状态，如果是，则屏蔽Sender端发送过来的所有消息
             */
            if (isVideoPhoneCalling())
            {
                Log.d(TAG, "The video phone is calling, discard all messages to the player!");
                return false;
            }

            Message message = Message.obtain();
            message.copyFrom(msg);

            Intent intent = (Intent) msg.obj;

            String strUniq = intent.getStringExtra(Constant.IntentKey.UNIQ);

            Log.d(TAG, "sendMsgToPlayer --> strUniq:" + strUniq);
            Log.d(TAG, "sendMsgToPlayer --> mMediaInfo.getUniq():" + mMediaInfo.getUniq());
            if (!strUniq.equals(mMediaInfo.getUniq()))
            {
                Log.e(TAG, "sendMsgToPlayer --> strUniq != mMediaInfo.getUniq()");

                return false;
            }

            Messenger msger = mPlayerCallbacks.get(String.valueOf(mMediaInfo.getPlayerType()));
            Log.d(TAG, "sendMsgToPlayer() --> MediaType = [" + mMediaInfo.getPlayerType() + "]");

            /**
             * 加入消息队列有两种情况：1、Player回调还没有反注册过来；2、Player回调已经反注册过来，但是消息队列不为空
             */
            if ((msger == null) || ((null != msger) && (!senderMsgQueue.isEmpty())))
            {
                Log.d(TAG, "sendMsgToPlayer --> Add to senderMsgQueue Msg.what=" + message.what);
                senderMsgQueue.add(message);
            }
            else
            {
                try
                {
                    Log.d(TAG, "sendMsgToPlayer --> msger.send(message:" + message.what + ")");
                    Log.d(TAG, "sendMsgToPlayer --> msger:" + msger);

                    msger.send(message);
                }
                catch (RemoteException e)
                {
                    Log.e(TAG, "sendMsgToPlayer --> RemoteException occured!");
                    msger = null;
                    mPlayerCallbacks.remove(String.valueOf(mMediaInfo.getPlayerType()));
                    return false;
                }
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, "sendMsgToSender --> Exception occured!");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean sendMsgToSender(Message msg)
    {
        Log.d(TAG, "sendMsgToSender");

        try
        {
            Intent intent = (Intent) msg.obj;
            int playerType = intent.getIntExtra(Constant.IntentKey.UNIQ, Constant.MediaType.UNKNOWN_TYPE);
            String strUniq = intent.getStringExtra(Constant.IntentKey.SENDER_UNIQ_PLAYER_TO_MCS);

            Message message = Message.obtain();
            message.copyFrom(msg);

            if (null == mMediaInfo)
            {
                Log.e(TAG, "sendMsgToSender --> The Current MediaInfo is not existed ");

                return false;
            }

            Log.d(TAG, "sendMsgToSender --> The PlayerType = [" + playerType + "]");
            Log.d(TAG, "sendMsgToSender --> senderClient = [" + strUniq + "]");
            Log.d(TAG, "sendMsgToSender --> mMediaInfo.getUniq():" + mMediaInfo.getUniq());
            Log.d(TAG, "sendMsgToSender --> mMediaInfo.getPlayerType():" + mMediaInfo.getPlayerType());

            if (playerType != mMediaInfo.getPlayerType())
            {
                Log.e(TAG, "sendMsgToSender --> playerType != mMediaInfo.getPlayerType()");
                return false;
            }

            if (strUniq == null || strUniq.trim().equals(""))
            {
                Log.e(TAG, "sendMsgToSender --> strUniq not Exist");

                return false;
            }

            if (!strUniq.equals(mMediaInfo.getUniq()))
            {
                Log.e(TAG, "sendMsgToSender --> strUniq != mMediaInfo.getUniq()");

                return false;
            }

            Messenger msger = mSenderCallbacks.get(mMediaInfo.getUniq());
            if (null == msger)
            {
                Log.e(TAG, "sendMsgToSender --> Can not find the callback in mSenderCallbacks whose Uniq is " + mMediaInfo.getUniq());

                return false;
            }

            Log.d(TAG, "sendMsgToSender --> start send the msg to sender");
            try
            {
                Log.d(TAG, "sendMsgToSender --> msger.send(message:" + message.what + ")");
                Log.d(TAG, "sendMsgToSender --> msger:" + msger);

                msger.send(message);
            }
            catch (RemoteException re)
            {
                Log.e(TAG, "sendMsgToSender --> RemoteException occured!");
                msger = null;
                mSenderCallbacks.remove(mMediaInfo.getUniq());
                re.printStackTrace();
                return false;
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, "sendMsgToSender --> Exception occured!");
            e.printStackTrace();
            return false;
        }
        Log.d(TAG, "sendMsgToSender --> End send the msg to sender");
        return true;
    }

    private class MediaInfo
    {
        // 唯一标示
        private String mUniq = null;

        // 播放器类型
        private int mPlayerType = Constant.MediaType.UNKNOWN_TYPE;

        /**
         * @param mUniq the mUniq to set
         */
        public void setUniq(String mUniq)
        {
            this.mUniq = mUniq;
        }

        /**
         * mUniq
         * 
         * @return the mUniq
         * @since 1.0.0
         */
        public String getUniq()
        {
            return mUniq;
        }

        /**
         * @param mPlayerType the mPlayerType to set
         */
        public void setPlayerType(int mPlayerType)
        {
            this.mPlayerType = mPlayerType;
        }

        /**
         * mPlayerType
         * 
         * @return the mPlayerType
         * @since 1.0.0
         */
        public int getPlayerType()
        {
            return mPlayerType;
        }
    }

    /**
     * 
     * startPlayerActivity 根据类型启动播放器且把播放列表 传入
     * 
     * @param mediaType：媒体类型
     * @param currentIndex：当前需要播放的媒体文件在播放列表中的索引
     * @param mediaBaseList：播放列表
     * @return void
     * @exception
     */
    private void startPlayerActivity(int mediaType, String senderClientUniq, ArrayList<Bundle> mediaBaseList, int currentIndex, boolean isMirrorOn)
    {
        // 根据播放器类型启动不同的播放器Activity
        Log.d(TAG, "startPlayerActivity");
        Log.d(TAG, "mediaType:" + mediaType);
        Intent intent = null;
        switch (mediaType)
        {
            case Constant.MediaType.VIDEO:
                Log.d(TAG, "start VideoPlayerActivity");
                intent = new Intent();
                intent.setClass(this, VideoPlayerActivity.class);
                intent.putExtra(Constant.IntentKey.CURRENT_INDEX, currentIndex);
                intent.putExtra(Constant.IntentKey.UNIQ, senderClientUniq);
                intent.putParcelableArrayListExtra(Constant.IntentKey.MEDIA_INFO_LIST, mediaBaseList);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
                break;
            case Constant.MediaType.AUDIO:
                Log.d(TAG, "start AudioPlayerActivity");                
                intent = new Intent();
                intent.putExtra(Constant.IntentKey.CURRENT_INDEX, currentIndex);
                intent.putExtra(Constant.IntentKey.UNIQ, senderClientUniq);
                intent.putParcelableArrayListExtra(Constant.IntentKey.MEDIA_INFO_LIST, mediaBaseList);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                if (!isMirrorOn)
                {
                    intent.setClass(this, AudioPlayerActivity.class);                    
                    startActivity(intent);
                }
                else
                {
                    intent.setClass(this, AudioPlayerService.class);                    
                    startService(intent);
                }
                break;
            case Constant.MediaType.IMAGE:
                Log.d(TAG, "start ImagePlayerActivity");
                intent = new Intent();
                intent.setClass(this, ImagePlayerActivity.class);
                intent.putExtra(Constant.IntentKey.CURRENT_INDEX, currentIndex);
                intent.putExtra(Constant.IntentKey.UNIQ, senderClientUniq);
                intent.putParcelableArrayListExtra(Constant.IntentKey.MEDIA_INFO_LIST, mediaBaseList);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    // 判断要使用的播放器是否处于后台
    public boolean isPlayerInBackground(int mediaType)
    {
        Log.d(TAG, "isPlayerInBackground --In");
        Log.d(TAG, "isPlayerInBackground --> mediaType:" + mediaType);

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

        Log.d(TAG, "isPlayerInBackground --> componentClassName:" + componentClassName);

        switch (mediaType)
        {
            case Constant.MediaType.VIDEO:
                if (componentClassName.equalsIgnoreCase(STR_VIDEO_CLASS_NAME))
                {
                    Log.d(TAG, "VideoPlayerActivity In Forgroud");

                    return false;
                }
                else
                {
                    Log.d(TAG, "VideoPlayerActivity In Backgroud");

                    return true;
                }

            case Constant.MediaType.AUDIO:
                if (componentClassName.equalsIgnoreCase(STR_AUDIO_CLASS_NAME) || isServiceWorked(activityManager))
                {
                    Log.d(TAG, "AudioPlayerActivity In Forgroud");

                    return false;
                }
                else
                {
                    Log.d(TAG, "AudioPlayerActivity In Backgroud");

                    return true;
                }

            case Constant.MediaType.IMAGE:
                if (componentClassName.equalsIgnoreCase(STR_IMAGE_CLASS_NAME))
                {
                    Log.d(TAG, "ImagePlayerActivity In Forgroud");

                    return false;
                }
                else
                {
                    Log.d(TAG, "ImagePlayerActivity In Backgroud");

                    return true;
                }

            default:
                Log.d(TAG, "Unknown Type, So not in Backgroud");

                return true;
        }
    }

    private void registerVideoPhoneReceiver(BroadcastReceiver videoPhoneReceiver)
    {

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.BroadcastMsg.ACTION_VPSERVICE_CALLING);
        filter.addAction(Constant.BroadcastMsg.ACTION_VPSERVICE_CALLED);
        registerReceiver(videoPhoneReceiver, filter);// , Constant.BROADCAST_PERMISSION_MOUNT_UNMOUNT_FILESYSTEMS, null);
    }

    private void unregisterVideoPhoneReceiver(BroadcastReceiver videoPhoneReceiver)
    {
        unregisterReceiver(videoPhoneReceiver);
    }

    /**
     * 视频电话Receiver
     */
    private class VideoPhoneReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();

            if (Constant.BroadcastMsg.ACTION_VPSERVICE_CALLING.equals(action))
            {
                // 接收到视频电话呼叫事件
                Log.d(TAG, "receive video phone calling");
                bVideoPhoneCalling = true;
            }
            else if (Constant.BroadcastMsg.ACTION_VPSERVICE_CALLED.equals(action))
            {
                // 接收到视频电话挂断事件
                Log.d(TAG, "receive video phone hang up");
                bVideoPhoneCalling = false;
            }
        }
    }

    private boolean isVideoPhoneCalling()
    {
        return bVideoPhoneCalling;
    }
    
    public static boolean isServiceWorked(ActivityManager activityManager)  
    {  
         
        ArrayList<RunningServiceInfo> runningService = (ArrayList<RunningServiceInfo>) activityManager.getRunningServices(30);  
        if (null == runningService || runningService.isEmpty())
        {
            return false;
        }
        for (int i = 0 ; i < runningService.size(); i++)  
        {  
            if (runningService.get(i).service.getClassName().toString().equals("com.rockchips.mediacenter.audioplayer.AudioPlayerService"))  
            {  
                return true;  
            } 
        }  
        return false; 
     }  

}
