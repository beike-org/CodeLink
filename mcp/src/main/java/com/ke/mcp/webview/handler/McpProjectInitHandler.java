package com.ke.mcp.webview.handler;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.ke.agentic.SideCarAgentManager;
import com.ke.StartupActivityHandler;
import com.ke.mcp.communication.sidecar.McpPTSHandler;
import com.ke.mcp.dto.McpConfigDTO;
import com.ke.mcp.dto.req.LaunchConfigReq;
import com.ke.mcp.manager.McpConfigFileManager;
import com.ke.agentic.topic.SideCarNotifier;

import java.util.ArrayList;

public class McpProjectInitHandler implements StartupActivityHandler {
	@Override
	public void init(Project project) {
		ApplicationManager.getApplication().executeOnPooledThread(() -> {
			project.getMessageBus().connect().subscribe(SideCarNotifier.SIDE_CAR_NOTIFIER_TOPIC, new SideCarNotifier() {
				@Override
				public void startSocket(Integer socketPort) {
				}

				/**
				 * Agent通过健康检查
				 */
				@Override
				public void agentReady() {
					startUpActivity(project);
				}

			});

			if (isAgentReady()) {
				startUpActivity(project);
			}
		});
	}

	private void startUpActivity(Project project) {
		McpConfigFileManager service = project.getService(McpConfigFileManager.class);
		service.setConfigAgentPort(SideCarAgentManager.getInstance().getAgentPort());
		service.setConfigSocketPort(SideCarAgentManager.getInstance().getSocketPort());
		Integer configAgentPort = SideCarAgentManager.getInstance().getAgentPort();
		ArrayList<McpConfigDTO> startUpConfig = new ArrayList<>(service.getMcpConfigDTOMap().values());
		startUpConfig.removeIf(mcpConfigDTO -> Boolean.TRUE.equals(mcpConfigDTO.isDelete()));
		LaunchConfigReq launchConfigReq = LaunchConfigReq.convertDTO(project.getLocationHash(), startUpConfig);
		McpPTSHandler.getInstance().launchMcp(launchConfigReq, configAgentPort);
	}

	private boolean isAgentReady() {
		return SideCarAgentManager.getInstance().isAgentReady();
	}
}
