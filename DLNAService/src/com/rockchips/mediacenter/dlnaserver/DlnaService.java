/**
 * Title: DlnaService.java<br>
 * Package: com.rockchips.mediacenter.dlnaserver<br>
 * Description: TODO<br>
 * @author c00224451
 * @version v1.0<br>
 * Date: 2014-7-28上午10:51:07<br> 
 * Copyright © Huawei Technologies Co., Ltd. 2014. All rights reserved.
 */

package com.rockchips.mediacenter.dlnaserver;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;

import com.rockchips.android.airsharing.api.ConnectionListener;
import com.rockchips.android.airsharing.api.HwServer;
import com.rockchips.android.airsharing.client.MediaBrowserClient;
import com.rockchips.android.airsharing.client.PlayerClient;
import com.rockchips.android.airsharing.constant.Constant;
import com.rockchips.android.airsharing.listener.IDlnaDmcCallbackListener;
import com.rockchips.android.airsharing.util.UtilMethodReflection;
import com.rockchips.mediacenter.basicutils.constant.Constant.BroadcastMsg;
import com.rockchips.mediacenter.basicutils.constant.Constant.ShareState;
import com.rockchips.mediacenter.basicutils.constant.DeviceDataConst;
import com.rockchips.mediacenter.basicutils.util.IICLOG;
import com.rockchips.mediacenter.basicutils.util.MountIntentUtil;
import com.rockchips.mediacenter.basicutils.util.PlatformUtil;
import com.rockchips.mediacenter.aidl.IDlnaFileShareService;
import com.rockchips.mediacenter.dlnaserver.db.FileShareHelper;
import com.rockchips.mediacenter.dlnaserver.sink.DlnaUniswitch;
import com.rockchips.mediacenter.dlnaserver.utils.DeviceNameUtils;
import com.rockchips.mediacenter.dlnaserver.utils.HideMethod;

/**
 * Description: TODO<br>
 * @author c00224451
 * @version v1.0 Date: 2014-7-28 上午10:51:07<br>
 */

public class DlnaService extends Service
{
    private static final IICLOG Log = IICLOG.getInstance();

    private static final String TAG = "DlnaService";

    private MediaBrowserClient mMediaBrowserClient;

    private boolean mIsMediaBrowserStartSuccess;

    private static final int DEFAULT_CACHE_SIZE = 100 * 1024 * 1024;

    private String mDefaultCachePath;

    private AidlDlnaFileShareBinder mAidlDlnaFileShare;
    
//    private static final String DEFAULT_DEVICE_NAME_DMS = "MediaCenterDMS";
    private boolean mIsRunning;
    private DevicesMountThread mDevicesMountThread;    
    
    /**************************新增推送逻辑****************************/
    private static final String DEFAULT_DEVICE_NAME_SINK = "MediaCenterSink";
    private boolean mIsSinkInitSuccess;
    private boolean mIsSinkStartServer;
    private PlayerClient mPlayerClient;    
    private boolean mIsApEnabled;
    private WifiManager mWifiManager;
    private String mWifiApStateChangeAction;
    private String mExtraWifiApState;
    private int mWiFiApStateEnabled;
    private static final int AP_READY_WAIT_TIME = 500;
    private static final int AP_READY_WAIT_NUM_MAX = 20;    
    private int mAPEnableCount;
    
    private DlnaUniswitch mDlnaUniswitch;
    
    private static final int ADD_SHARE = 0;
    private static final int CANCEL_SHARE = 1;
    
    private static final String SHARE_ALL_FILES = "mediaos://SHARE_ALL_FILES";
    private static final String USER_SHARE_OPERATE = "mediaos://USER_SHARE_OPERATE";
    
