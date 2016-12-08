package com.rockchips.mediacenter.basicutils.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.os.storage.IMountService;

/**
 * Mount工具封装
 *
 */
public class Mount
{
    public static String mountISO(IMountService mountService, String path)
    {
        Class clazz = mountService.getClass();
        
        String mountPath = null; 
        
        Method m1 = null;
        try
        {
            m1 = clazz.getDeclaredMethod("mountISO", String.class);
        }
        catch (NoSuchMethodException e)
        {
        }
        
        
        try
        {
            if(m1 != null){
                mountPath = (String)m1.invoke(mountService, path);
            }
        }
        catch (IllegalArgumentException e)
        {
        }
        catch (IllegalAccessException e)
        {
        }
        catch (InvocationTargetException e)
        {
        }
        
        return mountPath;
    }
    
    
    
    public static void unmountISO(IMountService mountService, String path)
    {
        Class clazz = mountService.getClass();
        
        String mountPath = null; 
        
        Method m1 = null;
        try
        {
            m1 = clazz.getDeclaredMethod("unmountISO", String.class);
        }
        catch (NoSuchMethodException e)
        {
        }
        
        try
        {
            if(m1 !=null){
                m1.invoke(mountService, path);
            }
        }
        catch (IllegalArgumentException e)
        {
        }
        catch (IllegalAccessException e)
        {
        }
        catch (InvocationTargetException e)
        {
        }
    }
}
