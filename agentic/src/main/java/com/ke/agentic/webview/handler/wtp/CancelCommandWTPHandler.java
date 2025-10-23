package com.ke.agentic.webview.handler.wtp;

import com.intellij.openapi.project.Project;
import com.ke.webview.communication.handler.wtp.BaseWTPHandler;
import com.ke.agentic.socket.SideCarSocketCacheManager;
import com.ke.agentic.webview.handler.dto.CancelCommandDTO;
import com.ke.utils.JsonUtil;
import com.ke.agentic.webview.handler.AgenticWebviewCommandEnums;


public class CancelCommandWTPHandler extends BaseWTPHandler {

	public CancelCommandWTPHandler(Project project) {
		super(s -> {
			CancelCommandDTO cancelCommandDTO = JsonUtil.getData(s, CancelCommandDTO.class);
			project.getService(SideCarSocketCacheManager.class).cancelCommand(cancelCommandDTO.getCommandId());
			return null;
		});
	}

	@Override
	public String getCommand() {
		return AgenticWebviewCommandEnums.CANCEL_COMMAND.getCommand();
	}
}
