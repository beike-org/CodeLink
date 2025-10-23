package com.ke.mcp.webview.handler;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.ke.mcp.enums.McpWebviewCommandEnums;
import com.ke.mcp.ui.McpConfigurationConfigurable;
import com.ke.webview.communication.handler.wtp.BaseWTPHandler;

public class AddMcpMarketWTPHandler extends BaseWTPHandler {

	public AddMcpMarketWTPHandler(Project project) {
		super(s -> {
			ApplicationManager.getApplication().invokeLater(() -> ShowSettingsUtil.getInstance().showSettingsDialog(project, McpConfigurationConfigurable.class));
			return null;
		});
	}

	@Override
	public String getCommand() {
		return McpWebviewCommandEnums.ADD_MCP_MARKET.getCommand();
	}
}