    private ArrayList<FileSharePair> mSharingTaskList;
    private Object mShareStateLock = new Object();
    @Override
    public void onCreate()
    {
        super.onCreate();
        
        android.util.Log.i(TAG, "onCreate start");
        
        mSharingTaskList = new ArrayList<FileSharePair>();
        
        mIsRunning = true;
        
        mMediaBrowserClient = new MediaBrowserClient.Builder(getBaseContext()).build(mConnectionListener);
        
        mDevicesMountThread = new DevicesMountThread();        
        //设置缓存目录
        setCachePath();
        init();
        
        /*****************初始化dlna推送对象*******************/
        mDlnaUniswitch = DlnaUniswitch.getInstance(this);
        mPlayerClient = PlayerClient.getInstance();               
        getHideFiledsValue();
        mIsSinkInitSuccess = false;
        mIsSinkStartServer = false;
        registerConnectivityActionBroadcast();
        registerApConnectivityBroadcast();
        initSink();
        if (isReadyForStartServer())
        {
            startSinkServer();
        }
        
        android.util.Log.i(TAG, "onCreate end");
    }

    @Override
    public void onDestroy()
    {
        mMediaBrowserClient.removeDlnaDmcCallbackListener(mIDlnaDmcCallbackListener);
        mIsRunning = false;
        stopServer();
        deinit();  
        /*****************去初始化dlna推送对象*****************/
        stopSinkServer();
        deinitSink();
        unregisterConnectivityActionBroadcast();
        unregisterApConnectivityBroadcast();
        mDlnaUniswitch.onDestroy();        
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent arg0)
    {
    	android.util.Log.i(TAG, "onBind start");
        if (null == mAidlDlnaFileShare)
        {
            mAidlDlnaFileShare = new AidlDlnaFileShareBinder();
        }
        android.util.Log.i(TAG, "onBind end");
        return mAidlDlnaFileShare;
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        mAidlDlnaFileShare = null;
        return super.onUnbind(intent);
    }
    
    
    private void setParam()
    {
        boolean ret = mMediaBrowserClient.setParams(DEFAULT_CACHE_SIZE, mDefaultCachePath);
        Log.d(TAG, "DMC   .setParams ret = " + ret);
    }

    private void init()
    {
        Log.d(TAG, "cc msg init");
        mMediaBrowserClient.init();
    }

    private void deinit()
    {
        mMediaBrowserClient.deInit();
    }

    private void startServer()
    {
        mIsMediaBrowserStartSuccess = mMediaBrowserClient.startServer(getSinkDeviceName());
        Log.d(TAG, "cc msg start DMC result = " + mIsMediaBrowserStartSuccess);
        if (mIsMediaBrowserStartSuccess)
        {
            mSettingsObserver.register(getApplicationContext());
            registerSettingBroadcastReceiver();
        }
    }
    
    private void stopServer()
    {
        if (mIsMediaBrowserStartSuccess)
        {
            unregisterSettingBroadcastReceiver();
            mSettingsObserver.unregister(getApplicationContext());
            mMediaBrowserClient.stopServer();
        }
        Log.d(TAG, "cc msg stop stopServer result OUT");
    }

    private void setCachePath()
    {
        mDefaultCachePath =  "/data/data/com.rockchips.android.airsharing/databases/";
    }

    private class AidlDlnaFileShareBinder extends IDlnaFileShareService.Stub
    {

        @Override
        public int getFolderShareStatus(String folderName, String mountPath) throws RemoteException
        {   
        	android.util.Log.i(TAG, "AidlDlnaFileShareBinder->getFolderShareStatus");
        	
            int state = FileShareManager.getInstance(DlnaService.this).getFolderShareStatus(folderName, mountPath);
            
            boolean isSharing = isSharingState(folderName);
            if (isSharing)
            {
                if (state == ShareState.SHARE_STATE_ENABLE)
                {
                    state = ShareState.SHARE_STATE_SHARING;
                }
                else if (state == ShareState.SHARE_STATE_SHARED)
                {
                    state = ShareState.SHARE_STATE_CANCEL_SHARING;
                } 
            }   
            return state;
        }

