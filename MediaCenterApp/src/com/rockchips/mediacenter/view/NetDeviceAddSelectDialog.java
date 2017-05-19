package com.rockchips.mediacenter.view;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import momo.cn.edu.fjnu.androidutils.utils.NetWorkUtils;
import momo.cn.edu.fjnu.androidutils.utils.ResourceUtils;
import momo.cn.edu.fjnu.androidutils.utils.ToastUtils;

import org.xutils.view.annotation.ViewInject;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.rockchips.mediacenter.R;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.utils.PlatformUtils;
/**
 * @author GaoFei
 * 网络设备添加选择对话框
 */
public class NetDeviceAddSelectDialog extends AppBaseDialog {
	
	public interface CallBack{
		//选择添加NFS或Samba设备
		void onSelect(int type);
		//刷新网络设备
		void onRefreshNetWorkDevice();
		//刷新所有设备
		void onRefreshAllDevices();
		//删除Smb或NFS设备
		void onDeleteSMBOrNFSDevices();
		//启动家庭媒体共享
		void onStartHomeMediaShare();
	}
	
	@ViewInject(R.id.list_device_select)
	private ListView mListNetAddSelect;
	
	private Context mContext;
	private CallBack mCallBack;
	
	public NetDeviceAddSelectDialog(Context context, CallBack callBack) {
		super(context);
		this.mContext = context;
		this.mCallBack = callBack;
	}

	@Override
	public int getLayoutRes() {
		return R.layout.dialog_net_device_add;
	}

	@Override
	public void initData() {
		/*if(PlatformUtils.getSDKVersion() <= 19){
			String[] oprations = mContext.getResources().getStringArray(R.array.net_devices);
			List<String> oprationList = new ArrayList<>();
			for(String itemOpration : oprations){
				oprationList.add(itemOpration);
			}
			oprationList.add(mContext.getString(R.string.start_home_media_share));
			ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, android.R.id.text1, oprationList);
			mListNetAddSelect.setAdapter(adapter);
		}*/
		
	}

	@Override
	public void initEvent() {
		mListNetAddSelect.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(position < 2 && !NetWorkUtils.haveInternet(mContext)){
					ToastUtils.showToast(ResourceUtils.getString(R.string.check_network));
					return;
				}
				if(position == 0){
					mCallBack.onSelect(ConstData.NetWorkDeviceType.DEVICE_NFS);
				}else if(position == 1){
					mCallBack.onSelect(ConstData.NetWorkDeviceType.DEVICE_SMB);
				}else if(position == 2){
					mCallBack.onDeleteSMBOrNFSDevices();
				}
				else if(position == 3){
					mCallBack.onRefreshNetWorkDevice();
				}else if(position == 4){
					mCallBack.onRefreshAllDevices();
				}else if(position == 5){
					mCallBack.onStartHomeMediaShare();
				}
				dismiss();
			}
		});
	}
	

}
