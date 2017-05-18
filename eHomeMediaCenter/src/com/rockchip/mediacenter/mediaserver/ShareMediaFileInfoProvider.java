/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Filename:    ShareMediaProvider.java  
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2011-9-15 下午03:45:10  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2011-9-15      xwf         1.0         create
*******************************************************************/   


package com.rockchip.mediacenter.mediaserver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.rockchip.mediacenter.R;
import com.rockchip.mediacenter.core.dlna.service.contentdirectory.format.MediaFormat;
import com.rockchip.mediacenter.mediaplayer.model.FileInfo;
import com.rockchip.mediacenter.mediaplayer.model.LastLevelFileInfo;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

public class ShareMediaFileInfoProvider {
	
	//图片
	public static final String[] PROJECTION_IMAGE = new String[] {
		MediaStore.Images.Media._ID,  //0
		MediaStore.Images.Media.DATA, //1
		MediaStore.Images.Media.TITLE,  //2
		MediaStore.Images.Media.MIME_TYPE, //3
		MediaStore.Images.Media.SIZE, //4
		MediaStore.Images.Media.DATE_MODIFIED //5
    };
	//音频
	public static final String[] PROJECTION_AUDIO = new String[] {
		MediaStore.Audio.Media._ID, //0
		MediaStore.Audio.Media.DATA,//1
		MediaStore.Audio.Media.TITLE,//2
		MediaStore.Audio.Media.MIME_TYPE,//3
		MediaStore.Audio.Media.SIZE,//4
		MediaStore.Audio.Media.DURATION,//5
		MediaStore.Audio.Media.DATE_MODIFIED//6
    };
	//视频
	public static final String[] PROJECTION_VIDEO = new String[] {
		MediaStore.Video.Media._ID,//0
		MediaStore.Video.Media.DATA,//1
		MediaStore.Video.Media.TITLE,//2
		MediaStore.Video.Media.MIME_TYPE,//3
		MediaStore.Video.Media.SIZE,//4
		MediaStore.Video.Media.DURATION,//5
		MediaStore.Video.Media.DATE_MODIFIED//6
    };
	
	private Context mContext;
	
	public ShareMediaFileInfoProvider(Context context){
		mContext = context;
	}
	
	/**
	 * 获取媒体类别
	 * 图片
	 * 音频
	 * 视频
	 * @return
	 */
	public List<FileInfo> getMediaCategory(){
		List<FileInfo> fileList = new ArrayList<FileInfo>();
		String[] medias = mContext.getResources().getStringArray(R.array.media_types);
		FileInfo item = null;
		for(String media : medias){
			item = new FileInfo(new File(media));
			item.setDir(true);
			if(MediaFormat.isImage(media)){
				item.setIcon(mContext.getResources().getDrawable(R.drawable.dms_share_image));
			}else if(MediaFormat.isAudio(media)){
				item.setIcon(mContext.getResources().getDrawable(R.drawable.dms_share_audio));
			}else if(MediaFormat.isVideo(media)){
				item.setIcon(mContext.getResources().getDrawable(R.drawable.dms_share_video));
			}
			fileList.add(item);
		}
		return fileList;
	}
	
	/**
	 * 获取媒体内容
	 * @param fileInfo
	 * @return
	 */
	public List<FileInfo> getMediaContents(String path){
		Uri uri = null;
		String[] projection = null;
		if(MediaFormat.isImage(path)){
			uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
			projection = PROJECTION_IMAGE;
		}else if(MediaFormat.isAudio(path)){
			uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
			projection = PROJECTION_AUDIO;
		}else if(MediaFormat.isVideo(path)){
			uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
			projection = PROJECTION_VIDEO;
		}else if(LastLevelFileInfo.isLastLevelPath(path)){
			return getMediaCategory();
		}else{
			return new ArrayList<FileInfo>();
		}
		return getMediaFileInfos(uri, projection);
	}
	
	
	/**
	 * 获取媒体库文件信息
	 * @return
	 */
	private List<FileInfo> getMediaFileInfos(Uri uri, String[] projection){
		Cursor cursor = mContext.getContentResolver().query(uri, projection, null, null, null);
    	List<FileInfo> fileList = new ArrayList<FileInfo>();
    	FileInfo item = null;
		while(cursor.moveToNext()){
			item = new FileInfo(new File(cursor.getString(1)));
			item.setDir(false);
			fileList.add(item);
		}
		cursor.close();
    	return fileList;
	}
}
