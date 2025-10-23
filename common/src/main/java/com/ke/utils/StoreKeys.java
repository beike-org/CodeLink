package com.ke.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/6/8 11:06
 * @Version 1.0
 * @Description
 */
@AllArgsConstructor
@Getter
public enum StoreKeys {

    /**
     * 是否更改过agentic diff展示方式
     */
    AGENTIC_DIFF_CHANGED("CodeLink.Agentic.Diff.Changed"),
    ;

    private String key;
}
