package com.ke.agentic.socket;

import fi.iki.elonen.NanoHTTPD;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @Author: zhangshaoxun001
 * @Date: 2025/3/10 14:50
 * @Description
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestEntity {

	private String path;

	private NanoHTTPD.Method method;

	private Map<String, String> headers;

	private Map<String, String> params;

	private String body;

}
