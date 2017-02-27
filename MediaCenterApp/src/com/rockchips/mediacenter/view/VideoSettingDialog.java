package com.rockchips.mediacenter.view;

import java.io.IOException;
import java.util.ArrayList;

import momo.cn.edu.fjnu.androidutils.utils.DeviceInfoUtils;
import momo.cn.edu.fjnu.androidutils.utils.ResourceUtils;
import momo.cn.edu.fjnu.androidutils.utils.SizeUtils;

import org.xutils.view.annotation.ViewInject;

import android.R.anim;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rockchips.mediacenter.R;
import com.rockchips.mediacenter.view.MenuCategory;
import com.rockchips.mediacenter.view.MenuItemAdapter;
import com.rockchips.mediacenter.view.MenuItemImpl;
import com.rockchips.mediacenter.view.OnSelectTypeListener;
import com.rockchips.mediacenter.view.WheelView;
/**
 * @author GaoFei 视频设置页面对话框
 */
public class VideoSettingDialog extends AppBaseDialog {

	public static final String TAG = VideoSettingDialog.class.getSimpleName();
	/**
	 * MenuItem Text Size
	 */
	public static final int MENU_ITEM_TEXTSIZE = 20;
	/**
	 * MenuItme top margin dp
	 */
	public static final int MENU_TIME_TOP_MARGIN = 6;
	/**
	 * 添加项的容器
	 */
	@ViewInject(R.id.layout_setting_container)
	private LinearLayout mLayoutSettingContainer;
	/**
	 * 分割线
	 */
	private ImageView mImgItemLine;
	/**
	 * 添加MenuItem的容器
	 */
	private LinearLayout mLayoutMenuItems;
	/**
	 * 对话框主视图
	 */
	private View mainView;
	/**
	 * 当前焦点
	 */
	private Point mFocusPoint;
	private OnSelectTypeListener mOnSelectTypeListener;
	
	
	/**
	 * Category列表
	 */
	private ArrayList<MenuCategory> mCategorys;
	private Context mContext;
	
	public VideoSettingDialog(Context context) {
		super(context);
		mFocusPoint = new Point(0, 0);
		this.mContext = context;
		mCategorys = new ArrayList<MenuCategory>();
	}

	@Override
	public int getLayoutRes() {
		return R.layout.dialog_video_setting;
	}

	@Override
	public void initView() {
		super.initView();
		mainView = getMainView();
		ViewGroup.LayoutParams mainViewParams = mainView.getLayoutParams();
		//对话框占用2/3屏幕
		mainViewParams.width = DeviceInfoUtils.getScreenWidth(mContext) * 2 / 3;
		getMainView().setLayoutParams(mainViewParams);
		for(MenuCategory item : mCategorys){
			addCategoryView(item);
		}
		//初始化焦点显示
		displayFocus();
	}

	@Override
	public void initData() {

	}

	@Override
	public void initEvent() {
		
	}

	public void setOnSelectTypeListener(OnSelectTypeListener newVal) {
		mOnSelectTypeListener = newVal;
	}
	
	
	/**
	 * 添加每一列
	 * @param category
	 */
	 public void addMenuCategory(MenuCategory category){
		 mCategorys.add(category);
		 
	 }

	 /**
	  * 恢复上次选中
	  */
	 public void replayLastSelected(){
		 
	 }
	 
	 /**
	  * 获取焦点项目
	  */
	 public MenuItemImpl getCurrentFocusItemImpl(){
		 if(mFocusPoint.x >= 0 && mFocusPoint.y >=0)
			 return mCategorys.get(mFocusPoint.x).getMenuItems().get(mFocusPoint.y);
		 return null;
	 }
	 
