package com.ke.setting.configuration.genral;

import com.intellij.openapi.options.Configurable;
import com.ke.setting.configuration.genral.ui.GeneralConfigurationComponent;
import com.ke.setting.configuration.genral.user.UserConfigState;
import com.ke.setting.configuration.genral.user.bean.PluginConfigVO;
import com.ke.setting.configuration.genral.user.bean.UserConfig;
import com.ke.webview.BaseCommandEnums;
import com.ke.webview.util.PTWUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

/**
 * 应用级配置
 */
public class GeneralConfigurationConfigurable implements Configurable {

    private GeneralConfigurationComponent generalConfigurationComponent;

    public GeneralConfigurationConfigurable() {
    }

    @Override
    public String getDisplayName() {
        return "CodeLink: GeneralSetting";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        generalConfigurationComponent = new GeneralConfigurationComponent();
        return generalConfigurationComponent.getPanel();
    }


    @Override
    public boolean isModified() {
        UserConfig state = UserConfigState.getInstance().getState();
        return !state.getLineMarker().equals(generalConfigurationComponent.getOpenLineMarker())
                || !generalConfigurationComponent.getOpenCodeBlockTrunk().equals(state.getPluginConfigVO().getIsCodeBlockTruncated())
                || !state.getStackAnalysis().equals(generalConfigurationComponent.getOpenStackAnalysis())
                || !state.getEditorSelectedPopup().equals(generalConfigurationComponent.getEditorSelectedPopup())
                || !state.getWebviewOSR().equals(generalConfigurationComponent.getOpenWebviewOSR())
                || !state.getAutoRun().equals(generalConfigurationComponent.getAutoRun());

    }

    @Override
    public void apply() {
        UserConfigState.getInstance().getState().setLineMarker(generalConfigurationComponent.getOpenLineMarker());
        UserConfigState.getInstance().getState().setStackAnalysis(generalConfigurationComponent.getOpenStackAnalysis());
        UserConfigState.getInstance().getState().setEditorSelectedPopup(generalConfigurationComponent.getEditorSelectedPopup());
        UserConfigState.getInstance().getState().setWebviewOSR(generalConfigurationComponent.getOpenWebviewOSR());
        UserConfigState.getInstance().getState().setAutoRun(generalConfigurationComponent.getAutoRun());
        UserConfigState.getInstance().getPluginConfig().setIsCodeBlockTruncated(generalConfigurationComponent.getOpenCodeBlockTrunk());

        PluginConfigVO pluginConfigVO = UserConfigState.getInstance().getPluginConfig();
        if (Objects.nonNull(pluginConfigVO) && !generalConfigurationComponent.getOpenCodeBlockTrunk().equals(pluginConfigVO.getIsCodeBlockTruncated())) {
            pluginConfigVO.setIsCodeBlockTruncated(generalConfigurationComponent.getOpenCodeBlockTrunk());
            PTWUtil.sendMessage(BaseCommandEnums.SYNC_PLUGIN_CONFIG, pluginConfigVO);
        }


    }

    @Override
    public void reset() {
        UserConfig state = UserConfigState.getInstance().getState();
        generalConfigurationComponent.setOpenLineMarker(state.getLineMarker());
        generalConfigurationComponent.setOpenCodeBlockTrunkToggleButton(Boolean.TRUE.equals(state.getPluginConfigVO().getIsCodeBlockTruncated()));
        generalConfigurationComponent.setOpenStackAnalysis(state.getStackAnalysis());
        generalConfigurationComponent.setEditorSelectedPopup(state.getEditorSelectedPopup());
        generalConfigurationComponent.setOpenWebviewOSR(state.getWebviewOSR());
        generalConfigurationComponent.setAutoRun(state.getAutoRun());
    }

    @Override
    public void disposeUIResources() {
        generalConfigurationComponent = null;
    }


}
