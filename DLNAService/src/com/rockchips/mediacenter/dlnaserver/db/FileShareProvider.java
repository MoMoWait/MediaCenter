package com.rockchips.mediacenter.dlnaserver.db;

import java.io.File;

import com.rockchips.android.airsharing.util.IICLOG;
import com.rockchips.mediacenter.basicutils.constant.LocDevProvConst;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;


public class FileShareProvider extends ContentProvider
{
    private static final String TAG = "FileShareProvider";
    private IICLOG mLog = IICLOG.getInstance();
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private SQLiteDatabase db = null;

    private static final FileShareProvider instance = new FileShareProvider();

    // 数据库表url
    static
    {
        sUriMatcher.addURI(LocDevProvConst.AUTHORITY, LocDevProvConst.FILE_SHARE_URI, LocDevProvConst.FILE_SHARE_CODE);

    }

    public static FileShareProvider getInstance()
    {

        return instance;
    }

    /**
     * 移除共享目录
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        mLog.d(TAG, "delete:Uri=" + uri.toString() + ", selection=" + selection);
        final int match = sUriMatcher.match(uri);
        if (match == LocDevProvConst.FILE_SHARE_CODE)
        {

            int id = db.delete(FileShareHelper.TABLE_NAME, selection, selectionArgs);
            return id;
        }
        return -1;
    }

    @Override
    public String getType(Uri uri)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * 添加共享目录
     */
    @Override
    public Uri insert(Uri uri, ContentValues values)
    {
        mLog.d(TAG, "insert:Uri=" + uri.toString() + ", values=" + values);
        final int match = sUriMatcher.match(uri);
        if (match == LocDevProvConst.FILE_SHARE_CODE)
        {
            long id = db.insert(FileShareHelper.TABLE_NAME, null, values);
            if (id == -1)
            {
                return null;
            }
            return Uri.parse(uri.toString() + File.separator + id);
        }
        return null;
    }

    @Override
    public boolean onCreate()
    {
        FileShareHelper fileShareHelper = FileShareHelper.getInstance(getContext());        
        db = fileShareHelper.getWritableDatabase();
        return true;
    }

    /**
     * 查询共享目录
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder)
    {
        final int match = sUriMatcher.match(uri);
        if (match == LocDevProvConst.FILE_SHARE_CODE)
        {
            Cursor cursor = db.query(FileShareHelper.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
            return cursor;
        }
        return null;
    }

    /**
     * 更新共享目录
     */
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        mLog.d(TAG, "update:Uri=" + uri.toString() + ", values=" + values);
        return  db.update(FileShareHelper.TABLE_NAME, values, selection, selectionArgs);       
    }
    
    



}
