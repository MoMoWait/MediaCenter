/**
 * Title: AsyncRetrieveMediaInfo.java<br>
 * Package: com.rockchips.mediacenter.retrieve<br>
 * Description: TODO<br>
 * @author c00224451
 * @version v1.0<br>
 * Date: 2014-8-29下午2:53:49<br>
 * Copyright © Huawei Technologies Co., Ltd. 2014. All rights reserved.
 */

package com.rockchips.mediacenter.retrieve;

import java.util.ArrayList;
import java.util.HashMap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;

import com.rockchips.mediacenter.basicutils.bean.LocalMediaInfo;
import com.rockchips.mediacenter.basicutils.constant.Constant;
import com.rockchips.mediacenter.basicutils.util.DateUtil;
import com.rockchips.mediacenter.basicutils.util.IICLOG;
import com.rockchips.mediacenter.basicutils.util.StringUtils;
import com.rockchips.mediacenter.audioplayer.SongInfo;

/**
 * Description: TODO<br>
 * @author c00224451
 * @version v1.0
 * Date: 2014-8-29 下午2:53:49<br>
 */

public class RetrieveInfoManager
{
    private ArrayList <RetrieveInfo> mWaitingQueue = new ArrayList <RetrieveInfo>();
    private RetrieveCompleteListener mRetrieveCompleteListener;
    private Object mLock = new Object();

    private static final String TAG = "MediaCenterApp";
    private static IICLOG Log = IICLOG.getInstance();

    private boolean bIsStop = false;

    private static RetrieveInfoManager mInstance = null;

    private Thread mRetrieveMediaInfoThread;
    private MediaMetadataRetriever retriever;
    
    private static final String INVALID_DUARTION = "INVALID_DUARTION";

    protected RetrieveInfoManager()
    {
        retriever = new MediaMetadataRetriever();
        mRetrieveMediaInfoThread = new Thread(mRunnable);
        if (!mRetrieveMediaInfoThread.isAlive())
        {
            mRetrieveMediaInfoThread.start();
        }
    }

    public static RetrieveInfoManager getInstance()
    {
        if (mInstance == null)
        {
            synchronized (RetrieveInfoManager.class )
            {
                if (mInstance == null)
                {
                    mInstance = new RetrieveInfoManager();
                }
            }
        }

        return mInstance;
    }

    public void addTask(LocalMediaInfo mediaInfo)
    {
        addTask(mediaInfo, 0, 0);
    }

    public void addTask(LocalMediaInfo mediaInfo, int width, int height)
    {
        addTask(mediaInfo, width, height, null);
    }

    public void addTask(LocalMediaInfo mediaInfo, int width, int height,
                        RetrieveCompleteListener retrieveCompleteListener)
    {
        if ((null == mediaInfo) || (null == mediaInfo.getUrl()))
        {
            return;
        }

        synchronized (mWaitingQueue)
        {
            RetrieveInfo info = new RetrieveInfo();
            info.info   = mediaInfo;
            info.width  = width;
            info.height = height;
            info.retrieveCompleteListener = retrieveCompleteListener;
            mWaitingQueue.add(info);
            mWaitingQueue.notifyAll();
        }
    }

    private RetrieveInfo getLastTask()
    {
        synchronized (mWaitingQueue)
        {
            RetrieveInfo media = null;
            if (mWaitingQueue.isEmpty())
            {
                try
                {
                    mWaitingQueue.wait();
                } catch (InterruptedException e) {
                    Log.d(TAG, "getLastTask get InterruptedException!");
                }
            }

            int len = mWaitingQueue.size();
            if (len > 0)
            {
                media = mWaitingQueue.get(len - 1);
                mWaitingQueue.clear();
            }

            return media;
        }
    }

    private Runnable mRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            while (!bIsStop)
            {
                RetrieveInfo retrieveInfo = getLastTask();
                if (retrieveInfo == null)
                {
                    break;
                }

                retrieveMediaInfo(retrieveInfo);
            }

