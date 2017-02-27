/*
 * 文件名: LRCUtil.java 版 权： Copyright Huawei Tech. Co. Ltd. All Rights Reserved. 描
 * 述: [该类的简要描述] 创建人: zKF16094 创建时间:2009-10-20 修改人：yaoge 修改时间: 2009-10-20 修改内容：新增
 */

/*
 * 修改人：yaoge 修改时间: 2009-10-23 修改内容：新增传入歌词文件路径解析歌词
 */

/*
 * 修改人：yaoge 修改时间: 2009-10-23 修改内容：新增获取指定行数歌词方法
 */
package com.rockchips.mediacenter.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

/**
 * 歌词解析类<BR>
 * [功能详细描述]
 * 
 * @author zKF16094
 * @version [MusicClient V100R001C08, 2009-10-20]
 */
public final class Lyric
{
    /**
     * LOG tag
     */
    private static final String TAG = "Lyric";

    /**
     * 实例
     */
    private static Lyric instance;

    private static Object lockObject = new Object();

    private Object lyricLock = new Object();

    /**
     * 歌词
     */
    public String[] mLyricList;

    /**
     * 时间：秒
     */
    public int mTimeList[];

    /**
     * 歌词包含的一些信息:0:歌名,1:歌手,2:专辑名,3:编者,4:时间补偿值
     */

    private String title = null; // 0:歌名

    private String artist = null; // ,1:歌手,

    private String album = null; // 2:专辑名,

    private String createBy = null;// 3:编者,

    private String offset = null; // 4:时间补偿值

    /**
     * 是有构造函数，单例
     */
    private Lyric()
    {
        ParseEncoding.initParseEncoding();
    }

    /**
     * 懒汉式单例<BR>
     * [功能详细描述]
     * 
     * @return LRCUtil
     */
    public static synchronized Lyric getInstance()
    {
        synchronized (lockObject)
        {
            if (null == instance)
            {
                instance = new Lyric();
            }
        }
        return instance;
    }

    public int getLineCount()
    {
        if (mLyricList != null)
        {
            return mLyricList.length;
        }
        return 0;
    }

    /**
     * 获取当前行时间
     * 
     * @param aLine 当前行
     * @return 时间
     * @see [类、类#方法、类#成员]
     */
    public int getTimeByLine(int aLine)
    {
        if (null != mTimeList && aLine > -1 && aLine < mTimeList.length)
        {
            return mTimeList[aLine];
        }
        else
        {
            return -1;
        }
    }

    public int getLineByTime(int time)
    {

        if (null == mTimeList)
        {
            return -1;
        }
        // TODO:采用二分法查找速度会更快， 暂且使用此方法
        int lineCount = getLineCount();
        if (lineCount < 2 || time < mTimeList[1])
        {
            return 0;
        }
        if (time >= mTimeList[lineCount - 1])
        {
            return lineCount - 1;
        }

        for (int i = 0; i < lineCount - 1; i++)
        {
            if (time >= mTimeList[i] && time < mTimeList[i + 1])
            {
                return i;
            }
        }
        return 0;

    }

    public String getLrcByLine(int line)
    {
        if (null != mLyricList && line > -1 && line < mLyricList.length)
        {
            return mLyricList[line];
        }
        else
        {
            return "";
        }
    }

    public int getStartTime(int line)
    {
        if (null != mTimeList && line > -1 && line < mTimeList.length)
        {
            return mTimeList[line];
        }
        else
        {
            return 0;
        }
    }

    public int getDuration(int line)
    {
        if (null != mTimeList && line > -1 && line < mTimeList.length)
        {
            if (mTimeList.length < 2 || line >= mTimeList.length - 1)
            {
                // 表示
                return Integer.MAX_VALUE;
            }

            return mTimeList[line + 1] - mTimeList[line];
        }
        else
        {

            return 0;
        }
    }

    /**
     * <一句话功能简述>初始化 <功能详细描述>
     * 
     * @see [类、类#方法、类#成员]
     */
    private void init()
    {
        title = null; // 0:歌名
        artist = null; // 1:歌手,
        album = null; // 2:专辑名,
        createBy = null; // 3:编者,
        offset = null; // 4:时间补偿值

    }

    /**
     * 对时间和歌词进行排序
     */
    private void sort()
    {
        for (int i = 1; i < mTimeList.length; i++)
        {
            for (int j = mTimeList.length - 1; j >= i; j--)
            {
                if (mTimeList[j] < mTimeList[j - 1])
                {
                    int temp = mTimeList[j];
                    mTimeList[j] = mTimeList[j - 1];
                    mTimeList[j - 1] = temp;
                    String tt = mLyricList[j];
                    mLyricList[j] = mLyricList[j - 1];
                    mLyricList[j - 1] = tt;
                }
            }
        }
    }

