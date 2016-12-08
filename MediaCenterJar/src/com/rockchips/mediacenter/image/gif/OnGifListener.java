package com.rockchips.mediacenter.image.gif;

/**
 * GIF图片回调
 * @author s00211113
 *
 */
public interface OnGifListener
{
    /**
     * <一句话功能简述>设置gif图片资源
     * <功能详细描述>
     * @param in gif图片流
     * @see [类、类#方法、类#成员]
     */
    void setGifImage(GifOpenHelper gif);
    
    /**
     * <一句话功能简述>开始播放gif图
     * <功能详细描述>
     * @see [类、类#方法、类#成员]
     */
    void startPlay();

    /**
     * <一句话功能简述>停止播放播放gif图
     * <功能详细描述>
     * @see [类、类#方法、类#成员]
     */
    void stopPlay();
}
