package com.ke.editor.linemarker.action.group;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsActions;
import com.ke.BaseAction;
import com.ke.webview.communication.handler.ptw.InlineChatPTWHandler;
import com.ke.webview.dto.InlineChatDTO;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public abstract class BasePopupAction extends BaseAction {

	public BasePopupAction(
			@Nullable @NlsActions.ActionText String text,
			@Nullable @NlsActions.ActionDescription String description,
			@Nullable Icon icon) {
		super(text, description, icon);

	}

	public BasePopupAction(
			@Nullable @NlsActions.ActionText String text,
			@Nullable @NlsActions.ActionDescription String description) {
		this(text, description, null);
	}


	protected void sendMessage(InlineChatDTO inlineChatDTO, Project project) {
		InlineChatPTWHandler.sendInlineChat(inlineChatDTO, project);
	}


	@Override
    @NotNull
	public ActionUpdateThread getActionUpdateThread() {
		return ActionUpdateThread.EDT;
	}
}
