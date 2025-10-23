package com.ke.mcp.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public enum McpTypeEnum {

	@JsonProperty("command")
	COMMAND("command"),

	@JsonProperty("sse")
	SSE("sse");

	private final String type;

	McpTypeEnum(String type) {
		this.type = type;
	}

	public static McpTypeEnum[] visibleValues() {
		return new McpTypeEnum[]{COMMAND, SSE};
	}
}
