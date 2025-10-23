package com.ke.stacktrace.filter;

import com.intellij.execution.filters.Filter;
import com.ke.setting.configuration.genral.user.UserConfigState;
import com.ke.stacktrace.render.ConsoleInLayProvider;
import kotlin.collections.CollectionsKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ErrorAIExplainFilter implements Filter {

	public int calcInlayPosition(@NotNull String line, int entireOffset) {
		return Math.max(entireOffset - line.length(), 0);
	}

	@Override
	public @Nullable Result applyFilter(@NotNull String line, int entireLength) {
		if (openStackTraceAnalysis() && isErrorBegin(line, entireLength)) {
			// 如果是错误起始行就增加一个按钮
			int inlayPosition = this.calcInlayPosition(line, entireLength);
			ConsoleInLayProvider result = new ConsoleInLayProvider(inlayPosition);
			return new Filter.Result(CollectionsKt.mutableListOf(result, new Filter.ResultItem(0, 0, null)));
		}
		return null;

	}

	private Boolean openStackTraceAnalysis() {
		return Boolean.TRUE.equals(UserConfigState.getInstance().getState().getStackAnalysis());
	}

	abstract public boolean isErrorBegin(@NotNull String line, int entireLength);

}
