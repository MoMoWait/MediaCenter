package com.rockchip.mediacenter.mediaplayer.ui;

import com.rockchip.mediacenter.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.widget.ProgressBar;
import android.widget.TextView;

public class WaitDialog extends Dialog {
	
	private static final int OPERATE_SHOW = 51;
	private static final int OPERATE_CLOSE = 52;
	private static final int OPERATE_RELEASE = 53;
	private HandlerThread mHandlerTherad = null;
	private Handler mMainHandler = null;
	private Handler mDialogHandler;
	private boolean isQuit = true;
	
    private ProgressBar mProgress;
    private TextView mMessageView;
    private CharSequence mMessage;
	
	public WaitDialog(Context context) {
		super(context, R.style.DialogAlert);
	}
	
	/** 
	 * <p>Title: onCreate</p> 
	 * <p>Description: </p> 
	 * @param savedInstanceState 
	 * @see com.rockchip.mediacenter.plugins.widget.Alert#onCreate(android.os.Bundle) 
	 */
	protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.sys_progress_dialog);
//        LayoutInflater inflater = LayoutInflater.from(getContext());
//		View view = inflater.inflate(R.layout.sys_progress_dialog, null);
        mProgress = (ProgressBar) findViewById(R.id.progress);
        mMessageView = (TextView) findViewById(R.id.message);
//        setView(view);
        if (mMessage != null) {
            setMessage(mMessage);
        }
		super.onCreate(savedInstanceState);
	}
	
	/** 
	 * <p>Title: setTitle</p> 
	 * <p>Description: </p> 
	 * @param title 
	 * @see android.app.Dialog#setTitle(java.lang.CharSequence) 
	 */
	@Override
	public void setTitle(CharSequence title) {
		//unused
	}
	
	public void setMessage(CharSequence message) {
        if (mProgress != null) {
            mMessageView.setText(message);
        } else {
            mMessage = message;
        }
    }
	
	/**
	 * 打开对话框
	 */
	public void showDialogInThread(){
		execute();
	}
	public void showDialogInThread(String title, String msg){
		setTitle(title);
		setMessage(msg);
		execute();
	}
	private void execute(){
		mMainHandler = new Handler(){
			public void handleMessage(Message msg) {
				switch(msg.what){
				case OPERATE_CLOSE:{
					WaitDialog.this.dismiss();
					mDialogHandler.removeMessages(OPERATE_SHOW);
					mDialogHandler.removeMessages(OPERATE_CLOSE);
					break;
				}
				case OPERATE_RELEASE:{
					if(!isQuit){
						isQuit = true;
						mDialogHandler.getLooper().quit();
					}
					break;
				}
				}
			}
		};
		mHandlerTherad = new HandlerThread("dialog");
		mHandlerTherad.setPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
		mHandlerTherad.start();
		mDialogHandler = new DialogHandler(mHandlerTherad.getLooper());
		mDialogHandler.sendEmptyMessage(OPERATE_SHOW);
		isQuit = false;
	}
	
	/** 
	 * <p>Title: onStop</p> 
	 * <p>Description: </p>  
	 * @see android.app.ProgressDialog#onStop() 
	 */
	@Override
	protected void onStop() {
		super.onStop();
		if(mMainHandler!=null)
			mMainHandler.sendEmptyMessageDelayed(OPERATE_RELEASE, 1000);
	}
	
	
	/**
	 * 关闭对话框
	 */
	public void closeDialogInThread(){
		mMainHandler.sendEmptyMessage(OPERATE_CLOSE);
	}
	
	private class DialogHandler extends Handler{
		
		public DialogHandler(Looper looper) {
			super(looper);
		}
		
		public void handleMessage(Message msg) {
			switch(msg.what){
			case OPERATE_SHOW:{
				WaitDialog.this.show();
				break;
			}
			}
		}
	}
}
