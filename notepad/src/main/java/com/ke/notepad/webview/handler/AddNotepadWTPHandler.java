package com.ke.notepad.webview.handler;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.ke.notepad.ui.NotepadPanel;
import com.ke.notepad.webview.NotepadWebviewCommandEnums;
import com.ke.webview.communication.handler.wtp.BaseWTPHandler;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class AddNotepadWTPHandler extends BaseWTPHandler {

	public AddNotepadWTPHandler(Project project) {
		super(s -> {
			ApplicationManager.getApplication().invokeLater(() -> {
				ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("NotePad");
				if (Objects.nonNull(toolWindow)) {
					toolWindow.show(() -> {
						// 工具窗口显示后，查找并点击新建记事本按钮
						Content content = toolWindow.getContentManager().getContent(0);
						if (content != null) {
							Component component = content.getComponent();
							if (component instanceof NotepadPanel notepadPanel) {
								// 查找新建记事本按钮并点击
								findAndClickNewNoteButton(notepadPanel);
							}
						}
					});
				}
			});
			return null;
		});
	}

	private static void findAndClickNewNoteButton(NotepadPanel notepadPanel) {
		// 遍历面板中的所有组件，查找"新建记事本"按钮
		for (Component comp : notepadPanel.getComponents()) {
			if (comp instanceof JPanel panel) {
				for (Component panelComp : panel.getComponents()) {
					if (panelComp instanceof JButton button) {
						if ("新建记事本".equals(button.getText())) {
							// 模拟点击按钮
							button.doClick();
							return;
						}
					}
				}
			}
		}
	}

	@Override
	public String getCommand() {
		return NotepadWebviewCommandEnums.ADD_NOTEPAD.getCommand();
	}
}
