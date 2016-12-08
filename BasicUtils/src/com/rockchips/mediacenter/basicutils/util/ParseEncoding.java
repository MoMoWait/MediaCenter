/*
 * 文件名: ParseEncoding.java 版 权： Copyright Huawei Tech. Co. Ltd. All Rights
 * Reserved. 描 述: [该类的简要描述] 创建人: w00138133 创建时间:2009-11-28 修改人： 修改时间:
 * 修改内容：[修改内容]
 */
package com.rockchips.mediacenter.basicutils.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * [一句话功能简述]<BR>
 * [功能详细描述]
 * 
 * @author w00138133
 * @version [MusicClientV100R001C08, 2009-11-28]
 */
public final class ParseEncoding extends Encode
{

    private static ParseEncoding parseEncoding;

    private ParseEncoding()
    {
        super();
        gb2312format = new int[94][94];
        gbkformat = new int[126][191];
        big5format = new int[94][158];
        euckrformat = new int[94][94];
        jpformat = new int[94][94];

        // 初始化编码格式
        init();
    }

    /**
     * 初始化 [功能详细描述]
     */
    public static void initParseEncoding()
    {
        if(parseEncoding == null){
            parseEncoding = new ParseEncoding();
        }
    }

    /**
     * 获取编码格式
     * 
     * @param path 文件名
     * @return 格式
     */
    public static String getEncoding(final String path)
    {
        return check(parseEncoding.getEncodeValue(path));
    }

    /**
     * 获取编码格式
     * 
     * @param in 流
     * @return 格式
     */
    public static String getEncoding(final InputStream in)
    {
        return check(parseEncoding.getEncodeValue(in));
    }

    /**
     * 获取编码格式
     * 
     * @param buffer 字节数组
     * @return 格式
     */
    public static String getEncoding(final byte[] buffer)
    {
        return check(parseEncoding.getEncodeValue(buffer));
    }

    /**
     * 获取编码格式
     * 
     * @param url 地址
     * @return 格式
     */
    public static String getEncoding(final URL url)
    {
        return check(parseEncoding.getEncodeValue(url));
    }

    private static String check(final int result)
    {
        if (result == -1)
        {
            return javaname[UNKNOWN];
        }
        return javaname[result];
    }

    /**
     * 解析指定字符串路径编码所用格式
     * 
     * @param path
     * @return
     */
    private int getEncodeValue(String path)
    {
        int express = UNKNOWN;
        if (path.startsWith("http://"))
        {
            try
            {
                express = getEncodeValue(new URL(path));
            }
            catch (MalformedURLException e)
            {
                express = -1;
            }
        }
        else
        {
            express = getEncodeValue(new File(path));
        }
        return express;
    }

    /**
     * 
     * 解析指定InputStream所用编码，返回或然率最高的编码类型数值
     * 
     * @param in InputStream
     * @return 编码类型数值
     */
    public int getEncodeValue(InputStream in)
    {
        byte[] rawtext = new byte[8192];
        int bytesread = 0;
        int byteoffset = 0;
        int express = UNKNOWN;
        InputStream stream = in;
        try
        {
            while ((bytesread = stream.read(rawtext,
                byteoffset,
                rawtext.length - byteoffset)) > 0)
            {
                byteoffset += bytesread;
            }
            stream.close();
            express = getEncodeValue(rawtext);
        }
        catch (Exception e)
        {
            express = -1;
        }
        return express;
    }

    /**
     * 解析指定url下数据所用编码，返回或然率最高的编码类型数值
     * 
     * @param url url
     * @return 编码类型数值
     */
    public int getEncodeValue(URL url)
    {

        InputStream stream;
        try
        {
            stream = url.openStream();
        }
        catch (IOException e)
        {
            stream = null;
            return -1;
        }

        return getEncodeValue(stream);
    }

    /**
     * 解析指定file所用编码，返回或然率最高的编码类型数值
     * 
     * @param file file
     * @return 编码类型数值
     */
    public int getEncodeValue(File file)
    {
        byte[] buffer;
        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream(file);
            buffer = read(fis);
        }
        catch (FileNotFoundException e)
        {
            buffer = null;
        }
        finally
        {
            if (null != fis)
            {
                try
                {
                    fis.close();
                }
                catch (IOException e)
                {
                }
            }
        }
        
