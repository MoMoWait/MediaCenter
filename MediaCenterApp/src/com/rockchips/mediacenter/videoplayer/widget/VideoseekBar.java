package com.rockchips.mediacenter.videoplayer.widget;

import momo.cn.edu.fjnu.androidutils.utils.SizeUtils;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsSeekBar;

import com.rockchips.mediacenter.R;
import com.rockchips.mediacenter.data.ConstData.PlayMode;
import com.rockchips.mediacenter.videoplayer.VideoPlayerActivity;

public class VideoseekBar extends AbsSeekBar
{
    private static final String TAG = "MyseekBar";
    
    private Bitmap imgPlaying, imgSeekbar_background,imgSeekbar_Speed, imgSeekbar_Played, imgThumb_Pressed, imgThumb_Stop,
        imgSeekbar_Played1, imgSeekbar_Cur_Up, imgSeekbar_Cur_Down;
    
    BitmapDrawable bdPlaying, bdSeekbar_background,bdSeekbar_Speed, bdSeekbar_Played, bdThumb_Stop, bdThumb_Pressed,
        bdSeekbar_Cur_Down, bdSeekbar_Cur_Up;
    
    /**修改者：l00174030；修改原因：进度条不能铺满屏幕**/
    //private static final int SCREEN_SEEKBAR_LENGTH = 1260;
    private static final int SCREEN_SEEKBAR_LENGTH = 1280;
    
    private int playingWidth;
    
    float mTouchProgressOffset;
    
    // 当前进度条播放的位置，它是一个百分比
    public float scale;
    
    // 进度条拖动的时候进度条停留的位置，它是一个百分比
    public float Kscale;
    
    // 进度条进度值
    private float progress = 0;
    
    // 播放和暂停状态进度条的状态标志,通过改变这个属性的状态可以改变SeekBar的状态
    private boolean isPlaying;
    
    // 是否点击的左键
    private boolean isLeft = true;
    
    // 是否用按键对seekbar进行了快进、快退等操作
    public boolean isOnkey = false;
    
    // 是否点击了确定键
    private boolean isEnSure = false;
    
    // 是否按确定键后，然后再进行快进操作
    private boolean isZero = false;
    
    // //第一次开始快进的标志
    // private boolean isFirst = true;
    
    private boolean isOneFase = true;
    
    // 判断是否拖动还是通过按键实现快进
    private boolean isTrack = false;
    
    // 判断是否是拖动的UP状态
    private boolean isUp = false;
    
    
    // 拖动按钮图标空白区域的边框宽度
    private int thumboffSet = 20;
    
    // SeekBar正常定时跟新时间
    private long normalTime = 200;
    
    // zkf61715 是否需要更新快进快退速率
    private boolean Xupdate = false;
    
    // zkf61715 是否是网络视频
    private boolean isFromNetwork = false;
    
    private boolean needUpdate = true;
    
    private int Xacceleration;
    
    Bitmap mSCBitmap = null;
    
    Canvas mCanvas = null;
    
    Paint paint;
    
    public int historyPosition = -1;
    
    int pus = 0;
    
    int accordposition;
    
    /** 修改者：l00174030；修改原因：先画背景，在画进度条**/
    // 把数字常量抽出，便于修改
	/* BEGIN: Modified by s00211113 for DTS2014031902280  2014/03/19 */
    private int seekbarLeftTop = 0;//42 + 10 + 5 + 8;
    
    // 进度条圆圈TOP
    private int pauseLeftTop = 0;//42 + 10 + 5;
	/* END: Modified by s00211113 for DTS2014031902280  2014/03/19 */
    
    // 游标下半部分TOP
    private int cursorDownTop = 42;
    
    // 游标中的时间的坐标
    private int cursorTextTop = 30;
    
    private Context context = null;
    
    private float dposition = 0;
    
    // zkf61715 播放模式  trickplay模式还是fastseet模式
    private int playMode = PlayMode.PLAY_TRICK;
    
    private long lastKeyDownTime = 0;
    
    // zkf61715 避免onkeyUp事件叠加
    private long lastUpTime;
    
