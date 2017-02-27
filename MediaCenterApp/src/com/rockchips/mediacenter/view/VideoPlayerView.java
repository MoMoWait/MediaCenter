package com.rockchips.mediacenter.view;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.app.Service;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.WindowManager;
import android.widget.VideoView;
import com.rockchips.mediacenter.service.IVideoPlayer;
import com.rockchips.mediacenter.bean.AudioInfoOfVideo;
import com.rockchips.mediacenter.bean.SubInfo;
import android.util.Log;
/**
 * @author GaoFei
 * 视频播放组件
 */
public class VideoPlayerView extends VideoView {


	//private static final String TAG = "OrigVideoView";
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
    /**Mender:l00174030;Reason:from android2.2 **/
    public boolean isSeeking = false;
    private final String TAG = "OrigVideoView";
    SurfaceHolder mSH = null;
    
    private int maxWidth;
    private int maxHeight;
    private int  videoOrigWidth;
    private int videoOrigHeight;
    private IVideoPlayer mPlayer;
    private AudioInfoOfVideo maudioInfoOfVidio;
    /**
     *constructor DLNAVideoView.
     *
     * @param context
     * @param attrs
     * @param defStyle
     */
    public VideoPlayerView(Context context, AttributeSet attrs, int defStyle)
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
    public VideoPlayerView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context);
    }

    /**
     *constructor DLNAVideoView.
     *
     * @param context
     */
    public VideoPlayerView(Context context)
    {
        super(context);
        init(context);
    }
 
    private Context mContext = null;
    private void init(Context context)
    {
    	setOnErrorListener(mOnErrorListener);
    	setOnPreparedListener(mPreparedListener);
    	setOnCompletionListener(mcompleteListener);
    	setOnInfoListener(mInfoListener);
        mContext = context;
        initDisplayViewAttr();       
    }
          
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {  
        setMeasuredDimension(videoOrigWidth, videoOrigHeight);
        
    }
    
    
    @Override
    public void setVideoURI(Uri uri) {
    	//此时VideoView会调用
    	super.setVideoURI(uri);
    }
    
    /**
     * 设置VideoPlayer接口器
     */
    public void setIVideoPlayer(IVideoPlayer player){
    	mPlayer = player;
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
        }
        height += navigationBarHeight;
        setDisplayH(height);
        maxWidth = width;
        maxHeight = height;
        videoOrigWidth = width;
        videoOrigHeight = height;
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


    public void isSeeking(boolean b)
    {
        isSeeking = b;
    }

    public boolean isSeeking()
    {
        return isSeeking;
    }

    private MediaPlayer.OnInfoListener mInfoListener = new MediaPlayer.OnInfoListener()
    {
        public boolean onInfo(MediaPlayer mp, int what, int extra)
        {
            if (mPlayer != null)
            {
                return mPlayer.onInfo(mp, what, extra);
            }
            return true;
        }
    };
   

    private MediaPlayer.OnSeekCompleteListener mseekListener = new MediaPlayer.OnSeekCompleteListener()
    {
        public void onSeekComplete(MediaPlayer mp)
        {
            if (mPlayer != null)
            {
                mPlayer.onSeekComplete(mp);
                isSeeking(false);
            }
        }
    };

    private MediaPlayer.OnCompletionListener mcompleteListener = new MediaPlayer.OnCompletionListener()
    {
        public void onCompletion(MediaPlayer mp)
        {
            if (mPlayer != null)
            {
                mPlayer.onCompletion(mp);
            }
        }
    };
    
    
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
    private List<AudioInfoOfVideo> audioinfos = new ArrayList<AudioInfoOfVideo>();
    private Map<String, Integer> audioTrackMap = new HashMap<String, Integer>();
    private List<SubInfo> subinfos = new ArrayList<SubInfo>();
    
    private boolean mIsPrepared = false;
    
    MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener()
    {
        public void onPrepared(MediaPlayer mp)
        {
            mIsPrepared = true;
            mMediaPlayer = mp;
            mp.setOnBufferingUpdateListener(mbufferingListener);
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
            
        }
    };

    
    private boolean isReady()
    {
        if(mMediaPlayer != null && mIsPrepared)
        {
            return true;
        }
        
        return false;
    }
    
    
    
    
    public List<SubInfo> getSubtitleList()
    {
         return subinfos;
    }
    

    private MediaPlayer.OnBufferingUpdateListener mbufferingListener = new MediaPlayer.OnBufferingUpdateListener()
    {
        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent)
        {
            if (mPlayer != null)
            {
                mPlayer.onBufferingUpdate(mp, percent);
            }
        }
    };
    
    private MediaPlayer.OnErrorListener mOnErrorListener = new MediaPlayer.OnErrorListener()
    {
        
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra)
        {
            if(mPlayer != null)
            {
                mPlayer.onError(mp, what, extra);
            }
            return true;
        }
    };
    
    public void setOutRange(int left, int top, int w, int h)
    {
        if(!isReady())
        {
            Log.e(TAG, "setOutRange isReady fasle!"); 
            return ;
        }
        
        Log.d(TAG, "setOutRange left :" + left + ", top " + top + ", w : " + w + ", h : " + h); 
        videoOrigWidth = w;
        videoOrigHeight = h;
        this.requestLayout();
    }

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
        
        maudioInfoOfVidio = getAudioinfos().get(id);
        //int i = getmediaPlayerAdapter().setSoundId(index);
        
        //Log.d(TAG, "setSoundId return :"+i);
    }

    public void setSubId(int id)
    {
        if(!isReady())
        {
            return ;
        }
        
        //int i = getmediaPlayerAdapter().setSubId(id);
        
       // Log.d(TAG, "setSubId return :"+i);
    }

    public void showSubtitle(boolean show)
    {
        if(!isReady())
        {
            return ;
        }
        
        int flag = show?0:1;
        
        //int i = getmediaPlayerAdapter().enableSubtitle(flag);
        
        //Log.d(TAG, "showSubtitle return :"+i);
        
    }

    /**
     * @return 返回 audioinfos
     */
    public List<AudioInfoOfVideo> getAudioinfos()
    {
        return audioinfos;
    }
    
    
    public AudioInfoOfVideo getCurrentAudioinfos()
    {
    	return maudioInfoOfVidio;
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
        
        //return mediaplayer.setScreenScale(w, h);
        return 0;
    }

    public void setSubSurfaceHolder(SurfaceHolder sh)
    {
        mSH = sh;
    }
    
    
    public int setSpeed(int i)
    {
        if(!isReady())
        {
            return -1;
        }
        
       //return mediaplayer.setSpeed(i);
        return 0;
    }

    /* BEGIN: Added by r00178559 for AR-0000698413 2014/02/13 */
    public boolean setAudioChannelMode(int channelMode)
    {
        //return getmediaPlayerAdapter().setAudioChannelMode(channelMode);
    	return true;
    }
    /* END: Added by r00178559 for AR-0000698413 2014/02/13 */

    /* BEGIN: Added by c00224451 for  AR-0000698413 外挂字幕  2014/2/24 */
    public int setSubPath(String path)
    {        
        //return getmediaPlayerAdapter().setSubPath(path);
    	return 0;
    }
    /* END: Added by c00224451 for  AR-0000698413 外挂字幕  2014/2/24 */
    
    public int getBufferSizeStatus()
    {
        // TODO Auto-generated method stub
        if(!isReady())
        {
            return -1;
        }
        //return mediaplayer.getBufferSizeStatus();
        return 0;
    }

    public int getBufferTimeStatus()
    {
        // TODO Auto-generated method stub
        if(!isReady())
        {
            return -1;
        }
        //return mediaplayer.getBufferTimeStatus();
        return 0;
    }
    
    
    @Override
    public void layout(int l, int t, int r, int b)
    {
    	Log.i(TAG, "layout");
        int left = (maxWidth - videoOrigWidth) / 2;
        int top = (maxHeight - videoOrigHeight) / 2;
        super.layout(left, top, left + videoOrigWidth, top + videoOrigHeight);        
    }
        
    private void setSubTrackInfo()
    {
        if (mMediaPlayer !=null) 
        {            
            audioinfos.clear();
            audioTrackMap.clear();
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
    
    
    public boolean isDolbyEnabled()
    {
    	//return getmediaPlayerAdapter().isDolbyEnabled();
    	return false;
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
    

}
