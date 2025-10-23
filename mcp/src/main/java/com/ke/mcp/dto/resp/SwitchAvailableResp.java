package com.ke.mcp.dto.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ke.mcp.enums.AvailableRespEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SwitchAvailableResp {

	@JsonProperty("server_name")
	private String serverName;

	private AvailableRespEnum status;
}

