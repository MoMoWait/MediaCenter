package com.hisilicon.android.mediaplayer;

/**
 * HiMediaPlayerInvoke interface<br>
 */
public class HiMediaPlayerInvoke
{
    /**
     * <br>
     * Set video frame decode mode<br>
     * <h3>Description:</h3> you could config decoder only decode I frames or IP
     * frames and so on.<br>
     * <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_SET_VIDEO_FRAME_MODE</p></td>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>int</p></td>
     * <td>{@see HiMediaPlayerDefine.ENUM_VIDEO_FRAME_MODE_ARG}</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>return value<br>
     * status code see system/core/include/utils/Errors.h</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> None
     */
    public final static int CMD_SET_VIDEO_FRAME_MODE = 0;

    /**
     * <br>
     * Get video frame decode mode<br>
     * <h3>Description:</h3> you could config decoder only decode I frames or IP
     * frames and so on.<br>
     * <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_SET_VIDEO_FRAME_MODE</p></td>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>int</p></td>
     * <td>{@see HiMediaPlayerDefine.ENUM_VIDEO_FRAME_MODE_ARG}</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>return value<br>
     * status code see system/core/include/utils/Errors.h</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> None
     */
    public final static int CMD_GET_VIDEO_FRAME_MODE = 1;

    /**
     * <br>
     * Set conversion mode of AspectRatio.<br>
     * <h3>Description:</h3> you could set letter box or full screen mode,refer
     * to {@see com.hisilicon.android.mediaplayer.HiMediaPlayerDefine.
     * ENUM_VIDEO_CVRS_ARG}<br>
     * <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_SET_VIDEO_CVRS</p></td>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>int</p></td>
     * <td>{@see com.hisilicon.android.mediaplayer.HiMediaPlayerDefine.
     * ENUM_VIDEO_CVRS_ARG}</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>return value<br>
     * status code see system/core/include/utils/Errors.h</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> None
     */
    public final static int CMD_SET_VIDEO_CVRS = 2;

    /**
     * <br>
     * Get conversion mode of AspectRatio.<br>
     * <h3>Description:</h3> to know the value you will get,please refer to
     * {@see com.hisilicon.android.mediaplayer.HiMediaPlayerDefine.
     * ENUM_VIDEO_CVRS_ARG}<br>
     * <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_GET_VIDEO_CVRS</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>return value<br>
     * status code see system/core/include/utils/Errors.h</p></td>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>int</p></td>
     * <td>{@see com.hisilicon.android.mediaplayer.HiMediaPlayerDefine.
     * ENUM_VIDEO_CVRS_ARG}</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> None
     */
    public final static int CMD_GET_VIDEO_CVRS = 3;

    /**
     * <br>
     * Set mute.<br>
     * <h3>Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_SET_AUDIO_MUTE_STATUS</p></td>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>int</p></td>
     * <td>{@see HiMediaPlayerDefine.ENUM_AUDIO_MUTE_STATUS_ARG}</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>return value<br>
     * status code see system/core/include/utils/Errors.h</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> None
     */
    public final static int CMD_SET_AUDIO_MUTE_STATUS = 4; // set mute status

    /**
     * <br>
     * Get mute status.<br>
     * <h3>Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_GET_AUDIO_MUTE_STATUS</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>return value<br>
     * status code see system/core/include/utils/Errors.h</p></td>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>int</p></td>
     * <td>{@see HiMediaPlayerDefine.ENUM_AUDIO_MUTE_STATUS_ARG}</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> None
     */
    public final static int CMD_GET_AUDIO_MUTE_STATUS = 5;

    /**
     * <br>
     * Set audio channel mode.<br>
     * <h3>Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_SET_AUDIO_CHANNEL_MODE</p></td>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>int</p></td>
     * <td>{@see HiMediaPlayerDefine.ENUM_AUDIO_CHANNEL_MODE_ARG}</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>return value<br>
     * status code see system/core/include/utils/Errors.h</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> None
     */
    public final static int CMD_SET_AUDIO_CHANNEL_MODE = 6;

    /**
     * <br>
     * Get audio channel mode.{@see
     * HiMediaPlayerDefine.ENUM_AUDIO_CHANNEL_MODE_ARG}<br>
     * <h3>Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_GET_AUDIO_CHANNEL_MODE</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>return value<br>
     * status code see system/core/include/utils/Errors.h</p></td>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>int</p></td>
     * <td>{@see HiMediaPlayerDefine.ENUM_AUDIO_CHANNEL_MODE_ARG}</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> None
     */
    public final static int CMD_GET_AUDIO_CHANNEL_MODE = 7; // get audio channel
                                                            // mode

    /**
     * <br>
     * Set sync mode.{@see HiMediaPlayerDefine.ENUM_SYNC_MODE_ARG}<br>
     * <h3>Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_SET_AV_SYNC_MODE</p></td>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>int</p></td>
     * <td>{@see HiMediaPlayerDefine.ENUM_SYNC_MODE_ARG}</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>return value<br>
     * status code see system/core/include/utils/Errors.h</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> None
     */
    public final static int CMD_SET_AV_SYNC_MODE = 8;

    /**
     * <br>
     * Get sync mode.{@see HiMediaPlayerDefine.ENUM_SYNC_MODE_ARG}<br>
     * <h3>Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_GET_AV_SYNC_MODE</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>return value<br>
     * status code see system/core/include/utils/Errors.h</p></td>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>int</p></td>
     * <td>{@see HiMediaPlayerDefine.ENUM_SYNC_MODE_ARG}</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> None
     */
    public final static int CMD_GET_AV_SYNC_MODE = 9; // get AV sync mode

    /**
     * <br>
     * <b>suggest using method {@see
     * com.hisilicon.android.mediaplayer.HiMediaPlayer#setFreezeMode}<b><br>
     */
    public final static int CMD_SET_VIDEO_FREEZE_MODE = 10; // set video freeze
                                                            // mode

