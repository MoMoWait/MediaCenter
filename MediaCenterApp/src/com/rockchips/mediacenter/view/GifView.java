package com.rockchips.mediacenter.view;

import com.rockchips.mediacenter.utils.GifOpenHelper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

/**
 * GifView<br>
 * 本类可以显示一个gif动画，其使用方法和android的其它view（如imageview)一样。<br>
 * 如果要显示的gif太大，会出现OOM的问题。
 * @author 
 *
 */
public class GifView extends View
{
    
    /**gif解码器*/
    GifOpenHelper mGifHelper;
    
    /**当前要画的帧的图*/
    private Bitmap mCurrentImage;
    
    private DrawThread mDrawThread;
    
    public GifView(Context context)
    {
        super(context);
        
    }
    
    public GifView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }
    
    public GifView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        
    }
    
    public void setGifOpenHelper(GifOpenHelper gif)
    {
        if (mDrawThread != null)
        {
            mDrawThread.isRun = false;
        }
        
        mGifHelper = gif;
        mDrawThread = new DrawThread(mGifHelper);
        mDrawThread.start();
    }
    
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        if (mGifHelper == null)
        {
            return;
        }
        if (mCurrentImage == null)
        {
            mCurrentImage = mGifHelper.getImage();
        }
        if (mCurrentImage == null || mCurrentImage.isRecycled())
        {
            return;
        }
        int saveCount = canvas.getSaveCount();
        canvas.save();
        canvas.translate((getWidth() - mCurrentImage.getWidth()) / 2, (getHeight() - mCurrentImage.getHeight()) / 2);
        canvas.drawBitmap(mCurrentImage, 0, 0, null);
        canvas.restoreToCount(saveCount);
    }
    
    public void stop()
    {
        if (mDrawThread != null)
        {
            mDrawThread.isRun = false;
        }
        mDrawThread = null;
    }
    
    private Handler mRedrawHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            invalidate();
        }
    };
    
    /**
     * 动画线程
     * @author 
     *
     */
    private class DrawThread extends Thread
    {
        public boolean isRun = true;
        
        private GifOpenHelper mGif;
        
        private static final int DECODE_FAIL_TIME = 100;
        
        public DrawThread(GifOpenHelper gif)
        {
            mGif = gif;
        }
        
        public void run()
        {
            if (mGif == null)
            {
                return;
            }
            
            int frameCount = mGif.getFrameCount();
            int index = 0;
            long sp;
            while (isRun)
            {
                index = index % frameCount;
                mCurrentImage = mGif.getFrame(index);
                sp = mGif.getDelay(index);
                //如果时间间隔解码失败默100ms
                sp = sp > 0 ? sp : DECODE_FAIL_TIME;
                if (mRedrawHandler != null)
                {
                    Message msg = mRedrawHandler.obtainMessage();
                    mRedrawHandler.sendMessage(msg);
                    SystemClock.sleep(sp);
                }
                else
                {
                    break;
                }
                
                index++;
            }
        }
        
        public void free()
        {
            if (mGif != null)
            {
                mGif.free();
            }
        }
    }
    
    @Override
    protected void finalize()
        throws Throwable
    {
        if (mDrawThread != null)
        {
            mDrawThread.free();
        }
        super.finalize();
    }
}
