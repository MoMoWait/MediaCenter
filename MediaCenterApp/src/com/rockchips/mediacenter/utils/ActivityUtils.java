/**
 * 
 */
package com.rockchips.mediacenter.utils;

import java.util.ArrayList;
import java.util.List;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.util.Log;
/**
 * @author GaoFei
 * Activity工具
 */
public class ActivityUtils {
    private static final String TAG = "ActivityUtils";
    
    /**
     * 获取所有Stack信息
     * @return
     */
    public static List<ActivityManager.StackInfo> getAllStackInfos(){
        List<ActivityManager.StackInfo> stackInfos = new ArrayList<ActivityManager.StackInfo>();
        try{
            stackInfos = ActivityManagerNative.getDefault().getAllStackInfos();
        }catch (Exception e){
            Log.i(TAG, "getAllStackInfos->exception:" + e);
        }
        //Log.i(TAG, "stackInfos:" + stackInfos);
        return stackInfos;
    }
    
    /**
     * 获取指定taskName对应的taskId列表
     * @param taskName
     * @return
     */
    public static List<Integer> getTaskIds(String taskName){
        Log.i(TAG, "getTaskIds->taskName:" + taskName);
        List<Integer> taskIds = new ArrayList<Integer>();
        List<ActivityManager.StackInfo> allStackInfos = getAllStackInfos();
        if(allStackInfos != null && allStackInfos.size() > 0){
            for(ActivityManager.StackInfo stackInfo : allStackInfos){
                int[] stackTaskIds = stackInfo.taskIds;
                String[] stackTaskNames = stackInfo.taskNames;
                for(int i = 0; i != stackTaskNames.length; ++i){
                    Log.i(TAG, "getTaskIds->stackTaskNames:" + stackTaskNames[i]);
                    if(stackTaskNames[i].equals(taskName)){
                        taskIds.add(stackTaskIds[i]);
                    }
                }
            }
        }
        return taskIds;
    }
    
    
    /**
     * 移除所有的Task列表
     * @param allTaskIds
     */
    public static void removeAllTask(List<Integer> allTaskIds){
        for(Integer itemTaskId : allTaskIds){
            try{
                ActivityManagerNative.getDefault().removeTask(itemTaskId);
            }catch (Exception e){
                Log.i(TAG, "removeAllTask->exception:" + e);
            }
        }
    }
    
}
