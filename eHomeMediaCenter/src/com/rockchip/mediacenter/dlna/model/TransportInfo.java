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
public class TransportInfo implements Parcelable {

	private boolean isSuccessed;
	private String currentTransportState;
	private String currentTransportStatus;
	private String currentSpeed;
	
	public String getCurrentTransportState() {
		return currentTransportState;
	}

	public void setCurrentTransportState(String currentTransportState) {
		this.currentTransportState = currentTransportState;
	}

	public String getCurrentTransportStatus() {
		return currentTransportStatus;
	}

	public void setCurrentTransportStatus(String currentTransportStatus) {
		this.currentTransportStatus = currentTransportStatus;
	}

	public String getCurrentSpeed() {
		return currentSpeed;
	}

	public void setCurrentSpeed(String currentSpeed) {
		this.currentSpeed = currentSpeed;
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
		dest.writeString(currentTransportState);
		dest.writeString(currentTransportStatus);
		dest.writeString(currentSpeed);
		dest.writeInt(isSuccessed?1:0);
	}
	
	public static final Parcelable.Creator<TransportInfo> CREATOR = new Creator<TransportInfo>() {

		@Override
		public TransportInfo createFromParcel(Parcel src) {
			TransportInfo item = new TransportInfo();
			item.currentTransportState = src.readString();
			item.currentTransportStatus = src.readString();
			item.currentSpeed = src.readString();
			item.isSuccessed = src.readInt()==1?true:false;
			return item;
		}

		@Override
		public TransportInfo[] newArray(int size) {
			return new TransportInfo[size];
		}
		
	};
}
