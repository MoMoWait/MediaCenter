package com.rockchips.mediacenter.provider;

/**
 * Title: ProjectionProvider.java<br>
 * Package: com.rockchips.mediacenter.basicutils.provider<br>
 * Description: TODO<br>
 * @author w00190739
 * @version v1.0<br>
 * Date: 2014-7-9下午5:59:57<br> 
 * Copyright © Huawei Technologies Co., Ltd. 2014. All rights reserved.
 */

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.database.Cursor;

import com.rockchips.mediacenter.basicutils.tool.SqlQueryTool;
import com.rockchips.mediacenter.basicutils.util.IICLOG;

/**
 * Description: TODO<br>
 * @author w00190739
 * @version v1.0 Date: 2014-7-9 下午5:59:57<br>
 */

public abstract class ProjectionProvider
{
    protected static final IICLOG Log = IICLOG.getInstance();

    protected static final String TAG = "ProjectionProvider";

    private Object mLocalObj;

    //
    private Date mDate;

    public int timeZoneOffset;

    public ProjectionProvider()
    {
        mDate = new Date();
        timeZoneOffset = mDate.getTimezoneOffset();
    }

    /**
     * @param mfi the localObj to set
     */
    public void setLocalObj(Object localObj)
    {
        this.mLocalObj = localObj;
    }

    /**
     * @return the localObj
     */
    public Object getLocalObj()
    {
        return mLocalObj;
    }

    /**
     * get projection list of columns
     * @return projection list of columns
     */
    public List<String> getProjList()
    {
        List<String> list = new ArrayList<String>();

        SqlQueryTool.insertUniqElementIntoList(list, null);

        return list;
    }

    /**
     * get all data of the row indicated by cursor implemented using the tool {@link SqlQueryTool}.
     * @param cursor
     * 
     */
    public void importRecord(Cursor cursor)
    {
        if (null == cursor)
        {
//            Log.e(TAG, "class projectionprovider function importRecord : input parameter cursor is null");
            return;
        }

        if (null == getLocalObj())
        {
//            Log.e(TAG, "class projectionprovider function importRecord : getLocalObj() is null");
            return;
        }
    }
    
    /*
     * get where clause
     */
    public abstract String getWhere(List<DyadicData> listData);

    /**
     * get order-by clause
     */
    public abstract String getOrderBy(QuerySummary qs);
}
