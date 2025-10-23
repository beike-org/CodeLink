package com.ke.ui;

import com.intellij.ide.ui.LafManager;
import com.intellij.ide.ui.LafManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.SystemInfo;
import com.ke.setting.configuration.genral.user.UserConfigState;
import com.ke.setting.configuration.genral.user.bean.PluginConfigVO;
import com.ke.toolwindow.content.CommonWebviewPanelEnums;
import com.ke.toolwindow.content.BaseH5Panel;
import com.ke.webview.BaseCommandEnums;
import com.ke.utils.PluginUtil;
import com.ke.webview.WebViewManager;
import com.ke.webview.util.PTWUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Objects;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/8/21 17:15
 * @Version 1.0
 * @Description 主题变更处理
 */
public class ThemeChangedListener implements LafManagerListener {

	@Override
	public void lookAndFeelChanged(@NotNull LafManager source) {
		PluginConfigVO pluginConfigVO = UserConfigState.getInstance().getState().getPluginConfigVO();

		String currentTheme = PluginUtil.isLightTheme() ? "light" : "dark";
		if (Objects.nonNull(pluginConfigVO) && !pluginConfigVO.getTheme().equals(currentTheme)) {
			pluginConfigVO.setTheme(currentTheme);
			PTWUtil.sendMessage(BaseCommandEnums.SYNC_PLUGIN_CONFIG, pluginConfigVO);

			//如果是mac,刷新边框
			if (SystemInfo.isMac) {
				Project[] projects = ProjectManager.getInstance().getOpenProjects();
				for (Project project : projects) {
					BaseH5Panel h5Panel = project.getService(WebViewManager.class).getH5Panel(CommonWebviewPanelEnums.KE_COPILOT.getName());
					if (Objects.nonNull(h5Panel)) {
						SwingUtilities.invokeLater(() -> h5Panel.refreshContent(project));
					}
				}
			}
		}
	}
}
