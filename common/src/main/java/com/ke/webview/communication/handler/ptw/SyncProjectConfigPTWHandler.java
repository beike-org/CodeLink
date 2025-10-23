package com.ke.webview.communication.handler.ptw;

import com.ke.toolwindow.content.BaseH5Panel;
import com.ke.webview.BaseCommandEnums;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/8/28 15:32
 * @Version 1.0
 * @Description 同步项目级配置给webview
 */
public class SyncProjectConfigPTWHandler extends BasePTWHandler {


	public SyncProjectConfigPTWHandler(BaseH5Panel h5Panel) {
		super(h5Panel);
	}

	@Override
	public String getCommand() {
		return BaseCommandEnums.SYNC_PROJECT_CONFIG.getCommand();
	}
}
