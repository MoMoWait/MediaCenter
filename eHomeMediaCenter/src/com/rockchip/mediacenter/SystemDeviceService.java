/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Filename:    SystemDeviceService.java  
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.rockchip.mediacenter.common.logging.Log;
import com.rockchip.mediacenter.common.logging.LogFactory;
import com.rockchip.mediacenter.core.dlna.DLNAManagerService;
import com.rockchip.mediacenter.core.dlna.LocalDeviceConfiguration;
import com.rockchip.mediacenter.core.dlna.LocalResourceConfiguration;
import com.rockchip.mediacenter.core.dlna.enumeration.ContentDirectoryPolicy;
import com.rockchip.mediacenter.core.dlna.enumeration.DLNAType;
import com.rockchip.mediacenter.core.dlna.enumeration.UploadPermission;
import com.rockchip.mediacenter.core.dlna.model.DeviceInfo;
import com.rockchip.mediacenter.core.dlna.model.IconInfo;
import com.rockchip.mediacenter.core.dlna.service.contentdirectory.Directory;
import com.rockchip.mediacenter.core.dlna.service.contentdirectory.DirectoryList;
import com.rockchip.mediacenter.core.net.HostInterface;
import com.rockchip.mediacenter.core.util.LocalStorageProvider;
import com.rockchip.mediacenter.dlna.dmr.DigitalMediaRenderer;
import com.rockchip.mediacenter.dlna.dms.DigitalMediaServer;
import com.rockchip.mediacenter.mediaserver.MediaShareType;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.StrictMode;

/**
 * 
 * public static final String KEY_CMD = "command";
 * public static final int CMD_UPDATE_CONFIG = 1;//修改配置
 * public static final int CMD_START_SERVER = 2;//启动媒体服务器
 * public static final int CMD_STOP_SERVER = 3;//停止媒体服务器
 * public static final int CMD_START_RENDER = 4;//启动Renderer
 * public static final int CMD_STOP_RENDER = 5;//停止媒体服务器
 * 
 * 将DMS DMR运行在另外一个进程
 * @author fxw
 * @since 1.0
 */
public class SystemDeviceService extends Service {
	
	public static Log logger = LogFactory.getLog(SystemDeviceService.class);
	public static final boolean USE_DEVICE_EXTRA_NAME = false;
	public static final String WIFI_P2P_CONNECTION_CHANGED_ACTION = "android.net.wifi.p2p.CONNECTION_STATE_CHANGE";
	public static final String ACTION_TETHER_STATE_CHANGED = "android.net.conn.TETHER_STATE_CHANGED";
	public static final String KEY_CMD = "command";
	public static final String KEY_FRIEND_NAME = "friendlyname";
	public static final String KEY_UPLOAD_PERMISSION = "permission";
	public static final int CMD_UPDATE_CONFIG = 1;//修改配置
	public static final int CMD_START_SERVER = 2;//启动媒体服务器
	public static final int CMD_STOP_SERVER = 3;//停止媒体服务器
	public static final int CMD_START_RENDER = 4;//启动Renderer
	public static final int CMD_STOP_RENDER = 5;//停止媒体服务器
	public static final int CMD_UPDATE_NAME_EXTERNAL = 6;//外部修改设备名称
	public static final int CMD_CONN_CHANGED = 7;//网络连接改变
	private boolean handleFirstConnected = false;;//第一次连接用于处理随机启动的项
	//内部使用, 有可能网络状态改变情况下设备启动失败, 
	//从而使systemDeviceService.isServerRunning()为false,下次网络再发生变化就不会再重启
	private boolean isServerRunning = false;
	private boolean isRendererRunning = false;
	
	private SystemDeviceServiceImpl systemDeviceService;
	private DLNAManagerService managerService;
	private LocalDeviceConfiguration deviceConfiguration;
	private LocalResourceConfiguration resourceConfiguration;
	private String[] bindAddresses;
	
