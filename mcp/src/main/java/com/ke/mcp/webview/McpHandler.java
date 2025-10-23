package com.ke.mcp.webview;

import com.intellij.openapi.project.Project;
import com.ke.mcp.enums.McpWebviewCommandEnums;
import com.ke.mcp.webview.handler.AddMcpMarketWTPHandler;
import com.ke.mcp.webview.handler.McpDetailInfoWTPHandler;
import com.ke.mcp.webview.handler.McpStatusSwitchWTPHandler;
import com.ke.mcp.webview.handler.McpWebviewRefreshPTWHandler;
import com.ke.toolwindow.content.BaseH5Panel;
import com.ke.webview.communication.handler.KeCopilotPanelHandler;
import com.ke.webview.communication.handler.ptw.BasePTWHandler;
import com.ke.webview.communication.handler.wtp.BaseWTPHandler;

import java.util.Map;

public class McpHandler implements KeCopilotPanelHandler {
	@Override
	public Map<String, BasePTWHandler> getPTWHandler(BaseH5Panel baseH5Panel, Project project) {
		return Map.of(
				McpWebviewCommandEnums.MCP_WEBVIEW_REFRESH.getCommand(), new McpWebviewRefreshPTWHandler(baseH5Panel)
		);
	}

	@Override
	public Map<String, BaseWTPHandler> getWTPHandler(BaseH5Panel baseH5Panel, Project project) {
		return Map.of(
				McpWebviewCommandEnums.MCP_STATUS_SWITCH.getCommand(), new McpStatusSwitchWTPHandler(project),
				McpWebviewCommandEnums.MCP_DETAIL_LIST.getCommand(), new McpDetailInfoWTPHandler(project),
				McpWebviewCommandEnums.ADD_MCP_MARKET.getCommand(), new AddMcpMarketWTPHandler(project)
		);
	}
}
