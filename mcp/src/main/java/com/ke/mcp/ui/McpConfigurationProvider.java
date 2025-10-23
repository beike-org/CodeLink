package com.ke.mcp.ui;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurableProvider;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

/**
 * mcp设置
 */
public class McpConfigurationProvider extends ConfigurableProvider {

	private final Project project;

	public McpConfigurationProvider(Project project) {
		this.project = project;
	}

	@Override
	public @Nullable Configurable createConfigurable() {
		return new McpConfigurationConfigurable(project);
	}

	@Override
	public boolean canCreateConfigurable() {
		return true;
	}
}
