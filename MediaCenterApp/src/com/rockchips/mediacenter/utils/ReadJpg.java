package com.rockchips.mediacenter.utils;

//JPG 文件中的读入后的 EXIFF 信息保存到这个结构体中   

// 读取 EXIF 过程中需要的结构体

class tag_Section
{

    long Data;

    int Type;

    long Size;

}

// ////////////////////////////////////////////////////////////////////////
public class ReadJpg
{

    public static TagExifInfo m_pExifInfo = new TagExifInfo(); //

    static int m_MotorolaOrder = 0; //

    static int m_ExifImageWidth = 0; //

    public static void setBytes(byte[] data)
    {
        bytes = data;
    }

    // ////////////////////////////////////////////////////////////////////////

    /* Describes format descriptor */

    static int[] m_BytesPerFormat =
    { 0, 1, 1, 2, 4, 8, 1, 1, 2, 4, 8, 4, 8 };

    static int NUM_FORMATS = 12;

    static final int FMT_BYTE = 1; // Format Byte

    static final int FMT_STRING = 2;

    static final int FMT_USHORT = 3;

    static final int FMT_ULONG = 4;

    static final int FMT_URATIONAL = 5;

    static final int FMT_SBYTE = 6;

    static final int FMT_UNDEFINED = 7;

    static final int FMT_SSHORT = 8;

    static final int FMT_SLONG = 9;

    static final int FMT_SRATIONAL = 10;

    static final int FMT_SINGLE = 11;

    static final int FMT_DOUBLE = 12;

    // ////////////////////////////////////////////////////////////////////////

    static final int MAX_SECTIONS = 20; // JPG 文20214 中-32515 够20801 许30340
                                        // 最22810 SECTION 个25968

    static final int M_SOF0 = 0xC0; // Start Of Frame N

    static final int M_SOF1 = 0xC1; // N indicates which compression process

    static final int M_SOF2 = 0xC2; // Only SOF0-SOF2 are now in common use

    static final int M_SOF3 = 0xC3;

    static final int M_SOF5 = 0xC5; // NB: codes C4 and CC are NOT SOF markers

    static final int M_SOF6 = 0xC6;

    static final int M_SOF7 = 0xC7;

    static final int M_SOF9 = 0xC9;

    static final int M_SOF10 = 0xCA;

    static final int M_SOF11 = 0xCB;

    static final int M_SOF13 = 0xCD;

    static final int M_SOF14 = 0xCE;

    static final int M_SOF15 = 0xCF;

    static final int M_SOI = 0xD8; // Start Of Image (beginning of datastream)

    static final int M_EOI = 0xD9; // End Of Image (end of datastream)

    static final int M_SOS = 0xDA; // Start Of Scan (begins compressed data)

    static final int M_JFIF = 0xE0; // Jfif marker

    static final int M_EXIF = 0xE1; // Exif marker

    static final int M_COM = 0xFE; // COMment

    // 定20041 APP 标-29754 (SECTION)

    static final int M_APP0 = 0xE0;

    static final int M_APP1 = 0xE1;

    static final int M_APP2 = 0xE2;

    static final int M_APP3 = 0xE3;

    static final int M_APP4 = 0xE4;

    static final int M_APP5 = 0xE5;

    static final int M_APP6 = 0xE6;

    // Describes tag values

    // 注24847 : 下-26782 的23450 义26159 按29031 static intel CPU 来23450 义30340
    // ，20063 就26159 说25152 有30340 都26159 高20301 在21518 ，

    // 这26679 的23450 义21487 能19982 EXIF 白30382 书19978 的23450 义19981 一-32268
    // 。20363 如30333 皮20070 上25226 TAG_MAKE 定20041 为F01

    // 下-26782 是20027 要20449 息

    static final int TAG_MAKE = 0x010F; // 相26426 DC 制-28640 商

    static final int TAG_MODEL = 0x0110; // DC 型21495

    static final int TAG_ORIENTATION = 0x0112; // 拍25668 时26041 向-244 例22914
                                               // 向24038 手26059 转 90度25293
                                               // 摄29031 片

    static final int TAG_XRESOLUTION = 0x011A; // X 轴20998 辨29575

    static final int TAG_YRESOLUTION = 0x011B; // Y 轴20998 辨29575