	/** 
	 * <p>Title: onCreate</p> 
	 * <p>Description: </p>  
	 * @see android.app.Service#onCreate() 
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		if (android.os.Build.VERSION.SDK_INT > 9) {
		    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		    StrictMode.setThreadPolicy(policy);
		}
		handleFirstConnected = false;
		managerService = new DLNAManagerService();
		resourceConfiguration = getResourceConfiguration();
		managerService.initContext(this, resourceConfiguration);
		deviceConfiguration = getDeviceConfiguration();
		managerService.setDeviceConfiguration(deviceConfiguration);
		onBind(null);
		IntentFilter filter = new IntentFilter();
		//filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		filter.addAction(WIFI_P2P_CONNECTION_CHANGED_ACTION);
		filter.addAction(ACTION_TETHER_STATE_CHANGED);
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
		if(intent!=null&&intent.hasExtra(KEY_CMD)){
			int cmd = intent.getIntExtra(KEY_CMD, -1);
			try {
				switch(cmd){
				case CMD_UPDATE_CONFIG:
					updateDeviceConfiguration(intent);
					break;
				case CMD_START_SERVER:
					systemDeviceService.startMediaServerWithSavedConfig();
					logger.debug("[onStartCommand] command to start mediaserver. ");
					break;
				case CMD_STOP_SERVER:
					systemDeviceService.stopMediaServer();
					logger.debug("[onStartCommand] command to stop mediaserver. ");
					break;
				case CMD_START_RENDER:
					systemDeviceService.startMediaRenderer();
					logger.debug("[onStartCommand] command to start mediarender. ");
					break;
				case CMD_STOP_RENDER:
					systemDeviceService.stopMediaRenderer();
					logger.debug("[onStartCommand] command to stop mediarender. ");
					break;
				case CMD_UPDATE_NAME_EXTERNAL:
					updateFriendlyNameForExternal(intent);
					break;
				case CMD_CONN_CHANGED:
					Intent connIntent = new Intent(ConnectivityManager.CONNECTIVITY_ACTION);
					connIntent.putExtra(ConnectivityManager.EXTRA_NETWORK_INFO, intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO));
					mMediaNetworkBroadCast.onReceive(this, connIntent);
					break;
				}
				
			} catch (RemoteException e) {
			}
		}
		return super.onStartCommand(intent, flags, startId);
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
	}
	
	/**
	 * Start Media Server with the saved configuration 
	 */
	public boolean startMediaServerWithSavedConfig(){
		setContentDirectoryPolicy();
		return getDigitalMediaServer().start();
	}
	
	/**
	 * Set Content Directory Policy
	 */
	public void setContentDirectoryPolicy(){
		DigitalMediaServer dms = getDigitalMediaServer();
		MediaShareType shareType = SystemSettingUtils.getMediaShareType(this);
		if(shareType==MediaShareType.FOLDER_SHARE){//Folder Share
    		dms.setContentDirectoryPolicy(ContentDirectoryPolicy.CustomDirectory);
    	}else if(shareType==MediaShareType.MEDIA_SHARE){//Media Store Share
    		dms.setContentDirectoryPolicy(ContentDirectoryPolicy.MediaStore);
    	}else{
    		dms.setContentDirectoryPolicy(null);
    	}
	}
	
	/**
	 * Update the name of Media Server and Media Renderer
	 */
	public void updateDeviceConfiguration(){
		byebyeIfNeed();
		DeviceInfo deviceInfo = new DeviceInfo();
		deviceInfo.setFriendlyName(SystemSettingUtils.getMediaServerName(this));
		deviceConfiguration.setDeviceInfo(deviceInfo);
		deviceConfiguration.setPermission(SystemSettingUtils.getMediaUploadPermission(this));
		setDeviceConfiguration();
	}
	private void updateDeviceConfiguration(Intent intent){
		byebyeIfNeed();
		if(intent.hasExtra(KEY_FRIEND_NAME)){
			DeviceInfo deviceInfo = new DeviceInfo();
			deviceInfo.setFriendlyName(SystemSettingUtils.getMediaServerName(this));
			deviceConfiguration.setDeviceInfo(deviceInfo);
		}
		if(intent.hasExtra(KEY_UPLOAD_PERMISSION)){
			boolean isAllow = intent.getBooleanExtra(KEY_UPLOAD_PERMISSION, true);
			UploadPermission permission = isAllow?UploadPermission.PERMISSION_ALLOW:UploadPermission.PERMISSION_REJECT;
			deviceConfiguration.setPermission(permission);
		}
		setDeviceConfiguration();
	}
	private void setDeviceConfiguration(){
		if(managerService.containService(DLNAType.DigitalMediaServer)){
			DigitalMediaServer dms = getDigitalMediaServer();
			dms.setDeviceConfiguration(deviceConfiguration);
			if(dms.isRunning()) dms.announce();
		}
		if(managerService.containService(DLNAType.DigitalMediaRenderer)){
			DigitalMediaRenderer dmr = getDigitalMediaRenderer();
			dmr.setDeviceConfiguration(deviceConfiguration);
			if(dmr.isRunning()) dmr.announce();
		}
		logger.debug("updateDeviceConfiguration. ");
	}
	private void byebyeIfNeed(){
		if(managerService.containService(DLNAType.DigitalMediaServer)){
			DigitalMediaServer dms = getDigitalMediaServer();
			if(dms.isRunning()) dms.byebye();
		}
		if(managerService.containService(DLNAType.DigitalMediaRenderer)){
			DigitalMediaRenderer dmr = getDigitalMediaRenderer();
			if(dmr.isRunning()) dmr.byebye();
		}
	}
	
