package com.rockchips.mediacenter.viewutils.menu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import momo.cn.edu.fjnu.androidutils.data.CommonValues;
import momo.cn.edu.fjnu.androidutils.utils.SizeUtils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rockchips.mediacenter.viewutils.R;

/***
 * SR-0000382162 媒体中心USB外接设备的内容展示 AR-0000698365媒体文件的删
 * @author zwx160481
 * @version 1.0
 * 
 */

public class BottomPopMenu extends PopupWindow
{

    private static final String TAG = "BottomMenu";

    /**
     * 自动隐藏菜单
     */
    private static final int MSG_UI_DISMISS = 101;

    /**
     * 自动隐藏时间 默认为5秒
     */
    private static final int DISMISS_TIME = 3000;

    /**
     * 底部弹出菜单栏间隔 px
     */
    private int mBottomY;

    /**
     * 底部弹出菜单栏间隔 px
     */
    private int mBottomX;

    /**
     * 上下文
     */
    private Context mContext;

    /**
     * UI主线程与子线程交互Handler
     */
    private Handler mHandler = new UIHandler();

    private OnSelectTypeListener mOnSelectTypeListener;    

    /**
     * BaseActivity 实现类的引用
     */
    // private OnSelectDisplayTypeListener mOnSelectDisplayTypeListener;

    /**
     * 菜单栏按钮布局
     **/
    private View mButtonLayout;

    /**
     * 按钮之间的距离 默认为10dip
     **/
    private static final int MENU_BUTTON_GAP = 50;

    /**
     * menu 按钮字体颜色 有默认值
     */
    private int mButtonTextColor;

    /**
     * 菜单栏按钮正常状态时的背景,默认不为空
     */
    private BitmapDrawable mMenuButtonNormalBg;

    /**
     * 音频控制
     */
    private AudioManager mAudioManager;

    /**
     * 布局文件实例化
     */
    private LayoutInflater mInflater;

    private MenuView mMenuView;

    private OnMenuListener mOnMenuListener;

    /**
     * 菜单监听回调接口
     * @author s00211113
     * 
     */
    public interface OnMenuListener
    {
        void onClose();

        void onItemFocusChanged(int id);
    }

    public void setOnMenuListener(OnMenuListener l)
    {
        this.mOnMenuListener = l;
    }

    private VolumeKeyListener mVolumeKeyListener;

    /**
     * 菜单构造函数，加载默认背景和布局
     */
    public BottomPopMenu(Context context)
    {
        super(context);
        init(context);
    }

    private void init(Context context)
    {
        mContext = context;
        this.setFocusable(true);
        mInflater = LayoutInflater.from(context);

        // mMenuButtonSelectedBg =
        // (BitmapDrawable)context.getResources().getDrawable(R.drawable.buttonview_focus_border);
        mMenuButtonNormalBg = (BitmapDrawable) context.getResources().getDrawable(R.drawable.buttonview_border);
        mButtonTextColor = context.getResources().getColor(R.color.menu_category_color_s);

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        @SuppressWarnings("deprecation")
        int width = wm.getDefaultDisplay().getWidth();
        //BitmapDrawable drawbleBg = (BitmapDrawable) context.getResources().getDrawable(R.drawable.option_bg);
        this.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#292930"))); // 设置TabMenu菜单背景
        this.setWidth(width);
        //this.setHeight(drawbleBg.getBitmap().getHeight());
        //fly.gao
        this.setHeight(SizeUtils.dp2px(mContext, 136));
        //this.setAnimationStyle(R.style.MenuAnimation);
        this.setFocusable(true); // menu菜单获得焦点 如果没有获得焦点menu菜单中的控件事件无法响应

        mMenuView = new MenuView(context);
        this.setContentView(mMenuView);
        mMenuView.requestFocus();
        mMenuView.setFocusable(true);

        if (mAudioManager == null)
        {
            Log.d(TAG, "audioManager == null, create a AudioManager object");
            mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        }
    }

