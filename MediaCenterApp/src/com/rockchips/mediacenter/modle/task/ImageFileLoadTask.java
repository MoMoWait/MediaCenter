/**
 * 
 */
package com.rockchips.mediacenter.modle.task;

import java.util.ArrayList;
import java.util.List;

import com.rockchips.mediacenter.bean.Device;
import com.rockchips.mediacenter.bean.FileInfo;
import com.rockchips.mediacenter.modle.db.FileInfoService;
import com.rockchips.mediacenter.utils.MediaFileUtils;

import android.os.AsyncTask;
import android.util.Log;

/**
 * @author GaoFei
 *
 */
public class ImageFileLoadTask extends AsyncTask<Object, Integer, Integer>{

	private static final String TAG = "ImageFileLoadTask";
	public interface CallBack{
		void onGetFiles(List<FileInfo> fileInfos);
	}
	private CallBack mCallBack;
	private List<FileInfo> mFileInfos = new ArrayList<FileInfo>();
	public ImageFileLoadTask(CallBack callBack){
		mCallBack = callBack;
	}
	
	@Override
	protected Integer doInBackground(Object... params) {
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		Device device = (Device)params[0];
		int mediaType = (Integer)params[1];
		String currFolder = (String)params[2];
		boolean isLoadRoot = (Boolean)params[3];
		Log.i(TAG, "ImageFileLoadTask->doInBackgournd: " + mediaType);
		Log.i(TAG, "ImageFileLoadTask->currFolder: " + currFolder);
		Log.i(TAG, "ImageFileLoadTask->isLoadRoot: " + isLoadRoot);
		FileInfoService fileInfoService= new FileInfoService();
		if(currFolder.equals(device.getLocalMountPath()) && isLoadRoot){
			mFileInfos = fileInfoService.getAllFolders(device.getDeviceID(), mediaType, device.getLocalMountPath());
		}else{
			mFileInfos = fileInfoService.getFileInfos(device.getDeviceID(), currFolder, MediaFileUtils.getFileTypeFromFolderType(mediaType));
		}
		return null;
	}

	@Override
	protected void onPostExecute(Integer result) {
		mCallBack.onGetFiles(mFileInfos);
	}
}
