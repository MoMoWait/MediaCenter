package com.rockchips.mediacenter.modle.db;
import java.util.ArrayList;
import java.util.List;
import org.xutils.db.sqlite.WhereBuilder;
import com.rockchips.mediacenter.application.MediaCenterApplication;
import com.rockchips.mediacenter.bean.Device;
import com.rockchips.mediacenter.bean.FileInfo;
import com.rockchips.mediacenter.bean.LocalDevice;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.utils.MediaFileUtils;

/**
 * @author GaoFei
 * 文件描述信息操作
 */
public class FileInfoService extends AppBeanService<FileInfo> {

	@Override
	public boolean isExist(FileInfo object) {
		return false;
	}

	/**
	 * 根据设备ID删除文件信息
	 * @param deviceID
	 */
	public void deleteFileInfosByDeviceID(String deviceID){
		try {
			MediaCenterApplication.mDBManager.delete(Device.class, WhereBuilder.b("deviceID", "=", deviceID));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 根据设备ID获取文件信息
	 * @param deviceID
	 * @return
	 */
	public List<FileInfo> getFileInfosByDeviceID(String deviceID){
		List<FileInfo> fileInfos = null;
		try {
			fileInfos = MediaCenterApplication.mDBManager.selector(FileInfo.class).where("deviceID", "=", deviceID).findAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fileInfos;
	}
	
	/**
	 * 根据设备ID和文件路径获取文件信息
	 * @param deviceID
	 * @return
	 */
	public FileInfo getFileInfosByDeviceIDAndPath(String deviceID, String path){
		FileInfo fileInfo = null;
		try {
			fileInfo = MediaCenterApplication.mDBManager.selector(FileInfo.class).
					where(WhereBuilder.b("deviceID", "=", deviceID).and("path", "=", path)).findFirst();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fileInfo;
	}
	
	/**
	 * 获取所有文件夹
	 * @param deviceID
	 * @param mediaType
	 * @param rootPath
	 * @return
	 */
	public List<FileInfo> getAllFolders(String deviceID, int mediaType, String rootPath){
		List<FileInfo> fileInfos = new ArrayList<FileInfo>();
		if(mediaType == ConstData.MediaType.AUDIOFOLDER){
			try {
				fileInfos = MediaCenterApplication.mDBManager.selector(FileInfo.class).where("musicCount", ">", 0).
				and("deviceID", "=", deviceID).orderBy("name", false).findAll();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else if(mediaType == ConstData.MediaType.VIDEOFOLDER){
			try {
				fileInfos = MediaCenterApplication.mDBManager.selector(FileInfo.class).where("videoCount", ">", 0).
				and("deviceID", "=", deviceID).orderBy("name", false).findAll();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else if(mediaType == ConstData.MediaType.IMAGEFOLDER){
			try {
				fileInfos = MediaCenterApplication.mDBManager.selector(FileInfo.class).where("imageCount", ">", 0).
				and("deviceID", "=", deviceID).orderBy("name", false).findAll();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		FileInfo rootFileInfo = null;
		if(mediaType == ConstData.MediaType.AUDIOFOLDER || mediaType == ConstData.MediaType.VIDEOFOLDER){
			if(fileInfos != null && fileInfos.size() > 0){
				for(FileInfo itemFileInfo : fileInfos){
					if(rootPath.equals(itemFileInfo.getPath())){
						rootFileInfo = itemFileInfo;
						break;
					}
				}
				
				if(rootFileInfo != null){
					fileInfos.remove(rootFileInfo);
					List<FileInfo> rootFileInfos = getFileInfos(deviceID, rootPath, MediaFileUtils.getFileTypeFromFolderType(mediaType));
					if(rootFileInfos != null && rootFileInfos.size() > 0)
						fileInfos.addAll(rootFileInfos);
				}
					
			}
			
		}
		
		return fileInfos;
	}
	
	public List<FileInfo> getFileInfos(String deviceID, String parentPath, int mediaType){
		List<FileInfo> fileInfos = null;
		try {
			fileInfos = MediaCenterApplication.mDBManager.selector(FileInfo.class).where("type", "=", mediaType).
			and("deviceID", "=", deviceID).and("parentPath", "=", parentPath).orderBy("name", false).findAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fileInfos;
	
	}
	
	public List<FileInfo> getFileInfos(String deviceID, int mediaType){
		List<FileInfo> fileInfos = null;
		try {
			fileInfos = MediaCenterApplication.mDBManager.selector(FileInfo.class).where("type", "=", mediaType).
			and("deviceID", "=", deviceID).orderBy("name", false).findAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fileInfos;
	
	}
	
	public List<FileInfo> getLocalFileInfos(int mediaType){
		List<FileInfo> fileInfos = new ArrayList<>();
		try {
			List<Device> allLocalDevices = new DeviceService().getAllLocalDevices();
			if(allLocalDevices != null && allLocalDevices.size() > 0){
				
				for(Device itemDevice : allLocalDevices){
					List<FileInfo> itemFileInfos = MediaCenterApplication.mDBManager.selector(FileInfo.class).where("type", "=", mediaType).
							and("deviceID", "=", itemDevice.getDeviceID()).orderBy("name", false).findAll();
					fileInfos.addAll(itemFileInfos);
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fileInfos;
	
	}
	
	/**
	 * 删除deviceID下包含includePath的数据
	 * @param deviceID
	 * @param includePath
	 */
	public void deleteFileInfos(String deviceID, String includePath){
		try {
			MediaCenterApplication.mDBManager.delete(FileInfo.class, WhereBuilder.b("deviceID", "=", deviceID)
					.and("path", "like", includePath + "%"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 删除所有数据
	 */
	public void deleteAll(){
		try {
			MediaCenterApplication.mDBManager.delete(FileInfo.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
