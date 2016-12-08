package com.hisilicon.android.mediaplayer;

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



import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Set;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.PowerManager;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

/**
 * HiMediaPlayer interface<br>
 *
 * HiMediaPlayer class can be used to control playback
 * of audio/video files and streams.<br>
 * An example on how to use
 * this class can be found in com.hisilicon.android.videoplayer.activity.HisiVideoView.
 *
 * <p>Topics covered here are:
 * <ol>
 * <li><a href="#Attention">Attention</a>
 * <li><a href="#How to play a file">How to play a file</a>
 * <li><a href="#StateDiagram">State Diagram</a>
 * </ol>
 *
 * <a name="Attention"></a>
 * <h3>Attention</h3>
 * <ul>
 * <li><b>Deprecated</b>:
 * indicate the section is useless</li>
 * <li>
 * <b>Currently not implemented:</b>indicate the section has not been implemented now,maybe it will be implemented in future.</li>
 * </ul>
 *
 * <a name="How to play a file"></a>
 * <h3>How to play a file</h3>
 *
 * <p>You should obey steps described in a diagram below if you want to use HiMediaPlayer interface
 * to play a media file or stream</p>
 *
 * <p><img src="../../../../../img/playstep.gif"
 *         alt="How to play a file"
 *         border="0" /></p>
 * <p>
 * <ul>
 * <li>
 * 1.First you should new HiMediaPlayer class object which include all operations playing a media file or stream.</li>
 * <li>
 * 2.You should register some listener by calling {@link #setOnCompletionListener(OnCompletionListener)},
 * {@link #setOnPreparedListener(OnPreparedListener)},{@link #setOnSeekCompleteListener(OnSeekCompleteListener)}
 * and so on.the listener is not obligatory.the doc will describe every listener detailedly below.</li>
 * <li>
 * 3.You should set the file or stream path also called url to HiMediaPlayer by {@link #setDataSource(FileDescriptor)}.</li>
 * <li>
 * 4.You should set subtitle display surface by {@link #setDisplay(SurfaceHolder)}.if you do not set subtitle surface,the subtitle can not display if the
 * file or stream include subtitle track.</li>
 * <li>
 * 5.You should set video display area by {@link #setVideoRange(int,int,int,int)}.</li>
 * <li>
 * 6.Then the media file or stream must be analysed by calling {@link #prepare()} or {@link #prepareAsync()},after prepared,the media info like track info of audio video or subtitle
 * can be get.</li>
 * <li>
 * 7.You can call regular control operation like {@link #start()}, or {@link #seekTo(int)} and so on.</li>
 * <li>
 * 8.Some listener message will report,if you register the listener.</li>
 * <li>
 * 9.end the playing,you can use {@link #release()}.</li>
 * </p>
 *
 * <a name="StateDiagram"></a>
 * <h3>State Diagram</h3>
 *
 * <p>Playback control of audio/video files and streams is managed as a state
 * machine. The following diagram shows the life cycle and the states of a
 * HiMediaPlayer object driven by the supported playback control operations.
 * The circle represent the states a HiMediaPlayer object may reside
 * in. The arcs and lines have arrow represent the playback control operations that drive the object
 * state transition. </p>
 *
 * <p><img src="../../../../../img/StateDiagram.gif"
 *         alt="MediaPlayer State diagram"
 *         border="0" /></p>
 *
 * <p>From this state diagram, one can see that a HiMediaPlayer object has the
 *    following states:</p>
 *
 * <ul>
 *     <li>When a HiMediaPlayer object is just created using <code>new</code> or
 *         after {@link #reset()} is called, it is in the <em>Idle</em> state; and after
 *         {@link #release()} is called, it is in the <em>End</em> state. Between these
 *         two states is the life cycle of the HiMediaPlayer object.
 *         <ul>
 *         <li>It is a programming error to invoke methods such
 *         as {@link #getCurrentPosition()},
 *         {@link #getDuration()}, {@link #getVideoHeight()},
 *         {@link #getVideoWidth()},
 *         {@link #setLooping(boolean)},
 *         {@link #setVolume(float, float)}, {@link #pause()}, {@link #start()},
 *         {@link #stop()}, {@link #seekTo(int)}, {@link #prepare()} or
 *         {@link #prepareAsync()} in the <em>Idle</em> state. When
 *          error occur,some invoke throw exception(such as prepare),some invoke
 *          report error info from error callback from
 *          {@link #setOnErrorListener(OnErrorListener)}(such as getDuration)</li>
 *     <li>It is also recommended that once
 *         a HiMediaPlayer object is no longer being used, call {@link #release()} immediately
 *         so that resources used by the internal player engine associated with the
 *         HiMediaPlayer object can be released immediately.Once the HiMediaPlayer
 *         object is in the <em>End</em> state, it can no longer be used and
 *         there is no way to bring it back to any other state. </li>
 *         </ul>
 *         </li>
 *     <li>In general, some playback control operation may fail due to various
 *         reasons, such as unsupported file and the like.
 *         Thus, error reporting and recovery is an important concern under
 *         these circumstances. Sometimes, due to programming errors, invoking a playback
 *         control operation in an invalid state may also occur. Under all these
 *         error conditions, the internal player engine invokes a user supplied
 *         OnErrorListener.onError() method if an OnErrorListener has been
 *         registered beforehand via
 *         {@link #setOnErrorListener(OnErrorListener)}.
 *         <ul>
 *         <li>It is important to note that once an error occurs, the
 *         HiMediaPlayer object enters the <em>Error</em> state (except INVALID_OPERATION(-38)),
 *         even if an error listener has not been registered by the application.
 *         Because the OnErrorListener callback maybe cause error state(can not operation again),
 *         so many no fatal error info is reported by OnInfoListener)</li>
 *         <li>In order to reuse a HiMediaPlayer object that is in the <em>
 *         Error</em> state and recover from the error,
 *         {@link #reset()} can be called to restore the object to its <em>Idle</em>
 *         state.</li>
 *         <li>It is good programming practice to have your application
 *         register a OnErrorListener to look out for error notifications from
 *         the internal player engine.</li>
 *         <li>IllegalStateException is
 *         thrown to prevent programming errors such as calling {@link #prepare()},
 *         {@link #prepareAsync()}, or one of the overloaded <code>setDataSource
 *         </code> methods in an invalid state. </li>
 *         </ul>
 *         </li>
 *     <li>Calling
 *         {@link #setDataSource(FileDescriptor)}, or
 *         {@link #setDataSource(String)}, or
 *         {@link #setDataSource(Context, Uri)}, or
 *         {@link #setDataSource(FileDescriptor, long, long)} transfers a
 *         HiMediaPlayer object in the <em>Idle</em> state to the
 *         <em>Initialized</em> state.
 *         <ul>
 *         <li>An IllegalStateException is or IOException thrown if
 *         setDataSource() is called in any other invalid state.</li>
 *         <li>It is good programming
 *         practice to always look out for <code>IllegalArgumentException</code>
 *         and <code>IOException</code> that may be thrown from the overloaded
 *         <code>setDataSource</code> methods.</li>
 *         </ul>
 *         </li>
 *     <li>A HiMediaPlayer object must first enter the <em>Prepared</em> state
 *         before playback can be started.
 *         <ul>
 *         <li>There are two ways (synchronous vs.
 *         asynchronous) that the <em>Prepared</em> state can be reached:
 *         either a call to {@link #prepare()} (synchronous) which
 *         transfers the object to the <em>Prepared</em> state once the method call
 *         returns, or a call to {@link #prepareAsync()} (asynchronous) which
 *         first transfers the object to the <em>Preparing</em> state after the
 *         call returns (which occurs almost right way) while the internal
 *         player engine continues working on the rest of preparation work
 *         until the preparation work completes. When the preparation completes or when {@link #prepare()} call returns,
 *         the internal player engine then calls a user supplied callback method,
 *         onPrepared() of the OnPreparedListener interface, if an
 *         OnPreparedListener is registered beforehand via {@link
 *         #setOnPreparedListener(OnPreparedListener)}.</li>
 *         <li>It is important to note that
 *         the <em>Preparing</em> state is a transient state, and the behavior
 *         of calling any method with side effect while a HiMediaPlayer object is
 *         in the <em>Preparing</em> state is undefined.</li>
 *         <li>An IllegalStateException is
 *         thrown if {@link #prepare()} or {@link #prepareAsync()} is called in
 *         any other state.</li>
 *         <li>While in the <em>Prepared</em> state, properties
 *         such as audio/sound volume, looping can be
 *         adjusted by invoking the corresponding set methods.</li>
 *         </ul>
 *         </li>
 *     <li>To start the playback, {@link #start()} must be called. After
 *         {@link #start()} returns successfully, the HiMediaPlayer object is in the
 *         <em>Started</em> state. {@link #isPlaying()} can be called to test
 *         whether the HiMediaPlayer object is in the <em>Started</em> state.
 *         <ul>
 *         <li>While in the <em>Started</em> state, the internal player engine calls
 *         a user supplied OnBufferingUpdateListener.onBufferingUpdate() callback
 *         method if a OnBufferingUpdateListener has been registered beforehand
 *         via {@link #setOnBufferingUpdateListener(OnBufferingUpdateListener)}.
 *         This callback allows applications to keep track of the buffering status
 *         while streaming audio/video.</li>
 *         <li>Calling {@link #start()} has not effect
 *         on a HiMediaPlayer object that is already in the <em>Started</em> state.</li>
 *         </ul>
 *         </li>
 *     <li>Playback can be paused and stopped. Playback can be paused via {@link #pause()}.
 *         When the call to {@link #pause()} returns, the HiMediaPlayer object enters the
 *         <em>Paused</em> state.
 *         <ul>
 *         <li>Calling {@link #start()} to resume playback for a paused
 *         HiMediaPlayer object, and the resumed playback
 *         position is the same as where it was paused. When the call to
 *         {@link #start()} returns, the paused HiMediaPlayer object goes back to
 *         the <em>Started</em> state.</li>
 *         <li>Calling {@link #pause()} has no effect on
 *         a MediaPlayer object that is already in the <em>Paused</em> state.</li>
 *         </ul>
 *         </li>
 *     <li>Calling  {@link #stop()} stops playback and causes a
 *         HiMediaPlayer in the <em>Started</em>, <em>Paused</em>, <em>Prepared
 *         </em> or <em>PlaybackCompleted</em> state to enter the
 *         <em>Stopped</em> state.
 *         <ul>
 *         <li>Calling {@link #stop()} has no effect on a MediaPlayer
 *         object that is already in the <em>Stopped</em> state.</li>
 *         </ul>
 *         </li>
 *     <li>The playback position can be adjusted with a call to
 *         {@link #seekTo(int)}.
 *         <ul>
 *         <li>Although the asynchronuous {@link #seekTo(int)}
 *         call returns right way, the actual seek operation may take a while to
 *         finish. When the actual seek operation completes, the internal player engine calls a user
 *         supplied OnSeekComplete.onSeekComplete() if an OnSeekCompleteListener
 *         has been registered beforehand via
 *         {@link #setOnSeekCompleteListener(OnSeekCompleteListener)}.</li>
 *         <li>Please
 *         note that {@link #seekTo(int)} can also be called in the other states,
 *         such as <em>Prepared</em>, <em>Paused</em> and <em>PlaybackCompleted
 *         </em> state.</li>
 *         <li>Furthermore, the actual current playback position
 *         can be retrieved with a call to {@link #getCurrentPosition()}, which
 *         is helpful for applications such as a Music player that need to keep
 *         track of the playback progress.</li>
 *         </ul>
 *         </li>
 *     <li>When the playback reaches the end of stream, the playback completes.
 *         <ul>
 *         <li>If the looping mode was being set to <var>true</var>with
 *         {@link #setLooping(boolean)}, the HiMediaPlayer object shall remain in
 *         the <em>Started</em> state.</li>
 *         <li>If the looping mode was set to <var>false
 *         </var>, the player engine calls a user supplied callback method,
 *         OnCompletion.onCompletion(), if a OnCompletionListener is registered
 *         beforehand via {@link #setOnCompletionListener(OnCompletionListener)}.
 *         The invoke of the callback signals that the object is now in the <em>
 *         PlaybackCompleted</em> state.</li>
 *         <li>While in the <em>PlaybackCompleted</em>
 *         state, calling {@link #start()} can restart the playback from the
 *         beginning of the audio/video source.</li>
 * </ul>
 */

