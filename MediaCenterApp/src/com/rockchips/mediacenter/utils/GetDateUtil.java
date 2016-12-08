package com.rockchips.mediacenter.utils;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import com.rockchips.mediacenter.R;

import android.content.Context;

public class GetDateUtil
{

    public static final int HOUR = 60 * 60;

    public static final int MINUTE = 60;

    public static String getDate(Context context,String year, String month, String day)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 24小时制
        java.util.Date d = new java.util.Date();
        String str = sdf.format(d);
        String nowyear = str.substring(0, 4);
        String nowmonth = str.substring(5, 7);
        String nowday = str.substring(8, 10);
        String result = null;

        if (year.equals(nowyear) && month.equals(nowmonth))
        {
            if (day.equals(nowday))
            {
                result = context.getString(R.string.date_today_string);
            }
            else if (Integer.parseInt(nowday) - Integer.parseInt(day) == 1)
            {
                result = context.getString(R.string.date_yesterday_string);
            }
            else
            {
                result = year + context.getString(R.string.date_year_string) 
                       + month + context.getString(R.string.date_month_string)
                       + day + context.getString(R.string.date_day_string);
            }
        }
        else
        {
            result = year + context.getString(R.string.date_year_string) 
                    + month + context.getString(R.string.date_month_string)
                    + day + context.getString(R.string.date_day_string);
        }
        return result;
    }

    public static String getTime(Context context,long timestamp)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = null;
        try
        {
            java.util.Date currentdate = new java.util.Date(); // 当前时间

            String str = sdf.format(new Timestamp(timestamp * 1000));
//            time = str.substring(11, 16);
//
//            String year = str.substring(0, 4);
//            String month = str.substring(5, 7);
//            String day = str.substring(8, 10);
//
//            time = getDate(context, year, month, day) + time;
            time = str;
        }
        catch (IndexOutOfBoundsException  e)
        {
            // TODO Auto-generated catch block
        }
        return time;
    }

    // java Timestamp构造函数需传入Long型
    public static long IntToLong(int i)
    {
        long result = (long) i;
        result *= 1000;
        return result;
    }

    public static String getNormalTime(int timeStap)
    {
        int hour = timeStap / HOUR;
        int minute = (timeStap - hour * HOUR) / MINUTE;
        int second = timeStap - minute * MINUTE - hour * HOUR;

        return String.format("%02d:%02d:%02d", hour, minute, second);
    }
}
