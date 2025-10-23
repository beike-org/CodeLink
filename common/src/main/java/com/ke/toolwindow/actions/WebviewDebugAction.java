package com.ke.toolwindow.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.ke.toolwindow.content.CommonWebviewPanelEnums;
import com.ke.webview.WebViewManager;
import org.jetbrains.annotations.NotNull;

public class WebviewDebugAction extends BaseToolWindowAction {

	private final String WEBVIEW_KEY;

	public WebviewDebugAction() {
		super("F12", "F12", AllIcons.Debugger.Console);
		WEBVIEW_KEY = CommonWebviewPanelEnums.KE_COPILOT.getName();
	}

	public WebviewDebugAction(String webviewKey) {
		super("F12", "F12", AllIcons.Debugger.Console);
		WEBVIEW_KEY = webviewKey;
	}

	@Override
	public void doAction(@NotNull AnActionEvent e) {
		e.getProject().getService(WebViewManager.class).getH5Panel(WEBVIEW_KEY).getJbCefBrowser().openDevtools();
	}

}
