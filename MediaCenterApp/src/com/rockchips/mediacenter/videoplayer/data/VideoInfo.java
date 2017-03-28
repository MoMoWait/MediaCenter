/**
 * Title: VideoInfo.java<br>
 * Package: com.rockchips.mediacenter.videoplayer.data<br>
 * Description: TODO<br>
 * @author c00224451
 * @version v1.0<br>
 * Date: 2014-7-22下午7:19:43<br> 
 * Copyright © Huawei Technologies Co., Ltd. 2014. All rights reserved.
 */

package com.rockchips.mediacenter.videoplayer.data;
import com.rockchips.mediacenter.bean.FileInfo;

/**
 * Description: TODO<br>
 * @author c00224451
 * @version v1.0
 * Date: 2014-7-22 下午7:19:43<br>
 */

public class VideoInfo extends FileInfo 
{   
	private static final long serialVersionUID = 1L;

	private float seekPosition;
    
    private int videoWidth;    
    
    private int videoHeight;

	public float getSeekPosition() {
		return seekPosition;
	}

	public void setSeekPosition(float seekPosition) {
		this.seekPosition = seekPosition;
	}

	public int getVideoWidth() {
		return videoWidth;
	}

	public void setVideoWidth(int videoWidth) {
		this.videoWidth = videoWidth;
	}

	public int getVideoHeight() {
		return videoHeight;
	}

	public void setVideoHeight(int videoHeight) {
		this.videoHeight = videoHeight;
	}
    
    
}
