/**
 * Title: FileShareDatabase.java<br>
 * Package: com.rockchips.mediacenter.dlnaserver.db<br>
 * Description: TODO<br>
 * @author c00224451
 * @version v1.0<br>
 * Date: 2014-7-28下午7:54:28<br> 
 * Copyright © Huawei Technologies Co., Ltd. 2014. All rights reserved.
 */

package com.rockchips.mediacenter.dlnaserver.db;

import java.io.File;
import java.io.IOException;

import com.rockchips.android.airsharing.util.IICLOG;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Description: TODO<br>
 * @author c00224451
 * @version v1.0
 * Date: 2014-7-28 下午7:54:28<br>
 */

public class FileShareHelper extends SQLiteOpenHelper
{          
    private static final String TAG = "FileShareHelper";
    private IICLOG mLog = IICLOG.getInstance();
    
    private static final String DATABASE_NAME = "fileShare.db";
    private static final int DATABASE_VERSION = 2;
    
    private static FileShareHelper mStFileShareHelper;
    private String mDatabaseSaveDir;
    private SQLiteDatabase mDatabase;
    
    public static final String TABLE_NAME = "fileShare";
    public static final String _ID = "_ID";
    public static final String FILE_PATH = "FILE_PATH";     
    public static final String MOUNT_PATH = "MOUNT_PATH";
    public static final String IS_FOLDER = "IS_FOLDER";
    
    private static final String CREATE_TABLE_SQL = "create table if not exists " + TABLE_NAME + 
            "(" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + FILE_PATH + " TEXT,"             
            + MOUNT_PATH + " TEXT," 
            + IS_FOLDER + " INTEGER)";
    
    public static FileShareHelper getInstance(Context context)
    {
        if (null == mStFileShareHelper)
        {
            mStFileShareHelper = new FileShareHelper(context);
        }
        return mStFileShareHelper;
    }
    
    private FileShareHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mDatabaseSaveDir = context.getCacheDir() + File.separator + "databases" + File.separator;
        mDatabase = getWritableDatabase();        
    }
    
    @Override
    public void onCreate(SQLiteDatabase arg0)
    {

        // TODO Auto-generated method stub

    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        mLog.d(TAG, "onUpgrade oldVersion = " + " newVersion = " + newVersion);
//        mDatabase = getWritableDatabase();
//        if (newVersion != oldVersion)
//        {
//            if (mDatabase == null)
//            {
//                return;
//            }
//            mDatabase.beginTransaction();
//            //情况表数据
//            String deleteSql = "drop table " + TABLE_NAME ;            
//            mDatabase.execSQL(deleteSql);
//            mDatabase.execSQL(CREATE_TABLE_SQL);
//            mDatabase.endTransaction();
//        }
    }

    @Override
    public SQLiteDatabase getWritableDatabase()
    {
        if (null == mDatabase)
        {            
            createDatabase();
        }
        return mDatabase;
        
    }
    
    private SQLiteDatabase createDatabase()
    {
        File dbFile = new File(mDatabaseSaveDir + DATABASE_NAME);
        File dbDir = new File(mDatabaseSaveDir);
        
        if (!dbDir.exists())
        {
            if (!dbDir.mkdir())
            {
                mLog.e(TAG, "mkdir failed!");
                return null;
            }            
        }
        if (!dbFile.exists())
        {
            // 数据库文件是否创建成功
            boolean isFileCreateSuccess = true;
            try
            {
                isFileCreateSuccess = dbFile.createNewFile();
                if (!isFileCreateSuccess)
                {
                    mLog.e(TAG, "createNewFile failed!");
                    return null;
                }
            }
            catch (IOException e)
            {
                return null;
            }
        }
                
        mDatabase = SQLiteDatabase.openOrCreateDatabase(mDatabaseSaveDir + DATABASE_NAME, null); 
        //数据库是否升级处理
        int oldVersion = mDatabase.getVersion();
        mLog.d(TAG, "cc msg createDatabase oldVersion = " + oldVersion + " database version = " + DATABASE_VERSION);
		try
		{
	        if (oldVersion != DATABASE_VERSION)
	        {
	            String deleteSql = "drop table if exists " + TABLE_NAME ;            
	            mDatabase.execSQL(deleteSql);            
	        }			
		}
		catch (SQLException e)
		{		    
		}
		mDatabase.setVersion(DATABASE_VERSION);
        mDatabase.execSQL(CREATE_TABLE_SQL);
        return mDatabase;
    }

}