	//Update friendly name for external app
	private void updateFriendlyNameForExternal(Intent intent){
		if(intent.hasExtra(KEY_FRIEND_NAME)){
			String friendlyName = intent.getStringExtra(KEY_FRIEND_NAME);
			logger.debug("Update friendly name from external app. Name is "+friendlyName);
			SystemSettingUtils.saveMediaServerName(this, friendlyName);
			updateDeviceConfiguration(intent);
		}
	}
	
	/**
	 * It will create a media server device
	 * @return the instance of DigitalMediaServer
	 */
	public DigitalMediaServer getDigitalMediaServer(){
		return managerService.getDigitalMediaServer();
	}
	
	/**
	 * It will create a media renderer device
	 * @return the instance of DigitalMediaRenderer
	 */
	public DigitalMediaRenderer getDigitalMediaRenderer(){
		return managerService.getDigitalMediaRenderer();
	}
	
	/**
	 * Close system all resource
	 */
	public void shutdown(){
		managerService.shutdown();
	}
	
	//Upload Download config
	private LocalResourceConfiguration getResourceConfiguration(){
		LocalResourceConfiguration config = new LocalResourceConfiguration();
		config.setUploadSavePath(LocalStorageProvider.getUploadLocalPath(this));
		config.setDownloadSavePath(LocalStorageProvider.getDownloadLocalPath(this));
		config.setImageFolderName(getString(R.string.dms_image));
		config.setAudioFolderName(getString(R.string.dms_audio));
		config.setVideoFolderName(getString(R.string.dms_video));
		return config;
	}
	
	//Device config
	private LocalDeviceConfiguration getDeviceConfiguration(){
		LocalDeviceConfiguration config = new LocalDeviceConfiguration();
		DeviceInfo deviceInfo = new DeviceInfo();
		deviceInfo.setFriendlyName(SystemSettingUtils.getMediaServerName(this));
		config.setDeviceInfo(deviceInfo);
		config.setPermission(SystemSettingUtils.getMediaUploadPermission(this));
		config.setIconList(buildIcons());
//		config.setIconInfo(buildIcon(R.drawable.icon));
		return config;
	}
	@SuppressWarnings("unused")
	private IconInfo buildIcon(int resId){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();       
		Bitmap bm = BitmapFactory.decodeResource(getResources(), resId);
	    bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		IconInfo icon = new IconInfo();
		icon.setData(baos.toByteArray());
		icon.setWidth(bm.getWidth());
		icon.setHeight(bm.getHeight());
		icon.setMimeType("image/png");
		icon.setDepth(24); 
		return icon;
	}
	private List<IconInfo> buildIcons(){
		List<IconInfo> iconList = new ArrayList<IconInfo>();
		IconInfo icon = new IconInfo("image/png", 120, 120, 24, null);
		icon.setData("/assets/icon/icon.png");
		iconList.add(icon);
		icon = new IconInfo("image/png", 48, 48, 24, null);
		icon.setData("/assets/icon/icon_s.png");
		iconList.add(icon);
		
		icon = new IconInfo("image/jpeg", 120, 120, 24, null);
		icon.setData("/assets/icon/icon.jpg");
		iconList.add(icon);
		icon = new IconInfo("image/jpeg", 48, 48, 24, null);
		icon.setData("/assets/icon/icon_s.jpg");
		iconList.add(icon);
		return iconList;
	}
	
