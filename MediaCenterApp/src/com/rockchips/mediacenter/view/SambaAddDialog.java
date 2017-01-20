package com.rockchips.mediacenter.view;

import java.util.UUID;

import momo.cn.edu.fjnu.androidutils.utils.ResourceUtils;
import momo.cn.edu.fjnu.androidutils.utils.ToastUtils;

import org.xutils.view.annotation.ViewInject;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.rockchips.mediacenter.R;
import com.rockchips.mediacenter.bean.SmbInfo;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.util.ValidUtils;
/**
 * @author GaoFei
 * 添加Samba设备网络对话框
 */
public class SambaAddDialog extends AppBaseDialog{

	public interface Callback{
		/**
		 * 回调函数，对话框确认关闭时的处理
		 * @param serverAddress
		 * @param userName
		 * @param password
		 * @param isUnknowName
		 */
		void onGetSambaInfo(SmbInfo smbInfo);
	}
	
	private Callback mCallback;
	
	@ViewInject(R.id.edit_server_address)
	private EditText mEditServerAddress;
	@ViewInject(R.id.edit_user_name)
	private EditText mEditUserName;
	@ViewInject(R.id.edit_password)
	private EditText mEditPassword;
	@ViewInject(R.id.cbox_unknow_name)
	private CheckBox mCboxUnknowName;
	@ViewInject(R.id.btn_ok)
	private Button mBtnOk;
	@ViewInject(R.id.btn_cancel)
	private Button mBtnCancel;
	
	public SambaAddDialog(Context context, Callback callback) {
		super(context);
		mCallback = callback;
	}


	@Override
	public int getLayoutRes() {
		return R.layout.dialog_samba_add;
	}

	@Override
	public void initData() {
		mEditServerAddress.setText("//10.10.10.130/share");
		//mEditServerAddress.setText("//10.10.10.164/Marshmallow_Repository");
		//mEditUserName.setText("sdk");
		//mEditPassword.setText("839919");
	}

	@Override
	public void initEvent() {
		mBtnOk.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				boolean isUnknowName = mCboxUnknowName.isChecked();
				String serverAddress = mEditServerAddress.getText().toString().trim();
				String userName = mEditUserName.getText().toString().trim();
				String password = mEditPassword.getText().toString().trim();
				if(!isUnknowName){
					if(TextUtils.isEmpty(serverAddress) || TextUtils.isEmpty(userName) || TextUtils.isEmpty(password)){
						ToastUtils.showToast(ResourceUtils.getString(R.string.enter_full));
						return;
					}
					
					if(!ValidUtils.isSambaAddress(serverAddress)){
						ToastUtils.showToast(ResourceUtils.getString(R.string.enter_right_samba_address));
						return;
					}
					
				}else{
					if(TextUtils.isEmpty(serverAddress)){
						ToastUtils.showToast(ResourceUtils.getString(R.string.enter_server_address));
						return;
					}
					
					if(!ValidUtils.isSambaAddress(serverAddress)){
						ToastUtils.showToast(ResourceUtils.getString(R.string.enter_right_samba_address));
						return;
					}
				}
				
				SmbInfo smbInfo = new SmbInfo();
				smbInfo.setNetWorkPath(serverAddress);
				String uuidPathName = UUID.randomUUID().toString();
				uuidPathName = uuidPathName.substring(uuidPathName.length() - 8, uuidPathName.length());
				smbInfo.setLocalMountPath(ConstData.NETWORK_DEVICE_MOUNT_DIR + "/" + uuidPathName);
				smbInfo.setPassword(password);
				smbInfo.setUniqueID(UUID.randomUUID().toString());
				smbInfo.setUnknowName(isUnknowName);
				smbInfo.setUserName(userName);
				mCallback.onGetSambaInfo(smbInfo);
				dismiss();
			}
		});
		
		mBtnCancel.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
	}


	
}
