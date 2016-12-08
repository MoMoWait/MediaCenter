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

package com.rockchips.mediacenter.portable.orig;

import java.io.IOException;
import java.util.Map;
import java.io.File;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.net.Uri;
import android.util.Log;

import com.rockchips.mediacenter.basicutils.constant.Constant;

/**
 * Displays a video file. The VideoView class can load images from various sources (such as resources or content providers), takes care of computing
 * its measurement from the video so that it can be used in any layout manager, and provides various display options such as scaling and tinting.
 */
public class VideoViewNoViewBase
{
    private String TAG = "VideoViewNoView";

    // settable by the client
    private Uri mUri;

    private Map<String, String> mHeaders;

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
    private MediaPlayer mMediaPlayer = null;

    private OnCompletionListener mOnCompletionListener;

    private MediaPlayer.OnPreparedListener mOnPreparedListener;

    private int mCurrentBufferPercentage;

    private OnErrorListener mOnErrorListener;

    private OnInfoListener mOnInfoListener;

    private int mSeekWhenPrepared; // recording the seek position while preparing

    private boolean mCanPause;

    private boolean mCanSeekBack;

    private boolean mCanSeekForward;

    private Context mContext = null;

    public VideoViewNoViewBase(Context context)
    {
        mContext = context;
		Log.i(TAG, "VideoViewNoViewBase->mContext:" + mContext.getClass().getName());
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
    	Log.i(TAG, "setVideoURI->uri:" + uri);
        mUri = uri;
        mHeaders = headers;
		//Log.i(TAG, "");
        mSeekWhenPrepared = 0;
        openVideo();
    }

    public void stopPlayback()
    {
        if (mMediaPlayer != null)
        {            
            mMediaPlayer.stop();                                     
            mMediaPlayer.release();
            mMediaPlayer = null;
            mCurrentState = STATE_IDLE;
            mTargetState = STATE_IDLE;
        }
    }

    @SuppressLint("NewApi")
    private void openVideo()
    {
        if (mUri == null)
        {
            // not ready for playback just yet, will try again later
            return;
        }
        // Tell the music playback service to pause
        // TODO: these constants need to be published somewhere in the framework.
        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "pause");
        mContext.sendBroadcast(i);//, Constant.BROADCAST_PERMISSION_MOUNT_UNMOUNT_FILESYSTEMS);

