package com.rockchips.mediacenter.localscan.diskscanner;

/**
 * 
 * 提供外设扫描的接口方法
 * 
 * @author  l00174030
 * @version  [2013-1-17]
 */
public interface IDiskScanController
{
    /**
     * 开始磁盘扫描
     * 
     * @param path 扫描的分区路径
     * @see [类、类#方法、类#成员]
     */
    void startDiskScan(String path);
    
    /**
     * 停止磁盘扫描
     * 
     * @param path 将要停止扫描的分区路径
     * @see [类、类#方法、类#成员]
     */
    void stopDiskScan(String path);    
}
