package com.rockchips.mediacenter.localscan.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.rockchips.mediacenter.localscan.service.LocalScanService;

/**
 * 
 * Description: 自启动服务<br>
 * @author c00224451
 * @version v1.0
 * Date: 2014-5-27 下午3:42:34<br>
 */
public class AutoBootReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {        
        if (intent != null)
        {
            String action = intent.getAction();
            if (action != null && action.equals(Intent.ACTION_BOOT_COMPLETED))
            {
                Intent newIntent = new Intent(context, LocalScanService.class);
                context.startService(newIntent);
            }
        }
    }

}
