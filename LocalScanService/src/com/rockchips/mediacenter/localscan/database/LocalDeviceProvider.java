package com.rockchips.mediacenter.localscan.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.rockchips.mediacenter.basicutils.bean.LocalDeviceInfo;
import com.rockchips.mediacenter.basicutils.constant.Constant;
import com.rockchips.mediacenter.basicutils.constant.DeviceDataConst;
import com.rockchips.mediacenter.basicutils.constant.DiskScanConst;
import com.rockchips.mediacenter.basicutils.constant.LocDevProvConst;
import com.rockchips.mediacenter.basicutils.util.IICLOG;
import com.rockchips.mediacenter.basicutils.util.StringUtils;
import com.rockchips.mediacenter.localscan.database.table.TableDeviceManager;
import com.rockchips.mediacenter.localscan.database.table.TableFileAndFolderManager;
import com.rockchips.mediacenter.localscan.devicemgr.DeviceDataUtils;
import com.rockchips.mediacenter.localscan.devicemgr.DevicesMountPath;

/**
 * 
 * 用来提供外设设备的数据查询操作 mymedia在获取外设及分取的文件列表时使用该类
 * 
 * @author l00174030
 * @version [2013-1-9]
 */
public class LocalDeviceProvider extends ContentProvider
{
    // Provider向外提供的URI
    private static UriMatcher mStUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // 标签
    private static final String TAG = "LocalScanService";    

    private TableDeviceManager mTableDeviceManager;
    private TableFileAndFolderManager mTableFileAndFolderManager;
    
    private IICLOG mLog = IICLOG.getInstance();

    static
    {
    	Log.i(TAG, "static mStUriMatcher");
        // 注册设备表URI
        mStUriMatcher.addURI(LocDevProvConst.AUTHORITY, LocDevProvConst.DEVICES_URI, LocDevProvConst.DEVICES_URI_CODE);

        // 注册设备分区查询URI
        mStUriMatcher.addURI(LocDevProvConst.AUTHORITY, LocDevProvConst.DISK_URI, LocDevProvConst.DISK_URI_CODE);

        // 注册文件夹查询URI
        mStUriMatcher.addURI(LocDevProvConst.AUTHORITY, LocDevProvConst.FOLDER_URI, LocDevProvConst.FOLDER_URI_CODE);

        // 检索文件接口
        mStUriMatcher.addURI(LocDevProvConst.AUTHORITY, LocDevProvConst.SEARCH_URI, LocDevProvConst.SEARCH_URI_CODE);

        // 文件删除URI
        mStUriMatcher.addURI(LocDevProvConst.AUTHORITY, LocDevProvConst.FILE_DEL_URI, LocDevProvConst.FILE_DEL_URI_CODE);

        // 目录删除URI
        mStUriMatcher.addURI(LocDevProvConst.AUTHORITY, LocDevProvConst.DIR_DEL_URI, LocDevProvConst.DIR_DEL_URI_CODE);

        // 查询目录下的所有文件
        mStUriMatcher.addURI(LocDevProvConst.AUTHORITY, LocDevProvConst.QUERY_DIR_FILE_URI, LocDevProvConst.QUERY_DIR_FILE_URI_CODE);

        // 查询目录下的所有文件件数
        mStUriMatcher.addURI(LocDevProvConst.AUTHORITY, LocDevProvConst.FOLDER_CNT_URI, LocDevProvConst.QUERY_FILE_CNT_CODE);

        // 异步查询目录下的所有文件信息
        mStUriMatcher.addURI(LocDevProvConst.AUTHORITY, LocDevProvConst.DIR_INFO_URI, LocDevProvConst.QUERY_DIR_INFO_CODE);

        // 查询文件的总件数，分页使用
        mStUriMatcher.addURI(LocDevProvConst.AUTHORITY, LocDevProvConst.DIR_PAGE_URI, LocDevProvConst.QUERY_DIR_PAGE_CODE);

        // 图片播放器/音乐播放器使用：获取有媒体文件的目录名（扁平化显示）
        mStUriMatcher.addURI(LocDevProvConst.AUTHORITY, LocDevProvConst.GET_FLAT_DIR_URI, LocDevProvConst.GET_FLAT_DIR_CODE);

        // 图片播放器/音乐播放器使用 ：根据目录名，显示其内部相应的媒体文件
        mStUriMatcher.addURI(LocDevProvConst.AUTHORITY, LocDevProvConst.GET_FLAT_DIR_FILE_URI, LocDevProvConst.GET_FLAT_DIR_FILE_CODE);
        
        // 图片播放器/音乐播放器使用：获取有磁盘媒体文件的目录名（扁平化显示）
        mStUriMatcher.addURI(LocDevProvConst.AUTHORITY, LocDevProvConst.GET_ALL_FLAT_DIR_URI, LocDevProvConst.GET_ALL_FLAT_DIR_CODE);
        
        //是否存在媒体文件
        mStUriMatcher.addURI(LocDevProvConst.AUTHORITY, LocDevProvConst.IS_EXIST_MEDIA_FILES_URI, LocDevProvConst.IS_EXIST_MEDIA_FILES_CODE);
    }

