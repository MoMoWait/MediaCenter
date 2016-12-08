/**
 *
 * com.rockchips.iptv.stb.dlna.util
 * Constant.java
 *
 * 2011-10-10-下午01:53:19
 * Copyright 2011 Huawei Technologies Co., Ltd
 *
 */
package com.rockchips.mediacenter.basicutils.constant;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

/**
 * 
 * Constant 保存常量
 * 
 * 2011-10-10 下午01:53:19
 * 
 * @version 1.0.0
 * 
 */
public class Constant
{
//    public static String  BROADCAST_PERMISSION_MOUNT_UNMOUNT_FILESYSTEMS = "android.permission.MOUNT_UNMOUNT_FILESYSTEMS";
    public static final int SEARCH_LIMIT = 500;
    public static class SpecialCode
    {
        public static final String SELECTION_CONN_CODE = "//";
    }

    public static class URI
    {
        // 数据库相关 ---------开始
        /**
         * 本地外设Audio的URI
         */
        public static final Uri LOCAL_AUDIO_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        /**
         * 本地外设Video的URI
         */
        public static final Uri LOCAL_VIDEO_URI = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        /**
         * 本地外设Images的URI
         */
        public static final Uri LOCAL_IMAGE_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        /**
         * 本地外设Device的URI
         */
        public static final Uri LOCAL_DEVICE_URI = Uri.parse("content://media/external/devices");

        /**
         * 本地Provider的URI
         */
        public static final Uri LOCAL_PROVIDER_URI = Uri.parse("content://" + LocDevProvConst.AUTHORITY);

        // 数据库相关 ---------结束
    }

    /**
     * MediaType 媒体类型 2011-10-10 下午08:09:43
     * @version 1.0.0
     */
    public static final class MediaType
    {
        public static final int UNKNOWN_TYPE = -1;

        public static final int BASE = 0;

        public static final int DEVICE = BASE + 1;

        public static final int FOLDER = BASE + 2;

        public static final int VIDEO = BASE + 4;

        public static final int AUDIO = BASE + 6;

        public static final int SUBTITLE = BASE + 7;

        public static final int IMAGE = BASE + 8;

        public static final int MEDIA = BASE + 9;

        // t00181037 add
        public static final int VIDEOFOLDER = BASE + 10;

        public static final int AUDIOFOLDER = BASE + 11;

        public static final int IMAGEFOLDER = BASE + 12;

        // cloud
        // t00181037 add
        public static final int CLOUDMUSIC = BASE + 13;

        public static final int CLOUDPHOTO = BASE + 14;

        public static final int CLOUDFRIEND = BASE + 15;

        public static String getMediaTypeName(int type)
        {
            String strRet = "UNKNOWN_TYPE";
            switch (type)
            {
                case DEVICE:
                    strRet = "DEVICE";
                    break;

                case FOLDER:
                    strRet = "FOLDER";
                    break;

                case AUDIO:
                    strRet = "AUDIO";
                    break;

                case VIDEO:
                    strRet = "VIDEO";
                    break;

                case IMAGE:
                    strRet = "IMAGE";
                    break;
                case SUBTITLE:
                    strRet = "SUBTITLE";
                    break;

                case VIDEOFOLDER:
                    strRet = "VIDEOFOLDER";
                    break;

                case AUDIOFOLDER:
                    strRet = "AUDIOFOLDER";
                    break;

                case IMAGEFOLDER:
                    strRet = "IMAGEFOLDER";
                    break;

                case CLOUDPHOTO:
                    strRet = "CLOUDPHOTO";
                    break;

                case CLOUDMUSIC:
                    strRet = "CLOUDMUSIC";
                    break;

                case CLOUDFRIEND:
                    strRet = "CLOUDFRIEND";
                    break;
                case UNKNOWN_TYPE:
                    strRet = "UNKNOWN_TYPE";
                    break;

                default:
                    strRet = "UNKNOWN_TYPE";
                    break;
            }

            return strRet;
        }
    }