    /**
     * <br>
     * <b>suggest using method {@see
     * com.hisilicon.android.mediaplayer.HiMediaPlayer#getFreezeMode}<b><br>
     */
    public final static int CMD_GET_VIDEO_FREEZE_MODE = 11; // get video freeze
                                                            // mode

    /**
     * <br>
     * Set audio track.<br>
     * 
     * <h3>Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_SET_AUDIO_TRACK_PID</p></td>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>int</p></td>
     * <td>the index number(0 based) of audio track</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>return value<br>
     * status code see system/core/include/utils/Errors.h</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> None
     */
    public final static int CMD_SET_AUDIO_TRACK_PID = 12;

    /**
     * <br>
     * Get audio track selected currently.<br>
     * <h3>Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_GET_AUDIO_TRACK_PID</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>return value<br>
     * status code see system/core/include/utils/Errors.h</p></td>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>int</p></td>
     * <td>the index number selected of audio track</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> None
     */
    public final static int CMD_GET_AUDIO_TRACK_PID = 13;

    /**
     * <br>
     * Seek by pos.<br>
     * <h3>Description:</h3> if you want to seek by time(ms),you can use {@see
     * com.hisilicon.android.mediaplayer.HiMediaPlayer#seekTo}<br>
     * <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_SET_SEEK_POS</p></td>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>int</p></td>
     * <td>High 32 bit of pos</p></td>
     * </tr>
     * <tr>
     * <td>2</p></td>
     * <td>int</p></td>
     * <td>low 32 bit of pos</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>return value<br>
     * status code see system/core/include/utils/Errors.h</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> None
     */
    public final static int CMD_SET_SEEK_POS = 15;

    /**
     * @deprecated
     */
    public final static int CMD_SET_EXT_SUBTITLE_FILE = 16;

    /**
     * @deprecated
     */
    public final static int CMD_GET_EXT_SUBTITLE_FILE = 17;

    /**
     * @deprecated
     */
    public final static int CMD_SET_TPLAY = 18;

    /**
     * @deprecated
     */
    public final static int CMD_GET_TPLAY = 19;

    /**
     * @deprecated
     */
    public final static int CMD_SET_AUDIO_VOLUME = 20;

    /**
     * @deprecated
     */
    public final static int CMD_GET_AUDIO_VOLUME = 21;

    /**
     * @deprecated
     */
    public final static int CMD_SET_SURFACE_SIZE = 22;

    /**
     * @deprecated
     */
    public final static int CMD_GET_SURFACE_SIZE = 23;

    /**
     * @deprecated
     */
    public final static int CMD_SET_SURFACE_POSITION = 24;

    /**
     * @deprecated
     */
    public final static int CMD_GET_SURFACE_POSITION = 25;

    /**
     * <br>
     * Set video ratio.for example 16:9 or 4:3<br>
     * <h3>Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_SET_SURFACE_RATIO</p></td>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>int</p></td>
     * <td>with of video ratio</p></td>
     * </tr>
     * <tr>
     * <td>2</p></td>
     * <td>int</p></td>
     * <td>height of video ratio</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>return value<br>
     * status code see system/core/include/utils/Errors.h</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> None
     */
    public final static int CMD_SET_SURFACE_RATIO = 26;

    /**
     * @deprecated
     */
    public final static int CMD_GET_SURFACE_RATIO = 27;

    /**
     * <br>
     * <b>suggest using method {@see
     * com.hisilicon.android.mediaplayer.HiMediaPlayer#getMediaInfo}</b><br>
     */
    public final static int CMD_GET_FILE_INFO = 28;

    /**
     * @deprecated
     */
    public final static int CMD_GET_PLAYER_INFO = 29;

    /**
     * @deprecated
     */
    public final static int CMD_GET_STREM_3DMODE = 30;

    /**
     * @deprecated
     */
    public final static int CMD_SET_STREM_3DMODE = 31;

    /**
     * <br>
     * Get all audio track format info.<br>
     * <h3>Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_GET_AUDIO_INFO</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>return value<br>
     * status code see system/core/include/utils/Errors.h</p></td>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>int</p></td>
     * <td>Audio track count</p></td>
     * </tr>
     * <tr>
     * <td>2</p></td>
     * <td>string</p></td>
     * <td>Language of every track</p></td>
     * </tr>
     * <tr>
     * <td>3</p></td>
     * <td>int</p></td>
     * <td>Audio format of every track {@see
     * com.hisilicon.android.mediaplayer.HiMediaPlayerDefine.
     * DEFINE_AUDIO_ENCODING_FORMAT}</p></td>
     * </tr>
     * <tr>
     * <td>4</p></td>
     * <td>int</p></td>
     * <td>Sample rate of every track</p></td>
     * </tr>
     * <tr>
     * <td>5</p></td>
     * <td>int</p></td>
     * <td>Channels of every track</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> if the file has two audio track.the arrangement in
     * parcel is:<br>
     * (result)<br>
     * (Audio track count)<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;(Language1)<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;(Audio format1)<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;(Sample rate1)<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;(Channels1)<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;(Language2)<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;(Audio format2)<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;(Sample rate2)<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;(Channels2)<br>
     * so you must know "Audio track count" first,then you can use for() loop to
     * get every track info.<br>
     */
    public final static int CMD_GET_AUDIO_INFO = 32; // 32 get audio info

    /**
     * <br>
     * Get video format info.<br>
     * <h3>Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_GET_VIDEO_INFO</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>return value<br>
     * status code see system/core/include/utils/Errors.h</p></td>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>int</p></td>
     * <td>{@see HiMediaPlayerDefine.DEFINE_VIDEO_ENCODING_FORMAT}</p></td>
     * </tr>
     * <tr>
     * <td>2</p></td>
     * <td>int</p></td>
     * <td>Fps Integer</p></td>
     * </tr>
     * <tr>
     * <td>3</p></td>
     * <td>int</p></td>
     * <td>Fps Decimal</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> None
     */
    public final static int CMD_GET_VIDEO_INFO = 33;

