package com.ke.mcp;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public class McpIcons {
	public static final Icon ENABLE = load("/images/mcpIcon/enable.svg");
	public static final Icon DISABLE = load("/images/mcpIcon/disable.svg");
	public static final Icon RECONNECT = load("/images/mcpIcon/reconnect.svg");
	public static final Icon EDIT = load("/images/mcpIcon/edit.svg");
	public static final Icon DELETE = load("/images/mcpIcon/delete.svg");

	public static final Icon GREEN = load("/images/mcpIcon/green.svg");
	public static final Icon RED = load("/images/mcpIcon/red.svg");
	public static final Icon GREY = load("/images/mcpIcon/grey.svg");

	private static Icon load(String path) {
		return IconLoader.getIcon(path, McpIcons.class);
	}
}

