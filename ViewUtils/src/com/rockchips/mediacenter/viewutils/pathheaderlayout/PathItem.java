package com.rockchips.mediacenter.viewutils.pathheaderlayout;

/**
 * <一句话功能简述>
 *      路径
 * <功能详细描述>
 * 
 * @author  zWX160481
 * @version  [版本号, 2013-3-25]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class PathItem
{
    /**
     * 路径名
     */
    private String mPahtName;
    /**
     * 间隔图片id
     */
    private int mResid;
    
    /**
     *  是否显示该路径 默认要显示
     */
    private boolean mbDisplay = true;
    
    public PathItem()
    {
    }

    public PathItem(String mPahtName, int mResid, boolean mbDisplay)
    {
        super();
        this.mPahtName = mPahtName;
        this.mResid = mResid;
        this.mbDisplay = mbDisplay;
    }

    public String getmPathName()
    {
        return mPahtName;
    }

    public void setmPahtName(String mPahtName)
    {
        this.mPahtName = mPahtName;
    }

    public int getmResid()
    {
        return mResid;
    }

    public void setmResid(int mResid)
    {
        this.mResid = mResid;
    }

    public boolean isDisplay()
    {
        return mbDisplay;
    }

    public void setDisplay(boolean mbDisplay)
    {
        this.mbDisplay = mbDisplay;
    }
    
    
}
