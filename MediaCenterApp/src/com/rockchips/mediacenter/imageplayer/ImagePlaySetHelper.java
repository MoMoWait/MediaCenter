package com.rockchips.mediacenter.imageplayer;
import com.rockchips.mediacenter.utils.IICLOG;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 *  图片播放设置参数存储类
 * 
 */
public class ImagePlaySetHelper
{

    private static final String TAG = "MediaCenterApp";

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

    /**
     * SharedPreferences --照片详情
     */
    private static final String IMAGE_DETAIL = "IMAGE_PALY_SET_IMAGE_DETAIL";

    /**
     * SharedPreferences --播放模式
     */
    private static final String PLAY_MODE = "IMAGE_PLAY_SET_PLAY_MODE";

    /**
     * SharedPreferences --播放模式
     */
    private static final String FIRST_START_IMAGEPLAY = "FIRST_START_IMAGEPLAY";
    
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
     * 上下文
     */
    private Context mContext;

    /**
     * 切换方式
     */
    private int mSwitchWith;

    /**
     * 切换时间
     */
    private int mSwitchTime;

    /**
     * 播放模式
     */
    private int mPlayMode;

    /**
     * 背景音乐
     */
    private boolean mbIsPlay;

    /**
     * 照片详情
     */
    private boolean mbDisplayDetail;

    /**
     * SharedPreferences
     */
    private SharedPreferences mPreferences;

    /**
     * Editor
     */
    private Editor mEditor;
    
    private IICLOG mLog = IICLOG.getInstance();

    private static final int DEFAULT_SWITCH_WITH_INDEX = EnumAnimationEffect.E_ANIMATION_EFFECT_FADE_OUT.ordinal(); // 默认取缩放动画,默认索引值从0开始

    private static final int DEFAULT_SWITCH_TIME_INDEX = 2; // 默认取动画时间为5S，默认索引值从1开始

    private static final int DEFAULT_PLAY_MODE_INDEX = 0; // 默认取循环播放,默认索引值从0开始

    public ImagePlaySetHelper(Context context)
    {
        this.mContext = context;
        initPlaySet();
    }

    /**
     * 初始化播放设置
     */
    private void initPlaySet()
    {
        if (mContext != null)
        {
            initImagePlayPreferences();
            /** 切换方式，默认为飞出 */
            mSwitchWith = getSwitchWith();

            /** 切换时间，默认5s */
            mSwitchTime = getSwitchTime();

            /** 背景音乐，默认是关闭 */
            mbIsPlay = mPreferences.getBoolean(BG_MUSIC, true);

            /** 照片详情，默认是关闭 */
            mbDisplayDetail = mPreferences.getBoolean(IMAGE_DETAIL, false);

            /***/
        }
    }
    private void initImagePlayPreferences()
    {
        mPreferences = mContext.getSharedPreferences(IMAGE_PLAY_SET, Context.MODE_PRIVATE);
        mEditor = mPreferences.edit();
        
        if (mPreferences.getBoolean(FIRST_START_IMAGEPLAY, true))
        {
            mLog.e(TAG, "==================initImagePlayPreferences=======================");
            mEditor.putInt(SWITCH_WITH, DEFAULT_SWITCH_WITH_INDEX);
            mEditor.putInt(SWITCH_TIME, DEFAULT_SWITCH_TIME_INDEX);
            mEditor.putBoolean(BG_MUSIC, true);
            mEditor.putBoolean(IMAGE_DETAIL, false);
            mEditor.putBoolean(FIRST_START_IMAGEPLAY, false);
            mEditor.commit();  
        }

    }
    /**
     * 存储切换方式标志
     * 
     * @param switchWith 1--飞出 2--消失
     */
    public void saveSwitchWith(int switchWith)
    {
        if (mEditor != null)
        {
            mEditor.putInt(SWITCH_WITH, switchWith);
            mEditor.commit();
            mLog.d(TAG, "SwitchWith==" + getSwitchWith());
        }
    }

