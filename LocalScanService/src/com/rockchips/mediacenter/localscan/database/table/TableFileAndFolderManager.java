/**
 * Title: TableFileAndFolderManager.java<br>
 * Package: com.rockchips.mediacenter.localscan.database.table<br>
 * Description: TODO<br>
 * @author c00224451
 * @version v1.0<br>
 * Date: 2014-7-4下午2:09:49<br> 
 * Copyright © Huawei Technologies Co., Ltd. 2014. All rights reserved.
 */

package com.rockchips.mediacenter.localscan.database.table;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.rockchips.mediacenter.basicutils.bean.LocalMediaInfo;
import com.rockchips.mediacenter.basicutils.constant.Constant;
import com.rockchips.mediacenter.basicutils.util.IICLOG;
import com.rockchips.mediacenter.basicutils.util.StringUtils;
import com.rockchips.mediacenter.localscan.database.LocalDBHelper;
import com.rockchips.mediacenter.localscan.devicemgr.DeviceDataUtils;

/**
 * Description: 用来管理文件表以及目录表的入库，查询，创建，删除<br>
 * @author c00224451
 * @version v1.0
 * Date: 2014-7-4 下午2:09:49<br>
 */

public final class TableFileAndFolderManager
{
    private static final String TAG = "LocalScanService";
    private IICLOG mLog = IICLOG.getInstance();
    private Context mContext;    
    
    public static final String TABLE_FOLDERS_SUFFIX = "_Folders";
    public static final String TABLE_FILES_SUFFIX = "_Files";
    
    // 删除文件表
    public static final String DROP_TABLE_PREFIX = "drop table ";    
    
    // 清空数据表
    public static final String CLEAR_TABLE_PREFIX = "delete from "; 
    // 插入文件目录表语句
    public static final String INSERT_PREFIX = "insert into ";
        
    public static final String CREATE_SQL_PREFIX = "CREATE TABLE IF NOT EXISTS ";
    
    public static final String CREATE_FOLDER_SQL_SUFFIX = TABLE_FOLDERS_SUFFIX + "("
            // 文件夹名称
            + LocalMediaInfo.FILE_NAME + " text, "
            // 文件夹所在路径
            + LocalMediaInfo.PARENT_PATH + " text, "
            // 该文件夹中子文件夹数
            + LocalMediaInfo.FOLDERS + " integer default 0,"
            // 该文件夹中媒体文件数目
            + LocalMediaInfo.FILES + " integer default 0, "
            + LocalMediaInfo.MODIFY_DATE + " integer default 0, "
            + LocalMediaInfo.PIN_YIN + " text,"
            + LocalMediaInfo.DEVICETYPE + " text,"
            + LocalMediaInfo.PHYSIC_ID + " text,"
            + "PRIMARY KEY (" + LocalMediaInfo.FILE_NAME + "," + LocalMediaInfo.PARENT_PATH + "));";
    
    // 创建文件对应的表结构
    public static final String CREATE_FILE_SQL_SUFFIX = TABLE_FILES_SUFFIX + "("
        // 文件名
        + LocalMediaInfo.FILE_NAME + " text, "
        // 文件所在的目录
        + LocalMediaInfo.PARENT_PATH + " text, "
        // 文件类型：1：视频， 2：音频， 3：图片
        + LocalMediaInfo.FILE_TYPE + " integer default 0,"
        // 文件最后修改时间 时间格式为：yyyymmdd
        + LocalMediaInfo.MODIFY_DATE + " integer default 0, "        
        // 文件大小
        + LocalMediaInfo.FILE_SIZE + " integer default 0,"
        + LocalMediaInfo.PIN_YIN + " text,"
        + LocalMediaInfo.DEVICETYPE + " text,"
        + LocalMediaInfo.PHYSIC_ID + " text,"
        + "PRIMARY KEY (" + LocalMediaInfo.FILE_NAME + "," + LocalMediaInfo.PARENT_PATH + "));";
    
    // 索引
    public static final String INDEX_PREFIX = "CREATE INDEX ";
    
