/**
 * 
 * com.rockchips.iptv.stb.dlna.widget
 * DLNAVideoView.java
 * 
 * 2011-12-17-下午07:01:43
 * Copyright 2011 Huawei Technologies Co., Ltd
 * 
 */
package com.rockchips.mediacenter.view;


import java.util.ArrayList;
import java.util.List;

import com.rockchips.mediacenter.service.IMediaPlayerAdapter;
import com.rockchips.mediacenter.service.IVideoViewAdapter;
import com.rockchips.mediacenter.bean.AudioInfoOfVideo;
import com.rockchips.mediacenter.bean.SubInfo;
import com.rockchips.mediacenter.service.OnBufferingUpdateListener;
import com.rockchips.mediacenter.service.OnCompleteListener;
import com.rockchips.mediacenter.service.OnErrorListener;
import com.rockchips.mediacenter.service.OnFastBackwordCompleteListener;
import com.rockchips.mediacenter.service.OnFastForwardCompleteListener;
import com.rockchips.mediacenter.service.OnInfoListener;
import com.rockchips.mediacenter.service.OnPreparedListener;
import com.rockchips.mediacenter.service.OnSeekCompleteListener;
import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.SurfaceHolder;


/**
 * 
 * DLNAVideoView
 * 
 * 2011-12-17 下午07:01:43
 * 
 * @version 1.0.0
 * 
 */
public class OrigVideoViewNoView extends VideoViewNoViewBase implements IVideoViewAdapter
{


    private int mDisplayW = 0;

    private int mDisplayH = 0;

    private int mSubtitleNumber;

    private int mExtSubtitleNumber;

    private int mAudioTrackNumber;

    private int mSelectSubtitleId = 0;

    private int mSelectAudioTrackId = 0;

    private List <String> mSubtitleLanguageList;

    private List <String> mExtraSubtitleList;

//    private List <String> mAudioTrackLanguageList;

    private List <String> mAudioFormatList;

    private List <String> mAudioSampleRateList;

    private List <String> mAudioChannelList;

    public String[] mSubFormat = {"ASS", "LRC", "SRT", "SMI", "SUB", "TXT", "PGS", "DVB", "DVD"};

    private MediaPlayer  mMediaPlayer = null;

    OnBufferingUpdateListener onBufferingUpdateListener= null;
    OnErrorListener onErrorListener= null;
//    OnInfoListener onInfoListener= null;
//    OnPreparedListener onPreparedListener= null;
//    OnSeekCompleteListener onSeekCompleteListener= null;
    OnCompleteListener onCompleteListener= null;
    
    SurfaceHolder mSH = null;


    /**
     *constructor DLNAVideoView.
     *
     * @param context
     */
    public OrigVideoViewNoView(Context context)
    {
        super(context);
        init(context);
    }
 
    private void init(Context context)
    {
        if(mediaplayer == null)
        {
            mediaplayer =  new OrigMediaPlayerAdapter(context);
        }
    }


    /**
     * @param mDisplayH the mDisplayH to set
     */
    public void setDisplayH(int mDisplayH)
    {
        this.mDisplayH = mDisplayH;
    }

    /**
     * mDisplayH
     *
     * @return  the mDisplayH
     * @since   1.0.0
     */

    public int getDisplayH()
    {
        return mDisplayH;
    }

    /**
     * @param mDisplayW the mDisplayW to set
     */
    public void setDisplayW(int mDisplayW)
    {
        this.mDisplayW = mDisplayW;
    }

    /**
     * mDisplayW
     *
     * @return  the mDisplayW
     * @since   1.0.0
     */

    public int getDisplayW()
    {
        return mDisplayW;
    }



    public void start()
    {
        super.start();
     
    }

    public void pause()
    {
        super.pause();
    }

    public void stopPlayback()
    {
        mIsPrepared = false;
        mMediaPlayer = null;
        
        super.stopPlayback();
    }

    public void resume() 
    {
        super.resume();
    }


    /**Mender:l00174030;Reason:from android2.2 **/
    public boolean isSeeking = false;

    private final String TAG = "DLNAVideoView";

    public void isSeeking(boolean b)
    {
        isSeeking = b;
    }

