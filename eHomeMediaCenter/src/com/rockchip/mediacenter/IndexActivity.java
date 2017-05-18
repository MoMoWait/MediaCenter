/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Filename:    IndexActivity.java  
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2012-4-18 下午08:33:46  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2012-4-18      fxw         1.0         create
*******************************************************************/   


package com.rockchip.mediacenter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import com.rockchip.mediacenter.common.logging.Log;
import com.rockchip.mediacenter.common.logging.LogFactory;
import com.rockchip.mediacenter.mediaplayer.MediaPlayer;
import com.rockchip.mediacenter.mediaserver.MediaServer;
import com.rockchip.mediacenter.plugins.widget.IndicatorGridView;
import com.rockchip.mediacenter.settings.MediaSettings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 *
 * @author fxw
 * @since 1.0
 */
public class IndexActivity extends Activity implements AdapterView.OnItemSelectedListener, AdapterView.OnItemClickListener {

	public static Log logger = LogFactory.getLog(IndexActivity.class);
	public static final int MSG_HIDE_PROGRESS = 1;
	public static final int MSG_SHOW_PROGRESS = 2;
	private static final int RUN_SPEED = 120;//120ms
	private static final int MAX_LEVEL = 6;
	private static final int STATE_INIT = 1;
	private static final int STATE_INITING = 2;
	private static final int STATE_INITED = 3;
	private IndicatorGridView mGridView;
	private View mHeadView;
	private TextView mAmTextView;
	private TextView mTimeTextView;
	private TextView mDateTextView;
	private ProgressBar mProgressBar;
	private Toast mDmsToast;
	private Handler mMainHandler = new MainHandler();
	private FunctionAdapter mFunctionAdapter;
	private SystemDeviceManager mSystemDeviceManager;
	private NetworkDetecting mNetworkDetecting;
	private int mInitState;
	
	//光圈运动相关变量
	private Drawable mCursorDrawable;
	private ImageView mSelectedIconView;
	private int mSelectedPosition = 1;//default 1
	private int mLevel = 0;
	private boolean isStopped = false;
	//用于时间格式
	private boolean isChinese = false;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mGridView = (IndicatorGridView)findViewById(R.id.main_function);
		mHeadView = findViewById(R.id.main_head);
		mAmTextView = (TextView)findViewById(R.id.main_am_pm);
		mTimeTextView = (TextView)findViewById(R.id.main_time);
		mDateTextView = (TextView)findViewById(R.id.main_date);
		mProgressBar = (ProgressBar)findViewById(R.id.main_loading);
		mFunctionAdapter = new FunctionAdapter(this, createFunctionList());
		mGridView.setAdapter(mFunctionAdapter);
		mGridView.setOnItemSelectedListener(this);
		mGridView.setOnItemClickListener(this);
		mGridView.setIconBackground(R.drawable.main_function_item_bg);
		mGridView.setCursorResource(R.drawable.main_cursor);
		mGridView.setMoveStep(40);
		mCursorDrawable = getResources().getDrawable(R.drawable.main_function_cursor);
		
