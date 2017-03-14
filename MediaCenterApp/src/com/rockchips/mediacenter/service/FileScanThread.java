package com.rockchips.mediacenter.service;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import android.media.iso.ISOManager;
import android.net.Uri;
import com.rockchips.mediacenter.bean.Device;
import com.rockchips.mediacenter.bean.DeviceScanInfo;
import com.rockchips.mediacenter.bean.FileInfo;
import com.rockchips.mediacenter.bean.LocalDevice;
import com.rockchips.mediacenter.bean.LocalMediaFile;
import com.rockchips.mediacenter.bean.LocalMediaFolder;
import com.rockchips.mediacenter.bean.ScanDirectory;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.modle.db.FileInfoService;
import com.rockchips.mediacenter.modle.db.LocalDeviceService;
import com.rockchips.mediacenter.modle.db.LocalMediaFileService;
import com.rockchips.mediacenter.modle.db.LocalMediaFolderService;
import com.rockchips.mediacenter.modle.db.ScanDirectoryService;
import com.rockchips.mediacenter.utils.MediaFileUtils;
import com.rockchips.mediacenter.utils.MediaUtils;

import android.util.Log;
/**
 * 本地设备文件扫描线程
 * @author GaoFei
 *
 */
public class FileScanThread extends Thread{
	public static final String TAG = "FileScanThread";
	private DeviceMonitorService mService;
	private String mPath;
	private Device mDevice;
	private FileInfoService mFileInfoService;
	private LocalMediaFolderService mediaFolderService;
	private ScanDirectoryService mScanDirectoryService;
	private LocalDeviceService mLocalDeviceService;
	/**
	 * 是否超过最大目录限制
	 */
	private boolean mIsOverMaxDirs;
	/**
	 * 队列最大目录
	 */
	public static final int MAX_DIRS = 2000;
	/**
	 * 文件列表
	 */
	private List<FileInfo> mTmpFileInfos = new ArrayList<FileInfo>();
	/**
	 * 扫描的目录列表
	 */
	private LinkedList<ScanDirectory> mScanDirectories = new LinkedList<ScanDirectory>();
	/**
	 * 暂存目录
	 */
	private List<ScanDirectory> mTmpDirectory = new ArrayList<ScanDirectory>();
	public FileScanThread(DeviceMonitorService service, Device device){
		this.mService = service;
		this.mDevice = device;
		this.mPath = device.getLocalMountPath();
	    mScanDirectories.add(new ScanDirectory(mPath, mDevice.getDeviceID()));
		mFileInfoService = new FileInfoService();
		mScanDirectoryService = new ScanDirectoryService();
	}
	
	
	@Override
	public void run() {
		long startTime = System.currentTimeMillis();
		Log.i(TAG, "FileScanThread start time:" + startTime);
		//设置当前扫描状态为正在扫描
		mService.getDeviceScanInfo(mDevice.getDeviceID()).setScanStatus(ConstData.DeviceScanStatus.SCANNING);
		while(!mScanDirectories.isEmpty()){
		    //获取设备扫描信息
			int mountState = mService.getDeviceScanInfo(mDevice.getDeviceID()).getMountState();
			//获取设备扫描状态
			int scanStatus = mService.getDeviceScanInfo(mDevice.getDeviceID()).getScanStatus();
			if(mountState == ConstData.DeviceMountState.DEVICE_DOWN || scanStatus != ConstData.DeviceScanStatus.SCANNING){
				//设备已经下线，不扫描直接返回
				Log.i(TAG, mDevice.getDeviceName() + "is offline or stop scanner");
				return;
			}
			//存在视频播放，并且设备已经上线
			boolean haveVideoPlay = MediaUtils.hasMediaClient();
			//Log.i(TAG, "FileScanThread->haveVideoPlay:" + haveVideoPlay);
			try {
				//存在视频播放，并且设备已经挂载
				while (haveVideoPlay && mountState == ConstData.DeviceMountState.DEVICE_UP && scanStatus == ConstData.DeviceScanStatus.SCANNING) {
					// 睡眠1s
					Thread.sleep(1000);
					Log.i(TAG, "FileScanThread->haveVideoPlay:" + haveVideoPlay);
					haveVideoPlay = MediaUtils.hasMediaClient();
					mountState = mService.getDeviceScanInfo(mDevice.getDeviceID()).getMountState();
					scanStatus = mService.getDeviceScanInfo(mDevice.getDeviceID()).getScanStatus();
				}
			}catch(Exception e){
				Log.e(TAG, "FileScanThread->sleep->exception:" + e);
			}
	      
		    
			if(mTmpFileInfos.size() >= 100){
				//入库
				mFileInfoService.saveAll(mTmpFileInfos);
				mTmpFileInfos.clear();
			}
			
			if(mScanDirectories.size() > MAX_DIRS){
				//超过了最大缓存目录标记
				mIsOverMaxDirs = true;
			}else if(mScanDirectories.size() < MAX_DIRS / 2 && mIsOverMaxDirs){
				//设置最大缓存标记为false
				mIsOverMaxDirs = false;
				//从数据库拿出数据
				loadScanDirectoriesFromDB();
			}
			File dirFile = new File(mScanDirectories.remove().getPath());
			//Log.i(TAG, "ScanDirectory->dirFile:" + dirFile);
			int musicCount = 0;
			int imageCount = 0;
			int videoCount = 0;
			int mediaCount = 0; 
			if(dirFile != null && dirFile.exists()){
				File[] subFiles = dirFile.listFiles();
				//拔插时候，容易出现subFiles为null
				if(subFiles == null || subFiles.length == 0)
					continue;
				for(File subFile : subFiles){
					if(!subFile.exists())
						continue;
					if(subFile.isDirectory()){
						//如果是蓝光文件夹
						if(ISOManager.isBDDirectory(subFile.getPath())){
							FileInfo fileInfo = MediaFileUtils.getBDFileInfo(subFile, mDevice);
							mTmpFileInfos.add(fileInfo);
							++mediaCount;
							++videoCount;
						}
						else if(mIsOverMaxDirs){
							ScanDirectory scanDirectory = new ScanDirectory();
							scanDirectory.setDeviceId(mDevice.getDeviceID());
							scanDirectory.setPath(subFile.getPath());
							mTmpDirectory.add(scanDirectory);
							if(mTmpDirectory.size() >= MAX_DIRS / 2){
								//操作MAX_DIRS/2入库
								mScanDirectoryService.saveAll(mTmpDirectory);
								mTmpDirectory.clear();
							}
						}else{
							//文件夹加入扫描队列
							mScanDirectories.add(new ScanDirectory(subFile.getPath(), mDevice.getDeviceID()));
						}
						
					}else{
						FileInfo fileInfo = MediaFileUtils.getFileInfoFromFile(subFile, mDevice);
						if(fileInfo != null){
							mTmpFileInfos.add(fileInfo);
							++mediaCount;
							if(fileInfo.getType() == ConstData.MediaType.AUDIO)
								++musicCount;
							else if(fileInfo.getType() == ConstData.MediaType.VIDEO)
								++videoCount;
							else if(fileInfo.getType() == ConstData.MediaType.IMAGE)
								++imageCount;
						}
					}
				}
				
				if(mediaCount > 0){
					FileInfo fileInfo = new FileInfo();
					fileInfo.setDeviceID(mDevice.getDeviceID());
					fileInfo.setModifyTime(dirFile.lastModified());
					fileInfo.setName(dirFile.getName());
					fileInfo.setPath(dirFile.getPath());
					fileInfo.setType(ConstData.MediaType.FOLDER);
					fileInfo.setParentPath(dirFile.getParent());
					fileInfo.setChildCount(dirFile.list().length);
					if(musicCount > 0){
						fileInfo.setMusicCount(musicCount);
					}
					
					if(imageCount > 0){
						fileInfo.setImageCount(imageCount);
					}
					
					if(videoCount > 0){
						fileInfo.setVideoCount(videoCount);
					}
					
					mTmpFileInfos.add(fileInfo);
				}
				
			}
		}
		Log.i(TAG, "mTmpFileInfos->size:" + mTmpFileInfos.size());
		//文件入库
		mFileInfoService.saveAll(mTmpFileInfos);
		mTmpFileInfos.clear();
		mService.getDeviceScanInfo(mDevice.getDeviceID()).setScanStatus(ConstData.DeviceScanStatus.FINISHED);
		long endTime = System.currentTimeMillis();
		Log.i(TAG, "FileScanThread end time:" + endTime);
		Log.i(TAG, "FileScanThread total time:" + (endTime - startTime) / 1000 + "s");
	}
	
	
	/**
	 * 从数据库中装载数据
	 */
	private void loadScanDirectoriesFromDB(){
		//从数据库拿出部分数据
		List<ScanDirectory> dbDirectories = mScanDirectoryService.getDirectoriesByDeviceId(mDevice.getDeviceID(), MAX_DIRS - mScanDirectories.size());
		if(dbDirectories != null && dbDirectories.size() > 0){
			for(ScanDirectory itemDirectory : dbDirectories){
				mScanDirectories.add(itemDirectory);
			}
			//删除数据库中对应的数据
			mScanDirectoryService.deleteAll(dbDirectories);
		}
	}
	
}
