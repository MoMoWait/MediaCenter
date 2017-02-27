package com.rockchips.mediacenter.service;

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
public interface RangeDownloadListener
{
    int RDC_ERRCODE_UNKNOWN = -1;
    int RDC_ERRCODE_SUCCESSFULL = 0;
    int RDC_ERRCODE_MANUAL_STOPED = 1;
    int RDC_ERRCODE_EXCEPTION = 2;
    
    void onComplete(String url, String local, long start, long end);
    
    void onError(int errCode, Object detail);
}