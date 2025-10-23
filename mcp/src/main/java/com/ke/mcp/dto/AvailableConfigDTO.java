package com.ke.mcp.dto;


import com.ke.mcp.enums.AvailableStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AvailableConfigDTO {
	private String name;
	private AvailableStatusEnum status;
	private boolean delete;
}
