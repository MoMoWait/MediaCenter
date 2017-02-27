package com.rockchips.mediacenter.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import android.os.SystemClock;
import android.util.Log;

/**
 * 
 * 分片下载 按照传入的 url、范围下载，可以通过api stopWhenPossible停止下载
 * 
 * @author t00181037
 * @version [版本号, 2013-6-13]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class RangeDownloader
{
    private URL downloadUrl;

    private long startByteIdx;

    private long endByteIdx;

    private RangeDownloadListener listener = null;

    public File getDestFile()
    {
        return destFile;
    }

    public URL getDownloadUrl()
    {
        return downloadUrl;
    }

    public long getStartByteIdx()
    {
        return startByteIdx;
    }

    public long getEndByteIdx()
    {
        return endByteIdx;
    }

    public boolean isStopped()
    {
        return stopped;
    }

    private File destFile;

    private boolean stopped;

    private int connTimeOut = 90000;

    private int readTimeOut = 90000;

    public RangeDownloader(URL downloadUrl, File destFile, long startByteIdx, long endByteIdx, int connTimeOut, int readTimeOut)
    {
        this.downloadUrl = downloadUrl;
        this.startByteIdx = startByteIdx;
        this.endByteIdx = endByteIdx;
        this.destFile = destFile;
        this.connTimeOut = connTimeOut;
        this.readTimeOut = readTimeOut;
    }

    public void stopWhenPossible()
    {
        stopped = true;
        destFile.deleteOnExit();
    }

    public static final int BUFFER_SIZE = 500;

    public static final long UNBOUNDED = -1;

    private int bytesDownloaded;

    public int getBytesDownloaded()
    {
        return bytesDownloaded;
    }

    public void download()
    {

        int iretrytime = 200;
        destFile.delete();
        long startIndexTmp = startByteIdx;
        do
        {

            URLConnection connection = null;
            InputStream inStream = null;

            startIndexTmp = startByteIdx + bytesDownloaded;
            Log.d("TSS", "start:" + startIndexTmp);

            try
            {
                RandomAccessFile raf = null;
                try
                {
                    Log.d("TSS", "download 1-1");
                    Log.d("TSS", "download 1-2");
                    // outStream = new FileOutputStream(destFile);

                    Log.d("TSS", "download 1-3");
                    connection = downloadUrl.openConnection();
                    Log.d("TSS", "download 1-4");
                    String endStr = endByteIdx == UNBOUNDED ? "" : String.valueOf(endByteIdx);
                    Log.d("TSS", "download 1-5");
                    connection.setRequestProperty("Range", "bytes=" + startIndexTmp + "-" + endStr);
                    Log.d("TSS", "download 1-6");
                    ((HttpURLConnection) connection).setRequestMethod("GET");
                    connection.setDoInput(true);
                    Log.d("TSS", "download 1-7");
                    connection.setDoOutput(false);
                    Log.d("TSS", "download 1-8");
                    connection.setConnectTimeout(connTimeOut);
                    Log.d("TSS", "download 1-9");
                    connection.setReadTimeout(readTimeOut);

                    Log.d("TSS", "B connection.connect()");
                    connection.connect();

                    Log.d("TSS", "E connection.connect()");

                    int iRetCode = ((HttpURLConnection) connection).getResponseCode();
                    if (iRetCode != 206 && iRetCode != 200)
                    {// 错误
                        Log.d("TSS", "E getResponseCode:" + iRetCode);
                        break;
                    }
                    InputStream coonIn = null;
                    try
                    {
                        coonIn = connection.getInputStream();
                        inStream = new BufferedInputStream(coonIn, BUFFER_SIZE);

                        Log.d("TSS", " download 1-10");
                        int bytesRead;
                        byte[] buffer = new byte[BUFFER_SIZE];

                        raf = new RandomAccessFile(destFile, "rw");

                        raf.seek(bytesDownloaded);

                        while ((bytesRead = inStream.read(buffer, 0, BUFFER_SIZE)) > 0)
                        {

                            if (stopped)
                            {
                                if (getListener() != null)
                                {
                                    getListener().onError(RangeDownloadListener.RDC_ERRCODE_MANUAL_STOPED, "manual stoped");
                                }

                                iretrytime = 0;
                                break;
                            }

                            bytesDownloaded += bytesRead;
                            raf.write(buffer, 0, bytesRead);
                        }

                    }
                    catch (IOException e)
                    {
                        break;
                    }
                    finally
                    {
                        if (coonIn != null)
                        {
                            try
                            {
                                coonIn.close();
                            }
                            catch (IOException e2)
                            {
                            }
                            coonIn = null;
                        }
                    }

                }
                finally
                {
                    Log.d("TSS", " download 2");

                    if (null != raf)
                    {
                        try
                        {
                            raf.close();
                        }
                        catch (IOException e2)
                        {
                        }
                        raf = null;
                    }

                    if (inStream != null)
                    {
                        try
                        {
                            inStream.close();
                        }
                        catch (IOException e)
                        {
                        }
                        inStream = null;
                    }
                }

                if (stopped)
                {
                    Log.e("TSS", "download 4:" + destFile.getAbsolutePath() + " tempDir.exists():" + destFile.exists());
                    int t = 1;
                    while (!destFile.delete() && t < 3)
                    {
                        t++;
                        SystemClock.sleep(10);

                        Log.e("TSS", "download 5");
                    }

                }
                if (getListener() != null)
                {
                    getListener().onComplete(getDownloadUrl().getPath(), getDestFile().getAbsolutePath(), this.startByteIdx, this.endByteIdx);
                }

            }
            catch (IOException ioex)
            {
                if (ioex.getMessage() != null && ioex.getMessage().contains("unexpected end of stream"))
                {// 重新下载
                    if (destFile.length() == endByteIdx - startByteIdx + 1)
                    {
                        Log.d("TSS", " destFile.length():" + destFile.length() + " == endByteIdx-startByteIdx + 1: "
                                + (endByteIdx - startByteIdx + 1));
                    }
                    else
                    {
                        Log.d("TSS", "retry destFile.length():" + destFile.length() + " != endByteIdx-startByteIdx + 1: "
                                + (endByteIdx - startByteIdx + 1));
                        Log.d("TSS", "startIndexTmp:" + startIndexTmp + "  bytesDownloaded:" + bytesDownloaded);

                        continue;
                    }

                }
                else
                {
                    if (getListener() != null)
                    {
                        getListener().onError(RangeDownloadListener.RDC_ERRCODE_EXCEPTION, ioex.getMessage());
                    }
                }
            }
            catch (Exception e)
            {
                Log.d("TSS", " download 6 EX:" + e.getMessage());

                if (getListener() != null)
                {
                    getListener().onError(RangeDownloadListener.RDC_ERRCODE_EXCEPTION, e.getMessage());
                }

                // throw new RuntimeException(e);
            }

            // 下载失败需要回调回去
            if (destFile.length() == endByteIdx - startByteIdx + 1)
            {

            }
            else
            {
                if (getListener() != null)
                {
                    getListener().onError(RangeDownloadListener.RDC_ERRCODE_EXCEPTION, "file download failed: lack some bytes");
                }
                Log.d("TSS", "bytesDownloaded:" + bytesDownloaded);
                //
                // continue;
            }

            break;

        }
        while (iretrytime-- > 0);

    }

    public RangeDownloadListener getListener()
    {
        return listener;
    }

    public void setListener(RangeDownloadListener listener)
    {
        this.listener = listener;
    }
}