        @Override
        public boolean isParentShared(String filePath, String mountPath) throws RemoteException
        {
        	android.util.Log.i(TAG, "AidlDlnaFileShareBinder->isParentShared");
            Log.d(TAG, "cc msg isParentShared filePath = " + filePath);
            return FileShareManager.getInstance(DlnaService.this).isParentShared(filePath, mountPath);
        }

        @Override
        public boolean cancelSharedFile(List<String> filePathList, String mountPath) throws RemoteException
        {   
        	android.util.Log.i(TAG, "AidlDlnaFileShareBinder->cancelSharedFile");
            addCancelShareTask(filePathList, mountPath);            
            return true;
        }

        @Override
        public boolean addSharedFile(List<String> filePathList, boolean isFolder, String mountPath) throws RemoteException
        {
        	android.util.Log.i(TAG, "AidlDlnaFileShareBinder->addSharedFile");
            Log.d(TAG, "cc msg addSharedFile mountPath = " + mountPath);
            addShareTask(filePathList, isFolder, mountPath);                        
            return true;                
        }

        @Override
        public boolean deleteSharedFile(List<String> filePathList, boolean isFolder, String mountPath) throws RemoteException
        {            
        	android.util.Log.i(TAG, "AidlDlnaFileShareBinder->deleteSharedFile");
            Log.d(TAG, "cc msg deleteSharedFile mountPath = " + mountPath);
            boolean ret = deleteSharePath(filePathList, CANCEL_SHARE, isFolder);            
            FileShareManager.getInstance(DlnaService.this).cancelSharedFile(filePathList, mountPath);
            return ret;
            
        }

        @Override
        public boolean deleteAllSharedFiles(String folderPath, String mountPath) throws RemoteException
        {            
        	android.util.Log.i(TAG, "AidlDlnaFileShareBinder->deleteAllSharedFiles");
            Cursor cursor = FileShareManager.getInstance(DlnaService.this).getAllShareFilesByFolder(mountPath, folderPath);
            if (cursor == null)
            {
                return true;
            }
            
            deleteAllFiles(cursor, mountPath, true);
			cursor.close();
            return true;            
        }
    }

    public boolean deleteSharePath(List<String> filePathList, int shareFlag, boolean isFolder)
    {
        boolean ret = false;
        Log.e(TAG, "cc msg deleteSharePath in");
        if (null == mMediaBrowserClient)
        {
            Log.e(TAG, "cc msg deleteSharePath mMediaBrowserClient == null!");
            return ret;
        }
        ret = mMediaBrowserClient.updateShareFilesPathWithDirFlag(filePathList, shareFlag, isFolder);
        Log.e(TAG, "cc msg deleteSharePath shareFlag = " + shareFlag + " ret = " + ret);
        return ret;
    }
    
    public boolean updateShareFilesPath(List<String> filePathList, int shareFlag)
    {
        boolean ret = false;
        Log.d(TAG, "cc msg updateShareFilesPath in");
        if (null == mMediaBrowserClient)
        {
            Log.e(TAG, "cc msg updateShareFilesPath mMediaBrowserClient == null!");
            return ret;
        }
        ret = mMediaBrowserClient.updateShareFilesPath(filePathList, shareFlag);
        Log.d(TAG, "cc msg updateShareFilesPath shareFlag = " + shareFlag + " ret = " + ret);
        return ret;
    }

    private ConnectionListener mConnectionListener = new ConnectionListener()
    {

        @Override
        public void onSuccess()
        {
            Log.d(TAG, "onSuccess     E");
            mMediaBrowserClient.addDlnaDmcCallbackListener(mIDlnaDmcCallbackListener);
            setParam();
            startServer();
            Log.d(TAG, "onSuccess     X");
        }

        @Override
        public void onFail()
        {
            Log.d(TAG, "onFail     E");
            mMediaBrowserClient.deInit();
            Log.e(TAG, "1 ============> mIsMediaBrowserStartSuccess = " + mIsMediaBrowserStartSuccess);
            if (mIsMediaBrowserStartSuccess)
            {
                Log.d(TAG, "1 --- Media Browser On Fail Restart E");
                mIsMediaBrowserStartSuccess = false;
                init();
            }
            Log.e(TAG, "2 ============> mIsSinkInitSuccess = " + mIsSinkInitSuccess);
            if (mIsSinkInitSuccess)
            {
                Log.d(TAG, "2 --- Play Client On Fail Restart E");
                mIsSinkInitSuccess = false;
                mIsSinkStartServer = false;
                deinitSink();
                initSink();
                if (isReadyForStartServer())
                {
                    startSinkServer();
                }  
                
            }
            Log.d(TAG, "onFail     X");
        }
    };
    
