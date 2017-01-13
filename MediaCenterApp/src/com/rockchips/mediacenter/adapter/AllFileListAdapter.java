package com.rockchips.mediacenter.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.rockchips.mediacenter.adapter.FileListAdapter.ViewHolder;
import com.rockchips.mediacenter.bean.AllFileInfo;
import com.rockchips.mediacenter.bean.LocalMediaFile;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author GaoFei
 * 所有本地文件适配器
 */
public class AllFileListAdapter extends ArrayAdapter<AllFileInfo> {

	private int mResourceId = 0;
	private LayoutInflater mInflater;
	public AllFileListAdapter(Context context, int resource, List<AllFileInfo> objects) {
		super(context, resource, objects);
		mResourceId = resource;
		mInflater = LayoutInflater.from(context);
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
		/*ImageView fileIconImageView = (ImageView)convertView.findViewById(R.id.img_file_icon);
		TextView fileNamTextView = (TextView)convertView.findViewById(R.id.text_file_name);*/
		AllFileInfo allFileInfo = getItem(position);
		int fileType = allFileInfo.getType();
		if(fileType == ConstData.MediaType.VIDEO){
			holder.imgFileIcon.setImageResource(R.drawable.icon_local_video);
		}else if(fileType == ConstData.MediaType.AUDIO){
			holder.imgFileIcon.setImageResource(R.drawable.icon_local_audio);
		}else if(fileType == ConstData.MediaType.IMAGE){
			holder.imgFileIcon.setImageResource(R.drawable.icon_local_image);
		}else if(fileType == ConstData.MediaType.FOLDER){
			holder.imgFileIcon.setImageResource(R.drawable.icon_local_folder);
		}else if(fileType == ConstData.MediaType.APK || fileType == ConstData.MediaType.UNKNOWN_TYPE){
			holder.imgFileIcon.setImageResource(R.drawable.unknow_file_type);
		}
		holder.textFileName.setText(allFileInfo.getFile().getName());
		return convertView;
	}

	
	
	
	final class ViewHolder{
		ImageView imgFileIcon;
		TextView textFileName;
	}
	
}