    /**
     * 设备类型
     * @author GaoFei
     *
     */
    public static final class DeviceType
    {
    	/**
    	 * NFS设备
    	 * GaoFei Add
    	 */
    	public static final int DEVICE_TYPE_NFS = 2001;
    	
    	
    	/**
    	 * SMB设备
    	 * GaoFei Add
    	 */
    	public static final int DEVICE_TYPE_SMB = 2002;
    	/**内部存储*/
    	public static final int DEVICE_TYPE_INTERNEL_STORAGE = -13;
    	
        /**
         * SD盘设备类型
         */
        public static final int DEVICE_TYPE_SD = -12;

        /**
         * U盘设备类型
         */
        public static final int DEVICE_TYPE_U = -11;

        /**
         * 预留 0-10为特殊设备
         */
        public static final int DEVICE_TYPE_OTHER_PRODUCT = 0;

        // 0,1,2 indicate rockchips settings(cellphone, stb, pad)
        public static final int DEVICE_TYPE_HW_CELLPHONE = 1;

        public static final int DEVICE_TYPE_HW_STB = 2;

        public static final int DEVICE_TYPE_HW_PAD = 3;

        // 搜索项
        public static final int DEVICE_TYPE_SEARCH = 4;

        /**
         * normal DMS设备类型
         */
        public static final int DEVICE_TYPE_DMS = 20;

        // add by zwx143228 加上收藏夹展示
        /**
         * 收藏夹类型
         * */
        public static final int DEVICE_TYPE_FAVORITE = 21;

        /**
         * /** 云设备
         */
        // modified by c00226539 云相册要放到固定设备之后，网络设备之前
        public static final int DEVICE_TYPE_CLOUD = -10;

        /**
         * 云设备
         */
        public static final int DEVICE_TYPE_HUAWEI_CLOUD = 1001;

        /**
         * QQ云设备
         */
        public static final int DEVICE_TYPE_QQ_CLOUD = 1003;

        /**
         * DropBox云设备
         */
        public static final int DEVICE_TYPE_DROPBOX_CLOUD = 1004;

        /**
         * 天翼云设备
         */
        public static final int DEVICE_TYPE_TIANYI_CLOUD = 1002;

        /*
         * 未知设备类型
         */
        public static final int DEVICE_TYPE_UNKNOWN = -1;

        // 设备类型相关------结束
        /**
         * 根据 type 判断是否为外设 <功能详细描述>
         * @param type
         * @return
         * @see [类、类#方法、类#成员]
         */
        public static boolean isExternalStorage(int type)
        {
            switch (type)
            {
                case DEVICE_TYPE_SD:
                case DEVICE_TYPE_U:
                    return true;

                case DEVICE_TYPE_DMS:
                case DEVICE_TYPE_HW_CELLPHONE:
                case DEVICE_TYPE_HW_PAD:
                case DEVICE_TYPE_HW_STB:
                case DEVICE_TYPE_OTHER_PRODUCT:
                case DEVICE_TYPE_CLOUD:
                    return false;

                default:
                    break;
            }

            return false;
        }

        /**
         * 根据 type 判断是否网络设备 <功能详细描述>
         * @param type
         * @return
         * @see [类、类#方法、类#成员]
         */
        public static boolean isDLNADevice(int type)
        {
            switch (type)
            {
                case DEVICE_TYPE_SD:
                case DEVICE_TYPE_U:
                case DEVICE_TYPE_CLOUD:
                    return false;

                case DEVICE_TYPE_DMS:
                case DEVICE_TYPE_HW_CELLPHONE:
                case DEVICE_TYPE_HW_PAD:
                case DEVICE_TYPE_HW_STB:
                case DEVICE_TYPE_OTHER_PRODUCT:

                    return true;

                default:
                    break;
            }

            return false;
        }

        public static boolean isHWDevice(int type)
        {
            switch (type)
            {
                case DEVICE_TYPE_HW_CELLPHONE:
                case DEVICE_TYPE_HW_PAD:
                case DEVICE_TYPE_HW_STB:
                    return true;

                default:
                    break;
            }

            return false;
        }

        /**
         * 根据 type 判断是否本地设备 <功能详细描述>
         * @param type
         * @return
         * @see [类、类#方法、类#成员]
         */
        public static boolean isLocalDevice(int type)
        {
            switch (type)
            {
                case DEVICE_TYPE_SD:
                case DEVICE_TYPE_U:
                case DEVICE_TYPE_INTERNEL_STORAGE:
                    return true;

                default:
                    break;
            }

            return false;
        }

    }