    // 文件表索引
    public static final String FILE_INDEX = TABLE_FILES_SUFFIX + "(" + LocalMediaInfo.PARENT_PATH + "," + LocalMediaInfo.FILE_TYPE + ");";
    public static final String FILE_MIDDLE = "files_index on ";
    
    // 文件夹索引
    public static final String FOLDER_INDEX = TABLE_FOLDERS_SUFFIX + "(" + LocalMediaInfo.PARENT_PATH + ");";
    public static final String FOLDER_MIDDLE = "folders_index on ";
    
    private static final String FILES_QUERY_PROJECTION   = LocalMediaInfo.FILE_NAME + ", "
                                                         + LocalMediaInfo.PARENT_PATH + ", "
                                                         + LocalMediaInfo.PARENT_PATH + "||'/'||" + LocalMediaInfo.FILE_NAME + " as " + LocalMediaInfo.DATA + ","
                                                         + "0 as " + LocalMediaInfo.FOLDERS + ", "
                                                         + "0 as " + LocalMediaInfo.FILES + ", "
                                                         + LocalMediaInfo.FILE_TYPE + ", "
                                                         + LocalMediaInfo.FILE_SIZE + ", "
                                                         + "1 as " + LocalMediaInfo.ORDER_FIELD + ", "
                                                         + LocalMediaInfo.MODIFY_DATE + ", "
                                                         + LocalMediaInfo.DEVICETYPE + ", "
                                                         + LocalMediaInfo.PHYSIC_ID + ", "
                                                         + LocalMediaInfo.PIN_YIN;
    
    private static final String FOLDERS_QUERY_PROJECTION = LocalMediaInfo.FILE_NAME + ", "
                                                         + LocalMediaInfo.PARENT_PATH + ", "
                                                         + LocalMediaInfo.PARENT_PATH + "||'/'||" + LocalMediaInfo.FILE_NAME + " as " + LocalMediaInfo.DATA + ","
                                                         + "0 as " + LocalMediaInfo.FOLDERS + ", "
                                                         + "0 as " + LocalMediaInfo.FILES + ", "
                                                         + "2 as " + LocalMediaInfo.FILE_TYPE + ", "
                                                         + "0 as " + LocalMediaInfo.FILE_SIZE + ", "
                                                         + "0 as " + LocalMediaInfo.ORDER_FIELD + ", "
                                                         + LocalMediaInfo.MODIFY_DATE + ", "
                                                         + LocalMediaInfo.DEVICETYPE + ", "
                                                         + LocalMediaInfo.PHYSIC_ID + ", "
                                                         + LocalMediaInfo.PIN_YIN;
    
    private static TableFileAndFolderManager mStTableFileAndFolderManager;
    private LocalDBHelper mLocalDBHelper;
    public static TableFileAndFolderManager getInstance(Context context)
    {
        if (null == mStTableFileAndFolderManager)
        {
            mStTableFileAndFolderManager = new TableFileAndFolderManager(context);
        }
        return mStTableFileAndFolderManager;
    }
    private TableFileAndFolderManager(Context context)
    {
        mContext = context;        
        mLocalDBHelper = LocalDBHelper.getInstance(context);
    }
    
    
    
    /**
     * 创建文件表，目录表
     * 根据分区路径构造表名
     * 
     * @param path 路径
     * @see [类、类#方法、类#成员]
     */
    public boolean createFileAndDirTable(String path)
    {
        mLog.i(TAG, "create file and dir table " + path);
        
        // 根据分区路径生成表名
        String table = DeviceDataUtils.createTableName(path);
        
        if (isTableFileExist(table))
        {
            dropFileAndDirTable(path);
        }
        List<String> sqls = new ArrayList<String>();
        // 目录表
        sqls.add(CREATE_SQL_PREFIX + table + CREATE_FOLDER_SQL_SUFFIX);
        // 文件表
        sqls.add(CREATE_SQL_PREFIX + table + CREATE_FILE_SQL_SUFFIX);
        
        // 创建索引 目录
        sqls.add(INDEX_PREFIX + table + FOLDER_MIDDLE + table + FOLDER_INDEX);
        
        // 创建索引 文件
        sqls.add(INDEX_PREFIX + table + FILE_MIDDLE + table + FILE_INDEX);
        
        // 根据分区创建目录表
        if (!mLocalDBHelper.batchExecSQL(sqls))        
        {
            return false;
        }
        
        return true;
    }
    
