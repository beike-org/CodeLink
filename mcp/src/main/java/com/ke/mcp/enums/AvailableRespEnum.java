package com.ke.mcp.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public enum AvailableRespEnum {

	@JsonProperty("Active")
	ACTIVE("Active", AvailableStatusEnum.ENABLED),

	@JsonProperty("Inactive")
	INACTIVE("Inactive", AvailableStatusEnum.DISABLED);

	private final String type;
	private final AvailableStatusEnum availableStatusEnum;

	AvailableRespEnum(String type, AvailableStatusEnum availableStatusEnum) {
		this.availableStatusEnum = availableStatusEnum;
		this.type = type;
	}
}