    /**
     * 增加菜单项
     * @param id Item的ID标志
     * @param selectType 标识item的，设置枚举类型
     * @param iconresId 资源图片ID
     * @param groupId 分组id
     * @param order 标志同组的显示顺序的id 数值越小越靠前显示
     * @param title item的标题
     * 
     * @return 生成的Item Menu
     */
    public MenuItem add(int id, Object selectType, int iconresId, int groupId, int order, String title)
    {
        return mMenuView.add(id, selectType, iconresId, groupId, order, title);
    }

    /**
     * 添加当前显示菜单项
     */
    public void addCurrentMenu(ArrayList<MenuItemImpl> list)
    {
        mMenuView.addCurrentMenu(list);
    }

    /**
     * 计时自动隐藏菜单
     */
    private void dismissDelay()
    {
        mHandler.removeMessages(MSG_UI_DISMISS);
        mHandler.sendEmptyMessageDelayed(MSG_UI_DISMISS, DISMISS_TIME);
    }

    /**
     * 显示菜单
     * @param parent 菜单显示需要一个parent，建议是Activity的第一个布局View
     */
    public void show(View parent)
    {
        if (parent != null)
        {
            showAtLocation(parent, Gravity.BOTTOM, mBottomX, mBottomY);
            dismissDelay();
        }
    }

    /**
     * 隐藏
     */
    public void hide()
    {
        dismiss();
    }

    /**
     * 安全的dismisse
     **/
    @Override
    public void dismiss()
    {
        mHandler.removeMessages(MSG_UI_DISMISS);
        try
        {
            super.dismiss();
        }
        catch (IllegalArgumentException e)
        {
            Log.d(TAG, "dismiss failed......");
        }
    }

    /**
     * 设置当前菜单Item项
     */
    public void setCurrentMenuItem(int id)
    {
        mMenuView.setCurrentMenuItem(id);
    }

    /**
     * 获取当前菜单Item项
     */
    public int getCurrentMenuItem()
    {
        return mMenuView.getCurrentMenuItem();
    }

    public ArrayList<MenuItemImpl> getCurrentMenuItemImpl()
    {
        return mMenuView.getCurrentMenuItemImpl();
    }

    public void clear()
    {
        mMenuView.clear();
    }

    /**
     * 菜单项被选择的回调接口
     * @author s00211113
     * 
     */
    public interface MenuItemSelectedListener
    {
        void onItemSelected(MenuItem item);
    }

    /**
     * MenuView布局
     * @author s00211113
     * 
     */
    private final class MenuView extends RelativeLayout
    {
        private LinearLayout mLayout;

        private ArrayList<MenuItemImpl> mItems;

        private int mCurrentItemIndex = -1;

        //private ImageView mFocusAnimView;

        private boolean mbNeedLayout;

        //private static final int LAYOUT_WIDTH;
        
        private int LAYOUT_WIDTH = SizeUtils.dp2px(CommonValues.application, 200);
        
        //private static final int LAYOUT_HIGTH;
        
        private int LAYOUT_HIGTH = SizeUtils.dp2px(CommonValues.application, 150);

        private static final int ANIMATION_DURATION = 200;

        /**
         * MenuView构造函数， 菜单上mLayout装载Item，mFocusAnimView承载Item选中时背景
         * @param context
         */
        private MenuView(Context context)
        {
            super(context);
            //LAYOUT_WIDTH = SizeUtils.dp2px(CommonValues.application, 260);
           // LAYOUT_HIGTH = SizeUtils.dp2px(CommonValues.application, 174);
            
            this.setFocusable(true);
            this.setFocusableInTouchMode(true);

            mbNeedLayout = false;

            this.setOnKeyListener(mKeyListener);
            mItems = new ArrayList<MenuItemImpl>();

            mLayout = new LinearLayout(context);
            mLayout.setOrientation(LinearLayout.HORIZONTAL);
            mLayout.setGravity(Gravity.CENTER);
            mLayout.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            this.setGravity(Gravity.CENTER);
            this.addView(mLayout);
        }

