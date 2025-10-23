package com.ke.agentic.socket.handler;

import com.intellij.openapi.project.Project;
import com.ke.agentic.reg.RegUtil;
import com.ke.agentic.socket.RequestEntity;
import com.ke.agentic.socket.dto.GetRipGrepResultDTO;
import fi.iki.elonen.NanoHTTPD;

/**
 * @Author: zhangshaoxun001
 * @Date: 2025/3/10 16:11
 * @Description
 */
public class GetRipGrepHandler implements RequestHandler {
	@Override
	public String getPath() {
		return "/agentic/rip_grep_path";
	}

	@Override
	public NanoHTTPD.Method getMethod() {
		return NanoHTTPD.Method.POST;
	}

	@Override
	public NanoHTTPD.Response doHandle(Project project, RequestEntity requestEntity) {
		return parseResponse(NanoHTTPD.Response.Status.OK, GetRipGrepResultDTO.builder().ripGrepPath(RegUtil.getBinaryPath()).build());
	}
}
