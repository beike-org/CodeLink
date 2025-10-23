package com.ke.webview.communication.handler.wtp;

import com.intellij.openapi.project.Project;
import com.ke.utils.BeanUtil;
import com.ke.utils.JsonUtil;
import com.ke.webview.BaseCommandEnums;
import com.ke.webview.WebViewManager;
import com.ke.webview.WebViewProjectConfig;
import com.ke.webview.util.PTWUtil;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/8/16 16:53
 * @Version 1.0
 * @Description 设置项目级配置
 */
public class SetProjectConfigWTPHandler extends BaseWTPHandler {

	public SetProjectConfigWTPHandler(Project project) {
		super(s -> {
			WebViewManager webViewManager = project.getService(WebViewManager.class);
			WebViewProjectConfig webViewProjectConfig = webViewManager.getWebViewProjectConfig();
			BeanUtil.copyNonNullProperties(JsonUtil.getData(s, WebViewProjectConfig.class), webViewProjectConfig);
			webViewManager.setWebViewProjectConfig(webViewProjectConfig);
			PTWUtil.sendMessage(BaseCommandEnums.SYNC_PROJECT_CONFIG, webViewProjectConfig, project);
			return null;
		});
	}

	@Override
	public String getCommand() {
		return BaseCommandEnums.SET_PROJECT_CONFIG.getCommand();
	}
}
