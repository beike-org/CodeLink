package com.ke.notepad.webview;

import com.intellij.openapi.project.Project;
import com.ke.notepad.webview.handler.AddNotepadWTPHandler;
import com.ke.notepad.webview.handler.NotepadFileListWTPHandler;
import com.ke.toolwindow.content.BaseH5Panel;
import com.ke.webview.communication.handler.KeCopilotPanelHandler;
import com.ke.webview.communication.handler.ptw.BasePTWHandler;
import com.ke.webview.communication.handler.wtp.BaseWTPHandler;

import java.util.Map;

public class NotepadKeCopilotPanelHandler implements KeCopilotPanelHandler {


	@Override
	public Map<String, BasePTWHandler> getPTWHandler(BaseH5Panel panel, Project project) {
		return Map.of();
	}

	@Override
	public Map<String, BaseWTPHandler> getWTPHandler(BaseH5Panel panel, Project project) {
		return Map.of(
				NotepadWebviewCommandEnums.NOTEPAD_FILE_LIST.getCommand(), new NotepadFileListWTPHandler(project),
				NotepadWebviewCommandEnums.ADD_NOTEPAD.getCommand(), new AddNotepadWTPHandler(project)
		);
	}
}
