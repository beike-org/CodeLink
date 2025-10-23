package com.ke.setting.configuration.genral.user;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.OptionTag;
import com.ke.setting.configuration.genral.user.bean.PluginConfigVO;
import com.ke.setting.configuration.genral.user.bean.UserConfig;
import com.ke.setting.configuration.genral.user.bean.converter.UserConfigConverter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/8/19 10:28
 * @Version 1.0
 * @Description
 */
@State(
        name = "com.ke.global.state.UserConfigState",
        storages = @Storage("Ke_UserConfigState.xml")
)
public class UserConfigState implements PersistentStateComponent<UserConfig> {

    @OptionTag(converter = UserConfigConverter.class)
    private UserConfig userConfig = new UserConfig();

    public static UserConfigState getInstance() {
        return ApplicationManager.getApplication().getService(UserConfigState.class);
    }

    public PluginConfigVO getPluginConfig(){
        PluginConfigVO pluginConfigVO = getState().getPluginConfigVO();
        if(Objects.isNull(pluginConfigVO)){
            pluginConfigVO = new PluginConfigVO();
            userConfig.setPluginConfigVO(pluginConfigVO);
        }
        return getState().getPluginConfigVO();
    }

    @Override
    @NotNull
    public UserConfig getState() {
        return userConfig;
    }

    @Override
    public void loadState(@NotNull UserConfig state) {
        XmlSerializerUtil.copyBean(state, this.userConfig);
    }
}
