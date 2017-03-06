package com.rockchips.mediacenter.modle.db;
import java.util.List;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;
import com.rockchips.mediacenter.application.MediaCenterApplication;
import com.rockchips.mediacenter.bean.Device;
import com.rockchips.mediacenter.bean.FileInfo;
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
		} catch (DbException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取所有文件夹
	 * @param deviceID
	 * @param mediaType
	 * @param rootPath
	 * @return
	 */
	public List<FileInfo> getAllFolders(String deviceID, int mediaType, String rootPath){
		List<FileInfo> fileInfos = null;
		if(mediaType == ConstData.MediaType.AUDIOFOLDER){
			try {
				fileInfos = MediaCenterApplication.mDBManager.selector(FileInfo.class).where("musicCount", ">", 0).
				and("deviceID", "=", deviceID).orderBy("name", false).findAll();
			} catch (DbException e) {
				e.printStackTrace();
			}
		}else if(mediaType == ConstData.MediaType.VIDEOFOLDER){
			try {
				fileInfos = MediaCenterApplication.mDBManager.selector(FileInfo.class).where("videoCount", ">", 0).
				and("deviceID", "=", deviceID).orderBy("name", false).findAll();
			} catch (DbException e) {
				e.printStackTrace();
			}
		}else if(mediaType == ConstData.MediaType.IMAGEFOLDER){
			try {
				fileInfos = MediaCenterApplication.mDBManager.selector(FileInfo.class).where("imageCount", ">", 0).
				and("deviceID", "=", deviceID).orderBy("name", false).findAll();
			} catch (DbException e) {
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
			}
			
			fileInfos.remove(rootFileInfo);
			
			if(rootFileInfo != null){
				//获取子文件数目
				List<FileInfo> rootFileInfos = getFileInfos(deviceID, rootPath, MediaFileUtils.getFileTypeFromFolderType(mediaType));
				if(rootFileInfos != null && rootFileInfos.size() > 0)
					fileInfos.addAll(rootFileInfos);
			}
		}
		
		return fileInfos;
	}
	
	public List<FileInfo> getFileInfos(String deviceID, String parentPath, int mediaType){
		List<FileInfo> fileInfos = null;
		try {
			fileInfos = MediaCenterApplication.mDBManager.selector(FileInfo.class).where("type", "=", mediaType).
			and("deviceID", "=", deviceID).and("parentPath", "=", parentPath).orderBy("name", false).findAll();
		} catch (DbException e) {
			e.printStackTrace();
		}
		return fileInfos;
	
	}
	
	/**
	 * 查找Path对应的位置，二分搜索
	 * @param fileInfos
	 * @param path
	 * @return
	 */
	public int getPositionByPath(List<FileInfo> fileInfos, String path){
		int left = 0;
		int right = fileInfos.size() - 1;
		int mid = (left + right) / 2;
		
		return -1;
	}
}
