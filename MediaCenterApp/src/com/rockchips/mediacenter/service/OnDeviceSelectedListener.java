package com.rockchips.mediacenter.service;

import com.rockchips.mediacenter.bean.Device;

/**
 * 设备被选中监听回调
 * @author GaoFei
 *
 */
public interface OnDeviceSelectedListener
{
    void onSelected(Device device, int offset);
}