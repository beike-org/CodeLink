package com.ke.webview.communication.handler.wtp;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.ke.webview.BaseCommandEnums;
import com.ke.webview.dto.InsertCodeDTO;
import com.ke.utils.JsonUtil;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/8/16 16:53
 * @Version 1.0
 * @Description
 */
public class InsertCodeWTPHandler extends BaseWTPHandler {

	public InsertCodeWTPHandler(Project project) {
		super(s -> {
			InsertCodeDTO insertCodeDTO = JsonUtil.getData(s, InsertCodeDTO.class);

			ApplicationManager.getApplication().invokeLater(() -> WriteCommandAction.runWriteCommandAction(project, () -> {
				var editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
				if (editor != null) {
					if (editor.getSelectionModel().hasSelection()) {
						editor.getDocument().replaceString(editor.getSelectionModel().getSelectionStart(), editor.getSelectionModel().getSelectionEnd(), insertCodeDTO.getCode());
						editor.getCaretModel().moveToOffset(editor.getSelectionModel().getSelectionStart() + insertCodeDTO.getCode().length());
					} else {
						editor.getDocument().insertString(editor.getCaretModel().getOffset(), insertCodeDTO.getCode());
						editor.getCaretModel().moveToOffset(editor.getCaretModel().getOffset() + insertCodeDTO.getCode().length());
					}
					editor.getScrollingModel().scrollToCaret(ScrollType.MAKE_VISIBLE);
				}
			}));
			return null;
		});
	}

	@Override
	public String getCommand() {
		return BaseCommandEnums.INSERT_CODE.getCommand();
	}
}
