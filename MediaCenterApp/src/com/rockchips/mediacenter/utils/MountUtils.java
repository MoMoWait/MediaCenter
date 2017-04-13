package com.rockchips.mediacenter.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import com.rockchips.mediacenter.R;
import momo.cn.edu.fjnu.androidutils.utils.ResourceUtils;
import momo.cn.edu.fjnu.androidutils.utils.ToastUtils;

import com.rockchips.mediacenter.bean.LocalDevice;
import com.rockchips.mediacenter.bean.NFSInfo;
import com.rockchips.mediacenter.bean.SmbInfo;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.modle.db.LocalDeviceService;
import com.rockchips.mediacenter.modle.db.LocalMediaFileService;
import com.rockchips.mediacenter.modle.db.LocalMediaFolderService;
import com.rockchips.mediacenter.modle.db.ScanDirectoryService;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemProperties;
import android.util.Log;
/**
 * @author GaoFei
 * 设备的挂载卸载工具
 */
public class MountUtils {
	public static final String TAG = "MountUtils";
	public static final String SHELL_FILE_DIR = "/data/etc";
	/**
	 * 执行Mount,Unmount命令的脚本文件
	 */
	public static final String SHELL_PATH = "/data/etc/cifsmanager.sh";
	/**
	 * 最多尝试挂载3次
	 */
	public static final int MAX_MOOUNT_COUNT = 3;
	
	private static String mMountResult = ConstData.MOUNT_RESULT.MOUNT_FAIL;	
	private static String mUnMountResult = ConstData.MOUNT_RESULT.MOUNT_FAIL;	
	/**
	 * 挂载NFS设备
	 */
	public static boolean mountNFS(NFSInfo nfsInfo){
		//Log.i(TAG, "mountNFS");
		//网络路径
		String netWorkPath = nfsInfo.getNetWorkPath();
		//本地挂载路径
		String localMountPath = nfsInfo.getLocalMountPath();
		File mountDirPath = new File(localMountPath);
		//Log.i(TAG, "mountNFS->mountDirPath.exists():" + mountDirPath.exists());
		if(mountDirPath.exists()){
			if(isMountSuccess(netWorkPath, localMountPath)){
				mMountResult = ConstData.MOUNT_RESULT.MOUNT_SUCC;
				return true;
			}
		}else{
			//创建目录
			mountDirPath.mkdirs();
		}
		//尝试卸载NFS设备
		//umountNFS(nfsInfo);
		writeCommandToShellFile("busybox mount -t nfs -o nolock " + nfsInfo.getNetWorkPath() + " " + nfsInfo.getLocalMountPath());
		for(int i = 0; i < MAX_MOOUNT_COUNT; ++i){
			SystemProperties.set("ctl.start", "cifsmanager");
			try{
				Thread.sleep(2000);
			}catch(Exception ex){
				Log.e(TAG, "mountNFS: " + ex);
				continue;
			}
			if(isMountSuccess(netWorkPath, localMountPath)){
				mMountResult = ConstData.MOUNT_RESULT.MOUNT_SUCC;
				return true;
			}
			
		}
		mMountResult = ConstData.MOUNT_RESULT.MOUNT_FAIL;
		return false;
	}
	
	/**
	 * 挂载Samba设置
	 * @param smbInfo
	 * @param result
	 */
	public static boolean mountSamba(SmbInfo smbInfo){
		Log.e(TAG, "mountSamba->smbInfo:" + smbInfo);
		//网络路径
		String netWorkPath = smbInfo.getNetWorkPath();
		//本地挂载路径
		String localMountPath = smbInfo.getLocalMountPath();
		File mountDirPath = new File(localMountPath);
		if(mountDirPath.exists()){
			Log.e(TAG, "mountSamba->mountDirPath exist->" + mountDirPath);
			if(isMountSuccess(netWorkPath, localMountPath)){
				mMountResult = ConstData.MOUNT_RESULT.MOUNT_SUCC;
				return true;
			}
		}else{
			//创建目录
			mountDirPath.mkdirs();
		}
		//尝试卸载Samba设备
		//umountSamba(smbInfo);
		if(smbInfo.isUnknowName())
			writeCommandToShellFile("busybox mount -t cifs -o iocharset=utf8,username=guest,uid=1000,gid=1015,file_mode=0775,dir_mode=0775,rw " + smbInfo.getNetWorkPath() + " " + smbInfo.getLocalMountPath());
		else
			writeCommandToShellFile("busybox mount -t cifs -o iocharset=utf8,username=" + smbInfo.getUserName() + ",password=" + smbInfo.getPassword() + ",uid=1000,gid=1015,file_mode=0775,dir_mode=0775,rw " 
		+ smbInfo.getNetWorkPath() + " " +smbInfo.getLocalMountPath());
		for(int i = 0; i < MAX_MOOUNT_COUNT; ++i){
			SystemProperties.set("ctl.start", "cifsmanager");
			try{
				Thread.sleep(2000);
			}catch(Exception ex){
				Log.e(TAG, "mountSamba: " + ex.getMessage());
				continue;
			}
			if(isMountSuccess(netWorkPath, localMountPath)){
				mMountResult = ConstData.MOUNT_RESULT.MOUNT_SUCC;
				return true;
			}
			
		}
		mMountResult = ConstData.MOUNT_RESULT.MOUNT_FAIL;
		return false;
	}
	
