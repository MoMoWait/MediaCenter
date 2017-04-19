/**
 * 
 */
package com.rockchips.mediacenter.view;
import momo.cn.edu.fjnu.androidutils.utils.ToastUtils;

import org.xutils.view.annotation.ViewInject;

import com.rockchips.mediacenter.R;

import android.text.TextUtils;
import android.view.View;
import android.content.Context;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

/**
 * @author GaoFei
 * 文件搜索对话框
 */
public class FileSearchDialog extends AppBaseDialog implements View.OnClickListener{
	public interface Callback{
		void onOk(String text);
	}
	@ViewInject(R.id.edit_search)
	private EditText mEditSearch;
	@ViewInject(R.id.btn_ok)
	private Button mBtnOK;
	@ViewInject(R.id.btn_cancel)
	private Button mBtnCancel;
	private Callback mCallback;
	private Context mContext;
	public FileSearchDialog(Context context, Callback callback){
		super(context);
		mCallback = callback;
		mContext = context;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
	}
	
	@Override
	public int getLayoutRes() {
		return R.layout.dialog_file_search;
	}
	

	@Override
	public void initData() {
		
	}

	@Override
	public void initEvent() {
		mBtnOK.setOnClickListener(this);
		mBtnCancel.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		if(v == mBtnOK){
			String searchStr = mEditSearch.getText().toString().trim();
			if(TextUtils.isEmpty(searchStr)){
				//请输入内容
				ToastUtils.showToast(mContext.getString(R.string.enter_search_content));
			}else{
				dismiss();
				mCallback.onOk(searchStr);
			}
		}else if(v == mBtnCancel){
			dismiss();
		}
	}
	
	public void focusInputSearch(){
		mEditSearch.requestFocus();
		mEditSearch.setSelection(mEditSearch.getText().toString().length());
	}
}