    static final int TAG_RESOLUTIONUNIT = 0x0128; // 分-28760 率21333 位-244 例22914
                                                  // inch, cm

    static final int TAG_DATATIME = 0x0132; // 日26399 时-27148

    static final int TAG_YBCR_POSITION = 0x0213; // YCbCr 位32622 控21046 ，20363
                                                 // 如u23621 u20013

    static final int TAG_COPYRIGHT = 0x8298; // 版26435

    static final int TAG_EXIF_OFFSET = 0x8769; // EXIF 偏31227 ，-28711 时20505
                                               // 相24403 于22788 理19968 个26032
                                               // 的XIF 信24687

    //

    static final int TAG_IMAGEWIDTH = 0x0001; // 图20687 宽24230

    static final int TAG_IMAGEHEIGHT = 0x0101; // 图20687 高24230

    // BOOKMARK

    // 辅21161 信24687

    static final int TAG_EXPOSURETIME = 0x829A; // 曝20809 时-27148 ，20363 如/30 秒

    static final int TAG_FNUMBER = 0x829D; // 光22280 ，20363 如2.8

    static final int TAG_EXIF_VERSION = 0x9000; // EXIF 信24687 版26412

    static final int TAG_DATETIME_ORIGINAL = 0x9003; // 照29255 拍25668 时-27148
                                                     // ，20363 如005-10-13
                                                     // 11:09:35

    static final int TAG_DATATIME_DIGITIZED = 0x9004; // 相29255 被20854 它22270
                                                      // 像20462 改-28817 件20462
                                                      // 改21518 的26102 间-244
                                                      // 例22914 2005-10-13
                                                      // 11:36:35

    static final int TAG_COMPONCONFIG = 0x9101; // ComponentsConfiguration
                                                // 色24425 空-27148 配32622

    static final int TAG_COMPRESS_BIT = 0x9202; // 每20687 素21387 缩20301 数

    static final int TAG_SHUTTERSPEED = 0x9201; // 快-27160 速24230 ，20363 如/30 秒

    static final int TAG_APERTURE = 0x9202; // 光22280 值-244 例22914 F2.8

    static final int TAG_BRIGHTNESS = 0x9203; // 亮24230

    static final int TAG_EXPOSURE_BIAS = 0x9204; // 曝20809 补20607 ，20363 如V0.0

    static final int TAG_MAXAPERTURE = 0x9205; // 最22823 光22280 值-244 例22914
                                               // F2.8

    static final int TAG_SUBJECT_DISTANCE = 0x9206; // 拍25668 物-29219 离-244
                                                    // 例22914 3.11 米

    static final int TAG_METERING_MODE = 0x9207; // 测20809 模24335 ，20363 如30697
                                                 // 阵

    static final int TAG_WHITEBALANCE = 0x9208; // LightSource 白24179 衡

    static final int TAG_FLASH = 0x9209; // 是21542 使29992 闪20809 灯

    static final int TAG_FOCALLENGTH = 0x920A; // 焦-29219 ，20363 如.09mm

    static final int TAG_USERCOMMENT = 0x9286; // 用25143 注-28214

    static final int TAG_MAKE_COMMENT = 0x927C; // 厂21830 注-28214 。-28711 个29256
                                                // 本19981 提20379 (2005-10-13)

    static final int TAG_SUBSECTIME = 0x9290; // SubSecTime

    static final int TAG_SUBTIME_ORIGINAL = 0x9291; // SubSecTimeOriginal

    static final int TAG_SUBTIME_DIGITIZED = 0x9292; // SubSecTimeDigitized

    static final int TAG_FLASHPIXVERSION = 0x00A0; // Flash Pix 版26412

    static final int TAG_COLORSPACE = 0x01A0; // 色24425 空-27148 ，20363 如RGB

    static final int TAG_PIXEL_XDIMENSION = 0x02A0; //

    static final int TAG_PIXEL_YDIMENSION = 0x03A0; //

    // EXIFR98

    // 缩30053 图

    static final int TAG_INTEROP_OFFSET = 0xa005; // 偏31227

    static final int TAG_FOCALPLANEXRES = 0xA20E; // 焦24179 面u-28812 u20998
                                                  // 辨29575 ，20363 如024000/278

