package com.rockchips.mediacenter.service;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import android.util.Log;
import com.rockchips.mediacenter.bean.LocalDevice;
import com.rockchips.mediacenter.bean.LocalMediaFile;
import com.rockchips.mediacenter.bean.LocalMediaFolder;
import com.rockchips.mediacenter.bean.ScanDirectory;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.modle.db.LocalDeviceService;
import com.rockchips.mediacenter.modle.db.LocalMediaFileService;
import com.rockchips.mediacenter.modle.db.LocalMediaFolderService;
import com.rockchips.mediacenter.modle.db.ScanDirectoryService;
import com.rockchips.mediacenter.util.MediaFileUtils;

/**
 * 文件扫描线程
 * @author GaoFei
 *
 */
public class FileScanThread extends Thread{
	public static final String TAG = FileScanThread.class.getSimpleName();
	private DeviceMonitorService mService;
	private boolean mIsMounted;
	private String mPath;
	private LocalDevice mDevice;
	private LocalMediaFileService mediaFileService;
	private LocalMediaFolderService mediaFolderService;
	private ScanDirectoryService mScanDirectoryService;
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
	private LinkedList<File> mScanDirectory = new LinkedList<File>();
	/**
	 * 暂存目录
	 */
	private List<ScanDirectory> mTmpDirectory = new ArrayList<ScanDirectory>();
	public FileScanThread(DeviceMonitorService service, LocalDevice device){
		this.mService = service;
		this.mDevice = device;
		this.mPath = device.getMountPath();
		this.mIsMounted = true;
		mScanDirectory.add(new File(mPath));
		mediaFileService = new LocalMediaFileService();
		mediaFolderService = new LocalMediaFolderService();
		mScanDirectoryService = new ScanDirectoryService();
	}
	
	
	@Override
	public void run() {
		//Log.i(TAG, "FileScanThread start");
		while(!mScanDirectory.isEmpty()){
			mIsMounted = mService.isMounted(mPath);
			if(!mIsMounted){
				//操作数据库，直接删除所有已经存在数据库的数据
				mediaFileService.deleteFilesByPhysicId(mDevice.getPhysic_dev_id());
				mediaFolderService.deleteFoldersByPhysicId(mDevice.getPhysic_dev_id());
				mScanDirectoryService.deleteDirectoriesByDeviceId(mDevice.getDeviceID());
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
			if(mScanDirectory.size() > MAX_DIRS){
				//超过了最大缓存目录标记
				mIsOverMaxDirs = true;
			}else if(mScanDirectory.size() < MAX_DIRS / 2){
				//设置最大缓存标记为false
				mIsOverMaxDirs = false;
				//从数据库拿出数据
				List<ScanDirectory> dbDirectories = mScanDirectoryService.getDirectoriesByDeviceId(mDevice.getDeviceID(), MAX_DIRS - mScanDirectory.size());
				if(dbDirectories != null && dbDirectories.size() > 0){
					for(ScanDirectory itemDirectory : dbDirectories){
						mScanDirectory.add(new File(itemDirectory.getPath()));
					}
					
					//Log.i(TAG, "dbDirecories:" + dbDirectories);
					//删除数据库中对应的数据
					mScanDirectoryService.deleteAll(dbDirectories);
				}
				
			}
			
			
			File dirFile = mScanDirectory.remove();
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
						if(mIsOverMaxDirs){
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
							mScanDirectory.add(subFile);
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
								else 
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
		LocalDeviceService localDeviceService = new LocalDeviceService();
		localDeviceService.update(mDevice);
		//Log.i(TAG, "FileScanThread end");
	}
	
	
}
