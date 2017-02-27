package com.rockchips.mediacenter.utils;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

/**
 * 读取MP3文件ID3V1信息
 */
public class MP3HeadInfoReader
{

    /**
     * 读取MP3头文件信息
     * @param f 文件对象
     * @return 头文件信息
     */
    public static String[] getHeadInfo(File f)
    {
        DataInputStream dis = null;
        String[] headInfo = null;
        try
        {
            byte[] buffer = new byte[128];
            dis = new DataInputStream(new FileInputStream(f));
            long size = f.length();
            if (size <= 128)
            {
                return headInfo;
            }
            long skipLength = dis.skip(size - 128);
            int dataRead = -1;
            if (skipLength > 0)
            {
                dataRead = dis.read(buffer);
            }
            if (dataRead > 0)
            {
                headInfo = read(buffer);
            }
        }
        catch (Exception e)
        {
            Log.e("Exception ", e.toString());
        }
        finally
        {
            if (dis != null)
            {
                try
                {
                    dis.close();
                }
                catch (Exception e)
                {
                    Log.e("Exception ", e.toString());
                }
            }
        }
        return headInfo;
    }

    /**
     * 读取MP3 IDV1TAG 信息
     * @param data byte array of idv1 tag. It's length must be 128.
     * @return String[] head info of the mp3 file. The order is:title,artist,special,year,genre
     * @see [类、类#方法、类#成员]
     */
    private static String[] read(byte[] data)
    {
        String[] str = new String[5];
        final String iD3Genre[] =
        {
                // standard genre
                // 0
                "Blues",
                // 1
                "ClassicRock",
                // 2
                "Country",
                // 3
                "Dance",
                // 4
                "Disco",
                // 5
                "Funk",
                // 6
                "Grunge",
                // 7
                "Hip-Hop",
                // 8
                "Jazz",
                // 9
                "Metal",
                // 10
                "NewAge",
                // 11
                "Oldies",
                // 12
                "Other",
                // 13
                "Pop",
                // 14
                "R&B",
                // 15
                "Rap",
                // 16
                "Reggae",
                // 17
                "Rock",
                // 18
                "Techno",
                // 19
                "Industrial",
                // 20
                "Alternative",
                // 21
                "Ska",
                // 22
                "DeathMetal",
                // 23
                "Pranks",
                // 24
                "Soundtrack",
                // 25
                "Euro-Techno",
                // 26
                "Ambient",
                // 27
                "Trip-Hop",
                // 28
                "Vocal",
                // 29
                "Jazz+Funk",
                // 30
                "Fusion",
                // 31
                "Trance",
                // 32
                "Classical",
                // 33
                "Instrumental",
                // 34
                "Acid",
                // 35
                "House",
                // 36
                "Game",
                // 37
                "SoundClip",
                // 38
                "Gospel",
                // 39
                "Noise",
                // 40
                "AlternRock",
                // 41
                "Bass",
                // 42
                "Soul",
                // 43
                "Punk",
                // 44
                "Space",
                // 45
                "Meditative",
                // 46
                "InstrumentalPop",
                // 47
                "InstrumentalRock",
                // 48
                "Ethnic",
                // 49
                "Gothic",
                // 50
                "Darkwave",
                // 51
                "Techno-Industrial",
                // 52
                "Electronic",
                // 53
                "Pop-Folk",
                // 54
                "Eurodance",
                // 55
                "Dream",
                // 56
                "SouthernRock",
                // 57
                "Comedy",
                // 58
                "Cult",
                // 59
                "Gangsta",
                // 60
                "Top40",
                // 61
                "ChristianRap",
                // 62
                "Pop/Funk",
                // 63
                "Jungle",
                // 64
                "NativeAmerican",
                // 65
                "Cabaret",
                // 66
                "NewWave",
                // 67
                "Psychadelic",
                // 68
                "Rave",
                // 69
                "Showtunes",
                // 70
                "Trailer",
                // 71
                "Lo-Fi",
                // 72
                "Tribal",
                // 73
                "AcidPunk",
                // 74
                "AcidJazz",
                // 75
                "Polka",
                // 76
                "Retro",
                // 77
                "Musical",
                // 78
                "Rock&Roll",
                // 79
                "HardRock",
        /* Extended genres */
        };
        if (data.length != 128)
        {
            return str;
        }
        // #mdebug
        // # Log.out("[MP3HeadInfoReader]got last 128 bit begin to analyze", Log.LOG_INFO);
        // #enddebug
        if (data[0] == 'T' && data[1] == 'A' && data[2] == 'G')
        {
            // #if SupportGB2312 == "false"
            // # byte[] tempByte;
            // #endif
            try
            {
                // #if SupportGB2312 == "true"
                str[0] = new String(data, 3, 30, "gb2312").trim();
                str[1] = new String(data, 33, 30, "gb2312").trim();
                str[2] = new String(data, 63, 30, "gb2312").trim();
                str[3] = new String(data, 93, 4, "gb2312").trim();
                // str[4] = new String(data, 97, 28, "gb2312").trim();
                if (data[127] == -1)
                {
                    str[4] = "";
                }
                else
                {
                    if (data[127] < 80)
                    {
                        str[4] = iD3Genre[data[127]];
                    }
                    else
                    {
                        str[4] = "";
                    }

                }
                // #elif SupportGB2312 == "false"
                // # tempByte = new byte[30];
                // # System.arraycopy(data, 3, tempByte, 0, 30);
                // # str[0] = GB2U.parseToString(tempByte).trim();
                // #
                // # System.arraycopy(data, 33, tempByte, 0, 30);
                // # str[1] = GB2U.parseToString(tempByte).trim();
                // #
                // # System.arraycopy(data, 63, tempByte, 0, 30);
                // # str[2] = GB2U.parseToString(tempByte).trim();
                // #
                // # tempByte = new byte[4];
                // # System.arraycopy(data, 93, tempByte, 0, 4);
                // # str[3] = GB2U.parseToString(tempByte).trim();
                // # if (data[127] == -1)
                // # {
                // # str[4] = "";
                // # }
                // # else
                // # {
                // # if (data[127] < 80)
                // # {
                // # str[4] = iD3Genre[data[127]];
                // # }
                // # else
                // # {
                // # str[4] = "";
                // # }
                // # }
                // #endif
            }
            catch (Exception e)
            {
                Log.e("Exception ", e.toString());
            }

        }
        return str;
    }

