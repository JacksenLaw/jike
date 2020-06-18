package com.jackson.common.util;

import android.annotation.SuppressLint;
import android.os.Looper;

import androidx.annotation.StringRes;
import androidx.arch.core.executor.ArchTaskExecutor;

import com.jackson.common.AppGlobals;

/**
 * Copyright (C), 2015-2020
 * FileName: CommonUtil
 * Author: Luo
 * Date: 2020/5/9 15:44
 * Description:
 */
public class CommonUtil {

    @SuppressLint("RestrictedApi")
    public static void showToast(final String msg) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            ToastUtil.showShort(msg);
        } else {
            ArchTaskExecutor.getMainThreadExecutor().execute(() ->
                    ToastUtil.showShort(msg)
            );
        }
    }

    @SuppressLint("RestrictedApi")
    public static void showToast(@StringRes final int resId) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            ToastUtil.showShort(AppGlobals.getApplication().getString(resId));
        } else {
            ArchTaskExecutor.getMainThreadExecutor().execute(() ->
                    ToastUtil.showShort(AppGlobals.getApplication().getString(resId))
            );
        }
    }

}
