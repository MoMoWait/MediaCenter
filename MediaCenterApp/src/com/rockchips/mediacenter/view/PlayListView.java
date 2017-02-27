package com.rockchips.mediacenter.view;

import java.util.ArrayList;
import java.util.List;

import momo.cn.edu.fjnu.androidutils.data.CommonValues;
import momo.cn.edu.fjnu.androidutils.utils.DeviceInfoUtils;
import momo.cn.edu.fjnu.androidutils.utils.SizeUtils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rockchips.mediacenter.data.ConstData.PlayState;
import com.rockchips.mediacenter.R;

/**
 * @author s00203507
 * 
 */
public class PlayListView extends RelativeLayout
{
    private static final String TAG = "PlayListView";

    /**
     * ListView中每一个Item的宽度
     */
    private int mListItemWidth;

    /**
     * ListView中每一个Item的高度
     */
    private int mListItemHeight;

    /**
     * Context引用
     */
    private Context mContext;

    /**
     * ListView焦点框
     */
    private GlobalFocus mFocusView;

    /**
     * 视图容器，存放Adapter中创建的View
     */
    private List<View> mViewList = new ArrayList<View>();

    /**
     * ListView上方填充空白
     */
    private int mListViewTopPadding = 0;

    /**
     * 当前聚焦的数据项索引
     */
    private int mCurrentFocusedIndex;

    /**
     * 当前播放索引
     */
    private int mCurrentPlayIndex;

    /**
     * 可视区域内第一个可聚焦的item
     */
    private int mFirstActiveSlot = 0;

    /**
     * 可视区域内最后一个可聚焦的item
     */
    private int mLastActiveSlot = 5;

    /**
     * 当前聚焦的activeSlot
     */
    private int mActiveSlot = 0;

    /**
     * 可视区域内的item个数
     */
    private int mVisibleItemCount;

    /**
     * 焦点改变监听器
     */
    private OnItemFocusChangeListener mItemFocusListener;

    /**
     * item按键点击监听器
     */
    private OnItemClickedListener mItemClickListener;

    /**
     * item空鼠点击监听器
     */
    private OnItemTouchListener mItemTouchListener;

    /**
     * 按键方向枚举类
     */
    private Direction mDirection;

    /**
     * 焦点框上下移动步长
     */
    private int mFocusYStep = 70;

    /**
     * 焦点框当前x值
     */
    private int mFocusX = 0;

    /**
     * 焦点框当前y值
     */
    private int mFocusY;

    /**
     * 焦点框平移前的x值
     */
    private int mFocusPreX = 0;

    /**
     * 焦点框平移前的y值
     */
    private int mFocusPreY;

    private TextView mHintText;

    /**
     * 数据总数
     */
    private int mItemCount;

    /**
     * 数据适配器
     */
    private IListDataAdapter mAdapter;

    /**
     * 是否只有一列，默认只有一列，待扩展
     */
    private boolean mSingleColumn = true;

    /**
     * 当前显示的数据在数据列表中的偏移索引
     */
    private int mOffsetIndex = 0;
    
    private PlayState mCurPlayState = PlayState.PLAY;

    public PlayListView(Context context)
    {
        super(context);
        init(context);
    }

