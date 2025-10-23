package com.ke.agentic.socket.handler;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.ke.exception.BusinessException;
import com.ke.agentic.socket.RequestEntity;
import com.ke.agentic.socket.dto.ReadFileDTO;
import com.ke.agentic.socket.dto.ReadFileResultDTO;
import com.ke.utils.FileUtil;
import com.ke.utils.JsonUtil;
import fi.iki.elonen.NanoHTTPD;

/**
 * @Author: zhangshaoxun001
 * @Date: 2025/3/10 16:11
 * @Description
 */
public class ReadFileHandler implements RequestHandler {
	@Override
	public String getPath() {
		return "/agentic/file_open";
	}

	@Override
	public NanoHTTPD.Method getMethod() {
		return NanoHTTPD.Method.POST;
	}

	@Override
	public NanoHTTPD.Response doHandle(Project project, RequestEntity requestEntity) {
		ReadFileDTO readFileDTO = JsonUtil.getData(requestEntity.getBody(), ReadFileDTO.class);
		try {
			ReadFileResultDTO fileResultDTO = ReadAction.compute(() -> {
				String fsFilePath = readFileDTO.getFsFilePath();
				VirtualFile virtualFile = FileUtil.findVirtualFile(fsFilePath);
				ReadFileResultDTO readFileResultDTO = ReadFileResultDTO.builder()
						.fsFilePath(fsFilePath)
						.startLine(readFileDTO.getStartLine())
						.endLine(readFileDTO.getEndLine())
						.build();

				if (!virtualFile.exists()) {
					readFileResultDTO.setExists(false);
					readFileResultDTO.setFileContents("");
					readFileResultDTO.setLanguage("");
					return readFileResultDTO;
				}
				readFileResultDTO.setExists(true);
				readFileResultDTO.setFileContents(FileUtil.getFileContent(fsFilePath, readFileDTO.getStartLine(), readFileDTO.getEndLine()));
				readFileResultDTO.setLanguage(FileUtil.findBestLanguage(virtualFile));
				return readFileResultDTO;
			});
			return parseResponse(NanoHTTPD.Response.Status.OK, fileResultDTO);
		} catch (BusinessException e) {
			return parseResponse(NanoHTTPD.Response.Status.OK, ReadFileResultDTO.builder()
					.fsFilePath(readFileDTO.getFsFilePath())
					.startLine(readFileDTO.getStartLine())
					.endLine(readFileDTO.getEndLine())
					.exists(false)
					.build());
		} catch (Exception e) {
			return parseResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, e.getMessage());
		}
	}
}
