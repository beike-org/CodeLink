package com.ke.utils;

import com.intellij.ide.util.PropertiesComponent;

/**
 * @Author: zhangshaoxun001
 * @Date: 2022/8/15 18:37
 * @Version 1.0
 */
public class StoreUtil {

    public static String getStore(String key){
        return PropertiesComponent.getInstance().getValue(key);
    }

    public static void putStore(String key,String value){
        PropertiesComponent.getInstance().setValue(key,value);
    }

    public static String getStore(StoreKeys key){
        return PropertiesComponent.getInstance().getValue(key.getKey());
    }

    public static void putStore(StoreKeys key,String value){
        PropertiesComponent.getInstance().setValue(key.getKey(),value);
    }

    public static void putStore(StoreKeys key,Boolean value){
        PropertiesComponent.getInstance().setValue(key.getKey(),value.toString());
    }

    public static void delStore(StoreKeys key){
        PropertiesComponent.getInstance().unsetValue(key.getKey());
    }



}
