package com.rockchips.mediacenter.basicutils.tool;

import java.util.List;

import android.database.Cursor;
import android.util.Log;

import com.rockchips.mediacenter.basicutils.util.StringUtils;

/**
 * @author t00181037
 * @version 1.0
 * @created 22-二月-2012 14:41:21
 */
/**
 * 读取数据库中字段工具类
 */
public final class SqlQueryTool
{
    private static final String TAG = "SqlQueryTool";

    private SqlQueryTool()
    {
    }

    /**
     * 
     * insert column into the list projList. To assure that each element of
     * projList is unique, <b>check whether column is in projList before
     * inserted into projList;</b>
     * 
     * @param projList
     * @param column the value to insert into projList
     */
    public static void insertUniqElementIntoList(List<String> projList, String column)
    {
        // check the input-parameter
        if (projList == null)
        {
            Log.e(TAG, "FUNCTION　insertUniqElementIntoList: input-parameter projList is null");
            return;
        }

        if (StringUtils.isEmpty(column))
        {
            Log.e(TAG, "FUNCTION　insertUniqElementIntoList: input-parameter column is empty");
            return;
        }

        if (projList.contains(column))
        {
            return;
        }

        projList.add(column);
    }

    /**
     * 
     * remove column into the list projList. To assure that each element of
     * projList is unique, <b>check whether column is in projList before
     * inserted into projList;</b>
     * 
     * @param projList
     * @param column the value to insert into projList
     */
    public static void removeUniqElementIntoList(List<String> projList, String column)
    {
        // check the input-parameter
        if (projList == null)
        {
            Log.e(TAG, "FUNCTION　removeUniqElementIntoList: input-parameter projList is null");
            return;
        }

        if (StringUtils.isEmpty(column))
        {
            Log.e(TAG, "FUNCTION　removeUniqElementIntoList: input-parameter column is empty");
            return;
        }

        if (projList.contains(column))
        {
            projList.remove(column);
        }

    }

    /**
     * return double-type data from column
     * 
     * @param colName
     * @param cursor
     * @return success return value, else Double.NEGATIVE_INFINITY;
     */
    public static double getDoubleColumn(final String colName, final Cursor cursor)
    {

        // check input parameter
        if (cursor == null)
        {
            Log.e(TAG, "FUNCTION　getDoubleColumn: input-parameter cursor is null");
            return Double.NEGATIVE_INFINITY;
        }

        if (StringUtils.isEmpty(colName))
        {
            Log.e(TAG, "FUNCTION　getDoubleColumn: input-parameter colName is empty");
            return Double.NEGATIVE_INFINITY;
        }

        double value = Double.NEGATIVE_INFINITY;
        if (!cursor.isClosed())
        {
            final int idx = cursor.getColumnIndex(colName);
            if (idx >= 0)
            {
                value = cursor.getDouble(idx);
            }
        }
        else
        {
            Log.e(TAG, "FUNCTION　getIntColumn: the cursor has been closed");
        }
        return value;
    }

    /**
     * return int-type data from column
     * 
     * @param colName
     * @param cursor
     * @return success return value, else Integer.MIN_VALUE;
     */
    public static int getIntColumn(final String colName, final Cursor cursor)
    {

        // check input parameter
        if (cursor == null)
        {
            Log.e(TAG, "FUNCTION　getIntColumn: input-parameter cursor is null");
            return Integer.MIN_VALUE;
        }

        if (StringUtils.isEmpty(colName))
        {
            Log.e(TAG, "FUNCTION　getIntColumn: input-parameter colName is empty");
            return Integer.MIN_VALUE;
        }

        int value = Integer.MIN_VALUE;

        if (!cursor.isClosed())
        {
            final int idx = cursor.getColumnIndex(colName);
            if (idx >= 0)
            {
                value = cursor.getInt(idx);
            }
        }
        else
        {
            Log.e(TAG, "FUNCTION　getIntColumn: the cursor has been closed");
        }

        return value;
    }

    /**
     * return Long-type data from column
     * 
     * @param colName
     * @param cursor
     * @return success return value, else Integer.MIN_VALUE;
     */
    public static Long getLongColumn(final String colName, final Cursor cursor)
    {

        // check input parameter
        if (cursor == null)
        {
            Log.e(TAG, "FUNCTION　getLongColumn: input-parameter cursor is null");
            return Long.MIN_VALUE;
        }

        if (StringUtils.isEmpty(colName))
        {
            Log.e(TAG, "FUNCTION　getLongColumn: input-parameter colName is empty");
            return Long.MIN_VALUE;
        }

        long value = Long.MIN_VALUE;

        if (!cursor.isClosed())
        {
            final int idx = cursor.getColumnIndex(colName);
            if (idx >= 0)
            {
                value = cursor.getLong(idx);
            }
        }
        else
        {
            Log.e(TAG, "FUNCTION　getLongColumn: the cursor has been closed");
        }

        return value;
    }

    /**
     * return string-type data from column
     * 
     * @param colName
     * @param cursor
     * @return success return value, else null;
     */
    public static String getStringColumn(final String colName, final Cursor cursor)
    {

        // check input parameter
        if (cursor == null)
        {
            Log.e(TAG, "FUNCTION　getStringColumn: input-parameter cursor is null");
            return null;
        }

        if (StringUtils.isEmpty(colName))
        {
            Log.e(TAG, "FUNCTION　getStringColumn: input-parameter colName is empty");
            return null;
        }

        String value = null;

        if (!cursor.isClosed())
        {
            final int idx = cursor.getColumnIndex(colName);
            if (idx >= 0)
            {
                value = cursor.getString(idx);
            }
        }
        else
        {
            Log.e(TAG, "FUNCTION　getStringColumn: the cursor has been closed");
        }

        return value;
    }

}