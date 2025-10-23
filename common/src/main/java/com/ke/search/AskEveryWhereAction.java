package com.ke.search;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.impl.EditorComponentImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.terminal.JBTerminalPanel;
import com.intellij.terminal.ui.TerminalWidget;
import com.jediterm.terminal.TerminalStarter;
import com.jediterm.terminal.TtyConnector;
import com.ke.BaseAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.terminal.TerminalToolWindowManager;

import java.awt.*;
import java.util.Objects;
import java.util.Set;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/8/15 17:22
 * @Version 1.0
 * @Description
 */
public class AskEveryWhereAction extends BaseAction {

	/**
	 * @see TerminalStarter#sendString(String) 通过 TerminalStarter#sendString(String) 方法发送命令,添加文本到终端
	 */
	@Override
	public void doAction(@NotNull AnActionEvent event) {
		Project project = event.getProject();
		if (Objects.isNull(project)) {
			return;
		}
		if (isTerminalPanel(project)) {
			Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
			if (focusOwner instanceof JBTerminalPanel) {
				TerminalWidget activeTerminalWidget = getActiveTerminalWidget(event);
				TtyConnector ttyConnector = null;
				if (Objects.nonNull(activeTerminalWidget)) {
					ttyConnector = activeTerminalWidget.getTtyConnector();
				}
				project.getService(AskEveryWhereService.class).show(AskMode.builder().type(AskMode.Type.TERMINAL).ttyConnector(ttyConnector).component(focusOwner).build());
			} else if (focusOwner instanceof EditorComponentImpl) {
				//终端新UI
				project.getService(AskEveryWhereService.class).show(AskMode.builder().type(AskMode.Type.NEW_TERMINAL).component(focusOwner).build());
			}

			return;

		}
		project.getService(AskEveryWhereService.class).show(AskMode.builder().type(AskMode.Type.NORMAL).build());

	}


	// 获取当前活跃的终端会话
	private TerminalWidget getActiveTerminalWidget(AnActionEvent e) {
		Project project = e.getProject();
		if (Objects.isNull(project)) {
			return null;
		}
		Set<TerminalWidget> terminalWidgets = TerminalToolWindowManager.getInstance(project).getTerminalWidgets();
		for (TerminalWidget terminalWidget : terminalWidgets) {
			if (terminalWidget.getComponent().isShowing()) {
				return terminalWidget;
			}
		}
		return null;

	}

	private static boolean isTerminalPanel(Project project) {
		ToolWindow toolWindow = TerminalToolWindowManager.getInstance(project).getToolWindow();
		return Objects.nonNull(toolWindow) && toolWindow.isActive();
	}

}
