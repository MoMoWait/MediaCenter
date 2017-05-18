/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Filename:    TransportInfo.java  
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2012-6-29 下午03:17:39  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2012-6-29      fxw         1.0         create
*******************************************************************/   


package com.rockchip.mediacenter.dlna.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 *
 * @author fxw
 * @since 1.0
 */
public class PositionInfo implements Parcelable {

	private String track;
	private String trackDuration;
	private String trackMetaData;
	private String trackURI;
	private String relTime;
	private String absTime;
	private String relCount;
	private String absCount;
	private boolean isSuccessed;
	
	public String getTrack() {
		return track;
	}

	public void setTrack(String track) {
		this.track = track;
	}

	public String getTrackDuration() {
		return trackDuration;
	}

	public void setTrackDuration(String trackDuration) {
		this.trackDuration = trackDuration;
	}

	public String getTrackMetaData() {
		return trackMetaData;
	}

	public void setTrackMetaData(String trackMetaData) {
		this.trackMetaData = trackMetaData;
	}

	public String getTrackURI() {
		return trackURI;
	}

	public void setTrackURI(String trackURI) {
		this.trackURI = trackURI;
	}

	public String getRelTime() {
		return relTime;
	}

	public void setRelTime(String relTime) {
		this.relTime = relTime;
	}

	public String getAbsTime() {
		return absTime;
	}

	public void setAbsTime(String absTime) {
		this.absTime = absTime;
	}

	public String getRelCount() {
		return relCount;
	}

	public void setRelCount(String relCount) {
		this.relCount = relCount;
	}

	public String getAbsCount() {
		return absCount;
	}

	public void setAbsCount(String absCount) {
		this.absCount = absCount;
	}
	
	public boolean isSuccessed() {
		return isSuccessed;
	}

	public void setSuccessed(boolean isSuccessed) {
		this.isSuccessed = isSuccessed;
	}

	/** 
	 * <p>Title: describeContents</p> 
	 * <p>Description: </p> 
	 * @return 
	 * @see android.os.Parcelable#describeContents() 
	 */
	@Override
	public int describeContents() {
		return 0;
	}

	/** 
	 * <p>Title: writeToParcel</p> 
	 * <p>Description: </p> 
	 * @param arg0
	 * @param arg1 
	 * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int) 
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(track);
		dest.writeString(trackDuration);
		dest.writeString(trackMetaData);
		dest.writeString(trackURI);
		dest.writeString(relTime);
		dest.writeString(absTime);
		dest.writeString(relCount);
		dest.writeString(absCount);
		dest.writeInt(isSuccessed?1:0);
	}
	
	public static final Parcelable.Creator<PositionInfo> CREATOR = new Creator<PositionInfo>() {

		@Override
		public PositionInfo createFromParcel(Parcel src) {
			PositionInfo item = new PositionInfo();
			item.track = src.readString();
			item.trackDuration = src.readString();
			item.trackMetaData = src.readString();
			item.trackURI = src.readString();
			item.relTime = src.readString();
			item.absTime = src.readString();
			item.relCount = src.readString();
			item.absCount = src.readString();
			item.isSuccessed = src.readInt()==1?true:false;
			return item;
		}

		@Override
		public PositionInfo[] newArray(int size) {
			return new PositionInfo[size];
		}
		
	};
}
