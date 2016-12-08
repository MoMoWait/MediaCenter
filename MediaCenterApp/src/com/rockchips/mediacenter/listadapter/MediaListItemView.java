package com.rockchips.mediacenter.listadapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Layout.Alignment;
import android.text.TextPaint;

import com.rockchips.mediacenter.R;
import com.rockchips.mediacenter.basicutils.util.ResLoadUtil;
import com.rockchips.mediacenter.viewutils.textdrawer.SmartLayoutTextDrawer;

/**
 * 
 * 就是listview的item 含有两种方案，单列文�?多列文字 <功能详细描述>
 * 
 * @author t00181037
 * @version [版本�? 2013-2-6]
 * @see [相关�?方法]
 * @since [产品/模块版本]
 */
public class MediaListItemView extends IconItemView
{

    public enum ENUMLISTITEMMODE {
        LIST_ITEM_MODE_ICON_DOUBLE, LIST_ITEM_MODE_ICON_SINGLE, LIST_ITEM_MODE_DOUBLE, LIST_ITEM_MODE_SINGLE, // 单列
        LIST_ITEM_MODE_UNKNOWN, // 双列
    }

    public enum ENUMLISTTYPEMODE {
        LIST_TYPE_ALL, // �?��歌曲展示
        LIST_TYPE_ARTIST, // 按艺术家展示
    }

    private ENUMLISTITEMMODE mEnumMode = ENUMLISTITEMMODE.LIST_ITEM_MODE_ICON_DOUBLE;

    private ENUMLISTTYPEMODE mTypeMode = ENUMLISTTYPEMODE.LIST_TYPE_ALL;

    private int mExtraColor = 0xFFFFFF;

    private int mExtraFontSize = 22;

    private int mNameColor = 0xFFFFFF;

    private int mNameFontSize = 26;

    private Rect mRectExtra = new Rect(440, 16, 620, 50);

    private Rect mRectName = new Rect(20, 11, 390, 50);

    // single
    private Rect mRectSingleModeName = new Rect(40, 15, 520, 50);

    // 横线
    private Rect mSeparatorLineRect = new Rect(10, 70, 620, 71);

    private String mStrExtraCol = null;

    private String mStrNameCol = null;

    // textpaint for EXTRA
    private TextPaint mTPExtra = new TextPaint();

    // textpaint for name
    private TextPaint mTPName = new TextPaint();

    protected SmartLayoutTextDrawer mTVExtraSltd = new SmartLayoutTextDrawer();

    protected SmartLayoutTextDrawer mTVNameSltd = new SmartLayoutTextDrawer();
    
    protected SmartLayoutTextDrawer mTVSharingText = new SmartLayoutTextDrawer();

    // protected TextView mTVNameSltd;

    // 图标
    private Bitmap mItemIcon = null;

    protected Rect mRectIconOfAR = new Rect(560, 11, 608, 59);

    protected Rect mRectIcon = new Rect(40, 11, 68, 59);

    // 共享图标
    private Bitmap mBmpShared = null;
    //共享文字
    private String mShareText = null;

    protected Rect mRectSharedOfAR = new Rect(20, 20, 56, 56);

    protected Rect mRectShared = new Rect(592, 20, 624, 56);
    
    protected Rect mRectSharing = new Rect(524, 20, 624, 56);

    public MediaListItemView(Context context)
    {
        super(context);

        // 该场合在新盒子有概率行空指针
        // try
        // {
        // mTVNameSltd = new TextView(context);
        // }
        // catch (Exception e)
        // {
        // Log.e("MediaListItemView", "new TextView(context) exception.");
        // }

        mTPName.setTextAlign(Align.LEFT);

        mTPName.setColor(mNameColor);
        mTPName.setAlpha(255);
        mTPName.setTextSize(mNameFontSize);

        mTPExtra.setTextAlign(Align.LEFT);
        mTPExtra.setColor(mExtraColor);
        mTPExtra.setAlpha(255);
        mTPExtra.setTextSize(mExtraFontSize);

        mTVNameSltd.setPaint(mTPName);
        mTVNameSltd.setMaxLen(mRectName.right - mRectName.left);
        mTVNameSltd.setMaxLens(1);
        mTVNameSltd.setAlignment(Alignment.ALIGN_NORMAL);
        // if (mTVNameSltd != null)
        // {
        // mTVNameSltd.setTextSize(26);
        // mTVNameSltd.setWidth(400);
        // }

        mTVExtraSltd.setPaint(mTPExtra);

        mTVExtraSltd.setMaxLens(1);
        mTVExtraSltd.setARflag(false);

        mTVExtraSltd.setMaxLen(180);
        mTVExtraSltd.setAlignment(Alignment.ALIGN_NORMAL);
        
        
        mTVSharingText.setPaint(mTPName);
        mTVSharingText.setMaxLen(mRectSharing.right - mRectSharing.left);
        mTVSharingText.setMaxLens(1);
        mTVSharingText.setAlignment(Alignment.ALIGN_NORMAL);
    }

