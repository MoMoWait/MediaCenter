/**
 * 
 */
package com.rockchips.mediacenter.service;
import com.rockchips.mediacenter.bean.LocalDevice;
import com.rockchips.mediacenter.modle.db.LocalMediaFileService;
import com.rockchips.mediacenter.modle.db.LocalMediaFolderService;
import com.rockchips.mediacenter.modle.db.ScanDirectoryService;
/**
 * @author GaoFei
 * 本地设备上下线处理线程
 */
public class LocalDeviceUpDownProcessThread extends Thread{
	private LocalDevice mLocalDevice;
	public LocalDeviceUpDownProcessThread(LocalDevice device){
		mLocalDevice = device;
	}
	
	@Override
	public void run() {
		
		LocalMediaFileService mediaFileService = new LocalMediaFileService();
		LocalMediaFolderService folderService = new LocalMediaFolderService();
		ScanDirectoryService scanDirectoryService = new ScanDirectoryService();
		
		/** 删除该设备对应的文件夹 */
		folderService.deleteFoldersByDeviceId(mLocalDevice.getDeviceID());
		/** 删除该设备对应的文件 */
		mediaFileService.deleteFilesByDeviceId(mLocalDevice.getDeviceID());
		/**
		 * 删除扫描缓存目录
		 */
		scanDirectoryService.deleteDirectoriesByDeviceId(mLocalDevice.getDeviceID());
	
	}
}
