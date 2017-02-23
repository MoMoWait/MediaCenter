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
	private boolean mIsAddNetWork;
	public NFSDeviceMountThread(DeviceMonitorService service, NFSInfo nfsInfo, boolean isAddNetWork){
		mService = service;
		mInfo = nfsInfo;
		mIsAddNetWork = isAddNetWork;
	}
	
	@Override
	public void run() {
		if(mInfo != null){
			//如果NFS已经mount成功，直接返回
			if(MountUtils.isMountSuccess(mInfo.getNetWorkPath(), mInfo.getLocalMountPath()))
				return;
			if(MountUtils.mountNFS(mInfo)){
				//mount NFS设备成功
				mService.processMountMsg(mInfo.getLocalMountPath(), Environment.MEDIA_MOUNTED, ConstData.DeviceType.DEVICE_TYPE_NFS, mIsAddNetWork);
			}else{
				//mount NFS设备失败
				mService.processMountMsg(mInfo.getLocalMountPath(), Environment.MEDIA_UNMOUNTED, ConstData.DeviceType.DEVICE_TYPE_NFS, mIsAddNetWork);
			}
		}
	}
}
