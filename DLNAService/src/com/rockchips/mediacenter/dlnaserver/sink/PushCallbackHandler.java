/*
 * 文 件 名:  PushCallbackHandler.java
 * 版    权:  Huawei Technologies Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  y63586
 * 修改时间:  2011-12-22
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.rockchips.mediacenter.dlnaserver.sink;

import com.rockchips.android.airsharing.util.IICLOG;
import com.rockchips.mediacenter.basicutils.constant.Constant;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * 
 * @author  y63586
 * @version  [版本号, 2011-12-22]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class PushCallbackHandler extends Handler
{
    private static final String TAG = "DLNAService:PushCallbackHandler";
    private IICLOG Log = IICLOG.getInstance();
    
    private static final int DLNA_DMR_ACTION_PLAY = 0;
    
    private static final int DLNA_DMR_ACTION_STOP = 1;
    
    private static final int DLNA_DMR_ACTION_PAUSE = 2;
    
    public static int duration = 0;
    
    public static boolean play_begin = false;
    
    public static boolean seek_begin = false; 
    
    public static long seek_begin_time = 0;
    
    public static int position_before_seek = 0;
    
    public static int position = 0;
    
    public PushCallbackHandler(Looper looper)
    {
        super(looper);
    }
    
    @Override
    public void handleMessage(Message msg)
    {
        // 回调过来的消息处理(回调过来的每一条消息都带有播放器类型)
        int playerType = Constant.MediaType.UNKNOWN_TYPE;
        Intent intent = null;
        switch (msg.what)
        {
            case Constant.MCSMessage.MSG_REQUEST_MEDIA_LIST:
                Log.d(TAG, "PushClient received: ---> player request media list");
                
                // 请求播放列表的消息
                intent = (Intent)msg.obj;
                int mediaRequestType =
                    intent.getIntExtra(Constant.IntentKey.MEDIA_REQUEST_TYPE, Constant.MediaRequestType.UNKNOWND_TYPE);
                Log.d(TAG, "PushClient received: ---> mediaRequestType:" + mediaRequestType);
                
                playerType = intent.getIntExtra(Constant.IntentKey.UNIQ, Constant.MediaType.UNKNOWN_TYPE);
                Log.d(TAG, "PushClient received: ---> playerType:" + playerType);                
                break;
            case Constant.MCSMessage.MSG_REPORT_ERROR:
                Log.d(TAG, "PushClient received: ---> player report error");
                
                // 报告播放器出错的消息
                intent = (Intent)msg.obj;
                String strError = intent.getStringExtra(Constant.IntentKey.ERROR_INFO);
                Log.d(TAG, "PushClient received: ---> ErrorInfo:" + strError);
                
                playerType = intent.getIntExtra(Constant.IntentKey.UNIQ, Constant.MediaType.UNKNOWN_TYPE);
                Log.d(TAG, "PushClient received: ---> playerType:" + playerType);
                
                //TODO:....
                
                break;
            case Constant.MCSMessage.MSG_PLAY:
                Log.d(TAG, "PushClient received: ---> player play status");
                
                // 播放器处于播放状态的消息
                intent = (Intent)msg.obj;
                playerType = intent.getIntExtra(Constant.IntentKey.UNIQ, Constant.MediaType.UNKNOWN_TYPE);
                Log.d(TAG, "PushClient received: ---> playerType:" + playerType);
                
                DlnaUniswitch.getInstance(null).onNotifyPlay();
//                DlnaUniswitch.getInstance().DlnaApiDmrSetMediaState(DlnaUniswitch.instanceID, DLNA_DMR_ACTION_PLAY);
                
                break;
            
            case Constant.MCSMessage.MSG_DURATION:
                Log.d(TAG, "PushClient received: ---> media duration");
                
                // 当前播放的媒体文件时长的消息
                intent = (Intent)msg.obj;
                playerType = intent.getIntExtra(Constant.IntentKey.UNIQ, Constant.MediaType.UNKNOWN_TYPE);
                Log.d(TAG, "PushClient received: ---> playerType:" + playerType);
                
                int unknownDuration = -1;
                duration = intent.getIntExtra(Constant.IntentKey.MEDIA_DURATION, unknownDuration);
                Log.d(TAG, "PushClient received: ---> duration:" + duration);
                
            position = intent.getIntExtra(Constant.IntentKey.SEEK_POS, 0);

            DlnaUniswitch.getInstance(null).onSetSeek(position, duration);
                play_begin = true;
                
                break;
            
            case Constant.MCSMessage.MSG_PAUSE:
                Log.d(TAG, "PushClient received: ---> player pause status");
                
                // 播放器暂停播放的消息
                intent = (Intent)msg.obj;
                playerType = intent.getIntExtra(Constant.IntentKey.UNIQ, Constant.MediaType.UNKNOWN_TYPE);
                DlnaUniswitch.getInstance(null).onNotifyPause();
//                DlnaUniswitch.getInstance().DlnaApiDmrSetMediaState(DlnaUniswitch.instanceID, DLNA_DMR_ACTION_PAUSE);
                Log.d(TAG, "PushClient received: ---> playerType:" + playerType);
                break;
            
            case Constant.MCSMessage.MSG_SEEK:
                Log.d(TAG, "PushClient received: ---> player seek status ");
                
                // 当前播放的媒体文件播放位置的消息（一秒钟返回一次播放位置）                
            intent = (Intent)msg.obj;
            int cur_time = intent.getIntExtra(Constant.IntentKey.SEEK_POS, 0);
                Log.d(TAG, "PushClient received: ---> SEEK_POS: cur_time = " + cur_time + ", seek_begin = " + seek_begin);
                
                if (cur_time == 0)
                {
                	play_begin = true;
                	//屏蔽此处：因为会影响DLNA客户端DMR推送断点续播，播放开始后seek的事件又被设置回0，引起从头播放
                	//position = cur_time;
                }
                
                //如果不处于seek模式，直接获取播放器上报的播放位置
                long cur_system_time = System.currentTimeMillis();
                if ((cur_system_time - seek_begin_time) > 1000 && seek_begin)
                {
                	position = cur_time;
                    seek_begin = false;
                    position_before_seek = 0;
                    seek_begin_time = 0;
                }
                
                //解决seek操作时DMC侧进度条抖动问题
                if (seek_begin && play_begin)
                {                                   
                	//快进seek操作
                	if(position_before_seek < position)
                	{
                		if (position <= cur_time)
                		{
                			position = cur_time;
                			position_before_seek = 0;
                            seek_begin = false;
                		}
                		
                	}
                	
                	if(position_before_seek > position)//快退seek操作
                	{
                		if (position >= cur_time)
                		{
                			position = cur_time;
                			position_before_seek = 0;
                            seek_begin = false;
                		}
                	}                	
                }
                else if (play_begin)//非seek模式，直接设置当前播放进度
                {
                	position = cur_time;
                }
               
                playerType = intent.getIntExtra(Constant.IntentKey.UNIQ, Constant.MediaType.UNKNOWN_TYPE);
                Log.d(TAG, "PushClient received: ---> SEEK_POS:" + position + "  ---> playerType:" + playerType);
                
                DlnaUniswitch.getInstance(null).onSetSeek(position, duration);
                
                break;
            case Constant.MCSMessage.MSG_STOP:
                Log.d(TAG, "PushClient received: ---> player stop status");
                
                // 播放器停止播放的消息
                if (PushCallbackHandler.play_begin)
                {
//                    DlnaUniswitch.getInstance().trackMetaData_temp = "";
//                    DlnaUniswitch.getInstance().trackURI_temp = "";
                }
                PushCallbackHandler.play_begin = false;
                PushCallbackHandler.seek_begin = false;

                intent = (Intent)msg.obj;
                playerType = intent.getIntExtra(Constant.IntentKey.UNIQ, Constant.MediaType.UNKNOWN_TYPE);
            String url = intent.getStringExtra(Constant.IntentKey.CURRENT_PLAY_URL);
            DlnaUniswitch.getInstance(null).onNotifyStop(url);
                Log.d(TAG, "PushClient received: ---> playerType:" + playerType);
                
                break;
                
            case Constant.MCSMessage.MSG_ADJUST_VOLUME:
                Log.d(TAG, "PushClient received: ---> player adjust volume");
                
                // 当前播放的媒体文件调节音量的消息
                intent = (Intent)msg.obj;
                playerType = intent.getIntExtra(Constant.IntentKey.UNIQ, Constant.MediaType.UNKNOWN_TYPE);
                Log.d(TAG, "PushClient received: ---> playerType:" + playerType);
                                   
                int volumeAdjustType = intent.getIntExtra(Constant.IntentKey.VOLUME_ADJUST_TYPE, Constant.VolumeAdjustType.ADJUST_UNKNOWND);
                Log.d(TAG, "PushClient received: ---> volumeAdjustType:" + volumeAdjustType);
                
                if(volumeAdjustType == Constant.VolumeAdjustType.ADJUST_UNKNOWND)
                {
                    Log.d(TAG, "PushClient received: ---> volumeAdjustType == Constant.VolumeAdjustType.ADJUST_UNKNOWND");
                    return;
                }
                
                // 音量调节类型为设置为指定的值
                if(volumeAdjustType == Constant.VolumeAdjustType.ADJUST_SET)
                {
                    Log.d(TAG, "PushClient received: ---> volumeAdjustType == Constant.VolumeAdjustType.ADJUST_SET");
                    
                    float volumeValuePercent = intent.getFloatExtra(Constant.IntentKey.VOLUME_SET_VALUE, -1);
                    Log.d(TAG, "PushClient received: ---> volumeValuePercent:" + volumeValuePercent);
                    
                    if(volumeValuePercent >= 0)
                    {
                        Log.d(TAG, "PushClient received: ---> volumeValuePercent >= 0");
                        
                        // 将音量值进行换算后传给协议栈
                        // TODO:....
//                        DlnaUniswitch.getInstance(null).onSetVolmue((int)volumeValue);
                        DlnaUniswitch.getInstance(null).onSetVolmue((int)(volumeValuePercent * 100));
                    }                       
                }
                
                // 其他类型的音量调节
                
                //TODO:....
                
                break;
                
            default:
                break;
        }
        super.handleMessage(msg);
    }
    
}
