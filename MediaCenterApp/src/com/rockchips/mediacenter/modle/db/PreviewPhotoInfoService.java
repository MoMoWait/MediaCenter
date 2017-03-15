/**
 * 
 */
package com.rockchips.mediacenter.modle.db;

import java.util.List;

import org.xutils.db.sqlite.WhereBuilder;
import com.rockchips.mediacenter.application.MediaCenterApplication;
import com.rockchips.mediacenter.bean.PreviewPhotoInfo;

/**
 * @author GaoFei
 * 预览缓存图操作
 */
public class PreviewPhotoInfoService extends AppBeanService<PreviewPhotoInfo>{

	@Override
	public boolean isExist(PreviewPhotoInfo object) {
		return false;
	}
	
	public PreviewPhotoInfo getPreviewPhotoInfo(String deviceID, String originPath){
		PreviewPhotoInfo photoInfo = null;
		try {
			photoInfo = MediaCenterApplication.mDBManager.selector(PreviewPhotoInfo.class).
			where(WhereBuilder.b("deviceID", "=", deviceID).and("originPath", "=", originPath))
			.findFirst();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return photoInfo;
	}

	/**
	 * 根据设备ID预览图信息列表
	 * @param deviceID
	 * @return
	 */
	public List<PreviewPhotoInfo> getPreviewPhotoInfosByDeviceID(String deviceID){
		List<PreviewPhotoInfo> previewPhotoInfos = null;
		try {
			previewPhotoInfos = MediaCenterApplication.mDBManager.selector(PreviewPhotoInfo.class).where(WhereBuilder.b("deviceID", "=", deviceID)).findAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return previewPhotoInfos;
	}
	
	public void deletePreviewPhotoByDeviceID(String deviceID){
		try {
			MediaCenterApplication.mDBManager.delete(PreviewPhotoInfo.class, WhereBuilder.b("deviceID", "=", deviceID));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 删除所有数据
	 */
	public void deleteAll(){
		try {
			MediaCenterApplication.mDBManager.delete(PreviewPhotoInfo.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
