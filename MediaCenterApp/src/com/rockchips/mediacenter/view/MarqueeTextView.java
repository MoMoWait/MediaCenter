package com.rockchips.mediacenter.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * 文字跑马灯效果
 * @author s00211113
 * 
 */
public class MarqueeTextView extends TextView
{
    private boolean mIsFocused;

    public MarqueeTextView(Context context)
    {
        super(context);
        init();
    }

    public MarqueeTextView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public MarqueeTextView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init();
    }

    private void init()
    {
        mIsFocused = true;
        this.setFocusable(false);
        this.setSingleLine();
        //this.setEllipsize(TruncateAt.MARQUEE);
        this.setFocusableInTouchMode(false);
        this.setMarqueeRepeatLimit(-1);
    }

    public void setRepeatLimit(int repeatLimit)
    {
        this.setMarqueeRepeatLimit(repeatLimit);
    }

    public void startMarquee()
    {
        this.mIsFocused = true;
        this.requestLayout();
    }

    public void stopMarquee()
    {
        this.mIsFocused = false;
        this.requestLayout();
    }

    @Override
    public boolean isFocused()
    {
        return mIsFocused;
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect)
    {
    	if(focused)
    	{
    		super.onFocusChanged(focused, direction, previouslyFocusedRect);
    	}
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus)
    {
    	if(hasWindowFocus)
    	{
    		super.onWindowFocusChanged(hasWindowFocus);
    	}
    }
}