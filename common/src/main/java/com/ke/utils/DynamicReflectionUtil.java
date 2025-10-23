package com.ke.utils;

import org.reflections.Reflections;

import java.util.Set;

/**
 * @Author: zhangshaoxun001
 * @Date: 2025/3/10 11:54
 * @Description
 */
public class DynamicReflectionUtil {


    public static <T> Set<Class<? extends T>> getAllSubTypesOf(Class<T> type) {

        // 动态扫描包
        Reflections reflections = new Reflections(type.getPackage().getName());

        // 获取某个接口的所有实现类
        return reflections.getSubTypesOf(type);

    }
}
