package com.rockchips.mediacenter.service;

import com.rockchips.mediacenter.service.IMediaPlayerAdapter;


/**
 * @author t00181037
 * @version 1.0
 * @created 18-十一月-2013 13:45:02
 */
public interface OnPreparedListener
{
    
    /**
     * 
     * @param mp
     */
    public void onPrepared(IMediaPlayerAdapter mp);
    
}