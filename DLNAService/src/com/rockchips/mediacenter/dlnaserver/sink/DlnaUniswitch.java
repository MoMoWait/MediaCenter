/**
 * Title: PlayerSinkListener.java<br>
 * Package: com.rockchips.mediacenter.dlnaserver.sink<br>
 * Description: TODO<br>
 * @author c00224451
 * @version v1.0<br>
 * Date: 2014-8-5下午2:12:57<br> 
 * Copyright © Huawei Technologies Co., Ltd. 2014. All rights reserved.
 */

package com.rockchips.mediacenter.dlnaserver.sink;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.rockchips.android.airsharing.api.EHwMediaInfoType;
import com.rockchips.android.airsharing.api.EHwTransportState;
import com.rockchips.android.airsharing.api.HwMediaInfo;
import com.rockchips.android.airsharing.api.HwMediaPosition;
import com.rockchips.android.airsharing.api.HwServer;
import com.rockchips.android.airsharing.api.IEventListener;
import com.rockchips.android.airsharing.client.PlayerClient;
import com.rockchips.android.airsharing.util.IICLOG;
import com.rockchips.mediacenter.basicutils.bean.LocalMediaInfo;
import com.rockchips.mediacenter.basicutils.constant.Constant;

/**
 * Description: TODO<br>
 * @author c00224451
 * @version v1.0
 * Date: 2014-8-5 下午2:12:57<br>
 */

public class DlnaUniswitch 
{
    private final static String TAG = DlnaUniswitch.class.getSimpleName();
    private IICLOG Log = IICLOG.getInstance();
    
    private static final int MSG_MEDIA_STOP = 1;
    private static final int MSG_MEDIA_PLAY = 2;
    private static final int MSG_MEDIA_PAUSE = 3;
    private static final int MSG_SET_MEDIA_DATA = 4;
    
    private static final int MSG_DELAY_TIME = 100;
    
    private PlayerClient mPlayerClient;
    private Context mContext;    
    private MediaCenterSenderClient mMediaCenterSenderClient;
    
    private HwMediaInfo mMediaInfo;
    private String mPlayerUrl = null;

    private static DlnaUniswitch mStDlnaUniswitch;
    public static DlnaUniswitch getInstance(Context context)
    {
        if (null == mStDlnaUniswitch)
        {
            mStDlnaUniswitch = new DlnaUniswitch(context);
        }
        return mStDlnaUniswitch; 
    }

    private DlnaUniswitch(Context context)     
    {
        mContext = context;
        mPlayerClient = PlayerClient.getInstance();
        mMediaCenterSenderClient = new MediaCenterSenderClient(mContext);
    }
    
    private Handler mHandler = new Handler() 
    {

        @Override
        public void handleMessage(Message msg) 
        {
            switch (msg.what) 
            {
                case MSG_MEDIA_STOP:
//                    onNotifyStop();  
                    mMediaCenterSenderClient.stop();
                    break;
                    
                case MSG_MEDIA_PLAY:
                    onNotifyPlay();
                    mMediaCenterSenderClient.play();
                    break;
                    
                case MSG_MEDIA_PAUSE:
                    onNotifyPause();
                    mMediaCenterSenderClient.pause();
                    break;
                    
                case MSG_SET_MEDIA_DATA:                    
                    setMediaData(isMirrorOn(msg));                    
                    break;
                    
                default:
                    break;
            }
        }
        
    };
    
    private boolean isMirrorOn(Message msg)
    {
        boolean isMirrorOn = false;
        if (msg != null && msg.getData() != null)
        {
            isMirrorOn = msg.getData().getBoolean(Constant.IntentKey.IS_MIRROR_ON);
        }
        return isMirrorOn;
    }

    private boolean stop(String eventType) 
    {
        Log.d(TAG, "stop");
        mHandler.sendEmptyMessage(MSG_MEDIA_STOP);
//        mHandler.sendEmptyMessageDelayed(MSG_MEDIA_STOP, MSG_DELAY_TIME);
        return true;
    }
    
