package com.rockchips.mediacenter.imageplayer;

/**
 * 用于动态的设置DLNAImageSwitcher的imageview的scaletype
 */
public interface DLNAImageSwitcherViewFactory
{
    void setScaleTypeCenterInSide();

    void setScaleTypeFixCenter();

    void displayProgreeBar();
}
