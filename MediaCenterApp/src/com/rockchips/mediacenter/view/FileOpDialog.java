
package com.rockchips.mediacenter.view;
import java.io.File;
import org.json.JSONObject;
import org.xutils.view.annotation.ViewInject;
import momo.cn.edu.fjnu.androidutils.utils.JsonUtils;
import momo.cn.edu.fjnu.androidutils.utils.SizeUtils;
import momo.cn.edu.fjnu.androidutils.utils.StorageUtils;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.rockchips.mediacenter.R;
import com.rockchips.mediacenter.bean.FileInfo;
import com.rockchips.mediacenter.data.ConstData;
/**
 * @author GaoFei
 * 文件操作对话框
 */
public class FileOpDialog extends AppBaseDialog implements OnItemClickListener{
	private static final String TAG = "FileOpDialog";
	private View mainView;
	private Context mContext;
	/**
	 * 当前文件信息
	 */
	private FileInfo mFileInfo;
	/**文件操作栏*/
	@ViewInject(R.id.list_file_op)
	private ListView mListFileOp;
	private boolean mIsEmptyFolder;
	public interface Callback{
		void onCopy(FileInfo fileInfo);
		void onDelete(FileInfo fileInfo);
		void onMove(FileInfo fileInfo);
		void onPaste(FileInfo fileInfo);
		void onRename(FileInfo fileInfo);
	}
	
	private Callback mCallback;
	
	public FileOpDialog(Context context, FileInfo fileInfo, boolean isEmptyFolder, Callback callback) {
		super(context);
		mContext = context;
		mFileInfo = fileInfo;
		mCallback = callback;
		mIsEmptyFolder = isEmptyFolder;
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
		String copyPath = StorageUtils.getDataFromSharedPreference(ConstData.SharedKey.COPY_FILE_PATH);
		String movePath = StorageUtils.getDataFromSharedPreference(ConstData.SharedKey.MOVE_FILE_PATH);
		if(TextUtils.isEmpty(copyPath) && TextUtils.isEmpty(movePath)){
			//屏蔽paste选项
			mListFileOp.setAdapter(new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, android.R.id.text1, mContext.getResources().getStringArray(R.array.file_oprations_no_paste)));
			return;
		}
		try{
			FileInfo copyOrMoveFileInfo = (FileInfo) (TextUtils.isEmpty(copyPath) ? JsonUtils.jsonToObject(FileInfo.class, new JSONObject(movePath)) :
				JsonUtils.jsonToObject(FileInfo.class, new JSONObject(copyPath)));
			Log.i(TAG, "copyOrMoveFileInfo:" + copyOrMoveFileInfo);
			Log.i(TAG, "mFileInfo:" + mFileInfo);
			if(mIsEmptyFolder){
				mListFileOp.setAdapter(new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, android.R.id.text1, mContext.getResources().getStringArray(R.array.file_oprations_only_paste)));
			}else if(copyOrMoveFileInfo.getParentPath().equals(mFileInfo.getParentPath()) || mFileInfo.getPath().startsWith(copyOrMoveFileInfo.getPath()) || !new File(copyOrMoveFileInfo.getPath()).exists()){
				mListFileOp.setAdapter(new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, android.R.id.text1, mContext.getResources().getStringArray(R.array.file_oprations_no_paste)));
			}
			
		}catch (Exception e){
			Log.i(TAG, "copyOrMoveFileInfo:->exception:" + e);
		}
		
		
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
			if(!mIsEmptyFolder)
				mCallback.onCopy(mFileInfo);
			else
				mCallback.onPaste(mFileInfo);
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
			mCallback.onRename(mFileInfo);
			dismiss();
			break;
		case 4:
			mCallback.onPaste(mFileInfo);
			dismiss();
			break;
		default:
			break;
		}
	}

}
