package com.rockchips.mediacenter.portable.orig;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.media.MediaPlayer;
import android.os.Parcel;
import android.view.SurfaceHolder;

public class A40HisiInvoke
{
    public static void invoke(MediaPlayer mediaplayer, Parcel _Request, Parcel _Reply)
    {
        Class clazz = mediaplayer.getClass();
        Method m1 = null;
        try
        {
            m1 = clazz.getDeclaredMethod("invoke", Parcel.class, Parcel.class);
        }
        catch (NoSuchMethodException e)
        {
            // TODO Auto-generated catch block
        }
        try
        {
            if(m1 != null)
            {
                m1.invoke(mediaplayer, _Request, _Reply);
            }
        }
        catch (IllegalArgumentException e)
        {
            // TODO Auto-generated catch block
        }
        catch (IllegalAccessException e)
        {
            // TODO Auto-generated catch block
        }
        catch (InvocationTargetException e)
        {
            // TODO Auto-generated catch block
        }
    }
    
    
    public static void setSubDisplay(MediaPlayer mediaplayer, SurfaceHolder sh)
    {
        Class clazz = mediaplayer.getClass();
        Method m1 = null;
        try
        {
            m1 = clazz.getDeclaredMethod("setSubDisplay", SurfaceHolder.class);
        }
        catch (NoSuchMethodException e)
        {
            // TODO Auto-generated catch block
        }
        try
        {
            if(m1 != null)
            {
                m1.invoke(mediaplayer, sh);
            }
        }
        catch (IllegalArgumentException e)
        {
            // TODO Auto-generated catch block
        }
        catch (IllegalAccessException e)
        {
            // TODO Auto-generated catch block
        }
        catch (InvocationTargetException e)
        {
            // TODO Auto-generated catch block
        }
    }
    
    
}
