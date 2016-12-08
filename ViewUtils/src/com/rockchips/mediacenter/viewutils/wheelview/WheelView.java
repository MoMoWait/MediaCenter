/*
 *  Android Wheel Control.
 *  https://code.google.com/p/android-wheel/
 *  
 *  Copyright 2011 Yuri Kanivets
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.rockchips.mediacenter.viewutils.wheelview;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;

import com.rockchips.mediacenter.viewutils.R;

/**
 * Numeric wheel view.
 * 
 * @author
 */
public class WheelView extends View
{

    private static final String TAG = "WheelView";

    /**
     * 子项焦点改变事件接口
     */
    public interface OnItemFocusChangedListener
    {
        /**
         * 子项焦点改变事件回调函数
         * 
         * @param view
         * @param position
         * @param hasFocus
         */
        void onFocusChange(View view, int position, boolean hasFocus);
    }

    /** 头部和底部阴影颜色 */
    private static final int[] SHADOWS_COLORS = new int[]
    { 0xFF111111, 0x00AAAAAA, 0x00AAAAAA };

    /** 头部和底部偏移量 (to hide that) */
    private static final int ITEM_OFFSET_PERCENT = 20;

    /** 左右间距 */
    private static final int PADDING = 10;

    /** 默认可见子项数目 */
    private static final int DEF_VISIBLE_ITEMS = 5;

    // 当前滚轮项
    private int mCurrentItem;

    // 可视子项数目
    private int mVisibleItems = DEF_VISIBLE_ITEMS;

    // 列表子项高度
    private int mItemHeight;

    // 中间焦点框
    private Drawable mCenterDrawable;

    // 头部和底部渐隐
    private GradientDrawable mTopShadow;

    private GradientDrawable mBbottomShadow;

    // 滚动
    private WheelScroller mScroller;

    private boolean mIsScrollingPerformed;

    private int mScrollingOffset;

    // 是否循环
    private boolean mIsCyclic;

    // 子项布局
    private LinearLayout mIitemsLayout;

    // 布局中显示第一项的索引
    private int mFirstItem;

    // 视图适配器
    private WheelViewAdapter mViewAdapter;

    // 回收
    private WheelRecycle mRrecycle = new WheelRecycle(this);

    // 监听器
    private List<OnWheelChangedListener> mChangingListeners = new LinkedList<OnWheelChangedListener>();

    private List<OnWheelScrollListener> mScrollingListeners = new LinkedList<OnWheelScrollListener>();

    private List<OnWheelClickedListener> mClickingListeners = new LinkedList<OnWheelClickedListener>();

    private OnItemFocusChangedListener mOnItemFocusChangedListener;