    private boolean setUri(String eventType, boolean isMirrorOn)
    {        
        mHandler.removeMessages(MSG_SET_MEDIA_DATA);
//        mHandler.sendEmptyMessage(MSG_SET_MEDIA_DATA);
        Message msg = mHandler.obtainMessage(MSG_SET_MEDIA_DATA);
        msg.what = MSG_SET_MEDIA_DATA;
        Bundle bundle = new Bundle();
        bundle.putBoolean(Constant.IntentKey.IS_MIRROR_ON, isMirrorOn);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
//        mHandler.sendMessageDelayed(msg, MSG_DELAY_TIME);                
        return true;
    }   
    
    private void setMediaData(boolean isMirrorOn)
    {
        mMediaInfo = mPlayerClient.getMediaInfo();
        if (mMediaInfo != null)
        {
            Log.d(TAG, "setMediaData in meidaInfo url:" + mMediaInfo.getUrl() + " title:" + mMediaInfo.getTitle());
            
            ArrayList<Bundle> bundleList = new ArrayList<Bundle>();
            Bundle bundle = HwMediaInfo2Bundle(mMediaInfo);            
            if (null == bundle)
            {
                return ;
            }
            bundleList.add(bundle);
            mMediaCenterSenderClient.setMediaData(bundleList, 0, isMirrorOn);
        }
        Log.d(TAG, "setMediaData out");
    }
    
    private boolean play(String eventType) 
    {
        Log.d(TAG, "Play");
        mHandler.sendEmptyMessage(MSG_MEDIA_PLAY);
//        mHandler.sendEmptyMessageDelayed(MSG_MEDIA_PLAY, MSG_DELAY_TIME);
        return true;
    }
    
    private boolean pause(String eventType)
    {
        Log.d(TAG, "pause");
        mHandler.sendEmptyMessage(MSG_MEDIA_PAUSE);
//        mHandler.sendEmptyMessageDelayed(MSG_MEDIA_PAUSE, MSG_DELAY_TIME);
        return true;
    }
    
    private boolean setPositionInfo(String eventType) 
    {
        Log.d(TAG, "setPositionInfo");
        int seekTarget = mPlayerClient.getSeekTarget();
        Log.d(TAG, "setPositionInfo seekTarget:" + seekTarget + "ms");
        mMediaCenterSenderClient.seek(seekTarget);
        return true;
    }

    private boolean setVolume(String eventType) 
    {
        int volume = mPlayerClient.getVolume();        
        Log.d(TAG, "SetVolume volume:" + volume);
        float volumePercent = (float) (volume * 0.01); 
        mMediaCenterSenderClient.adjustVolume(Constant.VolumeAdjustType.ADJUST_SET, volumePercent);
        return true;
    }
    
    private boolean setMute(String eventType)
    {
        Log.d(TAG, "SetMute MuteState:" + eventType);
        float volumePercent = 0;
        if (IEventListener.EVENT_TYPE_PLAYER_SET_MUTE_ON.equals(eventType))
        {
            int volume = mPlayerClient.getVolume();        
            volumePercent = (float) (volume * 0.01);
        }
        mMediaCenterSenderClient.adjustVolume(Constant.VolumeAdjustType.ADJUST_SET, volumePercent);
        return true;
    }
    
    
//    private LocalMediaInfo HwMediaInfo2LocalMediaInfo(HwMediaInfo hwMediaInfo)
//    {
//        if (hwMediaInfo == null)
//        {
//            return null;
//        }
//        LocalMediaInfo info = new LocalMediaInfo();
//        info.setmArtist(hwMediaInfo.getArtist());
//        info.setmThumbNail(hwMediaInfo.getIconUri());
//        info.setmFileType(HwMediaType2LocalMediaType(hwMediaInfo.getMediaInfoType()));
//        info.setmMimeType(hwMediaInfo.getMimeType());
//        info.setmFileName(hwMediaInfo.getName());
//        info.setmFileSize((int)hwMediaInfo.getSize());
//        info.setmTitle(hwMediaInfo.getTitle());
//        info.setmResUri(hwMediaInfo.getUrl());
//        info.setmResoulution(hwMediaInfo.getWidth() + "x" + hwMediaInfo.getHeight());
//        info.setmDeviceType(Constant.DeviceType.DEVICE_TYPE_DMS);
//        return info;
//    }
    
