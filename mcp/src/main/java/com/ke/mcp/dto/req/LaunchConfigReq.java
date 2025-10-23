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

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Data
@Builder
@AllArgsConstructor
public class LaunchConfigReq {

	@JsonProperty("project_id")
	private String projectId;
	@JsonProperty("config_list")
	private List<McpConfigReq> configList;


	public static LaunchConfigReq convertDTO(String projectId, McpConfigDTO mcpConfigDTO) {
		if (mcpConfigDTO == null) {
			return LaunchConfigReq.builder()
					.projectId(projectId)
					.configList(Collections.emptyList())
					.build();
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

		return LaunchConfigReq.builder()
				.projectId(projectId)
				.configList(Collections.singletonList(configBuilder.build()))
				.build();
	}

	/**
	 * 将同一项目下的多条 McpConfigDTO 转换为 LaunchConfigReq。
	 *
	 * @param projectId     项目 ID
	 * @param mcpConfigDTOs 配置 DTO 列表
	 * @return 构造好的 LaunchConfigReq
	 */
	public static LaunchConfigReq convertDTO(String projectId, List<McpConfigDTO> mcpConfigDTOs) {
		if (mcpConfigDTOs == null || mcpConfigDTOs.isEmpty()) {
			// 业务可自行决定抛异常还是返回空对象
			return LaunchConfigReq.builder()
					.projectId(projectId)
					.configList(Collections.emptyList())
					.build();
		}

		List<McpConfigReq> configReqList = mcpConfigDTOs.stream()
				.map(dto -> {
					// 公共字段先构建
					McpConfigReq.McpConfigReqBuilder builder = McpConfigReq.builder()
							.name(dto.getName())
							.type(dto.getType().getType())
							.available(dto.getStatus() == AvailableStatusEnum.ENABLED);

					// 分支字段
					if (dto instanceof CommandConfigDTO commandDTO) {
						McpConfigReq.CommandReq stdio = new McpConfigReq.CommandReq(
								commandDTO.getCommand(),
								commandDTO.getArgs(),
								commandDTO.getEnv()
						);
						builder.stdio(stdio).url(null);
					} else if (dto instanceof SseConfigDTO sseDTO) {
						builder.stdio(null).url(sseDTO.getUrl()).config(sseDTO.getUrl()).config(Objects.isNull(dto.getEnv()) ? null : JsonUtil.toJson(dto.getEnv()));
					} else {
						// 若将来出现新的实现类，可在此处扩展
						throw new IllegalArgumentException("Unsupported McpConfigDTO type: " + dto.getClass());
					}

					return builder.build();
				})
				.toList();

		return LaunchConfigReq.builder()
				.projectId(projectId)
				.configList(configReqList)
				.build();
	}
}
