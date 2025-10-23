package com.ke.webview.topic;

import com.intellij.util.messages.Topic;
import com.ke.toolwindow.content.BaseH5Panel;
import com.ke.webview.WebviewCommand;

import java.util.List;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/6/8 16:11
 * @Version 1.0
 * @Description 从插件发送消息给webview
 */
public interface PTWCommandNotifier {

	Topic<PTWCommandNotifier> PTW_COMMAND_TOPIC =
			Topic.create("CodeLink send command to webview", PTWCommandNotifier.class);

	void sendCommand(WebviewCommand command, Object data, List<BaseH5Panel> receivers);

}
