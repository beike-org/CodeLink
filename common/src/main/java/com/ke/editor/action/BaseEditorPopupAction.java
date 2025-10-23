package com.ke.editor.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsActions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

public abstract class BaseEditorPopupAction extends BaseEditorAction {

    public BaseEditorPopupAction(
            @Nullable @NlsActions.ActionText String text,
            @Nullable @NlsActions.ActionDescription String description,
            @Nullable Icon icon) {
        super(text, description, icon);

    }


    protected abstract void actionPerformed(Project project, Editor editor, String selectedText);

    @Override
    public void doAction(@NotNull AnActionEvent event) {
        var project = event.getProject();
        Editor editor = FileEditorManager.getInstance(event.getProject()).getSelectedTextEditor();
        actionPerformed(project, editor, editor.getSelectionModel().getSelectedText());
    }


    @Override
    protected boolean canShow(@NotNull AnActionEvent e) {
        boolean menuAllowed = false;
        if(Objects.isNull(e.getProject())){
            return false;
        }
        Editor editor = FileEditorManager.getInstance(e.getProject()).getSelectedTextEditor();
        if (editor != null)  {
            menuAllowed = editor.getSelectionModel().getSelectedText() != null;
        }
        return menuAllowed;
    }


    @Override
    public boolean displayTextInToolbar() {
        return true;
    }

}
