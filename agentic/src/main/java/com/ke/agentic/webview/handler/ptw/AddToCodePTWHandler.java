package com.ke.agentic.webview.handler.ptw;

import com.ke.toolwindow.content.BaseH5Panel;
import com.ke.webview.communication.handler.ptw.BasePTWHandler;
import com.ke.agentic.webview.handler.AgenticWebviewCommandEnums;


public class AddToCodePTWHandler extends BasePTWHandler {
	public AddToCodePTWHandler(BaseH5Panel h5Panel) {
		super(h5Panel);
	}

	@Override
	public String getCommand() {
		return AgenticWebviewCommandEnums.ADD_TO_CHAT.getCommand();
	}
}
