/**
 * Title: LocalDBHelper.java<br>
 * Package: com.rockchips.mediacenter.localscan.database<br>
 * Description: TODO<br>
 * @author c00224451
 * @version v1.0<br>
 * Date: 2014-6-30下午2:51:06<br> 
 * Copyright © Huawei Technologies Co., Ltd. 2014. All rights reserved.
 */

package com.rockchips.mediacenter.localscan.database;

import java.io.File;
import java.io.IOException;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.rockchips.mediacenter.basicutils.util.IICLOG;
import com.rockchips.mediacenter.basicutils.util.StringUtils;

/**
 * Description: TODO<br>
 * @author c00224451
 * @version v1.0
 * Date: 2014-6-30 下午2:51:06<br>
 */

public final class LocalDBHelper extends SQLiteOpenHelper
{
    private static final String TAG = "LocalDBHelper";
    private IICLOG mLog = IICLOG.getInstance();
    private static LocalDBHelper mStLocalDBHelper;
    private static Object mInstanceLock = new Object();
    
    private static final int DATABASE_VERSION = 1;
    private String mDatabaseSaveDir;
    private SQLiteDatabase mDatabase;
    
    // 标识数据库创建在本地还是内存：true 本地创建
    private static final boolean DB_STORE_MODE_IN_LOCAL = true; 
    private static final String DATABASE_NAME = "local.db";
    private static String DB_DIR_PATH;
    protected Object mLock;
    