    private String getTableFilesName(String tablePrefix)
    {
        return tablePrefix + TABLE_FILES_SUFFIX;
    }
    
    private String getTableFoldersName(String tablePrefix)
    {
        return tablePrefix + TABLE_FOLDERS_SUFFIX;
    }
    
    /**
     * 创建文件表，目录表
     * 根据分区路径构造表名
     * 
     * @param path 路径
     * @see [类、类#方法、类#成员]
     */
    private void dropFileAndDirTable(String path)
    {
        mLog.i(TAG, "drop file and dir table " + path);
        
        // 根据分区路径生成表名
        String table = DeviceDataUtils.createTableName(path);
        
        List<String> sqls = new ArrayList<String>();
        // 目录表
        sqls.add(DROP_TABLE_PREFIX + getTableFilesName(table));
        // 文件表
        sqls.add(DROP_TABLE_PREFIX + getTableFoldersName(table));
        
        // 根据分区创建目录表
        mLocalDBHelper.batchExecSQL(sqls);
    }
    
    public void clearFileAndDirTable(String path)
    {
        mLog.i(TAG, "clear file and dir table " + path);
        
        // 根据分区路径生成表名
        String table = DeviceDataUtils.createTableName(path);
        
        List<String> sqls = new ArrayList<String>();
        // 目录表
        sqls.add(CLEAR_TABLE_PREFIX + getTableFilesName(table));
        // 文件表
        sqls.add(CLEAR_TABLE_PREFIX + getTableFoldersName(table));
        
        // 根据分区创建目录表
        mLocalDBHelper.batchExecSQL(sqls);
    }
   
    /**
     * 文件表的插入功能
     * 
     * @param files 文件记录
     * @param path 分区路径
     * @see [类、类#方法、类#成员]
     */
    public void insertFile(List<LocalMediaInfo> files, String path)
    {        
        // 空值判断
        if (files == null || 0 == files.size())
        {
            mLog.e(TAG, "insert file table cnt = null ");
            return;
        }
        
        mLog.i(TAG, "insert file table cnt = " + files.size());
        
        // 根据分区路径生成表名
        String table = DeviceDataUtils.createTableName(path);
        String tableName = getTableFilesName(table);         
        List<ContentValues> valueList = new ArrayList<ContentValues>();
        for (int i = 0; i < files.size(); ++i)
        {
            ContentValues values = getFileContentValue(files.get(i));
            valueList.add(values);
        }
        mLocalDBHelper.insert(tableName, valueList);
    }
    
    /**
     * 目录表的插入功能
     * 
     * @param files 目录记录
     * @param path 分区路径
     * @see [类、类#方法、类#成员]
     */
    public void insertDir(List<LocalMediaInfo> dirs, String path)
    {
        // 空值判断
        if (dirs == null || 0 == dirs.size())
        {
            mLog.e(TAG, "insert dir table cnt = null ");
            return;
        }
        
        mLog.i(TAG, "insert dir table cnt = " + dirs.size());
        
        // 根据分区路径生成表名
        String table = DeviceDataUtils.createTableName(path);
        String tableName = getTableFoldersName(table);
                
        List<ContentValues> valueList = new ArrayList<ContentValues>();
        for (int i = 0; i < dirs.size(); ++i)
        {
            ContentValues values = getFolderContentValue(dirs.get(i));
            valueList.add(values);
        }
        
        mLocalDBHelper.insert(tableName, valueList);
    }
    
    public Cursor queryNotDistinct(String tableName, String[] columns, String selection, String[] selectionArgs, String orderBy)
    {
        return mLocalDBHelper.queryNotDistinct(tableName, columns, selection, selectionArgs, orderBy);
    }
    
