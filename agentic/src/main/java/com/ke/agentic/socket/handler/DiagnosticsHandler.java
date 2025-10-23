package com.ke.agentic.socket.handler;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.ke.agentic.diagnostics.DiagnosticsProcessor;
import com.ke.agentic.socket.RequestEntity;
import com.ke.agentic.socket.dto.*;
import com.ke.utils.FileUtil;
import com.ke.utils.JsonUtil;
import fi.iki.elonen.NanoHTTPD;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: zhangshaoxun001
 * @Date: 2025/3/10 16:11
 * @Description 检测warn以上的语法问题，注意需要用户至少打开过文件一次才能有结果
 */
public class DiagnosticsHandler implements RequestHandler {
	@Override
	public String getPath() {
		return "/agentic/diagnostics";
	}

	@Override
	public NanoHTTPD.Method getMethod() {
		return NanoHTTPD.Method.POST;
	}

	@Override
	public NanoHTTPD.Response doHandle(Project project, RequestEntity requestEntity) {
		DiagnosticsDTO diagnosticsDTO = JsonUtil.getData(requestEntity.getBody(), DiagnosticsDTO.class);
		Document document = ReadAction.compute(() -> FileDocumentManager.getInstance().getDocument(FileUtil.findVirtualFile(diagnosticsDTO.getFilePath())));
		assert document != null;
		List<HighlightInfo> diagnosticsInfo = ReadAction.compute(() -> DiagnosticsProcessor.getDiagnosticsInfo(diagnosticsDTO.getFilePath(), project, false, Math.max(0, diagnosticsDTO.getRange().getStartPosition().getByteOffset()), Math.min(diagnosticsDTO.getRange().getEndPosition().getByteOffset(), document.getTextLength())));
		if (CollectionUtils.isEmpty(diagnosticsInfo)) {
			return parseResponse(NanoHTTPD.Response.Status.OK, Lists.newArrayList());
		}
		List<DiagnosticsResult> diagnosticsResults = ReadAction.compute(() -> diagnosticsInfo.stream().map(highlightInfo -> {
			String message = highlightInfo.getSeverity().getDisplayName() + ":" + highlightInfo.getDescription();
			String text = highlightInfo.getText();
			int startOffset = highlightInfo.getStartOffset();
			int endOffset = highlightInfo.getEndOffset();

			int startLine = document.getLineNumber(startOffset);
			int endLine = document.getLineNumber(endOffset);

			String startCharacter = StringUtils.isEmpty(text) ? "" : text.substring(0, 1);
			String endCharacter = StringUtils.isEmpty(text) ? "" : text.substring(text.length() - 1);
			Position startPosition = Position.builder().line(startLine).character(startCharacter).byteOffset(startOffset).build();
			Position endPosition = Position.builder().line(endLine).character(endCharacter).byteOffset(endOffset).build();
			return DiagnosticsResult.builder().range(Range.builder().startPosition(startPosition).endPosition(endPosition).build()).filePath(diagnosticsDTO.getFilePath()).message(message).build();
		}).collect(Collectors.toList()));
		return parseResponse(NanoHTTPD.Response.Status.OK, DiagnosticsResultDTO.builder().diagnostics(diagnosticsResults).build());
	}
}
