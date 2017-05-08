/**
 * 图片播放模块
 * @author GaoFei
 */
package com.rockchips.mediacenter.imageplayer;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import momo.cn.edu.fjnu.androidutils.utils.JsonUtils;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.input.InputManager;
import android.media.AudioManager;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import com.rockchips.mediacenter.R;
import com.rockchips.mediacenter.bean.FileInfo;
import com.rockchips.mediacenter.bean.LocalMediaInfo;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.utils.DateUtil;
import com.rockchips.mediacenter.utils.IICLOG;
import com.rockchips.mediacenter.utils.PlatformUtil;
import com.rockchips.mediacenter.utils.PlatformUtils;
import com.rockchips.mediacenter.utils.SharedUtils;
import com.rockchips.mediacenter.utils.StringUtils;
import com.rockchips.mediacenter.utils.GifOpenHelper;
import com.rockchips.mediacenter.view.GifView;
import com.rockchips.mediacenter.service.OnGifListener;
import com.rockchips.mediacenter.service.IMediaPlayerAdapter;
import com.rockchips.mediacenter.service.IVideoViewAdapter;
import com.rockchips.mediacenter.service.OnCompleteListener;
import com.rockchips.mediacenter.service.OnErrorListener;
import com.rockchips.mediacenter.service.OnPreparedListener;
import com.rockchips.mediacenter.view.OrigVideoViewNoView;
import com.rockchips.mediacenter.utils.ResLoadUtil;
import com.rockchips.mediacenter.imageplayer.DLNAImageSwitcher.DLNAImageSwitcherListener;
import com.rockchips.mediacenter.imageplayer.image.CurlDownload;
import com.rockchips.mediacenter.imageplayer.image.ImageUtils;
import com.rockchips.mediacenter.imageplayer.image.UriTexture;
import com.rockchips.mediacenter.utils.GetDateUtil;
import com.rockchips.mediacenter.view.BackMusicDialog;
import com.rockchips.mediacenter.view.ImageSettingsDialog;
import com.rockchips.mediacenter.view.BottomPopMenu;
import com.rockchips.mediacenter.view.MenuCategory;
import com.rockchips.mediacenter.view.MenuItemImpl;
import com.rockchips.mediacenter.view.OnSelectTypeListener;

/**
 * ImagePlayerActivity
 * 
 * 2011-11-1 下午08:23:45
 * 
 * @version 1.0.0
 * 
 */
