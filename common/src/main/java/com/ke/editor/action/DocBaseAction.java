package com.ke.editor.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.ke.Bundle;
import com.ke.BaseAction;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/9/14 16:50
 * @Version 1.0
 * @Description 生成注释的基类, 用于判断是否可用
 */
public abstract class DocBaseAction extends BaseAction {


	public DocBaseAction() {
		super(Bundle.get("action.editor.default.label.generateComment"));
	}


	@Override
	protected boolean canShow(@NotNull AnActionEvent e) {
		Editor editor = e.getData(CommonDataKeys.EDITOR);
		return Objects.nonNull(editor) && editor.getDocument().isWritable();
	}

	@Override
	protected boolean isWebviewNeeded() {
		return false;
	}
}