    /**
     * <一句话功能简述>如果是MP3文件，解析图片信息 <功能详细描述>
     * 
     * @param path 该音乐文件的存放地址
     * @return vector 1、图片类型 2、图片的二进制数据
     * 
     * @see [类、类#方法、类#成员]
     */
    public static List<Object> getPictureFromMp3(String path)
    {
        // Vector v = new Vector();
        List<Object> v = new ArrayList<Object>();
        File fc = null;
        try
        {
            fc = new File(path);
            // 解析
            v = getPictureInfo(fc);
        }
        catch (Exception e)
        {
            return null;
        }
        // finally
        // {
        // // if (null != fc)
        // // {
        // // try
        // // {
        // // }
        // // catch (IOException e)
        // // {
        // // }
        // // }
        // }
        return v;
    }

    /**
     * <一句话功能简述>读取MP3 ID3v2 中的图片信息 <功能详细描述>
     * 
     * @param fConn A connection of the mp3 file.
     * @return vector 1、picture type；2、picture head info of the mp3 file.
     * @see [类、类#方法、类#成员]
     */
    public static List<Object> getPictureInfo(File fConn)
    {
        // #mdebug
        // # Log.out("[MP3HeadInfoReader]getPictureInfo : " , Log.LOG_INFO);
        // #enddebug
        // Vector v = new Vector();
        List<Object> v = new ArrayList<Object>();
        DataInputStream dis = null;
        byte[] pictureInfo = null;
        // 头信息的大小
        int headSize = 0;
        try
        {
            byte[] tagHead = new byte[10];
            dis = new DataInputStream(new FileInputStream(fConn));
            long size = fConn.length();
            if (size <= 10)
            {
                return v;
            }
            int dataRead = -1;
            // 读取标签头，10个字节

            dataRead = dis.read(tagHead);
            if (dataRead > 0)
            {
                // 解析标签头，获得整个头信息的大小
                headSize = readTagHeadInfo(tagHead);
                /**
                 * Begin: Added by w81004644, 2014/5/20 DTS:DTS2014051504840 reason: 容错处理，避免应用出现异常。读到的值过大，会导致dalvikvm内存溢出。
                 */
                if (headSize > 102400)
                {
                    headSize = 102400;
                    Log.i("<><><>---", "[MP3HeadInfoReader abnormal] revise headSize =  " + headSize);
                }
                /** End: Added by w81004644, 2014/5/20 DTS:DTS2014051504840 */
            }
            dataRead = -1;
            if (headSize - 10 > 0)
            {
                // 读取标签帧的信息，这里会是多个标签帧，要循环解析
                tagHead = new byte[headSize - 10];
                dataRead = dis.read(tagHead);
                if (dataRead > 0)
                {
                    // 解析标签帧
                    pictureInfo = readPicInfo(tagHead);
                }
                // 去掉描述图片类型和图片描述等的信息

                v = delSpilth(pictureInfo);
            }
        }
        catch (Exception e)
        {
            // Log.out(
            // "[MP3HeadInfoReader.getPictureInfo]:get picture info failed.",
            // Log.LOG_SERIOUS, false);
            // Log.out("[MP3HeadInfoReader.getPictureInfo]:Exception = "
            // + e.toString(), Log.LOG_SERIOUS, false);
            Log.e("Exception ", e.getLocalizedMessage());
        }
        finally
        {
            if (dis != null)
            {
                try
                {
                    dis.close();
                }
                catch (Exception e)
                {
                    // Log
                    // .out(
                    // "[MP3HeadInfoReader.getPictureInfo]:close stream failed.",
                    // Log.LOG_SERIOUS, false);
                    // Log.out("[MP3HeadInfoReader.getPictureInfo]:Exception = "
                    // + e.toString(), Log.LOG_SERIOUS, false);
                    Log.e("Exception ", e.getLocalizedMessage());
                }
            }
        }
        return v;
    }

