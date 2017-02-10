package com.rockchips.mediacenter.viewutils.devicelist;

import momo.cn.edu.fjnu.androidutils.data.CommonValues;
import momo.cn.edu.fjnu.androidutils.utils.DeviceInfoUtils;
import momo.cn.edu.fjnu.androidutils.utils.SizeUtils;
import momo.cn.edu.fjnu.androidutils.utils.ToastUtils;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import com.rockchips.mediacenter.viewutils.R;
import com.rockchips.mediacenter.viewutils.data.ConstData;
import com.rockchips.mediacenter.viewutils.utils.ImageHelper;

/**
 * 璁惧鍒楄〃椤�
 * @author s00211113
 * 
 */
public class DeviceItemView extends View implements OnGestureListener
{
    private static final String TAG = "DeviceItemView";

    private static final int SCREEN_WIDTH = DeviceInfoUtils.getScreenWidth(CommonValues.application);
    
    private Context mContext;

    private static final int ANIMATION_START_MSG_WHAT = 1;

    private static final int ANIMATIONING_MSG_WHAT = 2;

    // 鍔ㄧ敾缁撴潫鍚庡埛鏂版帶浠�
    private static final int ANIMATIONING_REFRESH_MSG = 3;

    private static final int COUNTVISIABLE = 4;

    private static final int BITMAP_WIDTH = 280;

    private static final int BITMAP_WIDTH_SHADOW = 200;

    private static final int BITMAP_HEIGHT = 180;

    private static final int MAX_OFFSETX = 260;

    private static final int NAMESIZE = 20;

    /**
     * 4寮犲垎绫绘捣鎶�
     */
    private Bitmap[] mIconReflectedImage = new Bitmap[COUNTVISIABLE];

    private static final float REFLECTRATESIZE = 0.3f;

    /**
     * 涓存椂鍙橀噺锛屽彸杈归槾褰憋紝宸﹁竟闃村奖
     */
    private Bitmap mDdestBmp, mRightShadowBmp, mLeftShadowBmp;

    private Paint mPaint;

    private Paint mShadowPaint;
    private float mBitmapHeight;

    private int mOffset = 1;

    private float mTranslateOffsetX;

    private float mScaleOffset;

    private int mAlpha;

    private boolean mRightKey = false;
    private boolean mLeftKey = false;
    private boolean mBeFristLoading = true;
    /**第一次绘制*/
    private boolean mIsFirstDraw = true;
    private Canvas mCanvas;
    
    /**
     * 鎵嬪娍璇嗗埆
     * */
    private GestureDetector mGestureDetector;

    private boolean mBMoving;
    
    private static final int TRANSLATE_OFFSET_X_PLUS = 52;

    private static final float SCALEOFFSET_PLUS = 0.08f;

    private static final int ALPHA_PLUS = 50;

    private static final int ANIMATION_DELAY_TIME = 40;
    private static final int[] IMAGEIDS =
    {  R.drawable.file_icon, R.drawable.photo_icon, R.drawable.music_icon, R.drawable.video_icon};

    private static final int[] TEXTIDS =
     { R.string.file, R.string.photo, R.string.view_music, R.string.view_video};

    public DeviceItemView(Context context)
    {
        super(context);
        mContext = context;
        initView();
    }

