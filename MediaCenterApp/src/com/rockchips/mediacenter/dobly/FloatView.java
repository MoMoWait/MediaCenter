package com.rockchips.mediacenter.dobly;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;

import com.rockchips.mediacenter.R;

/**
 *悬浮框
 */
public class FloatView extends ImageView{

	final String TAG = "MediaCenterApp";
	private int imgDubyId = R.drawable.dolby;
	private int imgDoubleOneId = R.drawable.dolbydouble;
	private int screenWidth;
	private int screenHeight;
	private int mTouchX = -1;
	private int mTouchY = -1;
	private int mWidth = -1;
	private int mHeight = -1;
	private double rate1 = 0.08;
	private double rate2 = 0.05;
	private double rate3 = 0.15;
	private double rate4 = 0.05;
	private double rate5 = 0.8;
	private double rate6 = rate5 + rate1 - rate2;
	boolean isShow = false;
	
	private final int HideWindowMsg = 1001;
	private final int WaitTimes = 5*1000;
	
	private HideWindowHandler mHideWindowHandler = null;
	
	private WindowManager windowManager ;
	
	private WindowManager.LayoutParams windowManagerParams = new WindowManager.LayoutParams();

	public FloatView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
    /**
    *imgId “1”或者缺省表示杜比图标
    *其他表示1+1
    */
	public FloatView(Context c,int imgId)
	{
		super(c);
		mHideWindowHandler = new HideWindowHandler();
		initView(c,imgId);
	}
	
	public FloatView(Context c)
	{
		super(c);
		mHideWindowHandler = new HideWindowHandler();
		initView(c,1);
	}
	
	public FloatView(Context c,int imgId,int x,int y)
	{
		super(c);
		mHideWindowHandler = new HideWindowHandler();
		initView(c,imgId);
		mTouchX = x;
		mTouchY = y;
	}
	
    /**
    *x 窗口左上角的x坐标
    *y 窗口左上角的y坐标 
    *w 窗口的宽度
    *h 窗口的高度
    */
	public FloatView(Context c,int imgId,int x,int y,int w,int h)
	{
		super(c);
		mHideWindowHandler = new HideWindowHandler();
		initView(c,imgId);
		mTouchX = x;
		mTouchY = y;
		mWidth = w;
		mHeight = h;
	}
	
    // 初始化窗体
	public void initView(Context c,int imgId)
	{
		windowManager = (WindowManager) c.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
		screenWidth = windowManager.getDefaultDisplay().getWidth();
		screenHeight = windowManager.getDefaultDisplay().getHeight();
		Log.i(TAG,"screenWidth===" + screenWidth + "screenHeight === " + screenHeight);
		windowManagerParams.type = LayoutParams.TYPE_PHONE;
		windowManagerParams.format = PixelFormat.RGBA_8888;	// 背景透明
		windowManagerParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
				| LayoutParams.FLAG_NOT_FOCUSABLE;
		// 调整悬浮窗口至左上角，便于调整坐标
		windowManagerParams.gravity = Gravity.LEFT | Gravity.TOP; 
		// 以屏幕左上角为原点，设置x、y初始值ֵ
		if(imgId == 1){
			this.setImageResource(imgDubyId);
			windowManagerParams.x = (int)(screenWidth * rate4);
			windowManagerParams.y = (int)(screenHeight * rate5);
			windowManagerParams.width = (int)(screenWidth * rate1);
			windowManagerParams.height = (int)(screenHeight * rate1);
			if(mWidth != -1 && mHeight != -1){	
				windowManagerParams.x = (int)(mWidth * rate4) + mTouchX;
				windowManagerParams.y = (int)(mHeight * rate5) + mTouchY;
				windowManagerParams.width = (int)(mWidth * rate1);
				windowManagerParams.height = (int)(mHeight * rate1);
			}else if(mTouchX != -1 && mTouchY != -1){
				windowManagerParams.x = mTouchX;
				windowManagerParams.y = mTouchY;
			}
		}else{
			this.setImageResource(imgDoubleOneId);
			windowManagerParams.x = (int)(screenWidth * rate3);
			windowManagerParams.y = (int)(screenHeight * rate6);
			windowManagerParams.width = (int)(screenWidth * rate2);
			windowManagerParams.height = (int)(screenHeight * rate2);
			if(mWidth != -1 && mHeight != -1){
				windowManagerParams.x = (int)(mWidth * rate3) + mTouchX;
				windowManagerParams.y = (int)(mHeight * rate6) + mTouchY;
				windowManagerParams.width = (int)(mWidth * rate2);
				windowManagerParams.height = (int)(mHeight * rate2);
			}else if(mTouchX != -1 && mTouchY != -1){
				windowManagerParams.x = mTouchX;
				windowManagerParams.y = mTouchY;
			}
		}		
	}
	
	public void setImgResource(int id)
	{
		imgDubyId = id;
	}
		
	// 隐藏该窗体
	public synchronized void hide()
	{
		if(isShow)
		{
			windowManager.removeView(this);
			isShow = false;
			if(mHideWindowHandler.hasMessages(HideWindowMsg)) {
				mHideWindowHandler.removeMessages(HideWindowMsg);
			}
		}			
	}
	
	// 显示该窗体
	public synchronized void show()
	{
		hide();
		if(isShow == false)
		{
			windowManager.addView(this, windowManagerParams);
			isShow = true;
			mHideWindowHandler.sendEmptyMessageDelayed(HideWindowMsg, WaitTimes);	
		}	
	}
	
    /**
     * 五秒隐藏框
     */
	private class HideWindowHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
		    hide();
		}
	}
}
