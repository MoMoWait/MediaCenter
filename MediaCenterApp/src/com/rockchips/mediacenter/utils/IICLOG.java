/**
 * Title: IIC_LOG.java<br>
 * Package: com.rockchips.shine.util<br>
 * Description: 自定义日志打印，根据类型开关判断是否打印日志<br>
 * @author h00184428
 * @version v1.0<br>
 * Date: 2012-2-29上午09:48:19<br>
 * Copyright © Huawei Technologies Co., Ltd. 2012. All rights reserved.
 */

package com.rockchips.mediacenter.utils;

import android.util.Log;

/**
 * Description: 自定义日志打印类<br>
 * @author h00184428
 * @version v1.0 Date: 2012-2-29 上午09:48:19
 * @version v1.1 Date: 2013-10-5 下午17:10:19<br>
 */

public class IICLOG
{
    private boolean mBVerboseSwitch = true;

    private boolean mBDebugSwithch = true;

    private boolean mBInfoSwitch = true;

    private boolean mBWarningSwitch = true;

    private boolean mBErrorSwitch = true;

    private boolean mBDetailSwitch = true;

    private static final int STACK_DEPTH = 4;

    private static IICLOG mStInstance;

    /**
     * 
     * 构造函数
     * @param bVerbose 冗余
     * @param bDebug 调试
     * @param bInfo 信息
     * @param bWarning 警告
     * @param bError 错误
     */
    public IICLOG(boolean bVerbose, boolean bDebug, boolean bInfo, boolean bWarning, boolean bError, boolean bDetail)
    {
        this.mBVerboseSwitch = bVerbose;
        this.mBDebugSwithch = bDebug;
        this.mBInfoSwitch = bInfo;
        this.mBWarningSwitch = bWarning;
        this.mBErrorSwitch = bError;
        this.mBDetailSwitch = bDetail;
    }

    /**
     * TODO 构造函数，固定配置文件路径
     */
    public IICLOG()
    {
    }

    /**
     * TODO 获取静态示例方法
     * @return
     * @return IICLOG
     * @throws
     */

    public static IICLOG getInstance()
    {
        if (null == mStInstance)
        {
            mStInstance = new IICLOG();
        }

        return mStInstance;
    }

    /**
     * Description: TODO<br>
     * @author h00184428
     * @version v1.0 Date: 2012-5-21 下午02:29:29<br>
     */
    public class FileErrorException extends Exception
    {
        /**
         * serialVersionUID:TODO
         */

        private static final long serialVersionUID = 1L;

        public FileErrorException()
        {
        }

        public FileErrorException(String message)
        {
            super(message);
        }
    }

    private String buildMsg(String msg)
    {
        StringBuilder buffer = new StringBuilder();

        if (mBDetailSwitch)
        {
            final StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[STACK_DEPTH];

            buffer.append("[ ");
            buffer.append(stackTraceElement.getFileName());
            buffer.append(": ");
            buffer.append(stackTraceElement.getLineNumber());
            buffer.append("]");
        }

        buffer.append(msg);

        return buffer.toString();
    }

    // ****************根据开关判断是否打印日志***************************
    public void v(String strTag, String strLog)
    {
        if (mBVerboseSwitch)
        {
            Log.v(strTag, buildMsg(strLog));
        }
    }

    public void d(String strTag, String strLog)
    {
        if (mBDebugSwithch)
        {
            Log.d(strTag, buildMsg(strLog));
        }
    }

    public void i(String strTag, String strLog)
    {
        if (mBInfoSwitch)
        {
            Log.i(strTag, buildMsg(strLog));
        }
    }

    public void w(String strTag, String strLog)
    {
        if (mBWarningSwitch)
        {
            Log.w(strTag, buildMsg(strLog));
        }
    }

    public void e(String strTag, String strLog)
    {
        if (mBErrorSwitch)
        {
            Log.e(strTag, buildMsg(strLog));
        }
    }

}
