package com.rockchips.mediacenter.videoplayer.widget;

import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.rockchips.mediacenter.videoplayer.data.VideoInfo;

public class PlayListShowLayoutBase extends RelativeLayout
{
    
    public PlayListShowLayoutBase(Context context)
    {
        super(context);
    }
    
    public PlayListShowLayoutBase(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }
    
    public PlayListShowLayoutBase(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }
    
    public void setOnItemClickListener(OnItemClickListener l)
    {
        
    }
    
    public void hidePopupWindow()
    {
    }
    
    public int getCurrentFocusIndex()
    {
        return 0;
    }
    
    public int getCurrentPlayIndex()
    {
        return 0;
    }
    
    public void setMediaList(List<VideoInfo> mediainfolist, int index)
    {
    }
    
    public void setPlayIndex(int index)
    {
    }
    
    public void setCurrentPlayIndex(int index)
    {
    }
    
    public int cvrtPosForAr(int position)
    {
        return position;
    }
    
    public PopupWindow getPlaylistPop()
    {
        return null;
    }
	/* BEGIN: Added by s00211113 for  DTS2014032605664   2014/3/27 */    
    public void showPrepare()
    {
        return;
    }
	/* END: Added by s00211113 for  DTS2014032605664   2014/3/27 */    
}
