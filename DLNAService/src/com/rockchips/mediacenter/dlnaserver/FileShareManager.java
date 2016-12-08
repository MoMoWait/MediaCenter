/**
 * Title: FileShareData.java<br>
 * Package: com.rockchips.mediacenter.dlnaserver<br>
 * Description: TODO<br>
 * @author c00224451
 * @version v1.0<br>
 * Date: 2014-7-29上午10:21:24<br> 
 * Copyright © Huawei Technologies Co., Ltd. 2014. All rights reserved.
 */

package com.rockchips.mediacenter.dlnaserver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.rockchips.mediacenter.basicutils.constant.Constant;
import com.rockchips.mediacenter.basicutils.util.IICLOG;
import com.rockchips.mediacenter.dlnaserver.db.FileShareHelper;
/**
 * Description: TODO<br>
 * @author c00224451
 * @version v1.0
 * Date: 2014-7-29 上午10:21:24<br>
 */

public final class FileShareManager
{
    private static final String TAG = "FileShareManager"; 
    private static IICLOG mStLog = IICLOG.getInstance();

    private static FileShareManager mStFileShareManager;
    private Context mContext;
    private FileShareHelper mFileShareHelper;
    SQLiteDatabase mDatabase = null;  
    
    public static final int FOLDER_TYPE = 1;
    public static final int MEDIA_TYPE = 0;
    
    private FileShareManager(Context context)
    {
        mContext = context;
        mFileShareHelper = FileShareHelper.getInstance(mContext);        
        try
        {
            mDatabase = mFileShareHelper.getWritableDatabase();
        }
        catch (SQLiteException e)
        {            
        }        
    }
    
    public static FileShareManager getInstance(Context context)
    {
        if (null == mStFileShareManager)
        {
            mStFileShareManager = new FileShareManager(context);
        }
        return mStFileShareManager;
    }
    /**
     * 判断目录共享状态
     * 
     * @param foldername 目录名称
     *            
     * @return 共享状态 see: ShareState
     */
    public int getFolderShareStatus(String foldername, String mountPath)
    {
        
        if (null == foldername || foldername.trim().equals(""))
        {
            return Constant.ShareState.SHARE_STATE_UNABLE;
        }
        
        // 判断目录是否已经被共享
        return containsSharedFile(foldername, mountPath);
    }
    
    private int containsSharedFile(String filePath, String mountPath)
    {
        Cursor cursor = null;         
        if (mDatabase == null)
        {            
            return Constant.ShareState.SHARE_STATE_UNABLE;
        }
        
        int ret = Constant.ShareState.SHARE_STATE_ENABLE;
        if (null != mountPath)
        {
            cursor = getShareFilesByMountpath(mountPath);
        }
        else
        {
            cursor = mDatabase.query(FileShareHelper.TABLE_NAME, null, null, null, null, null, null);
        }
        
        if (null != cursor)
        {
            while (cursor.moveToNext())
            {
                String key = cursor.getString(cursor.getColumnIndex(FileShareHelper.FILE_PATH));
                if (filePath != null)
                {
                    // 判断当前目录已被共享
                    if (filePath.equals(key))
                    {
                        ret = Constant.ShareState.SHARE_STATE_SHARED;
                        break;
                    }  
                    if (filePath.startsWith(key + File.separator))
                    {
                        ret = Constant.ShareState.SHARE_STATE_UNABLE;
                        break;
                    }
                    
                }
            }
            cursor.close();
        }                
        return ret;
    }
    
    public boolean isParentShared(String filePath, String mountPath)
    {
        boolean result = false;        
        Cursor cursor = null;       
        
        if (mDatabase == null)
        {            
            return result;
        }
        
        cursor = getShareFilesByMountpath(mountPath);
        
        if (null != cursor)
        {
            while (cursor.moveToNext())
            {
                String key = cursor.getString(cursor.getColumnIndex(FileShareHelper.FILE_PATH));
                if (filePath != null)
                {   
                    if (filePath.startsWith(key + File.separator))
                    {
                        result = true;
                        break;
                    }
                    
                }
            }
            cursor.close();
        }        
        return result;
    }
        
