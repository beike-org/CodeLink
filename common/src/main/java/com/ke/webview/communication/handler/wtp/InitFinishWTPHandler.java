package com.ke.webview.communication.handler.wtp;

import com.intellij.openapi.project.Project;
import com.ke.setting.configuration.genral.user.UserConfigState;
import com.ke.toolwindow.content.BaseH5Panel;
import com.ke.webview.BaseCommandEnums;
import com.ke.webview.WebViewManager;
import com.ke.webview.util.PTWUtil;


/**
 * @Author: zhangshaoxun001
 * @Date: 2023/8/16 16:53
 * @Version 1.0
 * @Description webview完成加载时
 */
public class InitFinishWTPHandler extends BaseWTPHandler {

	public InitFinishWTPHandler(BaseH5Panel baseH5Panel, Project project) {
		super(s -> {
			baseH5Panel.notifyPanelLoaded();

			baseH5Panel.doAfterWebviewInitFinish();

			//防止初始化因为时序注入用户信息失败，再注入一次
			PTWUtil.sendMessage(BaseCommandEnums.SYNC_PLUGIN_CONFIG, UserConfigState.getInstance().getState().getPluginConfigVO());
			PTWUtil.sendMessage(BaseCommandEnums.SYNC_PROJECT_CONFIG, project.getService(WebViewManager.class).getWebViewProjectConfig(), project);
			return null;
		});
	}

	@Override
	public String getCommand() {
		return BaseCommandEnums.INIT_FINISH.getCommand();
	}
}
