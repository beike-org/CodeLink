package com.ke.agentic.actions;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.ke.editor.action.BaseEditorPopupAction;
import com.ke.utils.IconUtil;
import com.ke.agentic.webview.handler.dto.SelectCodeDTO;
import com.ke.agentic.webview.handler.AgenticWebviewCommandEnums;
import com.ke.webview.util.PTWUtil;

/**
 * @Author: zhangshaoxun001
 * @Date: 2025/6/26 17:11
 * @Description
 */
public class Add2ChatAction extends BaseEditorPopupAction {
    public Add2ChatAction() {
        super("Add2Chat", null, IconUtil.getAdd2ChatIcon());
    }

    @Override
    protected void actionPerformed(Project project, Editor editor, String selectedText) {
        int startOffset = editor.getSelectionModel().getSelectionStart();
        int endOffset = editor.getSelectionModel().getSelectionEnd();
        Document document = editor.getDocument();
        int startLine = document.getLineNumber(startOffset) + 1;
        int endLine = document.getLineNumber(endOffset) + 1;

        SelectCodeDTO selectCodeDTO = SelectCodeDTO.builder()
                .code(selectedText)
                .source(editor.getVirtualFile().getPath())
                .language(com.ke.utils.EditorUtil.getLanguage(editor))
                .fsPath(editor.getVirtualFile().getPath())
                .startLine(startLine)
                .endLine(endLine)
                .startOffset(startOffset)
                .endOffset(endOffset)
                .fullContent(document.getText())
                .build();
        PTWUtil.sendMessage(AgenticWebviewCommandEnums.ADD_TO_CHAT, selectCodeDTO, project);
    }
}
