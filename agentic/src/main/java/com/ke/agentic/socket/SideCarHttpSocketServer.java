package com.ke.agentic.socket;

import fi.iki.elonen.NanoHTTPD;

import java.io.IOException;
import java.util.Map;

public class SideCarHttpSocketServer extends NanoHTTPD {

	public SideCarHttpSocketServer(int port) throws IOException {
		super(port);
		start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
	}

	@Override
	public Response serve(IHTTPSession session) {
		String uri = session.getUri();
		Method method = session.getMethod();
		Map<String, String> headers = session.getHeaders();
		Map<String, String> params = session.getParms();
		Map<String, String> files = new java.util.HashMap<>();

		RequestEntity requestEntity = RequestEntity.builder()
				.path(uri)
				.method(method)
				.headers(headers)
				.params(params)
				.build();

		try {
			session.parseBody(files);
		} catch (IOException | ResponseException e) {
			return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Server Error");
		}

		if (method == Method.POST) {
			String postData = files.get("postData");
			requestEntity.setBody(postData);
		}

		return SideCarHttpSocketManager.getInstance().handleRequest(requestEntity);
	}
}