    static final int TAG_FOCALPLANEYRES = 0xA20F; // 焦24179 面u-28812 u20998
                                                  // 辨29575 ，20363 如68000/209

    static final int TAG_FOCALPLANEUNITS = 0xA210; // 焦24179 面20998 辨29575
                                                   // 单20301

    static final int TAG_EXIF_IMAGEWIDTH = 0xA002; // EXIF 图20687 宽24230 (就26159
                                                   // 这24352 JPG 图20687 )

    static final int TAG_EXIF_IMAGELENGTH = 0xA003; // EXIF 图20687 高24230

    static final int TAG_EXPOSURE_PROGRAM = 0x8822; //

    static final int TAG_ISO_EQUIVALENT = 0x8827; //

    static final int TAG_COMPRESSION_LEVEL = 0x9102; //

    static final int TAG_THUMBNAIL_OFFSET = 0x0201; // 缩30053 图20559 移

    static final int TAG_THUMBNAIL_LENGTH = 0x0202; // 缩30053 图22823 小

    static final int TAG_GPS_VERSIONID = 0x0000; // GPS 版26412

    static final int TAG_GPS_LATITUDEREF = 0x0001; // 纬24230 参-32765 ，20363
                                                   // 如21335 纬

    static final int TAG_GPS_LATITUDE = 0x0002; // 纬24230 值

    static final int TAG_GPS_LONGITUDEREF = 0x0003; // 经24230 参-32765 ，20363
                                                    // 如19996 经

    static final int TAG_GPS_LONGITUDE = 0x0004; // 经24230 值

    static final int TAG_GPS_ALTITUDEREF = 0x0005; // 海25300 高24230 参-32765

    static final int TAG_GPS_ALTITUDE = 0x0006; // 海25300

    static final int TAG_GPS_TIMESTAMP = 0x0007; // 时-27148 戳

    static final int TAG_GPS_SATELLITES = 0x0008; // 卫26143

    static final int TAG_GPS_STATUS = 0x0009; // 状24577

    static final int TAG_GPS_MEASUREMODE = 0x000A; //

    static final int TAG_GPS_DOP = 0x000B; //

    static final int TAG_GPS_SPEEDREF = 0x000C; //

    static final int TAG_GPS_SPEED = 0x000D; //

    static final int TAG_GPS_TRACKREF = 0x000E; //

    static final int TAG_GPS_TRACK = 0x000F; //

    static final int TAG_GPS_IMGDIRECTIONREF = 0x0010; //

    static final int TAG_GPS_IMGDIRECTION = 0x0011; //

    static final int TAG_GPS_MAPDATUM = 0x0012; //

    static final int TAG_GPS_DESTLATITUDEREF = 0x0013; //

    static final int TAG_GPS_DESTLATITUDE = 0x0014; //

    static final int TAG_GPS_DESTLONGITUDEREF = 0x0015;//

    static final int TAG_GPS_DESTLONGITUDE = 0x0016; //

    static final int TAG_GPS_DESTBEARINGREF = 0x0017; //

    static final int TAG_GPS_DESTBEARING = 0x0018; //

    static final int TAG_GPS_DESTDISTANCEREF = 0x0019; //

    static final int TAG_GPS_DESTDISTANCE = 0x001A; //

    // ////////////////////////////////////////////////////////////////////////

    /*--------------------------------------------------------------------------  
    
     Get 16 bits motorola order (always) for jpeg header stuff.  
    
     --------------------------------------------------------------------------*/

    static byte[] bytes;

    static int EXIF_Get16m(int inx)

    {

        return (bytes[inx] << 8) | (bytes[inx + 1] & 0x00ff);

    }

    /*--------------------------------------------------------------------------  
    
     Convert a 16 bit unsigned value from file's native unsigned char order  
    
     --------------------------------------------------------------------------*/

    static int EXIF_Get16u(int inx)

    {

        if (m_MotorolaOrder == 1)

        {

            return (bytes[inx] << 8) | (bytes[inx + 1] & 0x00ff);

        }

        else

        {

            return (bytes[inx + 1] << 8) | (bytes[inx] & 0x00ff);

        }

    }

    /*--------------------------------------------------------------------------  
    
     Convert a 32 bit signed value from file's native unsigned char order  
    
     --------------------------------------------------------------------------*/

