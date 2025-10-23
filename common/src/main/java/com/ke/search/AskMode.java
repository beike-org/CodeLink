package com.ke.search;

import com.jediterm.terminal.TtyConnector;
import lombok.Builder;
import lombok.Data;

import java.awt.*;

/**
 * @Author: zhangshaoxun001
 * @Date: 2024/10/17 12:04
 * @Description
 */
@Data
@Builder
public class AskMode {

	private Type type;

	private TtyConnector ttyConnector;

	private Component component;

	public enum Type {
		NORMAL, TERMINAL, NEW_TERMINAL
	}
}
