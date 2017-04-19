package com.rockchips.mediacenter.modle.task;

import java.util.ArrayList;
import java.util.List;

import com.rockchips.mediacenter.bean.FileInfo;
import com.rockchips.mediacenter.utils.MediaFileUtils;

import android.os.AsyncTask;

/**
 * @author GaoFei
 * 文件搜索异步块
 */
public class FileSearchTask extends AsyncTask<Object, Integer, List<FileInfo>>{
	public interface Callback{
		void OnFinished(List<FileInfo> resultFileInfos);
	}
	private List<FileInfo> mAllFileInfos;
	private Callback mCallback;
	public FileSearchTask(List<FileInfo> allFileInfos, Callback callback){
		mAllFileInfos = allFileInfos;
		mCallback = callback;
	}
	
	@Override
	protected List<FileInfo> doInBackground(Object... params) {
		List<FileInfo> resultInfos = new ArrayList<>();
		String searchText = (String)params[0];
		for(FileInfo itemFileInfo : mAllFileInfos){
			String fileName = itemFileInfo.getName();
			List<String> pinYinHeads = MediaFileUtils.getHeadPinyins(fileName);
			if(fileName.toLowerCase().contains(searchText) 
					|| MediaFileUtils.getFullPinYin(fileName).toLowerCase().contains(searchText)){
				resultInfos.add(itemFileInfo);
			}else if(pinYinHeads != null && pinYinHeads.size() > 0){
				for(String pinYinHead : pinYinHeads){
					if(pinYinHead.toLowerCase().contains(searchText)){
						resultInfos.add(itemFileInfo);
						break;
					}
				}
			}
		}
		return resultInfos;
	}
	
	@Override
	protected void onPostExecute(List<FileInfo> resultFileInfos) {
		mCallback.OnFinished(resultFileInfos);
	}
}
