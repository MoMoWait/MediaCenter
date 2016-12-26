package com.rockchips.mediacenter.adapter;

import java.util.List;

import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;

import com.rockchips.mediacenter.adapter.AllFileListAdapter.ViewHolder;
import com.rockchips.mediacenter.bean.AllFileInfo;
import com.rockchips.mediacenter.bean.AllUpnpFileInfo;
import com.rockchips.mediacenter.data.ConstData;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.rockchips.mediacenter.R;
/**
 * @author GaoFei
 * 所有Upnp文件列表适配器
 */
public class AllUpnpFileListAdapter extends ArrayAdapter<AllUpnpFileInfo> {
	private int mResourceId = 0;
	private LayoutInflater mInflater;
	public AllUpnpFileListAdapter(Context context, int resource, List<AllUpnpFileInfo> objects) {
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
		AllUpnpFileInfo upnpFileInfo = getItem(position);
		int fileType = upnpFileInfo.getType();
		if(fileType == ConstData.MediaType.VIDEO){
			holder.imgFileIcon.setImageResource(R.drawable.icon_local_video);
		}else if(fileType == ConstData.MediaType.AUDIO){
			holder.imgFileIcon.setImageResource(R.drawable.icon_local_audio);
		}else if(fileType == ConstData.MediaType.IMAGE){
			holder.imgFileIcon.setImageResource(R.drawable.icon_local_image);
		}else if(fileType == ConstData.MediaType.FOLDER){
			holder.imgFileIcon.setImageResource(R.drawable.icon_local_folder);
		}
		Object object = upnpFileInfo.getFile();
		if(object instanceof Item){
			holder.textFileName.setText(((Item)object).getTitle());
		}else{
			holder.textFileName.setText(((Container)object).getTitle());
		}
		return convertView;
	}

	
	final class ViewHolder{
		ImageView imgFileIcon;
		TextView textFileName;
	}
	
}
