/**
 * 
 */
package com.rockchips.mediacenter.view;
import java.io.File;
import java.util.List;

import momo.cn.edu.fjnu.androidutils.utils.ToastUtils;

import org.xutils.view.annotation.ViewInject;

import com.rockchips.mediacenter.bean.AllFileInfo;
import com.rockchips.mediacenter.bean.FileInfo;

import android.R.raw;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.text.TextUtils;
import android.view.KeyboardShortcutGroup;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.rockchips.mediacenter.R;
/**
 * @author GaoFei
 * 文件重命名对话框
 */
public class FileRenameDialog extends AppBaseDialog implements View.OnClickListener{
	
	public interface Callback{
		void onFinish();
	}
	
	private FileInfo mFileInfo;
	private Callback mCallback;
	private Context mContext;
	@ViewInject(R.id.edit_file_name)
	private EditText mEditFileNmae;
	@ViewInject(R.id.btn_ok)
	private Button mBtnOk;
	@ViewInject(R.id.btn_cancel)
	private Button mBtnCancel;
	public FileRenameDialog(Context context, FileInfo fileInfo, Callback callback) {
		super(context);
		mFileInfo = fileInfo;
		mCallback = callback;
		mContext = context;
	}
	
	@Override
	public int getLayoutRes() {
		return R.layout.dialog_file_rename;
	}
	
	
	@Override
	public void initData() {
		mEditFileNmae.setText(mFileInfo.getName());
	}


	
	
	@Override
	public void initEvent() {
		mBtnOk.setOnClickListener(this);
		mBtnCancel.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.btn_ok:
			String fileName = mEditFileNmae.getText().toString().trim();
			if(TextUtils.isEmpty(fileName)){
				ToastUtils.showToast(mContext.getString(R.string.enter_new_file_name));
				return;
			}
			if(fileName.equals(mFileInfo.getName())){
				ToastUtils.showToast(mContext.getString(R.string.enter_diff_name));
			}else if(!new File(mFileInfo.getParentPath()).canWrite()){
				//没有写权限
				ToastUtils.showToast(mContext.getString(R.string.no_write_permission));
			}else{
				new File(mFileInfo.getPath()).renameTo(new File(new File(mFileInfo.getParentPath()), fileName));
				//MediaScannerConnection.scanFile(mContext, mFileInfo.getpa, mimeTypes, callback)
				mCallback.onFinish();
				dismiss();
			}
			break;
		case R.id.btn_cancel:
			dismiss();
			break;
		default:
			break;
		}
	}

	
	public void setAllFileInfo(FileInfo fileInfo){
		mFileInfo = fileInfo;
		mEditFileNmae.setText(mFileInfo.getName());
	}
	
}