        /**
         * 增加菜单项
         * @param id Item的ID标志
         * @param iconresId 资源图片ID
         * @param groupId 所属组id
         * @param order 显示顺序
         * @return 生成的Item Menu
         * 
         */
        public MenuItem add(int id, Object selectType, int iconresId, int groupId, int order, String title)
        {
            final MenuItemImpl item = new MenuItemImpl(mContext, id, selectType, iconresId, groupId, order, title);
            mItems.add(item);
            addViewByFilterGroudId(item);
            if (mCurrentItemIndex == -1)
            {
                changeFocusItem(0);
            }
            mbNeedLayout = true;
            return item;
        }

        @SuppressWarnings("deprecation")
        public void addCurrentMenu(ArrayList<MenuItemImpl> list)
        {
            if (mLayout != null && list != null)
            {
                mLayout.removeAllViews();
                LinearLayout.LayoutParams p = null;
                ImageView imageView = null;
                TextView textView = null;
                int size = list.size();
                for (int i = 0; i < size; i++)
                {
                    MenuItemImpl item = list.get(i);
                    mButtonLayout = mInflater.inflate(R.layout.menu_button_layout, null);
                    imageView = (ImageView) mButtonLayout.findViewById(R.id.menu_btn_image);
                    textView = (TextView) mButtonLayout.findViewById(R.id.menu_btn_tital);
                    imageView.setImageDrawable(item.getIcon());
                    textView.setText(item.getTitle());
                    textView.setTextColor(mButtonTextColor);

                    mButtonLayout.setTag(item);
                   /* if (mMenuButtonNormalBg != null)// 设置按钮正常的背景颜色
                    {
                        mButtonLayout.setBackgroundDrawable(mMenuButtonNormalBg);
                    }*/
                    p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    p.gravity = Gravity.CENTER;
                    p.leftMargin = MENU_BUTTON_GAP;
                    mLayout.addView(mButtonLayout, p);
                    mButtonLayout.setOnTouchListener(mTouchListener);
                }
                if (mCurrentItemIndex == -1)
                {
                    changeFocusItem(0);
                }
                mbNeedLayout = true;
            }
        }

        /**
         * 过滤添加MenuItem 如果是同一组的则只显示order最小的，
         * @param menuItemImpl
         * */
        @SuppressWarnings("deprecation")
        public void addViewByFilterGroudId(MenuItemImpl menuItemImpl)
        {
            if (mLayout != null)
            {
                mButtonLayout = mInflater.inflate(R.layout.menu_button_layout, null);

                ImageView imageView = (ImageView) mButtonLayout.findViewById(R.id.menu_btn_image);
                TextView textView = (TextView) mButtonLayout.findViewById(R.id.menu_btn_tital);
                imageView.setImageDrawable(menuItemImpl.getIcon());
                textView.setText(menuItemImpl.getTitle());
                textView.setTextColor(mButtonTextColor);

                mButtonLayout.setTag(menuItemImpl);
               /* if (mMenuButtonNormalBg != null)
                {
                    mButtonLayout.setBackgroundDrawable(mMenuButtonNormalBg);
                }*/

                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(SizeUtils.dp2px(mContext, 150),
                		SizeUtils.dp2px(mContext, 136));
                p.gravity = Gravity.CENTER;
                p.leftMargin = MENU_BUTTON_GAP;
                int childCount = mLayout.getChildCount();
                MenuItemImpl menuItem = null;
                if (childCount == 0)
                {
                    mLayout.addView(mButtonLayout, p);
                }
                else if (childCount > 0)// 同组button处理
                {
                    boolean hasSameGid = false;
                    for (int i = 0; i < childCount; i++)
                    {
                        menuItem = (MenuItemImpl) mLayout.getChildAt(i).getTag();
                        if (menuItemImpl.getGroupId() == menuItem.getGroupId())
                        {
                            hasSameGid = true;
                            if (menuItemImpl.getOrder() < menuItem.getOrder())
                            {
                                mLayout.removeView(mLayout.getChildAt(i));
                                mLayout.addView(mButtonLayout, p);
                            }
                        }
                    }
                    if (!hasSameGid)
                    {
                        mLayout.addView(mButtonLayout, p);
                    }
                }
                mButtonLayout.setOnTouchListener(mTouchListener);
            }
        }

