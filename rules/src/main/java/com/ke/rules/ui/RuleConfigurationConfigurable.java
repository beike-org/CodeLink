package com.ke.rules.ui;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class RuleConfigurationConfigurable implements Configurable {
	private static final Logger LOGGER = Logger.getInstance(RuleConfigurationConfigurable.class);
	private RuleConfigurationComponent ruleConfigurationComponent;
	private final Project project;

	public RuleConfigurationConfigurable(Project project) {
		this.project = project;
	}

	@Override
	public @NlsContexts.ConfigurableName String getDisplayName() {
		return "CodeLink: RulesSetting";
	}

	@Override
	public @Nullable JComponent createComponent() {
		try {
			ruleConfigurationComponent = new RuleConfigurationComponent(project);
			return ruleConfigurationComponent.getPanel();
		} catch (Exception e) {
			LOGGER.error("Failed to create rule configuration component", e);
			return new JPanel(); // 返回空面板而不是null，避免UI崩溃
		}
	}

	@Override
	public boolean isModified() {
		return false;
	}

	@Override
	public void apply() {
	}

	@Override
	public void disposeUIResources() {
		if (ruleConfigurationComponent != null) {
			// 清理资源
			ruleConfigurationComponent = null;
		}
	}
}
