package com.rockchips.mediacenter.utils;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.DIDLObject.Property;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;
import org.json.JSONArray;
import org.json.JSONObject;

import momo.cn.edu.fjnu.androidutils.data.CommonValues;
import momo.cn.edu.fjnu.androidutils.utils.JsonUtils;
import android.R.integer;
import android.os.Environment;
import android.provider.SyncStateContract.Constants;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.media.iso.ISOManager;
import com.rockchips.mediacenter.utils.HanziToPinyin;
import com.rockchips.mediacenter.utils.HanziToPinyin.Token;
import com.rockchips.mediacenter.adapter.AllUpnpFileListAdapter;
import com.rockchips.mediacenter.bean.Device;
import com.rockchips.mediacenter.bean.FileInfo;
import com.rockchips.mediacenter.bean.LocalMediaInfo;
import com.rockchips.mediacenter.bean.LocalDeviceInfo;
import com.rockchips.mediacenter.bean.AllFileInfo;
import com.rockchips.mediacenter.bean.AllUpnpFileInfo;
import com.rockchips.mediacenter.bean.LocalDevice;
import com.rockchips.mediacenter.bean.LocalMediaFile;
import com.rockchips.mediacenter.bean.LocalMediaFolder;
import com.rockchips.mediacenter.bean.NFSInfo;
import com.rockchips.mediacenter.bean.SmbInfo;
import com.rockchips.mediacenter.bean.UpnpFile;
import com.rockchips.mediacenter.bean.UpnpFolder;
import com.rockchips.mediacenter.data.ConstData;
/**
 * @author GaoFei
 *
 */
public class MediaFileUtils {
	public static final String TAG = MediaFileUtils.class.getSimpleName();
	/**
	 * 从文件读取媒体文件类型，不能是目录
	 * @param file
	 * @return
	 */
	public static int getMediaTypeFromFile(File file){
		//蓝光文件检测
		/*if(file.isDirectory() && ISOManager.isBDDirectory(file.getPath())){
			return ConstData.MediaType.VIDEO;
		}*/
		String path = file.getPath();
		int dotIndex = path.lastIndexOf(".");
		if(dotIndex < 0)
			return ConstData.MediaType.UNKNOWN_TYPE;
		int startIndex = dotIndex + 1;
		if(startIndex >= path.length())
			return ConstData.MediaType.UNKNOWN_TYPE;
		String tailEx = path.substring(startIndex).toLowerCase();
		if(Arrays.binarySearch(ConstData.AUDIO_SUFFIX, tailEx) >= 0){
			return ConstData.MediaType.AUDIO;
		}
		if(Arrays.binarySearch(ConstData.VIDEO_SUFFIX, tailEx) >= 0){
			return ConstData.MediaType.VIDEO;
		}
	/*	if(Arrays.binarySearch(ConstData.SUBTITLE_SUFFIX, tailEx) >= 0){
			return ConstData.MediaType.SUBTITLE;
		}*/
		
		if(Arrays.binarySearch(ConstData.IMAGE_SUFFIX, tailEx) >= 0){
			return ConstData.MediaType.IMAGE;
		}
		
		if(Arrays.binarySearch(ConstData.APK_SUFFIX, tailEx) >= 0){
			return ConstData.MediaType.APK;
		}
		
		return ConstData.MediaType.UNKNOWN_TYPE;
	}
	
	
	/**
	 * 从汉字到拼音的转换
	 * @param source
	 * @return
	 */
    public static String getFullPinYin(String source){ 
        
        /*if (!Arrays.asList(Collator.getAvailableLocales()).contains(Locale.CHINA))
        { 
            return source; 
        }  */
    
        ArrayList<Token> tokens = HanziToPinyin.getInstance().get(source); 
    
        if (tokens == null || tokens.size() == 0) 
        { 
           return source; 
        } 
    
        StringBuffer result = new StringBuffer(); 
    
        for (Token token : tokens)
        { 
           if (token.type == Token.PINYIN) 
           { 
               result.append(token.target); 
           } 
           else 
           { 
               result.append(token.source); 
           } 
        }
        
        return result.toString(); 
    }
	
    
    /**
     * 获取文件的大小
     * @param file
     * @return
     */
    public static String getFileSize(File file){
    	if(file.isDirectory())
    		return "";
    	else{
    		long bSize = file.length();
    		long kSize = bSize / 1024;
    		long mSize = kSize / 1024;
    		long gSize = mSize / 1024;
    		if(gSize > 0)
    			return gSize + "G";
    		else if(mSize > 0)
    			return mSize + "M";
    		else if(kSize > 0)
    			return kSize + "K";
    		else
    			return bSize + "B";
    	}
    }
    
    
    public static String getFileSizeFromByte(long size){
		long bSize = size;
		long kSize = bSize / 1024;
		long mSize = kSize / 1024;
		long gSize = mSize / 1024;
		if(gSize > 0)
			return gSize + "G";
		else if(mSize > 0)
			return mSize + "M";
		else if(kSize > 0)
			return kSize + "K";
		else
			return bSize + "B";
    }
    