    public DeviceItemView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        mContext = context;
        initView();

    }

    public DeviceItemView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        mContext = context;
        initView();
    }

    private OnDeviceSelectedListener mOnSelectedListener;

    public void setOnDeviceSelectedListener(OnDeviceSelectedListener l)
    {
        mOnSelectedListener = l;
    }

    private DeviceItem mDeviceItem;

    public void setDeviceItem(DeviceItem deviceItem)
    {
        mDeviceItem = deviceItem;
    }

    private Handler mHandler = new Handler()
    {
        public void handleMessage(android.os.Message msg)
        {
            switch (msg.what)
            {
                case ANIMATIONING_REFRESH_MSG:
                    mBeFristLoading = true;
                    mRightKey = true;
                    DeviceItemView.this.invalidate();
                    Log.d(TAG, "ANIMATIONING_REFRESH_MSG");
                    break;

                default:
                    mBMoving = true;
                    if (mTranslateOffsetX >= MAX_OFFSETX)
                    {
                        mTranslateOffsetX = 0;
                        mScaleOffset = 0;
                        mAlpha = 0;
                        if (mRightKey)
                        {
                            mOffset++;
                        }
                        else
                        {
                            mOffset += (COUNTVISIABLE - 1);
                        }
                        mOffset %= COUNTVISIABLE;

                        mBMoving = false;
                    }
                    else
                    {

                        mTranslateOffsetX += TRANSLATE_OFFSET_X_PLUS;
                        mScaleOffset += SCALEOFFSET_PLUS;
                        mAlpha += ALPHA_PLUS;

                        DeviceItemView.this.invalidate();
                        mHandler.sendEmptyMessageDelayed(ANIMATIONING_MSG_WHAT, ANIMATION_DELAY_TIME);
                    }
                    break;
            }

        };
    };

    public void refresh()
    {
        mHandler.sendEmptyMessage(ANIMATIONING_REFRESH_MSG);
    }

    private void initView()
    {
        mGestureDetector = new GestureDetector(mContext, this);
        mPaint = new Paint();

        mShadowPaint = new Paint();
        mShadowPaint.setAntiAlias(true);
        mShadowPaint.setFilterBitmap(true);

        mLeftShadowBmp = ImageHelper.createBitmap(getContext(), R.drawable.left_shadow, BITMAP_WIDTH_SHADOW, BITMAP_HEIGHT);
        mRightShadowBmp = ImageHelper.createBitmap(getContext(), R.drawable.right_shadow, BITMAP_WIDTH_SHADOW, BITMAP_HEIGHT);

        // 鍔犺浇鍥剧墖
        loadImages();

        // 鐢熸垚鍥剧墖楂樺害锛氬師鍥鹃珮+鍊掑奖楂�
        mBitmapHeight = BITMAP_HEIGHT * (1 + REFLECTRATESIZE);

    }

    private void loadImages()
    {
        Bitmap srcBitmap = null;

        for (int i = 0, len = IMAGEIDS.length; i < len; i++)
        {
            // 鍔犺浇鍥剧墖
            srcBitmap = ImageHelper.createBitmap(mContext, IMAGEIDS[i], BITMAP_WIDTH, BITMAP_HEIGHT);

            if (srcBitmap != null)
            {
                // 鐢熸垚鍊掑奖鍥剧墖
                mIconReflectedImage[i] = ImageHelper.createReflectedImage(srcBitmap, REFLECTRATESIZE);
    
                // 鍦ㄥ浘鐗囦笂娣诲姞鏂囧瓧
                ImageHelper.addText(mIconReflectedImage[i], mContext.getString(TEXTIDS[i]), NAMESIZE, Color.WHITE, BITMAP_WIDTH / 2, BITMAP_HEIGHT
                        - NAMESIZE);
    
                // 閲婃斁婧愬浘鐗�
                srcBitmap.recycle();
                srcBitmap = null;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        mGestureDetector.onTouchEvent(event);
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT)
        {
        	mRightKey = false;
        	mLeftKey = true;
            invalidate();
          
        }
        else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
        {
        	mRightKey = true;
        	mLeftKey = false;
        	invalidate();
        }
        else if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER)
        {
            if (mOnSelectedListener != null)
            {
                mOnSelectedListener.onSelected(mDeviceItem.mObject, mOffset);
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas)
    {
    	Log.i(TAG, "onDraw");
    	mCanvas = canvas;
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        canvas.drawColor(Color.TRANSPARENT);
        if(mBeFristLoading){
        	initDraw(canvas);
        	mBeFristLoading = false;
        }else if(mRightKey && !mLeftKey){
        	leftMove(canvas);
        	mRightKey = false;
        }else if(mLeftKey && !mRightKey){
        	rightMove(canvas);
        	mLeftKey = false;
        }else{
        	rightMove(canvas);
        	leftMove(canvas);
        }
        
    }

    private static final int ROTATE_Y_LEFT_IN = 8;

    private static final int TRANSLATE_OFFSET_X_RIGHT_PLUS = 80;

    private static final int TRANSLATE_OFFSET_X_210 = 210;

    private static final int TRANSLATE_OFFSET_X_45 = 45;

    private static final int ROTATE_Y_RIGHT_OUT = -8;

    private void rightMove(Canvas canvas)
    {
    	int offsetX = (SCREEN_WIDTH - BITMAP_WIDTH * 4) / 2;
    	//mOffset = (4 + mOffset - 1 ) % 4;
    	canvas.save();
    	canvas.translate(offsetX, (getHeight() - mBitmapHeight) / 2);
    	int index = 0;
    	for(int i = (mOffset + 2) % 4; i < mIconReflectedImage.length; ++i){
    		canvas.drawBitmap(mIconReflectedImage[i], index * BITMAP_WIDTH, 0, null);
    		++index;
    	}
    	
    	for(int i = 0; i < (mOffset + 2) % 4; ++i){
    		canvas.drawBitmap(mIconReflectedImage[i], index * BITMAP_WIDTH, 0, null);
    		++index;
    	}
    	
    	canvas.restore();
    	
    	//褰撳墠鍋忕Щ閲�
    	mOffset = (4 + mOffset - 1) % 4;
    	
    	canvas.save();
    	canvas.translate(offsetX + BITMAP_WIDTH, (getHeight() - mBitmapHeight) / 2);
    	canvas.scale(1.2f, 1.2f, BITMAP_WIDTH / 2, BITMAP_HEIGHT / 2);
    	canvas.drawBitmap(mIconReflectedImage[mOffset], 0, 0, null);
    	canvas.restore();
    }

    /**
     * <涓�鍙ヨ瘽鍔熻兘绠�杩�>娴锋姤缁樺埗椤哄簭锛�1432 <鍔熻兘璇︾粏鎻忚堪>
     * @param canvas
     * @see [绫汇�佺被#鏂规硶銆佺被#鎴愬憳]
     */
    private void leftMove(Canvas canvas)
    {
    	Log.i(TAG, "leftMove");
    	int offsetX = (SCREEN_WIDTH - BITMAP_WIDTH * 4) / 2;
    	
    	canvas.save();
    	canvas.translate(offsetX, (getHeight() - mBitmapHeight) / 2);
    	int index = 0;
    	for(int i = mOffset; i < mIconReflectedImage.length; ++i){
    		canvas.drawBitmap(mIconReflectedImage[i], index * BITMAP_WIDTH, 0, null);
    		++index;
    	}
    	
    	for(int i = 0; i < mOffset; ++i){
    		canvas.drawBitmap(mIconReflectedImage[i], index * BITMAP_WIDTH, 0, null);
    		++index;
    	}
    	
    	canvas.restore();
    	
    	//褰撳墠鍋忕Щ閲�
    	mOffset = (++mOffset) % 4;
    	
    	canvas.save();
    	canvas.translate(offsetX + BITMAP_WIDTH, (getHeight() - mBitmapHeight) / 2);
    	canvas.scale(1.2f, 1.2f, BITMAP_WIDTH / 2, BITMAP_HEIGHT / 2);
    	canvas.drawBitmap(mIconReflectedImage[mOffset], 0, 0, null);
    	canvas.restore();
    	
    	
    }

    private Bitmap getDestBmp(int index)
    {
        int i = index % 4;

        return mIconReflectedImage[i];
    }

    @Override
    public boolean onDown(MotionEvent arg0)
    {
        return false;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent arg0)
    {
        if (mOnSelectedListener != null)
        {
            mOnSelectedListener.onSelected(mDeviceItem.mObject, mOffset);
        }
        return false;
    }

    @Override
    public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3)
    {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent arg0)
    {
    }

    @Override
    public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3)
    {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent arg0)
    {
    }

    public void recycle()
    {
        // 閲婃斁娴锋姤鍥剧墖
        recycleReflectedImage();

        // 閲婃斁闃村奖鍥剧墖
        if (mLeftShadowBmp != null)
        {
            mLeftShadowBmp.recycle();
            mLeftShadowBmp = null;
        }

        if (mRightShadowBmp != null)
        {
            mRightShadowBmp.recycle();
            mRightShadowBmp = null;
        }
    }

    /** 閲婃斁娴锋姤鍥剧墖 */
    private void recycleReflectedImage()
    {
        if (mIconReflectedImage == null || mIconReflectedImage.length == 0)
        {
            return;
        }

        for (int i = 0; i < mIconReflectedImage.length; i++)
        {
            if (mIconReflectedImage[i] != null)
            {
                if (!mIconReflectedImage[i].isRecycled())
                {
                    mIconReflectedImage[i].recycle();
                }
                mIconReflectedImage[i] = null;
            }
        }
    }
    
    
    
    /**
     * 绗竴娆＄粯鍒舵椂璋冪敤
     * @param canvas
     */
    public void initDraw(Canvas canvas){
    	int offsetX = (SCREEN_WIDTH - BITMAP_WIDTH * 4) / 2;
    	
    	canvas.save();
    	canvas.translate(offsetX, (getHeight() - mBitmapHeight) / 2);
    	canvas.drawBitmap(mIconReflectedImage[0], 0, 0, null);
    	canvas.drawBitmap(mIconReflectedImage[1], BITMAP_WIDTH, 0, null);
    	canvas.drawBitmap(mIconReflectedImage[2], 2 * BITMAP_WIDTH, 0, null);
    	canvas.drawBitmap(mIconReflectedImage[3], 3 * BITMAP_WIDTH, 0, null);
    	canvas.restore();
    	
    	canvas.save();
    	canvas.translate(offsetX + BITMAP_WIDTH, (getHeight() - mBitmapHeight) / 2);
    	canvas.scale(1.2f, 1.2f, BITMAP_WIDTH / 2, BITMAP_HEIGHT / 2);
    	canvas.drawBitmap(mIconReflectedImage[1], 0, 0, null);
    	canvas.restore();
    	
    	Activity currentActivity = (Activity)mContext;
    	Intent currIntent = currentActivity.getIntent();
    	if(mIsFirstDraw && currIntent != null && ConstData.ActivityAction.INSTALL_APK.equals(currIntent.getAction())){
    		mIsFirstDraw = false;
    		rightMove(canvas);
    		ToastUtils.showToast(mContext.getResources().getString(R.string.apk_install_tip));
    	}
    		
    	
    }
    
    /**
     * 鍒濆鍖栧熀鏈暟鎹�
     */
    public void initData(){
    	mOffset = 1;
    	mBeFristLoading = true;
    }
    
}
