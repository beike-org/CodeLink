package com.ke.webview.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.ke.toolwindow.content.WebviewPanel;
import com.ke.toolwindow.content.BaseH5Panel;
import com.ke.webview.WebViewManager;
import com.ke.webview.WebviewCommand;
import com.ke.webview.topic.PTWCommandNotifier;

import java.util.List;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/8/29 10:47
 * @Version 1.0
 * @Description
 */
public class PTWUtil {

	public static void sendMessage(WebviewCommand webviewCommand, Object data) {
		ApplicationManager.getApplication().getMessageBus().syncPublisher(PTWCommandNotifier.PTW_COMMAND_TOPIC).sendCommand(webviewCommand, data, null);
	}

	public static void sendMessage(WebviewCommand webviewCommand, Object data, Project project) {
		sendMessage(webviewCommand, data, project, (List<BaseH5Panel>) null);
	}

	public static void sendMessage(WebviewCommand webviewCommand, Object data, Project project, WebviewPanel receiver) {
		BaseH5Panel h5Panel = project.getService(WebViewManager.class).getH5Panel(receiver.getName());
		List<BaseH5Panel> receivers = h5Panel == null ? null : List.of(h5Panel);
		project.getMessageBus().syncPublisher(PTWCommandNotifier.PTW_COMMAND_TOPIC).sendCommand(webviewCommand, data, receivers);
	}

	public static void sendMessage(WebviewCommand webviewCommand, Object data, Project project, List<BaseH5Panel> receivers) {
		project.getMessageBus().syncPublisher(PTWCommandNotifier.PTW_COMMAND_TOPIC).sendCommand(webviewCommand, data, receivers);
	}
}
