package com.jackson.network;

import java.lang.reflect.Type;

/**
 * Copyright (C), 2015-2020
 * FileName: Convert
 * Author: Luo
 * Date: 2020/3/18 9:46
 * Description: 转换器
 */
public interface Convert<T> {
    T convert(String response, Type type);

    T convert(String response, Class clazz);
}
