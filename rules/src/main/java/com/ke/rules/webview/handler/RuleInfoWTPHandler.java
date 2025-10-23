package com.ke.rules.webview.handler;

import com.alibaba.fastjson.JSON;
import com.intellij.openapi.project.Project;
import com.intellij.ui.jcef.JBCefJSQuery;
import com.ke.rules.webview.RulesWebviewCommandEnums;
import com.ke.setting.configuration.genral.user.UserConfigState;
import com.ke.webview.communication.handler.wtp.BaseWTPHandler;
import com.ke.rules.ProjectRuleManager;
import com.ke.rules.dto.ProjectRuleDTO;
import com.ke.rules.dto.RuleInfoDTO;
import com.ke.rules.enums.RuleType;
import com.ke.webview.dto.WebviewCallbackResponse;

import java.util.List;


public class RuleInfoWTPHandler extends BaseWTPHandler {


	public RuleInfoWTPHandler(Project project) {
		super(s -> {
			try {
				List<ProjectRuleDTO> projectRules = project.getService(ProjectRuleManager.class).getProjectRules();
				projectRules.removeIf(projectRuleDTO -> RuleType.MANUAL.equals(projectRuleDTO.getType()));
				String appRule = "";
				if (Boolean.TRUE.equals(UserConfigState.getInstance().getState().getEnableGlobalRules())) {
					appRule = project.getService(ProjectRuleManager.class).getAppRule();
				}
				RuleInfoDTO ruleInfoDTO = new RuleInfoDTO(appRule, projectRules);
				return new JBCefJSQuery.Response(JSON.toJSONString(
						WebviewCallbackResponse.builder()
								.success(true)
								.data(JSON.toJSONString(ruleInfoDTO))
								.build()));
			} catch (Exception e) {
				return new JBCefJSQuery.Response(JSON.toJSONString(
						WebviewCallbackResponse.builder()
								.success(false)
								.data(JSON.toJSONString("获取规则信息失败"))
								.build()));
			}
		});
	}

	@Override
	public String getCommand() {
		return RulesWebviewCommandEnums.RULE_INFO_REQ.getCommand();
	}
}
