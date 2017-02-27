package com.rockchips.mediacenter.utils;

import java.io.UnsupportedEncodingException;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import com.rockchips.mediacenter.data.DeviceDataConst;
import com.rockchips.mediacenter.utils.HanziToPinyin.Token;

/**
 * 
 * StringUtils
 * 
 * 2011-11-5 下午04:19:51
 * 
 * @version 1.0.0
 * 
 */
public final class StringUtils
{
    // private static final String TAG ="StringUtils";
    private StringUtils()
    {
    }

    /**
     * 判断字符串是否为空 isEmpty
     * 
     * @param str
     * @return boolean
     * @exception
     */
    public static boolean isEmpty(String str)
    {
        if (str == null || str.trim().equals(""))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * 判断字符串是否非空 isNotEmpty
     * 
     * @param str
     * @return boolean
     * @exception
     */
    public static boolean isNotEmpty(String str)
    {
        return !isEmpty(str);
    }

    /**
     * 判断当前系统用的是什么语言
     * @author zWX160481
     * @param context
     * @param country
     */
    public static boolean getLocalCountry(Context context, String country)
    {
        if (context == null || StringUtils.isEmpty(country))
        {
            return false;
        }
        String localCountry = context.getResources().getConfiguration().locale.getLanguage();
        // Log.D("", "localCountry--->language--->"+localCountry);
        // Log.D(TAG, "country--->language--->"+country);
        if (country.equals(localCountry))
        {
            return true;
        }
        else
        {
            return false;
        }

    }

    /**
     * 去掉空格 trim
     * 
     * @param text
     * @return String
     * @exception
     */
    public static String trim(String text)
    {
        return text == null ? null : text.trim();
    }

    /**
     * 比较两字符串，在忽略空格的时候，是否相同 isEqualsIgnoreBlank
     * 
     * @param str1
     * @param str2
     * @return boolean
     * @exception
     */
    public static boolean isEqualsIgnoreBlank(String str1, String str2)
    {
        if (str1 == null)
        {
            return str2 == null ? true : false;
        }
        else
        {
            return str1.trim().equals(trim(str2));
        }
    }

    /**
     * 是否为URI字符串 isURI
     * 
     * @param str
     * @return boolean
     * @exception
     */
    public static boolean isURI(String str)
    {
        if (isEmpty(str))
        {
            return false;
        }

        return str.startsWith("content://");
    }

    /**
     * 从一个字符串中获取文件名 获取原则如果: http://xdfd/xfds/sfsd/sfsd.xxx ==> sfsd
     * 
     * 
     * getFileName
     * 
     * @param str
     * @return null 不符合文件名的规范如: http://xdfd/xfds/sfsd/sfsd ,否则 返回 文件名 String
     * @exception
     */
    public static String getFileName(String str)
    {
        if (isEmpty(str))
        {
            return null;
        }

        int start = str.lastIndexOf('/');
        start++;

        int end = str.lastIndexOf('.');

        if (end <= start)
        {
            return null;
        }

        return str.substring(start, end);
    }

    /**
     * 获取系统时间的日志文本 getSystemTimeLogText
     * 
     * @return String
     * @exception
     */
    public static String getSystemTimeLogText()
    {
        SimpleDateFormat formatter = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss.SSS]");
        Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
        return formatter.format(curDate);
    }

    /**
     * 获取系统时间的文本 getSystemTimeText
     * 
     * @return String
     * @exception
     */
    public static String getSystemTimeText()
    {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
        return formatter.format(curDate);
    }

    /**
     * 
     * getRealText
     * 
     * @return String
     * @exception
     */
    public static String getRealText(String text)
    {
        if (isEmpty(text))
        {
            return text;
        }

        ParseEncoding.initParseEncoding();
        String encodeingString = ParseEncoding.getEncoding(text.getBytes());

        try
        {
            return new String(text.getBytes(), "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            return text;
        }
    }

    /**
     * 获取某整数的数字个数
     */
    public static int getNumCount(int number)
    {
        if (number < 0)
        {
            return 0;
        }
        else if (number <= 9)
        {
            return 1;
        }

        int result = 1;

        int tempNumber = number;

        while (tempNumber >= 10)
        {
            result++;

            // 大于0继续
            tempNumber /= 10;
        }

        return result;
    }

    /**
     * 获取某整数的格式化字符串
     */
    public static String getFormatString(int number, int length)
    {
        int size = getNumCount(number);

        if (size >= length)
        {
            return String.valueOf(number);
        }
        else
        {
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < length - size; i++)
            {
                sb.append("0");
            }

            sb.append(String.valueOf(number));

            return sb.toString();
        }
    }