    /**
     * Constructor
     */
    public WheelView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        initData(context);
    }

    /**
     * Constructor
     */
    public WheelView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initData(context);
    }

    /**
     * Constructor
     */
    public WheelView(Context context)
    {
        super(context);
        initData(context);
    }

    /**
     * Initializes class data
     * @param context the context
     */
    private void initData(Context context)
    {
        Log.d(TAG, "----------------->initData");
        mIsCyclic = false;
        mScroller = new WheelScroller(getContext(), mScrollingListener);
    }

    private static final int HEIGHT_OFFSET = 20;

    // Scrolling listener
    private WheelScroller.ScrollingListener mScrollingListener = new WheelScroller.ScrollingListener()
    {
        public void onStarted()
        {
            if (mOnItemFocusChangedListener != null)
            {
                Log.d("xxxx", "onStarted()---->currentItem=" + getCurrentItem());
                mOnItemFocusChangedListener.onFocusChange(mIitemsLayout.getChildAt(getCurrentItem() - mFirstItem), getCurrentItem(), false);
            }
            mIsScrollingPerformed = true;
            notifyScrollingListenersAboutStart();
        }

        public void onScroll(int distance)
        {
            doScroll(distance);

            int height = getHeight();
            if (mScrollingOffset > height + HEIGHT_OFFSET)
            {
                mScrollingOffset = height;
                mScroller.stopScrolling();
            }
            else if (mScrollingOffset < -height - HEIGHT_OFFSET)
            {
                mScrollingOffset = -height;
                mScroller.stopScrolling();
            }
        }

        public void onFinished()
        {
            if (mIsScrollingPerformed)
            {
                notifyScrollingListenersAboutEnd();
                mIsScrollingPerformed = false;
            }

            mScrollingOffset = 0;
            invalidate();

            if (mOnItemFocusChangedListener != null)
            {
                Log.d("xxxx", "onFinished()---->currentItem=" + getCurrentItem());
                mOnItemFocusChangedListener.onFocusChange(mIitemsLayout.getChildAt(getCurrentItem() - mFirstItem), getCurrentItem(), true);
            }
        }

        public void onJustify()
        {
            if (Math.abs(mScrollingOffset) > WheelScroller.MIN_DELTA_FOR_SCROLLING)
            {
                mScroller.scroll(mScrollingOffset, 0);
            }
        }
    };

    /**
     * 设置指定的滚动插补器
     * @param interpolator the interpolator
     */
    public void setInterpolator(Interpolator interpolator)
    {
        mScroller.setInterpolator(interpolator);
    }

    /**
     * 获取可见项的数目
     * 
     * @return the count of visible items
     */
    public int getVisibleItems()
    {
        return mVisibleItems;
    }

    /**
     * 设置自己需要的可见项数目 Actual amount of visible items depends on wheel layout
     * parameters. To apply changes and rebuild view call measure().
     * 
     * @param count the desired count for visible items
     */
    public void setVisibleItems(int count)
    {
        mVisibleItems = count;
    }

    /**
     * 获取视图适配器
     * @return the view adapter
     */
    public WheelViewAdapter getViewAdapter()
    {
        return mViewAdapter;
    }

    // 适配器监听
    private DataSetObserver mDataObserver = new DataSetObserver()
    {
        @Override
        public void onChanged()
        {
            invalidateWheel(false);
        }

        @Override
        public void onInvalidated()
        {
            invalidateWheel(true);
        }
    };

    /**
     * 设置视图适配器. 通常新的适配器包含多个视图, 所以 需要重建视图 调用 measure()方法.
     * 
     * @param viewAdapter the view adapter
     */
    public void setViewAdapter(WheelViewAdapter viewAdapter)
    {
        if (this.mViewAdapter != null)
        {
            this.mViewAdapter.unregisterDataSetObserver(mDataObserver);
        }
        this.mViewAdapter = viewAdapter;
        if (this.mViewAdapter != null)
        {
            this.mViewAdapter.registerDataSetObserver(mDataObserver);
        }

        invalidateWheel(true);
    }

    /**
     * Adds wheel changing listener
     * @param listener the listener
     */
    public void addChangingListener(OnWheelChangedListener listener)
    {
        mChangingListeners.add(listener);
    }

    /**
     * Removes wheel changing listener
     * @param listener the listener
     */
    public void removeChangingListener(OnWheelChangedListener listener)
    {
        mChangingListeners.remove(listener);
    }

    /**
     * Notifies changing listeners
     * @param oldValue the old wheel value
     * @param newValue the new wheel value
     */
    protected void notifyChangingListeners(int oldValue, int newValue)
    {
        for (OnWheelChangedListener listener : mChangingListeners)
        {
            listener.onChanged(this, oldValue, newValue);
        }
    }

    /**
     * 添加滚轮滚动监听
     * @param listener the listener
     */
    public void addScrollingListener(OnWheelScrollListener listener)
    {
        mScrollingListeners.add(listener);
    }

    /**
     * 删除滚轮滚动监听
     * @param listener the listener
     */
    public void removeScrollingListener(OnWheelScrollListener listener)
    {
        mScrollingListeners.remove(listener);
    }

    /**
     * 通知监听器开始发生滚动
     */
    protected void notifyScrollingListenersAboutStart()
    {
        for (OnWheelScrollListener listener : mScrollingListeners)
        {
            listener.onScrollingStarted(this);
        }
    }

    /**
     * 通知监听器停止滚动
     */
    protected void notifyScrollingListenersAboutEnd()
    {
        for (OnWheelScrollListener listener : mScrollingListeners)
        {
            listener.onScrollingFinished(this);
        }
    }

    /**
     * 添加点击滚轮监听
     * @param listener the listener
     */
    public void addClickingListener(OnWheelClickedListener listener)
    {
        mClickingListeners.add(listener);
    }

    /**
     * 删除点击滚轮监听
     * @param listener the listener
     */
    public void removeClickingListener(OnWheelClickedListener listener)
    {
        mClickingListeners.remove(listener);
    }

    /**
     * 通知监听器发生点击
     */
    protected void notifyClickListenersAboutClick(int item)
    {
        Log.d(TAG, "-------------->click");
        for (OnWheelClickedListener listener : mClickingListeners)
        {
            listener.onItemClicked(this, item);
        }
    }

    /**
     * 获取当前滚轮子项
     * 
     * @return the current value
     */
    public int getCurrentItem()
    {
        return mCurrentItem;
    }

    public int getFirstVisibleItem()
    {
        return mFirstItem;
    }

    /**
     * Sets the current item. Does nothing when index is wrong.
     * 
     * @param index the item index
     * @param animated the animation flag
     */
    public void setCurrentItem(int index, boolean animated)
    {
        Log.d(TAG, "-------------->setCurrentItem");
        if (mViewAdapter == null || mViewAdapter.getItemsCount() == 0)
        {
            return;
        }

        int itemCount = mViewAdapter.getItemsCount();
        Log.d(TAG, "-------------->index--->" + index);
        Log.d(TAG, "-------------->currentItem--->" + mCurrentItem);
        if (index < 0 || index > itemCount)
        {
            if (!mIsCyclic)
            {
                // 如果不循环的话，直接返回
                return;
            }
        }
        if (index != mCurrentItem)
        {

            if (animated)
            {
                // 要滚动的项数
                int itemsToScroll = index - mCurrentItem;
                if (mIsCyclic)
                {
                    int scroll = itemCount + Math.min(index, mCurrentItem) - Math.max(index, mCurrentItem);
                    if (scroll < Math.abs(itemsToScroll))
                    {
                        itemsToScroll = itemsToScroll < 0 ? scroll : -scroll;
                    }
                }
                // 触发滚动
                scroll(itemsToScroll, 0);
            }
            else
            {
                mScrollingOffset = 0;

                int old = mCurrentItem;
                mCurrentItem = index;

                notifyChangingListeners(old, mCurrentItem);

                invalidate();
            }
        }
    }

    /**
     * Sets the current item w/o animation. Does nothing when index is wrong.
     * 
     * @param index the item index
     */
    public void setCurrentItem(int index)
    {
        setCurrentItem(index, false);
    }

    /**
     * 判断滚轮是否是可循环的
     * @return true if wheel is cyclic
     */
    public boolean isCyclic()
    {
        return mIsCyclic;
    }

    /**
     * 设置滚轮循环标志
     * @param isCyclic the flag to set
     */
    public void setCyclic(boolean isCyclic)
    {
        Log.d(TAG, "------------>setCyclic");
        this.mIsCyclic = isCyclic;
        invalidateWheel(false);
    }

    /**
     * Invalidates wheel
     * @param clearCaches if true then cached views will be clear
     */
    public void invalidateWheel(boolean clearCaches)
    {
        Log.d(TAG, "------------>invalidateWheel");
        if (clearCaches)
        {
            // 清空子视图
            mRrecycle.clearAll();
            if (mIitemsLayout != null)
            {
                mIitemsLayout.removeAllViews();
            }
            mScrollingOffset = 0;
        }
        else if (mIitemsLayout != null)
        {
            // cache all items
            mRrecycle.recycleItems(mIitemsLayout, mFirstItem, new ItemsRange());
        }

        invalidate();
    }

    /**
     * 初始化资源
     */
    private void initResourcesIfNecessary()
    {
        Log.d(TAG, "------------>initResourcesIfNecessary");
        if (mCenterDrawable == null)
        {
            mCenterDrawable = getContext().getResources().getDrawable(R.drawable.wheel_val);
        }

        if (mTopShadow == null)
        {
            mTopShadow = new GradientDrawable(Orientation.TOP_BOTTOM, SHADOWS_COLORS);
        }

        if (mBbottomShadow == null)
        {
            mBbottomShadow = new GradientDrawable(Orientation.BOTTOM_TOP, SHADOWS_COLORS);
        }

        // setBackgroundResource(R.drawable.wheel_bg);
    }

    private static final int DIVISOR = 50;

    /**
     * 计算布局想要的高度
     * 
     * @param layout the source layout
     * @return the desired layout height
     */
    private int getDesiredHeight(LinearLayout layout)
    {
        if (layout != null && layout.getChildAt(0) != null)
        {
            mItemHeight = layout.getChildAt(0).getMeasuredHeight();
        }

        int desired = mItemHeight * mVisibleItems - mItemHeight * ITEM_OFFSET_PERCENT / DIVISOR;

        return Math.max(desired, getSuggestedMinimumHeight());
    }

    /**
     * 获取滚轮每一项的高度
     * @return the item height
     */
    private int getItemHeight()
    {
        if (mItemHeight != 0)
        {
            return mItemHeight;
        }

        if (mIitemsLayout != null && mIitemsLayout.getChildAt(0) != null)
        {
            mItemHeight = mIitemsLayout.getChildAt(0).getHeight();
            return mItemHeight;
        }

        return getHeight() / mVisibleItems;
    }

    /**
     * Calculates control width and creates text layouts
     * @param widthSize the input layout width
     * @param mode the layout mode
     * @return the calculated control width
     */
    private int calculateLayoutWidth(int widthSize, int mode)
    {
        Log.d(TAG, "-------------->calculateLayoutWidth");
        initResourcesIfNecessary();

        // 设置子布局的宽和高
        mIitemsLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        mIitemsLayout.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.UNSPECIFIED), // UNSPECIFIED：父布局没有给子布局任何限制，子布局可以任意大小。
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        int width = mIitemsLayout.getMeasuredWidth();

        if (mode == MeasureSpec.EXACTLY)
        {
            width = widthSize;
        }
        else
        {
            width += 2 * PADDING;

            // Check against our minimum width
            width = Math.max(width, getSuggestedMinimumWidth());

            if (mode == MeasureSpec.AT_MOST && widthSize < width)// AT_MOST：子布局可以根据自己的大小选择任意大小。
            {
                width = widthSize;
            }
        }

        mIitemsLayout.measure(MeasureSpec.makeMeasureSpec(width - 2 * PADDING, MeasureSpec.EXACTLY), // 水平方向限定大小不可改变
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)); // 垂直方向不限定

        return width;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        Log.d(TAG, "------------>onMeasure");
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        buildViewForMeasuring();

        int width = calculateLayoutWidth(widthSize, widthMode);

        int height = 0;
        if (heightMode == MeasureSpec.EXACTLY)
        {
            height = heightSize;
        }
        else
        {
            height = getDesiredHeight(mIitemsLayout);

            if (heightMode == MeasureSpec.AT_MOST)
            {
                height = Math.min(height, heightSize);
            }
        }
        // 设置子控件想要的大小
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        Log.d("xxxx", "------------>onLayout:");
        super.onLayout(changed, l, t, r, b);
        layout(r - l, b - t);

        if (mOnItemFocusChangedListener != null)
        {
            Log.d("xxxx", "------>onLayout---->currentItem=" + getCurrentItem());
            for (int i = 0; i < mIitemsLayout.getChildCount(); i++)
            {
                mOnItemFocusChangedListener.onFocusChange(mIitemsLayout.getChildAt(i - mFirstItem), getCurrentItem(),
                        (!mIsScrollingPerformed && getCurrentItem() == i));
            }
            // mOnItemFocusChangedListener.onFocusChange(itemsLayout.getChildAt(getCurrentItem()-firstItem),
            // getCurrentItem(), true);
        }
    }

    /**
     * 设置布局的宽度和高度
     * @param width the layout width
     * @param height the layout height
     */
    private void layout(int width, int height)
    {
        Log.d(TAG, "------------>layout");
        int itemsWidth = width - 2 * PADDING;

        mIitemsLayout.layout(0, 0, itemsWidth, height);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        Log.d(TAG, "------------>onDraw");
        super.onDraw(canvas);

        if (mViewAdapter != null && mViewAdapter.getItemsCount() > 0)
        {
            updateView();

            drawItems(canvas);
            // drawCenterRect(canvas);
        }
        // 屏蔽上下渐隐效果
        // drawShadows(canvas);
    }

    private static final int COUNT_1 = 1;

    private static final int COUNT_2 = 2;

    private static final int COUNT_3 = 3;

    private static final int COUNT_4 = 4;

    private static final int COUNT_5 = 5;

    private static final int TOP_OFFSET_1 = -13;

    private static final int TOP_OFFSET_2 = -62;

    private static final int TOP_OFFSET_3 = -12;

    private static final int TOP_OFFSET_4 = -50;

    private static final int TOP_OFFSET_5 = -20;

    private static final int TOP_OFFSET_OTHER = 10;
    /**
     * 绘制列表项
     * @param canvas the canvas for drawing
     */
    private void drawItems(Canvas canvas)
    {
        canvas.save();

        int top = (mCurrentItem - mFirstItem) * getItemHeight() + (getItemHeight() - getHeight()) / 2;

        int count = getViewAdapter().getItemsCount();

        if (count == COUNT_1)
        {
            top = top + TOP_OFFSET_1;
        }
        else if (count == COUNT_2)
        {
            top = top + TOP_OFFSET_2;
        }
        else if (count == COUNT_3)
        {
            top = top + TOP_OFFSET_3;
        }
        else if (count == COUNT_4)
        {
            top = top + TOP_OFFSET_4;
        }
        else if (count >= COUNT_5)
        {
            top = top + TOP_OFFSET_5;
        }
        else
        {
            top = top + TOP_OFFSET_OTHER;
        }
        canvas.translate(PADDING, -top + mScrollingOffset);
        // 渲染画布
        mIitemsLayout.draw(canvas);

        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        Log.d(TAG, "------------>onTouchEvent");
        if (!isEnabled() || getViewAdapter() == null)
        {
            return true;
        }

        switch (event.getAction())
        {
            case MotionEvent.ACTION_MOVE:
                if (getParent() != null)
                {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;

            case MotionEvent.ACTION_UP:
                if (!mIsScrollingPerformed)
                {
                    int distance = (int) event.getY() - getHeight() / 2;
                    if (distance > 0)
                    {
                        distance += getItemHeight() / 2;
                    }
                    else
                    {
                        distance -= getItemHeight() / 2;
                    }
                    int items = distance / getItemHeight();
                    if (items != 0 && isValidItemIndex(mCurrentItem + items))
                    {
                        notifyClickListenersAboutClick(mCurrentItem + items);
                    }
                }
                break;
            default:
                break;
        }

        return mScroller.onTouchEvent(event);
    }

    /**
     * Scrolls the wheel
     * @param delta the scrolling value
     */
    private void doScroll(int delta)
    {
        Log.d(TAG, "------------>doScroll:" + delta);

        mScrollingOffset += delta;

        int itemHeight = getItemHeight();
        Log.d(TAG, "-------------->doScroll:itemHeight" + itemHeight);
        // 跨过的滚动项数(可能为负数)
        int count = mScrollingOffset / itemHeight;

        int pos = mCurrentItem - count;
        Log.d(TAG, "-------------->doScroll:pos" + pos);
        int itemCount = mViewAdapter.getItemsCount();

        int fixPos = mScrollingOffset % itemHeight;
        Log.d(TAG, "-------------->doScroll:fixPos" + fixPos);

        if (Math.abs(fixPos) <= itemHeight / 2)
        {
            fixPos = 0;
        }

        if (mIsCyclic && itemCount > 0)
        {
            if (fixPos > 0)
            {
                pos--;
                count++;
            }
            else if (fixPos < 0)
            {
                pos++;
                count--;
            }
            // fix position by rotating
            while (pos < 0)
            {
                pos += itemCount;
            }
            // 如果跨度很大，pos > 0 且超出0—itemCount范围
            pos %= itemCount;
            // setCurrentItem();
        }
        else
        {
            if (pos < 0)
            {
                pos = 0;
                count = mCurrentItem;

            }
            else if (pos >= itemCount)
            {
                pos = itemCount - 1;
                count = mCurrentItem - itemCount + 1;

            }
            else if (pos > 0 && fixPos > 0)
            {
                pos--;
                count++;
            }
            else if (pos < itemCount - 1 && fixPos < 0)
            {
                pos++;
                count--;
            }
        }

        int offset = mScrollingOffset;
        if (pos != mCurrentItem)
        {
            setCurrentItem(pos, false);
        }
        else
        {
            invalidate();
        }

        // update offset
        mScrollingOffset = offset - count * itemHeight;
        if (mScrollingOffset > getHeight())
        {
            Log.d(TAG, "-------------->scrollingOffset > getHeight():scrollingOffset=" + mScrollingOffset + ",getHeight()=" + getHeight());
            mScrollingOffset = mScrollingOffset % getHeight() + getHeight();
        }
    }

    /**
     * Scroll the wheel
     * @param itemsToSkip items to scroll
     * @param time scrolling duration
     */
    public void scroll(int itemsToScroll, int time)
    {
        int distance = itemsToScroll * getItemHeight() - mScrollingOffset;
        mScroller.scroll(distance, time);
    }

    /**
     * Calculates range for wheel items
     * @return the items range
     */
    private ItemsRange getItemsRange()
    {
        Log.d(TAG, "------------------->getItemsRange");
        if (getItemHeight() == 0)
        {
            return null;
        }

        int first = mCurrentItem;
        int count = 1;

        while (count * getItemHeight() < getHeight())
        {
            first--;
            count += 2; // top + bottom items
        }

        if (mScrollingOffset != 0)
        {
            if (mScrollingOffset > 0)
            {
                first--;
            }
            count++;

            // process empty items above the first or below the second
            int emptyItems = mScrollingOffset / getItemHeight();
            first -= emptyItems;
            count += Math.asin(emptyItems);
        }
        return new ItemsRange(first, count);
    }

    /**
     * Rebuilds wheel items if necessary. Caches all unused items.
     * 
     * @return true if items are rebuilt
     */
    private boolean rebuildItems()
    {
        Log.d(TAG, "------------>rebuildItems");
        ItemsRange range = getItemsRange();
        if (null == range)
        {
            return false;
        }
        boolean updated = false;
        if (mIitemsLayout != null)
        {
            int first = mRrecycle.recycleItems(mIitemsLayout, mFirstItem, range);
            updated = mFirstItem != first;
            mFirstItem = first;
        }
        else
        {
            createItemsLayout();
            updated = true;
        }

        if (!updated)
        {
            updated = (mFirstItem != range.getFirst() || mIitemsLayout.getChildCount() != range.getCount());
        }

        if (mFirstItem > range.getFirst() && mFirstItem <= range.getLast())
        {
            for (int i = mFirstItem - 1; i >= range.getFirst(); i--)
            {
                if (!addViewItem(i, true))
                {
                    break;
                }
                mFirstItem = i;
            }
        }
        else
        {
            mFirstItem = range.getFirst();
        }

        int first = mFirstItem;
        for (int i = mIitemsLayout.getChildCount(); i < range.getCount(); i++)
        {
            if (!addViewItem(mFirstItem + i, false) && mIitemsLayout.getChildCount() == 0)
            {
                first++;
            }
        }
        mFirstItem = first;

        return updated;
    }

    /**
     * Updates view. Rebuilds items and label if necessary, recalculate items
     * sizes.
     */
    private void updateView()
    {
        Log.d(TAG, "------------>updateView");
        if (rebuildItems())
        {
            calculateLayoutWidth(getWidth(), MeasureSpec.EXACTLY); // EXACTLY：父布局决定子布局的确切大小。不论子布局多大，它都必须限制在这个界限里。
            layout(getWidth(), getHeight());
        }
    }

    /**
     * Creates item layouts if necessary
     */
    private void createItemsLayout()
    {
        Log.d(TAG, "-------------->createItemsLayout");
        if (mIitemsLayout == null)
        {
            mIitemsLayout = new LinearLayout(getContext());
            mIitemsLayout.setOrientation(LinearLayout.VERTICAL);
        }
    }

    /**
     * Builds view for measuring
     */
    private void buildViewForMeasuring()
    {
        // clear all items
        Log.d(TAG, "---------------->buildViewForMeasuring");
        if (mIitemsLayout != null)
        {
            mRrecycle.recycleItems(mIitemsLayout, mFirstItem, new ItemsRange());
        }
        else
        {
            createItemsLayout();
        }

        // add views
        int addItems = mVisibleItems / 2;
        for (int i = mCurrentItem + addItems; i >= mCurrentItem - addItems; i--)
        {
            if (addViewItem(i, true))
            {
                mFirstItem = i;

            }
        }
        Log.d(TAG, "------------------>firstItem:" + mFirstItem);
    }

    /**
     * 添加子列表项到子视图中
     * @param index the item index
     * @param first the flag indicates if view should be first
     * @return true if corresponding item exists and is added
     */
    private boolean addViewItem(int index, boolean first)
    {
        Log.d(TAG, "------------>addViewItem:" + index);
        View view = getItemView(index);
        if (view != null)
        {

            if (first)
            {
                mIitemsLayout.addView(view, 0);
                // itemsLayout.addView(view, 0, params)
            }
            else
            {
                mIitemsLayout.addView(view);
            }

            return true;
        }

        return false;
    }

    /**
     * 检查当前列表项是否合法
     * @param index the item index
     * @return true if item index is not out of bounds or the wheel is cyclic
     */
    private boolean isValidItemIndex(int index)
    {
        Log.d(TAG, "----------------->isValidItemIndex:" + index);
        return mViewAdapter != null && mViewAdapter.getItemsCount() > 0 && (mIsCyclic || index >= 0 && index < mViewAdapter.getItemsCount());
    }

    /**
     * 返回子项视图
     * @param index the item index
     * @return item view or empty view if index is out of bounds
     */
    public View getItemView(int index)
    {
        Log.d(TAG, "------------------>getItemView");
        if (mViewAdapter == null || mViewAdapter.getItemsCount() == 0)
        {
            return null;
        }
        int count = mViewAdapter.getItemsCount();
        if (!isValidItemIndex(index))
        {
            return mViewAdapter.getEmptyItem(mRrecycle.getEmptyItem(), mIitemsLayout);
        }
        else
        {
            while (index < 0)
            {
                index = count + index;
            }
        }

        index %= count;
        return mViewAdapter.getItem(index, mRrecycle.getItem(), mIitemsLayout);
    }

    /**
     * 停止滚动
     */
    public void stopScrolling()
    {
        mScroller.stopScrolling();
    }

    public void setOnItemFocusChangedListener(OnItemFocusChangedListener l)
    {
        mOnItemFocusChangedListener = l;
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect)
    {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (mOnItemFocusChangedListener != null)
        {
            if (mIitemsLayout != null)
            {
                mOnItemFocusChangedListener.onFocusChange(mIitemsLayout.getChildAt(getCurrentItem() - mFirstItem), getCurrentItem(), gainFocus);
            }
        }
    }

    public boolean isScrollingPerformed()
    {
        return mIsScrollingPerformed;
    }

    public void setScrollingPerformed(boolean isScrollingPerformed)
    {
        this.mIsScrollingPerformed = isScrollingPerformed;
    }

}
