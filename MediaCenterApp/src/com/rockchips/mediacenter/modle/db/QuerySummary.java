/**
 * Title: QuerySummary.java<br>
 * Package: com.rockchips.mediacenter.basicutils.provider<br>
 * Description: TODO<br>
 * @author w00190739
 * @version v1.0<br>
 * Date: 2014-7-10上午8:47:59<br> 
 * Copyright © Huawei Technologies Co., Ltd. 2014. All rights reserved.
 */

package com.rockchips.mediacenter.modle.db;

import com.rockchips.mediacenter.utils.IICLOG;

/**
 * Description: TODO<br>
 * @author w00190739
 * @version v1.0 Date: 2014-7-10 上午8:47:59<br>
 */

public class QuerySummary
{
    private static final String TAG = "QuerySummary";

    private static final IICLOG Log = IICLOG.getInstance();

    /**
     * begin-pos in table of db
     */
    private int mBeginPos;

    /**
     * the sum of the data that we'll query; if beginPos + length is more than totalRecord, modify length to totalrecord - beginPos
     */
    private int mLength;

    /**
     * the sum of db record;
     */
    private int mTotalRecord;

    public QuerySummary()
    {
    }

    public QuerySummary(QuerySummary hQuerySummary)
    {
        mBeginPos = hQuerySummary.getBeginPos();
        mLength = hQuerySummary.getLength();
        mTotalRecord = hQuerySummary.getTotalRecord();
    }

    /**
     * begin-pos in table of db
     */
    public int getBeginPos()
    {
        return mBeginPos;
    }

    /**
     * the sum of the data that we'll query if beginPos + length is more than totalRecord, modify length to totalrecord - beginPos
     */
    public int getLength()
    {
        return mLength;
    }

    /**
     * the sum of db record
     */
    public int getTotalRecord()
    {
        return mTotalRecord;
    }

    /**
     * begin-pos IN TABLE OF DB
     * 
     * @param newVal
     */
    public void setBeginPos(int newVal)
    {
        mBeginPos = newVal;
    }

    /**
     * the sum of the data that we'll query
     * 
     * @param newVal
     */
    public void setLength(int newVal)
    {
        mLength = newVal;
    }

    /**
     * the sum of db record
     * 
     * @param newVal
     */
    public void setTotalRecord(int newVal)
    {
        mTotalRecord = newVal;
    }

    @Override
    public String toString()
    {
        String str = " [ b:" + getBeginPos() + " l:" + getLength() + " t:" + getTotalRecord() + "]";
        Log.d(TAG, "class querysummary function tostring: " + str);
        return str;
    }
}
