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
import com.rockchips.mediacenter.adapter.FolderListAdapter.ViewHolder;
import com.rockchips.mediacenter.bean.LocalMediaFile;
import com.rockchips.mediacenter.data.ConstData;
/**
 * @author GaoFei
 *
 */
public class FileListAdapter extends ArrayAdapter<LocalMediaFile> {
	private int mResourceId = 0;
	private Context mContext;
	private LayoutInflater mInflater;
	private boolean mExtralVisable;
	private ArrayList<LocalMediaFile> mObjects;
	public FileListAdapter(Context context, int resource,List<LocalMediaFile> objects) {
		super(context, resource, objects);
		mResourceId = resource;
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mObjects = (ArrayList<LocalMediaFile>)objects;
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
		LocalMediaFile mediaFile = getItem(position);
		int fileType = mediaFile.getType();
		if(fileType == ConstData.MediaType.VIDEO){
			holder.imgFileIcon.setImageResource(R.drawable.icon_local_video);
		}else if(fileType == ConstData.MediaType.AUDIO){
			holder.imgFileIcon.setImageResource(R.drawable.icon_local_audio);
		}else if(fileType == ConstData.MediaType.IMAGE){
			holder.imgFileIcon.setImageResource(R.drawable.icon_local_image);
		}
		holder.textFileName.setText(mediaFile.getName());
		return convertView;
	}

	
	public ArrayList<LocalMediaFile> getmObjects() {
		return mObjects;
	}
	
	
	final class ViewHolder{
		ImageView imgFileIcon;
		TextView textFileName;
	}
	
}
