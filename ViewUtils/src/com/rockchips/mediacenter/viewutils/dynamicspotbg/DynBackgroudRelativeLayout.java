package com.rockchips.mediacenter.viewutils.dynamicspotbg;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.rockchips.mediacenter.viewutils.R;

/**
 * 动态斑点背景
 * @author s00211113
 * 
 */
public class DynBackgroudRelativeLayout extends RelativeLayout
{

    private Context mContext;

    private DynBg mDynBg;

    private static final int BG_W = 1280;

    private static final int BG_H = 720;

    private static final int BG_SPOT_COUNT = 20;

    private static final int MESSAGE_DELAY_TIME = 100;

    private List<Bitmap> mBmpList;

    private boolean mBStop;

    private UIHandler mui = new UIHandler();

    //动态斑点开关
    private static final boolean SWITCH = true;

    public DynBackgroudRelativeLayout(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        mContext = context;

        initBmpList();
    }

    public DynBackgroudRelativeLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        mContext = context;
        initBmpList();
    }

    public DynBackgroudRelativeLayout(Context context)
    {
        super(context);
        mContext = context;
        initBmpList();
    }

    private void initBmpList()
    {
        if (!SWITCH)
        {
            return;
        }
        if (mBmpList == null)
        {
            Options op = new Options();
            op.inSampleSize = 2;

            mBmpList = new ArrayList<Bitmap>();
            Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.halo1, op);
            mBmpList.add(bmp);
            bmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.halo2, op);
            mBmpList.add(bmp);
            bmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.halo3, op);
            mBmpList.add(bmp);
            bmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.halo4, op);
            mBmpList.add(bmp);
            bmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.halo5, op);
            mBmpList.add(bmp);
            bmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.halo6, op);
            mBmpList.add(bmp);
        }
        try
        {
            mDynBg = new DynBg(BG_W, BG_H, BG_SPOT_COUNT, mBmpList);
        }
        catch (InvalidParameterException e)
        {
        }

        mui.sendEmptyMessageDelayed(0, MESSAGE_DELAY_TIME);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom)
    {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void dispatchDraw(Canvas canvas)
    {
        if (mDynBg != null)
        {
            Paint paint = new Paint();
            mDynBg.updateDyncPoints();
            mDynBg.draw(canvas, paint);
        }
        super.dispatchDraw(canvas);
    }

    /**
     * 
     * 使本view失效的handler
     * @author s00211113
     * 
     */
    public class UIHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg)
        {

            removeMessages(0);
            if (!mBStop)
            {
                postInvalidate();
                sendEmptyMessageDelayed(0, MESSAGE_DELAY_TIME);
            }

            super.handleMessage(msg);
        }
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility)
    {

        if (visibility == VISIBLE)
        {
            mBStop = true;
        }
        else
        {
            mBStop = false;
            mui.removeMessages(0);
            mui.sendEmptyMessageDelayed(0, MESSAGE_DELAY_TIME);
        }

        super.onVisibilityChanged(changedView, visibility);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility)
    {
        Log.e("TSS", "onWindowVisibilityChanged visibility:" + visibility);
        super.onWindowVisibilityChanged(visibility);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus)
    {
        Log.e("TSS", "onWindowFocusChanged hasWindowFocus:" + hasWindowFocus);

        if (!hasWindowFocus)
        {
            mBStop = true;
        }
        else
        {
            mBStop = false;
            mui.removeMessages(0);
            mui.sendEmptyMessageDelayed(0, MESSAGE_DELAY_TIME);
        }

        super.onWindowFocusChanged(hasWindowFocus);
    }
}
