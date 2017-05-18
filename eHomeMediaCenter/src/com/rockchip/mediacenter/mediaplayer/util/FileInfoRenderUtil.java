package com.rockchip.mediacenter.mediaplayer.util;

import java.util.ArrayList;
import java.util.List;

import com.rockchip.mediacenter.dlna.model.DeviceItem;
import com.rockchip.mediacenter.dlna.dmp.model.ContentItem;
import com.rockchip.mediacenter.mediaplayer.FileControl;
import com.rockchip.mediacenter.mediaplayer.model.FileInfo;

/**
 * DLNA设备和文件路径由标识和名称组成
 * 根目录：DLNA_DIR
 * 设备目录：DLNA_DEVICE_DIR+设备UDN+设备名称
 * 文件目录: DLNA_FILE_DIR+设备UDN+设备名称+文件标识+文件名称
 * @author xwf
 */
public class FileInfoRenderUtil {

	public static final String SPLIT = "\\";
	public static final String SPLIT2 = "\\\\";
	public static final String DLNA_DIR = FileControl.TOP_DIR;
	public static final String DLNA_DEVICE_DIR = DLNA_DIR+"_devicelist";
	public static final String DLNA_FILE_DIR = DLNA_DIR+"_filelist";
	
	
	public static FileInfo toFileInfo(DeviceItem deviceItem){
		FileInfo fileInfo = new FileInfo(deviceItem);
		if(deviceItem==null) return fileInfo;
		fileInfo.setPath(DLNA_DEVICE_DIR+SPLIT+deviceItem.getUdn()+SPLIT+deviceItem.getFriendlyName());
		return fileInfo;
	}
	
	public static ArrayList<FileInfo> toFileInfo(List<DeviceItem> deviceItemList){
		ArrayList<FileInfo> fileInfoList = new ArrayList<FileInfo>();
		if(deviceItemList==null||deviceItemList.size()==0){
			return fileInfoList;
		}
		for(DeviceItem item : deviceItemList){
			fileInfoList.add(toFileInfo(item));
		}
		return fileInfoList;
	}
	
	public static FileInfo toFileInfoForMedia(ContentItem contentItem){
		FileInfo fileInfo = new FileInfo(contentItem);
		if(contentItem==null) return fileInfo;
		fileInfo.setPath(DLNA_FILE_DIR+SPLIT+contentItem.getId().getId()+SPLIT+contentItem.getTitle());
		return fileInfo;
	}
	
	public static ArrayList<FileInfo> toFileInfoForMedia(List<ContentItem> contentItemList){
		ArrayList<FileInfo> fileInfoList = new ArrayList<FileInfo>();
		if(contentItemList==null||contentItemList.size()==0){
			return fileInfoList;
		}
		for(ContentItem item : contentItemList){
			fileInfoList.add(toFileInfoForMedia(item));
		}
		return fileInfoList;
	}
	
	/**
	 * 判断是否为DLNA设备或文件
	 * @author xwf
	 */
	public static boolean isDLNADevice(String path){
		if(path==null) return false;
		return path.startsWith(DLNA_DEVICE_DIR);
	}
	
	/**
	 * 判断是否为DLNA文件
	 * @author xwf
	 */
	public static boolean isDLNAFile(String path){
		if(path==null) return false;
		return path.startsWith(DLNA_FILE_DIR);
	}
	
	/**
	 * 判断是否为DLNA根
	 * @author xwf
	 */
	public static boolean isDLNARoot(String path){
		if(path==null) return false;
		return path.equals(DLNA_DIR);
	}
	
	/**
	 * 判断是否为DLNA区域
	 * @author xwf
	 */
	public static boolean isDLNAZone(String path){
		if(path==null) return false;
		return path.startsWith(DLNA_DIR);
	}
	
	/**
	 * 获取DLNA文件路径
	 * @param curPath
	 * @param goPath
	 * @return
	 */
	public static String changeDLNAFilePath(String curPath, String goPath){
		if(!isDLNAZone(curPath)||!isDLNAFile(goPath)) return goPath;
		String path=null;
		if(isDLNADevice(curPath)){
			path = curPath.substring(DLNA_DEVICE_DIR.length()+1);
		}
		goPath = goPath.substring(DLNA_FILE_DIR.length()+1);
		if(path==null){
			path = curPath+SPLIT+goPath;
		}else{
			path = DLNA_FILE_DIR+SPLIT+path+SPLIT+goPath;
		}
		return path;
	}
	
	/**
	 * 获取DLNA文件父路径
	 */
	public static String getDLNAFileParentPath(String path){
		if(isDLNAZone(path)){
			String p[] = path.split(SPLIT2);
			if(p.length==5){
				return DLNA_DEVICE_DIR+SPLIT+p[1]+SPLIT+p[2];
			}else if(p.length>5){
				path = path.substring(0, path.lastIndexOf(SPLIT));
				path = path.substring(0, path.lastIndexOf(SPLIT));
				return path;
			}else{
				return DLNA_DIR;
			}
		}else{
			return path;
		}
	}
	
	/**
	 * 获取DLNA文件路径标识
	 */
	public static String getDLNAFilePathID(String path){
		String p[] = path.split(SPLIT2);
		if(!isDLNAZone(path)||p.length<3) return null;
		return p[p.length-2];
	}
	
	/**
	 * 获取DLNA文件路径设备标识
	 */
	public static String getDLNAFileDeviceID(String path){
		String p[] = path.split(SPLIT2);
		if(!isDLNAZone(path)||p.length<3) return null;
		return p[1];
	}
	
	/**
	 * 获取完整路径名称
	 * @return
	 */
	public static String getAbsolutePath(String path){
		String p[] = path.split(SPLIT2);
		if(!isDLNAZone(path)||p.length<3) return path;
		StringBuilder sb = new StringBuilder(DLNA_DIR);
		for(int i=2; i<p.length; i=i+2){
			sb.append(SPLIT+p[i]);
		}
		return sb.toString();
	}
	/**
	 * 获取完整路径标识
	 * @return
	 */
	public static String getAbsolutePathID(String path){
		String p[] = path.split(SPLIT2);
		if(!isDLNAZone(path)||p.length<3) return path;
		StringBuilder sb = new StringBuilder(DLNA_DIR);
		for(int i=1; i<p.length; i=i+2){
			sb.append(SPLIT+p[i]);
		}
		return sb.toString();
	}
	
	/**
	 * 根据index获取第index级完整路径
	 * 从0开始
	 * @return
	 */
	public static String getPathByIndex(String path, int index){
		String p[] = path.split(SPLIT2);
		if(!isDLNAZone(path)||p.length<3) return path;
		if(index==0) return DLNA_DIR;
		StringBuilder sb = null;
		if(isDLNADevice(path)||index==1){
			sb = new StringBuilder(DLNA_DEVICE_DIR);
		}else if(isDLNAFile(path)){
			sb = new StringBuilder(DLNA_FILE_DIR);
		}else{
			sb = new StringBuilder();
		}
		for(int i=1; i<p.length; i++){
			sb.append(SPLIT+p[i]);
			if(i%2==0&&i/2==index){
				break;
			}
		}
		return sb.toString();
	}
}
