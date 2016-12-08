package com.rockchips.mediacenter.basicutils.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class ID3V2
{

    private File file;

    private int tagSize = -1;

    // 存储ID3V2的帧，比如TALB等
    private Map<String, byte[]> tags = new HashMap<String, byte[]>();

//    public static void main(String[] args)
//    {
//
//        File f = new File("仰望.mp3");
//        ID3V2 id3v2 = new ID3V2(f);
//
//        try
//        {
//            id3v2.initialize();
//        }
//        catch (Exception e)
//        {
//        }
//
//        /**
//         * System.out.println(id3v2.tit2()); System.out.println(id3v2.tpe1());
//         * System.out.println(id3v2.talb());
//         */
//    }

    public ID3V2(File file)
    {
        this.file = file;
    }

    public void initialize() throws Exception, IOException
    {
        if (file == null)
        {
            throw new NullPointerException("MP3 file is not found");
        }
            
        FileInputStream is = null;
        try
        {
            is = new FileInputStream(file);
    
            byte[] header = new byte[10];
            is.read(header); // 判断是否是合法的ID3V2头
    
            if (header[0] != 'I' || header[1] != 'D' || header[2] != '3')
            {
//                throw new Exception("not invalid mp3 ID3 tag");
                is.close();
                is = null;
                return;
            }
    
            // 计算ID3V2的帧大小
            tagSize = (header[9] & 0xff) + ((header[8] & 0xff) << 7) + ((header[7] & 0xff) << 14) + ((header[6] & 0xff) << 21);
            int pos = 10;
            while (pos < tagSize)
            {
                byte[] tag = new byte[10];
    
                // 读取ID3V2的帧头，如果tag[0]=0，则跳出循环，结束解析ID3V2
                is.read(tag);
    
                if (tag[0] == 0)
                {
                    break;
                }
    
                String tagName = new StringBuffer().append((char) tag[0]).append((char) tag[1]).append((char) tag[2]).append((char) tag[3]).toString();
                // 计算ID3V2帧的大小，不包括前面的帧头大小
    
                byte[] size = new byte[]
                { tag[4], tag[5], tag[6], tag[7] };
                int length = getDataSize(size);
    
                /**
                 * int length = ((tag[4] & 0xff) << 24) + ((tag[5] & 0xff) << 16) +
                 * ((tag[6] & 0xff) << 8) + tag[7];
                 */
    
                if (length <= 0)
                {
                    continue;
                }
    
                byte[] data = new byte[length];
                is.read(data); // 将帧头和帧体存储在HashMap中
    
                if (tagName.equalsIgnoreCase("TIT2") || tagName.equalsIgnoreCase("TALB") || tagName.equalsIgnoreCase("TPE1"))
                {
                    tags.put(tagName, data);
                }
    
                // tags.put(tagName, data);
                pos = pos + length + 10;
            }
        }
        catch(IOException e)
        {
        }
        finally
        {
            if (is != null)
            {
                try
                {
                    is.close();
                }
                catch(IOException e1)
                {
                }
                is = null;
            }
        }
    }

    private int getDataSize(byte[] size)
    {
        int s = 0;
        final int mask = 0xFF;
        for (int i = 0; i < size.length; i++)
        {
            int tmp = size[i];
            tmp &= mask;
            s += (tmp << (24 - 8 * i));
        }
        return s;
    }

    public int getTagSize()
    {
        return tagSize;
    }

    public String tit2()
    {
        return getTagText("TIT2");
    }

    public String talb()
    {
        return getTagText("TALB");
    }

    public String tpe1()
    {
        return getTagText("TPE1");
    }

    private String getTagText(String tag)
    {
        byte[] data = (byte[]) tags.get(tag);

        if (data == null)
        {
            return "";
        }

        // 查询帧体的编码方式
        // String encoding = encoding(data[0]);
        String encoding = "gb2312";

        try
        {
            return new String(data, 1, data.length - 1, encoding);
        }
        catch (UnsupportedEncodingException e)
        {
        }
        return null;
    }

    public String encoding(byte data)
    {
        String encoding = null;
        switch (data)
        {
            case 0:
                encoding = "ISO-8859-1";
                break;
            case 1:
                encoding = "UTF-16";
                break;
            case 2:
                encoding = "UTF-16BE";
                break;
            case 3:
                encoding = "UTF-8";
                break;
            default:
                encoding = "ISO-8859-1";
        }
        return encoding;
    }
}
