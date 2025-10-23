package com.ke.agentic.pts.resp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SideCarResp<T> {
	private String code;
	private String error;
	private String errno;
	private String message;
	private T data;

	public boolean isSuccess() {
		return "200".equals(code) || "0".equals(code) || "0".equals(errno);
	}

	public String getMessage() {
		return message != null ? message : error;
	}
}