public class HiMediaPlayer
{
    /**
           <b>Currently not implemented</b>
       Constant to retrieve only the new metadata since the last
       call.
       // FIXME: unhide.
       // FIXME: add link to getMetadata(boolean, boolean)
       {@hide}
     */
    public static final boolean METADATA_UPDATE_ONLY = true;

    /**
           <b>Currently not implemented.</b>
       Constant to retrieve all the metadata.
       // FIXME: unhide.
       // FIXME: add link to getMetadata(boolean, boolean)
       {@hide}
     */
    public static final boolean METADATA_ALL = false;

    /**
           <b>Currently not implemented</b>
       Constant to enable the metadata filter during retrieval.
       // FIXME: unhide.
       // FIXME: add link to getMetadata(boolean, boolean)
       {@hide}
     */
    public static final boolean APPLY_METADATA_FILTER = true;

    /**
           <b>Currently not implemented.</b>
       Constant to disable the metadata filter during retrieval.
       // FIXME: unhide.
       // FIXME: add link to getMetadata(boolean, boolean)
       {@hide}
     */
    public static final boolean BYPASS_METADATA_FILTER = false;

    static {
        System.loadLibrary("himediaplayer_jni");
        native_init();
    }

    private final static String TAG = "HiMediaPlayer-Java";

    private int mNativeContext; // accessed by native methods
    private int mNativeSurfaceTexture;  // accessed by native methods
    private int mListenerContext; // accessed by native methods
    private Surface mSurface;
    private SurfaceHolder mSurfaceHolder;
    private EventHandler mEventHandler;
    private PowerManager.WakeLock mWakeLock = null;
    private boolean mScreenOnWhilePlaying;
    private boolean mStayAwake;

    static Context mContext = null;

    private int timeOut = 12000;

    /**
      * HiMediaPlayer initialization.
      * <br>
      */
    public HiMediaPlayer()
    {
        Looper looper;

        if ((looper = Looper.myLooper()) != null)
        {
            mEventHandler = new EventHandler(this, looper);
        }
        else if ((looper = Looper.getMainLooper()) != null)
        {
            mEventHandler = new EventHandler(this, looper);
        }
        else
        {
            mEventHandler = null;
        }

        /* Native setup requires a weak reference to our object.
         * It's easier to create it here than in C++.
         */
        native_setup(new WeakReference <HiMediaPlayer>(this));
    }

    private native void _setVideoSurface();

    /*
     * Update the MediaPlayer SurfaceTexture.
     * Call after setting a new display surface.
     */
    private native void _setVideoSurface(Surface surface);

    //set surface Position&Size
    private native void _setSubSurface();
    private native void _setSurfaceView();
        /**
      *
      * Set video file playback display width and height and location of the coordinate origin.
      * <br>
      * @param x Video display area coordinate origin x component<br>
      * @param y Video display area coordinate origin y component<br>
      * @param w Video display width<br>
      * @param h Video display height<br>
      * @return status code see system/core/include/utils/Errors.h <br>
      */
    public native int setVideoRange(int x, int y, int w, int h);
    private native int  _SetStereoVideoFmt(int inVideoFmt);
    private native void _SetStereoStrategy(int strategy);


    /**
     * Create a request parcel which can be routed to the native media
     * player using {@link #invoke(Parcel, Parcel)}.
     *
     * @return A parcel suitable to hold a request for the native
     * player.
     * {@hide}
     */
    public Parcel newRequest()
    {
        Parcel parcel = Parcel.obtain();
        return parcel;
    }

    /**
     * Invoke a generic method on the native player using opaque
     * parcels for the request and reply. Both payloads' format is a
     * convention between the java caller and the native player.
     * Must be called after setDataSource to make sure a native player
     * exists.
     *
     * @param request Parcel with the data for the extension. The
     * caller must use {@link #newRequest()} to get one.
     *
     * @param reply Output parcel with the data returned by the
     * native player.
     *
     * @return The status code see system/core/include/utils/Errors.h
     * {@hide}
     */
    public int invoke(Parcel request, Parcel reply)
    {
        int retcode = native_invoke(request, reply);

        reply.setDataPosition(0);
        return retcode;
    }

    /**
     * Sets the data source as a content Uri.
     * <br>
     * @param context the Context to use when resolving the Uri<br>
     * @param uri the Content URI of the data you want to play<br>
     * @throws IllegalStateException if it is called in an invalid state
     */
    public void setDataSource(Context context, Uri uri)
    throws IOException, IllegalArgumentException, SecurityException, IllegalStateException
    {
        setDataSource(context, uri, null);

        mContext = context;
    }

    /**
     * Sets the data source as a content Uri.
     * <br>
     * @param context the Context to use when resolving the Uri<br>
     * @param uri the Content URI of the data you want to play<br>
     * @param headers the headers to be sent together with the request for the data<br>
     * @throws IllegalStateException if it is called in an invalid state
     */
    public void setDataSource(Context context, Uri uri, Map <String, String> headers)
    throws IOException, IllegalArgumentException, SecurityException, IllegalStateException
    {
        mContext = context;
        String scheme = uri.getScheme();
        if ((scheme == null) || scheme.equals("file"))
        {
            setDataSource(uri.getPath());
            return;
        }

        AssetFileDescriptor fd = null;
        try
        {
            ContentResolver resolver = context.getContentResolver();
            fd = resolver.openAssetFileDescriptor(uri, "r");
            if (fd == null)
            {
                return;
            }
            // Note: using getDeclaredLength so that our behavior is the same
            // as previous versions when the content provider is returning
            // a full file.
            else if (fd.getDeclaredLength() < 0)
            {
                setDataSource(fd.getFileDescriptor());
            }
            else
            {
                setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getDeclaredLength());
            }

            return;
        } catch (SecurityException ex) {} catch (IOException ex) {}
        finally {
            if (fd != null)
            {
                fd.close();
            }
        }

        mContext = context;