public class ImagePlayerActivity extends PlayerBaseActivity implements DLNAImageSwitcherListener,
    OnGestureListener, OnTouchListener
{
    private static final String TAG = "ImagePlayerActivity";
    
    private IICLOG mLog = IICLOG.getInstance();
    
    private static Context mContext;
    
    /**
     * 显示名字
     */
    protected static final int SHOW_MSG_SHOWNAME = 1;
    
    /**
     * 音频文件错误不能播放
     */
    protected static final int SHOW_MSG_ERRORMUSIC = 2;
    
    /**
     * 连续音频文件错误，退出背景音乐播放
     */
    protected static final int SHOW_MSG_ERROR = 3;
    
    public static final int [] rejectkey = {116};
    
    private DLNAImageSwitcher mImageSwitcher = null;
    
    private LayoutParams mLayoutParams;
    
    //该控件负责播放gif图
    private GifView mGifView = null;
    
    private boolean mbAutoPlay = true;
    
    private View mLeftNavigation;
    
    private View mRightNavigation;
    
    private RelativeLayout mPicPlayerDetailLayout;
    
    /** 布局文件最外层 */
    private RelativeLayout mContainer;
    
    private TextView mPicTitle;
    
    /**
     *	图片缓存目录 
     */
    private String mCachePath;
    
    /**
     * 尺寸
     */
    private TextView mImagesize;
	
    /**
     * 序号
     */
    private TextView mImageOrder;
	
    /**
     * 摄影时间
     */
    private TextView mImageTime;
    
    /**
     * 照片详情
     */
    private LinearLayout mImageDetailInfo;
    
    private LinearLayout mDisplayException;
    
    /**
     * 加载图片提示错误信息
     */
    private TextView mImageErrorInfo;
    
    private GestureDetector mDetector;
    
    /**
     *	标志当前播放界面是否已经被其他界面覆盖 
     */
    private boolean mbCovered = false;
    
    /**
     * 显示位置
     */
    private TextView mPicPos;
    
    /**
     * 播放间隔 默认为5秒
     */
    private int mAutoPlayInterval = 5000;
    
    /**
     * 播放间隔 8S
     */
    private static final int AUTO_PLAY_INTERVAL_E = 8000;
    
    /**
     * 播放间隔 5S
     */
    private static final int AUTO_PLAY_INTERVAL_F = 5000;
    
    /**
     * 播放间隔 3S
     */
    private static final int AUTO_PLAY_INTERVAL_T = 3000;
    
    /**
     * 播放设置SharedPreferences标识
     */
    private static final String IMAGE_PLAY_SET = "IMAGE_PLAY_SET";
    
    /**
     * SharedPreferences --切换方式
     */
    private static final String SWITCH_WITH = "IMAGE_PALY_SET_SWITCH_WITH";
    
    /**
     * SharedPreferences --切换时间
     */
    private static final String SWITCH_TIME = "IMAGE_PALY_SET_SWITCH_TIME";
    
    /**
     * SharedPreferences --背景音乐
     */
    private static final String BG_MUSIC = "IMAGE_PALY_SET_BG_MUSIC";
    private static final String PERFS_DEVICE_ID = "PERFS_DEVICE_ID";
    private static final String PERFS_BG_AUDIO_URLS = "PERFS_BG_AUDIO_URLS";
    
    protected static final int MSG_UI_HIDE_NAV = 0;
    
    protected static final int MSG_UPDATE_PIC_POS = 1;
    
    protected static final int MSG_PROCESS_BAR = 2;
    
    protected static final int MSG_UI_SHOW_SIZE = 3;
    
    /**
     * 中间弹出菜单
     */
    protected ImageSettingsDialog mPopMenu;
    
    private ProgressBar mProgressBar = null;
    
    private LinearLayout mOperatingHint;
    
    /**
     * Image player RelativeLayout
     */
    private RelativeLayout mIMPRL = null;
    
    private TextView mOperationTextView;
    
    // Toast
    private Toast mCanntPreToast;
    
    private Toast mCanntNextToast;
    
    private Toast mStartAutoPlay;
    
    private Toast mStopAutoPlay;
    
    private Toast mCanntShow;
    
    private Toast mRotatedFailed;
    
    private String mCurMusicPath;
    
    private int mCurMusicRes;
    
    private IVideoViewAdapter mMediaPlayer;
    
    /**
     * ImagePlaySetHelper
     */
    private ImagePlaySetHelper mImagePlaySetHelper;
    
    /**
     * 长按标志
     */
    private boolean mLongPressFlag = false;
    
    /**
     * 按键计时，是否是长按键事件
     * */
    private long mKeydownTicket = 0;
    
    /**
     * 长按键判断标准
     * */
    protected static final int TICKET_DELAY = 40;
    
    private int mLastKeyCode = -1;
    
    public static boolean isNeedRejectKey = false;
    
    private String musicfilePath = "";
    
    private boolean beMusicPlayError = false;
    
    private static InputManager mInputManager = null;
    
    /**
     * 背景音乐播放器
     */
    private MediaPlayer mBackMusicPlayer;
    /**
     * 背景音乐信息
     */
    private List<FileInfo> mBackMusicInfos;
    private static final String BACK_MUSIC_NAME = "name_image_back_music";
    private static final String BACK_MUSIC_KEY = "key_image_back_music";
    /**
     * 背景音乐消息处理
     */
    private Handler mBackMusicHandler = new Handler(){
    	public void handleMessage(Message msg) {
    		playBackgroundMusic();
    	};
    };
    /**
     * 当前背景音乐播放位置
     */
    private int mBackMusicPlayPosition;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        mLog.d(TAG, "onCreate -- 1");
        super.onCreate(savedInstanceState);
    }
    
    /**
     *	设置底部提示信息 
     */
    private void setOperationText()
    {
        if (mOperationTextView != null)
        {
            
            //如果是内置播放器
            if (mbInternalPlayer)
            {
                mOperationTextView.setText(ResLoadUtil.getStringById(this, R.string.operation_text));
                
                //如果是外置播放器
            }
            else
            {
                mOperationTextView.setText(ResLoadUtil.getStringById(this, R.string.operation_text_to));
            }
        }
    }
    
    /**
     * 初始化播放设置
     */
    private void initPlaySet()
    {
        mImagePlaySetHelper = new ImagePlaySetHelper(this);
        mAutoPlayInterval = mImagePlaySetHelper.getPlayInterval();
        setImageDetail();
    }
    
    private void setImageDetail()
    {
        mLog.d(TAG, "mImageDetailInfo--->" + mImageDetailInfo);
        if (mImageDetailInfo != null && mImagePlaySetHelper != null)
        {
            if (mImagePlaySetHelper.isDisplayDetail())
            {
                mLog.d(TAG, "mImageDetailInfo.setVisibility(View.VISIBLE);");
                mImageDetailInfo.setVisibility(View.VISIBLE);
            }
            else
            {
                mLog.d(TAG, "mImageDetailInfo.setVisibility(View.GONE);");
                mImageDetailInfo.setVisibility(View.GONE);
            }
            
        }
    }
    
    
    @Override
    protected void onResume()
    {
        mLog.d(TAG, "onResume()");
        if (mBackFromImageScaleMove)
        {
            overridePendingTransition(0, 0);
            mBackFromImageScaleMove = false;
        }
        if (mImageSwitcher != null && mImagePlaySetHelper != null)
        {
            mLog.d(TAG, "setAutoMode()--->mbAutoPlay:" + mbAutoPlay);
            mbCovered = false;
			/* BEGIN: Modified by c00224451 for  DTS2014022708542 2014/3/4 */
            mImageSwitcher.setAudioPlayMode(mbAutoPlay, mImagePlaySetHelper.getPlayInterval());
            mImageSwitcher.currImage();
			/* END: Modified by c00224451 for  DTS2014022708542 2014/3/4 */
            mIsplayBackgroundMusic = mImagePlaySetHelper.isPlay();
            Log.i(TAG, "onResume->mIsplayBackgroundMusic:" + mIsplayBackgroundMusic);
            switch (mImagePlaySetHelper.getPlayModeIndex())
            {
                case 0:
                    //循环播放
                    setPlayMode(ConstData.MediaPlayMode.MP_MODE_ALL_CYC);
                    break;
                case 1:
                    setPlayMode(ConstData.MediaPlayMode.MP_MODE_ALL);
                    break;
                default:
                    break;
            }
            if(PlatformUtils.isSupportBackMusic()){
            	loadBackMusic();
                playBackgroundMusic();
            }
        }
        else
        {
            finish();
        }
        //begin add by caochao for  DTS2014111107357 随心控推送图片到盒子播放，推送成功后等待5S后，手机端提示“远端服务已经停止”
        syncPositionInfo();
		//end add by caochao for  DTS2014111107357 随心控推送图片到盒子播放，推送成功后等待5S后，手机端提示“远端服务已经停止”
        
        super.onResume();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.rockchips.iptv.stb.dlna.player.PlayerBaseActivity#onPause()
     */
    @Override
    protected void onPause()
    {
        mLog.d(TAG, "onPause()--->");
        mbCovered = true;
        if(PlatformUtils.isSupportBackMusic()){
        	releaseBackMusicPlayer();
            saveBackMusic(mBackMusicInfos);
        }
        super.onPause();
        if (mImageSwitcher != null)
        {
            mLog.d(TAG, "onPause()--->setAutoMode");
            mImageSwitcher.setAutoMode(false, 0);
        }
        
        
    }
    /**
     * 解除屏蔽键
     */
    public static void  setInputUnBlock(){
        if(mInputManager == null){
            mInputManager = (InputManager)mContext.getSystemService(Context.INPUT_SERVICE);
        }
        try
        {
            Method method = InputManager.class.getMethod("setInputUnBlock");
            method.invoke(mInputManager);
        }
        catch (NoSuchMethodException e)
        {
            // TODO Auto-generated catch block
        }
        catch (IllegalArgumentException e)
        {
            // TODO Auto-generated catch block
        }
        catch (IllegalAccessException e)
        {
            // TODO Auto-generated catch block
        }
        catch (InvocationTargetException e)
        {
            // TODO Auto-generated catch block
        }
    }
    /**
     * 屏蔽数组a中的所有按键
     * @param a
     */
    
    public static void  setInputHalfBlock(int[] a){
        if(mInputManager == null){
            mInputManager = (InputManager)mContext.getSystemService(Context.INPUT_SERVICE);
        }
        try
        {
            Method method = InputManager.class.getMethod("setInputHalfBlock",new Class[]{int[].class, int[].class});
            method.invoke(mInputManager,new Object[]{a,null});
        }
        catch (NoSuchMethodException e)
        {
            // TODO Auto-generated catch block
        }
        catch (IllegalArgumentException e)
        {
            // TODO Auto-generated catch block
        }
        catch (IllegalAccessException e)
        {
            // TODO Auto-generated catch block
        }
        catch (InvocationTargetException e)
        {
            // TODO Auto-generated catch block
        }
    }
    /*
     * (non-Javadoc)
     * 
     * @see com.rockchips.iptv.stb.dlna.player.PlayerBaseActivity#onStop()
     */
    @Override
    protected void onStop()
    {
        mLog.d(TAG, "onStop()--->");
        super.onStop();
        mLog.d(TAG, "onStop unBlock ==========================>");
        if(isNeedRejectKey){
            setInputUnBlock();
        }
        //置为false，为了解决有图片推送过来，再拉回来图片，回到图片界面时，如果图片无法加载，则显示无法加载
        UriTexture.setMbStop(false);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.rockchips.iptv.stb.dlna.player.PlayerBaseActivity#onDestroy()
     */
    @Override
    protected void onDestroy()
    {
        mLog.d(TAG, "onDestroy()---->");
        mLog.d(TAG, "onDestroy unBlock ==========================>");
        if(isNeedRejectKey){
            setInputUnBlock();
        }
        //取消下载云相册图片 
        UriTexture.cancelDownload();
        mCanntPreToast = null;
        mCanntNextToast = null;
        mStartAutoPlay = null;
        mStopAutoPlay = null;
        
        mCanntShow = null;
        if (mImageSwitcher != null)
        {
            mImageSwitcher.stopDLNAHandler();
            mImageSwitcher.uninit();
            mImageSwitcher = null;
        }
        
        if (mGifView != null)
        {
            //停止gif图播放线程，释放资源
            mGifView.stop();
            mGifView = null;
        }
        super.onDestroy();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.rockchips.iptv.stb.dlna.player.PlayerBaseActivity#play()
     */
    @Override
    protected void play()
    {
        mLog.d(TAG, "play - 1");
        if (mImageSwitcher != null)
        {
            mImageSwitcher.currImage();
        }
        
        mLog.d(TAG, "play - 2");
    }
    
    protected void pause()
    {
        if (uiHandler != null)
        {
            Message msg = Message.obtain();
            msg.arg1 = 0;
            msg.what = MSG_PROCESS_BAR;
            
            uiHandler.sendMessage(msg);
        }
        if (mImageSwitcher != null)
        {
            mImageSwitcher.setAutoMode(false, 0);
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.rockchips.iptv.stb.dlna.player.PlayerBaseActivity#stop()
     */
    @Override
    protected void stop()
    {
        if (mMediaCenterPlayerClient != null)
        {
            mLog.d(TAG, "Send the stop to Sender client");
            mMediaCenterPlayerClient.stop(mStrCurrentUrl);
        }
        
        // 需要关闭处理线程
        mProgressBar.setVisibility(View.GONE);
        
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.rockchips.iptv.stb.dlna.player.PlayerBaseActivity#loadResource()
     */
    @Override
    protected void loadResource()
    {
        //setContentView(R.layout.ip_image_fullscreen);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.rockchips.iptv.stb.dlna.player.PlayerBaseActivity#mcsStop(android.content
     * .Intent)
     */
    @Override
    protected void mcsStop(Intent intent)
    {
        mLog.d(TAG, "mcsStop --In");
        
        mbAutoPlay = false;
        if (mImageSwitcher != null)
        {
            mImageSwitcher.setAutoMode(mbAutoPlay, 0);
        }
        if (uiHandler != null)
        {
            Message msg = Message.obtain();
            msg.arg1 = 0;
            msg.what = MSG_PROCESS_BAR;
            
            uiHandler.sendMessage(msg);
        }
        
        unbind();
        finish();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.rockchips.iptv.stb.dlna.player.PlayerBaseActivity#mcsSeek(android.content
     * .Intent)
     */
    @Override
    protected void mcsSeek(Intent intent)
    {
        
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.rockchips.iptv.stb.dlna.player.PlayerBaseActivity#mcsPause(android.content
     * .Intent)
     */
    @Override
    protected void mcsPause(Intent intent)
    {
        if (uiHandler != null)
        {
            Message msg = Message.obtain();
            msg.arg1 = 0;
            msg.what = MSG_PROCESS_BAR;
            
            uiHandler.sendMessage(msg);
        }
        
        mbAutoPlay = false;
        if (mImageSwitcher != null)
        {
            mImageSwitcher.setAutoMode(mbAutoPlay, 0);
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.rockchips.iptv.stb.dlna.player.PlayerBaseActivity#mcsPlay(android.content
     * .Intent)
     */
    @Override
    protected void mcsPlay(Intent intent)
    {
        mLog.d(TAG, "mcsPlay - 1");
        
        if (mImageSwitcher != null)
        {
            LocalMediaInfo mbi = getCurrentMediaInfo();
            
            String strUrl = null;
            
            if (mbi != null)
            {
                strUrl = mbi.getUrl();
                mLog.d(TAG, "mcsPlay - 1--1");
            }
            
            if (mbi != null && StringUtils.isNotEmpty(strUrl))
            {
                mLog.d(TAG, "mcsPlay - 1--2");
                if (mImageSwitcher.isTheSameUrl(strUrl))
                {
                    return;
                }
                
            }
            else
            {
                mLog.d(TAG, "mcsPlay - 1--3");
                mImageSwitcher.currImage();
            }
        }
        mLog.d(TAG, "mcsPlay - 2");
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.rockchips.iptv.stb.dlna.player.PlayerBaseActivity#mcsSetMediaData(android
     * .content.Intent)
     */
    @Override
    protected void mcsSetMediaData(Intent intent)
    {
        parseInputIntent(intent);
        
        if (mImageSwitcher != null)
        {
            mLog.d(TAG, "mPlayStateInfo.getMediaList().size():" + mPlayStateInfo.getMediaList().size());
            mLog.d(TAG, "mPlayStateInfo.getSenderClientUniq().trim():" + mPlayStateInfo.getSenderClientUniq().trim());
            
            // 如果是推送或甩屏过来的，只有一个媒体文件的情况下，快速关闭自动播放
            if ((mPlayStateInfo.getMediaList().size() == 1)
                && (mPlayStateInfo.getSenderClientUniq().trim().equals(ConstData.ClientTypeUniq.PUSH_UNIQ.trim()) || mPlayStateInfo.getSenderClientUniq()
                    .trim()
                    .equals(ConstData.ClientTypeUniq.SYN_UINQ.trim())))
            {
                mLog.d(TAG, "Push only one image or Syn only one image to STB");
                mImageSwitcher.setAutoMode(false, 0);
            }
            
            // Added by zhaomingyang 00184367 2012年2月8日 --- Begin
            // 解决推送第一个图片后，再推送第二个图片没反应的问题
            mImageSwitcher.setPlayInfo(getPlayStateInfo());
            // Added by zhaomingyang 00184367 2012年2月8日 --- End
            
            mImageSwitcher.currImage();
			//begin add by caochao for  DTS2014111107357 随心控推送图片到盒子播放，推送成功后等待5S后，手机端提示“远端服务已经停止”
            syncPositionInfo();
			//end add by caochao for  DTS2014111107357 随心控推送图片到盒子播放，推送成功后等待5S后，手机端提示“远端服务已经停止”
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.rockchips.iptv.stb.dlna.player.DLNAImageSwitcher.
     * DLNAImageSwitcherListenerInterface
     * #showDetail(com.rockchips.iptv.stb.dlna.data.MediaFileInfo)
     */
    public void showDetail(final LocalMediaInfo mbi, int curIndex, int total)
    {
        if (mPicTitle != null)
        {
            mPicTitle.setText(mbi.getmFileName());
        }
        
        if (mImagesize != null && curIndex == -100)
        {
            mLog.d(TAG, "showDetail --->ImageSizeAsyncTask");
            new Thread()
            {
                public void run()
                {
                    String size = ImageUtils.getPicSize(mbi.getUrl(), mbi.getmDeviceType());
                    Message message = Message.obtain();
                    message.what = MSG_UI_SHOW_SIZE;
                    message.obj = size;
                    uiHandler.sendMessage(message);
                };
            }.start();
        }
        if (mImageTime != null)
        {
            String Date = null;
            if (mbi.getmDeviceType() == ConstData.DeviceType.DEVICE_TYPE_DMS)
            {
                Date = mbi.getmModifyDateStr();
            }
            else
            {
                Date = GetDateUtil.getTime(this, mbi.getmModifyDate());
            }
            if (Date == null)
            {
                Date = getString(R.string.unknown);
            }
            mLog.d(TAG, "date====" + Date);
            mImageTime.setText(Date);
        }
        mLog.d(TAG, "showDetail -" + mbi.getmFileName());
        
        // FIXME modified by zwx160481，当下载完之前设置详细信息时， curIndex=-100
        if (curIndex != -100)
        {
            if (mProgressBar != null)
            {
                mProgressBar.setVisibility(View.GONE);
            }
        }
        curIndex = curIndex + 1;
        if(curIndex>0){
        	mImageOrder.setText(String.valueOf(curIndex));
        }
        String pos = String.valueOf(curIndex) + "/" + String.valueOf(total);
        if (mPicPos != null)
        {
            mPicPos.setText(pos);
        }
        
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.rockchips.iptv.stb.dlna.player.DLNAImageSwitcher.
     * DLNAImageSwitcherListenerInterface#arriveFisrtElement()
     */
    public void arriveFirstElement(boolean b)
    {
        if (!isMyMediaType())
        {
            return;
        }
        
        mLog.d(TAG, "arriveFirstElement b:" + b);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.rockchips.iptv.stb.dlna.player.DLNAImageSwitcher.
     * DLNAImageSwitcherListenerInterface#arriveLastElement()
     */
    public void arriveLastElement(boolean b)
    {
        if (!isMyMediaType())
        {
            return;
        }
        
        mLog.d(TAG, "arriveLastElement b:" + b);
        
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.rockchips.iptv.stb.dlna.player.DLNAImageSwitcher.
     * DLNAImageSwitcherListenerInterface#cantMovePrevious()
     */
    public void cantMovePrevious()
    {
        
        mLog.d(TAG, "cantMovePrevious");
        if (mLeftNavigation != null)
        {
            mLeftNavigation.setVisibility(View.GONE);
        }
        if (mCanntPreToast != null)
        {
            mCanntPreToast.show();
        }
        
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.rockchips.iptv.stb.dlna.player.DLNAImageSwitcher.
     * DLNAImageSwitcherListenerInterface#cantMoveNext()
     */
    public void cantMoveNext()
    {
        if (mRightNavigation != null)
        {
            mRightNavigation.setVisibility(View.GONE);
        }
        
        if (mCanntNextToast != null && mbInternalPlayer)
        {
            mCanntNextToast.show();
        }
        
        // 重置自动播放
        pause();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onTouchEvent(android.view.MotionEvent)
     */
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        mLog.d(TAG, "onTouchEvent--->1");
        // 鼠标点击
        if (event.getAction() == MotionEvent.ACTION_DOWN)
        {
            
            if (!isMyMediaType())
            {
                return super.onTouchEvent(event);
            }
            
            if (mImageSwitcher != null)
            {
                mbAutoPlay = !mbAutoPlay;
                mImageSwitcher.setAutoMode(mbAutoPlay, mAutoPlayInterval);
            }
            if (mbAutoPlay)
            {
                
                if (mStartAutoPlay != null)
                {
                    mStartAutoPlay.show();
                }
                
            }
            else
            {
                
                if (mStopAutoPlay != null)
                {
                    mStopAutoPlay.show();
                }
                
            }
            
        }
        
        return super.onTouchEvent(event);
    }
    
    private long keytime = 0;
    
    private long keyuptime = 0;
    
    private int rotalAngle = 0;
    
    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        /**************** add by zhouhui at 2012-1-16 begin *********************/
        mLog.i(TAG, StringUtils.getSystemTimeLogText() + "onKeyDown----------------------------" + event);
        
        // 对音量键不进行按键累积，避免调节音量出现卡顿
        switch (keyCode)
        {
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                mLog.d(TAG, "KeyEvent.KEYCODE_VOLUME_UP || KeyEvent.KEYCODE_VOLUME_DOWN");
                if (audioManager == null)
                {
                    mLog.d(TAG, "audioManager == null, create a AudioManager object");
                    audioManager = (AudioManager)this.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                }
                
                int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                float volumePercent = (float)currentVolume / (float)maxVolume;
                mLog.d(TAG, "currentVolume:" + currentVolume);
                mLog.d(TAG, "maxVolume:" + maxVolume);
                mLog.d(TAG, "volumePercent:" + volumePercent);
                
                if (mMediaCenterPlayerClient != null)
                {
                    mLog.d(TAG, "Send the volume percent to Sender client");
                    mMediaCenterPlayerClient.adjustVolume(ConstData.VolumeAdjustType.ADJUST_SET, volumePercent);
                }
                return super.onKeyDown(keyCode, event);
            case KeyEvent.KEYCODE_BACK:
                return super.onKeyDown(keyCode, event);
            case KeyEvent.KEYCODE_MENU:
            case KeyEvent.KEYCODE_STAR:
                mLog.d(TAG, "KeyEvent.KEYCODE_MENU");
                openBottomMenu();
                
                return true;
        }
        /**************** add by zhouhui at 2012-1-16 end *********************/
        
        rotalAngle = 0;
        
        switch (keyCode)
        {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                mLog.i(TAG, "KEYCODE_DPAD_CENTER B");
                
                //内置播放器才响应ok键
                if (mbInternalPlayer)
                {
                    if (mImageSwitcher != null)
                    {
                        mLog.d(TAG, "KEYCODE_DPAD_CENTER E");
                        mbAutoPlay = !mbAutoPlay;
                        mImageSwitcher.setAutoMode(mbAutoPlay, mAutoPlayInterval);
                    }
                    if (mbAutoPlay)
                    {
                        if (mStartAutoPlay != null)
                        {
                            mStartAutoPlay.show();
                        }
                        
                    }
                    else
                    {
                        if (mStopAutoPlay != null)
                        {
                            mStopAutoPlay.show();
                        }
                        
                    }
                }
                
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                if (mImageSwitcher != null)
                {
                    
                    mbAutoPlay = false;
                    mImageSwitcher.setAutoMode(mbAutoPlay, mAutoPlayInterval);
                    
                    mLog.i(TAG, "KEYCODE_DPAD_UP B");
                    // 旋转图片 added by zwx160481 zouzhiyi 2013-4-12
                    mImageSwitcher.rotatePic(KeyEvent.KEYCODE_DPAD_UP,mGifView);
                    
                    mLog.i(TAG, "KEYCODE_DPAD_UP E");
                }
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                
                if (mImageSwitcher != null)
                {
                    mbAutoPlay = false;
                    mImageSwitcher.setAutoMode(mbAutoPlay, mAutoPlayInterval);
                    
                    mLog.i(TAG, "KEYCODE_DPAD_DOWN B");
                    
                    // 旋转图片 added by zwx160481 zouzhiyi 2013-4-12
                    mImageSwitcher.rotatePic(KeyEvent.KEYCODE_DPAD_DOWN,mGifView);
                    mLog.i(TAG, "KEYCODE_DPAD_DOWN E");
                }
                break;
            default:
                break;
        }
        
        return super.onKeyDown(keyCode, event);
    }
    
    @Override
    public void onOptionsMenuClosed(Menu menu)
    {
        super.onOptionsMenuClosed(menu);
        mLog.d(TAG, "onOptionsMenuClosed---->");
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onKeyUp(int, android.view.KeyEvent)
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event)
    {
        uiHandler.sendEmptyMessageDelayed(MSG_UI_HIDE_NAV, 2000);
        mLog.d(TAG, "onKeyUp----------->mLongPressFlag-->" + mLongPressFlag);
        long now = System.currentTimeMillis();
        if (now - keyuptime < 800)
        {
            return true;
        }
        
        keyuptime = now;
        switch (keyCode)
        {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                mLog.d(TAG, "onKeyUp----------->KEYCODE_DPAD_LEFT");
                // 推送或甩屏，如果过来的只有一个媒体，不提示"第一张图片"
                if (isPushType())
                {
                    if ((mPlayStateInfo != null) && (mPlayStateInfo.getMediaList() != null)
                        && (mPlayStateInfo.getMediaList().size() == 1))
                    {
                        break;
                    }
                }
                
                if (mImageSwitcher != null)
                {
                    mbAutoPlay = false;
                    mImageSwitcher.setAutoMode(mbAutoPlay, 0);
                    mImageSwitcher.preImage();
                    
                  //暂时注释，疑似云相册相关
//                    cacheImageList(-1);
                    
                }
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                mLog.d(TAG, "onKeyUp----------->KEYCODE_DPAD_RIGHT");
                mLog.d(TAG, "onKeyUp----------->11-->" + DateUtil.getCurrentTime());
                // 推送或甩屏，如果过来的只有一个媒体，不提示"最后一张图片"
                if (isPushType())
                {
                    if ((mPlayStateInfo != null) && (mPlayStateInfo.getMediaList() != null)
                        && (mPlayStateInfo.getMediaList().size() == 1))
                    {
                        break;
                    }
                }
                
                if (mImageSwitcher != null)
                {
                    mbAutoPlay = false;
                    mImageSwitcher.setAutoMode(mbAutoPlay, 0);
                    mImageSwitcher.nextImage();
                    
                    mLog.d(TAG, "onKeyUp----------->end-->" + DateUtil.getCurrentTime());
                    //暂时注释，疑似云相册相关
//                    cacheImageList(1);
                }
                break;
            
            default:
                break;
        }
        // }
        
        return super.onKeyUp(keyCode, event);
    }
    
    private Handler uiHandler = new Handler()
    {
        /*
         * (non-Javadoc)
         * 
         * @see android.os.Handler#handleMessage(android.os.Message)
         */
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case MSG_UI_HIDE_NAV:
                    if (mLeftNavigation != null)
                    {
                        mLeftNavigation.setVisibility(View.GONE);
                    }
                    if (mRightNavigation != null)
                    {
                        mRightNavigation.setVisibility(View.GONE);
                    }
                    
                    break;
                case MSG_UPDATE_PIC_POS:
                    
                    updatePicPos();
                    
                    break;
                case MSG_UI_SHOW_SIZE:
                    if (msg.obj != null && mImagesize != null)
                    {
                        String size = (String)msg.obj;
                        mImagesize.setText(size);
                    }
                    
                    break;
                case MSG_PROCESS_BAR:
                    
                    if (mProgressBar != null)
                    {
                        
                        if (msg.arg1 == 0)
                        {
                            mProgressBar.setVisibility(View.GONE);
                        }
                        else
                        {
                            mProgressBar.setVisibility(View.VISIBLE);
                        }
                    }
                    
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };
    
    /**
     * 记录菜单子项中当前状态
     */
    boolean bShownDetailInfo = false;
    
//    private ICloudPrecache mIcp = null;//暂时注释，待添加云相册后打开
    
//    private boolean mbCloudDevice = false;//暂时注释，待添加云相册后打开
    
    private boolean isShownDetailInfo()
    {
        return bShownDetailInfo;
    }
    
    private void reverseShownDetailInfo()
    {
        if (isShownDetailInfo())
        {
            // 当前显示 --> 就隐藏
            mLog.d(TAG, "reverseShownDetailInfo - 1");
            bShownDetailInfo = false;
            mPicPlayerDetailLayout.setVisibility(View.GONE);
            mPicTitle.setVisibility(View.GONE);
            mPicPos.setVisibility(View.GONE);
            
        }
        else
        {
            // 当前隐藏 --> 显示
            mLog.d(TAG, "reverseShownDetailInfo - 2");
            bShownDetailInfo = true;
            mPicPlayerDetailLayout.setVisibility(View.VISIBLE);
            mPicTitle.setVisibility(View.VISIBLE);
            mPicPos.setVisibility(View.VISIBLE);
        }
    }
    
    public boolean onOptionsItemSelected(MenuItem item)
    {
        return false;
    }
    
    private boolean mBackFromImageScaleMove;
    
    private void startImageScaleMove()
    {
        mLog.d(TAG, "startImageScalMove E");
        LocalMediaInfo media = getCurrentMediaInfo();
        if (null == media)
        {
            return;
        }
        ImageScaleMoveData.mBitmapFileUrl = media.getUrl();
        mLog.d(TAG, "startImageScalMove getData = " + ImageScaleMoveData.mBitmapFileUrl);
        
        Intent intent = new Intent();
        intent.setClass(getBaseContext(), ImageScaleMoveActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(0, 0);
        mBackFromImageScaleMove = true;
    }
    
    /* END: Added by r00178559 for DTS2014021408267 2014/02/14 */
    
    /*
     * (non-Javadoc)
     * 
     * @see com.rockchips.iptv.stb.dlna.player.PlayerBaseActivity#getUUID()
     */
    @Override
    protected int getUUID()
    {
        return ConstData.MediaType.IMAGE;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.rockchips.iptv.stb.dlna.player.DLNAImageSwitcher.
     * DLNAImageSwitcherListenerInterface#showImageEnd()
     */
    public void showImageEnd()
    {
        if (uiHandler != null)
        {
            uiHandler.sendEmptyMessageDelayed(MSG_UI_HIDE_NAV, 800);
            Message msg = Message.obtain();
            msg.arg1 = 0;
            msg.what = MSG_PROCESS_BAR;
            
            uiHandler.sendMessage(msg);
        }
    }
    
    @Override
    public void setCantDisplay(boolean b)
    {
        if (mDisplayException == null)
        {
            return;
        }
        if (b)
        {
            mLog.d(TAG, "setCantDisplay---->VISIBLE");
            mDisplayException.setVisibility(View.VISIBLE);
            setImageErrorInfo();
        }
        else
        {
            mLog.d(TAG, "setCantDisplay---->GONE");
            mDisplayException.setVisibility(View.GONE);
        }
        
    }
    
    // 设置加载图片出错信息
    private void setImageErrorInfo()
    {
        if (mImageErrorInfo != null)
        {
            mLog.d(TAG, "CurlDownload.mCurlCode-->" + CurlDownload.mCurlCode);
            switch (CurlDownload.mCurlCode)
            {
            
            //无法显示
                case -1:
                    mImageErrorInfo.setText(ResLoadUtil.getStringById(this, R.string.can_not_play_image));
                    break;
                
                //连接超时
                case 28:
                    mImageErrorInfo.setText(ResLoadUtil.getStringById(this, R.string.load_net_image_timeout));
                    break;
                
                //加载失败
                default:
                    mImageErrorInfo.setText(ResLoadUtil.getStringById(this, R.string.load_net_image_fail));
                    break;
            }
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.rockchips.iptv.stb.dlna.player.DLNAImageSwitcher.
     * DLNAImageSwitcherListenerInterface#canntshowImage()
     */
    public void canntshowImage()
    {
        
        // 推送或甩屏，如果过来的只有一个媒体Url，关闭图片播放器
        if (isPushType())
        {
            if ((mPlayStateInfo != null) && (mPlayStateInfo.getMediaList() != null)
                && (mPlayStateInfo.getMediaList().size() == 1))
            {
                mLog.d(TAG, "push or sync a image, but cannot be displayed, so finish the image player");
                
                // 回发播放器的stop状态
                if (mMediaCenterPlayerClient != null && mMediaCenterPlayerClient.isConnected())
                {
                    mLog.d(TAG, "Send stop status to sender");
                }
            }
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.rockchips.iptv.stb.dlna.player.DLNAImageSwitcher.
     * DLNAImageSwitcherListenerInterface#rotateIamgeFailed()
     */
    public void rotateIamgeFailed()
    {
        // 提示图片旋转失败
        if (mRotatedFailed != null)
        {
            mRotatedFailed.show();
        }
    }
    
    /**
     * mcsAppendList
     * 
     * @param intent
     *            void
     * @exception
     */
    public void mcsAppendList(Intent intent)
    {
        if (uiHandler != null)
        {
            uiHandler.sendEmptyMessage(MSG_UPDATE_PIC_POS);
        }
        
    }
    
    /**
     * mcsDelDevice
     * 
     * @param intent
     *            void
     * @exception
     */
    public void mcsDelDevice(Intent intent)
    {
        if (uiHandler != null)
        {
            uiHandler.sendEmptyMessage(MSG_UPDATE_PIC_POS);
        }
    }
    
    private void updatePicPos()
    {
        
        if (getPlayStateInfo() == null)
        {
            return;
        }
        
        int size = getPlayStateInfo().getMediaList().size();
        
        if (size == 0)
        {
            unbind();
            finish();
            return;
        }
        
        int index = getPlayStateInfo().getCurrentIndex();
        index = index + 1;
        String strPos = index + "/" + size;
        
        mLog.d(TAG, " mcsAppendList - strPos:" + strPos);
        
        mPicPos.setText(strPos);
    }
    
    /**
     * 
     * getMediaType 获取播放器媒体类型 void
     * 
     * @exception
     */
    protected int getMediaType()
    {
        return ConstData.MediaType.IMAGE;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.rockchips.iptv.stb.dlna.player.PlayerBaseActivity#onDelecteDeviceId(
     * java.lang.String)
     */
    @Override
    protected int onDelecteDeviceId(String devId)
    {
        
        return 0;
    }
    
    /**
     * 
     * @param 相片全路径
     * @return 相片拍摄时的角度
     */
    public int getOrientation(String filepath)
    {
        int degree = 0;
        ExifInterface exif = null;
        try
        {
            exif = new ExifInterface(filepath);
        }
        catch (IOException e)
        {
        }
        if (exif != null)
        {
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            if (orientation != -1)
            {
                switch (orientation)
                {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        break;
                }
            }
        }
        return degree;
    }
    
    private Bitmap decodeFile(String filename)
    {
        Bitmap bitmap = null;
        
        try
        {
            if (filename.startsWith("file://"))
            {
                filename = filename.substring("file://".length());
            }
            
            // 图片解码
            BitmapFactory.Options options = null;
            // 获取采样率
            options = UriTexture.getOptions(filename, 1280, 720);
            bitmap = BitmapFactory.decodeFile(filename, options);
            
            if (bitmap != null)
            {
                mLog.d(TAG, "--------->decode bitmap ok: " + filename);
            }
            else
            {
                mLog.d(TAG, "--------->decode bitmap failed: " + filename);
            }
            
        }
        catch (OutOfMemoryError ex)
        {
            mLog.d(TAG, "--------->decode bitmap failed: " + filename);
            mLog.d(TAG, "--------->error message: " + ex.getLocalizedMessage());
            bitmap = null;
        }
        catch (Exception e)
        {
            mLog.d(TAG, "--------->decode bitmap failed: " + filename);
            mLog.d(TAG, "--------->error message: " + e.getLocalizedMessage());
            bitmap = null;
            
        }
        return bitmap;
    }
    
    
    private final static int MENUBUTTON_STOREUP = 0;
    
    private final static int MENUBUTTON_BLINDWINDOW = 1;
    
    private final static int MENUBUTTON_PLAYINTERVAL = 2;
    
    private final static int MENUBUTTON_BACKGROUNDMUSIC = 3;
    
    private final static int MENUBUTTON_DELETE = 4;
    
    private final static int MENUBUTTON_SET = 5;
    
    //暂时注释，疑似云相册相关
//    @Override
//    public void onDownloadFinished(Bundle bd, boolean bThum)
//    {
//        
//        MediaInfo mi = null;
//        if (isCloudDevice())
//        {
//            CloudMediaInfo cmi = new CloudMediaInfo(null);
//            cmi.decompress(bd);
//            mi = cmi;
//        }
//        else
//        {
//            MediaFileInfo mfi = new MediaFileInfo(null);
//            mfi.decompress(bd);
//            
//            mi = mfi;
//        }
//        
//        mLog.d(TAG, "onDownloadFinished IN :" + mi + " -- bThum:" + bThum);
//        
//        if (bThum || mi == null)
//        {
//            mLog.d(TAG, "onDownloadFinished OUT EMPTY");
//            return;
//        }
//        
//        // TODO 可以刷新当前无法显示图片
//        if (getCurrentMediaInfo().getData().equalsIgnoreCase(mi.getData()))
//        {
//            
//        }
//        
//        mLog.d(TAG, "onDownloadFinished OUT ");
//        
//    }
//    
//    @Override
//    protected void parseInputIntent(Intent intent)
//    {
//        super.parseInputIntent(intent);
//        // 请求缓存
//        cacheImageList(0);
//    }
//    
//    // 请求缓存
//    protected void cacheImageList(int offsetOfcurrentIndex)
//    {
//        mLog.e(TAG, "cacheImageList offsetOfcurrentIndex:" + offsetOfcurrentIndex);
//        
//        // 请求缓存
//        List<LocalMediaInfo> list = getPlayStateInfo().getCacheList(offsetOfcurrentIndex);
//        
//        List<Bundle> listBundle = new ArrayList<Bundle>();
//        
//        int size = list.size();
//        for (int i = 0; i < size; i++)
//        {
//            listBundle.add(list.get(i).compress());
//        }
//        getCloudPreCache().preCacheMediaInfolist(listBundle);
//    }
//    
//    public ICloudPrecache getCloudPreCache()
//    {
//        
//        if (PrecacheInstance.getInstance().getCloudPreCache() == null)
//        {
//            return null;
//        }
//        
//        PrecacheInstance.getInstance().getCloudPreCache().setIDownloadCompleteListener(this);
//        
//        return PrecacheInstance.getInstance().getCloudPreCache();
//    }
    /** 设置播放间隔 */
    private void setPlayInterval(int index)
    {
        switch (index)
        {
            case 1:
                setAutoPlayInterval(AUTO_PLAY_INTERVAL_E);
                mImageSwitcher.setAutoMode(mbAutoPlay, AUTO_PLAY_INTERVAL_E);
                break;
            case 2:
                setAutoPlayInterval(AUTO_PLAY_INTERVAL_F);
                mImageSwitcher.setAutoMode(mbAutoPlay, AUTO_PLAY_INTERVAL_F);
                break;
            case 3:
                setAutoPlayInterval(AUTO_PLAY_INTERVAL_T);
                mImageSwitcher.setAutoMode(mbAutoPlay, AUTO_PLAY_INTERVAL_T);
                break;
        }
    }
    
    public boolean mIsplayBackgroundMusic = false;
    
    protected static int mmMusicIndex;
    
    int[] resids = new int[] {R.raw.bach_french};
    
    private static String BACKGROUNDMUSICINFO = "BackgroundMusicInfo";
    
    /** 播放背景音乐 */
    public synchronized void playBackgroundMusic()
    {
        mLog.d(TAG, "playBackgroundMusic--->1");
        releaseBackMusicPlayer();
        if (mIsplayBackgroundMusic){
        	mBackMusicPlayer = new MediaPlayer();
        	mBackMusicPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
				
				@Override
				public void onPrepared(MediaPlayer mp) {
					Log.i(TAG, "playBackgroundMusic->onPrepared");
					mBackMusicPlayer.start();
				}
			});
        	mBackMusicPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
				
				@Override
				public void onCompletion(MediaPlayer mp) {
					//播放下一首歌曲
					mBackMusicHandler.sendEmptyMessageDelayed(0, 1000);
				}
			});
        	mBackMusicPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
				
				@Override
				public boolean onError(MediaPlayer mp, int what, int extra) {
					Log.i(TAG, "playBackgroundMusic->onError");
					removeCurrBackMusic();
					//播放下一首歌曲
					mBackMusicHandler.sendEmptyMessageDelayed(0, 1000);
					return true;
				}
			});
        	if(mBackMusicInfos != null && mBackMusicInfos.size() > 0){
        		try{
            		mBackMusicPlayer.setDataSource(this, Uri.fromFile(new File(mBackMusicInfos.get(mBackMusicPlayPosition++ % mBackMusicInfos.size()).getPath())));
        		}catch (Exception e){
        			Log.e(TAG, "playBackgroundMusic->setDataSource->exception1:" + e);
        			removeCurrBackMusic();
        			//播放下一首歌曲
					mBackMusicHandler.sendEmptyMessageDelayed(0, 1000);
        			return;
        		}
        		
        	}else{
        		//播放默认背景音乐
        		String defaultBackUrl = "android.resource://" + getPackageName() + "/" + R.raw.bach_french;
        		try{
        			mBackMusicPlayer.setDataSource(this, Uri.parse(defaultBackUrl));
        		}catch (Exception e){
        			Log.e(TAG, "playBackgroundMusic->setDataSource->exception2:" + e);
        			removeCurrBackMusic();
        			//播放下一首歌曲
					mBackMusicHandler.sendEmptyMessageDelayed(0, 1000);
        			return;
        		}
        		
        	}
        	try{
        		mBackMusicPlayer.prepareAsync();
        	}catch (Exception e){
        		Log.e(TAG, "playBackgroundMusic->prepareAsync->exception:" + e);
        		removeCurrBackMusic();
    			//播放下一首歌曲
				mBackMusicHandler.sendEmptyMessageDelayed(0, 1000);
        	}
        	
        }
        
    }
    
    /**
     * 取到要播放的音乐的路径，主要是给子类重写使用
     * <功能详细描述>
     * @param mmMusicIndex
     * @return
     * @see [类、类#方法、类#成员]
     */
    protected String getMusicPath(int mmMusicIndex)
    {
        return null;
    }
    
    /**
     * 获取音乐个数。获取播放器选择的自定义背景音乐个数，该方法由内置图片播放器实现。
     * @return
     */
    protected int getMusicSize()
    {
        return 0;
    }
    
    /**
     * 清除音乐。该方法由内置图片播放器实现。
     */
    protected void clearMusic()
    {
        
    }
    
    /**
     * 取到将要播放的音乐名字。该方法由内置图片播放器实现。
     * <功能详细描述>
     * @param mmMusicIndex
     * @return
     * @see [类、类#方法、类#成员]
     */
    protected String getMusicName(int mmMusicIndex)
    {
        return null;
    }
    
    /**
     * 根据参数不同显示不同的提示信息，主要用于重写
     * <功能详细描述>
     * @param errorindex 提示索引
     * @see [类、类#方法、类#成员]
     */
    protected void showToast(int errorindex)
    {
        
    }
    
    /**
     * 连续4首不能播放的话退出
     */
    private static final int ERRORTHRESHOLD = 4;
    
    private int errorcount = 0;
    
    private void playMusic(int index, int position)
    {
        mLog.d(TAG, "playMusic--->start  mmMusicIndex=" + mmMusicIndex);
        int musicSize = getMusicSize();
        mLog.d(TAG, "playMusic---  musicSize=" + musicSize);
        if (musicSize > 0)
        {
            mmMusicIndex = mmMusicIndex % musicSize;
        }
        else
        {
            mmMusicIndex = 0;
        }
        
        String filepath = getMusicPath(mmMusicIndex);
        Uri uri = null;
        
        //获取自定义背景音乐地址
        if (musicSize > 0 && filepath != null)
        {
            mLog.d(TAG, "playMusic---  errorcount=" + errorcount);
            if (errorcount >= musicSize || errorcount > ERRORTHRESHOLD)
            {
                showToast(SHOW_MSG_ERROR);
                //所有音乐都不能播放
                clearMusic();
                
                playMusic(resids[0], 0);
                return;
            }
            
            if (filepath.equals(musicfilePath) && errorcount > 0)
            {
                //只有一首音乐，并且出错
                showToast(SHOW_MSG_ERROR);
                clearMusic();
                
                playMusic(resids[0], 0);
                return;
            }
            musicfilePath = filepath;
            
            uri = Uri.parse(filepath);
            
        }
        else
        {
            if (mMediaPlayer != null)
            {
                mMediaPlayer.stopPlayback();
                mMediaPlayer = null;
            }
            
            mCurMusicRes = resids[0];// 记录当前播放的音乐资源id
            
            uri = Uri.parse("android.resource://" + getPackageName() + "/" + mCurMusicRes);
        }
        
        if (mMediaPlayer == null)
        {
            mMediaPlayer = new OrigVideoViewNoView(this);
            if (mMediaPlayer != null)
            {
                mMediaPlayer.setOnCompletionListener(new OnCompleteListener()
                {
                    
                    @Override
                    public void onCompletion(IMediaPlayerAdapter mp)
                    {
                        mLog.d(TAG, "musicInfo --1 mmMusicIndex=" + mmMusicIndex);
                        mLog.d(TAG, "musicInfo --2 resids.length=" + resids.length);
                        mmMusicIndex++;
                        //              mmMusicIndex = mmMusicIndex % resids.length;
                        
                        mLog.d(TAG, "musicInfo --3 mmMusicIndex = " + mmMusicIndex);
                        playMusic(mmMusicIndex, 0);
                    }
                });
            }
            
            if (mMediaPlayer != null)
            {
                mMediaPlayer.setOnPreparedListener(new OnPreparedListener()
                {
                    
                    @Override
                    public void onPrepared(IMediaPlayerAdapter mp)
                    {
                        mLog.e(TAG, "mMediaPlayer onPrepared---");
                        errorcount = 0;
                    }
                });
            }
            
            if (mMediaPlayer != null)
            {
                mMediaPlayer.setOnErrorListener(new OnErrorListener()
                {
                    
                    @Override
                    public boolean onError(IMediaPlayerAdapter mp, int what, int extra)
                    {
                        errorcount++;
                        mmMusicIndex++;
                        
                        playMusic(resids[0], 0);
                        mLog.e(TAG, "mMediaPlayer onError---");
                        return false;
                    }
                });
            }
        }
        
        beMusicPlayError = false;
        
        if (mMediaPlayer != null)
        {
            mMediaPlayer.setVideoURI(uri);
            
            if (filepath == null)
            {
                mMediaPlayer.seekTo(position);
            }
            
            mMediaPlayer.start();
        }
        
        mLog.d(TAG, "playBackgroundMusic--->mbCovered--->" + mbCovered);
        //add by zouzhiyi 2013-7-11 为了解决当退出播放器后，背景音乐还在播放的bug
        if (mbCovered)
        {
            mLog.d(TAG, "mMediaPlayer myRelease--->");
        }
        else
        {
            mLog.d(TAG, "mMediaPlayer playing--->");
            
            if (mMediaPlayer != null)
            {
                mMediaPlayer.start();
            }
        }
    }
    
    /**
     * 跳转到音乐浏览界面，选择音乐 <功能详细描述>
     * 
     * @see [类、类#方法、类#成员]
     */
    protected boolean jumpToMusicBrowser()
    {
        return false;
    }
    
    public int getAutoPlayInterval()
    {
        return mAutoPlayInterval;
    }
    
    public void setAutoPlayInterval(int mAutoPlayInterval)
    {
        this.mAutoPlayInterval = mAutoPlayInterval;
    }
    
    @Override
    public boolean onDown(MotionEvent e)
    {
        mLog.d(TAG, "onFling-->onDown");
        return true;
    }
    
    @Override
    public void onShowPress(MotionEvent e)
    {
        mLog.d(TAG, "onFling-->onShowPress");
        
    }
    
    @Override
    public boolean onSingleTapUp(MotionEvent e)
    {
        mLog.d(TAG, "onFling-->onSingleTapUp");
        return false;
    }
    
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
    {
        mLog.d(TAG, "onFling-->onScroll");
        return false;
    }
    
    @Override
    public void onLongPress(MotionEvent e)
    {
        mLog.d(TAG, "onFling-->onLongPress");
    }
    
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
    {
        mLog.d(TAG, "onFling-->onFling");
        if (e1.getX() - e2.getX() > 120)
        {
            mLog.d(TAG, "onFling--->next");
            if (mImageSwitcher != null)
            {
                mbAutoPlay = false;
                mImageSwitcher.setAutoMode(mbAutoPlay, 0);
                mImageSwitcher.nextImage();
                
              //暂时注释，疑似云相册相关
//                cacheImageList(1);
            }
        }
        else if (e1.getX() - e2.getX() < -120)
        {
            mLog.d(TAG, "onFling--->pre");
            if (mImageSwitcher != null)
            {
                mbAutoPlay = false;
                mImageSwitcher.setAutoMode(mbAutoPlay, 0);
                mImageSwitcher.preImage();
                
                //暂时注释，疑似云相册相关
//                cacheImageList(-1);
                
            }
        }
        return true;
    }
    
    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        mLog.d(TAG, "onTouch--->");
        return this.mDetector.onTouchEvent(event);
    }
    
    @Override
    public void setAutoPlay()
    {
        // 恢复播放状态
        if (mImageSwitcher != null)
        {
            mLog.d(TAG, "setAutoPlay()--->mbAutoPlay-->" + mbAutoPlay);
            mImageSwitcher.setAutoMode(mbAutoPlay, mAutoPlayInterval);
            mImageSwitcher.setAutoPlayArg(mbAutoPlay, mAutoPlayInterval);
        }
    }
    
    //============*开始*弹出菜单相关的界面设定及功能实现============//
    public boolean openPopMenu(int loadManuFlag)
    {
        if (mPopMenu == null)
        {
            mPopMenu = new ImageSettingsDialog(this);
            mPopMenu.setOnSelectTypeListener(mPopMenuOnSelectTypeListener);
            mPopMenu.setOnDismissListener(mPopMenuOnDismissListener);
        }
        
        if (mPopMenu.isShowing())
        {
            mLog.d(TAG, "menuOpened isShowing");
            mPopMenu.hide();
        }
        else
        {
            mLog.d(TAG, "menuOpened isHide");
            mPopMenu.clearCategories();
            //if(!mPopMenu.isCreated())
            loadPopMenu(loadManuFlag);
            //mPopMenu.replayLastSelected();
            //mPopMenu.show(mContainer);
            if(mPopMenu.isCreated())
            	mPopMenu.rebuildView();
            mPopMenu.show();
            // 停止自动播放
            if (mImageSwitcher != null)
            {
                if (mbAutoPlay)
                {
                    mLog.d(TAG, "mImageSwitcher.setAutoMode(false, 22);");
                    mImageSwitcher.setAutoMode(false, 22);
                }
            }
        }
        // 返回为true 则显示系统menu
        return false;
    }
    
    private DialogInterface.OnDismissListener mPopMenuOnDismissListener = new DialogInterface.OnDismissListener()
    {
        @Override
        public void onDismiss(DialogInterface dialog)
        {
            setAutoPlay();
        }
    };
    
    // 加载切换效果菜单项
    private void loadMenuItemsForSwitchEffect(ArrayList<MenuItemImpl> itemImpls)
    {
        if (itemImpls == null)
        {
            mLog.e(TAG, "loadMenuItemsForSwitchEffect input itemImps is null!");
            return;
        }
        MenuItemImpl item = null;
        //缩放
        item =
            new MenuItemImpl(this, 1, EnumAnimationEffect.E_ANIMATION_EFFECT_ZOOM_OUT, R.drawable.menu_icon_set, 1, 1,
                getResources().getString(R.string.anim_type_zoom_out));
        itemImpls.add(item);
        
        //上升
        item =
            new MenuItemImpl(this, 1, EnumAnimationEffect.E_ANIMATION_EFFECT_FLOAT_UP, R.drawable.menu_icon_set, 1, 1,
                getResources().getString(R.string.anim_type_float_up));
        itemImpls.add(item);
        
        //淡入淡出
        item =
            new MenuItemImpl(this, 1, EnumAnimationEffect.E_ANIMATION_EFFECT_FADE_OUT, R.drawable.menu_icon_set, 1, 1,
                getResources().getString(R.string.anim_type_fade_out));
        itemImpls.add(item);
        
        //擦除
        item =
            new MenuItemImpl(this, 1, EnumAnimationEffect.E_ANIMATION_EFFECT_WIPE_OUT, R.drawable.menu_icon_set, 1, 1,
                getResources().getString(R.string.anim_type_scrape_out));
        itemImpls.add(item);
        
        //下降
        item =
            new MenuItemImpl(this, 1, EnumAnimationEffect.E_ANIMATION_EFFECT_FLOAT_DOWN, R.drawable.menu_icon_set, 1, 1,
                getResources().getString(R.string.anim_type_float_down));
        itemImpls.add(item);
        
        //展开
        item =
            new MenuItemImpl(this, 1, EnumAnimationEffect.E_ANIMATION_EFFECT_SPREAD_OUT, R.drawable.menu_icon_set, 1, 1,
                getResources().getString(R.string.anim_type_spread_out));
        itemImpls.add(item);
    }
    
    // 加载切换时间菜单项
    private void loadMenuItemsForSwitchTime(ArrayList<MenuItemImpl> itemImpls)
    {
        if (itemImpls == null)
        {
            mLog.e(TAG, "loadMenuItemsForSwitchTime input itemImps is null!");
            return;
        }
        MenuItemImpl item = null;
        // 8 秒
        item =
            new MenuItemImpl(this, 1, EnumImagePopmenuType.ENUM_AUTOPLAY_TIME_EIGHT, R.drawable.menu_icon_set, 1, 1,
                getResources().getString(R.string.eight_sec));
        itemImpls.add(item);
        //5秒
        item =
            new MenuItemImpl(this, 1, EnumImagePopmenuType.ENUM_AUTOPLAY_TIME_FINE, R.drawable.menu_icon_set, 1, 1,
                getResources().getString(R.string.fine_sec));
        itemImpls.add(item);
        //3 秒
        item =
            new MenuItemImpl(this, 1, EnumImagePopmenuType.ENUM_AUTOPLAY_TIME_THREE, R.drawable.menu_icon_set, 1, 1,
                getResources().getString(R.string.three_sec));
        itemImpls.add(item);
    }
    
    // 加载播放模式菜单项
    private void loadMenuItemsForPlayMode(ArrayList<MenuItemImpl> itemImpls)
    {
        if (itemImpls == null)
        {
            mLog.e(TAG, "loadMenuItemsForPlayMode input itemImps is null!");
            return;
        }
        MenuItemImpl item = null;
        item =
            new MenuItemImpl(this, 1, EnumImagePopmenuType.ENUM_AUTOPLAY_MODE_LOOP, R.drawable.menu_icon_set, 1, 1,
                getResources().getString(R.string.play_mode_loop_play));
        itemImpls.add(item);
        
        item =
            new MenuItemImpl(this, 1, EnumImagePopmenuType.ENUM_AUTOPLAY_MODE_SEQUENCE, R.drawable.menu_icon_set, 1, 1,
                getResources().getString(R.string.play_mode_sequence_play));
        itemImpls.add(item);
    }
    
    // 加载背景音乐播放开关菜单项
    private void loadMenuItemsForBgMusicSwitch(ArrayList<MenuItemImpl> itemImpls)
    {
        if (itemImpls == null)
        {
            mLog.e(TAG, "loadMenuItemsForBgMusicSwitch input itemImps is null!");
            return;
        }
        MenuItemImpl item = null;
        //开启背景音乐
        item =
            new MenuItemImpl(this, 1, EnumImagePopmenuType.ENUM_BG_MUSIC_OPEN, R.drawable.menu_icon_set, 1, 1,
                getResources().getString(R.string.open));
        itemImpls.add(item);
        //关闭背景音乐
        item =
            new MenuItemImpl(this, 1, EnumImagePopmenuType.ENUM_BG_MUSIC_CLOSE, R.drawable.menu_icon_set, 1, 1,
                getResources().getString(R.string.close));
        itemImpls.add(item);
    }
    
    // 加载照片详情开关菜单项
    private void loadMenuItemsForPicDetailSwitch(ArrayList<MenuItemImpl> itemImpls)
    {
        if (itemImpls == null)
        {
            mLog.e(TAG, "loadMenuItemsForPicDetailSwitch input itemImps is null!");
            return;
        }
        MenuItemImpl item = null;
        //开启显示照片详情
        item =
            new MenuItemImpl(this, 1, EnumImagePopmenuType.ENUM_PIC_DETAIL_OPEN, R.drawable.menu_icon_set, 1, 1,
                getResources().getString(R.string.open));
        itemImpls.add(item);
        //关闭显示照片详情
        item =
            new MenuItemImpl(this, 1, EnumImagePopmenuType.ENUM_PIC_DETAIL_CLOSE, R.drawable.menu_icon_set, 1, 1,
                getResources().getString(R.string.close));
        itemImpls.add(item);
    }
    
    private static final int MANU_FLAG_SWITH_WITH = 0x01;
    
    private static final int MANU_FLAG_SWITH_TIME = 0x02;
    
    private static final int MANU_FLAG_BACK_MUSIC = 0x04;
    
    private static final int MANU_FLAG_IMAGE_DETAIL = 0x08;
    
    private static final int MANU_FLAG_PLAY_MODE = 0x10;
    
    protected void loadPopMenu(int loadManuFlag)
    {
        MenuCategory menuCgy = null;
        ArrayList<MenuItemImpl> itemImpls = null;
        
        //只有内置的播放器才有“切换效果”、“切换时间”、“播放模式”选项
        if (mbInternalPlayer)
        {
            /**
             * 切换方式
             */
            if (0 != (loadManuFlag & MANU_FLAG_SWITH_WITH))
            {
                menuCgy = new MenuCategory();
                menuCgy.setCategoryName(getResources().getString(R.string.switch_with));
                itemImpls = new ArrayList<MenuItemImpl>();
                loadMenuItemsForSwitchEffect(itemImpls);
                menuCgy.setMenuItems(itemImpls);
                menuCgy.setSelectIndex(mImagePlaySetHelper.getSwitchWith());
                mPopMenu.addMenuCategory(menuCgy);
            }
            
            /**
             * 切换时间
             */
            if (0 != (loadManuFlag & MANU_FLAG_SWITH_TIME))
            {
                menuCgy = new MenuCategory();
                menuCgy.setCategoryName(getResources().getString(R.string.switch_time));
                itemImpls = new ArrayList<MenuItemImpl>();
                loadMenuItemsForSwitchTime(itemImpls);
                menuCgy.setMenuItems(itemImpls);            
                menuCgy.setSelectIndex(mImagePlaySetHelper.getSwitchTime() - 1);
                mPopMenu.addMenuCategory(menuCgy);
            }
            
            /**
             * 播放模式
             */
            if (0 != (loadManuFlag & MANU_FLAG_PLAY_MODE))
            {
                menuCgy = new MenuCategory();
                menuCgy.setCategoryName(getResources().getString(R.string.switch_with));
                itemImpls = new ArrayList<MenuItemImpl>();
                loadMenuItemsForPlayMode(itemImpls);
                menuCgy.setMenuItems(itemImpls);            
                menuCgy.setSelectIndex(mImagePlaySetHelper.getPlayModeIndex());
                mPopMenu.addMenuCategory(menuCgy);
            }
        }
        
        /**
         * 背景音乐
         */
        if (0 != (loadManuFlag & MANU_FLAG_BACK_MUSIC))
        {
            menuCgy = new MenuCategory();
            menuCgy.setCategoryName(getResources().getString(R.string.background_music));
            itemImpls = new ArrayList<MenuItemImpl>();
            loadMenuItemsForBgMusicSwitch(itemImpls);
            menuCgy.setMenuItems(itemImpls);
            menuCgy.setSelectIndex(mImagePlaySetHelper.isPlay() ? 0 : 1);
            mPopMenu.addMenuCategory(menuCgy);
        }
        
        /**
         * 照片详情
         */
        if (0 != (loadManuFlag & MANU_FLAG_IMAGE_DETAIL))
        {
            menuCgy = new MenuCategory();
            menuCgy.setCategoryName(getResources().getString(R.string.image_detail));
            itemImpls = new ArrayList<MenuItemImpl>();
            loadMenuItemsForPicDetailSwitch(itemImpls);
            menuCgy.setMenuItems(itemImpls);
            menuCgy.setSelectIndex(mImagePlaySetHelper.isDisplayDetail() ? 0 : 1);
            mPopMenu.addMenuCategory(menuCgy);
        }
    }
    
    private OnSelectTypeListener mPopMenuOnSelectTypeListener = new OnSelectTypeListener()
    {
        @Override
        public void onSelectType(MenuItemImpl menuItem)
        {
            if (null == menuItem) 
            {
                return;
            }
            Object type = menuItem.getSelectType();
            if (type instanceof EnumImagePopmenuType)
            {
                // 8秒
                if (type == EnumImagePopmenuType.ENUM_AUTOPLAY_TIME_EIGHT)
                {
                    setPlayInterval(1);
                    mImagePlaySetHelper.saveSwitchTime(1);
                }
                // 5秒
                else if (type == EnumImagePopmenuType.ENUM_AUTOPLAY_TIME_FINE)
                {
                    setPlayInterval(2);
                    mImagePlaySetHelper.saveSwitchTime(2);
                }
                // 3秒
                else if (type == EnumImagePopmenuType.ENUM_AUTOPLAY_TIME_THREE)
                {
                    setPlayInterval(3);
                    mImagePlaySetHelper.saveSwitchTime(3);
                }
                
                //顺序播放
                if(type == EnumImagePopmenuType.ENUM_AUTOPLAY_MODE_SEQUENCE)
                {
                    setPlayMode(ConstData.MediaPlayMode.MP_MODE_ALL);
                    mImagePlaySetHelper.savePlayModeIndex(1);
                }
                //循环播放
                else if(type == EnumImagePopmenuType.ENUM_AUTOPLAY_MODE_LOOP)
                {
                    setPlayMode(ConstData.MediaPlayMode.MP_MODE_ALL_CYC);
                    mImagePlaySetHelper.savePlayModeIndex(0);
                }
                
                // 关闭背景音乐
                if (type == EnumImagePopmenuType.ENUM_BG_MUSIC_CLOSE)
                {
                    mIsplayBackgroundMusic = false;
                    playBackgroundMusic();
                    mImagePlaySetHelper.saveBGMusic(false);
                }
                // 开启背景音乐
                else if (type == EnumImagePopmenuType.ENUM_BG_MUSIC_OPEN)
                {
                    
                    mLog.i(TAG, "onSelectType start music");
                    BackMusicDialog  backMusicDialog = new BackMusicDialog(ImagePlayerActivity.this, new BackMusicDialog.Callback() {
						
						@Override
						public void onFinished(List<FileInfo> fileInfos) {
							if(fileInfos != null){
								mBackMusicInfos = fileInfos;
								mBackMusicPlayPosition = 0;
								saveBackMusic(fileInfos);
							}
							playBackgroundMusic();
						}
					});
                    backMusicDialog.show();
                    mIsplayBackgroundMusic = true;
                    mImagePlaySetHelper.saveBGMusic(true);
                    
                }
                
                // 开启显示照片详情
                if (type == EnumImagePopmenuType.ENUM_PIC_DETAIL_OPEN)
                {
                    mLog.d(TAG, "ENUM_PIC_DETAIL_OPEN");
                    mImagePlaySetHelper.saveImageDetail(true);
                    setImageDetail();
                }
                // 关闭显示照片详情
                else if (type == EnumImagePopmenuType.ENUM_PIC_DETAIL_CLOSE)
                {
                    mLog.d(TAG, "ENUM_PIC_DETAIL_CLOSE");
                    mImagePlaySetHelper.saveImageDetail(false);
                    setImageDetail();
                }
            }
            else if (type instanceof EnumAnimationEffect)
            {
                if (mImageSwitcher == null)
                {
                    mLog.e(TAG, "onSelectType mImageSwitcher == null just return!");
                    return;
                }
                
                EnumAnimationEffect eAnimEffect = (EnumAnimationEffect) type;
                mLog.d(TAG, "eAnimEffect = " + eAnimEffect + " mImageSwitcher = " + mImageSwitcher);        
                mImageSwitcher.setAnimationWith(eAnimEffect.ordinal());
                mImagePlaySetHelper.saveSwitchWith(eAnimEffect.ordinal());
            }
        }
    };
    //============*结束*弹出菜单相关的界面设定及功能实现============//
    
    //============*开始*底部菜单相关的界面设定及功能实现============//
    private enum ENUM_BOTTOM_MENU_TYPE
    {
        ENUM_BOTTOM_MENU_IMAGE_SCALE_MOVE,
        
        ENUM_BOTTOM_MENU_IMAGE_SETTINGS,
        
        ENUM_BOTTOM_MENU_IMAGE_BG_MUSIC
    }
    
    private boolean mBottomPopMenuDismissWihtNoAction = true;
    
    private BottomPopMenu mBottomPopMenu;
    
    private boolean openBottomMenu()
    {
        mLog.d(TAG, "ImagePlayer openBottomMenu");
        
        showBottomMenu();
        
        // 停止自动播放
        if (mImageSwitcher != null)
        {
            if (mbAutoPlay)
            {
                mLog.d(TAG, "mImageSwitcher.setAutoMode(false, 22);");
                mImageSwitcher.setAutoMode(false, 22);
            }
        }
        
        return true;
    }
    
    private void loadBottomMenu()
    {
        if (mBottomPopMenu != null)
        {
            mBottomPopMenu.add(1,
                ENUM_BOTTOM_MENU_TYPE.ENUM_BOTTOM_MENU_IMAGE_SCALE_MOVE,
                R.drawable.scale_move,
                1,
                1,
                getResources().getString(R.string.bottom_menu_image_scale_move));
            mBottomPopMenu.add(2,
                ENUM_BOTTOM_MENU_TYPE.ENUM_BOTTOM_MENU_IMAGE_SETTINGS,
                R.drawable.menu_icon_settings,
                2,
                1,
                getResources().getString(R.string.bottom_menu_image_settings));
            if(PlatformUtils.isSupportBackMusic()){
            	 mBottomPopMenu.add(3,
                         ENUM_BOTTOM_MENU_TYPE.ENUM_BOTTOM_MENU_IMAGE_BG_MUSIC,
                         R.drawable.back_music,
                         3,
                         1,
                         getResources().getString(R.string.background_music));
            }
           
        }
    }
    
    private void showBottomMenu()
    {
        if (mBottomPopMenu == null)
        {
            mBottomPopMenu = new BottomPopMenu(this);
            mBottomPopMenu.setOnSelectTypeListener(mBottomPopMenuOnSelectTypeListener);
            mBottomPopMenu.setOnDismissListener(mBottomPopMenuOnDismissListener);
        }

        if (mBottomPopMenu.isShowing())
        {
            mBottomPopMenu.hide();
        }
        else
        {
            //清空之前备份当前聚焦的MenuItem的ID
            int id = -1;
            id = mBottomPopMenu.getCurrentMenuItem();
            mBottomPopMenu.clear();
            
            //重新加载menu项                
            loadBottomMenu();
            
            if (id != -1)
            {
                //恢复聚焦到上次的那个ID上
                mBottomPopMenu.setCurrentMenuItem(id);
            }
            
            mBottomPopMenu.show(mContainer);
            mBottomPopMenuDismissWihtNoAction = true;
        }
    }
    
    private OnSelectTypeListener mBottomPopMenuOnSelectTypeListener = new OnSelectTypeListener()
    {
        @Override
        public void onSelectType(MenuItemImpl menuItem)
        {
            if (null == menuItem) 
            {
                return;
            }
            Object type = menuItem.getSelectType();
            if (type instanceof ENUM_BOTTOM_MENU_TYPE)
            {
                if (type == ENUM_BOTTOM_MENU_TYPE.ENUM_BOTTOM_MENU_IMAGE_SCALE_MOVE)
                {
                    mBottomPopMenuDismissWihtNoAction = false;
                    mBottomPopMenu.dismiss();
                    startImageScaleMove();
                }
                else if (type == ENUM_BOTTOM_MENU_TYPE.ENUM_BOTTOM_MENU_IMAGE_SETTINGS)
                {
                    mBottomPopMenuDismissWihtNoAction = false;
                    mBottomPopMenu.dismiss();
                    int loadManuFlag = MANU_FLAG_SWITH_WITH | MANU_FLAG_SWITH_TIME | MANU_FLAG_IMAGE_DETAIL | MANU_FLAG_PLAY_MODE;
                    openPopMenu(loadManuFlag);
                }
                else if (type == ENUM_BOTTOM_MENU_TYPE.ENUM_BOTTOM_MENU_IMAGE_BG_MUSIC)
                {
                    mBottomPopMenuDismissWihtNoAction = false;
                    mBottomPopMenu.dismiss();
                    int loadManuFlag = MANU_FLAG_BACK_MUSIC;
                    openPopMenu(loadManuFlag);
                }
            }
        }
    };
    
    private PopupWindow.OnDismissListener mBottomPopMenuOnDismissListener = new PopupWindow.OnDismissListener()
    {
        @Override
        public void onDismiss()
        {
            if (mImageSwitcher != null && mImagePlaySetHelper != null && false == mBottomPopMenuDismissWihtNoAction)
            {                                
                setAutoPlay();
            }
        }
    };
    //============*结束*底部菜单相关的界面设定及功能实现============//
    
    
    // 获取当前播放图片的设备类型
    protected String getBgAudioDeviceId()
    {
        SharedPreferences sp = getImageSettingsSharedPreferences();
        return sp.getString(PERFS_DEVICE_ID, "");        
    }

    // 设置当前播放图片的设备类型
    protected void saveBgAudioDeviceId(String devId)
    {
        SharedPreferences sp = getImageSettingsSharedPreferences();
        Editor ed = sp.edit();
        ed.putString(PERFS_DEVICE_ID, devId);
        ed.commit();
    }
    // 获取背景图片的所有url信息
    protected Set<String> getBgAudioUrls()
    {
        SharedPreferences sp = getImageSettingsSharedPreferences();        
        return sp.getStringSet(PERFS_BG_AUDIO_URLS, null);        
    }

    // 设置背景图片的所有url信息
    protected void saveBgAudioUrls(Set<String> urls)
    {
        SharedPreferences sp = getImageSettingsSharedPreferences();
        Editor ed = sp.edit();
        ed.putStringSet(PERFS_BG_AUDIO_URLS, urls);
        ed.commit();
    }
    
    protected SharedPreferences getImageSettingsSharedPreferences()
    {
        return getSharedPreferences(BG_MUSIC, Context.MODE_PRIVATE);
    }
        
    protected boolean isFileExist(String url)
    {
        File file = new File(url);
        if (file.exists())
        {
            return true;
        }
        return false;
    }
    
    protected boolean isListEmpty(List<LocalMediaInfo> lists)
    {
        if (lists == null  || lists.size() == 0)
        {
            return true;
        }
        return false;
    }
	
	//begin add by caochao for  DTS2014111107357 随心控推送图片到盒子播放，推送成功后等待5S后，手机端提示“远端服务已经停止”
	private void syncPositionInfo()
	{
		if (mMediaCenterPlayerClient != null)
        {
            mMediaCenterPlayerClient.reportDuration(0, 0);
        }
	}
	//end add by caochao for  DTS2014111107357 随心控推送图片到盒子播放，推送成功后等待5S后，手机端提示“远端服务已经停止”

	@Override
	public void onServiceConnected() {
		
	}

	@Override
	public int getLayoutRes() {
		return R.layout.ip_image_fullscreen;
	}

	@Override
	public void init() {
		mContext = getApplicationContext();
		isNeedRejectKey = PlatformUtil.isGDDX();
		UriTexture.registerShareReceiver(getApplicationContext());
		UriTexture.bCanceled = false;
		mCachePath = getCacheDir().getAbsolutePath();
		mLog.d(TAG, "mCachePath------>" + mCachePath);

		// 海思:默认情况下，Android应用程序的2D部分的buffer是rgb565格式的。要改成32bit
		// buffer，需要在应用程序Activity的onCreate函数中加入：
		getWindow().setFormat(PixelFormat.TRANSLUCENT);

		mContainer = (RelativeLayout) findViewById(R.id.imagecontainer);
		if (mContainer != null) {
			mContainer.requestFocus();
			mContainer.setFocusable(true);
			mContainer.setOnTouchListener(this);
		}

		mOperationTextView = (TextView) findViewById(R.id.operation_text_id);
		setOperationText();
		mIMPRL = (RelativeLayout) findViewById(R.id.fullRelative);
		mDisplayException = (LinearLayout) findViewById(R.id.image_exception);

		mImageErrorInfo = (TextView) findViewById(R.id.text_info);
		mLog.d(TAG, "onCreate -- 2");
		// 左右导航
		mLeftNavigation = findViewById(R.id.leftNavigationImageView);
		mRightNavigation = findViewById(R.id.rightNavigationImageView);

		// 详细信息
		mPicPlayerDetailLayout = (RelativeLayout) findViewById(R.id.detail);
		mPicTitle = (TextView) findViewById(R.id.pic_detail_title);
		mPicPos = (TextView) findViewById(R.id.pic_detail_pos);

		mImageDetailInfo = (LinearLayout) findViewById(R.id.image_detail_info);

		mImagesize = (TextView) findViewById(R.id.image_size);
		mImageTime = (TextView) findViewById(R.id.imageplayer_image_time);
		mImageOrder = (TextView) findViewById(R.id.image_order);
		mLog.d(TAG, " onCreate -- 3");

		// 进度条
		mProgressBar = (ProgressBar) findViewById(R.id.circleProgressBar);
		mOperatingHint = (LinearLayout) findViewById(R.id.Operating_hint);
		if (mProgressBar != null) {
			mProgressBar.setVisibility(View.GONE);
		}

		Animation inleft = AnimationUtils.loadAnimation(this.getApplicationContext(), android.R.anim.fade_in);
		inleft.setDuration(100);

		Animation outright = AnimationUtils.loadAnimation(this.getApplicationContext(), android.R.anim.fade_out);
		outright.setDuration(800);
		if (mLeftNavigation != null) {
			mLeftNavigation.setAnimation(inleft);
			mLeftNavigation.setVisibility(View.GONE);
		}
		if (mRightNavigation != null) {
			mRightNavigation.setAnimation(outright);
			mRightNavigation.setVisibility(View.GONE);
		}

		// Toast初始化
		if (mCanntPreToast == null) {
			mCanntPreToast = Toast.makeText(getApplicationContext(),
					R.string.image_cannot_pre, TOAST_SHOW_TIME);
		}

		if (mCanntNextToast == null) {
			mCanntNextToast = Toast.makeText(getApplicationContext(),
					R.string.image_cannot_next, TOAST_SHOW_TIME);
		}

		if (mStartAutoPlay == null) {
			mStartAutoPlay = Toast.makeText(getApplicationContext(),
					R.string.image_start_autoplay, TOAST_SHOW_TIME);
		}

		if (mStopAutoPlay == null) {
			mStopAutoPlay = Toast.makeText(getApplicationContext(),
					R.string.image_stop_autoplay, TOAST_SHOW_TIME);
		}

		if (mCanntShow == null) {
			mCanntShow = Toast.makeText(getApplicationContext(),
					R.string.image_cannt_display, TOAST_SHOW_TIME);
		}

		if (mRotatedFailed == null) {
			mRotatedFailed = Toast.makeText(getApplicationContext(),
					R.string.image_rotated_failed, TOAST_SHOW_TIME);
		}

		mLog.d(TAG, " onCreate -- 4");
		initPlaySet();
		if (mImageSwitcher == null) {
			mLog.d(TAG, " onCreate -- 5");
			mImageSwitcher = new DLNAImageSwitcher(this);
			mImageSwitcher.init();
			UriTexture.setCacheDir(mCachePath);
			mImageSwitcher.setBackgroundColor(Color.TRANSPARENT);

			mImageSwitcher.setProgress(mProgressBar);
			mImageSwitcher.setOperatingHint(mOperatingHint);
			mImageSwitcher.setAnimateFirstView(false);
			mImageSwitcher.setListener(this);
			mImageSwitcher.setPlayInfo(getPlayStateInfo());
			mImageSwitcher.setInternalPlayer(mbInternalPlayer);

			mLayoutParams = new android.widget.RelativeLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			mLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT,
					RelativeLayout.TRUE);

			if (mIMPRL != null) {
				mIMPRL.addView(mImageSwitcher, mLayoutParams);
			}

			// 注册播放gif图监听
			mImageSwitcher.setGifListener(new OnGifListener() {

				@Override
				public void stopPlay() {
					if (mGifView != null) {
						mGifView.stop();
						mGifView.setVisibility(View.GONE);
						if (mImageSwitcher != null) {
							mImageSwitcher.setVisibility(View.VISIBLE);
						}

						if (mIMPRL != null) {
							mIMPRL.removeView(mGifView);
						}
						mGifView = null;
					}
				}

				@Override
				public void startPlay() {
				}

				@Override
				public void setGifImage(GifOpenHelper gif) {
					if (gif != null) {
						mLog.d(TAG, "switcher gif");
						if (mGifView != null) {
							mIMPRL.removeView(mGifView);
							// mGifView.free();
							mGifView = null;
						}

						mGifView = new GifView(getApplicationContext());
						if (mIMPRL != null) {
							mIMPRL.addView(mGifView, mLayoutParams);
						}
						mGifView.setGifOpenHelper(gif);
						if (mImageSwitcher != null) {
							mImageSwitcher.setVisibility(View.INVISIBLE);
						}
					}

				}
			});

		}
		if (mMediaCenterPlayerClient != null) {
			mMediaCenterPlayerClient.play();
		}
		mDetector = new GestureDetector(this);
	}
	
	/**
	 * 释放背景音乐播放器
	 */
	private void releaseBackMusicPlayer(){
		if(mBackMusicPlayer != null){
			try{
				mBackMusicPlayer.release();
			}catch (Exception e){
				//no handle
			}
			mBackMusicPlayer = null;
		}
	}
	
	/**
	 * 保存背景音乐
	 */
	private void saveBackMusic(List<FileInfo> fileInfos){
		try{
			if(fileInfos != null && fileInfos.size() > 0)
				SharedUtils.saveValue(BACK_MUSIC_NAME, BACK_MUSIC_KEY, JsonUtils.listToJsonArray(fileInfos).toString());
			else
				SharedUtils.saveValue(BACK_MUSIC_NAME, BACK_MUSIC_KEY, "");
		}catch (Exception e){
			//no handle
			Log.e(TAG, "saveBackMusic->exception:" + e);
		}
	}
	
	/**
	 * 加载背景音乐
	 */
	private void loadBackMusic(){
		try{
			String value = SharedUtils.getValue(BACK_MUSIC_NAME, BACK_MUSIC_KEY);
			if(!TextUtils.isEmpty(value)){
				JSONArray array  = new JSONArray(value);
				mBackMusicInfos = (List<FileInfo>)JsonUtils.arrayToList(FileInfo.class, array);
			}
		}catch (Exception e){
			Log.e(TAG, "loadBackMusic->exception:" + e);
		}
		
	}
	
	/**
	 * 移除当前播放的背景音乐
	 */
	private void removeCurrBackMusic(){
		//移除当前播放
		if(mBackMusicInfos != null && mBackMusicInfos.size() > 0){
			int currPlayIndex = (mBackMusicPlayPosition - 1 + mBackMusicInfos.size()) % mBackMusicInfos.size();
			mBackMusicInfos.remove(currPlayIndex);
		}
	}
}
