/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Filename:    PathTextView.java  
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2012-4-15 下午02:55:13  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2012-4-15      fxw         1.0         create
*******************************************************************/   


package com.rockchip.mediacenter.mediaserver.ui;

import com.rockchip.mediacenter.R;

import android.content.Context;
import android.view.Gravity;
import android.widget.TextView;

/**
 *
 * @author fxw
 * @since 1.0
 */
public class PathTextView extends TextView {

	public PathTextView(Context context, String name) {
		super(context);
		defaultInit(name);
	}

	private void defaultInit(String name){
		setTextColor(this.getResources().getColorStateList(R.drawable.dmp_path_text_color));
		setText(name);
		setFocusable(true);
		setClickable(true);
		setGravity(Gravity.CENTER_VERTICAL);
		setTextSize(20);
		setPadding(10, 0, 0, 0);
		setCompoundDrawablePadding(-30);
		setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.dmp_path_split, 0);
	}
}
