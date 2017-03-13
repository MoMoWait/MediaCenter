/**
 * 
 * com.rockchips.iptv.stb.dlna.util
 * FileUtils.java
 * 
 * 2011-11-4-下午05:13:11
 * Copyright 2011 Huawei Technologies Co., Ltd
 * 
 */
package com.rockchips.mediacenter.utils;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import momo.cn.edu.fjnu.androidutils.data.CommonValues;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
/**
 * 
 * NetUtils
 * 
 * 2011-11-4 下午05:13:11
 * 
 * @version 1.0.0
 * 
 */
public class NetUtils
{
	
	/**
	 * 是否已经连接网络
	 * @return
	 */
	public static boolean isConnectNetWork(){
		ConnectivityManager cm = (ConnectivityManager)CommonValues.application.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		boolean isConnected = activeNetwork != null && activeNetwork.isConnected();
		return isConnected;
	}
    public static InputStream getInputStreamFromNet(String url)
    {
        InputStream inputStream = null;
        try
        {
            URL mUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection)mUrl.openConnection();
            conn.setConnectTimeout(5 * 1000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            if (conn.getResponseCode() == 200)
            {
                inputStream = conn.getInputStream();
            }
        }
        catch (Exception e)
        {
        }
        return inputStream;
    }

    
    /**
     * 网络连接并收发数据
     */
    public static InputStream getInputStreamFromNet2(String urlPath)
        throws Exception
    {
        InputStream reader = null;
        BasicHttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
        HttpConnectionParams.setSoTimeout(httpParams, 15000);
        HttpClient client = new DefaultHttpClient(httpParams);
        HttpUriRequest request = new HttpPost(urlPath);
        ;
        HttpResponse response = client.execute(request);
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
        {
            reader = response.getEntity().getContent();
            return reader;
        }
        return null;
    }
    
    /**
     * 计算ImageView的大小（decodeFileDescriptor）
     * 
     * @param imageFile
     * @return
     */
    public static int[] computeWH_1(String imageFile)
    {
        int[] wh = {0, 0};
        if (imageFile == null || imageFile.length() == 0)
            return wh;
        
        FileDescriptor fd = null;
        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream(imageFile);
            fd = fis.getFD();
            
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 1;
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFileDescriptor(fd, null, options);
            if (options.mCancel || options.outWidth == -1 || options.outHeight == -1)
            {
                return wh;
            }
            wh[0] = options.outWidth;
            wh[1] = options.outHeight;
            
        }
        catch(IllegalArgumentException iae)
        {
            Log.e("NetUtils", "IllegalArgumentException : " + iae.getLocalizedMessage());
        }
        catch (Exception e)
        {
            Log.e("NetUtils", "e = " + e.getLocalizedMessage());
        }
        finally
        {
            if (fis != null)
            {
                try
                {
                    fis.close();
                }
                catch (IOException e)
                {
                    Log.d("NetUtils", "e = " + e.getLocalizedMessage());
                }
                fis = null;
            }
        }
        
        return wh;
    }
}
