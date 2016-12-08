package com.rockchips.mediacenter.viewutils.animgridview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Layout.Alignment;
import android.text.TextPaint;
import android.view.View;

import com.rockchips.mediacenter.viewutils.textdrawer.SmartLayoutTextDrawer;

/**
 * 横向列表控件的子item，任何自定义的子项都必须从这个继承，注意这个类并不是一个真正的view
 * @author yKF76250
 * 
 */
public abstract class HWListItemView
{
    /**
     * 父View，主要是横向列表控件
     */
    private View mParentView;

    /**
     * 子项的宽
     */
    private int mWidth;

    /**
     * 子项的高
     */
    private int mHeight;

    // added zkf61715 是否被选中
    protected boolean mChooseState;

    public void setChooseState(boolean chooseState)
    {
        this.mChooseState = chooseState;
        // zkf61715 状态改变时及时刷新
        invalidate();
    }

    public boolean getChooseState()
    {
        return mChooseState;
    }

    /**
     * 设置子项的父控件
     * @param view
     */
    protected final void setParentViewContainer(View view)
    {
        mParentView = view;
    }

    /**
     * 刷新显示
     */
    protected final void invalidate()
    {
        if (mParentView != null)
        {
            mParentView.invalidate();
        }
    }

    public HWListItemView(Context context)
    {
    }

    /**
     * 设置子项的大小
     * @param width
     * @param height
     */
    protected final void initSize(int width, int height)
    {
        this.mWidth = width;
        this.mHeight = height;
    }

    /**
     * 获取子项的高，注意这个高度可能为0
     * @return
     */
    public final int getHeight()
    {
        return mHeight;
    }

    /**
     * 获取子项的宽，这个宽可能为0
     * @return
     */
    public final int getWidth()
    {
        return mWidth;
    }

    /**
     * 子View自己绘制自己
     * @param isFocus
     * @param canvas
     * @param parentHasFocus
     */
    public abstract void draw(boolean isFocus, Canvas canvas, boolean parentHasFocus, Paint paint);

    /**
     * 给子View提供一个自己清理自身内部资源的机会
     */
    public abstract void recyle();

    public abstract boolean showBig();

    public abstract Bitmap getDrawBitmap();
    
    protected TextPaint getPaintBySizeAndColor(int textSize, int colorResId)
    {
        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(textSize);
        textPaint.setColor(colorResId);
        return textPaint;
    }

    protected void drawText(Canvas canvas, TextPaint paint, String text, Rect rect)
    {
        if (null == rect)
        {
            return;
        }
        canvas.save();
        canvas.clipRect(rect);
        canvas.translate(rect.left, rect.top);

        SmartLayoutTextDrawer sltd = new SmartLayoutTextDrawer();
        sltd.setPaint(paint);
        sltd.setAlignment(Alignment.ALIGN_CENTER);
        sltd.setEllipsis(".");
        sltd.setMaxLens(1);
        sltd.setOrignalText(text == null ? "unknown" : text);
        sltd.setMaxLen(150);

        sltd.draw(canvas);
        canvas.restore();
    }
}
