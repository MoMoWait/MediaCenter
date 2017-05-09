package com.rockchips.mediacenter.view;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.rockchips.mediacenter.data.HiMediaPlayerInvoke;
import com.rockchips.mediacenter.service.IMediaPlayerAdapter;
import com.rockchips.mediacenter.service.LanguageXmlParser;
import com.rockchips.mediacenter.bean.AudioInfoOfVideo;
import com.rockchips.mediacenter.bean.SubInfo;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Parcel;
import android.util.Log;
import android.view.SurfaceHolder;

/**
 * 适配android原生的MediaPlayer
 * @author t00181037
 * @version 1.0
 * @created 18-十一月-2013 13:44:58
 */
public class OrigMediaPlayerAdapter implements IMediaPlayerAdapter
{
    
    private static final String TAG = "AndroidOrigMediaPlayerAdapter";
    
    private MediaPlayer mediaplayer;
    
    public String[] mSubFormat = {"ASS", "LRC", "SRT", "SMI", "SUB", "TXT", "PGS", "DVB", "DVD"};
    
    private LanguageXmlParser mLanguageXmlParser;
    
    private SurfaceHolder mSubSurfaceHolder;
    
    private AudioManager audioManager;

    public OrigMediaPlayerAdapter(Context context)
    {
        mLanguageXmlParser = new LanguageXmlParser();
        
        if (null == audioManager && null != context)
        {
            audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        }

    }
    
    
    
    public MediaPlayer getOriginMediaPlayer(){
    	return mediaplayer;
    }
    
    /**
     * 获取视频中的音频信息
     */
    public List<AudioInfoOfVideo> getAudioInfos()
    {
        if (!isInvalidateMediaPlayer())
        {
            return null;
        }
        
        Parcel _Request = Parcel.obtain();
        Parcel _Reply = Parcel.obtain();
        _Request.writeInterfaceToken("android.media.IMediaPlayer");// write
        //CMD_GET_AUDIO_INFO == 32
        _Request.writeInt(HiMediaPlayerInvoke.CMD_GET_AUDIO_INFO);
        
        A40HisiInvoke.invoke(getMediaPlayer(), _Request, _Reply);
        
        Log.e(TAG, "mediaplayer 1");
        List<AudioInfoOfVideo> _AudioInfoList = new ArrayList<AudioInfoOfVideo>();
        
        // for get
        int ret = _Reply.readInt();
        if (ret != 0)
        {
            Log.e(TAG, "CMD_GET_AUDIO_INFO read failed");
            _Request.recycle();
            _Reply.recycle();
            return _AudioInfoList;
        }
        Log.e(TAG, "mediaplayer 2");
        int _Num = _Reply.readInt();
        String _Language = "";
        //        String _Format = "";
        //        String _SampleRate = "";
        //        String _Channel = "";
        Log.e(TAG, "mediaplayer _Num:" + _Num);
        AudioInfoOfVideo aiov = null;
        
        for (int i = 0; i < _Num; i++)
        {
            aiov = new AudioInfoOfVideo();
            _Language = _Reply.readString();
            if (_Language == null)
            {
                _Language = "";
            }
            
            Log.e(TAG, "mediaplayer _Language:" + _Language);
            
            aiov.setlauguage(mLanguageXmlParser.getLanguage(_Language));
            
            Log.e(TAG, "mediaplayer aiov _Language:" + aiov.getlauguage());
            //                _AudioInfoList.add(_Language);
            
            //                _Format = Integer.toString();
            aiov.setaudioformat(_Reply.readInt());
            Log.e(TAG, "mediaplayer aiov setaudioformat:" + aiov.getaudioformat());
            //                _AudioInfoList.add(_Format);
            
            //                _SampleRate = Integer.toString();
            //                _AudioInfoList.add(_SampleRate);
            aiov.setsampleRate(_Reply.readInt());
            Log.e(TAG, "mediaplayer aiov setsampleRate:" + aiov.getsampleRate());
            int _ChannelNum = _Reply.readInt();
            aiov.setchannelNum(_ChannelNum);
            Log.e(TAG, "mediaplayer aiov setchannelNum:" + aiov.getchannelNum());
            
            _AudioInfoList.add(aiov);
        }
        
        _Request.recycle();
        _Reply.recycle();
        
        return _AudioInfoList;
        
    }
    
