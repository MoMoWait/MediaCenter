package com.rockchips.mediacenter.modle.task;

import java.util.List;

import com.rockchips.mediacenter.bean.UpnpFolder;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.modle.db.UpnpFolderService;

import android.os.AsyncTask;

/**
 * @author GaoFei
 * Upnp设备目录加载器
 */
public class UpnpFolderLoadTask extends AsyncTask<String, Integer, Integer>{
	private String TAG = UpnpFolderLoadTask.class.getSimpleName();
	public interface Callback{
		void onSuccess(List<UpnpFolder> mediaFolders);
		void onFailed();
	}
	
	private Callback mCallback;
	private List<UpnpFolder> mFolders;
	
	public UpnpFolderLoadTask(Callback callback){
		mCallback = callback;
	}
	
	@Override
	protected Integer doInBackground(String... params) {
		String deviceID = params[0];
		int mediaType = Integer.parseInt(params[1]);
		UpnpFolderService upnpFolderService = new UpnpFolderService();
		mFolders = upnpFolderService.getUpnpFoldersByDeviceIdAndType(deviceID, mediaType);
		return ConstData.TaskExecuteResult.SUCCESS;
	}

	@Override
	protected void onPostExecute(Integer result) {
		if(result == ConstData.TaskExecuteResult.SUCCESS)
			mCallback.onSuccess(mFolders);
	}
}
