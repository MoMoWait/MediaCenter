/**
 * 
 */
package com.rockchips.mediacenter.adapter;

import java.util.List;

import com.rockchips.mediacenter.bean.FileInfo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;

/**
 * @author GaoFei
 * 背景音乐适配器
 */
public class BackMusicAdapter extends ArrayAdapter<FileInfo>{
	private LayoutInflater mInflater;
	private int mResource;
	public BackMusicAdapter(Context context, int resource,
			int textViewResourceId, List<FileInfo> objects) {
		super(context, resource, textViewResourceId, objects);
		mInflater = LayoutInflater.from(context);
		mResource = resource;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		CheckedTextView itemView;
		if(convertView == null){
			itemView = (CheckedTextView)mInflater.inflate(mResource, parent, false);
		}else{
			itemView = (CheckedTextView)convertView;
		}
		itemView.setText(getItem(position).getName());
		itemView.setChecked(getItem(position).isSelectDelete());
		return itemView;
	}
	

}
