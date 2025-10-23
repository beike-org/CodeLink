package com.ke.toolwindow;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.jcef.JBCefApp;
import com.ke.Bundle;

import com.ke.service.notify.NotifyServiceImpl;
import com.ke.toolwindow.actions.*;
import com.ke.toolwindow.content.BaseH5Panel;
import com.ke.toolwindow.content.CommonWebviewPanelEnums;
import com.ke.toolwindow.content.ToolWindowContentPanel;
import com.ke.webview.WebViewManager;
import com.ke.webview.topic.LoadNotifier;
import com.ke.webview.topic.RefreshNotifier;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ProjectToolWindowFactory implements ToolWindowFactory, DumbAware {

    //项目级判断toolwindow是否加载
    private static final Key<Boolean> KEY_PROJECT_INIT = Key.create("CodeLink.toolWindow.init");

    @Override
    public void init(@NotNull ToolWindow toolWindow) {
        ToolWindowFactory.super.init(toolWindow);
        toolWindow.setStripeTitle(Bundle.get("toolWindow.CodeLink.stripe"));
    }

    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {

        //如果项目初始化完后打开，则直接加载
        if (project.getService(WebViewManager.class).getCanLoaded()) {
            initContent(project, toolWindow);
        }

        //在项目初始化完之前打开，则使用懒加载，等待项目初始化完成后才加载
        //com.ke.toolwindow.actions.RefreshAction.update中也会做更新
        project.getMessageBus().connect().subscribe(LoadNotifier.LOAD_TOPIC, (LoadNotifier) project1 -> initContent(project, toolWindow));

        List<AnAction> actionList = new ArrayList<>();
        ToolWindowActionFactory.EP_NAME.getExtensionList().forEach(factory -> {
            List<AnAction> actions = factory.getActions();
            if (Objects.nonNull(actions) && !actions.isEmpty()) {
                actionList.addAll(actions);
            }
        });

        //添加工具栏的action
        if (ApplicationManager.getApplication().isInternal()) {
            actionList.addAll(Arrays.asList(new SettingAction(), new RefreshAction(), new WebviewDebugAction()));
        } else {
            actionList.addAll(Arrays.asList(new SettingAction(), new RefreshAction()));
        }
        toolWindow.setTitleActions(actionList);
    }

    private void initContent(Project project, ToolWindow toolWindow) {
        if (Objects.isNull(KEY_PROJECT_INIT.get(project))) {
            KEY_PROJECT_INIT.set(project, true);
            WebViewManager webViewManager = project.getService(WebViewManager.class);

            if (JBCefApp.isSupported()) {
                BaseH5Panel h5Panel = webViewManager.getH5Panel(CommonWebviewPanelEnums.KE_COPILOT.getName());
                addContent(toolWindow, h5Panel);

                project.getMessageBus().connect().subscribe(RefreshNotifier.REFRESH_ACTION_TOPIC, (RefreshNotifier) refreshProject -> {
                    //refreshProject为null刷新所有打开的project，否则只刷新当前project
                    if (Objects.isNull(refreshProject) || project.equals(refreshProject)) {
                        Content selectedContent = toolWindow.getContentManager().getSelectedContent();
                        if (Objects.nonNull(selectedContent)) {
                            ((ToolWindowContentPanel) selectedContent.getComponent()).refreshContent(project);
                        }
                    }
                });

                if (Integer.parseInt(ApplicationInfo.getInstance().getMajorVersion()) > 2024) {
                    h5Panel.refreshContent(project);
                }

            } else if (ApplicationInfo.getInstance().getVersionName().contains("Android Studio")
                    && ApplicationInfo.getInstance().getBuild().getBaselineVersion() < 222) {
                project.getService(NotifyServiceImpl.class).error(Bundle.get("plugin.error.android.version.cefNotSupported"));
                JLabel label = new JLabel("<html>" +
                                                  Bundle.get("plugin.error.android.version.cefNotSupported") +
                                                  "</html>"
                );
                addContent(toolWindow, label);

            } else {
                project.getService(NotifyServiceImpl.class).error(Bundle.get("plugin.error.cefNotSupported"));

                JLabel label = new JLabel("<html>" +
                                                  Bundle.get("plugin.error.cefNotSupported") +
                                                  "<br><br>解决步骤：<br>" +
                                                  "1. Help -> Find Action<br>" +
                                                  "2. 搜索 boot runtime，找到「Choose Boot Java Runtime for the IDE」选项<br>" +
                                                  "3. 在「New」中，选择任何一个带有 JCEF最大版本 的 Runtime<br>" +
                                                  "4. 等待Runtime替换完成,重启IDE" +
                                                  "</html>"
                );

                addContent(toolWindow, label);
            }
        }
    }

    public void addContent(ToolWindow toolWindow, JComponent panel, String displayName) {
        var contentManager = toolWindow.getContentManager();
        var content = contentManager.getFactory().createContent(panel, displayName, false);
        content.setCloseable(false);
        contentManager.addContent(content);
    }


    public void addContent(ToolWindow toolWindow, JComponent panel) {
        var contentManager = toolWindow.getContentManager();
        var content = contentManager.getFactory().createContent(panel, "", false);
        if (panel instanceof BaseH5Panel && !SystemInfo.isWindows) {
            //设置disposer，用于关闭时释放资源
            content.setDisposer((BaseH5Panel) panel);
        }
        content.setCloseable(false);
        contentManager.addContent(content);
    }

}
