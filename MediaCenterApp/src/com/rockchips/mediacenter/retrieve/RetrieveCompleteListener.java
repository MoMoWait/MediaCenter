package com.rockchips.mediacenter.retrieve;

import android.graphics.Bitmap;

import com.rockchips.mediacenter.basicutils.bean.LocalMediaInfo;
import com.rockchips.mediacenter.audioplayer.SongInfo;

public interface RetrieveCompleteListener
{
    public void onComplete(LocalMediaInfo mediaInfo, SongInfo songInfo);

    public void onComplete(LocalMediaInfo mediaInfo, Bitmap bitmap);
}