    /**
     * provider的创建 在创建provider时，重置数据库，启动外设监控服务
     * 
     * @see [类、类#方法、类#成员]
     */
    @Override
    public boolean onCreate()
    {
    	Log.i(TAG, "onCreate");
        mLog.d(TAG, "LocalDeviceDBProvider.onCreate");
        mTableDeviceManager = TableDeviceManager.getInstance(getContext());
        mTableFileAndFolderManager = TableFileAndFolderManager.getInstance(getContext());
        return true;
    }

    /**
     * 根据URI返回对应的处理码
     * 
     * @see [类、类#方法、类#成员]
     */
    @Override
    public String getType(Uri uri)
    {
    	Log.i(TAG, "getType");
    	
        int action = 0;
        int match = mStUriMatcher.match(uri);

        switch (match)
        {
        // 查询设备
            case LocDevProvConst.DEVICES_URI_CODE:
                action = LocDevProvConst.DEVICES_URI_CODE;
                break;
            // 查询分区
            case LocDevProvConst.DISK_URI_CODE:
                action = LocDevProvConst.DISK_URI_CODE;
                break;
            // 查询文件
            case LocDevProvConst.SEARCH_URI_CODE:
                action = LocDevProvConst.SEARCH_URI_CODE;
                break;
            // 查询目录
            case LocDevProvConst.FOLDER_URI_CODE:
                action = LocDevProvConst.FOLDER_URI_CODE;
                break;
            // 文件删除URI
            case LocDevProvConst.FILE_DEL_URI_CODE:
                action = LocDevProvConst.FILE_DEL_URI_CODE;
                break;
            // 目录删除URI
            case LocDevProvConst.DIR_DEL_URI_CODE:
                action = LocDevProvConst.DIR_DEL_URI_CODE;
                break;
            // 查询目录下的所有文件
            case LocDevProvConst.QUERY_DIR_FILE_URI_CODE:
                action = LocDevProvConst.QUERY_DIR_FILE_URI_CODE;
                break;
            // 查询目录下的所有文件
            case LocDevProvConst.QUERY_FILE_CNT_CODE:
                action = LocDevProvConst.QUERY_FILE_CNT_CODE;
                break;
            // 异步查询目录下的所有文件信息
            case LocDevProvConst.QUERY_DIR_INFO_CODE:
                action = LocDevProvConst.QUERY_DIR_INFO_CODE;
                break;
            // 查询文件的总件数，分页使用
            case LocDevProvConst.QUERY_DIR_PAGE_CODE:
                action = LocDevProvConst.QUERY_DIR_PAGE_CODE;
                break;
            // 图片播放器/音乐播放器使用：获取有媒体文件的目录名（扁平化显示）
            case LocDevProvConst.GET_FLAT_DIR_CODE:
                action = LocDevProvConst.GET_FLAT_DIR_CODE;
                break;
            // 图片播放器/音乐播放器使用 ：根据目录名，显示其内部相应的媒体文件
            case LocDevProvConst.GET_FLAT_DIR_FILE_CODE:
                action = LocDevProvConst.GET_FLAT_DIR_FILE_CODE;
                break;
             // 图片播放器/音乐播放器使用：获取有磁盘媒体文件的目录名（扁平化显示）
            case LocDevProvConst.GET_ALL_FLAT_DIR_CODE:
                action = LocDevProvConst.GET_ALL_FLAT_DIR_CODE;
                break;
             // 图片播放器/音乐播放器使用：获取有磁盘媒体文件的目录名（扁平化显示）
            case LocDevProvConst.IS_EXIST_MEDIA_FILES_CODE:
                action = LocDevProvConst.IS_EXIST_MEDIA_FILES_CODE;
                break;
            default:
                break;
        }

        return String.valueOf(action);
    }

