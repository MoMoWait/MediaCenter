/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Filename:    DLNAManager.java  
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2012-4-6 上午09:13:38  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2012-4-6      fxw         1.0         create
*******************************************************************/   


package com.rockchip.mediacenter;

import java.util.List;

import com.rockchip.mediacenter.dlna.model.DeviceItem;
import com.rockchip.mediacenter.dlna.dmc.DigitalMediaController;
import com.rockchip.mediacenter.dlna.dmd.DigitalMediaDownloader;
import com.rockchip.mediacenter.dlna.dmp.DigitalMediaPlayer;
import com.rockchip.mediacenter.dlna.dmt.DigitalMediaTransfer;
import com.rockchip.mediacenter.dlna.dmu.DigitalMediaUploader;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

/**
 *
 * @author fxw
 * @since 1.0
 */
public class DLNAManager {

	private BindListener mBindListener;
	private DLNAService dlnaService;
	private boolean isConnected;
	private Context mContext;
	
	public DLNAManager(Context context){
		this.mContext = context;
	}
	
	public List<DeviceItem> getMediaRendererDevice(boolean hasLocalDevice){
		return dlnaService.getMediaRendererDevice(hasLocalDevice);
	}
	
	public List<DeviceItem> getMediaServerDevice(boolean hasLocalDevice){
		return dlnaService.getMediaServerDevice(hasLocalDevice);
	}
	
	public DigitalMediaPlayer getDigitalMediaPlayer(){
		return dlnaService.getDigitalMediaPlayer();
	}
	
	public DigitalMediaController getDigitalMediaController(){
		return dlnaService.getDigitalMediaController();
	}
	
	public DigitalMediaDownloader getDigitalMediaDownloader(){
		return dlnaService.getDigitalMediaDownloader();
	}
	
	public DigitalMediaUploader getDigitalMediaUploader(){
		return dlnaService.getDigitalMediaUploader();
	}
	
	public DigitalMediaTransfer getDigitalMediaTransfer(){
		return dlnaService.getDigitalMediaTransfer();
	}
	
	public void shutdown(){
		dlnaService.shutdown();
	}
	
	/**
	 * 启动管理
	 * @param context
	 */
	public void startManager(){
		if(isConnectService()) return;
		Intent intent = new Intent(mContext, DLNAService.class);
		mContext.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
		
	}
	
	/**
	 * 停止管理
	 * @param context
	 */
	public void stopManager(){
		mContext.unbindService(mServiceConnection);
	}
	
	/**
	 * 是否已连接服务
	 * @return
	 */
	public boolean isConnectService(){
		return isConnected;
	}
	
	private ServiceConnection mServiceConnection = new ServiceConnection(){
		@Override
		public void onServiceConnected(ComponentName component, IBinder binder) {
			dlnaService = ((DLNAService.ServiceBinder)binder).getService();
			if(mBindListener!=null)
				mBindListener.onBindCompleted();
			isConnected = true;
			
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			dlnaService = null;
			isConnected = false;
		}
	};
	
	public void setBindListener(BindListener bindListener){
		mBindListener = bindListener;
	}
	
	public interface BindListener {
		public void onBindCompleted();
	}
	
}
