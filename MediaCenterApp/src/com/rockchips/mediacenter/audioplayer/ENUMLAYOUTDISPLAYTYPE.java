package com.rockchips.mediacenter.audioplayer;

/**
 * @author t00181037
 * @version 1.0
 * @created 16-一月-2013 17:34:35
 */
public enum ENUMLAYOUTDISPLAYTYPE
{
    
    ENUM_LDT_NONE,
    
    /**
     * 显示全部，类型
     */
    ENUM_LDT_ALL_CATEGORY,
    /**
     * 显示所有，不过滤
     */
    ENUM_LDT_ALL,
    /**
     * 按时间分类展示
     */
    ENUM_LDT_TIME,
    /**
     * 按目录分类展示
     */
    ENUM_LDT_DIR,
    /*
     * 按艺术家展示
     * */
    ENUM_LDT_ARTIST,
    /*
    * 按专辑展示
    * */
    ENUM_LDT_ALBUM,
    
    /**
     * 分類進入后的指定列表
     */
    ENUM_LDT_SPE_LIST,
    /**
     * 分類進入后的指定列表(ARTIST)
     */
    ENUM_LDT_SPE_LIST_OF_ARTIST,
    
    ENUM_LDT_SPE_LIST_OF_ALBUM,
    
    ENUM_LDT_SPE_LIST_OF_TIME,
    /**
     * 搜索
     */
    ENUM_LDT_SEARCH,
    
    /**
     * 删除
     */
    ENUM_LDT_DELETE,
    /**
     * 收藏
     */
    ENUM_LDT_FAVORITE,
    /**
     * 帮助
     */
    ENUM_LDT_HELP,
    
    /**
     * 刷新
     */
    ENUM_LDT_REFRESH,
    
    /**
     *  共享
     */
    ENUM_LDT_SHARE,
    
    /**
     *  取消共享
     */
    ENUM_LDT_CANCELSHARE,
    
    ENUM_LDT_FRIENDS,
    /**
     * 播放设置
     */
    ENUM_LDT_SET,
    
    /**
     * 飞出
     */
    ENUM_LDT_FLY_OUT,
    
    /**
     * 百叶窗
     */
    ENUM_LDT_SHUUTTER,
    
    /**
     * 百叶窗
     */
    ENUM_LDT_DISPLAY,
    
    /**
     * 8秒
     */
    ENUM_LDT_TIME_EIGHT,
    
    /**
     * 5秒
     */
    ENUM_LDT_TIME_FINE,
    
    /**
     * 3秒
     */
    ENUM_LDT_TIME_THREE,
    
    /**
     * 关闭背景音乐
     */
    ENUM_LDT_CLOSE,
    
    /**
     * 开启背景音乐
     */
    ENUM_LDT_OPEN,
    
    /**
     * 关闭显示照片详情
     */
    ENUM_PIC_DETAIL_CLOSE,
    
    /**
     * 开启显示照片详情
     */
    ENUM_PIC_DETAIL_OPEN,
    
    /**
     * 显示云相册相册，不过滤
     */
    ENUM_LDT_ALL_FOLD,
    
    ENUM_LDT_FRIEND_MORE,
    
    //关闭背景图片
    ENUM_CLOSE_BACKGROUND_PIC,
    
    //打开背景图片
    ENUM_OPEN_BACKGROUND_PIC,
    
    //随机播放
    ENUM_RANDOM_PLAY,
    
    //顺序播放
    ENUM_SEQUENTIAL_PLAY,
    
    //单曲播放
    ENUM_SINGLE_PLAY,
    
    //全部循环
    ENUM_LOOP_PLAY,
    
    //播放模式
    ENUM_AUDIO_PLAY_MODE,
    
    //播放背景
    ENUM_AUDIO_PLAY_BACKGROUND,
    
   	/* BEGIN: Modified by s00211113 for DTS2014021404690 2014/2/18 */   
    //更改浏览方式
    ENUM_LDT_CHANGE_VIEW_MODE,
    
    //全部删除
    ENUM_LDT_DELETE_ALL,
    
    //
    ENUM_LDT_ORDER_BY_TIME,
    
    //全部删除
    ENUM_LDT_ORDER_BY_CHARACTER,
   	/* END: Modified by s00211113 for DTS2014021404690 2014/2/18 */   
	/* BEGIN: Modified by s00211113 for DTS2014021404690 2014/2/24 */ 
    ENUM_LDT_DELETE_SELECTED,
	/* END: Modified by s00211113 for DTS2014021404690 2014/2/24 */ 
    
}