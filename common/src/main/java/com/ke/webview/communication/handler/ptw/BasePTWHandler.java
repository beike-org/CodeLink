package com.ke.webview.communication.handler.ptw;

import com.ke.webview.dto.WPCommunicateDTO;
import com.ke.toolwindow.content.BaseH5Panel;
import lombok.AllArgsConstructor;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/8/16 16:44
 * @Version 1.0
 * @Description 插件发送到web的消息处理器
 */
@AllArgsConstructor
public abstract class BasePTWHandler {

	private BaseH5Panel h5Panel;

	public void execute(Object data) {
		h5Panel.executeJS(WPCommunicateDTO.builder().command(getCommand()).data(data).build());
	}

	public abstract String getCommand();

}
