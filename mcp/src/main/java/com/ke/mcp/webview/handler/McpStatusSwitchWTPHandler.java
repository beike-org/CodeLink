package com.ke.mcp.webview.handler;

import com.alibaba.fastjson.JSON;
import com.intellij.openapi.project.Project;
import com.intellij.ui.jcef.JBCefJSQuery;
import com.ke.mcp.communication.sidecar.McpPTSHandler;
import com.ke.mcp.dto.McpStatusSwitchDTO;
import com.ke.mcp.dto.req.LaunchConfigReq;
import com.ke.mcp.dto.req.SwitchAvailableReq;
import com.ke.mcp.dto.resp.SwitchAvailableResp;
import com.ke.mcp.enums.AvailableStatusEnum;
import com.ke.mcp.enums.McpWebviewCommandEnums;
import com.ke.mcp.listener.McpConfigurationUpdateListener;
import com.ke.mcp.manager.McpConfigFileManager;
import com.ke.utils.JsonUtil;
import com.ke.webview.communication.handler.wtp.BaseWTPHandler;
import com.ke.webview.dto.WebviewCallbackResponse;

public class McpStatusSwitchWTPHandler extends BaseWTPHandler {

	public McpStatusSwitchWTPHandler(Project project) {
		super(s -> {
			try {
				McpStatusSwitchDTO data = JsonUtil.getData(s, McpStatusSwitchDTO.class);
				if (data == null) {
					return null;
				}

				// 获取当前状态的反状态（要切换到的目标状态）
				boolean wantEnable = AvailableStatusEnum.ENABLED.equals(data.getStatus());

				// 调用PTS切换状态
				SwitchAvailableResp resp = McpPTSHandler.getInstance().switchAvailable(
						new SwitchAvailableReq(project.getLocationHash(), data.getServerName(), !wantEnable),
						project.getService(McpConfigFileManager.class).getConfigAgentPort()
				);

				// 如果是要启用，需要调用launchMcp
				if (Boolean.TRUE.equals(wantEnable)) {
					McpPTSHandler.getInstance().launchMcp(
							LaunchConfigReq.convertDTO(
									project.getLocationHash(),
									project.getService(McpConfigFileManager.class).getMcpConfigDTOMap().get(data.getServerName())
							),
							project.getService(McpConfigFileManager.class).getConfigAgentPort()
					);
				}

				// 更新配置文件中的状态
				project.getService(McpConfigFileManager.class).switchConfig(
						data.getServerName(),
						resp.getStatus().getAvailableStatusEnum()
				);

				// 通知配置更新，如果McpConfigurationComponent是打开的，会自动刷新面板
				project.getMessageBus().syncPublisher(McpConfigurationUpdateListener.TOPIC).onConfigurationChanged();

				return new JBCefJSQuery.Response(JSON.toJSONString(
						WebviewCallbackResponse.builder()
								.success(true)
								.data(JsonUtil.toJson("okay"))
								.build()));
			} catch (Exception e) {
				return new JBCefJSQuery.Response(JSON.toJSONString(
						WebviewCallbackResponse.builder()
								.success(false)
								.data(JsonUtil.toJson("error:" + e.getMessage()))
								.build()));
			}
		});
	}

	@Override
	public String getCommand() {
		return McpWebviewCommandEnums.MCP_STATUS_SWITCH.getCommand();
	}
}
