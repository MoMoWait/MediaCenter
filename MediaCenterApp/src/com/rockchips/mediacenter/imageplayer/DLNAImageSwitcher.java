/**
 * 
 * com.rockchips.iptv.stb.dlna.player
 * DLNAImageSwitcher.java
 * 
 * 2011-11-2-上午08:28:25
 * Copyright 2011 Huawei Technologies Co., Ltd
 * 
 */
package com.rockchips.mediacenter.imageplayer;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ViewSwitcher;

import com.rockchips.mediacenter.R;
import com.rockchips.mediacenter.bean.LocalMediaInfo;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.utils.DateUtil;
import com.rockchips.mediacenter.utils.IICLOG;
import com.rockchips.mediacenter.utils.Performance;
import com.rockchips.mediacenter.utils.StringUtils;
import com.rockchips.mediacenter.bean.PlayStateInfo;
import com.rockchips.mediacenter.utils.GifOpenHelper;
import com.rockchips.mediacenter.view.GifView;
import com.rockchips.mediacenter.service.OnGifListener;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.rockchips.mediacenter.imageplayer.image.CurlDownload;
import com.rockchips.mediacenter.imageplayer.image.ImageUtils;
import com.rockchips.mediacenter.imageplayer.image.ParseImage;
import com.rockchips.mediacenter.imageplayer.image.UriTexture;

/**
 * 
 * DLNAImageSwitcher
 * 
 * AR-0000698422 图片内容的播放功能
 * 
 * 2011-11-2 上午08:28:25
 * 
 * @version 1.0.0
 * 
 */
