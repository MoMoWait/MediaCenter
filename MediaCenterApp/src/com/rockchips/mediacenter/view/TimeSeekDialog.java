package com.rockchips.mediacenter.view;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.rockchips.mediacenter.R;

/**
 * 精确时间定位
 * @author s00211113
 * 
 */
public class TimeSeekDialog extends Dialog implements OnClickListener
{
    private static final String TAG = "TimeSeekDialog";

    private static final int MSG_UI_DISMISS = 101;

    private static final int DISMISS_TIME = 6000;

    private static final int MILISECOND_TO_SECOND = 1000;

    private static final int SECOND_TO_MINITE = 60;

    private static final int MINITE_TO_HOUR = 60;

    private static final int TEN = 10;

    private static final int ITEM_0 = 0;

    private static final int ITEM_1 = 1;

    private static final int ITEM_2 = 2;

    private static final int ITEM_3 = 3;

    private static final int ITEM_4 = 4;

    private static final int ITEM_5 = 5;

    private static final int[] VALUES_MAX =
    { 9, 9, 5, 9, 5, 9 };

    private static final int TEXTVIEW_NUMBER = 6;

    private TextView[] mTvArr;

    private ImageView[] mIvUpArr;

    private ImageView[] mIvDownArr;

    private int[] mValues;

    private int mCurFocusIdx;

    private int mMinFocusIdx;

    private View mTimeSeekLayout;

    private int mTotleTime;

    private Handler mHandler = new UIHandler();

    private Window mWindow;

    private OnTimeSeekListener mOnTimeSeekListener;

    private int[] mTvResIdArr =
    { R.id.hourText1, R.id.hourText2, R.id.miniteText1, R.id.miniteText2, R.id.secondText1, R.id.secondText2 };

    private int[] mIvUpResIdArr =
    { R.id.hourImageUp1, R.id.hourImageUp2, R.id.miniteImageUp1, R.id.miniteImageUp2, R.id.secondImageUp1, R.id.secondImageUp2 };

    private int[] mIvDownResIdArr =
    { R.id.hourImageDown1, R.id.hourImageDown2, R.id.miniteImageDown1, R.id.miniteImageDown2, R.id.secondImageDown1, R.id.secondImageDown2 };

    public TimeSeekDialog(Context context)
    {
        super(context);
        init(context);
    }

    /**
     * 精确时间定位回调
     * @author s00211113
     * 
     */
    public interface OnTimeSeekListener
    {
        void onTimeSeeked(int time);

        void onDismiss();
    }

    @SuppressWarnings("deprecation")
    private void init(Context context)
    {
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        mTimeSeekLayout = LayoutInflater.from(context).inflate(R.layout.time_seek_layout, null);
        mTimeSeekLayout.setFocusable(true);
        mTimeSeekLayout.setFocusableInTouchMode(true);
        mTimeSeekLayout.requestFocus();
        //mTimeSeekLayout.setBackgroundDrawable(new BitmapDrawable(context.getResources())); // 必须加上此句，不然阴影会变深
        this.setOnKeyListener(mKeyListener);

        mTvArr = new TextView[TEXTVIEW_NUMBER];
        mIvUpArr = new ImageView[TEXTVIEW_NUMBER];
        mIvDownArr = new ImageView[TEXTVIEW_NUMBER];

        for (int i = 0; i < TEXTVIEW_NUMBER; i++)
        {
            mTvArr[i] = (TextView) mTimeSeekLayout.findViewById(mTvResIdArr[i]);
            mTvArr[i].setOnClickListener(this);
            mTvArr[i].setFocusable(true);
            mTvArr[i].setFocusableInTouchMode(true);
            mIvUpArr[i] = (ImageView) mTimeSeekLayout.findViewById(mIvUpResIdArr[i]);
            mIvUpArr[i].setOnClickListener(this);
            mIvDownArr[i] = (ImageView) mTimeSeekLayout.findViewById(mIvDownResIdArr[i]);
            mIvDownArr[i].setOnClickListener(this);
        }
        this.setContentView(mTimeSeekLayout);
    }

