/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Filename:    MediaCenterServiceImpl.java  
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2012-6-29 下午05:43:52  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2012-6-29      fxw         1.0         create
*******************************************************************/   


package com.rockchip.mediacenter.dlna;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import android.os.RemoteException;

import com.rockchip.mediacenter.DLNAService;
import com.rockchip.mediacenter.common.logging.Log;
import com.rockchip.mediacenter.common.logging.LogFactory;
import com.rockchip.mediacenter.core.dlna.protocols.response.avtransport.CommonControlPlayResponse;
import com.rockchip.mediacenter.core.dlna.protocols.response.avtransport.GetPositionInfoResponse;
import com.rockchip.mediacenter.core.dlna.protocols.response.avtransport.GetTransportInfoResponse;
import com.rockchip.mediacenter.core.dlna.protocols.response.avtransport.SetAVTransportURIResponse;
import com.rockchip.mediacenter.core.dlna.protocols.response.renderingcontrol.GetVolumeResponse;
import com.rockchip.mediacenter.core.dlna.protocols.response.renderingcontrol.SetVolumeResponse;
import com.rockchip.mediacenter.dlna.dmc.DigitalMediaController;
import com.rockchip.mediacenter.dlna.dmc.SeekMode;
import com.rockchip.mediacenter.dlna.dmp.DigitalMediaPlayer;
import com.rockchip.mediacenter.dlna.model.DeviceItem;
import com.rockchip.mediacenter.dlna.model.PositionInfo;
import com.rockchip.mediacenter.dlna.model.TransportInfo;

/**
 *
 * @author fxw
 * @since 1.0
 */
public class MediaCenterServiceImpl extends IMediaCenterService.Stub {
	
	private Log logger = LogFactory.getLog(MediaCenterServiceImpl.class);
	private DLNAService mService;
	private DigitalMediaPlayer dmp;
	private DigitalMediaController dmc;
	private DeviceItem mDeviceItem;
	private boolean isExiting;
	
	public MediaCenterServiceImpl(DLNAService service){
		this.mService = service;
		dmp = mService.getDigitalMediaPlayer();
		dmc = mService.getDigitalMediaController();
	}

	/** 
	 * <p>Title: search</p> 
	 * <p>Description: </p> 
	 * @throws RemoteException 
	 * @see com.rockchip.mediacenter.dlna.IMediaCenterService#search() 
	 */
	@Override
	public void search() throws RemoteException {
		dmp.search();
	}

	/** 
	 * <p>Title: research</p> 
	 * <p>Description: </p> 
	 * @throws RemoteException 
	 * @see com.rockchip.mediacenter.dlna.IMediaCenterService#research() 
	 */
	@Override
	public void research() throws RemoteException {
		dmp.removeAllDevice();
		dmp.search();
	}
	
	/** 
	 * <p>Title: getMediaServerDevice</p> 
	 * <p>Description: </p> 
	 * @return
	 * @throws RemoteException 
	 * @see com.rockchip.mediacenter.dlna.IMediaCenterService#getMediaServerDevice(boolean) 
	 */
	@Override
	public List<DeviceItem> getMediaServerDevice(boolean hasLocalDevice) throws RemoteException {
		return mService.getMediaServerDevice(hasLocalDevice);
	}

	/** 
	 * <p>Title: getMediaRendererDevice</p> 
	 * <p>Description: </p> 
	 * @return
	 * @throws RemoteException 
	 * @see com.rockchip.mediacenter.dlna.IMediaCenterService#getMediaRendererDevice() 
	 */
	@Override
	public List<DeviceItem> getMediaRendererDevice() throws RemoteException {
		return mService.getMediaRendererDevice(false);
	}

	/** 
	 * <p>Title: setTargetDevice</p> 
	 * <p>Description: </p> 
	 * @param deviceItem
	 * @throws RemoteException 
	 * @see com.rockchip.mediacenter.dlna.IMediaCenterService#setTargetDevice(com.rockchip.mediacenter.dlna.model.DeviceItem) 
	 */
	@Override
	public void setTargetDevice(DeviceItem deviceItem) throws RemoteException {
		mDeviceItem = deviceItem;
		setTargetDeviceInternal();
	}
	
	private void setTargetDeviceInternal(){
		dmc.setTargetDevice(mDeviceItem);
		logger.debug("Set target device: "+mDeviceItem.getFriendlyName());
	}

	/** 
	 * <p>Title: setAVTransportURI</p> 
	 * <p>Description: </p> 
	 * @param filePath
	 * @return
	 * @throws RemoteException 
	 * @see com.rockchip.mediacenter.dlna.IMediaCenterService#setAVTransportURI(java.lang.String) 
	 */
	@Override
	public boolean setAVTransportURI(String path) throws RemoteException {
		logger.debug("Incoming request url: "+path);
		SetAVTransportURIResponse response = null;
		if(isHttpURL(path)){//http url
			URL url;
			try {
				url = new URL(path);
				response = dmc.setAVTransportURI(url);
				logger.debug("Set http url result: "+response.isSuccessed());
			} catch (MalformedURLException e) {
				return false;
			}
		}else{
			File srcFile = new File(path);
			if(!srcFile.exists()) return false;
			response = dmc.setAVTransportURI(srcFile);
			logger.debug("Set local file result: "+response.isSuccessed());
		}
		return response!=null&&response.isSuccessed();
	}
	
	public boolean setMetaDataAndURI(String path, String title, String mimeType, long size, int duration) throws RemoteException {
		SetAVTransportURIResponse response = null;
		if(isHttpURL(path)){
			URL url;
			try {
				url = new URL(path);
				response = dmc.setAVTransportURI(url, title, mimeType, size, duration);
			} catch (MalformedURLException e) {
				return false;
			}
		}else{
			File srcFile = new File(path);
			if(!srcFile.exists()) return false;
			response = dmc.setAVTransportURI(srcFile, duration);
		}
		return response!=null&&response.isSuccessed();
	}
	
