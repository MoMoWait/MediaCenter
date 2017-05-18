/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Filename:    TransferOperateDialogView.java  
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2012-6-16 下午02:35:15  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2012-6-16      fxw         1.0         create
*******************************************************************/   


package com.rockchip.mediacenter.mediaplayer.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.rockchip.mediacenter.R;
import com.rockchip.mediacenter.dlna.dmt.model.TransferItem;
import com.rockchip.mediacenter.dlna.dmt.model.TransferStatus;
import com.rockchip.mediacenter.mediaplayer.util.Converter;

/**
 *
 * @author fxw
 * @since 1.0
 */
public class TransferOperateDialogViewBuilder implements View.OnClickListener {

	private LayoutInflater mLayoutInflater;
	private TransferItem mTransferItem;
	private View mDialogView;
	private TextView mTitleView;
	private TextView mFileSizeView;
	private TextView mSrcView;
	private TextView mDestView;
	private Button mPlayButton;
	private Button mRetryButton;
	private Button mStopButton;
	private Button mDeleteButton;
	private ITransferOperate mTransferOperate;
	
	public TransferOperateDialogViewBuilder(Context context){
		mLayoutInflater = LayoutInflater.from(context);
	}
	
	public View build(){
		if(mDialogView==null){
			mDialogView = mLayoutInflater.inflate(R.layout.dmp_transfer_dialog, null);
			mTitleView = (TextView)mDialogView.findViewById(R.id.dmp_transfer_dialog_title);
			mFileSizeView = (TextView)mDialogView.findViewById(R.id.dmp_transfer_dialog_filesize);
			mSrcView = (TextView)mDialogView.findViewById(R.id.dmp_transfer_dialog_src);
			mDestView = (TextView)mDialogView.findViewById(R.id.dmp_transfer_dialog_dest);
			mPlayButton = (Button)mDialogView.findViewById(R.id.dmp_transfer_dialog_btn_play);
			mRetryButton = (Button)mDialogView.findViewById(R.id.dmp_transfer_dialog_btn_retry);
			mStopButton = (Button)mDialogView.findViewById(R.id.dmp_transfer_dialog_btn_stop);
			mDeleteButton = (Button)mDialogView.findViewById(R.id.dmp_transfer_dialog_btn_delete);
			mPlayButton.setOnClickListener(this);
			mRetryButton.setOnClickListener(this);
			mStopButton.setOnClickListener(this);
			mDeleteButton.setOnClickListener(this);
		}
		update();
		return mDialogView;
	}

	public void update(){
		if(mDialogView==null){
			throw new IllegalStateException("It must call build() method before. ");
		}
		if(mTransferItem!=null){
			mTitleView.setText(mTransferItem.getTitle());
			mFileSizeView.setText(Converter.convertSizetoStr(mTransferItem.getFileSize()));
			mSrcView.setText(mTransferItem.getSourceName());
			mDestView.setText(mTransferItem.getDestName());
			TransferStatus status = mTransferItem.getTransferStatus();
			if(status==TransferStatus.WAITING||status==TransferStatus.TRANSFERING
					||status==TransferStatus.CONTINUE||status==TransferStatus.RENEW){
				mPlayButton.setVisibility(View.GONE);
				mRetryButton.setVisibility(View.GONE);
				mStopButton.setVisibility(View.VISIBLE);
			}else if(status==TransferStatus.SUCCESSED){
				mPlayButton.setVisibility(View.VISIBLE);
				mRetryButton.setVisibility(View.GONE);
				mStopButton.setVisibility(View.GONE);
			}else if(status==TransferStatus.FAILED){
				mPlayButton.setVisibility(View.GONE);
				mRetryButton.setVisibility(View.VISIBLE);
				mStopButton.setVisibility(View.GONE);
			}
		}
	}

	public TransferItem getTransferItem() {
		return mTransferItem;
	}

	public void setTransferItem(TransferItem mTransferItem) {
		this.mTransferItem = mTransferItem;
	}
	
	public void setTransferOperate(ITransferOperate transferOperate) {
		this.mTransferOperate = transferOperate;
	}
	
	/** 
	 * <p>Title: onClick</p> 
	 * <p>Description: </p> 
	 * @param arg0 
	 * @see android.view.View.OnClickListener#onClick(android.view.View) 
	 */
	@Override
	public void onClick(View view) {
		if(mTransferOperate==null) return;
		int viewId = view.getId();
		if(viewId == R.id.dmp_transfer_dialog_btn_play){
			mTransferOperate.play(mTransferItem);
		}else if(viewId == R.id.dmp_transfer_dialog_btn_retry){
			mTransferOperate.retry(mTransferItem);
		}else if(viewId == R.id.dmp_transfer_dialog_btn_stop){
			mTransferOperate.stop(mTransferItem);
		}else if(viewId == R.id.dmp_transfer_dialog_btn_delete){
			mTransferOperate.delete(mTransferItem);
		}
	}
	
	
	public interface ITransferOperate {
		public void play(TransferItem item);
		public void delete(TransferItem item);
		public void stop(TransferItem item);
		public void retry(TransferItem item);
	}
	
}
