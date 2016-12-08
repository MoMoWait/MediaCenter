package com.rockchips.mediacenter.videoplayer.bd;

import java.io.File;

import com.hisilicon.android.hibdinfo.HiBDInfo;

import android.util.Log;

public class BDInfo
{
    private static final String TAG = "MediaCenterApp";

    private HiBDInfo mHiBDInfo;

    private boolean DEBUG = false;

    public BDInfo()
    {
        mHiBDInfo = new HiBDInfo();
    }

    /**
     * Check the Blueray information. CN:检查蓝光信息
     * 
     * @param pPath CN:蓝光目录路径
     * @return Command execution results 0- Blu ray directory not 0- not blue
     *         directory CN:命令执行结果 0-是蓝光目录 非0-不是蓝光目录
     */
    public synchronized int checkDiscInfo(String pPath)
    {
        int _Result = 0;
        mHiBDInfo.openBluray(pPath);
        _Result = mHiBDInfo.checkDiscInfo();
        mHiBDInfo.closeBluray();

        return _Result;
    }

    /**
     * check is Blueray or not. CN:是否是蓝光文件
     * 
     * @param pPath CN:蓝光目录路径
     * @return true - 是 false - 否
     */
    public boolean isBDFile(String pPath)
    {
        if (DEBUG)
            Log.v(TAG, "path is " + pPath);

        if (!hasBDMVDir(pPath))
        {
            return false;
        }

        if (checkDiscInfo(pPath) < 0)
        {
            return false;
        }

        return true;
    }

    /**
     * Check the BDMV directory 是否包含BDMV目录
     * 
     * @param pPath CN:蓝光目录路径
     * @return true - 是 false - 否
     */
    public boolean hasBDMVDir(String pPath)
    {
        File _File = new File(pPath);

        if (!_File.exists())
        {
            return false;
        }

        File[] _Files = _File.listFiles();

        if (_Files == null)
        {
            return false;
        }

        for (int i = 0; i < _Files.length; i++)
        {
            if (_Files[i].getName().equalsIgnoreCase("BDMV"))
            {
                return true;
            }
        }

        return false;
    }
}