    /**
     * 存储切换时间标志
     * 
     * @param switchWith 1--8s 2--5s 3--3s
     */
    public void saveSwitchTime(int switchTime)
    {
        if (mEditor != null)
        {
            mEditor.putInt(SWITCH_TIME, switchTime);
            mEditor.commit();
            mLog.d(TAG, "SwitchTime==" + getSwitchTime());
        }
    }

    /**
     * 存储背景音乐标志
     * 
     * @param switchWith true--开启 false--关闭
     */
    public void saveBGMusic(boolean bgMusic)
    {
        if (mEditor != null)
        {
            mEditor.putBoolean(BG_MUSIC, bgMusic);
            mEditor.commit();
            mLog.d(TAG, "BGMusic==" + isPlay());
        }
    }

    /**
     * 存储显示照片详情标志
     * 
     * @param switchWith true--开启 false--关闭
     */
    public void saveImageDetail(boolean ImageDetail)
    {
        if (mEditor != null)
        {
            mEditor.putBoolean(IMAGE_DETAIL, ImageDetail);
            mEditor.commit();
            mLog.d(TAG, "ImageDetail==" + isDisplayDetail());
        }
    }

    /**
     * 存储播放模式标志
     * @param playMode 0:循环播放 1：顺序播放
     */
    public void savePlayModeIndex(int playMode)
    {
        if (mEditor != null)
        {
            mEditor.putInt(PLAY_MODE, playMode);
            mEditor.commit();
            mLog.d(TAG, "PlayMode==" + getPlayModeIndex());
        }
    }

    /**
     * 获取播放模式标志
     * @return 0:循环播放 1：顺序播放
     */
    public int getPlayModeIndex()
    {
        if (mPreferences != null)
        {
            mPlayMode = mPreferences.getInt(PLAY_MODE, DEFAULT_PLAY_MODE_INDEX);
        }
        return mPlayMode;
    }

    /**
     * 获取切换方式标志位
     * 
     * @return 1--飞出 2--消失
     */
    public int getSwitchWith()
    {
        if (mPreferences != null)
        {
            mSwitchWith = mPreferences.getInt(SWITCH_WITH, DEFAULT_SWITCH_WITH_INDEX);
        }
        return mSwitchWith;
    }

    /**
     * 获取切换时间标志位
     * 
     * @return 1--8s 2--5s 3--3s
     */
    public int getSwitchTime()
    {
        if (mPreferences != null)
        {
            mSwitchTime = mPreferences.getInt(SWITCH_TIME, DEFAULT_SWITCH_TIME_INDEX);
        }
        return mSwitchTime;
    }

    /**
     * 获取是否开启背景音乐标志位
     * 
     * @return true--开启 false--关闭
     */
    public boolean isPlay()
    {
        if (mPreferences != null)
        {
            mbIsPlay = mPreferences.getBoolean(BG_MUSIC, true);
        }
        return mbIsPlay;
    }

    /**
     * 获取是否开启背景音乐标志位
     * 
     * @return true--开启 false--关闭
     */
    public boolean isDisplayDetail()
    {
        if (mPreferences != null)
        {
            mbDisplayDetail = mPreferences.getBoolean(IMAGE_DETAIL, false);
        }
        return mbDisplayDetail;
    }

    /**
     * 返回播放间隔,默认为5秒
     **/
    public int getPlayInterval()
    {
        int mAutoPlayInterval = AUTO_PLAY_INTERVAL_F;
        switch (getSwitchTime())
        {
            case 1:
                mAutoPlayInterval = AUTO_PLAY_INTERVAL_E;
                break;
            case 2:
                mAutoPlayInterval = AUTO_PLAY_INTERVAL_F;
                break;
            case 3:
                mAutoPlayInterval = AUTO_PLAY_INTERVAL_T;
                break;
            default:
                break;
        }
        return mAutoPlayInterval;
    }

}
