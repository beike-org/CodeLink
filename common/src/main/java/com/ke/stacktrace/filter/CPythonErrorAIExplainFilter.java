package com.ke.stacktrace.filter;

import org.jetbrains.annotations.NotNull;

public class CPythonErrorAIExplainFilter extends ErrorAIExplainFilter {

	// 展示位置
	public int calcInlayPosition(@NotNull String line, int entireOffset) {
		return Math.max(entireOffset - 1, 0);
	}

	public boolean isErrorBegin(@NotNull String line, int entireLength) {
		return line.contains("Traceback (most recent call last):");
	}
}

