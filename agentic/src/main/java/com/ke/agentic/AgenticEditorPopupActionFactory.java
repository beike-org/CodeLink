package com.ke.agentic;

import com.intellij.openapi.actionSystem.AnAction;
import com.ke.agentic.actions.Add2ChatAction;
import com.ke.editor.EditorPopupActionFactory;

import java.util.List;

/**
 * @Author: zhangshaoxun001
 * @Date: 2025/6/26 17:20
 * @Description
 */
public class AgenticEditorPopupActionFactory implements EditorPopupActionFactory {

    @Override
    public List<AnAction> getActions() {
        return List.of(new Add2ChatAction());
    }

}
