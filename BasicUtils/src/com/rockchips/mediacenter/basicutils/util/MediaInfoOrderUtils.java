package com.rockchips.mediacenter.basicutils.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.rockchips.mediacenter.basicutils.bean.LocalMediaInfo;
import com.rockchips.mediacenter.basicutils.constant.Constant;

public class MediaInfoOrderUtils
{
    private static final String TAG = "MediaInfoOrderUtils";

    private static final IICLOG Log = IICLOG.getInstance();

    public List<LocalMediaInfo> getOrderedList(Constant.EBrowerType orderType, List<LocalMediaInfo> list)
    {
        Log.d(TAG, "orderType = " + orderType);
        if (Constant.EBrowerType.ORDER_TYPE_TIME == orderType)
        {
            OrderByTimeComparator comp = new OrderByTimeComparator();
            Collections.sort(list, comp);
        }
        else if (Constant.EBrowerType.ORDER_TYPE_CHARACTER == orderType)
        {
            OrderByCharacterComparator comp = new OrderByCharacterComparator();
            Collections.sort(list, comp);
        }

        return list;
    }

    private class OrderByTimeComparator implements Comparator<LocalMediaInfo>
    {
        @Override
        public int compare(LocalMediaInfo lhs, LocalMediaInfo rhs)
        {
            int ret = compareByDateModifiedStr(lhs, rhs);
            if (0 == ret)
            {
                return compareByPinyin(lhs, rhs);
            }

            return ret;
        }
    }

    /* BEGIN: Modified by s00211113 for DTS2014021404690 2014/2/26 */
    private class OrderByCharacterComparator implements Comparator<LocalMediaInfo>
    {
        @Override
        public int compare(LocalMediaInfo lhs, LocalMediaInfo rhs)
        {
            return compareByPinyin(lhs, rhs);
        }
    }

    private int compareByDateModifiedStr(LocalMediaInfo lhs, LocalMediaInfo rhs)
    {
        if (rhs.getmModifyDate() == 0 && lhs.getmModifyDate() == 0 && rhs.getmModifyDateStr() != null
                && lhs.getmModifyDateStr() != null)
        {
            return rhs.getmModifyDateStr().compareTo(lhs.getmModifyDateStr());
        }
        if (rhs.getmModifyDate() <= 0)
        {
            return -1;
        }
        else if (lhs.getmModifyDate() <= 0)
        {
            return 1;
        }
 //       return String.valueOf(rhs.getmModifyDate()).compareTo(String.valueOf(lhs.getmModifyDate()));

        /* BEGIN: Modified by zwx238093 for DTS2015012807517  2015/2/1 */
        return (rhs.getmModifyDate()- lhs.getmModifyDate() > 0) ? 1 : -1;
    }

    private int compareByPinyin(LocalMediaInfo lhs, LocalMediaInfo rhs)
    {
        if (rhs.getmPinyin() == null)
        {
            return -1;
        }
        else if (lhs.getmPinyin() == null)
        {
            return 1;
        }
        return lhs.getmPinyin().compareTo(rhs.getmPinyin());
    }
}
