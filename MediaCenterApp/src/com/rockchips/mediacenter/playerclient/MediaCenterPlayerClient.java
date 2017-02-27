/**
 * 
 * com.rockchips.iptv.stb.dlna.player
 * MediaCenterPlayerClient.java
 * 
 * 2011-10-15-下午02:57:06
 * Copyright 2011 Huawei Technologies Co., Ltd
 * 
 */
package com.rockchips.mediacenter.playerclient;

import java.util.LinkedList;
import java.util.Queue;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import com.rockchips.mediacenter.data.ConstData;

/**
 * 
 * MediaCenterPlayerClient
 * 
 * 2011-10-15 下午02:57:06
 * 
 * @author z00184367
 * @version 1.0.0
 * 
 */
public class MediaCenterPlayerClient
{
    private static final String TAG = "MediaCenterApp";
    
    private int mPlayerType = ConstData.MediaType.UNKNOWN_TYPE;
    
    private String mSenderUniq = null;
    
    /**
     * 消息缓存队列，缓存将要发送给MediaCenterService的消息
     */
    private Queue<Message> msgQueue = new LinkedList<Message>();
    
    /**
     * PlayerService端实例
     */
    private Messenger mPlayerService = null;
    
    /**
     * Sender端将要注册的回调
     */
    private Messenger mMessenger = null;
    
    /**
     * 保存传入的应用程序上下文
     */
    private Context mContext = null;
    
    /**
     * 连接监听器，主要是用于连接断开的时候向播放器发送消息，通知播放器停止
     */
    private Handler mConnectListenerHandler = null;
    
    /**
     * 与Service端MeidaCenterPlayerService建立连接
     */
    private ServiceConnection mConnection = new ServiceConnection()
    {
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            Log.d(TAG, "PlayerClient --> onServiceConnected - mPlayerType :" + mPlayerType);
            synchronized (MediaCenterPlayerClient.this)
            {
                mPlayerService = new Messenger(service);
                if (mPlayerType != ConstData.MediaType.UNKNOWN_TYPE && mMessenger != null)
                {
                    try
                    {
                        // 先注册CallBack，然后再处理消息队列
                        Message msg = Message.obtain();
                        msg.what = ConstData.MCSMessage.MSG_REGISTER_CALLBACK;
                        msg.replyTo = mMessenger;
                        
                        Intent intent = new Intent();
                        intent.putExtra(ConstData.IntentKey.UNIQ, mPlayerType);
                        msg.obj = intent;
                        Log.d(TAG, "mSenderService.send(), Msg.what=" + msg.what);
                        mPlayerService.send(msg);
                        
                        // 对队列中的消息进行处理
                        while (!msgQueue.isEmpty())
                        {
                            Message msgTemp = msgQueue.remove();
                            Log.d(TAG, "remove from msgQueue, msgTemp.what=" + msgTemp.what);
                            mPlayerService.send(msgTemp);
                        }
                    }
                    catch (RemoteException e)
                    {
                    }
                }
            }
        }
        
