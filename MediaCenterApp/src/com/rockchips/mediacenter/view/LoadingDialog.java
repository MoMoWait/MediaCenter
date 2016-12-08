package com.rockchips.mediacenter.view;


import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import com.rockchips.mediacenter.R;

/**
 * 进度条旋转对话框
 * Created by GaoFei on 2015/11/25.
 */
public class LoadingDialog extends Dialog {

    private Context mContext;
    //对话框是否可以取消
    private boolean isCancelable = true;
    public LoadingDialog(Context context, int theme) {
        super(context, theme);
        this.mContext=context;
    }


    public LoadingDialog(Context context, boolean cancelable) {
        super(context);
        this.isCancelable = cancelable;
        this.mContext=context;		//this.context=context;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_loading);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setCancelable(isCancelable);
        ImageView loadImageView= (ImageView)findViewById(R.id.img_loading);
        Animation loadAnimation= AnimationUtils.loadAnimation(mContext, R.anim.dialog_progress);
        loadImageView.startAnimation(loadAnimation);
    }

}