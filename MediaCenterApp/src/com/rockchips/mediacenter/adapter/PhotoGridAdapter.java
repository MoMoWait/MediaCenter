package com.rockchips.mediacenter.adapter;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import momo.cn.edu.fjnu.androidutils.utils.BitmapUtils;
import momo.cn.edu.fjnu.androidutils.utils.SizeUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import org.xutils.x;
import org.xutils.common.util.DensityUtil;
import org.xutils.image.ImageOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap.CompressFormat;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.rockchips.mediacenter.bean.FileInfo;
import com.rockchips.mediacenter.bean.PreviewPhotoInfo;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.modle.db.FileInfoService;
import com.rockchips.mediacenter.modle.db.PreviewPhotoInfoService;
import com.rockchips.mediacenter.service.DeviceMonitorService;
import com.rockchips.mediacenter.service.HttpPhotoLoadThread;
import com.rockchips.mediacenter.utils.PlatformUtils;
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
	private DeviceMonitorService mService;
	private int mLastViewPosition = -1;
	public PhotoGridAdapter(Context context, int resource,
			List<FileInfo> objects, DeviceMonitorService service) {
		super(context, resource, objects);
		mResourceId = resource;
		mInflater = LayoutInflater.from(context);
		mService = service;
		mImageOptions = new ImageOptions.Builder()
        .setSize(DensityUtil.dip2px(100), DensityUtil.dip2px(100))
        .setRadius(DensityUtil.dip2px(5))
        // 如果ImageView的大小不是定义为wrap_content, 不要crop.
        .setCrop(true) // 很多时候设置了合适的scaleType也不需要它.
        // 加载中或错误图片的ScaleType
        //.setPlaceholderScaleType(ImageView.ScaleType.MATRIX)
        .setImageScaleType(ImageView.ScaleType.FIT_XY)
        .setLoadingDrawableId(R.drawable.icon_preview_image)
        .setFailureDrawableId(R.drawable.icon_preview_image)
        .build();

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Log.i(TAG, "getView->position:" + position);
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
		if(fileInfo.getType() == ConstData.MediaType.IMAGE){
			if(PlatformUtils.getSDKVersion() >= 23 || !fileInfo.getPath().startsWith("http"))
				x.image().bind(holder.imgPhoto, fileInfo.getPath(), mImageOptions, null);
			else{
				//读取缓存缩列图
				if(!TextUtils.isEmpty(fileInfo.getPreviewPath()) && !ConstData.UNKNOW.equals(fileInfo.getPreviewPath()))
					x.image().bind(holder.imgPhoto, fileInfo.getPreviewPath(), mImageOptions, null);
				else
					loadHttpPhoto(holder.imgPhoto, fileInfo, position);
			}
		}else{
			holder.textFileCount.setVisibility(View.VISIBLE);
		}
		mLastViewPosition = position;
		return convertView;
	}
	
	final class ViewHolder{
		ImageView imgPhoto;
		TextView textFileName;
		TextView textFileCount;
	}
	
	/**
	 * 获取图片文件的预览图
	 * 
	 * @param allFileInfo
	 */
	private void loadHttpPhoto(ImageView img, FileInfo fileInfo, int position) {
		if(mLastViewPosition != position)
			mService.getHttpPhotoDownloadService().execute(new HttpPhotoLoadThread(img, fileInfo, mService));
	}
	
}
