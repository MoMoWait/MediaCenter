package com.rockchips.mediacenter.basicutils.constant;


/**
 * 
 * 提供Provider的相关常量
 * URI Code, Intent标识等常量
 * 
 * @author  l00174030
 * @version  [2013-1-9]
 */
public class LocDevProvConst
{
    // 认证标识
    public static final String AUTHORITY = "com.rockchips.mediacenter.localscan.provider";
    
    // 设备表对应的URL Code
    public static final int DEVICES_URI_CODE = 1;
    
    // 设备分区对应的URL Code
    public static final int DISK_URI_CODE = 2;
    
    // 文件夹查询URL Code
    public static final int FOLDER_URI_CODE = 3;
    
    // 文件查询URL Code
    public static final int SEARCH_URI_CODE = 4;
    
    // 目录删除URI Code
    public static final int DIR_DEL_URI_CODE = 5;
    
    // 文件删除URI Code
    public static final int FILE_DEL_URI_CODE = 6;
    
    // 查询文件夹下的所有媒体文件URI Code
    public static final int QUERY_DIR_FILE_URI_CODE = 7;
    
    // 查询文件夹下的所有媒体文件件数
    public static final int QUERY_FILE_CNT_CODE = 8;
    
    // 异步查询文件夹下的所有媒体文件信息
    public static final int QUERY_DIR_INFO_CODE = 9;
    
    // 查询文件夹下的所有媒体文件件数
    public static final int QUERY_DIR_PAGE_CODE = 10;
    
    // 图片播放器/音乐播放器使用：获取有媒体文件的目录名（扁平化显示）
    public static final int GET_FLAT_DIR_CODE = 11;
    
    // 图片播放器/音乐播放器使用 ：根据目录名，显示其内部相应的媒体文件
    public static final int GET_FLAT_DIR_FILE_CODE = 12;
    
    // 图片播放器/音乐播放器使用：获取有磁盘媒体文件的目录名（扁平化显示）
    public static final int GET_ALL_FLAT_DIR_CODE = 13;
    
    // 查询某个磁盘是否有媒体文件
    public static final int IS_EXIST_MEDIA_FILES_CODE = 14;
    
    // 文件共享URI
    public static final int FILE_SHARE_CODE = 15;
    
    // 文件夹查询URI
    public static final String FOLDER_URI = "Folders";
    
    // 文件夹查询URI
    public static final String FOLDER_CNT_URI = "FoldersCnt";
    
    // 异步查询文件夹下的文件数URI
    public static final String DIR_INFO_URI = "DirAsyCnt";
    
    // 异步查询文件夹下的文件数URI（目录下）
    public static final String DIR_PAGE_URI = "DirPageCnt";
    
    // 文件查询URI
    public static final String SEARCH_URI = "Search";
    
    // 设备查询URI
    public static final String DEVICES_URI = "Devices";
    
//    // 设备分区查询URI
    public static final String DISK_URI = "Disk";
    
    // 目录删除URI
    public static final String DIR_DEL_URI = "dir_del";
    
    // 文件删除URI
    public static final String FILE_DEL_URI = "file_del";
    
    // 查询文件夹下的所有媒体文件URI
    public static final String QUERY_DIR_FILE_URI = "query_dir_file";
    
    // 图片播放器/音乐播放器使用：获取有媒体文件的目录名（扁平化显示）
    public static final String GET_FLAT_DIR_URI = "get_flat_dir";
    
    // 图片播放器/音乐播放器使用 ：根据目录名，显示其内部相应的媒体文件
    public static final String GET_FLAT_DIR_FILE_URI = "get_flat_dir_file";
    
    // 图片播放器/音乐播放器使用：获取有磁盘媒体文件的目录名（扁平化显示）
    public static final String GET_ALL_FLAT_DIR_URI = "get_all_flat_dir";
    
    // 查询某个磁盘是否有媒体文件
    public static final String IS_EXIST_MEDIA_FILES_URI = "is_exist_media_files";
    
    //文件共享URI
    public static final String FILE_SHARE_URI = "file_share";
    
    // 分割符，尽量降低特殊字符的耦合（目录名，文件名）
    public static final String PROVIDER_SPLITER = "//";    
    
    // SQLlite中的特殊符号，需要'转义
    public static final char PERCENT_SYMBOL = '%';
}
