/**
 * Title: LocalDeviceDiskItemView.java<br>
 * Package: com.rockchips.mediacenter.listadapter<br>
 * Description: 查看USB设备 显示分区item布局<br>
 * @author w00190739
 * @version v1.0<br>
 * Date: 2014-7-15下午2:53:28<br> 
 * Copyright © Huawei Technologies Co., Ltd. 2014. All rights reserved.
 */

package com.rockchips.mediacenter.listadapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;

import com.rockchips.mediacenter.R;
import com.rockchips.mediacenter.basicutils.bean.LocalDeviceInfo;
import com.rockchips.mediacenter.basicutils.util.BitmapUtil;
import com.rockchips.mediacenter.basicutils.util.ResLoadUtil;
import com.rockchips.mediacenter.basicutils.util.StringUtils;
import com.rockchips.mediacenter.viewutils.animgridview.HWListItemView;
import com.rockchips.mediacenter.bean.LocalDevice;

/**
 * Description:查看USB设备 显示分区item布局
 * @author w00190739
 * @version v1.0 Date: 2014-7-15 下午3:01:48<br>
 */

public class LocalDeviceDiskItemView extends HWListItemView
{
    /**
     * 上下文
     */
    private Context mContext;

    /**
     * 字体大小 默认为20
     */
    private int mTextSize = 20;

    /**
     * 缩略图的长，默认为197px, 可设置
     */
    private int mThumbailWith = 190;

    /**
     * 缩略图的宽，默认为197px, 可设置
     */
    private int mThumbailHeight = 190;

    /**
     * 内容
     */
    private LocalDevice mMediaInfo;

    private static Bitmap mBitmap;

    /**
     * 根据文件显示位置截取的画布
     * */
    private static final Rect msTextRect = new Rect(18, 213, 168, 243);

    public LocalDeviceDiskItemView(Context context)
    {
        super(context);
        this.mContext = context;
    }

    @Override
    public void draw(boolean isFocus, Canvas canvas, boolean parentHasFocus, Paint paint)
    {
        canvas.drawBitmap(getDrawBitmap(), 0, 0, paint);

        TextPaint fileNamePaint = getPaintBySizeAndColor(26, mContext.getResources().getColor(R.color.folder_image_name));
        String displayName = StringUtils.getParentFolderName(getMediaInfo().getMountPath());
        drawText(canvas, fileNamePaint, displayName, msTextRect);
    }

    public LocalDevice getMediaInfo()
    {
        return mMediaInfo;
    }

    public void setMediaInfo(LocalDevice mMediaInfo)
    {
        this.mMediaInfo = mMediaInfo;
    }

    public int getTextSize()
    {
        return mTextSize;
    }

    public void setTextSize(int mTextSize)
    {
        this.mTextSize = mTextSize;
    }

    public Bitmap getDrawBitmap()
    {
        if (mBitmap == null || mBitmap.isRecycled())
        {
            mBitmap = ResLoadUtil.getBitmapById(mContext, R.drawable.disk_icon_big);
            mBitmap = Bitmap.createScaledBitmap(mBitmap, mThumbailWith, mThumbailHeight, true);
        }
        return mBitmap;
    }

    @Override
    public void recyle()
    {
        return;
    }

    @Override
    public boolean showBig()
    {
        return false;
    }

}
