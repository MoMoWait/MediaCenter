/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Filename:    TransferListAdapter.java  
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2012-6-7 上午10:53:31  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2012-6-7      fxw         1.0         create
*******************************************************************/   


package com.rockchip.mediacenter.mediaplayer.ui;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rockchip.mediacenter.R;
import com.rockchip.mediacenter.core.dlna.enumeration.MediaClassType;
import com.rockchip.mediacenter.dlna.dmt.model.TransferItem;
import com.rockchip.mediacenter.dlna.dmt.model.TransferStatus;
import com.rockchip.mediacenter.mediaplayer.util.Converter;

/**
 *
 * @author fxw
 * @since 1.0
 */
public class TransferListAdapter extends ArrayAdapter<TransferItem> {

	private LayoutInflater mLayoutInflater;
	private List<TransferItem> mDataList;
	
	public TransferListAdapter(Context context, List<TransferItem> objects) {
		super(context, 0, objects);
		mDataList = objects;
		mLayoutInflater = LayoutInflater.from(context);
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		TransferViewHolder  viewHolder;
		if(convertView==null){
			convertView = mLayoutInflater.inflate(R.layout.dmp_transfer_list_item, null);
			viewHolder = new TransferViewHolder(convertView);
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (TransferViewHolder)convertView.getTag();
		}
		TransferItem item = getItem(position);
		viewHolder.setValue(item);
		return convertView;
	}
	
	/**
	 * @return the mDataList
	 */
	public List<TransferItem> getDataList() {
		return mDataList;
	}
	
	
	class TransferViewHolder {
		public TransferItem transferItem;
		public ImageView imgView;
		public TextView titleView;
		public TextView srcView;
		public TextView destView;
		public TextView statusView;
		
		public TransferViewHolder(View convertView){
			imgView = (ImageView)convertView.findViewById(R.id.dmp_transfer_mime);
			titleView = (TextView)convertView.findViewById(R.id.dmp_transfer_title);
			srcView = (TextView)convertView.findViewById(R.id.dmp_transfer_src);
			destView = (TextView)convertView.findViewById(R.id.dmp_transfer_dest);
			statusView = (TextView)convertView.findViewById(R.id.dmp_transfer_status);
		}
		
		public void setValue(TransferItem item){
			MediaClassType type = item.getMediaClassType();
			if(type == MediaClassType.VIDEO){
				imgView.setImageResource(R.drawable.dmp_transfer_video);
			}else if(type == MediaClassType.AUDIO){
				imgView.setImageResource(R.drawable.dmp_transfer_audio);
			}else if(type == MediaClassType.IMAGE){
				imgView.setImageResource(R.drawable.dmp_transfer_image);
			}else{
				imgView.setImageResource(R.drawable.dmp_transfer_unknow);
			}
			transferItem = item;
			titleView.setText(item.getTitle());
			srcView.setText(item.getSourceName());
			destView.setText(item.getDestName());
			updateStatus(item);
		}
		
		public void updateStatus(TransferItem item){
			transferItem.update(item);
			TransferStatus status = item.getTransferStatus();
			if(status == TransferStatus.WAITING || status == TransferStatus.CONTINUE || status == TransferStatus.RENEW){
				statusView.setText(R.string.dmp_transfer_status_waiting);
			}else if(status == TransferStatus.SUCCESSED){
				statusView.setText(R.string.dmp_transfer_status_succ);
			}else if(status == TransferStatus.FAILED){
				statusView.setText(R.string.dmp_transfer_status_fail);
			}else{
				statusView.setText(Converter.convertSizetoStr(item.getTransferSize())+"/"+Converter.convertSizetoStr(item.getFileSize()));
			}
		}
	}

}
