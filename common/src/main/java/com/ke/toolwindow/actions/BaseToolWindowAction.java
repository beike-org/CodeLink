package com.ke.toolwindow.actions;

import com.intellij.openapi.util.NlsActions;
import com.ke.BaseAction;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/11/1 16:36
 * @Version 1.0
 * @Description
 */
public abstract class BaseToolWindowAction extends BaseAction {

	public BaseToolWindowAction() {
	}

	public BaseToolWindowAction(@Nullable @NlsActions.ActionText String text, @Nullable @NlsActions.ActionDescription String description, @Nullable Icon icon) {
		super(text, description, icon);
	}


	@Override
	protected boolean isWebviewNeeded() {
		return false;
	}
}
