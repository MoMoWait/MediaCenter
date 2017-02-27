/**
 * Title: LocalData.java<br>
 * Package: com.rockchips.mediacenter.basicutils.provider<br>
 * Description: TODO<br>
 * @author w00190739
 * @version v1.0<br>
 * Date: 2014-7-11下午1:45:20<br> 
 * Copyright © Huawei Technologies Co., Ltd. 2014. All rights reserved.
 */

package com.rockchips.mediacenter.modle.db;

import java.util.List;

import android.database.Cursor;
import android.net.Uri;

import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.utils.SqlQueryTool;
import com.rockchips.mediacenter.utils.IICLOG;

/**
 * Description: TODO<br>
 * @author w00190739
 * @version v1.0 Date: 2014-7-11 下午1:45:20<br>
 */

public class LocalData implements SqlQueryInfoInterface
{

    protected static final String TAG = "LocalData";

    protected static final IICLOG Log = IICLOG.getInstance();

    protected ProjectionProvider mProjectionProvider;

    private Uri mUri;

    public LocalData(ProjectionProvider pp)
    {
        if (null == pp)
        {
            Log.e(TAG, "class LocalData function construction : input parameter pp is null || uri is null !");
            return;
        }
        mProjectionProvider = pp;
    }

    /**
     * 返回cursor当前指向的记录 implemented using the tool {@link SqlQueryTool}.
     * @param cursor 不允许修改
     */
    @Override
    public void importRecord(final Cursor cursor)
    {
        if (null == cursor)
        {
            Log.e(TAG, "class LocalData importRecord function importRecord : input parameter cursor is null");
            return;
        }

        if (null == mProjectionProvider)
        {
            Log.d(TAG, "class LocalData importRecord function getProjList : pp is null");
            return;
        }

        mProjectionProvider.importRecord(cursor);
    }

    /**
     * 返回查询记录的列名列表
     * @return 查询记录的列名列表
     */
    @Override
    public String[] getProjection()
    {
        if (null == mProjectionProvider)
        {
            Log.e(TAG, "class LocalData function getProjList : pp is null");
            return null;
        }
        List<String> strList = mProjectionProvider.getProjList();

        return (String[]) (strList.toArray(new String[strList.size()]));
    }

    public String getWhere(List<DyadicData> listData)
    {
        if (null == mProjectionProvider)
        {
            Log.e(TAG, "class LocalData function getwhere : pp is null");
            return null;
        }

        return mProjectionProvider.getWhere(listData);
    }

    public Uri getUri()
    {
        return mUri;
    }

    public void setUri(String pathSegment)
    {
        mUri = Uri.withAppendedPath(ConstData.URI.LOCAL_PROVIDER_URI, pathSegment);
    }
}
