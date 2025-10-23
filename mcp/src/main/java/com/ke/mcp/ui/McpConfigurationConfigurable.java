package com.ke.mcp.ui;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;

import javax.swing.*;

public class McpConfigurationConfigurable implements Configurable {

	private final Project project;

	public McpConfigurationConfigurable(Project project) {
		this.project = project;
	}


	@Override
	public String getDisplayName() {
		return "CodeLink: McpSetting";
	}

	/**
	 * 配置mcp的ui页面
	 * <p>
	 * 从配置文件读取配置，展示到页面上
	 * 1.mcp.json读取工具项目
	 * 2.mcp_available.json读取可用的mcp
	 */
	@Override
	public JComponent createComponent() {
		McpConfigurationComponent mcpConfigurationComponent = new McpConfigurationComponent(project);
		return mcpConfigurationComponent.getPanel();
	}

	@Override
	public boolean isModified() {
		return false;
	}

	/**
	 * 保存设置
	 *
	 */
	@Override
	public void apply() {

	}

	@Override
	public void disposeUIResources() {
	}
}
