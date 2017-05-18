/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Filename:    SDCardStorageDevice.java  
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2011-9-5 上午10:38:39  
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

public class SDCardStorageDevice extends StorageDevice {
	
	public SDCardStorageDevice(String name){
		super(name);
	}
	
	/** 
	 * <p>Title: isLiving</p> 
	 * <p>Description: 设备是否在线</p> 
	 * @return 
	 * @see com.android.rockchip.mediashare.model.StorageDevice#isLiving() 
	 */
	@Override
	public boolean isLiving() {
		//String status = SystemProperties.get(); 
		String status = null;
		if(Build.VERSION.SDK_INT>=14){//Android 4.0
			status = (String)ReflectionUtils.invokeStaticMethod("android.os.Environment", "getSecondVolumeStorageState");
		}else{
			status = (String)ReflectionUtils.invokeStaticMethod("android.os.SystemProperties", "get", "EXTERNAL_STORAGE_STATE", Environment.MEDIA_REMOVED);
		}
		if (Environment.MEDIA_MOUNTED.equals(status)) {
    		return true;
    	}
    	return false;
	}

	/** 
	 * <p>Title: getPath</p> 
	 * <p>Description: </p> 
	 * @return 
	 * @see com.android.rockchip.mediashare.model.StorageDevice#getPath() 
	 */
	@Override
	public String getPath() {
		return EnvironmentConst.SDCARD_DIR;
	}
}
