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


import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.app.Service;
import android.content.Context;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.WindowManager;
import android.widget.VideoView;
import com.rockchips.mediacenter.utils.IICLOG;
import com.rockchips.mediacenter.utils.PlatformUtils;
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


/**
 * 
 * DLNAVideoView
 * 
 * 2011-12-17 下午07:01:43
 * 
 * @version 1.0.0
 * 
 */
public class OrigVideoView extends VideoView implements IVideoViewAdapter
{
	//private static final String TAG = "OrigVideoView";
    private IICLOG Log = IICLOG.getInstance();
    private WindowManager mWindowManager;

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
    //private Paint mTextPaint;
    OnBufferingUpdateListener onBufferingUpdateListener= null;
    OnErrorListener onErrorListener= null;
    OnCompleteListener onCompleteListener= null;
    
    SurfaceHolder mSH = null;
    
    private int maxWidth;
    private int maxHeight;
    private int  videoOrigWidth;
    private int videoOrigHeight;
            
    /**
     *constructor DLNAVideoView.
     *
     * @param context
     * @param attrs
     * @param defStyle
     */
    public OrigVideoView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);

        init(context);
    }

    /**
     *constructor DLNAVideoView.
     *
     * @param context
     * @param attrs
     */
    public OrigVideoView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context);
    }

    /**
     *constructor DLNAVideoView.
     *
     * @param context
     */
    public OrigVideoView(Context context)
    {
        super(context);
        init(context);
    }
 
    private Context mContext = null;
    private void init(Context context)
    {
    	setOnErrorListener(mOnErrorListener);
    	if(PlatformUtils.getSDKVersion() <= 19)
    		setOnPreparedListener();
        mContext = context;
        
        if(mediaplayer == null)
        {
            mediaplayer =  new OrigMediaPlayerAdapter(context);
        }
        initDisplayViewAttr();       
    }
          
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {  
    	Log.i(TAG, "onMeasure->videoOrigWidth:" + videoOrigWidth);
    	Log.i(TAG, "onMeasure->videoOrigHeight:" + videoOrigHeight);
    	if(android.os.Build.VERSION.SDK_INT < 24){
    	    setMeasuredDimension(videoOrigWidth, videoOrigHeight);
    	}else {
    	    boolean isInPictureMode = ((Activity)mContext).isInPictureInPictureMode();
            //Log.i(TAG, "240dp->px:" + SizeUtils.dp2px(mContext, 240));
            //Log.i(TAG, "135dp->px:" + SizeUtils.dp2px(mContext, 135));
    	    if(isInPictureMode)
                setMeasuredDimension(SizeUtils.dp2px(mContext, 240), SizeUtils.dp2px(mContext, 135));
            else
                setMeasuredDimension(videoOrigWidth, videoOrigHeight);
    	}
    	
    }
    
    
    @Override
    public void setVideoURI(Uri uri) {
    	super.setVideoURI(uri);
    }
    
    
    private void initDisplayViewAttr()
    {
        mWindowManager = (WindowManager)mContext.getSystemService(Service.WINDOW_SERVICE);

        int width = mWindowManager.getDefaultDisplay().getWidth();
        setDisplayW(width);

        int height = mWindowManager.getDefaultDisplay().getHeight();
        int navigationBarHeight = 0;
        int resourceId = mContext.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            navigationBarHeight = mContext.getResources().getDimensionPixelSize(resourceId);
            Log.i(TAG, "onCreate->navigationBarHeight:" + navigationBarHeight);
        }
        height += navigationBarHeight;
        setDisplayH(height);
        
        maxWidth = width;
        maxHeight = height;
        videoOrigWidth = width;
        videoOrigHeight = height;
        
        //        }
        //        
        Log.d(TAG, "onMeasure height :" + height + ", width " + width);
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
    	Log.i(TAG, "start()");
        super.start();
    }

    public void pause()
    {
        super.pause();
      
    }

    @Override
    public void stopPlayback()
    {
        mIsPrepared = false;
        super.stopPlayback();
        mMediaPlayer = null;
        mediaplayer.release();
       
    }

    public void resume() 
    {
        super.resume();
    }


    /**Mender:l00174030;Reason:from android2.2 **/
    public boolean isSeeking = false;

    private final String TAG = "OrigVideoView";

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
        super.setOnInfoListener(mInfoListener);
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

    private boolean mPausedByTransientLossOfFocus = false;

    private OnAudioFocusChangeListener mAudioFocusListener = new OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                    mPausedByTransientLossOfFocus = true;
                    OrigVideoView.super.pause();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    mPausedByTransientLossOfFocus = true;
                    OrigVideoView.super.pause();
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    if (mPausedByTransientLossOfFocus) {
                        mPausedByTransientLossOfFocus = false;
                        OrigVideoView.super.start();
                    }    
                    break;
            }    
            //updatePlayPause();
        }                                                                                                              
    };
        

    
    private List<AudioInfoOfVideo> audioinfos = new ArrayList<AudioInfoOfVideo>();
    private Map<String, Integer> audioTrackMap = new HashMap<String, Integer>();
    private List<SubInfo> subinfos = new ArrayList<SubInfo>();
    
    private boolean mIsPrepared = false;
    private OnPreparedListener mCustomPrepareListener = null;
    
    private OrigMediaPlayerAdapter mediaplayer = null;
    MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener()
    {
        public void onPrepared(MediaPlayer mp)
        {
        	Log.i(TAG, "OrigVideoView->onPrepared");
            mIsPrepared = true;
            mMediaPlayer = mp;
            mediaplayer.setMediaPlayer(mp);
            //setmediaPlayerAdapter(mediaplayer);
            mp.setOnBufferingUpdateListener(mbufferingListener);
            mp.setOnErrorListener(mOnErrorListener);
            mp.setOnCompletionListener(mcompleteListener);
            mp.setOnSeekCompleteListener(mseekListener);
            setSubTrackInfo();
            if(getAudioinfos() == null)
            {
                Log.e(TAG, "get Audio Infos failed, return null");
            }
            if(getAudioinfos().size()>0)
            {
            	maudioInfoOfVidio = getAudioinfos().get(0);
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
        //super.setOnErrorListener(mOnErrorListener);
    }

    private MediaPlayer.OnErrorListener mOnErrorListener = new MediaPlayer.OnErrorListener()
    {
        
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra)
        {
            if(onErrorListener != null)
            {
            	mediaplayer.setMediaPlayer(mp);
                //setmediaPlayerAdapter(mediaplayer);
                onErrorListener.onError(getmediaPlayerAdapter(), what, extra);
            }
            return true;
        }
    };

    @Override
    public void setOutRange(int left, int top, int w, int h)
    {
        if(!isReady())
        {
            Log.e(TAG, "setOutRange isReady fasle!"); 
            return ;
        }
        
//        int i = getmediaPlayerAdapter().setScreenOutRange(left, top, w, h);
//        
        Log.d(TAG, "setOutRange left :" + left + ", top " + top + ", w : " + w + ", h : " + h); 
//        setScreenScale(w, h);        
//        getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);        
        videoOrigWidth = w;
        videoOrigHeight = h;
        this.requestLayout();
    }

    @Override
    public void setSoundId(int id)
    {
        if(!isReady())
        {
            Log.d(TAG, "setSoundId not Ready  just return !");
            return ;
        }
        
        if (0 > id || id >= audioinfos.size())
        {
            Log.e(TAG, "setSoundId invalid id " + id);
            return;
        }
        String language = audioinfos.get(id).getlauguage();
        Integer index = audioTrackMap.get(language);
        Log.d(TAG, "setSoundId id = " + index + ", language = " + language);
        
        maudioInfoOfVidio = audioinfos.get(id);
        int i = getmediaPlayerAdapter().setSoundId(index);
        
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
    
    private AudioInfoOfVideo maudioInfoOfVidio= null;
    public AudioInfoOfVideo getCurrentAudioinfos()
    {
    	return maudioInfoOfVidio;
    }

//    /**
//     * @param 对audioinfos进行赋值
//     */
//    public void setAudioinfos(List<AudioInfoOfVideo> audioinfos)
//    {
//        this.audioinfos = audioinfos;
//        
//    }

    
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
    
    private OnFastForwardCompleteListener mOnFastForwardCompleteListener = null;
    private OnFastBackwordCompleteListener mOnFastBackwordCompleteListener = null;
    @Override
    public void setOnFastForwardCompleteListener(OnFastForwardCompleteListener l)
    {
        mOnFastForwardCompleteListener = l;
    }

    @Override
    public void setOnBackForwardCompleteListener(OnFastBackwordCompleteListener l)
    {
        mOnFastBackwordCompleteListener = l;
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
    public void layout(int l, int t, int r, int b)
    {
    /*	Log.i(TAG, "layout->l:" + l);
    	Log.i(TAG, "layout->t:" + t);
    	Log.i(TAG, "layout->r:" + r);
    	Log.i(TAG, "layout->b:" + b);*/
        int left = (maxWidth - videoOrigWidth) / 2;
        int top = (maxHeight - videoOrigHeight) / 2;
        if(android.os.Build.VERSION.SDK_INT < 24)
            super.layout(left, top, left + videoOrigWidth, top + videoOrigHeight);
        else{
            boolean isInPictureMode = ((Activity)mContext).isInPictureInPictureMode();
            if(isInPictureMode)
                super.layout(0, 0, SizeUtils.dp2px(mContext, 240), SizeUtils.dp2px(mContext, 135));
            else
                super.layout(left, top, left + videoOrigWidth, top + videoOrigHeight);
        }
    }
        
    private void setSubTrackInfo()
    {
        if (mMediaPlayer !=null) 
        {            
            audioinfos.clear();
            audioTrackMap.clear();
            subinfos.clear();
            //DTS2015012804581 by wWX170514  根据caochao问题单中的描述，调用接口函数被人修改，导致问题的出现
            //MediaPlayer.TrackInfo[] trackInfo = mMediaPlayer.getAudioTrack();
            MediaPlayer.TrackInfo[] trackInfo = mMediaPlayer.getTrackInfo();
            int trackType = -1;
            int s = 1;
            String language = null;
            //int subTitleId = 0;
            Log.d(TAG,"trackInfo.length = " + trackInfo.length);
            for (int i = 0; i < trackInfo.length; i++) 
            {
                trackType = trackInfo[i].getTrackType();
                language = trackInfo[i].getLanguage();                 
                Log.d(TAG," trackType = " + trackType);
                if (trackType == MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_AUDIO) 
                {
                    if("und".equals(language))
                    {
                    	language = "音轨"+s;
                    	s++;
                    }
                    Log.d(TAG," sound id = " + i + " language = " + language);
                    AudioInfoOfVideo audioInfo = new AudioInfoOfVideo();
                    audioInfo.setlauguage(language);                    
                    audioinfos.add(audioInfo);
                    audioTrackMap.put(language, i);
                }
                //内置字幕
                else if (trackType == MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT)
                {
                	//这里获取的是字幕信息
                	SubInfo subInfo  = new SubInfo();
                	subInfo.setLanguage(trackInfo[i].getLanguage());
                	subInfo.setFormat(trackInfo[i].getFormat().toString());
                	subInfo.setSubid(i);
                	subInfo.setIsExtra(false);
                	//trackInfo[i].getLanguage();
                	subinfos.add(subInfo);
                }
                //外挂字幕
                else if(trackType == MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_SUBTITLE){
                	//这里获取的是字幕信息
                	SubInfo subInfo  = new SubInfo();
                	subInfo.setLanguage(trackInfo[i].getLanguage());
                	subInfo.setFormat(trackInfo[i].getFormat().toString());
                	subInfo.setSubid(i);
                	subInfo.setIsExtra(true);
                	//trackInfo[i].getLanguage();
                	subinfos.add(subInfo);
                }
                
            }
        }
    }
    
    @Override
    public boolean isDolbyEnabled()
    {
    	return getmediaPlayerAdapter().isDolbyEnabled();
    }

    
    /**
     * 这里通过反射屏蔽VideoView默认Error处理
     */
    public void setErrorListener(){
    	try{
    		Field errorListener = getClass().getSuperclass().getDeclaredField("mErrorListener");
    		errorListener.setAccessible(true);
    		errorListener.set(this, mOnErrorListener);
    	}catch (Exception e){
    		Log.i(TAG, "setErrorListener->exception:" + e);
    	}
    	
    }
    /**
     * 通过反射屏蔽VideoView默认的Prepared处理
     */
    public void setOnPreparedListener(){
    	setFieldValue("mPreparedListener", mDefaultPreparedListener);
    }
    
    public void setFieldValue(String filedName, Object value){
    	try{
    		Field field = getClass().getSuperclass().getDeclaredField(filedName);
    		field.setAccessible(true);
    		field.set(this, value);
    	}catch (Exception e){
    		Log.e(TAG, "setFieldValue->filedName:" + filedName);
    		Log.e(TAG, "setFieldValue->value:" + value);
    		Log.e(TAG, "setFieldValue->exception:" + e);
    	}
    	
    }
    
    public Object getFieldValue(String filedName){
    	try{
    		Field field = getClass().getSuperclass().getDeclaredField(filedName);
    		field.setAccessible(true);
    		return field.get(this);
    	}catch (Exception e){
    		Log.e(TAG, "getFieldValue->filedName:" + filedName);
    		Log.e(TAG, "getFieldValue->exception:" + e);
    	}
    	return null;
    }
    
    public Object invokeMethod(Object object, String methodName, Class<?>[]paramTypes, Object[] values){
    	try{
    		Method method = object.getClass().getDeclaredMethod(methodName, paramTypes);
        	method.setAccessible(true);
        	return method.invoke(object, values);
        	//return method.invoke(object, paramTypes);
    	}catch (Exception e){
    		Log.e(TAG, "invokeMethod->methodName:" + methodName);
    		Log.e(TAG, "invokeMethod->exception:" + e);
    	
    	}
    	return null;
    }
    
    private MediaPlayer.OnPreparedListener mDefaultPreparedListener = new MediaPlayer.OnPreparedListener() {
		
		@Override
		public void onPrepared(MediaPlayer mp) {
			Log.i(TAG, "mDefaultPreparedListener->onPrepared");
			// mCurrentState = STATE_PREPARED;
			setFieldValue("mCurrentState", 2);
			setFieldValue("mCanPause", true);
			setFieldValue("mCanSeekBack", true);
			setFieldValue("mCanSeekForward", true);
			if (getFieldValue("mOnPreparedListener") != null) {
				mPreparedListener.onPrepared((MediaPlayer) getFieldValue("mMediaPlayer"));
			}
			if (getFieldValue("mMediaController") != null)
				invokeMethod(getFieldValue("mMediaController"), "setEnabled", new Class<?>[] { boolean.class }, new Object[] { true });
			setFieldValue("mVideoWidth", mp.getVideoWidth());
			setFieldValue("mVideoHeight", mp.getVideoWidth());
			int seekToPosition = (int) getFieldValue("mSeekWhenPrepared");
			if (seekToPosition != 0) {
				seekTo(seekToPosition);
			}
			int videoWidth = (int) getFieldValue("mVideoWidth");
			int videoHeight = (int) getFieldValue("mVideoHeight");
			if (videoWidth != 0 && videoHeight != 0) {
				getHolder().setFixedSize(videoWidth, videoHeight);
				if ((int) getFieldValue("mSurfaceWidth") == videoWidth
						&& (int) getFieldValue("mSurfaceHeight") == videoHeight) {
					if ((int) getFieldValue("mTargetState") == 3) {
						start();
						if (getFieldValue("mMediaController") != null) {
							invokeMethod(getFieldValue("mMediaController"),
									"show", new Class<?>[0], new Object[0]);
						}
					} else if (!isPlaying()
							&& (seekToPosition != 0 || getCurrentPosition() > 0)) {
						if (getFieldValue("mMediaController") != null) {
							invokeMethod(getFieldValue("mMediaController"),
									"show", new Class<?>[] { int.class },
									new Object[] { 0 });
						}
					}
				}
			} else {
				if ((int) getFieldValue("mTargetState") == 3) {
					start();
				}
			}
		}
	};
        
}
