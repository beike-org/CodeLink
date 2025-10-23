package com.ke.editor.linemarker;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.psi.PsiElement;
import com.ke.Bundle;
import com.ke.editor.action.DefaultPromptActionFactory;
import com.ke.editor.linemarker.action.group.BasePopupAction;
import com.ke.editor.linemarker.action.group.LineMarkerGroupAdapter;
import com.ke.utils.ComponentUtil;
import com.ke.webview.dto.InlineChatDTO;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import static com.ke.editor.linemarker.action.group.LineMarkerGroupAdapter.LINE_MARKER_GROUP_ADAPTER;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/7/18 15:18
 * @Version 1.0
 * @Description
 */
public class LineMarkerPopup {

	private static final Logger LOGGER = Logger.getInstance(LineMarkerPopup.class);

	public static ListPopup createPopup(DataContext dataContext, String language, String selectedText, PsiElement psiElement) {

		DefaultActionGroup defaultActionGroup = new DefaultActionGroup();

		//通用的Action
		DefaultPromptActionFactory.DEFAULT_ACTIONS.forEach((label, prompt) -> {
			var action = new BasePopupAction(label, label) {

				@Override
				protected void doAction(@NotNull AnActionEvent e) {
					sendMessage(InlineChatDTO.builder().command(prompt).context(InlineChatDTO.replaceTableKey(selectedText)).language(language).build(), e.getProject());
				}
			};
			defaultActionGroup.add(action);
		});
		List<LineMarkerGroupAdapter> extensionList = LINE_MARKER_GROUP_ADAPTER.getExtensionList();
		if (CollectionUtils.isNotEmpty(extensionList)) {
			extensionList.forEach(provider -> {
				List<AnAction> actions = provider.getActionGroup(psiElement);
				if (Objects.nonNull(actions)) {
					defaultActionGroup.addSeparator();
					defaultActionGroup.addAll(actions);
				}

			});

		}
		return ComponentUtil.createListPopup(Bundle.get("toolWindow.CodeLink.stripe"), defaultActionGroup, dataContext);
	}


}
