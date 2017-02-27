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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;

import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.utils.IICLOG;
import com.rockchips.mediacenter.utils.NetUtils;
import com.rockchips.mediacenter.utils.StringUtils;
import com.rockchips.mediacenter.utils.GifOpenHelper;

public class ImageUtils
{
    private static final int UNCONSTRAINED = -1;

    private static final String TAG = "Utils";
    
    private static IICLOG mLog = IICLOG.getInstance();

    public static final void writeUTF(DataOutputStream dos, String string) throws IOException
    {
        if (string == null)
        {
            dos.writeUTF(new String());
        }
        else
        {
            dos.writeUTF(string);
        }
    }

    public static final String readUTF(DataInputStream dis) throws IOException
    {
        String retVal = dis.readUTF();
        if (retVal.length() == 0)
            return null;
        return retVal;
    }

    public static final Bitmap resizeBitmap(Bitmap bitmap, int maxSize)
    {
        int srcWidth = bitmap.getWidth();
        int srcHeight = bitmap.getHeight();
        int width = maxSize;
        int height = maxSize;
        boolean needsResize = false;
        if (srcWidth > srcHeight)
        {
            if (srcWidth > maxSize)
            {
                needsResize = true;
                height = ((maxSize * srcHeight) / srcWidth);
            }
        }
        else
        {
            if (srcHeight > maxSize)
            {
                needsResize = true;
                width = ((maxSize * srcWidth) / srcHeight);
            }
        }
        if (needsResize)
        {
            Bitmap retVal = Bitmap.createScaledBitmap(bitmap, width, height, true);
            return retVal;
        }
        else
        {
            return bitmap;
        }
    }

    private static final long POLY64REV = 0x95AC9329AC4BC9B5L;

    private static final long INITIALCRC = 0xFFFFFFFFFFFFFFFFL;

    private static boolean init = false;

    private static long[] CRCTable = new long[256];

    /**
     * A function thats returns a 64-bit crc for string
     * 
     * @param in : input string
     * @return 64-bit crc value
     */
    public static final long Crc64Long(String in)
    {
        if (in == null || in.length() == 0)
        {
            return 0;
        }
        long crc = INITIALCRC, part;
        if (!init)
        {
            for (int i = 0; i < 256; i++)
            {
                part = i;
                for (int j = 0; j < 8; j++)
                {
                    int value = ((int) part & 1);
                    if (value != 0)
                        part = (part >> 1) ^ POLY64REV;
                    else
                        part >>= 1;
                }
                CRCTable[i] = part;
            }
            init = true;
        }
        int length = in.length();
        for (int k = 0; k < length; ++k)
        {
            char c = in.charAt(k);
            crc = CRCTable[(((int) crc) ^ c) & 0xff] ^ (crc >> 8);
        }
        return crc;
    }

    /**
     * A function that returns a human readable hex string of a Crx64
     * 
     * @param in : input string
     * @return hex string of the 64-bit CRC value
     */
    public static final String Crc64(String in)
    {
        if (in == null)
            return null;
        long crc = Crc64Long(in);
        /*
         * The output is done in two parts to avoid problems with architecture-dependent word order
         */
        int low = ((int) crc) & 0xffffffff;
        int high = ((int) (crc >> 32)) & 0xffffffff;
        String outVal = Integer.toHexString(high) + Integer.toHexString(low);
        return outVal;
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

    public static int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels)
    {
        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = (maxNumOfPixels == UNCONSTRAINED) ? 1 : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == UNCONSTRAINED) ? 128 : (int) Math.min(Math.floor(w / minSideLength), Math.floor(h / minSideLength));

        if (upperBound < lowerBound)
        {
            return lowerBound;
        }

