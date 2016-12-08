/**
 * Title: ALImageBrowserItemView.java<br>
 * Package: com.rockchips.mediacenter.listadapter<br>
 * Description: TODO<br>
 * @author w00190739
 * @version v1.0<br>
 * Date: 2014-7-15下午3:01:48<br> 
 * Copyright © Huawei Technologies Co., Ltd. 2014. All rights reserved.
 */

package com.rockchips.mediacenter.listadapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;

import com.rockchips.mediacenter.R;
import com.rockchips.mediacenter.basicutils.bean.LocalMediaInfo;
import com.rockchips.mediacenter.basicutils.constant.Constant;
import com.rockchips.mediacenter.basicutils.util.IICLOG;
import com.rockchips.mediacenter.basicutils.util.StringUtils;
import com.rockchips.mediacenter.viewutils.animgridview.HWListItemView;
import com.rockchips.mediacenter.widget.ThumbnailManager;

/**
 * Description: 浏览全部图片的itemview 要用到此itemview的有三处 1：浏览DLNA设备的全部图片 2：浏览USB和Sdcard设备图片，包括显示分区，显示图片，显示文件夹
 * @author w00190739
 * @version v1.0 Date: 2014-7-15 下午3:01:48<br>
 */

public class ALImageBrowserItemView extends HWListItemView
{
    private static final IICLOG Log = IICLOG.getInstance();

    private static final String TAG = "MediaCenterApp";

    /**
     * 上下文
     */
    private Context mContext;

    /**
     * 内容
     */
    private LocalMediaInfo mMediaInfo;
    
    /**
     * 缩略图的长，默认为197px, 可设置
     */
    private int mThumbailWith = 190;

    /**
     * 缩略图的宽，默认为197px, 可设置
     */
    private int mThumbailHeight = 190;

    /**
     * 缩略图位图
     */
    private Bitmap mBitmap;

    private int nameColorId;

    private int numColorId;

    private String strHint;

    private static Bitmap mImageDefaultBg;

    /**
     * 相册item默认背景图
     * */
    private Bitmap getImageDefaultBg(Context context)
    {
        if (null == mImageDefaultBg || mImageDefaultBg.isRecycled())
        {
            mImageDefaultBg = BitmapFactory.decodeResource(context.getResources(), R.drawable.all_image_item_default_bg);
        }
        return mImageDefaultBg;
    }

    /**
     * 根据文件显示位置截取的画布
     * */
    private Rect msTextRect;

    private Rect msFolderNum;

    public ALImageBrowserItemView(Context mContext)
    {
        super(mContext);
        this.mContext = mContext;
        nameColorId = mContext.getResources().getColor(R.color.folder_image_name);
        numColorId = mContext.getResources().getColor(R.color.file_num_bg);
        strHint = mContext.getResources().getString(R.string.image_unit);
        msTextRect = new Rect(18, 213, 168, 243);
        msFolderNum = new Rect(18, 243, 168, 273);
    }

    @Override
    public void draw(boolean isFocus, Canvas canvas, boolean parentHasFocus, Paint paint)
    {
        boolean isBmpNull = false;
        synchronized (this)
        {
            if (mBitmap == null || mBitmap.isRecycled())
            {
                requestDownloadThumbail();
            }
            if (mBitmap != null && !mBitmap.isRecycled())
            {
                canvas.drawBitmap(mBitmap, 0, 0, paint);
            }
            else
            {
                isBmpNull = true;
            }
        }
        if (isBmpNull)
        {
            canvas.drawBitmap(getImageDefaultBg(mContext), 0, 0, paint);
        }

        LocalMediaInfo mediaInfo = getMediaInfo();
        if (mediaInfo != null
                && (mediaInfo.getmFileType() == Constant.MediaType.IMAGEFOLDER || mediaInfo.getmFileType() == Constant.MediaType.FOLDER))
        {
            String imageSum = "0";
            if (null != getMediaInfo())
            {
                imageSum = String.valueOf(getMediaInfo().getmFiles());
            }
            imageSum += strHint;

            TextPaint fileNamePaint = getPaintBySizeAndColor(26, nameColorId);
            String displayName;
            if (mediaInfo.getmDeviceType() == Constant.DeviceType.DEVICE_TYPE_DMS)
            {
                displayName = mediaInfo.getmTitle();
            }
            else
            {
                displayName = StringUtils.getParentFolderName(getMediaInfo().getUrl());
            }
            drawText(canvas, fileNamePaint, displayName, msTextRect);

            TextPaint fileNumPaint = getPaintBySizeAndColor(20, numColorId);
            drawText(canvas, fileNumPaint, imageSum, msFolderNum);
        }
    }

