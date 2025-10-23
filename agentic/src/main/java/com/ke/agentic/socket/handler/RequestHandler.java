package com.ke.agentic.socket.handler;


import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.ke.agentic.socket.RequestEntity;
import com.ke.agentic.socket.dto.SocketExceptionDTO;
import fi.iki.elonen.NanoHTTPD;

/**
 * @Author: zhangshaoxun001
 * @Date: 2025/3/10 11:44
 * @Description
 */
public interface RequestHandler {

	Logger LOG = Logger.getInstance(RequestHandler.class);

	String getPath();

	NanoHTTPD.Method getMethod();

	default NanoHTTPD.Response handleRequest(Project project, RequestEntity requestEntity) {
		try {
			return doHandle(project, requestEntity);
		} catch (Exception e) {
			LOG.warn("RequestHandler error: ", e);
			return parseResponse(NanoHTTPD.Response.Status.BAD_REQUEST, SocketExceptionDTO.builder()
					.exception(e.getMessage())
					.build());
		}
	}

	NanoHTTPD.Response doHandle(Project project, RequestEntity requestEntity);


	default NanoHTTPD.Response parseResponse(NanoHTTPD.Response.Status status, Object body) {
		return NanoHTTPD.newFixedLengthResponse(status, "application/json", JSONObject.toJSONString(body, SerializerFeature.WriteNullStringAsEmpty));
	}

}