    class DevicesMountThread extends Thread
    {        
        @Override
        public void run()
        {
            while (mIsRunning)
            {                               
                // 获取一个设备上下线消息
                Intent intent = DevicesMountQueue.getInstance().dequeue();            
                // 发生异常，则退出
                if (intent == null)
                {
                    Log.d(TAG, "intent == null!");                    
                    continue;
                }                    
                if (!isNetWorkReady())
                {
                    Log.d(TAG, "isNetWorkReady no ok!");
                    continue;
                }                           
                // 处理Intent消息，解析后入库
                dealWithIntentMsg(intent);
            }            
        }        
    }
    
    private void dealWithIntentMsg(Intent intent)
    {
        String uDirPath = intent.getDataString();
        if (uDirPath != null && uDirPath.startsWith(DeviceDataConst.FILE_PROTOCOL)
                && uDirPath.length() > DeviceDataConst.FILE_PROTOCOL.length())
        {
            uDirPath = uDirPath.substring(DeviceDataConst.FILE_PROTOCOL.length()).trim();
        }        
        
        Cursor cursor = null;
           
        if (isUserOperateIntentMsg(uDirPath))
        {
            handleUserFileShare();
            return;
        }
        else if (isCustomizeIntentMsg(uDirPath))
        {
            cursor = FileShareManager.getInstance(DlnaService.this).getShareFiles();
        }
        else
        {
            cursor = FileShareManager.getInstance(DlnaService.this).getShareFilesByMountpath(uDirPath);
        }

        List<String> filePathList = new ArrayList<String>();        

        if (MountIntentUtil.isDeviceMountIntent(intent))
        {
            if (null != cursor)
            {
                while (cursor.moveToNext())
                {
                    String key = cursor.getString(cursor.getColumnIndex(FileShareHelper.FILE_PATH));
                    File file = new File(key);
                    if (file.exists())
                    {
                        filePathList.add(key);
                        Log.d(TAG, "cc msg add share path = " + key);
                    }

                }
                if (0 != filePathList.size())
                {
                    // 增加所有共享
                    updateShareFilesPath(filePathList, 0);
                }
            }
        }
        else if (MountIntentUtil.isDeviceUnmountIntent(intent))
        {            
            if (null == cursor)
            {
                return;
            }                           
            deleteAllFiles(cursor, uDirPath, false);
        }
        if (cursor != null)
        {
            cursor.close();
        }        
    }
    
    private void deleteAllFiles(Cursor cursor, String mountPath, boolean isDeleteFromShareDb)
    {
        List<String> folderPathList = new ArrayList<String>();
        List<String> filePathList = new ArrayList<String>();
        while (cursor.moveToNext())
        {
            String key = cursor.getString(cursor.getColumnIndex(FileShareHelper.FILE_PATH));
            int isFolder = cursor.getInt(cursor.getColumnIndex(FileShareHelper.IS_FOLDER));
            Log.d(TAG, "cc msg remove share path = " + key + " isFolder = " + isFolder);                    
            if (isFolder == FileShareManager.FOLDER_TYPE)
            {
                folderPathList.add(key);
            }
            else
            {
                filePathList.add(key);
            }                    
        }   
        
        if (0 != filePathList.size())
        {
            // 取消所有共享文件
            deleteSharePath(filePathList, CANCEL_SHARE, false);
            if (isDeleteFromShareDb)
            {
                FileShareManager.getInstance(DlnaService.this).cancelSharedFile(filePathList, mountPath);
            }
        }
        if (0 != folderPathList.size())
        {
            // 取消所有共享目录
            deleteSharePath(folderPathList, CANCEL_SHARE, true);
            if (isDeleteFromShareDb)
            {
                FileShareManager.getInstance(DlnaService.this).cancelSharedFile(folderPathList, mountPath);
            }
        }
    }
    