    /**
     * 从文件来分析歌词
     * 
     * @param text 文件
     * @return 解析是否成功
     */
    public synchronized boolean paser(String text)
    {
        if (text == null || text.trim().length() == 0)
        {
            text = "";
            return false;
        }

        // 简单判断下歌词是否符合规则 如果是WAP报文，则不画歌词
        if (text.indexOf("<wml>") != -1)
        {
            return false;
        }

        // add by s00203507 2012年10月27日 begin
        // DTS2012101507521 【天津联通B018，ishareV1.5.615】爱分享推送音乐至STB，STB歌词显示出错。
        // 当歌词不存在时，百度音乐掌门人会跳转到出错提示界面，歌词界面会出现显示出错提示界面javascript脚本的情况
        // 这时根据界面内容中是否包含“[ti:”和“[00:”标签来判断，如果不包含这两个标签，则说明不是歌词界面，不用继续解析歌词。
        if (text.indexOf("[00:") == -1)
        {
            return false;
        }
        // add by s00203507 2012年10月27日 end

        release();
        init();
        boolean result = false;
        text = text.trim();
        List<String> tmpLRC = new ArrayList<String>();
        List<String> tmpTime = new ArrayList<String>();
        try
        {
            while (text != null)
            {
                int bPos = 0;
                int ePos = 0;
                bPos = text.indexOf("[");
                ePos = text.indexOf("]");
                String temp = null;
                // 假如有标签就分析标签的内容

                if (bPos != -1 && ePos != -1)
                {
                    // 截取标签里的内容
                    temp = text.substring(bPos + 1, ePos);

                    if (temp.startsWith("ar:"))
                    {
                        artist = temp.substring("ar:".length());
                    }
                    else if (temp.startsWith("ti:"))
                    {
                        title = temp.substring("ti:".length());
                    }
                    else if (temp.startsWith("al:"))
                    {
                        album = temp.substring("al:".length());
                    }
                    else if (temp.startsWith("by:"))
                    {
                        createBy = temp.substring("by:".length());
                    }
                    else if (temp.startsWith("offset:"))
                    {
                        offset = temp.substring("offset:".length());
                    }
                    else
                    {
                        // 时间段
                        // Log.d(TAG, temp);
                        try
                        {
                            int startTime = 0;

                            // 解析毫秒，毫秒是以.分割
                            int dotIndex = temp.indexOf(".");
                            if (dotIndex != -1)
                            {
                                String millisecond = temp.substring(dotIndex + 1);
                                /*
                                 * BEGIN: Modified by c00224451 for 新增音乐播放，歌词滚动效果 2014/3/26
                                 */
                                // startTime += Integer.parseInt(millisecond) *
                                // 1000 % 1000;
                                startTime += Integer.parseInt(millisecond) * Math.pow(10, (3 - millisecond.length()));
                                /*
                                 * END: Modified by c00224451 for 新增音乐播放，歌词滚动效果 2014/3/26
                                 */
                                temp = temp.substring(0, dotIndex);
                            }

                            String time[] = temp.split(":");

                            if (time != null)
                            {
                                for (int k = 0; k < time.length; k++)
                                {
                                    startTime += Integer.parseInt(time[k]) * Math.pow(60, (time.length - 1 - k)) * 1000;
                                }
                            }
                            tmpTime.add(String.valueOf(startTime));

                        }
                        catch (NumberFormatException ne)
                        {
                            // 非法时间，跳过此行，继续执行下面一行
                            text = text.substring(ePos + 1, text.length());
                            continue;
                        }
                    }
                }
                if (ePos > -1)
                {
                    text = text.substring(ePos + 1, text.length());
                }
                // 分析完毕后..把内容存入对应的属性中
                else
                {
                    if (tmpTime.size() != 0)
				    {
				        tmpLRC.add(tmpTime.get(0));
	                    tmpLRC.add("");
				    }
                    text = null;
                    /* BEGIN: Modified by c00224451 for 新增音乐播放，歌词滚动效果 2014/3/26 */
                    int listSize = tmpLRC.size() / 2;
                    mTimeList = new int[listSize];
                    mLyricList = new String[listSize];

                    for (int i = 0; i < listSize; i++)
                    {
                        mTimeList[i] = Integer.parseInt(tmpLRC.get(i * 2));
                        mLyricList[i] = tmpLRC.get(i * 2 + 1);
                    }
                    /* END: Modified by c00224451 for 新增音乐播放，歌词滚动效果 2014/3/26 */
                    sort();
                    tmpLRC.clear();
                    tmpTime.clear();
                    tmpLRC = null;
                    tmpTime = null;
                    result = true;
                    break;
                }

                ePos = text.indexOf("[");
                // 截取歌词的内容

                if (ePos > -1)
                {
                    temp = text.substring(0, ePos);
                }
                else
                {
                    temp = text;
                }
                if (!temp.equals(""))
                {
                    for (int i = 0; i < tmpTime.size(); i++)
                    {

                        tmpLRC.add(tmpTime.get(i));

                        boolean isLRCCopyed = false;

                        for (int j = 0; j < temp.length(); j++)
                        {
                            if (temp.charAt(j) == '\r' || temp.charAt(j) == '\n')
                            {
                                String tLrc = temp.substring(0, j);

                                tmpLRC.add(tLrc);

                                isLRCCopyed = true;
                                break;
                            }
                        }

                        if (!isLRCCopyed)
                        {
                            tmpLRC.add(temp);
                        }
                    }
                    tmpTime.clear();
                }
            }
        }
        catch (Exception e)
        {
            return false;
        }
        // 如果 歌词没有一行，也解析错误
        if (null == mTimeList || mTimeList.length <= 0)
        {
            return false;
        }

        return result;
    }

