package com.ke.webview.communication.handler.wtp;

import com.intellij.openapi.project.Project;
import com.ke.toolwindow.content.BaseH5Panel;
import com.ke.webview.BaseCommandEnums;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/8/16 16:53
 * @Version 1.0
 * @Description
 */
public class RefreshWTPHandler extends BaseWTPHandler {

    public RefreshWTPHandler(BaseH5Panel baseH5Panel, Project project) {
        super(s -> {
            baseH5Panel.refreshContent(project);
            return null;
        });
    }

    @Override
    public String getCommand() {
        return BaseCommandEnums.REFRESH.getCommand();
    }
}