        return getEncodeValue(buffer);
    }

    /**
     * 将inputstream转为byte[]
     * 
     * @param inputStream
     * @return
     */
    private final byte[] read(final InputStream inputStream)
    {
        byte[] arrayByte = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] bytes = new byte[8192];
        try
        {
            bytes = new byte[inputStream.available()];
            int read;
            while ((read = inputStream.read(bytes)) >= 0)
            {
                byteArrayOutputStream.write(bytes,
                    0,
                    read);
            }
            arrayByte = byteArrayOutputStream.toByteArray();
        }
        catch (IOException e)
        {
            return null;
        }
        return arrayByte;
    }

    /**
     * 解析指定byte[]所用编码，返回或然率最高的数值类型
     * 
     * @param content byte[]
     * @return 编码类型数值
     */
    public int getEncodeValue(byte[] content)
    {
        if (content == null)
        {
            return -1;
        }
        int[] scores;
        int index;
        int maxscore = 0;
        int encoding = UNKNOWN;
        scores = new int[TOTALT];
        // 分配或然率
        scores[GB2312] = gb2312probability(content);
        scores[GBK] = gbkprobability(content);
        scores[BIG5] = big5probability(content);
        scores[UTF8] = utf8probability(content);
        scores[UNICODE] = utf16probability(content);
        scores[EUC_KR] = euckrprobability(content);
        scores[ASCII] = asciiprobability(content);
        scores[SJIS] = sjisprobability(content);
        scores[EUC_JP] = eucjpprobability(content);
        scores[UNKNOWN] = 0;

        // 概率比较
        for (index = 0; index < TOTALT; index++)
        {
            if (scores[index] > maxscore)
            {
                // 索引
                encoding = index;
                // 最大几率
                maxscore = scores[index];
            }
        }
        // 返回或然率大于50%的数据
        if (maxscore <= 50)
        {
            encoding = UNKNOWN;
        }
        return encoding;
    }

    /**
     * gb2312数据或然率计算
     * 
     * @param content
     * @return
     */
    private int gb2312probability(byte[] content)
    {
        int i;
        int rawtextlen = 0;

        int dbchars = 1;
        int gbchars = 1;
        long gbformat = 0;
        long totalformat = 1;
        float rangeval = 0;
        float formatval = 0;
        int row;
        int column;

        // 检查是否在亚洲汉字范围内
        rawtextlen = content.length;
        for (i = 0; i < rawtextlen - 1; i++)
        {            
            if (content[i] < 0)
            {
                dbchars++;
                // 汉字GB码由两个字节组成，每个字节的范围是0xA1 ~ 0xFE
                if ((byte) 0xA1 <= content[i] && content[i] <= (byte) 0xF7
                    && (byte) 0xA1 <= content[i + 1]
                    && content[i + 1] <= (byte) 0xFE)
                {
                    gbchars++;
                    totalformat += 500;
                    row = content[i] + 256 - 0xA1;
                    column = content[i + 1] + 256 - 0xA1;
                    if (gb2312format[row][column] != 0)
                    {
                        gbformat += gb2312format[row][column];
                    }
                    else if (15 <= row && row < 55)
                    {
                        // 在gb编码范围
                        gbformat += 200;
                    }

                }
                i++;
            }
        }
        rangeval = 50 * ((float) gbchars / (float) dbchars);
        formatval = 50 * ((float) gbformat / (float) totalformat);

        return (int) (rangeval + formatval);
    }

    /**
     * gb2312或然率计算
     * 
     * @param content
     * @return
     */
    private int gbkprobability(byte[] content)
    {
        int i;
        int rawtextlen = 0;

        int dbchars = 1;
        int gbchars = 1;
        long gbformat = 0;
        long totalformat = 1;
        float rangeval = 0;
        float formatval = 0;
        int row;
        int column;
        rawtextlen = content.length;
        for (i = 0; i < rawtextlen - 1; i++)
        {            
            if (content[i] < 0)
            {
                dbchars++;
                // gb范围:(byte) 0xA1 <= content[i + 1]&& content[i + 1] <= (byte)
                // 0xFE
                if ((byte) 0xA1 <= content[i] && content[i] <= (byte) 0xF7
                    && (byte) 0xA1 <= content[i + 1]
                    && content[i + 1] <= (byte) 0xFE)
                {
                    gbchars++;
                    totalformat += 500;
                    row = content[i] + 256 - 0xA1;
                    column = content[i + 1] + 256 - 0xA1;
                    if (gb2312format[row][column] != 0)
                    {
                        gbformat += gb2312format[row][column];
                    }
                    else if (15 <= row && row < 55)
                    {
                        gbformat += 200;
                    }

                }
                // gb扩展区域:(((byte) 0x80 <= content[i + 1] && content[i + 1] <=
                // (byte) 0xFE)
                // || ((byte) 0x40 <= content[i + 1] && content[i + 1] <= (byte)
                // 0x7E))
                else if ((byte) 0x81 <= content[i]
                    && content[i] <= (byte) 0xFE
                    && (((byte) 0x80 <= content[i + 1] && content[i + 1] <= (byte) 0xFE) 
                    || ((byte) 0x40 <= content[i + 1] && content[i + 1] <= (byte) 0x7E)))
                {
                    gbchars++;
                    totalformat += 500;
                    row = content[i] + 256 - 0x81;
                    if (0x40 <= content[i + 1] && content[i + 1] <= 0x7E)
                    {
                        column = content[i + 1] - 0x40;
                    }
                    else
                    {
                        column = content[i + 1] + 256 - 0x40;
                    }
                    if (gbkformat[row][column] != 0)
                    {
                        gbformat += gbkformat[row][column];
                    }
                }
                i++;
            }
        }
        rangeval = 50 * ((float) gbchars / (float) dbchars);
        formatval = 50 * ((float) gbformat / (float) totalformat);
        return (int) (rangeval + formatval) - 1;
    }

    /**
     * 解析为big5的或然率
     * 
     * @param content
     * @return
     */
    private int big5probability(byte[] content)
    {
        int i;
        int rawtextlen = 0;
        int dbchars = 1;
        int bfchars = 1;
        float rangeval = 0;
        float formatval = 0;
        long bfformat = 0;
        long totalformat = 1;
        int row;
        int column;
        rawtextlen = content.length;
        for (i = 0; i < rawtextlen - 1; i++)
        {            
            if (content[i] < 0)
            {
                dbchars++;
                if ((byte) 0xA1 <= content[i]
                    && content[i] <= (byte) 0xF9
                    && (((byte) 0x40 <= content[i + 1] && content[i + 1] <= (byte) 0x7E) 
                    || ((byte) 0xA1 <= content[i + 1] && content[i + 1] <= (byte) 0xFE)))
                {
                    bfchars++;
                    totalformat += 500;
                    row = content[i] + 256 - 0xA1;
                    if (0x40 <= content[i + 1] && content[i + 1] <= 0x7E)
                    {
                        column = content[i + 1] - 0x40;
                    }
                    else
                    {
                        column = content[i + 1] + 256 - 0x61;
                    }
                    if (big5format[row][column] != 0)
                    {
                        bfformat += big5format[row][column];
                    }
                    else if (3 <= row && row <= 37)
                    {
                        bfformat += 200;
                    }
                }
                i++;
            }
        }
        rangeval = 50 * ((float) bfchars / (float) dbchars);
        formatval = 50 * ((float) bfformat / (float) totalformat);

        return (int) (rangeval + formatval);
    }

    /**
     * 在utf-8中的或然率
     * 
     * @param content
     * @return
     */
    private int utf8probability(byte[] content)
    {
        int score = 0;
        int i;
        int rawtextlen = 0;
        int goodbytes = 0;
        int asciibytes = 0;
        // 检查是否为汉字可接受范围
        rawtextlen = content.length;
        for (i = 0; i < rawtextlen; i++)
        {
            if ((content[i] & (byte) 0x7F) == content[i])
            {
                asciibytes++;
            }
            else if (-64 <= content[i] && content[i] <= -33
                && i + 1 < rawtextlen && -128 <= content[i + 1]
                && content[i + 1] <= -65)
            {
                goodbytes += 2;
                i++;
            }
            else if (-32 <= content[i] && content[i] <= -17
                && i + 2 < rawtextlen && -128 <= content[i + 1]
                && content[i + 1] <= -65 && -128 <= content[i + 2]
                && content[i + 2] <= -65)
            {
                goodbytes += 3;
                i += 2;
            }
        }

        if (asciibytes == rawtextlen)
        {
            return 0;
        }

        score = (int) (100 * ((float) goodbytes / (float) (rawtextlen - asciibytes)));
        // 如果不高于98则减少到零
        if (score > 98)
        {
            return score;
        }
        else if (score > 95 && goodbytes > 30)
        {
            return score;
        }
        else
        {
            return 0;
        }

    }

    /**
     * 检查为utf-16的或然率
     * 
     * @param content
     * @return
     */
    private int utf16probability(byte[] content)
    {

        if (content.length > 1
            && ((byte) 0xFE == content[0] && (byte) 0xFF == content[1])
            || ((byte) 0xFF == content[0] && (byte) 0xFE == content[1]))
        {
            return 100;
        }
        return 0;
    }

    /**
     * 检查为ascii的或然率
     * 
     * @param content
     * @return
     */
    private int asciiprobability(byte[] content)
    {
        int score = 75;
        int i;
        int rawtextlen;

        rawtextlen = content.length;

        for (i = 0; i < rawtextlen; i++)
        {
            if (content[i] < 0)
            {
                score = score - 5;
            }
            else if (content[i] == (byte) 0x1B)
            {
                // ESC (used by ISO 2022)
                score = score - 5;
            }
            if (score <= 0)
            {
                return 0;
            }
        }
        return score;
    }

    /**
     * 检查为euc_kr的或然率
     * 
     * @param content byte[]
     * @return 或然率数值
     */
    private int euckrprobability(byte[] content)
    {
        int i;
        int rawtextlen = 0;

        int dbchars = 1;
        int krchars = 1;
        long krformat = 0;
        long totalformat = 1;
        float rangeval = 0;
        float formatval = 0;
        int row;
        int column;
        rawtextlen = content.length;
        for (i = 0; i < rawtextlen - 1; i++)
        {
            if (content[i] < 0)
            {
                dbchars++;
                if ((byte) 0xA1 <= content[i] && content[i] <= (byte) 0xFE
                    && (byte) 0xA1 <= content[i + 1]
                    && content[i + 1] <= (byte) 0xFE)
                {
                    krchars++;
                    totalformat += 500;
                    row = content[i] + 256 - 0xA1;
                    column = content[i + 1] + 256 - 0xA1;
                    if (euckrformat[row][column] != 0)
                    {
                        krformat += euckrformat[row][column];
                    }
                    else if (15 <= row && row < 55)
                    {
                        krformat += 0;
                    }

                }
                i++;
            }
        }
        rangeval = 50 * ((float) krchars / (float) dbchars);
        formatval = 50 * ((float) krformat / (float) totalformat);

        return (int) (rangeval + formatval);
    }

    private int eucjpprobability(byte[] content)
    {
        int i;
        int rawtextlen = 0;

        int dbchars = 1;
        int jpchars = 1;
        long jpFormat = 0;
        long totalformat = 1;
        float rangeval = 0;
        float formatval = 0;
        int row;
        int column;

        rawtextlen = content.length;
        for (i = 0; i < rawtextlen - 1; i++)
        {
            if (content[i] < 0)
            {
                dbchars++;
                if ((byte) 0xA1 <= content[i] && content[i] <= (byte) 0xFE
                    && (byte) 0xA1 <= content[i + 1]
                    && content[i + 1] <= (byte) 0xFE)
                {
                    jpchars++;
                    totalformat += 500;
                    row = content[i] + 256 - 0xA1;
                    column = content[i + 1] + 256 - 0xA1;
                    if (jpformat[row][column] != 0)
                    {
                        jpFormat += jpformat[row][column];
                    }
                    else if (15 <= row && row < 55)
                    {
                        jpFormat += 0;
                    }

                }
                i++;
            }
        }
        rangeval = 50 * ((float) jpchars / (float) dbchars);
        formatval = 50 * ((float) jpFormat / (float) totalformat);

        return (int) (rangeval + formatval);
    }

    private int sjisprobability(byte[] content)
    {
        int i;
        int rawtextlen = 0;

        int dbchars = 1;
        int jpchars = 1;
        long jpFormat = 0;
        long totalformat = 1;
        float rangeval = 0;
        float formatval = 0;
        int row;
        int column;
        int adjust;

        rawtextlen = content.length;
        for (i = 0; i < rawtextlen - 1; i++)
        {
            if (content[i] < 0)
            {
                dbchars++;
                if (i + 1 < content.length
                    && (((byte) 0x81 <= content[i] && content[i] <= (byte) 0x9F) 
                    || ((byte) 0xE0 <= content[i] && content[i] <= (byte) 0xEF))
                    && (((byte) 0x40 <= content[i + 1] && content[i + 1] <= (byte) 0x7E) 
                    || ((byte) 0x80 <= content[i + 1] && content[i + 1] <= (byte) 0xFC)))
                {
                    jpchars++;
                    totalformat += 500;
                    row = content[i] + 256;
                    column = content[i + 1] + 256;
                    if (column < 0x9f)
                    {
                        adjust = 1;
                        if (column > 0x7f)
                        {
                            column -= 0x20;
                        }
                        else
                        {
                            column -= 0x19;
                        }
                    }
                    else
                    {
                        adjust = 0;
                        column -= 0x7e;
                    }
                    if (row < 0xa0)
                    {
                        row = ((row - 0x70) << 1) - adjust;
                    }
                    else
                    {
                        row = ((row - 0xb0) << 1) - adjust;
                    }

                    row -= 0x20;
                    column = 0x20;
                    if (row < jpformat.length && column < jpformat[row].length
                        && jpformat[row][column] != 0)
                    {
                        jpFormat += jpformat[row][column];
                    }
                    i++;
                }                
            }
        }
        rangeval = 50 * ((float) jpchars / (float) dbchars);
        formatval = 50 * ((float) jpFormat / (float) totalformat);

        return (int) (rangeval + formatval) - 1;
    }
}
