package com.rockchips.mediacenter.util;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.fourthline.cling.model.meta.RemoteDevice;
import org.json.JSONArray;

import momo.cn.edu.fjnu.androidutils.data.CommonValues;
import momo.cn.edu.fjnu.androidutils.utils.JsonUtils;

import android.R.integer;
import android.os.Environment;
import android.provider.SyncStateContract.Constants;
import android.text.TextUtils;
import android.util.Log;

import com.rockchips.mediacenter.basicutils.constant.Constant;
import com.rockchips.mediacenter.basicutils.util.HanziToPinyin;
import com.rockchips.mediacenter.basicutils.util.HanziToPinyin.Token;
import com.rockchips.mediacenter.basicutils.bean.LocalMediaInfo;
import com.rockchips.mediacenter.basicutils.bean.LocalDeviceInfo;
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
		String path = file.getPath();
		int dotIndex = path.lastIndexOf(".");
		if(dotIndex < 0)
			return ConstData.MediaType.UNKNOWN_TYPE;
		int startIndex = dotIndex + 1;
		if(startIndex >= path.length())
			return ConstData.MediaType.UNKNOWN_TYPE;
		String tailEx = path.substring(startIndex);
		if(Arrays.binarySearch(ConstData.AUDIO_SUFFIX, tailEx) >= 0){
			return ConstData.MediaType.AUDIO;
		}
		if(Arrays.binarySearch(ConstData.VIDEO_SUFFIX, tailEx) >= 0){
			return ConstData.MediaType.VIDEO;
		}
		if(Arrays.binarySearch(ConstData.SUBTITLE_SUFFIX, tailEx) >= 0){
			return ConstData.MediaType.SUBTITLE;
		}
		
		if(Arrays.binarySearch(ConstData.IMAGE_SUFFIX, tailEx) >= 0){
			return ConstData.MediaType.IMAGE;
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
				else if(mediaType == Constant.MediaType.FOLDER && getMediaTypeFromFile(childFile) != Constant.MediaType.UNKNOWN_TYPE)
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
    	}else if(mediaType == Constant.MediaType.FOLDER && getMediaTypeFromFile(file) != Constant.MediaType.UNKNOWN_TYPE)
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
    		if(mediaType == Constant.MediaType.IMAGE){
    			LocalMediaInfo localMediaInfo = getMediaInfoFromFile(rootDirFile, Constant.MediaType.IMAGE, deviceInfo);
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
    	//Log.i(TAG, "getMediaFileFromFile->file:" + file);
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
		localMediaInfo.setmFiles(getMediaFileCount(parentFile, Constant.MediaType.IMAGE));
		localMediaInfo.setmFileType(Constant.MediaType.IMAGEFOLDER);
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
    public static LocalDeviceInfo getDeviceInfoFromDevice(LocalDevice device){
    	LocalDeviceInfo deviceInfo = new LocalDeviceInfo();
    	deviceInfo.setTotalSize(device.getSize());
    	deviceInfo.setUsedSize(device.getUsed());
    	deviceInfo.setFreeSize(device.getFree());
    	deviceInfo.setUsedPercent(device.getUsed());
    	deviceInfo.setPhysicId(device.getPhysic_dev_id());
    	deviceInfo.setIsPhysicDev(1);
    	deviceInfo.setMountPath(device.getMountPath());
    	deviceInfo.setIsScanned(1);
    	deviceInfo.setmDevCount(1);
    	deviceInfo.setmDeviceId(0);
    	deviceInfo.setDeviceType(device.getDevices_type());
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
}
