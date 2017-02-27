package com.rockchips.mediacenter.service;


import java.net.URI;
import java.util.List;

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

import android.net.Uri;
import android.view.SurfaceHolder;

/**
 * VideoView适配接口
 * @author t00181037
 * @version 1.0
 * @created 18-十一月-2013 13:45:05
 */
public interface IVideoViewAdapter {


    /**
     * 获取音轨列表
     */
    public List<AudioInfoOfVideo> getAudioinfos();
    
    public AudioInfoOfVideo getCurrentAudioinfos();

    /**
     * 获取目前播放位置
     */
    public int getCurrentPosition();

    public int getCurrentSoundId();

    /**
     * 获取目前显示器使用的字幕
     */
    public int getCurrentSudId();

    public IMediaPlayerAdapter getmediaPlayerAdapter();

    public OnBufferingUpdateListener getonBufferingUpdateListener();

    public OnErrorListener getonErrorListener();

    public OnInfoListener getonInfoListener();

    public OnPreparedListener getonPreparedListener();

    public OnSeekCompleteListener getonSeekCompleteListener();

    /**
     * 获取字幕列表
     */
    public List<SubInfo> getSubtitleList();

    public int getVideoHeight();

    public int getVideoWidth();

    /**
     * 判断是否正在播放中
     */
    public boolean isPlaying();

    /**
     * 判断是否正在seek中
     * fixme 。。。这个接口暴露的有问题
     */
    public boolean isSeeking();

    /**
     * 设置是否正在seek，保存外部变量
     * 
     * @param b
     */
    public void isSeeking(boolean b);

    public boolean isSubtitleShowing();

    public void pause();

    /**
     * 获取目前播放位置
     */
    public void seekTo(int i);

    /**
     * 
     * @param newVal
     */
    public void setmediaPlayerAdapter(IMediaPlayerAdapter newVal);

    /**
     * 
     * @param newVal
     */
    public void setOnBufferingUpdateListener(OnBufferingUpdateListener newVal);

    /**
     * 
     * @param newVal
     */
    public void setOnErrorListener(OnErrorListener newVal);

    /**
     * 
     * @param newVal
     */
    public void setOnInfoListener(OnInfoListener newVal);

    /**
     * 
     * @param newVal
     */
    public void setOnPreparedListener(OnPreparedListener newVal);

    /**
     * 
     * @param newVal
     */
    public void setOnSeekCompleteListener(OnSeekCompleteListener newVal);
    
    /**
     * 
     * @param newVal
     */
    public void setOnCompletionListener(OnCompleteListener newVal);

    /**
     * 
     * @param newVal
     */
    public void setOnFastForwardCompleteListener(OnFastForwardCompleteListener l);
    
    /**
     * 
     * @param newVal
     */
    public void setOnBackForwardCompleteListener(OnFastBackwordCompleteListener l);
    
    /**
     * 设置播放窗口
     * 
     * @param left
     * @param top
     * @param w
     * @param h
     */
    public void setOutRange(int left, int top, int w, int h);

    /**
     * 设置声音id
     * 
     * @param id
     */
    public void setSoundId(int id);

    /**
     * 设置字幕
     * 
     * @param id
     */
    public void setSubId(int id);
    
    /* BEGIN: Added by c00224451 for  AR-0000698413 外挂字幕  2014/2/24 */
    /**
     * 设置外置字幕
     * 
     * @param path,外置字幕路径
     */
    public int setSubPath(String path);
    /* END: Added by c00224451 for  AR-0000698413 外挂字幕  2014/2/24 */

    /**
     * 设置播放uri
     * 
     * @param uri
     * @return 
     */
    public void setVideoPath(String uri);
    
    /**
     * 设置播放uri
     * 
     * @param uri
     * @return 
     */
    public void setVideoURI(Uri uri);

    /**
     * 
     * @param show
     * @return 
     */
    public void showSubtitle(boolean show);

    public void start();

    public void stopPlayback();

    public int getDuration();
    
    
    /**
     *设置ｓｕｒｆａｃｅ宽高 
     * <功能详细描述>
     * @param w
     * @param h
     * @return 成功　返回０；否则失败
     * @see [类、类#方法、类#成员]
     */
    int setScreenScale(int w, int h);
    
    /**
     *  设置surfaceholder
     * <功能详细描述>
     * @param sh
     * @see [类、类#方法、类#成员]
     */
    void setSubSurfaceHolder(SurfaceHolder sh);
    
    /**
     * 设置速度
     * <功能详细描述>
     * @param i
     * @see [类、类#方法、类#成员]
     */
    int setSpeed(int i);
    
    // zkf61715
    int getBufferSizeStatus();
    int getBufferTimeStatus();
    
    /* BEGIN: Added by r00178559 for AR-0000698413 2014/02/13 */
    /**
     * 设置音效（立体声/左声道/右声道。。。）
     * @param channelMode
     * @return
     */
    boolean setAudioChannelMode(int channelMode);
    /* END: Added by r00178559 for AR-0000698413 2014/02/13 */

	boolean isDolbyEnabled();
}