    @Override
    public void draw(boolean isLast, Canvas canvas, boolean parentHasFocus, Paint paint)
    {
        // canvas.drawColor(R.color.gray);
        switch (getEnumMode())
        {

            case LIST_ITEM_MODE_SINGLE:

                if (mBmpShared != null)
                {
                    canvas.save();
                    canvas.translate(mRectShared.left, mRectShared.top);
                    canvas.drawBitmap(getBmpShared(), 0, 0, null);
                    canvas.restore();
                }
                
                if (getTextShared() != null)
                {
                    canvas.save();
                    canvas.translate(mRectSharing.left, mRectSharing.top);
                    mTVSharingText.setOrignalText(getTextShared());
                    mTVSharingText.draw(canvas);                    
                    canvas.restore();
                }

                canvas.save();
                if (getItemIcon() != null)
                {
                    canvas.translate(mRectIcon.left, mRectIcon.top);
                    canvas.drawBitmap(getItemIcon(), 0, 0, null);
                    canvas.restore();
                    canvas.save();
                    canvas.clipRect(mRectIcon.right + mRectSingleModeName.left, mRectSingleModeName.top, mRectSingleModeName.right, mRectSingleModeName.bottom);
                    canvas.translate(mRectIcon.right, 0);
                }

                canvas.translate(mRectSingleModeName.left, mRectSingleModeName.top);
                mStrNameCol = mStrNameCol == null ? ResLoadUtil.getStringById(getContext(), R.string.unknown) : mStrNameCol;
                mTVNameSltd.setOrignalText(mStrNameCol);
                mTVNameSltd.draw(canvas);
                canvas.restore();

                if (mStrExtraCol != null)
                {
                    canvas.save();
                    canvas.clipRect(mRectIcon.right + mRectSingleModeName.right, mRectSingleModeName.top, 0, mRectSingleModeName.bottom);
                    canvas.translate(mRectSingleModeName.right, mRectSingleModeName.top);
                    mTVExtraSltd.setOrignalText(mStrExtraCol);
                    mTVExtraSltd.draw(canvas);
                    canvas.restore(); 
                }

                break;

            case LIST_ITEM_MODE_DOUBLE:
                // 画文�?歌曲�?
                canvas.save();
                canvas.clipRect(mRectName);
                canvas.translate(mRectName.left, mRectName.top);
                if (getShowType() == ENUMLISTTYPEMODE.LIST_TYPE_ALL)
                {
                    mStrNameCol = mStrNameCol == null ? ResLoadUtil.getStringById(getContext(), R.string.unknown_title) : mStrNameCol;
                }
                else if (getShowType() == ENUMLISTTYPEMODE.LIST_TYPE_ARTIST)
                {
                    mStrNameCol = mStrNameCol == null ? ResLoadUtil.getStringById(getContext(), R.string.unknown_artist) : mStrNameCol;
                }
                else
                {
                    mStrNameCol = mStrNameCol == null ? ResLoadUtil.getStringById(getContext(), R.string.unknown) : mStrNameCol;
                }
                canvas.restore();

                // 画文本：艺术�?
                canvas.save();
                canvas.clipRect(mRectExtra);
                canvas.translate(mRectExtra.left, mRectExtra.top);

                if (getShowType() == ENUMLISTTYPEMODE.LIST_TYPE_ALL)
                {
                    mStrExtraCol = mStrExtraCol == null ? ResLoadUtil.getStringById(getContext(), R.string.unknown_artist) : mStrExtraCol;
                }
                else
                {
                    mStrExtraCol = mStrExtraCol == null ? ResLoadUtil.getStringById(getContext(), R.string.unknown) : mStrExtraCol;
                }

                mTVExtraSltd.setOrignalText(mStrExtraCol);
                mTVExtraSltd.draw(canvas);
                canvas.restore();
                break;
            case LIST_ITEM_MODE_ICON_DOUBLE:
                break;
            case LIST_ITEM_MODE_ICON_SINGLE:
                break;
            default:
                break;
        }

        if (!isLast)
        {
            // 画线
            canvas.save();
            canvas.clipRect(mSeparatorLineRect);
            canvas.translate(mSeparatorLineRect.left, mSeparatorLineRect.top);

            Drawable drawablebg = ResLoadUtil.getDrawableById(getContext(), R.drawable.list_separator_line);
            drawablebg.setAlpha(80);
            drawablebg.setBounds(0, 0, mSeparatorLineRect.right, mSeparatorLineRect.bottom);
            drawablebg.draw(canvas);

            canvas.restore();
        }
        else
        {
        }
    }

