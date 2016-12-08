/**
 * Title: SqlQueryInfoInterface.java<br>
 * Package: com.rockchips.mediacenter.basicutils.provider<br>
 * Description: TODO<br>
 * @author w00190739
 * @version v1.0<br>
 * Date: 2014-7-11上午9:17:00<br> 
 * Copyright © Huawei Technologies Co., Ltd. 2014. All rights reserved.
 */

package com.rockchips.mediacenter.provider;

import android.database.Cursor;

/**
 * Description: TODO<br>
 * @author w00190739
 * @version v1.0
 * Date: 2014-7-11 上午9:17:00<br>
 */

public interface SqlQueryInfoInterface
{

//    /**
//     * 获取数据库URI
//     * @return 数据库URI
//     */
//    public Uri getUri();

    /**
     * 获取表列
     * @return 查询表的列
     */
    String[] getProjection();

    /**
     * 返回cursor当前指向的记录
     * @param cursor 不允许修改
     */
    void importRecord(final Cursor cursor);

}
