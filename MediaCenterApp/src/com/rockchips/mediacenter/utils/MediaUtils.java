package com.rockchips.mediacenter.utils;

/**
 * @author GaoFei
 * 媒体工具
 */
public class MediaUtils {
    static{
        System.loadLibrary("mediacenter-jni");
    }
    public static native boolean hasMediaClient();
    public static native int getCurrentPostion();
}
