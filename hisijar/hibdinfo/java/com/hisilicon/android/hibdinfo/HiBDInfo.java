package com.hisilicon.android.hibdinfo;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;

/**
 * HiBDInfo: get bluray info, independent from HiBDPlayer Usage: openBluray->do
 * something->closeBluray
 */
public class HiBDInfo
{
    public static class BDCommand
    {
        public static int BD_CMD_OPEN_BLURAY;

        public static int BD_CMD_CLOSE_BLURAY;

        public static int BD_CMD_CHECK_DISC_INFO;

        public static int BD_CMD_GET_TITLE_NUMBER;

        public static int BD_CMD_GET_CHAPTER_NUMBER;

        public static int BD_CMD_GET_PLAYLIST;

        public static int BD_CMD_GET_DEFAULT_PLAYLIST;

        public static int BD_CMD_GET_CHAPTER_POSITION;

        public static int BD_CMD_GET_DEFAULT_TITLE;

        public static int BD_CMD_GET_TITLE;

        public static int BD_CMD_GET_SUBTITLE_LANGUAGE;

        public static int BD_CMD_GET_AUDIO_TRACK_LANGUAGE;

        public static int BD_CMD_GET_CUR_CHAPTER;

        public static int BD_CMD_GET_DURATION;

        public static int BD_CMD_CHECK_3D;
    }

    static
    {
        System.loadLibrary("bdinfo_jni");
    }

    private native final int native_invoke(Parcel pRequest, Parcel pReply);

    /**
     * do invoke command
     * @param pRequest request
     * @param pReply reply
     * @return 0 - success other - failure
     */
    public synchronized int invoke(Parcel pRequest, Parcel pReply)
    {
        pRequest.setDataPosition(0);

        int _Ret = native_invoke(pRequest, pReply);
        pReply.setDataPosition(0);
        return _Ret;
    }

    /**
     * open bluray
     * @param pPath bluray path
     * @return 0 - success other - failure
     */
    public int openBluray(String pPath)
    {
        return excuteCommand(BDCommand.BD_CMD_OPEN_BLURAY, pPath);
    }

    /**
     * close bluray
     * @return 0 - success other - failure
     */
    public int closeBluray()
    {
        return excuteCommand(BDCommand.BD_CMD_CLOSE_BLURAY);
    }

    /**
     * check disc info
     * @return 0 - is bluray other - is not bluray
     */
    public int checkDiscInfo()
    {
        return excuteCommand(BDCommand.BD_CMD_CHECK_DISC_INFO, true);
    }

    /**
     * get title number
     * @return title number
     */
    public int getTitleNumber()
    {
        return excuteCommand(BDCommand.BD_CMD_GET_TITLE_NUMBER, true);
    }

    /**
     * get chapter number by title id
     * @param pTitleId title id
     * @return chapter number
     */
    public int getChapterNumberByTitleId(int pTitleId)
    {
        return excuteCommand(BDCommand.BD_CMD_GET_CHAPTER_NUMBER, pTitleId, true);
    }

    /**
     * get playlist by title id
     * @param pTitleId title id
     * @return playlist
     */
    public int getPlaylistByTitleId(int pTitleId)
    {
        return excuteCommand(BDCommand.BD_CMD_GET_PLAYLIST, pTitleId, true);
    }

    /**
     * get default playlist
     * @return playlist
     */
    public int getDefaultPlaylist()
    {
        return excuteCommand(BDCommand.BD_CMD_GET_DEFAULT_PLAYLIST, true);
    }

    public int checkBluray3D()
    {
        return excuteCommand(BDCommand.BD_CMD_CHECK_3D, true);
    }
    /**
     * get default title id
     * @return title id
     */
    public int getDefaultTitleId()
    {
        return excuteCommand(BDCommand.BD_CMD_GET_DEFAULT_TITLE, true);
    }

    /**
     * get title id by playlist
     * @param pPlaylist playlist
     * @return title id
     */
    public int getTitleIdByPlaylist(int pPlaylist)
    {
        return excuteCommand(BDCommand.BD_CMD_GET_TITLE, pPlaylist, true);
    }

    /**
     * get chapter position
     * @param pTitleId title id
     * @param pChapterId chapter id
     * @return chapter position
     */
    public int getChapterPosition(int pTitleId, int pChapterId)
    {
        return excuteCommand(BDCommand.BD_CMD_GET_CHAPTER_POSITION, pTitleId, pChapterId, true);
    }

    /**
     * get current chapter id by title id and position
     * @param pTitleId title id
     * @param pPosition chapter position
     * @return chapter id
     */
    public int getCurChapterId(int pTitleId, int pPosition)
    {
        return excuteCommand(BDCommand.BD_CMD_GET_CUR_CHAPTER, pTitleId, pPosition, true);
    }

    /**
     * get duration by title id
     * @param pTitleId title id
     * @return duration
     */
    public int getDurationByTitleId(int pTitleId)
    {
        return excuteCommand(BDCommand.BD_CMD_GET_DURATION, pTitleId, true);
    }

