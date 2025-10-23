package com.ke.mcp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class McpAvailableParsedConfig {

	private Map<String, Boolean> mcpAcailableMap;
}
