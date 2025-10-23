package com.ke.mcp.dto;

import com.ke.mcp.enums.AvailableStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class McpStatusSwitchDTO {

	private String serverName;

	private AvailableStatusEnum status;
}