    public enum EBrowerType
    {
        ORDER_TYPE_NONE, ORDER_TYPE_TIME, ORDER_TYPE_CHARACTER, ORDER_TYPE_FOLDER, ORDER_TYPE_ALBUM, ORDER_TYPE_ARTIST
    }
    
    public enum ESearchType
    {
        SEARCH_TYPE_ALL, SEARCH_TYPE_BY_DEVICE
    }

    public static final class DeleteModeState
    {
        public static final int DELETE_MODE_STATE_NONE = 0;

        public static final int DELETE_MODE_STATE_UNSELECTED = 1;

        public static final int DELETE_MODE_STATE_SELECTED = 2;
    }

    public static final class MediaPlayMode
    {
        /**
         * 全体循环播放
         */
        public static final int MP_MODE_ALL_CYC = 0;

        /**
         * 单曲循环播放
         */
        public static final int MP_MODE_SINGLE_CYC = 1;

        /**
         * 随机播放
         */
        public static final int MP_MODE_RONDOM = 2;

        /**
         * 单曲播放
         */
        public static final int MP_MODE_SINGLE = 3;

        /**
         * 全体顺序播放
         */
        public static final int MP_MODE_ALL = 4;

    }

    public static final class IntentKey
    {
        /**
         * 获取媒体列表的key键
         */
        public static final String MEDIA_INFO_LIST = "MEDIA_INFO_LIST";

        public static final String IS_INTERNAL_PLAYER = "IS_INTERNAL_PLAYER";

        public static final String MEDIALIST_ID = "MEDIALIST_ID";

        public static final String MEDIALIST_PACKAGE_COUNT = "MEDIALIST_PACKAGE_COUNT";

        public static final String MEDIALIST_PACKAGE_ORDERID = "MEDIALIST_PACKAGE_ORDERID";
        
        public static final String IS_MIRROR_ON = "IS_MIRROR_ON";

        /**
         * 唯一标示的Key键
         */
        public static final String UNIQ = "UNIQ";

        /**
         * 播放器与MCS建立绑定时，返回给MCS的Sender端唯一标示的Key键
         */
        public static final String SENDER_UNIQ_PLAYER_TO_MCS = "SENDER_UNIQ_PLAYER_TO_MCS";

        /**
         * 定位播放位置的Key键
         */
        public static final String SEEK_POS = "SEEK_POS";

        /**
         * 报告错误信息的Key键
         */
        public static final String ERROR_INFO = "ERROR_INFO";

        /**
         * 传递上一个、下一个、上一页、下一页媒体请求的Key键
         */
        public static final String MEDIA_REQUEST_TYPE = "MEDIA_REQUEST_TYPE";

        /**
         * 当前播放器所播放的URL
         */
        public static final String CURRENT_PLAY_URL = "CURRENT_PLAY_URL";

        /**
         * 传递下线设备ID的Key键
         */
        public static final String DEVICE_ID = "DEVICE_ID";

        /**
         * 传递当前索引的Key键
         */
        public static final String CURRENT_INDEX = "CURRENT_INDEX";

        /**
         * 传递媒体的播放总长度
         */
        public static final String MEDIA_DURATION = "MEDIA_DURATION";

        /**
         * 传输Data-url
         */
        public static final String MEDIA_DATA = "MEDIA_DATA";

        // search intent begin

        public static final String SEARCH_DATA = "data";

        public static final String SEARCH_DISPLAYNAME = "_display_name";

        public static final String SEARCH_THUMBURL = "thumbnail_url";

        public static final String SEARCH_TITLE = "title";

        public static final String SEARCH_ALBUM = "album";

        public static final String SEARCH_ARTIST = "artist";

        public static final String SEARCH_MEDIATYPE = "media_type";

        // search intent end        

        /**
         * 传输一个MediaInfo,作为需要显示的对象 比如传入一个设备信息，要求显示该设备信息 传入一个文件夹信息，要求显示文件夹下的内容
         */
        public static final String CURRENTMEDIAINFO = "CURRENTMEDIAINFO";

        public static final String ISSHARE = "ISSHARE";

