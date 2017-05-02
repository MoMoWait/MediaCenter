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

package com.rockchips.mediacenter.imageplayer.image;

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
import java.util.Arrays;
import java.util.Timer;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.view.View;

import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.utils.DateUtil;
import com.rockchips.mediacenter.utils.FileUtil;
import com.rockchips.mediacenter.utils.IICLOG;
import com.rockchips.mediacenter.utils.Performance;
import com.rockchips.mediacenter.utils.PlatformUtils;
import com.rockchips.mediacenter.utils.StringUtils;
import com.rockchips.mediacenter.utils.Texture;
import com.rockchips.mediacenter.utils.Utils;
import com.rockchips.mediacenter.utils.GifOpenHelper;
import com.rockchips.mediacenter.utils.JpegHeaders;
import com.rockchips.mediacenter.utils.JpegHeaders.Section;
import com.rockchips.mediacenter.utils.ReadJpg;
import com.rockchips.mediacenter.service.MultiDownloadListener;
import com.rockchips.mediacenter.service.MultiThreadDownloader;
import com.rockchips.mediacenter.imageplayer.DLNAImageSwitcherViewFactory;
import com.rockchips.mediacenter.imageplayer.downloader.DownloadProgressListener;
import com.rockchips.mediacenter.imageplayer.downloader.FileDownloader;

/**
 * 
 * 图片内容播放的缓存和显示机制   
 * 
 */
public class UriTexture extends Texture
{
    
    private static final String TAG = "MediaCenterApp";
    
    private static IICLOG mLog = IICLOG.getInstance();
    
    public static final String BIGFILENAME = "bigfilename";
    
    public static final int MAX_RESOLUTION = 1280;
    
    private static final String USER_AGENT = "Cooliris-ImageDownload";
    
    public static final int DECODE_STREAM = 0x0001;
    
    public static final int DECODE_FILE = 0x0002;
    
    private static final int CONNECTION_TIMEOUT = 20000;
    
    public static final HttpParams HTTP_PARAMS;
    
    public static final SchemeRegistry SCHEME_REGISTRY;
    
    public static Context mContext;
    
    // 图片显示最大值
    public static final int BITMAP_MAX_W = 1280;
    
    public static final int BITMAP_MAX_H = 1080;
    
    public static final int FIX_BITMAP_MAX_H = 1000;
    
    // 是否断网
    public static boolean mbNetworkDisconnected = false;
    
    public static CurlDownload mCurlDownload;
    
    public static String URI_CACHE;
    
    /**
     * 图片的固有with或者分辨率超过最大值经过采样后图片的with
     */
    public static int mPicSizeX;
    
    /**
     * 图片的固有height或者分辨率超过最大值经过采样后图片的height
     */
    public static int mPicSizeY;
    
    /**
     * 控制设置DLNLSwticher的scaleType的接口
     */
    public static DLNAImageSwitcherViewFactory dlnaImageSwitcherViewFactory;
    
    /**
     * 下载云相册图片线程数
     */
    public static final int mThreadNum = 6;
    
    /**
     * 下载云相册图片超时时间 1000*60
     */
    public static final int mDelayTime = 60 * 1000;
    
    /**
     * 下载云相册图片线程数
     */
    private static String mLocalSavePath = null;
    
    private static Timer mTimer = null;
    
    protected String mUri;
    
    protected long mCacheId;
    
    /**
     * 标志是否取消下载mtd.stop(),用于控制是否显示默认图片，如果调用mtd.stop()则不显示加载失败
     */
    private static boolean mbStop = false;
    
