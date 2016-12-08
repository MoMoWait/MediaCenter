package com.rockchips.mediacenter.viewutils.timelayout;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.format.Time;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rockchips.mediacenter.viewutils.R;

/**
 * 
 * 视频播放器右上角的时间控件
 * 
 * @author l00174030
 * @version [2013-2-20]
 */
public class TimeLayout extends LinearLayout
{
    // 主线程UI线程
    private Handler mUiHandler;

    // 时间控件，显示hh:mm:
    private TextView mHhourView;

    // 时间控件，显示ss
    private TextView mSecondView;

    // 获取时间线程，包括格式化时间
    private HandlerThread mHThread;

    // 获取时间线程的handle
    private Handler mLogcialHandler;

    // 用来在消息中传递时间(获取时间线程使用)
    private TimeDto mTimeDto;

    // 用来在消息中传递时间(UI绘图线程使用)
    private TimeDto mTimeDtoUi;

    private static final int MSG_GET_TIME = 1;

    private static final int MSG_UPDATE_TIME = 2;

    private static final int HANDLER_DELAY_TIME = 1000;

    /**
     * <默认构造函数>
     */
    public TimeLayout(Context context)
    {
        super(context);
        TimeLayout.inflate(context, R.layout.time_layout, this);
        init();
    }

    /**
     * <默认构造函数>
     */
    public TimeLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        TimeLayout.inflate(context, R.layout.time_layout, this);
        init();

    }
    
    public void setColor(int color)
    {
        mHhourView.setTextColor(color);
        mSecondView.setTextColor(color);
    }

    /**
     * 初始化函数，用来启动获取时间线程及UI线程的handle
     * 
     * @see [类、类#方法、类#成员]
     */
    private void init()
    {
        // 初始化控件
        mHhourView = (TextView) findViewById(R.id.hourText);
        mSecondView = (TextView) findViewById(R.id.secondText);

        // 启动时间计时线程
        mHThread = new HandlerThread("TimeUpdateThread");
        mHThread.start();
        mLogcialHandler = new Handler(mHThread.getLooper(), mLogicalCallback);

        // 获取UI线程的handle
        mUiHandler = new UIHandler();

        // 开始获取时间
        mLogcialHandler.sendEmptyMessage(MSG_GET_TIME);
    }

    /**
     * 获取时间线程的handle，用来向UI线程发送时间更新消息
     * 
     * @see [类、类#方法、类#成员]
     */
    private Handler.Callback mLogicalCallback = new Handler.Callback()
    {
        // @Override
        public boolean handleMessage(Message msg)
        {

            switch (msg.what)
            {
                case MSG_GET_TIME:
                    // 获取时间
                    Time time = new Time();
                    time.setToNow();

                    mTimeDto = new TimeDto();
                    mTimeDto.setHour(time.hour, time.minute);
                    mTimeDto.setSecond(time.second);

                    // 把时间传递给UI线程绘图
                    Message msgUi = Message.obtain();
                    msgUi.what = MSG_UPDATE_TIME;
                    msgUi.obj = mTimeDto;
                    mUiHandler.sendMessage(msgUi);

                    // 给自己发送获取时间消息
                    mLogcialHandler.removeMessages(MSG_GET_TIME);
                    mLogcialHandler.sendEmptyMessageDelayed(MSG_GET_TIME, HANDLER_DELAY_TIME);
                    break;
                default:
                    break;
            }

            return false;
        }
    };

    /**
     * 
     * UI线程的handle，用来接收时间更新并画到时间控件上
     * 
     * @author l00174030
     * @version [2013-2-20]
     */
    private class UIHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg)
        {

            switch (msg.what)
            {
                case MSG_UPDATE_TIME:
                    // 获取传递过来的时间
                    mTimeDtoUi = (TimeDto) msg.obj;
                    if (mTimeDtoUi == null)
                    {
                        break;
                    }

                    // 更新时间
                    mHhourView.setText(mTimeDtoUi.getHour());
                    mSecondView.setText(mTimeDtoUi.getSecond());
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 
     * 用来在Messgage中传递时间信息
     * 
     * @author l00174030
     * @version [2013-2-20]
     */
    private class TimeDto
    {
        /**
         * 获取hh:mm:
         * 
         * @return hh:mm:
         * @see [类、类#方法、类#成员]
         */
        public String getHour()
        {
            return mHour;
        }

        /**
         * 设置小时及分钟
         * 
         * @param hour 小时
         * @param minute 分钟
         * @see [类、类#方法、类#成员]
         */
        public void setHour(int hour, int minute)
        {
            this.mHour = String.format("%02d:%02d:", hour, minute);
        }

        /**
         * 获取ss
         * 
         * @return ss
         * @see [类、类#方法、类#成员]
         */
        public String getSecond()
        {
            return mSecond;
        }

        /**
         * 设置秒
         * 
         * @param second 秒
         * @see [类、类#方法、类#成员]
         */
        public void setSecond(int second)
        {
            // this.second = formatIntValue(second);
            this.mSecond = String.format("%02d", second);
        }

        // 显示hh:mm:
        private String mHour;

        // 显示ss
        private String mSecond;
    }

    /**
     * 主要用来关闭时间更新线程
     * 
     * @see [类、类#方法、类#成员]
     */
    @Override
    protected void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();

        // 关闭时间更新线程，注意home键及menu键不会退出。
        mHThread.quit();

    }
}