            Log.d(TAG, "bIsStop = " + bIsStop);
        }
    };

    public void setRetrieveCompleteListener(RetrieveCompleteListener retrieveCompleteListener)
    {
        synchronized (mLock)
        {
            mRetrieveCompleteListener = retrieveCompleteListener;
        }
    }

    public void removeRetrieveCompleteListener()
    {
        synchronized (mLock)
        {
            mRetrieveCompleteListener = null;
        }
    }

    private void notifyComplete(RetrieveInfo retrieveInfo, SongInfo songInfo)
    {
        synchronized (mLock)
        {
            RetrieveCompleteListener listener = getRetrieveListener(retrieveInfo);
            if ((listener != null) && !bIsStop)
            {
                listener.onComplete(retrieveInfo.info, songInfo);
            }
        }
    }

    private void notifyComplete(RetrieveInfo retrieveInfo, Bitmap bitmap)
    {
        synchronized (mLock)
        {
            RetrieveCompleteListener listener = getRetrieveListener(retrieveInfo);
            if ((listener != null) && !bIsStop)
            {
                listener.onComplete(retrieveInfo.info, bitmap);
            }
        }
    }

    private RetrieveCompleteListener getRetrieveListener(RetrieveInfo retrieveInfo)
    {
        synchronized (mLock)
        {
            if ((retrieveInfo != null) && (retrieveInfo.retrieveCompleteListener != null))
            {
                return retrieveInfo.retrieveCompleteListener;
            }

            return mRetrieveCompleteListener;
        }
    }

    public void killSelf()
    {
        bIsStop = true;
        if (null != mRetrieveMediaInfoThread)
        {
            mRetrieveMediaInfoThread.interrupt();
            mRetrieveMediaInfoThread = null;
        }

        if (null != mRunnable)
        {
            removeRetrieveCompleteListener();
            mRunnable = null;
        }

        releaseRetrieve(retriever);
    }

    public void retrieveMediaInfo(RetrieveInfo retrieveInfo)
    {
        if (retrieveInfo == null)
        {
            return;
        }

        LocalMediaInfo info = retrieveInfo.info;
        int width  = retrieveInfo.width;
        int height = retrieveInfo.height;

        setDataSource(info);
        SongInfo songInfo = retrieveBasicInfo();
        if (StringUtils.isEmpty(songInfo.getDurnation()))
        {
        	//防止retriever获取的信息空掉！
        	releaseRetrieve(retriever);
        	retriever = new MediaMetadataRetriever();
        	setDataSource(info);
        	songInfo = retrieveBasicInfo();
        }
        
        if (INVALID_DUARTION.equals(songInfo.getDurnation()))
        {
        	songInfo.setDurnation(null);
        }
        notifyComplete(retrieveInfo, songInfo);

        if ((width > 0) && (height > 0))
        {
            Bitmap bitmap = null;
            if (info.getmFileType() == Constant.MediaType.AUDIO)
            {
                bitmap = retrieveAudioAlbum(width, height);
            }
            else if (info.getmFileType() == Constant.MediaType.VIDEO)
            {
                bitmap = retrieveVideoThumbnail(width, height);
            }

            notifyComplete(retrieveInfo, bitmap);
        }
    }

    public static synchronized SongInfo retrieve(LocalMediaInfo mediaInfo)
    {
        SongInfo songInfo = new SongInfo();

        return songInfo;
    }

    private void setDataSource(LocalMediaInfo mediaInfo)
    {
        String url = mediaInfo.getUrl();
        String NO_RANGE_FLAG = "?range=no";

        if (StringUtils.isEmpty(url))
        {
            return ;
        }

        try
        {
            if (url.contains(NO_RANGE_FLAG))
            {
                url = url.substring(0,
                                    url.lastIndexOf(NO_RANGE_FLAG))
                      + url.substring(url.lastIndexOf(NO_RANGE_FLAG) + NO_RANGE_FLAG.length());
            }

            if (StringUtils.isNetworkURI(url))
            {
                retriever.setDataSource(url, new HashMap <String, String>());
            }
            else
            {
                retriever.setDataSource(url);
            }
        } catch (Exception e) {
        	Log.e(TAG, "retrieve get  Exception!");
            return ;
        }
    }

    private void releaseRetrieve(MediaMetadataRetriever retriever)
    {
        if (retriever == null)
        {
            return;
        }

        try
        {
            Log.d(TAG, "releaseRetrieve retrieve release enter!");
            retriever.release();
            Log.d(TAG, "releaseRetrieve retrieve release ok!");
        } catch (RuntimeException ex) {
            Log.e(TAG, "releaseRetrieve retrieve release RuntimeException!");
        } catch (Exception e) {
            Log.e(TAG, "releaseRetrieve retrieve release Exception!");
        }
        finally
        {
            retriever = null;
        }
    }

    private SongInfo retrieveBasicInfo()
    {
        SongInfo songInfo = new SongInfo();

        if (retriever == null)
        {
            return songInfo;
        }

        String artistFromRetriever = null;
        String titleFromRetriever = null;
        String albumFromRetriever = null;
        String durationFromRetriever = null;

        durationFromRetriever =
            EncodeUtil.getRightEncodeString(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        artistFromRetriever =
            EncodeUtil.getRightEncodeString(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
        titleFromRetriever =
            EncodeUtil.getRightEncodeString(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
        albumFromRetriever =
            EncodeUtil.getRightEncodeString(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
        if (StringUtils.isNotEmpty(durationFromRetriever))
        {
            try
            {
                long duration = Long.valueOf(durationFromRetriever);
                if (duration < 0)
                {
                	durationFromRetriever = INVALID_DUARTION;
                }
                else
                {
                	durationFromRetriever = DateUtil.formatTime(duration);
                }                
            } catch (NumberFormatException e) {
                durationFromRetriever = null;
            }
        }
        else
        {
            durationFromRetriever = null;
        }

        if (StringUtils.isNotEmpty(artistFromRetriever))
        {
            artistFromRetriever = artistFromRetriever.trim();
        }
        else
        {
            artistFromRetriever = null;
        }

        if (StringUtils.isNotEmpty(titleFromRetriever))
        {
            titleFromRetriever = titleFromRetriever.trim();
        }
        else
        {
            titleFromRetriever = null;
        }

        if (StringUtils.isNotEmpty(albumFromRetriever))
        {
            albumFromRetriever = albumFromRetriever.trim();
        }
        else
        {
            albumFromRetriever = null;
        }

        songInfo.setAlbum(albumFromRetriever);
        songInfo.setArtist(artistFromRetriever);
        songInfo.setDurnation(durationFromRetriever);
        if (titleFromRetriever != null)
        {
            songInfo.setSongName(titleFromRetriever);
        }

        return songInfo;
    }

    private Bitmap retrieveAudioAlbum(int width, int height)
    {
        Log.d(TAG, " cc msg retrieveAudioAlbum enter!");
        if (retriever == null)
        {
            return null;
        }

        Bitmap bitmap = null;
        byte[] art = retriever.getEmbeddedPicture();
        if (art != null)
        {
            bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
        }

        if (bitmap != null)
        {
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        }

        Log.d(TAG, " cc msg retrieveAudioAlbum leave!");
        return bitmap;
    }

    private Bitmap retrieveVideoThumbnail(int width, int height)
    {
        Log.d(TAG, " cc msg retrieveVideoThumbnail enter!");
        if (retriever == null)
        {
            return null;
        }

        Bitmap bitmap = retriever.getFrameAtTime();
        if (bitmap != null)
        {
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        }

        Log.d(TAG, " cc msg retrieveVideoThumbnail leave!");
        return bitmap;
    }

    private class RetrieveInfo
    {
        public LocalMediaInfo info;
        public int width;
        public int height;
        public RetrieveCompleteListener retrieveCompleteListener;
    }
}
