package com.rockchips.mediacenter.service;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import android.media.iso.ISOManager;
import android.net.Uri;
import com.rockchips.mediacenter.bean.LocalDevice;
import com.rockchips.mediacenter.bean.LocalMediaFile;
import com.rockchips.mediacenter.bean.LocalMediaFolder;
import com.rockchips.mediacenter.bean.ScanDirectory;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.modle.db.LocalDeviceService;
import com.rockchips.mediacenter.modle.db.LocalMediaFileService;
import com.rockchips.mediacenter.modle.db.LocalMediaFolderService;
import com.rockchips.mediacenter.modle.db.ScanDirectoryService;
import com.rockchips.mediacenter.utils.MediaFileUtils;
import android.util.Log;
/**
 * 本地设备文件扫描线程
 * @author GaoFei
 *
 */
public class FileScanThread extends Thread{
	public static final String TAG = FileScanThread.class.getSimpleName();
	private DeviceMonitorService mService;
	private boolean mIsMounted;
	private int mScanStatus;
	private String mPath;
	private LocalDevice mDevice;
	private LocalMediaFileService mediaFileService;
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
	private List<LocalMediaFile> mTmpFiles = new ArrayList<LocalMediaFile>();
	/**
	 * 文件夹列表
	 */
	private List<LocalMediaFolder> mTmpFolders = new ArrayList<LocalMediaFolder>(); 
	/**
	 * 扫描的目录列表
	 */
	private LinkedList<ScanDirectory> mScanDirectories = new LinkedList<ScanDirectory>();
	/**
	 * 暂存目录
	 */
	private List<ScanDirectory> mTmpDirectory = new ArrayList<ScanDirectory>();
	public FileScanThread(DeviceMonitorService service, LocalDevice device){
		this.mService = service;
		this.mDevice = device;
		this.mPath = device.getMountPath();
		this.mIsMounted = true;
		if(device.getScanStatus() == ConstData.DeviceScanStatus.INITIAL)
			mScanDirectories.add(new ScanDirectory(mPath, mDevice.getDeviceID()));
		mediaFileService = new LocalMediaFileService();
		mediaFolderService = new LocalMediaFolderService();
		mScanDirectoryService = new ScanDirectoryService();
		mLocalDeviceService = new LocalDeviceService();
	}
	
	
	@Override
	public void run() {
		mScanStatus = mDevice.getScanStatus();
		if(mScanStatus == ConstData.DeviceScanStatus.SCANNING || mScanStatus == ConstData.DeviceScanStatus.FINISHED)
			return;
		//当前是暂停状态，重新从数据库装入数据
		if(mScanStatus == ConstData.DeviceScanStatus.PAUSE){
			Log.i(TAG, "scan device from pause:" + mPath);
			loadScanDirectoriesFromDB();
		}
		//当前正在扫描
		mScanStatus = ConstData.DeviceScanStatus.SCANNING;
		//修改当前扫描状态
		mService.setScanStatus(mPath, mScanStatus);
		long startTime = System.currentTimeMillis();
		Log.i(TAG, "FileScanThread start time:" + startTime);
		while(!mScanDirectories.isEmpty()){
		    try{
		        while(mService.isHaveVideoPlay()){
		            //睡眠1s
	                Thread.sleep(1000);
	            }
		    }catch (Exception e){
		        Log.i(TAG, "FileScanThread exception:" + e);
		    }
		  
			mIsMounted = mService.isMounted(mPath);
			mScanStatus = mService.getScanStatus(mPath);
			if(!mIsMounted){
				//操作数据库，直接删除所有已经存在数据库的数据
				mediaFileService.deleteFilesByPhysicId(mDevice.getPhysic_dev_id());
				mediaFolderService.deleteFoldersByPhysicId(mDevice.getPhysic_dev_id());
				mScanDirectoryService.deleteDirectoriesByDeviceId(mDevice.getDeviceID());
				return;
			}
			//暂停扫描
			if(mScanStatus == ConstData.DeviceScanStatus.PAUSE){
				Log.i(TAG, "pause scan device path:" + mPath);
				//文件入库
				mediaFileService.saveOrUpdateAll(mTmpFiles);
				mTmpFiles.clear();
				//文件夹入库
				mediaFolderService.saveOrUpdateAll(mTmpFolders);
				mTmpFolders.clear();
				//将扫描目录存储至数据库
				mScanDirectoryService.saveAll(mTmpDirectory);
				mScanDirectoryService.saveAll(mScanDirectories);
				//修改设备的扫描状态并存入数据库
				mDevice.setScanStatus(ConstData.DeviceScanStatus.PAUSE);
				mLocalDeviceService.update(mDevice);
				return;
			}
			if(mTmpFiles.size() >= 100){
				//入库
				mediaFileService.saveOrUpdateAll(mTmpFiles);
				mTmpFiles.clear();
			}
			
			if(mTmpFolders.size() >= 100){
				//入库
				mediaFolderService.saveOrUpdateAll(mTmpFolders);
				mTmpFolders.clear();
			}
			if(mScanDirectories.size() > MAX_DIRS){
				//超过了最大缓存目录标记
				mIsOverMaxDirs = true;
			}else if(mScanDirectories.size() < MAX_DIRS / 2){
				//设置最大缓存标记为false
				mIsOverMaxDirs = false;
				//从数据库拿出数据
				loadScanDirectoriesFromDB();
			}
			File dirFile = new File(mScanDirectories.remove().getPath());
			//Log.i(TAG, "FileScanThread->run->dirFile:" + dirFile);
			//Log.i(TAG, "FileScanThread->run->dirFile->exists:" + dirFile.exists());
			if(dirFile != null && dirFile.exists()){
				File[] subFiles = dirFile.listFiles();
				int audioCount = 0;
				int imageCount = 0;
				int videoCount = 0;
				int mediaCount = 0;
				//拔插时候，容易出现subFiles为null
				if(subFiles == null || subFiles.length == 0)
					continue;
				for(File subFile : subFiles){
					if(subFile.isDirectory()){
						//如果是蓝光文件夹
						if(ISOManager.isBDDirectory(subFile.getPath())){
							LocalMediaFile localMediaFile = MediaFileUtils.getBDMediaFile(subFile, mDevice);
							mTmpFiles.add(localMediaFile);
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
						LocalMediaFile localMediaFile = MediaFileUtils.getMediaFileFromFile(subFile, mDevice);
						if(localMediaFile != null){
							mTmpFiles.add(localMediaFile);
							if(localMediaFile.getType() != ConstData.MediaType.SUBTITLE){
								++mediaCount;
								if(localMediaFile.getType() == ConstData.MediaType.AUDIO)
									++audioCount;
								else if(localMediaFile.getType() == ConstData.MediaType.VIDEO)
									++videoCount;
								else if(localMediaFile.getType() == ConstData.MediaType.IMAGE)
									++imageCount;
							}
						}
					}
				}
				
				if(mediaCount > 0){
					LocalMediaFolder mediaFolder = MediaFileUtils.getMediaFolderFromFile(dirFile, mDevice);
					mediaFolder.setFolderType(ConstData.MediaType.FOLDER);
					mediaFolder.setFileCount(mediaCount);
					mTmpFolders.add(mediaFolder);
					if(audioCount > 0){
						LocalMediaFolder audioFolder = new LocalMediaFolder(mediaFolder);
						audioFolder.setFolderType(ConstData.MediaType.AUDIOFOLDER);
						audioFolder.setFileCount(audioCount);
						mTmpFolders.add(audioFolder);
					}
					
					if(imageCount > 0){
						LocalMediaFolder imageFolder = new LocalMediaFolder(mediaFolder);
						imageFolder.setFolderType(ConstData.MediaType.IMAGEFOLDER);
						imageFolder.setFileCount(imageCount);
						mTmpFolders.add(imageFolder);
					}
					
					if(videoCount > 0){
						LocalMediaFolder videoFolder = new LocalMediaFolder(mediaFolder);
						videoFolder.setFolderType(ConstData.MediaType.VIDEOFOLDER);
						videoFolder.setFileCount(videoCount);
						mTmpFolders.add(videoFolder);
					}
				}
				
			}
		}
		
		mIsMounted = mService.isMounted(mPath);
		mScanStatus = mService.getScanStatus(mPath);
		if(!mIsMounted){
			//操作数据库，直接删除所有已经存在数据库的数据
			mediaFileService.deleteFilesByPhysicId(mDevice.getPhysic_dev_id());
			mediaFolderService.deleteFoldersByPhysicId(mDevice.getPhysic_dev_id());
			mScanDirectoryService.deleteDirectoriesByDeviceId(mDevice.getDeviceID());
			return;
		}
		//文件入库
		mediaFileService.saveOrUpdateAll(mTmpFiles);
		mTmpFiles.clear();
		//文件夹入库
		mediaFolderService.saveOrUpdateAll(mTmpFolders);
		mTmpFolders.clear();
		mDevice.setHas_scaned(true);
		//扫描完成
		mDevice.setScanStatus(ConstData.DeviceScanStatus.FINISHED);
		mLocalDeviceService.update(mDevice);
		long endTime = System.currentTimeMillis();
		Log.i(TAG, "FileScanThread end time:" + endTime);
		Log.i(TAG, "FileScanThread total time:" + (endTime - startTime) / 1000 + "s");
		//Log.i(TAG, "FileScanThread end");
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