    public boolean isSeeking()
    {
        return isSeeking;
    }

    private OnInfoListener mOnInfoListener;
    private MediaPlayer.OnInfoListener mInfoListener = new MediaPlayer.OnInfoListener()
    {
        public boolean onInfo(MediaPlayer mp, int what, int extra)
        {
            if (mOnInfoListener != null)
            {
                return mOnInfoListener.onInfo(getmediaPlayerAdapter(), what, extra);
            }
            return true;
        }
    };
    public void setOnInfoListener(OnInfoListener l)
    {
        mOnInfoListener = l;
    }

    private OnSeekCompleteListener mOnSeekCompleteListener;
    OnSeekCompleteListener mSeekCompleteListener = new OnSeekCompleteListener()
    {

        public void onSeekComplete(IMediaPlayerAdapter mp)
        {
            // TODO Auto-generated method stub
            //          mCurrentState = STATE_PLAYBACK_COMPLETED;
            //          mTargetState = STATE_PLAYBACK_COMPLETED;
            Log.e(TAG, "seekTo  is complete---->" + isSeeking);
            //            if (mMediaController != null)
            //            {
            //                mMediaController.hide();
            //            }
            if (mOnSeekCompleteListener != null)
            {
                mOnSeekCompleteListener.onSeekComplete(mp);
            }
            isSeeking(false);
            Log.e(TAG, "seekTo  is complete  posistion---->" + getCurrentPosition());

        }
    };
    private MediaPlayer.OnSeekCompleteListener mseekListener = new MediaPlayer.OnSeekCompleteListener()
    {
        public void onSeekComplete(MediaPlayer arg0)
        {
            if (mOnSeekCompleteListener != null)
            {
                mOnSeekCompleteListener.onSeekComplete(getmediaPlayerAdapter());
            }
        }
    };
    public void setOnSeekCompleteListener(OnSeekCompleteListener l)
    {
        mOnSeekCompleteListener = l;
    }

    private OnCompleteListener mOnCompleteListener;
    private MediaPlayer.OnCompletionListener mcompleteListener = new MediaPlayer.OnCompletionListener()
    {
        public void onCompletion(MediaPlayer arg0)
        {
            if (mOnCompleteListener != null)
            {
                mOnCompleteListener.onCompletion(getmediaPlayerAdapter());
            }
        }
    };
    public void setOnCompletionListener(OnCompleteListener l)
    {
        mOnCompleteListener = l;
    }

    
    
    /**
     * @return 返回 mSubtitleNumber
     */
    public int getSubtitleNumber()
    {
        return mSubtitleNumber;
    }

    /**
     * @param 对mSubtitleNumber进行赋值
     */
    public void setSubtitleNumber(int mSubtitleNumber)
    {
        this.mSubtitleNumber = mSubtitleNumber;
    }

    /**
     * @return 返回 mExtSubtitleNumber
     */
    public int getExtSubtitleNumber()
    {
        return mExtSubtitleNumber;
    }

    /**
     * @param 对mExtSubtitleNumber进行赋值
     */
    public void setExtSubtitleNumber(int mExtSubtitleNumber)
    {
        this.mExtSubtitleNumber = mExtSubtitleNumber;
    }

    /**
     * @return 返回 mAudioTrackNumber
     */
    public int getAudioTrackNumber()
    {
        return mAudioTrackNumber;
    }

    /**
     * @param 对mAudioTrackNumber进行赋值
     */
    public void setAudioTrackNumber(int mAudioTrackNumber)
    {
        this.mAudioTrackNumber = mAudioTrackNumber;
    }

    /**
     * @return 返回 mSelectSubtitleId
     */
    public int getSelectSubtitleId()
    {
        return mSelectSubtitleId;
    }

    /**
     * @param 对mSelectSubtitleId进行赋值
     */
    public void setSelectSubtitleId(int mSelectSubtitleId)
    {
        this.mSelectSubtitleId = mSelectSubtitleId;
    }

    /**
     * @return 返回 mSelectAudioTrackId
     */
    public int getSelectAudioTrackId()
    {
        return mSelectAudioTrackId;
    }

