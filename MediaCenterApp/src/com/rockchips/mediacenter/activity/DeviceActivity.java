/**
 * Title: DevListenerActivity.java<br>
 * Package: com.rockchips.mediacenter.activity<br>
 * Description: TODO<br>
 * @author w00190739
 * @version v1.0<br>
 * Date: 2014-8-8下午4:45:29<br> 
 * Copyright © Huawei Technologies Co., Ltd. 2014. All rights reserved.
 */

package com.rockchips.mediacenter.activity;
import android.os.Bundle;
/**
 * Description: 监听本地及DLAN设备变化的Acitivity<br>
 * @author w00190739
 * @version v1.0 Date: 2014-8-8 下午4:45:29<br>
 */

public abstract class DeviceActivity extends AppBaseActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }
    
    
}

