/**
 * Title: LocalDeviceDiskAdapter.java<br>
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

import com.rockchips.mediacenter.basicutils.bean.LocalDeviceInfo;
import com.rockchips.mediacenter.viewutils.animgridview.HWListItemView;
import com.rockchips.mediacenter.viewutils.animgridview.HWListViewBaseAdapter;
import com.rockchips.mediacenter.bean.LocalDevice;

/**
 * Description: TODO<br>
 * @author w00190739
 * @version v1.0 Date: 2014-7-15 下午2:53:28<br>
 */

public class LocalDeviceDiskAdapter extends HWListViewBaseAdapter
{
    private List<LocalDevice> mDeviceList;

    public void setDevicesData(List<LocalDevice> devices)
    {
        mDeviceList = devices;
    }

    public List<LocalDevice> getDevicesData()
    {
        return mDeviceList;
    }

    /**
     * TODO
     * @return
     * @throws
     * @see com.rockchips.mediacenter.viewutils.animgridview.HWListViewBaseAdapter#getCount()
     */

    @Override
    public int getCount()
    {

        if (mDeviceList == null)
        {
            return 0;
        }
        return mDeviceList.size();
    }

    /**
     * TODO
     * @param mContext
     * @param position
     * @param recyledView
     * @return
     * @throws
     * @see com.rockchips.mediacenter.viewutils.animgridview.HWListViewBaseAdapter#getView(android.content.Context, int,
     *      com.rockchips.mediacenter.viewutils.animgridview.HWListItemView)
     */

    @Override
    public HWListItemView getView(Context context, int position, HWListItemView recyledView) 
    {
        
        if (recyledView == null)
        {
            recyledView = new LocalDeviceDiskItemView(context);
        }
        else
        {
            if (!(recyledView instanceof LocalDeviceDiskItemView))
            {
                recyledView = new LocalDeviceDiskItemView(context);
            }
        }
        
        int size = getDevicesData().size();

        if (position >= size)
        {
            return recyledView;
        }
        else
        {
            LocalDevice mi = null;
            mi = getDevicesData().get(position);
            ((LocalDeviceDiskItemView) recyledView).setMediaInfo(mi);
        }
        return recyledView;

    }

}
