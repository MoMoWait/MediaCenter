/**
 * @author GaoFei
 * 音乐播放模块
 */
package com.rockchips.mediacenter.audioplayer;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import momo.cn.edu.fjnu.androidutils.utils.DeviceInfoUtils;
import momo.cn.edu.fjnu.androidutils.utils.SizeUtils;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.rockchips.mediacenter.R;
import com.rockchips.mediacenter.service.LocalDeviceManager;
import com.rockchips.mediacenter.bean.Device;
import com.rockchips.mediacenter.bean.LocalDeviceInfo;
import com.rockchips.mediacenter.bean.LocalMediaInfo;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.data.ConstData.AudioPlayerMsg;
import com.rockchips.mediacenter.data.ConstData.EBrowerType;
import com.rockchips.mediacenter.data.ConstData.MediaType;
import com.rockchips.mediacenter.data.ConstData.PlayState;
import com.rockchips.mediacenter.utils.BitmapUtil;
import com.rockchips.mediacenter.utils.CharsetUtils;
import com.rockchips.mediacenter.utils.DateUtil;
import com.rockchips.mediacenter.utils.ID3V2;
import com.rockchips.mediacenter.utils.IICLOG;
import com.rockchips.mediacenter.utils.Lyric;
import com.rockchips.mediacenter.utils.SearchLrc;
import com.rockchips.mediacenter.utils.UriTexture;
import com.rockchips.mediacenter.bean.PlayStateInfo;
import com.rockchips.mediacenter.service.IMediaPlayerAdapter;
import com.rockchips.mediacenter.service.IVideoViewAdapter;
import com.rockchips.mediacenter.bean.AudioInfoOfVideo;
import com.rockchips.mediacenter.service.OnBufferingUpdateListener;
import com.rockchips.mediacenter.service.OnCompleteListener;
import com.rockchips.mediacenter.service.OnErrorListener;
import com.rockchips.mediacenter.service.OnInfoListener;
import com.rockchips.mediacenter.service.OnPreparedListener;
import com.rockchips.mediacenter.service.OnSeekCompleteListener;
import com.rockchips.mediacenter.view.OrigVideoViewNoView;
import com.rockchips.mediacenter.activity.MainActivity;
import com.rockchips.mediacenter.audioplayer.AudioPlayStateInfo.OnPlayListSyncCompletedListener;
import com.rockchips.mediacenter.dobly.DoblyPopWin;
import com.rockchips.mediacenter.imageplayer.DLNAImageSwitcher;
import com.rockchips.mediacenter.imageplayer.DLNAImageSwitcher.DLNAImageSwitcherListener;
import com.rockchips.mediacenter.retrieve.RetrieveCompleteListener;
import com.rockchips.mediacenter.retrieve.RetrieveInfoManager;
import com.rockchips.mediacenter.view.AudioSettingsDialog;
import com.rockchips.mediacenter.view.ListSelectItem;
import com.rockchips.mediacenter.view.ListSelectPopup;
import com.rockchips.mediacenter.view.ListSelectPopup.OnSelectPopupListener;
import com.rockchips.mediacenter.view.BottomPopMenu;
import com.rockchips.mediacenter.view.MenuCategory;
import com.rockchips.mediacenter.view.MenuItemImpl;
import com.rockchips.mediacenter.view.OnSelectTypeListener;
import com.rockchips.mediacenter.view.GlobalFocus;
import com.rockchips.mediacenter.view.PlayListView;
import com.rockchips.mediacenter.view.PlayListView.OnItemTouchListener;
import com.rockchips.mediacenter.view.PlayListView.OnMenuListener;
import com.rockchips.mediacenter.view.PreviewWidget;
import com.rockchips.mediacenter.utils.ToastUtil;
import com.rockchips.mediacenter.view.OnWheelChangedListener;
import com.rockchips.mediacenter.view.WheelView;
import com.rockchips.mediacenter.utils.StringUtils;
/**
 */
