/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Filename:    DLNAService.java  
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2012-4-1 上午09:32:40  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2012-4-1      fxw         1.0         create
*******************************************************************/   


package com.rockchip.mediacenter;

import java.util.ArrayList;
import java.util.List;

import com.rockchip.mediacenter.common.logging.Log;
import com.rockchip.mediacenter.common.logging.LogFactory;
import com.rockchip.mediacenter.core.dlna.DLNADevice;
import com.rockchip.mediacenter.core.dlna.DLNAManagerService;
import com.rockchip.mediacenter.core.dlna.LocalResourceConfiguration;
import com.rockchip.mediacenter.core.dlna.enumeration.DLNAType;
import com.rockchip.mediacenter.core.dlna.enumeration.DeviceType;
import com.rockchip.mediacenter.dlna.MediaCenterServiceImpl;
import com.rockchip.mediacenter.dlna.model.DeviceEntityUtils;
import com.rockchip.mediacenter.dlna.model.DeviceItem;
import com.rockchip.mediacenter.core.upnp.Device;
import com.rockchip.mediacenter.core.upnp.DeviceCache;
import com.rockchip.mediacenter.core.upnp.DeviceList;
import com.rockchip.mediacenter.core.upnp.device.DeviceChangeListener;
import com.rockchip.mediacenter.core.util.LocalStorageProvider;
import com.rockchip.mediacenter.dlna.dmc.DigitalMediaController;
import com.rockchip.mediacenter.dlna.dmd.DigitalMediaDownloader;
import com.rockchip.mediacenter.dlna.dmp.DigitalMediaPlayer;
import com.rockchip.mediacenter.dlna.dmp.impl.DigitalMediaPlayerImpl;
import com.rockchip.mediacenter.dlna.dmt.DigitalMediaTransfer;
import com.rockchip.mediacenter.dlna.dmu.DigitalMediaUploader;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;

/**
 *
 * @author fxw
 * @since 1.0
 */
public class DLNAService extends Service implements DeviceChangeListener {
	
	public static Log logger = LogFactory.getLog(DLNAService.class);
	public static final String ACTION_MEDIA_CENTER = "com.rockchip.mediacenter.action.MediaCenterService";
	public static final String ACTION_RESTART_DEVICE = "com.rockchip.mediacenter.action.RestartDevice";
	public static final String ACTION_ADD_RENDER_DEVICE = "com.rockchip.mediacenter.action.AddRenderDevice";
	public static final String ACTION_ADD_SERVER_DEVICE = "com.rockchip.mediacenter.action.AddServerDevice";
	public static final String ACTION_REMOVE_RENDER_DEVICE = "com.rockchip.mediacenter.action.RemoveRenderDevice";
	public static final String ACTION_REMOVE_SERVER_DEVICE = "com.rockchip.mediacenter.action.RemoveServerDevice";
	public static final String KEY_DEVICE = "deviceItem";
	public static final String KEY_IS_LOCAL = "isLocalDevice";
	public static final int CMD_START_SEARCH = 100;
	
	private IBinder binder;
	private DLNAManagerService managerService;
	private LocalResourceConfiguration resourceConfiguration;
	private boolean isControlPointRunning;
	private boolean handleFirstConnected;
	private HandlerThread mAsyncThread;
	private Handler mNetworkHandler = null;
	
