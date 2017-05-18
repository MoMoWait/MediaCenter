/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Filename:    IMediaCenterService  
* Description:   
* @author:     fxw@rock-chips.com 
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2011-11-22      xwf         1.0         create
*******************************************************************/   


package com.rockchip.mediacenter.dlna;

import com.rockchip.mediacenter.dlna.model.DeviceItem;
import com.rockchip.mediacenter.dlna.model.TransportInfo;
import com.rockchip.mediacenter.dlna.model.PositionInfo;


interface IMediaCenterService {
	
	void  search();
	void  research();
	List<DeviceItem> getMediaServerDevice(boolean containLocalDevice);
	List<DeviceItem> getMediaRendererDevice();
	
	//Digital Media Controller
	void    setTargetDevice(in DeviceItem deviceItem);
	boolean setAVTransportURI(String path);
	boolean setMetaDataAndURI(String path, String title, String mimeType, long size, int duration);
	boolean play();
	boolean pause();
	boolean stop();
	boolean exit();
	boolean asyncExit();
	boolean seek(String seekTarget);
	int     getVolume();
	int     getMaxVolume();
	boolean setVolume(int volume);
	TransportInfo getTransportInfo();
	PositionInfo  getPositionInfo();
	
}