        Log.d(TAG, "Couldn't open file on client side, trying server side");
        setDataSource(uri.toString(), headers);
        return;
    }

    /**
     * Sets the data source (file-path or http/rtsp URL) to use.
     * <br>
     * @param path the path of the file, or the http/rtsp URL of the stream you want to play<br>
     * @throws IllegalStateException if it is called in an invalid state
     */
    public native void setDataSource(String path)
    throws IOException, IllegalArgumentException, SecurityException, IllegalStateException;

    /**
     * Sets the data source (file-path or http/rtsp URL) to use.
     * <br>
     * @param path the path of the file, or the http/rtsp URL of the stream you want to play<br>
     * @param headers the headers associated with the http request for the stream you want to play<br>
     * @throws IllegalStateException if it is called in an invalid state
     * @hide pending API council
     */
    public void setDataSource(String path, Map <String, String> headers)
    throws IOException, IllegalArgumentException, SecurityException, IllegalStateException
    {
        String[] keys   = null;
        String[] values = null;

        if (headers != null)
        {
            keys   = new String[headers.size()];
            values = new String[headers.size()];

            int i = 0;
            for (Map.Entry <String, String> entry : headers.entrySet())
            {
                keys[i]   = entry.getKey();
                values[i] = entry.getValue();
                ++i;
            }
        }

        _setDataSource(path, keys, values);
    }

    private native void _setDataSource(String path, String[] keys, String[] values)
    throws IOException, IllegalArgumentException, SecurityException, IllegalStateException;

    /**
     * Sets the data source (FileDescriptor) to use. It is the caller's responsibility
     * to close the file descriptor. It is safe to do so as soon as this call returns.
     * <br>
     * @param fd the FileDescriptor for the file you want to play<br>
     * @throws IllegalStateException if it is called in an invalid state
     */
    public void setDataSource(FileDescriptor fd)
    throws IOException, IllegalArgumentException, IllegalStateException
    {
        // intentionally less than LONG_MAX
        setDataSource(fd, 0, 0x7ffffffffffffffL);
    }

    /**
     * Sets the data source (FileDescriptor) to use.  The FileDescriptor must be
     * seekable (N.B. a LocalSocket is not seekable). It is the caller's responsibility
     * to close the file descriptor. It is safe to do so as soon as this call returns.
     * <br>
     * @param fd the FileDescriptor for the file you want to play<br>
     * @param offset the offset into the file where the data to be played starts, in bytes<br>
     * @param length the length in bytes of the data to be played<br>
     * @throws IllegalStateException if it is called in an invalid state
     */
    public native void  setDataSource(FileDescriptor fd, long offset, long length)
    throws IOException, IllegalArgumentException, IllegalStateException;

    /**
     * Prepares the player for playback, synchronously.
     * After setting the datasource and the subtitle display surface and video display area, you need to either
     * call prepare() or prepareAsync(). For files, it is OK to call prepare(),
     * which blocks until HiMediaPlayer is ready for playback.But for streams like http/rtsp,you should use asynchronous method
     * prepareAsync.because synchronous long time block maybe cause anr error.<br>
     * @throws IllegalStateException if it is called in an invalid state
     */
    public native void  prepare() throws IOException, IllegalStateException;

    /**
     * Prepares the player for playback, asynchronously.
     * After setting the datasource and the display surface and video display area, you need to either
     * call prepare() or prepareAsync(). For network streams, you should call prepareAsync(),
     * which returns immediately, rather than blocking which maybe cause anr error until file has been analysed
     * .When prepared,OnPrepare will be call set by {@see #setOnPreparedListener}
     * <br>
     * @throws IllegalStateException if it is called in an invalid state
     */
    public native void  prepareAsync() throws IllegalStateException;

    /**
     * Starts or resumes playback. If playback had previously been paused,
     * playback will continue from where it was paused. If playback had
     * been stopped, or never started before, playback will start at the
     * beginning.
     * <br>
     * @throws IllegalStateException if it is called in an invalid state
     */
    public void start() throws IllegalStateException
    {
        stayAwake(true);
        _start();
    }

    private native void _start() throws IllegalStateException;

    /**
     * Stops playback after playback has been stopped or paused.
     * <br>
     * @throws IllegalStateException if the internal player engine has not been
     * initialized.
     */
    public void stop() throws IllegalStateException
    {
        stayAwake(false);
        _stop();
    }

    private native void _stop() throws IllegalStateException;

    /**
     * Pauses playback. Call start() to resume.
     * <br>
     * @throws IllegalStateException if the internal player engine has not been
     * initialized.
     */
    public void pause() throws IllegalStateException
    {
        stayAwake(false);
        _pause();
    }

    private native void _pause() throws IllegalStateException;

    private void stayAwake(boolean awake)
    {
        if (mWakeLock != null)
        {
            if (awake && !mWakeLock.isHeld())
            {
                mWakeLock.acquire();
            }
            else if (!awake && mWakeLock.isHeld())
            {
                mWakeLock.release();
            }
        }

        mStayAwake = awake;
        updateSurfaceScreenOn();
    }

    private void updateSurfaceScreenOn()
    {
        if (mSurfaceHolder != null)
        {
            mSurfaceHolder.setKeepScreenOn(mScreenOnWhilePlaying && mStayAwake);
        }
    }

    /**
     * Returns the width of the video.
     * <br>
     * @return the width of the video, or 0 if there is no video,
     * or the width has not been determined yet. The OnVideoSizeChangedListener can be registered via<br>
     * {@link #setOnVideoSizeChangedListener(OnVideoSizeChangedListener)}
     * to provide a notification when the width is available.
     */
    public native int         getVideoWidth();

    /**
     * Returns the height of the video.
     * <br>
     * @return the height of the video, or 0 if there is no video,
     * or the height has not been determined yet. The OnVideoSizeChangedListener can be registered via<br>
     * {@link #setOnVideoSizeChangedListener(OnVideoSizeChangedListener)}
     * to provide a notification when the height is available.
     */
    public native int         getVideoHeight();

    /**
     * Checks whether is playing.
     * <br>
     * @return true if currently playing, false otherwise<br>
     */
    public native boolean     isPlaying();

    /**
     * Seeks to specified time position.
     * <br>
     * @param msec the offset in milliseconds from the start to seek to<br>
     * @throws IllegalStateException if the internal player engine has not been
     * initialized
     */
    public native void        seekTo(int msec) throws IllegalStateException;

    /**
     * Gets the current playback position.
     * <br>
     * @return the current position in milliseconds<br>
     */
    public native int         getCurrentPosition();

    /**
     * Gets the duration of the file.
     * <br>
     * @return the duration in milliseconds<br>
     */
    public native int         getDuration();

    private int excuteCommand(int pCmdId, int pArg, boolean pIsGet)
    {
        Parcel Request = Parcel.obtain();
        Parcel Reply = Parcel.obtain();

        Request.writeInt(pCmdId);
        Request.writeInt(pArg);

        if (invoke(Request, Reply) != 0)
        {
            Request.recycle();
            Reply.recycle();
            return -1;
        }

        if (pIsGet)
        {
            Reply.readInt();
        }

        int Result = Reply.readInt();

        Request.recycle();
        Reply.recycle();

        return Result;
    }
    /**
     * Set the playback of the audio stream ID.
     * <br>
     * @param The int type, the index number of audio track info array you want to switch to<br>
     * @return Set the subtitle stream success. 0 - set successfully, -1 - set fail<br>
     */
    public int setAudioTrack(int track)
    {
        int flag = HiMediaPlayerInvoke.CMD_SET_AUDIO_TRACK_PID;

        return excuteCommand(flag, track, false);
    }
    /**
     * set audio channel mode.
     * <br>
     * @param channel Channel int types, values:<br>
     *                      0, stereo<br>
     *                      1, the left and right channel mixed output<br>
     *                      2, the left and right channel output left channel data<br>
     *                      3, the left and right channel output right channel data<br>
     *                      4, the left and right channel output, data exchange<br>
     *                      5, only the output of the right channel data<br>
     *                      6, only the output of left channel data<br>
     *                      7, mute<br>
     * @return Set the subtitle stream success. 0 - set successfully, -1 - set fail<br>
     */
    public int setAudioChannel(int channel)
    {
        int flag = HiMediaPlayerInvoke.CMD_SET_AUDIO_CHANNEL_MODE;

        return excuteCommand(flag, channel, false);
    }
    /**
     * Set caption display color.only for text subtitle,subtitle type is {@see HiMediaPlayerDefine.DEFINE_SUBT_FORMAT}
     * <br>
     * @param color Int type, value from 0x000000 - 0xFFFFFF high byte is R,middle byte is G,low byte is B<br>
     * @return Set the subtitle stream success. 0 - set successfully, -1 - set fail<br>
     */
    public int setSubFontColor(int color)
    {
        int flag = HiMediaPlayerInvoke.CMD_SET_SUB_FONT_COLOR;

        return excuteCommand(flag, color, false);
    }
    /**
     * Set caption display font type.only for text subtitle,subtitle type is {@see HiMediaPlayerDefine.DEFINE_SUBT_FORMAT}
     * <br>
     * @param style Style int types, values:<br>
     *              0, the normal<br>
     *              1, the shadow<br>
     *              2, hollow<br>
     *              3, the bold<br>
     *              4, italic<br>
     *              5, stroke<br>
     * @return Set the subtitle stream success. 0 - set successfully, -1 - set fail<br>
     */
    public int setSubFontStyle(int style)
    {
        int flag = HiMediaPlayerInvoke.CMD_SET_SUB_FONT_STYLE;

        return excuteCommand(flag, style, false);
    }
    /**
     * Set caption display font size.only for text subtitle,subtitle type is {@see HiMediaPlayerDefine.DEFINE_SUBT_FORMAT}
     * <br>
     * @param size Int type<br>
     * @return Set the subtitle stream success. 0 - set successfully, -1 - set fail<br>
     */
    public int setSubFontSize(int size)
    {
        int flag = HiMediaPlayerInvoke.CMD_SET_SUB_FONT_SIZE;

        return excuteCommand(flag, size, false);
    }

    /**
     * Set caption display character spacing.only for text subtitle,subtitle type is {@see HiMediaPlayerDefine.DEFINE_SUBT_FORMAT}
     * <br>
     * @param space Int type<br>
     * @return Set the subtitle stream success. 0 - set successfully, -1 - set fail<br>
     */
    public int setSubFontSpace(int space)
    {
        int flag = HiMediaPlayerInvoke.CMD_SET_SUB_FONT_SPACE;

        return excuteCommand(flag, space, false);
    }

    /**
     * Set caption display line spacing.only for text subtitle,subtitle type is {@see HiMediaPlayerDefine.DEFINE_SUBT_FORMAT}
     * <br>
     * @param linespace Int type<br>
     * @return Set the subtitle stream success. 0 - set successfully, -1 - set fail<br>
     */
    public int setSubFontLineSpace(int linespace)
    {
        int flag = HiMediaPlayerInvoke.CMD_SET_SUB_FONT_LINESPACE;

        return excuteCommand(flag, linespace, false);
    }
    /**
     * Set subtitle coding format.only for text subtitle,subtitle type is {@see HiMediaPlayerDefine.DEFINE_SUBT_FORMAT}
     * <br>
     * @param encode Int type<br>
              <table border="1" cellspacing="0" cellpadding="0">
              <tr>
                    <th>Value</th>
                    <th>Description</th>
              </tr>
              <tr>
                    <td>0</p></td>
                    <td>auto identify</p></td>
              </tr>
              <tr>
                  <td>1</p></td>
                  <td>Traditional Chinese(BIG5)</p></td>
             </tr>
             <tr>
                  <td>2</p></td>
                  <td>Universal Character Set(UTF8)</p></td>
             </tr>
             <tr>
                  <td>3</p></td>
                  <td>Western Europe(ISO8859_1)</p></td>
             </tr>
             <tr>
                  <td>4</p></td>
                  <td>Central Europe(ISO8859_2)</p></td>
             </tr>
             <tr>
                  <td>5</p></td>
                  <td>Southern Europe(ISO8859_3)</p></td>
             </tr>
             <tr>
                  <td>6</p></td>
                  <td>Nordic(ISO8859_4)</p></td>
             </tr>
             <tr>
                  <td>7</p></td>
                  <td>Slavic(ISO8859_5)</p></td>
             </tr>
                         <tr>
                  <td>8</p></td>
                  <td>Arabic(ISO8859_6)</p></td>
             </tr>
             <tr>
                  <td>9</p></td>
                  <td>Greek(ISO8859_7)</p></td>
             </tr>
             <tr>
                  <td>10</p></td>
                  <td>Hebrew(ISO8859_8)</p></td>
             </tr>
             <tr>
                  <td>11</p></td>
                  <td>Turkish(ISO8859_9)</p></td>
             </tr>
             <tr>
                  <td>12</p></td>
                  <td>Germanic(ISO8859_10)</p></td>
             </tr>
             <tr>
                  <td>13</p></td>
                  <td>Thai(ISO8859_11)</p></td>
             </tr>
             <tr>
                  <td>14</p></td>
                  <td>Baltic(ISO8859_13)</p></td>
             </tr>
             <tr>
                  <td>15</p></td>
                  <td>Celtic(ISO8859_14)</p></td>
             </tr>
             <tr>
                  <td>16</p></td>
                  <td>Western Europe(ISO8859_15)</p></td>
             </tr>
             <tr>
                  <td>17</p></td>
                  <td>Southeastern Europe(ISO8859_16)</p></td>
             </tr>
             <tr>
                  <td>18</p></td>
                  <td>Universal Character Set(UNICODE_16LE)</p></td>
             </tr>
             <tr>
                  <td>19</p></td>
                  <td>Universal Character Set(UNICODE_16BE)</p></td>
             </tr>
             <tr>
                  <td>20</p></td>
                  <td>Chinese(GBK)</p></td>
             </tr>
             <tr>
                  <td>21</p></td>
                  <td>Central Europe(CP1250)</p></td>
             </tr>
             <tr>
                  <td>22</p></td>
                  <td>Slavic(CP1251)</p></td>
             </tr>
             <tr>
                  <td>23</p></td>
                  <td>German(CP1252)</p></td>
             </tr>
             <tr>
                  <td>24</p></td>
                  <td>Greek(CP1253)</p></td>
             </tr>
             <tr>
                  <td>25</p></td>
                  <td>Turkish(CP1254)</p></td>
             </tr>
             <tr>
                  <td>26</p></td>
                  <td>Hebrew(CP1255)</p></td>
             </tr>
             <tr>
                  <td>27</p></td>
                  <td>Arabic(CP1256)</p></td>
             </tr>
             <tr>
                  <td>28</p></td>
                  <td>Baltic(CP1257)</p></td>
             </tr>
             <tr>
                  <td>29</p></td>
                  <td>Vietnamese(CP1258)</p></td>
             </tr>
             <tr>
                  <td>30</p></td>
                  <td>Thai(CP874)</p></td>
             </tr>
             <tr>
                  <td>31</p></td>
                  <td>Universal Character Set(UNICODE_32LE)</p></td>
             </tr>
             <tr>
                  <td>32</p></td>
                  <td>Universal Character Set(UNICODE_32BE)</p></td>
             </tr>
             </table>
     * @return Set the subtitle stream success. 0 - set successfully, -1 - set fail<br>
     */
    public int setSubEncode(int encode)
    {
        int flag = HiMediaPlayerInvoke.CMD_SET_SUB_FONT_ENCODE;

        return excuteCommand(flag, encode, false);
    }
    /**
     * Set caption display flow ID.
     * <br>
     * @param Int type,the subtitle index number(0 based) that you want to switch to<br>
     * @return Set the subtitle stream success. 0 - set successfully, -1 - set fail<br>
     */
    public int setSubTrack(int track)
    {
        int flag = HiMediaPlayerInvoke.CMD_SET_SUB_ID;

        return excuteCommand(flag, track, false);
    }
    /**
     * Set the caption and the synchronization time.
     * <br>
     * @param time Int type, used to adjust the caption display synchronization time, in MS unit.positive and negative number is valid<br>
     * @return Set the subtitle stream success. 0 - set successfully, -1 - set fail<br>
     */
    public int setSubTimeOffset(int time)
    {
        int flag = HiMediaPlayerInvoke.CMD_SET_SUB_TIME_SYNC;

        Parcel Request = Parcel.obtain();
        Parcel Reply = Parcel.obtain();

        Request.writeInt(flag);
        Request.writeInt(0);
        Request.writeInt(0);
        Request.writeInt(time);

        if (invoke(Request, Reply) != 0)
        {
            Request.recycle();
            Reply.recycle();
            return -1;
        }

        int Result = Reply.readInt();

        Request.recycle();
        Reply.recycle();

        return Result;
    }

        /**
     * Enable/Disable display subtitle.
     * <br>
     * @param int, <=0 -- enable. >0  -- disable<br>
     * @return 0 - set successfully, -1 - set fail<br>
     */
    public int enableSubtitle(int enable)
    {
        int flag = HiMediaPlayerInvoke.CMD_SET_SUB_DISABLE;

        return excuteCommand(flag, enable, false);
    }
    /**
     * Set caption display vertical position.
     * <br>
     * @param position Int type锛宻ubtitles distance from the bottom of the screen<br>
     * @return Set caption display vertical position of success. 0 - set successfully, -1 - set fail<br>
     */
    public int setSubVertical(int position)
    {
        int flag = HiMediaPlayerInvoke.CMD_SET_SUB_FONT_VERTICAL;

        return excuteCommand(flag, position, false);
    }
    /**
     * Import subtitle.
     * <br>
     * @param path,String type, subtitles path can be local file, also can be the network subtitles<br>
     * @return The success of import subtitle. 0 - into successful, -1 import failed<br>
     */
    public int setSubPath(String path)
    {
        int flag = HiMediaPlayerInvoke.CMD_SET_SUB_EXTRA_SUBNAME;
        Parcel Request = Parcel.obtain();
        Parcel Reply = Parcel.obtain();

        Request.writeInt(flag);
        Request.writeString(path);

        if (invoke(Request, Reply) != 0)
        {
            Request.recycle();
            Reply.recycle();
            return -1;
        }

        int Result = Reply.readInt();

        Request.recycle();
        Reply.recycle();

        return Result;
    }
    /**
     * Set playback speed.
     * <br>
     * @param speed Int type, range (-32, -16, -8, -4, -2,1,2,4,6,8,16,32).
     *              Less than 0 set to rewind playback, greater than 0 for fast playback, 1 indicates normal play.<br>
     * @return Speed play set success. 0 - set successfully, -1 - set fail<br>
     */
    public int setSpeed(int speed)
    {
        int flag;

        if(speed == 1)
        {
            flag = HiMediaPlayerInvoke.CMD_SET_STOP_FASTPLAY;
        }
        else if (speed == 2 || speed == 4 || speed == 8 || speed == 16 || speed == 32)
        {
            flag = HiMediaPlayerInvoke.CMD_SET_FORWORD;
        }
        else if (speed == -2 || speed == -4 || speed == -8 || speed == -16 || speed == -32)
        {
            flag  = HiMediaPlayerInvoke.CMD_SET_REWIND;
            speed = -speed;
        }
        else
        {
            Log.e(TAG,"setSpeed error:"+speed);
            return -1;
        }

        return excuteCommand(flag, speed, false);
    }
    /**
     * Gets the current audio and video format and file size, and metadata information.
     * <br>
     * @return To play the file information storage containers, including:
     * <table border="1" cellspacing="0" cellpadding="0">
     *   <tr>
     *     <th>Type</th>
     *     <th>Description</th>
     *   </tr>
     *   <tr>
     *     <td>int</p></td>
     *     <td>command execution results</p></td>
     *   </tr>
     *   <tr>
     *     <td>uint</p></td>
     *     <td>the current video coding format,<br>refer to {@see HiMediaPlayerDefine.DEFINE_VIDEO_ENCODING_FORMAT}</p></td>
     *   </tr>
     *   <tr>
     *     <td>uint</p></td>
     *     <td>audio coding format,<br>refer to {@see HiMediaPlayerDefine.DEFINE_AUDIO_ENCODING_FORMAT}</p></td>
     *   </tr>
     *   <tr>
     *     <td>long long</p></td>
     *     <td>file size</p></td>
     *   </tr>
     *   <tr>
     *     <td>int</p></td>
     *     <td>source type 0:local 1:vod 2:live</p></td>
     *   </tr>
     *     <td>String16</p></td>
     *     <td>Album</p></td>
     *   </tr>
     *   <tr>
     *     <td>String16</p></td>
     *     <td>Title</p></td>
     *   </tr>
     *   <tr>
     *     <td>String16</p></td>
     *     <td>Artist</p></td>
     *   </tr>
     *   <tr>
     *     <td>String16</p></td>
     *     <td>Genre</p></td>
     *   </tr>
     *   <tr>
     *     <td>String16</p></td>
     *     <td>Year</p></td>
     *   </tr>
     *   <tr>
     *     <td>String16</p></td>
     *     <td>date</p></td>
     *   </tr>
     * </table>
     */
    public Parcel getMediaInfo()
    {
        int flag = HiMediaPlayerInvoke.CMD_GET_FILE_INFO;
        Parcel Request = Parcel.obtain();
        Parcel Reply = Parcel.obtain();

        Request.writeInt(flag);

        if (invoke(Request, Reply) != 0)
        {
            Request.recycle();
            Reply.recycle();
            return null;
        }

        Reply.setDataPosition(0);

        Request.recycle();

        return Reply;
    }
    /**
     * set subtitle display surface.
     * <br>
     * @param sh The SurfaceHolder type, the display layer data.how to create surface,you can refer to com.hisilicon.android.videoplayer.activity.HisiVideoView<br>
     */
    public void setDisplay(SurfaceHolder sh)
    {
        mSurfaceHolder = sh;
        if (sh != null)
        {
            mSurface = sh.getSurface();
//            sh.setType(sh.SURFACE_TYPE_HISI_TRANSPARENT);
        }
        else
        {
            mSurface = null;
        }

        _setSurfaceView();
    }

    private int setBufferMaxSizeConfig(int max)
    {
        int flag = HiMediaPlayerInvoke.CMD_SET_BUFFER_MAX_SIZE;
        Parcel Request = Parcel.obtain();
        Parcel Reply = Parcel.obtain();

        Request.writeInt(flag);
        Request.writeInt(max);

        if (invoke(Request, Reply) != 0)
        {
            Request.recycle();
            Reply.recycle();
            return -1;
        }

        int Result = Reply.readInt();

        Request.recycle();
        Reply.recycle();

        return Result;
    }

    private int getBufferMaxSizeConfig()
    {
        int flag = HiMediaPlayerInvoke.CMD_GET_BUFFER_MAX_SIZE;
        Parcel Request = Parcel.obtain();
        Parcel Reply = Parcel.obtain();

        Request.writeInt(flag);

        if (invoke(Request, Reply) != 0)
        {
            Request.recycle();
            Reply.recycle();
            return -1;
        }

        Reply.readInt();
        int Result = Reply.readInt();

        Request.recycle();
        Reply.recycle();

        return Result;
    }
    /**
     * Setting data buffer(local buffer in network stream playback) in buffer size threshold.
     * there is 4 value you can set.the 4 values are start,enough,full and max.<br>
     * <b>start:</b>if local buffer size reach start and is less than enough threshold you set,the {@link #MEDIA_INFO_BUFFER_START} message will be reported by
     * {@see com.hisilicon.android.mediaplayer.HiMediaPlayer.OnInfoListener} you set,{@link #MEDIA_INFO_BUFFER_START} means that
     * local buffer starts downloading,if you set {@link com.hisilicon.android.mediaplayer.HiMediaPlayerInvoke#CMD_SET_BUFFER_UNDERRUN},himediaplayer
     * will pause the playback in order to accumulate more buffer to enough state.because in start state,little media buffer data maybe
     * cause unsmooth playback.<br>
     * <b>enough:</b>when local buffer size reach enough and is less than full threshold you set, {@link #MEDIA_INFO_BUFFER_ENOUGH} message
     * will be reported,this message means local buffer is enough for playback,you can resume playback.if you set
     * {@link com.hisilicon.android.mediaplayer.HiMediaPlayerInvoke#CMD_SET_BUFFER_UNDERRUN},himediaplayer will resume the playback.<br>
     * <b>full:</b>now full is no use,but in some reason,you must set a value,the value must obey the principle:max > total > enough > start<br>
     * <b>max:</b>now useless.but in some reason,you must set a value,the value must obey the principle:max > total > enough > start<br>
     * <b>setting value must obey the principle:max > total > enough > start</b>
     * <br>
     * @param    BufferConfig: The BufferConfig type, the structure variables respectively:
     *           1,     start:int type, start the download, with the unit of KByte锛?     *           2,     enough:int type, the buffered data to meet the playback requirements, can continue to play, to KByte as a unit.
     *           3,     full:int type, no use,but in some reason,you must set a value,the value must obey the principle:max > total > enough > start<br>
     *           4,     max:no use,but in some reason,you must set a value,the value must obey the principle:max > total > enough > start<br>
     * @return Data buffer size threshold setting success. 0 - set successfully, -1 - set fail<br>
     */
    public int setBufferSizeConfig(BufferConfig bufferConfig)
    {
        setBufferMaxSizeConfig(bufferConfig.max);

        int flag = HiMediaPlayerInvoke.CMD_SET_BUFFERSIZE_CONFIG;
        Parcel Request = Parcel.obtain();
        Parcel Reply = Parcel.obtain();

        Request.writeInt(flag);
        Request.writeInt(bufferConfig.full);
        Request.writeInt(bufferConfig.start);
        Request.writeInt(bufferConfig.enough);
        Request.writeInt(timeOut);

        if (invoke(Request, Reply) != 0)
        {
            Request.recycle();
            Reply.recycle();
            return -1;
        }

        int Result = Reply.readInt();

        Request.recycle();
        Reply.recycle();

        return Result;
    }
    /**
     * Set the size threshold in time value.
     * there is 4 value you can set.the 4 values are start,enough,full and max.<br>
     * <b>start:</b>if local buffer size reach start and is less than enough threshold you set,the {@link #MEDIA_INFO_BUFFER_START} message will be reported by
     * {@see com.hisilicon.android.mediaplayer.HiMediaPlayer.OnInfoListener} you set,{@link #MEDIA_INFO_BUFFER_START} means that
     * local buffer starts downloading,if you set {@link com.hisilicon.android.mediaplayer.HiMediaPlayerInvoke#CMD_SET_BUFFER_UNDERRUN},himediaplayer
     * will pause the playback in order to accumulate more buffer to enough state.because in start state,little media buffer data maybe
     * cause unsmooth playback.<br>
     * <b>enough:</b>when local buffer size reach enough and is less than full threshold you set, {@link #MEDIA_INFO_BUFFER_ENOUGH} message
     * will be reported,this message means local buffer is enough for playback,you can resume playback.if you set
     * {@link com.hisilicon.android.mediaplayer.HiMediaPlayerInvoke#CMD_SET_BUFFER_UNDERRUN},himediaplayer will resume the playback.<br>
     * <b>full:</b>no use,but in some reason,you must set a value,the value must obey the principle:max > total > enough > start<br>
     * <b>max:</b>the max buffer size in byte which bytes transformed from start,enough,full in time value can not exceed.<br>
     * <b>setting value must obey the principle:max > total > enough > start</b>
     * <br>
     * @param bufferConfig The BufferConfig type, the structure variables respectively:
     *           1,     start:int type, the current buffer data is not enough, can not meet the requirements to start the download, play, with the unit of MS
     *           2,     enough:int type, the buffered data to meet the playback requirements, can continue to play, to MS as a unit.
     *           3,     full:int type,no use,but in some reason,you must set a value,the value must obey the principle:max > total > enough > start<br>
     *           4,     max:int type, buffer only time setting is efficient in operation,
     *                      for some larger rate or resolution file, set the threshold size in byte which start,enough,full in time value can not exceed,
     *                      the max control memory usage threshold, always in KByte.<br>
     * @return Data buffer size threshold setting success. 0 - set successfully, -1 - set fail<br>
     */
    public int setBufferTimeConfig(BufferConfig bufferConfig)
    {
        setBufferMaxSizeConfig(bufferConfig.max);

        int flag = HiMediaPlayerInvoke.CMD_SET_BUFFERTIME_CONFIG;
        Parcel Request = Parcel.obtain();
        Parcel Reply = Parcel.obtain();

        Request.writeInt(flag);
        Request.writeInt(bufferConfig.full);
        Request.writeInt(bufferConfig.start);
        Request.writeInt(bufferConfig.enough);
        Request.writeInt(timeOut);

        if (invoke(Request, Reply) != 0)
        {
            Request.recycle();
            Reply.recycle();
            return -1;
        }

        int Result = Reply.readInt();

        Request.recycle();
        Reply.recycle();

        return Result;
    }
    /**
     * Gets the current playback buffer configuration size in byte.
     * <br>
     * @param bufferConfig The BufferConfig type, the structure variables respectively:<br>
     *          1,      start:int type, start the download, with the unit of KByte<br>
     *          2,      enough:int type, the buffered data to meet the playback requirements, can continue to play, to KByte as a unit.<br>
     *          3,      full:int type, the current buffer have all been used, data download full, with the unit of KByte<br>
     *          4,      max:int type, buffer only time setting is efficient in operation,<br>
     *                        for some rate or resolution of a larger file, set the threshold time will consume more memory space,
     *                        the max control memory usage threshold, always in KByte.<br>
     * @return Command execution results, 0 - to obtain information, -1 command failed<br>
     */
    public int getBufferSizeConfig(BufferConfig bufferConfig)
    {
        int flag = HiMediaPlayerInvoke.CMD_GET_BUFFERSIZE_CONFIG;
        Parcel Request = Parcel.obtain();
        Parcel Reply = Parcel.obtain();

        Request.writeInt(flag);

        if (invoke(Request, Reply) != 0)
        {
            Request.recycle();
            Reply.recycle();
            return -1;
        }

        Reply.readInt();

        bufferConfig.full   = Reply.readInt();
        bufferConfig.start  = Reply.readInt();
        bufferConfig.enough = Reply.readInt();
        bufferConfig.max = getBufferMaxSizeConfig();

        Request.recycle();
        Reply.recycle();

        return 0;
    }
    /**
     * Gets the current playback buffer configuration size in time(ms).
     * <br>
     * @param bufferConfig The BufferConfig type, the structure variables respectively:<br>
     *           1,     start:int type, the current buffer data is not enough, can not meet the requirements to start the download, play, with the unit of MS<br>
     *           2,     enough:int type, the buffered data to meet the playback requirements, can continue to play, to MS as a unit.<br>
     *           3,     full:int type, the current buffer have all been used, data download full, with the unit of MS<br>
     *           4,     max:int type, buffer only time setting is efficient in operation,
     *                      for some rate or resolution of a larger file, set the threshold time will consume more memory space,
     *                      the max control memory usage threshold, always in KByte.<br>
     * @return Command execution results, 0 - to obtain information, -1 command failed<br>
     */
    public int getBufferTimeConfig(BufferConfig bufferConfig)
    {
        int flag = HiMediaPlayerInvoke.CMD_GET_BUFFERTIME_CONFIG;
        Parcel Request = Parcel.obtain();
        Parcel Reply = Parcel.obtain();

        Request.writeInt(flag);

        if (invoke(Request, Reply) != 0)
        {
            Request.recycle();
            Reply.recycle();
            return -1;
        }

        Reply.readInt();

        bufferConfig.full   = Reply.readInt();
        bufferConfig.start  = Reply.readInt();
        bufferConfig.enough = Reply.readInt();
        bufferConfig.max = getBufferMaxSizeConfig();

        Request.recycle();
        Reply.recycle();

        return 0;
    }
    /**
     * The buffer size of the buffer data acquisition.
     * <br>
     * @return Returns the current buffer data size, with the unit of Kbytes.-1 call fail<br>
     */
    public int getBufferSizeStatus()
    {
        int flag = HiMediaPlayerInvoke.CMD_GET_BUFFER_STATUS;
        Parcel Request = Parcel.obtain();
        Parcel Reply = Parcel.obtain();

        Request.writeInt(flag);

        if (invoke(Request, Reply) != 0)
        {
            Request.recycle();
            Reply.recycle();
            return -1;
        }

        Reply.readInt();

        int Result = Reply.readInt();

        Request.recycle();
        Reply.recycle();

        return Result;
    }

    /**
     * Returns the current buffer data duration, with the unit of MS.
     * <br>
     * @return Returns the current cache data duration, with the unit of MS锛?1 call fail<br>
     */
    public int getBufferTimeStatus()
    {
        int flag = HiMediaPlayerInvoke.CMD_GET_BUFFER_STATUS;
        Parcel Request = Parcel.obtain();
        Parcel Reply = Parcel.obtain();

        Request.writeInt(flag);

        if (invoke(Request, Reply) != 0)
        {
            Request.recycle();
            Reply.recycle();
            return -1;
        }

        Reply.readInt();
        Reply.readInt();

        int Result = Reply.readInt()/1000;

        Request.recycle();
        Reply.recycle();

        return Result;
    }

    /**
     * Gets the current network bandwidth information.
     * <br>
     * @return Bandwidth information storage containers, including:<br>
     *         1, int type, command execution results,The status code see system/core/include/utils/Errors.h<br>
     *         2, int type, the current bandwidth information(bps)<br>
     */
    public Parcel getNetworkInfo()
    {
        int flag = HiMediaPlayerInvoke.CMD_GET_DOWNLOAD_SPEED;
        Parcel Request = Parcel.obtain();
        Parcel Reply = Parcel.obtain();

        Request.writeInt(flag);

        if (invoke(Request, Reply) != 0)
        {
            Request.recycle();
            Reply.recycle();
            return null;
        }

        Reply.setDataPosition(0);

        Request.recycle();

        return Reply;
    }
    /**
     * Set the current player frozen screen mode.
     * once you call {@link #reset()} --> {@link #setDataSource(FileDescriptor)} --> {@link #prepare()} to play next
     * file or stream.the last frozen picture int last file displays until next file plays if you set frozen mode.
     * black screen displays until next file plays if you set black screen mode.<br>
     * @param mode The int type, the last frame 锛? - frozen screen锛? 锛? - black screen锛?br>
     * @return Frozen screen mode is successful the player. 0 - set successfully, -1 - set fail<br>
     */
    public int setFreezeMode(int mode)
    {
        int flag = HiMediaPlayerInvoke.CMD_SET_VIDEO_FREEZE_MODE;

        return excuteCommand(flag, mode, false);
    }
    /**
     * Gets the current player frozen screen mode.
     * <br>
     * @return The current player frozen screen mode. The -1 command fails, the last frame 0 - frozen screen, 1 - frozen screen display screen<br>
     */
    public int getFreezeMode()
    {
        int flag = HiMediaPlayerInvoke.CMD_GET_VIDEO_FREEZE_MODE;
        Parcel Request = Parcel.obtain();
        Parcel Reply = Parcel.obtain();

        Request.writeInt(flag);

        if (invoke(Request, Reply) != 0)
        {
            Request.recycle();
            Reply.recycle();
            return -1;
        }

        Reply.readInt();

        int Result = Reply.readInt();

        Request.recycle();
        Reply.recycle();

        return Result;
    }

    /**
     * Set Video input format.
     * include 2D,TAB,SBS
     * <br>
     * @param inVideoFmt Int type, value type:
     *             {@see HiMediaPlayerDefine.ENUM_STEREOVIDEO_TYPE_ARG}<br>
     * @return The status code see system/core/include/utils/Errors.h
     */
    public int setStereoVideoFmt(int inVideoFmt)
    {
        return _SetStereoVideoFmt(inVideoFmt);
    }

    /**
     * Set the video output format.
         * for details,refer to from {@see com.hisilicon.android.mediaplayer.HiMediaPlayerDefine#DEFINE_STEREOVIDEO_STRATEGY_ADAPT_MASK} to {@see com.hisilicon.android.mediaplayer.HiMediaPlayerDefine#DEFINE_STEREOVIDEO_STRATEGY_3D_MASK}
     * <br>
     * @param strategy The int type, the value is as follows:
     *                 {@link com.hisilicon.android.mediaplayer.HiMediaPlayerDefine#DEFINE_STEREOVIDEO_STRATEGY_ADAPT_MASK}<br>
                       {@link com.hisilicon.android.mediaplayer.HiMediaPlayerDefine#DEFINE_STEREOVIDEO_STRATEGY_2D_MASK}<br>
                       {@link com.hisilicon.android.mediaplayer.HiMediaPlayerDefine#DEFINE_STEREOVIDEO_STRATEGY_3D_MASK}<br>
                       {@link com.hisilicon.android.mediaplayer.HiMediaPlayerDefine#DEFINE_STEREOVIDEO_STRATEGY_24FPS_MASK}<br>
     */
    public void setStereoStrategy(int strategy)
    {
        _SetStereoStrategy(strategy);
    }
    /** stream type
        @deprecated
    */
    public enum STREAM_TYPE_E
    {
        STREAM_ES,
        STREAM_TS,
        STREAM_NORMAL,
        STREAM_NET,
        STREAM_LIVE,

        STREAM_BUTT;

        //convert from int to STREAM_TYPE_E
        public static STREAM_TYPE_E valueOf(int value)
        {
            switch (value)
            {
            case 0:
                return STREAM_ES;
            case 1:
                return STREAM_TS;
            case 2:
                return STREAM_NORMAL;
            case 3:
                return STREAM_NET;
            case 4:
                return STREAM_LIVE;

            default:
                return STREAM_BUTT;
            }
        }
    }
    /**
     * Gets the media metadata.
     * <br>
     * @param  the param is invalid now.
     * @return The metadata, possibly empty. null if an error occured.i<br>
     * you can use Metadata returned like this pseudo-code :<br>
     * Metadata meta = App.GetMetaData();String TagAlbumString TagTitleString = meta.getString({@link com.hisilicon.android.mediaplayer.Metadata}.TITLE);<br>
     * {@link com.hisilicon.android.mediaplayer.Metadata}.ALBUM<br>
     * {@link com.hisilicon.android.mediaplayer.Metadata}.ARTIST<br>
     * {@link com.hisilicon.android.mediaplayer.Metadata}.TITLE<br>
     * {@link com.hisilicon.android.mediaplayer.Metadata}.GENRE<br>
     * can be returned<br>
     // FIXME: unhide.
     * {@hide}
     */
    public Metadata getMetadata(final boolean update_only,
                                final boolean apply_filter)
    {
        Parcel reply  = Parcel.obtain();
        Metadata data = new Metadata();

        if (!native_getMetadata(update_only, apply_filter, reply))
        {
            reply.recycle();
            return null;
        }

        // Metadata takes over the parcel, don't recycle it unless
        // there is an error.
        if (!data.parse(reply))
        {
            reply.recycle();
            return null;
        }

        return data;
    }

    /**
     * <b>Currently not implemented</b><br>
     * Set a filter for the metadata update notification and update
     * retrieval. The caller provides 2 set of metadata keys, allowed
     * and blocked. The blocked set always takes precedence over the
     * allowed one.
     * Metadata.MATCH_ALL and Metadata.MATCH_NONE are 2 sets available as
     * shorthands to allow/block all or no metadata.
     *
     * By default, there is no filter set.
     *
     * @param allow Is the set of metadata the client is interested
     *              in receiving new notifications for.
     * @param block Is the set of metadata the client is not interested
     *              in receiving new notifications for.
     * @return The call status code.
     *
     // FIXME: unhide.
     * {@hide}
     */
    public int setMetadataFilter(Set < Integer > allow, Set <Integer> block)
    {
        // Do our serialization manually instead of calling
        // Parcel.writeArray since the sets are made of the same type
        // we avoid paying the price of calling writeValue (used by
        // writeArray) which burns an extra int per element to encode
        // the type.
        Parcel request = newRequest();

        // The parcel starts already with an interface token. There
        // are 2 filters. Each one starts with a 4bytes number to
        // store the len followed by a number of int (4 bytes as well)
        // representing the metadata type.
        int capacity = request.dataSize() + 4 * (1 + allow.size() + 1 + block.size());

        if (request.dataCapacity() < capacity)
        {
            request.setDataCapacity(capacity);
        }

        request.writeInt(allow.size());
        for (Integer t : allow)
        {
            request.writeInt(t);
        }

        request.writeInt(block.size());
        for (Integer t : block)
        {
            request.writeInt(t);
        }

        return native_setMetadataFilter(request);
    }

    /**
     * Releases resources associated with this HiMediaPlayer object.
     * It is considered good practice to call this method when you're
     * done using the HiMediaPlayer.differing from reset call,you can
         * not do any HiMediaPlayer call after release call.
     * <br>
     */
    public void release()
    {
        stayAwake(false);
        updateSurfaceScreenOn();
        mOnPreparedListener = null;
        mOnBufferingUpdateListener = null;
        mOnCompletionListener   = null;
        mOnSeekCompleteListener = null;
        mOnErrorListener = null;
        mOnInfoListener = null;
        mOnVideoSizeChangedListener = null;
        mOnTimedTextListener = null;
        mOnFastBackwordCompleteListener = null;
        mOnFastForwardCompleteListener = null;
        _release();
    }

    private native void _release();

    /**
     * Resets the HiMediaPlayer to its uninitialized state. After calling
     * this method, you will have to re-initialize it by setting the
     * data source and calling prepare().
     * <br>
     */
    public void reset()
    {
        stayAwake(false);
        _reset();

        // make sure none of the listeners get called anymore
        mEventHandler.removeCallbacksAndMessages(null);
    }

    private native void  _reset();

    /**
     * Sets the player to be single-play looping or non-looping.
     * <br>
     * @param looping whether to loop or not<br>
     */
    public native void setLooping(boolean looping);

    /**
     * Checks whether the play mode is looping or non-looping.
     * <br>
     * @return true if is currently looping, false otherwise
     */
    public native boolean isLooping();

    /**
     * Sets the volume on this player.
     * This API is recommended for balancing the output of audio streams
     * within an application.
     * notice:now can not set right and left volume seperately,the final volum = (leftVolume + rightVolume) / 2
     *
     * @param leftVolume left volume scalar
     * @param rightVolume right volume scalar
     */
    public native void   setVolume(float leftVolume, float rightVolume);

    /**
     * <b>Currently not implemented</b>, returns null.
     * @hide<br>
     */
    public native Bitmap getFrameAt(int msec) throws IllegalStateException;

    /**
         * <b>Currently not implemented</b>
     * Attaches an auxiliary effect to the player. A typical auxiliary effect is a reverberation
     * effect which can be applied on any sound source that directs a certain amount of its
     * energy to this effect. This amount is defined by setAuxEffectSendLevel().
     * {@see #setAuxEffectSendLevel(float)}.
     * <p>After creating an auxiliary effect (e.g.
     * {@link android.media.audiofx.EnvironmentalReverb}), retrieve its ID with
     * {@link android.media.audiofx.AudioEffect#getId()} and use it when calling this method
     * to attach the player to the effect.
     * <p>To detach the effect from the player, call this method with a null effect id.
     * <p>This method must be called after one of the overloaded <code> setDataSource </code>
     * methods.
     * @param effectId system wide unique id of the effect to attach
     */
    public native void   attachAuxEffect(int effectId);

    /* Do not change these values (starting with KEY_PARAMETER) without updating
     * their counterparts in include/media/mediaplayer.h!
     */

    /**
     * <b>Currently not implemented</b>.
     * Key used in setParameter method.
     * Indicates the index of the timed text track to be enabled/disabled.
     * The index includes both the in-band and out-of-band timed text.
     * The index should start from in-band text if any. Application can retrieve the number
     * of in-band text tracks by using MediaMetadataRetriever::extractMetadata().
     * Note it might take a few hundred ms to scan an out-of-band text file
     * before displaying it.
     */
    private static final int KEY_PARAMETER_TIMED_TEXT_TRACK_INDEX = 1000;

    /**
         * <b>Currently not implemented</b>
     * Key used in setParameter method.
     * Used to add out-of-band timed text source path.
     * Application can add multiple text sources by calling setParameter() with
     * KEY_PARAMETER_TIMED_TEXT_ADD_OUT_OF_BAND_SOURCE multiple times.
     */
    private static final int KEY_PARAMETER_TIMED_TEXT_ADD_OUT_OF_BAND_SOURCE = 1001;

    // There are currently no defined keys usable from Java with get*Parameter.
    // But if any keys are defined, the order must be kept in sync with include/media/mediaplayer.h.
    // private static final int KEY_PARAMETER_... = ...;

    /**
         * <b>Currently not implemented</b>.
         * Sets the parameter indicated by key.
     * @param key key indicates the parameter to be set.
     * @param value value of the parameter to be set.
     * @return true if the parameter is set successfully, false otherwise
     * {@hide}
     */
    public native boolean setParameter(int key, Parcel value);

    /**
         * <b>Currently not implemented</b>.
     * Sets the parameter indicated by key.
     * @param key key indicates the parameter to be set.
     * @param value value of the parameter to be set.
     * @return true if the parameter is set successfully, false otherwise
     * {@hide}
     */
    public boolean setParameter(int key, String value)
    {
        Parcel p = Parcel.obtain();

        p.writeString(value);
        boolean ret = setParameter(key, p);
        p.recycle();
        return ret;
    }

    /**
         * <b>Currently not implemented</b>.
     * Sets the parameter indicated by key.
     * @param key key indicates the parameter to be set.
     * @param value value of the parameter to be set.
     * @return true if the parameter is set successfully, false otherwise
     * {@hide}
     */
    public boolean setParameter(int key, int value)
    {
        Parcel p = Parcel.obtain();

        p.writeInt(value);
        boolean ret = setParameter(key, p);
        p.recycle();
        return ret;
    }

    /**
     * <b>Currently not implemented</b>.
     * Gets the value of the parameter indicated by key.
     * @param key key indicates the parameter to get.
     * @param reply value of the parameter to get.
     */
    private native void getParameter(int key, Parcel reply);

    /**
     * <b>Currently not implemented</b>.
         * Gets the value of the parameter indicated by key.
     * The caller is responsible for recycling the returned parcel.
     * @param key key indicates the parameter to get.
     * @return value of the parameter.
     * {@hide}
     */
    public Parcel getParcelParameter(int key)
    {
        Parcel p = Parcel.obtain();

        getParameter(key, p);
        return p;
    }

    /**
     * <b>Currently not implemented</b>.
     * Gets the value of the parameter indicated by key.
     * @param key key indicates the parameter to get.
     * @return value of the parameter.
     * {@hide}
     */
    public String getStringParameter(int key)
    {
        Parcel p = Parcel.obtain();

        getParameter(key, p);
        String ret = p.readString();
        p.recycle();
        return ret;
    }

    /**
     * <b>Currently not implemented</b>.
     * Gets the value of the parameter indicated by key.
     * @param key key indicates the parameter to get.
     * @return value of the parameter.
     * {@hide}
     */
    public int getIntParameter(int key)
    {
        Parcel p = Parcel.obtain();

        getParameter(key, p);
        int ret = p.readInt();
        p.recycle();
        return ret;
    }

    /**
     * @param request Parcel destinated to the media player.
     * @param reply[out] Parcel that will contain the reply.
     * @return The status code.
     */
    private native final int         native_invoke(Parcel request, Parcel reply);

    /**
     * @param update_only If true fetch only the set of metadata that have
     *                    changed since the last invocation of getMetadata.
     *                    The set is built using the unfiltered
     *                    notifications the native player sent to the
     *                    MediaPlayerService during that period of
     *                    time. If false, all the metadatas are considered.
     * @param apply_filter  If true, once the metadata set has been built based on
     *                     the value update_only, the current filter is applied.
     * @param reply[out] On return contains the serialized
     *                   metadata. Valid only if the call was successful.
     * @return The status code.
     */
    private native final boolean     native_getMetadata(boolean update_only,
                                                        boolean apply_filter,
                                                        Parcel  reply);

    /**
     * @param request Parcel with the 2 serialized lists of allowed
     *                metadata types followed by the one to be
     *                dropped. Each list starts with an integer
     *                indicating the number of metadata type elements.
     * @return The status code.
     */
    private native final int         native_setMetadataFilter(Parcel request);

    private static native final void native_init();
    private native final void        native_setup(Object mediaplayer_this);
    private native final void        native_finalize();

    /**
         * <b>Currently not implemented</b>.
     * @param index The index of the text track to be turned on.
     * @return true if the text track is enabled successfully.
     * {@hide}
     */
    public boolean enableTimedTextTrackIndex(int index)
    {
        if (index < 0)
        {
            return false;
        }

        return setParameter(KEY_PARAMETER_TIMED_TEXT_TRACK_INDEX, index);
    }

    /**
         * <b>Currently not implemented</b>.
     * Enables the first timed text track if any.
     * @return true if the text track is enabled successfully
     * {@hide}
     */
    public boolean enableTimedText()
    {
        return enableTimedTextTrackIndex(0);
    }

    /**
     * <b>Currently not implemented</b>.
     * Disables timed text display.
     * @return true if the text track is disabled successfully.
     * {@hide}
     */
    public boolean disableTimedText()
    {
        return setParameter(KEY_PARAMETER_TIMED_TEXT_TRACK_INDEX, -1);
    }

    /**
     * <b>Currently not implemented</b>.
     * @param reply Parcel with audio/video duration info for battery
                    tracking usage
     * @return The status code.
     * {@hide}
     */
    public native static int native_pullBatteryData(Parcel reply);

    @Override
    protected void finalize()
    {
        native_finalize();
    }

    private static final int MEDIA_NOP = 0;
    private static final int MEDIA_PREPARED = 1;
    private static final int MEDIA_PLAYBACK_COMPLETE = 2;
    private static final int MEDIA_BUFFERING_UPDATE = 3;
    private static final int MEDIA_SEEK_COMPLETE  = 4;
    private static final int MEDIA_SET_VIDEO_SIZE = 5;
    private static final int MEDIA_TIMED_TEXT = 99;
    private static final int MEDIA_FAST_FORWORD_COMPLETE  = 20;
    private static final int MEDIA_FAST_BACKWORD_COMPLETE = 21;
    private static final int MEDIA_ERROR = 100;
    private static final int MEDIA_INFO = 200;

    private class EventHandler extends Handler
    {
        private HiMediaPlayer mMediaPlayer;

        public EventHandler(HiMediaPlayer mp, Looper looper)
        {
            super(looper);
            mMediaPlayer = mp;
        }

        @Override
        public void handleMessage(Message msg)
        {
            if (mMediaPlayer.mNativeContext == 0)
            {
                Log.w(TAG, "mediaplayer went away with unhandled events");
                return;
            }

            switch (msg.what)
            {
            case MEDIA_PREPARED:
                if (mOnPreparedListener != null)
                {
                    mOnPreparedListener.onPrepared(mMediaPlayer);
                }

                return;

            case MEDIA_PLAYBACK_COMPLETE:
                if (mOnCompletionListener != null)
                {
                    mOnCompletionListener.onCompletion(mMediaPlayer);
                }

                stayAwake(false);
                return;

            case MEDIA_BUFFERING_UPDATE:
                if (mOnBufferingUpdateListener != null)
                {
                    mOnBufferingUpdateListener.onBufferingUpdate(mMediaPlayer, msg.arg1);
                }

                return;

            case MEDIA_SEEK_COMPLETE:
                if (mOnSeekCompleteListener != null)
                {
                    mOnSeekCompleteListener.onSeekComplete(mMediaPlayer);
                }

                return;

            case MEDIA_SET_VIDEO_SIZE:
                if (mOnVideoSizeChangedListener != null)
                {
                    mOnVideoSizeChangedListener.onVideoSizeChanged(mMediaPlayer, msg.arg1, msg.arg2);
                }

                return;

            case MEDIA_FAST_FORWORD_COMPLETE:
                Log.v (TAG, "Recevied MEDIA_FAST_FORWORD_COMPLETE");
                if (mOnFastForwardCompleteListener != null)
                {
                    mOnFastForwardCompleteListener.onFastForwardComplete(mMediaPlayer);
                }

                return;

            case MEDIA_FAST_BACKWORD_COMPLETE:
                Log.v (TAG, "Recevied MEDIA_FAST_BACKWORD_COMPLETE");
                if (mOnFastBackwordCompleteListener != null)
                {
                    mOnFastBackwordCompleteListener.onFastBackwordComplete(mMediaPlayer);
                }

                return;

            case MEDIA_ERROR:

                // For PV specific error values (msg.arg2) look in
                // opencore/pvmi/pvmf/include/pvmf_return_codes.h
                Log.e(TAG, "Error (" + msg.arg1 + "," + msg.arg2 + ")");
                boolean error_was_handled = false;
                if (mOnErrorListener != null)
                {
                    error_was_handled = mOnErrorListener.onError(mMediaPlayer, msg.arg1, msg.arg2);
                }

                if ((mOnCompletionListener != null) && !error_was_handled)
                {
                    mOnCompletionListener.onCompletion(mMediaPlayer);
                }

                stayAwake(false);
                return;

            case MEDIA_INFO:
                if (msg.arg1 != MEDIA_INFO_VIDEO_TRACK_LAGGING)
                {
                    Log.i(TAG, "Info (" + msg.arg1 + "," + msg.arg2 + ")");
                }

                if (mOnInfoListener != null)
                {
                    mOnInfoListener.onInfo(mMediaPlayer, msg.arg1, msg.arg2);
                }

                // No real default action so far.
                return;
            case MEDIA_TIMED_TEXT:
                if (mOnTimedTextListener != null)
                {
                    if (msg.obj == null)
                    {
                        mOnTimedTextListener.onTimedText(mMediaPlayer, null);
                    }
                    else
                    {
                        if (msg.obj instanceof byte[])
                        {
                            TimedText text = new TimedText((byte[])(msg.obj));
                            mOnTimedTextListener.onTimedText(mMediaPlayer, text);
                        }
                    }
                }

                return;

            case MEDIA_NOP:     // interface test message - ignore
                break;

            default:
                Log.e(TAG, "Unknown message type " + msg.what);
                return;
            }
        }
    }

    /**
     * Called from native code when an interesting event happens.  This method
     * just uses the EventHandler system to post the event back to the main app thread.
     * We use a weak reference to the original MediaPlayer object so that the native
     * code is safe from the object disappearing from underneath it.  (This is
     * the cookie passed to native_setup().)
     */
    private static void postEventFromNative(Object mediaplayer_ref,
                                            int what, int arg1, int arg2, Object obj)
    {
        HiMediaPlayer mp = (HiMediaPlayer)((WeakReference)mediaplayer_ref).get();

        if (mp == null)
        {
            return;
        }

        if (mp.mEventHandler != null)
        {
            Message m = mp.mEventHandler.obtainMessage(what, arg1, arg2, obj);
            mp.mEventHandler.sendMessage(m);
        }
    }

    /**
     * Interface definition for a callback to be invoked when the media
     * source is ready for playback.
     */
    public interface OnPreparedListener
    {
        /**
         * Called when the media file is ready for playback.
         *
         * @param mp the MediaPlayer that is ready for playback
         */
        void onPrepared(HiMediaPlayer mp);
    }

    /**
     * Register a callback to be invoked when the media source is ready
     * for playback.
     *
     * @param listener the callback that will be run
     */
    public void setOnPreparedListener(OnPreparedListener listener)
    {
        mOnPreparedListener = listener;
    }

    private OnPreparedListener mOnPreparedListener;

    /**
     * Interface definition for a callback to be invoked when playback of
     * a media source has completed.
     */
    public interface OnCompletionListener
    {
        /**
         * Called when the end of a media source is reached during playback.
         *
         * @param mp the MediaPlayer that reached the end of the file
         */
        void onCompletion(HiMediaPlayer mp);
    }

    /**
     * Register a callback to be invoked when the end of a media source
     * has been reached during playback.
     *
     * @param listener the callback that will be run
     */
    public void setOnCompletionListener(OnCompletionListener listener)
    {
        mOnCompletionListener = listener;
    }

    private OnCompletionListener mOnCompletionListener;

    /**
     * Interface definition of a callback to be invoked indicating buffering
     * status of a media resource being streamed over the network.
     */
    public interface OnBufferingUpdateListener
    {
        /**
         * Called to update status in buffering a media stream received through
         * progressive HTTP download. The received buffering percentage
         * indicates how much of the content has been buffered or played.
         * For example a buffering update of 80 percent when half the content
         * has already been played indicates that the next 30 percent of the
         * content to play has been buffered.
         *
         * @param mp      the MediaPlayer the update pertains to
         * @param percent the percentage (0-100) of the content
         *                that has been buffered or played thus far
         */
        void onBufferingUpdate(HiMediaPlayer mp, int percent);
    }

    /**
     * Register a callback to be invoked when the status of a network
     * stream's buffer has changed.
     *
     * @param listener the callback that will be run.
     */
    public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener)
    {
        mOnBufferingUpdateListener = listener;
    }

    private OnBufferingUpdateListener mOnBufferingUpdateListener;

    /**
     * Interface definition of a callback to be invoked indicating
     * the completion of a seek operation.
     */
    public interface OnSeekCompleteListener
    {
        /**
         * Called to indicate the completion of a seek operation.
         *
         * @param mp the MediaPlayer that issued the seek operation
         */
        public void onSeekComplete(HiMediaPlayer mp);
    }

    /**
     * Register a callback to be invoked when a seek operation has been
     * completed.
     *
     * @param listener the callback that will be run
     */
    public void setOnSeekCompleteListener(OnSeekCompleteListener listener)
    {
        mOnSeekCompleteListener = listener;
    }

    private OnSeekCompleteListener mOnSeekCompleteListener;

    /**
     * Interface definition of a callback to be invoked when the
     * video size is first known or updated
     */
    public interface OnVideoSizeChangedListener
    {
        /**
         * Called to indicate the video size
         *
         * @param mp        the MediaPlayer associated with this callback
         * @param width     the width of the video
         * @param height    the height of the video
         */
        public void onVideoSizeChanged(HiMediaPlayer mp, int width, int height);
    }

    /**
     * Register a callback to be invoked when the video size is
     * known.
     *
     * @param listener the callback that will be run
     */
    public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener listener)
    {
        mOnVideoSizeChangedListener = listener;
    }

    private OnVideoSizeChangedListener mOnVideoSizeChangedListener;

    /**
       <b>Currently not implemented</b><br>
     * Interface definition of a callback to be invoked when a
     * timed text is available for display.
     * {@hide}
     */
    public interface OnTimedTextListener
    {
        /**
         * Called to indicate an avaliable timed text
         *
         * @param mp             the MediaPlayer associated with this callback
         * @param text           the timed text sample which contains the text
         *                       needed to be displayed and the display format.
         * {@hide}
         */
        public void onTimedText(HiMediaPlayer mp, TimedText text);
    }

    private OnTimedTextListener mOnTimedTextListener;

        /**
        <b>Currently not implemented</b>
        */
    public interface OnFastForwardCompleteListener
    {
        /**
         * Called to indicate the completion of a fast forword operation.
         *
         * @param mp the MediaPlayer that issued the fast forward operation
         */
        public void onFastForwardComplete(HiMediaPlayer mp);
    }

    /**
     * <b>Currently not implemented</b>
     * Register a callback to be invoked when a fast forward operation has been
     * completed.
     *
     * @param listener the callback that will be run
     */
    public void setFastForwardCompleteListener(OnFastForwardCompleteListener listener)
    {
        mOnFastForwardCompleteListener = listener;
    }

    private OnFastForwardCompleteListener mOnFastForwardCompleteListener;

    /**
     * <b>Currently not implemented</b>
     * Interface definition of a callback to be invoked indicating
     * the completion of a fast backword operation.
     */
    public interface OnFastBackwordCompleteListener
    {
        /**
         * Called to indicate the completion of a fast backword operation.
         *
         * @param mp the MediaPlayer that issued the fast backword operation
         */
        public void onFastBackwordComplete(HiMediaPlayer mp);
    }

    /**
     * <b>Currently not implemented</b>
     * Register a callback to be invoked when a fast backword operation has been
     * completed.
     *
     * @param listener the callback that will be run
     */
    public void setOnFastBackwordCompleteListener(OnFastBackwordCompleteListener listener)
    {
        mOnFastBackwordCompleteListener = listener;
    }

    private OnFastBackwordCompleteListener mOnFastBackwordCompleteListener;

    /* Do not change these values without updating their counterparts
     * in include/media/mediaplayer.h!
     */

    /** Unspecified media player error.no extra parameter.
     *  @see com.hisilicon.android.mediaplayer.HiMediaPlayer.OnErrorListener
     */
    public static final int MEDIA_ERROR_UNKNOWN = 1;

    /**
     * <b>Currently not implemented</b>
         * Media server died. In this case, the application must release the
     * HiMediaPlayer object and instantiate a new one.
     */
    public static final int MEDIA_ERROR_SERVER_DIED = 100;
   /**
     * <b>Currently not implemented</b><br>
     * The video is streamed and its container is not valid for progressive
     * playback i.e the video's index (e.g moov atom) is not at the start of the
     * file.
     */
    public static final int MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK = 200;

    /**
     * Interface definition of a callback to be invoked when there
     * has been an error during an asynchronous operation (other errors
     * will throw exceptions at method call time).
     */
    public interface OnErrorListener
    {
        /**
         * Called to indicate an error.
         *
         * @param mp      the HiMediaPlayer the error pertains to
         * @param what    the type of error that has occurred:
         * <ul>
         * <li>{@link #MEDIA_ERROR_UNKNOWN}:the message will report when {@link #prepare()} or
         * {@link #prepareAsync()}} fail
         * </ul>
         * @param extra an extra code, specific to the error. Typically
         * implementation dependant.
         * @return True if the method handled the error, false if it didn't.
         * Returning false, or not having an {@see com.hisilicon.android.mediaplayer.HiMediaPlayer.OnErrorListener} at all, will
         * cause the {@see com.hisilicon.android.mediaplayer.HiMediaPlayer.OnCompletionListener} to be called.
         */
        boolean onError(HiMediaPlayer mp, int what, int extra);
    }

    /**
     * Register a callback to be invoked when an error has happened
     * during an asynchronous operation.
     *
     * @param listener the callback that will be run
     */
    public void setOnErrorListener(OnErrorListener listener)
    {
        mOnErrorListener = listener;
    }

    private OnErrorListener mOnErrorListener;

    /* Do not change these values without updating their counterparts
     * in include/media/mediaplayer.h!
     */

    /**
     * <b>Currently not implemented</b><br>
     * Unspecified media player info.
     */
    public static final int MEDIA_INFO_UNKNOWN = 1;

    /**
     * <b>Currently not implemented</b><br>
     * The video is too complex for the decoder: it can't decode frames fast
     * enough. Possibly only the audio plays fine at this stage.
     */
    public static final int MEDIA_INFO_VIDEO_TRACK_LAGGING = 700;

    /**
     * <b>Currently not implemented</b><br>
     * Android defined buffer event<br>
     * HiMediaPlayer is temporarily pausing playback internally in order to<br>
     * buffer more data.<br>
     *
     */
    public static final int MEDIA_INFO_BUFFERING_START = 701;

    /**
     * <b>Currently not implemented</b><br>
     * Android defined buffer event
     * HiMediaPlayer is resuming playback after filling buffers.
     */
    public static final int MEDIA_INFO_BUFFERING_END = 702;
    /**
       <b>Currently not implemented</b>
    */
    public static final int MEDIA_INFO_NETWORK_BANDWIDTH = 703;
    /**
       Prepare progress message.
       this message is reported by {@see com.hisilicon.android.mediaplayer.HiMediaPlayer.OnInfoListener}
       extra data is progress(0--100).
    */
    public static final int MEDIA_INFO_PREPARE_PROGRESS = 710;

    /**
     * <b>Currently not implemented</b><br>
     * Bad interleaving means that a media has been improperly interleaved or
     * not interleaved at all, e.g has all the video samples first then all the
     * audio ones. Video is playing but a lot of disk seeks may be happening.
     */
    public static final int MEDIA_INFO_BAD_INTERLEAVING = 800;

    /**
     * <b>Currently not implemented</b>
     * The media cannot be seeked (e.g live stream)
     */
    public static final int MEDIA_INFO_NOT_SEEKABLE = 801;

    /**
     * <b>Currently not implemented</b><br>
     * A new set of metadata is available.
     */
    public static final int MEDIA_INFO_METADATA_UPDATE = 802;

    //Add Hisi Media Info
    /**
       Audio play fail    e.g. Fail to start audio decoder
       this message is reported by {@see com.hisilicon.android.mediaplayer.HiMediaPlayer.OnInfoListener}<br>
       extra--none
    */
    public static final int MEDIA_INFO_AUDIO_FAIL = 1000;
    /**
       Audio play fail    e.g. Fail to start video decoder
       this message is reported by {@see com.hisilicon.android.mediaplayer.HiMediaPlayer.OnInfoListener}<br>
       extra--none
    */
    public static final int MEDIA_INFO_VIDEO_FAIL = 1001;
    /**
      this message is reported by {@see com.hisilicon.android.mediaplayer.HiMediaPlayer.OnInfoListener}<br>
      extra---{@see HiMediaPlayerDefine.DEFINE_NETWORK_EVENT}
    */
    public static final int MEDIA_INFO_NETWORK = 1002;
    /**
       <b>Currently not implemented</b>
    */
    public static final int MEDIA_INFO_TIMEOUT = 1003;
    /**
       <b>Currently not implemented</b>
    */
    public static final int MEDIA_INFO_NOT_SUPPORT   = 1004;
    /**
       For network stream playback.Local cache buffer is empty<br>
       For details,refer to {@see #setBufferSizeConfig} and
       {@see #setBufferTimeConfig}<br>
       this message is reported by {@see com.hisilicon.android.mediaplayer.HiMediaPlayer.OnInfoListener}<br>
       extra--none
    */
    public static final int MEDIA_INFO_BUFFER_EMPTY  = 1005;
    /**
       For network stream playback.Begin cache local data<br>
       For details,refer to {@see #setBufferSizeConfig} and
       {@see #setBufferTimeConfig}<br>
       this message is reported by {@see com.hisilicon.android.mediaplayer.HiMediaPlayer.OnInfoListener}<br>
       extra--none
    */
    public static final int MEDIA_INFO_BUFFER_START  = 1006;
    /**
       For network stream playback.The local cache data is enough to
       continue playing.<br>
       For details,refer to {@see #setBufferSizeConfig} and
       {@see #setBufferTimeConfig}<br>
       this message is reported by {@see com.hisilicon.android.mediaplayer.HiMediaPlayer.OnInfoListener}<br>
       extra--none
    */
    public static final int MEDIA_INFO_BUFFER_ENOUGH = 1007;
    /**
       @deprecated<br>
       For network stream playback.The local cache data is full,do
       not download again.<br>
       For details,refer to {@see #setBufferSizeConfig} and
       {@see #setBufferTimeConfig}<br>
       this message is reported by {@see com.hisilicon.android.mediaplayer.HiMediaPlayer.OnInfoListener}<br>
       extra--none
    */
    public static final int MEDIA_INFO_BUFFER_FULL = 1008;
    /**
       For network stream playback.Finish downloading file
       this message is reported by {@see com.hisilicon.android.mediaplayer.HiMediaPlayer.OnInfoListener}<br>
       extra--none
    */
    public static final int MEDIA_INFO_BUFFER_DOWNLOAD_FIN = 1009;
    /**
       The decoded time(ms) of first frame from {@link #start()} operation<br>
       this message is reported by {@see com.hisilicon.android.mediaplayer.HiMediaPlayer.OnInfoListener}<br>
       extra--time(ms)
    */
    public static final int MEDIA_INFO_FIRST_FRAME_TIME = 1010;
    /**
        unknown network error
        this message is reported by {@see com.hisilicon.android.mediaplayer.HiMediaPlayer.OnInfoListener}<br>
        it is extra data of {@link #MEDIA_INFO_NETWORK}.
        {@see HiMediaPlayerDefine.DEFINE_NETWORK_EVENT}
     */
    public static final int MEDIA_INFO_NETWORK_ERROR_UNKNOW = 0;
    /**
       connection error
       this message is reported by {@see com.hisilicon.android.mediaplayer.HiMediaPlayer.OnInfoListener}<br>
       it is extra data of {@link #MEDIA_INFO_NETWORK}.
       {@see HiMediaPlayerDefine.DEFINE_NETWORK_EVENT}
    */
    public static final int MEDIA_INFO_NETWORK_ERROR_CONNECT_FAILED = 1;
    /**
       operation timeout
       this message is reported by {@see com.hisilicon.android.mediaplayer.HiMediaPlayer.OnInfoListener}<br>
       it is extra data of {@link #MEDIA_INFO_NETWORK}.
       {@see HiMediaPlayerDefine.DEFINE_NETWORK_EVENT}
    */
    public static final int MEDIA_INFO_NETWORK_ERROR_TIMEOUT   = 2;
    /**
       network disconnect
       this message is reported by {@see com.hisilicon.android.mediaplayer.HiMediaPlayer.OnInfoListener}<br>
       it is extra data of {@link #MEDIA_INFO_NETWORK}.
       {@see HiMediaPlayerDefine.DEFINE_NETWORK_EVENT}
    */
    public static final int MEDIA_INFO_NETWORK_ERROR_DISCONNECT = 3;
    /**
       file not found
       this message is reported by {@see com.hisilicon.android.mediaplayer.HiMediaPlayer.OnInfoListener}<br>
       it is extra data of {@link #MEDIA_INFO_NETWORK}.
       {@see HiMediaPlayerDefine.DEFINE_NETWORK_EVENT}
    */
    public static final int MEDIA_INFO_NETWORK_ERROR_NOT_FOUND = 4;
    /**
       status of network is normal
       this message is reported by {@see com.hisilicon.android.mediaplayer.HiMediaPlayer.OnInfoListener}<br>
       it is extra data of {@link #MEDIA_INFO_NETWORK}.
       {@see HiMediaPlayerDefine.DEFINE_NETWORK_EVENT}
    */
    public static final int MEDIA_INFO_NETWORK_NORMAL = 5;

    /**
     * Interface definition of a callback to be invoked to communicate some
     * info and/or warning about the media or its playback.
     */
    public interface OnInfoListener
    {
        /**
         * Called to indicate an info or a warning.
         *
         * @param mp      the HiMediaPlayer the info pertains to.
         * @param what    the type of info or warning.
         * <ul>
         * <li>{@link #MEDIA_INFO_PREPARE_PROGRESS}<br>
         -----hisi info-----
         * <li>{@link #MEDIA_INFO_AUDIO_FAIL}
         * <li>{@link #MEDIA_INFO_VIDEO_FAIL}
         * <li>{@link #MEDIA_INFO_NETWORK}
         * <li>{@link #MEDIA_INFO_BUFFER_EMPTY}
         * <li>{@link #MEDIA_INFO_BUFFER_START}
         * <li>{@link #MEDIA_INFO_BUFFER_ENOUGH}
         * <li>{@link #MEDIA_INFO_BUFFER_FULL}
         * <li>{@link #MEDIA_INFO_BUFFER_DOWNLOAD_FIN}
         * <li>{@link #MEDIA_INFO_FIRST_FRAME_TIME}
         * </ul>
         * @param extra an extra code, specific to the info. Typically
         * implementation dependant.
         * @return True if the method handled the info, false if it didn't.
         * Returning false, or not having an OnErrorListener at all, will
         * cause the info to be discarded.
         */
        boolean onInfo(HiMediaPlayer mp, int what, int extra);
    }

    /**
     * Register a callback to be invoked when an info/warning is available.
     *
     * @param listener the callback that will be run
     */
    public void setOnInfoListener(OnInfoListener listener)
    {
        mOnInfoListener = listener;
    }

    private OnInfoListener mOnInfoListener;
}
