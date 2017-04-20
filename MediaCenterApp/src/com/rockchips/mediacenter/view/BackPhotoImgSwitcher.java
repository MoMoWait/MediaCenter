/**
 * 
 */
package com.rockchips.mediacenter.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.widget.ImageSwitcher;
import android.widget.ImageView;

/**
 * @author GaoFei
 * 背景图切换
 */
public class BackPhotoImgSwitcher extends ImageSwitcher{

	public BackPhotoImgSwitcher(Context context) {
		super(context);
	}

	public BackPhotoImgSwitcher(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void setImageBitmap(Bitmap bmp){
		ImageView image = (ImageView)this.getNextView();
        image.setImageBitmap(bmp);
        showNext();
	}
	
	public void setBitmapDrawable(BitmapDrawable drawable){
		ImageView image = (ImageView)this.getNextView();
        image.setImageDrawable(drawable);
        showNext();
	}
	
}
