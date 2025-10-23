package com.ke.agentic.topic;

import com.intellij.util.messages.Topic;

/**
 * @Author: zhangshaoxun001
 * @Date: 2025/3/11 15:27
 * @Description
 */
public interface SideCarNotifier {

	Topic<SideCarNotifier> SIDE_CAR_NOTIFIER_TOPIC =
			Topic.create("Sidecar Notifier", SideCarNotifier.class);

	/**
	 * 拉起Socket服务
	 */
	void startSocket(Integer port);


	/**
	 * Agent通过健康检查
	 */
	void agentReady();
}