    private int HwMediaType2LocalMediaType(EHwMediaInfoType hwType)
    {        
        Log.d(TAG, "cc msg hwType = " + hwType);
        int type = Constant.MediaType.UNKNOWN_TYPE;
        switch (hwType)
        {
            case AUDIO:
                type = Constant.MediaType.AUDIO;
                break;
            case VIDEO:
                type = Constant.MediaType.VIDEO;
                break;
            case IMAGE:
                type = Constant.MediaType.IMAGE;
                break;
            default:
                type = Constant.MediaType.UNKNOWN_TYPE;
                break;
        }
        Log.d(TAG, "cc msg type = " + type);
        return type;
    }
    
    private int HwMimeType2MediaType(String mimeType)
    {
        int type = Constant.MediaType.UNKNOWN_TYPE;
        if (null == mimeType)
        {
            return type;
        }
        if (mimeType.contains("image"))
        {
            type = Constant.MediaType.IMAGE;
        }
        else if (mimeType.contains("audio"))
        {
            type = Constant.MediaType.AUDIO;
        }
        else if (mimeType.contains("video"))
        {
            type = Constant.MediaType.VIDEO;
        }
        Log.d(TAG, "cc HwMimeType2MediaType msg type = " + type);
        return type;
    }
    
    private Bundle HwMediaInfo2Bundle(HwMediaInfo hwMediaInfo)
    {
        Bundle bundle = new Bundle();        
        
        bundle.putString(LocalMediaInfo.FILE_NAME, hwMediaInfo.getTitle());
        int mediaType = HwMediaType2LocalMediaType(hwMediaInfo.getMediaInfoType());
        String mimeType = hwMediaInfo.getMimeType();        
        Log.d(TAG, "cc msg mimetype = " + mimeType);        
        if (Constant.MediaType.UNKNOWN_TYPE == mediaType)
        {
            mediaType = HwMimeType2MediaType(mimeType);
        }
        
        if (Constant.MediaType.UNKNOWN_TYPE == mediaType)
        {
            return null;
        }
        bundle.putInt(LocalMediaInfo.FILE_TYPE, mediaType);
        bundle.putLong(LocalMediaInfo.FILE_SIZE, hwMediaInfo.getSize());
        bundle.putString(LocalMediaInfo.THUMBNAIL, hwMediaInfo.getIconUri());
        bundle.putInt(LocalMediaInfo.DEVICETYPE, Constant.DeviceType.DEVICE_TYPE_DMS);
        bundle.putString(LocalMediaInfo.ARTIST, hwMediaInfo.getArtist());
        bundle.putString(LocalMediaInfo.TITLE, hwMediaInfo.getTitle());        
        bundle.putString(LocalMediaInfo.MIMETYPE, hwMediaInfo.getMimeType());
        mPlayerUrl = hwMediaInfo.getUrl();

        getUrlWithoutParams(mPlayerUrl);
        bundle.putString(LocalMediaInfo.RES_URI, mPlayerUrl);

        bundle.putString(LocalMediaInfo.RESOULUTION, hwMediaInfo.getWidth() + "x" + hwMediaInfo.getHeight());
        
        
        return bundle;
    }
    
