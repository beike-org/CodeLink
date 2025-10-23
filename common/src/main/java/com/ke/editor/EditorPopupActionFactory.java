package com.ke.editor;


import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.extensions.ExtensionPointName;

import java.util.List;

/**
 * 注册Action到选择代码时的EditorPopup
 */
public interface EditorPopupActionFactory {
	ExtensionPointName<EditorPopupActionFactory> EP_NAME = ExtensionPointName.create("com.ke.codelink.editorPopupActionFactory");


	List<AnAction> getActions();

}
