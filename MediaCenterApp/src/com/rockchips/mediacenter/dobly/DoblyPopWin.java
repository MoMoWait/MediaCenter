package com.rockchips.mediacenter.dobly;

import android.content.Context;

import com.rockchips.mediacenter.utils.PlatformUtil;

/**
 * 
 * 封装杜比的弹出窗口功能
 * 
 * @author  l00174030
 * @version  [2013-7-21]
 */
public class DoblyPopWin
{
    // 是否含有杜比
    private boolean hasDobly = false;
    
    // 杜比格式
    private final int FORMAT_AC3 = 3;
    private final int FORMAT_EAC3 = 43;
    private final int FORMAT_TRUEHD = 47;
    
    // 杜比信息弹窗
    private FloatView floatView1 = null;
    //1+1图标
    private FloatView floatView2 = null;
    //音频是否透传
    private boolean isAudioPassThrough = false;
    
    private Context context = null;
    
    private boolean isTheRegionNeedPop = true;
    
    public DoblyPopWin(Context c)
    {
        context = c;
        if (floatView1 == null)
        {
        	floatView1 = new FloatView(c);
        }
        if (floatView2 == null)
        {
        	floatView2 = new FloatView(c,0);
        }
        // 初始化区域
    }
    
    /**
     * 弹出杜比显示框
     * 
     * @see [类、类#方法、类#成员]
     */
    public void showDoblyWin()
    {
        if (floatView1 == null)
        {
            return;
        }
        
        if(hasDobly && isAudioPassThrough){
        	//透传只显示杜比
            floatView1.show();
            hasDobly = false;
        }else if(hasDobly && floatView2 != null){
        	// 有杜比音效，并且杜比信息没显示，才显示
        	floatView1.show();
//            floatView2.show();
            hasDobly = false;       	
        } 
        
    }
    
    /**
     * 隐藏杜比显示框
     * 
     * 1.马上按返回键的场合
     * 2.播放失败的场合
     * 3.视频切换的场合
     * 4.推屏拉回的场合
     * 5.播放完成的场合
     * 
     * @see [类、类#方法、类#成员]
     */
    public void hideDoblyWin()
    {
        if (floatView1 != null)
        {
            floatView1.hide();
        }
        if(floatView2 != null) {
            floatView2.hide();
        }
    }
    
    /**
     * 判断是否是杜比音效
     * 有一个音轨是杜比的就表示属于杜比
     * @param audioFormat 音频格式
     * @return 是否含有杜比音效
     * @see [类、类#方法、类#成员]
     */
    public void checkHasDobly(int audioFormat)
    {
        // 非指定场合，不弹出杜比的信息窗口
        if (!isTheRegionNeedPop)
        {
            return;
        }
        isAudioPassThrough = PlatformUtil.isAudioPassThrough();
        if (audioFormat == FORMAT_AC3 || audioFormat == FORMAT_EAC3
            ||audioFormat == FORMAT_TRUEHD || audioFormat == -100)
        {
            hasDobly = true;
        }
    }
    
}
