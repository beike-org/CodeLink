package com.ke.toolwindow.content;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/8/23 14:54
 * @Version 1.0
 * @Description
 */
@AllArgsConstructor
@Getter
public enum CommonWebviewPanelEnums implements WebviewPanel {
    KE_COPILOT("keCopilot"),
   ;

    private final String name;
}
