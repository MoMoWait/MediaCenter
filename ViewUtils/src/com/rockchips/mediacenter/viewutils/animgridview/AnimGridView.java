package com.rockchips.mediacenter.viewutils.animgridview;

import java.util.Stack;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import com.rockchips.mediacenter.viewutils.R;
import com.rockchips.mediacenter.viewutils.R.styleable;

/**
 * 带有焦点移动动画的gridview，也可用作listview
 * @author s00211113
 * 
 */
public class AnimGridView extends SurfaceView implements Callback, HWAdapterView, OnGestureListener
{

    private int mViewWidth; // 本View的宽

    private int mViewHeight; // 本View的高

    private int mItemWidth; // 子view项的宽

    private int mItemHeight; // 子view的高

    private int mItemsGap; // 横向每个子view之间的空隙宽度

    private int mLinesGap; // 竖向每一行之间的空隙宽

    private int mItemDefaultHeight; // 焦点的高

    private Rect mViewBounds; // 控件的大小

    private Rect mPadding; // 内容边距

    private Rect mContentBounds; // 内容的大小

    private Rect mLineBounds; // 每一行的大小

    private Rect mItemBounds; // 每一个子view的大小

    private Rect mItemDefaultBounds; // 子项默认大小

    private Rect mFocusBounds; // 焦点的大小

    private Rect mFocusViewBounds; // 焦点控件的大小

    private static final int FOCUS_VIEW_PADDING = 20; // 在焦点变大模式下的一个纠正数字

    private int mFocusPadding; // 焦点框的四周边距

    private int mFocusWidth; // 焦点框的宽

    private int mFocusHeight; // 焦点框的高

    private Context mContext; // 上下文

    private Paint mPaint; // 画笔

    private Point mFocusIndexPoint; // 焦点在数组中的坐标

    private Drawable mFocusDrawable; // 焦点框

    private Drawable mFocuShadowDrawable; // 焦点框

    private Drawable mGridViewBackGround; // GirdView底部图片

    private Drawable mItemDefaultDrawable; // 子项默认图片

    private int mDataSize; // 总数据大小

    private ItemView[][] mItemViews; // 固定大小的可视子view

    private int[] mItemCoordX; // 子view的x坐标，每个坐标都是相对每一行左上角为（0,0）的

    private int[] mItemCoordY; // 每一行的y轴坐标

    private boolean mHasFocus; // 当前view是否处于焦点状态

    private int mLineNum; // 行数

    private int mTotalVisibleLineNum; // 完全显示的行数

    private int mLineItemNum; // 每一行的子view数

    private SurfaceHolder mHolder; // surface的holder

    private int moveDistanceY; // 当前动画的移动距离

    private static final int FOCUS_SPEED_DEFAULT = 40;

    private int mFocusSpeed = FOCUS_SPEED_DEFAULT; // 动画移动速度

    private boolean mIsMoveing; // 标识当前是否正在移动

    private Handler mDrawHandler; // 绘图线程消息队列handler

    private DrawThread mDrawThread; // 绘图线程

    private boolean mIsViewInited; // view是否初始完毕

    private boolean mIsViewAndDataInit; // 数据是否初始化完毕

    private HWListViewBaseAdapter mAdapter; // 数据提供器

    private OnItemStateChangedListener mOnItemStateChangedListener; // 子view被选择或处于焦点状态是的回调接口

    private OnScrollerChangedListener mOnScrollerChangedListener; // 滚动消息回调

    private int mFocusPosition;

    private final Object mItemViewsLock = new Object(); // 数据锁

    private Drawable mScrollDrawable; // 下拉条

    private Rect mScrollBounds; // 下拉条的大小

    private boolean mHasScrollView = true; // 是否打开下拉条

    private static final int SCROLL_BAR_ALPHA_MAX = 255;

    private int mScrollBarAlpha = SCROLL_BAR_ALPHA_MAX; // 滚动条的透明度

    private GestureDetector mGestureDetector; // 手势识别

    private boolean mBScrollBoundsVisible;

    private long mKeydownTicket; // 按键计时，是否是长按键事件

    private boolean mLongPressFlag;

    private int mLastKeyCode = -1;

    private boolean mItemViewSmoothMove; // ItemView 的移动是否需要平滑移动

    private boolean mIsImageMarquee; // 界面是否是浏览图片时需要跑马灯

    private Stack<Integer> mFocusStack = new Stack<Integer>();

    private int mStartDP;

    private boolean mBackFlag;

    private boolean mBackFromPlayer;

    private int mItemViewMoveCount;

    private int mFixLeftOfFocus;

    private int mFixTopOfFocus;

    private int mFixWidthOfFocus;

    private int mFixHeightOfFocus;

    private int mItemViewSpeed;

    private boolean mbResetList = true; // 是否是重置了一个List给adapter,还是分页机制导致的setAdapter

    private boolean mHasFocusBig; // 在方格显示的时候焦点变大，用于图片浏览

    private boolean mBeKeyDowning; // 当前按键是否已经Up，true为按下未up

    private static final int FOCUS_SPEED_DISPLACEMENT = 3;

    private static final float FOCUS_ZOOM_OUT_MULTIPLE = 1.2f;

    private static final int FOCUS_SPEED_PLUS = 30;

    private static final int LONG_PRESS_FOCUS_SPEED = 150;
    
    private Object mObjectLock = new Object();

    public AnimGridView(Context context)
    {
        super(context);
        init(context, null);
    }

