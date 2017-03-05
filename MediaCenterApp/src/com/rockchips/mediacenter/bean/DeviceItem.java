package com.rockchips.mediacenter.bean;
/**
 * 设备信息
 * @author s00211113
 *
 */
public class DeviceItem
{
    //上层用来识别设备的对象
    public Device mObject;
    
    //设备名
    public String mName;
    
    //设备要展示的所有媒体类型对应的图片的id
    public int[] mImageIds;

    //设备要展示的所有媒体类型对应的文字的id
    public int[] mTextIds;

    public DeviceItem(Device object, String name, int[] imageIds, int[] textIds)
    {
        mObject = object;
        mName = name;
        mImageIds = imageIds;
        mTextIds = textIds;
    }
}