    public Cursor queryNotDistinct(String tableName, String[] columns, String selection, String[] selectionArgs, String orderBy, String limit)
    {
        return mLocalDBHelper.queryNotDistinct(tableName, columns, selection, selectionArgs, orderBy, limit);
    }
    
    private int delete(String tableName, String whereClause, String[] whereArgs)
    {
        return mLocalDBHelper.delete(tableName, whereClause, whereArgs);
    }
    
    public int deleteFile(String tablePrefix, String path)
    {        
        String whereClause = LocalMediaInfo.FILE_NAME   + " = ? " + " and "
                           + LocalMediaInfo.PARENT_PATH + " = ? ";
        //解析出文件父目录以及文件名称
        String fileName = getFileName(path);
        String parentPath = getParentPath(path);
        String[] wherArg = {fileName, parentPath};
        return delete(getTableFilesName(tablePrefix), whereClause, wherArg);
    }
    
    public void deleteFolder(String tablePrefix, String path, boolean isDelSelf)
    {
        String whereClause;
        // 删除指定文件夹下的媒体文件，不能删除目录
        whereClause = LocalMediaInfo.PARENT_PATH + " = ? " + " or "
                    + LocalMediaInfo.PARENT_PATH + " like ? " + " || '/%'";
        String[] args = {path, path};
        delete(getTableFilesName(tablePrefix), whereClause, args);    
        //删除指定文件夹下的所有文件夹
        whereClause = LocalMediaInfo.PARENT_PATH + " = ? " + " or "
                    + LocalMediaInfo.PARENT_PATH + " like ? " + " || '/%'";                
        delete(getTableFoldersName(tablePrefix), whereClause, args);
        if (isDelSelf)
        {
            //删除指定的文件夹，该文件夹本身        
            String fileName = getFileName(path);
            String parentPath = getParentPath(path);
            whereClause = LocalMediaInfo.FILE_NAME + " = ? " + " and "
                        + LocalMediaInfo.PARENT_PATH + " = ? ";
            String[] argsName = {fileName, parentPath};
            delete(getTableFoldersName(tablePrefix), whereClause, argsName);
        }
    }
    
    private String queryFlatFilesWithTypeSql(String tablePrefix, int type)
    {
        StringBuffer sqlBuf = new StringBuffer();
        String sql = null;
        
        String foldersTable = getTableFoldersName(tablePrefix);
        String filesTable = getTableFilesName(tablePrefix);
        
        sqlBuf.append(" select "   + foldersTable + "." + LocalMediaInfo.PARENT_PATH + " as " + LocalMediaInfo.PARENT_PATH + "," 
                                   + foldersTable + "." + LocalMediaInfo.FILE_NAME + " as " + LocalMediaInfo.FILE_NAME + "," 
                                   + "count(" + filesTable + "." + LocalMediaInfo.PARENT_PATH + ") as " + LocalMediaInfo.FILES + "," 
                                   + foldersTable + "." + LocalMediaInfo.PARENT_PATH + "||'/'||" + foldersTable + "." + LocalMediaInfo.FILE_NAME 
                                   + " as " + LocalMediaInfo.DATA + ","
                                   + "sum(" + filesTable + "." + LocalMediaInfo.FILE_SIZE + ") as " + LocalMediaInfo.FILE_SIZE + ","
                                   + Constant.MediaType.FOLDER + " as " + LocalMediaInfo.FILE_TYPE + "," 
                                   + foldersTable + "." + LocalMediaInfo.MODIFY_DATE + " as " + LocalMediaInfo.MODIFY_DATE + ","
                                   + foldersTable + "." + LocalMediaInfo.DEVICETYPE + " as " + LocalMediaInfo.DEVICETYPE + ", "
                                   + foldersTable + "." + LocalMediaInfo.PHYSIC_ID + " as " + LocalMediaInfo.PHYSIC_ID + ", "
                                   + foldersTable + "." + LocalMediaInfo.PIN_YIN + " as " + LocalMediaInfo.PIN_YIN
                    + " from "     + filesTable + ", " + foldersTable 
                    + " where "    + filesTable + "." + LocalMediaInfo.FILE_TYPE + " = " + type + " and " 
                                   + filesTable + "." + LocalMediaInfo.PARENT_PATH + " = " 
                                   + foldersTable + "." + LocalMediaInfo.PARENT_PATH + "||'/'||" + foldersTable + "." + LocalMediaInfo.FILE_NAME
                    + " group by " + filesTable + "." + LocalMediaInfo.PARENT_PATH);


        sql = sqlBuf.toString();
        return sql;
    }
    