        /**
         * 为了解决BroadcastReceiver生命周期较短的问题，使用IntentService处理业务逻辑
         */
        public static final String RECEIVER_TO_SERVICE_ACTION = "RECEIVER_TO_SERVICE_ACTION";

        /**
         * 传输U盘挂载的路径
         */
        public static final String MOUNTED_PATH = "MOUNTED_PATH";

        /**
         * 传递音量调节类型
         */
        public static final String VOLUME_ADJUST_TYPE = "VOLUME_ADJUST_TYPE";

        /**
         * 传递音量设置的值
         */
        public static final String VOLUME_SET_VALUE = "VOLUME_SET_VALUE";

        /**
         * 云相册展示类型
         */
        public static final String CLOUD_DISPLAY_TYPE = "CLOUD_DISPLAY_TYPE";

        public static final String IS_REUSE_AUDIOPLAYER = "IS_REUSE_AUDIOPLAYER";

        /**
         * 云相册跳转类型
         */
        public static final String CLOUD_LOGIN_JUMP_TYPE = "CLOUD_LOGIN_JUMP_TYPE";
    }

    public static final class MCSMessage
    {

        // 不可用消息标示
        public static final int MSG_MCS_UNKNOWN = -1;

        // Player注册所需消息
        public static final int MSG_REGISTER_CALLBACK = 0;

        public static final int MSG_UNREGISTER_CALLBACK = 1;

        // player向Sender端发送的报告
        public static final int MSG_REPORT_ERROR = 2;

        public static final int MSG_REQUEST_MEDIA_LIST = 3;

        // 播控消息
        public static final int MSG_SET_MEDIA_DATA = 4;

        public static final int MSG_APPEND_MEDIA_DATA = 5;

        public static final int MSG_PLAY = 6;

        public static final int MSG_PAUSE = 7;

        public static final int MSG_SEEK = 8;

        public static final int MSG_STOP = 9;

        public static final int MSG_PLAYER_LIST = 10;

        // 设备下线消息
        public static final int MSG_DEVICE_DOWN = 11;

        public static final int MSG_DURATION = 12;

        // 当前曲目
        public static final int MSG_CURRENT_PLAYING_PROGRAM = 13;

        // 新需求，DMC调节音量的消息
        public static final int MSG_ADJUST_VOLUME = 14;

        public static final int MSG_GET_TIME = 777888;

        public static final int MSG_UPDATE_TIME = 777889;

    }

    public static final class MediaRequestType
    {
        /**
         * 未知类型
         */
        public static final int UNKNOWND_TYPE = -1;

        /**
         * 上一个媒体文件
         */
        public static final int PREVIOUS_MEDIA = 0;

        /**
         * 下一个媒体文件
         */
        public static final int NEXT_MEDIA = 1;

        /**
         * 上一页媒体列表
         */
        public static final int PREVIOUS_PAGE = 2;

        /**
         * 下一页媒体列表
         */
        public static final int NEXT_PAGE = 3;

        /**
         * 所有媒体的列表
         */
        public static final int ALL_MEDIA_LIST = 4;
    }

    /**
     * 
     * 
     * VolumeAdjustType:推送端或甩屏端控制播放器调节音量的类型
     * 
     * 2012-3-17 下午04:35:18
     * 
     * @version 1.0.0
     * 
     */
    public static final class VolumeAdjustType
    {
        /**
         * 音量调节类型
         */
        public static final int ADJUST_UNKNOWND = -1;

        /**
         * 音量减小
         */
        public static final int ADJUST_LOWER = 0;

        /**
         * 音量不变
         */
        public static final int ADJUST_SAME = 1;

        /**
         * 音量增加
         */
        public static final int ADJUST_RAISE = 2;

        /**
         * 开启静音
         */
        public static final int ADJUST_MUTE_ON = 3;

        /**
         * 关闭静音
         */
        public static final int ADJUST_MUTE_OFF = 4;

        /**
         * 设置指定的音量值
         */
        public static final int ADJUST_SET = 5;
    }

    //
    /**
     * 
     * 
     * ServiceConnectionMSG service连接消息 2011-10-31 下午07:16:29
     * 
     * @version 1.0.0
     * 
     */
    public static final class ServiceConnectionMSG
    {