    public static String printHexString(byte[] msg)
    {
        int nWordOfLine = 0, newStart = 0, nTotal = 0;
        StringBuffer strTemp = new StringBuffer();
        StringBuffer tmp = new StringBuffer(16);
        int length = msg.length;
        strTemp.append("\n");
        int start = 0;
        int glowValue = 0;
        for (int i = 0; i < length; i++)
        {
            strTemp.append(Integer.toHexString(msg[i] >> 4 & 0xf));
            strTemp.append(Integer.toHexString(msg[i] & 0xf));
            strTemp.append(' ');
            tmp.append((char) (msg[i]));
            nWordOfLine++;

            if (nWordOfLine >= 16)
            {
                strTemp.append("\t");
                strTemp.append(tmp);
                strTemp.append("\n");
                nWordOfLine = 0;
                tmp.delete(0, tmp.length());
            }
        }
        if (nWordOfLine > 0)
        {
            int nFill = 16 - nWordOfLine;
            for (int i = 0; i < nFill; i++)
            {
                strTemp.append("   ");
            }
            strTemp.append("\t");
            strTemp.append(tmp);
            strTemp.append("\n");
        }
        return strTemp.toString();
    }

    /**
     * <一句话功能简述>解析MP3 ID3v2 中的头信息的标签头，获得整个头信息的大小 <功能详细描述>
     * 
     * @param data 标签头
     * 
     * @return 头信息的大小(包括标签头的10个字节)
     * @see [类、类#方法、类#成员]
     */
    public static int readTagHeadInfo(byte[] data)
    {
        int sizeTotal = 0;
        // String size = "";
        byte[] size = new byte[4];
        // char[] c = new char[4];
        if (data.length != 10)
        {
            return sizeTotal;
        }
        // 如果标签头前三个字节不是“ID3”，则认为标签不存在
        if (data[0] == 'I' && data[1] == 'D' && data[2] == '3')
        {
            try
            {
                System.arraycopy(data, 6, size, 0, 4);
            }
            catch (Exception e)
            {
                // Log
                // .out(
                // "[MP3HeadInfoReader.readTagHeadInfo]:read tag's head info failed.",
                // Log.LOG_SERIOUS, false);
                // Log.out("[MP3HeadInfoReader.readTagHeadInfo]:Exception = "
                // + e.toString(), Log.LOG_SERIOUS, false);
                return 0;
            }
            // c = size;
            sizeTotal = (size[0] & 0x7F) * 0x200000 + (size[1] & 0x7F) * 0x4000 + (size[2] & 0x7F) * 0x80 + (size[3] & 0x7F);

            return sizeTotal;
        }
        return 0;
    }

