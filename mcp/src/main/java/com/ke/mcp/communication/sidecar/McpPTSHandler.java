package com.ke.mcp.communication.sidecar;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.ke.agentic.SideCarAgentManager;
import com.ke.agentic.pts.PTSHandler;
import com.ke.exception.BusinessException;
import com.ke.mcp.dto.req.*;
import com.ke.mcp.dto.resp.SwitchAvailableResp;
import com.ke.mcp.dto.resp.ToolListResp;
import com.ke.utils.JsonUtil;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class McpPTSHandler extends PTSHandler {

	private static final Logger logger = Logger.getInstance(McpPTSHandler.class);

	@NotNull
	public static McpPTSHandler getInstance() {
		return ApplicationManager.getApplication().getService(McpPTSHandler.class);
	}

	@Override
	@NotNull
	public McpPTSApi getApi(Integer port) {
		try {
			if (port == 0) {
				port = SideCarAgentManager.getInstance().getAgentPort();
			}
			Integer finalPort = port;
			return (McpPTSApi) apiMap.computeIfAbsent(finalPort, k -> {
				OkHttpClient client = new OkHttpClient.Builder()
						.connectTimeout(60, TimeUnit.SECONDS)
						.readTimeout(60, TimeUnit.SECONDS)
						.build();
				Retrofit retrofit = new Retrofit.Builder()
						.baseUrl(BASE_URL_PREFIX + finalPort)
						.addConverterFactory(JacksonConverterFactory.create())
						.client(client)
						.build();

				return retrofit.create(McpPTSApi.class);
			});
		} catch (Exception e) {
			logger.error("Failed to create or get McpPTSApi instance for port: " + port, e);
			throw new RuntimeException("Failed to create or get McpPTSApi instance for port: " + port, e);
		}
	}

	public void launchMcp(@NotNull LaunchConfigReq request, @NotNull Integer port) throws RuntimeException {
		for (McpConfigReq mcpConfigReq : request.getConfigList()) {
			if (StringUtils.isBlank(mcpConfigReq.getConfig()) || "null".equals(mcpConfigReq.getConfig())) {
				Map<String, String> env = new HashMap<>();
				mcpConfigReq.setConfig(JsonUtil.toJson(env));
			} else {
				String config = mcpConfigReq.getConfig();
				try {
					@SuppressWarnings("unchecked")
					Map<String, String> configMap = JsonUtil.getData(config, Map.class);
					mcpConfigReq.setConfig(JsonUtil.toJson(configMap));
				} catch (Exception e) {
					logger.warn("Failed to parse config json: " + config, e);
					Map<String, String> env = new HashMap<>();
					mcpConfigReq.setConfig(JsonUtil.toJson(env));
				}
			}
		}
		try {
			executeCall(getApi(port).launchMcp(request));
		}catch (BusinessException ignore){

		}
	}

	@NotNull
	public List<ToolListResp> getToolList(@NotNull ToolListReq request, @NotNull Integer port) throws RuntimeException {
		return executeCall(getApi(port).getToolList(request)).getData();
	}

	@NotNull
	public SwitchAvailableResp switchAvailable(@NotNull SwitchAvailableReq request, @NotNull Integer port) throws RuntimeException {
		return executeCall(getApi(port).switchAvailable(request)).getData();
	}

	public void shutdownMcp(@NotNull ShutDownReq request, @NotNull Integer port) throws RuntimeException {
		executeCall(getApi(port).shutdownMcp(request));
	}

	public void updateTools(@NotNull UpdateConfigReq request, @NotNull Integer port) throws RuntimeException {
		executeCall(getApi(port).updateTools(request));
	}

	public void deleteMcpServers(@NotNull String projectId, @NotNull Integer port, @NotNull String serverName) throws RuntimeException {
		DeleteServerReq request = new DeleteServerReq(projectId, serverName);
		executeCall(getApi(port).deleteMcpServers(request));
	}


}
