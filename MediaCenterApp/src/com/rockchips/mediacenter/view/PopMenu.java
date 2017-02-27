package com.rockchips.mediacenter.view;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import momo.cn.edu.fjnu.androidutils.data.CommonValues;
import momo.cn.edu.fjnu.androidutils.utils.SizeUtils;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
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
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.rockchips.mediacenter.R;
import com.rockchips.mediacenter.view.OnWheelChangedListener;
import com.rockchips.mediacenter.view.WheelView;
import com.rockchips.mediacenter.view.WheelView.OnItemFocusChangedListener;

/**
 * @author zwx160481
 * @version 1.0
 */
public class PopMenu extends Dialog
{
    private static final String TAG = "PopMenu";

    /**
     * 自动隐藏菜单
     */
    private static final int MSG_UI_DISMISS = 101;

    /**
     * 自动隐藏时间 默认为5秒
     */
    private static final int DISMISS_TIME = 5000;

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
     * 音频控制
     */
    private AudioManager mAudioManager;

    /**
     * 布局文件实例化
     */
    private LayoutInflater mInflater;

    /**
     * popmenu 最外层布局
     * */
    private LinearLayout mLayoutContainer;

    /**
     * 承载menu内容的类
     */
    private MenuView mMenuView;

    /**
     * 菜单栏按钮布局
     **/
    private View mButtonLayout;

    /**
     * 按钮之间的距离 默认为10dip
     **/
    private static final int MENU_BUTTON_GAP = 10;

    /**
     * 菜单栏背景图
     */
    private BitmapDrawable mMenuOptionBg;

    /**
     * 菜单栏底部布局
     */
    public LinearLayout mLayout;

    /**
     * 菜单栏按钮选中时的背景,默认不为空
     */
    private BitmapDrawable mMenuButtonSelectedBg;

    /**
     * 菜单栏按钮正常状态时的背景,默认不为空
     */
    private BitmapDrawable mMenuButtonNormalBg;

    /**
     * menu 按钮字体颜色 有默认值
     */
    private int mButtonTextColor;

    private Window mWwindow;

    private OnMenuListener mOnMenuListener;

    /**
     * 菜单栏左边背景图
     */
    private BitmapDrawable mMenuItemLeftBg;

    /**
     * 菜单栏中间背景图
     */
    private BitmapDrawable mMenuItemMiddleBg;

    /**
     * 菜单栏中间背景图
     */
    private BitmapDrawable mMenuItemRightBg;

    /**
     * 菜单栏一个分类时的背景图
     */
    private BitmapDrawable mMenuItemSingleBg;

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

    public PopMenu(Context context)
    {
        super(context);

        init(context);
    }

    public PopMenu(Context context, int theme)
    {
        super(context, theme);

        init(context);
    }

    @SuppressWarnings("deprecation")
    private void init(Context context)
    {
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        mContext = context;
        mInflater = LayoutInflater.from(context);

        mMenuOptionBg = (BitmapDrawable) context.getResources().getDrawable(R.drawable.option_bg);
        mMenuButtonSelectedBg = (BitmapDrawable) context.getResources().getDrawable(R.drawable.menu_btn_bg_selected);
        mMenuButtonNormalBg = (BitmapDrawable) context.getResources().getDrawable(R.drawable.menu_btn_bg_normal);
        mButtonTextColor = context.getResources().getColor(R.color.menu_btn_tital_color);

        mLayoutContainer = (LinearLayout) mInflater.inflate(R.layout.menu_pop_layout, null, true);
        mLayoutContainer.setFocusable(true);
        // mLayoutContainer.setFocusableInTouchMode(true);
        mLayoutContainer.requestFocus();
        mLayoutContainer.setBackgroundDrawable(new BitmapDrawable(mContext.getResources())); // 必须加上此句，不然阴影会变深

        mMenuView = new MenuView(mContext);
        mLayoutContainer.addView(mMenuView);
        this.setContentView(mLayoutContainer);

        mMenuView.setFocusable(true);
        mMenuView.requestFocus();

        if (mAudioManager == null)
        {
            Log.d(TAG, "audioManager == null, create a AudioManager object");
            mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        }
    }

    private void showDialog()
    {

        windowDeploy();

        // 设置触摸对话框意外的地方取消对话框
        setCanceledOnTouchOutside(true);
        show();
    }

