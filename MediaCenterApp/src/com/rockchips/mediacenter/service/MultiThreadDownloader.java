package com.rockchips.mediacenter.service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import android.os.SystemClock;
import android.util.Log;

import com.rockchips.mediacenter.utils.FileUtil;
import com.rockchips.mediacenter.service.HttpUtils;
import com.rockchips.mediacenter.utils.MtdFileUtil;
import com.rockchips.mediacenter.service.HttpUtils.HttpDetails;
/**
 * 
 * 多线程下载 <功能详细描述>
 * 
 * @code reviewer t00181037
 * @version [版本号, 2013-6-13]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class MultiThreadDownloader implements IDownloadFile
{
    public static final String TAG = "TSS";

    private int chunkSize = 1000 * 1000;

    private int executorPoolSize = 5;

    private URL downloadUrl;

    private File destFile;

    private ExecutorService downloadExecutor;

    private MultiDownloadListener mdl = null;

    private int mConnTimeout = 30000;

    private int mReadTimeout = 50000;

    private SecureRandom mSecureRandom = new SecureRandom();

    public int getChunkSize()
    {
        return chunkSize;
    }

    public MultiThreadDownloader(URL downloadUrl, File destFile)
    {
        this.setDownloadUrl(downloadUrl);
        this.setDestFile(destFile);
    }

    private boolean chunkSizeSetExplicitly;

    public void setChunkSize(int chunkSize)
    {
        this.chunkSize = chunkSize;
        chunkSizeSetExplicitly = true;
    }

    public int getExecutorPoolSize()
    {
        return executorPoolSize;
    }

    public void setExecutorPoolSize(int executorPoolSize)
    {
        this.executorPoolSize = executorPoolSize;
    }

    /**
     * 计算块数 根据文件总长度(contentLength)，以及块尺寸计算出应该用多少块下载
     * @param contentLength 文件总长度 bytes
     * @param chunkSize 块的大小 bytes
     * @return
     * @see [类、类#方法、类#成员]
     */
    public static int countChunks(int contentLength, int chunkSize)
    {
        int chunks = contentLength / chunkSize;
        if (chunks == 0)
        {
            chunks = 1;
        }
        else
        {
            if (contentLength % chunkSize != 0)
            {
                chunks++;
            }
        }
        return chunks;
    }

    /**
     * 计算块的尺寸 通过下载线程个数（execuorPoolSize)总数同时运行下载这个文件(长度contentLength)，每个线程需要下载的块的大小
     * @param contentLength
     * @param executorPoolSize
     * @return
     * @see [类、类#方法、类#成员]
     */
    public static int calculateChunkSize(int contentLength, int executorPoolSize)
    {
        if (executorPoolSize >= contentLength)
        {
            return 1;
        }

        int chunkSize = contentLength / executorPoolSize;

        if (contentLength % executorPoolSize != 0)
        {
            chunkSize++;
        }

        return chunkSize;
    }

    static String className = MultiThreadDownloader.class.getName();

    static Logger log = Logger.getLogger(className);

    private List<RangeDownloader> downloaders;

    public void stop()
    {
        if (downloadExecutor != null)
        {
            downloadExecutor.shutdown();
        }

        if (downloaders != null)
        {
            for (RangeDownloader dl : downloaders)
            {
                dl.stopWhenPossible();
            }

        }

        if (tempDir != null)
        {
            Log.e("TSS", "tempDir :" + tempDir.getAbsolutePath() + " tempDir.exists():" + tempDir.exists());

            int t = 1;
            while (!FileUtil.deleteFile(tempDir) && t < 3)
            {
                t++;
                SystemClock.sleep(10);

                Log.e("TSS", "stop ");
            }
        }
    }

    private File tempDir;

    private int maxTimeToDownloadSeconds = 60 * 60 * 24 * 30;

    public int getMaxTimeToDownloadSeconds()
    {
        return maxTimeToDownloadSeconds;
    }

    public void setMaxTimeToDownloadSeconds(int maxTimeToDownloadSeconds)
    {
        this.maxTimeToDownloadSeconds = maxTimeToDownloadSeconds;
    }

    private int contentLength;

    private String mTempDir = "";

    private int errCode;

    /**
     * 执行下载任务 分片下载,注意：这是一个同步方法
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws IOException
     * @see [类、类#方法、类#成员]
     */
    private void downloadInner() throws ExecutionException, InterruptedException, IOException
    {
        downloadExecutor = Executors.newFixedThreadPool(executorPoolSize);

        HttpDetails details = HttpUtils.getHttpResourceDetails(getDownloadUrl(), mConnTimeout, mReadTimeout);
        contentLength = details.contentLength;

        Log.e("TSS", "contentLength:" + contentLength);

        if (!chunkSizeSetExplicitly)
        {
            chunkSize = calculateChunkSize(details.contentLength, executorPoolSize);
            Log.e("TSS", "chunkSize:" + chunkSize + " executorPoolSize:" + executorPoolSize);
        }
        Log.e("TSS", "2 chunkSize:" + chunkSize + " executorPoolSize:" + executorPoolSize);

        int totalChunks = countChunks(details.contentLength, chunkSize);

        Log.e("TSS", "3 chunkSize:" + chunkSize + " executorPoolSize:" + executorPoolSize);
        // 206代表断点续传，200服务器成功返回网页（非断点续传，qq不支持断点续传）
        if (details.responseCode == 200)
        {
            Log.d("TSS", "Server does not support byte ranges:" + details.acceptRanges + " Falling back to a single-thread download process.");
            totalChunks = 1;
        }

        // added by c00226539（dropbox不支持多线程下载）
        if (getDownloadUrl().getHost().equals("dl.dropboxusercontent.com"))
        {
            Log.d("TSS", "Server does not support byte ranges:" + details.acceptRanges + " Falling back to a single-thread download process.");
            totalChunks = 1;
        }

        
        tempDir = new File(getTempDir(), "mt-download-" + Math.round(mSecureRandom.nextDouble() * 1000000));
        FileUtil.deleteFile(tempDir);
        tempDir.mkdirs();

        Collection<Future<?>> futures = new LinkedList<Future<?>>();
        downloaders = new ArrayList<RangeDownloader>();
        for (int i = 0; i < totalChunks; i++)
        {
            RangeDownloader rd = prepareRangeDownloader(tempDir, i, totalChunks, details);

            // tss add
            rd.setListener(mRDListerner);

            downloaders.add(rd);
            futures.add(downloadExecutor.submit(new RunnableTask(rd)));
        }

        try
        {
            long start = System.currentTimeMillis();
            long timeout = maxTimeToDownloadSeconds * 1000L;
            for (Future<?> future : futures)
            {
                long curr = System.currentTimeMillis();
                long diff = curr - start;
                long timeoutForThisFuture = timeout - diff;
                future.get(timeoutForThisFuture, TimeUnit.MILLISECONDS);

                // future.cancel(arg0);
            }

        }
        catch (TimeoutException e)
        {
            throw new RuntimeException("Timed out waiting for download tasks.", e);
        }

    }

    /**
     * 下载+合并文件
     * 
     * 这是一个同步方法，只有下载完毕且把文件合并完毕后才退出
     * 
     * @see [类、类#方法、类#成员]
     */
    public void download()
    {
        try
        {
            /**
             * 下载
             */

            Log.d("TSS", "MT download 1");
            downloadInner();
            Log.d("TSS", "MT download 2");

            /**
             * 拼接
             */
            combineDownloadParts();
            Log.d("TSS", "MT download 3");
        }
        catch (Exception e)
        {
            Log.d("TSS", "MT download 4");
            if (getMListener() != null)
            {
                getMListener().onError(this, MultiDownloadListener.MDC_ERRCODE_EXCEPTION, e);
            }
            Log.d("TSS", "MT download 5");
            stop();
            Log.d("TSS", "MT download 6");
        }
    }

    private void combineDownloadParts() throws IOException
    {

        if (downloaders != null && downloaders.size() > 0)
        {
            List<File> parts = new ArrayList<File>();
            for (RangeDownloader dl : downloaders)
            {
                parts.add(dl.getDestFile());
            }

            MtdFileUtil.combineFilePartsAndDelete(getDestFile(), parts);
        }

        FileUtil.deleteFile(tempDir);
    }

    class RunnableTask implements Runnable
    {
        RangeDownloader rd;

        public RunnableTask(RangeDownloader rd)
        {
            this.rd = rd;
        }

        public void run()
        {
            Log.d(TAG, String.format("Starting download for range [%s, %s], url: %s.", rd.getStartByteIdx(), rd.getEndByteIdx(), rd.getDownloadUrl()));
            try
            {
                rd.download();
                Log.d(TAG, String.format("Completed download for range [%s, %s], url: %s. \n Dest file: %s.", rd.getStartByteIdx(),
                        rd.getEndByteIdx(), rd.getDownloadUrl(), rd.getDestFile().getAbsolutePath()));
            }
            catch (Exception e)
            {
                Log.e(TAG,
                        className
                                + "run"
                                + String.format("Could not complete download for range [%s, %s], url: %s.", rd.getStartByteIdx(), rd.getEndByteIdx(),
                                        rd.getDownloadUrl()), e);

            }
        }
    };

    /**
     * 准备下载 <功能详细描述>
     * @param tempDir
     * @param i
     * @param totalChunks
     * @param details
     * @return
     * @see [类、类#方法、类#成员]
     */
    private RangeDownloader prepareRangeDownloader(File tempDir, int i, int totalChunks, HttpDetails details)
    {
        File tmpFile = new File(tempDir, i + ".part");
        long startIdx = i * chunkSize;
        long endIdx;
        if (i == totalChunks - 1)
        {

            endIdx = details.contentLength;
        }
        else
        {
            endIdx = (i + 1) * chunkSize;
        }
        endIdx--;

        Log.d("TSS", "[" + startIdx + "," + endIdx + "]");

        RangeDownloader rd = new RangeDownloader(getDownloadUrl(), tmpFile, startIdx, endIdx, mConnTimeout, mReadTimeout);
        return rd;

    }

    public double getProgress()
    {
        if (contentLength == 0)
        {
            return 0;
        }
        if (downloaders == null)
        {
            return 0;
        }
        int sum = 0;
        for (RangeDownloader dl : downloaders)
        {
            sum += dl.getBytesDownloaded();
        }

        return ((double) sum) / contentLength;

    }

    private RDListener mRDListerner = new RDListener();

    class RDListener implements RangeDownloadListener
    {
        @Override
        public void onComplete(String url, String local, long start, long end)
        {

        }

        @Override
        public void onError(int errCode, Object detail)
        {
            synchronized (this)
            {

                if (getMListener() != null)
                {
                    getMListener().onError(MultiThreadDownloader.this, errCode, detail);
                }

                Log.e("TSS", "onERROR: " + errCode + " detail:" + detail);
                stop();
            }
        }

    }

    public String getTempDir()
    {
        return mTempDir;
    }

    public void setTempDir(String mTempDir)
    {
        this.mTempDir = mTempDir;
    }

    public int getErrCode()
    {
        return errCode;
    }

    public MultiDownloadListener getMListener()
    {
        return mdl;
    }

    public void setMListener(MultiDownloadListener mdl)
    {
        this.mdl = mdl;
    }

    public URL getDownloadUrl()
    {
        return downloadUrl;
    }

    public void setDownloadUrl(URL downloadUrl)
    {
        this.downloadUrl = downloadUrl;
    }

    public File getDestFile()
    {
        return destFile;
    }

    public void setDestFile(File destFile)
    {
        this.destFile = destFile;
    }

}
