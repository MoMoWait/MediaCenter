package com.rockchips.mediacenter.util;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;

/**
 * @author GaoFei
 * Activity退出工具
 */
public class ActivityExitUtils {
	
	static List<Activity> mAllActivities = new ArrayList<Activity>();
	
	private ActivityExitUtils(){
		
	}
	
	public static List<Activity> getAllActivities(){
		return mAllActivities;
	}
	
	public static void clearActivities(){
		mAllActivities.clear();
	}
	
	
	public static void addActivity(Activity activity){
		mAllActivities.add(activity);
	}
	
	public static void removeActivity(Activity activity){
		if(mAllActivities.size() > 0)
			mAllActivities.remove(activity);
	}
	
	public static void removeAllActivities(){
		if(mAllActivities.size() > 0){
			for(int i = mAllActivities.size() - 1; i >= 0; --i){
				Activity itemActivity = mAllActivities.get(i);
				if(itemActivity != null)
					itemActivity.finish();
			}
		}
	}
}