	  private void addCategoryView(MenuCategory category){
		  //Log.i(TAG, "addCategoryView->category:" + category);
          LinearLayout menuItemLayout = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.menu_item, null);
          TextView cateName = (TextView) menuItemLayout.findViewById(R.id.menu_category_name);
          cateName.setText(category.getCategoryName());
          mImgItemLine = (ImageView)menuItemLayout.findViewById(R.id.iv_menu_item_line);
          mLayoutMenuItems = (LinearLayout)menuItemLayout.findViewById(R.id.layout_menu_items);
          ArrayList<MenuItemImpl> menuItemImpls =  category.getMenuItems();
          if(menuItemImpls != null && menuItemImpls.size() > 0){
        	  for(MenuItemImpl menuItem : menuItemImpls){
        		  TextView menuItmeTextView = new TextView(mContext);
        		  menuItmeTextView.setTextColor(Color.WHITE);
            	  menuItmeTextView.setText(menuItem.getTitle());
            	  menuItmeTextView.setTextSize(MENU_ITEM_TEXTSIZE);
            	  menuItmeTextView.setGravity(Gravity.CENTER);
            	  menuItmeTextView.setSingleLine(true);
            	  menuItmeTextView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            	  LinearLayout.LayoutParams menuItemParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, SizeUtils.dp2px(mContext, 40));
            	  menuItemParams.topMargin = SizeUtils.dp2px(mContext, 20);
            	  mLayoutMenuItems.addView(menuItmeTextView, menuItemParams);
        	  }
        	 
          }
          
          TextView menuItemView = (TextView)(mLayoutMenuItems.getChildAt(category.getSelectIndex()));
          menuItemView.setBackgroundColor(Color.parseColor("#0D6795"));
          
          LinearLayout.LayoutParams categoryLayoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
          mLayoutSettingContainer.addView(menuItemLayout, categoryLayoutParams);
      }

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		//Log.i(TAG, "onKeyDown->keyCode:" + keyCode);
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			rightMove();
			break;
		case KeyEvent.KEYCODE_DPAD_LEFT:
			leftMove();
			break;
		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_ENTER:
			clickClose();
			break;
		case KeyEvent.KEYCODE_DPAD_DOWN:
			downMove();
			break;
		case KeyEvent.KEYCODE_DPAD_UP:
			upMove();
			break;
		case KeyEvent.KEYCODE_BACK:
		case KeyEvent.KEYCODE_MENU:
			closeDialog();
			break;
		default:
			break;
		}
		return true;
	}
	
	public void leftMove(){
		if(mFocusPoint.x >= 0 && mFocusPoint.y >= 0){
			clearFocus();
			mFocusPoint.x = (mFocusPoint.x - 1 + mCategorys.size()) % mCategorys.size();
			mFocusPoint.y = mFocusPoint.y % (mCategorys.get(mFocusPoint.x).getMenuItems().size());
			displayFocus();
		}
	}
	
	public void rightMove(){
		clearFocus();
		if(mFocusPoint.x == -1 && mFocusPoint.y == -1){
			mFocusPoint.x = mFocusPoint.y = 0;
		}else{
			mFocusPoint.x = (mFocusPoint.x + 1) % mCategorys.size();
			mFocusPoint.y = mFocusPoint.y % (mCategorys.get(mFocusPoint.x).getMenuItems().size());
		}
		displayFocus();
		
	}
	
	public void upMove(){
		if(mFocusPoint.x >= 0 && mFocusPoint.y >= 0){
			clearFocus();
			mFocusPoint.y = (mFocusPoint.y - 1 + mCategorys.get(mFocusPoint.x).getMenuItems().size()) % (mCategorys.get(mFocusPoint.x).getMenuItems().size());
			displayFocus();
		}
	}
	
	public void downMove(){
		if(mFocusPoint.x >= 0 && mFocusPoint.y >= 0){
			clearFocus();
			mFocusPoint.y = (mFocusPoint.y + 1)  % (mCategorys.get(mFocusPoint.x).getMenuItems().size());
			displayFocus();
		}
	}
	
	public void clickClose(){
		if(mFocusPoint.x >= 0 && mFocusPoint.y >= 0 && mOnSelectTypeListener != null){
			Point categoryFocusPoint = new Point(mFocusPoint.x, mCategorys.get(mFocusPoint.x).getSelectIndex());
			ViewGroup categoryView = (ViewGroup)mLayoutSettingContainer.getChildAt(categoryFocusPoint.x);
			LinearLayout menuItemsLayout = (LinearLayout)categoryView.findViewById(R.id.layout_menu_items);
			if(android.os.Build.VERSION.SDK_INT < 23){
				menuItemsLayout.getChildAt(mFocusPoint.y).setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.common_grey_border));
			}else{
				menuItemsLayout.getChildAt(mFocusPoint.y).setBackground(mContext.getResources().getDrawable(R.drawable.common_grey_border, mContext.getTheme()));
			}
			//menuItemsLayout.getChildAt(categoryFocusPoint.y).setBackground(mContext.getResources().getDrawable(R.drawable.common_grey_border, mContext.getTheme()));
			mCategorys.get(mFocusPoint.x).setSelectIndex(mFocusPoint.y);
			if(android.os.Build.VERSION.SDK_INT < 23){
				menuItemsLayout.getChildAt(mFocusPoint.y).setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.common_black_white_background));
			}else{
				menuItemsLayout.getChildAt(mFocusPoint.y).setBackground(mContext.getResources().getDrawable(R.drawable.common_black_white_background, mContext.getTheme()));
			}
			mainView.invalidate();
			//menuItemsLayout.getChildAt(mFocusPoint.y).setBackground(mContext.getResources().getDrawable(R.drawable.common_black_white_background, mContext.getTheme()));
			mOnSelectTypeListener.onSelectType(mCategorys.get(mFocusPoint.x).getMenuItems().get(mFocusPoint.y));
			closeDialog();
		}
		 
	}
	
	public void closeDialog(){
		dismiss();
	}
	
	/**
	 * 移除之前的焦点
	 */
	public void clearFocus(){
		if(mFocusPoint.x >= 0 && mFocusPoint.y >= 0){
			ViewGroup categoryView = (ViewGroup)mLayoutSettingContainer.getChildAt(mFocusPoint.x);
			LinearLayout menuItemsLayout = (LinearLayout)categoryView.findViewById(R.id.layout_menu_items);
			//adapter for 4.4
			if(android.os.Build.VERSION.SDK_INT < 23){
				menuItemsLayout.getChildAt(mFocusPoint.y).setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.common_grey_border));
			}else{
				menuItemsLayout.getChildAt(mFocusPoint.y).setBackground(mContext.getResources().getDrawable(R.drawable.common_grey_border, mContext.getTheme()));
			}
			//menuItemsLayout.getChildAt(mFocusPoint.y).setBackground(mContext.getResources().getDrawable(R.drawable.common_grey_border, mContext.getTheme()));
			//还原选中
			if(mCategorys.get(mFocusPoint.x).getSelectIndex() == mFocusPoint.y){
				menuItemsLayout.getChildAt(mFocusPoint.y).setBackgroundColor(Color.parseColor("#0D6795"));
			}
		}
		
		
	}
	
	/**
	 * 显示新焦点
	 */
	public void displayFocus(){
		if(mFocusPoint.x >= 0 && mFocusPoint.y >= 0){
			ViewGroup categoryView = (ViewGroup)mLayoutSettingContainer.getChildAt(mFocusPoint.x);
			LinearLayout menuItemsLayout = (LinearLayout)categoryView.findViewById(R.id.layout_menu_items);
			//adapter for 4.4
			if(android.os.Build.VERSION.SDK_INT < 23){
				menuItemsLayout.getChildAt(mFocusPoint.y).setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.common_white_border));
			}else{
				menuItemsLayout.getChildAt(mFocusPoint.y).setBackground(mContext.getResources().getDrawable(R.drawable.common_white_border, mContext.getTheme()));
			}
			//选中换其他背景
			if(mCategorys.get(mFocusPoint.x).getSelectIndex() == mFocusPoint.y){
				if(android.os.Build.VERSION.SDK_INT < 23){
					menuItemsLayout.getChildAt(mFocusPoint.y).setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.common_black_white_background));
				}else{
					menuItemsLayout.getChildAt(mFocusPoint.y).setBackground(mContext.getResources().getDrawable(R.drawable.common_black_white_background, mContext.getTheme()));
				}
				//menuItemsLayout.getChildAt(mFocusPoint.y).setBackground(mContext.getResources().getDrawable(R.drawable.common_black_white_background, mContext.getTheme()));
			}
		}
		
		mainView.invalidate();
	}
	
	
	/**
	 * 清除Categories
	 */
	public void clearCategories(){
		mCategorys.clear();
	}
	
	/**
	 * 重新构造视图
	 */
	public void rebuildView(){
		clearFocus();
		mFocusPoint.x = 0;
		mFocusPoint.y = 0;
		mLayoutSettingContainer.removeAllViews();
		for(MenuCategory item : mCategorys){
			addCategoryView(item);
		}
		//初始化焦点显示
		displayFocus();
	}
}
