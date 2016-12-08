package com.rockchips.mediacenter.imageplayer.image;

import android.content.Context;

import com.rockchips.mediacenter.mtd.download.IDownloadFile;

/***
 * 分享图片下载
 * 
 * 
 */
public class CloudShareImageDownloader implements IDownloadFile
{
    private boolean           isbStop    = false;

    private static final long TIME_OUT   = 3 * 1000 * 60;

    private static final long SLEEP_TIME = 100;

    private String            mData;

    private Context           mContext;

    private int               mState;

    private String            mediaName;

    private String            mSavePath  = null;

    public CloudShareImageDownloader(String url, Context context, String name)
    {
        super();
        setState(CloudShareImageDownloader.State.INIT);
        this.mContext = context;

        this.mData = url;

        this.mediaName = name;
    }

    /**
     * 
     *开始下载
     */
    @Override
    public void download()
    {
//        if (((ImagePlayerActivity) mContext).getCloudPreCache() == null)
//        {
//            return;
//        }
//        ((ImagePlayerActivity) mContext).getCloudPreCache().requestDownloadShareImage(mData);

        long start = System.currentTimeMillis();
        setState(CloudShareImageDownloader.State.DOWNLOADING);
        try
        {
            while (true)
            {

                Thread.sleep(SLEEP_TIME);

                long curtime = System.currentTimeMillis();

                if (curtime - start > TIME_OUT)
                {
                    setState(CloudShareImageDownloader.State.TIMEOUT);
                    break;
                }

                if (isStop())
                {
                    break;
                }

            }
        }

        catch (InterruptedException e)
        {
        }
        finally
        {
            if (getState() != CloudShareImageDownloader.State.TIMEOUT)
            {
                setState(CloudShareImageDownloader.State.IDLE);
            }

        }
    }

    @Override
    public void stop()
    {
        setbStop(true);
    }

    public boolean isStop()
    {
        return isbStop;
    }

    public void setbStop(boolean isbStop)
    {
        this.isbStop = isbStop;
    }

    public int getState()
    {
        return mState;
    }

    public void setState(int mState)
    {
        this.mState = mState;
    }

    public String getSavePath()
    {
        return mSavePath;
    }

    public void setSavePath(String mSavePath)
    {
        this.mSavePath = mSavePath;
    }

    public String getData()
    {
        return mData;
    }

    public static class State
    {

        public static int       INIT        = 0;

        public static final int IDLE        = 1;

        public static final int DOWNLOADING = 2;

        public static final int SUCCESS_OK  = 3;

        public static final int TIMEOUT     = 4;

        public static final int ERROR       = 5;
    }
}
