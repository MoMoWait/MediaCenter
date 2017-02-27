package com.rockchips.mediacenter.service;

import java.io.IOException;
import java.util.List;

import com.rockchips.mediacenter.bean.AudioInfoOfVideo;
import com.rockchips.mediacenter.bean.SubInfo;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Parcel;
import android.view.SurfaceHolder;

/**
 * @author t00181037
 * @version 1.0
 * @created 18-十一月-2013 13:44:55
 */
public interface IMediaPlayerAdapter {

	/**
	 * 获取视频中的音频信息
	 */
	public List<AudioInfoOfVideo> getAudioInfos();

	/**
	 * 返回当前播放位置的信息
	 */
	public int getCurrentPosition();

	/**
	 * 返回总时长
	 */
	public int getDuration();

	/**
	 * 返回当前正在使用的字幕ID
	 * 
	 * @param sublist
	 */
	public int getSubInfos(List<SubInfo> sublist);

	/**
	 * 获取当前在用的sub id <功能详细描述>
	 * 
	 * @return
	 * @see [类、类#方法、类#成员]
	 */
	public int getCurrentSubId();

	/**
	 * 获取视频高度 <功能详细描述>
	 * 
	 * @return
	 * @see [类、类#方法、类#成员]
	 */
	public int getVideoHeight();

	/**
	 * 获取视频宽度 <功能详细描述>
	 * 
	 * @return
	 * @see [类、类#方法、类#成员]
	 */
	public int getVideoWidth();

	/**
	 * 
	 * @param left
	 * @param top
	 * @param w
	 * @param h
	 * @return 0:success -1:failed
	 */
	public int setScreenOutRange(int left, int top, int w, int h);

	/**
	 * 设置声音id
	 * 
	 * @param sndid
	 * @return 0:success -1:failed
	 */
	public int setSoundId(int sndid);

	/**
	 * 兼容android4.0以及以下版本；
	 * 
	 * @param sh
	 */
	public void setSubDisplay(SurfaceHolder sh);

	/**
	 * 设置sub id，显示指定的字幕
	 * 
	 * @param subid
	 * @return 0:success -1:failed
	 */
	public int setSubId(int subid);
	
	/* BEGIN: Added by c00224451 for  AR-0000698413 外挂字幕  2014/2/24 */
	/**
     * 设置指定的外置字幕
     * 
     * @param path，表示选择的字幕路径
     * @return 0:success -1:failed
     */
    public int setSubPath(String path);
    /* END: Added by c00224451 for  AR-0000698413 外挂字幕  2014/2/24 */

	int getCurrentSndId();

	boolean isSubtitleShowing();

	// 使能subtitle
	int enableSubtitle(int enable);

	/**
	 * 设置ｓｕｒｆａｃｅ宽高 <功能详细描述>
	 * 
	 * @param w
	 * @param h
	 * @return 成功　返回０；否则失败
	 * @see [类、类#方法、类#成员]
	 */
	int setScreenScale(int w, int h);

	/**
	 * 设置速度 <功能详细描述>
	 * 
	 * @param i
	 * @see [类、类#方法、类#成员]
	 */
	int setSpeed(int i);
	
	// zkf61715
	int getBufferSizeStatus();
	int getBufferTimeStatus();

	void setDataSource(String path) throws IllegalArgumentException, SecurityException, IllegalStateException, IOException;

    void setDataSource(Context context, Uri path) throws IllegalArgumentException, SecurityException, IllegalStateException, IOException;

	void reset();

	void prepareAsync();

	void pause();

	void setOnBufferingUpdateListener(Object object);

	void setOnCompletionListener(Object object);

	void setOnErrorListener(Object object);

	void setOnInfoListener(Object object);

	void setOnPreparedListener(Object object);

	void setOnSeekCompleteListener(Object object);

	void setOnVideoSizeChangedListener(Object object);
	
	void release();
	
	void start();
	
	void stop();
	
	void seekTo(int seek);
	
	/**
	 * extra interface
	 * @param request
	 * @param reply
	 * @return
	 */
	int invoke(Parcel request, Parcel reply);


	boolean setAudioChannelMode(int channelMode);

	boolean isDolbyEnabled();
	
	/**
	 * 获取系统自带的MediaPlayer
	 * @return
	 */
	MediaPlayer getOriginMediaPlayer();
	
	public static final int MEDIAPLAYER_GET_WHETHER_DOBLY = 972;
}