    static int EXIF_Get32s(int inx)

    {

        if (m_MotorolaOrder == 1)

        {

            return (bytes[inx] << 24) | ((bytes[inx + 1] << 16) & 0x00ffffff)

            | ((bytes[inx + 2] << 8) & 0x0000ffff) | (bytes[inx + 3] & 0x000000ff);

        }

        else

        {

            int i = 0;
            i = i | (bytes[inx + 3] << 24) | ((bytes[inx + 2] << 16) & 0x00ffffff)

            | ((bytes[inx + 1] << 8) & 0x0000ffff) | (bytes[inx] & 0x000000ff);
            return i;

        }

    }

    /*--------------------------------------------------------------------------  
    
     Convert a 32 bit unsigned value from file's native unsigned char order  
    
     --------------------------------------------------------------------------*/

    static int EXIF_Get32u(int inx)
    {
        return EXIF_Get32s(inx);

    }

    /*--------------------------------------------------------------------------  
    
     Evaluate number, be it int, rational, or float from directory.  
    
     --------------------------------------------------------------------------*/

    static double EXIF_ConvertAnyFormat(int ValuePtr, int Format)

    {

        double Value;

        Value = 0;

        switch (Format)

        {

            case FMT_SBYTE:
                Value = bytes[ValuePtr];
                break;

            case FMT_BYTE:
                Value = bytes[ValuePtr];
                break;

            case FMT_USHORT:
                Value = EXIF_Get16u(ValuePtr);
                break;

            case FMT_ULONG:
                Value = EXIF_Get32u(ValuePtr);
                break;

            case FMT_URATIONAL:

            case FMT_SRATIONAL:

            {

                int Num, Den;

                Num = EXIF_Get32s(ValuePtr);

                Den = EXIF_Get32s(4 + ValuePtr);

                if (Den == 0)

                {

                    Value = 0;

                }

                else

                {

                    Value = (double) Num / Den;

                }

                break;

            }

            case FMT_SSHORT:
                Value = EXIF_Get16u(ValuePtr);
                break;

            case FMT_SLONG:
                Value = EXIF_Get32s(ValuePtr);
                break;

        }

        return Value;

    }

    /*********************************************************************
     * 函数声明: STATIC BOOL EXIF_ProcessExifDir(...)
     * 
     * 参 数:
     * 
     * IN: CONST UCHAR* DataStart: 数据流的起始位置。这个数值仅仅在函数 EXIF_Decode 中能够改变
     * 
     * CONST long dwFilePointerBeforeReadData: 在读取数据流之前的文件指针位置
     * 
     * UCHAR *DirStart: SECTION 中数据流，去除了前面的
     * EXIF\0\0(6)+II(2)+2A00(2)+08000000(6)=14
     * 
     * UCHAR *OffsetBase: 仅仅去除了 EXIFF\0\0(6)=6字节
     * 
     * UINT ExifLength: 整个 SECTION 数据流的长度去除 EXIF\0\0后的长度==All Length - 6
     * 
     * EXIFINFO * const m_exifinfo:
     * 
     * OUT:
     * 
     * I/O:
     * 
     * UCHAR **const LastExifRefdP: 偏移过后的位置
     * 
     * 返回值:
     * 
     * 功能描述:
     * 
     * 引 用:
     *********************************************************************/

    static boolean EXIF_ProcessExifDir(int DataStart, int dwFilePointerBeforeReadData,

    int DirStart, int OffsetBase, int ExifLength,

    TagExifInfo m_exifinfo)

