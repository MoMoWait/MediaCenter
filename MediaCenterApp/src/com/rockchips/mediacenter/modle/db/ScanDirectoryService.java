package com.rockchips.mediacenter.modle.db;
import java.util.ArrayList;
import java.util.List;

import org.xutils.db.sqlite.WhereBuilder;

import android.util.Log;

import com.rockchips.mediacenter.application.MediaCenterApplication;
import com.rockchips.mediacenter.bean.ScanDirectory;

public class ScanDirectoryService extends AppBeanService<ScanDirectory>{
	public static final String TAG = ScanDirectoryService.class.getSimpleName();
	@Override
	public boolean isExist(ScanDirectory object) {
		return false;
	}
	
	public List<ScanDirectory> getDirectoriesByDeviceId(String deviceId, int maxLimit){
		List<ScanDirectory> directories = new ArrayList<ScanDirectory>();
		try{
			directories =  MediaCenterApplication.mDBManager.selector(ScanDirectory.class).
			where("deviceId", "=", deviceId).limit(maxLimit).findAll();
		}catch (Exception e){
			Log.i(TAG, "getDirectoriesByDeviceId exception : " + e);
		}
		Log.i(TAG, "getDirectoriesByDeviceId->directories:" + directories);
		return directories;
	}
	
	/**
	 * 删除所有对应的数据
	 * @param directories
	 */
	public void deleteAll(List<ScanDirectory> directories){
		try{
			MediaCenterApplication.mDBManager.delete(directories);
		}catch (Exception e){
			Log.i(TAG, "deleteAll exception : " + e);
		}
	}
	
	/**
	 * 根据deviceID删除缓存目录
	 * @param deviceId
	 */
	public void deleteDirectoriesByDeviceId(String deviceId){
		try{
			MediaCenterApplication.mDBManager.delete(ScanDirectory.class, WhereBuilder.b("deviceId", "=", deviceId));
		}catch(Exception e){
			Log.i(TAG, "deleteDirectoriesByDeviceId exception : " + e);
		}
	}
}