    /**
     * 获取某个目录下的某种媒体文件的数目
     * @param file
     * @return
     */
    public static int getMediaFileCount(File file, int mediaType){
    	int mediaFileCount = 0;
		File[] files = file.listFiles();
		if(files != null && files.length > 0){
			for(File childFile : files){
				if(childFile.isDirectory() && isExistMediaFile(childFile, mediaType))
					++mediaFileCount;
				//如果媒体文件类型为FOLDER并且有扫描到文件夹
				else if(mediaType == ConstData.MediaType.FOLDER && getMediaTypeFromFile(childFile) != ConstData.MediaType.UNKNOWN_TYPE)
					++mediaFileCount;
				else if(mediaType == getMediaTypeFromFile(childFile))
					++mediaFileCount;
			}
		}
	
    	
    	return mediaFileCount;
    }
    
    /**
     * 判断某个文件夹(文件)是否存在媒体文件，只能是目录
     * @param file
     * @param mediaType
     * @return
     */
    public static boolean isExistMediaFile(File file, int mediaType){
    	if(file.isDirectory()){
    		File[] childFiles = file.listFiles();
    		if(childFiles == null || childFiles.length == 0)
    			return false;
    		else{
    			for(File childFile : childFiles){
    				if(isExistMediaFile(childFile, mediaType))
    					return true;
    			}
    			return false;
    		}
    	}else if(mediaType == ConstData.MediaType.FOLDER && getMediaTypeFromFile(file) != ConstData.MediaType.UNKNOWN_TYPE)
    		return true;
    	else if(mediaType == getMediaTypeFromFile(file))
    		return true;
    	else
    		return false;
    }
    
    
    /**
     * 获取某个目录下所有含有图片的目录
     */
    public static void getAllImageFolder(File rootDirFile, List<LocalMediaInfo> mediaInfos, LocalDeviceInfo deviceInfo){
    	if(rootDirFile.isDirectory()){
    		File[] childFiles = rootDirFile.listFiles();
    		if(childFiles != null && childFiles.length > 0){
    			for(File childFile : childFiles){
    				getAllImageFolder(childFile, mediaInfos, deviceInfo);
    			}
    		}
    	}else{
    		int mediaType = getMediaTypeFromFile(rootDirFile);
    		if(mediaType == ConstData.MediaType.IMAGE){
    			LocalMediaInfo localMediaInfo = getMediaInfoFromFile(rootDirFile, ConstData.MediaType.IMAGE, deviceInfo);
    			//提取出父目录相关信息
    			if(localMediaInfo != null){
    				LocalMediaInfo parentMediaInfo = getParentImageFolder(localMediaInfo);
        			if(!mediaInfos.contains(parentMediaInfo))
        				mediaInfos.add(parentMediaInfo);
    			}
    			
    			
    		}
    	}
    }
    
    /**
     * 从文件里解析媒体信息
     * @param file
     * @return
     */
    public static LocalMediaInfo getMediaInfoFromFile(File file, int mediaType, LocalDeviceInfo deviceInfo){
    	//Log.i(TAG, "getMediaInfoFromFile");
    	int currentMediaType = getMediaTypeFromFile(file);
    	if(currentMediaType != mediaType)
    		return null;
    	LocalMediaInfo localMediaInfo = new LocalMediaInfo();
		localMediaInfo.setmFileName(file.getName());
		localMediaInfo.setmParentPath(file.getParentFile().getPath());
		localMediaInfo.setmModifyDate((int) (file.lastModified() / 1000));
		localMediaInfo.setmPinyin(MediaFileUtils.getFullPinYin(file.getName()));
		localMediaInfo.setmDeviceType(deviceInfo.getDeviceType());
		localMediaInfo.setmPhysicId(deviceInfo.getPhysicId());
		localMediaInfo.setmFileSize(file.length());
		localMediaInfo.setmFiles(0);
		localMediaInfo.setmFileType(currentMediaType);
		return localMediaInfo;
    }
    
    /**
     * 从文件中获取媒体信息
     * @param file
     * @return
     */
    public static LocalMediaFile getMediaFileFromFile(File file, LocalDevice device){
    	int currentMediaType = getMediaTypeFromFile(file);
    	if(currentMediaType == ConstData.MediaType.UNKNOWN_TYPE)
    		return null;
    	LocalMediaFile localMediaFile = new LocalMediaFile();
    	localMediaFile.setDeviceID(device.getDeviceID());
    	localMediaFile.setDevicetype(device.getDevices_type());
    	localMediaFile.setLast_modify_date(file.lastModified() / 1000);
    	localMediaFile.setName(file.getName());
    	localMediaFile.setPath(file.getPath());
    	localMediaFile.setPhysic_dev_id(device.getPhysic_dev_id());
    	localMediaFile.setPinyin(getFullPinYin(file.getName()));
    	localMediaFile.setSize(file.length());
    	localMediaFile.setType(currentMediaType);
    	localMediaFile.setParentPath(file.getParentFile().getPath());
		//localMediaFile.setDeviceID()
		return localMediaFile;
    }
    
