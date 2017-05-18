/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Filename:    TransferListView.java  
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2012-6-8 上午10:47:54  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2012-6-8      fxw         1.0         create
*******************************************************************/   


package com.rockchip.mediacenter.mediaplayer.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

import com.rockchip.mediacenter.dlna.dmt.model.TransferItem;
import com.rockchip.mediacenter.mediaplayer.ui.TransferListAdapter.TransferViewHolder;

/**
 *
 * @author fxw
 * @since 1.0
 */
public class TransferListView extends ListView {

	private List<TransferItem> mTransferList = new ArrayList<TransferItem>();
	private TransferListAdapter mTransferListAdapter;
	
	public TransferListView(Context context) {
		this(context, null);
	}
	public TransferListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mTransferListAdapter = new TransferListAdapter(context, mTransferList);
		setAdapter(mTransferListAdapter);
	}
	public TransferListView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public synchronized void setDataSource(List<TransferItem> transferList){
		mTransferList.clear();
		mTransferList.addAll(transferList);
		mTransferListAdapter.notifyDataSetChanged();
	}
	
	public void updateStatusView(final TransferItem item){
		for(int i=0; i<getChildCount(); i++){
			View view = getChildAt(i);
			final TransferViewHolder viewHolder = (TransferViewHolder)view.getTag();
			if(viewHolder.transferItem.getId() == item.getId()&&getHandler()!=null){
				getHandler().post(new Runnable(){
					public void run() {
						viewHolder.updateStatus(item);
					}
				});
				break;
			}
		}
	}

}
