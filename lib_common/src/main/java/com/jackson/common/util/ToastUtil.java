package com.jackson.common.util;

import android.widget.Toast;

import com.jackson.common.AppGlobals;

/**
 * Copyright (C), 2015-2020
 * FileName: ToastUtil
 * Author: Luo
 * Date: 2020/5/29 16:53
 * Description:
 */
class ToastUtil {

    /**
     * 之前显示的内容
     */
    private static String oldMsg;
    /**
     * Toast对象
     */
    private static Toast mToast = null;
    /**
     * 第一次时间
     */
    private static long oneTime = 0;
    /**
     * 第二次时间
     */
    private static long twoTime = 0;

    static void showShort(String msg) {
        if (mToast == null) {
            mToast = Toast.makeText(AppGlobals.getApplication(), msg, Toast.LENGTH_SHORT);
            mToast.show();
            oneTime = System.currentTimeMillis();
        } else {
            twoTime = System.currentTimeMillis();
            if (msg.equals(oldMsg)) {
                if (twoTime - oneTime > Toast.LENGTH_SHORT) {
                    mToast.show();
                }
            } else {
                oldMsg = msg;
                mToast.setText(msg);
                mToast.show();
            }
        }
        oneTime = twoTime;

    }

    static void showLong(String msg) {
        if (mToast == null) {
            mToast = Toast.makeText(AppGlobals.getApplication(), msg, Toast.LENGTH_LONG);
            mToast.show();
            oneTime = System.currentTimeMillis();
        } else {
            twoTime = System.currentTimeMillis();
            if (msg.equals(oldMsg)) {
                if (twoTime - oneTime > Toast.LENGTH_LONG) {
                    mToast.show();
                }
            } else {
                oldMsg = msg;
                mToast.setText(msg);
                mToast.show();
            }
        }
        oneTime = twoTime;
    }

}
