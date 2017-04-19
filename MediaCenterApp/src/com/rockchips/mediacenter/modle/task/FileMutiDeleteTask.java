/**
 * 
 */
package com.rockchips.mediacenter.modle.task;

import java.io.File;
import java.util.List;

import com.rockchips.mediacenter.bean.Device;
import com.rockchips.mediacenter.bean.FileInfo;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.utils.FileOpUtils;

import android.os.AsyncTask;

/**
 * @author GaoFei
 * 多文件删除任务
 */
public class FileMutiDeleteTask extends AsyncTask<Object, Integer, Integer>{
	public interface Callback{
		void onFinished(int errCode);
		void onProgress(int value);
	}
	private Callback mCallback;
	private List<FileInfo> mFileInfos;
	private Device mCurrDevice;
	public FileMutiDeleteTask(Callback callback, Device device, List<FileInfo> fileInfos){
		mCallback = callback;
		mFileInfos = fileInfos;
		mCurrDevice = device;
	}
	
	@Override
	protected Integer doInBackground(Object... params) {
		int result;
		int delCount = 0;
		for(FileInfo itemFileInfo : mFileInfos){
			File delFile = new File(itemFileInfo.getPath());
			if(!delFile.canWrite()){
				//尝试设置可写
				if(delFile.setWritable(true)){
					FileOpUtils.deleteFile(itemFileInfo, mCurrDevice);
				}
					
			}else{
				result = FileOpUtils.deleteFile(itemFileInfo, mCurrDevice);
			}
			if(!new File(itemFileInfo.getPath()).exists())
				++delCount;
		}
		if(delCount == mFileInfos.size())
			result = ConstData.FileOpErrorCode.NO_ERR;
		else if(delCount == 0)
			result = ConstData.FileOpErrorCode.DELETE_ERR;
		else
			result = ConstData.FileOpErrorCode.DELETE_PART_FILE_ERR;
		return result;

	}
	
	@Override
	protected void onPostExecute(Integer result) {
		mCallback.onFinished(result);
	}
}
