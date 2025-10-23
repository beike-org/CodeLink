package com.ke.rules.webview.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.jcef.JBCefJSQuery;
import com.ke.utils.WorkspaceUtil;
import com.ke.rules.webview.RulesWebviewCommandEnums;
import com.ke.webview.dto.FileContentDTO;
import com.ke.utils.ApplicationUtil;
import com.ke.utils.EditorUtil;
import com.ke.utils.FileUtil;
import com.ke.webview.communication.handler.wtp.BaseWTPHandler;
import com.ke.webview.dto.WebviewCallbackResponse;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DefaultFileListWTPHandler extends BaseWTPHandler {


	public DefaultFileListWTPHandler(Project project) {
		super(s -> {
			try {
				List<FileContentDTO> fileList = new ArrayList<>();
				VirtualFile[] files = FileEditorManager.getInstance(project).getOpenFiles();

				// 使用 runReadAction 获取文件内容和文档
				ApplicationManager.getApplication().runReadAction(() -> {
					for (VirtualFile file : files) {
						if (file instanceof LightVirtualFile) {
							continue;
						}
						if (!file.getPath().startsWith(Objects.requireNonNull(project.getBasePath()))) {
							continue;
						}
						if (Boolean.TRUE.equals(FileUtil.filterByExtension(file.getExtension()))) {
							continue;
						}
						Icon icon = file.getFileType().getIcon();
						String iconStr = WorkspaceUtil.convertIconToBase64(icon);

						// 获取文档内容
						Document document = FileDocumentManager.getInstance().getDocument(file);
						if (StringUtils.isBlank(document.getText())) {
							continue;
						}
						int lastLineNumber = document.getLineCount() - 1;
						int lastLineStartOffset = document.getLineStartOffset(lastLineNumber);
						int lastLineLength = document.getTextLength() - lastLineStartOffset;
						// 构建文件信息对象
						fileList.add(FileContentDTO.builder()
								.icon(iconStr) // 图标转换为 Base64
								.name(file.getName())
								.path(file.getPath().replace(project.getBasePath(), ""))
								.content(document.getText()) // 获取文件内容
								.startLine(0) // 添加起始行号
								.endLine(document.getLineCount()) // 添加结束行号
								.startOffset(0) // 添加起始偏移量
								.endOffset(lastLineLength)
								.language(EditorUtil.getLanguage(ApplicationUtil.findCurrentProject()))// 添加结束偏移量
								.type("File")
								.build());
					}
				});

				// 返回结果
				return new JBCefJSQuery.Response(JSON.toJSONString(
						WebviewCallbackResponse.builder()
								.success(true)
								.data(JSON.toJSONString(fileList))
								.build()
				));
			} catch (Exception e) {
				return new JBCefJSQuery.Response(JSONObject.toJSONString(WebviewCallbackResponse.builder().reason("获取默认打开文件列表失败").success(false)));
			}
		});
	}

	@Override
	public String getCommand() {
		return RulesWebviewCommandEnums.GET_DEFAULT_FILE_LIST.getCommand();
	}

}