	/** 
	 * <p>Title: onConfigurationChanged</p> 
	 * <p>Description: </p> 
	 * @param newConfig 
	 * @see android.app.Service#onConfigurationChanged(android.content.res.Configuration) 
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		resourceConfiguration.setUploadSavePath(LocalStorageProvider.getUploadLocalPath(this));
		resourceConfiguration.setDownloadSavePath(LocalStorageProvider.getDownloadLocalPath(this));
		resourceConfiguration.setImageFolderName(getString(R.string.dms_image));
		resourceConfiguration.setAudioFolderName(getString(R.string.dms_audio));
		resourceConfiguration.setVideoFolderName(getString(R.string.dms_video));
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
		if(systemDeviceService==null){
			systemDeviceService = new SystemDeviceServiceImpl();
		}
		return systemDeviceService;
	}
	
	private class SystemDeviceServiceImpl extends ISystemDeviceService.Stub {
		
		//Digital Media Server
		public boolean startMediaServerWithSavedConfig() throws RemoteException {
			boolean result = SystemDeviceService.this.startMediaServerWithSavedConfig();
			isServerRunning = true;
			return result;
		}

		public boolean stopMediaServer() throws RemoteException {
			isServerRunning = false;
			return getDigitalMediaServer().stop();
		}
		
		public boolean restartMediaServer(){
			return getDigitalMediaServer().restart();
		}

		public boolean isServerRunning() throws RemoteException {
			return getDigitalMediaServer().isRunning();
		}

		public List<String> queryShareDirectory() throws RemoteException {
			DigitalMediaServer dms = getDigitalMediaServer();
			List<String> fileInfoList = new ArrayList<String>();
	    	DirectoryList directoryList = dms.queryShareDirectory();
	    	for(Directory dir : directoryList){
	    		File file = new File(dir.getPath());
	    		if(file.exists()){
	    			fileInfoList.add(dir.getPath());
	    		}else{
	    			dms.deleteShareDirectory(dir.getPath());
	    		}
	    	}
	    	return fileInfoList;
		}

		public boolean deleteShareDirectory(String path) throws RemoteException {
			return getDigitalMediaServer().deleteShareDirectory(path);
		}

		public boolean addShareDirectory(String path) throws RemoteException {
			return getDigitalMediaServer().addShareDirectory(path);
		}
		
		public void updateContentSharePolicy() throws RemoteException {
			SystemDeviceService.this.setContentDirectoryPolicy();
		}

		//Digital Media Renderer
		public boolean startMediaRenderer() throws RemoteException {
			boolean result = getDigitalMediaRenderer().start();
			isRendererRunning = true;
			return result;
		}
		
		public boolean restartMediaRenderer() {
			return getDigitalMediaRenderer().restart();
		}

		public boolean stopMediaRenderer() throws RemoteException {
			isRendererRunning = false;
			return getDigitalMediaRenderer().stop();
		}

		public boolean isRendererRunning() throws RemoteException {
			return getDigitalMediaRenderer().isRunning();
		}

		/** 
		 * <p>Title: updateDeviceConfiguration</p> 
		 * <p>Description: </p> 
		 * @throws RemoteException 
		 * @see com.rockchip.mediacenter.ISystemDeviceService#updateDeviceConfiguration() 
		 */
		@Override
		public void updateDeviceConfiguration() throws RemoteException {
			SystemDeviceService.this.updateDeviceConfiguration();
		}

		/** 
		 * <p>Title: shutdown</p> 
		 * <p>Description: </p> 
		 * @throws RemoteException 
		 * @see com.rockchip.mediacenter.ISystemDeviceService#shutdown() 
		 */
		@Override
		public void shutdown() throws RemoteException {
			SystemDeviceService.this.shutdown();
		}

