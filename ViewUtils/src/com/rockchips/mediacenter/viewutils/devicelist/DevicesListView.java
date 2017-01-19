package com.rockchips.mediacenter.viewutils.devicelist;

import java.util.ArrayList;
import java.util.List;

import android.R.integer;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.rockchips.mediacenter.viewutils.R;

/**
 * 设备列表
 * @author s00211113
 * 
 */
public class DevicesListView extends RelativeLayout implements OnClickListener
{
    private static final String TAG = "DeviceListView";

    private Context mContext;

    private static final int MSG_KEYCOD_LEFT = 0;

    private static final int MSG_KEYCOD_RIGHT = 1;

    private static final int MSG_KEYCOD_UP = 2;

    private static final int MSG_KEYCOD_DOWN = 3;

    private static final int MSG_KEYCOD_ENTER = 4;

    private Object mFocusDeviceObject;

    // 初始化动画的标志
    private boolean mBInitAnima;

    // 动画是否处于停止
    private boolean mBStopAnimation = true;

    // 初始化焦点
    private boolean mBInitFocus;

    // 搜索框
    private EditText mEditText;

    private ImageView mIvSearchFocus;

    private InputMethodManager mImm;

    // 焦点对应设备数据集合的位置index
    private int mFocusIndex = 1;

    // 焦点设备的分类显示组件
    private DeviceItemView mDeviceItem;

    // 设备名称显示载体
    private TextView[] mTextViews;

    private static final long ANIMTE_TIME = 600;

    // 焦点设备是否处于收缩失去焦点状体
    private boolean mBDeviceItemScale;

    private int mDeviceNum;

    private List<DeviceItem> mDevicesList = new ArrayList<DeviceItem>();

    public DevicesListView(Context context)
    {
        super(context);
        this.mContext = context;
        init();
    }

