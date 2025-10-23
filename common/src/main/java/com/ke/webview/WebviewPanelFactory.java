package com.ke.webview;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.Project;
import com.ke.toolwindow.content.BaseH5Panel;

import java.util.Map;

/**
 * @Author: zhangshaoxun001
 * @Date: 2024/1/2 16:16
 * @Version 1.0
 * @Description 注册webview页面到WebviewManager
 */
public interface WebviewPanelFactory {

    ExtensionPointName<WebviewPanelFactory> EP_NAME = ExtensionPointName.create("com.ke.codelink.webviewPanelFactory");


    Map<String, BaseH5Panel> getH5Panels(Project project);

}
