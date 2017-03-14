/**
 * Title: ImageScaleMoveActivity.java<br>
 * Package: com.rockchips.iptv.stb.dlna.imageplayer<br>
 * Description: TODO<br>
 * @author r00178559
 * @version v1.0<br>
 * Date: 2014-2-15下午2:08:34<br> 
 * Copyright © Huawei Technologies Co., Ltd. 2014. All rights reserved.
 */

package com.rockchips.mediacenter.imageplayer;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.rockchips.mediacenter.R;
import com.rockchips.mediacenter.utils.BitmapUtil;
import com.rockchips.mediacenter.utils.IICLOG;
import com.rockchips.mediacenter.utils.StringUtils;
import com.rockchips.mediacenter.service.MultiThreadDownloader;
import com.rockchips.mediacenter.activity.DeviceActivity;

/**
 * Description: TODO<br>
 * @author r00178559
 * @version v1.0 Date: 2014-2-15 下午2:08:34<br>
 */
/* BEGIN: Modified by r00178559 for DTS2014022410217 2014/02/25 */
public class ImageScaleMoveActivity extends DeviceActivity
{
    private static final String TAG = "MediaCenterApp";
    private IICLOG Log = IICLOG.getInstance();
    private enum ENUM_MODE
    {
        SCALE, MOVE
    };

    private ENUM_MODE mMode = ENUM_MODE.SCALE;

    private ImageView mIvDisplay;

    private Bitmap mBmpDisplay;

    private AtomicBoolean mExitActivity = new AtomicBoolean(false);

    private String mBmpFileSavePath;

    private WindowManager mWindowManager;

    private DisplayMetrics mWindowDisplayMetrics;

    private ViewGroup mVgScaleZoomArea;

    private ImageView mIvZoomOut;

    private ImageView mIvZoomIn;

    private ImageView mIvZoomIndictor;

    private ImageView mIvZoomBar;

    private ImageView mIvMoveUpIndictor;

    private ImageView mIvMoveDownIndictor;

    private ImageView mIvMoveLeftIndictor;

    private ImageView mIvMoveRightIndictor;

    private ViewGroup mVgThumbnailDispArea;

    private ImageView mIvThumbnailDisp;

    /* BEGIN: Added by r00178559 for DTS2014031901446 2014/03/19 */
    private TextView mTvHintOk;

    private TextView mTvHintBack;

    /* END: Added by r00178559 for DTS2014031901446 2014/03/19 */

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy()
    {
        mHandlerRefreshImage.removeMessages(0);
        if (null != mBmpDisplay && !mBmpDisplay.isRecycled())
        {
            mBmpDisplay.recycle();
        }
        clrCacheFile();
        super.onDestroy();
    }