    // 设置窗口显示
    @SuppressLint("InlinedApi")
    private void windowDeploy()
    {
        mWwindow = getWindow(); // 得到对话框
        mWwindow.setBackgroundDrawableResource(R.color.transparent_color); // 设置对话框背景为透明
        WindowManager.LayoutParams wl = mWwindow.getAttributes();
        wl.gravity = Gravity.CENTER; // 设置重力
        wl.width = LayoutParams.MATCH_PARENT;
        mWwindow.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        mWwindow.getDecorView().setSystemUiVisibility(View.INVISIBLE);
        mWwindow.setAttributes(wl);
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

    public void addMenuCategory(MenuCategory category)
    {
        try
        {
            mMenuView.addMenuCategory(category);
        }
        catch (IOException e)
        {
        }
        catch (Resources.NotFoundException e)
        {
            Log.w(TAG, e.getLocalizedMessage());
        }
    }

    /**
     * 恢复上次选中的item
     */
    public void replayLastSelected()
    {
        mMenuView.replayLastSelectItem();
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
        showDialog();
        dismissDelay();
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

    /**
     * <一句话功能简述>获取当前焦点项 <功能详细描述>
     * @return
     * @see [类、类#方法、类#成员]
     */
    public MenuItemImpl getCurrentFocusItemImpl()
    {
        return mMenuView.getCurrentFocusItem();
    }

    /**
     * 获取当前菜单所有Item项
     */
    public ArrayList<MenuItemImpl> getCurrentMenuItemImpl()
    {
        return mMenuView.getCurrentMenuItemImpl();
    }

    /**
     * 清空所有的Item项
     */
    public void clear()
    {
        mMenuView.clear();
    }

    /**
     * 菜单被选择监听回调
     * @author s00211113
     * 
     */
    public interface MenuItemSelectedListener
    {
        void onItemSelected(MenuItem item);
    }

    /**
     * 内部类，menu的布局内容
     * @author s00211113
     * 
     */
    private final class MenuView extends RelativeLayout implements OnWheelChangedListener, OnKeyListener, OnClickListener
    {
        private int LAYOUTPARAMS_WIDTH = SizeUtils.dp2px(CommonValues.application, 258);

        private int LAYOUTPARAMS_HIGHT = SizeUtils.dp2px(CommonValues.application, 493);

        private ArrayList<MenuItemImpl> mItems;

        private ArrayList<MenuCategory> mCategorys;

        private int mCurrentItemIndex = -1;

        private ImageView mFocusAnimView;

        private boolean mbNeedLayout;

        /**
         * MenuView构造函数， 菜单上mLayout装载Item，mFocusAnimView承载Item选中时背景
         * @param context
         */
        private MenuView(Context context)
        {
            super(context);
            this.setFocusable(true);
            this.setFocusableInTouchMode(true);
            this.setOnKeyListener(mKeyListener);
            mbNeedLayout = false;
            mItems = new ArrayList<MenuItemImpl>();
            mCategorys = new ArrayList<MenuCategory>();
            mLayout = new LinearLayout(context);
            mLayout.setOrientation(LinearLayout.HORIZONTAL);
            mLayout.setGravity(Gravity.CENTER);
            mLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            this.setGravity(Gravity.CENTER);

            mFocusAnimView = (ImageView) mInflater.inflate(R.layout.menu_focus_imageview, null);
            this.addView(mLayout);
            this.addView(mFocusAnimView);
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
            // this.requestLayout();
            return item;
        }

        /**
         * 添加菜单分类
         * @throws Exception
         * @throws OutOfMemoryError
         */
        public void addMenuCategory(MenuCategory category) throws IOException, Resources.NotFoundException
        {
            mCategorys.add(category);
            addCategoryView(category);
            if (mCurrentItemIndex == -1)
            {
                changeFocusItem(0);
            }
            mbNeedLayout = true;
        }

        /**
         * 添加分类View
         * @throws Exception
         * @throws OutOfMemoryError
         */
        private void addCategoryView(MenuCategory category) throws IOException, Resources.NotFoundException
        {
            LinearLayout menuItemLayout = (LinearLayout) mInflater.inflate(R.layout.menu_item, null);
            TextView cateName = (TextView) menuItemLayout.findViewById(R.id.menu_category_name);
            cateName.setText(category.getCategoryName());
          /*  WheelView wheelView = (WheelView) menuItemLayout.findViewById(R.id.type);
            int size = category.getMenuItems().size();
            wheelView.setVisibleItems(size);
            MenuItemAdapter mTypeAdapter = new MenuItemAdapter(mContext, category.getMenuItems());
            wheelView.setViewAdapter(mTypeAdapter);
            wheelView.setFocusable(true);
            wheelView.addChangingListener(this);
            wheelView.setOnKeyListener(mKeyListener);
            wheelView.setCurrentItem(category.getSelectIndex());
            wheelView.setCyclic(true);
            wheelView.setOnTouchListener(mTouchListener);

            wheelView.setTag(R.id.menu_select_index_tag, category.getSelectIndex());
            setViewHeight(wheelView, size);
            if (category.isMbItemFocusChanged())
            {
                wheelView.setOnItemFocusChangedListener(mItemFocusChangedListener);
            }
            else
            {
                wheelView.setTag(R.id.menu_favrite_tag, category.getMenuItems());
            }
            RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(LAYOUTPARAMS_WIDTH, LAYOUTPARAMS_HIGHT);
            mLayout.addView(menuItemLayout, p);
            setMenuBg();*/
        }

        private static final int MINHEIGHT_1 = 70;

        private static final int TOPMARGIN_1 = 155;

        private static final int MINHEIGHT_2 = 135;

        private static final int TOPMARGIN_2 = 75;

        private static final int MINHEIGHT_3 = 216;

        private static final int MINHEIGHT_4 = 255;

        private static final int TOPMARGIN_4 = 23;

        private static final int MINHEIGHT_5 = 340;

        private static final int TOPMARGIN_5 = 15;

        private static final int MINHEIGHT_DEFAULT = 340;

        private static final int TOPMARGIN_DEFAULT = 15;

        private static final int SIZE_1 = 1;

        private static final int SIZE_2 = 2;

        private static final int SIZE_3 = 3;

        private static final int SIZE_4 = 4;

        private static final int SIZE_5 = 5;

        private void setViewHeight(WheelView wheelView, int size)
        {
            if (wheelView == null)
            {
                return;
            }

            LayoutParams layoutParams = null;

            if (size == SIZE_1)
            {
                wheelView.setMinimumHeight(MINHEIGHT_1);
                layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                layoutParams.topMargin = TOPMARGIN_1;
                wheelView.setLayoutParams(layoutParams);
            }
            else if (size == SIZE_2)
            {
                wheelView.setMinimumHeight(MINHEIGHT_2);
                layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                layoutParams.topMargin = TOPMARGIN_2;
                wheelView.setLayoutParams(layoutParams);
            }
            else if (size == SIZE_3)
            {
                wheelView.setMinimumHeight(MINHEIGHT_3);
                layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                wheelView.setLayoutParams(layoutParams);
            }
            else if (size == SIZE_4)
            {
                layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                wheelView.setMinimumHeight(MINHEIGHT_4);
                layoutParams.topMargin = TOPMARGIN_4;
                wheelView.setLayoutParams(layoutParams);
            }
            else if (size == SIZE_5)
            {
                wheelView.setMinimumHeight(MINHEIGHT_5);
                layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                layoutParams.topMargin = TOPMARGIN_5;
                wheelView.setLayoutParams(layoutParams);
            }
            else
            {
                layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, MINHEIGHT_DEFAULT);
                layoutParams.topMargin = TOPMARGIN_DEFAULT;
                wheelView.setLayoutParams(layoutParams);
            }
        }

        /**
         * 恢复上次选中的item
         */
        private void replayLastSelectItem()
        {
            do
            {
                if (mLayout == null)
                {
                    break;
                }

                int count = mLayout.getChildCount();
                if (count <= 0)
                {
                    break;
                }

                LinearLayout menuItemLayout = null;
                WheelView wheelView = null;
                for (int i = 0; i < count; i++)
                {
                    menuItemLayout = (LinearLayout) mLayout.getChildAt(i);
                    if (menuItemLayout != null)
                    {
                       // wheelView = (WheelView) menuItemLayout.findViewById(R.id.type);
                        if (wheelView.getTag(R.id.menu_select_index_tag) != null)
                        {
                            int lastSelected = (Integer) (wheelView.getTag(R.id.menu_select_index_tag));
                            if (lastSelected >= 0)
                            {
                                wheelView.setCurrentItem(lastSelected);
                            }
                        }
                    }
                }
            }
            while (false);
        }

        private static final int MENU_BG_WIDTH_1 = 352;

        private static final int PADDING_LEFT_1 = 70;

        private static final int PADDING_TOP_1 = 0;

        private static final int PADDING_RIGHT_1 = 70;

        private static final int PADDING_BOTTOM_1 = 20;

        private static final int MENU_BG_WIDTH_2 = 250;

        private static final int PADDING_LEFT_2 = 48;

        private static final int PADDING_TOP_2 = 0;

        private static final int PADDING_RIGHT_2 = 0;

        private static final int PADDING_BOTTOM_2 = 20;

        private static final int MENU_BG_WIDTH_3 = 250;

        private static final int PADDING_LEFT_3 = 48;

        private static final int PADDING_TOP_3 = 0;

        private static final int PADDING_RIGHT_3 = 0;

        private static final int PADDING_BOTTOM_3 = 20;

        private static final int MENU_BG_WIDTH_MIDDLE_3 = 202;

        private static final int PADDING_LEFT_MIDDLE_3 = 0;

        private static final int PADDING_RIGHT_MIDDLE_3 = 0;
        
        private static final int MENU_COUNT_1 = 1;
        
        private static final int MENU_COUNT_2 = 2;
        
        private static final int MENU_COUNT_3 = 3;

        @SuppressWarnings("deprecation")
        private void setMenuBg() throws IOException, Resources.NotFoundException
        {
            if (mLayout != null)
            {
                int count = mLayout.getChildCount();
                LinearLayout layout = null;
                LinearLayout.LayoutParams p = null;

                if (count == MENU_COUNT_1)
                {
                    layout = (LinearLayout) mLayout.getChildAt(0);
                    if (mMenuItemSingleBg == null)
                    {
                        mMenuItemSingleBg = readBitmap(mContext, R.drawable.menu_bg);
                    }
                    p = (LinearLayout.LayoutParams) layout.getLayoutParams();
                    p.width = MENU_BG_WIDTH_1;
                    layout.setLayoutParams(p);
                    layout.setBackgroundDrawable(mMenuItemSingleBg);
                    layout.setPadding(PADDING_LEFT_1, PADDING_TOP_1, PADDING_RIGHT_1, PADDING_BOTTOM_1);

                }
                else if (count == MENU_COUNT_2)
                {
                    layout = (LinearLayout) mLayout.getChildAt(0);
                    if (mMenuItemLeftBg == null)
                    {
                        mMenuItemLeftBg = readBitmap(mContext, R.drawable.menu_item_left_bg);
                    }
                    p = (LinearLayout.LayoutParams) layout.getLayoutParams();
                    p.width = MENU_BG_WIDTH_2;
                    layout.setLayoutParams(p);
                    layout.setBackgroundDrawable(mMenuItemLeftBg);
                    layout.setPadding(PADDING_LEFT_2, PADDING_TOP_2, PADDING_RIGHT_2, PADDING_BOTTOM_2);

                    layout = (LinearLayout) mLayout.getChildAt(1);
                    if (mMenuItemRightBg == null)
                    {
                        mMenuItemRightBg = readBitmap(mContext, R.drawable.menu_item_right_bg);
                    }
                    p = (LinearLayout.LayoutParams) layout.getLayoutParams();
                    p.width = MENU_BG_WIDTH_2;
                    layout.setLayoutParams(p);
                    layout.setBackgroundDrawable(mMenuItemRightBg);
                    layout.setPadding(PADDING_RIGHT_2, PADDING_TOP_2, PADDING_LEFT_2, PADDING_BOTTOM_2);
                }
                /* BEGIN: Added by r00178559 for AR-0000698413 2014/02/13 */
                else if (count >= MENU_COUNT_3)
                {
                    layout = (LinearLayout) mLayout.getChildAt(0);
                    if (mMenuItemLeftBg == null)
                    {
                        mMenuItemLeftBg = readBitmap(mContext, R.drawable.menu_item_left_bg);
                    }
                    p = (LinearLayout.LayoutParams) layout.getLayoutParams();
                    p.width = MENU_BG_WIDTH_3;
                    layout.setLayoutParams(p);
                    layout.setBackgroundDrawable(mMenuItemLeftBg);
                    layout.setPadding(PADDING_LEFT_3, PADDING_TOP_3, PADDING_RIGHT_3, PADDING_BOTTOM_3);

                    for (int i = 1; i < count - 1; i++)
                    {
                        layout = (LinearLayout) mLayout.getChildAt(i);
                        if (mMenuItemMiddleBg == null)
                        {
                            mMenuItemMiddleBg = readBitmap(mContext, R.drawable.menu_item_middle_bg);
                        }
                        p = (LinearLayout.LayoutParams) layout.getLayoutParams();
                        p.width = MENU_BG_WIDTH_MIDDLE_3;
                        layout.setLayoutParams(p);
                        layout.setBackgroundDrawable(mMenuItemMiddleBg);
                        layout.setPadding(PADDING_LEFT_MIDDLE_3, PADDING_TOP_3, PADDING_RIGHT_MIDDLE_3, PADDING_BOTTOM_3);
                    }

                    layout = (LinearLayout) mLayout.getChildAt(count - 1);
                    if (mMenuItemRightBg == null)
                    {
                        mMenuItemRightBg = readBitmap(mContext, R.drawable.menu_item_right_bg);
                    }
                    p = (LinearLayout.LayoutParams) layout.getLayoutParams();
                    p.width = MENU_BG_WIDTH_3;
                    layout.setLayoutParams(p);
                    layout.setBackgroundDrawable(mMenuItemRightBg);
                    layout.setPadding(PADDING_RIGHT_3, PADDING_TOP_3, PADDING_LEFT_3, PADDING_BOTTOM_3);
                }
            }
        }

        /**
         * 添加当前在显示的menuItem
         */
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
                    if (mMenuButtonNormalBg != null)// 设置按钮正常的背景颜色
                    {
                        mButtonLayout.setBackgroundDrawable(mMenuButtonNormalBg);
                    }
                    p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    p.gravity = Gravity.CENTER;
                    p.leftMargin = MENU_BUTTON_GAP;
                    mLayout.addView(mButtonLayout, p);
                    mButtonLayout.setOnTouchListener(mTouchListener);
                    if (i == mCurrentItemIndex && mMenuButtonSelectedBg != null)
                    {
                        // 设置选中的按钮背景颜色
                        mButtonLayout.setBackgroundDrawable(mMenuButtonSelectedBg);
                    }
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
                if (mMenuButtonNormalBg != null)
                {
                    mButtonLayout.setBackgroundDrawable(mMenuButtonNormalBg);
                }

                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
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
            mFocusAnimView.setVisibility(View.GONE);

            mCurrentItemIndex = 0;

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
                        hide(); // 菜单可见，再按键Menu,隐藏菜单
                        return true;
                    }
                }