        // we shouldn't clear the target state, because somebody might have
        // called start() previously
        release(false);
        try
        {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnPreparedListener(mPreparedListener);
            mMediaPlayer.setOnCompletionListener(mCompletionListener);
            mMediaPlayer.setOnErrorListener(mErrorListener);
            mMediaPlayer.setOnInfoListener(mOnInfoListener);
            mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
            mCurrentBufferPercentage = 0;
			//Log.i(TAG, "mURi");
			//Need fix the Uri
			//mUri =   Uri.parse("/mnt/usb_storage/USB_DISK3/udisk0/B7426FE4FF6B50BD.mp3");
			//mMediaPlayer.setDataS
			Log.i(TAG, "openVideo->Muri.getPath:" + mUri.getPath());
			File file = new File(mUri.getPath());
			if(file.exists()){
				mUri = Uri.fromFile(file);
			}
			
            mMediaPlayer.setDataSource(mContext, mUri, mHeaders);
			//mMediaPlayer.setDataSource(mContext, Uri.fromFile(new File("/mnt/usb_storage/USB_DISK3/udisk0/Music/�������-��ɭ�������������� ����.mp3")));
			//mMediaPlayer.setDataSource(mUri.getPath());
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mMediaPlayer.prepareAsync();
            // we don't set the target state here either, but preserve the
            // target state that was there before.
            mCurrentState = STATE_PREPARING;
        }
        catch (IOException ex)
        {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        }
        catch (IllegalArgumentException ex)
        {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        }
    }

    MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener()
    {
        public void onPrepared(MediaPlayer mp)
        {
            mCurrentState = STATE_PREPARED;

            // Get the capabilities of the player for this stream
            mCanPause = mCanSeekBack = mCanSeekForward = true;

            if (mOnPreparedListener != null)
            {
                mOnPreparedListener.onPrepared(mMediaPlayer);
            }

            int seekToPosition = mSeekWhenPrepared; // mSeekWhenPrepared may be changed after seekTo() call
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

    private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener()
    {
        public void onCompletion(MediaPlayer mp)
        {
            mCurrentState = STATE_PLAYBACK_COMPLETED;
            mTargetState = STATE_PLAYBACK_COMPLETED;
            if (mOnCompletionListener != null)
            {
                mOnCompletionListener.onCompletion(mMediaPlayer);
            }
        }
    };

    private MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener()
    {
        public boolean onError(MediaPlayer mp, int framework_err, int impl_err)
        {
            Log.d(TAG, "Error: " + framework_err + "," + impl_err);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;

            /* If an error handler has been supplied, use it and finish. */
            if (mOnErrorListener != null)
            {
                if (mOnErrorListener.onError(mMediaPlayer, framework_err, impl_err))
                {
                    return true;
                }
            }

            /*
             * Otherwise, pop up an error dialog so the user knows that something bad has happened. Only try and pop up the dialog if we're attached
             * to a window. When we're going away and no longer have a window, don't bother showing the user an error.
             */
            // if (getWindowToken() != null) {
            // Resources r = mContext.getResources();
            // int messageId;
            //
            // if (framework_err == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
            // messageId = com.android.internal.R.string.VideoView_error_text_invalid_progressive_playback;
            // } else {
            // messageId = com.android.internal.R.string.VideoView_error_text_unknown;
            // }
            //
            // new AlertDialog.Builder(mContext)
            // .setMessage(messageId)
            // .setPositiveButton(com.android.internal.R.string.VideoView_error_button,
            // new DialogInterface.OnClickListener() {
            // public void onClick(DialogInterface dialog, int whichButton) {
            // /* If we get here, there is no onError listener, so
            // * at least inform them that the video is over.
            // */
            // if (mOnCompletionListener != null) {
            // mOnCompletionListener.onCompletion(mMediaPlayer);
            // }
            // }
            // })
            // .setCancelable(false)
            // .show();
            // }
            return true;
        }
    };

    private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener()
    {
        public void onBufferingUpdate(MediaPlayer mp, int percent)
        {
            mCurrentBufferPercentage = percent;
        }
    };

    /**
     * Register a callback to be invoked when the media file is loaded and ready to go.
     * 
     * @param l The callback that will be run
     */
    public void setOnPreparedListener(MediaPlayer.OnPreparedListener l)
    {
        mOnPreparedListener = l;
    }

    /**
     * Register a callback to be invoked when the end of a media file has been reached during playback.
     * 
     * @param l The callback that will be run
     */
    public void setOnCompletionListener(OnCompletionListener l)
    {
        mOnCompletionListener = l;
    }

    /**
     * Register a callback to be invoked when an error occurs during playback or setup. If no listener is specified, or if the listener returned
     * false, VideoView will inform the user of any errors.
     * 
     * @param l The callback that will be run
     */
    public void setOnErrorListener(OnErrorListener l)
    {
        mOnErrorListener = l;
    }

    /**
     * Register a callback to be invoked when an informational event occurs during playback or setup.
     * 
     * @param l The callback that will be run
     */
    public void setOnInfoListener(OnInfoListener l)
    {
        mOnInfoListener = l;
    }

    /*
     * release the media player in any state
     */
    private void release(boolean cleartargetstate)
    {
        if (mMediaPlayer != null)
        {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
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
            mMediaPlayer.start();
            mCurrentState = STATE_PLAYING;
        }
        mTargetState = STATE_PLAYING;
    }

    public void pause()
    {
        if (isInPlaybackState())
        {
            if (null != mMediaPlayer && mMediaPlayer.isPlaying())
            {
                mMediaPlayer.pause();
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

    public int getDuration()
    {
        if (isInPlaybackState())
        {
            return mMediaPlayer.getDuration();
        }

        return -1;
    }

    public int getCurrentPosition()
    {
        if (isInPlaybackState())
        {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public void seekTo(int msec)
    {
        if (isInPlaybackState())
        {
            mMediaPlayer.seekTo(msec);
            mSeekWhenPrepared = 0;
        }
        else
        {
            mSeekWhenPrepared = msec;
        }
    }

    public boolean isPlaying()
    {
        return isInPlaybackState() && mMediaPlayer.isPlaying();
    }

    public int getBufferPercentage()
    {
        if (mMediaPlayer != null)
        {
            return mCurrentBufferPercentage;
        }
        return 0;
    }

    private boolean isInPlaybackState()
    {
        return (mMediaPlayer != null && mCurrentState != STATE_ERROR && mCurrentState != STATE_IDLE && mCurrentState != STATE_PREPARING);
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