    /**
     * <一句话功能简述>获取ID3V2中的图片信息 <功能详细描述>
     * 
     * @param data 标签帧,多个标签帧
     * 
     * @return 图片内容
     * @see [类、类#方法、类#成员]
     */
    public static byte[] readPicInfo(byte[] data)
    {
        // 打印标签帧数据

        // for (int i = 0; i < data.length / 4; i++)
        // {
        // if ((i + 1) % 20 == 0)
        // {
        // }
        // }
        // 标签帧的大小（包括帧头）
        int sizeTotal = 0;
        byte[] size = new byte[4];
        byte[] content = null;
        int[] size1 = new int[4];
        if (data == null || data.length <= 10)
        {
            return content;
        }
        // #mdebug
        // # Log.out("begin try" , 1);
        // #enddebug
        try
        {
            // 取得标签帧的二进制数据

            System.arraycopy(data, 4, size, 0, 4);
            // #mdebug
            // # Log.out("1" , 1);
            // #enddebug
            // 如果数据超过byte的最大值，则在下面的for循环里赋值给char，再计算大小
            for (int i = 0; i < size.length; i++)
            {
                size1[i] = size[i];
                if (size[i] < 0)
                {
                    size1[i] = (char) (256 + size[i]);
                }
            }
            // #mdebug
            // # Log.out("MP3HeadInfoReader ->readPicInfo 1", 1);
            // #enddebug
            if (size[0] < 0 || size[1] < 0 || size[2] < 0 || size[3] < 0)
            {
                // 如果size数组里出现负数，则说明是超过byte长度导致，这时，用计算过后的size1数组来计算大小
                sizeTotal = (size1[0] << 30 << 2) + size1[1] * 0x10000 + size1[2] * 0x100 + size1[3];
            }
            else
            {
                sizeTotal = (size[0] << 30 << 2) + size[1] * 0x10000 + size[2] * 0x100 + size[3];
                // #mdebug
                // # Log.out("3", 1);
                // #enddebug
            }
            /**
             * Begin: Added by w81004644, 2014/5/20 DTS:DTS2014051504840 reason: 容错处理，避免应用出现异常。读到的值过大，会导致dalvikvm内存溢出。
             */
            if (sizeTotal <= 0 || sizeTotal >= (data.length - 10))
            {
                Log.i("<><><>---", "[sizeTotal abnormal]: " + sizeTotal);
                return content;
            }
            /** End: Added by w81004644, 2014/5/20 DTS:DTS2014051504840 */

            // 如果标签头前四个字节是“APIC”，则解析图片信息

            if (data[0] == 'A' && data[1] == 'P' && data[2] == 'I' && data[3] == 'C')
            {
                // #mdebug
                // # Log.out("begin size" + sizeTotal , 1);
                // #enddebug
                content = new byte[sizeTotal];
                // #mdebug
                // # Log.out("new byte is over" , 1);
                // #enddebug
                System.arraycopy(data, 10, content, 0, sizeTotal);
                // #mdebug
                // # Log.out("4", 1);
                // #enddebug
            }

            else
            {
                // 如果不是APIC标签帧，则去掉已经解析的部分，再次递归调用此方法解析，直到解析出APIC为止
                /**
                 * Begin: Added by w81004644, 2014/5/20 DTS:DTS2014051504840 reason: 容错处理，避免应用出现异常。读到的值过大，会导致dalvikvm内存溢出。
                 */
                int sizeLeft = data.length - sizeTotal - 10;
                if (sizeLeft <= 0 || sizeLeft >= 102400)
                {
                    Log.i("<><><>---", "[sizeLeft abnormal]: " + sizeLeft);
                    return content;
                }

                byte[] datas = new byte[sizeLeft];
                /** End: Added by w81004644, 2014/5/20 DTS:DTS2014051504840 */
                System.arraycopy(data, sizeTotal + 10, datas, 0, datas.length);
                content = readPicInfo(datas);
            }
            return content;
        }
        catch (Exception e)
        {
            return content;
        }

    }

