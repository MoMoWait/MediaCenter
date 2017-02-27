package com.rockchips.mediacenter.utils;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

/**
 * 
 * Description: toast管理，为了只显示最后一个toast<br>
 * @author r00178559
 * @version v1.0
 * Date: 2013-12-5 上午10:58:46<br>
 */
public final class ToastUtil
{
    private ToastUtil()
    {
        
    }
    
    public static final int TOAST_X_OFFSET = 0;

    public static final int TOAST_Y_OFFSET = 100;

    private static Context mStContext;

    private static Toast mStToast;

    /**
     * 
     * 使用这个类之前必须先build至少一次
     * @param context
     * @return void
     * @throws
     */
    public static void build(Context context)
    {
        mStContext = context;
    }

    public static void showToastContent(String content, int gravity, int xOffset, int yOffset)
    {
        if (null == mStContext)
        {
            return;
        }
        if (mStToast == null)
        {
            mStToast = Toast.makeText(mStContext, content, Toast.LENGTH_SHORT);
        }
        else
        {
            mStToast.setText(content);
        }
        mStToast.setGravity(gravity, xOffset, yOffset);
        mStToast.show();
    }
    
    public static void showDefault(String content)
    {
        showToastContent(content, Gravity.BOTTOM, TOAST_X_OFFSET, TOAST_Y_OFFSET);
    }
    
    public static void hide()
    {
        if (null != mStToast)
        {
            mStToast.cancel();
        }
    }
    
    
    public static void showBySetDur(String content, int duration)
    {
        if (null == mStContext)
        {
            return;
        }
        if (null == mStToast)
        {
            mStToast = Toast.makeText(mStContext, content, Toast.LENGTH_SHORT);
        }
        else
        {
            mStToast.setText(content);
        }
        mStToast.setGravity(Gravity.BOTTOM, TOAST_X_OFFSET, TOAST_Y_OFFSET);
        if (duration > 0)
        {
             mStToast.setDuration(duration);
        }
       
        mStToast.show();
    }

}