        /**
         * 断开连接
         */
        public static final int MSG_SERVICE_DISCONNECTED = 0;
    }

    public static final class ClientTypeUniq
    {
        /**
         * UNKNOWN
         */
        public static final String UNKNOWN_UNIQ = "com.rockchips.iptv.dlna.UNKNOWN";

        /**
         * DMS客户端的唯一标识
         */
        public static final String DMS_UNIQ = "com.rockchips.iptv.dlna.DMSClient";

        /**
         * 推送端的唯一标识
         */
        public static final String PUSH_UNIQ = "com.rockchips.iptv.dlna.PushClient";

        /**
         * 甩屏端的唯一标识
         */
        public static final String SYN_UINQ = "com.rockchips.iptv.dlna.SynClient";
    }

    public static final class AudioPlayerMsg
    {

        /**
         * 请求播放消息
         */
        public static final int MSG_REQUEST_PLAY = 1000;

        /**
         * 请求seek消息
         */
        public static final int MSG_REQUEST_SEEKTO = 102;

        /**
         * 请求退出消息
         */
        public static final int MSG_REQUEST_EXIT = 103;

        /**
         * 请求更新专辑封面消息
         */
        public static final int MSG_REQUEST_UPDATECOVER = 105;

        /**
         * 请求下载歌词消息
         */
        public static final int MSG_REQUEST_LYRIC = 106;

        /**
         * 请求刷新媒体相关信息（专辑名、艺术家）消息
         */
        public static final int MSG_REQUEST_REFRESH_MEDIAINFO = 107;

        /**
         * 播放音乐消息
         */
        public static final int MSG_CONTROL_PLAY = 201;

        /**
         * 暂停音乐消息
         */
        public static final int MSG_CONTROL_PAUSE = 202;

        /**
         * 快进消息
         */
        public static final int MSG_CONTROL_FASTFORWORD = 204;

        /**
         * 快退消息
         */
        public static final int MSG_CONTROL_FASTBACKWARD = 205;

        /**
         * 音乐播放错误消息
         */
        public static final int MSG_PROC_ERROR = 301;

        /**
         * 音乐播放完成消息
         */
        public static final int MSG_PROC_COMPLETED = 302;

        /**
         * 同步播放进度消息
         */
        public static final int MSG_SYNC_POSTION = 402;

        /**
         * 同步歌词消息
         */
        public static final int MSG_SYNC_LYRIC = 403;

        /**
         * 重设歌词消息
         */
        public static final int MSG_RESET_LYRIC = 404;

        /**
         * 设置音乐播放列表datalist消息
         */
        public static final int MSG_SET_PLAYLIST_DATA = 405;

        /**
         * 设置音乐播放列表adapter消息
         */
        public static final int MSG_SET_PLAYLIST_ADAPTER = 406;

        /**
         * 显示无歌词提示消息
         */
        public static final int MSG_ISSHOW_NO_LYRIC_TEXT = 407;

        /**
         * 设置歌词消息
         */
        public static final int MSG_SET_LYRICLIST = 501;

        /**
         * 更新剩余播放时长消息
         */
        public static final int MSG_UPDATE_REMAININGDURATION = 502;

        /**
         * 滚动播放列表消息
         */
        public static final int MSG_SCROLL_PLAYLIST = 503;

        /**
         * 刷新播放、暂停图标消息
         */
        public static final int MSG_REFRESH_PLAYICON = 504;

        /**
         * 刷新专辑封面消息
         */
        public static final int MSG_REFRESH_ALBUMICON = 505;

        /**
         * 请求隐藏弹出菜单消息
         */
        public static final int MSG_REQUEST_DISMISS_POPUPWINDOW = 506;

        /**
         * 随机滚动播放列表消息
         */
        public static final int MSG_RANDOM_SCROLL_PLAYLIST = 507;

        /**
         * 请求播放列表焦点重新回到当前正在播放的歌曲上
         */
        public static final int MSG_REQUEST_PLAYLIST_RESTORE = 508;

        /**
         * 更新音乐播放进度消息
         */
        public static final int MSG_UPDATE_MUSIC_PROGRESS = 509;

        /**
         * 更新音乐总进度消息
         */
        public static final int MSG_UPDATE_MUSIC_TOTALDURATION = 510;

