package com.ke.mcp.dto.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ke.mcp.enums.ToolListResponseEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Firstname Lastname
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolListResp {
	@JsonProperty("server_name")
	private String serverName;
	private ToolListResponseEnum status;
	private boolean connected;
	private List<ToolInfo> tools;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ToolInfo {
		@JsonProperty("tool_name")
		private String toolName;

		private String description;
	}
}

