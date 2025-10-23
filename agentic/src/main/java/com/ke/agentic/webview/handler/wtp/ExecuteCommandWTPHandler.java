package com.ke.agentic.webview.handler.wtp;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.ke.webview.communication.handler.wtp.BaseWTPHandler;
import com.ke.agentic.socket.SideCarSocketCacheManager;
import com.ke.agentic.webview.handler.dto.ExecuteCommandDTO;
import com.ke.utils.JsonUtil;
import com.ke.utils.TerminalUtil;
import com.ke.agentic.webview.handler.AgenticWebviewCommandEnums;

import java.util.Objects;


public class ExecuteCommandWTPHandler extends BaseWTPHandler {

	public ExecuteCommandWTPHandler(Project project) {
		super(s -> {
			ExecuteCommandDTO executeCommandDTO = JsonUtil.getData(s, ExecuteCommandDTO.class);

			if (Objects.isNull(executeCommandDTO)) {
				return null;
			}

			if (Boolean.TRUE.equals(executeCommandDTO.getInTerminal())) {
				ApplicationManager.getApplication().invokeLater(() ->
						TerminalUtil.executeCommand(
								project,
								executeCommandDTO.getCommand())
				);
			} else {
				project.getService(SideCarSocketCacheManager.class).executeCommand(executeCommandDTO.getCommandId());
			}
			return null;
		});
	}

	@Override
	public String getCommand() {
		return AgenticWebviewCommandEnums.EXECUTE_COMMAND.getCommand();
	}
}
