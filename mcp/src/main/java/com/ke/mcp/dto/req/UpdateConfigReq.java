package com.ke.mcp.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ke.mcp.dto.CommandConfigDTO;
import com.ke.mcp.dto.McpConfigDTO;
import com.ke.mcp.dto.SseConfigDTO;
import com.ke.mcp.enums.AvailableStatusEnum;
import com.ke.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateConfigReq {

	@JsonProperty("project_id")
	private String projectId;
	private McpConfigReq config;

	/**
	 * 将 McpConfigDTO 转为 UpdateConfigReq
	 */
	public static UpdateConfigReq convertDTO(String projectId, McpConfigDTO mcpConfigDTO) {
		if (mcpConfigDTO == null) {
			throw new IllegalArgumentException("McpConfigDTO cannot be null");
		}

		McpConfigReq.McpConfigReqBuilder configBuilder = McpConfigReq.builder()
				.name(mcpConfigDTO.getName())
				.type(mcpConfigDTO.getType().getType())
				.available(mcpConfigDTO.getStatus() == AvailableStatusEnum.ENABLED);

		if (mcpConfigDTO instanceof CommandConfigDTO commandDTO) {
			McpConfigReq.CommandReq stdio = new McpConfigReq.CommandReq(
					commandDTO.getCommand(),
					commandDTO.getArgs(),
					commandDTO.getEnv()
			);
			configBuilder.stdio(stdio).url(null);
		} else if (mcpConfigDTO instanceof SseConfigDTO sseDTO) {
			configBuilder.stdio(null).url(sseDTO.getUrl()).config(Objects.isNull(mcpConfigDTO.getEnv()) ? null : JsonUtil.toJson(mcpConfigDTO.getEnv()));
		} else {
			throw new IllegalArgumentException("Unsupported McpConfigDTO type: " + mcpConfigDTO.getClass());
		}

		return UpdateConfigReq.builder()
				.projectId(projectId)
				.config(configBuilder.build())
				.build();
	}

}

