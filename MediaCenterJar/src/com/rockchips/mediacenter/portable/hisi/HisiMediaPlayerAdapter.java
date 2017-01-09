package com.rockchips.mediacenter.portable.hisi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Parcel;
import android.util.Log;
import android.view.SurfaceHolder;

import com.hisilicon.android.mediaplayer.HiMediaPlayer;
import com.hisilicon.android.mediaplayer.HiMediaPlayerInvoke;
import com.rockchips.mediacenter.portable.IMediaPlayerAdapter;
import com.rockchips.mediacenter.portable.LanguageXmlParser;
import com.rockchips.mediacenter.portable.bean.AudioInfoOfVideo;
import com.rockchips.mediacenter.portable.bean.SubInfo;

/**
 * 适配android原生的MediaPlayer
 * 
 * @author t00181037
 * @version 1.0
 * @created 18-十一月-2013 13:44:59
 */
public class HisiMediaPlayerAdapter implements IMediaPlayerAdapter
{
    
    private String TAG = "HisiMediaPlayerAdapter";
    
    private HiMediaPlayer mediaplayer = null;
    
    public String[] mSubFormat = {"ASS", "LRC", "SRT", "SMI", "SUB", "TXT", "PGS", "DVB", "DVD"};
    
    private LanguageXmlParser mLanguageXmlParser;
    
    private SurfaceHolder mSubSurfaceHolder;
    
    public HisiMediaPlayerAdapter(Context context)
    {
        mLanguageXmlParser = new LanguageXmlParser();
    }
    
    public HiMediaPlayer getMediaPlayer()
    {
        return mediaplayer;
    }
    
    /**
     * 
     * @param newVal
     */
    public void setMediaPlayer(HiMediaPlayer newVal)
    {
        if(newVal == null)
        {
            mediaplayer = new HiMediaPlayer();
        }else
        {
            mediaplayer = newVal;
        }
        
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
        
        // CMD_GET_AUDIO_INFO == 32
        _Request.writeInt(HiMediaPlayerInvoke.CMD_GET_AUDIO_INFO);
        
        mediaplayer.invoke(_Request, _Reply);
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
        // String _Format = "";
        // String _SampleRate = "";
        // String _Channel = "";
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
            // _AudioInfoList.add(_Language);
            
            // _Format = Integer.toString();
            aiov.setaudioformat(_Reply.readInt());
            Log.e(TAG, "mediaplayer aiov setaudioformat:" + aiov.getaudioformat());
            // _AudioInfoList.add(_Format);
            
            // _SampleRate = Integer.toString();
            // _AudioInfoList.add(_SampleRate);
            aiov.setsampleRate(_Reply.readInt());
            Log.e(TAG, "mediaplayer aiov setsampleRate:" + aiov.getsampleRate());
            int _ChannelNum = _Reply.readInt();
            // switch (_ChannelNum)
            // {
            // case 0:
            // case 1:
            // case 2:
            // _Channel = _ChannelNum + ".0";
            // break;
            // default:
            // _Channel = (_ChannelNum - 1) + ".1";
            // break;
            // }
            
            // _AudioInfoList.add(_Channel);
            aiov.setchannelNum(_ChannelNum);
            Log.e(TAG, "mediaplayer aiov setchannelNum:" + aiov.getchannelNum());
            
            _AudioInfoList.add(aiov);
        }
        
        _Request.recycle();
        _Reply.recycle();
        
