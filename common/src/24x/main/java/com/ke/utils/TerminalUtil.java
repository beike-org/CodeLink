package com.ke.utils;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.terminal.ui.TerminalWidget;
import com.intellij.ui.content.Content;
import com.jediterm.terminal.ProcessTtyConnector;
import com.jediterm.terminal.TtyConnector;
import com.pty4j.PtyProcess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.terminal.*;
import org.jetbrains.plugins.terminal.action.TerminalNewPredefinedSessionAction;

import javax.swing.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class TerminalUtil {

    private final static Logger LOG = Logger.getInstance(TerminalUtil.class);

    public static final String TAB_NAME = "Local(CodeLink)";


    /**
     * 打开终端执行命令
     */
    public static void executeCommand(Project project, String command) {

        TerminalWidget pluginTerminalWidget = findPluginTerminalWidget(project);
        if (Objects.isNull(pluginTerminalWidget)) {

            LocalTerminalDirectRunner localBlockTerminalRunner = new LocalTerminalDirectRunner(project) {
//                @Override
                protected boolean isBlockTerminalEnabled() {
                    return false;
                }

                @NotNull
                @Override
                protected TerminalWidget createShellTerminalWidget(@NotNull Disposable parent, @NotNull ShellStartupOptions startupOptions) {
                    TerminalWidget shellTerminalWidget = super.createShellTerminalWidget(parent, startupOptions);
                    shellTerminalWidget.sendCommandToExecute(command);
                    return shellTerminalWidget;
                }

                @Override
                public @NotNull TtyConnector createTtyConnector(@NotNull PtyProcess process) {
                    return super.createTtyConnector(process);
                }

                @Override
                public boolean isTerminalSessionPersistent() {
                    return false;
                }
            };
            TerminalTabState tabState = new TerminalTabState();
            tabState.myTabName = TAB_NAME;
            TerminalToolWindowManager.getInstance(project).createNewSession(localBlockTerminalRunner, tabState);
            pluginTerminalWidget = findPluginTerminalWidget(project);
        }

        if (Objects.isNull(pluginTerminalWidget)) {
            LOG.error("执行命令失败", "打开终端异常");
            return;
        }

        JComponent component = pluginTerminalWidget.getComponent();
        ToolWindow terminal = ToolWindowManager.getInstance(project).getToolWindow("Terminal");
        if (Objects.nonNull(terminal)) {
            terminal.show();
            Content content = terminal.getContentManager().getContent(component);
            if (Objects.nonNull(content)) {
                terminal.getContentManager().setSelectedContent(content);
            }

        }

        try {
            pluginTerminalWidget.sendCommandToExecute(command);
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
        return terminalWidgets.stream().filter(terminalWidget -> {
                                                   try {
                                                       if (TAB_NAME.equals(terminalWidget.getTerminalTitle().buildTitle())) {
                                                           return !org.jetbrains.plugins.terminal.TerminalUtil.hasRunningCommands((ProcessTtyConnector) Objects.requireNonNull(terminalWidget.getTtyConnector()));
                                                       }
                                                   } catch (Exception ignore) {

                                                   }
                                                   return false;
                                               }

        ).findFirst().orElse(null);
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
