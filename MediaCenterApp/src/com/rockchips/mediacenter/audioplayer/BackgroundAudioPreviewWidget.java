package com.rockchips.mediacenter.audioplayer;

import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.media.MediaMetadataRetriever;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rockchips.mediacenter.R;
import com.rockchips.mediacenter.bean.LocalMediaInfo;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.utils.BitmapUtil;
import com.rockchips.mediacenter.utils.IICLOG;
import com.rockchips.mediacenter.retrieve.EncodeUtil;
import com.rockchips.mediacenter.retrieve.RetrieveCompleteListener;
import com.rockchips.mediacenter.retrieve.RetrieveInfoManager;

/**
 * 功能简介：
 *  音乐信息悬浮框
 *  内部封装了获取缩略图的逻辑
 *  author:wanghuanlai
 *  modify date:2014.3.6
 */

public class BackgroundAudioPreviewWidget extends LinearLayout
{
    private final String TAG = "MediaCenterApp";
    protected static final IICLOG Log = IICLOG.getInstance();
    private Context mContext = null; 

    //图片，歌名，专辑名，艺术家
    private TextView signerTextView;
    
    private TextView mTextViewName;
    
    private ImageView mImageView;
    
    private TextView albumTextView;
    
    //用来保存上面四个控件的变量
    private String diaplayName;
    
    private String artistFromRetriever;
    
    private String albumFromRetriever;
    
    private Bitmap mDefaultMusicBitmap;
    
    //默认的缩略图大小
    private int mThumbnailWidth = 170;
    
    private int mThumbnailHeight = 170;
    
    //媒体文件信息
    private LocalMediaInfo mMediaBaseInfo;
    
    public BackgroundAudioPreviewWidget(Context context)
    {
        super(context);
        mContext = context;
        loadLayout(mContext);
        // TODO Auto-generated constructor stub
    }

    /**
     * 加载布局
     */
    protected void loadLayout(Context context)
    {
        inflate(context, R.layout.background_audio_info, this);
        mImageView = (ImageView)findViewById(R.id.item_iconview);
        mTextViewName = (TextView)findViewById(R.id.song_name);
        signerTextView = (TextView)findViewById(R.id.background_audio_signer);
        albumTextView = (TextView)findViewById(R.id.background_audio_album);
    }

    public void setBaseMediaInfo(LocalMediaInfo LocalMediaInfo)
    {
        if (LocalMediaInfo == null)
        {
            return;
        }

        Log.d(TAG, "setBaseMediaInfo()--IN--");
        initBgAudioInfo();
        mMediaBaseInfo = LocalMediaInfo;
        diaplayName = mMediaBaseInfo.getmFileName();
        initWindowManeger(mContext);
        if (mUIHandler == null)
        {
            mUIHandler = new UIHandler();
        }

        loadDefaultMusicBitmap();
        if (!ConstData.DeviceType.isLocalDevice(LocalMediaInfo.getmDeviceType()))
        {
            artistFromRetriever = LocalMediaInfo.getmArtist();
            albumFromRetriever = LocalMediaInfo.getmAlbum();
        }

        mUIHandler.sendEmptyMessage(SetTextInfoMsg);
        RetrieveInfoManager.getInstance().addTask(mMediaBaseInfo, mThumbnailWidth, mThumbnailHeight,
                                                  mRetrieveCompleteListener);
    }
    
    /**
     * 以下是窗体的位置的调整和三秒隐藏
     */
    boolean isShow = false;
    private final int HideWindowMsg = 1001;
    private final int SetTextInfoMsg = 1002;
    private final int MSG_SHOW_PIC = 1003;
    private final int WaitTimes = 3*1000;
    private UIHandler mUIHandler = null;
    private WindowManager windowManager ;
    private WindowManager.LayoutParams windowManagerParams = new WindowManager.LayoutParams();

    // 隐藏该窗体
    public synchronized void hide()
    {
        if(isShow)
        {
            windowManager.removeView(this);
            isShow = false;
            if(mUIHandler.hasMessages(HideWindowMsg)) {
                mUIHandler.removeMessages(HideWindowMsg);
            }
        }           
    }
    
    // 显示该窗体
    public synchronized void show()
    {
        hide();
        if(isShow == false)
        {
            windowManager.addView(this, windowManagerParams);
            isShow = true;
            mUIHandler.sendEmptyMessageDelayed(HideWindowMsg, WaitTimes);   
        }   
    }
    
