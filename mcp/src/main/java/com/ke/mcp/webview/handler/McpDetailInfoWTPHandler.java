package com.ke.mcp.webview.handler;

import com.alibaba.fastjson.JSON;
import com.intellij.openapi.project.Project;
import com.intellij.ui.jcef.JBCefJSQuery;
import com.ke.mcp.communication.sidecar.McpPTSHandler;
import com.ke.mcp.dto.McpConfigDTO;
import com.ke.mcp.dto.McpDetailInfoDTO;
import com.ke.mcp.dto.req.ToolListReq;
import com.ke.mcp.dto.resp.ToolListResp;
import com.ke.mcp.enums.AvailableStatusEnum;
import com.ke.mcp.enums.McpWebviewCommandEnums;
import com.ke.mcp.manager.McpConfigFileManager;
import com.ke.webview.communication.handler.wtp.BaseWTPHandler;
import com.ke.webview.dto.WebviewCallbackResponse;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class McpDetailInfoWTPHandler extends BaseWTPHandler {
	public McpDetailInfoWTPHandler(Project project) {
		super(s -> {
			try {
				Map<String, McpConfigDTO> mcpConfigDTOMap = project.getService(McpConfigFileManager.class).getMcpConfigDTOMap();
				List<McpConfigDTO> configDTOS = new ArrayList<>(mcpConfigDTOMap.values());
				configDTOS.removeIf(mcpConfigDTO -> Boolean.TRUE.equals(mcpConfigDTO.isDelete()));
				List<String> nameList = new ArrayList<>(configDTOS.size());
				configDTOS.forEach(configDTO -> nameList.add(configDTO.getName()));
				ToolListReq toolListReq = new ToolListReq(project.getLocationHash(), nameList);
				List<ToolListResp> toolList = McpPTSHandler.getInstance().getToolList(toolListReq, project.getService(McpConfigFileManager.class).getConfigAgentPort());
				List<McpDetailInfoDTO> resultList = new ArrayList<>(configDTOS.size());
				configDTOS.forEach(mcpConfigDTO -> {
					String serverName = mcpConfigDTO.getName();
					AvailableStatusEnum status = mcpConfigDTO.getStatus();
					List<String> tools = new ArrayList<>();
					toolList.forEach(toolListResp -> {
						if (toolListResp.getServerName().equals(serverName) && CollectionUtils.isNotEmpty(toolListResp.getTools())) {
							toolListResp.getTools().forEach(toolInfo -> tools.add(toolInfo.getToolName()));
						}
					});
					McpDetailInfoDTO mcpDetailInfoDTO = new McpDetailInfoDTO(serverName, status, tools);
					resultList.add(mcpDetailInfoDTO);
				});
				return new JBCefJSQuery.Response(JSON.toJSONString(
						WebviewCallbackResponse.builder()
								.success(true)
								.data(JSON.toJSONString(resultList))
								.build()));
			} catch (Exception e) {
				return new JBCefJSQuery.Response(JSON.toJSONString(
						WebviewCallbackResponse.builder()
								.success(false)
								.data(JSON.toJSONString("获取详情失败"))
								.build()));
			}
		});
	}

	@Override
	public String getCommand() {
		return McpWebviewCommandEnums.MCP_DETAIL_LIST.getCommand();
	}
}
