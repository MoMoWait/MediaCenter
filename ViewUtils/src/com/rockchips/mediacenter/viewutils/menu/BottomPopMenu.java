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
 * SR-0000382162 底部弹出菜单
 * @author zwx160481
 * @version 1.0
 * 
 */

public class BottomPopMenu extends PopupWindow
{

    private static final String TAG = "BottomMenu";

    /**
     * 鑷姩闅愯棌鑿滃崟
     */
    private static final int MSG_UI_DISMISS = 101;

    /**
     * 鑷姩闅愯棌鏃堕棿 榛樿涓�5绉�
     */
    private static final int DISMISS_TIME = 3000;

    /**
     * 搴曢儴寮瑰嚭鑿滃崟鏍忛棿闅� px
     */
    private int mBottomY;

    /**
     * 搴曢儴寮瑰嚭鑿滃崟鏍忛棿闅� px
     */
    private int mBottomX;

    /**
     * 涓婁笅鏂�
     */
    private Context mContext;

    /**
     * UI涓荤嚎绋嬩笌瀛愮嚎绋嬩氦浜扝andler
     */
    private Handler mHandler = new UIHandler();

    private OnSelectTypeListener mOnSelectTypeListener;    

    /**
     * BaseActivity 瀹炵幇绫荤殑寮曠敤
     */
    // private OnSelectDisplayTypeListener mOnSelectDisplayTypeListener;

    /**
     * 鑿滃崟鏍忔寜閽竷灞�
     **/
    private View mButtonLayout;

    /**
     * 鎸夐挳涔嬮棿鐨勮窛绂� 榛樿涓�10dip
     **/
    private static final int MENU_BUTTON_GAP = 50;

    /**
     * menu 鎸夐挳瀛椾綋棰滆壊 鏈夐粯璁ゅ��
     */
    private int mButtonTextColor;

    /**
     * 鑿滃崟鏍忔寜閽甯哥姸鎬佹椂鐨勮儗鏅�,榛樿涓嶄负绌�
     */
    private BitmapDrawable mMenuButtonNormalBg;

    /**
     * 闊抽鎺у埗
     */
    private AudioManager mAudioManager;

    /**
     * 甯冨眬鏂囦欢瀹炰緥鍖�
     */
    private LayoutInflater mInflater;

    private MenuView mMenuView;

    private OnMenuListener mOnMenuListener;

    /**
     * 鑿滃崟鐩戝惉鍥炶皟鎺ュ彛
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
     * 鑿滃崟鏋勯�犲嚱鏁帮紝鍔犺浇榛樿鑳屾櫙鍜屽竷灞�
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
        this.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#292930"))); // 璁剧疆TabMenu鑿滃崟鑳屾櫙
        this.setWidth(width);
        //this.setHeight(drawbleBg.getBitmap().getHeight());
        //fly.gao
        this.setHeight(SizeUtils.dp2px(mContext, 136));
        //this.setAnimationStyle(R.style.MenuAnimation);
        this.setFocusable(true); // menu鑿滃崟鑾峰緱鐒︾偣 濡傛灉娌℃湁鑾峰緱鐒︾偣menu鑿滃崟涓殑鎺т欢浜嬩欢鏃犳硶鍝嶅簲

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
     * 澧炲姞鑿滃崟椤�
     * @param id Item鐨処D鏍囧織
     * @param selectType 鏍囪瘑item鐨勶紝璁剧疆鏋氫妇绫诲瀷
     * @param iconresId 璧勬簮鍥剧墖ID
     * @param groupId 鍒嗙粍id
     * @param order 鏍囧織鍚岀粍鐨勬樉绀洪『搴忕殑id 鏁板�艰秺灏忚秺闈犲墠鏄剧ず
     * @param title item鐨勬爣棰�
     * 
     * @return 鐢熸垚鐨処tem Menu
     */
    public MenuItem add(int id, Object selectType, int iconresId, int groupId, int order, String title)
    {
        return mMenuView.add(id, selectType, iconresId, groupId, order, title);
    }

    /**
     * 娣诲姞褰撳墠鏄剧ず鑿滃崟椤�
     */
    public void addCurrentMenu(ArrayList<MenuItemImpl> list)
    {
        mMenuView.addCurrentMenu(list);
    }

    /**
     * 璁℃椂鑷姩闅愯棌鑿滃崟
     */
    private void dismissDelay()
    {
        mHandler.removeMessages(MSG_UI_DISMISS);
        mHandler.sendEmptyMessageDelayed(MSG_UI_DISMISS, DISMISS_TIME);
    }

    /**
     * 鏄剧ず鑿滃崟
     * @param parent 鑿滃崟鏄剧ず闇�瑕佷竴涓猵arent锛屽缓璁槸Activity鐨勭涓�涓竷灞�View
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
     * 闅愯棌
     */
    public void hide()
    {
        dismiss();
    }

    /**
     * 瀹夊叏鐨刣ismisse
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
     * 璁剧疆褰撳墠鑿滃崟Item椤�
     */
    public void setCurrentMenuItem(int id)
    {
        mMenuView.setCurrentMenuItem(id);
    }

