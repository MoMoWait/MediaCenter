/**
 * 
 */
package com.rockchips.mediacenter.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import com.rockchips.mediacenter.bean.Device;
import com.rockchips.mediacenter.bean.FileInfo;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.service.ProgressUpdateListener;
import momo.cn.edu.fjnu.androidutils.data.CommonValues;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * @author GaoFei
 * 文件操作工具
 */
public class FileOpUtils {
	private static final String TAG = "FileOpUtils";
	private static boolean isStopCopy;
	private FileOpUtils(){
		
	}
	/**
	 * 删除文件操作
	 * @param targetFile
	 * @return
	 */
	public static int deleteFile(FileInfo targetFileInfo, Device device){
		Log.i(TAG, "deleteFile->targetFileInfo:" + targetFileInfo);
		File targetFile = new File(targetFileInfo.getPath());
		//目录文件列表
		LinkedList<File> dirFiles = new LinkedList<File>();
		//待删除目录列表
		LinkedList<File> delDirFiles = new LinkedList<File>();
		//被删除的文件列表
		List<String> delFilePaths = new ArrayList<String>();
		if(targetFile.isFile()){
			targetFile.delete();
			delFilePaths.add(targetFile.getPath());
		}else{
			dirFiles.add(targetFile);
		}
		//删除文件
		while(!dirFiles.isEmpty()){
			File lastDirFile = dirFiles.removeFirst();
			if(!lastDirFile.canWrite())
				lastDirFile.setWritable(true);
			File[] childFiles = lastDirFile.listFiles();
			//空目录可以直接删除
			if(childFiles == null || childFiles.length == 0)
				lastDirFile.delete();
			else{
				int childLength = childFiles.length;
				int fileCount = 0;
				for(File childFile : childFiles){
					if(childFile.isFile()){
						++fileCount;
						if(!childFile.canWrite())
							childFile.setWritable(true);
						childFile.delete();
						delFilePaths.add(childFile.getPath());
					}else{
						dirFiles.add(childFile);
					}
				}
				if(fileCount == childLength)
					lastDirFile.delete();
				else
					delDirFiles.add(lastDirFile);
					
			}
		}
		
		//删除目录
		while(!delDirFiles.isEmpty()){
			delDirFiles.removeLast().delete();
		}
		//更新媒体库,Samba，NFS目录下的文件不更新
		if(delFilePaths != null && delDirFiles.size() > 0 && !targetFileInfo.getPath().startsWith(ConstData.NETWORK_DEVICE_MOUNT_DIR))
			MediaScannerConnection.scanFile(CommonValues.application, delFilePaths.toArray(new String[0]), null, null);
		//更新本地数据库
		/*FileInfoService fileInfoService = new FileInfoService();
		fileInfoService.deleteFileInfos(targetFileInfo.getDeviceID(), targetFileInfo.getPath());
		targetFile = new File(targetFileInfo.getPath());*/
		Log.i(TAG, "deleteFile->send rescan broad cast");
		String deviceID = ConstData.devicePathIDs.get(device.getLocalMountPath());
		if(deviceID != null){
			//更新本地数据库
			Intent broadIntent = new Intent(ConstData.BroadCastMsg.RESCAN_DEVICE);
			broadIntent.putExtra(ConstData.IntentKey.EXTRA_DEVICE_ID, deviceID);
			LocalBroadcastManager.getInstance(CommonValues.application).sendBroadcast(broadIntent);
		}
		//不存在，表示文件已经成功删除
		if(!targetFile.exists())
			return ConstData.FileOpErrorCode.NO_ERR;
		return ConstData.FileOpErrorCode.DELETE_PART_FILE_ERR;
		
	}
	
	/**
	 * 文件复制操作
	 * @param srcFile
	 * @param targetFile
	 * @return 拷贝结果
	 */
	public static int copyFile(File srcFile, File targetFile, ProgressUpdateListener updateListener){
		long allTotalSize = getAllFilesSize(srcFile);
		Log.i(TAG, "copyFile->allTotalSize:" + allTotalSize);
		long currentProgressSize = 0;
		if(srcFile.isFile()){
			return copyRealFile(srcFile, targetFile, updateListener, allTotalSize, currentProgressSize);
		}else{
			int totalFileCount = getFilesCount(srcFile);
			Log.i(TAG, "copyFile->totalFileCount:" + totalFileCount);
			int copyFileCount = 0;
			LinkedList<File> srcDirFiles = new LinkedList<File>();
			srcDirFiles.add(srcFile);
			String srcParentPath = srcFile.getParentFile().getPath();
			File targetParentFile = targetFile.getParentFile();
			File lastDirFile;
			while(!srcDirFiles.isEmpty()){
				File srcDirFile = srcDirFiles.removeFirst();
				String srcDirPath = srcDirFile.getPath();
				lastDirFile = new File(targetParentFile, srcDirPath.substring(srcParentPath.length() + 1));
				if(lastDirFile.mkdirs())
					++copyFileCount;
				File[] childFiles = srcDirFile.listFiles();
				if(childFiles != null && childFiles.length > 0){
					for(File childFile : childFiles){
						if(isStopCopy)
							return ConstData.FileOpErrorCode.STOP_PASTE;
						if(childFile.isFile()){
							int copyResult = copyRealFile(childFile, new File(lastDirFile, childFile.getName()), updateListener, allTotalSize, currentProgressSize);
							Log.i(TAG, "copyFile->copyResult:" + copyResult);
							if(copyResult == ConstData.FileOpErrorCode.NO_ERR)
								++copyFileCount;
							currentProgressSize += childFile.length();
							Log.i(TAG, "copyFile->currentProgressSize:" + allTotalSize);
						}else{
							srcDirFiles.add(childFile);
							/*if(new File(lastDirFile, childFile.getName()).mkdir())
								++copyFileCount;*/
						}
					}
				}
			}
			Log.i(TAG, "copyFile->copyFileCount:" + copyFileCount);
			if(copyFileCount == 0)
				return ConstData.FileOpErrorCode.PASTE_ERR;
			if(totalFileCount == copyFileCount)
				return ConstData.FileOpErrorCode.NO_ERR;
			return ConstData.FileOpErrorCode.PASTE_PART_FILE_ERR;
		}
	}
	
