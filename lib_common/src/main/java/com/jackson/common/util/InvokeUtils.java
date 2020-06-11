package com.jackson.common.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Copyright (C), 2015-2020
 * FileName: InvokeUtils
 * Author: Luo
 * Date: 2020/6/11 16:05
 * Description: 反射工具类
 */
public class InvokeUtils {

    public static Object invokeMethod(String className, String methodName) {
        try {
            Class clazz = Class.forName(className);
            Method method = clazz.getMethod(methodName);
            method.setAccessible(true);
            return method.invoke(clazz.newInstance());
        } catch (Exception e) {
            e.printStackTrace();
            KLog.e(className + " - " + methodName + " 反射失败");
        }
        return new Object();
    }

    public static Object invokeDeclaredMethod(String className, String methodName) {
        try {
            Class clazz = Class.forName(className);
            Method method = clazz.getDeclaredMethod(methodName);
            method.setAccessible(true);
            return method.invoke(clazz.newInstance());
        } catch (Exception e) {
            e.printStackTrace();
            KLog.e(className + " - " + methodName + " 反射失败");
        }
        return new Object();
    }

    public static Object invokeField(String className, String fieldName) {
        try {
            Class clazz = Class.forName(className);
            Field method = clazz.getDeclaredField(fieldName);
            method.setAccessible(true);
            return method.get(clazz.newInstance());
        } catch (Exception e) {
            e.printStackTrace();
            KLog.e(className + " - " + fieldName + " 反射失败");
        }
        return new Object();
    }

}
