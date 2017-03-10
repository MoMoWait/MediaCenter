package com.rockchips.mediacenter.modle.task;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import android.media.iso.ISOManager;
import com.rockchips.mediacenter.bean.AllFileInfo;
import com.rockchips.mediacenter.bean.Device;
import com.rockchips.mediacenter.bean.FileInfo;
import com.rockchips.mediacenter.bean.LocalMediaFile;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.modle.db.FileInfoService;
import com.rockchips.mediacenter.utils.MediaFileUtils;

import android.os.AsyncTask;
import android.util.Log;

/**
 * @author GaoFei
 * 所有文件列表加载器
 */
public class AllFileLoadTask extends AsyncTask<Object, Integer, Integer> {
	private static final String TAG = "AllFileLoadTask";
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
							if(ISOManager.isBDDirectory(itemFile.getPath())){
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
						mFileInfos.add(fileInfo);
					}
				}
			}
			if(mFileInfos.size() > 1){
				Collections.sort(mFileInfos, new Comparator<FileInfo>() {

					@Override
					public int compare(FileInfo lhs, FileInfo rhs) {
						File lFile = new File(lhs.getPath());
						File rFile = new File(rhs.getPath());
						if(lFile.isDirectory() && rFile.isFile())
							return -1;
						else if(lFile.isDirectory() && rFile.isDirectory()
						        || lFile.isFile() && rFile.isFile())
						    return lFile.getPath().compareTo(rFile.getPath());
						else if(lFile.isFile() && rFile.isDirectory())
							return 1;
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
