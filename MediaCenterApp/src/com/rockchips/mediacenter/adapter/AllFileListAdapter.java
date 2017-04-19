package com.rockchips.mediacenter.adapter;
import java.util.List;
import com.rockchips.mediacenter.bean.FileInfo;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author GaoFei
 * 所有本地文件适配器
 */
public class AllFileListAdapter extends ArrayAdapter<FileInfo> {

	private int mResourceId = 0;
	private LayoutInflater mInflater;
	private boolean mIsDeleteMode;
	public AllFileListAdapter(Context context, int resource, List<FileInfo> objects) {
		super(context, resource, objects);
		mResourceId = resource;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null){
			convertView = mInflater.inflate(mResourceId, parent, false);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.checkSelect = (CheckBox)convertView.findViewById(R.id.check_select);
			if(mIsDeleteMode)
				viewHolder.checkSelect.setVisibility(View.VISIBLE);
			viewHolder.imgFileIcon =  (ImageView)convertView.findViewById(R.id.img_file_icon);
			viewHolder.textFileName = (TextView)convertView.findViewById(R.id.text_file_name);
			convertView.setTag(viewHolder);
		}
		ViewHolder holder = (ViewHolder)convertView.getTag();
		FileInfo fileInfo = getItem(position);
		int fileType = fileInfo.getType();
		if(fileType == ConstData.MediaType.VIDEO){
			holder.imgFileIcon.setImageResource(R.drawable.icon_local_video);
		}else if(fileType == ConstData.MediaType.AUDIO){
			holder.imgFileIcon.setImageResource(R.drawable.icon_local_audio);
		}else if(fileType == ConstData.MediaType.IMAGE){
			holder.imgFileIcon.setImageResource(R.drawable.icon_local_image);
		}else if(fileType == ConstData.MediaType.FOLDER){
			holder.imgFileIcon.setImageResource(R.drawable.icon_local_folder);
		}else if(fileType == ConstData.MediaType.APK){
			holder.imgFileIcon.setImageResource(R.drawable.icon_file_apk);
		}else{
			holder.imgFileIcon.setImageResource(R.drawable.unknow_file_type);
		}
		holder.textFileName.setText(fileInfo.getName());
		holder.checkSelect.setChecked(fileInfo.isSelectDelete());
		return convertView;
	}
	
	public void setIsDeleteMode(boolean isDeleteMode){
		mIsDeleteMode = isDeleteMode;
	}
	
	final class ViewHolder{
		CheckBox checkSelect;
		ImageView imgFileIcon;
		TextView textFileName;
	}
	
}
