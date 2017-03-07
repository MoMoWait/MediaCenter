/**
 * 
 */
package com.rockchips.mediacenter.modle.db;

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

}