    public Cursor queryFlatFilesWithType(String tablePrefix, int type, int limit, int offset, String sortOrder)
    {
        String sql = queryFlatFilesWithTypeSql(tablePrefix, type);    
        if (sortOrder != null)
        {
            sql += " order by " + sortOrder;
        }
        if (limit > 0)
        {
            sql += " limit " + limit + " offset " + offset;
        }
        return mLocalDBHelper.query(sql);
    }
    
    public Cursor queryAllPartionFlatFolders(String[] tablePrefixArray, int type)
    {
        if (null == tablePrefixArray || tablePrefixArray.length == 0)
        {
            return null;
        }
        StringBuffer sqlBuf = new StringBuffer(); 
        int position = 0;
        for (int i = 0; i < tablePrefixArray.length; ++i)
        {            
            if (position != 0)
            {
                sqlBuf.append(" union ");
            }
            
            position++;
            String sql = queryFlatFilesWithTypeSql(tablePrefixArray[i], type);
            sqlBuf.append(sql);
        }
        
        if (position == 0)
        {
            return null;
        }
        String allPartionSqls = " select * "
                              + " from ( " + sqlBuf.toString() + ") temp "
                              + " order by " + LocalMediaInfo.PIN_YIN;
        return mLocalDBHelper.query(allPartionSqls);
    }
        
    private String getMediaFilesSql(String tablePrefix, String sortOrder)
    {
        String getAllContents = "select * " 
                              + " from (" + getFoldersSql(tablePrefix) + " union " + getFilesSql(tablePrefix) + ") temp "
                              + " order by " + LocalMediaInfo.ORDER_FIELD;
        if (null != sortOrder)
        {
            getAllContents += "," + sortOrder;
        }
        return getAllContents;
    }
    
    private String getFilesSql(String tablePrefix)
    {
        String getAllMedias  = "select " + FILES_QUERY_PROJECTION
                + " from " + getTableFilesName(tablePrefix)
                + " where " + LocalMediaInfo.PARENT_PATH + " = ? and " + LocalMediaInfo.FILE_TYPE + " = ? ";
        return getAllMedias;
    }
    
    private String getFoldersSql(String tablePrefix)
    {
        String getAllFolders = "select " + FOLDERS_QUERY_PROJECTION
                + " from " + getTableFoldersName(tablePrefix)
                + " where " + LocalMediaInfo.PARENT_PATH + " = ? ";
        return getAllFolders;
    }   
    
    private String getAllFilesSql(String tablePrefix, String sortOrder)
    {        
        String getAllMedias  = "select " + FILES_QUERY_PROJECTION
                + " from " + getTableFilesName(tablePrefix)
                + " where " + LocalMediaInfo.PARENT_PATH + " = ? and " + LocalMediaInfo.FILE_TYPE + " != " + Constant.MediaType.SUBTITLE;
        String getAllContents = "select * " 
                + " from (" + getFoldersSql(tablePrefix) + " union " + getAllMedias + ") temp "
                + " order by " + LocalMediaInfo.ORDER_FIELD;
        if (null != sortOrder)
        {
            getAllContents += ", " + sortOrder;
        }
        return getAllContents;
    }
    
    private String getPageSql()
    {
        return " limit ? offset ? ";
    }
    public Cursor queryMediaFiles(String tablePrefix, boolean isPage, String[] params, String sortOrder)
    {        
        String sqls = getMediaFilesSql(tablePrefix, sortOrder);
        if (isPage)
        {
            sqls += getPageSql();
        }
        return mLocalDBHelper.query(sqls, params);
    }
    
