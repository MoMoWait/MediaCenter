package com.rockchips.mediacenter.viewutils.animgridview;

import android.content.Context;

/**
 * 横向列表控件的数据适配器
 * 
 */
public abstract class HWListViewBaseAdapter
{

    private HWAdapterView mAdapterView;

    /**
     * 数据发送了变化，通知控件
     */
    public final void notifyDataChanged()
    {
        if (mAdapterView != null)
        {
            mAdapterView.notifyDataChanged();
        }
    }

    /**
     * 数据发送变化，通知view重绘
     * @param adapterView
     */
    protected final void setAdapterView(HWAdapterView adapterView)
    {
        mAdapterView = adapterView;
    }

    /**
     * 获取总数据大小
     * @return
     */
    public abstract int getCount();

    /**
     * @param mContext
     * @param position
     * @param recyledView
     * @return
     */
    public abstract HWListItemView getView(Context mContext,
            int position, HWListItemView recyledView);

}
