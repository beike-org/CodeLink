package com.ke.stacktrace.filter;

import com.intellij.execution.filters.ExceptionBaseFilterFactory;
import com.intellij.execution.filters.Filter;
import com.intellij.execution.impl.InlayProvider;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.ke.stacktrace.meta.JavaStackTraceInfoProvider;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class JvmErrorAIExplainFilter extends ErrorAIExplainFilter {

	private final Filter defaultJvmExceptionFilter;

	// 上一行是否是错误行,因为单测里没有InlayProvider,所以需要记录上一行是否是错误行
	private Boolean lastIsExceptionLine = false;

	public JvmErrorAIExplainFilter(Project project) {
		this.defaultJvmExceptionFilter = (new ExceptionBaseFilterFactory()).create(project, GlobalSearchScope.allScope(project));

	}

	public boolean isErrorBegin(@NotNull String line, int entireLength) {

		Result filterResult = this.defaultJvmExceptionFilter.applyFilter(line, entireLength);

		//如果filterResult为空,说明不是错误行
		if (filterResult == null) {
			if (!JavaStackTraceInfoProvider.isExceptionLine(line)) {
				lastIsExceptionLine = false;
			}
			return false;
		}

		//如果包含InlayProvider,说明是错误开始行
		List<ResultItem> resultItems = filterResult.getResultItems();
		for (ResultItem item : resultItems) {
			if (item instanceof InlayProvider) {
				lastIsExceptionLine = true;
				return true;
			}
		}

		//如果不包含InlayProvider,但是上一行是错误行,也是错误开始行
		if (!lastIsExceptionLine) {
			lastIsExceptionLine = true;
			return true;
		}

		return false;

	}
}