    private void showDialog()
    {
        Log.d(TAG, "popmenu showDialog start");

        windowDeploy();

        // 设置触摸对话框意外的地方取消对话框
        setCanceledOnTouchOutside(true);
        show();

        mTimeSeekLayout.requestFocus();

        Log.d(TAG, "popmenu showDialog end");
    }

    // 设置窗口显示
    @SuppressLint("InlinedApi")
    private void windowDeploy()
    {
        mWindow = getWindow(); // 得到对话框
        mWindow.setBackgroundDrawableResource(R.color.transparent_color); // 设置对话框背景为透明
        WindowManager.LayoutParams wl = mWindow.getAttributes();
        wl.gravity = Gravity.CENTER; // 设置重力
        wl.width = LayoutParams.MATCH_PARENT;
        mWindow.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        mWindow.getDecorView().setSystemUiVisibility(View.INVISIBLE);

        mWindow.setAttributes(wl);
    }

    private OnKeyListener mKeyListener = new OnKeyListener()
    {
        @Override
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event)
        {
            if (isShowing() && event.getAction() == KeyEvent.ACTION_DOWN)
            {
                Log.d(TAG, "TimeSeekLayout onKey() keyCode:" + keyCode);
                switch (keyCode)
                {
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        if (mCurFocusIdx > mMinFocusIdx)
                        {
                            loseFocus(mCurFocusIdx);
                            mCurFocusIdx--;
                            getFocus(mCurFocusIdx);
                        }
                        break;
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        if (mCurFocusIdx < TEXTVIEW_NUMBER - 1)
                        {
                            loseFocus(mCurFocusIdx);
                            mCurFocusIdx++;
                            getFocus(mCurFocusIdx);
                        }
                        break;
                    case KeyEvent.KEYCODE_DPAD_UP:
                        if (!checkIsTimeExceed(mValues[mCurFocusIdx] + 1))
                        {
                            mValues[mCurFocusIdx]++;
                            mTvArr[mCurFocusIdx].setText(String.valueOf(mValues[mCurFocusIdx]));
                            getFocus(mCurFocusIdx);
                        }
                        break;
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        if (mValues[mCurFocusIdx] - 1 >= 0)
                        {
                            mValues[mCurFocusIdx]--;
                            mTvArr[mCurFocusIdx].setText(String.valueOf(mValues[mCurFocusIdx]));
                            getFocus(mCurFocusIdx);
                        }
                        break;

                    case KeyEvent.KEYCODE_0:
                    case KeyEvent.KEYCODE_1:
                    case KeyEvent.KEYCODE_2:
                    case KeyEvent.KEYCODE_3:
                    case KeyEvent.KEYCODE_4:
                    case KeyEvent.KEYCODE_5:
                    case KeyEvent.KEYCODE_6:
                    case KeyEvent.KEYCODE_7:
                    case KeyEvent.KEYCODE_8:
                    case KeyEvent.KEYCODE_9:
                        if (!checkIsTimeExceed(keyCode - KeyEvent.KEYCODE_0))
                        {
                            mValues[mCurFocusIdx] = keyCode - KeyEvent.KEYCODE_0;
                            mTvArr[mCurFocusIdx].setText(String.valueOf(mValues[mCurFocusIdx]));
                            getFocus(mCurFocusIdx);
                        }
                        break;
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                    case KeyEvent.KEYCODE_ENTER:
                        if (mOnTimeSeekListener != null)
                        {
                            Log.d(TAG, "time seek");
                            mOnTimeSeekListener.onTimeSeeked(getTotalTime() * MILISECOND_TO_SECOND);
                        }

                        // 移除在调整时间时调用dismissDelay()产生的延迟消息
                        mHandler.removeMessages(MSG_UI_DISMISS);
                        mHandler.sendEmptyMessage(MSG_UI_DISMISS);
                        return false;
                    default:
                        break;
                }
            }

            dismissDelay(); // 按键重新计时隐藏菜单
            return false;
        }
    };

    private int[] getTimeValues(int time)
    {
        int[] values = new int[TEXTVIEW_NUMBER];

        int temptime = time / MILISECOND_TO_SECOND;
        int minute = temptime / SECOND_TO_MINITE;
        int hour = minute / MINITE_TO_HOUR;
        int second = temptime % SECOND_TO_MINITE;
        minute %= MINITE_TO_HOUR;

        values[ITEM_0] = hour / TEN;
        values[ITEM_1] = hour % TEN;
        values[ITEM_2] = minute / TEN;
        values[ITEM_3] = minute % TEN;
        values[ITEM_4] = second / TEN;
        values[ITEM_5] = second % TEN;

        Log.d(TAG, "getTimeValues: values[0]:" + values[ITEM_0] + "values[1]:" + values[ITEM_1] + "values[2]:" + values[ITEM_2] + "values[3]:"
                + values[ITEM_3] + "values[4]:" + values[ITEM_4] + "values[5]:" + values[ITEM_5]);

        return values;
    }

    private Boolean checkIsTimeExceed(int newCurFocusValue)
    {
        int timeTmp = 0;

        if (newCurFocusValue <= mValues[mCurFocusIdx])
        {
            return false;
        }

        if (newCurFocusValue > VALUES_MAX[mCurFocusIdx])
        {
            return true;
        }

        timeTmp = getTotalTime();

        switch (mCurFocusIdx)
        {
            case ITEM_0:
                timeTmp += (newCurFocusValue - mValues[mCurFocusIdx]) * SECOND_TO_MINITE * MINITE_TO_HOUR * TEN;
                break;
            case ITEM_1:
                timeTmp += (newCurFocusValue - mValues[mCurFocusIdx]) * SECOND_TO_MINITE * MINITE_TO_HOUR;
                break;
            case ITEM_2:
                timeTmp += (newCurFocusValue - mValues[mCurFocusIdx]) * SECOND_TO_MINITE * TEN;
                break;
            case ITEM_3:
                timeTmp += (newCurFocusValue - mValues[mCurFocusIdx]) * SECOND_TO_MINITE;
                break;
            case ITEM_4:
                timeTmp += (newCurFocusValue - mValues[mCurFocusIdx]) * TEN;
                break;
            case ITEM_5:
                timeTmp += (newCurFocusValue - mValues[mCurFocusIdx]);
                break;
            default:
                break;
        }

        if (timeTmp > mTotleTime)
        {
            return true;
        }

        return false;
    }

    private int getTotalTime()
    {
        return ((mValues[ITEM_0] * TEN + mValues[ITEM_1]) * SECOND_TO_MINITE * MINITE_TO_HOUR + (mValues[ITEM_2] * TEN + mValues[ITEM_3])
                * SECOND_TO_MINITE + mValues[ITEM_4] * TEN + mValues[ITEM_5]);
    }

    private void loseFocus(int idx)
    {
        mTvArr[mCurFocusIdx].setBackgroundResource(R.drawable.video_seek_normal_background);
        mIvUpArr[idx].setVisibility(View.INVISIBLE);
        mIvDownArr[idx].setVisibility(View.INVISIBLE);
    }

    private void getFocus(int idx)
    {
        mTvArr[mCurFocusIdx].requestFocus();
        mTvArr[mCurFocusIdx].setBackgroundResource(R.drawable.video_seek_select_background);
        if (!checkIsTimeExceed(mValues[idx] + 1))
        {
            mIvUpArr[idx].setVisibility(View.VISIBLE);
        }
        else
        {
            mIvUpArr[idx].setVisibility(View.INVISIBLE);
        }
        if (mValues[idx] - 1 >= 0)
        {
            mIvDownArr[idx].setVisibility(View.VISIBLE);
        }
        else
        {
            mIvDownArr[idx].setVisibility(View.INVISIBLE);
        }
    }

    private void dismissDelay()
    {
        mHandler.removeMessages(MSG_UI_DISMISS);
        mHandler.sendEmptyMessageDelayed(MSG_UI_DISMISS, DISMISS_TIME);
    }

    /**
     * 显示菜单
     * @param parent 菜单显示需要一个parent，建议是Activity的第一个布局View
     */
    public void show(View parent, int currentTime, int totleTime)
    {
        Log.d(TAG, "TimeSeekLayout show");

        int[] totalValues;
        mTotleTime = totleTime / MILISECOND_TO_SECOND;

        mMinFocusIdx = -1;

        mValues = getTimeValues(currentTime);
        totalValues = getTimeValues(totleTime);

        for (int i = 0; i < TEXTVIEW_NUMBER; i++)
        {
            mTvArr[i].setText(String.valueOf(mValues[i]));
            if (mMinFocusIdx == -1 && totalValues[i] > 0)
            {
                mMinFocusIdx = i;
            }
        }

        if (mMinFocusIdx == -1)
        {
            mMinFocusIdx = 0;
        }

        Log.d(TAG, "mMinFocusIdx = " + mMinFocusIdx);

        mTimeSeekLayout.requestFocus();

        mCurFocusIdx = mMinFocusIdx;

        for (int i = mMinFocusIdx; i < TEXTVIEW_NUMBER; i++)
        {
            if (mValues[mCurFocusIdx] == 0 && checkIsTimeExceed(mValues[mCurFocusIdx] + 1))
            {
                mCurFocusIdx++;
            }
            else
            {
                break;
            }
        }

        getFocus(mCurFocusIdx);

        showDialog();
        dismissDelay();
    }

    /**
     * UI处理handler，将dialog关闭
     * @author s00211113
     * 
     */
    private class UIHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            switch (msg.what)
            {
                case MSG_UI_DISMISS:
                    dismiss();
                    break;

                default:
                    break;
            }
        }
    }

    @Override
    public void dismiss()
    {
        loseFocus(mCurFocusIdx);
        try
        {
            super.dismiss();
        }
        catch (IllegalArgumentException e)
        {
            Log.d(TAG, "dismiss failed......");
        }

        if (mOnTimeSeekListener != null)
        {
            mOnTimeSeekListener.onDismiss();
        }
    }

    public void dismissNotPlay()
    {
        loseFocus(mCurFocusIdx);
        mHandler.removeMessages(MSG_UI_DISMISS);
        try
        {
            super.dismiss();
        }
        catch (IllegalArgumentException e)
        {
            Log.d(TAG, "dismiss failed......");
        }
    }

    public void setOnTimeSeekListener(OnTimeSeekListener newVal)
    {
        mOnTimeSeekListener = newVal;
    }
    
    @Override
    public void onClick(View v)
    {
        int id = v.getId();
        
        Log.d(TAG, "onClick");

        for (int i = 0; i < TEXTVIEW_NUMBER; i++)
        {
            if (id == mTvResIdArr[i])
            {
                if (i >= mMinFocusIdx)
                {
                    Log.d(TAG, "focus:" + i);
                    loseFocus(mCurFocusIdx);
                    mCurFocusIdx = i;
                    getFocus(mCurFocusIdx);
                }
                break;
            }
            else if (id == mIvUpResIdArr[i])
            {
                Log.d(TAG, "up:" + i);
                mValues[mCurFocusIdx]++;
                mTvArr[mCurFocusIdx].setText(String.valueOf(mValues[mCurFocusIdx]));
                getFocus(mCurFocusIdx);
                break;
            }
            else if (id == mIvDownResIdArr[i])
            {
                Log.d(TAG, "down:" + i);
                mValues[mCurFocusIdx]--;
                mTvArr[mCurFocusIdx].setText(String.valueOf(mValues[mCurFocusIdx]));
                getFocus(mCurFocusIdx);
                break;
            }
        }

        dismissDelay(); // 按键重新计时隐藏菜单
    }
}
