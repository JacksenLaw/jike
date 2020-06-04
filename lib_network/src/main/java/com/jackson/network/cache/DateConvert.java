package com.jackson.network.cache;

import androidx.room.TypeConverter;

import java.util.Date;

/**
 * Copyright (C), 2015-2020
 * FileName: DateConvert
 * Author: Luo
 * Date: 2020/3/25 22:23
 * Description: 类型转换
 */
public class DateConvert {

    @TypeConverter
    public static Long date2Long(Date date){
        return date.getTime();
    }

    @TypeConverter
    public static Date long2Date(Long leng){
        return new Date(leng);
    }

}
