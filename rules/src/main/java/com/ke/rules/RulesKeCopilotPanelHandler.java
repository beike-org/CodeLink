package com.ke.rules;

import com.intellij.openapi.project.Project;
import com.ke.rules.webview.RulesWebviewCommandEnums;
import com.ke.rules.webview.handler.*;
import com.ke.toolwindow.content.BaseH5Panel;
import com.ke.webview.communication.handler.KeCopilotPanelHandler;
import com.ke.webview.communication.handler.ptw.BasePTWHandler;
import com.ke.webview.communication.handler.wtp.BaseWTPHandler;

import java.util.Map;


/**
 * @Author: zhangshaoxun001
 * @Date: 2024/1/2 16:24
 * @Version 1.0
 * @Description
 */
public class RulesKeCopilotPanelHandler implements KeCopilotPanelHandler {

	/**
	 * 获取PTWHandler
	 */
	@Override
	public Map<String, BasePTWHandler> getPTWHandler(BaseH5Panel panel, Project project) {
		return Map.of(
				RulesWebviewCommandEnums.SEARCH_FILE_LIST_RESP.getCommand(), new SearchFileListRespHandler(panel)
		);
	}

	/**
	 * 获取WTPHandler
	 */
	@Override
	public Map<String, BaseWTPHandler> getWTPHandler(BaseH5Panel h5Panel, Project project) {
		return Map.of(RulesWebviewCommandEnums.RULE_INFO_REQ.getCommand(), new RuleInfoWTPHandler(project),
				RulesWebviewCommandEnums.MANUAL_RULE_LIST_REQ.getCommand(), new ManualRuleListWTPHandler(project),
				RulesWebviewCommandEnums.ADD_PROJECT_RULES.getCommand(), new AddProjectRulesWTPHandler(project),
				RulesWebviewCommandEnums.SEARCH_FILE_LIST.getCommand(), new SearchFileListWTPHandler(project),
				RulesWebviewCommandEnums.GET_DEFAULT_FILE_LIST.getCommand(), new DefaultFileListWTPHandler(project));
	}
}
