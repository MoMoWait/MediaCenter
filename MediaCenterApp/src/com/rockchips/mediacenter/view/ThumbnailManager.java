/**
 * com.rockchips.mediacenter.basicutils.widget
 * ThumbnailManager.java
 * Description: TODO<br>
 * @author w00190739
 * @version v1.0<br>
 * Date: 2014-7-15下午4:07:16<br> 
 * Copyright © Huawei Technologies Co., Ltd. 2014. All rights reserved.
 */
package com.rockchips.mediacenter.view;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;

import com.rockchips.mediacenter.bean.LocalMediaInfo;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.utils.BitmapUtil;
import com.rockchips.mediacenter.utils.FileUtil;
import com.rockchips.mediacenter.utils.IICLOG;
import com.rockchips.mediacenter.utils.StringUtils;
import com.rockchips.mediacenter.service.CacheManager;
/**
 * Description: TODO<br>
 * @author w00190739
 * @version v1.0 Date: 2014-7-15 下午4:07:16<br>
 */
public class ThumbnailManager
{
    private static final String TAG = "ThumbnailManager";

    private static final IICLOG Log = IICLOG.getInstance();

//    private static final int STATE_IDLE = 0;
//
//    private static final int STATE_BUSY = 1;
//
//    private static final int STATE_PAUSE = 2;
//
//    private int state = STATE_IDLE;

    // 并发真正瓶颈在磁盘写入，目前增加线程可抢占更多执行资源 TODO： 将写cache另外分开
    private int MAX_THUMB_THREAD = 3;

    private ExecutorService thumbService = Executors.newFixedThreadPool(MAX_THUMB_THREAD);

    private List<ThumbnailTask> taskList = new ArrayList<ThumbnailTask>();

    private ThumbnailTask doingTask = null;

    private Object doingTaskLock = new Object();

    // 控件请求缩略图的优先级get
    public static final int LEVEL_HIGH = 3;

    public static final int LEVEL_MID = 2;

    public static final int LEVEL_LOW = 1;

    private Handler mHandler = null;

    private HandlerThread mProcThread = null;

    /**
     * 
     * 缩略图更新监听事件
     * 
     * 2011-10-24 上午11:21:34
     * 
     * @version 1.0.0
     * 
     */
    public interface ThumbnailChangedListener
    {
        public int getLevel();

        public void onError();

        public void onFinished(LocalMediaInfo info, Bitmap thumbnail);

    }

    /**
     * 单例 <功能详细描述>
     * @return 单例的ThumbnailManager
     * @see [类、类#方法、类#成员]
     */
    private static ThumbnailManager mThumbnailManager = null;

    public static synchronized ThumbnailManager getInstance()
    {
        if (mThumbnailManager == null)
        {
            mThumbnailManager = new ThumbnailManager();
        }
        return mThumbnailManager;
    }

    private ThumbnailManager()
    {
        mProcThread = new HandlerThread("ThumbnailManager", Thread.MIN_PRIORITY);
        mProcThread.start();
        mHandler = new Handler(mProcThread.getLooper(), mProcCallback);

    }

    private static final int MSG_REQUEST_DOWNLOAD = 101; // 请求下载

    private static final int MSG_REQUEST_CANCEL = 102; // 取消下载

    private static final int MSG_REQUEST_CANCELALL = 103; // 取消所有