    /**
     * 返回当前播放位置的信息
     */
    public int getCurrentPosition()
    {
        if (!isInvalidateMediaPlayer())
        {
            return -1;
        }
        
        return getMediaPlayer().getCurrentPosition();
    }
    
    /**
     * 返回总时长
     */
    public int getDuration()
    {
        if (!isInvalidateMediaPlayer())
        {
            return -1;
        }
        
        return getMediaPlayer().getDuration();
    }
    
    /**
     * <一句话功能简述>
     * <功能详细描述>
     * @return -1失敗 0 成功
     * @see [类、类#方法、类#成员]
     */
    public int getCurrentSubId()
    {
        if (!isInvalidateMediaPlayer())
        {
            return -1;
        }
        
        Parcel requestParcel = Parcel.obtain();// Parcel allocation
        requestParcel.writeInterfaceToken("android.media.IMediaPlayer");// write
        // parameter
        requestParcel.writeInt(HiMediaPlayerInvoke.CMD_GET_SUB_ID);
        
        Parcel replyParcel = Parcel.obtain();
        
        A40HisiInvoke.invoke(getMediaPlayer(), requestParcel, replyParcel);
        
        int subIdRet = replyParcel.readInt();
        
        int subId = replyParcel.readInt();
        
        Log.d(TAG, "Current Sub id: " + subId);
        
        requestParcel.recycle();
        replyParcel.recycle();
        
        return subIdRet >= 0 ? subId : -1;
    }
    
    /**
     * 设置声音id
     * 
     * @param sndi
     * @return -1:failed 0:success
     */
    public int setSoundId(int sndid)
    {
        if (!isInvalidateMediaPlayer())
        {
            return -1;
        }
        
        getMediaPlayer().start();
        getMediaPlayer().selectTrack(sndid);//setAudioTrack(sndid);
        
        return sndid;
    }
    
    /**
     * 设置sub id，显示指定的字幕
     * 
     * @param subid 
     * @return -1失败 >=0 成功
     */
    public int setSubId(int subid)
    {
        if (!isInvalidateMediaPlayer())
        {
            return -1;
        }
        
        Parcel requestParcel = Parcel.obtain();// Parcel allocation
        requestParcel.writeInterfaceToken("android.media.IMediaPlayer");// write
        // parameter
        requestParcel.writeInt(HiMediaPlayerInvoke.CMD_SET_SUB_ID);
        requestParcel.writeInt(subid);
        
        Parcel replyParcel = Parcel.obtain();
        
        A40HisiInvoke.invoke(getMediaPlayer(), requestParcel, replyParcel); // invoke
        
        int setSubIdRet = replyParcel.readInt();
        
        requestParcel.recycle();
        replyParcel.recycle();
        
        return setSubIdRet;
    }
    
    public int getVideoHeight()
    {
        if (!isInvalidateMediaPlayer())
        {
            return -1;
        }
        
        return getMediaPlayer().getVideoHeight();
    }
    
    public int getVideoWidth()
    {
        if (!isInvalidateMediaPlayer())
        {
            return -1;
        }
        return getMediaPlayer().getVideoWidth();
    }
    
