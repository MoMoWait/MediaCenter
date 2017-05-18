/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Filename:    FileDirectoryManager.java  
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2011-9-3 下午04:50:43  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2011-9-3      xwf         1.0         create
*******************************************************************/   


package com.rockchip.mediacenter.mediaserver;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import com.rockchip.mediacenter.mediaplayer.model.FileInfo;
import com.rockchip.mediacenter.mediaserver.constants.EnvironmentConst;

public class FileDirectoryManager {
	
	private Listener mListener;
	private boolean isOnlyDirectory = true;
	private FileFilter mFileFilter;
	private List<String> mSavePathList;
	private int mSavePoint = 0;
	private boolean canDeletePath = false;
	private String mCurrentPath;
	
	public FileDirectoryManager(Listener listener){
		mListener = listener;
		init();
	}
	
	public void setOnlyDirectory(boolean flag){
		isOnlyDirectory = flag;
	}
	
	public void setFileFilter(FileFilter fileFilter){
		mFileFilter = fileFilter;
	}
	
	/**
	 * 初始化
	 */
	public void init(){
		mSavePathList = new ArrayList<String>();
		//mSavePathList.add(EnvironmentConst.TOP_DIR);
		mSavePoint = 0;
	}
	
	/**
	 * 浏览顶层目录
	 * @param isUpdatePath 是否修改路径
	 */
	public void browseTopDirectory(boolean isUpdatePath){
		if(isUpdatePath){
			deleteSavePath();
			addSavePath(EnvironmentConst.TOP_DIR);
		}
		mCurrentPath = EnvironmentConst.TOP_DIR;
		showFileList(getTopLevelDirecotry());
		updateTitle();
	}
	public void browseTopDirectory(){
		browseTopDirectory(true);
	}

	/**
	 * 浏览下一级目录
	 * 更改路径保存点
	 * @param fileInfo
	 */
	public void browseDirectChildren(FileInfo fileInfo){
		if(fileInfo.isFileItem()){
			File file = (File)fileInfo.getItem();
			browseDirectChildren(file);
		}
	}
	public void browseDirectChildren(String path){
		browseDirectChildren(new File(path));
	}
	public void browseDirectChildren(File file){
		deleteSavePath();
		browseDirectory(file);
		addSavePath(file.getPath());
	}
	
	/**
	 * 浏览上个目录
	 * 更改路径保存点
	 */
	public boolean browsePreviousDirectory(){
		if(mSavePathList.size() > 1){
			canDeletePath = true;	
			if(mSavePoint >= 1){
				if(mSavePathList.get(mSavePoint - 1).equals(EnvironmentConst.TOP_DIR)){
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
	 * 浏览下个目录
	 * 更改路径保存点
	 */
	public boolean browseNextDirectory(){
		if(mSavePoint < (mSavePathList.size() - 1)){
			mSavePoint ++;
			if(mSavePathList.get(mSavePoint).equals(EnvironmentConst.TOP_DIR)){
				browseTopDirectory(false);
			}else{
				browseDirectory(mSavePathList.get(mSavePoint));
			}
			return true;
		}
		return false;
	}
	
	/**
	 * 浏览上一级目录
	 */
	public void browseLastLevelDirectory(){
		File currrentFile = new File(mCurrentPath);
		File parent = currrentFile.getParentFile();
		if(parent!=null)
			browseDirectory(parent);
		else
			browseTopDirectory();
	}
	
	/**
	 * 浏览目录
	 */
	public void browseDirectory(FileInfo fileInfo){
		if(fileInfo.isFileItem()){
			File file = (File)fileInfo.getItem();
			browseDirectory(file);
		}
	}
	public void browseDirectory(String path){
		browseDirectory(new File(path));
	}
	public void browseDirectory(final File file){
		if(isTopDirectory(file.getPath())){
			browseTopDirectory(false);
		}else{
			new Thread(){
				public void run() {
					final List<FileInfo> fileInfoList = new ArrayList<FileInfo>();
					File[] files = null;
					if(mFileFilter!=null)
						files = file.listFiles(mFileFilter);
					else
						files = file.listFiles();
					
					if(files!=null){
						for(File item : files){
							if(item.canRead()){
								if(item.isDirectory()){
									fileInfoList.add(0, convertFile(item));
								}else if(item.isFile()&&!isOnlyDirectory){
									fileInfoList.add(convertFile(item));
								}
							}
						}
					}
					mCurrentPath = file.getPath();
					showFileList(fileInfoList);
					updateTitle();
				}
			}.start();
		}
	}
	
	
	/**
	 * 显示文件列表
	 */
	private void showFileList(List<FileInfo> fileInfoList){
		mListener.setDataSource(fileInfoList);
	}
	
	/**
	 * 获取顶级目录
	 * @return
	 */
	public List<FileInfo> getTopLevelDirecotry(){
		return mListener.getTopLevelDirecotry();
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
	 * 转换文件为文件实体对象
	 */
	public FileInfo convertFile(File file){
		if(file==null) return null;
		FileInfo fileInfo = new FileInfo(file);
		if(file.isDirectory()){
			fileInfo.setDir(true);
		}else{
			fileInfo.setDir(false);
		}
		return fileInfo;
	}
	
	/**
	 * 更新标题
	 * @return
	 */
	public void updateTitle(){
		mListener.updateTitle(mCurrentPath);
	}
	
	public String getCurrentPath() {
		return mCurrentPath;
	}
	
	public boolean isTopDirectory(){
		return isTopDirectory(mCurrentPath);
	}
	public static boolean isTopDirectory(String path){
		return EnvironmentConst.TOP_DIR.equals(path);
	}
	
	public interface Listener {
		public void updateTitle(String currentPath);
		public void setDataSource(List<FileInfo> fileInfoList);
		public List<FileInfo> getTopLevelDirecotry();
	}
	
}
