package com.ke.utils;

import cn.hutool.core.bean.copier.CopyOptions;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/9/6 18:58
 * @Version 1.0
 * @Description
 */
public class BeanUtil {

    public static void copyNonNullProperties(Object source, Object dest) {
        cn.hutool.core.bean.BeanUtil.copyProperties(source, dest, new CopyOptions().ignoreNullValue());
    }
}
