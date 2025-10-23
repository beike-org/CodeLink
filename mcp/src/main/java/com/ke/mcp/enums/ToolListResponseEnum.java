package com.ke.mcp.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ToolListResponseEnum {

	@JsonProperty("Active")
	ACTIVE(),

	@JsonProperty("Inactive")
	INACTIVE(),

	@JsonProperty("Unknown")
	UNKNOWN();

	ToolListResponseEnum() {
	}
}
