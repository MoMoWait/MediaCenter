/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Filename:    USBStorageDevice.java  
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2011-9-22 上午11:11:53  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2011-9-22      xwf         1.0         create
*******************************************************************/   


package com.rockchip.mediacenter.mediaserver.model;

import com.rockchip.mediacenter.common.util.ReflectionUtils;
import com.rockchip.mediacenter.mediaserver.constants.EnvironmentConst;

import android.os.Environment;

public class USBStorageDevice extends StorageDevice {

	public USBStorageDevice(String name) {
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
		return (isMountUSB() || isMountUSB0() || isMountUSB1() || isMountUSB2() || isMountUSB3() || isMountUSB4() || isMountUSB5()
					);
	}
	
	private boolean isMountUSB(){
		String status = (String)ReflectionUtils.invokeStaticMethod("android.os.Environment", "getHostStorageState");
		//String status = Environment.getHostStorageState();
		if (Environment.MEDIA_MOUNTED.equals(status)) {
    		return true;
    	}
    	return false;
    }
	private boolean isMountUSB0(){
		String status = (String)ReflectionUtils.invokeStaticMethod("android.os.Environment", "getHostStorage_Extern_0_State");
    	return (Environment.MEDIA_MOUNTED.equals(status));
    }
	private boolean isMountUSB1(){
		String status = (String)ReflectionUtils.invokeStaticMethod("android.os.Environment", "getHostStorage_Extern_1_State");
    	return (Environment.MEDIA_MOUNTED.equals(status));
    }
	private boolean isMountUSB2(){
		String status = (String)ReflectionUtils.invokeStaticMethod("android.os.Environment", "getHostStorage_Extern_2_State");
    	return (Environment.MEDIA_MOUNTED.equals(status));
    }
	private boolean isMountUSB3(){
		String status = (String)ReflectionUtils.invokeStaticMethod("android.os.Environment", "getHostStorage_Extern_3_State");
    	return (Environment.MEDIA_MOUNTED.equals(status));
    }
	private boolean isMountUSB4(){
		String status = (String)ReflectionUtils.invokeStaticMethod("android.os.Environment", "getHostStorage_Extern_4_State");
    	return (Environment.MEDIA_MOUNTED.equals(status));
    }
	private boolean isMountUSB5(){
		String status = (String)ReflectionUtils.invokeStaticMethod("android.os.Environment", "getHostStorage_Extern_5_State");
    	return (Environment.MEDIA_MOUNTED.equals(status));
    }

	/** 
	 * <p>Title: getPath</p> 
	 * <p>Description: </p> 
	 * @return 
	 * @see com.android.rockchip.mediashare.model.StorageDevice#getPath() 
	 */
	@Override
	public String getPath() {
		return EnvironmentConst.USB_DIR;
	}

}
