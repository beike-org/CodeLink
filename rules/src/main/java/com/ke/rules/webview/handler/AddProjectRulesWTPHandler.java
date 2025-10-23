package com.ke.rules.webview.handler;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.ke.rules.ui.RuleConfigurationConfigurable;
import com.ke.rules.webview.RulesWebviewCommandEnums;
import com.ke.webview.communication.handler.wtp.BaseWTPHandler;

public class AddProjectRulesWTPHandler extends BaseWTPHandler {

	public AddProjectRulesWTPHandler(Project project) {
		super(s -> {

			ApplicationManager.getApplication().invokeLater(() -> ShowSettingsUtil.getInstance().showSettingsDialog(project, RuleConfigurationConfigurable.class));
			return null;
		});
	}

	@Override
	public String getCommand() {
		return RulesWebviewCommandEnums.ADD_PROJECT_RULES.getCommand();
	}
}
