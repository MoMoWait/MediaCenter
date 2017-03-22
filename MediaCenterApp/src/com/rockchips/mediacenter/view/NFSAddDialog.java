package com.rockchips.mediacenter.view;
import java.io.File;
import java.util.Random;
import java.util.UUID;

import momo.cn.edu.fjnu.androidutils.utils.ResourceUtils;
import momo.cn.edu.fjnu.androidutils.utils.ToastUtils;

import org.xutils.view.annotation.ViewInject;
import com.rockchips.mediacenter.R;
import com.rockchips.mediacenter.bean.NFSInfo;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.utils.FileUtils;
import com.rockchips.mediacenter.utils.ValidUtils;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
/**
 * 添加NFS设备网络对话框
 * @author GaoFei
 *
 */

public class NFSAddDialog extends AppBaseDialog{
	public interface Callback{
		/**获取服务端地址*/
		void onGetNFSInfo(NFSInfo nfsInfo);
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
	
	public NFSAddDialog(Context context, Callback callback) {
		super(context);
		mCallback = callback;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
	}

	@Override
	public int getLayoutRes() {
		return R.layout.dialog_nfs_add;
	}

	@Override
	public void initData() {
		//mEditServerAddress.setText("120.24.18.183:/home/nfs");
	}

	@Override
	public void initEvent() {
		mBtnOk.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String serverAddress = mEditServerAddress.getText().toString().trim();
				if(TextUtils.isEmpty(serverAddress)){
					ToastUtils.showToast(ResourceUtils.getString(R.string.enter_server_address));
					return;
				}
				
				if(!ValidUtils.isNFSAddress(serverAddress)){
					ToastUtils.showToast(ResourceUtils.getString(R.string.enter_right_nfs_address));
					return;
				}
				
				NFSInfo nfsInfo = new NFSInfo();
				String uuidPathName = UUID.randomUUID().toString();
                uuidPathName = uuidPathName.substring(uuidPathName.length() - 8, uuidPathName.length());
				String mountPath = ConstData.NETWORK_DEVICE_MOUNT_DIR +  "/" + uuidPathName;
				int randomNum = new Random().nextInt(10000);
				//mountPath += randomNum;
				while(FileUtils.isExist(Environment.getExternalStorageDirectory(), mountPath + randomNum))
					randomNum = new Random().nextInt(10000);
				mountPath += randomNum;
				nfsInfo.setNetWorkPath(serverAddress);
				nfsInfo.setLocalMountPath(mountPath);
				mCallback.onGetNFSInfo(nfsInfo);
				//nfsInfo.setLocalMountPath(E)
				//mCallback.onGetServerAddress(serverAddress);
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
