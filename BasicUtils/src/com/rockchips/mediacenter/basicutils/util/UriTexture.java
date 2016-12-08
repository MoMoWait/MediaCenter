/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rockchips.mediacenter.basicutils.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.rockchips.mediacenter.basicutils.constant.Constant;

public class UriTexture extends Texture
{
    
    private static final String tag = "UriTexture";
    
    public static final int MAX_RESOLUTION = 1280;
    
    private static final String TAG = "UriTexture";
    
    protected String mUri;
    
    protected long mCacheId;
    
    private static final int MAX_RESOLUTION_A = MAX_RESOLUTION;
    
    private static final int MAX_RESOLUTION_B = MAX_RESOLUTION;
    
    private static final String USER_AGENT = "Cooliris-ImageDownload";
    
    private static final int CONNECTION_TIMEOUT = 20000; // ms.
    
    public static final HttpParams HTTP_PARAMS;
    
    public static final SchemeRegistry SCHEME_REGISTRY;
    
    private static String URI_CACHE = null;
    static
    {
        
        //cache dir
        URI_CACHE = Environment.getDownloadCacheDirectory().getAbsolutePath();
        
        Log.d(tag, "get uri cache dir:" + URI_CACHE);
        
        // Prepare HTTP parameters.
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setStaleCheckingEnabled(params, false);
        HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT);
        HttpConnectionParams.setSoTimeout(params, CONNECTION_TIMEOUT);
        HttpClientParams.setRedirecting(params, true);
        HttpProtocolParams.setUserAgent(params, USER_AGENT);
        HTTP_PARAMS = params;
        
