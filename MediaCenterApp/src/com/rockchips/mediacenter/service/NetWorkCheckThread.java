package com.rockchips.mediacenter.service;

import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import momo.cn.edu.fjnu.androidutils.utils.JsonUtils;
import momo.cn.edu.fjnu.androidutils.utils.StorageUtils;
import com.rockchips.mediacenter.bean.Device;
import com.rockchips.mediacenter.bean.NFSInfo;
import com.rockchips.mediacenter.bean.SmbInfo;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.modle.db.DeviceService;
import com.rockchips.mediacenter.utils.MountUtils;
import com.rockchips.mediacenter.utils.NetUtils;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

/**
 * @author GaoFei
 * 网络检测线程
 */
public class NetWorkCheckThread extends Thread{
	private static final String TAG = "NetWorCheckThread";
	private DeviceMonitorService mService;
	public NetWorkCheckThread(DeviceMonitorService service){
		mService = service;
	}
	
	@Override
	public void run() {
		/*ConnectivityManager cm = (ConnectivityManager)mService.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();*/
		boolean isConnected = NetUtils.isConnectNetWork();
		Log.i(TAG, "NetWorkCheckThread->isConnected:" + isConnected);
		/*Network[] allNetworks = cm.getAllNetworks();
		if(allNetworks != null && allNetworks.length > 0){
			for(Network itemNetwork : allNetworks){
				NetworkInfo itemNetworkInfo = cm.getNetworkInfo(itemNetwork);
				if(itemNetworkInfo !=  null && itemNetworkInfo.isConnected()){
					Log.i(TAG, itemNetworkInfo.getTypeName() + " is connected");
				}
			}
		}*/
		//读取Smb列表
		String smbStr = StorageUtils.getDataFromSharedPreference(ConstData.SharedKey.SMB_INFOS);
		//smb列表
		List<SmbInfo> smbInfos = null;
		if(!TextUtils.isEmpty(smbStr)){
			try {
				smbInfos = (List<SmbInfo>)JsonUtils.arrayToList(SmbInfo.class, new JSONArray(smbStr));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		DeviceService deviceService = new DeviceService();
		List<Device> allNetWorkDevices = deviceService.getAllNetWorkDevices();
		Bundle deviceBundle = null;
		if(!isConnected){
			//删除所有网络(Samba,NFS,DLNA)设备，启动DeviceMount线程
			if(allNetWorkDevices != null && allNetWorkDevices.size() > 0){
				for(Device itemDevice : allNetWorkDevices){
					//尝试卸载设备
					deviceBundle = new Bundle();
					String localMountPath = itemDevice.getLocalMountPath();
					String name = localMountPath.substring(localMountPath.lastIndexOf("/") + 1, localMountPath.length());
					deviceBundle.putString(ConstData.DeviceMountMsg.DEVICE_NAME, name);
					deviceBundle.putString(ConstData.DeviceMountMsg.MOUNT_PATH, itemDevice.getLocalMountPath());
					deviceBundle.putInt(ConstData.DeviceMountMsg.MOUNT_STATE, ConstData.DeviceMountState.DEVICE_DOWN);
					deviceBundle.putBoolean(ConstData.DeviceMountMsg.IS_FROM_NETWORK, false);
					deviceBundle.putInt(ConstData.DeviceMountMsg.MOUNT_TYPE, itemDevice.getDeviceType());
					deviceBundle.putString(ConstData.DeviceMountMsg.NETWORK_PATH, itemDevice.getNetWorkPath());
					mService.getDeviceMountService().execute(new DeviceMountThread(mService, deviceBundle));
				}
				
			}
			//先启动DeviceMountThread,再UnMount设备
			/*if(smbInfos != null && smbInfos.size() > 0){
				for(SmbInfo itemSmbInfo : smbInfos){
					MountUtils.umountSamba(itemSmbInfo);
				}
			}*/
		}else{
			//有新的网络已经连接,检测NFS，Samba设备是否连接,重新挂载网络设备
			Log.i(TAG, "NetWorkCheckThread->smbStr:" + smbStr);
			Log.i(TAG, "NetWorkCheckThread->smbInfos:" + smbInfos);
			if(smbInfos != null && smbInfos.size() > 0){
				Iterator<SmbInfo> smbIterator = smbInfos.iterator();
				while(smbIterator.hasNext()){
					SmbInfo itemSmbInfo = smbIterator.next();
					deviceBundle = new Bundle();
					String localMountPath = itemSmbInfo.getLocalMountPath();
					String name = localMountPath.substring(localMountPath.lastIndexOf("/") + 1, localMountPath.length());
					deviceBundle.putString(ConstData.DeviceMountMsg.DEVICE_NAME, name);
					deviceBundle.putString(ConstData.DeviceMountMsg.MOUNT_PATH, itemSmbInfo.getLocalMountPath());
					deviceBundle.putBoolean(ConstData.DeviceMountMsg.IS_FROM_NETWORK, false);
					deviceBundle.putInt(ConstData.DeviceMountMsg.MOUNT_TYPE, ConstData.DeviceType.DEVICE_TYPE_SMB);
					deviceBundle.putString(ConstData.DeviceMountMsg.NETWORK_PATH, itemSmbInfo.getNetWorkPath());
					if(MountUtils.isMountSuccess(itemSmbInfo.getNetWorkPath(), itemSmbInfo.getLocalMountPath()) || !MountUtils.isMountSuccess(itemSmbInfo.getNetWorkPath(), itemSmbInfo.getLocalMountPath())
							&& MountUtils.mountSamba(itemSmbInfo)){
						//挂载samba成功，启动DeviceMountThread
						deviceBundle.putInt(ConstData.DeviceMountMsg.MOUNT_STATE, ConstData.DeviceMountState.DEVICE_UP);
						mService.getDeviceMountService().execute(new DeviceMountThread(mService, deviceBundle));
					}else if(!MountUtils.isMountSuccess(itemSmbInfo.getNetWorkPath(), itemSmbInfo.getLocalMountPath())){
						//网路存在，仍然挂载失败
						//移除SmbInfo
						smbIterator.remove();
						//挂载samba失败,启动线程处理
						deviceBundle.putInt(ConstData.DeviceMountMsg.MOUNT_STATE, ConstData.DeviceMountState.DEVICE_DOWN);
						mService.getDeviceMountService().execute(new DeviceMountThread(mService, deviceBundle));
					}
				
				}
			}
			if(smbInfos != null && smbInfos.size() > 0)
				StorageUtils.saveDataToSharedPreference(ConstData.SharedKey.SMB_INFOS, JsonUtils.listToJsonArray(smbInfos).toString());
			else
				StorageUtils.saveDataToSharedPreference(ConstData.SharedKey.SMB_INFOS, "");
			
			//读取NFS列表
			String nfsStr = StorageUtils.getDataFromSharedPreference(ConstData.SharedKey.NFS_INFOS);
			List<NFSInfo> nfsInfos = null;
			try{
				if(!TextUtils.isEmpty(nfsStr))
					nfsInfos = (List<NFSInfo>)JsonUtils.arrayToList(NFSInfo.class, new JSONArray(nfsStr));
			}catch (Exception e){
				e.printStackTrace();
			}
			if(nfsInfos != null && nfsInfos.size() > 0){
				Iterator<NFSInfo> nfsIterator = nfsInfos.iterator();
				while(nfsIterator.hasNext()){
					NFSInfo itemNfsInfo = nfsIterator.next();
					deviceBundle = new Bundle();
					String localMountPath = itemNfsInfo.getLocalMountPath();
					String name = localMountPath.substring(localMountPath.lastIndexOf("/") + 1, localMountPath.length());
					deviceBundle.putString(ConstData.DeviceMountMsg.DEVICE_NAME, name);
					deviceBundle.putString(ConstData.DeviceMountMsg.MOUNT_PATH, itemNfsInfo.getLocalMountPath());
					deviceBundle.putBoolean(ConstData.DeviceMountMsg.IS_FROM_NETWORK, false);
					deviceBundle.putInt(ConstData.DeviceMountMsg.MOUNT_TYPE, ConstData.DeviceType.DEVICE_TYPE_NFS);
					deviceBundle.putString(ConstData.DeviceMountMsg.NETWORK_PATH, itemNfsInfo.getNetWorkPath());
					if(MountUtils.isMountSuccess(itemNfsInfo.getNetWorkPath(), itemNfsInfo.getLocalMountPath()) || !MountUtils.isMountSuccess(itemNfsInfo.getNetWorkPath(), itemNfsInfo.getLocalMountPath())
							&& MountUtils.mountNFS(itemNfsInfo)){
						//挂载samba成功，启动DeviceMountThread
						deviceBundle.putInt(ConstData.DeviceMountMsg.MOUNT_STATE, ConstData.DeviceMountState.DEVICE_UP);
						mService.getDeviceMountService().execute(new DeviceMountThread(mService, deviceBundle));
					}else if(!MountUtils.isMountSuccess(itemNfsInfo.getNetWorkPath(), itemNfsInfo.getLocalMountPath())){
						//挂载samba失败,启动
						deviceBundle.putInt(ConstData.DeviceMountMsg.MOUNT_STATE, ConstData.DeviceMountState.DEVICE_DOWN);
						mService.getDeviceMountService().execute(new DeviceMountThread(mService, deviceBundle));
					}
				
				}
			}
		}
	}
}
