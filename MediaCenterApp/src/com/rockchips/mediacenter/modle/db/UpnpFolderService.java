package com.rockchips.mediacenter.modle.db;

import java.util.List;

import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;
import android.util.Log;
import com.rockchips.mediacenter.application.MediaCenterApplication;
import com.rockchips.mediacenter.bean.UpnpFolder;
import com.rockchips.mediacenter.data.ConstData;

/**
 * @author GaoFei
 * Upnp目录服务
 */
public class UpnpFolderService extends AppBeanService<UpnpFolder> {
	
	private String TAG = UpnpFolderService.class.getSimpleName();
	
	@Override
	public boolean isExist(UpnpFolder object) {
		return false;
	}
	
	public void deleteFoldersByDeviceId(String deviceId){
		try {
			MediaCenterApplication.mDBManager.delete(UpnpFolder.class, 
					WhereBuilder.b("deviceID", "=", deviceId));
		} catch (DbException e) {
			e.printStackTrace();
			Log.i(TAG, "deleteFoldersByDeviceId->exception:" + e);
		}
		
	}
	
	/**
	 * 根据设备ID和目录类型获取目录列表
	 * @param deviceID
	 * @param type
	 * @return
	 */
	public List<UpnpFolder> getUpnpFoldersByDeviceIdAndType(String deviceID, int type){
		List<UpnpFolder> folders = null;
		if(type == ConstData.MediaType.FOLDER){
			try {
				folders = MediaCenterApplication.mDBManager.selector(UpnpFolder.class).
				where("deviceID", "=", deviceID).and("fileCount", ">", 0).findAll();
			} catch (DbException e) {
				Log.i(TAG, "getUpnpFoldersByDeviceIdAndType->exception:" + e);
				e.printStackTrace();
			}
		}else if(type == ConstData.MediaType.IMAGEFOLDER){
			try {
				folders = MediaCenterApplication.mDBManager.selector(UpnpFolder.class).
				where("deviceID", "=", deviceID).and("imageCount", ">", 0).findAll();
			} catch (DbException e) {
				Log.i(TAG, "getUpnpFoldersByDeviceIdAndType->exception:" + e);
				e.printStackTrace();
			}
		}else if(type == ConstData.MediaType.AUDIOFOLDER){
			try {
				folders = MediaCenterApplication.mDBManager.selector(UpnpFolder.class).
				where("deviceID", "=", deviceID).and("audioCount", ">", 0).findAll();
			} catch (DbException e) {
				Log.i(TAG, "getUpnpFoldersByDeviceIdAndType->exception:" + e);
				e.printStackTrace();
			}
		}else if(type == ConstData.MediaType.VIDEOFOLDER){
			try {
				folders = MediaCenterApplication.mDBManager.selector(UpnpFolder.class).
				where("deviceID", "=", deviceID).and("videoCount", ">", 0).findAll();
			} catch (DbException e) {
				Log.i(TAG, "getUpnpFoldersByDeviceIdAndType->exception:" + e);
				e.printStackTrace();
			}
		}
		
		return folders;
	}

}