        /**
         * 清空所有的Item项
         */
        public void clear()
        {
            mCurrentItemIndex = -1;
            mItems.clear();
            mLayout.removeAllViews();
            mbNeedLayout = true;

            this.requestLayout(); // 调用此方法，要求parent
                                  // view重新调用他的onMeasure,onLayout来对重新设置自己位置
        }

        /**
         * 按键监听事件
         */
        private OnKeyListener mKeyListener = new OnKeyListener()
        {
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                switch (keyCode)
                {
                    case KeyEvent.KEYCODE_VOLUME_UP:
                        if (mAudioManager == null)
                        {
                            Log.d(TAG, "audioManager == null, create a AudioManager object");
                            mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
                        }

                        mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FX_FOCUS_NAVIGATION_UP);

                        // 向Sender端回传媒体中心播放器的音量值
                        if (mVolumeKeyListener != null)
                        {
                            Log.d(TAG, "DLNAMenu report the volume to sender");
                            mVolumeKeyListener.reportVolumeToSender();
                        }
                        return true;

                    case KeyEvent.KEYCODE_VOLUME_DOWN:
                        if (mAudioManager == null)
                        {
                            Log.d(TAG, "mAudioManager == null, create a AudioManager object");
                            mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
                        }
                        mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FX_FOCUS_NAVIGATION_UP);

                        // 向Sender端回传媒体中心播放器的音量值
                        if (mVolumeKeyListener != null)
                        {
                            Log.d(TAG, "DLNAMenu report the volume to sender");
                            mVolumeKeyListener.reportVolumeToSender();
                        }
                        return true;
                    default:

                }

                // tangss add it to hide dialog
                if (isShowing() && event.getAction() == KeyEvent.ACTION_DOWN)
                {
                    if (keyCode == KeyEvent.KEYCODE_MENU)
                    {
                        hide();
                        return true;
                    }
                }

