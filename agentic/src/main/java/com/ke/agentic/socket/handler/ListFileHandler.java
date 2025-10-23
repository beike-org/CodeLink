package com.ke.agentic.socket.handler;

import com.intellij.openapi.project.Project;
import com.ke.agentic.socket.RequestEntity;
import com.ke.agentic.socket.dto.ListFileDTO;
import com.ke.utils.FileUtil;
import com.ke.utils.JsonUtil;
import fi.iki.elonen.NanoHTTPD;

/**
 * @Author: zhangshaoxun001
 * @Date: 2025/3/10 16:11
 * @Description
 */
public class ListFileHandler implements RequestHandler {
	@Override
	public String getPath() {
		return "/agentic/list_files";
	}

	@Override
	public NanoHTTPD.Method getMethod() {
		return NanoHTTPD.Method.POST;
	}

	@Override
	public NanoHTTPD.Response doHandle(Project project, RequestEntity requestEntity) {
		ListFileDTO listFileDTO = JsonUtil.getData(requestEntity.getBody(), ListFileDTO.class);
		return parseResponse(NanoHTTPD.Response.Status.OK, FileUtil.bfsDirectory(listFileDTO.getDirectoryPath(), listFileDTO.getRecursive(), listFileDTO.getLimit()));
	}
}