        /**
         * 推送音乐播放完毕延时消息
         */
        public static final int PUAH_MEDIAFILE_PLAY_COMPLETE = 511;

        /**
         * 推送音乐seek消息
         */
        public static final int PUAH_MEDIAFILE_PLAY_SEEK = 512;

        /**
         * 弹出菜单延迟消失时间
         */
        public static final int POPUPWINDOW_DISMISS_DELAYMILLIS = 5000;
    }

    /**
     * 播放状态：播放、暂停
     */
    public enum PlayState
    {
        PLAY, PAUSE
    }
	
	public static final class ScreenMode
    {
        /**
         * 全屏大小
         */
        public static final int SCREEN_FULL = 1;
        
        /**
         * 原始大小
         */
        public static final int SCREEN_ORIGINAL = 2;
        
        /**
         * 等比拉伸
         */
        public static final int SCREEN_SCALE = 3;        
    }
    
    //zkf61715
    public static final class PlayMode
    {
        public static final int PLAY_COMMON = 0;
        // 快进快退模式
        public static final int PLAY_TRICK = 1;
        // 快速拖动模式
        public static final int PLAY_SEEK = 2;
    }

    public static final class BroadcastMsg
    {
        // 消息广播相关------开始

        /**
         * 添加共享目录
         */
        public static final String ACTION_ON_DMS_ADDSHARE = "com.rockchips.dlna.service.action.addShare";

        /**
         * 添加共享目录返回状态
         */
        public static final String ACTION_ON_DMS_ADDSHARE_RESULT = "com.rockchips.dlna.service.action.addShare.result";

        /**
         * 添加共享目录回结果消息
         */
        public static final int MSG_ACTION_ON_DMS_ADDSHARE_RESULT = 0X0514;

        /**
         * 删除共享目录
         */
        public static final String ACTION_ON_DMS_DELSHARE = "com.rockchips.dlna.service.action.delShare";

        /**
         * 删除共享目录返回状态
         */
        public static final String ACTION_ON_DMS_DELSHARE_RESULT = "com.rockchips.dlna.service.action.delShare.result";

        /**
         * 删除共享目录回结果消息
         */
        public static final int MSG_ACTION_ON_DMS_DELSHARE_RESULT = 0X0515;

        /**
         * UPNP init通知
         */
        public static final String ACTION_ON_STACK_INIT = "com.rockchips.dlna.service.action.OnStackInit";

        /**
         * 设备上线通知
         */
        public static final String ACTION_ON_DMS_UP = "com.rockchips.dlna.service.action.OnDMSUp";

        public static final int MSG_ACTION_ON_DMS_UP = 1;

        /**
         * 设备下线通知
         */
        public static final String ACTION_ON_DMS_DOWN = "com.rockchips.dlna.service.action.OnDMSDown";

        public static final int MSG_ACTION_ON_DMS_DOWN = 2;

        /**
         * // 通知设备刷新
         */
        public static final String ACTION_ON_DMS_SEARCH = "com.rockchips.dlna.mymedia.dms.search";

        /**
         * 特定DMS设备内容更新通知
         */
        public static final String ACTION_ON_DMS_BROWSE_RESULT = "com.rockchips.dlna.service.action.OnDMSBrowseResult";

        public static final int MSG_ACTION_ON_DMS_BROWSE_RESULT = 3;

        /**
         * 设备的挂载（U盘）
         */
        public static final String ACTION_ON_MEDIA_MOUTED = Intent.ACTION_MEDIA_MOUNTED;

        public static final int MSG_ACTION_ON_MEDIA_MOUTED = 4;

        /**
         * 设备的卸载（U盘）
         */
        public static final String ACTION_ON_MEDIA_UNMOUTED = Intent.ACTION_MEDIA_UNMOUNTED;

        public static final int MSG_ACTION_ON_MEDIA_UNMOUTED = 5;

        /**
         * 扫描完成（U盘）
         */
        public static final String ACTION_ON_MEDIA_SCANNER_FINISHED = Intent.ACTION_MEDIA_SCANNER_FINISHED;

        public static final int MSG_ACTION_ON_MEDIA_SCANNER_FINISHED = 6;

