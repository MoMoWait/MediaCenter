package com.rockchips.mediacenter.service;

import android.media.MediaPlayer;

/**
 * @author GaoFei
 * 实现视频播放器相关接口
 */
public interface IVideoPlayer {
	void onBufferingUpdate(MediaPlayer mp, int percent);
	void onPrepared(MediaPlayer mp);
	void onCompletion(MediaPlayer mp);
	boolean onError(MediaPlayer mp, int framework_err, int impl_err);
	boolean onInfo(MediaPlayer mp, int arg1, int arg2);
	void onSeekComplete(MediaPlayer mp);
}
