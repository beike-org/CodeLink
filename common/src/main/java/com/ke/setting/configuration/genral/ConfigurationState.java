package com.ke.setting.configuration.genral;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.ke.webview.WebViewProjectConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
        name = "com.ke.global.state.CodeLink_Configuration",
        storages = @Storage(StoragePathMacros.WORKSPACE_FILE)
)
public class ConfigurationState implements PersistentStateComponent<ConfigurationState> {

    //Webview项目级缓存
    public WebViewProjectConfig webViewProjectConfig;


    public static ConfigurationState getInstance(Project project) {
        return project.getService(ConfigurationState.class);
    }

    @Nullable
    @Override
    public ConfigurationState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull ConfigurationState state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