    @Override
    public void onBackPressed()
    {
        mExitActivity.set(true);
        if (null != mMultiThreadDownloader)
        {
            mMultiThreadDownloader.stop();
        }
        super.onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER)
        {
            if (mBmpDisplay != null)
            {
                if (ENUM_MODE.SCALE == mMode)
                {
                    changeMode(ENUM_MODE.MOVE);
                    updateUIWhenModeChanged(ENUM_MODE.MOVE);
                    updateMoveStep();
                }
                else if (ENUM_MODE.MOVE == mMode)
                {
                    changeMode(ENUM_MODE.SCALE);
                    updateUIWhenModeChanged(ENUM_MODE.SCALE);
                }

                return true;
            }
        }
        else if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            mExitActivity.set(true);
        }
        else if (keyCode == KeyEvent.KEYCODE_DPAD_UP)
        {
            if (mBmpDisplay != null)
            {
                if (ENUM_MODE.SCALE == mMode)
                {
                    mIvZoomOut.setImageResource(R.drawable.ic_image_scale_zoom_out_pressed);
                    zoomOut();
                    updateZoomIndictor();
                }
                else if (ENUM_MODE.MOVE == mMode)
                {
                    mIvMoveUpIndictor.setImageResource(R.drawable.ic_image_move_up_pressed);
                    moveUp();
                    updateThumbnail();
                }
            }
        }
        else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN)
        {
            if (mBmpDisplay != null)
            {
                if (ENUM_MODE.SCALE == mMode)
                {
                    mIvZoomIn.setImageResource(R.drawable.ic_image_scale_zoom_in_pressed);
                    zoomIn();
                    updateZoomIndictor();
                }
                else if (ENUM_MODE.MOVE == mMode)
                {
                    mIvMoveDownIndictor.setImageResource(R.drawable.ic_image_move_down_pressed);
                    moveDown();
                    updateThumbnail();
                }
            }
        }
        else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT)
        {
            if (mBmpDisplay != null)
            {
                if (ENUM_MODE.MOVE == mMode)
                {
                    mIvMoveLeftIndictor.setImageResource(R.drawable.ic_image_move_left_pressed);
                    moveLeft();
                    updateThumbnail();
                }
            }
        }
        else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
        {
            if (mBmpDisplay != null)
            {
                if (ENUM_MODE.MOVE == mMode)
                {
                    mIvMoveRightIndictor.setImageResource(R.drawable.ic_image_move_right_pressed);
                    moveRight();
                    updateThumbnail();
                }
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_DPAD_UP)
        {
            if (ENUM_MODE.SCALE == mMode)
            {
                mIvZoomOut.setImageResource(R.drawable.ic_image_scale_zoom_out_normal);
            }
            else if (ENUM_MODE.MOVE == mMode)
            {
                mIvMoveUpIndictor.setImageResource(R.drawable.ic_image_move_up_normal);
            }
        }
        else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN)
        {
            if (ENUM_MODE.SCALE == mMode)
            {
                mIvZoomIn.setImageResource(R.drawable.ic_image_scale_zoom_in_normal);
            }
            else if (ENUM_MODE.MOVE == mMode)
            {
                mIvMoveDownIndictor.setImageResource(R.drawable.ic_image_move_down_normal);
            }
        }
        else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT)
        {
            if (ENUM_MODE.MOVE == mMode)
            {
                mIvMoveLeftIndictor.setImageResource(R.drawable.ic_image_move_left_normal);
            }
        }
        else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
        {
            if (ENUM_MODE.MOVE == mMode)
            {
                mIvMoveRightIndictor.setImageResource(R.drawable.ic_image_move_right_normal);
            }
        }

        return super.onKeyUp(keyCode, event);
    }

    /* =========开始MODE切换逻辑及界面显示相关代码======== */
    private void changeMode(ENUM_MODE mode)
    {
        mMode = mode;
    }

    private void updateUIWhenModeChanged(ENUM_MODE newMode)
    {
        if (ENUM_MODE.SCALE == newMode)
        {
            mTvHintOk.setText(R.string.image_scale_mode_ok_hint);
            mTvHintBack.setText(R.string.image_scale_mode_back_hint);
            mVgScaleZoomArea.setVisibility(View.VISIBLE);
            mIvMoveUpIndictor.setVisibility(View.INVISIBLE);
            mIvMoveDownIndictor.setVisibility(View.INVISIBLE);
            mIvMoveLeftIndictor.setVisibility(View.INVISIBLE);
            mIvMoveRightIndictor.setVisibility(View.INVISIBLE);
            mVgThumbnailDispArea.setVisibility(View.INVISIBLE);
        }
        else if (ENUM_MODE.MOVE == newMode)
        {
            mTvHintOk.setText(R.string.image_move_mode_ok_hint);
            mTvHintBack.setText(R.string.image_move_mode_back_hint);
            mVgScaleZoomArea.setVisibility(View.INVISIBLE);
            mIvMoveUpIndictor.setVisibility(View.VISIBLE);
            mIvMoveDownIndictor.setVisibility(View.VISIBLE);
            mIvMoveLeftIndictor.setVisibility(View.VISIBLE);
            mIvMoveRightIndictor.setVisibility(View.VISIBLE);
            mVgThumbnailDispArea.setVisibility(View.INVISIBLE);
            updateThumbnail();
        }
        else
        {
            Log.e(TAG, "updateUIWhenModeChanged but invalid mode " + newMode);
        }
    }

    /* =========结束MODE切换逻辑及界面显示相关代码======== */

    /* =========开始缩放逻辑及图片操作相关代码======== */
    private static final float ZOOM_STEP_PERCENT = 0.2f;

    private static final float ZOOM_OUT_STEP_PERCENT = 1.0f + ZOOM_STEP_PERCENT;

    private static final float ZOOM_IN_STEP_PERCENT = 1.0f - ZOOM_STEP_PERCENT;

    private static final float ZOOM_OUT_MAX_FACTOR = 8.0f;

    private float ZOOM_IN_MIN_FACTOR = 1.0f;

    private static final float MOVE_STEP_PERCENT = 0.2f;

    private BitmapFactory.Options mImageOptions;

    private void getImageOptions(String filePath)
    {
        mImageOptions = BitmapUtil.getBitmapOptions(filePath);
    }

    private void calcZoomInMinFactor(Bitmap bmp)
    {
        if (0 >= bmp.getWidth() || 0 >= bmp.getHeight())
        {
            return;
        }
        float scaleX = ((float) mWindowDisplayMetrics.widthPixels) / bmp.getWidth();
        float scaleY = ((float) mWindowDisplayMetrics.heightPixels) / bmp.getHeight();

        ZOOM_IN_MIN_FACTOR = (scaleX < scaleY) ? scaleX : scaleY;
        if (1.0f < ZOOM_IN_MIN_FACTOR)
        {
            ZOOM_IN_MIN_FACTOR = 1.0f;
        }
        else if (0f > ZOOM_IN_MIN_FACTOR)
        {
            ZOOM_IN_MIN_FACTOR = 0.1f;
        }
        Log.d(TAG, "calcZoomInMinFactor = " + ZOOM_IN_MIN_FACTOR);
    }

    private void zoomOut()
    {
        float[] values = new float[9];
        mIvDisplay.getImageMatrix().getValues(values);
        if (values[0] * mIvDisplay.getScaleX() >= ZOOM_OUT_MAX_FACTOR || values[4] * mIvDisplay.getScaleY() >= ZOOM_OUT_MAX_FACTOR)
        {
            return;
        }

        // 图片还未全部展开则先放大图片
        if (1.0f > values[0] || 1.0f > values[4])
        {
            float newScaleX = values[0] * ZOOM_OUT_STEP_PERCENT;
            float newScaleY = values[4] * ZOOM_OUT_STEP_PERCENT;
            float maxScale = (newScaleX > newScaleY) ? newScaleX : newScaleY;
            if (maxScale > 1.0f)
            {
                newScaleX = 1.0f;
                newScaleY = 1.0f;
            }
            Log.d(TAG, "zoomOut 1 newScaleX = " + newScaleX + ", newScaleY = " + newScaleY);
            Matrix matrix = getZoomMatrix(newScaleX, newScaleY);
            mIvDisplay.setImageMatrix(matrix);
        }
        else
        {
            float newScaleX = mIvDisplay.getScaleX() * ZOOM_OUT_STEP_PERCENT;
            float newScaleY = mIvDisplay.getScaleY() * ZOOM_OUT_STEP_PERCENT;
            if (ZOOM_OUT_MAX_FACTOR < newScaleX)
            {
                newScaleX = ZOOM_OUT_MAX_FACTOR;
            }
            if (ZOOM_OUT_MAX_FACTOR < newScaleY)
            {
                newScaleY = ZOOM_OUT_MAX_FACTOR;
            }
            Log.d(TAG, "zoomOut 2 newScaleX = " + newScaleX + ", newScaleY = " + newScaleY);

            float newTranslateX = mIvDisplay.getTranslationX();
            if (1.0f != mIvDisplay.getScaleX())
            {
                newTranslateX *= (newScaleX - 1.0f) / (mIvDisplay.getScaleX() - 1.0f);
            }
            float newTransLateY = mIvDisplay.getTranslationY();
            if (1.0f != mIvDisplay.getScaleY())
            {
                newTransLateY *= (newScaleY - 1.0f) / (mIvDisplay.getScaleY() - 1.0f);
            }

            mIvDisplay.setScaleX(newScaleX);
            mIvDisplay.setScaleY(newScaleY);
            mIvDisplay.setTranslationX(newTranslateX);
            mIvDisplay.setTranslationY(newTransLateY);
        }
    }

    private void zoomIn()
    {
        float[] values = new float[9];
        mIvDisplay.getImageMatrix().getValues(values);
        if (values[0] * mIvDisplay.getScaleX() <= ZOOM_IN_MIN_FACTOR || values[4] * mIvDisplay.getScaleY() <= ZOOM_IN_MIN_FACTOR)
        {
            return;
        }

        // 画布还未恢复原始大小，则优先恢复画布
        if (1.0f < mIvDisplay.getScaleX() || 1.0f < mIvDisplay.getScaleY())
        {
            float newScaleX = mIvDisplay.getScaleX() * ZOOM_IN_STEP_PERCENT;
            float newScaleY = mIvDisplay.getScaleY() * ZOOM_IN_STEP_PERCENT;
            float minScale = (newScaleX < newScaleY) ? newScaleX : newScaleY;
            if (minScale < 1.0f)
            {
                newScaleX = 1.0f;
                newScaleY = 1.0f;
            }
            Log.d(TAG, "zoomIn 1 newScaleX = " + newScaleX + ", newScaleY = " + newScaleY);

            if (1.0f > newScaleX)
            {
                newScaleX = 1.0f;
            }
            if (1.0f > newScaleY)
            {
                newScaleY = 1.0f;
            }

            float newTranslateX = mIvDisplay.getTranslationX();
            if (1.0f != mIvDisplay.getScaleX())
            {
                newTranslateX *= (newScaleX - 1.0f) / (mIvDisplay.getScaleX() - 1.0f);
            }
            float newTransLateY = mIvDisplay.getTranslationY();
            if (1.0f != mIvDisplay.getScaleY())
            {
                newTransLateY *= (newScaleY - 1.0f) / (mIvDisplay.getScaleY() - 1.0f);
            }

            mIvDisplay.setScaleX(newScaleX);
            mIvDisplay.setScaleY(newScaleY);
            mIvDisplay.setTranslationX(newTranslateX);
            mIvDisplay.setTranslationY(newTransLateY);
        }
        else
        {
            float newScaleX = values[0] * ZOOM_IN_STEP_PERCENT;
            float newScaleY = values[4] * ZOOM_IN_STEP_PERCENT;

            float minScale = (newScaleX < newScaleY) ? newScaleX : newScaleY;
            if (minScale < ZOOM_IN_MIN_FACTOR)
            {
                newScaleX = newScaleX * ZOOM_IN_MIN_FACTOR / minScale;
                newScaleY = newScaleY * ZOOM_IN_MIN_FACTOR / minScale;
            }
            Log.d(TAG, "zoomIn 2 newScaleX = " + newScaleX + ", newScaleY = " + newScaleY);

            Matrix matrix = getZoomMatrix(newScaleX, newScaleY);
            mIvDisplay.setImageMatrix(matrix);
        }
    }

    private Matrix getZoomMatrix(float scaleX, float scaleY)
    {
        float[] values = new float[9];
        mIvDisplay.getImageMatrix().getValues(values);
        values[0] = scaleX;
        values[4] = scaleY;
        values[8] = 1.0f;
        Log.d(TAG, "getZoomMatrix getWidth = " + mBmpDisplay.getWidth() + ", scaleX = " + scaleX);
        if (mWindowDisplayMetrics.widthPixels <= mBmpDisplay.getWidth())
        {
            values[2] = (mWindowDisplayMetrics.widthPixels - values[0] * mBmpDisplay.getWidth()) / 2.0f;
        }
        else
        {
            values[2] = (1.0f - values[0]) * mBmpDisplay.getWidth() / 2.0f;
        }
        if (mWindowDisplayMetrics.heightPixels < mBmpDisplay.getHeight())
        {
            values[5] = (mWindowDisplayMetrics.heightPixels - values[4] * mBmpDisplay.getHeight()) / 2.0f;
        }
        else
        {
            values[5] = (1.0f - values[4]) * mBmpDisplay.getHeight() / 2.0f;
        }
        Matrix matrix = new Matrix();
        matrix.setValues(values);
        Log.d(TAG, "getZoomMatrix values = " + Arrays.toString(values));
        return matrix;
    }

    private void updateZoomIndictor()
    {
        int totalLength = mIvZoomBar.getHeight() - mIvZoomIndictor.getHeight();
        float[] values = new float[9];
        mIvDisplay.getImageMatrix().getValues(values);
        float translatePercent = (values[4] * mIvDisplay.getScaleY() - ZOOM_IN_MIN_FACTOR) / (ZOOM_OUT_MAX_FACTOR - ZOOM_IN_MIN_FACTOR);
        int indictorTranslate = (int) (totalLength * translatePercent);
        if (0 > indictorTranslate)
        {
            indictorTranslate = 0;
        }
        if (totalLength < indictorTranslate)
        {
            indictorTranslate = totalLength;
        }
        Log.d(TAG, "updateZoomIndictor indictorTranslate = " + (-indictorTranslate));
        mIvZoomIndictor.setTranslationY(-indictorTranslate);
    }

    private void loadBitmapAsBigAsPossibleInBackground()
    {
        if (mExitActivity.get())
        {
            return;
        }

        new Thread(mLoadBitmapAsBigAsPossibleRunnable).start();
    }

    private Runnable mLoadBitmapAsBigAsPossibleRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            Bitmap bmp = null;
            final float decStep = 0.1f;
            float curScale = 1.0f;

            if (StringUtils.isNetworkURI(ImageScaleMoveData.mBitmapFileUrl))
            {
                getHttpFileSync(ImageScaleMoveData.mBitmapFileUrl);
                mBmpFileSavePath = getSelfCacheFilePath();
                if (mExitActivity.get())
                {
                    return;
                }
            }
            else
            {
                mBmpFileSavePath = ImageScaleMoveData.mBitmapFileUrl;
            }
            getImageOptions(mBmpFileSavePath);

            int maxWidth = mImageOptions.outWidth;
            int maxHeight = mImageOptions.outHeight;
            if (0 >= maxWidth || 0 >= maxHeight)
            {
                maxWidth = (int) (mWindowDisplayMetrics.widthPixels * ZOOM_OUT_MAX_FACTOR);
                maxHeight = (int) (mWindowDisplayMetrics.heightPixels * ZOOM_OUT_MAX_FACTOR);
            }
            Log.d(TAG, "load bmp maxWidth = " + maxWidth + ", maxHeight = " + maxHeight);
            int destWidth = maxWidth;
            int destHeight = maxHeight;
            while (curScale > 0f)
            {
                destWidth = (int) (maxWidth * curScale);
                destHeight = (int) (maxHeight * curScale);

                curScale -= decStep;
                bmp = BitmapUtil.getBitmapByPath(mBmpFileSavePath, destWidth, destHeight);
                if (null != bmp)
                {
                    Log.d(TAG, "load bmp destWidth = " + destWidth + ", destHeight = " + destHeight);
                    Log.d(TAG, "load bmp bmpWidth = " + bmp.getWidth() + ", bmpHeight = " + bmp.getHeight());
                    calcZoomInMinFactor(bmp);
                    break;
                }
                if (mExitActivity.get())
                {
                    break;
                }
            }

            if (mExitActivity.get())
            {
                return;
            }
            Message msg = Message.obtain();
            msg.what = 0;
            msg.obj = bmp;
            mHandlerRefreshImage.sendMessage(msg);
        }
    };

    private Handler mHandlerRefreshImage = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            mBmpDisplay = (Bitmap) msg.obj;
            if (null != mBmpDisplay)
            {
                Log.d(TAG, "load bmp mBmpDisplayWidth = " + mBmpDisplay.getWidth() + ", mBmpDisplayHeight = " + mBmpDisplay.getHeight());
                mIvDisplay.setImageBitmap(mBmpDisplay);
                Matrix matrix = getZoomMatrix(ZOOM_IN_MIN_FACTOR, ZOOM_IN_MIN_FACTOR);
                mIvDisplay.setImageMatrix(matrix);
            }
        }
    };

    private static final String CACHE_FILE_NAME = "cacheFile";

    private String getSelfCacheFilePath()
    {
        return getCacheDir().getAbsolutePath() + File.separator + CACHE_FILE_NAME;
    }

    private void clrCacheFile()
    {
        File cacheFile = new File(getSelfCacheFilePath());

        if (cacheFile.exists())
        {
            cacheFile.delete();
        }
    }

    private MultiThreadDownloader mMultiThreadDownloader;

    private void getHttpFileSync(String path)
    {
        String savePath = getSelfCacheFilePath();
        Log.d(TAG, "getHttpFile savePath = " + savePath);
        try
        {
            mMultiThreadDownloader = new MultiThreadDownloader(new URL(ImageScaleMoveData.mBitmapFileUrl), new File(savePath));
            mMultiThreadDownloader.setExecutorPoolSize(200 * 1024);
            mMultiThreadDownloader.setChunkSize(2 * 1024 * 1024);
            mMultiThreadDownloader.setTempDir(getCacheDir().getAbsolutePath());
            mMultiThreadDownloader.download();
        }
        catch (MalformedURLException e)
        {
        }
    }

    /* =========结束缩放逻辑及图片操作相关代码======== */

    /* =========开始移动逻辑及图片操作相关代码======== */
    private int mMoveVerticalStep;

    private int mMoveHorizontalStep;

    private void initMoveStep()
    {
        mMoveHorizontalStep = (int) (mWindowDisplayMetrics.widthPixels * MOVE_STEP_PERCENT);
        mMoveVerticalStep = (int) (mWindowDisplayMetrics.heightPixels * MOVE_STEP_PERCENT);
    }

    private void updateMoveStep()
    {
        mMoveHorizontalStep = (int) (mBmpDisplay.getWidth() * ZOOM_IN_MIN_FACTOR * MOVE_STEP_PERCENT);
        mMoveVerticalStep = (int) (mBmpDisplay.getHeight() * ZOOM_IN_MIN_FACTOR * MOVE_STEP_PERCENT);
    }

    private void moveUp()
    {
        float[] values = new float[9];
        mIvDisplay.getImageMatrix().getValues(values);
        if (0f > values[5])
        {
            values[5] += mMoveVerticalStep;
            if (mWindowDisplayMetrics.heightPixels < mBmpDisplay.getHeight())
            {
                if (0f < values[5])
                {
                    values[5] = 0f;
                }
            }
            Log.d(TAG, "moveUp values = " + Arrays.toString(values));
            Matrix matrix = new Matrix();
            matrix.setValues(values);
            mIvDisplay.setImageMatrix(matrix);
        }
        else
        {
            float translationY = mIvDisplay.getTranslationY();
            translationY += mMoveVerticalStep;
            float maxTranslateY = getTranslateYLimit();
            Log.d(TAG, "moveUp maxTranslateY = " + maxTranslateY);
            if (maxTranslateY < translationY)
            {
                translationY = maxTranslateY;
            }
            Log.d(TAG, "moveUp translationY = " + translationY);
            mIvDisplay.setTranslationY(translationY);
        }
    }

    private void moveDown()
    {
        float[] values = new float[9];
        mIvDisplay.getImageMatrix().getValues(values);
        float minVerticalTranslate = mWindowDisplayMetrics.heightPixels - values[4] * mBmpDisplay.getHeight();
        if (minVerticalTranslate < values[5])
        {
            values[5] -= mMoveVerticalStep;
            if (mWindowDisplayMetrics.heightPixels < mBmpDisplay.getHeight())
            {
                if (minVerticalTranslate > values[5])
                {
                    values[5] = minVerticalTranslate;
                }
            }
            Log.d(TAG, "moveDown values = " + Arrays.toString(values));
            Matrix matrix = new Matrix();
            matrix.setValues(values);
            mIvDisplay.setImageMatrix(matrix);
        }
        else
        {
            float translationY = mIvDisplay.getTranslationY();
            translationY -= mMoveVerticalStep;
            float minTranslateY = -getTranslateYLimit();
            Log.d(TAG, "moveDown minTranslateY = " + minTranslateY);
            if (minTranslateY > translationY)
            {
                translationY = minTranslateY;
            }
            Log.d(TAG, "moveDown translationY = " + translationY);
            mIvDisplay.setTranslationY(translationY);
        }
    }

    private float getTranslateYLimit()
    {
        float limitTranslateY = 0f;
        if (mWindowDisplayMetrics.heightPixels < mBmpDisplay.getHeight())
        {
            limitTranslateY = (mIvDisplay.getScaleY() - 1.0f) * mIvDisplay.getHeight() / 2;
        }
        else
        {
            if (mIvDisplay.getScaleY() * mIvDisplay.getHeight() <= mWindowDisplayMetrics.heightPixels)
            {
                limitTranslateY = 0f;
            }
            else
            {
                limitTranslateY = (mIvDisplay.getScaleY() * mIvDisplay.getHeight() - mWindowDisplayMetrics.heightPixels) / 2.0f;
            }
        }
        return limitTranslateY;
    }

    private void moveLeft()
    {
        float[] values = new float[9];
        mIvDisplay.getImageMatrix().getValues(values);
        if (0f > values[2])
        {
            values[2] += mMoveHorizontalStep;
            if (mWindowDisplayMetrics.widthPixels < mBmpDisplay.getWidth())
            {
                if (0f < values[2])
                {
                    values[2] = 0f;
                }
            }
            Log.d(TAG, "moveLeft values = " + Arrays.toString(values));
            Matrix matrix = new Matrix();
            matrix.setValues(values);
            mIvDisplay.setImageMatrix(matrix);
        }
        else
        {
            float translationX = mIvDisplay.getTranslationX();
            translationX += mMoveHorizontalStep;
            float maxTranslateX = getTranslateXLimit();
            Log.d(TAG, "moveLeft maxTranslateX = " + maxTranslateX);
            if (maxTranslateX < translationX)
            {
                translationX = maxTranslateX;
            }
            Log.d(TAG, "moveLeft translationX = " + translationX);
            mIvDisplay.setTranslationX(translationX);
        }
    }

    private void moveRight()
    {
        float[] values = new float[9];
        mIvDisplay.getImageMatrix().getValues(values);
        float minHorizontalTranslate = mWindowDisplayMetrics.widthPixels - values[0] * mBmpDisplay.getWidth();
        if (minHorizontalTranslate < values[2])
        {
            values[2] -= mMoveHorizontalStep;
            if (mWindowDisplayMetrics.widthPixels < mBmpDisplay.getWidth())
            {
                if (minHorizontalTranslate > values[2])
                {
                    values[2] = minHorizontalTranslate;
                }
            }
            Log.d(TAG, "moveRight values = " + Arrays.toString(values));
            Matrix matrix = new Matrix();
            matrix.setValues(values);
            mIvDisplay.setImageMatrix(matrix);
        }
        else
        {
            float translationX = mIvDisplay.getTranslationX();
            translationX -= mMoveHorizontalStep;
            float minTranslateX = -getTranslateXLimit();
            Log.d(TAG, "moveRight minTranslateX = " + minTranslateX);
            if (minTranslateX > translationX)
            {
                translationX = minTranslateX;
            }
            Log.d(TAG, "moveRight translationX = " + translationX);
            mIvDisplay.setTranslationX(translationX);
        }
    }

    private float getTranslateXLimit()
    {
        float limitTranslateX = 0f;
        if (mWindowDisplayMetrics.widthPixels < mBmpDisplay.getWidth())
        {
            limitTranslateX = (mIvDisplay.getScaleX() - 1.0f) * mIvDisplay.getWidth() / 2;
        }
        else
        {
            if (mIvDisplay.getScaleX() * mIvDisplay.getWidth() <= mWindowDisplayMetrics.widthPixels)
            {
                limitTranslateX = 0f;
            }
            else
            {
                limitTranslateX = (mIvDisplay.getScaleX() * mIvDisplay.getWidth() - mWindowDisplayMetrics.widthPixels) / 2.0f;
            }
        }
        return limitTranslateX;
    }

    /* =========结束移动逻辑及图片操作相关代码======== */

    /* =========开始移动示意缩略图相关代码======== */
    private void updateThumbnail()
    {
        mIvThumbnailDisp.setImageBitmap(mBmpDisplay);
    }
    /* =========结束移动示意缩略图相关代码======== */

	@Override
	public void onServiceConnected() {
		
	}

	@Override
	public int getLayoutRes() {
		return R.layout.layout_image_scale_move;
	}

	@Override
	public void init() {
		mIvDisplay = (ImageView) findViewById(R.id.iv_ivscalemove_image);
		mTvHintOk = (TextView) findViewById(R.id.tv_hint_ok_text);
		mTvHintBack = (TextView) findViewById(R.id.tv_hint_back_text);
		mVgScaleZoomArea = (ViewGroup) findViewById(R.id.rl_scale_zoom_area);
		mIvZoomOut = (ImageView) findViewById(R.id.iv_scale_mode_zoom_out);
		mIvZoomIn = (ImageView) findViewById(R.id.iv_scale_mode_zoom_in);
		mIvZoomIndictor = (ImageView) findViewById(R.id.iv_scale_mode_zoom_indictor);
		mIvZoomBar = (ImageView) findViewById(R.id.iv_scale_mode_zoom_bar);
		mIvMoveUpIndictor = (ImageView) findViewById(R.id.iv_move_mode_up_indictor);
		mIvMoveDownIndictor = (ImageView) findViewById(R.id.iv_move_mode_down_indictor);
		mIvMoveLeftIndictor = (ImageView) findViewById(R.id.iv_move_mode_left_indictor);
		mIvMoveRightIndictor = (ImageView) findViewById(R.id.iv_move_mode_right_indictor);
		mVgThumbnailDispArea = (ViewGroup) findViewById(R.id.rl_move_mode_thumbnail_area);
		mIvThumbnailDisp = (ImageView) findViewById(R.id.iv_move_mode_thumbnail_disp);
		updateUIWhenModeChanged(mMode);
		mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		mWindowDisplayMetrics = new DisplayMetrics();
		mWindowManager.getDefaultDisplay().getMetrics(mWindowDisplayMetrics);
		initMoveStep();
		loadBitmapAsBigAsPossibleInBackground();
	}
}
/* END: Modified by r00178559 for DTS2014022410217 2014/02/25 */
