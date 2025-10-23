package com.ke.notepad.ui;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import org.jetbrains.annotations.NotNull;

public class NotepadToolWindowFactory implements ToolWindowFactory, DumbAware {
	@Override
	public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
		NotepadPanel notepadPanel = new NotepadPanel(project);
		var contentManager = toolWindow.getContentManager();
		var content = contentManager.getFactory().createContent(notepadPanel, "", false);
		content.setCloseable(false);
		contentManager.addContent(content);
	}

	@Override
	public void init(@NotNull ToolWindow toolWindow) {
		ToolWindowFactory.super.init(toolWindow);
		toolWindow.setStripeTitle("NotePad");
	}
}