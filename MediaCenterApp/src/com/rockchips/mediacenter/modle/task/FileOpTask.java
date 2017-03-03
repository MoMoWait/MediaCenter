/**
 * 
 */
package com.rockchips.mediacenter.modle.task;

import java.io.File;

import momo.cn.edu.fjnu.androidutils.utils.StorageUtils;

import com.rockchips.mediacenter.bean.AllFileInfo;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.utils.FileOpUtils;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

/**
 * @author GaoFei
 * 文件操作任务
 */
public class FileOpTask extends AsyncTask<AllFileInfo, Integer, Integer> {
	
	private static final String TAG = "FileOpTask";
	
	public interface CallBack{
		void onFinish();
	}
	
	/**
	 * 操作模式
	 */
	private int mOpMode;
	private CallBack mCallBack;
	private AllFileInfo mAllFileInfo;
	public FileOpTask(CallBack callBack){
		mCallBack = callBack;
	}
	/**
	 * 设置操作模式
	 * @param mode
	 */
	public void setOpMode(int mode){
		mOpMode = mode;
	}
	@Override
	protected Integer doInBackground(AllFileInfo... params) {
		mAllFileInfo = params[0];
		switch(mOpMode){
			case ConstData.FileOpMode.COPY:
				break;
			case ConstData.FileOpMode.DELETE:
				FileOpUtils.deleteFile(mAllFileInfo.getFile());
				break;
			case ConstData.FileOpMode.MOVE:
				break;
			case ConstData.FileOpMode.PASTE:
				String srcPath = StorageUtils.getDataFromSharedPreference(ConstData.SharedKey.COPY_FILE_PATH);
				Log.i(TAG, "paste srcPath:" + srcPath);
				Log.i(TAG, "current directory:" + mAllFileInfo.getFile().getParentFile().getPath());
				if(!TextUtils.isEmpty(srcPath)){
					File srcCopyFile = new File(srcPath);
					//拷贝文件
					FileOpUtils.copyFile(srcCopyFile, new File(mAllFileInfo.getFile().getParentFile(), srcCopyFile.getName()));
				}else{
					//移动文件
					srcPath = StorageUtils.getDataFromSharedPreference(ConstData.SharedKey.MOVE_FILE_PATH);
					File srcMoveFile = new File(srcPath);
					if(TextUtils.isEmpty(srcPath))
						return ConstData.TaskExecuteResult.SUCCESS;
					//拷贝文件
					FileOpUtils.copyFile(srcMoveFile, new File(mAllFileInfo.getFile().getParentFile(), srcMoveFile.getName()));
					//删除源文件
					FileOpUtils.deleteFile(srcMoveFile);
				}
				StorageUtils.saveDataToSharedPreference(ConstData.SharedKey.COPY_FILE_PATH, "");
				StorageUtils.saveDataToSharedPreference(ConstData.SharedKey.MOVE_FILE_PATH, "");
				break;
			case ConstData.FileOpMode.RENAME:
				break;
		}
		return ConstData.TaskExecuteResult.SUCCESS;
	}

	@Override
	protected void onPostExecute(Integer result) {
		mCallBack.onFinish();
	}
}
