/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Filename:    FolderAddFileInfo.java  
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2012-4-17 下午04:49:59  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2012-4-17      fxw         1.0         create
*******************************************************************/   


package com.rockchip.mediacenter.mediaserver.model;

import com.rockchip.mediacenter.mediaplayer.model.FileInfo;
import com.rockchip.mediacenter.mediaplayer.model.LastLevelFileInfo;

/**
 * 添加共享目录
 * @author fxw
 * @since 1.0
 */
public final class FolderAddFileInfo extends FileInfo {

public static final String FOLDER_ADD_PATH = "/+++";
	
	public FolderAddFileInfo(){
		setPath(FOLDER_ADD_PATH);
		setDir(true);
	}
	
	public static String getLastLevelPath(){
		return FOLDER_ADD_PATH;
	}
	
	public static boolean isFolderAddFileInfo(FileInfo fileInfo){
		if(fileInfo==null) return false;
		return (fileInfo instanceof LastLevelFileInfo)||FOLDER_ADD_PATH.equals(fileInfo.getPath());
	}
	
	public static boolean isFolderAddPath(String path){
		return FOLDER_ADD_PATH.equals(path);
	}
	
	
}
