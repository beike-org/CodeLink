package com.ke.webview;

import com.google.common.collect.Maps;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.ui.jcef.JBCefApp;
import com.ke.setting.configuration.genral.ConfigurationState;
import com.ke.toolwindow.content.CommonWebviewPanelEnums;
import com.ke.toolwindow.content.BaseH5Panel;
import com.ke.toolwindow.content.KeCopilotWindowPanel;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.MapUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/8/19 16:04
 * @Version 1.0
 * @Description
 */
@Service(Service.Level.PROJECT)
public class WebViewManager {

    private final Project project;

    public static final AtomicBoolean isWebviewSupported = new AtomicBoolean(true);


    //是否可以开始加载
    @Setter
    @Getter
    private Boolean canLoaded;

    private WebViewProjectConfig webViewProjectConfig;

    //存储h5页面
    private Map<String, BaseH5Panel> h5PanelMap = Maps.newConcurrentMap();

    public WebViewManager(Project project) {
        this.project = project;
        this.canLoaded = false;
        this.webViewProjectConfig = WebViewProjectConfig.builder().build();
        if (!JBCefApp.isSupported()) {
            isWebviewSupported.set(false);
        }
    }

    public void initWebViewManager() {
        // 支持webview
        if (JBCefApp.isSupported()) {

            WebviewPanelFactory.EP_NAME.getExtensionList().forEach(webviewPanelFactory -> {
                if (MapUtils.isNotEmpty(webviewPanelFactory.getH5Panels(project))) {
                    h5PanelMap.putAll(webviewPanelFactory.getH5Panels(project));
                }
            });

            h5PanelMap.put(CommonWebviewPanelEnums.KE_COPILOT.getName(), new KeCopilotWindowPanel(project));

        } else {
            h5PanelMap = Map.of();
        }
    }

    public BaseH5Panel getH5Panel(String key) {
        return h5PanelMap.get(key);
    }

    public @NotNull WebViewProjectConfig getWebViewProjectConfig() {
        return webViewProjectConfig;
    }

    public void setWebViewProjectConfig(@NotNull WebViewProjectConfig webViewProjectConfig) {
        this.webViewProjectConfig = webViewProjectConfig;
        project.getService(ConfigurationState.class).webViewProjectConfig = webViewProjectConfig;
    }

    /**
     * 当从webview打开其他组件时，可能出现焦点丢失的问题(有光标但无焦点)，这里进行焦点转移
     */
    public void transferFocus(JComponent component) {

        BaseH5Panel h5Panel = getH5Panel(CommonWebviewPanelEnums.KE_COPILOT.getName());
        if (h5Panel.isVisible()) {
            h5Panel.getJbCefBrowser().getComponent().transferFocus();
            h5Panel.transferFocusBackward();
            if (Objects.nonNull(component)) {
                component.requestFocus();
            }
        }

    }
}
