package com.jackson.network.schedulers;

import java.util.concurrent.Executor;

/**
 * Copyright (C), 2015-2020
 * FileName: MainScheduler
 * Author: Luo
 * Date: 2020/5/14 13:53
 * Description: 主线程
 */
public class MainScheduler extends Scheduler {
    @Override
    public Executor getExecutor() {
        return mainExecutor();
    }
}
