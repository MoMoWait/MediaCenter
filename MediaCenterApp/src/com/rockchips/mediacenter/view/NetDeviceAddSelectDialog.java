package com.rockchips.mediacenter.view;
import momo.cn.edu.fjnu.androidutils.utils.NetWorkUtils;
import momo.cn.edu.fjnu.androidutils.utils.ResourceUtils;
import momo.cn.edu.fjnu.androidutils.utils.ToastUtils;

import org.xutils.view.annotation.ViewInject;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.rockchips.mediacenter.R;
import com.rockchips.mediacenter.data.ConstData;
/**
 * @author GaoFei
 * 网络设备添加选择对话框
 */
public class NetDeviceAddSelectDialog extends AppBaseDialog {
	
	public interface CallBack{
		void onSelect(int type);
		/**
		 * 刷新网络设备
		 */
		void onRefreshNetWorkDevice();
		/**
		 * 刷新所有设备
		 */
		void onRefreshAllDevices();
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
					mCallBack.onRefreshNetWorkDevice();
				}else if(position == 3){
					mCallBack.onRefreshAllDevices();
				}
				dismiss();
			}
		});
	}
	

}
