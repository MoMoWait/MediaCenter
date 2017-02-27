/**
 * Title: FileUtil.java<br>
 * Package: com.rockchips.mediacenter.basicutils.util<br>
 * Description: TODO<br>
 * @author w00190739
 * @version v1.0<br>
 * Date: 2014-7-16下午5:14:01<br> 
 * Copyright © Huawei Technologies Co., Ltd. 2014. All rights reserved.
 */

package com.rockchips.mediacenter.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Description: TODO<br>
 * @author w00190739
 * @version v1.0 Date: 2014-7-16 下午5:14:01<br>
 */

public class FileUtil
{

    public static String getFirstFilePath(String path)
    {
        if (StringUtils.isEmpty(path))
        {
            return null;
        }
        File file = new File(path);
        
        if (null == file || !file.exists())
        {
            return null;
        }
        
        String[] strArray = file.list();
        if (strArray == null)
        {
            return null;
        }
        
        for (String first : strArray)
        {
            for (String subfix : DiskScanUtil.IMAGE_SUFFIX)
            {
                if (first.endsWith(subfix))
                {
                    return String.valueOf(new StringBuffer().append(path).append(File.separator).append(first));
                }
            }

        }
        return null;
    }
    
    public static boolean deleteSubFiles(String path)
    {
        if (path == null)
        {
            return false;
        }
        
        File file = new File(path);

        if (file.exists() == false)
        {
            return false;
        }
        else
        {
            if (file.isFile())
            {
                return false;
            }
            if (file.isDirectory())
            {
                File[] childFile = file.listFiles();
                if (childFile == null || childFile.length == 0)
                {
                    file.delete();
                    return true;
                }
                for (File f : childFile)
                {
                    deleteFile(f);
                }
            }
        }
        
        return true;
    
    }

    public static boolean deleteFile(String path)
    {
        if (path == null)
        {
            return false;
        }
        
        return deleteFile(new File(path));
    }

    public static boolean deleteFile(File file)
    {
        if (file.exists() == false)
        {
            return false;
        }
        else
        {
            if (file.isFile())
            {
                file.delete();
                return true;
            }
            if (file.isDirectory())
            {
                File[] childFile = file.listFiles();
                if (childFile == null || childFile.length == 0)
                {
                    file.delete();
                    return true;
                }
                for (File f : childFile)
                {
                    deleteFile(f);
                }
                file.delete();
            }
        }
        
        return true;
    }

    
    /**
     * 取得文件大小
     */
    public static long getFileSizes(File f)
    {
        long s = -1;
        if (null == f || !f.exists())
        {
            return s;
        }
        
        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream(f);
            s = fis.available();
        }
        catch (FileNotFoundException e)
        {
        }
        catch (IOException e)
        {
        }
        finally
        {
            if (null != fis)
            {
                try
                {
                    fis.close();
                }
                catch (IOException e)
                {
                }
            }
        }
        return s;
    }
    
    /**
     *  取得文件夹大小 
     */
    public static long getFileSize(File f)
    {
    	if (null == f || !f.exists())
        {
            return 0;
        }
    	
        long size = 0;
        File flist[] = f.listFiles();
        if (null == flist)
        {
            return 0;
        }
        int flistLen = flist.length;
        for (int i = 0; i < flistLen; i++)
        {
            if (flist[i].isDirectory())
            {
                size = size + getFileSize(flist[i]);
            }
            else
            {
                size = size + flist[i].length();
            }
        }
        return size;
    }
}
