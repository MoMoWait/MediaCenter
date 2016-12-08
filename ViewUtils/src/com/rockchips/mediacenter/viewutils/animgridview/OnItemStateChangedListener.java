/*
 * File:OnItemStateChangedListener.java
 * (C) 版权所有 2000-2001 华为技术有限公司
 */
package com.rockchips.mediacenter.viewutils.animgridview;

/**
 * 横向列表控件Item子项状态变化（获取焦点和被选中）监听接口
 * @author yKF76250
 * 2013-1-31
 */
public interface OnItemStateChangedListener
{
    /**
     * list的某个item被选中事件
     * @param position 被选中的子项的数据位置
     */
    void onItemSelected(int position);

    /**
     * list的某个item切换到焦点事件
     * @param position 获取焦点的子项的数据位置
     */
    void onItemGetForcus(int position);
    
    
    /**
     * 界面开始移动
     * */
    void onUIChange();
}