		mInitState = STATE_INIT;
		initSystemService();
		mNetworkDetecting = new NetworkDetecting(this);
		mSystemDeviceManager = new SystemDeviceManager(this);
		mSystemDeviceManager.setBindListener(new SystemDeviceManager.BindListener() {
			public void onBindCompleted() {
				initSystemDevice();
			}
		});
		mSystemDeviceManager.startManager();
		mGridView.setSelection(1);
	}
	
	/** 
	 * <p>Title: onResume</p> 
	 * <p>Description: </p>  
	 * @see android.app.Activity#onResume() 
	 */
	@Override
	protected void onResume() {
		mNetworkDetecting.detect();
		mHeadView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.main_head_top_in));
		super.onResume();
		startAnimation();
		startUpdateTime();
		if(mSystemDeviceManager.isConnectService()){
			initSystemDevice();
		}
		mGridView.requestFocus();
		System.gc();
		Locale locale = Locale.getDefault();
		isChinese = "zh".equals(locale.getLanguage());
	}
	
	/** 
	 * <p>Title: onPause</p> 
	 * <p>Description: </p>  
	 * @see android.app.Activity#onPause() 
	 */
	@Override
	protected void onPause() {
		super.onPause();
		stopAnimation();
		stopUpdateTime();
		//mHeadView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.main_head_top_out));
	}
	
	/** 
	 * <p>Title: onDestroy</p> 
	 * <p>Description: </p>  
	 * @see android.app.Activity#onDestroy() 
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mSystemDeviceManager.stopManager();
	}
	
	/** 
	 * <p>Title: onBackPressed</p> 
	 * <p>Description: </p>  
	 * @see android.app.Activity#onBackPressed() 
	 */
	@Override
	public void onBackPressed() {
		Intent intent = new Intent(this, DLNAService.class);
		stopService(intent);//关闭控制点服务, 如果要将控制点作为服务, 后台发现则不要关闭该服务
		super.onBackPressed();
	}
	
	/**
	 * 服务初始化
	 */
	private synchronized void initSystemService(){
		Intent intent = new Intent(this, DLNAService.class);
		startService(intent);
		intent = new Intent(this, SystemDeviceService.class);
		startService(intent);
	}
	
	private synchronized void initSystemDevice(){
		if(!mNetworkDetecting.isConnect()){
			return;
		}
		if(mInitState != STATE_INIT){
			logger.debug("System Device Initing or Inited...");
			return;
		}
		mInitState = STATE_INITING;
		new Thread(){
			public void run() {
//				try {
//					sleep(500);
//				} catch (InterruptedException e) {
//				}
				logger.debug("System Device Start init...");
				mMainHandler.sendEmptyMessage(MSG_SHOW_PROGRESS);
				if(SystemSettingUtils.getMediaServerState(IndexActivity.this)){
					mSystemDeviceManager.startMediaServerWithSavedConfig();
				}
				if(SystemSettingUtils.getMediaRendererAutoable(IndexActivity.this)){
					mSystemDeviceManager.startMediaRenderer();
				}
				mMainHandler.sendEmptyMessage(MSG_HIDE_PROGRESS);
				mInitState = STATE_INITED;
				logger.debug("System Device finish init...");
			}
		}.start();
	}
	
	/** 
	 * <p>Title: onItemSelected</p> 
	 * <p>Description: </p> 
	 */
	@Override
	public void onItemSelected(AdapterView<?> arg0, View view, int position,
			long arg3) {
		stopAnimation();
		startAnimation();
	}
	public void onNothingSelected(AdapterView<?> arg0) {
		stopAnimation();
	}
	
	/** 
	 * <p>Title: onItemClick</p> 
	 * <p>Description: </p> 
	 * @param parent
	 * @param view
	 * @param position
	 * @param id 
	 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long) 
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		stopAnimation();
		FunctionItem item = (FunctionItem)parent.getItemAtPosition(position);
		if(item.activity.equals(MediaPlayer.class)){
			if(!mNetworkDetecting.detect()){
				return;
			}
			Intent intent = new Intent(this, item.activity);
			startActivity(intent);
			finish();
		}else if(item.activity.equals(MediaServer.class)||item.activity.equals(MediaSettings.class)){
			if(mInitState==STATE_INIT){
				initSystemDevice();
			}else if(mInitState==STATE_INITING){
				if(mDmsToast==null){
					mDmsToast = Toast.makeText(IndexActivity.this, R.string.main_dms_initing, Toast.LENGTH_LONG);
				}
				mDmsToast.show();
			}else{
				if(mDmsToast!=null){
					mDmsToast.cancel();
				}
				Intent intent = new Intent(this, item.activity);
				startActivity(intent);
			}
		}else{
			Intent intent = new Intent(this, item.activity);
			startActivity(intent);
		}
	}
	
	//start update
	private void startUpdateTime(){
		mMainHandler.post(mTimeRunnable);
	}
	private void stopUpdateTime(){
		mMainHandler.removeCallbacks(mTimeRunnable);
	}
	//更新时钟
	private Runnable mTimeRunnable = new Runnable(){
		public void run() {
			Calendar calendar = Calendar.getInstance();
			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH)+1;
			int day = calendar.get(Calendar.DAY_OF_MONTH);
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			if(isChinese){
				String ampm = "上午";
				if(hour>=12){
					ampm = "下午";
				}
				int minute = calendar.get(Calendar.MINUTE);
				mDateTextView.setText(year+" - "+padZero(month)+" - "+padZero(day));//年月日
				mTimeTextView.setText(padZero(hour)+":"+padZero(minute));//时分
				mAmTextView.setText(ampm);
			}else{
				String ampm = "AM";
				if(hour>=12){
					ampm = "PM";
					hour -= 12; 
				}
				int minute = calendar.get(Calendar.MINUTE);
				//int second = calendar.get(Calendar.SECOND);
				mDateTextView.setText(padZero(month)+" / "+padZero(day)+" / "+year);//月日年
				mTimeTextView.setText(padZero(hour)+":"+padZero(minute));//时分
				//mAmTextView.setText(padZero(second));//AM/PM
				mAmTextView.setText(ampm);
			}
			mMainHandler.postDelayed(this, 5000);
		}
	};
	//补0
	private String padZero(int value){
		if(value<10) return "0"+value;
		return ""+value;
	}
	
	//start
	private void startAnimation(){
		isStopped = false;
		mMainHandler.post(mCursorRunnable);
	}
	//stop
	private void stopAnimation(){
		mMainHandler.removeCallbacks(mCursorRunnable);
		isStopped = true;
		resetIconView();
	}
	//绘制运动光圈
	private Runnable mCursorRunnable = new Runnable(){
		public void run() {
			int position = mGridView.getSelectedItemPosition();
			if(position<0||mGridView.getSelectedView()==null||
					!mGridView.isFocused()){
				mMainHandler.removeCallbacks(this);
				isStopped = true;
				return;
			}
			if(position!=mSelectedPosition){
				mSelectedPosition = position;
				resetIconView();
			}
			if(mSelectedIconView==null){
				if(mGridView.getChildCount()>mSelectedPosition){
					View view = mGridView.getChildAt(mSelectedPosition);
					mSelectedIconView = (ImageView)view.findViewById(R.id.main_icon);
				}
			}else{
				mCursorDrawable.setLevel(mLevel);
				mSelectedIconView.setBackgroundDrawable(mCursorDrawable);
				mLevel++;
				if(mLevel>MAX_LEVEL) mLevel=1;
			}
			if(!isStopped){
				mMainHandler.postDelayed(this, RUN_SPEED);
			}
		}
	};
	private void resetIconView(){
		if(mSelectedIconView!=null){
			mSelectedIconView.setBackgroundDrawable(null);
			mSelectedIconView = null;
		}
	}
	
	//创建功能列表 媒体共享--媒体中心--系统设置
	private List<FunctionItem> createFunctionList(){
		List<FunctionItem> itemList = new ArrayList<FunctionItem>();
		FunctionItem item = new FunctionItem();
		//DMS
		item.iconResID = R.drawable.main_dms;
		item.nameResID = R.string.dms_title;
		item.activity = MediaServer.class;
		itemList.add(item);
		//DMP
		item = new FunctionItem();
		item.iconResID = R.drawable.main_dmp;
		item.nameResID = R.string.dmp_title;
		item.activity = MediaPlayer.class;
		itemList.add(item);
		//Setting
		item = new FunctionItem();
		item.iconResID = R.drawable.main_setting;
		item.nameResID = R.string.settings_title;
		item.activity = MediaSettings.class;
		itemList.add(item);
		return itemList;
	}
	
	//Handler
	public final class MainHandler extends Handler {
		public void handleMessage(Message msg) {
			switch(msg.what){
			case MSG_HIDE_PROGRESS:
				mProgressBar.setVisibility(View.INVISIBLE);
				if(mDmsToast!=null){
					mDmsToast.cancel();
				}
				break;
			case MSG_SHOW_PROGRESS:
				mProgressBar.setVisibility(View.VISIBLE);
				break;
			}
		}
	}
	
	//功能项
	public final class FunctionItem {
		public boolean isSeleted;
		public int iconResID;
		public int nameResID;
		public Class<?> activity;
		
		public FunctionItem(){}
		
		public FunctionItem(int iconResID, int nameResID) {
			this.iconResID = iconResID;
			this.nameResID = nameResID;
		}
	}
	
	//功能项适配器
	public final class FunctionAdapter extends ArrayAdapter<FunctionItem> {

		private LayoutInflater mInflater;
		public FunctionAdapter(Context context, List<FunctionItem> objects) {
			super(context, 0, objects);
			mInflater = LayoutInflater.from(context);
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView==null){
				convertView = mInflater.inflate(R.layout.main_function_item, null);
			}
			convertView.setId(position);
			ImageView imageView = (ImageView)convertView.findViewById(R.id.main_icon);
			TextView textView = (TextView)convertView.findViewById(R.id.main_title);
			FunctionItem item = getItem(position);
			imageView.setImageResource(item.iconResID);
			textView.setText(item.nameResID);
			return convertView;
		}
		
	}

	/** 
	 * <p>Title: onConfigurationChanged</p> 
	 * <p>Description: </p> 
	 * @param newConfig 
	 * @see android.app.Activity#onConfigurationChanged(android.content.res.Configuration) 
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Locale locale = Locale.getDefault();
		isChinese = "zh".equals(locale.getLanguage());
	}

}