    /*****************************新增dlna推送逻辑**************************/       
    private boolean initSink()
    {
        mIsSinkInitSuccess = mPlayerClient.init(this);
        Log.d(TAG, "cc msg init mPlayerClient.init " + mIsSinkInitSuccess);
        mPlayerClient.setHwSharingListener(mDlnaUniswitch.getEventListener());
        return mIsSinkInitSuccess;
    }

    private void deinitSink()
    {
        mPlayerClient.clsHwSharingListener(mDlnaUniswitch.getEventListener());
        mPlayerClient.deInit();
    }

    private void startSinkServer()
    {           
        if (PlatformUtil.supportHisiMediaPlayerOnJava())
        {
            mIsSinkStartServer = mPlayerClient.startServer(getSinkDeviceName(), Constant.PLAYER_SINK_TYPE_ALL);
        }
        else
        {
            mIsSinkStartServer = mPlayerClient.startServer(getSinkDeviceName(), Constant.PLAYER_SINK_TYPE_DLNA_DMR);
        }
        Log.d(TAG, "cc msg startServer: mIsStartServer : " + mIsSinkStartServer);
    }

    private void stopSinkServer()
    { 
        mPlayerClient.stopServer();
    }

    private String getSinkDeviceName()
    {
        DeviceNameUtils deviceNameUtils = new DeviceNameUtils();
        String name = deviceNameUtils.getDeviceName(getApplicationContext(), DEFAULT_DEVICE_NAME_SINK);
        return name;
    }
    private boolean isReadyForStartServer()
    {
        if (mIsSinkInitSuccess && isNetWorkReady() && !mIsSinkStartServer)
        {
            return true;
        }
        return false;
    }
    
    private void getHideFiledsValue()
    {
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mWifiApStateChangeAction = HideMethod.getStringValue(mWifiManager, HideMethod.TAG_WIFI_AP_STATE_CHANGED_ACTION);
        mExtraWifiApState = HideMethod.getStringValue(mWifiManager, HideMethod.TAG_EXTRA_WIFI_AP_STATE);
        mWiFiApStateEnabled = HideMethod.getIntValue(mWifiManager, HideMethod.TAG_WIFI_AP_STATE_ENABLED);
    }
    
    private void registerApConnectivityBroadcast()
    {
        IntentFilter filterAP = new IntentFilter();
        filterAP.addAction(mWifiApStateChangeAction);
        registerReceiver(mAPConnectivityBroadcastReceiver, filterAP);
    }
    
    private void unregisterApConnectivityBroadcast()
    {
        unregisterReceiver(mAPConnectivityBroadcastReceiver);
    }
    
