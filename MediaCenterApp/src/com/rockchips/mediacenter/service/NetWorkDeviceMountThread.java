package com.rockchips.mediacenter.service;

import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;

import android.os.Environment;
import android.text.TextUtils;

import com.rockchips.mediacenter.bean.NFSInfo;
import com.rockchips.mediacenter.bean.SmbInfo;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.util.MountUtils;

import momo.cn.edu.fjnu.androidutils.utils.JsonUtils;
import momo.cn.edu.fjnu.androidutils.utils.StorageUtils;

/**
 * @author GaoFei
 * 网络设备（Samba，NFS）挂载处理
 * 挂载已经挂载过的samba，nfs设备
 */
public class NetWorkDeviceMountThread extends Thread{
	private DeviceMonitorService mService;
	private int mNetWorkDeviceType;
	public NetWorkDeviceMountThread(DeviceMonitorService service, int netWorkDeviceType){
		mService = service;
		mNetWorkDeviceType = netWorkDeviceType;
	}
	
	@Override
	public void run() {
		String nfsInfoStrs = StorageUtils.getDataFromSharedPreference(ConstData.SharedKey.NFS_INFOS);
		if(!TextUtils.isEmpty(nfsInfoStrs) && 
				(mNetWorkDeviceType == ConstData.NetWorkDeviceType.DEVICE_ALL || mNetWorkDeviceType == ConstData.NetWorkDeviceType.DEVICE_NFS)){
			//挂载已经挂载（存在）过NFS设备
			JSONArray nfsInfoArray = null;
			try{
				nfsInfoArray = new JSONArray(nfsInfoStrs);
			}catch(Exception e){
				
			}
			List<NFSInfo> nfsList = (List<NFSInfo>)JsonUtils.arrayToList(NFSInfo.class, nfsInfoArray);
			Iterator<NFSInfo> nfsIterator = nfsList.iterator();
			while(nfsIterator.hasNext()){
				NFSInfo nfsInfo = nfsIterator.next();
				if(MountUtils.mountNFS(nfsInfo)){
					//挂载成功
					mService.processMountMsg(nfsInfo.getLocalMountPath(), Environment.MEDIA_MOUNTED, ConstData.DeviceType.DEVICE_TYPE_NFS, false);
				}else{
					//挂载失败
					mService.processMountMsg(nfsInfo.getLocalMountPath(), Environment.MEDIA_UNMOUNTED, ConstData.DeviceType.DEVICE_TYPE_NFS, false);
					nfsIterator.remove();
				}
			}
			
			if(nfsList != null && nfsList.size() > 0){
				StorageUtils.saveDataToSharedPreference(ConstData.SharedKey.NFS_INFOS, JsonUtils.listToJsonArray(nfsList).toString());
			}else{
				StorageUtils.saveDataToSharedPreference(ConstData.SharedKey.NFS_INFOS, "");
			}
		}
	
		String smbInfoStrs = StorageUtils.getDataFromSharedPreference(ConstData.SharedKey.SMB_INFOS);
		if(!TextUtils.isEmpty(smbInfoStrs)&& 
					(mNetWorkDeviceType == ConstData.NetWorkDeviceType.DEVICE_ALL || mNetWorkDeviceType == ConstData.NetWorkDeviceType.DEVICE_SMB)){
			//挂载已经挂载（存在）过Samba设备
			JSONArray sambInfoArray = null;
			try{
				sambInfoArray = new JSONArray(smbInfoStrs);
			}catch(Exception e){
				
			}
			List<SmbInfo> smbList = (List<SmbInfo>)JsonUtils.arrayToList(SmbInfo.class, sambInfoArray);
			Iterator<SmbInfo> smbIterator = smbList.iterator();
			while(smbIterator.hasNext()){
				SmbInfo smbInfo = smbIterator.next();
				if(MountUtils.mountSamba(smbInfo)){
					//挂载成功
					mService.processMountMsg(smbInfo.getLocalMountPath(), Environment.MEDIA_MOUNTED, ConstData.DeviceType.DEVICE_TYPE_SMB, false);
				}else{
					//挂载失败
					mService.processMountMsg(smbInfo.getLocalMountPath(), Environment.MEDIA_UNMOUNTED, ConstData.DeviceType.DEVICE_TYPE_SMB, false);
					smbIterator.remove();
				}
			}
			
			if(smbList != null && smbList.size() > 0){
				StorageUtils.saveDataToSharedPreference(ConstData.SharedKey.SMB_INFOS, JsonUtils.listToJsonArray(smbList).toString());
			}else{
				StorageUtils.saveDataToSharedPreference(ConstData.SharedKey.SMB_INFOS, "");
			}
		}
	}
}
