package com.ke.webview.communication.handler.wtp;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.jcef.JBCefJSQuery;
import com.ke.utils.ApplicationUtil;
import com.ke.utils.EditorUtil;
import com.ke.utils.FileUtil;
import com.ke.utils.WorkspaceUtil;
import com.ke.webview.BaseCommandEnums;
import com.ke.webview.dto.FileContentDTO;
import com.ke.webview.dto.WebviewCallbackResponse;
import org.apache.commons.lang3.StringUtils;

public class GetFileContentWTPHandler extends BaseWTPHandler {

	public GetFileContentWTPHandler(Project project) {
		super(s -> {
			try {
				// 获取当前聚焦的文件
				VirtualFile currentFile = FileEditorManager.getInstance(project).getSelectedFiles()[0];
				FileContentDTO fileContentDTO = new FileContentDTO();

				// 使用 runReadAction 获取文件内容和文档
				ApplicationManager.getApplication().runReadAction((Computable<FileContentDTO>) () -> {
					if (currentFile != null && !Boolean.TRUE.equals(FileUtil.filterByExtension(currentFile.getExtension()))) {
						// 获取文档内容
						Document document = FileDocumentManager.getInstance().getDocument(currentFile);
						if (document != null && StringUtils.isNotBlank(document.getText())) {
							int lastLineNumber = document.getLineCount() - 1;
							int lastLineStartOffset = document.getLineStartOffset(lastLineNumber);
							int lastLineLength = document.getTextLength() - lastLineStartOffset;

							// 获取文件图标
							String iconStr = WorkspaceUtil.convertIconToBase64(currentFile.getFileType().getIcon());

							fileContentDTO.setIcon(iconStr);
							fileContentDTO.setContent(document.getText());
							fileContentDTO.setName(currentFile.getName());
							fileContentDTO.setPath(currentFile.getPath().replace(project.getBasePath(), ""));
							fileContentDTO.setStartLine(0);
							fileContentDTO.setEndLine(document.getLineCount());
							fileContentDTO.setStartOffset(0);
							fileContentDTO.setEndOffset(lastLineLength);
							fileContentDTO.setLanguage(EditorUtil.getLanguage(ApplicationUtil.findCurrentProject()));
							fileContentDTO.setType("File");
							return fileContentDTO;
						}
					}
					return fileContentDTO;
				});

				return new JBCefJSQuery.Response(JSON.toJSONString(
						WebviewCallbackResponse.builder()
								.success(true)
								.data(JSON.toJSONString(fileContentDTO))
								.build()));

			} catch (Exception e) {
				return new JBCefJSQuery.Response(JSONObject.toJSONString(
						WebviewCallbackResponse.builder()
								.success(false)
								.reason("获取文件内容失败：" + e.getMessage())
								.build()));
			}
		});
	}

	@Override
	public String getCommand() {
		return BaseCommandEnums.GET_FILE_CONTENT.getCommand();
	}
}

