package com.ke.editor.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.ke.Bundle;
import com.ke.editor.swing.CustomPromptDialog;
import com.ke.utils.EditorUtil;
import com.ke.webview.dto.InlineChatDTO;

import javax.swing.*;

public class CustomPromptEditorAction extends BaseEditorAction {

	public CustomPromptEditorAction() {
		super(Bundle.get("action.editor.custom.prompt"), Bundle.get("action.editor.custom.prompt"), AllIcons.Actions.Run_anything);
	}

	private static String previousUserPrompt = "";

	protected void actionPerformed(Project project, Editor editor, String selectedText) {
		if (selectedText != null && !selectedText.isEmpty()) {
			var dialog = new CustomPromptDialog(previousUserPrompt);
			if (dialog.showAndGet()) {
				previousUserPrompt = dialog.getUserPrompt();
				SwingUtilities.invokeLater(() -> sendMessage(InlineChatDTO.builder().question(dialog.getFullPrompt()).context(InlineChatDTO.replaceTableKey(selectedText)).language(EditorUtil.getLanguage(editor)).build(), project));
			}
		}
	}

}