        public void onServiceDisconnected(ComponentName name)
        {
            Log.d(TAG, "PlayerClient --> onServiceDisconnected");
            
            synchronized (MediaCenterPlayerClient.this)
            {
                mPlayerService = null;
                
                if (null != mConnectListenerHandler)
                {
                    Log.d(TAG, "Send disconnect msg to player!");
                    mConnectListenerHandler.sendEmptyMessage(ConstData.ServiceConnectionMSG.MSG_SERVICE_DISCONNECTED);
                    mConnectListenerHandler = null;
                }
            }
        }
    };
    
    /**
     * 
     * doBindService：绑定MediaCenter的播放器服务
     *  @param context 传入上下文
     * @return void
     * @exception
     */
    public void doBindService(Context context)
    {
        Log.d(TAG, "PlayerClient --> doBindService()");
        mContext = context;
        Intent intent = new Intent("com.rockchips.mediacenter.dlna.MediaCenterService.PlayerService");
        context.startService(intent);
        context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        
        Log.d(TAG, "MediaCenterPlayerClient ---> Binded the PlayerService");
    }
    
    /**
     * 
     * isConnected：连接是否已经建立，服务是否得到
     * 
     * @return true if connected, else false
     * @exception
     */
    public boolean isConnected()
    {
        synchronized (this)
        {
            return mPlayerService != null;
        }
    }
    
    /**
     *   
     * doUnbindService：取消与MediaCenter播放器服务的绑定
     *  
     * @return void
     * @exception
     */
    public void doUnbindService()
    {
        Log.d(TAG, "PlayerClient --> doUnbindService()");
        
        synchronized (this)
        {
            if (null != mPlayerService)
            {
                // 取消与PlayerService的绑定
                mContext.unbindService(mConnection);
                
                mPlayerService = null;
                
                Log.d(TAG, "MediaCenterPlayerClient ---> unBinded the PlayerService");
            }
        }
    }
    
    /**
     * 
     * requestMediaList：向Sender端请求媒体列表
     * 
     * @param mediaRequestType：请求类型 --- 所有列表、上一页、下一页、上一个、下一个
     * void
     * @exception
     */
    public void requestMediaList(int mediaRequestType)
    {
        Log.d(TAG, "PlayerClient --> requestMediaList()");
        
        Message msg = Message.obtain();
        msg.what = ConstData.MCSMessage.MSG_REQUEST_MEDIA_LIST;
        
        if (mPlayerType == ConstData.MediaType.UNKNOWN_TYPE)
        {
            Log.e(TAG, "mPlayerType == ConstData.MediaType.UNKNOWN_TYPE");
            return;
        }
        
        if (mediaRequestType == ConstData.MediaRequestType.UNKNOWND_TYPE)
        {
            Log.e(TAG, "mediaRequestType == ConstData.MediaRequestType.UNKNOWND_TYPE");
            return;
        }
        
        if(mSenderUniq == null || mSenderUniq.trim().equals(""))
        {
            Log.e(TAG, "mSenderUniq == null");
            return;
        }
        
        Intent intent = new Intent();
        intent.putExtra(ConstData.IntentKey.UNIQ, mPlayerType);
        intent.putExtra(ConstData.IntentKey.MEDIA_REQUEST_TYPE, mediaRequestType);
        intent.putExtra(ConstData.IntentKey.SENDER_UNIQ_PLAYER_TO_MCS, mSenderUniq);
        
        msg.obj = intent;
        
        sendMsgToMediaCenter(msg);
    }
    
    /**
     * 
     * reportError：报告播放器的错误状态
     * 
     * @param strErrorInfo：错误信息
     * void
     * @exception
     */
    public void reportError(String strErrorInfo)
    {
        Log.d(TAG, "PlayerClient --> reportError()");
        
        Message msg = Message.obtain();
        msg.what = ConstData.MCSMessage.MSG_REPORT_ERROR;
        
        if (mPlayerType == ConstData.MediaType.UNKNOWN_TYPE)
        {
            Log.e(TAG, "mPlayerType == ConstData.MediaType.UNKNOWN_TYPE!");
            return;
        }
        
        if(mSenderUniq == null || mSenderUniq.trim().equals(""))
        {
            Log.e(TAG, "mSenderUniq == null");
            return;
        }
        
        Intent intent = new Intent();
        intent.putExtra(ConstData.IntentKey.UNIQ, mPlayerType);
        intent.putExtra(ConstData.IntentKey.ERROR_INFO, strErrorInfo);
        intent.putExtra(ConstData.IntentKey.SENDER_UNIQ_PLAYER_TO_MCS, mSenderUniq);
        
        msg.obj = intent;
        
        sendMsgToMediaCenter(msg);
    }
    
    /**
     * 
     * reportDuration：通知Sender端，媒体的播放总时长
     * 
     * void
     * @exception
     */
    public void reportDuration(int position, int duration)
    {
        Log.d(TAG, "PlayerClient --> reportDuration()");
        
        Message msg = Message.obtain();
        msg.what = ConstData.MCSMessage.MSG_DURATION;
        
        if (mPlayerType == ConstData.MediaType.UNKNOWN_TYPE)
        {
            Log.e(TAG, "mPlayerType == ConstData.MediaType.UNKNOWN_TYPE!");
            return;
        }
        
        if(mSenderUniq == null || mSenderUniq.trim().equals(""))
        {
            Log.e(TAG, "mSenderUniq == null");
            return;
        }
        
        Intent intent = new Intent();
        intent.putExtra(ConstData.IntentKey.UNIQ, mPlayerType);
        intent.putExtra(ConstData.IntentKey.SEEK_POS, position);
        intent.putExtra(ConstData.IntentKey.MEDIA_DURATION, duration);
        intent.putExtra(ConstData.IntentKey.SENDER_UNIQ_PLAYER_TO_MCS, mSenderUniq);
        
        msg.obj = intent;
        
        sendMsgToMediaCenter(msg);
    }
    
    public void play()
    {
        Log.d(TAG, "PlayerClient --> play()");
        
        Message msg = Message.obtain();
        msg.what = ConstData.MCSMessage.MSG_PLAY;
        
        if (mPlayerType == ConstData.MediaType.UNKNOWN_TYPE)
        {
            Log.e(TAG, "mPlayerType == ConstData.MediaType.UNKNOWN_TYPE!");
            return;
        }
        
        if(mSenderUniq == null || mSenderUniq.trim().equals(""))
        {
            Log.e(TAG, "mSenderUniq == null");
            return;
        }
        
        Intent intent = new Intent();
        intent.putExtra(ConstData.IntentKey.UNIQ, mPlayerType);
        intent.putExtra(ConstData.IntentKey.SENDER_UNIQ_PLAYER_TO_MCS, mSenderUniq);
        
        msg.obj = intent;
        
        sendMsgToMediaCenter(msg);
    }
    
    /**
     * 
     * pause：通知Sender端，播放器已经暂停播放
     * 
     * void
     * @exception
     */
    public void pause()
    {
        Log.d(TAG, "PlayerClient --> pause()");
        
        Message msg = Message.obtain();
        msg.what = ConstData.MCSMessage.MSG_PAUSE;
        
        if (mPlayerType == ConstData.MediaType.UNKNOWN_TYPE)
        {
            Log.e(TAG, "mPlayerType == ConstData.MediaType.UNKNOWN_TYPE!");
            return;
        }
        
        if(mSenderUniq == null || mSenderUniq.trim().equals(""))
        {
            Log.e(TAG, "mSenderUniq == null");
            return;
        }
        
        Intent intent = new Intent();
        intent.putExtra(ConstData.IntentKey.UNIQ, mPlayerType);
        intent.putExtra(ConstData.IntentKey.SENDER_UNIQ_PLAYER_TO_MCS, mSenderUniq);
        
        msg.obj = intent;
        
        sendMsgToMediaCenter(msg);
    }
    
    /**
     * 
     * request list：
     * 
     * void
     * @exception
     */
    public void requestList()
    {
        Log.d(TAG, "PlayerClient --> requestList()");
        
        Message msg = Message.obtain();
        msg.what = ConstData.MCSMessage.MSG_REQUEST_MEDIA_LIST;
        
        if (mPlayerType == ConstData.MediaType.UNKNOWN_TYPE)
        {
            Log.e(TAG, "mPlayerType == ConstData.MediaType.UNKNOWN_TYPE!");
            return;
        }
        
        if(mSenderUniq == null || mSenderUniq.trim().equals(""))
        {
            Log.e(TAG, "mSenderUniq == null");
            return;
        }
        
        Intent intent = new Intent();
        intent.putExtra(ConstData.IntentKey.UNIQ, mPlayerType);
        intent.putExtra(ConstData.IntentKey.SENDER_UNIQ_PLAYER_TO_MCS, mSenderUniq);
        
        msg.obj = intent;
        
        sendMsgToMediaCenter(msg);
    }
    
    /**
     * 
     * seek：通知Sender端，播放器当前的播放位置
     * 
     * @param seekTo：播放位置
     * void
     * @exception
     */
    public void seek(int seekTo)
    {
        Log.d(TAG, "PlayerClient --> seek():" + seekTo);
        
        Message msg = Message.obtain();
        msg.what = ConstData.MCSMessage.MSG_SEEK;
        
        if (mPlayerType == ConstData.MediaType.UNKNOWN_TYPE)
        {
            Log.e(TAG, "mPlayerType == ConstData.MediaType.UNKNOWN_TYPE!");
            return;
        }
        
        if(mSenderUniq == null || mSenderUniq.trim().equals(""))
        {
            Log.e(TAG, "mSenderUniq == null");
            return;
        }
        
        Intent intent = new Intent();
        intent.putExtra(ConstData.IntentKey.UNIQ, mPlayerType);
        intent.putExtra(ConstData.IntentKey.SEEK_POS, seekTo);
        intent.putExtra(ConstData.IntentKey.SENDER_UNIQ_PLAYER_TO_MCS, mSenderUniq);
        
        msg.obj = intent;
        
        sendMsgToMediaCenter(msg);
    }
    
    /**
     * 
     * stop:通知Sender端，播放器已经停止播放
     * 
     * void
     * @exception
     */
    public void stop(String url)
    {
        Log.d(TAG, "PlayerClient --> stop()");
        
        Message msg = Message.obtain();
        msg.what = ConstData.MCSMessage.MSG_STOP;
        
        if (mPlayerType == ConstData.MediaType.UNKNOWN_TYPE)
        {
            Log.e(TAG, "mPlayerType == ConstData.MediaType.UNKNOWN_TYPE!");
            return;
        }
        
        if(mSenderUniq == null || mSenderUniq.trim().equals(""))
        {
            Log.e(TAG, "mSenderUniq == null");
            return;
        }
        
        Intent intent = new Intent();
        intent.putExtra(ConstData.IntentKey.CURRENT_PLAY_URL, url);
        intent.putExtra(ConstData.IntentKey.UNIQ, mPlayerType);
        intent.putExtra(ConstData.IntentKey.SENDER_UNIQ_PLAYER_TO_MCS, mSenderUniq);
        
        msg.obj = intent;
        
        sendMsgToMediaCenter(msg);
    }
    
    /**
     * 
     * adjustVolume
     * 
     * @param volumeAdjustType:音量调节的类型
     *      ConstData.VolumeAdjustType.ADJUST_LOWER:降低音量
     *      ConstData.VolumeAdjustType.ADJUST_SAME:音量不变
     *      ConstData.VolumeAdjustType.ADJUST_RAISE:增加音量
     *      ConstData.VolumeAdjustType.ADJUST_MUTE_ON:开启静音
     *      ConstData.VolumeAdjustType.ADJUST_MUTE_OFF:关闭静音
     *      ConstData.VolumeAdjustType.ADJUST_SET:设置指定的音量
     *      
     * @param volumeValue：对应于音量调节类型的附加参数
     *      当类型为Constant.VolumeAdjustType.ADJUST_SET时，该参数才有意义，其值为要设置的音量值所占最大音量的百分比(0~1)
     *      其余音量调节类型，该参数可指定为-1
     *void
     * @exception
     */
    public void adjustVolume(int volumeAdjustType, float volumeValue)
    {
        Log.d(TAG, "PlayerClient --> adjustVolume()");
        
        Message msg = Message.obtain();
        msg.what = ConstData.MCSMessage.MSG_ADJUST_VOLUME;
        
        if (mPlayerType == ConstData.MediaType.UNKNOWN_TYPE)
        {
            Log.e(TAG, "mPlayerType == ConstData.MediaType.UNKNOWN_TYPE!");
            return;
        }
        
        if (mSenderUniq == null || mSenderUniq.trim().equals(""))
        {
            Log.e(TAG, "mSenderUniq == null");
            return;
        }
        
        Log.d(TAG, "volumeAdjustType:" + volumeAdjustType);
        Log.d(TAG, "volumeValue:" + volumeValue);
        
        Intent intent = new Intent();
        intent.putExtra(ConstData.IntentKey.UNIQ, mPlayerType);
        intent.putExtra(ConstData.IntentKey.VOLUME_ADJUST_TYPE, volumeAdjustType);
        intent.putExtra(ConstData.IntentKey.VOLUME_SET_VALUE, volumeValue);
        intent.putExtra(ConstData.IntentKey.SENDER_UNIQ_PLAYER_TO_MCS, mSenderUniq);
        msg.obj = intent;
        
        sendMsgToMediaCenter(msg);
    }
    
    /**
     * 注册播放器回调，便于MediaCenterService控制播放器的播放状态
     * 
     * registerPlayerCallBack
     * 
     * @param handler：播放器回调
     * @return void
     * @exception
     */
    public void registerPlayerCallBack(Handler handler)
    {
        Log.d(TAG, "PlayerClient --> registerPlayerCallBack 1");
        mMessenger = new Messenger(handler);
    }
    
    /**
     * 
     * 取消播放器回调的注册，便于播放器脱离MediaCenterService的控制
     * 
     * unregisterPlayerCallBack
     * 
     * @return void
     * @exception
     */
    public void unregisterPlayerCallBack()
    {
        Log.d(TAG, "PlayerClient --> unregisterPlayerCallBack()");
        
        Message msg = Message.obtain();
        msg.what = ConstData.MCSMessage.MSG_UNREGISTER_CALLBACK;
        msg.replyTo = mMessenger;
        
        Intent intent = new Intent();
        intent.putExtra(ConstData.IntentKey.UNIQ, mPlayerType);
        msg.obj = intent;
        
        sendMsgToMediaCenter(msg);
    }
    
    /**
     * 
     * sendMsgToMediaCenter：将消息加入消息队列或者发送给MediaCenter
     * 
     * @param msg
     * @return 
     * boolean
     * @exception
     */
    private boolean sendMsgToMediaCenter(Message msg)
    {
        Log.d(TAG, "PlayerClient --> sendMsgToMediaCenter()");
        
        synchronized (this)
        {
            /**
             * 加入消息队列有两种情况：1、连接没有建立完成；2、连接已经建立完成，但是消息队列不为空
             */
            if ((null == mPlayerService) || ((null != mPlayerService) && (!msgQueue.isEmpty())))
            {
                Log.d(TAG, "msgQueue.add(), Msg.what=" + msg.what);
                msgQueue.add(msg);
            }
            else
            {
                Log.d(TAG, "mSenderService.send(), Msg.what=" + msg.what);
                try
                {
                    mPlayerService.send(msg);
                }
                catch (RemoteException e)
                {
                    Log.e(TAG, "get remoteException !");
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * setPlayerType
     * 
     * @param playerType: The player type to be set
     * void
     * @exception
     */
    public void setPlayerType(int playerType)
    {
        this.mPlayerType = playerType;
    }
    
    public void setSenderUniq(String senderUniq)
    {
        this.mSenderUniq = senderUniq;
    }
    
    /**
     * setListner
     * 
     * @param connectListenerHandler 
     * void
     * @exception 
    */
    public void setListener(Handler connectListenerHandler)
    {
        Log.d(TAG, "PlayerClient --> setListener");
        if (connectListenerHandler != null)
        {
            Log.d(TAG, "set the mConnectListenerHandler");
            synchronized (MediaCenterPlayerClient.this)
            {
                mConnectListenerHandler = connectListenerHandler;
            }
        }
    }
}