                if (isShowing() && event.getAction() == KeyEvent.ACTION_UP)
                {
                    if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER)
                    {
                        mHandler.removeMessages(MSG_UI_DISMISS);
                        if (mLayout != null && mLayout.getChildCount() > 0 && mLayout.getChildAt(mCurrentItemIndex) != null)
                        {
                            mHandler.sendEmptyMessage(MSG_UI_DISMISS);
                        }

                        // 点击有效Item
                        clickFocusItem();

                        return true;
                    }
                }

                if (isShowing() && event.getAction() == KeyEvent.ACTION_DOWN)
                {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT)
                    {
                        if (mCurrentItemIndex > 0)
                        {
                            changeFocusItem(mCurrentItemIndex - 1);
                        }
                        return true;
                    }
                    else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
                    {
                        if (mCurrentItemIndex < mCategorys.size() - 1)
                        {
                            changeFocusItem(mCurrentItemIndex + 1);
                        }
                        return true;
                    }
                    else if (keyCode == KeyEvent.KEYCODE_DPAD_UP)
                    {
                        Log.d(TAG, "KeyEvent.KEYCODE_DPAD_UP");
                        LinearLayout layout = (LinearLayout) mLayout.getChildAt(mCurrentItemIndex);
                        //WheelView view = (WheelView) layout.findViewById(R.id.type);
                        //int cuItem = view.getCurrentItem();
                        //cuItem -= 1;
                        //view.setCurrentItem(cuItem, true);
                    }
                    else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN)
                    {
                        Log.d(TAG, "KeyEvent.KEYCODE_DPAD_DOWN");
                        LinearLayout layout = (LinearLayout) mLayout.getChildAt(mCurrentItemIndex);
                        //WheelView view = (WheelView) layout.findViewById(R.id.type);
                        //int cuItem = view.getCurrentItem();
                        //cuItem += 1;
                        //view.setCurrentItem(cuItem, true);
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
                WheelView wheelView = null;
                WheelView tempWheelView = null;
                int size = mLayout.getChildCount();
                LinearLayout menuItemLayout = null;
                for (int i = 0; i < size; i++)
                {
                    menuItemLayout = (LinearLayout) mLayout.getChildAt(i);
                    if (menuItemLayout != null)
                    {
                        //tempWheelView = (WheelView) menuItemLayout.findViewById(R.id.type);
                        if (tempWheelView == v)
                        {
                            wheelView = tempWheelView;
                            break;
                        }
                    }
                }

                switch (event.getAction())
                {
                    case MotionEvent.ACTION_MOVE:
                        if (getParent() != null)
                        {
                            Log.d(TAG, "MotionEvent.ACTION_MOVE-->");
                            getParent().requestDisallowInterceptTouchEvent(true);
                        }
                        break;
                    case KeyEvent.ACTION_DOWN:
                        Log.d(TAG, "MotionEvent.ACTION_DOWN-->");
                        break;

                    case KeyEvent.ACTION_UP:
                        Log.d(TAG, "MotionEvent.ACTION_UP-->1111");
                        if (wheelView != null && !wheelView.isScrollingPerformed())
                        {
                            Log.d(TAG, "MotionEvent.ACTION_UP-->2222");
                            hide();
                            // 点击有效Item
                            clickFocusItem();
                        }
                        break;
                    default:
                        break;
                }

                if (wheelView != null)
                {
                    Log.d(TAG, "wheelView.onTouchEvent-->1111");
                    dismissDelay();
                    return wheelView.onTouchEvent(event);
                }
                else
                {
                    return true;
                }

            }
        };

        /**
         * 当前选中改变时监听
         */
        private OnItemFocusChangedListener mItemFocusChangedListener = new OnItemFocusChangedListener()
        {

            @Override
            public void onFocusChange(View view, int position, boolean hasFocus)
            {
                if (view == null)
                {
                    return;
                }
                if (hasFocus)
                {
                    if (mLayout.getChildCount() < 2)
                    {
                        return;
                    }
                    LinearLayout layout = (LinearLayout) mLayout.getChildAt(1);
                    if (layout == null)
                    {
                        return;
                    }

                   // WheelView wheelView = (WheelView) layout.findViewById(R.id.type);

                   /* if (wheelView == null)
                    {
                        return;
                    }
                    if (wheelView.getTag(R.id.menu_favrite_tag) == null)
                    {
                        return;
                    }*/
                }
            }

        };

        // 点击有效Item 所有menuItem都会生效
        private void clickFocusItem()
        {
            if (mCurrentItemIndex >= 0 && mCurrentItemIndex < mCategorys.size())
            {
                int count = mLayout.getChildCount();
                LinearLayout layout = null;
                WheelView wheelView = null;
                MenuItemAdapter mwAdapter = null;
                MenuItemImpl menuItem = null;

                for (int i = 0; i < count; i++)
                {
                    layout = (LinearLayout) mLayout.getChildAt(i);
                    //wheelView = (WheelView) layout.findViewById(R.id.type);
                    //wheelView.setTag(R.id.menu_select_index_tag, wheelView.getCurrentItem());
                    mwAdapter = (MenuItemAdapter) wheelView.getViewAdapter();
                    if (wheelView.getCurrentItem() < mwAdapter.getTypeItemList().size())
                    {
                        menuItem = mwAdapter.getTypeItemList().get(wheelView.getCurrentItem());
                    }

                    if (mOnSelectTypeListener != null && null != menuItem)
                    {
                        mOnSelectTypeListener.onSelectType(menuItem);
                    }
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
         * 改变分类名字体颜色
         * @param i 选中的索引
         */
        private void changeCategoryNameColor(int i)
        {
            if (mLayout != null)
            {
                int count = mLayout.getChildCount();
                LinearLayout layout = null;
                TextView textView = null;
                ImageView imageView = null;
                for (int j = 0; j < count; j++)
                {
                    layout = (LinearLayout) mLayout.getChildAt(j);
                    textView = (TextView) layout.findViewById(R.id.menu_category_name);
                    imageView = (ImageView) layout.findViewById(R.id.iv_menu_item_line);
                    if (imageView != null && textView != null)
                    {
                        if (j == i)
                        {
                            textView.setTextColor(getResources().getColor(R.color.menu_category_color_s));
                            imageView.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            textView.setTextColor(getResources().getColor(R.color.menu_category_color_uns));
                            imageView.setVisibility(View.INVISIBLE);
                        }    
                    }
                }
            }
        }

        private static final int OFFSET_OFFSET = 48;

        private static final int ANIMATION_DURATION = 200;

        /**
         * 动画方式切换聚焦
         * @param newIdx：新的聚焦Item的Index
         */
        private void setCurrentFocusItemByAnim(int newIdx)
        {
            mFocusAnimView.clearAnimation();
            final int myNexIdx = newIdx;
            View currentView = mLayout.getChildAt(mCurrentItemIndex);
            View newView = mLayout.getChildAt(newIdx);

            showFocusBackgroundAt(mCurrentItemIndex);

            int xoffset = newView.getLeft() - currentView.getLeft();
            if (newIdx == 0)
            {
                xoffset = xoffset + OFFSET_OFFSET;
            }
            if (mCurrentItemIndex == 0)
            {
                xoffset = xoffset - OFFSET_OFFSET;
            }
            int yoffset = newView.getTop() - currentView.getTop();
            // 做焦点移动的动画效果在此处实现
            mCurrentItemIndex = newIdx;
            showFocusBackgroundAt(mCurrentItemIndex);
            Animation focusAnimation = new TranslateAnimation(-xoffset, 0, -yoffset, 0);
            focusAnimation.setDuration(ANIMATION_DURATION);
            focusAnimation.setAnimationListener(new AnimationListener()
            {
                public void onAnimationStart(Animation animation)
                {

                }

                public void onAnimationRepeat(Animation animation)
                {

                }

                public void onAnimationEnd(Animation animation)
                {
                    if (mOnMenuListener != null)
                    {
                        int id = mItems.get(mCurrentItemIndex).getItemId();
                        mOnMenuListener.onItemFocusChanged(id);
                        // 改变按钮背景

                    }
                    changeCategoryNameColor(myNexIdx);
                }
            });
            // 开始动画
            mFocusAnimView.startAnimation(focusAnimation);
        }

        /**
         * 常规方式设置当前聚焦的Item
         * @param newIdx
         */
        private void setCurrentFocusItem(int newIdx)
        {
            showFocusBackgroundAt(newIdx);
            mCurrentItemIndex = newIdx;
        }

        private static final int MARGIN_LEFT_PLUS_1 = 70;

        private static final int MARGIN_LEFT_PLUS_2 = 48;

        private void showFocusBackgroundAt(int index)
        {
            Log.d(TAG, "showFocusBackgroundAt:" + index);
            if (index >= 0 && mLayout.getChildCount() > 0)
            {

                if (mCurrentItemIndex == -1)// 首次弹出menu时将第一个button背景置为选中状态
                {
                    View currentView = mLayout.getChildAt(index);
                    TextView textView = (TextView) currentView.findViewById(R.id.menu_category_name);
                    textView.setTextColor(getResources().getColor(R.color.menu_category_color_s));
                    ImageView imageView = (ImageView) currentView.findViewById(R.id.iv_menu_item_line);
                    imageView.setVisibility(View.VISIBLE);
                }
                if (!mLayout.isLayoutRequested())
                {
                    View currentView = mLayout.getChildAt(index);
                    if (currentView == null)
                    {
                        mFocusAnimView.setVisibility(View.GONE);
                        return;
                    }
                    
                    //View imageView = currentView.findViewById(R.id.typeRectBox);
                    View imageView = new View(mContext);
                    //View rlView = currentView.findViewById(R.id.rl_menu_category);
                    View rlView = new View(mContext);
                    RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) mFocusAnimView.getLayoutParams();

                    p.leftMargin = imageView.getLeft() + currentView.getLeft();
                    p.topMargin = (int) imageView.getTop() + currentView.getTop() + rlView.getTop();
                    p.height = imageView.getHeight();
                    p.width = imageView.getWidth();

                    if (index == 0)
                    {
                        if (mLayout.getChildCount() == 1)
                        {
                            p.leftMargin = p.leftMargin + MARGIN_LEFT_PLUS_1;
                        }
                        else
                        {
                            p.leftMargin = p.leftMargin + MARGIN_LEFT_PLUS_2;
                        }
                    }

                    mFocusAnimView.setLayoutParams(p);
                    mFocusAnimView.setVisibility(View.VISIBLE);
                }
                else
                {
                    // 正在布局菜单项，等布局完了再显示聚焦的光标
                    mbNeedLayout = true;
                    mFocusAnimView.setVisibility(View.GONE);
                }
            }
            else
            {
                mFocusAnimView.setVisibility(View.GONE);
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

        // 获取焦点项目
        public MenuItemImpl getCurrentFocusItem()
        {
            MenuItemImpl menuItem = null;
            if (mCurrentItemIndex >= 0 && mCurrentItemIndex < mCategorys.size())
            {
                LinearLayout layout = (LinearLayout) mLayout.getChildAt(mCurrentItemIndex);
                //WheelView wheelView = (WheelView) layout.findViewById(R.id.type);
                //MenuItemAdapter mwAdapter = (MenuItemAdapter) wheelView.getViewAdapter();
                //menuItem = mwAdapter.getTypeItemList().get(wheelView.getCurrentItem());
            }
            return menuItem;
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
            Log.d(TAG, "onLayout: changed:" + changed + ", mbNeedLayout:" + mbNeedLayout);
            super.onLayout(changed, l, t, r, b);
            if (changed || mbNeedLayout)
            {
                showFocusBackgroundAt(mCurrentItemIndex);
                mbNeedLayout = false;
            }
        }

        @Override
        public void onClick(DialogInterface dialog, int which)
        {
        }

        @Override
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event)
        {
            return false;
        }

        @Override
        public void onChanged(WheelView wheel, int oldValue, int newValue)
        {
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

    public BitmapDrawable getMenuOptionBg()
    {
        return mMenuOptionBg;
    }

    public LinearLayout getLayoutContainer()
    {
        return mLayoutContainer;
    }

    /**
     * 设置底部menu背景
     * */
    @SuppressWarnings("deprecation")
    public void setMenuOptionBg(BitmapDrawable mMenuOptionBg)
    {
        if (mLayout != null)
        {
            mLayout.setBackgroundDrawable(mMenuOptionBg);
        }
    }

    public BitmapDrawable getMenuButtonSelectedBg()
    {
        return mMenuButtonSelectedBg;
    }

    /**
     * 设置菜单按钮选中时的背景，当设置为空时，将不显示选中背景
     * @param mMenuButtonNormalBg 图片
     */
    public void setMenuButtonSelectedBg(BitmapDrawable mMenuButtonSelectedBg)
    {
        this.mMenuButtonSelectedBg = mMenuButtonSelectedBg;
    }

    public BitmapDrawable getMenuButtonNormalBg()
    {
        return mMenuButtonNormalBg;
    }

    /**
     * 设置菜单按钮正常状态时的背景，
     * @param mMenuButtonNormalBg 图片
     */
    public void setMenuButtonNormalBg(BitmapDrawable mMenuButtonNormalBg)
    {
        this.mMenuButtonNormalBg = mMenuButtonNormalBg;
    }

    /**
     * 设置菜单的整体背景颜色，是否显示阴影
     * @param color 颜色值
     */
    public void setMenuBackgroundColor(int color)
    {
        if (mLayoutContainer != null)
        {
            mLayoutContainer.setBackgroundColor(color);
        }
    }

    public int getButtonTextColor()
    {
        return mButtonTextColor;
    }

    /**
     * 设置字体按钮字体颜色
     * @param textColor 字体颜色
     */
    public void setButtonTextColor(int mButtonTextColor)
    {
        this.mButtonTextColor = mButtonTextColor;
    }

    /**
     * 以最省内存的方式读取本地资源的图片
     * 
     * @param context
     * @param resId
     * @return
     */
    public static BitmapDrawable readBitmap(Context context, int resId) throws IOException, Resources.NotFoundException
    {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        // 获取资源图片
        InputStream is = context.getResources().openRawResource(resId);
        Bitmap bm = BitmapFactory.decodeStream(is, null, opt);
        BitmapDrawable bd = new BitmapDrawable(context.getResources(), bm);
        is.close();
        Log.d(TAG, "bd===" + bd);
        return bd;
    }

}
