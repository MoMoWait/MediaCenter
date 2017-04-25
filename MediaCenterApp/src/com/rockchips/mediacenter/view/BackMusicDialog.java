package com.rockchips.mediacenter.view;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import momo.cn.edu.fjnu.androidutils.utils.DeviceInfoUtils;
import momo.cn.edu.fjnu.androidutils.utils.ToastUtils;
import org.xutils.view.annotation.ViewInject;
import com.rockchips.mediacenter.R;
import com.rockchips.mediacenter.adapter.BackMusicAdapter;
import com.rockchips.mediacenter.bean.FileInfo;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.modle.db.FileInfoService;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * @author GaoFei
 * 背景音乐选择对话框
 */
public class BackMusicDialog extends AppBaseDialog implements OnClickListener, OnItemClickListener{

	
	public interface Callback{
		void onFinished(List<FileInfo> fileInfos);
	}
	
	@ViewInject(R.id.list_music)
	private ListView mListMusic;
	@ViewInject(R.id.btn_ok)
	private Button mBtnOK;
	@ViewInject(R.id.btn_cancel)
	private Button mBtnCancel;
	@ViewInject(R.id.layout_content)
	private LinearLayout mLayouContent;
	@ViewInject(R.id.text_no_music)
	private TextView mTextNoMusic;
	private Callback mCallback;
	private View mainView;
	private Context mContext;
	private AsyncTask<Void, Void, List<FileInfo>> mLoadMusicTask;
	private List<FileInfo> mSelectFileInfos = new LinkedList<>();
	private List<FileInfo> mAllFileInfos;
	private boolean mIsOK;
	public BackMusicDialog(Context context, Callback callback){
		super(context);
		mContext = context;
		mCallback = callback;
	}

	@Override
	public int getLayoutRes() {
		return R.layout.dialog_back_music;
	}

	@Override
	public void initData() {
		//装载数据
		mLoadMusicTask = new AsyncTask<Void, Void, List<FileInfo>>(){
			@Override
			protected List<FileInfo> doInBackground(Void... params) {
				FileInfoService fileInfoService = new FileInfoService();
				mAllFileInfos = fileInfoService.getLocalFileInfos(ConstData.MediaType.AUDIO);
				List<String> strList = new ArrayList<>();
				if(mAllFileInfos != null && mAllFileInfos.size() > 0){
					for(FileInfo itemFileInfo : mAllFileInfos){
						strList.add(itemFileInfo.getName());
					}
				}
				return mAllFileInfos;
			}
			
			@Override
			protected void onPostExecute(List<FileInfo> result) {
				//数据加载完毕，更新页面
				if(result != null && result.size() > 0){
					BackMusicAdapter adapter = new BackMusicAdapter(mContext, android.R.layout.simple_list_item_multiple_choice, android.R.id.text1, mAllFileInfos);
					mListMusic.setAdapter(adapter);
					mListMusic.requestFocus();
					//mListMusic.setSelection(0);
				}else{
					mLayouContent.setVisibility(View.GONE);
					mTextNoMusic.setVisibility(View.VISIBLE);
				}
			}
		};
		mLoadMusicTask.execute();
	}

	@Override
	public void initEvent() {
		mBtnOK.setOnClickListener(this);
		mBtnCancel.setOnClickListener(this);
		mListMusic.setOnItemClickListener(this);
		setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface dialog) {

				if(mLoadMusicTask != null && mLoadMusicTask.getStatus() == Status.RUNNING)
					mLoadMusicTask.cancel(true);
			
				if(mIsOK)
					mCallback.onFinished(mSelectFileInfos);
				else
					mCallback.onFinished(null);
			}
		});
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
	public void onClick(View v) {
		if(v == mBtnOK){
			if(mSelectFileInfos.size() == 0){
				ToastUtils.showToast(mContext.getString(R.string.sel_back_photo));
				return;
			}
			mIsOK = true;
			dismiss();
		}else if(v == mBtnCancel){
			mIsOK = false;
			dismiss();
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		FileInfo itemFileInfo = mAllFileInfos.get(position);
		CheckedTextView checkedTextView = (CheckedTextView)view;
		checkedTextView.setChecked(!checkedTextView.isChecked());
		if(checkedTextView.isChecked()){
			if(mSelectFileInfos.size() == ConstData.MAX_BACK_MUSIC_COUNT){
				//提示最多只能选择20首背景音乐
				ToastUtils.showToast(mContext.getString(R.string.max_sel_music_tip));
			}else{
				mSelectFileInfos.add(itemFileInfo);
			}
		}else{
			mSelectFileInfos.remove(itemFileInfo);
		}
	}

	public void setIsOK(boolean isOK){
		mIsOK = isOK;
	}

}
