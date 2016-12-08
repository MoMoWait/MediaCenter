package com.rockchips.mediacenter.imageplayer;

import java.io.Serializable;

public class BackgroundMusic implements Serializable {
    /**
     * 注释内容
     */
    private static final long serialVersionUID = -6550270540212185977L;

    String mMusicName;

    String mMusicPath;

    public String getMusicName() {
        return mMusicName;
    }

    public String getMusicPath() {
        return mMusicPath;
    }

    public BackgroundMusic(String name, String path) {
        mMusicName = name;
        mMusicPath = path;
    }

    @Override
    public String toString() {
        return "BackgroundMusic [mMusicName=" + mMusicName + ", mMusicPath="
                + mMusicPath + "]";
    }

}