    /**
     * 从文件种获取FileInfo
     * @param file
     * @param device
     * @return
     */
    public static FileInfo getFileInfoFromFile(File file, Device device){
    	int currentMediaType = getMediaTypeFromFile(file);
    	if(currentMediaType == ConstData.MediaType.UNKNOWN_TYPE ||
    			currentMediaType == ConstData.MediaType.APK)
    		return null;
    	FileInfo fileInfo = new FileInfo();
    	fileInfo.setDeviceID(device.getDeviceID());
    	fileInfo.setModifyTime(file.lastModified());
    	fileInfo.setName(file.getName());
    	fileInfo.setPath(file.getPath());
    	fileInfo.setSize(file.length());
    	fileInfo.setType(currentMediaType);
    	fileInfo.setParentPath(file.getParent());
		return fileInfo;
    }
    
    
    /**
     * 获取蓝光视频文件,与getMediaFileFromFile区别开来，为了提高扫描效率
     * @param file
     * @return
     */
    public static LocalMediaFile getBDMediaFile(File file, LocalDevice device){
    	LocalMediaFile localMediaFile = new LocalMediaFile();
    	localMediaFile.setDeviceID(device.getDeviceID());
    	localMediaFile.setDevicetype(device.getDevices_type());
    	localMediaFile.setLast_modify_date(file.lastModified() / 1000);
    	localMediaFile.setName(file.getName());
    	localMediaFile.setPath(file.getPath());
    	localMediaFile.setPhysic_dev_id(device.getPhysic_dev_id());
    	localMediaFile.setPinyin(getFullPinYin(file.getName()));
    	localMediaFile.setSize(file.length());
    	localMediaFile.setType(ConstData.MediaType.VIDEO);
    	localMediaFile.setParentPath(file.getParentFile().getPath());
		//localMediaFile.setDeviceID()
		return localMediaFile;
    }
    
    /**
     * 获取蓝光视频文件
     * @param file
     * @param device
     * @return
     */
    public static FileInfo getBDFileInfo(File file, Device device){
    	FileInfo fileInfo = new FileInfo();
    	fileInfo.setDeviceID(device.getDeviceID());
    	fileInfo.setModifyTime(file.lastModified());
    	fileInfo.setName(file.getName());
    	fileInfo.setPath(file.getPath());
    	fileInfo.setType(ConstData.MediaType.VIDEO);
    	return fileInfo;
    }
    
    /**
     * 从文件中获取媒体文件夹
     * @param file
     * @param device
     * @return
     */
    public static LocalMediaFolder getMediaFolderFromFile(File file, LocalDevice device){
    	LocalMediaFolder mediaFolder = new LocalMediaFolder();
    	mediaFolder.setDeviceID(device.getDeviceID());
    	mediaFolder.setDevicetype(device.getDevices_type());
    	//mediaFolder.setFolderType(folderType);
    	mediaFolder.setLast_modify_date(file.lastModified() / 1000);
    	mediaFolder.setName(file.getName());
    	mediaFolder.setPath(file.getPath());
    	mediaFolder.setPhysic_dev_id(device.getPhysic_dev_id());
    	mediaFolder.setPinyin(getFullPinYin(file.getName()));
    	mediaFolder.setParentPath(file.getParentFile().getPath());
    	return mediaFolder;
    }
    
    private static String getDeviceIdFromMountFile(File mountFile){
    	String mountPath = mountFile.getPath();
    	String[] strPaths = mountPath.split("/");
    	if(strPaths != null && strPaths.length > 0){
    		return strPaths[strPaths.length - 1];
    	}
    	return null;
    }
    
    /**
     * 获取照片父目录的相关信息
     * @param mediaInfo
     * @return
     */
    public static LocalMediaInfo getParentImageFolder(LocalMediaInfo mediaInfo){
    	//获取父目录路径
    	String parentPath = mediaInfo.getmParentPath();
    	String parentName = parentPath.substring(parentPath.lastIndexOf("/") + 1);
    	File parentFile = new File(parentPath);
    	LocalMediaInfo localMediaInfo = new LocalMediaInfo();
		localMediaInfo.setmFileName(parentName);
		localMediaInfo.setmParentPath(parentFile.getParentFile().getPath());
		localMediaInfo.setmModifyDate((int) (parentFile.lastModified() / 1000));
		localMediaInfo.setmPinyin(MediaFileUtils.getFullPinYin(parentFile.getName()));
		localMediaInfo.setmDeviceType(mediaInfo.getmDeviceType());
		localMediaInfo.setmPhysicId(mediaInfo.getmPhysicId());
		localMediaInfo.setmFileSize(parentFile.length());
		localMediaInfo.setmFiles(getMediaFileCount(parentFile, ConstData.MediaType.IMAGE));
		localMediaInfo.setmFileType(ConstData.MediaType.IMAGEFOLDER);
		return localMediaInfo;
    }
    