    public DevicesListView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.mContext = context;
        init();
    }

    public DevicesListView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        this.mContext = context;
        init();
    }

    /**
     * 赋值并且添加view显示
     * @param devicelist 设备列表
     * @see [类、类#方法、类#成员]
     */
    public void setDevicesList(List<DeviceItem> devicelist, boolean isAddNetWork)
    {
        if (devicelist == null)
        {
            devicelist = new ArrayList<DeviceItem>();
        }
        mDeviceNum = devicelist.size();
        Log.d(TAG, "setDevicesList:" + mDeviceNum);

        mDevicesList.clear();
        mDevicesList.addAll(devicelist);

        // 添加
        if (mBInitFocus)
        {
            adjustFocus();
        }
        else
        {
            initFocus(isAddNetWork);
            mBInitFocus = true;
        }        
    }
    
    public void notifyDataChanged()
    {
        refreshDevicesName();
    }

    /**
     * <一句话功能简述>返回设备个数 <功能详细描述>
     * @return
     * @see [类、类#方法、类#成员]
     */
    public int getDeviceNum()
    {
        return mDevicesList.size();
    }

    public void setOnDeviceSelectedListener(OnDeviceSelectedListener l)
    {
        if (mDeviceItem != null)
        {
            mDeviceItem.setOnDeviceSelectedListener(l);
        }
    }
    
    public interface OnSearchListener
    {
        void onSearch(String key);
    }
    
    private OnSearchListener mOnSearchListener;
    
    public void setOnSearchListener(OnSearchListener l)
    {
        mOnSearchListener = l;
    }

    private void init()
    {
        mImm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        mImm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

        findView();
        initData();

    }

    private static final int TEXTVIEW_NUMBER = 6;

    private static final int TEXTVIEW_ZERO = 0;

    private static final int TEXTVIEW_FIRST = 1;

    private static final int TEXTVIEW_SECOND = 2;

    private static final int TEXTVIEW_THIRD = 3;

    private static final int TEXTVIEW_FOURTH = 4;

    private static final int TEXTVIEW_FIVE = 5;

    private void findView()
    {
        View.inflate(mContext, R.layout.devicelist_layout, this);
        mEditText = (EditText) findViewById(R.id.deviceSearch);
        mIvSearchFocus = (ImageView) findViewById(R.id.focus);
        mEditText.setOnClickListener(this);
        
        mEditText.setOnEditorActionListener(new OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                Log.d(TAG, "onEditorAction : actionId=" + actionId);
                if (actionId == EditorInfo.IME_ACTION_DONE)
                {
                    Log.d(TAG, "onEditorAction : EditorInfo.IME_ACTION_DONE");
                    String str = mEditText.getText().toString();
                    if (mOnSearchListener != null)
                    {
                        mOnSearchListener.onSearch(str);
                    }
                }
                return false;
            }
        });

        mDeviceItem = (DeviceItemView) findViewById(R.id.gallery);

        mTextViews = new TextView[TEXTVIEW_NUMBER];
        mTextViews[TEXTVIEW_ZERO] = (TextView) findViewById(R.id.zeroDevice);
        mTextViews[TEXTVIEW_FIRST] = (TextView) findViewById(R.id.firstDevice);
        mTextViews[TEXTVIEW_SECOND] = (TextView) findViewById(R.id.secondDevice);
        mTextViews[TEXTVIEW_THIRD] = (TextView) findViewById(R.id.thirdDevice);
        mTextViews[TEXTVIEW_FOURTH] = (TextView) findViewById(R.id.fourthDevice);
        mTextViews[TEXTVIEW_FIVE] = (TextView) findViewById(R.id.fiveDevice);
    }

    private static final int TRANSLATE_TO_X = 40;

    private void initData()
    {
        mDevicesList = new ArrayList<DeviceItem>();

        adjustFocus();

        refreshDevicesName();

        // 放大获得焦点的设备名称
        ScaleAnimation focusDeviceScaleAnim = new ScaleAnimation(1.0F, FOCUS_SCALE_RATE, 1.0F, FOCUS_SCALE_RATE);
        focusDeviceScaleAnim.setDuration(0);

        // 添加设备第一次缩放动画
        Animation focusDeviceTransAnim = new TranslateAnimation(0, TRANSLATE_TO_X, 0, 0);
        focusDeviceScaleAnim.setDuration(0);

        AnimationSet initAnimationSet = new AnimationSet(true);
        initAnimationSet.setFillAfter(true);
        initAnimationSet.setFillEnabled(true);
        initAnimationSet.addAnimation(focusDeviceTransAnim);
        initAnimationSet.addAnimation(focusDeviceScaleAnim);

        // mTextViews[3]为焦点设备显示载体
        mTextViews[TEXTVIEW_THIRD].setAnimation(initAnimationSet);
        mTextViews[TEXTVIEW_THIRD].setBackgroundResource(R.drawable.current_select_device_background);
    }

    private void initFocus(boolean isAddNetWork)
    {
        if (mDevicesList == null || mDeviceNum <= 1)
        {
            setCurrentFocus(0);
        }
        else
        {
        	if(isAddNetWork)
        		setCurrentFocus(mDeviceNum - 1);
        	else
        		setCurrentFocus(1);
        }
        Log.i(TAG, "initFocus->mFocusIndex:" + mFocusIndex);
    }
    
    public void setCurrentFocus(int focusIndex)
    {
        mFocusIndex = focusIndex;
        refreshFocusDeviceId();
    }

    /**
     * <一句话功能简述>调整焦点 <功能详细描述>
     * @see [类、类#方法、类#成员]
     */
    private void adjustFocus()
    {
        if (mDevicesList == null || mDeviceNum <= 1)
        {
            mFocusIndex = 0;
        }
        else
        {
            refreshFocusIdByFocusDeviceId();
            if (mFocusIndex >= mDevicesList.size())
            {
                mFocusIndex = mDevicesList.size() - 1;
            }
        }
        Log.i(TAG, "adjustFocus->mFocusIndex:" + mFocusIndex);
        refreshFocusDeviceId();

    }

    /** 获取焦点设备 */
    public DeviceItem getFocusDevice()
    {
        if (mFocusIndex >= mDevicesList.size() || mDevicesList.size() == 0)
        {
            return null;
        }
        return mDevicesList.get(mFocusIndex);
    }

    // 初始动画。由于动画需要用到View的属性值，所以动画需要在所有View初始化完毕之后，才能初始化。
    private void initAnimation()
    {
        initDownAnimation();
        initUpAnimation();
        initDeviceItemFocusAnimation();

    }

    // 焦点设备名称缩放率
    private static final float FOCUS_SCALE_RATE = 1.5f;

    // 分类海报画廊的Y坐标
    private static final int GY = 250;

    // 下移距离
    private static final int DOWN_MOVE_DISTANCE = 200;

    // 上移距离
    private static final int UP_MOVE_DISTANCE = 50;

    private AnimatorSet mGalleryAnimatorSetDown;

    private AnimationSet mFirstViewSetDown;

    private AnimationSet mSecondViewSetDown;

    private AnimationSet mThirdViewSetDown;

    private AnimationSet mFourthViewSetDown;

    private AnimationSet mFiveViewSetDown;

    // 只有一个设备时的动画
    private AnimationSet mSecondViewSetDownOne;

    private AnimationSet mFourthViewSetDownOne;

    private static final float FADEOUT_ALPHA_FROM = 1f;

    private static final float FADEOUT_ALPHA_TO = 0;

    private static final float FADEIN_ALPHA_FROM = 0.1f;

    private static final float FADEIN_ALPHA_TO = 1f;

    // 初始化下滚动画
    private void initDownAnimation()
    {
        int setIdx = 1;
        // Gallery alpha动画
        ObjectAnimator fadeOutAnim = ObjectAnimator.ofFloat(mDeviceItem, "alpha", FADEOUT_ALPHA_FROM, FADEOUT_ALPHA_TO);
        fadeOutAnim.setDuration(ANIMTE_TIME / 2);

        ObjectAnimator tranOutAnim = ObjectAnimator.ofFloat(mDeviceItem, "Y", GY, GY + DOWN_MOVE_DISTANCE);
        tranOutAnim.setDuration(ANIMTE_TIME / 2);

        ObjectAnimator fadeInAnim = ObjectAnimator.ofFloat(mDeviceItem, "alpha", FADEIN_ALPHA_FROM, FADEIN_ALPHA_TO);
        fadeInAnim.setDuration(ANIMTE_TIME / 2);

        ObjectAnimator tranInAnim = ObjectAnimator.ofFloat(mDeviceItem, "Y", GY - UP_MOVE_DISTANCE, GY);
        tranInAnim.setDuration(ANIMTE_TIME / 2);

        mGalleryAnimatorSetDown = new AnimatorSet();
        mGalleryAnimatorSetDown.play(fadeOutAnim).with(tranOutAnim);
        mGalleryAnimatorSetDown.play(fadeInAnim).with(tranInAnim);
        mGalleryAnimatorSetDown.play(fadeOutAnim).before(fadeInAnim);

        // 第一个设备动画
        Animation firstViewTranAnim = new TranslateAnimation(0, 0, -(mTextViews[setIdx].getY() - mTextViews[setIdx - 1].getY()), 0);
        firstViewTranAnim.setDuration(ANIMTE_TIME);

        Animation firstViewAlphaAnim = new AlphaAnimation(0f, 0f);
        firstViewAlphaAnim.setDuration(ANIMTE_TIME);

        mFirstViewSetDown = new AnimationSet(true);
        mFirstViewSetDown.setFillAfter(true);
        mFirstViewSetDown.setFillEnabled(true);
        mFirstViewSetDown.addAnimation(firstViewTranAnim);
        mFirstViewSetDown.addAnimation(firstViewAlphaAnim);

        // 第二个设备动画
        setIdx++;
        Animation secondViewTranAnim = new TranslateAnimation(0, 0, -(mTextViews[setIdx].getY() - mTextViews[setIdx - 1].getY()), 0);
        secondViewTranAnim.setDuration(ANIMTE_TIME);

        Animation secondViewAlphaAnim = new AlphaAnimation(0f, 0.2f);
        secondViewAlphaAnim.setDuration(ANIMTE_TIME);

        mSecondViewSetDown = new AnimationSet(true);
        mSecondViewSetDown.setFillAfter(true);
        mSecondViewSetDown.setFillEnabled(true);
        mSecondViewSetDown.addAnimation(secondViewTranAnim);
        mSecondViewSetDown.addAnimation(secondViewAlphaAnim);

        // 第三个设备动画
        setIdx++;
        Animation thirdViewTranAnim = new TranslateAnimation(0, TRANSLATE_TO_X, -(mTextViews[setIdx].getY() - mTextViews[setIdx - 1].getY()), 0);
        thirdViewTranAnim.setDuration(ANIMTE_TIME);

        Animation thirdViewScaleAnima = new ScaleAnimation(1f, FOCUS_SCALE_RATE, 1f, FOCUS_SCALE_RATE);
        thirdViewScaleAnima.setDuration(ANIMTE_TIME);

        Animation thirdViewAlphaAnim = new AlphaAnimation(0.2f, 1f);
        thirdViewAlphaAnim.setDuration(ANIMTE_TIME);

        mThirdViewSetDown = new AnimationSet(true);
        mThirdViewSetDown.setFillAfter(true);
        mThirdViewSetDown.setFillEnabled(true);
        mThirdViewSetDown.setAnimationListener(new AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation)
            {
            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {
            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                mBStopAnimation = true;
            }
        });
        mThirdViewSetDown.addAnimation(thirdViewTranAnim);
        mThirdViewSetDown.addAnimation(thirdViewScaleAnima);
        mThirdViewSetDown.addAnimation(thirdViewAlphaAnim);

        // 第四个设备动画
        setIdx++;
        Animation fourthViewTranAnim = new TranslateAnimation(TRANSLATE_TO_X, 0, -(mTextViews[setIdx].getY() - mTextViews[setIdx - 1].getY())
                / FOCUS_SCALE_RATE, 0);
        fourthViewTranAnim.setDuration(ANIMTE_TIME);

        Animation fourthViewScaleAnima = new ScaleAnimation(FOCUS_SCALE_RATE, 1f, FOCUS_SCALE_RATE, 1f);
        fourthViewScaleAnima.setDuration(ANIMTE_TIME);

        Animation fourthViewAlphaAnim = new AlphaAnimation(1f, 0.1f);
        fourthViewAlphaAnim.setDuration(ANIMTE_TIME);

        mFourthViewSetDown = new AnimationSet(true);
        mFourthViewSetDown.setFillAfter(true);
        mFourthViewSetDown.setFillEnabled(true);
        mFourthViewSetDown.addAnimation(fourthViewTranAnim);
        mFourthViewSetDown.addAnimation(fourthViewScaleAnima);
        mFourthViewSetDown.addAnimation(fourthViewAlphaAnim);

        // 第五个设备动画
        setIdx++;
        Animation fiveViewTranAnim = new TranslateAnimation(0, 0, -(mTextViews[setIdx].getY() - mTextViews[setIdx - 1].getY()), 0);
        fiveViewTranAnim.setDuration(ANIMTE_TIME);

        Animation fiveViewAlphaAnim = new AlphaAnimation(0.1f, 0f);
        fiveViewAlphaAnim.setDuration(ANIMTE_TIME);

        mFiveViewSetDown = new AnimationSet(true);
        mFiveViewSetDown.setFillAfter(true);
        mFiveViewSetDown.setFillEnabled(true);
        mFiveViewSetDown.addAnimation(fiveViewTranAnim);
        mFiveViewSetDown.addAnimation(fiveViewAlphaAnim);

        // ------------只有一个设备时的动画------------
        Animation secondViewAlphaAnimOne = new AlphaAnimation(0.0f, 0.2f);
        secondViewAlphaAnimOne.setDuration(ANIMTE_TIME);
        mSecondViewSetDownOne = new AnimationSet(true);
        mSecondViewSetDownOne.setFillAfter(true);
        mSecondViewSetDownOne.setFillEnabled(true);
        mSecondViewSetDownOne.addAnimation(secondViewTranAnim);
        mSecondViewSetDownOne.addAnimation(secondViewAlphaAnimOne);

        Animation fourthViewAlphaAnimOne = new AlphaAnimation(1f, 0.0f);
        fourthViewAlphaAnimOne.setDuration(ANIMTE_TIME);
        mFourthViewSetDownOne = new AnimationSet(true);
        mFourthViewSetDownOne.setFillAfter(true);
        mFourthViewSetDownOne.setFillEnabled(true);
        mFourthViewSetDownOne.addAnimation(fourthViewTranAnim);
        mFourthViewSetDownOne.addAnimation(fourthViewScaleAnima);
        mFourthViewSetDownOne.addAnimation(fourthViewAlphaAnimOne);

    }

    private AnimatorSet mGalleryAnimatorSetUp;

    private AnimationSet mZeroViewSetUp;

    private AnimationSet mFirstViewSetUp;

    private AnimationSet mSecondViewSetUp;

    private AnimationSet mThirdViewSetUp;

    private AnimationSet mFourthViewSetUp;

    private AnimationSet mFiveViewSetUp;

    // 初始化上滚动画
    private void initUpAnimation()
    {
        int setIdx = 1;
        // Gallery alpha动画
        ObjectAnimator fadeOutAnim = ObjectAnimator.ofFloat(mDeviceItem, "alpha", 1f, 0f);
        fadeOutAnim.setDuration(ANIMTE_TIME / 2);
        // fadeOutAnim.setInterpolator(new DecelerateInterpolator());

        ObjectAnimator tranOutAnim = ObjectAnimator.ofFloat(mDeviceItem, "Y", GY, GY - UP_MOVE_DISTANCE);
        tranOutAnim.setDuration(ANIMTE_TIME / 2);

        ObjectAnimator fadeInAnim = ObjectAnimator.ofFloat(mDeviceItem, "alpha", 0.1f, 1f);
        fadeInAnim.setDuration(ANIMTE_TIME / 2);
        // fadeInAnim.setInterpolator(new DecelerateInterpolator());

        ObjectAnimator tranInAnim = ObjectAnimator.ofFloat(mDeviceItem, "Y", GY + DOWN_MOVE_DISTANCE, GY);
        tranInAnim.setDuration(ANIMTE_TIME / 2);

        mGalleryAnimatorSetUp = new AnimatorSet();
        mGalleryAnimatorSetUp.play(fadeOutAnim).with(tranOutAnim);
        mGalleryAnimatorSetUp.play(fadeInAnim).with(tranInAnim);
        mGalleryAnimatorSetUp.play(fadeOutAnim).before(fadeInAnim);

        // 第零个设备动画
        Animation zeroViewTranAnim = new TranslateAnimation(0, 0, mTextViews[setIdx].getY() - mTextViews[setIdx - 1].getY(), 0);
        zeroViewTranAnim.setDuration(ANIMTE_TIME);

        Animation zeroViewAlphaAnim = new AlphaAnimation(0f, 0f);
        zeroViewAlphaAnim.setDuration(ANIMTE_TIME);

        mZeroViewSetUp = new AnimationSet(true);
        mZeroViewSetUp.setFillAfter(true);
        mZeroViewSetUp.setFillEnabled(true);
        mZeroViewSetUp.addAnimation(zeroViewTranAnim);
        mZeroViewSetUp.addAnimation(zeroViewAlphaAnim);

        // 第一个设备动画
        setIdx++;
        Animation firstViewTranAnim = new TranslateAnimation(0, 0, mTextViews[setIdx].getY() - mTextViews[setIdx - 1].getY(), 0);
        firstViewTranAnim.setDuration(ANIMTE_TIME);

        Animation firstViewAlphaAnim = new AlphaAnimation(0.2f, 0f);
        firstViewAlphaAnim.setDuration(ANIMTE_TIME);

        mFirstViewSetUp = new AnimationSet(true);
        mFirstViewSetUp.setFillAfter(true);
        mFirstViewSetUp.setFillEnabled(true);
        mFirstViewSetUp.addAnimation(firstViewAlphaAnim);
        mFirstViewSetUp.addAnimation(firstViewTranAnim);

        // 第二个设备动画
        setIdx++;
        Animation secondViewTranAnim = new TranslateAnimation(TRANSLATE_TO_X * FOCUS_SCALE_RATE, 0, mTextViews[setIdx].getY() - mTextViews[setIdx - 1].getY(), 0);
        secondViewTranAnim.setDuration(ANIMTE_TIME);

        Animation secondViewScaleAnima = new ScaleAnimation(FOCUS_SCALE_RATE, 1f, FOCUS_SCALE_RATE, 1f);
        secondViewScaleAnima.setDuration(ANIMTE_TIME);

        Animation secondViewAlphaAnim = new AlphaAnimation(1f, 0.2f);
        secondViewAlphaAnim.setDuration(ANIMTE_TIME);

        mSecondViewSetUp = new AnimationSet(true);
        mSecondViewSetUp.setFillAfter(true);
        mSecondViewSetUp.setFillEnabled(true);
        mSecondViewSetUp.addAnimation(secondViewScaleAnima);
        mSecondViewSetUp.addAnimation(secondViewTranAnim);
        mSecondViewSetUp.addAnimation(secondViewAlphaAnim);

        // 第三个设备动画
        setIdx++;
        Animation thirdViewTranAnim = new TranslateAnimation(0, TRANSLATE_TO_X, mTextViews[setIdx].getY() - mTextViews[setIdx - 1].getY(), 0);
        thirdViewTranAnim.setDuration(ANIMTE_TIME);

        Animation thirdViewScaleAnima = new ScaleAnimation(1f, FOCUS_SCALE_RATE, 1f, FOCUS_SCALE_RATE);
        thirdViewScaleAnima.setDuration(ANIMTE_TIME);

        Animation thirdViewAlphaAnim = new AlphaAnimation(0.1f, 1f);
        thirdViewAlphaAnim.setDuration(ANIMTE_TIME);

        mThirdViewSetUp = new AnimationSet(true);
        mThirdViewSetUp.setFillAfter(true);
        mThirdViewSetUp.setFillEnabled(true);
        mThirdViewSetUp.setAnimationListener(new AnimationListener()
        {

            @Override
            public void onAnimationStart(Animation animation)
            {
            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {
            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                mBStopAnimation = true;
            }
        });
        mThirdViewSetUp.addAnimation(thirdViewTranAnim);
        mThirdViewSetUp.addAnimation(thirdViewAlphaAnim);
        mThirdViewSetUp.addAnimation(thirdViewScaleAnima);

        // 第四个设备动画
        setIdx++;
        Animation fourthViewTranAnim = new TranslateAnimation(0, 0, mTextViews[setIdx].getY() - mTextViews[setIdx - 1].getY(), 0);
        fourthViewTranAnim.setDuration(ANIMTE_TIME);

        Animation fourthViewAlphaAnim = new AlphaAnimation(0f, 0.1f);
        fourthViewAlphaAnim.setDuration(ANIMTE_TIME);

        mFourthViewSetUp = new AnimationSet(true);
        mFourthViewSetUp.setFillAfter(true);
        mFourthViewSetUp.setFillEnabled(true);
        mFourthViewSetUp.addAnimation(fourthViewTranAnim);
        mFourthViewSetUp.addAnimation(fourthViewAlphaAnim);
    }

    private AnimationSet mDeviceNameZoomInSet;

    // 设备item放大
    private AnimationSet mDeviceItemZoomInSet;

    private AnimationSet mDeviceNameZoomOutSet;

    // 设备item缩小
    private AnimationSet mDeviceItemZoomOutSet;

    private void initDeviceItemFocusAnimation()
    {
        // --------焦点项获得焦点放大动画----------
        // 设备名动画
        Animation nameScaleAnima = new ScaleAnimation(1f, FOCUS_SCALE_RATE, 1f, FOCUS_SCALE_RATE);
        nameScaleAnima.setDuration(ANIMTE_TIME);
        Animation nameTranAnim = new TranslateAnimation(0, TRANSLATE_TO_X * FOCUS_SCALE_RATE, 0, 0);
        nameTranAnim.setDuration(ANIMTE_TIME);

        Animation nameAlphaAnim = new AlphaAnimation(0.2f, 1f);
        nameAlphaAnim.setDuration(ANIMTE_TIME);

        mDeviceNameZoomInSet = new AnimationSet(true);
        mDeviceNameZoomInSet.setFillAfter(true);
        mDeviceNameZoomInSet.setFillEnabled(true);
        mDeviceNameZoomInSet.addAnimation(nameScaleAnima);
        mDeviceNameZoomInSet.addAnimation(nameAlphaAnim);
        mDeviceNameZoomInSet.addAnimation(nameTranAnim);

        // 设备item动画
        Animation scaleAnima = new ScaleAnimation(0.9f, 1f, 0.9f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnima.setDuration(ANIMTE_TIME);

        Animation alphaAnim = new AlphaAnimation(0.5f, 1f);
        alphaAnim.setDuration(ANIMTE_TIME);

        mDeviceItemZoomInSet = new AnimationSet(true);
        mDeviceItemZoomInSet.setFillAfter(true);
        mDeviceItemZoomInSet.setFillEnabled(true);
        mDeviceItemZoomInSet.setAnimationListener(new AnimationListener()
        {

            @Override
            public void onAnimationStart(Animation animation)
            {
            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {
            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                mBDeviceItemScale = false;
                mDeviceItem.refresh();
            }
        });
        mDeviceItemZoomInSet.addAnimation(scaleAnima);
        mDeviceItemZoomInSet.addAnimation(alphaAnim);

        // --------焦点项获得焦点缩小动画----------
        // 设备名动画
        nameScaleAnima = new ScaleAnimation(FOCUS_SCALE_RATE, 1f, FOCUS_SCALE_RATE, 1f);
        nameScaleAnima.setDuration(ANIMTE_TIME);

        nameTranAnim = new TranslateAnimation(TRANSLATE_TO_X * FOCUS_SCALE_RATE, 0, 0, 0);
        nameTranAnim.setDuration(ANIMTE_TIME);

        nameAlphaAnim = new AlphaAnimation(1f, 0.2f);
        nameAlphaAnim.setDuration(ANIMTE_TIME);

        mDeviceNameZoomOutSet = new AnimationSet(true);
        mDeviceNameZoomOutSet.setFillAfter(true);
        mDeviceNameZoomOutSet.setFillEnabled(true);
        mDeviceNameZoomOutSet.addAnimation(nameScaleAnima);
        mDeviceNameZoomOutSet.addAnimation(nameAlphaAnim);
        mDeviceNameZoomOutSet.addAnimation(nameTranAnim);

        // 设备item动画
        scaleAnima = new ScaleAnimation(1f, 0.9f, 1f, 0.9f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnima.setDuration(ANIMTE_TIME);

        alphaAnim = new AlphaAnimation(1f, 0.5f);
        alphaAnim.setDuration(ANIMTE_TIME);

        mDeviceItemZoomOutSet = new AnimationSet(true);
        mDeviceItemZoomOutSet.setFillAfter(true);
        mDeviceItemZoomOutSet.setFillEnabled(true);
        mDeviceItemZoomOutSet.setAnimationListener(new AnimationListener()
        {

            @Override
            public void onAnimationStart(Animation animation)
            {
            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {
            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                mBDeviceItemScale = true;
            }
        });
        mDeviceItemZoomOutSet.addAnimation(scaleAnima);
        mDeviceItemZoomOutSet.addAnimation(alphaAnim);
    }

    public void clearAnimation()
    {

    }

    /** {@inheritDoc} */

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        Message msg = Message.obtain();
        switch (keyCode)
        {
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (!mBDeviceItemScale && mBStopAnimation)
                {
                    msg.what = MSG_KEYCOD_RIGHT;
                    msg.arg1 = keyCode;
                    msg.obj = event;
                    mHandler.removeMessages(MSG_KEYCOD_RIGHT);
                    mHandler.sendMessage(msg);
                }
                break;

            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (!mBDeviceItemScale && mBStopAnimation)
                {
                    msg.what = MSG_KEYCOD_LEFT;
                    msg.arg1 = keyCode;
                    msg.obj = event;
                    mHandler.removeMessages(MSG_KEYCOD_LEFT);
                    mHandler.sendMessage(msg);
                }
                break;

            case KeyEvent.KEYCODE_DPAD_UP:
                if (mBStopAnimation)
                {
                    mHandler.removeMessages(MSG_KEYCOD_UP);
                    mHandler.sendEmptyMessage(MSG_KEYCOD_UP);
                }
                break;

            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (mBStopAnimation)
                {
                    mHandler.removeMessages(MSG_KEYCOD_DOWN);
                    mHandler.sendEmptyMessage(MSG_KEYCOD_DOWN);
                }
                break;

            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                // 所有选中（onKeyDown）和点中（onSingleTapUp）事件,都在 DeviceItemView2类中处理。
                mDeviceItem.onKeyDown(keyCode, event);
                break;

            default:
                break;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v)
    {
        if (v.getId() == R.id.deviceSearch)
        {
            requestSearchFocus();
            // 弹出软键盘
            mImm.showSoftInput(mEditText, 0);
        }
    }

    private Handler mHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case MSG_KEYCOD_LEFT:
                case MSG_KEYCOD_RIGHT:
                    if (getFocusDevice() != null && !mEditText.hasFocus())
                    {
                        mDeviceItem.onKeyDown(msg.arg1, (KeyEvent) msg.obj);
                    }

                    break;

                case MSG_KEYCOD_UP:
                    keyUp();

                    break;

                case MSG_KEYCOD_DOWN:
                    keyDown();

                    break;

                case MSG_KEYCOD_ENTER:

                    break;

                default:
                    break;
            }
        };
    };

    private void keyDown()
    {
        if (mDevicesList.size() > 0 && mEditText.hasFocus())
        {
            cancelSearchFocus();
            return;
        }
        if (mDevicesList.size() > 1)
        {
            if (mFocusIndex < mDevicesList.size() - 1)
            {
                scrollUpAnimation();
            }
        }
    }

    private void keyUp()
    {
        if (mDevicesList.size() > 0)
        {
            if (mFocusIndex > 0)
            {
                scrollDownAnimation();
            }
            
           /* else
            {
                // mSearchButton.setFocusable(true);
                requestSearchFocus();
                mImm.showSoftInput(mEditText, 0);
            }*/
        }
    }

    private void refreshFocusIdByFocusDeviceId()
    {
        if (mDevicesList == null)
        {
            return;
        }
        for (int i = 0; i < mDevicesList.size(); i++)
        {
            if (mFocusDeviceObject == null)
            {
                mFocusIndex = mDevicesList.size() - 1;
                return;
            }
            else if (mFocusDeviceObject.equals(mDevicesList.get(i).mObject))
            {
                mFocusIndex = i;
                Log.d(TAG, "i =====> " + mFocusIndex);
                return;
            }
        }
    }

    private void refreshFocusDeviceId()
    {
    	//mTextViews[mFocusIndex].setBackgroundResource(R.drawable.current_select_device_background);
        mFocusDeviceObject = getCurrentFocusObj();
        /*if(mFocusDeviceObject == null)
        	return;*/
        /*for(int i = 0; i < mTextViews.length; ++i){
    		if(mTextViews[i].getText().toString().equals(mDevicesList.get(mFocusIndex).mName)){
    			mTextViews[i].setBackgroundResource(R.drawable.current_select_device_background);
    			break;
    		}
    	}*/
    }
    
    public Object getCurrentFocusObj()
    {        
        if (mDevicesList == null || mDevicesList.size() == 0 || mDevicesList.size() <= mFocusIndex)
        {
            return null;
        }
        else
        {
             return mDevicesList.get(mFocusIndex).mObject;
        }
    }

    private void requestSearchFocus()
    {
        mEditText.setFocusable(true);
        mEditText.requestFocus();
        mIvSearchFocus.setVisibility(View.VISIBLE);
        deviceItemZoomOut();
    }

    private void cancelSearchFocus()
    {
        mEditText.setText("");
        mEditText.setFocusable(false);
        mIvSearchFocus.setVisibility(View.INVISIBLE);
        deviceItemZoomIn();
    }

    /** 设备Item放大 */
    private void deviceItemZoomIn()
    {
        if (!mBInitAnima)
        {
            initAnimation();
            mBInitAnima = true;
        }

        // 焦点设备处于缩小状态时才放大
        if (mBDeviceItemScale)
        {
            mTextViews[TEXTVIEW_THIRD].startAnimation(mDeviceNameZoomInSet);
            mDeviceItem.startAnimation(mDeviceItemZoomInSet);
        }
    }

    /** 设备Item缩小 */
    private void deviceItemZoomOut()
    {
        if (!mBInitAnima)
        {
            initAnimation();
            mBInitAnima = true;
        }

        // 焦点设备还没缩小时才缩小
        if (!mBDeviceItemScale)
        {
            mTextViews[TEXTVIEW_THIRD].startAnimation(mDeviceNameZoomOutSet);
            mDeviceItem.startAnimation(mDeviceItemZoomOutSet);
        }
    }

    private void scrollDownAnimation()
    {
        //
        if (!mBInitAnima)
        {
            initAnimation();
            mBInitAnima = true;
        }

        // 焦点向下滚动一位
        if (mFocusIndex > 0)
        {
            mFocusIndex--;
            refreshFocusDeviceId();
        }
        else
        {
            return;
        }

        // 更换数据
        refreshDevicesName();

        if (getDeviceNum() == 1)
        {
            mTextViews[TEXTVIEW_SECOND].setAlpha(1f);
            mTextViews[TEXTVIEW_THIRD].setAlpha(1f);
            mTextViews[TEXTVIEW_FOURTH].setAlpha(1f);

            mBStopAnimation = false;
            mTextViews[TEXTVIEW_SECOND].startAnimation(mSecondViewSetDownOne);
            mTextViews[TEXTVIEW_THIRD].startAnimation(mThirdViewSetDown);
            mTextViews[TEXTVIEW_FOURTH].startAnimation(mFourthViewSetDown);
        }
        else if (getDeviceNum() == 2)
        {
            mTextViews[TEXTVIEW_FIRST].setAlpha(1f);
            mTextViews[TEXTVIEW_SECOND].setAlpha(1f);
            mTextViews[TEXTVIEW_THIRD].setAlpha(1f);
            mTextViews[TEXTVIEW_FOURTH].setAlpha(1f);

            mBStopAnimation = false;
            mTextViews[TEXTVIEW_FIRST].startAnimation(mFirstViewSetDown);
            mTextViews[TEXTVIEW_SECOND].startAnimation(mSecondViewSetDown);
            mTextViews[TEXTVIEW_THIRD].startAnimation(mThirdViewSetDown);
            mTextViews[TEXTVIEW_FOURTH].startAnimation(mFourthViewSetDown);
        }
        else
        {

            mTextViews[TEXTVIEW_FIRST].setAlpha(1f);
            mTextViews[TEXTVIEW_SECOND].setAlpha(1f);
            mTextViews[TEXTVIEW_THIRD].setAlpha(1f);
            mTextViews[TEXTVIEW_FOURTH].setAlpha(1f);
            mTextViews[TEXTVIEW_FIVE].setAlpha(1f);

            // 播放动画
            mBStopAnimation = false;
            mTextViews[TEXTVIEW_FIRST].startAnimation(mFirstViewSetDown);
            mTextViews[TEXTVIEW_SECOND].startAnimation(mSecondViewSetDown);
            mTextViews[TEXTVIEW_THIRD].startAnimation(mThirdViewSetDown);
            mTextViews[TEXTVIEW_FOURTH].startAnimation(mFourthViewSetDown);
            mTextViews[TEXTVIEW_FIVE].startAnimation(mFiveViewSetDown);
        }
        mGalleryAnimatorSetDown.start();

    }

    private void scrollUpAnimation()
    {

        if (!mBInitAnima)
        {
            initAnimation();
            mBInitAnima = true;
        }

        // mFocusIndex = (++mFocusIndex) % mDevicesList.size();
        if (mFocusIndex < mDevicesList.size() - 1)
        {
            mFocusIndex++;
            refreshFocusDeviceId();
        }
        else
        {
            return;
        }

        refreshDevicesName();

        if (getDeviceNum() == 1)
        {
            mTextViews[TEXTVIEW_SECOND].setAlpha(1f);
            mTextViews[TEXTVIEW_THIRD].setAlpha(1f);

            mBStopAnimation = false;
            mTextViews[TEXTVIEW_SECOND].startAnimation(mSecondViewSetUp);
            mTextViews[TEXTVIEW_THIRD].startAnimation(mThirdViewSetUp);
        }
        else if (getDeviceNum() == 2)
        {
            mTextViews[TEXTVIEW_FIRST].setAlpha(1f);
            mTextViews[TEXTVIEW_SECOND].setAlpha(1f);
            mTextViews[TEXTVIEW_THIRD].setAlpha(1f);

            mBStopAnimation = false;
            mTextViews[TEXTVIEW_FIRST].startAnimation(mFirstViewSetUp);
            mTextViews[TEXTVIEW_SECOND].startAnimation(mSecondViewSetUp);
            mTextViews[TEXTVIEW_THIRD].startAnimation(mThirdViewSetUp);
        }
        else
        {
            mTextViews[TEXTVIEW_ZERO].setAlpha(1f);
            mTextViews[TEXTVIEW_FIRST].setAlpha(1f);
            mTextViews[TEXTVIEW_SECOND].setAlpha(1f);
            mTextViews[TEXTVIEW_THIRD].setAlpha(1f);
            mTextViews[TEXTVIEW_FOURTH].setAlpha(1f);

            // 播放动画
            mBStopAnimation = false;
            mTextViews[TEXTVIEW_ZERO].startAnimation(mZeroViewSetUp);
            mTextViews[TEXTVIEW_FIRST].startAnimation(mFirstViewSetUp);
            mTextViews[TEXTVIEW_SECOND].startAnimation(mSecondViewSetUp);
            mTextViews[TEXTVIEW_THIRD].startAnimation(mThirdViewSetUp);
            mTextViews[TEXTVIEW_FOURTH].startAnimation(mFourthViewSetUp);

        }
        mGalleryAnimatorSetUp.start();
    }

    /**
     * <一句话功能简述>刷新设备的显示位置 <功能详细描述>
     * @see [类、类#方法、类#成员]
     */
    private void refreshDevicesName()
    {
        // 避免出现空指针的极限情况 ouxiaoyong
        if (mDeviceItem == null)
        {
            return;
        }
        if (mDevicesList == null || mDevicesList.size() == 0)
        {
            mTextViews[TEXTVIEW_ZERO].setText("");
            mTextViews[TEXTVIEW_FIRST].setText("");
            mTextViews[TEXTVIEW_SECOND].setText("");
            mTextViews[TEXTVIEW_THIRD].setText("");
            mTextViews[TEXTVIEW_FOURTH].setText("");
            mTextViews[TEXTVIEW_FIVE].setText("");
            // 避免出现空指针的极限情况 ouxiaoyong
            if (mDeviceItem == null)
            {
                return;
            }
            mDeviceItem.setVisibility(View.INVISIBLE);
        }
        else
        {
            // 避免出现空指针的极限情况 ouxiaoyong
            if (mDeviceItem == null)
            {
                return;
            }
            mDeviceItem.setVisibility(View.VISIBLE);
            // 把焦点设备的数据设置到分类item中
            mDeviceItem.setDeviceItem(getFocusDevice());

            int dSize = mDevicesList.size();
            mTextViews[TEXTVIEW_ZERO].setText((mFocusIndex - TEXTVIEW_THIRD) >= 0 ? mDevicesList.get(mFocusIndex - TEXTVIEW_THIRD).mName : "");
            mTextViews[TEXTVIEW_FIRST].setText((mFocusIndex - 2) >= 0 ? mDevicesList.get(mFocusIndex - 2).mName : "");
            mTextViews[TEXTVIEW_SECOND].setText((mFocusIndex - 1) >= 0 ? mDevicesList.get(mFocusIndex - 1).mName : "");
            mTextViews[TEXTVIEW_THIRD].setText(mDevicesList.get(mFocusIndex).mName);
            mTextViews[TEXTVIEW_FOURTH].setText((mFocusIndex + 1) < dSize ? mDevicesList.get(mFocusIndex + 1).mName : "");
            mTextViews[TEXTVIEW_FIVE].setText((mFocusIndex + 2) < dSize ? mDevicesList.get(mFocusIndex + 2).mName : "");

        }
    }

    public DeviceItemView getDeviceItem()
    {
        return mDeviceItem;
    }

    public void recycle()
    {
        // 回收设备分类显示海报
        if (mDeviceItem != null)
        {
            mDeviceItem.recycle();
            mDeviceItem = null;
        }
    }
}
