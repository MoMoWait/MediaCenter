/*******************************************************************
 * Company:     Fuzhou Rockchip Electronics Co., Ltd
 * Filename:    AlwaysMarqueeTextView.java
 * Description:   
 * @author:     fxw@rock-chips.com
 * Create at:   下午09:36:41
 * 
 * Modification History:  
 * Date         Author      Version     Description  
 * ------------------------------------------------------------------  
 * 2012-4-7      xwf         1.0         create
 *******************************************************************/
package com.rockchip.mediacenter.mediaplayer.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class AlwaysMarqueeTextView extends TextView {
	
	public AlwaysMarqueeTextView(Context context) {  
        super(context);  
    }  
  
    public AlwaysMarqueeTextView(Context context, AttributeSet attrs) {  
        super(context, attrs);  
    }  
  
    public AlwaysMarqueeTextView(Context context, AttributeSet attrs, int defStyle) {  
        super(context, attrs, defStyle);  
    }  
      
    @Override  
    public boolean isFocused() {  
        return true;  
    }
	
}
