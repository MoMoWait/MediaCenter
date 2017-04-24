/**
 * @author GaoFei
 * 视频播放器
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
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.icu.text.BreakIterator;
import android.icu.text.StringPrepParseException;
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
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.lang.reflect.Method;

import org.xutils.x;
import org.xutils.view.annotation.ViewInject;

import momo.cn.edu.fjnu.androidutils.data.CommonValues;
import momo.cn.edu.fjnu.androidutils.utils.DeviceInfoUtils;
import momo.cn.edu.fjnu.androidutils.utils.SizeUtils;
import momo.cn.edu.fjnu.androidutils.utils.StorageUtils;
import momo.cn.edu.fjnu.androidutils.utils.ToastUtils;
import com.rockchips.mediacenter.service.HiMediaPlayer;
import com.rockchips.mediacenter.R;
import com.rockchips.mediacenter.service.LocalDeviceManager;
import com.rockchips.mediacenter.bean.LocalMediaInfo;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.data.ConstData.PlayMode;
import com.rockchips.mediacenter.utils.DateUtil;
import com.rockchips.mediacenter.utils.MathUtil;
import com.rockchips.mediacenter.utils.MediaUtils;
import com.rockchips.mediacenter.utils.PlatformUtil;
import com.rockchips.mediacenter.utils.PlatformUtils;
import com.rockchips.mediacenter.utils.StringUtils;
import com.rockchips.mediacenter.service.IMediaPlayerAdapter;
import com.rockchips.mediacenter.service.IVideoViewAdapter;
import com.rockchips.mediacenter.bean.AudioInfoOfVideo;
import com.rockchips.mediacenter.bean.Device;
import com.rockchips.mediacenter.bean.FileInfo;
import com.rockchips.mediacenter.bean.SubInfo;
import com.rockchips.mediacenter.service.OnBufferingUpdateListener;
import com.rockchips.mediacenter.service.OnCompleteListener;
import com.rockchips.mediacenter.service.OnErrorListener;
import com.rockchips.mediacenter.service.OnFastBackwordCompleteListener;
import com.rockchips.mediacenter.service.OnFastForwardCompleteListener;
import com.rockchips.mediacenter.service.OnInfoListener;
import com.rockchips.mediacenter.service.OnPreparedListener;
import com.rockchips.mediacenter.service.OnSeekCompleteListener;
import com.rockchips.mediacenter.view.OrigVideoView;
import com.rockchips.mediacenter.activity.MainActivity;
import com.rockchips.mediacenter.bean.LocalDevice;
import com.rockchips.mediacenter.dobly.DoblyPopWin;
import com.rockchips.mediacenter.videoplayer.data.HistoryListRecord;
import com.rockchips.mediacenter.videoplayer.data.PlayerStateRecorder;
import com.rockchips.mediacenter.videoplayer.data.VideoInfo;
import com.rockchips.mediacenter.videoplayer.data.HistoryListRecord.SubObject;
import com.rockchips.mediacenter.videoplayer.widget.PlayListShowLayoutBase;
import com.rockchips.mediacenter.videoplayer.widget.SeekBarLayout;
import com.rockchips.mediacenter.videoplayer.widget.SubtitleSelectPopup;
import com.rockchips.mediacenter.videoplayer.widget.SubtitleSelectPopup.OnSubtileSelectListener;
import com.rockchips.mediacenter.view.VideoSettingDialog;
import com.rockchips.mediacenter.view.BottomPopMenu;
import com.rockchips.mediacenter.view.MenuCategory;
import com.rockchips.mediacenter.view.MenuItemImpl;
import com.rockchips.mediacenter.view.OnSelectTypeListener;
import com.rockchips.mediacenter.view.PopMenu;
import com.rockchips.mediacenter.view.TimeLayout;
import com.rockchips.mediacenter.view.TimeSeekDialog;
import com.rockchips.mediacenter.view.TimeSeekDialog.OnTimeSeekListener;
import android.os.SystemProperties;
/**
 * 
 * VideoPlayerActivity
 *
 *
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
    protected static final int DELAY_TIME = 5000;
    /**改变当前播放位置的时长*/
    private static final int CHANGE_PLAY_TIME_STEP = 10000;
    /**
     * 是否播放下一首
     */
    public static String KEY_NEXT_PLAY = "is_next_play";
    protected boolean bContinue = false;
    // 之前是否被用户暂停播放器（遥控器暂停、推送端暂停、甩屏端暂停）
    private boolean bIsPausedByUser = false;
    private UIHandler mUIHandler = new UIHandler();
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
    private static final int PLAY = 0;
    private static final int PAUSE = 1;
    private SurfaceHolder mSubHolder;
    private IMediaPlayerAdapter mMediaPlayer;
    private SubObject sub;
    // the num. of subtitle
    private int subNum = 0;
    // the id of the current suntitle
    private int subId = 0;
    /**当前选中声道*/
    private int mChannelModeIndex = 0;
    // 保存外挂字幕路径
    private String subtitlePath = "";
    private int soundNum = 0;
    // the id of the current sound
    private int soundId = 0;
    /** liyang DTS2013051702993 **/
    // 本地播放视频文件，设置视频循环播放，影片播放完，在将要播放下一影片时，切换全屏播放模式或音轨或字幕时，停止运行。
    private boolean isMenuNeedShow = false;
    // 菜单是否已被创建
    private boolean isMenuHasCreated = false;
    //标识视频是否已经准备好
    private boolean isHasPrepared = false;
    // 若片源是杜比音效，则必须弹出杜比标识
    private DoblyPopWin doblyPopWin = null;
    // zkf61715 是否支持快进快退
    // 底部弹出菜单
    private BottomPopMenu mBottomPopMenu;
    private TimeSeekDialog mTimeSeekDialog;
    private SubtitleSelectPopup mSubtitleSelectPopup = null;
    private List<LocalMediaInfo> mGetAllFlatFolders;
    // zkf61715
    private boolean timeSeekToPlay = false;
    // zkf61715 低级别的线程用来循环检测seek操作，当网络视频断网时进行seek操作超时45秒退出
    // 保存进行seek操作时的时间，用来判断超时
    private long timeWhenSeek = 0;
    private Object mCurrSelectType;
    //begin add by caochao for DTS2014111006777 媒体中心视频时概率性出现“该视频无法播放”
    private int mBufferUpdatePercent = 0;
    /**
     * 是否关闭字幕
     */
    private boolean mIsCloseSubtitle;
    /**
     * 错误提示对话框
     */
    private AlertDialog mErrorTipDialog;
    /**是否第一次快进快退*/
    private boolean mIsFirstBackOrGo = true;
    /**视频seek位置*/
    private int mSeekPosition;
    /**右键快进触发次数*/
    private int mFastGoCount;
    /**左键快退触发次数*/
    private int mFastBackCount;
    /**是否是第一次seek到上次播放的位置*/
    private boolean mIsFirstSeek = true;
    /**是否需要弹出控制栏*/
    private boolean mIsNeedShowPop;
    @ViewInject(R.id.vv)
    private OrigVideoView mVV;
    @ViewInject(R.id.video_layout)
    private RelativeLayout myvideo;
    @ViewInject(R.id.subtitle)
    private SurfaceView mSubSurface;
    @ViewInject(R.id.seekbarlayout)
    private SeekBarLayout mSeekBarLayout;
    @ViewInject(R.id.timeLayout)
    private TimeLayout tiemLayout;
    @ViewInject(R.id.circleProgressBar)
    private ProgressBar mCircleProgressBar;
    @ViewInject(R.id.text_subtitle)
    private TextView mTextSubtitle;
    @ViewInject(R.id.text_restart_play)
	private TextView mTextRestartPlay;
    
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "VideoPlayerActivity --> onCreate()--");
        super.onCreate(savedInstanceState);
    }

    private void initViews()
    {
        Log.d(TAG, "onCreate ---> initViews--");
    	//默认不支持加速
        mVV.setOnErrorListener(onErrorListener);
        mVV.setOnPreparedListener(onPreparedListener);
        mVV.setOnInfoListener(onInfoListener);
        mVV.setOnSeekCompleteListener(onSeekCompleteListener);
        mVV.setOnBufferingUpdateListener(onBufferingUpdateListener);
        if (mSubHolder == null)
        {
            mSubHolder = mSubSurface.getHolder();
        }
        mVV.setSubSurfaceHolder(mSubHolder);
        //控制栏初始不显示
        mSeekBarLayout.setVisibility(View.INVISIBLE);
        //时间显示初始时不显示
        tiemLayout.setVisibility(View.INVISIBLE);        
    }

    
    private void initEvent(){
        final String CRLF = System.getProperty("line.separator");
        mTextSubtitle.setVisibility(View.GONE);
    	MediaPlayer originMediaPlayer = mMediaPlayer.getOriginMediaPlayer();
    	originMediaPlayer.setOnTimedTextListener(new  MediaPlayer.OnTimedTextListener() {
			
			@Override
			public void onTimedText(MediaPlayer mp, TimedText text) {
			    if(text == null){
			        mTextSubtitle.setVisibility(View.GONE);
			        return;
			    }
				Log.i(TAG, "onTimedText->text:" + text.getText());
				String subTitle = text.getText();
				subTitle = subTitle.replace(CRLF, "<br/>");
			    mTextSubtitle.setVisibility(View.VISIBLE);
			    mTextSubtitle.setText(Html.fromHtml(subTitle));
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
        //获取字幕信息
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
        //遮罩层
        mSubSurface.setVisibility(View.VISIBLE);
        /** Mender:l00174030;Reason:DTS2013041400627播放视频的过程中长按菜单键，调出近期任务，再次点击媒体中心，视频黑屏停止播放 **/
        /** 把设置URL从oncreate地方下移到此处 **/
        // 设置视频源以及seek位置
        setMediaData();
        // 恢复进度同步
        //syncSeekPos();

        delayHidePop(1);

        super.onResume();

        // 视频电话返回后，如果之前播放器处于暂停状态，恢复为暂停状态
        if (bIsPausedByUser)
        {
            Log.d(TAG, "Resume to pause status  11");
            pause();
            showPop();
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
        super.onPause();
        
        Log.d(TAG, "VideoPlayerActivity --> onPause() --1 --");
        if (doblyPopWin != null)
        {
            doblyPopWin.hideDoblyWin();
        }
        mSubSurface.setVisibility(View.INVISIBLE);
        
        try
        {
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
    }

    @Override
    protected void onStop() {
    	Log.d(TAG, "VideoPlayerActivity --> onStop()--");
        super.onStop();
        
    }
    
    
    @Override
    protected void onDestroy()
    {
   
        Log.d(TAG, "VideoPlayerActivity --> onDestroy()--");
        mVV.isSeeking(false);
        mUIHandler = null;
        mSeekHandlerThread.getLooper().quit();
        mSeekHandlerThread = null;
        mSeekHandler = null;
        super.onDestroy();
    }

     
    @Override
    public void finish() {
    	Log.i(TAG, "finish->stackTrace:" + android.util.Log.getStackTraceString(new Throwable()));
    	super.finish();
    }
    
    
    /**
     * 处理按键堆积
     */

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        // 对音量键不进行按键累积，避免调节音量出现卡顿
        switch (keyCode)
        {
            case KeyEvent.KEYCODE_MENU:
            	if(mVV.isSeeking())
            		return true;
            	if(!isMenuNeedShow)
            		return true;
            	hideRestartPlayTip();
                openBottomMenu();
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_MEDIA_REWIND:
            	if(!isHasPrepared)
            		return true;
            	if(mTextRestartPlay.getVisibility() == View.VISIBLE){
            		mVV.isSeeking(true);
            		sendSeekMsg(0);
            		mTextRestartPlay.setVisibility(View.INVISIBLE);
            	}else if(mSeekBarLayout.getVisibility() != View.VISIBLE){
            		showPop();
            	}else{
            		if(!mVV.isSeeking() && null != mUIHandler){
            			mUIHandler.removeMessages(ConstData.VideoPlayUIMsg.MSG_PROGRESS_CHANGED);
            			changePlayPosition(-1);
            			mFastBackCount++;
            		}
            		
            	}
            	break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
            	if(!isHasPrepared)
            		return true;
            	hideRestartPlayTip();
            	if(mSeekBarLayout.getVisibility() != View.VISIBLE){
            		showPop();
            	}else{
            		if(!mVV.isSeeking() && null != mUIHandler){
            			mUIHandler.removeMessages(ConstData.VideoPlayUIMsg.MSG_PROGRESS_CHANGED);
                		changePlayPosition(1);
                		mFastGoCount ++;
            		}
            	}
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            	if(!isHasPrepared)
            		return true;
            	hideRestartPlayTip();
            	Log.d(TAG, "onKeyDown - KEYCODE_DPAD_CENTER or KEYCODE_MEDIA_PLAY_PAUSE --");
                //显示控制条
                showPop();
                mUIHandler.sendEmptyMessage(ConstData.VideoPlayUIMsg.MSG_UI_VIDEOVIEW_REVERSE_STATE);
                break;

            case KeyEvent.KEYCODE_DPAD_UP:
                Log.d(TAG, "onKeyDown - KEYCODE_DPAD_UP --");
                // preProgram();
                //showPlayListLayout();
                break;

            case KeyEvent.KEYCODE_DPAD_DOWN:
                Log.d(TAG, "onKeyDown - KEYCODE_DPAD_DOWN --");
                //去除右边侧边栏			
                //showPlayListLayout();
                break;

            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_MEDIA_STOP:
                Log.d("vvvv", "onKeyDown - KEYCODE_BACK --" + this);
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
		switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_LEFT:
			case KeyEvent.KEYCODE_MEDIA_REWIND:
				if(mSeekPosition >= 0 && !mIsFirstBackOrGo){
					mUIHandler.removeMessages(ConstData.VideoPlayUIMsg.MSG_FAST_BACK);
					Log.i(TAG, "mSeekPosition:" + DateUtil.getMediaTime(mSeekPosition));
					mIsNeedShowPop = true;
					mVV.isSeeking(true);
					sendSeekMsg(mSeekPosition, 500);
				}
				mFastBackCount = 0;
				mIsFirstBackOrGo = true;
				mSeekPosition = 0;
				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
			case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
				if(mSeekPosition > 0 && !mIsFirstBackOrGo){
					mUIHandler.removeMessages(ConstData.VideoPlayUIMsg.MSG_FAST_GO);
					Log.i(TAG, "mSeekPosition:" + DateUtil.getMediaTime(mSeekPosition));
					mIsNeedShowPop = true;
					mVV.isSeeking(true);
					sendSeekMsg(mSeekPosition, 500);
				}
				mFastGoCount = 0;
				mIsFirstBackOrGo = true;
				mSeekPosition = 0;
				break;
		}
        return true;
    }

    
    @Override
    protected void onNewIntent(Intent intent) {
        Log.i(TAG, "onNewIntent");
        setIntent(intent);
    }
    

    /**
     * 显示无法播放提示框
     * @param tip
     * @param isNextPlay
     */
    private void showCannotPlayDialog(String tip, boolean isNextPlay){
    	Log.i(TAG, "showCannotPlayDialog->tip:" + tip);
    	Log.i(TAG, "showCannotPlayDialog->isNextPlay:" + isNextPlay);
    	//if(mErrorTipDialog != null && mErrorTipDialog.isShowing())
    	//	return;
    	mErrorTipDialog = new AlertDialog.Builder(this).setMessage(tip).setCancelable(false).create();
    	mErrorTipDialog.show();
    	int displayTime = isNextPlay ? 1000 : 2000;
    	Message errorMessage = new Message();
    	errorMessage.what = ConstData.VideoPlayUIMsg.MSG_CLOSE_ERROR_DIALOG;
    	Bundle bundle = new Bundle();
    	bundle.putBoolean(KEY_NEXT_PLAY, isNextPlay);
    	errorMessage.setData(bundle);
    	mUIHandler.sendMessageDelayed(errorMessage, displayTime);
    	//mUIHandler.sendEmptyMessageDelayed(MSG_CLOSE_ERROR_DIALOG, displayTime);
    	
    }
    
    // 设置播放器缓冲图标
    private void setProgressBar(int arg)
    {
        Log.d(TAG, "setProgressBar():{VISIBLE = 0;INVISIBLE = 4;GONE = 8}" + arg);
        mCircleProgressBar.setVisibility(arg);
    }

    // add by w00184463
    // 进度条初始为0
    private void setDuration(int duration)
    {
        Log.d(TAG, "setDuration(): " + duration);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event)
    {
        return super.onKeyLongPress(keyCode, event);
    }

    private void showPop()
    {
        Log.d(TAG, "showPop() --IN--");

        cancelHidPopMessage();
        if (null != mUIHandler)
        {
            mUIHandler.sendEmptyMessage(ConstData.VideoPlayUIMsg.MSG_SHOW_CONTROLER);
        }
        delayHidePop(DELAY_TIME);
    }


    private void cancelHidPopMessage()
    {
        Log.d(TAG, "cancelHidPopMessage()--");
        if (null != mUIHandler)
        {
            mUIHandler.removeMessages(ConstData.VideoPlayUIMsg.MSG_HIDE_CONTROLER);
        }
    }

    private void delayHidePop(int time)
    {
        Log.d(TAG, "delayHidePop()--");
        if (null != mUIHandler)
        {
        	mUIHandler.sendEmptyMessageDelayed(ConstData.VideoPlayUIMsg.MSG_HIDE_CONTROLER, time);
        }
    }

    private void progressGone()
    {
    	Log.i("ProgressBar_Debug", "progressGone");
    	
    	 setProgressBar(View.GONE);
    	
      /*  if (mCircleProgressBar.getVisibility() == View.VISIBLE)
        {
            mUIHandler.sendEmptyMessage(ConstData.VideoPlayUIMsg.MSG_HIDE_PROGRESS);
        }      */  
    }

    private OnCompleteListener onCompletionListener = new OnCompleteListener()
    {
        public void onCompletion(IMediaPlayerAdapter mp)
        {
            Log.d(TAG, "OnCompletionListener -- onCompletion() --");
            //mVV.setOnCompletionListener(null);
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
                    case ConstData.VIDEO_PLAY_ERROR_CODE.ERROR_ID_COPYRIGHT_NO_SVQ:
                    	 messageId = R.string.no_svq;
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
            isHasPrepared = false;
            mSeekPosition = 0;
            progressGone();

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

            playerErrorTimes++;

            // 若是播放为single模式or连续6次无法播放，退出播放器
            if (getPlayMode() == ConstData.MediaPlayMode.MP_MODE_SINGLE || playerErrorTimes >= PLAYER_ERROR_TIMES_MAX)
            {
                playerErrorTimes = 0;
                // 不能播放的提示
                showCannotPlayDialog(getString(messageId), false);
                return true;
            }

            // 播放模式为全体循环播放
            Log.d(TAG, "mCurrentIndex:" + mPlayStateInfo.getCurrentIndex());

            FileInfo mbi = getNextMediaInfo();

            if (mbi == null)
            {
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

    private OnSeekCompleteListener onSeekCompleteListener = new OnSeekCompleteListener()
    {
        public void onSeekComplete(IMediaPlayerAdapter mp)
        {
        	Log.i(TAG, "onSeekComplete");
        	if(mVV.isSeeking()){
        		mVV.isSeeking(false);
        		//显示从头播放提示布局
        		if(null != mUIHandler && mIsFirstSeek){
        			mIsFirstSeek = false;
        			mUIHandler.sendEmptyMessage(ConstData.VideoPlayUIMsg.MSG_SHOW_RESTART);
        		}
            	play();
            	if(mIsNeedShowPop){
            		mIsNeedShowPop = false;
            		showPop();
            	}
        	}
        	
        }
    };

    protected int mDuration;

    private OnPreparedListener onPreparedListener = new OnPreparedListener()
    {
        public void onPrepared(IMediaPlayerAdapter mp)
        {       
        	Log.i(TAG, "VideoPlayerActivity->onPrepared");
            mMediaPlayer = mp;
            initEvent();
            updateMenuScreen();
            mDuration = mp.getDuration();
            int position = mp.getCurrentPosition();
            // 处理来自界面点击/甩屏的请求，甩、推的时候带有seek，其他不会带有
            int seek = 0;
            String strUrl = null;
            FileInfo mbi = getCurrentMediaInfo();
            if (mbi != null)
            {
                strUrl = mbi.getPath();
            }
            //获取已存储的视频记录
            String lastPlayPath = StorageUtils.getDataFromSharedPreference(ConstData.SharedKey.LAST_VIDEO_PLAY_PATH);
            String strLastPlayPos = StorageUtils.getDataFromSharedPreference(ConstData.SharedKey.LAST_VIDEO_PLAY_POSITION);
            if(!TextUtils.isEmpty(lastPlayPath) && !TextUtils.isEmpty(strLastPlayPos)
            		&& lastPlayPath.equals(strUrl)){
            	int lastPlayPosition = Integer.parseInt(strLastPlayPos);
            	if(lastPlayPosition > 0 && lastPlayPosition < mDuration){
            		mVV.isSeeking(true);
            		sendSeekMsg(lastPlayPosition);
            			
            	}
            }else{
            	mIsFirstSeek = false;
            	StorageUtils.saveDataToSharedPreference(ConstData.SharedKey.LAST_VIDEO_PLAY_PATH, "");
            	StorageUtils.saveDataToSharedPreference(ConstData.SharedKey.LAST_VIDEO_PLAY_POSITION, "");
            
            }
            progressGone();
            isHasPrepared = true;
            // 弹出杜比信息框
            // 之前要設置信息
            showDoblyWin();
            // 重置无法播放次数为0
            playerErrorTimes = 0;

            // 当结束的时候把当前播放索引返回到broeser界面
            //passIntentForBrowser();

            // 提前创建菜单，解决菜单显示慢问题
            createPopMenu();
            //loadMenu();
            isMenuHasCreated = true;
            /** liyang DTS2013051702993 **/
            // 本地播放视频文件，设置视频循环播放，影片播放完，在将要播放下一影片时，切换全屏播放模式或音轨或字幕时，停止运行。
            isMenuNeedShow = true;
            //重置视频字幕显示
            resetSubtitleVisible();
            play();
        }
    };

    
    /**
     * 重置视频字幕显示
     */
    private void resetSubtitleVisible(){
    	setVideoSubtitleVisible(true);
    }
    
    /**
     * 设置视频字幕可见性
     * @param visible
     */
    private void setVideoSubtitleVisible(boolean visible){
    	 MediaPlayer originMediaPlayer = mMediaPlayer.getOriginMediaPlayer();
     	try{
     		Method method = MediaPlayer.class.getDeclaredMethod("setSubtitleVisible", boolean.class);
     		method.invoke(originMediaPlayer, visible);
     	}catch (Exception e){
     		Log.i(TAG, "resetSubtitleVisible->resetSubtitleVisible->exception2:" + e);
     	}
    }
    
    /**
     * 当前系统是否支持PIP模式
     * @return
     */
    private boolean isSupportPIPMode(){
        return android.os.Build.VERSION.SDK_INT >= 24;
    }
    
    /**
     * 展示杜比标志 <功能详细描述>
     * @see [类、类#方法、类#成员]
     */
    private void showDoblyWin()
    {

		boolean isDolbyEnabled = mVV.isDolbyEnabled();
		Log.d(TAG, " showDoblyWin() isDolbyEnabled======= "+isDolbyEnabled);
		if(isDolbyEnabled){
			doblyPopWin.checkHasDobly(-100);
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
    
    OnBufferingUpdateListener onBufferingUpdateListener = new OnBufferingUpdateListener()
    {

        @Override
        public boolean onBufferingUpdate(IMediaPlayerAdapter mp, int percent)
        {        	
            return false;
        }
    };

    private boolean isBuffereIng = false;

    public void setPlayMode(int playMode)
    {
    	//Log.i(TAG, "setPlayMode->stackTrace:" + android.util.Log.getStackTraceString(new Throwable()));
        mPlayStateInfo.setPlayMode(playMode);
        //mEditor.putInt(ConstData.SharedKey.VIDEO_CYCLE_PLAY_MODE, getPlayMode()).commit();
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

        return ConstData.ScreenMode.SCREEN_FULL;
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
        FileInfo mbi = new VideoInfo();
        mbi = getCurrentMediaInfo();
        if (mbi == null)
        {
            //播放器关闭，Activity销毁时，先回发stop状态，再立即解除绑定，其后不再向Sender端回发播放器的任何状态
            stop();
            mSubSurface.setVisibility(View.INVISIBLE);
            finish();
            return false;
        }
        mVV.isSeeking(false);
        String strUrl = mbi.getPath();
        if (!StringUtils.isEmpty(strUrl))
        {
            delayHidePop(1);
            mUIHandler.sendEmptyMessage(ConstData.VideoPlayUIMsg.MSG_SHOW_PROGRESS);
            // 处理来自界面点击/甩屏的请求，甩、推的时候带有seek，其他不会带有
            int seek = 0;
            Message msgVideo = Message.obtain();
            msgVideo.what = ConstData.VideoPlayUIMsg.MSG_UI_VIDEOVIEW_SETDATA;
            msgVideo.arg1 = seek;
            msgVideo.obj = strUrl;
            mUIHandler.sendMessage(msgVideo);
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
        if(mUIHandler != null)
        	mUIHandler.sendEmptyMessage(ConstData.VideoPlayUIMsg.MSG_UI_VIDEOVIEW_PLAY);
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
    
    private void sendSeekMsg(int msec, int dealyTime)
    {
        if (mSeekHandler == null)
        {
            return;
        }

        Message msg = Message.obtain();
        msg.arg1 = msec;
        msg.what = 0;
        mSeekHandler.sendMessageDelayed(msg, dealyTime);
    }

    protected void pause()
    {

        if (!mVV.isSeeking())
        {
            mUIHandler.sendEmptyMessage(ConstData.VideoPlayUIMsg.MSG_UI_VIDEOVIEW_PAUSE);
        }
    }

    /**
     * 
     * stop：停止UI更新相关操作
     * 
     * void
     * @exception
     */
    protected void stop()
    {

        Log.d(TAG, "stop is invoke  stopallsyncseek");
        /** liyang DTS2013051702993 **/
        // 本地播放视频文件，设置视频循环播放，影片播放完，在将要播放下一影片时，切换全屏播放模式或音轨或字幕时，停止运行。
        isMenuNeedShow = false;
        // 返回键时，菜单需要重新加载
        isMenuHasCreated = false;
        //视频未准备好
        isHasPrepared = false;
        // 隐藏杜比弹出框
        //sendDoblyWinMsg(false);
        mUIHandler.removeAllMsgs();
        Log.d(TAG, "mUIHandler removeAllMsgs for the IllegalStateException.");
        if (mSeekHandler != null)
        {
            mSeekHandler.removeMessages(0);
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
        // bundle.putInt("mediaType", ConstData.MediaType.VIDEO);

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
    	mUIHandler.sendEmptyMessage(ConstData.VideoPlayUIMsg.MSG_SYNC_SEEK);
    }
    

    /**
     * 中间弹出菜单
     */
    private VideoSettingDialog mPopMenu;

    public boolean menuOpened()
    {
        Log.d(TAG, "videoPlayer onMenuOpened");
        
        delayHidePop(1);

        // 弹出菜单时，暂停视频的播放。
        pause();

        // 创建菜单
        createPopMenu();
        
        Log.i(TAG, "menuOpened->isMenuNeedShow:" + isMenuNeedShow);
        

        if (mPopMenu.isShowing())
        {
            mPopMenu.hide();
        }
        else
        {
        	loadMenu();
            isMenuHasCreated = true;
            mPopMenu.replayLastSelected();
            if(mPopMenu.isCreated()){
            	mPopMenu.rebuildView();
            }
            mPopMenu.show();
            // mPopMenu.getCurrentFocus().requestFocus();
        }

        return false;
    }

    private boolean openBottomMenu()
    {
        Log.d(TAG, "videoPlayer openBottomMenu");

        //隐藏控制器
        delayHidePop(1);

        // 弹出菜单时，暂停视频的播放。
        pause();

        createBottomPopMenu();

        return true;
    }

    private void loadBottomMenu()
    {
        if (mBottomPopMenu != null)
        {
            mBottomPopMenu.add(1, BottomMenuSelectType.TIME_SEEK, R.drawable.time_seek, 1, 1, getResources().getString(R.string.time_seek));
            mBottomPopMenu.add(2, BottomMenuSelectType.PLAY_SEETING, R.drawable.menu_icon_settings, 2, 2,
                    getResources().getString(R.string.play_settings));
            if(isSupportPIPMode()){
                mBottomPopMenu.add(3, BottomMenuSelectType.PIC_TO_PIC, R.drawable.menu_icon_pip, 3, 3,
                        getResources().getString(R.string.pip));
            }
            
        }
    }

    private void createBottomPopMenu()
    {
        mCurrSelectType = null;
        if (mBottomPopMenu == null)
        {
            mBottomPopMenu = new BottomPopMenu(this);
        }
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
        if (mVV.isSeeking())
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
        if (getPlayMode() == ConstData.MediaPlayMode.MP_MODE_ALL_CYC)
        {
            menuCgy.setSelectIndex(0);
        }
        else if(getPlayMode() == ConstData.MediaPlayMode.MP_MODE_SINGLE_CYC)
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
        if (!hasAvailExtraSub()){
            Log.d(TAG, "the movie has no subtitle.");
            return;
        }

        // 加载“字幕”面板
        MenuCategory menuCgy = new MenuCategory();
        menuCgy.setCategoryName(getResources().getString(R.string.video_menu_subtitle));
        ArrayList<MenuItemImpl> itemImpls = new ArrayList<MenuItemImpl>();

        MenuItemImpl item = null;
        String str = getApplication().getString(R.string.video_hint_subtitle_id);

        //int order = 0;

        if (mVV != null)
        {
            List<SubInfo> list1 = mVV.getSubtitleList();
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
                   /* else
                    {

                        item = new MenuItemImpl(this, i, PopMenuSelectType.SUBTITLE_INNER_SET, R.drawable.video_menu_subtitile, 1, tmp.getSubid(), str + "."
                                + String.valueOf(i + 1));
                        itemImpls.add(item);
                    }*/
                   // order++;
                }
                //关闭字幕选项
                item = new MenuItemImpl(this, list1.size(), PopMenuSelectType.SUBTITLE_INNER_SET, R.drawable.video_menu_subtitile, 1, Integer.MAX_VALUE, getString(R.string.close_subtitle));
                itemImpls.add(item);
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
   /*     if (trackIndex == subId)
        {
            Log.d(TAG, "subtitle index == subId!");
            return;
        }*/
        if (isMenuNeedShow && subNum > 0)
        {
            if (mVV != null)
            {
            	MediaPlayer mediaPlayer = mMediaPlayer.getOriginMediaPlayer();
            	if(menuIndex != subNum){
            		mIsCloseSubtitle = false;
            		setVideoSubtitleVisible(true);
            		mediaPlayer.selectTrack(trackIndex);
            	}
            	else{
            		mIsCloseSubtitle = true;
            		setVideoSubtitleVisible(true);
            	}
            	subId = menuIndex;
               /* Log.d(TAG, "subtitle 1 subNum:" + subNum);
                mVV.setSubId(index);
                subId = mVV.getCurrentSudId();*/
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
            if (mVV != null)
            {
                Log.d(TAG, "subtitle 1 subNum:" + subNum);

                int ret = mVV.setSubPath(path);
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

        List<AudioInfoOfVideo> list = mVV.getAudioinfos();
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

                if (mVV != null)
                {
                    mVV.setSoundId(index);
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
    	//初始化，左声道，右声道，立体声等
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
        menuCgy.setSelectIndex(mChannelModeIndex);
        mPopMenu.addMenuCategory(menuCgy);
    }

    int channelModeCodes = DEFAULT_CHANNEL_MODE_INDEX;
    private void setChannelMode(int index)
    {
        boolean ret = false;
        if (null != mVV)
        {
            if (index >= 0 && index < mChannelModeCodes.length)
            {
                channelModeCodes = mChannelModeCodes[index];
            }
            ret = mVV.setAudioChannelMode(channelModeCodes);
            mChannelModeIndex = index;
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
    protected void loadResource(){
    	
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
    
    /**
     * 
     * getMediaType 获取播放器媒体类型 void
     * @exception
     */
    protected int getMediaType()
    {
        return ConstData.MediaType.VIDEO;
    }

    /**
     * 删除的是当前播放的视频 onDelecteDeviceId
     * 
     * @return int
     * @exception
     */
    protected int onDelecteDeviceId(String devId)
    {
        mUIHandler.sendEmptyMessage(ConstData.VideoPlayUIMsg.MSG_UI_VIDEOVIEW_STOP);
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
        	Log.i(TAG, "SeekHandler->handleMessage");
            mVV.seekTo(msg.arg1);
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
        // remove all messages
        public void removeAllMsgs()
        {
            for (int ib = ConstData.VideoPlayUIMsg.MSG_UI_VIDEOVIEW_MIN; ib < ConstData.VideoPlayUIMsg.MSG_UI_VIDEOVIEW_MAX; ib++)
            {
                this.removeMessages(ib);
            }
        }

        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case ConstData.VideoPlayUIMsg.MSG_UI_PROCESSBAR:
                    if (mVV == null)
                    {
                        return;
                    }

                    mCircleProgressBar.setVisibility(msg.arg1);
                    break;

                case ConstData.VideoPlayUIMsg.MSG_UI_VIDEOVIEW_SETDATA:
                    Log.d(TAG, "MSG_UI_VIDEOVIEW_SETDATA");
                    timeOutBegin = 0;
                    if (mVV == null)
                    {
                        return;
                    }
                    String strurl = (String) (msg.obj);
                    Log.d(TAG, "MSG_UI_VIDEOVIEW_SETDATA url = " + strurl);
//                    progressGone();
                    if (StringUtils.isNotEmpty(strurl))
                    {
                        // 隐藏杜比的信息窗口
                        sendDoblyWinMsg(false);

//                        mUIHandler.sendEmptyMessage(MSG_SHOW_PROGRESS);

                        /** Mender:l00174030;Reason:videoview drop the special character #,so replace it. **/
					    
                        Uri uri;
						File file = new File(strurl);
						if(file.exists()){
							uri = Uri.fromFile(file);
						}else{
							uri = Uri.parse(Uri.encode(strurl));
						}
						if(mCurrDevice.getDeviceType() == ConstData.DeviceType.DEVICE_TYPE_DMS || 
								mCurrDevice.getDeviceType() == ConstData.DeviceType.DEVICE_TYPE_OTHER)
							uri = Uri.parse(strurl);
                        // mVV.setVideoPath(strurl);
                        Log.e(TAG, "要播放的视频URL为 ：" + String.valueOf(uri));
                        //尝试停止当前播放
                        try{
                        	 mVV.stopPlayback();
                        }catch (Exception e){
                        	Log.i(TAG, "UIHandler->MSG_UI_VIDEOVIEW_SETDATA->stopPlayback->exception:" + e);
                        }
                       
                        mVV.setVideoURI(uri);
                        soundId = 0;
                        subId = 0;
                        mChannelModeIndex = 0;
                        // zkf61715 seekTo为假异步，需要在子线程中调用避免ANR
                        // mVV.seekTo(msg.arg1);
                        //sendSeekMsg(msg.arg1);
                    }
                    else
                    {
                        progressGone();
                    }
                    // 播放下一个视频时，初始显示等比缩放
                    //setScreenMode(ConstData.ScreenMode.SCREEN_SCALE);
                    // 播放另外的视频时菜单需要消失重置
                    if (mPopMenu != null && mPopMenu.isShowing())
                    {
                        mPopMenu.hide();
                    }

                    videoHeight = 0;
                    videoWidth = 0;

                    break;
                case  ConstData.VideoPlayUIMsg.MSG_UI_VIDEOVIEW_STOP:

                    Log.d(TAG, "MSG_UI_VIDEOVIEW_STOP");


                    /** liyang DTS2013051702993 **/
                    // 本地播放视频文件，设置视频循环播放，影片播放完，在将要播放下一影片时，切换全屏播放模式或音轨或字幕时，停止运行。
                    isMenuNeedShow = false;
                    // 返回键时，菜单需要重新加载
                    isMenuHasCreated = false;

                    if (mVV != null)
                    {
                        mVV.stopPlayback();
                    }

                    break;

                case ConstData.VideoPlayUIMsg.MSG_UI_VIDEOVIEW_PAUSE:
                    Log.e(TAG, "MSG_UI_VIDEOVIEW_PAUSE");
                    Log.d(TAG, " play----> pause");
                    mUIHandler.removeMessages(ConstData.VideoPlayUIMsg.MSG_UI_VIDEOVIEW_PLAY);
                    mVV.pause();
                    removeMessages(ConstData.VideoPlayUIMsg.MSG_PROGRESS_CHANGED);
                    //调整状态图
                    mSeekBarLayout.setPlayStatus(ConstData.VIDEO_PLAY_STATUS.PAUSED);
                    int tempPos = mVV.getCurrentPosition();
                    Log.d(TAG, "MSG_UI_VIDEOVIEW_PAUSE ---" + tempPos);
                    if (mSeekBarLayout.isFocusable() && !mVV.isSeeking())
                    {
                        Log.d("MSG_UI_VIDEOVIEW_PAUSE", "----seek");

                    }
                    break;
                case ConstData.VideoPlayUIMsg.MSG_UI_VIDEOVIEW_MCSPLAY:
                    Log.d(TAG, "MSG_UI_VIDEOVIEW_MCSPLAY");
                    if (mCircleProgressBar.getVisibility() == View.GONE)
                    {
                        mUIHandler.sendEmptyMessageDelayed(ConstData.VideoPlayUIMsg.MSG_HIDE_ACCELERATION, 3000);
                    }
                    mUIHandler.sendEmptyMessage(ConstData.VideoPlayUIMsg.MSG_UI_VIDEOVIEW_PLAY);
                    break;
                case ConstData.VideoPlayUIMsg.MSG_UI_VIDEOVIEW_PLAY:
                    if (mVV != null)
                    {
                        Log.d("dddd", "MSG_UI_VIDEOVIEW_PLAY" + mVV.isSeeking());
                    }
                    if (mVV == null || mVV.isSeeking())
                    {
                        return;
                    }
                    if (!mVV.isPlaying())
                    {
                        mUIHandler.removeMessages(ConstData.VideoPlayUIMsg.MSG_UI_VIDEOVIEW_PAUSE);
                        mVV.setOnCompletionListener(onCompletionListener);
                        //再次隐藏加载栏
                        //progressGone();
                        Log.i("ProgressBar_Debug", "mVV.start()");
                        mVV.start();
                        mSeekBarLayout.setPlayStatus(ConstData.VIDEO_PLAY_STATUS.PLAYING);
                        //这里设置字幕是否显示
                        //Log.i(TAG, "mVV.start()->stackTrace:" + android.util.Log.getStackTraceString(new Throwable()));
                        /**
                         * Mender:l00174030;Reason:when you pause & play, there is some wrong with the original model, set the video size to skip it.
                         **/
                        //pauseToPlay();
                        removeMessages(ConstData.VideoPlayUIMsg.MSG_PROGRESS_CHANGED);
                        sendEmptyMessage(ConstData.VideoPlayUIMsg.MSG_PROGRESS_CHANGED);

                        /** Mender:l00174030;Reason:pause a long time,then play,the progress bar can't disappear. **/
                        if (null != mUIHandler)
                        {
                            mUIHandler.removeMessages(ConstData.VideoPlayUIMsg.MSG_HIDE_CONTROLER);
                            mUIHandler.sendEmptyMessageDelayed(ConstData.VideoPlayUIMsg.MSG_HIDE_CONTROLER, 5000);
                        }
                        Log.d(TAG, "video play" + mVV.getCurrentPosition() + "state==");

                    }else{
                    	 mSeekBarLayout.setPlayStatus(ConstData.VIDEO_PLAY_STATUS.PLAYING);
                    }
                    break;

                case ConstData.VideoPlayUIMsg.MSG_UI_VIDEOVIEW_REVERSE_STATE:

                    Log.e(TAG, "MSG_UI_VIDEOVIEW_REVERSE_STATE" + "state---");
                    if (mVV.isPlaying())
                    {
                        sendEmptyMessage(ConstData.VideoPlayUIMsg.MSG_UI_VIDEOVIEW_PAUSE);

                        bIsPausedByUser = true;
                    }
                    else
                    {
                        Log.d(TAG, "pause ---->play  is start");
                        sendEmptyMessage(ConstData.VideoPlayUIMsg.MSG_UI_VIDEOVIEW_PLAY);
                    }

                    break;
                case ConstData.VideoPlayUIMsg.MSG_UI_VIDEOVIEW_SEEK_TO:
                    Log.d(TAG, "MSG_UI_VIDEOVIEW_SEEK_TO");
                    removeMessages(ConstData.VideoPlayUIMsg.MSG_HIDE_CONTROLER);
                    mUIHandler.sendEmptyMessageDelayed(ConstData.VideoPlayUIMsg.MSG_HIDE_CONTROLER, 5000);
                    if (mVV == null)
                    {
                        return;
                    }
                    sendSeekMsg(msg.arg1);
                    Log.d(TAG, "MSG_UI_VIDEOVIEW_SEEK_TO-------->play");

                    mUIHandler.sendEmptyMessage(ConstData.VideoPlayUIMsg.MSG_UI_VIDEOVIEW_PLAY);
                    break;

                case ConstData.VideoPlayUIMsg.MSG_UI_VIDEOVIEW_SAVE_POS:

                    Log.d(TAG, "---->MSG_UI_VIDEOVIEW_SAVE_POS  invoke");

                    FileInfo mbi = getCurrentMediaInfo();

                    if (mbi != null)
                    {
                        int videoPhoneCurrentPos = 0;

                        if (mVV != null)
                        {
                            videoPhoneCurrentPos = mVV.getCurrentPosition();
                        }
                        //mbi.setmSeekTo(videoPhoneCurrentPos);
                        // 保存到历史列表 l00174030
                        String strUrl = mbi.getPath();
                        if (strUrl != null)
                        {
                            Log.i(TAG, "save to history URL = " + strUrl + " Pos = " + videoPhoneCurrentPos);
                            HistoryListRecord.getInstance().put(strUrl, videoPhoneCurrentPos);
                        }

                    }
                    break;
                case ConstData.VideoPlayUIMsg.MSG_PROGRESS_CHANGED:
                    Log.d(TAG, "isbHide==" + isbHide());
                    if (isbHide())
                    {
                        break;
                    }
                    mSeekBarLayout.setPosition(mVV.getCurrentPosition(), mSeekPosition, mDuration, false);
                    //Log.d(TAG, "---->ttttt " + mVV.isSeeking());
                    sendEmptyMessageDelayed(ConstData.VideoPlayUIMsg.MSG_PROGRESS_CHANGED, 500);
                    break;

                case ConstData.VideoPlayUIMsg.MSG_HIDE_CONTROLER:
                	mSeekBarLayout.setVisibility(View.INVISIBLE);
            		tiemLayout.setVisibility(View.INVISIBLE);
            		if(null != mUIHandler)
            			mUIHandler.removeMessages(ConstData.VideoPlayUIMsg.MSG_PROGRESS_CHANGED);
                    break;

                case ConstData.VideoPlayUIMsg.MSG_SHOW_CONTROLER:
                    mSeekBarLayout.setVisibility(View.VISIBLE);
                    /** 修改者：l00174030；修改原因：时间控件和控制条一起显示 **/
                    // 显示
                    tiemLayout.setVisibility(View.VISIBLE);
                    mSeekBarLayout.setPosition(mVV.getCurrentPosition(), mSeekPosition, mDuration, false);
                    setbHide(false);
                    removeMessages(ConstData.VideoPlayUIMsg.MSG_PROGRESS_CHANGED);
                    sendEmptyMessageDelayed(ConstData.VideoPlayUIMsg.MSG_PROGRESS_CHANGED, 200);
                    break;

                case ConstData.VideoPlayUIMsg.MSG_SYNC_SEEK_POS:

                    if (!bSyncSeek)
                    {
                        break;
                    }

                    if (mVV.isPlaying())
                    {

                        int tmpPos = mVV.getCurrentPosition();
                        if (tmpPos != oldPos)
                        {
                            String url = getCurMediaUrl();
                            if (url == null)
                            {
                                return;
                            }
                            oldPos = tmpPos;
                        }
                    }

                    // 每隔1s同步一次seek位置
                    sendEmptyMessageDelayed(ConstData.VideoPlayUIMsg.MSG_SYNC_SEEK_POS, 1000);

                    break;

                case ConstData.VideoPlayUIMsg.MSG_SYNC_SEEK:
                    bSyncSeek = true;
                    sendEmptyMessage(ConstData.VideoPlayUIMsg.MSG_SYNC_SEEK_POS);

                    break;

                case ConstData.VideoPlayUIMsg.MSG_STOP_SYNC_SEEK:
                    bSyncSeek = false;
                    break;

                case ConstData.VideoPlayUIMsg.MSG_HIDE_HINT:
                    break;
                case ConstData.VideoPlayUIMsg.MSG_HIDE_ACCELERATION:  
                    Log.d(TAG, "MSG_HIDE_ACCELERATION is finish ");
                    break;
                case ConstData.VideoPlayUIMsg.MSG_SHOW_PROGRESS:
                    setProgressBar(View.VISIBLE);
                    break;
                case ConstData.VideoPlayUIMsg.MSG_HIDE_PROGRESS:

                    setProgressBar(View.GONE);
                    break;

                case ConstData.VideoPlayUIMsg.MSG_MCS_PLAY:
                    Log.d(TAG, "MSG_MCS_PLAY");
                    break;
                case ConstData.VideoPlayUIMsg.MSG_MCS_HIDEMODE:
                    /** 添加杜比信息的弹窗处理 **/
                case  ConstData.VideoPlayUIMsg.MSG_DOBLY_SHOW:
                    doblyPopWin.showDoblyWin();
                    break;
                case  ConstData.VideoPlayUIMsg.MSG_DOBLY_HIDE:
                    doblyPopWin.hideDoblyWin();
                    break;
                case ConstData.VideoPlayUIMsg.MSG_CLOSE_ERROR_DIALOG:
                	boolean isNextPlay = msg.getData().getBoolean(KEY_NEXT_PLAY);
                	if(isNextPlay){
    					if(mErrorTipDialog.isShowing())
    						mErrorTipDialog.dismiss();
    					if(mExtraVideoUri != null)
    						finishPlay();
    		             setMediaData();
    		             play();
    				}else{
    					if(mErrorTipDialog.isShowing())
    						mErrorTipDialog.dismiss();
    					finishPlay();
    				}
                	break;
                case ConstData.VideoPlayUIMsg.MSG_FAST_GO:
                	changePlayPosition(1);
                	mUIHandler.sendEmptyMessageDelayed(ConstData.VideoPlayUIMsg.MSG_FAST_GO, 10);
                	break;
                case ConstData.VideoPlayUIMsg.MSG_FAST_BACK:
                	changePlayPosition(-1);
                	mUIHandler.sendEmptyMessageDelayed(ConstData.VideoPlayUIMsg.MSG_FAST_BACK, 10);
                	break;
                case ConstData.VideoPlayUIMsg.MSG_SHOW_RESTART:
                	mTextRestartPlay.setVisibility(View.VISIBLE);
                	mUIHandler.sendEmptyMessageDelayed(ConstData.VideoPlayUIMsg.MSG_HIDE_RESTART, 3000);
                	break;
                case ConstData.VideoPlayUIMsg.MSG_HIDE_RESTART:
                	mTextRestartPlay.setVisibility(View.INVISIBLE);
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
            //Log.d(TAG, "--->setbHide==" + bHide);
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

        
    }

    private void delayHideHintMsg()
    {
    	mUIHandler.removeMessages(ConstData.VideoPlayUIMsg.MSG_HIDE_HINT);
        mUIHandler.sendEmptyMessageDelayed(ConstData.VideoPlayUIMsg.MSG_HIDE_HINT, DELAY_TIME);
    }

    private void hideHintMsg()
    {
    	 mUIHandler.removeMessages(ConstData.VideoPlayUIMsg.MSG_HIDE_HINT);
         mUIHandler.sendEmptyMessageDelayed(ConstData.VideoPlayUIMsg.MSG_HIDE_HINT, 100);
    }

    private boolean hasAvailExtraSub()
    {
        if (mVV != null)
        {
            subNum = mVV.getSubtitleList().size();
        }
        else
        {
            subNum = 0;
        }
        return (subNum > 0);
    }

    private boolean hasSound()
    {
        if (mVV != null)
        {
            soundNum = mVV.getAudioinfos().size();
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
    	Log.i(TAG, "updateMenuScreen->screenMode:" + getScreenMode());
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
        
        double widthScale = mScreenWidth * 1.0 / width;
        double heightScale = mScreenHeight * 1.0 / height;
        //等比长度
        int scaleWidth = width;
        //等比高度
        int scaleHeight = height;
        if (widthScale > heightScale)
        {
            scaleWidth = (int) (heightScale * width);
            scaleHeight = mScreenHeight;
        }
        else
        {
            scaleWidth = mScreenWidth;
            scaleHeight = (int) (widthScale * height);
        }
        switch (getScreenMode())
        {
            case ConstData.ScreenMode.SCREEN_FULL:
                width = mScreenWidth;
                height = mScreenHeight;
                break;
            case ConstData.ScreenMode.SCREEN_ORIGINAL:
                Log.i(TAG, "screenMode: SCREEN ORIGINAL");
                if(width > mScreenWidth || height > mScreenHeight){
                    //此时等比拉伸视频
                    width = scaleWidth;
                    height = scaleHeight;
                }
                break;
            case ConstData.ScreenMode.SCREEN_SCALE:
                width = scaleWidth;
                height = scaleHeight;
                break;
            default:
                break;
        }
        
        
        
        int l = (mScreenWidth - width) / 2;
        int t = (mScreenHeight - height) / 2;
        Log.d(TAG, "l,t,w,h:" + l + " " + t + " " + width + " " + height);
        mVV.setOutRange(l, t, width, height);
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
        if (height >= mScreenHeight || width >= mScreenWidth)
        {
            Log.d(TAG, "video is larger then screen, width = " + width + " height = " + height);
            return false;
        }

        return true;
    }

    // 显示屏幕的分辨率,这里动态获取，用于兼容不同的设备
    private int mScreenWidth = DeviceInfoUtils.getScreenWidth(CommonValues.application);

    private int mScreenHeight;

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

        if (getScreenMode() == ConstData.ScreenMode.SCREEN_ORIGINAL)
        {
            mVV.setScreenScale(videoWidth, videoHeight);
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
    }

    /**
     * 同步保存进度
     * 
     * @see [类、类#方法、类#成员]
     */
    private void savePositionNow()
    {
        Log.d(TAG, "---->MSG_UI_VIDEOVIEW_SAVE_POS  invoke");

        FileInfo mbi = getCurrentMediaInfo();

        if (mbi != null)
        {
            int videoPhoneCurrentPos = 0;

            Log.d(TAG, "---->MSG_UI_VIDEOVIEW_SAVE_POS  invoke mVV= " + mVV);
            if (mVV != null)
            {
                videoPhoneCurrentPos = mVV.getCurrentPosition();
            }
            //mbi.setmSeekTo(videoPhoneCurrentPos);
            // 保存到历史列表 l00174030
            String strUrl = mbi.getPath();
            if (strUrl != null)
            {
                Log.i(TAG, "save to history URL = " + strUrl + " Pos = " + videoPhoneCurrentPos);
                StorageUtils.saveDataToSharedPreference(ConstData.SharedKey.LAST_VIDEO_PLAY_PATH, strUrl);
                StorageUtils.saveDataToSharedPreference(ConstData.SharedKey.LAST_VIDEO_PLAY_POSITION, String.valueOf(videoPhoneCurrentPos));
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

        if (mVV == null)
        {
            return;
        }

        // always show the pop windows if it is pause
        Log.d(TAG, " play----> pause");
        mUIHandler.removeMessages(ConstData.VideoPlayUIMsg.MSG_UI_VIDEOVIEW_PLAY);
        mVV.pause();
        mUIHandler.removeMessages(ConstData.VideoPlayUIMsg.MSG_PROGRESS_CHANGED);
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
        if (mVV != null)
        {
            mVV.isSeeking(true);
            Log.d(TAG, "------seekToNow() --" + mVV.isSeeking());
        }

        // 同步seek
        Log.d(TAG, "MSG_UI_VIDEOVIEW_SEEK_TO");
        // removeMessages(MSG_SHOW_CONTROLER);
        mUIHandler.removeMessages(ConstData.VideoPlayUIMsg.MSG_HIDE_CONTROLER);
        mUIHandler.sendEmptyMessageDelayed(ConstData.VideoPlayUIMsg.MSG_HIDE_CONTROLER, 5000);
        if (mVV == null)
        {
            return;
        }
        sendSeekMsg(msec);
        Log.d(TAG, "seekToNow MSG_UI_VIDEOVIEW_SEEK_TO-------->play");

        mUIHandler.removeMessages(ConstData.VideoPlayUIMsg.MSG_PROGRESS_CHANGED);
        mUIHandler.sendEmptyMessageDelayed(ConstData.VideoPlayUIMsg.MSG_PROGRESS_CHANGED, 500);
        String url = getCurMediaUrl();
        if (url == null)
        {
            return;
        }

        // 媒体中心模式时保存进度 l00174030
        if (url != null)
        {
        	 HistoryListRecord.getInstance().put(url, msec);
        }
    }



    private FileInfo getItemMediaInfo(int index)
    {
        if (mPlayStateInfo == null)
        {
            return null;
        }

        FileInfo mbi = mPlayStateInfo.getIndexMediaInfo(index);
        if (mbi == null)
        {
            return null;
        }

        String strUrl = mbi.getPath();

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
        if (isShow)
        {
            // 显示Dobly的信息视窗
            mUIHandler.sendEmptyMessage(ConstData.VideoPlayUIMsg.MSG_DOBLY_SHOW);
        }
        else
        {
            // 隐藏Dobly的信息视窗
            mUIHandler.sendEmptyMessage(ConstData.VideoPlayUIMsg.MSG_DOBLY_HIDE);
        }
    
    }

    private static final String BD_PREFIX = "bluray:";

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
                mTimeSeekDialog.show(myvideo, mVV.getCurrentPosition(), mVV.getDuration());
            }
        }
        else if (mCurrSelectType == BottomMenuSelectType.PLAY_SEETING)
        {
            mBottomPopMenu.dismiss();
            menuOpened();
        }
        else if(mCurrSelectType == BottomMenuSelectType.PIC_TO_PIC){
            mBottomPopMenu.dismiss();
            //enterPictureInPictureMode();
        }

        else
        {
            /** 播放模式 **/
            if (mCurrSelectType == PopMenuSelectType.REPEAT_LIST)
            {
                Log.d(TAG, "change to play loop.");
                setPlayMode(ConstData.MediaPlayMode.MP_MODE_ALL_CYC);
                saveCycPlayMode();
            }
            else if (mCurrSelectType == PopMenuSelectType.REPEAT_ONE)
            {
                Log.d(TAG, "change to repeat one.");
                setPlayMode(ConstData.MediaPlayMode.MP_MODE_SINGLE_CYC);
                saveCycPlayMode();
            }
            else if(mCurrSelectType == PopMenuSelectType.SINGLE_PLAY){
            	 Log.d(TAG, "change to single play.");
                 setPlayMode(ConstData.MediaPlayMode.MP_MODE_SINGLE);
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
                setScreenM(ConstData.ScreenMode.SCREEN_FULL);
                saveScreenDisplay();
            }
            else if (mCurrSelectType == PopMenuSelectType.SCREEN_DISPLAY_ORIGINAL)
            {
                Log.d(TAG, "set the screen original");
                setScreenM(ConstData.ScreenMode.SCREEN_ORIGINAL);
                saveScreenDisplay();
            }
            else if (mCurrSelectType == PopMenuSelectType.SCREEN_DISPLAY_SCALE)
            {
                Log.d(TAG, "set the screen scale");
                setScreenM(ConstData.ScreenMode.SCREEN_SCALE);
                saveScreenDisplay();
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
        	Log.i(TAG, "OnTimeSeekListener->onTImeSeeked:" + DateUtil.getMediaTime(time));
        	sendSeekMsg(time);
        }

        @Override
        public void onDismiss()
        {
        	Log.i(TAG, "OnTimeSeekListener->onDimiss:");
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
       // mGetAllFlatFolders = manager.getAllFlatAVIFolders(ConstData.MediaType.SUBTITLE, 0, 100);
    }

    public enum BottomMenuSelectType
    {
        TIME_SEEK, // 底部菜单定位时间功能
        PLAY_SEETING, // 底部菜单播放设置功能
        PIC_TO_PIC // 画中画功能
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

    //默认列表循环播放
    private static final int DEFAULT_CYCLE_PLAY_MODE_INDEX = ConstData.MediaPlayMode.MP_MODE_ALL_CYC;
    //默认等比缩放
    private static final int DEFAULT_SCREEN_DISPLAYE_MODE_INDEX = ConstData.ScreenMode.SCREEN_SCALE;

    private static final int DEFAULT_CHANNEL_MODE_INDEX = 0; // 0 对应的是环绕立体声

    private void initData()
    {
        mPreferences = getSharedPreferences(VIDEO_PLAY_SET, Context.MODE_PRIVATE);
        mEditor = mPreferences.edit();
        //首次启动Activity，初始化配置
        if (mPreferences.getBoolean(ConstData.SharedKey.FIRST_START_VIDEOPLAY, true))
        {
            Log.w(TAG, "================== init Video Play Preferences =======================");
            mEditor.putInt(ConstData.SharedKey.VIDEO_CYCLE_PLAY_MODE, DEFAULT_CYCLE_PLAY_MODE_INDEX);
            mEditor.putInt(ConstData.SharedKey.VIDEO_SCREEN_DISPLAY_MODE, DEFAULT_SCREEN_DISPLAYE_MODE_INDEX);
            mEditor.putInt(ConstData.SharedKey.VIDEO_CHANNEL_MODE, DEFAULT_CHANNEL_MODE_INDEX);
            mEditor.putBoolean(ConstData.SharedKey.FIRST_START_VIDEOPLAY, false);
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
            mEditor.putInt(ConstData.SharedKey.VIDEO_CYCLE_PLAY_MODE, getPlayMode());
            mEditor.commit();
            Log.d(TAG, "saveCycPlayMode OK");
        }
    }

    private void saveScreenDisplay()
    {
        Log.d(TAG, "saveScreenDisplay E getScreenMode() =" + getScreenMode());
        if (mEditor != null)
        {
            mEditor.putInt(ConstData.SharedKey.VIDEO_SCREEN_DISPLAY_MODE, getScreenMode());
            mEditor.commit();
            Log.d(TAG, "saveScreenDisplay OK");
        }
    }

    private void saveChannelMode()
    {
        Log.d(TAG, "saveChannelMode E  channelModeCodes =" + channelModeCodes);
        if (mEditor != null)
        {
            mEditor.putInt(ConstData.SharedKey.VIDEO_CHANNEL_MODE, channelModeCodes);
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
		
        //视频未准备好
        isHasPrepared = false;
        //播放完成，重置当前Seek位置
        mSeekPosition = 0;
        
        //隐藏控制栏
        if(mSeekBarLayout.getVisibility() == View.VISIBLE){
        	if(null != mUIHandler){
        		mUIHandler.removeMessages(ConstData.VideoPlayUIMsg.MSG_SHOW_CONTROLER);
        		mUIHandler.removeMessages(ConstData.VideoPlayUIMsg.MSG_HIDE_CONTROLER);
        	}
        	mSeekBarLayout.setVisibility(View.INVISIBLE);
        }
        
        //重置视频播放记录
        StorageUtils.saveDataToSharedPreference(ConstData.SharedKey.LAST_VIDEO_PLAY_PATH, "");
        StorageUtils.saveDataToSharedPreference(ConstData.SharedKey.LAST_VIDEO_PLAY_POSITION, "");
        
        //隐藏控制栏
        if (mPopMenu != null && mPopMenu.isShowing())
        {
            mPopMenu.hide();
        }
        progressGone();
        String url = getCurMediaUrl();
        if (url == null)
        {
            Log.d(TAG, "OnCompletionListener -- onCompletion mbi == null --");

        }
        Log.d(TAG, "======playMode2:" + getPlayMode());

        // 单文件播放模式
        if (getPlayMode() == ConstData.MediaPlayMode.MP_MODE_SINGLE)
        {
            ToastUtils.showToast(getString(R.string.video_program_completion));
            // 播放器关闭，Activity销毁时，先回发stop状态，再立即解除绑定，其后不再向Sender端回发播放器的任何状态

            stop();

            mSubSurface.setVisibility(View.INVISIBLE);
            finish();
            return;
        }

        // 单文件循环，全体循环播放模式
        Log.d(TAG, "onCompletion() -- getNextMediaInfo() --");

        FileInfo mbi = getNextMediaInfo();

        if (mbi == null)
        {
            Log.d(TAG, "onCompletion() -- Can not Find the NextMediaInfo --");
            
            ToastUtils.showToast(getString(R.string.video_program_completion));
            
            stop();

            mSubSurface.setVisibility(View.INVISIBLE);
            finish();
        }
        else
        {
            Log.d(TAG, "onCompletion() -- Find the NextMediaInfo --");


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
    	if(mVV != null)
    		return mVV.getCurrentPosition();
    	return -1;
    }
    
    /**
     * @author fly.gao
     * 返回当前视频的长度
     * @return
     */
    public int getDuration(){
    	if(mVV != null)
    		return mVV.getDuration();
    	return -1;
    }

	@Override
	public void onServiceConnected() {
		
	}

	@Override
	public int getLayoutRes() {
		return R.layout.video_video_fullscreen;
	}

	@Override
	public void init() {
		Log.i(TAG, "onCreate->screenWidth:" + mScreenWidth);
		Log.i(TAG, "onCreate->screenHeight:" + mScreenHeight);
		Resources resources = getResources();
		int navigationBarHeight = 0;
		int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
		if (resourceId > 0) {
			navigationBarHeight = resources.getDimensionPixelSize(resourceId);
			Log.i(TAG, "onCreate->navigationBarHeight:" + navigationBarHeight);
		}
		mScreenHeight = DeviceInfoUtils.getScreenHeight(CommonValues.application) + navigationBarHeight;
		if (mExtraVideoUri != null) {
			mCurrDevice = new Device();
			mCurrDevice.setDeviceType(ConstData.DeviceType.DEVICE_TYPE_OTHER);
		}
		initData();
		initViews();
		// 初始化杜比的弹出视窗
		doblyPopWin = new DoblyPopWin(this);
		/* BEGIN: Added by r00178559 for AR-0000698413 2014/02/13 */
		loadChannelModeResources();
		/* END: Added by r00178559 for AR-0000698413 2014/02/13 */
		//默认全体循环
		setPlayMode(mPreferences.getInt(ConstData.SharedKey.VIDEO_CYCLE_PLAY_MODE, DEFAULT_CYCLE_PLAY_MODE_INDEX));
		//默认等比缩放
		setScreenMode(mPreferences.getInt(ConstData.SharedKey.VIDEO_SCREEN_DISPLAY_MODE, DEFAULT_SCREEN_DISPLAYE_MODE_INDEX));
		if (mSeekHandlerThread == null) {
			mSeekHandlerThread = new HandlerThread(SEEK_THREAD_TAG);
			mSeekHandlerThread.start();
		}

		if (mSeekHandler == null) {
			mSeekHandler = new SeekHandler(mSeekHandlerThread.getLooper());
		}

		bIsPausedByUser = false;

	}
	
	public void updateCurrPlayPosition(){
		 int currPosition = mVV.getCurrentPosition();
        int duration = mVV.getDuration();
        mSeekBarLayout.setCurrPlayPosition(currPosition, duration);
        mSeekBarLayout.setTotalDuration(duration);
        //mSeekBarLayout.setPosition(currPosition, mSeekPosition, duration);
	}
	
	/**
	 * 调整播放位置
	 */
	public void changePlayPosition(int type){
		int currPosition = mVV.getCurrentPosition();
        int duration = mVV.getDuration();
        if(mIsFirstBackOrGo){
        	mIsFirstBackOrGo = false;
        	mSeekPosition = currPosition;
        }
		switch (type) {
		case 1:
			//快进
			mSeekBarLayout.setPlayStatus(ConstData.VIDEO_PLAY_STATUS.FAST_GO);
			//Log.i(TAG, "change play step:" + (mFastGoCount << 1));
			mSeekPosition += ((mFastGoCount >> 1) * 1000 + CHANGE_PLAY_TIME_STEP);
			if(mSeekPosition > duration)
				mSeekPosition = duration;
			break;
		case -1:
			//快退
			mSeekBarLayout.setPlayStatus(ConstData.VIDEO_PLAY_STATUS.FAST_BACK);
			mSeekPosition -= ((mFastBackCount >> 1) * 1000 + CHANGE_PLAY_TIME_STEP);
			if(mSeekPosition < 0)
				mSeekPosition = 0;
			break;
		default:
			break;
		}
		mSeekBarLayout.setPosition(currPosition, mSeekPosition, duration, true);
	}
	
	/**
	 * 隐藏从头开始播放提示
	 */
	private void hideRestartPlayTip(){
		if(mTextRestartPlay.getVisibility() == View.VISIBLE)
    		mTextRestartPlay.setVisibility(View.INVISIBLE);
	}
}