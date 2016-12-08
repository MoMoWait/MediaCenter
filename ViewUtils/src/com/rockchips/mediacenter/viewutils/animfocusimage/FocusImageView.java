package com.rockchips.mediacenter.viewutils.animfocusimage;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

public class FocusImageView extends View implements ValueAnimator.AnimatorUpdateListener, AnimatorListener
{
    
    private static final String TAG = "FocusImageView";
    private CircleRectFocus mFocus = null;
    private Rect mSrcRect = new Rect();
    private Rect mTargetRect = new Rect();
    private float mSrcAlpha = 1f;
    private float mDstAlpha = 1f;
    private int mDuration = 200;//ms
    
    public FocusImageView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }
    
    public FocusImageView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }
    
    public FocusImageView(Context context)
    {
        super(context);
        // TODO Auto-generated constructor stub
        
        
    }
    
    public void setFocus(CircleRectFocus focus)
    {
        mFocus  = focus;
    }
    
    public void startAnimator()
    {
        createAnimator();
        mAnimatorSet.start();
    }
    
    public void setDuration(int duration)
    {
        mDuration = duration;
    }
    
    AnimatorSet mAnimatorSet  = null;
    
    private Rect mSrcCopy = new Rect();
    //计算焦点位置 alpha的地方
    private void createAnimator()
    {
        if(mAnimatorSet == null)
        {
            mAnimatorSet = new AnimatorSet();
            mAnimatorSet.addListener(this);

        }
        
        PropertyValuesHolder pvht = PropertyValuesHolder.ofInt("t",getSrcRect().top,getDstRect().top);
        PropertyValuesHolder pvhl = PropertyValuesHolder.ofInt("l",getSrcRect().left,getDstRect().left);
        PropertyValuesHolder pvhr = PropertyValuesHolder.ofInt("r",getSrcRect().right,getDstRect().right);
        PropertyValuesHolder pvhb = PropertyValuesHolder.ofInt("b",getSrcRect().bottom,getDstRect().bottom);
        PropertyValuesHolder pvha = PropertyValuesHolder.ofFloat("alpha",getSrcAlpha(),getDstAlpha());
        
        ObjectAnimator whxyaBouncer = ObjectAnimator.ofPropertyValuesHolder(mFocus, pvht, pvhl,
            pvhr, pvhb,pvha).setDuration(mDuration);
        
        whxyaBouncer.setInterpolator(new  AccelerateDecelerateInterpolator());
        whxyaBouncer.addUpdateListener(this);

        ((AnimatorSet)mAnimatorSet).playTogether(whxyaBouncer);
    }
    
    @Override
    protected void onDraw(Canvas canvas)
    {
        canvas.save();
        mFocus.onDraw(canvas);
        canvas.restore();
    }
    
    public void onAnimationUpdate(ValueAnimator animation)
    {
        invalidate();
    }
    
  

    /**
     * @return the srcRect
     */
    public Rect getSrcRect()
    {
        return mSrcRect;
    }

    /**
     * @param srcRect the srcRect to set
     */
    public void setSrcRect(Rect srcRect)
    {
        mSrcRect = srcRect;
        
        mFocus.setRect(mSrcRect);
       
    }

    /**
     * @return the targetRect
     */
    public Rect getDstRect()
    {
        return mTargetRect;
    }

    /**
     * @param targetRect the targetRect to set
     */
    public void setDstRect(Rect targetRect)
    {
        mTargetRect.top = targetRect.top;
        mTargetRect.left = targetRect.left;
        mTargetRect.right = targetRect.right;
        mTargetRect.bottom = targetRect.bottom;
    }

    /**
     * @return the srcAlpha
     */
    public float getSrcAlpha()
    {
        return mSrcAlpha;
    }

    /**
     * @param srcAlpha the srcAlpha to set
     */
    public void setSrcAlpha(float srcAlpha)
    {
        mSrcAlpha = srcAlpha;
    }

    /**
     * @return the dstAlpha
     */
    public float getDstAlpha()
    {
        return mDstAlpha;
    }

    /**
     * @param dstAlpha the dstAlpha to set
     */
    public void setDstAlpha(float dstAlpha)
    {
        mDstAlpha = dstAlpha;
    }

    @Override
    public void onAnimationCancel(Animator arg0)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onAnimationEnd(Animator arg0)
    {

        //for test
        setDstRect(mSrcCopy);
        
        super.onAnimationEnd();
    }

    @Override
    public void onAnimationRepeat(Animator arg0)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onAnimationStart(Animator arg0)
    {
        mSrcCopy.top = getSrcRect().top;
        mSrcCopy.left = getSrcRect().left;
        mSrcCopy.right = getSrcRect().right;
        mSrcCopy.bottom = getSrcRect().bottom;
    }
    
}
