package com.ke.mcp.dto;

import com.ke.mcp.enums.AvailableStatusEnum;
import com.ke.mcp.enums.McpTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class McpConfigDTO {
	private String name;
	private McpTypeEnum type;
	private AvailableStatusEnum status;
	private boolean delete;
	private Map<String, String> env;
}