	private boolean isHttpURL(String path){
		return path!=null&&path.length()>4&&"http".equalsIgnoreCase(path.substring(0, 4));
	}

	/** 
	 * <p>Title: play</p> 
	 * <p>Description: </p> 
	 * @return
	 * @throws RemoteException 
	 * @see com.rockchip.mediacenter.dlna.IMediaCenterService#play() 
	 */
	@Override
	public boolean play() throws RemoteException {
		CommonControlPlayResponse response = dmc.play();
		return response!=null&&response.isSuccessed();
	}

	/** 
	 * <p>Title: pause</p> 
	 * <p>Description: </p> 
	 * @return
	 * @throws RemoteException 
	 * @see com.rockchip.mediacenter.dlna.IMediaCenterService#pause() 
	 */
	@Override
	public boolean pause() throws RemoteException {
		CommonControlPlayResponse response = dmc.pause();
		return response!=null&&response.isSuccessed();
	}

	/** 
	 * <p>Title: stop</p> 
	 * <p>Description: </p> 
	 * @return
	 * @throws RemoteException 
	 * @see com.rockchip.mediacenter.dlna.IMediaCenterService#stop() 
	 */
	@Override
	public boolean stop() throws RemoteException {
		CommonControlPlayResponse response = dmc.stopPlay();
		return response!=null&&response.isSuccessed();
	}

	/** 
	 * <p>Title: seek</p> 
	 * <p>Description: </p> 
	 * @param seekTarget
	 * @return
	 * @throws RemoteException 
	 * @see com.rockchip.mediacenter.dlna.IMediaCenterService#seek(java.lang.String) 
	 */
	@Override
	public boolean seek(String seekTarget) throws RemoteException {
		CommonControlPlayResponse response = dmc.seek(SeekMode.REL_TIME, seekTarget);
		return response!=null&&response.isSuccessed();
	}

	/** 
	 * <p>Title: getVolume</p> 
	 * <p>Description: </p> 
	 * @return
	 * @throws RemoteException 
	 * @see com.rockchip.mediacenter.dlna.IMediaCenterService#getVolume() 
	 */
	@Override
	public int getVolume() throws RemoteException {
		GetVolumeResponse response = dmc.getVolume();
		return response.getCurrentVolume();
	}

	/** 
	 * <p>Title: setVolume</p> 
	 * <p>Description: </p> 
	 * @param volume
	 * @return
	 * @throws RemoteException 
	 * @see com.rockchip.mediacenter.dlna.IMediaCenterService#setVolume(int) 
	 */
	@Override
	public boolean setVolume(int volume) throws RemoteException {
		SetVolumeResponse response = dmc.setVolume(volume);
		return response!=null&&response.isSuccessed();
	}
	
	/**
	 * 获取远程设备最大音量值
	 * @return
	 * @throws RemoteException
	 */
	public int getMaxVolume() throws RemoteException {
		return dmc.getMaxVolume();
	}

	/** 
	 * <p>Title: getTransportInfo</p> 
	 * <p>Description: </p> 
	 * @return
	 * @throws RemoteException 
	 * @see com.rockchip.mediacenter.dlna.IMediaCenterService#getTransportInfo() 
	 */
	@Override
	public TransportInfo getTransportInfo() throws RemoteException {
		GetTransportInfoResponse response = dmc.getTransportInfo();
		TransportInfo info = new TransportInfo();
		info.setSuccessed(response.isSuccessed());
		if(response.isSuccessed()){
			info.setCurrentTransportState(response.getCurrentTransportState());
			info.setCurrentTransportStatus(response.getCurrentTransportStatus());
			info.setCurrentSpeed(response.getCurrentSpeed());
		}
		return info;
	}

	/** 
	 * <p>Title: getPositionInfo</p> 
	 * <p>Description: </p> 
	 * @return
	 * @throws RemoteException 
	 * @see com.rockchip.mediacenter.dlna.IMediaCenterService#getPositionInfo() 
	 */
	@Override
	public PositionInfo getPositionInfo() throws RemoteException {
		GetPositionInfoResponse response = dmc.getPositionInfo();
		PositionInfo info = new PositionInfo();
		info.setSuccessed(response.isSuccessed());
		if(response.isSuccessed()){
			info.setTrack(response.getTrack());
			info.setTrackDuration(response.getTrackDuration());
			info.setTrackMetaData(response.getTrackMetaData());
			info.setTrackURI(response.getTrackURI());
			info.setAbsCount(response.getAbsCount());
			info.setRelCount(response.getRelCount());
			info.setAbsTime(response.getAbsTime());
			info.setRelTime(response.getRelTime());
		}
		return info;
	}

	/** 
	 * <p>Title: asyncExit</p> 
	 * <p>Description: </p> 
	 * @return
	 * @throws RemoteException 
	 * @see com.rockchip.mediacenter.dlna.IMediaCenterService#asyncExit() 
	 */
	@Override
	public boolean asyncExit() throws RemoteException {
		if(isExiting){
			return false;
		}
		isExiting = true;
		new Thread(){
			public void run() {
				dmc.exitPlayer();
				isExiting = false;
			}
		}.start();
		return true;
	}
	
	/** 
	 * <p>Title: exit</p> 
	 * <p>Description: </p> 
	 * @return
	 * @throws RemoteException 
	 * @see com.rockchip.mediacenter.dlna.IMediaCenterService#exit() 
	 */
	@Override
	public boolean exit() throws RemoteException {
		CommonControlPlayResponse response = dmc.exitPlayer();
		return response!=null&&response.isSuccessed();
	}

}
