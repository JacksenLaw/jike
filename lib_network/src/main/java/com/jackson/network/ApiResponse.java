package com.jackson.network;

/**
 * Copyright (C), 2015-2020
 * FileName: ApiResponse
 * Author: Luo
 * Date: 2020/3/18 9:46
 * Description:
 */
public class ApiResponse<T> {
    public boolean success;
    public int status;
    public String message;
    public T body;
}
