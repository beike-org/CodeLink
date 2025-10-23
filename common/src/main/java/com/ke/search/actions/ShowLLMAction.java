package com.ke.search.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.popup.ListPopup;
import com.ke.BaseAction;
import com.ke.service.llm.LLMService;
import com.ke.setting.configuration.genral.user.UserConfigState;
import com.ke.setting.configuration.genral.user.bean.ModelConfiguration;
import com.ke.utils.ComponentUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.Set;

/**
 * @Author: zhangshaoxun001
 * @Date: 2024/10/12 17:05
 * @Description
 */
public class ShowLLMAction extends BaseAction {

	public ShowLLMAction() {
		super(UserConfigState.getInstance().getPluginConfig().getModelConfiguration().getModelName());
	}

	@Override
	protected void doAction(@NotNull AnActionEvent e) {
		ModelConfiguration modelConfiguration = UserConfigState.getInstance().getPluginConfig().getModelConfiguration();
		if (Objects.isNull(modelConfiguration)){
			return;
		}
		String selectedProvider = modelConfiguration.getSelectedProvider();
		if (StringUtils.isBlank(selectedProvider)){
			return;
		}

        Set<String> llMs = ApplicationManager.getApplication().getService(LLMService.class).getLLMs(selectedProvider);
		DefaultActionGroup defaultActionGroup = new DefaultActionGroup();
		llMs.forEach(llm -> defaultActionGroup.add(new BaseLLMAction(llm)));
		ListPopup popup = ComponentUtil.createListPopup("", defaultActionGroup, e.getDataContext());
		if (e.getInputEvent() instanceof MouseEvent mouseEvent) {
			Point clickPoint = mouseEvent.getPoint();
			SwingUtilities.convertPointToScreen(clickPoint, mouseEvent.getComponent());
			popup.showInScreenCoordinates(mouseEvent.getComponent(), clickPoint);
		}
	}

	@Override
	public void update(@NotNull AnActionEvent e) {
		super.update(e);
	}
}
