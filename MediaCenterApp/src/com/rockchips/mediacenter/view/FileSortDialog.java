package com.rockchips.mediacenter.view;
import org.xutils.view.annotation.ViewInject;
import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import com.rockchips.mediacenter.R;
/**
 * @author GaoFei
 * 文件排序对话框
 */
public class FileSortDialog extends AppBaseDialog{
	private Context mContext;
	
	@ViewInject(R.id.list_sort_way)
	private ListView mListSortWay;
	@ViewInject(R.id.list_sort_type)
	private ListView mListSortType;
	@ViewInject(R.id.btn_ok)
	private Button mBtnOK;
	@ViewInject(R.id.btn_cancel)
	private Button mBtnCancel;
	public FileSortDialog(Context context){
		super(context);
		mContext = context;
	}

	@Override
	public int getLayoutRes() {
		return R.layout.dialog_file_sort;
	}

	@Override
	public void initData() {
		String[] sortWays = mContext.getResources().getStringArray(R.array.file_sort_ways);
		ArrayAdapter<String> sortWayAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_single_choice, android.R.id.text1, sortWays);
		mListSortWay.setAdapter(sortWayAdapter);
		String[] sortTypes = mContext.getResources().getStringArray(R.array.file_sort_ways);
		ArrayAdapter<String> sortTypeAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_single_choice, android.R.id.text1, sortWays);
		mListSortType.setAdapter(sortTypeAdapter);
	}

	@Override
	public void initEvent() {
		
	}
	
}
