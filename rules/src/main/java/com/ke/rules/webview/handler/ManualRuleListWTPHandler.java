package com.ke.rules.webview.handler;

import com.alibaba.fastjson.JSON;
import com.intellij.openapi.project.Project;
import com.intellij.ui.jcef.JBCefJSQuery;
import com.ke.webview.communication.handler.wtp.BaseWTPHandler;
import com.ke.rules.ProjectRuleManager;
import com.ke.rules.dto.ProjectRuleDTO;
import com.ke.rules.enums.RuleType;
import com.ke.webview.dto.WebviewCallbackResponse;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ManualRuleListWTPHandler extends BaseWTPHandler {
	public ManualRuleListWTPHandler(Project project) {
		super(s -> {
			try {
				List<ProjectRuleDTO> projectRules = project.getService(ProjectRuleManager.class).getProjectRules();
				List<ProjectRuleDTO> collect = projectRules.stream().filter(projectRuleDTO -> RuleType.MANUAL.equals(projectRuleDTO.getType())).collect(Collectors.toList());
				return new JBCefJSQuery.Response(JSON.toJSONString(
						WebviewCallbackResponse.builder()
								.success(true)
								.data(JSON.toJSONString(collect))
								.build()));
			} catch (Exception e) {
				return new JBCefJSQuery.Response(JSON.toJSONString(
						WebviewCallbackResponse.builder()
								.success(false)
								.data(JSON.toJSONString(Collections.emptyList()))
								.build()));
			}
		});
	}

	@Override
	public String getCommand() {
		return "";
	}
}
