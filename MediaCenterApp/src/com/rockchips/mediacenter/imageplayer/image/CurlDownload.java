package com.rockchips.mediacenter.imageplayer.image;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.rockchips.mediacenter.utils.IICLOG;
import com.rockchips.mediacenter.imageplayer.DLNAImageSwitcherViewFactory;

/**
 * curl下载包装 1、new CurlDownload 2、syncdownload 3、canceldownload
 * 
 * 【很重要】syncdownload是阻塞的
 * 
 * @author t00181037
 * 
 */
public class CurlDownload
{

    private static final String TAG = "MediaCenterApp";
    
    private IICLOG mLog = IICLOG.getInstance();

    private String mCmd[] = null;

    private String mRemoteUrl = null;

    private String mLocalSavePath = null;

    private Process mProc = null;

    // curl返回码
    public static int mCurlCode = -1;

    public static DLNAImageSwitcherViewFactory dlnaImageSwitcherViewFactory;

    /**
     * <使用的时候:需要在XML中加入网络访问权限>
     * 
     * @param url :远端地址
     * @param localSavePath : 本地URL
     */
    public CurlDownload(String url, String localSavePath)
    {
        // TODO ... 入口预判
        mLog.d(TAG, "CurlDownload--->net url--------->" + url);
        mLog.d(TAG, "CurlDownload--->localSavePath url--------->" + localSavePath);

        mCmd = new String[]
        { "/system/bin/axel", "-a", "-n", "8", "-o", localSavePath, url };
        mRemoteUrl = url;
        mLocalSavePath = localSavePath;
    }

    /**
     * 同步阻塞下载 建议用户在使用的时候，放在一个线程中，避免阻塞
     * 
     * @return
     */
    public boolean syncdownload()
    {

        Runtime runtime = Runtime.getRuntime();
        mLog.d(TAG, "syncdownload ------->1");
        int exitValue = -1;
        try
        {
            mLog.d(TAG, "syncdownload ------->2");
            if (dlnaImageSwitcherViewFactory != null)
            {
                dlnaImageSwitcherViewFactory.displayProgreeBar();
            }
            mProc = runtime.exec(mCmd);
            System.exit(2);
            mLog.d(TAG, "syncdownload ------->3");
            mLog.d(TAG, "Start to Excute EXE:" + mCmd[0] + " data:" + mRemoteUrl + " - localpath:" + mLocalSavePath + " proc:" + mProc);

            cleanProcessBuffer(mProc);
            exitValue = mProc.waitFor();
            mProc.destroy();
            mCurlCode = exitValue;
            mLog.d(TAG, "syncdownload ------->4");
            mLog.d(TAG, "End to Excute EXE:" + mCmd[0] + " data:" + mRemoteUrl + " - localpath:" + mLocalSavePath + " proc:" + mProc);
        }
        catch (IOException e)
        {
            mLog.d(TAG, "syncdownload IOException:" + e.getMessage());
            mProc = null;
            return false;
        }
        catch (InterruptedException e)
        {
            mLog.d(TAG, "syncdownload Interrupted:" + e.getMessage());
            mProc = null;
            return false;
        }
        catch (NullPointerException e)
        {
            mLog.d(TAG, "syncdownload NullPointerException:" + e.getMessage());
            mProc = null;
            return false;
        }

        if (exitValue != 0)
        {
            mLog.d(TAG, "syncdownload exit code:" + exitValue);
            mLog.d(TAG, "syncdownload pic ------>fail");
            deleteCacheImage();
            mProc = null;
            return false;
        }
        else
        {
            mLog.d(TAG, "syncdownload exit code:" + exitValue);
            mLog.d(TAG, "syncdownload pic ------>success");
            mProc = null;
            return true;
        }
    }

    /**
     * cancel download curl
     */
    public void cancel()
    {
        try
        {
            if (mProc != null)
            {
                mLog.d(TAG, "mProc.destroy()---->");
                mLog.d(TAG, "Thread.sleep(1000)---->");
                Thread.sleep(1000);
                mProc.destroy();
                mProc = null;
                deleteCacheImage();
            }
        }
        catch (Exception ex)
        {
        }
    }

    // 删除缓存图片
    private synchronized void deleteCacheImage()
    {
        if (mLocalSavePath != null)
        {
            File file = new File(mLocalSavePath);
            if (file.exists())
            {
                mLog.d(TAG, "delete curl dowad cache file---->");
                file.delete();
            }
        }
    }

    /**
     * 读取 piple缓存区域 进程的输出
     */
    private void cleanProcessBuffer(final Process proc)
    {
        new Thread()
        {
            public void run()
            {
                final InputStream errorStream = mProc.getErrorStream();
                final InputStream inputStream = mProc.getInputStream();
                readErrorStream(proc, errorStream);
                readInputStream(proc, inputStream);
                try
                {
                    errorStream.close();
                    inputStream.close();
                }
                catch (IOException e)
                {
                }
            }
        }.start();
    }

    /**
     * 读取错误流
     */
    protected void readErrorStream(Process proc, InputStream errorStream)
    {
        if (mProc == null)
        {
            return;
        }
        mLog.d(TAG, "cleanProcessBuffer ------getErrorStream()->start");
        mLog.d(TAG, "cleanProcessBuffer getErrorStream------->1");
        BufferedReader br = new BufferedReader(new InputStreamReader(errorStream));
        mLog.d(TAG, "cleanProcessBuffer getErrorStream------->2");
        if (dlnaImageSwitcherViewFactory != null)
        {
            dlnaImageSwitcherViewFactory.displayProgreeBar();
        }
        mLog.d(TAG, "cleanProcessBuffer getErrorStream------->3");
        try
        {
            String lineC = null;
            mLog.d(TAG, "cleanProcessBuffer getErrorStream------->4");
            while ((lineC = br.readLine()) != null)
            {
                if (lineC != null)
                    mLog.d(TAG, proc + "-ERROR OUTPUT-" + lineC);
            }
        }
        catch (Exception e)
        {
        }

        try
        {
            br.close();
        }
        catch (Exception e)
        {
        }
        mLog.d(TAG, "cleanProcessBuffer getErrorStream------->end");
    }

    /**
     * 读取输入流
     */
    protected void readInputStream(Process proc, InputStream inputStream)
    {
        if (mProc == null)
        {
            return;
        }
        mLog.d(TAG, "cleanProcessBuffer ------getInputStream()->start");
        mLog.d(TAG, "cleanProcessBuffer getInputStream------->2");
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        mLog.d(TAG, "cleanProcessBuffer getInputStream------->3");
        mLog.d(TAG, "dlnaImageSwitcherViewFactory------->" + dlnaImageSwitcherViewFactory);
        if (dlnaImageSwitcherViewFactory != null)
        {
            dlnaImageSwitcherViewFactory.displayProgreeBar();
        }
        try
        {
            String lineB = null;
            while ((lineB = br.readLine()) != null)
            {
                if (lineB != null)
                {
                    mLog.d(TAG, proc + "-NORMAL OUTPUT-" + lineB);
                }

            }
        }
        catch (Exception e)
        {
        }
        mLog.d(TAG, "cleanProcessBuffer getInputStream------->end");
        try
        {
            br.close();
        }
        catch (Exception e)
        {
        }
    }

    public interface CurlDownloadListenerInterface
    {
        public void showProgressBar();

        public void hideProgressBar();
    }

}
