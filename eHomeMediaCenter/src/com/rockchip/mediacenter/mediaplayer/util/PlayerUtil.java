/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Filename:    PlayerUtil.java  
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2012-6-16 下午04:56:15  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2012-6-16      fxw         1.0         create
*******************************************************************/   


package com.rockchip.mediacenter.mediaplayer.util;

import java.io.File;

import com.rockchip.mediacenter.common.util.StringUtils;
import com.rockchip.mediacenter.core.constants.MediaPlayConsts;
import com.rockchip.mediacenter.core.dlna.enumeration.MediaClassType;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 *
 * @author fxw
 * @since 1.0
 */
public class PlayerUtil {

	public static boolean startPlayer(Context context, String title, String url, String mimeType){
		if(StringUtils.isEmptyObj(url)){
			return false;
		}
		Uri uri = null;
		if(isHttpURL(url)){
			uri = Uri.parse(url);
		}else{
			uri = Uri.fromFile(new File(url));
		}
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		MediaClassType itemClass = MediaClassType.getMediaClassTypeByMime(mimeType);
		if(MediaClassType.IMAGE==itemClass){
			intent.setClass(context, com.rockchip.mediacenter.plugins.pictureplay.PictureViewer.class);
		}else if(MediaClassType.AUDIO==itemClass){
			intent.setClass(context, com.rockchip.mediacenter.plugins.musicplay.MusicPlayer.class);
		}else if(MediaClassType.VIDEO==itemClass){
			intent.setClass(context, com.rockchip.mediacenter.plugins.videoplay.VideoPlayer.class);
		}
		intent.putExtra(MediaPlayConsts.KEY_MEDIA_TITLE, title);
		intent.setData(uri);
		try{
			context.startActivity(intent);
			return true;
		}catch(ActivityNotFoundException anfe){
			return false;
		}
	}
	
	public static boolean isHttpURL(String url){
		String httpURL = "http";
		if(StringUtils.isEmptyObj(url)){
			return false;
		}
		if(url.length()>httpURL.length()
				&&httpURL.equalsIgnoreCase(url.substring(0, httpURL.length()))){
			return true;
		}
		return false;
	}
	
}
