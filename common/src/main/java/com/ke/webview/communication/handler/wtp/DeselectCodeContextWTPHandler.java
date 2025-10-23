package com.ke.webview.communication.handler.wtp;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerConsoleView;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunContentManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.ke.webview.util.PTWUtil;
import com.ke.webview.BaseCommandEnums;
import com.ke.webview.dto.SelectCodeContext;
import com.ke.utils.EditorUtil;

import java.util.Objects;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/8/16 16:53
 * @Version 1.0
 * @Description
 */
public class DeselectCodeContextWTPHandler extends BaseWTPHandler {

	public DeselectCodeContextWTPHandler(Project project) {
		super(s -> {
			ApplicationManager.getApplication().invokeLater(() -> {
				try {

					SelectionModel selectionModel = EditorUtil.getSelectionModel(project);
					if (selectionModel.hasSelection()) {
						WriteCommandAction.runWriteCommandAction(project, () -> selectionModel.removeSelection());
					}

					RunContentDescriptor currentContentDescriptor = RunContentManager.getInstance(project).getSelectedContent();
					if (Objects.nonNull(currentContentDescriptor) && currentContentDescriptor.getExecutionConsole() instanceof ConsoleView) {
						ExecutionConsole executionConsole = currentContentDescriptor.getExecutionConsole();
						Editor consoleEditor = null;
						if (executionConsole instanceof ConsoleViewImpl) {
							consoleEditor = ((ConsoleViewImpl) executionConsole).getEditor();
						} else if (executionConsole instanceof SMTRunnerConsoleView) {
							consoleEditor = ((ConsoleViewImpl) ((SMTRunnerConsoleView) executionConsole).getConsole()).getEditor();
						}
						if (Objects.nonNull(consoleEditor) && consoleEditor.getSelectionModel().hasSelection()) {
							Editor finalConsoleEditor = consoleEditor;
							WriteCommandAction.runWriteCommandAction(project, () -> finalConsoleEditor.getSelectionModel().removeSelection());
						}
					}

				} catch (Exception ignore) {

				}
				PTWUtil.sendMessage(BaseCommandEnums.SELECT_CODE_CONTEXT, SelectCodeContext.builder().code("").build(), project);

			});
			return null;
		});
	}

	@Override
	public String getCommand() {
		return BaseCommandEnums.DE_SELECT_CODE_CONTEXT.getCommand();
	}
}
