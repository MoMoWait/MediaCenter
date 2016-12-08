package com.rockchips.mediacenter.viewutils.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * 图片处理
 * @author s00211113
 *
 */
public final class ImageHelper
{
    public static final float REFLECTRATIOOFORIG = 0.25f;
    
    public static final int LINEARGRADIENT_COLOR_0 = 0x70ffffff;
    
    public static final int LINEARGRADIENT_COLOR_1 = 0x00ffffff;
    
    private ImageHelper()
    {
    }
    
    public static float[] parseToFloatArray(String[] strArr)
    {
        if (strArr == null || strArr.length == 0)
        {
            return null;
        }
        
        float[] destArr = new float[strArr.length];
        
        for (int i = 0; i < strArr.length; i++)
        {
            destArr[i] = Float.valueOf(strArr[i]);
        }
        return destArr;
    }
    
    /**
     * 参考 @createReflectedImage
     * <功能详细描述>
     * @param originalImage
     * @param reflectedSize
     * @return
     * @see [类、类#方法、类#成员]
     */
    @SuppressWarnings("deprecation")
    public static Drawable createReflectedImage(Drawable originalImage, float reflectedSize)
    {
        if (originalImage == null)
        {
            return null;
        }
        BitmapDrawable bd = (BitmapDrawable)originalImage;
        Bitmap tarBitmap = createReflectedImage(bd.getBitmap(), reflectedSize);
        return new BitmapDrawable(tarBitmap);
    }
    
    /**
     * 生产原图+倒影 连体图，可以通过reflectedSize控制倒影长度（截取）
     * <功能详细描述>
     * @param originalImage
     * @param reflectedSize 倒影长度，原图片高度的比例
     * @return
     * @see [类、类#方法、类#成员]
     */
    public static Bitmap createReflectedImage(Bitmap originalImage, float reflectedSize)
    {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        Matrix matrix = new Matrix();
        // 实现图片翻转90度
        matrix.preScale(1, -1);
        // 倒影的长度（高）
        int reflectedLength = (int)(height * reflectedSize);
        // 创建倒影图片（是原始图片的一半大小）
        Bitmap reflectionImage =
            Bitmap.createBitmap(originalImage, 0, height - reflectedLength, width, reflectedLength, matrix, false);
        // 创建总图片（原图片 + 倒影图片）
        Bitmap finalReflection = Bitmap.createBitmap(width, (height + reflectedLength), Config.ARGB_8888);
        // 创建画布
        Canvas canvas = new Canvas(finalReflection);
        canvas.drawBitmap(originalImage, 0, 0, null);
        // 把倒影图片画到画布上
        canvas.drawBitmap(reflectionImage, 0, height + 1, null);
        Paint shaderPaint = new Paint();
        // 创建线性渐变LinearGradient对象
        LinearGradient shader =
            new LinearGradient(0, originalImage.getHeight(), 0, finalReflection.getHeight() + 1, LINEARGRADIENT_COLOR_0,
                    LINEARGRADIENT_COLOR_1, TileMode.MIRROR);
        shaderPaint.setShader(shader);
        shaderPaint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
        // 画布画出反转图片大小区域，然后把渐变效果加到其中，就出现了图片的倒影效果。
        canvas.drawRect(0, height + 1, width, finalReflection.getHeight(), shaderPaint);
        
        if (reflectionImage != null)
        {
            reflectionImage.recycle();
        }
        return finalReflection;
    }
    
    /**
     * <一句话功能简述>在图片上添加上文字
     * <功能详细描述>
     * @param originalImage 需要添加文字的图片
     * @param text 要显示的文字
     * @param size 文字显示大小
     * @param color 文字颜色
     * @param pointX 文字中心点  在图片上的x坐标
     * @param pointY 文字中心点 在图片上的y坐标
     * @see [类、类#方法、类#成员]
     */
    public static void addText(Bitmap originalImage, String text, int size, int color, int pointX, int pointY)
    {
        Rect rect = new Rect();
        
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(size);
        paint.setColor(color);
        paint.getTextBounds(text, 0, text.length(), rect);
        
        Canvas canvas = new Canvas(originalImage);
        canvas.drawText(text, pointX - rect.width() / 2, pointY + rect.height() / 2, paint);
    }
    
