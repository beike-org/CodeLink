package com.ke.agentic.webview.handler;

import com.intellij.openapi.project.Project;
import com.ke.agentic.webview.handler.wtp.*;
import com.ke.toolwindow.content.BaseH5Panel;
import com.ke.webview.communication.handler.KeCopilotPanelHandler;
import com.ke.webview.communication.handler.ptw.*;
import com.ke.webview.communication.handler.wtp.*;
import com.ke.agentic.webview.handler.ptw.AddToCodePTWHandler;

import java.util.Map;

public class AgenticKeCopilotPanelHandler implements KeCopilotPanelHandler {
	@Override
	public Map<String, BasePTWHandler> getPTWHandler(BaseH5Panel h5Panel, Project project) {
		return Map.ofEntries(
				Map.entry(AgenticWebviewCommandEnums.ADD_TO_CHAT.getCommand(), new AddToCodePTWHandler(h5Panel))
		);

	}

	@Override
	public Map<String, BaseWTPHandler> getWTPHandler(BaseH5Panel h5Panel, Project project) {
		return Map.ofEntries(
				Map.entry(AgenticWebviewCommandEnums.EXECUTE_COMMAND.getCommand(), new ExecuteCommandWTPHandler(project)),
				Map.entry(AgenticWebviewCommandEnums.CANCEL_COMMAND.getCommand(), new CancelCommandWTPHandler(project)),
				Map.entry(AgenticWebviewCommandEnums.AGENTIC_DIFF_FILE.getCommand(), new AgenticDiffFileWTPHandler(project)),
				Map.entry(AgenticWebviewCommandEnums.AGENTIC_REJECT_CHANGE.getCommand(), new AgenticRejectChangeWTPHandler(project)),
				Map.entry(AgenticWebviewCommandEnums.SEARCH_DIRECTORY.getCommand(), new SearchDirectoryWTPHandler(project)),
				Map.entry(AgenticWebviewCommandEnums.DEFAULT_DIRECTORY_LIST.getCommand(), new DefaultDirectoryListWTPHandler(project))
		);
	}
}
