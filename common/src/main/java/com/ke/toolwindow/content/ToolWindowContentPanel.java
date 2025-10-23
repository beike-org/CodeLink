package com.ke.toolwindow.content;

import com.intellij.openapi.project.Project;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/8/10 17:51
 * @Version 1.0
 * @Description
 */
public interface ToolWindowContentPanel {

    /**
     * 点击刷新按钮触发
     */
    void refreshContent(Project project);

}
