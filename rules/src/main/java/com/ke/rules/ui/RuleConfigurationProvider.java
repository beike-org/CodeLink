package com.ke.rules.ui;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurableProvider;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

public class RuleConfigurationProvider extends ConfigurableProvider {

	private final Project project;

	public RuleConfigurationProvider(Project project) {
		this.project = project;
	}

	@Override
	public @Nullable Configurable createConfigurable() {
		return new RuleConfigurationConfigurable(project);
	}

	@Override
	public boolean canCreateConfigurable() {
		return true;
	}
}