	/** 
	 * <p>Title: onCreate</p> 
	 * <p>Description: </p>  
	 * @see android.app.Service#onCreate() 
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		mAsyncThread = new HandlerThread("Async Service");
		mAsyncThread.start();
		mNetworkHandler = new Handler(mAsyncThread.getLooper());
		
		managerService = new DLNAManagerService();
		resourceConfiguration = getResourceConfiguration();
		managerService.initContext(this, resourceConfiguration);

		IntentFilter filter = new IntentFilter();
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		filter.addAction(SystemDeviceService.ACTION_TETHER_STATE_CHANGED);
		filter.addAction(SystemDeviceService.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		registerReceiver(mMediaNetworkBroadCast, filter);
	}
	
	/** 
	 * <p>Title: onStartCommand</p> 
	 * <p>Description: </p> 
	 * @param intent
	 * @param flags
	 * @param startId
	 * @return 
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int) 
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(intent!=null&&intent.hasExtra(SystemDeviceService.KEY_CMD)){
			int cmd = intent.getIntExtra(SystemDeviceService.KEY_CMD, -1);
			switch(cmd){
			case SystemDeviceService.CMD_UPDATE_CONFIG:
				removeControlpointCache();
				break;
			case CMD_START_SEARCH://启动搜索
				getMediaRendererDevice(false);
				break;
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}
	
	public void removeControlpointCache(){
		if(managerService.containService(DLNAType.DigitalMediaPlayer)){
			getDigitalMediaPlayer().removeAllDevice();
			logger.debug("Remove cache. ");
		}
	}
	
	/** 
	 * <p>Title: onDestroy</p> 
	 * <p>Description: </p>  
	 * @see android.app.Service#onDestroy() 
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mMediaNetworkBroadCast);
		managerService.shutdown();
		if(mAsyncThread!=null) mAsyncThread.quit();
		logger.debug("Shutdown controlpoint service. ");
	}
	
	/**
	 * Get the list of current renderer device
	 * @param hasLocalDevice whether contains local device
	 * @return the list of current renderer device
	 */
	public List<DeviceItem> getMediaRendererDevice(boolean hasLocalDevice){
		DigitalMediaPlayer dmp = getDigitalMediaPlayer();
		dmp.addDeviceChangeListener(this);
		dmp.search();
		DeviceList deviceList = DeviceCache.getInstance().getDeviceList();
		List<DeviceItem> deviceItemList = new ArrayList<DeviceItem>();
		for(int i=0; i<deviceList.size(); i++){
			Device device = deviceList.getDevice(i);
			if(isRenderDevice(device)){
				if(!hasLocalDevice&&isLocalDevice(device)){
					continue;
				}
				deviceItemList.add(DeviceEntityUtils.getDeviceItem(device));
			}
		}
		return deviceItemList;
	}
	
	/**
	 * Get the list of current server device
	 * @param hasLocalDevice whether contains local device
	 * @return the list of current server device
	 */
	public List<DeviceItem> getMediaServerDevice(boolean hasLocalDevice){
		DigitalMediaPlayer dmp = getDigitalMediaPlayer();
		dmp.addDeviceChangeListener(this);
		dmp.search();
		DeviceList deviceList = DeviceCache.getInstance().getDeviceList();
		List<DeviceItem> deviceItemList = new ArrayList<DeviceItem>();
		for(int i=0; i<deviceList.size(); i++){
			Device device = deviceList.getDevice(i);
			if(isServerDevice(device)){
				if(!hasLocalDevice&&isLocalDevice(device)){
					continue;
				}
				deviceItemList.add(DeviceEntityUtils.getDeviceItem(device));
			}
			getEmbeddedMediaServerDevice(deviceItemList, device, hasLocalDevice);
		}
		
		return deviceItemList;
	}
	private void getEmbeddedMediaServerDevice(List<DeviceItem> deviceItemList, Device parentDevice, boolean hasLocalDevice){
		DeviceList deviceList = parentDevice.getDeviceList();
		for(int i=0; i<deviceList.size(); i++){
			Device device = deviceList.getDevice(i);
			if(isServerDevice(device)){
				if(!hasLocalDevice&&isLocalDevice(device)){
					continue;
				}
				deviceItemList.add(DeviceEntityUtils.getDeviceItem(device));
			}
			getEmbeddedMediaServerDevice(deviceItemList, device, hasLocalDevice);
		}
	}
	
	/**
	 * It will start a control point, and then return a instance of DigitalMediaPlayer.
	 * @return the instance of DigitalMediaPlayer
	 * @see DigitalMediaPlayerImpl
	 */
	public DigitalMediaPlayer getDigitalMediaPlayer(){
		isControlPointRunning = true;
		return managerService.getDigitalMediaPlayer();
	}
	