    /**
     * <br>
     * <b>suggest using method {@see
     * com.hisilicon.android.mediaplayer.HiMediaPlayer#setVideoRange}<b><br>
     */
    public final static int CMD_SET_OUTRANGE = 100;

    /**
     * <br>
     * Set Subtitle track.<br>
     * <h3>Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_SET_SUB_ID</p></td>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>int</p></td>
     * <td>Subtitle track index num</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>return value<br>
     * status code see system/core/include/utils/Errors.h</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> None
     */
    public final static int CMD_SET_SUB_ID = 101; // set subtitle ID

    /**
     * <br>
     * Get Subtitle track selected currently.<br>
     * <h3>Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_GET_SUB_ID</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>The index of current display subtitle</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> None
     */
    public final static int CMD_GET_SUB_ID = 102;

    /**
     * <br>
     * Get all subtitle info.<br>
     * <h3>Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_GET_SUB_INFO</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>return value<br>
     * status code see system/core/include/utils/Errors.h</p></td>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>int</p></td>
     * <td>Subtitle count</p></td>
     * </tr>
     * <tr>
     * <td>2</p></td>
     * <td>int</p></td>
     * <td>Subtitle index(0 based)</p></td>
     * </tr>
     * <tr>
     * <td>3</p></td>
     * <td>int</p></td>
     * <td>If Ext Subtitle</p></td>
     * </tr>
     * <tr>
     * <td>4</p></td>
     * <td>string</p></td>
     * <td>Subtitle language(if no language info,"-" return)</p></td>
     * </tr>
     * <tr>
     * <td>5</p></td>
     * <td>int</p></td>
     * <td>{@see HiMediaPlayerDefine.DEFINE_SUBT_FORMAT}</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> if the file has two subtitle track.the arrangement in
     * parcel is:<br>
     * (result)<br>
     * (Subtitle count)<br>
     * (Subtitle index1)<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;(If Ext Subtitle1)<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;(Subtitle language1)<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;(format1)<br>
     * 
     * &nbsp;&nbsp;&nbsp;&nbsp;(Subtitle index2)<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;(If Ext Subtitle2)<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;(Subtitle language2)<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;(format2)<br>
     * so you must know "Subtitle count" first,then you can use for() loop to
     * get every track info.<br>
     */
    public final static int CMD_GET_SUB_INFO = 103; // get subtitle info,
                                                    // subtitle
                                                    // Id、external/internal、sub_TITLE

    /**
     * <br>
     * Set font size.<br>
     * <h3>Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_SET_SUB_FONT_SIZE</p></td>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>int</p></td>
     * <td>font size</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>return value<br>
     * status code see system/core/include/utils/Errors.h</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> only for text subtitle,subtitle type is {@see
     * HiMediaPlayerDefine.DEFINE_SUBT_FORMAT}
     */
    public final static int CMD_SET_SUB_FONT_SIZE = 104; // set font size

    /**
     * <br>
     * Get font size.<br>
     * <h3>Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_GET_SUB_FONT_SIZE</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>font size</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> only for text subtitle,subtitle type is {@see
     * HiMediaPlayerDefine.DEFINE_SUBT_FORMAT}
     */
    public final static int CMD_GET_SUB_FONT_SIZE = 105; // get font size

    /**
     * <br>
     * Set subtitle font position.<br>
     * <h3>Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_SET_SUB_FONT_POSITION</p></td>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>int</p></td>
     * <td>X coordinate</p></td>
     * </tr>
     * <tr>
     * <td>2</p></td>
     * <td>int</p></td>
     * <td>Y coordinate</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>None</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> None
     */
    public final static int CMD_SET_SUB_FONT_POSITION = 106; // set subtitle
                                                             // position

    /**
     * <br>
     * Get subtitle font position.<br>
     * <h3>Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_GET_SUB_FONT_POSITION</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>X coordinate</p></td>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>int</p></td>
     * <td>Y coordinate</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> None
     */
    public final static int CMD_GET_SUB_FONT_POSITION = 107; // get subtitle
                                                             // position

    /**
     * <br>
     * Set subtitle font horizontal position.<br>
     * <h3>Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_SET_SUB_FONT_HORIZONTAL</p></td>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>int</p></td>
     * <td>X coordinate</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>None</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> None
     */
    public final static int CMD_SET_SUB_FONT_HORIZONTAL = 108;

    /**
     * <br>
     * Get subtitle font horizontal position.<br>
     * <h3>Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_GET_SUB_FONT_HORIZONTAL</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>X coordinate</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> None
     */
    public final static int CMD_GET_SUB_FONT_HORIZONTAL = 109;

    /**
     * <br>
     * <b>suggest using method {@see
     * com.hisilicon.android.mediaplayer.HiMediaPlayer#setSubVertical}<b><br>
     */
    public final static int CMD_SET_SUB_FONT_VERTICAL = 110; // set subtitle
                                                             // vertical
                                                             // position

    /**
     * <br>
     * Get subtitle font vertical position.<br>
     * <h3>Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_GET_SUB_FONT_VERTICAL</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Y coordinate</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> None
     */
    public final static int CMD_GET_SUB_FONT_VERTICAL = 111; // get subtitle
                                                             // vertical
                                                             // position

    /**
     * <br>
     * Set subtitle font alignment mode.<br>
     * <h3>Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_SET_SUB_FONT_ALIGNMENT</p></td>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>int</p></td>
     * <td>{@see HiMediaPlayerDefine.ENUM_SUB_FONT_ALIGNMENT_ARG}</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>None</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> None
     */
    public final static int CMD_SET_SUB_FONT_ALIGNMENT = 112; // set subtitle
                                                              // alignment
                                                              // (Left/Center/Right)

