/**
 * Title: Gallery3D.java<br>
 * Package: com.rockchips.shine.videophone.widget<br>
 * Description: TODO<br>
 * @author r00178559
 * @version v1.0<br>
 * Date: 2013-7-2下午4:31:20<br> 
 * Copyright © Huawei Technologies Co., Ltd. 2013. All rights reserved.
 */

package com.rockchips.mediacenter.viewutils.gallery3d;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Transformation;
import android.widget.Gallery;

/**
 * Description: TODO<br>
 * @author r00178559
 * @version v1.0 Date: 2013-7-2 下午4:31:20<br>
 */

@SuppressWarnings("deprecation")
public class Gallery3D extends Gallery
{
    private static final float ZOOM_SCALE = 0.6f;
    
    private static final float OVERLAP_SCLAE = 0.3f;
    
    private Camera mCamera = new Camera();

    private int mCoveflowCenter;
    
    private float mZoomScale;
    
    private float mOverlapScale;
    
    private int mReflectAreaCenterY;

    public Gallery3D(Context context)
    {
        super(context, null);
    }

    public Gallery3D(Context context, AttributeSet attrs)
    {
        super(context, attrs, 0);
    }

    public Gallery3D(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        // Enable set transformation.
        this.setStaticTransformationsEnabled(true);
        // Enable set the children drawing order.
        this.setChildrenDrawingOrderEnabled(true);
    }
    
    public void setZoomScale(float scale)
    {
        if (0f >= scale)
        {
            mZoomScale = ZOOM_SCALE;
        }
        else
        {
            mZoomScale = scale;
        }
    }
    
    public void setReflectAreaCenterY(int centerY)
    {
        mReflectAreaCenterY = centerY;
    }
    
    public void setOverlapScale(float scale)
    {
        if ((1.0f < scale) || (0f > scale))
        {
            mOverlapScale = OVERLAP_SCLAE;
        }
        else
        {
            mOverlapScale = scale;
        }
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i)
    {
        // Current selected index.
        int selectedIndex = getSelectedItemPosition() - getFirstVisiblePosition();
        if (selectedIndex < 0)
        {
            return i;
        }
        
        if (i < selectedIndex)
        {
            return i;
        }
        else if (i >= selectedIndex)
        {
            return childCount - 1 - i + selectedIndex;
        }
        else
        {
            return i;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        mCoveflowCenter = getCenterOfCoverflow();
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private int getCenterOfView(View view)
    {
        return view.getLeft() + view.getWidth() / 2;
    }
    
    private int getCenterOfCoverflow()
    {
        return (getWidth() - getPaddingLeft() - getPaddingRight()) / 2 + getPaddingLeft();
    }

    @Override
    protected boolean getChildStaticTransformation(View child, Transformation t)
    {
        super.getChildStaticTransformation(child, t);

        t.clear();
        t.setTransformationType(Transformation.TYPE_MATRIX);

        transformImageBitmap(child, t);
        return true;
    }

    private void transformImageBitmap(View child, Transformation t)
    {
        final Matrix imageMatrix = t.getMatrix();
        final int imageHeight = child.getHeight();
        final int imageWidth = child.getWidth();
        int centerY = mReflectAreaCenterY;
        if ((0 > mReflectAreaCenterY) || (imageHeight < mReflectAreaCenterY))
        {
            centerY = imageHeight / 2;
        }
        
        mCamera.save();

        float scale = ((float)(mCoveflowCenter - getCenterOfView(child))) / imageWidth;
        float translateX = getTranslateX(imageWidth, scale);
        if (0 > scale)
        {
            translateX = -translateX;
        }
        
        // Get the matrix from the camera, in fact, the matrix is S (scale) transformation.
        mCamera.getMatrix(imageMatrix);
        float scale1 = (float) Math.pow(Math.pow(mZoomScale, Math.abs(scale)) , 1.0f / 2);
        imageMatrix.postScale(scale1, scale1);
        imageMatrix.postTranslate(translateX, 0);
        // The matrix final is T2 * S * T1, first translate the center point to (0, 0),
        // then scale, and then translate the center point to its original point.
        // T * S * T

        // S * T1
        imageMatrix.postTranslate((imageWidth / 2), centerY);
        // (T2 * S) * T1
        imageMatrix.preTranslate(-(imageWidth / 2), -centerY);
        
        mCamera.restore();
    }
    
    private float getTranslateX(int width, float scale)
    {
        float scaleAbs = Math.abs(scale);
        float translateX = 0;
        if (0 == scaleAbs)
        {
            translateX = 0f;
        }
        else
        {
            float factor = scaleAbs;
            while (1 < factor)
            {
                translateX += (float) (width * (1.0f - Math.pow(mZoomScale, factor)) / 2);
                factor -= 1;
            }
            translateX += (float) (width * (1.0f - Math.pow(mZoomScale, factor)) / 2);
        }
        
        float factor = scaleAbs;
        while (1 < factor)
        {
            translateX += width * (1.0f - Math.pow(mZoomScale, factor)) * mOverlapScale;
            factor -= 1;
        }
        translateX += width * (1.0f - Math.pow(mZoomScale, factor)) * mOverlapScale;
        return translateX;
    }
}
