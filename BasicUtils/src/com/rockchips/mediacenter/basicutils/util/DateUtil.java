package com.rockchips.mediacenter.basicutils.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil
{
    
    public DateUtil()
    {
    }
    /**
     *  将毫秒数换算成x天x时x分x秒
     * @param ms 毫秒数 
     * 0---->00:00
     * */
    public static String formatDuration(long ms){
        int ss = 1000;
        int mi = ss * 60;
        int hh = mi * 60;
        int dd = hh * 24;
        long day = ms / dd;
        long hour = (ms - day * dd) / hh;
        long minute = (ms - day * dd - hour * hh) / mi;
        long second = (ms - day * dd - hour * hh - minute * mi) / ss;
        long milliSecond = ms - day * dd - hour * hh - minute * mi - second * ss;
        String strDay    = day==0 ? "" : day+":";
        String strHour   = day==0&&hour==0 ? "" : hour+":";
        String strMinute = day==0&&hour==0&&minute==0 ? "00:" : minute+":";
        String strSecond = second+""; 
        
        strDay    = strDay.length()==2 ? "0"+strDay : strDay;
        strHour   = strHour.length()==2 ? "0"+strHour : strHour;
        strMinute = strMinute.length()==2 ? "0"+strMinute : strMinute;
        strSecond = strSecond.length()==1 ? "0"+strSecond : strSecond;
        
        String strMilliSecond = milliSecond < 10 ? "0" + milliSecond : "" + milliSecond;
        strMilliSecond = milliSecond < 100 ? "0" + strMilliSecond : "" + strMilliSecond;
        return strDay + strHour + strMinute + strSecond;
//        return strDay + strHour + strMinute + strSecond + "." + strMilliSecond;
    }
    
	public static String getCurrentTime(){
		String curTime = "";
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		curTime = df.format(new Date());// new Date()为获取当前系统时间
		return curTime;
	}
	
    /**
     *  将毫秒数换算成x天x时x分x秒
     * @param ms 毫秒数 
     * 0---->00:00:00
     * private static final int LEN_HH_MM_SS = 8;
     * */
	private static final int LEN_MM_SS = 5;
	public static String formatTime(long millis)
    {
		StringBuffer buf = new StringBuffer();
		buf.append(formatDuration(millis));
		if (buf.length() <= LEN_MM_SS)
		{
			buf.insert(0, "00:");
		}
        return String.valueOf(buf);
    }
}