    private IEventListener mEventListener = new IEventListener()
    {
        @Override
        public boolean onEvent(int eventId, String eventType)
        {
            // 设置唯一标识（如果协议栈支持多个客户端，如：能区分出不同手机的多手机推送，请将此处修改为设备Id）
            if (null != mMediaCenterSenderClient)
            {
                mMediaCenterSenderClient.bindMcs();
            }
            
            Log.e(TAG, eventId + ":" + eventType);
            boolean isMirrorOn = false;
            if (eventType != null && IEventListener.EVENT_TYPE_PLAYER_SET_MEDIA_INFO_MIRROR_ON.equals(eventType))
            {
                isMirrorOn = true;
            }
            boolean retVal = false;
            switch(eventId)
            {
            case IEventListener.EVENT_ID_NOTIFY_PLAYER_MEDIA_STOP:
                retVal = stop(eventType);
                break;

            case IEventListener.EVENT_ID_NOTIFY_PLAYER_SET_MEDIA_INFO:
                retVal = setUri(eventType, isMirrorOn);
                break;
                
            case IEventListener.EVENT_ID_NOTIFY_PLAYER_MEDIA_PLAY:
                retVal = play(eventType);
                break;
                
            case IEventListener.EVENT_ID_NOTIFY_PLAYER_MEDIA_PAUSE:
                retVal = pause(eventType);
                break;
                
            case IEventListener.EVENT_ID_NOTIFY_PLAYER_MEDIA_POSITION_CHANGED:
                retVal = setPositionInfo(eventType);
                break;
                
            case IEventListener.EVENT_ID_NOTIFY_PLAYER_SET_VOLUME:
                retVal = setVolume(eventType);
                break;
                
            case IEventListener.EVENT_ID_NOTIFY_PLAYER_SET_MUTE:
                retVal = setMute(eventType);
                break;       
                
            default:
                break;
            }
            
            Log.d(TAG, "retVal:" + retVal);
            return retVal;          
        }
        
    };
    public IEventListener getEventListener()
    {
        return mEventListener;
    }
    
    public void onDestroy()
    {
        if (null != mMediaCenterSenderClient)
        {
            mMediaCenterSenderClient.unBindMcs();
        }
    }

    private void getUrlWithoutParams(String url)
    {
        if (null == url)
        {
            return;
        }

        int index = url.indexOf("?formatID");
        if (index != -1)
        {
            mPlayerUrl = url.substring(0, index);
        }
    }

    public void onNotifyStop(String url)
    {
        Log.e(TAG, "onNotifyStop url = " + url + " mPlayerUrl = " + mPlayerUrl);
        if ((url != null) && (mPlayerUrl != null) && url.contains(mPlayerUrl))
        {
            mPlayerClient.notifyTransportStateChanged(EHwTransportState.STOPPED);
        }
    }
    public void onNotifyPause()
    {
        mPlayerClient.notifyTransportStateChanged(EHwTransportState.PAUSED_PLAYBACK);
    }
    public void onNotifyPlay()
    {
        mPlayerClient.notifyTransportStateChanged(EHwTransportState.PLAYING);        
    }
    
    public void onSetSeek(int seekTo, int duration)
    {
        if (null == mMediaInfo)
        {
            return;
        }
        HwMediaPosition positionInfo = new HwMediaPosition();
        positionInfo.setRelTime(millistoTime(seekTo));
        positionInfo.setTrackDur(millistoTime(duration));
        positionInfo.setTrackMetaData(mMediaInfo.getMetaData());
        positionInfo.setTrackURI(mMediaInfo.getUrl());
        mPlayerClient.notifyPositionChanged(positionInfo);
    }
    public void onSetVolmue(int volume)
    {
        mPlayerClient.notifyVolumeChanged(volume);
    }
    
    private String millistoTime(int millis)
    {
        int seconds = millis / 1000;
        int sec = seconds % 60;
        int min = seconds / 60 % 60;
        int hour = seconds / 3600;   
        
        String strHour = String.valueOf(hour);
        if (strHour.length() < 2)
        {
            strHour = "0" + strHour;
        }
        
        String strMin = String.valueOf(min);
        
        if (strMin.length() < 2)
        {
            strMin = "0" + strMin;
        }
        
        String strSec = String.valueOf(sec);
        
        if (strSec.length() < 2)
        {
            strSec = "0" + strSec;
        }
        
        return strHour + ":" + strMin + ":" + strSec;
    }
    
    private int timetoMillis(String time)
    {
        String[] my = time.split(":");
        
        int hour = Integer.parseInt(my[0]);
        
        int min = Integer.parseInt(my[1]);
        
        String[] secs = my[2].split("\\.");
        int sec = 0;
        int totalMillis = 0;
        if (secs.length > 1)
        {
            sec = Integer.parseInt(secs[0]);
            totalMillis = (hour * 3600 + min * 60 + sec) * 1000 + Integer.parseInt(secs[1]);
        }
        else
        {
            sec = Integer.parseInt(my[2]);
            totalMillis = (hour * 3600 + min * 60 + sec) * 1000;
        }
        
        return totalMillis;
    }
}
