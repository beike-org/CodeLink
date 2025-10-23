package com.ke.editor.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsActions;
import com.ke.BaseAction;
import com.ke.webview.communication.handler.ptw.InlineChatPTWHandler;
import com.ke.webview.dto.InlineChatDTO;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public abstract class BaseEditorAction extends BaseAction {

    private static final Logger LOGGER = Logger.getInstance(BaseEditorAction.class);

    public BaseEditorAction(
            @Nullable @NlsActions.ActionText String text,
            @Nullable @NlsActions.ActionDescription String description,
            @Nullable Icon icon) {
        super(text, description, icon);
    }

    public BaseEditorAction(
            @Nullable @NlsActions.ActionText String text,
            @Nullable @NlsActions.ActionDescription String description) {
        this(text, description, null);
    }


    protected abstract void actionPerformed(Project project, Editor editor, String selectedText);

    @Override
    public void doAction(@NotNull AnActionEvent event) {
        var project = event.getProject();
        var editor = event.getData(PlatformDataKeys.EDITOR);
        if (editor != null && project != null) {
            actionPerformed(project, editor, editor.getSelectionModel().getSelectedText());
        }
    }


    @Override
    protected boolean canShow(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        boolean menuAllowed = false;
        if (editor != null && project != null) {
            menuAllowed = editor.getSelectionModel().getSelectedText() != null;
        }
        return menuAllowed;
    }


    protected void sendMessage(InlineChatDTO inlineChatDTO, Project project) {
        InlineChatPTWHandler.sendInlineChat(inlineChatDTO, project);
    }
}
