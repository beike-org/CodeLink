package com.ke.rules.webview.handler;

import com.ke.rules.webview.RulesWebviewCommandEnums;
import com.ke.toolwindow.content.BaseH5Panel;
import com.ke.webview.communication.handler.ptw.BasePTWHandler;

public class SearchFileListRespHandler extends BasePTWHandler {

	public SearchFileListRespHandler(BaseH5Panel h5Panel) {
		super(h5Panel);
	}

	@Override
	public String getCommand() {
		return RulesWebviewCommandEnums.SEARCH_FILE_LIST_RESP.getCommand();
	}

}
