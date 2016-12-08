package com.rockchips.mediacenter.service;
import android.os.Environment;
import com.rockchips.mediacenter.bean.NFSInfo;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.util.MountUtils;
/**
 * @author GaoFei
 * 单个NFS设备挂载器
 */
public class NFSDeviceMountThread extends Thread{
	private DeviceMonitorService mService;
	private NFSInfo mInfo;
	public NFSDeviceMountThread(DeviceMonitorService service, NFSInfo nfsInfo){
		mService = service;
		mInfo = nfsInfo;
	}
	
	@Override
	public void run() {
		if(mInfo != null){
			if(MountUtils.mountNFS(mInfo)){
				//mount NFS设备成功
				mService.processMountMsg(mInfo.getLocalMountPath(), Environment.MEDIA_MOUNTED, ConstData.DeviceType.DEVICE_TYPE_NFS);
			}else{
				//mount NFS设备失败
				mService.processMountMsg(mInfo.getLocalMountPath(), Environment.MEDIA_UNMOUNTED, ConstData.DeviceType.DEVICE_TYPE_NFS);
			}
		}
	}
}