		/** 
		 * <p>Title: switchAutoRunRenderer</p> 
		 * <p>Description: </p> 
		 * @param auto
		 * @throws RemoteException 
		 * @see com.rockchip.mediacenter.ISystemDeviceService#switchAutoRunRenderer(boolean) 
		 */
		@Override
		public void switchAutoRunRenderer(boolean auto) throws RemoteException {
			SystemSettingUtils.saveMediaRendererAutoable(SystemDeviceService.this, auto);
		}
	}
	
	/**
     * Listen network
     */
	private Handler mNetworkHandler = new Handler();
    private BroadcastReceiver mMediaNetworkBroadCast = new BroadcastReceiver(){
		public void onReceive(Context context, Intent intent) {
			mNetworkHandler.removeCallbacks(mNetworkChangeAction);
			if(ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())){
				NetworkInfo networkInfo = (NetworkInfo)intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
				if(networkInfo!=null&&networkInfo.getDetailedState()!=NetworkInfo.DetailedState.CONNECTED){
					logger.debug("[mMediaNetworkBroadCast] receive Connectivity Broadcast, conecte state: "+networkInfo.getDetailedState().name());
					return;
				}
				NetworkDetecting networkDetecting = new NetworkDetecting(SystemDeviceService.this);
				boolean isConnected = networkDetecting.isConnect();
				if(isConnected||HostInterface.getNHostAddresses()>0){
					mNetworkHandler.removeCallbacks(mNetworkChangeAction);
					mNetworkHandler.postDelayed(mNetworkChangeAction, 100);
				}
			}else if(ACTION_TETHER_STATE_CHANGED.equals(intent.getAction())){
				mNetworkHandler.postDelayed(mNetworkChangeAction, 2000);
			}else if(WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(intent.getAction())){
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
			NetworkDetecting networkDetecting = new NetworkDetecting(SystemDeviceService.this);
			boolean isConnected = networkDetecting.isConnect();
			if(isConnected){
				String[] ipaddrs = getHostInterfaces();
				if(!isHostInterfaceChanged(ipaddrs)){
					logger.debug("Network changed, but ipaddress not changed. ");
					return;
				}
				if(handleFirstConnected){//已处理完第一次连接, 再次收到连接则重启
					if(isServerRunning){
						systemDeviceService.restartMediaServer();
						logger.debug("[mNetworkChangeAction] restart mediaServer when network changed. ");
					}
					if(isRendererRunning){
						systemDeviceService.restartMediaRenderer();
						logger.debug("[mNetworkChangeAction] restart mediaRender when network changed. ");
					}
					bindAddresses = getHostInterfaces();//保存IP地址， 用于判断IP地址变化
				}else{//处理第一次连接
					handleAutoRun();
					bindAddresses = getHostInterfaces();//save current local interface
					handleFirstConnected = true;
				}
			}
		}
    };
    
    //处理随机启动
    private void handleAutoRun(){
    	if(SystemSettingUtils.getMediaRendererAutoable(this)){
    		try {
				systemDeviceService.startMediaRenderer();
				logger.debug("[handleAutoRun] auto start mediarender. ");
			} catch (RemoteException e) {
			}
    	}
    	
    	if(SystemSettingUtils.getMediaServerAutoable(this)){
    		try {
				systemDeviceService.startMediaServerWithSavedConfig();
				logger.debug("[handleAutoRun] auto start mediaserver. ");
			} catch (RemoteException e) {
			}
    	}
    }
    
    //获取设备名称+设备型号
    private String getAbsFriendlyName(String devName){
    	if(USE_DEVICE_EXTRA_NAME)
    		return devName+"("+Build.MODEL+")";
    	else
    		return devName;
    }
    
    //获取当前IP地址
    private String[] getHostInterfaces(){
		int nHostAddrs = HostInterface.getNHostAddresses();
		String addresses[] = new String[nHostAddrs]; 
		for (int n=0; n<nHostAddrs; n++) {
			addresses[n] = HostInterface.getHostAddress(n);
		}
		return addresses;
    }
    //网络地址是否发生变化
    private boolean isHostInterfaceChanged(String[] addrs){
    	if(bindAddresses==null||addrs==null){
    		logger.debug("[isHostInterfaceChanged] bindAddresses/addrs is null. ");
    		return true;
    	}
    	boolean changed = false;
    	for(String addr : addrs){
    		boolean found = false;
    		for(String bindAddr : bindAddresses){
    			if(addr!=null&&addr.equals(bindAddr)){
    				found = true;
    				break;
    			}
    		}
    		if(!found){//no found
    			changed = true;
    			break;
    		}
    	}
    	if(changed){
    		for(String addr : addrs){
    			logger.debug("[New address] "+addr);
    		}
    		for(String bindAddr : bindAddresses){
    			logger.debug("[Old address] "+bindAddr);
    		}
    	}
    	return changed;
    }
}
