package com.rockchips.mediacenter.common;

import java.util.ArrayList;
import java.util.List;

import com.rockchips.android.airsharing.api.HwServer;
import com.rockchips.android.airsharing.api.dlna.dmc.bean.DlnaBaseObjectInfo;
import com.rockchips.android.airsharing.api.dlna.dmc.common.EDlnaMediaType;
import com.rockchips.mediacenter.basicutils.bean.LocalDeviceInfo;
import com.rockchips.mediacenter.basicutils.bean.LocalMediaInfo;
import com.rockchips.mediacenter.basicutils.constant.Constant;
import com.rockchips.mediacenter.basicutils.constant.Constant.MediaType;

/**
 * 
 * Description: dlna数据和本地数据之间的转换方法<br>
 * @author s00211113
 * @version v1.0
 * Date: 2014-7-29 下午7:43:39<br>
 */
public final class MediaInfoConvertor
{
    public MediaInfoConvertor()
    {
    }

    public static LocalDeviceInfo HwServer2LocalDeviceInfo(HwServer hwServer)
    {
        if (hwServer == null)
        {
            return null;
        }
        LocalDeviceInfo localDeviceInfo = new LocalDeviceInfo();
        localDeviceInfo.setDeviceType(Constant.DeviceType.DEVICE_TYPE_DMS);
        localDeviceInfo.setMountPath(hwServer.getIpAddr());
        localDeviceInfo.setPhysicId(hwServer.getName());
        localDeviceInfo.setmDeviceId(hwServer.getPort());

        return localDeviceInfo;
    }

    public static HwServer LocalDeviceInfo2HwServer(LocalDeviceInfo localDeviceInfo)
    {
        if (localDeviceInfo == null || localDeviceInfo.getDeviceType() != Constant.DeviceType.DEVICE_TYPE_DMS)
        {
            return null;
        }

        return new HwServer(localDeviceInfo.getPhysicId(), null, localDeviceInfo.getmDeviceId(), localDeviceInfo.getMountPath());
    }

    public static List<LocalDeviceInfo> HwServerList2LocalDeviceInfoList(List<HwServer> hwServerList)
    {
        if (hwServerList == null || hwServerList.size() == 0)
        {
            return null;
        }

        List<LocalDeviceInfo> list = new ArrayList<LocalDeviceInfo>();

        for (HwServer server : hwServerList)
        {
            list.add(HwServer2LocalDeviceInfo(server));
        }

        return list;
    }

    public static List<HwServer> LocalDeviceInfoList2HwServerList(List<LocalDeviceInfo> localDeviceInfoList)
    {
        if (localDeviceInfoList == null || localDeviceInfoList.size() == 0)
        {
            return null;
        }

        List<HwServer> list = new ArrayList<HwServer>();

        for (LocalDeviceInfo localDev : localDeviceInfoList)
        {
            list.add(LocalDeviceInfo2HwServer(localDev));
        }

        return list;
    }
    
    public static LocalMediaInfo DlnaBaseObjectInfo2LocalMediaInfo(DlnaBaseObjectInfo dlnaInfo)
    {
        if (dlnaInfo == null)
        {
            return null;
        }
        LocalMediaInfo localMediaInfo = new LocalMediaInfo();
        localMediaInfo.setmDeviceType(Constant.DeviceType.DEVICE_TYPE_DMS);
        localMediaInfo.setmFileName(dlnaInfo.getsTitle());
        localMediaInfo.setmFiles(dlnaInfo.getiChildCount());
        localMediaInfo.setmFileSize(dlnaInfo.getiSize());
        localMediaInfo.setmFileType(DlnaType2LocalType(dlnaInfo.getiMediaType()));
        localMediaInfo.setmModifyDateStr(dlnaInfo.getsDateCreated());
        localMediaInfo.setmResUri(dlnaInfo.getsResUri());
        localMediaInfo.setmObjectId(dlnaInfo.getsObjectId());
        localMediaInfo.setmThumbNail(dlnaInfo.getsDisplayURI());
        localMediaInfo.setmTitle(dlnaInfo.getsTitle());

        localMediaInfo.setmAlbum(dlnaInfo.getsAlbum());
        localMediaInfo.setmArtist(dlnaInfo.getsArtist());
        localMediaInfo.setmResoulution(dlnaInfo.getsResoulution());
        localMediaInfo.setmMimeType(dlnaInfo.getStProtocolInfo());
        localMediaInfo.setmDuration(dlnaInfo.getsDuration());
        
        return localMediaInfo;
    }
    
    public static List<LocalMediaInfo> DlnaBaseObjectInfoList2LocalMediaInfoList(List<DlnaBaseObjectInfo> dlnaBaseObjectInfoList)
    {
        if (dlnaBaseObjectInfoList == null || dlnaBaseObjectInfoList.size() == 0)
        {
            return null;
        }

        List<LocalMediaInfo> list = new ArrayList<LocalMediaInfo>();

        for (DlnaBaseObjectInfo info : dlnaBaseObjectInfoList)
        {
            list.add(DlnaBaseObjectInfo2LocalMediaInfo(info));
        }

        return list;
    }

    public static int DlnaType2LocalType(int dlnaType)
    {
        if (dlnaType == EDlnaMediaType.DLNA_MEDIA_TYPE_AUDIO.getValue())
        {
            return MediaType.AUDIO;
        }
        else if (dlnaType == EDlnaMediaType.DLNA_MEDIA_TYPE_VIDEO.getValue())
        {
            return MediaType.VIDEO;
        }
        else if (dlnaType == EDlnaMediaType.DLNA_MEDIA_TYPE_IMAGE.getValue())
        {
            return MediaType.IMAGE;
        }
        else if (dlnaType == EDlnaMediaType.DLNA_MEDIA_TYPE_CONTAINER.getValue())
        {
            return MediaType.FOLDER;
        }
        else
        {
            return MediaType.UNKNOWN_TYPE;
        }
    }

    public static EDlnaMediaType LocalType2DlnaType(int localType)
    {
        switch (localType)
        {
            case MediaType.AUDIO:
                return EDlnaMediaType.DLNA_MEDIA_TYPE_AUDIO;
            case MediaType.VIDEO:
                return EDlnaMediaType.DLNA_MEDIA_TYPE_VIDEO;
            case MediaType.IMAGE:
                return EDlnaMediaType.DLNA_MEDIA_TYPE_IMAGE;
            case MediaType.FOLDER:
                return EDlnaMediaType.DLNA_MEDIA_TYPE_ALL;
            default:
                return EDlnaMediaType.DLNA_MEDIA_TYPE_INVALID;
        }
    }

}