    public Cursor queryAllFiles(String tablePrefix, boolean isPage, String[] params, String sortOrder)
    {        
        String sqls = getAllFilesSql(tablePrefix, sortOrder);
        if (isPage)
        {
            sqls += getPageSql();
        }
        return mLocalDBHelper.query(sqls, params);
    }
    
    public Cursor queryStaticsInfo(String tablePrefix, String path, int type)
    {        
        StringBuffer sqls = new StringBuffer();
        sqls.append("select sum(" + LocalMediaInfo.FILES + ") as " + LocalMediaInfo.FILES);
        if (type == Constant.MediaType.VIDEO)
        {
            sqls.append(", sum(" + LocalMediaInfo.FILE_SIZE + ") as " + LocalMediaInfo.FILE_SIZE);
        }
        else
        {
            sqls.append(", 0 as " + LocalMediaInfo.FILE_SIZE);
        }
        
        sqls.append(" from ( ");        
        sqls.append(" select count(*) as " + LocalMediaInfo.FILES);
        if (type == Constant.MediaType.VIDEO)
        {
            sqls.append(", sum(" + LocalMediaInfo.FILE_SIZE + ") as " + LocalMediaInfo.FILE_SIZE);
        }
        
        sqls.append(" from " + getTableFilesName(tablePrefix));
        sqls.append(" where " + LocalMediaInfo.PARENT_PATH + " = ? ");
        if (type == Constant.MediaType.VIDEO
         || type == Constant.MediaType.AUDIO
         || type == Constant.MediaType.IMAGE)
        {
            sqls.append(" and " + LocalMediaInfo.FILE_TYPE + " = " + type);
            sqls.append(" union ");
        }
        else 
        {
            sqls.append(" and " + LocalMediaInfo.FILE_TYPE + " != " + Constant.MediaType.SUBTITLE);
            sqls.append(" union all ");
        }
        
        sqls.append(" select count(*) as " + LocalMediaInfo.FILES);
        if (type == Constant.MediaType.VIDEO)
        {
            sqls.append(", 0 as " + LocalMediaInfo.FILE_SIZE);
        }
        sqls.append(" from " + getTableFoldersName(tablePrefix));
        sqls.append(" where " + LocalMediaInfo.PARENT_PATH + " = ? )");
        String[] params = {path, path};
        
        mLog.d(TAG, "cc msg sql = " + sqls.toString());
        return mLocalDBHelper.query(sqls.toString(), params);
    }
    
    public Cursor getMediaCount(String tablePrefix, int type)
    {
        if (!isTableFileExist(tablePrefix))
        {
            return null;
        }
        String sql = "select count(*) as " + LocalMediaInfo.FILES 
                  + " from " + getTableFilesName(tablePrefix) 
                  + " where " + LocalMediaInfo.FILE_TYPE + " = " + type;
        return mLocalDBHelper.query(sql);
    }
    
    public Cursor getAllCount(String tablePrefix)
    {        
        String sql = "select (count(*) - 1 ) as " + LocalMediaInfo.FILES 
                  + " from " + getTableFoldersName(tablePrefix);
        return mLocalDBHelper.query(sql);
    } 
    
    
    private String getMediaFilesCountSql(String tablePrefix)
    {
        String getAllContents = "select count(*) as " + LocalMediaInfo.FILES
                              + " from (" + getFoldersSql(tablePrefix) + " union " + getFilesSql(tablePrefix) + ") temp ";
        return getAllContents;
    }
    
    public Cursor queryMediaFilesCount(String tablePrefix, String[] params)
    {        
        String sqls = getMediaFilesCountSql(tablePrefix);
        return mLocalDBHelper.query(sqls, params);
    }
    
    private String getAllFilesCountSql(String tablePrefix)
    {
        String allFilesSql = "select " + FILES_QUERY_PROJECTION
                + " from " + getTableFilesName(tablePrefix)
                + " where " + LocalMediaInfo.PARENT_PATH + " = ? and "
                + LocalMediaInfo.FILE_TYPE + " != " + Constant.MediaType.SUBTITLE;
        String getAllContents = "select count(*) as " + LocalMediaInfo.FILES
                + " from (" + getFoldersSql(tablePrefix) + " union " + allFilesSql + ") temp ";
        return getAllContents;
    }
    