    /**
     * 从当前目录下获取某种类型的媒体文件信息
     * @return
     */
    public static List<LocalMediaInfo> getChildMediaInfos(String dirPath, int mediaType, LocalDeviceInfo deviceInfo){
    	//Log.i(TAG, "getChildMediaInfos->dirPath:" + dirPath);
    	List<LocalMediaInfo> mediaInfos = new ArrayList<LocalMediaInfo>();
    	File dirFile = new File(dirPath);
    	File[] childFiles = dirFile.listFiles();
    	for(File childFile : childFiles){
    		if(!childFile.isDirectory()){
    			LocalMediaInfo mediaInfo = getMediaInfoFromFile(childFile, mediaType, deviceInfo);
    			//Log.i(TAG, "getChildMediaInfos->mediaInfo:" + mediaInfo);
    			if(mediaInfo != null)
    				mediaInfos.add(mediaInfo);
    		}
    	}
    	return mediaInfos;
    }
    
    /**
     * 从挂载路径生成Device
     * @param mountPath
     * @param deviceType
     * @return
     */
    public static Device getDeviceFromMountPath(String mountPath, String netWorkPath , int deviceType, String deviceName){
    	Device device = new Device();
    	File mountFile = new File(mountPath);
    	if(!mountFile.exists() && deviceType != ConstData.DeviceType.DEVICE_TYPE_DMS)
    		return null;
    	//本地设备有3种模式
    	if(deviceType == ConstData.DeviceType.DEVICE_TYPE_LOCAL){
    		if(StorageUtils.isMountUsb(CommonValues.application, mountPath)){
    			device.setDeviceType(ConstData.DeviceType.DEVICE_TYPE_U);
    		}else if(StorageUtils.isMountSdCard(CommonValues.application, mountPath)){
    			device.setDeviceType(ConstData.DeviceType.DEVICE_TYPE_SD);
    		}else{
    			device.setDeviceType(ConstData.DeviceType.DEVICE_TYPE_INTERNEL_STORAGE);
    		}
    		device.setLocalMountPath(mountPath);
    		device.setDeviceName(getDeviceIdFromMountFile(mountFile));
    		device.setNetWorkPath(netWorkPath);
    	}else{
    		if(deviceType != ConstData.DeviceType.DEVICE_TYPE_DMS)
    			device.setDeviceName(getDeviceIdFromMountFile(mountFile));
    		else
    			device.setDeviceName(deviceName);
    		device.setDeviceType(deviceType);
    		device.setLocalMountPath(mountPath);
    		device.setNetWorkPath(netWorkPath);
    	}
    	return device;
    }
    
