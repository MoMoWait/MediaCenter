package com.rockchips.mediacenter.audioplayer;

import com.rockchips.mediacenter.service.IMediaPlayerAdapter;

//远程播控接口
public interface RemoteCallback {
	public void onStop();
	public void onProgress(int pos);
	public void onPause();
	public void onPlay();
	public void onPrepared(IMediaPlayerAdapter mp) ;
	public void onCompletion(IMediaPlayerAdapter mp);
	public void onError(IMediaPlayerAdapter mp, int what, int extra);
}