    private long firstDownTime;
    private long persistTime = 0; 
    
    private static final int ACCELERATE_BASE_DURATION = 8000;
    
    /**fly.gao 快进，快退步长度,单位毫秒*/
    private static final int SEEK_STEP_LENGTH = 10000;
    
    // zkf61715 是否支持快进快退
    private boolean canAccelerate = true;
    public void canAccelerate(boolean canAccelerate)
    {
        this.canAccelerate = canAccelerate;
    }
    // zkf61715 是否收到keyUp事件
    private boolean hasKeyUp = true;
    
    // zkf61715 是否收到keyDown事件
    private boolean hasKeyDown = false;
    
    private int lastKeycode;
    
    private int pos;
    
    private boolean mIsNeedShowSeekLayout;
    
    /**
     * 父布局
     */
    private SeekBarLayout mSeekBarLayout;
    
    public boolean hasKeyUp(){
        return hasKeyUp;
    }
    
    public void setHasKeyUp(boolean hasKeyUp){
        this.hasKeyUp = hasKeyUp;
    }
    
    // zkf61715 是否需要seek操作，普通快进快退不需要用到seek
    private boolean needSeek = false;
    
    public boolean needSeek(){
        return needSeek;
    }
    
    
    // zkf61715 是否是从trickplay模式进入快速seek模式
    private boolean isTrickBefore = false;
    
    public boolean isTrcikBefore(){
        return isTrickBefore;
    }
    
    public boolean isXupdate(){
        return Xupdate;
    }
    
    public void setXupdate(boolean Xupdate){
        this.Xupdate = Xupdate;
    }
    
    public boolean isFromNetwork()
    {
        return isFromNetwork;
    }

    public void setFromNetwork(boolean isFromNetwork)
    {
        this.isFromNetwork = isFromNetwork;
    }

    public long getPersistTime(){
        return persistTime;
    }
    
    public int getPlayMode(){
        return playMode;
    }
    
    public void setPlayMode(int playMode){
        this.playMode = playMode;
    }
    
    public int getXacceleration()
    {
        return Xacceleration;
    }
    
    public void setXacceleration(int Xacceleration)
    {
        this.Xacceleration = Xacceleration;
    }
    
    public long getNormalTime()
    {
        return normalTime;
    }
    
    public float getScale()
    {
        return scale;
    }
    
    public void setScale(float scale)
    {
        this.scale = scale;
    }
    
    public float getKscale()
    {
        return Kscale;
    }
    
    public void setKscale(float kscale)
    {
        Kscale = kscale;
    }
    
    // 每走一次的时间，单位为毫秒
    private long sleepTime = 1000;
    
    public long getSleepTime()
    {
        return sleepTime;
    }
    
    public void setSleepTime(long sleepTime)
    {
        this.sleepTime = sleepTime;
    }
    
    // 进度条总共要走的时间，单位为秒,它可以确定拖动条快进的进度
    private float totalTime = 100000;
    
    private static int bdCurson_Up_Width;
    
    /** 标识位 用于在按键处理时是否需要对进度条界面进行刷新 */
    private boolean bflush = true;
    
    public boolean isBflush()
    {
        return bflush;
    }
    
    public void setBflush(boolean bflush)
    {
        this.bflush = bflush;
    }
    
    public boolean isOnkey()
    {
        return isOnkey;
    }
    
    public void setOnkey(boolean isOnkey)
    {
        //Log.d(TAG, "onkey id ==" + isOnkey);
        this.isOnkey = isOnkey;
    }
    
    public boolean isEnSure()
    {
        return isEnSure;
    }
    
    public void setEnSure(boolean isEnSure)
    {
        this.isEnSure = isEnSure;
    }
    
    public boolean isLeft()
    {
        return isLeft;
    }
    
    public void setLeft(boolean isLeft)
    {
        this.isLeft = isLeft;
    }
    
    public float getTotalTime()
    {
        return totalTime;
    }
    
    public void setTotalTime(int totalTime)
    {
        this.totalTime = totalTime;
    }
    
    public Handler mHandler = new Handler();
    
    public boolean isPlaying()
    {
        return isPlaying;
    }
    
