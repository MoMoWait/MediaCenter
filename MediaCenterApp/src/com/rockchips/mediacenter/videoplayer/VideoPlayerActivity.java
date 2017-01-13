/**
 * 
 * com.rockchips.iptv.stb.dlna.player
 * VideoPlayerActivity.java
 * 
 * AR-0000698410
 * 4.2平台通地海思播放器实现trickplay
 *  
 * AR-0000698411
 * 4.0中通过seek实现trickplay
 * 
 * AR-0000698413
 * 视频播放的功能 
 * 
 * AR-0000698414
 * 视频播放的性能
 * 
 * 2011-10-18 下午02:02:55
 * Copyright 2011 Huawei Technologies Co., Ltd
 * 
 */
package com.rockchips.mediacenter.videoplayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import android.R.anim;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.TimedText;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.ServiceManager;
import android.os.storage.IMountService;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
import java.io.File;

import momo.cn.edu.fjnu.androidutils.data.CommonValues;
import momo.cn.edu.fjnu.androidutils.utils.DeviceInfoUtils;
import momo.cn.edu.fjnu.androidutils.utils.SizeUtils;

import com.hisilicon.android.hibdinfo.HiBDInfo;
import com.hisilicon.android.mediaplayer.HiMediaPlayer;
import com.rockchips.mediacenter.R;
import com.rockchips.mediacenter.api.localImp.LocalDeviceManager;
import com.rockchips.mediacenter.basicutils.bean.LocalMediaInfo;
import com.rockchips.mediacenter.basicutils.constant.Constant;
import com.rockchips.mediacenter.basicutils.constant.Constant.PlayMode;
import com.rockchips.mediacenter.basicutils.util.MathUtil;
import com.rockchips.mediacenter.basicutils.util.Mount;
import com.rockchips.mediacenter.basicutils.util.PlatformUtil;
import com.rockchips.mediacenter.basicutils.util.StringUtils;
import com.rockchips.mediacenter.config.PlatformConfig;
import com.rockchips.mediacenter.portable.IMediaPlayerAdapter;
import com.rockchips.mediacenter.portable.IVideoViewAdapter;
import com.rockchips.mediacenter.portable.bean.AudioInfoOfVideo;
import com.rockchips.mediacenter.portable.bean.SubInfo;
import com.rockchips.mediacenter.portable.hisi.HisiVideoView;
import com.rockchips.mediacenter.portable.listener.OnBufferingUpdateListener;
import com.rockchips.mediacenter.portable.listener.OnCompleteListener;
import com.rockchips.mediacenter.portable.listener.OnErrorListener;
import com.rockchips.mediacenter.portable.listener.OnFastBackwordCompleteListener;
import com.rockchips.mediacenter.portable.listener.OnFastForwardCompleteListener;
import com.rockchips.mediacenter.portable.listener.OnInfoListener;
import com.rockchips.mediacenter.portable.listener.OnPreparedListener;
import com.rockchips.mediacenter.portable.listener.OnSeekCompleteListener;
import com.rockchips.mediacenter.portable.orig.OrigVideoView;
import com.rockchips.mediacenter.activity.MainActivity;
import com.rockchips.mediacenter.bean.LocalDevice;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.dobly.DoblyPopWin;
import com.rockchips.mediacenter.videoplayer.bd.BDInfo;
import com.rockchips.mediacenter.videoplayer.data.HistoryListRecord;
import com.rockchips.mediacenter.videoplayer.data.PlayerStateRecorder;
import com.rockchips.mediacenter.videoplayer.data.VideoInfo;
import com.rockchips.mediacenter.videoplayer.data.HistoryListRecord.SubObject;
import com.rockchips.mediacenter.videoplayer.widget.PlayListShowLayout;
import com.rockchips.mediacenter.videoplayer.widget.PlayListShowLayoutBase;
import com.rockchips.mediacenter.videoplayer.widget.SeekBarLayout;
import com.rockchips.mediacenter.videoplayer.widget.SubtitleSelectPopup;
import com.rockchips.mediacenter.videoplayer.widget.SeekBarLayout.SeekBarListener;
import com.rockchips.mediacenter.videoplayer.widget.SubtitleSelectPopup.OnSubtileSelectListener;
import com.rockchips.mediacenter.view.VideoSettingDialog;
import com.rockchips.mediacenter.viewutils.menu.BottomPopMenu;
import com.rockchips.mediacenter.viewutils.menu.MenuCategory;
import com.rockchips.mediacenter.viewutils.menu.MenuItemImpl;
import com.rockchips.mediacenter.viewutils.menu.OnSelectTypeListener;
import com.rockchips.mediacenter.viewutils.menu.PopMenu;
import com.rockchips.mediacenter.viewutils.timelayout.TimeLayout;
import com.rockchips.mediacenter.viewutils.timeseek.TimeSeekDialog;
import com.rockchips.mediacenter.viewutils.timeseek.TimeSeekDialog.OnTimeSeekListener;

/**
 * 
 * VideoPlayerActivity
 * 
 * AR-0000698426 播放异常处理
 * 
 * 2011-10-18 下午02:02:55
 * 
 * @author t00181037,z00184367,w00184463
 * @version 1.0.0
 * 
 */
public class VideoPlayerActivity extends PlayerBaseActivity implements OnSelectTypeListener
{
    private static final String TAG = "VideoPlayerActivity";

    private SeekBarLayout mSbpw = null;

    //
    // /**修改者：l00174030；修改原因：把2.2的移植到应用层来做 **/
    // private DLNAVideoView mVV = null;
    // 增加4.2兼容
    private IVideoViewAdapter mVVAdapter = null;

    private View mVV = null;

    // MIN MSG CODE ; for remove all messages
    public static final int MSG_UI_VIDEOVIEW_MIN = 0;

    protected static final int MSG_PROGRESS_CHANGED = 0;

    protected static final int MSG_HIDE_CONTROLER = 1;

    protected static final int MSG_SHOW_CONTROLER = 2;

    protected static final int MSG_SYNC_SEEK_POS = 3;

    protected static final int MSG_SYNC_SEEK = 4;

    protected static final int MSG_STOP_SYNC_SEEK = 5;

    protected static final int MSG_HIDE_ACCELERATION = 15;

    protected static final int MSG_SHOW_ACCELERATION = 16;

    protected static final int MSG_SHOW_PROGRESS = 18;

    protected static final int MSG_HIDE_PROGRESS = 19;

    protected static final int MSG_MCS_PLAY = 20;

    protected static final int MSG_MCS_HIDEMODE = 21;

    /**
     * 当前播放的媒体下线，播放下一个
     */
    protected static final int MSG_CURRENT_DOWN_PLAY_NEXT = 6;

    public static final int MSG_UI_PROCESSBAR = 7;

    private static final int MSG_UI_VIDEOVIEW_SETDATA = 8;

    public static final int MSG_UI_VIDEOVIEW_STOP = 9;

    public static final int MSG_UI_VIDEOVIEW_PAUSE = 10;

    public static final int MSG_UI_VIDEOVIEW_PLAY = 11;

    public static final int MSG_UI_VIDEOVIEW_REVERSE_STATE = 12;

    public static final int MSG_UI_VIDEOVIEW_SEEK_TO = 13;

    public static final int MSG_UI_VIDEOVIEW_SAVE_POS = 14;

    public static final int MSG_UI_VIDEOVIEW_MCSPLAY = 17;

    // MAX MSG CODE ; for remove all messages
    public static final int MSG_UI_VIDEOVIEW_MAX = 30;

    protected static final int DELAY_TIME = 5000;

    protected boolean bContinue = false;

    // 之前是否被用户暂停播放器（遥控器暂停、推送端暂停、甩屏端暂停）
    private boolean bIsPausedByUser = false;

    // 提示
    private Toast mToast = null;

    /**
     * 等待进度
     */
    private ProgressBar mCircleProgressBar = null;

    private UIHandler mUIHandler = null;

    // zkf61715 seek为假异步，为避免响应超时，需要在子线程中执行seek操作
    private final String SEEK_THREAD_TAG = "SeekThread";

    private HandlerThread mSeekHandlerThread = null;

    private SeekHandler mSeekHandler = null;

    // 循环播放时，遇到连续的6次无法播放时，退出播放器
    private static final int PLAYER_ERROR_TIMES_MAX = 6;

    // zkf61715 播放网络视频，遇到网络断连或者超时超过45时，退出播放器
    private static final long PLAYER_TIMEOUT_MAX = 30 * 1000;

    // zkf61715 超时开始时间
    private long timeOutBegin = 0;

    // zkf61715 超时时间
    private long timeOutTime = 0;

    // 当前播放器无法播放的次数
    private int playerErrorTimes = 0;

    // Menu item ID
    public static final int MENU_ID_PLAYMODE = Menu.FIRST + 1;

    public static final int MENU_ID_SUBTITLE = Menu.FIRST + 2;

    public static final int MENU_ID_SOUND = Menu.FIRST + 3;

    /*
     * l00174030；添加视屏的屏幕比例切换（自动切换、全屏拉伸、等比拉伸
     */
    public static final int MENU_ID_SCREEN = Menu.FIRST + 4;

    private static final int PLAY = 0;

    private static final int PAUSE = 1;

    private TranslateAnimation mSlideIn = null;

    private TranslateAnimation mSlideOut = null;

    private SurfaceView mSubSurface;

    private SurfaceHolder mSubHolder;

    private IMediaPlayerAdapter mMediaPlayer;

    private RelativeLayout myvideo;

    private SubObject sub;

    private static final int MSG_HIDE_HINT = 901; // hide the hint container

    // the num. of subtitle
    private int subNum = 0;

    // the id of the current suntitle
    private int subId = 0;

    // 保存外挂字幕路径
    private String subtitlePath = "";

    private int soundNum = 0;

    // the id of the current sound
    private int soundId = 0;

    // 判断是第一个播放的
    private boolean isFirstPlayVideo = true;

    /** liyang DTS2013051702993 **/
    // 本地播放视频文件，设置视频循环播放，影片播放完，在将要播放下一影片时，切换全屏播放模式或音轨或字幕时，停止运行。
    private boolean isMenuNeedShow = false;

    // 菜单是否已被创建
    private boolean isMenuHasCreated = false;

    /** 修改者：l00174030；修改原因：时间控件和控制条一起显示 **/
    private TimeLayout tiemLayout = null;

    // playlist列表显示的控件
    private PlayListShowLayoutBase mPlayListLayout;

    // 若片源是杜比音效，则必须弹出杜比标识
    private DoblyPopWin doblyPopWin = null;

    // zkf61715 是否支持快进快退
    private boolean canAccelerate = false;

    // 添加杜比的弹出消息
    protected static final int MSG_DOBLY_SHOW = 2222;

    protected static final int MSG_DOBLY_HIDE = 2223;

    // 底部弹出菜单
    private BottomPopMenu mBottomPopMenu;

    private TimeSeekDialog mTimeSeekDialog;

    private SubtitleSelectPopup mSubtitleSelectPopup = null;

    private List<LocalMediaInfo> mGetAllFlatFolders;

    private HiBDInfo mHiBDInfo;

    // zkf61715
    private boolean timeSeekToPlay = false;

    // Add by c00229449 for player state control
    private boolean isSharingStop = false;

    private boolean isFromNetwork = false;

    // zkf61715 低级别的线程用来循环检测seek操作，当网络视频断网时进行seek操作超时45秒退出
    private Timer timer;

    // 保存进行seek操作时的时间，用来判断超时
    private long timeWhenSeek = 0;

