package com.rockchips.mediacenter.adapter;

import java.util.ArrayList;
import java.util.List;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.rockchips.mediacenter.R;
import com.rockchips.mediacenter.bean.LocalMediaFolder;
/**
 * @author GaoFei
 * 文件夹列表适配器
 */
public class FolderListAdapter extends ArrayAdapter<LocalMediaFolder> {
	private int mResourceId = 0;
	private Context mContext;
	private LayoutInflater mInflater;
	private List<LocalMediaFolder> mObjects;
	public FolderListAdapter(Context context, int resource,
			List<LocalMediaFolder> objects) {
		super(context, resource, objects);
		mResourceId = resource;
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
		mObjects = objects;
	}

	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null){
			convertView = mInflater.inflate(mResourceId, parent, false);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.imgFileIcon =  (ImageView)convertView.findViewById(R.id.img_file_icon);
			viewHolder.textFileName = (TextView)convertView.findViewById(R.id.text_file_name);
			convertView.setTag(viewHolder);
		}
		ViewHolder holder = (ViewHolder)convertView.getTag();
		LocalMediaFolder mediaFolder = getItem(position);
		int fileType = mediaFolder.getFolderType();
		holder.imgFileIcon.setImageResource(R.drawable.icon_local_folder);
		holder.textFileName.setText(mediaFolder.getName());
		return convertView;
	}
	
	
	final class ViewHolder{
		ImageView imgFileIcon;
		TextView textFileName;
	}
}