    /**
     * 分析下载来的歌词
     * 
     * @param aLRC 歌词报文
     * @return 是否成功
     */
    private boolean paser(byte[] aLRC)
    {
        String tmp = null;
        boolean res = false;
        try
        {
            // CharsetDetector charDect = new CharsetDetector();
            // String encodeingString = charDect.detectCharset(aLRC);

            String encodeingString = ParseEncoding.getEncoding(aLRC);
            if (encodeingString.equals("ASCII") || encodeingString.equals("ISO8859-1"))
            {
                CharsetDetector charDect = new CharsetDetector();
                encodeingString = charDect.detectCharset(aLRC);
            }

            tmp = new String(aLRC, encodeingString);
            res = paser(tmp);
        }
        catch (Exception e)
        {
            Log.d(TAG, e.toString());
            res = false;
        }
        return res;
    }

    /**
     * <一句话功能简述>释放资源 <功能详细描述>
     * 
     * @see [类、类#方法、类#成员]
     */
    public void release()
    {
        synchronized (lyricLock)
        {

            mLyricList = null;
            mTimeList = null;
        }

    }

    /**
     * 根据URL解析歌词<BR>
     * [功能详细描述]
     * 
     * @param url url
     * @return boolean
     */
    public synchronized boolean parserFileToLrc(String url)
    {
        synchronized (lyricLock)
        {
            if (null == url || url.equals(""))
            {
                return false;
            }

            Log.d(TAG, "parserFileToLrc url= " + url);
            FileInputStream fis = null;
            File file = null;
            try
            {
                file = new File(url);

                // 如果不存在则不解析
                if (!file.exists())
                {
                    return false;
                }
                int size = (int) file.length();
                // 如果歌词的size小于等于0则返回false
                if (size <= 0 || size > 20 * 1024)
                {
                    return false;
                }
                byte[] bytes = new byte[size];

                // 从文件中读取歌词
                fis = new FileInputStream(file);
                int resultSize = fis.read(bytes);

                // findbugs
                if (resultSize < 1)
                {
                    return false;
                }

                // 解析歌词
                boolean isSuccess = paser(bytes);

                return isSuccess;

            }
            catch (FileNotFoundException e)
            {
                Log.d(TAG, e.toString());
                return false;
            }
            catch (IOException e)
            {
                Log.d(TAG, e.toString());
                return false;
            }
            finally
            {
                try
                {
                    if (null != fis)
                    {
                        fis.close();
                    }
                }
                catch (IOException e)
                {
                    Log.e(TAG, e.toString());
                }
                file = null;
                fis = null;
            }
        }
    }

    public String[] getLyricList()
    {
        return mLyricList;
    }

    public int[] getTimeList()
    {
        return mTimeList;
    }

    private String[] mCurrentLyricArray = new String[2];

    public String[] getCurrentLyricArray(int time)
    {
        int line = -1;
        line = getLineByTime(time);
        if (line == -1)
        {
            for (int i = 0; i < mCurrentLyricArray.length; i++)
            {
                mCurrentLyricArray[i] = "";
            }
        }
        else
        {
            if ((line % 2) == 0)
            {
                mCurrentLyricArray[0] = mLyricList[line];
                if ((line + 1) <= (mLyricList.length - 1))
                {
                    mCurrentLyricArray[1] = mLyricList[line + 1];
                }
                else
                {
                    mCurrentLyricArray[1] = "";
                }
            }
            else
            {
                /* BEGIN: Modified by c00224451 for 新增音乐播放，歌词滚动效果 2014/3/26 */
                mCurrentLyricArray[1] = mLyricList[line];
                // if ((line-1) >= 0) {
                // // mCurrentLyricArray[0] = mLyricList[line-1];
                // } else {
                // mCurrentLyricArray[0] = "";
                // }
                if ((line + 1) <= (mLyricList.length - 1))
                {
                    mCurrentLyricArray[0] = mLyricList[line + 1];
                }
                else
                {
                    mCurrentLyricArray[0] = "";
                }
                /* END: Modified by c00224451 for 新增音乐播放，歌词滚动效果 2014/3/26 */
            }
        }

        return mCurrentLyricArray;
    }

    public boolean isFirstLine(int line)
    {
        if (line < 0)
        {
            return false;
        }
        if (mLyricList == null || mLyricList.length == 0)
        {
            return false;
        }
        if (line == 0)
        {
            return true;
        }
        return false;
    }

    public boolean isLastLine(int line)
    {
        if (line < 0)
        {
            return false;
        }
        if (mLyricList == null || mLyricList.length == 0)
        {
            return false;
        }

        if (line == (mLyricList.length - 1))
        {
            return true;
        }
        return false;
    }

}
