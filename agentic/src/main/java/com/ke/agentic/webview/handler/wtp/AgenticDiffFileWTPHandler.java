package com.ke.agentic.webview.handler.wtp;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.ke.webview.communication.handler.wtp.BaseWTPHandler;
import com.ke.agentic.webview.handler.dto.AgenticDiffFileDTO;
import com.ke.utils.DiffUtil;
import com.ke.utils.JsonUtil;
import com.ke.agentic.webview.handler.AgenticWebviewCommandEnums;


public class AgenticDiffFileWTPHandler extends BaseWTPHandler {

	public AgenticDiffFileWTPHandler(Project project) {
		super(s -> {
			AgenticDiffFileDTO agenticDiffFileDTO = JsonUtil.getData(s, AgenticDiffFileDTO.class);
			ApplicationManager.getApplication().invokeLater(() -> DiffUtil.showUnifiedDiffContentInEditor(project, agenticDiffFileDTO.getFilePath(), agenticDiffFileDTO.getOldContent(), agenticDiffFileDTO.getFilePath(), "Agentic Diff Changes", agenticDiffFileDTO.getFilePath(), true));
			return null;
		});
	}

	@Override
	public String getCommand() {
		return AgenticWebviewCommandEnums.AGENTIC_DIFF_FILE.getCommand();
	}
}
