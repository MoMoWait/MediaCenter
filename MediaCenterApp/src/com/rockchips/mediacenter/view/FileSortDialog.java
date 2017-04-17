package com.rockchips.mediacenter.view;
import momo.cn.edu.fjnu.androidutils.utils.StorageUtils;

import org.xutils.view.annotation.ViewInject;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import com.rockchips.mediacenter.R;
import com.rockchips.mediacenter.data.ConstData;
/**
 * @author GaoFei
 * 文件排序对话框
 */
public class FileSortDialog extends AppBaseDialog implements View.OnClickListener{
	
	public interface CallBack{
		void onSelected(int way, int type);
	}
	private CallBack mCallBack;
	private Context mContext;
	@ViewInject(R.id.list_sort_way)
	private ListView mListSortWay;
	@ViewInject(R.id.list_sort_type)
	private ListView mListSortType;
	@ViewInject(R.id.btn_ok)
	private Button mBtnOK;
	@ViewInject(R.id.btn_cancel)
	private Button mBtnCancel;
	public FileSortDialog(Context context, CallBack callBack){
		super(context);
		mContext = context;
		mCallBack = callBack;
	}

	@Override
	public int getLayoutRes() {
		return R.layout.dialog_file_sort;
	}

	@Override
	public void initData() {
		String[] sortWays = mContext.getResources().getStringArray(R.array.file_sort_ways);
		ArrayAdapter<String> sortWayAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_single_choice, android.R.id.text1, sortWays);
		mListSortWay.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
		mListSortWay.setAdapter(sortWayAdapter);
		String sortWay = StorageUtils.getDataFromSharedPreference(ConstData.SharedKey.FILE_SORT_WAY);
		if(TextUtils.isEmpty(sortWay)){
			mListSortWay.setItemChecked(ConstData.FILE_SORT_WAY.TYPE, true);
		}else{
			mListSortWay.setItemChecked(Integer.parseInt(sortWay), true);
		}
		String[] sortTypes = mContext.getResources().getStringArray(R.array.file_sort_types);
		ArrayAdapter<String> sortTypeAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_single_choice, android.R.id.text1, sortTypes);
		mListSortType.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
		mListSortType.setAdapter(sortTypeAdapter);
		String sortType = StorageUtils.getDataFromSharedPreference(ConstData.SharedKey.FILE_SORT_TYPE);
		if(TextUtils.isEmpty(sortType)){
			mListSortType.setItemChecked(ConstData.FILE_SORT_TYPE.INCREASING, true);
		}else{
			mListSortType.setItemChecked(Integer.parseInt(sortType), true);
		}
	}

	@Override
	public void initEvent() {
		mBtnOK.setOnClickListener(this);
		mBtnCancel.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		if(v == mBtnOK){
			StorageUtils.saveDataToSharedPreference(ConstData.SharedKey.FILE_SORT_WAY, "" + mListSortWay.getCheckedItemPosition());
			StorageUtils.saveDataToSharedPreference(ConstData.SharedKey.FILE_SORT_TYPE, "" + mListSortType.getCheckedItemPosition());
			mCallBack.onSelected(mListSortWay.getCheckedItemPosition(), mListSortType.getCheckedItemPosition());
			dismiss();
		}else if(v == mBtnCancel){
			dismiss();
		}
	}
}
