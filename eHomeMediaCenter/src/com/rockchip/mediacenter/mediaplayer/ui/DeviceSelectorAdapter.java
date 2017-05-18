/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Filename:    DeviceSelectorAdapter.java  
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2012-6-21 下午03:34:50  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2012-6-21      fxw         1.0         create
*******************************************************************/   


package com.rockchip.mediacenter.mediaplayer.ui;

import java.util.List;

import com.rockchip.mediacenter.R;
import com.rockchip.mediacenter.dlna.model.DeviceItem;
import com.rockchip.mediacenter.plugins.imageloader.ImageLoader;
import com.rockchip.mediacenter.plugins.imageloader.ImageLoader.BitmapCallback;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

/**
 *
 * @author fxw
 * @since 1.0
 */
public class DeviceSelectorAdapter extends ArrayAdapter<DeviceItem> implements BitmapCallback {

	private LayoutInflater mInflater;
	private ImageLoader mImageLoader;
	private View mSelectedView;
	private int mSelectedIndex = -1;
	private ListView mListView;
	private List<DeviceItem> mDeviceItemList;
	
	public DeviceSelectorAdapter(Context context, List<DeviceItem> deviceItemList, ImageLoader imageLoader) {
		super(context, 0, deviceItemList);
		mDeviceItemList = deviceItemList;
		mImageLoader = imageLoader;
		mInflater = LayoutInflater.from(context);
	}
	
	public void setListView(ListView listView){
		this.mListView = listView;
	}
	
	public void setDataSource(List<DeviceItem> deviceItemList){
		mDeviceItemList.clear();
		mDeviceItemList.addAll(deviceItemList);
		notifyDataSetChanged();
	}
	
	public void setSelectedIndex(int index){
		this.mSelectedIndex = index;
	}
	public int getSelectedIndex(){
		return this.mSelectedIndex;
	}
	
	public DeviceItem getSelctedItem(){
		if(mSelectedIndex>=0&&mSelectedIndex<mDeviceItemList.size()){
			return getItem(mSelectedIndex);
		}else{
			return null;
		}
	}
	
	public void setSelectedView(View view){
		ViewHolder holder = null;
		if(mSelectedView!=null){
			holder = (ViewHolder)mSelectedView.getTag();
			holder.rb.setChecked(false);
		}
		holder = (ViewHolder)view.getTag();
		holder.rb.setChecked(true);
		this.mSelectedView = view;
	}
	public View getSelectedView(){
		return this.mSelectedView;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if(convertView==null){
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.dms_share_choice_item, null);
			viewHolder.iconView = (ImageView)convertView.findViewById(R.id.dms_img_share_item);
			viewHolder.textView = (TextView)convertView.findViewById(R.id.dms_tv_share_name);
			viewHolder.rb = (RadioButton)convertView.findViewById(R.id.dms_rb_share);
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder)convertView.getTag();
		}
		final DeviceItem deviceItem = getItem(position);
		viewHolder.iconView.setBackgroundResource(R.drawable.dmp_device_item);
		if(deviceItem.getLargestIconURL()!=null){
			viewHolder.iconView.setTag(deviceItem.getLargestIconURL());
			Handler handler = mListView.getHandler();
			if(handler!=null){
				handler.postDelayed(new Runnable(){
					public void run() {
						mImageLoader.load(deviceItem.getLargestIconURL(), DeviceSelectorAdapter.this);
					}
				}, 10);
			}
			mImageLoader.load(deviceItem.getLargestIconURL(), this);
		}
		viewHolder.textView.setText(deviceItem.getFriendlyName());
		if(mSelectedIndex==position){
			viewHolder.rb.setChecked(true);
			mSelectedView = convertView;
		}else{
			viewHolder.rb.setChecked(false);
		}
		return convertView;
	}
	
	public final class ViewHolder {
		ImageView iconView;
		TextView textView;
		RadioButton rb;
	}


	/** 
	 * <p>Title: onLoaded</p> 
	 * <p>Description: </p> 
	 * @param arg0
	 * @param arg1 
	 * @see com.rockchip.mediacenter.plugins.imageloader.ImageLoader.BitmapCallback#onLoaded(java.lang.String, android.graphics.Bitmap) 
	 */
	@Override
	public void onLoaded(String url, Bitmap bitmap) {
		for(int i=0; i<mListView.getChildCount(); i++){
			View view = mListView.getChildAt(i);
			if(view.getTag()!=null){
				ViewHolder holder = (ViewHolder) view.getTag();
				Object tag = holder.iconView.getTag();
				if(url.equals(tag)){
					holder.iconView.setBackgroundDrawable(new BitmapDrawable(bitmap));
					break;
				}
			}
		}
		
	}
	
	public void onError(String arg0, Throwable arg1) {
		
	}

}
