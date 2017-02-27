/**
 * Title: LocDevDiskProjectionProvider.java<br>
 * Package: com.rockchips.mediacenter.basicutils.provider<br>
 * Description: TODO<br>
 * @author w00190739
 * @version v1.0<br>
 * Date: 2014-7-10上午11:00:57<br> 
 * Copyright © Huawei Technologies Co., Ltd. 2014. All rights reserved.
 */
package com.rockchips.mediacenter.modle.db;

import java.util.List;

import android.database.Cursor;

import com.rockchips.mediacenter.bean.LocalDeviceInfo;
import com.rockchips.mediacenter.utils.SqlQueryTool;

/**
 * Description:本地设备及其分区的查询，提供数据对外接口,提供查询语句的封装<br>
 * @author w00190739
 * @version v1.0 Date: 2014-7-10 上午11:00:57<br>
 */

public class LocDevProjectionProvider extends ProjectionProvider
{
//
//    /**
//     * 组织设备查询语句的返回值
//     * @return 游标的列的组合
//     * @see [类、类#方法、类#成员]
//     */
//    @Override
//    public List<String> getProjList()
//    {
//        List<String> list = new ArrayList<String>();
//        return list;
//    }

    private static final int DEFAULT_STRING_BUILDER_CAPACITY = 128;
    
    /**
     * 解析语句的查询结果 解析游标
     * @param cursor 游标
     * @see [类、类#方法、类#成员]
     */
    @Override
    public void importRecord(final Cursor cursor)
    {
        Log.d(TAG, "LocDevProjectionProvider importRecord E");
        // 判断游标是否为NULL
        super.importRecord(cursor);

        LocalDeviceInfo devInfo = new LocalDeviceInfo();
        // 设备类型：SD卡，USB设备
        devInfo.setDeviceType(SqlQueryTool.getIntColumn(LocalDeviceInfo.DEVICE_TYPE, cursor));
        // 设备容量及其使用情况(设备发现时不使用)
        devInfo.setTotalSize(SqlQueryTool.getStringColumn(LocalDeviceInfo.TOTAL_SIZE, cursor));
        devInfo.setFreeSize(SqlQueryTool.getStringColumn(LocalDeviceInfo.FREE_SIZE, cursor));
        devInfo.setUsedSize(SqlQueryTool.getStringColumn(LocalDeviceInfo.USED_SIZE, cursor));
        devInfo.setPhysicId(SqlQueryTool.getStringColumn(LocalDeviceInfo.PHYSIC_ID, cursor));
        devInfo.setIsPhysicDev(SqlQueryTool.getIntColumn(LocalDeviceInfo.IS_PHYSIC_DEV, cursor));
        devInfo.setUsedPercent(SqlQueryTool.getStringColumn(LocalDeviceInfo.USED_PERCENT, cursor));
        devInfo.setMountPath(SqlQueryTool.getStringColumn(LocalDeviceInfo.MOUNT_PATH, cursor));
        devInfo.setIsScanned(SqlQueryTool.getIntColumn(LocalDeviceInfo.IS_SCANNED, cursor));
        setLocalObj(devInfo);
        
        Log.d(TAG, "LocDevProjectionProvider importRecord X devInfo = " + devInfo.getMountPath());
    }

    @Override
    public String getWhere(List<DyadicData> listData)
    {
        if (listData != null && listData.size() > 0)
        {
            return listData.get(0).getStrValue();
        }
        return null;
    }

    /**
     * 组装排序语句 通过设备类型进行排序
     * @param qs 参数
     * @return 排序语句
     * @see [类、类#方法、类#成员]
     */
    @Override
    public String getOrderBy(QuerySummary qs)
    {
        // 以设备类型进行排序，设备类型：SD卡，USB设备
        StringBuilder strOrderBy = new StringBuilder(DEFAULT_STRING_BUILDER_CAPACITY);

        return strOrderBy.toString();
    }

    @Override
    public void setLocalObj(Object localObj)
    {
        if (localObj instanceof LocalDeviceInfo)
        {
            super.setLocalObj(localObj);
        }
    }
}
