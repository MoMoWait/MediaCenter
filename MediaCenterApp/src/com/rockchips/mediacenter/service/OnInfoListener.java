package com.rockchips.mediacenter.service;

import com.rockchips.mediacenter.service.IMediaPlayerAdapter;


/**
 * @author t00181037
 * @version 1.0
 * @created 18-十一月-2013 13:45:03
 */
public interface OnInfoListener {

    /**
     * 适配onInfo
     * 
     * @param mp
     * @param what
     * @param extra
     */
    public boolean onInfo(IMediaPlayerAdapter mp, int what, int extra);

}