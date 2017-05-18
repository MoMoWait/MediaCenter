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


package com.rockchip.mediacenter.mediaserver.ui;

import java.io.File;
import java.util.List;

import com.rockchip.mediacenter.R;
import com.rockchip.mediacenter.common.util.StringUtils;
import com.rockchip.mediacenter.core.dlna.service.contentdirectory.format.MediaFormat;
import com.rockchip.mediacenter.dlna.dmp.model.MediaItem;
import com.rockchip.mediacenter.mediaplayer.model.FileInfo;
import com.rockchip.mediacenter.mediaplayer.model.LastLevelFileInfo;
import com.rockchip.mediacenter.mediaplayer.util.Converter;
import com.rockchip.mediacenter.mediaserver.model.FolderAddFileInfo;
import com.rockchip.mediacenter.plugins.widget.BaseMediaAdapter;

import android.content.Context;
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
public class ShareListAdapter extends BaseMediaAdapter {

	private LayoutInflater mInflater;
	private List<FileInfo> mDataList;
	private boolean isTopDirectory = false;
	private boolean isShareEnabled = false;
	
	public ShareListAdapter(Context context, List<FileInfo> dataList) {
		super(context, dataList);
		mDataList = dataList;
		mInflater = LayoutInflater.from(context);
	}
	
	/**
	 * 是否在顶级目录
	 * @param isTopDirectory
	 */
	public void setTopDirectory(boolean isTopDirectory){
		this.isTopDirectory = isTopDirectory;
	}
	
	/**
	 * 是否已启用共享
	 * @param isEnabled
	 */
	public void setShareEnabled(boolean isEnabled){
		isShareEnabled = isEnabled;
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
		holder.title.setTextColor(getColor(R.color.item_color));
		holder.desc.setTextColor(getColor(R.color.item_desc_color));
		final FileInfo fileInfo = mDataList.get(position);
		if(fileInfo.isFileItem()){//共享目录、文件
			File file = (File) fileInfo.getItem();
			if(file.exists()){//有实际文件存在的
				if(file.isDirectory()){
					holder.img.setImageDrawable(mFolderIcon);
					holder.desc.setText("");
				}else{
					if(MediaFormat.isImage(file)){
						holder.img.setImageDrawable(mImageIcon);
					}else if(MediaFormat.isAudio(file)){
						holder.img.setImageDrawable(mAudioIcon);
					}else if(MediaFormat.isVideo(file)){
						holder.img.setImageDrawable(mVideoIcon);
					}else{
						holder.img.setImageDrawable(null);
					}
					holder.desc.setText(Converter.convertSizetoStr(file.length()));
					holder.desc.setTextColor(getColor(R.color.item_desc_color));
				}
			}else{
				holder.img.setImageDrawable(fileInfo.getIcon());
				holder.desc.setText("");
			}
			holder.title.setText(file.getName());
			if(isTopDirectory){//设置共享图标
				if(isShareEnabled){
					RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)holder.playingImg.getLayoutParams();
					lp.setMargins(-holder.playingImg.getDrawable().getIntrinsicWidth(), 0, 0, 0);
					holder.playingImg.setLayoutParams(lp);
					holder.playingImg.setImageResource(R.drawable.dms_media_sharing);
					holder.playingImg.setVisibility(View.VISIBLE);
					holder.desc.setText(R.string.dms_share_enabled);
				}else{
					holder.playingImg.setVisibility(View.INVISIBLE);
					holder.desc.setText(R.string.dms_share_disabled);
				}
				holder.desc.setTextColor(getColor(R.color.item_selected_color));
			}else{
				holder.playingImg.setVisibility(View.INVISIBLE);
			}
		}else{
			if(LastLevelFileInfo.isLastLevelFileInfo(fileInfo)){
				holder.img.setImageResource(R.drawable.dmp_last_level_dir);
				holder.title.setText(R.string.dmp_back_last_dir);
			}else if(FolderAddFileInfo.isFolderAddFileInfo(fileInfo)){
				holder.img.setImageResource(R.drawable.dms_share_folder_add);
				holder.title.setText(R.string.dms_add_share_folder);
			}
			holder.playingImg.setVisibility(View.INVISIBLE);
			holder.title.setTextColor(getColor(R.color.item_desc_color));
			holder.desc.setText("");
		}
		convertView.setBackgroundDrawable(null);
		return convertView; 
	}
	
	public void setMediaItemState(MediaItem mediaItem, ViewHolder holder, boolean isSelected){
		if(isSelected){
			if(mediaItem.isImage()){
				holder.img.setImageDrawable(mImageIcon);
			}else if(mediaItem.isAudio()){
				holder.img.setImageDrawable(mAudioIcon);
			}else if(mediaItem.isVideo()){
				holder.img.setImageDrawable(mVideoIcon);
			}
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)holder.playingImg.getLayoutParams();
			lp.setMargins(-holder.playingImg.getDrawable().getIntrinsicWidth(), 0, 0, 0);
			holder.playingImg.setLayoutParams(lp);
			holder.playingImg.setVisibility(View.VISIBLE);
			holder.desc.setText(getContext().getString(R.string.dmp_media_playing));
			holder.desc.setTextColor(getColor(R.color.item_selected_color));
		}else{
			String size = Converter.convertSizetoStr(mediaItem.getSize());
			String split_str = " / ";
			if(mediaItem.isImage()){
				holder.img.setImageDrawable(mImageIcon);
				holder.desc.setText(size+split_str+StringUtils.transformNvl(mediaItem.getResolution(), ""));
			}else if(mediaItem.isAudio()){
				holder.img.setImageDrawable(mAudioIcon);
				holder.desc.setText(size+split_str+StringUtils.transformNvl(mediaItem.getDuration(), ""));
			}else if(mediaItem.isVideo()){
				holder.img.setImageDrawable(mVideoIcon);
				holder.desc.setText(size+split_str+StringUtils.transformNvl(mediaItem.getDuration(), ""));
			}
			holder.playingImg.setVisibility(View.INVISIBLE);
			holder.desc.setTextColor(getColor(R.color.item_desc_color));
		}
		holder.title.setText(mediaItem.getTitle());
	}

}