                if (isShowing() && event.getAction() == KeyEvent.ACTION_UP)
                {
                    if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER)
                    {
                        mHandler.removeMessages(MSG_UI_DISMISS);

                        mHandler.sendEmptyMessageDelayed(MSG_UI_DISMISS, DISMISS_TIME);
                        // 点击有效Item
                        clickFocusItem();
                        // 改变menuItem
                        changeItemMenu();
                        return true;
                    }
                    else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT)
                    {
                        if (mCurrentItemIndex > 0)
                        {
                            changeFocusItem(mCurrentItemIndex - 1);
                        }
                        return true;
                    }
                    else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
                    {
                        if (mCurrentItemIndex < mItems.size() - 1)
                        {
                            changeFocusItem(mCurrentItemIndex + 1);
                        }
                        return true;
                    }
                }
                dismissDelay(); // 按键重新计时隐藏菜单
                return false;
            }
        };

        /**
         * 点击监听事件
         */
        private OnTouchListener mTouchListener = new OnTouchListener()
        {
            public boolean onTouch(View v, MotionEvent event)
            {
                if (KeyEvent.ACTION_UP == event.getAction())
                {
                    int idx = -1;
                    int size = mLayout.getChildCount();
                    View child = null;
                    for (int i = 0; i < size; i++)
                    {
                        child = mLayout.getChildAt(i);
                        if (child == v)
                        {
                            idx = i;
                            break;
                        }
                        else if (child instanceof ViewGroup)
                        {
                            boolean findOk = false;
                            ViewGroup childGroup = (ViewGroup) child;
                            int count = childGroup.getChildCount();
                            for (int k = 0; k < count; k++)
                            {
                                if (childGroup.getChildAt(k) == v)
                                {
                                    findOk = true;
                                    break;
                                }
                            }
                            if (findOk)
                            {
                                idx = i;
                                break;
                            }
                        }
                    }

                    // changeFocusItem(idx);
                    setCurrentFocusItem(idx);

                    hide();
                    // 点击有效Item
                    clickFocusItem();
                    changeItemMenu();
                }
                return true;
            }
        };

        // 点击有效Item
        private void clickFocusItem()
        {
            if (mCurrentItemIndex >= 0 && mCurrentItemIndex < mItems.size())
            {
                MenuItemImpl menuItem = (MenuItemImpl) mLayout.getChildAt(mCurrentItemIndex).getTag();
                if (mOnSelectTypeListener != null)
                {
                    mOnSelectTypeListener.onSelectType(menuItem);
                }
            }
        }

        /**
         * 当menuItem 是由一组过滤条件组合成的时候，点击切换icon和文字提示
         */
        protected void changeItemMenu()
        {
            if (mLayout != null && mLayout.getChildCount() != 0)
            {
                ArrayList<MenuItemImpl> tempMenuItems = new ArrayList<MenuItemImpl>();
                View currentView = (View) mLayout.getChildAt(mCurrentItemIndex);
                MenuItemImpl currentItemImpl = (MenuItemImpl) currentView.getTag();
                for (MenuItemImpl itemImpl : mItems)
                {
                    if (itemImpl.getGroupId() == currentItemImpl.getGroupId())
                    {
                        tempMenuItems.add(itemImpl);
                    }
                }
                Collections.sort(tempMenuItems, new SortByOrder());
                MenuItemImpl nextItemImpl = null;
                int size = tempMenuItems.size();
                MenuItemImpl itemImpl = null;
                for (int i = 0; i < size; i++)
                {
                    itemImpl = tempMenuItems.get(i);
                    if (itemImpl.getOrder() > currentItemImpl.getOrder())
                    {
                        nextItemImpl = itemImpl;
                        break;
                    }
                    if (itemImpl.getOrder() == currentItemImpl.getOrder())
                    {
                        nextItemImpl = tempMenuItems.get(0);
                    }
                }
                if (nextItemImpl != null)
                {
                    ImageView imageView = (ImageView) currentView.findViewById(R.id.menu_btn_image);
                    TextView textView = (TextView) currentView.findViewById(R.id.menu_btn_tital);
                    imageView.setImageDrawable(nextItemImpl.getIcon());
                    textView.setText(nextItemImpl.getTitle());
                    currentView.setTag(nextItemImpl);
                }
            }

        }

        /**
         * 按小到大排序
         * */
        private class SortByOrder implements Comparator<MenuItemImpl>
        {
            @Override
            public int compare(MenuItemImpl o1, MenuItemImpl o2)
            {
                MenuItemImpl m1 = (MenuItemImpl) o1;
                MenuItemImpl m2 = (MenuItemImpl) o2;
                if (m1.getOrder() < m2.getOrder())
                {
                    return -1;
                }
                else
                {
                    return 0;
                }
            }
        }

        /**
         * 切换聚焦Item， 可实现动画效果
         * @param newIdx 新的聚焦item的Index
         */
        private void changeFocusItem(int newIdx)
        {
            if (mCurrentItemIndex == newIdx)
            {
                return;
            }
            View currentView = mLayout.getChildAt(mCurrentItemIndex);
            if (currentView != null)
            	currentView.setBackgroundColor(Color.TRANSPARENT);
            if (newIdx >= 0 && newIdx < mLayout.getChildCount())
            {
                if (mCurrentItemIndex >= 0 && mCurrentItemIndex < mLayout.getChildCount())
                {
                    setCurrentFocusItemByAnim(newIdx);
                }
                else
                {
                    setCurrentFocusItem(newIdx);
                }

            }
        }

        /**
         * 动画方式切换聚焦
         * @param newIdx：新的聚焦Item的Index
         */
        private void setCurrentFocusItemByAnim(int newIdx)
        {
            View currentView = mLayout.getChildAt(mCurrentItemIndex);
            View newView = mLayout.getChildAt(newIdx);
            //showFocusBackgroundAt(mCurrentItemIndex);
            int xoffset = newView.getLeft() - currentView.getLeft();
            int yoffset = newView.getTop() - currentView.getTop();
            // 做焦点移动的动画效果在此处实现
            mCurrentItemIndex = newIdx;
            showFocusBackgroundAt(mCurrentItemIndex);
            if (mOnMenuListener != null)
            {
                int id = mItems.get(mCurrentItemIndex).getItemId();
                mOnMenuListener.onItemFocusChanged(id);
            }

        }

        /**
         * ~{3#9f7=J=IhVC51G0>[=95D~}Item
         * @param newIdx
         */
        private void setCurrentFocusItem(int newIdx)
        {
            mCurrentItemIndex = newIdx;
            showFocusBackgroundAt(newIdx);
        }

        private void showFocusBackgroundAt(int index)
        {
            Log.d(TAG, "showFocusBackgroundAt:" + index);
            if (index >= 0 && mLayout.getChildCount() > 0)
            {
                if (!mLayout.isLayoutRequested())
                {
                    View currentView = mLayout.getChildAt(index);
                    if (currentView != null)
                    {
                    	currentView.setBackgroundColor(Color.parseColor("#19395B"));
                    }
                 
                }
                else
                {
                    // 正在布局菜单项，等布局完了再显示聚焦的光标
                    mbNeedLayout = true;
                }
            }
           
        }

        public int getCurrentMenuItem()
        {
            // 点击有效Item
            if (mCurrentItemIndex >= 0 && mCurrentItemIndex < mItems.size())
            {
                return mItems.get(mCurrentItemIndex).getItemId();
            }
            else
            {
                return -1;
            }
        }

        /**
         * 获取当前菜单所有Item项
         */
        public ArrayList<MenuItemImpl> getCurrentMenuItemImpl()
        {
            ArrayList<MenuItemImpl> menuItems = new ArrayList<MenuItemImpl>();
            if (mLayout != null)
            {
                int childCount = mLayout.getChildCount();
                MenuItemImpl itemImpl = null;
                for (int i = 0; i < childCount; i++)
                {
                    itemImpl = (MenuItemImpl) mLayout.getChildAt(i).getTag();
                    menuItems.add(itemImpl);
                }
            }
            return menuItems;
        }

        public void setCurrentMenuItem(int id)
        {
            int size = mItems.size();
            for (int i = 0; i < size; i++)
            {
                if (mItems.get(i).getItemId() == id)
                {
                    setCurrentFocusItem(i);
                    break;
                }
            }
        }

        /**
         * 必须重写此方法，requestLayout()才有效
         * */
        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b)
        {
            Log.d(TAG, "onLayout: changed:" + changed + ", mbNeedLayout" + mbNeedLayout);
            super.onLayout(changed, l, t, r, b);
            if (changed || mbNeedLayout)
            {
                showFocusBackgroundAt(mCurrentItemIndex);
                mbNeedLayout = false;
            }
        }

    }

    public void setOnSelectTypeListener(OnSelectTypeListener newVal)
    {
        mOnSelectTypeListener = newVal;
    }

    /**
     * 关闭菜单handler
     * @author s00211113
     * 
     */
    private class UIHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            switch (msg.what)
            {
                case MSG_UI_DISMISS:                    
                    hide();
                    break;

                default:
                    break;
            }
        }
    }

    /**
     * Menu处于焦点并按下音量键时的回调接口
     * @author s00211113
     * 
     */
    public interface VolumeKeyListener
    {
        // 向Sender端回传媒体中心播放器的音量
        void reportVolumeToSender();
    }

    public void setVolumeKeyListener(VolumeKeyListener mVolumeKeyListener)
    {
        Log.d(TAG, "setVolumeKeyListener()");
        this.mVolumeKeyListener = mVolumeKeyListener;
    }

    public int getBottomY()
    {
        return mBottomY;
    }

    public void setBottomY(int mBottomY)
    {
        this.mBottomY = mBottomY;
    }

    public int getBottomX()
    {
        return mBottomX;
    }

    public void setBottomX(int mBottomX)
    {
        this.mBottomX = mBottomX;
    }

}