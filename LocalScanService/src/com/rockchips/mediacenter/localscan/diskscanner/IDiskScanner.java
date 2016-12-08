package com.rockchips.mediacenter.localscan.diskscanner;

/**
 * 
 * 磁盘扫描器的接口
 * 
 * @author  l00174030
 * @version  [2013-1-21]
 */
public interface IDiskScanner
{
    /**
     * 磁盘扫描的具体实现
     * 
     * @param path 分区路径
     * @see [类、类#方法、类#成员]
     */
    void scanDiskByPath(String path);
    
}
