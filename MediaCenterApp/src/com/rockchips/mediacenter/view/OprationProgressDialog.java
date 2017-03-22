/**
 * 
 */
package com.rockchips.mediacenter.view;
import org.xutils.view.annotation.ViewInject;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.rockchips.mediacenter.R;
/**
 * @author GaoFei
 *
 */
public class OprationProgressDialog extends AppBaseDialog{
	public interface Callback{
		//停止操作
		void onStop();
	}
	@ViewInject(R.id.progress_loading)
	private ProgressBar mProgressLoading;
	@ViewInject(R.id.text_progress_num)
	private TextView mTextProgressNum;
	@ViewInject(R.id.btn_stop)
	private Button mBtnStop;
	private Callback mCallback;
	public OprationProgressDialog(Context context, Callback callback) {
		super(context);
		mCallback = callback;
	}

	@Override
	public int getLayoutRes() {
		return R.layout.dialog_opration_progress;
	}

	@Override
	public void initData() {
	}

	@Override
	public void initEvent() {
		mBtnStop.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mCallback.onStop();
			}
		});
	}

	/**
	 * 更新进度
	 * @param value
	 */
	public void updateProgress(int value){
		mTextProgressNum.setText("" + value);
		mProgressLoading.setProgress(value);
	}
	
}
