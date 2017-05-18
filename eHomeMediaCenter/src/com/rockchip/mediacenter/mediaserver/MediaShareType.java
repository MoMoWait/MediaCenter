/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Filename:    MediaShareType.java  
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2011-9-15 下午03:10:43  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2011-9-15      xwf         1.0         create
*******************************************************************/   


package com.rockchip.mediacenter.mediaserver;

public enum MediaShareType {
	
	//文件夹共享方式
	FOLDER_SHARE(1, "文件夹共享方式"),
	
	//媒体文件归类共享方式
	MEDIA_SHARE(2, "媒体文件归类共享方式")
	;
	
	private int id;
	private String name;
	
	MediaShareType(int id, String name){
		this.id = id;
		this.name = name;
	}
	
	public static MediaShareType getById(String id){
		return getById(Integer.parseInt(id));
	}
	
	public static MediaShareType getById(int id){
		MediaShareType[] types = MediaShareType.values();
		for(MediaShareType type : types){
			if(type.getId() == id){
				return type;
			}
		}
		return null;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	
}
