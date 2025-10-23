package com.ke.mcp.enums;

import lombok.Getter;

@Getter
public enum PTSRequestEnum {
	LAUNCH_CONFIG("launchConfig", "/launch", "POST"),
	REMOTE_CONFIG("remoteMcpConfig", "/fetch/remote/list", "POST"),
	TOOL_LIST("toolList", "/tools/list", "POST"),
	SWITCH_ENABLE_STATUS("shiftEnableStatus", "/available/switch", "GET"),
	SHUT_DOWN("shutdown", "/shutdown", "POST"),
	DELETE_SERVER("delete", "/delete", "POST"),
	UPDATE_MCP_CONFIG("updateMcpConfig", "/tools/update", "POST");

	private final String type;
	private final String path;
	private final String method;

	PTSRequestEnum(String type, String path, String method) {
		this.type = type;
		this.path = path;
		this.method = method;
	}
}
