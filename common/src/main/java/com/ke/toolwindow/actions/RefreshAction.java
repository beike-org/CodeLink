package com.ke.toolwindow.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.ke.Bundle;
import com.ke.toolwindow.content.BaseH5Panel;
import com.ke.toolwindow.content.CommonWebviewPanelEnums;
import com.ke.utils.ComponentUtil;
import com.ke.utils.RuntimeEnvUtil;
import com.ke.webview.WebViewManager;
import com.ke.webview.WebViewProjectConfig;
import com.ke.webview.topic.RefreshNotifier;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class RefreshAction extends BaseToolWindowAction {

    public RefreshAction() {
        super(Bundle.get("action.refresh"), Bundle.get("action.refresh"), AllIcons.Actions.Refresh);
    }


    @Override
    public void doAction(@NotNull AnActionEvent e) {

        Project project = e.getProject();

        //刷新webview中加载的git
        WebViewProjectConfig webViewProjectConfig = project.getService(WebViewManager.class).getWebViewProjectConfig();

        webViewProjectConfig.setJdkVersion(RuntimeEnvUtil.getJDKVersion(project));
        webViewProjectConfig.setIsMavenProject(RuntimeEnvUtil.isMavenProject(project));

        //通知webview刷新
        ApplicationManager.getApplication().getMessageBus().syncPublisher(RefreshNotifier.REFRESH_ACTION_TOPIC).refresh(project);
    }


    @Override
    public void update(@NotNull AnActionEvent e) {
        if (Objects.nonNull(e.getProject()) && e.getProject().getService(WebViewManager.class).getCanLoaded()) {
            BaseH5Panel h5Panel = e.getProject().getService(WebViewManager.class).getH5Panel(CommonWebviewPanelEnums.KE_COPILOT.getName());
            ToolWindow codeLinkToolWindow = ComponentUtil.getCodeLinkToolWindow(e.getProject());

            if (Objects.nonNull(h5Panel) && Objects.nonNull(codeLinkToolWindow)  &&
                    !codeLinkToolWindow.getComponent().equals(h5Panel.getParent())) {
				codeLinkToolWindow.getComponent().add(h5Panel);
            }
        }

    }
}
