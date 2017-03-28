/**
 * 
 */
package com.rockchips.mediacenter.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * @author GaoFei
 * 快进快退时，气泡提示seek位置
 */
public class SeekTimeTextView extends TextView{

	public SeekTimeTextView(Context context) {
		super(context);
	}

	public SeekTimeTextView(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	public SeekTimeTextView(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public SeekTimeTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}
}
