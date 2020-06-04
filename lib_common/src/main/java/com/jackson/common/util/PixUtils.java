package com.jackson.common.util;

import android.util.DisplayMetrics;

import com.jackson.common.AppGlobals;

/**
 * Copyright (C), 2015-2020
 * FileName: PixUtils
 * Author: Luo
 * Date: 2020/3/26 20:43
 * Description: 像素转换工具类
 */
public class PixUtils {

    public static int dp2px(int dpVaule) {
        DisplayMetrics metrics = AppGlobals.getApplication().getResources().getDisplayMetrics();
        return (int) (metrics.density * dpVaule + 0.5f);
    }

    public static int getScreenWidth(){
        DisplayMetrics metrics = AppGlobals.getApplication().getResources().getDisplayMetrics();
        return metrics.widthPixels;
    }

    public static int getScreenHeight(){
        DisplayMetrics metrics = AppGlobals.getApplication().getResources().getDisplayMetrics();
        return metrics.heightPixels;
    }

}
