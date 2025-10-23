package com.ke.agentic.pts.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class HealthCheckResponse {
	private Boolean done;
	private String ide;
	@JsonProperty("sidecar_version")
	private String sidecarVersion;
}