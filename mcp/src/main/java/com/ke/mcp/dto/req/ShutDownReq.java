package com.ke.mcp.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ShutDownReq {
	@JsonProperty("project_id")
	private String projectId;
}