    public void cancelSharedFile(List<String> filePathList, String mountPath)
    {
        if (null == filePathList)
        {
            return;
        }
        deleteShareFiles(filePathList);
    }
    
    public void addSharedFile(List<String> filePathList, boolean isFolder, String mountPath)
    {
        if (null == filePathList)
        {
            return;
        }        
        int type;
        if (isFolder)
        {
            type = FOLDER_TYPE;
        }
        else
        {
            type = MEDIA_TYPE;
        }
        mDatabase.beginTransaction();
        try
        {
            for (int i = 0; i < filePathList.size(); ++i)
            {
                String path = filePathList.get(i);
                deleteSubFiles(path, mountPath);
                ContentValues values = new ContentValues();
                values.put(FileShareHelper.FILE_PATH, path);            
                values.put(FileShareHelper.MOUNT_PATH, mountPath);
                values.put(FileShareHelper.IS_FOLDER, type);
                mDatabase.insert(FileShareHelper.TABLE_NAME, null, values);
            }
            
            // 如果全部SQL执行成功，则置事务成功
            mDatabase.setTransactionSuccessful();
        }
        catch (SQLException e)
        {
            mStLog.e(TAG, "insert exception ");
        }
        
        // 提交事务，如果有异常，则事务成功不会被设置，此处会回滚
        mDatabase.endTransaction();  
    }
    
    private void deleteSubFiles(String path, String mountPath)
    {
        Cursor cursor = getShareFilesByMountpath(mountPath);
        if (cursor == null)
        {
            return;
        }
        List<String> filePathList = new ArrayList<String>();
        while (cursor.moveToNext())
        {
            String key = cursor.getString(cursor.getColumnIndex(FileShareHelper.FILE_PATH));
            if (key.startsWith(path  + File.separator))
            {
                filePathList.add(key);
            }
        }
        cursor.close();
        
        if (filePathList.size() != 0)
        {
            deleteShareFiles(filePathList);
        }
    }
    
    private void deleteShareFiles(List<String> filePathList)
    {        
        mDatabase.beginTransaction();
        
        try
        {
            for (int i = 0; i < filePathList.size(); ++i)
            {
                String path = filePathList.get(i);
              	 String whereClause = FileShareHelper.FILE_PATH + " = ? or " + FileShareHelper.FILE_PATH + " like '" +  path + File.separator +  "%'";
                String[] whereArgs = {path};
                mDatabase.delete(FileShareHelper.TABLE_NAME, whereClause, whereArgs);                
            }
            
            // 如果全部SQL执行成功，则置事务成功
            mDatabase.setTransactionSuccessful();
        }
        catch (SQLException e)
        {
            mStLog.e(TAG, "delete exception ");
        }
        
        // 提交事务，如果有异常，则事务成功不会被设置，此处会回滚
        mDatabase.endTransaction();
    }
    
    public Cursor getShareFilesByMountpath(String mountPath)
    {
        if (null != mDatabase)
        {
            String selection = FileShareHelper.MOUNT_PATH + " = ? ";
            String[] selectionArgs = {mountPath};
            Cursor cursor = mDatabase.query(FileShareHelper.TABLE_NAME, null, selection, selectionArgs, null, null, null);
            return cursor;
        }
        return null;
    }
    
    public Cursor getShareFiles()
    {
        if (null != mDatabase)
        {            
            Cursor cursor = mDatabase.query(FileShareHelper.TABLE_NAME, null, null, null, null, null, null);
            return cursor;
        }
        return null;
    }
    
    public Cursor getAllShareFilesByFolder(String mountPath, String parentPath)
    {
        if (null != mDatabase)
        {            
            String selection = FileShareHelper.MOUNT_PATH + " = ? and " 
                                + FileShareHelper.FILE_PATH + " like '" +  parentPath + File.separator +  "%'";
            String[] selectionArgs = {mountPath};
            Cursor cursor = mDatabase.query(FileShareHelper.TABLE_NAME, null, selection, selectionArgs, null, null, null);
            return cursor;
        }
        return null;
    }
}