    /**
     * @param 对mSelectAudioTrackId进行赋值
     */
    public void setSelectAudioTrackId(int mSelectAudioTrackId)
    {
        this.mSelectAudioTrackId = mSelectAudioTrackId;
    }

    /**
     * @return 返回 mSubtitleLanguageList
     */
    public List <String> getSubtitleLanguageList()
    {
        return mSubtitleLanguageList;
    }

    /**
     * @param 对mSubtitleLanguageList进行赋值
     */
    public void setSubtitleLanguageList(List <String> mSubtitleLanguageList)
    {
        this.mSubtitleLanguageList = mSubtitleLanguageList;
    }

    /**
     * @return 返回 mExtraSubtitleList
     */
    public List <String> getExtraSubtitleList()
    {
        return mExtraSubtitleList;
    }

    /**
     * @param 对mExtraSubtitleList进行赋值
     */
    public void setExtraSubtitleList(List <String> mExtraSubtitleList)
    {
        this.mExtraSubtitleList = mExtraSubtitleList;
    }

    /**
     * @return 返回 mAudioTrackLanguageList
     */
    public List <String> getAudioTrackLanguageList()
    {
//        return mAudioTrackLanguageList;
        return null;
    }

    /**
     * @return 返回 mAudioFormatList
     */
    public List <String> getAudioFormatList()
    {
        return mAudioFormatList;
    }

    /**
     * @param 对mAudioFormatList进行赋值
     */
    public void setAudioFormatList(List <String> mAudioFormatList)
    {
        this.mAudioFormatList = mAudioFormatList;
    }

    /**
     * @return 返回 mAudioSampleRateList
     */
    public List <String> getAudioSampleRateList()
    {
        return mAudioSampleRateList;
    }

    /**
     * @param 对mAudioSampleRateList进行赋值
     */
    public void setAudioSampleRateList(List <String> mAudioSampleRateList)
    {
        this.mAudioSampleRateList = mAudioSampleRateList;
    }

    /**
     * @return 返回 mAudioChannelList
     */
    public List <String> getAudioChannelList()
    {
        return mAudioChannelList;
    }

    /**
     * @param 对mAudioChannelList进行赋值
     */
    public void setAudioChannelList(List <String> mAudioChannelList)
    {
        this.mAudioChannelList = mAudioChannelList;
    }

    /**end**/
    
    private List<AudioInfoOfVideo> audioinfos = new ArrayList<AudioInfoOfVideo>();
    private List<SubInfo> subinfos = new ArrayList<SubInfo>();
    
    private boolean mIsPrepared = false;
    private OnPreparedListener mCustomPrepareListener = null;
    
    private OrigMediaPlayerAdapter mediaplayer = null;
    MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener()
    {
        public void onPrepared(MediaPlayer mp)
        {
            mIsPrepared = true;
            mMediaPlayer = mp;
            mediaplayer.setMediaPlayer(mp);
            setmediaPlayerAdapter(mediaplayer);
            mp.setOnBufferingUpdateListener(mbufferingListener);
            mp.setOnErrorListener(mOnErrorListener);
            mp.setOnCompletionListener(mcompleteListener);
            mp.setOnSeekCompleteListener(mseekListener);
            mp.setOnInfoListener(mInfoListener);
            //获取音轨 字幕信息
            setAudioinfos(getmediaPlayerAdapter().getAudioInfos());
            if(getAudioinfos() == null)
            {
                Log.e(TAG, "get Audio Infos failed, return null");
            }
            
            int iret = getmediaPlayerAdapter().getSubInfos(subinfos);
            if(iret != 0)
            {
                Log.e(TAG, "get Subinfos failed, return null");
            }
            
                        
            if(mSH != null)
            {
//                mSH.setFixedSize(getVideoWidth(), getVideoHeight());
                A40HisiInvoke.setSubDisplay(mp, mSH);
            }
            mCustomPrepareListener.onPrepared(getmediaPlayerAdapter());
            
        }
    };

    
    public void setOnPreparedListener(OnPreparedListener l)
    {
        mCustomPrepareListener = l;
        super.setOnPreparedListener(mPreparedListener);
    }