    public PlayListView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context);
    }

    public PlayListView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init(context);
    }

    /**
     * 初始化context
     * 
     * @param context
     */
    public void initContext(Context context)
    {
        mContext = context;
    }

    /**
     * 初始化可见的Item个数
     */
    public void initVisibleItemCount()
    {
        Log.d(TAG, "initVisibleItemCount() IN...");
        int count = mLastActiveSlot - mFirstActiveSlot + 1;
        mVisibleItemCount = (count < mItemCount) ? count : mItemCount;
        Log.d(TAG, "mVisibleItemCount is " + mVisibleItemCount);
    }

    /**
     * 初始化item宽和高
     */
    public void initItemSize()
    {
        this.mListItemHeight = 70;
        //计算item宽度，这里根据屏幕宽度，动态生成
        this.mListItemWidth = DeviceInfoUtils.getScreenWidth(mContext) / 2 - SizeUtils.dp2px(mContext, 20);
    }

    /**
     * @return the mListItemWidth
     */
    public int getListItemWidth()
    {
        return mListItemWidth;
    }

    /**
     * @param mListItemWidth the mListItemWidth to set
     */
    public void setListItemWidth(int listItemWidth)
    {
        this.mListItemWidth = listItemWidth;
    }

    /**
     * @return the mListItemHeight
     */
    public int getListItemHeight()
    {
        return mListItemHeight;
    }

    /**
     * @param mListItemHeight the mListItemHeight to set
     */
    public void setListItemHeight(int listItemHeight)
    {
        this.mListItemHeight = listItemHeight;
    }

    /**
     * 构造器中调用的初始化函数，初始化Context、item宽高等
     * 
     * @param context
     */
    public void init(Context context)
    {
        Log.d(TAG, "init() IN...");

        initContext(context);

        initItemSize();

        initVisibleItemCount();

    }

    /**
     * 为Adapter创建的View布局，并将View添加到RelativeLayout中显示
     */
    public void layoutItems()
    {
        Log.d(TAG, "layoutItems() IN...");

        View v = null;
        if (mViewList.size() == 0)
        {
            for (int i = 0; i < mVisibleItemCount; i++)
            {
                v = layoutItemView(i);
                mViewList.add(v);
                addView(v);
            }

        }
    }

    public interface OnMenuListener
    {
        void onOpen();

        void onActiveItemChanged(int item);
    }

    private OnMenuListener mOnMenuListener;

    public void setOnMenuListener(OnMenuListener callback)
    {
        mOnMenuListener = callback;
    }

    /**
     * 判断Adapter是否为空
     * 
     * @return
     */
    public boolean isAdapterEmpty()
    {
        if (mAdapter == null || mAdapter.getDataList() == null || mAdapter.getDataList().size() == 0)
        {
            return true;
        }

        return false;
    }

    /**
     * 为itemview布局
     * 
     * @param itemIndex
     * @return
     */
    public View layoutItemView(int itemIndex)
    {
        Log.d(TAG, "layoutItemView() IN...");
        if (mAdapter == null)
        {
            return null;
        }

        View view = mAdapter.createItemView(itemIndex, mOffsetIndex, mVisibleItemCount);

        RelativeLayout.LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.width = mListItemWidth;
        params.height = mListItemHeight;
        params.topMargin = mListViewTopPadding + itemIndex * mListItemHeight;

        view.setLayoutParams(params);

        view.setTag(itemIndex + mOffsetIndex);

        view.setOnTouchListener(mOnTouchListener);
        return view;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        Log.d(TAG, "onLayout() IN...");
        super.onLayout(changed, l, t, r, b);
        layoutItems();
    }

    /**
     * 为ListView添加焦点
     * 
     * @param focus 焦点框对象
     * 
     */
    public void addFocus(ImageView focus)
    {
        if (focus == null)
        {
            return;
        }
        this.mFocusView = (GlobalFocus) focus;

        initFocusPosition();
    }

    public boolean processKeyDown(int keyCode)
    {
        if (isAdapterEmpty())
        {
            return false;
        }

        switch (keyCode)
        {
            case KeyEvent.KEYCODE_DPAD_UP:
                mDirection = Direction.TOP;
                calculateFocusOffset();
                if (mHintText != null)
                {

                    mHintText.setText(mCurrentFocusedIndex + "");
                }
                return true;

            case KeyEvent.KEYCODE_DPAD_DOWN:
                mDirection = Direction.BOTTOM;
                calculateFocusOffset();
                if (mHintText != null)
                {

                    mHintText.setText(mCurrentFocusedIndex + "");
                }

                return true;

            default:
                break;
        }
        return false;
    }

    public void processRandomPlay(int targetIndex)
    {
        if (isAdapterEmpty())
        {
            return;
        }
        if (targetIndex < 0 || targetIndex >= mAdapter.getCount())
        {
            return;
        }

        int step = targetIndex - mCurrentFocusedIndex;
        mCurrentFocusedIndex = targetIndex;
        Log.d(TAG, "mActiveSlot " + mActiveSlot + ",mCurrentFocusedIndex " + mCurrentFocusedIndex + ",step " + step + ",targetIndex " + targetIndex);

        if (isInVisibleRegion(targetIndex))
        {
            Log.d(TAG, "111");
            // 向下滚动时，如果当前聚焦的view在可视区域内的倒数第二项，并且目标聚焦项不是最后一条数据，
            // 则焦点框不移动，视图内容向上滚动；如果是最后一条数据，则焦点框可以下移
            if (mActiveSlot == (mLastActiveSlot - 1))
            {
                if (step > 0)
                {
                    if (!isLastItem(targetIndex))
                    {
                        mOffsetIndex++;
                        mViewList = mAdapter.updateItemView(mViewList, mVisibleItemCount, mOffsetIndex);
                        processMarquee();
                        return;
                    }
                }
            }

            // 向上滚动时，如果当前聚焦的view在可视区域内的第二项，并且目标聚焦项不是第一条数据，
            // 则焦点框不移动，视图内容向下滚动；如果是第一条数据，则焦点框可以上移
            if (mActiveSlot == (mFirstActiveSlot + 1))
            {
                if (step < 0)
                {
                    if (!isFirstItem(targetIndex))
                    {
                        mOffsetIndex--;
                        mViewList = mAdapter.updateItemView(mViewList, mVisibleItemCount, mOffsetIndex);
                        processMarquee();
                        return;
                    }
                }
            }

            mActiveSlot += step;
            mFocusY += step * getYStep();

            // 回调焦点框的焦点改变方法，实现焦点框上移动画
            doFocusTranslateAnimation();
            processMarquee();
            return;
        }

        // 目标索引不在可视范围内，step>0说明需要展示的目标索引值在当前索引值的下方，需要将焦点下移，数据内容上移到目标索引值在可视范围内的最后一条数据上
        if (step > 0)
        {
            Log.d(TAG, "222");
            // 向下移动焦点
            // step = mLastActiveSlot - mActiveSlot;
            step = mLastActiveSlot - mActiveSlot;
            mActiveSlot += step;
            mFocusY += step * getYStep();
            doFocusTranslateAnimation();

            // mOffsetIndex = targetIndex - mVisibleItemCount + 1;
            mOffsetIndex = targetIndex - mVisibleItemCount + 1;
            mViewList = mAdapter.updateItemView(mViewList, mVisibleItemCount, mOffsetIndex);
            processMarquee();
        }

        // 目标索引不在可视范围内，step<0说明需要展示的目标索引值在当前索引值的上方，需要将焦点上移，数据内容下移到目标索引值在可视范围内的第一条数据上
        else if (step < 0)
        {
            Log.d(TAG, "333");
            // 向上移动焦点
            // step = mFirstActiveSlot - mActiveSlot;
            step = mFirstActiveSlot - mActiveSlot;
            mActiveSlot += step;
            mFocusY += step * getYStep();
            doFocusTranslateAnimation();

            // mOffsetIndex = targetIndex;
            mOffsetIndex = targetIndex;
            mViewList = mAdapter.updateItemView(mViewList, mVisibleItemCount, mOffsetIndex);
            processMarquee();
        }

    }

    public boolean isLastItem(int index)
    {
        if (index == mAdapter.getCount() - 1)
        {
            return true;
        }
        return false;
    }

    public boolean isFirstItem(int index)
    {
        if (index == 0)
        {
            return true;
        }
        return false;
    }

    /**
     * 判断目标播放索引是否在可视区域内
     * 
     * @param targetIndex 目标索引值
     * @return
     */
    public boolean isInVisibleRegion(int targetIndex)
    {
        if (targetIndex < 0 || targetIndex >= mAdapter.getCount())
        {
            return false;
        }

        if (targetIndex >= mOffsetIndex && targetIndex < mOffsetIndex + mVisibleItemCount)
        {
            return true;
        }

        return false;

    }

    /**
     * 计算焦点框偏移量，进行焦点框平移或者更新视图内容
     * 
     * @return
     */
    public boolean calculateFocusOffset()
    {
        Log.d(TAG, "calculateFocusOffset() IN...");
        switch (mDirection)
        {
            case TOP:
                // 如果当前焦点已经在第一条数据上，则焦点无法上移
                if (mCurrentFocusedIndex == 0)
                {
                    // Log.d(TAG, "111");
                    return false;
                }

                mCurrentFocusedIndex--;
                mDirection = Direction.TOP;

                int step = -1;

                if (mActiveSlot > mFirstActiveSlot)
                {
                    // Log.d(TAG, "999");
                    // 可聚焦的数据项在视窗内，直接将焦点上移
                    mActiveSlot += step;
                    mFocusY += step * getYStep();

                    // 回调焦点框的焦点改变方法，实现焦点框上移动画
                    doFocusTranslateAnimation();

                    processMarquee();
                    return true;

                }
                else
                {
                    // Log.d(TAG, "101010");
                    mOffsetIndex += step;
                    mViewList = mAdapter.updateItemView(mViewList, mVisibleItemCount, mOffsetIndex);
                    processMarquee();
                }

                return true;
            case BOTTOM:

                // 如果当前聚焦的数据已经是最后一项，则焦点无法下移
                if (mCurrentFocusedIndex < (mAdapter.getCount() - 1))
                {
                    mCurrentFocusedIndex++;
                    mDirection = Direction.BOTTOM;
                    int stepDown = 1;

                    // 说明焦点可以向下平移
                    if (mActiveSlot < mLastActiveSlot)
                    {
                        mActiveSlot += stepDown;
                        mFocusY += stepDown * getYStep();

                        doFocusTranslateAnimation();
                        processMarquee();

                        return true;
                    }
                    else
                    {
                        // 焦点已经在可视视窗内的最后一条数据上，不能向下移动，则更新ItemView里面的显示内容。
                        mOffsetIndex += stepDown;
                        mViewList = mAdapter.updateItemView(mViewList, mVisibleItemCount, mOffsetIndex);
                        processMarquee();
                        return false;
                    }
                }

                default:
                    break;
        }

        return true;
    }

    /**
     * 调用焦点框回调方法，对焦点进行平移动画
     */
    public void doFocusTranslateAnimation()
    {
        if (mItemFocusListener != null)
        {
            mItemFocusListener.onItemFocusChange(mCurrentFocusedIndex, mFocusPreX, mFocusX, mFocusPreY, mFocusY);
        }

        // 焦点框动画完成后，保存焦点框当前位置。
        mFocusPreY = mFocusY;
        mFocusPreX = mFocusX;

        // processMarquee();
    }

    public int getYStep()
    {
        return mFocusYStep;
    }

    public void setYStep(int yStep)
    {
        this.mFocusYStep = yStep;
    }

    /**
     * 焦点改变监听器
     */
    public interface OnItemFocusChangeListener
    {

        /**
         * 聚焦的item改变时回调方法
         * 
         * @param index
         */
        void onItemFocusChange(int index, float fromXDelta, float toXDelta, float fromYDelta, float toYDelta);

        // void onStartScroll(int direction);
    }

    /**
     * @return the mItemFocusListener
     */
    public OnItemFocusChangeListener getItemFocusListener()
    {
        return mItemFocusListener;
    }

    /**
     * @param mItemFocusListener the mItemFocusListener to set
     */
    public void setItemFocusListener(OnItemFocusChangeListener itemFocusListener)
    {
        this.mItemFocusListener = itemFocusListener;
    }

    public TextView getHintText()
    {
        return mHintText;
    }

    public void setHintText(TextView hintText)
    {
        this.mHintText = hintText;
    }

    /**
     * @return the mAdapter
     */
    public IListDataAdapter getAdapter()
    {
        return mAdapter;
    }

    /**
     * 设置ListView相关联的Adapter，并设置需要聚焦的第一个数据项
     * 
     * @param adapter 持有ListView显示所需要的数据的适配器
     * @param index 需要聚焦的第一个数据项索引
     */
    public void setAdapter(IListDataAdapter adapter, int index)
    {
        Log.d(TAG, "setAdapter() IN...");
        this.mAdapter = adapter;
        if (adapter != null)
        {
            mItemCount = adapter.getCount();
            // Log.d(TAG, "after setAdapter(), mItemCount is " + mItemCount);
        }

        setCurrentItem(index);

        initVisibleItemCount();
        mViewList.clear();
        removeAllViews();
        layoutItems();

        if (mFocusView != null)
        {
            initFocusPosition();
        }

        if (mOnMenuListener != null)
        {
            mOnMenuListener.onOpen();
        }

        processMarquee();

    }

    /**
     * 根据可聚焦的数据项索引值初始化焦点框位置
     */
    public void initFocusPosition()
    {
        // mCurrentFocusedIndex = 0;

        if (isAdapterEmpty())
        {
            return;
        }

        if (mAdapter.getCount() == 1)
        {
            mActiveSlot = 0;
        }

        LinearLayout.LayoutParams params = mFocusView.getFocusInitParams();
        //params.topMargin = SizeUtils.dp2px(CommonValues.application, 40) + mActiveSlot * mListItemHeight;
        params.topMargin = mFocusView.getInitTopMargin() + mActiveSlot * mListItemHeight;
        params.leftMargin = mFocusView.getInitLeftMargin();
        params.rightMargin = SizeUtils.dp2px(mContext, 10);
        //params.leftMargin = DeviceInfoUtils.getScreenWidth(mContext) / 2;
        mFocusView.setLayoutParams(params);

        // mCurrentFocusedIndex = 0;
        // mActiveSlot = 0;

        mFocusY = mFocusView.getInitTopMargin() + mActiveSlot * mListItemHeight;
        mFocusPreY = mFocusY;

        mFocusView.setVisibility(INVISIBLE);

    }

    /**
     * @return
     */
    public OnItemClickedListener getItemClickListener()
    {
        return mItemClickListener;
    }

    /**
     * @param itemClickListener
     */
    public void setItemClickListener(OnItemClickedListener itemClickListener)
    {
        this.mItemClickListener = itemClickListener;
    }

    /**
     * 数据项按键点击监听器
     */
    public interface OnItemClickedListener
    {
        void onItemClicked(int index);
    }

    /**
     * 数据项鼠标点击监听器
     */
    public interface OnItemTouchListener
    {
        void onItemTouch(View v);
    }

    public OnItemTouchListener getItemTouchListener()
    {
        return mItemTouchListener;
    }

    public void setItemTouchListener(OnItemTouchListener itemTouchListener)
    {
        this.mItemTouchListener = itemTouchListener;
    }

    /**
     * @return the mSingleColumn
     */
    public boolean isSingleColumn()
    {
        return mSingleColumn;
    }

    /**
     * @param mSingleColumn the mSingleColumn to set
     */
    public void setSingleColumn(boolean singleColumn)
    {
        this.mSingleColumn = singleColumn;
    }

    /**
     * 焦点发生变化时处理文字跑马灯效果 1、取消前一个聚焦项的跑马灯效果 2、对当前聚焦的数据项添加跑马灯效果
     * 
     * @see [类、类#方法、类#成员]
     */
    public void processMarquee()
    {
        RelativeLayout layout;
        TextView view;
        ImageView playControlIcon;

        for (int i = 0; i < mViewList.size(); i++)
        {
            layout = (RelativeLayout) mViewList.get(i);
            playControlIcon = (ImageView) layout.getChildAt(0);
            view = (TextView) layout.getChildAt(1);
            int indexOfView = (Integer) layout.getTag();

            if (indexOfView == mCurrentPlayIndex)
            {
                if (mCurPlayState == PlayState.PLAY)
                {
                    playControlIcon.setImageResource(R.drawable.music_play_icon);
                }
                else
                {
                    playControlIcon.setImageResource(R.drawable.music_pause_icon);
                }
                playControlIcon.setVisibility(VISIBLE);
                view.setSelected(true);
                view.setTextColor(mContext.getResources().getColor(R.color.white));
            }
            else
            {
                playControlIcon.setVisibility(INVISIBLE);
                view.setSelected(false);
                view.setTextColor(mContext.getResources().getColor(R.color.itemUnFocused));
            }
        }
    }

    public void switchPlayControlIcon(PlayState state)
    {
        RelativeLayout layout;
        ImageView playControlIcon;
        
        mCurPlayState = state;

        for (int i = 0; i < mViewList.size(); i++)
        {
            layout = (RelativeLayout) mViewList.get(i);
            playControlIcon = (ImageView) layout.getChildAt(0);

            if (playControlIcon == null)
            {
                return;
            }
            int indexOfView = (Integer) layout.getTag();

            if (indexOfView == mCurrentPlayIndex)
            {
                switch (state)
                {
                    case PLAY:
                        playControlIcon.setImageResource(R.drawable.music_play_icon);
                        break;

                    case PAUSE:
                        playControlIcon.setImageResource(R.drawable.music_pause_icon);
                        break;
                }
                return;
            }
        }
    }

    /**
     * 设置ListView当前聚焦的数据项索引值，并根据index设置Adapter需要高亮显示的数据项索引
     * 
     * @param index 需要聚焦的数据项索引值
     */
    public void setCurrentItem(int index)
    {
    	
    	//设置背景颜色为透明
    	/*if(mCurrentPlayIndex >= 0 && mViewList != null 
    			&& mViewList.size() > 0 && mCurrentPlayIndex < mViewList.size()){
        	//mViewList.get(mCurrentFocusedIndex).setBackground(new ColorDrawable(Color.TRANSPARENT));
        	mViewList.get(mCurrentFocusedIndex).setBackgroundColor(Color.TRANSPARENT);
    	}*/
    	
        // index为0，需要将焦点放在可视区域内的第一个位置上
        if (index == 0)
        {
            mOffsetIndex = index;
            mAdapter.setIndexNeedColored(0);
        }

        // 当index>=1时，需要将焦点放在可视区域内的第二个位置上
        if (index >= 1)
        {
            mOffsetIndex = index - 1;
            mActiveSlot = 1;
            mAdapter.setIndexNeedColored(1);
        }

        mCurrentFocusedIndex = index;
        //设置背景颜色
      /*  if(mViewList != null && mViewList.size() > 0){
        	mViewList.get(mCurrentFocusedIndex).setBackgroundColor(Color.parseColor("#2158A9"));
        }*/

    }

    public int getCurrentItem()
    {
        return mCurrentFocusedIndex;
    }

    public void setCurrentPlayIndex(int currentPlayIndex)
    {
        mCurrentPlayIndex = currentPlayIndex;
    }

    private OnTouchListener mOnTouchListener = new OnTouchListener()
    {

        @Override
        public boolean onTouch(View v, MotionEvent event)
        {
            Log.d(TAG, "view is " + v + ", view position is " + v.getTag());

            // 没有数据的话直接返回，无需添加点击事件
            if (mAdapter.getDataList() == null || mAdapter.getDataList().size() == 0)
            {
                return false;
            }

            int targetIndex = (Integer) v.getTag();

            mActiveSlot += targetIndex - mCurrentFocusedIndex;

            mFocusY += (targetIndex - mCurrentFocusedIndex) * getYStep();
            if (mItemFocusListener != null)
            {
                mItemFocusListener.onItemFocusChange(mCurrentFocusedIndex, mFocusPreX, mFocusX, mFocusPreY, mFocusY);
            }

            mFocusPreY = mFocusY;
            mFocusPreX = mFocusX;

            mCurrentPlayIndex = targetIndex;
            mCurrentFocusedIndex = targetIndex;

            if (mItemTouchListener != null)
            {
                mItemTouchListener.onItemTouch(v);
            }
            return true;
        }
    };

    public void resetItemName(String name)
    {
        if (mViewList.size() == 0 || name == null)
        {
            return;
        }

        RelativeLayout layout;
        TextView textView;
        layout = (RelativeLayout) mViewList.get(0);
        textView = (TextView) layout.getChildAt(1);

        if (textView == null)
        {
            return;
        }

        textView.setText(name);
    }
}
