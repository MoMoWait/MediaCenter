/**
 * 
 * com.rockchips.iptv.stb.dlna.receiver
 * MediaCenterSenderClient.java
 * 
 * 2011-10-10-下午02:43:04
 * Copyright 2011 Huawei Technologies Co., Ltd
 * 
 */
package com.rockchips.mediacenter.dlnaserver.sink;

import java.util.ArrayList;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;

import com.rockchips.android.airsharing.util.IICLOG;
import com.rockchips.mediacenter.basicutils.constant.Constant;

/**
 * 
 * MediaCenterSenderClient
 * 
 * 2011-10-10 下午02:43:04
 * 
 * @author z00184367
 * @version 1.0.0
 * 
 */
public class MediaCenterSenderClient
{
    private static final String TAG = "MediaCenterSenderClient";
    private IICLOG Log = IICLOG.getInstance();
    
    private ArrayList<MessageWithTime> msgQueue = new ArrayList<MessageWithTime>();
    
    /**
     * SenderService端实例
     */
    private Messenger mSenderService = null;
    
    /**
     * 标示客户端的标志
     */
    private String mUniq = null;
    
    private Messenger mMessenger = null;
    
    private MessageProcessThread mMsgProcessThread = null;
    
    private boolean isCallbackRegistered = false;
    
   /************ 回调应用的对象 *********************************/    
    private Handler mCallback = null;    
    private HandlerThread mCallbackHandlerThread = null;    
    
    public MediaCenterSenderClient(Context context)
    {
        mContext = context;
        bindMcs();
    }
    
    /**
     * 与Service端MeidaCenterSenderService建立连接
     */
    private ServiceConnection mConnection = new ServiceConnection()
    {
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            Log.d(TAG, "SenderClient --> onServiceConnected");
            
            synchronized (this)
            {
                mSenderService = new Messenger(service);
                
                if (mUniq != null && mMessenger != null)
                {
                    // 注册CallBack
                    Message msg = Message.obtain();
                    msg.what = Constant.MCSMessage.MSG_REGISTER_CALLBACK;
                    msg.replyTo = mMessenger;
                    
                    Intent intent = new Intent();
                    intent.putExtra(Constant.IntentKey.UNIQ, mUniq);
                    
                    Log.d(TAG, "mUniq:" + mUniq);
                    msg.obj = intent;
                    
                    if (sendMsgToMediaCenter(msg))
                    {
                        // 设置Callback是否已经注册
                        isCallbackRegistered = true;
                    }
                }
            }
        }
        
