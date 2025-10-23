package com.ke.search.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.ke.BaseAction;
import com.ke.setting.configuration.genral.user.UserConfigState;
import com.ke.webview.util.PTWUtil;
import com.ke.search.topic.LLMChangeNotifier;
import com.ke.setting.configuration.genral.user.bean.PluginConfigVO;
import com.ke.webview.BaseCommandEnums;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * @Author: zhangshaoxun001
 * @Date: 2024/10/12 17:00
 * @Description
 */
public class BaseLLMAction extends BaseAction {

	private final String llm;

	public BaseLLMAction(@Nullable String text) {
		super(text);
		this.llm = text;
	}

	@Override
	protected void doAction(@NotNull AnActionEvent e) {
		PluginConfigVO pluginConfigVO = UserConfigState.getInstance().getPluginConfig();
		pluginConfigVO.getModelConfiguration().setModelName(llm);
		if (Objects.nonNull(e.getProject())) {
			e.getProject().getMessageBus().syncPublisher(LLMChangeNotifier.LLM_CHANGE_NOTIFIER_TOPIC).change(llm);
			PTWUtil.sendMessage(BaseCommandEnums.SYNC_PLUGIN_CONFIG, pluginConfigVO);
		}
	}


}