	/**
	 * It will start the DigitalMediaPlayer(if it's not started by
	 *  {@link #getDigitalMediaPlayer()} or
	 *  {@link #getDigitalMediaUploader()} or
	 *  {@link #getDigitalMediaDownloader()}
	 * ) and the DigitalMediaController. 
	 * It equivalent to call {@link #getDigitalMediaPlayer()} at first.
	 * if you want to stop all, you need call {@link DigitalMediaPlayer#stop()} too.
	 * @return the instance of DigitalMediaController
	 */
	public DigitalMediaController getDigitalMediaController(){
		isControlPointRunning = true;
		return managerService.getDigitalMediaController();
	}
	
	/**
	 * It will start the DigitalMediaPlayer(if it's not started by
	 *  {@link #getDigitalMediaPlayer()} or
	 *  {@link #getDigitalMediaUploader()} or
	 *  {@link #getDigitalMediaController()}
	 * ) and the DigitalMediaController. 
	 * It equivalent to call {@link #getDigitalMediaPlayer()} at first.
	 * if you want to stop all, you need call {@link DigitalMediaPlayer#stop()} too.
	 * @return the instance of DigitalMediaDownloader
	 */
	public DigitalMediaDownloader getDigitalMediaDownloader(){
		isControlPointRunning = true;
		return managerService.getDigitalMediaDownloader();
	}
	
	/**
	 * It will start the DigitalMediaPlayer(if it's not started by
	 *  {@link #getDigitalMediaPlayer()} or
	 *  {@link #getDigitalMediaDownloader()} or
	 *  {@link #getDigitalMediaController()}
	 * ) and the DigitalMediaController. 
	 * It equivalent to call {@link #getDigitalMediaPlayer()} at first.
	 * if you want to stop all, you need call {@link DigitalMediaPlayer#stop()} too.
	 * @return the instance of DigitalMediaUploader
	 */
	public DigitalMediaUploader getDigitalMediaUploader(){
		isControlPointRunning = true;
		return managerService.getDigitalMediaUploader();
	}
	
	public DigitalMediaTransfer getDigitalMediaTransfer(){
		return managerService.getDigitalMediaTransfer();
	}
	
	/**
	 * Close system all resource
	 */
	public void shutdown(){
		isControlPointRunning = false;
		managerService.shutdown();
	}
	
	/** 
	 * <p>Title: onBind</p> 
	 * <p>Description: </p> 
	 * @param intent
	 * @return 
	 * @see android.app.Service#onBind(android.content.Intent) 
	 */
	@Override
	public IBinder onBind(Intent intent) {
		if(intent!=null&&ACTION_MEDIA_CENTER.equals(intent.getAction())){
			return new MediaCenterServiceImpl(this);
		}else{
			if(binder==null)
				binder = new ServiceBinder();
			return binder;
		}
	}
	
	public class ServiceBinder extends Binder {
		public DLNAService getService(){
			return DLNAService.this;
		}
	}

	/** 
	 * <p>Title: deviceAdded</p> 
	 * <p>Description: </p> 
	 * @param dev 
	 * @see com.rockchip.mediacenter.core.upnp.device.DeviceChangeListener#deviceAdded(com.rockchip.mediacenter.core.upnp.Device) 
	 */
	@Override
	public void deviceAdded(Device dev) {
		deviceAddedWithEmbbed(dev);
	}
	//Handle with embbed device
	private void deviceAddedWithEmbbed(Device dev){
		Intent intent = new Intent();
		DeviceItem deviceItem = DeviceEntityUtils.getDeviceItem(dev);//new DeviceItem(dev);
		if(isServerDevice(dev)){
			intent.setAction(ACTION_ADD_SERVER_DEVICE);
			intent.putExtra(KEY_DEVICE, deviceItem);
			intent.putExtra(KEY_IS_LOCAL, deviceItem.isLocalDevice());
			sendBroadcast(intent);
		}else if(isRenderDevice(dev)){
			intent.setAction(ACTION_ADD_RENDER_DEVICE);
			intent.putExtra(KEY_DEVICE, deviceItem);
			intent.putExtra(KEY_IS_LOCAL, deviceItem.isLocalDevice());
			sendBroadcast(intent);
		}
		DeviceList deviceList = dev.getDeviceList();
		for(Device newDev : deviceList){
			deviceAddedWithEmbbed(newDev);
		}
	}

