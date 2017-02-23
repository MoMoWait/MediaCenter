package com.rockchips.mediacenter.application;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.xutils.DbManager;
import org.xutils.x;

import momo.cn.edu.fjnu.androidutils.base.BaseApplication;
import momo.cn.edu.fjnu.androidutils.utils.StorageUtils;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.os.RemoteException;
import android.util.Log;
import com.rockchips.mediacenter.api.localImp.LocalDeviceManager;
import com.rockchips.mediacenter.basicutils.bean.LocalDeviceInfo;
import com.rockchips.mediacenter.basicutils.constant.Constant;
import com.rockchips.mediacenter.basicutils.util.IICLOG;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.service.DeviceMonitorService;
import com.rockchips.mediacenter.util.ActivityExitUtils;
import com.rockchips.mediacenter.util.MediaUtils;
public class MediaCenterApplication extends BaseApplication
{
    private static final String TAG = "MediaCenterApplication";
    
    private static final IICLOG LOG = IICLOG.getInstance();

    
    public static DbManager mDBManager;
    

    public void onCreate()
    {
        super.onCreate();
        
        //Log.i(TAG, "currentMediaPostion:" + MediaUtils.getCurrentPostion());
        
        try{
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            Log.i(TAG, "versionName:" + packageInfo.versionName);
        }catch (Exception e){
        	Log.i(TAG, "getVersionName->exception:" + e);
        }
        
        ActivityExitUtils.clearActivities();
        //初始化xutils3框架
        x.Ext.init(this);
        //传入Application实例给ObjectFactory
        //初始化服务
        initService();
        //初始化数据库
        initDB();
        //初始化数据
        initData();
    }

    @Override
    public void onLowMemory()
    {
        super.onLowMemory();
    }


    @Override
    public void onTerminate() {
    	super.onTerminate();
    	try{
    		mDBManager.close();
    	}catch (Exception e){
    		
    	}
    	
    }
    
    
    /**
     * 初始化数据库
     */
    private void initDB() {
        DbManager.DaoConfig dbConfig = new DbManager.DaoConfig().setDbDir(new File(ConstData.DB_DIRECTORY))
                .setDbName(ConstData.DB_NAME).setDbVersion(ConstData.DB_VERSION).setAllowTransaction(true)
                .setDbOpenListener(new DbManager.DbOpenListener() {
                    @Override
                    public void onDbOpened(DbManager db) {
                        // 开启WAL, 对写入加速提升巨大
                        db.getDatabase().enableWriteAheadLogging();
                    	LOG.i(TAG, "database->maxNumSize:" + db.getDatabase().getMaximumSize());
                    	LOG.i(TAG, "database->path:" + db.getDatabase().getPath());
                    }
                }).setDbUpgradeListener(null);
        if (null == mDBManager)
            mDBManager = x.getDb(dbConfig);
    }
    
    /**
     * 初始化服务
     */
    private void initService(){
    	Intent deviceMonitorIntent = new Intent(this, DeviceMonitorService.class);
    	startService(deviceMonitorIntent);
    }
    
    /**
     * 初始化数据
     */
    public void initData(){
    	//当前拷贝文件清空
    	StorageUtils.saveDataToSharedPreference(ConstData.SharedKey.COPY_FILE_PATH, "");
    	//当前剪切文件清空
    	StorageUtils.saveDataToSharedPreference(ConstData.SharedKey.MOVE_FILE_PATH, "");
    }

}
