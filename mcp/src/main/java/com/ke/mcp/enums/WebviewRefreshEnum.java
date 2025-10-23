package com.ke.mcp.enums;

import lombok.Getter;

@Getter
public enum WebviewRefreshEnum {

	AVAILABLE_STATUS("available_status"),

	SERVER_LIST("server_list");


	private final String type;

	WebviewRefreshEnum(String type) {
		this.type = type;
	}

}
