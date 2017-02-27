package com.rockchips.mediacenter.utils;

import com.rockchips.mediacenter.R;
import com.rockchips.mediacenter.data.ConstData;

public class MediaTypeIcon 
{
    public static int getHeaderIcon(int type)
    {
        int resId;
        switch(type)
        {
            case ConstData.MediaType.AUDIO:
                resId = R.drawable.icon_audio_header;
                break;
            case ConstData.MediaType.FOLDER:
                resId = R.drawable.icon_folder_header;
                break;
            case ConstData.MediaType.IMAGE:
                resId = R.drawable.icon_image_header;
                break;
            case ConstData.MediaType.VIDEO:
                resId = R.drawable.icon_video_header;
                break;
            default:
                resId = R.drawable.icon_folder_header;
                break;
        }
        return resId;
    }
}
