/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Filename:    TransferActivity.java  
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2012-4-16 下午04:25:50  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2012-4-16      fxw         1.0         create
*******************************************************************/   


package com.rockchip.mediacenter.mediaplayer.ui;

import java.io.File;
import java.util.List;

import com.rockchip.mediacenter.DLNAManager;
import com.rockchip.mediacenter.R;
import com.rockchip.mediacenter.common.logging.Log;
import com.rockchip.mediacenter.common.logging.LogFactory;
import com.rockchip.mediacenter.dlna.dmt.DigitalMediaTransfer;
import com.rockchip.mediacenter.dlna.dmt.ITransferListener;
import com.rockchip.mediacenter.dlna.dmt.model.TransferItem;
import com.rockchip.mediacenter.dlna.dmt.model.TransferStatus;
import com.rockchip.mediacenter.dlna.dmt.model.TransferType;
import com.rockchip.mediacenter.mediaplayer.ui.TransferOperateDialogViewBuilder.ITransferOperate;
import com.rockchip.mediacenter.mediaplayer.util.PlayerUtil;
import com.rockchip.mediacenter.plugins.widget.Alert;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.Toast;

/**
 *
 * @author fxw
 * @since 1.0
 */
public class TransferActivity extends Activity implements ITransferListener, AdapterView.OnItemClickListener, ITransferOperate {

	public static final Log logger = LogFactory.getLog(TransferActivity.class);
	public static final int DIALOG_TRANSFER = 1;
	public static final int DIALOG_TRANSFER_DELETE = 2;
	private TransferListView mTransferListView;
	private DigitalMediaTransfer mTransferService;
	private TransferItem mClickedTransferItem;
	private TransferOperateDialogViewBuilder mViewBuilder;
	private DLNAManager dlnaManager;
	
	/** 
	 * <p>Title: onCreate</p> 
	 * <p>Description: </p> 
	 * @param savedInstanceState 
	 * @see android.app.Activity#onCreate(android.os.Bundle) 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setWindowAnimations(R.style.PopupAnimation);
		setContentView(R.layout.dmp_transfer);
		getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		initView();
		dlnaManager = new DLNAManager(this);
		dlnaManager.setBindListener(new DLNAManager.BindListener() {
			public void onBindCompleted() {
				mTransferService = dlnaManager.getDigitalMediaTransfer();
				mTransferService.setTransferListener(TransferActivity.this);
//				String srcURL = "http://192.168.1.102:17679/FileProvider/M$0/O$0/S$/P$JPEG_SM/I$image/jpeg/106";
//				srcURL = "http://192.168.1.102:17679/FileProvider/M$75000/O$1/S$/P$/I$video/avi/607";
//				String destURL = "/mnt/sdcard/tt.jpeg";
//				destURL = "/mnt/sdcard/tt.avi";
//				//mTransferService.upload(new File("/mnt/sdcard/tt.avi"), "http://192.168.1.102:4004/ImportContent?id=cc81d1ad-9ff1-318a-be1b-52183ab");
//				mTransferService.download(srcURL, new File(destURL), "video/avi");
//				final Handler hanlder = new Handler();
//				final String s = srcURL;
//				final String d = destURL;
//				hanlder.postDelayed(new Runnable(){
//					public void run() {
//						mTransferService.download(s, new File(d), "video/avi");
//						hanlder.postDelayed(this, 10000);
//						setDataSource();
//					}
//				}, 5000);
				

				setDataSource();
			}
		});
		dlnaManager.startManager();
	}
	
	private void initView(){
		mTransferListView = (TransferListView)findViewById(R.id.dmp_transfer_list_view);
		mTransferListView.setOnItemClickListener(this);
	}
	
	private void setDataSource(){
		if(mTransferService!=null){
			List<TransferItem> itemList = mTransferService.queryTransferList();
			mTransferListView.setDataSource(itemList);
		}
	}
	
	/** 
	 * <p>Title: onResume</p> 
	 * <p>Description: </p>  
	 * @see android.app.Activity#onResume() 
	 */
	@Override
	protected void onResume() {
		super.onResume();
		if(mTransferService!=null)
			mTransferService.setTransferListener(this);
	}
	
	/** 
	 * <p>Title: onPause</p> 
	 * <p>Description: </p>  
	 * @see android.app.Activity#onPause() 
	 */
	@Override
	protected void onPause() {
		super.onPause();
		if(mTransferService!=null)
			mTransferService.setTransferListener(null);
	}
	
	/** 
	 * <p>Title: onDestroy</p> 
	 * <p>Description: </p>  
	 * @see android.app.Activity#onDestroy() 
	 */
	@Override
	protected void onDestroy() {
		dlnaManager.stopManager();
		super.onDestroy();
	}
	