    /**
     * 根据URI对外提供的查询接口
     * 
     * @see [类、类#方法、类#成员]
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
    	Log.i(TAG, "query->uri:" + uri);
        // TODO 特殊字符的查询或插入 linux:% windows:' %
        // 获取查询动作
        int action = Integer.parseInt(getType(uri));

        Cursor cursor = null;
        switch (action)
        {
        // 查询设备
            case LocDevProvConst.DEVICES_URI_CODE:
                cursor = mTableDeviceManager.queryAllDevByIsPhysicDev(true);                
                break;

            // 查询分区
            case LocDevProvConst.DISK_URI_CODE:  
                String physicId;
                if (null == selectionArgs || 0 == selectionArgs.length)
                {
                    physicId = null;
                }
                else
                {
                    physicId = selectionArgs[0];
                }
                cursor = mTableDeviceManager.queryAllPartion(physicId, false);                
                break;

            // 查询文件
            case LocDevProvConst.FOLDER_URI_CODE:
                // TODO: 测试时间
                long start = System.currentTimeMillis();

                // 获取文件夹及文件类容
                cursor = getDevContent(selectionArgs, sortOrder);

                long end = System.currentTimeMillis();
                mLog.i(TAG, "query used start time = " + start + " end time = " + end + " total = " + (end - start));
                break;

            // 检索相关
            case LocDevProvConst.SEARCH_URI_CODE:
                // 搜索的检索处理
                cursor = getSearchContent(selectionArgs);
                break;

            // 查询目录下的所有文件
            case LocDevProvConst.QUERY_DIR_FILE_URI_CODE:
                if (isEmpty(selectionArgs))
                {
                    return null;
                }
                String tablePrefix = getTablePrefix(selectionArgs[0]);
                // 从路径中解析出表前缀
                if (StringUtils.isEmpty(tablePrefix))
                {
                    return null;
                }      
                int limit = 0;
                int offset = 0;
                if (selectionArgs.length >= 3)
                {
                    limit = Integer.parseInt(selectionArgs[1]);
                    offset = Integer.parseInt(selectionArgs[2]);
                }
                
                cursor = mTableFileAndFolderManager.queryAllFilsByPath(tablePrefix, selectionArgs[0], limit, offset, sortOrder);

                break;
            // 查询目录下的文件件数
            case LocDevProvConst.QUERY_FILE_CNT_CODE:
                cursor = getDevContentCnt(selectionArgs);
                break;
            // 异步查询目录下的所有文件信息
            case LocDevProvConst.QUERY_DIR_INFO_CODE:
                cursor = getDirInfoAsy(selectionArgs);
                break;
            // 查询文件的总件数，分页使用
            case LocDevProvConst.QUERY_DIR_PAGE_CODE:
                cursor = getDevContentPageCnt(selectionArgs);
                break;
            // 图片播放器/音乐播放器使用：获取有媒体文件的目录名（扁平化显示）
            case LocDevProvConst.GET_FLAT_DIR_CODE:
                cursor = getFlatDirWithType(selectionArgs, sortOrder);
                break;
            // 图片播放器/音乐播放器使用 ：根据目录名，显示其内部相应的媒体文件
            case LocDevProvConst.GET_FLAT_DIR_FILE_CODE:
                cursor = getFlatDirFileWithType(selectionArgs, sortOrder);
                break;
             // 图片播放器/音乐播放器使用：获取有磁盘媒体文件的目录名（扁平化显示）
            case LocDevProvConst.GET_ALL_FLAT_DIR_CODE:
                cursor = getAllFlatDirWithType(selectionArgs);                
             // 图片播放器/音乐播放器使用：获取有磁盘媒体文件的目录名（扁平化显示）
                break;
            case LocDevProvConst.IS_EXIST_MEDIA_FILES_CODE:
                cursor = getTopMediaFiles(selectionArgs);
                break;
            default:
                break;
        }
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values)
    {
        return null;
    }

    /**
     * 根据URI对外提供的删除接口
     * 
     * @see [类、类#方法、类#成员]
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {        
        // 获取参数
        if (selectionArgs == null)
        {
            mLog.e(TAG, "delete condition is null.");
            return 0;
        }

        // 从路径中解析出表前缀
        String tablePrefix = getTablePrefix(selectionArgs[0]);
        if (StringUtils.isEmpty(tablePrefix))
        {
            return 0;
        }
        // 获取查询动作
        int action = Integer.parseInt(getType(uri));
                
        switch (action)
        {
        // 文件删除URI
            case LocDevProvConst.FILE_DEL_URI_CODE:                
                mTableFileAndFolderManager.deleteFile(tablePrefix, selectionArgs[0]);                               
                break;
            // 目录删除URI
            case LocDevProvConst.DIR_DEL_URI_CODE:
                boolean isDelSelf = true;
                if (selection != null && selection.equals("false"))
                {
                    isDelSelf = false;
                }
                // 只删除媒体文件，不能删除目录
                mTableFileAndFolderManager.deleteFolder(tablePrefix, selectionArgs[0], isDelSelf);
                break;
            default:
                break;
        }

        return 0;       

    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        return 0;
    }

    /**
     * 获取文件夹及文件类容
     * 
     * @param params 参数path
                        type
     * @return 游标
     * @see [类、类#方法、类#成员]
     */
    private Cursor getDevContent(String[] params, String sortOrder)
    {
        Cursor cursor = null;

        // 获取目录路径
        String path = "";
        // 获取分类
        int category = 0;    

        // 获取目录路径
        if (isEmpty(params))
        {
            mLog.e(TAG, "get DeviceContent params is invaild ");
            return null;
        }
        
        if (params.length < 2)
        {
            mLog.e(TAG, "get DeviceContent params nums is invaild ");
            return null;
        }

        // 是否分页
        boolean isPage = false;
        String limit = null;
        String offset = null;
        if (params.length == 4)
        {
            isPage = true;
        }

        // 参数path
        path = params[0];
        // 媒体类型
        category = Integer.parseInt(params[1]);
        // 分页时
        if (isPage)
        {
            limit = params[2];
            offset = params[3];            
        }

        // 从路径中解析出表前缀
        String tablePrefix = getTablePrefix(path);
        if (StringUtils.isEmpty(tablePrefix))
        {
            return null;
        }       
        // 获取对应的数据
        switch (category)
        {
        // 取视频类数据
            case Constant.MediaType.VIDEO:
                // 取音频类数据
            case Constant.MediaType.AUDIO:
                // 取图片类数据
            case Constant.MediaType.IMAGE:
                String type = String.valueOf(category);
                String[] param = null;
                if (isPage)
                {
                    param = new String[]
                    { path, path, type, limit, offset };
                }
                else
                {
                    param = new String[]
                    { path, path, type };
                }
                cursor = mTableFileAndFolderManager.queryMediaFiles(tablePrefix, isPage, param, sortOrder);                                
                break;
            // 获取所有的数据
            default:
                String[] paramAll = null;
                if (isPage)
                {
                    paramAll = new String[]
                    { path, path, limit, offset };
                }
                else
                {
                    paramAll = new String[]
                    { path, path };
                }
                cursor = mTableFileAndFolderManager.queryAllFiles(tablePrefix, isPage, paramAll, sortOrder);                
                break;
        }

        return cursor;
    }

