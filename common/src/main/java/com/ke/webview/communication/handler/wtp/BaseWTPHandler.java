package com.ke.webview.communication.handler.wtp;

import com.intellij.ui.jcef.JBCefJSQuery;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Function;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/8/16 16:44
 * @Version 1.0
 * @Description web发送到插件的消息处理器
 */
public abstract class BaseWTPHandler {

	private final Function<? super String, ? extends JBCefJSQuery.Response> handler;

	public BaseWTPHandler(@NotNull Function<? super String, ? extends JBCefJSQuery.Response> handler) {
		this.handler = handler;
	}

	public JBCefJSQuery.Response execute(Object data) {
		if (Objects.isNull(data)) {
			return handler.apply("");
		} else {
			return handler.apply(data.toString());
		}
	}

	public abstract String getCommand();

}
