package com.rockchips.mediacenter.view;

/**
 * 
 * Description: 列表项数据<br>
 * @author s00211113
 * @version v1.0
 * Date: 2014-7-23 下午3:23:21<br>
 */
public class ListSelectItem
{
    private String mName;

    private String mOther;

    private Object mObject;

    public ListSelectItem(String name, String other, Object object)
    {
        mName = name;
        mOther = other;
        mObject = object;
    }
    
    public void setName(String name)
    {
        mName = name;
    }
    
    public String getName()
    {
        return mName;
    }
    
    public void setOther(String other)
    {
        mOther = other;
    }
    
    public String getOther()
    {
        return mOther;
    }
    
    public void setObject(Object object)
    {
        mObject = object;
    }
    
    public Object getObject()
    {
        return mObject;
    }
}
