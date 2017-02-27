package com.rockchips.mediacenter.utils;

/*
 * 文 件 名:  Encoding.java
 * 版    权:  Huawei Technologies Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  yangyang
 * 修改时间:  2011-4-23
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */

/**
 * [一句话功能简述]<BR>
 * [功能详细描述]
 * 
 * @author  yangyang
 * @version  [版本号, 2011-4-23]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class Encoding
{
    /**
     * 支持的字符格式。GB2312
     */
    public static final int GB2312 = 0;
    /**
     * 支持的字符格式。GBK
     */
    public static final int GBK = 1;
    /**
     * 支持的字符格式。BIG5
     */
    public static final int BIG5 = 2;
    /**
     * 支持的字符格式。UTF8
     */
    public static final int UTF8 = 3;
    /**
     * 支持的字符格式。UNICODE
     */
    public static final int UNICODE = 4;
    /**
     * 支持的字符格式。EUC_KR
     */
    public static final int EUC_KR = 5;
    /**
     * 支持的字符格式。SJIS
     */
    public static final int SJIS = 6;
    /**
     * 支持的字符格式。EUC_JP
     */
    public static final int EUC_JP = 7;
    /**
     * 支持的字符格式。ASCII
     */
    public static final int ASCII = 8;
    /**
     * UNKNOWN
     */
    public static final int UNKNOWN = 9;
    /**
     * 支持的字符格式。TOTALT
     */
    public static final int TOTALT = 10;
    /**
     * 支持的字符格式。SIMP
     */
    public static final int SIMP = 0;
    /**
     * 支持的字符格式。TRAD
     */
    public static final int TRAD = 1;

    /**
     * 解析名称用
     */
    static String[] javaname = new String[TOTALT];

    /**
     * 初始化
     */
    public Encoding()
    {
        javaname[GB2312] = "GB2312";
        javaname[GBK] = "GBK";
        javaname[BIG5] = "BIG5";
        javaname[UTF8] = "UTF-8";
        javaname[UNICODE] = "UTF-16";
        javaname[EUC_KR] = "EUC-KR";
        javaname[SJIS] = "SJIS";
        javaname[EUC_JP] = "EUC_JP";
        javaname[ASCII] = "ASCII";
        //可以根据应用将默认设为UTF-8?
        javaname[UNKNOWN] = "ISO8859-1";
    }

    /**
     * 转码
     * 
     * @param type 类型
     * @return String
     */
    public static String toEncoding(final int type)
    {
        return javaname[type];
    }
}
