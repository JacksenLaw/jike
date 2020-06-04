package com.jackson.network;

/**
 * Copyright (C), 2015-2020
 * FileName: GetRequest
 * Author: Luo
 * Date: 2020/3/18 9:46
 * Description:
 */
public class GetRequest<T> extends Request<T, GetRequest> {
    public GetRequest(String url) {
        super(url);
    }

    @Override
    protected okhttp3.Request generateRequest(okhttp3.Request.Builder builder) {
        //get 请求把参数拼接在 url后面
        String url = UrlCreator.createUrlFromParams(mUrl, params);
        okhttp3.Request request = builder.get().url(url).build();
        return request;
    }
}
