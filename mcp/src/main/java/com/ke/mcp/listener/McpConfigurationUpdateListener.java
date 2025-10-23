package com.ke.mcp.listener;

import com.intellij.util.messages.Topic;

public interface McpConfigurationUpdateListener {
	Topic<McpConfigurationUpdateListener> TOPIC = Topic.create("MCP_CONFIGURATION_CHANGED", McpConfigurationUpdateListener.class);

	void onConfigurationChanged();
}