    /**
     * 从挂载路径获取设备信息
     * @param mountFile
     * @return
     */
    public static LocalDevice getLocalDeviceFromFile(File mountFile) {
    	if(mountFile == null || !mountFile.exists())
    		return null;
    	LocalDevice localDevice = new LocalDevice();
    	if(StorageUtils.isMountUsb(CommonValues.application, mountFile.getPath())){
    		localDevice.setDevices_type(ConstData.DeviceType.DEVICE_TYPE_U);
    	}else if(StorageUtils.isMountSdCard(CommonValues.application, mountFile.getPath())){
    		localDevice.setDevices_type(ConstData.DeviceType.DEVICE_TYPE_SD);
    	}else if(StorageUtils.getFlashStoragePath().equals(mountFile.getPath())){
    		localDevice.setDevices_type(ConstData.DeviceType.DEVICE_TYPE_INTERNEL_STORAGE);
    	}
    	else{
    		//尝试读取Samba,NFS设备
    		String nfsInfos = momo.cn.edu.fjnu.androidutils.utils.StorageUtils.getDataFromSharedPreference(ConstData.SharedKey.NFS_INFOS);
    		String sambaInfos = momo.cn.edu.fjnu.androidutils.utils.StorageUtils.getDataFromSharedPreference(ConstData.SharedKey.SMB_INFOS);
    		boolean isFindCifs = false;
    		if(!TextUtils.isEmpty(nfsInfos)){
    			try{
    				JSONArray nfsArray = new JSONArray(nfsInfos);
    				List<NFSInfo> nfsList = (List<NFSInfo>)JsonUtils.arrayToList(NFSInfo.class, nfsArray);
    				for(NFSInfo itemNfsInfo : nfsList){
    					if(itemNfsInfo.getLocalMountPath().equals(mountFile.getPath())){
    						isFindCifs = true;
    						//没有挂载成功直接返回NULL
    						if(!MountUtils.isMountSuccess(itemNfsInfo.getNetWorkPath(), itemNfsInfo.getLocalMountPath()))
    							return null;
    						localDevice.setDevices_type(ConstData.DeviceType.DEVICE_TYPE_NFS);
    						break;
    					}
    				}
    			}catch (Exception e){
    				return null;
    			}
    		}
    		
    		if(!TextUtils.isEmpty(sambaInfos) && !isFindCifs){
    			try{
    				JSONArray sambaArray = new JSONArray(sambaInfos);
    				List<SmbInfo> smbList = (List<SmbInfo>)JsonUtils.arrayToList(SmbInfo.class, sambaArray);
    				for(SmbInfo itemSmbInfo : smbList){
    					if(itemSmbInfo.getLocalMountPath().equals(mountFile.getPath())){
    						//没有挂载成功直接返回NULL
    						if(!MountUtils.isMountSuccess(itemSmbInfo.getNetWorkPath(), itemSmbInfo.getLocalMountPath()))
    							return null;
    						localDevice.setDevices_type(ConstData.DeviceType.DEVICE_TYPE_SMB);
    						break;
    					}
    				}
    			}catch (Exception e){
    				return null;
    			}
    		}
    	}
    	localDevice.setFree(getFileSizeFromByte(mountFile.getFreeSpace()));
    	localDevice.setHas_scaned(false);
    	localDevice.setMountPath(mountFile.getPath());
    	localDevice.setPhysic_dev_id(getDeviceIdFromMountFile(mountFile));
    	localDevice.setSize(getFileSizeFromByte(mountFile.getTotalSpace()));
    	localDevice.setUsed(getFileSizeFromByte(mountFile.getTotalSpace() - mountFile.getFreeSpace()));
    	return localDevice;
    }
    
    
    /**
     * 从LocalDevice到LocalDeviceInfo的转换
     * @param device
     * @return
     */
    public static LocalDeviceInfo getDeviceInfoFromDevice(Device device){
    	LocalDeviceInfo deviceInfo = new LocalDeviceInfo();
    	deviceInfo.setTotalSize("");
    	deviceInfo.setUsedSize("");
    	deviceInfo.setFreeSize("");
    	deviceInfo.setUsedPercent("");
    	deviceInfo.setPhysicId(device.getDeviceName());
    	deviceInfo.setIsPhysicDev(1);
    	deviceInfo.setMountPath(device.getLocalMountPath());
    	deviceInfo.setIsScanned(1);
    	deviceInfo.setmDevCount(1);
    	deviceInfo.setmDeviceId(0);
    	deviceInfo.setDeviceType(device.getDeviceType());
    	return deviceInfo;
    }
    
    public static LocalMediaInfo getMediaInfoFromFile(LocalMediaFile mediaFile){
    	LocalMediaInfo localMediaInfo = new LocalMediaInfo();
		localMediaInfo.setmFileName(mediaFile.getName());
		localMediaInfo.setmParentPath(mediaFile.getParentPath());
		localMediaInfo.setmModifyDate((int)mediaFile.getLast_modify_date());
		localMediaInfo.setmPinyin(MediaFileUtils.getFullPinYin(mediaFile.getName()));
		localMediaInfo.setmDeviceType(mediaFile.getDevicetype());
		localMediaInfo.setmPhysicId(mediaFile.getPhysic_dev_id());
		localMediaInfo.setmFileSize(mediaFile.getSize());
		localMediaInfo.setmFiles(0);
		localMediaInfo.setmFileType(mediaFile.getType());
		return localMediaInfo;
    }
    
    public static LocalMediaInfo getMediaInfoFromUpnpFile(UpnpFile mediaFile){
    	LocalMediaInfo localMediaInfo = new LocalMediaInfo();
		localMediaInfo.setmFileName(mediaFile.getName());
		localMediaInfo.setmParentPath("");
		localMediaInfo.setmModifyDate((int)mediaFile.getLast_modify_date());
		localMediaInfo.setmPinyin(MediaFileUtils.getFullPinYin(mediaFile.getName()));
		localMediaInfo.setmDeviceType(mediaFile.getDevicetype());
		localMediaInfo.setmPhysicId(mediaFile.getPhysic_dev_id());
		localMediaInfo.setmFileSize(mediaFile.getSize());
		localMediaInfo.setmFiles(0);
		localMediaInfo.setmFileType(mediaFile.getType());
		localMediaInfo.setmResUri(mediaFile.getPath());
		return localMediaInfo;
    }
    