	/**
	 * 拷贝实际文件
	 * @param srcFile
	 * @param targetFile
	 * @throws Exception
	 */
	public static int copyRealFile(File srcFile, File targetFile, ProgressUpdateListener updateListener, long totalSize, long currentProgressSize){
		Log.i(TAG, "copyRealFile->srcFile:: " + srcFile.getPath());
		Log.i(TAG, "copyRealFile->targetFile: " + targetFile.getPath());
		BufferedInputStream srcInputStream = null;
		BufferedOutputStream targetOutputStream = null;
		try{
			boolean isCreateSuccess = targetFile.createNewFile();
			Log.i(TAG, "copyRealFile->isCreateSuccess:" + isCreateSuccess);
			if(isCreateSuccess){
				if(!targetFile.canWrite()){
					boolean isWriteable = targetFile.setWritable(true);
					//目标文件不可写
					if(!isWriteable)
						return ConstData.FileOpErrorCode.WRITE_ERR;
				}
			}else{
				//创建文件失败
				return ConstData.FileOpErrorCode.FILE_CREATE_FAILED;
			}
			
			if(!srcFile.canRead()){
				//设置可读
				boolean isReadable = srcFile.setReadable(true);
				if(!isReadable)
					return ConstData.FileOpErrorCode.READ_ERR;
			}
			long curretnSize = currentProgressSize;
			srcInputStream = new BufferedInputStream(new FileInputStream(srcFile));
			targetOutputStream = new BufferedOutputStream(new FileOutputStream(targetFile));
			byte[] buffer = new byte[2048];
			int readLength = 0;
			while((readLength = srcInputStream.read(buffer)) > 0){
				if(isStopCopy){
					srcInputStream.close();
					targetOutputStream.close();
					//删除已经复制的文件
					targetFile.delete();
					throw new Exception("Stop copy this File");
				}
				curretnSize += readLength;
				targetOutputStream.write(buffer, 0, readLength);
				updateListener.onUpdateProgress((int)(curretnSize * 1.0f / totalSize * 100));
				
			}
			targetOutputStream.flush();
		}catch (IOException e){
			Log.i(TAG, "copyRealFile->exception:" + e);
		}catch (Exception e) {
			return ConstData.FileOpErrorCode.STOP_PASTE;
		}finally{
			if(srcInputStream != null){
				try {
					srcInputStream.close();
				} catch (Exception e) {
					//no handle
				}
			}
			
			if(targetOutputStream != null){
				try {
					targetOutputStream.close();
				} catch (Exception e) {
					//no handle
				}
			}
		}
		
		return ConstData.FileOpErrorCode.NO_ERR;
	}
	
	/**
	 * 计算所有文件的字节数
	 * @param targetFile
	 * @return
	 */
	public static long getAllFilesSize(File targetFile){
		long total = 0;
		if(targetFile.isFile())
			total += targetFile.length();
		else{
			List<File> dirFiles = new LinkedList<File>();
			dirFiles.add(targetFile);
			while(!dirFiles.isEmpty()){
				File dirFile = dirFiles.remove(0);
				File[] childFiles = dirFile.listFiles();
				if(childFiles != null && childFiles.length > 0){
					for(File childFile : childFiles){
						if(childFile.isFile())
							total += childFile.length();
						else
							dirFiles.add(childFile);
					}
				}
			}
		}
		return total;
	}
	
	/**
	 * 获取所有子文件路径
	 * @param targetFile
	 * @return
	 */
	public static List<String> getAllFilePaths(File targetFile){
		Log.i(TAG, "getAllFilePaths->targetFile:" + targetFile);
		List<String> paths = new ArrayList<String>();
		if(targetFile.isFile())
			paths.add(targetFile.getPath());
		else{
			LinkedList<File> dirFiles = new LinkedList<File>();
			dirFiles.add(targetFile);
			while(!dirFiles.isEmpty()){
				File dirFile = dirFiles.removeFirst();
				File[] childFiles = dirFile.listFiles();
				if(childFiles != null && childFiles.length > 0){
					for(File childFile : childFiles){
						if(childFile.isFile())
							paths.add(childFile.getPath());
						else
							dirFiles.add(childFile);
					}
				}
			}
		}
		return paths;
	}
	
	/**
	 * 获取某个文件夹下所有的文件格式（包含文件夹）
	 * @param file
	 * @return
	 */
	public static int getFilesCount(File targetDirFile){
		int fileCount = 0;
		LinkedList<File> dirFiles = new LinkedList<File>();
		dirFiles.add(targetDirFile);
		while(!dirFiles.isEmpty()){
			File dirFile = dirFiles.removeFirst();
			++fileCount;
			File[] childFiles = dirFile.listFiles();
			if(childFiles != null && childFiles.length > 0){
				for(File childFile : childFiles){
					if(childFile.isFile())
						++fileCount;
					else
						dirFiles.add(childFile);
				}
			}
		}
	    return fileCount;
	}
	
	public static void setStopCopy(boolean stopCopy){
		isStopCopy = stopCopy;
	}
	
	public static boolean isStopCopy() {
		return isStopCopy;
	}
}
