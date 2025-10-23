package com.ke.toolwindow.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.ke.Bundle;
import com.ke.setting.configuration.genral.GeneralConfigurationConfigurable;
import com.ke.utils.IconUtil;
import org.jetbrains.annotations.NotNull;

public class SettingAction extends BaseToolWindowAction {

    public SettingAction() {
        super(Bundle.get("action.setting"), Bundle.get("action.setting"), IconUtil.getSettingIcon());
    }

    @Override
    public void doAction(@NotNull AnActionEvent e) {
        ShowSettingsUtil.getInstance().showSettingsDialog(e.getProject(), GeneralConfigurationConfigurable.class);
    }
}
