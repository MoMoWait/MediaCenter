package com.rockchips.mediacenter.view;

import org.xutils.x;

import momo.cn.edu.fjnu.androidutils.base.BaseDialog;
import momo.cn.edu.fjnu.androidutils.utils.DeviceInfoUtils;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

/**
 * @author GaoFei
 * App基础对话框
 */
public abstract class AppBaseDialog extends BaseDialog {
	/**
	 * 对话框主视图
	 */
	private View mView;
	private Context mContext;
	private boolean isCreated = false;
	public AppBaseDialog(Context context) {
		super(context);
		mContext = context;
	}

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT);
		isCreated = true;
	}
	
	
	@Override
	public void initView() {
		LayoutInflater inflater = LayoutInflater.from(mContext);
		mView = inflater.inflate(getLayoutRes(), null);
		setContentView(mView, new LinearLayout.LayoutParams(DeviceInfoUtils.getDeviceInfo(mContext).getScreenWidth() / 2, LinearLayout.LayoutParams.WRAP_CONTENT));
		x.view().inject(this, mView);
	}
	
	
	/**
	 * 返回资源ID
	 * @return
	 */
	public abstract int getLayoutRes();
	
	
	/**
	 * 获取对话框的主视图
	 * @return
	 */
	public View getMainView(){
		return mView;
	}
	
	
	/**
	 * 判断对话框是否已经创建
	 * @return
	 */
	public boolean isCreated(){
		return isCreated;
	}
	

}
