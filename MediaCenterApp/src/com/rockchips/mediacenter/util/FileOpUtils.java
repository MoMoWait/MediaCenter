/**
 * 
 */
package com.rockchips.mediacenter.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.LinkedList;

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
	public static boolean deleteFile(File targetFile){
		//目录文件列表
		LinkedList<File> dirFiles = new LinkedList<File>();
		//待删除目录列表
		LinkedList<File> delDirFiles = new LinkedList<File>();
		if(targetFile.isFile())
			targetFile.delete();
		else
			dirFiles.add(targetFile);
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
		return true;
	}
	
	/**
	 * 文件复制操作
	 * @param srcFile
	 * @param targetFile
	 * @return
	 */
	public static boolean copyFile(File srcFile, File targetFile){
		//父文件夹相同，无法复制
		//不能将父文件夹复制到子文件夹
		if(srcFile.isFile()){
			try{
				copyRealFile(srcFile, targetFile);
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
							copyFile(childFile, new File(lastDirFile, childFile.getName()));
						}catch (Exception e){
							Log.i(TAG, "copyFile->exception2:" + e);
						}
					}else{
						new File(lastDirFile, childFile.getPath()).mkdir();
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
	public static void copyRealFile(File srcFile, File targetFile) throws Exception{
		FileInputStream srcInputStream = new FileInputStream(srcFile);
		FileOutputStream targetOutputStream = new FileOutputStream(targetFile);
		FileChannel inChannel = srcInputStream.getChannel();
		FileChannel outChannel = targetOutputStream.getChannel();
		inChannel.transferTo(0, inChannel.size(), outChannel);
		inChannel.close();
		outChannel.close();
		srcInputStream.close();
		targetOutputStream.close();
	}
	
}