    {

        int de = 0; //

        int a = 0; //

        int NumTagEntries = 0; // 包含的 TAG 的个数

        int ThumbnailOffset = 0; // 缩略图偏移量

        int ThumbnailSize = 0; // 缩略图的大小

        int BytesCount = 0; //

        int TagEntry = 0; // 每个 TAG 的入口

        int Tag, Format, Components;

        int ValuePtr = 0; // 偏移后的位置。因为 TAG 与内容很多时候都不是连续的，而是中间有个偏移量

        int OffsetVal = 0; // 偏移量

        // 读取文件中存在 TAG 个数

        NumTagEntries = EXIF_Get16u(DirStart);

        // 判断 EXIF 信息的长度是否正确

        // 下面 DirStart+2 指再去除了 NumTagEntries 所占的 2 个字节

        if ((DirStart + 2 + NumTagEntries * 12) > (OffsetBase + ExifLength))

        {

            System.out.println("Illegally sized directory");

            return false;

        }

        for (de = 0; de < NumTagEntries; de++)

        {

            // 在下面的操作中，所有的数据通通使用 UCHAR* 来表示

            TagEntry = DirStart + 2 + 12 * de; // TagEntry 的入口点

            Tag = EXIF_Get16u(TagEntry);
            Tag = Tag & 0x0000ffff;

            Format = EXIF_Get16u(TagEntry + 2);

            Components = EXIF_Get32u(TagEntry + 4);

            if ((Format - 1) >= NUM_FORMATS)

            {

                // (-1) catches illegal zero case as unsigned underflows to
                // positive large

                System.out.println("Illegal format code in EXIF dir " + Format);

                return false;

            }

            BytesCount = Components * m_BytesPerFormat[Format];

            if (BytesCount > 4)

            {

                OffsetVal = EXIF_Get32u(TagEntry + 8);

                // If its bigger than 4 unsigned chars, the dir entry contains
                // an offset.

                if (OffsetVal + BytesCount > ExifLength)

                {

                    // JPG 文件内容遭到破坏

                    System.out.println("Illegal pointer offset value in EXIF.");

                    return false;

                }

                ValuePtr = OffsetBase + OffsetVal;

            }

            else

            {

                // 4 unsigned chars or less and value is in the dir entry itself

                ValuePtr = TagEntry + 8;

            }
            // System.out.println(Integer.toHexString(Tag));
            // Extract useful components of tag
            switch (Tag)

            {

                case TAG_MAKE:
                    String str = new String();
                    for (int i = 0; i < 31; i++)
                    {
                        if (bytes[ValuePtr + i] != '\0')
                        {
                            str += (char) bytes[ValuePtr + i];
                        }
                        else
                        {
                            break;
                        }
                    }
                    m_exifinfo.CameraMake = str;

                    break;

                case TAG_MODEL:

                    str = new String();
                    for (int i = 0; i < 39; i++)
                    {
                        if (bytes[ValuePtr + i] != '\0')
                        {
                            str += (char) bytes[ValuePtr + i];
                        }
                        else
                        {
                            break;
                        }
                    }
                    m_exifinfo.CameraModel = str;

                    break;

                case TAG_EXIF_VERSION:

                    m_exifinfo.Version = new String(bytes, ValuePtr, 4);

                    break;

                // 日期和时间

                case TAG_DATETIME_ORIGINAL:
                    m_exifinfo.DateTime = new String(bytes, ValuePtr, 19);

                    break;

                case TAG_DATATIME_DIGITIZED:

                    m_exifinfo.DateTimeDigitized = new String(bytes, ValuePtr, 19);

                    break;

                // 光圈

                case TAG_FNUMBER:

                    m_exifinfo.ApertureFNumber = (float) EXIF_ConvertAnyFormat(ValuePtr, Format);

                    break;

                case TAG_APERTURE: // 光圈值

                case TAG_MAXAPERTURE: // 最大光圈值

                    // More relevant info always comes earlier, so only

                    // use this field if we don't have appropriate aperture

                    // information yet.

                    if (m_exifinfo.ApertureFNumber == 0)

                    {

                        m_exifinfo.ApertureFNumber = (float) Math.exp(EXIF_ConvertAnyFormat(ValuePtr, Format) * Math.log(2) * 0.5);// ATTENTION

                        m_exifinfo.ApertureFNumber = (float) (EXIF_ConvertAnyFormat(ValuePtr, Format) * Math.log(2) * 0.5);

                    }

                    break;

                // Brightness

                case TAG_BRIGHTNESS:

                    m_exifinfo.Brightness = (float) EXIF_ConvertAnyFormat(ValuePtr, Format);

                    break;

                // 焦距信息(例如 7.09mm)

                case TAG_FOCALLENGTH:

                    // Nice digital cameras actually save the focal length

                    // as a function of how farthey are zoomed in.

                    m_exifinfo.FocalLength = (float) EXIF_ConvertAnyFormat(ValuePtr, Format);

                    break;

                // 目标距离(例如 1.11米)

                case TAG_SUBJECT_DISTANCE:

                    // Inidcates the distacne the autofocus camera is focused
                    // to.

                    // Tends to be less accurate as distance increases.

                    m_exifinfo.Distance = (float) EXIF_ConvertAnyFormat(ValuePtr, Format);

                    break;

                // 曝光时间(例如 1/30 秒)

                case TAG_EXPOSURETIME:

                    // Simplest way of expressing exposure time, so I

                    // trust it most. (overwrite previously computd value

                    // if there is one)

                    m_exifinfo.ExposureTime =

                    (float) EXIF_ConvertAnyFormat(ValuePtr, Format);

                    break;

                // SHUTTERSPEED 快门速度不需要

                case TAG_SHUTTERSPEED:

                    // More complicated way of expressing exposure time,

                    // so only use this value if we don't already have it

                    // from somewhere else.

                    /*
                     * if (m_exifinfo.ExposureTime == 0)
                     * 
                     * {
                     * 
                     * m_exifinfo.ExposureTime = (float)
                     * 
                     * (1/Math.exp(EXIF_ConvertAnyFormat(ValuePtr,
                     * Format)*Math.log(2)));
                     * 
                     * }
                     */

                    break;

                // FLASH 闪光灯信息不需要

                case TAG_FLASH:
                    // System.out.println(EXIF_ConvertAnyFormat(ValuePtr,
                    // Format));
                    int f = (int) EXIF_ConvertAnyFormat(ValuePtr, Format);
                    if (f == 32)
                    {

                        m_exifinfo.FlashUsed = "No function";

                    }

                    else if (f == 0)
                    {
                        m_exifinfo.FlashUsed = "Fired";
                    }
                    else
                    {

                        m_exifinfo.FlashUsed = "Not Fired";

                    }

                    break;

                case TAG_ORIENTATION:

                    m_exifinfo.Orientation = (int) EXIF_ConvertAnyFormat(ValuePtr, Format);
                    System.out.println("pppppppppppp---->" + m_exifinfo.Orientation);
                    if (m_exifinfo.Orientation < 1 || m_exifinfo.Orientation > 8)

                    {

                        System.out.println("Undefined rotation value");

                        m_exifinfo.Orientation = 0;

                    }

                    break;

                // EXIF 图像高度与宽度(例如 1024*768)

                case TAG_EXIF_IMAGELENGTH:

                case TAG_EXIF_IMAGEWIDTH:

                    a = (int) EXIF_ConvertAnyFormat(ValuePtr, Format);

                    if (m_ExifImageWidth < a)
                        m_ExifImageWidth = a;

                    break;

                // 焦平面 X 轴分辨率(例如 1024000/278)，理论上与 Y 一致

                case TAG_FOCALPLANEXRES:

                    m_exifinfo.FocalplaneXRes = (float) EXIF_ConvertAnyFormat(ValuePtr, Format);

                    break;

                // 焦平面 Y 轴分辨率(例如 768000/209)，理论上与 X 一致

                case TAG_FOCALPLANEYRES:

                    m_exifinfo.FocalplaneYRes = (float) EXIF_ConvertAnyFormat(ValuePtr, Format);

                    break;

                case TAG_RESOLUTIONUNIT:

                    switch ((int) EXIF_ConvertAnyFormat(ValuePtr, Format))

                    {

                        case 1:
                            m_exifinfo.ResolutionUnit = 1.0f;
                            break; // 1 inch

                        case 2:
                            m_exifinfo.ResolutionUnit = 1.0f;
                            break; //

                        case 3:
                            m_exifinfo.ResolutionUnit = 0.3937007874f;
                            break; // 1 centimeter

                        case 4:
                            m_exifinfo.ResolutionUnit = 0.03937007874f;
                            break; // 1 millimeter

                        case 5:
                            m_exifinfo.ResolutionUnit = 0.00003937007874f; // 1
                                                                           // micrometer

                    }

                    break;

                // 焦平面分辨率单位(例如米)

                case TAG_FOCALPLANEUNITS:

                    switch ((int) EXIF_ConvertAnyFormat(ValuePtr, Format))

                    {

                        case 1:
                            m_exifinfo.FocalplaneUnits = 1.0f;
                            break; // 1 inch

                        case 2:
                            m_exifinfo.FocalplaneUnits = 1.0f;
                            break; //

                        case 3:
                            m_exifinfo.FocalplaneUnits = 0.3937007874f;
                            break; // 1 centimeter

                        case 4:
                            m_exifinfo.FocalplaneUnits = 0.03937007874f;
                            break; // 1 millimeter

                        case 5:
                            m_exifinfo.FocalplaneUnits = 0.00003937007874f;
                            break; // 1 micrometer//

                    }

                    break;

                // 曝光补偿信息

                case TAG_EXPOSURE_BIAS:

                    m_exifinfo.ExposureBias = (float) EXIF_ConvertAnyFormat(ValuePtr, Format);

                    break;

                // 白平衡

                case TAG_WHITEBALANCE:

                    m_exifinfo.Whitebalance = (int) EXIF_ConvertAnyFormat(ValuePtr, Format);

                    break;

                case TAG_METERING_MODE:

                    m_exifinfo.MeteringMode = (int) EXIF_ConvertAnyFormat(ValuePtr, Format);

                    break;

                case TAG_EXPOSURE_PROGRAM:

                    m_exifinfo.ExposureProgram = (int) EXIF_ConvertAnyFormat(ValuePtr, Format);

                    break;

                case TAG_ISO_EQUIVALENT:

                    m_exifinfo.ISOequivalent = (int) EXIF_ConvertAnyFormat(ValuePtr, Format);

                    if (m_exifinfo.ISOequivalent < 50)
                        m_exifinfo.ISOequivalent *= 200;

                    break;

                case TAG_COMPRESSION_LEVEL:

                    m_exifinfo.CompressionLevel = (int) EXIF_ConvertAnyFormat(ValuePtr, Format);

                    break;

                // X 轴分辨率

                case TAG_XRESOLUTION:

                    m_exifinfo.Xresolution = (float) EXIF_ConvertAnyFormat(ValuePtr, Format);

                    break;

                // Y 轴分辨率

                case TAG_YRESOLUTION:

                    m_exifinfo.Yresolution = (float) EXIF_ConvertAnyFormat(ValuePtr, Format);

                    break;

                // 缩略图 偏移量

                case TAG_THUMBNAIL_OFFSET:

                    System.out.println("there is a thumbnailOffset");

                    ThumbnailOffset = (int) Math.abs(EXIF_ConvertAnyFormat(ValuePtr, Format));

                    break;

                // 缩略图的大小

                case TAG_THUMBNAIL_LENGTH:

                    System.out.println("there is a thumbnailSize");

                    ThumbnailSize = (int) Math.abs(EXIF_ConvertAnyFormat(ValuePtr, Format));

                    break;

            } // end switch(Tag)

            // EXIF 信息偏移

            //
            if (Tag == TAG_EXIF_OFFSET || Tag == TAG_INTEROP_OFFSET)

            {
                int SubdirStart;

                SubdirStart = OffsetBase + EXIF_Get32u(ValuePtr);
                if (SubdirStart < OffsetBase ||

                SubdirStart > OffsetBase + ExifLength)

                {
                    System.out.println("Illegal subdirectory link");

                    return false;

                }
                EXIF_ProcessExifDir(DataStart, dwFilePointerBeforeReadData, SubdirStart, OffsetBase, ExifLength, m_exifinfo);
                continue;

            }

        } // end for {for (de=0;de<NumTagEntries;de++)}

        if (ThumbnailSize != 0 && ThumbnailOffset != 0)

        {
            // 如果文件中存在缩略图，那么将缩略图的数据保存

            // 注意：这里仅仅负责 malloc，调用者需要自己 free

            if (ThumbnailSize + ThumbnailOffset <= ExifLength)

            {

                // 将缩略图的数据全部拷贝到一块新开辟的内存

                if (m_exifinfo.dwExifType != 0)

                {

                    int pThumbnailData = OffsetBase + ThumbnailOffset;

                    int dw = pThumbnailData - DataStart + dwFilePointerBeforeReadData;

                    // suo lue tu

                    //
                    // m_exifinfo.ThumbnailPointer =
                    // (UCHAR*)malloc(ThumbnailSize);
                    //
                    // memcpy(m_exifinfo.ThumbnailPointer, pThumbnailData,
                    // ThumbnailSize);

                }

                else

                {

                    m_exifinfo.ThumbnailPointer = (int) (OffsetBase + ThumbnailOffset - DataStart + dwFilePointerBeforeReadData);

                }

                m_exifinfo.ThumbnailSize = ThumbnailSize;

            }

        }

        return true;

    }