    public static LocalMediaInfo getMediaInfoFromFolder(LocalMediaFolder mediaFolder){
    	LocalMediaInfo localMediaInfo = new LocalMediaInfo();
		localMediaInfo.setmFileName(mediaFolder.getName());
		localMediaInfo.setmParentPath(mediaFolder.getParentPath());
		localMediaInfo.setmModifyDate((int)mediaFolder.getLast_modify_date());
		localMediaInfo.setmPinyin(MediaFileUtils.getFullPinYin(mediaFolder.getName()));
		localMediaInfo.setmDeviceType(mediaFolder.getDevicetype());
		localMediaInfo.setmPhysicId(mediaFolder.getPhysic_dev_id());
		localMediaInfo.setmFileSize(0);
		localMediaInfo.setmFiles(mediaFolder.getFileCount());
		localMediaInfo.setmFileType(mediaFolder.getFolderType());
		return localMediaInfo;
    }
    
    
    public static LocalMediaInfo getMediaInfoFromUpnpFolder(UpnpFolder mediaFolder, int folderType){
    	LocalMediaInfo localMediaInfo = new LocalMediaInfo();
		localMediaInfo.setmFileName(mediaFolder.getName());
		localMediaInfo.setmParentPath("");
		localMediaInfo.setmModifyDate((int)mediaFolder.getLast_modify_date());
		localMediaInfo.setmPinyin(MediaFileUtils.getFullPinYin(mediaFolder.getName()));
		localMediaInfo.setmDeviceType(mediaFolder.getDevicetype());
		localMediaInfo.setmPhysicId(mediaFolder.getPhysic_dev_id());
		localMediaInfo.setmFileSize(0);
		localMediaInfo.setFirstPhotoUrl(mediaFolder.getFirstPhotoUrl());
		if(folderType == ConstData.MediaType.IMAGEFOLDER)
			localMediaInfo.setmFiles(mediaFolder.getImageCount());
		else if(folderType == ConstData.MediaType.VIDEOFOLDER)
			localMediaInfo.setmFiles(mediaFolder.getVideoCount());
		else if(folderType == ConstData.MediaType.AUDIOFOLDER)
			localMediaInfo.setmFiles(mediaFolder.getAudioCount());
		else
			localMediaInfo.setmFiles(mediaFolder.getFileCount());
		localMediaInfo.setmFileType(folderType);
		return localMediaInfo;
    }
    
    
    public static List<LocalMediaInfo> getMediaInfoList(List<LocalMediaFile> mediaFiles){
    	List<LocalMediaInfo> mediaInfos = new ArrayList<LocalMediaInfo>();
    	if(mediaFiles != null && mediaFiles.size() > 0){
    		for(LocalMediaFile mediaFile : mediaFiles){
    			mediaInfos.add(getMediaInfoFromFile(mediaFile));
    		}
    	}
    	return mediaInfos;
    }
    
    
    public static List<LocalMediaInfo> getMediaInfoListFromUpnpFileList(List<UpnpFile> upnpFiles){
    	List<LocalMediaInfo> mediaInfos = new ArrayList<LocalMediaInfo>();
    	if(upnpFiles != null && upnpFiles.size() > 0){
    		for(UpnpFile mediaFile : upnpFiles){
    			mediaInfos.add(getMediaInfoFromUpnpFile(mediaFile));
    		}
    	}
    	return mediaInfos;
    }
    
    
    
    public static List<LocalMediaInfo> getMediaInfoListFromFolders(List<LocalMediaFolder> mediaFolders){
    	List<LocalMediaInfo> mediaInfos = new ArrayList<LocalMediaInfo>();
    	if(mediaFolders != null && mediaFolders.size() > 0){
    		for(LocalMediaFolder mediaFolder : mediaFolders){
    			mediaInfos.add(getMediaInfoFromFolder(mediaFolder));
    		}
    	}
    	return mediaInfos;
    }
    
    
    public static List<LocalMediaInfo> getMediaInfoListFromUpnpFolders(List<UpnpFolder> mediaFolders, int folderType){
    	List<LocalMediaInfo> mediaInfos = new ArrayList<LocalMediaInfo>();
    	if(mediaFolders != null && mediaFolders.size() > 0){
    		for(UpnpFolder mediaFolder : mediaFolders){
    			mediaInfos.add(getMediaInfoFromUpnpFolder(mediaFolder, folderType));
    		}
    	}
    	return mediaInfos;
    }
    
    
    
    /**
     * 从远程UPNP设备映射至本地设备
     * @param remoteDevice
     * @return
     */
    public static LocalDevice getLocalDeviceFromRemoteDevice(RemoteDevice remoteDevice){
    	LocalDevice pnpServerDevice = new LocalDevice();
    	//这里的deviceID由自己指定
    	pnpServerDevice.setDeviceID(remoteDevice.getIdentity().getUdn().getIdentifierString());
    	pnpServerDevice.setDevices_type(ConstData.DeviceType.DEVICE_TYPE_DMS);
    	pnpServerDevice.setFree("");
    	pnpServerDevice.setHas_scaned(false);
    	pnpServerDevice.setMountPath(remoteDevice.getIdentity().getDescriptorURL().toString());
    	pnpServerDevice.setPhysic_dev_id(remoteDevice.getDetails().getFriendlyName());
    	pnpServerDevice.setSize("");
    	pnpServerDevice.setUsed("");
    	return pnpServerDevice;
    }
    
