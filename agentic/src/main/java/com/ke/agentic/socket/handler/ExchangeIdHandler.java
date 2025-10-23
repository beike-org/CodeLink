package com.ke.agentic.socket.handler;

import com.intellij.openapi.project.Project;
import com.ke.agentic.socket.RequestEntity;
import com.ke.agentic.socket.SideCarSocketCacheManager;
import com.ke.agentic.socket.dto.ExchangeIdDTO;
import com.ke.agentic.socket.dto.ExchangeIdResultDTO;
import com.ke.utils.JsonUtil;
import fi.iki.elonen.NanoHTTPD;

/**
 * @Author: zhangshaoxun001
 * @Date: 2025/3/10 16:11
 * @Description
 */
public class ExchangeIdHandler implements RequestHandler {
	@Override
	public String getPath() {
		return "/agentic/new_exchange";
	}

	@Override
	public NanoHTTPD.Method getMethod() {
		return NanoHTTPD.Method.POST;
	}

	@Override
	public NanoHTTPD.Response doHandle(Project project, RequestEntity requestEntity) {
		ExchangeIdDTO exchangeIdDTO = JsonUtil.getData(requestEntity.getBody(), ExchangeIdDTO.class);
		return parseResponse(NanoHTTPD.Response.Status.OK, ExchangeIdResultDTO.builder().exchangeId("response" + project.getService(SideCarSocketCacheManager.class).getExchangeId(exchangeIdDTO.getSessionId())).build());


	}
}