    public AnimGridView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context, attrs);
    }

    public AnimGridView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    /**
     * 对本view的部分参数进行初始化
     * @param context
     * @param attrs
     */
    private void init(Context context, AttributeSet attrs)
    {
        mContext = context;
        mGestureDetector = new GestureDetector(mContext, this);

        if (attrs == null)
        {
            throw new UnsupportedOperationException(mContext.getResources().getString(R.string.gridView_not_support));
        }
        else
        {
            TypedArray typeArray = mContext.obtainStyledAttributes(attrs, styleable.CustomListView);
            // 获取子项的宽和高
            mItemWidth = typeArray.getInt(styleable.CustomListView_itemWidth, 0);
            mItemWidth = mItemWidth < 0 ? 0 : mItemWidth;
            mItemHeight = typeArray.getInt(styleable.CustomListView_itemHeight, 0);
            mItemHeight = mItemHeight < 0 ? 0 : mItemHeight;
            mItemDefaultHeight = typeArray.getInt(styleable.CustomListView_itemDefaultHeight, 0);
            mItemDefaultHeight = mItemDefaultHeight < 0 ? 0 : mItemDefaultHeight;

            // 获取横竖行距
            mItemsGap = typeArray.getInt(styleable.CustomListView_itemGap, 0);
            mItemsGap = mItemsGap < 0 ? 0 : mItemsGap;
            mLinesGap = typeArray.getInt(styleable.CustomListView_lineGap, 0);
            mLinesGap = mLinesGap < 0 ? 0 : mLinesGap;

            // 获取焦点框的边距
            mFocusPadding = typeArray.getInt(styleable.CustomListView_focusPadding, 0);
            mFocusPadding = mFocusPadding < 0 ? 0 : mFocusPadding;

            // 初始化焦点样式
            int focusDrawableResId = typeArray.getResourceId(styleable.CustomListView_focusDrawable, -1);
            try
            {
                mFocusDrawable = mContext.getResources().getDrawable(focusDrawableResId);
            }
            catch (Resources.NotFoundException ex)
            {
                mFocusDrawable = null;
            }
            // 获取GridView的背景图片
            int focusBackGroundResId = typeArray.getResourceId(styleable.CustomListView_bgDrawable, -1);
            try
            {
                mGridViewBackGround = mContext.getResources().getDrawable(focusBackGroundResId);
            }
            catch (Resources.NotFoundException ex)
            {
                mGridViewBackGround = null;
            }

            // 获取默认子项样式，如果设置了这个属性，这个控件会保证界面上子项都会被填充，如果数据为空就会用这个图片来进行填充
            int defaultResId = typeArray.getResourceId(styleable.CustomListView_defaultDrawable, -1);
            try
            {
                mItemDefaultDrawable = mContext.getResources().getDrawable(defaultResId);
            }
            catch (Resources.NotFoundException ex)
            {
                mItemDefaultDrawable = null;
            }

            mViewBounds = new Rect();
            mPadding = new Rect();
            mFocusBounds = new Rect();
            mFocusViewBounds = new Rect();
            mContentBounds = new Rect();
            mLineBounds = new Rect();
            mItemBounds = new Rect();
            mItemDefaultBounds = new Rect();
            mScrollBounds = new Rect();

            mPadding.left = getPaddingLeft();
            mPadding.top = getPaddingTop();
            mPadding.right = getPaddingRight();
            mPadding.bottom = getPaddingBottom();

            mHolder = getHolder();
            mHolder.setFormat(PixelFormat.TRANSPARENT);

            mHolder.addCallback(this);
            mFocusIndexPoint = new Point(0, 1);
            mDataSize = 0;
            mFocusPosition = 0;
            typeArray.recycle();

            setFocusSpeed(mItemHeight >> FOCUS_SPEED_DISPLACEMENT);

        }

    }

    /**
     * 完成对本view的大小参数的获取
     */
    protected void onLayout(boolean changed, int left, int top, int right, int bottom)
    {
        super.onLayout(changed, left, top, right, bottom);
        if (changed)
        {
            // 初始化控件的宽高
            mViewWidth = getWidth();
            mViewHeight = getHeight();
            // 初始化控件大小
            mViewBounds.left = 0;
            mViewBounds.right = mViewWidth;
            mViewBounds.top = 0;
            mViewBounds.bottom = mViewHeight;

            // 初始化内容边距
            mPadding.left = getPaddingLeft();
            mPadding.top = getPaddingTop();
            mPadding.right = getPaddingRight();
            mPadding.bottom = getPaddingBottom();

            // 初始化内容区域大小
            mContentBounds.left = 0;
            mContentBounds.top = 0;
            mContentBounds.right = mViewWidth - mPadding.right - mPadding.left;
            mContentBounds.bottom = mViewHeight - mPadding.bottom - mPadding.top;

            // 初始化横向行的大小
            mLineBounds.left = 0;
            mLineBounds.top = 0;
            mLineBounds.right = mContentBounds.right;
            mLineBounds.bottom = mItemHeight;

            // 初始化子项的大小
            mItemBounds.left = 0;
            mItemBounds.top = 0;
            mItemBounds.right = mItemWidth;
            mItemBounds.bottom = mItemHeight;

            // 子项的默认宽度，因为有些子项在底部会有标题，为了不让焦点框不覆盖标题，就需要设置这个属性，如果不存在标题，则这个大小应该和子项的大小一致
            mItemDefaultBounds.left = 0;
            mItemDefaultBounds.right = mItemWidth;
            mItemDefaultBounds.top = 0;
            mItemDefaultBounds.bottom = mItemDefaultHeight;
            if (mItemDefaultDrawable != null)
            {
                mItemDefaultDrawable.setBounds(mItemDefaultBounds);
            }

            if (mHasFocusBig)
            {
                mFocusWidth = (int) ((mItemWidth + mFocusPadding + mFocusPadding + mFixWidthOfFocus) * FOCUS_ZOOM_OUT_MULTIPLE);
                mFocusHeight = (int) ((mItemDefaultHeight + mFocusPadding + mFocusPadding + mFixHeightOfFocus) * FOCUS_ZOOM_OUT_MULTIPLE);
            }
            else
            {
                mFocusWidth = mItemWidth + mFocusPadding + mFocusPadding + mFixWidthOfFocus;
                mFocusHeight = mItemDefaultHeight + mFocusPadding + mFocusPadding + mFixHeightOfFocus;
            }
            mFocusBounds.left = 0;
            mFocusBounds.top = 0;
            mFocusBounds.right = mFocusWidth;
            mFocusBounds.bottom = mFocusHeight;

            mFocusViewBounds.left = FOCUS_VIEW_PADDING;
            mFocusViewBounds.top = FOCUS_VIEW_PADDING;
            mFocusViewBounds.right = mFocusWidth - FOCUS_VIEW_PADDING;
            mFocusViewBounds.bottom = mFocusHeight - FOCUS_VIEW_PADDING;

            //
            if (getImageMarquee())
            {
                setZOrderMediaOverlay(true);
            }
            else
            {
                setZOrderOnTop(true);
            }

            if (getItemViewSmooth())
            {
                setItemViewSpeed(mItemHeight >> 2); // view 平滑移动
            }
            else
            {
                setItemViewSpeed(mItemHeight + mLinesGap); // view 一次性移动一个
            }

            initView();
        }
    }

    /**
     * 在获取大小后对view进行初始化.<br>
     * 将控件的view模型初始化出来，此时可以没有数据，一旦初始化完毕，则就可以进行绘制操作。
     */
    private void initView()
    {
        mIsViewInited = false;
        setFocusable(false);
        mIsViewAndDataInit = false;
        clearItems();
        if (mViewWidth <= 0 || mViewHeight <= 0 || mItemWidth <= 0 || mItemHeight <= 0)
        {
            mLineItemNum = 0;
            mLineNum = 0;
            return;
        }
        // 每一行可以显示的子view数，不允许出现只显示一半的情况,考虑到最右边的那个子view的右边界是可以没有间隙的，
        // 所以要用view的可视宽度加上一个字view间的间隙
        mLineItemNum = (mContentBounds.right + mItemsGap) / (mItemWidth + mItemsGap);
        // 计算可以显示的子view的行数，包括只显示部分的子view所在行，都 要计算在可视view的范围内。
        mTotalVisibleLineNum = mContentBounds.bottom / (mItemHeight + mLinesGap);
        // 如果可视行与每一个所占的高度相乘小于原view的可视区域高度，则说明有一行子view可能会只显示 了部分，所以需要加1
        if (mTotalVisibleLineNum * (mItemHeight + mLinesGap) < mContentBounds.bottom)
        {
            mLineNum = mTotalVisibleLineNum + 1;
        }
        // 为了增加缓存的view，用于动画，所以要加两行子view，这两行字view不会显示出来，它们分别作为可视view的最上面一行和最下面一行。
        mLineNum += 2;

        // 建立view的数据模型，
        mItemCoordX = new int[mLineItemNum];
        mItemCoordY = new int[mLineNum];
        int grideLineHeight = mLineBounds.bottom + mLinesGap;
        mItemViews = new ItemView[mLineNum][mLineItemNum];
        synchronized (mItemViewsLock)
        {
            for (int i = 0; i < mLineNum; i++)
            {
                mItemCoordY[i] = -grideLineHeight + i * grideLineHeight;
                for (int j = 0; j < mLineItemNum; j++)
                {
                    mItemViews[i][j] = new ItemView();
                    mItemViews[i][j].mDataposition = -1;
                    mItemViews[i][j].mItemView = null;
                    mItemCoordX[j] = j * (mItemWidth + mItemsGap);

                }
            }
        }
        mIsViewInited = true;
        reset();
        if (mAdapter != null)
        {
            fillDataFromStartDP(getStartDPFromVar());
        }

        if (mOnItemStateChangedListener != null)
        {
            mOnItemStateChangedListener.onItemGetForcus(mFocusPosition);
        }
    }

    /**
     * 在方格得到焦点是否会变大 在开始的时候进行设置，默认为false
     * @param changeBig true 变大
     * @see [类、类#方法、类#成员]
     */
    public void setFocusChangeBig(boolean changeBig)
    {
        mHasFocusBig = changeBig;
        if (mHasFocusBig)
        {
            mFocusWidth = (int) ((mItemWidth + mFocusPadding + mFocusPadding + mFixWidthOfFocus) * FOCUS_ZOOM_OUT_MULTIPLE);
            mFocusHeight = (int) ((mItemDefaultHeight + mFocusPadding + mFocusPadding + mFixHeightOfFocus) * FOCUS_ZOOM_OUT_MULTIPLE);
        }
    }

    /**
     * 设置控件的数据提供器。<br>
     * 如何新设置的adapter和原来的adapter一致，则视之为数据发生了更新。
     * @param adapter
     */
    public void setAdapter(HWListViewBaseAdapter adapter)
    {
        onAdapterChanged(adapter);
    }

    /**
     * 为下拉条定义自己的图片
     * @param drawable 下拉条自定义样式
     * @param isscroll 是否打开下拉条开关 true为打开
     * @see [类、类#方法、类#成员]
     */
    public void setScrollDrawable(Drawable drawable, boolean isscroll)
    {
        mScrollDrawable = drawable;
        mHasScrollView = isscroll;
    }

    /**
     * 获取当前的数据提供器
     * @return
     */
    public HWListViewBaseAdapter getAdapter()
    {
        return mAdapter;
    }

    @Override
    public void notifyDataChanged()
    {
        Log.d("TAG", "notifyDataChanged view");
        refresh();
    }

    /**
     * 数据提供接口发送改变。<br>
     * 数据提供接口发送了变化，将焦点置为初始位置，并重新填充数据.
     * @param newAdapter
     */
    private void onAdapterChanged(HWListViewBaseAdapter newAdapter)
    {
        reset();
        if (mAdapter != null)
        {
            mAdapter.setAdapterView(null);
//            mAdapter = null;
        }
        mIsViewAndDataInit = false;        
        mAdapter = newAdapter;

        if (mbResetList)
        {
            mFocusIndexPoint.x = 0;
            mFocusIndexPoint.y = 1;
        }
        else if (getBackFlag())
        {
            mFocusIndexPoint.x = 0;
            mFocusIndexPoint.y = 2;
        }
        else
        {
            mFocusIndexPoint.x = getFocusPos()[0];
            mFocusIndexPoint.y = getFocusPos()[1];
        }
        mStartDP = 0;

        if (mAdapter == null)
        {
            mDataSize = 0;
        }
        else
        {
            mAdapter.setAdapterView(this);
            mDataSize = mAdapter.getCount();
        }
        if (mIsViewInited)
        {
            if (mBackFromPlayer || (getBackFlag() && mbResetList))
            {
                fillDataFromStartDP(getStartDp());
            }
            else
            // 此分支是接收应用设置的数据填充点
            {
                fillDataFromStartDP(getStartDPFromVar());
            }
        }
    }

    public void setStartDp(int dp)
    {
        mStartDP = dp;
    }

    private void reset()
    {
        if (mDrawHandler != null)
        {
            mDrawHandler.removeMessages(DRAW_MSG);
            mDrawHandler.removeMessages(MOVE_LEFT_MSG);
            mDrawHandler.removeMessages(MOVE_RIGHT_MSG);
            mDrawHandler.removeMessages(MOVE_DOWN_MSG);
            mDrawHandler.removeMessages(MOVE_UP_MSG);
        }
        if (mUiHandler != null)
        {
//            mUiHandler.removeMessages(VIEW_MOVE_FINISH);
            mUiHandler.removeMessages(SCROLL_FINISH_MSG);
        }
    }

    private void clearViewMoveMsg()
    {
        if (mDrawHandler != null)
        {
            mDrawHandler.removeMessages(DRAW_MSG);
            mDrawHandler.removeMessages(VIEW_UP_MSG);
            mDrawHandler.removeMessages(VIEW_DOWN_MSG);
        }
    }

    /**
     * 填充数据。<br>
     * 从可显示的第一个数据开始填充数据，第一个子项填充的数据以参数定义的为准，其它位置的数据都相对这个位置进行填充
     * @param startDataPosition
     */
    public void fillDataFromStartDP(int startDataPosition)
    {

        startDataPosition = startDataPosition < 0 ? 0 : startDataPosition;

        Log.d("TAG", "startDataPosition =" + startDataPosition);
        synchronized (mItemViewsLock)
        {
            for (int i = 0; i < mLineNum; i++)
            {
                for (int j = 0; j < mLineItemNum; j++)
                {
                    int tempDP = startDataPosition + ((i * mLineItemNum + j) - mLineItemNum);
                    if (mDataSize <= 0)
                    {
                        mDataSize = 0;
                        tempDP = -1;
                    }
                    else if (tempDP < 0)
                    {
                        tempDP = -1;
                    }
                    else if (tempDP >= mDataSize)
                    {
                        tempDP = -1;
                    }
                    if (tempDP == -1 || mAdapter == null)
                    {
                        mItemViews[i][j].mItemView = null;
                        mItemViews[i][j].mDataposition = -1;
                    }
                    else
                    {
                        mItemViews[i][j].mDataposition = tempDP;
                        if (getBackFlag() || mbResetList)// 分页造成的重置adapter,不需要回收
                        {
                            if (mItemViews[i][j].mItemView != null)
                            {
                                mItemViews[i][j].mItemView.recyle();
                            }
                        }
                        // 分页造成的重置adapter，可视区域的中间部分可省去这个步骤，不然界面有重刷
                        if (!getBackFlag() && !mbResetList)
                        {
                            if (i > mLineNum - 2 || i == 0)
                            {
                                mItemViews[i][j].mItemView = mAdapter.getView(mContext, tempDP, mItemViews[i][j].mItemView);
                            }
                        }
                        else
                        {
                            mItemViews[i][j].mItemView = mAdapter.getView(mContext, tempDP, mItemViews[i][j].mItemView);
                        }

                        if (mItemViews[i][j].mItemView != null)
                        {
                            mItemViews[i][j].mItemView.setParentViewContainer(this);
                        }
                    }
                }
            }
        }
        mIsViewAndDataInit = true;
        focusIndexCheck();
        if (mDataSize > 0)
        {
            mFocusPosition = mItemViews[mFocusIndexPoint.y][mFocusIndexPoint.x].mDataposition;
        }
        setFocusable(mDataSize > 0);
        if (!getBackFlag() && !mbResetList)
        {
            // 啥都不用做
            Log.d("TAG", "no do");
        }
        else
        {

            Log.d("TAG", " do uiRefresh");
            uiRefresh();
        }
    }

    /**
     * 清楚所有数据
     */
    public void clearItems()
    {
        if (mItemViews != null)
        {
            synchronized (mItemViewsLock)
            {
                for (int i = 0; i < mLineNum; i++)
                {
                    for (int j = 0; j < mLineItemNum; j++)
                    {
                        if (mItemViews[i][j] != null && mItemViews[i][j].mItemView != null)
                        {
                            mItemViews[i][j].mItemView.recyle();
                            mItemViews[i][j].mItemView = null;
                        }
                    }
                }
            }
        }
    }

    private void focusIndexCheck()
    {
        // Added by c00226539屏蔽数组越界
        if (mFocusIndexPoint.y < 0 || mFocusIndexPoint.x < 0)
        {
            Log.e("AnimGridView", "Index out of boundry");
            return;
        }

        if (mItemViews[mFocusIndexPoint.y][mFocusIndexPoint.x].mDataposition == -1 && mDataSize > 0)
        {
            if (mLineItemNum == 0)
            {
                setFocus(0, 1);
            }
            else
            {
                int line = mDataSize / mLineItemNum;
                int item = mDataSize % mLineItemNum;
                if (item > 0)
                {
                    line++;
                    item--;
                }
                line = line > mTotalVisibleLineNum ? mTotalVisibleLineNum : line;
                setFocus(item, line);
            }
        }
        else
        {
            setFocus(mFocusIndexPoint.x, mFocusIndexPoint.y);
        }
    }

    private int getStartDp()
    {
        int startDP = 0;
        if (mLineItemNum > 0)
        {
            if (mFocusPosition < 0 || mFocusPosition >= mDataSize)
            {
                mFocusPosition = 0;
            }
            /* BEGIN: Modified by c00224451 for DTS2014032605313 2014/3/27 */
            int focusDataLineY = (mFocusPosition) / mLineItemNum;
            int totalDataLineY = mDataSize / mLineItemNum;
            /* END: Modified by c00224451 for DTS2014032605313 2014/3/27 */
            if (focusDataLineY + mTotalVisibleLineNum > totalDataLineY)
            {
                int startDPY = focusDataLineY - mTotalVisibleLineNum + 1;
                startDP = startDPY * mLineItemNum;
            }
            else
            {
                // 修改在多列的时候焦点会超出范围
                startDP = mFocusPosition / mLineItemNum;
                startDP = startDP * mLineItemNum;
            }

            if (mLineItemNum <= 1)
            {
                startDP--;
            }
            // end 修改在多列的时候焦点会超出范围

            if (startDP >= mDataSize || startDP < 0)
            {
                startDP = 0;
            }
            /* BEGIN: Modified by c00224451 for DTS2014032605313 2014/3/27 */
            mFocusIndexPoint.x = (mFocusPosition - startDP + mLineItemNum) % mLineItemNum;
            /* END: Modified by c00224451 for DTS2014032605313 2014/3/27 */
            mFocusIndexPoint.y = (mFocusPosition - startDP) / mLineItemNum + 1;
            if (mFocusIndexPoint.y < 1)
            {
                mFocusIndexPoint.y = 1;
            }
        }
        mStartDP = startDP;
        mItemViewMoveCount = startDP / mLineItemNum;
        return startDP;
    }

    /**
     * 直接获取此刻的数据填充的开始点
     * */
    public int getStartDPFromVar()
    {
        return getItemMoveCount() * mLineItemNum;
    }

    /**
     * 设置焦点位置
     * @param focusIndexX
     * @param focusIndexY
     */
    private boolean setFocus(int focusIndexX, int focusIndexY)
    {
        if (focusIndexX < 0 || focusIndexY < 0)
        {
            Log.e("AnimGridView", "Index out of boundry");
            return false;
        }
        if (mIsViewInited)
        {
            mFocusIndexPoint.x = focusIndexX;
            mFocusIndexPoint.y = focusIndexY;

            mFocusBounds.left = mItemCoordX[mFocusIndexPoint.x] - mFocusPadding;
            mFocusBounds.top = mItemCoordY[mFocusIndexPoint.y] - mFocusPadding;
            mFocusBounds.right = mFocusBounds.left + mFocusWidth;
            mFocusBounds.bottom = mFocusBounds.top + mFocusHeight;
            mFocusViewBounds = mFocusBounds;
        }
        return true;
    }

    /**
     * 设置用户的角度位置
     * @param focusIndexX
     * @param focusIndexY
     */
    public void setCurrentFocusIndex(int focusIndexX, int focusIndexY)
    {
        if (focusIndexX > mLineItemNum)
        {
            mFocusIndexPoint.x = mLineItemNum - 1;
        }
        else
        {
            mFocusIndexPoint.x = focusIndexX;
        }

        if (focusIndexY > mTotalVisibleLineNum)
        {
            mFocusIndexPoint.y = mTotalVisibleLineNum > 1 ? mTotalVisibleLineNum : 1;
		}
        else
        {
            mFocusIndexPoint.y = focusIndexY;
        }

        if (mIsViewAndDataInit)
        {
            focusIndexCheck();
        }
    }

    /**
     * <一句话功能简述> <功能详细描述>
     * @param dp
     * @see [类、类#方法、类#成员]
     */
    public void setCurrentFocusDataPosition(int dp)
    {
        Log.e("AnimGridView", "dp: " + dp);
        this.mFocusPosition = dp;
    }

    /**
     * 覆盖父类的postInvalidate方法，不用为外界提供多余的刷新显示接口
     */
    public void postInvalidate()
    {
        uiRefresh();
    }

    /*
     * @see @see android.view.View#invalidate()
     */
    public void invalidate()
    {
        uiRefresh();
    }

    protected void onDraw(Canvas canvas)
    {
        uiRefresh();

    }

    private void uiRefresh()
    {
        if (mDrawHandler != null)
        {
            mDrawHandler.removeMessages(DRAW_MSG);
            Message msg = this.mDrawHandler.obtainMessage(DRAW_MSG, 0, mLineNum - 1);
            mDrawHandler.sendMessage(msg);
        }
    }

    /**
     * 刷新函数，保证在界面重新载入时，数据能够及时显示
     */
    public void refresh()
    {
        for (int i = 0; i < mLineNum; i++)
        {
            for (int j = 0; j < mLineItemNum; j++)
            {
                int position = mItemViews[i][j].mDataposition;
                if (position >= 0 && position < mDataSize)
                {
                    mItemViews[i][j].mItemView = mAdapter.getView(mContext, position, mItemViews[i][j].mItemView);
                    if (mItemViews[i][j].mItemView != null)
                    {
                        mItemViews[i][j].mItemView.initSize(mItemWidth, mItemHeight);
                        // add by 143228 加此句来解决取不到专辑封面。。。。。（待查证）
                        mItemViews[i][j].mItemView.setParentViewContainer(this);
                    }
                }
                else
                {
                    mItemViews[i][j].mItemView = null;
                    mItemViews[i][j].mDataposition = -1;
                }
            }
        }
        if (mDrawHandler != null)
        {
            mDrawHandler.removeMessages(DRAW_MSG);
            Message msg = mDrawHandler.obtainMessage(DRAW_MSG, 1, mLineNum - 1);
            mDrawHandler.sendMessage(msg);
        }
    }

    /**
     * surfaceview改变，在surface创建或者转屏等都会调用这个方法
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {

    }

    /**
     * 
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        mDrawThread = new DrawThread("drawThread");
        if (mPaint == null)
        {
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
        }
        mDrawThread.start();
        mDrawHandler = new Handler(mDrawThread.getLooper(), mDrawThread);
        if (mIsViewAndDataInit)
        {
            refresh();
        }
    }

    /**
     * 
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        mDrawThread.getLooper().quit();
        mDrawThread.quit();
        mDrawThread.interrupt();
        mDrawThread = null;
        mDrawHandler = null;
        for (int i = 0; i < mLineNum; i++)
        {
            for (int j = 0; j < mLineItemNum; j++)
            {
                if (mItemViews[i][j] != null && mItemViews[i][j].mItemView != null)
                {
                    mItemViews[i][j].mItemView.recyle();
                }
            }
        }
        if (mUiHandler != null)
        {
            mUiHandler.removeMessages(VIEW_MOVE_FINISH);
        }
    }

    protected void onDetachedFromWindow()
    {
        mContext = null;
        mUiHandler = null;
        super.onDetachedFromWindow();
    }

    /**
     * 设置子项交互事件的监听器
     * @param lis
     */
    public void setOnItemStateChangedListener(OnItemStateChangedListener lis)
    {
        this.mOnItemStateChangedListener = lis;
    }

    public int getFocusPosition()
    {
        return mFocusPosition;
    }

    /**
     * 设置控件的滚动事件的监听器
     * @param scrollerListener
     */
    public void setScrollerListener(OnScrollerChangedListener scrollerListener)
    {
        this.mOnScrollerChangedListener = scrollerListener;
    }

    /**
     * 处理焦点事件，主要是为了，通知其他组件当前某个子view获取了焦点
     */
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect)
    {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        mHasFocus = gainFocus;
        postInvalidate();
        if (!mHasFocus)
        {
            return;
        }
        if (mFocusIndexPoint.x > mLineItemNum || mFocusIndexPoint.x < 0 || mFocusIndexPoint.y < 1 || mFocusIndexPoint.y > mLineItemNum - 2)
        {
            return;
        }
        if (mItemViews[mFocusIndexPoint.y][mFocusIndexPoint.x].mItemView == null
                || mItemViews[mFocusIndexPoint.y][mFocusIndexPoint.x].mDataposition == -1)
        {
            return;
        }
        int focusPosition = mItemViews[mFocusIndexPoint.y][mFocusIndexPoint.x].mDataposition;
        if (mOnItemStateChangedListener != null)
        {
            mOnItemStateChangedListener.onItemGetForcus(focusPosition);
        }
    }

    public boolean onKeyLongPress(int keyCode, KeyEvent event)
    {
        return super.onKeyLongPress(keyCode, event);
    }
    
    private boolean mLockKey = false;
    
    private static final int UNLOCK_KEY_DURATION = 500;
    public void setLockKey(boolean isLock)
    {
        synchronized (mObjectLock)
        {
            mLockKey = isLock;  
            if (isLock)
            {
                mLockKeyHandler.sendEmptyMessageDelayed(0, UNLOCK_KEY_DURATION);
            }
            else
            {
                mLockKeyHandler.removeMessages(0);
            }
        }
    }
    
    public boolean getLockKey()
    {
        synchronized (mObjectLock)
        {
            return mLockKey;
        }
    }
    
    private Handler mLockKeyHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            setLockKey(false);
        }        
    };
    
    /**
     * 处理键盘事件
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        synchronized (mObjectLock)
        {
            if (getLockKey())
            {            
                Log.d("AnimGridView", "onKeyDown： mLockKey!");
                return super.onKeyDown(keyCode, event);
            }
            setLockKey(true);
        }
        
        Log.d("AnimGridView", "onKeyDown： not lock key!");
        // 移除隐藏滚动条的消息
        if (mUiHandler != null)
        {
            mUiHandler.removeMessages(HIDDEN_SCROLLVIEW);
        }
        mScrollBarAlpha = SCROLL_BAR_ALPHA_MAX;

        mBeKeyDowning = true;

        setFocusSpeed(mItemHeight >> 2);
        mBackFlag = false;

        // 先判断是否是长按键事件
        mLongPressFlag = false;
        long nowTicket = System.currentTimeMillis();
        if (nowTicket - mKeydownTicket < TICKET_DELAY && mLastKeyCode == keyCode)
        {
            mLongPressFlag = true;
        }
        mKeydownTicket = nowTicket;
        mLastKeyCode = keyCode;
        if (mLongPressFlag)
        {
            setFocusSpeed(mItemHeight + FOCUS_SPEED_PLUS >> 1); // 暂且屏蔽掉吧
            if (getItemViewSmooth())
            {
                setItemViewSpeed(LONG_PRESS_FOCUS_SPEED);
            }
        }

        if (mDrawHandler != null && mIsViewAndDataInit)
        {
            switch (keyCode)
            {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                {
                    // 将滚动条置为可见
                    mBScrollBoundsVisible = true;

                    if (!mIsMoveing)
                    {
                        if (mFocusIndexPoint.x == 0)
                        {
                            return false;
                        }
                        mDrawHandler.sendEmptyMessage(MOVE_LEFT_MSG);
                    }

                    return false;
                }
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                {
                    // 将滚动条置为可见
                    mBScrollBoundsVisible = true;

                    if (!mIsMoveing)
                    {
                        if (mFocusIndexPoint.x == mLineItemNum - 1)
                        {
                            return false;
                        }
                        mDrawHandler.sendEmptyMessage(MOVE_RIGHT_MSG);
                    }
                    return false;
                }
                case KeyEvent.KEYCODE_DPAD_UP:
                {
                    // 将滚动条置为可见
                    mBScrollBoundsVisible = true;
                    if (!mIsMoveing)
                    {
                        if (mItemViews != null && mItemViews[mFocusIndexPoint.y][0].mDataposition == 0)
                        {
                            // 第一行无需滚动
                            return false;
                        }
                        mDrawHandler.sendEmptyMessage(MOVE_UP_MSG);
                    }
                    return false;
                }
                case KeyEvent.KEYCODE_DPAD_DOWN:
                {
                    // 将滚动条置为可见
                    mBScrollBoundsVisible = true;

                    if (!mIsMoveing)
                    {
                        if (mItemViews[mFocusIndexPoint.y][0].mDataposition + mLineItemNum >= mDataSize)
                        {
                            return false;
                        }
                        mDrawHandler.sendEmptyMessage(MOVE_DOWN_MSG);
                    }
                    return false;
                }
                case KeyEvent.KEYCODE_DPAD_CENTER:
                {
                    mScrollBarAlpha = 0;
                    mFocusStack.push(mItemViewMoveCount);

                    // added by c00226539屏蔽空指针异常
                    if (mFocusIndexPoint.x < 0 || mFocusIndexPoint.y < 0)
                    {
                        break;
                    }

                    // zkf61715 设置选中项的状态
                    int pos = mItemViews[mFocusIndexPoint.y][mFocusIndexPoint.x].mDataposition;
                    focusItemStateChange(ITEM_SELECT_MSG, pos);
                    return false;
                }
                case KeyEvent.KEYCODE_BACK:
                {
                    mScrollBarAlpha = 0;
                    if (getBackProcFlag())
                    {
                        mBackFlag = true;
                        if (mFocusStack.size() > 0)
                        {
                            mStartDP = mFocusStack.pop();
                            mItemViewMoveCount = mStartDP;
                        }
                    }
                    return false;
                }
                default:
                {
                    break;
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private static final int HIDE_SCROLLBAR_GRADE_MAX = 10;

    private static final int HIDE_SCROLLBAR_DELAY_TIME = 2000;

    private static final int HIDE_SCROLLBAR_DELAY_TIME_PLUS = 50;

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event)
    {
        mBeKeyDowning = false;
        if (mDrawHandler != null)
        {
            Log.d("GridView", "onKeyUp removeAllMessages");
            mDrawHandler.removeMessages(MOVE_LEFT_MSG);
            mDrawHandler.removeMessages(MOVE_RIGHT_MSG);
            mDrawHandler.removeMessages(MOVE_DOWN_MSG);
            mDrawHandler.removeMessages(MOVE_UP_MSG);
        }

        // 松手时，过2秒隐藏滚动条
        if (mUiHandler != null)
        {
            Message msg;
            for (int i = 0; i < HIDE_SCROLLBAR_GRADE_MAX; i++)
            {
                msg = mUiHandler.obtainMessage();
                msg.what = HIDDEN_SCROLLVIEW;
                msg.arg1 = SCROLL_BAR_ALPHA_MAX * (HIDE_SCROLLBAR_GRADE_MAX - i - 1) / HIDE_SCROLLBAR_GRADE_MAX; // 滚动条的alpha值
                mUiHandler.sendMessageDelayed(msg, HIDE_SCROLLBAR_DELAY_TIME + i * HIDE_SCROLLBAR_DELAY_TIME_PLUS);
            }
        }
        // 松手时最后一次刷新界面
        // 2014.2.13 此处为了防止每次按键抬起后延时刷新的问题
        invalidate();
        return super.onKeyUp(keyCode, event);

    }

    public boolean onTouchEvent(MotionEvent event)
    {
        mGestureDetector.onTouchEvent(event);
        return true;
    }

    /**
     * 将焦点左移。 这个动作只会触发焦点的左移，view不会左移。 可以移动的条件，焦点框未到达左边界。
     */
    private void moveFocusLeft()
    {
        // 如果焦点的x轴坐标等于0，说明在左边界了
        if (mFocusIndexPoint.x <= 0 || mIsMoveing || !mIsViewAndDataInit)
        {
            return;
        }
        int desFocusX = 0;
        desFocusX = mItemCoordX[mFocusIndexPoint.x - 1];
        mIsMoveing = true;
        while (true)
        {
            mFocusBounds.left -= mFocusSpeed;
            mFocusBounds.right = mFocusBounds.left + mFocusWidth;
            if (mFocusBounds.left > desFocusX - mFocusPadding)
            {
                // 两行不可见的view就可以不用重绘了
                draw(1, mLineNum - 2);
            }
            else
            {
                break;
            }
        }
        mIsMoveing = false;
		mFocusIndexPoint.x--;

        if (setFocus(mFocusIndexPoint.x, mFocusIndexPoint.y))
        {
            int pos = mItemViews[mFocusIndexPoint.y][mFocusIndexPoint.x].mDataposition;
            focusItemStateChange(ITEM_FOCUS_MSG, pos);
        }
        draw(1, mLineNum - 2);

    }

    /**
     * 将焦点右移。 这个只会触发焦点的右移，view不会右移。 焦点可以移动的条件是焦点框未到达右边界且右边那个view不为空
     */
    private void moveFocusRight()
    {
        // 如果焦点已经到达右边界或者焦点右边的view的数据为空，不可再移动
        if (mFocusIndexPoint.x + 1 >= mLineItemNum || mItemViews[mFocusIndexPoint.y][mFocusIndexPoint.x + 1].mItemView == null || mIsMoveing
                || !mIsViewAndDataInit)
        {
            return;
        }
        int desFocusX = 0;
        desFocusX = mItemCoordX[mFocusIndexPoint.x + 1];
        mIsMoveing = true;
        while (true)
        {
            mFocusBounds.left += mFocusSpeed;
            mFocusBounds.right = mFocusBounds.left + mFocusWidth;
            if (mFocusBounds.left < desFocusX - mFocusPadding)
            {
                draw(1, mLineNum - 2);
            }
            else
            {
                break;
            }
        }
        mIsMoveing = false;

        mFocusIndexPoint.x++;

        setFocus(mFocusIndexPoint.x, mFocusIndexPoint.y);
        draw(1, mLineNum - 2);
        int pos = mItemViews[mFocusIndexPoint.y][mFocusIndexPoint.x].mDataposition;
        focusItemStateChange(ITEM_FOCUS_MSG, pos);

    }

    /**
     * 焦点框上移。这个有可能是触发焦点框上移，也有可能触发view下移，但是不可能同时触发两个同时移动。
     * 焦点上移的条件是，如果焦点上移，焦点的边界坐标必须在可视范围内，即view在数据模型中的坐标大于1。
     * view下移的条件是，焦点不可动且焦点所在行不是数据的第一行，焦点上一行数据不为空。
     */
    private void moveFocusUp()
    {
        if (mIsMoveing || !mIsViewAndDataInit)
        {
            return;
        }
        if (mFocusIndexPoint.y == -1 || mFocusIndexPoint.y - 1 == -1)
        {
            return;
        }
        int pos = mItemViews[mFocusIndexPoint.y - 1][mFocusIndexPoint.x].mDataposition;
        // 上面判断为网格型的GridView 下面判断为List型的GridView
        if ((getItemViewSmooth() && mFocusIndexPoint.y > 1)
                || (!getItemViewSmooth() && (mFocusIndexPoint.y > 2 || (mFocusIndexPoint.y == 2 && (mItemViews[0][0].mItemView == null || mItemViews[0][0].mDataposition == -1)))))
        {
            int desFocusY = mItemCoordY[mFocusIndexPoint.y - 1];
            mIsMoveing = true;
            while (true)
            {
                mFocusBounds.top -= mFocusSpeed;
                mFocusBounds.bottom = mFocusBounds.top + mFocusHeight;
                if (mFocusBounds.top > desFocusY - mFocusPadding)
                {
                    draw(1, mLineNum - 2);
                }
                else
                {
                    break;
                }
            }
            mIsMoveing = false;
            mFocusIndexPoint.y--;
            setFocus(mFocusIndexPoint.x, mFocusIndexPoint.y);
            draw(1, mLineNum - 2);
            focusItemStateChange(ITEM_FOCUS_MSG, pos);
        }
        // view下移,如果焦点当前所在行的数据已经是第一行的数据了，就说明不可以再移动了
        else if (mItemViews[mFocusIndexPoint.y][0].mDataposition == 0)
        {
            return;
        }
        else
        {
            if (mUiHandler != null)
            {
                mUiHandler.removeMessages(SCROLL_FINISH_MSG);
            }
            moveDistanceY = 0;
            int desDistance = mItemHeight + mLinesGap;
            mIsMoveing = true;
            while (true)
            {
                moveDistanceY += getItemViewSpeed();
                if (moveDistanceY < desDistance)
                {
                    draw(0, mLineNum - 2);
                }
                else
                {
                    break;
                }
            }
            moveDistanceY = desDistance;
            draw(0, mLineNum - 2);
            if (mUiHandler != null)
            {
                Message msg = mUiHandler.obtainMessage(VIEW_MOVE_FINISH, DOWN_DIRECTION, 0);
                mUiHandler.sendMessage(msg);
                mUiHandler.removeMessages(SCROLL_FINISH_MSG);
                // 300改为50为了能及时的方法把时间缩短，有可能会导致快速的按enter键时导致焦点不正确进入不是想进的文件夹
                mUiHandler.sendEmptyMessageDelayed(SCROLL_FINISH_MSG, HIDE_SCROLLBAR_DELAY_TIME_PLUS);
                focusItemStateChange(ITEM_FOCUS_MSG, pos);
            }
        }
    }

    private void moveUp(int pos, int msgId)
    {
        if (mUiHandler != null)
        {
            mUiHandler.removeMessages(SCROLL_FINISH_MSG);
        }
        // 焦点左移且view上移
        moveDistanceY = 0;
        int desDistance = mItemHeight + mLinesGap;
        if (mItemViews[mFocusIndexPoint.y + 1][mFocusIndexPoint.x].mItemView == null
                || mItemViews[mFocusIndexPoint.y + 1][mFocusIndexPoint.x].mDataposition == -1)
        {
            int focusIndexX = 0;
            for (int i = mFocusIndexPoint.x - 1; i >= 0; i--)
            {
                if (mItemViews[mFocusIndexPoint.y + 1][i].mItemView != null && mItemViews[mFocusIndexPoint.y + 1][i].mDataposition != -1)
                {
                    focusIndexX = i;
                    break;
                }
            }
            int desFocusX = mItemCoordX[focusIndexX];
            boolean focusMoveFinish = false;
            boolean viewMoveFinish = false;
            int moveLeftSpeed = (mItemCoordX[mFocusIndexPoint.x] - desFocusX) / MOVE_SPEED_DIVISOR;
            int moveDownSpeed = desDistance / MOVE_SPEED_DIVISOR;
            mIsMoveing = true;
            while (true)
            {
                if (!focusMoveFinish)
                {
                    mFocusBounds.left -= moveLeftSpeed;
                    if (mFocusBounds.left > desFocusX - mFocusPadding)
                    {
                        mFocusBounds.right = mFocusBounds.left + mFocusWidth;
                    }
                    else
                    {
                        focusMoveFinish = true;
                        mFocusIndexPoint.x = focusIndexX;
                        setFocus(mFocusIndexPoint.x, mFocusIndexPoint.y);
                    }
                }
                if (!viewMoveFinish)
                {
                    moveDistanceY -= moveDownSpeed;
                    if (moveDistanceY <= -desDistance)
                    {
                        moveDistanceY = -desDistance;
                        viewMoveFinish = true;
                    }
                }
                draw(1, mLineNum - 1);
                if (focusMoveFinish && viewMoveFinish)
                {
                    break;
                }
            }
            if (msgId == MOVE_DOWN_MSG)
            {
                mIsMoveing = false;
                draw(1, mLineNum - 1);
            }
        }
        // 仅view上移
        else
        {
            mIsMoveing = true;
            while (true)
            {
                moveDistanceY -= getItemViewSpeed();
                if (moveDistanceY > -desDistance)
                {
                    draw(1, mLineNum - 1);
                }
                else
                {
                    break;
                }
            }
            moveDistanceY = -desDistance;
            draw(1, mLineNum - 1);
        }
        if (mUiHandler != null)
        {
            int delayTime = SCROLL_FINISH_DELAY;
            if (msgId == MOVE_DOWN_MSG)
            {
                delayTime = HIDE_SCROLLBAR_DELAY_TIME_PLUS;
            }

            Message msg = mUiHandler.obtainMessage(VIEW_MOVE_FINISH, UP_DIRECTION, 0);
            mUiHandler.sendMessage(msg);
            mUiHandler.removeMessages(SCROLL_FINISH_MSG);
            mUiHandler.sendEmptyMessageDelayed(SCROLL_FINISH_MSG, delayTime);
            focusItemStateChange(ITEM_FOCUS_MSG, pos);
        }
    }

    private static final int MOVE_SPEED_DIVISOR = 6;

    /**
     * 焦点框下移。这个有可能触发焦点下移，有可能触发view上移，也有可能同时触发焦点左移和view上移。
     * 焦点下移的条件是，焦点不在可视区域的下边界，如果焦点移动到下一行，则其坐标都必须在可视范围内。
     * view上动的条件是，下一行数据不为空，但是如果下一行数据不为空但是不全，如果焦点框正下方的的view的数据为空，
     * 则需要同时移动view和焦点框，此时view向上移动，焦点框向左移动到包含数据的view上。
     */
    private void moveFocusDown()
    {
        if (mIsMoveing || !mIsViewAndDataInit)
        {
            return;
        }
        if (mFocusIndexPoint.y == -1 || mFocusIndexPoint.y + 1 >= mItemViews.length)
        {
            return;
        }
        // 如果焦点的下一行的数据为空，则都不可以移动
        if (mItemViews[mFocusIndexPoint.y + 1][0].mItemView == null || mItemViews[mFocusIndexPoint.y + 1][0].mDataposition == -1)
        {
            return;
        }
        int pos = mItemViews[mFocusIndexPoint.y + 1][mFocusIndexPoint.x].mDataposition;
        if (pos == -1)
        {
            pos = getAdapter().getCount() - 1; // 取最后一项
        }
        // 焦点下移，这样判断焦点是否可以下移是因为可能有的子view没有完全显示。如果可以下移有两种情况，
        // 一种是可以直接下移，另外一种是当前焦点的下一行对象位置的数据为空，例如，当焦点处于倒数第二行时
        // 此时最后一行并没有被铺满，就会出现这种情况，所以，焦点不但要下移，还要左移。
        // 上面判断为网格型的GridView 下面判断为List型的GridView
        if ((mItemCoordY[mFocusIndexPoint.y + 1] + mItemHeight <= mContentBounds.bottom)
                || (!getItemViewSmooth() && ((mItemCoordY[mFocusIndexPoint.y + 1] + mItemHeight <= mContentBounds.bottom) || ((mItemViews[mLineNum - 1][0].mItemView == null || mItemViews[mLineNum - 1][0].mDataposition == -1)))))
        {
            // 此时焦点需要同时向下和向左移动
            if (mItemViews[mFocusIndexPoint.y + 1][mFocusIndexPoint.x].mItemView == null
                    || mItemViews[mFocusIndexPoint.y + 1][mFocusIndexPoint.x].mDataposition == -1)
            {
                // 需要找到最后一条数据所在位置
                int focusIndexX = 0;
                for (int i = mFocusIndexPoint.x - 1; i >= 0; i--)
                {
                    if (mItemViews[mFocusIndexPoint.y + 1][i].mItemView != null && mItemViews[mFocusIndexPoint.y + 1][i].mDataposition != -1)
                    {
                        focusIndexX = i;
                        break;
                    }
                }
                int desFocusX = mItemCoordX[focusIndexX];
                int desFocusY = mItemCoordY[mFocusIndexPoint.y + 1];
                boolean focusXMoveFinish = false;
                boolean focusYMoveFinish = false;
                int moveLeftSpeed = (mItemCoordX[mFocusIndexPoint.x] - desFocusX) / MOVE_SPEED_DIVISOR;
                int moveDownSpeed = (desFocusY - mItemCoordY[mFocusIndexPoint.y]) / MOVE_SPEED_DIVISOR;
                mIsMoveing = true;
                while (true)
                {
                    if (!focusXMoveFinish)
                    {
                        mFocusBounds.left -= moveLeftSpeed;
                        if (mFocusBounds.left > desFocusX - mFocusPadding)
                        {
                            mFocusBounds.right = mFocusBounds.left + mFocusWidth;
                        }
                        else
                        {
                            mFocusBounds.left = desFocusX - mFocusPadding;
                            mFocusBounds.right = mFocusBounds.left + mFocusWidth;
                            focusXMoveFinish = true;
                            mFocusIndexPoint.x = focusIndexX;
                        }

                    }
                    if (!focusYMoveFinish)
                    {
                        mFocusBounds.top += moveDownSpeed;
                        if (mFocusBounds.top < desFocusY - mFocusPadding)
                        {
                            mFocusBounds.bottom = mFocusBounds.top + mFocusHeight;
                        }
                        else
                        {
                            mFocusBounds.top = desFocusY - mFocusPadding;
                            mFocusBounds.bottom = mFocusBounds.top + mFocusHeight;
                            focusYMoveFinish = true;
                            mFocusIndexPoint.y++;
                        }
                    }
                    draw(1, mLineNum - 2);
                    if (focusXMoveFinish && focusYMoveFinish)
                    {
                        break;
                    }

                }
                mIsMoveing = false;
                draw(1, mLineNum - 2);
            }
            // 直接把焦点向下移动
            else
            {
                int desFocusY = mItemCoordY[mFocusIndexPoint.y + 1];
                mIsMoveing = true;
                while (true)
                {
                    mFocusBounds.top += mFocusSpeed;
                    mFocusBounds.bottom = mFocusBounds.top + mFocusHeight;
                    if (mFocusBounds.top < desFocusY - mFocusPadding)
                    {
                        draw(1, mLineNum - 2);
                    }
                    else
                    {
                        break;
                    }
                }
                mIsMoveing = false;
                if (mFocusIndexPoint.y < mTotalVisibleLineNum + 1)
                {
                    mFocusIndexPoint.y++;
                }
                setFocus(mFocusIndexPoint.x, mFocusIndexPoint.y);
                draw(1, mLineNum - 2);
                focusItemStateChange(ITEM_FOCUS_MSG, pos);
            }
        }
        // view上移，此时同样会出现两种情况，一种是view直接上移，另外一种是view上移，焦点左移
        else
        {
            moveUp(pos, MOVE_DOWN_MSG);
        }
    }

    /**
     * View 向上移动
     * */
    private void viewMoveUp(int moveCount)
    {

        for (int count = 0; count < moveCount && !mIsMoveing; count++)
        {
            mBScrollBoundsVisible = true;

            // 如果下一行的数据为空，则都不可以移动
            if (mItemViews[getLineNum() - 1][0].mItemView == null || mItemViews[getLineNum() - 1][0].mDataposition == -1)
            {
                return;
            }

            int pos = mItemViews[mFocusIndexPoint.y + 1][mFocusIndexPoint.x].mDataposition;
            if (pos == -1)
            {
                pos = getAdapter().getCount() - 1; // 取最后一项
            }

            moveUp(pos, VIEW_UP_MSG);
            delayD(SLEEP_DELAY);
        }
    }

    private static final int SCROLL_FINISH_DELAY = 300;

    private static final int SLEEP_DELAY = 50;

    /**
     * View 向下移动
     * */
    private void viewMoveDown(int moveCount)
    {

        for (int count = 0; count < moveCount && !mIsMoveing; count++)
        {

            mBScrollBoundsVisible = true;
            // view下移,如果已经是第一行的数据了，就说明不可以再移动了
            if (mItemViews[0][0].mItemView == null || mItemViews[0][0].mDataposition == -1)
            {
                return;
            }

            int pos = mItemViews[mFocusIndexPoint.y - 1][mFocusIndexPoint.x].mDataposition;
            if (mUiHandler != null)
            {
                mUiHandler.removeMessages(SCROLL_FINISH_MSG);
            }
            moveDistanceY = 0;
            int desDistance = mItemHeight + mLinesGap;
            mIsMoveing = true;
            while (true)
            {
                moveDistanceY += getItemViewSpeed();
                if (moveDistanceY < desDistance)
                {
                    draw(0, mLineNum - 2);
                }
                else
                {
                    break;
                }
            }
            moveDistanceY = desDistance;
            draw(0, mLineNum - 2);
            if (mUiHandler != null)
            {
                Message msg = mUiHandler.obtainMessage(VIEW_MOVE_FINISH, DOWN_DIRECTION, 0);
                mUiHandler.sendMessage(msg);
                mUiHandler.removeMessages(SCROLL_FINISH_MSG);
                mUiHandler.sendEmptyMessageDelayed(SCROLL_FINISH_MSG, SCROLL_FINISH_DELAY);
                focusItemStateChange(ITEM_FOCUS_MSG, pos);
            }
            delayD(SLEEP_DELAY);
        }
    }

    /**
     * view移动完成后的逻辑处理，主要完成数据的填充
     * @param direction
     */
    private void viewMoveFinish(int direction)
    {
        // long startTime = System.currentTimeMillis();
        if (mIsViewAndDataInit)
        {
            moveDistanceY = 0;

            if (direction == UP_DIRECTION)
            {
                mItemViewMoveCount++;
                mStartDP++;
                ItemView[] tempItemViews = mItemViews[0];
                System.arraycopy(mItemViews, 1, mItemViews, 0, mLineNum - 1);
                for (int i = 0; i < mLineItemNum; i++)
                {
                    tempItemViews[i].mDataposition = -1;
                    if (tempItemViews[i].mItemView != null)
                    {
                        tempItemViews[i].mItemView.recyle();
                    }
                    int lastDataPosition = mItemViews[mLineNum - 1][mLineItemNum - 1].mDataposition;
                    if (lastDataPosition == -1 || mItemViews[mLineNum - 1][mLineItemNum - 1].mItemView == null)
                    {
                        tempItemViews[i].mDataposition = -1;
                        tempItemViews[i].mItemView = null;
                    }
                    else
                    {
                        int nextDataposition = lastDataPosition + 1 + i;
                        if (nextDataposition < mDataSize)
                        {
                            tempItemViews[i].mDataposition = nextDataposition;
                            tempItemViews[i].mItemView = mAdapter.getView(mContext, nextDataposition, tempItemViews[i].mItemView);

                            // add by zengxiaowen
                            // 重新注册刷新回调,原因：itemView已经重用了，得重新注册
                            tempItemViews[i].mItemView.setParentViewContainer(this);
                        }
                        else
                        {
                            tempItemViews[i].mDataposition = -1;
                            tempItemViews[i].mItemView = null;
                        }
                    }
                }
                mItemViews[mLineNum - 1] = tempItemViews;
            }
            else if (direction == DOWN_DIRECTION)
            {
                mItemViewMoveCount--;
                mStartDP--;
                ItemView[] tempItemViews = mItemViews[mLineNum - 1];
                System.arraycopy(mItemViews, 0, mItemViews, 1, mLineNum - 1);
                for (int i = 0; i < mLineItemNum; i++)
                {
                    tempItemViews[i].mDataposition = -1;
                    if (tempItemViews[i].mItemView != null)
                    {
                        tempItemViews[i].mItemView.recyle();
                    }
                    int nextDataposition = mItemViews[0][0].mDataposition - (mLineItemNum - i);
                    if (nextDataposition >= 0)
                    {
                        tempItemViews[i].mDataposition = nextDataposition;
                        tempItemViews[i].mItemView = mAdapter.getView(mContext, nextDataposition, tempItemViews[i].mItemView);

                        // add by zengxiaowen 重新注册刷新回调,原因：itemView已经重用了，得重新注册
                        tempItemViews[i].mItemView.setParentViewContainer(this);
                    }
                    else
                    {
                        tempItemViews[i].mDataposition = -1;
                        tempItemViews[i].mItemView = null;
                    }
                }
                mItemViews[0] = tempItemViews;
            }
            mIsMoveing = false;
        }
        else
        {
            // 如果数据和view没有准备好，那么过100毫秒再来一次，否则移动将不能再响应。
            Message msg = mUiHandler.obtainMessage(VIEW_MOVE_FINISH, direction, 0);
            mUiHandler.sendMessageDelayed(msg, 100);
        }
    }

    private void delayD(int iDelay)
    {
        try
        {
            Thread.sleep(iDelay);
        }
        catch (InterruptedException e)
        {
        }
    }

    /**
     * 当发生滚动后，当前显示的数据范围发生了变化，通知需要监听此事件的监听器去处理
     * @param startPosition
     * @param endPosition
     * @param totalSize
     */
    private void notifyCurrentPositionChange(int startPosition, int endPosition, int totalSize)
    {
        if (mOnScrollerChangedListener != null)
        {
            mOnScrollerChangedListener.onListScrolll(startPosition, endPosition, totalSize);
        }
    }

    /**
     * 焦点改变逻辑处理
     */
    // modify by zwx143228 传参的方式解决掉getFocus和Select项不一致问题
    private void focusItemStateChange(int statemsg, int pos)
    {
        mFocusPosition = mItemViews[mFocusIndexPoint.y][mFocusIndexPoint.x].mDataposition;
        if (mFocusPosition != -1 && mUiHandler != null)
        {
            if (pos < 0)
            {
                return;
            }
            Message msg = mUiHandler.obtainMessage(statemsg, pos, 0);
            mUiHandler.sendMessage(msg);
        }
    }

    private static final int DRAW_RECT_LEFT = 0;

    private static final int DRAW_RECT_TOP = 430;

    private static final int DRAW_RECT_RIGHT = 1280;

    private static final int DRAW_RECT_BOTTOM = 650;

    private static final int TRANSLATE_DIVISOR = 12;

    private static final int TRANSLATE_WIDTH_SUBTRAHEND = 30;

    private static final int HEIGHT_PLUS_MAX = 20;

    private static final int RECT_TOP_PLUS = 5;

    private static final int RECT_RIGHT = 3;

    /**
     * 绘图，从startLine绘制到endLine
     * @param startLine
     * @param endLine
     */
    private void draw(int startLine, int endLine)
    {
        Canvas canvas = mHolder.lockCanvas();
        int listLength = 0;
        boolean drawItem = false;
        if (canvas == null)
        {
            Log.e(VIEW_LOG_TAG, "cc msg draw canvas == null!");
            return;
        }

        if (getAdapter() != null)
        {
            listLength = getAdapter().getCount();
        }
        else
        {
            Log.e(VIEW_LOG_TAG, "cc msg draw getAdapter() == null!");
            mHolder.unlockCanvasAndPost(canvas);
            return;
        }
        if (canvas != null)
        {
            /**
             * modify zwx143228 由于图片浏览和其他的浏览设置SurfaceView的层级不同（见onLayout部分），
             * 需要用不同的渲染模式来解决掉界面上的一些由于层级的关系导致的小问题
             * */
            if (getImageMarquee())
            {
                canvas.drawColor(Color.BLACK, Mode.SRC);
            }
            else
            {
                canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
            }
            Rect rc = new Rect(DRAW_RECT_LEFT, DRAW_RECT_TOP, DRAW_RECT_RIGHT, DRAW_RECT_BOTTOM);
            if (getImageMarquee())
            {
                canvas.save();
                mGridViewBackGround.setBounds(rc);
                mGridViewBackGround.draw(canvas);
                canvas.restore();
            }

            canvas.save();
            canvas.translate(mPadding.left, mPadding.top);
            canvas.clipRect(mContentBounds);
            {
                canvas.save();
                canvas.translate(0, moveDistanceY);
                synchronized (mItemViewsLock)
                {
                    for (int i = startLine; i <= endLine; i++)
                    {

                        canvas.save();
                        canvas.translate(0, mItemCoordY[i]);
                        canvas.clipRect(mLineBounds);
                        for (int j = 0; j < mLineItemNum; j++)
                        {
                            canvas.save();
                            canvas.translate(mItemCoordX[j], 0);
                            canvas.clipRect(mItemBounds);
                            if (mItemViews != null && mItemViews[i][j].mItemView != null && listLength > 0)
                            {
                                boolean isLast = false;
                                if ((mItemViews[i][j].mDataposition + mLineItemNum >= listLength && ((mItemViews[i][j].mDataposition) % mLineItemNum == 0))
                                        || mItemViews[i][j].mDataposition + mLineItemNum > listLength)
                                {
                                    isLast = true;
                                }

                                mItemViews[i][j].mItemView.draw(isLast, canvas, mHasFocus, mPaint);
                                drawItem = true; // 标志已经画了ItemView
                            }
                            else if (mItemDefaultDrawable != null)
                            {
                                Log.d("TAG", "draw nnnnn itemDefaultDrawable");
                                mItemDefaultDrawable.draw(canvas);
                            }
                            canvas.restore();
                        }
                        canvas.restore();
                    }
                }
                canvas.restore();
            }
            canvas.restore();

            // 绘制焦点框
            canvas.save();
            // modified by keke 主要修改在方格显示焦点变大功能2013.10.31

            if (mHasFocusBig)
            {
                if (mLineItemNum > 1)
                {
                    canvas.translate(mPadding.left + mFixLeftOfFocus - mFocusWidth / TRANSLATE_DIVISOR, mPadding.top + mFixTopOfFocus - mFocusHeight
                            / TRANSLATE_DIVISOR);
                }
                else
                {
                    canvas.translate(mPadding.left + mFixLeftOfFocus, mPadding.top + mFixTopOfFocus);
                }
            }
            else
            {
                canvas.translate(mPadding.left + mFixLeftOfFocus, mPadding.top + mFixTopOfFocus);
            }
            if (mHasFocus && drawItem && mFocusDrawable != null && mDataSize > 0)
            {
                if (mHasFocusBig)
                {

                    focusChangeBig(canvas);
                }
                else
                {
                    mFocusDrawable.setBounds(mFocusBounds);
                    mFocusDrawable.draw(canvas);
                }
            }
            canvas.restore();

            int countVisibleLine = 0;
            if (mTotalVisibleLineNum * (mItemHeight + mLinesGap) < mContentBounds.bottom)
            {
                countVisibleLine = mTotalVisibleLineNum + 1;
            }
            else
            {
                countVisibleLine = mTotalVisibleLineNum;
            }
            // 初始化下拉条
            if (mDataSize > countVisibleLine * mLineItemNum && mHasScrollView && mBScrollBoundsVisible)
            {
                canvas.save();

                canvas.translate(mViewWidth - TRANSLATE_WIDTH_SUBTRAHEND, mPadding.top);
                // 1改为0for修改下拉条不能到头的bug 20131106
                int startPosition = 0;
                if (null != mItemViews)
                {
                    startPosition = mItemViews[0][0].mDataposition / mLineItemNum;
                }

                float countheight = mContentBounds.bottom - mLinesGap;

                int countLineNum = 0;
                if (mDataSize % mLineItemNum == 0)
                {
                    countLineNum = mDataSize / mLineItemNum;
                }
                else
                {
                    countLineNum = mDataSize / mLineItemNum + 1;
                }
                float height = (float) countVisibleLine / countLineNum * countheight;
                if (height < HEIGHT_PLUS_MAX)
                {
                    height = HEIGHT_PLUS_MAX;
                }

                float startheight = (float) startPosition / countLineNum * countheight;

                if (startheight > (mContentBounds.bottom - mLinesGap - height))
                {
                    startheight = mContentBounds.bottom - mLinesGap - height;
                }

                canvas.clipRect(0, startheight + RECT_TOP_PLUS, RECT_RIGHT, startheight + height);

                if (mScrollDrawable == null)
                {
                    mScrollDrawable = mContext.getResources().getDrawable(R.drawable.scrollbar_thumb);
                }
                canvas.drawColor(Color.TRANSPARENT);
                mScrollBounds.top = 0;
                mScrollBounds.left = 0;
                mScrollBounds.right = RECT_RIGHT;
                mScrollBounds.bottom = mContentBounds.bottom - mLinesGap;
                mScrollDrawable.setBounds(mScrollBounds);
                mScrollDrawable.setAlpha(mScrollBarAlpha);
                mScrollDrawable.draw(canvas);

                canvas.restore();
            }

        }
        mHolder.unlockCanvasAndPost(canvas);
    }

    private boolean mBFocusChangeBig;

    private static final int FOCUS_BIG_SHADOW_OFFSET = 50;

    private static final int FOCUS_BIG_OFFSET = 20;

    /**
     * 当图片得到焦点的时候放大显示 <功能详细描述>
     * @param canvas
     * @see [类、类#方法、类#成员]
     */
    private void focusChangeBig(Canvas canvas)
    {
        // added by c00226539数组越界
        if (mFocusIndexPoint.y < 0 || mFocusIndexPoint.x < 0)
        {
            return;
        }

        ItemView view = mItemViews[mFocusIndexPoint.y][mFocusIndexPoint.x];
        if (!mBeKeyDowning && !mIsMoveing)
        {
            // true:需要放大
            if (view.mItemView.showBig())
            {
                mBFocusChangeBig = true;
                // 阴影效果
                if (mFocuShadowDrawable == null)
                {
                    mFocuShadowDrawable = mContext.getResources().getDrawable(R.drawable.image_focus_shadow);
                }

                mFocuShadowDrawable.setBounds(mFocusViewBounds.left - FOCUS_BIG_SHADOW_OFFSET, mFocusViewBounds.top - FOCUS_BIG_SHADOW_OFFSET,
                        mFocusViewBounds.right + FOCUS_BIG_SHADOW_OFFSET, mFocusViewBounds.bottom + FOCUS_BIG_SHADOW_OFFSET);
                mFocuShadowDrawable.draw(canvas);

                // 获取焦点处图片
                BitmapDrawable bit = new BitmapDrawable(mContext.getResources(), view.mItemView.getDrawBitmap());

                // 放大焦点处图片，并放大显示
                if (bit != null && bit.getBitmap() != null && !bit.getBitmap().isRecycled())
                {
                    bit.setBounds(mFocusViewBounds.left + FOCUS_BIG_OFFSET, mFocusViewBounds.top + FOCUS_BIG_OFFSET, mFocusViewBounds.right
                            - FOCUS_BIG_OFFSET, mFocusViewBounds.bottom - FOCUS_BIG_OFFSET);
                    bit.draw(canvas);

                }
            }
        }
        if (mBFocusChangeBig)
        {
            // 绘制放大的焦点框，放大的焦点大小由setFocusChangeBig(boolean changeBig)方法设置
            mFocusDrawable.setBounds(mFocusBounds);
            mBFocusChangeBig = false;
        }
        // 移动过程中，没有放大的焦点框
        else
        {
            mFocusDrawable.setBounds(mFocusBounds.left + mFocusWidth / TRANSLATE_DIVISOR, mFocusBounds.top + mFocusHeight / TRANSLATE_DIVISOR,
                    mFocusBounds.right - mFocusWidth / TRANSLATE_DIVISOR, mFocusBounds.bottom - mFocusHeight / TRANSLATE_DIVISOR);
        }
        mFocusDrawable.draw(canvas);
    }

    /**
     * @author yKF76250 每一个数学模型中保持的 子项，包含了子项的位置以及子项的视图
     */
    private class ItemView
    {
        /**
         * 这个view中的数据位置
         */
        private int mDataposition;

        /**
         * 子View
         */
        private HWListItemView mItemView;
    }

    /**
     * @author yKF76250 绘图线程，主要处理包括移动动画在内的所有和绘图有关的消息
     */
    private class DrawThread extends HandlerThread implements Handler.Callback
    {

        public DrawThread(String name)
        {
            super(name);
        }

        @Override
        public boolean handleMessage(Message msg)
        {
            Message message;
            switch (msg.what)
            {

                case DRAW_MSG:
                    draw(msg.arg1, msg.arg2);
                    break;
                case MOVE_LEFT_MSG:
                    message = mUiHandler.obtainMessage(START_MOVEING, 0, 0);
                    mUiHandler.sendMessage(message);
                    moveFocusLeft();
                    break;
                case MOVE_RIGHT_MSG:
                    message = mUiHandler.obtainMessage(START_MOVEING, 0, 0);
                    mUiHandler.sendMessage(message);
                    moveFocusRight();

                    break;
                case MOVE_UP_MSG:
                    message = mUiHandler.obtainMessage(START_MOVEING, 0, 0);
                    mUiHandler.sendMessage(message);
                    moveFocusUp();
                    break;
                case MOVE_DOWN_MSG:
                    message = mUiHandler.obtainMessage(START_MOVEING, 0, 0);
                    mUiHandler.sendMessage(message);
                    moveFocusDown();
                    break;
                case VIEW_UP_MSG:
                    message = mUiHandler.obtainMessage(START_MOVEING, 0, 0);
                    mUiHandler.sendMessage(message);
                    viewMoveUp(msg.arg1);
                    break;
                case VIEW_DOWN_MSG:
                    message = mUiHandler.obtainMessage(START_MOVEING, 0, 0);
                    mUiHandler.sendMessage(message);
                    viewMoveDown(msg.arg1);
                    break;
                default:
                    break;
            }

            return true;
        }
    }

    /**
     * UI线程的handler，用来处理界面显示有关的消息
     */
    private Handler mUiHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case SCROLL_FINISH_MSG:
                    int startPosition = mItemViews[1][0].mDataposition;
                    startPosition = startPosition == -1 ? 0 : startPosition;
                    int endPosition = startPosition + mLineNum * mLineItemNum;
                    endPosition = endPosition > mDataSize ? mDataSize : endPosition;
                    if (endPosition > startPosition)
                    {
                        notifyCurrentPositionChange(startPosition, endPosition, mDataSize);
                    }
                    break;
                case ITEM_FOCUS_MSG:
                    if (mOnItemStateChangedListener != null)
                    {
                        mOnItemStateChangedListener.onItemGetForcus(msg.arg1);
                    }
                    break;
                case ITEM_SELECT_MSG:
                    if (mOnItemStateChangedListener != null)
                    {
                        mOnItemStateChangedListener.onItemSelected(msg.arg1);
                    }
                    break;
                case VIEW_MOVE_FINISH:
                    viewMoveFinish(msg.arg1);
                    break;
                case START_MOVEING:
                    if (mOnItemStateChangedListener != null)
                    {
                        mOnItemStateChangedListener.onUIChange();
                    }
                    break;
                case HIDDEN_SCROLLVIEW:
                    mScrollBarAlpha = msg.arg1;
                    invalidate();
                    break;
                default:
                    break;
            }
        }
    };

    // ------------------以下为手势事件---------------------
    @Override
    public boolean onDown(MotionEvent e)
    {
        return false;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent event)
    {

        int pointy = (int) event.getY() - mPadding.top;
        for (int i = 1; i < mLineNum - 1; i++)
        {
            if (pointy > mItemCoordY[i] && pointy <= (mItemCoordY[i] + mItemHeight))
            {
                int pointx = (int) event.getX();
                for (int j = 0; j < mLineItemNum; j++)
                {
                    if (pointx > mItemCoordX[j] && pointx < mItemCoordX[j] + mItemWidth)
                    {
                        if (mOnItemStateChangedListener != null && mItemViews[i][j].mItemView != null && mItemViews[i][j].mDataposition != -1)
                        {
                            mOnItemStateChangedListener.onItemSelected(mItemViews[i][j].mDataposition);
                            return true;
                        }
                    }
                }
                break;
            }
        }
        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
    {
        float distanceY = e2.getRawY() - e1.getRawY();
        int moveLine = Math.abs((int) distanceY / (mItemHeight >> 2));
        moveLine = moveLine < 0 ? 1 : moveLine;

        if (distanceY < 0)
        {
            Message msg = new Message();
            msg.what = VIEW_UP_MSG;
            msg.arg1 = moveLine;
            clearViewMoveMsg();
            if (mDrawHandler != null)
            {
                mDrawHandler.sendMessage(msg);
            }
        }
        else if (distanceY > 0)
        {
            Message msg = new Message();
            msg.what = VIEW_DOWN_MSG;
            msg.arg1 = moveLine;
            clearViewMoveMsg();
            if (mDrawHandler != null)
            {
                mDrawHandler.sendMessage(msg);
            }
        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e)
    {

    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
    {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e)
    {

    }

    public int getFocusY()
    {
        return mFocusIndexPoint.y;
    }

    public int[] getFocusPositionOnScreen()
    {
        int[] pos = new int[2];
        pos[0] = mFocusBounds.left;
        pos[1] = mFocusBounds.top;
        return pos;
    }

    private int[] getFocusPos()
    {
        int[] pos = new int[2];
        pos[0] = mFocusIndexPoint.x;
        pos[1] = mFocusIndexPoint.y;
        return pos;
    }

    public void setFixFocusPos(int l, int t, int w, int h)
    {
        mFixLeftOfFocus = l;
        mFixTopOfFocus = t;
        mFixWidthOfFocus = w;
        mFixHeightOfFocus = h;
    }

    public void setFocusSpeed(int speed)
    {
        if (speed > 0)
        {
            this.mFocusSpeed = speed;
        }
    }

    public void setItemViewSpeed(int speed)
    {
        mItemViewSpeed = speed;
    }

    public int getItemViewSpeed()
    {
        return mItemViewSpeed;
    }

    public void setItemViewSmooth(boolean smooth)
    {
        this.mItemViewSmoothMove = smooth;
    }

    public boolean getItemViewSmooth()
    {
        return mItemViewSmoothMove;
    }

    public boolean getImageMarquee()
    {
        return mIsImageMarquee;
    }

    public void setImageMarquee(boolean mIsImageMarquee)
    {
        this.mIsImageMarquee = mIsImageMarquee;
    }

    public boolean getMoveFlag()
    {
        return mIsMoveing;
    }

    public void setBackFromPlayerFlag(boolean isBackFromPlayer)
    {
        mBackFromPlayer = isBackFromPlayer;
    }

    /**
     * 整个ItemView上下移动的计数器
     * */
    public int getItemMoveCount()
    {
        return mItemViewMoveCount;
    }

    /**
     * 放出接口供应用可以指定数据填充起始点
     * */
    public void setItemMoveCount(int num)
    {
        mItemViewMoveCount = num;
    }

    /**
     * 获取一行的子View数
     * */
    public int getItemLine()
    {
        return mLineItemNum;
    }

    public int getLineNum()
    {
        return mLineNum;
    }

    // 只供外界设置
    public void setResetAdapterFlag(boolean isReset)
    {
        mbResetList = isReset;
    }

    /**
     * 设置back标志
     * */
    public void setBackFlag(boolean isBack)
    {
        mBackFlag = isBack;
    }

    public boolean getBackFlag()
    {
        return mBackFlag;
    }

    private boolean mbIsBackProc = true;

    public void setBackProcFlag(boolean isBackProc)
    {
        mbIsBackProc = isBackProc;
    }

    public boolean getBackProcFlag()
    {
        return mbIsBackProc;
    }

    /**
     * 刷新绘制消息
     */
    private static final int DRAW_MSG = 0x0001;

    /**
     * 左移消息
     */
    private static final int MOVE_LEFT_MSG = 0x0002;

    /**
     * 右移消息
     */
    private static final int MOVE_RIGHT_MSG = 0x0003;

    /**
     * 上移消息
     */
    private static final int MOVE_UP_MSG = 0x0004;

    /**
     * 下移消息
     */
    private static final int MOVE_DOWN_MSG = 0x0005;

    /**
     * 移动完成消息，这个消息用来view发生了滚动，如果连续滚动，这个消息不会发出。
     */
    private static final int SCROLL_FINISH_MSG = 0x0006;

    /**
     * 子项获取焦点消息
     */
    private static final int ITEM_FOCUS_MSG = 0x0007;

    /**
     * 子项被选中消息
     */
    private static final int ITEM_SELECT_MSG = 0x0008;

    /**
     * view移动完成消息，这个消息用来指示需要进行数据填充
     */
    private static final int VIEW_MOVE_FINISH = 0x0009;

    /**
     * 开始移动消息
     * */
    private static final int START_MOVEING = 0x00010;

    /**
     * 隐藏滚动条ScrollView
     * */
    private static final int HIDDEN_SCROLLVIEW = 0x00011;

    /**
     * 无方向
     * */
//    private static final int NOT_DIRECTION = 0;

    /**
     * 上
     */
    private static final int UP_DIRECTION = -1;

    /**
     * 下
     */
    private static final int DOWN_DIRECTION = 1;

    /**
     * 上移消息
     */
    private static final int VIEW_UP_MSG = 0x0011;

    /**
     * 下移消息
     */
    private static final int VIEW_DOWN_MSG = 0x0012;

    /**
     * 长按键判断标准
     * */
    protected static final int TICKET_DELAY = 40;
}
