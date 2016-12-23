package com.rockchips.mediacenter.modle.task;

import java.util.List;

import com.rockchips.mediacenter.bean.LocalMediaFolder;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.modle.db.LocalMediaFolderService;

import android.os.AsyncTask;
/**
 * 文件夹列表加载任务
 * @author GaoFei
 *
 */
public class FolderLoadTask extends AsyncTask<String, Integer, Integer> {
	
	public interface Callback{
		void onSuccess(List<LocalMediaFolder> mediaFolders);
		void onFailed();
	}
	
	private Callback mCallback;
	private List<LocalMediaFolder> mFolders;
	public FolderLoadTask(Callback callback){
		mCallback = callback;
	}
	
	@Override
	protected Integer doInBackground(String... params) {
		//设置最高优先级
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		int folderType = Integer.parseInt(params[0]);
		String deviceId = params[1];
		LocalMediaFolderService folderService = new LocalMediaFolderService();
		if(params.length == 2)
			mFolders = folderService.getFoldersByFolderTypeAndDeviceId(folderType, deviceId);
		else{
			int maxCount = Integer.parseInt(params[2]);
			mFolders = folderService.getFoldersByFolderTypeAndDeviceId(folderType, deviceId, maxCount);
		}
		return ConstData.TaskExecuteResult.SUCCESS;
	}
	
	@Override
	protected void onPostExecute(Integer result) {
		if(result == ConstData.TaskExecuteResult.SUCCESS){
			mCallback.onSuccess(mFolders);
		}else{
			mCallback.onFailed();
		}
	}
}
