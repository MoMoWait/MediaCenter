package com.rockchips.mediacenter.bean;


public class SubInfo
{
    //字幕id
    private int subid = -1;
    
    //字幕标题
    private String title = null;
    
    //是否为外置字幕
    private boolean bExtra = false;

    private String format = "";
    
    private String language = "";
    /**
     * @return 返回 subid
     */
    public int getSubid()
    {
        return subid;
    }

    /**
     * @param 对subid进行赋值
     */
    public void setSubid(int subid)
    {
        this.subid = subid;
    }

    /**
     * @return 返回 title
     */
    public String getTitle()
    {
        return getFormat()+" "+getLanguage();
    }

    /**
     * @return 返回 isExtra
     */
    public boolean isExtra()
    {
        return bExtra;
    }

    /**
     * @param 对isExtra进行赋值
     */
    public void setIsExtra(boolean b)
    {
        this.bExtra =b ;
    }

    /**
     * @return 返回 format
     */
    public String getFormat()
    {
        return format;
    }

    /**
     * @param 对format进行赋值
     */
    public void setFormat(String format)
    {
        this.format = format;
    }

    /**
     * @return 返回 language
     */
    public String getLanguage()
    {
        return language;
    }

    /**
     * @param 对language进行赋值
     */
    public void setLanguage(String language)
    {
        this.language = language;
    }
    
    
}