    /**
     * 
     * @param left
     * @param top
     * @param w
     * @param h
     */
    public int setScreenOutRange(int left, int top, int w, int h)
    {
        if (!isInvalidateMediaPlayer())
        {
            return -1;
        }
        Parcel requestParcel = Parcel.obtain();
        requestParcel.writeInterfaceToken("android.media.IMediaPlayer");
        // CMD_SET_SURFACE_POSITION = 24
        // CMD_SET_SURFACE_SIZE = 22
        // CMD_SET_OUTRANGE = 100
        //
        requestParcel.writeInt(HiMediaPlayerInvoke.CMD_SET_SURFACE_POSITION);
        //x 
        requestParcel.writeInt(left);
        //y 
        requestParcel.writeInt(top);
        Parcel replyParcel = Parcel.obtain();
        A40HisiInvoke.invoke(getMediaPlayer(), requestParcel, replyParcel); // invoke
        int ret = replyParcel.readInt();

        requestParcel.recycle();
        requestParcel = Parcel.obtain();
        requestParcel.writeInterfaceToken("android.media.IMediaPlayer");
        // CMD_SET_SURFACE_POSITION = 24
        // CMD_SET_SURFACE_SIZE = 22
        // CMD_SET_OUTRANGE = 100
        //
        requestParcel.writeInt(HiMediaPlayerInvoke.CMD_SET_SURFACE_SIZE);
        //w
        requestParcel.writeInt(w - left);
        //h 
        requestParcel.writeInt(h - top);
        replyParcel.recycle();
        replyParcel = Parcel.obtain();
        A40HisiInvoke.invoke(getMediaPlayer(), requestParcel, replyParcel); // invoke
        ret = replyParcel.readInt();
        
        
        requestParcel.recycle();
        replyParcel.recycle();
        return ret;
        
    }
    
    /**
     * 兼容android4.0以及以下版本；
     * 这里不需要该接口
     * @param sh
     */
    public void setSubDisplay(SurfaceHolder sh)
    {
        mSubSurfaceHolder = sh;
    }
    
    @Override
    public int getSubInfos(List<SubInfo> sublist)
    {
        if (!isInvalidateMediaPlayer())
        {
            return -1;
        }
        
        Parcel requestParcel = Parcel.obtain(); // Parcel allocation
        requestParcel.writeInterfaceToken("android.media.IMediaPlayer");// write
        // parameter
        requestParcel.writeInt(HiMediaPlayerInvoke.CMD_GET_SUB_INFO);// CMD_GET_SUB_INFO =103

        Parcel replyParcel = Parcel.obtain();

        A40HisiInvoke.invoke(getMediaPlayer(),requestParcel, replyParcel); // invoke

        int subInfoRet = replyParcel.readInt();
        int subNum = replyParcel.readInt();

        Log.d(TAG, "---->subNum " + subNum);
        
        SubInfo si = null;
        
        if(sublist == null)
        {
            sublist = new ArrayList<SubInfo>();
        }
        
        for (int i = 0; i < subNum; i++)
        {
            int infosubId = replyParcel.readInt();
            int isExtra = replyParcel.readInt();//1:ext 0:inner
            String language = replyParcel.readString();
            
            int iformat = replyParcel.readInt();
            
            Log.e(TAG, "---->sub " + i + "iformat == " +iformat);
            String subFormat = "N/A";
            
            if(iformat < mSubFormat.length)
            {
                subFormat = mSubFormat[iformat];
            }
            Log.e(TAG, "---->sub " + i + "name == " + language+" "+subFormat);
            
            si = new SubInfo();
            si.setIsExtra(isExtra == 1);
            si.setSubid(infosubId);
            si.setLanguage(mLanguageXmlParser.getLanguage(language));
            si.setFormat(subFormat);
            Log.e(TAG, "---->sub " + i + "getLanguage == " + si.getLanguage());
            sublist.add(si);
        }

        requestParcel.recycle();
        replyParcel.recycle();
        
        return subInfoRet;
    }
    
    /**
     * {@inheritDoc}
     */
    public int getCurrentSndId()
    {
        if (!isInvalidateMediaPlayer())
        {
            return -1;
        }
        
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeInterfaceToken("android.media.IMediaPlayer");// write
        request.writeInt(HiMediaPlayerInvoke.CMD_GET_AUDIO_TRACK_PID);// 设置-12 获取-13 CMD_SET_AUDIO_TRACK_PID in
        // frameworks/base/include/media/MediaPlayerInvoke.h"
        //        request.writeInt(audioPid);
        
        A40HisiInvoke.invoke(getMediaPlayer(), request, reply); // invoke
        
        int ret = reply.readInt();
        if (ret != 0)
        {
            request.recycle();
            reply.recycle();
            Log.d(TAG, "getCurrentSndId fail.");
            return 0;
        }
        
        int currsndid = reply.readInt();
        
        request.recycle();
        reply.recycle();
        
        return currsndid;
    }
    