    /**
     * <br>
     * Get subtitle font alignment mode.<br>
     * <h3>Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_GET_SUB_FONT_ALIGNMENT</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>{@see HiMediaPlayerDefine.ENUM_SUB_FONT_ALIGNMENT_ARG}</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> None
     */
    public final static int CMD_GET_SUB_FONT_ALIGNMENT = 113; // get subtitle
                                                              // alignment
                                                              // (Left/Center/Right)

    /**
     * <br>
     * Set sub font color<br>
     * <br>
     * <b>suggest using method {@see
     * com.hisilicon.android.mediaplayer.HiMediaPlayer#setSubFontColor}<b>
     */
    public final static int CMD_SET_SUB_FONT_COLOR = 114;

    /**
     * <br>
     * Get font color,only for text subtitle.<br>
     * <h3>Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_GET_SUB_FONT_COLOR</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>value from 0x000000 - 0xFFFFFF high byte is R,middle byte is G,low
     * byte is B</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> only for text subtitle,subtitle type is {@see
     * HiMediaPlayerDefine.DEFINE_SUBT_FORMAT}
     */
    public final static int CMD_GET_SUB_FONT_COLOR = 115; // get font color

    /**
     * <br>
     * Set font back color,only for text subtitle.<br>
     * <h3>Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_SET_SUB_FONT_BACKCOLOR</p></td>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>int</p></td>
     * <td>32bit value, from high to low byte indicate ARGB</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>None</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> only for text subtitle,subtitle type is {@see
     * HiMediaPlayerDefine.DEFINE_SUBT_FORMAT}
     */
    public final static int CMD_SET_SUB_FONT_BACKCOLOR = 116; // set font
                                                              // backcolor ARGB
                                                              // -32Bits, only
                                                              // for character
                                                              // subtitle

    /**
     * <br>
     * Get font back color.<br>
     * <h3>Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_GET_SUB_FONT_BACKCOLOR</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>32bit value, from high to low byte indicate ARGB</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> only for text subtitle,subtitle type is {@see
     * HiMediaPlayerDefine.DEFINE_SUBT_FORMAT}
     */
    public final static int CMD_GET_SUB_FONT_BACKCOLOR = 117; // get font
                                                              // backcolor

    /**
     * <br>
     * Set font shadow style,only for text subtitle.<br>
     * <h3>Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_SET_SUB_FONT_SHADOW</p></td>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>int</p></td>
     * <td>if > 0，set shadow style,if <= 0 close shadow style</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>None</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> only for text subtitle,subtitle type is {@see
     * HiMediaPlayerDefine.DEFINE_SUBT_FORMAT}
     */
    public final static int CMD_SET_SUB_FONT_SHADOW = 118; // set font shadow,
                                                           // only for character
                                                           // subtitle

    /**
     * <br>
     * Get if font is shadow style.<br>
     * <h3>Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_GET_SUB_FONT_SHADOW</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>if 1,shadow style,0 not</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> only for text subtitle,subtitle type refers to {@see
     * HiMediaPlayerDefine.DEFINE_SUBT_FORMAT}
     */
    public final static int CMD_GET_SUB_FONT_SHADOW = 119; // get font shadow

    /**
     * <br>
     * Set font hollow style,only for text subtitle.<br>
     * <h3>Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_SET_SUB_FONT_HOLLOW</p></td>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>int</p></td>
     * <td>if > 0，set hollow style,if <= 0 close hollow style</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>None</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> only for text subtitle,subtitle type is {@see
     * HiMediaPlayerDefine.DEFINE_SUBT_FORMAT}
     */
    public final static int CMD_SET_SUB_FONT_HOLLOW = 120; // set font hollow,
                                                           // only for character
                                                           // subtitle

    /**
     * <br>
     * Get if font is hollow style.<br>
     * <h3>Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_GET_SUB_FONT_HOLLOW</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>if 1,hollow style,0 not</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> only for text subtitle,subtitle type refers to {@see
     * HiMediaPlayerDefine.DEFINE_SUBT_FORMAT}
     */
    public final static int CMD_GET_SUB_FONT_HOLLOW = 121;

    /**
     * <br>
     * <b>suggest using method {@see
     * com.hisilicon.android.mediaplayer.HiMediaPlayer#setSubFontSpace}<b><br>
     */
    public final static int CMD_SET_SUB_FONT_SPACE = 122;

    /**
     * <br>
     * Get space between characters in subtitle.only for text subtitle<br>
     * <h3>Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_GET_SUB_FONT_SPACE</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>space value between fonts</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> only for text subtitle,subtitle type is {@see
     * HiMediaPlayerDefine.DEFINE_SUBT_FORMAT}
     */
    public final static int CMD_GET_SUB_FONT_SPACE = 123; // get font space

    /**
     * <br>
     * <b>suggest using method {@see
     * com.hisilicon.android.mediaplayer.HiMediaPlayer#setSubFontLineSpace}<b><br>
     */
    public final static int CMD_SET_SUB_FONT_LINESPACE = 124; // set font
                                                              // linespace, only
                                                              // for character
                                                              // subtitle

    /**
     * <br>
     * Get sub font space between lines.<br>
     * <h3>Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_GET_SUB_FONT_LINESPACE</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>space value between lines</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> only for text subtitle,subtitle type refers to {@see
     * HiMediaPlayerDefine.DEFINE_SUBT_FORMAT}
     */
    public final static int CMD_GET_SUB_FONT_LINESPACE = 125; // get font
                                                              // linespace

    /**
     * <br>
     * Set font file path.<br>
     * <h3>Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>String</p></td>
     * <td>Value of CMD_SET_SUB_FONT_PATH</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>return value<br>
     * status code see system/core/include/utils/Errors.h</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> None
     */
    public final static int CMD_SET_SUB_FONT_PATH = 126; // set font_file path

