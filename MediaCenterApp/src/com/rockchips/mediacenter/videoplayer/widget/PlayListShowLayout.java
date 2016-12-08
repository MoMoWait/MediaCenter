package com.rockchips.mediacenter.videoplayer.widget;

import java.util.List;

import momo.cn.edu.fjnu.androidutils.data.CommonValues;
import momo.cn.edu.fjnu.androidutils.utils.DeviceInfoUtils;
import momo.cn.edu.fjnu.androidutils.utils.SizeUtils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.rockchips.mediacenter.R;
import com.rockchips.mediacenter.videoplayer.PlayStateInfo;
import com.rockchips.mediacenter.videoplayer.data.VideoInfo;

/**
 * 
 *播放页面的playlist
 * 
 * @author  lWX165420
 * @version  [版本号, 2013-4-15]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class PlayListShowLayout extends PlayListShowLayoutBase
{
    private static final String TAG = "MediaCenterApp";
    
    private static final int LINEVISIABLENUM = 4;
    
    private static final int FOCUSCHANGEWIGHT = 303;
    
    private static final int FOCUSCHANGEHEIGHT = SizeUtils.dp2px(CommonValues.application, 124);
    
    private TextView mDeviceNameTextV;
    
    private TextView mDeviceNameHead;
    
    private ListView mPlayListGridView;
    
    private ImageView mFocusImgView;
    
    private PlayListAdapter mPlayListAdapter;
    
    // 播放列表
    private List<VideoInfo> mMediaList = null;
    
    private String currDevName = "";
    
    private int playIndex = 0;
    
    private Context mContext;
    
    private LayoutInflater inflater;
    
    private PopupWindow mPlaylistPop;
    
    private int currentposition = 0;
    
    //第一个显示的item的索引
    private int firstvisibleItem=0;
    
    private PlayStateInfo mPlayStateInfo;
    
    
    private Handler mHandler = new Handler()
    {
        public void handleMessage(android.os.Message msg)
        {
            if (msg.what == LINEVISIABLENUM)
            {
                hidePopupWindow();
            }
        };
    };
    
    public PlayListShowLayout(Context context, PlayStateInfo playStateInfo)
    {
        super(context);
        init(context, playStateInfo);
    }
    
    public PlayListShowLayout(Context context, AttributeSet attrs, PlayStateInfo playStateInfo)
    {
        super(context, attrs);
        init(context, playStateInfo);
    }
    
    public PlayListShowLayout(Context context, AttributeSet attrs, int defStyle, PlayStateInfo playStateInfo)
    {
        super(context, attrs, defStyle);
        init(context, playStateInfo);
    }
    
    private void init(Context context, PlayStateInfo playStateInfo)
    {
        mPlayStateInfo = playStateInfo;
        mContext = context;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.video_playlist_layout, this);
        mDeviceNameTextV = (TextView)findViewById(R.id.playlist_device_name);
        mDeviceNameHead = (TextView)findViewById(R.id.playlist_dev_name);
        mPlayListGridView = (ListView)findViewById(R.id.playlist_listview);
        
        mPlayListGridView.setOnItemSelectedListener(new OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                currentposition = position;
                focusChange();
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
            }
        });
        
        mPlayListGridView.setOnScrollListener(new OnScrollListener()
        {
            
            public void onScrollStateChanged(AbsListView view, int scrollState)
            {
            }
            
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
            {
                firstvisibleItem=firstVisibleItem;
                /* BEGIN: Modified by s00211113 for  DTS2014032605664   2014/3/27 */
                Log.d(TAG, "focusChange before: firstvisibleItem=" + firstvisibleItem + "visibleItemCount=" + visibleItemCount + "totalItemCount=" + totalItemCount);
				/* BEGIN: Added by s00211113 for  DTS2014031902437   2014/3/24 */
