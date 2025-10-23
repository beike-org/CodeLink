package com.ke.stacktrace.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.ke.BaseAction;
import com.ke.Bundle;
import com.ke.stacktrace.RuntimeErrorAIExplanationProvider;
import com.ke.stacktrace.entity.SelectedTraceback;
import com.ke.utils.EditorUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RunTimeErrorExplainMenuActionEx extends BaseAction {

	public RunTimeErrorExplainMenuActionEx() {
		super(Bundle.get("action.console.explain"));
	}

	@Override
	protected void doAction(@NotNull AnActionEvent e) {
		Project project = e.getProject();
		if (project == null) {
			return;
		}

		@Nullable Editor editor = e.getData(PlatformDataKeys.EDITOR);
		if (editor == null) {
			return;
		}
		SelectedTraceback selectTraceback = getSelectedText(editor);
		// 执行补全
		new RuntimeErrorAIExplanationProvider(
				editor.getProject(),
				editor,
				selectTraceback).run();
	}

	public void update(@NotNull AnActionEvent event) {
		// 没选字符时不可点击
		Presentation presentation = event.getPresentation();
		Editor editor = event.getData(PlatformDataKeys.EDITOR);

		Project project = event.getProject();
		if (project == null || editor == null) {
			presentation.setEnabled(false);
		} else {
			SelectionModel selectionModel = editor.getSelectionModel();
			String selectedText = selectionModel.getSelectedText();
			presentation.setEnabled(StringUtils.isNotBlank(selectedText));
		}
	}


	public SelectedTraceback getSelectedText(@NotNull Editor editor) {
		SelectionModel selectionModel = editor.getSelectionModel();
		String selectedText = "```\n" + selectionModel.getSelectedText() + "\n```\n";
		// 如果用户什么都没选中，那么就返回null
		if (StringUtils.isBlank(selectedText)) {
			return null;
		}
		return SelectedTraceback.builder()
				.traceback(selectedText)
				.lineFrom(EditorUtil.getLineNumber(editor, selectionModel.getSelectionStart()))
				.lineTo(EditorUtil.getLineNumber(editor, selectionModel.getSelectionEnd()))
				.build();

	}
}