    /**
     * 异步获取文件夹及文件类容件数
     * 
     * @param selection 参数path//type
     * @return 游标
     * @see [类、类#方法、类#成员]
     */
    private Cursor getDirInfoAsy(String[] params)
    {
        Cursor cursor = null;

        // 获取目录路径
        String path = "";
        // 获取分类
        int category = 0;      

        // 获取目录路径
        if (isEmpty(params))
        {
            mLog.e(TAG, "get getDirInfoAsy params is invaild ");
            return null;
        }
        
        if (params.length < 2)
        {
            mLog.e(TAG, "get getDirInfoAsy params num is invaild ");
            return null;
        }

        // 参数path
        path = params[0];
        // 媒体类型
        category = Integer.parseInt(params[1]);

        // 从路径中解析出表前缀
        String tablePrefix = getTablePrefix(path);
        if (StringUtils.isEmpty(tablePrefix))
        {
            return null;
        }

        // 构造检索件数的SQL语句      
        mLog.i(TAG, "getDirInfoAsy table = " + tablePrefix + " type =" + category + " path = " + path);
        cursor = mTableFileAndFolderManager.queryStaticsInfo(tablePrefix, path, category);

        return cursor;
    }

    /**
     * 获取文件夹及文件类容件数（CNT）
     * 
     * @param selection 参数path//type
     * @return 游标
     * @see [类、类#方法、类#成员]
     */
    private Cursor getDevContentCnt(String[] params)
    {
        Cursor cursor = null;

        // 获取目录路径
        String path = "";              

        // 获取目录路径
        if (isEmpty(params))
        {
            mLog.e(TAG, "get getDevContentCnt params is invaild ");
            return null;
        }
        
//        if (params.length != 2)
//        {
//            mLog.e(TAG, "get getDevContentCnt params nums is invaild ");
//            return null;
//        }
//
//        // 参数path
//        path = params[0];
//        // 媒体类型
//        category = Integer.parseInt(params[1]);
//
//        // 从路径中解析出表前缀
//        String tablePrefix = getTablePrefix(path);
//        if (StringUtils.isEmpty(tablePrefix))
//        {
//            return null;
//        }
//
//        mLog.i(TAG, "getDevContentCnt table = " + tablePrefix + " type =" + category);
//
//        // 获取对应的数据
//        switch (category)
//        {
//        // 取视频类数据
//            case Constant.MediaType.VIDEO:
//                // 取音频类数据
//            case Constant.MediaType.AUDIO:
//                // 取图片类数据
//            case Constant.MediaType.IMAGE:                
//                cursor = mTableFileAndFolderManager.getMediaCount(tablePrefix, category);
//                break;
//            // 获取所有的数据
//            default:                
//                cursor = mTableFileAndFolderManager.getAllCount(tablePrefix);
//                break;
//        }

        return cursor;
    }

