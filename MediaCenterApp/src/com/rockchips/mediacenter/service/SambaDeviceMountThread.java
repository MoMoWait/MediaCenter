package com.rockchips.mediacenter.service;



import android.os.Environment;

import com.rockchips.mediacenter.bean.SmbInfo;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.util.MountUtils;

/**
 * @author GaoFei
 * 单个Samba设备挂载器
 */
public class SambaDeviceMountThread extends Thread{
	
	private DeviceMonitorService mService;
	private SmbInfo mInfo;
	private boolean mIsAddNetwork;
	public SambaDeviceMountThread(DeviceMonitorService service, SmbInfo smbInfo, boolean isAddNetWork){
		mService = service;
		mInfo = smbInfo;
		mIsAddNetwork = isAddNetWork;
	}
	
	@Override
	public void run() {
		if(mInfo != null){
			if(MountUtils.mountSamba(mInfo)){
				//mount Samba设备成功
				mService.processMountMsg(mInfo.getLocalMountPath(), Environment.MEDIA_MOUNTED, ConstData.DeviceType.DEVICE_TYPE_SMB, mIsAddNetwork);
			}else{
				//mount Samba设备失败
				mService.processMountMsg(mInfo.getLocalMountPath(), Environment.MEDIA_UNMOUNTED, ConstData.DeviceType.DEVICE_TYPE_SMB, mIsAddNetwork);
			}
		}
	}

}
