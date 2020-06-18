package com.jackson.jike;

import android.app.Application;
import android.os.Looper;

import com.jackson.common.crash.Cockroach;
import com.jackson.common.crash.ExceptionHandler;
import com.jackson.common.util.CommonUtil;
import com.jackson.common.util.KLog;
import com.jackson.network.ApiService;

/**
 * Copyright (C), 2015-2020
 * FileName: JiKeApplication
 * Author: Luo
 * Date: 2020/3/26 22:47
 * Description:
 */
public class JiKeApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ApiService.init("http://123.56.232.18:8080/serverdemo", null);

        final Thread.UncaughtExceptionHandler sysExcepHandler = Thread.getDefaultUncaughtExceptionHandler();
        Cockroach.install(this, new ExceptionHandler() {
            @Override
            protected void onUncaughtExceptionHappened(Thread thread, Throwable throwable) {
                KLog.e("--->onUncaughtExceptionHappened:" + thread + "<---", throwable);
                KLog.i("上报至bugly");
                CommonUtil.showToast(throwable.getLocalizedMessage());
            }

            @Override
            protected void onBandageExceptionHappened(Throwable throwable) {
                throwable.printStackTrace();//打印警告级别log，该throwable可能是最开始的bug导致的，无需关心
                CommonUtil.showToast(throwable.getLocalizedMessage());
            }

            @Override
            protected void onEnterSafeMode() {
                CommonUtil.showToast("已经进入安全模式");
            }

            @Override
            protected void onMayBeBlackScreen(Throwable e) {
                Thread thread = Looper.getMainLooper().getThread();
                KLog.e("--->onUncaughtExceptionHappened:" + thread + "<---", e);
                //黑屏时建议直接杀死app
                sysExcepHandler.uncaughtException(thread, new RuntimeException("black screen"));
            }
        });
    }
}