    public Cursor queryAllFilesCount(String tablePrefix, String[] params)
    {        
        String sqls = getAllFilesCountSql(tablePrefix);
        return mLocalDBHelper.query(sqls, params);
    }
    
    public Cursor queryFlatMediaFiles(String tablePrefix, String fileType, String path, int limit, int offset, String sortOrder)
    {        
        String selection = LocalMediaInfo.FILE_TYPE + " = " + fileType + " and " + LocalMediaInfo.PARENT_PATH + " = ? ";
        String[] selectionArg = {path};
        String strLimit = null;
        if (limit > 0)
        {
            strLimit = " " + offset + " , " + limit + " ";
        }
        return queryNotDistinct(getTableFilesName(tablePrefix), null, selection, selectionArg, sortOrder, strLimit);
    }
        
    public Cursor queryAllFilsByPath(String tablePrefix, String selection, int limit, int offset, String sortOrder)
    {        
        String selectionNew = LocalMediaInfo.PARENT_PATH + " = ? "
                        + " or " + LocalMediaInfo.PARENT_PATH + " like ? " + " || '/%' ";
        String[] selectionArg = {selection, selection};
        String strLimit = null;
        if (limit > 0)
        {
            strLimit = " " + offset + " , " + limit + " ";
        }
        return queryNotDistinct(getTableFilesName(tablePrefix), null, selectionNew, selectionArg, sortOrder, strLimit);
    }

    private String getFileName(String path)
    {
        int index = path.lastIndexOf("/");
        String fileName = path.substring(index + 1);
        return fileName;
    }
    
    private String getParentPath(String path)
    {
        int index = path.lastIndexOf("/");
        String parentPath = path.substring(0, index);
        return parentPath;
    }
    
    public Cursor getTopMediaFiles(String tablePrefix, String mountPath, int type)
    {        
        String sql;
        if (Constant.MediaType.FOLDER == type)
        {
            sql = " select *"
                + " from " + getTableFoldersName(tablePrefix)
                + " limit 1";
        }
        else
        {
            sql = " select *"
                + " from " + getTableFilesName(tablePrefix) 
                + " where " + LocalMediaInfo.FILE_TYPE + " = " + type
                + " limit 1";
        }
        return mLocalDBHelper.query(sql);
    }
    
    public Cursor searchMediaFiles(List<String> tablePrefixList, String keyWord)
    {        
        StringBuffer buffer = new StringBuffer();
        // 添加件数限制
        buffer.append("select * from ( ");
        
        int position = 0;
        // 遍历检索数据
        for (int i = 0; i < tablePrefixList.size(); ++i)
        {     
            // 连接所有的表
            if (position != 0)
            {
                buffer.append(" union ");
            }
            position ++;
            buffer.append("select " + LocalMediaInfo.PARENT_PATH + ",")
                              .append(LocalMediaInfo.FILE_NAME + ",")
                              .append(LocalMediaInfo.PARENT_PATH + File.separator + LocalMediaInfo.FILE_NAME + " as " + LocalMediaInfo.DATA + ",")
                              .append(LocalMediaInfo.PHYSIC_ID + ",")
                              .append(LocalMediaInfo.DEVICETYPE + ",")
                              .append(LocalMediaInfo.MODIFY_DATE + ",")
                              .append(LocalMediaInfo.FILE_TYPE + ",")
                              .append(LocalMediaInfo.FILE_SIZE + ",")
                              .append(LocalMediaInfo.PIN_YIN);                             
            buffer.append(" from " + getTableFilesName(tablePrefixList.get(i)));
            buffer.append(" where ( " + LocalMediaInfo.FILE_NAME + " like '%' || '" + sqliteEscape(keyWord) + "' || '%' escape '/' " 
                                    + "OR " + LocalMediaInfo.PIN_YIN + " like '%' || '" + sqliteEscape(keyWord) + "' || '%' escape '/' "
                                    + "OR " + LocalMediaInfo.PIN_YIN + " like '" + getFirstCharSql(keyWord) + "' || '%' escape '/') " 
                                    + "AND " + LocalMediaInfo.FILE_TYPE + " != " + Constant.MediaType.SUBTITLE + " ");
        }

        if (position == 0)
        {
            return null;
        }
        // 添加件数限制
        buffer.append(" ) temp limit ").append(Constant.SEARCH_LIMIT).append(" offset 0 ");        
        // 在所有的文件表中检索文件名相似的数据
        return  mLocalDBHelper.query(buffer.toString());

        
    }
    
