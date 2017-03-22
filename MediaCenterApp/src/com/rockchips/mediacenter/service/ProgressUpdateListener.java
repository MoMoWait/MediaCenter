/**
 * 
 */
package com.rockchips.mediacenter.service;

/**
 * @author GaoFei
 * 进度更新监听器
 */
public interface ProgressUpdateListener {
	void onUpdateProgress(int value);
	void onError(int errorCode);
}