        public void onServiceDisconnected(ComponentName name)
        {
            Log.d(TAG, "SenderClient --> onServiceDisconnected");
            
            synchronized (this)
            {
                mSenderService = null;
            }
        }
        
    };
    
    /**
     * 保存传入的应用程序上下文
     */
    private Context mContext = null;
    
    /**
     * 
     * isConected：连接是否已经建立，服务是否得到
     * 
     * @return true if connected, else false
     * @exception
     */
    public boolean isConnected()
    {
        synchronized (this)
        {
            return mSenderService != null;
        }
    }
    
    public boolean isCallbackExist()
    {
        return mMessenger != null;
    }
    
    public boolean isUniqExist()
    {
        return mUniq != null;
    }
    
    private boolean isCallbackRegistered()
    {
        return isCallbackRegistered;
    }
    
    /**
     * 
     * doBindService：绑定推送甩屏的MediaCenter服务
     *  
     * @return void
     * @exception
     */
    public void doBindService()
    {
        Log.d(TAG, "SenderClient --> doBindService");        
        Intent intent = new Intent("com.rockchips.mediacenter.dlna.MediaCenterService.SenderService");
        mContext.startService(intent);
        
        mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        
        // 建立绑定的时候，开启信令累积处理线程
        if (mMsgProcessThread == null)
        {
            //消息处理线程启动
            Log.d(TAG, "start MessageProcessThread");
            mMsgProcessThread = new MessageProcessThread();
            mMsgProcessThread.startThread();
        }
    }
    
    /**
     *   
     * unbindMediaCenterSenderService：取消绑定推送甩屏的AIDL服务
     *  
     * @return void
     * @exception
     */
    public void doUnbindService()
    {
        Log.d(TAG, "SenderClient --> doUnbindService()");
        
        // 由于信令累积延迟了500ms，此处是为了屏蔽解除绑定的时候，消息队列中还有未发送完的消息
        // （主要是MSG_UNREGISTER_CALLBACK消息）
        boolean isMsgQueueEmpty = false;
        do
        {
            synchronized (msgQueue)
            {
                isMsgQueueEmpty = msgQueue.isEmpty();
            }
            if (isMsgQueueEmpty)
            {
                break;
            }
            SystemClock.sleep(500);
        }
        while (true);
        
        synchronized (this)
        {
            if (null != mSenderService)
            {
                // 取消与PlayerService的绑定
                mContext.unbindService(mConnection);
                
                mSenderService = null;
                
                Log.d(TAG, "MediaCenterSenderClient --> unBinded the SenderService");
            }
            
            // 解除绑定的时候，关闭信令累积处理线程
            if (mMsgProcessThread != null)
            {
                mMsgProcessThread.stopThread();
                mMsgProcessThread = null;
            }
        }
    }
    
    public void setUniqueIndication(String uniq)
    {
        Log.d(TAG, "SenderClient --> setUniqueIndication():" + uniq);
        mUniq = uniq;
    }
    
    /**
     * 
     * registerSenderCallBack：注册回调
     * 
     * @param handler 
     * @return void
     * @exception
     */
    public void registerSenderCallBack(Handler handler)
    {
        Log.d(TAG, "SenderClient --> register the Sender CallBack this:" + this);
        
        if (mMessenger == null)
        {
            Log.d(TAG, "SenderClient --> mMessenger == null");
            mMessenger = new Messenger(handler);
            
            Log.d(TAG, "SenderClient --> mMessenger == " + mMessenger);
        }
    }
    
    /**
     * 
     * unregisterSenderCallBack：取消回调的注册
     *  
     * @param handler 
     * @return void
     * @exception
     */
    public void unregisterSenderCallBack(Handler handler)
    {
        Log.d(TAG, "SenderClient --> unregister the Sender CallBack");
        
        Message msg = Message.obtain();
        msg.what = Constant.MCSMessage.MSG_UNREGISTER_CALLBACK;
        msg.replyTo = mMessenger;
        
        Intent intent = new Intent();
        intent.putExtra(Constant.IntentKey.UNIQ, mUniq);
        msg.obj = intent;
        
        pushMessageToQueue(msg);
    }
    
    /**
     * 
     * notifyDeviceDown：通知Player端设备下线
     * 
     * @param deviceId:V下线的设备ID
     * void
     * @exception
     */
    public void notifyDeviceDown(String deviceId)
    {
        Log.d(TAG, "SenderClient --> notifyDeviceDown(), deviceId = [" + deviceId + "]");
        
        Message msg = Message.obtain();
        msg.what = Constant.MCSMessage.MSG_DEVICE_DOWN;
        Intent intent = new Intent();
        
        if (mUniq == null || mUniq.trim().equals(""))
        {
            Log.e(TAG, "SenderClient --> notifyDeviceDown - mUniq == null");
            return;
        }
        
        if (null == deviceId || deviceId.trim().equals(""))
        {
            Log.e(TAG, "SenderClient --> notifyDeviceDown - deviceId == null");
            return;
        }
        
        intent.putExtra(Constant.IntentKey.UNIQ, mUniq);
        intent.putExtra(Constant.IntentKey.DEVICE_ID, deviceId);
        msg.obj = intent;
        
        pushMessageToQueue(msg);
    }
    
    /**
     * 
     * setMediaData：设置推送端、DMS或甩屏端传递过来的媒体信息列表
     * 
     * @param  currentIndex：当前需要播放的媒体文件在媒体列表中的索引
     * @param mediaInfoList：媒体信息列表
     * @return void
     * @exception
     */
    public void setMediaData(ArrayList<Bundle> mediaInfoList, int currentIndex, boolean isMirrorOn)
    {
        Log.d(TAG, "SenderClient --> setMediaData()");
        
        if (mUniq == null)
        {
            Log.e(TAG, "SenderClient --> setMediaData - mUniq == null");
            return;
        }
        
        if (mediaInfoList == null)
        {
            Log.e(TAG, "SenderClient --> setMediaData - mediaInfoList == null");
            return;
        }
        
        if (currentIndex < 0)
        {
            currentIndex = 0;
        }
        
        Message msg = Message.obtain();
        msg.what = Constant.MCSMessage.MSG_SET_MEDIA_DATA;
        Intent intent = new Intent();
        
        intent.putExtra(Constant.IntentKey.IS_MIRROR_ON, isMirrorOn);
        intent.putExtra(Constant.IntentKey.UNIQ, mUniq);
        intent.putExtra(Constant.IntentKey.CURRENT_INDEX, currentIndex);
        intent.putParcelableArrayListExtra(Constant.IntentKey.MEDIA_INFO_LIST, mediaInfoList);
        msg.obj = intent;
        
        pushMessageToQueue(msg);
    }
    
    /**
     * 
     * appendMediaData：向Player端媒体播放列表中添加媒体列表
     * 
     * @param mediaInfoList：需要添加的媒体列表
     * void
     * @exception
     */
    public void appendMediaData(ArrayList<Bundle> mediaInfoList)
    {
        Log.d(TAG, "SenderClient --> appendMediaData()");
        
        Message msg = Message.obtain();
        msg.what = Constant.MCSMessage.MSG_APPEND_MEDIA_DATA;
        Intent intent = new Intent();
        
        if (mUniq == null)
        {
            Log.e(TAG, "appendMediaData -- mUniq == null");
            return;
        }
        
        if (mediaInfoList == null)
        {
            Log.e(TAG, "appendMediaData -- mediaInfoList == null");
            return;
        }
        
        intent.putExtra(Constant.IntentKey.UNIQ, mUniq);
        intent.putParcelableArrayListExtra(Constant.IntentKey.MEDIA_INFO_LIST, mediaInfoList);
        msg.obj = intent;
        
        pushMessageToQueue(msg);
    }
    
    /**
     * 
     * play：播放媒体文件
     * 
     * @return void
     * @exception
     */
    public void play()
    {
        Log.d(TAG, "SenderClient --> play()");
        
        Message msg = Message.obtain();
        msg.what = Constant.MCSMessage.MSG_PLAY;
        Intent intent = new Intent();
        
        if (mUniq == null)
        {
            Log.e(TAG, "play -- mUniq == null");
            return;
        }
        
        intent.putExtra(Constant.IntentKey.UNIQ, mUniq);
        msg.obj = intent;
        
        pushMessageToQueue(msg);
    }
    
    /**
     * 
     * pause：暂停播放操作
     *  
     * @return void
     * @exception
     */
    public void pause()
    {
        Log.d(TAG, "SenderClient --> pause()");
        
        Message msg = Message.obtain();
        msg.what = Constant.MCSMessage.MSG_PAUSE;
        
        Intent intent = new Intent();
        
        if (mUniq == null)
        {
            Log.e(TAG, "pause -- mUniq == null");
            return;
        }
        
        intent.putExtra(Constant.IntentKey.UNIQ, mUniq);
        msg.obj = intent;
        
        pushMessageToQueue(msg);
    }
    
    /**
     * 
     * seek：定位播放操作
     * 
     * @param seekTo：定位播放的位置
     * @return void
     * @exception
     */
    public void seek(int seekTo)
    {
        Log.d(TAG, "SenderClient --> seek(), the seek position = [" + seekTo + "]");
        
        Message msg = Message.obtain();
        msg.what = Constant.MCSMessage.MSG_SEEK;
        
        Intent intent = new Intent();
        
        if (mUniq == null)
        {
            Log.e(TAG, "seek -- mUniq == null");
            return;
        }
        
        intent.putExtra(Constant.IntentKey.UNIQ, mUniq);
        intent.putExtra(Constant.IntentKey.SEEK_POS, seekTo);
        
        msg.obj = intent;
        
        pushMessageToQueue(msg);
    }
    
    /**
     * 
     * stop：停止播放
     *  
     * @return void
     * @exception
     */
    public void stop()
    {
        Log.d(TAG, "SenderClient --> stop()");
        
        Message msg = Message.obtain();
        Intent intent = new Intent();
        msg.what = Constant.MCSMessage.MSG_STOP;
        
        if (mUniq == null)
        {
            Log.e(TAG, "stop -- mUniq == null");
            return;
        }
        
        intent.putExtra(Constant.IntentKey.UNIQ, mUniq);
        msg.obj = intent;
        
        pushMessageToQueue(msg);
    }
    
    /**
     * 
     * adjustVolume
     * 
     * @param volumeAdjustType:音量调节的类型
     *      Constant.VolumeAdjustType.ADJUST_LOWER:降低音量
     *      Constant.VolumeAdjustType.ADJUST_SAME:音量不变
     *      Constant.VolumeAdjustType.ADJUST_RAISE:增加音量
     *      Constant.VolumeAdjustType.ADJUST_MUTE_ON:开启静音
     *      Constant.VolumeAdjustType.ADJUST_MUTE_OFF:关闭静音
     *      Constant.VolumeAdjustType.ADJUST_SET:设置指定的音量
     *      
     * @param volumeValue：对应于音量调节类型的附加参数
     *      当类型为Constant.VolumeAdjustType.ADJUST_SET时，该参数才有意义，其值为要设置的音量值所占最大音量的百分比(0~1)
     *      其余音量调节类型，该参数可指定为-1
     *void
     * @exception
     */
    public void adjustVolume(int volumeAdjustType, float volumeValue)
    {
        Log.d(TAG, "SenderClient --> adjustVolume()");
        
        Message msg = Message.obtain();
        msg.what = Constant.MCSMessage.MSG_ADJUST_VOLUME;
        Intent intent = new Intent();
        
        if (mUniq == null)
        {
            Log.e(TAG, "adjustVolume -- mUniq == null");
            return;
        }
        
        Log.d(TAG, "volumeAdjustType:" + volumeAdjustType);
        Log.d(TAG, "volumeValue:" + volumeValue);
        
        intent.putExtra(Constant.IntentKey.UNIQ, mUniq);
        intent.putExtra(Constant.IntentKey.VOLUME_ADJUST_TYPE, volumeAdjustType);
        intent.putExtra(Constant.IntentKey.VOLUME_SET_VALUE, volumeValue);
        msg.obj = intent;
        
        // 调节音量的命令，直接发送过去，避免调节过卡
        sendMsgToMediaCenter(msg);
    }
    
    /**
     * 
     * pushMessageToQueue：将消息压入队列，解决信令累积问题
     * 
     * @param msg
     *            ：需要压入消息队列的消息
     * 
     * @exception
     */
    public void pushMessageToQueue(Message msg)
    {
        Log.d(TAG, "SenderClient --> pushMessageToQueue:" + msg);
        
        MessageWithTime mwt = new MessageWithTime(msg);
        
        if (msg.what == Constant.MCSMessage.MSG_STOP)
        {
            if (mUniq == null || mUniq.trim().equals(""))
            {
                Log.d(TAG, "SenderClient --> push stop command to queue");
                
                // 如果是stop命令就延时长些
                mwt.setTimeUpInteral(500);
            }
        }
        
        synchronized (msgQueue)
        {
            // 判断是否为SetMediaData，如果是，则清除消息队列，然后将该消息加入队列
            if (msg.what == Constant.MCSMessage.MSG_SET_MEDIA_DATA)
            {
                Log.d(TAG, "SenderClient --> pushMessageToQueue:msgQueue.clear()");
                
                msgQueue.clear();
                
                Log.d(TAG, "SenderClient --> pushMessageToQueue:add the Msg to end of msgQueue --1");
                msgQueue.add(mwt);
                return;
            }
            
            int endIndex = msgQueue.size() - 1;
            if (endIndex >= 0)
            {
                Log.d(TAG, "SenderClient --> pushMessageToQueue:endIndex = " + endIndex);
                
                // 消息队列中存在消息，获取队列尾消息，并与当前消息进行比较
                //如果是append消息就不合并了
                MessageWithTime tempMsg = msgQueue.get(endIndex);
                
                if (msg.what == Constant.MCSMessage.MSG_APPEND_MEDIA_DATA)
                {
                    //append加入到队列中
                    Log.d(TAG, "SenderClient --> pushMessageToQueue, add message:MSG_APPEND_MEDIA_DATA;msg:" + msg.what);
                    
                    msgQueue.add(mwt);
                }
                else if (tempMsg.getMsgType() != msg.what)
                {
                    // 不在队尾部，添加到队尾
                    Log.d(TAG, "SenderClient --> pushMessageToQueue:add the Msg to end of msgQueue --2, msg:"
                        + msg.what);
                    
                    msgQueue.add(mwt);
                }
                else
                {
                    // 在队尾，替换原来的消息（取最新的该消息携带的信息）
                    Log.d(TAG, "SenderClient --> pushMessageToQueue:Replace the Msg at the end of msgQueue --3, msg:"
                        + msg.what);
                    
                    tempMsg.setMsg(msg);
                    tempMsg.setTimeMark(System.currentTimeMillis());
                }
            }
            else
            {
                Log.d(TAG, "SenderClient --> pushMessageToQueue:add the Msg to end of msgQueue --4, msg:" + msg.what);
                
                // 消息队列中不存在任何消息，加入该消息到消息队列
                msgQueue.add(mwt);
            }
        }
    }
    
    /**
     * 
     * 
     * MessageProcessThread：消息处理线程
     * 
     * 2011-10-12 下午04:26:19
     * 
     * @version 1.0.0
     *
     */
    public class MessageProcessThread extends Thread
    {
        private boolean bMessageDispatchRun = false;
        
        @Override
        public void run()
        {
            MessageWithTime msgWithTime = null;
            while (bMessageDispatchRun)
            {
                // 线程每次循环都睡眠100ms
                try
                {
                    // 睡眠100ms
                    Thread.sleep(100);
                }
                catch (Exception e)
                {                    
                }
                
                // 如果还没与Service建立连接，暂时不处理消息队列
                if (!isConnected())
                {                    
                    continue;
                }
                
                // 如果Callback不存在，暂时不处理消息队列
                if (!isCallbackExist())
                {                    
                    continue;
                }
                
                // 如果Callback没有注册给MCS，暂时不处理消息队列
                if (!isCallbackRegistered())
                {                   
                    continue;
                }
                
                synchronized (msgQueue)
                {
                    if (!(msgQueue.isEmpty()))
                    {
                        // 从消息列表中获取第一个消息
                        msgWithTime = msgQueue.get(0);
                    }
                    
                    if (msgWithTime != null && msgWithTime.isTimeUp())
                    {
                        if (sendMsgToMediaCenter(msgWithTime.getMsg()))
                        {                            
                        }
                        
                        // 从消息队列中移除
                        msgQueue.remove(0);
                    }
                }
                
                msgWithTime = null;
            }
            super.run();
        }
        
        public void startThread()
        {
            bMessageDispatchRun = true;
            this.start();
        }
        
        public void stopThread()
        {
            bMessageDispatchRun = false;
        }
    }
    
    /**
     * 带有时间的消息
     * 
     * MessageWithTime
     * 
     * 2011-12-7 上午10:48:01
     * 
     * @version 1.0.0
     *
     */
    public static class MessageWithTime
    {
        
        //记录消息时间
        private long mTickMark = 0;
        
        //消息
        private Message mMsg = null;
        
        //消息超时间隔,单位ms 默认100ms
        private long mTimeUpInterval = 100;
                
        public MessageWithTime(Message msg)
        {
            //该消息进来的时间
            mTickMark = System.currentTimeMillis();
            
            setMsg(msg);
        }
        
        /**
         * 返回消息类型
         * getMsgType
         * 
         * @return 
         *int
         * @exception
         */
        public int getMsgType()
        {
            if (getMsg() != null)
            {
                return getMsg().what;
            }
            
            return Constant.MCSMessage.MSG_MCS_UNKNOWN;
        }
        
        /**
         * 更新时间戳
         * setTimeMark
         * 
         * @param t 
         *void
         * @exception
         */
        public void setTimeMark(long t)
        {
            mTickMark = t;
        }
        
        /**
         * 设置超时时长
         * setTimeUpInteral
         * 
         * @param timeupInterval 
         *void
         * @exception
         */
        public void setTimeUpInteral(long timeupInterval)
        {
            mTimeUpInterval = timeupInterval;
        }
        
        /**
         * 判断是否到时间处理了
         * isTimeUp
         * 
         * @return 
         *boolean
         * @exception
         */
        public boolean isTimeUp()
        {
            if ((System.currentTimeMillis() - mTickMark) >= mTimeUpInterval)
            {
                //如果消息超时了，返回true
                return true;
            }
            
            return false;
        }
             
        @Override
        public int hashCode()
        {
            return super.hashCode();
            
        }
        @Override
        public boolean equals(Object o)
        {
            if (o == null)
            {
                return false;
            }
            
            MessageWithTime mwt = (MessageWithTime)o;
            
            return getMsgType() == mwt.getMsgType();
        }
        
        /**
         * @param mMsg the mMsg to set
         */
        public void setMsg(Message mMsg)
        {
            this.mMsg = mMsg;
        }
        
        /**
         * mMsg
         *
         * @return  the mMsg
         * @since   1.0.0
        */
        
        public Message getMsg()
        {
            return mMsg;
        }
        
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
        Log.d(TAG, "SenderClient --> sendMsgToMediaCenter(), Msg.what=" + msg.what);
        
        synchronized (this)
        {
            try
            {
                mSenderService.send(msg);
            }
            catch (RemoteException e)
            {                
                return false;
            }
        }
        return true;
    }
    
    public void bindMcs()
    {
        /**
         * 注册CallBack、设置客户端唯一标识、建立绑定
         */
        // 创建Callback对象
        if (null == mCallback)
        {
            if (mCallbackHandlerThread == null)
            {
                mCallbackHandlerThread = new HandlerThread("CallbackHandlerThread");
            }
            
            mCallbackHandlerThread.start();
            mCallback = new PushCallbackHandler(mCallbackHandlerThread.getLooper());
        }
        
        // 设置唯一标识（如果协议栈支持多个客户端，如：能区分出不同手机的多手机推送，请将此处修改为设备Id）
        if (!isUniqExist())
        {
            setUniqueIndication(Constant.ClientTypeUniq.PUSH_UNIQ);
        }
        
        // 注册Callback对象
        if (!isCallbackExist())
        {
            registerSenderCallBack(mCallback);
        }
        
        // 与媒体中心建立绑定
        if (!(isConnected()))
        {
            doBindService();
        }
    }
    
    public void unBindMcs()
    {
        Log.d(TAG, "unBindMcs ------");
        
        /**
         * 销毁的时候，解除与媒体中心的绑定
         */
        
        try
        {
            if (isConnected())
            {
                if (mCallback != null)
                {
                    unregisterSenderCallBack(mCallback);
                }
                
                doUnbindService();
            }
        }
        catch (Exception e)
        {
            Log.i(TAG, "senderClient unregiste faild.");
        }

        
        /**
         * 退出回调 Handler的循环处理
         */
        if (mCallback != null)
        {
            mCallback.getLooper().quit();
            mCallback = null;
        }
    }
    
}
