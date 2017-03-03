
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
	private AllFileInfo mAllFileInfo;
	/**文件操作栏*/
	@ViewInject(R.id.list_file_op)
	private ListView mListFileOp;
	
	public interface Callback{
		void onCopy(AllFileInfo allFileInfo);
		void onDelete(AllFileInfo allFileInfo);
		void onMove(AllFileInfo allFileInfo);
		void onPaste(AllFileInfo allFileInfo);
		void onRename(AllFileInfo allFileInfo);
	}
	
	private Callback mCallback;
	
	public FileOpDialog(Context context, AllFileInfo allFileInfo, Callback callback) {
		super(context);
		mContext = context;
		mAllFileInfo = allFileInfo;
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
			mCallback.onCopy(mAllFileInfo);
			dismiss();
			break;
		case 1:
			mCallback.onDelete(mAllFileInfo);
			dismiss();
			break;
		case 2:
			mCallback.onMove(mAllFileInfo);
			dismiss();
			break;
		case 3:
			mCallback.onPaste(mAllFileInfo);
			dismiss();
			break;
		case 4:
			mCallback.onRename(mAllFileInfo);
			dismiss();
			break;
		default:
			break;
		}
	}

}
