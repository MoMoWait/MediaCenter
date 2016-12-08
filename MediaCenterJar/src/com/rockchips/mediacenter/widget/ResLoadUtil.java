/*
 * 文 件 名:  ResLoadUtil.java
 * 版    权:  Huawei Technologies Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  t00181037
 * 修改时间:  2013-1-29
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.rockchips.mediacenter.widget;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * 资源加载工具，避免重复加载
 * <功能详细描述>
 * 
 * @author  t00181037
 * @version  [版本号, 2013-1-29]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */

public class ResLoadUtil
{
    
    private static Map<Integer, Bitmap>  msBitmap = new HashMap<Integer, Bitmap>();
    private static Map<Integer, Drawable>  msDrawable = new HashMap<Integer, Drawable>();
    private static Map<Integer, String>  msString = new HashMap<Integer, String>();
    
    public static synchronized Bitmap getBitmapById(Context context, int drawResid)
    {
        Bitmap bmp = msBitmap.get(drawResid);
        
        if((bmp == null || bmp.isRecycled()) && context != null)
        {
            bmp = ((BitmapDrawable)(context.getResources().getDrawable(drawResid))).getBitmap();
            msBitmap.put(drawResid, bmp);
        }
        
        return bmp;
    }

    public static synchronized Bitmap getBitmapByIdNoCache(Context context, int drawResid)
    {
        //        Bitmap bmp = ((BitmapDrawable)(context.getResources().getDrawable(drawResid))).getBitmap();
        Bitmap bmp = null;
        if (context != null)
        {
            bmp = BitmapFactory.decodeResource(context.getResources(), drawResid);
        }
        return bmp;
    }

    public static synchronized String getStringById(Context context, int strResid)
    {
		/**
        String str = msString.get(strResid);        
        if(str == null)
        {
            str = context.getResources().getString(strResid);
            msString.put(strResid, str);
        }**/
        String str = context.getResources().getString(strResid);
        return str;
    }
    
    public static synchronized Drawable getDrawableById(Context context, int drawResid)
    {
        Drawable drawable = msDrawable.get(drawResid);
        
        if(drawable == null)
        {
            drawable = (Drawable)(context.getResources().getDrawable(drawResid));
            msDrawable.put(drawResid, drawable);
        }
        
        return drawable;
    }
    
}