    /**
     * 获取某个媒体文件对应该目录下的所有文件
     * @param allFileInfo
     * @return
     */
    public static List<LocalMediaInfo> getMediaInfosFromAllFileInfo(AllFileInfo allFileInfo, LocalDevice device){
    	List<LocalMediaInfo> mediaInfos = new ArrayList<LocalMediaInfo>();
    	File parentFile = allFileInfo.getFile().getParentFile();
    	if(parentFile != null){
    		File[] subFiles = parentFile.listFiles();
    		if(subFiles != null && subFiles.length > 0){
    			for(File itemFile : subFiles){
    				//LocalMediaInfo localMediaInfo = getMediaInfoFromFile(itemFile, allFileInfo.getType(), getDeviceInfoFromDevice(device));
    				LocalMediaInfo localMediaInfo = null;
    				if(localMediaInfo != null)
    					mediaInfos.add(localMediaInfo);
    			}
    		}
    	}
    	return mediaInfos;
    }
    
    
    /**
     * 获取某个媒体文件对应该目录下的所有文件
     * @param allFileInfo
     * @return
     */
    public static List<LocalMediaInfo> getLocalMediaInfos(List<FileInfo> fileInfos, Device device, int type){
    	List<LocalMediaInfo> mediaInfos = new ArrayList<LocalMediaInfo>();
    	if(fileInfos != null && fileInfos.size() > 0){
    		for(FileInfo itemFileInfo : fileInfos){
    			if(itemFileInfo.getType() != type)
    				continue;
    			LocalMediaInfo localMediaInfo = new LocalMediaInfo();
    			localMediaInfo.setmFileName(itemFileInfo.getName());
    			localMediaInfo.setmParentPath(itemFileInfo.getParentPath());
    			localMediaInfo.setmModifyDate((int) (itemFileInfo.getModifyTime() / 1000));
    			localMediaInfo.setmPinyin(MediaFileUtils.getFullPinYin(itemFileInfo.getName()));
    			localMediaInfo.setmDeviceType(device.getDeviceType());
    			localMediaInfo.setmPhysicId(device.getDeviceName());
    			localMediaInfo.setmFileSize(itemFileInfo.getSize());
    			localMediaInfo.setmFiles(0);
    			localMediaInfo.setmFileType(itemFileInfo.getType());
    			mediaInfos.add(localMediaInfo);
    		}
    	}
    	return mediaInfos;
    }
 
    /**
     * 获取当前目录下与upnpFileInfo对应的同类型的媒体文件列表
     * @param upnpFileInfo
     * @param container
     * @return
     */
    public static List<LocalMediaInfo>  getMediaInfosFromAllUpnpFileInfo(AllUpnpFileInfo upnpFileInfo, DIDLContent content, LocalDevice localDevice){
    	List<LocalMediaInfo> mediaInfos = new ArrayList<LocalMediaInfo>();
    	List<Item> items = content.getItems();
    	Log.i(TAG, "getMediaInfosFromAllUpnpFileInfo->items:" + items);
    	if(items != null && items.size() > 0){
    		for(Item item : items){
    			if(upnpFileInfo.getType() == getMediaTypeFromUpnpItem(item)){
    				LocalMediaInfo localMediaInfo = new LocalMediaInfo();
    				localMediaInfo.setmFileName(item.getTitle());
    				localMediaInfo.setmParentPath("");
    				//localMediaInfo.setmModifyDate((int)mediaFile.getLast_modify_date());
    				localMediaInfo.setmPinyin(MediaFileUtils.getFullPinYin(item.getTitle()));
    				localMediaInfo.setmDeviceType(ConstData.DeviceType.DEVICE_TYPE_DMS);
    				localMediaInfo.setmPhysicId(localDevice.getPhysic_dev_id());
    				localMediaInfo.setmFileSize(getFileSizeFromUpnpItem(item));
    				localMediaInfo.setmFiles(0);
    				localMediaInfo.setmFileType(upnpFileInfo.getType());
    				localMediaInfo.setmResUri(getPathFromUpnpItem(item));
    				mediaInfos.add(localMediaInfo);
    			}
    		}
    	}
    	
		return mediaInfos;
    }
    
    /**
     * 从Item中获取媒体文件类型
     * @param item
     * @return
     */
    public static int getMediaTypeFromUpnpItem(Item item){
    	String contentFormat = null;
    	try{
    		contentFormat = item.getResources().get(0).getProtocolInfo().getContentFormat();
    	}catch (Exception e){
    		
    	}
    	if(contentFormat != null){
    		if(contentFormat.contains("audio"))
    			return ConstData.MediaType.AUDIO;
    		else if(contentFormat.contains("video"))
    			return ConstData.MediaType.VIDEO;
    		else if(contentFormat.contains("image"))
    			return ConstData.MediaType.IMAGE;
    		return ConstData.MediaType.UNKNOWN_TYPE;
    	}
    	return ConstData.MediaType.UNKNOWN_TYPE;
    }
    
