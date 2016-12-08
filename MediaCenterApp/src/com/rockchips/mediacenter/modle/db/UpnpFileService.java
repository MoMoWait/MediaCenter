package com.rockchips.mediacenter.modle.db;

import java.util.ArrayList;
import java.util.List;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;
import android.util.Log;
import com.rockchips.mediacenter.application.MediaCenterApplication;
import com.rockchips.mediacenter.bean.UpnpFile;

/**
 * @author GaoFei
 * Upnp文件服务
 */
public class UpnpFileService extends AppBeanService<UpnpFile> {
	private String TAG = UpnpFolderService.class.getSimpleName();
	@Override
	public boolean isExist(UpnpFile object) {
		return false;
	}

	/**
	 * 根据设备ID删除对应文件 
	 * @param deviceID
	 */
	public void deleteFilesByDeviceId(String deviceID){
		try {
			MediaCenterApplication.mDBManager.delete(UpnpFile.class, 
					WhereBuilder.b("deviceID", "=", deviceID));
		} catch (DbException e) {
			e.printStackTrace();
			Log.i(TAG, "deleteFilesByDeviceId->e:" + e);
		}
	}
	
	
	/**
	 * 根据设备ID和父目录ID获取文件列表
	 * @param deviceId
	 * @param parentID
	 * @return
	 */
	public List<UpnpFile> getFilesByDeviceIdAndParentId(String deviceId, String parentID, int fileType){
		List<UpnpFile> upnpFiles = new ArrayList<UpnpFile>();
		if(fileType == -1){
			try {
				upnpFiles = MediaCenterApplication.mDBManager.selector(UpnpFile.class).
				where("deviceID", "=", deviceId).and("parentId", "=", parentID).findAll();
			} catch (DbException e) {
				e.printStackTrace();
				Log.i(TAG, "getFilesByDeviceIdAndParentId->exception:" + e);
			}
		}else{
			try {
				upnpFiles = MediaCenterApplication.mDBManager.selector(UpnpFile.class).
						where("deviceID", "=", deviceId).and("parentId", "=", parentID).and("type", "=", fileType).findAll();
			} catch (DbException e) {
				e.printStackTrace();
				Log.i(TAG, "getFilesByDeviceIdAndParentId->exception:" + e);
			}
		}
		return upnpFiles;
	}
}
