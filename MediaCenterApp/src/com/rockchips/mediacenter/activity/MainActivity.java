package com.rockchips.mediacenter.activity;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.io.File;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;
import org.json.JSONArray;
import momo.cn.edu.fjnu.androidutils.utils.DeviceInfoUtils;
import momo.cn.edu.fjnu.androidutils.utils.JsonUtils;
import momo.cn.edu.fjnu.androidutils.utils.NetWorkUtils;
import momo.cn.edu.fjnu.androidutils.utils.ResourceUtils;
import momo.cn.edu.fjnu.androidutils.utils.StorageUtils;
import momo.cn.edu.fjnu.androidutils.utils.ToastUtils;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.SyncStateContract.Constants;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.rockchips.mediacenter.R;
import com.rockchips.mediacenter.basicutils.bean.LocalDeviceInfo;
import com.rockchips.mediacenter.basicutils.bean.LocalMediaInfo;
import com.rockchips.mediacenter.basicutils.constant.Constant;
import com.rockchips.mediacenter.basicutils.util.IICLOG;
import com.rockchips.mediacenter.audioplayer.ENUMLAYOUTDISPLAYTYPE;
import com.rockchips.mediacenter.bean.LocalDevice;
import com.rockchips.mediacenter.bean.NFSInfo;
import com.rockchips.mediacenter.bean.SmbInfo;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.modle.db.LocalDeviceService;
import com.rockchips.mediacenter.modle.db.LocalMediaFileService;
import com.rockchips.mediacenter.modle.db.LocalMediaFolderService;
import com.rockchips.mediacenter.modle.db.ScanDirectoryService;
import com.rockchips.mediacenter.service.DeviceMonitorService;
import com.rockchips.mediacenter.util.DialogUtils;
import com.rockchips.mediacenter.util.MediaFileUtils;
import com.rockchips.mediacenter.util.MediaUtils;
import com.rockchips.mediacenter.util.MountUtils;
import com.rockchips.mediacenter.util.ShellUtils;
import com.rockchips.mediacenter.utils.DeviceTypeStr;
import com.rockchips.mediacenter.view.LoadingDialog;
import com.rockchips.mediacenter.view.NFSAddDialog;
import com.rockchips.mediacenter.view.NetDeviceAddSelectDialog;
import com.rockchips.mediacenter.view.SambaAddDialog;
import com.rockchips.mediacenter.viewutils.devicelist.DeviceItem;
import com.rockchips.mediacenter.viewutils.devicelist.DevicesListView;
import com.rockchips.mediacenter.viewutils.devicelist.OnDeviceSelectedListener;
import com.rockchips.mediacenter.viewutils.devicelist.DevicesListView.OnSearchListener;
import com.rockchips.mediacenter.viewutils.menu.BottomPopMenu;
import com.rockchips.mediacenter.viewutils.menu.MenuItemImpl;
import com.rockchips.mediacenter.viewutils.menu.OnSelectTypeListener;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;

/**
 * 设备列表显示页面
 * @author GaoFei
 *
 */
public class MainActivity extends AppBaseActivity implements OnDeviceSelectedListener, OnSearchListener
{
    private static final String TAG = "MediaCenter_MainActivity";

    private static final IICLOG LOG = IICLOG.getInstance();

    private DevicesListView mDevicesListView;

    //private ImageView mIvLogo; 

    private List<DeviceItem> mDeviceItemList = new ArrayList<DeviceItem>();

    private List<LocalDevice> mDevInfoList = new ArrayList<LocalDevice>();

    /**
     * Smb设备列表
     */
    private List<SmbInfo> mSmbList;
    /**
     * NFS设备列表
     */
    private List<NFSInfo> mNFSList;
    
    /**
     * Smb匹配信息
     */
    private Map<String, SmbInfo> mSmbMap = new HashMap<String, SmbInfo>();
    /**
     * NFS匹配信息
     */
    private Map<String, NFSInfo> mNFSMap = new HashMap<String, NFSInfo>();
    private LinearLayout mLlNoDev;