    /**
     * TODO: 处理查询关键字中的特殊字符： % 和 _
     * 
     * @param keyWord 输入的查询关键字
     * @return：将 keyWord中的 % ====> \\% _ ====> \\_
     */    
    public String sqliteEscape(String keyWord)
    {
        keyWord = keyWord.replace("/", "//");
        keyWord = keyWord.replace("'", "''");
        keyWord = keyWord.replace("[", "/[");
        keyWord = keyWord.replace("]", "/]");
        keyWord = keyWord.replace("%", "/%");
        keyWord = keyWord.replace("&", "/&");
        keyWord = keyWord.replace("_", "/_");
        keyWord = keyWord.replace("(", "/(");
        keyWord = keyWord.replace(")", "/)");
        return keyWord;
    }

    
    public boolean isTableFileExist(String tablePrefix)
    {
        return mLocalDBHelper.isTableExist(getTableFilesName(tablePrefix));
    }
    
    public boolean isTableFolderExist(String tablePrefix)
    {
        return mLocalDBHelper.isTableExist(getTableFoldersName(tablePrefix));
    }
    
    public static ContentValues getFileContentValue(LocalMediaInfo fileInfo)
    {
        ContentValues values = new ContentValues();
        values.put(LocalMediaInfo.FILE_NAME, fileInfo.getmFileName());
        values.put(LocalMediaInfo.PARENT_PATH, fileInfo.getmParentPath());
        values.put(LocalMediaInfo.FILE_TYPE, fileInfo.getmFileType());
        values.put(LocalMediaInfo.MODIFY_DATE, fileInfo.getmModifyDate());
        values.put(LocalMediaInfo.FILE_SIZE, fileInfo.getmFileSize());
        values.put(LocalMediaInfo.PIN_YIN, fileInfo.getmPinyin());      
        values.put(LocalMediaInfo.DEVICETYPE, fileInfo.getmDeviceType());
        values.put(LocalMediaInfo.PHYSIC_ID, fileInfo.getmPhysicId());
        return values;
    }
    
    public static ContentValues getFolderContentValue(LocalMediaInfo folderInfo)
    {
        ContentValues values = new ContentValues();
        values.put(LocalMediaInfo.FILE_NAME, folderInfo.getmFileName());
        values.put(LocalMediaInfo.PARENT_PATH, folderInfo.getmParentPath());
        values.put(LocalMediaInfo.FOLDERS, folderInfo.getmFolders());
        values.put(LocalMediaInfo.MODIFY_DATE, folderInfo.getmModifyDate());
        values.put(LocalMediaInfo.FILES, folderInfo.getmFiles());
        values.put(LocalMediaInfo.PIN_YIN, folderInfo.getmPinyin());    
        values.put(LocalMediaInfo.DEVICETYPE, folderInfo.getmDeviceType());
        values.put(LocalMediaInfo.PHYSIC_ID, folderInfo.getmPhysicId());
        return values;
    }
    
    private String getFirstCharSql(String keys)
    {
        if (StringUtils.isEmpty(keys))
        {
            return null;
        }
        String keyWord = sqliteEscape(keys);
        StringBuffer sb = new StringBuffer();
        int length = keyWord.length();
        for (int i = 0; i < length; ++i)
        {
            char ch = keyWord.charAt(i);
            sb.append(ch);
            if (Character.isLetter(ch))
            {
                sb.append("%");
                if (i != length -1)
                {
                    sb.append(" ");
                }                
            }            
        }
        return sb.toString();
    }    
    
}
