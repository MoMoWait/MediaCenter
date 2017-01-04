package com.rockchips.mediacenter.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import momo.cn.edu.fjnu.androidutils.utils.NetWorkUtils;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;
import com.rockchips.mediacenter.bean.LocalDevice;
import com.rockchips.mediacenter.bean.NFSInfo;
import com.rockchips.mediacenter.bean.SmbInfo;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.data.ConstData.ESearchType;
import com.rockchips.mediacenter.modle.db.LocalDeviceService;
import com.rockchips.mediacenter.modle.db.LocalMediaFileService;
import com.rockchips.mediacenter.modle.db.LocalMediaFolderService;
import com.rockchips.mediacenter.modle.db.ScanDirectoryService;
import com.rockchips.mediacenter.modle.db.UpnpFileService;
import com.rockchips.mediacenter.modle.db.UpnpFolderService;
import com.rockchips.mediacenter.util.MediaFileUtils;
import com.rockchips.mediacenter.util.MountUtils;
import com.rockchips.mediacenter.util.StorageUtils;

import android.R.integer;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.AsyncTask.Status;
import android.os.Process;
import android.os.storage.StorageManager;
import android.os.storage.StorageEventListener;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

/**
 * 设备监视服务，设备上下线监听
 * 
 * @author GaoFei
 * 
 */
public class DeviceMonitorService extends Service {

	public static final String TAG = DeviceMonitorService.class.getSimpleName();
	public static final int DELAY_MESSAGE_TIME = 2000;
	private MonitorBinder mBinder;
	private StorageManager mStorageManager;
	private MountListener mountListener;
	private Map<String, Boolean> mountMsgs = new HashMap<String, Boolean>();
	/**
	 * 当前正在处理的消息
	 */
	private Map<String, Boolean> mCurrProcessMsgs = new HashMap<String, Boolean>();
	private ThreadPoolExecutor mScanDeviceService;
	/**
	 * 单线程池服务，用于挂载Samba设备，NFS设备
	 */
	private ExecutorService mMountNetWorkDeviceService;
	private MountThread mountThread;
	private boolean isMountRuning = true;
	private List<NFSInfo> mNFSList;
	private List<SmbInfo> mSmbList;
	/**
	 * 网络设备挂载监听器
	 */
	private NetWorkDeviceMountReceiver mNetWorkDeviceMountReceiver;
	/**
	 * 获取接UPNP口服务
	 */
	private AndroidUpnpService mUpnpService;
	/**
	 * UPNP设备上下线监听器
	 */
	private UpnpRegistryListener mRegistryListener;
	/**
	 * DLNA设备监听绑定器
	 */
	private ServiceConnection mUpnpConnection;
	/**
	 * 设备上下线处理
	 */
	private MountDeviceHandler mDeviceHandler;
	
	/**
	 * 异步执行刷新所有设备
	 */
	private AsyncTask<String, Integer, Integer> mRefreshAllTask;
	/**
	 * 远程设备Map表 
	 */
	private Map<String, RemoteDevice> mRemoteDevices = Collections.synchronizedMap(new HashMap<String, RemoteDevice>());
	
	/**
	 * Upnp设备搜索器
	 */
	private AsyncTask<String, Integer, Integer> mUpnpSearchTask;
	
	/**
	 * 是否有网络
	 */
	private boolean isHaveNetwork;
	/**
	 * 设备上下线消息
	 * @author GaoFei
	 *
	 */
	interface DeviceMountMsgs{
		int DEVICE_UP = 1;
		int DEVICE_DOWN = 2;
	}
	
	@Override
	public void onCreate() {
		//Log.i(TAG, "onCreate");
		initData();
		attachService();
		initStorage();
		initEvent();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//Log.i(TAG, "onStartCommand");
		attachService();
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		if (mBinder == null)
			mBinder = new MonitorBinder();
		return mBinder;
	}

