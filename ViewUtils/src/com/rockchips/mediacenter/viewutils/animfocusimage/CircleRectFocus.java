package com.rockchips.mediacenter.viewutils.animfocusimage;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class CircleRectFocus
{
    private Bitmap mBmpLeftTop;
    
    private Bitmap mBmpRightTop;
    
    private Bitmap mBmpLeftBottom;
    
    private Bitmap mBmpRightBottom;
    
    private Bitmap mBmpLeft;
    
    private Bitmap mBmpTop;
    
    private Bitmap mBmpRight;
    
    private Bitmap mBmpBottom;
    
    private Bitmap mBmpContext ;
    
    public CircleRectFocus()
    {
    }
    
    public void setRect(Rect rect)
    {
        mRect = rect;
    }
    
    private Rect mRect = new Rect();
    
    public Rect getRect()
    {
        return mRect;
    }
    
    public void setL(int i)
    {
        getRect().left = i;
    }
    
    public void setR(int i)
    {
        getRect().right = i;
    }
    
    public void setT(int i)
    {
        getRect().top = i;
    }
    
    public void setB(int i)
    {
        getRect().bottom = i;
    }
    
    /**
     * @return the bmpLeftTop
     */
    public Bitmap getBmpLeftTop()
    {
        return mBmpLeftTop;
    }
    
    /**
     * @param bmpLeftTop the bmpLeftTop to set
     */
    public void setBmpLeftTop(Bitmap bmpLeftTop)
    {
        mBmpLeftTop = bmpLeftTop;
    }
    
    /**
     * @return the bmpRightTop
     */
    public Bitmap getBmpRightTop()
    {
        return mBmpRightTop;
    }
    
    /**
     * @param bmpRightTop the bmpRightTop to set
     */
    public void setBmpRightTop(Bitmap bmpRightTop)
    {
        mBmpRightTop = bmpRightTop;
    }
    
    /**
     * @return the bmpLeftBottom
     */
    public Bitmap getBmpLeftBottom()
    {
        return mBmpLeftBottom;
    }
    
    /**
     * @param bmpLeftBottom the bmpLeftBottom to set
     */
    public void setBmpLeftBottom(Bitmap bmpLeftBottom)
    {
        mBmpLeftBottom = bmpLeftBottom;
    }
    
    /**
     * @return the bmpRightBottom
     */
    public Bitmap getBmpRightBottom()
    {
        return mBmpRightBottom;
    }
    
    /**
     * @param bmpRightBottom the bmpRightBottom to set
     */
    public void setBmpRightBottom(Bitmap bmpRightBottom)
    {
        mBmpRightBottom = bmpRightBottom;
    }
    
    /**
     * @return the bmpLeft
     */
    public Bitmap getBmpLeft()
    {
        return mBmpLeft;
    }
    
    /**
     * @param bmpLeft the bmpLeft to set
     */
    public void setBmpLeft(Bitmap bmpLeft)
    {
        mBmpLeft = bmpLeft;
    }
    
    /**
     * @return the bmpTop
     */
    public Bitmap getBmpTop()
    {
        return mBmpTop;
    }
    
    /**
     * @param bmpTop the bmpTop to set
     */
    public void setBmpTop(Bitmap bmpTop)
    {
        mBmpTop = bmpTop;
    }
    
    /**
     * @return the bmpRight
     */
    public Bitmap getBmpRight()
    {
        return mBmpRight;
    }
    
    /**
     * @param bmpRight the bmpRight to set
     */
    public void setBmpRight(Bitmap bmpRight)
    {
        mBmpRight = bmpRight;
    }
    
    /**
     * @return the bmpBottom
     */
    public Bitmap getBmpBottom()
    {
        return mBmpBottom;
    }
    
    /**
     * @param bmpBottom the bmpBottom to set
     */
    public void setBmpBottom(Bitmap bmpBottom)
    {
        mBmpBottom = bmpBottom;
    }
    
    public void initBitmap(Bitmap bmp_left_top, Bitmap bmp_right_top, Bitmap bmp_left_bottom, Bitmap bmp_right_bottom,
        Bitmap bmp_left, Bitmap bmp_top, Bitmap bmp_right, Bitmap bmp_bottom)
    {
        mBmpLeftTop = bmp_left_top;
        mBmpRightTop = bmp_right_top;
        mBmpLeftBottom = bmp_left_bottom;
        mBmpRightBottom = bmp_right_bottom;
        mBmpLeft = bmp_left;
        mBmpTop = bmp_top;
        mBmpRight = bmp_right;
        mBmpBottom = bmp_bottom;
    }
    
    private float mAlpha = 0f;
    
    public void setAlpha(float alpha)
    {
        this.mAlpha = alpha;
        mPaint.setAlpha((int)((mAlpha * 255f) + .5f));
    }
    
    private Paint mPaint = new Paint();
    
    private Rect mDstTop = new Rect();
    
    private Rect mDstLeft = new Rect();
    
    private Rect mDstBtm = new Rect();
    
    private Rect mDstRight = new Rect();
    
    private Rect mContextRect = new Rect();
    
    public void onDraw(Canvas canvas)
    {
        canvas.save();
        //top
        mDstTop.left = getRect().left + getBmpLeftTop().getWidth();
        mDstTop.top = getRect().top;
        mDstTop.right = getRect().right - getBmpRightTop().getWidth();
        mDstTop.bottom = mDstTop.top + getBmpLeftTop().getHeight();
        canvas.drawBitmap(getBmpTop(), null, mDstTop, mPaint);
        
        
        //left
        mDstLeft.right = mDstTop.left;
        mDstLeft.left = getRect().left;
        mDstLeft.top = mDstTop.bottom;
        mDstLeft.bottom = getRect().bottom - getBmpRightBottom().getHeight();
        canvas.drawBitmap(getBmpLeft(), null, mDstLeft, mPaint);
        
        //bottom
        mDstBtm.top = mDstLeft.bottom;
        mDstBtm.left = mDstLeft.right;
        mDstBtm.right = mDstTop.right;
        mDstBtm.bottom = getRect().bottom;
        canvas.drawBitmap(getBmpBottom(), null, mDstBtm, mPaint);
        
        //Right
        mDstRight.top = mDstLeft.top;
        mDstRight.left = mDstTop.right;
        mDstRight.right = getRect().right;
        mDstRight.bottom = mDstLeft.bottom;
        canvas.drawBitmap(getBmpRight(), null, mDstRight, mPaint);
        
        //左上角
        canvas.drawBitmap(getBmpLeftTop(), getRect().left, getRect().top, mPaint);
        //右上角
        canvas.drawBitmap(getBmpRightTop(), mDstTop.right, getRect().top, mPaint);
        //左下角
        canvas.drawBitmap(getBmpLeftBottom(), getRect().left, mDstLeft.bottom, mPaint);
        //右下角
        canvas.drawBitmap(getBmpRightBottom(), mDstBtm.right, mDstRight.bottom, mPaint);
        
        canvas.drawBitmap(getBmpContext(), mDstTop.left, mDstLeft.top, mPaint);
        
        mContextRect.left = mDstTop.left;
        mContextRect.top = mDstLeft.top;
        mContextRect.right = mDstBtm.right;
        mContextRect.bottom = mDstRight.bottom;
        
        canvas.drawBitmap(getBmpContext(), null, mContextRect, mPaint);
        
        canvas.restore();
    }

    public Bitmap getBmpContext()
    {
        return mBmpContext;
    }

    public void setBmpContext(Bitmap bmpContext)
    {
        mBmpContext = bmpContext;
    }
}
