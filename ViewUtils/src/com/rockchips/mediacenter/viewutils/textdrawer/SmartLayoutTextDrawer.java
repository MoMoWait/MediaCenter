package com.rockchips.mediacenter.viewutils.textdrawer;

import android.graphics.Canvas;
import android.text.Layout;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;

/**
 * 
 * 文本画图器 1、根据输入的画笔画文字 2、根据允许的绘图区长度，多行显示 3、如果显示不全，那么就以掺入的ELLIPSIS结尾显示
 * 
 * "如果显示不全，那么就以掺入的ELLIPSIS结尾显示"
 * 
 * @author t00181037
 * @version [版本号, 2013-1-28]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class SmartLayoutTextDrawer
{
    // 结束符
    private String mEllipsis = "...";

    // 多个艺术家用分隔符隔开，数据源默认用','分隔
    private char mDefaultArtistSeparator = ',';

    // 根据显示要求，更改多个艺术家分隔符为'、'。
    private char mArtistSeparator = '、';

    // 画笔，有默认值
    private TextPaint mPaint = new TextPaint();

    // 间距：1.0f * 字体大小
    private float mLineSpacingMultiplier = 1.0f;

    private static final float LINEADDITIONALVERTICALPADDING_DEFAULT = 0.0f;
    // 段落间距
    private float mLineAdditionalVerticalPadding = LINEADDITIONALVERTICALPADDING_DEFAULT;

    private static final int MAX_LEN_DEFAULT = 200;
    // 允许的一行最大空间 px
    private int mMaxLen = MAX_LEN_DEFAULT;

    // 画文字时，对齐方式
    private Alignment mAlignment = Alignment.ALIGN_CENTER;

    // 显示的文本方式,mProcessedText经过处理的文本
    private String mProcessedText = "";

    // 原始的文本，
    private String mOrignalText = "";

    // 最大能显示的函数：默认是2
    private int mMaxLens = 2;

    private boolean mbAR;

    public SmartLayoutTextDrawer()
    {
    }

    /**
     * 开始画图 <功能详细描述>
     * @param canvas
     * @see [类、类#方法、类#成员]
     */
    public void draw(Canvas canvas)
    {
        Layout layout = getTextLayout();
        if (layout != null)
        {
            layout.draw(canvas);
        }
    }

    /**
     * 设置是否是阿拉伯地区标志
     * */
    public void setARflag(boolean arFlag)
    {
        mbAR = arFlag;
    }

    /**
     * 
     * <功能详细描述>
     * @see [类、类#方法、类#成员]
     */
    /**
     * 根据允许显示的长宽，函数 对字符串处理 <功能详细描述>
     * @return layout句柄
     * @see [类、类#方法、类#成员]
     */
    private static final int MAX_NUMBER = 20;
    public Layout getTextLayout()
    {

        String workingText = getProcessedText();

        int maxLines = getMaxLens();

        // 需要被返回的句柄
        Layout layout = null;

        // 截取详细过程
        if (maxLines != -1)
        {
            Layout layout1 = createWorkingLayout(workingText);
            layout = layout1;

            if (layout1.getLineCount() > maxLines)
            {
                if (getProcessedText().length() > MAX_NUMBER)
                {
                    workingText = getProcessedText().substring(0, MAX_NUMBER).trim();
                }
                else
                {
                    workingText = getProcessedText().trim();
                }
                
                if (workingText.length() > MAX_NUMBER)
                {
                    workingText = workingText.substring(0, MAX_NUMBER);
                }
                if (mbAR)
                {
                    workingText = getEllipsis() + workingText;
                }
                else
                {
                    workingText = workingText + getEllipsis();
                }
            }
        }

        if (!workingText.equals(getProcessedText()))
        {
            setProcessedText(workingText);

            // 内容超了就需要重新获取
            layout = createWorkingLayout(getProcessedText());
        }

        return layout;
    }

    /**
     * 获取layout <功能详细描述>
     * @param workingText
     * @return
     * @see [类、类#方法、类#成员]
     */
    private Layout createWorkingLayout(String workingText)
    {
        if (getMaxLen() < 0)
        {
            mMaxLen = 0;
        }
        if (workingText == null)
        {
            workingText = "";
        }
        return new StaticLayout(workingText, getPaint(), getMaxLen(), getAlignment(), getLineSpacingMultiplier(), getLineSpacingMultiplier(), true);
    }

    /**
     * @return 返回 mEllipsis
     */
    public String getEllipsis()
    {
        return mEllipsis;
    }

    /**
     * @param 对mEllipsis进行赋值
     */
    public void setEllipsis(String ellipsis)
    {
        this.mEllipsis = ellipsis;
    }

    /**
     * @return 返回 mPaint
     */
    public TextPaint getPaint()
    {
        return mPaint;
    }

    /**
     * @param 对mPaint进行赋值
     */
    public void setPaint(TextPaint paint)
    {
        this.mPaint = paint;
    }

    /**
     * @return 返回 mLineSpacingMultiplier
     */
    public float getLineSpacingMultiplier()
    {
        return mLineSpacingMultiplier;
    }

    /**
     * @param 对mLineSpacingMultiplier进行赋值
     */
    public void setLineSpacingMultiplier(float lineSpacingMultiplier)
    {
        this.mLineSpacingMultiplier = lineSpacingMultiplier;
    }

    /**
     * @return 返回 mLineAdditionalVerticalPadding
     */
    public float getLineAdditionalVerticalPadding()
    {
        return mLineAdditionalVerticalPadding;
    }

    /**
     * @param 对mLineAdditionalVerticalPadding进行赋值
     */
    public void setLineAdditionalVerticalPadding(float lineAdditionalVerticalPadding)
    {
        this.mLineAdditionalVerticalPadding = lineAdditionalVerticalPadding;
    }

    /**
     * @return 返回 mMaxLen
     */
    public int getMaxLen()
    {
        return mMaxLen;
    }

    /**
     * @param 对mMaxLen进行赋值
     */
    public void setMaxLen(int maxLen)
    {
        this.mMaxLen = maxLen;
    }

    /**
     * @return 返回 mAlignment
     */
    public Alignment getAlignment()
    {
        return mAlignment;
    }

    /**
     * @param 对mAlignment进行赋值
     */
    public void setAlignment(Alignment alignment)
    {
        this.mAlignment = alignment;
    }

    /**
     * @return 返回 mProcessedText
     */
    public String getProcessedText()
    {
        return mProcessedText;
    }

    /**
     * @param 对mProcessedText进行赋值
     */
    private void setProcessedText(String processedText)
    {
        this.mProcessedText = processedText;
    }

    /**
     * @return 返回 mOrignalText
     */
    public String getOrignalText()
    {
        return mOrignalText;
    }

    /**
     * @param 对mOrignalText进行赋值
     */
    public void setOrignalText(String orignalText)
    {
        if (orignalText == null)
        {
            return;
        }

        // add by zengxiaowen
        // 多个艺术家时，数据源 默认是用','号隔开，改为'、'号隔开。
        orignalText = orignalText.replace(mDefaultArtistSeparator, mArtistSeparator);

        setProcessedText(orignalText);
        this.mOrignalText = orignalText;
    }

    /**
     * @return 返回 mMaxLens
     */
    public int getMaxLens()
    {
        return mMaxLens;
    }

    /**
     * @param 对mMaxLens进行赋值
     */
    public void setMaxLens(int maxLens)
    {
        this.mMaxLens = maxLens;
    }

}
