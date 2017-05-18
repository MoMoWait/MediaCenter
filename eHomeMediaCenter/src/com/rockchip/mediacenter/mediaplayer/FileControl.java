/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Filename:    FileControl.java  
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2011-9-27 上午11:11:42  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2011-9-27      xwf         1.0         create
*******************************************************************/   


package com.rockchip.mediacenter.mediaplayer;

import java.util.ArrayList;
import java.util.List;

import com.rockchip.mediacenter.core.upnp.Device;
import com.rockchip.mediacenter.core.upnp.DeviceCache;
import com.rockchip.mediacenter.mediaplayer.util.FileInfoRenderUtil;

public class FileControl {
	
	public static final String TOP_DIR = "D L N A";
	private MediaPlayer mMediaPlayer;
	private FileExplorer mFileExplorer;
	private List<String> mSavePathList;
	private int mSavePoint = 0;
	private boolean canDeletePath = false;
	private String mParentPath;
	private String mCurrentPath;
	
	public FileControl(MediaPlayer mediaPlayer, FileExplorer fileExplorer){
		mMediaPlayer = mediaPlayer;
		mFileExplorer = fileExplorer;
		init();
	}
	
	/**
	 * 初始化
	 */
	public void init(){
		mSavePathList = new ArrayList<String>();
		mSavePoint = 0;
	}
	
	/**
	 * 刷新当前目录
	 */
	public void refreshDirectory(){
		if(isTopDirectory(mCurrentPath)){
			mFileExplorer.clearCache(false);
			browseTopDirectory(false);
		}else{
			browseDirectory(mCurrentPath);
		}
	}
	
	/**
	 * 浏览顶层目录
	 * @param isUpdatePath 是否修改路径
	 */
	public void browseTopDirectory(boolean isUpdatePath){
		if(isUpdatePath){
			deleteSavePath();
			addSavePath(TOP_DIR);
		}
		mFileExplorer.getDLNAContent(TOP_DIR);
		mCurrentPath = TOP_DIR;
		updateTitle();
	}

	/**
	 * 浏览上个目录
	 * 更改路径保存点
	 */
	public boolean browsePreviousDirectory(){
		if(mSavePathList.size() > 1){
			canDeletePath = true;	
			if(mSavePoint >= 1){
				if(mSavePathList.get(mSavePoint - 1).equals(TOP_DIR)){
					browseTopDirectory(false);
				}else{
					browseDirectory(mSavePathList.get(mSavePoint - 1));
				}	
				mSavePoint --;
				return true;
			}
		}
		return false;
	}
	/**
	 * 浏览上级目录
	 */
	public boolean browseLastLevelDirectory(){
		if(!TOP_DIR.equals(mCurrentPath)&&mCurrentPath!=null){
			String path = FileInfoRenderUtil.getDLNAFileParentPath(mCurrentPath);
			if(path!=null&&!path.equals(mCurrentPath)){
				browseDirectory(path);
				//addSavePath(path);
				return true;
			}
			return false;
		}else{
			return false;
		}
		
	}
	
	/**
	 * 浏览下个目录
	 * 更改路径保存点
	 */
	public boolean browseNextDirectory(){
		if(mSavePoint < (mSavePathList.size() - 1)){
			mSavePoint ++;
			if(mSavePathList.get(mSavePoint).equals(TOP_DIR)){
				browseTopDirectory(false);
			}else{
				browseDirectory(mSavePathList.get(mSavePoint));
			}
			return true;
		}
		return false;
	}
	
	/**
	 * 浏览Direct目录
	 */
	public void browseDirectDirectory(String path){
		deleteSavePath();
		browseDirectory(path);
		addSavePath(path);
	}
	
	/**
	 * 浏览目录
	 */
	private void browseDirectory(String path){
		mFileExplorer.getDLNAContent(path);
		updateTitle();
	}
	
	/**
	 * 添加目录
	 */
	public void addSavePath(String path){
		mSavePathList.add(path);
		mSavePoint = mSavePathList.size()-1;
	}
	
	/**
	 * 删除目录
	 */
	public void deleteSavePath(){
		if(canDeletePath){
			canDeletePath = false;
			int size = mSavePathList.size()-1;
			for(int i = size; i > mSavePoint; i--){
				mSavePathList.remove(i);
			}
		}
	}

	/**
	 * 设置标题
	 * @return
	 */
	public void updateTitle(){
		mMediaPlayer.updateTitle();
	}
	
	public boolean isTopDirectory(String path){
		return TOP_DIR.equals(path);
	}
	
	public String getCurrentPath() {
		return mCurrentPath;
	}
	public void setCurrentPath(String currentPath) {
		this.mCurrentPath = currentPath;
	}
	public String getParentPath() {
		return mParentPath;
	}
	public void setParentPath(String parentPath) {
		this.mParentPath = parentPath;
	}
	
	/**
	 * 获得当前设备
	 */
	public Device getCurrentDevice(){
		if(mCurrentPath==null) return null;
		
		String udn = FileInfoRenderUtil.getDLNAFileDeviceID(mCurrentPath);
		return DeviceCache.getInstance().getDeviceByUDN(udn);
	}
}
