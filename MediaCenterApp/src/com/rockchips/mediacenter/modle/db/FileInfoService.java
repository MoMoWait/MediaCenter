package com.rockchips.mediacenter.modle.db;

import java.util.List;

import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;

import com.rockchips.mediacenter.application.MediaCenterApplication;
import com.rockchips.mediacenter.bean.AllFileInfo;
import com.rockchips.mediacenter.bean.Device;
import com.rockchips.mediacenter.bean.FileInfo;
import com.rockchips.mediacenter.data.ConstData;

/**
 * @author GaoFei
 * 文件描述信息操作
 */
public class FileInfoService extends AppBeanService<FileInfo> {

	@Override
	public boolean isExist(FileInfo object) {
		return false;
	}

	/**
	 * 根据设备ID删除文件信息
	 * @param deviceID
	 */
	public void deleteFileInfosByDeviceID(String deviceID){
		try {
			MediaCenterApplication.mDBManager.delete(Device.class, WhereBuilder.b("deviceID", "=", deviceID));
		} catch (DbException e) {
			e.printStackTrace();
		}
	}
	
	public List<FileInfo> getAllFolders(String deviceID, int mediaType){
		List<FileInfo> fileInfos = null;
		if(mediaType == ConstData.MediaType.AUDIOFOLDER){
			try {
				fileInfos = MediaCenterApplication.mDBManager.selector(FileInfo.class).where("musicCount", ">", 0).
				and("deviceID", "=", deviceID).orderBy("name", false).findAll();
			} catch (DbException e) {
				e.printStackTrace();
			}
		}else if(mediaType == ConstData.MediaType.VIDEOFOLDER){
			try {
				fileInfos = MediaCenterApplication.mDBManager.selector(FileInfo.class).where("imageCount", ">", 0).
				and("deviceID", "=", deviceID).orderBy("name", false).findAll();
			} catch (DbException e) {
				e.printStackTrace();
			}
		}else if(mediaType == ConstData.MediaType.IMAGEFOLDER){
			try {
				fileInfos = MediaCenterApplication.mDBManager.selector(FileInfo.class).where("imageCount", ">", 0).
				and("deviceID", "=", deviceID).orderBy("name", false).findAll();
			} catch (DbException e) {
				e.printStackTrace();
			}
		}
		return fileInfos;
	}
	
	public List<FileInfo> getFileInfos(String deviceID, String parentPath, int mediaType){
		List<FileInfo> fileInfos = null;
		try {
			fileInfos = MediaCenterApplication.mDBManager.selector(FileInfo.class).where("type", "=", mediaType).
			and("deviceID", "=", deviceID).and("parentPath", "=", parentPath).orderBy("name", false).findAll();
		} catch (DbException e) {
			e.printStackTrace();
		}
		return fileInfos;
	
	}
	
}