        if ((maxNumOfPixels == UNCONSTRAINED) && (minSideLength == UNCONSTRAINED))
        {
            return 1;
        }
        else if (minSideLength == UNCONSTRAINED)
        {
            return lowerBound;
        }
        else
        {
            return upperBound;
        }
    }

    public static boolean isGifFile(Context context, String filePath)
    {

        if (filePath == null)
        {
            return false;
        }
        GifOpenHelper gHelper = new GifOpenHelper();
        InputStream is = null;
        try
        {
            is = context.getContentResolver().openInputStream(Uri.parse(filePath));
        }
        catch (FileNotFoundException e)
        {
        }
        boolean isGif = false;
        if (is != null && gHelper.STATUS_FORMAT_ERROR != gHelper.read(is))
        {
            isGif = true;
        }
        return isGif;
    }

    /**
     * @author zWX160481 获取缩略图尺寸
     * @param uri
     * @param mDeviceType
     * @return
     */
    public static String getPicSize(String uri, int mDeviceType)
    {
        mLog.d(TAG, "getPicSize start --->" + "uri:" + uri + "|" + "mDeviceType:" + mDeviceType);
        String size = "";
        int[] SizeArr = null;
        if (StringUtils.isEmpty(uri))
        {
            mLog.d(TAG, "uri--->" + uri);
            return "0x0";
        }
        if (StringUtils.isNetworkURI(uri))
        {
            mLog.d(TAG, "DeviceType--->" + mDeviceType);
            // 先去缓存查看是否存在这个文件，有就去缓存获取
            long crc64 = ImageUtils.Crc64Long(uri);
            String crc64uri = UriTexture.createFilePathFromCrc64(crc64, UriTexture.BITMAP_MAX_W);
            mLog.d(TAG, "crc64uri--->" + crc64uri);

            // 查找缓存图片名字
            String fileName = UriTexture.createFileName(crc64, UriTexture.BITMAP_MAX_W);
            String[] strArr = UriTexture.findFile(fileName);
            mLog.d(TAG, "getPicSize--->fileName-->" + fileName);
            if (strArr != null && strArr.length >= 2 && strArr[1] != null && !"-1x-1".equals(strArr[1])
                    && mDeviceType != ConstData.DeviceType.DEVICE_TYPE_CLOUD)
            {
                // SizeArr = FileUtils.computeWH_1(crc64uri);
                size = strArr[1];
                mLog.d(TAG, "getPicSize--->from cache--size-->" + size);
            }
            if (size != null && "0x0".equals(size) || "".equals(size))
            {
                InputStream stream = NetUtils.getInputStreamFromNet(uri);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 1;
                options.inJustDecodeBounds = true;
                int with = 0;
                int height = 0;
                if (stream != null)
                {
                    Bitmap bitmap = BitmapFactory.decodeStream(stream, null, options);
                    mLog.d(TAG, "bitmap:" + bitmap);
                }
                if (options.mCancel || options.outWidth == -1 || options.outHeight == -1)
                {
                    mLog.d(TAG, "getImageSize fail:" + options);
                    size = "0x0";
                }
                else
                {
                    with = options.outWidth;
                    height = options.outHeight;
                    size = with + "x" + height;
                }
                if (stream != null)
                {
                    try
                    {
                        stream.close();
                    }
                    catch (IOException e)
                    {
                    }
                }
                // 如果是云相册图片
                if ("0x0".equals(size) && mDeviceType == ConstData.DeviceType.DEVICE_TYPE_CLOUD && strArr != null)
                {
                    SizeArr = NetUtils.computeWH_1(strArr[0]);
                    size = String.valueOf(SizeArr[0]) + "x" + String.valueOf(SizeArr[1]);
                }
                mLog.d(TAG, "getPicSize--->from net-->size-->" + size);
            }
        }
        else
        {
            mLog.d(TAG, "DeviceType--->" + mDeviceType);
            SizeArr = NetUtils.computeWH_1(uri);
            size = String.valueOf(SizeArr[0]) + "x" + String.valueOf(SizeArr[1]);
            mLog.d(TAG, "getPicSize--->from Local-->size-->" + size);
        }
        if ("".equals(size))
        {
            size = "0x0";
        }

        return size;
    }

    /**
     * 获取内存使用
     * @return m
     **/
    public static String used()
    {
        long total = Runtime.getRuntime().totalMemory();
        long free = Runtime.getRuntime().freeMemory();
        long used = (total - free) / (1024 * 1024);

        return used + "m";
    }

    /**
     * 获取字符串所在矩形，得到宽高
     * @param text 字符串
     * @param paint 画笔
     * @param textSize 字体大小
     * */
    public static Rect getTextRec(String text, float textSize)
    {
        Rect rect = new Rect();
        Paint paint = new Paint();
        if (paint != null && text != null)
        {
            paint.setTextSize(textSize);
            paint.getTextBounds(text, 0, text.length(), rect);
        }
        return rect;
    }

    /**
     * 判断当前系统用的是什么语言
     * @author zWX160481
     * @param context
     * @param country
     */
    public static boolean getLocalCountry(Context context, String country)
    {
        if (context == null || StringUtils.isEmpty(country))
        {
            return false;
        }
        String localCountry = context.getResources().getConfiguration().locale.getLanguage();
        mLog.d(TAG, "localCountry--->language--->" + localCountry);
        mLog.d(TAG, "country--->language--->" + country);
        if (country.equals(localCountry))
        {
            return true;
        }
        else
        {
            return false;
        }

    }
}