    private static final int MEDIA_TYPE_FOLDER = 0;

    private static final int MEDIA_TYPE_PHOTO = 1;

    private static final int MEDIA_TYPE_MUSIC = 2;

    private static final int MEDIA_TYPE_VIDEO = 3;

    private static final int HANDLER_MSG_DEV_UPDATE = 1;

    private static MainActivity mStMainActivity;
    
    private DeviceUpDownReceiver mDeviceUpDownReceiver;
    
    private BottomPopMenu mPopMenu;

    /**
     * 每一张海报的宽度
     */
    private static int BITMAP_WIDTH = 280;
    
    /**
     * 设备列表
     */
    private RelativeLayout mLayoutDevices;
    
    /**
     * NFS信息数组
     */
    private JSONArray mNFSInfoArray;
    //private Map<String, NFSInfo> mNFSMap;
    /**
     * Smb信息数组
     */
    private JSONArray mSmbInfoArray;
    /**
     * 本地设备(U盘，SD卡，移动硬盘)监听绑定器
     */
    private ServiceConnection mDeviceMonitorConnection;
    private DeviceMonitorService mDeviceMonitorService;
    /**
     * 是否绑定DeviceMonitorService
     */
    private boolean isBindDeviceMonitorService;
    /**
     * 本地设备上下线监听
     */
    private LocalDeviceUpDownListener mLocalDeviceUpDownListener;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mStMainActivity = this;
        setContentView(R.layout.activity_main);
        mDevicesListView = (DevicesListView) findViewById(R.id.deviceList);
        mLlNoDev = (LinearLayout) findViewById(R.id.layout_no_device);
        mDevicesListView.setOnSearchListener(this);
        initView();
        //初始化数据
        initData();
        //绑定设备监听服务
        attachServices();
        //初始化网络设备
        initNetWorkDevices();
    }

  
    
    /**
     * 初始化网络设备(NFS,Samba,DLNA)
     */
    private void initNetWorkDevices(){
    	//检测网络
    	checkNetWork();
    	mNFSList = readNFSInfos();
    	mSmbList = readSmbInfos();
    	if(mNFSList != null && mNFSList.size() > 0){
    		for(NFSInfo nfsInfo : mNFSList){
    			mNFSMap.put(nfsInfo.getLocalMountPath(), nfsInfo);
    			//尝试挂载NFS设备
    			mountNFSDevice(nfsInfo);
    		}
    	}
    	
    	if(mSmbList != null && mSmbList.size() > 0){
    		for(SmbInfo smbInfo : mSmbList){
    			mSmbMap.put(smbInfo.getLocalMountPath(), smbInfo);
    			//尝试挂载samba设备
    			mountSmbDevice(smbInfo);
    		}
    	}
    }
    
    /**
     * 搜索UPNP设备
     */
    private void searchUpnpDevice(){
    	Intent intent = new Intent(ConstData.BroadCastMsg.REFRESH_NETWORK_DEVICE);
    	LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent);
    }
    
    
    private void mountSmbDevice(SmbInfo smbInfo){
    	Intent smbMountIntent = new Intent(ConstData.BroadCastMsg.SAMBA_MOUNT);
    	smbMountIntent.putExtra(ConstData.IntentKey.EXTRA_SAMBA_INFO, smbInfo);
    	smbMountIntent.putExtra(ConstData.IntentKey.EXTRA_IS_ADD_NETWORK_DEVICE, false);
		LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(smbMountIntent);
    }
    
    private void mountNFSDevice(NFSInfo nfsInfo){
    	Intent nfsMountIntent = new Intent(ConstData.BroadCastMsg.NFS_MOUNT);
		nfsMountIntent.putExtra(ConstData.IntentKey.EXTRA_NFS_INFO, nfsInfo);
		nfsMountIntent.putExtra(ConstData.IntentKey.EXTRA_IS_ADD_NETWORK_DEVICE, false);
		LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(nfsMountIntent);
    }
    
    /**
     * 发送网络检测广播
     */
    private void checkNetWork(){
    	Intent netWrokIntent = new Intent(ConstData.BroadCastMsg.CHECK_NETWORK);
		LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(netWrokIntent);
    }
    
    public static MainActivity getInstance()
    {
        return mStMainActivity;
    }

    /**
     * 初始化数据,读取网络设备相关信息
     */
    private void initData(){
    	mDeviceUpDownReceiver = new DeviceUpDownReceiver();
    	mLocalDeviceUpDownListener = new LocalDeviceUpDownListener();
    	mDeviceMonitorConnection = new ServiceConnection() {
			
			@Override
			public void onServiceDisconnected(ComponentName name) {
				
			}
			
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				LOG.i(TAG, "MainActivity->serviceConnection on ServiceConnected" );
				DeviceMonitorService.MonitorBinder serviceBinder = (DeviceMonitorService.MonitorBinder)service;
				mDeviceMonitorService = serviceBinder.getMonitorService();
				//注册本地设备上下线监听
				mDeviceMonitorService.registerLocalDeviceListener(mLocalDeviceUpDownListener);
			}
		};
		
		
    }
    
    /**
     * 初始化视图
     */
    private void initView(){
    	mLayoutDevices = (RelativeLayout)findViewById(R.id.layout_devices);
    	for(int i = 0; i != mLayoutDevices.getChildCount(); ++i){
    		TextView deviceTextView = (TextView)mLayoutDevices.getChildAt(i);
    		//设置DeviceItem的padding
    		RelativeLayout.LayoutParams textParams = (RelativeLayout.LayoutParams)deviceTextView.getLayoutParams();
    		textParams.leftMargin = (DeviceInfoUtils.getScreenWidth(this) - BITMAP_WIDTH * 4) / 2 + BITMAP_WIDTH / 2;
    		//deviceTextView.setPadding((DeviceInfoUtils.getScreenWidth(this) - BITMAP_WIDTH * 4) / 2 + BITMAP_WIDTH / 2, 0, 0, 0);
    		deviceTextView.setLayoutParams(textParams);
    	}
    	
    }
    
    /**
     * 读取已经记录的NFS信息
     */
    public List<NFSInfo> readNFSInfos(){
    	List<NFSInfo> nfsList = new ArrayList<NFSInfo>();
    	String nfsInfos = StorageUtils.getDataFromSharedPreference(ConstData.SharedKey.NFS_INFOS);
    	//Log.i(TAG, "readNFSInfos->nfsInfos:" + nfsInfos);
    	if(!TextUtils.isEmpty(nfsInfos)){
    		try{
    			mNFSInfoArray = new JSONArray(nfsInfos);
    			nfsList = (List<NFSInfo>)JsonUtils.arrayToList(NFSInfo.class, mNFSInfoArray);
    		}catch (Exception e){
    			//Log.i(TAG, "readNFSInfos->" + e);
    			//此处发生异常，直接清空数据
    			StorageUtils.saveDataToSharedPreference(ConstData.SharedKey.NFS_INFOS, "");
    			ToastUtils.showToast(getString(R.string.read_nfs_error));
    		}
    		
    	}
    	
    	return nfsList;
    }
    
    /**
     * 读取已经记录的Smb信息
     */
	public List<SmbInfo> readSmbInfos() {
		List<SmbInfo> smbList = new ArrayList<SmbInfo>();
		String smbInfos = StorageUtils.getDataFromSharedPreference(ConstData.SharedKey.SMB_INFOS);
		//Log.i(TAG, "readSmbInfos->smbinfos:" + smbInfos);
		if(!TextUtils.isEmpty(smbInfos)){
			try {
				mSmbInfoArray = new JSONArray(smbInfos);
				smbList = (List<SmbInfo>)JsonUtils.arrayToList(SmbInfo.class, mSmbInfoArray);
			} catch (Exception e) {
				//Log.i(TAG, "" + e);
				//此处发生异常，直接清空数据
				StorageUtils.saveDataToSharedPreference(ConstData.SharedKey.SMB_INFOS, "");
				ToastUtils.showToast(getString(R.string.read_samba_error));
			}
		}
		
		return smbList;
	}
    
	/**
	 * 刷新所有设备
	 */
	public void refreshAllDevices(){
		Intent intent = new Intent(ConstData.BroadCastMsg.REFRESH_ALL_DEVICES);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}
	
    /**
     * 添加网络设备选择对话框
     */
    private void showNetWorkSelectDialog(){
    	NetDeviceAddSelectDialog selectDialog = new NetDeviceAddSelectDialog(this, new NetDeviceAddSelectDialog.CallBack() {
			
			@Override
			public void onSelect(int type) {
				//回调处理
				if(type == ConstData.NetWorkDeviceType.DEVICE_NFS){
					showNFSAddDialog();
				}else{
					showSambaAddDialog();
				}
			}
			
			@Override
			public void onRefreshNetWorkDevice() {
				searchUpnpDevice();
			}
			
			@Override
			public void onRefreshAllDevices() {
				refreshAllDevices();
			}
		});
    	selectDialog.show();
    	
    }
    
    /**
     * 显示NFS设备添加对话框
     */
    public void showNFSAddDialog(){
    	NFSAddDialog nfsAddDialog = new NFSAddDialog(this, new NFSAddDialog.Callback() {
			@Override
			public void onGetNFSInfo(NFSInfo nfsInfo) {
				mNFSList = readNFSInfos();
				mNFSList.add(nfsInfo);
				mNFSMap.put(nfsInfo.getLocalMountPath(), nfsInfo);
				final NFSInfo newNfsInfo = nfsInfo;
				//存储至SharedPreference
				try{
					StorageUtils.saveDataToSharedPreference(ConstData.SharedKey.NFS_INFOS, JsonUtils.listToJsonArray(mNFSList).toString());
				}catch (Exception e){
					LOG.i(TAG, "showNFSAddDialog->e" + e);
				}
				DialogUtils.showLoadingDialog(MainActivity.this, false);
				new AsyncTask<Void, Integer, Integer>(){
					protected  Integer doInBackground(Void[] params) {
						if(MountUtils.mountNFS(newNfsInfo))
							return ConstData.TaskExecuteResult.SUCCESS;
						return ConstData.TaskExecuteResult.FAILED;
					};
					
					@Override
					protected void onPostExecute(Integer result) {
						DialogUtils.closeLoadingDialog();
						if(result == ConstData.TaskExecuteResult.SUCCESS){
						    //提示挂载成功
						    ToastUtils.showToast(getString(R.string.mount_success));
							//发送广播
							Intent nfsMountIntent = new Intent(ConstData.BroadCastMsg.NFS_MOUNT);
							nfsMountIntent.putExtra(ConstData.IntentKey.EXTRA_NFS_INFO, newNfsInfo);
							nfsMountIntent.putExtra(ConstData.IntentKey.EXTRA_IS_ADD_NETWORK_DEVICE, true);
							LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(nfsMountIntent);
						}else{
							//提示挂载失败
							ToastUtils.showToast(getString(R.string.mount_fail));
						}
					}
					
				}.execute();
				
			}
		});
    	
    	nfsAddDialog.show();
    }
    
    /**
     * 显示Samba设备添加对话框
     */
    public void showSambaAddDialog(){
    	SambaAddDialog sambaAddDialog = new SambaAddDialog(this, new SambaAddDialog.Callback() {
			public void onGetSambaInfo(SmbInfo smbInfo) {
				mSmbList = readSmbInfos();
				mSmbList.add(smbInfo);
				mSmbMap.put(smbInfo.getLocalMountPath(), smbInfo);
				final SmbInfo  newSambaInfo = smbInfo;
				//存储至SharedPreference
				try{
					StorageUtils.saveDataToSharedPreference(ConstData.SharedKey.SMB_INFOS, JsonUtils.listToJsonArray(mSmbList).toString());
				}catch (Exception e){
					LOG.i(TAG, "showSambaAddDialog->e" + e);
				}
				DialogUtils.showLoadingDialog(MainActivity.this, false);
				new AsyncTask<Void, Integer, Integer>(){
					protected  Integer doInBackground(Void[] params) {
						if(MountUtils.mountSamba(newSambaInfo))
							return ConstData.TaskExecuteResult.SUCCESS;
						return ConstData.TaskExecuteResult.FAILED;
					};
					
					@Override
					protected void onPostExecute(Integer result) {
						DialogUtils.closeLoadingDialog();
						//Log.i(TAG, "showSambaAddDialog->mountResult:" + result);
						if(result == ConstData.TaskExecuteResult.SUCCESS){
							//提示挂载成功
							ToastUtils.showToast(getString(R.string.mount_success));
							//发送广播
							Intent sambaMountIntent = new Intent(ConstData.BroadCastMsg.SAMBA_MOUNT);
							sambaMountIntent.putExtra(ConstData.IntentKey.EXTRA_SAMBA_INFO, newSambaInfo);
							sambaMountIntent.putExtra(ConstData.IntentKey.EXTRA_IS_ADD_NETWORK_DEVICE, true);
							LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(sambaMountIntent);
						}else{
							//提示挂载失败
							ToastUtils.showToast(getString(R.string.mount_fail));
						}
					}
					
				}.execute();
				//loadDeviceInfoList();

			};
    	});
    	sambaAddDialog.show();
    }
    
    
    @Override
    protected void onStart() {
    	super.onStart();
    	
    }
    
    @Override
    protected void onResume()
    {
        loadDeviceInfoList(false);
        registerDeviceUpDownListener();
        //devUpdate();
        super.onResume();
    }

    @Override
    protected void onPause()
    {
    	unRegisterDeviceUpDownListener();
        super.onPause();
    }

    @Override
    protected void onDestroy()
    {
    	unBindServices();
        mDevicesListView.recycle();
        if(mDeviceMonitorService != null)
        	mDeviceMonitorService.unRegisterLocalDeviceListener(mLocalDeviceUpDownListener);
        super.onDestroy();
    }

    @Override
    public void onSelected(Object object, int offset)
    {
        Intent intent = new Intent();
        LocalDevice selectDevice = (LocalDevice) object;
        intent.putExtra(ConstData.IntentKey.EXTRAL_LOCAL_DEVICE, selectDevice);
        switch (offset)
        {
            case MEDIA_TYPE_FOLDER:
            	//Log.i(TAG, "onSelected->offset:" + "MEDIA_TYPE_FOLDER");
                intent.putExtra(ConstData.IntentKey.EXTRAL_MEDIA_TYPE, ConstData.MediaType.FOLDER);
                if(selectDevice.getDevices_type() == ConstData.DeviceType.DEVICE_TYPE_DMS)
                	intent.setClass(this, AllUpnpFileListActivity.class);
                else
                	intent.setClass(this, AllFileListActivity.class);
                break;
            case MEDIA_TYPE_PHOTO:
                intent.putExtra(ConstData.IntentKey.EXTRAL_MEDIA_TYPE, ConstData.MediaType.IMAGEFOLDER);
                if(selectDevice.getDevices_type() == ConstData.DeviceType.DEVICE_TYPE_DMS)
                	intent.setClass(this, UpnpImageActivity.class);
                else
                	intent.setClass(this, ALImageActivity.class);
                break;
            case MEDIA_TYPE_MUSIC:
                intent.putExtra(ConstData.IntentKey.EXTRAL_MEDIA_TYPE, ConstData.MediaType.AUDIOFOLDER);
                if(selectDevice.getDevices_type() == ConstData.DeviceType.DEVICE_TYPE_DMS)
                	intent.setClass(this, UpnpFileListActivity.class);
                else
                	intent.setClass(this, FileListActivity.class);
                break;
            case MEDIA_TYPE_VIDEO:
                intent.putExtra(ConstData.IntentKey.EXTRAL_MEDIA_TYPE, ConstData.MediaType.VIDEOFOLDER);
                if(selectDevice.getDevices_type() == ConstData.DeviceType.DEVICE_TYPE_DMS)
                	intent.setClass(this, UpnpFileListActivity.class);
                else
                	intent.setClass(this, FileListActivity.class);
                break;
            default:
                LOG.e(TAG, "onSelected, invalid offset:" + offset);
                return;
        }
     /*   if(selectedDeviceInfo.getDeviceType() == Constant.DeviceType.DEVICE_TYPE_NFS){
        	intent.putExtra(ConstData.IntentKey.NFS_INFO, mNFSMap.get(selectedDeviceInfo.getmUUID()));
        }*/
        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        switch (keyCode)
        {
        	case KeyEvent.KEYCODE_MENU:
        		showNetWorkSelectDialog();
        		break;
            case KeyEvent.KEYCODE_BACK:
                finish();
                break;
            //case KeyEvent.KEYCODE_1:
            //    onOpenMenu();
           //     return true;
            //case KeyEvent.KEYCODE_2:
            //	showNetWorkSelectDialog();
            //	return true;
            default:
                mDevicesListView.onKeyDown(keyCode, event);
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    
    /**
     * 加载所有挂载设备信息的列表
     */
    public void loadDeviceInfoList(boolean isAddNetWork){
    	LOG.i(TAG, "loadDeviceInfoList");
    	devUpdate(isAddNetWork);
    	
    }
    
    private void devUpdate(boolean isAddNetWork)
    {
        List<LocalDevice> tmpList;
        mDevInfoList.clear();
        LocalDeviceService deviceService = new LocalDeviceService();
        tmpList = deviceService.getAll(LocalDevice.class);
		
        if (tmpList != null && tmpList.size() > 0)
        {
          
            mDevInfoList.addAll(tmpList);
        }
        
		
        if (mDevInfoList.size() == 0)
        {
            LOG.d(TAG, "devUpdate: mLocalDevList is null or size is 0!");
            mDevicesListView.setVisibility(View.GONE);
            mLlNoDev.setVisibility(View.VISIBLE);
            return;
        }

        LOG.d(TAG, "devUpdate: mDevInfoList.size():" + mDevInfoList.size());
        
        mLlNoDev.setVisibility(View.GONE);
        mDevicesListView.setOnDeviceSelectedListener(this);
        mDevicesListView.setVisibility(View.VISIBLE);

        int[] imageIds =
        { R.drawable.video_icon, R.drawable.file_icon, R.drawable.photo_icon, R.drawable.music_icon };
        int[] textIds =
        { R.string.video, R.string.file, R.string.photo, R.string.music };

        DeviceItem device;

        mDeviceItemList.clear();
        String name;
        for (int i = 0; i < mDevInfoList.size(); ++i)
        {
            LocalDevice info = mDevInfoList.get(i);
            if(info.getDevices_type() == ConstData.DeviceType.DEVICE_TYPE_SMB ){
            	name = DeviceTypeStr.getDevTypeStr(this, info.getDevices_type()) + mSmbMap.get(info.getMountPath()).getNetWorkPath() +
            			"(" + info.getPhysic_dev_id() + ")" ;
            }else if(info.getDevices_type() == ConstData.DeviceType.DEVICE_TYPE_NFS){
            	name = DeviceTypeStr.getDevTypeStr(this, info.getDevices_type()) + mNFSMap.get(info.getMountPath()).getNetWorkPath() +
            			"(" + info.getPhysic_dev_id() + ")" ;
            }else{
            	 name = DeviceTypeStr.getDevTypeStr(this, info.getDevices_type()) + info.getPhysic_dev_id();
            }
            device = new DeviceItem(info, name, imageIds, textIds);
            mDeviceItemList.add(device);
        }

        mDevicesListView.setDevicesList(mDeviceItemList, isAddNetWork);
        mDevicesListView.notifyDataChanged();
        if(isAddNetWork){
        	onKeyDown(KeyEvent.KEYCODE_DPAD_UP, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_UP));
        	onKeyDown(KeyEvent.KEYCODE_DPAD_DOWN, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN));
        }
    }
    
    /**
     * 刷新NFS设备,存储至SharedPreference
     */
    private void refreshNFSDevice(){
    	//mNFSList = readNFSInfos();
    	if(mNFSList != null && mNFSList.size() > 0){
    		//此时数据重新写入SharedPreference
    		StorageUtils.saveDataToSharedPreference(ConstData.SharedKey.NFS_INFOS, JsonUtils.listToJsonArray(mNFSList).toString());
    	}else{
    		StorageUtils.saveDataToSharedPreference(ConstData.SharedKey.NFS_INFOS, "");
    	}
    	
    }
    
    /**
     * 刷新SMB设备,存储至SharedPreference
     * @return
     */
    private void refreshSmbDevice(){
    	//mSmbList = readSmbInfos();
    	if(mSmbList != null && mSmbList.size() > 0){
    		//此时数据重新写入SharedPreference
    		StorageUtils.saveDataToSharedPreference(ConstData.SharedKey.SMB_INFOS, JsonUtils.listToJsonArray(mSmbList).toString());
    	}else{
    		StorageUtils.saveDataToSharedPreference(ConstData.SharedKey.SMB_INFOS, "");
    	}
    	
    }
    

    private static final int HANDLER_DELAY_TIME = 200;
    

    @Override
    public void onSearch(String key)
    {
        if (key == null || key.length() <= 0)
        {
        	ToastUtils.showToast(getString(R.string.search_tips));
            return;
        }
        Intent intent = new Intent();
        intent.setClass(this, FileListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Constant.EXTRA_IS_SEARCH, true);
        //intent.putExtra(BaseActivity.SEARCH_KEY, key);
        startActivity(intent);
    }
    
    /**
     * 注册设备上下线监听器
     */
    public void registerDeviceUpDownListener(){
    	IntentFilter intentFilter = new IntentFilter(ConstData.BroadCastMsg.DEVICE_UP);
    	intentFilter.addAction(ConstData.BroadCastMsg.DEVICE_DOWN);
    	LocalBroadcastManager.getInstance(this).registerReceiver(mDeviceUpDownReceiver, intentFilter);
    }
    
    /**
     * 取消注册设备上下线监听
     */
    public void unRegisterDeviceUpDownListener(){
    	LocalBroadcastManager.getInstance(this).unregisterReceiver(mDeviceUpDownReceiver);
    }
    
    /**
     * 绑定各种服务
     */
    public void attachServices(){
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
    
    class DeviceUpDownReceiver extends BroadcastReceiver{
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		//Log.i(TAG, "DeviceUpDownReceiver->onReceive");
    		//int deviceType = intent.getIntExtra(ConstData.IntentKey.EXTRA_DEVICE_TYPE, -1);
    		//设备路径
    		String devicePath = intent.getStringExtra(ConstData.IntentKey.EXTRA_DEVICE_PATH);
    		boolean isAddNetWork = intent.getBooleanExtra(ConstData.IntentKey.EXTRA_IS_ADD_NETWORK_DEVICE, false);
    		int deviceType = intent.getIntExtra(ConstData.IntentKey.EXTRA_DEVICE_TYPE, -1);
    		Log.i(TAG, "DeviceUpDownReceiver->devicePath:" + devicePath);
    		Log.i(TAG, "DeviceUpDownReceiver->isAddNetWork:" + isAddNetWork);
    		if(deviceType != ConstData.DeviceType.DEVICE_TYPE_SD && deviceType != ConstData.DeviceType.DEVICE_TYPE_U){
    			loadDeviceInfoList(isAddNetWork);
    		}
    		
    	}
    }
    
    /**
     * 本地设备上下线监听
     * @author GaoFei
     *
     */
    class LocalDeviceUpDownListener implements DeviceMonitorService.LocalDeviceListener{

		@Override
		public void onDeviceUpOrDown(Message message) {
			Log.i(TAG, "onDeviceUpOrDown");
			Log.i(TAG, "currentTime:" + System.currentTimeMillis());
			//刷新列表
			loadDeviceInfoList(false);
		}
    	
    }
}
