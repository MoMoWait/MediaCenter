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

import android.util.Log;

/**
 * 
 * HistoryListRecord
 * 
 * 2011-10-27 下午02:08:43
 * 
 * @version 1.0.0
 * 
 */
public class HistoryListRecord
{
    /**
     * 保留播放过的url对应的seek位置
     */
    private Map<String, Integer> mHistoryList = new HashMap<String, Integer>();

    /**
     * 字幕
     */
    private Map<String, SubObject> mHistorySub = new HashMap<String, HistoryListRecord.SubObject>();

    private static final int MAXLEN_OF_MAP = 1000;

    private static HistoryListRecord msMe = null;

    private HistoryListRecord()
    {

    }

    public static HistoryListRecord getInstance()
    {
        if (msMe == null)
        {
            msMe = new HistoryListRecord();
        }

        return msMe;
    }

    public SubObject getSubObject()
    {
        return new SubObject();
    }

    public class SubObject
    {
        int subId;

        int soundId;

        int soundNum;

        int subNum;

        public SubObject(int subId, int soundId, int soundNum, int subNum)
        {
            super();
            this.subId = subId;
            this.soundId = soundId;
            this.soundNum = soundNum;
            this.subNum = subNum;
        }

        public SubObject()
        {
            super();
            // TODO Auto-generated constructor stub
        }

        public int getSubId()
        {
            return subId;
        }

        public void setSubId(int subId)
        {
            this.subId = subId;
        }

        public int getSoundId()
        {
            return soundId;
        }

        public void setSoundId(int soundId)
        {
            this.soundId = soundId;
        }

        public int getSoundNum()
        {
            return soundNum;
        }

        public void setSoundNum(int soundNum)
        {
            this.soundNum = soundNum;
        }

        public int getSubNum()
        {
            return subNum;
        }

        public void setSubNum(int subNum)
        {
            this.subNum = subNum;
        }
    }

    public void put(String key, int seek)
    {
        Log.e("onkey", "history save " + seek);
        if (mHistoryList.size() > MAXLEN_OF_MAP)
        {
            mHistoryList.clear();
        }

        mHistoryList.put(key, seek);
    }

    public int get(String key)
    {
        Integer iRet = mHistoryList.get(key);

        if (iRet == null)
        {

            iRet = 0;
            put(key, iRet);
        }

        return iRet.intValue();
    }

    public void putSubInfo(String key, SubObject seek)
    {
        if (mHistorySub.size() > MAXLEN_OF_MAP)
        {
            mHistorySub.clear();
        }

        mHistorySub.put(key, seek);
    }

    public SubObject getSubInfo(String key)
    {
        SubObject iRet = mHistorySub.get(key);

        return iRet;
    }
}
