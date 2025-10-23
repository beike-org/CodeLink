package com.ke;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.util.NlsActions;
import com.ke.webview.WebViewManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/9/27 10:32
 * @Version 1.0
 * @Description
 */
public abstract class BaseAction extends AnAction {

	public BaseAction() {
	}

	public BaseAction(@Nullable @NlsActions.ActionText String text) {
		super(text);
	}

	public BaseAction(@Nullable @NlsActions.ActionText String text, @Nullable @NlsActions.ActionDescription String description, @Nullable Icon icon) {
		super(text, description, icon);
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		doAction(e);
	}

	/**
	 * 子action的实际逻辑
	 */
	protected abstract void doAction(@NotNull AnActionEvent e);



	/**
	 * 是否与webview相关联
	 */
	protected boolean isWebviewNeeded() {
		return true;
	}

	/**
	 * 是否满足显示条件,默认是
	 */
	protected boolean canShow(@NotNull AnActionEvent e) {
		return true;
	}


	@Override
	public void update(@NotNull AnActionEvent e) {
		e.getPresentation().setEnabledAndVisible(
						(!isWebviewNeeded() || WebViewManager.isWebviewSupported.get()) &&
						canShow(e));
	}

	/**
	 * 默认使用EDT线程。
	 * 但如果在update的过程中,对Virtual File System, PSI, references,query indexes/stubs等操作,则需要使用后台线程。
	 */
	@Override
	public @NotNull ActionUpdateThread getActionUpdateThread() {
		return ActionUpdateThread.EDT;
	}
}
