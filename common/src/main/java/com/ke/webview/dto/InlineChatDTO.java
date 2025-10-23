package com.ke.webview.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/8/19 16:34
 * @Version 1.0
 * @Description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InlineChatDTO {

    private String context;

    private String question;

    private String command;

    private String language;

    public static String replaceTableKey(String original) {
        return original.replace("\t", " ");
    }

}
