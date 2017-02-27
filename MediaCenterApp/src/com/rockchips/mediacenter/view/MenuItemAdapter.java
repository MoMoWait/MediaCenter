package com.rockchips.mediacenter.view;

import java.util.List;

import android.content.Context;

import com.rockchips.mediacenter.R;
import com.rockchips.mediacenter.view.AbstractWheelTextAdapter;
import com.rockchips.mediacenter.view.WheelView;

/**
 * 菜单项数据适配
 * @author s00211113
 *
 */
public class MenuItemAdapter extends AbstractWheelTextAdapter
{
    private List<MenuItemImpl> mTypeItemList;
    
    private boolean mTtypeFirstLoad;
    
    private WheelView mTypeFilter;
    public void setTypeItemList(List<MenuItemImpl> itemList)
    {
        this.mTypeItemList = itemList;
    }

    public MenuItemAdapter(Context context, List<MenuItemImpl> list)
    {
        super(context, R.layout.option_type_layout, NO_RESOURCE);
        setItemTextResource(R.id.item_name);
        mTypeItemList = list;
        mTtypeFirstLoad = false;
    }

    @Override
    public int getItemsCount()
    {
        return mTypeItemList != null ? mTypeItemList.size() : 0;
    }

    @Override
    protected CharSequence getItemText(int index)
    {
        return mTypeItemList.get(index).getTitle();
    }
    
    public List<MenuItemImpl> getTypeItemList()
    {
        return mTypeItemList;
    }
    public WheelView getTypeFilter()
    {
        return mTypeFilter;
    }
    public void setTypeFilter(WheelView typeFilter)
    {
        this.mTypeFilter = typeFilter;
    }
    public boolean isTypeFirstLoad()
    {
        return mTtypeFirstLoad;
    }
    public void setTypeFirstLoad(boolean typeFirstLoad)
    {
        this.mTtypeFirstLoad = typeFirstLoad;
    }
    
}