    /**
     * 该窗体的UI线程
     */
    private class UIHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what)
            {
            case HideWindowMsg:
                hide();
                break;

            case MSG_SHOW_PIC:
                if (msg.obj != null)
                {
                    Bitmap bitmap = (Bitmap)msg.obj;
                    if ((bitmap != null) && !bitmap.isRecycled())
                    {
                        bitmap = BitmapUtil.getRoundedCornerBitmap(bitmap, 20.0f);
                        mImageView.setImageBitmap(bitmap);
                    }
                }

                break;

            case SetTextInfoMsg:
                if (diaplayName == null)
                {
                    mTextViewName.setText(mContext.getString(R.string.unknown_title));
                }
                else if (mTextViewName != null)
                {
                    mTextViewName.setText(diaplayName);
                }

                if ((artistFromRetriever == null) && (signerTextView != null))
                {
                    signerTextView.setText(mContext.getString(R.string.unknown_artist));
                }
                else if (signerTextView != null)
                {
                    signerTextView.setText(artistFromRetriever);
                }

                if (albumFromRetriever == null)
                {
                    albumTextView.setText(mContext.getString(R.string.unknown_album));
                }
                else if (albumTextView != null)
                {
                    albumTextView.setText(albumFromRetriever);
                }

                break;

            default:
                break;
            }
        }
    }
    
    private void initWindowManeger(Context context){
        synchronized (this)
        {
            windowManager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        }
        windowManagerParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        windowManagerParams.format = PixelFormat.RGBA_8888; // 背景透明
        windowManagerParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        // 调整悬浮窗口至左下角，便于调整坐标
        windowManagerParams.gravity = Gravity.LEFT | Gravity.BOTTOM; 
        windowManagerParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        windowManagerParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
//        windowManagerParams.width = 850;
//        windowManagerParams.height = 250;
        windowManagerParams.x = 75;
        windowManagerParams.y = 30;
    }
       
    public Bitmap loadDefaultMusicBitmap()
    {
        mDefaultMusicBitmap =
            BitmapUtil.createBitmapFromResource(getResources(),
                R.drawable.album_default_icon,
                mThumbnailWidth,
                mThumbnailHeight);
        return mDefaultMusicBitmap;
    }

    public final Message obtainUiMessage(int what, int arg1, int arg2, Object obj)
    {
        
        if (mUIHandler != null)
        {
            return Message.obtain(mUIHandler, what, arg1, arg2, obj);
        }
        else
        {
            return null;
        }
    }
    
    public final void sendUiMessage(Message msg, int delayMillis)
    {
        // Log.d(TAG, "---------->sendUiMessage(),msg.what " + msg.what);
        if (msg != null)
        {
            if (mUIHandler != null)
            {
                mUIHandler.sendMessageDelayed(msg, delayMillis);
            }
        }
    }
    
    private void initBgAudioInfo()
    {
        artistFromRetriever = null;
        albumFromRetriever = null;
    }

    private RetrieveCompleteListener mRetrieveCompleteListener = new RetrieveCompleteListener()
    {
        @Override
        public void onComplete(LocalMediaInfo mediaInfo, SongInfo songInfo)
        {
            if (isCurrentSameUri(mediaInfo) && (songInfo != null))
            {
                artistFromRetriever = songInfo.getArtist();
                albumFromRetriever = songInfo.getAlbum();
                mUIHandler.sendEmptyMessage(SetTextInfoMsg);
            }
        }

        @Override
        public void onComplete(LocalMediaInfo mediaInfo, Bitmap bitmap)
        {
            if (isCurrentSameUri(mediaInfo))
            {
                if (bitmap != null)
                {
                    sendUiMessage(obtainUiMessage(MSG_SHOW_PIC, 0, 0, bitmap), 0);
                }
                else
                {
                    sendUiMessage(obtainUiMessage(MSG_SHOW_PIC, 0, 0, mDefaultMusicBitmap), 0);
                }
            }
        }
    };

    private boolean isCurrentSameUri(LocalMediaInfo mediaInfo)
    {
        if ((mediaInfo == null) || (null == mediaInfo.getUrl()))
        {
            return false;
        }

        if (null == mMediaBaseInfo)
        {
            return false;
        }

        Log.d(TAG, "cc msg isCurrentSameUri url = " + mediaInfo.getUrl() + " current focus url = "
              + mMediaBaseInfo.getUrl());
        if (mediaInfo.getUrl().equals(mMediaBaseInfo.getUrl()))
        {
            return true;
        }

        return false;
    }
}
