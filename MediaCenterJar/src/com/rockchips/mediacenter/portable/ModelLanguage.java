package com.rockchips.mediacenter.portable;


public class ModelLanguage
{
    /** language abbreviation */
    private String mAbb;

    /** language */
    private String mLang;

    public ModelLanguage(String pAbb, String pLang)
    {
        mAbb = pAbb;
        mLang = pLang;
    }

    public String getAbb()
    {
        return mAbb;
    }

    public void setAbb(String pAbb)
    {
        mAbb = pAbb;
    }

    public String getLang()
    {
        return mLang;
    }

    public void setLang(String pLang)
    {
        mLang = pLang;
    }
}
