/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hisilicon.android.mediaplayer;

import java.io.IOException;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;

/**
 * Displays a video file. The VideoView class can load images from various
 * sources (such as resources or content providers), takes care of computing its
 * measurement from the video so that it can be used in any layout manager, and
 * provides various display options such as scaling and tinting.
 */
public class HiVideoView extends SurfaceView implements MediaPlayerControl
{
    private String TAG = "HiVideoView";

    private Context mContext;

    // settable by the client
    private Uri mUri;

    private Map<String, String> mHeaders;

    private int mDuration;

    // all possible internal states
    private static final int STATE_ERROR = -1;

    private static final int STATE_IDLE = 0;

    private static final int STATE_PREPARING = 1;

    private static final int STATE_PREPARED = 2;

    private static final int STATE_PLAYING = 3;

    private static final int STATE_PAUSED = 4;

    private static final int STATE_PLAYBACK_COMPLETED = 5;

    // mCurrentState is a VideoView object's current state.
    // mTargetState is the state that a method caller intends to reach.
    // For instance, regardless the VideoView object's current state,
    // calling pause() intends to bring the object to a target state
    // of STATE_PAUSED.
    private int mCurrentState = STATE_IDLE;

    private int mTargetState = STATE_IDLE;

    // All the stuff we need for playing and showing a video
    private SurfaceHolder mSurfaceHolder = null;

    private HiMediaPlayer mHiMediaPlayer = null;

    private int mVideoWidth;

    private int mVideoHeight;

    private int mSurfaceWidth;

    private int mSurfaceHeight;

    private MediaController mMediaController;

    private HiMediaPlayer.OnCompletionListener mOnCompletionListener;

    private HiMediaPlayer.OnPreparedListener mOnPreparedListener;

    private int mCurrentBufferPercentage;

    private HiMediaPlayer.OnErrorListener mOnErrorListener;

    private int mSeekWhenPrepared; // recording the seek position while
                                   // preparing

    private boolean mCanPause;

    private boolean mCanSeekBack;

    private boolean mCanSeekForward;

    public HiVideoView(Context context)
    {
        super(context);
        mContext = context;
        initVideoView();
    }

