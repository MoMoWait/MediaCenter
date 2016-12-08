package com.rockchips.mediacenter.viewutils.tipdialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.rockchips.mediacenter.viewutils.R;

/**
 * 提示对话框
 * @author s00211113
 * 
 */
public class TipDialog extends Dialog implements OnClickListener, OnKeyListener
{
    private static final String TAG = "TipDialog";

    private View mView;

    private TextView mTvTip;

    private Button mBtnLeft;

    private Button mBtnRight;

    /**
     * 对话框按钮点击回调
     * @author s00211113
     * 
     */
    public interface OnTipDialogClickListener
    {
        void onTipDialogClick(String tip, boolean isLeft);
    }

    private OnTipDialogClickListener mOnTipDialogClickListener;

    public TipDialog(Context context)
    {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mView = View.inflate(context, R.layout.tipdialog, null);
        mTvTip = (TextView) mView.findViewById(R.id.tv_dialog_tip);
        mBtnLeft = (Button) mView.findViewById(R.id.bt_dialog_left);
        mBtnRight = (Button) mView.findViewById(R.id.bt_dialog_right);
        setContentView(mView);
        windowDeploy();
    }

    public boolean setParams(String tip, String leftBtn, String rightBtn, OnTipDialogClickListener l)
    {
        if (tip == null || leftBtn == null && rightBtn == null)
        {
            return false;
        }

        mTvTip.setText(tip);

        if (leftBtn != null)
        {
            mBtnLeft.setText(leftBtn);
            mBtnLeft.setVisibility(View.VISIBLE);
            mBtnLeft.setOnClickListener(this);
            mBtnLeft.setClickable(true);
            mBtnLeft.setFocusable(true);
            mBtnLeft.requestFocus();
            mBtnLeft.setBackgroundResource(R.drawable.menu_focus);
        }
        else
        {
            mBtnLeft.setVisibility(View.GONE);
        }

        if (rightBtn != null)
        {
            mBtnRight.setText(rightBtn);
            mBtnRight.setVisibility(View.VISIBLE);
            mBtnRight.setOnClickListener(this);
            mBtnRight.setClickable(true);
            mBtnRight.setFocusable(true);
            if (leftBtn != null)
            {
                mBtnRight.setBackgroundResource(R.color.transparent_color);
            }
            else
            {
                mBtnRight.setBackgroundResource(R.drawable.menu_focus);
            }
        }
        else
        {
            mBtnRight.setVisibility(View.GONE);
        }

        if (leftBtn != null && rightBtn != null)
        {
            setOnKeyListener(this);
        }
        else
        {
            setOnKeyListener(null);
        }

        mOnTipDialogClickListener = l;

        return true;
    }

    @Override
    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event)
    {
        if (event.getAction() == KeyEvent.ACTION_DOWN)
        {
            Log.d(TAG, "mDeleteDlgKeyListener onKey() keyCode:" + keyCode);
            switch (keyCode)
            {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    mBtnLeft.requestFocus();
                    mBtnLeft.setBackgroundResource(R.drawable.menu_focus);
                    mBtnRight.setBackgroundResource(R.color.transparent_color);
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    mBtnRight.requestFocus();
                    mBtnRight.setBackgroundResource(R.drawable.menu_focus);
                    mBtnLeft.setBackgroundResource(R.color.transparent_color);
                    break;
                default:
                    break;
            }
        }

        return false;
    }

    @Override
    public void onClick(View v)
    {
        dismiss();
        if (mOnTipDialogClickListener != null)
        {
            mOnTipDialogClickListener.onTipDialogClick(mTvTip.getText().toString(), v.getId() == R.id.bt_dialog_left);
        }
    }

    @SuppressLint("InlinedApi")
	private void windowDeploy()
    {
        Window window = getWindow(); // 得到对话框
        window.setBackgroundDrawableResource(R.color.transparent_color); // 设置对话框背景为透明
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.gravity = Gravity.CENTER; // 设置重力
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        window.getDecorView().setSystemUiVisibility(View.INVISIBLE);
        window.setAttributes(wl);
    }
}
