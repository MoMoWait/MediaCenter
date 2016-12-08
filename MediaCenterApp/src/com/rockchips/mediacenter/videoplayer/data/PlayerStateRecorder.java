/**
 * 
 * com.rockchips.iptv.stb.dlna.data
 * PlayerStateRecorder.java
 * 
 * 2011-11-9-下午08:23:54
 * Copyright 2011 Huawei Technologies Co., Ltd
 * 
 */

/**
 * 
 * com.rockchips.iptv.stb.dlna.data
 * HistoryListRecord.java
 * 
 * 2011-10-27-下午02:08:43
 * Copyright 2011 Huawei Technologies Co., Ltd
 * 
 */
package com.rockchips.mediacenter.videoplayer.data;

import java.util.HashMap;
import java.util.Map;

import com.rockchips.mediacenter.basicutils.constant.Constant;

/**
 * 
 * 
 * PlayerStateRecorder
 * 
 * 2011-11-9 下午08:24:14
 * 
 * @version 1.0.0
 *
 */
public class PlayerStateRecorder
{
    
    public static final int VIDEO_PLAY_MODE = Constant.MediaPlayMode.MP_MODE_SINGLE;
    
    private Map<Integer, Integer> mPlayerStateRecoderList = new HashMap<Integer, Integer>();
    
    private static final int MAXLEN_OF_MAP = 1000;
    
    private static PlayerStateRecorder msMe = null;
    
    private PlayerStateRecorder()
    {
        
    }
    
    public static PlayerStateRecorder getInstance()
    {
        if (msMe == null)
        {
            msMe = new PlayerStateRecorder();
        }
        
        return msMe;
    }
    
    public void put(int key, int seek)
    {
        if (mPlayerStateRecoderList.size() > MAXLEN_OF_MAP)
        {
            mPlayerStateRecoderList.clear();
        }
        
        mPlayerStateRecoderList.put(key, seek);
    }
    
    public int get(int key)
    {
        Integer iRet = mPlayerStateRecoderList.get(key);
        
        if (iRet == null)
        {
            
            iRet = 0;
            put(key, iRet);
        }
        
        return iRet.intValue();
    }
}