    public void setPlaying(boolean isPlaying)
    {
        this.isPlaying = isPlaying;
    }
    
    public void setProgress(float progress)
    {
    	Log.i("VideoKey", "MyseekBar->setProgress:" + progress);
        this.progress = progress;
    }
    
    public int getProgress()
    {
        return (int)progress;
    }
    
    public int getThumboffSet()
    {
        return thumboffSet;
    }
    
    public void setThumboffSet(int thumboffSet)
    {
        this.thumboffSet = thumboffSet;
        postInvalidate();
    }
    
    public VideoseekBar(Context context, AttributeSet attrs)
    {
        
        super(context, attrs);
        setFocusable(true);
        this.context = context;
        init();
    }
    
    public VideoseekBar(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        this.context = context;
    }
    
    public VideoseekBar(Context context)
    {
        super(context);
        this.context = context;
    }
    
    /**
     * 此方法用来重新画seekBar图形，它在invalidate()方法的时候调用
     */
    @SuppressLint("DrawAllocation")
	@Override
    protected synchronized void onDraw(Canvas canvas)
    {
    	Log.i("VideoKey", "onDraw");
        Paint paint = new Paint();
        canvas.save();
        int available = this.getWidth();
        Log.i("VideoKey", "onDraw->available:" + available);
        int thumbWidth = bdPlaying.getIntrinsicWidth() - getThumboffSet();
        Log.i("VideoKey", "onDraw->thumbWidth:" + thumbWidth);
        int thumbHeight = bdPlaying.getIntrinsicHeight();
        Log.i("VideoKey", "onDraw->thumbHeight:" + thumbHeight);
        available += getThumbOffset() * 2;
        int thumbPos = (int)(scale * available);
        Log.i("VideoKey", "onDraw->thumbPos:" + thumbPos);
        int thumbPos1 = (int)(Kscale * available);
        Log.i("VideoKey", "onDraw->thumbPos1:" + thumbPos);
        int seekPosition = (int)(Kscale * available);  
        /** 修改者：l00174030；修改原因：先画背景，在画进度条**/
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0X7f233619);
        paint.setAntiAlias(true);
        RectF rectfbg = new RectF(0, 0 , getWidth(), getHeight());
        canvas.drawRect(rectfbg, paint);
        seekDraw(canvas, seekPosition);
        playDraw(canvas, thumbPos);
        canvas.restore();
    }
    
    public interface OnSeekBarChangeListener
    {
        
        /**
         * 此方法在seekBar拖动和进度改变时调用，可以知道拖动条的进度
         */
        void onProgressChanged(VideoseekBar seekBar, int progress, boolean fromUser, int kprogress);
        
        /**
         * 此方法在seekBar开始拖动时调用
         */
        void onStartTrackingTouch(VideoseekBar seekBar);
        
        /**
         * 此方法在seekbar停止拖动时调用
         */
        void onStopTrackingTouch(VideoseekBar seekBar);
        
        /**
         * 按左右键，快进或者快退调用的方法，在此方法中可以对快进的倍数进行操作
         */
        void onKeyTounch(VideoseekBar seekBar);
        
        /**
         * 获取当前播放进度返回NAN时调用
         */
        
        public float onNan();
    }
    
    public OnSeekBarChangeListener mOnSeekBarChangeListener;
    
    public void setOnSeekBarChangeListener(OnSeekBarChangeListener l)
    {
        mOnSeekBarChangeListener = l;
    }
    
    void onStartTrackingTouch()
    {
        if (mOnSeekBarChangeListener != null)
        {
            mOnSeekBarChangeListener.onStartTrackingTouch(this);
        }
    }
    
    void onStopTrackingTouch()
    {
        if (mOnSeekBarChangeListener != null)
        {
            mOnSeekBarChangeListener.onStopTrackingTouch(this);
        }
    }
    
    void onKeyTouch()
    {
    	Log.i("VideoKey", "onKeyTouch");
        if (mOnSeekBarChangeListener != null)
        {
            mOnSeekBarChangeListener.onKeyTounch(this);
        }
    }
    
    // @Override
    public void onProgressRefresh(float scale, boolean fromUser)
    {
    	Log.i("VideoKey", "MySeekBar->onProgressRefresh->scale:" + scale);
    	Log.i("VideoKey", "MySeekBar->onProgressRefresh->fromUser:" + fromUser);
        if (mOnSeekBarChangeListener != null)
        {
            mOnSeekBarChangeListener.onProgressChanged(this, getProgress(), fromUser, (int)(Kscale * getMax()));
        }
        
        if (isFromNetwork) return;            
        Log.i("VideoKey", "MySeekBar->onProgressRefresh->canAccelerate:" + canAccelerate);
        Log.i("VideoKey", "MySeekBar->onProgressRefresh->Xacceleration:" + Xacceleration);
        if (!canAccelerate && Xacceleration != 0 && Xacceleration != 1)
        {
        	Log.i("VideoKey", "MySeekBar->onProgressRefresh->onKeyTouch");
            onKeyTouch();
        }
        else
        {
            // zkf61715 进度条发生变化时实时更新播放进度
            switch (playMode)
            {
                case PlayMode.PLAY_TRICK:
                    break;
                case PlayMode.PLAY_SEEK:
                    if (needSeek)
                    {
                        onKeyTouch();
                    }
                    break;
                
                default:
                    break;
            }
        }
    }
    
    private void trackTouchEvent(MotionEvent event)
    {
        final int width = getWidth();
        final int available = width - getPaddingLeft() - getPaddingRight();
        int x = (int)event.getX();
        if (x < getPaddingLeft())
        {
            Kscale = 0.0f;
        }
        else if (x > width - getPaddingRight() - 2)
        {
            Kscale = 1.0f;
        }
        else
        {
            Kscale = (float)(x - getPaddingLeft()) / (float)available;
            progress = (int)mTouchProgressOffset;
        }
        final int max = getMax();
        progress += Kscale * max;
        if (progress < 0)
        {
            progress = 0;
        }
        if (progress > getMax())
        {
            progress = getMax();
        }
        postInvalidate();
        setProgress(progress);
    }
    
    private void attemptClaimDrag()
    {
        if (getParent() != null)
        {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if (!isEnabled())
        {
            return false;
        }
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                if (mHandler == null)
                {
                    mHandler = new Handler();
                }
                isTrack = true;
                setPressed(true);
                setOnkey(false);
                onStartTrackingTouch();
                trackTouchEvent(event);
                postInvalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                trackTouchEvent(event);
                attemptClaimDrag();
                onProgressRefresh(scale, true);
                System.out.println("MOVE");
                postInvalidate();
                break;
            case MotionEvent.ACTION_UP:
                isTrack = false;
                isUp = true;
                setPressed(false);
                scale = Kscale;
                trackTouchEvent(event);
                onProgressRefresh(scale, true);
                onStopTrackingTouch();
                // invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
                onStopTrackingTouch();
                setPressed(false);
                postInvalidate(); // see above explanation
                break;
        }
        return true;
    }
    


    

    
  

    /** 设置绘制风格 */
    private Paint setStyle()
    {
        Paint paint2 = new Paint();
        paint2.setColor(Color.rgb(62, 62, 62));
        
        /** 修改者：l00174030；修改原因：先画背景，在画进度条**/
        // 进度条的游标的字体大小改为30
        //paint2.setTextSize(20);
        paint2.setTextSize(30);
        
        return paint2;
    }
    
    /** 资源的初始化 */
    public void init()
    {
        bdPlaying = (BitmapDrawable)context.getResources().getDrawable(R.drawable.video_seekbar_position_point);
        bdSeekbar_background = (BitmapDrawable)context.getResources().getDrawable(R.drawable.video_seekbar_bg);
        // bdThumb_Pressed =
        // (BitmapDrawable)getResources().getDrawable(R.drawable.seek_pressed);
        // bdThumb_Stop =
        // (BitmapDrawable)getResources().getDrawable(R.drawable.seekbar_position_point);
        bdSeekbar_Played = (BitmapDrawable)(context.getResources().getDrawable(R.drawable.video_seekbar));
        bdSeekbar_Speed = (BitmapDrawable)(context.getResources().getDrawable(R.drawable.video_seekbar_speed));
        bdSeekbar_Cur_Up = (BitmapDrawable)context.getResources().getDrawable(R.drawable.video_seekbar_curson_point_up);
        bdSeekbar_Cur_Down =
            (BitmapDrawable)context.getResources().getDrawable(R.drawable.video_seekbar_curson_point_down);
        
        imgPlaying = bdPlaying.getBitmap();
        imgSeekbar_background = bdSeekbar_background.getBitmap();
        imgSeekbar_background =
            imgSeekbar_background.createScaledBitmap(imgSeekbar_background,
                SCREEN_SEEKBAR_LENGTH - bdPlaying.getIntrinsicWidth() / 2,
                5,
                false);
        // imgThumb_Pressed = bdThumb_Pressed.getBitmap();
        // imgThumb_Stop = bdThumb_Stop.getBitmap();
        imgSeekbar_Played = bdSeekbar_Played.getBitmap();
        imgSeekbar_Played =
            imgSeekbar_Played.createScaledBitmap(imgSeekbar_Played,
                SCREEN_SEEKBAR_LENGTH - bdPlaying.getIntrinsicWidth() / 2,
                5,
                false);
				
		/* BEGIN: Modified by s00211113 for DTS2014031902280  2014/03/19 */
        pos = SCREEN_SEEKBAR_LENGTH - bdPlaying.getIntrinsicWidth() / 2;
		/* END: Modified by s00211113 for DTS2014031902280  2014/03/19 */
        
        imgSeekbar_Speed=bdSeekbar_Speed.getBitmap();
        
        imgSeekbar_Speed =
        		imgSeekbar_Speed.createScaledBitmap(imgSeekbar_Speed,
                    SCREEN_SEEKBAR_LENGTH - bdPlaying.getIntrinsicWidth() / 2,
                    5,
                    false);
        
        imgSeekbar_Cur_Up = bdSeekbar_Cur_Up.getBitmap();
        
        imgSeekbar_Cur_Down = bdSeekbar_Cur_Down.getBitmap();
        
        bdCurson_Up_Width = bdSeekbar_Cur_Up.getIntrinsicWidth() / 2;
        
        playingWidth = bdPlaying.getIntrinsicWidth() / 2;
        
        // handlerThread = new HandlerThread("run ondraw");
        // handlerThread.start();
        //
        // myHandle = new MyHandle(handlerThread.getLooper());
    }
    
    
	/* END: Modified by s00211113 for DTS2014031902280  2014/03/19 */
    


    /** 开始绘制 */
    public void playDraw(Canvas canvas, int thumbPos)
    {
    	Log.i("VideoKey", "playDraw->thumbPos:" + thumbPos);
        /** 修改者：l00174030；修改原因：先画背景，在画进度条**/
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0Xcc1fb1e7);
        paint.setAntiAlias(true);
        RectF rectfbg = new RectF(0, 0 , thumbPos, getHeight());
        canvas.drawRect(rectfbg, paint);
    }
    
    /**
     * 绘制seek效果
     * @param canvas
     * @param seekPosition
     */
    public void seekDraw(Canvas canvas, int seekPosition){
    	if(!mIsNeedShowSeekLayout){
    		mSeekBarLayout.setSeekTimeVisibility(View.INVISIBLE);
    		return;
    	}
    		
        /** 修改者：l00174030；修改原因：先画背景，在画进度条**/
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0X6F1296db);
        paint.setAntiAlias(true);
        RectF rectfbg = new RectF(0, 0 , seekPosition, getHeight());
        canvas.drawRect(rectfbg, paint);
        mSeekBarLayout.updateSeekLayoutPosition(seekPosition);
    }
        
    /**
     * 设置父布局
     */
    public void setSeekBarLayout(SeekBarLayout seekBarLayout){
    	mSeekBarLayout = seekBarLayout;
    }
    
    /**
     * 是否显示Seek布局
     * @param needShow
     */
    public void setNeedShowSeekLayout(boolean needShow){
    	mIsNeedShowSeekLayout = needShow;
    }
    
}
