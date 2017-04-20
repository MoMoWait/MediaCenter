package com.rockchips.mediacenter.modle.task;
import java.util.List;
import com.rockchips.mediacenter.bean.BackMusicPhotoInfo;
import com.rockchips.mediacenter.modle.db.BackMusicPhotoInfoService;

import android.os.AsyncTask;

/**
 * @author GaoFei
 * 音乐 播放背景图任务加载器
 */
public class BackPhotoLoadTask extends AsyncTask<Object, Integer, List<BackMusicPhotoInfo>> {
	
	public interface Callback{
		void onFinished(List<BackMusicPhotoInfo> musicPhotoInfos);
	}
	
	private Callback mCallback;
	
	public BackPhotoLoadTask(Callback callback){
		mCallback = callback;
	}
	
	@Override
	protected List<BackMusicPhotoInfo> doInBackground(Object... params) {
		BackMusicPhotoInfoService backInfoService = new BackMusicPhotoInfoService();
		return backInfoService.getAll(BackMusicPhotoInfo.class);
	}
	
	protected void onPostExecute(List<BackMusicPhotoInfo> result) {
		mCallback.onFinished(result);
	}
}