    private boolean isReady()
    {
        if(mMediaPlayer != null && mIsPrepared)
        {
            return true;
        }
        
        return false;
    }
    
    @Override
    public int getCurrentSoundId()
    {
        if(!isReady())
        {
            return -1;
        }
        
        return getmediaPlayerAdapter().getCurrentSndId();
    }

    @Override
    public int getCurrentSudId()
    {
        if(!isReady())
        {
            return -1;
        }
        
        return getmediaPlayerAdapter().getCurrentSubId();
    }

    @Override
    public IMediaPlayerAdapter getmediaPlayerAdapter()
    {
        return mediaplayer;
    }

    @Override
    public OnBufferingUpdateListener getonBufferingUpdateListener()
    {
        return onBufferingUpdateListener;
        
    }

    @Override
    public OnErrorListener getonErrorListener()
    {
        return onErrorListener;
    }

    @Override
    public OnInfoListener getonInfoListener()
    {
        return mOnInfoListener;
    }

    @Override
    public OnPreparedListener getonPreparedListener()
    {
        return mCustomPrepareListener;
    }

    @Override
    public OnSeekCompleteListener getonSeekCompleteListener()
    {
        return mOnSeekCompleteListener;
    }

    @Override
    public List<SubInfo> getSubtitleList()
    {
         return subinfos;
    }

    @Override
    public int getVideoHeight()
    {
        if(!isReady())
        {
            return -1;
        }
        
        return getmediaPlayerAdapter().getVideoHeight();
    }

    @Override
    public int getVideoWidth()
    {
        if(!isReady())
        {
            return -1;
        }
        
        return getmediaPlayerAdapter().getVideoWidth();
    }

    @Override
    public boolean isSubtitleShowing()
    {
        if(!isReady())
        {
            return false;
        }
        
        return getmediaPlayerAdapter().isSubtitleShowing();
    }


    @Override
    public void setmediaPlayerAdapter(IMediaPlayerAdapter newVal)
    {
        mediaplayer = (OrigMediaPlayerAdapter)newVal;
    }

    private MediaPlayer.OnBufferingUpdateListener mbufferingListener = new MediaPlayer.OnBufferingUpdateListener()
    {
        @Override
        public void onBufferingUpdate(MediaPlayer arg0, int percent)
        {
            if (onBufferingUpdateListener != null)
            {
                onBufferingUpdateListener.onBufferingUpdate(getmediaPlayerAdapter(), percent);
            }
        }
    };
    @Override
    public void setOnBufferingUpdateListener(OnBufferingUpdateListener newVal)
    {
        onBufferingUpdateListener = newVal;
    }

    @Override
    public void setOnErrorListener(OnErrorListener newVal)
    {
        onErrorListener = newVal;
        super.setOnErrorListener(mOnErrorListener);
    }

    private MediaPlayer.OnErrorListener mOnErrorListener = new MediaPlayer.OnErrorListener()
    {
        
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra)
        {
            if(onErrorListener != null)
            {
                if(getmediaPlayerAdapter() == null)
                {
                    mediaplayer.setMediaPlayer(mp);
                    setmediaPlayerAdapter(mediaplayer);
                }
                
                onErrorListener.onError(getmediaPlayerAdapter(), what, extra);
            }
            return false;
        }
    };
