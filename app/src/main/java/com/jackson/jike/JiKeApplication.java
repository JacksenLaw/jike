package com.jackson.jike;

import android.app.Application;

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

    }
}
