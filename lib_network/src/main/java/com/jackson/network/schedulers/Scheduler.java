package com.jackson.network.schedulers;

import androidx.arch.core.executor.ArchTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Copyright (C), 2015-2020
 * FileName: Scheduler
 * Author: Luo
 * Date: 2020/5/14 12:56
 * Description: 线程切换
 */
public abstract class Scheduler {

    private static class Holder {
        static final Executor MAIN = ArchTaskExecutor.getMainThreadExecutor();
        static final Executor IO = ArchTaskExecutor.getIOThreadExecutor();
    }

    public static Executor ioExecutor() {
        return Holder.IO;
    }

    public static Executor mainExecutor() {
        return Holder.MAIN;
    }

    public abstract Executor getExecutor();

    public static Scheduler mainThread() {
        return new MainScheduler();
    }

    public static Scheduler ioThread() {
        return new IoScheduler();
    }

}
