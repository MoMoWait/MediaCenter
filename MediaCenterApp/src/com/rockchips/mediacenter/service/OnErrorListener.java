package com.rockchips.mediacenter.service;

import com.rockchips.mediacenter.service.IMediaPlayerAdapter;



public interface OnErrorListener
{
    
    /**
     * 适配onError
     * 
     * @param mp
     * @param what
     * @param extra
     */
    public boolean onError(IMediaPlayerAdapter mp, int what, int extra);
    
}