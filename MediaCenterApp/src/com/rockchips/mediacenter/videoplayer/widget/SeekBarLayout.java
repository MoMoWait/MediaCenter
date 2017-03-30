package com.rockchips.mediacenter.videoplayer.widget;

import momo.cn.edu.fjnu.androidutils.utils.SizeUtils;

import org.xutils.x;
import org.xutils.view.annotation.ViewInject;

import android.R.integer;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.rockchips.mediacenter.R;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.utils.DateUtil;
public class SeekBarLayout extends RelativeLayout
{
	private Context mContext;
	@ViewInject(R.id.playStatusImg)
	private ImageView mImgPlayStatus;
	@ViewInject(R.id.seekbar)
	private VideoseekBar mVideoseekBar;
	@ViewInject(R.id.totalDuration)
	private TextView mTextTotalTime;
	@ViewInject(R.id.elapsedDuration)
	private TextView mTextCurrPlayTime;
	@ViewInject(R.id.text_seek_time)
	private TextView mTextSeekTime;
	@ViewInject(R.id.layout_seek_time)
	private LinearLayout mLayoutSeekTime;
    public SeekBarLayout(Context context)
    {
        super(context);
        mContext = context;
        SeekBarLayout.inflate(context, R.layout.video_seekbar_layout, this);
        initView();
    }

    public SeekBarLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        mContext = context;
        SeekBarLayout.inflate(context, R.layout.video_seekbar_layout, this);
        initView();
    }

    public SeekBarLayout(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        mContext = context;
        SeekBarLayout.inflate(context, R.layout.video_seekbar_layout, this);
        initView();
    }
    
    public void initView(){
    	x.view().inject(this);
    	mVideoseekBar.setSeekBarLayout(this);
    }

    /**
     * 设置播放状态
     * @param status
     */
    public void setPlayStatus(int status){
    	switch (status) {
		case ConstData.VIDEO_PLAY_STATUS.PAUSED:
			mImgPlayStatus.setImageResource(R.drawable.play_status_pause);
			break;
		case ConstData.VIDEO_PLAY_STATUS.PLAYING:
			mImgPlayStatus.setImageResource(R.drawable.play_status_play);
			break;
		case ConstData.VIDEO_PLAY_STATUS.FAST_GO:
			mImgPlayStatus.setImageResource(R.drawable.play_status_acc);
			break;
		case ConstData.VIDEO_PLAY_STATUS.FAST_BACK:
			mImgPlayStatus.setImageResource(R.drawable.play_status_backacc);
			break;
		default:
			break;
		}
    }
    /**
     * 设置播放总时长
     * @param duration
     */
    public void setTotalDuration(int duration){
    	mTextTotalTime.setText(DateUtil.getMediaTime(duration));
    }
    
    /**
     * 设置当前播放位置
     * @param postion
     */
    public void setCurrPlayPosition(int postion, int duration){
    	mTextCurrPlayTime.setText(DateUtil.getMediaTime(postion));
    	mVideoseekBar.setScale(postion * 1.0f / duration);
    	mVideoseekBar.invalidate();
    }
    
    /**
     * 设置当前seek位置
     * @param position
     */
    public void setSeekPosition(int position){
    	
    }
    
    /**
     * 同步播放位置
     */
    public void syncPlayPosition(){
    	
    }
    
    public void setPosition(int playPosition, int seekPosition, int duration, boolean needShowSeekLayout){
    	mTextCurrPlayTime.setText(DateUtil.getMediaTime(playPosition));
    	mTextTotalTime.setText(DateUtil.getMediaTime(duration));
    	mVideoseekBar.setScale(playPosition * 1.0f / duration);
    	mVideoseekBar.setKscale(seekPosition * 1.0f / duration);
    	mTextSeekTime.setText(DateUtil.getMediaTime(seekPosition));
    	mVideoseekBar.setNeedShowSeekLayout(needShowSeekLayout);
    	mVideoseekBar.invalidate();
    }
    /**
     * 更新当前Seek位置
     * @param position
     */
    public void updateSeekLayoutPosition(int position){
    	RelativeLayout.LayoutParams seekTextParams = (RelativeLayout.LayoutParams)mLayoutSeekTime.getLayoutParams();
    	seekTextParams.leftMargin = SizeUtils.dp2px(mContext, 170) + position - mLayoutSeekTime.getWidth() / 2;
    	mLayoutSeekTime.setLayoutParams(seekTextParams);
    	if(mLayoutSeekTime.getVisibility() != View.VISIBLE){
    		mLayoutSeekTime.setVisibility(View.VISIBLE);
    	}
    }
    
    /**
     * 设置Seek布局的可见性
     * @param type
     */
    public void setSeekTimeVisibility(int type){
    	mLayoutSeekTime.setVisibility(type);
    }
    
    public int getSeekTimeVisibility(){
    	return mLayoutSeekTime.getVisibility();
    }
}
