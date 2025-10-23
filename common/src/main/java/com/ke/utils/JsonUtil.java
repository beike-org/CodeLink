package com.ke.utils;

import com.alibaba.fastjson.JSONObject;
import com.ke.exception.ExceptionEnum;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/8/28 14:43
 * @Version 1.0
 * @Description
 */
public class JsonUtil {

    public static <T> T getData(String s, Class<T> clazz) {
        try {
            return JSONObject.parseObject(s, clazz);
        } catch (Exception e) {
            ExceptionEnum.JSON_SERIALIZE_EXCEPTION.asBusinessException();
            throw e;
        }
    }

    public static String toJson(Object data) {
        return JSONObject.toJSONString(data);
    }
}