        // Register HTTP protocol.
        SCHEME_REGISTRY = new SchemeRegistry();
        SCHEME_REGISTRY.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
    }
    
    public static void setCacheDir(String cacheDir)
    {
        URI_CACHE = cacheDir;
    }
    
    public UriTexture(String imageUri)
    {
        mUri = imageUri;
    }
    
    public void setCacheId(long id)
    {
        mCacheId = id;
    }
    
    private static int computeSampleSize(InputStream stream, int maxResolutionX, int maxResolutionY)
    {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(stream, null, options);
        int maxNumOfPixels = maxResolutionX * maxResolutionY;
        int minSideLength = Math.min(maxResolutionX, maxResolutionY) / 2;
        return Utils.computeSampleSize(options, minSideLength, maxNumOfPixels);
    }
    
    public static final SoftReference<Bitmap> createFromUri(Context context, String uri, int maxResolutionX,
        int maxResolutionY, long cacheId, ClientConnectionManager connectionManager, int devType)
        throws IOException, URISyntaxException, OutOfMemoryError
    {
        Log.d(TAG, "createFromUri 1");
        final BitmapFactory.Options options = new BitmapFactory.Options();
        Log.d(TAG, "createFromUri 2");
        options.inScaled = false;
        
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inDither = true;
        long crc64 = 0;
        Bitmap bitmap = null;
        SoftReference<Bitmap> srBmp = null;
        
        //SCHEME 的形式
        if (uri.startsWith(ContentResolver.SCHEME_CONTENT))
        {
            Log.d(TAG, "createFromUri 3");
            // We need the filepath for the given content uri

            crc64 = cacheId;
        }
        else
        {
            Log.d(TAG, "createFromUri 4");
            
            Log.d(TAG,"uri:" + uri);
            
            crc64 = Utils.Crc64Long(uri);
        }
       Log.d(TAG, "createFromUri 5");
        
        bitmap = createFromCache(crc64, maxResolutionX, maxResolutionY);
        
        Log.d(TAG, "createFromUri 6");
        if (bitmap != null)
        {
            Log.d(TAG, "createFromUri 7");
            
            srBmp = new SoftReference<Bitmap>(bitmap);
            
            bitmap = null;
            
            return srBmp;
        }
        
        //标志是否为本地文件
        final boolean bLocalFile = uri.startsWith(ContentResolver.SCHEME_CONTENT) || uri.startsWith("file://");
        
        Log.d(TAG, "createFromUri 8");
        
        //whether it is content format
        if (uri.startsWith(ContentResolver.SCHEME_CONTENT) || uri.startsWith(ContentResolver.SCHEME_FILE))
        {
            BufferedInputStream bufferedInput = null;
            Log.d(TAG, "createFromUri 10");
            // Get the stream from a local file.
            bufferedInput =
                new BufferedInputStream(context.getContentResolver().openInputStream(Uri.parse(uri)), 50 * 1024);//DTS2010082401913
            
            // Compute the sample size, i.e., not decoding real pixels.
            if (bufferedInput != null)
            {
                Log.d(TAG, "createFromUri 13");
                options.inSampleSize = computeSampleSize(bufferedInput, maxResolutionX, maxResolutionY);
                
            }
            
            bufferedInput =
                new BufferedInputStream(context.getContentResolver().openInputStream(Uri.parse(uri)), 50 * 1024);
            
            Log.d(TAG, "createFromUri 15");
            options.inDither = true;
            options.inJustDecodeBounds = false;
            
            bitmap = BitmapFactory.decodeStream(bufferedInput, null, options);
            
            srBmp = new SoftReference<Bitmap>(bitmap);
            
            bitmap = null;
        }
        else
        {//resource from net
            Log.d(TAG, "createFromUri 16");
            
            Log.d(TAG, "resource from net: " + uri);
            
            srBmp = CreateBitMap(uri, devType, options, maxResolutionX, maxResolutionY);
            
            if(srBmp == null)
            {
                Log.d(TAG, "CreateBitMap --> srBmp == null");
            }
            else
            {
                Log.d(TAG, "CreateBitMap --> srBmp != null");
            }
            
        }
        
        Log.d(TAG, "createFromUri 17");
        
        return srBmp;
    }
    
    private static final BufferedInputStream createInputStreamFromRemoteUrl(String uri,
        ClientConnectionManager connectionManager)
    {
        InputStream contentInput = null;
        if (connectionManager == null)
        {
            try
            {
                URL url = new URI(uri).toURL();
                URLConnection conn = url.openConnection();
                conn.connect();
                contentInput = conn.getInputStream();
            }
            catch (Exception e)
            {
                Log.w(TAG, "Request failed: " + uri);
                return null;
            }
        }
        else
        {
            // We create a cancelable http request from the client
            final DefaultHttpClient mHttpClient = new DefaultHttpClient(connectionManager, HTTP_PARAMS);
            HttpUriRequest request = new HttpGet(uri);
            // Execute the HTTP request.
            HttpResponse httpResponse = null;
            try
            {
                httpResponse = mHttpClient.execute(request);
                HttpEntity entity = httpResponse.getEntity();
                if (entity != null)
                {
                    // Wrap the entity input stream in a GZIP decoder if
                    // necessary.
                    contentInput = entity.getContent();
                }
            }
            catch (Exception e)
            {
                Log.w(TAG, "Request failed: " + request.getURI());
                return null;
            }
        }
        if (contentInput != null)
        {
            return new BufferedInputStream(contentInput, 4096);
        }
        else
        {
            return null;
        }
    }
    
    /**
     * append the cache file path with crc64 of url and the max resolution
     * createFilePathFromCrc64
     * 
     * @param crc64
     * @param maxResolution
     * @return 
     *String
     * @exception
     */
    public static final String createFilePathFromCrc64(long crc64, int maxResolution)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(URI_CACHE);
        sb.append("/");
        sb.append(crc64);
        sb.append("_");
        sb.append(maxResolution);
        sb.append(".cache");
        
        Log.d(TAG, "createFilePathFromCrc64 PATH: " + sb.toString());
        
        return sb.toString();
    }
    
    /**
     * whether crc64 has existed
     * isCached
     * 
     * @param crc64
     * @param maxResolution
     * @return 
     *boolean
     * @exception
     */
    public static boolean isCached(long crc64, int maxResolution)
    {
        
        String file = null;
        FileInputStream fi = null;
        int iret = -1;
        
        if (crc64 != 0)
        {
            file = createFilePathFromCrc64(crc64, maxResolution);
            try
            {
                fi = new FileInputStream(file);
                iret = fi.read();
            }
            catch (FileNotFoundException e)
            {
            }
            catch (IOException e)
            {
            }
            finally
            {
                if (fi != null)
                {
                    try
                    {
                        fi.close();
                    }
                    catch (IOException e)
                    {
                    }
                    fi = null;
                }
            }
        }

        if (iret == -1)
        {
            return false;
        }
        else
        {
            return true;
        }
    }
    
    /**
     * create bitmap from cache file
     * createFromCache
     * 
     * @param crc64
     * @param maxResolution
     * @return 
     *Bitmap
     * @exception
     */
    public static Bitmap createFromCache(long crc64, int maxResolutionX, int maxResolutionY)
    {
        Log.d(TAG, "createFromCache --> maxResolutionX:" + maxResolutionX);
        Log.d(TAG, "createFromCache --> maxResolutionY:" + maxResolutionY);
        
        try
        {
            String file = null;
            Bitmap bitmap = null;
            BitmapFactory.Options options = null;
            
            if (crc64 != 0)
            {
                file = createFilePathFromCrc64(crc64, maxResolutionX);
                
                File filehandler = new File(file);
                filehandler.setLastModified(System.currentTimeMillis());
                
                //获取采样率
                options = getOptions(file, maxResolutionX, maxResolutionY);
                
                bitmap = BitmapFactory.decodeFile(file, options);
                
            }
            return bitmap;
        }
        catch (Exception e)
        {
            return null;
        }
    }
    
    /**
     * getOptions
     * 
     * @param file
     * @param maxResolutionX
     * @param maxResolutionY 
     *void
     * @exception 
    */
    public static Options getOptions(String file, int maxResolutionX, int maxResolutionY)
    {
        if (file == null)
        {
            return null;
        }
        
        Log.d(TAG, "getOptions --> maxResolutionX:" + maxResolutionX);
        Log.d(TAG, "getOptions --> maxResolutionY:" + maxResolutionY);
        
        BitmapFactory.Options opts = new BitmapFactory.Options();
        //只是解边框
        opts.inJustDecodeBounds = true;
        opts.outWidth = maxResolutionX;
        opts.outHeight = maxResolutionY;
        
        //不真正解码，只是获取属性options
        Performance performance = new Performance();
        performance.start();
        BitmapFactory.decodeFile(file, opts);
        performance.end();
        
        Log.d(TAG, "outWidth = " + opts.outWidth + " outHeight:" + opts.outHeight);
        
        if (opts.outHeight <= 1080 && opts.outWidth <= 1920)
        {
            opts.inSampleSize = 1;
        }
        else
        {
            // --- Modified by zhaomingyang 00184367 2012年2月7日 --- Begin
            // 为了解决8192*8192大像素图片解码失败的Bug，修改采样计算方法
            
//            if (opts.outWidth >= 1920)
//            {
//                opts.outWidth = maxResolutionX;
//                Log.d(TAG, "Justice: outW = " + opts.outWidth);
//            }
//            
//            if (opts.outHeight >= 1080)
//            {
//                opts.outHeight = maxResolutionY;
//                Log.d(TAG, "Justice: outH = " + opts.outHeight);
//            }
//            
//            int maxNumOfPixels = maxResolutionX * maxResolutionY;
            
            int maxNumOfPixels = opts.outWidth * opts.outHeight;

//            int minSideLength = Math.min(maxResolutionX, maxResolutionY) / 2;
            int minSideLength = Math.min(maxResolutionX, maxResolutionY);
            
            opts.inSampleSize = Utils.computeSampleSize(opts, minSideLength, maxNumOfPixels);
                        
         // --- Modified by zhaomingyang 00184367 2012年2月7日 --- End
        }
        
        Log.d(TAG, "opts.inSampleSize:" + opts.inSampleSize);
        
        //设置为解析全部
        opts.inJustDecodeBounds = false;
        opts.inDither = true;
        
        return opts;
    }
    
    /**
     * 
     * writeToCache
     * 
     * @param crc64
     * @param maxResolution
     * @param in 
     *void
     * @exception
     */
    public static void writeToCache(long crc64, int maxResolution, InputStream in)
    {
        
        if (in == null)
        {
            return;
        }
        
        if (isCached(crc64, maxResolution))
        {//TODO update the modify time
            return;
        }
        
        String file = createFilePathFromCrc64(crc64, maxResolution);
        
        FileOutputStream fos = null;
        BufferedInputStream bis = null;
        
        //8k
        int BUFFER_SIZE = 8192;
        byte[] buf = new byte[BUFFER_SIZE];
        int size = 0;
        
        try
        {
            //获取网络输入流    
            bis = new BufferedInputStream(in);
            //建立文件    
            fos = new FileOutputStream(file);
            
            //保存文件    
            while ((size = bis.read(buf)) != -1)
            {
                fos.write(buf, 0, size);
            }
            
            fos.flush();
        }
        catch (FileNotFoundException e)
        {
        }
        catch (IOException e)
        {
        }
        finally
        {
            if (null != fos)
            {
                try
                {
                    fos.close();
                }
                catch (IOException e)
                {
                }
            }
            if (null != bis)
            {
                try
                {
                    bis.close();
                }
                catch (IOException e)
                {
                }
            }
        }
        
    }
    
    /**
     * write bitmap to cache
     * writeToCache
     * 
     * @param crc64
     * @param bitmap
     * @param maxResolution 
     *void
     * @exception
     */
    public static void writeToCache(long crc64, Bitmap bitmap, int maxResolution)
    {
        String file = createFilePathFromCrc64(crc64, maxResolution);
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        
        if (bitmap != null && file != null && crc64 != 0)
        {
            try
            {
                File fileC = new File(file);
                fileC.createNewFile();
                fos = new FileOutputStream(fileC);
                bos = new BufferedOutputStream(fos, 16384);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
                bos.flush();
            }
            catch (FileNotFoundException e)
            {
            }
            catch (IOException e)
            {
            }
            finally
            {
                if (bos != null)
                {
                    try
                    {
                        bos.close();
                    }
                    catch (IOException e1)
                    {
                    }
                }
                
                if (fos != null)
                {
                    try
                    {
                        fos.close();
                    }
                    catch (IOException e1)
                    {
                    }
                }
            }
        }
    }
    
    public static void invalidateCache(long crc64, int maxResolution)
    {
        Log.w(TAG, "invalidateCache 1");
        String file = createFilePathFromCrc64(crc64, maxResolution);
        CacheFileThread cachethread = new CacheFileThread();
        
        cachethread.setParams(file, crc64);
        
        cachethread.start();
    }
    
    @Override
    public void finalize()
    {
    }
    
    public static SoftReference<Bitmap> CreateBitMap(String url, int devType, /*OUT*/Options opt, int w, int h)
    {
        if (StringUtils.isEmpty(url))
        {
            return null;
        }
        
        Log.d(TAG, "CreateBitMap --> url:" + url);
        Log.d(TAG, "CreateBitMap --> devType:" + devType);
        Log.d(TAG, "CreateBitMap --> w:" + w);
        Log.d(TAG, "CreateBitMap --> h:" + h);
        
        long crc64 = Utils.Crc64Long(url);
        
        URL myFileUrl = null;
        
        Bitmap bitmap = null;
        
//        Log.d(TAG, "CreateBitMap 1");
        
        try
        {
//            Log.d(TAG, "CreateBitMap 2");
            
            InputStream is = null;
            HttpURLConnection conn = null;
            if (devType == Constant.DeviceType.DEVICE_TYPE_DMS)
            {
//                Log.d(TAG, "CreateBitMap 3");
                try
                {
                    myFileUrl = new URL(url);
                    
                }
                catch (MalformedURLException e)
                {
                }
                
//                Log.d(TAG, "CreateBitMap 4");
                
                if (myFileUrl == null)
                {
//                    Log.d(TAG, "CreateBitMap 5");
                    Log.e(TAG, "CreateBitMap - HttpURLConnection (myFileUrl == null)");
                    return null;
                }
                
//                Log.d(TAG, "CreateBitMap 6");
                conn = (HttpURLConnection)myFileUrl.openConnection();
                
                if (conn == null)
                {
//                    Log.d(TAG, "CreateBitMap 7");
                    Log.e(TAG, "CreateBitMap - HttpURLConnection conn == null");
                    return null;
                }
                
//                Log.d(TAG, "CreateBitMap 8");
                conn.setDoInput(true);
                conn.setConnectTimeout(2000); //连接超时2s，避免断网死等
                conn.setReadTimeout(5000); //读数据超时5s，避免断网死等
                //conn.setDoOutput(true);
                //conn.setRequestMethod("GET");
                
                try
                {
                    conn.connect();
                }
                catch (Exception e)
                {
                    conn.disconnect();
                    conn = null;
                    return null;
                }
                
                is = conn.getInputStream();
//                Log.d(TAG, "CreateBitMap 9");
                
            }
            else if (devType == Constant.DeviceType.DEVICE_TYPE_U)
            {//此处给的格式可能是URI的格式
            
//                Log.d(TAG, "CreateBitMap 10");
                
                File file = new File(url);
                
                if (!file.exists())
                {
                    Log.d(TAG, "File is not exists");
                    return null;
                }
                
                FileInputStream fis = new FileInputStream(url);
                
                is = fis;
                
//                Log.d(TAG, "CreateBitMap 11");
            }
            else
            {
//                Log.d(TAG, "CreateBitMap 12");
                Log.e(TAG, "CreateBitMap - error deviceType:" + devType);
                
                return null;
            }
            
            if (is == null)
            {
//                Log.d(TAG, "CreateBitMap 13");
                Log.d(TAG, "CreateBitMap - is == null");
                
                if (conn != null)
                {
                    conn.disconnect();
                    
                    conn = null;
                }
                
                return null;
            }
            
            //增加性能计数
            Performance writetocachePf = new Performance("WRITE BMP STREAM INTO CACHE PF");
            //保存图片到临时空间
            writetocachePf.start();
            writeToCache(crc64, w, is);
            writetocachePf.end();
            
            //clear input stream handler & disconnect input stream
            is.close();
            is = null;
            if (conn != null)
            {
                conn.disconnect();
                
                conn = null;
            }
            
            Log.d(TAG, "CreateBitMap Start to Retrieve bitmap from the cache");
            
            
            Performance decodePf = new Performance("DECODE PF");
            //Retrieve bitmap from the cache
            decodePf.start();
            bitmap = createFromCache(crc64, w, h);
            decodePf.end();
            
            if (devType == Constant.DeviceType.DEVICE_TYPE_DMS)
            {
                //delete cache file
                invalidateCache(crc64, w);
            }
            
//            Log.d(TAG, "CreateBitMap 20");
            if (bitmap == null)
            {
                Log.d(TAG, "CreateBitMap - bitmap == null");
                
                return null;
            }
            
//            Log.d(TAG, "CreateBitMap 21");
            
        }
        catch (IOException e)
        {
            if (bitmap != null && !bitmap.isRecycled())
            {
                bitmap.recycle();
            }
            
            return null;
            
        }
        
        //System.gc();
        Log.d(TAG, "CreateBitMap return softReference bmp");
        
        SoftReference<Bitmap> srBmp = new SoftReference<Bitmap>(bitmap);
        bitmap = null;
        
        return srBmp;
        
    }
    
    /** 根据大小获得Options实例
     * <功能详细描述>
     * @param size 图片大小;
     * @return 用于创建改大小图片的Options对象
     * @see [类、类#方法、类#成员]
     */
    public static Options getOptions(byte[] bt, long size, int maxW, int maxH)
    {
        if (bt == null)
        {
            return null;
        }
        
        BitmapFactory.Options opts = new BitmapFactory.Options();
        //只是解边框
        opts.inJustDecodeBounds = true;
        opts.outWidth = maxW;
        opts.outHeight = maxH;
        
        //不真正解码，只是获取属性options
        Performance performance = new Performance();
        performance.start();
        BitmapFactory.decodeByteArray(bt, 0, (int)size, opts);
        performance.end();
        
        int outWidth = opts.outWidth;
        int outHeight = opts.outHeight;
        
        Log.d(TAG, "outWidth = " + outWidth + " outHeight:" + outHeight);
        
        if (opts.outWidth >= 3000)
        {
            opts.outWidth = maxW;
            Log.d(TAG, "Justice: outW = " + opts.outWidth);
        }
        
        if (opts.outHeight >= 3000)
        {
            opts.outHeight = maxH;
            Log.d(TAG, "Justice: outH = " + opts.outHeight);
        }
        
        int maxNumOfPixels = maxW * maxH;
        int minSideLength = Math.min(maxW, maxH) / 2;
        opts.inSampleSize = Utils.computeSampleSize(opts, minSideLength, maxNumOfPixels);
        
        //设置为解析全部
        opts.inJustDecodeBounds = false;
        opts.inDither = true;
        
        return opts;
    }
    
    public static byte[] getBytes(InputStream is)
        throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] b = new byte[1024];
        int len = 0;
        while ((len = is.read(b, 0, 1024)) != -1)
        {
            baos.write(b, 0, len);
            baos.flush();
        }
        b = null;
        
        byte[] bytes = baos.toByteArray();
        baos.close();
        baos = null;
        return bytes;
    }
    
    // Rotates the bitmap by the specified degree.
    // If a new bitmap is created, the original bitmap is recycled.
    public static Bitmap rotate(Bitmap b, int degrees)
    {
        
        Bitmap b2 = null;
        
        if (degrees != 0 && b != null)
        {
            Matrix m = new Matrix();
            m.postRotate(degrees);
            try
            {
                
                b2 = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), m, true);
                
            }
            catch (OutOfMemoryError ex)
            {
                // We have no memory to rotate. Return the original bitmap.
            }
        }
        return b2;
    }
    
    //    
    //    protected static class TimeOutThread extends Thread
    //    {
    //        
    //        private Options options = null;
    //        
    //        private boolean bRun = false;
    //        
    //        private static TimeOutThread mTimeOutThread = null;
    //        
    //        public static TimeOutThread getInstance()
    //        {
    //            
    //            if (mTimeOutThread == null)
    //            {
    //                mTimeOutThread = new TimeOutThread("BMP DEC");
    //            }
    //            
    //            return mTimeOutThread;
    //        }
    //        
    //        /**
    //         *constructor TimeOutThread.
    //         *
    //         * @param string
    //         */
    //        public TimeOutThread(String string)
    //        {
    //            super(string);
    //        }
    //        
    //        public void setOpt(Options opt)
    //        {
    //            
    //            bRun = false;
    //            
    //            options = opt;
    //        }
    //        
    //        public void run()
    //        {
    //            try
    //            {
    //                int i = 600;
    //                Log.d(TAG, "TimeOutThread  run 1");
    //                
    //                while (i-- > 0 && bRun)
    //                {
    //                    Thread.sleep(50);
    //                }
    //                
    //                if (bRun)
    //                {
    //                    options.requestCancelDecode();
    //                }
    //                
    //                if (!bRun)
    //                {
    //                    bRun = true;
    //                }
    //                
    //            }
    //            catch (InterruptedException e)
    //            {
    //            }
    //        }
    //        
    //    }
    
    protected static class CacheFileThread extends Thread
    {
        private String mFilename;
        
        private long mCrc64;
        
        public void setParams(String filename, long crc64)
        {
            mFilename = filename;
            mCrc64 = crc64;
        }
        
        /* (non-Javadoc)
         * @see java.lang.Thread#run()
         */
        @Override
        public void run()
        {
            String dir = mFilename.substring(0, mFilename.lastIndexOf('/'));
            File dirFile = new File(dir);
            if (dirFile.isDirectory())
            {
                File[] fileArray = dirFile.listFiles();
                
                if (fileArray != null && fileArray.length > 0)
                {
                    for (File file : fileArray)
                    {
                        //30s删除
                        if (file.lastModified() + 50000 < System.currentTimeMillis())
                        {
                            file.delete();
                        }
                    }    
                }
            }
            super.run();
        }
    }
}
