package com.ke.mcp.dto;

import com.ke.mcp.enums.AvailableStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class McpDetailInfoDTO {

	private String serverName;

	private AvailableStatusEnum status;

	private List<String> tools;

}
