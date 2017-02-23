package com.rockchips.mediacenter.modle.task;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import android.media.iso.ISOManager;
import com.rockchips.mediacenter.bean.AllFileInfo;
import com.rockchips.mediacenter.bean.LocalMediaFile;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.util.MediaFileUtils;

import android.os.AsyncTask;
import android.util.Log;

/**
 * @author GaoFei
 * 所有文件列表加载器
 */
public class AllFileLoadTask extends AsyncTask<String, Integer, Integer> {
	private static final String TAG = "AllFileLoadTask";
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
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
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
						//如果是蓝光文件夹
						if(ISOManager.isBDDirectory(itemFile.getPath())){
							allFileInfo.setType(ConstData.MediaType.VIDEO);
						}else{
							allFileInfo.setType(ConstData.MediaType.FOLDER);
						}
					}else{
						allFileInfo.setType(MediaFileUtils.getMediaTypeFromFile(itemFile));
					}
					mAllFileInfos.add(allFileInfo);
				}
			}
		}
		if(mAllFileInfos.size() > 1){
			Collections.sort(mAllFileInfos, new Comparator<AllFileInfo>() {

				@Override
				public int compare(AllFileInfo lhs, AllFileInfo rhs) {
					if(lhs.getFile().isDirectory() && rhs.getFile().isFile())
						return -1;
					else if(lhs.getFile().isDirectory() && rhs.getFile().isDirectory()
					        || lhs.getFile().isFile() && rhs.getFile().isFile())
					    return lhs.getFile().getPath().compareTo(rhs.getFile().getPath());
					return 0;
				}
			});
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(Integer result) {
		mCallBack.onGetFiles(mAllFileInfos);
	}
	
	
	/**
	 * 同步调用task
	 * @param params
	 */
	public void run(String... params){
		doInBackground(params);
		mCallBack.onGetFiles(mAllFileInfos);
	}
}
