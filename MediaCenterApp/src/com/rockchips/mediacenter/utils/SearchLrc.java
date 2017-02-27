package com.rockchips.mediacenter.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLEncoder;

import android.util.Log;

public class SearchLrc
{

    private static final String LOGTAG = "SearchLrc";

    public static final String DEFAULT_LOCAL = "GB2312";

    public static final String local = "UTF-8";

    public static final boolean isUseProxy = false;

    private URL url;

    // 包含歌词路径的XML字符串
    StringBuffer sb = new StringBuffer();

    private boolean findNumber = false;

    private String mMusicNameAfterEncode = null;

    private String mSingerNameAfterEncode = null;

    /**
     * 播放DMS上的歌曲时歌词缓存路径
     */
    // public static final String LYRIC_CACHE_PATH = "/mnt/sdcard/lyric/";
    public static final String LYRIC_CACHE_PATH_DMS = "/data/data/com.rockchips.mediacenter/cache/lyric";

    /**
     * 播放推送、甩屏的歌曲时歌词缓存路径
     */
    // public static final String LYRIC_CACHE_PATH = "/mnt/sdcard/lyric/";
    public static final String LYRIC_CACHE_PATH_EXTERNAL = LYRIC_CACHE_PATH_DMS;

    /**
     * 歌词缓存路径，根据内置播放器还是外置播放器设定不同的存储路径
     */
    public static String LYRIC_CACHE_PATH = LYRIC_CACHE_PATH_DMS;

    /*
     * 初期化，根据参数取得lrc的地址
     */
    public SearchLrc(String musicName, String singerName)
    {

        // 传进来的如果是汉字，那么就要进行编码转化
        try
        {
            musicName = URLEncoder.encode(musicName, local);
            singerName = URLEncoder.encode(singerName, local);

            mMusicNameAfterEncode = musicName;
            mSingerNameAfterEncode = singerName;
        }
        catch (UnsupportedEncodingException e2)
        {
            Log.e(LOGTAG, "UnsupportedEncodingException");
            return;
        }

        String strUrl = "http://box.zhangmen.baidu.com/x?op=12&count=1&title=" + musicName + "$$" + singerName + "$$$$";
        Log.d(LOGTAG, strUrl);

        try
        {
            url = new URL(strUrl);
            Log.d(LOGTAG, "SearchLRC===============" + "url = " + url);
        }
        catch (MalformedURLException  e1)
        {
            Log.e(LOGTAG, "MalformedURLException");
            return;
        }

        HttpURLConnection httpConn = null;
        BufferedReader br = null;

        try
        {
            httpConn = (HttpURLConnection) url.openConnection();

            // 在你发起Http请求之前设置一下属性
            // System.setProperty("proxySet", "true");
            // System.setProperty("http.proxyHost", "proxycn2.rockchips.com");
            // System.setProperty("http.proxyPort", "8080");

            if (isUseProxy)
            {
                System.setProperty("http.proxyHost", "192.168.0.110");
                System.setProperty("http.proxyPort", "808");

                Authenticator.setDefault(new BasicAuthenticator("User-001", ""));
            }

            // Authenticator.setDefault(new BasicAuthenticator("", ""));

            httpConn.setConnectTimeout(10000);
            httpConn.connect();

            InputStreamReader inReader = new InputStreamReader(httpConn.getInputStream());

            Log.d(LOGTAG, "the encode is " + inReader.getEncoding());
            br = new BufferedReader(inReader);

            String s;
            while ((s = br.readLine()) != null)
            {
                sb.append(s);
                sb.append("\r\n");
            }
            Log.d(LOGTAG, "SearchLrc :sb = " + sb);
        }
        catch (IOException e)
        {
            Log.e(LOGTAG, "", e);
        }
        finally
        {

            try
            {
                if (br != null)
                {
                    br.close();
                }
            }
            catch (IOException e)
            {
                Log.e(LOGTAG, "", e);
            }

            try
            {
                if (httpConn != null)
                {
                    httpConn.disconnect();
                }
            }
            catch (Exception e)
            {
                Log.e(LOGTAG, "", e);
            }
        }
    }

