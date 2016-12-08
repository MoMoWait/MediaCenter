/**
 * 
 * com.rockchips.iptv.stb.dlna.util
 * CharsetUtils.java
 * 
 * 2012-1-6-下午07:44:38
 * Copyright 2012 Huawei Technologies Co., Ltd
 * 
 */
package com.rockchips.mediacenter.basicutils.util;

import java.io.UnsupportedEncodingException;

/**
 * 
 * CharsetUtils
 * 
 * 2012-1-6 下午07:44:38
 * 
 * @version 1.0.0
 * 
 */
public class CharsetUtils
{
    private static final String PRE_FIX_UTF = "&#x";

    private static final String POS_FIX_UTF = ";";

    /**
     * Translate charset encoding to unicode
     * 
     * @param sTemp charset encoding is gb2312
     * @return charset encoding is unicode
     */
    public static String XmlFormalize(String sTemp)
    {
        StringBuffer sb = new StringBuffer();

        if (sTemp == null || sTemp.equals(""))
        {
            return "";
        }
        String s = tranEncodeToGB(sTemp);
        for (int i = 0; i < s.length(); i++)
        {
            char cChar = s.charAt(i);
            if (isGB2312(cChar))
            {
                sb.append(PRE_FIX_UTF);
                sb.append(Integer.toHexString(cChar));
                sb.append(POS_FIX_UTF);
            }
            else
            {
                switch ((int) cChar)
                {
                    case 32:
                        sb.append("&#32;");
                        break;
                    case 34:
                        sb.append("&quot;");
                        break;
                    case 38:
                        sb.append("&amp;");
                        break;
                    case 60:
                        sb.append("&lt;");
                        break;
                    case 62:
                        sb.append("&gt;");
                        break;
                    default:
                        sb.append(cChar);
                }
            }
        }
        return sb.toString();
    }

    /**
     * 将字符串编码格式转成GB2312
     * 
     * @param str
     * @return
     */
    public static String tranEncodeToGB(String str)
    {
        try
        {
            String strEncode = getEncoding(str);
            String temp = new String(str.getBytes(strEncode), "GB2312");
            return temp;
        }
        catch (java.io.IOException ex)
        {

            return null;
        }
    }

    /**
     * 将字符串编码格式转成UTF-8
     * 
     * @param str
     * @return
     */
    public static String tranEncodeToUTF8(String str)
    {
        try
        {
            String strEncode = getEncoding(str);
            String temp = new String(str.getBytes(strEncode), "UTF-8");
            return temp;
        }
        catch (java.io.IOException ex)
        {

            return null;
        }
    }

    /**
     * 判断输入字符是否为gb2312的编码格式
     * 
     * @param c 输入字符
     * @return 如果是gb2312返回真，否则返回假
     */
    public static boolean isGB2312(char c)
    {
        Character ch = new Character(c);
        String sCh = ch.toString();
        try
        {
            byte[] bb = sCh.getBytes("gb2312");
            if (bb.length > 1)
            {
                return true;
            }
        }
        catch (java.io.UnsupportedEncodingException ex)
        {
            return false;
        }
        return false;
    }

    /**
     * 判断字符串的编码
     * 
     * @param str
     * @return
     */
    public static String getEncoding(String str)
    {

        try
        {
            if (str.equals(new String(str.getBytes(Encoding.toEncoding(Encoding.GB2312)), Encoding.toEncoding(Encoding.GB2312))))
            {
                return Encoding.toEncoding(Encoding.GB2312);
            }
            if (str.equals(new String(str.getBytes(Encoding.toEncoding(Encoding.GBK)), Encoding.toEncoding(Encoding.GBK))))
            {
                return Encoding.toEncoding(Encoding.GBK);
            }
            if (str.equals(new String(str.getBytes(Encoding.toEncoding(Encoding.BIG5)), Encoding.toEncoding(Encoding.BIG5))))
            {
                return Encoding.toEncoding(Encoding.BIG5);
            }
            if (str.equals(new String(str.getBytes(Encoding.toEncoding(Encoding.UTF8)), Encoding.toEncoding(Encoding.UTF8))))
            {
                return Encoding.toEncoding(Encoding.UTF8);
            }
            if (str.equals(new String(str.getBytes(Encoding.toEncoding(Encoding.UNICODE)), Encoding.toEncoding(Encoding.UNICODE))))
            {
                return Encoding.toEncoding(Encoding.UNICODE);
            }
            if (str.equals(new String(str.getBytes(Encoding.toEncoding(Encoding.EUC_KR)), Encoding.toEncoding(Encoding.EUC_KR))))
            {
                return Encoding.toEncoding(Encoding.EUC_KR);
            }
            if (str.equals(new String(str.getBytes(Encoding.toEncoding(Encoding.SJIS)), Encoding.toEncoding(Encoding.SJIS))))
            {
                return Encoding.toEncoding(Encoding.SJIS);
            }
            if (str.equals(new String(str.getBytes(Encoding.toEncoding(Encoding.EUC_JP)), Encoding.toEncoding(Encoding.EUC_JP))))
            {
                return Encoding.toEncoding(Encoding.EUC_JP);
            }
            if (str.equals(new String(str.getBytes(Encoding.toEncoding(Encoding.ASCII)), Encoding.toEncoding(Encoding.ASCII))))
            {
                return Encoding.toEncoding(Encoding.ASCII);
            }
            if (str.equals(new String(str.getBytes(Encoding.toEncoding(Encoding.UNKNOWN)), Encoding.toEncoding(Encoding.UNKNOWN))))
            {
                return Encoding.toEncoding(Encoding.UNKNOWN);
            }

        }
        catch (UnsupportedEncodingException e)
        {
        }
        return Encoding.toEncoding(Encoding.UTF8);
        // String encode = "GB2312";
        // try {
        // if (str.equals(new String(str.getBytes(encode), encode))) {
        // String s = encode;
        // return s;
        // }
        // } catch (Exception exception) {
        // }
        // encode = "ISO-8859-1";
        // try {
        // if (str.equals(new String(str.getBytes(encode), encode))) {
        // String s1 = encode;
        // return s1;
        // }
        // } catch (Exception exception1) {
        // }
        // encode = "UTF-8";
        // try {
        // if (str.equals(new String(str.getBytes(encode), encode))) {
        // String s2 = encode;
        // return s2;
        // }
        // } catch (Exception exception2) {
        // }
        // encode = "GBK";
        // try {
        // if (str.equals(new String(str.getBytes(encode), encode))) {
        // String s3 = encode;
        // return s3;
        // }
        // } catch (Exception exception3) {
        // }
        // return "";
    }

    public static String tranEncode(String str)
    {
        try
        {
            String strEncode = getEncoding(str);
            String temp = null;

            // 如果字符串原来的编码是“GB2312”、“GBK”、“UTF-8”中的一种，就用原来的编码将其转回；
            // 否则的话统一用“GB2312”转码。
            if (strEncode.equals(Encoding.toEncoding(Encoding.GB2312)) || strEncode.equals(Encoding.toEncoding(Encoding.GBK)) || strEncode.equals(Encoding.toEncoding(Encoding.UTF8)))
            {
                temp = new String(str.getBytes(strEncode), strEncode);
            }
            else
            {
                temp = new String(str.getBytes(strEncode), "GB2312");
            }
            return temp;
        }
        catch (java.io.IOException ex)
        {

            return null;
        }
    }

}
