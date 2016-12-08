/**
 * Title: DiscShowAdapter.java<br>
 * Package: com.rockchips.mediacenter.listadapter<br>
 * Description: TODO<br>
 * @author c00224451
 * @version v1.0<br>
 * Date: 2014-7-15下午8:57:06<br> 
 * Copyright © Huawei Technologies Co., Ltd. 2014. All rights reserved.
 */

package com.rockchips.mediacenter.listadapter;

import java.util.List;

import android.content.Context;

import com.rockchips.mediacenter.R;
import com.rockchips.mediacenter.basicutils.bean.LocalDeviceInfo;
import com.rockchips.mediacenter.basicutils.bean.LocalMediaInfo;
import com.rockchips.mediacenter.basicutils.util.DiskUtil;
import com.rockchips.mediacenter.basicutils.util.ResLoadUtil;
import com.rockchips.mediacenter.viewutils.animgridview.HWListItemView;
import com.rockchips.mediacenter.viewutils.animgridview.HWListViewBaseAdapter;
import com.rockchips.mediacenter.listadapter.MediaListItemView.ENUMLISTITEMMODE;

/**
 * Description: TODO<br>
 * @author c00224451
 * @version v1.0
 * Date: 2014-7-15 下午8:57:06<br>
 */

public class DiskShowAdapter extends HWListViewBaseAdapter 
{    
    private List<LocalDeviceInfo> mDeviceList;
    public void setDevicesData(List<LocalDeviceInfo> devices)
    {
        mDeviceList = devices;
    }
    public List<LocalDeviceInfo> getDevicesData()
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

        if (getDevicesData() == null)
        {
            return 0;
        }
        return getDevicesData().size(); 
    }

    /** 
     * TODO
     * @param mContext
     * @param position
     * @param recyledView
     * @return
     * @throws
     * @see com.rockchips.mediacenter.viewutils.animgridview.HWListViewBaseAdapter#getView(android.content.Context, int, com.rockchips.mediacenter.viewutils.animgridview.HWListItemView)  
     */

    @Override
    public HWListItemView getView(Context context, int position, HWListItemView recyledView) 
    {

        if (getDevicesData() == null)
        {
            return recyledView;
        }

        if (getDevicesData().size() <= position)
        {
            return recyledView;
        }

        LocalDeviceInfo deviceInfo = getDevicesData().get(position);

        if (deviceInfo == null)
        {
            return recyledView;
        }

        MediaListItemView mliView = new MediaListItemView(context);
        mliView.setEnumMode(ENUMLISTITEMMODE.LIST_ITEM_MODE_SINGLE);        
        mliView.setItemIcon(ResLoadUtil.getBitmapByIdNoCache(context, R.drawable.icon_local_disk_thumb)); 
        
        mliView.setStrNameCol(DiskUtil.getDiskName(deviceInfo.getMountPath()));
        recyledView = mliView;
        return recyledView;      

    }

}
