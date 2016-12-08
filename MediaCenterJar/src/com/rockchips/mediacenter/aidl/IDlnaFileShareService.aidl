package com.rockchips.mediacenter.aidl;
import java.util.List;

interface IDlnaFileShareService
{
    //获取当前文件的共享状态
    int getFolderShareStatus(String folderName, String mountPath);
	//判断父文件夹是否处于共享状态
	boolean isParentShared(String filePath, String mountPath);
	//取消共享文件列表
	boolean cancelSharedFile(in List<String> filePathList, String mountPath);	
	//删除共享文件列表
    boolean deleteSharedFile(in List<String> filePathList, boolean isFolder, String mountPath);
    //删除共享文件夹下所有列表
    boolean deleteAllSharedFiles(String folderPath, String mountPath);
	//添加共享文件列表
	boolean addSharedFile(in List<String> filePathList, boolean isFolder, String mountPath);
}