    /**
     * <br>
     * Get subtitle font file path.<br>
     * <h3>Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_GET_SUB_FONT_PATH</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>String</p></td>
     * <td>Path of font file</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> None
     */
    public final static int CMD_GET_SUB_FONT_PATH = 127; // get font_file path

    /**
     * <br>
     * <b>suggest using method {@see
     * com.hisilicon.android.mediaplayer.HiMediaPlayer#setSubEncode}<b><br>
     */
    public final static int CMD_SET_SUB_FONT_ENCODE = 128; // set font encode
                                                           // charset, only for
                                                           // character subtitle

    /**
     * <br>
     * Get subtitle encode format.<br>
     * <h3>Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_GET_SUB_FONT_ENCODE</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>value of subtitle encode format<br>
     * {@see HiMediaPlayerDefine.DEFINE_SUBT_ENCODE_FORMAT}</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> only for text subtitle,subtitle type is {@see
     * HiMediaPlayerDefine.DEFINE_SUBT_FORMAT}
     */
    public final static int CMD_GET_SUB_FONT_ENCODE = 129; // get font encode
                                                           // charset

    /**
     * <br>
     * Set video/audio/subtitle display time offset.<br>
     * <h3>Description:</h3> if video,audio,subtitle is out of sync.you can
     * adjust to sync <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_SET_SUB_TIME_SYNC</p></td>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>int</p></td>
     * <td>video offset(ms)</p></td>
     * </tr>
     * <tr>
     * <td>2</p></td>
     * <td>int</p></td>
     * <td>audio offset(ms)</p></td>
     * </tr>
     * <tr>
     * <td>3</p></td>
     * <td>int</p></td>
     * <td>subtitle offset(ms)</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>return value<br>
     * status code see system/core/include/utils/Errors.h</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> None
     */
    public final static int CMD_SET_SUB_TIME_SYNC = 130; // set
                                                         // audio/video/subtitl
                                                         // sync timestamp

    /**
     * <br>
     * Get video/audio/subtitle display time offset.<br>
     * <h3>Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_GET_SUB_TIME_SYNC</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>return value<br>
     * status code see system/core/include/utils/Errors.h</p></td>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>int</p></td>
     * <td>video offset(ms)</p></td>
     * </tr>
     * <tr>
     * <td>2</p></td>
     * <td>int</p></td>
     * <td>audio offset(ms)</p></td>
     * </tr>
     * <tr>
     * <td>3</p></td>
     * <td>int</p></td>
     * <td>subtitle offset(ms)</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> None
     */
    public final static int CMD_GET_SUB_TIME_SYNC = 131; // get
                                                         // audio/video/subtitl
                                                         // sync timestamp

    /**
     * <br>
     * Import external subtitle <br>
     * <b>suggest using method {@see
     * com.hisilicon.android.mediaplayer.HiMediaPlayer#setSubPath}<b>
     */
    public final static int CMD_SET_SUB_EXTRA_SUBNAME = 132;

    /**
     * <br>
     * Set subtitle style <br>
     * <b>suggest using method {@see
     * com.hisilicon.android.mediaplayer.HiMediaPlayer#setSubFontStyle}<b>
     */
    public final static int CMD_SET_SUB_FONT_STYLE = 133;

    /**
     * <br>
     * Get last font style setting.<br>
     * <h3>Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_GET_SUB_FONT_STYLE</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>int</p></td>
     * <td>{@see HiMediaPlayerDefine.ENUM_SUB_FONT_STYLE_ARG}</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> only for text subtitle,subtitle type is {@see
     * HiMediaPlayerDefine.DEFINE_SUBT_FORMAT}
     */
    public final static int CMD_GET_SUB_FONT_STYLE = 134;

    /**
     * <br>
     * <b>suggest using method {@see
     * com.hisilicon.android.mediaplayer.HiMediaPlayer#enableSubtitle}<b><br>
     */
    public final static int CMD_SET_SUB_DISABLE = 135;

    /**
     * <br>
     * Get if subtitle disable.<br>
     * <h3>Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_GET_SUB_DISABLE</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>int</p></td>
     * <td>>0 disable,<=0 enable</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> None
     */
    public final static int CMD_GET_SUB_DISABLE = 136;

    /**
     * @deprecated
     */
    public final static int CMD_SET_SUB_LANGUAGE = 137;

    /**
     * @deprecated
     */
    public final static int CMD_GET_SUB_LANGUAGE = 138;

    /**
     * <br>
     * Check if is it bmp subtitle.<br>
     * <h3>Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_GET_SUB_ISBMP</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>return value<br>
     * status code see system/core/include/utils/Errors.h</p></td>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>int</p></td>
     * <td>1 bmp subtitle,0 not bmp subtitle</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> None
     */
    public final static int CMD_GET_SUB_ISBMP = 139; // get current subtitle is
                                                     // picture

    /**
     * <br>
     * Set subtitle config file.<br>
     * config file format you can see himediaplayer/jni/default_font_config.xml
     * <h3>Description:</h3> the default_font_config.xml include
     * "font file path",font size ...... <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_SET_SUB_CONFIG_PATH</p></td>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>String</p></td>
     * <td>subtitle config file path</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>return value<br>
     * status code see system/core/include/utils/Errors.h</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> None
     */
    public final static int CMD_SET_SUB_CONFIG_PATH = 140; // set
                                                           // font_config_file
                                                           // path

    /**
     * <br>
     * Set Volume lock.<br>
     * if lock set,you can not change volume by {@see
     * com.hisilicon.android.mediaplayer.HiMediaPlayer#setVolume} <h3>
     * Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_SET_VOLUME_LOCK</p></td>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>int</p></td>
     * <td>1 lock,0 unlock</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>return value<br>
     * status code see system/core/include/utils/Errors.h</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> None
     */
    public final static int CMD_SET_VOLUME_LOCK = 141; // set mediaplayer volume
                                                       // lock

