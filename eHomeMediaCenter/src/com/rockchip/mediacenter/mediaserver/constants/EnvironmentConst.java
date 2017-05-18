/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Filename:    EnvironmentConst.java  
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2011-9-3 下午04:41:57  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2011-9-3      xwf         1.0         create
*******************************************************************/   


package com.rockchip.mediacenter.mediaserver.constants;

import java.io.File;

import com.rockchip.mediacenter.common.util.ReflectionUtils;

import android.os.Build;
import android.os.Environment;

public class EnvironmentConst {

	public static final String TOP_DIR = "top_level_directory";
	public static String SDCARD_DIR = "";
	public static String FLASH_DIR = "";
	public static String USB_DIR = "";
	
	static {
		String clsName = "android.os.Environment";
		try{
			if(Build.VERSION.SDK_INT>=14){//Android 4.0
				File sdcardFile = (File)ReflectionUtils.invokeStaticMethod(clsName, "getSecondVolumeStorageDirectory");
				if(sdcardFile!=null) SDCARD_DIR = sdcardFile.getAbsolutePath();
				FLASH_DIR = Environment.getExternalStorageDirectory().getPath();
			}else{
				SDCARD_DIR = Environment.getExternalStorageDirectory().getPath();
				File flashFile = (File) ReflectionUtils.invokeStaticMethod(clsName, "getFlashStorageDirectory");
				FLASH_DIR = flashFile.getAbsolutePath();
			}
			File usbFile = (File) ReflectionUtils.invokeStaticMethod(clsName, "getHostStorageDirectory");
			if(usbFile == null) usbFile = new File("/mnt/udisk/usb");
			USB_DIR = usbFile.getAbsolutePath();
		}catch(Exception ex){
			//ignore
		}
	}
}
