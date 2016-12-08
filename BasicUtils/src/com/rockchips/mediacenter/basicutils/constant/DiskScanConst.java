package com.rockchips.mediacenter.basicutils.constant;


/**
 * 
 * 扫描外设具体文件功能相关的常量
 * 
 * AR-0000698216
 * 基于海思的能力支持媒体格式
 * 
 * @author  l00174030
 * @version  [2013-1-17]
 */
public class DiskScanConst
{
    // 音频格式后缀
    public static final String[] AUDIO_SUFFIX = {"mp3", "wma", "amr", "aac", "wav", "wave", "ogg", "mka", "ac3", "m4a", "ra", "flac",
        "ape", "mpa", "aif", "aiff", "at3p", "au", "snd", "dts", "rmi", "mid", "mp1", "mp2", "pcm", "lpcm", "l16",
        "ram"};
    
    // 视频格式后缀
    public static final String[] VIDEO_SUFFIX = {"avi", "wmv", "mp4", "rmvb", "kkv", "3gp", "ts", "mpeg", "mpg", "mkv", "m3u8", "mov",
        "m2ts", "flv", "m2t", "mts", "vob", "dat", "m4v", "asf", "f4v", "3g2", "m1v", "m2v", "tp", "trp", "m2p", "rm",
        "avc", "dv", "divx", "mjpg", "mjpeg", "mpe", "mp2p", "mp2t", "mpg2", "mpeg2", "m4p", "mp4ps", "ogm", "hdmov",
        "qt", "iso"};
    
    // 图片格式后缀
    public static final String[] IMAGE_SUFFIX = {"bmp", "gif", "png", "jpg", "jpeg", "jpe", "tiff", "tif", "pcd", "qti", "qtf", "qtif",
        "ico", "pnm", "ppm"};
    
    // 字幕格式后缀
    public static final String[] SUBTITLE_SUFFIX = {"ass", "lrc", "srt", "smi", "sub", "txt", "pgs", "dvb", "dvd"};
    
    // 批量入库的界限
    public static final int INSERT_CNT = 100;
    
    // 扫描线程睡眠的时间
    public static final int SCAN_SLEEP_CNT = 80;
    
    // 发送内容更新广播的限制
    public static final int BC_UPDATE_CNT = 5000;
    
    // 是否已扫描 1:已经扫描
    public static final int HAS_ALREAY_SCANNED = 1;
    
//    // 文件类型：1：视频
//    int FILE_TYPE_VIDEO = 1;
//    
//    // 文件类型：2：音频
//    int FILE_TYPE_AUDIO = 2;
//    
//    // 文件类型：3：图片
//    int FILE_TYPE_POTO = 3;
    
    // 日期中的 月
    public static final String[] MONTH_NAMES = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    
    // 空串
    public static final String BLANK_STR = "";
    
    // 换行符\n
    public static final String ENTER_STR = "\n";
    
    // 分割符/
    public static final String SPLITER_STR = "/";
    
    // 冒号
    public static final String COLON_STR = ":";
    
    // 中划线
    public static final String MID_UNDER_LINE = "-";
    
    // 输入时间格式
    public static final String IN_FORMATTER = "yyyy/MM/dd";
    
    // 输出时间格式
    public static final String OUT_FORMATTER = "yyyyMMdd";
    
    // 线程名
    public static final String THREAD_NAME_JNI = "JnILayerScanner";
    
    // 线程名
    public static final String THREAD_NAME_JAVA = "JavaLayerScanner";
    
    // 线程名
    public static final String THREAD_NAME_INTENT = "LocListenIntent";
}
