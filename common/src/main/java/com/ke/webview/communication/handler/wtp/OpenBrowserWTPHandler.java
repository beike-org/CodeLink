package com.ke.webview.communication.handler.wtp;

import com.intellij.ide.BrowserUtil;
import com.ke.utils.JsonUtil;
import com.ke.webview.BaseCommandEnums;
import com.ke.webview.dto.OpenBrowserDTO;

import java.util.Objects;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/8/16 16:53
 * @Version 1.0
 * @Description
 */
public class OpenBrowserWTPHandler extends BaseWTPHandler {

	public OpenBrowserWTPHandler() {
		super(s -> {
			OpenBrowserDTO openBrowserDTO = JsonUtil.getData(s, OpenBrowserDTO.class);
			if (Objects.nonNull(openBrowserDTO.getUrl())) {
				BrowserUtil.browse(openBrowserDTO.getUrl());
			}
			return null;
		});
	}

	@Override
	public String getCommand() {
		return BaseCommandEnums.OPEN_BROWSER.getCommand();
	}
}