    private void registerConnectivityActionBroadcast()
    {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mConnectionChangeReceiver, filter);
    }

    private void unregisterConnectivityActionBroadcast()
    {
        unregisterReceiver(mConnectionChangeReceiver);
    }

    private BroadcastReceiver mConnectionChangeReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            onHandleConnectReceive(context, action);
        }
    };

    private void onHandleConnectReceive(Context context, String action)
    {
        if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION))
        {
            Log.d(TAG, "cc msg BroadcastReceiver CONNECTIVITY_ACTION!");
            onConnectivityChangedAction();
        }
    }
    
    private void onConnectivityChangedAction()
    {
        //网络可达，共享所有文件        
        if (!mIsSinkInitSuccess && isNetWorkReady())
        {
            initSink();
            Log.d(TAG, "cc msg onConnectivityChangedAction init()!");
        }
        if (isReadyForStartServer())
        {
            Log.d(TAG, "cc msg onConnectivityChangedAction startServer()!");
            startSinkServer();
        }
    }

    private boolean isNetWorkReady()
    {
        return isNetWorkConnected() || mIsApEnabled;
    }
    
    private boolean isNetWorkConnected()
    {
        ConnectivityManager connectivityManager;
        NetworkInfo info;

        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (null == connectivityManager)
        {
            return false;
        }
        info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable())
        {
            Log.d(TAG, "cc msg isNetWorkConnected info.getTypeName():" + info.getTypeName());
            return true;
        }
        else
        {
            Log.d(TAG, "cc msg isNetWorkConnected no network");
            return false;
        }
    }    
    private BroadcastReceiver  mAPConnectivityBroadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (mWifiApStateChangeAction.equals(action))
            {
                Log.d(TAG, "cc msg Ap BroadcastReceiver TAG_WIFI_AP_STATE_CHANGED_ACTION!");
                int apState = intent.getIntExtra(mExtraWifiApState, Integer.MIN_VALUE);

                if (apState == mWiFiApStateEnabled)
                { 
                    mAPEnabledHandler.removeMessages(0);
                    mAPEnableCount = 0;
                    mAPEnabledHandler.sendEmptyMessage(0);
                } 
                else
                {
                    mIsApEnabled = false;
                    mAPEnabledHandler.removeMessages(0);
                    mAPEnableCount = 0;
                }
            }
        }
    };
    
    private boolean getAPTethered()
    {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        String[] tethered = getTetheredIfaces(connMgr);
        if (tethered != null && tethered.length > 0)
        {
            return true;
        }
        return false;
    }

    private String[] getTetheredIfaces(ConnectivityManager mConnManager)
    {
        Method method = null;
        try
        {
            method = ConnectivityManager.class.getDeclaredMethod("getTetheredIfaces");
        }
        catch (NoSuchMethodException e)
        {
            return null;
        }

        Object object = UtilMethodReflection.invokeMethod(mConnManager, method);
        if (null != object)
        {
            return (String[]) object;
        }

        return null;
    }
    
    private Handler mAPEnabledHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            
            if (msg.what == 0)
            {
                if (getAPTethered())
                {
                    Log.d(TAG, "cc msg Ap getApTethered true!");
                    mIsApEnabled = true;
                    onConnectivityChangedAction();
                    mAPEnabledHandler.removeMessages(0);
                    mAPEnableCount = 0;
                }
                else
                {
                    Log.d(TAG, "cc msg Ap getApTethered false!");
                    if (mAPEnableCount < AP_READY_WAIT_NUM_MAX)
                    {
                        mAPEnableCount++;
                        mAPEnabledHandler.sendEmptyMessageDelayed(0, AP_READY_WAIT_TIME);
                    }
                }
            }
        }
    };
    
    
    private IDlnaDmcCallbackListener.Stub mIDlnaDmcCallbackListener = new IDlnaDmcCallbackListener.Stub()
    {

//        private static final int DLNA_DEV_UP = 0;
//        
        private static final int DLNA_SELF_UP = 2; //注:这个值不可以修改 ，设备自身dlna启动成功后回调的值.
//
//        private static final int DLNA_DEV_DOWN = 1;
//        
//        private static final int DLNA_SELF_DOWN = 3;
        
        @Override
        public void onDeviceChangeEvent(int eventId, List<HwServer> devList) throws RemoteException
        {
            Log.d("TAG", "mIDlnaDmcCallbackListener onDeviceChangeEvent  Enter eventId= " + eventId + ", devList size= " + devList.size());
            if (eventId == DLNA_SELF_UP && mIsMediaBrowserStartSuccess)
            {
                if (!mDevicesMountThread.isAlive())
                {
                    mDevicesMountThread.start();
                }
                Log.d(TAG, "SEND FROM DLNA_RESTART_SUCCESS_ACTION");
                Intent shareAllFilesIntent= new Intent("DLNA_RESTART_SUCCESS_ACTION");
                sendBroadcast(shareAllFilesIntent, com.rockchips.mediacenter.basicutils.constant.Constant.MEDIACENTER_PERMISSION);
                /* End:  Added by c00224451 at 2014-12-15 DTS:DTS2014101802518 */
                sendMessageToShareAllFiles();
            }
        }

        @Override
        public void onMediaChangeEvent(int devId, int mediaType) throws RemoteException
        {
            Log.d("TAG", "mIDlnaDmcCallbackListener onMediaChangeEvent  Enter devId = " + devId + ", mediaType = " + mediaType);

        }
    };
    
    //=====================开始监听设备名称变化的模块==============================//
    private SettingsObserver mSettingsObserver = new SettingsObserver(null);
    
    private class SettingsObserver extends ContentObserver
    {
        public SettingsObserver(Handler handler)
        {
            super(handler);
        }

        public void register(Context context)
        {
            ContentResolver cr = context.getContentResolver();
            Uri uri = null;

            try
            {
                uri = Uri.parse("content://settings/stbconfig/stb_device_name");
                cr.registerContentObserver(uri, false, this);
            }
            catch (NullPointerException e)
            {
            }
            catch (IllegalArgumentException e)
            {
            }

            try
            {
                uri = Uri.parse("content://stbconfig/stbconfig/stb_device_name");
                cr.registerContentObserver(uri, false, this);
            }
            catch (NullPointerException e)
            {
            }
            catch (IllegalArgumentException e)
            {
            }
        }
        
        public void unregister(Context context)
        {
            ContentResolver cr = context.getContentResolver();
            cr.unregisterContentObserver(this);
        }
        
        public void onChange(boolean selfChange)
        {
            super.onChange(selfChange);
            mMediaBrowserClient.updateDeviceName(getSinkDeviceName());
        }
    }
    
    private static final String DEIVCE_NAME_CHANGE_BROADCAST = "com.hisi.devicename.changed";
    
    private class SettingBroadcastReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (null == intent)
            {
                return;
            }
            String action = intent.getAction();
            if (DEIVCE_NAME_CHANGE_BROADCAST.equals(action))
            {
                mMediaBrowserClient.updateDeviceName(getSinkDeviceName());
            }
        }
    }
    
    private SettingBroadcastReceiver mSettingBroadcastReceiver;
    
    private void registerSettingBroadcastReceiver()
    {
        if (null == mSettingBroadcastReceiver)
        {
            mSettingBroadcastReceiver = new SettingBroadcastReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(DEIVCE_NAME_CHANGE_BROADCAST);
            getBaseContext().registerReceiver(mSettingBroadcastReceiver, intentFilter);
        }
    }
    
    private void unregisterSettingBroadcastReceiver()
    {
        if (null != mSettingBroadcastReceiver)
        {
            getBaseContext().unregisterReceiver(mSettingBroadcastReceiver);
            mSettingBroadcastReceiver = null;
        }
    }
    
    private void sendMessageToShareAllFiles()
    {
        Intent intent = new Intent();        
        intent.setData(Uri.parse(SHARE_ALL_FILES));
        intent.setAction(Intent.ACTION_MEDIA_MOUNTED);
        DevicesMountQueue.getInstance().enqueue(intent);
    }
    
    private boolean isCustomizeIntentMsg(String data)
    {        
        if (SHARE_ALL_FILES.equals(data))
        {
            return true;
        }
        return false;
    }
    
    private boolean isUserOperateIntentMsg(String data)
    {
        if (USER_SHARE_OPERATE.equals(data))
        {
            return true;
        }
        return false;
    }
    
    private void sendMessageForUserOperate()
    {
        Intent intent = new Intent();        
        intent.setData(Uri.parse(USER_SHARE_OPERATE)); 
        intent.setAction(Intent.ACTION_MEDIA_MOUNTED);
        DevicesMountQueue.getInstance().enqueue(intent);
    }
    
    private void addCancelShareTask(List<String> taskList, String mountPath)
    {
        if (taskList == null || taskList.size() == 0)
        {
            return;
        }        
        synchronized(mShareStateLock)
        {
            FileSharePair sharePair = new FileSharePair();
            sharePair.filePathList = taskList;
            sharePair.shareState = CANCEL_SHARE;
            sharePair.mountPath = mountPath;
            mSharingTaskList.add(sharePair);
            sendMessageForUserOperate();
        }        
    }
    
    private void addShareTask(List<String> taskList, boolean isFolder, String mountPath)
    {
        if (taskList == null || taskList.size() == 0)
        {
            return;
        }        
        synchronized(mShareStateLock)
        {
            FileSharePair sharePair = new FileSharePair();
            sharePair.filePathList = taskList;
            sharePair.shareState = ADD_SHARE;
            sharePair.isFolder = isFolder;
            sharePair.mountPath = mountPath;
            mSharingTaskList.add(sharePair);
            sendMessageForUserOperate();
        }        
    }
    
    private FileSharePair getTopSharingTask()
    {
        FileSharePair fileSharePair = null;
        synchronized(mShareStateLock)
        {
            if (mSharingTaskList.size() != 0)
            {
                fileSharePair = mSharingTaskList.get(0);
            }            
        }
        return fileSharePair;
    }
    
    private void removeTopSharingTask()
    {
        synchronized(mShareStateLock)
        {
            if (mSharingTaskList.size() != 0)
            {
                mSharingTaskList.remove(0);
            }            
        }
    }
        
    
    private boolean isSharingState(String path)
    {
        synchronized(mShareStateLock)
        {
            for (int i = 0; i < mSharingTaskList.size(); ++i)
            {
                FileSharePair sharePair = mSharingTaskList.get(i);
                List<String> pathList = sharePair.filePathList;
                if (pathList != null && pathList.contains(path))
                {
                    return true;
                }
            }            
            return false;
        }
    }
    
    private class FileSharePair
    {
        public List<String> filePathList;
        public int shareState;
        public boolean isFolder;
        public String mountPath;        
    }
    
    private void handleUserFileShare()
    {
        while (true)
        {
            FileSharePair fileSharePair = getTopSharingTask();
            if (fileSharePair == null)
            {
                return;
            }
            
            List<String> filePathList = fileSharePair.filePathList;
            String mountPath = fileSharePair.mountPath;
            Intent intent = new Intent();
            if (fileSharePair.shareState == ADD_SHARE)
            {
                boolean isFolder = fileSharePair.isFolder;
                Log.d(TAG, "cc msg handleUserFileShare addShare mountPath = " + mountPath + " isFolder = " + isFolder);
                boolean ret = updateShareFilesPath(filePathList, ADD_SHARE);                
                if (ret)
                {
                    FileShareManager.getInstance(DlnaService.this).addSharedFile(filePathList, isFolder, mountPath);
                }
                intent.setAction(BroadcastMsg.ACTION_ADD_FILE_SHARE_RESULT);
                intent.putExtra(BroadcastMsg.EXTRA_FILE_SHARE_RESULT, ret);
                sendBroadcast(intent);
                
            }
            else if (fileSharePair.shareState == CANCEL_SHARE)
            {
                Log.d(TAG, "cc msg handleUserFileShare cancelShare mountPath = " + mountPath);
                boolean ret = updateShareFilesPath(filePathList, CANCEL_SHARE);
                if (ret)
                {
                    FileShareManager.getInstance(DlnaService.this).cancelSharedFile(filePathList, mountPath);
                }
                intent.setAction(BroadcastMsg.ACTION_CANCEL_FILE_SHARE_RESULT);
                intent.putExtra(BroadcastMsg.EXTRA_FILE_SHARE_RESULT, ret);
                sendBroadcast(intent);
            }
            removeTopSharingTask();
        }
        
    }
    
    
    //=====================结束监听设备名称变化的模块==============================//
}
