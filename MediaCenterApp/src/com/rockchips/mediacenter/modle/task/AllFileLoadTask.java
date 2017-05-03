package com.rockchips.mediacenter.modle.task;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.R.anim;
import android.media.iso.ISOManager;
import com.rockchips.mediacenter.bean.Device;
import com.rockchips.mediacenter.bean.FileInfo;
import com.rockchips.mediacenter.bean.LocalMediaFile;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.modle.db.FileInfoService;
import com.rockchips.mediacenter.utils.MediaFileUtils;
import com.rockchips.mediacenter.utils.PlatformUtils;

import android.os.AsyncTask;
import android.util.Log;

/**
 * @author GaoFei
 * 所有文件列表加载器
 */
public class AllFileLoadTask extends AsyncTask<Object, Integer, Integer> {
	private static final String TAG = "AllFileLoadTask";
	private int mSortWay;
	private int mSortType;
	public interface CallBack{
		void onGetFiles(List<FileInfo> fileInfos);
	}
	
	private CallBack mCallBack;
	private List<FileInfo> mFileInfos = new ArrayList<FileInfo>();
	public AllFileLoadTask(CallBack callBack){
		mCallBack = callBack;
	}
	
	
	@Override
	protected Integer doInBackground(Object... params) {
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		Device device = (Device)params[0];
		int mediaType = (Integer)params[1];
		String currFolder = (String)params[2];
		FileInfoService fileInfoService = new FileInfoService();
		if(mediaType == ConstData.MediaType.FOLDER){
			mSortWay = (Integer)params[3];
			mSortType = (Integer)params[4];
			File dirFile = new File(currFolder);
			FileInfo fileInfo;
			if(dirFile.exists()){
				File[] files = dirFile.listFiles();
				if(files != null && files.length > 0){
					for(File itemFile : files){
						fileInfo = new FileInfo();
						fileInfo.setDeviceID(device.getDeviceID());
						fileInfo.setModifyTime(itemFile.lastModified());
						fileInfo.setName(itemFile.getName());
						fileInfo.setPath(itemFile.getPath());
						fileInfo.setParentPath(currFolder);
						if(itemFile.isDirectory()){
							//如果是蓝光文件夹
							if(PlatformUtils.getSDKVersion() >= 23 && ISOManager.isBDDirectory(itemFile.getPath())){
								fileInfo.setType(ConstData.MediaType.VIDEO);
							}else{
								fileInfo.setType(ConstData.MediaType.FOLDER);
								fileInfo.setChildCount(itemFile.listFiles().length);
								fileInfo.setSize(itemFile.length());
							}
						}else{
							fileInfo.setSize(itemFile.length());
							fileInfo.setType(MediaFileUtils.getMediaTypeFromFile(itemFile));
						}
						if(!PlatformUtils.isSupportIPTV())
							mFileInfos.add(fileInfo);
						else if(fileInfo.getType() != ConstData.MediaType.APK)
							mFileInfos.add(fileInfo);
					}
				}
			}
			if(mFileInfos.size() > 1){
				Collections.sort(mFileInfos, new Comparator<FileInfo>() {

					@Override
					public int compare(FileInfo lhs, FileInfo rhs) {
						if(mSortWay == ConstData.FILE_SORT_WAY.NAME){
							File lFile = new File(lhs.getPath());
							File rFile = new File(rhs.getPath());
							if(mSortType == ConstData.FILE_SORT_TYPE.INCREASING)
								return lFile.getName().compareTo(rFile.getName());
							return rFile.getName().compareTo(lFile.getName());
						}else if(mSortWay == ConstData.FILE_SORT_WAY.TIME){
							File lFile = new File(lhs.getPath());
							File rFile = new File(rhs.getPath());
							if(mSortType == ConstData.FILE_SORT_TYPE.INCREASING){
								if(lFile.lastModified() < rFile.lastModified())
									return -1;
								else if(lFile.lastModified() > rFile.lastModified())
									return 1;
								else
									return lFile.getName().compareTo(rFile.getName());
							}else{
								if(lFile.lastModified() < rFile.lastModified())
									return -1;
								else if(lFile.lastModified() > rFile.lastModified())
									return 1;
								else
									return lFile.getName().compareTo(rFile.getName());
							}
						}else if(mSortWay == ConstData.FILE_SORT_WAY.TYPE){
							File lFile = new File(lhs.getPath());
							File rFile = new File(rhs.getPath());
							if(lFile.isDirectory() && rFile.isFile()){
								if(mSortType == ConstData.FILE_SORT_TYPE.INCREASING)
									return -1;
								return 1;
							}else if(lFile.isDirectory() && rFile.isDirectory()
							        || lFile.isFile() && rFile.isFile())
							    return lFile.getName().compareTo(rFile.getName());
							else if(lFile.isFile() && rFile.isDirectory()){
								if(mSortType == ConstData.FILE_SORT_TYPE.INCREASING)
									return 1;
								return -1;
							}
							
						}else if(mSortWay == ConstData.FILE_SORT_WAY.SIZE){
							File lFile = new File(lhs.getPath());
							File rFile = new File(rhs.getPath());
							if(mSortType == ConstData.FILE_SORT_TYPE.INCREASING){
								if(lFile.isFile() && rFile.isFile()){
									if(rFile.length() > lFile.length())
										return -1;
									else if(rFile.length() < lFile.length())
										return 1;
								}else if(lFile.isDirectory() && rFile.isFile()){
									return -1;
								}else if(lFile.isFile() && rFile.isDirectory()){
									return 1;
								}else{
									return lFile.getName().compareTo(rFile.getName());
								}
							}else{
								if(lFile.isFile() && rFile.isFile()){
									if(rFile.length() > lFile.length())
										return 1;
									else if(rFile.length() < lFile.length())
										return -1;
								}else if(lFile.isDirectory() && rFile.isFile()){
									return 1;
								}else if(lFile.isFile() && rFile.isDirectory()){
									return -1;
								}else{
									return lFile.getName().compareTo(rFile.getName());
								}
							
							}
						}
						return 0;
					}
				});
			}
		}else {
			if(currFolder.equals(device.getLocalMountPath())){
				mFileInfos = fileInfoService.getAllFolders(device.getDeviceID(), mediaType, device.getLocalMountPath());
			}else{
				mFileInfos = fileInfoService.getFileInfos(device.getDeviceID(), currFolder, MediaFileUtils.getFileTypeFromFolderType(mediaType));
			}
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(Integer result) {
		mCallBack.onGetFiles(mFileInfos);
	}
	
	
	/**
	 * 同步调用task
	 * @param params
	 */
	public void run(Object... params){
		doInBackground(params);
		mCallBack.onGetFiles(mFileInfos);
	}
	
}
