package com.rockchips.mediacenter.portable.listener;

import com.rockchips.mediacenter.portable.IMediaPlayerAdapter;



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