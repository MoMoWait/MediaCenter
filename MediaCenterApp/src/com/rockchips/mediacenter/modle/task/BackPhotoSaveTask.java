package com.rockchips.mediacenter.modle.task;

import java.util.ArrayList;
import java.util.List;

import com.rockchips.mediacenter.bean.BackMusicPhotoInfo;
import com.rockchips.mediacenter.bean.FileInfo;
import com.rockchips.mediacenter.modle.db.BackMusicPhotoInfoService;

import android.os.AsyncTask;

/**
 * @author GaoFei
 * 背景图保存任务
 */
public class BackPhotoSaveTask extends AsyncTask<Void, Void, List<BackMusicPhotoInfo>>{
	public interface Callback{
		void onFinished(List<BackMusicPhotoInfo> photoInfos);
	}
	private List<FileInfo> mFileInfos;
	private Callback mCallback;
	public BackPhotoSaveTask(List<FileInfo> fileInfos, Callback callback){
		mFileInfos = fileInfos;
		mCallback = callback;
	}
	
	@Override
	protected List<BackMusicPhotoInfo> doInBackground(Void... params) {
		List<BackMusicPhotoInfo> backInfos = new ArrayList<>();
		for(FileInfo itemInfo : mFileInfos){
			BackMusicPhotoInfo photoInfo = new BackMusicPhotoInfo();
			photoInfo.setPath(itemInfo.getPath());
			backInfos.add(photoInfo);
			
		}
		BackMusicPhotoInfoService infoService = new BackMusicPhotoInfoService();
		infoService.deleteAll();
		infoService.saveAll(backInfos);
		return backInfos;
	}
	
	@Override
	protected void onPostExecute(List<BackMusicPhotoInfo> result) {
		mCallback.onFinished(result);
	}
}
