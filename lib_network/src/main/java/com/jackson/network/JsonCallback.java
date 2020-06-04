package com.jackson.network;

/**
 * Copyright (C), 2015-2020
 * FileName: JsonCallback
 * Author: Luo
 * Date: 2020/3/18 9:46
 * Description:
 */
public abstract class JsonCallback<T> {
    public void onSuccess(ApiResponse<T> response) {

    }

    public void onError(ApiResponse<T> response) {

    }

    public void onCacheSuccess(ApiResponse<T> response) {

    }
}
