package com.rockchips.mediacenter.adapter;
import java.util.List;
import org.xutils.x;
import org.xutils.common.util.DensityUtil;
import org.xutils.image.ImageOptions;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import com.rockchips.mediacenter.bean.FileInfo;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.R;

/**
 * @author GaoFei
 * 图片列表适配器
 */
public class BackgroundPhotoAdapter extends ArrayAdapter<FileInfo> {
	public static final String TAG = BackgroundPhotoAdapter.class.getSimpleName();
	private int mResourceId = 0;
	private LayoutInflater mInflater;
	private ImageOptions mImageOptions;
	public BackgroundPhotoAdapter(Context context, int resource,
			List<FileInfo> objects) {
		super(context, resource, objects);
		mResourceId = resource;
		mInflater = LayoutInflater.from(context);
		mImageOptions = new ImageOptions.Builder()
        .setSize(DensityUtil.dip2px(100), DensityUtil.dip2px(100))
        .setRadius(DensityUtil.dip2px(5))
        // 加载中或错误图片的ScaleType
        //.setPlaceholderScaleType(ImageView.ScaleType.MATRIX)
        .setImageScaleType(ImageView.ScaleType.FIT_XY)
        .setLoadingDrawableId(R.drawable.icon_preview_image)
        .setFailureDrawableId(R.drawable.icon_preview_image)
        .build();

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null){
			convertView = mInflater.inflate(mResourceId, parent, false);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.checkSelect = (CheckBox)convertView.findViewById(R.id.check_select);
			viewHolder.imgPhoto = (ImageView)convertView.findViewById(R.id.img_photo);
			viewHolder.textFileName = (TextView)convertView.findViewById(R.id.text_file_name);
			convertView.setTag(viewHolder);
		}
		ViewHolder holder = (ViewHolder)convertView.getTag();
		FileInfo fileInfo = getItem(position);
		holder.textFileName.setText(fileInfo.getName());
		holder.checkSelect.setChecked(fileInfo.isSelectDelete());
		if(fileInfo.getType() == ConstData.MediaType.IMAGE)
			x.image().bind(holder.imgPhoto, fileInfo.getPath(), mImageOptions, null);
		return convertView;
	}
	
	final class ViewHolder{
		CheckBox checkSelect;
		ImageView imgPhoto;
		TextView textFileName;
	}
}
