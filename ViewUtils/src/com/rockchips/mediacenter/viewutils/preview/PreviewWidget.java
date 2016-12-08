package com.rockchips.mediacenter.viewutils.preview;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rockchips.mediacenter.viewutils.R;
import com.rockchips.mediacenter.viewutils.marqueetextview.MarqueeTextView;
import com.rockchips.mediacenter.viewutils.utils.ImageHelper;

/**
 * 
 * Description: 预览控件<br>
 * @author s00211113
 * @version v1.0 Date: 2014-7-11 下午2:50:43<br>
 */
public class PreviewWidget extends LinearLayout
{
    private static final String TAG = "PreviewView";

    protected ImageView mImageView; // 显示缩略图ImageView

    protected ImageView mShadowImageView; // 倒影ImageView

    protected TextView mNameView; // 名字

    protected TextView mOtherView; // 其他说明

    private AnimationSet mImageAnimaSet;

    private AnimationSet mShadowImageAnimaSet;

    private static final int ANIMATION_TIME = 500;

    private static final float IMAGE_ANIMATION_TRANSLATE_Y_FROM = -150;

    private static final float SHADOW_ANIMATION_TRANSLATE_Y_FROM = 50;

    public PreviewWidget(Context context)
    {
        super(context);
        init(context);
    }

    public PreviewWidget(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context);
    }

    public PreviewWidget(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context)
    {
        loadLayout(context);
        setFocusable(false);
        initAnimation();
    }

    private void loadLayout(Context context)
    {
        PreviewWidget.inflate(context, R.layout.preview_layout, this);

        mImageView = (ImageView) findViewById(R.id.preview_image);
        mShadowImageView = (ImageView) findViewById(R.id.preview_image_shadow);
        mNameView = (TextView) findViewById(R.id.preview_name);
        ((MarqueeTextView) mNameView).startMarquee();
        mOtherView = (TextView) findViewById(R.id.preview_other);
    }

    public void setRes(Bitmap bitmap, String name, String other)
    {
        mImageView.setImageBitmap(bitmap);
        Bitmap shadowBitmap = getShadowBitmap(bitmap);

        if (shadowBitmap != null)
        {
            mShadowImageView.setImageBitmap(shadowBitmap);
        }
        mNameView.setText(name);
        mOtherView.setText(other);
    }

    public void updateImage(Bitmap bitmap)
    {
        switchImage(bitmap);
        Bitmap shadowBitmap = getShadowBitmap(bitmap);
        if (shadowBitmap != null)
        {
            switchShadowImage(shadowBitmap);
        }
    }

    public void setImage(Bitmap bitmap)
    {
        mImageView.setImageBitmap(bitmap);
        Bitmap shadowBitmap = getShadowBitmap(bitmap);

        if (shadowBitmap != null)
        {
            mShadowImageView.setImageBitmap(shadowBitmap);
        }
    }

    public void updateName(String name)
    {
        mNameView.setText(name);
    }

    public void updateOtherText(String other)
    {
        mOtherView.setText(other);
    }

    public void switchImage(Bitmap bitmap)
    {
        mImageView.setImageBitmap(bitmap);
        mImageView.startAnimation(mImageAnimaSet);

    }

    public void switchShadowImage(Bitmap bitmap)
    {
        mShadowImageView.setImageBitmap(bitmap);
        mShadowImageView.startAnimation(mShadowImageAnimaSet);
    }

    private void initAnimation()
    {
        if (mImageAnimaSet == null)
        {
            Animation tranAnim = new TranslateAnimation(0, 0, IMAGE_ANIMATION_TRANSLATE_Y_FROM, 0);
            tranAnim.setDuration(ANIMATION_TIME);
            tranAnim.setInterpolator(new AccelerateInterpolator());

            Animation alphaAnim = new AlphaAnimation(0.0f, 1.0f);
            alphaAnim.setDuration(ANIMATION_TIME);

            mImageAnimaSet = new AnimationSet(true);
            mImageAnimaSet.setFillAfter(true);
            mImageAnimaSet.setFillEnabled(true);
            mImageAnimaSet.addAnimation(tranAnim);
            mImageAnimaSet.addAnimation(alphaAnim);
        }

        if (mShadowImageAnimaSet == null)
        {
            Animation tranAnim = new TranslateAnimation(0, 0, SHADOW_ANIMATION_TRANSLATE_Y_FROM, 0);
            tranAnim.setDuration(ANIMATION_TIME);
            tranAnim.setInterpolator(new AccelerateInterpolator());

            Animation alphaAnim = new AlphaAnimation(0.0f, 1.0f);
            alphaAnim.setDuration(ANIMATION_TIME);

            mShadowImageAnimaSet = new AnimationSet(true);
            mShadowImageAnimaSet.setFillAfter(true);
            mShadowImageAnimaSet.setFillEnabled(true);
            mShadowImageAnimaSet.addAnimation(tranAnim);
            mShadowImageAnimaSet.addAnimation(alphaAnim);
        }
    }

    public void showPreview()
    {
        if (mImageAnimaSet == null || mShadowImageAnimaSet == null)
        {
            initAnimation();
        }
        mImageView.startAnimation(mImageAnimaSet);
        mShadowImageView.startAnimation(mShadowImageAnimaSet);
    }

    private Bitmap getShadowBitmap(Bitmap bitmap)
    {
        Bitmap shadowBitmap = null;
        try
        {
            shadowBitmap = ImageHelper.createReflection(bitmap, ImageHelper.REFLECTRATIOOFORIG);
        }
        catch (IllegalArgumentException e)
        {
            Log.d(TAG, e.getLocalizedMessage());

            return null;
        }
        
        return shadowBitmap;
    }
    
    public ImageView getImageView(){
    	return mImageView;
    }
}