        /**
         * DMC通知DMR需要播放的媒体URI
         */
        public static final String ACTION_ON_SET_MEDIA_URI = "com.rockchips.dlna.service.action.OnSetMediaUri";

        public static final int MSG_ACTION_ON_SET_MEDIA_URI = 7;

        /**
         * DMC通知DMR开始播放
         */
        public static final String ACTION_ON_PLAY = "com.rockchips.dlna.service.action.OnPlay";

        public static final int MSG_ACTION_ON_PLAY = 8;

        /**
         * DMC通知DMR暂停播放
         */
        public static final String ACTION_ON_PAUSE = "com.rockchips.dlna.service.action.OnPause";

        public static final int MSG_ACTION_ON_PAUSE = 9;

        /**
         * DMC通知DMR停止播放
         */
        public static final String ACTION_ON_STOP = "com.rockchips.dlna.service.action.OnStop";

        public static final int MSG_ACTION_ON_STOP = 10;

        /**
         * DMC通知DMR定位播放
         */
        public static final String ACTION_ON_SEEK = "com.rockchips.dlna.service.action.OnSeek";

        public static final int MSG_ACTION_ON_SEEK = 11;

        /**
         * 网络状态变化（网络断开或者连接）
         */
        public static final String ACTION_ON_NETWORK_STATE_CHANGED = "android.net.ethernet.STATE_CHANGE";

        public static final String ACTION_ON_NETWORK_ETH_STATE_CHANGED = "android.net.ethernet.ETH_STATE_CHANGED";

        public static final int MSG_ACTION_ON_NETWORK_CONNECTED = 12;

        public static final int MSG_ACTION_ON_NETWORK_DISCONNECTED = 13;

        /**
         * 视频通话
         */
        public static final String ACTION_VPSERVICE_CALLING = "com.rockchips.iptv.vpservice.action.calling";

        public static final String ACTION_VPSERVICE_CALLED = "com.rockchips.iptv.vpservice.action.called";

        /**
         * 扩展参数
         */
        public static final String EXTRA_RESERVE = "com.rockchips.dlna.service.extra.reserve";

        /**
         * 扩展参数:推送的Seek模式
         */
        public static final String EXTRA_SEEK_MODE = "com.rockchips.dlna.service.extra.seekMode";

        /**
         * 扩展参数:推送的Seek位置
         */
        public static final String EXTRA_SEEK_TARGET = "com.rockchips.dlna.service.extra.seekTarget";

        /**
         * 媒体文件URI
         */
        public static final String EXTRA_URI = "com.rockchips.dlna.service.extra.uri";

        /**
         * 媒体信息
         */
        public static final String EXTRA_MEDIA_INFO = "com.rockchips.dlna.service.extra.mediaInfo";

        /**
         * 设备ID
         */
        public static final String EXTRA_DEVICE_ID = "com.rockchips.dlna.service.extra.deviceID";
        
        public static final String ACTION_ADD_FILE_SHARE_RESULT = "com.rockchips.mediacenter.addshare";
        public static final String ACTION_CANCEL_FILE_SHARE_RESULT = "com.rockchips.mediacenter.cancelshare";
        public static final String EXTRA_FILE_SHARE_RESULT = "com.rockchips.mediacenter.extra.share.result";
        // 消息广播相关------结束
    }
    
    /**
     * 
     * 
     * 共享状态
     * 
     * 2011-10-31 下午02:25:53
     * 
     * @version 1.0.0
     *
     */
    public static final class ShareState
    {
        /**
         * 可以共享
         */
        public static final int SHARE_STATE_ENABLE = 101;
        
        /**
         * 不可以共享
         */
        public static final int SHARE_STATE_UNABLE = 102;
        
        /**
         * 已共享
         */
        public static final int SHARE_STATE_SHARED = 103;
        /**
         * 正在共享
         */
        public static final int SHARE_STATE_SHARING = 104;        
        /**
         * 正在取消共享
         */
        public static final int SHARE_STATE_CANCEL_SHARING = 105;
    }
    
    public static final String EXTRA_IS_SEARCH = "is_search";
    
    public static final String MEDIACENTER_PERMISSION = "com.android.rockchips.permission.MEDIACENTER_SEND_RECV";
}