public class DLNAImageSwitcher extends ImageSwitcher implements
        ViewSwitcher.ViewFactory, DLNAImageSwitcherViewFactory {
    private static final String TAG = "MediaCenterApp";

    private IICLOG mLog = IICLOG.getInstance();

    // 播放
    protected static final int MSG_DLNA_IS_PLAY = 0;

    // 向前play
    protected static final int MSG_DLNA_IS_PRE = 1;

    // 向后play
    protected static final int MSG_DLNA_IS_NEXT = 2;

    // 自动播放
    protected static final int MSG_DLNA_IS_AUTO_PLAY = 3;

    // 停止自动播放
    protected static final int MSG_DLNA_IS_STOP_AUTO_PLAY = 4;

    // 显示图片
    protected static final int MSG_DLNA_UI_IS_SHOWPIC = 5;

    // 显示当前index图片
    protected static final int MSG_DLNA_IS_CUR = 6;

    // 重置图片动画
    protected static final int MSG_DLNA_UI_RESET_ROTATE = 18;

    protected static final int MSG_DLNA_UI_SHOWEXCEPTION = 19;

    /**
     * 下载handler的消息
     */
    public static final int MSG_DOWNLOAD_PIC = 7;

    public static final int MSG_DOWNLOAD_CLOUD_PIC = 22;

    public static final int MSG_DOWNLOAD_CANCEL = 8;

    protected static final int MSG_DLNA_UI_IS_BEFORE_PLAY = 9;

    protected static final int MSG_DLNA_UI_IS_AFTER_PLAY = 10;

    protected static final int MSG_DLNA_UI_IS_CANNT_PRE = 11;

    protected static final int MSG_DLNA_UI_IS_CANNT_NEXT = 12;

    protected static final int MSG_DLNA_UI_IS_CANNT_SHOW = 13;

    protected static final int MSG_DLNA_IS_ROTATION = 14;

    protected static final int MSG_DLNA_UI_SET_FIX_CENTER = 15;

    protected static final int MSG_DLNA_UI_SET_CENTER_INSIDE = 16;

    protected static final int MSG_DLNA_UI_SET_IMAGE_DETAIL = 17;

    protected static final int MSG_DLNA_INVALIDATECACHE = 20;

    // 播放gif图
    protected static final int MSG_DLNA_UI_PLAY_GIF = 30;

    // 是否是内置播放器标志位 是--true 否--false
    private boolean mbInternalPlayer = false;

    /**
     * 动画类型
     */
    public static final int ANIMATION_TYPE_NO = -1;

    public static final int ANIMATION_TYPE_FADE = 0;

    public static final int ANIMATION_TYPE_LEFT_IN_RIGHT_OUT = 1;

    public static final int ANIMATION_TYPE_RIGHT_IN_LEFT_OUT = 2;

    // 是否在播放云相册图片默认为false
    private boolean mbPlayCloudPic = false;

    // 图片显示最大值
    public static final int BITMAP_MAX_W = 1280;

    public static final int BITMAP_MAX_H = 1080;

    // 小图片
    private static final int BITMAP_MAX_W_S = 1080;

    private static final int BITMAP_MAX_H_S = 720;

    // 延时发送显示转圈信息时间
    public static final int DELAY_DISPLAY_PROGRESS_TIME = 1500;

    private static final int HIDDEN_HINT_LAYOUT = 1;

    private DLNAISHandler mDLNAISHandler = null;

    private HandlerThread mHandlerThread = null;

    // 是否按了左右键
    private boolean mbPressLeftOrRight = false;

    /**
     * 设置动画标志位 默认为 1是左进右出 2 是淡入淡出
     */
    private int mAnimationWith = 1;

    /**
     * ImagePlaySetHelper
     */
    private ImagePlaySetHelper mImagePlaySetHelper;

    /**
     * 回调接口
     */
    private DLNAImageSwitcherListener mListener = null;

    /**
     * gif图片操作回调接口
     */
    private OnGifListener mGifListener = null;

    /**
     * 应用程序上下文保存
     */
    private Context mContext = null;

    /**
     * 播放数据
     */
    private PlayStateInfo mPlayStateInfo = null;

    /**
     * 显示图片的view
     */
    private ImageView mIv = null;

    /**
     * image没显示的时候显示progressbar
     */
    private ProgressBar mProgressBar = null;

    /**
     * 刚进入播放器时的操作提示
     */
    private LinearLayout mOperatingHint = null;

    /**
     * 标志 是否正在下载图片
     */
    private boolean mIsDownLoadBigDrawble = false;

    /**
     * 标志 是否正在加载图片并切换
     */
    private boolean mIsLoadingImage = false;

    /**
     * 正常切换
     */
    private static final int NORMAL_SWITCHING = 1;

    /**
     * 百叶窗切换
     */
    private static final int BLINDWINDOW_SWITCHING = 2;

    private int mSwitchingModel = NORMAL_SWITCHING;

    /**
     * 显示图片的ImageView
     */
    private ImageView mImageView;

    /**
     * 图片旋转的度数
     */
    private int mDegree = 0;

    /**
     * 是否正循环下载云相册图片
     */
    private boolean mbCycledDownload = false;

    /**
     * 云相册图片 url
     */
    private String mImageUrl;

    /**
     * 是否正下载云相册图片
     */
    public boolean isLoading = false;

    /**
     * 开源下载包
     */
    protected ImageLoader imageLoader = ImageLoader.getInstance();

    // zkf61715
    private int[] defaultAudioBgPic =
    { R.drawable.default_audio_bg_a };

    private int defaultAudioBgPicIndex = 0;

    /**
     * gif图片最大允许界面大小:2M
     */
    private static final long GIF_MAX_SIZE = 5 * 1024 * 1024;
    
    private boolean isFirstImageShown;

    /**
     * constructor DLNAImageSwitcher.
     * 
     * @param context
     * @param attrs
     */
    public DLNAImageSwitcher(Context context, AttributeSet attrs) {
        super(context, attrs);
        mLog.e(TAG, "----------------------> DLNAImageSwitcher C 3");
        mContext = context;
        UriTexture.dlnaImageSwitcherViewFactory = this;
        CurlDownload.dlnaImageSwitcherViewFactory = this;
        initCFG();
    }

    /**
     * constructor DLNAImageSwitcher.
     * 
     * @param context
     */
    public DLNAImageSwitcher(Context context) {
        super(context);
        mLog.e(TAG, "----------------------> DLNAImageSwitcher C 2");
        mContext = context;
        UriTexture.dlnaImageSwitcherViewFactory = this;
        CurlDownload.dlnaImageSwitcherViewFactory = this;
        UriTexture.mContext = context;
        initImageLoader(mContext);
        initCFG();
    }

    DisplayImageOptions options;

    public static void initImageLoader(Context context) {
        // This configuration tuning is custom. You can tune every option, you
        // may tune some of them,
        // or you can create default configuration by
        // ImageLoaderConfiguration.createDefault(this);
        // method.
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                context).threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .discCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO).enableLogging()
                // Not necessary in common
                .build();
        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);
    }

    private void initCFG() {
        options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.ic_launcher)
                .showImageOnFail(R.drawable.ic_launcher)
                .resetViewBeforeLoading().cacheOnDisc()
                .imageScaleType(ImageScaleType.EXACTLY)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new FadeInBitmapDisplayer(300)).build();

    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what)
            {
            // 隐藏操作提示hint
                case HIDDEN_HINT_LAYOUT:
                    if (mOperatingHint != null) {
                        mOperatingHint.setVisibility(View.INVISIBLE);
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public void init() {
        mLog.e(TAG, "----------------------> DLNAImageSwitcher init 1");
        isFirstImageShown = false;
        // set cache directory for UriTexture
        if (mHandlerThread == null) {
            mHandlerThread = new HandlerThread("DLNAImageSwitcher");
            mHandlerThread.start();
        }

        if (mDLNAISHandler == null) {
            mLog.e(TAG, "----------------------> DLNAImageSwitcher init 2");

            mDLNAISHandler = new DLNAISHandler(mHandlerThread.getLooper());
        }

        removeAllViews();

        setFactory(this);

        setAnimateFirstView(true);
        setAnimationCacheEnabled(true);
        setAlwaysDrawnWithCacheEnabled(true);

        setInAnimation(null);
        setOutAnimation(null);
        mImagePlaySetHelper = new ImagePlaySetHelper(mContext);
    }

    public void uninit() {

        mLog.d(TAG, "uninit");

        setRun(false);

        if (wrfbm != null) {
            BitmapDrawable bd = (BitmapDrawable) wrfbm;
            if (!bd.getBitmap().isRecycled()) {
                bd.getBitmap().recycle();
            }
            bd = null;
            // SoftReference<Drawable> srf = new SoftReference<Drawable>(wrfbm);
            wrfbm = null;
            // srf = null;
        }

        mDLNAISHandler.getLooper().quit();

        mDLNAISHandler = null;
        mHandlerThread = null;
        mProgressBar = null;
        mListener = null;
        uiHandler = null;

        System.gc();
    }

    public void stopDLNAHandler() {
        if (null != mDLNAISHandler) {
            setRun(false);
        }

        if (mHandlerThread != null) {
            // 停止Looper循环
            mHandlerThread.getLooper().quit();
            mHandlerThread.quit();

            mHandlerThread = null;
        }
    }

    public void setListener(DLNAImageSwitcherListener listener) {
        mListener = listener;
    }

    public void setGifListener(OnGifListener listener) {
        mGifListener = listener;
    }

    public void setSwitchingModel(int model) {
        mSwitchingModel = model;
    }

    private Drawable wrfbm = null;

    /****************************************************
     * 播放处理handler 开始
     ****************************************************/
    public Handler uiHandler = new Handler() {
        /**
         * 处理消息
         */
        public void handleMessage(android.os.Message msg) {

            LocalMediaInfo mbi = null;
            switch (msg.what)
            {
            // 下载云相册图片
                case MSG_DOWNLOAD_CLOUD_PIC:
                    mLog.d(TAG, "MSG_DOWNLOAD_CLOUD_PIC IN");
                    removeMessages(MSG_DOWNLOAD_CLOUD_PIC);
                    if (msg.obj == null) {
                        mLog.d(TAG, "MSG_DOWNLOAD_PIC obj is null");
                        if (uiHandler != null) {
                            uiHandler
                                    .sendEmptyMessage(MSG_DLNA_UI_IS_AFTER_PLAY);
                        }
                        return;
                    }
                    LocalMediaInfo mfi = (LocalMediaInfo) msg.obj;
                    String urlStr = mfi.getUrl();
                    if (StringUtils.isEmpty(urlStr)) {
                        if (uiHandler != null) {
                            uiHandler
                                    .sendEmptyMessage(MSG_DLNA_UI_IS_AFTER_PLAY);
                        }
                        return;
                    }
                    urlStr = getExistLocalPath(mfi);
                    mfi.setmData(urlStr);
                    if (uiHandler != null) {
                        uiHandler
                                .sendEmptyMessage(MSG_DLNA_UI_SET_IMAGE_DETAIL);
                    }

                    setImageUrl(urlStr);
                    LoadBmpDrawable(urlStr, mfi.getmDeviceType());
                    break;
                case MSG_DLNA_UI_IS_BEFORE_PLAY:
                    mLog.d(TAG, "MSG_DLNA_UI_IS_BEFORE_PLAY");
                    if (mProgressBar != null) {
                        uiHandler.removeMessages(MSG_DLNA_UI_IS_AFTER_PLAY);

                        // 没有播放图片前设置进度条为可见
                        mProgressBar.setVisibility(View.VISIBLE);
                    }

                    break;

                case MSG_DLNA_UI_IS_AFTER_PLAY:
                {
                    mLog.d(TAG, "MSG_DLNA_UI_IS_AFTER_PLAY");
                    if (mProgressBar != null) {
                        uiHandler.removeMessages(MSG_DLNA_UI_IS_BEFORE_PLAY);
                        // 播放图片后设置进度条为不可见
                        mProgressBar.setVisibility(View.GONE);
                    }

                    if (mPlayStateInfo != null
                            && mPlayStateInfo.getMediaList() != null) {
                        int curIndex = mPlayStateInfo.getCurrentIndex();
                        int total = mPlayStateInfo.getMediaList().size();
                        mbi = mPlayStateInfo.getCurrentMediaInfo();
                        if (mbi != null) {
                            if (mListener != null) {
                                mListener.showDetail(mbi, curIndex, total);
                            }
                        }
                    }

                    break;
                }
                case MSG_DLNA_UI_RESET_ROTATE:
                    resetRotate();
                    mLog.i(TAG, "MSG_DLNA_UI_RESET_ROTATE E");
                    break;
                case MSG_DLNA_UI_IS_CANNT_PRE:
                    mLog.i(TAG, "MSG_DLNA_UI_IS_CANNT_PRE B");
                    if (mListener != null) {
                        mLog.i(TAG, "MSG_DLNA_UI_IS_CANNT_PRE 1");
                        mListener.cantMovePrevious();
                    }
                    mLog.d(TAG, "MSG_DLNA_UI_IS_AFTER_PLAY");
                    sendEmptyMessage(MSG_DLNA_UI_IS_AFTER_PLAY);
                    mLog.i(TAG, "MSG_DLNA_UI_IS_CANNT_PRE E");
                    break;

                case MSG_DLNA_UI_IS_CANNT_NEXT:

                    if (mListener != null) {
                        mListener.cantMoveNext();
                    }
                    mLog.d(TAG, "MSG_DLNA_UI_IS_AFTER_PLAY");
                    sendEmptyMessage(MSG_DLNA_UI_IS_AFTER_PLAY);
                    break;

                case MSG_DLNA_UI_IS_CANNT_SHOW:// 各种原因导致,无法解码，无法下载

                    if (mListener != null) {
                        mListener.canntshowImage();
                    }
                    mLog.d(TAG, "MSG_DLNA_UI_IS_AFTER_PLAY");
                    sendEmptyMessage(MSG_DLNA_UI_IS_AFTER_PLAY);

                    break;
                case MSG_DLNA_UI_SET_FIX_CENTER:
                    if (mImageView != null) {
                        mImageView.setScaleType(ScaleType.FIT_CENTER);
                    }
                    break;
                case MSG_DLNA_UI_SET_CENTER_INSIDE:
                    if (mImageView != null) {
                        mImageView.setScaleType(ScaleType.CENTER_INSIDE);
                    }
                    break;
                case MSG_DLNA_UI_SET_IMAGE_DETAIL:
                    if (mPlayStateInfo != null) {
                        int curIndex = mPlayStateInfo.getCurrentIndex();
                        int total = mPlayStateInfo.getMediaList().size();
                        mbi = mPlayStateInfo.getCurrentMediaInfo();
                        if (mbi != null) {
                            if (mListener != null) {
                                mListener.showDetail(mbi, -100, total);
                            }
                        }
                    }
                    break;
                case MSG_DLNA_UI_SHOWEXCEPTION:
                    if (mListener != null) {
                        if (msg.arg1 == 1) {
                            mListener.setCantDisplay(true);
                        } else if (msg.arg1 == 2) {
                            mListener.setCantDisplay(false);
                        }
                    }
                    break;
                case MSG_DLNA_UI_IS_SHOWPIC:
                    mLog.d(TAG,
                            "onKeyUp----------->10-->"
                                    + DateUtil.getCurrentTime());
                    if (mDLNAISHandler == null || (isFirstImageShown && !isRun())) {
                        this.removeMessages(MSG_DLNA_UI_IS_AFTER_PLAY);// kbtest
                        sendEmptyMessage(MSG_DLNA_UI_IS_AFTER_PLAY);

                        return;
                    }
                    
                    isFirstImageShown = true;

                    mLog.i(TAG, "MSG_DLNA_UI_IS_SHOWPIC B");

                    if (mProgressBar != null) {
                        mProgressBar.setVisibility(View.GONE);
                    }
                    // zkf61715
                    if (showDefaultPic) {
                        if (defaultAudioBgPicIndex > defaultAudioBgPic.length - 1) {
                            defaultAudioBgPicIndex = 0;
                        }

                        View view = getNextView();
                        view.setPadding(0, 0, 0, 0);
                        setImageResource(defaultAudioBgPic[defaultAudioBgPicIndex++]);
                        if (mDLNAISHandler.isAutoPlay()
                                && defaultAudioBgPic.length > 1) {
                            // 设置动画
                            /*
                             * BEGIN: Modified by c00224451 for DTS2014021408267
                             * 2014/2/19
                             */
                            setAnimationParams();
                            /*
                             * END: Modified by c00224451 for DTS2014021408267
                             * 2014/2/19
                             */
                            sendEmptyMessageDelayed(MSG_DLNA_UI_IS_SHOWPIC,
                                    mDLNAISHandler.getInterval());
                        }
                        break;
                    }

                    // tss add
                    mbi = mPlayStateInfo.getCurrentMediaInfo();
                    if (mbi != null
                            && mbi.getmDeviceType() == ConstData.DeviceType.DEVICE_TYPE_CLOUD) {
                        mLog.e(TAG, "MSG_DLNA_UI_IS_SHOWPIC bCanceled UriTexture.bCanceled:" + UriTexture.bCanceled);

                        if (UriTexture.bCanceled) {
                            UriTexture.bCanceled = false;

                            return;
                        }

                        UriTexture.bCanceled = false;
                    }

                    if (wrfbm != null) {
                        SoftReference<Drawable> srf = new SoftReference<Drawable>(
                                wrfbm);
                        wrfbm = null;
                        srf = null;
                    }

                    if (msg.obj == null) {
                        mLog.e(TAG, "MSG_DLNA_UI_IS_SHOWPIC msg.obj == null");
                        sendEmptyMessage(MSG_DLNA_UI_IS_AFTER_PLAY);

                        // 停止gif动画
                        if (mGifListener != null) {
                            mGifListener.stopPlay();
                        }
                        // 切换图片
                        setImageDrawable(null);

                        mListener.setCantDisplay(true);
                        if (mDLNAISHandler.isAutoPlay()) {
                            //
                            // mDLNAISHandler.setRun(true);

                            Message msgauto = Message.obtain();
                            msgauto.what = MSG_DLNA_IS_AUTO_PLAY;
                            msgauto.arg1 = mDLNAISHandler.getInterval();
                            mDLNAISHandler.sendMessageDelayed(msgauto, 500);

                            mLog.i(TAG,
                                    "MSG_DLNA_UI_IS_SHOWPIC MSG_DOWNLOAD_PIC isAutoPlay interval:"
                                            + mDLNAISHandler.getInterval());
                        }

                        // Added by zhaomingyang 00184367 --- Begin
                        mListener.canntshowImage();
                        // Added by zhaomingyang 00184367 --- End

                        return;
                    } else {
                        mListener.setCantDisplay(false);
                    }
                    BitmapDrawable da = null;
                    String url = null;
                    int degreeT = 0;
                    ParseImage parseImage = (ParseImage) msg.obj;
                    if (parseImage != null) {
                        SoftReference<Drawable> sfrDrawable = parseImage
                                .getSrfBitmap();
                        if (sfrDrawable != null && sfrDrawable.get() != null) {
                            da = (BitmapDrawable) sfrDrawable.get();
                        }
                        url = parseImage.getUrl();
                        degreeT = parseImage.getDegree();
                    }

                    if (da == null) {
                        mLog.d(TAG, "MSG_DLNA_UI_IS_SHOWPIC da == null");
                        sendEmptyMessage(MSG_DLNA_UI_IS_AFTER_PLAY);

                        // 停止gif动画
                        if (mGifListener != null) {
                            mGifListener.stopPlay();
                        }
                        // 切换图片
                        setImageDrawable(null);

                        mLog.d(TAG,
                                "MSG_DLNA_UI_IS_SHOWPIC UriTexture.mbStop--->"
                                        + UriTexture.getMbStop());
                        if (!UriTexture.getMbStop()) {
                            mListener.setCantDisplay(true);
                        }
                        UriTexture.setMbStop(false);
                        mLog.d(TAG, "mDLNAISHandler.isAutoPlay()9999--->"
                                + mDLNAISHandler.isAutoPlay());
                        if (mDLNAISHandler.isAutoPlay()) {
                            //
                            // mDLNAISHandler.setRun(true);
                            mLog.d(TAG, "mDLNAISHandler.isAutoPlay()-555->");
                            Message msgauto = Message.obtain();
                            msgauto.what = MSG_DLNA_IS_AUTO_PLAY;
                            msgauto.arg1 = mDLNAISHandler.getInterval();
                            mDLNAISHandler.sendMessageDelayed(msgauto, 500);

                            mLog.i(TAG,
                                    "MSG_DLNA_UI_IS_SHOWPIC MSG_DOWNLOAD_PIC isAutoPlay interval-3333:"
                                            + mDLNAISHandler.getInterval());
                        }

                        // Added by zhaomingyang 00184367 --- Begin
                        mListener.canntshowImage();
                        // Added by zhaomingyang 00184367 --- End
                        UriTexture.bCanceled = false;
                        return;
                    } else {
                        mListener.setCantDisplay(false);
                    }

                    if (da.getBitmap() == null) {
                        mLog.e(TAG,
                                "MSG_DLNA_UI_IS_SHOWPIC da.getBitmap() == null");
                        sendEmptyMessage(MSG_DLNA_UI_IS_AFTER_PLAY);

                        // 停止gif动画
                        if (mGifListener != null) {
                            mGifListener.stopPlay();
                        }
                        // 切换图片
                        setImageDrawable(null);

                        mListener.setCantDisplay(true);
                        if (mDLNAISHandler.isAutoPlay()) {
                            //
                            // mDLNAISHandler.setRun(true);

                            Message msgauto = Message.obtain();
                            msgauto.what = MSG_DLNA_IS_AUTO_PLAY;
                            msgauto.arg1 = mDLNAISHandler.getInterval();
                            mDLNAISHandler.sendMessageDelayed(msgauto, 500);

                            mLog.i(TAG,
                                    "MSG_DLNA_UI_IS_SHOWPIC MSG_DOWNLOAD_PIC isAutoPlay interval:"
                                            + mDLNAISHandler.getInterval());
                        }

                        // Added by zhaomingyang 00184367 --- Begin
                        mListener.canntshowImage();
                        // Added by zhaomingyang 00184367 --- End

                        return;
                    } else {
                        mListener.setCantDisplay(false);
                    }
                    sendEmptyMessage(MSG_DLNA_UI_IS_AFTER_PLAY);

                    wrfbm = new BitmapDrawable(da.getBitmap());

                    if (wrfbm != null) {
                        mLog.d(TAG,
                                "MSG_DLNA_UI_IS_SHOWPIC wrfbm.get() != null ");
                        // add by zouzhiyi 显示之前判断是否要旋转---start
                        mLog.d(TAG,
                                "parseRotate start  time--->"
                                        + DateUtil.getCurrentTime());
                        if (url != null) {
                            UriTexture.parseRotate(degreeT, getNextView(),
                                    getCurrBitmapWith(), getCurrBitmapHeight());

                            // 清除缓存处理
                            Message cacheMsg = obtainMessage();
                            cacheMsg.what = MSG_DLNA_INVALIDATECACHE;
                            cacheMsg.obj = url;
                            mDLNAISHandler.sendMessage(cacheMsg);

                            // 清除缓存处理
                            // long crc64 = Utils.Crc64Long(url);
                            // UriTexture.invalidateCache(crc64,
                            // UriTexture.BITMAP_MAX_W);
                        }
                        mLog.d(TAG,
                                "parseRotate end  time--->"
                                        + DateUtil.getCurrentTime());
                        // add by zouzhiyi 显示之前判断是否要旋转---end

                        // 停止gif动画
                        if (mGifListener != null) {
                            mGifListener.stopPlay();
                        }

                        // 设置ImageSwitcher的子View(ImageView)的显示区域。
                        // 解决由GifOpenHelper类解码出来的Bitmap，显示有时会被拉伸的问题。
                        Bitmap bitmap = da.getBitmap();
                        View view = getNextView();
                        if (bitmap.getWidth() < getWidth()
                                && bitmap.getHeight() < getHeight()) {
                            int bW = bitmap.getWidth();
                            int bH = bitmap.getHeight();
                            int hp = (getWidth() - bW) / 2;
                            int vp = (getHeight() - bH) / 2;
                            view.setPadding(hp, vp, hp, vp);
                        } else {
                            view.setPadding(0, 0, 0, 0);
                        }

                        // 切换图片
                        setImageDrawable(wrfbm);
                    }
                    mLog.d(TAG,
                            "onKeyUp----------->121212-->"
                                    + DateUtil.getCurrentTime());
                    da = null;
                    url = null;
                    if (mPlayStateInfo != null
                            && mPlayStateInfo.isFirstElement()) {
                        mLog.d(TAG, "MSG_DLNA_UI_IS_SHOWPIC - 2");
                        if (mListener != null) {
                            mListener.arriveFirstElement(true);
                        }
                    } else {
                        mLog.d(TAG, "MSG_DLNA_UI_IS_SHOWPIC - 3 1");
                        if (mListener != null) {
                            mListener.arriveFirstElement(false);
                        }
                    }

                    if (mPlayStateInfo != null
                            && mPlayStateInfo.isLastElement()) {
                        mLog.d(TAG, "MSG_DLNA_UI_IS_SHOWPIC - 4");

                        if (mListener != null) {
                            mListener.arriveLastElement(true);
                        }
                    } else {
                        mLog.d(TAG, "MSG_DLNA_UI_IS_SHOWPIC - 5 1");
                        if (mListener != null) {
                            mListener.arriveLastElement(false);
                        }
                    }
                    int curIndex = 0;
                    if (mPlayStateInfo != null) {
                        curIndex = mPlayStateInfo.getCurrentIndex();
                    }
                    int total = 0;
                    if (mPlayStateInfo != null) {
                        total = mPlayStateInfo.getMediaList().size();
                        mbi = mPlayStateInfo.getCurrentMediaInfo();
                    }
                    if (mbi != null) {
                        if (mListener != null) {
                            mListener.showDetail(mbi, curIndex, total);
                        }
                    }

                    if (mListener != null) {
                        mListener.showImageEnd();
                    }
                    mLog.d(TAG, "mDLNAISHandler.isAutoPlay()---7"
                            + mDLNAISHandler.isAutoPlay());
                    if (mDLNAISHandler.isAutoPlay()) {
                        //
                        // mDLNAISHandler.setRun(true);

                        Message msgauto = Message.obtain();
                        msgauto.what = MSG_DLNA_IS_AUTO_PLAY;
                        msgauto.arg1 = mDLNAISHandler.getInterval();
                        mDLNAISHandler.sendMessageDelayed(msgauto, 500);

                        mLog.i(TAG,
                                "MSG_DLNA_UI_IS_SHOWPIC MSG_DOWNLOAD_PIC isAutoPlay interval:"
                                        + mDLNAISHandler.getInterval());
                    }

                    mLog.i(TAG, "MSG_DLNA_UI_IS_SHOWPIC E");

                    break;

                case MSG_DLNA_UI_PLAY_GIF:

                    // 播放gif图
                    if (mGifListener != null) {
                        GifOpenHelper gif = (GifOpenHelper) msg.obj;
                        mGifListener.setGifImage(gif);
                    }
                    break;
                default:
                    break;
            }

            super.handleMessage(msg);
        };
    };

    /**
     * <一句话功能简述>解码gif图片 <功能详细描述>
     * @param url
     * @param type
     * @param name
     * @return
     * @see [类、类#方法、类#成员]
     */
    private GifOpenHelper getGif(String url, int devType, String name) {
        if (StringUtils.isEmpty(url)) {
            mLog.e(TAG, "getGif url is null");
            return null;
        }

        GifOpenHelper gif = UriTexture.createGifFromUrl(mContext, url,
                BITMAP_MAX_W, BITMAP_MAX_H, devType, name, GIF_MAX_SIZE);

        return gif;
    }

    // modify by xkf76249 增加一个图片名字参数
    private SoftReference<Drawable> getBmpDrawable(String url, int type,
            int fileSize, String name) {
        mLog.d(TAG, "getBmpDrawable --In");
        mLog.d(TAG, "getBmpDrawable url:" + url + " type:" + type);

        if (StringUtils.isEmpty(url)) {
            mLog.e(TAG, "getBmpDrawable url is null");
            return null;
        }

        int iRetryTimes = 0;

        // 云相册图片尝试下载5次
        if (type == ConstData.DeviceType.DEVICE_TYPE_CLOUD) {
            iRetryTimes = 4;
        } else {
            iRetryTimes = 0;
        }

        int bmpMaxW = BITMAP_MAX_W;
        int bmpMaxH = BITMAP_MAX_H;

        int bmpMaxW_S = BITMAP_MAX_W_S;
        int bmpMaxH_S = BITMAP_MAX_H_S;

        while (iRetryTimes-- >= 0) {
            mLog.d(TAG, "iRetryTimes---->:" + iRetryTimes);

            mbCycledDownload = true;
            SoftReference<Bitmap> srBmp = null;

            if (type == ConstData.DeviceType.DEVICE_TYPE_CLOUD) {
                // 云相册只提示加载失败
                CurlDownload.mCurlCode = 0;
            }
            try {

                // modify by xkf76249 增加一个图片名字参数
                srBmp = UriTexture.createFromUri(mContext, url, bmpMaxW,
                        bmpMaxH, 0, null, type, fileSize, name);
                // 在播放前判断图片的角度是否正常，是否需要旋转调整角度 注释 modified by zouzhiyi
                // mDegree = UriTexture.getOrientation(url,
                // UriTexture.BITMAP_MAX_W);

                if (srBmp == null) {
                    mLog.d(TAG, "getBmpDrawable --> srBmp == null");
                } else {
                    mLog.d(TAG, "getBmpDrawable --> srBmp != null");
                    setPressLeftOrRight(true);
                }
            } catch (OutOfMemoryError e) {
                mLog.d(TAG, "getBmpDrawable --> OutOfMemoryError");
                mLog.w(TAG, e.getLocalizedMessage());
                srBmp = null;

            } catch (IOException e) {
                srBmp = null;

            } catch (URISyntaxException e) {

            }

            if (srBmp == null) {

                // 如果没按左右键或者播放完成后按左右键时都会走这里
                if (!isPressLeftOrRight() && !UriTexture.bCanceled) {
                    mLog.d(TAG, "getBmpDrawable --->tre again");
                    continue;
                } else {
                    setPressLeftOrRight(false);
                    mLog.d(TAG, "getBmpDrawable --->return null");
                    return null;
                }
            } else {
                mLog.d(TAG, "getBmpDrawable --> OK");

                BitmapDrawable bmpDraw = new BitmapDrawable(srBmp.get());
                SoftReference<Drawable> wrf = new SoftReference<Drawable>(
                        bmpDraw);
                bmpDraw = null;

                return wrf;
            }

        }

        mLog.d(TAG, "getBmpDrawable --Out");
        return null;

    }

    private boolean mbRun = false;

    // 记录当前在播放的URL
    public String mCurrentShowUrl = null;

    /**
     * 是否我大分辨率图片
     */
    private boolean mbLarger = true;

    /**
     * @param bRun the bRun to set
     */
    public void setRun(boolean bRun) {
        mbRun = bRun;
    }

    /**
     * bRun
     * 
     * @return the bRun
     * @since 1.0.0
     */

    public boolean isRun() {
        return mbRun;
    }

    /**
     * 判断url是否与正在播放的是同一个url isTheSameUrl
     * 
     * @param url
     * @return boolean
     * @exception
     */
    public boolean isTheSameUrl(String url) {
        if (StringUtils.isNotEmpty(mCurrentShowUrl)
                && StringUtils.isNotEmpty(url)) {
            if (mCurrentShowUrl.equalsIgnoreCase(url)) {
                return true;
            }
        }

        return false;
    }

    public class DLNAISHandler extends Handler {

        private boolean mbAutoPlay = false;

        private int interval = 5000;

        /**
         * constructor DLNAImageSwitcher.DownloadHandler.
         * 
         */
        public DLNAISHandler(Looper looper) {
            super(looper);
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.os.Handler#handleMessage(android.os.Message)
         */
        @Override
        public void handleMessage(Message msg) {
            mLog.d(TAG, "handleMessage - msg CODE :" + msg.what);

            if (!isRun() && !isPlayCloudPic()) {
                mLog.w(TAG, "!isRun()");

                mLog.d(TAG, "handleMessage - isRun false");

                super.handleMessage(msg);

                return;
            }

            Message msgDraw = null;

            switch (msg.what)
            {
                case MSG_DOWNLOAD_PIC:
                    mLog.d(TAG, "MSG_DOWNLOAD_PIC IN");

                    if (msg.obj == null) {
                        mLog.d(TAG, "MSG_DOWNLOAD_PIC obj is null");

                        if (uiHandler != null) {
                            uiHandler
                                    .sendEmptyMessage(MSG_DLNA_UI_IS_AFTER_PLAY);
                        }

                        return;
                    }

                    setRun(false);

                    LocalMediaInfo mbi = (LocalMediaInfo) msg.obj;
                    String url = mbi.getUrl();
                    mLog.d(TAG,
                            "onKeyUp----------->555-->"
                                    + DateUtil.getCurrentTime());
                    mLog.d(TAG, "MSG_DOWNLOAD_PIC url = " + url);
                    if (StringUtils.isEmpty(url)) {
                        mLog.d(TAG, "MSG_DOWNLOAD_PIC mbi.getData is null");
                        setRun(true);

                        if (uiHandler != null) {
                            uiHandler
                                    .sendEmptyMessage(MSG_DLNA_UI_IS_AFTER_PLAY);
                        }
                        return;
                    }
                    mLog.d(TAG, "Block ==========================>");
                    if (ImagePlayerActivity.isNeedRejectKey) {
                        ImagePlayerActivity
                                .setInputHalfBlock(ImagePlayerActivity.rejectkey);
                    }
                    mLog.d(TAG,
                            "onKeyUp----------->555-->"
                                    + DateUtil.getCurrentTime());
                    // 判断是否相同url
                    if (mCurrentShowUrl != null
                            && mCurrentShowUrl.equalsIgnoreCase(url)) {// 相同

                        mLog.w(TAG,
                                "Ok, that's the same Current Show URL with: "
                                        + mCurrentShowUrl);
                    } else {
                        mCurrentShowUrl = url;

                        mLog.d(TAG, "MSG_DOWNLOAD_PIC --> mCurrentShowUrl="
                                + mCurrentShowUrl);
                    }

                    url = getExistLocalPath(mbi);
                    mbi.setmData(url);

                    // added by c00226539去除更新标签
//                    if (ConstData.DeviceType.isCloudDevice(mbi.getmDeviceType())) {
//                        String tablename = DataBaseUtil.getTableName(mContext,
//                                mbi.getmPhysicId());
//                        mLog.d(TAG, "getTableName = " + tablename);
//                        if (StringUtils.isNotEmpty(tablename)
//                                && StringUtils.isNotEmpty(mbi.getmParentPath())) {
//                            ContentValues cv = new ContentValues();
//                            cv.put("MEDIAISNEW", 0);
//                            int updateResult = getContext()
//                                    .getContentResolver().update(
//                                            Uri.parse(CloudProvider.MEDIAURI
//                                                    + tablename), cv,
//                                            " LARGIMGID = ? ", new String[]
//                                            { mbi.getmParentPath() });
//                            mLog.d(TAG, "updateResult = " + updateResult);
//                        }
//                    }

                    if (uiHandler != null) {
                        uiHandler
                                .sendEmptyMessage(MSG_DLNA_UI_SET_IMAGE_DETAIL);
                        mLog.d(TAG, "MSG_DLNA_UI_SET_IMAGE_DETAIL");
                    }
                    mLog.d(TAG,
                            "onKeyUp----------->6666-->"
                                    + DateUtil.getCurrentTime());
                    mLog.d(TAG, "url--->" + url);
                    mIsLoadingImage = true;

                    mImageView = (ImageView) getNextView();
                    if (uiHandler != null) {
                        // 重置旋转参数
                        uiHandler.sendEmptyMessage(MSG_DLNA_UI_RESET_ROTATE);
                    }
                    mLog.d(TAG, "onKeyUp----------->getBmpDrawable start-->"
                            + DateUtil.getCurrentTime());

                    int fileSize = 0;

                    if (mbi.getmDeviceType() == ConstData.DeviceType.DEVICE_TYPE_CLOUD) {
                        isLoading = true;
                    }

                    // 加载图片进行显示
                    SoftReference<Drawable> drawable = null;
                    if (mGifListener != null && isGifImage(mbi)) {
                        GifOpenHelper gif = getGif(url, mbi.getmDeviceType(),
                                mbi.getmFileName());
                        if (gif != null) {
                            Bitmap bitmap = gif.getImage();
                            if (bitmap != null) {
                                BitmapDrawable bmpDraw = new BitmapDrawable(
                                        bitmap);
                                drawable = new SoftReference<Drawable>(bmpDraw);

                                if (uiHandler != null) {
                                    Message gifMsg = uiHandler.obtainMessage();
                                    gifMsg.what = MSG_DLNA_UI_PLAY_GIF;
                                    gifMsg.obj = gif;

                                    uiHandler.sendMessageDelayed(gifMsg, 500);
                                }
                            }
                        }
                    }

                    if (drawable == null) {
                        // modify by xkf76249 增加一个图片名字参数
                        drawable = getBmpDrawable(url, mbi.getmDeviceType(),
                                fileSize, mbi.getmFileName());
                    }

                    mLog.d(TAG, "onKeyUp----------->getBmpDrawable end-->"
                            + DateUtil.getCurrentTime());
                    mbCycledDownload = false;
                    if (uiHandler != null
                            && mbi.getmDeviceType() != ConstData.DeviceType.DEVICE_TYPE_CLOUD
                            && drawable != null && drawable.get() != null) {
                        uiHandler
                                .sendEmptyMessage(MSG_DLNA_UI_SET_IMAGE_DETAIL);
                        mLog.d(TAG, "MSG_DLNA_UI_SET_IMAGE_DETAIL");
                    }

                    int degree = 0;

                    mIsLoadingImage = false;

                    if (drawable == null) {
                        mLog.d(TAG, "MSG_DOWNLOAD_PIC --> drawable == null");
                    } else {
                        mLog.d(TAG, "MSG_DOWNLOAD_PIC --> drawable != null");
                    }

                    setRun(true);

                    ParseImage parseImage = new ParseImage();
                    parseImage.setSrfBitmap(drawable);
                    parseImage.setUrl(url);
                    parseImage.setDegree(degree);

                    msgDraw = Message.obtain();
                    msgDraw.what = MSG_DLNA_UI_IS_SHOWPIC;
                    msgDraw.obj = parseImage;
                    drawable = null;
                    parseImage = null;

                    if (uiHandler != null) {
                        // kbtest
                        uiHandler.removeMessages(MSG_DLNA_UI_IS_SHOWPIC);
                        uiHandler.sendMessage(msgDraw);
                    }
                    mLog.d(TAG,
                            "onKeyUp----------->99999-->"
                                    + DateUtil.getCurrentTime());
                    if (uiHandler != null) {
                        uiHandler.removeMessages(MSG_DLNA_UI_IS_BEFORE_PLAY);
                        uiHandler.sendEmptyMessage(MSG_DLNA_UI_IS_AFTER_PLAY);
                    }

                    // 下载线程完成后
                    isLoading = false;
                    UriTexture.setMbStop(false);
                    mLog.d(TAG, "unBlock ==========================>");
                    if (ImagePlayerActivity.isNeedRejectKey) {
                        ImagePlayerActivity.setInputUnBlock();
                    }
                    break;
                case MSG_DLNA_INVALIDATECACHE:
                    if (msg.obj != null) {
                        mLog.d(TAG,
                                "onKeyUp----------->MSG_DLNA_INVALIDATECACHE");
                        String tmpurl = (String) msg.obj;
                        long crc64 = ImageUtils.Crc64Long(tmpurl);
                        UriTexture.invalidateCache(crc64,
                                UriTexture.BITMAP_MAX_W);
                    }

                    break;
                case MSG_DLNA_IS_ROTATION:

                    mLog.i(TAG, "MSG_DLNA_IS_ROTATION B");

                    if (wrfbm == null) {
                        mLog.e(TAG, "MSG_DLNA_IS_ROTATION null");
                        return;
                    }

                    BitmapDrawable bmpda = (BitmapDrawable) wrfbm;

                    Bitmap bmp = bmpda.getBitmap();

                    mLog.d(TAG, "MSG_DLNA_IS_ROTATION bmp is " + bmp);

                    if (uiHandler != null) {
                        uiHandler.sendEmptyMessage(MSG_DLNA_UI_IS_BEFORE_PLAY);
                        // FIXME
                        // uiHandler.sendEmptyMessageDelayed(MSG_DLNA_UI_IS_BEFORE_PLAY,
                        // DELAY_DISPLAY_PROGRESS_TIME);
                    }

                    int angle = msg.arg1;
                    Bitmap bmpRet = UriTexture.rotate(bmp, angle);
                    bmp = null;

                    if (bmpRet == null) {
                        mLog.e(TAG,
                                "MSG_DLNA_IS_ROTATION bmpRet is null , and UriTexture.rotate angle: "
                                        + angle);

                        // 避免转圈一直不消失
                        if (uiHandler != null) {
                            mLog.d(TAG, "MSG_DLNA_UI_IS_AFTER_PLAY");
                            uiHandler
                                    .sendEmptyMessage(MSG_DLNA_UI_IS_AFTER_PLAY);
                        }

                        // 显示图片旋转失败
                        if (mListener != null) {
                            mLog.d(TAG, "mListener.rotateIamgeFailed()");
                            mListener.rotateIamgeFailed();
                        }
                        return;
                    }

                    BitmapDrawable dw = new BitmapDrawable(bmpRet);
                    SoftReference<Drawable> srbd = new SoftReference<Drawable>(
                            dw);
                    dw = null;
                    msgDraw = Message.obtain();
                    msgDraw.what = MSG_DLNA_UI_IS_SHOWPIC;
                    msgDraw.obj = srbd;
                    srbd = null;

                    if (uiHandler != null) {
                        uiHandler.removeMessages(MSG_DLNA_UI_IS_SHOWPIC);// kbtest
                        uiHandler.sendMessage(msgDraw);
                    }
                    mLog.i(TAG, "MSG_DLNA_IS_ROTATION E");
                    break;
                case MSG_DOWNLOAD_CANCEL:

                    removeMessages(MSG_DOWNLOAD_PIC);

                    break;

                case MSG_DLNA_IS_PLAY:

                    mLog.d(TAG, "MSG_DLNA_IS_PLAY - 1");

                    if (uiHandler != null) {
                        // uiHandler.sendEmptyMessage(MSG_DLNA_UI_IS_BEFORE_PLAY);
                        // FIXME
                        uiHandler.sendEmptyMessageDelayed(
                                MSG_DLNA_UI_IS_BEFORE_PLAY,
                                DELAY_DISPLAY_PROGRESS_TIME);
                    }

                    Message msge = new Message();
                    msge.what = MSG_DLNA_UI_SHOWEXCEPTION;
                    msge.arg1 = 2;
                    if (uiHandler != null) {
                        uiHandler.sendMessage(msge);
                    }

                    mLog.d(TAG,
                            "removeMessages(MSG_DLNA_IS_PLAY)---------------->9");

                    Message msgdown = Message.obtain();
                    msgdown.copyFrom(msg);
                    msgdown.what = MSG_DOWNLOAD_PIC; 
                    mLog.d(TAG, "MSG_DLNA_IS_PLAY - 2");
                    this.removeMessages(MSG_DOWNLOAD_PIC);// kbtest
                    sendMessage(msgdown);

                    // 恢复curl错误码
                    CurlDownload.mCurlCode = -1;
                    UriTexture.mbDownloadStatu = false;
                    mLog.d(TAG,
                            "onKeyUp----------->555-->"
                                    + DateUtil.getCurrentTime());

                    break;

                case MSG_DLNA_IS_CUR:

                    mLog.d(TAG, "MSG_DLNA_IS_CUR - 1");
                    if (uiHandler != null) {
                        // FIXME
                        uiHandler.sendEmptyMessageDelayed(
                                MSG_DLNA_UI_IS_BEFORE_PLAY,
                                DELAY_DISPLAY_PROGRESS_TIME);
                    }

                    mbi = mPlayStateInfo.getCurrentMediaInfo();

                    if (mbi == null) {

                        if (uiHandler != null) {
                        }
                        mLog.d(TAG,
                                "sendEmptyMessage(MSG_DLNA_IS_STOP_AUTO_PLAY)------->1");
                        sendEmptyMessage(MSG_DLNA_IS_STOP_AUTO_PLAY);

                        if (uiHandler != null) {

                            uiHandler
                                    .sendEmptyMessage(MSG_DLNA_UI_IS_AFTER_PLAY);
                        }

                        setRun(false);

                    } else {
                        mLog.d(TAG, "MSG_DLNA_IS_CUR - 2");
                        mLog.d(TAG, "mbi.getData():" + mbi.getUrl());

                        Message msgpre = Message.obtain();
                        msgpre.obj = mbi;
                        msgpre.what = MSG_DLNA_IS_PLAY;
                        mLog.d(TAG, "MSG_DLNA_IS_CUR - 3");
                        if (mDLNAISHandler != null) {
                            // 移去之前播放的请求
                            mLog.d(TAG,
                                    "removeMessages(MSG_DLNA_IS_PLAY)---------------->10");
                            mDLNAISHandler.removeMessages(MSG_DLNA_IS_PLAY);
                            mLog.d(TAG,
                                    "mDLNAISHandler------->sendMessage------->");
                            mDLNAISHandler.sendMessage(msgpre);
                        }
                        mLog.d(TAG, "MSG_DLNA_IS_CUR - 4");

                        // 设置播放云相册图片标志
                        if (mbi.getmDeviceType() == ConstData.DeviceType.DEVICE_TYPE_CLOUD) {
                            setPlayCloudPic(true);
                        }
                    }

                    break;
                case MSG_DLNA_IS_PRE:

                    mLog.d(TAG, "mPlayStateInfo.getMediaList().size():"
                            + mPlayStateInfo.getMediaList().size());
                    if (isMCSRequest()) {
                        mLog.d(TAG,
                                "mPlayStateInfo.getSenderClientUniq().trim():"
                                        + mPlayStateInfo.getSenderClientUniq()
                                                .trim());

                        // 如果是推送或甩屏过来的，只有一个媒体文件的情况下，不响应上一个和下一个
                        if ((mPlayStateInfo.getMediaList().size() == 1)
                                && (mPlayStateInfo
                                        .getSenderClientUniq()
                                        .trim()
                                        .equals(ConstData.ClientTypeUniq.PUSH_UNIQ
                                                .trim()) || mPlayStateInfo
                                        .getSenderClientUniq()
                                        .trim()
                                        .equals(ConstData.ClientTypeUniq.SYN_UINQ
                                                .trim()))) {
                            mLog.d(TAG,
                                    "Push only one image or Syn only one image to STB");
                            break;
                        }
                    }

                    mLog.d(TAG, "MSG_DLNA_IS_PRE - 1");
                    mbi = mPlayStateInfo.getPreMediaInfo();

                    if (mbi == null) {
                        mLog.d(TAG, "MSG_DLNA_IS_PRE - 2");
                        if (uiHandler != null) {
                            uiHandler
                                    .sendEmptyMessage(MSG_DLNA_UI_IS_CANNT_PRE);
                        }
                        mLog.d(TAG,
                                "sendEmptyMessage(MSG_DLNA_IS_STOP_AUTO_PLAY)------->2");
                        if (!isMCSRequest()) {
                            // 如果是推送或甩屏过来的，只有一个媒体文件的情况下，不响应上一个和下一个
                            sendEmptyMessage(MSG_DLNA_IS_STOP_AUTO_PLAY);
                        }
                        //

                        if (uiHandler != null) {
                            uiHandler
                                    .sendEmptyMessage(MSG_DLNA_UI_IS_AFTER_PLAY);
                        }

                        setRun(false);
                    } else {
                        mLog.d(TAG, "MSG_DLNA_IS_PRE - 3");
                        Message msgpre = Message.obtain();
                        msgpre.obj = mbi;
                        msgpre.what = MSG_DLNA_IS_PLAY;
                        // kbtest
                        this.removeMessages(MSG_DLNA_IS_PLAY);
                        sendMessage(msgpre);
                    }
                    mLog.d(TAG, "MSG_DLNA_IS_PRE - 4");

                    break;
                case MSG_DLNA_IS_NEXT:
                    // zkf61715
                    if (mPlayStateInfo.getMediaList() == null) {
                        mLog.e(TAG, "mPlayStateInfo.getMediaList() is null !");
                        break;
                    }
                    mLog.d(TAG, "mPlayStateInfo.getMediaList().size():"
                            + mPlayStateInfo.getMediaList().size());
                    mLog.d(TAG,
                            "onKeyUp----------->333-->"
                                    + DateUtil.getCurrentTime());
                    if (isMCSRequest()) {
                        // 如果是推送或甩屏过来的，只有一个媒体文件的情况下，不响应上一个和下一个
                        if ((mPlayStateInfo.getMediaList().size() == 1)
                                && (mPlayStateInfo
                                        .getSenderClientUniq()
                                        .trim()
                                        .equals(ConstData.ClientTypeUniq.PUSH_UNIQ
                                                .trim()) || mPlayStateInfo
                                        .getSenderClientUniq()
                                        .trim()
                                        .equals(ConstData.ClientTypeUniq.SYN_UINQ
                                                .trim()))) {
                            mLog.d(TAG,
                                    "Push only one image or Syn only one image to STB");
                            break;
                        }
                    }

                    removeMessages(MSG_DLNA_IS_AUTO_PLAY);
                    // kbtest removeMessages(MSG_DLNA_IS_NEXT);

                    mLog.d(TAG, "MSG_DLNA_IS_NEXT - 1");
                    mbi = mPlayStateInfo.getNextMediaInfo();

                    if (mbi == null) {
                        mLog.e(TAG, "MSG_DLNA_IS_NEXT - 2");
                        if (uiHandler != null) {
                            uiHandler
                                    .sendEmptyMessage(MSG_DLNA_UI_IS_CANNT_NEXT);
                        }

                        removeMessages(MSG_DLNA_IS_AUTO_PLAY);
                        mLog.d(TAG,
                                "sendEmptyMessage(MSG_DLNA_IS_STOP_AUTO_PLAY)------->3");
                        sendEmptyMessage(MSG_DLNA_IS_STOP_AUTO_PLAY);

                        if (uiHandler != null) {
                            uiHandler
                                    .sendEmptyMessage(MSG_DLNA_UI_IS_AFTER_PLAY);
                        }
                        setRun(false);
                    } else {
                        if (mPlayStateInfo.isMbiInDeletingDevices(mbi)) {
                            mLog.d(TAG,
                                    "The next meida url is in deleting list");

                            break;
                        } else {
                            mLog.d(TAG,
                                    "removeMessages(MSG_DLNA_IS_PLAY)---------------->1");
                            removeMessages(MSG_DLNA_IS_PLAY);
                            Message msgnext = Message.obtain();
                            msgnext.obj = mbi;
                            msgnext.what = MSG_DLNA_IS_PLAY;

                            sendMessage(msgnext);
                            mLog.d(TAG,
                                    "onKeyUp----------->444-->"
                                            + DateUtil.getCurrentTime());
                        }
                    }

                    break;
                case MSG_DLNA_IS_AUTO_PLAY:

                    mLog.d(TAG, "MSG_DLNA_IS_AUTO_PLAY - 1 interval: "
                            + getInterval());
                    /*
                     * BEGIN: Modifed by c00224451 for DTS2014021408267
                     * 2014/2/18
                     */
                    setAnimationParams();
                    /* END: Modifed by c00224451 for DTS2014021408267 2014/2/18 */
                    Message msgclone = Message.obtain();
                    msgclone.copyFrom(msg);

                    if (showDefaultPic) {
                        if (defaultAudioBgPic.length <= 1)
                            break;
                        uiHandler.removeMessages(MSG_DLNA_UI_IS_SHOWPIC);
                        uiHandler.sendEmptyMessageDelayed(
                                MSG_DLNA_UI_IS_SHOWPIC, msg.arg1);
                    } else {
                        sendEmptyMessageDelayed(MSG_DLNA_IS_NEXT, msg.arg1);
                    }

                    setAutoPlayMode(true);

                    setInterval(msg.arg1);

                    break;
                case MSG_DLNA_IS_STOP_AUTO_PLAY:

                    mLog.d(TAG, "MSG_DLNA_IS_AUTO_PLAY - 1 interval: "
                            + getInterval());

                    setAutoPlayMode(false);

                    if (uiHandler != null && !isLoading) {
                        uiHandler.sendEmptyMessage(MSG_DLNA_UI_IS_AFTER_PLAY);
                    }
                    mLog.d(TAG,
                            "removeMessages(MSG_DLNA_IS_PLAY)---------------->2");
                    if (!isLoading) {
                        removeMessages(MSG_DLNA_IS_PLAY);
                        removeMessages(MSG_DLNA_IS_AUTO_PLAY);
                    }

                    break;
                default:
                    break;
            }

            super.handleMessage(msg);
        }

        private boolean isMCSRequest() {
            return mPlayStateInfo.getSenderClientUniq() != null;
        }

        private void delayD(int iDelay) {
            try {
                Thread.sleep(iDelay);
            } catch (InterruptedException e) {
            }
        }

        private void downloadImageToCache() {
            final List<LocalMediaInfo> medias = mPlayStateInfo.getMediaList();
            final int index = mPlayStateInfo.getCurrentIndex();
            new Thread(new Runnable() {
                public void run() {
                    for (int i = 0; i < 1; i++) {
                        // 下载后面图片
                        mLog.d(TAG, "downloadImageToCache ----------->2");
                        mLog.d(TAG, "downloadImageToCache ----------->3");
                        int ind = index + 1 + i;
                        int size = medias.size();
                        if (mPlayStateInfo != null
                                && mPlayStateInfo.getMediaList() != null
                                && mPlayStateInfo.getMediaList().size() > 0
                                && ind < mPlayStateInfo.getMediaList().size()
                                && ind >= 0) {
                            mLog.d(TAG, "downloadImageToCache ----------->4");
                            LocalMediaInfo mbi = mPlayStateInfo.getMediaList()
                                    .get(ind);

                            // modify by XKF76249
//                            if (ConstData.DeviceType.isCloudDevice(mbi
//                                    .getmDeviceType())) {
//                                mLog.d(TAG,
//                                        "downloadImageToCache ----------->5");
//                                if (StringUtils
//                                        .isNetworkURI(getExistLocalPath(mbi))) {
//                                    mLog.d(TAG,
//                                            "downloadImageToCache ----------->6");
//                                    loadImageToCache(mbi);
//                                }
//                            } else 
                            {
                                loadImageToCache(mbi);
                            }
                        }

                        ind = index - 1 - i;
                        if (mPlayStateInfo != null
                                && mPlayStateInfo.getMediaList() != null
                                && ind < mPlayStateInfo.getMediaList().size()
                                && mPlayStateInfo.getMediaList().size() > 0
                                && ind >= 0) {
                            LocalMediaInfo mbi = mPlayStateInfo.getMediaList()
                                    .get(ind);

                            // modify by XKF76249
//                            if (ConstData.DeviceType.isCloudDevice(mbi
//                                    .getmDeviceType())) {
//                                if (StringUtils
//                                        .isNetworkURI(getExistLocalPath(mbi))) {
//                                    loadImageToCache(mbi);
//                                }
//                            } else 
                            {
                                loadImageToCache(mbi);
                            }
                        }

                    }
                }

                private void loadImageToCache(LocalMediaInfo mbi) {
                    String url = mbi.getUrl();
                    if (url != null) {

                        mIsDownLoadBigDrawble = true;

                        UriTexture.DownloadCloudImageToCache(url,
                                mbi.getmDeviceType(), BITMAP_MAX_W,
                                BITMAP_MAX_H, mContext, mbi.getmFileName());
                        // }
                        mIsDownLoadBigDrawble = false;

                        // 该时间必须比 等待当前图片下载完毕的循环时间30ms长
                        delayD(50);
                    }
                }
            }).start();
        }

        private boolean isGifImage(LocalMediaInfo mbi) {
            if (mbi == null) {
                return false;
            }
            mLog.d(TAG, "isGifImage()--->mbi.getData():" + mbi.getUrl()
                    + " ,mbi.getMimeType():" + mbi.getmMimeType());

            String url = mbi.getUrl();
            String mimeType = mbi.getmMimeType();
            if (url != null) {
                if (mimeType != null
                        && mimeType.startsWith("http-get:*:image/gif")) {
                    mLog.d(TAG, "isGifImage()--->mimeTyep:" + mimeType);
                    return true;
                } else if (url.endsWith(".gif") || url.endsWith(".GIF")
                        || url.contains(".gif?") || url.contains(".GIF?")) {
                    mLog.d(TAG, "isGifImage()--->url:" + url);
                    return true;
                }
            }
            return false;
        }

        /**
         * @param mbAutoPlay the mbAutoPlay to set
         */
        public void setAutoPlayMode(boolean mbAutoPlay) {
            // mDLNAISHandler.setRun(mbAutoPlay);
            this.mbAutoPlay = mbAutoPlay;
        }

        /**
         * mbAutoPlay
         * 
         * @return the mbAutoPlay
         * @since 1.0.0
         */

        public boolean isAutoPlay() {
            return mbAutoPlay;
        }

        /**
         * @param interval the interval to set
         */
        public void setInterval(int interval) {
            this.interval = interval;
        }

        /**
         * interval
         * 
         * @return the interval
         * @since 1.0.0
         */

        public int getInterval() {
            if (mImagePlaySetHelper != null) {
                return mImagePlaySetHelper.getPlayInterval();
            }
            return interval;

        }

    }

    public Drawable CreateBitMap(String url, int devType)
    {
        URL myFileUrl = null;
        Bitmap bitmap = null;
        InputStream is = null;

        try
        {

            if (devType == ConstData.DeviceType.DEVICE_TYPE_DMS)
            {
                try
                {
                    myFileUrl = new URL(url);

                }
                catch (MalformedURLException e)
                {
                }

                if (myFileUrl == null)
                {
                    mLog.e(TAG, "CreateBitMap - HttpURLConnection (myFileUrl == null)");
                    return null;
                }

                HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();

                if (conn == null)
                {
                    mLog.e(TAG, "CreateBitMap - HttpURLConnection conn == null");
                    return null;
                }

                conn.connect();
                is = conn.getInputStream();

            }
            else if (devType == ConstData.DeviceType.DEVICE_TYPE_U)
            {// 此处给的格式可能是URI的格式

                FileInputStream fis = new FileInputStream(url);

                is = fis;

            }
            else
            {
                mLog.e(TAG, "CreateBitMap - error deviceType:" + devType);
                return null;
            }

            if (is == null)
            {
                mLog.d(TAG, "CreateBitMap - is == null");
                return null;
            }
            byte[] bt = getBytes(is);
            if (bt == null)
            {
                mLog.d(TAG, "CreateBitMap - bt == null");
                is.close();
                return null;
            }

            int len = bt.length;

            BitmapFactory.Options options = getOptions(bt, len, 1080, 1000);

            bitmap = BitmapFactory.decodeByteArray(bt, 0, len, options);
            if (bitmap == null)
            {
                mLog.d(TAG, "CreateBitMap - bitmap == null");
                is.close();
                return null;
            }

            mLog.d(TAG, "W:" + options.outWidth + " H:" + options.outHeight + " outMimeType:" + options.outMimeType);

            bt = null;

        }
        catch (IOException e)
        {
            if (bitmap != null && !bitmap.isRecycled())
            {
                bitmap.recycle();
                System.gc();
            }

            if (is != null)
            {
                try
                {
                    is.close();
                }
                catch (IOException e1)
                {
                }
            }
            return null;
        }
        finally
        {
            if (is != null)
            {
                try
                {
                    is.close();
                }
                catch (IOException e)
                {
                }
            }
        }
        BitmapDrawable retDrawable = new BitmapDrawable(bitmap);

        bitmap = null;
        System.gc();
        return retDrawable;

    }

    private byte[] getBytes(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] b = new byte[1024];
        int len = 0;
        while ((len = is.read(b, 0, 1024)) != -1) {
            baos.write(b, 0, len);
            baos.flush();
        }
        byte[] bytes = baos.toByteArray();
        return bytes;
    }

    /**
     * 根据大小获得Options实例 <功能详细描述>
     * 
     * @param size 图片大小;
     * @return 用于创建改大小图片的Options对象
     * @see [类、类#方法、类#成员]
     */
    public Options getOptions(byte[] bt, long size, int maxW, int maxH) {
        if (bt == null) {
            return null;
        }

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        opts.outWidth = maxW;
        opts.outHeight = maxH;

        Performance performance = new Performance();
        performance.start();
        BitmapFactory.decodeByteArray(bt, 0, (int) size, opts);
        performance.end();

        int outWidth = opts.outWidth;
        int outHeight = opts.outHeight;

        mLog.d(TAG, "outWidth = " + outWidth + " outHeight:" + outHeight);

        opts.inDither = false;
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
        opts.inSampleSize = 1;

        if (outWidth != 0 && outHeight != 0 && maxW != 0 && maxH != 0) {
            int sampleSize = (outWidth / maxW + outHeight / maxW);
            mLog.d(TAG, "sampleSize = " + sampleSize);
            sampleSize = sampleSize < 1 ? 1 : sampleSize;

            opts.inSampleSize = sampleSize;

            mLog.d(TAG, "sampleSize = " + opts.inSampleSize);
        }

        int maxNumOfPixels = maxW * maxH;
        int minSideLength = Math.min(maxW, maxH) / 2;
        opts.inSampleSize = ImageUtils.computeSampleSize(opts, minSideLength,
                maxNumOfPixels);
        mLog.d(TAG, "sampleSize 2= " + opts.inSampleSize);

        opts.inJustDecodeBounds = false;

        return opts;
    }

    /****************************************************
     * 播放处理handler 结束
     ****************************************************/

    /****************************************************
     * 设置动画 开始
     ****************************************************/

    /**
     * 设置进入动画
     */
    /* BEGIN: Modified by c00224451 for DTS2014021408267 2014/2/19 */
    public void setAnimation(int animationType) {
        Animation inAni = null;
        Animation outAni = null;

        if (EnumAnimationEffect.E_ANIMATION_EFFECT_ZOOM_OUT.ordinal() == animationType) {
            inAni = AnimationUtils.loadAnimation(mContext,
                    R.anim.anim_effect_zoom_in);
            outAni = AnimationUtils.loadAnimation(mContext,
                    R.anim.anim_effect_zoom_out);
        } else if (EnumAnimationEffect.E_ANIMATION_EFFECT_FLOAT_UP.ordinal() == animationType) {
            inAni = AnimationUtils.loadAnimation(mContext,
                    R.anim.anim_effect_float_up_in);
            outAni = AnimationUtils.loadAnimation(mContext,
                    R.anim.anim_effect_float_up_out);
        } else if (EnumAnimationEffect.E_ANIMATION_EFFECT_FADE_OUT.ordinal() == animationType) {
            int defaultDuration = getResources().getInteger(
                    android.R.integer.config_longAnimTime);
            inAni = AnimationUtils.loadAnimation(mContext,
                    android.R.anim.fade_in);
            inAni.setStartOffset(defaultDuration);
            inAni.setDuration(defaultDuration);
            outAni = AnimationUtils.loadAnimation(mContext,
                    android.R.anim.fade_out);
            outAni.setDuration(defaultDuration);
        } else if (EnumAnimationEffect.E_ANIMATION_EFFECT_WIPE_OUT.ordinal() == animationType) {
            inAni = AnimationUtils.loadAnimation(mContext,
                    R.anim.ip_slide_left_in);
            outAni = AnimationUtils.loadAnimation(mContext,
                    R.anim.ip_slide_right_out);
        } else if (EnumAnimationEffect.E_ANIMATION_EFFECT_FLOAT_DOWN.ordinal() == animationType) {
            inAni = AnimationUtils.loadAnimation(mContext,
                    R.anim.anim_effect_float_down_in);
            outAni = AnimationUtils.loadAnimation(mContext,
                    R.anim.anim_effect_float_down_out);
        } else if (EnumAnimationEffect.E_ANIMATION_EFFECT_SPREAD_OUT.ordinal() == animationType) {
            inAni = AnimationUtils.loadAnimation(mContext,
                    R.anim.anim_effect_spread_in);
        } else if (EnumAnimationEffect.E_ANIMATION_EFFECT_LEFT_IN.ordinal() == animationType) {
            inAni = AnimationUtils.loadAnimation(mContext,
                    R.anim.ip_slide_left_in);
            outAni = AnimationUtils.loadAnimation(mContext,
                    R.anim.ip_slide_right_out);
        } else if (EnumAnimationEffect.E_ANIMATION_EFFECT_RIGHT_IN.ordinal() == animationType) {
            inAni = AnimationUtils.loadAnimation(mContext,
                    R.anim.ip_slide2_right_in);
            outAni = AnimationUtils.loadAnimation(mContext,
                    R.anim.ip_slide2_left_out);
        }

        super.setInAnimation(inAni);
        // comment it for test
        super.setOutAnimation(outAni);
    }

    /* END: Modified by c00224451 for DTS2014021408267 2014/2/19 */

    /****************************************************
     * 设置动画 结束
     ****************************************************/

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.ViewSwitcher.ViewFactory#makeView()
     */
    public View makeView() {
        // 先remove所有的views 再加image
        mLog.d(TAG, "------makeView()---->");
        mIv = null;

        mIv = new ImageView(mContext);

        mIv.setDrawingCacheEnabled(true);

        mIv.setBackgroundColor(Color.TRANSPARENT);

        mIv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        mIv.setLayoutParams(new ImageSwitcher.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        return mIv;
    }

    /* BEGIN: Modified by c00224451 for DTS2014022708542 2014/3/4 */
    public void setAudioPlayMode(boolean auto, int interval) {
        mLog.i(TAG, "setAudioPlayMode B");
        if (mDLNAISHandler == null) {
            return;
        }
        mLog.i(TAG, "setAudioPlayMode E");

        setRun(auto);
        mDLNAISHandler.setAutoPlayMode(auto);
        mDLNAISHandler.setInterval(interval);

        if (auto) {
            /* BEGIN: Modified by c00224451 for DTS2014021408267 2014/2/19 */
            setAnimationParams();
            /* END: Modified by c00224451 for DTS2014021408267 2014/2/19 */
        }
    }

    /* END: Modified by c00224451 for DTS2014022708542 2014/3/4 */

    /**
     * 设置自动播放接口
     */
    public void setAutoModeToo(boolean auto, int interval) {
        mLog.i(TAG, "setAutoMode B");
        if (mDLNAISHandler == null) {
            return;
        }
        mLog.i(TAG, "setAutoMode E");
        setRun(true);

        Message msg = Message.obtain();
        msg.arg1 = interval;
        msg.what = MSG_DLNA_IS_AUTO_PLAY;

        if (auto) {

            mDLNAISHandler.removeMessages(MSG_DLNA_IS_AUTO_PLAY);

            // mDLNAISHandler.sendMessageDelayed(msg, 500);
            mDLNAISHandler.setAutoPlayMode(true);
            /* BEGIN: Modified by c00224451 for DTS2014021408267 2014/2/19 */
            setAnimationParams();
            /* END: Modified by c00224451 for DTS2014021408267 2014/2/19 */
        } else {
            setRun(false);

            // --- Added by zhaomingyang 00184367 2012年1月11日 --- Begin
            // 快速关闭自动播放，解决问题：点击播放，停1S，切换到上一张，播放器停一下又切换回进入时的图片
            mLog.d(TAG, "removeMessages(MSG_DLNA_IS_PLAY)---------------->3");
            mDLNAISHandler.removeMessages(MSG_DLNA_IS_PLAY);
            mDLNAISHandler.removeMessages(MSG_DLNA_IS_NEXT);
            // --- Added by zhaomingyang 00184367 2012年1月11日 --- End

            mDLNAISHandler.removeMessages(MSG_DLNA_IS_AUTO_PLAY);
            mLog.d(TAG, "sendEmptyMessage(MSG_DLNA_IS_STOP_AUTO_PLAY)------->4");
            mDLNAISHandler.sendEmptyMessage(MSG_DLNA_IS_STOP_AUTO_PLAY);
        }

    }

    /**
     * 设置自动播放接口
     */
    public void setAutoMode(boolean auto, int interval) {
        mLog.d(TAG, "setAutoMode B");
        if (mDLNAISHandler == null) {
            return;
        }
        mLog.d(TAG, "setAutoMode E");
        setRun(true);

        Message msg = Message.obtain();
        msg.arg1 = interval;
        msg.what = MSG_DLNA_IS_AUTO_PLAY;
        mLog.d(TAG, "setAutoMode B-->interval-->" + interval);
        if (auto) {
            mLog.d(TAG, "start_auto_play---------------->" + auto);
            mDLNAISHandler.removeMessages(MSG_DLNA_IS_AUTO_PLAY);

            // 加载图片完毕再去延时播放
            if (!isLoading) {
                mLog.d(TAG, "setAutoMode isLoading-->" + isLoading);
                mDLNAISHandler.sendMessageDelayed(msg, 500);
            }
            mDLNAISHandler.removeMessages(MSG_DLNA_IS_STOP_AUTO_PLAY);
            mDLNAISHandler.setAutoPlayMode(true);

            // 外置播放器图片切换默认为淡入淡出
            /* BEGIN: Modified by c00224451 for DTS2014021408267 2014/2/19 */
            setAnimationParams();
            /* END: Modified by c00224451 for DTS2014021408267 2014/2/19 */

        } else {
            if (!isLoading) {
                setRun(false);
            }
            // --- Added by zhaomingyang 00184367 2012年1月11日 --- Begin
            // 快速关闭自动播放，解决问题：点击播放，停1S，切换到上一张，播放器停一下又切换回进入时的图片
            mLog.d(TAG, "removeMessages(MSG_DLNA_IS_PLAY)---------------->3");
            // if(!isLoading){
            mDLNAISHandler.removeMessages(MSG_DLNA_IS_PLAY);
            mDLNAISHandler.removeMessages(MSG_DLNA_IS_NEXT);
            // --- Added by zhaomingyang 00184367 2012年1月11日 --- End

            mDLNAISHandler.removeMessages(MSG_DLNA_IS_AUTO_PLAY);
            mLog.d(TAG, "sendEmptyMessage(MSG_DLNA_IS_STOP_AUTO_PLAY)------->4");
            // }
            mLog.d(TAG, "kbtest setAutoMode stop_auto_play---------------->");
            mDLNAISHandler.removeMessages(MSG_DLNA_IS_STOP_AUTO_PLAY);
            mDLNAISHandler.sendEmptyMessage(MSG_DLNA_IS_STOP_AUTO_PLAY);

        }

    }

    public void setPlayInfo(PlayStateInfo playStateInfo) {
        mPlayStateInfo = playStateInfo;
    }

    /**
     * 
     * rotateImage 旋转
     * 
     * @param angle void
     * @exception
     */
    public void rotateImage(int angle) {
        mLog.i(TAG, "DLNAImageSwither B angle:" + angle);

        if (mDLNAISHandler == null) {
            return;
        }

        mDLNAISHandler.setAutoPlayMode(false);

        Message msg = Message.obtain();
        msg.what = MSG_DLNA_IS_ROTATION;
        msg.arg1 = angle;

        setRun(false);

        // 移去之前播放的请求
        mLog.d(TAG, "removeMessages(MSG_DLNA_IS_PLAY)---------------->4");
        mDLNAISHandler.removeMessages(MSG_DLNA_IS_PLAY);
        mDLNAISHandler.removeMessages(MSG_DLNA_IS_CUR);
        mDLNAISHandler.removeMessages(MSG_DLNA_IS_ROTATION);
        mDLNAISHandler.removeMessages(MSG_DLNA_IS_AUTO_PLAY);
        mDLNAISHandler.removeMessages(MSG_DLNA_IS_PRE);
        mDLNAISHandler.removeMessages(MSG_DLNA_IS_NEXT);

        // 移去之前播放的请求
        setRun(true);
        mDLNAISHandler.sendMessage(msg);
        /* BEGIN: Modified by c00224451 for DTS2014021408267 2014/2/19 */
        setAnimation(EnumAnimationEffect.E_ANIMATION_EFFECT_FADE_OUT.ordinal());
        /* END: Modified by c00224451 for DTS2014021408267 2014/2/19 */
        mLog.i(TAG, "DLNAImageSwither E");

    }

    // zkf61715
    private boolean showDefaultPic = false;

    public void setShowDefaultPic(boolean showDefaultPic) {
        this.showDefaultPic = showDefaultPic;
    }

    /**
     * 播放当前图片
     */
    public void currImage() {
        mLog.i(TAG, "currImage B");
        if (mDLNAISHandler == null) {
            return;
        }
        setRun(true);

        UriTexture.setMbStop(false);
        // 移去之前播放的请求
        mLog.d(TAG, "removeMessages(MSG_DLNA_IS_PLAY)---------------->5");
        mDLNAISHandler.removeMessages(MSG_DLNA_IS_PLAY);

        if (showDefaultPic) {
            // kbtest
            uiHandler.removeMessages(MSG_DLNA_UI_IS_SHOWPIC);
            uiHandler.sendEmptyMessage(MSG_DLNA_UI_IS_SHOWPIC);
            return;
        }
        // kbtest
        mDLNAISHandler.removeMessages(MSG_DLNA_IS_CUR);
        mDLNAISHandler.sendEmptyMessage(MSG_DLNA_IS_CUR);

        setAnimation(null);

        mLog.i(TAG, "currImage E");
    }

    public void preImage() {
        if (mDLNAISHandler == null) {
            return;
        }

        mDLNAISHandler.setAutoPlayMode(false);

        if (mbCycledDownload) {
            setPressLeftOrRight(true);
        } else {
            setPressLeftOrRight(false);
        }

        // 取消下载云相册图片
        UriTexture.cancelDownload();

        setRun(true);

        // 移去之前播放的请求
        mLog.d(TAG, "removeMessages(MSG_DLNA_IS_PLAY)---------------->6");
        mDLNAISHandler.removeMessages(MSG_DLNA_IS_PLAY);

        /* BEGIN: Modified by c00224451 for DTS2014021408267 2014/2/19 */
        setAnimationParams();
        /* END: Modified by c00224451 for DTS2014021408267 2014/2/19 */

        mDLNAISHandler.removeMessages(MSG_DLNA_IS_PRE);

        mDLNAISHandler.sendEmptyMessageDelayed(MSG_DLNA_IS_PRE, 40);
    }

    public void nextImage() {
        mLog.d(TAG, "nextImage()----------->");
        if (mDLNAISHandler == null) {
            return;
        }

        mDLNAISHandler.setAutoPlayMode(false);

        if (mbCycledDownload) {
            setPressLeftOrRight(true);
        } else {
            setPressLeftOrRight(false);
        }

        // 取消下载云相册图片
        UriTexture.cancelDownload();

        setRun(true);
        // 移去之前播放的请求
        mLog.d(TAG, "removeMessages(MSG_DLNA_IS_PLAY)---------------->7");
        mDLNAISHandler.removeMessages(MSG_DLNA_IS_PLAY);

        // 设置动画
        /* BEGIN: Modified by c00224451 for DTS2014021408267 2014/2/19 */
        setAnimationParams();
        /* END: Modified by c00224451 for DTS2014021408267 2014/2/19 */

        mDLNAISHandler.removeMessages(MSG_DLNA_IS_NEXT);
        // FIXME modify by zouzhiyi 不需要延时发送
        // mDLNAISHandler.sendEmptyMessageDelayed(MSG_DLNA_IS_NEXT, 40);
        mLog.d(TAG, "mDLNAISHandler.sendEmptyMessage(MSG_DLNA_IS_NEXT)--->1");
        mDLNAISHandler.sendEmptyMessage(MSG_DLNA_IS_NEXT);
        mLog.d(TAG, "onKeyUp----------->22-->" + DateUtil.getCurrentTime());

    }

    private void LoadBmpDrawable(String url, int deviceType) {
        mLog.d(TAG, "LoadBmpDrawable-->url---->" + url);
        ImageView image = (ImageView) getNextView();
        mLog.d(TAG, "LoadBmpDrawable-->getNextView()---->" + image);
        imageLoader.displayImage(url, image, options,
                new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        isLoading = true;
                        if (uiHandler != null) {
                            uiHandler.removeMessages(MSG_DLNA_UI_IS_AFTER_PLAY);
                            uiHandler
                                    .sendEmptyMessage(MSG_DLNA_UI_IS_BEFORE_PLAY);
                        }
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view,
                            FailReason failReason) {
                        String message = null;
                        switch (failReason.getType())
                        {
                            case IO_ERROR:
                                message = "Input/Output error";
                                CurlDownload.mCurlCode = 28;
                                break;
                            case DECODING_ERROR:
                                message = "Image can't be decoded";
                                CurlDownload.mCurlCode = -1;
                                break;
                            case NETWORK_DENIED:
                                message = "Downloads are denied";
                                break;
                            case OUT_OF_MEMORY:
                                message = "Out Of Memory error";
                                CurlDownload.mCurlCode = 2;
                                break;
                            case UNKNOWN:
                                message = "Unknown error";
                                CurlDownload.mCurlCode = 2;
                                break;
                        }
                        mLog.d(TAG, "onLoadingFailed-->message---->" + message);
                        isLoading = false;
                        sendDisplayMsg(null, imageUri);
                        if (uiHandler != null) {
                            uiHandler
                                    .removeMessages(MSG_DLNA_UI_IS_BEFORE_PLAY);
                            uiHandler
                                    .sendEmptyMessage(MSG_DLNA_UI_IS_AFTER_PLAY);
                        }
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view,
                            Bitmap loadedImage) {
                        mLog.d(TAG, "onLoadingComplete-->Bitmap---->"
                                + loadedImage);
                        isLoading = false;
                        if (imageUri != null && imageUri.equals(getImageUrl())) {
                            // if (uiHandler != null) {
                            // uiHandler.removeMessages(MSG_DLNA_UI_IS_BEFORE_PLAY);
                            // uiHandler.sendEmptyMessage(MSG_DLNA_UI_IS_AFTER_PLAY);
                            // }
                            sendDisplayMsg(loadedImage, imageUri);
                        }
                    }

                });
    }

    /**
     * 发送显示图片msg
     */
    private void sendDisplayMsg(Bitmap loadedImage, String imageUri) {
        mLog.d(TAG, "sendDisplayMsg--------->start");
        BitmapDrawable bmpDraw = null;
        SoftReference<Drawable> wrf = null;
        if (loadedImage == null) {
            wrf = null;
        } else {
            bmpDraw = new BitmapDrawable(loadedImage);
            wrf = new SoftReference<Drawable>(bmpDraw);
        }
        ParseImage parseImage = new ParseImage();
        parseImage.setSrfBitmap(wrf);
        parseImage.setUrl(imageUri);
        parseImage.setDegree(0);

        Message msgDraw = new Message();
        msgDraw.what = MSG_DLNA_UI_IS_SHOWPIC;
        msgDraw.obj = parseImage;
        bmpDraw = null;
        parseImage = null;
        mLog.d(TAG, "sendDisplayMsg----------->END");
        if (uiHandler != null) {
            uiHandler.sendMessage(msgDraw);
        }
    }

    /**
     * @author XKF76249 获取云相册的本地图片路径
     */
    public synchronized String getExistLocalPath(LocalMediaInfo mbi) {
        if (mbi == null) {
            return null;
        }
        String url = mbi.getUrl();
        if (mContext == null) {
            return url;
        }

//        if (ConstData.DeviceType.isCloudDevice(mbi.getmDeviceType())) {// 云设备，可以检测下是否有本地文件
//            if (((ImagePlayerActivity) mContext).getCloudPreCache() == null) {
//
//                return url;
//            }
//            String urlLocal = ((ImagePlayerActivity) mContext)
//                    .getCloudPreCache().getCachePath(mbi.compress());
//            mLog.d(TAG, "urlLocal---" + urlLocal);
//            if (!StringUtils.isEmpty(urlLocal)) {
//                File file = new File(urlLocal);
//                mLog.d(TAG, "file---");
//                if (file.exists()) {
//                    mLog.e(TAG, "change url into urllocal:" + urlLocal);
//                    url = urlLocal;
//                }
//                // Fix DTS2013090401758
//                else if (urlLocal.startsWith("http://dl.dropboxusercontent")) {
//                    mLog.d(TAG, "Dropbox url:" + urlLocal);
//                    url = urlLocal;
//                } else {
//                    mLog.d(TAG, "getExistLocalPath doesn't effect !");
//                }
//            }
//        }
        return url;
    }

    public void nextImageD() {
        if (mDLNAISHandler == null) {
            return;
        }

        mDLNAISHandler.setAutoPlayMode(true);

        setRun(true);
        // 移去之前播放的请求
        mLog.d(TAG, "removeMessages(MSG_DLNA_IS_PLAY)---------------->8");
        mDLNAISHandler.removeMessages(MSG_DLNA_IS_PLAY);

        /* BEGIN: Modified by c00224451 for DTS2014021408267 2014/2/19 */
        setAnimationParams();
        /* END: Modified by c00224451 for DTS2014021408267 2014/2/19 */

        mDLNAISHandler.removeMessages(MSG_DLNA_IS_NEXT);
        mDLNAISHandler.sendEmptyMessageDelayed(MSG_DLNA_IS_NEXT, 40);

    }

    public void setProgress(ProgressBar progressBar) {
        mProgressBar = progressBar;
    }

    public void setOperatingHint(LinearLayout operatingHint) {
        mOperatingHint = operatingHint;
        handler.sendEmptyMessageDelayed(HIDDEN_HINT_LAYOUT, 5000);
    }

    /**
     * 
     * 
     * DLNAImageSwitcherListenerInterface 监听回调接口 2011-11-2 上午09:35:33
     * 
     * @version 1.0.0
     * 
     */
    public interface DLNAImageSwitcherListener {
        /**
         * 
         * showDetail 展示详情
         * 
         * @param mbi void
         * @exception
         */
        public void showDetail(LocalMediaInfo mbi, int curIndex, int total);

        /**
         * 
         * arriveFisrtElement 到头
         * 
         * @return void
         * @exception
         */
        public void arriveFirstElement(boolean b);

        /**
         * 
         * arriveLastElement 到尾了
         * 
         * @return void
         * @exception
         */
        public void arriveLastElement(boolean b);

        /**
         * 
         * cantMovePrevious 不能向前移动了 void
         * 
         * @exception
         */
        public void cantMovePrevious();

        /**
         * 
         * cantMoveNext 不能向后移动了 void
         * 
         * @exception
         */
        public void cantMoveNext();

        /**
         * 
         * showImageEnd 播放一个图片结束后 void
         * 
         * @exception
         */
        public void showImageEnd();

        /**
         * 
         * canntshowImage 无法播放 void
         * 
         * @exception
         */
        public void canntshowImage();

        /**
         * 
         * rotateIamgeFailed 图片旋转失败 void
         * 
         * @exception
         */
        public void rotateIamgeFailed();

        /**
         * 控制无法显示图片界面
         */
        public void setCantDisplay(boolean b);

        /**
         * 设置图片自动播放
         */
        public void setAutoPlay();

    }

    public int getAnimationWith() {
        return mAnimationWith;
    }

    public void setAnimationWith(int mAnimationWith) {
        this.mAnimationWith = mAnimationWith;
    }

    @Override
    public void setScaleTypeCenterInSide() {
        if (uiHandler != null) {
            uiHandler.sendEmptyMessage(MSG_DLNA_UI_SET_CENTER_INSIDE);
            mLog.d(TAG, "setScaleTypeCenterInSide");
        }
    }

    @Override
    public void setScaleTypeFixCenter() {
        if (uiHandler != null) {
            uiHandler.sendEmptyMessage(MSG_DLNA_UI_SET_FIX_CENTER);
            mLog.d(TAG, "setScaleTypeFixCenter");
        }
    }

    public Drawable getWrfbm() {
        return wrfbm;
    }

    /**
     * @author zWX160481 旋转图片
     * @since 2013-4-12
     * @param key 区分的up还是dowm
     */
    public void rotatePic(int key, GifView mGifView) {
        mLog.d(TAG, "rotatePic--->Start");
        View curView = getCurrentView();
        BitmapDrawable bmpda = (BitmapDrawable) getWrfbm();
        mLog.d(TAG, "rotatePic--->bmpda-->" + bmpda);
        if (curView != null) {
            // 当前显示图片的长宽
            int BitmapW = 0;
            int BitmapH = 0;
            if (bmpda != null) {
                BitmapW = getCurrBitmapWith();
                BitmapH = getCurrBitmapHeight();
                bmpda = null;
                mLog.d(TAG, "rotatePic--->BitmapW-->" + BitmapW);
                mLog.d(TAG, "rotatePic--->BitmapH-->" + BitmapH);
            } else {
                // 图片旋转失败
                if (mListener != null) {
                    mListener.rotateIamgeFailed();
                }
                return;
            }

            // 图片旋转处理
            if (KeyEvent.KEYCODE_DPAD_UP == key) {
                mDegree += 90;
                curView.setRotation(mDegree);
                if (mGifView != null) {
                    mGifView.setRotation(mDegree);
                }
                if (mDegree == 360) {
                    mDegree = 0;
                }
            } else if (KeyEvent.KEYCODE_DPAD_DOWN == key) {
                mDegree -= 90;
                curView.setRotation(mDegree);
                if (mGifView != null) {
                    mGifView.setRotation(mDegree);
                }
                if (mDegree == -360) {
                    mDegree = 0;
                }
            }

            mLog.d(TAG, "rotatePic--->UriTexture.mPicSizeX-->"
                    + UriTexture.mPicSizeX);
            // 图片拉伸处理
            if (UriTexture.mPicSizeX >= UriTexture.FIX_BITMAP_MAX_H) {
                mLog.d(TAG, "rotatePic--->mDegree-->" + mDegree);
                UriTexture.parseScale(curView, mDegree, BitmapW, BitmapH);
            }
        } else {
            // 图片旋转失败
            if (mListener != null) {
                mListener.rotateIamgeFailed();
            }
        }

        mLog.d(TAG, "rotatePic--->end");
    }

    /**
     * 恢复图片的旋转之前的设置
     */
    private void resetRotate() {
        if (mImageView != null) {
            // mImageView.setRotation(0);
            // mImageView.setScaleX(1);
            // mImageView.setScaleY(1);
            mDegree = 0;
        }
    }

    public int getDegree() {
        return mDegree;
    }

    public void setDegree(int mDegree) {
        this.mDegree = mDegree;
    }

    /**
     * 获取当前显示的图片的with
     */
    private int getCurrBitmapWith() {
        int with = 0;
        if (getWrfbm() != null) {
            BitmapDrawable bmpda = (BitmapDrawable) getWrfbm();
            if (bmpda != null && bmpda.getBitmap() != null) {
                with = bmpda.getBitmap().getWidth();
            }
            bmpda = null;
        }
        return with;
    }

    /**
     * 获取当前显示的图片的Height
     */
    private int getCurrBitmapHeight() {
        int height = 0;
        if (getWrfbm() != null) {
            BitmapDrawable bmpda = (BitmapDrawable) getWrfbm();
            if (bmpda != null && bmpda.getBitmap() != null) {
                height = bmpda.getBitmap().getHeight();
            }
            bmpda = null;
        }
        return height;
    }

    public boolean isPlayCloudPic() {
        return mbPlayCloudPic;
    }

    public void setPlayCloudPic(boolean mbPlayCloudPic) {
        this.mbPlayCloudPic = mbPlayCloudPic;
    }

    @Override
    public void displayProgreeBar() {
        mLog.d(TAG, "displayProgreeBar()-----1");
        if (uiHandler != null) {
            if (mProgressBar != null)
            {
                mLog.d(TAG, "mProgressBar----->" + mProgressBar);
                mLog.d(TAG, "mProgressBar.getVisibility()----->" + mProgressBar.getVisibility());
                mLog.d(TAG, "displayProgreeBar()-----2");
                uiHandler.removeMessages(MSG_DLNA_UI_IS_AFTER_PLAY);
                // 没有播放图片前设置进度条为可见
                uiHandler.sendEmptyMessage(MSG_DLNA_UI_IS_BEFORE_PLAY);
                uiHandler.removeMessages(MSG_DLNA_UI_IS_AFTER_PLAY);
            }

        }
    }

    public boolean isPressLeftOrRight() {
        return mbPressLeftOrRight;
    }

    public void setPressLeftOrRight(boolean mbPressLeftOrRight) {
        this.mbPressLeftOrRight = mbPressLeftOrRight;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String mImageUrl) {
        this.mImageUrl = mImageUrl;
    }

    public boolean isInternalPlayer() {
        return mbInternalPlayer;
    }

    public void setInternalPlayer(boolean mbInternalPlayer) {
        this.mbInternalPlayer = mbInternalPlayer;
    }

    public void setAutoPlayArg(boolean isAuto, int interval) {
        if (mDLNAISHandler != null) {
            mDLNAISHandler.setAutoPlayMode(isAuto);
            mDLNAISHandler.setInterval(interval);
        }
    }

    /* BEGIN: Added by c00224451 for DTS2014021408267 2014/2/18 */
    private void setAnimationParams() {
        if (mImagePlaySetHelper != null && mbInternalPlayer) {
            int animEffect = mImagePlaySetHelper.getSwitchWith();
            setAnimation(animEffect);
        }

    }
    /* END: Added by c00224451 for DTS2014021408267 2014/2/18 */
}