//                if ((playIndex != 0) && (mMediaList.size() > 5))
//                {
//                    firstvisibleItem ++;
//                }
                if (visibleItemCount == 7 || (visibleItemCount == 6 && firstVisibleItem + visibleItemCount == totalItemCount))
                {
                    firstvisibleItem++;
                }
				/* END: Added by s00211113 for  DTS2014031902437   2014/3/24 */
				/* END: Modified by s00211113 for  DTS2014032605664   2014/3/27 */
            }
        });
        mFocusImgView = (ImageView)findViewById(R.id.playlist_gridview_focus);
        
        if (null != mPlayStateInfo)
        {
            mMediaList = mPlayStateInfo.getMediaList();
            playIndex = mPlayStateInfo.getCurrentIndex();
            currDevName = mPlayStateInfo.getCurrDevName();
        }
        currentposition = playIndex;
        mPlayListAdapter = new PlayListAdapter();
        mPlayListGridView.setAdapter(mPlayListAdapter);
    }
    
    public void setMediaList(List<VideoInfo> mediainfolist, int index)
    {
        mMediaList = mediainfolist;
        playIndex = index;
        currentposition = index;
        mPlayListAdapter.notifyDataSetChanged();
    }
    
    public void setPlayIndex(int index)
    {
        playIndex = index;
        currentposition = index;
        mPlayListAdapter.notifyDataSetChanged();
    }
    
    public void setCurrentPlayIndex(int index)
    {
        playIndex = index;
        mPlayListAdapter.notifyDataSetChanged();
    }
    
    public void setOnItemClickListener(OnItemClickListener l)
    {
        mPlayListGridView.setOnItemClickListener(l);
    }
    
    public PopupWindow getPlaylistPop()
    {
        if (mPlaylistPop == null)
        {
            mPlaylistPop =
                new PopupWindow(PlayListShowLayout.this, SizeUtils.dp2px(mContext, 400) , android.view.ViewGroup.LayoutParams.MATCH_PARENT);
            //mPlaylistPop.setBackgroundDrawable(new BitmapDrawable());
        }
        
        // 添加播放设备信息
        String str = "";
        if (currDevName != null && currDevName.trim().length() != 0)
        {
            str = currDevName;
        }
        mDeviceNameHead.setText(str);
        
        str = mContext.getResources().getString(R.string.video_playlist_all_left)+(playIndex+1)+"/"+mMediaList.size();
        str = str.concat(mContext.getResources().getString(R.string.video_playlist_all_right));
        mDeviceNameTextV.setText(str);
        
        if (mHandler.hasMessages(LINEVISIABLENUM))
        {
            mHandler.removeMessages(LINEVISIABLENUM);
        }
        mHandler.sendEmptyMessageDelayed(LINEVISIABLENUM, 4000);
        mPlaylistPop.setFocusable(true);
        mPlayListGridView.setSelection(playIndex);
        return mPlaylistPop;
    }
    
    public boolean isShowing()
    {
        
        if (mPlaylistPop != null && mPlaylistPop.isShowing())
        {
            return true;
        }
        return false;
    }
    
    public void hidePopupWindow()
    {
        if (mHandler.hasMessages(LINEVISIABLENUM))
        {
            mHandler.removeMessages(LINEVISIABLENUM);
        }
        if (mPlaylistPop != null && mPlaylistPop.isShowing())
        {
            mPlaylistPop.dismiss();
        }
    }
    
	/* BEGIN: Added by s00211113 for  DTS2014032605664   2014/3/27 */
    public void showPrepare()
    {
        currentposition = firstvisibleItem;
        if (mMediaList.size() - playIndex < 5)
        {
            mFocusImgView.setTranslationY(((mMediaList.size() < 5)?playIndex:5 + playIndex - mMediaList.size()) * FOCUSCHANGEHEIGHT);
        }
        else
        {
            mFocusImgView.setTranslationY(0);
        }
    }
	/* END: Added by s00211113 for  DTS2014032605664   2014/3/27 */
    
    public int getCurrentFocusIndex()
    {
        return mPlayListGridView.getSelectedItemPosition();
    }
    
    public int getCurrentPlayIndex()
    {
        return playIndex;
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        
        Log.d(TAG, "keyCode:" + keyCode);
        switch (keyCode)
        {
            case KeyEvent.KEYCODE_BACK:
                mPlaylistPop.dismiss();
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                
                if (currentposition % LINEVISIABLENUM > 0)
                {
                    currentposition--;
                    mPlayListGridView.setSelection(currentposition);
                }
                
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if ((currentposition + 1) % LINEVISIABLENUM != 0)
                {
                    currentposition++;
                    mPlayListGridView.setSelection(currentposition);
                }
                return true;
            case KeyEvent.KEYCODE_DPAD_UP:
                
            case KeyEvent.KEYCODE_DPAD_DOWN:
                
            case KeyEvent.KEYCODE_DPAD_CENTER:
            default:
                break;
        }
        return false;
    }
    
    private void focusChange()
    {
        if (mHandler.hasMessages(LINEVISIABLENUM))
        {
            mHandler.removeMessages(LINEVISIABLENUM);
        }
        mHandler.sendEmptyMessageDelayed(LINEVISIABLENUM, 4000);
        /* BEGIN: Modified by s00211113 for  DTS2014031902437   2014/3/21 */
//        ObjectAnimator animator =
//            ObjectAnimator.ofFloat(mFocusImgView, "translationX", (currentposition % LINEVISIABLENUM)
//                * FOCUSCHANGEWIGHT);
//        animator.setDuration(250);
//        int i = currentposition - firstvisibleItem;
//        i = i / LINEVISIABLENUM;
//        
//        ObjectAnimator transxanimator = ObjectAnimator.ofFloat(mFocusImgView, "translationY", i * FOCUSCHANGEHEIGHT);
//        transxanimator.setDuration(250);
        Log.d(TAG, "focusChange: currentposition=" + currentposition + "firstvisibleItem=" + firstvisibleItem);
		/* BEGIN: Added by s00211113 for  DTS2014031902437   2014/3/24 */
		/* BEGIN: Added by s00211113 for  DTS2014032605587   2014/3/27 */
        if (currentposition - firstvisibleItem >= 5)
        {
            return;
        }
		/* END: Added by s00211113 for  DTS2014032605587   2014/3/27 */
        if (currentposition < firstvisibleItem)
        {
            firstvisibleItem = currentposition;
        }
		/* END: Added by s00211113 for  DTS2014031902437   2014/3/24 */
        ObjectAnimator transYanimator = ObjectAnimator.ofFloat(mFocusImgView, "translationY", (currentposition - firstvisibleItem) * FOCUSCHANGEHEIGHT);
        AnimatorSet animatorset = new AnimatorSet();
        animatorset.setDuration(250);
//        animatorset.playTogether(animator, transxanimator);
        animatorset.play(transYanimator);
        animatorset.start();
		/* END: Modified by s00211113 for  DTS2014031902437   2014/3/21 */
    }
    
    //用于playlistItem
    class PlayListAdapter extends BaseAdapter
    {
        
        @Override
        public int getCount()
        {
            return mMediaList != null ? mMediaList.size() : 0;
        }
        
        @Override
        public Object getItem(int position)
        {
            return mMediaList != null ? mMediaList.get(playIndex) : null;
        }
        
        @Override
        public long getItemId(int position)
        {
            return position;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            
            ViewHolder viewholder;
            
            if (convertView == null)
            {
                viewholder = new ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.video_playlist_item_layout, null);
                viewholder.name = (TextView)convertView.findViewById(R.id.playlist_item_videoname);
                viewholder.icon = (ImageView)convertView.findViewById(R.id.playlist_item_palyicon);
                convertView.setTag(viewholder);
            }
            else
            {
                viewholder = (ViewHolder)convertView.getTag();
            }
            
            viewholder.name.setText(mMediaList.get(position).getmFileName());
            
            if (position == playIndex)
            {
                viewholder.icon.setVisibility(View.VISIBLE);
            }
            else
            {
                viewholder.icon.setVisibility(View.GONE);
            }
            
            return convertView;
        }
    }
    
    public int cvrtPosForAr(int position)
    {
        return position;
    }
    
    private class ViewHolder
    {
        TextView name;
        
        ImageView icon;
    }
    
}
