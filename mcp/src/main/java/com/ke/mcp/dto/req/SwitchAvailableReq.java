package com.ke.mcp.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SwitchAvailableReq {
	@JsonProperty("project_id")
	private String projectId;

	@JsonProperty("serve_name")
	private String serveName;

	private Boolean disabled;
}