    @Override
    public boolean isSubtitleShowing()
    {
        if (!isInvalidateMediaPlayer())
        {
            return false;
        }
        
        Parcel requestParcel = Parcel.obtain(); // Parcel allocation
        requestParcel.writeInterfaceToken("android.media.IMediaPlayer");
        // parameter
        requestParcel.writeInt(HiMediaPlayerInvoke.CMD_GET_SUB_DISABLE);// CMD_GET_SUB_DISABLE=136
        
        Parcel replyParcel = Parcel.obtain();
        
        A40HisiInvoke.invoke(getMediaPlayer(), requestParcel, replyParcel); // invoke
        
        int subStateRet = replyParcel.readInt();
        
        int subState = replyParcel.readInt();
        
        requestParcel.recycle();
        replyParcel.recycle();
        
        return subState == 0 ? true : false;
    }
    
    @Override
    public int enableSubtitle(int enable)
    {
        if (!isInvalidateMediaPlayer())
        {
            return -1;
        }
        
        Parcel requestParcel = Parcel.obtain();// Parcel allocation
        requestParcel.writeInterfaceToken("android.media.IMediaPlayer");// write
        // parameter
        requestParcel.writeInt(HiMediaPlayerInvoke.CMD_SET_SUB_DISABLE);
        requestParcel.writeInt(enable);
        
        Parcel replyParcel = Parcel.obtain();
        
        A40HisiInvoke.invoke(getMediaPlayer(), requestParcel, replyParcel); // invoke
        
        int subStateRet = replyParcel.readInt();
        
        requestParcel.recycle();
        replyParcel.recycle();
        
        return subStateRet;
    }
    
    public int setScreenScale(int w, int h)
    {
        if (!isInvalidateMediaPlayer())
        {
            Log.e(TAG, "setScreenScale mMediaPlayer = null");
            return -1;
        }
        
        Parcel requestParcel = Parcel.obtain();
        requestParcel.writeInterfaceToken("android.media.IMediaPlayer");
        // CMD_SET_SURFACE_SIZE = 22
        requestParcel.writeInt(HiMediaPlayerInvoke.CMD_SET_SURFACE_SIZE);
        
        // width
        requestParcel.writeInt(w);
        // height
        requestParcel.writeInt(h);
        
        Parcel replyParcel = Parcel.obtain();
        
        A40HisiInvoke.invoke(getMediaPlayer(), requestParcel, replyParcel); // invoke
        
        int ret = replyParcel.readInt();
        

        requestParcel.recycle();
        replyParcel.recycle();
        
        return ret;
    }
    
    @Override
    public int setSpeed(int i)
    {
        //        if (!isInvalidateMediaPlayer())
        //        {
        //            return -1;
        //        }
        //        
        //        return mediaplayer.setSpeed(i);
        
        return -1;
    }
    
    private boolean isInvalidateMediaPlayer()
    {
        if (getMediaPlayer() == null)
        {
            Log.e(TAG, "Orig Media Player is null");
            return false;
        }
        return true;
    }
    
    /**
     * @return 返回 mediaplayer
     */
    public MediaPlayer getMediaPlayer()
    {
        return mediaplayer;
    }
    
    /**
     * @param 对mediaplayer进行赋值
     */
    public void setMediaPlayer(MediaPlayer newVal)
    {
    	mediaplayer = newVal;
    }
  
    
    @Override
    public void setDataSource(String path) throws IllegalArgumentException, SecurityException, IllegalStateException, IOException
    {
        if (!isInvalidateMediaPlayer())
        {
            Log.e(TAG, "setDataSource mMediaPlayer = null");
            return;
        }
        mediaplayer.setDataSource(path);
    }
    
    @Override
    public void reset()
    {
        if (!isInvalidateMediaPlayer())
        {
            Log.e(TAG, "setDataSource mMediaPlayer = null");
            return;
        }
        
        mediaplayer.reset();
    }
    
