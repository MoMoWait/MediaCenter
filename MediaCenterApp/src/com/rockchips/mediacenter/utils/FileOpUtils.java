/**
 * 
 */
package com.rockchips.mediacenter.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.rockchips.mediacenter.bean.FileInfo;
import com.rockchips.mediacenter.modle.db.FileInfoService;
import com.rockchips.mediacenter.service.ProgressUpdateListener;

import momo.cn.edu.fjnu.androidutils.data.CommonValues;
import android.media.MediaScannerConnection;
import android.util.Log;

/**
 * @author GaoFei
 * 文件操作工具
 */
public class FileOpUtils {
	private static final String TAG = "FileOpUtils";
	
	private FileOpUtils(){
		
	}
	/**
	 * 删除文件操作
	 * @param targetFile
	 * @return
	 */
	public static boolean deleteFile(FileInfo targetFileInfo){
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
		//更新媒体库
		if(delFilePaths != null && delDirFiles.size() > 0)
			MediaScannerConnection.scanFile(CommonValues.application, delFilePaths.toArray(new String[0]), null, null);
		//更新本地数据库
		FileInfoService fileInfoService = new FileInfoService();
		fileInfoService.deleteFileInfos(targetFileInfo.getDeviceID(), targetFileInfo.getPath());
		return true;
	}
	
	/**
	 * 文件复制操作
	 * @param srcFile
	 * @param targetFile
	 * @return
	 */
	public static boolean copyFile(File srcFile, File targetFile, ProgressUpdateListener updateListener){
		//父文件夹相同，无法复制
		//不能将父文件夹复制到子文件夹
		long allTotalSize = getAllFilesSize(srcFile);
		Log.i(TAG, "copyFile->allTotalSize:" + allTotalSize);
		long currentProgressSize = 0;
		if(srcFile.isFile()){
			try{
				copyRealFile(srcFile, targetFile, updateListener, allTotalSize, currentProgressSize);
			}catch (Exception e){
				Log.i(TAG, "copyFile->exception1:" + e);
			}
			
		}else{
			LinkedList<File> srcDirFiles = new LinkedList<File>();
			srcDirFiles.add(srcFile);
			if(!targetFile.exists())
				targetFile.mkdir();
			File lastDirFile = targetFile.getParentFile();
			while(!srcDirFiles.isEmpty()){
				File srcDirFile = srcDirFiles.removeFirst();
				lastDirFile = new File(lastDirFile, srcDirFile.getName());
				File[] childFiles = srcDirFile.listFiles();
				for(File childFile : childFiles){
					if(childFile.isFile()){
						try{
							copyRealFile(childFile, new File(lastDirFile, childFile.getName()), updateListener, allTotalSize, currentProgressSize);
							currentProgressSize += childFile.length();
							Log.i(TAG, "copyFile->currentProgressSize:" + allTotalSize);
						}catch (Exception e){
							Log.i(TAG, "copyFile->exception2:" + e);
						}
					}else{
						srcDirFiles.add(childFile);
						new File(lastDirFile, childFile.getName()).mkdir();
					}
				}
			}
			
		}
		return true;
	}
	
	/**
	 * 拷贝实际文件
	 * @param srcFile
	 * @param targetFile
	 * @throws Exception
	 */
	public static void copyRealFile(File srcFile, File targetFile, ProgressUpdateListener updateListener, long totalSize, long currentProgressSize){
		try{
			long curretnSize = currentProgressSize;
			BufferedInputStream srcInputStream = new BufferedInputStream(new FileInputStream(srcFile));
			BufferedOutputStream targetOutputStream = new BufferedOutputStream(new FileOutputStream(targetFile));
			byte[] buffer = new byte[2048];
			int readLength = 0;
			while((readLength = srcInputStream.read(buffer)) > 0){
				curretnSize += readLength;
				targetOutputStream.write(buffer, 0, readLength);
				updateListener.onUpdateProgress((int)(curretnSize * 1.0f / totalSize * 100));
				
			}
			targetOutputStream.flush();
			targetOutputStream.close();
			srcInputStream.close();
		}catch (Exception e){
			Log.i(TAG, "copyRealFile->exception:" + e);
		}
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
	
}
