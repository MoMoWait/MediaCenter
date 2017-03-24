/**
 * 
 */
package com.rockchips.mediacenter.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import momo.cn.edu.fjnu.androidutils.utils.JsonUtils;
import momo.cn.edu.fjnu.androidutils.utils.StorageUtils;
import momo.cn.edu.fjnu.androidutils.utils.ToastUtils;

import org.xutils.view.annotation.ViewInject;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyboardShortcutGroup;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;
import com.rockchips.mediacenter.R;
import com.rockchips.mediacenter.bean.Device;
import com.rockchips.mediacenter.bean.NFSInfo;
import com.rockchips.mediacenter.bean.SmbInfo;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.modle.db.DeviceService;
import com.rockchips.mediacenter.utils.DeviceTypeStr;
import com.rockchips.mediacenter.utils.Utils;
/**
 * @author GaoFei
 * Samba或NFS设备删除对话框
 */
public class SmbOrNfsDeleteDialog extends AppBaseDialog{
	private static final String TAG = "SmbOrNfsDeleteDialog";
	@ViewInject(R.id.text_title)
	private TextView mTextTitle;
	@ViewInject(R.id.list_devices)
	private ListView mListDevices;
	@ViewInject(R.id.btn_ok)
	private Button mBtnOk;
	@ViewInject(R.id.btn_cancel)
	private Button mBtnCancel;
	private Context mContext;
	/**
	 * 当前选中设备
	 */
	private Map<Integer, Device> mSelectedDevices = new HashMap<>();
	private List<Device> mAllDevices;
	private DeviceService mDeviceService;
	public SmbOrNfsDeleteDialog(Context context){
		super(context);
		mContext = context;
	}

	@Override
	public int getLayoutRes() {
		return R.layout.dialog_smb_or_nfs_delete;
	}

	@Override
	public void initData() {
		//加载数据，从数据库中读取Smb或者NFS设备列表,数据量下，直接在主线程加载
		loadSmbOrNFSDevices();
	}

	@Override
	public void initEvent() {
		mListDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				CheckedTextView itemView = (CheckedTextView)view;
				itemView.setChecked(!itemView.isChecked());
				if(itemView.isChecked())
					mSelectedDevices.put(position, mAllDevices.get(position));
				else
					mSelectedDevices.remove(position);
			}
		});
		mBtnOk.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mSelectedDevices.size() > 0){
					List<SmbInfo> smbInfos = Utils.readSmbInfos();
					Log.i(TAG, "onOkClick->smbInfos->size:" + smbInfos.size());
					List<NFSInfo> nfsInfos = Utils.readNFSInfos();
					Log.i(TAG, "onOkClick->nfsInfos->size:" + nfsInfos.size());
					Set<Integer> keys = mSelectedDevices.keySet();
					Iterator<Integer> iterator = keys.iterator();
					SmbInfo delSmbInfo = new SmbInfo();
					NFSInfo delNfsInfo = new NFSInfo();
					while(iterator.hasNext()){
						Device device = mSelectedDevices.get(iterator.next());
						//发送设备删除广播
						Intent broadIntent = new Intent(ConstData.BroadCastMsg.DELETE_DEVICE);
						broadIntent.putExtra(ConstData.IntentKey.EXTRAL_LOCAL_DEVICE, device);
						LocalBroadcastManager.getInstance(mContext).sendBroadcast(broadIntent);
						//mDeviceService.delete(device);
						if(device.getDeviceType() == ConstData.DeviceType.DEVICE_TYPE_SMB && smbInfos != null
								&& smbInfos.size() > 0){
							delSmbInfo.setLocalMountPath(device.getLocalMountPath());
							smbInfos.remove(delSmbInfo);
						}
						else if(device.getDeviceType() == ConstData.DeviceType.DEVICE_TYPE_NFS && nfsInfos != null
								&& nfsInfos.size() > 0){
							delNfsInfo.setLocalMountPath(device.getLocalMountPath());
							nfsInfos.remove(delNfsInfo);
						}
					}
					if(smbInfos != null && smbInfos.size() > 0){
						Log.i(TAG, "onOkClick->smbInfos->size:" + smbInfos.size());
						StorageUtils.saveDataToSharedPreference(ConstData.SharedKey.SMB_INFOS, JsonUtils.listToJsonArray(smbInfos).toString());
					}else{
						StorageUtils.saveDataToSharedPreference(ConstData.SharedKey.SMB_INFOS, "");
					}
					if(nfsInfos != null && nfsInfos.size() > 0){
						Log.i(TAG, "onOkClick->nfsInfos->size:" + nfsInfos.size());
						StorageUtils.saveDataToSharedPreference(ConstData.SharedKey.NFS_INFOS, JsonUtils.listToJsonArray(nfsInfos).toString());
					}else{
						StorageUtils.saveDataToSharedPreference(ConstData.SharedKey.NFS_INFOS, "");
					}
					dismiss();
				}else{
					if(mAllDevices != null && mAllDevices.size() > 0)
						ToastUtils.showToast(mContext.getString(R.string.tip_select_delete));
					else
						dismiss();
				}
				
			}
		});
		mBtnCancel.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
	}
	
	public void loadSmbOrNFSDevices(){
		mDeviceService = new DeviceService();
		mAllDevices = mDeviceService.getAllSmbOrNFSDevices();
		if(mAllDevices != null && mAllDevices.size() > 0){
			List<String> deviceNames = new ArrayList<>();
			for(Device itemDevice : mAllDevices)
				deviceNames.add(DeviceTypeStr.getDevTypeStr(mContext, itemDevice.getDeviceType()) + itemDevice.getNetWorkPath() +
            			"(" + itemDevice.getDeviceName() + ")");
			ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_multiple_choice, android.R.id.text1, deviceNames);
			mListDevices.setAdapter(adapter);
		}else{
			//暂未添加SMB设备或NFS设备
			mTextTitle.setText(R.string.no_add_smb_nfs);
		}
		
	}
}
