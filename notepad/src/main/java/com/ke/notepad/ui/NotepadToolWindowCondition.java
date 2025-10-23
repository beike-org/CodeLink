package com.ke.notepad.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import org.jetbrains.annotations.NotNull;

public class NotepadToolWindowCondition implements Condition<Project> {
	@Override
	public boolean value(@NotNull Project project) {
		return true;
	}
}