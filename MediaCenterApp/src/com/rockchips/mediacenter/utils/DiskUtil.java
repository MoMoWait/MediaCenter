package com.rockchips.mediacenter.utils;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import com.rockchips.mediacenter.data.DeviceDataConst;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

/**
 * 
 * 磁盘工具
 * 1、转换磁盘尺寸为合适的大小（串)
 * 
 * @author  t00181037
 * @version  [版本号, 2013-2-1]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class DiskUtil
{
    
    private static final int DISK_LEVEL_KB = 0;
    
    private static final String DISK_LEVEL_KB_DES = "KB";
    
    private static final int DISK_LEVEL_MB = 1;
    
    private static final String DISK_LEVEL_MB_DES = "MB";
    
    private static final int DISK_LEVEL_GB = 2;
    
    private static final String DISK_LEVEL_GB_DES = "GB";
    
    private static final int DISK_LEVEL_TB = 3;
    
    private static final String DISK_LEVEL_TB_DES = "TB";
    
    private static final String DISK_LEVEL_UNKNOWN_DEFAULT_DES = " ";
    
    private static final String TAG = "DiskUtil";
    /**
     * 转换为合适的字符串输出
     * 100000-->100K/100千字节
     * 10，000,000-->10M/10兆字节
     * <功能详细描述>
     * @param 输入单位是KB字节
     * 	 * @param unknownid  R.string.nullity 
	 * @param kbid	 R.string.unit_disk_size_kb
	 * @param mbid	 R.string.unit_disk_size_mb
	 * @param gbid	 R.string.unit_disk_size_gb
	 * @param tbid	 R.string.unit_disk_size_tb
     * @return
     * @see [类、类#方法、类#成员]
     */
    public static String getDiskSizeString(int size, int unknownid, int kbid, int mbid, int gbid, int tbid)
    {
        return getDiskSizeString(null, size, unknownid, kbid, mbid, gbid, tbid);
    }
    
    /**
     * 转换为合适的字符串输出
     * 100000-->100K/100千字节
     * 10，000,000-->10M/10兆字节
     * <功能详细描述>
     * @param 输入单位是KB字节
	 * @param unknownid  R.string.nullity 
	 * @param kbid	 R.string.unit_disk_size_kb
	 * @param mbid	 R.string.unit_disk_size_mb
	 * @param gbid	 R.string.unit_disk_size_gb
	 * @param tbid	 R.string.unit_disk_size_tb
     * @return
     * @see [类、类#方法、类#成员]
     */
    public static String getDiskSizeString(Context context, int size, int unknownid, int kbid, int mbid, int gbid, int tbid)
    {
        String strSize = "";
        
        if (size <= 0)
        {//未知大小
        
            if (context == null)
            {
                strSize += DISK_LEVEL_UNKNOWN_DEFAULT_DES;
            }
            else
            {
                strSize += ResLoadUtil.getStringById(context, unknownid);
            }
            return strSize;
        }
        
        //1就是M,以此加
        int iLevel = DISK_LEVEL_KB;
        
        int sizeTmp = size >> 10;
        while (sizeTmp > 0)
        {
            iLevel++;
            
            sizeTmp >>= 10;
            
            if (iLevel == DISK_LEVEL_TB)
            {
                break;
            }
        }
        
        // 整数位
        strSize += size >> (10 * iLevel);
        
        // 小数位, KB不用显示小数
        if (iLevel - 2 >= 0)
        {
            int tempSize = (size % (1 << (10 * iLevel))) >> (10 * (iLevel - 1));
            
            if (tempSize != 0)
            {
                strSize += ".";
                strSize += String.valueOf(tempSize).charAt(0);
            }
        }
        
        strSize += " ";
        
        switch (iLevel)
        {
            case DISK_LEVEL_KB:
                if (context == null)
                {
                    strSize += DISK_LEVEL_KB_DES;
                }
                else
                {
                    strSize += ResLoadUtil.getStringById(context, kbid);
                }
                break;
            
            case DISK_LEVEL_MB:
                if (context == null)
                {
                    strSize += DISK_LEVEL_MB_DES;
                }
                else
                {
                    strSize += ResLoadUtil.getStringById(context, mbid);
                }
                break;
            
            case DISK_LEVEL_GB:
                
                if (context == null)
                {
                    strSize += DISK_LEVEL_GB_DES;
                }
                else
                {
                    strSize += ResLoadUtil.getStringById(context, gbid);
                }
                
                break;
            
            case DISK_LEVEL_TB:
                
                if (context == null)
                {
                    strSize += DISK_LEVEL_TB_DES;
                }
                else
                {
                    strSize += ResLoadUtil.getStringById(context, tbid);
                }
                
                break;
            default:
                
                if (context == null)
                {
                    strSize += DISK_LEVEL_UNKNOWN_DEFAULT_DES;
                }
                else
                {
                    strSize += ResLoadUtil.getStringById(context, unknownid);
                }
                break;
        }
        
        return strSize;
        
    }
    
    /**
     * 转换为合适的字符串输出
     * 100000-->100K/100千字节
     * 10，000,000-->10M/10兆字节
     * <功能详细描述>
     * @param 输入单位是KB字节
     * 	 * @param unknownid  R.string.nullity 
	 * @param kbid	 R.string.unit_disk_size_kb
	 * @param mbid	 R.string.unit_disk_size_mb
	 * @param gbid	 R.string.unit_disk_size_gb
	 * @param tbid	 R.string.unit_disk_size_tb
     * @return
     * @see [类、类#方法、类#成员]
     */
    public static String getDiskSizeStringL(Context context, long size, int unknownid, int kbid, int mbid, int gbid, int tbid)
    {
        String strSize = "";
        
        if (size <= 0)
        {//未知大小
            if (context == null)
            {
                strSize += DISK_LEVEL_UNKNOWN_DEFAULT_DES;
            }
            else
            {
                strSize += ResLoadUtil.getStringById(context, unknownid);
            }
            return strSize;
        }
        
        //1就是M,以此加
        int iLevel = DISK_LEVEL_KB;
        
        long sizeTmp = size >> 10;
        while (sizeTmp > 0)
        {
            iLevel++;
            
            sizeTmp >>= 10;
            
            if (iLevel == DISK_LEVEL_TB)
            {
                break;
            }
        }
        
        // 整数位
        strSize += size >> (10 * iLevel);
        
        // 小数位, KB不用显示小数
        if (iLevel - 1 >= 0)
        {
            long tempSize = (size % (1 << (10 * iLevel))) >> (10 * (iLevel - 1));
            
            if (tempSize != 0)
            {
                strSize += ".";
                strSize += String.valueOf(tempSize).charAt(0);
            }
        }
        
        strSize += " ";
        
        switch (iLevel)
        {
            case DISK_LEVEL_KB:
                if (context == null)
                {
                    strSize += DISK_LEVEL_KB_DES;
                }
                else
                {
                    strSize += ResLoadUtil.getStringById(context, kbid);
                }
                break;
            
            case DISK_LEVEL_MB:
                if (context == null)
                {
                    strSize += DISK_LEVEL_MB_DES;
                }
                else
                {
                    strSize += ResLoadUtil.getStringById(context, mbid);
                }
                break;
            
            case DISK_LEVEL_GB:
                if (context == null)
                {
                    strSize += DISK_LEVEL_GB_DES;
                }
                else
                {
                    strSize += ResLoadUtil.getStringById(context, gbid);
                }
                break;
            
            case DISK_LEVEL_TB:
                if (context == null)
                {
                    strSize += DISK_LEVEL_TB_DES;
                }
                else
                {
                    strSize += ResLoadUtil.getStringById(context, tbid);
                }
                break;
            default:
                if (context == null)
                {
                    strSize += DISK_LEVEL_UNKNOWN_DEFAULT_DES;
                }
                else
                {
                    strSize += ResLoadUtil.getStringById(context, unknownid);
                }
                break;
        }
        
        return strSize;
        
    }
    
    public static String getDiskName(String mountPath)
    {
        if (null == mountPath)
        {
            return DeviceDataConst.BLANK_STRING;
        }
        int index = mountPath.lastIndexOf(File.separator);
        if (index != -1)
        {
            String diskName = mountPath.substring(index + 1);
            return diskName;
        }
        else
        {
            return DeviceDataConst.BLANK_STRING;
        }
    }

    public static float getDataFreeSizeInMB()
    {
    	File data = Environment.getDataDirectory();
    	StatFs dataStatus = new StatFs(data.getPath());
    	long availBlocks = dataStatus.getAvailableBlocksLong();
    	long blockSize = dataStatus.getBlockSizeLong();
    	float freeSpace = (availBlocks*blockSize)/1024/1024;
    	IICLOG.getInstance().d(TAG, "free sapce for cache "+ freeSpace);
    	return freeSpace;
    	
    }
    
    public static long getFreeSize(File diskFile){
    	StatFs statFs = new StatFs(diskFile.getPath());
    	return statFs.getAvailableBlocksLong() * statFs.getBlockSizeLong();
    }
    
    public static long getAllSize(File diskFile){
    	return diskFile.getTotalSpace();
    }
    
    public static long getFileSizes(File targetFile){
    	List<File> dirFiles = new LinkedList<>();
    	if(targetFile.isFile())
    		return targetFile.length();
    	else{
    		long totalSize = 0;
    		dirFiles.add(targetFile);
    		while(!dirFiles.isEmpty()){
    			File dirFile = dirFiles.remove(0);
    			File[] childFiles = dirFile.listFiles();
    			if(childFiles != null && childFiles.length > 0){
    				for(File itemFile : childFiles){
    					totalSize += itemFile.length();
    					if(itemFile.isDirectory())
    						dirFiles.add(itemFile);
    				}
    			}
    		}
    		return totalSize;
    	}
    }
    
}
