package com.rockchips.mediacenter.modle.db;

import java.util.ArrayList;
import java.util.List;
import org.xutils.db.sqlite.WhereBuilder;

import android.R.integer;

import com.rockchips.mediacenter.application.MediaCenterApplication;
import com.rockchips.mediacenter.bean.LocalMediaFolder;

/**
 * @author GaoFei
 *
 */
public class LocalMediaFolderService extends AppBeanService<LocalMediaFolder> {

	@Override
	public boolean isExist(LocalMediaFolder object) {
		return false;
	}
	
	public void deleteFoldersByDeviceId(String deviceId){
		try{
			MediaCenterApplication.mDBManager.delete(LocalMediaFolder.class, WhereBuilder.b("deviceID", "=", deviceId));
		}catch (Exception e){
			
		}
	}
	
	/**
	 * 通过物理设备ID删除文件夹
	 * @param phySicId
	 */
	public void deleteFoldersByPhysicId(String phySicId){
		try{
			MediaCenterApplication.mDBManager.delete(LocalMediaFolder.class, WhereBuilder.b("physic_dev_id", "=", phySicId));
		}catch (Exception e){
			
		}
	}
	/**
	 * 根据文件夹类型获取文件夹列表
	 * @param floderType
	 * @return
	 */
	public List<LocalMediaFolder> getFoldersByFolderTypeAndDeviceId(int floderType, String deviceId){
		List<LocalMediaFolder> allMediaFolders = new ArrayList<LocalMediaFolder>();
		try {
			 allMediaFolders = MediaCenterApplication.mDBManager.selector(LocalMediaFolder.class)
					 .where("folderType", "=", floderType).and("deviceID", "=", deviceId).findAll();
		} catch (Exception e) {
			//no handle
		}
		return allMediaFolders;
	}
	
	
	/**
	 * 根据文件夹类型获取文件夹列表
	 * @param floderType
	 * @return
	 */
	public List<LocalMediaFolder> getFoldersByFolderTypeAndDeviceId(int floderType, String deviceId, int maxCount){
		List<LocalMediaFolder> allMediaFolders = new ArrayList<LocalMediaFolder>();
		try {
			 allMediaFolders = MediaCenterApplication.mDBManager.selector(LocalMediaFolder.class)
					 .where("folderType", "=", floderType).and("deviceID", "=", deviceId).limit(maxCount).findAll();
		} catch (Exception e) {
			//no handle
		}
		return allMediaFolders;
	}
	
}
