package com.ke.mcp.listener;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import com.ke.mcp.communication.sidecar.McpPTSHandler;
import com.ke.mcp.dto.req.ShutDownReq;
import com.ke.mcp.manager.McpConfigFileManager;
import org.jetbrains.annotations.NotNull;

public class McpProjectCloseListener implements ProjectManagerListener {

	@Override
	public void projectClosing(@NotNull Project project) {
		ShutDownReq req = new ShutDownReq(project.getLocationHash());
		McpPTSHandler.getInstance().shutdownMcp(req, project.getService(McpConfigFileManager.class).getConfigAgentPort());
	}
}

