/**
 * Title: SubTitleInfo.java<br>
 * Package: com.rockchips.iptv.stb.dlna.videoplayer.player<br>
 * Description: 保存设备上可能的字幕信息<br>
 * @author r00178559
 * @version v1.0<br>
 * Date: 2014-2-24下午7:53:17<br> 
 * Copyright © Huawei Technologies Co., Ltd. 2014. All rights reserved.
 */

package com.rockchips.mediacenter.videoplayer.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: TODO<br>
 * @author r00178559
 * @version v1.0
 * Date: 2014-2-24 下午7:53:17<br>
 */

public class SubTitleInfo
{
    public static class SubTitleFile
    {
        public boolean isFolder;
        
        public String mDisplayName;
        
        public String mFullPath;
        
        public List<SubTitleFile> mSubTitleFileList;
    }
    
    private static List<SubTitleFile> mSubTileFolderList;
    
    public static void setSubTitleFolderList(List<SubTitleFile> list)
    {
        if (null != list)
        {
            mSubTileFolderList = new ArrayList<SubTitleInfo.SubTitleFile>(list);
        }
        else
        {
            mSubTileFolderList = null;
        }
    }
    
    public static List<SubTitleFile> getSubTitleFolderList()
    {
        return mSubTileFolderList;
    }
}