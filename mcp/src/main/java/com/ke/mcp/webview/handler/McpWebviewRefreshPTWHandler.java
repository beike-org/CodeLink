package com.ke.mcp.webview.handler;

import com.intellij.openapi.project.Project;
import com.ke.mcp.enums.McpWebviewCommandEnums;
import com.ke.mcp.enums.WebviewRefreshEnum;
import com.ke.toolwindow.content.BaseH5Panel;
import com.ke.webview.communication.handler.ptw.BasePTWHandler;
import com.ke.webview.util.PTWUtil;


public class McpWebviewRefreshPTWHandler extends BasePTWHandler {

	public McpWebviewRefreshPTWHandler(BaseH5Panel h5Panel) {
		super(h5Panel);
	}

	@Override
	public String getCommand() {
		return McpWebviewCommandEnums.MCP_WEBVIEW_REFRESH.getCommand();
	}

	public static void notifyConfigChange(WebviewRefreshEnum webviewRefreshEnum, Project project) {
		PTWUtil.sendMessage(McpWebviewCommandEnums.MCP_WEBVIEW_REFRESH, webviewRefreshEnum, project);
	}
}