    /**
     * 鑾峰彇褰撳墠鑿滃崟Item椤�
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
     * 鑿滃崟椤硅閫夋嫨鐨勫洖璋冩帴鍙�
     * @author s00211113
     * 
     */
    public interface MenuItemSelectedListener
    {
        void onItemSelected(MenuItem item);
    }

    /**
     * MenuView甯冨眬
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
         * MenuView鏋勯�犲嚱鏁帮紝 鑿滃崟涓妋Layout瑁呰浇Item锛宮FocusAnimView鎵胯浇Item閫変腑鏃惰儗鏅�
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
         * 澧炲姞鑿滃崟椤�
         * @param id Item鐨処D鏍囧織
         * @param iconresId 璧勬簮鍥剧墖ID
         * @param groupId 鎵�灞炵粍id
         * @param order 鏄剧ず椤哄簭
         * @return 鐢熸垚鐨処tem Menu
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
                   /* if (mMenuButtonNormalBg != null)// 璁剧疆鎸夐挳姝ｅ父鐨勮儗鏅鑹�
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
         * 杩囨护娣诲姞MenuItem 濡傛灉鏄悓涓�缁勭殑鍒欏彧鏄剧ずorder鏈�灏忕殑锛�
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
                else if (childCount > 0)// 鍚岀粍button澶勭悊
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
         * 娓呯┖鎵�鏈夌殑Item椤�
         */
        public void clear()
        {
            mCurrentItemIndex = -1;
            mItems.clear();
            mLayout.removeAllViews();
            mbNeedLayout = true;

            this.requestLayout(); // 璋冪敤姝ゆ柟娉曪紝瑕佹眰parent
                                  // view閲嶆柊璋冪敤浠栫殑onMeasure,onLayout鏉ュ閲嶆柊璁剧疆鑷繁浣嶇疆
        }

        /**
         * 鎸夐敭鐩戝惉浜嬩欢
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

                        // 鍚慡ender绔洖浼犲獟浣撲腑蹇冩挱鏀惧櫒鐨勯煶閲忓��
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

                        // 鍚慡ender绔洖浼犲獟浣撲腑蹇冩挱鏀惧櫒鐨勯煶閲忓��
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
                        // 鐐瑰嚮鏈夋晥Item
                        clickFocusItem();
                        // 鏀瑰彉menuItem
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
                dismissDelay(); // 鎸夐敭閲嶆柊璁℃椂闅愯棌鑿滃崟
                return false;
            }
        };

        /**
         * 鐐瑰嚮鐩戝惉浜嬩欢
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
                    // 鐐瑰嚮鏈夋晥Item
                    clickFocusItem();
                    changeItemMenu();
                }
                return true;
            }
        };

        // 鐐瑰嚮鏈夋晥Item
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
         * 褰搈enuItem 鏄敱涓�缁勮繃婊ゆ潯浠剁粍鍚堟垚鐨勬椂鍊欙紝鐐瑰嚮鍒囨崲icon鍜屾枃瀛楁彁绀�
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
         * 鎸夊皬鍒板ぇ鎺掑簭
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
         * 鍒囨崲鑱氱劍Item锛� 鍙疄鐜板姩鐢绘晥鏋�
         * @param newIdx 鏂扮殑鑱氱劍item鐨処ndex
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
         * 鍔ㄧ敾鏂瑰紡鍒囨崲鑱氱劍
         * @param newIdx锛氭柊鐨勮仛鐒tem鐨処ndex
         */
        private void setCurrentFocusItemByAnim(int newIdx)
        {
            View currentView = mLayout.getChildAt(mCurrentItemIndex);
            View newView = mLayout.getChildAt(newIdx);
            //showFocusBackgroundAt(mCurrentItemIndex);
            int xoffset = newView.getLeft() - currentView.getLeft();
            int yoffset = newView.getTop() - currentView.getTop();
            // 鍋氱劍鐐圭Щ鍔ㄧ殑鍔ㄧ敾鏁堟灉鍦ㄦ澶勫疄鐜�
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
                    // 姝ｅ湪甯冨眬鑿滃崟椤癸紝绛夊竷灞�瀹屼簡鍐嶆樉绀鸿仛鐒︾殑鍏夋爣
                    mbNeedLayout = true;
                }
            }
           
        }

        public int getCurrentMenuItem()
        {
            // 鐐瑰嚮鏈夋晥Item
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
         * 鑾峰彇褰撳墠鑿滃崟鎵�鏈塈tem椤�
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
         * 蹇呴』閲嶅啓姝ゆ柟娉曪紝requestLayout()鎵嶆湁鏁�
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
     * 鍏抽棴鑿滃崟handler
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
     * Menu澶勪簬鐒︾偣骞舵寜涓嬮煶閲忛敭鏃剁殑鍥炶皟鎺ュ彛
     * @author s00211113
     * 
     */
    public interface VolumeKeyListener
    {
        // 鍚慡ender绔洖浼犲獟浣撲腑蹇冩挱鏀惧櫒鐨勯煶閲�
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