package com.rockchips.mediacenter.view;

import java.util.List;

import android.view.View;

/**
 * 
 * Description: 播放列表数据适配器接口<br>
 * @author s00211113
 * @version v1.0
 * Date: 2014-7-16 上午10:14:59<br>
 */
public interface IListDataAdapter
{

    /**
     * 获得数据总数
     * 
     * @return
     */
    int getCount();

    /**
     * 创建可视范围内ItemView
     * 
     * @param index 可视范围内的Item索引值
     * @return
     */
    View createItemView(int index, int offset, int visibleItemCount);

    /**
     * 更新ItemView，当发生滚动时由ListView调用此方法刷新显示。
     * 
     * @param viewList
     * @param visibleItemCount
     * @param offset
     */
    List<View> updateItemView(List<View> viewList, int visibleItemCount, int offset);

    /**
     * 获得数据列表
     * 
     * @return
     */
    List<String> getDataList();

    /**
     * 设置数据列表
     * 
     * @param list
     */
    void setDataList(List<String> list);

    void setIndexNeedColored(int indexNeedColored);

}
