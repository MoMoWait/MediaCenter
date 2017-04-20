/**
 * 
 */
package com.rockchips.mediacenter.modle.db;
import com.rockchips.mediacenter.application.MediaCenterApplication;
import com.rockchips.mediacenter.bean.BackMusicPhotoInfo;

/**
 * @author GaoFei
 * 音乐背景图片服务
 */
public class BackMusicPhotoInfoService extends AppBeanService<BackMusicPhotoInfo>{
	@Override
	public boolean isExist(BackMusicPhotoInfo object) {
		return false;
	}
	
	public void deleteAll(){
		try{
			MediaCenterApplication.mDBManager.delete(BackMusicPhotoInfo.class);
		}catch (Exception e){
			//no handle
		}
		
	}

}