    public static boolean isNetworkURI(String url)
    {
        if (isEmpty(url))
        {
            return false;
        }
        if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("rtsp://"))
        {
            return true;
        }
        return false;
    }

    public static boolean isDropBoxURL(String url)
    {
        if (isEmpty(url))
        {
            return false;
        }

        if (url.startsWith("http://dl.dropboxusercontent"))
        {
            return true;
        }
        return false;
    }

    public static boolean isMSPURI(String url)
    {
        if (isEmpty(url))
        {
            return false;
        }

        if (url.startsWith("nsp://"))
        {
            return true;
        }
        return false;
    }

    /**
     * sql 语句规范化函数，置换特殊字符
     * 
     * @param strSql
     * @return
     */
    public static String sqlStandardization(String strSql)
    {
        if (strSql != null)
        {
            strSql = strSql.replace("'", "''");
        }
        return strSql;
    }

    /**
     * 规范化设备显示名称
     * 
     * @param displayName
     * @param r
     * @param resid 资源ID
     * @return
     */
    public static String formatDeviceDisplayName(String displayName, Resources r, int resid)
    {
        if (isEmpty(displayName))
        {
            return r.getString(resid);
        }
        else
        {
            return displayName;
        }
    }

    /**
     * 规范化文件夹显示名称
     * 
     * @param displayName
     * @param r
     * @param resid 资源ID
     * @return
     */
    public static String formatFolderDisplayName(String displayName, Resources r, int resid)
    {
        if (isEmpty(displayName))
        {
            return r.getString(resid);
        }
        else
        {
            return displayName;
        }
    }

    public static String replaceString(String source, String hi, String hiTarget)
    {
        if (StringUtils.isEmpty(source) || StringUtils.isEmpty(hiTarget) || StringUtils.isEmpty(hi))
        {
            return source;
        }

        return source.replace(hi, hiTarget);

    }

    /**
     * 
     * @param timelong ms
     * @param format yyyy-MM-dd
     * @return
     */
    public static String covertToDataFromLong(long timelong, String format)
    {
        if (StringUtils.isEmpty(format))
        {
            format = "yyyy-MM-dd";
        }

        Date date = new Date();
        date.setTime(timelong);
        SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat(format);

        try
        {
            String strRet = mSimpleDateFormat.format(date);
            return strRet;
        }
        catch (Exception ex)
        {
        }

        return "";
    }

    public static String getParentFolderName(String filePath)
    {
        if (isEmpty(filePath))
        {
            return null;
        }
        if (filePath.endsWith("/"))
        {
            filePath = filePath.substring(0, filePath.lastIndexOf("/"));
        }
        return filePath.substring(filePath.lastIndexOf("/") + 1);
    }

    // /**
    // * 把 strB与listData连接起来
    // *
    // * @param string
    // * [IN,OUT]
    // * @param list
    // * @return
    // */
    // public static String appendDyadicList(StringBuilder strB,
    // List<DyadicData> listData)
    // {
    // if (listData == null || listData.size() == 0)
    // {
    // return strB.toString();
    // }
    //
    // for (int size = 0; size < listData.size(); size++)
    // {
    // DyadicData dd = listData.get(size);
    // if (dd != null && com.rockchips.iptv.stb.util.StringUtils.isNotEmpty(dd.getStrName()))
    // {
    // if (com.rockchips.iptv.stb.util.StringUtils.isNotEmpty(strB.toString()))
    // {
    // strB.append(" AND ");
    // }
    // strB.append(dd.getStrName());
    // strB.append(dd.getStrValue());
    // }
    // }
    //
    // return strB.toString();
    //
    // }
    //
    // /**
    // * 规范化设备显示名称
    // *
    // * @param displayName
    // * @param r
    // * @return
    // */
    // public static String formatDeviceDisplayName(String displayName, Resources r)
    // {
    // if (com.rockchips.iptv.stb.util.StringUtils.isEmpty(displayName))
    // {
    // return r.getString(R.string.unknown);
    // } else
    // {
    // return displayName;
    // }
    // }
    //
    // /**
    // * 规范化文件夹显示名称
    // *
    // * @param displayName
    // * @param r
    // * @return
    // */
    // public static String formatFolderDisplayName(String displayName, Resources r)
    // {
    // if (com.rockchips.iptv.stb.util.StringUtils.isEmpty(displayName))
    // {
    // return r.getString(R.string.unknown);
    // } else
    // {
    // return displayName;
    // }
    // }

    public static String getFullPinYinLower(String source)
    {
        if (null == source)
        {
            return null;
        }

        List<Locale> locales = Arrays.asList(Collator.getAvailableLocales());
        if (!locales.contains(Locale.CHINA) &&
            !locales.contains(Locale.CHINESE) &&
            !locales.contains(Locale.SIMPLIFIED_CHINESE))
        {
            return source.toLowerCase();
        }

        ArrayList<Token> tokens = HanziToPinyin.getInstance().get(source);

        if (tokens == null || tokens.size() == 0)
        {
            return source.toLowerCase();
        }

        StringBuffer result = new StringBuffer();

        for (int i = 0; i < tokens.size(); ++i)
        {            
            Token token = tokens.get(i);
            if (token == null)
            {
                continue;
            }
            if (token.type == Token.PINYIN)
            {
                result.append(token.target);
            }
            else
            {
                result.append(token.source);
            }
            if (i != tokens.size() - 1)
            {
                result.append(" ");
            }
        }

        return result.toString().toLowerCase();
    }

    /**
     * 把NULL数据转换成""数据，防止NULL异常
     * 
     * @param data 要转换的数据
     * @return data为NULL时返回""
     * @see [类、类#方法、类#成员]
     */
    public static String filterNullData(String data)
    {
        return data == null ? DeviceDataConst.BLANK_STRING : data;
    }

    public static int getTextLengthOfPix(String text, Paint paint, float textSize)
    {
        int lengthOfPix = 0;
        Rect rc = new Rect();
        if (paint != null && text != null)
        {
            paint.setTextSize(textSize);
            paint.getTextBounds(text, 0, text.length(), rc);
            lengthOfPix = rc.width();
        }

        return lengthOfPix;
    }
}
