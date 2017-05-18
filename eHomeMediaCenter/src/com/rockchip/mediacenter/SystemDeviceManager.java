/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Filename:    SystemDeviceManager.java  
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

import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

/**
 *
 * @author fxw
 * @since 1.0
 */
public class SystemDeviceManager {

	private BindListener mBindListener;
	private ISystemDeviceService mSystemDeviceService;
	private boolean isConnected;
	private Context mContext;
	
	public SystemDeviceManager(Context context){
		this.mContext = context;
	}
	
	/**
	 * 启动管理
	 * @param context
	 */
	public void startManager(){
		if(isConnectService()) return;
		Intent intent = new Intent(mContext, SystemDeviceService.class);
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
			mSystemDeviceService = ISystemDeviceService.Stub.asInterface(binder);
			if(mBindListener!=null)
				mBindListener.onBindCompleted();
			isConnected = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mSystemDeviceService = null;
			isConnected = false;
		}
	};
	
	public void setBindListener(BindListener bindListener){
		mBindListener = bindListener;
	}
	
	public interface BindListener {
		public void onBindCompleted();
	}

	/**
	 * 启动媒体服务器设备
	 * @return
	 */
	public boolean startMediaServerWithSavedConfig() {
		try {
			return mSystemDeviceService.startMediaServerWithSavedConfig();
		} catch (RemoteException e) {
			return false;
		}
	}

	/**
	 * 停止媒体服务器设备
	 * @return
	 */
	public boolean stopMediaServer() {
		try {
			return mSystemDeviceService.stopMediaServer();
		} catch (RemoteException e) {
			return false;
		}
	}

	/**
	 * 获取服务器是否允许
	 * @return
	 */
	public boolean isServerRunning(){
		try {
			return mSystemDeviceService.isServerRunning();
		} catch (RemoteException e) {
			return false;
		}
	}

	/**
	 * 查询共享目录
	 * @return
	 * @throws RemoteException
	 */
	public List<String> queryShareDirectory()  {
		try {
			return mSystemDeviceService.queryShareDirectory();
		} catch (RemoteException e) {
			return new ArrayList<String>();
		}
	}

	/**
	 * 删除共享目录
	 * @param path
	 * @return
	 */
	public boolean deleteShareDirectory(String path) {
		try {
			return mSystemDeviceService.deleteShareDirectory(path);
		} catch (RemoteException e) {
			return false;
		}
	}


	/**
	 * 添加共享目录
	 * @param path
	 * @return
	 * @throws RemoteException
	 */
	public boolean addShareDirectory(String path) {
		try {
			return mSystemDeviceService.addShareDirectory(path);
		} catch (RemoteException e) {
			return false;
		}
	}
	
	/**
	 * 根据配置修改共享策略
	 */
	public void updateContentSharePolicy() {
		try {
			mSystemDeviceService.updateContentSharePolicy();
		} catch (RemoteException e) {
		}
	}


	/**
	 * 启动播放设备
	 * @return
	 */
	public boolean startMediaRenderer() {
		try {
			return mSystemDeviceService.startMediaRenderer();
		} catch (RemoteException e) {
			return false;
		}
	}

	/**
	 * 停止播放设备
	 * @return
	 */
	public boolean stopMediaRenderer() {
		try {
			return mSystemDeviceService.stopMediaRenderer();
		} catch (RemoteException e) {
			return false;
		}
	}

	/**
	 * 播放设备是否运行
	 * @return
	 */
	public boolean isRendererRunning() {
		try {
			return mSystemDeviceService.isRendererRunning();
		} catch (RemoteException e) {
			return false;
		}
	}

	/**
	 * 配置发生改变修改配置
	 */
	public void updateDeviceConfiguration() {
		try {
			mSystemDeviceService.updateDeviceConfiguration();
		} catch (RemoteException e) {
		}
	}

	/**
	 * 关闭所有服务
	 */
	public void shutdown() {
		try {
			mSystemDeviceService.shutdown();
		} catch (RemoteException e) {
		}
	}
	
}
