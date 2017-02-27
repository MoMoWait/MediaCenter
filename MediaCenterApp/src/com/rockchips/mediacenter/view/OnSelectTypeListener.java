package com.rockchips.mediacenter.view;

/**
 * 选中后的监听类，
 * 
 * 实现者：实现它用来处理观察�?调用�?
 * 
 * 调用者：调用该接口进入到处理流程中�?
 * @author t00181037
 * @version 1.0
 */

public interface OnSelectTypeListener
{
    /**
     * 目前被Menu 使用，被MyMediaBaseActivity的子类实�? 单menu中�?择要进行的分类显示，就会掉该接口 <功能详细描述>
     * @param enumLDT 按照�?��显示
     * @see [类�?�?方法、类#成员]
     */
    void onSelectType(MenuItemImpl menuItem);

}