    /**
     * 
     * [一句话功能简述]<BR>
     * 去掉Text encoding $xx 一个字节，以00表示 MIME type <text string> $00 一个字节 Picture type $xx 一个字节，以00表示 Description <text string according to encoding> $00
     * (00) 以00开头，以00结束 取后面的 Picture data <binary data> [功能详细描述]
     * 
     * @param data 帧内容
     * 
     * @return Vector 第一个数据表示图片类型，String类型；第二个是图片内容，byte数组
     */
    public static List<Object> delSpilth(byte[] data)
    {
        List<Object> v = new ArrayList<Object>();

        int index = -1;
        byte[] b = null;
        byte num1 = 00;
        if (data != null)
        {
            for (int i = 1; i < data.length; i++)
            {
                if (data[i] == num1)
                {
                    index = i;
                    break;
                }
            }
            // 解析MIME type 获取图片的类型，bmp、png、jpg、gif
            byte[] typeContent = new byte[index - 1];
            System.arraycopy(data, 1, typeContent, 0, index - 1);
            String type = getPicType(typeContent);
            v.add(type);
            // v.addElement(type);

            for (int i = index + 2; i < (data.length - 1); i++)
            {
                if ((data[i] == num1) && (data[i + 1] != num1))
                {
                    index = i;
                    break;
                }
            }
            b = new byte[data.length - index - 1];
            System.arraycopy(data, index + 1, b, 0, b.length);
            v.add(b);
            // v.addElement(b);
        }
        return v;
    }

    /**
     * 
     * [一句话功能简述]<BR>
     * 解析MIME type ，获得图片的类型 [功能详细描述]
     * 
     * @param data 表示 MIME type 的数据
     * 
     * @return picture type
     */
    public static String getPicType(byte[] data)
    {
        String picType = "";
        StringBuffer type = new StringBuffer();
        char[] typeInfo = new char[data.length];

        // 如果数据超过byte的最大值，则在下面的for循环里赋值给char，再计算大小
        for (int i = 0; i < data.length; i++)
        {
            typeInfo[i] = (char) data[i];
            if (data[i] < 0)
            {
                typeInfo[i] = (char) (256 + data[i]);
            }
        }
        type.append(typeInfo);
        picType = type.toString();
        if (picType.equals("image/jpeg"))
        {
            picType = "jpeg";
        }
        else if (picType.equals("image/bmp"))
        {
            picType = "bmp";
        }
        else if (picType.equals("image/png"))
        {
            picType = "png";
        }
        else if (picType.equals("image/gif"))
        {
            picType = "gif";
        }
        else if (picType.equals("image/jpg"))
        {
            picType = "jpg";
        }
        else
        {
            picType = "jpeg";
        }
        return picType;
    }
}
