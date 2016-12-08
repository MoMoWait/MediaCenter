package com.rockchips.mediacenter.viewutils.dynamicspotbg;

import java.security.InvalidParameterException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;

/**
 * 动态spot背景数据
 * @author t00181037
 * 
 */
public class DynBg
{
    private int mWidth;

    private int mHeight;

    private List<Bitmap> mBmpList;

    private int mSpotCount;

    private static final int SPOT_DEFAULT_COUNT = 10;

    private static final int GRADE_0 = 0;

    private static final int GRADE_1 = 1;

    private static final int GRADE_2 = 2;

    private static final int GRADE_3 = 3;

    private static final int GRADE_4 = 4;

    private static final int GRADE_5 = 5;

    private static final int POINT_1_X = 10;

    private static final int POINT_1_Y = 50;

    private static final int POINT_2_X = 10;

    private static final int POINT_2_Y = 80;

    private static final int POINT_3_X = 10;

    private static final int POINT_3_Y = 90;

    private static final int POINT_4_X = 130;

    private static final int POINT_4_Y = 150;

    private static final int POINT_5_X = 160;

    private static final int POINT_5_Y = 180;

    private static final int POINT_DEFAULT_X = 10;

    private static final int POINT_DEFAULT_Y = 50;

    private static final int ALPHA_1 = 20;

    private static final int ALPHA_2 = 50;

    private static final int ALPHA_3 = 60;

    private static final int ALPHA_4 = 120;

    private static final int ALPHA_5 = 150;

    private static final int ALPHA_DEFAULT = 90;

    private static final int ALPHA_PLUS = 20;
    
    private List<DynSpot> mDsList;

    private static final double POINT_TREND_DIVISION = 0.5;

    private static final float POINT_TREND_A = 1f;

    private static final float POINT_TREND_B = -1f;

    private static final double ALPHA_TREND_DIVISION = 0.8;
    
    private SecureRandom mSecureRandom;

    public DynBg(int bgw, int bgh, int nCount, List<Bitmap> srcbmplist)
    {
        mSecureRandom = new SecureRandom();
        if (bgw <= 0 || bgh <= 0 || srcbmplist == null)
        {
            throw new InvalidParameterException("DynBg input bg width or height is less than 0");
        }

        nCount = nCount > 0 ? nCount : SPOT_DEFAULT_COUNT;
        setWidth(bgw);
        setHeight(bgh);
        setBmpList(srcbmplist);

        setSpotCount(nCount);

        mDsList = randomGenDyncPoints();

    }


    /**
     * 在给定范围内，随机产生一个点
     * 
     * @return
     */
    DynSpot randomGenDyncPoint(DynSpot ds)
    {

        if (ds == null)
        {
            ds = new DynSpot(mWidth, mHeight);
        }

        PointF point = new PointF();
        point.x = (float) (mSecureRandom.nextDouble() * getWidth());
        point.y = (float) (mSecureRandom.nextDouble() * getHeight());
        ds.point = point;

        point = new PointF();
        point.x = mSecureRandom.nextDouble() > POINT_TREND_DIVISION ? POINT_TREND_A : POINT_TREND_B;
        point.y = mSecureRandom.nextDouble() > POINT_TREND_DIVISION ? POINT_TREND_A : POINT_TREND_B;
        ds.pointtrend = point;

        double rd = mSecureRandom.nextDouble();

        ds.alphascope = genAlphaScope(rd);
        ds.alpha = genAlpha(rd);
        ds.alphatrend = 1;
        ds.alphatrend = mSecureRandom.nextDouble() >= ALPHA_TREND_DIVISION ? ds.alphatrend : -ds.alphatrend;

        ds.bmp = mBmpList.get((int) (rd * mBmpList.size()));

        ds.setRadius(ds.bmp.getWidth());
        return ds;
    }

    private Point genAlphaScope(double rd)
    {
        int grade = (int) (mBmpList.size() * rd);

        switch (grade)
        {
            case GRADE_0:
                return new Point(POINT_1_X, POINT_1_Y);
            case GRADE_1:
                return new Point(POINT_2_X, POINT_2_Y);
            case GRADE_2:
            case GRADE_3:
                return new Point(POINT_3_X, POINT_3_Y);
                // return new Point(110,120);
            case GRADE_4:
                return new Point(POINT_4_X, POINT_4_Y);
            case GRADE_5:
                return new Point(POINT_5_X, POINT_5_Y);
            default:
                return new Point(POINT_DEFAULT_X, POINT_DEFAULT_Y);
        }
    }

    private int genAlpha(double rd)
    {
        int grade = (int) (mBmpList.size() * rd);

        switch (grade)
        {
            case GRADE_0:
                return ALPHA_1 + (int) (rd * ALPHA_PLUS);
            case GRADE_1:
                return ALPHA_2 + (int) (rd * ALPHA_PLUS);
            case GRADE_2:
            case GRADE_3:
                return ALPHA_3 + (int) (rd * ALPHA_PLUS);
                // return 90+ (int)(rd*30);
            case GRADE_4:
                return ALPHA_4 + (int) (rd * ALPHA_PLUS);
            case GRADE_5:
                return ALPHA_5 + (int) (rd * ALPHA_PLUS);
            default:
                return ALPHA_DEFAULT + (int) (rd * ALPHA_PLUS);
        }
    }

    public void randomChangeDyncPoint(DynSpot ds)
    {
        if (ds == null)
        {
            return;
        }

        PointF point = new PointF();
        point.x = (int) (mSecureRandom.nextDouble() * getWidth());
        point.y = (int) (mSecureRandom.nextDouble() * getHeight());
        ds.point = point;
    }

    /**
     * 在给定范围内，随机产生一组点
     * 
     * @return
     */
    private List<DynSpot> randomGenDyncPoints()
    {
        List<DynSpot> list = new ArrayList<DynSpot>();
        int count = mSpotCount;
        while (--count > 0)
        {
            list.add(randomGenDyncPoint(null));
        }

        return list;
    }

    public void updateDyncPoints()
    {

        DynSpot ds = null;

        for (int i = 0; i < mDsList.size(); i++)
        {
            ds = mDsList.get(i);
            ds.update(this);
        }
    }

    public void draw(Canvas canvas, Paint paint)
    {

        for (int i = 0; i < mDsList.size(); i++)
        {
            DynSpot ds = mDsList.get(i);
            paint.setAlpha(ds.alpha);
            canvas.drawBitmap(ds.bmp, ds.point.x, ds.point.y, paint);
        }
    }

    /**
     * @return 返回 mWidth
     */
    public int getWidth()
    {
        return mWidth;
    }

    /**
     * @param 对mWidth进行赋值
     */
    public void setWidth(int mWidth)
    {
        this.mWidth = mWidth;
    }

    /**
     * @return 返回 mHeight
     */
    public int getHeight()
    {
        return mHeight;
    }

    /**
     * @param 对mHeight进行赋值
     */
    public void setHeight(int mHeight)
    {
        this.mHeight = mHeight;
    }

    /**
     * @return 返回 mBmp
     */
    public List<Bitmap> getBmpList()
    {
        return mBmpList;
    }

    /**
     * @param 对mBmp进行赋值
     */
    public void setBmpList(List<Bitmap> bmpList)
    {
        this.mBmpList = bmpList;
    }

    /**
     * @return 返回 mSpotCount
     */
    public int getSpotCount()
    {
        return mSpotCount;
    }

    /**
     * @param 对mSpotCount进行赋值
     */
    public void setSpotCount(int mSpotCount)
    {
        this.mSpotCount = mSpotCount;
    }

}
