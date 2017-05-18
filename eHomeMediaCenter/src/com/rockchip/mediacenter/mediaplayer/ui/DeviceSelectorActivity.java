/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Filename:    DeviceSelectorActivity.java  
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2012-6-21 涓婂崍11:23:55  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2012-6-21      fxw         1.0         create
*******************************************************************/   


package com.rockchip.mediacenter.mediaplayer.ui;

import java.util.ArrayList;
import java.util.List;

import com.rockchip.mediacenter.DLNAManager;
import com.rockchip.mediacenter.DLNAService;
import com.rockchip.mediacenter.R;
import com.rockchip.mediacenter.dlna.model.DeviceItem;
import com.rockchip.mediacenter.plugins.imageloader.ImageLoader;
import com.rockchip.mediacenter.plugins.imageloader.LocalBitmapCache;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/**
 *
 * @author fxw
 * @since 1.0
 */
public class DeviceSelectorActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener {

	public static final String EXTRA_SELECTED_DEVICE = "SelectedDevice";
	private DLNAManager dlnaManager;
	private TextView mTitleView;
	private ListView mListView;
	private Button mSureButton;
	private ImageLoader mImageLoader;
	private DeviceSelectorAdapter mSelectorAdapter;
	
	/** 
	 * <p>Title: onCreate</p> 
	 * <p>Description: </p> 
	 * @param savedInstanceState 
	 * @see android.app.Activity#onCreate(android.os.Bundle) 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setWindowAnimations(R.style.PopupAnimation);
		setContentView(R.layout.dms_share_choice);
		mImageLoader = new ImageLoader();
		mImageLoader.setContext(this);
		mImageLoader.setImageCacheStrategy(new LocalBitmapCache(this));
		initView();
		dlnaManager = new DLNAManager(this);
		dlnaManager.setBindListener(new DLNAManager.BindListener() {
			public void onBindCompleted() {
				dlnaManager.getDigitalMediaPlayer().search();
				String udn = getSaveDeviceUDN();
				List<DeviceItem> deviceItemList = dlnaManager.getMediaServerDevice(false);
				if(udn!=null){
					for(int i=0; i<deviceItemList.size(); i++){
						if(udn.equals(deviceItemList.get(i).getUdn())){
							mListView.setSelection(i);
							mSelectorAdapter.setSelectedIndex(i);
							mSureButton.requestFocus();
							break;
						}
					}
				}
				mSelectorAdapter.setDataSource(deviceItemList);
				
			}
		});
		dlnaManager.startManager();
		registerAddOrRemoveDevice();
	}
	
	private void initView(){
		mTitleView = (TextView)findViewById(R.id.dms_share_choice_title);
		mListView = (ListView)findViewById(R.id.dms_share_choice_listview);
		mSureButton = (Button)findViewById(R.id.button1);
		mSelectorAdapter = new DeviceSelectorAdapter(this, new ArrayList<DeviceItem>(), mImageLoader);
		mSelectorAdapter.setListView(mListView);
		mListView.setAdapter(mSelectorAdapter);
		mListView.setOnItemClickListener(this);
		mListView.setSelector(R.drawable.sys_dialog_item_shape);
		mTitleView.setText(R.string.dmu_device_selector_title);
		mSureButton.setOnClickListener(this);
		findViewById(R.id.button2).setOnClickListener(this);
		
		LinearLayout layout = (LinearLayout)findViewById(R.id.parent_container);
		LayoutParams params = layout.getLayoutParams();
		Display display = getWindowManager().getDefaultDisplay();
		int minLen = display.getHeight()>display.getWidth()?display.getWidth():display.getHeight();
		params.height = (int)(minLen*0.8);
		layout.setLayoutParams(params);
	}
	
	private void refreshDataSource(){
		List<DeviceItem> deviceItemList = dlnaManager.getMediaServerDevice(false);
		mSelectorAdapter.setDataSource(deviceItemList);
	}
	
	/** 
	 * <p>Title: onResume</p> 
	 * <p>Description: </p>  
	 * @see android.app.Activity#onResume() 
	 */
	@Override
	protected void onResume() {
		super.onResume();
		if(mSelectorAdapter.getSelectedIndex()>=0){
			mListView.setSelection(mSelectorAdapter.getSelectedIndex());
		}
	}
	
	/** 
	 * <p>Title: onDestroy</p> 
	 * <p>Description: </p>  
	 * @see android.app.Activity#onDestroy() 
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mDeviceAddOrRemoveListener);
		dlnaManager.stopManager();
		//mImageLoader.clear();
	}

	/** 
	 * <p>Title: onClick</p> 
	 * <p>Description: </p> 
	 * @param view 
	 * @see android.view.View.OnClickListener#onClick(android.view.View) 
	 */
	@Override
	public void onClick(View view) {
		int viewId = view.getId();
		if(viewId == R.id.button1){
			DeviceItem item = mSelectorAdapter.getSelctedItem();
			saveDeviceUDN(item);
			if(item!=null){
				Intent data = new Intent();
				data.putExtra(EXTRA_SELECTED_DEVICE, item);
				setResult(RESULT_OK, data);
			}else{
				setResult(RESULT_CANCELED);
			}
			finish();		
		}else if(viewId == R.id.button2){
			setResult(RESULT_CANCELED);
			finish();
		}
	}
	

	/** 
	 * <p>Title: onItemClick</p> 
	 * <p>Description: </p> 
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3 
	 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long) 
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//		DeviceItem deviceItem = (DeviceItem)parent.getItemAtPosition(position);
		mSelectorAdapter.setSelectedView(view);
		mSelectorAdapter.setSelectedIndex(position);
		mListView.invalidate();
//		saveDeviceUDN(deviceItem);
//		Intent data = new Intent();
//		setResult(RESULT_OK, data);
	}
	
	/**
	 * 淇濆瓨閫変腑鐨勮澶�
	 * @param device
	 */
	private void saveDeviceUDN(DeviceItem device){
		if(device==null) return;
		SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = preference.edit();
		editor.putString("device_udn", device.getUdn());
		editor.commit();
	}
	
	/**
	 * 鑾峰彇涓婃閫変腑鐨勮澶�
	 * @return
	 */
	private String getSaveDeviceUDN(){
		SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
		return preference.getString("device_udn", null);
	}
	

	/**
	 * 娉ㄥ唽璁惧娣诲姞绉婚櫎骞挎挱鐩戝惉
	 */
	public void registerAddOrRemoveDevice() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(DLNAService.ACTION_ADD_SERVER_DEVICE);
		intentFilter.addAction(DLNAService.ACTION_REMOVE_SERVER_DEVICE);
		registerReceiver(mDeviceAddOrRemoveListener, intentFilter);
	}
	
	/**
	 * 璁惧娣诲姞绉婚櫎骞挎挱鐩戝惉
	 */
	private BroadcastReceiver mDeviceAddOrRemoveListener = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			DeviceItem deviceItem = intent.getParcelableExtra(DLNAService.KEY_DEVICE);
			if (action.equals(DLNAService.ACTION_ADD_SERVER_DEVICE)) {
				refreshDataSource();
			} else if (action.equals(DLNAService.ACTION_REMOVE_SERVER_DEVICE)) {
				refreshDataSource();
				if(deviceItem.getIconURL()!=null){
					mImageLoader.unload(deviceItem.getIconURL());
				}
			}
		}
	};

	
}
