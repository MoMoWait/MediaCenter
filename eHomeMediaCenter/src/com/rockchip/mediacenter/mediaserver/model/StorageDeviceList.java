/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Filename:    StorageDeviceList.java  
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2011-9-5 上午10:58:53  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2011-9-5      xwf         1.0         create
*******************************************************************/   


package com.rockchip.mediacenter.mediaserver.model;

import java.util.Iterator;
import java.util.Vector;

public class StorageDeviceList extends Vector<StorageDevice> {
	private static final long serialVersionUID = 1L;
	
	public StorageDevice getStorageDevice(int n){
		return get(n);
	}

	/**
	 * 由路径映射设备名称
	 * @param path 设备路径
	 * @return
	 */
	public String mappingDeviceName(String path){
		Iterator<StorageDevice> it = iterator();
		while(it.hasNext()){
			StorageDevice device = it.next();
			if(device.getPath().equals(path)){
				return device.getName();
			}
		}
		return null;
	}
	
	/**
	 * 由设备名称映射设备路径
	 * @param name
	 * @return
	 */
	public String mappingDevicePath(String name){
		Iterator<StorageDevice> it = iterator();
		while(it.hasNext()){
			StorageDevice device = it.next();
			if(device.getName().equals(name)){
				return device.getName();
			}
		}
		return null;
	}
	
	/**
	 * 是否包含指定路径的设备
	 * @param path
	 * @return
	 */
	public boolean isIncludePath(String path){
		Iterator<StorageDevice> it = iterator();
		while(it.hasNext()){
			StorageDevice device = it.next();
			if(device.getPath().equals(path)){
				return true;
			}
		}
		return false;
	}
	
}
