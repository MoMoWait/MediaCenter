/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Filename:    MediaListAdapter.java  
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2012-4-6 下午12:04:29  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2012-4-6      fxw         1.0         create
*******************************************************************/   


package com.rockchip.mediacenter.mediaplayer.ui;

import java.util.List;

import com.rockchip.mediacenter.R;
import com.rockchip.mediacenter.common.util.StringUtils;
import com.rockchip.mediacenter.dlna.model.DeviceItem;
import com.rockchip.mediacenter.dlna.dmp.model.ContainerItem;
import com.rockchip.mediacenter.dlna.dmp.model.MediaItem;
import com.rockchip.mediacenter.mediaplayer.model.FileInfo;
import com.rockchip.mediacenter.mediaplayer.util.Converter;
import com.rockchip.mediacenter.plugins.imageloader.ImageLoader;
import com.rockchip.mediacenter.plugins.imageloader.ImageLoader.BitmapCallback;
import com.rockchip.mediacenter.plugins.widget.BaseMediaAdapter;
import com.rockchip.mediacenter.plugins.widget.MediaGridView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 *
 * @author fxw
 * @since 1.0
 */
public class MediaListAdapter extends BaseMediaAdapter implements BitmapCallback {

	private LayoutInflater mInflater;
	private List<FileInfo> mDataList;
	private Drawable mDeviceIcon;
	private ImageLoader mImageLoader;
	private MediaGridView mMediaGridView;
	private boolean isShowing;
	private String iconURL;
	
	public MediaListAdapter(Context context, List<FileInfo> dataList, ImageLoader imageLoader) {
		super(context, dataList);
		mDataList = dataList;
		mImageLoader = imageLoader;
		mInflater = LayoutInflater.from(context);
		mDeviceIcon = context.getResources().getDrawable(R.drawable.dmp_device_item);
	}
	
	
	public void setMediaGridView(MediaGridView mediaGridView){
		mMediaGridView = mediaGridView;
	}
	
	/**
	 * @param isShowing the isShowing to set
	 */
	public void setShowing(boolean isShowing) {
		this.isShowing = isShowing;
	}
	
	public void notifyDataSetChanged() {
		iconURL = null;
		setShowing(true);
		super.notifyDataSetChanged();
	}
	