	/**
	 * 卸载NFS设备
	 */
	public static void umountNFS(NFSInfo nfsInfo){
		writeCommandToShellFile("busybox umount " + nfsInfo.getLocalMountPath());
		SystemProperties.set("ctl.start", "cifsmanager");
		try{
			Thread.sleep(2000);
		}catch(Exception ex){
			Log.e(TAG, "umountNFS: " + ex);
		}
		if(isUMountSuccess(nfsInfo.getLocalMountPath())){
			mMountResult = ConstData.UMOUNT_RESULT.UMOUNT_SUCC;
			return;
		}
		mMountResult = ConstData.UMOUNT_RESULT.UMOUNT_FAIL;
	}
	
	/**
	 * 卸载Samba设备
	 * @param smbInfo
	 * @param result
	 */
	public static void umountSamba(SmbInfo smbInfo){
		Log.i(TAG, "umountSamba->smbInfo:" + smbInfo);
		writeCommandToShellFile("busybox umount " + smbInfo.getLocalMountPath());
		SystemProperties.set("ctl.start", "cifsmanager");
		/*for(int i = 0; i < MAX_MOOUNT_COUNT; ++i){
			try{
				Thread.sleep(30000);
			}catch(Exception ex){
				Log.e(TAG, "umountSamba: " + ex);
			}
			
			if(isUMountSuccess(smbInfo.getLocalMountPath())){
				Log.i(TAG, "unMountSuccess->mountPath:" + smbInfo.getLocalMountPath());
				mUnMountResult = ConstData.UMOUNT_RESULT.UMOUNT_SUCC;
				return;
			}
		}*/
		mUnMountResult = ConstData.UMOUNT_RESULT.UMOUNT_FAIL;
	}
	
	/**
	 * 过滤NFS设备列表，挂载成功才可以加入列表中
	 * @param nfsList
	 */
	public static List<NFSInfo> filterNFSDevices(List<NFSInfo> nfsList){
		//String mountResult = ConstData.MOUNT_RESULT.MOUNT_FAIL;
		if(nfsList == null || nfsList.size() == 0)
			return nfsList;
		Iterator<NFSInfo> iterator = nfsList.iterator();
		while(iterator.hasNext()){
			NFSInfo nfsInfo = iterator.next();
			mountNFS(nfsInfo);
			if(mMountResult.equals(ConstData.MOUNT_RESULT.MOUNT_FAIL)){
				//ToastUtils.showToast(ResourceUtils.getString(R.string.nfs_device) + " " + nfsInfo.getLocalMountPath() + " " + ResourceUtils.getString(R.string.mount_fail));
				iterator.remove();
			}
				
		}
		
		return nfsList;
	}
	
	/**
	 * 过滤Samba设备，挂载成功才可以进入列表中
	 * @param nfsList
	 */
	public static List<SmbInfo> filterSambaDevices(List<SmbInfo> sambaList){
		if(sambaList == null || sambaList.size() == 0)
			return sambaList;
		Iterator<SmbInfo> iterator = sambaList.iterator();
		while(iterator.hasNext()){
			final SmbInfo smbInfo = iterator.next();
			mountSamba(smbInfo);
			//Log.i(TAG, "filterSambaDevices->mountResult:" + mMountResult);
			if(mMountResult.equals(ConstData.MOUNT_RESULT.MOUNT_FAIL)){
				iterator.remove();
			}
		}
		return sambaList;
	}
	
