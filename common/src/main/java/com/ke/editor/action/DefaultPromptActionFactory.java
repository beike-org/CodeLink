package com.ke.editor.action;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.ke.Bundle;
import com.ke.utils.EditorUtil;
import com.ke.utils.RuntimeEnvUtil;
import com.ke.webview.dto.InlineChatDTO;

import java.util.LinkedHashMap;
import java.util.Map;

public class DefaultPromptActionFactory {

	public static Map<String, String> DEFAULT_ACTIONS = new LinkedHashMap<>(Map.of(
			Bundle.get("action.editor.default.label.checkBug"), Bundle.get("action.editor.default.prompt.checkBug"),
			Bundle.get("action.editor.default.label.createUnitTest"), Bundle.get("action.editor.default.prompt.createUnitTest"),
			Bundle.get("action.editor.default.label.explainCode"), Bundle.get("action.editor.default.prompt.explainCode"),
			Bundle.get("action.editor.default.label.refactorCode"), Bundle.get("action.editor.default.prompt.refactorCode"),
			Bundle.get("action.editor.default.label.optimizeCode"), Bundle.get("action.editor.default.prompt.optimizeCode")));


	public static void refreshActions() {
		AnAction actionGroup = ActionManager.getInstance().getAction("com.ke.CodeLink.EditorActionGroup");
		if (actionGroup instanceof DefaultActionGroup) {
			DefaultActionGroup group = (DefaultActionGroup) actionGroup;
			group.removeAll();
			group.add(new CustomPromptEditorAction());
			group.addSeparator();

			DEFAULT_ACTIONS.forEach((label, prompt) -> {

				var action = new BaseEditorAction(label, label) {

					@Override
					protected void actionPerformed(Project project, Editor editor, String selectedText) {
						sendMessage(InlineChatDTO.builder()
								.command(prompt)
								.context(InlineChatDTO.replaceTableKey(selectedText))
								.language(EditorUtil.getLanguage(editor))
								.build(), project);
					}
				};
				group.add(action);
			});

			group.addSeparator();
			if (RuntimeEnvUtil.isClassPresent(Bundle.get("plugin.env.check.psiClass"))) {
				group.add(new JavaDocClassAction());
			}

		}
	}


}