	/** 
	 * <p>Title: getView</p> 
	 * <p>Description: </p> 
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @return 
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup) 
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.dmp_media_list_item, null);
			holder.img = (ImageView) convertView.findViewById(R.id.dmp_img_media_item);
			holder.playingImg = (ImageView) convertView.findViewById(R.id.dmp_img_media_play);
			holder.title = (TextView) convertView.findViewById(R.id.dmp_tv_media_title);
			holder.desc = (TextView) convertView.findViewById(R.id.dmp_tv_media_desc);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.img.setTag(null);
		holder.title.setTextColor(getColor(R.color.item_color));
		holder.desc.setTextColor(getColor(R.color.item_desc_color));
		final FileInfo fileInfo = mDataList.get(position);
		if(fileInfo.isDeviceItem()){
			final DeviceItem deviceItem = (DeviceItem) fileInfo.getItem();
			Drawable icon = fileInfo.getIcon();
			if(icon!=null){
				holder.img.setImageDrawable(icon);
			}else{
				holder.img.setImageDrawable(mDeviceIcon);
				if(deviceItem.getLargestIconURL()!=null&&isShowing){
					holder.img.setTag(deviceItem.getLargestIconURL());
					if(!deviceItem.getLargestIconURL().equals(iconURL)){//避免重复加载
						Handler handler = mMediaGridView.getHandler();
						if(handler!=null){
							iconURL = deviceItem.getLargestIconURL();
							handler.postDelayed(new Runnable(){
								public void run() {
									mImageLoader.load(deviceItem.getLargestIconURL(), MediaListAdapter.this);
								}
							}, 10);
						}
					}
				}
			}
			holder.playingImg.setVisibility(View.INVISIBLE);
			holder.title.setText(deviceItem.getFriendlyName());
			holder.desc.setText("");
		}else if(fileInfo.isContainerItem()){
			ContainerItem containerItem = (ContainerItem) fileInfo.getItem();
			holder.img.setImageDrawable(mFolderIcon);
			holder.playingImg.setVisibility(View.INVISIBLE);
			holder.title.setText(containerItem.getTitle());
			if(containerItem.hasChildCount()){
				holder.desc.setText(containerItem.getChildCount()+getContext().getString(R.string.dmp_device_item_desc));
			}else{
				holder.desc.setText("");
			}
		}else if(fileInfo.isMediaItem()){
			MediaItem mediaItem = (MediaItem) fileInfo.getItem();
			setMediaItemState(mediaItem, holder, fileInfo.isSelected());
		}else{
			holder.img.setImageResource(R.drawable.dmp_last_level_dir);
			holder.playingImg.setVisibility(View.INVISIBLE);
			holder.title.setTextColor(getColor(R.color.item_desc_color));
			holder.title.setText(R.string.dmp_back_last_dir);
			holder.desc.setText("");
		}
		convertView.setBackgroundDrawable(null);
		return convertView; 
	}
	
	public void setMediaItemState(MediaItem mediaItem, ViewHolder holder, boolean isSelected){
		if(isSelected){
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)holder.playingImg.getLayoutParams();
			lp.setMargins(-holder.playingImg.getDrawable().getIntrinsicWidth(), 0, 0, 0);
			holder.playingImg.setLayoutParams(lp);
			holder.playingImg.setVisibility(View.VISIBLE);
			holder.desc.setTextColor(getColor(R.color.item_selected_color));
		}else{
			holder.playingImg.setVisibility(View.INVISIBLE);
			holder.desc.setTextColor(getColor(R.color.item_desc_color));
		}
		String desc = Converter.convertSizetoStr(mediaItem.getSize());
		String split_str = " / ";
		if(mediaItem.isImage()){
			holder.img.setImageDrawable(mImageIcon);
			if(StringUtils.hasText(mediaItem.getResolution())){
				desc += split_str+mediaItem.getResolution();
			}
			holder.desc.setText(desc);
		}else if(mediaItem.isAudio()){
			holder.img.setImageDrawable(mAudioIcon);
			if(StringUtils.hasText(mediaItem.getDuration())){
				desc += split_str+mediaItem.getDuration();
			}
			holder.desc.setText(desc);
		}else if(mediaItem.isVideo()){
			holder.img.setImageDrawable(mVideoIcon);
			if(StringUtils.hasText(mediaItem.getDuration())){
				desc += split_str+mediaItem.getDuration();
			}
			holder.desc.setText(desc);
		}else{
			holder.img.setImageDrawable(null);
		}
		holder.title.setText(mediaItem.getTitle());
	}


	/** 
	 * <p>Title: onLoaded</p> 
	 * <p>Description: </p> 
	 * @param url
	 * @param bitmap 
	 * @see com.rockchip.mediacenter.plugins.imageloader.ImageLoader.BitmapCallback#onLoaded(java.lang.String, android.graphics.Bitmap) 
	 */
	@Override
	public void onLoaded(String url, Bitmap bitmap) {
		for(int i=0; i<mMediaGridView.getChildCount(); i++){
			View view = mMediaGridView.getChildAt(i);
			if(view.getTag()!=null){
				ViewHolder holder = (ViewHolder) view.getTag();
				Object tag = holder.img.getTag();
				if(url.equals(tag)){
					try{
						if(bitmap!=null&&!bitmap.isRecycled()){
							bitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, true);
							holder.img.setImageBitmap(bitmap);
						}
					}catch(OutOfMemoryError ome){
						System.gc();
					}
					break;
				}
			}
		}
		
	}

	/** 
	 * <p>Title: onError</p> 
	 * <p>Description: </p> 
	 * @param url
	 * @param error 
	 * @see com.rockchip.mediacenter.plugins.imageloader.ImageLoader.BitmapCallback#onError(java.lang.String, java.lang.Throwable) 
	 */
	@Override
	public void onError(String url, Throwable error) {
		
	}

}
