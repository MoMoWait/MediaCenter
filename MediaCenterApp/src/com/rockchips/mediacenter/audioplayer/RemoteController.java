package com.rockchips.mediacenter.audioplayer;

import android.content.Intent;

//远程播控接口
public interface RemoteController {
	public void stop(Intent intent);
	public void seekTo(Intent intent);
	public void pause(Intent intent);
	public void play(Intent intent);
	public void setDataSource(Intent intent);
	
	public int getDuration();
	public int getPosition();
	
	public void onRemoteDisconnect();
	
	
	public void setCallback(RemoteCallback callback) ;
}