    /**
     * <br>
     * Get volume setting lock state.<br>
     * <h3>Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_GET_VOLUME_LOCK</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>return value<br>
     * status code see system/core/include/utils/Errors.h</p></td>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>int</p></td>
     * <td>1 lock,0 unlock</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> None
     */
    public final static int CMD_GET_VOLUME_LOCK = 142; // get mediaplayer volume
                                                       // lock

    // 143-198 is reserved

    /**
     * <br>
     * stop ff/bw,resume normal speed play <br>
     * Please,use method {@see
     * com.hisilicon.android.mediaplayer.HiMediaPlayer#setSpeed}
     */
    public final static int CMD_SET_STOP_FASTPLAY = 199; // stop fast
                                                         // forword/backword

    /**
     * <br>
     * fast forward play operation <br>
     * Please,use method {@see
     * com.hisilicon.android.mediaplayer.HiMediaPlayer#setSpeed}
     */
    public final static int CMD_SET_FORWORD = 200; // fast forword

    /**
     * <br>
     * backward play operation <br>
     * Please,use method {@see
     * com.hisilicon.android.mediaplayer.HiMediaPlayer#setSpeed}
     */
    public final static int CMD_SET_REWIND = 201; // fast backword

    /**
     * @deprecated
     */
    public final static int CMD_SET_ZOOMIN = 202; // zoom in

    /**
     * @deprecated
     */
    public final static int CMD_SET_ZOOMOUT = 203; // zoom out

    /**
     * @deprecated
     */
    public final static int CMD_SET_SLOWLY = 204; // set play slowly

    /**
     * <br>
     * Get audio track number in current program.<br>
     * <h3>Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_GET_PID_NUMBER</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>return value<br>
     * status code see system/core/include/utils/Errors.h</p></td>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>int</p></td>
     * <td>audio track count number</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> None
     */
    public final static int CMD_GET_PID_NUMBER = 205; // get audio track count

    /**
     * <br>
     * Get program number.<br>
     * <h3>Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_GET_PROGRAM_NUMBER</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>return value<br>
     * status code see system/core/include/utils/Errors.h</p></td>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>int</p></td>
     * <td>program count number</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> None
     */
    public final static int CMD_GET_PROGRAM_NUMBER = 206;

    /**
     * @deprecated
     */
    public final static int CMD_GET_PROGRAM_STREAM_TYPE = 207; // get program
                                                               // stream type

    /**
     * @deprecated
     */
    public final static int CMD_SET_NET_SURFACE_RECT = 208; // set net play
                                                            // surface

    // 209-299 is reserved
    /**
     * <br>
     * Get hls stream count of multiple bit rate stream.<br>
     * <h3>Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_GET_HLS_STREAM_NUM</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>return value<br>
     * status code see system/core/include/utils/Errors.h</p></td>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>int</p></td>
     * <td>stream count</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> None
     */
    public final static int CMD_GET_HLS_STREAM_NUM = 300; // get hls streams
                                                          // count

    /**
     * <br>
     * you use methods {@see
     * com.hisilicon.android.mediaplayer.HiMediaPlayer#getNetworkInfo} to<br>
     * get download speed. <br>
     */
    public final static int CMD_GET_HLS_BANDWIDTH = 301;

    /**
     * <br>
     * Get info of the stream that you specify.<br>
     * <h3>Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_GET_HLS_STREAM_INFO</p></td>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>int</p></td>
     * <td>hls stream index you specify</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>return value<br>
     * status code see system/core/include/utils/Errors.h</p></td>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>int</p></td>
     * <td>stream index</p></td>
     * </tr>
     * <tr>
     * <td>2</p></td>
     * <td>int</p></td>
     * <td>stream bit rate</p></td>
     * </tr>
     * <tr>
     * <td>3</p></td>
     * <td>int</p></td>
     * <td>segment count</p></td>
     * </tr>
     * <tr>
     * <td>4</p></td>
     * <td>string</p></td>
     * <td>stream url</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> None
     */
    public final static int CMD_GET_HLS_STREAM_INFO = 302; // get specified
                                                           // stream info while
                                                           // hls

    /**
     * <br>
     * Get hls seg info that is downloading.<br>
     * <h3>Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_GET_HLS_SEGMENT_INFO</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>return value<br>
     * status code see system/core/include/utils/Errors.h</p></td>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>int</p></td>
     * <td>stream index which seg is in</p></td>
     * </tr>
     * <tr>
     * <td>2</p></td>
     * <td>int</p></td>
     * <td>seg total time(ms)</p></td>
     * </tr>
     * <tr>
     * <td>3</p></td>
     * <td>int</p></td>
     * <td>bandwith of stream which the seg is in </p></td>
     * </tr>
     * <tr>
     * <td>4</p></td>
     * <td>int</p></td>
     * <td>current seg index number</p></td>
     * </tr>
     * <tr>
     * <td>5</p></td>
     * <td>string</p></td>
     * <td>segment url</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> None
     */
    public final static int CMD_GET_HLS_SEGMENT_INFO = 303;