    /*
     * 根据lrc的地址，读取lrc文件流 生成歌词的ArryList 每句歌词是一个String
     */
    public String fetchLyric()
    {
        int begin = 0;
        int end = 0;
        int number = 0;// number=0表示暂无歌词

        String strid = "";
        begin = sb.indexOf("<lrcid>");
        Log.d(LOGTAG, "sb = " + sb);

        try
        {
            if (begin != -1)
            {
                end = sb.indexOf("</lrcid>", begin);
                strid = sb.substring(begin + 7, end);
                number = Integer.parseInt(strid);
                if (number <= 0)
                {
                    Log.w(LOGTAG, "lrcid---------------------0");
                    return null;
                }

            }
            else
            {
                Log.w(LOGTAG, "<lrcid>---------------------not found");
                return null;
            }
        }
        catch (Exception ex)
        {
            Log.e(LOGTAG, "", ex);
            return null;
        }

        String geciURL = "http://box.zhangmen.baidu.com/bdlrc/" + number / 100 + "/" + number + ".lrc";
        SetFindLRC(number);

        Log.d(LOGTAG, "geciURL = " + geciURL);

        StringBuilder gcContent = new StringBuilder();

        String s = new String();
        try
        {
            url = new URL(geciURL);
        }
        catch (MalformedURLException e2)
        {
            Log.e(LOGTAG, "", e2);
        }

        InputStreamReader isr = null;
        BufferedReader br = null;
//        BufferedWriter bw = null;
        FileOutputStream fos = null;
//        OutputStreamWriter osw = null;

        File file = null;
        try
        {
            isr = new InputStreamReader(url.openStream(), "GB2312");
            br = new BufferedReader(isr);
            file = createLyricFile();
            fos = new FileOutputStream(file);
            
            while ((s = br.readLine()) != null)
            {
                s += "\r\n";
                gcContent.append(s);                
                Log.d(LOGTAG, "s = " + s);
                fos.write(s.getBytes());
            }
            Log.d(LOGTAG, "gcContent = " + gcContent.toString());
        }
        catch(UnsupportedEncodingException e)
        {
        }
        catch(FileNotFoundException e)
        {
        }
        catch (IOException e)
        {
        }
        finally
        {
            if (isr != null)
            {
                try
                {
                    isr.close();
                }
                catch (IOException e)
                {
                }
            }
            if (br != null)
            {
                try
                {
                    br.close();
                }
                catch (IOException e)
                {
                }
            }
            if (fos != null)
            {
                try
                {
                    fos.close();
                }
                catch (IOException e)
                {
                }
            }
        }

        if (isUseProxy)
        {
            try
            {
                System.clearProperty("http.proxyHost");
                System.clearProperty("http.proxyPort");
            }
            catch (Exception ex)
            {
                Log.e(LOGTAG, "", ex);
            }
        }

        return gcContent.toString();
    }

    private void SetFindLRC(int number)
    {
        if (number == 0)
            findNumber = false;
        else
            findNumber = true;
    }

    public boolean GetFindLRC()
    {
        return findNumber;
    }

    class BasicAuthenticator extends Authenticator
    {
        String userName;

        String password;

        public BasicAuthenticator(String userName, String password)
        {
            this.userName = userName;
            this.password = password;
        }

        /**
         * Called when password authorization is needed. Subclasses should override the default implementation, which returns null.
         * 
         * @return The PasswordAuthentication collected from the user, or null if none is provided.
         */
        protected PasswordAuthentication getPasswordAuthentication()
        {
            return new PasswordAuthentication(userName, password.toCharArray());
        }
    }

    /**
     * 创建歌词缓存文件
     * 
     * @return
     */
    public File createLyricFile()
    {
        if (StringUtils.isEmpty(mMusicNameAfterEncode) || StringUtils.isEmpty(mSingerNameAfterEncode))
        {
            return null;
        }

        File directory = new File(LYRIC_CACHE_PATH);
        if (!directory.exists())
        {
            directory.mkdirs();
        }

        String lyricFilePath = LYRIC_CACHE_PATH + mMusicNameAfterEncode + "_" + mSingerNameAfterEncode + ".lrc";

        File lyricPath = new File(lyricFilePath);

        return lyricPath;
    }

    /**
     * 歌词是否已存在缓存中
     * 
     * @param path
     * @return
     */
    public static boolean isLyricExistInCache(String path)
    {
        Log.d(LOGTAG, "isLyricExistInCache() IN..., path " + path);
        if (StringUtils.isEmpty(path))
        {
            return false;
        }
        File file = new File(path);

        if (!file.exists() || file.length() == 0)
        {
            return false;
        }

        return true;
    }

    public static String getLyricCachePath(String musicName, String singerName)
    {
        Log.d(LOGTAG, "getLyricCachePath() IN...");
        try
        {
            musicName = URLEncoder.encode(musicName, local);
            singerName = URLEncoder.encode(singerName, local);
        }
        catch (UnsupportedEncodingException e)
        {
        }

        if (StringUtils.isEmpty(musicName) || StringUtils.isEmpty(singerName))
        {
            return null;
        }

        setLyricCachePath();

        String lyricFilePath = LYRIC_CACHE_PATH + musicName + "_" + singerName + ".lrc";
        Log.d(LOGTAG, "lyricFilePath " + lyricFilePath);
        return lyricFilePath;
    }

    static boolean bInternalAudioPlayer = false;

    public static boolean isInternalPlayer()
    {
        return bInternalAudioPlayer;
    }

    public static boolean isInternalPlayer(boolean b)
    {
        return bInternalAudioPlayer = b;
    }

    public static void setLyricCachePath()
    {
        if (bInternalAudioPlayer)
        {
            LYRIC_CACHE_PATH = LYRIC_CACHE_PATH_DMS;
        }
        else
        {
            LYRIC_CACHE_PATH = LYRIC_CACHE_PATH_EXTERNAL;
        }
    }
}