    /**
     * 从路径中解析出表前缀
     * 
     * @param path 路径
     * @return 表前缀
     * @see [类、类#方法、类#成员]
     */
    private String getTablePrefix(String path)
    {
        // 判断文件路径是否合法
        if (path == null || path.trim().length() == 0)
        {
            mLog.e(TAG, "get table prefix is invalid " + path);
            return null;
        }
        
        String mountPath = DevicesMountPath.getMountPath(path);  
        if (mountPath == null)
        {
            mLog.e(TAG, "get current mount path null!");
            return null;
        }
        return DeviceDataUtils.createTableName(mountPath);
    }
    

    /**
     * 语音搜索的检索处理
     * 
     * @param selection 检索关键字
     * @return
     * @see [类、类#方法、类#成员]
     */
    private Cursor getSearchContent(String[] selectionArgs)
    {
        Cursor cursor = null;

        // 检索关键字合法性判断
        if (selectionArgs == null || selectionArgs.length == 0)
        {
            mLog.e(TAG, "Search interface parameter is null");
            return null;
        }
        
        List<String> tablePrefixList = new ArrayList<String>();
        String keyWord;
        //参数为1，默认查询所有磁盘
        if (1 == selectionArgs.length)
        {
            keyWord = selectionArgs[0];
            //获取所有设备的mountPath
            cursor = mTableDeviceManager.queryAllPartion(null, false);            
        }
        //参数大于2，查询指定磁盘
        else
        {
            String physicId = selectionArgs[0];
            keyWord = selectionArgs[1];
            //获取指定设备的mountPath
            cursor = mTableDeviceManager.queryAllPartion(physicId, false);
        }
        
        if (null == cursor)
        {
            return null;
        }
        // 遍历检索数据
        while (cursor.moveToNext())
        {
            // 挂载路径
            String path = cursor.getString(cursor.getColumnIndex(LocalDeviceInfo.MOUNT_PATH));                
            // 存在的表的前缀
            String tablePrefix = getTablePrefix(path);                
            tablePrefixList.add(tablePrefix);
        }
        cursor.close();
        cursor = null;
        
        return mTableFileAndFolderManager.searchMediaFiles(tablePrefixList, keyWord); 
    }

