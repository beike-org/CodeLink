package com.ke.search.topic;

import com.intellij.util.messages.Topic;


/**
 * @Author: zhangshaoxun001
 * @Date: 2023/6/8 16:11
 * @Version 1.0
 * @Description
 */
public interface LLMChangeNotifier {

	Topic<LLMChangeNotifier> LLM_CHANGE_NOTIFIER_TOPIC =
			Topic.create("LLM Change", LLMChangeNotifier.class);


	void change(String model);

}
