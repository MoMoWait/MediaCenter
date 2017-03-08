/**
 * 
 */
package com.rockchips.mediacenter.modle.task;

import java.io.File;
import momo.cn.edu.fjnu.androidutils.utils.StorageUtils;
import com.rockchips.mediacenter.bean.FileInfo;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.service.ProgressUpdateListener;
import com.rockchips.mediacenter.utils.FileOpUtils;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

/**
 * @author GaoFei
 * 文件操作任务
 */
public class FileOpTask extends AsyncTask<FileInfo, Integer, Integer> {
	
	private static final String TAG = "FileOpTask";
	
	public interface CallBack{
		void onFinish(int errorCode);
		void onProgress(int value);
	}
	
	/**
	 * 操作模式
	 */
	private int mOpMode;
	private CallBack mCallBack;
	private FileInfo mFileInfo;
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
	protected Integer doInBackground(FileInfo... params) {
		mFileInfo = params[0];
		int result = ConstData.FileOpErrorCode.NO_ERR;
		switch(mOpMode){
			case ConstData.FileOpMode.COPY:
				break;
			case ConstData.FileOpMode.DELETE:
				File delFile = new File(mFileInfo.getPath());
				if(!delFile.canWrite()){
					//没有写权限
					result = ConstData.FileOpErrorCode.WRITE_ERR;
				}else{
					FileOpUtils.deleteFile(mFileInfo);
					result = ConstData.FileOpErrorCode.NO_ERR;
				}
				
				break;
			case ConstData.FileOpMode.MOVE:
				break;
			case ConstData.FileOpMode.PASTE:
				String srcPath = StorageUtils.getDataFromSharedPreference(ConstData.SharedKey.COPY_FILE_PATH);
				Log.i(TAG, "paste srcPath:" + srcPath);
				Log.i(TAG, "current directory:" + new File(mFileInfo.getPath()).getParentFile().getPath());
				if(!TextUtils.isEmpty(srcPath)){
					File srcCopyFile = new File(srcPath);
					//拷贝文件
					FileOpUtils.copyFile(srcCopyFile, new File(new File(mFileInfo.getParentPath()), srcCopyFile.getName()), new ProgressUpdateListener() {
						
						@Override
						public void onUpdateProgress(int value) {
							publishProgress(value);
						}
					});
				}else{
					//移动文件
					srcPath = StorageUtils.getDataFromSharedPreference(ConstData.SharedKey.MOVE_FILE_PATH);
					File srcMoveFile = new File(srcPath);
					if(TextUtils.isEmpty(srcPath))
						return ConstData.TaskExecuteResult.SUCCESS;
					//拷贝文件
					FileOpUtils.copyFile(srcMoveFile, new File(new File(mFileInfo.getPath()).getParentFile(), srcMoveFile.getName()), new ProgressUpdateListener() {
						
						@Override
						public void onUpdateProgress(int value) {
							publishProgress(value);
						}
					});
					//删除源文件
					//FileOpUtils.deleteFile(srcMoveFile);
				}
				StorageUtils.saveDataToSharedPreference(ConstData.SharedKey.COPY_FILE_PATH, "");
				StorageUtils.saveDataToSharedPreference(ConstData.SharedKey.MOVE_FILE_PATH, "");
				break;
			case ConstData.FileOpMode.RENAME:
				break;
		}
		return result;
	}

	@Override
	protected void onPostExecute(Integer result) {
		mCallBack.onFinish(result);
	}
	
	@Override
	protected void onProgressUpdate(Integer... values) {
		mCallBack.onProgress(values[0]);
	}
	
}
