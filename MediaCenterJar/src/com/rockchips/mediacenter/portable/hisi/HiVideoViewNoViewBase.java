package com.rockchips.mediacenter.portable.hisi;

import java.io.IOException;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.hisilicon.android.mediaplayer.HiMediaPlayer;
import com.hisilicon.android.mediaplayer.Metadata;
import com.rockchips.mediacenter.basicutils.constant.Constant;

/**
 * Displays a video file. The VideoView class can load images from various sources (such as resources or content providers), takes care of computing
 * its measurement from the video so that it can be used in any layout manager, and provides various display options such as scaling and tinting.
 */
public class HiVideoViewNoViewBase
{
    private String TAG = "HiVideoViewNoView";

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
    private HiMediaPlayer mHiMediaPlayer = null;

    private HiMediaPlayer.OnCompletionListener mOnCompletionListener;

    private HiMediaPlayer.OnPreparedListener mOnPreparedListener;

    private int mCurrentBufferPercentage;

    private HiMediaPlayer.OnErrorListener mOnErrorListener;

    private HiMediaPlayer.OnInfoListener mOnInfoListener;

    private int mSeekWhenPrepared; // recording the seek position while
                                   // preparing

    private boolean mCanPause;

    private boolean mCanSeekBack;

    private boolean mCanSeekForward;

    private Context mContext = null;

    public HiVideoViewNoViewBase(Context context)
    {
        mContext = context;
        initVideoView();
    }

    private void initVideoView()
    {
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
        if (mUri == null)
        {
            // not ready for playback just yet, will try again later
            return;
        }
        // Tell the music playback service to pause
        // TODO: these constants need to be published somewhere in the
        // framework.
        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "pause");
        mContext.sendBroadcast(i);//, Constant.BROADCAST_PERMISSION_MOUNT_UNMOUNT_FILESYSTEMS);

        // we shouldn't clear the target state, because somebody might have
        // called start() previously
        release(false);
        try
        {
            mHiMediaPlayer = new HiMediaPlayer();
            mHiMediaPlayer.setOnPreparedListener(mPreparedListener);
            mDuration = -1;
            mHiMediaPlayer.setOnCompletionListener(mCompletionListener);
            mHiMediaPlayer.setOnErrorListener(mErrorListener);
            mHiMediaPlayer.setOnInfoListener(mInfoListener);
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

            int seekToPosition = mSeekWhenPrepared; // mSeekWhenPrepared may be
                                                    // changed after seekTo()
                                                    // call
            if (seekToPosition != 0)
            {
                seekTo(seekToPosition);
            }
            // We don't know the video size yet, but should start anyway.
            // The video size might be reported to us later.
            if (mTargetState == STATE_PLAYING)
            {
                start();
            }
        }
    };

    private HiMediaPlayer.OnCompletionListener mCompletionListener = new HiMediaPlayer.OnCompletionListener()
    {
        public void onCompletion(HiMediaPlayer mp)
        {
            mCurrentState = STATE_PLAYBACK_COMPLETED;
            mTargetState = STATE_PLAYBACK_COMPLETED;

        }
    };

    private HiMediaPlayer.OnErrorListener mErrorListener = new HiMediaPlayer.OnErrorListener()
    {
        public boolean onError(HiMediaPlayer mp, int framework_err, int impl_err)
        {
            Log.d(TAG, "Error: " + framework_err + "," + impl_err);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;

            /* If an error handler has been supplied, use it and finish. */
            if (mOnErrorListener != null)
            {
                if (mOnErrorListener.onError(mHiMediaPlayer, framework_err, impl_err))
                {
                    return true;
                }
            }

            /*
             * Otherwise, pop up an error dialog so the user knows that something bad has happened. Only try and pop up the dialog if we're attached
             * to a window. When we're going away and no longer have a window, don't bother showing the user an error.
             */
            // if (mContext.getWindowToken() != null) {
            // Resources r = mContext.getResources();
            // int messageId;
            //
            // if (framework_err ==
            // HiMediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
            // messageId =
            // com.android.internal.R.string.VideoView_error_text_invalid_progressive_playback;
            // } else {
            // messageId =
            // com.android.internal.R.string.VideoView_error_text_unknown;
            // }
            //
            // new AlertDialog.Builder(mContext)
            // .setTitle(com.android.internal.R.string.VideoView_error_title)
            // .setMessage(messageId)
            // .setPositiveButton(com.android.internal.R.string.VideoView_error_button,
            // new DialogInterface.OnClickListener() {
            // public void onClick(DialogInterface dialog, int whichButton) {
            // /* If we get here, there is no onError listener, so
            // * at least inform them that the video is over.
            // */
            // if (mOnCompletionListener != null) {
            // mOnCompletionListener.onCompletion(mHiMediaPlayer);
            // }
            // }
            // })
            // .setCancelable(false)
            // .show();
            // }
            return true;
        }
    };

    // add by wanghuanlai
    private HiMediaPlayer.OnInfoListener mInfoListener = new HiMediaPlayer.OnInfoListener()
    {
        public boolean onInfo(HiMediaPlayer mp, int framework_info, int impl_info)
        {
            Log.d(TAG, "onInfo: " + framework_info + "," + impl_info);

            if (mOnInfoListener != null)
            {
                if (mOnInfoListener.onInfo(mHiMediaPlayer, framework_info, impl_info))
                {
                    return true;
                }
            }
            return false;
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
     * Register a callback to be invoked when the media file is loaded and ready to go.
     * 
     * @param l The callback that will be run
     */
    public void setOnPreparedListener(HiMediaPlayer.OnPreparedListener l)
    {
        mOnPreparedListener = l;
    }

    /**
     * Register a callback to be invoked when the end of a media file has been reached during playback.
     * 
     * @param l The callback that will be run
     */
    public void setOnCompletionListener(HiMediaPlayer.OnCompletionListener l)
    {
        mOnCompletionListener = l;
    }

    /**
     * Register a callback to be invoked when an error occurs during playback or setup. If no listener is specified, or if the listener returned
     * false, VideoView will inform the user of any errors.
     * 
     * @param l The callback that will be run
     */
    public void setOnErrorListener(HiMediaPlayer.OnErrorListener l)
    {
        mOnErrorListener = l;
    }

    /**
     * Info Listener,add by wanghuanlai
     * @param l
     */
    public void setOnInfoListener(HiMediaPlayer.OnInfoListener l)
    {
        mOnInfoListener = l;
    }

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
}
