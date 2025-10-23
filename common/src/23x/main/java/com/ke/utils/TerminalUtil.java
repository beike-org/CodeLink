package com.ke.utils;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.terminal.JBTerminalWidget;
import com.intellij.terminal.ui.TerminalWidget;
import com.intellij.ui.content.Content;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.terminal.*;
import org.jetbrains.plugins.terminal.action.TerminalNewPredefinedSessionAction;

import javax.swing.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 实际这里分包也没生效，只在调试过程中有效，实际运行环境都用的23x的包
 * 如果要根据版本分包，要么打两个版本的包
 * 要么在代码中动态加载不同的包，即先发布两个Terminal23和Terminal24两个jar包，然后根据IDEA版本调用Terminal23或者Terminal24
 */
public final class TerminalUtil {

	private final static Logger LOG = Logger.getInstance(TerminalUtil.class);

	public static final String TAB_NAME = "Local(CodeLink)";


	/**
	 * 打开终端执行命令
	 */
	public static void executeCommand(Project project, String command) {

		TerminalWidget terminalWidget = findPluginTerminalWidget(project);
		ShellTerminalWidget pluginTerminalWidget;

		if (Objects.isNull(terminalWidget)) {
			LocalTerminalDirectRunner localTerminalDirectRunner = new LocalTerminalDirectRunner(project) {
				@Override
				public boolean isTerminalSessionPersistent() {
					return false;
				}
			};

			TerminalTabState tabState = new TerminalTabState();
			tabState.myTabName = TAB_NAME;
			TerminalToolWindowManager.getInstance(project).createNewSession(localTerminalDirectRunner, tabState);
			terminalWidget = findPluginTerminalWidget(project);
		}

		try {
			if (terminalWidget == null) {
				LOG.error("获取插件终端失败");
				return;
			}
			pluginTerminalWidget = (ShellTerminalWidget) JBTerminalWidget.asJediTermWidget(terminalWidget);
		} catch (Exception e) {
			LOG.error("获取插件终端失败", e);
			return;
		}

		JComponent component = terminalWidget.getComponent();
		ToolWindow terminal = ToolWindowManager.getInstance(project).getToolWindow("Terminal");
		if (Objects.nonNull(terminal)) {
			terminal.show();
			Content content = terminal.getContentManager().getContent(component);
			if (Objects.nonNull(content)) {
				terminal.getContentManager().setSelectedContent(content);
			}

		}

		try {
            assert pluginTerminalWidget != null;
            pluginTerminalWidget.executeCommand(command);
		} catch (Exception e) {
			LOG.error("执行命令失败", e);
		}

	}

	/**
	 * 找到插件创建的终端
	 */
	public static TerminalWidget findPluginTerminalWidget(Project project) {
		Set<TerminalWidget> terminalWidgets = TerminalToolWindowManager.getInstance(project).getTerminalWidgets();
		if (terminalWidgets.isEmpty()) {
			return null;
		}
		return terminalWidgets.stream().filter(terminalWidget -> TAB_NAME.equals(terminalWidget.getTerminalTitle().buildTitle())).findFirst().orElse(null);
	}

	/**
	 * 是否使用新的终端UI
	 */
	public static boolean isNewTerminalUi() {
		return Registry.is("terminal.new.ui", false);
	}

	/**
	 * 参考org.jetbrains.plugins.terminal.action.TerminalNewPredefinedSessionAction.OpenShellActions实现
	 *
	 * @see TerminalNewPredefinedSessionAction
	 */
	public static void openTerminal(Project project, String title, List<String> commandLine) {
		try {
			LocalTerminalDirectRunner runner = new LocalTerminalDirectRunner(project) {
				@Override
				public @NotNull List<String> getInitialCommand(@NotNull Map<String, String> envs) {
					return commandLine;
				}

				@Override
				public boolean isTerminalSessionPersistent() {
					return false;
				}
			};
			TerminalTabState tabState = new TerminalTabState();
			tabState.myTabName = title;
			TerminalView.getInstance(project).createNewSession(runner, tabState);
			ToolWindow codeLinkToolWindow = ToolWindowManager.getInstance(project).getToolWindow("com.ke.CodeLink");
			if (Objects.nonNull(codeLinkToolWindow)) {
				codeLinkToolWindow.hide();
				codeLinkToolWindow.show();
			}

		} catch (Exception e) {
			LOG.error("Terminal open error,Error occurs while openning terminal", e);
		}
	}
}