    /**
     * <一句话功能简述>根据宽高和图片source id 生成图片
     * <功能详细描述>
     * @param context
     * @param id
     * @param width
     * @param height
     * @return
     * @see [类、类#方法、类#成员]
     */
    public static Bitmap createBitmap(Context context, int id, int width, int height)
    {
        Bitmap bitmap = null;
        try
        {
            bitmap = BitmapFactory.decodeResource(context.getResources(), id);
        }
        catch (OutOfMemoryError e)
        {
            System.out.println("OutOfMemoryError = " + e.getLocalizedMessage());
        }
        
        Bitmap zoomBitmap = null;
        if (bitmap != null)
        {
            zoomBitmap = zoomBitmap(bitmap, width, height);
            bitmap.recycle();
            bitmap = null;
        }
        
        return zoomBitmap;
    }
    
    /**
     * <一句话功能简述>根据宽高缩放图片
     * <功能详细描述>
     * @param bitmap
     * @param w
     * @param h
     * @return
     * @see [类、类#方法、类#成员]
     */
    public static Bitmap zoomBitmap(Bitmap bitmap, int w, int h)
    {
        if (bitmap == null)
        {
            return null;
        }
        
        // 源文件的大小
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        // 设置矩阵比例
        Matrix matrix = new Matrix();
        float scaleWidht = ((float)w / width);
        float scaleHeight = ((float)h / height);
        matrix.postScale(scaleWidht, scaleHeight);
        
        // 按矩阵比例把源文件画入
        Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        return newbmp;
    }
    
    /**
     * 生产湖中岸边树木的倒影，可以通过reflectedSize控制倒影长度（截取）
     * <功能详细描述>
     * @param originalImage
     * @param reflectedSize 倒影长度，原图片高度的比例
     * @return
     * @see [类、类#方法、类#成员]
     */
    public static Bitmap createReflection(Bitmap originalImage, float reflectedSize)
    {
        return createReflection(originalImage, reflectedSize, LINEARGRADIENT_COLOR_0, LINEARGRADIENT_COLOR_1);
    }
    
    /**
     * 生产湖中岸边树木的倒影，可以通过reflectedSize控制倒影长度（截取）
     * <功能详细描述>
     * @param originalImage
     * @param reflectedSize 倒影长度，原图片高度的比例
     * @return
     * @see [类、类#方法、类#成员]
     */
    public static Bitmap createReflection(Bitmap originalImage, float reflectedSize, int startC, int endC)
    {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        Matrix matrix = new Matrix();
        // 实现图片翻转90度
        matrix.preScale(1, -1);
        // 倒影的长度（高）
        int reflectedLength = (int)(height * reflectedSize);
        // 创建倒影图片（是原始图片的一半大小）
        Bitmap reflectionImage =
            Bitmap.createBitmap(originalImage, 0, height - reflectedLength, width, reflectedLength, matrix, false);
        //        // 创建总图片（原图片 + 倒影图片）
        Bitmap finalReflection = Bitmap.createBitmap(width, reflectedLength, Config.ARGB_8888);
        // 创建画布
        Canvas canvas = new Canvas(finalReflection);
        canvas.drawBitmap(reflectionImage, 0, 0, null);
        
        Paint shaderPaint = new Paint();
        // 创建线性渐变LinearGradient对象
        LinearGradient shader = new LinearGradient(0, 0, 0, reflectedLength, startC, endC, TileMode.MIRROR);
        shaderPaint.setShader(shader);
        shaderPaint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
        // 画布画出反转图片大小区域，然后把渐变效果加到其中，就出现了图片的倒影效果。
        canvas.drawRect(0, 0, width, reflectedLength, shaderPaint);
        
        if (reflectionImage != null)
        {
            reflectionImage.recycle();
        }
        return finalReflection;
    }
}
