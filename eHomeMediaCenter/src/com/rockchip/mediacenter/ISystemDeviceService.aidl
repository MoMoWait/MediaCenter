/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Filename:    ISystemDeviceService  
* Description:   
* @author:     fxw@rock-chips.com 
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2011-11-22      xwf         1.0         create
*******************************************************************/   


package com.rockchip.mediacenter;

interface ISystemDeviceService {
	
	//Digital Media Server
	boolean startMediaServerWithSavedConfig();
	boolean stopMediaServer();
	boolean isServerRunning();
	List<String> queryShareDirectory();
	boolean deleteShareDirectory(String path);
	boolean addShareDirectory(String path);
	void updateContentSharePolicy();
	
	//Digital Media Renderer
	boolean startMediaRenderer();
	boolean stopMediaRenderer();
	boolean isRendererRunning();
	void switchAutoRunRenderer(boolean auto);
	void updateDeviceConfiguration();
	void shutdown();
	
}
