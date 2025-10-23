package com.ke.mcp.dto;

import com.ke.mcp.enums.AvailableStatusEnum;
import com.ke.mcp.enums.McpTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
public class SseConfigDTO extends McpConfigDTO {
	private String url;

	public SseConfigDTO(String name, String url) {
		super(name, McpTypeEnum.SSE, AvailableStatusEnum.ENABLED, false, null);
		this.url = url;
	}

	public SseConfigDTO(String name, String url, Map<String, String> env) {
		super(name, McpTypeEnum.SSE, AvailableStatusEnum.ENABLED, false, env);
		this.url = url;
	}

	public SseConfigDTO(String name, String url, boolean delete) {
		super(name, McpTypeEnum.SSE, AvailableStatusEnum.ENABLED, delete, null);
		this.url = url;
	}

	public SseConfigDTO(String name, String url, boolean delete, Map<String, String> env) {
		super(name, McpTypeEnum.SSE, AvailableStatusEnum.ENABLED, delete, env);
		this.url = url;
	}
}
