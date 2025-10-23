package com.ke.webview.communication.handler;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.Project;
import com.ke.toolwindow.content.BaseH5Panel;
import com.ke.webview.communication.handler.ptw.BasePTWHandler;
import com.ke.webview.communication.handler.wtp.BaseWTPHandler;

import java.util.Map;

/**
 * @Author: zhangshaoxun001
 * @Date: 2024/1/2 16:16
 * @Version 1.0
 * @Description 根据不同的webview页面注册不同的消息处理器
 */
public interface KeCopilotPanelHandler {

	ExtensionPointName<KeCopilotPanelHandler> EP_NAME = ExtensionPointName.create("com.ke.codelink.keCopilotPanelHandler");

	/**
	 * 获取PTWHandler
	 */
	Map<String, BasePTWHandler> getPTWHandler(BaseH5Panel baseH5Panel, Project project);

	/**
	 * 获取WTPHandler
	 */
	Map<String, BaseWTPHandler> getWTPHandler(BaseH5Panel baseH5Panel, Project project);

}