    /**
     * <br>
     * Get max bit rate stream info for playlist type file or stream.<br>
     * <h3>Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_GET_PLAYLIST_STREAM_DETAIL_INFO</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>return value<br>
     * status code see system/core/include/utils/Errors.h</p></td>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>int</p></td>
     * <td>playlist type.{@see HiMediaPlayerDefine.ENUM_PLAYLIST_TYPE_ARG}</p></td>
     * </tr>
     * <tr>
     * <td>2</p></td>
     * <td>int</p></td>
     * <td>bit rate</p></td>
     * </tr>
     * <tr>
     * <td>3</p></td>
     * <td>String</p></td>
     * <td>stream url</p></td>
     * </tr>
     * <tr>
     * <td>4</p></td>
     * <td>int</p></td>
     * <td>stream index</p></td>
     * </tr>
     * <tr>
     * <td>5</p></td>
     * <td>int</p></td>
     * <td>is live stream</p></td>
     * </tr>
     * <tr>
     * <td>6</p></td>
     * <td>int</p></td>
     * <td>Max duration of segments(ms)</p></td>
     * </tr>
     * <tr>
     * <td>7</p></td>
     * <td>int</p></td>
     * <td>Segment count</p></td>
     * </tr>
     * <tr>
     * <td>8</p></td>
     * <td>int</p></td>
     * <td>every segment starttime(ms)</p></td>
     * </tr>
     * <tr>
     * <td>9</p></td>
     * <td>int</p></td>
     * <td>every segment total time(ms)</p></td>
     * </tr>
     * <tr>
     * <td>10</p></td>
     * <td>String</p></td>
     * <td>every segment url</p></td>
     * </tr>
     * <tr>
     * <td>11</p></td>
     * <td>String</p></td>
     * <td>key info</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> you should use 'for' loop and 'Segment count' to read
     * every sections which has '*' prefix. for example:if 'Segment count' is
     * 3,so the parcel format arrange is (return value)...(Max duration of
     * segments(ms)),Segment count(3), (segment starttime1),(segment total
     * time1),(segment url1),(key info1),(segment starttime2),(segment total
     * time2),(segment url2),(key info2), (segment starttime3),(segment total
     * time3),(segment url3),(key info3)
     */
    public final static int CMD_GET_PLAYLIST_STREAM_DETAIL_INFO = 304;

    /**
     * <br>
     * Set stream index you want to play in multiple bit rate stream.<br>
     * <h3>Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_SET_HLS_STREAM_ID</p></td>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>int</p></td>
     * <td>stream index you want to set</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>return value<br>
     * status code see system/core/include/utils/Errors.h</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> None
     */
    public final static int CMD_SET_HLS_STREAM_ID = 305; // set indicated steam
                                                         // for playback while
                                                         // hls

    /**
     * <br>
     * set buffer cache size by byte size while play network stream <br>
     * suggest using method<br>
     * {@see
     * com.hisilicon.android.mediaplayer.HiMediaPlayer#setBufferSizeConfig}<br>
     */
    public final static int CMD_SET_BUFFERSIZE_CONFIG = 306; // set HiPlayer
                                                             // buffer config
                                                             // Kbytes

    /**
     * <br>
     * get buffer cache size by byte while playing network stream <br>
     * suggest using method<br>
     * {@see
     * com.hisilicon.android.mediaplayer.HiMediaPlayer#getBufferSizeConfig}<br>
     */
    public final static int CMD_GET_BUFFERSIZE_CONFIG = 307; // get HiPlayer
                                                             // Buffer config
                                                             // Kbytes

    /**
     * <br>
     * set buffer cache size by time while play network stream <br>
     * suggest using method<br>
     * {@see
     * com.hisilicon.android.mediaplayer.HiMediaPlayer#setBufferTimeConfig}<br>
     */
    public final static int CMD_SET_BUFFERTIME_CONFIG = 308; // set HiPlayer
                                                             // buffer config ms

    /**
     * <br>
     * get buffer cache size by time while play network stream. <br>
     * suggest using method<br>
     * {@see
     * com.hisilicon.android.mediaplayer.HiMediaPlayer#getBufferTimeConfig}<br>
     */
    public final static int CMD_GET_BUFFERTIME_CONFIG = 309; // get HiPlayer
                                                             // Buffer config ms

    /**
     * @deprecated
     */
    public final static int CMD_GET_BUFFER_STATUS = 310;

    /**
     * <br>
     * Get download speed<br>
     * Please,use method {@see
     * com.hisilicon.android.mediaplayer.HiMediaPlayer#getNetworkInfo}
     */
    public final static int CMD_GET_DOWNLOAD_SPEED = 311; // get DownloadSpeed
                                                          // bps

    /**
     * <br>
     * Set max buffer cache size.<br>
     * <h3>Description:</h3> this is only used for time buffer setting{@see
     * com.hisilicon.android.mediaplayer.HiMediaPlayer#setBufferTimeConfig}.<br>
     * e.g. if you set max buffer size 100MB，{@see
     * com.hisilicon.android.mediaplayer.HiMediaPlayer#setBufferTimeConfig} set
     * buffer full time 100s. The buffer cache can only store 100MB max,in spite
     * of if the duration of data has 100s. <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_SET_BUFFER_MAX_SIZE</p></td>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>int</p></td>
     * <td>Max buffer size(KByte)</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>return value<br>
     * status code see system/core/include/utils/Errors.h</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> you can also use {@see
     * com.hisilicon.android.mediaplayer.HiMediaPlayer#setBufferTimeConfig} to
     * set max size. refer to {@see
     * com.hisilicon.android.mediaplayer.BufferConfig#max}
     */
    public final static int CMD_SET_BUFFER_MAX_SIZE = 312; // set buffer maxsize
                                                           // Kbytes

    /**
     * <br>
     * Get max buffer cache size setting.<br>
     * <h3>Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_GET_BUFFER_MAX_SIZE</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>return value<br>
     * status code see system/core/include/utils/Errors.h</p></td>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>int</p></td>
     * <td>Max buffer size(KByte)</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> None
     */
    public final static int CMD_GET_BUFFER_MAX_SIZE = 313; // get buffer maxsize
                                                           // Kbytes

    /**
     * <br>
     * Set Z order of video.<br>
     * <h3>Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_SET_VIDEO_Z_ORDER</p></td>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>int</p></td>
     * <td>refer to parameter description {@see
     * HiMediaPlayerDefine.ENUM_VIDEO_Z_ORDER_ARG}</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>return value<br>
     * status code see system/core/include/utils/Errors.h</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> None
     */
    public final static int CMD_SET_VIDEO_Z_ORDER = 314; // set vo z order