    public static void setMbStop(boolean stop)
    {
        mbStop = stop;
    }
    public static boolean getMbStop()
    {
        return mbStop;
    }
    static
    {
        
        URI_CACHE = Environment.getDownloadCacheDirectory().getAbsolutePath();
        
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
    
    public static void registerShareReceiver(Context context)
    {
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
    
    public static final GifOpenHelper createGifFromUrl(Context context, String uri, int maxW, int maxH, int devType,
        String name, long maxSize)
    {
        if (StringUtils.isEmpty(uri))
        {
            mLog.e(TAG, "getGif url is null");
            return null;
        }
        mLog.d(TAG, "createGifFromUrl----------->url = " + uri);
        
        GifOpenHelper gif = null;
        InputStream is = null;
        try
        {
            //本地设备
            if (ConstData.DeviceType.isLocalDevice(devType))
            {
                mLog.d(TAG, "createGifFromUrl----------->sd or u");
                File file = new File(uri);
                
                if(file.exists() && file.length() < maxSize)
                {
                    is = new FileInputStream(file);
                }
            }
            else if (uri.startsWith(ContentResolver.SCHEME_CONTENT) || uri.startsWith(ContentResolver.SCHEME_FILE))
            {
                mLog.d(TAG, "createGifFromUrl----------->SCHEME_CONTENT or SCHEME_FILE");
                is = context.getContentResolver().openInputStream(Uri.parse(uri));
            }
            else
            {
                //从缓存里读取
                String size = "0x0";
                int degree = 0;
                long crc64 = Utils.Crc64Long(uri);
                String filePath = createFilePathFromCrc64WithSize(crc64, maxW, size, degree);
                File file = new File(filePath);
                if (file.exists())
                {
                    is = new FileInputStream(file);
                }
                mLog.d(TAG, "createGifFromUrl----------->cache file = "+file);
            }
            
            if (is != null)
            {
                mLog.d(TAG, "createGifFromUrl----------->is != null");
                gif = new GifOpenHelper();
                gif.read(is);
                mLog.d(TAG, "createGifFromUrl----------->gif = " + gif);
            }
            
            if (gif == null)
            {
                mLog.d(TAG, "createGifFromUrl----------->createGifFromNet");
                //从网络下载
                gif = createGifFromNet(context, uri, maxW, maxH, devType, name, maxSize);
                mLog.d(TAG, "createGifFromUrl----------->fromNet gif = " + gif);
            }
        }
        catch (FileNotFoundException e)
        {
        }
        catch (OutOfMemoryError e)
        {
        }
        catch(Exception e)
        {
        }
        finally
        {
            if (is != null)
            {
                try
                {
                    is.close();
                }
                catch (IOException e)
                {
                }
            }
        }
        
        return gif;
    }
    
    private static GifOpenHelper createGifFromNet(Context context, String url, int maxW, int maxH, int devType,
        String name, long maxSize)
    {
        mLog.d(TAG, "createGifFromNet------->start time:" + DateUtil.getCurrentTime());
        mLog.d(TAG, "createGifFromNet---url--->:" + url);
        
        if (StringUtils.isEmpty(url) || !StringUtils.isNetworkURI(url))
        {
            return null;
        }
        
        InputStream inputStream = null;
        HttpURLConnection conn = null;
        URL mUrl = null;
        GifOpenHelper gif = null;
        long fileLength = 0;
        String filePath = null;
        
        mLog.d(TAG, "createGifFromNet-1---->CurrentTime:" + DateUtil.getCurrentTime());
        // 获取gif图片大小
        try
        {
            mUrl = new URL(url);
            conn = (HttpURLConnection)mUrl.openConnection();
            conn.setConnectTimeout(10 * 1000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setReadTimeout(10 * 1000);
            if (conn.getResponseCode() == 200)
            {
                fileLength = conn.getContentLength();
            }
            
            mLog.d(TAG, "createGifFromNet-2---->fileLength:" + fileLength);
            //只有满足大小的图片才下载
            if (fileLength <= maxSize)
            {
                filePath = DownloadCloudImageToCache(url, devType, maxW, maxH, context);
            }
            
            //解码成gif动态图对象
            if (filePath != null)
            {
                InputStream is = null;
                try
                {
                    is = new FileInputStream(filePath);
                    gif = new GifOpenHelper();
                    gif.read(is);
                }
                catch (FileNotFoundException e)
                {
                }
                catch (OutOfMemoryError e)
                {
                }
                finally
                {
                    if (is != null)
                    {
                        is.close();
                    }
                }
            }
        }
        catch (MalformedURLException e)
        {
        }
        catch (IOException e)
        {
        }
        finally
        {
            // 关闭资源
            closeConn(conn, inputStream);
        }
        mLog.d(TAG, "createGifFromNet-3---->CurrentTime:" + DateUtil.getCurrentTime());
        
        return gif;
    }
    
    public static final SoftReference<Bitmap> createFromUri(Context context, String uri, int maxResolutionX,
        int maxResolutionY, long cacheId, ClientConnectionManager connectionManager, int devType, int fileSize,
        String name)
        throws IOException, URISyntaxException, OutOfMemoryError
    {
        
        mLog.d(TAG, "createFromUri 1");
        final BitmapFactory.Options options = new BitmapFactory.Options();
        mLog.d(TAG, "createFromUri 2");
        options.inScaled = false;
        
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inDither = true;
        long crc64 = 0;
        Bitmap bitmap = null;
        SoftReference<Bitmap> srBmp = null;
        mLog.d(TAG, "createFromUri--->devType-->" + devType);
        // 如果播放的是U盘、sdcard、储存在本地云设备的图片则直接去本地读取而不去下载到缓存
        if (ConstData.DeviceType.isLocalDevice(devType) || devType == ConstData.DeviceType.DEVICE_TYPE_NFS || devType == ConstData.DeviceType.DEVICE_TYPE_SMB)
        {
            mLog.d(TAG, "createFromUri--->createFromLocal--->start--->" + DateUtil.getCurrentTime());
            
            bitmap = createFromLocal(uri, maxResolutionX, maxResolutionY);
            
            mLog.d(TAG, "createFromUri--->createFromLocal--->end----->" + DateUtil.getCurrentTime());
            mLog.d(TAG, "createFromUri--->createFromLocal--->bitmap:" + bitmap);
        }
        else
        {
            // SCHEME 的形式
            if (uri.startsWith(ContentResolver.SCHEME_CONTENT))
            {
                mLog.d(TAG, "createFromUri 3");
                
                crc64 = cacheId;
            }
            else
            {
                mLog.d(TAG, "createFromUri 4");
                
                mLog.d(TAG, "uri:" + uri);
                
                crc64 = Utils.Crc64Long(uri);
            }
            mLog.d(TAG, "createFromUri 5");
            
            // 先去缓存加载，如果没有再去网络下载
            bitmap = createFromCache(crc64, maxResolutionX, maxResolutionY);
            
            mLog.d(TAG, "---->createFromCache bitmap==" + bitmap);
            mLog.d(TAG, "createFromUri 6");
        }
        
        if (bitmap != null)
        {
            mLog.d(TAG, "bitmap.getWidth() = " + bitmap.getWidth() + " ;bitmap.getHeight() = " + bitmap.getHeight());
            mLog.d(TAG, "createFromUri 7");
            
            srBmp = new SoftReference<Bitmap>(bitmap);
            
            bitmap = null;
            
            return srBmp;
        }
        
        mLog.d(TAG, "createFromUri 8");
        
        // whether it is content format
        if (uri.startsWith(ContentResolver.SCHEME_CONTENT) || uri.startsWith(ContentResolver.SCHEME_FILE))
        {
            BufferedInputStream bufferedInput = null;
            mLog.d(TAG, "createFromUri 10");
            // Get the stream from a local file.
            bufferedInput =
                new BufferedInputStream(context.getContentResolver().openInputStream(Uri.parse(uri)), 50 * 1024);// DTS2010082401913
            
            // Compute the sample size, i.e., not decoding real pixels.
            if (bufferedInput != null)
            {
                mLog.d(TAG, "createFromUri 13");
                options.inSampleSize = computeSampleSize(bufferedInput, maxResolutionX, maxResolutionY);
                
            }
            
            bufferedInput =
                new BufferedInputStream(context.getContentResolver().openInputStream(Uri.parse(uri)), 50 * 1024);
            
            mLog.d(TAG, "createFromUri 15");
            options.inDither = true;
            options.inJustDecodeBounds = false;
            
            bitmap = BitmapFactory.decodeStream(bufferedInput, null, options);
            
            srBmp = new SoftReference<Bitmap>(bitmap);
            
            bitmap = null;
        }
        else
        { // resource from net
            mLog.d(TAG, "createFromUri 16");
            
            mLog.d(TAG, "resource from net: " + uri);
            
            //modify by xkf76249 增加一个分支判断 ，判断url是否是来自分享图片
            if (devType == ConstData.DeviceType.DEVICE_TYPE_CLOUD
                && (StringUtils.isNetworkURI(uri) || uri.startsWith("/Photoshare")))
            {
                mLog.d(TAG, "CreateCloudPicBitMap is net url" + name);
                //modify by xkf76249 增加一个图片名字参数
                srBmp = createCloudPicBitMap(uri, maxResolutionX, maxResolutionY, devType, name);
            }
            else
            {
                srBmp = createBitmapFromNet(uri, maxResolutionX, maxResolutionY, devType);
            }
            if (srBmp == null)
            {
                mLog.d(TAG, "CreateBitMap --> srBmp == null");
            }
            else
            {
                mLog.d(TAG, "CreateBitMap --> srBmp != null");
            }
            
        }
        
        mLog.d(TAG, "createFromUri 17");
        
        return srBmp;
    }
    
    /**
     * 等待图片下载完成
     * @param fileSize 文件大小
     */
    private static void waitForDownloadFinished(long crc64, int maxResolutionX, int maxResolutionY, int fileSize)
    {
        // 查找缓存图片名字
        String fileName = createFileName(crc64, maxResolutionX);
        String[] strArr = findFile(fileName);
        if (strArr == null || strArr.length < 2)
        {
            return;
        }
        String fileStr = strArr[0];
        mLog.d(TAG, "waitForDownloadFinished files----->" + fileStr);
        if (StringUtils.isEmpty(fileStr))
        {
            return;
        }
        mLog.d(TAG, "waitForDownloadFinished start--fileSize--->" + fileSize);
        File file = new File(fileStr);
        if (file.exists())
        {
            while (true)
            {
                long firstLength = file.length();
                if (firstLength == fileSize || firstLength == 0)
                {
                    break;
                }
                else
                {
                    continue;
                }
            }
            
        }
        mLog.d(TAG, "waitForDownloadFinished end----->");
        
    }
    
    /**
     * 获取云相册的图片 先下载到本地，再去获取
     */
    private static SoftReference<Bitmap> createCloudPicBitMap(String url, int w, int h, int devType, String name)
    {
        
        if (StringUtils.isEmpty(url))
        {
            return null;
        }
        if (!StringUtils.isNetworkURI(url))
        {
            if (!url.startsWith("/Photoshare"))
            {
                return null;
            }
        }
        mLog.d(TAG, "createCloudPicBitMap--->start-->url-->" + url);
        long crc64 = Utils.Crc64Long(url);
        Bitmap bitmap = null;
        
        mLog.d(TAG, "DownloadImageToCache--->start");
        
        // 下载到缓存 modify by xkf76249 增加2个参数 context 和图片名字
        String fileP = DownloadCloudImageToCache(url, devType, w, h, mContext, name);
        mLog.d(TAG, "DownloadImageToCache--->end");
        
        Performance decodePf = new Performance("DECODE PF");
        decodePf.start();
        
        mLog.d(TAG, "CreateCloudPicBitMap Start to Retrieve bitmap from the cache");
        
        // 从缓存获取
        if (!bCanceled)
        {
            // 从缓存获取
            
            mLog.d(TAG, "!bDownloadCloudFailed && !bCanceled");
            bitmap = createFromCache_cloud(fileP, w, h);
        }
        
        bDownloadCloudFailed = false;
        
        decodePf.end();
        
        if (bitmap == null)
        {
            mLog.d(TAG, "CreateCloudPicBitMap - bitmap == null");
            
            return null;
        }
        
        mLog.d(TAG, "CreateCloudPicBitMap return softReference bmp");
        
        SoftReference<Bitmap> srBmp = new SoftReference<Bitmap>(bitmap);
        bitmap = null;
        mLog.d(TAG, "createCloudPicBitMap--->end-->srBmp" + srBmp);
        return srBmp;
    }
    
    /**
     * 去缓存获取Bitmap
     * 
     * @author zWX160481
     * @param crc64
     * @param maxResolution
     * @return Bitmap
     * @exception
     */
    public static Bitmap createFromCache_cloud(String filePath, int maxResolutionX, int maxResolutionY)
    {
        mLog.d(TAG, "createFromCache_cloud --> start");
        
        Bitmap bitmap = null;
        BitmapFactory.Options options = null;
        try
        {
            if (StringUtils.isEmpty(filePath))
            {
                return null;
            }
            
            File file = new File(filePath);
            if (!file.exists())
            {
                return null;
            }
            
            // 获取采样率
            options = getOptions(filePath, maxResolutionX, maxResolutionY);
            
            // 动态设置ImageSwitcher的ImageView 的ScaleType
            if (options != null)
            {
                setImageScaleType(options.outWidth, options.outHeight, maxResolutionX, maxResolutionY);
                mPicSizeX = options.outWidth;
                mPicSizeY = options.outHeight;
            }
            if(options != null){
                mLog.d(TAG, "createFromCache--->decodeFile 1--->options.inSampleSize--->" + options.inSampleSize);
            }
            mLog.d(TAG, "createFromCache -->decodeFile 1 start  -->time:" + DateUtil.getCurrentTime());
            
            // 会发生内存泄露危险
            bitmap = BitmapFactory.decodeFile(filePath, options);
            mLog.d(TAG, "createFromCache -->decodeFile 1 end  -->time:" + DateUtil.getCurrentTime());
        }
        catch (OutOfMemoryError e)
        {
            mLog.e(TAG, "createFromCache-->OutOfMemoryError-->1");
            System.gc();
            if (bitmap != null && !bitmap.isRecycled())
            {
                bitmap.recycle();
                bitmap = null;
            }
            
            mLog.w(TAG, "decode image failed 1:" + e.getLocalizedMessage());
            if (options != null && options.inSampleSize == 1)
            {
                options.inSampleSize = 2;
            }
            if(options != null){
                options.inSampleSize *= 2;
            }
            try
            {
                if(options != null){
                    mLog.d(TAG, "try decode 2 inSampleSize====" + options.inSampleSize);
                }
                mLog.d(TAG, "try decode 2 file====" + filePath);
                mLog.d(TAG, "createFromCache -->decodeFile 2 start  -->time:" + DateUtil.getCurrentTime());
                
                bitmap = BitmapFactory.decodeFile(filePath, options);
                
                mLog.d(TAG, "createFromCache -->decodeFile 2 start  -->time:" + DateUtil.getCurrentTime());
                
                mLog.d(TAG, "createFromCache -->decodeFile 2  bitmap= " + bitmap);
            }
            catch (OutOfMemoryError outOfMemoryError)
            {
                mLog.e(TAG, "createFromCache-->OutOfMemoryError-->2");
                mLog.w(TAG, "createFromCache -->decodeFile failed 2：" + e.getLocalizedMessage());
                System.gc();
                if (bitmap != null && !bitmap.isRecycled())
                {
                    bitmap.recycle();
                    bitmap = null;
                }
            }
            catch (Exception ex)
            {
                mLog.w(TAG, "decode image failed 2:" + e.getLocalizedMessage());
                bitmap = null;
            }
        }
        mLog.d(TAG, "createFromCache -->bitmap:" + bitmap);
        return bitmap;
    }
    
    private static Callback cb = new Callback();
    
    private static MultiThreadDownloader mtd = null;
    
    public static boolean bCanceled = false;
    
    private static Object shareImageLock = new Object();
    
    public static CloudShareImageDownloader shareImageDownloader;
    
    /**
     * 下载云网络图片到缓存 采用curl命令下载 modify by xkf76249 重载该方法
     */
    public static String DownloadCloudImageToCache(String url, int devType, int w, int h, Context context, String name)
    {
        if (StringUtils.isEmpty(url))
        {
            return null;
        }
        mLog.d(TAG, "DownloadCloudImageToCache --> url:" + url);
        mLog.d(TAG, "DownloadCloudImageToCache --> devType:" + devType);
        mLog.d(TAG, "DownloadCloudImageToCache --> w:" + w);
        mLog.d(TAG, "DownloadCloudImageToCache --> h:" + h);
        
        if (url.startsWith("/Photoshare/"))
        {
            synchronized (shareImageLock)
            {
                mLog.d(TAG, "start shareImageLock time == " + System.currentTimeMillis());
                if (dlnaImageSwitcherViewFactory != null)
                {
                    dlnaImageSwitcherViewFactory.displayProgreeBar();
                }
                bCanceled = false;
                CloudShareImageDownloader download = new CloudShareImageDownloader(url, context, name);
                shareImageDownloader = download;
                download.download();
                
                if (shareImageDownloader == null)
                {
                    mLog.d(TAG, "shareImageDownloader");
                    return null;
                }
                String savePath = null;
                
                try
                {
                    savePath = shareImageDownloader.getSavePath();
                }
                catch (Exception e)
                {
                }
                mLog.d(TAG, "end shareImageLock time == " + System.currentTimeMillis() + "-path =" + savePath);
                return savePath;
            }
            
        }
        else
        {
            long crc64 = Utils.Crc64Long(url);
            
            mLog.d(TAG, "DownloadCloudImageToCache--->start time--->" + DateUtil.getCurrentTime());
            String file = null;
            try
            {
                // 增加性能计数
                Performance writetocachePf = new Performance("WRITE BMP STREAM INTO CACHE PF");
                
                // 保存图片到临时空间
                writetocachePf.start();
                
                String size = "0x0";
                int degree = 0;
                file = createFilePathFromCrc64WithSize(crc64, w, size, degree);
                File findFile = new File(file);
                
                mLog.d(TAG, "DownloadCloudImageToCache--->start curlDownload:" + DateUtil.getCurrentTime());
                
                // 写入缓冲
                URL urlUri = null;
                try
                {
                    mLog.d("TSS", "URL 1");
                    urlUri = new URL(url);
                    mLog.d("TSS", "URL 2");
                }
                catch (MalformedURLException e)
                {
                    mLog.d("TSS", "URL MalformedURLException 1");
                    return null;
                }
                mLog.d("TSS", "URL 3");
                mtd = new MultiThreadDownloader(urlUri, findFile);
                
                mtd.setTempDir(URI_CACHE);
                mtd.setMListener(cb);
                
                mLog.d("TSS", "mtd:" + mtd + " url:" + url + " local:" + file);
                
                bCanceled = false;
                if (dlnaImageSwitcherViewFactory != null)
                {
                    dlnaImageSwitcherViewFactory.displayProgreeBar();
                }
                mtd.download();
                
                // 如果是取消掉的则删除本地的缓存
                if (mbStop)
                {
                    deleteCacheImage(file);
                }
                mtd = null;
                
                mLog.d(TAG, "mtd download end--------->");
                
                mLog.d(TAG, "curlDownload--->file:" + file);
                mLog.d(TAG, "DownloadCloudImageToCache--->end :" + DateUtil.getCurrentTime());
                writetocachePf.end();
                
            }
            catch (Exception e)
            {
            }
            return file;
        }
        
    }
    
    public static String DownloadCloudImageToCache(String url, int devType, int w, int h, Context context)
    {
        if (StringUtils.isEmpty(url))
        {
            return null;
        }
        // String url = info.getData();
        mLog.d(TAG, "DownloadCloudImageToCache --> url:" + url);
        mLog.d(TAG, "DownloadCloudImageToCache --> devType:" + devType);
        mLog.d(TAG, "DownloadCloudImageToCache --> w:" + w);
        mLog.d(TAG, "DownloadCloudImageToCache --> h:" + h);
        
        long crc64 = Utils.Crc64Long(url);
        
        mLog.d(TAG, "DownloadCloudImageToCache--->start time--->" + DateUtil.getCurrentTime());
        String file = null;
        try
        {
            // 增加性能计数
            Performance writetocachePf = new Performance("WRITE BMP STREAM INTO CACHE PF");
            
            // 保存图片到临时空间
            writetocachePf.start();
            
            String size = "0x0";
            int degree = 0;
            file = createFilePathFromCrc64WithSize(crc64, w, size, degree);
            File findFile = new File(file);
            
            mLog.d(TAG, "DownloadCloudImageToCache--->start curlDownload:" + DateUtil.getCurrentTime());
            
            // 写入缓冲
            URL urlUri = null;
            try
            {
                mLog.d("TSS", "URL 1");
                urlUri = new URL(url);
                mLog.d("TSS", "URL 2");
            }
            catch (MalformedURLException e)
            {
                mLog.d("TSS", "URL MalformedURLException 1");
                return null;
            }
            mLog.d("TSS", "URL 3");
            mtd = new MultiThreadDownloader(urlUri, findFile);
            
            mtd.setTempDir(URI_CACHE);
            mtd.setMListener(cb);
            
            mLog.d("TSS", "mtd:" + mtd + " url:" + url + " local:" + file);
            
            bCanceled = false;
            if (dlnaImageSwitcherViewFactory != null)
            {
                dlnaImageSwitcherViewFactory.displayProgreeBar();
            }
            mtd.download();
            
            // 如果是取消掉的则删除本地的缓存
            if (mbStop)
            {
                deleteCacheImage(file);
            }
            mtd = null;
            
            mLog.d(TAG, "mtd download end--------->");
            
            mLog.d(TAG, "curlDownload--->file:" + file);
            mLog.d(TAG, "DownloadCloudImageToCache--->end :" + DateUtil.getCurrentTime());
            writetocachePf.end();
            
        }
        catch (Exception e)
        {
        }
        return file;
        
    }
    /**
     * 连接超时任务
     */
    static class ConnectTimerTask extends java.util.TimerTask
    {
        @Override
        public void run()
        {
            mLog.d(TAG, "ConnectTimerTask run--------->");
            cancelAxelDownload();
        }
    }
    
    private static boolean bDownloadCloudFailed = false;
    
    /**
     * 下载云相册图片回调函数
     */
    static class Callback implements MultiDownloadListener
    {
        
        @Override
        public void onError(MultiThreadDownloader mtd, int errCode, Object detail)
        {
            
            if (errCode != 0)
            {
                bDownloadCloudFailed = true;
                CurlDownload.mCurlCode = 0;
            }
            else
            {
                bDownloadCloudFailed = false;
            }
            
        }
        
    }
    
    public static boolean mbDownloadStatu = false;
    
    /**
     * 主线程(UI线程) 对于显示控件的界面更新只是由UI线程负责，如果是在非UI线程更新控件的属性值，更新后的显示界面不会反映到屏幕上
     * 如果想让更新后的显示界面反映到屏幕上，需要用Handler设置。
     * @param path
     * @param savedir
     */
    public static void download(final String path, final File savedir, final String saveFileName)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                // 开启3个线程进行下载
                FileDownloader loader = new FileDownloader(mContext, path, savedir, 3, saveFileName);
                final int fileSize = loader.getFileSize();
                mLog.d(TAG, "mutThreadDownload fileSize-->" + fileSize);
                try
                {
                    loader.download(new DownloadProgressListener()
                    {
                        @Override
                        public void onDownloadSize(int size)
                        {// 实时获知文件已经下载的数据长度
                            mLog.d(TAG, "mutThreadDownload success -size--->" + size);
                            if (size == fileSize)
                            {
                                mLog.d(TAG, "mutThreadDownload success");
                            }
                        }
                        
                        @Override
                        public void onDownloadSuccess()
                        {
                            mLog.d(TAG, "onDownloadSuccess--->");
                            mbDownloadStatu = true;
                        }
                        
                        @Override
                        public void onDownloadError()
                        {
                            mLog.d(TAG, "onDownloadError--->");
                        }
                    });
                }
                catch (Exception e)
                {
                }
            }
        }).start();
    }
    
    // 删除缓存图片
    private synchronized static void deleteCacheImage(String fileName)
    {
        if (!StringUtils.isEmpty(fileName))
        {
            File file = new File(fileName);
            if (file.exists())
            {
                mLog.d(TAG, "delete curl dowad cache file---->");
                file.delete();
            }
        }
    }
    
    /**
     * 取消下载云相册图片
     */
    public static void cancelDownload()
    {
        mLog.d(TAG, "cancelDownload");
        cancelAxelDownload();
    }
    public static synchronized void cancelAxelDownload()
    {
        if (mtd != null)
        {
            mLog.d(TAG, "mtd.stop()---->");
            mtd.stop();
            mbStop = true;
            bCanceled = true;
            mtd = null;
        }
        else
        {
            bCanceled = false;
        }
        if (UriTexture.shareImageDownloader != null)
        {
            UriTexture.shareImageDownloader.stop();
            mbStop = true;
            bCanceled = true;
            UriTexture.shareImageDownloader = null;
        }
    }
    
    /**
     * 设置ImageView的scaleType
     * @author zWX160481
     * @param size 图片原尺寸 如1920x1080
     */
    private static void setImageScaleType(String size, Options options, int maxResolutionX, int maxResolutionY)
    {
        if (StringUtils.isEmpty(size) || size.lastIndexOf("x") == -1)
        {
            return;
        }
        int with = 0;
        int height = 0;
        String withStr = size.substring(0, size.lastIndexOf("x"));
        String heightStr = size.substring(size.lastIndexOf("x") + 1);
        if (withStr != null)
        {
            with = Integer.parseInt(withStr);
        }
        if (heightStr != null)
        {
            height = Integer.parseInt(heightStr);
        }
        if (with == 0 && height == 0 && options != null && options.outWidth != -1 && options.outHeight != -1)
        {
            with = options.outWidth;
            height = options.outHeight;
        }
        setImageScaleType(with, height, maxResolutionX, maxResolutionY);
    }
    
    /**
     * 设置ImageView的scaleType
     * @author zWX160481
     * @param imageWith 图片的with
     * @param imageHeight 图片height
     * @param maxResolutionX 要显示图片的最大分辨率
     */
    private static void setImageScaleType(int imageWith, int imageHeight, int maxResolutionX, int maxResolutionY)
    {
        if (imageHeight >= maxResolutionY && imageWith >= maxResolutionX && dlnaImageSwitcherViewFactory != null)
        {
            mLog.d(TAG, "dlnaImageSwitcherViewFactory.setScaleTypeFixCenter()");
            dlnaImageSwitcherViewFactory.setScaleTypeFixCenter();
        }
        else if (dlnaImageSwitcherViewFactory != null)
        {
            mLog.d(TAG, "dlnaImageSwitcherViewFactory.setScaleTypeCenterInSide()");
            dlnaImageSwitcherViewFactory.setScaleTypeCenterInSide();
        }
    }
    
    /**
     * 读取网络图片
     * 
     * @param url
     * @param options
     * @return 经过采用后的图片位图的软引用
     */
    public static SoftReference<Bitmap> createBitmapFromNet(final String url, final int maxResolutionX,
        final int maxResolutionY, int devType)
    {
        mLog.d(TAG, "createFromNet------->start time:" + DateUtil.getCurrentTime());
        mLog.d(TAG, "createFromNet---url--->:" + url);
        
        if (StringUtils.isEmpty(url) || !StringUtils.isNetworkURI(url))
        {
            return null;
        }
        int degree = 0;
        InputStream inputStream = null;
        Bitmap bitmap = null;
        HttpURLConnection conn = null;
        BitmapFactory.Options options = null;
        URL mUrl = null;
        boolean isSaveBitmap = true;
        try
        {
            mLog.d(TAG, "createFromNet 1---->:" + DateUtil.getCurrentTime());
            
            // 获取采样率
            mUrl = new URL(url);
            conn = (HttpURLConnection)mUrl.openConnection();
            // l00174030, DTS2013091405382,幻灯片播放时5秒过短，改为10S
            //conn.setConnectTimeout(5 * 1000);
            conn.setConnectTimeout(10 * 1000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // l00174030, DTS2013091405382,幻灯片播放时5秒过短，改为10S
            conn.setReadTimeout(10 * 1000);
            if (conn.getResponseCode() == 200)
            {
                inputStream = conn.getInputStream();
            }
            
            mLog.d(TAG, "createFromNet-2---->:" + DateUtil.getCurrentTime());
            
            if (inputStream != null)
            {
                options = getOptions(inputStream, maxResolutionX, maxResolutionY);
                
                // 动态设置ImageSwitcher的ImageView 的ScaleType
                setImageScale(options, maxResolutionX, maxResolutionY);
            }
            
            // 关闭资源
            closeConn(conn, inputStream);
            
            mLog.d(TAG, "createFromNet-3---->:" + DateUtil.getCurrentTime());
            
            // 解码图片
            mUrl = new URL(url);
            conn = (HttpURLConnection)mUrl.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            if (conn.getResponseCode() == 200)
            {
                inputStream = conn.getInputStream();
            }
            mLog.d(TAG, "createFromNet-4---->:" + DateUtil.getCurrentTime());
            try
            {
                mLog.d(TAG, "createFromNet decodeStream 1 start------->:" + DateUtil.getCurrentTime());
                
                bitmap = BitmapFactory.decodeStream(inputStream, null, options);
                
                if (bitmap != null && options != null)
                {
                    mLog.d(TAG, "createFromNet-->inSampleSize->" + options.inSampleSize);
                    //分辨率大于屏幕的图片，要根据屏幕大小等比例缩放图片。
                    //由于图片只能2的等比数采样，所以采样后要根据屏幕大小等比例缩放图片，才能满屏显示
                    if(options.inSampleSize > 1 && options.outHeight < maxResolutionY && options.outWidth < maxResolutionX )
                    {
                        float srcAspect = bitmap.getWidth() * 1.0f / bitmap.getHeight();
                        float dstAspect = maxResolutionX * 1.0f / maxResolutionY;
                        Rect srcRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
                        Rect dstRect = null;
                        if (srcAspect > dstAspect)
                        {
                            dstRect = new Rect(0, 0, maxResolutionX, (int)(maxResolutionX / srcAspect));
                        }
                        else
                        {
                            dstRect = new Rect(0, 0, (int)(maxResolutionY * srcAspect), maxResolutionY);
                        }
                        Bitmap scaledBitmap = Bitmap.createBitmap(dstRect.width(), dstRect.height(), Config.ARGB_8888);
                        
                        Canvas canvas = new Canvas(scaledBitmap);
                        
                        //该方法能很好的保持图片的清晰度
                        canvas.drawBitmap(bitmap, srcRect, dstRect, new Paint(Paint.FILTER_BITMAP_FLAG));
                        
                        if (scaledBitmap != bitmap && !bitmap.isRecycled())
                        {
                            bitmap.recycle();
                        }
                        bitmap = scaledBitmap;
                        scaledBitmap = null;
                    }
                }
                
                if (bitmap == null)
                {
                    mLog.d(TAG, "createFromNet-->bitmap--->null");
                    SoftReference<Bitmap> srBmp = CreateBitMap(url, devType, options, maxResolutionX, maxResolutionY);
                    return srBmp;
                }
                
                mLog.d(TAG, "createFromNet decodeStream 1 end--------->" + DateUtil.getCurrentTime());
            }
            catch (OutOfMemoryError e)
            {
                mLog.d(TAG, "createBitmapFromNet--->OutOfMemoryError--->1");
                mLog.w(TAG, e.getLocalizedMessage());
                if (bitmap != null && !bitmap.isRecycled())
                {
                    bitmap.recycle();
                    bitmap = null;
                }
                System.gc();
                options.inSampleSize *= 2;
                try
                {
                    mLog.d(TAG, "decodeStream 2 start-------> " + DateUtil.getCurrentTime());
                    
                    bitmap = BitmapFactory.decodeStream(inputStream, null, options);
                    //缩小的图不能重用，故不保存,
                    isSaveBitmap = false;
                    
                    mLog.d(TAG, "decodeStream 2 end------->" + DateUtil.getCurrentTime());
                }
                catch (OutOfMemoryError e1)
                {
                    mLog.d(TAG, "createBitmapFromNet--->OutOfMemoryError--->2");
                    mLog.w(TAG, e1.getLocalizedMessage());
                    if (bitmap != null && !bitmap.isRecycled())
                    {
                        bitmap.recycle();
                        bitmap = null;
                    }
                    System.gc();
                }
                catch (Exception e2)
                {
                    //提示网络连接超时
                    CurlDownload.mCurlCode = 28;
                }
            }
            catch (Exception e)
            {
                //提示网络连接超时
                CurlDownload.mCurlCode = 28;
            }
            finally
            {
                
                // 关闭资源
                closeConn(conn, inputStream);
            }
            
            mLog.d(TAG, "createFromNet-5---->:" + DateUtil.getCurrentTime());
            mLog.d(TAG, "createFromNet-6---->:" + DateUtil.getCurrentTime());
            mLog.d(TAG, "openConnection------->444:" + DateUtil.getCurrentTime());
        }
        catch (Exception e)
        {
            //提示网络连接超时
            CurlDownload.mCurlCode = 28;
        }
        catch (OutOfMemoryError e)
        {
            mLog.d(TAG, "createBitmapFromNet-->OutOfMemoryError");
            System.gc();
            mLog.w(TAG, e.getLocalizedMessage());
            return null;
        }
        if (bitmap == null)
        {
            return null;
        }
        
        // 将图片保存到缓存
        String size = mPicSizeX + "x" + mPicSizeY;
        if(isSaveBitmap)
        {
            saveBitmap(bitmap, url, size, maxResolutionX);
        }
        mLog.d(TAG, "createFromNet------->end time:" + DateUtil.getCurrentTime());
        return new SoftReference<Bitmap>(bitmap);
    }
    
    /**
     * @author zWX160481 关闭资源
     **/
    public static void closeConn(HttpURLConnection conn, InputStream inputStream)
    {
        if (conn != null)
        {
            conn.disconnect();
        }
        if (inputStream != null)
        {
            try
            {
                inputStream.close();
            }
            catch (IOException e)
            {
            }
        }
    }
    
    /**
     * 
     * 设置ImageView的scaletype
     */
    private static void setImageScale(Options options, int maxResolutionX, int maxResolutionY)
    {
        if (options.outHeight >= maxResolutionY && options.outWidth >= maxResolutionX
            && dlnaImageSwitcherViewFactory != null)
        {
            mLog.d(TAG, "dlnaImageSwitcherViewFactory.setScaleTypeFixCenter()");
            dlnaImageSwitcherViewFactory.setScaleTypeFixCenter();
        }
        else if (dlnaImageSwitcherViewFactory != null)
        {
            mLog.d(TAG, "dlnaImageSwitcherViewFactory.setScaleTypeCenterInSide()");
            dlnaImageSwitcherViewFactory.setScaleTypeCenterInSide();
        }
    }
    
    /**
     * 对jpeg图片进行解码获取拍摄角度
     * @return 拍摄角度
     */
    public static int getOrJpeg(InputStream inputStream)
    {
        JpegHeaders headers = null;
        int orientation = 0;
        int degree = 0;
        try
        {
            headers = new JpegHeaders(inputStream);
        }
        catch (IOException e)
        {
        }
        if(headers != null){
            Section sce = (Section)headers.m_sectionList.get(0);
            ReadJpg.setBytes(sce.asBytes());
            ReadJpg.EXIF_process_EXIF(0, 0, 2, sce.asBytes().length);
        }
        System.out.println("m_pExifInfo.Orientation--------->" + ReadJpg.m_pExifInfo.Orientation);
        System.out.println("finished");
        orientation = ReadJpg.m_pExifInfo.Orientation;
        if (orientation != -1)
        {
            switch (orientation)
            {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        }
        return degree;
    }
    
    private static void writeToCache(String bigfilename2, InputStream in)
    {
        if (in == null)
        {
            return;
        }
        try
        {
            // 必须reset，否则获取不到bitmap
            in.reset();
        }
        catch (IOException e)
        {
        }
        mLog.d(TAG, "writeToCache--->file:" + bigfilename2);
        FileOutputStream fos = null;
        BufferedInputStream bis = null;
        
        // 8k
        int BUFFER_SIZE = 8192;
        byte[] buf = new byte[BUFFER_SIZE];
        int size = 0;
        
        try
        {
            // 获取网络输入流
            bis = new BufferedInputStream(in);
            // 建立文件
            fos = new FileOutputStream(bigfilename2);
            
            // 保存文件
            while ((size = bis.read(buf)) != -1)
            {
                fos.write(buf, 0, size);
            }
            
            fos.flush();
            mLog.d(TAG, "writeToCache--->close");
        }
        catch (IOException e)
        {
            mLog.d(TAG, "writeToCache--->IOException:" + e.getLocalizedMessage());
        }
        finally
        {
            if (bis != null)
            {
                try
                {
                    bis.close();
                }
                catch (IOException e)
                {
                }
            }
            if (fos != null)
            {
                try
                {
                    fos.close();
                }
                catch (IOException e)
                {
                }
            }
        }
    }
    
    /**
     * 保存图片到缓存
     * @author zWX160481
     * @param hBitmap 经过压缩的位图
     * @param url 图片路径
     * @param size 图片尺寸
     * @param maxResolutionX 指定图片的尺寸with
     */
    private static void saveBitmap(final Bitmap hBitmap, final String url, final String size, final int maxResolutionX)
    {
        new Thread()
        {
            public void run()
            {
                mLog.d(TAG, "saveBitmap--->start---->" + DateUtil.getCurrentTime());
                if (hBitmap == null || url == null)
                {
                    return;
                }
                if (hBitmap != null && hBitmap.isRecycled())
                {
                    return;
                }
                int degree = 0;
                InputStream inputStream = null;
                HttpURLConnection conn = null;
                URL mUrl = null;
                
                // 获取网络图片的拍摄角度
                try
                {
                    mUrl = new URL(url);
                    conn = (HttpURLConnection)mUrl.openConnection();
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    if (conn.getResponseCode() == 200)
                    {
                        inputStream = conn.getInputStream();
                        degree = getOrJpeg(inputStream);
                    }
                }
                catch (Exception e)
                {
                }
                finally
                {
                    
                    // 关闭资源
                    closeConn(conn, inputStream);
                }
                
                long crc64 = Utils.Crc64Long(url);
                
                // 生成文件名
                String fileName = createFilePathFromCrc64WithSize(crc64, maxResolutionX, size, degree);
                File f = new File(fileName);
                FileOutputStream fOut = null;
                try
                {
                    f.getParentFile().mkdirs();
                    f.createNewFile();
                    fOut = new FileOutputStream(f);
                    hBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                    fOut.flush();
                    fOut.close();
                    mLog.d(TAG, "saveBitmap--->end---->" + DateUtil.getCurrentTime());
                }
                catch (Exception e1)
                {
                }
                finally
                {
                    if (fOut != null)
                    {
                        try
                        {
                            fOut.close();
                        }
                        catch (IOException e)
                        {
                        }
                    }
                }
            };
        }.start();
        
    }
    
    /**
     * 读取u盘或者sdcard的图片
     */
    private static Bitmap createFromLocal(String uri, int maxResolutionX, int maxResolutionY)
    {
        mLog.d(TAG, "createFromLocal --> maxResolutionX:" + maxResolutionX);
        mLog.d(TAG, "createFromLocal --> maxResolutionY:" + maxResolutionY);
        mLog.d(TAG, "createFromLocal --> uri:" + uri);
        Bitmap bitmap = null;
        BitmapFactory.Options options = null;
        
        // 获取采样率
        options = getOptions(uri, maxResolutionX, maxResolutionY);
        
        // 动态设置ImageSwitcher的ImageView 的ScaleType
        if (options.outHeight >= maxResolutionY && options.outWidth >= maxResolutionX
            && dlnaImageSwitcherViewFactory != null)
        {
            mLog.d(TAG, "dlnaImageSwitcherViewFactory.setScaleTypeFixCenter()");
            dlnaImageSwitcherViewFactory.setScaleTypeFixCenter();
        }
        else if (dlnaImageSwitcherViewFactory != null)
        {
            mLog.d(TAG, "dlnaImageSwitcherViewFactory.setScaleTypeCenterInSide()");
            dlnaImageSwitcherViewFactory.setScaleTypeCenterInSide();
        }
        
        mLog.e(TAG, "createFromLocal--->decodeFile--->options.inSampleSize：" + options.inSampleSize);
        try
        {
            mLog.d(TAG, "createFromLocal -->decodeFile 1 start  -->time:" + DateUtil.getCurrentTime());
            // 会发生内存泄露危险
            bitmap = BitmapFactory.decodeFile(uri, options);
            
            mLog.d(TAG, "createFromLocal -->decodeFile 1 end  --->time:" + DateUtil.getCurrentTime());
            mLog.e(TAG, "createFromLocal -->decodeFile 1 bitmap--->" + bitmap);
        }
        catch (OutOfMemoryError e)
        {
            
            System.gc();
            mLog.e(TAG, "createFromLocal 1 --->OutOfMemoryError");
            if (bitmap != null && !bitmap.isRecycled())
            {
                bitmap.recycle();
                bitmap = null;
            }
            
            mLog.w(TAG, "decode image failed 1:" + e.getLocalizedMessage());
            if (options.inSampleSize == 1)
            {
                options.inSampleSize = 2;
            }
            options.inSampleSize *= 2;
            try
            {
                mLog.d(TAG, "try decode inSampleSize again-->" + options.inSampleSize);
                mLog.d(TAG, "createFromLocal -->decodeFile 2 start  -->time:" + DateUtil.getCurrentTime());
                mLog.d(TAG, "createFromLocal -->decodeFile 2 -->uri:" + uri);
                
                bitmap = BitmapFactory.decodeFile(uri, options);
                
                mLog.d(TAG, "createFromLocal -->decodeFile 2 end  -->time:" + DateUtil.getCurrentTime());
                mLog.e(TAG, "createFromLocal -->decodeFile 2 bitmap--->" + bitmap);
            }
            catch (OutOfMemoryError outOfMemoryError)
            {
                mLog.e(TAG, "createFromLocal 2 --->OutOfMemoryError");
                mLog.w(TAG, "decode image failed 2:" + e.getLocalizedMessage());
                System.gc();
                if (bitmap != null && !bitmap.isRecycled())
                {
                    bitmap.recycle();
                    bitmap = null;
                }
            }
            catch (Exception ex)
            {
                mLog.w(TAG, "decode image failed 2:" + e.getLocalizedMessage());
                bitmap = null;
            }
        }
        mLog.d(TAG, "createFromLocal -->bitmap:" + bitmap);
        return bitmap;
    }
    
    // 按缩放比例小的边缩放图片
    private static Bitmap scaleBitmap(Bitmap bitmap)
    {
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        float scaleWidth = ((float)BITMAP_MAX_W) / width;
        float scaleHeight = ((float)BITMAP_MAX_H) / height;
        float scale = scaleWidth > scaleHeight ? scaleHeight : scaleWidth;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        Bitmap newbm = null;
        try
        {
            // 得到新的图片 会发生内存泄露危险
            newbm = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        }
        catch (OutOfMemoryError e)
        {
            mLog.e(TAG, "scaleBitmap--->createBitmap--->OutOfMemoryError");
            newbm = null;
            return bitmap;
        }
        bitmap.recycle();
        bitmap = null;
        
        return newbm;
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
                mLog.w(TAG, "Request failed: " + uri);
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
                mLog.w(TAG, "Request failed: " + request.getURI());
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
     * @return String
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
        
        mLog.d(TAG, "createFilePathFromCrc64 PATH: " + sb.toString());
        
        return sb.toString();
    }
    
    public static final String createFileName(long crc64, int maxResolution)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(crc64);
        sb.append("_");
        sb.append(maxResolution);
        
        mLog.d(TAG, "createFileName: " + sb.toString());
        
        return sb.toString();
    }
    
    /**
     * append the cache file path with crc64 of url and the max resolution
     * createFilePathFromCrc64
     * 
     * @param crc64
     * @param maxResolution
     * @param size 尺寸
     * @param degree 角度
     * @return String
     * @exception
     */
    public static final String createFilePathFromCrc64WithSize(long crc64, int maxResolution, String size, int degree)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(URI_CACHE);
        sb.append("/");
        sb.append(crc64);
        sb.append("_");
        sb.append(maxResolution);
        sb.append("_");
        sb.append(size);
        sb.append("x");
        sb.append(degree);
        sb.append(".cache");
        
        mLog.d(TAG, "createFilePathFromCrc64 PATH: " + sb.toString());
        
        return sb.toString();
    }
    
    /**
     * append the cache file path with crc64 of url and the max resolution
     * createFilePathFromCrc64
     * 
     * @param crc64
     * @param maxResolution
     * @param size 尺寸
     * @param degree 角度
     * @return String
     * @exception
     */
    public static final String createFileNameFromCrc64WithSize(long crc64, int maxResolution, String size, int degree)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(crc64);
        sb.append("_");
        sb.append(maxResolution);
        sb.append("_");
        sb.append(size);
        sb.append("x");
        sb.append(degree);
        sb.append(".cache");
        
        mLog.d(TAG, "createFileNameFromCrc64WithSize fileName: " + sb.toString());
        
        return sb.toString();
    }
    
    /**
     * @author zWX160481 创建路径
     */
    public static final String createFilePath(String str)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(URI_CACHE);
        sb.append("/");
        sb.append(str);
        sb.append(".cache");
        
        mLog.d(TAG, "createFilePath PATH: " + sb.toString());
        
        return sb.toString();
    }
    
    /**
     * whether crc64 has existed isCached
     * 
     * @param crc64
     * @param maxResolution
     * @return boolean
     * @exception
     */
    public static boolean isCached(long crc64, int maxResolution)
    {
        
        String file = null;
        FileInputStream fi = null;
        
        if (crc64 != 0)
        {
            file = createFilePathFromCrc64(crc64, maxResolution);
            try
            {
                fi = new FileInputStream(file);
                int iret = fi.read();
                
                if (iret == -1)
                {
                    return false;
                }
                else
                {
                    return true;
                }
                
            }
            catch (FileNotFoundException e)
            {
                return false;
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
                }
            }
        }
        return false;
    }
    
    /**
     * 去缓存获取Bitmap
     * @author zWX160481
     * @param crc64
     * @param maxResolution
     * @return Bitmap
     * @exception
     */
    public static Bitmap createFromCache(long crc64, int maxResolutionX, int maxResolutionY)
    {
        mLog.d(TAG, "createFromCache --> maxResolutionX:" + maxResolutionX);
        mLog.d(TAG, "createFromCache --> maxResolutionY:" + maxResolutionY);
        
        String file = null;
        Bitmap bitmap = null;
        BitmapFactory.Options options = null;
        try
        {
            if (crc64 != 0)
            {
                file = createFilePathFromCrc64(crc64, maxResolutionX);
                
                // File filehandler = new File(file);
                
                // 查找缓存图片名字
                String fileName = createFileName(crc64, maxResolutionX);
                String[] strArr = findFile(fileName);
                if (strArr == null || strArr.length < 2)
                {
                    return null;
                }
                file = strArr[0];
                
                mLog.d(TAG, "files22222222----->" + file);
                if (StringUtils.isEmpty(file))
                {
                    return null;
                }
                // filehandler.setLastModified(System.currentTimeMillis());
                
                // 获取采样率
                options = getOptions(file, maxResolutionX, maxResolutionY);
                
                // 动态设置ImageSwitcher的ImageView 的ScaleType
                setImageScaleType(strArr[1], options, maxResolutionX, maxResolutionY);
                
                if (strArr != null && strArr.length >= 2 && strArr[1].lastIndexOf("x") != -1)
                {
                    mPicSizeX = Integer.parseInt(strArr[1].substring(0, strArr[1].lastIndexOf("x")));
                    mPicSizeY = Integer.parseInt(strArr[1].substring(strArr[1].lastIndexOf("x") + 1));
                }
                mLog.d(TAG, "createFromCache--->decodeFile 1--->options.inSampleSize--->" + options.inSampleSize);
                mLog.d(TAG, "createFromCache -->decodeFile 1 start  -->time:" + DateUtil.getCurrentTime());
                
                // 会发生内存泄露危险
                bitmap = BitmapFactory.decodeFile(file, options);
                mLog.d(TAG, "createFromCache -->decodeFile 1 end  -->time:" + DateUtil.getCurrentTime());
                
            }
        }
        catch (OutOfMemoryError e)
        {
            mLog.e(TAG, "createFromCache-->OutOfMemoryError-->1");
            System.gc();
            if (bitmap != null && !bitmap.isRecycled())
            {
                bitmap.recycle();
                bitmap = null;
            }
            
            mLog.w(TAG, "decode image failed 1:" + e.getLocalizedMessage());
            if (options != null && options.inSampleSize == 1)
            {
                options.inSampleSize = 2;
            }
            if (options != null){
                options.inSampleSize *= 2;
            }
            try
            {
                if(options != null){
                    mLog.d(TAG, "try decode 2 inSampleSize====" + options.inSampleSize);
                }
                mLog.d(TAG, "try decode 2 file====" + file);
                mLog.d(TAG, "createFromCache -->decodeFile 2 start  -->time:" + DateUtil.getCurrentTime());
                
                bitmap = BitmapFactory.decodeFile(file, options);
                
                mLog.d(TAG, "createFromCache -->decodeFile 2 start  -->time:" + DateUtil.getCurrentTime());
                
                mLog.d(TAG, "createFromCache -->decodeFile 2  bitmap= " + bitmap);
            }
            catch (OutOfMemoryError outOfMemoryError)
            {
                mLog.e(TAG, "createFromCache-->OutOfMemoryError-->2");
                mLog.w(TAG, "createFromCache -->decodeFile failed 2：" + e.getLocalizedMessage());
                System.gc();
                if (bitmap != null && !bitmap.isRecycled())
                {
                    bitmap.recycle();
                    bitmap = null;
                }
            }
            catch (Exception ex)
            {
                mLog.w(TAG, "decode image failed 2:" + e.getLocalizedMessage());
                bitmap = null;
            }
        }
        mLog.d(TAG, "createFromCache -->bitmap:" + bitmap);
        return bitmap;
    }
    
    /**
     * 二分法查找
     * @author zWX160481
     * @param array 要查找的数组
     * @param startIndex 查找的起始索引
     * @param endIndex 查找的结束索引
     * @param 要查找的值
     * @return 如果它包含在数组中，则返回搜索键的索引；否则返回 (-(插入点) - 1)。插入点 被定义为将键插入数组的那一点：
     *         即第一个大于此键的元素索引，如果数组中的所有元素都小于指定的键，则为
     *         a.length。注意，这保证了当且仅当此键被找到时，返回的值将 >= 0。
     */
    private static int binSearch(String[] array, int startIndex, int endIndex, String value)
    {
        int lo = startIndex;
        int hi = endIndex - 1;
        
        int k = 0;
        while (lo <= hi)
        {
            k = k + 1;
            int mid = (lo + hi) >>> 1;
            int midValCmp = 0;
            if (array[mid].lastIndexOf("_") != -1)
            {
                midValCmp = ((Comparable<String>)array[mid].substring(0, array[mid].lastIndexOf("_"))).compareTo(value);
            }
            
            if (midValCmp < 0)
            {
                lo = mid + 1;
            }
            else if (midValCmp > 0)
            {
                hi = mid - 1;
            }
            else
            {
                return mid; // value found
            }
        }
        return ~lo; // value not present
    }
    
    /**
     * 去缓存查找文件(二分法) 根据缓存文件的命名规则: sb.append(crc64); sb.append("_");
     * sb.append(maxResolution);--图片指定的with sb.append("_");
     * sb.append(size);--图片的尺寸 sb.append(".cache");
     * @return 存储了原图的尺寸大小和路径的数组
     */
    public static String[] findFile(String file)
    {
        mLog.d(TAG, "findFile--start---->" + DateUtil.getCurrentTime());
        mLog.d(TAG, "findFile--targetFileName---->" + file);
        if (file == null)
        {
            return null;
        }
        File cacheDir = new File(URI_CACHE);
        String[] str = null;
        if (cacheDir.exists())
        {
            String[] filePaths = cacheDir.list();
            if (filePaths == null || (filePaths != null && filePaths.length == 0))
            {
                return null;
            }
            String filePath = null;
            mLog.d(TAG, "binSearch--start---->" + DateUtil.getCurrentTime());
            
            Arrays.sort(filePaths);
            int index = binSearch(filePaths, 0, filePaths.length, file);
            
            mLog.d(TAG, "binSearch--end---->" + DateUtil.getCurrentTime());
            if (index >= 0 && index < filePaths.length)
            {
                filePath = filePaths[index];
                mLog.d(TAG, "findFile--haveFindedFileName---->" + file);
                if (filePath != null && filePath.lastIndexOf("x") != -1 && filePath.lastIndexOf("_") != -1
                    && filePath.lastIndexOf(".") != -1)
                {
                    str = new String[3];
                    // 路径
                    str[0] = URI_CACHE + "/" + filePath;
                    
                    // 尺寸
                    String size = filePath.substring(filePath.lastIndexOf("_") + 1, filePath.lastIndexOf("x"));
                    str[1] = size;
                    
                    // 拍摄角度
                    String degree = filePath.substring(filePath.lastIndexOf("x") + 1, filePath.lastIndexOf("."));
                    str[2] = degree;
                }
                
            }
        }
        mLog.d(TAG, "findFile--end----->" + DateUtil.getCurrentTime());
        return str;
    }
    
    /**
     * 计算本地图片的采用率
     * 
     * @author zWX160481
     * @since 2013-4-27
     * @param stream 图片流
     * @param maxResolutionX 图片指定的with
     * @param maxResolutionY 图片指定的height
     * @return Options
     */
    public static Options getOptions(InputStream stream, int maxResolutionX, int maxResolutionY)
    {
        if (stream == null)
        {
            return null;
        }
        
        mLog.d(TAG, "getOptions --> maxResolutionX:" + maxResolutionX);
        mLog.d(TAG, "getOptions --> maxResolutionY:" + maxResolutionY);
        
        BitmapFactory.Options opts = new BitmapFactory.Options();
        
        // 只是解边框
        opts.inJustDecodeBounds = true;
        opts.inSampleSize = 1;
        
        // 不真正解码，只是获取属性options
        Performance performance = new Performance();
        performance.start();
        BitmapFactory.decodeStream(stream, null, opts);
        performance.end();
        
        mLog.d(TAG, "outWidth = " + opts.outWidth + " outHeight:" + opts.outHeight);
        // Modified by zwx160481 2013年4月12日
        mPicSizeX = opts.outWidth == -1 ? 0 : opts.outWidth;
        mPicSizeY = opts.outHeight == -1 ? 0 : opts.outHeight;
        if (opts.outHeight <= maxResolutionY && opts.outWidth <= maxResolutionX)
        {
            opts.inSampleSize = 1;
        }
        else if (opts.outHeight >= 8000 || opts.outWidth >= 8000)
        {
            opts.inSampleSize = 30;
        }
        else
        {
            opts.inSampleSize = 1;
            // --- Modified by zwx160481 2013年4月12日
            int outWith = opts.outWidth;
            int outHeight = opts.outHeight;
            while (outHeight > maxResolutionY || outWith > maxResolutionX)
            {
                outHeight /= 2;
                outWith /= 2;
                opts.inSampleSize = opts.inSampleSize * 2;
            }
        }
        
        mLog.d(TAG, "opts.inSampleSize:" + opts.inSampleSize);
        
        // 设置为解析全部
        opts.inJustDecodeBounds = false;
        opts.inDither = true;
        opts.inJustDecodeBounds = false;
        opts.inPreferredConfig = Bitmap.Config.RGB_565;
        opts.inPurgeable = true;
        opts.inInputShareable = true;
        return opts;
    }
    
    /**
     * 计算本地图片的采用率
     * 
     * @author zWX160481
     * @param file 图片路径
     * @param maxResolutionX 图片指定的with
     * @param maxResolutionY 图片指定的height
     * @return Options
     */
    public static Options getOptions(String file, int maxResolutionX, int maxResolutionY)
    {
        if (file == null)
        {
            return null;
        }
        mLog.d(TAG, "getOptions --> maxResolutionX:" + maxResolutionX);
        mLog.d(TAG, "getOptions --> maxResolutionY:" + maxResolutionY);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        
        // 只是解边框
        opts.inJustDecodeBounds = true;
        opts.inSampleSize = 1;
        
        // 不真正解码，只是获取属性options
        Performance performance = new Performance();
        performance.start();
        BitmapFactory.decodeFile(file, opts);
        performance.end();
        
        mLog.d(TAG, "outWidth = " + opts.outWidth + " outHeight:" + opts.outHeight);
        mPicSizeX = opts.outWidth;
        mPicSizeY = opts.outHeight;
        
        // 都小于指定分辨率时
        if (opts.outHeight <= maxResolutionY && opts.outWidth <= maxResolutionX)
        {
            opts.inSampleSize = 1;
            
            // 都大于指定8000分辨率时
        }
        else if (opts.outHeight >= 8000 || opts.outWidth >= 8000)
        {
            opts.inSampleSize = 30;
            
            // 都大于指定分辨率时
        }
        else
        {
            opts.inSampleSize = 1;
            int outWith = opts.outWidth;
            int outHeight = opts.outHeight;
            while (outHeight > maxResolutionY && outWith > maxResolutionX)
            {
                outHeight /= 2;
                outWith /= 2;
                opts.inSampleSize = opts.inSampleSize * 2;
            }
        }
        
        mLog.d(TAG, "opts.inSampleSize:" + opts.inSampleSize);
        
        // 设置为解析全部
        opts.inJustDecodeBounds = false;
        opts.inDither = true;
        opts.inJustDecodeBounds = false;
        opts.inPreferredConfig = Bitmap.Config.RGB_565;
        opts.inPurgeable = true;
        opts.inInputShareable = true;
        mLog.d(TAG, "mPicSizeX-->" + mPicSizeX);
        mLog.d(TAG, "mPicSizeY-->" + mPicSizeY);
        return opts;
    }
    
    /**
     * 
     * writeToCache
     * 
     * @param crc64
     * @param maxResolution
     * @param in void
     * @exception
     */
    public static String writeToCache(long crc64, int maxResolution, InputStream in)
    {
        
        if (in == null)
        {
            return null;
        }
        
        if (isCached(crc64, maxResolution))
        {// TODO update the modify time
            return null;
        }
        
        // String file = createFilePathFromCrc64(crc64, maxResolution);
        // FIXME
        String size1 = mPicSizeX + "x" + mPicSizeY;
        String file = createFilePathFromCrc64WithSize(crc64, maxResolution, size1, 0);
        mLog.d(TAG, "writeToCache--->file:" + file);
        FileOutputStream fos = null;
        BufferedInputStream bis = null;
        
        // 8k
        int BUFFER_SIZE = 8192;
        byte[] buf = new byte[BUFFER_SIZE];
        int size = 0;
        
        try
        {
            // 获取网络输入流
            bis = new BufferedInputStream(in);
            // 建立文件
            fos = new FileOutputStream(file);
            
            // 保存文件
            while ((size = bis.read(buf)) != -1)
            {
                fos.write(buf, 0, size);
            }
            
            fos.flush();
            mLog.d(TAG, "writeToCache--->close");
        }
        catch (IOException e)
        {
            mLog.d(TAG, "writeToCache--->IOException:" + e.getLocalizedMessage());
        }
        finally
        {
            if (bis != null)
            {
                try
                {
                    bis.close();
                }
                catch (IOException e)
                {
                }
            }
            if (fos != null)
            {
                try
                {
                    fos.close();
                }
                catch (IOException e)
                {
                }
            }
        }
        return file;
    }
    
    /**
     * write bitmap to cache writeToCache
     * 
     * @param crc64
     * @param bitmap
     * @param maxResolution void
     * @exception
     */
    public static void writeToCache(long crc64, Bitmap bitmap, int maxResolution)
    {
        String file = createFilePathFromCrc64(crc64, maxResolution);
        if (bitmap != null && file != null && crc64 != 0)
        {
            FileOutputStream fos = null;
            BufferedOutputStream bos = null;
            try
            {
                File fileC = new File(file);
                fileC.createNewFile();
                fos = new FileOutputStream(fileC);
                bos = new BufferedOutputStream(fos, 16384);
                bitmap.compress(Bitmap.CompressFormat.WEBP, 80, bos);
                bos.flush();
            }
            catch (Exception e)
            {
            }
            finally
            {
                if (fos != null)
                {
                    try
                    {
                        fos.close();
                    }
                    catch (IOException e)
                    {
                    }
                }
                if (bos != null)
                {
                    try
                    {
                        bos.close();
                    }
                    catch (IOException e)
                    {
                    }
                }
            }
        }
    }
    
    public static void invalidateCache(long crc64, int maxResolution)
    {
        mLog.w(TAG, "invalidateCache 1");
        String file = createFilePathFromCrc64(crc64, maxResolution);
        CacheFileThread cachethread = new CacheFileThread();
        
        cachethread.setParams(file, crc64);
        
        cachethread.start();
    }
    
    @Override
    public void finalize()
    {
//        if (mConnectionManager != null)
//        {
//            mConnectionManager.shutdown();
//        }
    }
    
    public static SoftReference<Bitmap> CreateBitMap(final String url, final int devType, /* OUT */
        Options opt, final int w, final int h)
    {
        long crc64 = Utils.Crc64Long(url);
        Bitmap bitmap = null;
        mLog.d(TAG, "DownloadImageToCache--->start");
        // 下载大图到缓存
        String filePath = DownloadImageToCache(url, devType, w, h);
        if (null == filePath)
        {
            return null;
        }
        // 下载到缓存
        // DownloadCloudImageToCache(url, devType, w, h);
        
        mLog.d(TAG, "DownloadImageToCache--->end");
        Performance decodePf = new Performance("DECODE PF");
        decodePf.start();
        mLog.d(TAG, "CreateBitMap Start to Retrieve bitmap from the cache");
        bitmap = createFromCache(crc64, w, h);
        
        String size = ImageUtils.getPicSize(filePath, devType);
        
        // 重命名图片名字
        if (filePath != null)
        {
            File file = new File(filePath);
            if (file.exists())
            {
                String newFileName = createFilePathFromCrc64WithSize(crc64, w, size, 0);
                File newPath = new File(newFileName);
                file.renameTo(newPath);
            }
        }
        
        // 将采样后的图片保存到缓存
        
        decodePf.end();
        
        if (bitmap == null)
        {
            mLog.d(TAG, "CreateBitMap - bitmap == null");
            
            return null;
        }
        
        mLog.d(TAG, "CreateBitMap return softReference bmp");
        SoftReference<Bitmap> srBmp = new SoftReference<Bitmap>(bitmap);
        bitmap = null;
        
        return srBmp;
        
    }
    
    public static String DownloadImageToCache(String url, int devType, int w, int h)
    {
        if (StringUtils.isEmpty(url))
        {
            return null;
        }
        String filePath = null;
        mLog.d(TAG, "CreateBitMap --> url:" + url);
        mLog.d(TAG, "CreateBitMap --> devType:" + devType);
        mLog.d(TAG, "CreateBitMap --> w:" + w);
        mLog.d(TAG, "CreateBitMap --> h:" + h);
        
        long crc64 = Utils.Crc64Long(url);
        
        URL myFileUrl = null;
        
        mLog.d(TAG, "DownloadImageToCache--->start time--->" + DateUtil.getCurrentTime());
        if (dlnaImageSwitcherViewFactory != null)
        {
            dlnaImageSwitcherViewFactory.displayProgreeBar();
        }
        try
        {
            InputStream is = null;
            HttpURLConnection conn = null;
            if (devType == ConstData.DeviceType.DEVICE_TYPE_DMS)
            {
            	if(PlatformUtils.getSDKVersion() >= 23){
                    mLog.d(TAG, "CreateBitMap 3");
                    try
                    {
                        myFileUrl = new URL(url);
                        
                    }
                    catch (MalformedURLException e)
                    {
                    }
                    
                    if (myFileUrl == null)
                    {
                        mLog.e(TAG, "CreateBitMap - HttpURLConnection (myFileUrl == null)");
                        return null;
                    }
                    
                    conn = (HttpURLConnection)myFileUrl.openConnection();
                    
                    if (conn == null)
                    {
                        mLog.e(TAG, "CreateBitMap - HttpURLConnection conn == null");
                        return null;
                    }
                    
                    conn.setConnectTimeout(2000); // 连接超时2s，避免断网死等
                    conn.setReadTimeout(5000); // 读数据超时5s，避免断网死等
                    conn.setRequestMethod("GET");
                    
                    try
                    {
                        conn.connect();
                    }
                    catch (Exception e)
                    {
                        conn.disconnect();
                        conn = null;
                        //异常信息网络连接超时
                        CurlDownload.mCurlCode = 28;
                        return null;
                    }
                    
                    is = conn.getInputStream();
            	}else{
					HttpClient client = new DefaultHttpClient();
					client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 2000); 
					client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 5000);
					HttpGet httpGet = new HttpGet(url);
					HttpResponse response = client.execute(httpGet);
					if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
						is = response.getEntity().getContent();
				
            	}

                mLog.d(TAG, "CreateBitMap 9");
                
            }
            else if (devType == ConstData.DeviceType.DEVICE_TYPE_U)
            {// 此处给的格式可能是URI的格式
            
                // mLog.d("CreateBitMap 10");
                
                File file = new File(url);
                
                if (!file.exists())
                {
                    mLog.d(TAG, "File is not exists");
                    return null;
                }
                
                FileInputStream fis = new FileInputStream(url);
                
                is = fis;
                
                // mLog.d("CreateBitMap 11");
            }
            else if (devType == ConstData.DeviceType.DEVICE_TYPE_CLOUD)
            {
                // 非网络uri,即本地uri
                if (!StringUtils.isNetworkURI(url))
                {
                    File file = new File(url);
                    
                    if (!file.exists())
                    {
                        mLog.d(TAG, "File is not exists:" + url);
                        return null;
                    }
                    
                    FileInputStream fis = new FileInputStream(url);
                    
                    is = fis;
                }
                // 网络uri
                else
                {
                    try
                    {
                        myFileUrl = new URL(url);
                        
                    }
                    catch (MalformedURLException e)
                    {
                    }
                    
                    if (myFileUrl == null)
                    {
                        mLog.e(TAG, "CreateBitMap - HttpURLConnection (myFileUrl == null)");
                        return null;
                    }
                    
                    conn = (HttpURLConnection)myFileUrl.openConnection();
                    
                    if (conn == null)
                    {
                        mLog.e(TAG, "CreateBitMap - HttpURLConnection conn == null");
                        return null;
                    }
                    
                    conn.setConnectTimeout(5000); // 连接超时2s，避免断网死等
                    conn.setReadTimeout(10000); // 读数据超时5s，避免断网死等
                    conn.setRequestMethod("GET");
                    
                    try
                    {
                        conn.connect();
                    }
                    catch (Exception e)
                    {
                        conn.disconnect();
                        conn = null;
                        //异常信息--网络连接超时
                        CurlDownload.mCurlCode = 28;
                        return null;
                    }
                    
                    is = conn.getInputStream();
                }
            }
            else
            {
                mLog.e(TAG, "CreateBitMap - error deviceType:" + devType);
                
                return null;
            }
            
            if (is == null)
            {
                mLog.d(TAG, "CreateBitMap - is == null");
                
                if (conn != null)
                {
                    conn.disconnect();
                    
                    conn = null;
                }
                
                return null;
            }
            
            // 增加性能计数
            Performance writetocachePf = new Performance("WRITE BMP STREAM INTO CACHE PF");
            // 保存图片到临时空间
            writetocachePf.start();
            
            mLog.d(TAG, "writeToCache--->start:" + DateUtil.getCurrentTime());
            // 写入缓冲
            filePath = writeToCache(crc64, w, is);
            mLog.d(TAG, "writeToCache--->end:" + DateUtil.getCurrentTime());
            writetocachePf.end();
            
            // clear input stream handler & disconnect input stream
            is.close();
            is = null;
            if (conn != null)
            {
                conn.disconnect();
                
                conn = null;
            }
        }
        catch (Exception e)
        {
        }
        mLog.d(TAG, "DownloadImageToCache--->end time--->" + DateUtil.getCurrentTime());
        return filePath;
    }
    
    /**
     * 根据大小获得Options实例 <功能详细描述>
     * 
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
        // 只是解边框
        opts.inJustDecodeBounds = true;
        opts.outWidth = maxW;
        opts.outHeight = maxH;
        
        // 不真正解码，只是获取属性options
        Performance performance = new Performance();
        performance.start();
        BitmapFactory.decodeByteArray(bt, 0, (int)size, opts);
        performance.end();
        
        int outWidth = opts.outWidth;
        int outHeight = opts.outHeight;
        
        mLog.d(TAG, "outWidth = " + outWidth + " outHeight:" + outHeight);
        
        if (opts.outWidth >= 3000)
        {
            opts.outWidth = maxW;
            mLog.d(TAG, "Justice: outW = " + opts.outWidth);
        }
        
        if (opts.outHeight >= 3000)
        {
            opts.outHeight = maxH;
            mLog.d(TAG, "Justice: outH = " + opts.outHeight);
        }
        
        int maxNumOfPixels = maxW * maxH;
        int minSideLength = Math.min(maxW, maxH) / 2;
        opts.inSampleSize = Utils.computeSampleSize(opts, minSideLength, maxNumOfPixels);
        
        // 设置为解析全部
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
                
                // 会发生内存溢出危险
                b2 = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), m, true);
            }
            catch (OutOfMemoryError e)
            {
                if (b2 != null && !b2.isRecycled())
                {
                    b2.recycle();
                    System.gc();
                }
                try
                {
                    b2 = Bitmap.createBitmap(b, 0, 0, b.getWidth() - 100, b.getHeight() - 100, m, true);
                }
                catch (OutOfMemoryError error)
                {
                }
                
            }
        }
        return b2;
    }
    
    //
    // protected static class TimeOutThread extends Thread
    // {
    //
    // private Options options = null;
    //
    // private boolean bRun = false;
    //
    // private static TimeOutThread mTimeOutThread = null;
    //
    // public static TimeOutThread getInstance()
    // {
    //
    // if (mTimeOutThread == null)
    // {
    // mTimeOutThread = new TimeOutThread("BMP DEC");
    // }
    //
    // return mTimeOutThread;
    // }
    //
    // /**
    // *constructor TimeOutThread.
    // *
    // * @param string
    // */
    // public TimeOutThread(String string)
    // {
    // super(string);
    // }
    //
    // public void setOpt(Options opt)
    // {
    //
    // bRun = false;
    //
    // options = opt;
    // }
    //
    // public void run()
    // {
    // try
    // {
    // int i = 600;
    // mLog.d(TAG, "TimeOutThread  run 1");
    //
    // while (i-- > 0 && bRun)
    // {
    // Thread.sleep(50);
    // }
    //
    // if (bRun)
    // {
    // options.requestCancelDecode();
    // }
    //
    // if (!bRun)
    // {
    // bRun = true;
    // }
    //
    // }
    // catch (InterruptedException e)
    // {
    // }
    // }
    //
    // }
    
    protected static class CacheFileThread extends Thread
    {
        private String mFilename;
        
        private long mCrc64;
        
        public void setParams(String filename, long crc64)
        {
            mFilename = filename;
            mCrc64 = crc64;
        }
        
        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Thread#run()
         */
        @Override
        public void run()
        {
            String dir = mFilename.substring(0, mFilename.lastIndexOf('/'));
            mLog.d(TAG, "CacheFileThread--->dir===" + dir);
            mLog.d(TAG, "CacheFileThread---id---->" + Thread.currentThread().getId());
            File dirFile = new File(dir);
            if (dirFile.isDirectory())
            {
                File[] fileArray = dirFile.listFiles();
                long dirSize = 0;
                try
                {
                    dirSize = FileUtil.getFileSize(dirFile);
                }
                catch (Exception e)
                {
                }
                // modified by zwx160481 当文件夹大于100M时则删除--start
                mLog.d(TAG, "CacheFileThread--->dirSize===" + dirSize);
                mLog.d(TAG, "CacheFileThread--->fixSize===" + 50 * 1024 * 1024);
                
                if (dirSize > 50 * 1024 * 1024 && fileArray != null && fileArray.length > 0)
                {
                    mLog.d(TAG, "CacheFileThread--->dirSize-->file.delete()");
                    for (File file : fileArray)
                    {
                        file.delete();
                    }
                }
            }
            
            super.run();
        }
    }
    
    public static Bitmap parseRotate(String uri, Bitmap map)
    {
        
        String strDisplayName = StringUtils.getFileName(uri);
        
        mLog.d(TAG, "--------------->parseRotate  strDisplayName ==" + strDisplayName + "-->uri ==" + uri);
        
        if (strDisplayName != null)
        {
            // 图片解码
            BitmapFactory.Options options = null;
            // 获取采样率
            options = UriTexture.getOptions(strDisplayName, 1280, 720);
            
            Bitmap bmap = decodeFile(strDisplayName);
            
            mLog.d(TAG, "--------------->parseRotate map ==" + bmap);
            
            if (bmap != null)
            {
                map = bmap;
            }
            // 检查图片是横的还是竖的，自动旋转
            int degree = getOrientation(uri);
            mLog.d(TAG, "--------------->parseRotate degree ==" + degree);
            map = makeBitmap(map, degree);
        }
        return map;
    }
    
    /**
     * 图片旋转处理
     * 
     * @author zWX160481
     * @since 2013-4-14
     * @param uri 图片路径
     * @param v 要旋转的ImagView
     * @param w 图片with
     * @param h 图片height
     */
    public static void parseRotate(int degree, View curView, float w, float h)
    {
        mLog.d(TAG, "parseRotate degree---------------------->" + degree);
        if (curView != null)
        {
            if (degree != 0)
            {
                mLog.d(TAG, "parseRotate curView--------------------->" + curView);
                curView.setRotation(degree);
                parseScale(curView, degree, w, h);
            }
            else
            {
                mLog.d(TAG, "parseRotate curView--------------------->" + curView);
                curView.setRotation(0);
                curView.setScaleX(1);
                curView.setScaleY(1);
            }
            
        }
    }
    
    /**
     * 图片拉伸处理
     * 
     * @author zWX160481
     * @since 2013-4-14
     * @param curView 旋转对象
     * @param mDegree 旋转角度
     * @param w 图片with
     * @param h 图片height
     */
    public static void parseScale(View curView, int mDegree, float w, float h)
    {
        mLog.d(TAG, "parseScale--->mDegree--->" + mDegree);
        mLog.d(TAG, "parseScale--->w--->" + w);
        mLog.d(TAG, "parseScale--->h--->" + h);
        if (curView != null)
        {
            int ds = mDegree / 90;
            if ((ds & 1) != 0)
            {// 奇数 90*1
                float scale = (float)h / (float)w;
                mLog.d(TAG, "parseScale--->scale--->" + scale);
                curView.setScaleX(scale);
                curView.setScaleY(scale);
            }
            else
            {// 偶数 90*2
                mLog.d(TAG, "parseScale--->scale--->" + 1);
                curView.setScaleX(1);
                curView.setScaleY(1);
            }
        }
    }
    
    /**
     * @author zWX160481
     * @since 2013-4-14
     * @param 相片全路径
     * @param fixW 相片指定的with 默认为1920
     * @return 相片拍摄时的角度
     */
    public static int getOrientation(String uri, int fixW)
    {
        int degree = 0;
        mLog.d(TAG, "getOrientation--->uri------>" + uri);
        mLog.d(TAG, "getOrientation--->start------>" + DateUtil.getCurrentTime());
        if (StringUtils.isNetworkURI(uri))
        {
            
            // 缓存路径
            File cacheDir = new File(URI_CACHE);
            File[] files = cacheDir.listFiles();
            
            // 如果是网络图片,且缓存的图片不超过10张时先去缓存加载
            if (files != null)
            {
                long crc64 = Utils.Crc64Long(uri);
                String str = createFilePathFromCrc64(crc64, BITMAP_MAX_W);
                String fileName = createFileName(crc64, BITMAP_MAX_W);
                String[] strArr = findFile(fileName);
                
                // 如果已经缓存有,则从路径中获取拍摄角度
                if (strArr != null && strArr.length >= 3 && strArr[2] != null)
                {
                    degree = Integer.parseInt(strArr[2]);
                    mLog.d(TAG, "getOrientation--->from cache------>" + degree);
                }
                else
                {
                    
                    // 否则解析图片获取拍摄角度
                    degree = getDegreeFromNet(uri);
                }
            }
            else
            {
                
                // 否则解析图片获取拍摄角度
                degree = getDegreeFromNet(uri);
            }
        }
        else
        {
            
            // 获取本地图片的拍摄角度
            degree = getOrientation(uri);
            
            mLog.d(TAG, "getOrientation--->from Local------>" + degree);
        }
        mLog.d(TAG, "getOrientation--->end------>" + DateUtil.getCurrentTime());
        return degree;
    }
    
    /**
     * 获取网络图片的拍摄角度
     * @author zWX160481
     * @param uri 图片路径
     * @return 拍摄角度
     */
    private static int getDegreeFromNet(String uri)
    {
        mLog.d(TAG, "getDegreeFromNet--->start------>" + DateUtil.getCurrentTime());
        int degree = 0;
        InputStream inputStream = null;
        HttpURLConnection conn = null;
        URL mUrl = null;
        try
        {
            mUrl = new URL(uri);
            conn = (HttpURLConnection)mUrl.openConnection();
            conn.setConnectTimeout(5 * 1000);
            conn.setReadTimeout(5000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            if (conn.getResponseCode() == 200)
            {
                inputStream = conn.getInputStream();
                
                degree = getOrJpeg(inputStream);
                mLog.d(TAG, "getOrientation--->from Net------>" + degree);
            }
        }
        catch (Exception e)
        {
        }
        finally
        {
            
            // 关闭资源
            closeConn(conn, inputStream);
        }
        mLog.d(TAG, "getDegreeFromNet--->end------>" + DateUtil.getCurrentTime());
        return degree;
    }
    
    /**
     * @author zWX160481
     * @since 2013-4-14
     * @param 相片全路径
     * @param fixW 相片指定的with 默认为1920
     * @return 相片拍摄时的角度
     */
    public static int getOrientation()
    {
        String tempUri = createFilePath(BIGFILENAME);
        File file = new File(tempUri);
        if (file.exists())
        {
            return getOrientation(tempUri);
        }
        return 0;
    }
    
    /**
     * 
     * @param 相片全路径
     * @return 相片拍摄时的角度
     */
    public static int getOrientation(String filepath)
    {
        mLog.d(TAG, "--------------->getOrientation  filepath ==" + filepath);
        int degree = 0;
        ExifInterface exif = null;
        try
        {
            exif = new ExifInterface(filepath);
            mLog.d(TAG, "--------------->getOrientation  exif == " + exif);
        }
        catch (IOException e)
        {
        }
        if (exif != null)
        {
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            mLog.d(TAG, "--------------->getOrientation  orientation == " + orientation);
            if (orientation != -1)
            {
                switch (orientation)
                {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        break;
                }
            }
        }
        mLog.d(TAG, "--------------->getOrientation  degree==" + degree);
        return degree;
    }
    
    private static Bitmap makeBitmap(Bitmap srcBitmap, int angle)
    {
        Bitmap destBitmap = srcBitmap;
        mLog.d(TAG, "--------------->makeBitmap  isinvoke" + angle);
        if (angle != 0)
        {
            mLog.d(TAG, "----" + "----------->makeBitmap  rotateing ");
            // 需要旋转
            destBitmap = UriTexture.rotate(destBitmap, angle);
            if (destBitmap != null)
            {
                // if (srcBitmap != destBitmap && !srcBitmap.isRecycled()) {
                // srcBitmap.recycle();
                // }
                srcBitmap = destBitmap;
            }
            else
            {
                mLog.d(TAG, "--------------->rotate bitmap failed.");
            }
        }
        return srcBitmap;
    }
    
    private static Bitmap decodeFile(String filename)
    {
        Bitmap bitmap = null;
        try
        {
            if (filename.startsWith("file://"))
            {
                filename = filename.substring("file://".length());
            }
            // 图片解码
            BitmapFactory.Options options = null;
            // 获取采样率
            options = UriTexture.getOptions(filename, 1280, 720);
            bitmap = BitmapFactory.decodeFile(filename, options);
            
            if (bitmap != null)
            {
                mLog.d(TAG, "--------->decode bitmap ok: " + filename);
                // setShowMsg(false);
                // setState(STATE_LOAD_COMPLETED);
            }
            else
            {
                mLog.d(TAG, "--------->decode bitmap failed: " + filename);
                // setShowMsg(true);
                // setState(STAFAILED);
            }
            
        }
        catch (OutOfMemoryError ex)
        {
            mLog.d(TAG, "--------->decode bitmap failed: " + filename);
            mLog.d(TAG, "--------->error message: " + ex.getLocalizedMessage());
            bitmap = null;
        }
        catch (Exception e)
        {
            mLog.d(TAG, "--------->decode bitmap failed: " + filename);
            mLog.d(TAG, "--------->error message: " + e.getLocalizedMessage());
            bitmap = null;
            
        }
        return bitmap;
    }
}