    @Override
    public void prepareAsync()
    {
        if (!isInvalidateMediaPlayer())
        {
            Log.e(TAG, "setDataSource mMediaPlayer = null");
            return;
        }
        
        mediaplayer.prepareAsync();
    }
    
    @Override
    public void pause()
    {
        if (!isInvalidateMediaPlayer())
        {
            Log.e(TAG, "setDataSource mMediaPlayer = null");
            return;
        }
        
        mediaplayer.pause();
    }
    
    @Override
    public void setOnBufferingUpdateListener(Object object)
    {
        if (!isInvalidateMediaPlayer())
        {
            Log.e(TAG, "setOnBufferingUpdateListener mMediaPlayer = null");
            return;
        }
        
        mediaplayer.setOnBufferingUpdateListener((MediaPlayer.OnBufferingUpdateListener)object);
    }
    
    @Override
    public void setOnCompletionListener(Object object)
    {
        if (!isInvalidateMediaPlayer())
        {
            Log.e(TAG, "setOnCompletionListener mMediaPlayer = null");
            return;
        }
        
        mediaplayer.setOnCompletionListener((MediaPlayer.OnCompletionListener)object);
    }
    
    @Override
    public void setOnErrorListener(Object object)
    {
        if (!isInvalidateMediaPlayer())
        {
            Log.e(TAG, "setOnErrorListener mMediaPlayer = null");
            return;
        }
        
        mediaplayer.setOnErrorListener((MediaPlayer.OnErrorListener)object);
    }
    
    @Override
    public void setOnInfoListener(Object object)
    {
        if (!isInvalidateMediaPlayer())
        {
            Log.e(TAG, "setOnInfoListener mMediaPlayer = null");
            return;
        }
        
        mediaplayer.setOnInfoListener((MediaPlayer.OnInfoListener)object);
    }
    
    @Override
    public void setOnPreparedListener(Object object)
    {
        if (!isInvalidateMediaPlayer())
        {
            Log.e(TAG, "setOnPreparedListener mMediaPlayer = null");
            return;
        }
        
        mediaplayer.setOnPreparedListener((MediaPlayer.OnPreparedListener)object);
    }
    
    @Override
    public void setOnSeekCompleteListener(Object object)
    {
        if (!isInvalidateMediaPlayer())
        {
            Log.e(TAG, "setOnSeekCompleteListener mMediaPlayer = null");
            return;
        }
        
        mediaplayer.setOnSeekCompleteListener((MediaPlayer.OnSeekCompleteListener)object);
    }
    
    @Override
    public void setOnVideoSizeChangedListener(Object object)
    {
        if (!isInvalidateMediaPlayer())
        {
            Log.e(TAG, "setOnVideoSizeChangedListener mMediaPlayer = null");
            return;
        }
        
        mediaplayer.setOnVideoSizeChangedListener((MediaPlayer.OnVideoSizeChangedListener)object);
    }

    @Override
    public void release()
    {
        if (!isInvalidateMediaPlayer())
        {
            Log.e(TAG, "release mMediaPlayer = null");
            return;
        }
        
        mediaplayer = null;
    }
    

    @Override
    public void start()
    {
        if (!isInvalidateMediaPlayer())
        {
            Log.e(TAG, "start mMediaPlayer = null");
            return;
        }
        
        mediaplayer.start();
    }

    @Override
    public void stop()
    {
        if (!isInvalidateMediaPlayer())
        {
            Log.e(TAG, "stop mMediaPlayer = null");
            return;
        }
        
        mediaplayer.stop();
    }

    @Override
    public void seekTo(int seek)
    {
        if (!isInvalidateMediaPlayer())
        {
            Log.e(TAG, "seekTo mMediaPlayer = null");
            return;
        }
        
        mediaplayer.seekTo(seek);
    }
    
    public void  setDataSource(Context context, Uri uri) throws IllegalArgumentException, SecurityException, IllegalStateException, IOException
    {
        if (!isInvalidateMediaPlayer())
        {
            Log.e(TAG, "setDataSource mMediaPlayer = null");
            return;
        }
        
        mediaplayer.setDataSource(context,uri);
    }

