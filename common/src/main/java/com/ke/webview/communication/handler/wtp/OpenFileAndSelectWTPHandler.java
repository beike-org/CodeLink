package com.ke.webview.communication.handler.wtp;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfoRt;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.ke.webview.BaseCommandEnums;
import com.ke.webview.dto.OpenAndSelectDTO;
import com.ke.utils.JsonUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.regex.Matcher;

/**
 * 根据插件传过来的信息在Editor选中相应代码块
 */
public class OpenFileAndSelectWTPHandler extends BaseWTPHandler {
	public OpenFileAndSelectWTPHandler(Project project) {
		super(s -> {
			OpenAndSelectDTO selectCodeDTO = JsonUtil.getData(s, OpenAndSelectDTO.class);
			if (selectCodeDTO == null || StringUtils.isEmpty(selectCodeDTO.getFileUri())) {
				return null;
			}

			ApplicationManager.getApplication().invokeLater(() -> {
				try {
					// 处理文件路径，确保跨平台兼容性
					String filePath = selectCodeDTO.getFileUri();
					if (SystemInfoRt.isWindows) {
						filePath = filePath.replaceAll("/", Matcher.quoteReplacement(File.separator));
					}

					// 获取文件
					VirtualFile file = LocalFileSystem.getInstance().findFileByPath(filePath);
					if (file != null && file.exists()) {
						// 打开文件
						OpenFileDescriptor descriptor = new OpenFileDescriptor(project, file);
						Editor editor = FileEditorManager.getInstance(project).openTextEditor(descriptor, true);

						if (editor != null && selectCodeDTO.getStartOffset() != null && selectCodeDTO.getEndOffset() != null) {
							// 选中代码
							SelectionModel selectionModel = editor.getSelectionModel();
							int startOffset = Math.max(0, selectCodeDTO.getStartOffset());
							int endOffset = Math.min(editor.getDocument().getTextLength(), selectCodeDTO.getEndOffset());
							selectionModel.setSelection(startOffset, endOffset);

							// 滚动到选中位置
							editor.getScrollingModel().scrollToCaret(ScrollType.CENTER);
						}
					}
				} catch (Exception e) {
					// 处理异常
				}
			});
			return null;
		});
	}

	@Override
	public String getCommand() {
		return BaseCommandEnums.OPEN_FILE_AND_SELECT.getCommand();
	}
}
