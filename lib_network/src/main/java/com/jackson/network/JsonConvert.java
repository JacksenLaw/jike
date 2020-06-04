package com.jackson.network;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.lang.reflect.Type;

/**
 * Copyright (C), 2015-2020
 * FileName: JsonConvert
 * Author: Luo
 * Date: 2020/3/18 9:46
 * Description: 默认的Json转 Java Bean的转换器
 */
public class JsonConvert implements Convert {
    @Override
    public Object convert(String response, Type type) {
        JSONObject jsonObject = JSON.parseObject(response);
        JSONObject data = jsonObject.getJSONObject("data");
        if (data != null) {
            Object data1 = data.get("data");
            return JSON.parseObject(data1.toString(), type);
        }
        return null;
    }

    @Override
    public Object convert(String response, Class clazz) {
        JSONObject jsonObject = JSON.parseObject(response);
        JSONObject data = jsonObject.getJSONObject("data");
        if (data != null) {
            Object data1 = data.get("data");
            return JSON.parseObject(data1.toString(), clazz);
        }
        return null;
    }
}
