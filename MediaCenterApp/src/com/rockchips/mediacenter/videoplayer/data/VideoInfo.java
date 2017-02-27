/**
 * Title: VideoInfo.java<br>
 * Package: com.rockchips.mediacenter.videoplayer.data<br>
 * Description: TODO<br>
 * @author c00224451
 * @version v1.0<br>
 * Date: 2014-7-22下午7:19:43<br> 
 * Copyright © Huawei Technologies Co., Ltd. 2014. All rights reserved.
 */

package com.rockchips.mediacenter.videoplayer.data;

import android.os.Bundle;

import com.rockchips.mediacenter.bean.LocalMediaInfo;

/**
 * Description: TODO<br>
 * @author c00224451
 * @version v1.0
 * Date: 2014-7-22 下午7:19:43<br>
 */

public class VideoInfo extends LocalMediaInfo 
{             
    private float mSeekTo;
    private static final String SEEK_TO = "SEEK_TO";
    
    private String mResolution;    
    public static final String RESOLUTION = "resolution";
    
    public float getmSeekTo()
    {
        return mSeekTo;
    }
    public void setmSeekTo(float mSeekTo)
    {
        this.mSeekTo = mSeekTo;
    }

    public String getmResolution() 
    {
        return mResolution;
    }
    public void setmResolution(String mResolution) 
    {
        this.mResolution = mResolution;
    }

    public Bundle compress()
    {
        Bundle bundle = super.compress();
        bundle.putFloat(SEEK_TO, mSeekTo); 
        bundle.putString(RESOLUTION, mResolution);        
        return bundle;
    }
    
    public void decompress(Bundle bundle)
    {
        if (null == bundle)
        {
            return;
        }
        super.decompress(bundle);  
        mSeekTo = bundle.getFloat(SEEK_TO); 
        mResolution = bundle.getString(RESOLUTION);  
    }
}
