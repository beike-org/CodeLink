package com.ke.webview.communication.handler.wtp;

import com.ke.setting.configuration.genral.user.UserConfigState;
import com.ke.setting.configuration.genral.user.bean.PluginConfigVO;
import com.ke.utils.BeanUtil;
import com.ke.utils.JsonUtil;
import com.ke.webview.BaseCommandEnums;
import com.ke.webview.util.PTWUtil;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/8/16 16:53
 * @Version 1.0
 * @Description
 */
public class SetPluginConfigWTPHandler extends BaseWTPHandler {

	public SetPluginConfigWTPHandler() {
		super(s -> {
			PluginConfigVO pluginConfigVO = UserConfigState.getInstance().getState().getPluginConfigVO();
			pluginConfigVO = pluginConfigVO == null ? new PluginConfigVO() : pluginConfigVO;
			BeanUtil.copyNonNullProperties(JsonUtil.getData(s, PluginConfigVO.class), pluginConfigVO);
			UserConfigState.getInstance().getState().setPluginConfigVO(pluginConfigVO);
			PTWUtil.sendMessage(BaseCommandEnums.SYNC_PLUGIN_CONFIG, pluginConfigVO);
			return null;
		});
	}

	@Override
	public String getCommand() {
		return BaseCommandEnums.SET_PLUGIN_CONFIG.getCommand();
	}
}
