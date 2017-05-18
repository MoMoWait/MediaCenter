/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Filename:    FlashStorageDevice.java  
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2011-9-5 上午10:43:21  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2011-9-5      xwf         1.0         create
*******************************************************************/   


package com.rockchip.mediacenter.mediaserver.model;

import com.rockchip.mediacenter.common.util.ReflectionUtils;
import com.rockchip.mediacenter.mediaserver.constants.EnvironmentConst;

import android.os.Build;
import android.os.Environment;

/**
 * FLASH INTENEL
 *
 */
public class FlashStorageDevice extends StorageDevice {

	public FlashStorageDevice(String name){
		super(name);
	}
	
	/** 
	 * <p>Title: isLiving</p> 
	 * <p>Description: </p> 
	 * @return 
	 * @see com.android.rockchip.mediashare.model.StorageDevice#isLiving() 
	 */
	@Override
	public boolean isLiving() {
		if(Build.VERSION.SDK_INT>=14){//Android 4.0
			return true;
		}else{
			String status = (String)ReflectionUtils.invokeStaticMethod("android.os.Environment", "getFlashStorageState");
	        if (Environment.MEDIA_MOUNTED.equals(status)) {
	            return true;
	        }
	        return false;
		}
	}

	/** 
	 * <p>Title: getPath</p> 
	 * <p>Description: </p> 
	 * @return 
	 * @see com.android.rockchip.mediashare.model.StorageDevice#getPath() 
	 */
	@Override
	public String getPath() {
		return EnvironmentConst.FLASH_DIR;
	}
}