	/**
	 * 根据挂载目录判断是否挂载成功,结合mount命令和文件存在性共同验证
	 * @param mountDir
	 * @return
	 */
	public static boolean isMountSuccess(String netWorkPath, String mountDir){
		//Log.i(TAG, "isMountSuccess->netWorkPath:" + netWorkPath);
		//Log.i(TAG, "isMountSuccess->mountDir:" + mountDir);
		List<String> lines = ShellUtils.getMountMsgs();
		File dirFile = new File(mountDir);
		String[] fileNames = dirFile.list();
		//Log.i(TAG, "isMountSuccess->fileNames:" +Arrays.toString(fileNames));
		for(String line : lines){
			//Log.i(TAG, "isMountSuccess->line:" + line);
			//对于NFS挂载
			if(line.contains(netWorkPath) && fileNames != null && fileNames.length > 0)
				return true;
			//对于Samba挂载
			String replaceLine = line.replaceAll("134", "");
			replaceLine = replaceLine.replaceAll("\\\\", "/");
			if(replaceLine.contains(netWorkPath) && fileNames != null && fileNames.length > 0)
				return true;
		}
		//Log.i(TAG, "isMountSuccess->result:" + false);
		return false;
	}
	
	/**
	 * 根据挂载目录判断是否卸载成功
	 * @param mountDir
	 * @return
	 */
	public static boolean isUMountSuccess(String mountDir){
		List<String> lines = ShellUtils.getDfMsgs();
		File dirFile = new File(mountDir);
		String[] fileNames = dirFile.list();
		for(String line : lines){
			if(line.contains(mountDir))
				return false;
		}
		return true;
	}
	
	
	/**
	 * 写入命令至Shell文件
	 */
	public static boolean writeCommandToShellFile(String command){
		//Log.i(TAG, "writeCommandToShellFile->command:" + command);
		File shellDirFile = new File(SHELL_FILE_DIR);
		if(!shellDirFile.exists())
			shellDirFile.mkdirs();
		File shell = new File(SHELL_PATH);
		if (!shell.exists()){
			try {
				shell.createNewFile();
				shell.setExecutable(true);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		
		try {
			BufferedWriter buffwr = new BufferedWriter(new FileWriter(shell));
			buffwr.write("#!/system/bin/sh");
			buffwr.newLine();
			buffwr.write(command);
			buffwr.newLine();
			buffwr.flush();
			buffwr.close();
		} catch (IOException e) {
			e.printStackTrace();
			//Log.i(TAG, "writeCommandToShellFile->" + e);
			return false;
		}
		
		return true;	
	}
	
	/**
	 * 删除未挂载的设备
	 */
	public static void deleteUnMountDevices(){
		LocalDeviceService localDeviceService = new LocalDeviceService();
		LocalMediaFolderService localMediaFolderService = new LocalMediaFolderService();
		LocalMediaFileService localMediaFileService = new LocalMediaFileService();
		ScanDirectoryService scanDirectoryService = new ScanDirectoryService();
		List<LocalDevice> allLocalDevices = localDeviceService.getAll(LocalDevice.class);
		if(allLocalDevices != null && allLocalDevices.size() > 0){
			for(LocalDevice itemDevice : allLocalDevices){
				File mountFile = new File(itemDevice.getMountPath());
				if(itemDevice.getDevices_type() == ConstData.DeviceType.DEVICE_TYPE_U
						|| itemDevice.getDevices_type() == ConstData.DeviceType.DEVICE_TYPE_SD){
					if(mountFile == null || !mountFile.exists() || !mountFile.isDirectory()){
						localDeviceService.deleteDeviceByPath(itemDevice.getMountPath());
						localMediaFolderService.deleteFoldersByPhysicId(itemDevice.getPhysic_dev_id());
						localMediaFileService.deleteFilesByPhysicId(itemDevice.getPhysic_dev_id());
						scanDirectoryService.deleteDirectoriesByDeviceId(itemDevice.getDeviceID());
					}
				}else if(itemDevice.getDevices_type() == ConstData.DeviceType.DEVICE_TYPE_SMB
						|| itemDevice.getDevices_type() == ConstData.DeviceType.DEVICE_TYPE_NFS){
					if(mountFile == null || !mountFile.exists() || !mountFile.isDirectory() 
							|| mountFile.list() == null || mountFile.list().length == 0){
						localDeviceService.deleteDeviceByPath(itemDevice.getMountPath());
						localMediaFolderService.deleteFoldersByPhysicId(itemDevice.getPhysic_dev_id());
						localMediaFileService.deleteFilesByPhysicId(itemDevice.getPhysic_dev_id());
						scanDirectoryService.deleteDirectoriesByDeviceId(itemDevice.getDeviceID());
					}
				}
				
			}
		}
	}
}
