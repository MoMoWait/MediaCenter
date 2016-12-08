package com.rockchips.mediacenter.listadapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.rockchips.mediacenter.basicutils.bean.LocalMediaInfo;
import com.rockchips.mediacenter.viewutils.animgridview.HWListItemView;

/**
 * 
 * 定义itemView图片
 * <功能详细描述>
 * 
 * @author  t00181037
 * @version  [版本号, 2013-1-7]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public abstract class IconItemView extends HWListItemView
{
    private static final String TAG = "MediaCenterApp";
    
    /**
     * 默认,真实解压出来后的Icon图标
     */
    private Bitmap mDefaultBitmap = null;
    
    //由 ThumbnailChangedListener 返回
    private Bitmap mIconBitmap = null;
    
    /**
     * 定义ItemView的高、宽
     */
    private int mBmpWidth = 0;
    
    private int mBmpHeight = 0;
    
    /**
     * 定义关联数据
     */
//    private MediaInfo mMediaInfo = null;
    
    private LocalMediaInfo mMediaInfo;
    
    /**
     * 上下文
     */
    private Context mContext = null;
    
    public IconItemView(Context context)
    {
        super(context);
        setContext(context);
    }
    
    /**
     * 给子view一个机会，用于控制其内部绘图的大小
     * @param width
     * @param height
     */
    public void init(int width, int height)
    {
        setBmpWidth(width);
        setBmpHeight(height);
    }
    
    /**
     * 给子View提供一个自己清理自身内部资源的机会
     */
    public void recyle()
    {
        if (mIconBitmap != null && !mIconBitmap.isRecycled())
        {
            mIconBitmap.recycle();
        }
        mIconBitmap = null;
        mMediaInfo = null;
    }
    
    public int getBmpWidth()
    {
        return mBmpWidth;
    }
    
    public void setBmpWidth(int width)
    {
        this.mBmpWidth = width;
    }
    
    public int getBmpHeight()
    {
        return mBmpHeight;
    }
    
    public void setBmpHeight(int height)
    {
        mBmpHeight = height;
    }
    
    public Bitmap getIconBitmap()
    {
        return mIconBitmap;
    }
    
    public void setIconBitmap(Bitmap iconBitmap)
    {
        mIconBitmap = iconBitmap;
    }
    
    public Bitmap getDefaultBitmap()
    {
        return mDefaultBitmap;
    }
    
    public void setDefaultBitmap(Bitmap defaultBitmap)
    {
        mDefaultBitmap = defaultBitmap;
    }
    
    public LocalMediaInfo getMediaInfo()
    {
        return mMediaInfo;
    }
    
    /**
     * 设置MediaInfo
     * 1、设置
     * 2、如果是网络的请求URL缩略图，如果非网络的采用默认图片
     * 
     * @param mediainfo
     * @see [类、类#方法、类#成员]
     */
    public void setMediaInfo(LocalMediaInfo mediainfo)
    {
        if (mediainfo == null)
        {
            return;
        }
        if (mediainfo == mMediaInfo)
        {
            return;
        }
        
        mMediaInfo = mediainfo;
    }
    
    public Context getContext()
    {
        return mContext;
    }
    
    public void setContext(Context context)
    {
        mContext = context;
    }
    
    /**
     * 子View自己绘制自己
     * @param isFocus
     * @param canvas
     * @param parentHasFocus
     */
    public abstract void draw(boolean isFocus, Canvas canvas, boolean parentHasFocus, Paint paint);
    
}
