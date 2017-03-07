/**
 * 
 */
package com.rockchips.mediacenter.service;

import com.rockchips.mediacenter.data.ConstData;

/**
 * @author GaoFei
 * 预览图加载线程,抽象类,用于优先级队列
 */
public abstract class AbstractPreviewLoadThread extends Thread implements Comparable<AbstractPreviewLoadThread>{
	
	private int threadPriporty = ConstData.THREAD_PRIORITY--;
	
	@Override
	public int compareTo(AbstractPreviewLoadThread o) {
		if(getThreadPriporty() < o.getThreadPriporty())
			return -1;
		else if(getThreadPriporty() == o.getThreadPriporty())
			return 0;
		return 1;
	}
	
	/**
	 * 获取线程优先级
	 * @return
	 */
	public int getThreadPriporty(){
		return threadPriporty;
	}
}
