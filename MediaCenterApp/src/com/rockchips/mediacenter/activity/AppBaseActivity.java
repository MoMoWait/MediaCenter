package com.rockchips.mediacenter.activity;

import java.util.List;
import momo.cn.edu.fjnu.androidutils.utils.ToastUtils;
import com.rockchips.mediacenter.bean.Device;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.service.DeviceMonitorService;
import com.rockchips.mediacenter.utils.ActivityExitUtils;
import com.rockchips.mediacenter.utils.DialogUtils;
import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import com.rockchips.mediacenter.R;
/**
 * @author GaoFei
 * App基本Activity
 */
public abstract class AppBaseActivity extends Activity{
	private static final String TAG = "AppBaseActivity";
	private DeviceUpDownReceiver mDeviceUpDownReceiver;
	private Device mCurrDevice;
    /**
     * 定时器处理
     */
    private Handler mTimeHandler;
    /**
     * 定时器任务
     */
    private TimerTask mTimerTask;
    /**
     * 是否超时
     */
    private boolean mIsOverTime;
    /**
     * 设备监听服务
     */
    protected DeviceMonitorService mDeviceMonitorService;
    private ServiceConnection mDeviceMonitorConnection;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActivityExitUtils.addActivity(this);
		initData();
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		initBaseEvent();
	}
	
	
	private void initData(){
		mCurrDevice = (Device)getIntent().getSerializableExtra(ConstData.IntentKey.EXTRAL_LOCAL_DEVICE);
		mDeviceUpDownReceiver = new DeviceUpDownReceiver();
		mTimeHandler = new Handler();
		mTimerTask = new TimerTask();
		mDeviceMonitorConnection = new ServiceConnection() {
			
			@Override
			public void onServiceDisconnected(ComponentName name) {
				
			}
			
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.i(TAG, "AppBaseActivity->serviceConnection on ServiceConnected" );
				DeviceMonitorService.MonitorBinder serviceBinder = (DeviceMonitorService.MonitorBinder)service;
				mDeviceMonitorService = serviceBinder.getMonitorService();
				AppBaseActivity.this.onServiceConnected();
			}
		};
		//attachServices();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(getClass() != MainActivity.class){
			IntentFilter deviceUpDownFilter = new IntentFilter();
			//注册设备下线广播
			deviceUpDownFilter.addAction(ConstData.BroadCastMsg.DEVICE_DOWN);
			LocalBroadcastManager.getInstance(this).registerReceiver(mDeviceUpDownReceiver, deviceUpDownFilter);
		}
	}
	
	
	@Override
	protected void onPause() {
		super.onPause();
		if(getClass() != MainActivity.class){
			LocalBroadcastManager.getInstance(this).unregisterReceiver(mDeviceUpDownReceiver);
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unBindServices();
		ActivityExitUtils.removeActivity(this);
	}
	
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		hideNavigationBar();
	}
	
	/**
	 * 隐藏NavigationBar
	 */
	public void hideNavigationBar(){
		View decorView = getWindow().getDecorView();
		int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
		              | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
		decorView.setSystemUiVisibility(uiOptions);
	}
	
	
	public void initBaseEvent(){
		View decorView = getWindow().getDecorView();
		decorView.setOnSystemUiVisibilityChangeListener
		        (new View.OnSystemUiVisibilityChangeListener() {
		    @Override
		    public void onSystemUiVisibilityChange(int visibility) {
		        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
		        	hideNavigationBar();
		        } else {
		            
		        }
		    }
		});
	}
	
	 /**
     * 绑定各种服务
     */
    protected void attachServices(){
    	Intent intent = new Intent(this, DeviceMonitorService.class);
    	//绑定设备监听服务
    	bindService(intent, mDeviceMonitorConnection, Service.BIND_AUTO_CREATE);
    }
	
    
    /**
     * 解除服务绑定
     * @author GaoFei
     *
     */
    
    public void unBindServices(){
    	unbindService(mDeviceMonitorConnection);
    }
    
    /**
     * 服务连接回调
     */
    public abstract void onServiceConnected();
    
	/**
	 * 启动定时器
	 * @param time 处罚时间
	 */
	public void startTimer(long time){
	    mIsOverTime = false;
	    mTimeHandler.postDelayed(mTimerTask, time);
	}
	
	/**
	 * 结束定时器
	 */
	public void endTimer(){
	    mTimeHandler.removeCallbacks(mTimerTask);
	}
	
	/**
	 * 定时器回调方法
	 */
	public void onTimerArrive(){
	    mIsOverTime = true;
	    DialogUtils.closeLoadingDialog();
	    ToastUtils.showToast(getString(R.string.load_over_time));
	}
	
	/**
	 * 是否超时
	 * @return
	 */
	public boolean isOverTimer(){
	    return mIsOverTime;
	}
	
	class DeviceUpDownReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			String mountPath = intent.getStringExtra(ConstData.DeviceMountMsg.MOUNT_PATH);
			if(mCurrDevice != null && mCurrDevice.getLocalMountPath().equals(mountPath)){
				//退出Activity
				List<Activity> allActivities = ActivityExitUtils.getAllActivities();
				if(allActivities != null && allActivities.size() > 0){
					for(int i = allActivities.size() - 1; i >= 0; --i){
						Activity itemActivity = allActivities.get(i);
						if(itemActivity != null && itemActivity.getClass() != MainActivity.class){
							itemActivity.finish();
						}
					}
				}
			}
		}
	}
	
	/**
     * 定时器任务
     * @author GaoFei
     *
     */
    class TimerTask implements Runnable{
        @Override
        public void run() {
           onTimerArrive();
        }
    }
}