public class AudioPlayerActivity extends PlayerBaseActivity implements OnWheelChangedListener, OnKeyListener, OnClickListener, OnSelectTypeListener,
        OnSelectPopupListener, DLNAImageSwitcherListener
{
    private static final String TAG = "AudioPlayer_REAL";
    private IICLOG Log = IICLOG.getInstance();
    private static final String ACTION = "com.rockchips.iptv.stb.dlna.action.exitplayer";

    private boolean bResumePlay;

    private DLNAImageSwitcher mImageSwitcher = null;

    /**
     * 歌词是否下载成功
     */
    private boolean isLrcReady = false;

    private boolean isPrepared = false;

    protected static final int TWO_MUSIC_INTERVAL = 12 * 1000;
    // 底部菜单
    private BottomPopMenu mBottomPopMenu = null;

    /**
     * 音乐播放器对象
     */
    private MusicPlayer mMusicPlayer;

    /**
     * 进度条
     */
    private SeekBar mSeekBar;

    /**
     * 播放列表组件
     */
    private PlayListView mPlayListView;

    /**
     * 播放列表数据适配器
     */
    private TextViewListAdapter mAdapter;

    /**
     * 整个布局父视图
     */
    private LinearLayout mParentLinear;

    /**
     * 专辑信息组件，包括封面、专辑名称、艺术家
     */
    private PreviewWidget mAlbumInfoView;

    /**
     * 播放模式显示文本
     */
    private TextView mPlayModeText;

    private Object mHandlerLock = new Object();

    // 是否退出音乐播放器界面
    private boolean mBExitPage;

    /**
     * 当前播放的媒体对象
     */
    private LocalMediaInfo mCurrentMediaInfo;

    private int mSeekValue;

    private long mBeginPrepareTime;

    private long mEndPrepareTime;

    /**
     * 数据处理handler
     */
    private Handler mLogicalHandler;

    /**
     * 数据处理线程
     */
    private HandlerThread mLogicalThread;

    /**
     * 音乐是否正在播放
     */
    private boolean mIsPlaying;

    private boolean mIsPauseByOkKey;

//    private static final String ACTION_ON_AUDIO_PLAYED = "com.rockchips.iptv.stb.dlna.audioplayer.action.audioplayed";

    /**
     * 进度条最大进度值
     */
    private static final int PROGRESS_MAXVALUE = 1000;

    /**
     * seek步长
     */
    private static final int SEEK_BASE_STEP = 10000;

    /**
     * 播放列表是否正在聚焦，当按上下键时播放列表获得焦点，5秒之后失去焦点
     */
    private boolean mMusicListFocused;

    /**
     * 播放模式图标
     */
    private ImageView mPlayModeIcon;

    /**
     * 已播放进度
     */
    private TextView mMusicAlreadyPlayedDuration;

    /**
     * 总进度
     */
    private TextView mMusicTotalDuration;

    /**
     * 左边歌词
     */
    private TextView mLeftLyric;

    /**
     * 右边歌词
     */
    private TextView mRightLyric;

    /* BEGIN: Modified by c00224451 for 新增音乐播放，歌词滚动效果 2014/3/26 */
    private TextView mCurrFocusLyric;

    private String mCurrPlayLyric;

    /* END: Modified by c00224451 for 新增音乐播放，歌词滚动效果 2014/3/26 */

    private GlobalFocus mGlobalFocus;

    private Bitmap mDefaultMusicBitmap;

    private PopupWindow mPlayModeSetting;

    private MusicPlayModeAdapter mMusicPlayModeAdapter;

    private WheelView mWheelView;

    private boolean mPlayModeSettingFocused;

    private boolean mMusicPageFocused = true;

    private static final int MSG_REQUEST_PLAYMODESETTING_MENU_DISMISS = 700;

    private static final int MSG_REQUSET_EXIT_MUSICPLAYER = 701;

    private static final int MSG_REQUSET_REFRESH_CURRENT_POSITION = 702;

    private static final int MSG_REQUSET_EXIT_MUSICPLAYER_SEEK = 703;
    
    private static final int MSG_REQUEST_PLAY_MUSIC = 704;

    private static final int THUMBNAIL_WIDTH = 280;

    private static final int THUMBNAIL_HEIGHT = 280;

    private int mCurrentPlayIndex;

    private boolean mbSeeking;

    private boolean mbSeekKeyUp;

    private String[] mFilter =
    { ",", "/", "-", ".", "、" };

    // 若片源是杜比音效，则必须弹出杜比标识
    private DoblyPopWin doblyPopWin;

    // 菜单
    protected AudioSettingsDialog mPopMenu;

    // 背景图片下标
    private int mCurrentImageIndex;

    // 没有图片时显示的进度条
    private ProgressBar mProgressBar;

    // 无歌词提示信息
    private TextView mNoLyricText;

    // 显示图片时弹出的歌曲信息
    private BackgroundAudioPreviewWidget mBackgroundAudioPreviewWidget;

    /**
     * 播放间隔 默认为5秒
     */
    private static final int AUTO_PLAY_INTERVAL = 5000;

    // 添加杜比的弹出消息
    protected static final int MSG_DOBLY_SHOW = 2222;

    protected static final int MSG_DOBLY_HIDE = 2223;

    protected static final int MSG_SHOW_BACKGRPUND_PICS = 2225;

    /**
     * 设置音乐歌词提示消息
     */
    public static final int MSG_SETTING_LYRIC = 2226;

    public static final int MSG_HIDE_LYRIC = 2227;

    // private MediaplayerPriInterface mPriInterface;

//    private AudioManager mAudioManager;

    private RelativeLayout mIMPRL; // 背景图片播放的layout

    private LinearLayout mDisplayException;

    private boolean mIsPicActivityShow;

    private boolean mShowDefaultBg;

    private static boolean mStIsShowBackgroundPic = true;

    // Sharedpreferences名字
    private static final String PERFS_NAME = "audioSetting";
    
    private static final String PERFS_DEVICE_TYPE = "PERFS_DEVICE_TYPE";
    private static final String PERFS_DEVICE_ID = "PERFS_DEVICE_ID";
    private static final String PERFS_BG_IMAGE_URLS = "PERFS_BG_IMAGE_URLS";
    
    private static final int INVALID_VALUE = -1;
    
    public static AudioPlayerActivity mInstance;

    // zkf61715 当多个菜单选项变化时，而主选不在背景图片这一栏
    private boolean mBackgroundRet = true; // 为了避免再次进入音乐播放界面时只显示动态背景 初始值设为true

    // zkf61715 首次进入播放界面
    private boolean mFirstShowBg = true;

    private Bundle mBundle;

    // DTS2014011707941 是否需要在播放之后seek.
    private boolean mIsResumeNeedSeek;

    // DTS2014011707941 如果需要seek,这个值就是需要seek到那个位置。
    private int mResumeNeedSeekValue;

    private List<LocalMediaInfo> mMediaList = new ArrayList<LocalMediaInfo>();

    protected LocalDeviceManager mLocalDeviceManager;   
    
    private PlayStateInfo mImagePlayStateInfo;

    //    private RetrieveInfoManager mAsyncRetrieveMediaInfo;
    //    private Thread mRetrieveMediaInfoThread;
    private int mMCSSeekTarget = 0;
    private static final int BG_IMAGE_SHOW_DELAY_TIME = 10 * 1000;
    private static final int TOAST_SHOW_TIME = 500;
    private Device mCurrentDevice;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    	mCurrentDevice = (Device)getIntent().getSerializableExtra(ConstData.IntentKey.EXTRAL_LOCAL_DEVICE);
    	if(mCurrentDevice == null){
    		mCurrentDevice = new Device();
    		mCurrentDevice.setDeviceType(ConstData.DeviceType.DEVICE_TYPE_OTHER);
    	}
        SearchLrc.isInternalPlayer(true);
        
        mImagePlayStateInfo = new PlayStateInfo();
        
        super.onCreate(savedInstanceState);
    }

    // 获取背景图片标志
    private void getBackgroundPicFlag()
    {
       /* SharedPreferences sp = getAudioSettingsSharedPreferences();
        boolean flag = sp.getBoolean("isShowBackgroundPic", true);
        mStIsShowBackgroundPic = flag;*/
    	mStIsShowBackgroundPic = false;
    }

    // 设置背景图片是否开启
    private void setBackgroundPicFlag(boolean flag)
    {
        SharedPreferences sp = getAudioSettingsSharedPreferences();
        Editor ed = sp.edit();
        ed.putBoolean("isShowBackgroundPic", flag);
        ed.commit();
    }

    public void loadPlayModeSettingLayout()
    {
        mMusicPlayModeAdapter = new MusicPlayModeAdapter(this);
        List<String> list = new ArrayList<String>();
        list.add(getResources().getString(R.string.play_mode_random_play));
        list.add(getResources().getString(R.string.play_mode_sequence_play));
        list.add(getResources().getString(R.string.play_mode_single_play));
        list.add(getResources().getString(R.string.play_mode_loop_play));

        mMusicPlayModeAdapter.setTypeItemList(list);

        mPlayModeSetting = new PopupWindow(getLayoutInflater().inflate(R.layout.playmode_setting_page, null));
        mPlayModeSetting.setAnimationStyle(R.style.MusicPopupWindowAnimation);
        mPlayModeSetting.setWidth(300);
        mPlayModeSetting.setHeight(493);

        mWheelView = (WheelView) mPlayModeSetting.getContentView().findViewById(R.id.music_type);

        mWheelView.setViewAdapter(mMusicPlayModeAdapter);

        mWheelView.setFocusable(true);
        mWheelView.addChangingListener(this);
        mWheelView.setOnKeyListener(this);
        mWheelView.setCurrentItem(1);
        mWheelView.setCyclic(true);
    }

    @Override
    protected int getUUID()
    {
        return ConstData.MediaType.AUDIO;
    }

    @Override
    protected int getMediaType()
    {
        return ConstData.MediaType.AUDIO;
    }

    @Override
    protected int onDeleteDeviceId(String devId)
    {
        return 0;
    }

    @Override
    protected void onDestroy()
    {
        Log.d(TAG, "---->enter onDestroy() IN...");
        // zkf61715
        removeUiMessage(MSG_SHOW_BACKGRPUND_PICS);
        mChangeLyricColorHandler.removeMessages(0);
        if (mImageSwitcher != null)
        {
            mImageSwitcher.stopDLNAHandler();
            mImageSwitcher.uninit();
            mImageSwitcher = null;
        }
        super.onDestroy();
        mIsResumeNeedSeek = false;
        mResumeNeedSeekValue = 0;
        releaseResource();
        destoryMediaRetrieve();
        Log.d(TAG, "---->enter onDestroy() OUT...");
    }

    @Override
    protected void onStop()
    {
        Log.d(TAG, "---->enter onStop()");
        sendDoblyWinMsg(false);
        super.onStop();
        
        mMusicPlayer.release();
        removeLogicalMessage(AudioPlayerMsg.MSG_SYNC_POSTION);
        removeUiMessage(AudioPlayerMsg.MSG_UPDATE_MUSIC_PROGRESS);        
    }

    @Override
    protected void onPause()
    {
        Log.d(TAG, "---->enter onPause()");

        //        ThumbnailManager.getInstance().cancelRequestThumbnail(mThumbnailChangedListener);
        if (!mIsPicActivityShow)
        {
            onPause2StopMediaPlayer();
        }

        removeUiMessage(MSG_SHOW_BACKGRPUND_PICS);
        if (mBackgroundAudioPreviewWidget.isShown())
        {
            mBackgroundAudioPreviewWidget.hide();
        }		
        super.onPause();
    }

    // DTS2014011707941 android 4.2 平台，不支持多个MeidaPlay , Activity 进入pause后先释放在OnResume时重新播放
    public void onPause2StopMediaPlayer()
    {
        mBExitPage = true;
        int pausePosition = 0;
        synchronized (mPlayerLock)
        {
            if (mMediaPlayer != null)
            {
                pausePosition = mMediaPlayer.getCurrentPosition();
            }
        }
        int currentIndex = mAudioPlayStateInfo.getCurrentIndex();
       
        Log.d(TAG, "onPause save current seekValue = " + mSeekValue + " and , currentIndex = " + currentIndex);
        onPauseSaveValue(pausePosition, currentIndex);
        stop(true);
        bResumePlay = true;
    }

    // DTS2014011707941 android 4.2 平台，不支持多个MeidaPlay , Activity 进入pause后先释放在OnResume时重新播放
    private void onResume2StartMediaPlayer()
    {
        mIsResumeNeedSeek = true;
        Bundle bundle = onResumeReadValue();
        mResumeNeedSeekValue = bundle.getInt("position");
        int currentIndex = bundle.getInt("index");
        Log.d(TAG, "onResume read value seekValue = " + mResumeNeedSeekValue + "  , currentIndex = " + currentIndex);
        // send msg or called this req
        removeLogicalMessage(AudioPlayerMsg.MSG_REQUEST_PLAY);
        sendLogicalMessage(obtainLogicalMessage(AudioPlayerMsg.MSG_REQUEST_PLAY, 0, 0, mAudioPlayStateInfo.getMediaInfo(currentIndex)),
                0);
        bResumePlay = false;
    }

    // DTS2014011707941 onPause 如果音乐正在播放，停止播放，保存seek
    private void onPauseSaveValue(int position, int currentIndex)
    {
        if (!mBMCSMode)
        {
            Log.d(TAG, "pausePosition = " + position + " ,  currentIndex = " + currentIndex);
            SharedPreferences sp = getSharedPreferences("saveMusicPosition.xml", Context.MODE_PRIVATE);
            Editor edit = sp.edit();
            edit.putInt("position", position);
            edit.putInt("index", currentIndex);
            edit.commit();
        }
    }

    // DTS2014011707941 onResume 取是否是上次pause seek 清空值。
    private Bundle onResumeReadValue()
    {
        SharedPreferences sp = getSharedPreferences("saveMusicPosition.xml", Context.MODE_PRIVATE);

        Bundle bundle = new Bundle();
        bundle.putInt("position", sp.getInt("position", 0));
        bundle.putInt("index", sp.getInt("index", 0));
        // 读取之后清空值
        // FIXME ondestry 在清除 sp.edit().clear().commit();
        return bundle;
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);

        // mResumePlay = false;
        // AudioPlayer app = (AudioPlayer) this.getApplication();
        // if (app.getAudioPlayerModule() != null ) {
        // app.getAudioPlayerModule().stop(true);
        // }

        // bResumePlay = false;
        // stop(true);
    }

    @Override
    protected void onResume()
    {
        Log.d(TAG, "---->enter onResume()");
        createMediaRetrieve();
        super.onResume();
        mBExitPage = false;

        if (bResumePlay)
        {
            onResume2StartMediaPlayer();
        }
        Message msg = new Message();
        msg.what = MSG_SHOW_BACKGRPUND_PICS;
        boolean fromResume = true;
        msg.obj = fromResume;

        // zkf61715 DTS2014011516299 10秒显示
        if (mFirstShowBg)
        {
            sendUiMessage(msg, 2 * BG_IMAGE_SHOW_DELAY_TIME);
        }
        else
        {
            sendUiMessage(msg, BG_IMAGE_SHOW_DELAY_TIME);
        }

        Log.d(TAG, "current deviceId " + mAudioPlayStateInfo.getDeviceId());
    }

    private int mLastKeyCode = -1;

    private long mKeyPressedTimeTick = System.currentTimeMillis();

    private long mNowTick;

    private static final int KEYDOWN_DELAY_TIME = 100;

    private Object mSeekLock = new Object();

    private static final int SEEK_LEFT_DURATION = 3000;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        Log.d(TAG, "proc KEYCODE_DPAD_CENTER IN..." + keyCode);
        int targetIndex = -1;
        mNowTick = System.currentTimeMillis();
        if (mIMPRL.isShown() && keyCode != KeyEvent.KEYCODE_VOLUME_DOWN && keyCode != KeyEvent.KEYCODE_VOLUME_UP)
        {
            mIMPRL.setVisibility(View.GONE);
            mImageSwitcher.setAutoMode(false, 0);
            if (mBackgroundAudioPreviewWidget.isShown())
            {
                mBackgroundAudioPreviewWidget.hide();
            }
            return true;
        }

        removeUiMessage(MSG_SHOW_BACKGRPUND_PICS);

        switch (keyCode)
        {
            case KeyEvent.KEYCODE_DPAD_CENTER:
                Log.d(TAG, "proc KEYCODE_DPAD_CENTER IN...");
                // 没有显示弹出菜单时，切换暂停、播放
                if (mMusicPageFocused)
                {
                    if (mMusicListFocused)
                    {
                        requestPlay(mPlayListView.getCurrentItem());
                        mPlayListView.processMarquee();
                    }
                    else
                    {
                        /* BEGIN: Modified by c00224451 for 新增音乐播放，歌词滚动效果 2014/3/26 */
                        if (isPlaying())
                        {
                            if (mUiHandler != null)
                            {
                                mUiHandler.removeMessages(AudioPlayerMsg.MSG_SYNC_LYRIC);
                            }
                            mNextLineLyricDuration -= (System.currentTimeMillis() - mCurrPauseTimeStamp);
                            mChangeLyricColorHandler.removeMessages(0);
                            sendLogicalMessage(AudioPlayerMsg.MSG_CONTROL_PAUSE, 0);

                        }
                        else
                        {
                            if (mIsPauseByOkKey)
                            {
                                sendUiMessage(AudioPlayerMsg.MSG_SYNC_LYRIC, mNextLineLyricDuration < 0 ? 0 : mNextLineLyricDuration);
                                mChangeLyricColorHandler.removeMessages(0);
                                mChangeLyricColorHandler.sendEmptyMessage(0);
                            }

                            sendLogicalMessage(AudioPlayerMsg.MSG_CONTROL_PLAY, 0);
                        }
                        /* END: Modified by c00224451 for 新增音乐播放，歌词滚动效果 2014/3/26 */
                    }
                }

                // 当显示弹出菜单时，按确定键切换播放模式
                else
                {
                    switchPlayMode();
                }
                mLastKeyCode = keyCode;
                mKeyPressedTimeTick = mNowTick;
                return true;

            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (mMusicPageFocused)
                {
                    if (mLastKeyCode == keyCode && (mNowTick - mKeyPressedTimeTick) < KEYDOWN_DELAY_TIME)
                    {
                        return true;
                    }

                    targetIndex = mPlayListView.getCurrentItem() + 1;
                    if (targetIndex < mAudioPlayStateInfo.getMediaList().size())
                    {
                        mGlobalFocus.setVisibility(View.VISIBLE);
                        sendUiMessage(obtainUiMessage(AudioPlayerMsg.MSG_RANDOM_SCROLL_PLAYLIST, targetIndex, 0, false), 0);
                    }
                    else
                    {
                        ToastUtil.showBySetDur(getBaseContext().getResources().getString(R.string.reach_last_item), TOAST_SHOW_TIME);
                    }
                    mLastKeyCode = keyCode;
                    mKeyPressedTimeTick = mNowTick;
                }
                else if (mPlayModeSettingFocused)
                {
                    int cuItem = mWheelView.getCurrentItem();
                    cuItem += 1;
                    mWheelView.setCurrentItem(cuItem, true);
                    sendPlayModeMenuDismissMsg(AudioPlayerMsg.POPUPWINDOW_DISMISS_DELAYMILLIS);
                }
                return true;
            case KeyEvent.KEYCODE_DPAD_UP:
                if (mMusicPageFocused)
                {
                    if (mLastKeyCode == keyCode && (mNowTick - mKeyPressedTimeTick) < KEYDOWN_DELAY_TIME)
                    {
                        return true;
                    }

                    targetIndex = mPlayListView.getCurrentItem() - 1;
                    if (targetIndex >= 0)
                    {
                        ToastUtil.hide();
                        mGlobalFocus.setVisibility(View.VISIBLE);
                        sendUiMessage(obtainUiMessage(AudioPlayerMsg.MSG_RANDOM_SCROLL_PLAYLIST, targetIndex, 0, false), 0);
                    }
                    mLastKeyCode = keyCode;
                    mKeyPressedTimeTick = mNowTick;
                }
                else if (mPlayModeSettingFocused)
                {
                    int cuItem = mWheelView.getCurrentItem();
                    cuItem -= 1;
                    mWheelView.setCurrentItem(cuItem, true);
                    sendPlayModeMenuDismissMsg(AudioPlayerMsg.POPUPWINDOW_DISMISS_DELAYMILLIS);
                }
                return true;

            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_FORWARD:

                if (mMusicPageFocused)
                {
                    // 按左右键seek时，立刻隐藏焦点框。
                    if (mGlobalFocus.getVisibility() == View.VISIBLE)
                    {
                        removeUiMessage(AudioPlayerMsg.MSG_REQUEST_PLAYLIST_RESTORE);
                        removeUiMessage(MSG_REQUEST_FOCUS_DISMISS);
                        mGlobalFocus.setVisibility(View.INVISIBLE);
                    }

                    if (mLastKeyCode == keyCode && (mNowTick - mKeyPressedTimeTick) < KEYDOWN_DELAY_TIME)
                    {
                        return true;
                    }

                    if (isMusicPlayerReady())
                    {
                        synchronized (mSeekLock)
                        {
                            if (mbSeeking || mbSeekKeyUp)
                            {
                                return true;
                            }

                            if (realSeekValue == -1)
                            {
                                realSeekValue = mMusicPlayer.getCurrentPosition();
                            }

                            realSeekValue += SEEK_BASE_STEP;
                            // requestRefreshCurrentPosition();
                            sendLogicalMessage(MSG_REQUSET_REFRESH_CURRENT_POSITION, 0);
                        }
                    }
                    mLastKeyCode = keyCode;
                    mKeyPressedTimeTick = mNowTick;
                }
                return true;

            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_MEDIA_REWIND:
                if (mMusicPageFocused)
                {
                    // 按左右键seek时，立刻隐藏焦点框。
                    if (mGlobalFocus.getVisibility() == View.VISIBLE)
                    {
                        removeUiMessage(AudioPlayerMsg.MSG_REQUEST_PLAYLIST_RESTORE);
                        removeUiMessage(MSG_REQUEST_FOCUS_DISMISS);
                        mGlobalFocus.setVisibility(View.INVISIBLE);
                    }
                    if (mLastKeyCode == keyCode && (mNowTick - mKeyPressedTimeTick) < KEYDOWN_DELAY_TIME)
                    {
                        return true;
                    }

                    if (isMusicPlayerReady())
                    {
                        synchronized (mSeekLock)
                        {
                            if (mbSeeking || mbSeekKeyUp)
                            {
                                return true;
                            }

                            if (realSeekValue == -1)
                            {
                                realSeekValue = mMusicPlayer.getCurrentPosition();
                            }

                            realSeekValue = realSeekValue - SEEK_BASE_STEP;
                            sendLogicalMessage(MSG_REQUSET_REFRESH_CURRENT_POSITION, 0);
                            // requestRefreshCurrentPosition();
                        }
                    }

                    mLastKeyCode = keyCode;
                    mKeyPressedTimeTick = mNowTick;
                }
                return true;

            case KeyEvent.KEYCODE_MENU:
                Log.d(TAG, "process menu key 2...");
                // createOptionsMenu(true);
                // onMenuOpened();
                createBottomPopMenu();
                break;

            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (mAudioManager == null)
                {
                    Log.d(TAG, "audioManager == null, create a AudioManager object");
                    mAudioManager = (AudioManager) this.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                }

                int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                float volumePercent = (float) currentVolume / (float) maxVolume;
                Log.d(TAG, "currentVolume:" + currentVolume);
                Log.d(TAG, "maxVolume:" + maxVolume);
                Log.d(TAG, "volumePercent:" + volumePercent);

                if (mMediaCenterPlayerClient != null)
                {
                    Log.d(TAG, "Send the volume percent to Sender client");
                    mMediaCenterPlayerClient.adjustVolume(ConstData.VolumeAdjustType.ADJUST_SET, volumePercent);
                }

                return super.onKeyDown(keyCode, event);

            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_MEDIA_STOP:
                Log.d(TAG, "proc KEYCODE_BACK IN...");
                if (mIMPRL.isShown())
                {
                    if (mImageSwitcher != null)
                    {
                        Log.d(TAG, "onPause()--->setAutoMode");
                        mImageSwitcher.setAutoMode(false, 0);
                    }
                    mIMPRL.setVisibility(View.GONE);
                }
                else
                {
                    if (mPlayModeSettingFocused)
                    {
                        sendPlayModeMenuDismissMsg(0);
                    }
                    else
                    {
                        passIntentForAudioBrowser();
                        AudioPlayStateInfo.recycle();
                        releaseResource();
                        this.finish();
                    }
                }
                return true;

            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                if (mMusicPageFocused)
                {
                    if (isPlaying())
                    {
                        sendLogicalMessage(AudioPlayerMsg.MSG_CONTROL_PAUSE, 0);

                    }
                    else
                    {
                        sendLogicalMessage(AudioPlayerMsg.MSG_CONTROL_PLAY, 0);
                    }
                }
                return true;
        }
        return super.onKeyDown(keyCode, event);

    }

    public void sendPlayModeMenuDismissMsg(int delayMillis)
    {
        removeUiMessage(MSG_REQUEST_PLAYMODESETTING_MENU_DISMISS);
        sendUiMessage(MSG_REQUEST_PLAYMODESETTING_MENU_DISMISS, delayMillis);
    }

    private int realSeekValue = -1;

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_MENU)
        {
            // Menu键自带5s的显示时间要包含进去
            sendUiMessage(MSG_SHOW_BACKGRPUND_PICS, 11000);
        }
        else
        {
            sendUiMessage(MSG_SHOW_BACKGRPUND_PICS, BG_IMAGE_SHOW_DELAY_TIME);
        }

        switch (keyCode)
        {
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_MEDIA_REWIND:
            case KeyEvent.KEYCODE_FORWARD:
                Log.d(TAG, "onKeyUp() IN...,keyCode " + keyCode);
                if (mMusicPageFocused)
                {

                    if (isMusicPlayerReady())
                    {
                        int seekValue = -1;
                        synchronized (mSeekLock)
                        {
                            seekValue = realSeekValue;
                            if (realSeekValue != -1)
                            {
                                mbSeekKeyUp = true;
                                mbSeeking = true;
                            }
                        }
                        if (-1 != seekValue)
                        {
                            mMusicPlayer.seekTo(seekValue);
							sendLogicalMessage(AudioPlayerMsg.MSG_CONTROL_PLAY, 0);
                        }
                    }
                    return true;
                }
        }

        return super.onKeyUp(keyCode, event);
    }

    // @Override
    // public void onBackPressed()
    // {
    // Log.d(TAG, "onBackPressed() IN...");
    // if (mPlayModeSettingFocused)
    // {
    // sendPlayModeMenuDismissMsg(0);
    // }
    // else
    // {
    // passIntentForAudioBrowser();
    // super.onBackPressed();
    // }
    // }

    // 歌词处理线程
    private class LyricThread extends Thread
    {

        private LocalMediaInfo mMediaInfo = null;

        public LyricThread(LocalMediaInfo hMediaInfo)
        {

            mMediaInfo = hMediaInfo;

        }

        @Override
        public void run()
        {
            if (mMediaInfo != null)
            {

                // 检查当前媒体的Url
                String Url = mMediaInfo.getUrl();
                String title = null;
                String artist = null;

                if (StringUtils.isEmpty(Url))
                {

                    return;
                }

                Log.d(TAG, "LYRIC_PARSE-------------------------" + Url);

                if (Url.trim().startsWith("content://"))
                {
                    DBUtils.fillValuesByDisplayName(mMediaInfo, getApplicationContext(), Url);
                }

                if (ConstData.DeviceType.isLocalDevice(mMediaInfo.getmDeviceType()))
                {
                    if (StringUtils.isEmpty(mMediaInfo.getmArtist()))
                    {
                        // 如果是U盘还需查找歌手名称、专辑名称等
                        if (Url.trim().endsWith(".mp3"))
                        {
                            RandomAccessFile ran = null;
                            try
                            {
                                // 按ID3V1读取
                                File file = new File(Url);
                                if (!file.exists())
                                {
                                    return;
                                }
                                ran = new RandomAccessFile(file, "r");

                                byte[] buffer = new byte[128];
                                ran.seek(ran.length() - 128);
                                ran.read(buffer);

                                SongInfo songInfo = new SongInfo(buffer);
                                if (StringUtils.isNotEmpty(songInfo.getArtist()))
                                {
                                    mMediaInfo.setmArtist(CharsetUtils.tranEncode(songInfo.getArtist()));

                                    if (StringUtils.isEmpty(mMediaInfo.getmAlbum()))
                                    {
                                        if (StringUtils.isNotEmpty(songInfo.getAlbum()))
                                        {
                                            mMediaInfo.setmAlbum(CharsetUtils.tranEncode(songInfo.getAlbum()));
                                        }
                                    }
                                }
                                else
                                {
                                    // ID3V1未读取信息，转为按ID3V2读取
                                    File f = new File(Url);
                                    ID3V2 id3v2 = new ID3V2(f);

                                    id3v2.initialize();

                                    mMediaInfo.setmArtist(id3v2.tpe1());
                                    mMediaInfo.setmAlbum(id3v2.talb());
                                }
                                requestRefreshMediaInfo();
                            }
                            catch (Exception e)
                            {
                            }
                            finally
                            {
                                if (ran != null)
                                {
                                    try
                                    {
                                        ran.close();
                                    }
                                    catch (IOException e)
                                    {
                                    }
                                }
                            }
                        }
                    }
                }
                // 查询最后一个点的位置,将后缀名前的名称截取
                int lastPointIndex = Url.lastIndexOf(".");
                if (lastPointIndex > 0 && lastPointIndex < Url.length() - 1)
                {
                    // 合法的点号处在中间，即大于0，小于字符串长度-1
                    String lyricUrl = Url.substring(0, lastPointIndex + 1) + "lrc";

                    if (lyricUrl.trim().startsWith("file://"))
                    {
                        lyricUrl = lyricUrl.substring("file://".length());
                    }

                    if (lyricUrl.trim().startsWith("content://"))
                    {
                        lyricUrl = lyricUrl.substring("content://".length());
                    }

                    try
                    {
                        Lyric lrcParser = Lyric.getInstance();
                        isLrcReady = lrcParser.parserFileToLrc(lyricUrl);

                        if (!isLrcReady)
                        {
                            // 网络搜索歌词
                            title = StringUtils.isNotEmpty(mMediaInfo.getmTitle()) ? mMediaInfo.getmTitle() : mMediaInfo.getmFileName();
                            artist = mMediaInfo.getmArtist();

                            String[] songs = null;
                            if (StringUtils.isNotEmpty(artistFromRetriever))
                            {
                                artist = artistFromRetriever;
                            }

                            if (StringUtils.isNotEmpty(titleFromRetriever))
                            {
                                // 如果歌曲名称中包含有歌手名，则仅使用歌名作为搜索条件。例如凤凰传奇的《最炫民族风》，有时
                                // displayName为凤凰传奇-最炫民族风，则必须过滤掉displayName中包含的歌手名，才能搜索到歌词。
                                boolean isContainReg = false;

                                for (String s : mFilter)
                                {
                                    if (-1 != titleFromRetriever.indexOf(s))
                                    {
                                        isContainReg = true;
                                        songs = titleFromRetriever.split(s);

                                        for (String temp : songs)
                                        {
                                            if (!temp.contains(artist))
                                            {
                                                title = temp;
                                                break;
                                            }
                                        }

                                        break;
                                    }
                                }

                                if (!isContainReg)
                                {
                                    title = titleFromRetriever;
                                }
                                if (StringUtils.isNotEmpty(artist))
                                {
                                    // 如果歌手是多个，则仅使用一个歌手名搜索歌词，使用多个歌手是搜索不到歌词的。例如：陶喆/蔡依林合唱的《今天你要嫁给我》，
                                    // 如果使用“陶喆/蔡依林”作为歌手名，是搜索不到歌词的，但是使用陶喆或者蔡依林，都能搜到到歌词。

                                    artist = artistFromRetriever.replaceAll("-", ",").replaceAll("/", ",").replaceAll("&", ",");  
                                    String[] artists = artist.split(",");
                                    //默认取第一个艺术家去搜索
                                    artist = artists[0]; 
                                } 

                            }

                            Log.d(TAG, "title is: " + title);
                            Log.d(TAG, "artist is: " + artist);
                        }

                        if (StringUtils.isNotEmpty(title) && StringUtils.isNotEmpty(artist))
                        {
                            String lyricPath = SearchLrc.getLyricCachePath(title, artist);
                            if (SearchLrc.isLyricExistInCache(lyricPath))
                            {
                                Log.d(TAG, "lyric has existed in cache!");
                                isLrcReady = lrcParser.parserFileToLrc(lyricPath);
                            }
                            else
                            {
                                Log.d(TAG, "lyric has not existed in cache, start search lyric from internet!");
                                SearchLrc search = new SearchLrc(title, artist);
                                String lycText = search.fetchLyric();
                                Log.d(TAG, "lycText is " + lycText);
                                if (StringUtils.isNotEmpty(lycText))
                                {
                                    isLrcReady = lrcParser.paser(lycText);
                                }
                            }

                        }

                    }
                    catch (Exception ex)
                    {
                    }

                }
            }
            if (isLrcReady)
            {
                Log.d(TAG, "download lyric successfully from internet!");
                requestSyncLyric();
                Message msg = Message.obtain();
                msg.what = MSG_SETTING_LYRIC;
                msg.arg1 = 0;
                sendUiMessage(msg, 0);
            }
            else
            {
                Log.d(TAG, "download lyric failed from internet!");
                removeUiMessage(AudioPlayerMsg.MSG_SYNC_LYRIC);
                Message msg = Message.obtain();
                msg.what = MSG_SETTING_LYRIC;
                msg.arg1 = 1;
                sendUiMessage(msg, 0);
            }
            requestRefreshMediaInfo();
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();
    }

    // 请求退出
    public void requestExit()
    {
        Log.d(TAG, "requestExit....");
		
        if (mIMPRL.isShown())
        {
            if (mImageSwitcher != null)
            {
                Log.d(TAG, "onPause()--->setAutoMode");
                mImageSwitcher.setAutoMode(false, 0);
            }
            mIMPRL.setVisibility(View.GONE);
        }
        passIntentForAudioBrowser();
        AudioPlayStateInfo.recycle();
        releaseResource();
        this.finish();
    }

    public final Message obtainLogicalMessage(int what, int arg1, int arg2, Object obj)
    {

        // Log.d(TAG, "---------->obtainLogicalMessage(),what " + what);
        synchronized (mHandlerLock)
        {
            if (mLogicalHandler != null)
            {

                return Message.obtain(mLogicalHandler, what, arg1, arg2, obj);
            }
            else
            {

                return null;
            }
        }
    }

    public final Message obtainUiMessage(int what, int arg1, int arg2, Object obj)
    {

        // Log.d(TAG, "---------->obtainUiMessage(),what " + what);
        synchronized (mHandlerLock)
        {
            if (mUiHandler != null)
            {
                return Message.obtain(mUiHandler, what, arg1, arg2, obj);
            }
            else
            {
                return null;
            }
        }
    }

    public final void sendLogicalMessage(int what, int delayMillis)
    {

        // Log.d(TAG, "---------->sendLogicalMessage(),what " + what);
        synchronized (mHandlerLock)
        {
            if (mLogicalHandler != null)
            {
                mLogicalHandler.sendEmptyMessageDelayed(what, delayMillis);
            }
        }

    }

    public final void sendLogicalMessage(Message msg, int delayMillis)
    {

        // Log.d(TAG, "---------->sendLogicalMessage(),msg.what " + msg.what);
        if (msg != null)
        {
            synchronized (mHandlerLock)
            {
                if (mLogicalHandler != null)
                {
                    mLogicalHandler.sendMessageDelayed(msg, delayMillis);
                }
            }
        }

    }

    public final void sendUiMessage(int what, int delayMillis)
    {

        // Log.d(TAG, "---------->sendUiMessage(),what " + what);
        synchronized (mHandlerLock)
        {
            if (mUiHandler != null)
            {
                mUiHandler.sendEmptyMessageDelayed(what, delayMillis);
            }
        }

    }

    public final void sendUiMessage(Message msg, int delayMillis)
    {

        // Log.d(TAG, "---------->sendUiMessage(),msg.what " + msg.what);
        if (msg != null)
        {
            synchronized (mHandlerLock)
            {
                if (mUiHandler != null)
                {
                    mUiHandler.sendMessageDelayed(msg, delayMillis);
                }
            }
        }

    }

    public void removeLogicalMessage(int what)
    {

        // Log.d(TAG, "---------->removeLogicalMessage(),what " + what);
        synchronized (mHandlerLock)
        {
            if (mLogicalHandler != null)
            {
                mLogicalHandler.removeMessages(what);
            }
        }

    }

    public void removeUiMessage(int what)
    {

        // Log.d(TAG, "---------->removeUiMessage(),what " + what);
        synchronized (mHandlerLock)
        {
            if (mUiHandler != null)
            {
                mUiHandler.removeMessages(what);
            }
        }

    }

    public void stop(boolean bExit)
    {

        Log.d(TAG, "---------->stop(boolean bExit)");
        mMusicPlayer.resetPlayer();
        isPrepared = false;
        // requestRefreshPlayerState();
        setPlaying(false, false);
//        if (mRemoteCallback != null)
//        {
//
//            mRemoteCallback.onStop();
//        }
    }

    public void openFile(String filepath)
    {
		//Add by c00229449, remove last message to avoid exitting when time out after new music played
        removeUiMessage(AudioPlayerMsg.PUAH_MEDIAFILE_PLAY_COMPLETE);
        Log.d(TAG, "-------->openFile:" + filepath);
        mMusicPlayer.setDataSourceAsync(filepath);
    }

    private void play()
    {
        Log.d(TAG, "---------->play()");
        if (mMusicPlayer.isInitialized())
        {
            mMusicPlayer.start();
            setPlaying(true, true);
            // BroadCast the current LocalMediaInfo that is playing
//            broadCastCurtMediaInfo(mCurrentMediaInfo);
        }
    }

    public void pause()
    {
        Log.d(TAG, "---------->pause()");
        synchronized (this)
        {
            if (isPlaying())
            {
                mMusicPlayer.pause();
                /* BEGIN: Modified by c00224451 for 新增音乐播放，歌词滚动效果 2014/3/26 */
                mIsPauseByOkKey = true;
                /* END: Modified by c00224451 for 新增音乐播放，歌词滚动效果 2014/3/26 */
                setPlaying(false, false);
            }
        }

    }

    public void pauseNeedSwitchIcon()
    {
        Log.d(TAG, "---------->pause()");
        synchronized (this)
        {
            if (isPlaying())
            {
                mMusicPlayer.pause();
                setPlaying(false, true);
            }
        }
    }

    // add by s00203507 2012年8月17日 begin
    // 当音乐处于播放或暂停状态时，按上一首或下一首不需要切换播放、暂停图标，会先显示缓冲图标，缓冲完成显示暂停图标。
    // 只有当按播放、暂停键时，才需要切换播放、暂停图标
    public void setPlaying(boolean bPlaying, boolean isNeedSwitchIcon)
    {
        Log.d(TAG, "---------->setPlaying() bPlaying=" + bPlaying + ", isNeedSwitchIcon=" + isNeedSwitchIcon);

        if (mIsPlaying != bPlaying)
        {
            mIsPlaying = bPlaying;

            if (mIsPlaying)
            {
                removeLogicalMessage(AudioPlayerMsg.MSG_SYNC_POSTION);
                sendLogicalMessage(AudioPlayerMsg.MSG_SYNC_POSTION, 0);
            }
        }

        if (isNeedSwitchIcon)
        {
            // requestRefreshPlayerState();
        }

    }

    public void requestRefreshPlayerState()
    {

    }

    public boolean isPlaying()
    {

        Log.d(TAG, "---------->isPlaying()");
        if (mMusicPlayer.isInitialized() && mIsPlaying)
        {
            setPlaying(true, false);

            return true;
        }
        else
        {
            setPlaying(false, false);

            return false;
        }

    }

    private void requestRefreshMediaInfo()
    {
        mUiHandler.sendEmptyMessage(AudioPlayerMsg.MSG_REQUEST_REFRESH_MEDIAINFO);
    }

    /**
     * 请求同步歌词
     */
    private void requestSyncLyric()
    {
        Log.d(TAG, "---------->requestSyncLyric()");
        if (isLrcReady && isMusicPlayerReady())
        {
            removeUiMessage(AudioPlayerMsg.MSG_SYNC_LYRIC);
            sendUiMessage(AudioPlayerMsg.MSG_SYNC_LYRIC, 0);            
        }

    }

    /**
     * 请求播放播放列表中索引为index的媒体文件
     * 
     * @param index 媒体文件在播放列表中的索引值
     */
    private void requestPlay(int index)
    {
        Log.d(TAG, "requestPlay() IN");
        Log.d(TAG, "input-parameter index is : " + index);
        mAudioPlayStateInfo.setCurrentIndex(index);
        mPlayListView.setCurrentPlayIndex(index);
        mCurrentPlayIndex = index;

        Log.d(TAG, "PlayStateInfo.getInstance().getCurrentIndex is : " + mAudioPlayStateInfo.getCurrentIndex());

        LocalMediaInfo LocalMediaInfo = mAudioPlayStateInfo.getMediaInfo(index);

        Log.d(TAG, "mCurrentMediaInfo is " + mCurrentMediaInfo);
        Log.d(TAG, "LocalMediaInfo is " + LocalMediaInfo);
        //Log.d(TAG, "LocalMediaInfo url is : " + LocalMediaInfo.getUrl());
        if (LocalMediaInfo != null && LocalMediaInfo != mCurrentMediaInfo)
        {
            // Log.d("1111", "1111");
            resetSeekValue();
            mMusicListFocused = false;

            // 切换歌曲时清空之前的歌词
            clearLyric();

            // 每次切歌时隐藏杜比弹出框
            sendDoblyWinMsg(false);

            isLrcReady = false;

            // 将已播放进度、音乐总时长、进度值重置为0
            resetMusicProgressText();
            mSeekBar.setProgress(0);

            removeLogicalMessage(AudioPlayerMsg.MSG_REQUEST_PLAY);
            sendLogicalMessage(obtainLogicalMessage(AudioPlayerMsg.MSG_REQUEST_PLAY, 0, 0, LocalMediaInfo), 0);

        }
        else
        {
            // Log.d("1111", "2222");
            removeLogicalMessage(AudioPlayerMsg.MSG_REQUEST_PLAY);
        }
        Log.d(TAG, "requestPlay() OUT");
    }

    private OnPlayListSyncCompletedListener mOnPlayListSyncCompletedListener = new OnPlayListSyncCompletedListener()
    {

        @Override
        public void onPlayListSyncCompleted(boolean isNeedSetAdapter)
        {
            sendUiMessage(obtainUiMessage(AudioPlayerMsg.MSG_SET_PLAYLIST_ADAPTER, 0, 0, isNeedSetAdapter), 0);
        }
    };

    private OnMenuListener mOnMenuListener = new OnMenuListener()
    {

        @Override
        public void onOpen()
        {
            Log.d(TAG, "--------->onOpen() ");
            if (mCurrentMediaInfo == null)
            {
                // 第一次打开Menu，自动播放
                Log.d(TAG, "first open menu,auto play!!!");
                requestPlay(mAudioPlayStateInfo.getCurrentIndex());

            }
            else
            {
                Log.d(TAG, "mCurrentMediaInfo is not null, will replay!!!");

                // DTS2012091402762 【STC Android
                // B008】（概率性30%）推送后返回到音乐列表页面，播放某一音乐，右边音乐与实际播放的音乐不符。
                if (mAudioPlayStateInfo.isDMSType())
                {
                    requestPlay(mAudioPlayStateInfo.getCurrentIndex());
                }
                else
                {
                    requestPlay(0);
                }
            }

        }

        @Override
        public void onActiveItemChanged(int item)
        {
            Log.d(TAG, "--------->OnActiveItemChanged() item=" + item);
        }
    };

    private Handler.Callback mLogicalHandlerCallback = new Handler.Callback()
    {

        @Override
        public boolean handleMessage(Message msg)
        {

            switch (msg.what)
            {
                case AudioPlayerMsg.MSG_REQUEST_PLAY:
                {
                    Log.d(TAG, "--------proc message AudioPlayerMsg.MSG_REQUEST_PLAY");
                    if (msg.obj != null)
                    {

                        // DTS2012061804317 播放音乐时按上下键切换，概率性（50%）播放歌曲不是列表当前指向的歌曲
                        // begin
                        // 正常流程应该是先调用stop方法停止音乐播放器，再从消息中取出将要播放的音乐，但由于stop方法耗时较长，会导致stop前后的mCurrentMediaInfo不一致。
                        mCurrentMediaInfo = (LocalMediaInfo) msg.obj;

                        stop(true);

                        removeUiMessage(AudioPlayerMsg.MSG_SYNC_LYRIC);

                        // DTS2012061804317 播放音乐时按上下键切换，概率性（50%）播放歌曲不是列表当前指向的歌曲 end
                        String uri = mCurrentMediaInfo.getUrl();
                        
                        Log.i(TAG, "AudioPlayerMsg.MSG_REQUEST_PLAY->uri:" + uri);
                        if(uri == null)
                        	uri = mExtraUri.toString();
                        if (uri != null)
                        {
                            Lyric.getInstance().release();

                            isPrepared = false;

                            mBeginPrepareTime = System.currentTimeMillis();
                            if (mMusicPlayer != null)
                            {
                                mMusicPlayer.setMeidaInfo(mCurrentMediaInfo);
                            }
                            openFile(uri);

                            if (mBMCSMode)
                            {
                                artistFromRetriever = mCurrentMediaInfo.getmArtist();
                                titleFromRetriever = mCurrentMediaInfo.getmTitle();
                                albumFromRetriever = mCurrentMediaInfo.getmAlbum();
                                
                                removeLogicalMessage(AudioPlayerMsg.MSG_REQUEST_LYRIC);
                                sendLogicalMessage(obtainLogicalMessage(AudioPlayerMsg.MSG_REQUEST_LYRIC, 0, 0, mCurrentMediaInfo), 0);
                            }
                            titleFromRetriever = "";
                            artistFromRetriever = "";
                            albumFromRetriever = "";
                            RetrieveInfoManager.getInstance().addTask(mCurrentMediaInfo, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);
                            requestRefreshMediaInfo();
                            sendUiMessage(obtainUiMessage(AudioPlayerMsg.MSG_REFRESH_ALBUMICON, 0, 0,
                                                      mDefaultMusicBitmap), 0);
                        

                            //begin modify by caochao for DTS2014110900287 媒体中心推送音乐显示背景图片，然后推送下一首音乐信息不刷新仍为第一次推送的音乐信息
                            runOnUiThread(new Runnable()
                            {    
								@Override
								public void run() 
								{
									refreshAudioPreview(mAudioPlayStateInfo.getCurrentIndex());									
								}
                            }); 
                            //end modify by caochao for DTS2014110900287 媒体中心推送音乐显示背景图片，然后推送下一首音乐信息不刷新仍为第一次推送的音乐信息
                        }
                    }
                    else
                    {
                        // DTS2012061804317 播放音乐时按上下键切换，概率性（50%）播放歌曲不是列表当前指向的歌曲
                        // begin
                        // 正常流程应该是先调用stop方法停止音乐播放器，再从消息中取出将要播放的音乐，但由于stop方法耗时较长，会导致stop前后的mCurrentMediaInfo不一致。
                        mCurrentMediaInfo = null;

                        stop(true);
                        // DTS2012061804317 播放音乐时按上下键切换，概率性（50%）播放歌曲不是列表当前指向的歌曲 end
                    }

                    removeLogicalMessage(AudioPlayerMsg.MSG_SYNC_POSTION);
                    sendLogicalMessage(AudioPlayerMsg.MSG_SYNC_POSTION, 0);

                    break;
                }

                case AudioPlayerMsg.MSG_REQUEST_LYRIC:
                {
                    Log.d(TAG, "--------proc message AudioPlayerMsg.MSG_REQUEST_LYRIC");
                    if (msg.obj != null)
                    {
                        LocalMediaInfo hMediaInfo = (LocalMediaInfo) msg.obj;
                        new LyricThread(hMediaInfo).start();
                    }
                    break;
                }
                case AudioPlayerMsg.MSG_CONTROL_PLAY:
                    Log.d(TAG, "--------proc message AudioPlayerMsg.MSG_CONTROL_PLAY");
                    if (mMusicPlayer.isInitialized())
                    {
                        sendUiMessage(obtainUiMessage(AudioPlayerMsg.MSG_REFRESH_PLAYICON, 0, 0, PlayState.PLAY), 0);
                        play();
                    }
                    break;
                case AudioPlayerMsg.MSG_CONTROL_PAUSE:
                    Log.d(TAG, "--------proc message AudioPlayerMsg.MSG_CONTROL_PAUSE");
                    if (mMusicPlayer.isInitialized())
                    {
                        sendUiMessage(obtainUiMessage(AudioPlayerMsg.MSG_REFRESH_PLAYICON, 0, 0, PlayState.PAUSE), 0);
                        pause();
                    }
                    break;

                case AudioPlayerMsg.MSG_PROC_ERROR:
                {
                    Log.d(TAG, "--------proc message AudioPlayerMsg.MSG_PROC_ERROR");
                    int currentIndex = mAudioPlayStateInfo.getCurrentIndex();
                    int next = mAudioPlayStateInfo.getNextIndex();
                    mAudioPlayStateInfo.saveIsCanPlay(currentIndex, false);

                    // 返回-1则返回到播放列表界面，返回值的逻辑已封装在PlayStateInfo这里面，不需要再处理
                    if (-1 == next)
                    {
                        Message quiteMsg = Message.obtain();
                        quiteMsg.what = AudioPlayerMsg.MSG_REQUEST_EXIT;
                        quiteMsg.arg1 = 1;
                        sendUiMessage(quiteMsg, 0);
                    }
                    else
                    {
                        Log.e(TAG, "next Index = " + next);
                        sendUiMessage(obtainUiMessage(AudioPlayerMsg.MSG_RANDOM_SCROLL_PLAYLIST, next, 0, true), 0);
                    }
                    break;
                }
                case AudioPlayerMsg.MSG_PROC_COMPLETED:
                {                    
                    Log.d(TAG, "--------proc message AudioPlayerMsg.MSG_PROC_COMPLETED");
                    int currentIndex = mAudioPlayStateInfo.getCurrentIndex();
                    int next = mAudioPlayStateInfo.getNextIndex();
                    Log.d(TAG, "next is " + next);
                    if (next == -1)
                    {
                        sendUiMessage(AudioPlayerMsg.MSG_REQUEST_EXIT, 0);
                    }
                    else if (next == currentIndex)
                    {
                        if (mBMCSMode)
                        {
                            sendUiMessage(AudioPlayerMsg.PUAH_MEDIAFILE_PLAY_SEEK, 0);
     
                            break;
                        }
						mMusicPlayer.seekTo(0);
                        Log.d(TAG, "--------proc message AudioPlayerMsg.MSG_PROC_COMPLETED");
                        // 循环播放时只有一首歌，需要重新设置一下歌词
                        requestSyncLyric();
                        sendLogicalMessage(AudioPlayerMsg.MSG_CONTROL_PLAY, 0);
                    }
                    else
                    {
                        sendUiMessage(obtainUiMessage(AudioPlayerMsg.MSG_RANDOM_SCROLL_PLAYLIST, next, 0, true), 0);
                    }                                        
                    break;
                }
                case AudioPlayerMsg.MSG_SYNC_POSTION:
                    // Log.d(TAG,
                    // "---------->proc message AudioPlayerMsg.MSG_SYNC_POSTION()");
                    requestRefreshCurrentPosition();
                    int pos = mMusicPlayer.getCurrentPosition();
                    
                    removeLogicalMessage(AudioPlayerMsg.MSG_SYNC_POSTION);
                    sendLogicalMessage(AudioPlayerMsg.MSG_SYNC_POSTION, 300);

                    break;

                case MSG_REQUSET_REFRESH_CURRENT_POSITION:
                    requestRefreshCurrentPosition();
                    break;

                default:
                    Log.d(TAG, "---------->proc message default");

                    return false;
            }

            return true;
        }
    };


    public Bitmap loadDefaultMusicBitmap()
    {
        mDefaultMusicBitmap = BitmapUtil.createBitmapFromResource(getResources(), R.drawable.album_default_icon, THUMBNAIL_WIDTH,
                THUMBNAIL_HEIGHT);
        return mDefaultMusicBitmap;
    }


    /**
     * BroadCast the current LocalMediaInfo
     * 
     * @param currentMediaInfo
     */
//    public void broadCastCurtMediaInfo(LocalMediaInfo currentMediaInfo)
//    {
//        Intent intent = new Intent();
//        intent.setAction(ACTION_ON_AUDIO_PLAYED);
//
//        intent.putExtra("data", currentMediaInfo.getUrl());
//        intent.putExtra("thumbnail", currentMediaInfo.getmThumbNail());
//        intent.putExtra("displayname", currentMediaInfo.getmFileName());
//
//        getApplication().getApplicationContext().sendBroadcast(intent);
//
//        Log.d(TAG, "broadCastCurtMediaInfo-->" + currentMediaInfo.toString());
//    }

    /* BEGIN: Modified by c00224451 for 新增音乐播放，歌词滚动效果 2014/3/26 */
    private int mNextLineLyricDuration = 0;

    private long mCurrPauseTimeStamp = 0;

    private int prevTargetItem = -1;
    
    private static final int FORCUS_CURRENT_PLAY_DELAY_TIME = 10000;

    private Handler mUiHandler = new Handler(new Handler.Callback()
    {
        @Override
        public boolean handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case AudioPlayerMsg.MSG_SYNC_LYRIC:
                    Log.d(TAG, "proc msg MSG_SYNC_LYRIC IN...");
                    mUiHandler.removeMessages(AudioPlayerMsg.MSG_SYNC_LYRIC);
                    if (isMusicPlayerReady())
                    {
                        String[] currentLyricArray = null;
                        int currentPos = mMusicPlayer.getCurrentPosition();
                        currentLyricArray = Lyric.getInstance().getCurrentLyricArray(currentPos);
                        if (null == currentLyricArray)
                        {
                            return false;
                        }
                        int targetItem = Lyric.getInstance().getLineByTime(currentPos);
                        int lineDuration = Lyric.getInstance().getDuration(targetItem);
                        int lineStartTime = Lyric.getInstance().getStartTime(targetItem);
                        // currentPos = mMusicPlayer.getCurrentPosition();
                        Log.d(TAG, "cc msg currentPos " + currentPos + ", targetItem " + targetItem + " lineStartTime = " + lineStartTime
                                + " lineDuration = " + lineDuration);

                        // if (currentLyricArray != null)
                        // {
                        // for (int i = 0; i < currentLyricArray.length; i++)
                        // {
                        // if (i == 0)
                        // {
                        // Log.d(TAG, "leftLyric is " + currentLyricArray[i]);
                        // }
                        // else
                        // {
                        // Log.d(TAG, "rightLyric is " + currentLyricArray[i]);
                        // }
                        // }
                        // }
                        mNextLineLyricDuration = lineStartTime + lineDuration - currentPos + 100;
                        sendUiMessage(AudioPlayerMsg.MSG_SYNC_LYRIC, mNextLineLyricDuration);
                        mCurrPauseTimeStamp = System.currentTimeMillis();
                        if (prevTargetItem != targetItem)
                        {
                            colorLyric(currentLyricArray, targetItem, lineDuration);
                            prevTargetItem = targetItem;
                        }
                        Log.d(TAG, "cc msg currentLine = " + targetItem + " duration = " + lineDuration + " lyricArray[0] = " + currentLyricArray[0]
                                + " lyricArray[1] = " + currentLyricArray[1] + " mNextLineLyricDuration = " + mNextLineLyricDuration);

                    }

                    break;
                /* END: Modified by c00224451 for 新增音乐播放，歌词滚动效果 2014/3/26 */
                case AudioPlayerMsg.MSG_UPDATE_MUSIC_PROGRESS:
                    int duration =  mMusicPlayer.getDuration();
                    int curDur = msg.arg1;
                    if (curDur > duration)
                    {
                        curDur = duration;
                    }
                    else if (curDur < 0)
                    {
                        curDur = 0;
                    }
                    Log.d(TAG, "DateUtil.formatTime(curDur):"+curDur);
                    mMusicAlreadyPlayedDuration.setText(DateUtil.formatTime(curDur));
                    break;
                case AudioPlayerMsg.MSG_REQUEST_REFRESH_MEDIAINFO:
                    String name = "";
                    String albumArtist = "";
                    String albumName = "";
                    if (StringUtils.isNotEmpty(titleFromRetriever))
                    {
                        name = titleFromRetriever;
                    }
                    else
                    {
                        if (mCurrentMediaInfo != null)
                        {
                            name = mCurrentMediaInfo.getmFileName();
                        }
                    }

                    if (StringUtils.isNotEmpty(artistFromRetriever))
                    {
                        albumArtist = artistFromRetriever;
                    }
                    else
                    {
                        if (mCurrentMediaInfo != null)
                        {
                            albumArtist = StringUtils.isNotEmpty(mCurrentMediaInfo.getmArtist()) ? mCurrentMediaInfo.getmArtist()
                                    : getResources().getString(R.string.unknown_artist);
                        }
                    }

                    if (StringUtils.isNotEmpty(albumFromRetriever))
                    {
                        albumName = albumFromRetriever;
                    }
                    else
                    {
                        if (mCurrentMediaInfo != null)
                        {
                            albumName = StringUtils.isNotEmpty(mCurrentMediaInfo.getmAlbum()) ? mCurrentMediaInfo.getmAlbum() : getResources()
                                    .getString(R.string.unknown_album);
                        }
                    }
                    mAlbumInfoView.updateName(name);
                    mAlbumInfoView.updateOtherText(albumArtist + getString(R.string.newline) + albumName);
                    break;

                case AudioPlayerMsg.MSG_SCROLL_PLAYLIST:
                    // mPlayListView.processKeyDown(KeyEvent.KEYCODE_DPAD_DOWN);
                    break;

                case AudioPlayerMsg.MSG_RANDOM_SCROLL_PLAYLIST:
                    int index = msg.arg1;
                    boolean needPlay = (Boolean) msg.obj;
                    mPlayListView.processRandomPlay(index);
                    if (needPlay)
                    {
                        requestPlay(index);
                        mPlayListView.processMarquee();
                    }
                    else
                    {
                        if (index == mAudioPlayStateInfo.getCurrentIndex())
                        {
                            mMusicListFocused = false;
                        }
                        else
                        {
                            mMusicListFocused = true;
                        }
                        //begin modify by caochao for DTS2014110900287 媒体中心推送音乐显示背景图片，然后推送下一首音乐信息不刷新仍为第一次推送的音乐信息
                        refreshAudioPreview(index);
                        //end modify by caochao for DTS2014110900287 媒体中心推送音乐显示背景图片，然后推送下一首音乐信息不刷新仍为第一次推送的音乐信息
                        removeUiMessage(AudioPlayerMsg.MSG_REQUEST_PLAYLIST_RESTORE);
                        sendUiMessage(AudioPlayerMsg.MSG_REQUEST_PLAYLIST_RESTORE, FORCUS_CURRENT_PLAY_DELAY_TIME);
                    }
                    break;

                case AudioPlayerMsg.MSG_REFRESH_PLAYICON:
                    if (msg.obj != null)
                    {
                        refreshPlayIcon((PlayState) msg.obj);
                    }
                    break;

                case AudioPlayerMsg.MSG_REFRESH_ALBUMICON:
                    if (msg.obj != null)
                    {
                        Bitmap bitmap = (Bitmap) msg.obj;
                        mAlbumInfoView.setImage((Bitmap) msg.obj);
                    }
                    break;

                case AudioPlayerMsg.MSG_SET_PLAYLIST_ADAPTER:
                    List<String> strList = new ArrayList<String>();
                    List<LocalMediaInfo> mediaInfoList = mAudioPlayStateInfo.getMediaList();
                    for (LocalMediaInfo info : mediaInfoList)
                    {
                        strList.add(info.getmFileName());
                    }
                    mAdapter.setDataList(strList);
                    if ((Boolean) msg.obj)
                    {
                        mPlayListView.setAdapter(mAdapter, mAudioPlayStateInfo.getCurrentIndex());
                    }
                    break;

                case AudioPlayerMsg.MSG_ISSHOW_NO_LYRIC_TEXT:
                    // 没有歌词时，显示无歌词提示，隐藏歌词组件
                    break;

                case AudioPlayerMsg.MSG_REQUEST_PLAYLIST_RESTORE:
                    mPlayListView.processRandomPlay(mAudioPlayStateInfo.getCurrentIndex());
                    mMusicListFocused = false;

                    sendUiMessage(MSG_REQUEST_FOCUS_DISMISS, 1000);
                    break;

                case AudioPlayerMsg.MSG_UPDATE_MUSIC_TOTALDURATION:
                    requestRefreshTotalDuration();
                    break;
                
                case AudioPlayerMsg.PUAH_MEDIAFILE_PLAY_COMPLETE:
                case AudioPlayerMsg.PUAH_MEDIAFILE_PLAY_SEEK:
                    exitRemoteConnect();                    
                    break;
                    
                case MSG_REQUEST_FOCUS_DISMISS:
                    mGlobalFocus.setVisibility(View.INVISIBLE);
                    break;
                case MSG_REQUEST_PLAY_MUSIC:
                    requestPlay(0);
                    break;

                case MSG_REQUSET_EXIT_MUSICPLAYER:
                case MSG_REQUSET_EXIT_MUSICPLAYER_SEEK:
                    Log.d(TAG, "proc msg MSG_REQUSET_EXIT_MUSICPLAYER IN...");
                    if (!mBMCSMode)
                    {
                        releaseResource();
                        AudioPlayerActivity.this.finish();
    
                        Intent intent = new Intent();
                        intent.setClass(AudioPlayerActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);// 必须加上，开启MyMediaActivity时将会清除该进程空间的所有Activity。
                        startActivity(intent);
                    }

                    break;

                case MSG_REQUEST_PLAYMODESETTING_MENU_DISMISS:
                    mPlayModeSettingFocused = false;
                    mMusicPageFocused = true;
                    mPlayModeSetting.dismiss();
                    break;

                /** 添加杜比信息的弹窗处理 **/
                case MSG_DOBLY_SHOW:
                    doblyPopWin.showDoblyWin();
                    break;
                case MSG_DOBLY_HIDE:
                    doblyPopWin.hideDoblyWin();
                    break;

                case MSG_SHOW_BACKGRPUND_PICS:
                    boolean fromResume = false;
                    if (msg.obj != null)
                    {
                        fromResume = (Boolean) msg.obj;
                    }
                    if (mStIsShowBackgroundPic)
                        showBackgroundPics(fromResume);
                    break;

                case MSG_SETTING_LYRIC:
                    if (msg.arg1 == 0)
                    {
                        mNoLyricText.setVisibility(View.GONE);
                    }
                    else if (msg.arg1 == 1)
                    {
                        mNoLyricText.setVisibility(View.VISIBLE);
                        sendUiMessage(MSG_HIDE_LYRIC, 5000);
                    }
                    break;

                case MSG_HIDE_LYRIC:
                    mNoLyricText.setVisibility(View.GONE);
                    break;

                case AudioPlayerMsg.MSG_REQUEST_EXIT:
                    Log.d(TAG, "--------proc message AudioPlayerMsg.MSG_REQUEST_EXIT");
                    if (msg.arg1 == 1)
                    {
                        ToastUtil.showBySetDur(getBaseContext().getResources().getString(R.string.audio_cannot_paly), TOAST_SHOW_TIME);
                    }
                    requestExit();
                    break;
            }

            return true;
        }
    });

    private static final int MSG_REQUEST_FOCUS_DISMISS = 603;

    public Handler getUiHandler()
    {
        return mUiHandler;
    }

    public Handler getLogicalHandler()
    {
        synchronized (mHandlerLock)
        {
            return mLogicalHandler;
        }
    }

    private IVideoViewAdapter mMediaPlayer = null;

    private Object mPlayerLock = new Object();
    
    private class MusicPlayer
    {
        // private IMediaPlayerAdapter mMediaPlayer = null;

        private Object miObj = new Object();

        private LocalMediaInfo mi = null;

        private boolean mIsInitialized = false;

        public MusicPlayer()
        {

            Log.d(TAG, "---------->MusicPlayer()");

        }

        public void setMeidaInfo(LocalMediaInfo mi)
        {
            synchronized (miObj)
            {
                this.mi = mi;
            }
        }

        public void resetPlayer()
        {
            Log.d(TAG, "---------->resetPlayer()");
            release();
        }

        public void setDataSourceAsync(String path)
        {
            Log.d(TAG, "---------->setDataSourceAsync()");
            resetPlayer();
            synchronized (mPlayerLock)
            {
                try
                {
                    OrigVideoViewNoView tmp = new OrigVideoViewNoView(getApplicationContext());
                    mMediaPlayer = tmp;
                    mIsInitialized = false;
                    mMediaPlayer.setOnPreparedListener(preparedListener);

                    mMediaPlayer.setOnCompletionListener(mOnCompletionListener);
                    mMediaPlayer.setOnErrorListener(mOnErrorListener);
                    mMediaPlayer.setOnInfoListener(mOnInfoListener);
                    mMediaPlayer.setOnSeekCompleteListener(mOnSeekCompleteListener);
                    mMediaPlayer.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
                    //Log.i(TAG, "setDataSourceAsync->path:" + path);
                    if(mCurrentDevice.getDeviceType() == ConstData.DeviceType.DEVICE_TYPE_DMS
                    		|| mCurrentDevice.getDeviceType() == ConstData.DeviceType.DEVICE_TYPE_OTHER)
                    	mMediaPlayer.setVideoURI(Uri.parse(path));
                    else
                    	mMediaPlayer.setVideoURI(Uri.parse(Uri.encode(path)));

                }
                catch (IllegalArgumentException ex)
                {
                	
                    mMediaPlayer = null;

                    removeLogicalMessage(AudioPlayerMsg.MSG_PROC_ERROR);
                    sendLogicalMessage(AudioPlayerMsg.MSG_PROC_ERROR, 0);
                    return;

                }
                catch (RuntimeException ex)
                {
                	//Log.i(TAG, "setDataSourceAsync->IllegalArgumentException->ex:" + ex);
                    // retriever = null;
                    // artistFromRetriever = null;
                    // titleFromRetriever = null;
                    //
                    // if (null != mOnErrorListener) {
                    // mOnErrorListener.onError(mMediaPlayer,
                    // MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
                    // }

                }

            }

        }

        public boolean isInitialized()
        {
            synchronized (mPlayerLock)
            {
                return mIsInitialized;
            }
        }

        public void start()
        {
            Log.d(TAG, "---------->start()");
            synchronized (mPlayerLock)
            {
                if (mIsInitialized && mMediaPlayer != null)
                {
                    mMediaPlayer.start();
                    //requestRefreshMediaInfo();
                    // DTS2014011707941 播放之后在拉回。
                    if (mIsResumeNeedSeek)
                    {
                        Log.d(TAG, "after this MediaPlayer start.we're seek duration to onPause save the position.");
                        int totalDuration = mMediaPlayer.getDuration();
                        mSeekValue = (int) ((Float.valueOf(mResumeNeedSeekValue) / Float.valueOf(totalDuration)) * PROGRESS_MAXVALUE);
                        mSeekBar.setProgress(mSeekValue);
                        Log.d(TAG, "mSeekValue:"+mSeekValue);
                        Log.d(TAG, "mResumeNeedSeekValue:"+mResumeNeedSeekValue);
                        sendUiMessage(obtainUiMessage(AudioPlayerMsg.MSG_UPDATE_MUSIC_PROGRESS, mResumeNeedSeekValue, 0, null), 0);
                        SystemClock.sleep(100);
                        mMusicPlayer.seekTo(mResumeNeedSeekValue);
                        mIsResumeNeedSeek = false;
                        mResumeNeedSeekValue = 0;
                    }

                }
            }

        }

        public void stop()
        {
            synchronized (mPlayerLock)
            {
                Log.d(TAG, "---------->stop()");
                if (mIsInitialized)
                {
//                    if (mMediaPlayer != null)
//                    {
//                        Log.d(TAG, "---------->stopPlayback()");
//                        mMediaPlayer.stopPlayback();
//                    }
                    mIsInitialized = false;
                }

//                mAudioManager.abandonAudioFocus(mAudioFocusListener);
                Log.d(TAG, "---------->mAudioManager.abandonAudioFocus() end !");
                // 隐藏杜比弹出框
                // sendDoblyWinMsg(false);
            }
        }

        public void release()
        {
            Log.d(TAG, "---------->release()");
//            synchronized (mPlayerLock)
//            {
//                // 清掉回调监听
//                if (mMediaPlayer != null)
//                {
//                    mMediaPlayer.setOnBufferingUpdateListener(null);
//                    mMediaPlayer.setOnCompletionListener(null);
//                    mMediaPlayer.setOnErrorListener(null);
//                     mMediaPlayer.setOnInfoListener(null);
//                     mMediaPlayer.setOnPreparedListener(null);
//                    mMediaPlayer.setOnSeekCompleteListener(null);
//                    // mMediaPlayer.setOnVideoSizeChangedListener(null);
//                }
//            }

            stop();

            synchronized (mPlayerLock)
            {
                if (mMediaPlayer != null)
                {
                    // FIXME: 快速切换歌曲时，无法退出
                    Log.d(TAG, "--------> MediaPlayer.release() start");
                    mMediaPlayer.stopPlayback();
                    mMediaPlayer = null;
                    Log.d(TAG, "--------> MediaPlayer.release() end: ok");

                }
            }

        }

        public void pause()
        {
            Log.d(TAG, "---------->pause()");
            synchronized (mPlayerLock)
            {
                if (mIsInitialized && mMediaPlayer != null)
                {
                    mMediaPlayer.pause();
                }
            }

        }

        public int getDuration()
        {
            // Log.d(TAG, "---------->getDuration()");
            synchronized (mPlayerLock)
            {
                if (mIsInitialized && mMediaPlayer != null)
                {

                    return mMediaPlayer.getDuration();
                }
                else
                {
                    return -1;
                }
            }
        }

        public int getCurrentPosition()
        {

            // Log.d(TAG, "---------->getCurrentPosition()");
            synchronized (mPlayerLock)
            {
                if (mIsInitialized && mMediaPlayer != null)
                {

                    return mMediaPlayer.getCurrentPosition();
                }
                else
                {

                    return -1;
                }
            }
        }

        public int seekTo(int whereto)
        {
            mMCSSeekTarget = whereto;
            Log.d(TAG, "---------->seekTo(): " + whereto);
            synchronized (mPlayerLock)
            {
                if (mIsInitialized && mMediaPlayer != null)
                {
                    // add by wanghuanlai for DLNA audio player cannot auto exit.wait 12s for seekComplete.
                    //removeUiMessage(MSG_REQUSET_EXIT_MUSICPLAYER_SEEK);
                    //sendUiMessage(MSG_REQUSET_EXIT_MUSICPLAYER_SEEK, TWO_MUSIC_INTERVAL);
					removeUiMessage(AudioPlayerMsg.PUAH_MEDIAFILE_PLAY_SEEK);
                    sendUiMessage(AudioPlayerMsg.PUAH_MEDIAFILE_PLAY_SEEK,TWO_MUSIC_INTERVAL);
                    mMediaPlayer.seekTo((int) whereto);
                }                
            }

            return whereto;
        }

        private OnPreparedListener preparedListener = new OnPreparedListener()
        {

            @Override
            public void onPrepared(IMediaPlayerAdapter mp)
            {
                // tss add
                setVolumeControlStream(AudioManager.STREAM_MUSIC);

//                int ret = mAudioManager.requestAudioFocus(mAudioFocusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);

                // Log.d(TAG, "---------->onPrepared()");

                Log.d(TAG, "requestAudioFocus ret:" + AudioManager.AUDIOFOCUS_REQUEST_GRANTED
                        + " 1 is AudioManager.AUDIOFOCUS_REQUEST_GRANTED else failed");

                mIsInitialized = true;
                if (isMCSMode() && mMCSSeekTarget != 0 && mMediaPlayer != null)
                {
                    mMediaPlayer.seekTo(mMCSSeekTarget);
                }
                mOnPreparedListener.onPrepared(mp);                
//                broadCastCurtMediaInfo(mCurrentMediaInfo);
            }
        };
    }

    private OnBufferingUpdateListener mOnBufferingUpdateListener = new OnBufferingUpdateListener()
    {

        @Override
        public boolean onBufferingUpdate(IMediaPlayerAdapter mp, int percent)
        {

//            Log.d(TAG, "--------->onBufferingUpdate() percent=" + percent);
            return true;

        }
    };

    /**
     * 音乐播放完成监听器
     */
    private OnCompleteListener mOnCompletionListener = new OnCompleteListener()
    {

        @Override
        public void onCompletion(IMediaPlayerAdapter mp)
        {

            Log.d(TAG, "--------->MediaPlayer.OnCompletionListener:  onCompletion()");
            setPlaying(false, false);
            removeLogicalMessage(AudioPlayerMsg.MSG_SYNC_POSTION);

//            sendUiMessage(AudioPlayerMsg.PUAH_MEDIAFILE_PLAY_COMPLETE,TWO_MUSIC_INTERVAL);
            sendLogicalMessage(AudioPlayerMsg.MSG_PROC_COMPLETED, 0);
        }
    };

    /**
     * 音乐播放出错监听器
     */
    private OnErrorListener mOnErrorListener = new OnErrorListener()
    {

        @Override
        public boolean onError(IMediaPlayerAdapter mp, int what, int extra)
        {

            Log.d(TAG, "--------->MediaPlayer.OnErrorListener:  onError(): what=" + what + ", extra=" + extra);

            sendUiMessage(AudioPlayerMsg.MSG_UPDATE_MUSIC_TOTALDURATION, 0);
            setPlaying(false, false);

            switch (what)
            {
                case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                    Log.d(TAG, "---------->media server died..........");
					mMusicPlayer.resetPlayer();
                    sendUiMessage(MSG_REQUSET_EXIT_MUSICPLAYER, 0);
                    return true;
                case -38:
                {
                    // FIXME:
                    return true;
                }
                case -2147483648:
                {
                    return true;
                }
                default:
                    Log.d(TAG, "Unkown Error: " + what + "," + extra);
                    break;
            }

            // 每次切歌时隐藏杜比弹出框
            sendDoblyWinMsg(false);

            removeLogicalMessage(AudioPlayerMsg.MSG_PROC_ERROR);
            sendLogicalMessage(AudioPlayerMsg.MSG_PROC_ERROR, 0);
            return true;
        }
    };

    /**
     * 网络错误码，供mOnInfoListener使用 copy from bigfish/hidolphin/component/player/include/hi_svr_format.h
     * @author w00227386
     */
    public enum HI_FORMAT_MSG_NETWORK_E
    {
        HI_FORMAT_MSG_NETWORK_ERROR_UNKNOW, HI_FORMAT_MSG_NETWORK_ERROR_CONNECT_FAILED, HI_FORMAT_MSG_NETWORK_ERROR_TIMEOUT, HI_FORMAT_MSG_NETWORK_ERROR_DISCONNECT, HI_FORMAT_MSG_NETWORK_ERROR_NOT_FOUND, HI_FORMAT_MSG_NETWORK_NORMAL, HI_FORMAT_MSG_NETWORK_ERROR_BUTT,
    }

    /**
     * modify by wanghuanlai 音乐播放信息码
     */
    private OnInfoListener mOnInfoListener = new OnInfoListener()
    {
        @Override
        public boolean onInfo(IMediaPlayerAdapter mp, int what, int extra)
        {
            return false;
        }
    };

    /**
     * 检测音频是否支持杜比 By ouxiaoyong
     * 
     */
    private void checkAudioInfo(IMediaPlayerAdapter mp)
    {
        if (mp == null)
        {
            return;
        }
        List<AudioInfoOfVideo> listAudioInfo = mp.getAudioInfos();

        if (listAudioInfo == null)
        {
            Log.d(TAG, "listAudioInfo == null");
            return;
        }

        // 音频个数
        int audioSum = listAudioInfo.size();

        if (audioSum == 0)
        {
            Log.d(TAG, "get audio audioSum is 0");
            return;
        }
        AudioInfoOfVideo audioInfo;
        // 音频信息
        for (int i = 0; i < audioSum; i++)
        {
            // audioFormat
            audioInfo = listAudioInfo.get(i);
            if (audioInfo != null)
            {
                Log.d(TAG, "audioformat = " + audioInfo.getaudioformat());
                doblyPopWin.checkHasDobly(audioInfo.getaudioformat());
            }
        }
    }

    /**
     * 音乐准备完成监听器
     */
    private OnPreparedListener mOnPreparedListener = new OnPreparedListener()
    {

        @Override
        public void onPrepared(IMediaPlayerAdapter mp)
        {
            mEndPrepareTime = System.currentTimeMillis();
            Log.d(TAG, "beginTime is " + mBeginPrepareTime);
            Log.d(TAG, "endTime is " + mEndPrepareTime);
            Log.d(TAG, "cost is " + (mEndPrepareTime - mBeginPrepareTime));
            Log.d(TAG, "---------->onPrepared()");
            if (!mBExitPage)
            {
                isPrepared = true;
                int delayTime = (mEndPrepareTime - mBeginPrepareTime > 1000) ? 0 : (int) (1000 - (mEndPrepareTime - mBeginPrepareTime));
                sendLogicalMessage(AudioPlayerMsg.MSG_CONTROL_PLAY, delayTime);
                
                sendUiMessage(AudioPlayerMsg.MSG_UPDATE_MUSIC_TOTALDURATION, 0);

                requestSyncLyric();

                // 弹出杜比信息框
                checkAudioInfo(mp);
                sendDoblyWinMsg(true);
            }
            else
            {
                Log.d(TAG, "---------->has exited page, will not play song!!!");
            }
        }
    };

    /**
     * seek完成监听器
     */
    private OnSeekCompleteListener mOnSeekCompleteListener = new OnSeekCompleteListener()
    {

        @Override
        public void onSeekComplete(IMediaPlayerAdapter mp)
        {
            Log.d(TAG, "onSeekComplete() IN...");
            int currentPosition = mMusicPlayer.getCurrentPosition();
            Log.d(TAG, "onSeekComplete() IN..."+currentPosition);
            requestSyncLyric();
            resetSeekValue();

            removeUiMessage(AudioPlayerMsg.PUAH_MEDIAFILE_PLAY_SEEK);
            removeUiMessage(MSG_REQUSET_EXIT_MUSICPLAYER_SEEK);         
            removeLogicalMessage(AudioPlayerMsg.MSG_SYNC_POSTION);
            sendLogicalMessage(AudioPlayerMsg.MSG_SYNC_POSTION, 500);
        }
    };

    private String titleFromRetriever = null;

    private String artistFromRetriever = null;

    private String albumFromRetriever = null;
    
    /**
     * 刷新歌曲当前播放位置
     */
    public void requestRefreshCurrentPosition()
    {
        // Log.d(TAG, "requestRefreshCurrentPosition() IN...");
        if (mMusicPlayer.isInitialized())
        {

            int totalDuration = mMusicPlayer.getDuration();
            int seekvalue = -1;
            
            synchronized (mSeekLock)
            {
                seekvalue = realSeekValue;
            }

            if (seekvalue != -1)
            {
                mSeekValue = (int) ((Float.valueOf(seekvalue) / Float.valueOf(totalDuration)) * PROGRESS_MAXVALUE);
                mSeekBar.setProgress(mSeekValue);
                Log.d(TAG, " requestRefreshCurrentPosition() " + seekvalue);
                sendUiMessage(obtainUiMessage(AudioPlayerMsg.MSG_UPDATE_MUSIC_PROGRESS, seekvalue, 0, null), 0);
            }
            else
            {
                int currentPosition = mMusicPlayer.getCurrentPosition();
                // Log.d(TAG, "currentPosition " + currentPosition);
                mSeekValue = (int) ((Float.valueOf(currentPosition) / Float.valueOf(totalDuration)) * PROGRESS_MAXVALUE);
                if (0 == mResumeNeedSeekValue)
                {
                    mSeekBar.setProgress(mSeekValue);
                    Log.d(TAG, " requestRefreshCurrentPosition()+ currentPosition" + currentPosition);
                    sendUiMessage(obtainUiMessage(AudioPlayerMsg.MSG_UPDATE_MUSIC_PROGRESS, currentPosition, 0, null), 0);
                }
            }

        }
    }

    /**
     * 根据播放状态切换图标
     * 
     * @param state 播放状态
     */
    public void refreshPlayIcon(PlayState state)
    {
        mPlayListView.switchPlayControlIcon(state);
    }

    public void showPlayModeSetting()
    {
        mPlayModeSettingFocused = true;
        mPlayModeSetting.showAtLocation(mParentLinear, Gravity.CENTER_HORIZONTAL, 30, 20);
        sendPlayModeMenuDismissMsg(AudioPlayerMsg.POPUPWINDOW_DISMISS_DELAYMILLIS);

    }

    private void showPlayMode(int playMode)
    {
        switch (playMode)
        {
            case ConstData.MediaPlayMode.MP_MODE_ALL:
                mPlayModeText.setText(R.string.play_mode_sequence_play);
                mPlayModeIcon.setImageResource(R.drawable.playmode_icon_sequential);
                break;
            case ConstData.MediaPlayMode.MP_MODE_SINGLE:
                mPlayModeText.setText(R.string.play_mode_single_play);
                mPlayModeIcon.setImageResource(R.drawable.playmode_icon_single_play);
                break;
            case ConstData.MediaPlayMode.MP_MODE_RONDOM:
                mPlayModeText.setText(R.string.play_mode_random_play);
                mPlayModeIcon.setImageResource(R.drawable.playmode_icon_random);
                break;
            case ConstData.MediaPlayMode.MP_MODE_ALL_CYC:
                mPlayModeText.setText(R.string.play_mode_loop_play);
                mPlayModeIcon.setImageResource(R.drawable.playmode_icon_loop);
                break;
        }
    }

    public void switchPlayMode()
    {
        // Log.d("ooo", "mWheelView.getCurrentItem() " + mWheelView.getCurrentItem());
        switch (mWheelView.getCurrentItem())
        {
            case 0:
                mPlayModeText.setText(R.string.play_mode_random_play);
                mPlayModeIcon.setImageResource(R.drawable.playmode_icon_random);
                AudioPlayStateInfo.setPlayMode(ConstData.MediaPlayMode.MP_MODE_RONDOM);
                sendPlayModeMenuDismissMsg(0);
                break;
            case 1:
                mPlayModeText.setText(R.string.play_mode_loop_play);
                mPlayModeIcon.setImageResource(R.drawable.playmode_icon_loop);
                AudioPlayStateInfo.setPlayMode(ConstData.MediaPlayMode.MP_MODE_ALL_CYC);
                sendPlayModeMenuDismissMsg(0);
                break;
            case 2:
                mPlayModeText.setText(R.string.play_mode_sequence_play);
                mPlayModeIcon.setImageResource(R.drawable.playmode_icon_sequential);
                AudioPlayStateInfo.setPlayMode(ConstData.MediaPlayMode.MP_MODE_ALL);
                sendPlayModeMenuDismissMsg(0);
                break;
            case 3:
                mPlayModeText.setText(R.string.play_mode_single_play);
                mPlayModeIcon.setImageResource(R.drawable.playmode_icon_single_play);
                AudioPlayStateInfo.setPlayMode(ConstData.MediaPlayMode.MP_MODE_SINGLE);
                sendPlayModeMenuDismissMsg(0);
                break;
        }

    }

    /**
     * 判断音乐播放器是否准备完成
     * 
     * @return
     */
    public boolean isMusicPlayerReady()
    {
        if (mMusicPlayer != null && mMusicPlayer.isInitialized())
        {
            return true;
        }
        return false;
    }

    public void requestRefreshTotalDuration()
    {
        Log.d(TAG, "requestRefreshTotalDuration() IN...");
        Log.d(TAG, "duration " + mMusicPlayer.getDuration());
        mMusicTotalDuration.setText(DateUtil.formatTime(mMusicPlayer.getDuration()));
        Log.d(TAG, "requestRefreshTotalDuration() OUT...");
    }

    public void colorLyric(String[] lyricArray, int currentLine, int duration)
    {
        if (lyricArray == null || lyricArray.length == 0)
        {
            // mNoLyric.setVisibility(View.VISIBLE);
            return;
        }
        mChangeLyricColorHandler.removeMessages(0);
        // mNoLyric.setVisibility(View.INVISIBLE);
        if (currentLine % 2 == 0)
        {
            // mRightLyric.setText(lyricArray[1]);
            // mRightLyric.setTextColor(getResources().getColor(R.color.lyricFocused));
            mRightLyric.setText(lyricArray[1]);
            mRightLyric.setTextColor(Color.WHITE);
            mCurrFocusLyric = mLeftLyric;
            mCurrPlayLyric = lyricArray[0];
        }
        else
        {
            // mLeftLyric.setText(lyricArray[0]);
            // mLeftLyric.setTextColor(getResources().getColor(R.color.lyricFocused));
            mLeftLyric.setText(lyricArray[0]);
            mLeftLyric.setTextColor(Color.WHITE);
            mCurrFocusLyric = mRightLyric;
            mCurrPlayLyric = lyricArray[1];
        }

        if (null != mCurrPlayLyric && 0 != mCurrPlayLyric.length())
        {
            mCounter = 1;
            lineDuration = duration;
            computeLyricCount(mCurrPlayLyric.length(), duration);
            mChangeLyricColorHandler.sendEmptyMessage(0);
        }
    }

    private static final int DEFAULT_COUNT = 20;

    private int notifyRefreshDuration = 1;

    private int lineDuration = 1;

    private int mCounter = 1;

    private int mMessageCount = 1;

    private void computeLyricCount(int strLength, int duration)
    {
        mMessageCount = strLength >= DEFAULT_COUNT ? strLength : DEFAULT_COUNT;
        notifyRefreshDuration = duration / mMessageCount;
    }

    private Handler mChangeLyricColorHandler = new Handler()
    {

        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            removeMessages(0);
            if (null != mCurrFocusLyric && null != mCurrPlayLyric)
            {
                int length = mCurrPlayLyric.length();
                int end = (int) Math.round(mCounter++ * 1.0 * notifyRefreshDuration * length / lineDuration);
                if (end > length)
                {
                    end = length;
                }
                SpannableStringBuilder spannable = highlight(mCurrPlayLyric, end);
                mCurrFocusLyric.setText(spannable);
                mChangeLyricColorHandler.sendEmptyMessageDelayed(0, notifyRefreshDuration);
            }
        }

    };

    private SpannableStringBuilder highlight(String showText, int end)
    {

        return highlight(showText, 0, end);
    }

    private SpannableStringBuilder highlight(String showText, int start, int end)
    {
        SpannableStringBuilder spannable = new SpannableStringBuilder(showText);// 用于可变字符串
        ForegroundColorSpan span = new ForegroundColorSpan(Color.parseColor("#49F3FE"));
        spannable.setSpan(span, start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    public void clearLyric()
    {
        mLeftLyric.setText(" ");
        mRightLyric.setText(" ");
        mCurrFocusLyric = null;
        mCurrPlayLyric = null;
        mChangeLyricColorHandler.removeMessages(0);
    }

    /* END: Modified by c00224451 for 新增音乐播放，歌词滚动效果 2014/3/26 */
    public void resetMusicProgressText()
    {
        mMusicAlreadyPlayedDuration.setText(DateUtil.formatTime(0));
        mMusicTotalDuration.setText(DateUtil.formatTime(0));
    }

    public void resetSeekValue()
    {
        synchronized (mSeekLock)
        {
            mbSeekKeyUp = false;
            mbSeeking = false;
            realSeekValue = -1;
        }
    }

    @Override
    public void onClick(View v)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onChanged(WheelView arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub

    }

    public void releaseResource()
    {
        Log.d(TAG, "releaseResource() IN...");     
        mMusicPlayer.release();

        removeLogicalMessage(AudioPlayerMsg.MSG_SYNC_POSTION);
        removeUiMessage(AudioPlayerMsg.MSG_UPDATE_MUSIC_PROGRESS);

        synchronized (mHandlerLock)
        {

            if (mLogicalHandler != null)
            {
//                synchronized (mmLogicalHandlerLock)
//                {
                    mLogicalHandler.getLooper().quit();

                    mLogicalHandlerCallback = null;
                    mLogicalHandler = null;
                    mLogicalThread = null;
//                }
            }
        }
		
		// add by s00203507 2012年8月1日 begin
        // 播放推送的音乐过程中按back键退出时，需要向mediacenterservice发送stop消息，将当前播放媒体文件置空；
        // 如果不调用此接口，连续进行搜索-推送-搜索-推送操作时会导致无法发送退出音乐播放器广播，造成IFXF_Event_Handler
        // failed with errorCode=-1异常。
    }

    private OnItemTouchListener mOnItemTouchListener = new OnItemTouchListener()
    {

        @Override
        public void onItemTouch(View arg0)
        {
            Log.d(TAG, "onItemTouch() IN...");
            removeUiMessage(AudioPlayerMsg.MSG_RANDOM_SCROLL_PLAYLIST);
            sendUiMessage(obtainUiMessage(AudioPlayerMsg.MSG_RANDOM_SCROLL_PLAYLIST, mPlayListView.getCurrentItem(), 0, true), 0);

        }
    };

    /**
     * 将当前播放索引传回音乐浏览界面
     * @see [类、类#方法、类#成员]
     */
    public void passIntentForAudioBrowser()
    {
        Log.d(TAG, "passIntentForAudioBrowser() IN...");
        Bundle bundle = new Bundle();
        bundle.putInt("playIndex", mCurrentPlayIndex);
        bundle.putInt("mediaType", ConstData.MediaType.AUDIO);

        Intent intent = new Intent();
        intent.putExtras(bundle);

        setResult(RESULT_OK, intent);
    }

    private OnSeekBarChangeListener mOnSeekBarChangeListener = new OnSeekBarChangeListener()
    {

        @Override
        public void onStopTrackingTouch(SeekBar seekBar)
        {
            Log.d(TAG, "onStopTrackingTouch() IN..., " + seekBar.getProgress());
            if (isMusicPlayerReady())
            {
                int whereto = (seekBar.getProgress() * mMusicPlayer.getDuration()) / PROGRESS_MAXVALUE;
                mMusicPlayer.seekTo(whereto);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar)
        {
            Log.d(TAG, "onStartTrackingTouch() IN...");
            if (isMusicPlayerReady())
            {
                removeLogicalMessage(AudioPlayerMsg.MSG_SYNC_POSTION);
            }
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
        {
            // TODO Auto-generated method stub

        }
    };

    /**
     * 处理杜比信息的弹出框 isShow = true：弹出 isShow = false：隐藏
     * 
     * @param isShow
     * @see [类、类#方法、类#成员]
     */
    private void sendDoblyWinMsg(boolean isShow)
    {
        if (isShow)
        {
            // 显示Dobly的信息视窗
            mUiHandler.sendEmptyMessage(MSG_DOBLY_SHOW);
        }
        else
        {
            // 隐藏Dobly的信息视窗
            mUiHandler.sendEmptyMessage(MSG_DOBLY_HIDE);
        }
    }

    // public boolean onCreateOptionsMenu(Menu menu) {
    // //Log.i("wanghuanlai","onCreateOptionsMenu ...");
    // if (mPopMenu == null) {
    // mPopMenu = new MyMediaPopMenu_v2(this);
    // // 注意： 必须创建一项
    // // 由于我们是截获了系统的Menu，所以一定要创建一个，否则系统以为没有菜单，就不会调用onMenuOpened函数
    // mPopMenu.setOnSelectDisplayTypeListener(this);
    //
    // // 重现加载menu项
    // mPopMenu.clear();
    // loadMenu();
    // }
    // // 注意： 必须创建一项
    // // 由于我们是截获了系统的Menu，所以一定要创建一个，否则系统以为没有菜单，就不会调用onMenuOpened函数
    // return false;
    // //menu.add("menu");
    // //return super.onCreateOptionsMenu(menu);
    // }

    // zkf61715
    public void setPicActivityShow(boolean misPicActivityShow)
    {
        mIsPicActivityShow = misPicActivityShow;
    }

    protected void loadMenu(boolean isPlayMode)
    {
        MenuCategory menuCgy = null;
        ArrayList<MenuItemImpl> itemImpls = null;
        MenuItemImpl item = null;
        mPopMenu.clearCategories();
        /**
         * 切换方式
         */
        menuCgy = new MenuCategory();
        menuCgy.setCategoryName(getResources().getString(R.string.play_mode_audio));
        itemImpls = new ArrayList<MenuItemImpl>();
        // 随机播放
        item = new MenuItemImpl(this, 1, ENUMLAYOUTDISPLAYTYPE.ENUM_RANDOM_PLAY, R.drawable.menu_btn_item_icon, 1, 1, getResources().getString(
                R.string.play_mode_random_play));
        itemImpls.add(item);

        // 循环播放
        item = new MenuItemImpl(this, 1, ENUMLAYOUTDISPLAYTYPE.ENUM_LOOP_PLAY, R.drawable.menu_btn_item_icon, 1, 1, getResources().getString(
                R.string.play_mode_loop_play));
        itemImpls.add(item);

        // 顺序播放
        item = new MenuItemImpl(this, 1, ENUMLAYOUTDISPLAYTYPE.ENUM_SEQUENTIAL_PLAY, R.drawable.menu_btn_item_icon, 1, 1, getResources().getString(
                R.string.play_mode_sequence_play));
        itemImpls.add(item);

        // 单曲循环
        item = new MenuItemImpl(this, 1, ENUMLAYOUTDISPLAYTYPE.ENUM_SINGLE_PLAY, R.drawable.menu_btn_item_icon, 1, 1, getResources().getString(
                R.string.play_mode_single_play));
        itemImpls.add(item);
        menuCgy.setMenuItems(itemImpls);
        menuCgy.setSelectIndex(getMenuIndex(AudioPlayStateInfo.getPlayMode()));
        if (isPlayMode)
        	mPopMenu.addMenuCategory(menuCgy);
        // 要播放背景图片时，请打开下面注释的代码
        /**
         * 播放背景
         */
/*        menuCgy = new MenuCategory();
        menuCgy.setCategoryName(getResources().getString(R.string.background_pics));
        itemImpls = new ArrayList<MenuItemImpl>();
        // 关闭播放背景
        item = new MenuItemImpl(this, 1, ENUMLAYOUTDISPLAYTYPE.ENUM_CLOSE_BACKGROUND_PIC, R.drawable.menu_btn_item_icon, 1, 1, getResources()
                .getString(R.string.close));
        itemImpls.add(item);
        // 打开播放背景
        item = new MenuItemImpl(this, 1, ENUMLAYOUTDISPLAYTYPE.ENUM_OPEN_BACKGROUND_PIC, R.drawable.menu_btn_item_icon, 1, 1, getResources()
                .getString(R.string.open_album));
        itemImpls.add(item);
        menuCgy.setMenuItems(itemImpls);
        menuCgy.setSelectIndex(mStIsShowBackgroundPic ? 1 : 0);
        if (!isPlayMode)
            mPopMenu.addMenuCategory(menuCgy);
*/
        openMenu();
    }

    // zkf61715
    private int getMenuIndex(int playmode)
    {
        switch (playmode)
        {
            case ConstData.MediaPlayMode.MP_MODE_RONDOM:
                return 0;
            case ConstData.MediaPlayMode.MP_MODE_ALL_CYC:
                return 1;
            case ConstData.MediaPlayMode.MP_MODE_ALL:
                return 2;
            case ConstData.MediaPlayMode.MP_MODE_SINGLE:
                return 3;

            default:
                return 0;
        }
    }

    public boolean openMenu()
    {
        if (mPopMenu != null)
        {
            if (mPopMenu.isShowing())
            {
                mPopMenu.hide();
            }
            else
            {
                //mPopMenu.replayLastSelected();
            	if(mPopMenu.isCreated())
            		mPopMenu.rebuildView();
                mPopMenu.show();
            }
        }
        // 返回为true 则显示系统menu
        return false;
    }

    public boolean createOptionsMenu(boolean isPlayMode)
    {
        if (mPopMenu == null)
        {
            mPopMenu = new AudioSettingsDialog(this);
            // 注意： 必须创建一项
            // 由于我们是截获了系统的Menu，所以一定要创建一个，否则系统以为没有菜单，就不会调用onMenuOpened函数
            mPopMenu.setOnSelectTypeListener(this);
            // mPopMenu.setSwitcherListenerInterface(this);
            // 重现加载menu项
        }
        //mPopMenu.clear();
        loadMenu(isPlayMode);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        //Log.i(TAG, "onActivityResult ");
        // TODO Auto-generated method stub
        mIsPicActivityShow = false;
        if (data != null)
            mShowDefaultBg = data.getBooleanExtra("hasNoContent", false);
        setImageList();
        showBackgroundPics(true);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setImageList()
    {
    }

    private String getDeviceId()
    {
        /** 解析intent获取设备id 用于获取数据 */
        Bundle bundle = getIntent().getBundleExtra(LocalDeviceInfo.DEVICE_EXTRA_NAME);
        Log.d(TAG, "parseIntent---bundle==" + bundle);
        String deviceId = null;
        if (bundle != null)
        {
            int deviceType = bundle.getInt(LocalDeviceInfo.DEVICE_TYPE);
            if (ConstData.DeviceType.isDLNADevice(deviceType))
            { // DMS设备类型
                deviceId = bundle.getString(LocalDeviceInfo.PHYSIC_ID);
            }
            else if (ConstData.DeviceType.isExternalStorage(deviceType))
            { // U盘设备类型 SD盘设备类型,返回的类型为string
                deviceId = bundle.getString(LocalDeviceInfo.MOUNT_PATH);
            }
        }
        return deviceId;
    }

    private int getmDeviceType()
    {
        Bundle bundle = getIntent().getBundleExtra(LocalDeviceInfo.DEVICE_EXTRA_NAME);
        if (bundle == null)
        {
            return ConstData.DeviceType.DEVICE_TYPE_UNKNOWN;
        }
        return bundle.getInt("devices_type");
    }

    private void createBottomPopMenu()
    {
        if (mBottomPopMenu == null)
        {
            mBottomPopMenu = new BottomPopMenu(this);
        }
        mBottomPopMenu.setOnSelectTypeListener(this);

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
            reloadBottomMenu();

            if (id != -1)
            {
                // 恢复聚焦到上次的那个ID上
                mBottomPopMenu.setCurrentMenuItem(id);
            }

            mBottomPopMenu.show(mParentLinear);
        }
    }

    private void reloadBottomMenu()
    {
        /* BEGIN: Modified by c00224451 for DTS2014031902972 2014/3/19 */
        if (mBottomPopMenu != null)
        {
            mBottomPopMenu.add(1, ENUMLAYOUTDISPLAYTYPE.ENUM_AUDIO_PLAY_MODE, R.drawable.menu_icon_audio_play_mode, 1, 1,
                    getResources().getString(R.string.play_mode_audio));
            //mBottomPopMenu.add(2, ENUMLAYOUTDISPLAYTYPE.ENUM_AUDIO_PLAY_BACKGROUND, R.drawable.menu_icon_background_pic, 2, 2, getResources()
             //       .getString(R.string.background_pics));
        }
        /* END: Modified by c00224451 for DTS2014031902972 2014/3/19 */
    }

    private ListSelectPopup mListSelectPopup;

    private void showImageSelectDialog()
    {
        if (mListSelectPopup == null)
        {
            mListSelectPopup = new ListSelectPopup(this, getDataList(getmDeviceType(), getDeviceId()), getDeviceId(), 1, R.style.DialogCustomizeStyle);
        }
        else
        {
            mListSelectPopup.initParam();
        }
        Log.d(TAG, "showImageSelectDialog: getDevIdForSelectImg()=" + PlayStateInfo.getDevIdForSelectImg() + ",getDeviceId()=" + getDeviceId());
        if (PlayStateInfo.getDevIdForSelectImg() == null || PlayStateInfo.getDevIdForSelectImg().equals(getDeviceId()))
        {
            mListSelectPopup.setSelected(PlayStateInfo.getSelectedImgIdxListForAudioPlayer());
        }
        mListSelectPopup.setOnSelectPopupListener(this);
        mListSelectPopup.setLogoAndTip(R.drawable.image_default, R.string.select_bg_image, R.string.no_album);
        mListSelectPopup.showDialog();
    }

    private List<ListSelectItem> getDataList(int deviceType, String deviceId)
    {
        List<ListSelectItem> miList = new ArrayList<ListSelectItem>();
        List<LocalMediaInfo> mediaInfoList = null;

        Bundle bundle = getIntent().getBundleExtra(LocalDeviceInfo.DEVICE_EXTRA_NAME);
        if (bundle == null)
        {
            return null;
        }
        /*
        if (ConstData.DeviceType.isDLNADevice(deviceType))
        {
            //Log.i(TAG, "DEVICE_TYPE_DMS");
            int devId = bundle.getInt(LocalDeviceInfo.DEVICE_ID, -1);
            if (devId == -1)
            {
                return null;
            }
            List<DlnaBaseObjectInfo> tmpList = ObjectFactory.getMediaBrowserClient().getFlatFileFolder(devId,
                    MediaInfoConvertor.LocalType2DlnaType(ConstData.MediaType.IMAGE));
            mediaInfoList = MediaInfoConvertor.DlnaBaseObjectInfoList2LocalMediaInfoList(tmpList);
        }*/
        
      //else if (deviceType == ConstData.DeviceType.DEVICE_TYPE_U || deviceType == ConstData.DeviceType.DEVICE_TYPE_SD)
        else if (ConstData.DeviceType.isLocalDevice(deviceType)) 
        {
            //Log.i(TAG, "DEVICE_TYPE_U");
            String mountPath = bundle.getString(LocalDeviceInfo.MOUNT_PATH);
            mediaInfoList = mLocalDeviceManager.getFlatAVIFile(mountPath, MediaType.IMAGE, 0, 100, EBrowerType.ORDER_TYPE_CHARACTER);
        }
        else
        {
            //Log.i(TAG, "unknown device type");
        }

        ListSelectItem item;
        if (mediaInfoList != null && mediaInfoList.size() != 0)
        {
            for (LocalMediaInfo info : mediaInfoList)
            {
                item = new ListSelectItem(info.getmFileName(), info.getmFiles() + getString(R.string.image_unit), info);
                miList.add(item);
            }
        }
        else
        {
            //Log.i(TAG, "no Image data in it");
        }

        return miList;
    }

    @Override
    public void onListSelected(List<ListSelectItem> list, ArrayList<Integer> selectedIdxList)
    {
        int deviceType = getmDeviceType();

        mMediaList.clear();
        /*
        if (ConstData.DeviceType.isDLNADevice(deviceType))
        {
            int devId = getDevId();
            if (devId == -1)
            {
                return;
            }

            for (ListSelectItem mi : list)
            {
                LocalMediaInfo info = (LocalMediaInfo) mi.getObject();
                List<DlnaBaseObjectInfo> tmpList = ObjectFactory.getMediaBrowserClient().getMediaListByTypeInFolder(devId, info.getmObjectId(),
                        MediaInfoConvertor.LocalType2DlnaType(ConstData.MediaType.IMAGE), EDlnaSortType.DLNA_SORT_TYPE_BY_DATE_DESC);

                List<LocalMediaInfo> localMediaInfoList = MediaInfoConvertor.DlnaBaseObjectInfoList2LocalMediaInfoList(tmpList);
                if (localMediaInfoList != null && localMediaInfoList.size() > 0)
                {
                    mMediaList.addAll(localMediaInfoList);
                }
            }
        }
      //else if (deviceType == ConstData.DeviceType.DEVICE_TYPE_U || deviceType == ConstData.DeviceType.DEVICE_TYPE_SD)
        else*/ if (ConstData.DeviceType.isLocalDevice(deviceType)) 
        {
            Set<String> urls = new HashSet<String>();
            for (ListSelectItem mi : list)
            {
                LocalMediaInfo info = (LocalMediaInfo) mi.getObject();
                List<LocalMediaInfo> listflat = getImagesByUrl(info.getUrl());
                mMediaList.addAll(listflat);
                urls.add(info.getUrl());
            }            
            saveBgPicImageUrls(urls);
            saveBgPicDeviceId(getDeviceId());
        }
        PlayStateInfo.setBackgroundImages(mMediaList);
        mImagePlayStateInfo.setMediaFileList(mMediaList);
        Log.d(TAG, "showImageSelectDialog: onListSelected()=" + PlayStateInfo.getDevIdForSelectImg() + ",getDeviceId()=" + getDeviceId()
                + ",selectedIdxList.size()=" + selectedIdxList.size());
        PlayStateInfo.setDevIdForSelectImg(getDeviceId());
        PlayStateInfo.setSelectedImgIdxListForAudioPlayer(selectedIdxList);
        mImagePlayStateInfo.setCurrentIndex(0);
        mBackgroundRet = true;
        showBackgroundPics(true);
    }
    
    private List<LocalMediaInfo> getImagesByUrl(String url)
    {
        return mLocalDeviceManager.getFlatAVIFileSubWithType(url, MediaType.IMAGE, 0, 100, EBrowerType.ORDER_TYPE_CHARACTER);
    }

    @Override
    public void onSelectPopupHide()
    {
    }

    @Override
    public void onSelectType(MenuItemImpl menuItem)
    {
        ENUMLAYOUTDISPLAYTYPE enumLDT = (ENUMLAYOUTDISPLAYTYPE) menuItem.getSelectType();
        switch (enumLDT)
        {
            case ENUM_RANDOM_PLAY:
                mPlayModeText.setText(R.string.play_mode_random_play);
                mPlayModeIcon.setImageResource(R.drawable.playmode_icon_random);
                AudioPlayStateInfo.setPlayMode(ConstData.MediaPlayMode.MP_MODE_RONDOM);
                break;

            case ENUM_LOOP_PLAY:
                mPlayModeText.setText(R.string.play_mode_loop_play);
                mPlayModeIcon.setImageResource(R.drawable.playmode_icon_loop);
                AudioPlayStateInfo.setPlayMode(ConstData.MediaPlayMode.MP_MODE_ALL_CYC);
                break;

            case ENUM_SEQUENTIAL_PLAY:
                mPlayModeText.setText(R.string.play_mode_sequence_play);
                mPlayModeIcon.setImageResource(R.drawable.playmode_icon_sequential);
                AudioPlayStateInfo.setPlayMode(ConstData.MediaPlayMode.MP_MODE_ALL);
                break;

            case ENUM_SINGLE_PLAY:
                mPlayModeText.setText(R.string.play_mode_single_play);
                mPlayModeIcon.setImageResource(R.drawable.playmode_icon_single_play);
                AudioPlayStateInfo.setPlayMode(ConstData.MediaPlayMode.MP_MODE_SINGLE);
                break;

            case ENUM_OPEN_BACKGROUND_PIC:
                MenuItemImpl menuFocusItem = mPopMenu.getCurrentFocusItemImpl();
                mStIsShowBackgroundPic = false;
                // xWX184171 DTS2014021910705 需要断电保存
                setBackgroundPicFlag(true);
                mBackgroundRet = true;

                Bundle bundle = getIntent().getBundleExtra(LocalDeviceInfo.DEVICE_EXTRA_NAME);
                if (bundle == null)
                {
                    // 搜索出来的音乐，直接显示默认相册
                    if (menuFocusItem != null && enumLDT == menuFocusItem.getSelectType())
                    {
                        mBackgroundRet = false;
                    }
                    mShowDefaultBg = true;
                    showBackgroundPics(true);
                }
                else
                {

                    if (menuFocusItem != null && enumLDT == menuFocusItem.getSelectType())
                    {
                        mBackgroundRet = false;
                        showImageSelectDialog();
                        mUiHandler.removeMessages(MSG_SHOW_BACKGRPUND_PICS);
                    }
                }
                break;

            case ENUM_CLOSE_BACKGROUND_PIC:
                mStIsShowBackgroundPic = false;
                setBackgroundPicFlag(false);
                removeUiMessage(MSG_SHOW_BACKGRPUND_PICS);
                if (mIMPRL != null)
                {
                    mIMPRL.setVisibility(View.GONE);
                }
                if (mImageSwitcher != null)
                {
                    if (mIMPRL != null)
                    {
                        mIMPRL.removeView(mImageSwitcher);
                    }
                    mImageSwitcher.stopDLNAHandler();
                    mImageSwitcher.uninit();
                    mImageSwitcher = null;
                }
                break;

            case ENUM_AUDIO_PLAY_MODE:
                mBottomPopMenu.hide();
                createOptionsMenu(true);
                break;
            case ENUM_AUDIO_PLAY_BACKGROUND:
                mBottomPopMenu.hide();
                createOptionsMenu(false);
                break;
            default:
                break;
        }
    }

    @Override
    public void arriveFirstElement(boolean arg0)
    {
    }

    @Override
    public void arriveLastElement(boolean arg0)
    {
    }

    @Override
    public void canntshowImage()
    {
    }

    @Override
    public void cantMoveNext()
    {
    }

    @Override
    public void cantMovePrevious()
    {
    }

    @Override
    public void rotateIamgeFailed()
    {
    }

    @Override
    public void setAutoPlay()
    {
        // 恢复播放状态
        if (mImageSwitcher != null)
        {
            mImageSwitcher.setAutoMode(true, AUTO_PLAY_INTERVAL);
            mImageSwitcher.setAutoPlayArg(true, AUTO_PLAY_INTERVAL);
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
            Log.d(TAG, "setCantDisplay---->VISIBLE");
            mDisplayException.setVisibility(View.VISIBLE);
        }
        else
        {
            Log.d(TAG, "setCantDisplay---->GONE");
            mDisplayException.setVisibility(View.GONE);
        }
    }

    @Override
    public void showDetail(LocalMediaInfo arg0, int arg1, int arg2)
    {
    }

    @Override
    public void showImageEnd()
    {
    }

    private void showBackgroundPics(boolean fromResult)
    {
        Log.d(TAG, " showBackgroundPics");

        List<LocalMediaInfo> mediaLists;
        String bgDevId;
        
        if (mImageSwitcher == null)
        {
            mImageSwitcher = new DLNAImageSwitcher(this);
            mImageSwitcher.init();
            UriTexture.setCacheDir(getCacheDir().getAbsolutePath());
            mImageSwitcher.setBackgroundColor(Color.TRANSPARENT);

            mImageSwitcher.setProgress(mProgressBar);
            mImageSwitcher.setAnimateFirstView(false);
            mImageSwitcher.setListener(this);
            
            int deviceType = getmDeviceType();
            if (deviceType == ConstData.DeviceType.DEVICE_TYPE_U || deviceType == ConstData.DeviceType.DEVICE_TYPE_SD)
            {
                mediaLists = getBgImagesFromPreferences();
                bgDevId = getBgPicDeviceId();
                Log.d(TAG, "cc msg showBackgroundPics bgDevId = " + bgDevId + " getDeviceId() = " + getDeviceId());
                if (!isListEmpty(mediaLists) && bgDevId != null && bgDevId.equals(getDeviceId()))
                {                
                    if (isListEmpty(PlayStateInfo.getBackgroundImages()))
                    {
                        PlayStateInfo.setDevIdForSelectImg(bgDevId);
                        PlayStateInfo.setBackgroundImages(mediaLists);
                    }                    
                }
            }
            
            mImagePlayStateInfo.setMediaFileList(PlayStateInfo.getBackgroundImages());
            mImageSwitcher.setPlayInfo(mImagePlayStateInfo);
            mImageSwitcher.setInternalPlayer(true);
            android.widget.RelativeLayout.LayoutParams lp = new android.widget.RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,
                    LayoutParams.FILL_PARENT);
            lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            if (mIMPRL != null)
            {
                mIMPRL.addView(mImageSwitcher, lp);
            }
        }

        if (!mIMPRL.isShown())
        {
            if (fromResult || mBackgroundRet)
            {
                mBackgroundRet = false;
                if (PlayStateInfo.getDevIdForSelectImg() == null || !(PlayStateInfo.getDevIdForSelectImg().equals(getDeviceId()))
                        || mImagePlayStateInfo.getMediaList().size() == 0)
                {
                    mImageSwitcher.setShowDefaultPic(true);
                }
                else
                {
                    mImageSwitcher.setShowDefaultPic(false);
                }
                mImageSwitcher.currImage();
            }
            mIMPRL.setVisibility(View.VISIBLE);
            // zkf61715 避免出现刷新同一张图片
            mImageSwitcher.setAutoMode(true, AUTO_PLAY_INTERVAL);

            // zkf61715 避免焦点框的出现
            mGlobalFocus.setVisibility(View.INVISIBLE);

            // zkf61715 DTS2014011516299 选择完图片回到音乐界面展示背景图片
          
            //begin modify by caochao for DTS2014110900287 媒体中心推送音乐显示背景图片，然后推送下一首音乐信息不刷新仍为第一次推送的音乐信息
            refreshAudioPreview(mAudioPlayStateInfo.getCurrentIndex());   
            //end modify by caochao for DTS2014110900287 媒体中心推送音乐显示背景图片，然后推送下一首音乐信息不刷新仍为第一次推送的音乐信息
            mFirstShowBg = false;            

        }
        else
        {
            // Toast.makeText(this, "failed to play image",Toast.LENGTH_SHORT).show();
        }
    }

    private int getDevId()
    {
        Bundle bundle = getIntent().getBundleExtra(LocalDeviceInfo.DEVICE_EXTRA_NAME);
        if (bundle == null)
        {
            return -1;
        }
        return bundle.getInt(LocalDeviceInfo.DEVICE_ID, -1);
    }
    
    private void exitRemoteConnect()
    {
        stop(true);
        requestExit();
    }
    
    
    private RetrieveCompleteListener mRetrieveCompleteListener = new RetrieveCompleteListener()
    {
        @Override
        public void onComplete(LocalMediaInfo mediaInfo, SongInfo songinfo)
        {
            if (isCurrentSameUri(mediaInfo))
            {
                if (!StringUtils.isEmpty(songinfo.getArtist()))
                {
                    artistFromRetriever = songinfo.getArtist();
                }

                if (!StringUtils.isEmpty(songinfo.getSongName()))
                {
                    titleFromRetriever = songinfo.getSongName();
                }

                if (!StringUtils.isEmpty(songinfo.getAlbum()))
                {
                    albumFromRetriever = songinfo.getAlbum();
                }

                requestRefreshMediaInfo();
            }

            removeLogicalMessage(AudioPlayerMsg.MSG_REQUEST_LYRIC);
            sendLogicalMessage(obtainLogicalMessage(AudioPlayerMsg.MSG_REQUEST_LYRIC, 0, 0, mCurrentMediaInfo), 0);
        }

        @Override
        public void onComplete(LocalMediaInfo mediaInfo, Bitmap bitmap)
        {
            if (isCurrentSameUri(mediaInfo) && (bitmap != null))
            {
                sendUiMessage(obtainUiMessage(AudioPlayerMsg.MSG_REFRESH_ALBUMICON, 0, 0, bitmap), 0);
            }
        }
    };

    private boolean isCurrentSameUri(LocalMediaInfo mediaInfo)
    {
        if ((mediaInfo == null) || (null == mediaInfo.getUrl()))
        {
            return false;
        }

        if (null == mCurrentMediaInfo)
        {
            return false;
        }

        Log.d(TAG, "cc msg onComplete url = " + mediaInfo.getUrl() + " current focus url = " + mCurrentMediaInfo.getUrl());
        if (mediaInfo.getUrl().equals(mCurrentMediaInfo.getUrl()))
        {
            return true;
        }

        return false;
    }

    //    // 获取当前播放图片的设备类型
    //    private int getBgPicDeviceType()
    //    {
    //        SharedPreferences sp = getAudioSettingsSharedPreferences();
    //        return sp.getInt(PERFS_DEVICE_TYPE, INVALID_VALUE);
    //    }
    //
    //    // 设置当前播放图片的设备类型
    //    private void saveBgPicDeviceType(int type)
    //    {
    //        SharedPreferences sp = getAudioSettingsSharedPreferences();
    //        Editor ed = sp.edit();
    //        ed.putInt(PERFS_DEVICE_TYPE, type);
    //        ed.commit();
    //    }

    // 获取当前播放图片的设备类型
    private String getBgPicDeviceId()
    {
        SharedPreferences sp = getAudioSettingsSharedPreferences();
        return sp.getString(PERFS_DEVICE_ID, "");        
    }

    // 设置当前播放图片的设备类型
    private void saveBgPicDeviceId(String devId)
    {
        SharedPreferences sp = getAudioSettingsSharedPreferences();
        Editor ed = sp.edit();
        ed.putString(PERFS_DEVICE_ID, devId);
        ed.commit();
    }
    // 获取背景图片的所有url信息
    private Set<String> getBgPicImageUrls()
    {
        SharedPreferences sp = getAudioSettingsSharedPreferences();        
        return sp.getStringSet(PERFS_BG_IMAGE_URLS, null);        
    }

    // 设置背景图片的所有url信息
    private void saveBgPicImageUrls(Set<String> urls)
    {
        SharedPreferences sp = getAudioSettingsSharedPreferences();
        Editor ed = sp.edit();
        ed.putStringSet(PERFS_BG_IMAGE_URLS, urls);
        ed.commit();
    }
    
    private SharedPreferences getAudioSettingsSharedPreferences()
    {
        return getSharedPreferences(PERFS_NAME, Context.MODE_PRIVATE);
    }
    
    private List<LocalMediaInfo> getBgImagesFromPreferences()
    {
        List<LocalMediaInfo> lists = new ArrayList<LocalMediaInfo>();
        Set<String> urls =  getBgPicImageUrls();
        if (urls == null || urls.size() == 0)
        {
            return lists;
        }
        Iterator<String> iterator = urls.iterator();
        while (iterator.hasNext())
        {
            String url = iterator.next();
            if (isFileExist(url))
            {
                List<LocalMediaInfo> listflat = getImagesByUrl(url);
                if (!isListEmpty(listflat))
                {
                    lists.addAll(listflat);
                }
            }
        }
        
        return lists;
    }
    
    private boolean isFileExist(String url)
    {
        File file = new File(url);
        if (file.exists())
        {
            return true;
        }
        return false;
    }
    
    private boolean isListEmpty(List<LocalMediaInfo> lists)
    {
        if (lists == null  || lists.size() == 0)
        {
            return true;
        }
        return false;
    }
	
	//begin add by caochao for DTS2014110900287 媒体中心推送音乐显示背景图片，然后推送下一首音乐信息不刷新仍为第一次推送的音乐信息
    private void refreshAudioPreview(int index)
    {
    	if (mIMPRL != null && mIMPRL.isShown())
        {
            LocalMediaInfo tempMediaBaseInfo = mAudioPlayStateInfo.getMediaInfo(index);
            if (tempMediaBaseInfo != null)
            {
               
                mBackgroundAudioPreviewWidget.setBaseMediaInfo(tempMediaBaseInfo);
                mBackgroundAudioPreviewWidget.show();
            }
        }
    }
    //end add by caochao for DTS2014110900287 媒体中心推送音乐显示背景图片，然后推送下一首音乐信息不刷新仍为第一次推送的音乐信息
	
	private void createMediaRetrieve()
    {

        RetrieveInfoManager.getInstance().setRetrieveCompleteListener(mRetrieveCompleteListener);
    }
    private void destoryMediaRetrieve()
    { 
    }

	@Override
	public void onServiceConnected() {
		
	}

	@Override
	public int getLayoutRes() {
		return R.layout.activity_audio_player;
	}

	@Override
	public void init() {
		mLocalDeviceManager = LocalDeviceManager.getInstance(getBaseContext());

		// xWX184171 DTS2014021910705 需要断电保存 add by 2014.2.20
		getBackgroundPicFlag();

		// mAudioManager = (AudioManager)
		// getSystemService(Context.AUDIO_SERVICE);

		// CacheManager.getInstance().setCacheDir(getBaseContext().getCacheDir().toString()
		// + "/");

		synchronized (mHandlerLock) {
			// 创建过程执行线程
			mLogicalThread = new HandlerThread(this.getClass().toString());
			mLogicalThread.start();
			mLogicalHandler = new Handler(mLogicalThread.getLooper(), mLogicalHandlerCallback);

		}

		mParentLinear = (LinearLayout) findViewById(R.id.parent_linear);

		// 音乐专辑组件
		mAlbumInfoView = (PreviewWidget) findViewById(R.id.music_info);

		// 进度条
		mSeekBar = (SeekBar) findViewById(R.id.music_seekbar);
		mSeekBar.setFocusable(false);
		mSeekBar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);

		mMusicPlayer = new MusicPlayer();

		// 创建焦点框
		mGlobalFocus = new GlobalFocus(this);
		mGlobalFocus.setFocusRes(R.color.music_list_select_color);
		mGlobalFocus.setVisibility(View.INVISIBLE);

		LayoutParams params = new LayoutParams(DeviceInfoUtils.getScreenWidth(this) / 2 - SizeUtils.dp2px(this, 20), SizeUtils.dp2px(this, 69));

		params.topMargin = -SizeUtils.dp2px(this, 652);
		params.leftMargin = DeviceInfoUtils.getScreenWidth(this) / 2;
		mGlobalFocus.setFocusInitParams(params);
		mParentLinear.addView(mGlobalFocus);

		// 播放列表组件
		mPlayListView = (PlayListView) findViewById(R.id.music_playlist);
		mPlayListView.setOnMenuListener(mOnMenuListener);
		mPlayListView.addFocus(mGlobalFocus);
		mPlayListView.setItemFocusListener(mGlobalFocus);
		mPlayListView.setItemTouchListener(mOnItemTouchListener);

		// 播放列表适配器
		mAdapter = new TextViewListAdapter(this);

		mPlayModeIcon = (ImageView) findViewById(R.id.playmode_icon);
		mPlayModeText = (TextView) findViewById(R.id.main_playmode_text);

		// zkf61715
		showPlayMode(AudioPlayStateInfo.getPlayMode());

		mMusicAlreadyPlayedDuration = (TextView) findViewById(R.id.music_already_played_duration);
		mMusicTotalDuration = (TextView) findViewById(R.id.music_total_duration);

		// 左边歌词
		mLeftLyric = (TextView) findViewById(R.id.left_lyric);
		// 右边歌词
		mRightLyric = (TextView) findViewById(R.id.right_lyric);

		loadDefaultMusicBitmap();

		mAlbumInfoView.setRes(mDefaultMusicBitmap, null, null);

		loadPlayModeSettingLayout();

		// 注册同步播放列表完成监听器
		mAudioPlayStateInfo.registerOnPlayListSyncCompletedListener(mOnPlayListSyncCompletedListener);

		// mPriInterface = new MediaplayerPriInterface();

		// 初始化杜比的弹出视窗
		doblyPopWin = new DoblyPopWin(this);

		// 进度条
		mProgressBar = (ProgressBar) findViewById(R.id.circleProgressBar);
		if (mProgressBar != null) {
			mProgressBar.setVisibility(View.GONE);
		}
		mIMPRL = (RelativeLayout) findViewById(R.id.fullRelative);
		mDisplayException = (LinearLayout) findViewById(R.id.image_exception);
		mNoLyricText = (TextView) findViewById(R.id.center_no_lyric);
		// zkf61715 暂时不显示“暂无歌词
		mNoLyricText.setVisibility(View.GONE);
		if (mIMPRL != null) {
			mIMPRL.setVisibility(View.GONE);
		}
		mBackgroundAudioPreviewWidget = new BackgroundAudioPreviewWidget(this);

		mGlobalFocus.setBackgroud(mIMPRL);

		ToastUtil.build(getApplicationContext());
	}
}