    /*********************************************************************
     * 函数声明: STATIC BOOL EXIF_process_EXIF(UCHAR * CharBuf, UINT length)
     * 
     * 参 数:
     * 
     * IN: CONST UCHAR* DataStart: 数据流的起始位置。这个数值仅仅在函数 EXIF_Decode 中能够改变
     * 
     * CONST int dwFilePointerBeforeReadData: 在读取数据流之前的文件指针位置
     * 
     * UCHAR * CharBuf: 这个 SECTION 数据内容。注意：前面已经去掉了包含长度的2个字符
     * 
     * CONST UINT length: 这个 SECTION 数据流的长度
     * 
     * 返回值:
     * 
     * 功能描述: 处理某个 SECTION 中的 EXIF 信息。
     * 
     * 成功返回TRUE表示EXIF信息存在且正确，失败返回FALSE
     * 
     * 引 用:
     *********************************************************************/

    public static boolean EXIF_process_EXIF(int DataStart, int dwFilePointerBeforeReadData,

    int CharBuf, int length)

    {

        int FirstOffset = 0;

        int LastExifRefd = 0;

        m_pExifInfo.FlashUsed = "";

        m_ExifImageWidth = 0;

        // 检查 EXIF 头是否正确

        {

            String ExifHeader = "Exif\0\0";
            String inbyte = new String(bytes, CharBuf, 6);
            if (!inbyte.equals(ExifHeader))
            {

                System.out.println("Incorrect Exif header");

                m_pExifInfo.Orientation = 0;
                return false;

            }

        }

        // 判断内存中数据的排列是按照 Intel 还是按照 Motorola CPU 排列的
        String str = new String(bytes, CharBuf + 6, 2);
        if (str.equals("II"))

        {

            m_MotorolaOrder = 0; //

        }

        else if (str.equals("MM"))

        {

            m_MotorolaOrder = 1; //

        }

        else

        {

            System.out.println("Invalid Exif alignment marker.");

            return false;

        }

        // 检查下面 2 个字节是否是 0x2A00

        if (EXIF_Get16u(CharBuf + 8) != 0x2A)

        {

            System.out.println("Invalid Exif start (1)");

            return false;

        }

        // 判断下面的 0th IFD Offset 是否是 0x08000000

        FirstOffset = EXIF_Get32u(CharBuf + 10);

        if (FirstOffset < 8 || FirstOffset > 16)

        {

            System.out.println("Suspicious offset of first IFD value");

            return false;

        }

        LastExifRefd = CharBuf;

        // 开始处理 EXIF 信息

        if (!EXIF_ProcessExifDir(DataStart, dwFilePointerBeforeReadData,

        CharBuf + 14, CharBuf + 6, length - 6, m_pExifInfo))

        {

            return false;

        }

        // This is how far the interesting (non thumbnail) part of the exif
        // went.

        // int ExifSettingsLength = LastExifRefd - CharBuf;

        // 计算 CCD 宽度(单位:毫米)

        if (m_pExifInfo.FocalplaneXRes != 0)

        {

            m_pExifInfo.CCDWidth = (float) (m_ExifImageWidth * m_pExifInfo.FocalplaneUnits / m_pExifInfo.FocalplaneXRes);

        }

        return true;

    }

    static void EXIF_process_SOFn(int Data, int marker)

    {

        int data_precision, num_components;

        data_precision = bytes[Data + 2];

        m_pExifInfo.Height = EXIF_Get16m((Data + 3));

        m_pExifInfo.Width = EXIF_Get16m((Data + 5));

        num_components = bytes[Data + 7];

        if (num_components == 3)

        {

            m_pExifInfo.IsColor = 1;

        }

        else

        {

            m_pExifInfo.IsColor = 0;

        }

        m_pExifInfo.Process = marker;

        // if (ShowTags)
        // printf("JPEG image is %uw * %uh, %d color components, %d bits per sample\n",

        // ImageInfo.Width, ImageInfo.Height, num_components, data_precision);

    }

}
