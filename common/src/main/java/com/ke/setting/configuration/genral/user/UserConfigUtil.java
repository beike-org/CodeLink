package com.ke.setting.configuration.genral.user;

import com.ke.setting.configuration.genral.user.bean.UserConfig;

/**
 * @Author: zhangshaoxun001
 * @Date: 2025/7/16 11:45
 * @Description
 */
public class UserConfigUtil {

    public static boolean isModelAvailable() {
        UserConfig userConfig = UserConfigState.getInstance().getState();
        return userConfig.getPluginConfigVO() != null
                && userConfig.getPluginConfigVO().getModelConfiguration() != null
                && userConfig.getPluginConfigVO().getModelConfiguration().getSelectedProvider() != null
                && userConfig.getPluginConfigVO().getModelConfiguration().getSelectedProviderApiKey() != null
                && userConfig.getPluginConfigVO().getModelConfiguration().getModelName() != null;
    }

}
