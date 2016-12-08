/*
 * HWHorizontalListView.java
 * (C) 版权所有 2000-2001 华为技术有限公司
 */
package com.rockchips.mediacenter.viewutils.animgridview;

/**
 * @author yKF76250
 * 
 */
public interface OnScrollerChangedListener
{
    /**
     * 当列表发生滚动事件。
     * @param startPosition 可视范围的起始数据位置
     * @param endPosition 可视范围的结束数据位置
     * @param totalSize 数据总大小
     */
    void onListScrolll(int startPosition, int endPosition, int totalSize);
}