    /**
     * 缩略图更新监听类
     */
    private ThumbnailManager.ThumbnailChangedListener mThumbnailChangedListener = new ThumbnailManager.ThumbnailChangedListener()
    {

        @Override
        public int getLevel()
        {
            int level = ThumbnailManager.LEVEL_HIGH;
            return level;
        }

        @Override
        public void onError()
        {
            Log.d(TAG, "download fail---->fail");
        }

        /** 图片下载完成后的回调处理,刷新界面，重写draw */
        @Override
        public void onFinished(LocalMediaInfo info, Bitmap thumbnail)
        {
            Log.d(TAG, "download thumbnail---->start");
            if (info == getMediaInfo())
            {

                try
                {
                    if (thumbnail != null && thumbnail.getWidth() > 0 && thumbnail.getHeight() > 0)
                    {
                        recyle();
                        boolean bmpNotEqualTl = false;
                        synchronized (ALImageBrowserItemView.this)
                        {
                            mBitmap = Bitmap.createScaledBitmap(thumbnail, mThumbailWith, mThumbailHeight, true);
                            if (mBitmap != thumbnail)
                            {
                                bmpNotEqualTl = true;
                            }
                        }
                        if (bmpNotEqualTl && !thumbnail.isRecycled())
                        {
                            thumbnail.recycle();
                        }
                        thumbnail = null;

                        ALImageBrowserItemView.this.invalidate();
                    }
                }
                catch (OutOfMemoryError e)
                {
                    Log.d(TAG, "Bitmap.createScaledBitmap OutOfMemoryError");
                    // mBitmap = ResLoadUtil.getBitmapById(mContext, R.drawable.all_image_item_default_bg);
                }
            }
            else
            {
                Log.d(TAG, "reflash thumbnail---->fail");
            }
        }

    };

    /** 发送下载图片缩略图请求 */
    private void requestDownloadThumbail()
    {
        if (mMediaInfo != null && mThumbnailChangedListener != null)
        {
            ThumbnailManager.getInstance().requestThumbnail(mMediaInfo, mThumbailWith, mThumbailHeight, mThumbnailChangedListener);
        }
    }

    public LocalMediaInfo getMediaInfo()
    {
        return mMediaInfo;
    }

    /**
     * 设置内容信息时，同时发送下载图片缩略图请求
     */
    public void setMediaInfo(LocalMediaInfo mediaInfo)
    {
        Log.d(TAG, mediaInfo.getmParentPath() + "====" + mediaInfo.getmFiles() + "====" + mediaInfo.getmFileType());
        if (mMediaInfo == null)
        {
            mMediaInfo = mediaInfo;
        }
        else
        {
            if (mMediaInfo.equals(mediaInfo))
            {
                // Log.e(TAG, "equals mMediaInfo =======");
                return;
            }
            else
            {
                // Log.e(TAG, "equals mMediaInfo !!!!!!!!!!");
                mMediaInfo = mediaInfo;
            }
        }
        
        /** 加载默认位图 */
       // getDrawBitmap();
        /** 异步请求下载缩略图 */
        requestDownloadThumbail();
    }

    public Bitmap getDrawBitmap()
    {
        Bitmap bmp = null;
        synchronized (this)
        {
            if (mBitmap != null && !mBitmap.isRecycled())
            {
                bmp = mBitmap;
            }
        }
        if (null == bmp)
        {
            bmp = Bitmap.createScaledBitmap(getImageDefaultBg(mContext), mThumbailWith, mThumbailHeight, true);
        }
        return bmp;
    }

    @Override
    public void recyle()
    {
        synchronized (this)
        {
            if (mBitmap != null && !mBitmap.isRecycled())
            {
                mBitmap.recycle();
            }
            mBitmap = null;
        }
    }

    @Override
    public boolean showBig()
    {
        return (mMediaInfo.getmFileType() == Constant.MediaType.IMAGE) ? true : false;
    }

}
