package com.rockchips.mediacenter.application;
import com.rockchips.mediacenter.utils.ActivityExitUtils;
import android.os.Process;
import android.util.Log;
/**
 * App奔溃处理器
 * @author GaoFei
 *
 */
public class AppCrashHandler implements Thread.UncaughtExceptionHandler{
	private static final String TAG = "AppCrashHandler";
	private static AppCrashHandler mCrashHandler;
	
	private AppCrashHandler(){
		
	}
	
	public static AppCrashHandler getInstace(){
		if(mCrashHandler == null)
			mCrashHandler = new AppCrashHandler();
		return mCrashHandler;
	}
	
	@Override
	public void uncaughtException(Thread t, Throwable e) {
		handleUncaughtException(t, e);
	}
	
	private void handleUncaughtException(Thread t, Throwable e){
		Log.e(TAG, "handleUncaughtException->exception:" + e);
		//关闭所有Activities
		 ActivityExitUtils.removeAllActivities();
		//重启应用
		/* Intent intent = new Intent(CommonValues.application, MainActivity.class);
		 intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
         PendingIntent restartIntent = PendingIntent.getActivity(CommonValues.application, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT); 
         AlarmManager mgr = (AlarmManager)CommonValues.application.getSystemService(Context.ALARM_SERVICE); 
         //1秒钟后重启应用 
         mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent);*/
         //关闭进程
        Process.killProcess(Process.myPid());
	}

}
