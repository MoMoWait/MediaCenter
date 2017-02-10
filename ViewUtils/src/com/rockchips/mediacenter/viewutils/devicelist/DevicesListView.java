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
 * 璁惧鍒楄〃
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

    // 鍒濆鍖栧姩鐢荤殑鏍囧織
    private boolean mBInitAnima;

    // 鍔ㄧ敾鏄惁澶勪簬鍋滄
    private boolean mBStopAnimation = true;

    // 鍒濆鍖栫劍鐐�
    private boolean mBInitFocus;

    // 鎼滅储妗�
    private EditText mEditText;

    private ImageView mIvSearchFocus;

    private InputMethodManager mImm;

    // 鐒︾偣瀵瑰簲璁惧鏁版嵁闆嗗悎鐨勪綅缃甶ndex
    private int mFocusIndex = 1;

    // 鐒︾偣璁惧鐨勫垎绫绘樉绀虹粍浠�
    private DeviceItemView mDeviceItem;

    // 璁惧鍚嶇О鏄剧ず杞戒綋
    private TextView[] mTextViews;

    private static final long ANIMTE_TIME = 600;

    // 鐒︾偣璁惧鏄惁澶勪簬鏀剁缉澶卞幓鐒︾偣鐘朵綋
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
     * 璧嬪�煎苟涓旀坊鍔爒iew鏄剧ず
     * @param devicelist 璁惧鍒楄〃
     * @see [绫汇�佺被#鏂规硶銆佺被#鎴愬憳]
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

        // 娣诲姞
        if (mBInitFocus)
        {
            adjustFocus(isAddNetWork);
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
     * <涓�鍙ヨ瘽鍔熻兘绠�杩�>杩斿洖璁惧涓暟 <鍔熻兘璇︾粏鎻忚堪>
     * @return
     * @see [绫汇�佺被#鏂规硶銆佺被#鎴愬憳]
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

        adjustFocus(false);

        refreshDevicesName();

        // 鏀惧ぇ鑾峰緱鐒︾偣鐨勮澶囧悕绉�
        ScaleAnimation focusDeviceScaleAnim = new ScaleAnimation(1.0F, FOCUS_SCALE_RATE, 1.0F, FOCUS_SCALE_RATE);
        focusDeviceScaleAnim.setDuration(0);

        // 娣诲姞璁惧绗竴娆＄缉鏀惧姩鐢�
        Animation focusDeviceTransAnim = new TranslateAnimation(0, TRANSLATE_TO_X, 0, 0);
        focusDeviceScaleAnim.setDuration(0);

        AnimationSet initAnimationSet = new AnimationSet(true);
        initAnimationSet.setFillAfter(true);
        initAnimationSet.setFillEnabled(true);
        initAnimationSet.addAnimation(focusDeviceTransAnim);
        initAnimationSet.addAnimation(focusDeviceScaleAnim);

        // mTextViews[3]涓虹劍鐐硅澶囨樉绀鸿浇浣�
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
     * <涓�鍙ヨ瘽鍔熻兘绠�杩�>璋冩暣鐒︾偣 <鍔熻兘璇︾粏鎻忚堪>
     * @see [绫汇�佺被#鏂规硶銆佺被#鎴愬憳]
     */
    private void adjustFocus(boolean isAddNetWork)
    {
        if (mDevicesList == null || mDeviceNum <= 1)
        {
            mFocusIndex = 0;
        }
        else
        {
            if(isAddNetWork)
            	mFocusIndex = mDevicesList.size() - 1;
            else{
            	refreshFocusIdByFocusDeviceId();
            	 if (mFocusIndex >= mDevicesList.size())
                 {
                     mFocusIndex = mDevicesList.size() - 1;
                 }
            }
        }
        Log.i(TAG, "adjustFocus->mFocusIndex:" + mFocusIndex);
        refreshFocusDeviceId();
    }

    /** 鑾峰彇鐒︾偣璁惧 */
    public DeviceItem getFocusDevice()
    {
        if (mFocusIndex >= mDevicesList.size() || mDevicesList.size() == 0)
        {
            return null;
        }
        return mDevicesList.get(mFocusIndex);
    }

    // 鍒濆鍔ㄧ敾銆傜敱浜庡姩鐢婚渶瑕佺敤鍒癡iew鐨勫睘鎬у�硷紝鎵�浠ュ姩鐢婚渶瑕佸湪鎵�鏈塚iew鍒濆鍖栧畬姣曚箣鍚庯紝鎵嶈兘鍒濆鍖栥��
    private void initAnimation()
    {
        initDownAnimation();
        initUpAnimation();
        initDeviceItemFocusAnimation();

    }

    // 鐒︾偣璁惧鍚嶇О缂╂斁鐜�
    private static final float FOCUS_SCALE_RATE = 1.5f;

    // 鍒嗙被娴锋姤鐢诲粖鐨刌鍧愭爣
    private static final int GY = 250;

    // 涓嬬Щ璺濈
    private static final int DOWN_MOVE_DISTANCE = 200;

    // 涓婄Щ璺濈
    private static final int UP_MOVE_DISTANCE = 50;

    private AnimatorSet mGalleryAnimatorSetDown;

    private AnimationSet mFirstViewSetDown;

    private AnimationSet mSecondViewSetDown;

    private AnimationSet mThirdViewSetDown;

    private AnimationSet mFourthViewSetDown;

    private AnimationSet mFiveViewSetDown;

    // 鍙湁涓�涓澶囨椂鐨勫姩鐢�
    private AnimationSet mSecondViewSetDownOne;

    private AnimationSet mFourthViewSetDownOne;

    private static final float FADEOUT_ALPHA_FROM = 1f;

    private static final float FADEOUT_ALPHA_TO = 0;

    private static final float FADEIN_ALPHA_FROM = 0.1f;

    private static final float FADEIN_ALPHA_TO = 1f;

    // 鍒濆鍖栦笅婊氬姩鐢�
    private void initDownAnimation()
    {
        int setIdx = 1;
        // Gallery alpha鍔ㄧ敾
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

        // 绗竴涓澶囧姩鐢�
        Animation firstViewTranAnim = new TranslateAnimation(0, 0, -(mTextViews[setIdx].getY() - mTextViews[setIdx - 1].getY()), 0);
        firstViewTranAnim.setDuration(ANIMTE_TIME);

        Animation firstViewAlphaAnim = new AlphaAnimation(0f, 0f);
        firstViewAlphaAnim.setDuration(ANIMTE_TIME);

        mFirstViewSetDown = new AnimationSet(true);
        mFirstViewSetDown.setFillAfter(true);
        mFirstViewSetDown.setFillEnabled(true);
        mFirstViewSetDown.addAnimation(firstViewTranAnim);
        mFirstViewSetDown.addAnimation(firstViewAlphaAnim);

        // 绗簩涓澶囧姩鐢�
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

        // 绗笁涓澶囧姩鐢�
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

        // 绗洓涓澶囧姩鐢�
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

        // 绗簲涓澶囧姩鐢�
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

        // ------------鍙湁涓�涓澶囨椂鐨勫姩鐢�------------
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

    // 鍒濆鍖栦笂婊氬姩鐢�
    private void initUpAnimation()
    {
        int setIdx = 1;
        // Gallery alpha鍔ㄧ敾
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

        // 绗浂涓澶囧姩鐢�
        Animation zeroViewTranAnim = new TranslateAnimation(0, 0, mTextViews[setIdx].getY() - mTextViews[setIdx - 1].getY(), 0);
        zeroViewTranAnim.setDuration(ANIMTE_TIME);

        Animation zeroViewAlphaAnim = new AlphaAnimation(0f, 0f);
        zeroViewAlphaAnim.setDuration(ANIMTE_TIME);

        mZeroViewSetUp = new AnimationSet(true);
        mZeroViewSetUp.setFillAfter(true);
        mZeroViewSetUp.setFillEnabled(true);
        mZeroViewSetUp.addAnimation(zeroViewTranAnim);
        mZeroViewSetUp.addAnimation(zeroViewAlphaAnim);

        // 绗竴涓澶囧姩鐢�
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

        // 绗簩涓澶囧姩鐢�
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

        // 绗笁涓澶囧姩鐢�
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

        // 绗洓涓澶囧姩鐢�
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

    // 璁惧item鏀惧ぇ
    private AnimationSet mDeviceItemZoomInSet;

    private AnimationSet mDeviceNameZoomOutSet;

    // 璁惧item缂╁皬
    private AnimationSet mDeviceItemZoomOutSet;

    private void initDeviceItemFocusAnimation()
    {
        // --------鐒︾偣椤硅幏寰楃劍鐐规斁澶у姩鐢�----------
        // 璁惧鍚嶅姩鐢�
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

        // 璁惧item鍔ㄧ敾
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

        // --------鐒︾偣椤硅幏寰楃劍鐐圭缉灏忓姩鐢�----------
        // 璁惧鍚嶅姩鐢�
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

        // 璁惧item鍔ㄧ敾
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
                // 鎵�鏈夐�変腑锛坥nKeyDown锛夊拰鐐逛腑锛坥nSingleTapUp锛変簨浠�,閮藉湪 DeviceItemView2绫讳腑澶勭悊銆�
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
            // 寮瑰嚭杞敭鐩�
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

    /** 璁惧Item鏀惧ぇ */
    private void deviceItemZoomIn()
    {
        if (!mBInitAnima)
        {
            initAnimation();
            mBInitAnima = true;
        }

        // 鐒︾偣璁惧澶勪簬缂╁皬鐘舵�佹椂鎵嶆斁澶�
        if (mBDeviceItemScale)
        {
            mTextViews[TEXTVIEW_THIRD].startAnimation(mDeviceNameZoomInSet);
            mDeviceItem.startAnimation(mDeviceItemZoomInSet);
        }
    }

    /** 璁惧Item缂╁皬 */
    private void deviceItemZoomOut()
    {
        if (!mBInitAnima)
        {
            initAnimation();
            mBInitAnima = true;
        }

        // 鐒︾偣璁惧杩樻病缂╁皬鏃舵墠缂╁皬
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

        // 鐒︾偣鍚戜笅婊氬姩涓�浣�
        if (mFocusIndex > 0)
        {
            mFocusIndex--;
            refreshFocusDeviceId();
        }
        else
        {
            return;
        }

        // 鏇存崲鏁版嵁
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

            // 鎾斁鍔ㄧ敾
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

            // 鎾斁鍔ㄧ敾
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
     * <涓�鍙ヨ瘽鍔熻兘绠�杩�>鍒锋柊璁惧鐨勬樉绀轰綅缃� <鍔熻兘璇︾粏鎻忚堪>
     * @see [绫汇�佺被#鏂规硶銆佺被#鎴愬憳]
     */
    private void refreshDevicesName()
    {
        // 閬垮厤鍑虹幇绌烘寚閽堢殑鏋侀檺鎯呭喌 ouxiaoyong
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
            // 閬垮厤鍑虹幇绌烘寚閽堢殑鏋侀檺鎯呭喌 ouxiaoyong
            if (mDeviceItem == null)
            {
                return;
            }
            mDeviceItem.setVisibility(View.INVISIBLE);
        }
        else
        {
            // 閬垮厤鍑虹幇绌烘寚閽堢殑鏋侀檺鎯呭喌 ouxiaoyong
            if (mDeviceItem == null)
            {
                return;
            }
            mDeviceItem.setVisibility(View.VISIBLE);
            // 鎶婄劍鐐硅澶囩殑鏁版嵁璁剧疆鍒板垎绫籭tem涓�
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
        // 鍥炴敹璁惧鍒嗙被鏄剧ず娴锋姤
        if (mDeviceItem != null)
        {
            mDeviceItem.recycle();
            mDeviceItem = null;
        }
    }
}