    /**
     * <br>
     * If cut out subtitle in tab/sbs 3D file.<br>
     * <h3>Description:</h3> Mass tab/sbs 3d file has two same<br>
     * subtitle in two sides.we must cut out one sides subtitle to show<br>
     * subtitle normal.but some other tab/sbs 3d file has just one subtitle,<br>
     * so we should not cut out it <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_SET_3D_SUBTITLE_CUT_METHOD</p></td>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>int</p></td>
     * <td>refer to parameter description {@see
     * HiMediaPlayerDefine.ENUM_3D_SUBTITLE_CUT_METHOD_ARG}</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>return value<br>
     * status code see system/core/include/utils/Errors.h</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> None
     */
    public final static int CMD_SET_3D_SUBTITLE_CUT_METHOD = 315;

    /**
     * <br>
     * Set underrun mode.<br>
     * <h3>Description:</h3> refer to the method {@see
     * com.hisilicon.android.mediaplayer.HiMediaPlayer#setBufferTimeConfig} or<br>
     * {@see
     * com.hisilicon.android.mediaplayer.HiMediaPlayer#setBufferSizeConfig}.if
     * current buffer is less than 'start'<br>
     * setting,player will pause automatic,in order to accumulate more buffer to
     * play.when buffer<br>
     * rise up to 'enough' or more,HiMediaPlayer resume play automatic.User can
     * also disable the<br>
     * inner cache buffer management mechanism,and do it in app by the buffer
     * event come from event call back. <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_SET_BUFFER_UNDERRUN</p></td>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>int</p></td>
     * <td>1--open 0--close</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>return value<br>
     * status code see system/core/include/utils/Errors.h</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> you can refer to {@see
     * com.hisilicon.android.mediaplayer.HiMediaPlayer#setBufferTimeConfig} to
     * know more about buffer cache control.
     */
    public final static int CMD_SET_BUFFER_UNDERRUN = 316; // set buffer
                                                           // underrun property

    /**
     * <br>
     * Setting not send 'Range: bytes' field in http request.<br>
     * <h3>Description:</h3> In some condition,user must not send 'Range: bytes'
     * in http request. <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_SET_NOT_SUPPORT_BYTERANGE</p></td>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>int</p></td>
     * <td>1--not support 'Range:' 0--support 'Range:'</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>return value<br>
     * status code see system/core/include/utils/Errors.h</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> None
     */
    public final static int CMD_SET_NOT_SUPPORT_BYTERANGE = 317; // set
                                                                 // byte-range

    /**
     * <br>
     * Set the referer info in http request.<br>
     * <h3>Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_SET_REFERER</p></td>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>String</p></td>
     * <td>referer info</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>return value<br>
     * status code see system/core/include/utils/Errors.h</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> None
     */
    public final static int CMD_SET_REFERER = 318; // set referer

    /**
     * <br>
     * Set the useragent info in http request.<br>
     * <h3>Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_SET_USER_AGENT</p></td>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>String</p></td>
     * <td>useragent info</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>return value<br>
     * status code see system/core/include/utils/Errors.h</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> None
     */
    public final static int CMD_SET_USER_AGENT = 319; // set user agent

    /**
     * <br>
     * Set dynamic range compression cut scale factor for dolby.<br>
     * <h3>Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_SET_DOLBY_RANGEINFO</p></td>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>int</p></td>
     * <td>range info(0--100)</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>return value<br>
     * status code see system/core/include/utils/Errors.h</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> None
     */
    public final static int CMD_SET_DOLBY_RANGEINFO = 320;

    /**
     * <br>
     * Get dolby info.<br>
     * <h3>Description:</h3> None <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_GET_DOLBYINFO</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>return value<br>
     * status code see system/core/include/utils/Errors.h</p></td>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>int</p></td>
     * <td>AC Mode</p></td>
     * </tr>
     * <tr>
     * <td>2</p></td>
     * <td>int</p></td>
     * <td>{@see HiMediaPlayerDefine.ENUM_DOLBYINFO_ARG}</p></td>
     * </tr>
     * <tr>
     * <td>3</p></td>
     * <td>int</p></td>
     * <td>Audio decoder ID</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> None
     */
    public final static int CMD_GET_DOLBYINFO = 321;

    /**
     * <br>
     * Set avsync start region.<br>
     * <h3>Description:</h3> in dolby authentication,we must set avsync start
     * region <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_SET_AVSYNC_START_REGION</p></td>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>int</p></td>
     * <td>Plus time range during video synchronization</p></td>
     * </tr>
     * <tr>
     * <td>2</p></td>
     * <td>int</p></td>
     * <td>Negative time range during video synchronization</p></td>
     * </tr>
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>return value<br>
     * status code see system/core/include/utils/Errors.h</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> None
     */
    public final static int CMD_SET_AVSYNC_START_REGION = 322;

    public final static int CMD_SET_DAC_DECT_ENABLE = 323;

    /**
     * <br>
     * Set video framerate<br>
     * <h3>Description:</h3> You could setup video's framerate by this invoke<br>
     * <h3>Request:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>Value of CMD_SET_VIDEO_FPS</p></td>
     * </tr>
     * <tr>
     * <td>1</p></td>
     * <td>int</p></td>
     * <td>The FPS value you want to assign in integer part.</p></td>
     * </tr>
     * <tr>
     * <td>2</p></td>
     * <td>int</p></td>
     * <td>The FPS value you want to assign in decimal part.</p></td>
     * </tr>
     * 
     * </table>
     * <h3>Reply:</h3>
     * <table border="1" cellspacing="0" cellpadding="0">
     * <tr>
     * <th>Parcel Index</th>
     * <th>Type</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>0</p></td>
     * <td>int</p></td>
     * <td>return value<br>
     * status code see system/core/include/utils/Errors.h</p></td>
     * </tr>
     * </table>
     * <br>
     * <h3>Attention:</h3> None
     */
    public final static int CMD_SET_VIDEO_FPS = 324; // user request to set
                                                     // video framerate

    public final static int CMD_TYPE_BUTT = 325; // unsurport ID
}
