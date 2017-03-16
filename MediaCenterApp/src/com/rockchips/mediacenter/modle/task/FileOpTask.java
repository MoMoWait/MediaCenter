/**
 * 
 */
package com.rockchips.mediacenter.modle.task;

import java.io.File;
import java.util.List;

import org.json.JSONObject;

import momo.cn.edu.fjnu.androidutils.data.CommonValues;
import momo.cn.edu.fjnu.androidutils.utils.JsonUtils;
import momo.cn.edu.fjnu.androidutils.utils.StorageUtils;

import com.rockchips.mediacenter.bean.Device;
import com.rockchips.mediacenter.bean.FileInfo;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.service.ProgressUpdateListener;
import com.rockchips.mediacenter.utils.FileOpUtils;

import android.content.Intent;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
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
	private Device mDevice;
	public FileOpTask(Device device, CallBack callBack){
		mCallBack = callBack;
		mDevice = device;
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
					//尝试设置可写
					if(!delFile.setWritable(true))
						//没有写权限
						result = ConstData.FileOpErrorCode.WRITE_ERR;
					else{
						result = FileOpUtils.deleteFile(mFileInfo, mDevice);
					}
						
				}else{
					result = FileOpUtils.deleteFile(mFileInfo, mDevice);
				}
				
				break;
			case ConstData.FileOpMode.MOVE:
				break;
			case ConstData.FileOpMode.PASTE:
				String strSrcCopy = StorageUtils.getDataFromSharedPreference(ConstData.SharedKey.COPY_FILE_PATH);
				String strSrcMove = StorageUtils.getDataFromSharedPreference(ConstData.SharedKey.MOVE_FILE_PATH);
				Log.i(TAG, "paste srcPath:" + strSrcCopy);
				FileInfo srcFileInfo = null;
				if(!TextUtils.isEmpty(strSrcCopy)){
					try{
						srcFileInfo = (FileInfo)JsonUtils.jsonToObject(FileInfo.class, new JSONObject(strSrcCopy));
						Log.i(TAG, "paste srcFileInfo:" + srcFileInfo);
					}catch (Exception e){
						
					}
					if(srcFileInfo == null){
						result = ConstData.FileOpErrorCode.PASTE_ERR;
						clearCopyOrMove();
						break;
					}
					File targetFile = null;
					if(mFileInfo.getType() == ConstData.MediaType.FOLDER){
						File selectFile = new File(mFileInfo.getPath());
						//选中目录存在相同文件名
						if(isExistName(srcFileInfo.getName(), selectFile)){
							result = ConstData.FileOpErrorCode.PASTE_SAME_FILE;
							break;
						}
						//当前目录不可写
						if(!selectFile.canWrite()){
							result = ConstData.FileOpErrorCode.WRITE_ERR;
							break;
						}
						targetFile = new File(selectFile, srcFileInfo.getName());
					}else{
						//父目录不可写
						File parentFile = new File(mFileInfo.getParentPath());
						//选中目录存在相同文件名
						if(isExistName(srcFileInfo.getName(), parentFile)){
							result = ConstData.FileOpErrorCode.PASTE_SAME_FILE;
							break;
						}
						if(!parentFile.canWrite()){
							result = ConstData.FileOpErrorCode.WRITE_ERR;
							break;
						}
						targetFile = new File(parentFile + File.separator + srcFileInfo.getName());
					}
					//拷贝文件
					FileOpUtils.copyFile(new File(srcFileInfo.getPath()), targetFile, new ProgressUpdateListener() {
						@Override
						public void onUpdateProgress(int value) {
							publishProgress(value);
						}
					});
					List<String> targePaths = FileOpUtils.getAllFilePaths(targetFile);
					//需要更新媒体库
					if(targePaths != null && targePaths.size() > 0){
						MediaScannerConnection.scanFile(CommonValues.application, targePaths.toArray(new String[0]), null, null);
					}
					String deviceID = ConstData.devicePathIDs.get(mDevice.getLocalMountPath());
					if(deviceID != null){
						//更新本地数据库
						Intent broadIntent = new Intent(ConstData.BroadCastMsg.RESCAN_DEVICE);
						broadIntent.putExtra(ConstData.IntentKey.EXTRA_DEVICE_ID, deviceID);
						LocalBroadcastManager.getInstance(CommonValues.application).sendBroadcast(broadIntent);
					}
					
				}else if(!TextUtils.isEmpty(strSrcMove)){
					try{
						srcFileInfo = (FileInfo)JsonUtils.jsonToObject(FileInfo.class, new JSONObject(strSrcMove));
						Log.i(TAG, "paste srcFileInfo:" + srcFileInfo);
					}catch (Exception e){
						
					}
					if(srcFileInfo == null){
						result = ConstData.FileOpErrorCode.PASTE_ERR;
						clearCopyOrMove();
						break;
					}
					File srcFile = new File(srcFileInfo.getPath());
					File targetFile = null;
					//判断当前文件夹是否具有写权限
					if(mFileInfo.getType() == ConstData.MediaType.FOLDER){
						File selectFile = new File(mFileInfo.getPath());
						if(isExistName(srcFileInfo.getName(), selectFile)){
							result = ConstData.FileOpErrorCode.PASTE_SAME_FILE;
							break;
						}
						//当前目录不可写
						if(!selectFile.canWrite() || !srcFile.canWrite()){
							result = ConstData.FileOpErrorCode.WRITE_ERR;
							break;
						}
						targetFile = new File(selectFile, srcFileInfo.getName());
					}else{
						//父目录不可写
						File parentFile = new File(mFileInfo.getParentPath());
						if(isExistName(srcFileInfo.getName(), parentFile)){
							result = ConstData.FileOpErrorCode.PASTE_SAME_FILE;
							break;
						}
						if(!parentFile.canWrite() || !srcFile.canWrite()){
							result = ConstData.FileOpErrorCode.WRITE_ERR;
							break;
						}
						targetFile = new File(parentFile + File.separator + srcFileInfo.getName());
					}
					//拷贝文件
					FileOpUtils.copyFile(srcFile, targetFile, new ProgressUpdateListener() {
						@Override
						public void onUpdateProgress(int value) {
							publishProgress(value);
						}
					});
					//删除源文件
					List<String> delPaths = FileOpUtils.getAllFilePaths(new File(srcFileInfo.getPath()));
					FileOpUtils.deleteFile(srcFileInfo, mDevice);
					List<String> targePaths = FileOpUtils.getAllFilePaths(targetFile);
					//需要更新媒体库
					if(targePaths != null && targePaths.size() > 0){
						MediaScannerConnection.scanFile(CommonValues.application, targePaths.toArray(new String[0]), null, null);
					}
					if(delPaths != null && delPaths.size() > 0){
						MediaScannerConnection.scanFile(CommonValues.application, delPaths.toArray(new String[0]), null, null);
					}
					//更新本地数据库
					Intent srcIntent = new Intent(ConstData.BroadCastMsg.RESCAN_DEVICE);
					srcIntent.putExtra(ConstData.IntentKey.EXTRA_DEVICE_ID, srcFileInfo.getDeviceID());
					LocalBroadcastManager.getInstance(CommonValues.application).sendBroadcast(srcIntent);
					String deviceID = ConstData.devicePathIDs.get(mDevice.getLocalMountPath());
					if(deviceID != null){
						//更新本地数据库
						Intent targetIntent = new Intent(ConstData.BroadCastMsg.RESCAN_DEVICE);
						targetIntent.putExtra(ConstData.IntentKey.EXTRA_DEVICE_ID, deviceID);
						LocalBroadcastManager.getInstance(CommonValues.application).sendBroadcast(targetIntent);
					}
				}
				clearCopyOrMove();
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
	
	private void clearCopyOrMove(){
		StorageUtils.saveDataToSharedPreference(ConstData.SharedKey.COPY_FILE_PATH, "");
		StorageUtils.saveDataToSharedPreference(ConstData.SharedKey.MOVE_FILE_PATH, "");
	}
	
	/**
	 * 是否存在同名文件
	 * @param fileName
	 * @param dirFile
	 * @return
	 */
	private boolean isExistName(String fileName, File dirFile){
		File[] childFiles = dirFile.listFiles();
		if(childFiles != null && childFiles.length > 0){
			for(File childFile : childFiles){
				if(childFile.getName().equals(fileName)){
					return true;
				}
			}
		}
		return false;
	}
}
