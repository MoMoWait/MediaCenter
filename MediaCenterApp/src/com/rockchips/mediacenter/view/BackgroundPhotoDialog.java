package com.rockchips.mediacenter.view;
import java.util.LinkedList;
import java.util.List;
import org.xutils.view.annotation.ViewInject;
import com.rockchips.mediacenter.R;
import com.rockchips.mediacenter.adapter.BackgroundPhotoAdapter;
import com.rockchips.mediacenter.bean.Device;
import com.rockchips.mediacenter.bean.FileInfo;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.modle.db.FileInfoService;
import momo.cn.edu.fjnu.androidutils.utils.DeviceInfoUtils;
import momo.cn.edu.fjnu.androidutils.utils.ToastUtils;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.DialogInterface.OnDismissListener;
import android.view.View.OnClickListener;
/**
 * @author GaoFei
 * 背景图选择对话框
 */
public class BackgroundPhotoDialog extends AppBaseDialog implements OnDismissListener, OnClickListener, OnItemClickListener{
	
	public interface Callback{
		void onFinished(List<FileInfo> fileInfos);
	}
	
	@ViewInject(R.id.grid_image)
	private GridView mGridImage;
	@ViewInject(R.id.btn_ok)
	private Button mBtnOK;
	@ViewInject(R.id.btn_cancel)
	private Button mBtnCancel;
	@ViewInject(R.id.layout_control)
	private LinearLayout mLayoutControl;
	@ViewInject(R.id.text_no_image)
	private TextView mTextNoImage;
	
	private Callback mCallback;
	private View mainView;
	private Context mContext;
	private AsyncTask<Void, Void, List<FileInfo>> mLoadPhotoTask;
	private Device mCurrDevice;
	private List<FileInfo> mSelectFileInfos = new LinkedList<>();
	public BackgroundPhotoDialog(Context context, Device device, Callback callback){
		super(context);
		mContext = context;
		mCurrDevice = device;
		mCallback = callback;
	}

	@Override
	public int getLayoutRes() {
		return R.layout.dialog_background_photo;
	}

	@Override
	public void initData() {
		//装载数据
		mLoadPhotoTask = new AsyncTask<Void, Void, List<FileInfo>>(){
			@Override
			protected List<FileInfo> doInBackground(Void... params) {
				FileInfoService fileInfoService = new FileInfoService();
				return fileInfoService.getFileInfos(mCurrDevice.getDeviceID(), ConstData.MediaType.IMAGE);
			}
			
			@Override
			protected void onPostExecute(List<FileInfo> result) {
				//数据加载完毕，更新页面
				if(result != null && result.size() > 0){
					BackgroundPhotoAdapter photoGridAdapter = new BackgroundPhotoAdapter(mContext,  R.layout.adapter_background_photo, result);
					mGridImage.setAdapter(photoGridAdapter);
					mGridImage.setFocusable(true);
					mGridImage.setFocusableInTouchMode(true);
					mGridImage.requestFocus();
				}else{
					mGridImage.setVisibility(View.GONE);
					mLayoutControl.setVisibility(View.GONE);
					mTextNoImage.setVisibility(View.VISIBLE);
				}
			}
		};
		mLoadPhotoTask.execute();
	}

	@Override
	public void initEvent() {
		setOnDismissListener(this);
		mBtnOK.setOnClickListener(this);
		mBtnCancel.setOnClickListener(this);
		mGridImage.setOnItemClickListener(this);
	}
	
	@Override
	public void initView() {
		super.initView();
		mainView = getMainView();
		ViewGroup.LayoutParams mainViewParams = (ViewGroup.LayoutParams)mainView.getLayoutParams();
		//对话框占用3/4屏幕
		mainViewParams.width = DeviceInfoUtils.getScreenWidth(mContext) * 3 / 4;
		mainViewParams.height = DeviceInfoUtils.getScreenHeight(mContext) * 3 / 4;
		getMainView().setLayoutParams(mainViewParams);
	}
	
	@Override
	public void onDismiss(DialogInterface dialog) {
		if(mLoadPhotoTask != null && mLoadPhotoTask.getStatus() == Status.RUNNING)
			mLoadPhotoTask.cancel(true);
	}
	

	@Override
	public void onClick(View v) {
		if(v == mBtnOK){
			if(mSelectFileInfos.size() == 0){
				ToastUtils.showToast(mContext.getString(R.string.sel_back_photo));
				return;
			}
			dismiss();
			mCallback.onFinished(mSelectFileInfos);
		}else if(v == mBtnCancel){
			dismiss();
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		FileInfo itemFileInfo = (FileInfo)parent.getAdapter().getItem(position);
		CheckBox checkBox = (CheckBox)view.findViewById(R.id.check_select);
		checkBox.setChecked(!checkBox.isChecked());
		if(checkBox.isChecked()){
			mSelectFileInfos.add(itemFileInfo);
		}else{
			mSelectFileInfos.remove(itemFileInfo);
		}
	}

}