	/** 
	 * <p>Title: onItemClick</p> 
	 * <p>Description: </p> 
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		mClickedTransferItem = (TransferItem)parent.getItemAtPosition(position);
		showDialog(DIALOG_TRANSFER);
	}
	
	/** 
	 * <p>Title: onPrepareDialog</p> 
	 * <p>Description: </p> 
	 * @param id
	 * @param dialog 
	 * @see android.app.Activity#onPrepareDialog(int, android.app.Dialog) 
	 */
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		if(id==DIALOG_TRANSFER&&mClickedTransferItem!=null){
			if(mViewBuilder!=null){
				mViewBuilder.setTransferItem(mClickedTransferItem);
				mViewBuilder.update();
			}
			return;
		}
		super.onPrepareDialog(id, dialog);
	}
	
	
	/** 
	 * <p>Title: onCreateDialog</p> 
	 * <p>Description: </p> 
	 * @param id
	 * @return 
	 * @see android.app.Activity#onCreateDialog(int) 
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		if(id==DIALOG_TRANSFER&&mClickedTransferItem!=null){
			Alert.Builder builder = new Alert.Builder(this);
			builder.setTitle(R.string.dialog_operate);
			mViewBuilder = new TransferOperateDialogViewBuilder(this);
			mViewBuilder.setTransferItem(mClickedTransferItem);
			mViewBuilder.setTransferOperate(this);
			builder.setView(mViewBuilder.build());
			return builder.create();
		}else if(id==DIALOG_TRANSFER_DELETE){
			Alert.Builder builder = new Alert.Builder(this);
			builder.setTitle(R.string.dmd_key_clear);
			builder.setMessage(R.string.dmd_clear_transfer_msg);
			builder.setPositiveButton(R.string.dmd_clear_transfer_all, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int arg1) {
					mTransferService.deleteAllTransfer();
					setDataSource();
					dialog.dismiss();
				}
			});
			builder.setNeutralButton(R.string.dmd_clear_transfer_succ, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int arg1) {
					mTransferService.deleteAllTransfer(TransferStatus.SUCCESSED);
					setDataSource();
					dialog.dismiss();
				}
			});
			builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int arg1) {
					dialog.dismiss();
				}
			});
			return builder.create();
		}
		return super.onCreateDialog(id);
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==84||keyCode==KeyEvent.KEYCODE_MENU){
			showDialog(DIALOG_TRANSFER_DELETE);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	/** 
	 * <p>Title: onStart</p> 
	 * <p>Description: </p> 
	 * @param item
	 * @param destExists
	 * @param total
	 * @return 
	 */
	@Override
	public boolean onStart(TransferItem item, boolean destExists) {
		return true;
	}

	/** 
	 * <p>Title: onUpdate</p> 
	 * <p>Description: </p> 
	 * @param item
	 * @param readCnt
	 * @param total 
	 */
	@Override
	public void onUpdate(TransferItem item) {
		mTransferListView.updateStatusView(item);
	}

	/** 
	 * <p>Title: onStop</p> 
	 * <p>Description: </p> 
	 * @param item
	 * @param result 
	 */
	@Override
	public void onStop(TransferItem item, boolean result) {
		mTransferListView.updateStatusView(item);
	}

	
	/** 
	 * <p>Title: play</p> 
	 * <p>Description: </p> 
	 * @param item 
	 * @see com.rockchip.mediacenter.mediaplayer.ui.TransferOperateDialogViewBuilder.ITransferOperate#play(com.rockchip.mediacenter.mediaplayer.model.TransferItem) 
	 */
	@Override
	public void play(TransferItem item) {
		String url = null;
		if(TransferType.DOWNLOAD==item.getTransferType()){
			url = item.getDestURL();
			if(!new File(url).exists()){
				Toast.makeText(this, R.string.dmd_file_not_exitsted, Toast.LENGTH_LONG).show();
				return;
			}
		}else{
			url = item.getSourceURL();
		}
		PlayerUtil.startPlayer(this, item.getTitle(), url, item.getMimeType());
		dismissDialog(DIALOG_TRANSFER);
	}

	/** 
	 * <p>Title: delete</p> 
	 * <p>Description: </p> 
	 * @param item 
	 * @see com.rockchip.mediacenter.mediaplayer.ui.TransferOperateDialogViewBuilder.ITransferOperate#delete(com.rockchip.mediacenter.mediaplayer.model.TransferItem) 
	 */
	@Override
	public void delete(TransferItem item) {
		mTransferService.deleteTransfer(item.getId());
		setDataSource();
		dismissDialog(DIALOG_TRANSFER);
	}

	/** 
	 * <p>Title: stop</p> 
	 * <p>Description: </p> 
	 * @param item 
	 * @see com.rockchip.mediacenter.mediaplayer.ui.TransferOperateDialogViewBuilder.ITransferOperate#stop(com.rockchip.mediacenter.mediaplayer.model.TransferItem) 
	 */
	@Override
	public void stop(TransferItem item) {
		mTransferService.stopTransfer(item.getId());
		if(item.getTransferStatus()!=TransferStatus.SUCCESSED)
			item.setTransferStatus(TransferStatus.FAILED);
		mTransferListView.updateStatusView(item);
		dismissDialog(DIALOG_TRANSFER);
	}

	/** 
	 * <p>Title: retry</p> 
	 * <p>Description: </p> 
	 * @param item 
	 * @see com.rockchip.mediacenter.mediaplayer.ui.TransferOperateDialogViewBuilder.ITransferOperate#retry(com.rockchip.mediacenter.mediaplayer.model.TransferItem) 
	 */
	@Override
	public void retry(TransferItem item) {
		mTransferService.continueTransfer(item.getId());
		item.setTransferStatus(TransferStatus.CONTINUE);
		mTransferListView.updateStatusView(item);
		dismissDialog(DIALOG_TRANSFER);
	}
	
}
