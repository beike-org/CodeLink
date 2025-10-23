package com.ke.mcp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 解析mcp配置后的数据对象
 */
@Data
@Builder
@AllArgsConstructor
public class McpParsedConfig {
	private List<CommandConfigDTO> commandMcp;
	private List<SseConfigDTO> sseMcp;
}
