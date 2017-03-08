
package com.rockchips.mediacenter.view;

import java.io.File;
import java.util.List;

import org.xutils.view.annotation.ViewInject;

import momo.cn.edu.fjnu.androidutils.utils.DeviceInfoUtils;
import momo.cn.edu.fjnu.androidutils.utils.SizeUtils;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyboardShortcutGroup;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import com.rockchips.mediacenter.R;
import com.rockchips.mediacenter.bean.AllFileInfo;
import com.rockchips.mediacenter.bean.FileInfo;
import com.rockchips.mediacenter.utils.FileOpUtils;
/**
 * @author GaoFei
 * 文件操作对话框
 */
public class FileOpDialog extends AppBaseDialog implements OnItemClickListener{
	
	private View mainView;
	private Context mContext;
	/**
	 * 当前文件信息
	 */
	private FileInfo mFileInfo;
	/**文件操作栏*/
	@ViewInject(R.id.list_file_op)
	private ListView mListFileOp;
	
	public interface Callback{
		void onCopy(FileInfo fileInfo);
		void onDelete(FileInfo fileInfo);
		void onMove(FileInfo fileInfo);
		void onPaste(FileInfo fileInfo);
		void onRename(FileInfo fileInfo);
	}
	
	private Callback mCallback;
	
	public FileOpDialog(Context context, FileInfo fileInfo, Callback callback) {
		super(context);
		mContext = context;
		mFileInfo = fileInfo;
		mCallback = callback;
	}

	
	@Override
	public int getLayoutRes() {
		return R.layout.dialog_file_op;
	}

	
	@Override
	public void initView() {
		super.initView();
		mainView = getMainView();
		ViewGroup.LayoutParams mainParams = mainView.getLayoutParams();
		mainParams.width = SizeUtils.dp2px(mContext, 150);
		mainView.setLayoutParams(mainParams);
	}
	

	@Override
	public void initData() {
	}


	@Override
	public void initEvent() {
		mListFileOp.setOnItemClickListener(this);
	}


	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		switch (position) {
		case 0:
			mCallback.onCopy(mFileInfo);
			dismiss();
			break;
		case 1:
			mCallback.onDelete(mFileInfo);
			dismiss();
			break;
		case 2:
			mCallback.onMove(mFileInfo);
			dismiss();
			break;
		case 3:
			mCallback.onPaste(mFileInfo);
			dismiss();
			break;
		case 4:
			mCallback.onRename(mFileInfo);
			dismiss();
			break;
		default:
			break;
		}
	}

}
