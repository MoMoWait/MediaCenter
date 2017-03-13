/**
 * 
 */
package com.rockchips.mediacenter.service;

import java.util.List;

import com.rockchips.mediacenter.bean.FileInfo;

/**
 * @author GaoFei
 * Upnp文件加载回调函数
 */
public interface UpnpFileLoadCallback {
	public void onSuccess(List<FileInfo> fileInfos);
	public void onFailed();
}
