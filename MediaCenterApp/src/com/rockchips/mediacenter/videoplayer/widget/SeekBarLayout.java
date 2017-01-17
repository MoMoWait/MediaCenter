package com.rockchips.mediacenter.videoplayer.widget;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rockchips.mediacenter.R;
import com.rockchips.mediacenter.basicutils.constant.Constant;
import com.rockchips.mediacenter.basicutils.constant.Constant.PlayMode;
import com.rockchips.mediacenter.portable.IVideoViewAdapter;
import com.rockchips.mediacenter.videoplayer.data.VideoInfo;
import com.rockchips.mediacenter.videoplayer.widget.MyseekBar.OnSeekBarChangeListener;
import com.rockchips.mediacenter.viewutils.marqueetextview.MarqueeTextView;

public class SeekBarLayout extends RelativeLayout
{

    // 如果>15min才采用计算步进计算 15min

    // private static final int MIN_DURAION_TO_SEEK = 900000;

    private static final String TAG = "SeekBarLayout";

    // 最后的按键code
    private int lastKeyCode = -1;

    // zkf61715
    private long lastTouchTime;
    
    private static final int SEEK_FORWORD_MAX_ACCELEATE = 8;
    private static final int SEEK_BACKWORD_MAX_ACCELEATE = 0 - SEEK_FORWORD_MAX_ACCELEATE;

    /* BEGIN: Modified by s00211113 for DTS2014031902280 2014/03/19 */
    public SeekBarLayout(Context context)
    {
        super(context);
        SeekBarLayout.inflate(context, R.layout.video_seekbar_layout, this);
        init();
    }