    /**
     * 获取文件夹及文件类容(件数)
     * 
     * @param selection 参数path//type
     * @return 游标
     * @see [类、类#方法、类#成员]
     */
    private Cursor getDevContentPageCnt(String[] params)
    {
        Cursor cursor = null;

        // 获取目录路径
        String path = "";
        // 获取分类
        int category = 0;        

        // 获取目录路径
        if (isEmpty(params))
        {
            mLog.e(TAG, "get DeviceContent params is invaild ");
            return null;
        }
        
        if (params.length < 2)
        {
            mLog.e(TAG, "get DeviceContent params num is invaild ");
            return null;
        }

        // 参数path
        path = params[0];
        // 媒体类型
        category = Integer.parseInt(params[1]);

        // 从路径中解析出表前缀
        String tablePrefix = getTablePrefix(path);
        if (StringUtils.isEmpty(tablePrefix))
        {
            return null;
        }

        // 获取对应的数据
        switch (category)
        {
        // 取视频类数据
            case Constant.MediaType.VIDEO:
                // 取音频类数据
            case Constant.MediaType.AUDIO:
                // 取图片类数据
            case Constant.MediaType.IMAGE:
                String type = String.valueOf(category);
                String[] param = new String[]
                { path, path, type };
                cursor = mTableFileAndFolderManager.queryMediaFilesCount(tablePrefix, param);
                break;
            // 获取所有的数据
            default:
                String[] paramAll = new String[]
                { path, path };
                cursor = mTableFileAndFolderManager.queryAllFilesCount(tablePrefix, paramAll);                
                break;
        }

        return cursor;
    }

    /**
     * 图片播放器/音乐播放器使用：获取有媒体文件的目录（扁平化显示）
     * 
     * @param selection 参数DevID//type
     * @return 游标
     * @see [类、类#方法、类#成员]
     */
    private Cursor getFlatDirWithType(String[] params, String sortOrder)
    {
        // 获取目录路径
        String mountPath = "";
        // 获取分类
        int category = 0;
        
        // 获取目录路径
        if (isEmpty(params))
        {
            mLog.e(TAG, "get getFlatDirWithType params is invaild ");
            return null;
        }

        if (params.length < 2)
        {
            mLog.e(TAG, "get getFlatDirWithType params num is invaild ");
            return null;
        }

        // 参数外设标识
        mountPath = params[0];
        // 媒体类型
        category = Integer.parseInt(params[1]);
        
        int limit = 0;
        int offset = 0;
        if (params.length == 4)
        {
            limit = Integer.parseInt(params[2]);
            offset = Integer.parseInt(params[3]);
        }

        mLog.i(TAG, "cc msg devId == " + mountPath);
        String tablePrefix = getTablePrefix(mountPath);
        if (StringUtils.isEmpty(tablePrefix))
        {            
            return null;
        }
        return mTableFileAndFolderManager.queryFlatFilesWithType(tablePrefix, category, limit, offset, sortOrder);
    }
    
