package com.ke.notepad.webview.handler;

import com.alibaba.fastjson.JSON;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.jcef.JBCefJSQuery;
import com.ke.notepad.webview.NotepadWebviewCommandEnums;
import com.ke.utils.FileUtil;
import com.ke.webview.communication.handler.wtp.BaseWTPHandler;
import com.ke.notepad.webview.dto.NotepadFileListDTO;
import com.ke.webview.dto.WebviewCallbackResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NotepadFileListWTPHandler extends BaseWTPHandler {
	private final static Logger LOG = Logger.getInstance(NotepadFileListWTPHandler.class);

	public NotepadFileListWTPHandler(Project project) {
		super(s -> {
			List<NotepadFileListDTO> result = new ArrayList<>();
			// 获取记事本目录
			Path notepadDir = Path.of(Objects.requireNonNull(project.getBasePath()), ".idea", "notepad");
			try {
				// 确保目录存在
				Files.createDirectories(notepadDir);
				// 获取所有.md文件
				Files.list(notepadDir)
						.filter(path -> path.toString().endsWith(".md"))
						.forEach(path -> {
							try {
								NotepadFileListDTO dto = new NotepadFileListDTO();
								dto.setFileName(path.getFileName().toString());
								dto.setFileContent(FileUtil.getFileContent(path.toAbsolutePath().toString()));
								result.add(dto);
							} catch (IOException e) {
								LOG.warn(e.getMessage());
							}
						});
			} catch (IOException e) {
				LOG.warn(e.getMessage());
			}
			return new JBCefJSQuery.Response(JSON.toJSONString(
					WebviewCallbackResponse.builder()
							.success(true)
							.data(JSON.toJSONString(result))
							.build()));
		});
	}

	@Override
	public String getCommand() {
		return NotepadWebviewCommandEnums.NOTEPAD_FILE_LIST.getCommand();
	}
}