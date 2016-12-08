/**
 * Title: ALImageBrowserAdapater.java<br>
 * Package: com.rockchips.mediacenter.listadapter<br>
 * Description: TODO<br>
 * @author w00190739
 * @version v1.0<br>
 * Date: 2014-7-15下午2:53:28<br> 
 * Copyright © Huawei Technologies Co., Ltd. 2014. All rights reserved.
 */

package com.rockchips.mediacenter.listadapter;

import java.util.List;

import android.content.Context;

import com.rockchips.mediacenter.basicutils.bean.LocalMediaInfo;
import com.rockchips.mediacenter.basicutils.util.IICLOG;
import com.rockchips.mediacenter.viewutils.animgridview.HWListItemView;
import com.rockchips.mediacenter.viewutils.animgridview.HWListViewBaseAdapter;

/**
 * Description: TODO<br>
 * @author w00190739
 * @version v1.0 Date: 2014-7-15 下午2:53:28<br>
 */

public class ALImageBrowserAdapater extends HWListViewBaseAdapter
{
    private static final IICLOG Log = IICLOG.getInstance();

    private static final String TAG = "MediaCenterApp";

    public ALImageBrowserAdapater(Context context)
    {        
    }

    @Override
    public HWListItemView getView(Context context, int position, HWListItemView recyledView)
    {
        Log.d(TAG, "AlImageBrowserAdapater  ------------- getView");
        if (recyledView == null)
        {
            Log.d(TAG, "recyledView == null");
            recyledView = new ALImageBrowserItemView(context);
        }
        else
        {
            Log.d(TAG, "recyledView != null");
            if (!(recyledView instanceof ALImageBrowserItemView))
            {
                Log.d(TAG, "recyledView instanceof AlImageBrowserItemView == false");
                recyledView = new ALImageBrowserItemView(context);
            }
        }

        int size = getList().size();

        if (position >= size)
        {
            Log.d(TAG, position + ">=" + size);
            return recyledView;
        }
        else
        {
            Log.d(TAG, "----->" + position + "<" + size);
            LocalMediaInfo mi = null;
            mi = getList().get(position);
            ((ALImageBrowserItemView) recyledView).setMediaInfo(mi);
        }

        return recyledView;
    }

    private List<LocalMediaInfo> mList = null;

    public void setList(List<LocalMediaInfo> list)
    {
        mList = list;
    }

    public List<LocalMediaInfo> getList()
    {
        return mList;
    }

    public int getCount()
    {
        if (null == mList)
        {
            return 0;
        }
        else
        {
            return mList.size();
        }
    }

}
