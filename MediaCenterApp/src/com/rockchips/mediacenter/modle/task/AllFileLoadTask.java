package com.rockchips.mediacenter.modle.task;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.rockchips.mediacenter.bean.AllFileInfo;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.util.MediaFileUtils;

import android.os.AsyncTask;

/**
 * @author GaoFei
 * 所有文件列表加载器
 */
public class AllFileLoadTask extends AsyncTask<String, Integer, Integer> {
	
	public interface CallBack{
		void onGetFiles(List<AllFileInfo> fileInfos);
	}
	
	private CallBack mCallBack;
	private List<AllFileInfo> mAllFileInfos = new ArrayList<AllFileInfo>();
	public AllFileLoadTask(CallBack callBack){
		mCallBack = callBack;
	}
	
	
	@Override
	protected Integer doInBackground(String... params) {
		String dirPath = params[0];
		File dirFile = new File(dirPath);
		AllFileInfo allFileInfo;
		if(dirFile.exists()){
			File[] files = dirFile.listFiles();
			if(files != null && files.length > 0){
				for(File itemFile : files){
					allFileInfo = new AllFileInfo();
					allFileInfo.setFile(itemFile);
					if(itemFile.isDirectory()){
						allFileInfo.setType(ConstData.MediaType.FOLDER);
					}else{
						allFileInfo.setType(MediaFileUtils.getMediaTypeFromFile(itemFile));
					}
					mAllFileInfos.add(allFileInfo);
				}
			}
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(Integer result) {
		mCallBack.onGetFiles(mAllFileInfos);
	}
	
}
