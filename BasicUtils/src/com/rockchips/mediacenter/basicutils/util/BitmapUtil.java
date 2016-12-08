package com.rockchips.mediacenter.basicutils.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;

/**
 * 
 * Description: 提供一些Bitmap相关的基本处理函数<br>
 * @author r00178559
 * @version v1.0 Date: 2013-9-16 下午4:04:48<br>
 */
public final class BitmapUtil
{
    private static final String TAG = "BitmapUtil";

    private static final IICLOG Log = IICLOG.getInstance();

    private final static int BITMAP_TEMP_STORAGE_SIZE = 16 * 1024;

    private final static int BITMAP_MAX_MEMORY = 16000000;

    private BitmapUtil()
    {

    }

    public static BitmapFactory.Options getBitmapOptions(String filePath)
    {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, opts);

        return opts;
    }

    public static Bitmap getBitmapByPath(String filePath, int width, int height)
    {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, opts);
        Bitmap thumbnail = null;
		
		/* BEGIN: Added by s00211113 for DTS2014041004040  2014/04/10 */
        try
        {
            opts.inJustDecodeBounds = false;
            opts.inSampleSize = computeSampleSize(opts, (width < height) ? width : height);
            int maxSize = computeMaxSize(opts);
            int maxSide = (width < height) ? height : width;
            while (maxSize >= BITMAP_MAX_MEMORY || (maxSide / opts.inSampleSize > 4096))
            {
                opts.inSampleSize += 1;
                maxSize = computeMaxSize(opts);
            }

            thumbnail = BitmapFactory.decodeFile(filePath, opts);
        }
        catch (OutOfMemoryError e2)
        {
            Log.e(TAG, "getBitmapByPath out of memory");
            return null;
        }

        Log.d(BitmapUtil.class.getSimpleName(), "getBitmapByPath: filePath = " + filePath + ",thumbnail = "
                + ((thumbnail == null) ? "null" : "not null"));
        /* END: Added by s00211113 for DTS2014041004040 2014/04/10 */
        return thumbnail;
    }

    public static int computeSampleSize(BitmapFactory.Options options, int target)
    {
        int w = options.outWidth;
        int h = options.outHeight;

        int candidateW = w / target;
        int candidateH = h / target;

        int candidate = Math.max(candidateW, candidateH);

        if (candidate == 0)
        {
            return 1;
        }

        return candidate;
    }

    public static int computeMaxSize(BitmapFactory.Options options)
    {
        int maxSize = options.outWidth * options.outHeight;

        if (options.inPreferredConfig == Bitmap.Config.ARGB_8888)
        {
            return maxSize * 4 / (options.inSampleSize * options.inSampleSize);
        }
        if (options.inPreferredConfig == Bitmap.Config.RGB_565)
        {
            return maxSize * 3 / (options.inSampleSize * options.inSampleSize);
        }
        if (options.inPreferredConfig == Bitmap.Config.ARGB_4444)
        {
            return maxSize * 2 / (options.inSampleSize * options.inSampleSize);
        }
        if (options.inPreferredConfig == Bitmap.Config.ALPHA_8)
        {
            return maxSize * 1 / (options.inSampleSize * options.inSampleSize);
        }

        return 0;
    }

    /**
     * 获取本地MP3的封面位图
     * @param musicPath 本地MP3路径
     */
    private static final int DEFAULT_DSTFILE_SIZE = 233;

    public static Bitmap getMp3ID3Bitmap(String musicPath, int dstWidth, int dstHeight)
    {

        // 解析MP3图片
        List<Object> vector = MP3HeadInfoReader.getPictureFromMp3(musicPath);

        if (null != vector && !vector.isEmpty())
        {
            byte bytes[] = (byte[]) vector.get(1);
            Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (bm != null)
            {
                if (dstWidth <= 0 || dstHeight <= 0)
                {
                    dstWidth = DEFAULT_DSTFILE_SIZE;
                    dstHeight = DEFAULT_DSTFILE_SIZE;
                }
                Bitmap newBit = Bitmap.createScaledBitmap(bm, dstWidth, dstHeight, true);

                return newBit;
            }
        }
        return null;
    }

    public static Bitmap createBitmapFromResource(Resources res, int resId, int w, int h)
    {
        Bitmap oldBitmap;
        Bitmap newBitmap;

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inDither = true;

        oldBitmap = BitmapFactory.decodeResource(res, resId, options);

        if (oldBitmap != null)
        {
            // 采用裁剪缩放算法进行优化处理
            int x;
            int y;
            int width = oldBitmap.getWidth();
            int height = oldBitmap.getHeight();

            if (width < w || height < h)
            {
                if (width < w)
                {
                    x = 0;
                }
                else
                {
                    x = (width - w) / 2;
                    width = w;
                }
                if (height < h)
                {
                    y = 0;
                }
                else
                {
                    y = (height - h) / 2;
                    height = h;
                }
                Log.d(TAG, "---------->(" + x + "," + y + "," + width + "," + height + ")");
                newBitmap = Bitmap.createBitmap(oldBitmap, x, y, width, height, null, true);
                // newBitmap = oldBitmap.createScaledBitmap(oldBitmap, w, h,
                // true);
            }
            else
            {
                float wrate = (float) w / (float) oldBitmap.getWidth();
                float hrate = (float) h / (float) oldBitmap.getHeight();

                if (Math.abs(wrate - hrate) < 0.01f)
                {
                    x = 0;
                    y = 0;
                    width = oldBitmap.getWidth();
                    height = oldBitmap.getHeight();
                }
                else if (wrate - hrate > 0.0f)
                {
                    wrate = hrate;
                    width = oldBitmap.getWidth();
                    height = (int) (h / wrate);
                    x = 0;
                    y = (oldBitmap.getHeight() - height) / 2;
                    hrate = wrate;
                }
                else
                {
                    height = oldBitmap.getHeight();
                    width = (int) (w / hrate);

                    x = (oldBitmap.getWidth() - width) / 2;
                    y = 0;
                }
                Matrix m = new Matrix();
                // m.setScale(wrate, hrate);
                m.preScale(wrate, hrate);
                Log.d(TAG, "---------->(" + wrate + "," + hrate + ")(" + x + "," + y + "," + width + "," + height + ")");
                newBitmap = Bitmap.createBitmap(oldBitmap, x, y, width, height, m, true);
            }

            if (newBitmap != oldBitmap && !oldBitmap.isRecycled())
            {
                oldBitmap.recycle();

            }
            oldBitmap = null;

            return newBitmap;
        }
        else
        {
            return null;
        }

    }

    public static int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels)
    {

        int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);

        int roundedSize;
        if (initialSize <= 8)
        {
            roundedSize = 1;
            while (roundedSize < initialSize)
            {
                roundedSize <<= 1;
            }
        }
        else
        {
            roundedSize = (initialSize + 7) / 8 * 8;
        }

        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels)
    {

        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(Math.floor(w / minSideLength), Math.floor(h / minSideLength));

        if (upperBound < lowerBound)
        {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }

        if ((maxNumOfPixels == -1) && (minSideLength == -1))
        {
            return 1;
        }
        else if (minSideLength == -1)
        {
            return lowerBound;
        }
        else
        {
            return upperBound;
        }
    }

    public static Bitmap createBitmapforListIcon(String filepath, int w, int h)
    {
        Bitmap bitmap = null;
        try
        {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inPurgeable = true;
            opts.inInputShareable = true;
            opts.inTempStorage = new byte[BITMAP_TEMP_STORAGE_SIZE];
            opts.inJustDecodeBounds = true; // just get width and height
            BitmapFactory.decodeFile(filepath, opts); // get opts,not real decode

            opts.inSampleSize = computeSampleSize(opts, -1, w * h);
            opts.inJustDecodeBounds = false; // decode real file
            Log.d(TAG, "opts.inSampleSize=" + opts.inSampleSize);
            bitmap = BitmapFactory.decodeFile(filepath, opts);
        }
        catch (OutOfMemoryError e)
        {
            System.gc();
            e.printStackTrace();
            Log.w(TAG, "BitmapFactory.decodeFile OutOfMemoryError!!!");
            bitmap = null;
        }
        return bitmap;
    }

    public static BitmapDrawable createBitmapforResource(Resources res, int resId, int w, int h)
    {
        Bitmap bitmap = null;
        try
        {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inPurgeable = true;
            opts.inInputShareable = true;
            opts.inTempStorage = new byte[BITMAP_TEMP_STORAGE_SIZE];
            opts.inJustDecodeBounds = true; // just get width and height
            BitmapFactory.decodeResource(res, resId, opts); // get opts,not real decode

            opts.inSampleSize = computeSampleSize(opts, -1, w * h);
            opts.inJustDecodeBounds = false; // decode real file
            Log.d(TAG, "opts.inSampleSize=" + opts.inSampleSize);
            bitmap = BitmapFactory.decodeResource(res, resId, opts);
        }
        catch (OutOfMemoryError e)
        {
            System.gc();
            e.printStackTrace();
            Log.w(TAG, "BitmapFactory.decodeFile OutOfMemoryError!!!");
            bitmap = null;
        }
        if (null != bitmap)
        {
            return new BitmapDrawable(bitmap);
        }
        else
        {
            return null;
        }
    }

    public static Bitmap createBitmapFromNetwork(String url, int w, int h)
    {
        Log.d(TAG, "createBitmapFromNetwork------->start time:" + DateUtil.getCurrentTime());
        Log.d(TAG, "createBitmapFromNetwork---url--->:" + url);

        if (StringUtils.isEmpty(url) || !StringUtils.isNetworkURI(url))
        {
            return null;
        }
        InputStream inputStream = null;
        Bitmap bitmap = null;
        HttpURLConnection conn = null;
        BitmapFactory.Options opts = new BitmapFactory.Options();
        URL mUrl = null;
        try
        {
            Log.d(TAG, "createBitmapFromNetwork 1---->:" + DateUtil.getCurrentTime());

            // 获取采样率
            mUrl = new URL(url);
            conn = (HttpURLConnection) mUrl.openConnection();
            conn.setConnectTimeout(10 * 1000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setReadTimeout(10 * 1000);
            opts.inPurgeable = true;
            opts.inInputShareable = true;
            opts.inTempStorage = new byte[BITMAP_TEMP_STORAGE_SIZE];
            opts.inJustDecodeBounds = true; // just get width and height
            if (conn.getResponseCode() == 200)
            {
                inputStream = conn.getInputStream();
            }

            Log.d(TAG, "createBitmapFromNetwork-2---->:" + DateUtil.getCurrentTime());

            if (inputStream != null)
            {
                Bitmap temp = BitmapFactory.decodeStream(inputStream, null, opts);
                inputStream.close();
                
                if (opts.outHeight == -1 || opts.outWidth == -1)
                {
                    Log.w(TAG, "createBitmapFromNetwork-3---->:");
                    conn.disconnect();
                    return null;
                }

                opts.inSampleSize = BitmapUtil.computeSampleSize(opts, w, w * h);

            }

            // 关闭资源
            conn.disconnect();
            Log.d(TAG, "createBitmapFromNetwork-4---->:" + DateUtil.getCurrentTime());

            // 解码图片
            mUrl = new URL(url);
            conn = (HttpURLConnection) mUrl.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            if (conn.getResponseCode() == 200)
            {
                inputStream = conn.getInputStream();
            }
            Log.d(TAG, "createBitmapFromNetwork-5---->:" + DateUtil.getCurrentTime());
            try
            {
                if (inputStream != null)
                {
                    opts.inJustDecodeBounds = false;
                    bitmap = BitmapFactory.decodeStream(inputStream, null, opts);
                    inputStream.close();
                }
                // 关闭资源
                conn.disconnect();

                if (bitmap == null)
                {
                    Log.d(TAG, "createBitmapFromNetwork-->bitmap--->null");
                    return null;
                }
                else
                {
                    return bitmap;
                }

            }
            catch (OutOfMemoryError e)
            {

                System.gc();
            }
            catch (Exception e)
            {
                // 提示网络连接超时
                Log.d(TAG, "createBitmapFromNetwork-->network unknow error");
            }
            finally
            {

                try
                {
                    conn.disconnect();
                    if (inputStream != null)
                        inputStream.close();
                }
                catch (Exception e)
                {

                }

            }
            Log.d(TAG, "openConnection------->444:" + DateUtil.getCurrentTime());
        }
        catch (Exception e)
        {
            // 提示网络连接超时
            Log.d(TAG, "createBitmapFromNetwork-->network unknow error");
        }
        catch (OutOfMemoryError e)
        {
            Log.d(TAG, "createBitmapFromNet-->OutOfMemoryError");
            System.gc();
            Log.w(TAG, e.getLocalizedMessage());
            return null;
        }

        return null;
    }
    
    public static boolean saveBitmap(final Bitmap hBitmap, final String absloutPath)
    {
        boolean ret = true;
        if (hBitmap == null)
        {
            return false;
        }
        if (hBitmap != null && hBitmap.isRecycled())
        {
            return false;
        }
        File f = new File(absloutPath);
        FileOutputStream fOut = null;
        try
        {
            f.getParentFile().mkdirs();
            f.createNewFile();
            fOut = new FileOutputStream(f);
            hBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
        }
        catch (IOException e1)
        {
            ret = false;
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
                    ret = false;
                }
            }
        }

        return ret;
    }

    // 获得圆角图片的方法
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx)
    {
        if (bitmap != null)
        {
            Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
            Canvas canvas = new Canvas(output);

            final int color = 0xff424242;
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            final RectF rectF = new RectF(rect);

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

            paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);

            return output;
        }
        else
        {
            return null;
        }
    }

    /**
     * <一句话功能简述>根据宽高比最大限度的截取缩略图。 <功能详细描述>
     * @param bitmap
     * @param w
     * @param h
     * @return 如果需要截取，返回另一张新的按照截取规则截取的缩略图，原图被销毁。 如果不需要截取，返回原图。如果原图为null或者内存不够,则返回null。
     * @see [类、类#方法、类#成员]
     */
    public static Bitmap cutBitmap(Bitmap bitmap, int w, int h)
    {
        if (bitmap == null)
        {
            return null;
        }
        if (w <= 0 || h <= 0)
        {
            return bitmap;
        }

        // 截取缩略
        int bw = bitmap.getWidth();
        int bh = bitmap.getHeight();
        float bs = ((float) bw) / bh;
        float ts = ((float) w) / h;
        Log.d(TAG, "bs:" + bs + ",ts:" + ts);
        Bitmap targBitmap = null;

        try
        {

            // 如果采样后的图片 大于希望的宽高*2，证明上一步的采样时失败的，
            // 采样失败的大图，值截取正中间部分
            if (bw > w * 2 && bh > h * 2)
            {
                targBitmap = Bitmap.createBitmap(bitmap, (int) (bw - w) / 2, (int) (bh - h) / 2, w, h);
                bitmap.recycle();
                return targBitmap;
            }

            // 如果图片的宽高比大于目标宽高比，即宽超出目标宽高比，宽度需要截取
            if (bs > ts)
            {
                targBitmap = Bitmap.createBitmap(bitmap, (int) (bw - bh * ts) / 2, 0, (int) (bh * ts), bh);
                bitmap.recycle();
            }
            else if (bs < ts)
            {
                targBitmap = Bitmap.createBitmap(bitmap, 0, (int) (bh - bw / ts) / 2, bw, (int) (bw / ts));
                bitmap.recycle();
            }
            else
            {
                // 克隆一张图片
                targBitmap = bitmap;
            }
        }
        catch (OutOfMemoryError e)
        {
            return null;
        }
        catch (Exception e)
        {
            return null;
        }
        return targBitmap;
    }

}
