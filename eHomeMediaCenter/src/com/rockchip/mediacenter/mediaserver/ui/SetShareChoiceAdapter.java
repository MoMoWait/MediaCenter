/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Filename:    SetShareDialogAdapter.java  
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2012-4-15 下午07:00:42  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2012-4-15      fxw         1.0         create
*******************************************************************/   


package com.rockchip.mediacenter.mediaserver.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rockchip.mediacenter.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

/**
 *
 * @author fxw
 * @since 1.0
 */
public class SetShareChoiceAdapter extends BaseAdapter  {
	
	private List<String> choiceList = new ArrayList<String>();
	private Map<Integer, Boolean> selectedMap = new HashMap<Integer, Boolean>();
	private int mFocusPosition = -1;
	
	private LayoutInflater mInflater;
	private Context mContext;
	
	public SetShareChoiceAdapter(Context context) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
		choiceList.add(context.getString(R.string.dms_set_share_mediastore));
		choiceList.add(context.getString(R.string.dms_set_share_folder));
		selectedMap.put(0, false);
		selectedMap.put(1, false);
	}
	
	/**
	 * 设置选择项
	 * @param position
	 */
	public void setSelectedIndex(int position){
		clearSelected();
		selectedMap.put(position, true);
	}
	
	/**
	 * 获取选择项
	 * @return
	 */
	public int getSelectedIndex(){
		for(Integer i=0; i<selectedMap.size(); i++){
			if(selectedMap.get(i)){
				return i;
			}
		}
		return -1;
	}
	
	public void setFocusPosition(int position){
		mFocusPosition = position;
	}
	
	
	/**
	 * 清除选择
	 */
	public void clearSelected(){
		for(Integer i=0; i<selectedMap.size(); i++){
			selectedMap.put(i, false);
		}
	}

	public int getCount() {
		return choiceList.size();
	}

	public String getItem(int i) {
		return choiceList.get(i);
	}


	public long getItemId(int i) {
		return 0;
	}
	
	/** 
	 * <p>Title: getView</p> 
	 * <p>Description: </p> 
	 * @param position
	 * @param convertView
	 * @param parent
	 * @return 
	 * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup) 
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView==null)
			convertView = mInflater.inflate(R.layout.dms_share_choice_item, null);
		ImageView iconView = (ImageView)convertView.findViewById(R.id.dms_img_share_item);
		TextView textView = (TextView)convertView.findViewById(R.id.dms_tv_share_name);
		RadioButton rb = (RadioButton)convertView.findViewById(R.id.dms_rb_share);
		textView.setText(getItem(position));
		if(position==0){
			iconView.setImageResource(R.drawable.dms_media_store);
		}else if(position==1){
			iconView.setImageResource(R.drawable.dmp_folder_item);
		}
		if(selectedMap.get(position)){
			textView.setTextColor(mContext.getResources().getColor(R.color.item_desc_color));
			rb.setChecked(true);
		}else{
			textView.setTextColor(mContext.getResources().getColor(R.color.item_color));
			rb.setChecked(false);
		}
		if(position==mFocusPosition){
			convertView.setBackgroundResource(R.drawable.sys_dialog_item_shape);
		}else{
			convertView.setBackgroundDrawable(null);
		}
		return convertView;
	}

}