    /**
     * 图片播放器/音乐播放器使用：获取有磁盘媒体文件的目录（扁平化显示）
     * 
     * @param selection 参数DevID//type
     * @return 游标
     * @see [类、类#方法、类#成员]
     */
    private Cursor getAllFlatDirWithType(String[] params)
    {
        // 获取目录路径
        if (isEmpty(params))
        {
            mLog.e(TAG, "cc msg get getAllFlatDirWithType params is invaild ");
            return null;
        }
        
        int type = Integer.parseInt(params[1]);
        mLog.d(TAG, "cc msg get getAllFlatDirWithType type = " + type);
        //获取所有磁盘的mountPath
        Cursor cursor = mTableDeviceManager.queryAllPartion(null, false);
        if (null == cursor)
        {
            mLog.e(TAG, "cc msg queryAllPartion cursor null! ");
            return null;
        }
        
        List<String> mountPathList = new ArrayList<String>();
        while (cursor.moveToNext())
        {
            String mountPath = cursor.getString(cursor.getColumnIndex(LocalDeviceInfo.MOUNT_PATH));
            mountPathList.add(getTablePrefix(mountPath));
        }
        cursor.close();
        
        String[] tablePrefixList = new String[mountPathList.size()];
        return mTableFileAndFolderManager.queryAllPartionFlatFolders(mountPathList.toArray(tablePrefixList), type);
    }
    
    /**
     * 获取指定磁盘是否有指定的媒体文件
     * 
     * @param selection 参数DevID//type
     * @return 游标
     * @see [类、类#方法、类#成员]
     */
    private Cursor getTopMediaFiles(String[] params)
    {
        // 获取目录路径
        if (isEmpty(params) || params.length < 2)
        {
            mLog.e(TAG, "get getAllFlatDirWithType params is invaild ");
            return null;
        }
        
        String mountPath = params[0];
        int type = Integer.parseInt(params[1]);  
        
        // 从路径中解析出表前缀
        String tablePrefix = getTablePrefix(mountPath);
        if (StringUtils.isEmpty(tablePrefix))
        {
            return null;
        }
        
        return mTableFileAndFolderManager.getTopMediaFiles(tablePrefix, mountPath, type);
    }

    /**
     * 图片播放器/音乐播放器使用：获取有媒体文件的目录下对应的文件（扁平化显示）
     * 
     * @param selection 参数path//type
     * @return 游标
     * @see [类、类#方法、类#成员]
     */
    private Cursor getFlatDirFileWithType(String[] params, String sortOrder)
    {
        Cursor cursor = null;

        // 获取目录路径
        String path = "";
        
        // 获取目录路径
        if (isEmpty(params))
        {
            mLog.e(TAG, "get getFlatDirWithType params is invaild ");
            return null;
        }
        
        if (params.length < 2)
        {
            mLog.e(TAG, "get getFlatDirWithType params num is invaild ");
            return null;
        }

        // 参数path
        path = params[0];

        // 从路径中解析出表前缀
        String tablePrefix = getTablePrefix(path);
        if (StringUtils.isEmpty(tablePrefix))
        {
            return null;
        }
        int limit = 0;
        int offset = 0;
        if (params.length >= 4)
        {
            limit = Integer.parseInt(params[2]);
            offset = Integer.parseInt(params[3]);
        }

        cursor = mTableFileAndFolderManager.queryFlatMediaFiles(tablePrefix, params[1], path, limit, offset, sortOrder);

        return cursor;
    }
    
    private boolean isEmpty(String[] params)
    {
        if (params == null || params.length == 0)
        {
            return true;
        }
        return false;
    }
}
