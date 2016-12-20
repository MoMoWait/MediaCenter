package com.rockchips.mediacenter.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.xutils.x;
import org.xutils.common.util.DensityUtil;
import org.xutils.image.ImageOptions;

import android.content.Context;
import android.provider.SyncStateContract.Constants;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rockchips.mediacenter.basicutils.bean.LocalMediaInfo;
import com.rockchips.mediacenter.basicutils.constant.Constant;
import com.rockchips.mediacenter.R;
import com.rockchips.mediacenter.data.ConstData;

/**
 * @author GaoFei
 * 图片列表适配器
 */
public class PhotoGridAdapter extends ArrayAdapter<LocalMediaInfo> {
	public static final String TAG = PhotoGridAdapter.class.getSimpleName();
	private int mResourceId = 0;
	private Context mContext;
	private LayoutInflater mInflater;
	private ArrayList<LocalMediaInfo> mObjects;
	private ImageOptions mImageOptions;
	public PhotoGridAdapter(Context context, int resource,
			List<LocalMediaInfo> objects) {
		super(context, resource, objects);
		mObjects = (ArrayList<LocalMediaInfo>)objects;
		mContext = context;
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
		LocalMediaInfo mediaInfo = getItem(position);
		holder.textFileName.setText(mediaInfo.getmFileName());
		holder.textFileCount.setText("" + mediaInfo.getmFiles());
		if(mediaInfo.getmDeviceType() != ConstData.DeviceType.DEVICE_TYPE_DMS){
			File imgFile = new File(mediaInfo.getmParentPath() + "/" + mediaInfo.getmFileName());
			Log.i(TAG, "getView->imgFile.path:" + imgFile.getPath());
			if(imgFile.isDirectory()){
				File[] subFiles = imgFile.listFiles();
				Log.i(TAG, "getView->subFiles:" + Arrays.toString(subFiles));
				if(subFiles != null && subFiles.length > 0){
					//x.image().bind(holder.imgPhoto, subFiles[0].getPath(), mImageOptions, null);
				}
			}else{ 
				holder.textFileCount.setVisibility(View.GONE);
				x.image().bind(holder.imgPhoto, mediaInfo.getmParentPath() + "/" + mediaInfo.getmFileName(), mImageOptions, null);
			}
		}else{
			if(mediaInfo.getmFiles() > 0){
				//x.image().bind(holder.imgPhoto, mediaInfo.getFirstPhotoUrl(), mImageOptions, null);
			}else{
				holder.textFileCount.setVisibility(View.GONE);
				x.image().bind(holder.imgPhoto, mediaInfo.getmResUri(), mImageOptions, null);
			}
		}
		
		
		return convertView;
	}
	
	final class ViewHolder{
		ImageView imgPhoto;
		TextView textFileName;
		TextView textFileCount;
	}
}