    public SeekBarLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        SeekBarLayout.inflate(context, R.layout.video_seekbar_layout, this);
        init();
    }

    public SeekBarLayout(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        SeekBarLayout.inflate(context, R.layout.video_seekbar_layout, this);
        init();
    }

    /* END: Modified by s00211113 for DTS2014031902280 2014/03/19 */

    public static final String ACTION_MOUSE_DISPLAY_CHANGE = "android.intent.action.MOUSE_DISPLAY_CHANGE";

    private Drawable mThumbPlay = null;

    private Drawable mThumbPause = null;

    private Drawable mThumbPlayMouse = null;

    private Drawable mThumbPauseMouse = null;

    private static final int PLAY = 0;

    RelativeLayout.LayoutParams params = null;

    // zkf61715 避免一次按键产生两次onkeyup事件
    private boolean hasKeyDown = false;

    /**
     * 播放条
     */
    private MyseekBar mSeekBar = null;

    /**
     * 播放进度
     */
    // private int position = 0;

    /**
     * 显示总时间
     */
    private TextView durationTextView = null;

    /**
     * 播放时间
     */
    private TextView playedTextView = null;

    /**
     * 播放时间
     */
    private MarqueeTextView playerName = null;

    public SeekBarListener mSeekBarPopWindowListener = null;

    /**
     * 总时长
     */
    private int mDuration = 0;

    /**
     * 处理按键堆积
     */
    private long historyTime = System.currentTimeMillis();

    /** 修改者：l00174030；修改原因：UCD改变播控方式 **/
    // 快进倍率
    // private TextView accelerateImg = null;
    // 播控状态
    private ImageView playStatusImg = null;

    /****/

    /**
     * SeekBarListener 回调接口
     * @author t00181037
     * 
     */

    /***
     * 按键中
     */
    public boolean iskeyDown = false;

    // 快退快进的倍数
    public int Xacceleration = 0;

    // seekBar的正常播放进度
    int mprogress = 0;

    // seekBar的快进和拖动时的进度
    int sprogress = 0;

    /** Mender:l00174030;Reason:from 2.2 to 4.0 **/

    /**
     * tangss modify for merge android4.0 &android 4.2
     */
    private IVideoViewAdapter mVideoView;

    private VideoInfo mmbi;

    public boolean isTrackingTouch = false;

    // zkf61715 是否支持快进快退
    private boolean canAccelerate = true;

    // 视频是否准备好
    public boolean bPrepared = false;

    // zkf61715 是否快进快退到头
    private boolean isAcceleCompl = false;

    // zkf61715 64X和快速seek的临界点，小于此长度的片源不做seek处理
    private static final int TIME_CRISIS = 32 * 20 * 1000;

    private boolean seekIsNecessary = true;

    // ouxiaoyong add seek快进到片尾
    private boolean isAcceleToEnd = false;

    private boolean isSeekBarEnd = false;

    public void setSeekBarEnd(boolean isSeekBarEnd)
    {
        this.isSeekBarEnd = isSeekBarEnd;
    }

    public void setAcceleCompl(boolean isAcceleCompl)
    {
    	Log.i("VideoKey", "setAcceleCompl->isAcc:" + isAcceleCompl);
    	Log.i("VideoKey", "setAcceleCompl->stackTrace::" + Log.getStackTraceString(new Throwable()));
        this.isAcceleCompl = isAcceleCompl;
    }

    public boolean isAcceleCompl()
    {
        return isAcceleCompl;
    }

    public boolean getIskeyDown()
    {
        return mSeekBar.isOnkey;
    }

    public void setAcceleToEnd(boolean end)
    {
        isAcceleToEnd = end;
    }

    public void setbPrepared(boolean bPrepared)
    {
        this.bPrepared = bPrepared;
    }

    public boolean isbPrepared()
    {
        return this.bPrepared;
    }

    public interface SeekBarListener
    {

        /**
         * 在SeekBar中进行seek操作的时候
         * @param seekto
         */
        void seekto(int seekto);

        void reverseState();

        void hide(boolean showMenu);

        void immediatelyHide();

        void nextProgram();

        void preProgram();

        int getPlayState();

        void showMenu();

        void onXChange(int X);

        void onTrackingTouchChange(boolean isTrackingTouch);

        float onNan();

        void onBack(int keyCode, KeyEvent event);

    }

    /** Mender:l00174030;Reason:from 2.2 to 4.0 **/
    public IVideoViewAdapter getmVideoView()
    {
        return mVideoView;
    }

    /** Mender:l00174030;Reason:from 2.2 to 4.0 **/
    public void setVideoView(IVideoViewAdapter mVideoView)
    {
        this.mVideoView = mVideoView;
    }

    // zkf61715
    public void canAccelerate(boolean canAccelerate)
    {
        if (mSeekBar != null)
        {
            // 两个变量要保持一致
            this.canAccelerate = canAccelerate;
            mSeekBar.canAccelerate(canAccelerate);
        }
    }

    public VideoInfo getMmbi()
    {
        return mmbi;
    }

    public void setMmbi(VideoInfo mmbi)
    {
        this.mmbi = mmbi;
    }

    public void setListener(SeekBarListener seekBarPopWindowListener)
    {
        mSeekBarPopWindowListener = seekBarPopWindowListener;
    }

    /**
     * init 初始化进度条上的参数以及
     * @param screenHeight 要显示的高
     * @param screenWide 要显示的框
     */
    private void init()
    {
        initDrawable();
        initWidget();
        initAttribute();
        mSeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
        initThumb();
    }

    public void initAttribute()
    {
        mCurrPos = 0;
        setDuration(0);
        if (mmbi != null)
        {
            playerName.setText(mmbi.getmFileName());
            playerName.setFocusable(true);
        }
    }

    public void initThumb()
    {
        if (isMouseShow())
        {
            // Log.e(TAG, "isMouseShow  true");
            setThumb(mThumbPlay, 0);
        }
        else
        {
            // Log.e(TAG, "isMouseShow  false");
            setThumb(mThumbPlayMouse, 0);
        }
    }

    /**
     * OnSeekBarChangeListener ����Ĺ���?
     */
    public OnSeekBarChangeListener mSeekBarChangeListener = new OnSeekBarChangeListener()
    {
        public void onStopTrackingTouch(MyseekBar seekBar)
        {
        	Log.i(TAG, "onStopTrackingTouch");
            // TODO Auto-generated method stub
            // mVideoView.seekTo(mprogress);
            // isTrackingTouch = false;
            // mSeekBarPopWindowListener.onTrackingTouchChange(isTrackingTouch);
            // Xacceleration = 0;
            // mSeekBar.setXacceleration(0);
            // mSeekBarPopWindowListener.onXChange(0);
            mSeekBarPopWindowListener.seekto(mprogress);

        }

        public void onStartTrackingTouch(MyseekBar seekBar)
        {
        	Log.i(TAG, "onStartTrackingTouch");
            // TODO Auto-generated method stub
            isTrackingTouch = true;
            mSeekBarPopWindowListener.onTrackingTouchChange(isTrackingTouch);
        }

        // 按键操作快进时会回调此方法,通过此方法还可以控制快进的速率倍数，按确定键继续正常播放
        public void onKeyTounch(MyseekBar seekBar)
        {
        	Log.i("VideoKey", "SeekBarLayout->onKeyTounch");
            // 网络视频不支持快进快退
            if (getmSeekBar().isFromNetwork())
            {
                if (mSeekBar.isEnSure())
                {
                    doEnter();
                }
                else
                {
                    Log.d(TAG, "isEnSure 2");

                    doAccerate(mSeekBar.isLeft());

                    mSeekBarPopWindowListener.onXChange(mSeekBar.getXacceleration());

                }
                return;
            }

            if (!canAccelerate)
            {
            	Log.i("VideoKey", "onKeyTounch->!canAccelerate");
                switch (mSeekBar.getPlayMode())
                {
                    case PlayMode.PLAY_TRICK:
                    	setAcceleCompl(false);
                        if (mSeekBar.isXupdate())
                        {
                        	Log.i("VideoKey", "onKeyTounch->mSeekBar->isXupdate");
                            doAccerate(mSeekBar.isLeft());
                            mSeekBarPopWindowListener.onXChange(Xacceleration);
                            mSeekBar.setXupdate(false);
                        }
                        doEnter();
                        break;

                    case PlayMode.PLAY_SEEK:
                        // 避免长按时一直触发
                        if (!mSeekBar.hasKeyUp() && isAcceleCompl)
                        {
                            break;
                        }
                        long nowTime = System.currentTimeMillis();
                        if (nowTime - lastTouchTime < 500 && !mSeekBar.hasKeyUp())
                        {
                            break;
                        }
                        else
                        {
                            lastTouchTime = nowTime;
                        }
                        doAccerate(mSeekBar.isLeft());
                        if (!(Xacceleration == mSeekBar.getXacceleration()))
                        {
                            // 如果已经是此速率 ，就不必再重新设置了
                            mSeekBarPopWindowListener.onXChange(Xacceleration);
                        }
                        doEnter();
                        if (mSeekBar.hasKeyUp())
                        {
                            mSeekBar.setPlayMode(PlayMode.PLAY_TRICK);
                            mSeekBar.setXacceleration(1);
                            Xacceleration = 1;
                            mSeekBar.setOnkey(false);
                            mSeekBar.setEnSure(false);
                            mSeekBarPopWindowListener.onXChange(Xacceleration);
                            setAcceleCompl(false);
                        }
                        break;

                    default:
                        break;
                }
            }
            else
            {
                // zkf61715
                if (mSeekBar.getPlayMode() == PlayMode.PLAY_TRICK)
                {
                    if (mSeekBar.isEnSure())
                    {
                        mSeekBar.setXacceleration(1);
                        Xacceleration = 1;
                        mSeekBar.setOnkey(false);
                        mSeekBar.setEnSure(false);
                    }
                    else
                    {
                        doAccerate(mSeekBar.isLeft());
                    }
                    mSeekBarPopWindowListener.onXChange(Xacceleration);
                }

                else
                {

                    Log.d(TAG, "isEnSure 2");
                    if (!mSeekBar.hasKeyUp() && isAcceleToEnd)
                    {
                        return;
                    }
                    doAccerate(mSeekBar.isLeft());

                    if (!(Xacceleration == mSeekBar.getXacceleration()))
                    {
                        // 如果已经是此速率 ，就不必再重新设置了
                        mSeekBarPopWindowListener.onXChange(Xacceleration);
                    }
                    if (mSeekBar.needSeek() && seekIsNecessary)
                    {
                        mSeekBarPopWindowListener.seekto(sprogress);
                        mSeekBar.setScale(mSeekBar.getKscale());
                    }
                    // zkf61715 当收到keyup事件时正常播放
                    if (mSeekBar.hasKeyUp())
                    {
                        mSeekBar.setPlayMode(PlayMode.PLAY_TRICK);
                        mSeekBar.setXacceleration(1);
                        Xacceleration = 1;
                        // zkf61715 DTS2014011704430
                        // 避免从track快进快退模式转到seek模式500-2000ms正常播放时松开按键进度条不消失
                        if (!seekIsNecessary || mSeekBar.getXacceleration() == 1)
                            mSeekBarPopWindowListener.onXChange(Xacceleration);
                        mSeekBar.setOnkey(false);
                        mSeekBar.setEnSure(false);
                    }
                }
            }
        }

        // 拖动和通过左右键 操作快进的话，会回调此方法
        public void onProgressChanged(MyseekBar seekBar, int progress, boolean fromUser, int kprogress)
        {
        	Log.i("VideoKey", "SeekBarLayout->onProgressChanged");
            // TODO Auto-generated method stub
            // Log.e(TAG, "seekbarlayout onProgressChanged" + progress + ":" +
            // kprogress);
            mprogress = progress;
            sprogress = kprogress;
        }

        public float onNan()
        {
        	Log.i(TAG, "onNan");
            // TODO Auto-generated method stub
            return mSeekBarPopWindowListener.onNan();
        }
    };

    /**
     * �ؼ��ĳ�ʼ��
     */
    public void initWidget()
    {
        if (mSeekBar == null)
        {
            mSeekBar = (MyseekBar) findViewById(R.id.seekbar);
            mSeekBar.setSeekBarLayout(this);
        }

        if (durationTextView == null)
        {
            durationTextView = (TextView) findViewById(R.id.totalDuration);
        }

        if (playedTextView == null)
        {
            playedTextView = (TextView) findViewById(R.id.elapsedDuration);
        }

        if (playerName == null)
        {
            playerName = (MarqueeTextView) findViewById(R.id.elapsedName);
            playerName.setFocusable(true);
        }

        /* BEGIN: Modified by s00211113 for DTS2014031902280 2014/03/19 */
        /** 修改者：l00174030；修改原因：UCD改变播控方式 **/
        // 快进倍率
        // if (accelerateImg == null)
        // {
        // accelerateImg = (TextView)findViewById(R.id.accelerateImg);
        // }
        // 播控状态
        if (playStatusImg == null)
        {
            playStatusImg = (ImageView) findViewById(R.id.playStatusImg);
        }
        //
        // if (rlCursor == null)
        // {
        // rlCursor = (RelativeLayout)findViewById(R.id.rl_play_cursor);
        // }
        /* END: Modified by s00211113 for DTS2014031902280 2014/03/19 */
    }

    /**
     * ͼƬ��Դ�ĳ�ʼ��
     */
    public void initDrawable()
    {
        mThumbPlay = getResources().getDrawable(R.drawable.video_seekbar_thumb_play);
        mThumbPause = getResources().getDrawable(R.drawable.video_seekbar_thumb_pause);
        mThumbPlayMouse = getResources().getDrawable(R.drawable.video_seekbar_thumb_play_mouse);
        mThumbPauseMouse = getResources().getDrawable(R.drawable.video_seekbar_thumb_pause_mouse);
    }

    /**
     * ���Ҽ���˿�� ����
     */
    public void doAccerate(boolean isleft)
    {
        if (!mSeekBar.isFromNetwork())
        {
            if (mSeekBar.getXacceleration() == 1)
            {
                mSeekBar.setBflush(true);
            }
            else
            {
                mSeekBar.setBflush(false);
            }
        }
        // zkf61715
        if (mSeekBar.getPlayMode() == PlayMode.PLAY_TRICK)
        {
            int newx = 0;
            Xacceleration = mSeekBar.getXacceleration();
            if (Xacceleration == 0)
            {
                Xacceleration = 1;
            }
            if (isleft)
            {
                // zkf61715 快进快退体验，和iptv保持一致
//                if (Xacceleration == SEEK_BACKWORD_MAX_ACCELEATE || Xacceleration >= 1)
//                {
//                    newx = -2;
//                }
//                else
//                {
//                    newx = Xacceleration * 2;
//                }
                newx = -2;
            }
            else
            {
//                if (Xacceleration == SEEK_FORWORD_MAX_ACCELEATE || Xacceleration <= 1)
//                {
//                    newx = 2;
//                }
//                else
//                {
//                    newx = Xacceleration * 2;
//                }
                newx = 2;
            }
            Xacceleration = newx;
        }

        else
        {

            long persistTime = mSeekBar.getPersistTime();
            if (isleft)
            {

                if (persistTime < 10000)
                {
//                    if (!mSeekBar.isTrcikBefore())
//                    {
////                        Xacceleration = -4;
//                        Xacceleration = -2;
//                    }
//                    else
//                    {
//                        Xacceleration = 1;
//                    }
                    Xacceleration = -2;
                }
//                else if (persistTime >= 2000 && persistTime < 1000)
//                {
//                    Xacceleration = -2;
//                }                
                else if (persistTime >= 10000 && seekIsNecessary)
                {
                    // 此时是正常播放速度，但是要调用到seek来精确定位
                    Xacceleration = 1;
                }
                else
                {
                    Log.i(TAG, "------->" + persistTime);
                }
            }
            else
            {
                if (persistTime < 10000)
                {
//                    if (!mSeekBar.isTrcikBefore())
//                    {
//                        Xacceleration = 2;
//                    }
//                    else
//                    {
//                        Xacceleration = 1;
//                    }
                    Xacceleration = 2;
                }
//                else if (persistTime >= 2000 && persistTime < 10000)
//                {
//                    Xacceleration = 2;
//                }                
                else if (persistTime >= 10000 && seekIsNecessary)
                {
                    Xacceleration = 1;
                }
                else
                {
                    Log.i(TAG, "------->" + persistTime);
                }
            }

            if (mSeekBar.hasKeyUp())
            {
//                Xacceleration = 1;
                mSeekBar.setPlayMode(PlayMode.PLAY_TRICK);
            }
            Log.i(TAG, "-------> Xacceleration" + Xacceleration);
        }
        // Log.e("onkey", "----------------------------x" + Xacceleration);
        // mSeekBar.setXacceleration(Xacceleration);
        // sleepTime();
        if (mSeekBar.isFromNetwork())
        {
            mSeekBar.setXacceleration(Xacceleration);
        }
        mSeekBar.setSleepTime(500);

        mSeekBar.setOnkey(true);
        mSeekBar.setTotalTime(mVideoView.getDuration());
    }

    /**
     * ȷ���� ����
     */
    public void doEnter()
    {
    	Log.i("VideoKey", "doEnter");
    	Log.i("VideoKey", "doEnter->isAcceleCompl:" + isAcceleCompl);
        if (mSeekBar.isFromNetwork())
        {
            doEnterNetwork();
            return;
        }

        Log.d(TAG, "isEnSure 1");
        // mVideoView.seekTo(sprogress);
        if (isAcceleCompl)
        {
            return;
        }
        if (!mVideoView.isSeeking())
        {
            mSeekBarPopWindowListener.seekto(sprogress);
        }
        // Log.e("aaaa", "onKeyTounch  draw  scale==" + mSeekBar.getScale() +
        // " Kscale" + mSeekBar.getKscale());
        mSeekBar.setScale(mSeekBar.getKscale());
        if (mSeekBar.isEnSure())
        {
            mSeekBar.setOnkey(false);
            mSeekBar.setEnSure(false);
            // mSeekBar.historyPosition = -1;
            Xacceleration = 0;
            mSeekBar.setXacceleration(0);
            mSeekBarPopWindowListener.onXChange(mSeekBar.getXacceleration());
        }
        // mSeekBarPopWindowListener.hideAcc();

    }

    private void doEnterNetwork()
    {
        mSeekBarPopWindowListener.seekto(sprogress);
        mSeekBar.setScale(mSeekBar.getKscale());
        mSeekBar.setOnkey(false);
        mSeekBar.setEnSure(false);
        mSeekBar.historyPosition = -1;
        Xacceleration = 0;
        mSeekBar.setXacceleration(0);
        mSeekBarPopWindowListener.onXChange(0);
    }

    public void resetSeekbar()
    {
        Log.d(TAG, "resetSeekbar 1");
        mSeekBar.setOnkey(false);
        mSeekBar.setEnSure(false);
        mSeekBar.historyPosition = -1;
        Xacceleration = 0;
        mSeekBar.setXacceleration(0);
    }

    /**
     * ���� ˢ��ʱ��
     */
    public void sleepTime()
    {

        switch (Math.abs(Xacceleration))
        {
            case 1:
                mSeekBar.setSleepTime(200);
                break;
            case 2:
                mSeekBar.setSleepTime(150);
                break;
            case 3:
                mSeekBar.setSleepTime(100);
                break;
            default:
                break;
        }
        // Log.e("onkey", "sleeptime==" + mSeekBar.getSleepTime());
    }

    public void startMouseStateChangedReceiver(Context context)
    {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_MOUSE_DISPLAY_CHANGE);
        context.registerReceiver(mouseStateChangedReceiver, filter);//, Constant.BROADCAST_PERMISSION_MOUNT_UNMOUNT_FILESYSTEMS, null);
    }

    public void stopMouseStateChangedReceiver(Context context)
    {
        context.unregisterReceiver(mouseStateChangedReceiver);
    }

    private BroadcastReceiver mouseStateChangedReceiver = new BroadcastReceiver()
    {

        public void onReceive(Context context, Intent intent)
        {
            if (ACTION_MOUSE_DISPLAY_CHANGE.equals(intent.getAction()))
            {
                boolean bMouseShow = intent.getBooleanExtra("MouseShow", false);
                if (bMouseShow)
                {
                    if (mSeekBarPopWindowListener != null)
                    {

                        if (mSeekBarPopWindowListener.getPlayState() == PLAY)
                        {
                            setThumb(mThumbPlayMouse, mSeekBar.getProgress());
                        }
                        else
                        {
                            setThumb(mThumbPauseMouse, mSeekBar.getProgress());
                        }
                    }
                }
                else
                {
                    if (mSeekBarPopWindowListener != null)
                    {

                        if (mSeekBarPopWindowListener.getPlayState() == PLAY)
                        {
                            setThumb(mThumbPlay, mSeekBar.getProgress());
                        }
                        else
                        {
                            setThumb(mThumbPause, mSeekBar.getProgress());
                        }
                    }
                }
            }
        }
    };

    public void setDuration(int duration)
    {

        mDuration = duration;

        mSeekBar.setMax(duration);
        mSeekBar.setProgress(0);

        durationTextView.setText(getTime(duration));

        playedTextView.setText(getTime(0));

        if (mmbi != null)
        {
            playerName.setText(mmbi.getmFileName());
            playerName.setFocusable(true);
        }

        if (mDuration < TIME_CRISIS)
        {
            seekIsNecessary = false;
        }
        else
        {
            seekIsNecessary = true;
        }

    }

    private int getDuration()
    {
        return mDuration;

    }

    /***
     * ��¼��ǰ���Ž��?
     */
    private int mCurrPos = 0;

    public int getmCurrPos()
    {
        return mCurrPos;
    }

    public void setmCurrPos(int mCurrPos)
    {
        this.mCurrPos = mCurrPos;
    }

    /**
     * seek ��ʾseek��λ��
     * @param duration
     */
    public void seekto(int duration)
    {
    	Log.i("VideoKey", "SeekBarLayout->isSeekBarEnd:" + isSeekBarEnd);
        if (isSeekBarEnd && (mSeekBar.getXacceleration() == 1 || mSeekBar.getXacceleration() == 0) && !mSeekBar.isLeft()
                && mSeekBar.getPlayMode() == PlayMode.PLAY_SEEK)
        {
            return;
        }
        Log.d("VideoKey", "SeekBarLayout->seekto->duration:  " + duration);
        mCurrPos = duration;

        // Log.e(TAG, "seekto: " + duration + "--->time --" +
        // System.currentTimeMillis());

        if (mSeekBar == null)
        {
            // Log.e(TAG, "seek mSeekBar is null");
            return;
        }
        if (!mSeekBar.isOnkey() && !mVideoView.isSeeking())
        {

            mSeekBar.setProgress2(duration);
        }
        else
        {
            mSeekBar.setProgress3(duration);
        }

        playedTextView.setText(getTime(duration));

        if (isAcceleToEnd)
        {
            isSeekBarEnd = true;
        }
    }

    public MyseekBar getmSeekBar()
    {
        return mSeekBar;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event)
    {
    	Log.i("VideoKey", "SeekBarLayout->onKeyUp->keyCode:" + keyCode);
        if (mSeekBar.isFromNetwork())
        {
            return onKeyUpNetwork(keyCode, event);
        }
        switch (keyCode)
        {
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_MEDIA_REWIND:
                if (hasKeyDown)
                {
                    mSeekBar.onKeyUp(keyCode, event);
                    hasKeyDown = false;
                }
                else
                {
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                if (hasKeyDown)
                {
                    mSeekBar.onKeyUp(keyCode, event);
                    hasKeyDown = false;
                }
                else
                {
                    return true;
                }
                break;
            default:
                mSeekBar.setHasKeyUp(true);
                break;
        }
        return super.onKeyUp(keyCode, event);
    }

    private boolean onKeyUpNetwork(int keyCode, KeyEvent event)
    {
        switch (keyCode)
        {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                mSeekBar.onKeyUp(keyCode, event);
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                mSeekBar.onKeyUp(keyCode, event);
                break;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
    	Log.i("VideoKey", "SeekBarLayout->onKeyDown->keyCode:" + keyCode);
        // ���������ϵͳ���?��Ҫ��������¼�������������Ҳ������������?ԭ500ms����һ��)
        switch (keyCode)
        {
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                return super.onKeyDown(keyCode, event);
        }
        long currentTime = System.currentTimeMillis();
        Log.i(TAG, "---onkeydown--1>KEYCODE_CODE =" + keyCode + "----lastkeycode = " + lastKeyCode);
        switch (keyCode)
        {
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_MEDIA_REWIND:
                if (!mVideoView.isPlaying())
                {
                    // zkf61715 DTS2013112807986如果在暂停的时候进行快进快退，先让视频进行正常播放
                    mSeekBarPopWindowListener.reverseState();
                }
                if (!mSeekBar.isFromNetwork())
                {
                    // zkf61715
                    setAcceleToEnd(false);
                    isSeekBarEnd = false;
                    hasKeyDown = true;
                }else{
                	//不处理网络设备
                	return true;
                }
                Log.d("onkey", "----->KEYCODE_DPAD_LEFT" + isbPrepared() + mVideoView.isSeeking());

                if (isbPrepared() && !mVideoView.isSeeking())
                {
                    historyTime = currentTime;

                    iskeyDown = true;

                    mSeekBar.onKeyDown(keyCode, event);
                }
                lastKeyCode = keyCode;
                return true;

            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                if (!mVideoView.isPlaying())
                {
                    mSeekBarPopWindowListener.reverseState();
                }
                if (!mSeekBar.isFromNetwork())
                {
                    hasKeyDown = true;
                }else{
                	return true;
                }
                Log.d("onkey", "----->KEYCODE_DPAD_RIGHT" + isbPrepared() + mVideoView.isSeeking());
                if (isbPrepared() && !mVideoView.isSeeking())
                {
                    historyTime = currentTime;
                    iskeyDown = true;
                    mSeekBar.onKeyDown(keyCode, event);

                }
                lastKeyCode = keyCode;
                return true;
            default:
                break;
        }

        if ((currentTime - historyTime) < 400 && keyCode == lastKeyCode)
        {
            return true;
        }

        lastKeyCode = keyCode;
        historyTime = currentTime;

        switch (keyCode)
        {
            case KeyEvent.KEYCODE_DPAD_CENTER:
                Log.d(TAG, "----->KEYCODE_DPAD_CENTER");
                if (!isbPrepared())
                {
                    break;
                }
                if (mSeekBar.isOnkey())
                {
                    Log.d(TAG, "----->KEYCODE_DPAD_CENTER  onkey");

                    mSeekBar.onKeyDown(keyCode, event);

                    iskeyDown = false;
                }
                else
                {
                    mSeekBarPopWindowListener.reverseState();
                }
                break;
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                if (!isbPrepared() || mVideoView.isSeeking())
                {
                    break;
                }
                iskeyDown = false;
                // Log.e("cccc", "------" + mSeekBar.isOnkey());
                if (mSeekBar.isOnkey())
                {
                    mSeekBar.onKeyDown(KeyEvent.KEYCODE_DPAD_CENTER, event);
                }
                else
                {
                    mSeekBarPopWindowListener.reverseState();
                }
                break;

            case KeyEvent.KEYCODE_DPAD_UP:
                /*
                 * Xacceleration = 0; mSeekBar.setXacceleration(0);
                 */
                // Log.e(TAG, "mSeekBar.Kscale up" + mSeekBar.Kscale);
                if (mSeekBar.isOnkey && Xacceleration != 0)
                {
                }
                else
                {
                    mSeekBarPopWindowListener.preProgram();
                }
                break;

            case KeyEvent.KEYCODE_DPAD_DOWN:
                // Xacceleration = 0;
                // mSeekBar.setXacceleration(0);

                // Log.e(TAG, "mSeekBar.Kscale" + mSeekBar.Kscale);
                if (mSeekBar.isOnkey && Xacceleration != 0)
                {
                }
                else
                {
                    mSeekBarPopWindowListener.nextProgram();
                }
                break;

            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_MEDIA_STOP:
                if (!mSeekBar.isPlaying() && !mSeekBar.isOnkey())
                {
                    mSeekBarPopWindowListener.onBack(keyCode, event);

                    break;
                }
                if (Xacceleration != 0)
                {
                    if (mSeekBar.isFromNetwork())
                    {
                        mSeekBar.Kscale = mSeekBar.scale;

                        // Log.e(TAG, "mSeekBar.Kscale" + mSeekBar.scale);

                        mSeekBar.setOnkey(false);
                        mSeekBar.setEnSure(false);
                        mSeekBar.setReAccelerate();

                        Xacceleration = 0;
                        mSeekBar.setXacceleration(0);

                        mSeekBar.onProgressRefresh(mSeekBar.Kscale, true);
                    }
                    else
                    {
                        mSeekBar.setXacceleration(1);
                        Xacceleration = 1;
                        mSeekBar.setEnSure(false);
                        mSeekBar.setOnkey(false);
                        mSeekBarPopWindowListener.onXChange(Xacceleration);
                    }
                }

                mSeekBarPopWindowListener.immediatelyHide();
                if (!mSeekBar.isFromNetwork())
                    mSeekBarPopWindowListener.onBack(keyCode, event);
                break;

            case KeyEvent.KEYCODE_MENU:

                mSeekBarPopWindowListener.hide(true);

                return super.onKeyDown(keyCode, event);

            default:
                return super.onKeyDown(keyCode, event);

        }

        return true;
    }

    // @Override
    // public boolean onKeyUp(int keyCode, KeyEvent event)
    // {
    // Message msg = new Message();
    //
    // msg.what = SAMPLE_MSG_START_SEEKTO;
    // switch (keyCode)
    // {
    // case KeyEvent.KEYCODE_DPAD_LEFT:
    //
    // Log.e(TAG, "onKeyup KEYCODE_DPAD_LEFT seekto:" + " pressTime:" +
    // mSeekPressTime + " mCurrPos:"
    // + mCurrPos);
    // //mSeekBarPopWindowListener.seekto(mCurrPos);
    // sampleHandler.sendMessageDelayed(msg, 200);
    //
    // break;
    //
    // case KeyEvent.KEYCODE_DPAD_RIGHT:
    //
    // Log.e(TAG, "onKeyup KEYCODE_DPAD_RIGHT seekto:" + " pressTime:" +
    // mSeekPressTime + " mCurrPos:"
    // + mCurrPos);
    // sampleHandler.sendMessageDelayed(msg, 200);
    //
    // break;
    //
    // default:
    //
    // break;
    // }
    //
    // Log.e(TAG, "onKeyUp" + 1);
    // mSeekBarPopWindowListener.hide(false);
    // sampleHandler.stopCount();
    // return true;
    // }

    public void pause()
    {
        mSeekBar.setPlaying(false);

        if (isMouseShow())
        {
            setThumb(mThumbPauseMouse, mSeekBar.getProgress());
        }
        else
        {
            setThumb(mThumbPause, mSeekBar.getProgress());

        }
    }

    public void play()
    {
        mSeekBar.setPlaying(true);

        if (isMouseShow())
        {
            setThumb(mThumbPlayMouse, mSeekBar.getProgress());
        }
        else
        {
            setThumb(mThumbPlay, mSeekBar.getProgress());

        }

    }

    private void setThumb(Drawable d, int left)
    {
        long width = mSeekBar.getWidth();
        long duration = getDuration();

        if (duration != 0)
        {
            left = (int) (left * width / duration);
        }
        else
        {
            left = 0;
        }

        if (left > d.getIntrinsicWidth())
        {
            left = left - d.getIntrinsicWidth();
        }

        d.setBounds(new Rect(left, 0, left + d.getIntrinsicWidth(), d.getIntrinsicHeight()));
        mSeekBar.setThumb(d);
        mSeekBar.setThumbOffset(0);

    }

    /**
     * 影片的时间格式化
     * 
     * @param temptime int
     * @return str
     */
    public String getTime(int temptime)
    {
        temptime /= 1000;
        int minute = temptime / 60;
        int hour = minute / 60;
        int second = temptime % 60;
        minute %= 60;

        String str = null;
        if (hour == 0)
        {
            str = String.format("%02d:%02d:%02d", hour, minute, second);
        }
        else
        {
            str = String.format("%02d:%02d:%02d", hour, minute, second);
        }

        return str;
    }

    View.OnTouchListener mTouchListener = new View.OnTouchListener()
    {

        public boolean onTouch(View v, MotionEvent event)
        {
            if (KeyEvent.ACTION_DOWN == event.getAction())
            {
            }
            else if (KeyEvent.ACTION_UP == event.getAction())
            {
            }

            if (mSeekBarPopWindowListener != null)
            {
                mSeekBarPopWindowListener.hide(false);
            }

            return true;
        }
    };

    private static boolean isMouseShow()
    {
        boolean isShow = false;
        /*
         * IMouseManager mouseManager = null; IBinder service = ServiceManager.getService("mouse"); if (service != null) { mouseManager =
         * IMouseManager.Stub.asInterface(service); } if (null != mouseManager) { try { isShow = mouseManager.isMouseShow(); } catch (RemoteException
         * e) { } }
         */

        try
        {
            Class<?> cls = Class.forName("android.os.ServiceManager");
            if (cls != null)
            {
                Class<?>[] paraTypes = new Class[]
                { String.class };
                Method method = cls.getMethod("getService", paraTypes);
                if (method != null)
                {
                    Object[] paraValues = new Object[]
                    { "mouse" };

                    Object service = method.invoke(cls, paraValues);
                    if (service != null)
                    {
                        Class<?> clsMouseManager = Class.forName("android.os.IMouseManager$Stub");
                        Class<?>[] paraTypesX = new Class[]
                        { IBinder.class };
                        Method asInterfaceMethod = clsMouseManager.getMethod("asInterface", paraTypesX);
                        if (asInterfaceMethod != null)
                        {
                            Object mouseManager = asInterfaceMethod.invoke(asInterfaceMethod, service);
                            if (mouseManager != null)
                            {
                                Method isMouseShowMethod = mouseManager.getClass().getMethod("isMouseShow");
                                if (isMouseShowMethod != null)
                                {
                                    Object isMouseShow = isMouseShowMethod.invoke(mouseManager);
                                    if (isMouseShow != null)
                                    {
                                        isShow = ((Boolean) isMouseShow).booleanValue();
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
        catch (ClassNotFoundException e)
        {
            Log.e(TAG, "ClassNotFoundException", e);
        }
        catch (SecurityException e)
        {
            Log.e(TAG, "SecurityException", e);
        }
        catch (NoSuchMethodException e)
        {
            Log.e(TAG, "NoSuchMethodException", e);
        }
        catch (IllegalArgumentException e)
        {
            Log.e(TAG, "IllegalArgumentException", e);
        }
        catch (IllegalAccessException e)
        {
            Log.e(TAG, "IllegalAccessException", e);
        }
        catch (InvocationTargetException e)
        {
            Log.e(TAG, "InvocationTargetException", e);
        }
        return isShow;
    }

    /* BEGIN: Modified by s00211113 for DTS2014031902280 2014/03/19 */
    // /** 修改者：l00174030；修改原因：UCD改变播控方式 **/
    // /**
    // * 显示快进的倍率
    // *
    // * @param rate 需要显示的速率
    // */
    // public void ShowAccelerate(int rate)
    // {
    // if (accelerateImg != null)
    // {
    // switch (rate)
    // {
    // // 0消失
    // case 0:
    // // 1倍加速
    // case 1:
    // accelerateImg.setVisibility(View.INVISIBLE);
    // break;
    // // 2倍加速
    // case 2:
    // accelerateImg.setVisibility(View.VISIBLE);
    // accelerateImg.setTextSize(18);
    // accelerateImg.setText("2X ");
    // break;
    // // 3倍加速
    // case 4:
    // accelerateImg.setVisibility(View.VISIBLE);
    // accelerateImg.setTextSize(18);
    // accelerateImg.setText("4X ");
    // break;
    // case 8:
    // accelerateImg.setVisibility(View.VISIBLE);
    // accelerateImg.setTextSize(18);
    // accelerateImg.setText("8X ");
    // break;
    // case 16:
    // accelerateImg.setVisibility(View.VISIBLE);
    // accelerateImg.setTextSize(18);
    // accelerateImg.setText("16X ");
    // break;
    // case 32:
    // accelerateImg.setVisibility(View.VISIBLE);
    // accelerateImg.setTextSize(18);
    // accelerateImg.setText("32X ");
    // break;
    // case 64:
    // accelerateImg.setVisibility(View.VISIBLE);
    // accelerateImg.setTextSize(18);
    // accelerateImg.setText("64X ");
    // break;
    // default:
    // accelerateImg.setVisibility(View.INVISIBLE);
    // break;
    // }
    //
    // Log.i(TAG, "accelerateImg is " + rate);
    // }
    // else
    // {
    // Log.e(TAG, "accelerateImg is null.");
    // }
    //
    // }
    /* END: Modified by s00211113 for DTS2014031902280 2014/03/19 */

    /**
     * 播控状态图标控制
     * @param imgId 图标
     */
    public void ShowPlayStatusImg(int imgId)
    {
        if (playStatusImg == null)
        {
            Log.e(TAG, "playStatusImg is null");
            return;
        }

        // 如果小于零，则要隐藏
        if (imgId < 0)
        {
            playStatusImg.setBackgroundDrawable(null);
            playStatusImg.setVisibility(View.GONE);
        }
        else
        {
            playStatusImg.setVisibility(View.VISIBLE);

            /* END: Modified by s00211113 for DTS2014031902280 2014/03/19 */
            playStatusImg.setImageResource(imgId);
            /* END: Modified by s00211113 for DTS2014031902280 2014/03/19 */
        }
    }

    /**
     * 获取播放控制的图片显示状态
     * @return 显示状态
     */
    public int getPlayStatusImgVisible()
    {
        if (playStatusImg == null)
        {
            return -1;
        }

        return playStatusImg.getVisibility();
    }    
}