    /**
     * get subtitle language list
     * @deprecated
     * @return language list
     */
    public List<String> getSubtitleLanguageList()
    {
        Parcel _Request = Parcel.obtain();
        Parcel _Reply = Parcel.obtain();
        List<String> _LanguageList = new ArrayList<String>();

        _Request.writeInt(BDCommand.BD_CMD_GET_SUBTITLE_LANGUAGE);

        if (invoke(_Request, _Reply) != 0)
        {
            _Request.recycle();
            _Reply.recycle();
            return null;
        }

        // for get
        _Reply.readInt();
        int _SubtitleNum = _Reply.readInt();
        String _Language = "";

        for (int i = 0; i < _SubtitleNum; i++)
        {
            _Language = _Reply.readString();
            if (_Language.equals(""))
            {
                _Language = "und";
            }
            _LanguageList.add(_Language);
        }

        _Request.recycle();
        _Reply.recycle();

        return _LanguageList;
    }

    /**
     * get audio language list
     * @deprecated
     * @return language list
     */
    public List<String> getAudioTrackLanguageList()
    {
        Parcel _Request = Parcel.obtain();
        Parcel _Reply = Parcel.obtain();
        List<String> _LanguageList = new ArrayList<String>();

        _Request.writeInt(BDCommand.BD_CMD_GET_AUDIO_TRACK_LANGUAGE);

        if (invoke(_Request, _Reply) != 0)
        {
            _Request.recycle();
            _Reply.recycle();
            return null;
        }

        // for get
        _Reply.readInt();
        int _AudioTrackNum = _Reply.readInt();
        String _Language = "";

        for (int i = 0; i < _AudioTrackNum; i++)
        {
            _Language = _Reply.readString();
            if (_Language.equals(""))
            {
                _Language = "und";
            }
            _LanguageList.add(_Language);
        }

        _Request.recycle();
        _Reply.recycle();

        return _LanguageList;
    }

    private int excuteCommand(int pCmdId, boolean pIsGet)
    {
        Parcel _Request = Parcel.obtain();
        Parcel _Reply = Parcel.obtain();
        _Request.setDataPosition(0);
        _Request.writeInt(pCmdId);

        if (invoke(_Request, _Reply) != 0)
        {
            _Request.recycle();
            _Reply.recycle();
            return -1;
        }

        if (pIsGet)
        {
            _Reply.readInt();
        }

        int _Result = _Reply.readInt();

        _Request.recycle();
        _Reply.recycle();

        return _Result;
    }

    private int excuteCommand(int pCmdId, int pArg1, boolean pIsGet)
    {
        Parcel _Request = Parcel.obtain();
        Parcel _Reply = Parcel.obtain();
        _Request.setDataPosition(0);
        _Request.writeInt(pCmdId);
        _Request.writeInt(pArg1);

        if (invoke(_Request, _Reply) != 0)
        {
            _Request.recycle();
            _Reply.recycle();
            return -1;
        }

        if (pIsGet)
        {
            _Reply.readInt();
        }

        int _Result = _Reply.readInt();

        _Request.recycle();
        _Reply.recycle();

        return _Result;
    }

    private int excuteCommand(int pCmdId, String pArg1, boolean pIsGet)
    {
        Parcel _Request = Parcel.obtain();
        Parcel _Reply = Parcel.obtain();
        _Request.setDataPosition(0);
        _Request.writeInt(pCmdId);
        _Request.writeString(pArg1);

        if (invoke(_Request, _Reply) != 0)
        {
            _Request.recycle();
            _Reply.recycle();
            return -1;
        }

        if (pIsGet)
        {
            _Reply.readInt();
        }

        int _Result = _Reply.readInt();

        _Request.recycle();
        _Reply.recycle();

        return _Result;
    }

    private int excuteCommand(int pCmdId, int pArg1, int pArg2, boolean pIsGet)
    {
        Parcel _Request = Parcel.obtain();
        Parcel _Reply = Parcel.obtain();
        _Request.setDataPosition(0);
        _Request.writeInt(pCmdId);
        _Request.writeInt(pArg1);
        _Request.writeInt(pArg2);

        if (invoke(_Request, _Reply) != 0)
        {
            _Request.recycle();
            _Reply.recycle();
            return -1;
        }

        if (pIsGet)
        {
            _Reply.readInt();
        }

        int _Result = _Reply.readInt();

        _Request.recycle();
        _Reply.recycle();

        return _Result;
    }

    private int excuteCommand(int pCmdId)
    {
        return excuteCommand(pCmdId, false);
    }

    private int excuteCommand(int pCmdId, int pArg1)
    {
        return excuteCommand(pCmdId, pArg1, false);
    }

    private int excuteCommand(int pCmdId, String pArg1)
    {
        return excuteCommand(pCmdId, pArg1, false);
    }

    private int excuteCommand(int pCmdId, int pArg1, int pArg2)
    {
        return excuteCommand(pCmdId, pArg1, pArg2, false);
    }
}
