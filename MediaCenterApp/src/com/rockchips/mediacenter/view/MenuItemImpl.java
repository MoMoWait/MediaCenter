package com.rockchips.mediacenter.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.ActionProvider;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

/**
 * 弹出菜单的每个item
 * @author zwx160481
 * 
 * */
public class MenuItemImpl implements MenuItem
{
    /**
     * 标志item的id
     * */
    private int mId;
    
    /**
     * 为区分该item 属于哪个类，相同类的 mGroupId相同
     * */
    private int mGroupId;
    
    /**
     *  标志item的枚举类�?
     */
    private Object mSelectType;
    
    /**
     * item显示顺序标志,越小越靠前显�?
     */
    private int mOrder;
    
    /**
     * item 显示title
     */
    private CharSequence mTitle;
    
    /**
     * 摘要
     * */
    private CharSequence mTitleCondensed;
    
    /**
     * 上下文
     * */
    private Context mContext;
    
    private Intent mIntent;
    
    /** 
     * 图片资源
     */
    private Drawable mIconDrawable;
    
    /**
     * 图片资源的id
     */
    private int mIconResId = NO_ICON;
    
    /** Used for the icon resource ID if this item does not have an icon */
    static final int NO_ICON = 0;
    
    public MenuItemImpl(Context context, int id, Object selectType, int iconresId, int groupId, int order,
        CharSequence title)
    {
        mContext = context;
        mId = id;
        mSelectType = selectType;
        mIconResId = iconresId;
        mIconDrawable = null;
        mGroupId = groupId;
        mOrder = order;
        mTitle = title;
    }
    
    public MenuItemImpl()
    {
    }
    
    public Object getSelectType()
    {
        return mSelectType;
    }
    
    public void setSelectType(Object mSelectType)
    {
        this.mSelectType = mSelectType;
    }
    
    public int getItemId()
    {
        return mId;
    }
    
    public int getGroupId()
    {
        return mGroupId;
    }
    
    public int getOrder()
    {
        return mOrder;
    }
    
    public MenuItem setTitle(CharSequence title)
    {
        mTitle = title;
        return this;
    }
    
    public MenuItem setTitle(int title)
    {
        return setTitle(mContext.getString(title));
    }
    
    public CharSequence getTitle()
    {
        return mTitle;
    }
    
    public MenuItem setTitleCondensed(CharSequence title)
    {
        mTitleCondensed = title;
        
        return this;
    }
    
    public CharSequence getTitleCondensed()
    {
        return mTitleCondensed;
    }
    
    public MenuItem setIcon(Drawable icon)
    {
        mIconResId = NO_ICON;
        mIconDrawable = icon;
        return this;
    }
    
    public MenuItem setIcon(int iconResId)
    {
        mIconDrawable = null;
        mIconResId = iconResId;
        return this;
    }
    
    public Drawable getIcon()
    {
        if (mIconDrawable != null)
        {
            return mIconDrawable;
        }
        
        if (mIconResId != NO_ICON)
        {
            return mContext.getResources().getDrawable(mIconResId);
        }
        return null;
    }
    
    public int getmIconResId()
    {
        return mIconResId;
    }
    
    public MenuItem setIntent(Intent intent)
    {
        mIntent = intent;
        return this;
    }
    
    public Intent getIntent()
    {
        return mIntent;
    }

    @Override
    public boolean collapseActionView()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean expandActionView()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public ActionProvider getActionProvider()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public View getActionView()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public char getAlphabeticShortcut()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public ContextMenuInfo getMenuInfo()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public char getNumericShortcut()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public SubMenu getSubMenu()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean hasSubMenu()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isActionViewExpanded()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isCheckable()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isChecked()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isEnabled()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isVisible()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public MenuItem setActionProvider(ActionProvider actionProvider)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MenuItem setActionView(View view)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MenuItem setActionView(int resId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MenuItem setAlphabeticShortcut(char alphaChar)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MenuItem setCheckable(boolean checkable)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MenuItem setChecked(boolean checked)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MenuItem setEnabled(boolean enabled)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MenuItem setNumericShortcut(char numericChar)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MenuItem setOnActionExpandListener(OnActionExpandListener listener)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MenuItem setOnMenuItemClickListener(OnMenuItemClickListener menuItemClickListener)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MenuItem setShortcut(char numericChar, char alphaChar)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setShowAsAction(int actionEnum)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public MenuItem setShowAsActionFlags(int actionEnum)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MenuItem setVisible(boolean visible)
    {
        return null;
    }

	@Override
	public String toString() {
		return "MenuItemImpl [mId=" + mId + ", mGroupId=" + mGroupId
				+ ", mSelectType=" + mSelectType + ", mOrder=" + mOrder
				+ ", mTitle=" + mTitle + ", mTitleCondensed=" + mTitleCondensed
				+ ", mContext=" + mContext + ", mIntent=" + mIntent
				+ ", mIconDrawable=" + mIconDrawable + ", mIconResId="
				+ mIconResId + "]";
	}

    
    
}
