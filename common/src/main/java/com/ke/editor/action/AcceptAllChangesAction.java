package com.ke.editor.action;

import com.intellij.diff.FrameDiffTool;
import com.intellij.diff.tools.simple.SimpleDiffViewer;
import com.intellij.diff.tools.util.DiffDataKeys;
import com.intellij.diff.util.Side;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.ex.EditorEx;
import com.ke.BaseAction;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class AcceptAllChangesAction extends BaseAction {


    public AcceptAllChangesAction() {
        super("Accept All Changes", "Accept all changes", AllIcons.Diff.ApplyNotConflictsRight);
    }

    @Override
    public void doAction(@NotNull AnActionEvent e) {

        FrameDiffTool.DiffViewer diffViewer = e.getData(DiffDataKeys.DIFF_VIEWER);
        if (diffViewer instanceof SimpleDiffViewer) {
            EditorEx rightEditor = ((SimpleDiffViewer) diffViewer).getEditor(Side.RIGHT);
            EditorEx leftEditor = ((SimpleDiffViewer) diffViewer).getEditor(Side.LEFT);
            WriteCommandAction.runWriteCommandAction(e.getProject(),()-> leftEditor.getDocument().setText(rightEditor.getDocument().getText()));
        }
    }


    @Override
    protected boolean canShow(@NotNull AnActionEvent e) {
        return e.getData(DiffDataKeys.DIFF_VIEWER) instanceof SimpleDiffViewer &&
                ((SimpleDiffViewer) Objects.requireNonNull(e.getData(DiffDataKeys.DIFF_VIEWER))).getEditor(Side.LEFT).getDocument().isWritable();
    }

    @Override
    protected boolean isWebviewNeeded() {
        return false;
    }
}