    /**
     * 从Item获取文件大小
     * @param item
     * @return
     */
    public static long getFileSizeFromUpnpItem(Item item){
    	long size = 0;
    	try{
    		size = item.getResources().get(0).getSize();
    	}catch (Exception e){
    		
    	}
    	return size;
    }
    
    /**
     * 从Item中获取文件路径
     * @param item
     * @return
     */
    public static String getPathFromUpnpItem(Item item){
    	String path = "";
    	try{
    		path = item.getResources().get(0).getValue();
    	}catch(Exception e){
    		
    	}
    	
    	return path;
    }
    
    
	/**
	 * 从文件夹类型获取对应的媒体文件类型
	 * @param folderType
	 * @return
	 */
	public static int getFileTypeFromFolderType(int folderType){
		int fileType = -1;
		switch (folderType) {
		case ConstData.MediaType.AUDIOFOLDER:
			fileType = ConstData.MediaType.AUDIO;
			break;
		case ConstData.MediaType.VIDEOFOLDER:
			fileType = ConstData.MediaType.VIDEO;
			break;
		case ConstData.MediaType.IMAGEFOLDER:
			fileType = ConstData.MediaType.IMAGE;
			break;
		default:
			break;
		}
		return fileType;
	}
    
	/**
	 * 获取文件列表信息
	 * @param content
	 * @return
	 */
	public static List<FileInfo> getFileInfos(DIDLContent content, Device device){
		Log.i(TAG, "getFileInfos->content:" + content);
		DialogUtils.closeLoadingDialog();
		List<Container> containers = content.getContainers();
		List<Item> items = content.getItems();
		List<FileInfo> fileInfos = new ArrayList<FileInfo>();
		Log.i(TAG, "fillUpnpFileAdapter->containers:" + containers);
		if(containers != null && containers.size() > 0){
			try{
				for(Container itemContainer : containers){
					List<Property> properties = itemContainer.getProperties();
					if(properties != null && properties.size() > 0){
						for(Property property : properties){
							Log.i(TAG, "Container->property->name:" + property.getDescriptorName() + " "
									+ "Container->property->value:" + property.getValue().toString());
							
						}
					}
					FileInfo fileInfo = new FileInfo();
					fileInfo.setChildCount(itemContainer.getChildCount());
					fileInfo.setDeviceID(device.getDeviceID());
					fileInfo.setName(itemContainer.getTitle());
					fileInfo.setType(ConstData.MediaType.FOLDER);
					JSONObject jsonInfo = new JSONObject();
					jsonInfo.put(ConstData.UpnpFileOhterInfo.ID, itemContainer.getId());
					jsonInfo.put(ConstData.UpnpFileOhterInfo.PARENT_ID, itemContainer.getParentID());
					jsonInfo.put(ConstData.UpnpFileOhterInfo.DATE, "2017-3-13");
					fileInfo.setOtherInfo(jsonInfo.toString());
					fileInfos.add(fileInfo);
				}
			}catch (Exception e){
				
			}

			
		}
		Log.i(TAG, "fillUpnpFileAdapter->items:" + items);
		if(items != null && items.size() > 0){
			try{
				for(Item item : items){
					List<Property> properties = item.getProperties();
					if(properties != null && properties.size() > 0){
						for(Property property : properties){
							Log.i(TAG, "Item->property->name:" + property.getDescriptorName() + " "
									+ "Item->property->value:" + property.getValue().toString());
							
						}
					}
					FileInfo fileInfo = new FileInfo();
					fileInfo.setDeviceID(device.getDeviceID());
					fileInfo.setName(item.getTitle());
					JSONObject jsonInfo = new JSONObject();
					jsonInfo.put(ConstData.UpnpFileOhterInfo.ID, item.getId());
					jsonInfo.put(ConstData.UpnpFileOhterInfo.PARENT_ID, item.getParentID());
					jsonInfo.put(ConstData.UpnpFileOhterInfo.DATE, "2017-3-13");
					fileInfo.setOtherInfo(jsonInfo.toString());
					List<Res> resources = item.getResources();
					if(resources != null &&  resources.size() > 0 && resources.get(0) != null && resources.get(0).getProtocolInfo() != null
							&& resources.get(0).getProtocolInfo().getContentFormat() != null){
						String contentFormat = resources.get(0).getProtocolInfo().getContentFormat();
						fileInfo.setPath(resources.get(0).getValue());
						if(contentFormat.contains("audio")){
							fileInfo.setType(ConstData.MediaType.AUDIO);
						}else if(contentFormat.contains("video")){
							fileInfo.setType(ConstData.MediaType.VIDEO);
						}else if(contentFormat.contains("image")){
							fileInfo.setType(ConstData.MediaType.IMAGE);
						}else {
							fileInfo.setType(ConstData.MediaType.UNKNOWN_TYPE);
						}
					}else{
						fileInfo.setType(ConstData.MediaType.UNKNOWN_TYPE);
					}
					fileInfos.add(fileInfo);
				}
			}catch (Exception e){
				
			}

		}
		return fileInfos;
	}
}
