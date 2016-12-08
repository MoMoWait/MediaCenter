package com.rockchips.mediacenter.viewutils.menu;

import java.util.ArrayList;


/**
 * <一句话功能简述>
 *  封装菜单每个分类
 * <功能详细描述>
 *  包括分类名，及分类下的选项
 * @author  zWX160481
 * @version  [版本号, 2013-3-18]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class MenuCategory
{
    /**
     *  分类名 
     */
    private String mCategoryName;
    
    /**
     *  分类下的选项集 
     */
    private ArrayList<MenuItemImpl> mMenuItems;
    
    /**
     *  默认选中的索引
     */
    private int mSelectIndex;
    
    /**
     *  是否响应Wheel 的OnItemFocusChangedListener 默认为不响应滚动监听
     */
    private boolean mbItemFocusChanged;
    
    public MenuCategory()
    {
        mbItemFocusChanged = false;
    }

    public String getCategoryName()
    {
        return mCategoryName;
    }

    public void setCategoryName(String mCategoryName)
    {
        this.mCategoryName = mCategoryName;
    }

    public ArrayList<MenuItemImpl> getMenuItems()
    {
        return mMenuItems;
    }

    public void setMenuItems(ArrayList<MenuItemImpl> mMenuItems)
    {
        this.mMenuItems = mMenuItems;
    }

    public int getSelectIndex()
    {
        return mSelectIndex;
    }

    public void setSelectIndex(int mSelectIndex)
    {
        this.mSelectIndex = mSelectIndex;
    }

    public boolean isMbItemFocusChanged()
    {
        return mbItemFocusChanged;
    }

    public void setItemFocusChanged(boolean mbItemFocusChanged)
    {
        this.mbItemFocusChanged = mbItemFocusChanged;
    }

	@Override
	public String toString() {
		return "MenuCategory [mCategoryName=" + mCategoryName + ", mMenuItems="
				+ mMenuItems + ", mSelectIndex=" + mSelectIndex
				+ ", mbItemFocusChanged=" + mbItemFocusChanged + "]";
	}
    
    
    
}
