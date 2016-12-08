package com.rockchips.mediacenter.portable.listener;

import com.rockchips.mediacenter.portable.IMediaPlayerAdapter;


/**
 * @author t00181037
 * @version 1.0
 * @created 18-十一月-2013 13:45:04
 */
public interface OnBufferingUpdateListener {

    /**
     * 适配<font color="#3f7f5f">onBufferingUpdate</font>
     * 
     * @param mp
     * @param percent
     */
    public boolean onBufferingUpdate(IMediaPlayerAdapter mp, int percent);

}