    public int invoke(Parcel request, Parcel reply)
    {
        if (!isInvalidateMediaPlayer())
        {
            Log.e(TAG, "invoke mMediaPlayer = null");
            return -1;
        }
        
        A40HisiInvoke.invoke(mediaplayer,request, reply);
        return 0;
    }

    /* BEGIN: Added by r00178559 for AR-0000698413 2014/02/13 */
    /************BEGIN: Added by w00190739 for DTS2014102402150  2014/11/05
     * 仅仅用于瑞芯微平台，其他平台的版本不用同步该代码 ********************/
    @Override
    public boolean setAudioChannelMode(int channelMode)
    {
        Method setDtvOutputMode = null;
        try
        {
            setDtvOutputMode = audioManager.getClass().getDeclaredMethod("setDtvOutputMode", int.class);
        }
        catch (NoSuchMethodException e)
        {
            Log.e(TAG, "setDtvOutputMode NoSuchMethodException");
            return false;
        }
        try
        {
            setDtvOutputMode.invoke(audioManager, channelMode);
        }
        catch (IllegalAccessException e)
        {
            Log.e(TAG, "setDtvOutputMode IllegalAccessException");
            return false;
        }
        catch (IllegalArgumentException e)
        {
            Log.e(TAG, "setDtvOutputMode IllegalArgumentException");
            return false;

        }
        catch (InvocationTargetException e)
        {
            Log.e(TAG, "setDtvOutputMode InvocationTargetException");
            return false;

        }
        return true;
    }
    /************END: Added by w00190739 for DTS2014102402150  2014/11/05  ********************/
    /* END: Added by r00178559 for AR-0000698413 2014/02/13 */

    /* BEGIN: Added by c00224451 for  AR-0000698413 外挂字幕  2014/2/24 */
    @Override
    public int setSubPath(String path)
    {
        if (null == mediaplayer)
        {
            return -1;
        }
        
        Method setSubPath = null;
        try
        {
            setSubPath = mediaplayer.getClass().getDeclaredMethod("setSubPath", String.class);
        }
        catch (NoSuchMethodException e)
        {
            Log.e(TAG, "setSubPath NoSuchMethodException");
            return -1;
        }
        
        Object mResult = null;
        try
        {
            mResult = setSubPath.invoke(mediaplayer, path);
        }
        catch (IllegalArgumentException e)
        {
            Log.e(TAG, "setSubPath IllegalArgumentException");
            return -1;
        }
        catch (IllegalAccessException e)
        {
            Log.e(TAG, "setSubPath IllegalAccessException");
            return -1;
        }
        catch (InvocationTargetException e)
        {
            Log.e(TAG, "setSubPath InvocationTargetException");
            return -1;
        }
        
        if (null == mResult)
        {
            return -1;
        }
        if (!(mResult instanceof Boolean))
        {
            return -1;
        }
        boolean result = ((Boolean) mResult).booleanValue();
        Log.d(TAG, "setSubPath result = " + result);
        return result ? 0 : -1;
    }
    /* END: Added by c00224451 for  AR-0000698413 外挂字幕  2014/2/24 */

    @Override
    public int getBufferSizeStatus()
    {
        // TODO Auto-generated method stub
        return -1;
    }

    @Override
    public int getBufferTimeStatus()
    {
        // TODO Auto-generated method stub
        return -1;
    }

	public boolean isDolbyEnabled()
    {
        int isDobly = 0;
		if (null != mediaplayer) {
			Parcel parcel = mediaplayer.getParcelParameter(IMediaPlayerAdapter.MEDIAPLAYER_GET_WHETHER_DOBLY);
			isDobly = parcel.readInt();
			parcel.recycle();			
		}
		Log.d(TAG, " getParcelParameter isDobly= " + isDobly+",isDolbyEnabled= "+((isDobly == 1) ? true : false));
		return ((isDobly == 1) ? true : false);
    }

}