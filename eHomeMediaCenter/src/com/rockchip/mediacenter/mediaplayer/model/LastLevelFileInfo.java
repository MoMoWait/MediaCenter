/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Filename:    LastLevelFileInfo.java  
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2012-4-16 下午05:16:17  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2012-4-16      fxw         1.0         create
*******************************************************************/   


package com.rockchip.mediacenter.mediaplayer.model;

/**
 * 上一级目录
 * @author fxw
 * @since 1.0
 */
public final class LastLevelFileInfo extends FileInfo {
	
	public static final String LAST_LEVEL_PATH = "/...";
	
	public LastLevelFileInfo(){
		setPath(LAST_LEVEL_PATH);
		setDir(true);
	}
	
	public static String getLastLevelPath(){
		return LAST_LEVEL_PATH;
	}
	
	public static boolean isLastLevelFileInfo(FileInfo fileInfo){
		if(fileInfo==null) return false;
		return (fileInfo instanceof LastLevelFileInfo)||LAST_LEVEL_PATH.equals(fileInfo.getPath());
	}
	
	public static boolean isLastLevelPath(String path){
		return LAST_LEVEL_PATH.equals(path);
	}
}
