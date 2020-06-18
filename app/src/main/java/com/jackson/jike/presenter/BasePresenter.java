package com.jackson.jike.presenter;

import android.os.Looper;

import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.jackson.common.AppGlobals;
import com.jackson.jike.model.User;
import com.jackson.jike.ui.login.UserManager;

/**
 * Copyright (C), 2015-2020
 * FileName: BasePresenter
 * Author: Luo
 * Date: 2020/6/18 16:34
 * Description:
 */
public class BasePresenter {
    public static boolean isLogin(LifecycleOwner owner, Observer<User> observer) {
        if (UserManager.get().isLogin()) {
            return true;
        } else {
            LiveData<User> liveData = UserManager.get().login(AppGlobals.getApplication());
            if (owner != null) {
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    liveData.observe(owner, observer);
                } else {
                    ArchTaskExecutor.getMainThreadExecutor().execute(() ->
                            liveData.observe(owner, observer)
                    );
                }
            } else {
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    liveData.observeForever(observer);
                } else {
                    ArchTaskExecutor.getMainThreadExecutor().execute(() ->
                            liveData.observeForever(observer)
                    );
                }
            }
        }
        return false;
    }
}
