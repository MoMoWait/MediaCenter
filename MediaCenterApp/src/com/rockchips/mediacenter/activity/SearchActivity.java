package com.rockchips.mediacenter.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.rockchips.mediacenter.R;
import com.rockchips.mediacenter.basicutils.constant.Constant;
import com.rockchips.mediacenter.viewutils.animfocusimage.CircleRectFocus;
import com.rockchips.mediacenter.viewutils.animfocusimage.FocusImageView;
import com.rockchips.mediacenter.viewutils.toast.ToastUtil;

/**
 * SR-0000382162 媒体中心USB外接设备的内容展示 AR-0000698363 媒体内容搜索功能 <一句话功能简述> <功能详细描述>
 * 
 * @author oWX194981
 * @version [版本号, 2014-4-10]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class SearchActivity extends AppBaseActivity implements Button.OnClickListener
{
    protected static final String TAG = "MediaCenterApp";

    private EditText mEditText;

    private Button mBtnSearch;

    private String SEARCH_KEY = "search_key";

    private InputMethodManager imm = null;

    private CircleRectFocus mCRF = new CircleRectFocus();

    private FocusImageView mFIV = null;

    private Bitmap mBmpL;

    private Bitmap mBmpT;

    private Bitmap mBmpR;

    private Bitmap mBmpB;

    private Bitmap mBmpLT;

    private Bitmap mBmpLB;

    private Bitmap mBmpRT;

    private Bitmap mBmpRB;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.mymedia_search);
        mEditText = (EditText) findViewById(R.id.keyword);
        mBtnSearch = (Button) findViewById(R.id.btn_confirm);
        mFIV = (FocusImageView) findViewById(R.id.focus);
        // 设置四个角
        mBmpL = getBitmapByIdNoCache(this, R.drawable.left);
        mBmpT = getBitmapByIdNoCache(this, R.drawable.top);
        mBmpR = getBitmapByIdNoCache(this, R.drawable.right);
        mBmpB = getBitmapByIdNoCache(this, R.drawable.bottom);
        mBmpLT = getBitmapByIdNoCache(this, R.drawable.left_top_center);
        mBmpLB = getBitmapByIdNoCache(this, R.drawable.left_bottom_center);
        mBmpRT = getBitmapByIdNoCache(this, R.drawable.right_top_center);
        mBmpRB = getBitmapByIdNoCache(this, R.drawable.right_bottom_center);
        // 设置四个角 + 边
        mCRF.setBmpTop(mBmpT);
        mCRF.setBmpLeft(mBmpL);
        mCRF.setBmpRight(mBmpR);
        mCRF.setBmpBottom(mBmpB);
        mCRF.setBmpLeftTop(mBmpLT);
        mCRF.setBmpLeftBottom(mBmpLB);
        mCRF.setBmpRightTop(mBmpRT);
        mCRF.setBmpRightBottom(mBmpRB);

        mCRF.setBmpContext(getBitmapByIdNoCache(this, R.drawable.none));

        mCRF.setAlpha(1f);

        mFIV.setAlpha(1f);
        mFIV.setFocus(mCRF);

        mFIV.setLeft(200);
        mFIV.setTop(200);
        mFIV.setRight(500);
        mFIV.setBottom(500);

        Rect srcRect;
        Rect dstRect;

        srcRect = new Rect(282, 0, 790, 100);
        dstRect = new Rect(780, 0, 960, 100);

        float srcAlpha = 1f;
        float dstAlpha = 1f;

        mFIV.setSrcRect(srcRect);
        mFIV.setDstRect(dstRect);

        mFIV.setSrcAlpha(srcAlpha);
        mFIV.setDstAlpha(dstAlpha);

        mEditText.setFocusable(true);
        mEditText.requestFocus();

        mBtnSearch.setOnClickListener(this);
        mEditText.setOnClickListener(this);

        imm = (InputMethodManager) SearchActivity.this.getSystemService(INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        imm.showSoftInput(mEditText, 0);

        mEditText.setOnEditorActionListener(new OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                {
                    String key = mEditText.getText().toString();
                    if (key == null || key.length() <= 0)
                    {
                        ToastUtil.showToastContent(getString(R.string.search_tips), Gravity.BOTTOM, 0, 0);
                        return false;
                    }
                    Intent intent = new Intent();
                    intent.setClass(SearchActivity.this, FileListActivity.class);
                    intent.putExtra(Constant.EXTRA_IS_SEARCH, true);
                    //intent.putExtra(BaseActivity.SEARCH_KEY, key);
                    startActivity(intent);
                    SearchActivity.this.finish();
                }
                return false;
            }

        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        boolean ret = super.onKeyDown(keyCode, event);
        switch (keyCode)
        {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (mEditText.hasFocus())
                {
                    mBtnSearch.requestFocus();
                    mFIV.startAnimator();
                }
                else if (mBtnSearch.hasFocus())
                {
                    mEditText.requestFocus();
                    mFIV.startAnimator();

                }
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (mBtnSearch.hasFocus())
                {
                    mEditText.requestFocus();
                    mFIV.startAnimator();
                }
                else if (mEditText.hasFocus())
                {
                    mBtnSearch.requestFocus();
                    mFIV.startAnimator();
                }

                break;
            default:
                break;
        }

        return ret;
    }

    public void onClick(View v)
    {
        int id = v.getId();
        switch (id)
        {
            case R.id.btn_confirm:
                String key = mEditText.getText().toString();
                if (key == null || key.length() <= 0)
                {
                    ToastUtil.showToastContent(getString(R.string.search_tips), Gravity.BOTTOM, 0, 0);
                    return;
                }
                Intent intent = new Intent();
                intent.setClass(SearchActivity.this, FileListActivity.class);
                intent.putExtra(Constant.EXTRA_IS_SEARCH, true);
                //intent.putExtra(BaseActivity.SEARCH_KEY, key);
                startActivity(intent);
                SearchActivity.this.finish();
                break;
            // 解决4.2第一次进入调用软键盘，无法调出来
            case R.id.keyword:
                imm.showSoftInput(mEditText, 0);
                break;
            default:
                break;

        }
    }

    // 按re-search键时，保存检索关键字
    @Override
    protected void onResume()
    {
        Intent intent = getIntent();

        if (intent != null && intent.hasExtra(SEARCH_KEY))
        {
            String key = intent.getStringExtra(SEARCH_KEY);
            if (key != null && key.trim().length() != 0 && mEditText != null)
            {
                mEditText.setText(key);

                mEditText.setSelection(key.length());
            }
        }

        super.onResume();
    }

    private static synchronized Bitmap getBitmapByIdNoCache(Context context, int drawResid)
    {
        Bitmap bmp = ((BitmapDrawable) (context.getResources().getDrawable(drawResid))).getBitmap();
        return bmp;
    }
}