	/** 
	 * <p>Title: deviceRemoved</p> 
	 * <p>Description: </p> 
	 * @param dev 
	 * @see com.rockchip.mediacenter.core.upnp.device.DeviceChangeListener#deviceRemoved(com.rockchip.mediacenter.core.upnp.Device) 
	 */
	@Override
	public void deviceRemoved(Device dev) {
		deviceRemovedWithEmbbed(dev);
	}
	private void deviceRemovedWithEmbbed(Device dev){
		Intent intent = new Intent();
		DeviceItem deviceItem = DeviceEntityUtils.getDeviceItem(dev);
		if(isServerDevice(dev)){
			intent.setAction(ACTION_REMOVE_SERVER_DEVICE);
			intent.putExtra(KEY_DEVICE, deviceItem);
			intent.putExtra(KEY_IS_LOCAL, deviceItem.isLocalDevice());
			sendBroadcast(intent);
		}else if(isRenderDevice(dev)){
			intent.setAction(ACTION_REMOVE_RENDER_DEVICE);
			intent.putExtra(KEY_DEVICE, deviceItem);
			intent.putExtra(KEY_IS_LOCAL, deviceItem.isLocalDevice());
			sendBroadcast(intent);
		}
		DeviceList deviceList = dev.getDeviceList();
		for(Device newDev : deviceList){
			deviceRemovedWithEmbbed(newDev);
		}
	}
	
	// whether media renderer device
	public static boolean isRenderDevice(Device device){
		return DeviceType.getDeviceType(device.getDeviceType())==DeviceType.MediaRender;
	}
	// whether media server device
	public static boolean isServerDevice(Device device){
		return DeviceType.getDeviceType(device.getDeviceType())==DeviceType.MediaServer;
	}
	// whether the local device
	public static boolean isLocalDevice(Device device){
		return DLNADevice.isLocalDevice(device);
	}
	
	//Upload Download config
	private LocalResourceConfiguration getResourceConfiguration(){
		LocalResourceConfiguration config = new LocalResourceConfiguration();
		config.setUploadSavePath(LocalStorageProvider.getUploadLocalPath(this));
		config.setDownloadSavePath(LocalStorageProvider.getDownloadLocalPath(this));
		return config;
	}
	
	/**
     * Listen network
     */
    private BroadcastReceiver mMediaNetworkBroadCast = new BroadcastReceiver(){
		public void onReceive(Context context, Intent intent) {
			if(mNetworkHandler==null) return;
			mNetworkHandler.removeCallbacks(mNetworkChangeAction);
			if(ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())){
				NetworkDetecting networkDetecting = new NetworkDetecting(DLNAService.this);
				boolean isConnected = networkDetecting.isConnect();
				if(isConnected){
					mNetworkHandler.postDelayed(mNetworkChangeAction, 100);
				}
			}else if(SystemDeviceService.ACTION_TETHER_STATE_CHANGED.equals(intent.getAction())){
				mNetworkHandler.postDelayed(mNetworkChangeAction, 2000);
			}else if(SystemDeviceService.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(intent.getAction())){
				NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
				if(networkInfo!=null&&networkInfo.isConnected()){
					mNetworkHandler.removeCallbacks(mNetworkChangeAction);
					mNetworkHandler.postDelayed(mNetworkChangeAction, 2000);
				}
			}
		}
    };
    //Handle when network changed
    private Runnable mNetworkChangeAction = new Runnable(){
		public void run() {
			NetworkDetecting networkDetecting = new NetworkDetecting(DLNAService.this);
			boolean isConnected = networkDetecting.isConnect();
			if(isConnected){
				if(handleFirstConnected){//已处理完第一次连接, 再次收到信连接则重启
					if(isControlPointRunning){
						getDigitalMediaPlayer().restart();
						Intent restartIntent = new Intent(ACTION_RESTART_DEVICE);
						sendBroadcast(restartIntent);
					}
				}else{//处理第一次连接
					handleFirstConnected = true;
				}
			}
		}
	};
}
