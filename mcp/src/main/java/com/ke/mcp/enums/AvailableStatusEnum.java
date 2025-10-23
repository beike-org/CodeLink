package com.ke.mcp.enums;

import lombok.Getter;

@Getter
public enum AvailableStatusEnum {


	ENABLED("enabled", "已启用"),

	DISABLED("disabled", "已禁用");

	private final String status;
	private final String desc;

	AvailableStatusEnum(String status, String desc) {
		this.status = status;
		this.desc = desc;
	}

	public static AvailableStatusEnum fromString(String status) {
		if (status != null) {
			for (AvailableStatusEnum value : AvailableStatusEnum.values()) {
				if (value.status.equalsIgnoreCase(status)) {
					return value;
				}
			}
		}
		throw new IllegalArgumentException("Unknown status: " + status);
	}
}
