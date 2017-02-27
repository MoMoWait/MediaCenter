/**
 * Title: LocAVIProjectionProvider.java<br>
 * Package: com.rockchips.mediacenter.basicutils.provider<br>
 * Description: TODO<br>
 * @author w00190739
 * @version v1.0<br>
 * Date: 2014-7-14 上午9:34:18<br> 
 * Copyright © Huawei Technologies Co., Ltd. 2014. All rights reserved.
 */

package com.rockchips.mediacenter.modle.db;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;

import com.rockchips.mediacenter.bean.LocalMediaInfo;
import com.rockchips.mediacenter.utils.SqlQueryTool;
import com.rockchips.mediacenter.utils.StringUtils;

/**
 * Description: 本地音、视频及图片的DB查询接口类<br>
 * @author w00190739
 * @version v1.0 Date: 2014-7-14 上午9:34:18<br>
 */

public class LocAVIProjectionProvider extends ProjectionProvider
{
    private static final int DEFAULT_STRING_BUILDER_CAPACITY = 128;

    /**
     * get projection list of columns
     * @return projection list of columns
     */
    public List<String> getProjList()
    {
        Log.d(TAG, "Loc_Audio_ProjectionProvider getProjList E");
        List<String> list = new ArrayList<String>();
        SqlQueryTool.insertUniqElementIntoList(list, LocalMediaInfo.DATA);
        SqlQueryTool.insertUniqElementIntoList(list, LocalMediaInfo.FILE_NAME);
        SqlQueryTool.insertUniqElementIntoList(list, LocalMediaInfo.FILE_SIZE);
        SqlQueryTool.insertUniqElementIntoList(list, LocalMediaInfo.FILE_TYPE);
        SqlQueryTool.insertUniqElementIntoList(list, LocalMediaInfo.FILES);
        SqlQueryTool.insertUniqElementIntoList(list, LocalMediaInfo.FOLDERS);
        SqlQueryTool.insertUniqElementIntoList(list, LocalMediaInfo.MODIFY_DATE);
        SqlQueryTool.insertUniqElementIntoList(list, LocalMediaInfo.ORDER_FIELD);
        SqlQueryTool.insertUniqElementIntoList(list, LocalMediaInfo.PARENT_PATH);
        SqlQueryTool.insertUniqElementIntoList(list, LocalMediaInfo.PIN_YIN);
        SqlQueryTool.insertUniqElementIntoList(list, LocalMediaInfo.DEVICETYPE);
        SqlQueryTool.insertUniqElementIntoList(list, LocalMediaInfo.PHYSIC_ID);
		SqlQueryTool.insertUniqElementIntoList(list, LocalMediaInfo.DESCRIPTION);
        Log.d(TAG, "Loc_Audio_ProjectionProvider getProjList X");
        return list;
    }

    @Override
    public String getWhere(List<DyadicData> listData)
    {
        return getWhereClauseByList(listData);
    }

    @Override
    public String getOrderBy(QuerySummary qs)
    {
        StringBuilder strOrderBy = new StringBuilder(DEFAULT_STRING_BUILDER_CAPACITY);
        strOrderBy.append(LocalMediaInfo.MODIFY_DATE);
        return strOrderBy.toString();

    }

    /**
     * get all data of the row indicated by cursor implemented using the tool {@link SqlQueryTool}.
     * @param cursor
     * 
     */
    public void importRecord(final Cursor cursor)
    {
//        Log.d(TAG, "LocAVIProjectionProvider importRecord E");
        // 判断游标是否为NULL
        super.importRecord(cursor);

        LocalMediaInfo aviInfo = new LocalMediaInfo();
        aviInfo.setmData(SqlQueryTool.getStringColumn(LocalMediaInfo.DATA, cursor));
        aviInfo.setmFileName(SqlQueryTool.getStringColumn(LocalMediaInfo.FILE_NAME, cursor));
        aviInfo.setmFileSize(SqlQueryTool.getIntColumn(LocalMediaInfo.FILE_SIZE, cursor));
        aviInfo.setmFileType(SqlQueryTool.getIntColumn(LocalMediaInfo.FILE_TYPE, cursor));
        aviInfo.setmFiles(SqlQueryTool.getIntColumn(LocalMediaInfo.FILES, cursor));
        aviInfo.setmFolders(SqlQueryTool.getIntColumn(LocalMediaInfo.FOLDERS, cursor));
        aviInfo.setmOrderField(SqlQueryTool.getIntColumn(LocalMediaInfo.ORDER_FIELD, cursor));
        aviInfo.setmParentPath(SqlQueryTool.getStringColumn(LocalMediaInfo.PARENT_PATH, cursor));
        aviInfo.setmPinyin(SqlQueryTool.getStringColumn(LocalMediaInfo.PIN_YIN, cursor));
        aviInfo.setmModifyDate(SqlQueryTool.getIntColumn(LocalMediaInfo.MODIFY_DATE, cursor));
        aviInfo.setmDeviceType(SqlQueryTool.getIntColumn(LocalMediaInfo.DEVICETYPE, cursor));
        aviInfo.setmPhysicId(SqlQueryTool.getStringColumn(LocalMediaInfo.PHYSIC_ID, cursor));
		aviInfo.setmDescription(SqlQueryTool.getStringColumn(LocalMediaInfo.DESCRIPTION, cursor));
        setLocalObj(aviInfo);
//        Log.d(TAG, "LocAVIProjectionProvider importRecord X getmPhysicId = " + aviInfo.getmPhysicId());
//        Log.d(TAG, "LocAVIProjectionProvider importRecord X getmDeviceType = " + aviInfo.getmDeviceType());
    }

    @Override
    public void setLocalObj(Object localObj)
    {
        if (localObj instanceof LocalMediaInfo)
        {
            super.setLocalObj(localObj);
        }
    }

    /**
     * get where-clause by list of dyadicdata
     * @param listData
     * @return
     */
    protected String getWhereClauseByList(List<DyadicData> listData)
    {

        StringBuilder strWhere = new StringBuilder(DEFAULT_STRING_BUILDER_CAPACITY);

        if (null != listData)
        {
            int totalSize = listData.size();
            for (int size = 0; size < totalSize; size++)
            {
                DyadicData dd = listData.get(size);
                if (dd != null && StringUtils.isNotEmpty(dd.getStrName()))
                {
                    if (StringUtils.isNotEmpty(strWhere.toString()))
                    {
                        strWhere.append(" AND ");
                    }
                    strWhere.append(dd.getStrName());
                    strWhere.append(dd.getStrValue());
                }
            }
        }

        if (StringUtils.isNotEmpty(strWhere.toString()))
        {
            return strWhere.toString();
        }
        else
        {
            Log.d(TAG, "strWhere toString  is null.....");
            return null;
        }
    }
}