        return _AudioInfoList;
        
    }
    
    /* BEGIN: Added by r00178559 for AR-0000698413 2014/02/12 */
    public boolean setAudioChannelMode(int channelMode)
    {
        Log.d(TAG, "setAudioChannel mode(" + channelMode + ") E");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        
        request.writeInt(HiMediaPlayerInvoke.CMD_SET_AUDIO_CHANNEL_MODE);
        request.writeInt(channelMode);
        mediaplayer.invoke(request, reply);
        
        int ret = reply.readInt();
        if (ret != 0)
        {
            Log.e(TAG, "setAudioChannel mode(" + channelMode + ") failed");
            request.recycle();
            reply.recycle();
            return false;
        }
        
        request.recycle();
        reply.recycle();
        Log.d(TAG, "setAudioChannel mode(" + channelMode + ") X");
        return true;
    }
    /* END: Added by r00178559 for AR-0000698413 2014/02/12 */
    
    /**
     * 返回当前播放位置的信息
     */
    public int getCurrentPosition()
    {
        if (!isInvalidateMediaPlayer())
        {
            return -1;
        }
        
        return mediaplayer.getCurrentPosition();
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
        
        return mediaplayer.getDuration();
    }
    
    /**
     * <一句话功能简述> <功能详细描述>
     * 
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
        // parameter
        requestParcel.writeInt(HiMediaPlayerInvoke.CMD_GET_SUB_ID);
        
        Parcel replyParcel = Parcel.obtain();
        
        mediaplayer.invoke(requestParcel, replyParcel); // invoke
        
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
        
        // 设置
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeInt(HiMediaPlayerInvoke.CMD_SET_AUDIO_TRACK_PID);// 设置-12 获取-13 CMD_SET_AUDIO_TRACK_PID in
        // frameworks/base/include/media/MediaPlayerInvoke.h"
        request.writeInt(sndid);
        
        mediaplayer.invoke(request, reply);
        
        // 第一个位置是返回值 -1是失败
        sndid = reply.readInt();
        
        request.recycle();
        reply.recycle();
        
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
        
        return mediaplayer.setSubTrack(subid);
    }
    
    public int getVideoHeight()
    {
        if (!isInvalidateMediaPlayer())
        {
            return -1;
        }
        
        return mediaplayer.getVideoHeight();
    }
    
    public int getVideoWidth()
    {
        if (!isInvalidateMediaPlayer())
        {
            return -1;
        }
        return mediaplayer.getVideoWidth();
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
        
        return mediaplayer.setVideoRange(left, top, w, h);
    }
    
    /**
     * 兼容android4.0以及以下版本； 这里不需要该接口
     * 
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
        // parameter
        requestParcel.writeInt(HiMediaPlayerInvoke.CMD_GET_SUB_INFO);// CMD_GET_SUB_INFO
                                                                               // =103
        
        Parcel replyParcel = Parcel.obtain();
        
        mediaplayer.invoke(requestParcel, replyParcel); // invoke
        
        int subInfoRet = replyParcel.readInt();
        int subNum = replyParcel.readInt();
        // sub.setSubNum(subNum);
        
//        String[] titles = new String[subNum];
        Log.d(TAG, "---->subNum " + subNum);
        
        SubInfo si = null;
        
        if (sublist == null)
        {
            sublist = new ArrayList<SubInfo>();
        }
        
        for (int i = 0; i < subNum; i++)
        {
            int infosubId = replyParcel.readInt();
            int isExtra = replyParcel.readInt();// 1:ext 0:inner
            String language = replyParcel.readString();
            String subFormat = mSubFormat[replyParcel.readInt()];
            Log.e(TAG, "---->sub " + i + "name == " + language + " " + subFormat);
            
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
    
    private boolean isInvalidateMediaPlayer()
    {
        if (mediaplayer == null)
        {
            Log.e(TAG, "Hisi Media Player is null");
            return false;
        }
        return true;
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
        request.writeInt(HiMediaPlayerInvoke.CMD_GET_AUDIO_TRACK_PID);// 设置-12
                                                                                // 获取-13
                                                                                // CMD_SET_AUDIO_TRACK_PID
                                                                                // in
        // frameworks/base/include/media/MediaPlayerInvoke.h"
        // request.writeInt(audioPid);
        
        mediaplayer.invoke(request, reply);
        
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
        // parameter
        requestParcel.writeInt(HiMediaPlayerInvoke.CMD_GET_SUB_DISABLE);// CMD_GET_SUB_DISABLE=136
        
        Parcel replyParcel = Parcel.obtain();
        
        mediaplayer.invoke(requestParcel, replyParcel);
        
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
        
        return mediaplayer.enableSubtitle(enable);
    }
    
    public int setScreenScale(int w, int h)
    {
        if (!isInvalidateMediaPlayer())
        {
            Log.e(TAG, "setScreenScale mMediaPlayer = null");
            return -1;
        }
        
        Parcel requestParcel = Parcel.obtain();
        // requestParcel.writeInterfaceToken("android.media.IMediaPlayer");
        // CMD_SET_SURFACE_SIZE = 22
        requestParcel.writeInt(HiMediaPlayerInvoke.CMD_SET_SURFACE_SIZE);
        
        // width
        requestParcel.writeInt(w);
        // height
        requestParcel.writeInt(h);
        
        Parcel replyParcel = Parcel.obtain();
        
        mediaplayer.invoke(requestParcel, replyParcel); // invoke
        
        int ret = replyParcel.readInt();
        
        
        requestParcel.recycle();
        replyParcel.recycle();
        
        return ret;
    }
    
    @Override
    public int setSpeed(int i)
    {
        if (!isInvalidateMediaPlayer())
        {
            return -1;
        }
        
        return mediaplayer.setSpeed(i);
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
        
        mediaplayer.setOnBufferingUpdateListener((HiMediaPlayer.OnBufferingUpdateListener)object);
    }
    
    @Override
    public void setOnCompletionListener(Object object)
    {
        if (!isInvalidateMediaPlayer())
        {
            Log.e(TAG, "setOnCompletionListener mMediaPlayer = null");
            return;
        }
        
        mediaplayer.setOnCompletionListener((HiMediaPlayer.OnCompletionListener)object);
    }
    
    @Override
    public void setOnErrorListener(Object object)
    {
        if (!isInvalidateMediaPlayer())
        {
            Log.e(TAG, "setOnErrorListener mMediaPlayer = null");
            return;
        }
        
        mediaplayer.setOnErrorListener((HiMediaPlayer.OnErrorListener)object);
    }
    
    @Override
    public void setOnInfoListener(Object object)
    {
        if (!isInvalidateMediaPlayer())
        {
            Log.e(TAG, "setOnInfoListener mMediaPlayer = null");
            return;
        }
        
        mediaplayer.setOnInfoListener((HiMediaPlayer.OnInfoListener)object);
    }
    
    @Override
    public void setOnPreparedListener(Object object)
    {
        if (!isInvalidateMediaPlayer())
        {
            Log.e(TAG, "setOnPreparedListener mMediaPlayer = null");
            return;
        }
        
        mediaplayer.setOnPreparedListener((HiMediaPlayer.OnPreparedListener)object);
    }
    
    @Override
    public void setOnSeekCompleteListener(Object object)
    {
        if (!isInvalidateMediaPlayer())
        {
            Log.e(TAG, "setOnSeekCompleteListener mMediaPlayer = null");
            return;
        }
        
        mediaplayer.setOnSeekCompleteListener((HiMediaPlayer.OnSeekCompleteListener)object);
    }
    
    @Override
    public void setOnVideoSizeChangedListener(Object object)
    {
        if (!isInvalidateMediaPlayer())
        {
            Log.e(TAG, "setOnVideoSizeChangedListener mMediaPlayer = null");
            return;
        }
        
        mediaplayer.setOnVideoSizeChangedListener((HiMediaPlayer.OnVideoSizeChangedListener)object);
    }

    @Override
    public void release()
    {
        if (!isInvalidateMediaPlayer())
        {
            Log.e(TAG, "release mMediaPlayer = null");
            return;
        }
        
        mediaplayer.release();
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
        
        return mediaplayer.invoke(request, reply);
    }

    /* BEGIN: Added by c00224451 for  AR-0000698413 外挂字幕  2014/2/24 */
    @Override
    public int setSubPath(String path)
    {        
        return mediaplayer.setSubPath(path);
    }
    /* END: Added by c00224451 for  AR-0000698413 外挂字幕  2014/2/24 */

    @Override
    public int getBufferSizeStatus()
    {
        // TODO Auto-generated method stub
        return mediaplayer.getBufferSizeStatus();
    }

    @Override
    public int getBufferTimeStatus()
    {
        // TODO Auto-generated method stub
        return mediaplayer.getBufferTimeStatus();
    }

	public boolean isDolbyEnabled() {
		int isDobly = 0;
		if (null != mediaplayer) {
			Parcel parcel = mediaplayer.getParcelParameter(IMediaPlayerAdapter.MEDIAPLAYER_GET_WHETHER_DOBLY);
			isDobly = parcel.readInt();
			parcel.recycle();
		}
		Log.d(TAG, " getParcelParameter isDobly= " + isDobly);
		return ((isDobly == 1) ? true : false);
	}
	
	public MediaPlayer getOriginMediaPlayer(){
		return null;
	}

}