package com.ke.mcp.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class McpConfigReq {
	private String name;
	private String type;
	private CommandReq stdio;
	private String url;
	private boolean available;
	private String config;

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class CommandReq {
		private String command;
		private List<String> args;
		private Map<String, String> env;
	}
}


