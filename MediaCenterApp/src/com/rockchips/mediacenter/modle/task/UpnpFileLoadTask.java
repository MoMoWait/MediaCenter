package com.rockchips.mediacenter.modle.task;

import java.util.List;
import com.rockchips.mediacenter.bean.UpnpFile;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.modle.db.UpnpFileService;

import android.os.AsyncTask;

/**
 * @author GaoFei
 * UPnp文件获取任务
 */
public class UpnpFileLoadTask extends AsyncTask<String, Integer, Integer> {

	public interface Callback{
		void onSuccess(List<UpnpFile> mediaFiles);
		void onFailed();
	}
	
	private Callback mCallback;
	private List<UpnpFile> mFiles;
	public UpnpFileLoadTask(Callback callback){
		mCallback = callback;
	}
	
	@Override
	protected Integer doInBackground(String... params) {
		String deviceId = params[0];
		String parentId = params[1];
		int fileType = Integer.parseInt(params[2]);
		UpnpFileService upnpFileService = new UpnpFileService();
		mFiles = upnpFileService.getFilesByDeviceIdAndParentId(deviceId, parentId, fileType);
		//LocalMediaFileService fileService = new LocalMediaFileService();
		//mFiles = fileService.getFilesByParentPath(parentPath, mediaType);
		return ConstData.TaskExecuteResult.SUCCESS;
	}
	
	@Override
	protected void onPostExecute(Integer result) {
		if(result == ConstData.TaskExecuteResult.SUCCESS){
			mCallback.onSuccess(mFiles);
		}else{
			mCallback.onFailed();
		}
	}

}
