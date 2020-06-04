package com.jackson.jike.util;

import java.util.Calendar;

/**
 * Copyright (C), 2015-2020
 * FileName: TimeConvert
 * Author: Luo
 * Date: 2020/5/10 19:26
 * Description: 时间转换
 */
public class TimeConvert {

    public static String calculate(long time) {
        long timeInMillis = Calendar.getInstance().getTimeInMillis();

        time = parseTime(time);
        long diff = (timeInMillis - time) / 1000;
        if (diff <= 5) {
            return "刚刚";
        } else if (diff < 60) {
            return diff + "秒前";
        } else if (diff < 3600) {
            return diff / 60 + "分钟前";
        } else if (diff < 3600 * 24) {
            return diff / (3600) + "小时前";
        } else {
            return diff / (3600 * 24) + "天前";
        }
    }

    private static long parseTime(long time) {
        if (time <= 0) {
            return Calendar.getInstance().getTimeInMillis();
        }
        //兼容脏数据。抓取的数据有些帖子的时间戳不是标准的十三位
        String valueOf = String.valueOf(time);
        if (valueOf.length() < 13) {
            return time * Long.valueOf("1" + appendZero(13 - valueOf.length()));
        }
        return time;
    }

    private static String appendZero(long num) {
        StringBuilder zero = new StringBuilder();
        for (long i = 1; i <= num; i++) {
            zero.append("0");
        }
        return zero.toString();
    }

}