    /**
     * @return 返回 mEnumMode
     */
    public ENUMLISTITEMMODE getEnumMode()
    {
        return mEnumMode;
    }

    /**
     * @return 返回 extraColor
     */
    public int getExtraColor()
    {
        return mExtraColor;
    }

    /**
     * @return 返回 extraSize
     */
    public int getExtraSize()
    {
        return mExtraFontSize;
    }

    /**
     * @return 返回 nameColor
     */
    public int getNameColor()
    {
        return mNameColor;
    }

    /**
     * @return 返回 nameSize
     */
    public int getNameSize()
    {
        return mNameFontSize;
    }

    /**
     * @return 返回 rectExtra
     */
    public Rect getRectExtra()
    {
        return mRectExtra;
    }

    /**
     * @return 返回 rectName
     */
    public Rect getRectName()
    {
        return mRectName;
    }

    /**
     * @return 返回 strExtraCol
     */
    public String getStrExtraCol()
    {
        return mStrExtraCol;
    }

    /**
     * @return 返回 strNameCol
     */
    public String getStrNameCol()
    {
        return mStrNameCol;
    }

    /**
     * 显示类型的控�?
     * */
    public ENUMLISTTYPEMODE getShowType()
    {
        return mTypeMode;
    }

    public void setShowType(ENUMLISTTYPEMODE type)
    {
        this.mTypeMode = type;
    }

    /**
     * @param 对mEnumMode进行赋�?
     */
    public void setEnumMode(ENUMLISTITEMMODE mEnumMode)
    {
        this.mEnumMode = mEnumMode;
    }

    /**
     * @param 对extraColor进行赋�?
     */
    public void setExtraColor(int extraColor)
    {
        mExtraColor = extraColor;
    }

    /**
     * @param 对extraSize进行赋�?
     */
    public void setExtraSize(int extraSize)
    {
        mExtraFontSize = extraSize;
    }

    /**
     * @param 对nameColor进行赋�?
     */
    public void setNameColor(int nameColor)
    {
        mNameColor = nameColor;
    }

    /**
     * @param 对nameSize进行赋�?
     */
    public void setNameSize(int nameSize)
    {
        mNameFontSize = nameSize;
    }

    /**
     * @param 对rectExtra进行赋�?
     */
    public void setRectExtra(Rect rectExtra)
    {
        mRectExtra = rectExtra;
    }

    // 表示模式的枚�?

    /**
     * @param 对rectName进行赋�?
     */
    public void setRectName(Rect rectName)
    {
        mRectName = rectName;
    }

    /**
     * @param 对strExtraCol进行赋�?
     */
    public void setStrExtraCol(String strExtraCol)
    {
        mStrExtraCol = strExtraCol;
    }

    /**
     * @param 对strNameCol进行赋�?
     */
    public void setStrNameCol(String strNameCol)
    {
        mStrNameCol = strNameCol;
    }

    /**
     * @return 返回 mItemIcon
     */
    public Bitmap getItemIcon()
    {
        return mItemIcon;
    }

    /**
     * @param 对mItemIcon进行赋�?
     */
    public void setItemIcon(Bitmap itemIcon)
    {
        this.mItemIcon = itemIcon;
    }

    /**
     * @return 返回 bmpShared
     */
    public Bitmap getBmpShared()
    {
        return mBmpShared;
    }

    /**
     * @param 对bmpShared进行赋�?
     */
    public void setBmpShared(Bitmap bmpShared)
    {
        mBmpShared = bmpShared;
    }
    
    /**
     * @return 返回 bmpShared
     */
    public String getTextShared()
    {
        return mShareText;
    }

    /**
     * @param 对bmpShared进行赋�?
     */
    public void setTextShared(String shareText)
    {
        mShareText = shareText;
    }

    /**
     * 添加已经选择按钮，用于图片播放时候的背景音乐选择
     * @param bmpShared
     * @see [类�?�?方法、类#成员]
     */
    public void setBmpFavorie(Bitmap bmpShared)
    {
        mBmpShared = bmpShared;
        mRectShared = new Rect(572, 10, 624, 56);
    }

    @Override
    public boolean showBig()
    {
        return false;
    }

    @Override
    public Bitmap getDrawBitmap()
    {
        // TODO Auto-generated method stub
        return null;
    }

}