    public HiVideoView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
        mContext = context;
        initVideoView();
    }

    public HiVideoView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        mContext = context;
        initVideoView();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        // Log.i("@@@@", "onMeasure");
        int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
        int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
        if (mVideoWidth > 0 && mVideoHeight > 0)
        {
            if (mVideoWidth * height > width * mVideoHeight)
            {
                // Log.i("@@@", "image too tall, correcting");
                height = width * mVideoHeight / mVideoWidth;
            }
            else if (mVideoWidth * height < width * mVideoHeight)
            {
                // Log.i("@@@", "image too wide, correcting");
                width = height * mVideoWidth / mVideoHeight;
            }
            else
            {
                // Log.i("@@@", "aspect ratio is correct: " +
                // width+"/"+height+"="+
                // mVideoWidth+"/"+mVideoHeight);
            }
        }
        // Log.i("@@@@@@@@@@", "setting size: " + width + 'x' + height);
        setMeasuredDimension(width, height);
    }

    public int resolveAdjustedSize(int desiredSize, int measureSpec)
    {
        int result = desiredSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        switch (specMode)
        {
            case MeasureSpec.UNSPECIFIED:
                /*
                 * Parent says we can be as big as we want. Just don't be larger
                 * than max size imposed on ourselves.
                 */
                result = desiredSize;
                break;

            case MeasureSpec.AT_MOST:
                /*
                 * Parent says we can be as big as we want, up to specSize.
                 * Don't be larger than specSize, and don't be larger than the
                 * max size imposed on ourselves.
                 */
                result = Math.min(desiredSize, specSize);
                break;

            case MeasureSpec.EXACTLY:
                // No choice. Do what we are told.
                result = specSize;
                break;
        }
        return result;
    }

    private void initVideoView()
    {
        mVideoWidth = 0;
        mVideoHeight = 0;
        getHolder().addCallback(mSHCallback);
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        mCurrentState = STATE_IDLE;
        mTargetState = STATE_IDLE;
    }

    public void setVideoPath(String path)
    {
        setVideoURI(Uri.parse(path));
    }

    public void setVideoURI(Uri uri)
    {
        setVideoURI(uri, null);
    }

    /**
     * @hide
     */
    public void setVideoURI(Uri uri, Map<String, String> headers)
    {
        mUri = uri;
        mHeaders = headers;
        mSeekWhenPrepared = 0;
        openVideo();
        requestLayout();
        invalidate();
    }

    public void stopPlayback()
    {
        if (mHiMediaPlayer != null)
        {
            mHiMediaPlayer.stop();
            mHiMediaPlayer.release();
            mHiMediaPlayer = null;
            mCurrentState = STATE_IDLE;
            mTargetState = STATE_IDLE;
        }
    }

    private void openVideo()
    {
        if (mUri == null || mSurfaceHolder == null)
        {
            // not ready for playback just yet, will try again later
            return;
        }
        // Tell the music playback service to pause
        // TODO: these constants need to be published somewhere in the
        // framework.
        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "pause");
        mContext.sendBroadcast(i);

        // we shouldn't clear the target state, because somebody might have
        // called start() previously
        release(false);
        try
        {
            mHiMediaPlayer = new HiMediaPlayer();
            mHiMediaPlayer.setOnPreparedListener(mPreparedListener);
            mHiMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
            mDuration = -1;
            mHiMediaPlayer.setOnCompletionListener(mCompletionListener);
            mHiMediaPlayer.setOnErrorListener(mErrorListener);
            mHiMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
            mCurrentBufferPercentage = 0;
            mHiMediaPlayer.setDataSource(mContext, mUri, mHeaders);
            // mHiMediaPlayer.setDisplay(mSurfaceHolder);
            // mHiMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            // mHiMediaPlayer.setScreenOnWhilePlaying(true);
            mHiMediaPlayer.prepareAsync();
            // we don't set the target state here either, but preserve the
            // target state that was there before.
            mCurrentState = STATE_PREPARING;
            attachMediaController();
        }
        catch (IOException ex)
        {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mErrorListener.onError(mHiMediaPlayer, HiMediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        }
        catch (IllegalArgumentException ex)
        {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mErrorListener.onError(mHiMediaPlayer, HiMediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        }
    }

    public void setMediaController(MediaController controller)
    {
        if (mMediaController != null)
        {
            mMediaController.hide();
        }
        mMediaController = controller;
        attachMediaController();
    }

    private void attachMediaController()
    {
        if (mHiMediaPlayer != null && mMediaController != null)
        {
            mMediaController.setMediaPlayer(this);
            View anchorView = this.getParent() instanceof View ? (View) this.getParent() : this;
            mMediaController.setAnchorView(anchorView);
            mMediaController.setEnabled(isInPlaybackState());
        }
    }

    HiMediaPlayer.OnVideoSizeChangedListener mSizeChangedListener = new HiMediaPlayer.OnVideoSizeChangedListener()
    {
        public void onVideoSizeChanged(HiMediaPlayer mp, int width, int height)
        {
            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();
            if (mVideoWidth != 0 && mVideoHeight != 0)
            {
                getHolder().setFixedSize(mVideoWidth, mVideoHeight);
            }
        }
    };

    HiMediaPlayer.OnPreparedListener mPreparedListener = new HiMediaPlayer.OnPreparedListener()
    {
        public void onPrepared(HiMediaPlayer mp)
        {
            mCurrentState = STATE_PREPARED;

            // Get the capabilities of the player for this stream
            Metadata data = mp.getMetadata(HiMediaPlayer.METADATA_ALL, HiMediaPlayer.BYPASS_METADATA_FILTER);

            if (data != null)
            {
                mCanPause = !data.has(Metadata.PAUSE_AVAILABLE) || data.getBoolean(Metadata.PAUSE_AVAILABLE);
                mCanSeekBack = !data.has(Metadata.SEEK_BACKWARD_AVAILABLE) || data.getBoolean(Metadata.SEEK_BACKWARD_AVAILABLE);
                mCanSeekForward = !data.has(Metadata.SEEK_FORWARD_AVAILABLE) || data.getBoolean(Metadata.SEEK_FORWARD_AVAILABLE);
            }
            else
            {
                mCanPause = mCanSeekBack = mCanSeekForward = true;
            }

            if (mOnPreparedListener != null)
            {
                mOnPreparedListener.onPrepared(mHiMediaPlayer);
            }
            if (mMediaController != null)
            {
                mMediaController.setEnabled(true);
            }
            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();

            int seekToPosition = mSeekWhenPrepared; // mSeekWhenPrepared may be
                                                    // changed after seekTo()
                                                    // call
            if (seekToPosition != 0)
            {
                seekTo(seekToPosition);
            }
            if (mVideoWidth != 0 && mVideoHeight != 0)
            {
                // Log.i("@@@@", "video size: " + mVideoWidth +"/"+
                // mVideoHeight);
                getHolder().setFixedSize(mVideoWidth, mVideoHeight);
                if (mSurfaceWidth == mVideoWidth && mSurfaceHeight == mVideoHeight)
                {
                    // We didn't actually change the size (it was already at the
                    // size
                    // we need), so we won't get a "surface changed" callback,
                    // so
                    // start the video here instead of in the callback.
                    if (mTargetState == STATE_PLAYING)
                    {
                        start();
                        if (mMediaController != null)
                        {
                            mMediaController.show();
                        }
                    }
                    else if (!isPlaying() && (seekToPosition != 0 || getCurrentPosition() > 0))
                    {
                        if (mMediaController != null)
                        {
                            // Show the media controls when we're paused into a
                            // video and make 'em stick.
                            mMediaController.show(0);
                        }
                    }
                }
            }
            else
            {
                // We don't know the video size yet, but should start anyway.
                // The video size might be reported to us later.
                if (mTargetState == STATE_PLAYING)
                {
                    start();
                }
            }
        }
    };

    private HiMediaPlayer.OnCompletionListener mCompletionListener = new HiMediaPlayer.OnCompletionListener()
    {
        public void onCompletion(HiMediaPlayer mp)
        {
            mCurrentState = STATE_PLAYBACK_COMPLETED;
            mTargetState = STATE_PLAYBACK_COMPLETED;
            if (mMediaController != null)
            {
                mMediaController.hide();
            }
            if (mOnCompletionListener != null)
            {
                mOnCompletionListener.onCompletion(mHiMediaPlayer);
            }
        }
    };

    private HiMediaPlayer.OnErrorListener mErrorListener = new HiMediaPlayer.OnErrorListener()
    {
        public boolean onError(HiMediaPlayer mp, int framework_err, int impl_err)
        {
            Log.d(TAG, "Error: " + framework_err + "," + impl_err);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            if (mMediaController != null)
            {
                mMediaController.hide();
            }

            /* If an error handler has been supplied, use it and finish. */
            if (mOnErrorListener != null)
            {
                if (mOnErrorListener.onError(mHiMediaPlayer, framework_err, impl_err))
                {
                    return true;
                }
            }

            /*
             * Otherwise, pop up an error dialog so the user knows that
             * something bad has happened. Only try and pop up the dialog if
             * we're attached to a window. When we're going away and no longer
             * have a window, don't bother showing the user an error.
             */
            /*
             * if (getWindowToken() != null) { Resources r =
             * mContext.getResources(); int messageId = -1;
             * 
             * if (framework_err ==
             * HiMediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
             * messageId = com.android.internal.R.string.
             * VideoView_error_text_invalid_progressive_playback; } else {
             * messageId =
             * com.android.internal.R.string.VideoView_error_text_unknown; }
             * 
             * new AlertDialog.Builder(mContext)
             * .setTitle(com.android.internal.R.string.VideoView_error_title)
             * .setMessage(messageId)
             * .setPositiveButton(com.android.internal.R.string
             * .VideoView_error_button, new DialogInterface.OnClickListener() {
             * public void onClick(DialogInterface dialog, int whichButton) {
             * 
             * if (mOnCompletionListener != null) {
             * mOnCompletionListener.onCompletion(mHiMediaPlayer); } } })
             * .setCancelable(false) .show(); } return true; }
             */
            return true;
        }
    };

    private HiMediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener = new HiMediaPlayer.OnBufferingUpdateListener()
    {
        public void onBufferingUpdate(HiMediaPlayer mp, int percent)
        {
            mCurrentBufferPercentage = percent;
        }
    };

    /**
     * Register a callback to be invoked when the media file is loaded and ready
     * to go.
     * 
     * @param l The callback that will be run
     */
    public void setOnPreparedListener(HiMediaPlayer.OnPreparedListener l)
    {
        mOnPreparedListener = l;
    }

    /**
     * Register a callback to be invoked when the end of a media file has been
     * reached during playback.
     * 
     * @param l The callback that will be run
     */
    public void setOnCompletionListener(HiMediaPlayer.OnCompletionListener l)
    {
        mOnCompletionListener = l;
    }

    /**
     * Register a callback to be invoked when an error occurs during playback or
     * setup. If no listener is specified, or if the listener returned false,
     * VideoView will inform the user of any errors.
     * 
     * @param l The callback that will be run
     */
    public void setOnErrorListener(HiMediaPlayer.OnErrorListener l)
    {
        mOnErrorListener = l;
    }

    SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback()
    {
        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h)
        {
            int left = 800;//holder.getSurface().getVideoRect().left;
            int right = 800;//holder.getSurface().getVideoRect().right;
            int top = 800;//holder.getSurface().getVideoRect().top;
            int bottom = 800;//holder.getSurface().getVideoRect().bottom;
            mSurfaceWidth = w;
            mSurfaceHeight = h;
            boolean isValidState = (mTargetState == STATE_PLAYING);
            boolean hasValidSize = (mVideoWidth == w && mVideoHeight == h);
            if (mHiMediaPlayer != null && isValidState && hasValidSize)
            {
                if (mSeekWhenPrepared != 0)
                {
                    seekTo(mSeekWhenPrepared);
                }
                start();
            }
            if (mHiMediaPlayer != null)
            {
                Log.i(TAG, "left:" + left + " right:" + right + " top:" + top + " bottom:" + bottom);
                mHiMediaPlayer.setVideoRange(left, top, right - left, bottom - top);
            }
        }

        public void surfaceCreated(SurfaceHolder holder)
        {
            mSurfaceHolder = holder;
            openVideo();
        }

        public void surfaceDestroyed(SurfaceHolder holder)
        {
            // after we return from this we can't use the surface any more
            mSurfaceHolder = null;
            if (mMediaController != null)
                mMediaController.hide();
            release(true);
        }
    };

    /*
     * release the media player in any state
     */
    private void release(boolean cleartargetstate)
    {
        if (mHiMediaPlayer != null)
        {
            mHiMediaPlayer.reset();
            mHiMediaPlayer.release();
            mHiMediaPlayer = null;
            mCurrentState = STATE_IDLE;
            if (cleartargetstate)
            {
                mTargetState = STATE_IDLE;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev)
    {
        if (isInPlaybackState() && mMediaController != null)
        {
            toggleMediaControlsVisiblity();
        }
        return false;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev)
    {
        if (isInPlaybackState() && mMediaController != null)
        {
            toggleMediaControlsVisiblity();
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK && keyCode != KeyEvent.KEYCODE_VOLUME_UP
                && keyCode != KeyEvent.KEYCODE_VOLUME_DOWN && keyCode != KeyEvent.KEYCODE_VOLUME_MUTE && keyCode != KeyEvent.KEYCODE_MENU
                && keyCode != KeyEvent.KEYCODE_CALL && keyCode != KeyEvent.KEYCODE_ENDCALL;
        if (isInPlaybackState() && isKeyCodeSupported && mMediaController != null)
        {
            if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
            {
                if (mHiMediaPlayer.isPlaying())
                {
                    pause();
                    mMediaController.show();
                }
                else
                {
                    start();
                    mMediaController.hide();
                }
                return true;
            }
            else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY)
            {
                if (!mHiMediaPlayer.isPlaying())
                {
                    start();
                    mMediaController.hide();
                }
                return true;
            }
            else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE)
            {
                if (mHiMediaPlayer.isPlaying())
                {
                    pause();
                    mMediaController.show();
                }
                return true;
            }
            else
            {
                toggleMediaControlsVisiblity();
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private void toggleMediaControlsVisiblity()
    {
        if (mMediaController.isShowing())
        {
            mMediaController.hide();
        }
        else
        {
            mMediaController.show();
        }
    }

    public void start()
    {
        if (isInPlaybackState())
        {
            mHiMediaPlayer.start();
            mCurrentState = STATE_PLAYING;
        }
        mTargetState = STATE_PLAYING;
    }

    public void pause()
    {
        if (isInPlaybackState())
        {
            if (mHiMediaPlayer.isPlaying())
            {
                mHiMediaPlayer.pause();
                mCurrentState = STATE_PAUSED;
            }
        }
        mTargetState = STATE_PAUSED;
    }

    public void suspend()
    {
        release(false);
    }

    public void resume()
    {
        openVideo();
    }

    // cache duration as mDuration for faster access
    public int getDuration()
    {
        if (isInPlaybackState())
        {
            if (mDuration > 0)
            {
                return mDuration;
            }
            mDuration = mHiMediaPlayer.getDuration();
            return mDuration;
        }
        mDuration = -1;
        return mDuration;
    }

    public int getCurrentPosition()
    {
        if (isInPlaybackState())
        {
            return mHiMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public void seekTo(int msec)
    {
        if (isInPlaybackState())
        {
            mHiMediaPlayer.seekTo(msec);
            mSeekWhenPrepared = 0;
        }
        else
        {
            mSeekWhenPrepared = msec;
        }
    }

    public boolean isPlaying()
    {
        return isInPlaybackState() && mHiMediaPlayer.isPlaying();
    }

    public int getBufferPercentage()
    {
        if (mHiMediaPlayer != null)
        {
            return mCurrentBufferPercentage;
        }
        return 0;
    }

    private boolean isInPlaybackState()
    {
        return (mHiMediaPlayer != null && mCurrentState != STATE_ERROR && mCurrentState != STATE_IDLE && mCurrentState != STATE_PREPARING);
    }

    public boolean canPause()
    {
        return mCanPause;
    }

    public boolean canSeekBackward()
    {
        return mCanSeekBack;
    }

    public boolean canSeekForward()
    {
        return mCanSeekForward;
    }

    public int getAudioSessionId()
    {
        return 0;
    }
}