//    
//    private com.hisilicon.android.mediaplayer.HiMediaPlayer.OnFastForwardCompleteListener mFF = new HiMediaPlayer.OnFastForwardCompleteListener()
//    {
//        
//        @Override
//        public void onFastForwardComplete(HiMediaPlayer mp)
//        {
//            if(mOnFastForwardCompleteListener != null)
//            {
//                if(getmediaPlayerAdapter() == null)
//                {
//                    mediaplayer.setmediaPlayer(mp);
//                    setmediaPlayerAdapter(mediaplayer);
//                }
//                
//                mOnFastForwardCompleteListener.onFastForwardComplete(getmediaPlayerAdapter());
//            }
//        }
//
//    };
//    
//    private com.hisilicon.android.mediaplayer.HiMediaPlayer.OnFastBackwordCompleteListener mFB = new HiMediaPlayer.OnFastBackwordCompleteListener()
//    {
//        
//        @Override
//        public void onFastBackwordComplete(HiMediaPlayer mp)
//        {
//            if(mOnFastBackwordCompleteListener != null)
//            {
//                if(getmediaPlayerAdapter() == null)
//                {
//                    mediaplayer.setmediaPlayer(mp);
//                    setmediaPlayerAdapter(mediaplayer);
//                }
//                
//                mOnFastBackwordCompleteListener.onFastBackwordComplete(getmediaPlayerAdapter());
//            }
//        }
//
//    };
//    
//    
    @Override
    public void setOutRange(int left, int top, int w, int h)
    {
        if(!isReady())
        {
            return ;
        }
        
        int i = getmediaPlayerAdapter().setScreenOutRange(left, top, w, h);
        
        Log.d(TAG, "setOutRange return :"+i);
    }

    @Override
    public void setSoundId(int id)
    {
        if(!isReady())
        {
            return ;
        }
        
        int i = getmediaPlayerAdapter().setSoundId(id);
        
        Log.d(TAG, "setSoundId return :"+i);
    }

    @Override
    public void setSubId(int id)
    {
        if(!isReady())
        {
            return ;
        }
        
        int i = getmediaPlayerAdapter().setSubId(id);
        
        Log.d(TAG, "setSubId return :"+i);
    }

    @Override
    public void showSubtitle(boolean show)
    {
        if(!isReady())
        {
            return ;
        }
        
        int flag = show?0:1;
        
        int i = getmediaPlayerAdapter().enableSubtitle(flag);
        
        Log.d(TAG, "showSubtitle return :"+i);
        
    }

    /**
     * @return 返回 audioinfos
     */
    public List<AudioInfoOfVideo> getAudioinfos()
    {
        return audioinfos;
    }

    /**
     * @param 对audioinfos进行赋值
     */
    public void setAudioinfos(List<AudioInfoOfVideo> audioinfos)
    {
        this.audioinfos = audioinfos;
        
    }

    
    /**
     *设置ｓｕｒｆａｃｅ宽高 
     * <功能详细描述>
     * @param w
     * @param h
     * @return 成功　返回０；否则失败
     * @see [类、类#方法、类#成员]
     */
    public int setScreenScale(int w, int h)
    {
        if(!isReady())
        {
            return -1;
        }
        
        return mediaplayer.setScreenScale(w, h);
    }

    @Override
    public void setSubSurfaceHolder(SurfaceHolder sh)
    {
        mSH = sh;
    }
    
    @Override
    public void setOnFastForwardCompleteListener(OnFastForwardCompleteListener l)
    {
    }

    @Override
    public void setOnBackForwardCompleteListener(OnFastBackwordCompleteListener l)
    {
    }

    public int setSpeed(int i)
    {
        if(!isReady())
        {
            return -1;
        }
        
       return mediaplayer.setSpeed(i);
    }

    /* BEGIN: Added by r00178559 for AR-0000698413 2014/02/13 */
    @Override
    public boolean setAudioChannelMode(int channelMode)
    {
        return getmediaPlayerAdapter().setAudioChannelMode(channelMode);
    }
    /* END: Added by r00178559 for AR-0000698413 2014/02/13 */


    /* BEGIN: Added by c00224451 for  AR-0000698413 外挂字幕  2014/2/24 */
    @Override
    public int setSubPath(String path)
    {
        return getmediaPlayerAdapter().setSubPath(path);
    }
    /* END: Added by c00224451 for  AR-0000698413 外挂字幕  2014/2/24 */
    
    @Override
    public int getBufferSizeStatus()
    {
        // TODO Auto-generated method stub
        if(!isReady())
        {
            return -1;
        }
        return mediaplayer.getBufferSizeStatus();
    }

    @Override
    public int getBufferTimeStatus()
    {
        // TODO Auto-generated method stub
        if(!isReady())
        {
            return -1;
        }
        return mediaplayer.getBufferTimeStatus();
    }

	@Override
    public boolean isDolbyEnabled()
    {
    	return getmediaPlayerAdapter().isDolbyEnabled();
    }
	@Override
	public AudioInfoOfVideo getCurrentAudioinfos()
    {
    	return null;
    }
}