	@Override
	public void onDestroy() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mNetWorkDeviceMountReceiver);
		if (mStorageManager != null)
			mStorageManager.unregisterListener(mountListener);
		unBindServices();
		isMountRuning = false;
		
	}

	/**
	 * 初始化数据
	 */
	private void initData() {
		mMountNetWorkDeviceService = Executors.newSingleThreadExecutor();
		mNetWorkDeviceMountReceiver = new NetWorkDeviceMountReceiver();
		mDeviceHandler = new MountDeviceHandler();
		mRegistryListener = new UpnpRegistryListener();
		mBinder = new MonitorBinder();
		mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
		mountListener = new MountListener();
		mScanDeviceService = new ThreadPoolExecutor(5, 5, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
		mountThread = new MountThread();

		mUpnpConnection = new ServiceConnection() {

			@Override
			public void onServiceDisconnected(ComponentName name) {
				//Log.i(TAG, "DeviceMonitorService->component name:" + name.getClass().getName());
				//Log.i(TAG, "DeviceMonitorService->mUpnpConnection onServiceDisconnected:" + name);
			}

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				//Log.i(TAG, "DeviceMonitorService->mUpnpConnection on ServiceConnected");
				mUpnpService = (AndroidUpnpService) service;
				if(mUpnpSearchTask != null && mUpnpSearchTask.getStatus() == Status.RUNNING)
					mUpnpSearchTask.cancel(true);
				mUpnpSearchTask = new AsyncTask<String, Integer, Integer>(){
					protected  Integer doInBackground(String[] params) {
						//删除相关数据
						LocalDeviceService localDeviceService = new LocalDeviceService();
						List<LocalDevice> allUpnpDevices = localDeviceService.getAllUpnpDevices();
						UpnpFolderService upnpFolderService = new UpnpFolderService();
						UpnpFileService upnpFileService = new UpnpFileService();
						if(allUpnpDevices != null && allUpnpDevices.size() > 0){
							for(LocalDevice upnpDevice : allUpnpDevices){
								synchronized (mCurrProcessMsgs) {
									mCurrProcessMsgs.put(upnpDevice.getMountPath(), false);
								}
								localDeviceService.delete(upnpDevice);
								upnpFolderService.deleteFoldersByDeviceId(upnpDevice.getDeviceID());
								upnpFileService.deleteFilesByDeviceId(upnpDevice.getDeviceID());
							}
						}
						return ConstData.TaskExecuteResult.SUCCESS;
					};
					
					protected void onPostExecute(Integer result) {
						mUpnpService.getRegistry().addListener(mRegistryListener);
						mUpnpService.getControlPoint().search();
					};
				};
				mUpnpSearchTask.execute();
				
			}
		};
	}

	/**
	 * 初始化监听事件
	 */
	private void initEvent() {
		mStorageManager.registerListener(mountListener);
		mountThread.start();
		//注册NFS，Samba设备挂载请求
		IntentFilter netWorkDeviceMountFilter = new IntentFilter();
		netWorkDeviceMountFilter.addAction(ConstData.BroadCastMsg.NFS_MOUNT);
		netWorkDeviceMountFilter.addAction(ConstData.BroadCastMsg.SAMBA_MOUNT);
		netWorkDeviceMountFilter.addAction(ConstData.BroadCastMsg.REFRESH_NETWORK_DEVICE);
		netWorkDeviceMountFilter.addAction(ConstData.BroadCastMsg.REFRESH_ALL_DEVICES);
		netWorkDeviceMountFilter.addAction(ConstData.BroadCastMsg.CHECK_NETWORK);
		LocalBroadcastManager.getInstance(this).registerReceiver(mNetWorkDeviceMountReceiver, netWorkDeviceMountFilter);
	}

	/**
	 * 初始化本地存储
	 */
	private void initStorage(){
		initLocalDevices();
		initNetWorkDevices();
	}
	
	private void initLocalDevices(){
		//重新挂载SD卡，U盘
		//挂载这些设备是耗时操作，需要开启线程
		LocalDeviceMountThread localDeviceMountThread = new LocalDeviceMountThread(this);
		mScanDeviceService.execute(localDeviceMountThread);
	}
	
	private void initNetWorkDevices(){
		//重新挂载Samba,NFS设备
		//挂载samba,NFS设备是耗时操作，需要开启线程操作
		NetWorkDeviceMountThread deviceMountThread = new NetWorkDeviceMountThread(this, ConstData.NetWorkDeviceType.DEVICE_ALL);
		mScanDeviceService.execute(deviceMountThread);
	}
	
	/**
	 * 处理U盘，SD卡，移动硬盘，Samba设备，NFS设备，DLNA设备的挂载，卸载信息
	 * @param path
	 * @param state
	 * @param deviceType
	 */
	public synchronized void processMountMsg(String path, String state, int deviceType) {
		//Log.i(TAG, "processMountMsg->path:" + path);
		//Log.i(TAG, "processMountMsg->state:" + state);
		synchronized (mountMsgs) {
			LocalDeviceService localDeviceService = new LocalDeviceService();
			LocalMediaFileService mediaFileService = new LocalMediaFileService();
			LocalMediaFolderService folderService = new LocalMediaFolderService();
			ScanDirectoryService scanDirectoryService = new ScanDirectoryService();
			Message message = new Message();
			/**
			 * 根据挂载路径搜索数据库对应的设备
			 */
			LocalDevice device = localDeviceService.getDeviceByPath(path);
			localDeviceService.deleteDeviceByPath(path);
			if (device != null) {
				/** 删除该设备对应的文件夹 */
				folderService.deleteFoldersByPhysicId(device.getPhysic_dev_id());
				/** 删除该设备对应的文件 */
				mediaFileService.deleteFilesByPhysicId(device.getPhysic_dev_id());
				/**
				 * 删除扫描缓存目录
				 */
				scanDirectoryService.deleteDirectoriesByDeviceId(device.getDeviceID());
			}
			
			if (state.equals(Environment.MEDIA_MOUNTED)) {
				device = MediaFileUtils.getLocalDeviceFromFile(new File(path));
				if(device != null){
					localDeviceService.save(device);
					message.what = DeviceMountMsgs.DEVICE_UP;
				}
				
			}else{
				message.what = DeviceMountMsgs.DEVICE_DOWN;
			}
			message.arg1 = deviceType;
			message.obj = path;
			mDeviceHandler.sendMessageDelayed(message, DELAY_MESSAGE_TIME);
			mountMsgs.put(path, state.equals(Environment.MEDIA_MOUNTED));
		}

	}

	/**
	 * 处理NFS设备消息挂载信息
	 * 
	 * @param nfsList
	 */
	public synchronized void processNFSDevicesMountMsg(List<NFSInfo> nfsList) {
		mNFSList = nfsList;
		if (nfsList != null && nfsList.size() > 0) {
			LocalDeviceService deviceService = new LocalDeviceService();
			for (NFSInfo itemInfo : nfsList) {
				LocalDevice nfsDevice = deviceService.getDeviceByPath(itemInfo
						.getLocalMountPath());
				if (nfsDevice == null
						&& MountUtils.isMountSuccess(itemInfo.getNetWorkPath(),
								itemInfo.getLocalMountPath()))
					processMountMsg(itemInfo.getLocalMountPath(),
							Environment.MEDIA_MOUNTED, ConstData.DeviceType.DEVICE_TYPE_NFS);
				else if (nfsDevice != null
						&& !MountUtils.isMountSuccess(
								itemInfo.getNetWorkPath(),
								itemInfo.getLocalMountPath())) {
					processMountMsg(itemInfo.getLocalMountPath(),
							Environment.MEDIA_UNMOUNTED, ConstData.DeviceType.DEVICE_TYPE_NFS);
				} else {

				}
			}
		}
	}

	/**
	 * 处理Samba设备挂载信息
	 * 
	 * @param smbList
	 */
	public synchronized void processSambaDevicesMountMsg(List<SmbInfo> smbList) {
		mSmbList = smbList;
		if (smbList != null && smbList.size() > 0) {
			LocalDeviceService deviceService = new LocalDeviceService();
			for (SmbInfo itemInfo : smbList) {
				LocalDevice sambaDevice = deviceService
						.getDeviceByPath(itemInfo.getLocalMountPath());
				//Log.i(TAG,
				//		"processSambaDevicesMountMsg->itemInfo->localMountPath:"
				//				+ itemInfo.getLocalMountPath());
				if (sambaDevice == null
						&& MountUtils.isMountSuccess(itemInfo.getNetWorkPath(),
								itemInfo.getLocalMountPath())) {
					processMountMsg(itemInfo.getLocalMountPath(),
							Environment.MEDIA_MOUNTED, ConstData.DeviceType.DEVICE_TYPE_SMB);
				} else if (sambaDevice != null
						&& !MountUtils.isMountSuccess(
								itemInfo.getNetWorkPath(),
								itemInfo.getLocalMountPath())) {
					processMountMsg(itemInfo.getLocalMountPath(),
							Environment.MEDIA_UNMOUNTED, ConstData.DeviceType.DEVICE_TYPE_SMB);
				} else {

				}
			}
		}
	}

	public  boolean isMounted(String path) {
		synchronized (mCurrProcessMsgs) {
			if(mCurrProcessMsgs.get(path) == null)
				return false;
			return mCurrProcessMsgs.get(path);
		}
		
	}

	/**
	 * 搜索UPNP设备
	 */
	public void searchUpnpDevice(){
		//Log.i(TAG, "searchUpnpService");
		//数据库操作，这里需要启动线程进行处理
		new AsyncTask<Void, Integer, Integer>() {
			@Override
			protected Integer doInBackground(Void... params) {
				//Log.i(TAG, "searchUpnpService->doInBackground");
				LocalDeviceService localDeviceService = new LocalDeviceService();
				List<LocalDevice> allUpnpDevices = localDeviceService.getAllUpnpDevices();
				UpnpFolderService upnpFolderService = new UpnpFolderService();
				UpnpFileService upnpFileService = new UpnpFileService();
				if(allUpnpDevices != null && allUpnpDevices.size() > 0){
					for(LocalDevice upnpDevice : allUpnpDevices){
						synchronized (mCurrProcessMsgs) {
							mCurrProcessMsgs.put(upnpDevice.getMountPath(), false);
						}
						localDeviceService.delete(upnpDevice);
						upnpFolderService.deleteFoldersByDeviceId(upnpDevice.getDeviceID());
						upnpFileService.deleteFilesByDeviceId(upnpDevice.getDeviceID());
					}
				}
				Message message = new Message();
				message.what = DeviceMountMsgs.DEVICE_DOWN;
				message.arg1 = ConstData.DeviceType.DEVICE_TYPE_DMS;
				mDeviceHandler.sendMessageDelayed(message, DELAY_MESSAGE_TIME);
				return null;
			}
			
			@Override
			protected void onPostExecute(Integer result) {
				//Log.i(TAG, "searchUpnpService->onPostExecute");
				unBindServices();
				try{
					mUpnpService.getConfiguration().shutdown();
					mUpnpService.get().shutdown();
					mUpnpService.getRegistry().shutdown();
				}catch (Exception e){
					
				}
				
				attachService();
			}
		}.execute();
		
	}
	
	/**
	 * 刷新所有设备
	 */
	public void refreshAllDevices(){
		//Log.i(TAG, "refreshAllDevices");
		//先停止正在扫描的线程,这里使用AsyncTask
		synchronized (mCurrProcessMsgs) {
			Set<String> processPaths = mCurrProcessMsgs.keySet();
			if(processPaths != null && processPaths.size() > 0){
				Iterator<String> processIterator = processPaths.iterator();
				while(processIterator.hasNext()){
					mCurrProcessMsgs.put(processIterator.next(), false);
				}
			}
		}
		if(mRefreshAllTask != null && mRefreshAllTask.getStatus() == Status.RUNNING)
			mRefreshAllTask.cancel(true);
		mRefreshAllTask = new AsyncTask<String, Integer, Integer>(){
			protected  Integer doInBackground(String[] params) {
				try{
					//等待2秒
					TimeUnit.SECONDS.sleep(2);
					while(mScanDeviceService.getActiveCount() != 0)
						TimeUnit.SECONDS.sleep(2);
					return ConstData.TaskExecuteResult.SUCCESS;
				}catch(Exception e){
					//no handle
					//Log.i(TAG, "refreshAllDevices->doInBackground:" + e);
				}
				
				return ConstData.TaskExecuteResult.FAILED;
			};
			
			@Override
			protected void onPostExecute(Integer result) {
				if(result == ConstData.TaskExecuteResult.SUCCESS){
					//Log.i(TAG, "refreshAllDevices->onPostExecute->success");
					initStorage();
					searchUpnpDevice();
				}
				
			}
		};
		
		mRefreshAllTask.execute();
	}
	
	/**
	 * 移除正在处理的消息
	 * @param path
	 */
	public void removeProcessMsg(String path){
		synchronized (mCurrProcessMsgs) {
			mCurrProcessMsgs.remove(path);
		}
	}
	/**
	 * 绑定各种服务
	 */
	private void attachService() {
		Intent upnpIntent = new Intent(this, AndroidUpnpServiceImpl.class);
		try{
			// 绑定UPNP服务
			bindService(upnpIntent, mUpnpConnection, Service.BIND_AUTO_CREATE);
		}catch (Exception e){
			//Log.i(TAG, "attachService->exception:" + e);
		}
		
	}

	/**
	 * 解绑各种服务
	 */
	private void unBindServices() {
		if(mUpnpService != null){
			mUpnpService.getRegistry().removeListener(mRegistryListener);
		}
		unbindService(mUpnpConnection);
	}
	
	
	/**
	 * 删除Upnp数据
	 */
	private void deleteUpnpDatas(RemoteDevice remoteDevice){
		LocalDeviceService localDeviceService = new LocalDeviceService();
		localDeviceService.deleteDeviceByPath(remoteDevice.getIdentity().getDescriptorURL().toString());
		
		UpnpFolderService upnpFolderService = new UpnpFolderService();
		upnpFolderService.deleteFoldersByDeviceId(remoteDevice.getIdentity().getUdn().getIdentifierString());
		
		UpnpFileService upnpFileService = new UpnpFileService();
		upnpFileService.deleteFilesByDeviceId(remoteDevice.getIdentity().getUdn().getIdentifierString());
		
		mCurrProcessMsgs.put(remoteDevice.getIdentity().getDescriptorURL().toString(), false);
	}
	
	
	/**
	 * 获取Upnp服务
	 * @return
	 */
	public AndroidUpnpService getUpnpService(){
		return mUpnpService;
	}
	
	/**
	 * 获取远程设备表
	 * @return
	 */
	public Map<String, RemoteDevice> getRemoteDevices(){
		return mRemoteDevices;
	}
	
	/**
	 * 循环监测挂载队列的数据
	 * 
	 * @author GaoFei
	 * 
	 */
	class MountThread extends Thread {
		@Override
		public void run() {
			while (isMountRuning) {
				String path = "";
				boolean isMounted = false;

				synchronized (mountMsgs) {
					if (!mountMsgs.isEmpty()) {
						path = mountMsgs.keySet().iterator().next();
						isMounted = mountMsgs.get(path);
						mountMsgs.remove(path);
					}
				}

				if (!TextUtils.isEmpty(path)) {
					mCurrProcessMsgs.put(path, isMounted);
				}

				if (isMounted) {
					// 启动文件扫描线程
					LocalDeviceService deviceService = new LocalDeviceService();
					LocalDevice device = deviceService.getDeviceByPath(path);
					//Log.i(TAG, "MountThread->run->device:" + device);
					//Log.i(TAG, "execute FileScanThread");
					mScanDeviceService.execute(new FileScanThread(
							DeviceMonitorService.this, device));
				}
				
				// processSambaDevicesMountMsg(mSmbList);
				// processNFSDevicesMountMsg(mNFSList);
				
				boolean haveNetWork = NetWorkUtils.haveInternet(DeviceMonitorService.this);
				if(isHaveNetwork != haveNetWork){
					isHaveNetwork = haveNetWork;
					searchUpnpDevice();
				}
				
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
					//Log.i(TAG, "MountThread->e:" + e);
				}

			}
		}
	}

	/**
	 * 设备挂载，卸载监听
	 * 
	 * @author GaoFei
	 * 
	 */
	class MountListener extends StorageEventListener {

		public void onUsbMassStorageConnectionChanged(boolean connected) {
			//Log.i(TAG, "onUsbMassStorageConnectionChanged->connected:"
			//		+ connected);
		}

		public void onStorageStateChanged(String path, String oldState,
				String newState) {
			//Log.i(TAG, "path =" + path + "   " + "oldState=" + oldState + "   "
			//		+ "newState=" + newState);
			if(newState.equals(Environment.MEDIA_MOUNTED) || newState.equals(Environment.MEDIA_UNMOUNTED)){
				processMountMsg(path, newState, ConstData.DeviceType.DEVICE_TYPE_SD);
			}
				
		}
	}

	public class MonitorBinder extends Binder {
		public DeviceMonitorService getMonitorService() {
			return DeviceMonitorService.this;
		}
	}

	/**
	 * Upnp设备上下线监听器
	 * 
	 * @author GaoFei
	 */
	class UpnpRegistryListener extends DefaultRegistryListener {
		LocalDeviceService localDeviceService = new LocalDeviceService();

		@Override
		public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
			if(device.getType().getType().equals("MediaServer")){
				deleteUpnpDatas(device);
				// 添加至数据库或更新数据库数据
				LocalDevice upnpDevice = MediaFileUtils.getLocalDeviceFromRemoteDevice(device);
				String friendName = device.getDetails().getFriendlyName();
				localDeviceService.saveOrUpdate(upnpDevice);
				mRemoteDevices.put(upnpDevice.getMountPath(), device);
				synchronized (mCurrProcessMsgs) {
					mCurrProcessMsgs.put(upnpDevice.getMountPath(), true);
					Message message = new Message();
					message.what = DeviceMountMsgs.DEVICE_UP;
					message.arg1 = ConstData.DeviceType.DEVICE_TYPE_DMS;
					message.obj = upnpDevice.getMountPath();
					mDeviceHandler.sendMessageDelayed(message, DELAY_MESSAGE_TIME);
					//启动扫描器
					mScanDeviceService.execute(new UpnpFileScanThread(device, DeviceMonitorService.this, mUpnpService));
				}
			
				
			}
		}

		@Override
		public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
			if(device.getType().getType().equals("MediaServer")){
				deleteUpnpDatas(device);
				//移除远程设备
				mRemoteDevices.remove(device.getIdentity().getDescriptorURL().toString());
				Message message = new Message();
				message.what = DeviceMountMsgs.DEVICE_DOWN;
				message.arg1 = ConstData.DeviceType.DEVICE_TYPE_DMS;
				message.obj = device.getIdentity().getDescriptorURL().toString();
				mDeviceHandler.sendMessageDelayed(message, DELAY_MESSAGE_TIME);
			}
			
		}
	}
	
	
	/**
	 * 设备上下线处理
	 * @author GaoFei
	 *
	 */
	class MountDeviceHandler extends Handler{
		@Override
		public void handleMessage(Message msg) {
			Intent intent = new Intent();
			intent.putExtra(ConstData.IntentKey.EXTRA_DEVICE_TYPE, msg.arg1);
			intent.putExtra(ConstData.IntentKey.EXTRA_DEVICE_PATH, (String)msg.obj);
			switch (msg.what) {
			case DeviceMountMsgs.DEVICE_UP:
				intent.setAction(ConstData.BroadCastMsg.DEVICE_UP);
				//发送设备上线广播
				LocalBroadcastManager.getInstance(DeviceMonitorService.this).sendBroadcast(intent);
				break;
			case DeviceMountMsgs.DEVICE_DOWN:
				intent.setAction(ConstData.BroadCastMsg.DEVICE_DOWN);
				//发送设备下线广播
				LocalBroadcastManager.getInstance(DeviceMonitorService.this).sendBroadcast(intent);
				break;
			default:
				break;
			}
		}
	}

	
	/**
	 * 网络设备挂载接受请求
	 * @author GaoFei
	 *
	 */
	class NetWorkDeviceMountReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			//Log.i(TAG, "NetWorkDeviceMountReceiver->receive action:" + action);
			if(action.equals(ConstData.BroadCastMsg.NFS_MOUNT)){
				NFSInfo nfsInfo = (NFSInfo)intent.getSerializableExtra(ConstData.IntentKey.EXTRA_NFS_INFO);
				NFSDeviceMountThread nfsDeviceMountThread = new NFSDeviceMountThread(DeviceMonitorService.this, nfsInfo);
				mMountNetWorkDeviceService.execute(nfsDeviceMountThread);
			}else if(action.equals(ConstData.BroadCastMsg.SAMBA_MOUNT)){
				SmbInfo smbInfo = (SmbInfo)intent.getSerializableExtra(ConstData.IntentKey.EXTRA_SAMBA_INFO);
				SambaDeviceMountThread sambaDeviceMountThread = new SambaDeviceMountThread(DeviceMonitorService.this, smbInfo);
				mMountNetWorkDeviceService.execute(sambaDeviceMountThread);
			}else if(action.equals(ConstData.BroadCastMsg.REFRESH_NETWORK_DEVICE)){
				//刷新网络设备
				searchUpnpDevice();
			}else if(action.equals(ConstData.BroadCastMsg.REFRESH_ALL_DEVICES)){
				//刷新所有设备
				refreshAllDevices();
			}else if(action.equals(ConstData.BroadCastMsg.CHECK_NETWORK)){
				//检测网络
				if(!NetWorkUtils.haveInternet(getApplicationContext())){
					//刷新网络设备
					searchUpnpDevice();
				}
			}
		}
		
	}
}