    private Object mCurrSelectType;
    //begin add by caochao for DTS2014111006777 媒体中心视频时概率性出现“该视频无法播放”
    private int mBufferUpdatePercent = 0;
    //end add by caochao for DTS2014111006777 媒体中心视频时概率性出现“该视频无法播放”
    private LocalDevice mCurrentDevice;
    /**
     * 错误提示对话框
     */
    private AlertDialog mErrorTipDialog;
    
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "VideoPlayerActivity --> onCreate()--");
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate->screenWidth:" + SCREEN_WIDTH);
        Log.i(TAG, "onCreate->screenHeight:" + SCREEN_HEIGHT);
        
        Resources resources = getResources();
        int navigationBarHeight = 0;
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            navigationBarHeight = resources.getDimensionPixelSize(resourceId);
            Log.i(TAG, "onCreate->navigationBarHeight:" + navigationBarHeight);
        }
        SCREEN_HEIGHT = DeviceInfoUtils.getScreenHeight(CommonValues.application) + navigationBarHeight;
        mCurrentDevice = (LocalDevice)getIntent().getSerializableExtra(ConstData.IntentKey.EXTRAL_LOCAL_DEVICE);
        initVideoPlayPreferences();
        initViews();
        //initEvent();
        // 初始化杜比的弹出视窗
        doblyPopWin = new DoblyPopWin(this);

        timer = new Timer(true);

        /* BEGIN: Added by r00178559 for AR-0000698413 2014/02/13 */
        loadChannelModeResources();
        /* END: Added by r00178559 for AR-0000698413 2014/02/13 */

        if (isSenderMyMedia())
        {
            setPlayMode(mPreferences.getInt(CYCLE_PLAY_MODE, DEFAULT_CYCLE_PLAY_MODE_INDEX));
//            if (0 == PlayerStateRecorder.getInstance().get(PlayerStateRecorder.VIDEO_PLAY_MODE))
//            {
//                PlayerStateRecorder.getInstance().put(PlayerStateRecorder.VIDEO_PLAY_MODE, Constant.MediaPlayMode.MP_MODE_SINGLE);
//            }
//
//            setPlayMode(PlayerStateRecorder.getInstance().get(PlayerStateRecorder.VIDEO_PLAY_MODE));
        }
        else
        {
            // 不是myMedia过来的请求,默认全体循环
            setPlayMode(Constant.MediaPlayMode.MP_MODE_ALL_CYC);
        }

        if (mUIHandler == null)
        {
            mUIHandler = new UIHandler();
        }

        if (mSeekHandlerThread == null)
        {
            mSeekHandlerThread = new HandlerThread(SEEK_THREAD_TAG);
            mSeekHandlerThread.start();
        }

        if (mSeekHandler == null)
        {
            mSeekHandler = new SeekHandler(mSeekHandlerThread.getLooper());
        }

        bIsPausedByUser = false;

        initExtSubTitleInBackground();
    }

    private void initViews()
    {
        Log.d(TAG, "onCreate ---> initViews--");
        myvideo = (RelativeLayout) findViewById(R.id.video_layout);
        // creat VideoView
        if (mVV == null || mVVAdapter == null)
        {
             canAccelerate = PlatformConfig.isSupportHisiMediaplayer();
            if (canAccelerate)
            {
                mVV = (HisiVideoView) findViewById(R.id.vv);
            }
            else
            {
                mVV = (OrigVideoView) findViewById(R.id.vv);
            }

            mVVAdapter = (IVideoViewAdapter) mVV;
            mVVAdapter.setOnErrorListener(onErrorListener);
            mVVAdapter.setOnPreparedListener(onPreparedListener);
            mVVAdapter.setOnInfoListener(onInfoListener);
            mVVAdapter.setOnSeekCompleteListener(onSeekCompleteListener);

            mVVAdapter.setOnBackForwardCompleteListener(onFastBackwordCompleteListener);
            mVVAdapter.setOnFastForwardCompleteListener(onFastForwardCompleteListener);
            mVVAdapter.setOnBufferingUpdateListener(onBufferingUpdateListener);
        }

        // subtitle
        if (mSubSurface == null)
        {
            mSubSurface = (SurfaceView) findViewById(R.id.subtitle);
        }
        if (mSubHolder == null)
        {
            mSubHolder = mSubSurface.getHolder();
        }
        //mSubHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
        //mSubHolder.setFormat(PixelFormat.RGBA_8888);
       // mSubHolder.setFixedSize(SCREEN_WIDTH, SCREEN_HEIGHT);

        mVVAdapter.setSubSurfaceHolder(mSubHolder);

        if (mSbpw == null)
        {
            mSbpw = (SeekBarLayout) findViewById(R.id.seekbarlayout);
        }

        mPlayListLayout = new PlayListShowLayout(this, mPlayStateInfo);
        mPlayListLayout.setOnItemClickListener(mPlaylistItemclickListener);

        mSbpw.startMouseStateChangedReceiver(this);
        mSbpw.setListener(onSeekBarPopWindowListener);
        mSbpw.setVisibility(View.GONE);
        mSbpw.setVideoView(mVVAdapter);
        mSbpw.canAccelerate(canAccelerate);

        VideoInfo mbi = new VideoInfo();

        mbi = getCurrentMediaInfo();

        mSbpw.setMmbi(mbi);

        tiemLayout = (TimeLayout) findViewById(R.id.timeLayout);
        // 初始时不显示
        if (tiemLayout != null)
        {
            tiemLayout.setVisibility(View.INVISIBLE);
        }

        mSlideOut = new TranslateAnimation(-0, 0, 0, -75);
        mSlideOut.setDuration(500);

        mSlideIn = new TranslateAnimation(-0, 0, -75, 0);
        mSlideIn.setDuration(500);
        mToast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);
    }

    
    private void initEvent(){
    	MediaPlayer originMediaPlayer = mMediaPlayer.getOriginMediaPlayer();
    	originMediaPlayer.setOnTimedTextListener(new  MediaPlayer.OnTimedTextListener() {
			
			@Override
			public void onTimedText(MediaPlayer mp, TimedText text) {
				if(mSubHolder != null){
					Canvas canvas = null;
					try {
					    canvas = mSubHolder.lockCanvas();
					    synchronized (mSubHolder) {
					        //canvas.drawColor(Color.WHITE);
					        //canvas.drawBitmap(enemy1, enemy1X, enemy1Y, null);
					        Paint paint = new Paint();
					        paint.setColor(Color.WHITE);
					        float textWidth = paint.measureText(text.getText());
					        float left = (SCREEN_WIDTH - textWidth) / 2;
					        if(left < 0){
					        	left = 0;
					        }
					        canvas.drawText(text.getText(), left, SCREEN_HEIGHT - SizeUtils.dp2px(VideoPlayerActivity.this, 40), paint);
					        //canvas.drawText(text.getText(), Scr, 100, paint);
					    }
					} catch (Exception e) {
					    Log.e(TAG, "run() lockCanvas()" + e);
					} finally {
					    if (canvas != null) {
					        mSubHolder.unlockCanvasAndPost(canvas);
					    }
					}
				}
			}
		});
    }
    
    @Override
    protected void onRestart()
    {
        Log.d(TAG, "VideoPlayerActivity --> onRestart()--");

        super.onRestart();
    }

    @Override
    protected void onStart()
    {
        Log.d(TAG, "VideoPlayerActivity --> onStart()--");

        String url = getCurMediaUrl();
        sub = HistoryListRecord.getInstance().getSubInfo(url);

        if (sub != null)
        {
            //subId = sub.getSubId();
//            soundId = sub.getSoundId();
            subNum = sub.getSubNum();
            soundNum = sub.getSoundNum();
            Log.d("subinfolog", "sub info 11=" + sub.getSubId() + ":" + sub.getSoundId());
        }
        else
        {
            sub = HistoryListRecord.getInstance().getSubObject();
        }

        super.onStart();
    }

    @Override
    protected void onResume()
    {
        Log.d(TAG, "VideoPlayerActivity --> onResume()--");

        mSubSurface.setVisibility(View.VISIBLE);

        if (mVVAdapter != null)
        {
            mVVAdapter.isSeeking(false);
            // 重新设置
            //mSubHolder.setFormat(PixelFormat.RGBA_8888);
            //mSubHolder.setFixedSize(SCREEN_WIDTH, SCREEN_HEIGHT);
            //mVVAdapter.setSubSurfaceHolder(mSubHolder);
        }
        /** Mender:l00174030;Reason:DTS2013041400627播放视频的过程中长按菜单键，调出近期任务，再次点击媒体中心，视频黑屏停止播放 **/
        /** 把设置URL从oncreate地方下移到此处 **/
        // 设置视频源以及seek位置
        setMediaData();

        isFromNetwork = isNetWorkVideo(getCurMediaUrl());
        mSbpw.getmSeekBar().setFromNetwork(isFromNetwork);

        // zkf61715 网络视频setSpeed会有问题，快进快退时改用连续seek
        if (canAccelerate && isFromNetwork)
        {
            canAccelerate = false;
            mSbpw.canAccelerate(canAccelerate);
        }

        // 恢复进度同步
        syncSeekPos();

        delayHidePop(1);

        super.onResume();

        mUIHandler.sendEmptyMessage(MSG_HIDE_ACCELERATION);

        // 视频电话返回后，如果之前播放器处于暂停状态，恢复为暂停状态
        if (bIsPausedByUser)
        {
            // DTS2012072604665:播放搜索的视频，暂停时推送媒体文件到机顶盒播放，返回后，视频仍处于暂停状态，但黑屏
            if (!bMCSMode)
            {
                Log.d(TAG, "Resume to play status ---------changge pause to play state !");

                play();

            }
            else
            {
                Log.d(TAG, "Resume to pause status  11");
                pause();
                showPop();
            }
        }
        else
        {
            Log.d(TAG, "Resume to play status  22");
            play();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        Log.d(TAG, "VideoPlayerActivity --> onSaveInstanceState()--");
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause()
    {
        Log.d(TAG, "VideoPlayerActivity --> onPause() --1 --");
        if (doblyPopWin != null)
        {
            doblyPopWin.hideDoblyWin();
        }
        if (mHiBDInfo != null)
        {
            mHiBDInfo.closeBluray();
        }
        mSubSurface.setVisibility(View.INVISIBLE);

        // 停止同步seek位置
        stopSyncSeekPos();

        cancelHidPopMessage();
        mPlayListLayout.hidePopupWindow();

        hidePop();

        /** Mender:l00174030;Reaseon:When push another player, there must be another stop command **/
        if (mMediaPlayer == null)
        {
            Log.e(TAG, "there must be another stop command");
            // 此时也需要释放资源
            if (bMCSMode)
                stop();
            super.onPause();
            return;
        }

        try
        {
            // pause();
            pauseNow();

            Log.e(TAG, "onpause is bMCSMode 11");
            // 仅仅在Pause的时候保存，原因是怕状态在onStop的时候状态乱了，无法获取duratson
            saveSeekPosIntoMbi();

            Log.e(TAG, "onpause is bMCSMode 22");
            /** Mender:l00174030;Reason:push and come back, sometimes can't malloc buffer for the vdec **/
            HistoryListRecord.getInstance().putSubInfo(getCurMediaUrl(), sub);
            stop();

        }
        catch (Exception e)
        {
            Log.e(TAG, "There is something wrong with the Mediaplayer, maybe more than one stop.");
        }
        super.onPause();
    }

//    @Override
//    protected void onStop()
//    {
//        Log.d(TAG, "VideoPlayerActivity --> onStop()--");
//        super.onStop();
//    }

    @Override
    protected void onDestroy()
    {
        Log.d(TAG, "VideoPlayerActivity --> onDestroy()--");
        passIntentForVideoBrowser();

        timer.cancel();
        if (mUIHandler != null)
        {
            mUIHandler.removeAllMsgs();
        }

        if (mSeekHandler != null)
        {
            mSeekHandler.removeMessages(0);
        }

        mSbpw.stopMouseStateChangedReceiver(this);

        mSbpw.getmSeekBar().mHandler = null;

        mVVAdapter.isSeeking(false);

        if (mPlayListLayout != null)
        {
            mPlayListLayout.hidePopupWindow();
        }
        mUIHandler = null;

        mSeekHandlerThread.getLooper().quit();
        mSeekHandlerThread = null;
        mSeekHandler = null;

        VideoInfo mbi = new VideoInfo();
        mbi = getCurrentMediaInfo();

        if (mbi != null)
        {
            String strUrl = mbi.getUrl();
            if (PlatformConfig.isSupportHisiMediaplayer() && Constant.DeviceType.isLocalDevice(mbi.getmDeviceType()) && strUrl.trim().toLowerCase().endsWith(".iso"))
            {
                BDInfo bDInfo = new BDInfo();
                if (bDInfo.isBDFile(strUrl))
                {
                    IMountService mountService = getMountService();
                    if (null != mountService && null != mBdMntPath)
                    {
                        Mount.unmountISO(mountService, mBdMntPath);
                    }
                }
            }
        }
        if (null != mMediaPlayer)
        {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        super.onDestroy();
    }

    /**
     * 处理按键堆积
     */

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
    	
        Log.d(TAG, "onKeyDown, keyCode:" + keyCode + ", event:" + event);

        // 对音量键不进行按键累积，避免调节音量出现卡顿
        switch (keyCode)
        {
            case KeyEvent.KEYCODE_MENU:
                // menuOpened();
                int x = mSbpw.getmSeekBar().getXacceleration();
                if (x != 0 && x != 1)
                {
                    play();
                    mSbpw.Xacceleration = 0;
                    mSbpw.getmSeekBar().setXacceleration(0);
                    mSbpw.getmSeekBar().setOnkey(false);
                    try
                    {
                        // 休眠是为了保证play()发送的消息已经被执行，在播放状态下控制条才能被掩藏
                        Thread.sleep(100);
                    }
                    catch (InterruptedException e)
                    {
                        Log.e(TAG, "onKeyDown, open menu ::: error");
                    }
                }
                openBottomMenu();
                return true;

            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                Log.d(TAG, "KeyEvent.KEYCODE_VOLUME_UP || KeyEvent.KEYCODE_VOLUME_DOWN");
                if (audioManager == null)
                {
                    Log.d(TAG, "audioManager == null, create a AudioManager object");
                    audioManager = (AudioManager) this.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                }

                int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                float volumePercent = Float.valueOf(currentVolume) / Float.valueOf(maxVolume);
                Log.d(TAG, "currentVolume:" + currentVolume + " maxVolume: " + maxVolume + " volumePercent:" + volumePercent);
                // Log.d(TAG, "maxVolume:" + maxVolume);
                // Log.d(TAG, "volumePercent:" + volumePercent);

                if (mMediaCenterPlayerClient != null)
                {
                    Log.d(TAG, "Send the volume percent to Sender client");
                    mMediaCenterPlayerClient.adjustVolume(Constant.VolumeAdjustType.ADJUST_SET, volumePercent);
                }

                return super.onKeyDown(keyCode, event);

            default:
                break;
        }

        switch (keyCode)
        {
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_MEDIA_REWIND:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                mUIHandler.removeMessages(MSG_PROGRESS_CHANGED);

                showPop();
                break;

            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                Log.d(TAG, "onKeyDown - KEYCODE_DPAD_CENTER or KEYCODE_MEDIA_PLAY_PAUSE --");
                showPop();
                if (mSbpw.getmSeekBar().isOnkey())
                {
                    mSbpw.getmSeekBar().onKeyDown(keyCode, event);
                    break;
                }
                if (mUIHandler != null)
                {
                    mUIHandler.sendEmptyMessage(MSG_UI_VIDEOVIEW_REVERSE_STATE);
                }

                mSbpw.iskeyDown = false;

                break;

            case KeyEvent.KEYCODE_DPAD_UP:
                Log.d(TAG, "onKeyDown - KEYCODE_DPAD_UP --");
                // preProgram();
                if (bMCSMode)
                {
                    preProgram();
                }
                else
                {
                    //showPlayListLayout();
                }

                break;

            case KeyEvent.KEYCODE_DPAD_DOWN:
                Log.d(TAG, "onKeyDown - KEYCODE_DPAD_DOWN --");
                if (bMCSMode)
                {
                    nextProgram();
                }
                else
                {
                	//去除右边侧边栏			
                    //showPlayListLayout();
                }
                //

                break;

            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_MEDIA_STOP:
                Log.d("vvvv", "onKeyDown - KEYCODE_BACK --" + this);

                // 播放器关闭，Activity销毁时，先回发stop状态，再立即解除绑定，其后不再向Sender端回发播放器的任何状态
                if (bMCSMode)
                {
                    Log.d(TAG, "KEYCODE_BACK -- bMCSMode");
                    onNotifyStop();
                    unbind();

                    /** 推送的场合，直接弄死自己，防止状态异常 **/
                    // 如果是推送场景，直接结束自己。
                    Log.e(TAG, "onpause activity kill itself.");
                    // finish();
                    // android.os.Process.killProcess(android.os.Process.myPid());
                }

                /** Mender:l00174030;Reason:push and come back, sometimes can't malloc buffer for the vdec **/
                // stop();

                if (mSbpw.isShown())
                {
                    hidePop();
                }

                // 在关闭当前activity前，得先把显示字幕的surfaceview隐藏，解决4.2系统 退出时有个黑块的bug
                mSubSurface.setVisibility(View.INVISIBLE);
                // Log.e(TAG, "finish");
                finish();
                break;
            default:
                break;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event)
    {
    	Log.i(TAG, "onKeyUp->keyCode:" + keyCode);
        boolean retkeyup = mSbpw.onKeyUp(keyCode, event);
        mUIHandler.removeMessages(MSG_PROGRESS_CHANGED);
        mUIHandler.sendEmptyMessage(MSG_PROGRESS_CHANGED);
        return retkeyup;
    }

    private void preProgram()
    {
        Log.d(TAG, "preProgram() --IN--");

        if (!isMyMediaType())
        {
            // 如果不是点击播放的给出提示就返回
            setToast(getString(R.string.video_cannot_pre));
            return;
        }

        // 测试要求切换到下一首的时候，隐藏进度条
        delayHidePop(1);
        if (mUIHandler != null)
        {
            mUIHandler.sendEmptyMessage(MSG_HIDE_ACCELERATION);
        }

        VideoInfo mbi = new VideoInfo();
        mbi = getPreMediaInfo();
        // 播放列表中是否存在上一个视频
        if (mbi != null)
        {
            // 先停止进度同步
            stopAllSyncSeek();
            // 存在，则直接播放
            if (mUIHandler != null)
            {
                mUIHandler.sendEmptyMessage(MSG_UI_VIDEOVIEW_STOP);
                // 视频上下切换，显示缓冲图标
                mUIHandler.sendEmptyMessage(MSG_SHOW_PROGRESS);
            }

            mSbpw.setMmbi(mbi);
            setDuration(0);

            String strUrl = mbi.getUrl();
            Log.d(TAG, "preProgram :" + strUrl);
            if (mUIHandler != null && StringUtils.isNotEmpty(strUrl))
            {
                Message msgSetVideo = Message.obtain();
                msgSetVideo.arg1 = HistoryListRecord.getInstance().get(strUrl);
                msgSetVideo.what = MSG_UI_VIDEOVIEW_SETDATA;
                msgSetVideo.obj = strUrl;
                mUIHandler.sendMessage(msgSetVideo);
            }
            play();
        }
        else
        {
            if (isSenderMyMedia())
            {
                setToast(getString(R.string.video_cannot_pre));
            }
        }
    }

    private void nextProgram()
    {
        Log.d(TAG, "nextProgram() --IN--");
        delayHidePop(1);
        if (mUIHandler != null)
        {
            mUIHandler.sendEmptyMessage(MSG_HIDE_ACCELERATION);
        }

        if (!isMyMediaType())
        {
            setToast(getString(R.string.video_cannot_next));
            return;
        }

        VideoInfo mbi = new VideoInfo();

        mbi = getNextMediaInfo();

        if (mbi != null)
        {
            stopAllSyncSeek();
            Log.d(TAG, "nextProgram :" + mbi.getUrl());
            if (mUIHandler != null)
            {
                mUIHandler.sendEmptyMessage(MSG_UI_VIDEOVIEW_STOP);
                mUIHandler.sendEmptyMessage(MSG_SHOW_PROGRESS);
            }
            mSbpw.setMmbi(mbi);
            setDuration(0);

            String strUrl = mbi.getUrl();
            if (mUIHandler != null && StringUtils.isNotEmpty(strUrl))
            {
                Message msgSetVideo = Message.obtain();
                msgSetVideo.arg1 = HistoryListRecord.getInstance().get(strUrl);
                msgSetVideo.what = MSG_UI_VIDEOVIEW_SETDATA;
                msgSetVideo.obj = strUrl;
                mUIHandler.sendMessage(msgSetVideo);
            }

            play();
        }
        else
        {
            if (isSenderMyMedia())
            {
                setToast(getString(R.string.video_cannot_next));
            }
        }
    }

    // 设置提示信息
    private void setToast(String string)
    {
        Log.d(TAG, "setToast(): " + string);
        mToast.setText(string);
        mToast.show();
    }

    /**
     * 显示无法播放提示框
     * @param tip
     * @param isNextPlay
     */
    private void showCannotPlayDialog(String tip, final boolean isNextPlay){
    	Log.i(TAG, "showCannotPlayDialog->tip:" + tip);
    	//if(mErrorTipDialog != null && mErrorTipDialog.isShowing())
    	//	return;
    	mErrorTipDialog = new AlertDialog.Builder(this).setMessage(tip).setCancelable(false).create();
    	int displayTime = isNextPlay ? 1000 : 2000;
    	new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				if(isNextPlay){
					 mErrorTipDialog.dismiss();
					 mPlayListLayout.setCurrentPlayIndex(mPlayStateInfo.getCurrentIndex());
		             setMediaData();
		             play();
				}else{
					mErrorTipDialog.dismiss();
					finishPlay();
				}
			}
		}, displayTime);
    	mErrorTipDialog.show();
    }
    
    // 设置播放器缓冲图标
    private void setProgressBar(int arg)
    {
        Log.d(TAG, "setProgressBar():{VISIBLE = 0;INVISIBLE = 4;GONE = 8}" + arg);

//        if (mUIHandler != null)
//        {
//            Message msg = Message.obtain();
//            msg.what = MSG_UI_PROCESSBAR;
//            msg.arg1 = arg;
//            mUIHandler.sendMessage(msg);
//        }
        if (mCircleProgressBar != null)
        {
            /***********Add by w00190739 2014-11-07 DTS2014110603386 只为瑞芯微修改，其他的不要合入；
             * 原因是瑞芯微平台必现：连续播放时，到下一个视频时，屏幕中间会展示一个小圆圈，*******************/
            mCircleProgressBar.setVisibility(arg);
        }

    }

    // add by w00184463
    // 进度条初始为0
    private void setDuration(int duration)
    {
        if (mSbpw != null)
        {
            Log.d(TAG, "setDuration(): " + duration);
            mSbpw.setDuration(duration);
        }

    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event)
    {
        return super.onKeyLongPress(keyCode, event);
    }

    private SeekBarListener onSeekBarPopWindowListener = new SeekBarListener()
    {
        public void seekto(int seekTo)
        {
            Log.d(TAG, "seekbar is syn to MVV");
            cancelHidPopMessage();

            // modified by keke 2013.4.9 修改在seek的时候进度条回退
            if (mSbpw.getVisibility() != View.VISIBLE)
            {
                if (mUIHandler == null)
                {
                    return;
                }
                else
                {
                    mUIHandler.sendEmptyMessage(MSG_SHOW_CONTROLER);
                }
            }

            if (seekTo >= 0 && seekTo <= mDuration)
            {
                firstSeek = false;

                Log.d(TAG, "onSeekBarPopWindowListener --> seekTo:" + seekTo);

                VideoPlayerActivity.this.seekTo(seekTo);

                timeWhenSeek = 0;
                if (mSbpw.getmSeekBar().isFromNetwork())
                {
                    TimerTask task = new TimerTask()
                    {
                        @Override
                        public void run()
                        {
                            // TODO Auto-generated method stub
                            if (mVVAdapter.isSeeking())
                            {
                                timeWhenSeek += 1000;
                                if (timeWhenSeek >= PLAYER_TIMEOUT_MAX)
                                {
                                    this.cancel();
                                    finish();
                                }
                                return;
                            }
                            this.cancel();
                        }
                    };
                    timer.schedule(task, 1000, 1000);
                }
            }

            if (!bIsPausedByUser)
            {
                delayHidePop(DELAY_TIME);
            }

            Log.d(TAG, "onSeekBarPopWindowListenersseekTo:  " + seekTo);
        }

        public void reverseState()
        {
            Log.d(TAG, "reverseState");
            if (mUIHandler != null)
            {
                mUIHandler.sendEmptyMessage(MSG_UI_VIDEOVIEW_REVERSE_STATE);
            }

        }

        public void hide(boolean bShowM)
        {
            Log.d(TAG, "hide - :bShowM : " + bShowM);
            if (bShowM)
            {
                // 显示Menu
                immediatelyHide();
            }
            else
            {
                cancelHidPopMessage();
                delayHidePop(DELAY_TIME);
            }
        }

        public void immediatelyHide()
        {
            mUIHandler.setbAlwaysShowPopSeekbar(false);
            cancelHidPopMessage();
            delayHidePop(1);
            mUIHandler.sendEmptyMessage(MSG_HIDE_ACCELERATION);
        }

        public void nextProgram()
        {
            // TODO　keke
            if (bMCSMode)
            {
                VideoPlayerActivity.this.nextProgram();
            }
            else
            {
                //showPlayListLayout();
            }
        }

        public void preProgram()
        {
            if (bMCSMode)
            {
                VideoPlayerActivity.this.preProgram();
            }
            else
            {
                //showPlayListLayout();
            }
        }

        public int getPlayState()
        {
            if (mVVAdapter.isPlaying())
            {
                return PLAY;
            }
            else
            {
                return PAUSE;
            }
        }

        public void showMenu()
        {
            openOptionsMenu();
        }

        public void onXChange(int X)
        {
            mUIHandler.removeMessages(MSG_HIDE_ACCELERATION);

            mUIHandler.setbAlwaysShowPopSeekbar(false);

            if (!isFromNetwork)
            {
                if (canAccelerate)
                {
                    // zkf61715 DTS2014010909031 设置倍速时先判断状态
                    if (mVVAdapter.isPlaying() && mVVAdapter.setSpeed(X) == 0)
                    {
                        mSbpw.getmSeekBar().setXacceleration(X);
                    }
                }
                else
                {
                    mSbpw.getmSeekBar().setXacceleration(X);
                }
                X = mSbpw.getmSeekBar().getXacceleration();
            }
            Log.d(TAG, "~~~~~~~~~~~~~~~~~~~X =" + X);

            if (X == 0 || X == 1)
            {
                Log.d(TAG, "firstSeek==" + firstSeek);
                mUIHandler.sendEmptyMessage(MSG_MCS_PLAY);
                // 协议上获取状态是有时间间隔的，如果发的太快，会被覆盖
                if (isNetWorkVideo(getCurMediaUrl()))
                {
                    mUIHandler.sendEmptyMessageDelayed(MSG_UI_VIDEOVIEW_PLAY, 1000);
                }
                else
                {
                    mUIHandler.sendEmptyMessage(MSG_UI_VIDEOVIEW_PLAY);
                }

                if (isFromNetwork)
                {
                    mUIHandler.sendEmptyMessageDelayed(MSG_HIDE_ACCELERATION, 3000);
                }
                else
                {
                    mSbpw.getmSeekBar().setOnkey(false);
                    cancelHidPopMessage();
                    delayHidePop(DELAY_TIME);
                }
            }
            else
            {
                /* BEGIN: Modified by s00211113 for DTS2014031902280 2014/03/19 */
                int imgId = 0;
                switch (X)
                {
//                    case 2:
//                        imgId = R.drawable.play_status_acc2x;
//                        break;
//                    case 4:
//                        imgId = R.drawable.play_status_acc4x;
//                        break;
//                    case 8:
//                        imgId = R.drawable.play_status_acc8x;
//                        break;
//                    case 16:
//                        imgId = R.drawable.play_status_acc16x;
//                        break;
//                    case 32:
//                        imgId = R.drawable.play_status_acc32x;
//                        break;
//                    case -2:
//                        imgId = R.drawable.play_status_backacc2x;
//                        break;
//                    case -4:
//                        imgId = R.drawable.play_status_backacc4x;
//                        break;
//                    case -8:
//                        imgId = R.drawable.play_status_backacc8x;
//                        break;
//                    case -16:
//                        imgId = R.drawable.play_status_backacc16x;
//                        break;
//                    case -32:
//                        imgId = R.drawable.play_status_backacc32x;
                    case 2:
                        imgId = R.drawable.play_status_acc;                        
                        break;
                    case -2:
                        imgId = R.drawable.play_status_backacc;
                        break;
                    default:
                        break;
                }
                mSbpw.ShowPlayStatusImg(imgId);
            }

        }

        public void onTrackingTouchChange(boolean isTrackingTouch)
        {
            Log.e(TAG, "chumoping");
            if (isTrackingTouch)
            {
                if (mUIHandler != null)
                {
                    mUIHandler.removeMessages(MSG_HIDE_CONTROLER);
                    mUIHandler.sendEmptyMessage(MSG_SHOW_CONTROLER);
                }
            }
            else
            {
                if (mUIHandler != null)
                {
                    mUIHandler.removeMessages(MSG_SHOW_CONTROLER);
                    mUIHandler.sendEmptyMessageDelayed(MSG_HIDE_CONTROLER, 3000);
                }
            }
        }

        public float onNan()
        {
            int position = mMediaPlayer.getCurrentPosition();
            int maxposition = mMediaPlayer.getDuration();

            return Float.valueOf(position) / Float.valueOf(maxposition);
        }

        public void onBack(int keyCode, KeyEvent event)
        {
            onKeyDown(keyCode, event);
        }

    };

    private void showPop()
    {
        Log.d(TAG, "showPop() --IN--");

        cancelHidPopMessage();
        if (null != mUIHandler)
        {
            mUIHandler.sendEmptyMessage(MSG_SHOW_CONTROLER);
        }
        delayHidePop(DELAY_TIME);
    }

    private void hidePop()
    {
        Log.d(TAG, "hidePop() --IN--");
        try
        {
            mSbpw.setVisibility(View.GONE);
            mSbpw.setFocusable(false);

            /** 修改者：l00174030；修改原因：时间控件和控制条一起显示 **/
            // 进度条消失时时间不显示
            if (tiemLayout != null)
            {
                tiemLayout.setVisibility(View.INVISIBLE);
            }
        }
        catch (Exception e)
        {
        }
    }

    private void cancelHidPopMessage()
    {
        Log.d(TAG, "cancelHidPopMessage()--");
        if (null != mUIHandler)
        {
            mUIHandler.removeMessages(MSG_HIDE_CONTROLER);
        }
    }

    private void delayHidePop(int time)
    {
        Log.d(TAG, "delayHidePop()--");
        if (null != mUIHandler)
        {
            mUIHandler.sendEmptyMessageDelayed(MSG_HIDE_CONTROLER, time);
        }
    }

    private void progressGone()
    {
        if (mCircleProgressBar.getVisibility() == View.VISIBLE)
        {
            mUIHandler.sendEmptyMessage(MSG_HIDE_PROGRESS);
        }        
    }

    private OnCompleteListener onCompletionListener = new OnCompleteListener()
    {
        public void onCompletion(IMediaPlayerAdapter mp)
        {
            Log.d(TAG, "OnCompletionListener -- onCompletion() --");
            mVVAdapter.setOnCompletionListener(null);
            onCompleteOperate(mp);
        }

    };

    private OnErrorListener onErrorListener = new OnErrorListener()
    {
        public boolean onError(IMediaPlayerAdapter mp, int what, int extra)
        {
        	//错误提示对话框已经存在，不再上报错误
        	if(mErrorTipDialog != null && mErrorTipDialog.isShowing())
        		return true;
            Log.d(TAG, "onErrorListener -- onError() Error Code:" + what + "  extra:" + extra);
            int messageId = R.string.VideoView_error_title;
            if (what == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
                messageId = R.string.invalid_url;
            }else {
                //LogUtil.LogPlayer(TAG,"Fatal Error: impl_err =" + impl_err);
                switch (extra) {
                    case ConstData.VIDEO_PLAY_ERROR_CODE.ERROR_NETWORK:
                        messageId = R.string.VideoView_error_networkfail;
                        break;
                    case ConstData.VIDEO_PLAY_ERROR_CODE.ERROR_ID_COPYRIGHT_NO_RMVB_DIVX:
                        messageId = R.string.MediaError_CopyRight;
                        break;
                    case ConstData.VIDEO_PLAY_ERROR_CODE.ERROR_ID_COPYRIGHT_DIFF_CHIP:
                        messageId = R.string.MediaError_ChipDiff;
                        break;
                    case ConstData.VIDEO_PLAY_ERROR_CODE.ERROR_ID_TOO_HIGH_BITRATE:
                        messageId = R.string.MediaError_HightBitrate;
                        break;
                    case ConstData.VIDEO_PLAY_ERROR_CODE.ERROR_ID_NO_CODEC:
                        messageId = R.string.MediaError_NoCodec;
                        break;
                    case ConstData.VIDEO_PLAY_ERROR_CODE.ERROR_ID_VPU_MPEG4_ROSOLUTION:
                        messageId = R.string.MediaError_MPEG4;
                        break;
                    case ConstData.VIDEO_PLAY_ERROR_CODE.ERROR_ID_10BIT_NOT_SUPPORT:
                        messageId = R.string.MediaError_10BIT;
                        break;
                    default:
                    	messageId = R.string.VideoView_error_text_unknown;
                        break;
                }
            }
            
            // 播放失败时，菜单需要重新加载
            isMenuHasCreated = false;
            isMenuNeedShow = false;

            stopSyncSeekPos();
            progressGone();
            mPlayListLayout.hidePopupWindow();

            // 隐藏杜比弹出框
            sendDoblyWinMsg(false);

            // modified by keke 2013.4.9 主要修改在u盘播放的时候拔掉U盘退出播放器
            // 暂时没有发现会影响到循环播放
            if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED)
            {
                playerErrorTimes = 0;
                finishPlay();
                gotoHome();
                return true;
            }

            String url = getCurMediaUrl();
            if (url != null)
            {
                if (isMyMediaType())
                {
                    HistoryListRecord.getInstance().put(url, 0);
                }
            }

            playerErrorTimes++;

            // 若是播放为single模式or连续6次无法播放，退出播放器
            if (getPlayMode() == Constant.MediaPlayMode.MP_MODE_SINGLE || playerErrorTimes >= PLAYER_ERROR_TIMES_MAX)
            {
                playerErrorTimes = 0;
                // 不能播放的提示
                //setToast(getString(R.string.video_error));
                showCannotPlayDialog(getString(messageId), false);
                return true;
            }

            // 播放模式为全体循环播放
            Log.d(TAG, "mCurrentIndex:" + mPlayStateInfo.getCurrentIndex());

            VideoInfo mbi = getNextMediaInfo();

            if (mbi == null)
            {
                stopSyncSeekPos();
                showCannotPlayDialog(getString(messageId), false);
                //finishPlay();
            }
            else
            {
            	showCannotPlayDialog(getString(messageId), true);
            }

            return true;
        }
    };

    /**
     * error导致播放停止 提取出的相同的代码 by keke 2013.4.10 <功能详细描述>
     * @see [类、类#方法、类#成员]
     */
    private void finishPlay()
    {
        if (bMCSMode)
        {
            onNotifyStop();
            unbind();
        }
        stop();

        mSubSurface.setVisibility(View.INVISIBLE);
        finish();
    }

    /**
     * 跳转到首页 <功能详细描述>
     * @see [类、类#方法、类#成员]
     */
    protected void gotoHome()
    {
        Intent intent = new Intent();
        intent.setClass(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    /** 标记第一次seek到进度条为0 */
    private boolean firstSeek = true;

    private OnSeekCompleteListener onSeekCompleteListener = new OnSeekCompleteListener()
    {
        public void onSeekComplete(IMediaPlayerAdapter mp)
        {
            Log.d(TAG, "mp.getCurrentPosition() = " + mp.getCurrentPosition());

            if (isFromNetwork)
            {
                if (mUIHandler != null && null != mVV)
                {
                    mVVAdapter.isSeeking(false);
                    Log.d(TAG, "OnSeekCompleteListener --- " + firstSeek);
                    // 判断是否是第一次开始播放时的seek，如果不是则代表是最后播放完后的seek
                    mSbpw.mSeekBarPopWindowListener.onTrackingTouchChange(false);
                    mSbpw.Xacceleration = 0;
                    mSbpw.getmSeekBar().setXacceleration(0);
                    onSeekBarPopWindowListener.onXChange(0);
                    firstSeek = false;
                    return;
                }
            }
            float Kscale = mSbpw.getmSeekBar().getKscale();  
            if (mp.getCurrentPosition() >= mp.getDuration())
            {
                mSbpw.setAcceleToEnd(true);
            }
            else
            {
                mSbpw.setAcceleToEnd(false);
            }
            if (mUIHandler != null && null != mVV)
            {
                mVVAdapter.isSeeking(false);                
                if (Kscale == 0 || Kscale == 1)
                {
                    // 快进快退到头时正常播放
                    mSbpw.Xacceleration = 0;
                    mSbpw.getmSeekBar().setXacceleration(0);
                    mSbpw.mSeekBarPopWindowListener.onXChange(0);
                    mSbpw.setAcceleCompl(true);
                }
                                
                Log.d(TAG, "OnSeekCompleteListener --- " + firstSeek);
                // 判断是否是第一次开始播放时的seek，如果不是则代表是最后播放完后的seek

                mSbpw.mSeekBarPopWindowListener.onTrackingTouchChange(false);

                if (mSbpw.getmSeekBar().isEnSure())
                {
                    mSbpw.Xacceleration = 0;
                    mSbpw.getmSeekBar().setXacceleration(0);
                    onSeekBarPopWindowListener.onXChange(0);
                }
                firstSeek = false;
            }
            if (timeSeekToPlay)
            {
                timeSeekToPlay = false;
                chgVideoStatusWhenPopMenu();
            }  
            
            if (Kscale == 1)
            {                  
                onCompleteOperate(mp);                 
            }
        }
    };

    protected int mDuration;

    private OnPreparedListener onPreparedListener = new OnPreparedListener()
    {
        public void onPrepared(IMediaPlayerAdapter mp)
        {            
            mMediaPlayer = mp;
         /*   if (mVVAdapter.getSubtitleList() != null && mVVAdapter.getSubtitleList().size() > 0)
            {
                mVVAdapter.showSubtitle(true);
            }
            */
            
            initEvent();
            
            if (!PlatformConfig.isSupportHisiMediaplayer())
            {
                updateMenuScreen();
            }

            mDuration = mp.getDuration();
            int position = mp.getCurrentPosition();

            // z00184367 add 2011年12月15日:将当前媒体文件的播放总长度返回给客户端
            if (bMCSMode)
            {
                if (mMediaCenterPlayerClient != null)
                {
                    mMediaCenterPlayerClient.reportDuration(position, mDuration);
                }
            }

            Log.d(TAG, "onPreparedListener this is first :" + isFirstPlayVideo + " get duration = " + mDuration);
            // 处理来自界面点击/甩屏的请求，甩、推的时候带有seek，其他不会带有
            int seek = 0;
            String strUrl = null;
            VideoInfo mbi = getCurrentMediaInfo();
            if (mbi != null)
            {
                strUrl = mbi.getUrl();
            }
            if (!StringUtils.isEmpty(strUrl))
            {
                if (isFirstPlayVideo)
                {
                    isFirstPlayVideo = false;
                    if (isMyMediaType())
                    {
                        seek = HistoryListRecord.getInstance().get(strUrl);
                    }
                }
                seek = seek == 0 ? MathUtil.ConvertPercentageToValue(mbi.getmSeekTo(), mDuration) : seek;

                if (isMyMediaType())
                {
                    HistoryListRecord.getInstance().put(strUrl, seek);
                }
            }

            if (0 < seek)
            {
                seekToNow(seek);
            }
            // 同步seek位置
            syncSeekPos();
            progressGone();
            // 弹出杜比信息框
            // 之前要設置信息
            showDoblyWin();
            setDuration(mDuration);

            // 重置无法播放次数为0
            playerErrorTimes = 0;
            mSbpw.setbPrepared(true);

            // 当结束的时候把当前播放索引返回到broeser界面
            passIntentForBrowser();

            // 提前创建菜单，解决菜单显示慢问题
            createPopMenu();
            loadMenu();
            isMenuHasCreated = true;

            /** liyang DTS2013051702993 **/
            // 本地播放视频文件，设置视频循环播放，影片播放完，在将要播放下一影片时，切换全屏播放模式或音轨或字幕时，停止运行。
            isMenuNeedShow = true;

            play();
        }
    };

    /**
     * 展示杜比标志 <功能详细描述>
     * @see [类、类#方法、类#成员]
     */
    private void showDoblyWin()
    {
        if (PlatformUtil.isHisiPlatform()){
//    		List<AudioInfoOfVideo> aiovs = mVVAdapter.getAudioinfos();
//
//            if (aiovs != null)
//            {
//                for (int i = 0; i < aiovs.size(); i++)
//                {
//                    doblyPopWin.checkHasDobly(aiovs.get(i).getaudioformat());
//                }
//            }
        	AudioInfoOfVideo maudioInfoOfVideo = mVVAdapter.getCurrentAudioinfos();
        	if(maudioInfoOfVideo != null)
        	{
            	doblyPopWin.checkHasDobly(maudioInfoOfVideo.getaudioformat());
            	Log.i(TAG, "maudioInfoOfVideo" + maudioInfoOfVideo);
            	Log.i(TAG, "maudioInfoOfVideo.getaudioformat()" + maudioInfoOfVideo.getaudioformat());
        	}
    	}else{
    		boolean isDolbyEnabled = mVVAdapter.isDolbyEnabled();
    		Log.d(TAG, " showDoblyWin() isDolbyEnabled======= "+isDolbyEnabled);
    		if(isDolbyEnabled){
    			doblyPopWin.checkHasDobly(-100);
    		}
    	}
    	sendDoblyWinMsg(true);
    }

    // 播放器的断网功能，底层海思播放器挪到了OnInfoListener中上报
    private OnInfoListener onInfoListener = new OnInfoListener()
    {
        public boolean onInfo(IMediaPlayerAdapter mp, int what, int extra)
        {
            Log.i(TAG, "---onInfo--- what = " + what + " ------ extra = " + extra);
            if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START)
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {    
                        setProgressBar(View.GONE);
                    }                    
                });
                
            }
            // extra == 11
            if (what == HiMediaPlayer.MEDIA_INFO_NETWORK && extra == 11)
            {
                return onErrorListener.onError(mp, what, extra);
            }
            //begin add by caochao for DTS2014111006777 媒体中心视频时概率性出现“该视频无法播放”
            if (what == HiMediaPlayer.MEDIA_INFO_BUFFER_DOWNLOAD_FIN)
            {
            	//下载完毕，超时清零
            	timeOutBegin = 0;
            }  
            //end add by caochao for DTS2014111006777 媒体中心视频时概率性出现“该视频无法播放”

            if (what == HiMediaPlayer.MEDIA_INFO_VIDEO_FAIL)
            {
                return onErrorListener.onError(mp, what, extra);
            }

            if (what == HiMediaPlayer.MEDIA_INFO_NOT_SUPPORT)
            {
                return onErrorListener.onError(mp, what, extra);
            }

            if (what == HiMediaPlayer.MEDIA_INFO_TIMEOUT || (what == HiMediaPlayer.MEDIA_INFO_NETWORK && (extra >= 0 && extra <= 4)))
            {
                Log.i(TAG, "---onInfo--- BufferTimeStatus = " + mp.getBufferTimeStatus() + " ------  timeOutBegin = " + timeOutBegin);
                if (mp.getBufferTimeStatus() <= 1)
                {
                    if (timeOutBegin == 0)
                    {
                        timeOutBegin = System.currentTimeMillis();
                    }
                    return onErrorListener.onError(mp, what, extra);
                }
                else
                {
                    // 开始有缓存，说明网络正常
                    timeOutBegin = 0;
                }
            }

            if (what == HiMediaPlayer.MEDIA_INFO_NETWORK && extra == 5)
            {
                // 网络连接正常
                timeOutBegin = 0;
            }                       

            // bufferInfo(what);

            return false;
        }
    };

    OnFastBackwordCompleteListener onFastBackwordCompleteListener = new OnFastBackwordCompleteListener()
    {
        public void onFastBackwordComplete(IMediaPlayerAdapter mp)
        {
            Log.i(TAG, "------------------->>>>BackwordComplete");

            mSbpw.Xacceleration = 1;

            mSbpw.getmSeekBar().setXacceleration(1);

            onSeekBarPopWindowListener.onXChange(1);
        }
    };

    OnFastForwardCompleteListener onFastForwardCompleteListener = new OnFastForwardCompleteListener()
    {

        @Override
        public void onFastForwardComplete(IMediaPlayerAdapter mp)
        {
            Log.i(TAG, "-------------------->>>>Forward");
        }
    };

    OnBufferingUpdateListener onBufferingUpdateListener = new OnBufferingUpdateListener()
    {

        @Override
        public boolean onBufferingUpdate(IMediaPlayerAdapter mp, int percent)
        {
            // TODO Auto-generated method stub
        	//begin add by caochao for DTS2014111006777 媒体中心视频时概率性出现“该视频无法播放”
        	//buffer处于更新状态或者加载已经完成，超时计数清零
        	Log.i(TAG, "onBufferingUpdate percent = " + percent);
        	if (mBufferUpdatePercent != percent || percent == 100)
        	{
        		mBufferUpdatePercent = percent;
        		timeOutBegin = 0;
        	}
        	//end add by caochao for DTS2014111006777 媒体中心视频时概率性出现“该视频无法播放”
            return false;
        }
    };

    private boolean isBuffereIng = false;

    public void setPlayMode(int playMode)
    {
    	//Log.i(TAG, "setPlayMode->stackTrace:" + android.util.Log.getStackTraceString(new Throwable()));
        mPlayStateInfo.setPlayMode(playMode);
    }

    public int getPlayMode()
    {
        return mPlayStateInfo.getPlayMode();
    }

    /**
     * 获取3D模式
     * @return
     */
    public int get3DMode(){
    	return mPlayStateInfo.get3DMode();
    }
    
    /**
     * 设置3D模式
     * @param mode
     */
    public void set3DMode(int mode){
    	//使用Mediaplayer设置3D模式
    	MediaPlayer mediaPlayer = mMediaPlayer.getOriginMediaPlayer();
    	mediaPlayer.set3DMode(mode);
    	mPlayStateInfo.set3DMode(mode);
    }
    
    /** l00174030；添加视屏的屏幕比例切换（自动切换、全屏拉伸、等比拉伸） **/
    public void setScreenMode(int screenMode)
    {
        if (mPlayStateInfo != null)
        {
            mPlayStateInfo.setScreenMode(screenMode);
        }
    }

    public int getScreenMode()
    {
        if (mPlayStateInfo != null)
        {
            return mPlayStateInfo.getScreenMode();
        }

        return Constant.ScreenMode.SCREEN_FULL;
    }

    /****/

    /**
     * 
     * setMediaData：给ViedeoView设置播放数据
     * 
     * void
     * @exception
     */
    protected boolean setMediaData()
    {
        if (mUIHandler != null)
        {
            mUIHandler.sendEmptyMessage(MSG_HIDE_ACCELERATION);
        }

        VideoInfo mbi = new VideoInfo();
        mbi = getCurrentMediaInfo();

        if (mbi == null)
        {

            // 播放器关闭，Activity销毁时，先回发stop状态，再立即解除绑定，其后不再向Sender端回发播放器的任何状态
            if (bMCSMode)
            {
                onNotifyStop();
                unbind();
            }

            stop();

            // Log.e(TAG, "---------before finish");
            mSubSurface.setVisibility(View.INVISIBLE);
            finish();

            return false;
        }

        if (mSbpw != null)
        {
            mSbpw.setMmbi(mbi);
            mSbpw.resetSeekbar();
            mVVAdapter.isSeeking(false);
        }

        String strUrl = mbi.getUrl();

        if (!StringUtils.isEmpty(strUrl))
        {
            stopSyncSeekPos();

            delayHidePop(1);
            if (mUIHandler != null)
            {
                mUIHandler.sendEmptyMessage(MSG_SHOW_PROGRESS);
            }

            mBdMntPath = null;
            if (PlatformConfig.isSupportHisiMediaplayer() && Constant.DeviceType.isLocalDevice(mbi.getmDeviceType()) && strUrl.trim().toLowerCase().endsWith(".iso"))
            {
                if (mHiBDInfo == null)
                {
                    mHiBDInfo = new HiBDInfo();
                }
                IMountService mountService = getMountService();
                if (null != mountService)
                {
                    mBdMntPath = Mount.mountISO(mountService, strUrl);
                }
                BDInfo bDInfo = new BDInfo();
                if (bDInfo.isBDFile(mBdMntPath))
                {
                    StringBuffer _Buf = new StringBuffer();
                    _Buf.append(BD_PREFIX);
                    _Buf.append(mBdMntPath);
                    _Buf.append("?playlist=");
                    _Buf.append(mHiBDInfo.getDefaultPlaylist());
                    strUrl = _Buf.toString();
                    Log.d(TAG, "MSG_UI_VIDEOVIEW_SETDATA strUrl = " + strUrl);
                    mHiBDInfo.openBluray(strUrl);
                }
            }

            // 处理来自界面点击/甩屏的请求，甩、推的时候带有seek，其他不会带有
            int seek = 0;
            if (mUIHandler != null)
            {
                Message msgVideo = Message.obtain();
                msgVideo.what = MSG_UI_VIDEOVIEW_SETDATA;
                msgVideo.arg1 = seek;
                msgVideo.obj = strUrl;

                mUIHandler.sendMessage(msgVideo);
            }
            else
            {
                if (mHiBDInfo != null)
                {
                    mHiBDInfo.closeBluray();
                }
            }
        }

        return true;
    }

    /**
     * 
     * play：执行播放操作
     * 
     * void
     * @exception
     */
    protected void play()
    {
        Log.d(TAG, "player is invoke");
        if (mUIHandler != null)
        {
            mUIHandler.sendEmptyMessage(MSG_UI_VIDEOVIEW_PLAY);
        }
    }

    /**
     * 
     * seekTo：Seek定位操作
     * 
     * @param msec：定位到得播放位置 void
     * @exception
     */
    private void seekTo(int msec)
    {
        mVVAdapter.isSeeking(true);
        Log.d(TAG, "------seekTo() --" + mVVAdapter.isSeeking());
        if (mUIHandler != null)
        {
            Message msgseek = Message.obtain();
            msgseek.what = MSG_UI_VIDEOVIEW_SEEK_TO;
            msgseek.arg1 = msec;
            mUIHandler.sendMessage(msgseek);

            mUIHandler.removeMessages(MSG_PROGRESS_CHANGED);
            mUIHandler.sendEmptyMessageDelayed(MSG_PROGRESS_CHANGED, 500);
        }
        else
        {
            mVVAdapter.isSeeking(false);
        }

        String url = getCurMediaUrl();
        if (url == null)
        {
            return;
        }

        // 媒体中心模式时保存进度 l00174030
        if (url != null)
        {
            if (isMyMediaType())
            {
                HistoryListRecord.getInstance().put(url, msec);
            }
        }
    }

    private void sendSeekMsg(int msec)
    {
        if (mSeekHandler == null)
        {
            return;
        }

        Message msg = Message.obtain();
        msg.arg1 = msec;
        msg.what = 0;

        mSeekHandler.sendMessage(msg);
    }

    protected void pause()
    {

        if (mUIHandler != null && null != mVVAdapter && !mVVAdapter.isSeeking())
        {
            mUIHandler.sendEmptyMessage(MSG_UI_VIDEOVIEW_PAUSE);
        }
    }

    /**
     * 
     * stop：停止操作
     * 
     * void
     * @exception
     */
    protected void stop()
    {
        if (bMCSMode && isSharingStop)
            return;

        mSbpw.setbPrepared(false);

        Log.d(TAG, "stop is invoke  stopallsyncseek");

        /** liyang DTS2013051702993 **/
        // 本地播放视频文件，设置视频循环播放，影片播放完，在将要播放下一影片时，切换全屏播放模式或音轨或字幕时，停止运行。
        isMenuNeedShow = false;
        // 返回键时，菜单需要重新加载
        isMenuHasCreated = false;

        // 隐藏杜比弹出框
        sendDoblyWinMsg(false);

        if (mVV != null)
        {
            stopAllSyncSeek();
            /** Mender:l00174030;Reason:在stop之前，删除所有的消息，防止状态异常 **/
            if (mUIHandler != null)
            {
                mUIHandler.removeAllMsgs();
                Log.d(TAG, "mUIHandler removeAllMsgs for the IllegalStateException.");
            }

            if (mSeekHandler != null)
            {
                mSeekHandler.removeMessages(0);
            }

            stopNow();
        }
    }

    public void passIntentForBrowser()
    {
        int index = -1;
        if (mPlayStateInfo != null)
        {
            index = mPlayStateInfo.getCurrentIndex();
        }
        Bundle bundle = new Bundle();
        bundle.putInt("playIndex", index);
        // bundle.putInt("mediaType", Constant.MediaType.VIDEO);

        Intent intent = new Intent();
        intent.putExtras(bundle);

        Log.i(TAG, "passIntentForBrowser -index = --" + index);

        setResult(RESULT_OK, intent);
    }

    /**
     * 
     * syncSeekPos：同步seek位置
     * 
     * void
     * @exception
     */
    private void syncSeekPos()
    {

        if (mUIHandler != null)
        {
            mUIHandler.sendEmptyMessage(MSG_SYNC_SEEK);
        }
    }

    /**
     * 
     * stopSyncSeekPos：停止seek位置的同步
     * 
     * void
     * @exception
     */
    private void stopSyncSeekPos()
    {

        if (mUIHandler != null)
        {
            mUIHandler.sendEmptyMessage(MSG_STOP_SYNC_SEEK);
        }
    }

    /**
     * 停止所有的seek操作，包括seek进度条同步、seek pos同步到sender stopAllSyncSeek
     * 
     * void
     * @exception
     */
    private void stopAllSyncSeek()
    {
        if (mUIHandler != null)
        {
            mUIHandler.stopAllSyncSeek();
        }
    }

    /**
     * 中间弹出菜单
     */
    private VideoSettingDialog mPopMenu;

    public boolean menuOpened()
    {
        Log.d(TAG, "videoPlayer onMenuOpened");
        if (mSbpw == null || !mSbpw.isbPrepared())
        {
            return false;
        }

        delayHidePop(1);

        // 弹出菜单时，暂停视频的播放。
        pause();

        // 创建菜单
        createPopMenu();
        
        Log.i(TAG, "menuOpened->isMenuNeedShow:" + isMenuNeedShow);
        
        /** liyang DTS2013051702993 **/
        // 本地播放视频文件，设置视频循环播放，影片播放完，在将要播放下一影片时，切换全屏播放模式或音轨或字幕时，停止运行。
        if (isMenuNeedShow)
        {
            // Log.e("subinfolog", "onMenuOpened");
           /* if (mVVAdapter.getSubtitleList().size() > 0)
            {
                subId = mVVAdapter.getCurrentSudId();
            }*/

            if (mVVAdapter.getAudioinfos() != null && mVVAdapter.getAudioinfos().size() > 0)
            {
//            	Log.d(TAG, "soundId =" + mVVAdapter.getCurrentSoundId());
//                soundId = mVVAdapter.getCurrentSoundId();
            }
        }

        if (mPopMenu.isShowing())
        {
            mPopMenu.hide();
        }
        else
        {
           /* // 加载菜单的显示项
            if (!isMenuHasCreated)
            {
              
            }*/
        	loadMenu();
            isMenuHasCreated = true;
            mPopMenu.replayLastSelected();
            if(mPopMenu.isCreated())
            	mPopMenu.rebuildView();
            mPopMenu.show();
            // mPopMenu.getCurrentFocus().requestFocus();
        }

        return false;
    }

    private boolean openBottomMenu()
    {
        Log.d(TAG, "videoPlayer openBottomMenu");

        if (mSbpw == null || !mSbpw.isbPrepared())
        {
            return false;
        }

        delayHidePop(1);

        // 弹出菜单时，暂停视频的播放。
        pause();

        createBottomPopMenu();

        if (isMenuNeedShow)
        {
            // Log.e("subinfolog", "onMenuOpened");
            /*if (mVVAdapter.getSubtitleList().size() > 0)
            {
                subId = mVVAdapter.getCurrentSudId();
            }*/

            if (mVVAdapter.getAudioinfos() != null && mVVAdapter.getAudioinfos().size() > 0)
            {
//                soundId = mVVAdapter.getCurrentSoundId();
            }
        }

        return true;
    }

    private void loadBottomMenu()
    {
        if (mBottomPopMenu != null)
        {
            mBottomPopMenu.add(1, BottomMenuSelectType.TIME_SEEK, R.drawable.time_seek, 1, 1, getResources().getString(R.string.time_seek));
            mBottomPopMenu.add(2, BottomMenuSelectType.PLAY_SEETING, R.drawable.menu_icon_settings, 2, 2,
                    getResources().getString(R.string.play_settings));
        }
    }

    private void createBottomPopMenu()
    {
        // isBottomPopupAutoDismiss = true;
        mCurrSelectType = null;
        if (mBottomPopMenu == null)
        {
            mBottomPopMenu = new BottomPopMenu(this);
        }
        Log.e("", "sdfsdfsdfsdfsdfsdfsd");

        mBottomPopMenu.setOnSelectTypeListener(this);
        mBottomPopMenu.setOnDismissListener(mPopupWindowDismissListener);

        if (mBottomPopMenu.isShowing())
            mBottomPopMenu.hide();
        else
        {
            // 清空之前备份当前聚焦的MenuItem的ID
            int id = -1;
            id = mBottomPopMenu.getCurrentMenuItem();
            ArrayList<MenuItemImpl> menuItems = mBottomPopMenu.getCurrentMenuItemImpl();
            mBottomPopMenu.clear();

            // 重新加载menu项
            loadBottomMenu();

            if (id != -1)
            {
                // 恢复聚焦到上次的那个ID上
                mBottomPopMenu.setCurrentMenuItem(id);
            }

            mBottomPopMenu.show(myvideo);
        }
    }

    private PopupWindow.OnDismissListener mPopupWindowDismissListener = new PopupWindow.OnDismissListener()
    {
        @Override
        public void onDismiss()
        {
            if (mCurrSelectType == null || (mCurrSelectType != BottomMenuSelectType.TIME_SEEK && mCurrSelectType != BottomMenuSelectType.PLAY_SEETING))
            {
                chgVideoStatusWhenPopMenu();
            }
        }
    };

    /**
     * 创建菜单
     * 
     * @see [类、类#方法、类#成员]
     */
    private void createPopMenu()
    {
        if (mPopMenu == null)
        {
            // onCreateOptionsMenu(menu);
            mPopMenu = new VideoSettingDialog(this);
            // 注意： 必须创建一项
            // 由于我们是截获了系统的Menu，所以一定要创建一个，否则系统以为没有菜单，就不会调用onMenuOpened函数
            mPopMenu.setOnSelectTypeListener(this);
            mPopMenu.setOnDismissListener(mPopMenuDismissListener);
            // 重现加载menu项
            //mPopMenu.clear();
        }
    }

    private OnDismissListener mPopMenuDismissListener = new OnDismissListener()
    {
        @Override
        public void onDismiss(DialogInterface arg0)
        {
            Log.d(TAG, "onDismiss mCurrSelectType = " + mCurrSelectType);
            MenuItemImpl currFocusItem = mPopMenu.getCurrentFocusItemImpl();
            if (currFocusItem != null)
            {
                if (currFocusItem.getSelectType() != PopMenuSelectType.SUBTITLE_OUTTER_SET)
                {
                    chgVideoStatusWhenPopMenu();
                }
            }
        }

    };

    public void chgVideoStatusWhenPopMenu()
    {
        // zkf61715 从定位对话框按确定播放时，如果此时还未seek完成，需要等到seekComplete时再执行play()方法
        // 否则会因为MSG_UI_VIDEOVIEW_PLAY消息未执行而概率性导致按确定键以后画面卡在当前的帧
        if (mVVAdapter.isSeeking())
        {
            timeSeekToPlay = true;
            return;
        }
        play();
    }

    /**
     * 加载菜单的显示项
     * 
     * @see [类、类#方法、类#成员]
     */
    private void loadMenu()
    {
        // 清空menu
        mPopMenu.clearCategories();
        Log.d(TAG, "videoPlayer loadMenu");

        // 播放比例菜单的显示项
        loadMenuScreenRate();

        /* BEGIN: Added by r00178559 for AR-0000698413 2014/02/12 */
        /* 支持音效选择 */
        loadMenuChannelMode();
        /* END: Added by r00178559 for AR-0000698413 2014/02/12 */

        // 播放模式菜单的显示项
        loadMenuPlayMode();

        // 字幕菜单的显示项
        loadMenuSubTitle();

        // 音轨菜单的显示项
        loadMenuSound();
        
        //加载3D模式
        load3DMode();
    }

    /**
     * 加载播放模式菜单的显示项
     * 
     * @see [类、类#方法、类#成员]
     */
    private void loadMenuPlayMode()
    {
        // 加载“播放模式”面板
        MenuCategory menuCgy = new MenuCategory();
        menuCgy.setCategoryName(getResources().getString(R.string.play_mode));
        ArrayList<MenuItemImpl> itemImpls = new ArrayList<MenuItemImpl>();
        /* BEGIN: Modified by c00224451 for DTS2014021706506 2014/2/21 */
        // 列表循环
        MenuItemImpl item = new MenuItemImpl(this, 1, PopMenuSelectType.REPEAT_LIST, R.drawable.video_menu_subtitile, 1, 1, getResources()
                .getString(R.string.repeat_list));
        itemImpls.add(item);

        // 单曲循环
        item = new MenuItemImpl(this, 1, PopMenuSelectType.REPEAT_ONE, R.drawable.video_menu_subtitile, 1, 1, getResources().getString(
                R.string.repeat_one));
        itemImpls.add(item);

        
        // 只播放当前，播放完之后退出
        item = new MenuItemImpl(this, 1, PopMenuSelectType.SINGLE_PLAY, R.drawable.video_menu_subtitile, 1, 1, getResources().getString(
                R.string.single_play));
        itemImpls.add(item);
        
        // 把播放模式面板加入到菜单
        menuCgy.setMenuItems(itemImpls);

        // 设置当前菜单的焦点框
        if (getPlayMode() == Constant.MediaPlayMode.MP_MODE_ALL_CYC)
        {
            menuCgy.setSelectIndex(0);
        }
        else if(getPlayMode() == Constant.MediaPlayMode.MP_MODE_SINGLE_CYC)
        {
            menuCgy.setSelectIndex(1);
        }
        else {
        	menuCgy.setSelectIndex(2);
        }
        /* BEGIN: Modified by c00224451 for DTS2014021706506 2014/2/21 */

        // 把播放模式面板加入到菜单
        mPopMenu.addMenuCategory(menuCgy);
    }

    /**
     * 加载字幕菜单的显示项
     * 
     * @see [类、类#方法、类#成员]
     */
    private void loadMenuSubTitle()
    {

        // reset index
        innerSubIndex = 0;
        extraSubIndex = 0;
        subIndex = 0;

        // 若没有字幕则不显示字幕菜单
        if (!hasAvailExtraSub())
        {
            Log.d(TAG, "the movie has no subtitle.");
            // return;
        }

        // 加载“字幕”面板
        MenuCategory menuCgy = new MenuCategory();
        menuCgy.setCategoryName(getResources().getString(R.string.video_menu_subtitle));
        ArrayList<MenuItemImpl> itemImpls = new ArrayList<MenuItemImpl>();

        MenuItemImpl item = null;
        String str = getApplication().getString(R.string.video_hint_subtitle_id);

        //int order = 0;

        if (mVVAdapter != null)
        {
            List<SubInfo> list1 = mVVAdapter.getSubtitleList();
            Log.i(TAG, "loadMenuSubTitle->list1:" + list1);
            if (list1 == null || list1.size() == 0)
            {
                Log.e(TAG, "loadMenuSubTitle get subtile list is null or empty!");
            }
            else
            {
                for (int i = 0; i < list1.size(); i++)
                {
                   // order = i;
                    SubInfo tmp = list1.get(i);

                    if (tmp != null)
                    {
                        String title = getSubTitleName(tmp);

                        item = new MenuItemImpl(this, i, PopMenuSelectType.SUBTITLE_INNER_SET, R.drawable.video_menu_subtitile, 1, tmp.getSubid(), title);
                        itemImpls.add(item);
                    }
                    else
                    {

                        item = new MenuItemImpl(this, i, PopMenuSelectType.SUBTITLE_INNER_SET, R.drawable.video_menu_subtitile, 1, tmp.getSubid(), str + "."
                                + String.valueOf(i + 1));
                        itemImpls.add(item);
                    }
                   // order++;
                }
                
                // 把播放模式面板加入到
                menuCgy.setMenuItems(itemImpls);
                menuCgy.setSelectIndex(subId);
                mPopMenu.addMenuCategory(menuCgy);
            }
        }

        // 其它
      /*  item = new MenuItemImpl(this, 1, PopMenuSelectType.SUBTITLE_OUTTER_SET, R.drawable.video_menu_subtitile, 1, order, getResources().getString(
                R.string.video_hint_subtitle_state_other));
        itemImpls.add(item);*/

      
    }

    private int innerSubIndex = 0;

    private int extraSubIndex = 0;

    private int subIndex = 0;

    private String getSubTitleName(SubInfo si)
    {
        if (si == null)
        {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        if (si.isExtra())
        {
            extraSubIndex++;
            sb.append(getString(R.string.video_hint_subtitle_id));
            sb.append(++subIndex);
        }
        else
        {
            innerSubIndex++;
            sb.append(getString(R.string.video_hint_subtitle_id));
            sb.append(++subIndex);
        }

        sb.append(" ");

        if (si.getLanguage() != null)
        {
            if (si.getLanguage().equals("-"))
            {
                // sb.append(si.getLanguage());
            }
            else if (StringUtils.isNotEmpty(si.getLanguage()))
            {
                return si.getLanguage();
            }

        }

        return sb.toString();
    }

    /**
     * 设置字幕的当前状态
     * 
     * @see [类、类#方法、类#成员]
     */
    public void setSubtitle(int trackIndex, int menuIndex)
    {
        Log.d(TAG, "subtitle 1 isMenuNeedShow:" + isMenuNeedShow);
        Log.d(TAG, "subtitle 1 subNum:" + subNum);
        // 防止重入
        if (trackIndex == subId)
        {
            Log.d(TAG, "subtitle index == subId!");
            return;
        }
        if (isMenuNeedShow && subNum > 0)
        {
            if (mVVAdapter != null)
            {
            	MediaPlayer mediaPlayer = mMediaPlayer.getOriginMediaPlayer();
            	mediaPlayer.selectTrack(trackIndex);
            	subId = menuIndex;
               /* Log.d(TAG, "subtitle 1 subNum:" + subNum);
                mVVAdapter.setSubId(index);
                subId = mVVAdapter.getCurrentSudId();*/
            }

        }
    }

    /* BEGIN: Added by c00224451 for AR-0000698413 外挂字幕 2014/2/24 */

    /**
     * 加载设置外置字幕界面
     * 
     * @see [类、类#方法、类#成员]
     */
    private void loadExtraSubtitle()
    {
        if (null == mGetAllFlatFolders || mGetAllFlatFolders.size() == 0)
        {
            Toast.makeText(this, getString(R.string.video_toast_no_exist_subtiles), Toast.LENGTH_SHORT).show();
            return;
        }

        if (null == mSubtitleSelectPopup)
        {
            mSubtitleSelectPopup = new SubtitleSelectPopup(this, mGetAllFlatFolders, R.style.DialogCustomizeStyle);
            mSubtitleSelectPopup.setOnSelectListener(mOnSubtileSelectListener);
            mSubtitleSelectPopup.setOnDismissListener(mSubtileDismissListener);
        }
        else
        {
            mSubtitleSelectPopup.initParam();
            mSubtitleSelectPopup.restoreData();
        }

        mSubtitleSelectPopup.showDialog();
        pause();
    }

    private OnSubtileSelectListener mOnSubtileSelectListener = new OnSubtileSelectListener()
    {
        @Override
        public void onSelect(String fullPath)
        {
            setSubtitlePath(fullPath);
        }
    };

    private OnDismissListener mSubtileDismissListener = new OnDismissListener()
    {

        @Override
        public void onDismiss(DialogInterface arg0)
        {
            chgVideoStatusWhenPopMenu();
        }

    };

    /**
     * 设置外挂字幕
     * 
     * @see [类、类#方法、类#成员]
     */
    public void setSubtitlePath(String path)
    {
        // 防止重入
        if (null == path || path.equals(subtitlePath))
        {
            return;
        }

        Log.d(TAG, "subtitle 1 isMenuNeedShow:" + isMenuNeedShow + " subtitle path:" + path);
        if (isMenuNeedShow)
        {
            if (mVVAdapter != null)
            {
                Log.d(TAG, "subtitle 1 subNum:" + subNum);

                int ret = mVVAdapter.setSubPath(path);
                if (0 == ret)
                {
                    subtitlePath = path;
                }
                else
                {
                    Toast.makeText(this, getString(R.string.video_toast_invalid_subtile), Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    /**
     * 字幕设置选项
     * 
     * @see [类、类#方法、类#成员]
     */
    public void subtitleSetting()
    {
        loadExtraSubtitle();
    }

    /**
     * 加载音轨菜单的显示项
     * 
     * @see [类、类#方法、类#成员]
     */
    private void loadMenuSound()
    {
        // 若没有音频则不显示音频菜单
        if (!hasSound())
        {
            Log.d(TAG, "the movie has only one sound track.");
            return;
        }

        // 加载“Lauguage”面板
        MenuCategory menuCgy = new MenuCategory();
        menuCgy.setCategoryName(getResources().getString(R.string.video_menu_sound));
        ArrayList<MenuItemImpl> itemImpls = new ArrayList<MenuItemImpl>();

        // 把获取资源放到循环外面，调高部分性能。
        String audio = getResources().getString(R.string.video_menu_sund_lang);

        MenuItemImpl item = null;

        List<AudioInfoOfVideo> list = mVVAdapter.getAudioinfos();
        soundNum = list.size();

        Log.e(TAG, "soundNum:" + soundNum);

        for (int i = 0; i < soundNum; i++)
        {
            AudioInfoOfVideo aiov = list.get(i);
            if (aiov != null)
            {
                if (StringUtils.isNotEmpty(aiov.getlauguage()))
                {
                    item = new MenuItemImpl(this, 1, PopMenuSelectType.TRACK_MODE_SET, R.drawable.video_menu_default, 1, i, aiov.getlauguage());
                }
                else
                {
                    item = new MenuItemImpl(this, 1, PopMenuSelectType.TRACK_MODE_SET, R.drawable.video_menu_default, 1, i, audio + " " + i + 1);
                }
            }
            else
            {
                item = new MenuItemImpl(this, 1, PopMenuSelectType.TRACK_MODE_SET, R.drawable.video_menu_default, 1, i, audio + " " + i + 1);
            }

            itemImpls.add(item);
        }

        // 把播放模式面板加入到
        menuCgy.setMenuItems(itemImpls);
        menuCgy.setSelectIndex(soundId);
        mPopMenu.addMenuCategory(menuCgy);
    }
    
    
    /**
     * 添加3D菜单
     */
    private void load3DMode(){
        // 加载“3D”面板
        MenuCategory menuCgy = new MenuCategory();
        menuCgy.setCategoryName(getResources().getString(R.string.three_d_change));
        ArrayList<MenuItemImpl> itemImpls = new ArrayList<MenuItemImpl>();
        /* BEGIN: Modified by c00224451 for DTS2014021706506 2014/2/21 */
        // 2D模式
        MenuItemImpl item = new MenuItemImpl(this, 1, PopMenuSelectType.MODE_2D, R.drawable.video_menu_subtitile, 1, 1, getResources()
                .getString(R.string.two_d));
        itemImpls.add(item);

        // MVC 3D模式
        item = new MenuItemImpl(this, 1, PopMenuSelectType.MODE_MVC_3D, R.drawable.video_menu_subtitile, 1, 1, getResources().getString(
                R.string.mvc_three_d));
        itemImpls.add(item);

        
        // 左右3D模式
        item = new MenuItemImpl(this, 1, PopMenuSelectType.MODE_SIDE_BY_SIDE_TO_3D, R.drawable.video_menu_subtitile, 1, 1, getResources().getString(
                R.string.left_right_three_d));
        itemImpls.add(item);
        
        // 上下3D模式
        item = new MenuItemImpl(this, 1, PopMenuSelectType.MODE_TOP_BOTTOM_TO_3D, R.drawable.video_menu_subtitile, 1, 1, getResources().getString(
                R.string.up_down_three_d));
        itemImpls.add(item);
        
        // 左右2D模式
        item = new MenuItemImpl(this, 1, PopMenuSelectType.MODE_SIDE_BY_SIDE_TO_2D, R.drawable.video_menu_subtitile, 1, 1, getResources().getString(
                R.string.left_right_two_d));
        itemImpls.add(item);
        
        // 上下2D模式
        item = new MenuItemImpl(this, 1, PopMenuSelectType.MODE_TOP_BOTTOM_TO_2D, R.drawable.video_menu_subtitile, 1, 1, getResources().getString(
                R.string.up_down_two_d));
        itemImpls.add(item);
        // 把播放模式面板加入到菜单
        menuCgy.setMenuItems(itemImpls);
        menuCgy.setSelectIndex(get3DMode());
        // 把播放模式面板加入到菜单
        mPopMenu.addMenuCategory(menuCgy);
    
    }

    /**
     * 设置音轨
     * 
     * @param index 音频索引
     * @see [类、类#方法、类#成员]
     */
    public void setSound(int index)
    {
        // 防止重入
        if (soundId == index)
        {
            return;
        }

        /** liyang DTS2013051702993 已经stop时不显示菜单项 **/
        // 本地播放视频文件，设置视频循环播放，影片播放完，在将要播放下一影片时，切换全屏播放模式或音轨或字幕时，停止运行。
        if (mMediaPlayer != null && isMenuNeedShow)
        {
            if (soundNum > 0)
            {
                Log.d(TAG, "MENU_ID_SOUND soundId" + soundId);

                if (mVVAdapter != null)
                {
                    mVVAdapter.setSoundId(index);
                    showDoblyWin();
                    soundId = index;
                }
            }
        }
    }

    /**
     * 加载播放比例菜单的显示项
     * 
     * @see [类、类#方法、类#成员]
     */
    private void loadMenuScreenRate()
    {
        // 加载“播放模式”面板
        MenuCategory menuCgy = new MenuCategory();
        menuCgy.setCategoryName(getResources().getString(R.string.video_menu_video_display));
        ArrayList<MenuItemImpl> itemImpls = new ArrayList<MenuItemImpl>();

        // 全屏
        MenuItemImpl item = new MenuItemImpl(this, 1, PopMenuSelectType.SCREEN_DISPLAY_FULL, R.drawable.video_menu_subtitile, 1, 1, getResources()
                .getString(R.string.video_menu_video_full));
        itemImpls.add(item);

        // 原尺寸
        item = new MenuItemImpl(this, 1, PopMenuSelectType.SCREEN_DISPLAY_ORIGINAL, R.drawable.video_menu_subtitile, 1, 1, getResources().getString(
                R.string.video_menu_video_original));
        itemImpls.add(item);

        // 等比拉伸
        item = new MenuItemImpl(this, 1, PopMenuSelectType.SCREEN_DISPLAY_SCALE, R.drawable.video_menu_subtitile, 1, 1, getResources().getString(
                R.string.video_menu_video_scale));
        itemImpls.add(item);

        // 把播放模式面板加入到
        menuCgy.setMenuItems(itemImpls);
        menuCgy.setSelectIndex(getScreenMode() - 1);
        mPopMenu.addMenuCategory(menuCgy);
    }

    /**
     * 设置播放比例
     * 
     * @param mode 全屏还是原始比例
     * @see [类、类#方法、类#成员]
     */
    public void setScreenM(int mode)
    {
        // 防止相同状态的重入
        if (mode == getScreenMode())
        {
            return;
        }

        // 设置播放比例
        setScreenMode(mode);
        updateMenuScreen();
    }

    /* BEGIN: Added by r00178559 for AR-0000698413 2014/02/12 */
    /* 支持音效选择 */
    private String[] mChannelModeNames;

    private int[] mChannelModeCodes;

    private void loadChannelModeResources()
    {
        mChannelModeNames = getResources().getStringArray(R.array.channel_mode_name);
        mChannelModeCodes = getResources().getIntArray(R.array.channel_mode_code);
    }

    private boolean isValidChannelModeExist()
    {
        if (null == mChannelModeNames || null == mChannelModeCodes)
        {
            return false;
        }

        if (mChannelModeCodes.length != mChannelModeNames.length)
        {
            return false;
        }

        return true;
    }

    private void loadMenuChannelMode()
    {
        if (!isValidChannelModeExist())
        {
            Log.e(TAG, "loadMenuChannelMode error no ValidChannelModeExist");
            return;
        }

        MenuCategory menuCgy = new MenuCategory();
        menuCgy.setCategoryName(getResources().getString(R.string.video_menu_channel_mode));
        ArrayList<MenuItemImpl> itemImpls = new ArrayList<MenuItemImpl>();

        MenuItemImpl item = null;
        for (int i = 0; i < mChannelModeNames.length; i++)
        {
            item = new MenuItemImpl(this, 1, PopMenuSelectType.CHANNEL_MODE_SET, R.drawable.video_menu_default, 1, i, mChannelModeNames[i]);
            itemImpls.add(item);
        }

        menuCgy.setMenuItems(itemImpls);
        // TODO 此处需根据当前播放的实际情况确定
        menuCgy.setSelectIndex(0);
        mPopMenu.addMenuCategory(menuCgy);
    }

    int channelModeCodes = DEFAULT_CHANNEL_MODE_INDEX;
    private void setChannelMode(int index)
    {
        boolean ret = false;
        if (null != mVVAdapter)
        {
            if (index >= 0 && index < mChannelModeCodes.length)
            {
                channelModeCodes = mChannelModeCodes[index];
            }
            ret = mVVAdapter.setAudioChannelMode(channelModeCodes);
        }
        Log.d(TAG, "setChannelMode " + ret);
    }
    /* END: Added by r00178559 for AR-0000698413 2014/02/12 */

    // private boolean isOpenSub = false;

    /**
     * 监听菜单选项点击事件
     */
    public boolean onOptionsItemSelected(MenuItem item)
    {

        return false;
    }

    public boolean onCreateOptionsMenu(Menu menu)
    {

        Log.i(TAG, "videoplayer onCreateOptionsMenu");

        delayHidePop(1);

        // 注意： 必须创建一项
        // 由于我们是截获了系统的Menu，所以一定要创建一个，否则系统以为没有菜单，就不会调用onMenuOpened函数
        menu.add("menu");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void loadResource()
    {
        if (PlatformConfig.isSupportHisiMediaplayer())
        {
            setContentView(R.layout.video_video_fullscreen_hisisdk17);
        }
        else if (PlatformUtil.isHisiPlatform())
        {
            setContentView(R.layout.video_video_fullscreen);
        }
        else
        {
            setContentView(R.layout.video_video_fullscreen);
        }

        mCircleProgressBar = (ProgressBar) findViewById(R.id.circleProgressBar);
    }

    @Override
    protected void mcsStop(Intent intent)
    {
        bIsPausedByUser = false;

        if (bMCSMode)
        {
            onNotifyStop();
            unbind();
        }

        /** 推送的场合，直接弄死自己，防止状态异常 **/
        finish();

    }

    @Override
    protected void mcsSeek(Intent intent)
    {

        firstSeek = false;

        int seekTo = 0;

        try
        {
            seekTo = intent.getIntExtra(Constant.IntentKey.SEEK_POS, -1);
        }
        catch(Exception e)
        {
            seekTo = -1;
        }
        
        if (-1 == seekTo)
        {
            seekTo = MathUtil.ConvertPercentageToValue(intent.getFloatExtra(Constant.IntentKey.SEEK_POS, -1), mDuration);
    
            if (seekTo < 0)
            {
                seekTo = 0;                
            }
        }

        showPop();

        if (seekTo > mDuration && mDuration != 0)
        {
            Log.e(TAG, "cc msg mcsSeek seekTo = " + seekTo + " mDuration = " + mDuration);
            return;
        }
        if (seekTo <= mDuration)
        {
            VideoPlayerActivity.this.seekTo(seekTo);
        }

        String url = getCurMediaUrl();
        if (url == null)
        {
            return;
        }

        if (isMyMediaType())
        {
            HistoryListRecord.getInstance().put(url, seekTo);
        }
        else
        {
            VideoInfo mbi = getCurrentMediaInfo();
            if (mbi != null)
            {
                mbi.setmSeekTo(seekTo);
            }
        }

    }

    @Override
    protected void mcsPause(Intent intent)
    {
        bIsPausedByUser = true;

        showPop();

        pause();

        if (mSbpw.getmSeekBar().isOnkey())
        {
            mSbpw.doEnter();

            // onSeekBarPopWindowListener.onXChange(0);

        }

    }

    @Override
    protected void mcsPlay(Intent intent)
    {

        bIsPausedByUser = false;

        // 解决推送端在暂停和播放之间进行状态切换时，播控条不消失的现象
        cancelHidPopMessage();
        delayHidePop(DELAY_TIME);

        if (mSbpw.getmSeekBar().isOnkey())
        {
            mSbpw.doEnter();

            // onSeekBarPopWindowListener.onXChange(0);
        }
        else
        {
            mUIHandler.sendEmptyMessage(MSG_UI_VIDEOVIEW_MCSPLAY);
        }
    }

    protected void mcsSetMediaData(Intent intent)
    {
        if (mUIHandler != null)
        {
            mUIHandler.sendEmptyMessage(MSG_HIDE_ACCELERATION);
        }

        bIsPausedByUser = false;

        stopAllSyncSeek();

        if (mUIHandler != null)
        {
            mUIHandler.sendEmptyMessage(MSG_UI_VIDEOVIEW_STOP);
        }

        // 解析传递过来的数据
        parseInputIntent(intent);

        if (mPlayStateInfo != null)
        {

            // 如果是推送或甩屏过来的，恢复为初始播放模式，不复用上一次播放模式
            if (mPlayStateInfo.getSenderClientUniq().trim().equals(Constant.ClientTypeUniq.PUSH_UNIQ.trim())
                    || mPlayStateInfo.getSenderClientUniq().trim().equals(Constant.ClientTypeUniq.SYN_UINQ.trim()))
            {
                setPlayMode(Constant.MediaPlayMode.MP_MODE_SINGLE);
                if (mUIHandler != null)
                {
                    mUIHandler.sendEmptyMessage(MSG_MCS_HIDEMODE);
                }

                PlayerStateRecorder.getInstance().put(PlayerStateRecorder.VIDEO_PLAY_MODE, Constant.MediaPlayMode.MP_MODE_SINGLE);
            }
        }

        // 设置VideoView数据
        setMediaData();

        // 利用callback的接收消息功能，给它发送play命令执行播放操作
        Message msg = Message.obtain();
        msg.what = Constant.MCSMessage.MSG_PLAY;
        sendMessage(msg);

        VideoInfo mbi = new VideoInfo();

        mbi = getCurrentMediaInfo();
        mSbpw.setMmbi(mbi);

    }

    /**
     * 保存seekPos点 saveSeekPosIntoMbi
     * 
     * void
     * @exception
     */
    public void saveSeekPosIntoMbi()
    {
        savePositionNow();
    }

    @Override
    protected int getUUID()
    {
        return Constant.MediaType.VIDEO;
    }

    /**
     * 
     * getMediaType 获取播放器媒体类型 void
     * @exception
     */
    protected int getMediaType()
    {
        return Constant.MediaType.VIDEO;
    }

    /**
     * 删除的是当前播放的视频 onDelecteDeviceId
     * 
     * @return int
     * @exception
     */
    protected int onDelecteDeviceId(String devId)
    {
        stopAllSyncSeek();

        if (mUIHandler != null)
        {
            mUIHandler.sendEmptyMessage(MSG_UI_VIDEOVIEW_STOP);
        }

        onNotifyStop();
        setMediaData();

        return 0;
    }

    private class SeekHandler extends Handler
    {
        public SeekHandler(Looper looper)
        {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg)
        {
            mVVAdapter.seekTo(msg.arg1);
            super.handleMessage(msg);
        }

    }

    protected class UIHandler extends Handler
    {
        public boolean bAlwaysShowPopSeekbar = false;

        public UIHandler()
        {

        }

        private boolean bHide = false;

        private int oldPos = 0;

        boolean bSyncSeek = true;

        public void stopAllSyncSeek()
        {
            bSyncSeek = false;
            setbHide(true);

            hidePop();
        }

        // remove all messages
        public void removeAllMsgs()
        {
            for (int ib = MSG_UI_VIDEOVIEW_MIN; ib < MSG_UI_VIDEOVIEW_MAX; ib++)
            {
                this.removeMessages(ib);
            }
        }

        @Override
        public void handleMessage(Message msg)
        {
            if (bMCSMode && isSharingStop)
            {
                return;
            }

            switch (msg.what)
            {
                case MSG_UI_PROCESSBAR:
                    if (mVV == null)
                    {
                        return;
                    }

                    if (mCircleProgressBar != null)
                    {
                        mCircleProgressBar.setVisibility(msg.arg1);
                    }
                    break;

                case MSG_UI_VIDEOVIEW_SETDATA:
                    mSbpw.setAcceleToEnd(false);
                    mSbpw.setSeekBarEnd(false);
                    Log.d(TAG, "MSG_UI_VIDEOVIEW_SETDATA");
					//begin add by caochao for DTS2014111006777 媒体中心视频时概率性出现“该视频无法播放”
                    timeOutBegin = 0;
					//end add by caochao for DTS2014111006777 媒体中心视频时概率性出现“该视频无法播放”
                    mUIHandler.sendEmptyMessage(MSG_HIDE_ACCELERATION);

                    mSbpw.setbPrepared(false);

                    if (mVV == null)
                    {
                        return;
                    }

                    if (mVV != null)
                    {
                        mUIHandler.sendEmptyMessage(MSG_HIDE_ACCELERATION);

                        String strurl = (String) (msg.obj);

                        Log.d(TAG, "MSG_UI_VIDEOVIEW_SETDATA url = " + strurl);

//                        progressGone();

                        if (StringUtils.isNotEmpty(strurl))
                        {
                            // 设置开始播放时，先回给Sender端播放位置0
                            // 为了解决：UPNP连续推送，进度条保留1s上次的进度；
                            if (bMCSMode)
                            {
                                // 场景：DLNA连续推送，其他场景目前没有；
                                if (mMediaCenterPlayerClient != null && StringUtils.isNotEmpty(getSenderClientUniq())
                                        && getSenderClientUniq().equalsIgnoreCase(Constant.ClientTypeUniq.PUSH_UNIQ))
                                {
                                    mMediaCenterPlayerClient.seek(0);
                                }
                            }

                            // 隐藏杜比的信息窗口
                            sendDoblyWinMsg(false);

//                            mUIHandler.sendEmptyMessage(MSG_SHOW_PROGRESS);

                            /** Mender:l00174030;Reason:videoview drop the special character #,so replace it. **/
						    
                            Uri uri;
							File file = new File(strurl);
							if(file.exists()){
								uri = Uri.fromFile(file);
							}else{
								uri = Uri.parse(Uri.encode(strurl));
							}
							if(mCurrentDevice.getDevices_type() == ConstData.DeviceType.DEVICE_TYPE_DMS)
								uri = Uri.parse(strurl);
                            // mVV.setVideoPath(strurl);
                            Log.e(TAG, "要播放的视频URL为 ：" + String.valueOf(uri));
                            mVVAdapter.setVideoURI(uri);
                            soundId = 0;
                            // zkf61715 seekTo为假异步，需要在子线程中调用避免ANR
                            // mVVAdapter.seekTo(msg.arg1);
                            sendSeekMsg(msg.arg1);
                            setbAlwaysShowPopSeekbar(false);
                        }
                        else
                        {
                            progressGone();
                        }
                    }

                    // 播放下一个视频时，初始任然为全屏
                    setScreenMode(Constant.ScreenMode.SCREEN_FULL);
                    // 播放另外的视频时菜单需要消失重置
                    if (mPopMenu != null && mPopMenu.isShowing())
                    {
                        mPopMenu.hide();
                    }

                    videoHeight = 0;
                    videoWidth = 0;

                    break;
                case MSG_UI_VIDEOVIEW_STOP:

                    Log.d(TAG, "MSG_UI_VIDEOVIEW_STOP");

                    firstSeek = true;


                    /** liyang DTS2013051702993 **/
                    // 本地播放视频文件，设置视频循环播放，影片播放完，在将要播放下一影片时，切换全屏播放模式或音轨或字幕时，停止运行。
                    isMenuNeedShow = false;
                    // 返回键时，菜单需要重新加载
                    isMenuHasCreated = false;

                    if (mVVAdapter != null)
                    {
                        setbAlwaysShowPopSeekbar(false);

                        mVVAdapter.stopPlayback();
                    }

                    break;

                case MSG_UI_VIDEOVIEW_PAUSE:

                    Log.e(TAG, "MSG_UI_VIDEOVIEW_PAUSE");

                    // Log.e(TAG, "----getseek" + mVV.getIsSeeking() + "::" + isBuffereIng);

                    if (mVVAdapter == null)
                    {
                        return;
                    }

                    if (bMCSMode)
                    {
                        Log.d(TAG, "mMediaCenterPlayerClient  pause");
                        if (mMediaCenterPlayerClient != null)
                        {
                            mMediaCenterPlayerClient.pause();
                        }
                    }

                    Log.d(TAG, " play----> pause");
                    setbAlwaysShowPopSeekbar(true);

                    mUIHandler.removeMessages(MSG_UI_VIDEOVIEW_PLAY);

                    mVVAdapter.pause();
                    if (mSbpw != null)
                    {
                        Log.d(TAG, "pause problem --->" + "prepare==" + mSbpw.isbPrepared());
                    }

                    if (mSbpw != null && mSbpw.isbPrepared())
                    {
                        removeMessages(MSG_HIDE_ACCELERATION);
                        mSbpw.ShowPlayStatusImg(R.drawable.play_status_pause);
                    }

                    removeMessages(MSG_PROGRESS_CHANGED);

                    int tempPos = mVVAdapter.getCurrentPosition();

                    Log.d(TAG, "MSG_UI_VIDEOVIEW_PAUSE ---" + tempPos);

                    if (mVVAdapter != null && mSbpw != null && mSbpw.isFocusable() && !mVVAdapter.isSeeking())
                    {
                        Log.d("MSG_UI_VIDEOVIEW_PAUSE", "----seek");

                        mSbpw.seekto(tempPos);
                    }
                    if (mSbpw != null)
                    {
                        mSbpw.pause();
                    }

                    break;
                case MSG_UI_VIDEOVIEW_MCSPLAY:
                    Log.d(TAG, "MSG_UI_VIDEOVIEW_MCSPLAY");
                    if (mCircleProgressBar.getVisibility() == View.GONE)
                    {
                        mSbpw.ShowPlayStatusImg(R.drawable.play_status_play);
                        mUIHandler.sendEmptyMessageDelayed(MSG_HIDE_ACCELERATION, 3000);
                    }
                    mUIHandler.sendEmptyMessage(MSG_UI_VIDEOVIEW_PLAY);
                    break;
                case MSG_UI_VIDEOVIEW_PLAY:
                    if (mVVAdapter != null)
                    {
                        Log.d("dddd", "MSG_UI_VIDEOVIEW_PLAY" + mVVAdapter.isSeeking());
                    }
                    if (mVVAdapter == null || mVVAdapter.isSeeking())
                    {
                        return;
                    }

                    if (bMCSMode)
                    {

                        if (mMediaCenterPlayerClient != null)
                        {
                            Log.d("dddd", "mMediaCenterPlayerClient--->play");
                            mMediaCenterPlayerClient.play();
                        }
                    }
                    Log.d(TAG, "---------mvv =" + isBuffereIng + "::" + mSbpw.isbPrepared());
                    if (!mVVAdapter.isPlaying())
                    {
                        mUIHandler.removeMessages(MSG_UI_VIDEOVIEW_PAUSE);

                        setbAlwaysShowPopSeekbar(false);
                        mVVAdapter.setOnCompletionListener(onCompletionListener);
                        mVVAdapter.start();

                        /**
                         * Mender:l00174030;Reason:when you pause & play, there is some wrong with the original model, set the video size to skip it.
                         **/
                        pauseToPlay();

                        removeMessages(MSG_PROGRESS_CHANGED);
                        sendEmptyMessage(MSG_PROGRESS_CHANGED);

                        mSbpw.play();

                        /** Mender:l00174030;Reason:pause a long time,then play,the progress bar can't disappear. **/
                        if (null != mUIHandler)
                        {
                            mUIHandler.removeMessages(MSG_HIDE_CONTROLER);
                            mUIHandler.sendEmptyMessageDelayed(MSG_HIDE_CONTROLER, 5000);
                        }
                        Log.d(TAG, "video play" + mVVAdapter.getCurrentPosition() + "state==");

                    }

                    /** 修改者：l00174030；修改原因：UCD改变播控方式 **/
                    if (mSbpw.getmSeekBar().getPlayMode() == PlayMode.PLAY_SEEK
                            || (mSbpw.getmSeekBar().getXacceleration() == 0 || mSbpw.getmSeekBar().getXacceleration() == 1))
                        mSbpw.ShowPlayStatusImg(R.drawable.play_status_play);
                    Log.d(TAG, "mSbpw.getIskeyDown()==" + mSbpw.getIskeyDown() + " mSbpw.isTrackingTouch==" + mSbpw.isTrackingTouch);
                    if (mSbpw.getIskeyDown() == false && mSbpw.isTrackingTouch == false)
                    {
                        Log.d(TAG, "play send MSG_PROGRESS_CHANGED");
                        removeMessages(MSG_PROGRESS_CHANGED);
                        sendEmptyMessage(MSG_PROGRESS_CHANGED);
                    }
                    break;

                case MSG_UI_VIDEOVIEW_REVERSE_STATE:

                    Log.e(TAG, "MSG_UI_VIDEOVIEW_REVERSE_STATE" + "state---");
                    // Log.e(TAG, "MSG_UI_VIDEOVIEW_REVERSE_STATE--" + mVV.getIsSeeking());
                    if (mVVAdapter == null || mSbpw.getmSeekBar().isOnkey || !mSbpw.isbPrepared() || mVVAdapter.isSeeking())
                    {
                        return;
                    }
                    if (mVVAdapter.isPlaying())
                    {
                        sendEmptyMessage(MSG_UI_VIDEOVIEW_PAUSE);

                        bIsPausedByUser = true;
                    }
                    else
                    {
                        Log.d(TAG, "pause ---->play  is start");
                        removeMessages(MSG_HIDE_ACCELERATION);
                        mSbpw.ShowPlayStatusImg(R.drawable.play_status_play);
                        /****/

                        sendEmptyMessage(MSG_UI_VIDEOVIEW_PLAY);
                        bIsPausedByUser = false;
                        sendEmptyMessageDelayed(MSG_HIDE_ACCELERATION, 3000);
                    }

                    break;
                case MSG_UI_VIDEOVIEW_SEEK_TO:
                    Log.d(TAG, "MSG_UI_VIDEOVIEW_SEEK_TO");
                    setbAlwaysShowPopSeekbar(false);
                    removeMessages(MSG_HIDE_CONTROLER);
                    mUIHandler.sendEmptyMessageDelayed(MSG_HIDE_CONTROLER, 5000);
                    if (mVVAdapter == null)
                    {
                        return;
                    }

                    if (bMCSMode)
                    {
                        Log.d(TAG, "MSG_UI_VIDEOVIEW_SEEK_TO  1111");
                        if (mMediaCenterPlayerClient != null)
                        {
                            mMediaCenterPlayerClient.seek(msg.arg1);
                            // Log.d(TAG, "MSG_UI_VIDEOVIEW_SEEK_TO  22222");
                        }
                    }
                    sendSeekMsg(msg.arg1);
                    Log.d(TAG, "MSG_UI_VIDEOVIEW_SEEK_TO-------->play");

                    mUIHandler.sendEmptyMessage(MSG_UI_VIDEOVIEW_PLAY);
                    break;

                case MSG_UI_VIDEOVIEW_SAVE_POS:

                    Log.d(TAG, "---->MSG_UI_VIDEOVIEW_SAVE_POS  invoke");

                    VideoInfo mbi = getCurrentMediaInfo();

                    if (mbi != null)
                    {
                        int videoPhoneCurrentPos = 0;

                        if (mVVAdapter != null)
                        {
                            videoPhoneCurrentPos = mVVAdapter.getCurrentPosition();
                        }
                        mbi.setmSeekTo(videoPhoneCurrentPos);
                        // 保存到历史列表 l00174030
                        String strUrl = mbi.getUrl();
                        if (strUrl != null)
                        {
                            Log.i(TAG, "save to history URL = " + strUrl + " Pos = " + videoPhoneCurrentPos);
                            HistoryListRecord.getInstance().put(strUrl, videoPhoneCurrentPos);
                        }

                    }
                    break;
                case MSG_PROGRESS_CHANGED:
                    Log.d(TAG, "isbHide==" + isbHide());
                    if (isbHide())
                    {
                        break;
                    }
                    if (mSbpw != null && mSbpw.getmSeekBar().isOnkey)
                    {
                        removeMessages(MSG_HIDE_CONTROLER);
                        setbAlwaysShowPopSeekbar(true);
                    }
                    else
                    {
                    }

                    int newPos = mVVAdapter.getCurrentPosition();
                    Log.d(TAG, "---->ttttt " + mVVAdapter.isSeeking());
                    if (mVV != null && mSbpw != null && mSbpw.isFocusable() && !(mVVAdapter.isSeeking()))
                    {
                        mSbpw.seekto(newPos);

                        removeMessages(MSG_PROGRESS_CHANGED);

                    }
                    sendEmptyMessageDelayed(MSG_PROGRESS_CHANGED, 500);
                    break;

                case MSG_HIDE_CONTROLER:
                    if (!isbAlwaysShowPopSeekbar())
                    {
                        hidePop();
                        removeMessages(MSG_SHOW_CONTROLER);
                        setbHide(true);
                    }
                    break;

                case MSG_SHOW_CONTROLER:
                    if (mSbpw == null)
                    {
                        break;
                    }

                    mSbpw.setVisibility(View.VISIBLE);
                    mSbpw.setFocusable(true);
                    mSbpw.requestFocus();

                    /** 修改者：l00174030；修改原因：时间控件和控制条一起显示 **/
                    // 显示
                    if (tiemLayout != null)
                    {
                        tiemLayout.setVisibility(View.VISIBLE);
                    }
                    /****/

                    int newCurrPos = mVVAdapter.getCurrentPosition();
                    Log.d(TAG, "MSG_SHOW_CONTROLER----" + mVVAdapter.isSeeking() + newCurrPos);

                    if (!mSbpw.getmSeekBar().isOnkey && !mVVAdapter.isSeeking())
                    {
                        int duration = mVVAdapter.getDuration();

                        if (duration != 0)
                        {
                            mSbpw.getmSeekBar().setKscale((float) newCurrPos / duration);
                        }
                    }
                    if (!mVVAdapter.isSeeking())
                    {
                        Log.d(TAG, "MSG_SHOW_CONTROLER seek to " + newCurrPos);
                        mSbpw.seekto(newCurrPos);

                    }
                    setbHide(false);

                    removeMessages(MSG_PROGRESS_CHANGED);
                    sendEmptyMessageDelayed(MSG_PROGRESS_CHANGED, 200);

                    break;

                case MSG_SYNC_SEEK_POS:

                    if (!bSyncSeek)
                    {
                        break;
                    }

                    if (mVVAdapter.isPlaying())
                    {

                        int tmpPos = mVVAdapter.getCurrentPosition();
                        if (tmpPos != oldPos)
                        {
                            String url = getCurMediaUrl();
                            if (url == null)
                            {
                                return;
                            }
                            if (bMCSMode)
                            {
                                if (mMediaCenterPlayerClient != null)
                                {
                                    mMediaCenterPlayerClient.seek(tmpPos);
                                }
                                if (isMyMediaType())
                                {
                                    HistoryListRecord.getInstance().put(url, tmpPos);
                                }
                            }

                            oldPos = tmpPos;
                        }
                    }

                    // 每隔1s同步一次seek位置
                    sendEmptyMessageDelayed(MSG_SYNC_SEEK_POS, 1000);

                    break;

                case MSG_SYNC_SEEK:

                    bSyncSeek = true;
                    sendEmptyMessage(MSG_SYNC_SEEK_POS);

                    break;

                case MSG_STOP_SYNC_SEEK:

                    bSyncSeek = false;

                    break;

                case MSG_HIDE_HINT:
                    break;
                case MSG_HIDE_ACCELERATION:

                    Log.e(TAG, "MSG_HIDE_ACCELERATION");

                    if (mSbpw != null && mVVAdapter != null)
                    {
                        if (mVVAdapter.isPlaying())
                        {
                            mSbpw.ShowPlayStatusImg(R.drawable.play_status_play);
                        }
                        else
                        {
                            mSbpw.ShowPlayStatusImg(R.drawable.play_status_pause);
                        }
                    }
                    Log.d(TAG, "MSG_HIDE_ACCELERATION is finish ");
                    break;
                case MSG_SHOW_PROGRESS:
                    setProgressBar(View.VISIBLE);
                    break;
                case MSG_HIDE_PROGRESS:

                    setProgressBar(View.GONE);
                    break;

                case MSG_MCS_PLAY:
                    Log.d(TAG, "MSG_MCS_PLAY");
                    mSbpw.ShowPlayStatusImg(R.drawable.play_status_play);
                    firstSeek = true;
                    break;
                case MSG_MCS_HIDEMODE:
                    /** 添加杜比信息的弹窗处理 **/
                case MSG_DOBLY_SHOW:
                    doblyPopWin.showDoblyWin();
                    break;
                case MSG_DOBLY_HIDE:
                    doblyPopWin.hideDoblyWin();
                    break;
                default:
                    break;
            }

            super.handleMessage(msg);
        }

        /**
         * @param bHide the bHide to set
         */
        public void setbHide(boolean bHide)
        {
            Log.d(TAG, "--->setbHide==" + bHide);
            this.bHide = bHide;
        }

        public Context getAppContext()
        {
            return getApplicationContext();
        }

        /**
         * bHide
         * 
         * @return the bHide
         * @since 1.0.0
         */

        public boolean isbHide()
        {
            return bHide;
        }

        /**
         * @param bAlwaysShowPopSeekbar the bAlwaysShowPopSeekbar to set
         */
        public void setbAlwaysShowPopSeekbar(boolean bAlwaysShowPopSeekbar)
        {
            this.bAlwaysShowPopSeekbar = bAlwaysShowPopSeekbar;
        }

        /**
         * bAlwaysShowPopSeekbar
         * 
         * @return the bAlwaysShowPopSeekbar
         * @since 1.0.0
         */

        public boolean isbAlwaysShowPopSeekbar()
        {
            return bAlwaysShowPopSeekbar;
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {

        if (event.getAction() == MotionEvent.ACTION_UP)
        {
            if (mSbpw.getmSeekBar().isOnkey)
            {
                mSbpw.getmSeekBar().setEnSure(true);
                mSbpw.getmSeekBar().mOnSeekBarChangeListener.onKeyTounch(mSbpw.getmSeekBar());
            }
            else
            {
                onSeekBarPopWindowListener.reverseState();
            }
            showPop();
        }

        return super.onTouchEvent(event);
    }

    private void delayHideHintMsg()
    {
        if (mUIHandler != null)
        {
            mUIHandler.removeMessages(MSG_HIDE_HINT);
            mUIHandler.sendEmptyMessageDelayed(MSG_HIDE_HINT, DELAY_TIME);
        }
    }

    private void hideHintMsg()
    {

        if (mUIHandler != null)
        {
            mUIHandler.removeMessages(MSG_HIDE_HINT);
            mUIHandler.sendEmptyMessageDelayed(MSG_HIDE_HINT, 100);
        }
    }

    private boolean hasAvailExtraSub()
    {
        if (mVVAdapter != null)
        {
            subNum = mVVAdapter.getSubtitleList().size();
        }
        else
        {
            subNum = 0;
        }
        return (subNum > 0);
    }

    private boolean hasSound()
    {
        if (mVVAdapter != null)
        {
            soundNum = mVVAdapter.getAudioinfos().size();
        }
        else
        {
            soundNum = 0;
        }

        return (soundNum > 0);
    }

    // private int selectedSubId = 0;

    public boolean isNetWorkVideo(String url)
    {
        Log.d(TAG, "url==" + url);
        if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("rtsp://"))
        {
            return true;
        }
        return false;

    }

    /** l00174030；添加视屏的屏幕比例切换（自动切换、全屏拉伸、等比拉伸） **/
    private void updateMenuScreen()
    {
        // 获取视频的原始大小
        if (mMediaPlayer == null)
        {
            Log.e(TAG, "mMediaPlayer is null, can't scale video.");
            return;
        }

        int height = mMediaPlayer.getVideoHeight();
        int width = mMediaPlayer.getVideoWidth();
        Log.d(TAG, "Video width = " + width + " height = " + height);
        /* BEGIN: Modified by c00224451 for DTS2014021706506 2014/2/21 */
        if (width == 0 || height == 0)
        {
            return;
        }
        switch (getScreenMode())
        {
            case Constant.ScreenMode.SCREEN_FULL:
                width = SCREEN_WIDTH;
                height = SCREEN_HEIGHT;
                break;
            case Constant.ScreenMode.SCREEN_ORIGINAL:
                break;
            case Constant.ScreenMode.SCREEN_SCALE:
                double widthScale = SCREEN_WIDTH * 1.0 / width;
                double heightScale = SCREEN_HEIGHT * 1.0 / height;
                if (widthScale > heightScale)
                {
                    width = (int) (heightScale * width);
                    height = SCREEN_HEIGHT;
                }
                else
                {
                    width = SCREEN_WIDTH;
                    height = (int) (widthScale * height);
                }
                break;
            default:
                break;
        }

        int l = (SCREEN_WIDTH - width) / 2;
        int t = (SCREEN_HEIGHT - height) / 2;
        Log.d(TAG, "l,t,w,h:" + l + " " + t + " " + width + " " + height);
        mVVAdapter.setOutRange(l, t, width, height);
        videoWidth = width;
        videoHeight = height;
        /* END: Modified by c00224451 for DTS2014021706506 2014/2/21 */
    }

    /**
     * 判断是否需要显示视频的缩放菜单 如果已经能够铺满全屏则不显示
     * @return true：需要全屏；false：不需要全屏
     */
    private boolean isNeedScreenScale()
    {
        // 获取视频的原始大小
        if (mMediaPlayer == null)
        {
            Log.e(TAG, "mMediaPlayer is null, can't scale video.");
            return false;
        }

        // 获取视频的原始宽高
        int height = mMediaPlayer.getVideoHeight();
        int width = mMediaPlayer.getVideoWidth();

        // 若高大于720或宽大于1280，则不需要显示该菜单
        if (height >= SCREEN_HEIGHT || width >= SCREEN_WIDTH)
        {
            Log.d(TAG, "video is larger then screen, width = " + width + " height = " + height);
            return false;
        }

        return true;
    }

    // 显示屏幕的分辨率,这里动态获取，用于兼容不同的设备
    private final int SCREEN_WIDTH = DeviceInfoUtils.getScreenWidth(CommonValues.application);

    private int SCREEN_HEIGHT;

    private int videoWidth = 0;

    private int videoHeight = 0;

    private void pauseToPlay()
    {
        // 获取视频的原始大小
        if (mMediaPlayer == null || videoHeight == 0 || videoWidth == 0)
        {
            Log.e(TAG, "mMediaPlayer is null or is the first play, can't scale video.");
            return;
        }

        if (getScreenMode() == Constant.ScreenMode.SCREEN_ORIGINAL)
        {
            mVVAdapter.setScreenScale(videoWidth, videoHeight);
        }
    }

    /** Mender:l00174030;Reason:push and come back, sometimes can't malloc buffer for the vdec **/
    /**
     * 同步stop
     * 
     * @see [类、类#方法、类#成员]
     */
    private void stopNow()
    {
        Log.d(TAG, "MSG_UI_VIDEOVIEW_STOP now");

        if (bMCSMode)
        {
            onNotifyStop();
        }

        if (mUIHandler != null)
        {
            mUIHandler.setbAlwaysShowPopSeekbar(false);
        }

        if (mVVAdapter != null)
        {
            if (!bMCSMode || !isSharingStop)
            {
                try
                {
                    isSharingStop = true;
                    mVVAdapter.stopPlayback();
                }
                catch (IllegalStateException e)
                {
                    Log.e(TAG, "stopNow >>> release error: " + e);
                }

            }

        }

        firstSeek = true;
    }

    /**
     * 同步保存进度
     * 
     * @see [类、类#方法、类#成员]
     */
    private void savePositionNow()
    {
        Log.d(TAG, "---->MSG_UI_VIDEOVIEW_SAVE_POS  invoke");

        VideoInfo mbi = getCurrentMediaInfo();

        if (mbi != null)
        {
            int videoPhoneCurrentPos = 0;

            Log.d(TAG, "---->MSG_UI_VIDEOVIEW_SAVE_POS  invoke mVV= " + mVVAdapter);
            if (mVVAdapter != null)
            {
                videoPhoneCurrentPos = mVVAdapter.getCurrentPosition();
            }
            mbi.setmSeekTo(videoPhoneCurrentPos);
            // 保存到历史列表 l00174030
            String strUrl = mbi.getUrl();
            if (strUrl != null)
            {
                Log.i(TAG, "save to history URL = " + strUrl + " Pos = " + videoPhoneCurrentPos);
                HistoryListRecord.getInstance().put(strUrl, videoPhoneCurrentPos);
            }

        }
    }

    /**
     * 同步暂停
     * 
     * @see [类、类#方法、类#成员]
     */
    private void pauseNow()
    {
        Log.e(TAG, "pauseNow MSG_UI_VIDEOVIEW_PAUSE");

        if (mVVAdapter == null)
        {
            return;
        }

        if (bMCSMode)
        {
            Log.d(TAG, "mMediaCenterPlayerClient  pause");
            if (mMediaCenterPlayerClient != null)
            {
                mMediaCenterPlayerClient.pause();
            }
        }

        // always show the pop windows if it is pause
        Log.d(TAG, " play----> pause");
        mUIHandler.setbAlwaysShowPopSeekbar(true);

        mUIHandler.removeMessages(MSG_UI_VIDEOVIEW_PLAY);

        mVVAdapter.pause();
        if (mSbpw != null)
        {
            Log.d(TAG, "pause problem --->" + "prepare==" + mSbpw.isbPrepared());
        }

        if (mSbpw != null && mSbpw.isbPrepared())
        {
            mUIHandler.removeMessages(MSG_HIDE_ACCELERATION);
            mSbpw.ShowPlayStatusImg(R.drawable.play_status_pause);
            /****/

        }

        mUIHandler.removeMessages(MSG_PROGRESS_CHANGED);
        if (!bMCSMode || !isSharingStop)
        {
            int tempPos = mVVAdapter.getCurrentPosition();

            Log.d(TAG, "MSG_UI_VIDEOVIEW_PAUSE ---" + tempPos);

            if (mVV != null && mSbpw != null && mSbpw.isFocusable() && !mVVAdapter.isSeeking())
            {
                Log.d("MSG_UI_VIDEOVIEW_PAUSE", "----seek");
                mSbpw.seekto(tempPos);
            }
        }
        if (mSbpw != null)
        {
            mSbpw.pause();
        }
    }

    /**
     * onprepare时的第一次采用同步seek
     * 
     * DTS2013040807527 下载应用的过程中甩屏到STB，STB从头开始播放。拉屏回来后，手机声音为0，再甩屏进去，则从头开始播放，然后从推屏的进度开始播放
     * @param msec 时间
     * @see [类、类#方法、类#成员]
     */
    public void seekToNow(int msec)
    {
        if (mVVAdapter != null)
        {
            mVVAdapter.isSeeking(true);
            Log.d(TAG, "------seekToNow() --" + mVVAdapter.isSeeking());
        }
        if (mUIHandler != null)
        {
            // 同步seek
            Log.d(TAG, "MSG_UI_VIDEOVIEW_SEEK_TO");
            mUIHandler.setbAlwaysShowPopSeekbar(false);
            // removeMessages(MSG_SHOW_CONTROLER);
            mUIHandler.removeMessages(MSG_HIDE_CONTROLER);
            mUIHandler.sendEmptyMessageDelayed(MSG_HIDE_CONTROLER, 5000);
            if (mVVAdapter == null)
            {
                return;
            }

            if (bMCSMode)
            {
                Log.d(TAG, "MSG_UI_VIDEOVIEW_SEEK_TO seekToNow  1111");
                if (mMediaCenterPlayerClient != null)
                {
                    mMediaCenterPlayerClient.seek(msec);
                }
            }
            sendSeekMsg(msec);
            Log.d(TAG, "seekToNow MSG_UI_VIDEOVIEW_SEEK_TO-------->play");

            mUIHandler.removeMessages(MSG_PROGRESS_CHANGED);
            mUIHandler.sendEmptyMessageDelayed(MSG_PROGRESS_CHANGED, 500);
        }
        else
        {
            if (null != mVVAdapter)
            {
                mVVAdapter.isSeeking(false);
            }
        }

        String url = getCurMediaUrl();
        if (url == null)
        {
            return;
        }

        // 媒体中心模式时保存进度 l00174030
        if (url != null)
        {
            if (isMyMediaType())
            {
                HistoryListRecord.getInstance().put(url, msec);
            }
        }

        if (mSbpw != null)
        {
            mSbpw.ShowPlayStatusImg(R.drawable.play_status_play);
            mSbpw.resetSeekbar();
        }

    }

    private OnItemClickListener mPlaylistItemclickListener = new OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            Log.i(TAG, "onItemClick:" + position);

            if (position == mPlayListLayout.getCurrentPlayIndex())
            {
                mPlayListLayout.hidePopupWindow();

                // 在播放列表界面时，要显示与停止显示控制条；l00174030
                if (mVVAdapter != null)
                {
                    if (mVVAdapter.isPlaying())
                    {
                        Log.i(TAG, "playlist show the pop.");
                        showPop();
                    }
                    else
                    {
                        Log.i(TAG, "playlist show the pop.");
                        hidePop();
                    }
                }

                if (mUIHandler != null)
                {
                    mUIHandler.sendEmptyMessage(MSG_UI_VIDEOVIEW_REVERSE_STATE);
                }

                return;
            }

            final int i = position;
            resetPlaylistshow(position);
            mUIHandler.post(new Runnable()
            {

                @Override
                public void run()
                {
                    playOnItemClick(i);

                }
            });

        }
    };

    private void showPlayListLayout()
    {
        PopupWindow listpop1 = mPlayListLayout.getPlaylistPop();
        /* BEGIN: Added by s00211113 for DTS2014032605664 2014/3/27 */
        mPlayListLayout.showPrepare();
        /* END: Added by s00211113 for DTS2014032605664 2014/3/27 */
        /* BEGIN: Modified by s00211113 for DTS2014031902437 2014/3/21 */
        if (listpop1 != null)
        {
        	//Log.i(TAG, "showPlayListLayout->screenWidth:" + DeviceInfoUtils.getScreenWidth(CommonValues.application));
            listpop1.showAtLocation(myvideo, Gravity.RIGHT,0, 0);
        }
        /* END: Modified by s00211113 for DTS2014031902437 2014/3/21 */
    }

    private void resetPlaylistshow(int index)
    {
        mPlayListLayout.hidePopupWindow();
        mPlayListLayout.setCurrentPlayIndex(index);
    }

    private void playOnItemClick(int index)
    {
        VideoInfo mbi = new VideoInfo();

        mbi = getItemMediaInfo(index);
        if (mbi != null)
        {

            // 视频上下切换，显示缓冲图标
            if (mUIHandler != null)
            {
                mUIHandler.sendEmptyMessage(MSG_SHOW_PROGRESS);
            }

            // 先停止进度同步
            stopAllSyncSeek();

            // 存在，则直接播放
            Log.d(TAG, "preProgram :" + mbi.getUrl());

            if (mUIHandler != null)
            {
                mUIHandler.setbAlwaysShowPopSeekbar(false);
            }
            if (mVVAdapter != null)
            {
                mVVAdapter.stopPlayback();
            }

            mSbpw.setMmbi(mbi);
            setDuration(0);

            String strUrl = mbi.getUrl();
            if (mUIHandler != null && StringUtils.isNotEmpty(strUrl))
            {
                Message msgSetVideo = Message.obtain();
                msgSetVideo.arg1 = HistoryListRecord.getInstance().get(strUrl);
                msgSetVideo.what = MSG_UI_VIDEOVIEW_SETDATA;
                msgSetVideo.obj = strUrl;

                mUIHandler.sendMessage(msgSetVideo);
            }
            // Log.e(TAG, "player is invoke2222");
            play();
        }
    }

    private VideoInfo getItemMediaInfo(int index)
    {
        if (mPlayStateInfo == null)
        {
            return null;
        }

        VideoInfo mbi = mPlayStateInfo.getIndexMediaInfo(index);
        if (mbi == null)
        {
            return null;
        }

        String strUrl = mbi.getUrl();

        mStrCurrentUrl = new String(strUrl);
        return mbi;
    }

    /**
     * 返回当前的播放索引
     * 
     * @return 播放索引
     * @see [类、类#方法、类#成员]
     */
    public int getCurrentIndex()
    {
        if (mPlayStateInfo != null)
        {
            return mPlayStateInfo.getCurrentIndex();
        }
        return 0;
    }

    /**
     * 处理杜比信息的弹出框 isShow = true：弹出 isShow = false：隐藏
     * @param isShow
     * @see [类、类#方法、类#成员]
     */
    private void sendDoblyWinMsg(boolean isShow)
    {
        if (mUIHandler != null)
        {
            if (isShow)
            {
                // 显示Dobly的信息视窗
                mUIHandler.sendEmptyMessage(MSG_DOBLY_SHOW);
            }
            else
            {
                // 隐藏Dobly的信息视窗
                mUIHandler.sendEmptyMessage(MSG_DOBLY_HIDE);
            }
        }
    }

    private static final String BD_PREFIX = "bluray:";

    private String mBdMntPath;

    private static IMountService getMountService()
    {
        IBinder service = ServiceManager.getService("mount");
        if (service != null)
        {
            return IMountService.Stub.asInterface(service);
        }
        else
        {
            Log.e("MediaFileListService", "Can't get mount service");
        }
        return null;
    }

    @Override
    public void onSelectType(MenuItemImpl menuItem)
    {
    	Log.i(TAG, "onSelectType");
        if (null == menuItem)
        {
            return;
        }

        mCurrSelectType = menuItem.getSelectType();
        int index = menuItem.getOrder();
        if (mCurrSelectType == BottomMenuSelectType.TIME_SEEK)
        {
            pause();
            mBottomPopMenu.dismiss();
            if (mTimeSeekDialog == null)
            {
                mTimeSeekDialog = new TimeSeekDialog(this);
            }

            mTimeSeekDialog.setOnTimeSeekListener(mOnTimeSeekListener);

            if (mTimeSeekDialog.isShowing())
            {
                Log.d(TAG, "mTimeSeekLayout show--->dismiss");
                mTimeSeekDialog.dismiss();
            }
            else
            {
                Log.d(TAG, "mTimeSeekLayout dismiss--->show");
                mTimeSeekDialog.show(myvideo, mVVAdapter.getCurrentPosition(), mVVAdapter.getDuration());
            }
        }
        else if (mCurrSelectType == BottomMenuSelectType.PLAY_SEETING)
        {
            mBottomPopMenu.dismiss();
            menuOpened();
        }

        else
        {
            /** 播放模式 **/
            if (mCurrSelectType == PopMenuSelectType.REPEAT_LIST)
            {
                Log.d(TAG, "change to play loop.");
                setPlayMode(Constant.MediaPlayMode.MP_MODE_ALL_CYC);
                PlayerStateRecorder.getInstance().put(PlayerStateRecorder.VIDEO_PLAY_MODE, Constant.MediaPlayMode.MP_MODE_ALL_CYC);
                saveCycPlayMode();
            }
            else if (mCurrSelectType == PopMenuSelectType.REPEAT_ONE)
            {
                Log.d(TAG, "change to repeat one.");
                setPlayMode(Constant.MediaPlayMode.MP_MODE_SINGLE_CYC);
                PlayerStateRecorder.getInstance().put(PlayerStateRecorder.VIDEO_PLAY_MODE, Constant.MediaPlayMode.MP_MODE_SINGLE_CYC);
                saveCycPlayMode();
            }
            else if(mCurrSelectType == PopMenuSelectType.SINGLE_PLAY){
            	 Log.d(TAG, "change to single play.");
                 setPlayMode(Constant.MediaPlayMode.MP_MODE_SINGLE);
                 PlayerStateRecorder.getInstance().put(PlayerStateRecorder.VIDEO_PLAY_MODE, Constant.MediaPlayMode.MP_MODE_SINGLE);
                 saveCycPlayMode();
            }
            /** 设置字幕 **/
            else if (mCurrSelectType == PopMenuSelectType.SUBTITLE_INNER_SET)
            {
                Log.d(TAG, "set the subtitle = " + index);
                setSubtitle(index, menuItem.getItemId());
            }
            else if (mCurrSelectType == PopMenuSelectType.SUBTITLE_OUTTER_SET)
            {
                MenuItemImpl currFocusItem = mPopMenu.getCurrentFocusItemImpl();
                if (currFocusItem != null)
                {
                    if (currFocusItem.getSelectType() != PopMenuSelectType.SUBTITLE_OUTTER_SET)
                    {
                        return;
                    }
                }
                Log.d(TAG, "subtitle Setting.");
                subtitleSetting();
            }
            /** 设置音频 **/
            else if (mCurrSelectType == PopMenuSelectType.TRACK_MODE_SET)
            {
                Log.d(TAG, "set the sound = " + index);
                setSound(index);
            }
            /** 设置播放比例 **/
            else if (mCurrSelectType == PopMenuSelectType.SCREEN_DISPLAY_FULL)
            {
                Log.d(TAG, "set the screen full");
                setScreenM(Constant.ScreenMode.SCREEN_FULL);
            }
            else if (mCurrSelectType == PopMenuSelectType.SCREEN_DISPLAY_ORIGINAL)
            {
                Log.d(TAG, "set the screen full");
                setScreenM(Constant.ScreenMode.SCREEN_ORIGINAL);
            }
            else if (mCurrSelectType == PopMenuSelectType.SCREEN_DISPLAY_SCALE)
            {
                Log.d(TAG, "set the screen full");
                setScreenM(Constant.ScreenMode.SCREEN_SCALE);
            }
            else if (mCurrSelectType == PopMenuSelectType.CHANNEL_MODE_SET)
            {
                Log.d(TAG, "set sound effect " + index);
                setChannelMode(index);
            }
            /**3D模式*/
            else if(mCurrSelectType == PopMenuSelectType.MODE_2D){
            	set3DMode(ConstData.ThreeDMode.TWO_D);
            }else if(mCurrSelectType == PopMenuSelectType.MODE_MVC_3D){
            	set3DMode(ConstData.ThreeDMode.MVC_3D);
            }else if(mCurrSelectType == PopMenuSelectType.MODE_TOP_BOTTOM_TO_3D){
            	set3DMode(ConstData.ThreeDMode.UD_3D);
            }else if(mCurrSelectType == PopMenuSelectType.MODE_SIDE_BY_SIDE_TO_3D){
            	set3DMode(ConstData.ThreeDMode.LR_3D);
            }else if(mCurrSelectType == PopMenuSelectType.MODE_SIDE_BY_SIDE_TO_2D){
            	set3DMode(ConstData.ThreeDMode.LR_2D);
            }else if(mCurrSelectType == PopMenuSelectType.MODE_TOP_BOTTOM_TO_2D){
            	set3DMode(ConstData.ThreeDMode.UD_2D);
            }
            else
            {
                Log.e(TAG, "menu is out of focus.");
            }
        }

    }

    private OnTimeSeekListener mOnTimeSeekListener = new OnTimeSeekListener()
    {
        @Override
        public void onTimeSeeked(int time)
        {
            seekToNow(time);
        }

        @Override
        public void onDismiss()
        {
            chgVideoStatusWhenPopMenu();
        }
    };

    private void initExtSubTitleInBackground()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                findAndSetSubTitleInfo();
            }
        }).start();
    }

    private void findAndSetSubTitleInfo()
    {
        //LocalDeviceManager manager = LocalDeviceManager.getInstance(this);
       // mGetAllFlatFolders = manager.getAllFlatAVIFolders(Constant.MediaType.SUBTITLE, 0, 100);
    }

    /**
     * 将当前播放索引传回音乐浏览界面
     * @see [类、类#方法、类#成员]
     */
    public void passIntentForVideoBrowser()
    {
        Log.d(TAG, "passIntentForVideoBrowser() IN...");
        Bundle bundle = new Bundle();
        bundle.putInt("playIndex", getCurrentIndex());
        bundle.putInt("mediaType", Constant.MediaType.VIDEO);

        Intent intent = new Intent();
        intent.putExtras(bundle);

        Log.d(TAG, "passIntentForVideoBrowser() IN..." + getCurrentIndex());

        setResult(RESULT_OK, intent);
    }

    public enum BottomMenuSelectType
    {
        TIME_SEEK, // 底部菜单定位时间功能
        PLAY_SEETING // 底部菜单播放设置功能
    }

    public enum PopMenuSelectType
    {
        /******** 视频播放比例菜单 **************/
        SCREEN_DISPLAY_FULL, // 全屏播放
        SCREEN_DISPLAY_ORIGINAL, // 原始比例播放
        SCREEN_DISPLAY_SCALE, // 等比例播放

        /************* 视频声道 **************/
        CHANNEL_MODE_SET, // 声道

        /************* 视频播放模式 **************/
        //CYCLE_PLAY_OPEN, // 开启循环播放
        //CYCLE_PLAY_CLOSE, // 关闭循环播放
        
        /**列表循环播放*/
        REPEAT_LIST,
        /**单曲循环播放*/
        REPEAT_ONE,
        /**单个播放*/
        SINGLE_PLAY,
        
        /************* 视频字幕设置 **************/
        SUBTITLE_INNER_SET, // 内置字幕设置
        SUBTITLE_OUTTER_SET, // 外挂字幕设置

        /************* 视频音轨 **************/
        TRACK_MODE_SET, // 音轨
        
        /**2D*/
        MODE_2D,
        /**MVC 3D*/
        MODE_MVC_3D,
        /**左右3D*/
        MODE_SIDE_BY_SIDE_TO_3D,
        /**上下3D*/
        MODE_TOP_BOTTOM_TO_3D,
        /**左右转2D*/
        MODE_SIDE_BY_SIDE_TO_2D,
        /**上下转2D*/
        MODE_TOP_BOTTOM_TO_2D
    }
    
    // ===============[DTS2014102406965 /
    // DTS2014102402686]====================//
    private Editor mEditor;

    private SharedPreferences mPreferences;

    private static final String VIDEO_PLAY_SET = "VIDEO_PLAY_SET";

    private static final String CYCLE_PLAY_MODE = "CYCLE_PLAY_MODE";

    private static final String SCREEN_DISPLAY_MODE = "SCREEN_DISPLAY_MODE";

    private static final String CHANNEL_MODE = "CHANNEL_MODE";

    private static final String FIRST_START_VIDEOPLAY = "FIRST_START_VIDEOPLAY";

    //默认列表循环播放
    private static final int DEFAULT_CYCLE_PLAY_MODE_INDEX = Constant.MediaPlayMode.MP_MODE_ALL_CYC;

    private static final int DEFAULT_SCREEN_DISPLAYE_MODE_INDEX = Constant.ScreenMode.SCREEN_FULL;

    private static final int DEFAULT_CHANNEL_MODE_INDEX = 0; // 0 对应的是环绕立体声

    private void initVideoPlayPreferences()
    {
        mPreferences = getSharedPreferences(VIDEO_PLAY_SET, Context.MODE_PRIVATE);
        mEditor = mPreferences.edit();

        if (mPreferences.getBoolean(FIRST_START_VIDEOPLAY, true))
        {
            Log.w(TAG, "================== init Video Play Preferences =======================");
            mEditor.putInt(CYCLE_PLAY_MODE, DEFAULT_CYCLE_PLAY_MODE_INDEX);
            mEditor.putInt(SCREEN_DISPLAY_MODE, DEFAULT_SCREEN_DISPLAYE_MODE_INDEX);
            mEditor.putInt(CHANNEL_MODE, DEFAULT_CHANNEL_MODE_INDEX);
            mEditor.putBoolean(FIRST_START_VIDEOPLAY, false);
            mEditor.commit();
        }
    }

    /**
     * 是否开启连续播放
     * @param true--开启 false--关闭
     */
    private void saveCycPlayMode()
    {
        Log.d(TAG, "saveCycPlayMode E cycPlayModeIndex =" + getPlayMode());
        if (mEditor != null)
        {
            mEditor.putInt(CYCLE_PLAY_MODE, getPlayMode());
            mEditor.commit();
            Log.d(TAG, "saveCycPlayMode OK");
        }
    }

    private void saveScreenDisplay()
    {
        Log.d(TAG, "saveScreenDisplay E getScreenMode() =" + getScreenMode());
        if (mEditor != null)
        {
            mEditor.putInt(SCREEN_DISPLAY_MODE, getScreenMode());
            mEditor.commit();
            Log.d(TAG, "saveScreenDisplay OK");
        }
    }

    private void saveChannelMode()
    {
        Log.d(TAG, "saveChannelMode E  channelModeCodes =" + channelModeCodes);
        if (mEditor != null)
        {
            mEditor.putInt(CHANNEL_MODE, channelModeCodes);
            mEditor.commit();
            Log.d(TAG, "saveChannelMode OK");
        }
    }	
	
    private void onNotifyStop()
    {
        if (mMediaCenterPlayerClient != null)
        {
            mMediaCenterPlayerClient.stop(mStrCurrentUrl);
        }
    }
	
    private void onCompleteOperate(IMediaPlayerAdapter mp)
    {
    	Log.i(TAG, "onCompleteOperate");
        /** liyang DTS2013051702993 **/
        // 本地播放视频文件，设置视频循环播放，影片播放完，在将要播放下一影片时，切换全屏播放模式或音轨或字幕时，停止运行。
        isMenuNeedShow = false;

        // 播放完毕时，菜单需要重新加载
        isMenuHasCreated = false;
        if (bMCSMode && mp != null && mMediaCenterPlayerClient != null)
        {
            int duration = mp.getDuration();
            mMediaCenterPlayerClient.seek(duration);
        }
		
        if (mPopMenu != null && mPopMenu.isShowing())
        {
            mPopMenu.hide();
        }

        stopSyncSeekPos();

        progressGone();

        mPlayListLayout.hidePopupWindow();

        String url = getCurMediaUrl();
        if (url == null)
        {
            Log.d(TAG, "OnCompletionListener -- onCompletion mbi == null --");

        }
        else if (isMyMediaType())
        {
            HistoryListRecord.getInstance().put(url, 0);
        }

        Log.d(TAG, "======playMode2:" + getPlayMode());

        firstSeek = true;
        // 单文件播放模式
        if (getPlayMode() == Constant.MediaPlayMode.MP_MODE_SINGLE)
        {
            setToast(getString(R.string.video_program_completion));

            // 播放器关闭，Activity销毁时，先回发stop状态，再立即解除绑定，其后不再向Sender端回发播放器的任何状态
            if (bMCSMode)
            {
                Log.d(TAG, "onCompletion -- bMCSMode");
                onNotifyStop();
                unbind();
            }

            stop();

            mSubSurface.setVisibility(View.INVISIBLE);
            finish();
            return;
        }

        // 单文件循环，全体循环播放模式
        Log.d(TAG, "onCompletion() -- getNextMediaInfo() --");

        VideoInfo mbi = getNextMediaInfo();

        if (mbi == null)
        {
            Log.d(TAG, "onCompletion() -- Can not Find the NextMediaInfo --");

            setToast(getString(R.string.video_program_completion));

            stopSyncSeekPos();

            if (bMCSMode)
            {
                Log.d(TAG, "onCompletion -- bMCSMode");
                onNotifyStop();
                unbind();
            }

            stop();

            mSubSurface.setVisibility(View.INVISIBLE);
            finish();
        }
        else
        {
            Log.d(TAG, "onCompletion() -- Find the NextMediaInfo --");

            mPlayListLayout.setCurrentPlayIndex(mPlayStateInfo.getCurrentIndex());

            setMediaData();

            play();
        }
    }
    
    /**
     * @author fly.gao 
     * 返回当前播放位置
     * @return
     */
    public int getCurrentPosition(){
    	if(mVVAdapter != null)
    		return mVVAdapter.getCurrentPosition();
    	return -1;
    }
    
    /**
     * @author fly.gao
     * 返回当前视频的长度
     * @return
     */
    public int getDuration(){
    	if(mVVAdapter != null)
    		return mVVAdapter.getDuration();
    	return -1;
    }
    
    
}