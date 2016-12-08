package com.rockchips.mediacenter.mtd.download;

/**
 * 
 * 分片下载监听器
 * <功能详细描述>
 * 
 * @author  t00181037
 * @version  [版本号, 2013-6-14]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public interface MultiDownloadListener
{
    int MDC_ERRCODE_UNKNOWN = -1;
    int MDC_ERRCODE_SUCCESSFULL = 0;
    int MDC_ERRCODE_MANUAL_STOPED = 1;
    int MDC_ERRCODE_EXCEPTION = 2;
    void onError(MultiThreadDownloader mtd, int errCode, Object detail);
}