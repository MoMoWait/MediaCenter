/**
 * Title: MountMsgQueue.java<br>
 * Package: com.rockchips.mediacenter.basicutils.util<br>
 * Description: TODO<br>
 * @author c00224451
 * @version v1.0<br>
 * Date: 2014年10月20日下午3:53:43<br> 
 * Copyright © Huawei Technologies Co., Ltd. 2014. All rights reserved.
 */

package com.rockchips.mediacenter.basicutils.util;

import java.util.LinkedList;

import android.content.Intent;

/**
 * Description: TODO<br>
 * @author c00224451
 * @version v1.0
 * Date: 2014年10月20日 下午3:53:43<br>
 */

public class MountMsgQueue
{
    private static final String TAG = "MountMsgQueue";
    private IICLOG mLog = IICLOG.getInstance();
    private LinkedList<Intent> mItemList;
    
    public MountMsgQueue()
    {
        mItemList = new LinkedList<Intent>();
    }
    public void enqueue(Intent e)
    {       
        //插入执行策略        
        synchronized (mItemList)
        {           
            enqueueStrategy(e);
            mItemList.addLast(e);
            mItemList.notifyAll();  
        }
    }
    
    private void enqueueStrategy(Intent e)
    {
        if (e == null)
        {
            return;
        }
                
        String dataUri = e.getDataString();        
        
        if (dataUri == null)
        {
            return;
        }
        
        synchronized (mItemList)
        {
            Intent intent = searchTheSameIntent(dataUri);
            if (null != intent)
            {
                mItemList.remove(intent);
            }            
        }
    }
    
    private Intent searchTheSameIntent(String dataUri)
    {
        Intent intent = null;
        synchronized (mItemList)
        {
            for (int i = 0; i < mItemList.size(); ++i)
            {
                intent = mItemList.get(i);
                if (intent != null && intent.getDataString() != null)
                {
                    if (dataUri.equals(intent.getDataString()))
                    {
                        return intent;
                    }
                }                
            }
            return null;
        }
    }
    
    public Intent dequeue()
    {
        Intent e = null;       
        synchronized (mItemList)
        {
            if (mItemList.isEmpty())
            {
                try
                {
                    mItemList.wait();
                }
                catch (InterruptedException exception)
                {
                    mLog.e(TAG, "getItem catch InterruptedExceptiond!");
                    return e;
                }
            }
            
            if (!mItemList.isEmpty())
            {
                e = mItemList.getFirst();
                mItemList.removeFirst();  
            }
        }
        return e;
    }    
}
