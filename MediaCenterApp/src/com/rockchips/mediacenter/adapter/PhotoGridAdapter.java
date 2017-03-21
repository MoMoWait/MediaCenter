package com.rockchips.mediacenter.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.xutils.x;
import org.xutils.common.util.DensityUtil;
import org.xutils.image.ImageOptions;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.rockchips.mediacenter.bean.FileInfo;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.R;

/**
 * @author GaoFei
 * 图片列表适配器
 */
public class PhotoGridAdapter extends ArrayAdapter<FileInfo> {
	public static final String TAG = PhotoGridAdapter.class.getSimpleName();
	private int mResourceId = 0;
	private LayoutInflater mInflater;
	private ImageOptions mImageOptions;
	public PhotoGridAdapter(Context context, int resource,
			List<FileInfo> objects) {
		super(context, resource, objects);
		mResourceId = resource;
		mInflater = LayoutInflater.from(context);
		mImageOptions = new ImageOptions.Builder()
        .setSize(DensityUtil.dip2px(100), DensityUtil.dip2px(100))
        .setRadius(DensityUtil.dip2px(5))
        // 如果ImageView的大小不是定义为wrap_content, 不要crop.
        .setCrop(true) // 很多时候设置了合适的scaleType也不需要它.
        // 加载中或错误图片的ScaleType
        //.setPlaceholderScaleType(ImageView.ScaleType.MATRIX)
        .setImageScaleType(ImageView.ScaleType.FIT_XY)
        .setLoadingDrawableId(R.drawable.image_browser_default)
        .setFailureDrawableId(R.drawable.image_browser_default)
        .build();

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null){
			convertView = mInflater.inflate(mResourceId, parent, false);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.imgPhoto = (ImageView)convertView.findViewById(R.id.img_photo);
			viewHolder.textFileName = (TextView)convertView.findViewById(R.id.text_file_name);
			viewHolder.textFileCount = (TextView)convertView.findViewById(R.id.text_file_count);
			convertView.setTag(viewHolder);
		}
		ViewHolder holder = (ViewHolder)convertView.getTag();
		FileInfo fileInfo = getItem(position);
		holder.textFileName.setText(fileInfo.getName());
		holder.textFileCount.setText("" + fileInfo.getImageCount());
		holder.textFileCount.setVisibility(View.GONE);
		if(fileInfo.getType() == ConstData.MediaType.IMAGE)
			x.image().bind(holder.imgPhoto, fileInfo.getPath(), mImageOptions, null);
		else
			holder.textFileCount.setVisibility(View.VISIBLE);
		return convertView;
	}
	
	final class ViewHolder{
		ImageView imgPhoto;
		TextView textFileName;
		TextView textFileCount;
	}
}
