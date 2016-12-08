package com.rockchips.mediacenter.viewutils.playlist;

import momo.cn.edu.fjnu.androidutils.utils.SizeUtils;
import android.content.Context;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;

import com.rockchips.mediacenter.viewutils.playlist.PlayListView.OnItemFocusChangeListener;

public class GlobalFocus extends ImageView implements OnItemFocusChangeListener
{
    /**
     * 动画消息handler
     */
    private Handler mHandler;

    /**
     * 动画插值器
     */
    private Interpolator mInterpolator;

    /**
     * 焦点框动画对象
     */
    private Animation mAnimation;

    /**
     * 动画持续时长，单位毫秒
     */
    private int mDuration;

    /**
     * 动画消息延迟发送时长，单位毫秒
     */
    private int mDelayMillis = 0;

    /**
     * 动画开始消息
     */
    private static final int MSG_START_ANIMATION = 1001;

    /**
     * 焦点框参数
     */
    private LayoutParams mLayoutParams;

    private float fromXDelta;

    private float toXDelta;

    private float fromYDelta;

    private float toYDelta;

    /**
     * 焦点框初始topMargin
     */
    private int mInitTopMargin;

    /**
     * 焦点框初始leftMargin
     */
    private int mInitLeftMargin;

    // zkf61715 背景，显示时焦点框不显示
    private View mBackgroud;

    public GlobalFocus(Context context)
    {
        super(context);
        init();
    }

    public GlobalFocus(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public GlobalFocus(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init();
    }

    public void setBackgroud(View view)
    {
        mBackgroud = view;
    }

    /**
     * 初始化函数
     */
    public void init()
    {
        // 初始化动画持续时长
        mDuration = 200;

        // 初始化动画插值器，默认线性插值
        mInterpolator = new LinearInterpolator();

        mHandler = new Handler(callback);
    }

    /**
     * 获得动画插值器
     * 
     * @return
     */
    public Interpolator getInterpolator()
    {
        return mInterpolator;
    }

    /**
     * 设置动画插值器
     * 
     * @param mInterpolator
     */
    public void setInterpolator(Interpolator mInterpolator)
    {
        this.mInterpolator = mInterpolator;
    }

    /**
     * 设置焦点框显示图片
     * 
     * @param resId 焦点框资源id
     */
    public void setFocusRes(int resId)
    {
        setScaleType(ScaleType.CENTER);
        this.setBackgroundResource(resId);
    }

    /**
     * 根据偏移量做动画
     */
    public void doAnimation()
    {
        clearAnimation();
        mAnimation = new TranslateAnimation(fromXDelta, toXDelta, fromYDelta - toYDelta, 0);
        mAnimation.setDuration(mDuration);

        mLayoutParams.leftMargin = mInitLeftMargin;
        mLayoutParams.topMargin = (int) toYDelta;
        setLayoutParams(mLayoutParams);

        mAnimation.setInterpolator(mInterpolator);

        startAnimation(mAnimation);
    }

    /**
     * handler回调接口
     */
    private Callback callback = new Callback()
    {

        @Override
        public boolean handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case MSG_START_ANIMATION:
                    // zkf61715
                    if (mBackgroud != null && mBackgroud.isShown())
                    {
                        return true;
                    }
                    doAnimation();
                    return true;
            }
            return false;
        }

    };

    /**
     * 清除并发送消息
     * 
     * @param what 消息内容
     * @param delayMills 消息延迟发送时长
     */
    public void clearAndSendMsg(int what, long delayMills)
    {
        if (mHandler != null)
        {
            mHandler.removeMessages(what);
            mHandler.sendEmptyMessageAtTime(what, delayMills);
        }
    }

    /**
     * 设置焦点框初始参数（topMargin，leftMargin）
     * 
     * @param params
     */
    public void setFocusInitParams(LayoutParams params)
    {
        setLayoutParams(params);
        this.mLayoutParams = params;
        mInitTopMargin = params.topMargin;
        mInitLeftMargin = params.leftMargin;
    }

    /**
     * 获得焦点框初始化参数
     * 
     * @return
     */
    public LayoutParams getFocusInitParams()
    {
        return mLayoutParams;
    }

    /**
     * @return the initTopMargin
     */
    public int getInitTopMargin()
    {
        return mInitTopMargin;
    }

    /**
     * @return the initLeftMargin
     */
    public int getInitLeftMargin()
    {
        return mInitLeftMargin;
    }

    @Override
    public void onItemFocusChange(int index, float fromXDelta, float toXDelta, float fromYDelta, float toYDelta)
    {
        // Log.d(TAG, "onItemFocusChange() IN...");
        // Log.d(TAG, "fromXDelta:" + fromXDelta + "," + "toXDelta:" + toXDelta
        // + "," + "fromYDelta:" + fromYDelta + "," + "toYDelta:"
        // + toYDelta);

        setFocusOffset(fromXDelta, toXDelta, fromYDelta, toYDelta);
        clearAndSendMsg(MSG_START_ANIMATION, mDelayMillis);
    }

    /**
     * 设置焦点框x、y方向偏移量
     * 
     * @param fromXDelta
     * @param toXDelta
     * @param fromYDelta
     * @param toYDelta
     */
    public void setFocusOffset(float fromXDelta, float toXDelta, float fromYDelta, float toYDelta)
    {
        this.fromXDelta = fromXDelta;
        this.toXDelta = toXDelta;
        this.fromYDelta = fromYDelta;
        this.toYDelta = toYDelta;
    }

    /**
     * @return the mDuration
     */
    public int getDuration()
    {
        return mDuration;
    }

    /**
     * @param duration the mDuration to set
     */
    public void setDuration(int duration)
    {
        this.mDuration = duration;
    };

}