    private Handler.Callback mProcCallback = new Handler.Callback()
    {

        @Override
        public boolean handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case MSG_REQUEST_DOWNLOAD:
                {

                    if (msg.obj != null)
                    {
                        ThumbnailTask task = (ThumbnailTask) msg.obj;
                        // 从下载队列中删除
                        synchronized (taskList)
                        {
                            Iterator<ThumbnailTask> thumbIt = taskList.iterator();
                            while (thumbIt.hasNext())
                            {
                                ThumbnailTask tt = thumbIt.next();
                                if (tt.getChangedListener() == task.getChangedListener() 
                                        && tt.getDataInfo()== task.getDataInfo())
                                {
                                    Log.d(TAG, task.getDataInfo().getmFileName() + " in TaskList already ......");
                                    return true;
                                }
                                if (tt.getChangedListener() == task.getChangedListener())
                                {
                                    thumbIt.remove();
                                    Log.d(TAG, "remove same task........");
                                }
                            }
                            Log.d(TAG, "add one task........");
                            taskList.add(task);
                        }

                        startTask();
                    }
                    break;
                }
                case MSG_REQUEST_CANCEL:
                {
                    if (msg.obj != null)
                    {
                        ThumbnailChangedListener callback = (ThumbnailChangedListener) msg.obj;
                        // 在任务队列中删除
                        synchronized (taskList)
                        {
                            for (int i = 0; i < taskList.size(); i++)
                            {
                                if (taskList.get(i).getChangedListener() == callback)
                                {
                                    Log.d(TAG, "remove one task........");
                                    taskList.remove(i);
                                    break;
                                }
                            }
                        }

                        // FIXME:由于线程无法中止，正在执行的任务无法取消
                        // ？！
                        // 正在运行的任务取消
                        synchronized (doingTaskLock)
                        {
                            if (doingTask != null)
                            {
                                if (doingTask.getChangedListener() == callback)
                                {
                                    Log.d(TAG, "cancel doing task........");
                                    doingTask.setTaskState(ThumbnailTask.TASK_STATE_CANCELED);
                                }

                            }
                        }
                    }

                    break;
                }
                case MSG_REQUEST_CANCELALL:
                {
                    synchronized (taskList)
                    {
                        taskList.clear();
                    }
                    synchronized (doingTaskLock)
                    {
                        if (doingTask != null)
                        {
                            doingTask.setTaskState(ThumbnailTask.TASK_STATE_CANCELED);
                        }
                    }
                    break;
                }
                default:
                {
                    break;
                }
            }
            return true;
        }
    };

    /**
     * 
     * 异步方式请求缩略图
     * 
     * @param info
     * @param callback 异步方式更新，避免解析MP3文件、视频文件或者从网络下载的时间过长
     * @return void
     * @exception
     */

    public int requestThumbnail(LocalMediaInfo info, int width, int height, ThumbnailChangedListener callback)
    {
        ThumbnailTask task = new ThumbnailTask(info, callback);
        task.setBitmapSize(width, height);

        mHandler.sendMessage(Message.obtain(mHandler, MSG_REQUEST_DOWNLOAD, task));

        return task.getTaskId();
    }

    private Bitmap loadThumbnailFromCache(LocalMediaInfo info, int width, int height)
    {
        if (null == info)
        {
            Log.e(TAG, "loadThumbnailFromCache inputParam LocalMediaInfo is NULL");
            return null;
        }

        String url = null;
        switch (info.getmFileType())
        {
            case ConstData.MediaType.IMAGEFOLDER:
                // case ConstData.MediaType.AUDIOFOLDER:
                // case ConstData.MediaType.VIDEOFOLDER:
                url = getAlbumartUri(info);
                break;
            default:
                url = info.getUrl();
                break;
        }

        if (null == url)
        {
            return null;
        }
        Log.d(TAG, "----tag url---" + url);
        String cached_file_path = getCacheFilePath(url, width, height);

        File cachedThumbFile = new File(cached_file_path);

        if (null != cachedThumbFile && cachedThumbFile.exists())
        {
            Log.d(TAG, cached_file_path + " --> Exist");
            // it should be the cache path to read!!!!!!!!
            Bitmap dstBMP = BitmapUtil.createBitmapforListIcon(cached_file_path, width, height);
            return dstBMP;
        }

        Log.w(TAG, " ThumbNail :" + cached_file_path + " Not Exist");
        return null;
    }

    static public String getCacheFilePath(String url, int width, int height)
    {
        return CacheManager.getInstance().getCacheBitmapPath(url, width, height);
    }

    public void cancelRequestThumbnail(ThumbnailChangedListener callback)
    {
        mHandler.sendMessage(Message.obtain(mHandler, MSG_REQUEST_CANCEL, callback));
    }

    public void cancelAllRequest()
    {
        mHandler.sendMessage(Message.obtain(mHandler, MSG_REQUEST_CANCELALL));
    }

    /**
     * 根据要求创建缩略图 <功能详细描述>
     * @param url
     * @param opt
     * @param w
     * @param h
     * @return
     * @see [类、类#方法、类#成员]
     */
    public Bitmap CreateBitmap(String url, Options opt, int w, int h)
    {
        Log.d(TAG, "try create bitmap:w=" + w + ", h=" + h);

        Bitmap oldBitmap = CreateBitmapFromFile(url, opt, w, h);
        return oldBitmap;
    }

    public Bitmap CreateBitmapFromFile(String url, Options opt, int w, int h)
    {
        String local_filename = url;
        Bitmap bitmap = null;
        if (StringUtils.isNetworkURI(url))
        {
            bitmap = BitmapUtil.createBitmapFromNetwork(url, w, h);
        }
        else
        {
            bitmap = BitmapUtil.createBitmapforListIcon(local_filename, w, h);
        }

        return bitmap;

    }

    private synchronized void startTask()
    {
        thumbService.execute(mDownloadRunnable);
    }

    private Runnable mDownloadRunnable = new Runnable()
    {

        @Override
        public void run()
        {
            Log.d(TAG, "thumbnail thread run ....E :" + SystemClock.currentThreadTimeMillis());
            do
            {
                // 取一个任务执行
                ThumbnailTask needDoingTask = null;
                synchronized (taskList)
                {
                    if (taskList.size() > 0)
                    {
                        try
                        {
                            needDoingTask = taskList.remove(0);
                        }
                        catch (Exception e)
                        {
                            Log.e(TAG, "Exception : " + e.getLocalizedMessage());
                        }
                        if (needDoingTask == null)
                        {
                            return;
                        }
                        Log.i(TAG, Thread.currentThread().getId() + " fix queue taskList size " + taskList.size() + needDoingTask.getDataUri());
                        Log.i(TAG, Thread.currentThread().getId() + " fix queue taskList is doing "
                                + (needDoingTask.getTaskState() == ThumbnailTask.TASK_STATE_DOING));

                        needDoingTask.setTaskState(ThumbnailTask.TASK_STATE_DOING);
                    }
                    else
                    {
                        return;
                    }
                }

                LocalMediaInfo nowDoingTaskLocalMediaInfo = null;
                nowDoingTaskLocalMediaInfo = needDoingTask.getDataInfo();
                if (nowDoingTaskLocalMediaInfo != null)
                {
                    Log.d(TAG, "proc task.... FileName = " + nowDoingTaskLocalMediaInfo.getmFileName());

                    long startTime = SystemClock.currentThreadTimeMillis();
                    // Log.i(TAG, "-----> from Cache Start At : " +
                    // SystemClock.elapsedRealtime());

                    Bitmap thumbnail_bitmap = loadThumbnailFromCache(nowDoingTaskLocalMediaInfo, needDoingTask.getWidth(), needDoingTask.getHeight());

                    // Log.i(TAG, "-----> from Cache End At :" +
                    // SystemClock.elapsedRealtime());
                    Log.e(TAG, "x-----> from Cache Used  = " + (SystemClock.currentThreadTimeMillis() - startTime) + " ms");

                    if (thumbnail_bitmap == null)
                    {
                        Log.d(TAG, "load thumbnail from cache: failed!!! try download....");
                        int mediaType = nowDoingTaskLocalMediaInfo.getmFileType();
                        int devType = nowDoingTaskLocalMediaInfo.getmDeviceType();
                        final BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inScaled = false;
                        options.inPreferredConfig = Bitmap.Config.RGB_565;
                        options.inDither = true;

                        String downloadUri = null;

                        if (mediaType == ConstData.MediaType.IMAGE || mediaType == ConstData.MediaType.AUDIO || mediaType == ConstData.MediaType.VIDEO)
                        {
                            if (ConstData.DeviceType.isDLNADevice(devType))
                            {
                                downloadUri = nowDoingTaskLocalMediaInfo.getUrl();
                            }
                            else
                            {
                                downloadUri = nowDoingTaskLocalMediaInfo.getUrl();
                            }
                        }
                        else if (mediaType == ConstData.MediaType.IMAGEFOLDER || mediaType == ConstData.MediaType.FOLDER)
                        {
                            if (ConstData.DeviceType.isDLNADevice(devType))
                            {
                                downloadUri = nowDoingTaskLocalMediaInfo.getUrl();
                            }
                            else
                            {
                                downloadUri = getAlbumartUri(nowDoingTaskLocalMediaInfo);
                            }
                        }
                        Log.d(TAG, "thumbnail downloadUri = " + downloadUri);
                        Log.d(TAG, "load startTime = " + System.currentTimeMillis());
                        if (downloadUri != null && !StringUtils.isDropBoxURL(downloadUri))
                        {
                            switch (mediaType)
                            {
                                case ConstData.MediaType.IMAGE:
                                case ConstData.MediaType.IMAGEFOLDER:
                                    // Log.i(TAG, "=====> from File Start At :"
                                    // + SystemClock.elapsedRealtime());
                                    startTime = SystemClock.currentThreadTimeMillis();

                                    Log.d(TAG, "try parse locale Image thumb");
                                    thumbnail_bitmap = CreateBitmapFromFile(downloadUri, options, needDoingTask.getWidth(), needDoingTask.getHeight());

                                    Log.d(TAG, "parse locale Image thumb ok:");

                                    // Log.i(TAG, "=====>  from File End At :" +
                                    // SystemClock.elapsedRealtime());
                                    Log.e(TAG, "x-----> from File Used  = " + (SystemClock.currentThreadTimeMillis() - startTime) + " ms");
                                    break;
                                //                                case ConstData.MediaType.AUDIO:
                                //                                    thumbnail_bitmap = createAlbumThumbnail(downloadUri, needDoingTask.getWidth(), needDoingTask.getHeight());
                                //                                    break;
                                //                                case ConstData.MediaType.VIDEO:
                                //                                    thumbnail_bitmap = createVideoThumbnail(downloadUri, needDoingTask.getWidth(), needDoingTask.getHeight());
                                //                                    break;
                                default:
                                    Log.d(TAG, "not valid  mediaType .........");
                                    break;
                            }
                        }
                        else
                        {
                            Log.d(TAG, "invalid url .........");
                        }

                        // 保存缓存
                        if (thumbnail_bitmap != null)
                        {

                            // 保存到cache中
                            if (needDoingTask.getDataUri() != null)
                            {
                                if (CacheManager.getInstance().checkCacheable())
                                {
                                    String local_cache_path = getCacheFilePath(needDoingTask.getDataUri(), needDoingTask.getWidth(),
                                            needDoingTask.getHeight());

                                Log.d(TAG, "try save to cached....local_cache_path = " + local_cache_path);
                                if (BitmapUtil.saveBitmap(thumbnail_bitmap, local_cache_path))
                                {
                                    Log.d(TAG, "save bitmap  ok:" + local_cache_path);
                                }
                                else
                                {
                                    Log.d(TAG, "save bitmap  failed:" + local_cache_path);
                                }

                                }
                                else
                                {
                                    // do clear cache and drop this thumb
                                    Log.d(TAG, "save bitmap  failed for the cache is full");
                                    CacheManager.getInstance().doCacheClear();
                                }
                            }
                        }
                    }
                    else
                    {
                        Log.d(TAG, "load thumbnail from cache: Ok !!!");
                    }
                    Log.d(TAG, "begin callback....");
                    // 先通知界面刷新,通知后，thumbnail_bitmap 不能继续用
                    if (needDoingTask.getTaskState() != ThumbnailTask.TASK_STATE_CANCELED)
                    {
                        if (needDoingTask.getChangedListener() != null)
                        {
                            if (thumbnail_bitmap != null)
                            {
                                needDoingTask.getChangedListener().onFinished(nowDoingTaskLocalMediaInfo, thumbnail_bitmap);
                            }
                            else
                            {
                                needDoingTask.getChangedListener().onError();
                            }
                        }
                    }
                    Log.d(TAG, "end callback....");

                    thumbnail_bitmap = null;
                }
                // }
                // Thread.yield(); // 让出CPU让主线程处理一下
                Log.d(TAG, "begin other callback....");
                /*
                 * TO BE DONE : cancle out side
                 */
                synchronized (taskList)
                {
                    Log.i(TAG, "fix queue taskList size " + taskList.size());
                    List<ThumbnailTask> removeList = new ArrayList<ThumbnailTask>();
                    for (int i = 0; i < taskList.size(); i++)
                    {
                        ThumbnailTask task = taskList.get(i);
                        if (task.getTaskState() != ThumbnailTask.TASK_STATE_DOING)
                        {
                            if (task.getDataInfo() == nowDoingTaskLocalMediaInfo)
                            {
                                Log.i(TAG, "the same request");
                                removeList.add(task);
                            }
                        }
                    }
                    if (!removeList.isEmpty())
                    {
                        Log.i(TAG, "fix queue remove " + removeList.size() + " tasks.");
                        taskList.removeAll(removeList);
                        removeList.clear();
                        removeList = null;
                    }
                }
                /**/
                Log.d(TAG, "end other callback....  ");

            }
            while (true);

        }
    };

    private class ThumbnailTask
    {
        // 当前任务的状态
        protected static final int TASK_STATE_NONE = 0; // 任务未开始

        protected static final int TASK_STATE_FINISHED = 1; // 任务已完成

        protected static final int TASK_STATE_DOING = 2; // 任务执行中

        protected static final int TASK_STATE_CANCELED = 3; // 任务取消

        private LocalMediaInfo dataInfo;

        private ThumbnailChangedListener changedListener;

        private int taskState;

        private int width = -1;

        private int height = -1;

        public ThumbnailTask(LocalMediaInfo info, ThumbnailChangedListener callback)
        {
            dataInfo = info;
            changedListener = callback;

            taskState = TASK_STATE_NONE;

        }

        public int getTaskId()
        {
            return this.hashCode();
        }

        public LocalMediaInfo getDataInfo()
        {
            return dataInfo;
        }

        public int getTaskState()
        {
            return taskState;
        }

        public void setTaskState(int state)
        {
            taskState = state;
        }

        public ThumbnailChangedListener getChangedListener()
        {
            return changedListener;
        }

        public void setBitmapSize(int w, int h)
        {
            width = w;
            height = h;
        }

        public int getWidth()
        {
            return width;
        }

        public int getHeight()
        {
            return height;
        }

        public String getDataUri()
        {
            String uri = null;
            if (dataInfo != null)
            {
                uri = dataInfo.getUrl();
            }
            Log.d(TAG, "uri = " + uri);
            return uri;
        }
    }

    private String getAlbumartUri(LocalMediaInfo folderInfo)
    {
        String uri = null;
        if (folderInfo != null)
        {
            String path = String.valueOf(new StringBuffer().append(folderInfo.getmParentPath()).append(File.separator)
                    .append(folderInfo.getmFileName()));
            uri = FileUtil.getFirstFilePath(path);
            Log.d(TAG, "uri = " + uri);
        }

        return uri;
    }

    private Bitmap[] defaultBitmap_120x106 = null;

    public Bitmap getDefaultThumbnail_120x106(Context context, int hashCode)
    {
        return defaultBitmap_120x106[Math.abs(hashCode) % defaultBitmap_120x106.length];
    }

    private Bitmap[] defaultBitmap_170x150 = null;

    public Bitmap getDefaultThumbnail_170x150(Context context, int hashCode)
    {
        return defaultBitmap_170x150[Math.abs(hashCode) % defaultBitmap_170x150.length];
    }

    public Bitmap getDefaultThumbnail_150x150(Context context, int hashCode)
    {
        return getDefaultThumbnail_300x300(context, hashCode);
    }

    private Bitmap[] defaultBitmap_260x146 = null;

    public Bitmap getDefaultThumbnail_260x146(Context context, int hashCode)
    {
        return defaultBitmap_260x146[Math.abs(hashCode) % defaultBitmap_260x146.length];
    }

    public Bitmap getDefaultThumbnail_170x170(Context context, int hashCode)
    {
        return getDefaultThumbnail_300x300(context, hashCode);
    }

    private Bitmap[] defaultBitmap_300x300 = null;

    public Bitmap getDefaultThumbnail_300x300(Context context, int hashCode)
    {
        return defaultBitmap_300x300[Math.abs(hashCode) % defaultBitmap_300x300.length];
    }

    public Bitmap getDefaultThumbnail_350x330(Context context, int hashCode)
    {
        return getDefaultThumbnail_300x300(context, hashCode);
    }

    private Bitmap[] defaultBitmap_Folder = null;

    /*
     * folderType: 0: 空目录， 1： 子目录， 2：子文件
     */
    public static final int FOLDER_ICONTYPE_EMPTY = 0;

    public static final int FOLDER_ICONTYPE_SUBFOLDER = 1;

    public static final int FOLDER_ICONTYPE_SUBFILE = 2;

    public static final int FOLDER_ICONTYPE_USB = 3;

    public static final int FOLDER_ICONTYPE_DLNA = 4;

    public static final int FOLDER_ICONTYPE_DATE = 5;

    public static final int FOLDER_ICONTYPE_CELLPHONE = 6;

    public static final int FOLDER_ICONTYPE_PAD = 7;

    public static final int FOLDER_ICONTYPE_STB = 8;

    public static final int FOLDER_ICONTYPE_Shared = 9;

    public Bitmap getDefaultFolder(Context context, int folderType)
    {
        return defaultBitmap_Folder[folderType];
    }

    private Bitmap mImageDefaultThumb = null;

    public Bitmap getImageDefaultThumb(Context context)
    {
        return mImageDefaultThumb;
    }

    private Bitmap mAudioDefaultThumb = null;

    public Bitmap getAudioDefaultThumb(Context context)
    {
        return mAudioDefaultThumb;
    }

    private Bitmap mVideoDefaultThumb = null;

    public Bitmap getVideoDefaultThumb(Context context)
    {
        return mVideoDefaultThumb;
    }

    private Bitmap mVideoDefaultThumb_260x146 = null;

    public Bitmap getVideoDefaultThumb260x146(Context context)
    {
        return mVideoDefaultThumb_260x146;
    }

    private Bitmap mImageDefaultThumbInfolder = null;

    public Bitmap getImageDefaultThumbInfolder(Context context)
    {
        return mImageDefaultThumbInfolder;
    }

    private Bitmap mAudioDefaultThumbInfolder = null;

    public Bitmap getAudioDefaultThumbInfolder(Context context)
    {
        return mAudioDefaultThumbInfolder;
    }

    private Bitmap mVideoDefaultThumbInfolder = null;

    public Bitmap getVideoDefaultThumbInfolder(Context context)
    {
        return mVideoDefaultThumbInfolder;
    }
}
