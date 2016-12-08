package com.rockchips.mediacenter.viewutils.dynamicspotbg;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;

/**
 * 动态斑点
 * @author s00211113
 *
 */
public class DynSpot
{
    //动态点的位置
    public PointF point;
    
    //点移动趋式
    public PointF pointtrend;
    
    //点的透明度
    public Point alphascope;
    
    public int alpha;
    
    //透明度趋式(1-10)
    public int alphatrend;
    
    //点大小的缩放(0-1)
    public float scale;
    
    //缩放趋势(0.05-0.1)
    public float scaletrend;
    
    public Bitmap bmp;
    
    private int mRadius;
    
    private int mOutW;
    
    private int mOutH;
    
    /**
     * @return 返回 mRadius
     */
    public int getRadius()
    {
        return mRadius;
    }
    
    /**
     * @param 对mRadius进行赋值
     */
    public void setRadius(int mRadius)
    {
        this.mRadius = mRadius;
    }
    
    public DynSpot(int outw, int outh)
    {
        mOutW = outw;
        mOutH = outh;
    }
    
    DynSpot(PointF p, int outw, int outh)
    {
        point = p;
        
        mOutW = outw;
        mOutH = outh;
    }
    
    /**
     * 判断是否出界
     * <功能详细描述>
     * @param w
     * @param h
     * @param r
     * @return
     */
    private boolean isOutRange()
    {
        if (point.x <= -mRadius || point.y <= -mRadius || point.x >= mOutW + mRadius || point.y >= mOutH + mRadius)
        {
            return true;
        }
        
        return false;
    }
    
    /**
     * 判断是否alpha消失
     * <功能详细描述>
     * @param w
     * @param h
     * @param r
     * @return
     */
    private boolean isOutAlpha()
    {
        if (alpha <= 0)
        {
            return true;
        }
        
        return false;
    }
    
    /**
     * 是否要消失了
     * <功能详细描述>
     * @return
     */
    public boolean isDisappear()
    {
        if (alphascope.x <= 0)
        {
            if (isOutRange() || isOutAlpha())
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            if (isOutRange())
            {
                return true;
            }
            else
            {
                return false;
            }
        }
    }
    
    /**
     * 每一帧更新
     * <功能详细描述>
     * @param dbg
     */
    public void update(DynBg dbg)
    {
        if (isDisappear())
        {
            dbg.randomGenDyncPoint(this);
            alphatrend = Math.abs(alphatrend);
            alpha = 1;
            return;
        }
        
        if (alpha <= alphascope.x)
        {
            alphatrend = Math.abs(alphatrend);
            alpha += alphatrend;
        }
        else if (alpha >=  alphascope.y)
        {
            alphatrend = -Math.abs(alphatrend);
            alpha += alphatrend;
        }
        else
        {
            alpha += alphatrend;
        }
        
        point.x += pointtrend.x;
        point.y += pointtrend.y;
    }
    
}