    private LocalDBHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.i(TAG, "LocalDBHelper");
        mLock = new Object();
        mDatabaseSaveDir = context.getCacheDir() + File.separator + "databases" + File.separator;
        DB_DIR_PATH = mDatabaseSaveDir;
        mLog.d(TAG, "local DB Helper dir = " + mDatabaseSaveDir);
        mDatabase = getWritableDatabase();           
    }
    
    public static LocalDBHelper getInstance(Context context)
    {
        synchronized (mInstanceLock)
        {
            if (null == mStLocalDBHelper)
            {
                mStLocalDBHelper = new LocalDBHelper(context);
            }
            return mStLocalDBHelper;
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {

        // TODO Auto-generated method stub

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {

    }
          
    @Override
    public SQLiteDatabase getWritableDatabase()
    {
        synchronized (mLock)
        {
            if (null == mDatabase)
            {
                createDatabase();
            }
            return mDatabase;
        }
    }
    
    private SQLiteDatabase createDatabase()
    {        
        if (DB_STORE_MODE_IN_LOCAL)
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
            // 数据库文件是否创建成功
            boolean isFileCreateSuccess = false; 
            if (dbFile.exists())
            {
                dbFile.delete();
            } 
            
            try
            {
                isFileCreateSuccess = dbFile.createNewFile();
            }
            catch (IOException ioex)
            {
            }
            
            if (isFileCreateSuccess)
            {
                mDatabase = SQLiteDatabase.openOrCreateDatabase(mDatabaseSaveDir + DATABASE_NAME, null);
            }
            else
            {
                mLog.e(TAG, "create database file failed!");
                return null;
            }         
            
        }
        else
        {
            mLog.d(TAG, "DBHelper getWritableDatabase() create DB in memory.");
            mDatabase = SQLiteDatabase.create(null);
        }  
        
        return mDatabase;
    }    
    
    
    public void execSQL(String sqls)
    {
        mLog.d(TAG, "execSQL start");
        
        if (StringUtils.isEmpty(sqls) || null == mDatabase)
        {
            mLog.e(TAG, "sql is null");
            return;
        }

        try
        {
            synchronized (mLock)
            {
                mDatabase.execSQL(sqls);
            }
        }
        catch (SQLException e)
        {
            mLog.e(TAG, "execSQL exception ");
        }
    } 
    
    public void execSQL(String sqls, String[] conditions)
    {
        mLog.d(TAG, "execSQL start");
        
        if (StringUtils.isEmpty(sqls) || null == mDatabase)
        {
            mLog.e(TAG, "sql is null");
            return;
        }
        
        if (conditions == null || conditions.length == 0)
        {
            mLog.e(TAG, "conditions is null");
            return;
        }
                
        try
        {
            synchronized (mLock)
            {
                mDatabase.execSQL(sqls, conditions);
            }
        }
        catch (SQLException e)
        {
            mLog.e(TAG, "execSQL exception ");            
        }
    }
    
    public long insert(String tableName, ContentValues values)
    {
    	Log.i(TAG, "insert value");
        long rowId = -1;
        if (null == values || null == mDatabase)
        {
            return rowId;
        }        
        synchronized (mLock)
        {
            rowId = mDatabase.insert(tableName, null, values);
        }
        
        return rowId;
    }
    
    public void insert(String tableName, List<ContentValues> valueList)
    {
    	Log.i(TAG, "insert valueList");
        if (null == valueList || null == mDatabase)
        {
            return;
        }
        synchronized (mLock)
        {
         // 获取数据库对象，开始事务
            mDatabase.beginTransaction();
            
            try
            {
                for (int i = 0; i < valueList.size(); i++)
                {
                    mDatabase.insert(tableName, null, valueList.get(i));
                }
                
                // 如果全部SQL执行成功，则置事务成功
                mDatabase.setTransactionSuccessful();
            }
            catch (SQLException e)
            {
                mLog.e(TAG, "insert exception ");
            }
            // 提交事务，如果有异常，则事务成功不会被设置，此处会回滚
            mDatabase.endTransaction();
        }
    }
    
    public int update(String tableName, ContentValues values, String whereClause, String[] whereArgs)
    {
        int rowId = -1;
        if (null == values || null == mDatabase)
        {
            return rowId;
        }        
        synchronized (mLock)
        {
            rowId = mDatabase.update(tableName, values, whereClause, whereArgs);
        }        
        return rowId;        
    }
    
    public int delete(String tableName, String whereClause, String[] whereArgs)
    {
    	Log.i(TAG, "delete row");
    	
        if (null == mDatabase)
        {
            return 0;
        }
        synchronized (mLock)
        {
            return mDatabase.delete(tableName, whereClause, whereArgs);
        } 
    }
    
    /**
     * 数据库查询
     * 
     * @param sql
     *            sql
     * @param conditions
     *            conditions
     * @return 游标
     */
    public Cursor query(String sql, String[] conditions)
    {
        if (sql == null || sql.trim().length() == 0 || null == mDatabase)
        {
            return null;
        }
        
        if (conditions == null || conditions.length == 0)
        {
            return null;
        }
            
        Cursor cursor = null;
        synchronized (mLock)
        {
            cursor = mDatabase.rawQuery(sql.trim(), conditions);
        }
        
        return cursor;
        
    }
    
    /**
     * 数据库查询
     * 
     * @param sql
     *            sql
     * @param conditions
     *            conditions
     * @return 游标
     */
    public Cursor query(String sql)
    {          
        if (sql == null || sql.trim().length() == 0 || null == mDatabase)
        {
            return null;
        }        
        
        Cursor cursor = null;
        synchronized (mLock)
        {
            cursor = mDatabase.rawQuery(sql.trim(), null);
        }
        
        return cursor;
        
    }
    
    public Cursor queryDistinct(String tableName, String[] columns, String selection, String[] selectionArgs, String orderBy)
    {
        return queryDistinct(tableName, columns, selection, selectionArgs, orderBy, null);
    }
    
    public Cursor queryNotDistinct(String tableName, String[] columns, String selection, String[] selectionArgs, String orderBy)
    {        
        return queryNotDistinct(tableName, columns, selection, selectionArgs, orderBy, null);
    }
    
    public Cursor queryDistinct(String tableName, String[] columns, String selection, String[] selectionArgs, String orderBy, String limit)
    {
        if (null == tableName || null == mDatabase)
        {
            return null;
        }
        Cursor cursor = null;
        synchronized (mLock)
        {            
            cursor = 
                    mDatabase.query(true, tableName, columns, selection, selectionArgs, null, null, orderBy, limit);
        }
        return cursor;
    }
    
    public Cursor queryNotDistinct(String tableName, String[] columns, String selection, String[] selectionArgs, String orderBy, String limit)
    {
        if (null == tableName || null == mDatabase) 
        {
            return null;
        }
        Cursor cursor = null;
        synchronized (mLock)
        {            
            cursor = 
                    mDatabase.query(false, tableName, columns, selection, selectionArgs, null, null, orderBy, limit);
        }
        return cursor;
    }
    
    /**
     * 批量操作数据
     * 该函数需要SQL语句及对应的参数
     * 
     * @param sql SQL语句
     * @see [类、类#方法、类#成员]
     */
    public boolean batchExecSQL(List<String> sql)
    {
        mLog.d(TAG, "LocalDBHelper batchExecSQL start");
        
        // 缺少SQL语句
        if (sql == null || sql.size() == 0 || null == mDatabase)
        {
            return false;
        }
        
        synchronized (mLock)
        {
            // 获取数据库对象，开始事务
            mDatabase.beginTransaction();
            
            try
            {
                for (int i = 0; i < sql.size(); i++)
                {
                    mDatabase.execSQL(sql.get(i));
                }            
                // 如果全部SQL执行成功，则置事务成功
                mDatabase.setTransactionSuccessful();
            }
            catch (SQLException e)
            {
                mLog.e(TAG, "LocalDBHelper batchExecSQL exception ");
                // 提交事务，如果有异常，则事务成功不会被设置，此处会回滚
                mDatabase.endTransaction();
                return false;
            }
            
            // 提交事务，如果有异常，则事务成功不会被设置，此处会回滚
            mDatabase.endTransaction();
        }
        
        mLog.d(TAG, "LocalDBHelper batchExecSQL end");
        return true;
    }
    
    public boolean isTableExist(String tabName)
    {
        boolean result = false;
        if (tabName == null)
        {
            return false;
        }
        Cursor cursor = null;
        String sql = "select count(*) as c from sqlite_master where type ='table' and name ='" + tabName.trim() + "'";
        cursor = query(sql);
        if (null == cursor)
        {
            return false;
        }
        if (cursor.moveToNext())
        {
            int count = cursor.getInt(0);
            if (count > 0)
            {
                result = true;
            }
        }
        cursor.close();
        return result;
    }


    /**获取数据库目录*/
    public static String getDbDirPath(){
    	return DB_DIR_PATH;
    }
}
