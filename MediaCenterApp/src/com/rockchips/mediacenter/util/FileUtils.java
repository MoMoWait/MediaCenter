package com.rockchips.mediacenter.util;

import java.io.File;

/**
 * @author GaoFei
 * 文件操作工具
 */
public class FileUtils {
	/**
	 * 是否存在某个文件
	 * @param dirFile
	 * @param path
	 * @return
	 */
	public static boolean isExist(File dirFile, String path){
		boolean isExist = false;
		File[] childFiles = dirFile.listFiles();
		if(childFiles != null && childFiles.length > 0){
			for(File childFile : childFiles){
				if(childFile.getPath().equals(path)){
					isExist = true;
					break;
				}
			}
		}
		return isExist;
	}
	
	
	
}
